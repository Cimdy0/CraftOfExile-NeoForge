package com.cimdy.coe.data.codec.skill.phase.action.control;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.data.codec.skill.runtime.SkillTriggerHelper;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * 触发技能动作 — 触发另一个技能 (类似 PoE 的触发辅助宝石)。
 */
public record TriggerSkillAction(
        Identifier skillId,
        Optional<ExpressionCodec.Value> powerMultiplier
) implements Action {
    public static final Codec<TriggerSkillAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("skill_id").forGetter(TriggerSkillAction::skillId),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("power_multiplier")
                    .forGetter(TriggerSkillAction::powerMultiplier)
    ).apply(i, TriggerSkillAction::new));

    @Override public Action.Type<?> getType() { return ActionTypes.TRIGGER_SKILL; }

    @Override
    public void execute(ActionContext context) {
        float mult = powerMultiplier.map(v -> (float) v.getValue()).orElse(1.0f);
        SkillTriggerHelper.trigger(context.skill(), context, skillId, mult);
    }
}
