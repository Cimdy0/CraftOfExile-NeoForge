package com.cimdy.coe.data.codec.skill.phase.action.control;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.helper.hub.SpiritManager;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * 触发动作 — 生命周期触发器 + 动作（+ 可选周期门控）。
 * 是 SkillPhase 中的基本执行单元。
 *
 * <p><b>{@code period_ticks}（周期门控）</b>：声明本动作「每 N tick 至多执行一次」。
 * 存在背景：精魂常驻技能的效果由 {@code SpiritManager} <b>每 tick 重放</b> ON_CAST，
 * 于是「While active, every X seconds Y」一类真值（Time of Need「每 (12—10.1) 秒降下祝福」）
 * 要么每 tick 触发（错误），要么因无从表达而整体缺失。周期门控把节流下沉到<b>动作声明</b>，
 * 使「每 X 秒」成为数据可表达的一等公民，无需为每个技能在消费点按 id 分支。
 *
 * <p>读点为 {@code SpiritManager#applyPhaseAura}（常驻重放路径）。普通施放路径不读该字段
 * （一次施放只执行一次动作，无需节流）。
 */
public record TriggeredAction(
        LifecycleTrigger trigger,
        Action action,
        Optional<ExpressionCodec.Value> period
) {
    public static final Codec<TriggeredAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            LifecycleTrigger.CODEC.fieldOf("trigger").forGetter(TriggeredAction::trigger),
            Action.CODEC.fieldOf("action").forGetter(TriggeredAction::action),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("period_ticks").forGetter(TriggeredAction::period)
    ).apply(i, TriggeredAction::new));

    /** 便捷构造：无周期门控（绝大多数动作）。 */
    public TriggeredAction(LifecycleTrigger trigger, Action action) {
        this(trigger, action, Optional.empty());
    }
}
