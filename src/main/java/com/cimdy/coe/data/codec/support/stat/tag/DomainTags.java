package com.cimdy.coe.data.codec.support.stat.tag;

import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 词缀领域标签派生（D6 终态，team-lead 2026-09-19 裁定）：领域标签的<b>唯一权威来源是 StatEffect</b>。
 * 本工具从效果的<b>事件标签 {@link StatEffect#tags()}（{@link GateExpr}）</b>中<b>自动抽取</b>领域维度，
 * 无需在别处逐词缀簿记精选表（旧 {@code GateTagRegistry.seedStatic} 已删除）。
 *
 * <h2>为什么从事件标签抽取</h2>
 * 效果构造时本就在 {@code tags} 中声明了 fire/cold/bleed/ailment/spell/attack 等维度
 * （供 {@link GateExpr#matches} / {@link StatEngine#conditionsHold} 做触发作用域过滤），这些维度<b>与领域分类同义</b>。
 * 因此「在其构造处声明领域」=「在其构造处的事件标签里写 fire」——
 * 新增一个 fire 类效果只需在事件标签写 "fire"，自动继承 fire + elemental 域，零额外登记。
 *
 * <h2>词汇白名单 + 父类别展开</h2>
 * 仅抽取 {@link #VOCAB} 中的领域词（排除 hit/kill/crit/taken 等纯触发作用域词）；
 * 并展开父类别（fire→elemental、armor→defence、bleed→ailment+damage_over_time，
 * 复用 {@link GateTag#parentsOf}），使「无法 roll 元素/防御/异常」类工艺可一次性覆盖全部子维度。
 *
 * <p>显式 {@code domainTags} 字段（{@code AbstractStatEffect} 构造参数 / JSON {@code domain} 字段）作为
 * 覆盖逃生舱保留：当其非空时 {@link AbstractStatEffect#domainTags()} 直接返回它，否则回退到本派生。
 */
public final class DomainTags {

    /** 领域词汇：词缀分类维度白名单（排除纯触发/作用域词：hit/kill/crit/taken/self/aura…）。 */
    private static final Set<String> VOCAB = Set.of(
            // 伤害元素（共存、各自独立；elemental 为父类别）
            GateTag.FIRE, GateTag.COLD, GateTag.LIGHTNING, GateTag.CHAOS, GateTag.PHYSICAL, GateTag.ELEMENTAL,
            // 释放 / 命中形态
            GateTag.ATTACK, GateTag.SPELL, GateTag.PROJECTILE, GateTag.AREA, GateTag.MELEE,
            GateTag.BOW, GateTag.CROSSBOW, GateTag.AXE, GateTag.MACE, GateTag.SPEAR, GateTag.QUARTERSTAFF,
            // 异常状态（ailment 为父类别；dot 维度单独列出）
            GateTag.AILMENT, GateTag.BLEED, GateTag.POISON, GateTag.IGNITE, GateTag.SHOCK,
            GateTag.FREEZE, GateTag.CHILL, GateTag.ELECTROCUTE, GateTag.PIN, GateTag.STUN,
            // 防御（defence 为父类别）
            GateTag.ARMOR, GateTag.EVASION, GateTag.ENERGY_SHIELD, GateTag.BLOCK, GateTag.RESISTANCE,
            GateTag.DEFLECT, GateTag.PARRY, GateTag.GUARD, GateTag.DEFENCE,
            // 资源
            GateTag.HEALTH, GateTag.MANA, GateTag.ENERGY, GateTag.SPIRIT,
            // 杂项维度
            GateTag.ACCURACY, GateTag.CRIT, GateTag.DAMAGE, GateTag.DAMAGE_OVER_TIME,
            GateTag.MOVEMENT, GateTag.RANGE, GateTag.SLOW
    );

    private DomainTags() {}

    /** 从 {@link GateExpr} 抽取领域标签（含父类别展开）。 */
    public static Set<String> fromTags(GateExpr tags) {
        Set<String> out = new HashSet<>();
        for (String leaf : tags.leaves()) {
            String t = leaf.startsWith("!") ? leaf.substring(1) : leaf;
            if (VOCAB.contains(t)) {
                out.add(t);
                out.addAll(GateTag.parentsOf(t));
            }
        }
        return Set.copyOf(out);
    }

    /**
     * 属性 → 领域（FOLD 型 ModifierStatEffect 的域来源）：纯属性加成效果（+X 闪避 / +X 护盾 /
     * +X 格挡 / +X 抗性 / +X 魔力）的事件标签通常是 {@link GateExpr#ALWAYS}（无域 token），
     * 无法靠 {@link #fromTags} 抽取；但其<b>修饰的属性本身即域信号</b>
     * （闪避→evasion、护盾→energy_shield、抗性→resistance、魔力→mana…）。
     * 故以「属性注册键 → 领域」的<b>有界词汇</b>派生，仅覆盖防御 / 资源维度，
     * 属属性级登记（~十项），非逐词缀簿记，符合「不逐词缀登记」铁律；并展开父类别
     * （evasion/energy_shield/block/resistance/armor → defence）。
     * <p>此表只认得关键防御 / 资源属性；一般属性（力量 / 敏捷 / 智慧）不在此表，自然无域（它们非化石排除维度）。
     */
    private static final Map<String, String> ATTR_DOMAIN = Map.ofEntries(
            Map.entry("coe:inc_evasion", GateTag.EVASION),
            Map.entry("coe:max_energy_shield", GateTag.ENERGY_SHIELD),
            Map.entry("coe:max_block", GateTag.BLOCK),
            Map.entry("coe:block_chance", GateTag.BLOCK),
            Map.entry("coe:resistances", GateTag.RESISTANCE),
            Map.entry("coe:max_resistances", GateTag.RESISTANCE),
            Map.entry("coe:resistance_penetration", GateTag.RESISTANCE),
            Map.entry("coe:max_mana", GateTag.MANA),
            Map.entry("coe:mana_take_of_damage", GateTag.MANA_TAKE),
            Map.entry("coe:armor", GateTag.ARMOR),
            Map.entry("minecraft:generic.armor", GateTag.ARMOR),
            Map.entry("minecraft:generic.max_health", GateTag.HEALTH)
    );

    /** 从效果修饰的属性派生领域标签（含父类别展开）。属性键未知则返回空。 */
    public static Set<String> fromAttribute(Holder<Attribute> attribute) {
        if (attribute == null) return Set.of();
        String key = attribute.unwrapKey().map(k -> k.identifier().toString()).orElse("");
        String domain = ATTR_DOMAIN.get(key);
        if (domain == null) return Set.of();
        Set<String> out = new HashSet<>();
        out.add(domain);
        out.addAll(GateTag.parentsOf(domain));
        return Set.copyOf(out);
    }
}
