package com.cimdy.coe.event;

import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * 触发通道的伤害数值载体：{@link com.cimdy.coe.data.codec.support.stat.engine.StatEngine#fire} 在伤害事件发生时，
 * 把「本次伤害结算的关键数值」一并传给 {@link com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect#onTrigger}，
 * 使「按造成伤害 / 承受伤害比例回复资源」类效果（偷取 / 补给 / 受击转能量）无需在伤害管线里硬编码副作用。
 *
 * <p>字段仅保留效果真正读取的数值：
 * <ul>
 *     <li>{@link #damages}：攻击方造成的各类型伤害分额 [物理, 混沌, 火, 冰, 电]（减免后、招架/格挡前），供偷取计算总量；</li>
 *     <li>{@link #takenPreBlock}：减免后、招架/格挡<b>前</b>的承伤总额（{@code State.totalDamageTaken}），
 *         原管线补给/受击转能量据此触发（招架/格挡归零承伤前已记账）；</li>
 *     <li>{@link #takenPostBlock}：招架/格挡<b>后</b>的最终承伤（{@code State.newDamage}），
 *         原管线偷取据此守卫（被完全招架/格挡时不偷取）；</li>
 *     <li>{@link #skill}：本次技能标识（供偷取来源标记）。</li>
 * </ul>
 * 攻击方 / 承受方实体改由 {@link com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext#owner()} /
 * {@link com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext#triggerTarget()} 统一提供，
 * 不再在载体里重复持有（消除与 {@code StatEvalContext} 的字段重复）。
 */
public record TriggerContext(
        float[] damages,
        float takenPreBlock,
        float takenPostBlock,
        @Nullable Identifier skill,
        boolean deflected
) {
}
