package com.cimdy.coe.data.codec.item.currency;

import com.cimdy.coe.data.codec.support.rarity.Rarity;

import java.util.List;
import java.util.Set;

/**
 * 通货前置条件（数据驱动）。所有 orb 原先重复粘贴的
 * 镜像/鉴定/稀有度/类型 校验在此统一表达，由 {@link com.cimdy.coe.item.currency.CurrencyApplier} 执行。
 *
 * <p>{@code domain} 声明本通货可作用于哪一类物品（装备 / 武器 / 护甲 / 药剂 / 护符 /
 * 任意词缀载体 / 技能石）——「作用于什么」因此成为数据：技能石与药剂 / 护符类通货无需再改分发器
 * 即可接入。稀有度门槛与域是<b>正交</b>的两个轴：域答「哪类物品」，稀有度答「什么档位」。</p>
 */
public record Precondition(
        Set<Rarity> allowedRarities,
        boolean blockMirror,
        boolean requireUnidentified,
        boolean requireType,
        TargetDomain domain) {

    /** 不限制任何稀有度（用于鉴定等rare-agnostic 通货）。 */
    public static final Precondition ALLOW_ALL = new Precondition(Set.of(), false, false, false);

    /** 兼容构造器：缺省作用于装备域。 */
    public Precondition(Set<Rarity> allowedRarities, boolean blockMirror, boolean requireUnidentified, boolean requireType) {
        this(allowedRarities, blockMirror, requireUnidentified, requireType, TargetDomain.EQUIPMENT);
    }

    /** 默认（装备域）：限制指定稀有度，且镜像阻断、要求未鉴定、要求有类型。 */
    public static Precondition of(Rarity... allowed) {
        return new Precondition(Set.of(allowed), true, true, true);
    }

    /**
     * 指定目标域 + 稀有度门槛（词缀 / 稀有度类通货的主力构造）。
     * 「是否要求类型已解析」由域自身声明（{@link TargetDomain#isEquipmentLike()}），
     * 避免域与要求各写一份而彼此漂移。
     */
    public static Precondition of(TargetDomain domain, Rarity... allowed) {
        return new Precondition(Set.of(allowed), true, true, domain.isEquipmentLike(), domain);
    }

    public static Precondition of(List<Rarity> allowed, boolean blockMirror, boolean requireUnidentified, boolean requireType) {
        return new Precondition(Set.copyOf(allowed), blockMirror, requireUnidentified, requireType);
    }

    /**
     * 按目标域构造（用于非「稀有度敏感」通货：品质 / 孔类）。
     * 不限制稀有度，但沿用镜像阻断与「需已鉴定」；依赖类型链的域额外要求类型已解析。
     */
    public static Precondition ofDomain(TargetDomain domain) {
        return new Precondition(Set.of(), true, true, domain.isEquipmentLike(), domain);
    }
}
