package com.cimdy.coe.data.codec.skill.modifier;

import com.cimdy.coe.data.codec.skill.modifier.impl.effect.AdditionalActionEffect;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.DeliveryMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EffectMod;
import com.mojang.serialization.Codec;

import java.util.Map;

/**
 * 技能修正 — 技能本身与辅助宝石「共享」的效果形状。
 *
 * <p>设计原则（与诉求一致）：PoE2 中辅助技能能做到的事（斩杀、得球、附加伤害、转伤、
 * 范围/投射物修正等），技能本身也能完成，二者是交叉关系而非包含关系。因此这些运行时
 * 效果数据不归辅助宝石独有，而是抽象为 {@code SkillModifier}，由 {@code Skill} 与
 * {@code SupportGem} 共同持有。
 *
 * <p>本接口仅承载「运行时需逐次查询」的修正（DeliveryMod / EffectMod /
 * AdditionalActionEffect）。等级 / 品质 / 施法速度等「装配期特殊修正」属于辅助宝石独有
 * （见 support.SpecialMod），不在此处——技能自身的等级/品质由 SkillData 承载。
 *
 * <p>通过 dispatchedMap 实现多态序列化；新增一种修正形状只需在此注册，无需为技能/辅助分别建类。
 */
public interface SkillModifier {
    Codec<Map<SkillModifier.Type<?>, SkillModifier>> MAP_CODEC = Codec.dispatchedMap(
            SkillModifierTypes.REGISTRY.byNameCodec(),
            SkillModifier.Type::codec);

    Codec<SkillModifier> CODEC = MAP_CODEC.xmap(
            map -> map.values().iterator().next(),
            expr -> Map.of(expr.getType(), expr));

    SkillModifier.Type<?> getType();
    String descriptionKey();

    record Type<T extends SkillModifier>(Codec<T> codec) {}
}
