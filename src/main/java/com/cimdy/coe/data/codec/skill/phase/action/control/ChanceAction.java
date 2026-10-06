package com.cimdy.coe.data.codec.skill.phase.action.control;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * 几率分支动作 — 按几率决定是否执行一次内部动作链。
 *
 * <p><b>与 {@link ConditionalAction} 同构</b>：后者按<b>条件</b>分支（吃 {@code MathExpression}），
 * 本类按<b>几率</b>门控（吃数值）。二者可任意嵌套组合
 * （如「条件=击杀带元素异常 → 几率=25% → 生成残片」）。
 *
 * <p><b>为什么不在表达式里做随机</b>：表达式系统（{@code MathExpressions}）是<b>纯函数</b>求值，
 * 刻意不含 random/chance 原语——同一表达式在 fold 通道（属性聚合）中会被反复求值，
 * 引入随机会让属性读取不确定、并使「面板值」与「实际结算」不一致。
 * 项目的既定范式是<b>几率走 Java 侧掷骰</b>：
 * <ul>
 *   <li>{@code DamageAction} 暴击：{@code level.getRandom().nextFloat() < chance}；</li>
 *   <li>{@code DamageAction} 近距击退：先经 fold 通道读 {@code KNOCKBACK_CHANCE} 属性再掷骰；</li>
 *   <li>{@code RemnantState#drop}：读 {@code REMNANT_EXTRA_CHANCE} 属性后 {@code getRandom().nextDouble()}。</li>
 * </ul>
 * 本类沿用同一范式：几率值本身可写常量（技能固有值）或读属性（可被词缀加成），
 * 掷骰固定走 {@code level.getRandom()}。
 *
 * @param chance  几率（0~1）。{@code <=0} 恒不执行；{@code >=1} 恒执行（省去掷骰）。
 *                可由 {@code attribute:} 节点读取属性，从而被「几率提高」类词缀加成。
 * @param actions 命中几率后执行的动作链。
 */
public record ChanceAction(
        ExpressionCodec.Value chance,
        List<Action> actions
) implements Action {
    public static final Codec<ChanceAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("chance").forGetter(ChanceAction::chance),
            Action.CODEC.listOf().fieldOf("actions").forGetter(ChanceAction::actions)
    ).apply(i, ChanceAction::new));

    @Override
    public Action.Type<?> getType() {
        return ActionTypes.CHANCE;
    }

    @Override
    public void execute(ActionContext context) {
        // 优先复用事件回调已构造的带事件上下文（EventListenPhase 注入），使 event: 节点可读；
        // 常规阶段路径下退回 forSkill 组装，与 ConditionalAction 同一口径。
        ExpressionContext ec = context.eventContext()
                .orElseGet(() -> ExpressionContext.forSkill(context.skill(), null));
        double c = chance.getValue(ec);
        if (c <= 0.0) return;
        if (c < 1.0 && context.caster().level().getRandom().nextDouble() >= c) return;
        actions.forEach(action -> action.execute(context));
    }
}
