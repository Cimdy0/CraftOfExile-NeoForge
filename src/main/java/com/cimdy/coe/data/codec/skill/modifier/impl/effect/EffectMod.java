package com.cimdy.coe.data.codec.skill.modifier.impl.effect;

import com.cimdy.coe.data.codec.skill.modifier.SkillModifierTypes;

import com.cimdy.coe.data.codec.skill.modifier.impl.damage.AddedDamage;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.Convert;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.DeliveryMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.Exposure;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.Penetration;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.Slow;
import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * 修正效果 — 对技能的「效果」（伤害/暴击/速度/附加伤害/转伤/斩杀等）进行修正，
 * 并可按投递类型、伤害类型、触发条件限定作用域。一个技能或辅助宝石的「某段效果如何被修改」
 * 全部由本类描述，不再为每种效果各建一个类。技能与辅助宝石共用本类。
 *
 * <p>字段均为可选，按"命中即生效"组合：
 * <ul>
 *   <li>{@code delivery}：仅当本次命中来自该投递类型时生效（projectile/area/beam/aura/self）。</li>
 *   <li>{@code damage_type}：仅作用于该伤害类型（用于 suppress / execute 的作用域）。</li>
 *   <li>{@code condition}：触发条件，hit（默认）或 crit（仅暴击时）。</li>
 *   <li>{@code more} / {@code less}：对匹配作用域的伤害乘以该倍率（1.3=+30%，0.7=-30%）。</li>
 *   <li>{@code damage_multiplier_per_split}：每次分裂（Fork）后投射物伤害倍率（全局/投射物作用域）。</li>
 *   <li>{@code crit_chance_multiplier} / {@code crit_damage_multiplier}：暴击率/暴击伤害倍率（全局）。</li>
 *   <li>{@code suppress}：若为 true，则禁止造成 damage_type 指定的伤害（置 0）。</li>
 *   <li>{@code added_damage}：附加伤害（结构化：damage_type / min / max）。</li>
 *   <li>{@code convert}：转伤（结构化：from / to / ratio / added_as_extra）。</li>
 *   <li>{@code execute_threshold}：斩杀阈值（目标生命比例低于此值时该伤害致命）。</li>
 * </ul>
 *
 * <p>示例（技能或辅助宝石通用）：
 * <pre>
 * // 增加范围伤害 = 范围投递(DeliveryMod) + 伤害效果(本类的 more)
 * { "coe:delivery_mod": { "delivery":"area", "params":{"radius_multiplier":1.3} } }
 * { "coe:effect_mod": { "more": 1.4 } }
 * // 对某种投递的伤害修正：投射物 +25%
 * { "coe:effect_mod": { "delivery":"projectile", "more": 1.25 } }
 * // 对某种伤害类型的伤害修正：火焰 +30%
 * { "coe:effect_mod": { "damage_type":"fire", "more": 1.3 } }
 * // 投射物在暴击时火焰总增、且不造成闪电（事件插入：condition=crit）
 * { "coe:effect_mod": { "delivery":"projectile","condition":"crit","damage_type":"fire","more":1.4 } }
 * { "coe:effect_mod": { "delivery":"projectile","damage_type":"lightning","suppress":true } }
 * // 转伤 + 禁伤
 * { "coe:effect_mod": { "convert": {"from":"physical","to":"fire","ratio":1.0} } }
 * { "coe:effect_mod": { "damage_type":"lightning","suppress":true } }
 * // 斩杀（目标生命<=10%）—— 技能本身也能做到
 * { "coe:effect_mod": { "execute_threshold": 0.1 } }
 * // 附加火焰伤害
 * { "coe:effect_mod": { "added_damage": {"damage_type":"fire","min":5,"max":10} } }
 * </pre>
 */
public record EffectMod(
        Optional<String> delivery,
        Optional<String> damageType,
        Optional<String> condition,
        Optional<ExpressionCodec.Value> more,
        Optional<ExpressionCodec.Value> less,
        Optional<ExpressionCodec.Value> damageMultiplierPerSplit,
        Optional<ExpressionCodec.Value> critChanceMultiplier,
        Optional<ExpressionCodec.Value> critDamageMultiplier,
        boolean suppress,
        Optional<AddedDamage> added,
        Optional<Convert> convert,
        Optional<ExpressionCodec.Value> executeThreshold,
        Optional<Penetration> penetration,
        Optional<Exposure> exposure,
        Optional<Slow> slow
) implements SkillModifier {
    public static final Codec<EffectMod> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("delivery").forGetter(EffectMod::delivery),
            Codec.STRING.optionalFieldOf("damage_type").forGetter(EffectMod::damageType),
            Codec.STRING.optionalFieldOf("condition").forGetter(EffectMod::condition),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("more").forGetter(EffectMod::more),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("less").forGetter(EffectMod::less),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("damage_multiplier_per_split").forGetter(EffectMod::damageMultiplierPerSplit),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("crit_chance_multiplier").forGetter(EffectMod::critChanceMultiplier),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("crit_damage_multiplier").forGetter(EffectMod::critDamageMultiplier),
            Codec.BOOL.optionalFieldOf("suppress", false).forGetter(EffectMod::suppress),
            AddedDamage.CODEC.optionalFieldOf("added_damage").forGetter(EffectMod::added),
            Convert.CODEC.optionalFieldOf("convert").forGetter(EffectMod::convert),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("execute_threshold").forGetter(EffectMod::executeThreshold),
            Penetration.CODEC.optionalFieldOf("penetration").forGetter(EffectMod::penetration),
            Exposure.CODEC.optionalFieldOf("exposure").forGetter(EffectMod::exposure),
            Slow.CODEC.optionalFieldOf("slow").forGetter(EffectMod::slow)
    ).apply(i, EffectMod::new));

    @Override public SkillModifier.Type<?> getType() { return SkillModifierTypes.EFFECT_MOD; }
    @Override public String descriptionKey() { return "skill.coe.effect_mod"; }
}
