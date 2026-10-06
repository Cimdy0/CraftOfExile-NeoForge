package com.cimdy.coe.data.codec.item.addition;

import com.cimdy.coe.data.codec.item.addition.impl.GlobalAddition;
import com.cimdy.coe.data.codec.item.addition.impl.LocalAddition;
import com.cimdy.coe.data.codec.item.addition.impl.MultiAddition;
import net.minecraft.resources.Identifier;

/**
 * 爬取词缀生成器专用的 {@link AdditionType} 常量集（承接 {@link AdditionType} 既有常量之外、
 * 爬取家族所需但尚缺的语义：双占位 added 伤害、额外伤害转化、威压光环、局部/全局多 stat 组合等）。
 *
 * <h2>作用域纪律（关键）</h2>
 * <ul>
 *   <li>全局角色属性（抗性 / 属性 / 生命 / 法力 / 全局伤害% / 威压光环 / 额外伤害转化 / 药剂药草）一律经
 *       {@link AdditionType#global(Identifier...)}（{@code type="global"}）承载。</li>
 *   <li>局部装备属性（护甲 / 闪避 / 能量护盾 / 暴击 / 灵魄 / 格挡 / 属性需求降低 / 攻击范围伤害）一律经
 *       {@link LocalAddition}（{@code type="local"}）承载——绝不可塞进 {@link GlobalAddition}。</li>
 *   <li>单值多 stat 组合（同一 roll 值驱动多个同类 stat，如 X% 提高 护甲/闪避/能量护盾、X% 提高
 *       火焰/冰霜抗性）写一个 {@link LocalAddition} 或 {@code global(...)} 实例（多 ref 共享一个值），
 *       <b>不</b>用多条 AdditionType 串接。</li>
 *   <li>仅当一条词缀<b>真的有两个不同 roll 值</b>（如「X% 提高护甲 且 Y 提高生命」）或<b>跨作用域</b>
 *       （局部 % + 全局生命）时，才用 {@link MultiAddition}（如 {@code MultiAddition.of(a, b)}）组合两个 AdditionType。</li>
 * </ul>
 */
public final class ModAdditions {

    // ── 双占位 added 伤害（min / max，全局；引用 add_*_damage，两 effect 各占一值槽）──

    public static final AdditionRead ADD_COLD_DAMAGE_MINMAX = AdditionRead.of("add_cold_damage_minmax");
    public static final AdditionRead ADD_FIRE_DAMAGE_MINMAX = AdditionRead.of("add_fire_damage_minmax");
    public static final AdditionRead ADD_LIGHTNING_DAMAGE_MINMAX = AdditionRead.of("add_lightning_damage_minmax");
    public static final AdditionRead ADD_PHYSICAL_DAMAGE_MINMAX = AdditionRead.of("add_physical_damage_minmax");
    public static final AdditionRead GAIN_COLD_DAMAGE_AS_EXTRA = AdditionRead.of("gain_cold_damage_as_extra");
    public static final AdditionRead GAIN_FIRE_DAMAGE_AS_EXTRA = AdditionRead.of("gain_fire_damage_as_extra");
    public static final AdditionRead GAIN_LIGHTNING_DAMAGE_AS_EXTRA = AdditionRead.of("gain_lightning_damage_as_extra");
    public static final AdditionRead GAIN_PHYSICAL_DAMAGE_AS_EXTRA = AdditionRead.of("gain_physical_damage_as_extra");
    public static final AdditionRead PRESENCE_DAMAGE = AdditionRead.of("presence_damage");
    public static final AdditionRead PRESENCE_CRIT_CHANCE = AdditionRead.of("presence_crit_chance");
    public static final AdditionRead PRESENCE_CRIT_DAMAGE = AdditionRead.of("presence_crit_damage");
    public static final AdditionRead PRESENCE_ATTACK_SPEED = AdditionRead.of("presence_attack_speed");
    public static final AdditionRead PRESENCE_CAST_SPEED = AdditionRead.of("presence_cast_speed");
    public static final AdditionRead PRESENCE_LIFE_REGEN = AdditionRead.of("presence_life_regen");
    public static final AdditionRead PRESENCE_ALL_ELEMENTAL_RESISTANCES = AdditionRead.of("presence_all_elemental_resistances");
    public static final AdditionRead ADD_COLD_DAMAGE_TO_ATTACKS = AdditionRead.of("add_cold_damage_to_attacks");
    public static final AdditionRead ADD_FIRE_DAMAGE_TO_ATTACKS = AdditionRead.of("add_fire_damage_to_attacks");
    public static final AdditionRead ADD_LIGHTNING_DAMAGE_TO_ATTACKS = AdditionRead.of("add_lightning_damage_to_attacks");
    public static final AdditionRead ADD_PHYSICAL_DAMAGE_TO_ATTACKS = AdditionRead.of("add_physical_damage_to_attacks");
    public static final AdditionRead THORNS = AdditionRead.of("thorns");
    public static final AdditionRead ALL_ATTRIBUTES = AdditionRead.of("all_attributes");
    public static final AdditionRead ALL_RESISTANCES = AdditionRead.of("all_resistances");
    public static final AdditionRead ADD_STRENGTH = AdditionRead.of("add_strength");
    public static final AdditionRead ADD_DEXTERITY = AdditionRead.of("add_dexterity");
    public static final AdditionRead ADD_INTELLIGENCE = AdditionRead.of("add_intelligence");
    public static final AdditionRead LEECH_TRIGGER = AdditionRead.of("leech_trigger");
    public static final AdditionRead LEECH_HEALTH_INC = AdditionRead.of("leech_health_inc");
    public static final AdditionRead LEECH_MANA_INC = AdditionRead.of("leech_mana_inc");
    public static final AdditionRead ADD_MAX_HEALTH = AdditionRead.of("add_max_health");
    public static final AdditionRead ADD_MAX_MANA = AdditionRead.of("add_max_mana");
    public static final AdditionRead ADD_MAX_RUNIC_WARD = AdditionRead.of("add_max_runic_ward");
    public static final AdditionRead MAX_HEALTH_INC = AdditionRead.of("max_health_inc");
    public static final AdditionRead MAX_MANA_INC = AdditionRead.of("max_mana_inc");
    public static final AdditionRead ADD_HEALTH_REGENERATION_PER_SECOND = AdditionRead.of("add_health_regeneration_per_second");
    public static final AdditionRead HEALTH_REGENERATION_PERCENTAGE_PER_SECOND = AdditionRead.of("health_regeneration_percentage_per_second");
    public static final AdditionRead MANA_REGENERATION_RATE_INC = AdditionRead.of("mana_regeneration_rate_inc");
    public static final AdditionRead ENERGY_RECHARGE_RATE_INC = AdditionRead.of("energy_recharge_rate_inc");
    public static final AdditionRead ADD_ARMOR_LOCAL = AdditionRead.of("add_armor_local");
    public static final AdditionRead ARMOR_INC_LOCAL = AdditionRead.of("armor_inc_local");
    public static final AdditionRead ADD_EVASION_LOCAL = AdditionRead.of("add_evasion_local");
    public static final AdditionRead EVASION_INC_LOCAL = AdditionRead.of("evasion_inc_local");
    public static final AdditionRead ADD_ENERGY_LOCAL = AdditionRead.of("add_energy_local");
    public static final AdditionRead ENERGY_INC_LOCAL = AdditionRead.of("energy_inc_local");
    public static final AdditionRead ARMOR_ENERGY_INC_LOCAL = AdditionRead.of("armor_energy_inc_local");
    public static final AdditionRead BLOCK_CHANCE_INC_LOCAL = AdditionRead.of("block_chance_inc_local");
    public static final AdditionRead ADD_MAX_SPIRIT = AdditionRead.of("add_max_spirit");
    public static final AdditionRead PHYSICAL_DAMAGE_INC_LOCAL = AdditionRead.of("physical_damage_inc_local");
    public static final AdditionRead ADD_ACCURACY = AdditionRead.of("add_accuracy");
    public static final AdditionRead ATTACK_SPEED_INC = AdditionRead.of("attack_speed_inc");
    public static final AdditionRead CRIT_CHANCE_INC = AdditionRead.of("crit_chance_inc");
    public static final AdditionRead ADD_CRIT_DAMAGE = AdditionRead.of("add_crit_damage");
    public static final AdditionRead ATTACK_DAMAGE_INC = AdditionRead.of("attack_damage_inc");
    public static final AdditionRead ATTACK_PHYSICAL_DAMAGE_INC = AdditionRead.of("attack_physical_damage_inc");
    public static final AdditionRead FIRE_DAMAGE_INC = AdditionRead.of("fire_damage_inc");
    public static final AdditionRead COLD_DAMAGE_INC = AdditionRead.of("cold_damage_inc");
    public static final AdditionRead LIGHTNING_DAMAGE_INC = AdditionRead.of("lightning_damage_inc");
    public static final AdditionRead CHAOS_DAMAGE_INC = AdditionRead.of("chaos_damage_inc");
    public static final AdditionRead ADD_FIRE_DAMAGE = AdditionRead.of("add_fire_damage");
    public static final AdditionRead ADD_COLD_DAMAGE = AdditionRead.of("add_cold_damage");
    public static final AdditionRead ADD_LIGHTNING_DAMAGE = AdditionRead.of("add_lightning_damage");
    public static final AdditionRead ADD_ATTACK_DAMAGE = AdditionRead.of("add_attack_damage");
    public static final AdditionRead ATTACK_ELEMENTAL_DAMAGE_INC = AdditionRead.of("attack_elemental_damage_inc");
    public static final AdditionRead MOVEMENT_SPEED_INC = AdditionRead.of("movement_speed_inc");
    public static final AdditionRead PROJECTILE_SPEED_INC = AdditionRead.of("projectile_speed_inc");
    public static final AdditionRead LAUNCH_ADDITIONAL_PROJECTILE_BOLT = AdditionRead.of("launch_additional_projectile_bolt");
    public static final AdditionRead PROJECTILE_PIERCE = AdditionRead.of("projectile_pierce");
    public static final AdditionRead ADD_FIRE_RESISTANCE = AdditionRead.of("add_fire_resistance");
    public static final AdditionRead ADD_COLD_RESISTANCE = AdditionRead.of("add_cold_resistance");
    public static final AdditionRead ADD_LIGHTNING_RESISTANCE = AdditionRead.of("add_lightning_resistance");
    public static final AdditionRead ADD_CHAOS_RESISTANCE = AdditionRead.of("add_chaos_resistance");
    public static final AdditionRead ADD_MAX_FIRE_RESISTANCE = AdditionRead.of("add_max_fire_resistance");
    public static final AdditionRead ADD_MAX_COLD_RESISTANCE = AdditionRead.of("add_max_cold_resistance");
    public static final AdditionRead ADD_MAX_LIGHTNING_RESISTANCE = AdditionRead.of("add_max_lightning_resistance");
    public static final AdditionRead ADD_MAX_CHAOS_RESISTANCE = AdditionRead.of("add_max_chaos_resistance");
    public static final AdditionRead ADD_MAX_ELEMENTAL_RESISTANCE = AdditionRead.of("add_max_elemental_resistance");
    public static final AdditionRead ADD_STUN_THRESHOLD = AdditionRead.of("add_stun_threshold");
    public static final AdditionRead STUN_THRESHOLD_INC = AdditionRead.of("stun_threshold_inc");
    public static final AdditionRead STUN_DURATION_INC = AdditionRead.of("stun_duration_inc");
    public static final AdditionRead CAUSES_STUN_BUILDUP_INC = AdditionRead.of("causes_stun_buildup_inc");
    public static final AdditionRead FOUND_ITEM_RARITY_INC = AdditionRead.of("found_item_rarity_inc");
    public static final AdditionRead SPELL_DAMAGE_INC = AdditionRead.of("spell_damage_inc");
    public static final AdditionRead ADD_ELEMENTAL_DAMAGE = AdditionRead.of("add_elemental_damage");
    public static final AdditionRead ADD_PHYSICAL_DAMAGE = AdditionRead.of("add_physical_damage");
    public static final AdditionRead AILMENT_MAGNITUDE_FREEZE = AdditionRead.of("ailment_magnitude_freeze");
    public static final AdditionRead FLASK_MANA_RECOVERY = AdditionRead.of("flask_mana_recovery");
    public static final AdditionRead FOUND_ITEM_RARITY = AdditionRead.of("found_item_rarity");
    public static final AdditionRead MINION_ATTACK_SPEED = AdditionRead.of("minion_attack_speed");
    public static final AdditionRead PENETRATE_CHAOS = AdditionRead.of("penetrate_chaos");
    public static final AdditionRead PHYSICAL_TAKEN_AS_COLD = AdditionRead.of("physical_taken_as_cold");
    public static final AdditionRead PHYSICAL_TAKEN_AS_LIGHTNING = AdditionRead.of("physical_taken_as_lightning");
    public static final AdditionRead PHYSICAL_TAKEN_AS_CHAOS = AdditionRead.of("physical_taken_as_chaos");
    public static final AdditionRead EXTRA_FIRE_DAMAGE = AdditionRead.of("extra_fire_damage");
    public static final AdditionRead EXTRA_COLD_DAMAGE = AdditionRead.of("extra_cold_damage");
    public static final AdditionRead EXTRA_LIGHTNING_DAMAGE = AdditionRead.of("extra_lightning_damage");
    public static final AdditionRead EXTRA_PHYSICAL_DAMAGE = AdditionRead.of("extra_physical_damage");
    public static final AdditionRead CONVERT_EXTRA_PHYS_TO_FIRE = AdditionRead.of("convert_extra_phys_to_fire");

    public static final AdditionRead ADD_ELEMENTAL_RESISTANCE = AdditionRead.of("add_elemental_resistance");
    public static final AdditionRead ARMOR_EVASION_INC_LOCAL = AdditionRead.of("armor_evasion_inc_local");
    public static final AdditionRead ENERGY_EVASION_INC_LOCAL = AdditionRead.of("energy_evasion_inc_local");
    public static final AdditionRead ATTACK_SPEED_INC_LOCAL = AdditionRead.of("attack_speed_inc_local");
    public static final AdditionRead WEAPON_ELEMENTAL_DAMAGE_PERCENT = AdditionRead.of("weapon_elemental_damage_percent");

private ModAdditions() {
    }
}
