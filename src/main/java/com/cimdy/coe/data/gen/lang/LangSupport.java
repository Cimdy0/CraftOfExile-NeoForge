package com.cimdy.coe.data.gen.lang;

import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.Convert;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;

import java.util.Map;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.support.require.Require;

public final class LangSupport {
    private final LangProvider provider;
    private final String locale;

    LangSupport(LangProvider provider, String locale) {
        this.provider = provider;
        this.locale = locale;
    }

    public LangProvider provider() {
        return this.provider;
    }

    public void add(String key, String value) {
        this.provider.add(key, value);
    }

    public void addZh(String key, String zh) {
    if (!"zh_cn".equals(this.locale)) {
            this.add(key, englishFor(key));
            return;
        }
        this.add(key, zh);
    }

    public void addPair(String key, String en, String zh) {
        this.add(key, "zh_cn".equals(this.locale) ? zh : en);
    }

    private static String englishFor(String key) {
        String explicit = STAT_EN.get(key);
    if (explicit != null) return explicit;
        int dot = key.lastIndexOf('.');
        return ValueExpr.humanize(dot < 0 ? key : key.substring(dot + 1));
    }

    private static final Map<String, String> STAT_EN = Map.ofEntries(
            Map.entry("stat.armor", "Armor"),
            Map.entry("stat.level", "Level"),
            Map.entry("stat.low", "Low"),
            Map.entry("stat.high", "High"),
            Map.entry("stat.full", "Full"),
            Map.entry("stat.crit", "Crit"),
            Map.entry("stat.number", "Count"),
            Map.entry("stat.health", "Health"),
            Map.entry("stat.mana", "Mana"),
            Map.entry("stat.spirit", "Spirit"),
            Map.entry("stat.energy", "Energy Shield"),
            Map.entry("stat.light", "Lightning"),
            Map.entry("stat.fire", "Fire"),
            Map.entry("stat.cold", "Cold"),
            Map.entry("stat.chaos", "Chaos"),
            Map.entry("stat.physical", "Physical"),
            Map.entry("stat.damage", "Damage"),
            Map.entry("stat.resistance", "Resistance"),
            Map.entry("stat.evasion", "Evasion"),
            Map.entry("stat.block", "Block"),
            Map.entry("stat.ignite", "Ignite"),
            Map.entry("stat.poison", "Poison"),
            Map.entry("stat.shock", "Shock"),
            Map.entry("stat.chill", "Chill"),
            Map.entry("stat.bleed", "Bleed"),
            Map.entry("stat.freeze", "Freeze"),
            Map.entry("stat.enemy", "Enemy"),
            Map.entry("stat.movement", "Movement"),
            Map.entry("stat.reload", "Reload"),
            Map.entry("stat.leeches", "Leech"),
            Map.entry("stat.stun", "Stun"),
            Map.entry("stat.accuracy", "Accuracy"),
            Map.entry("stat.element", "Elemental"),
            Map.entry("stat.elemental", "Elemental"),
            Map.entry("stat.critical", "Critical"),
            Map.entry("stat.attack", "Attack"),
            Map.entry("stat.chance", "Chance"),
            Map.entry("stat.duration", "Duration"),
            Map.entry("stat.defences", "Defences"),
            Map.entry("stat.recharge", "Recharge"),
            Map.entry("stat.cross_bow", "Crossbow"),
            Map.entry("stat.projectile", "Projectile"),
            Map.entry("stat.rarity", "Rarity"),
            Map.entry("stat.skill", "Skill"),
            Map.entry("stat.spell", "Spell"),
            Map.entry("stat.hit", "Hit"),
            Map.entry("stat.time", "Time"),
            Map.entry("stat.item", "Item"),
            Map.entry("stat.cost", "Cost"),
            Map.entry("stat.cooldown", "Cooldown"),
            Map.entry("stat.speed", "Speed"),
            Map.entry("stat.strength", "Strength"),
            Map.entry("stat.dexterity", "Dexterity"),
            Map.entry("stat.intelligence", "Intelligence"),
            Map.entry("stat.buff", "Buff"),
            Map.entry("stat.second", "Second"),
            Map.entry("stat.rate", "Rate"),
            Map.entry("stat.faster", "Faster"),
            Map.entry("stat.taken", "Taken"),
            Map.entry("stat.causes", "Causes"),
            Map.entry("stat.buildup", "Buildup"),
            Map.entry("stat.thorns", "Thorns"),
            Map.entry("stat.found", "Found"),
            Map.entry("stat.launch", "Launch"),
            Map.entry("stat.area", "Area"),
            Map.entry("stat.bolt", "Bolt"),
            Map.entry("stat.arrow", "Arrow"),
            Map.entry("stat.bow", "Bow"),
            Map.entry("stat.melee", "Melee"),
            Map.entry("stat.base", "Base"),
            Map.entry("stat.support", "Support"),
            Map.entry("stat.aura", "Aura"),
            Map.entry("stat.require", "Require"),
            Map.entry("stat.apply", "Apply"),
            Map.entry("stat.attribute", "Attribute"),
            Map.entry("stat.weakness", "Weakness"),
            Map.entry("stat.global", "Global"),
            Map.entry("stat.max", "Max"),
            Map.entry("stat.s", "'s"),
            Map.entry("stat.per", "per"),
            Map.entry("stat.aoe", "AoE"),
            Map.entry("stat.inc", "Increased"),
            Map.entry("stat.reduced", "Reduced"),
            Map.entry("stat.addition", "Additional"),
            Map.entry("stat.additional", "Additional"),
            Map.entry("stat.on_hit", "On Hit"),
            Map.entry("stat.to_poison", "Causes Poison"),
            Map.entry("stat.to_maim", "Causes Maim"),
            Map.entry("stat.to_bleeding", "Causes Bleeding"),
            Map.entry("stat.skill.title", "Skill Assembly"),
            Map.entry("stat.skill.aura_on", "Aura"),
            Map.entry("stat.skill.section.assembly", "Assembly"),
            Map.entry("stat.skill.section.inventory", "Inventory"),
            // ── attribute.name.* 英文质量覆盖（humanize 会拆错缩写 / 非英文短语，见 loc_copy_table §C 落地）──
            Map.entry("attribute.name.stun_threshold_from_es", "Stun Threshold from Energy Shield"),
            Map.entry("attribute.name.curse_target_es_recharge_disabled", "Curse Target Energy Shield Recharge Disabled"),
            Map.entry("attribute.name.quiver_bonus_inc", "Quiver Bonus"),
            Map.entry("attribute.name.archon_recovery_period_mult", "Archon Recovery Period Multiplier"),
            Map.entry("attribute.name.buff_duration_mult", "Buff Duration Multiplier"),
            Map.entry("attribute.name.debuff_duration_mult", "Debuff Duration Multiplier"),
            Map.entry("attribute.name.duration_mult", "Duration Multiplier"),
            Map.entry("attribute.name.cross_bow_reload_time", "Crossbow Reload Time"),
            // marker word-key English: aligned with ModLang stat.<token>
            Map.entry("stat.activation", "Activation"),
            Map.entry("stat.advance", "Advance"),
            Map.entry("stat.ammo", "Ammo"),
            Map.entry("stat.as", "As"),
            Map.entry("stat.at", "At"),
            Map.entry("stat.avoid", "Avoid"),
            Map.entry("stat.banner", "Banner"),
            Map.entry("stat.bonus", "Bonus"),
            Map.entry("stat.both", "Both"),
            Map.entry("stat.category", "Category"),
            Map.entry("stat.cause", "Cause"),
            Map.entry("stat.charge", "Charge"),
            Map.entry("stat.consume", "Consume"),
            Map.entry("stat.consumed", "Consumed"),
            Map.entry("stat.convert", "Convert"),
            Map.entry("stat.corpse", "Corpse"),
            Map.entry("stat.corruption", "Corruption"),
            Map.entry("stat.count", "Count"),
            Map.entry("stat.debilitate", "Debilitate"),
            Map.entry("stat.defend", "Defend"),
            Map.entry("stat.dmg", "Damage"),
            Map.entry("stat.ele", "Element"),
            Map.entry("stat.empty", "Empty"),
            Map.entry("stat.endures", "Endures"),
            Map.entry("stat.entity", "Entity"),
            Map.entry("stat.equip", "Equip"),
            Map.entry("stat.equipment", "Equipment"),
            Map.entry("stat.equipped", "Equipped"),
            Map.entry("stat.es", "Energy Shield"),
            Map.entry("stat.exempt", "Exempt"),
            Map.entry("stat.expended", "Expended"),
            Map.entry("stat.expire", "Expire"),
            Map.entry("stat.extra", "Extra"),
            Map.entry("stat.flask", "Flask"),
            Map.entry("stat.form", "Form"),
            Map.entry("stat.front", "Front"),
            Map.entry("stat.gate", "Gate"),
            Map.entry("stat.glory", "Glory"),
            Map.entry("stat.grenade", "Grenade"),
            Map.entry("stat.guard", "Guard"),
            Map.entry("stat.hands", "Hands"),
            Map.entry("stat.history", "History"),
            Map.entry("stat.human", "Human"),
            Map.entry("stat.ignore", "Ignore"),
            Map.entry("stat.immune", "Immune"),
            Map.entry("stat.inside", "Inside"),
            Map.entry("stat.interval", "Interval"),
            Map.entry("stat.invocation", "Invocation"),
            Map.entry("stat.leech", "Leech"),
            Map.entry("stat.leeched", "Leeched"),
            Map.entry("stat.life", "Life"),
            Map.entry("stat.limit", "Limit"),
            Map.entry("stat.loss", "Loss"),
            Map.entry("stat.magnitude", "Magnitude"),
            Map.entry("stat.martial", "Martial"),
            Map.entry("stat.min", "Min"),
            Map.entry("stat.minions", "Minions"),
            Map.entry("stat.more", "More"),
            Map.entry("stat.no", "No"),
            Map.entry("stat.not", "Not"),
            Map.entry("stat.offering", "Offering"),
            Map.entry("stat.on", "On"),
            Map.entry("stat.outside", "Outside"),
            Map.entry("stat.penalty", "Penalty"),
            Map.entry("stat.quarterstaff", "Quarterstaff"),
            Map.entry("stat.range", "Range"),
            Map.entry("stat.reapply", "Reapply"),
            Map.entry("stat.recent", "Recent"),
            Map.entry("stat.recover", "Recover"),
            Map.entry("stat.reflect", "Reflect"),
            Map.entry("stat.regen", "Regen"),
            Map.entry("stat.requirements", "Requirements"),
            Map.entry("stat.res", "Resistance"),
            Map.entry("stat.retain", "Retain"),
            Map.entry("stat.rule", "Rule"),
            Map.entry("stat.shapeshift", "Shapeshift"),
            Map.entry("stat.shield", "Shield"),
            Map.entry("stat.skip", "Skip"),
            Map.entry("stat.spawn", "Spawn"),
            Map.entry("stat.stage", "Stage"),
            Map.entry("stat.take", "Take"),
            Map.entry("stat.to", "To"),
            Map.entry("stat.travel", "Travel"),
            Map.entry("stat.trigger", "Trigger"),
            Map.entry("stat.tripled", "Tripled"),
            Map.entry("stat.unarmed", "Unarmed"),
            Map.entry("stat.unreserved", "Unreserved"),
            Map.entry("stat.volatility", "Volatility"),
            Map.entry("stat.while", "While"),
            Map.entry("stat.ailment", "Ailment")
    );
}
