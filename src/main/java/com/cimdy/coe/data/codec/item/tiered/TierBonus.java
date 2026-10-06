package com.cimdy.coe.data.codec.item.tiered;

import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 阶梯上的<b>一格</b>：<b>词缀 + 幅度 + 所属阶</b> —— 值侧的全部内容。
 *
 * <p><b>为什么词缀在值里、不在键里</b>：一条词缀栏目的完整定义是「在**（部位 × 落位）**的这一格上，
 * 装什么词缀、取哪一档数值、属于哪一阶」。把词缀放进键，等于把「一格」拆成「一格 × 一条词缀」，
 * 于是同一部位同一落位下换词缀就得新开一个键 —— 键随词缀数量膨胀，而部位 × 落位这两段
 * <b>在整族里是重复无数遍的</b>。词缀进了值之后：</p>
 *
 * <ul>
 *   <li><b>精髓第四阶换词缀</b>（如前三阶 {@code add_attack_damage}、第四阶换
 *       {@code attack_physical_damage_inc}）与<b>符文四阶只换数值</b>，是同一件事 ——
 *       一格 = 一阶，格里的 {@code type} 想换就换，两侧构造因此同构；</li>
 *   <li>同部位同落位下<b>同一阶并列多条词缀</b>（符文一阶同时给 {@code add_physical_damage}
 *       与 {@code extra_lightning_damage}）就是一个格里的两条，不必各占一键。</li>
 * </ul>
 *
 * <p><b>阶是本记录自带的字段</b>（不是数组下标）：故 {@link BonusFamily#tiers()} 直接由值中出现的
 * {@link Tier} 推导，不存在「下标 → 阶」的隐式约定。</p>
 *
 * <p><b>{@code type} 存 {@link AdditionType} 本身</b>，不是它的名字 / 路径字符串 —— 与
 * {@link Bonus} 同一口径：消费端直接取用，无需「字符串 → 效果」翻译表，写错在编译期即暴露。</p>
 *
 * <p><b>区间语义按载体区分</b>：符文的 min~max 是伤害区间本身（取中值落定、不随机）；
 * 精髓的 min~max 是取值范围（词缀作为前后缀参与 roll，生成时随机一次）。</p>
 *
 * @param type 本格所装的词缀（{@code AdditionTypes} 分派，任意实现）
 * @param min  幅度下界（按上述载体语义解释）
 * @param max  幅度上界
 * @param tier 所属阶（声明侧不写，由 {@code tier(...)} 的调用次序注入）
 */
public record TierBonus(AdditionType type, double min, double max, Tier tier) {

    public static final Codec<TierBonus> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AdditionType.CODEC.fieldOf("type").forGetter(TierBonus::type),
            Codec.DOUBLE.fieldOf("min").forGetter(TierBonus::min),
            Codec.DOUBLE.fieldOf("max").forGetter(TierBonus::max),
            Tier.CODEC.fieldOf("tier").forGetter(TierBonus::tier)
    ).apply(instance, TierBonus::new));
}
