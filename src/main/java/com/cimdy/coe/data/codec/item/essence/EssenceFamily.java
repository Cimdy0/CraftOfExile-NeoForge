package com.cimdy.coe.data.codec.item.essence;

import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.tiered.BonusFamily;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 精髓定义（一文件 = 一精髓）：<b>加成集合 {@code bonuses}</b>。
 *
 * <h2>精髓没有「族」这层抽象</h2>
 * 精髓名（PoE2 的商品名：{@code Abrasion} / {@code Battle} / …）<b>不代表效果</b>，且无需存储 ——
 * 条目身份所需的 Identifier 与互斥所需的「族」都<b>直接从词缀 {@code AdditionType} 取</b>
 * （与 {@code CommonMod} 同一个来源）。故本类<b>不存名称、不存族</b>，只存加成集合。
 *
 * <h2>冲突不靠名称，靠词缀 Identifier</h2>
 * 精髓之间以及精髓与普通词缀之间的互斥，一律由<b>词缀身份</b>判定 —— 即 {@code AdditionType} 自带的
 * {@code Identifier}，再按冲突表查表。名称不参与任何互斥逻辑：两个名称不同的精髓若施加同一条词缀，
 * 照样互斥。
 *
 * <h2>落位不在这里</h2>
 * 词缀落 {@code PREFIX} / {@code SUFFIX}（占据一条原生前后缀）是<b>每条词缀自身</b>的维度
 * （同一精髓族的不同词缀可以一个落前缀、一个落后缀），见 {@code Bonus} 的 {@code Optional<Location>}；
 * 本类（族）与 {@code EssenceState}（条目）都不持有落位 —— 一律前缀只是语料未定时的临时口径，
 * 一旦补齐就不需要改数据结构。
 *
 * <p>运行时由 {@link EssenceRegistry} 解析本记录的 {@code CODEC}，取 {@link #bonuses()} 按阶展开为
 * 多条 {@link EssenceState}（一名字 × 一阶）。</p>
 */
public record EssenceFamily(BonusFamily bonuses) {

    public static final Codec<EssenceFamily> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BonusFamily.CODEC.fieldOf("bonuses").forGetter(EssenceFamily::bonuses)
    ).apply(instance, EssenceFamily::new));

    /** 精髓族工厂（落位随各条加成声明，本层不设默认）。 */
    public static EssenceFamily of(BonusFamily bonuses) {
        return new EssenceFamily(bonuses);
    }
}
