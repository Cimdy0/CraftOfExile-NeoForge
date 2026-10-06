package com.cimdy.coe.data.codec.skill.phase.action.control;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * 条件分支动作 — 根据条件执行不同的动作链。
 * 条件为 math 表达式，经 {@link ExpressionContext#forSkill} 组装数据基站后集中判定。
 */
public record ConditionalAction(
        MathExpression condition,
        List<Action> ifTrue,
        List<Action> ifFalse
) implements Action {
    public static final Codec<ConditionalAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            MathExpression.CODEC.fieldOf("condition").forGetter(ConditionalAction::condition),
            Action.CODEC.listOf().fieldOf("if_true").forGetter(ConditionalAction::ifTrue),
            Action.CODEC.listOf().optionalFieldOf("if_false", List.of()).forGetter(ConditionalAction::ifFalse)
    ).apply(i, ConditionalAction::new));

    @Override public Action.Type<?> getType() { return ActionTypes.CONDITIONAL; }

    @Override
    public void execute(ActionContext context) {
        // 动作链无阶段实例：伤害子站缺省（damage.* 读 0），其余子站照常组装
        if (ExpressionContext.forSkill(context.skill(), null).test(condition)) {
            ifTrue.forEach(action -> action.execute(context));
        } else {
            ifFalse.forEach(action -> action.execute(context));
        }
    }
}
