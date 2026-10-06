package com.cimdy.coe.data.helper.engine.ground;

import com.cimdy.coe.data.codec.skill.phase.action.movement.CreateGroundAction;
import com.cimdy.coe.data.codec.support.stat.effect.grant.CreateGroundOnAilmentEffect;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.cimdy.coe.data.codec.support.math.context.query.Selector;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.status.registry.ground.GroundFieldData;
import com.cimdy.coe.data.codec.support.status.registry.ground.GroundFieldSaveData;
import com.cimdy.coe.data.codec.support.status.registry.ground.GroundTargeting;
import com.cimdy.coe.data.codec.support.stat.effect.status.StatusStatEffect;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.network.sync.GroundFieldSyncPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 地面场（Ground Field）服务端运行系统——本类即地面子系统的承载实现，不依赖任何实体。
 *
 * <p><b>设计要点（对齐用户裁定：地面脱离技能/施法者，服务端运算、客户端简单渲染）</b>：
 * <ul>
 *   <li>每维度状态存于 {@code Level} 附件 {@link AttachmentRegistry#GROUND_FIELDS}
 *       （NeoForge Data Attachment，随维度存档，重启仍在）；不使用任何实体 / 方块实体，避免卡顿。</li>
 *   <li>每 tick 遍历 {@code getAllLevels()}，对每片场：递减剩余时间、每 {@value #APPLY_INTERVAL} tick
 *       刷新一次范围内实体的统计贡献（镜像 PresenceAura 的「持续刷新 + 短时长」语义）。</li>
 *   <li><b>通用地面效果</b>：地面的效果即 {@link GroundFieldData#stats()}（{@link StatEntry} 列表），
 *       由 {@link #applyGround} 按 {@link GroundTargeting} 阵营门控后作用于范围内目标：
 *       非触发型（连续属性加成，如锯齿承伤 +20%）注入 {@code STAT_STORE} 由引擎逐 tick 持续读取；
 *       触发型（异常/减益，如点燃/感电/冰缓）每节拍<b>主动施加</b>为对应状态——「持续常驻异常」，
 *       站在地面即持续获得异常，不依赖被击中触发。全程不使用「存在性状态」间接层（R3 安全）。</li>
 *   <li>客户端渲染数据经 {@code GroundFieldSyncPacket} 包周期性全量下发该维度玩家（覆盖延迟进入/登录客户端），
 *       客户端自管粒子 + 贴地网格。</li>
 * </ul>
 *
 * <p><b>创建入口</b>：{@link #createField} 由 {@code CreateGroundAction} /
 * {@code CreateGroundOnAilmentEffect} / {@code CreateGroundEffect}（词缀）调用。
 */
@EventBusSubscriber(modid = CraftOfExile.MODID)
public final class GroundFieldManager {

    /** 统计贡献刷新节拍（tick）：5 tick。 */
    private static final int APPLY_INTERVAL = 5;
    /** 注入 STAT_STORE 的持续时间（tick）：略大于 APPLY_INTERVAL，保证在半径内期间持续覆盖、离开后自然过期。 */
    private static final int STAT_REFRESH_TICKS = APPLY_INTERVAL + 3;
    /** 客户端同步节拍（tick）：全维度地面场全量下发，覆盖迟到的客户端。 */
    private static final int SYNC_INTERVAL = 10;

    private GroundFieldManager() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            tickLevel(level);
        }
    }

    /**
     * 创建一片地面场（供技能动作 / 异常触发 / 词缀调用）。
     * {@code ownerUuid} 为 {@code null} 时记 {@link GroundFieldData#NO_OWNER} 哨兵，阵营取 {@link Camp#NONE}。
     */
    public static void createField(ServerLevel level, Vec3 center, float radius, int durationTicks,
                                   @Nullable UUID ownerUuid, GroundTargeting targeting, int color,
                                   List<StatEntry> stats) {
        Camp camp = Camp.NONE;
        LivingEntity ownerLiving = null;
        if (ownerUuid != null) {
            Entity e = level.getEntity(ownerUuid);
            if (e instanceof LivingEntity le) { camp = le.getData(AttachmentRegistry.CAMP); ownerLiving = le; }
        }
        // 地面场半径经「范围属性」作用域注入：半径 = 配置半径 × EFFECT_AREA（{area} 作用域），
        // 与 Orb/Ball/Wave 等技能投递同源，非存在范围（地面是区域效果）统一走范围属性作用域。
        double areaMult = ownerLiving != null
                ? AilmentHelper.withScope(ownerLiving, Set.of(GateTag.AREA), AttributeRegistry.EFFECT_AREA) : 1.0;
        GroundFieldData data = new GroundFieldData(
                UUID.randomUUID(), center, Math.max(0.5F, (float) (radius * areaMult)),
                Math.max(1, durationTicks), ownerUuid != null ? ownerUuid : GroundFieldData.NO_OWNER,
                camp, targeting, color, List.copyOf(stats));
        GroundFieldSaveData cur = level.getData(AttachmentRegistry.GROUND_FIELDS);
        List<GroundFieldData> next = new ArrayList<>(cur.fields());
        next.add(data);
        level.setData(AttachmentRegistry.GROUND_FIELDS, new GroundFieldSaveData(next));
    }

    private static void tickLevel(ServerLevel level) {
        GroundFieldSaveData data = level.getData(AttachmentRegistry.GROUND_FIELDS);
        if (data.fields().isEmpty()) return;

        long gameTime = level.getGameTime();
        boolean applyTick = gameTime % APPLY_INTERVAL == 0;

        List<GroundFieldData> next = new ArrayList<>();
        boolean removed = false;
        for (GroundFieldData f : data.fields()) {
            int remaining = f.remainingTicks() - 1;
            if (remaining <= 0) { removed = true; continue; }
            GroundFieldData nf = new GroundFieldData(f.id(), f.center(), f.radius(), remaining,
                    f.ownerUuid(), f.camp(), f.targeting(), f.color(), f.stats());
            next.add(nf);
            if (applyTick) applyGround(level, nf);
        }

        // 结构变化（到期移除）或到期标记刷新都落盘，保证重启后仍按剩余时间推进。
        if (removed || applyTick) {
            level.setData(AttachmentRegistry.GROUND_FIELDS, new GroundFieldSaveData(next));
        }

        // 周期性把本维度地面场全量同步给该维度内玩家（含延迟进入/登录客户端）。
        if (gameTime % SYNC_INTERVAL == 0 || removed) {
            syncToClients(level, next);
        }
    }

    /**
     * 对半径内活体按阵营门控后，把地面 {@code stats} 注入其 {@code STAT_STORE}（镜像 PresenceAura 刷新范式）：
     * 每 {@value #APPLY_INTERVAL} tick 以 {@value #STAT_REFRESH_TICKS} 时长重加（同键覆盖刷新），
     * 离开半径后停止刷新，由 {@code StatStore.tick()} 自动过期。
     */
    private static void applyGround(ServerLevel level, GroundFieldData f) {
        if (f.stats().isEmpty()) return;
        AABB box = AABB.ofSize(f.center(), f.radius() * 2.0, 2.0, f.radius() * 2.0);
        ExpressionContext base = ExpressionContext.of(level, null);
        Selector<LivingEntity> sel = Selector.living(base.queryEntities(EntityQuery.box(box)));
        // 地面所有者（异常来源）：无主/离线则为 null（状态 source 置空，符合「地面脱离施法者」裁定）。
        LivingEntity owner = f.ownerUuid().equals(GroundFieldData.NO_OWNER) ? null
                : level.getEntity(f.ownerUuid()) instanceof LivingEntity le ? le : null;
        for (LivingEntity living : sel.result()) {
            double dx = living.getX() - f.center().x;
            double dz = living.getZ() - f.center().z;
            if (dx * dx + dz * dz > (double) (f.radius() * f.radius())) continue;
            Camp targetCamp = living.getData(AttachmentRegistry.CAMP);
            if (!f.targeting().affects(f.camp(), targetCamp)) continue;
            // 非触发型（连续属性加成，如 jagged 的承伤 +20%）：照旧注入 STAT_STORE，由引擎逐 tick 持续读取（R3 安全）。
            StatStore store = living.getData(AttachmentRegistry.STAT_STORE);
            Map<String, StatEntry> keyed = new LinkedHashMap<>();
            int i = 0;
            for (StatEntry s : f.stats()) {
                if (!s.isTrigger()) keyed.put("ground:" + f.id() + ":" + (i++), s);
            }
            if (!keyed.isEmpty()) {
                living.setData(AttachmentRegistry.STAT_STORE, store.addModifiers(keyed, STAT_REFRESH_TICKS));
            }
            // 触发型（异常/减益，如点燃/感电/冰缓）：每节拍主动施加到范围内目标——「持续常驻异常」，
            // 站在地面即持续获得异常，不再依赖被击中触发（落地同 StatusStatEffect.onTrigger 的量纲推导）。
            for (StatEntry s : f.stats()) {
                if (!s.isTrigger() || !(s.effect() instanceof StatusStatEffect se)) continue;
                double v = s.evaluateAmount(ExpressionContext.of(level, living));
                if (v <= 0) continue;
                float magnitude = se.aspect() == StatusStatEffect.Aspect.MAGNITUDE ? (float) v : 1.0F;
                int duration = se.aspect() == StatusStatEffect.Aspect.DURATION ? (int) v
                        : se.status().defaultDurationTicks;
                int stacks = se.aspect() == StatusStatEffect.Aspect.STACKS ? (int) v : 1;
                StatusEngine.apply(living, se.status(), owner, magnitude, duration, stacks);
            }
        }
    }

    private static void syncToClients(ServerLevel level, List<GroundFieldData> fields) {
        PacketDistributor.sendToPlayersInDimension(level,
                new GroundFieldSyncPacket(List.copyOf(fields)));
    }
}
