package com.cimdy.coe.data.codec.skill.phase;

import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 技能阶段体包装层 —— 作用于任意 {@link net.minecraft.world.entity.LivingEntity} 施法者/目标，不限于玩家。
 * 决定 {@link SkillPhase} 是否在当前条件下激活。
 *
 * <p>激活条件已统一为 math 表达式（获取 / 求值两步分离）：「获取」由表达式内读值节点
 * （技能状态 / 伤害子站 {@code damage.*} / 属性 / 效果 / 装备 / 计时…）从 Context 数据基站读出，
 * 「求值」由 math 比较节点完成；判定经 {@link ExpressionContext#forSkill} 组装基站后
 * {@link ExpressionContext#test} 集中给出结果（替代原 SkillCondition / Condition 系统）。
 *
 * @see SkillPhase 核心阶段体
 */
public record SkillPhasePlayer(
        Optional<MathExpression> condition,
        SkillPhase phase
) {
    public static final Codec<SkillPhasePlayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MathExpression.CODEC.optionalFieldOf("condition").forGetter(SkillPhasePlayer::condition),
            SkillPhase.CODEC.fieldOf("phase").forGetter(SkillPhasePlayer::phase)
    ).apply(instance, SkillPhasePlayer::new));

    public boolean matches(SkillInstance skillInstance, SkillPhaseInstance phaseInstance) {
        return condition.isEmpty()
                || ExpressionContext.forSkill(skillInstance, phaseInstance).test(condition.get());
    }

    public List<SkillPhaseInstance> run(SkillInstance skillInstance, SkillPhaseInstance phaseInstance) {
        List<SkillPhaseInstance> list = new ArrayList<>();
        if (matches(skillInstance, phaseInstance)) {
            list = phase.initialize(skillInstance, phaseInstance);
        }
        return list;
    }

    public Set<String> getLabels(SkillContext context) {
        return phase.getLabels(context);
    }

    public void addComponent(List<Component> tooltip, SkillContext context) {
        phase.addComponent(tooltip, context);
    }

    /** 返回覆写阶段（phase）后的副本；条件原样保留。 */
    public SkillPhasePlayer withPhase(SkillPhase phase) {
        return new SkillPhasePlayer(condition, phase);
    }
}
