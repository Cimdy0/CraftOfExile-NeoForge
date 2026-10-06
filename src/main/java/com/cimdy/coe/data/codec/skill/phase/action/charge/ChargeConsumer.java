package com.cimdy.coe.data.codec.skill.phase.action.charge;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Optional;

/**
 * 充能球消耗器 — 消耗充能球来强化动作效果。
 * 嵌入到其他 Action 中的消耗配置。
 *
 * PoE2: 每消耗一个 Endurance Charge 获得额外伤害/效果
 */
public record ChargeConsumer(
        ChargeType chargeType,
        ExpressionCodec.Value consumeCount,
        Optional<ExpressionCodec.Value> extraDamageMultiplierPerCharge,
        Optional<ExpressionCodec.Value> extraAreaMultiplierPerCharge,
        Optional<ExpressionCodec.Value> extraProjectilesPerCharge,
        Optional<List<Action>> bonusActionsOnConsume
) {
    public static final Codec<ChargeConsumer> CODEC = RecordCodecBuilder.create(i -> i.group(
            ChargeType.CODEC.fieldOf("charge_type").forGetter(ChargeConsumer::chargeType),
            ExpressionCodec.VALUE_CODEC.fieldOf("consume_count").forGetter(ChargeConsumer::consumeCount),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("extra_damage_multiplier_per_charge")
                    .forGetter(ChargeConsumer::extraDamageMultiplierPerCharge),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("extra_area_multiplier_per_charge")
                    .forGetter(ChargeConsumer::extraAreaMultiplierPerCharge),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("extra_projectiles_per_charge")
                    .forGetter(ChargeConsumer::extraProjectilesPerCharge),
            Action.CODEC.listOf().optionalFieldOf("bonus_actions_on_consume")
                    .forGetter(ChargeConsumer::bonusActionsOnConsume)
    ).apply(i, ChargeConsumer::new));

}
