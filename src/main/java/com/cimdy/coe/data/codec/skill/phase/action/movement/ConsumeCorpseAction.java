package com.cimdy.coe.data.codec.skill.phase.action.movement;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.data.codec.skill.phase.action.support.BannerAction;
import com.cimdy.coe.data.codec.skill.context.SkillContext;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 消耗尸体动作（POE2 Corpse 子系统）— 施放时消耗 {@code amount} 具尸体、按概率保留、授予
 * 「近期消耗尸体」窗口增益（详见 {@link ResourceRegistry#consumeAndEmpowerCorpse}）。
 *
 * <p>与 {@link BannerAction} 同构（构造动作 → ON_CAST 触发 → 经 CorpseState 落地），但为自身即时动作
 * （无区域/实体投放），仅操作施法者身上的尸体计数与状态。
 */
public record ConsumeCorpseAction(
        ExpressionCodec.Value amount
) implements Action {

    public static final Codec<ConsumeCorpseAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("amount", ExpressionCodec.of(1.0))
                    .forGetter(ConsumeCorpseAction::amount)
    ).apply(i, ConsumeCorpseAction::new));

    @Override public Action.Type<?> getType() { return ActionTypes.CONSUME_CORPSE; }

    @Override
    public void execute(ActionContext context) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;
        int n = (int) Math.max(1, Math.round(amount.getFloat(skillCtx)));
        ResourceRegistry.consumeAndEmpowerCorpse(caster, n);
    }
}
