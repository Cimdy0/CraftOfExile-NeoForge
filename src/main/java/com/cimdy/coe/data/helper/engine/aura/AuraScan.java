package com.cimdy.coe.data.helper.engine.aura;

import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 光环类驱动的<b>公共目标解析基元</b>——供所有「持有者每 tick 周期性作用于周围实体」的驱动复用
 * （{@code MarkBlindAura} / {@code ShieldFrontDebilitateAura}），避免每个光环各自重写一遍
 * 「球形扫描 + 阵营过滤」。
 *
 * <p><b>计时不在这里</b>：节拍判定统一走 {@link PeriodicDriver#beat(long, int)}
 * （单一权威计时入口，技能侧同样复用）。本类只回答「跑谁」。
 *
 * <p>本类<b>不施加任何效果</b>：只负责「这一拍该不该跑」与「跑谁」。施加仍由各驱动自己完成
 * （状态走 {@code StatusEngine.apply}，属性授予走 {@code MultiStatEffect} 子效果自身），
 * 与「驱动只做节拍 + 门控 + 目标解析」的分层一致。
 *
 * <p>目标解析只处理<b>几何 + 阵营</b>；语义目标（在场友方 / 随从 / 图腾…）由
 * {@code TargetSelector} 解析，不走本类。
 */
public final class AuraScan {

    private AuraScan() {
    }

    /**
     * 以 {@code center} 为球心、{@code radius} 为半径，收集<b>与参考阵营敌对</b>的存活实体（全向）。
     *
     * <p>过滤链（与抽取前三处光环的逐条实现一一对应）：
     * <ol>
     *   <li>排除 {@code center} 自身；</li>
     *   <li>排除已死亡 / 正在死亡；</li>
     *   <li>排除 {@link Camp#NONE}（中立，非敌非友）；</li>
     *   <li>排除阵营等于 {@code referenceCamp} 者（友方）。</li>
     * </ol>
     * 注：<b>光环持有者自身</b>无需显式排除——持有者阵营恒等于 {@code referenceCamp}，
     * 已被第 4 条过滤（原 {@code MarkBlindAura} 里 {@code n == living} 的显式判断是冗余的）。
     *
     * @param referenceCamp 参照阵营（通常取光环持有者的阵营，而非球心阵营——
     *                      如「被标记敌人周边」场景球心是敌人、参照阵营是持有者的）
     */
    public static List<LivingEntity> hostilesAround(LivingEntity center, double radius, Camp referenceCamp) {
        return hostilesAround(center, radius, referenceCamp, null);
    }

    /**
     * 同上，额外限制在 {@code center} <b>正前方圆锥</b>内：{@code look · normalize(to) >= minLookDot}
     * （{@code minLookDot} 为半角余弦，{@code null} = 不限方向，退化为全向球）。
     */
    public static List<LivingEntity> hostilesAround(LivingEntity center, double radius,
                                                    Camp referenceCamp, @Nullable Double minLookDot) {
        if (radius <= 0) return List.of();
        boolean cone = minLookDot != null;
        Vec3 look = cone ? center.getLookAngle() : null;
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : center.level().getEntitiesOfClass(LivingEntity.class,
                center.getBoundingBox().inflate(radius))) {
            if (e == center || e.isDeadOrDying()) continue;
            Camp c = e.getData(AttachmentRegistry.CAMP);
            if (c == Camp.NONE || c == referenceCamp) continue;   // 中立与友方均剔除
            if (cone) {
                Vec3 to = e.position().subtract(center.position());
                if (to.lengthSqr() < 1.0e-6) continue;            // 重合点无方向，剔除
                if (look.dot(to.normalize()) < minLookDot) continue;
            }
            out.add(e);
        }
        return out;
    }
}
