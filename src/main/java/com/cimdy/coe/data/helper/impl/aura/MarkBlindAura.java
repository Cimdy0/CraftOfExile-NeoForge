package com.cimdy.coe.data.helper.impl.aura;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.event.combat.AttributeEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.helper.engine.aura.AuraScan;
import com.cimdy.coe.data.helper.engine.periodic.PeriodicDriver;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 标记致盲光环引擎：持有 {@code ENEMIES_NEAR_ENEMIES_YOU_MARK_ARE_BLINDED} 的实体（玩家/图腾等）
 * 每 {@code INTERVAL} tick 驱动一轮：
 * <ul>
     *   <li>① 找出「我标记的敌人」——持有 {@link com.cimdy.coe.data.codec.support.status.def.StatusKind#MARK} 类状态（动态注册、存于 StatusStore）且 source == 持有者 UUID 的敌方实体
     *       （状态由 Mark 技能施放时经 {@link com.cimdy.coe.data.helper.impl.aura.MarkHelper#applyMarkToNearby} 施加到周围敌人，source 即标记者）；</li>
 *   <li>② 对每个被标记敌人，球形扫描其邻近实体，对<b>敌对阵营</b>者施加 {@link DebuffStatusTypes#BLIND}。</li>
 * </ul>
 * 全部复用现成基元（MARK / BLIND / EntityQuery 同款 getEntitiesOfClass 扫描 / StatusEngine.apply），
 * 零新增事件订阅，R3 安全。
 */
public final class MarkBlindAura {

    private MarkBlindAura() {
    }

    /** 「near」判定半径（格）：被标记敌周边此范围内的敌人被致盲。 */
    private static final double MARK_BLIND_RADIUS = 5.0;
    /** 持有者周边扫描半径（格）：用于收集「我标记的敌人」（覆盖典型 Presence 范围）。 */
    private static final double MARKED_SEARCH_RADIUS = 24.0;
    /** 刷新节流：每 {@code INTERVAL} tick 执行一轮（状态时长 80 > 间隔 → 持续刷新 = 常驻光环）。 */
    private static final int INTERVAL = 20;
    // 致盲强度/时长不再本地镜像常量：统一走 AilmentHelper.blindMagnitude / blindDurationTicks
    // （属性驱动，与命中致盲同口径；镜像常量会绕过 inc_blind_effect 等 Stat，属 #48 同型死线）。

    /** 每实体每 tick 调用（AttributeEvent.EntityTickEvent.Pre，紧邻 PresenceAura.tick）。无 store / 无该 Stat 零开销短路。 */
    public static void tick(LivingEntity living) {
        if (living.level().isClientSide()) return;
        // 门控：零基 flag 属性——仅持有该 Stat 的实体（attribute>0）才执行驱动
        if (living.getAttributeValue(AttributeRegistry.MARK_BLINDED) <= 0) return;
        if (!PeriodicDriver.beat(living.tickCount, INTERVAL)) return;

        Camp myCamp = living.getData(AttachmentRegistry.CAMP);
        if (myCamp == Camp.NONE) return;
        // 光环半径经「范围属性」作用域注入：致盲半径 = 基础 5.0 × EFFECT_AREA（{area} 作用域），
        // 与技能面积加成同源；非存在范围（光环）统一走范围属性作用域，不新建 aura_radius 轴。
        double blindRadius = MARK_BLIND_RADIUS
                * AilmentHelper.withScope(living, Set.of(GateTag.AREA), AttributeRegistry.EFFECT_AREA);
        UUID me = living.getUUID();
        Level level = living.level();

        // ① 收集「我标记的敌人」：持有者周边带 MARK 且来源为我
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class,
                living.getBoundingBox().inflate(MARKED_SEARCH_RADIUS));
        for (LivingEntity marked : candidates) {
            if (marked == living || marked.isDeadOrDying()) continue;
            if (!MarkHelper.isMarkedBy(marked, me, level.getGameTime())) continue;

            // ② 该标记敌人附近的所有敌人均致盲（目标解析走公共扫描器：球心=被标记敌人，
            //    参照阵营=持有者阵营——持有者自身因阵营等于参照而被剔除，无需显式排除）
            for (LivingEntity n : AuraScan.hostilesAround(marked, blindRadius, myCamp)) {
                StatusEngine.apply(n, StatusDefs.BLIND, living,
                        AilmentHelper.blindMagnitude(living, n), AilmentHelper.blindDurationTicks(living, n), 1, true);
            }
        }
    }
}
