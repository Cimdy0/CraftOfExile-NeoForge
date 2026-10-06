package com.cimdy.coe.data.codec.skill.modifier.impl.control;

import com.cimdy.coe.data.codec.skill.modifier.SkillModifierTypes;

import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;
import java.util.Optional;

/**
 * 修正投递 — 指定一种投递类型（projectile/area/beam/aura/self），并携带一组投递参数修正。
 *
 * <p>设计原则：不再为「每种投递 × 每种效果」各建一个修正类，而是用「一个投递判别字段 +
 * 一组参数」统一表达所有投递修正。新增投递类型（如 beam/aura/self）无需新增类，
 * 只需其执行器按参数名读取即可。技能与辅助宝石共用本类：
 * <ul>
 *   <li>projectile: additional_projectiles / fork_count / chain_count / pierce_count / speed_multiplier / return_to_caster / fire_in_circle / barrage_count</li>
 *   <li>area:       radius_multiplier / additional_targets / nova / cascade</li>
 *   <li>aura/beam:  radius_multiplier（及其各自理解的参数）</li>
 * </ul>
 *
 * <p>示例：
 * <pre>
 * // 技能或辅助：增加投射物数量
 * { "coe:delivery_mod": { "delivery": "projectile",
 *     "params": { "additional_projectiles": 2 } } }
 * // 增加范围半径
 * { "coe:delivery_mod": { "delivery": "area",
 *     "params": { "radius_multiplier": 1.3 } } }
 * </pre>
 */
public record DeliveryMod(
        String delivery,
        Map<String, ExpressionCodec.Value> params
) implements SkillModifier {
    public static final Codec<DeliveryMod> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("delivery").forGetter(DeliveryMod::delivery),
            Codec.unboundedMap(Codec.STRING, ExpressionCodec.VALUE_CODEC)
                    .fieldOf("params").forGetter(DeliveryMod::params)
    ).apply(i, DeliveryMod::new));

    public Optional<ExpressionCodec.Value> param(String name) {
        return Optional.ofNullable(params.get(name));
    }

    public float floatParam(String name, SkillContext ctx, float def) {
        return param(name).map(v -> v.getFloat(ctx)).orElse(def);
    }

    public int intParam(String name, SkillContext ctx, int def) {
        return param(name).map(v -> v.getInt(ctx)).orElse(def);
    }

    public boolean boolParam(String name, SkillContext ctx, boolean def) {
        return param(name).map(v -> v.getBool(ctx)).orElse(def);
    }

    @Override public SkillModifier.Type<?> getType() { return SkillModifierTypes.DELIVERY_MOD; }
    @Override public String descriptionKey() { return "skill.coe.delivery_mod"; }
}
