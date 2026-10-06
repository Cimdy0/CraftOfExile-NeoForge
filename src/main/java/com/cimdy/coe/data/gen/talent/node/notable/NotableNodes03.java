package com.cimdy.coe.data.gen.talent.node.notable;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision.Explode;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.Exposure;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.Penetration;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.Slow;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.cimdy.coe.data.codec.support.rarity.Rarity;

public final class NotableNodes03 {

    private NotableNodes03() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();
        // [Stat] 10% increased Area Damage
        // [Stat] 12% increased Area of Effect if you have [Stun|Stunned] an Enemy [Recently|Recently]
        list.add(TalentNodeDef.of(
                "7542", -7443.51f, -2242.61f,
                "encompassing_domain", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_IF_YOU_HAVE_STUNNED_AN_ENEMY_RECENTLY, 0.12)
                ),
                List.of("28516", "36737")
        ));
        // [Stat] +1 to Level of all [Cold] Skills
        list.add(TalentNodeDef.of(
                "19955", 244.435f, -11875.1f,
                "endless_blizzard", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_LEVEL_OF_ALL_COLD_SKILLS, 1.0)
                ),
                List.of("52799", "60515")
        ));
        // [Stat] 25% chance on [Consume|Consuming] a [Shock] on an Enemy to reapply it
        list.add(TalentNodeDef.of(
                "8273", 8922.01f, -4178.25f,
                "endless_circuit", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_ON_CONSUMING_A_SHOCK_ON_AN_ENEMY_TO_REAPPLY_IT, 0.25)
                ),
                List.of("25565")
        ));
        // [Stat] Shapeshift Skills have 30% increased Skill Effect Duration
        list.add(TalentNodeDef.of(
                "56890", -8391.24f, -8464.78f,
                "endlessly_soaring", "passives_druidshapeshiftwyvernnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.SHAPESHIFT_SKILLS_HAVE_INC_SKILL_EFFECT_DURATION, 0.3)
                ),
                List.of("28489")
        ));
        // [Stat] +2 to Maximum [Charges|Endurance Charges]
        list.add(TalentNodeDef.of(
                "5663", -7025.42f, 7409.4f,
                "endurance", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_ENDURANCE_CHARGES, 2.0)
                ),
                List.of("7972")
        ));
        // [Stat] 10% of [Physical] Damage taken [Recoup|Recouped] as Life
        // [Stat] 20% increased [Physical] Damage
        list.add(TalentNodeDef.of(
                "15443", 3553.55f, -5454.76f,
                "endured_suffering", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_PHYSICAL_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.1),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.2)
                ),
                List.of("19426", "31928")
        ));
        // [Stat] 30% increased [Archon] Buff duration
        list.add(TalentNodeDef.of(
                "59070", -3544.92f, -9603.35f,
                "enduring_archon", "passives_archongenericnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARCHON_BUFF_DURATION, 0.3)
                ),
                List.of("32309")
        ));
        // [Stat] 20% increased [Evasion|Evasion Rating]
        // [Stat] Prevent +3% of Damage from [Deflect|Deflected] [HitDamage|Hits]
        list.add(TalentNodeDef.of(
                "42103", 6654.65f, 7287.83f,
                "enduring_deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.2),
                        TalentNodeGen.stat(StatIds.PREVENT_OF_DAMAGE_FROM_DEFLECTED_HIT_DAMAGE, 0.03)
                ),
                List.of("331", "62427")
        ));
        // [Stat] 25% chance for [Trigger] skills to refund half of [Energy] Spent
        list.add(TalentNodeDef.of(
                "40399", 6653.08f, -9950.52f,
                "energise", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_TRIGGER_SKILLS_TO_REFUND_HALF_OF_ENERGY_SPENT, 0.25)
                ),
                List.of("61056")
        ));
        // [Stat] 30% increased [Archon] Buff duration
        // [Stat] 20% faster start of [FasterESRechargeStart|Energy Shield Recharge] while affected by an [Archon] Buff
        list.add(TalentNodeDef.of(
                "43633", 4163.94f, -9309.65f,
                "energising_archon", "passives_archongenericnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARCHON_BUFF_DURATION, 0.3),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHILE_AFFECTED_BY_AN_ARCHON_BUFF, 0.2)
                ),
                List.of("40929")
        ));
        // [Stat] 12% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        // [Stat] 6% increased [Deflect|Deflection Rating]
        list.add(TalentNodeDef.of(
                "34541", 8594.95f, -1003.98f,
                "energising_deflection", "passives_energyshieldrechargedeflectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.12),
                        TalentNodeGen.stat(StatIds.INC_DEFLECTION_RATING, 0.06)
                ),
                List.of("52743", "63762")
        ));
        // [Stat] 4% increased Area of Effect for [Attack|Attacks] per Enemy you've [Ignite|Ignited] in the last 8 seconds, up to 40%
        list.add(TalentNodeDef.of(
                "2814", 3360.93f, 10998.1f,
                "engineered_blaze", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS_PER_ENEMY_YOU_VE_IGNITED_IN_THE_LAST_SECONDS_UP_TO, 0.04)
                ),
                List.of("41447", "50516")
        ));
        // [Stat] 25% increased maximum [EnergyShield|Energy Shield]
        // [Stat] 5% of Maximum Life [StatConversion|Converted] to [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "44299", -2697.32f, -10733.5f,
                "enhanced_barrier", "passives_energyshield",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.25),
                        TalentNodeGen.stat(StatIds.OF_MAX_HEALTH_STAT_CONVERSION_TO_ENERGY_SHIELD, 0.05)
                ),
                List.of("38105", "49455")
        ));
        // [Stat] 20% increased [Evasion|Evasion Rating]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        // [Stat] 8% increased [Dexterity]
        list.add(TalentNodeDef.of(
                "51707", 9392.17f, 584.236f,
                "enhanced_reflexes", "passives_evade",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.2),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05),
                        TalentNodeGen.stat(StatIds.INC_DEXTERITY, 0.08)
                ),
                List.of("38728", "65091")
        ));
        // [Stat] 12% increased [Spell] Damage for each different Non-Instant [Attack] you've used in the past 8 seconds
        list.add(TalentNodeDef.of(
                "56237", -9310.65f, -5187.95f,
                "enhancing_attacks", "passives_inquistitor_increasedelementaldamageattackcastespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_FOR_EACH_DIFFERENT_NON_INSTANT_ATTACK_YOU_VE_USED_IN_THE_PAST_SECONDS, 0.12)
                ),
                List.of("61835")
        ));
        // [Stat] +10 to Maximum [Rage|Rage] while wielding an [Axe|Axe]
        list.add(TalentNodeDef.of(
                "62439", -8953.58f, 7046.06f,
                "enraged_reaver", "passives_damageaxe",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_RAGE_WHILE_WIELDING_AN_AXE, 10.0)
                ),
                List.of("52300")
        ));
        // [Stat] [Minion|Minions] have +13% to [Chaos] [Resistances|Resistance]
        // [Stat] [Minion|Minions] gain 10% of [Physical|Physical] Damage as [Chaos|Chaos] Damage
        list.add(TalentNodeDef.of(
                "30720", -1858.55f, -8528.3f,
                "entropic_incarnation", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_CHAOS_RESISTANCES, 0.13),
                        TalentNodeGen.stat(StatIds.MINIONS_GAIN_OF_PHYSICAL_DAMAGE_AS_CHAOS_DAMAGE, 0.1)
                ),
                List.of("54036")
        ));
        // [Stat] 30% increased [Presence|Presence] Area of Effect
        // [Stat] [Aura] Skills have 6% increased [BuffMagnitude|Magnitudes]
        list.add(TalentNodeDef.of(
                "65243", -6473.25f, -450.303f,
                "enveloping_presence", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.3),
                        TalentNodeGen.stat(StatIds.AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE, 0.06)
                ),
                List.of("21861", "61836")
        ));
        // [Stat] 30% increased [Attack] Damage if you've Cast a [Spell] [Recently]
        // [Stat] 10% increased Cast Speed if you've Attacked [Recently]
        list.add(TalentNodeDef.of(
                "61404", -8468.97f, -2457.13f,
                "equilibrium", "passives_inquistitor_increasedelementaldamageattackcastespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_IF_YOU_VE_CAST_A_SPELL_RECENTLY, 0.3),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_IF_YOU_VE_ATTACKED_RECENTLY, 0.1)
                ),
                List.of("61429")
        ));
        // [Stat] [ArmourBreak|Break] 50% of [Armour] on [Pinned|Pinning] an Enemy
        list.add(TalentNodeDef.of(
                "52684", -1845.12f, 5209.16f,
                "eroding_chains", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_OF_ARMOR_ON_PINNING_AN_ENEMY, 0.5)
                ),
                List.of("23993", "8115")
        ));
        // [Stat] 16% increased Cast Speed if you've dealt a [Critical|Critical Hit] [Recently|Recently]
        // [Stat] 10% reduced [Critical|Critical Hit Chance]
        list.add(TalentNodeDef.of(
                "20032", -5107.55f, -7145.78f,
                "erraticism", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_IF_YOU_VE_DEALT_A_CRITICAL_HIT_RECENTLY, 0.16),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, -0.1)
                ),
                List.of("56762")
        ));
        // [Stat] 10% increased Damage for each [Hazard] triggered [Recently], up to 50%
        list.add(TalentNodeDef.of(
                "42032", 10299.3f, 4740.9f,
                "escalating_mayhem", "passives_trap",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_FOR_EACH_HAZARD_TRIGGERED_RECENTLY_UP_TO, 0.1)
                ),
                List.of("17523")
        ));
        // [Stat] 10% increased [Poison] Duration for each [Poison] you have inflicted Recently, up to a maximum of 100%
        list.add(TalentNodeDef.of(
                "38628", 11197.0f, -1557.81f,
                "escalating_toxins", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POISON_DURATION_FOR_EACH_POISON_YOU_HAVE_INFLICTED_RECENTLY_UP_TO_A_MAX_OF, 0.1)
                ),
                List.of("53595")
        ));
        // [Stat] 25% increased [Warcry|Warcry] Speed
        // [Stat] 20% increased Damage for each different [Warcry|Warcry] you've used [Recently|Recently]
        list.add(TalentNodeDef.of(
                "9187", -5245.72f, 7506.52f,
                "escalation", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_SPEED, 0.25),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_FOR_EACH_DIFFERENT_WARCRY_YOU_VE_USED_RECENTLY, 0.2)
                ),
                List.of("37226")
        ));
        // [Stat] 100% increased [Evasion|Evasion Rating] if you have been [HitDamage|Hit] [Recently|Recently]
        // [Stat] 30% reduced [Evasion] Rating if you haven't been [HitDamage|Hit] [Recently]
        list.add(TalentNodeDef.of(
                "5227", 3893.31f, 6051.3f,
                "escape_strategy", "passives_evade",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION_IF_YOU_HAVE_BEEN_HIT_DAMAGE_RECENTLY, 1.0),
                        TalentNodeGen.stat(StatIds.INC_EVASION_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY, -0.3)
                ),
                List.of("50342")
        ));
        // [Stat] 3% increased Movement Speed
        // [Stat] 30% increased [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "17854", 6365.75f, 3186.83f,
                "escape_velocity", "passives_evade",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.03),
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.3)
                ),
                List.of("55275", "62464")
        ));
        // [Stat] 12% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        // [Stat] +12 to [Intelligence]
        list.add(TalentNodeDef.of(
                "42077", -1279.86f, -4132.44f,
                "essence_infusion", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.12),
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 12.0)
                ),
                List.of("49235", "56564")
        ));
        // [Stat] [Gain] 5% of Damage as Extra [Cold] Damage
        // [Stat] 20% increased [Freeze|Freeze] Buildup
        list.add(TalentNodeDef.of(
                "32683", 3389.56f, -908.495f,
                "essence_of_the_mountain", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.2)
                ),
                List.of("40068", "53149")
        ));
        // [Stat] [Gain] 5% of Damage as Extra [Lightning] Damage
        // [Stat] 30% increased chance to [Shock]
        list.add(TalentNodeDef.of(
                "36479", 2480.75f, -2481.04f,
                "essence_of_the_storm", "passives_stormweaver",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.3)
                ),
                List.of("12925", "36778")
        ));
        // [Stat] 25% reduced Mana Regeneration Rate while stationary
        // [Stat] 50% increased Mana Regeneration Rate while moving
        // [Stat] 5% reduced Movement Speed Penalty from using Skills while moving
        list.add(TalentNodeDef.of(
                "16256", 3212.04f, -9806.75f,
                "ether_flow", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE_WHILE_STATIONARY, -0.25),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE_WHILE_MOVING, 0.5),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING, -0.05)
                ),
                List.of("10314", "61923")
        ));
        // [Stat] 53% increased [Chaos] Damage
        // [Stat] [LifeLoss|Lose] 3% of maximum Life and [EnergyShield|Energy Shield] when you use a Chaos Skill
        list.add(TalentNodeDef.of(
                "52191", 6536.96f, -5291.42f,
                "event_horizon", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.53),
                        TalentNodeGen.stat(StatIds.HEALTH_LOSS_OF_MAX_HEALTH_AND_ENERGY_SHIELD_WHEN_YOU_USE_A_CHAOS_SKILL, -0.03)
                ),
                List.of("20782", "57724")
        ));
        // [Stat] 30% increased Skill Effect Duration
        list.add(TalentNodeDef.of(
                "49929", -9932.89f, -7952.29f,
                "everlasting_bloom", "passives_reducedskilleffectdurationnode",
                List.of(),
                List.of("31554")
        ));
        // [Stat] Skills have a 15% chance to not consume [Glory]
        list.add(TalentNodeDef.of(
                "13524", -3586.52f, 7933.74f,
                "everlasting_glory", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.SKILLS_HAVE_A_CHANCE_TO_NOT_CONSUME_GLORY, 0.15)
                ),
                List.of("4921", "54923")
        ));
        // [Stat] Skills have 10% chance to not remove [ElementalInfusion|Elemental Infusions] but still count as consuming them
        list.add(TalentNodeDef.of(
                "24087", -1165.53f, -7676.31f,
                "everlasting_infusions", "passives_instillationsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.SKILLS_HAVE_CHANCE_TO_NOT_ADD_ELEMENTAL_INFUSIONS_BUT_STILL_COUNT_AS_CONSUMING_THEM, 0.1)
                ),
                List.of("33639")
        ));
        // [Stat] 25% increased [Critical|Critical Hit] Chance if you've [Trigger|Triggered] a Skill [Recently]
        // [Stat] [Meta] Skills gain 25% increased [Energy] if you've dealt a [Critical] [HitDamage|Hit] [Recently]
        list.add(TalentNodeDef.of(
                "41753", 10039.3f, -5356.19f,
                "evocational_practitioner", "passives_ashfrostandstorm",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_IF_YOU_VE_TRIGGERED_A_SKILL_RECENTLY, 0.25),
                        TalentNodeGen.stat(StatIds.META_SKILLS_GAIN_INC_ENERGY_IF_YOU_VE_DEALT_A_CRITICAL_HIT_DAMAGE_RECENTLY, 0.25)
                ),
                List.of("56334")
        ));
        // [Stat] [Archon] recovery period expires 30% slower
        // [Stat] [Archon] Buffs also grant 50% increased [CriticalDamageBonus|Critical Damage Bonus]
        // [Stat] [Archon] Buffs also grant 30% increased [Critical|Critical Hit Chance]
        list.add(TalentNodeDef.of(
                "11428", -7447.27f, -5424.03f,
                "exhaust_all_power", "passives_archongenericnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.ARCHON_RECOVERY_PERIOD_EXPIRES_SLOWER, -0.3),
                        TalentNodeGen.stat(StatIds.ARCHON_BUFFS_ALSO_GRANT_INC_CRITICAL_DAMAGE_BONUS, 0.5),
                        TalentNodeGen.stat(StatIds.ARCHON_BUFFS_ALSO_GRANT_INC_CRITICAL_HIT_CHANCE, 0.3)
                ),
                List.of("29197")
        ));
        // [Stat] 20% increased Minion Duration
        // [Stat] [TemporaryMinion|Temporary Minion] Skills have +2 to [Limit] of [Minion|Minions] summoned
        list.add(TalentNodeDef.of(
                "47420", -7205.17f, -8949.18f,
                "expendable_army", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MINION_DURATION, 0.2),
                        TalentNodeGen.stat(StatIds.TEMPORARY_MINION_SKILLS_HAVE_TO_LIMIT_OF_MINIONS_SUMMONED, 2.0)
                ),
                List.of("11048", "52676", "64653", "752")
        ));
        // [Stat] 25% increased Damage with [HitDamage|Hits] against Enemies affected by [ElementalAilments|Elemental Ailments]
        // [Stat] 15% increased Duration of [Ignite], [Shock] and [Chill] on Enemies
        list.add(TalentNodeDef.of(
                "39050", 6157.17f, 6361.42f,
                "exploit", "passives_elementaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_ELEMENTAL_AILMENTS, 0.25),
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_IGNITE_SHOCK_AND_CHILL_ON_ENEMIES, 0.15)
                ),
                List.of("6660")
        ));
        // [Stat] 24% increased Damage with [HitDamage|Hits] against Enemies affected by [ElementalAilments|Elemental Ailments]
        // [Stat] 30% increased chance to inflict [Ailments] against [Rarity|Rare or Unique] Enemies
        list.add(TalentNodeDef.of(
                "48581", -2076.24f, -5540.25f,
                "exploit_the_elements", "passives_elementaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_ELEMENTAL_AILMENTS, 0.24),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_INFLICT_AILMENTS_AGAINST_RARE_OR_UNIQUE_ENEMIES, 0.3)
                ),
                List.of("24338", "33242")
        ));
        // [Stat] [Empowered|Empowered Attacks] deal 20% increased Damage
        // [Stat] Enemies you kill with [Empowered] [Attack|Attacks] have a 10% chance to Explode, dealing a tenth of their maximum Life as [Fire] Damage
        list.add(TalentNodeDef.of(
                "36333", -8156.53f, -3941.85f,
                "explosive_empowerment", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.EMPOWERED_ATTACKS_DEAL_INC_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.ENEMIES_YOU_KILL_WITH_EMPOWERED_ATTACKS_HAVE_A_CHANCE_TO_EXPLODE_DEALING_A_TENTH_OF_THEIR_MAX_HEALTH_AS_FIRE_DAMAGE, 0.1)
                ),
                List.of("42059", "62360")
        ));
        // [Stat] 15% increased Area of Effect
        // [Stat] [Burning] Enemies you kill have a 5% chance to Explode, dealing a
        //        tenth of their maximum Life as [Fire] Damage
        list.add(TalentNodeDef.of(
                "21206", -2461.19f, -6941.15f,
                "explosive_impact", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT, 0.15),
                        TalentNodeGen.stat(StatIds.BURNING_ENEMIES_YOU_KILL_HAVE_A_CHANCE_TO_EXPLODE_DEALING_A_TENTH_OF_THEIR_MAX_HEALTH_AS_FIRE_DAMAGE, 0.05)
                ),
                List.of("45899")
        ));
        // [Stat] Damage [Penetration|Penetrates] 18% [Resistances|Cold Resistance]
        // [Stat] 20% increased chance to inflict [Ailments|Ailments] against Enemies with [Exposure]
        list.add(TalentNodeDef.of(
                "55835", 9431.91f, -3521.12f,
                "exposed_to_the_cosmos", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.18),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_INFLICT_AILMENTS_AGAINST_ENEMIES_WITH_EXPOSURE, 0.2)
                ),
                List.of("20909", "52537")
        ));
        // [Stat] Damage [Penetration|Penetrates] 18% [Resistances|Fire Resistance]
        // [Stat] 15% increased Duration of [Ailments|Ailments] against Enemies with [Exposure]
        list.add(TalentNodeDef.of(
                "10423", 11025.4f, -2697.56f,
                "exposed_to_the_inferno", "passives_firedamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_FIRE_RESISTANCE, 0.18),
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_AILMENTS_AGAINST_ENEMIES_WITH_EXPOSURE, 0.15)
                ),
                List.of("37905", "43867")
        ));
        // [Stat] Damage [Penetration|Penetrates] 18% [Resistances|Lightning Resistance]
        // [Stat] 15% increased [Critical|Critical Hit Chance] against enemies with [Exposure]
        list.add(TalentNodeDef.of(
                "40990", 8993.62f, -3178.16f,
                "exposed_to_the_storm", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.18),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_WITH_EXPOSURE, 0.15)
                ),
                List.of("18815")
        ));
        // [Stat] 15% increased chance to inflict [Ailments]
        // [Stat] [HitDamage|Hits] [ArmourBreak|Break] 30% increased [Armour|Armour] on targets with [Ailments]
        list.add(TalentNodeDef.of(
                "43383", -621.677f, 3698.92f,
                "exposed_wounds", "passives_physicaldamagechaosnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_INFLICT_AILMENTS, 0.15),
                        TalentNodeGen.stat(StatIds.HIT_DAMAGE_ARMOR_BREAK_INC_ARMOR_ON_TARGETS_WITH_AILMENTS, 0.3)
                ),
                List.of("37450", "43201")
        ));
        // [Stat] Remove [Ignite] when you [Warcry]
        list.add(TalentNodeDef.of(
                "56112", -8323.54f, -3652.59f,
                "extinguishing_exhalation", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.ADD_IGNITE_WHEN_YOU_WARCRY, 1.0)
                ),
                List.of("42059", "53804")
        ));
        // [Stat] 4% increased [Attack] Speed
        // [Stat] 1% increased [Attack] Speed per 400 [Accuracy|Accuracy] Rating, up to 20%
        list.add(TalentNodeDef.of(
                "60034", 7762.38f, 6434.67f,
                "falcon_dive", "passives_accuracydex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.04),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_PER_ACCURACY_UP_TO, 0.01)
                ),
                List.of("15207", "22208")
        ));
        // [Stat] 1% increased [Attack] Speed per 25 [Dexterity]
        list.add(TalentNodeDef.of(
                "31172", 6860.6f, 1366.16f,
                "falcon_technique", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_PER_DEXTERITY, 0.01)
                ),
                List.of("35671")
        ));
        // [Stat] 25% reduced [Ignite|Ignite] Duration on you
        // [Stat] 40% increased [AilmentThreshold|Elemental Ailment Threshold]
        list.add(TalentNodeDef.of(
                "60464", 6713.93f, 5993.21f,
                "fan_the_flames", "passives_spellsuppresionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_DURATION_ON_YOU, -0.25),
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_AILMENT_THRESHOLD, 0.4)
                ),
                List.of("38678", "58971")
        ));
        // [Stat] 30% reduced penalty to [Accuracy] Rating at range
        list.add(TalentNodeDef.of(
                "35477", 7675.0f, 538.473f,
                "far_sighted", "passives_accuracydex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PENALTY_TO_ACCURACY_AT_RANGE, -0.3)
                ),
                List.of("10277")
        ));
        // [Stat] [DamagingAilments|Damaging Ailments] deal damage 12% faster
        list.add(TalentNodeDef.of(
                "55", 4219.73f, 5087.65f,
                "fast_acting_toxins", "passives_physicaldamagechaosnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGING_AILMENTS_DEAL_DAMAGE_FASTER, 0.12)
                ),
                List.of("13030")
        ));
        // [Stat] 40% increased Damage while Leeching Life
        list.add(TalentNodeDef.of(
                "8827", -4749.89f, 10821.6f,
                "fast_metabolism", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_LEECHING_HEALTH, 0.4)
                ),
                List.of("16691", "2888")
        ));
        // [Stat] 20% increased [Reservation] [Efficiency] of [Herald] Skills
        list.add(TalentNodeDef.of(
                "3921", -3297.22f, 11227.2f,
                "fate_finding", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_HERALD_SKILLS, 0.2)
                ),
                List.of("31898", "38398")
        ));
        // [Stat] 30% increased [Curse] Duration
        // [Stat] Targets [Curse|Cursed] by you have 50% reduced Life Regeneration Rate
        // [Stat] Enemies you [Curse] cannot Recharge [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "59214", 6485.94f, -6987.61f,
                "fated_end", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_DURATION, 0.3),
                        TalentNodeGen.stat(StatIds.TARGETS_CURSED_BY_YOU_HAVE_INC_HEALTH_REGENERATION_RATE, -0.5),
                        TalentNodeGen.stat(StatIds.ENEMIES_YOU_CURSE_CANNOT_RECHARGE_ENERGY_SHIELD)
                ),
                List.of("26268")
        ));
        // [Stat] 30% increased [Block|Block] chance while [Surrounded]
        // [Stat] 10% increased [Deflect|Deflection Rating] while [Surrounded]
        // [Stat] 40% increased [AilmentThreshold|Ailment] and [StunThreshold|Stun] Threshold while [Surrounded]
        list.add(TalentNodeDef.of(
                "19546", -4430.72f, 9163.92f,
                "favourable_odds", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE_WHILE_SURROUNDED, 0.3),
                        TalentNodeGen.stat(StatIds.INC_DEFLECTION_RATING_WHILE_SURROUNDED, 0.1),
                        TalentNodeGen.stat(StatIds.INC_AILMENT_THRESHOLD_AND_STUN_THRESHOLD_WHILE_SURROUNDED, 0.4)
                ),
                List.of("55617")
        ));
        // [Stat] Enemies are [Intimidate|Intimidated] for 4 seconds when you [Immobilised|Immobilise] them
        list.add(TalentNodeDef.of(
                "22532", 1195.42f, 4000.27f,
                "fearful_paralysis", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_ARE_INTIMIDATED_FOR_SECONDS_WHEN_YOU_IMMOBILISED_THEM, 4.0)
                ),
                List.of("59289", "64550")
        ));
        // [Stat] Increases and Reductions to [Projectile|Projectile] Speed also apply to Damage with [Bow|Bows]
        list.add(TalentNodeDef.of(
                "60764", 11251.2f, 4220.02f,
                "feathered_fletching", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AND_REDUCTIONS_TO_PROJECTILE_SPEED_ALSO_APPLY_TO_DAMAGE_WITH_BOWS, 1.0)
                ),
                List.of("328", "34612")
        ));
        // [Stat] 20% increased [Armour] and [Evasion] Rating
        // [Stat] 20% increased [StunThreshold|Stun Threshold]
        list.add(TalentNodeDef.of(
                "44836", 1.945f, 2993.7f,
                "feel_no_pain", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.2),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.2)
                ),
                List.of("38779", "47150")
        ));
        // [Stat] 25% reduced [Shock|Shock] duration on you
        // [Stat] 40% increased [AilmentThreshold|Elemental Ailment Threshold]
        list.add(TalentNodeDef.of(
                "9968", 6932.69f, 5176.77f,
                "feel_the_earth", "passives_spellsuppresionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SHOCK_DURATION_ON_YOU, -0.25),
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_AILMENT_THRESHOLD, 0.4)
                ),
                List.of("30463", "38678")
        ));
        // [Stat] +2 to Maximum [Charges|Frenzy Charges]
        list.add(TalentNodeDef.of(
                "21537", 10863.8f, 1506.1f,
                "fervour", "passives_chargedex",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_FRENZY_CHARGES, 2.0)
                ),
                List.of("57970")
        ));
        // [Stat] 20% increased Cast Speed when on [LowLife|Low Life]
        // [Stat] 10% reduced Cast Speed when on Full Life
        list.add(TalentNodeDef.of(
                "2999", -6352.07f, -10150.4f,
                "final_barrage", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_WHEN_ON_LOW_HEALTH, 0.2),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_WHEN_ON_FULL_HEALTH, -0.1)
                ),
                List.of("28774")
        ));
        // [Stat] 120% increased Damage with [HitDamage|Hits] against Enemies that are on [LowLife|Low Life]
        // [Stat] 5% increased Damage taken while on [LowLife|Low Life]
        list.add(TalentNodeDef.of(
                "51867", -5443.28f, 6845.42f,
                "finality", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, 1.2),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_TAKEN_WHILE_ON_LOW_HEALTH, 0.05)
                ),
                List.of("17924")
        ));
        // [Stat] 10% increased [Accuracy] Rating
        // [Stat] Gain Accuracy Rating equal to your [Intelligence]
        list.add(TalentNodeDef.of(
                "38969", 4944.98f, -2386.46f,
                "finesse", "passives_accuracydex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.1),
                        TalentNodeGen.stat(StatIds.GAIN_ACCURACY_EQUAL_TO_YOUR_INTELLIGENCE, 1.0)
                ),
                List.of("13367", "50588", "6010")
        ));
        // [Stat] 40% increased [CullingStrike|Culling Strike] Threshold against [Immobilised] Enemies
        list.add(TalentNodeDef.of(
                "29899", 4609.95f, 8953.29f,
                "finish_them", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CULLING_STRIKE_THRESHOLD_AGAINST_IMMOBILISED_ENEMIES, 0.4)
                ),
                List.of("19001", "21792")
        ));
        // [Stat] 60% increased Damage with [HitDamage|Hits] against Enemies that are on [LowLife|Low Life]
        // [Stat] 30% increased [Stun|Stun] Buildup against Enemies that are on [LowLife|Low Life]
        list.add(TalentNodeDef.of(
                "45013", 1786.02f, 6006.98f,
                "finishing_blows", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, 0.6),
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, 0.3)
                ),
                List.of("36596", "8440")
        ));
        // [Stat] 80% increased [Flammability] [BuffMagnitude|Magnitude]
        // [Stat] Enemies [Ignite|Ignited] by you have -5% to Fire [Resistances|Resistance]
        list.add(TalentNodeDef.of(
                "54911", -2586.44f, -7108.15f,
                "firestarter", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLAMMABILITY_BUFF_MAGNITUDE, 0.8),
                        TalentNodeGen.stat(StatIds.ENEMIES_IGNITED_BY_YOU_HAVE_TO_FIRE_RESISTANCES, -0.05)
                ),
                List.of("39716", "44092")
        ));
        // [Stat] 40% increased [Critical|Critical Hit Chance] against Enemies that are on Full Life
        // [Stat] Cannot be [Blind|Blinded] while on Full Life
        // [Stat] 80% increased Damage with [HitDamage|Hits] against Enemies that are on Full Life
        list.add(TalentNodeDef.of(
                "29527", 8660.17f, -594.435f,
                "first_approach", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH, 0.4),
                        TalentNodeGen.stat(StatIds.CANNOT_BE_BLINDED_WHILE_ON_FULL_HEALTH, 1.0),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH, 0.8)
                ),
                List.of("60560", "61800")
        ));
        // [Stat] 20% increased [Evasion|Evasion Rating]
        // [Stat] 20% increased maximum [EnergyShield|Energy Shield]
        // [Stat] +5% to [Resistances|Cold Resistance]
        // [Stat] +5% to [Resistances|Lightning Resistance]
        list.add(TalentNodeDef.of(
                "49356", 9546.86f, -2869.28f,
                "first_principle_of_the_hollow", "passives_evasionandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.2),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.2),
                        TalentNodeGen.stat(StatIds.TO_COLD_RESISTANCE, 0.05),
                        TalentNodeGen.stat(StatIds.TO_LIGHTNING_RESISTANCE, 0.05)
                ),
                List.of("60239")
        ));
        // [Stat] 30% chance to [Poison] on [HitDamage|Hit] against Enemies that are not Poisoned
        // [Stat] 80% increased [BuffMagnitude|Magnitude] of [Poison] you inflict on targets that are not Poisoned
        list.add(TalentNodeDef.of(
                "6088", -4735.65f, -5595.5f,
                "first_sting", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_POISON_ON_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_NOT_POISONED, 0.3),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT_ON_TARGETS_THAT_ARE_NOT_POISONED, 0.8)
                ),
                List.of("34990", "46069")
        ));
        // [Stat] +5% to [Resistances|Fire Resistance]
        // [Stat] +5% to [Resistances|Chaos Resistance]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        // [Stat] 10% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "59657", 8968.35f, -2535.28f,
                "first_teachings_of_the_keeper", "passives_energyshieldrechargedeflect",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_FIRE_RESISTANCE, 0.05),
                        TalentNodeGen.stat(StatIds.TO_CHAOS_RESISTANCE, 0.05),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.1)
                ),
                List.of("26556", "42078")
        ));
        // [Stat] 20% increased [Fire] Damage
        // [Stat] 15% increased [Ignite|Ignite] [BuffMagnitude|Magnitude]
        // [Stat] 30% reduced [BuffMagnitude|Magnitude] of [Ignite|Ignite] on you
        list.add(TalentNodeDef.of(
                "52669", -10699.2f, 174.465f,
                "flamekeeper", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FIRE_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_IGNITE_BUFF_MAGNITUDE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU, -0.3)
                ),
                List.of("55188")
        ));
        // [Stat] 40% reduced [BuffMagnitude|Magnitude] of [Ignite|Ignite] on you
        // [Stat] [Gain] 15% of Damage as Extra [Fire] Damage while on [IgnitedGround|Ignited Ground]
        list.add(TalentNodeDef.of(
                "62963", -1981.48f, -9989.29f,
                "flamewalker", "passives_flameborn",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU, -0.4),
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_WHILE_ON_IGNITED_GROUND, 0.15)
                ),
                List.of("25763", "27186")
        ));
        // [Stat] Gain 2% of Damage as Extra [Fire] Damage per [Charges|Endurance Charge] consumed [Recently]
        list.add(TalentNodeDef.of(
                "43584", -2568.32f, 3948.16f,
                "flare", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_PER_ENDURANCE_CHARGE_CONSUMED_RECENTLY, 0.02)
                ),
                List.of("15494")
        ));
        // [Stat] 30% increased chance to [Shock]
        // [Stat] Damage [Penetration|Penetrates] 15% [Resistances|Lightning Resistance]
        list.add(TalentNodeDef.of(
                "12337", 2880.86f, 9533.6f,
                "flash_storm", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.3),
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.15)
                ),
                List.of("5295", "53965")
        ));
        // [Stat] 12% increased [Block] chance
        // [Stat] 20% increased [ParriedDebuff|Parried Debuff] Duration
        list.add(TalentNodeDef.of(
                "64851", 4563.02f, 2137.46f,
                "flashy_parrying", "passives_bucklersnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.12),
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_DURATION, 0.2)
                ),
                List.of("45693", "49545")
        ));
        // [Stat] 30% increased Mana Regeneration Rate while [Shock|Shocked]
        // [Stat] +500 to [Armour] while Frozen
        // [Stat] 21% increased [StunThreshold|Stun Threshold]
        // [Stat] 21% increased [AilmentThreshold|Elemental Ailment Threshold]
        // [Stat] 30% increased Life Regeneration rate while [Ignite|Ignited]
        list.add(TalentNodeDef.of(
                "8723", -8668.68f, -9843.12f,
                "flesh_withstands", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE_WHILE_SHOCKED, 0.3),
                        TalentNodeGen.stat(StatIds.TO_ARMOR_WHILE_FROZEN, 500.0),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.21),
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_AILMENT_THRESHOLD, 0.21),
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_IGNITED, 0.3)
                ),
                List.of("9554", "3681", "21374")
        ));
        // [Stat] [Minion|Minions] gain 15% of their maximum Life as Extra maximum [EnergyShield|Energy Shield]
        // [Stat] 4% of Maximum Life [StatConversion|Converted] to [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "21164", -9574.83f, -2582.41f,
                "fleshcrafting", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_GAIN_OF_THEIR_MAX_HEALTH_AS_EXTRA_MAX_ENERGY_SHIELD, 0.15),
                        TalentNodeGen.stat(StatIds.OF_MAX_HEALTH_STAT_CONVERSION_TO_ENERGY_SHIELD, 0.04)
                ),
                List.of("18245", "41497")
        ));
        // [Stat] Recover 50% of maximum Life when you [HeavyStun|Heavy Stun] a [Rarity|Rare or Unique] Enemy
        list.add(TalentNodeDef.of(
                "4985", -7494.69f, 1560.92f,
                "flip_the_script", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_WHEN_YOU_HEAVY_STUN_A_RARE_OR_UNIQUE_ENEMY, 0.5)
                ),
                List.of("7204")
        ));
        // [Stat] 8% increased [Attack] and Cast Speed
        // [Stat] +5 to [Dexterity|Dexterity] and [Intelligence|Intelligence]
        list.add(TalentNodeDef.of(
                "49220", 2243.98f, -1446.94f,
                "flow_like_water", "passives_harrier",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED, 0.08),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY_AND_INTELLIGENCE, 5.0)
                ),
                List.of("14725", "21336", "33866", "36778", "4157", "44223", "53960", "58182")
        ));
        // [Stat] [Debuff|Debuffs] on you expire 10% faster
        // [Stat] 20% increased speed of [Recoup] Effects
        list.add(TalentNodeDef.of(
                "59438", -2280.15f, -5538.98f,
                "flow_of_life", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_ON_YOU_EXPIRE_FASTER, 0.1),
                        TalentNodeGen.stat(StatIds.INC_SPEED_OF_RECOUP_EFFECTS, 0.2)
                ),
                List.of("15801", "18101")
        ));
        // [Stat] [Buff|Buffs] on you expire 10% slower
        // [Stat] 20% increased speed of [Recoup] Effects
        list.add(TalentNodeDef.of(
                "32128", -2720.45f, -5537.28f,
                "flow_of_time", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.BUFFS_ON_YOU_EXPIRE_SLOWER, -0.1),
                        TalentNodeGen.stat(StatIds.INC_SPEED_OF_RECOUP_EFFECTS, 0.2)
                ),
                List.of("15801", "18101")
        ));
        // [Stat] 5% increased [SkillSpeed|Skill Speed]
        // [Stat] 15% increased Mana Regeneration Rate
        list.add(TalentNodeDef.of(
                "34233", 2671.29f, -2143.98f,
                "flow_state", "passives_harrier",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.05),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.15)
                ),
                List.of("14725", "32545")
        ));
        // [Stat] 20% increased [Cold] Damage
        // [Stat] 10% increased Cast Speed while Chilled
        // [Stat] 5% reduced Movement Speed Penalty from using Cold Skills while moving
        list.add(TalentNodeDef.of(
                "33852", -5013.88f, -8978.47f,
                "flurry", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_WHILE_CHILLED, 0.1),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_COLD_SKILLS_WHILE_MOVING, -0.05)
                ),
                List.of("4833")
        ));
        // [Stat] [Channelling] Skills deal 25% increased Damage
        // [Stat] 50% increased [StunThreshold|Stun Threshold] while [Channelling]
        list.add(TalentNodeDef.of(
                "33730", -4387.1f, -3135.22f,
                "focused_channel", "passives_channellingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANNELLING_SKILLS_DEAL_INC_DAMAGE, 0.25),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_WHILE_CHANNELLING, 0.5)
                ),
                List.of("10260", "49172")
        ));
        // [Stat] 75% increased [Melee] Damage with [Spear|Spears] while [Surrounded]
        // [Stat] 40% increased Projectile Damage with [Spear|Spears] while there are no Enemies within 3m
        list.add(TalentNodeDef.of(
                "9227", 8989.17f, 6990.15f,
                "focused_thrust", "passives_spearsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE_WITH_SPEARS_WHILE_SURROUNDED, 0.75),
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE_WITH_SPEARS_WHILE_THERE_ARE_NO_ENEMIES_WITHIN_XM, 0.4)
                ),
                List.of("35223", "36677")
        ));
        // [Stat] 25% increased [CriticalDamageBonus|Critical Damage Bonus]
        // [Stat] +10 to [Intelligence]
        list.add(TalentNodeDef.of(
                "20677", 5061.13f, -4971.48f,
                "for_the_jugular", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.25),
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 10.0)
                ),
                List.of("4346", "535", "9782")
        ));
        // [Stat] 5% reduced maximum Life
        // [Stat] -10% to all [ElementalDamage|Elemental] [Resistances]
        // [Stat] [Minion|Minions] [Recoup] 30% of Damage taken as Life
        // [Stat] [Minion|Minions] [Gain] 20% of Elemental Damage as Extra Chaos Damage
        list.add(TalentNodeDef.of(
                "53910", -6753.23f, -6208.67f,
                "forbidden_path", "passives_minionchaosresistancenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_HEALTH, -0.05),
                        TalentNodeGen.stat(StatIds.TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES, -0.1),
                        TalentNodeGen.stat(StatIds.MINIONS_RECOUP_OF_DAMAGE_TAKEN_AS_HEALTH, 0.3),
                        TalentNodeGen.stat(StatIds.MINIONS_GAIN_OF_ELEMENTAL_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, 0.2)
                ),
                List.of("58368", "61113")
        ));
        // [Stat] [Attack] Damage Penetrates 15% of Enemy [ElementalDamage|Elemental] [Resistances]
        list.add(TalentNodeDef.of(
                "3985", 3534.61f, 8622.88f,
                "forces_of_nature", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACK_DAMAGE_PENETRATES_OF_ENEMY_ELEMENTAL_DAMAGE_RESISTANCES, 0.15)
                ),
                List.of("64140")
        ));
        // [Stat] 20% increased [Stun] Buildup
        // [Stat] 20% increased [Knockback] Distance
        // [Stat] 20% increased [Physical] Damage
        list.add(TalentNodeDef.of(
                "48103", 8436.64f, -1367.59f,
                "forcewave", "passives_knockback",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.2),
                        TalentNodeGen.stat(StatIds.INC_KNOCKBACK_DISTANCE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.2)
                ),
                List.of("26726")
        ));
        // [Stat] 16% reduced Skill Effect Duration
        // [Stat] 10% increased [CooldownRecovery|Cooldown Recovery Rate]
        list.add(TalentNodeDef.of(
                "55568", 3666.62f, -10961.2f,
                "forthcoming", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, -0.16),
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE, 0.1)
                ),
                List.of("44690")
        ));
        // [Stat] 100% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] from Equipped [Shield]
        list.add(TalentNodeDef.of(
                "23940", -10591.5f, -5430.09f,
                "fortified_aegis", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_FROM_EQUIPPED_SHIELD, 1.0)
                ),
                List.of("14342", "17349")
        ));
        // [Stat] 10% increased [Armour] and [Evasion] Rating per Summoned [Totem] in your [Presence]
        // [Stat] 10% increased [Attack] Damage per Summoned [Totem] in your [Presence]
        list.add(TalentNodeDef.of(
                "53607", -2630.11f, 9553.52f,
                "fortified_location", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION_PER_SUMMONED_TOTEM_IN_YOUR_PRESENCE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_PER_SUMMONED_TOTEM_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("40597")
        ));
        // [Stat] 15% increased amount of Life [LifeLeech|Leeched]
        // [Stat] 40% increased [Armour] and [Evasion] Rating while Leeching
        list.add(TalentNodeDef.of(
                "35855", 1748.39f, 3934.74f,
                "fortifying_blood", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.15),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION_WHILE_LEECHING, 0.4)
                ),
                List.of("37190", "48583")
        ));
        // [Stat] 30% reduced [Accuracy] Rating while [Surrounded]
        // [Stat] 100% increased [Attack|Attack] Damage while [Surrounded|Surrounded]
        list.add(TalentNodeDef.of(
                "59208", -4714.77f, 9327.92f,
                "frantic_fighter", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WHILE_SURROUNDED, -0.3),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_SURROUNDED, 1.0)
                ),
                List.of("23036")
        ));
        // [Stat] 20% reduced [Accuracy] Rating
        // [Stat] 18% increased Area of Effect for [Attack|Attacks]
        list.add(TalentNodeDef.of(
                "39911", 8886.63f, -4654.62f,
                "frantic_reach", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, -0.2),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.18)
                ),
                List.of("11252")
        ));
        // [Stat] Enemies Blinded by you have 50% reduced [Critical|Critical Hit] Chance
        list.add(TalentNodeDef.of(
                "28441", 6254.37f, -2079.42f,
                "frantic_swings", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_BLINDED_BY_YOU_HAVE_INC_CRITICAL_HIT_CHANCE, -0.5)
                ),
                List.of("10011", "24570")
        ));
        // [Stat] 15% increased Mana Regeneration Rate
        // [Stat] 30% increased [BuffMagnitude|Magnitude] of [Shock|Shock] you inflict
        list.add(TalentNodeDef.of(
                "32301", 7573.53f, 5472.94f,
                "frazzled", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.3)
                ),
                List.of("50277")
        ));
        // [Stat] 20% increased [Evasion|Evasion Rating]
        // [Stat] 10% reduced [Slow|Slowing] Potency of [Debuff|Debuffs] on You
        // [Stat] 5% reduced Movement Speed Penalty from using Skills while moving
        list.add(TalentNodeDef.of(
                "51606", 5943.13f, 3918.83f,
                "freedom_of_movement", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.2),
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.1),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING, -0.05)
                ),
                List.of("12120", "65207")
        ));
        // [Stat] 10% chance when you gain a [Charges|Frenzy Charge] to gain an additional [Charges|Frenzy Charge]
        // [Stat] +1 to Maximum [Charges|Frenzy Charges]
        list.add(TalentNodeDef.of(
                "40270", 5646.25f, 635.095f,
                "frenetic", "passives_chargedex",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_WHEN_YOU_GAIN_A_FRENZY_CHARGE_TO_GAIN_AN_ADDITIONAL_FRENZY_CHARGE, 0.1),
                        TalentNodeGen.stat(StatIds.TO_MAX_FRENZY_CHARGES, 1.0)
                ),
                List.of()
        ));
        // [Stat] [Minion|Minions] deal 10% increased Damage with [Command] Skills for each different type of [Persistent] [Minion] in your [Presence]
        list.add(TalentNodeDef.of(
                "64139", -11600.6f, -5570.77f,
                "friend_to_many", "passives_miniondamageblue",
                List.of(),
                List.of("22221", "43385")
        ));
        // [Stat] Apply [Debilitate] to Enemies 30 Metres in front of you while your [Shield] is raised
        list.add(TalentNodeDef.of(
                "45751", -7472.17f, 5930.06f,
                "frightening_shield", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.APPLY_DEBILITATE_TO_ENEMIES_METRES_IN_FRONT_OF_YOU_WHILE_YOUR_SHIELD_IS_RAISED, 30.0)
                ),
                List.of("6900")
        ));
        // [Stat] 40% reduced Effect of [Chill|Chill] on you
        // [Stat] [Gain] 15% of Damage as Extra [Cold] Damage while on [ChilledGround|Chilled Ground]
        list.add(TalentNodeDef.of(
                "48699", -911.045f, -9989.29f,
                "frostwalker", "passives_frostborn",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_CHILL_ON_YOU, -0.4),
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_ON_CHILLED_GROUND, 0.15)
                ),
                List.of("1091", "1700")
        ));
        // [Stat] [Gain] 8% of Damage as Extra [Cold] Damage while [Shapeshift|Shapeshifted]
        list.add(TalentNodeDef.of(
                "20289", -10439.9f, -3500.92f,
                "frozen_claw", "passives_druidshapeshiftwolfnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_SHAPESHIFTED, 0.08)
                ),
                List.of("53354", "61896")
        ));
        // [Stat] +1 to maximum [ElementalInfusion|Cold Infusions]
        list.add(TalentNodeDef.of(
                "50715", -606.772f, -9666.08f,
                "frozen_limit", "passives_instillationsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_COLD_INFUSIONS, 1.0)
                ),
                List.of("29391")
        ));
        // [Stat] 15% increased Life Regeneration rate
        // [Stat] 15% increased Mana Regeneration Rate
        // [Stat] 12% increased Cast Speed while on Full Mana
        list.add(TalentNodeDef.of(
                "37543", -3658.52f, -3153.91f,
                "full_recovery", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_WHILE_ON_FULL_MANA, 0.12)
                ),
                List.of("38420")
        ));
        // [Stat] 25% increased Damage with [Crossbow|Crossbows] for each type of [Ammunition|Ammunition] fired in the past 10 seconds
        list.add(TalentNodeDef.of(
                "33887", 2137.56f, 11024.1f,
                "full_salvo", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_CROSSBOWS_FOR_EACH_TYPE_OF_AMMUNITION_FIRED_IN_THE_PAST_SECONDS, 0.25)
                ),
                List.of("48137")
        ));
        // [Stat] 80% increased [Flammability] [BuffMagnitude|Magnitude]
        // [Stat] 40% increased Damage with [HitDamage|Hits] against [Ignite|Ignited] Enemies
        list.add(TalentNodeDef.of(
                "24630", -10708.8f, 2443.13f,
                "fulmination", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLAMMABILITY_BUFF_MAGNITUDE, 0.8),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_IGNITED_ENEMIES, 0.4)
                ),
                List.of("11433", "15522")
        ));
        // [Stat] 5% increased Maximum Life if you have at least 10 Red [SupportGem|Support Gems] Socketed
        // [Stat] 5% increased Maximum Mana if you have at least 10 Blue [SupportGem|Support Gems] Socketed
        // [Stat] 5% increased Movement Speed if you have at least 10 Green [SupportGem|Support Gems] Socketed
        list.add(TalentNodeDef.of(
                "32976", -766.945f, 11309.6f,
                "gem_enthusiast", "passives_gemling_gemlingnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_HEALTH_IF_YOU_HAVE_AT_LEAST_RED_SUPPORT_GEMS_SOCKETED, 0.05),
                        TalentNodeGen.stat(StatIds.INC_MAX_MANA_IF_YOU_HAVE_AT_LEAST_BLUE_SUPPORT_GEMS_SOCKETED, 0.05),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_IF_YOU_HAVE_AT_LEAST_GREEN_SUPPORT_GEMS_SOCKETED, 0.05)
                ),
                List.of("14428", "34202", "38776")
        ));
        // [Stat] 40% increased chance to [Shock]
        // [Stat] 5% increased [Attack] and Cast Speed with [Lightning|Lightning] Skills
        list.add(TalentNodeDef.of(
                "27875", 5979.38f, -1521.97f,
                "general_electric", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.4),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED_WITH_LIGHTNING_SKILLS, 0.05)
                ),
                List.of("23905", "32123", "55463")
        ));
        // [Stat] [Gain] 8% of [Evasion] Rating as extra [Armour]
        list.add(TalentNodeDef.of(
                "17150", -1077.89f, 11762.0f,
                "general_s_bindings", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_EVASION_AS_EXTRA_ARMOR, 0.08)
                ),
                List.of("53647", "7390")
        ));
        // [Stat] 25% increased Damage with [HitDamage|Hits] against [Rarity|Rare and Unique] Enemies
        // [Stat] 20% increased [Accuracy] Rating against [Rarity|Rare or Unique] Enemies
        // [Stat] 20% increased chance to inflict [Ailments] against [Rarity|Rare or Unique] Enemies
        list.add(TalentNodeDef.of(
                "9020", 2689.83f, 11697.9f,
                "giantslayer", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_RARE_AND_UNIQUE_ENEMIES, 0.25),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_AGAINST_RARE_OR_UNIQUE_ENEMIES, 0.2),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_INFLICT_AILMENTS_AGAINST_RARE_OR_UNIQUE_ENEMIES, 0.2)
                ),
                List.of("37767")
        ));
        // [Stat] 2% chance that if you would gain [Charges|Power Charges], you instead gain up to
        //        your maximum number of [Charges|Power Charges]
        // [Stat] +1 to Maximum [Charges|Power Charges]
        list.add(TalentNodeDef.of(
                "54289", -7915.44f, -3108.31f,
                "gift_of_the_plains", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_POWER_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_POWER_CHARGES, 0.02),
                        TalentNodeGen.stat(StatIds.TO_MAX_POWER_CHARGES, 1.0)
                ),
                List.of("43486")
        ));
        // [Stat] Your Minions are [Gigantic]
        // [Stat] 25% reduced [Reservation] [Efficiency] of [Minion] Skills
        list.add(TalentNodeDef.of(
                "46365", -2362.28f, -9378.04f,
                "gigantic_following", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.YOUR_MINIONS_ARE_GIGANTIC),
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_MINION_SKILLS, -0.25)
                ),
                List.of("229")
        ));
        // [Stat] [Allies] in your [Presence] Regenerate 2% of your Maximum Life per second
        // [Stat] 30% increased Life Cost of Skills
        list.add(TalentNodeDef.of(
                "13468", -4276.59f, -3844.77f,
                "give_up_your_essence", "passives_manastr",
                List.of(),
                List.of("57596")
        ));
        // [Stat] Damage [Penetration|Penetrates] 18% [Resistances|Cold Resistance]
        // [Stat] [Gain] 6% of [ElementalDamage|Elemental Damage] as Extra [Cold] Damage
        list.add(TalentNodeDef.of(
                "41972", 488.435f, -11452.5f,
                "glaciation", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.18),
                        TalentNodeGen.stat(StatIds.GAIN_OF_ELEMENTAL_DAMAGE_AS_EXTRA_COLD_DAMAGE, 0.06)
                ),
                List.of("56649", "60515")
        ));
        // [Stat] 10% increased [Deflect|Deflection Rating]
        list.add(TalentNodeDef.of(
                "56488", 5265.67f, 5191.09f,
                "glancing_deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DEFLECTION_RATING, 0.1)
                ),
                List.of("28038", "48889")
        ));
        // [Stat] 3% of Damage Taken [Recoup|Recouped] as Life, Mana and [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "23939", -2697.32f, -9995.46f,
                "glazed_flesh", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH_MANA_AND_ENERGY_SHIELD, 0.03)
                ),
                List.of("51797", "59425")
        ));
        // [Stat] Skills gain 1 [Glory] every 2 seconds for each [Rarity|Rare or Unique] monster in your [Presence]
        list.add(TalentNodeDef.of(
                "63031", -8619.62f, 7618.79f,
                "glorious_anticipation", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.SKILLS_GAIN_GLORY_EVERY_SECONDS_FOR_EACH_RARE_OR_UNIQUE_MONSTER_IN_YOUR_PRESENCE, 1.0)
                ),
                List.of("45162")
        ));
        // [Stat] 3% increased maximum Life
        // [Stat] 20% increased amount of Life [LifeLeech|Leeched]
        list.add(TalentNodeDef.of(
                "47316", -5056.08f, 10515.4f,
                "goring", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_HEALTH, 0.03),
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.2)
                ),
                List.of("2888", "9583")
        ));
        // [Stat] [Minion|Minions] [Reviving|Revive] 15% faster
        // [Stat] Recover 2% of maximum Life when one of your [Minion|Minions] is [Reviving|Revived]
        list.add(TalentNodeDef.of(
                "41905", 2165.23f, -6650.5f,
                "gravedigger", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_REVIVING_FASTER, 0.15),
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_WHEN_ONE_OF_YOUR_MINIONS_IS_REVIVING, 0.02)
                ),
                List.of("44255")
        ));
        // [Stat] 4% increased [Attack] Damage per 75 [ItemArmour|Item Armour] and [ItemEvasion|Evasion] on Equipped Shield
        list.add(TalentNodeDef.of(
                "27687", -8655.75f, 1787.01f,
                "greatest_defence", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_PER_ITEM_ARMOR_AND_ITEM_EVASION_ON_EQUIPPED_SHIELD, 0.04)
                ),
                List.of("30371")
        ));
        // [Stat] [Grenade] Skills have +1 Cooldown Use
        list.add(TalentNodeDef.of(
                "58714", -1039.72f, 6312.71f,
                "grenadier", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GRENADE_SKILLS_HAVE_COOLDOWN_USE, 1.0)
                ),
                List.of("48429")
        ));
        // [Stat] [Minion|Minions] have 40% increased [CriticalDamageBonus|Critical Damage Bonus]
        list.add(TalentNodeDef.of(
                "31175", -2022.55f, -8528.3f,
                "grip_of_evil", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_CRITICAL_DAMAGE_BONUS, 0.4)
                ),
                List.of("15809")
        ));
        // [Stat] 10% chance when you gain an [Charges|Endurance Charge] to gain an additional [Charges|Endurance Charge]
        // [Stat] +1 to Maximum [Charges|Endurance Charges]
        list.add(TalentNodeDef.of(
                "20416", -4747.08f, -251.955f,
                "grit", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_WHEN_YOU_GAIN_AN_ENDURANCE_CHARGE_TO_GAIN_AN_ADDITIONAL_ENDURANCE_CHARGE, 0.1),
                        TalentNodeGen.stat(StatIds.TO_MAX_ENDURANCE_CHARGES, 1.0)
                ),
                List.of("28432")
        ));
        // [Stat] 16% increased Skill Effect Duration
        // [Stat] 16% increased [StunThreshold|Stun Threshold]
        list.add(TalentNodeDef.of(
                "25429", -2399.01f, -1380.22f,
                "grounded_in_the_earth", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.16),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.16)
                ),
                List.of("38103", "41838")
        ));
        // [Stat] [Minion|Minions] have 20% increased Area of Effect
        // [Stat] Minions have 20% increased Cooldown Recovery Rate
        list.add(TalentNodeDef.of(
                "14945", -4550.58f, -5724.9f,
                "growing_swarm", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_AREA_OF_EFFECT, 0.2),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_COOLDOWN_RECOVERY_RATE, 0.2)
                ),
                List.of("1218", "1447")
        ));
        // [Stat] The next [Attack] you use within 4 seconds after [HeavyStun|Heavy Stunning] a [Rarity|Rare or Unique] Enemy is [AncestralBoostSpell|Ancestrally Boosted]
        // [Stat] [AncestralBoost|Ancestrally Boosted] [Attack|Attacks] deal 30% increased Damage
        // DUP-VALUE-27：THE_NEXT_ATTACK_YOU_USE_WITHIN_SECONDS_AFTER_HEAVY_STUNNING_A_RARE_OR_UNIQUE_ENEMY_IS_ANCESTRALLY_BOOSTED
        // 是 flag 条目，其 Wire 已把 amount 烘死为 1.0（StatEntry 唯一数值槽被 flag 占用），故此处只能无值授予；
        // 原先注入的 4.0 会被静默忽略（虚值），已移除。
        // TODO: 语料「within 4 seconds」的时间窗尚未建模——需独立的时间窗通道，勿塞进 $value。
        list.add(TalentNodeDef.of(
                "4331", -10588.9f, 5580.44f,
                "guided_hand", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.THE_NEXT_ATTACK_YOU_USE_WITHIN_SECONDS_AFTER_HEAVY_STUNNING_A_RARE_OR_UNIQUE_ENEMY_IS_ANCESTRALLY_BOOSTED),
                        TalentNodeGen.stat(StatIds.ANCESTRALLY_BOOSTED_ATTACKS_DEAL_INC_DAMAGE, 0.3)
                ),
                List.of("17112")
        ));
        // [Stat] Recover 3% of maximum Life for each [Charges|Endurance Charge] consumed
        // [Stat] +1 to Maximum [Charges|Endurance Charges]
        list.add(TalentNodeDef.of(
                "46499", -8210.95f, 2826.15f,
                "guts", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_FOR_EACH_ENDURANCE_CHARGE_CONSUMED, 0.03),
                        TalentNodeGen.stat(StatIds.TO_MAX_ENDURANCE_CHARGES, 1.0)
                ),
                List.of("38365")
        ));
        // [Stat] 25% increased [Warcry|Warcry] Speed
        // [Stat] [Warcry|Warcries] [Debilitate] Enemies
        // [Stat] [Warcry|Warcry] Skills have 25% increased Area of Effect
        list.add(TalentNodeDef.of(
                "29762", -8422.83f, -1740.77f,
                "guttural_roar", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_SPEED, 0.25),
                        TalentNodeGen.stat(StatIds.WARCRIES_DEBILITATE_ENEMIES, 1.0),
                        TalentNodeGen.stat(StatIds.WARCRY_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.25)
                ),
                List.of("40328", "62200")
        ));
        // [Stat] Enemies you inflict [Bleeding] on cannot Regenerate Life
        // regen} 作用域），条件按敌人持 bleed 求值。
        list.add(TalentNodeDef.of(
                "33229", 4326.12f, -10969.2f,
                "haemorrhaging_cuts", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_YOU_INFLICT_BLEEDING_ON_CANNOT_REGENERATE_HEALTH)
                ),
                List.of("64996", "8938")
        ));
        // [Stat] [Empowered] [Attack|Attacks] [Gain] 16% of Damage as Extra [Cold] Damage
        list.add(TalentNodeDef.of(
                "44974", 5965.19f, -239.805f,
                "hail", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.EMPOWERED_ATTACKS_GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE, 0.16)
                ),
                List.of("60700")
        ));
        // [Stat] 15% increased Life Recovery rate
        list.add(TalentNodeDef.of(
                "15374", -3410.15f, 7031.06f,
                "hale_heart", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_RATE, 0.15)
                ),
                List.of("48035")
        ));
        // [Stat] 20% increased Life Recovery from [Flask|Flasks]
        // [Stat] Life [Flask|Flasks] gain 0.1 charges per Second
        list.add(TalentNodeDef.of(
                "52803", 6555.55f, -892.865f,
                "hale_traveller", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.2),
                        TalentNodeGen.stat(StatIds.HEALTH_FLASKS_GAIN_CHARGES_PER_SECOND, 0.1)
                ),
                List.of("26363", "45709")
        ));
        // [Stat] Gain additional [AilmentThreshold|Ailment Threshold] equal to 20% of maximum [EnergyShield|Energy Shield]
        // [Stat] Gain additional [StunThreshold|Stun Threshold] equal to 20% of maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "34531", -1692.81f, -6600.67f,
                "hallowed", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.2),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.2)
                ),
                List.of("25363", "44098")
        ));
        // [Stat] 40% increased [Flask] Life Recovery rate
        // [Stat] Regenerate 0.75% of maximum Life per second
        list.add(TalentNodeDef.of(
                "50609", -182.505f, 3700.21f,
                "hard_to_kill", "passives_increasedmaximumlifenotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_HEALTH_RECOVERY_RATE, 0.4),
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND, 0.0075)
                ),
                List.of("11916", "14926")
        ));
        // [Stat] [Totem|Totems] gain +20% to all Elemental [Resistances]
        // [Stat] [Totem|Totems] have 20% additional [Physical] Damage Reduction
        list.add(TalentNodeDef.of(
                "24438", -4114.17f, 4317.84f,
                "hardened_wood", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.TOTEMS_GAIN_TO_ALL_ELEMENTAL_RESISTANCES, 0.2),
                        TalentNodeGen.stat(StatIds.TOTEMS_HAVE_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION, 0.2)
                ),
                List.of("13171", "46748", "48121")
        ));
        // [Stat] 15% increased [Critical|Critical Hit] Chance against [Shock|Shocked] Enemies
        // [Stat] 40% increased [BuffMagnitude|Magnitude] of [Shock|Shock] you inflict with [Critical|Critical Hits]
        list.add(TalentNodeDef.of(
                "40480", 5368.5f, -1842.95f,
                "harmonic_generator", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_SHOCKED_ENEMIES, 0.15),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT_WITH_CRITICAL_HITS, 0.4)
                ),
                List.of("1953")
        ));
        // [Stat] 20% increased Damage for each type of [ElementalAilments|Elemental Ailment] on Enemy
        list.add(TalentNodeDef.of(
                "12611", 5581.73f, 1347.41f,
                "harness_the_elements", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_FOR_EACH_TYPE_OF_ELEMENTAL_AILMENT_ON_ENEMY, 0.2)
                ),
                List.of("32155", "44204", "64213")
        ));
        // [Stat] 8% increased Cast Speed with [Cold] Skills
        // [Stat] 16% increased Skill Effect Duration
        list.add(TalentNodeDef.of(
                "26331", 6314.44f, -7691.65f,
                "harsh_winter", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_WITH_COLD_SKILLS, 0.08),
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.16)
                ),
                List.of("12166", "3128")
        ));
        return list;
    }
}
