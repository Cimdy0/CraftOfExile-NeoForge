package com.cimdy.coe.data.helper.impl.aura;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.helper.engine.aura.AuraScan;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.helper.engine.periodic.PeriodicDriver;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Set;
import com.cimdy.coe.data.helper.engine.LivingTickDriver;

/**
 * 举盾前方痿顿光环：持有
 * {@code APPLY_DEBILITATE_TO_ENEMIES_METRES_IN_FRONT_OF_YOU_WHILE_YOUR_SHIELD_IS_RAISED}
 * INERT 标记的实体，在<b>举盾格挡</b>（{@link LivingEntity#isBlocking()}）姿态下每 {@code INTERVAL} tick 驱动一轮：
 * <ul>
 *   <li>读取标记幅度作为锥形半径（米，节点值如 30）；</li>
 *   <li>扫描半径内所有实体，仅取「敌对阵营」（与持有者不同且非 {@link Camp#NONE}）且位于持有者<b>正前方</b>的圆锥内者；</li>
 *   <li>对其周期性施加 {@link StatusDefs#DEBILITATE} 状态（动作速度 -magnitude%）。</li>
 * </ul>
 * 全部复用现成基元（markerMagnitude / EntityQuery getEntitiesOfClass / StatusEngine.apply），零新增事件订阅，R3 安全。
 */
public final class ShieldFrontDebilitateAura {

    private ShieldFrontDebilitateAura() {
    }

    /** 刷新节流：每 {@code INTERVAL} tick 执行一轮（状态时长 80 > 间隔 → 举盾期间持续刷新 = 常驻光环）。 */
    private static final int INTERVAL = 20;
    /** 前方圆锥半角余弦：90° 全角锥（正前方 ±45°）；look·to ≥ 该值即判定为「正前方」。 */
    private static final double COS_HALF_ANGLE = Math.cos(Math.toRadians(45.0));
    /** 痿顿幅度：动作速度 -20%（规范痿顿强度；StatusDefs.DEBILITATE_DEF 无烘焙值，由调用方提供）。 */
    private static final double DEBILITATE_MAGNITUDE = 0.20;
    /** 痿顿时长（tick）：80 > INTERVAL，举盾期间持续刷新以维持常驻（StatusTypeDef 默认时长）。 */
    private static final int DEBILITATE_DURATION_TICKS = 80;

    /** 每实体每 tick 调用（LivingTickDriver，紧邻 CreateGroundDriver.tick）。无 store / 未举盾 / 无标记 → 零开销短路。 */
    public static void tick(LivingEntity living) {
        if (living.level().isClientSide()) return;
        // 门控：仅举盾格挡中的实体才驱动（cheap 早退，绝大多数实体此处短路）
        if (!living.isBlocking()) return;
        if (!PeriodicDriver.beat(living.tickCount, INTERVAL)) return;

        // 半径经「范围属性」作用域注入：光环半径 = 标记幅度(米) × EFFECT_AREA（{area} 作用域）。
        // 与技能面积加成同源（统一 ProjectileDeliveryExecutor 的 ORB/BALL 分支、AreaDeliveryExecutor 经 withScope({area}, EFFECT_AREA)），
        // 非存在范围（光环）统一走范围属性作用域，而非新建 aura_radius 轴。
        double radius = StatEngine.magnitudeOf(living, "shield_front_debilitate", Set.of())
                * AilmentHelper.withScope(living, Set.of(GateTag.AREA), AttributeRegistry.EFFECT_AREA);
        if (radius <= 0) return;

        Camp myCamp = living.getData(AttachmentRegistry.CAMP);
        if (myCamp == Camp.NONE) return;

        // 目标解析走公共扫描器：球心=持有者自身，参照阵营=持有者阵营，附加正前方圆锥半角余弦。
        for (LivingEntity e : AuraScan.hostilesAround(living, radius, myCamp, COS_HALF_ANGLE)) {
            StatusEngine.apply(e, StatusDefs.DEBILITATE, living,
                    DEBILITATE_MAGNITUDE, DEBILITATE_DURATION_TICKS, 1, true);
        }
    }
}
