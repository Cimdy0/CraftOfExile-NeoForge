package com.cimdy.coe.data.codec.item.flask;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 活跃 Flask 效果（POE2 实证：Flask 回复是「持续恢复」——使用后在一段时间内每 tick 回复，
 * <b>目标资源已满时效果立即移除</b>，但受两条标记门控）。存于玩家附件 {@code flask_active}（槽索引 → 效果，
 * 手持饮用用键 {@code -1}），由 {@link com.cimdy.coe.item.flask.FlaskHelper#tick} 每 tick 驱动。
 * <p>
 * 自带 {@code resource} 而非回查槽内物品：使用后即便把药剂从槽里取出，已发动的回复也应按 POE2
 * 继续走完（效果与物品解耦），同时兼容任意资源键（health/mana/energy/rage）。
 * <p>
 * 两条标记（boolean）在饮用瞬间由对应词缀经 StatEngine.gateOf 读取并烘焙进本记录（按 {@code {flask, 资源}}
 * 作用域，即「加上资源类型」），运行时不再回查 Stat——它们只是「标记效果」：
 * <ul>
 *   <li>{@code notRemovedWhenFull} —— 回复满后不停止（"not removed when Unreserved Life is Filled"）；</li>
 *   <li>{@code canOverflow} —— 回复可溢出上限（资源已满仍视为有效）。</li>
 * </ul>
 */
public record FlaskEffect(String resource, float perTick, int remainingTicks,
                         boolean notRemovedWhenFull, boolean canOverflow) {

    public static final Codec<FlaskEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("resource").forGetter(FlaskEffect::resource),
            Codec.FLOAT.fieldOf("per_tick").forGetter(FlaskEffect::perTick),
            Codec.INT.fieldOf("remaining_ticks").forGetter(FlaskEffect::remainingTicks),
            Codec.BOOL.optionalFieldOf("not_removed_when_full", false).forGetter(FlaskEffect::notRemovedWhenFull),
            Codec.BOOL.optionalFieldOf("can_overflow", false).forGetter(FlaskEffect::canOverflow)
    ).apply(instance, FlaskEffect::new));

    public FlaskEffect tickDown() {
        return new FlaskEffect(resource, perTick, remainingTicks - 1, notRemovedWhenFull, canOverflow);
    }
}
