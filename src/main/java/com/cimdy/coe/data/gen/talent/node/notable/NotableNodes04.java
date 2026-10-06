package com.cimdy.coe.data.gen.talent.node.notable;

import com.cimdy.coe.mixin.client.render.Sprinting;
import com.cimdy.coe.data.gen.stat.wirings.wire.flow.WireWhile;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.Slow;
import com.cimdy.coe.data.helper.engine.ground.VolatilityDetonationManager;

import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.cimdy.coe.data.codec.support.rarity.Rarity;

public final class NotableNodes04 {

    private NotableNodes04() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();
        // [Stat] 5% increased Cast Speed
        // [Stat] 10% increased Cast Speed when on Full Life
        list.add(TalentNodeDef.of(
                "44293", -1958.74f, -4418.41f,
                "hastening_barrier", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.05),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_WHEN_ON_FULL_HEALTH, 0.1)
                ),
                List.of("14096")
        ));
        // [Stat] [Projectile|Projectiles] have 30% increased [CriticalDamageBonus|Critical Damage Bonus] against Enemies further than 6m
        // [Stat] [Projectile|Projectiles] have 20% increased [Critical] Hit Chance against Enemies further than 6m
        // [Stat] 25% chance to inflict [Daze] with [HitDamage|Hits] against Enemies further than 6m
        list.add(TalentNodeDef.of(
                "48215", -1169.57f, 7550.25f,
                "headshot", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_FURTHER_THAN_XM, 0.3),
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_FURTHER_THAN_XM, 0.2),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_DAZE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_FURTHER_THAN_XM, 0.25)
                ),
                List.of("516", "61347", "64488", "7201")
        ));
        // [Stat] Regenerate 0.5% of maximum Life per second if you have been Hit [Recently]
        // [Stat] 8% of Damage taken [Recoup|Recouped] as Life
        list.add(TalentNodeDef.of(
                "35966", -6922.03f, -5171.81f,
                "heart_tissue", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND_IF_YOU_HAVE_BEEN_HIT_RECENTLY, 0.005),
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.08)
                ),
                List.of("38368", "54288")
        ));
        // [Stat] 25% increased [CriticalDamageBonus|Critical Damage Bonus]
        // [Stat] +10 to [Strength]
        list.add(TalentNodeDef.of(
                "13407", 11471.9f, 733.091f,
                "heartbreaking", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.25),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 10.0)
                ),
                List.of("23040", "2936")
        ));
        // [Stat] +10 to [Intelligence]
        // [Stat] 20% increased [Critical|Critical Hit Chance]
        list.add(TalentNodeDef.of(
                "38537", 11240.0f, 501.159f,
                "heartstopping", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 10.0),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.2)
                ),
                List.of("54983", "55621")
        ));
        // [Stat] Enemies in your [Presence] have 75% reduced Life Regeneration rate
        list.add(TalentNodeDef.of(
                "9896", -4361.86f, -9358.09f,
                "heartstopping_presence", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_IN_YOUR_PRESENCE_HAVE_INC_HEALTH_REGENERATION_RATE, -0.75)
                ),
                List.of("40006")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] +30% of [Armour] also applies to [Fire|Fire Damage]
        // [Stat] 30% reduced [BuffMagnitude|Magnitude] of [Ignite|Ignite] on you
        list.add(TalentNodeDef.of(
                "372", -5427.8f, 5511.25f,
                "heatproof", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_FIRE_DAMAGE, 0.3),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU, -0.3)
                ),
                List.of("14655")
        ));
        // [Stat] 5% reduced [Attack] Speed
        // [Stat] 40% increased [Projectile] Damage
        // [Stat] 40% increased [Projectile|Projectile] [Stun|Stun Buildup]
        list.add(TalentNodeDef.of(
                "11826", 2231.82f, 3124.71f,
                "heavy_ammunition", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, -0.05),
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.4),
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_STUN_BUILDUP, 0.4)
                ),
                List.of("17726")
        ));
        // [Stat] Gain [Armour] equal to 150% of total [Strength] Requirements of Equipped Boots, Legs and Helmet
        list.add(TalentNodeDef.of(
                "59589", -9449.77f, 4077.68f,
                "heavy_armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ARMOR_EQUAL_TO_OF_TOTAL_STRENGTH_REQUIREMENTS_OF_EQUIPPED_BOOTS_LEGS_AND_HELMET, 1.5)
                ),
                List.of("10830", "52659")
        ));
        // [Stat] 25% increased Damage with Swords
        list.add(TalentNodeDef.of(
                "27290", -3711.3f, 11983.9f,
                "heavy_blade", "passives_damagesword",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SWORDS, 0.25)
                ),
                List.of("38564")
        ));
        // [Stat] 40% increased maximum [EnergyShield|Energy Shield]
        // [Stat] 5% of Damage taken bypasses [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "27491", -1486.69f, -6231.67f,
                "heavy_buffer", "passives_energyshield",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.4),
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_BYPASSES_ENERGY_SHIELD, 0.05)
                ),
                List.of("36994", "4061")
        ));
        // [Stat] Hits that [HeavyStun|Heavy Stun] Enemies have [CullingStrike|Culling Strike]
        list.add(TalentNodeDef.of(
                "56997", -4469.07f, 5221.32f,
                "heavy_contact", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.HITS_THAT_HEAVY_STUN_ENEMIES_HAVE_CULLING_STRIKE, 1.0)
                ),
                List.of("23861", "54886")
        ));
        // [Stat] 20% increased Life Recovery from [Flask|Flasks]
        // [Stat] Life [Flask|Flasks] applied to you grant [Guard] for 4 seconds equal to 8% of the Life Recovery per Second they apply
        list.add(TalentNodeDef.of(
                "15617", -3759.41f, 5937.79f,
                "heavy_drinker", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.2),
                        TalentNodeGen.stat(StatIds.HEALTH_FLASKS_APPLIED_TO_YOU_GRANT_GUARD_FOR_SECONDS_EQUAL_TO_OF_THE_HEALTH_RECOVERY_PER_SECOND_THEY_APPLY, 4.0)
                ),
                List.of("45962")
        ));
        // [Stat] 20% increased [Freeze|Freeze] Buildup
        // [Stat] [HitDamage|Hits] ignore non-negative [ElementalDamage|Elemental] [Resistances] of [Frozen] Enemies
        list.add(TalentNodeDef.of(
                "4959", 6689.44f, -7908.16f,
                "heavy_frost", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.2),
                        TalentNodeGen.stat(StatIds.HIT_DAMAGE_IGNORE_NON_NEGATIVE_ELEMENTAL_DAMAGE_RESISTANCES_OF_FROZEN_ENEMIES, 1.0)
                ),
                List.of("12166", "22713")
        ));
        // [Stat] 15% increased [Melee] Damage
        // [Stat] 15% increased [Stun] Buildup with [Melee|Melee Damage]
        // [Stat] +15 to [Strength]
        list.add(TalentNodeDef.of(
                "41512", 1410.1f, 3696.45f,
                "heavy_weaponry", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP_WITH_MELEE_DAMAGE, 0.15),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 15.0)
                ),
                List.of("43575")
        ));
        // [Stat] +3 to [StunThreshold|Stun Threshold] per [Strength|Strength]
        list.add(TalentNodeDef.of(
                "48418", -3060.09f, 7913.29f,
                "hefty_unit", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STUN_THRESHOLD_PER_STRENGTH, 3.0)
                ),
                List.of("34671", "61934")
        ));
        // [Stat] 20% increased Physical Damage with Bows
        // [Stat] [Bow|Bow] [Attack|Attacks] have [CullingStrike|Culling Strike]
        list.add(TalentNodeDef.of(
                "17894", -12379.7f, -807.525f,
                "her_final_bite", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE_WITH_BOWS, 0.2),
                        TalentNodeGen.stat(StatIds.BOW_ATTACKS_HAVE_CULLING_STRIKE, 1.0)
                ),
                List.of("60708")
        ));
        // [Stat] 20% increased chance to inflict [Ailments]
        // [Stat] 20% increased [Physical] Damage
        list.add(TalentNodeDef.of(
                "45777", -4308.03f, 11034.5f,
                "hidden_barb", "passives_physicaldamagechaosnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_INFLICT_AILMENTS, 0.2),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.2)
                ),
                List.of("27999")
        ));
        // [Stat] [Gain] 8% of Damage as Extra Damage of a random [ElementalDamage|Element] while [Shapeshift|Shapeshifted]
        list.add(TalentNodeDef.of(
                "479", -6036.75f, -1514.07f,
                "hidden_forms", "passives_druidgenericshapeshiftnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_DAMAGE_OF_A_RANDOM_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED, 0.08)
                ),
                List.of("28770", "29126")
        ));
        // [Stat] 40% increased [Armour] while [Shapeshift|Shapeshifted]
        // [Stat] +1% to [MaximumResistances|Maximum Fire Resistance] while [Shapeshift|Shapeshifted]
        // [Stat] 25% increased [StunThreshold|Stun Threshold] while [Shapeshift|Shapeshifted]
        list.add(TalentNodeDef.of(
                "41935", -9155.13f, -6541.86f,
                "hide_of_the_bear", "passives_druidshapeshiftbearnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_SHAPESHIFTED, 0.4),
                        TalentNodeGen.stat(StatIds.TO_MAX_FIRE_RESISTANCE_WHILE_SHAPESHIFTED, 0.01),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_WHILE_SHAPESHIFTED, 0.25)
                ),
                List.of("6735")
        ));
        // [Stat] 50% increased [Evasion|Evasion Rating] when on Full Life
        // [Stat] 25% increased [StunThreshold|Stun Threshold] while on Full Life
        list.add(TalentNodeDef.of(
                "30456", 5286.04f, 719.085f,
                "high_alert", "passives_evade",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION_WHEN_ON_FULL_HEALTH, 0.5),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_WHILE_ON_FULL_HEALTH, 0.25)
                ),
                List.of("11813", "38044")
        ));
        // [Stat] 30% increased Damage with [HitDamage|Hits] against [Hinder|Hindered] Enemies
        // [Stat] [Debuff|Debuffs] you inflict have 10% increased [SlowMagnitudeModifier|Slow Magnitude]
        list.add(TalentNodeDef.of(
                "54805", 4322.07f, -5417.76f,
                "hindered_capabilities", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_HINDERED_ENEMIES, 0.3),
                        TalentNodeGen.stat(StatIds.DEBUFFS_YOU_INFLICT_HAVE_INC_SLOW_MAGNITUDE, 0.1)
                ),
                List.of("59053")
        ));
        // [Stat] [Debuff|Debuffs] inflicted by [Hazard|Hazards] have 30% increased [SlowMagnitudeModifier|Slow Magnitude]
        // [Stat] 30% increased [Hazard] [Immobilised|Immobilisation] buildup
        list.add(TalentNodeDef.of(
                "60273", 7001.73f, 9638.79f,
                "hindering_obstacles", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_INFLICTED_BY_HAZARDS_HAVE_INC_SLOW_MAGNITUDE, 0.3),
                        TalentNodeGen.stat(StatIds.INC_HAZARD_IMMOBILISATION_BUILDUP, 0.3)
                ),
                List.of("28199", "34845")
        ));
        // [Stat] [Minion|Minions] have 25% increased maximum Life
        // [Stat] 10% increased [Block] chance
        list.add(TalentNodeDef.of(
                "23078", -7639.42f, -6210.15f,
                "holy_protector", "passives_miraclemaker",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.25),
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.1)
                ),
                List.of("54964")
        ));
        // [Stat] 8% increased [Projectile] Speed
        // [Stat] 8% increased [Attack] Speed
        // [Stat] +10 to [Dexterity]
        list.add(TalentNodeDef.of(
                "28992", 2484.25f, 991.319f,
                "honed_instincts", "passives_hunter",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.08),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0)
                ),
                List.of("13783", "16460", "18548", "22795", "4313", "43923", "50328")
        ));
        // [Stat] 25% increased [Armour] if you've [HitDamage|Hit] an Enemy with a [Melee|Melee Attack] [Recently]
        // [Stat] 50% increased [Melee|Melee Damage] against [Immobilised] Enemies
        list.add(TalentNodeDef.of(
                "48014", -8623.21f, 2983.12f,
                "honourless", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_IF_YOU_VE_HIT_DAMAGE_AN_ENEMY_WITH_A_MELEE_ATTACK_RECENTLY, 0.25),
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE_AGAINST_IMMOBILISED_ENEMIES, 0.5)
                ),
                List.of("63790")
        ));
        // [Stat] [Warcry|Warcries] inflict 3 [CriticalWeakness|Critical Weakness] on Enemies
        list.add(TalentNodeDef.of(
                "30395", -7117.38f, -2467.53f,
                "howling_beast", "passives_druidshapeshiftwolfnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.WARCRIES_INFLICT_CRITICAL_WEAKNESS_ON_ENEMIES, 3.0)
                ),
                List.of("37260")
        ));
        // [Stat] 30% increased [Stun] Buildup
        // [Stat] +15 to [Strength]
        list.add(TalentNodeDef.of(
                "4673", -7877.16f, -1345.91f,
                "hulking_smash", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.3),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 15.0)
                ),
                List.of("10047")
        ));
        // [Stat] Recover 20 Life when you [Block]
        // [Stat] +2% to maximum [Block] chance
        // [Stat] 80% less [Knockback] Distance for [Block|Blocked] [HitDamage|Hits]
        list.add(TalentNodeDef.of(
                "57471", -8655.75f, 2121.01f,
                "hunker_down", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_HEALTH_WHEN_YOU_BLOCK, 20.0),
                        TalentNodeGen.stat(StatIds.TO_MAX_BLOCK_CHANCE, 0.02),
                        TalentNodeGen.stat(StatIds.MORE_KNOCKBACK_DISTANCE_FOR_BLOCKED_HIT_DAMAGE, -0.8)
                ),
                List.of("42578")
        ));
        // [Stat] 50% increased Damage against Demons
        // [Stat] 50% increased Duration of [Ailments|Ailments] on Beasts
        // [Stat] 50% increased [Critical|Critical Hit Chance] against Humanoids
        // [Stat] 50% increased [Immobilised|Immobilisation] buildup against Constructs
        list.add(TalentNodeDef.of(
                "48617", 1847.18f, 10118.8f,
                "hunter", "passives_witchhunter_witchunternode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_DEMONS, 0.5),
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_AILMENTS_ON_BEASTS, 0.5),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_HUMANOIDS, 0.5),
                        TalentNodeGen.stat(StatIds.INC_IMMOBILISATION_BUILDUP_AGAINST_CONSTRUCTS, 0.5)
                ),
                List.of("11667", "15839", "1915", "6266")
        ));
        // [Stat] +1 [Charm] Slot
        list.add(TalentNodeDef.of(
                "33099", 7116.45f, 8846.08f,
                "hunter_s_talisman", "passives_charmnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.CHARM_SLOT, 1.0)
                ),
                List.of("21111", "43522")
        ));
        // [Stat] 20% increased [CullingStrike|Culling Strike] Threshold
        // [Stat] [CullingStrike|Culling Strike] against Beasts while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "32655", 7050.34f, 2711.86f,
                "hunting_companion", "passives_companionsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CULLING_STRIKE_THRESHOLD, 0.2),
                        TalentNodeGen.stat(StatIds.CULLING_STRIKE_AGAINST_BEASTS_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 1.0)
                ),
                List.of("24178", "52415")
        ));

        // [Stat] [Gain] 6% of [Cold] damage as Extra [Lightning] damage
        // [Stat] Unaffected by [ChilledGround|Chilled Ground]
        list.add(TalentNodeDef.of(
                "23907", -317.615f, -7755.36f,
                "ice_storm", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_COLD_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.UNAFFECTED_BY_CHILLED_GROUND, 1.0)
                ),
                List.of("17044", "6554")
        ));
        // [Stat] 200% increased [IceCrystals|Ice Crystal] Life
        list.add(TalentNodeDef.of(
                "55847", 3884.6f, -8091.37f,
                "ice_walls", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ICE_CRYSTAL_HEALTH, 2.0)
                ),
                List.of("23455")
        ));
        // [Stat] Gain 50% of maximum [EnergyShield|Energy Shield] as additional [FreezeThreshold|Freeze Threshold]
        list.add(TalentNodeDef.of(
                "4031", 8366.22f, -5546.51f,
                "icebreaker", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_MAX_ENERGY_SHIELD_AS_ADDITIONAL_FREEZE_THRESHOLD, 0.5)
                ),
                List.of("32672")
        ));
        // [Stat] Every [Rage] also grants 1% increased [Fire] Damage
        list.add(TalentNodeDef.of(
                "32932", -4204.37f, 5061.4f,
                "ichlotl_s_inferno", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.EVERY_RAGE_ALSO_GRANTS_INC_FIRE_DAMAGE, 0.01)
                ),
                List.of("14205", "7720")
        ));
        // [Stat] Gain 3 [Rage] when [HitDamage|Hit] by an Enemy
        // [Stat] Every [Rage] also grants 2% increased [StunThreshold|Stun Threshold]
        list.add(TalentNodeDef.of(
                "7341", -5946.57f, 8472.91f,
                "ignore_pain", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_WHEN_HIT_DAMAGE_BY_AN_ENEMY, 3.0),
                        TalentNodeGen.stat(StatIds.EVERY_RAGE_ALSO_GRANTS_INC_STUN_THRESHOLD, 0.02)
                ),
                List.of("56342")
        ));
        // [Stat] 20% increased [LightRadius|Light Radius]
        // [Stat] 70% increased [EnergyShield|Energy Shield] from Equipped Helmet
        list.add(TalentNodeDef.of(
                "1823", -2331.94f, -6719.67f,
                "illuminated_crown", "passives_energyshield",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHT_RADIUS, 0.2),
                        TalentNodeGen.stat(StatIds.INC_ENERGY_SHIELD_FROM_EQUIPPED_HELMET, 0.7)
                ),
                List.of("25363", "7424")
        ));
        // [Stat] 6% increased [Attack] Speed during any [Flask] Effect
        // [Stat] 25% increased Damage during any [Flask] Effect
        list.add(TalentNodeDef.of(
                "50912", 4380.72f, 197.945f,
                "imbibed_power", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_DURING_ANY_FLASK_EFFECT, 0.06),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_DURING_ANY_FLASK_EFFECT, 0.25)
                ),
                List.of("21314", "49461")
        ));
        // [Stat] 50% increased [Evasion|Evasion Rating] if [ESRecharge|Energy Shield Recharge] has started in the past 2 seconds
        // [Stat] 30% increased [Evasion|Evasion Rating] while you have [EnergyShield|Energy Shield]
        // 两条均已接线：上条 → COND_ES_RECHARGE_STARTED_2S（ES 充能启动边沿，2 秒窗口，见 WireWhile）；
        // 下条 → COND_HAS_ENERGY_SHIELD（既有）。权威数值 poe2wiki evasion_and_energy_shield14 = 50 / 30。
        list.add(TalentNodeDef.of(
                "19156", 4435.38f, -1728.44f,
                "immaterial", "passives_evasionandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION_IF_ENERGY_SHIELD_RECHARGE_HAS_STARTED_IN_THE_PAST_SECONDS, 0.5),
                        TalentNodeGen.stat(StatIds.INC_EVASION_WHILE_YOU_HAVE_ENERGY_SHIELD, 0.3)
                ),
                List.of("12249")
        ));
        // [Stat] 25% increased [Ignite|Ignite] [BuffMagnitude|Magnitude]
        // [Stat] +10 to [Strength]
        list.add(TalentNodeDef.of(
                "53030", -8778.46f, -5561.23f,
                "immolation", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_BUFF_MAGNITUDE, 0.25),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 10.0)
                ),
                List.of("11525")
        ));
        // [Stat] 6% increased Life Recovery rate
        // [Stat] Recover 2% of maximum Life on Kill
        // [Stat] +10 to [Intelligence]
        list.add(TalentNodeDef.of(
                "24062", 6807.02f, -3393.53f,
                "immortal_infamy", "passives_hiredkiller2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_RATE, 0.06),
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_ON_KILL, 0.02),
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 10.0)
                ),
                List.of("54351")
        ));
        // [Stat] 15% increased maximum [EnergyShield|Energy Shield]
        // [Stat] 25% increased amount of Mana [ManaLeech|Leeched]
        list.add(TalentNodeDef.of(
                "51871", 7065.9f, -3324.15f,
                "immortal_thirst", "passives_manaleechthemednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.15),
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_MANA_LEECH, 0.25)
                ),
                List.of("8045")
        ));
        // [Stat] 12% increased Area of Effect if you have [Stun|Stunned] an Enemy [Recently|Recently]
        // [Stat] 12% increased Area of Effect for [Attack|Attacks]
        list.add(TalentNodeDef.of(
                "16626", -7004.17f, 906.325f,
                "impact_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_IF_YOU_HAVE_STUNNED_AN_ENEMY_RECENTLY, 0.12),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.12)
                ),
                List.of("53089", "9918")
        ));
        // [Stat] 20% increased [Stun] Buildup
        // [Stat] 25% increased [Attack] Area Damage
        list.add(TalentNodeDef.of(
                "64443", -6572.89f, 1155.33f,
                "impact_force", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.2),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AREA_DAMAGE, 0.25)
                ),
                List.of("10474", "41126")
        ));
        // [Stat] 25% increased Damage with One Handed Weapons
        // [Stat] [Attack|Attacks] have 10% chance to [Maim|Maim] on [HitDamage|Hit]
        list.add(TalentNodeDef.of(
                "46696", -5481.04f, 2464.26f,
                "impair", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_ONE_HANDED_WEAPONS, 0.25),
                        TalentNodeGen.stat(StatIds.ATTACKS_HAVE_CHANCE_TO_MAIM_ON_HIT_DAMAGE, 0.1)
                ),
                List.of("57552")
        ));
        // [Stat] 40% faster [Curse] Activation
        // [Stat] Your [Curse|Curses] have 20% increased [BuffMagnitude|Magnitudes] if 50% of [Curse] Duration expired
        list.add(TalentNodeDef.of(
                "21748", 6738.25f, -6987.61f,
                "impending_doom", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.FASTER_CURSE_ACTIVATION, 0.4),
                        TalentNodeGen.stat(StatIds.YOUR_CURSES_HAVE_INC_BUFF_MAGNITUDE_IF_OF_CURSE_DURATION_EXPIRED, 0.2)
                ),
                List.of("54725")
        ));
        // [Stat] Defend with 150% of [Armour] against [HitDamage|Hits] from Enemies that are further than 6m away
        list.add(TalentNodeDef.of(
                "65023", -1529.48f, 6665.33f,
                "impenetrable_shell", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.DEFEND_WITH_OF_ARMOR_AGAINST_HIT_DAMAGE_FROM_ENEMIES_THAT_ARE_FURTHER_THAN_XM_AWAY, 1.5)
                ),
                List.of("13942")
        ));
        // [Stat] Regenerate 2.5% of maximum Life per second while [Surrounded|Surrounded]
        list.add(TalentNodeDef.of(
                "35028", -625.037f, 8869.68f,
                "in_the_thick_of_it", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_SURROUNDED, 0.025)
                ),
                List.of("22556")
        ));
        // [Stat] 40% increased [Melee] Damage with [HitDamage|Hits] at [CloseRange|Close Range]
        list.add(TalentNodeDef.of(
                "57379", -9859.06f, -827.832f,
                "in_your_face", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE_WITH_HIT_DAMAGE_AT_CLOSE_RANGE, 0.4)
                ),
                List.of("15427", "39190")
        ));
        // [Stat] 60% increased [Flammability] [BuffMagnitude|Magnitude]
        // [Stat] 30% increased Damage with [HitDamage|Hits] against [Burning|Burning] Enemies
        list.add(TalentNodeDef.of(
                "62310", -3533.23f, 9699.68f,
                "incendiary", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLAMMABILITY_BUFF_MAGNITUDE, 0.6),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BURNING_ENEMIES, 0.3)
                ),
                List.of("36325", "9796")
        ));
        // [Stat] 50% increased effect of [Incision]
        list.add(TalentNodeDef.of(
                "36630", 3556.83f, 3447.44f,
                "incision", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_INCISION, 0.5)
                ),
                List.of("28268", "49473", "60829")
        ));
        // [Stat] 40% increased [Freeze|Freeze] Buildup
        // [Stat] 20% increased [Freeze] Duration on Enemies
        list.add(TalentNodeDef.of(
                "47270", -243.565f, -11875.1f,
                "inescapable_cold", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.4),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_DURATION_ON_ENEMIES, 0.2)
                ),
                List.of("52799", "62914")
        ));
        // [Stat] 10% chance for [Attack] [HitDamage|Hits] to apply ten [Incision]
        list.add(TalentNodeDef.of(
                "22817", 7125.44f, 6789.98f,
                "inevitable_rupture", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_TEN_INCISION, 0.1)
                ),
                List.of("22962", "55724")
        ));
        // [Stat] +1 to maximum [ElementalInfusion|Fire Infusions]
        list.add(TalentNodeDef.of(
                "61354", -763.995f, -9489.3f,
                "infernal_limit", "passives_instillationsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_FIRE_INFUSIONS, 1.0)
                ),
                List.of("29391")
        ));
        // [Stat] +20 to maximum Life
        // [Stat] 8% of Damage taken [Recoup|Recouped] as Life
        list.add(TalentNodeDef.of(
                "57110", 2471.48f, -3214.01f,
                "infused_flesh", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_HEALTH, 20.0),
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.08)
                ),
                List.of("62159")
        ));
        // [Stat] +1 to maximum number of [ElementalInfusion|Elemental Infusions]
        list.add(TalentNodeDef.of(
                "38965", -4891.99f, -10304.5f,
                "infused_limits", "passives_instillationsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_NUMBER_OF_ELEMENTAL_INFUSIONS, 1.0)
                ),
                List.of("51303")
        ));
        // [Stat] 10% chance when collecting an [ElementalInfusion|Elemental Infusion] to gain an
        //        additional [ElementalInfusion|Elemental Infusion] of the same type
        list.add(TalentNodeDef.of(
                "24764", -4956.7f, -10063.0f,
                "infusing_power", "passives_instillationsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_WHEN_COLLECTING_AN_ELEMENTAL_INFUSION_TO_GAIN_AN_ADDITIONAL_ELEMENTAL_INFUSION_OF_THE_SAME_TYPE, 0.1)
                ),
                List.of("12324")
        ));
        // [Stat] Gain a [Charges|Power Charge] when you consume an [ElementalInfusion|Elemental Infusion]
        list.add(TalentNodeDef.of(
                "59387", 2081.9f, -8262.87f,
                "infusion_of_power", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_A_POWER_CHARGE_WHEN_YOU_CONSUME_AN_ELEMENTAL_INFUSION, 1.0)
                ),
                List.of("51892")
        ));
        // [Stat] +25 to [Intelligence]
        list.add(TalentNodeDef.of(
                "39567", 2922.78f, -10630.7f,
                "ingenuity", "passives_plusintelligence",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 25.0)
                ),
                List.of("50755", "50816")
        ));
        // [Stat] [Warcry|Warcries] have 15% chance to [Empowered|Empower] 3 additional [Attack|Attacks]
        list.add(TalentNodeDef.of(
                "46683", -9561.93f, 2828.92f,
                "inherited_strength", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.WARCRIES_HAVE_CHANCE_TO_EMPOWERED_ADDITIONAL_ATTACKS, 0.15)
                ),
                List.of("15247")
        ));
        // [Stat] 30% increased [Melee] Damage when on Full Life
        // [Stat] 16% increased [Attack] Speed if you haven't Attacked [Recently|Recently]
        list.add(TalentNodeDef.of(
                "23227", -2620.07f, 6887.23f,
                "initiative", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE_WHEN_ON_FULL_HEALTH, 0.3),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_IF_YOU_HAVEN_T_ATTACKED_RECENTLY, 0.16)
                ),
                List.of("3516")
        ));
        // [Stat] Adds 1 to 37 [Lightning] damage to [Attack|Attacks]
        list.add(TalentNodeDef.of(
                "64239", -7004.99f, -196.949f,
                "innate_rune", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.ADD_TO_LIGHTNING_DAMAGE_TO_ATTACKS, 1.0)
                ),
                List.of("61367")
        ));
        // [Stat] 20% increased [Evasion|Evasion Rating]
        // [Stat] 20% increased maximum [EnergyShield|Energy Shield]
        // [Stat] 25% reduced [BuffEffect|effect] of [Curse|Curses] on you
        list.add(TalentNodeDef.of(
                "30562", 4356.4f, -6892.55f,
                "inner_faith", "passives_evasionandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.2),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.2),
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_CURSES_ON_YOU, -0.25)
                ),
                List.of("13711", "3203")
        ));
        // [Stat] 18% increased maximum [EnergyShield|Energy Shield]
        // [Stat] 12% increased Mana Regeneration Rate
        // [Stat] 6% increased [Intelligence]
        list.add(TalentNodeDef.of(
                "116", 1359.02f, -3942.15f,
                "insightfulness", "passives_energyshield",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.18),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.12),
                        TalentNodeGen.stat(StatIds.INC_INTELLIGENCE, 0.06)
                ),
                List.of("6715")
        ));
        // [Stat] Increases and Reductions to [Companion] Damage also apply to you
        list.add(TalentNodeDef.of(
                "16150", 12070.4f, 2129.97f,
                "inspiring_ally", "passives_companionsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AND_REDUCTIONS_TO_COMPANION_DAMAGE_ALSO_APPLY_TO_YOU, 1.0)
                ),
                List.of("44699", "57933")
        ));
        // [Stat] [Banner|Banners] also grant +25% to all [ElementalDamage|Elemental] [Resistances] affected targets
        list.add(TalentNodeDef.of(
                "4661", -3689.44f, 9419.58f,
                "inspiring_leader", "passives_bannerareanotable",
                List.of(
                        TalentNodeGen.stat(StatIds.BANNERS_ALSO_GRANT_TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES_AFFECTED_TARGETS, 0.25)
                ),
                List.of("12821", "20842")
        ));
        // [Stat] 25% chance that when [Volatility] on you explodes, you regain an equivalent amount of [Volatility]
        // 爆炸事件已接 Stat 触发（volatility_detonated，VolatilityDetonationManager.detonate fire）；
        // 返还量简化为固定能量（EnergyHelper 基准）。
        list.add(TalentNodeDef.of(
                "43944", 7034.63f, -4489.25f,
                "instability", "passives_cursemitigationclusternode",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_THAT_WHEN_VOLATILITY_ON_YOU_EXPLODES_YOU_REGAIN_AN_EQUIVALENT_AMOUNT_OF_VOLATILITY, 0.25)
                ),
                List.of("30102", "53177")
        ));
        // [Stat] 25% increased [Armour] and [Evasion] Rating
        // [Stat] Gain [AilmentThreshold|Ailment Threshold] equal to the lowest of [Evasion|Evasion] and [Armour|Armour] on your Boots
        list.add(TalentNodeDef.of(
                "9736", 1582.62f, 8936.47f,
                "insulated_treads", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.25),
                        TalentNodeGen.stat(StatIds.GAIN_AILMENT_THRESHOLD_EQUAL_TO_THE_LOWEST_OF_EVASION_AND_ARMOR_ON_YOUR_BOOTS, 1.0)
                ),
                List.of("45037", "61318")
        ));
        // [Stat] 20% [FasterESRechargeStart|faster start of Energy Shield Recharge] while [Shapeshift|Shapeshifted]
        // [Stat] +20% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage] while [Shapeshift|Shapeshifted]
        list.add(TalentNodeDef.of(
                "48649", -3943.26f, -1152.64f,
                "insulating_hide", "passives_druidgenericshapeshiftnode",
                List.of(
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHILE_SHAPESHIFTED, 0.2),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED, 0.2)
                ),
                List.of("31673")
        ));
        // [Stat] 20% increased chance to inflict [Ailments]
        // [Stat] 15% increased Duration of [DamagingAilments|Damaging Ailments] on Enemies
        list.add(TalentNodeDef.of(
                "46182", 4766.0f, 3895.65f,
                "intense_dose", "passives_physicaldamagechaosnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_INFLICT_AILMENTS, 0.2),
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_DAMAGING_AILMENTS_ON_ENEMIES, 0.15)
                ),
                List.of("33838", "42460")
        ));
        // [Stat] 35% increased Damage with [HitDamage|Hits] against [Burning|Burning] Enemies
        list.add(TalentNodeDef.of(
                "65016", -3039.7f, -6941.15f,
                "intense_flames", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BURNING_ENEMIES, 0.35)
                ),
                List.of("7378")
        ));
        // [Stat] 20% chance to [Aggravate] [Bleeding] on targets you [HitDamage|Hit] with [Empowered] [Attack|Attacks]
        // [Stat] [Empowered|Empowered Attacks] deal 30% increased Damage
        list.add(TalentNodeDef.of(
                "7668", -5578.69f, 4936.32f,
                "internal_bleeding", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_HIT_DAMAGE_WITH_EMPOWERED_ATTACKS, 0.2),
                        TalentNodeGen.stat(StatIds.EMPOWERED_ATTACKS_DEAL_INC_DAMAGE, 0.3)
                ),
                List.of("47371")
        ));
        // [Stat] 35% increased Mana Regeneration Rate while stationary
        list.add(TalentNodeDef.of(
                "35369", -7642.03f, -4244.19f,
                "investing_energies", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE_WHILE_STATIONARY, 0.35)
                ),
                List.of("1459")
        ));
        // [Stat] [Archon] Buffs also grant +20% to all [ElementalDamage|Elemental] [Resistances]
        // [Stat] [Archon] Buffs also grant 10% increased Movement Speed
        list.add(TalentNodeDef.of(
                "41394", 4340.72f, -9132.87f,
                "invigorating_archon", "passives_archongenericnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.ARCHON_BUFFS_ALSO_GRANT_TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES, 0.2),
                        TalentNodeGen.stat(StatIds.ARCHON_BUFFS_ALSO_GRANT_INC_MOVEMENT_SPEED, 0.1)
                ),
                List.of("55938")
        ));
        // [Stat] Recover 1% of maximum Life per [Glory] consumed
        list.add(TalentNodeDef.of(
                "50023", -4057.89f, 8060.05f,
                "invigorating_grandeur", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_PER_GLORY_CONSUMED, 0.01)
                ),
                List.of("4921", "59180")
        ));
        // [Stat] Consume all [Rage] when [Shapeshift|Shapeshifting] to Human form to recover 1% of maximum life per [Rage] Consumed
        list.add(TalentNodeDef.of(
                "28408", -8943.24f, -835.177f,
                "invigorating_hate", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.CONSUME_ALL_RAGE_WHEN_SHAPESHIFTING_TO_HUMAN_FORM_TO_RECOVER_OF_MAX_HEALTH_PER_RAGE_CONSUMED, 0.01)
                ),
                List.of("13845", "21413")
        ));
        // [Stat] [Invoke|Invocated] [Spell|Spells] have 40% chance to consume half as much [Energy]
        list.add(TalentNodeDef.of(
                "24491", 1802.15f, -9298.16f,
                "invoked_echoes", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INVOKED_SPELLS_HAVE_CHANCE_TO_CONSUME_HALF_AS_MUCH_ENERGY, 0.4)
                ),
                List.of("18856")
        ));
        // [Stat] 10% increased Mana Cost [Efficiency]
        // [Stat] [Trigger|Triggered] [Spell|Spells] deal 40% increased [Spell] Damage
        list.add(TalentNodeDef.of(
                "51934", 6324.58f, -9381.54f,
                "invoked_efficiency", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_EFFICIENCY, 0.1),
                        TalentNodeGen.stat(StatIds.TRIGGERED_SPELLS_DEAL_INC_SPELL_DAMAGE, 0.4)
                ),
                List.of("43281")
        ));
        // [Stat] [Invoke|Invocated] skills have 30% increased Maximum [Energy]
        list.add(TalentNodeDef.of(
                "338", 1802.15f, -8728.16f,
                "invoked_limit", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INVOKED_SKILLS_HAVE_INC_MAX_ENERGY, 0.3)
                ),
                List.of("38069")
        ));
        // [Stat] +2 to [Armour] per 1 [ItemEnergyShield|Item Energy Shield] on Equipped Boots
        // [Stat] 12% reduced [Slow|Slowing] Potency of [Debuff|Debuffs] on You
        list.add(TalentNodeDef.of(
                "31724", -5081.34f, -955.755f,
                "iron_slippers", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ARMOR_PER_ITEM_ENERGY_SHIELD_ON_EQUIPPED_BOOTS, 2.0),
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.12)
                ),
                List.of("10635", "45301")
        ));
        // [Stat] 100% increased [ArmourBreak|Armour Break] Duration
        list.add(TalentNodeDef.of(
                "22626", -8119.16f, 5168.12f,
                "irreparable", "passives_armorbreak2bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_BREAK_DURATION, 1.0)
                ),
                List.of("30136", "45227")
        ));
        // [Stat] 2% increased Damage per 5 of your lowest [Attributes|Attribute]
        list.add(TalentNodeDef.of(
                "16618", 575.984f, 12271.5f,
                "jack_of_all_trades", "passives_ascendants_skillpoint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_PER_OF_YOUR_LOWEST_ATTRIBUTES, 0.02)
                ),
                List.of("24511", "35688")
        ));
        // [Stat] 40% increased [CriticalDamageBonus|Critical Damage Bonus] with [Spear|Spears]
        list.add(TalentNodeDef.of(
                "10265", 8120.55f, 7491.65f,
                "javelin", "passives_spearsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_SPEARS, 0.4)
                ),
                List.of("18910")
        ));
        // [Stat] [Attack|Attacks] used by [Ballista|Ballistas] have 10% increased [Attack] Speed
        // [Stat] 50% increased [Ballista] [Immobilised|Immobilisation] buildup
        list.add(TalentNodeDef.of(
                "37302", -3045.67f, 9857.89f,
                "kept_at_bay", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACKS_USED_BY_BALLISTAS_HAVE_INC_ATTACK_SPEED, 0.1),
                        TalentNodeGen.stat(StatIds.INC_BALLISTA_IMMOBILISATION_BUILDUP, 0.5)
                ),
                List.of("24368")
        ));
        // [Stat] 40% increased [Attack] Damage while on Full Life
        // [Stat] 60% increased [Attack|Attack] Damage while on [LowLife|Low Life]
        list.add(TalentNodeDef.of(
                "56453", 6002.36f, -2928.96f,
                "killer_instinct", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_ON_FULL_HEALTH, 0.4),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_ON_LOW_HEALTH, 0.6)
                ),
                List.of("37691")
        ));
        // [Stat] 3% increased Movement Speed
        // [Stat] 15% increased [Projectile] Speed
        // [Stat] 15% increased [Projectile] Damage
        list.add(TalentNodeDef.of(
                "26107", 7868.42f, -3177.92f,
                "kite_runner", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.03),
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, 0.15),
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.15)
                ),
                List.of("33713", "52361")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 12% of [Evasion|Evasion Rating]
        // [Stat] 15% increased [Block] chance
        // [Stat] 15% increased [ParriedDebuff|Parried Debuff] [BuffMagnitude|Magnitude]
        list.add(TalentNodeDef.of(
                "24736", -2790.24f, 8964.03f,
                "knight_of_chitus", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.12),
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_BUFF_MAGNITUDE, 0.15)
                ),
                List.of("3191", "35623")
        ));
        // [Stat] +12% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 10% of [Evasion|Evasion Rating]
        // [Stat] Banner Skills have 15% increased Aura [BuffMagnitude|Magnitudes]
        // [Stat] 25% reduced [ArmourBreak|Armour Break] taken
        list.add(TalentNodeDef.of(
                "9863", -3201.14f, 8928.08f,
                "knight_of_izaro", "passives_armorelementaldamagedeflect",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.12),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.1),
                        TalentNodeGen.stat(StatIds.BANNER_SKILLS_HAVE_INC_AURA_BUFF_MAGNITUDE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_BREAK_TAKEN, -0.25)
                ),
                List.of("3191", "65324")
        ));
        // [Stat] +20% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] 30% increased [Presence|Presence] Area of Effect
        // [Stat] 15% increased [Glory] generation
        list.add(TalentNodeDef.of(
                "1861", -3437.73f, 8590.2f,
                "knight_of_tarcus", "passives_elementalresistance2",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.3),
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION, 0.15)
                ),
                List.of("65324", "658")
        ));
        // [Stat] [TemporaryMinion|Temporary Minion] Skills have +2 to [Limit] of [Minion|Minions] summoned
        list.add(TalentNodeDef.of(
                "57079", -11113.6f, -6041.29f,
                "known_by_all", "passives_miniondamageblue",
                List.of(),
                List.of("19966", "22221")
        ));
        // [Stat] 25% increased [Attack] Damage if you have been [HeavyStunPlayer|Heavy Stunned] [Recently]
        // [Stat] 25% increased [Attack] Damage while you have no Life [Flask] uses left
        // [Stat] 25% increased [Attack|Attack] Damage while [Surrounded|Surrounded]
        // [Stat] 25% increased [Attack|Attack] Damage while on [LowLife|Low Life]
        list.add(TalentNodeDef.of(
                "2397", -1955.88f, 11130.6f,
                "last_stand", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_IF_YOU_HAVE_BEEN_HEAVY_STUNNED_RECENTLY, 0.25),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_YOU_HAVE_NO_HEALTH_FLASK_USES_LEFT, 0.25),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_SURROUNDED, 0.25),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_ON_LOW_HEALTH, 0.25)
                ),
                List.of("16111", "46268", "5324", "54437")
        ));
        // [Stat] 20% reduced [Slow|Slowing] Potency of [Debuff|Debuffs] on You
        // [Stat] [Buff|Buffs] on you expire 10% slower
        list.add(TalentNodeDef.of(
                "64659", -2933.5f, -11385.2f,
                "lasting_boons", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.2),
                        TalentNodeGen.stat(StatIds.BUFFS_ON_YOU_EXPIRE_SLOWER, -0.1)
                ),
                List.of("22538", "3355")
        ));
        // [Stat] 20% increased [Spell] Damage
        // [Stat] 15% increased Skill Effect Duration
        list.add(TalentNodeDef.of(
                "58096", -4366.41f, -2858.85f,
                "lasting_incantations", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.15)
                ),
                List.of("34096", "6008")
        ));
        // [Stat] 10% increased Skill Effect Duration
        // [Stat] 40% increased [Poison] Duration
        list.add(TalentNodeDef.of(
                "61741", 6359.19f, 2826.09f,
                "lasting_toxins", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.1),
                        TalentNodeGen.stat(StatIds.INC_POISON_DURATION, 0.4)
                ),
                List.of("23608", "40024")
        ));
        // [Stat] 5% reduced [Attack] Speed
        // [Stat] 30% increased [BuffMagnitude|Magnitude] of [Ailments] you inflict
        // [Stat] 20% increased Duration of [DamagingAilments|Damaging Ailments] on Enemies
        list.add(TalentNodeDef.of(
                "18496", -7463.54f, 5210.9f,
                "lasting_trauma", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, -0.05),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_AILMENTS_YOU_INFLICT, 0.3),
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_DAMAGING_AILMENTS_ON_ENEMIES, 0.2)
                ),
                List.of("13856")
        ));
        // [Stat] 30% increased Mana Cost [Efficiency] of [Attack|Attacks] during any Mana [Flask] Effect
        list.add(TalentNodeDef.of(
                "8607", -7459.86f, 621.685f,
                "lavianga_s_brew", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_EFFICIENCY_OF_ATTACKS_DURING_ANY_MANA_FLASK_EFFECT, 0.3)
                ),
                List.of("1973")
        ));
        // [Stat] 1% increased Damage per 1% Chance to [Block|Block]
        list.add(TalentNodeDef.of(
                "45599", -1349.52f, 5222.79f,
                "lay_siege", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_PER_CHANCE_TO_BLOCK, 0.01)
                ),
                List.of("32885", "6689")
        ));
        // [Stat] 30% increased [Presence|Presence] Area of Effect
        // [Stat] [Allies] in your [Presence] have 30% increased [Glory] generation
        list.add(TalentNodeDef.of(
                "40687", 3636.66f, 10951.4f,
                "lead_by_example", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.3),
                        TalentNodeGen.stat(StatIds.ALLIES_IN_YOUR_PRESENCE_HAVE_INC_GLORY_GENERATION, 0.3)
                ),
                List.of("55930", "5740")
        ));
        // [Stat] 50% increased [Critical|Critical Hit Chance] against Enemies that are on Full Life
        list.add(TalentNodeDef.of(
                "8531", -3273.57f, -4571.19f,
                "leaping_ambush", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH, 0.5)
                ),
                List.of("51534", "59256")
        ));
        // [Stat] +1 to [Evasion] Rating per 1 [ItemArmour|Item Armour] on Equipped Legs
        list.add(TalentNodeDef.of(
                "51446", -1656.4f, 11762.0f,
                "leather_bound_gauntlets", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_EVASION_PER_ITEM_ARMOR_ON_EQUIPPED_LEGS, 1.0)
                ),
                List.of("29399", "53647")
        ));
        // [Stat] 30% increased [BuffMagnitude|Magnitude] of [Poison] you inflict
        // [Stat] Recover 2% of maximum Life on Killing a [Poison|Poisoned] Enemy
        list.add(TalentNodeDef.of(
                "63431", 9022.31f, -1873.69f,
                "leeching_toxins", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.3),
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_ON_KILLING_A_POISONED_ENEMY, 0.02)
                ),
                List.of("61119")
        ));
        // [Stat] [Minion|Minions] have 20% additional [Physical] Damage Reduction
        // [Stat] [Minion|Minions] have +23% to [Chaos] [Resistances|Resistance]
        // [Stat] [Attack|Attacks] [Gain] 5% of Damage as extra [Chaos|Chaos] Damage
        list.add(TalentNodeDef.of(
                "19644", -4003.92f, -10617.2f,
                "left_hand_of_darkness", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION, 0.2),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_CHAOS_RESISTANCES, 0.23),
                        TalentNodeGen.stat(StatIds.ATTACKS_GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, 0.05)
                ),
                List.of("32258")
        ));
        // [Stat] 20% increased speed of [Recoup] Effects
        list.add(TalentNodeDef.of(
                "4091", -7355.04f, -5171.81f,
                "left_ventricle", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPEED_OF_RECOUP_EFFECTS, 0.2)
                ),
                List.of("38368", "38835")
        ));
        // [Stat] [Minion|Minions] have 15% increased maximum Life
        // [Stat] 5% increased Life and Mana Regeneration Rate for each [Minion] in your [Presence], up to a maximum of 40%
        list.add(TalentNodeDef.of(
                "55375", -6232.51f, -6126.75f,
                "licking_wounds", "passives_lifeandmana",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.15),
                        TalentNodeGen.stat(StatIds.INC_HEALTH_AND_MANA_REGENERATION_RATE_FOR_EACH_MINION_IN_YOUR_PRESENCE_UP_TO_A_MAX_OF, 0.05)
                ),
                List.of("23195", "38433", "40511")
        ));
        // [Stat] Recover 3% of maximum Life on Kill
        list.add(TalentNodeDef.of(
                "7344", 3224.82f, -1861.84f,
                "life_from_death", "passives_hiredkiller2",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_ON_KILL, 0.03)
                ),
                List.of("26931", "58182")
        ));
        // [Stat] [Minion|Minions] [Reviving|Revive] 35% faster if all your [Minion|Minions] are [Companion|Companions]
        list.add(TalentNodeDef.of(
                "31129", 12484.5f, 2765.08f,
                "lifelong_friend", "passives_companionsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_REVIVING_FASTER_IF_ALL_YOUR_MINIONS_ARE_COMPANIONS, 0.35)
                ),
                List.of("47443", "63618")
        ));
        // [Stat] 3% increased Movement Speed
        // [Stat] Immune to [Hinder]
        // [Stat] Immune to [Maim]
        list.add(TalentNodeDef.of(
                "55131", -2394.17f, 8932.47f,
                "light_on_your_feet", "passives_legstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.03),
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_HINDER, 1.0),
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_MAIM, 1.0)
                ),
                List.of("17045")
        ));
        // [Stat] 14% increased [Lightning] Damage
        // [Stat] 8% increased [Attack] and Cast Speed with [Lightning|Lightning] Skills
        list.add(TalentNodeDef.of(
                "13738", 2641.65f, -9309.5f,
                "lightning_quick", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.14),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED_WITH_LIGHTNING_SKILLS, 0.08)
                ),
                List.of("37372")
        ));
        // [Stat] 30% chance for [Lightning] Damage with [HitDamage|Hits] to be [Lucky]
        list.add(TalentNodeDef.of(
                "44566", 1353.57f, 9646.22f,
                "lightning_rod", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_LIGHTNING_DAMAGE_WITH_HIT_DAMAGE_TO_BE_LUCKY, 0.3)
                ),
                List.of("48846")
        ));
        // [Stat] [Gain] 6% of [Lightning] damage as Extra [Cold] damage
        // [Stat] Unaffected by [ShockedGround|Shocked Ground]
        list.add(TalentNodeDef.of(
                "60878", -211.585f, -7418.46f,
                "lightning_storm", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_LIGHTNING_DAMAGE_AS_EXTRA_COLD_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.UNAFFECTED_BY_SHOCKED_GROUND, 1.0)
                ),
                List.of("17044", "44405")
        ));
        // [Stat] 4% increased Movement Speed
        // [Stat] 14% increased [CooldownRecovery|Cooldown Recovery Rate]
        list.add(TalentNodeDef.of(
                "37888", -6329.73f, -1193.34f,
                "limitless_pursuit", "passives_movementspeedandevasion",
                List.of(),
                List.of("63772")
        ));
        // [Stat] 23% increased [Chaos] Damage
        // [Stat] 15% increased Skill Effect Duration
        list.add(TalentNodeDef.of(
                "56063", 2317.51f, -5438.63f,
                "lingering_horror", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.23),
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.15)
                ),
                List.of("12419")
        ));
        // [Stat] 40% increased [Curse] Duration
        // [Stat] 10% increased [Curse] [BuffMagnitude|Magnitudes]
        list.add(TalentNodeDef.of(
                "16499", 1075.8f, -9419.0f,
                "lingering_whispers", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_DURATION, 0.4),
                        TalentNodeGen.stat(StatIds.INC_CURSE_BUFF_MAGNITUDE, 0.1)
                ),
                List.of("36814")
        ));
        // [Stat] [Minion|Minions] have +22% to all Elemental [Resistances]
        // [Stat] [Minion|Minions] have +3% to all [MaximumResistances|Maximum Elemental Resistances]
        list.add(TalentNodeDef.of(
                "62887", 1965.28f, -9849.58f,
                "living_death", "passives_minionelementalresistancesnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_ALL_ELEMENTAL_RESISTANCES, 0.22),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_ALL_MAX_ELEMENTAL_RESISTANCES, 0.03)
                ),
                List.of("4271")
        ));
        // [Stat] 40% increased [Attack] Damage against Maimed Enemies
        // [Stat] Enemies are [Maim|Maimed] for 4 seconds after becoming [Pinned|Unpinned]
        list.add(TalentNodeDef.of(
                "31745", 2333.85f, 6156.5f,
                "lockdown", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_AGAINST_MAIMED_ENEMIES, 0.4),
                        TalentNodeGen.stat(StatIds.ENEMIES_ARE_MAIMED_FOR_SECONDS_AFTER_BECOMING_UNPINNED, 4.0)
                ),
                List.of("3234")
        ));
        // [Stat] 15% increased [Critical|Critical Hit Chance] for [Attack|Attacks]
        // [Stat] 15% increased [Accuracy] Rating
        list.add(TalentNodeDef.of(
                "56999", 2590.36f, 5169.65f,
                "locked_on", "passives_accuracydex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS, 0.15),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.15)
                ),
                List.of("30829")
        ));
        // [Stat] [Aura] Skills have 14% increased [BuffMagnitude|Magnitudes]
        // [Stat] Your [Aura] [Buff|Buffs] do not affect [Allies]
        list.add(TalentNodeDef.of(
                "12964", -6369.67f, -4773.94f,
                "lone_warrior", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE, 0.14),
                        TalentNodeGen.stat(StatIds.YOUR_AURA_BUFFS_DO_NOT_AFFECT_ALLIES)
                ),
                List.of("48264")
        ));
        // [Stat] 30% increased [Presence|Presence] Area of Effect
        // [Stat] [Minion|Minions] have 15% increased Area of Effect
        list.add(TalentNodeDef.of(
                "31826", 7837.15f, 5581.3f,
                "long_distance_relationship", "passives_companionsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.3),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_AREA_OF_EFFECT, 0.15)
                ),
                List.of("55664")
        ));
        // [Stat] 20% of [ElementalDamage|Elemental Damage] taken [Recoup|Recouped] as Life
        list.add(TalentNodeDef.of(
                "13542", 664.815f, -6148.06f,
                "loose_flesh", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ELEMENTAL_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.2)
                ),
                List.of("27658")
        ));
        // [Stat] 12% increased [Reservation] [Efficiency] of [Minion] Skills
        list.add(TalentNodeDef.of(
                "33240", -4341.09f, -11671.5f,
                "lord_of_horrors", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_MINION_SKILLS, 0.12)
                ),
                List.of("61842")
        ));
        // [Stat] 60% increased [BuffMagnitude|Magnitude] of [Poison] you inflict on targets that are not Poisoned
        list.add(TalentNodeDef.of(
                "42959", 7088.65f, 763.165f,
                "low_tolerance", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT_ON_TARGETS_THAT_ARE_NOT_POISONED, 0.6)
                ),
                List.of("32896", "59644")
        ));
        // [Stat] 8% of Damage is taken from Mana before Life
        // [Stat] +15 to [Intelligence]
        list.add(TalentNodeDef.of(
                "51891", 7124.22f, -6643.64f,
                "lucidity", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_IS_TAKEN_FROM_MANA_BEFORE_HEALTH, 0.08),
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 15.0)
                ),
                List.of("62936")
        ));
        // [Stat] 30% increased Damage while you have an active [Charm|Charm]
        // [Stat] 6% increased Movement Speed while you have an active [Charm|Charm]
        list.add(TalentNodeDef.of(
                "59303", 7200.38f, 9159.3f,
                "lucky_rabbit_foot", "passives_charmnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_YOU_HAVE_AN_ACTIVE_CHARM, 0.3),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_WHILE_YOU_HAVE_AN_ACTIVE_CHARM, 0.06)
                ),
                List.of("11094", "28823")
        ));
        // [Stat] 40% increased Mana Regeneration Rate while [Shapeshift|Shapeshifted]
        list.add(TalentNodeDef.of(
                "14432", -10437.9f, -4316.54f,
                "lunar_boon", "passives_druidshapeshiftwolfnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE_WHILE_SHAPESHIFTED, 0.4)
                ),
                List.of("55897")
        ));
        // [Stat] 10% chance when you gain a [Charges|Power Charge] to gain an additional [Charges|Power Charge]
        // [Stat] +1 to Maximum [Charges|Power Charges]
        list.add(TalentNodeDef.of(
                "1104", -5886.61f, -5253.73f,
                "lust_for_power", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_WHEN_YOU_GAIN_A_POWER_CHARGE_TO_GAIN_AN_ADDITIONAL_POWER_CHARGE, 0.1),
                        TalentNodeGen.stat(StatIds.TO_MAX_POWER_CHARGES, 1.0)
                ),
                List.of("56876")
        ));
        // [Stat] 50% increased [Minion] Damage while you have at least two different active [Offering|Offerings]
        list.add(TalentNodeDef.of(
                "27009", 702.015f, -9772.2f,
                "lust_for_sacrifice", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MINION_DAMAGE_WHILE_YOU_HAVE_AT_LEAST_TWO_DIFFERENT_ACTIVE_OFFERINGS, 0.5)
                ),
                List.of("43713")
        ));
        // [Stat] 30% increased [Armour]
        // [Stat] 5% of Physical Damage prevented [Recoup|Recouped] as Life
        list.add(TalentNodeDef.of(
                "44952", -9276.88f, 4722.91f,
                "made_to_last", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.3),
                        TalentNodeGen.stat(StatIds.OF_PHYSICAL_DAMAGE_PREVENTED_RECOUPED_AS_HEALTH, 0.05)
                ),
                List.of("6153", "9163")
        ));
        // [Stat] [Gain] 8% of [Physical|Physical] Damage as extra [Chaos|Chaos] Damage
        list.add(TalentNodeDef.of(
                "23738", 2590.88f, -7324.15f,
                "madness_in_the_bones", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, 0.08)
                ),
                List.of("34520")
        ));
        // [Stat] 25% increased [Attack] Damage
        // [Stat] [Attack|Attacks] have 25% chance to [Maim|Maim] on [HitDamage|Hit]
        list.add(TalentNodeDef.of(
                "41580", 8857.17f, 4221.87f,
                "maiming_strike", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.25),
                        TalentNodeGen.stat(StatIds.ATTACKS_HAVE_CHANCE_TO_MAIM_ON_HIT_DAMAGE, 0.25)
                ),
                List.of("13799", "25055")
        ));
        // [Stat] +20 to maximum Mana
        // [Stat] 20% increased Mana Regeneration Rate
        list.add(TalentNodeDef.of(
                "61027", 480.295f, -3752.19f,
                "mana_blessing", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_MANA, 20.0),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.2)
                ),
                List.of("22691", "517")
        ));
        // [Stat] 50% increased amount of Mana [ManaLeech|Leeched]
        // [Stat] 25% increased chance to inflict [Ailments] against [Rarity|Rare or Unique] Enemies
        list.add(TalentNodeDef.of(
                "37742", 9541.96f, -758.575f,
                "manifold_method", "passives_manaleechthemednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_MANA_LEECH, 0.5),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_INFLICT_AILMENTS_AGAINST_RARE_OR_UNIQUE_ENEMIES, 0.25)
                ),
                List.of("37548", "8246")
        ));
        // [Stat] 12% increased Movement Speed while Sprinting
        list.add(TalentNodeDef.of(
                "64050", 8280.06f, 715.955f,
                "marathon_runner", "passives_movementspeedandevasion",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_WHILE_SPRINTING, 0.12)
                ),
                List.of("43453")
        ));
        // [Stat] 60% increased Mana Cost [Efficiency] of [Mark|Marks]
        // [Stat] 4% increased Movement Speed if you've used a [Mark] [Recently]
        list.add(TalentNodeDef.of(
                "44756", 7778.15f, 8416.73f,
                "marked_agility", "passives_marknode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_EFFICIENCY_OF_MARKS, 0.6),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_IF_YOU_VE_USED_A_MARK_RECENTLY, 0.04)
                ),
                List.of("36927", "44841")
        ));
        // [Stat] [CullingStrike|Culling Strike] against Enemies you [Mark]
        list.add(TalentNodeDef.of(
                "36976", 8250.5f, 8416.73f,
                "marked_for_death", "passives_marknode",
                List.of(
                        TalentNodeGen.stat(StatIds.CULLING_STRIKE_AGAINST_ENEMIES_YOU_MARK, 1.0)
                ),
                List.of("28258")
        ));
        // [Stat] Enemies you [Mark] have 10% reduced [Accuracy|Accuracy Rating]
        // [Stat] Enemies you [Mark] take 10% increased Damage
        list.add(TalentNodeDef.of(
                "63830", 8250.5f, 7944.38f,
                "marked_for_sickness", "passives_marknode",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_YOU_MARK_HAVE_INC_ACCURACY, -0.1),
                        TalentNodeGen.stat(StatIds.ENEMIES_YOU_MARK_TAKE_INC_DAMAGE, 0.1)
                ),
                List.of("13624", "45390")
        ));
        // [Stat] 25% increased [Accuracy] Rating with [Quarterstaff|Quarterstaves]
        // [Stat] 25% increased [CriticalDamageBonus|Critical Damage Bonus] with [Quarterstaff|Quarterstaves]
        // [Stat] +25 to [Dexterity]
        list.add(TalentNodeDef.of(
                "2113", 10601.5f, -3193.04f,
                "martial_artistry", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WITH_QUARTERSTAVES, 0.25),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_QUARTERSTAVES, 0.25),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 25.0)
                ),
                List.of("326", "44516", "59694")
        ));
        return list;
    }
}
