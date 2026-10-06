package com.cimdy.coe.data.helper.engine;

import com.cimdy.coe.event.world.MovingLightEvent;
import com.cimdy.coe.registry.AttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.cimdy.coe.data.codec.support.stat.StatIds;

/**
 * 移动光源引擎（服务端）。
 *
 * <p><b>为什么自建：</b>原版没有「光半径」属性，方块光只能由方块自身发出。本引擎把
 * {@link AttributeRegistry#LIGHT_RADIUS}（由 {@code StatIds.INC_LIGHT_RADIUS} 经
 * {@code EFF_ADD_VALUE_ATTRIBUTE_COE_LIGHT_RADIUS} 平加）翻译为一枚跟随实体移动的
 * {@link Blocks#LIGHT} <b>锚点方块</b>。原版方块光每格衰减 1，故「光照等级 N」天然等价
 * 「半径 N 格」——属性值即光照等级，消费端零换算。
 *
 * <p><b>为什么用锚点方块而非直接写光引擎：</b>{@code LevelLightEngine} 没有「注入无方块来源的光」
 * 的公开入口；绕过方块状态直填光图会在区块保存/重算（{@code LightEngine} 重建、区块重新加载）时
 * 被抹掉，且与客户端光图不一致。反之 {@link Blocks#LIGHT} 是原版为此设计的方块：
 * 不可见（{@code RenderShape.INVISIBLE}）、无碰撞箱、无掉落、光照等级 0-15 可调。
 * 经 {@code Level#setBlock} 落子后，{@code LevelChunk.setBlockState} 会自动
 * {@code getLightEngine().checkBlock(pos)}，由原版光引擎负责传播与区块重光照——不会破坏区块光照。
 *
 * <p><b>安全纪律（防世界破坏）：</b>
 * <ol>
 *   <li><b>只占空气</b>：目标位非 {@code isAir()} 且非自家锚点时直接放弃本 tick，绝不覆盖玩家方块；</li>
 *   <li><b>只撤自家锚点</b>：撤销前校验该位仍是 {@link Blocks#LIGHT} 且光照等级与落子时记录一致，
 *       否则视为已被玩家改动，原样留下；</li>
 *   <li><b>不强加载区块</b>：落子/撤销前均过 {@code Level#isLoaded}，绝不因光源而拉起区块；</li>
 *   <li><b>UPDATE_CLIENTS 而非 UPDATE_ALL</b>：只同步客户端 + 触发光引擎（光照更新在
 *       {@code LevelChunk.setBlockState} 内部，与 update flag 无关），跳过邻居更新，
 *       故不会惊动红石 / 沙砾掉落等相邻机制。</li>
 * </ol>
 */
public final class MovingLightManager {

    /** 节流：每 3 tick（约 6.7 次/秒）对齐一次锚点，冲刺时最大滞后约 1 格；同时压制光引擎抖动。 */
    private static final int THROTTLE_TICKS = 3;

    /** 半径 &gt;0 时至少给 1 级光，避免 0.4 格这类小值四舍五入成「有属性但不发光」。 */
    private static final int MIN_LIGHT_LEVEL = 1;

    /** 已落子的锚点：实体 UUID → 所在维度 + 坐标 + 落子时的光照等级。 */
    private record Anchor(ResourceKey<Level> dimension, BlockPos pos, int lightLevel) {}

    private static final Map<UUID, Anchor> ANCHORS = new ConcurrentHashMap<>();

    private MovingLightManager() {
    }

    /**
     * 每 tick 由 {@code MovingLightEvent} 调用（内部自行节流）。
     * 读 {@link AttributeRegistry#LIGHT_RADIUS} 定尺寸；半径归零 / 实体死亡即撤锚。
     */
    public static void tick(LivingEntity living) {
        if (!(living.level() instanceof ServerLevel level)) return;
        if (living.tickCount % THROTTLE_TICKS != 0) return;

        int lightLevel = desiredLightLevel(living);
        if (lightLevel <= 0) {
            clear(living);
            return;
        }

        BlockPos target = BlockPos.containing(living.getX(), living.getEyeY(), living.getZ());
        Anchor current = ANCHORS.get(living.getUUID());
        // 未换格、未换维度、亮度未变 → 零操作（静止实体不产生任何方块/光引擎调用）
        if (current != null
                && current.dimension().equals(level.dimension())
                && current.pos().equals(target)
                && current.lightLevel() == lightLevel) {
            return;
        }

        // 先撤旧锚点（可能位于另一维度，故按记录的维度键回查）
        if (current != null) removeAnchor(level.getServer(), current);

        if (placeAnchor(level, target, lightLevel)) {
            ANCHORS.put(living.getUUID(), new Anchor(level.dimension(), target, lightLevel));
        } else {
            // 目标位被占（实体卡在方块里 / 水中 / 区块未加载）：本 tick 不发光，下次重试
            ANCHORS.remove(living.getUUID());
        }
    }

    /** 实体离场（死亡移除 / 换维度 / 登出 / 区块卸载卸实体）时撤锚，防锚点泄漏。 */
    public static void clear(LivingEntity living) {
        Anchor anchor = ANCHORS.remove(living.getUUID());
        if (anchor != null) removeAnchor(living.level().getServer(), anchor);
    }

    /** 服务器关停时清空全部锚点，避免存档里残留隐形光块。 */
    public static void clearAll(MinecraftServer server) {
        for (Anchor anchor : ANCHORS.values()) removeAnchor(server, anchor);
        ANCHORS.clear();
    }

    /** 属性值 → 光照等级（属性上限已在注册处钳到 15 = {@link LightBlock#MAX_LEVEL}）。 */
    private static int desiredLightLevel(LivingEntity living) {
        if (living.isRemoved() || living.isDeadOrDying()) return 0;
        if (!living.getAttributes().hasAttribute(AttributeRegistry.LIGHT_RADIUS)) return 0;
        double radius = living.getAttributeValue(AttributeRegistry.LIGHT_RADIUS);
        if (radius <= 0.0) return 0;
        return Math.min(LightBlock.MAX_LEVEL, Math.max(MIN_LIGHT_LEVEL, (int) Math.round(radius)));
    }

    /** 落子；仅当目标位是空气或自家锚点时才写入。返回是否成功持有该位。 */
    private static boolean placeAnchor(ServerLevel level, BlockPos pos, int lightLevel) {
        if (!level.isLoaded(pos)) return false;
        BlockState existing = level.getBlockState(pos);
        if (!existing.isAir() && !isAnchorState(existing)) return false;

        BlockState anchor = Blocks.LIGHT.defaultBlockState()
                .setValue(LightBlock.LEVEL, lightLevel)
                .setValue(LightBlock.WATERLOGGED, false);
        if (existing == anchor) return true;
        return level.setBlock(pos, anchor, Block.UPDATE_CLIENTS);
    }

    /** 撤锚；仅当该位仍是我们落下的那枚锚点（同方块 + 同光照等级）时才清除。 */
    private static void removeAnchor(MinecraftServer server, Anchor anchor) {
        if (server == null) return;
        ServerLevel level = server.getLevel(anchor.dimension());
        if (level == null || !level.isLoaded(anchor.pos())) return;
        BlockState state = level.getBlockState(anchor.pos());
        if (!isAnchorState(state) || state.getValue(LightBlock.LEVEL) != anchor.lightLevel()) return;
        level.setBlock(anchor.pos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    /** 是否为本引擎形态的锚点方块（非含水，含水者视为玩家手放的光块，不动）。 */
    private static boolean isAnchorState(BlockState state) {
        return state.is(Blocks.LIGHT) && !state.getValue(LightBlock.WATERLOGGED);
    }
}
