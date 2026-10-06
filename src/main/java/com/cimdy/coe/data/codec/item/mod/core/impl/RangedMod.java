package com.cimdy.coe.data.codec.item.mod.core.impl;

import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import net.minecraft.util.RandomSource;

/**
 * <b>范围态</b>词缀：{@link Addition} 的 {@code min}~{@code max} 是<b>取值范围</b>，

 * 落槽前须随机一次落定实际值 —— {@code Addition.from(type, min, max)} 的 {@code value} 为空，
 * 不落定则 tooltip 与属性结算都拿不到实际值。
 *
 * <p>与符文相对：{@link RuneMod} 的 {@code min}~{@code max} 是「附加 X 到 Y 点伤」的
 * <b>伤害区间本身</b>（不是取值范围），按既定口径取 {@code (min+max)/2} 落定、<b>不随机</b>，
 * 故 {@link RuneMod} 与 {@link BaseMod} 不实现本接口（基底无 roll 语义、符文按既定口径落定）。</p>
 *
 * <p>实现者：{@link AffixMod}（下缀：普通词缀 / 精髓词缀）。基底（{@link BaseMod}）与符文（{@link RuneMod}）不实现本接口。</p>
 */
public interface RangedMod extends ModifierEntry {

    /** 以新加成重建自身（保留本条词缀的其余维度：等级 / 名称来源 / 位置 / 冲突值）。 */
    ModifierEntry withAddition(Addition addition);

    /** 落值：对本条加成随机一次并重建（无加成时原样返回 {@code this}）。 */
    default ModifierEntry roll(RandomSource random) {
        Addition a = addition();
        return a == null ? this : withAddition(a.random(random));
    }
}
