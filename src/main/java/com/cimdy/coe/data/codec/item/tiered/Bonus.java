package com.cimdy.coe.data.codec.item.tiered;

import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * 单条加成：解析到某一阶后的原子 —— <b>词缀 {@code type} + 落位 {@code location} + 幅度 {@code min}~{@code max}</b>。
 *
 * <p><b>精髓的值 = 词缀 × 落位 × 阶</b>：本记录承载其中「词缀 + 落位 + 幅度」，阶由宿主条目持有
 * （{@link BonusFamily#resolve(Tier)} 按阶筛出本记录）。三者合起来才是一条词缀的完整定义 ——
 * 缺落位则不知占前缀还是后缀，缺阶则不知取哪一档数值。</p>
 *
 * <p><b>落位可空 = 该词缀没有落位概念</b>：符文不是下缀、占不了前后缀档位，落位对它<b>没有意义</b>，
 * 故符文族一律 {@link Optional#empty()}；
 * 精髓词缀逐条声明落位（{@link Location#PREFIX} / {@link Location#SUFFIX}），
 * 同族不同词缀可以各占各的 —— 所以落位<b>不在族层、不在条目层</b>，而是本记录的字段。</p>
 *
 * <p><b>宿主部位不是本记录的字段</b>：部位（分支表达式，如 {@code armor} / {@code weapon, !wand} /
 * {@code *}）由运行侧存储 {@code Map<String, List<Bonus>>} 的 <b>key</b> 承载（{@code RuneState} /
 * {@code EssenceState} 的 {@code branches}），供 {@code BranchExpr.collect} 按宿主类型链求值。</p>
 *
 * <p>{@code type} = 原生 {@link AdditionType} <b>本身</b>（作者侧以 {@link com.cimdy.coe.data.codec.item.addition.AdditionRead}
 * 常量声明），<b>不是</b>常量名字符串 —— 消费端直接取用，无需「字符串 → 效果」翻译表，键写错在编译期即暴露。
 * {@code min}/{@code max} 为该阶的幅度区间。</p>
 *
 * <p><b>区间语义按载体区分</b>：符文的 min~max 是「附加 X 到 Y 点伤」的伤害区间本身（取中值落定、不随机）；
 * 精髓的 min~max 是取值范围（词缀作为前后缀参与 roll，生成时随机一次）。</p>
 */
public record Bonus(double min, double max, AdditionType type, Optional<Location> location) {

    /**
     * 落位字段编码：<b>缺字段即 {@link Optional#empty()}（符文无落位）、有字段即 {@code Optional.of(...)}</b>。
     *
     * <p>用的是 {@code optionalFieldOf} 的<b>单参重载</b>，而非 {@code optionalFieldOf(name, 默认值)} ——
     * 后者会在缺字段时填一个具体落位，把「无落位」偷换成「有落位」；本重载缺字段即 {@code Optional.empty()}，
     * 正是本记录想要的语义。</p>
     */
    public static final MapCodec<Optional<Location>> LOCATION_CODEC =
            Location.CODEC.optionalFieldOf("location");

    public static final Codec<Bonus> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("min").forGetter(Bonus::min),
            Codec.DOUBLE.fieldOf("max").forGetter(Bonus::max),
            AdditionType.CODEC.fieldOf("type").forGetter(Bonus::type),
            LOCATION_CODEC.forGetter(Bonus::location)
    ).apply(instance, Bonus::new));
}
