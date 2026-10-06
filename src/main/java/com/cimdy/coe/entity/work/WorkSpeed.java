package com.cimdy.coe.entity.work;

import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.registry.AttributeRegistry;
import net.minecraft.world.entity.LivingEntity;

/**
 * 工作速度计算与 Stat 词缀接口。
 * <p>
 * 有效工作速度 = 个体基础速度 × 适应度倍率 × 词缀速度倍率：
 * <pre>
 *   effective = baseWorkSpeed
 *             × (1 + 总适应度 × APTITUDE_PER_LEVEL)
 *             × work_speed属性(默认1.0)
 * </pre>
 * 其中「总适应度 = 实体档案中的工种适应度 + {@code work_aptitude} 属性」。
 * <p>
 * <b>Stat 词缀系统的介入点</b>：本类只读取两个属性
 * （{@code AttributeRegistry.WORK_SPEED} 与 {@code AttributeRegistry.WORK_APTITUDE}），
 * 二者已随 {@code AttributeRegistry} 注册并自动挂载到全部实体。任何词缀 / 天赋 / 装备只要产出指向这两个
 * 属性的 {@code StatEntry}（{@code ModifierStatEffect}），即可在运行时提升「工作速度」或「工作适应度等级」，
 * 无需改动本类。
 */
public final class WorkSpeed {
    /** 每 1 点工作适应度等级带来的工作速度提升（+10%）。 */
    public static final double APTITUDE_PER_LEVEL = 0.10;

    private WorkSpeed() {
    }

    /** 某实体在某工种下的有效工作速度（数值越大工作越快）。 */
    public static double effectiveSpeed(LivingEntity entity, String workType) {
        WorkProfile profile = WorkProfile.get(entity);
        double base = profile.baseWorkSpeed();
        double aptitude = aptitude(entity, workType);
        double aptitudeMul = 1.0 + aptitude * APTITUDE_PER_LEVEL;
        double speedMul = entity.getAttributeValue(AttributeRegistry.WORK_SPEED);
        return base * aptitudeMul * Math.max(0.01, speedMul);
    }

    /** 某实体在某工种下的「总工作适应度等级」= 档案适应度 + work_aptitude 属性加成。 */
    public static double aptitude(LivingEntity entity, String workType) {
        return WorkProfile.get(entity).aptitude(workType)
                + entity.getAttributeValue(AttributeRegistry.WORK_APTITUDE);
    }

    /** 某实体在某工种下的基础工作速度（不含词缀倍率，仅个体档案值）。 */
    public static double baseSpeed(LivingEntity entity) {
        return WorkProfile.get(entity).baseWorkSpeed();
    }
}
