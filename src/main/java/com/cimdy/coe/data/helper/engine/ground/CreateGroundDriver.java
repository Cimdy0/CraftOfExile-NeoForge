package com.cimdy.coe.data.helper.engine.ground;

import com.cimdy.coe.event.combat.AttributeEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.status.registry.ground.GroundTypes;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.effect.grant.CreateGroundStatEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 地面生成引擎：每 tick 驱动持有者 STAT_STORE 中的 {@link CreateGroundStatEffect} 条目，
 * 在持有者脚下周期性生成地面场（见 {@link GroundFieldManager}）。<b>独立管道</b>，
 * 不混入 PresenceAura（光环状态施加），与用户裁定「地面运行机制单独运行」一致。
 *
 * <p>机制：每实体每 tick 调用（{@code AttributeEvent.EntityTickEvent.Pre}，紧邻
 * PresenceAura.tick / MarkBlindAura.tick）。无 store / 无地面生成条目 / 服务端外 零开销短路；
 * 对每个地面生成条目按 {@code intervalTicks} 节流，到拍即在脚下
 * {@link GroundFieldManager#createField} 投放地面，效果由
 * {@link GroundTypes#def} 解析（直接注入范围内实体 STAT_STORE，无存在性状态层）。
 *
 * <p>扫描策略：GROUND 型效果不单独建索引（避免 StatStore 构造器膨胀），此处对持有者条目
 * 全量扫描并 instanceof 过滤——持有者条目数有限，每 tick 成本恒定且与 PresenceAura
 * 同量级；若未来地面生成词缀普遍化，可升级为 StatStore 预建 groundIndex（同 auraIndex 范式）。
 */
public final class CreateGroundDriver {

    private CreateGroundDriver() {
    }

    /** 每实体每 tick 调用（AttributeEvent.EntityTickEvent.Pre）。无 store / 无地面生成条目 / 客户端 零开销短路。 */
    public static void tick(LivingEntity living) {
        if (living.level().isClientSide()) return;
        if (!living.hasData(AttachmentRegistry.STAT_STORE)) return;
        StatStore store = living.getData(AttachmentRegistry.STAT_STORE);
        for (StatEntry e : store.keyedEntries().values()) {
            if (!(e.effect() instanceof CreateGroundStatEffect eff)) continue;
            // 刷新节流：按条目自身节拍，避免每 tick 重复投放（地面场自身有 duration 计时域）。
            if (living.tickCount % eff.intervalTicks() != 0) continue;
            spawn(living, eff);
        }
    }

    /** 在持有者脚下投放一片地面场。 */
    private static void spawn(LivingEntity source, CreateGroundStatEffect eff) {
        if (!(source.level() instanceof ServerLevel serverLevel)) return;
        Vec3 pos = source.position();
        GroundTypes.GroundDef def = GroundTypes.def(eff.groundId());
        GroundFieldManager.createField(serverLevel, pos, eff.radius(), (int) eff.duration(),
                source.getUUID(), def.targeting(), def.color(), def.stats());
    }
}
