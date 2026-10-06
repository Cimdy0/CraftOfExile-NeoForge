package com.cimdy.coe.data.codec.skill.support;

import com.cimdy.coe.data.codec.skill.modifier.impl.control.DeliveryMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EffectMod;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;
import java.util.Optional;

/**
 * 特殊修正（辅助宝石独有）— 在「装配期」作用于链接技能的等级 / 品质 / 速度，
 * 与运行时投递/效果修正（DeliveryMod / EffectMod，技能与辅助共享）分离。
 *
 * <p>这些参数在技能链接装配时（SkillLinkResolver）一次性汇总并写入技能数据，
 * 而非在每次施放时查询，因此不进入运行时 SupportProfile。技能自身的等级/品质由 SkillData 承载，
 * 故本类仅由辅助宝石持有（对应 PoE2 中辅助宝石对链接技能的特殊加成，如 Enhance）。
 *
 * <p><b>三个速度维度相互独立，不可混用</b>（POE2 语义）：
 * <ul>
 *   <li>{@code castSpeedMultiplier} —— <b>Cast Speed</b>：只对<b>法术</b>技能生效。</li>
 *   <li>{@code attackSpeedMultiplier} —— <b>Attack Speed</b>：只对<b>攻击</b>技能生效。</li>
 *   <li>{@code skillSpeedMultiplier} —— <b>Skill Speed</b>：POE2 单一速度维度，
 *       对<b>攻击与法术皆</b>生效（对攻击表现为攻击速度、对法术表现为施法速度）。
 *       例如 20% less Skill Speed = 0.8，无论链接的是攻击还是法术，施放时间一律 ÷0.8。</li>
 * </ul>
 * 聚合时按技能类型二选一（见 {@code SkillLinkResolver#getTotalSpeedMultiplier}）：
 * 若误把 Skill Speed 拆成 cast×attack 两通道同设为 0.8，会得到 0.8×0.8=0.64（−36%），语义错误。
 *
 * <p>示例：
 * <pre>
 * // 辅助宝石提升链接技能等级 / 品质 / 施法速度
 * { "special": { "level_bonus": 2, "quality_bonus": 10,
 *     "cast_speed_multiplier": 1.2, "attack_speed_multiplier": 1.2 } }
 * // 20% less Skill Speed（攻击与法术都适用）
 * { "special": { "skill_speed_multiplier": 0.8 } }
 * </pre>
 */
public record SpecialMod(
        Optional<ExpressionCodec.Value> levelBonus,
        Optional<ExpressionCodec.Value> qualityBonus,
        Optional<ExpressionCodec.Value> castSpeedMultiplier,
        Optional<ExpressionCodec.Value> attackSpeedMultiplier,
        Optional<ExpressionCodec.Value> skillSpeedMultiplier
) {
    public static final Codec<SpecialMod> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("level_bonus").forGetter(SpecialMod::levelBonus),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("quality_bonus").forGetter(SpecialMod::qualityBonus),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("cast_speed_multiplier").forGetter(SpecialMod::castSpeedMultiplier),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("attack_speed_multiplier").forGetter(SpecialMod::attackSpeedMultiplier),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("skill_speed_multiplier").forGetter(SpecialMod::skillSpeedMultiplier)
    ).apply(i, SpecialMod::new));

    /** 根据辅助宝石自身等级计算给链接技能的等效等级加成。 */
    public int getAdditionalLevels(int supportLevel) {
        return levelBonus.map(v -> {
            ExpressionContext ctx = ExpressionContext.builder().build()
                    .withVariables(Map.of("skill.level", ExpressionCodec.of(supportLevel)));
            return (int) Math.round(ExpressionCodec.getValue(v.value(), ctx));
        }).orElse(0);
    }

    public double getQualityBonus() {
        return qualityBonus.map(ExpressionCodec.Value::getValue).orElse(0.0);
    }

    public double getCastSpeedMultiplier() {
        return castSpeedMultiplier.map(ExpressionCodec.Value::getValue).orElse(1.0);
    }

    public double getAttackSpeedMultiplier() {
        return attackSpeedMultiplier.map(ExpressionCodec.Value::getValue).orElse(1.0);
    }

    /** POE2「Skill Speed」：对攻击与法术皆生效的单一速度维度。 */
    public double getSkillSpeedMultiplier() {
        return skillSpeedMultiplier.map(ExpressionCodec.Value::getValue).orElse(1.0);
    }
}
