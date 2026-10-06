package com.cimdy.coe.data.gen.gem.support;

import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision.Pierce;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.Exposure;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.Penetration;
import com.cimdy.coe.data.gen.lang.LangProvider;

public final class SupportLang {
    private SupportLang() {
    }

    public static void addAll(LangProvider provider) {
    addAllPart1(provider);
    addAllPart2(provider);
}

    private static void addAllPart1(LangProvider provider) {

        provider.addZh("gem.support.name.coe.spark_lightning_infusion", "闪电灌注");
        provider.addPair("gem.support.description.coe.spark_lightning_infusion", "Infuses the Skill with Lightning Damage.", "将闪电伤害注入技能。");
        provider.addZh("gem.support.name.coe.spark_high_voltage", "高压电");
        provider.addPair("gem.support.description.coe.spark_high_voltage", "Increases the magnitude of Ailments such as Shock.", "提升感电等异常状态的强度。");
        provider.addZh("gem.support.name.coe.frostbolt_cold_infusion", "冰霜灌注");
        provider.addPair("gem.support.description.coe.frostbolt_cold_infusion", "Infuses the Skill with Cold Damage.", "将冰霜伤害注入技能。");
        provider.addZh("gem.support.name.coe.fireball_fire_infusion", "火焰灌注");
        provider.addPair("gem.support.description.coe.fireball_fire_infusion", "Infuses the Skill with Fire Damage.", "将火焰伤害注入技能。");
        provider.addZh("gem.support.name.coe.blink_swift_displacement", "急速位移");
        provider.addPair("gem.support.description.coe.blink_swift_displacement", "Increases Blink's damage and movement speed.", "提升闪现的伤害与位移速度。");
        provider.addZh("gem.support.name.coe.fire_nova_flame_infusion", "烈焰灌注");
        provider.addPair("gem.support.description.coe.fire_nova_flame_infusion", "Infuses the Skill with Fire Damage.", "将火焰伤害注入技能。");
        provider.addZh("gem.support.name.coe.bone_spear_piercing", "穿刺强化");
        provider.addPair("gem.support.description.coe.bone_spear_piercing", "Adds projectiles and Pierce, and grants Physical damage.", "增加投射物数量与穿透，并附加物理伤害。");
        provider.addZh("gem.support.name.coe.volcanic_fissure_magma_infusion", "熔岩灌注");
        provider.addPair("gem.support.description.coe.volcanic_fissure_magma_infusion", "Infuses the Skill with Fire Damage.", "将火焰伤害注入技能。");
        provider.addZh("gem.support.name.coe.ice_shard_frost_infusion", "寒霜灌注");
        provider.addPair("gem.support.description.coe.ice_shard_frost_infusion", "Infuses the Skill with Cold Damage.", "将冰霜伤害注入技能。");
        provider.addZh("gem.support.name.coe.brutality", "残暴");
        provider.addZh("gem.support.name.coe.fire_infusion", "火焰灌注");
        provider.addZh("gem.support.name.coe.heavy_swing", "重击");
        provider.addZh("gem.support.name.coe.fork", "分叉");
        provider.addZh("gem.support.name.coe.chain", "连锁");
        provider.addZh("gem.support.name.coe.culling_strike", "处决打击");
        // 元宝石（Cast on X，P2-3）
        provider.addZh("gem.support.name.coe.cast_on_crit", "暴击时施放");
        provider.addPair("gem.support.description.coe.cast_on_crit", "Triggers a linked Spell (default: Chaos Bolt) when you deal a Critical Hit. 0.5s internal cooldown; at most 1 trigger per attack.", "你暴击时触发一道被链接的法术（默认：混沌箭）。触发有 0.5 秒内部冷却，每次攻击最多触发 1 次。");
        provider.addZh("gem.support.name.coe.cast_on_ignite", "点燃时施放");
        provider.addPair("gem.support.description.coe.cast_on_ignite", "Triggers a linked Spell (default: Shockwave Totem) when your Skill Ignites an enemy. 0.5s internal cooldown.", "你的技能使敌人点燃时触发一道被链接的法术（默认：震波图腾）。触发有 0.5 秒内部冷却。");
        provider.addZh("gem.support.name.coe.cast_on_shock", "感电时施放");
        provider.addPair("gem.support.description.coe.cast_on_shock", "Triggers a linked Spell (default: Raise Zombie) when your Skill Shocks an enemy. 0.5s internal cooldown.", "你的技能使敌人感电时触发一道被链接的法术（默认：唤起僵尸）。触发有 0.5 秒内部冷却。");
        // <<<AUTO_SUPPORTS_START>>>
        provider.add("gem.support.name.coe.adhesive_grenades", "Adhesive Grenades");
        provider.add("gem.support.description.coe.adhesive_grenades", "Mana: 100%; Supported Grenade Skills deal 10% less Damage; Supported Grenade Skills stop where they initially land");
        provider.add("gem.support.name.coe.alignment", "Alignment");
        provider.add("gem.support.description.coe.alignment", "Mana: 120%; Supported Skills deal 30% more Projectile Damage when Attacking in indicated direction; Supported Skills indicate one of four directions Indicated direction changes when an Attack with Supported Skill matches indicated direction");
        provider.add("gem.support.name.coe.ambush", "Ambush");
        provider.add("gem.support.description.coe.ambush", "Mana: 100%; Supported Skill has 100% more Critical Hit Chance against Enemies that are on Full Life");
        provider.add("gem.support.name.coe.astral_projection", "Astral Projection");
        provider.add("gem.support.description.coe.astral_projection", "Mana: 100%; Supported Skills have 25% less Area of Effect; Supported Skills Cast at the targeted location instead of around you");
        provider.add("gem.support.name.coe.battershout", "Battershout");
        provider.add("gem.support.description.coe.battershout", "Mana: 130%; Supported Skills cannot Break Armor; Supported Skills Consume Fully Broken Armor on Enemies within range to Trigger Battershout; Deals Physical Damage equal to 40% Broken Armor of the target Triggered from Does not take into account Overbroken Armor; Explosion radius is 2 metres; +5% to Critical Hit Chance; 50% more damage against Unique enemies; Cannot Break Armor");
        provider.add("gem.support.name.coe.blindside", "Blindside");
        provider.add("gem.support.description.coe.blindside", "Mana: 120%; Supported Skills have 15% more Critical Damage Bonus with Hits against Blinded Enemies; Supported Skills have 15% more chance to Critically Hit Blinded Enemies; Supported Skills cannot inflict Blind");
        provider.add("gem.support.name.coe.branching_fissures", "Branching Fissures");
        provider.add("gem.support.description.coe.branching_fissures", "Mana: 120%; Supported Skills' Fissures branch 2 times; Supported Skills' Branched Fissures have 35% less area of effect; Supported Skills' Branched Fissures deal 40% less damage");
        provider.add("gem.support.name.coe.bursting_plague", "Bursting Plague");
        provider.add("gem.support.description.coe.bursting_plague", "Mana: 120%; Poison from Supported Skills also adds 100% of Damage per Second to Enemy as Plague, up to a max of 20% of Enemy's max Life; Burst radius is 1.8 metres; Has up to 100% more Area of Effect at max Plague; Deals Physical Damage equal to 100% of Plague on Triggering Enemy; +5% to Critical Hit Chance; This Skill is Triggered when an Enemy Dies with Plague on it");
        provider.add("gem.support.name.coe.cirels_cultivation", "Cirels Cultivation");
        provider.add("gem.support.description.coe.cirels_cultivation", "Mana: 115%; Counts allied Totems in radius of 4 metres; Supported Totems can gain benefits for having up to 5 Totems in effect radius; Totems created by supported Skills regenerate 1% of max Life per second per allied Totem in effect radius; Totems created by Supported Skills deal 10% more damage per allied Totem in effect radius; Totems created by Supported Skills have 5% increased area of effect per allied Totem in effect radius; Supported Skills can only have one active Totem");
        provider.add("gem.support.name.coe.clash", "Clash");
        provider.add("gem.support.description.coe.clash", "Mana: 120%; Supported Skills deal 40% more Melee Damage against Enemies with a higher percentage of their Life remaining than you, but deal 20% less Melee Damage against Enemies with a lower percentage of their Life remaining than you");
        provider.add("gem.support.name.coe.close_combat", "Close Combat");
        provider.add("gem.support.description.coe.close_combat", "Mana: 120%; Supported Skills deal 20% more Attack Damage to  enemies within 1 metre of you, scaling down to no bonus at 3.5 metres or further");
        provider.add("gem.support.name.coe.concentrated_area", "Concentrated Area");
        provider.add("gem.support.description.coe.concentrated_area", "Mana: 100%; Supported Skills have 50% less Area of Effect; Supported Skills deal 30% more Area Damage");
        provider.add("gem.support.name.coe.corrupting_cry", "Corrupting Cry");
        provider.add("gem.support.description.coe.corrupting_cry", "Supported Warcries have 30% less Area of Effect; Supported Skills Trigger Corrupted Cry for each enemy in range; Corrupted Blood duration is 5 seconds");
        provider.add("gem.support.name.coe.deadly_poison", "Deadly Poison");
        provider.add("gem.support.description.coe.deadly_poison", "Mana: 100%; Supported Skills deal 25% less Damage with Hits; 75% more Magnitude of Poison inflicted with Supported Skills");
        provider.add("gem.support.name.coe.deep_cuts", "Deep Cuts");
        provider.add("gem.support.description.coe.deep_cuts", "Mana: 100%; Supported Skills deal 25% less Damage with Hits; 75% more Magnitude of Bleeding inflicted with Supported Skills");
        provider.add("gem.support.name.coe.echoing_cry", "Echoing Cry");
        provider.add("gem.support.description.coe.echoing_cry", "Mana: 150%; Supported Skills Echo 2 times, with a 1.3 second delay; Supported Skills have 50% less Area of Effect; Supported Skills have 100% more area of effect per time they have Echoed; Supported Skills become usable after you have moved 10 metres Supported Skills become unusable again after use; Supported Skills count enemies as having 50% less Power; Supported Skills deal 40% less Damage");
        provider.add("gem.support.name.coe.elemental_focus", "Elemental Focus");
        provider.add("gem.support.description.coe.elemental_focus", "Mana: 100%; Supported Skills deal 25% more Elemental Damage; Supported Skills cannot inflict Elemental Ailments");
        provider.add("gem.support.name.coe.encroaching_ground", "Encroaching Ground");
        provider.add("gem.support.description.coe.encroaching_ground", "Mana: 110%; Ground Surfaces created by Supported Skills gain 20% increased Area of Effect per second, up to a max of 100%");
        provider.add("gem.support.name.coe.expand", "Expand");
        provider.add("gem.support.description.coe.expand", "Mana: 120%; Supported Skills have a max of 3 Seals; Supported Spells gain a Seal every 200% of cast time; Supported Skills get 30% increased area of effect per Seal broken");
        provider.add("gem.support.name.coe.expanse", "Expanse");
        provider.add("gem.support.description.coe.expanse", "Mana: 100%; CooldownTime: 8000; Supported Skills have a Cooldown of 8 seconds; Supported Skills have 50% increased Area of Effect");
        provider.add("gem.support.name.coe.fortress", "Fortress");
        provider.add("gem.support.description.coe.fortress", "Mana: 100%; Supported Skills deal 40% less damage with Hits; Walls from Supported Skills are created in a Circle instead");
    }

    private static void addAllPart2(LangProvider provider) {

        provider.add("gem.support.name.coe.freezefork", "Freezefork");
        provider.add("gem.support.description.coe.freezefork", "Mana: 100%; Supported Skills Fork when hitting Frozen Enemies; Supported Skills cannot cause Freeze buildup");
        provider.add("gem.support.name.coe.gambleshot", "Gambleshot");
        provider.add("gem.support.description.coe.gambleshot", "Mana: 100%; Projectiles from Supported Skills deal 15% less Damage; Projectiles fired by Supported Skills Fork, Chain an additional time, or Pierce an additional time at random");
        provider.add("gem.support.name.coe.garukhans_resolve", "Garukhans Resolve");
        provider.add("gem.support.description.coe.garukhans_resolve", "Mana: 100%; Max Critical Hit chance with Supported Skills is 50%; Attack Critical Hit Chance with Supported Skills Bifurcates");
        provider.add("gem.support.name.coe.inexorable_critical", "Inexorable Critical");
        provider.add("gem.support.description.coe.inexorable_critical", "Mana: 120%; Supported Skills gain 10% increased Critical Hit Chance each second, up to 100%. This Critical Hit Chance is lost whenever a skill supported by Inexorable Critical Critically Hits");
        provider.add("gem.support.name.coe.ixchels_torment", "Ixchels Torment");
        provider.add("gem.support.description.coe.ixchels_torment", "Mana: 120%; Skills Triggered by Supported Skills recover between % and 20% of cost on use; Skills Triggered by Supported Skills deal between 40% less and 60% more Damage; Skills Triggered by Supported Skills have between 40% less and 50% more Area of Effect; Skills Triggered by Supported Skills have between 40% less and 50% more Projectile Speed");
        provider.add("gem.support.name.coe.kaoms_madness", "Kaoms Madness");
        provider.add("gem.support.description.coe.kaoms_madness", "Mana: 100%; Supported Skills create 3 additional fissures; Supported Skills deal 20% less Damage; Supported Skills have 20% less Skill Speed; Supported Skills have 30% less Area of Effect");
        provider.add("gem.support.name.coe.longshot", "Longshot");
        provider.add("gem.support.description.coe.longshot", "Mana: 120%; Supported Skills deal 25% more Attack Damage to  enemies further than 7 metres from you, scaling down to no bonus at 3.5 metres or closer");
        provider.add("gem.support.name.coe.magnified_area", "Magnified Area");
        provider.add("gem.support.description.coe.magnified_area", "Mana: 130%; Supported Skills have 35% increased Area of Effect");
        provider.add("gem.support.name.coe.morganas_tempest", "Morganas Tempest");
        provider.add("gem.support.description.coe.morganas_tempest", "Mana: 100%; Supported Skills have 60% more Storm travel speed; Supported Skills have 25% less area of effect; Supported Skills have 35% less damage; Supported Storm Skills have +1 to Limit; Supported Skills originate from the player and move towards the target location");
        provider.add("gem.support.name.coe.multishot", "Multishot");
        provider.add("gem.support.description.coe.multishot", "Mana: 100%; Supported Skills fire 2 additional Projectiles; Supported Skills deal 35% less Damage; Supported Skills have 20% less Skill Speed");
        provider.add("gem.support.name.coe.murderous_intent", "Murderous Intent");
        provider.add("gem.support.description.coe.murderous_intent", "Mana: 120%; Skills Empowered by Supported Skills have Culling Strike");
        provider.add("gem.support.name.coe.overextend", "Overextend");
        provider.add("gem.support.description.coe.overextend", "Mana: 115%; Supported Skills have 30% more Critical Damage Bonus; You are Dazed for 5 seconds on Critically Striking with Supported Skill");
        provider.add("gem.support.name.coe.overreach", "Overreach");
        provider.add("gem.support.description.coe.overreach", "Mana: 100%; Supported Skills have 40% increased Area of Effect; Supported Skills have 80% less Accuracy Rating against Enemies within two metres");
        provider.add("gem.support.name.coe.pierce", "Pierce");
        provider.add("gem.support.description.coe.pierce", "Mana: 100%; Projectiles from Supported Skills Pierce an Enemy; Projectiles from Supported Skills deal 20% less Damage if they've Pierced an enemy");
        provider.add("gem.support.name.coe.pinpoint_critical", "Pinpoint Critical");
        provider.add("gem.support.description.coe.pinpoint_critical", "Mana: 100%; Supported Skills have 60% more Critical Hit Chance; Supported Skills have 30% less Critical Damage Bonus");
        provider.add("gem.support.name.coe.profanity", "Profanity");
        provider.add("gem.support.description.coe.profanity", "Mana: 100%; Supported Skills deal 30% more Chaos Damage if consuming a Corpse; Supported Skills consume a Corpse within 5 metres on use, if possible");
        provider.add("gem.support.name.coe.pursuit", "Pursuit");
        provider.add("gem.support.description.coe.pursuit", "Mana: 120%; Supported Skills deal 20% more Melee Damage if you have Damaged an Enemy with a Projectile Attack Hit in the last eight seconds; Supported Skills deal no Projectile damage");
        provider.add("gem.support.name.coe.retreat", "Retreat");
        provider.add("gem.support.description.coe.retreat", "Mana: 120%; Supported Skills deal 20% more Projectile Damage if you have Damaged an Enemy with a Melee Hit in the last eight seconds; Supported Skills deal no Melee damage");
        provider.add("gem.support.name.coe.rigwalds_ferocity", "Rigwalds Ferocity");
        provider.add("gem.support.description.coe.rigwalds_ferocity", "Mana: 100%; Supported Skills have 30% increased Attack Speed if used in Weapon Set I; Supported Skills deal 15% less Damage if used in Weapon Set I; Supported Skills have 10% reduced Attack Speed if used in Weapon Set II; Supported Skills deal 30% more Damage if used in Weapon Set II");
        provider.add("gem.support.name.coe.rising_tempest", "Rising Tempest");
        provider.add("gem.support.description.coe.rising_tempest", "Mana: 120%; Supported Skills deal 12% more Elemental Damage for each Skill used Recently of a different Elemental type");
        provider.add("gem.support.name.coe.ritualistic_curse", "Ritualistic Curse");
        provider.add("gem.support.description.coe.ritualistic_curse", "Mana: 100%; Curse zones from Supported Skills erupt after 30% more delay; Supported Skills have 50% increased Area of Effect");
        provider.add("gem.support.name.coe.searing_flame", "Searing Flame");
        provider.add("gem.support.description.coe.searing_flame", "Mana: 100%; Supported Skills deal 25% less Damage with Hits; 75% more Magnitude of Ignite inflicted with Supported Skills");
        provider.add("gem.support.name.coe.spectral_volley", "Spectral Volley");
        provider.add("gem.support.description.coe.spectral_volley", "Mana: 120%; Supported Skills deal 20% more Damage when fired from Spectral Projectiles; Supported Skills deal 60% less Damage when not fired from Spectral Projectiles; Spectral Projectiles will fire if no Spectral Projectiles have been created in the last 3 seconds; Max 5 Spectral Projectiles; Attacks with Supported Skills create a Spectral Projectile");
        provider.add("gem.support.name.coe.spell_cascade", "Spell Cascade");
        provider.add("gem.support.description.coe.spell_cascade", "Mana: 100%; Supported Skills Cascade to either side of the targeted Area along a random axis; Cascades from Supported Skills have 20% less Area of Effect; Supported Skills have 20% less Area of Effect; Supported Skills deal 30% less Damage");
        provider.add("gem.support.name.coe.spell_echo", "Spell Echo");
        provider.add("gem.support.description.coe.spell_echo", "Mana: 150%; Supported Skills have 30% less Cast Speed; Supported Spells Echo 1 times; Echoes from Supported Spells have 40% increased Area of Effect");
        provider.add("gem.support.name.coe.splinter_totem", "Splinter Totem");
        provider.add("gem.support.description.coe.splinter_totem", "Mana: 120%; Totems created by Supported Skills can splinter up to 2 times; Totems created by Supported Skills splinter after performing 1 action; Totems created by Supported Skills have 10% less Skill Speed for each time they have splintered; Totems created by Supported Skills deal 20% less damage for each time they have splintered; Totems created by Supported Skills have 20% less max Life for each time they have splintered; Totems created by Supported Skills have 20% less area of effect for each time they have splintered; Supported Skills can only have one active Totem");
        provider.add("gem.support.name.coe.streamlined_rounds", "Streamlined Rounds");
        provider.add("gem.support.description.coe.streamlined_rounds", "Mana: 120%; Supported Skills load 5 additional bolts; Supported Skills have 50% more Attack speed; Supported Skills deal 60% less Damage; Supported Skills have 50% less reload speed");
        provider.add("gem.support.name.coe.supercritical", "Supercritical");
        provider.add("gem.support.description.coe.supercritical", "Mana: 100%; Supported Skills have 100% increased Critical Damage Bonus; Supported Skills have 20% less Critical Hit chance");
        provider.add("gem.support.name.coe.tectonic_slams", "Tectonic Slams");
        provider.add("gem.support.description.coe.tectonic_slams", "Mana: 130%; Supported Skills cause 1 additional Aftershocks ahead and to each side of the initial area; Supported Skills deal 45% less damage with Aftershocks; Supported Skills have 30% less Aftershock area of effect; Supported Skills have 15% less Attack Speed");
        provider.add("gem.support.name.coe.unleash", "Unleash");
        provider.add("gem.support.description.coe.unleash", "Mana: 150%; Supported Skills deal 50% less Damage when Repeating; Supported Spells gain a Seal every 200% of cast time; Supported Skills have a max of 2 Seals; Supported Skills Repeat 1 time per Seal broken");
        provider.add("gem.support.name.coe.upheaval", "Upheaval");
        provider.add("gem.support.description.coe.upheaval", "Mana: 100%; Supported Skills create 1 additional fissures; Supported Skills deal 20% less Damage; Supported Skills have 20% less Skill Speed");
        provider.add("gem.support.name.coe.vruuns_inevitability", "Vruuns Inevitability");
        provider.add("gem.support.description.coe.vruuns_inevitability", "Mana: 100%; Supported Skills have 6% increased area of effect per Combo expended; Supported Skills lose Combo if they generate no Combo for 6 seconds; Supported Skills can gain up to 10 Combo Supported Skills expend all Combo on use");
        provider.add("gem.support.name.coe.withering_touch", "Withering Touch");
        provider.add("gem.support.description.coe.withering_touch", "Mana: 100%; Supported Skills deal 25% less Damage; Withered lasts 4 seconds; Supported Skills have 1% chance to inflict Withered on Hit for every 0.57142857142857% of enemy Ailment Threshold dealt as Chaos damage; Chance to inflict Withered on Hit with Supported Skills can Surpass 100%");
        provider.add("gem.support.name.coe.zarokhs_refrain", "Zarokhs Refrain");
        provider.add("gem.support.description.coe.zarokhs_refrain", "Mana: 130%; CooldownTime: 5000; Supported Spells Echo 3 times; Echoes from Supported Spells have 40% increased Area of Effect");
        provider.add("gem.support.name.coe.zarokhs_revolt", "Zarokhs Revolt");
        provider.add("gem.support.description.coe.zarokhs_revolt", "Mana: 200%; CooldownTime: 10000; Supported Skills deal 20% less Damage when Repeating; Supported Spells gain a Seal every 50% of cast time; Supported Skills have a max of 5 Seals; Supported Skills Repeat 1 time per Seal broken");
        provider.add("gem.support.name.coe.zerphis_infamy", "Zerphis Infamy");
        provider.add("gem.support.description.coe.zerphis_infamy", "Mana: 100%; Supported Skills requires 50 Glory to use; Supported Skills gain Glory equal to 5% of  Life spent on other Skills; Supported Skills have 50% increased Area of Effect; Supported Skills deal 50% more Damage");
        // ── 权威映射（design/refactor/skill-data/crawled/all_skills.json + name_resolution.md）──
        // 本地旧名 unchanged（本轮不重命名），仅按权威 name_en/name_zh 补 lang 文案。
        // added_*_damage → *Attunement；*_exposure → 对应元素 Exposure；arbiter_ignition → arbiter's ignition。
        provider.addZh("gem.support.name.coe.added_chaos_damage", "混沌同調（Chaos Attunement）");
        provider.add("gem.support.description.coe.added_chaos_damage", "Supports any skill that deals damage, causing them to Gain Chaos Damage but deal less Damage of other Types.");
        provider.addZh("gem.support.name.coe.added_cold_damage", "冰冷同調（Cold Attunement）");
        provider.add("gem.support.description.coe.added_cold_damage", "Supports any skill that deals damage, causing them to Gain Cold Damage but deal less Fire and Lightning Damage.");
        provider.addZh("gem.support.name.coe.added_fire_damage", "火焰同調（Fire Attunement）");
        provider.add("gem.support.description.coe.added_fire_damage", "Supports any skill that deals damage, causing them to Gain Fire Damage but deal less Cold and Lightning Damage.");
        provider.addZh("gem.support.name.coe.added_lightning_damage", "閃電同調（Lightning Attunement）");
        provider.add("gem.support.description.coe.added_lightning_damage", "Supports any skill that deals damage, causing them to Gain Lightning Damage but deal less Cold and Fire Damage.");
        provider.addZh("gem.support.name.coe.arbiter_ignition", "仲裁者的燃灼（Arbiter's Ignition）");
        provider.add("gem.support.description.coe.arbiter_ignition", "Supports Fire Spell Skills causing you to gain Elemental Archon when Igniting with them.");
        provider.addZh("gem.support.name.coe.biting_exposure", "冰冷曝曬（Cold Exposure）");
        provider.add("gem.support.description.coe.biting_exposure", "Supports any skill that Hits enemies, causing it to inflict Exposure when it inflicts Freeze.");
        provider.addZh("gem.support.name.coe.conductive_exposure", "閃電曝曬（Lightning Exposure）");
        provider.add("gem.support.description.coe.conductive_exposure", "Supports any skill that Hits enemies, causing it to inflict Exposure when inflicting Shock.");
        provider.addZh("gem.support.name.coe.scorching_exposure", "火焰曝曬（Fire Exposure）");
        provider.add("gem.support.description.coe.scorching_exposure", "Supports any skill that Hits enemies, causing it to inflict Exposure when Igniting.");
        // 三个穿透辅助同样缺 lang（此前误以为已有键）：权威 all_skills.json 直接命中。
        provider.addZh("gem.support.name.coe.cold_penetration", "冰冷穿透（Cold Penetration）");
        provider.add("gem.support.description.coe.cold_penetration", "Supports any skill that Hits enemies, making those Hits Penetrate enemy Cold resistance.");
        provider.addZh("gem.support.name.coe.fire_penetration", "火焰穿透（Fire Penetration）");
        provider.add("gem.support.description.coe.fire_penetration", "Supports any skill that Hits enemies, making those Hits Penetrate enemy Fire resistance.");
        provider.addZh("gem.support.name.coe.lightning_penetration", "閃電穿透（Lightning Penetration）");
        provider.add("gem.support.description.coe.lightning_penetration", "Supports any skill that Hits enemies, making those Hits Penetrate enemy Lightning resistance.");
        // martial_projectiles：poe2db 权威数据中无对应条目（name_resolution.md 列为「需人工裁定」），
        // 按本地 id 写英文原名，不臆造中文名与描述。
        provider.add("gem.support.name.coe.martial_projectiles", "Martial Projectiles");
        // <<<AUTO_SUPPORTS_END>>>
    
    }

}
