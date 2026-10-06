package com.cimdy.coe.data.gen.equipment;

import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.addition.ModAdditions;
import com.cimdy.coe.data.codec.item.addition.impl.GlobalAddition;
import com.cimdy.coe.data.codec.item.addition.impl.LocalAddition;
import com.cimdy.coe.data.codec.item.addition.impl.MultiAddition;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import com.cimdy.coe.data.codec.item.mod.value.ModPreset;
import com.cimdy.coe.data.codec.item.mod.value.ModValue;

import com.cimdy.coe.data.codec.item.equipment.EquipmentModPreset;
import com.cimdy.coe.data.gen.equipment.EquipmentGen;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import com.cimdy.coe.data.codec.item.ItemInstance;
import java.util.List;
import java.util.Map;

/**
 * 显式词缀数据（离线烘焙）：每条词缀 = 一条字面量建造者调用，无运行时解析。
 * <b>去重 + 分层</b>：以「家族 + 前后缀 + 数值签名」分区；数值一致的槽位合并为一条大类层 {@code ModPreset}（可挂 {@code Local} 条件、用 {@code types} 收窄），
 * 数值不同的槽位各自单独生成以保留数值；分区按槽位互斥，同一物品不会重复命中同一词缀。
 *
 * <p><b>⚠️ 已知「死条件」：{@code ARMOR_GROUP_MOD_*} 中含 {@code SHIELD / BUCKLER / FOCI} 的 {@code types} 收窄条目当前一律不可达。</b>
 * 类型树把 {@code shield / buckler / foci} 挂在 {@code weapon/off_hand} 之下，故它们的 {@code types()} 里<b>不含 {@code armor}</b>，
 * 而 {@code ModifierPipeline.matchesScope} 只查池 namespace —— 组层池 namespace 为 {@code armor}，对这些副手永远命中不了，
 * {@code types} 里写 {@code SHIELD / BUCKLER / FOCI} 也就成了无效收窄（表现为「看起来已注册、实际永不生效」）。
 * <b>副手的对应词缀已由 {@link #s6} 在槽位层补齐，勿再依赖本处的组层条目。</b>
 * （#242 裁决：不从 {@code types} 列表摘掉这些 token —— 摘掉会让条目退化成无条件命中，反而污染胸甲 / 头 / 腿 / 靴。）
 */
public final class ExplicitModGen {

    // ===================== 大类层（数值一致的多槽位合并） =====================

    private static void g0(BootstrapContext<ItemInstance> bootstrap) {
        EquipmentGen.registerMod(bootstrap, EquipmentGen.ACCESSORY_GROUP_MOD_PREFIX, "ItemFoundRarityIncreasePrefix",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.AMULET), EquipmentGen.id(EquipmentGen.RING)), ModPreset.builder(ModAdditions.FOUND_ITEM_RARITY_INC)
                .withTier("magpie_s", ModValue.of(8), 10, 1000)
                .withTier("collector_s", ModValue.of(12, 15), 29, 1000)
                .withTier("hoarder_s", ModValue.of(16, 19), 47, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ACCESSORY_GROUP_MOD_PREFIX, "IncreasedAccuracy",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.AMULET), EquipmentGen.id(EquipmentGen.RING)), ModPreset.builder(ModAdditions.ADD_ACCURACY)
                .withTier("precise", ModValue.of(11), 1, 800)
                .withTier("reliable", ModValue.of(33, 60), 11, 800)
                .withTier("focused", ModValue.of(61, 84), 18, 800)
                .withTier("deliberate", ModValue.of(85, 123), 26, 800)
                .withTier("consistent", ModValue.of(124, 167), 36, 800)
                .withTier("steady", ModValue.of(168, 236), 48, 800)
                .withTier("hunter_s", ModValue.of(237, 346), 58, 800)
                .withTier("ranger_s", ModValue.of(347, 450), 67, 400)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "IncreasedAccuracyBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.CROSS_BOW)), ModPreset.builder(ModAdditions.ADD_ACCURACY)
                .withTier("precise", ModValue.of(11), 8, 800)
                .withTier("reliable", ModValue.of(33, 60), 13, 800)
                .withTier("focused", ModValue.of(61, 84), 18, 800)
                .withTier("deliberate", ModValue.of(85, 123), 26, 800)
                .withTier("consistent", ModValue.of(124, 167), 36, 800)
                .withTier("steady", ModValue.of(168, 236), 48, 600)
                .withTier("hunter_s", ModValue.of(237, 346), 58, 600)
                .withTier("ranger_s", ModValue.of(347, 450), 67, 300)
                .withTier("amazon_s", ModValue.of(451, 550), 76, 200)
                .withTier("valkyrie_s", ModValue.of(551, 650), 82, 100)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "IncreasedAccuracyClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(ModAdditions.ADD_ACCURACY)
                .withTier("precise", ModValue.of(11), 8, 1)
                .withTier("reliable", ModValue.of(33, 60), 13, 1)
                .withTier("focused", ModValue.of(61, 84), 18, 1)
                .withTier("deliberate", ModValue.of(85, 123), 26, 1)
                .withTier("consistent", ModValue.of(124, 167), 36, 1)
                .withTier("steady", ModValue.of(168, 236), 48, 1)
                .withTier("hunter_s", ModValue.of(237, 346), 58, 1)
                .withTier("ranger_s", ModValue.of(347, 450), 67, 1)
                .withTier("amazon_s", ModValue.of(451, 550), 76, 1)
                .build()));

        // ⚠️ 凡 ARMOR_GROUP_MOD_* 中 types 列表含 SHIELD / BUCKLER / FOCI 的条目，对副手一律不可达
        //    （副手挂在 weapon/off_hand，types() 不含 armor）。副手侧已由 s6() 在槽位层补齐，勿再依赖本处。
        //    详见类注释「已知死条件」。此处勿摘掉那些 token：摘掉会退化成无条件命中，污染胸甲/头/腿/靴。
        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_PREFIX, "IncreasedAccuracy",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.HEAD), EquipmentGen.id(EquipmentGen.LEGS)), ModPreset.builder(ModAdditions.ADD_ACCURACY)
                .withTier("precise", ModValue.of(11), 1, 800)
                .withTier("reliable", ModValue.of(33, 60), 11, 800)
                .withTier("focused", ModValue.of(61, 84), 18, 800)
                .withTier("deliberate", ModValue.of(85, 123), 26, 800)
                .withTier("consistent", ModValue.of(124, 167), 36, 800)
                .withTier("steady", ModValue.of(168, 236), 48, 800)
                .withTier("hunter_s", ModValue.of(237, 346), 58, 800)
                .withTier("ranger_s", ModValue.of(347, 450), 67, 400)
                .withTier("amazon_s", ModValue.of(451, 550), 76, 200)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "IncreasedAccuracyOneHandMace",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(ModAdditions.ADD_ACCURACY)
                .withTier("precise", ModValue.of(11), 8, 800)
                .withTier("reliable", ModValue.of(33, 60), 13, 800)
                .withTier("focused", ModValue.of(61, 84), 18, 800)
                .withTier("deliberate", ModValue.of(85, 123), 26, 800)
                .withTier("consistent", ModValue.of(124, 167), 36, 800)
                .withTier("steady", ModValue.of(168, 236), 48, 600)
                .withTier("hunter_s", ModValue.of(237, 346), 58, 600)
                .withTier("ranger_s", ModValue.of(347, 450), 67, 300)
                .withTier("amazon_s", ModValue.of(451, 550), 76, 200)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_PREFIX, "IncreasedLifeFeet",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.FEET), EquipmentGen.id(EquipmentGen.LEGS)), ModPreset.builder(ModAdditions.ADD_MAX_HEALTH)
                .withTier("hale", ModValue.of(10), 1, 1000)
                .withTier("healthy", ModValue.of(20, 29), 6, 1000)
                .withTier("sanguine", ModValue.of(30, 39), 16, 1000)
                .withTier("stalwart", ModValue.of(40, 59), 24, 1000)
                .withTier("stout", ModValue.of(60, 69), 33, 1000)
                .withTier("robust", ModValue.of(70, 84), 38, 1000)
                .withTier("rotund", ModValue.of(85, 99), 46, 1000)
                .withTier("virile", ModValue.of(100, 119), 54, 1000)
                .withTier("athlete_s", ModValue.of(120, 149), 60, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_PREFIX, "IncreasedLifeBuckler",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BUCKLER), EquipmentGen.id(EquipmentGen.SHIELD)), ModPreset.builder(ModAdditions.ADD_MAX_HEALTH)
                .withTier("hale", ModValue.of(10), 1, 1000)
                .withTier("healthy", ModValue.of(20, 29), 6, 1000)
                .withTier("sanguine", ModValue.of(30, 39), 16, 1000)
                .withTier("stalwart", ModValue.of(40, 59), 24, 1000)
                .withTier("stout", ModValue.of(60, 69), 33, 1000)
                .withTier("robust", ModValue.of(70, 84), 38, 1000)
                .withTier("rotund", ModValue.of(85, 99), 46, 1000)
                .withTier("virile", ModValue.of(100, 119), 54, 1000)
                .withTier("athlete_s", ModValue.of(120, 149), 60, 1000)
                .withTier("fecund", ModValue.of(150, 174), 65, 1000)
                .withTier("vigorous", ModValue.of(175, 189), 70, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_PREFIX, "IncreasedMana",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.FEET), EquipmentGen.id(EquipmentGen.LEGS)), ModPreset.builder(ModAdditions.ADD_MAX_MANA)
                .withTier("beryl", ModValue.of(10), 1, 1000)
                .withTier("cobalt", ModValue.of(15, 24), 6, 1000)
                .withTier("azure", ModValue.of(25, 34), 16, 1000)
                .withTier("teal", ModValue.of(35, 54), 25, 1000)
                .withTier("cerulean", ModValue.of(55, 64), 33, 1000)
                .withTier("aqua", ModValue.of(65, 79), 38, 1000)
                .withTier("opalescent", ModValue.of(80, 89), 46, 1000)
                .withTier("gentian", ModValue.of(90, 104), 54, 1000)
                .withTier("chalybeous", ModValue.of(105, 124), 60, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "IncreasedMana",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.SCEPTRE), EquipmentGen.id(EquipmentGen.WAND)), ModPreset.builder(ModAdditions.ADD_MAX_MANA)
                .withTier("beryl", ModValue.of(10), 1, 1000)
                .withTier("cobalt", ModValue.of(15, 24), 6, 1000)
                .withTier("azure", ModValue.of(25, 34), 16, 1000)
                .withTier("teal", ModValue.of(35, 54), 25, 1000)
                .withTier("cerulean", ModValue.of(55, 64), 33, 1000)
                .withTier("aqua", ModValue.of(65, 79), 38, 1000)
                .withTier("opalescent", ModValue.of(80, 89), 46, 1000)
                .withTier("gentian", ModValue.of(90, 104), 54, 1000)
                .withTier("chalybeous", ModValue.of(105, 124), 60, 1000)
                .withTier("mazarine", ModValue.of(125, 149), 65, 1000)
                .withTier("blue", ModValue.of(150, 164), 70, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "LifeRegeneration",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.FEET), EquipmentGen.id(EquipmentGen.HEAD)), ModPreset.builder(ModAdditions.ADD_HEALTH_REGENERATION_PER_SECOND)
                .withTier("of_the_newt", ModValue.of(1), 1, 1000)
                .withTier("of_the_lizard", ModValue.of(2.1, 3), 5, 1000)
                .withTier("of_the_flatworm", ModValue.of(3.1, 4), 11, 1000)
                .withTier("of_the_starfish", ModValue.of(4.1, 6), 17, 1000)
                .withTier("of_the_hydra", ModValue.of(6.1, 9), 26, 1000)
                .withTier("of_the_troll", ModValue.of(9.1, 13), 35, 1000)
                .withTier("of_convalescence", ModValue.of(13.1, 18), 47, 1000)
                .withTier("of_recuperation", ModValue.of(18.1, 23), 58, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "CriticalStrikeMultiplierBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.CROSS_BOW), EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(ModAdditions.ADD_CRIT_DAMAGE)
                .withTier("of_ire", ModValue.of(10), 8, 1000)
                .withTier("of_anger", ModValue.of(12, 13), 21, 1000)
                .withTier("of_rage", ModValue.of(14, 16), 30, 1000)
                .withTier("of_fury", ModValue.of(17, 19), 44, 500)
                .withTier("of_ferocity", ModValue.of(20, 22), 59, 250)
                .withTier("of_destruction", ModValue.of(23, 25), 73, 125)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "CriticalStrikeMultiplierClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(ModAdditions.ADD_CRIT_DAMAGE)
                .withTier("of_ire", ModValue.of(10), 8, 1)
                .withTier("of_anger", ModValue.of(12, 13), 21, 1)
                .withTier("of_rage", ModValue.of(14, 16), 30, 1)
                .withTier("of_fury", ModValue.of(17, 19), 44, 1)
                .withTier("of_ferocity", ModValue.of(20, 22), 59, 1)
                .withTier("of_destruction", ModValue.of(23, 25), 73, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "CriticalStrikeMultiplierSceptre",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.SCEPTRE), EquipmentGen.id(EquipmentGen.WAND)), ModPreset.builder(ModAdditions.ADD_CRIT_DAMAGE)
                .withTier("of_ire", ModValue.of(10), 8, 1000)
                .withTier("of_anger", ModValue.of(15, 19), 21, 1000)
                .withTier("of_rage", ModValue.of(20, 24), 30, 1000)
                .withTier("of_fury", ModValue.of(25, 29), 44, 500)
                .withTier("of_ferocity", ModValue.of(30, 34), 59, 250)
                .withTier("of_destruction", ModValue.of(35, 39), 73, 125)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "CriticalStrikeChanceIncreaseBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.CROSS_BOW), EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(ModAdditions.CRIT_CHANCE_INC)
                .withTier("of_menace", ModValue.of(1.01, 1.5), 1, 1000)
                .withTier("of_havoc", ModValue.of(1.51, 2.1), 20, 1000)
                .withTier("of_disaster", ModValue.of(2.11, 2.7), 30, 1000)
                .withTier("of_calamity", ModValue.of(3.11, 3.8), 44, 500)
                .withTier("of_ruin", ModValue.of(3.81, 4.4), 59, 250)
                .withTier("of_unmaking", ModValue.of(4.41, 5), 73, 125)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "CriticalStrikeChanceIncreaseClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(ModAdditions.CRIT_CHANCE_INC)
                .withTier("of_menace", ModValue.of(1.01, 1.5), 1, 1)
                .withTier("of_havoc", ModValue.of(1.51, 2.1), 20, 1)
                .withTier("of_disaster", ModValue.of(2.11, 2.7), 30, 1)
                .withTier("of_calamity", ModValue.of(3.11, 3.8), 44, 1)
                .withTier("of_ruin", ModValue.of(3.81, 4.4), 59, 1)
                .withTier("of_unmaking", ModValue.of(4.41, 5), 73, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ACCESSORY_GROUP_MOD_SUFFIX, "ManaRegeneration",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.AMULET), EquipmentGen.id(EquipmentGen.RING)), ModPreset.builder(ModAdditions.MANA_REGENERATION_RATE_INC)
                .withTier("of_excitement", ModValue.of(10), 1, 1000)
                .withTier("of_joy", ModValue.of(20, 29), 18, 1000)
                .withTier("of_elation", ModValue.of(30, 39), 29, 1000)
                .withTier("of_bliss", ModValue.of(40, 49), 42, 1000)
                .withTier("of_euphoria", ModValue.of(50, 59), 55, 1000)
                .withTier("of_nirvana", ModValue.of(60, 69), 79, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "ManaRegeneration",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.SCEPTRE), EquipmentGen.id(EquipmentGen.WAND)), ModPreset.builder(ModAdditions.MANA_REGENERATION_RATE_INC)
                .withTier("of_excitement", ModValue.of(10), 1, 1000)
                .withTier("of_joy", ModValue.of(20, 29), 18, 1000)
                .withTier("of_elation", ModValue.of(30, 39), 29, 1000)
                .withTier("of_bliss", ModValue.of(40, 49), 42, 1000)
                .withTier("of_euphoria", ModValue.of(50, 59), 55, 1000)
                .withTier("of_nirvana", ModValue.of(60, 69), 79, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ACCESSORY_GROUP_MOD_SUFFIX, "ItemFoundRarityIncrease",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.AMULET), EquipmentGen.id(EquipmentGen.RING)), ModPreset.builder(ModAdditions.FOUND_ITEM_RARITY_INC)
                .withTier("of_plunder", ModValue.of(6), 3, 1000)
                .withTier("of_raiding", ModValue.of(11, 14), 24, 1000)
                .withTier("of_archaeology", ModValue.of(15, 18), 40, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "ItemFoundRarityIncrease",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.FEET), EquipmentGen.id(EquipmentGen.HEAD), EquipmentGen.id(EquipmentGen.LEGS)), ModPreset.builder(ModAdditions.FOUND_ITEM_RARITY_INC)
                .withTier("of_plunder", ModValue.of(6), 3, 1000)
                .withTier("of_raiding", ModValue.of(11, 14), 24, 1000)
                .withTier("of_archaeology", ModValue.of(15, 18), 40, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "IncreaseSocketedGemLevelClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD)), ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_dueling", ModValue.of(1), 18, 1)
                .withTier("of_conflict", ModValue.of(2, 2), 36, 1)
                .withTier("of_battle", ModValue.of(3, 3), 55, 1)
                .withTier("of_war", ModValue.of(4, 4), 81, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "IncreaseSocketedGemLevelQuarterstaff",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_dueling", ModValue.of(2), 18, 750)
                .withTier("of_conflict", ModValue.of(3, 3), 36, 500)
                .withTier("of_battle", ModValue.of(4, 4), 55, 250)
                .withTier("of_war", ModValue.of(5, 5), 81, 100)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "IncreaseSocketedGemLevelTwoHandAxe",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_dueling", ModValue.of(2), 18, 1)
                .withTier("of_conflict", ModValue.of(3, 3), 36, 1)
                .withTier("of_battle", ModValue.of(4, 4), 55, 1)
                .withTier("of_war", ModValue.of(5, 5), 81, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ACCESSORY_GROUP_MOD_SUFFIX, "Dexterity",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.AMULET), EquipmentGen.id(EquipmentGen.RING)), ModPreset.builder(ModAdditions.ADD_DEXTERITY)
                .withTier("of_the_mongoose", ModValue.of(5), 1, 1000)
                .withTier("of_the_lynx", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_fox", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_falcon", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_panther", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_leopard", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_jaguar", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_phantom", ModValue.of(31, 33), 74, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "DexterityBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.QUIVER)), ModPreset.builder(ModAdditions.ADD_DEXTERITY)
                .withTier("of_the_mongoose", ModValue.of(5), 1, 1000)
                .withTier("of_the_lynx", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_fox", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_falcon", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_panther", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_leopard", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_jaguar", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_phantom", ModValue.of(31, 33), 74, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "Dexterity",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BUCKLER), EquipmentGen.id(EquipmentGen.CHEST), EquipmentGen.id(EquipmentGen.FEET), EquipmentGen.id(EquipmentGen.HEAD)), ModPreset.builder(ModAdditions.ADD_DEXTERITY)
                .withTier("of_the_mongoose", ModValue.of(5), 1, 1000)
                .withTier("of_the_lynx", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_fox", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_falcon", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_panther", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_leopard", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_jaguar", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_phantom", ModValue.of(31, 33), 74, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "DexterityClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TRAP), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(ModAdditions.ADD_DEXTERITY)
                .withTier("of_the_mongoose", ModValue.of(5), 1, 1)
                .withTier("of_the_lynx", ModValue.of(9, 12), 11, 1)
                .withTier("of_the_fox", ModValue.of(13, 16), 22, 1)
                .withTier("of_the_falcon", ModValue.of(17, 20), 33, 1)
                .withTier("of_the_panther", ModValue.of(21, 24), 44, 1)
                .withTier("of_the_leopard", ModValue.of(25, 27), 55, 1)
                .withTier("of_the_jaguar", ModValue.of(28, 30), 66, 1)
                .withTier("of_the_phantom", ModValue.of(31, 33), 74, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "DexterityQuarterstaff",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR)), ModPreset.builder(ModAdditions.ADD_DEXTERITY)
                .withTier("of_the_mongoose", ModValue.of(5), 1, 750)
                .withTier("of_the_lynx", ModValue.of(9, 12), 11, 750)
                .withTier("of_the_fox", ModValue.of(13, 16), 22, 750)
                .withTier("of_the_falcon", ModValue.of(17, 20), 33, 750)
                .withTier("of_the_panther", ModValue.of(21, 24), 44, 750)
                .withTier("of_the_leopard", ModValue.of(25, 27), 55, 750)
                .withTier("of_the_jaguar", ModValue.of(28, 30), 66, 750)
                .withTier("of_the_phantom", ModValue.of(31, 33), 74, 750)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ACCESSORY_GROUP_MOD_SUFFIX, "Intelligence",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.AMULET), EquipmentGen.id(EquipmentGen.RING)), ModPreset.builder(ModAdditions.ADD_INTELLIGENCE)
                .withTier("of_the_pupil", ModValue.of(5), 1, 1000)
                .withTier("of_the_student", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_prodigy", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_augur", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_philosopher", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_sage", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_savant", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_virtuoso", ModValue.of(31, 33), 74, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "Intelligence",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CHEST), EquipmentGen.id(EquipmentGen.FEET), EquipmentGen.id(EquipmentGen.FOCI), EquipmentGen.id(EquipmentGen.LEGS)), ModPreset.builder(ModAdditions.ADD_INTELLIGENCE)
                .withTier("of_the_pupil", ModValue.of(5), 1, 1000)
                .withTier("of_the_student", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_prodigy", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_augur", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_philosopher", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_sage", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_savant", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_virtuoso", ModValue.of(31, 33), 74, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "IntelligenceStaff",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.STAFF), EquipmentGen.id(EquipmentGen.WAND)), ModPreset.builder(ModAdditions.ADD_INTELLIGENCE)
                .withTier("of_the_pupil", ModValue.of(5), 1, 1000)
                .withTier("of_the_student", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_prodigy", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_augur", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_philosopher", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_sage", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_savant", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_virtuoso", ModValue.of(31, 33), 74, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "IntelligenceDagger",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.TRAP)), ModPreset.builder(ModAdditions.ADD_INTELLIGENCE)
                .withTier("of_the_pupil", ModValue.of(5), 1, 1)
                .withTier("of_the_student", ModValue.of(9, 12), 11, 1)
                .withTier("of_the_prodigy", ModValue.of(13, 16), 22, 1)
                .withTier("of_the_augur", ModValue.of(17, 20), 33, 1)
                .withTier("of_the_philosopher", ModValue.of(21, 24), 44, 1)
                .withTier("of_the_sage", ModValue.of(25, 27), 55, 1)
                .withTier("of_the_savant", ModValue.of(28, 30), 66, 1)
                .withTier("of_the_virtuoso", ModValue.of(31, 33), 74, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ACCESSORY_GROUP_MOD_SUFFIX, "Strength",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.AMULET), EquipmentGen.id(EquipmentGen.RING)), ModPreset.builder(ModAdditions.ADD_STRENGTH)
                .withTier("of_the_brute", ModValue.of(5), 1, 1000)
                .withTier("of_the_wrestler", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_bear", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_lion", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_gorilla", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_goliath", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_leviathan", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_titan", ModValue.of(31, 33), 74, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "Strength",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CHEST), EquipmentGen.id(EquipmentGen.FEET), EquipmentGen.id(EquipmentGen.HEAD), EquipmentGen.id(EquipmentGen.LEGS), EquipmentGen.id(EquipmentGen.SHIELD)), ModPreset.builder(ModAdditions.ADD_STRENGTH)
                .withTier("of_the_brute", ModValue.of(5), 1, 1000)
                .withTier("of_the_wrestler", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_bear", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_lion", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_gorilla", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_goliath", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_leviathan", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_titan", ModValue.of(31, 33), 74, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "StrengthOneHandMace",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(ModAdditions.ADD_STRENGTH)
                .withTier("of_the_brute", ModValue.of(5), 1, 1000)
                .withTier("of_the_wrestler", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_bear", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_lion", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_gorilla", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_goliath", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_leviathan", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_titan", ModValue.of(31, 33), 74, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "StrengthFlail",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(ModAdditions.ADD_STRENGTH)
                .withTier("of_the_brute", ModValue.of(5), 1, 1)
                .withTier("of_the_wrestler", ModValue.of(9, 12), 11, 1)
                .withTier("of_the_bear", ModValue.of(13, 16), 22, 1)
                .withTier("of_the_lion", ModValue.of(17, 20), 33, 1)
                .withTier("of_the_gorilla", ModValue.of(21, 24), 44, 1)
                .withTier("of_the_goliath", ModValue.of(25, 27), 55, 1)
                .withTier("of_the_leviathan", ModValue.of(28, 30), 66, 1)
                .withTier("of_the_titan", ModValue.of(31, 33), 74, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "StrengthSceptre",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.SCEPTRE), EquipmentGen.id(EquipmentGen.SPEAR)), ModPreset.builder(ModAdditions.ADD_STRENGTH)
                .withTier("of_the_brute", ModValue.of(5), 1, 250)
                .withTier("of_the_wrestler", ModValue.of(9, 12), 11, 250)
                .withTier("of_the_bear", ModValue.of(13, 16), 22, 250)
                .withTier("of_the_lion", ModValue.of(17, 20), 33, 250)
                .withTier("of_the_gorilla", ModValue.of(21, 24), 44, 250)
                .withTier("of_the_goliath", ModValue.of(25, 27), 55, 250)
                .withTier("of_the_leviathan", ModValue.of(28, 30), 66, 250)
                .withTier("of_the_titan", ModValue.of(31, 33), 74, 250)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ACCESSORY_GROUP_MOD_SUFFIX, "ChaosResistance",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.AMULET), EquipmentGen.id(EquipmentGen.BELT), EquipmentGen.id(EquipmentGen.RING)), ModPreset.builder(ModAdditions.ADD_CHAOS_RESISTANCE)
                .withTier("of_the_lost", ModValue.of(4), 16, 250)
                .withTier("of_banishment", ModValue.of(8, 11), 30, 250)
                .withTier("of_eviction", ModValue.of(12, 15), 44, 250)
                .withTier("of_expulsion", ModValue.of(16, 19), 56, 250)
                .withTier("of_exile", ModValue.of(20, 23), 68, 250)
                .withTier("of_bameth", ModValue.of(24, 27), 81, 250)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "ChaosResistance",
            EquipmentModPreset.of(ModPreset.builder(ModAdditions.ADD_CHAOS_RESISTANCE)
                .withTier("of_the_lost", ModValue.of(4), 16, 250)
                .withTier("of_banishment", ModValue.of(8, 11), 30, 250)
                .withTier("of_eviction", ModValue.of(12, 15), 44, 250)
                .withTier("of_expulsion", ModValue.of(16, 19), 56, 250)
                .withTier("of_exile", ModValue.of(20, 23), 68, 250)
                .withTier("of_bameth", ModValue.of(24, 27), 81, 250)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ACCESSORY_GROUP_MOD_SUFFIX, "ColdResistance",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.AMULET), EquipmentGen.id(EquipmentGen.BELT), EquipmentGen.id(EquipmentGen.RING)), ModPreset.builder(ModAdditions.ADD_COLD_RESISTANCE)
                .withTier("of_the_seal", ModValue.of(6), 1, 1000)
                .withTier("of_the_penguin", ModValue.of(11, 15), 14, 1000)
                .withTier("of_the_narwhal", ModValue.of(16, 20), 26, 1000)
                .withTier("of_the_yeti", ModValue.of(21, 25), 38, 1000)
                .withTier("of_the_walrus", ModValue.of(26, 30), 50, 1000)
                .withTier("of_the_polar_bear", ModValue.of(31, 35), 60, 1000)
                .withTier("of_the_ice", ModValue.of(36, 40), 71, 1000)
                .withTier("of_haast", ModValue.of(41, 45), 82, 1000)
                .build()));

    }

    private static void g1(BootstrapContext<ItemInstance> bootstrap) {
        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "ColdResistance",
            EquipmentModPreset.of(ModPreset.builder(ModAdditions.ADD_COLD_RESISTANCE)
                .withTier("of_the_seal", ModValue.of(6), 1, 1000)
                .withTier("of_the_penguin", ModValue.of(11, 15), 14, 1000)
                .withTier("of_the_narwhal", ModValue.of(16, 20), 26, 1000)
                .withTier("of_the_yeti", ModValue.of(21, 25), 38, 1000)
                .withTier("of_the_walrus", ModValue.of(26, 30), 50, 1000)
                .withTier("of_the_polar_bear", ModValue.of(31, 35), 60, 1000)
                .withTier("of_the_ice", ModValue.of(36, 40), 71, 1000)
                .withTier("of_haast", ModValue.of(41, 45), 82, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ACCESSORY_GROUP_MOD_SUFFIX, "FireResistance",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.AMULET), EquipmentGen.id(EquipmentGen.BELT), EquipmentGen.id(EquipmentGen.RING)), ModPreset.builder(ModAdditions.ADD_FIRE_RESISTANCE)
                .withTier("of_the_whelpling", ModValue.of(6), 1, 1000)
                .withTier("of_the_salamander", ModValue.of(11, 15), 12, 1000)
                .withTier("of_the_drake", ModValue.of(16, 20), 24, 1000)
                .withTier("of_the_kiln", ModValue.of(21, 25), 36, 1000)
                .withTier("of_the_furnace", ModValue.of(26, 30), 48, 1000)
                .withTier("of_the_volcano", ModValue.of(31, 35), 60, 1000)
                .withTier("of_magma", ModValue.of(36, 40), 71, 1000)
                .withTier("of_tzteosh", ModValue.of(41, 45), 82, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "FireResistance",
            EquipmentModPreset.of(ModPreset.builder(ModAdditions.ADD_FIRE_RESISTANCE)
                .withTier("of_the_whelpling", ModValue.of(6), 1, 1000)
                .withTier("of_the_salamander", ModValue.of(11, 15), 12, 1000)
                .withTier("of_the_drake", ModValue.of(16, 20), 24, 1000)
                .withTier("of_the_kiln", ModValue.of(21, 25), 36, 1000)
                .withTier("of_the_furnace", ModValue.of(26, 30), 48, 1000)
                .withTier("of_the_volcano", ModValue.of(31, 35), 60, 1000)
                .withTier("of_magma", ModValue.of(36, 40), 71, 1000)
                .withTier("of_tzteosh", ModValue.of(41, 45), 82, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ACCESSORY_GROUP_MOD_SUFFIX, "LightningResistance",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.AMULET), EquipmentGen.id(EquipmentGen.BELT), EquipmentGen.id(EquipmentGen.RING)), ModPreset.builder(ModAdditions.ADD_LIGHTNING_RESISTANCE)
                .withTier("of_the_cloud", ModValue.of(6), 1, 1000)
                .withTier("of_the_squall", ModValue.of(11, 15), 13, 1000)
                .withTier("of_the_storm", ModValue.of(16, 20), 25, 1000)
                .withTier("of_the_thunderhead", ModValue.of(21, 25), 37, 1000)
                .withTier("of_the_tempest", ModValue.of(26, 30), 49, 1000)
                .withTier("of_the_maelstrom", ModValue.of(31, 35), 60, 1000)
                .withTier("of_the_lightning", ModValue.of(36, 40), 71, 1000)
                .withTier("of_ephij", ModValue.of(41, 45), 82, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "LightningResistance",
            EquipmentModPreset.of(ModPreset.builder(ModAdditions.ADD_LIGHTNING_RESISTANCE)
                .withTier("of_the_cloud", ModValue.of(6), 1, 1000)
                .withTier("of_the_squall", ModValue.of(11, 15), 13, 1000)
                .withTier("of_the_storm", ModValue.of(16, 20), 25, 1000)
                .withTier("of_the_thunderhead", ModValue.of(21, 25), 37, 1000)
                .withTier("of_the_tempest", ModValue.of(26, 30), 49, 1000)
                .withTier("of_the_maelstrom", ModValue.of(31, 35), 60, 1000)
                .withTier("of_the_lightning", ModValue.of(36, 40), 71, 1000)
                .withTier("of_ephij", ModValue.of(41, 45), 82, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_PREFIX, "Thorns",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BUCKLER), EquipmentGen.id(EquipmentGen.CHEST), EquipmentGen.id(EquipmentGen.SHIELD)), ModPreset.builder(ModAdditions.THORNS)
                .withTier("thorny", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 4)), 1, 1000)
                .withTier("spiny", Map.of("min", ModValue.of(5, 7), "max", ModValue.of(7, 10)), 10, 1000)
                .withTier("barbed", Map.of("min", ModValue.of(11, 16), "max", ModValue.of(17, 23)), 19, 1000)
                .withTier("pointed", Map.of("min", ModValue.of(24, 35), "max", ModValue.of(36, 53)), 38, 1000)
                .withTier("spiked", Map.of("min", ModValue.of(40, 60), "max", ModValue.of(61, 92)), 48, 1000)
                .withTier("edged", Map.of("min", ModValue.of(64, 97), "max", ModValue.of(98, 145)), 63, 1000)
                .withTier("jagged", Map.of("min", ModValue.of(101, 151), "max", ModValue.of(152, 220)), 74, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "StunThreshold",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BUCKLER), EquipmentGen.id(EquipmentGen.CHEST), EquipmentGen.id(EquipmentGen.SHIELD)), ModPreset.builder(ModAdditions.ADD_STUN_THRESHOLD)
                .withTier("of_thick_skin", ModValue.of(6), 1, 800)
                .withTier("of_reinforced_skin", ModValue.of(12, 29), 8, 800)
                .withTier("of_stone_skin", ModValue.of(30, 49), 15, 800)
                .withTier("of_iron_skin", ModValue.of(50, 72), 22, 800)
                .withTier("of_steel_skin", ModValue.of(73, 97), 29, 800)
                .withTier("of_granite_skin", ModValue.of(98, 124), 36, 800)
                .withTier("of_platinum_skin", ModValue.of(125, 163), 45, 800)
                .withTier("of_adamantite_skin", ModValue.of(164, 206), 54, 800)
                .withTier("of_corundum_skin", ModValue.of(207, 253), 63, 800)
                .withTier("of_obsidian_skin", ModValue.of(254, 304), 72, 800)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_PREFIX, "DefencesPercent",
            EquipmentModPreset.from(EquipmentModPreset.Local.ARMOR, List.of(EquipmentGen.id(EquipmentGen.FEET), EquipmentGen.id(EquipmentGen.HEAD), EquipmentGen.id(EquipmentGen.LEGS)), ModPreset.builder(ModAdditions.ARMOR_INC_LOCAL)
                .withTier("reinforced", ModValue.of(15), 2, 1000)
                .withTier("infixed", ModValue.of(15, 26), 2, 1000)
                .withTier("scrapper_s", ModValue.of(15, 26), 2, 1000)
                .withTier("protective", ModValue.of(15, 26), 2, 1000)
                .withTier("shade_s", ModValue.of(15, 26), 2, 1000)
                .withTier("shadowy", ModValue.of(15, 26), 2, 1000)
                .withTier("layered", ModValue.of(27, 42), 16, 1000)
                .withTier("ingrained", ModValue.of(27, 42), 16, 1000)
                .withTier("brawler_s", ModValue.of(27, 42), 16, 1000)
                .withTier("strong_willed", ModValue.of(27, 42), 16, 1000)
                .withTier("ghost_s", ModValue.of(27, 42), 16, 1000)
                .withTier("ethereal", ModValue.of(27, 42), 16, 1000)
                .withTier("instilled", ModValue.of(43, 55), 33, 1000)
                .withTier("fencer_s", ModValue.of(43, 55), 33, 1000)
                .withTier("resolute", ModValue.of(43, 55), 33, 1000)
                .withTier("spectre_s", ModValue.of(43, 55), 33, 1000)
                .withTier("unworldly", ModValue.of(43, 55), 33, 1000)
                .withTier("lobstered", ModValue.of(43, 55), 35, 1000)
                .withTier("buttressed", ModValue.of(56, 67), 46, 1000)
                .withTier("infused", ModValue.of(56, 67), 46, 1000)
                .withTier("gladiator_s", ModValue.of(56, 67), 46, 1000)
                .withTier("fearless", ModValue.of(56, 67), 46, 1000)
                .withTier("wraith_s", ModValue.of(56, 67), 46, 1000)
                .withTier("ephemeral", ModValue.of(56, 67), 46, 1000)
                .withTier("thickened", ModValue.of(68, 79), 54, 1000)
                .withTier("inculcated", ModValue.of(68, 79), 54, 1000)
                .withTier("duelist_s", ModValue.of(68, 79), 54, 1000)
                .withTier("dauntless", ModValue.of(68, 79), 54, 1000)
                .withTier("phantasm_s", ModValue.of(68, 79), 54, 1000)
                .withTier("evanescent", ModValue.of(68, 79), 54, 1000)
                .withTier("girded", ModValue.of(80, 91), 60, 1000)
                .withTier("interpolated", ModValue.of(80, 91), 60, 1000)
                .withTier("hero_s", ModValue.of(80, 91), 60, 1000)
                .withTier("indomitable", ModValue.of(80, 91), 60, 1000)
                .withTier("nightmare_s", ModValue.of(80, 91), 60, 1000)
                .withTier("unreal", ModValue.of(80, 91), 60, 1000)
                .withTier("impregnable", ModValue.of(92, 100), 65, 1000)
                .withTier("inspired", ModValue.of(92, 100), 65, 1000)
                .withTier("legend_s", ModValue.of(92, 100), 65, 1000)
                .withTier("unassailable", ModValue.of(92, 100), 65, 1000)
                .withTier("mirage_s", ModValue.of(92, 100), 65, 1000)
                .withTier("illusory", ModValue.of(92, 100), 65, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_PREFIX, "BaseLocalDefencesAndLife",
            EquipmentModPreset.from(EquipmentModPreset.Local.ARMOR_ENERGY, List.of(EquipmentGen.id(EquipmentGen.CHEST), EquipmentGen.id(EquipmentGen.HEAD), EquipmentGen.id(EquipmentGen.LEGS)), ModPreset.builder(MultiAddition.keyed(ModAdditions.ARMOR_ENERGY_INC_LOCAL, ModAdditions.ADD_MAX_HEALTH))
                .withTier("augur_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(7, 10)), 8, 1000)
                .withTier("bully_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(7, 10)), 8, 1000)
                .withTier("oyster_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(7, 10)), 8, 1000)
                .withTier("monk_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(7, 10)), 8, 1000)
                .withTier("flea_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(7, 10)), 8, 1000)
                .withTier("poet_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(7, 10)), 8, 1000)
                .withTier("auspex_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(11, 19)), 16, 1000)
                .withTier("thug_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(11, 19)), 16, 1000)
                .withTier("lobster_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(11, 19)), 16, 1000)
                .withTier("prior_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(11, 19)), 16, 1000)
                .withTier("fawn_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(11, 19)), 16, 1000)
                .withTier("musician_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(11, 19)), 16, 1000)
                .withTier("druid_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(20, 25)), 33, 1000)
                .withTier("brute_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(20, 25)), 33, 1000)
                .withTier("urchin_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(20, 25)), 33, 1000)
                .withTier("abbot_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(20, 25)), 33, 1000)
                .withTier("mouflon_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(20, 25)), 33, 1000)
                .withTier("troubadour_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(20, 25)), 33, 1000)
                .withTier("haruspex_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(26, 32)), 46, 1000)
                .withTier("assailant_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(26, 32)), 46, 1000)
                .withTier("nautilus", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(26, 32)), 46, 1000)
                .withTier("bishop_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(26, 32)), 46, 1000)
                .withTier("ram_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(26, 32)), 46, 1000)
                .withTier("bard_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(26, 32)), 46, 1000)
                .withTier("visionary_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(33, 41)), 60, 1000)
                .withTier("aggressor_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(33, 41)), 60, 1000)
                .withTier("octopus", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(33, 41)), 60, 1000)
                .withTier("exarch_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(33, 41)), 60, 1000)
                .withTier("ibex_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(33, 41)), 60, 1000)
                .withTier("minstrel_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(33, 41)), 60, 1000)
                .withTier("prophet_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(42, 49)), 78, 1000)
                .withTier("predator_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(42, 49)), 78, 1000)
                .withTier("crocodile_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(42, 49)), 78, 1000)
                .withTier("pope_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(42, 49)), 78, 1000)
                .withTier("stag_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(42, 49)), 78, 1000)
                .withTier("maestro_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(42, 49)), 78, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_PREFIX, "BaseLocalDefences",
            EquipmentModPreset.from(EquipmentModPreset.Local.ARMOR, List.of(EquipmentGen.id(EquipmentGen.FEET), EquipmentGen.id(EquipmentGen.LEGS)), ModPreset.builder(ModAdditions.ADD_ARMOR_LOCAL)
                .withTier("lacquered", ModValue.of(16), 1, 1000)
                .withTier("supple", ModValue.of(9, 16), 1, 1000)
                .withTier("blessed", ModValue.of(9, 16), 1, 1000)
                .withTier("agile", ModValue.of(11, 18), 1, 1000)
                .withTier("will_o_wisp_s", ModValue.of(6, 10), 1, 1000)
                .withTier("shining", ModValue.of(10, 17), 1, 1000)
                .withTier("studded", ModValue.of(28, 56), 8, 1000)
                .withTier("dancer_s", ModValue.of(19, 46), 8, 1000)
                .withTier("glimmering", ModValue.of(18, 24), 8, 1000)
                .withTier("ribbed", ModValue.of(57, 77), 16, 1000)
                .withTier("pliant", ModValue.of(17, 46), 16, 1000)
                .withTier("anointed", ModValue.of(17, 46), 16, 1000)
                .withTier("acrobat_s", ModValue.of(47, 66), 16, 1000)
                .withTier("nymph_s", ModValue.of(11, 41), 16, 1000)
                .withTier("glittering", ModValue.of(25, 30), 16, 1000)
                .withTier("fortified", ModValue.of(78, 98), 25, 1000)
                .withTier("fleet", ModValue.of(67, 87), 25, 1000)
                .withTier("glowing", ModValue.of(31, 35), 25, 1000)
                .withTier("plated", ModValue.of(99, 127), 33, 1000)
                .withTier("flexible", ModValue.of(47, 71), 33, 1000)
                .withTier("sanctified", ModValue.of(47, 71), 33, 1000)
                .withTier("blurred", ModValue.of(88, 116), 33, 1000)
                .withTier("sylph_s", ModValue.of(42, 64), 33, 1000)
                .withTier("radiating", ModValue.of(36, 41), 33, 1000)
                .withTier("carapaced", ModValue.of(128, 159), 46, 1000)
                .withTier("durable", ModValue.of(72, 85), 46, 1000)
                .withTier("hallowed", ModValue.of(72, 85), 46, 1000)
                .withTier("phased", ModValue.of(117, 146), 46, 1000)
                .withTier("cherub_s", ModValue.of(65, 78), 46, 1000)
                .withTier("pulsing", ModValue.of(42, 47), 46, 1000)
                .withTier("encased", ModValue.of(160, 190), 54, 1000)
                .withTier("vaporous", ModValue.of(147, 176), 54, 1000)
                .withTier("blazing", ModValue.of(48, 60), 54, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "LocalAttributeRequirementsBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.CROSS_BOW), EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SCEPTRE), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.STAFF), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE), EquipmentGen.id(EquipmentGen.WAND)), ModPreset.builder(LocalAddition.of(StatIds.REDUCED_ATTRIBUTE_REQUIREMENTS))
                .withTier("of_the_worthy", ModValue.of(15), 24, 1000)
                .withTier("of_the_apt", ModValue.of(20, 20), 32, 1000)
                .withTier("of_the_talented", ModValue.of(25, 25), 40, 1000)
                .withTier("of_the_skilled", ModValue.of(30, 30), 52, 1000)
                .withTier("of_the_proficient", ModValue.of(35, 35), 60, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "LocalAttributeRequirements",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BUCKLER), EquipmentGen.id(EquipmentGen.FOCI), EquipmentGen.id(EquipmentGen.HEAD), EquipmentGen.id(EquipmentGen.SHIELD)), ModPreset.builder(LocalAddition.of(StatIds.REDUCED_ATTRIBUTE_REQUIREMENTS))
                .withTier("of_the_worthy", ModValue.of(15), 24, 800)
                .withTier("of_the_apt", ModValue.of(20, 20), 32, 800)
                .withTier("of_the_talented", ModValue.of(25, 25), 40, 800)
                .withTier("of_the_skilled", ModValue.of(30, 30), 52, 800)
                .withTier("of_the_proficient", ModValue.of(35, 35), 60, 800)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "LocalAttributeRequirementsClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(LocalAddition.of(StatIds.REDUCED_ATTRIBUTE_REQUIREMENTS))
                .withTier("of_the_worthy", ModValue.of(15), 24, 1)
                .withTier("of_the_apt", ModValue.of(20, 20), 32, 1)
                .withTier("of_the_talented", ModValue.of(25, 25), 40, 1)
                .withTier("of_the_skilled", ModValue.of(30, 30), 52, 1)
                .withTier("of_the_proficient", ModValue.of(35, 35), 60, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "EnergyShieldRegeneration",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.FEET), EquipmentGen.id(EquipmentGen.HEAD), EquipmentGen.id(EquipmentGen.LEGS)), ModPreset.builder(AdditionType.global(StatIds.INC_ENERGY_SHIELD_RECHARGE_RATE))
                .withTier("of_buffering", ModValue.of(16), 48, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "ArmourAppliesToElementalDamageChest",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CHEST), EquipmentGen.id(EquipmentGen.SHIELD)), ModPreset.builder(AdditionType.global(StatIds.ARMOUR_APPLIES_ELEMENTAL_DAMAGE))
                .withTier("of_covering", ModValue.of(14), 1, 1000)
                .withTier("of_sheathing", ModValue.of(20, 25), 16, 1000)
                .withTier("of_lining", ModValue.of(26, 31), 36, 1000)
                .withTier("of_padding", ModValue.of(32, 37), 48, 1000)
                .withTier("of_furring", ModValue.of(38, 43), 66, 1000)
                .withTier("of_thermokryptance", ModValue.of(44, 50), 81, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "ArmourAppliesToElementalDamageFeet",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.FEET), EquipmentGen.id(EquipmentGen.HEAD), EquipmentGen.id(EquipmentGen.LEGS)), ModPreset.builder(AdditionType.global(StatIds.ARMOUR_APPLIES_ELEMENTAL_DAMAGE))
                .withTier("of_covering", ModValue.of(14), 1, 1000)
                .withTier("of_sheathing", ModValue.of(20, 25), 16, 1000)
                .withTier("of_lining", ModValue.of(26, 31), 36, 1000)
                .withTier("of_padding", ModValue.of(32, 37), 48, 1000)
                .withTier("of_furring", ModValue.of(38, 43), 66, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "EvasionAppliesToDeflectionBuckler",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BUCKLER), EquipmentGen.id(EquipmentGen.CHEST)), ModPreset.builder(AdditionType.global(StatIds.EVASION_APPLIES_TO_DEFLECTION))
                .withTier("of_deflecting", ModValue.of(8), 1, 1000)
                .withTier("of_bending", ModValue.of(12, 14), 16, 1000)
                .withTier("of_curvation", ModValue.of(15, 17), 36, 1000)
                .withTier("of_diversion", ModValue.of(18, 20), 48, 1000)
                .withTier("of_flexure", ModValue.of(21, 23), 66, 1000)
                .withTier("of_warping", ModValue.of(24, 26), 81, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "EvasionAppliesToDeflectionFeet",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.FEET), EquipmentGen.id(EquipmentGen.HEAD), EquipmentGen.id(EquipmentGen.LEGS)), ModPreset.builder(AdditionType.global(StatIds.EVASION_APPLIES_TO_DEFLECTION))
                .withTier("of_deflecting", ModValue.of(8), 1, 1000)
                .withTier("of_bending", ModValue.of(12, 14), 16, 1000)
                .withTier("of_curvation", ModValue.of(15, 17), 36, 1000)
                .withTier("of_diversion", ModValue.of(18, 20), 48, 1000)
                .withTier("of_flexure", ModValue.of(21, 23), 66, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "IncreasedWeaponElementalDamagePercentBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.SPEAR)), ModPreset.builder(ModAdditions.ATTACK_ELEMENTAL_DAMAGE_INC)
                .withTier("catalysing", ModValue.of(19), 4, 500)
                .withTier("infusing", ModValue.of(36, 52), 16, 500)
                .withTier("empowering", ModValue.of(53, 62), 33, 500)
                .withTier("unleashed", ModValue.of(63, 72), 46, 500)
                .withTier("overpowering", ModValue.of(73, 86), 60, 500)
                .withTier("devastating", ModValue.of(87, 100), 81, 500)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "IncreasedWeaponElementalDamagePercentClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD)), ModPreset.builder(ModAdditions.ATTACK_ELEMENTAL_DAMAGE_INC)
                .withTier("catalysing", ModValue.of(19), 4, 1)
                .withTier("infusing", ModValue.of(36, 52), 16, 1)
                .withTier("empowering", ModValue.of(53, 62), 33, 1)
                .withTier("unleashed", ModValue.of(63, 72), 46, 1)
                .withTier("overpowering", ModValue.of(73, 86), 60, 1)
                .withTier("devastating", ModValue.of(87, 100), 81, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "IncreasedWeaponElementalDamagePercentCrossBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CROSS_BOW), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(ModAdditions.ATTACK_ELEMENTAL_DAMAGE_INC)
                .withTier("catalysing", ModValue.of(34), 4, 500)
                .withTier("infusing", ModValue.of(48, 71), 16, 500)
                .withTier("empowering", ModValue.of(72, 85), 33, 500)
                .withTier("unleashed", ModValue.of(86, 99), 46, 500)
                .withTier("overpowering", ModValue.of(100, 119), 60, 500)
                .withTier("devastating", ModValue.of(120, 139), 81, 500)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "IncreasedWeaponElementalDamagePercentTwoHandAxe",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(ModAdditions.ATTACK_ELEMENTAL_DAMAGE_INC)
                .withTier("catalysing", ModValue.of(34), 4, 1)
                .withTier("infusing", ModValue.of(48, 71), 16, 1)
                .withTier("empowering", ModValue.of(72, 85), 33, 1)
                .withTier("unleashed", ModValue.of(86, 99), 46, 1)
                .withTier("overpowering", ModValue.of(100, 119), 60, 1)
                .withTier("devastating", ModValue.of(120, 139), 81, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "LocalPhysicalDamagePercentBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.CROSS_BOW), EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(ModAdditions.ATTACK_PHYSICAL_DAMAGE_INC)
                .withTier("heavy", ModValue.of(40), 1, 1000)
                .withTier("serrated", ModValue.of(50, 64), 8, 1000)
                .withTier("wicked", ModValue.of(65, 84), 16, 1000)
                .withTier("vicious", ModValue.of(85, 109), 33, 400)
                .withTier("bloodthirsty", ModValue.of(110, 134), 46, 200)
                .withTier("cruel", ModValue.of(135, 154), 60, 100)
                .withTier("tyrannical", ModValue.of(155, 169), 75, 50)
                .withTier("merciless", ModValue.of(170, 179), 82, 25)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "LocalPhysicalDamagePercentClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(ModAdditions.ATTACK_PHYSICAL_DAMAGE_INC)
                .withTier("heavy", ModValue.of(40), 1, 1)
                .withTier("serrated", ModValue.of(50, 64), 8, 1)
                .withTier("wicked", ModValue.of(65, 84), 16, 1)
                .withTier("vicious", ModValue.of(85, 109), 33, 1)
                .withTier("bloodthirsty", ModValue.of(110, 134), 46, 1)
                .withTier("cruel", ModValue.of(135, 154), 60, 1)
                .withTier("tyrannical", ModValue.of(155, 169), 75, 1)
                .withTier("merciless", ModValue.of(170, 179), 82, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "LocalIncreasedPhysicalDamagePercentAndAccuracyRatingBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.CROSS_BOW), EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(MultiAddition.keyed(ModAdditions.ATTACK_PHYSICAL_DAMAGE_INC, ModAdditions.ADD_ACCURACY))
                .withTier("squire_s", Map.of("value", ModValue.of(15, 19), "value_2", ModValue.of(16, 20)), 8, 1000)
                .withTier("journeyman_s", Map.of("value", ModValue.of(20, 24), "value_2", ModValue.of(21, 46)), 14, 1000)
                .withTier("reaver_s", Map.of("value", ModValue.of(25, 34), "value_2", ModValue.of(47, 72)), 23, 1000)
                .withTier("mercenary_s", Map.of("value", ModValue.of(35, 44), "value_2", ModValue.of(73, 97)), 38, 1000)
                .withTier("champion_s", Map.of("value", ModValue.of(45, 54), "value_2", ModValue.of(98, 123)), 54, 600)
                .withTier("conqueror_s", Map.of("value", ModValue.of(55, 64), "value_2", ModValue.of(124, 149)), 65, 400)
                .withTier("emperor_s", Map.of("value", ModValue.of(65, 74), "value_2", ModValue.of(150, 174)), 70, 200)
                .withTier("dictator_s", Map.of("value", ModValue.of(75, 79), "value_2", ModValue.of(175, 200)), 81, 100)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "LocalIncreasedPhysicalDamagePercentAndAccuracyRatingClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(MultiAddition.keyed(ModAdditions.ATTACK_PHYSICAL_DAMAGE_INC, ModAdditions.ADD_ACCURACY))
                .withTier("squire_s", Map.of("value", ModValue.of(15, 19), "value_2", ModValue.of(16, 20)), 8, 1)
                .withTier("journeyman_s", Map.of("value", ModValue.of(20, 24), "value_2", ModValue.of(21, 46)), 14, 1)
                .withTier("reaver_s", Map.of("value", ModValue.of(25, 34), "value_2", ModValue.of(47, 72)), 23, 1)
                .withTier("mercenary_s", Map.of("value", ModValue.of(35, 44), "value_2", ModValue.of(73, 97)), 38, 1)
                .withTier("champion_s", Map.of("value", ModValue.of(45, 54), "value_2", ModValue.of(98, 123)), 54, 1)
                .withTier("conqueror_s", Map.of("value", ModValue.of(55, 64), "value_2", ModValue.of(124, 149)), 65, 1)
                .withTier("emperor_s", Map.of("value", ModValue.of(65, 74), "value_2", ModValue.of(150, 174)), 70, 1)
                .withTier("dictator_s", Map.of("value", ModValue.of(75, 79), "value_2", ModValue.of(175, 200)), 81, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "LightningDamageClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD)), ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("humming", Map.of("min", ModValue.of(1), "max", ModValue.of(4, 6)), 1, 1)
                .withTier("buzzing", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(13, 19)), 8, 1)
                .withTier("snapping", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(20, 30)), 16, 1)
                .withTier("crackling", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(36, 52)), 33, 1)
                .withTier("sparking", Map.of("min", ModValue.of(1, 3), "max", ModValue.of(55, 60)), 46, 1)
                .withTier("arcing", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(63, 82)), 54, 1)
                .withTier("shocking", Map.of("min", ModValue.of(1, 6), "max", ModValue.of(85, 107)), 60, 1)
                .withTier("discharging", Map.of("min", ModValue.of(1, 8), "max", ModValue.of(111, 152)), 65, 1)
                .withTier("electrocuting", Map.of("min", ModValue.of(1, 10), "max", ModValue.of(157, 196)), 75, 1)
                .withTier("vapourising", Map.of("min", ModValue.of(1, 12), "max", ModValue.of(202, 234)), 81, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "LightningDamageTwoHandAxe",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("humming", Map.of("min", ModValue.of(1), "max", ModValue.of(7, 10)), 1, 1)
                .withTier("buzzing", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(19, 27)), 8, 1)
                .withTier("snapping", Map.of("min", ModValue.of(1, 3), "max", ModValue.of(31, 43)), 16, 1)
                .withTier("crackling", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(53, 76)), 33, 1)
                .withTier("sparking", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(80, 88)), 46, 1)
                .withTier("arcing", Map.of("min", ModValue.of(1, 6), "max", ModValue.of(93, 122)), 54, 1)
                .withTier("shocking", Map.of("min", ModValue.of(1, 8), "max", ModValue.of(128, 162)), 60, 1)
                .withTier("discharging", Map.of("min", ModValue.of(1, 13), "max", ModValue.of(168, 231)), 65, 1)
                .withTier("electrocuting", Map.of("min", ModValue.of(1, 16), "max", ModValue.of(239, 300)), 75, 1)
                .withTier("vapourising", Map.of("min", ModValue.of(1, 19), "max", ModValue.of(310, 358)), 81, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "ColdDamageBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.SPEAR)), ModPreset.builder(ModAdditions.ADD_COLD_DAMAGE_MINMAX)
                .withTier("frosted", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 4)), 1, 800)
                .withTier("chilled", Map.of("min", ModValue.of(3, 5), "max", ModValue.of(6, 9)), 8, 800)
                .withTier("icy", Map.of("min", ModValue.of(6, 9), "max", ModValue.of(10, 16)), 16, 800)
                .withTier("frigid", Map.of("min", ModValue.of(11, 15), "max", ModValue.of(17, 24)), 33, 800)
                .withTier("freezing", Map.of("min", ModValue.of(17, 20), "max", ModValue.of(26, 32)), 46, 800)
                .withTier("frozen", Map.of("min", ModValue.of(22, 29), "max", ModValue.of(34, 44)), 54, 800)
                .withTier("glaciated", Map.of("min", ModValue.of(31, 38), "max", ModValue.of(47, 59)), 60, 480)
                .withTier("polar", Map.of("min", ModValue.of(40, 53), "max", ModValue.of(62, 80)), 65, 320)
                .withTier("entombing", Map.of("min", ModValue.of(55, 69), "max", ModValue.of(83, 106)), 75, 200)
                .withTier("crystalising", Map.of("min", ModValue.of(72, 81), "max", ModValue.of(110, 123)), 81, 80)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "ColdDamageClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD)), ModPreset.builder(ModAdditions.ADD_COLD_DAMAGE_MINMAX)
                .withTier("frosted", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 4)), 1, 1)
                .withTier("chilled", Map.of("min", ModValue.of(3, 5), "max", ModValue.of(6, 9)), 8, 1)
                .withTier("icy", Map.of("min", ModValue.of(6, 9), "max", ModValue.of(10, 16)), 16, 1)
                .withTier("frigid", Map.of("min", ModValue.of(11, 15), "max", ModValue.of(17, 24)), 33, 1)
                .withTier("freezing", Map.of("min", ModValue.of(17, 20), "max", ModValue.of(26, 32)), 46, 1)
                .withTier("frozen", Map.of("min", ModValue.of(22, 29), "max", ModValue.of(34, 44)), 54, 1)
                .withTier("glaciated", Map.of("min", ModValue.of(31, 38), "max", ModValue.of(47, 59)), 60, 1)
                .withTier("polar", Map.of("min", ModValue.of(40, 53), "max", ModValue.of(62, 80)), 65, 1)
                .withTier("entombing", Map.of("min", ModValue.of(55, 69), "max", ModValue.of(83, 106)), 75, 1)
                .withTier("crystalising", Map.of("min", ModValue.of(72, 81), "max", ModValue.of(110, 123)), 81, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "ColdDamageCrossBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CROSS_BOW), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(ModAdditions.ADD_COLD_DAMAGE_MINMAX)
                .withTier("frosted", Map.of("min", ModValue.of(2), "max", ModValue.of(4, 6)), 1, 800)
                .withTier("chilled", Map.of("min", ModValue.of(5, 8), "max", ModValue.of(9, 14)), 8, 800)
                .withTier("icy", Map.of("min", ModValue.of(10, 14), "max", ModValue.of(15, 23)), 16, 800)
                .withTier("frigid", Map.of("min", ModValue.of(16, 23), "max", ModValue.of(25, 35)), 33, 800)
                .withTier("freezing", Map.of("min", ModValue.of(25, 30), "max", ModValue.of(38, 46)), 46, 800)
                .withTier("frozen", Map.of("min", ModValue.of(32, 43), "max", ModValue.of(49, 66)), 54, 800)
                .withTier("glaciated", Map.of("min", ModValue.of(46, 57), "max", ModValue.of(70, 88)), 60, 480)
                .withTier("polar", Map.of("min", ModValue.of(60, 80), "max", ModValue.of(92, 121)), 65, 320)
                .withTier("entombing", Map.of("min", ModValue.of(84, 107), "max", ModValue.of(126, 161)), 75, 200)
                .withTier("crystalising", Map.of("min", ModValue.of(112, 124), "max", ModValue.of(168, 189)), 81, 80)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "ColdDamageTwoHandAxe",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(ModAdditions.ADD_COLD_DAMAGE_MINMAX)
                .withTier("frosted", Map.of("min", ModValue.of(2), "max", ModValue.of(4, 6)), 1, 1)
                .withTier("chilled", Map.of("min", ModValue.of(5, 8), "max", ModValue.of(9, 14)), 8, 1)
                .withTier("icy", Map.of("min", ModValue.of(10, 14), "max", ModValue.of(15, 23)), 16, 1)
                .withTier("frigid", Map.of("min", ModValue.of(16, 23), "max", ModValue.of(25, 35)), 33, 1)
                .withTier("freezing", Map.of("min", ModValue.of(25, 30), "max", ModValue.of(38, 46)), 46, 1)
                .withTier("frozen", Map.of("min", ModValue.of(32, 43), "max", ModValue.of(49, 66)), 54, 1)
                .withTier("glaciated", Map.of("min", ModValue.of(46, 57), "max", ModValue.of(70, 88)), 60, 1)
                .withTier("polar", Map.of("min", ModValue.of(60, 80), "max", ModValue.of(92, 121)), 65, 1)
                .withTier("entombing", Map.of("min", ModValue.of(84, 107), "max", ModValue.of(126, 161)), 75, 1)
                .withTier("crystalising", Map.of("min", ModValue.of(112, 124), "max", ModValue.of(168, 189)), 81, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "FireDamageClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD)), ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("heated", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 5)), 1, 1)
                .withTier("smouldering", Map.of("min", ModValue.of(4, 6), "max", ModValue.of(7, 10)), 8, 1)
                .withTier("smoking", Map.of("min", ModValue.of(7, 11), "max", ModValue.of(13, 19)), 16, 1)
                .withTier("burning", Map.of("min", ModValue.of(13, 19), "max", ModValue.of(21, 29)), 33, 1)
                .withTier("flaming", Map.of("min", ModValue.of(20, 24), "max", ModValue.of(32, 37)), 46, 1)
                .withTier("scorching", Map.of("min", ModValue.of(25, 33), "max", ModValue.of(38, 54)), 54, 1)
                .withTier("incinerating", Map.of("min", ModValue.of(35, 44), "max", ModValue.of(56, 71)), 60, 1)
                .withTier("blasting", Map.of("min", ModValue.of(47, 59), "max", ModValue.of(74, 97)), 65, 1)
                .withTier("cremating", Map.of("min", ModValue.of(62, 85), "max", ModValue.of(101, 129)), 75, 1)
                .withTier("carbonising", Map.of("min", ModValue.of(88, 101), "max", ModValue.of(133, 154)), 81, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "FireDamageTwoHandAxe",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("heated", Map.of("min", ModValue.of(2), "max", ModValue.of(5, 7)), 1, 1)
                .withTier("smouldering", Map.of("min", ModValue.of(6, 9), "max", ModValue.of(10, 16)), 8, 1)
                .withTier("smoking", Map.of("min", ModValue.of(11, 17), "max", ModValue.of(19, 28)), 16, 1)
                .withTier("burning", Map.of("min", ModValue.of(19, 27), "max", ModValue.of(30, 42)), 33, 1)
                .withTier("flaming", Map.of("min", ModValue.of(30, 37), "max", ModValue.of(45, 56)), 46, 1)
                .withTier("scorching", Map.of("min", ModValue.of(39, 53), "max", ModValue.of(59, 80)), 54, 1)
                .withTier("incinerating", Map.of("min", ModValue.of(56, 70), "max", ModValue.of(84, 107)), 60, 1)
                .withTier("blasting", Map.of("min", ModValue.of(73, 97), "max", ModValue.of(112, 149)), 65, 1)
                .withTier("cremating", Map.of("min", ModValue.of(102, 130), "max", ModValue.of(155, 198)), 75, 1)
                .withTier("carbonising", Map.of("min", ModValue.of(135, 156), "max", ModValue.of(205, 236)), 81, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "PhysicalDamageBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.SPEAR)), ModPreset.builder(ModAdditions.ADD_PHYSICAL_DAMAGE_MINMAX)
                .withTier("glinting", Map.of("min", ModValue.of(1), "max", ModValue.of(4, 5)), 1, 1000)
                .withTier("burnished", Map.of("min", ModValue.of(4, 6), "max", ModValue.of(7, 11)), 8, 1000)
                .withTier("polished", Map.of("min", ModValue.of(6, 9), "max", ModValue.of(11, 16)), 16, 1000)
                .withTier("honed", Map.of("min", ModValue.of(8, 12), "max", ModValue.of(14, 21)), 33, 1000)
                .withTier("gleaming", Map.of("min", ModValue.of(10, 15), "max", ModValue.of(18, 26)), 46, 1000)
                .withTier("annealed", Map.of("min", ModValue.of(13, 20), "max", ModValue.of(23, 35)), 54, 600)
                .withTier("razor_sharp", Map.of("min", ModValue.of(16, 24), "max", ModValue.of(28, 42)), 60, 400)
                .withTier("tempered", Map.of("min", ModValue.of(21, 31), "max", ModValue.of(36, 53)), 65, 200)
                .withTier("flaring", Map.of("min", ModValue.of(26, 39), "max", ModValue.of(44, 66)), 75, 100)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "PhysicalDamageClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD)), ModPreset.builder(ModAdditions.ADD_PHYSICAL_DAMAGE_MINMAX)
                .withTier("glinting", Map.of("min", ModValue.of(1), "max", ModValue.of(4, 5)), 1, 1)
                .withTier("burnished", Map.of("min", ModValue.of(4, 6), "max", ModValue.of(7, 11)), 8, 1)
                .withTier("polished", Map.of("min", ModValue.of(6, 9), "max", ModValue.of(11, 16)), 16, 1)
                .withTier("honed", Map.of("min", ModValue.of(8, 12), "max", ModValue.of(14, 21)), 33, 1)
                .withTier("gleaming", Map.of("min", ModValue.of(10, 15), "max", ModValue.of(18, 26)), 46, 1)
                .withTier("annealed", Map.of("min", ModValue.of(13, 20), "max", ModValue.of(23, 35)), 54, 1)
                .withTier("razor_sharp", Map.of("min", ModValue.of(16, 24), "max", ModValue.of(28, 42)), 60, 1)
                .withTier("tempered", Map.of("min", ModValue.of(21, 31), "max", ModValue.of(36, 53)), 65, 1)
                .withTier("flaring", Map.of("min", ModValue.of(26, 39), "max", ModValue.of(44, 66)), 75, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "PhysicalDamageCrossBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CROSS_BOW), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(ModAdditions.ADD_PHYSICAL_DAMAGE_MINMAX)
                .withTier("glinting", Map.of("min", ModValue.of(2), "max", ModValue.of(5, 7)), 1, 1000)
                .withTier("burnished", Map.of("min", ModValue.of(5, 8), "max", ModValue.of(10, 15)), 8, 1000)
                .withTier("polished", Map.of("min", ModValue.of(8, 12), "max", ModValue.of(15, 22)), 16, 1000)
                .withTier("honed", Map.of("min", ModValue.of(11, 17), "max", ModValue.of(20, 30)), 33, 1000)
                .withTier("gleaming", Map.of("min", ModValue.of(14, 21), "max", ModValue.of(25, 37)), 46, 1000)
                .withTier("annealed", Map.of("min", ModValue.of(19, 29), "max", ModValue.of(33, 49)), 54, 600)
                .withTier("razor_sharp", Map.of("min", ModValue.of(23, 35), "max", ModValue.of(39, 59)), 60, 400)
                .withTier("tempered", Map.of("min", ModValue.of(29, 44), "max", ModValue.of(50, 75)), 65, 200)
                .withTier("flaring", Map.of("min", ModValue.of(37, 55), "max", ModValue.of(63, 94)), 75, 100)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_PREFIX, "PhysicalDamageTwoHandAxe",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(ModAdditions.ADD_PHYSICAL_DAMAGE_MINMAX)
                .withTier("glinting", Map.of("min", ModValue.of(2), "max", ModValue.of(5, 7)), 1, 1)
                .withTier("burnished", Map.of("min", ModValue.of(5, 8), "max", ModValue.of(10, 15)), 8, 1)
                .withTier("polished", Map.of("min", ModValue.of(8, 12), "max", ModValue.of(15, 22)), 16, 1)
                .withTier("honed", Map.of("min", ModValue.of(11, 17), "max", ModValue.of(20, 30)), 33, 1)
                .withTier("gleaming", Map.of("min", ModValue.of(14, 21), "max", ModValue.of(25, 37)), 46, 1)
                .withTier("annealed", Map.of("min", ModValue.of(19, 29), "max", ModValue.of(33, 49)), 54, 1)
                .withTier("razor_sharp", Map.of("min", ModValue.of(23, 35), "max", ModValue.of(39, 59)), 60, 1)
                .withTier("tempered", Map.of("min", ModValue.of(29, 44), "max", ModValue.of(50, 75)), 65, 1)
                .withTier("flaring", Map.of("min", ModValue.of(37, 55), "max", ModValue.of(63, 94)), 75, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "IncreasedAttackSpeedBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.CROSS_BOW)), ModPreset.builder(ModAdditions.ATTACK_SPEED_INC)
                .withTier("of_skill", ModValue.of(5), 1, 1000)
                .withTier("of_ease", ModValue.of(8, 10), 11, 1000)
                .withTier("of_mastery", ModValue.of(11, 13), 22, 1000)
                .withTier("of_renown", ModValue.of(14, 16), 30, 500)
                .withTier("of_acclaim", ModValue.of(17, 19), 37, 400)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "IncreasedAttackSpeedClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(ModAdditions.ATTACK_SPEED_INC)
                .withTier("of_skill", ModValue.of(5), 1, 1)
                .withTier("of_ease", ModValue.of(8, 10), 11, 1)
                .withTier("of_mastery", ModValue.of(11, 13), 22, 1)
                .withTier("of_renown", ModValue.of(14, 16), 30, 1)
                .withTier("of_acclaim", ModValue.of(17, 19), 37, 1)
                .withTier("of_fame", ModValue.of(20, 22), 45, 1)
                .withTier("of_infamy", ModValue.of(23, 25), 60, 1)
                .withTier("of_celebration", ModValue.of(26, 28), 77, 1)
                .build()));

    }

    private static void g2(BootstrapContext<ItemInstance> bootstrap) {
        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "IncreasedAttackSpeedOneHandMace",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(ModAdditions.ATTACK_SPEED_INC)
                .withTier("of_skill", ModValue.of(5), 1, 1000)
                .withTier("of_ease", ModValue.of(8, 10), 11, 1000)
                .withTier("of_mastery", ModValue.of(11, 13), 22, 1000)
                .withTier("of_renown", ModValue.of(14, 16), 30, 1000)
                .withTier("of_acclaim", ModValue.of(17, 19), 37, 500)
                .withTier("of_fame", ModValue.of(20, 22), 45, 500)
                .withTier("of_infamy", ModValue.of(23, 25), 60, 200)
                .withTier("of_celebration", ModValue.of(26, 28), 77, 100)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "LightRadiusAndAccuracyBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.CROSS_BOW), EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(MultiAddition.keyed(AdditionType.global(StatIds.LIGHT_RADIUS), ModAdditions.ADD_ACCURACY))
                .withTier("of_shining", Map.of("value", ModValue.of(10), "value_2", ModValue.of(5, 5)), 8, 1000)
                .withTier("of_light", Map.of("value", ModValue.of(21, 40), "value_2", ModValue.of(10, 10)), 15, 1000)
                .withTier("of_radiance", Map.of("value", ModValue.of(41, 60), "value_2", ModValue.of(15, 15)), 30, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "LightRadiusAndAccuracyClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(MultiAddition.keyed(AdditionType.global(StatIds.LIGHT_RADIUS), ModAdditions.ADD_ACCURACY))
                .withTier("of_shining", Map.of("value", ModValue.of(10), "value_2", ModValue.of(5, 5)), 8, 1)
                .withTier("of_light", Map.of("value", ModValue.of(21, 40), "value_2", ModValue.of(10, 10)), 15, 1)
                .withTier("of_radiance", Map.of("value", ModValue.of(41, 60), "value_2", ModValue.of(15, 15)), 30, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "LifeGainedFromEnemyDeathBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.CROSS_BOW), EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.STAFF), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE), EquipmentGen.id(EquipmentGen.WAND)), ModPreset.builder(AdditionType.global(StatIds.LIFE_GAINED_FROM_ENEMY_DEATH))
                .withTier("of_success", ModValue.of(4), 1, 750)
                .withTier("of_victory", ModValue.of(7, 9), 11, 750)
                .withTier("of_triumph", ModValue.of(10, 18), 22, 750)
                .withTier("of_conquest", ModValue.of(19, 28), 33, 750)
                .withTier("of_vanquishing", ModValue.of(29, 40), 44, 750)
                .withTier("of_valour", ModValue.of(41, 53), 55, 750)
                .withTier("of_glory", ModValue.of(54, 68), 66, 750)
                .withTier("of_legend", ModValue.of(69, 84), 77, 750)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "LifeGainedFromEnemyDeathClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TRAP), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(AdditionType.global(StatIds.LIFE_GAINED_FROM_ENEMY_DEATH))
                .withTier("of_success", ModValue.of(4), 1, 1)
                .withTier("of_victory", ModValue.of(7, 9), 11, 1)
                .withTier("of_triumph", ModValue.of(10, 18), 22, 1)
                .withTier("of_conquest", ModValue.of(19, 28), 33, 1)
                .withTier("of_vanquishing", ModValue.of(29, 40), 44, 1)
                .withTier("of_valour", ModValue.of(41, 53), 55, 1)
                .withTier("of_glory", ModValue.of(54, 68), 66, 1)
                .withTier("of_legend", ModValue.of(69, 84), 77, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "ManaGainedFromEnemyDeathBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.CROSS_BOW), EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.STAFF), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE), EquipmentGen.id(EquipmentGen.WAND)), ModPreset.builder(AdditionType.global(StatIds.MANA_GAINED_FROM_ENEMY_DEATH))
                .withTier("of_absorption", ModValue.of(2), 1, 750)
                .withTier("of_osmosis", ModValue.of(4, 5), 12, 750)
                .withTier("of_infusion", ModValue.of(6, 9), 23, 750)
                .withTier("of_enveloping", ModValue.of(10, 14), 34, 750)
                .withTier("of_consumption", ModValue.of(15, 20), 45, 750)
                .withTier("of_siphoning", ModValue.of(21, 27), 56, 750)
                .withTier("of_devouring", ModValue.of(28, 35), 67, 750)
                .withTier("of_assimilation", ModValue.of(36, 45), 78, 750)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "ManaGainedFromEnemyDeathClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TRAP), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(AdditionType.global(StatIds.MANA_GAINED_FROM_ENEMY_DEATH))
                .withTier("of_absorption", ModValue.of(2), 1, 1)
                .withTier("of_osmosis", ModValue.of(4, 5), 12, 1)
                .withTier("of_infusion", ModValue.of(6, 9), 23, 1)
                .withTier("of_enveloping", ModValue.of(10, 14), 34, 1)
                .withTier("of_consumption", ModValue.of(15, 20), 45, 1)
                .withTier("of_siphoning", ModValue.of(21, 27), 56, 1)
                .withTier("of_devouring", ModValue.of(28, 35), 67, 1)
                .withTier("of_assimilation", ModValue.of(36, 45), 78, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "LifeGainPerTargetBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.CROSS_BOW), EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(AdditionType.global(StatIds.LIFE_GAIN_PER_TARGET))
                .withTier("of_rejuvenation", ModValue.of(2), 8, 1000)
                .withTier("of_restoration", ModValue.of(3, 3), 20, 1000)
                .withTier("of_regrowth", ModValue.of(4, 4), 30, 1000)
                .withTier("of_nourishment", ModValue.of(5, 5), 40, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "LifeGainPerTargetClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(AdditionType.global(StatIds.LIFE_GAIN_PER_TARGET))
                .withTier("of_rejuvenation", ModValue.of(2), 8, 1)
                .withTier("of_restoration", ModValue.of(3, 3), 20, 1)
                .withTier("of_regrowth", ModValue.of(4, 4), 30, 1)
                .withTier("of_nourishment", ModValue.of(5, 5), 40, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "LifeLeechBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.CROSS_BOW)), ModPreset.builder(AdditionType.global(StatIds.LEECH_PHYSICAL_DAMAGE_AS_LIFE))
                .withTier("of_the_locust", ModValue.of(6, 6.9), 21, 1000)
                .withTier("of_the_remora", ModValue.of(7, 7.9), 38, 1000)
                .withTier("of_the_lamprey", ModValue.of(8, 8.9), 54, 1000)
                .withTier("of_the_vampire", ModValue.of(9, 9.9), 65, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "LifeLeechClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(AdditionType.global(StatIds.LEECH_PHYSICAL_DAMAGE_AS_LIFE))
                .withTier("of_the_locust", ModValue.of(6, 6.9), 21, 1)
                .withTier("of_the_remora", ModValue.of(7, 7.9), 38, 1)
                .withTier("of_the_lamprey", ModValue.of(8, 8.9), 54, 1)
                .withTier("of_the_vampire", ModValue.of(9, 9.9), 65, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "LifeLeechOneHandMace",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(AdditionType.global(StatIds.LEECH_PHYSICAL_DAMAGE_AS_LIFE))
                .withTier("of_the_locust", ModValue.of(6, 6.9), 21, 1000)
                .withTier("of_the_remora", ModValue.of(7, 7.9), 38, 1000)
                .withTier("of_the_lamprey", ModValue.of(8, 8.9), 54, 1000)
                .withTier("of_the_vampire", ModValue.of(9, 9.9), 65, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "ManaLeechBow",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BOW), EquipmentGen.id(EquipmentGen.CROSS_BOW)), ModPreset.builder(AdditionType.global(StatIds.LEECH_PHYSICAL_DAMAGE_AS_MANA))
                .withTier("of_the_parched", ModValue.of(5, 5.9), 21, 1000)
                .withTier("of_the_arid", ModValue.of(6, 6.9), 38, 1000)
                .withTier("of_the_drought", ModValue.of(7, 7.9), 54, 1000)
                .withTier("of_the_desperate", ModValue.of(8, 8.9), 65, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "ManaLeechClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(AdditionType.global(StatIds.LEECH_PHYSICAL_DAMAGE_AS_MANA))
                .withTier("of_the_parched", ModValue.of(5, 5.9), 21, 1)
                .withTier("of_the_arid", ModValue.of(6, 6.9), 38, 1)
                .withTier("of_the_drought", ModValue.of(7, 7.9), 54, 1)
                .withTier("of_the_desperate", ModValue.of(8, 8.9), 65, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "ManaLeechOneHandMace",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(AdditionType.global(StatIds.LEECH_PHYSICAL_DAMAGE_AS_MANA))
                .withTier("of_the_parched", ModValue.of(5, 5.9), 21, 1000)
                .withTier("of_the_arid", ModValue.of(6, 6.9), 38, 1000)
                .withTier("of_the_drought", ModValue.of(7, 7.9), 54, 1000)
                .withTier("of_the_desperate", ModValue.of(8, 8.9), 65, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_PREFIX, "IncreasedShieldBlockPercentage",
            EquipmentModPreset.from(EquipmentModPreset.Local.BLOCK_CHANCE, List.of(EquipmentGen.id(EquipmentGen.BUCKLER), EquipmentGen.id(EquipmentGen.SHIELD)), ModPreset.builder(ModAdditions.BLOCK_CHANCE_INC_LOCAL)
                .withTier("steadfast", ModValue.of(15), 1, 1000)
                .withTier("unrelenting", ModValue.of(20, 24), 33, 1000)
                .withTier("adamant", ModValue.of(25, 30), 65, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "MaximumChaosResistance",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BUCKLER), EquipmentGen.id(EquipmentGen.SHIELD)), ModPreset.builder(ModAdditions.ADD_MAX_CHAOS_RESISTANCE)
                .withTier("of_regularity", ModValue.of(1), 68, 125)
                .withTier("of_concord", ModValue.of(2, 2), 75, 125)
                .withTier("of_harmony", ModValue.of(3, 3), 81, 125)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "MaximumColdResist",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BUCKLER), EquipmentGen.id(EquipmentGen.SHIELD)), ModPreset.builder(ModAdditions.ADD_MAX_COLD_RESISTANCE)
                .withTier("of_furs", ModValue.of(1), 68, 250)
                .withTier("of_the_tundra", ModValue.of(2, 2), 75, 250)
                .withTier("of_the_mammoth", ModValue.of(3, 3), 81, 250)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "MaximumFireResist",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BUCKLER), EquipmentGen.id(EquipmentGen.SHIELD)), ModPreset.builder(ModAdditions.ADD_MAX_FIRE_RESISTANCE)
                .withTier("of_the_bushfire", ModValue.of(1), 68, 250)
                .withTier("of_the_molten_core", ModValue.of(2, 2), 75, 250)
                .withTier("of_the_solar_storm", ModValue.of(3, 3), 81, 250)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "MaximumLightningResistance",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BUCKLER), EquipmentGen.id(EquipmentGen.SHIELD)), ModPreset.builder(ModAdditions.ADD_MAX_LIGHTNING_RESISTANCE)
                .withTier("of_impedance", ModValue.of(1), 68, 250)
                .withTier("of_shockproofing", ModValue.of(2, 2), 75, 250)
                .withTier("of_the_lightning_rod", ModValue.of(3, 3), 81, 250)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ARMOR_GROUP_MOD_SUFFIX, "MaximumResistances",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.BUCKLER), EquipmentGen.id(EquipmentGen.SHIELD)), ModPreset.builder(ModAdditions.ADD_MAX_ELEMENTAL_RESISTANCE)
                .withTier("of_the_deathless", ModValue.of(1), 75, 125)
                .withTier("of_the_everlasting", ModValue.of(2, 2), 81, 125)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "StunDurationIncreasePercentClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(ModAdditions.STUN_DURATION_INC)
                .withTier("of_impact", ModValue.of(11), 5, 1)
                .withTier("of_dazing", ModValue.of(14, 16), 18, 1)
                .withTier("of_stunning", ModValue.of(17, 19), 30, 1)
                .withTier("of_slamming", ModValue.of(20, 22), 44, 1)
                .withTier("of_staggering", ModValue.of(23, 26), 58, 1)
                .withTier("of_the_concussion", ModValue.of(27, 30), 71, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "StunDurationIncreasePercentOneHandMace",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(ModAdditions.STUN_DURATION_INC)
                .withTier("of_impact", ModValue.of(11), 5, 1000)
                .withTier("of_dazing", ModValue.of(14, 16), 18, 1000)
                .withTier("of_stunning", ModValue.of(17, 19), 30, 1000)
                .withTier("of_slamming", ModValue.of(20, 22), 44, 1000)
                .withTier("of_staggering", ModValue.of(23, 26), 58, 1000)
                .withTier("of_the_concussion", ModValue.of(27, 30), 71, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "StunDamageIncreaseClaw",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.CLAW), EquipmentGen.id(EquipmentGen.DAGGER), EquipmentGen.id(EquipmentGen.FLAIL), EquipmentGen.id(EquipmentGen.ONE_HAND_AXE), EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD), EquipmentGen.id(EquipmentGen.TWO_HAND_AXE), EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), ModPreset.builder(ModAdditions.CAUSES_STUN_BUILDUP_INC)
                .withTier("of_the_pugilist", ModValue.of(21), 5, 1)
                .withTier("of_the_brawler", ModValue.of(31, 40), 20, 1)
                .withTier("of_the_boxer", ModValue.of(41, 50), 30, 1)
                .withTier("of_the_combatant", ModValue.of(51, 60), 44, 1)
                .withTier("of_the_gladiator", ModValue.of(61, 70), 58, 1)
                .withTier("of_the_champion", ModValue.of(71, 80), 74, 1)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "StunDamageIncreaseOneHandMace",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.ONE_HAND_MACE), EquipmentGen.id(EquipmentGen.QUARTERSTAFF), EquipmentGen.id(EquipmentGen.SPEAR), EquipmentGen.id(EquipmentGen.TALISMAN), EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), ModPreset.builder(ModAdditions.CAUSES_STUN_BUILDUP_INC)
                .withTier("of_the_pugilist", ModValue.of(21), 5, 1000)
                .withTier("of_the_brawler", ModValue.of(31, 40), 20, 1000)
                .withTier("of_the_boxer", ModValue.of(41, 50), 30, 1000)
                .withTier("of_the_combatant", ModValue.of(51, 60), 44, 1000)
                .withTier("of_the_gladiator", ModValue.of(61, 70), 58, 1000)
                .withTier("of_the_champion", ModValue.of(71, 80), 74, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "LightRadiusAndManaRegeneration",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.SCEPTRE), EquipmentGen.id(EquipmentGen.STAFF), EquipmentGen.id(EquipmentGen.WAND)), ModPreset.builder(MultiAddition.keyed(AdditionType.global(StatIds.LIGHT_RADIUS), ModAdditions.MANA_REGENERATION_RATE_INC))
                .withTier("of_warmth", Map.of("value", ModValue.of(8), "value_2", ModValue.of(5, 5)), 8, 1000)
                .withTier("of_kindling", Map.of("value", ModValue.of(13, 17), "value_2", ModValue.of(10, 10)), 15, 1000)
                .withTier("of_the_hearth", Map.of("value", ModValue.of(18, 22), "value_2", ModValue.of(15, 15)), 30, 1000)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "IgniteChanceIncrease",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.STAFF), EquipmentGen.id(EquipmentGen.WAND)), ModPreset.builder(AdditionType.global(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_MAGNITUDE_CURSE_FIRE_TAKEN))
                .withTier("of_ignition", ModValue.of(51), 15, 1000)
                .withTier("of_scorching", ModValue.of(61, 70), 30, 1000)
                .withTier("of_incineration", ModValue.of(71, 80), 45, 1000)
                .withTier("of_combustion", ModValue.of(81, 90), 60, 500)
                .withTier("of_conflagration", ModValue.of(91, 100), 75, 500)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "FreezeDamageIncrease",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.STAFF), EquipmentGen.id(EquipmentGen.WAND)), ModPreset.builder(ModAdditions.AILMENT_MAGNITUDE_FREEZE)
                .withTier("of_freezing", ModValue.of(31), 15, 1000)
                .withTier("of_bleakness", ModValue.of(41, 50), 30, 1000)
                .withTier("of_the_glacier", ModValue.of(51, 60), 45, 1000)
                .withTier("of_the_hyperboreal", ModValue.of(61, 70), 60, 500)
                .withTier("of_the_arctic", ModValue.of(71, 80), 75, 500)
                .build()));

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WEAPON_GROUP_MOD_SUFFIX, "ShockChanceIncrease",
            EquipmentModPreset.from(List.of(EquipmentGen.id(EquipmentGen.STAFF), EquipmentGen.id(EquipmentGen.WAND)), ModPreset.builder(AdditionType.global(StatIds.INC_CHANCE_TO_SHOCK))
                .withTier("of_shocking", ModValue.of(51), 15, 1000)
                .withTier("of_zapping", ModValue.of(61, 70), 30, 1000)
                .withTier("of_electrocution", ModValue.of(71, 80), 45, 1000)
                .withTier("of_voltages", ModValue.of(81, 90), 60, 500)
                .withTier("of_the_thunderbolt", ModValue.of(91, 100), 75, 500)
                .build()));

    }

    // ===================== 槽位层（单槽位 或 数值与其他槽位不同） =====================

    private static void s0(BootstrapContext<ItemInstance> bootstrap) {
        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_PREFIX, "IncreasedPhysicalDamageReductionRatingPercent",
            ModPreset.builder(ModAdditions.ARMOR_INC_LOCAL)
                .withTier("reinforced", ModValue.of(10), 2, 1000)
                .withTier("layered", ModValue.of(15, 20), 16, 1000)
                .withTier("lobstered", ModValue.of(21, 26), 33, 1000)
                .withTier("buttressed", ModValue.of(27, 32), 46, 1000)
                .withTier("thickened", ModValue.of(33, 38), 54, 1000)
                .withTier("girded", ModValue.of(39, 44), 65, 1000)
                .withTier("impregnable", ModValue.of(45, 50), 75, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_PREFIX, "EvasionRatingPercent",
            ModPreset.builder(ModAdditions.EVASION_INC_LOCAL)
                .withTier("shade_s", ModValue.of(10), 2, 1000)
                .withTier("ghost_s", ModValue.of(15, 20), 16, 1000)
                .withTier("spectre_s", ModValue.of(21, 26), 33, 1000)
                .withTier("wraith_s", ModValue.of(27, 32), 46, 1000)
                .withTier("phantasm_s", ModValue.of(33, 38), 54, 1000)
                .withTier("nightmare_s", ModValue.of(39, 44), 70, 1000)
                .withTier("mirage_s", ModValue.of(45, 50), 77, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.HEAD_MOD_PREFIX, "ItemFoundRarityIncreasePrefix",
            ModPreset.builder(ModAdditions.FOUND_ITEM_RARITY_INC)
                .withTier("magpie_s", ModValue.of(8), 10, 1000)
                .withTier("collector_s", ModValue.of(12, 15), 29, 1000)
                .withTier("hoarder_s", ModValue.of(16, 19), 47, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_PREFIX, "SpellDamage",
            ModPreset.builder(AdditionType.global(StatIds.INC_SPELL_DAMAGE))
                .withTier("apprentice_s", ModValue.of(3), 1, 1000)
                .withTier("adept_s", ModValue.of(8, 12), 16, 1000)
                .withTier("scholar_s", ModValue.of(13, 17), 33, 1000)
                .withTier("professor_s", ModValue.of(18, 22), 46, 1000)
                .withTier("occultist_s", ModValue.of(23, 26), 60, 1000)
                .withTier("incanter_s", ModValue.of(27, 30), 75, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_PREFIX, "EnergyShieldPercent",
            ModPreset.builder(ModAdditions.ENERGY_INC_LOCAL)
                .withTier("protective", ModValue.of(10), 2, 1000)
                .withTier("strong_willed", ModValue.of(15, 20), 16, 1000)
                .withTier("resolute", ModValue.of(21, 26), 33, 1000)
                .withTier("fearless", ModValue.of(27, 32), 46, 1000)
                .withTier("dauntless", ModValue.of(33, 38), 54, 1000)
                .withTier("indomitable", ModValue.of(39, 44), 65, 1000)
                .withTier("unassailable", ModValue.of(45, 50), 75, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_PREFIX, "MaximumLifeIncreasePercent",
            ModPreset.builder(ModAdditions.MAX_HEALTH_INC)
                .withTier("hopeful", ModValue.of(3), 33, 300)
                .withTier("optimistic", ModValue.of(5, 6), 60, 300)
                .withTier("confident", ModValue.of(7, 8), 75, 300)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_PREFIX, "MaximumManaIncreasePercent",
            ModPreset.builder(AdditionType.global(StatIds.INC_MAX_MANA))
                .withTier("cognizant", ModValue.of(3), 33, 300)
                .withTier("perceptive", ModValue.of(5, 6), 60, 300)
                .withTier("mnemonic", ModValue.of(7, 8), 75, 300)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_PREFIX, "IncreasedAccuracy",
            ModPreset.builder(ModAdditions.ADD_ACCURACY)
                .withTier("precise", ModValue.of(11), 1, 800)
                .withTier("reliable", ModValue.of(33, 60), 11, 800)
                .withTier("focused", ModValue.of(61, 84), 18, 800)
                .withTier("deliberate", ModValue.of(85, 123), 26, 800)
                .withTier("consistent", ModValue.of(124, 167), 36, 800)
                .withTier("steady", ModValue.of(168, 236), 48, 800)
                .withTier("hunter_s", ModValue.of(237, 346), 58, 800)
                .withTier("ranger_s", ModValue.of(347, 450), 67, 400)
                .withTier("amazon_s", ModValue.of(451, 550), 76, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_PREFIX, "BaseSpirit",
            ModPreset.builder(ModAdditions.ADD_MAX_SPIRIT)
                .withTier("lady_s", ModValue.of(30), 16, 500)
                .withTier("baronness", ModValue.of(34, 37), 25, 500)
                .withTier("viscountess", ModValue.of(38, 42), 33, 500)
                .withTier("marchioness", ModValue.of(43, 46), 46, 500)
                .withTier("countess", ModValue.of(47, 50), 54, 400)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHEST_MOD_PREFIX, "BaseSpirit",
            ModPreset.builder(ModAdditions.ADD_MAX_SPIRIT)
                .withTier("lady_s", ModValue.of(30), 16, 500)
                .withTier("baronness", ModValue.of(34, 37), 25, 500)
                .withTier("viscountess", ModValue.of(38, 42), 33, 500)
                .withTier("marchioness", ModValue.of(43, 46), 46, 500)
                .withTier("countess", ModValue.of(47, 50), 54, 400)
                .withTier("duchess", ModValue.of(51, 53), 60, 300)
                .withTier("princess", ModValue.of(54, 56), 65, 200)
                .withTier("queen_s", ModValue.of(57, 61), 78, 100)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_PREFIX, "IncreasedEnergyShield",
            ModPreset.builder(ModAdditions.ADD_ENERGY_LOCAL)
                .withTier("shining", ModValue.of(8), 1, 1000)
                .withTier("glimmering", ModValue.of(15, 20), 11, 1000)
                .withTier("glittering", ModValue.of(21, 24), 16, 1000)
                .withTier("glowing", ModValue.of(25, 33), 25, 1000)
                .withTier("radiating", ModValue.of(34, 41), 33, 1000)
                .withTier("pulsing", ModValue.of(42, 51), 46, 1000)
                .withTier("blazing", ModValue.of(52, 61), 54, 1000)
                .withTier("dazzling", ModValue.of(62, 70), 65, 1000)
                .withTier("scintillating", ModValue.of(71, 79), 70, 1000)
                .withTier("incandescent", ModValue.of(80, 89), 80, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_PREFIX, "IncreasedLife",
            ModPreset.builder(ModAdditions.ADD_MAX_HEALTH)
                .withTier("hale", ModValue.of(10), 1, 1000)
                .withTier("healthy", ModValue.of(20, 29), 6, 1000)
                .withTier("sanguine", ModValue.of(30, 39), 16, 1000)
                .withTier("stalwart", ModValue.of(40, 59), 24, 1000)
                .withTier("stout", ModValue.of(60, 69), 33, 1000)
                .withTier("robust", ModValue.of(70, 84), 38, 1000)
                .withTier("rotund", ModValue.of(85, 99), 46, 1000)
                .withTier("virile", ModValue.of(100, 119), 54, 1000)
                .withTier("athlete_s", ModValue.of(120, 149), 60, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BELT_MOD_PREFIX, "IncreasedLife",
            ModPreset.builder(ModAdditions.ADD_MAX_HEALTH)
                .withTier("hale", ModValue.of(10), 1, 1000)
                .withTier("healthy", ModValue.of(20, 29), 6, 1000)
                .withTier("sanguine", ModValue.of(30, 39), 16, 1000)
                .withTier("stalwart", ModValue.of(40, 59), 24, 1000)
                .withTier("stout", ModValue.of(60, 69), 33, 1000)
                .withTier("robust", ModValue.of(70, 84), 38, 1000)
                .withTier("rotund", ModValue.of(85, 99), 46, 1000)
                .withTier("virile", ModValue.of(100, 119), 54, 1000)
                .withTier("athlete_s", ModValue.of(120, 149), 60, 1000)
                .withTier("fecund", ModValue.of(150, 174), 65, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.HEAD_MOD_PREFIX, "IncreasedLife",
            ModPreset.builder(ModAdditions.ADD_MAX_HEALTH)
                .withTier("hale", ModValue.of(10), 1, 1000)
                .withTier("healthy", ModValue.of(20, 29), 6, 1000)
                .withTier("sanguine", ModValue.of(30, 39), 16, 1000)
                .withTier("stalwart", ModValue.of(40, 59), 24, 1000)
                .withTier("stout", ModValue.of(60, 69), 33, 1000)
                .withTier("robust", ModValue.of(70, 84), 38, 1000)
                .withTier("rotund", ModValue.of(85, 99), 46, 1000)
                .withTier("virile", ModValue.of(100, 119), 54, 1000)
                .withTier("athlete_s", ModValue.of(120, 149), 60, 1000)
                .withTier("fecund", ModValue.of(150, 174), 65, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHEST_MOD_PREFIX, "IncreasedLife",
            ModPreset.builder(ModAdditions.ADD_MAX_HEALTH)
                .withTier("hale", ModValue.of(10), 1, 1000)
                .withTier("healthy", ModValue.of(20, 29), 6, 1000)
                .withTier("sanguine", ModValue.of(30, 39), 16, 1000)
                .withTier("stalwart", ModValue.of(40, 59), 24, 1000)
                .withTier("stout", ModValue.of(60, 69), 33, 1000)
                .withTier("robust", ModValue.of(70, 84), 38, 1000)
                .withTier("rotund", ModValue.of(85, 99), 46, 1000)
                .withTier("virile", ModValue.of(100, 119), 54, 1000)
                .withTier("athlete_s", ModValue.of(120, 149), 60, 1000)
                .withTier("fecund", ModValue.of(150, 174), 65, 1000)
                .withTier("vigorous", ModValue.of(175, 189), 70, 1000)
                .withTier("rapturous", ModValue.of(190, 199), 75, 1000)
                .withTier("prime", ModValue.of(200, 214), 80, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_PREFIX, "IncreasedLife",
            ModPreset.builder(ModAdditions.ADD_MAX_HEALTH)
                .withTier("hale", ModValue.of(10), 1, 1000)
                .withTier("healthy", ModValue.of(20, 29), 6, 1000)
                .withTier("sanguine", ModValue.of(30, 39), 16, 1000)
                .withTier("stalwart", ModValue.of(40, 59), 24, 1000)
                .withTier("stout", ModValue.of(60, 69), 33, 1000)
                .withTier("robust", ModValue.of(70, 84), 38, 1000)
                .withTier("rotund", ModValue.of(85, 99), 46, 1000)
                .withTier("virile", ModValue.of(100, 119), 54, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_PREFIX, "IncreasedMana",
            ModPreset.builder(ModAdditions.ADD_MAX_MANA)
                .withTier("beryl", ModValue.of(10), 1, 1000)
                .withTier("cobalt", ModValue.of(15, 24), 6, 1000)
                .withTier("azure", ModValue.of(25, 34), 16, 1000)
                .withTier("teal", ModValue.of(35, 54), 25, 1000)
                .withTier("cerulean", ModValue.of(55, 64), 33, 1000)
                .withTier("aqua", ModValue.of(65, 79), 38, 1000)
                .withTier("opalescent", ModValue.of(80, 89), 46, 1000)
                .withTier("gentian", ModValue.of(90, 104), 54, 1000)
                .withTier("chalybeous", ModValue.of(105, 124), 60, 1000)
                .withTier("mazarine", ModValue.of(125, 149), 65, 1000)
                .withTier("blue", ModValue.of(150, 164), 70, 1000)
                .withTier("zaffre", ModValue.of(165, 179), 75, 1000)
                .withTier("ultramarine", ModValue.of(180, 189), 82, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BELT_MOD_PREFIX, "IncreasedMana",
            ModPreset.builder(ModAdditions.ADD_MAX_MANA)
                .withTier("beryl", ModValue.of(10), 1, 1000)
                .withTier("cobalt", ModValue.of(15, 24), 6, 1000)
                .withTier("azure", ModValue.of(25, 34), 16, 1000)
                .withTier("teal", ModValue.of(35, 54), 25, 1000)
                .withTier("cerulean", ModValue.of(55, 64), 33, 1000)
                .withTier("aqua", ModValue.of(65, 79), 38, 1000)
                .withTier("opalescent", ModValue.of(80, 89), 46, 1000)
                .withTier("gentian", ModValue.of(90, 104), 54, 1000)
                .withTier("chalybeous", ModValue.of(105, 124), 60, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_PREFIX, "IncreasedMana",
            ModPreset.builder(ModAdditions.ADD_MAX_MANA)
                .withTier("beryl", ModValue.of(10), 1, 1000)
                .withTier("cobalt", ModValue.of(15, 24), 6, 1000)
                .withTier("azure", ModValue.of(25, 34), 16, 1000)
                .withTier("teal", ModValue.of(35, 54), 25, 1000)
                .withTier("cerulean", ModValue.of(55, 64), 33, 1000)
                .withTier("aqua", ModValue.of(65, 79), 38, 1000)
                .withTier("opalescent", ModValue.of(80, 89), 46, 1000)
                .withTier("gentian", ModValue.of(90, 104), 54, 1000)
                .withTier("chalybeous", ModValue.of(105, 124), 60, 1000)
                .withTier("mazarine", ModValue.of(125, 149), 65, 1000)
                .withTier("blue", ModValue.of(150, 164), 70, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.HEAD_MOD_PREFIX, "IncreasedMana",
            ModPreset.builder(ModAdditions.ADD_MAX_MANA)
                .withTier("beryl", ModValue.of(10), 1, 1000)
                .withTier("cobalt", ModValue.of(15, 24), 6, 1000)
                .withTier("azure", ModValue.of(25, 34), 16, 1000)
                .withTier("teal", ModValue.of(35, 54), 25, 1000)
                .withTier("cerulean", ModValue.of(55, 64), 33, 1000)
                .withTier("aqua", ModValue.of(65, 79), 38, 1000)
                .withTier("opalescent", ModValue.of(80, 89), 46, 1000)
                .withTier("gentian", ModValue.of(90, 104), 54, 1000)
                .withTier("chalybeous", ModValue.of(105, 124), 60, 1000)
                .withTier("mazarine", ModValue.of(125, 149), 65, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_PREFIX, "IncreasedMana",
            ModPreset.builder(ModAdditions.ADD_MAX_MANA)
                .withTier("beryl", ModValue.of(10), 1, 1000)
                .withTier("cobalt", ModValue.of(15, 24), 6, 1000)
                .withTier("azure", ModValue.of(25, 34), 16, 1000)
                .withTier("teal", ModValue.of(35, 54), 25, 1000)
                .withTier("cerulean", ModValue.of(55, 64), 33, 1000)
                .withTier("aqua", ModValue.of(65, 79), 38, 1000)
                .withTier("opalescent", ModValue.of(80, 89), 46, 1000)
                .withTier("gentian", ModValue.of(90, 104), 54, 1000)
                .withTier("chalybeous", ModValue.of(105, 124), 60, 1000)
                .withTier("mazarine", ModValue.of(125, 149), 65, 1000)
                .withTier("blue", ModValue.of(150, 164), 70, 1000)
                .withTier("zaffre", ModValue.of(165, 179), 75, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.STAFF_MOD_PREFIX, "IncreasedMana",
            ModPreset.builder(ModAdditions.ADD_MAX_MANA)
                .withTier("beryl", ModValue.of(20), 1, 1000)
                .withTier("cobalt", ModValue.of(29, 48), 6, 1000)
                .withTier("azure", ModValue.of(49, 68), 16, 1000)
                .withTier("sapphire", ModValue.of(69, 108), 25, 1000)
                .withTier("cerulean", ModValue.of(109, 128), 33, 1000)
                .withTier("aqua", ModValue.of(129, 158), 38, 1000)
                .withTier("opalescent", ModValue.of(159, 178), 46, 1000)
                .withTier("gentian", ModValue.of(179, 208), 54, 1000)
                .withTier("chalybeous", ModValue.of(209, 248), 60, 1000)
                .withTier("mazarine", ModValue.of(249, 298), 65, 1000)
                .withTier("blue", ModValue.of(299, 328), 70, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TRAP_MOD_PREFIX, "IncreasedMana",
            ModPreset.builder(ModAdditions.ADD_MAX_MANA)
                .withTier("beryl", ModValue.of(10), 1, 1)
                .withTier("cobalt", ModValue.of(15, 24), 6, 1)
                .withTier("azure", ModValue.of(25, 34), 16, 1)
                .withTier("teal", ModValue.of(35, 54), 25, 1)
                .withTier("cerulean", ModValue.of(55, 64), 33, 1)
                .withTier("aqua", ModValue.of(65, 79), 38, 1)
                .withTier("opalescent", ModValue.of(80, 89), 46, 1)
                .withTier("gentian", ModValue.of(90, 104), 54, 1)
                .withTier("chalybeous", ModValue.of(105, 124), 60, 1)
                .withTier("mazarine", ModValue.of(125, 149), 65, 1)
                .withTier("blue", ModValue.of(150, 164), 70, 1)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_SUFFIX, "LifeRegeneration",
            ModPreset.builder(ModAdditions.ADD_HEALTH_REGENERATION_PER_SECOND)
                .withTier("of_the_newt", ModValue.of(1), 1, 1000)
                .withTier("of_the_lizard", ModValue.of(2.1, 3), 5, 1000)
                .withTier("of_the_flatworm", ModValue.of(3.1, 4), 11, 1000)
                .withTier("of_the_starfish", ModValue.of(4.1, 6), 17, 1000)
                .withTier("of_the_hydra", ModValue.of(6.1, 9), 26, 1000)
                .withTier("of_the_troll", ModValue.of(9.1, 13), 35, 1000)
                .withTier("of_convalescence", ModValue.of(13.1, 18), 47, 1000)
                .withTier("of_recuperation", ModValue.of(18.1, 23), 58, 1000)
                .withTier("of_resurgence", ModValue.of(23.1, 29), 68, 1000)
                .withTier("of_immortality", ModValue.of(29.1, 33), 75, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_SUFFIX, "LifeRegeneration",
            ModPreset.builder(ModAdditions.ADD_HEALTH_REGENERATION_PER_SECOND)
                .withTier("of_the_newt", ModValue.of(1), 1, 1000)
                .withTier("of_the_lizard", ModValue.of(2.1, 3), 5, 1000)
                .withTier("of_the_flatworm", ModValue.of(3.1, 4), 11, 1000)
                .withTier("of_the_starfish", ModValue.of(4.1, 6), 17, 1000)
                .withTier("of_the_hydra", ModValue.of(6.1, 9), 26, 1000)
                .withTier("of_the_troll", ModValue.of(9.1, 13), 35, 1000)
                .withTier("of_convalescence", ModValue.of(13.1, 18), 47, 1000)
                .withTier("of_recuperation", ModValue.of(18.1, 23), 58, 1000)
                .withTier("of_resurgence", ModValue.of(23.1, 29), 68, 1000)
                .withTier("of_immortality", ModValue.of(29.1, 33), 75, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BELT_MOD_SUFFIX, "LifeRegeneration",
            ModPreset.builder(ModAdditions.ADD_HEALTH_REGENERATION_PER_SECOND)
                .withTier("of_the_newt", ModValue.of(1), 1, 1000)
                .withTier("of_the_lizard", ModValue.of(2.1, 3), 5, 1000)
                .withTier("of_the_flatworm", ModValue.of(3.1, 4), 11, 1000)
                .withTier("of_the_starfish", ModValue.of(4.1, 6), 17, 1000)
                .withTier("of_the_hydra", ModValue.of(6.1, 9), 26, 1000)
                .withTier("of_the_troll", ModValue.of(9.1, 13), 35, 1000)
                .withTier("of_convalescence", ModValue.of(13.1, 18), 47, 1000)
                .withTier("of_recuperation", ModValue.of(18.1, 23), 58, 1000)
                .withTier("of_resurgence", ModValue.of(23.1, 29), 68, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHEST_MOD_SUFFIX, "LifeRegeneration",
            ModPreset.builder(ModAdditions.ADD_HEALTH_REGENERATION_PER_SECOND)
                .withTier("of_the_newt", ModValue.of(1), 1, 1000)
                .withTier("of_the_lizard", ModValue.of(2.1, 3), 5, 1000)
                .withTier("of_the_flatworm", ModValue.of(3.1, 4), 11, 1000)
                .withTier("of_the_starfish", ModValue.of(4.1, 6), 17, 1000)
                .withTier("of_the_hydra", ModValue.of(6.1, 9), 26, 1000)
                .withTier("of_the_troll", ModValue.of(9.1, 13), 35, 1000)
                .withTier("of_convalescence", ModValue.of(13.1, 18), 47, 1000)
                .withTier("of_recuperation", ModValue.of(18.1, 23), 58, 1000)
                .withTier("of_resurgence", ModValue.of(23.1, 29), 68, 1000)
                .withTier("of_immortality", ModValue.of(29.1, 33), 75, 1000)
                .withTier("of_the_phoenix", ModValue.of(33.1, 36), 81, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_SUFFIX, "LifeRegeneration",
            ModPreset.builder(ModAdditions.ADD_HEALTH_REGENERATION_PER_SECOND)
                .withTier("of_the_newt", ModValue.of(1), 1, 1000)
                .withTier("of_the_lizard", ModValue.of(2.1, 3), 5, 1000)
                .withTier("of_the_flatworm", ModValue.of(3.1, 4), 11, 1000)
                .withTier("of_the_starfish", ModValue.of(4.1, 6), 17, 1000)
                .withTier("of_the_hydra", ModValue.of(6.1, 9), 26, 1000)
                .withTier("of_the_troll", ModValue.of(9.1, 13), 35, 1000)
                .withTier("of_convalescence", ModValue.of(13.1, 18), 47, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_SUFFIX, "IncreasedCastSpeed",
            ModPreset.builder(AdditionType.global(StatIds.INC_CAST_SPEED))
                .withTier("of_talent", ModValue.of(9), 1, 1)
                .withTier("of_nimbleness", ModValue.of(13, 15), 18, 1)
                .withTier("of_expertise", ModValue.of(16, 18), 35, 1)
                .withTier("of_sortilege", ModValue.of(19, 21), 51, 1)
                .withTier("of_legerdemain", ModValue.of(22, 24), 60, 1)
                .withTier("of_prestidigitation", ModValue.of(25, 28), 66, 1)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_SUFFIX, "IncreasedCastSpeed",
            ModPreset.builder(AdditionType.global(StatIds.INC_CAST_SPEED))
                .withTier("of_talent", ModValue.of(9), 1, 1000)
                .withTier("of_nimbleness", ModValue.of(13, 16), 15, 1000)
                .withTier("of_expertise", ModValue.of(17, 20), 30, 1000)
                .withTier("of_sortilege", ModValue.of(21, 24), 45, 1000)
                .withTier("of_legerdemain", ModValue.of(25, 28), 60, 1000)
                .withTier("of_prestidigitation", ModValue.of(29, 32), 70, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_SUFFIX, "IncreasedCastSpeed",
            ModPreset.builder(AdditionType.global(StatIds.INC_CAST_SPEED))
                .withTier("of_talent", ModValue.of(9), 1, 1)
                .withTier("of_nimbleness", ModValue.of(13, 15), 18, 1)
                .withTier("of_expertise", ModValue.of(16, 18), 35, 1)
                .withTier("of_sortilege", ModValue.of(19, 21), 51, 1)
                .withTier("of_legerdemain", ModValue.of(22, 24), 60, 1)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_SUFFIX, "IncreasedCastSpeed",
            ModPreset.builder(AdditionType.global(StatIds.INC_CAST_SPEED))
                .withTier("of_talent", ModValue.of(5), 6, 1000)
                .withTier("of_nimbleness", ModValue.of(9, 12), 21, 1000)
                .withTier("of_expertise", ModValue.of(13, 16), 36, 1000)
                .withTier("of_sortilege", ModValue.of(17, 20), 56, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.STAFF_MOD_SUFFIX, "IncreasedCastSpeed",
            ModPreset.builder(AdditionType.global(StatIds.INC_CAST_SPEED))
                .withTier("of_talent", ModValue.of(14), 1, 1000)
                .withTier("of_nimbleness", ModValue.of(20, 25), 15, 1000)
                .withTier("of_expertise", ModValue.of(26, 31), 30, 1000)
                .withTier("of_sortilege", ModValue.of(32, 37), 45, 1000)
                .withTier("of_legerdemain", ModValue.of(38, 43), 60, 1000)
                .withTier("of_prestidigitation", ModValue.of(44, 49), 70, 500)
                .withTier("of_finesse", ModValue.of(50, 52), 80, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WAND_MOD_SUFFIX, "IncreasedCastSpeed",
            ModPreset.builder(AdditionType.global(StatIds.INC_CAST_SPEED))
                .withTier("of_talent", ModValue.of(9), 1, 1000)
                .withTier("of_nimbleness", ModValue.of(13, 16), 15, 1000)
                .withTier("of_expertise", ModValue.of(17, 20), 30, 1000)
                .withTier("of_sortilege", ModValue.of(21, 24), 45, 1000)
                .withTier("of_legerdemain", ModValue.of(25, 28), 60, 1000)
                .withTier("of_prestidigitation", ModValue.of(29, 32), 70, 500)
                .withTier("of_finesse", ModValue.of(33, 35), 80, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_SUFFIX, "CriticalStrikeMultiplier",
            ModPreset.builder(ModAdditions.ADD_CRIT_DAMAGE)
                .withTier("of_ire", ModValue.of(10), 8, 1000)
                .withTier("of_anger", ModValue.of(15, 19), 21, 1000)
                .withTier("of_rage", ModValue.of(20, 24), 31, 1000)
                .withTier("of_fury", ModValue.of(25, 29), 45, 500)
                .withTier("of_ferocity", ModValue.of(30, 34), 59, 250)
                .withTier("of_destruction", ModValue.of(35, 39), 74, 125)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_SUFFIX, "CriticalStrikeMultiplier",
            ModPreset.builder(ModAdditions.ADD_CRIT_DAMAGE)
                .withTier("of_ire", ModValue.of(10), 8, 1000)
                .withTier("of_anger", ModValue.of(15, 19), 21, 1000)
                .withTier("of_rage", ModValue.of(20, 24), 31, 1000)
                .withTier("of_fury", ModValue.of(25, 29), 45, 500)
                .withTier("of_ferocity", ModValue.of(30, 34), 59, 250)
                .withTier("of_destruction", ModValue.of(35, 39), 74, 125)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_SUFFIX, "CriticalStrikeMultiplier",
            ModPreset.builder(ModAdditions.ADD_CRIT_DAMAGE)
                .withTier("of_ire", ModValue.of(10), 8, 1000)
                .withTier("of_anger", ModValue.of(15, 19), 21, 1000)
                .withTier("of_rage", ModValue.of(20, 24), 30, 1000)
                .withTier("of_fury", ModValue.of(25, 29), 44, 500)
                .withTier("of_ferocity", ModValue.of(30, 34), 59, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.LEGS_MOD_SUFFIX, "CriticalStrikeMultiplier",
            ModPreset.builder(ModAdditions.ADD_CRIT_DAMAGE)
                .withTier("of_ire", ModValue.of(10), 8, 1000)
                .withTier("of_anger", ModValue.of(15, 19), 21, 1000)
                .withTier("of_rage", ModValue.of(20, 24), 31, 1000)
                .withTier("of_fury", ModValue.of(25, 29), 45, 500)
                .withTier("of_ferocity", ModValue.of(30, 34), 59, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.STAFF_MOD_SUFFIX, "CriticalStrikeMultiplier",
            ModPreset.builder(ModAdditions.ADD_CRIT_DAMAGE)
                .withTier("of_ire", ModValue.of(15), 8, 1000)
                .withTier("of_anger", ModValue.of(23, 29), 21, 1000)
                .withTier("of_rage", ModValue.of(30, 36), 30, 1000)
                .withTier("of_fury", ModValue.of(38, 44), 44, 500)
                .withTier("of_ferocity", ModValue.of(45, 51), 59, 250)
                .withTier("of_destruction", ModValue.of(53, 59), 73, 125)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TRAP_MOD_SUFFIX, "CriticalStrikeMultiplier",
            ModPreset.builder(ModAdditions.ADD_CRIT_DAMAGE)
                .withTier("of_ire", ModValue.of(10), 8, 1)
                .withTier("of_anger", ModValue.of(15, 19), 21, 1)
                .withTier("of_rage", ModValue.of(20, 24), 30, 1)
                .withTier("of_fury", ModValue.of(25, 29), 44, 1)
                .withTier("of_ferocity", ModValue.of(30, 34), 59, 1)
                .withTier("of_destruction", ModValue.of(35, 39), 73, 1)
                .build());

    }

    private static void s1(BootstrapContext<ItemInstance> bootstrap) {
        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_SUFFIX, "CriticalStrikeChanceIncrease",
            ModPreset.builder(ModAdditions.CRIT_CHANCE_INC)
                .withTier("of_menace", ModValue.of(10), 5, 1000)
                .withTier("of_havoc", ModValue.of(15, 19), 20, 1000)
                .withTier("of_disaster", ModValue.of(20, 24), 30, 1000)
                .withTier("of_calamity", ModValue.of(25, 29), 44, 500)
                .withTier("of_ruin", ModValue.of(30, 34), 58, 250)
                .withTier("of_unmaking", ModValue.of(35, 38), 72, 125)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_SUFFIX, "CriticalStrikeChanceIncrease",
            ModPreset.builder(ModAdditions.CRIT_CHANCE_INC)
                .withTier("of_menace", ModValue.of(10), 5, 1000)
                .withTier("of_havoc", ModValue.of(15, 19), 20, 1000)
                .withTier("of_disaster", ModValue.of(20, 24), 30, 1000)
                .withTier("of_calamity", ModValue.of(25, 29), 44, 500)
                .withTier("of_ruin", ModValue.of(30, 34), 58, 250)
                .withTier("of_unmaking", ModValue.of(35, 38), 72, 125)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.HEAD_MOD_SUFFIX, "CriticalStrikeChanceIncrease",
            ModPreset.builder(ModAdditions.CRIT_CHANCE_INC)
                .withTier("of_menace", ModValue.of(10), 5, 1000)
                .withTier("of_havoc", ModValue.of(15, 19), 20, 1000)
                .withTier("of_disaster", ModValue.of(20, 24), 30, 1000)
                .withTier("of_calamity", ModValue.of(25, 29), 44, 500)
                .withTier("of_ruin", ModValue.of(30, 34), 58, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_SUFFIX, "CriticalStrikeChanceIncrease",
            ModPreset.builder(ModAdditions.CRIT_CHANCE_INC)
                .withTier("of_menace", ModValue.of(10), 11, 1000)
                .withTier("of_havoc", ModValue.of(15, 19), 21, 1000)
                .withTier("of_disaster", ModValue.of(20, 24), 28, 1000)
                .withTier("of_calamity", ModValue.of(25, 29), 41, 500)
                .withTier("of_ruin", ModValue.of(30, 34), 59, 250)
                .withTier("of_unmaking", ModValue.of(35, 38), 76, 125)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TRAP_MOD_SUFFIX, "CriticalStrikeChanceIncrease",
            ModPreset.builder(ModAdditions.CRIT_CHANCE_INC)
                .withTier("of_menace", ModValue.of(10), 11, 1)
                .withTier("of_havoc", ModValue.of(20, 39), 21, 1)
                .withTier("of_disaster", ModValue.of(40, 59), 28, 1)
                .withTier("of_calamity", ModValue.of(60, 79), 41, 1)
                .withTier("of_ruin", ModValue.of(80, 99), 59, 1)
                .withTier("of_unmaking", ModValue.of(100, 109), 76, 1)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_SUFFIX, "ManaRegeneration",
            ModPreset.builder(ModAdditions.MANA_REGENERATION_RATE_INC)
                .withTier("of_excitement", ModValue.of(10), 1, 1000)
                .withTier("of_joy", ModValue.of(20, 29), 18, 1000)
                .withTier("of_elation", ModValue.of(30, 39), 29, 1000)
                .withTier("of_bliss", ModValue.of(40, 49), 42, 1000)
                .withTier("of_euphoria", ModValue.of(50, 59), 55, 1000)
                .withTier("of_nirvana", ModValue.of(60, 69), 79, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.STAFF_MOD_SUFFIX, "ManaRegeneration",
            ModPreset.builder(ModAdditions.MANA_REGENERATION_RATE_INC)
                .withTier("of_excitement", ModValue.of(15), 1, 1000)
                .withTier("of_joy", ModValue.of(30, 44), 18, 1000)
                .withTier("of_elation", ModValue.of(45, 59), 29, 1000)
                .withTier("of_bliss", ModValue.of(60, 74), 42, 1000)
                .withTier("of_euphoria", ModValue.of(75, 89), 55, 1000)
                .withTier("of_nirvana", ModValue.of(90, 104), 79, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TRAP_MOD_SUFFIX, "ManaRegeneration",
            ModPreset.builder(ModAdditions.MANA_REGENERATION_RATE_INC)
                .withTier("of_excitement", ModValue.of(10), 1, 1)
                .withTier("of_joy", ModValue.of(20, 29), 18, 1)
                .withTier("of_elation", ModValue.of(30, 39), 29, 1)
                .withTier("of_bliss", ModValue.of(40, 49), 42, 1)
                .withTier("of_euphoria", ModValue.of(50, 59), 55, 1)
                .withTier("of_nirvana", ModValue.of(60, 69), 79, 1)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_SUFFIX, "DamageTakenGainedAsLife",
            ModPreset.builder(AdditionType.global(StatIds.DAMAGE_TAKEN_GAINED_AS_LIFE))
                .withTier("of_mending", ModValue.of(10), 30, 500)
                .withTier("of_bandaging", ModValue.of(13, 15), 44, 500)
                .withTier("of_stitching", ModValue.of(16, 18), 56, 500)
                .withTier("of_suturing", ModValue.of(19, 21), 68, 500)
                .withTier("of_fleshbinding", ModValue.of(22, 24), 79, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_SUFFIX, "PercentDamageGoesToMana",
            ModPreset.builder(AdditionType.global(StatIds.PERCENT_DAMAGE_GOES_TO_MANA))
                .withTier("of_reprieve", ModValue.of(10), 31, 500)
                .withTier("of_solace", ModValue.of(13, 15), 45, 500)
                .withTier("of_tranquility", ModValue.of(16, 18), 57, 500)
                .withTier("of_serenity", ModValue.of(19, 21), 69, 500)
                .withTier("of_zen", ModValue.of(22, 24), 80, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_SUFFIX, "IncreaseSocketedGemLevel",
            ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_combat", ModValue.of(1), 5, 500)
                .withTier("of_the_taskmaster", ModValue.of(1, 1), 5, 500)
                .withTier("of_the_archer", ModValue.of(1, 1), 5, 500)
                .withTier("of_the_mage", ModValue.of(1, 1), 10, 500)
                .withTier("of_dueling", ModValue.of(2, 2), 41, 250)
                .withTier("of_the_despot", ModValue.of(2, 2), 41, 250)
                .withTier("of_the_fletcher", ModValue.of(2, 2), 41, 250)
                .withTier("of_the_enchanter", ModValue.of(2, 2), 41, 250)
                .withTier("of_battle", ModValue.of(3, 3), 75, 100)
                .withTier("of_the_overseer", ModValue.of(3, 3), 75, 100)
                .withTier("of_the_sharpshooter", ModValue.of(3, 3), 75, 100)
                .withTier("of_the_sorcerer", ModValue.of(3, 3), 75, 100)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BOW_MOD_SUFFIX, "IncreaseSocketedGemLevel",
            ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_the_fletcher", ModValue.of(1), 18, 750)
                .withTier("of_the_sharpshooter", ModValue.of(2, 2), 36, 500)
                .withTier("of_the_marksman", ModValue.of(3, 3), 55, 250)
                .withTier("of_the_sniper", ModValue.of(4, 4), 81, 100)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CROSSBOW_MOD_SUFFIX, "IncreaseSocketedGemLevel",
            ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_the_fletcher", ModValue.of(2), 18, 750)
                .withTier("of_the_sharpshooter", ModValue.of(3, 3), 36, 500)
                .withTier("of_the_marksman", ModValue.of(4, 4), 55, 250)
                .withTier("of_the_sniper", ModValue.of(5, 5), 81, 100)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_SUFFIX, "IncreaseSocketedGemLevel",
            ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_the_mage", ModValue.of(1), 10, 500)
                .withTier("of_the_enchanter", ModValue.of(2, 2), 41, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.HEAD_MOD_SUFFIX, "IncreaseSocketedGemLevel",
            ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_the_taskmaster", ModValue.of(1), 5, 500)
                .withTier("of_the_despot", ModValue.of(2, 2), 41, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.LEGS_MOD_SUFFIX, "IncreaseSocketedGemLevel",
            ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_combat", ModValue.of(1), 5, 500)
                .withTier("of_dueling", ModValue.of(2, 2), 41, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ONE_HAND_MACE_MOD_SUFFIX, "IncreaseSocketedGemLevel",
            ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_dueling", ModValue.of(1), 18, 750)
                .withTier("of_conflict", ModValue.of(2, 2), 36, 500)
                .withTier("of_battle", ModValue.of(3, 3), 55, 250)
                .withTier("of_war", ModValue.of(4, 4), 81, 100)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_SUFFIX, "IncreaseSocketedGemLevel",
            ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_the_archer", ModValue.of(1), 5, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_SUFFIX, "IncreaseSocketedGemLevel",
            ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_the_taskmaster", ModValue.of(1), 2, 750)
                .withTier("of_the_despot", ModValue.of(2, 2), 25, 500)
                .withTier("of_the_overseer", ModValue.of(3, 3), 55, 250)
                .withTier("of_the_slavedriver", ModValue.of(4, 4), 78, 100)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SPEAR_MOD_SUFFIX, "IncreaseSocketedGemLevel",
            ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_dueling", ModValue.of(1), 18, 375)
                .withTier("of_the_fletcher", ModValue.of(1, 1), 18, 375)
                .withTier("of_conflict", ModValue.of(2, 2), 36, 250)
                .withTier("of_the_sharpshooter", ModValue.of(2, 2), 36, 250)
                .withTier("of_battle", ModValue.of(3, 3), 55, 125)
                .withTier("of_the_marksman", ModValue.of(3, 3), 55, 125)
                .withTier("of_war", ModValue.of(4, 4), 81, 50)
                .withTier("of_the_sniper", ModValue.of(4, 4), 81, 50)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.STAFF_MOD_SUFFIX, "IncreaseSocketedGemLevel",
            ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_anarchy", ModValue.of(1), 2, 1000)
                .withTier("of_snow", ModValue.of(1, 1), 2, 1000)
                .withTier("of_coals", ModValue.of(1, 1), 2, 1000)
                .withTier("of_sparks", ModValue.of(1, 1), 2, 1000)
                .withTier("of_agony", ModValue.of(1, 1), 2, 1000)
                .withTier("of_the_mage", ModValue.of(2, 2), 5, 200)
                .withTier("of_turmoil", ModValue.of(2, 2), 18, 750)
                .withTier("of_sleet", ModValue.of(2, 2), 18, 750)
                .withTier("of_cinders", ModValue.of(2, 2), 18, 750)
                .withTier("of_static", ModValue.of(2, 2), 18, 750)
                .withTier("of_suffering", ModValue.of(2, 2), 18, 750)
                .withTier("of_the_enchanter", ModValue.of(3, 3), 25, 150)
                .withTier("of_ruin", ModValue.of(3, 4), 36, 500)
                .withTier("of_ice", ModValue.of(3, 4), 36, 500)
                .withTier("of_flames", ModValue.of(3, 4), 36, 500)
                .withTier("of_electricity", ModValue.of(3, 4), 36, 500)
                .withTier("of_torment", ModValue.of(3, 4), 36, 500)
                .withTier("of_the_evoker", ModValue.of(4, 4), 55, 100)
                .withTier("of_havoc", ModValue.of(5, 6), 55, 250)
                .withTier("of_rime", ModValue.of(5, 6), 55, 250)
                .withTier("of_immolation", ModValue.of(5, 6), 55, 250)
                .withTier("of_voltage", ModValue.of(5, 6), 55, 250)
                .withTier("of_desolation", ModValue.of(5, 6), 55, 250)
                .withTier("of_the_sorcerer", ModValue.of(5, 6), 78, 50)
                .withTier("of_armageddon", ModValue.of(7, 7), 81, 100)
                .withTier("of_frostbite", ModValue.of(7, 7), 81, 100)
                .withTier("of_inferno", ModValue.of(7, 7), 81, 100)
                .withTier("of_thunder", ModValue.of(7, 7), 81, 100)
                .withTier("of_grief", ModValue.of(7, 7), 81, 100)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TRAP_MOD_SUFFIX, "IncreaseSocketedGemLevel",
            ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_explosives", ModValue.of(1), 2, 1)
                .withTier("of_shrapnel", ModValue.of(2, 2), 18, 1)
                .withTier("of_sabotage", ModValue.of(3, 3), 36, 1)
                .withTier("of_detonation", ModValue.of(4, 4), 55, 1)
                .withTier("of_pyrotechnics", ModValue.of(5, 5), 81, 1)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WAND_MOD_SUFFIX, "IncreaseSocketedGemLevel",
            ModPreset.builder(AdditionType.global(StatIds.INC_SOCKETED_GEM_LEVEL))
                .withTier("of_anarchy", ModValue.of(1), 2, 1000)
                .withTier("of_snow", ModValue.of(1, 1), 2, 1000)
                .withTier("of_coals", ModValue.of(1, 1), 2, 1000)
                .withTier("of_sparks", ModValue.of(1, 1), 2, 1000)
                .withTier("of_agony", ModValue.of(1, 1), 2, 1000)
                .withTier("of_the_mage", ModValue.of(1, 1), 5, 200)
                .withTier("of_turmoil", ModValue.of(2, 2), 18, 750)
                .withTier("of_sleet", ModValue.of(2, 2), 18, 750)
                .withTier("of_cinders", ModValue.of(2, 2), 18, 750)
                .withTier("of_static", ModValue.of(2, 2), 18, 750)
                .withTier("of_suffering", ModValue.of(2, 2), 18, 750)
                .withTier("of_the_enchanter", ModValue.of(2, 2), 25, 150)
                .withTier("of_ruin", ModValue.of(3, 3), 36, 500)
                .withTier("of_ice", ModValue.of(3, 3), 36, 500)
                .withTier("of_flames", ModValue.of(3, 3), 36, 500)
                .withTier("of_electricity", ModValue.of(3, 3), 36, 500)
                .withTier("of_torment", ModValue.of(3, 3), 36, 500)
                .withTier("of_havoc", ModValue.of(4, 4), 55, 250)
                .withTier("of_rime", ModValue.of(4, 4), 55, 250)
                .withTier("of_immolation", ModValue.of(4, 4), 55, 250)
                .withTier("of_voltage", ModValue.of(4, 4), 55, 250)
                .withTier("of_desolation", ModValue.of(4, 4), 55, 250)
                .withTier("of_the_sorcerer", ModValue.of(3, 3), 55, 100)
                .withTier("of_the_wizard", ModValue.of(4, 4), 78, 50)
                .withTier("of_armageddon", ModValue.of(5, 5), 81, 100)
                .withTier("of_frostbite", ModValue.of(5, 5), 81, 100)
                .withTier("of_inferno", ModValue.of(5, 5), 81, 100)
                .withTier("of_thunder", ModValue.of(5, 5), 81, 100)
                .withTier("of_grief", ModValue.of(5, 5), 81, 100)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CROSSBOW_MOD_SUFFIX, "Dexterity",
            ModPreset.builder(ModAdditions.ADD_DEXTERITY)
                .withTier("of_the_mongoose", ModValue.of(5), 1, 500)
                .withTier("of_the_lynx", ModValue.of(9, 12), 11, 500)
                .withTier("of_the_fox", ModValue.of(13, 16), 22, 500)
                .withTier("of_the_falcon", ModValue.of(17, 20), 33, 500)
                .withTier("of_the_panther", ModValue.of(21, 24), 44, 500)
                .withTier("of_the_leopard", ModValue.of(25, 27), 55, 500)
                .withTier("of_the_jaguar", ModValue.of(28, 30), 66, 500)
                .withTier("of_the_phantom", ModValue.of(31, 33), 74, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "DexterityShield",
            ModPreset.builder(ModAdditions.ADD_DEXTERITY)
                .withTier("of_the_mongoose", ModValue.of(5), 1, 500)
                .withTier("of_the_lynx", ModValue.of(9, 12), 11, 500)
                .withTier("of_the_fox", ModValue.of(13, 16), 22, 500)
                .withTier("of_the_falcon", ModValue.of(17, 20), 33, 500)
                .withTier("of_the_panther", ModValue.of(21, 24), 44, 500)
                .withTier("of_the_leopard", ModValue.of(25, 27), 55, 500)
                .withTier("of_the_jaguar", ModValue.of(28, 30), 66, 500)
                .withTier("of_the_phantom", ModValue.of(31, 33), 74, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.LEGS_MOD_SUFFIX, "Dexterity",
            ModPreset.builder(ModAdditions.ADD_DEXTERITY)
                .withTier("of_the_mongoose", ModValue.of(5), 1, 1000)
                .withTier("of_the_lynx", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_fox", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_falcon", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_panther", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_leopard", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_jaguar", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_phantom", ModValue.of(31, 33), 74, 1000)
                .withTier("of_the_wind", ModValue.of(34, 36), 81, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.HEAD_MOD_SUFFIX, "Intelligence",
            ModPreset.builder(ModAdditions.ADD_INTELLIGENCE)
                .withTier("of_the_pupil", ModValue.of(5), 1, 1000)
                .withTier("of_the_student", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_prodigy", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_augur", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_philosopher", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_sage", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_savant", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_virtuoso", ModValue.of(31, 33), 74, 1000)
                .withTier("of_the_genius", ModValue.of(34, 36), 81, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUARTERSTAFF_MOD_SUFFIX, "Intelligence",
            ModPreset.builder(ModAdditions.ADD_INTELLIGENCE)
                .withTier("of_the_pupil", ModValue.of(5), 1, 250)
                .withTier("of_the_student", ModValue.of(9, 12), 11, 250)
                .withTier("of_the_prodigy", ModValue.of(13, 16), 22, 250)
                .withTier("of_the_augur", ModValue.of(17, 20), 33, 250)
                .withTier("of_the_philosopher", ModValue.of(21, 24), 44, 250)
                .withTier("of_the_sage", ModValue.of(25, 27), 55, 250)
                .withTier("of_the_savant", ModValue.of(28, 30), 66, 250)
                .withTier("of_the_virtuoso", ModValue.of(31, 33), 74, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_SUFFIX, "Intelligence",
            ModPreset.builder(ModAdditions.ADD_INTELLIGENCE)
                .withTier("of_the_pupil", ModValue.of(5), 1, 750)
                .withTier("of_the_student", ModValue.of(9, 12), 11, 750)
                .withTier("of_the_prodigy", ModValue.of(13, 16), 22, 750)
                .withTier("of_the_augur", ModValue.of(17, 20), 33, 750)
                .withTier("of_the_philosopher", ModValue.of(21, 24), 44, 750)
                .withTier("of_the_sage", ModValue.of(25, 27), 55, 750)
                .withTier("of_the_savant", ModValue.of(28, 30), 66, 750)
                .withTier("of_the_virtuoso", ModValue.of(31, 33), 74, 750)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "IntelligenceShield",
            ModPreset.builder(ModAdditions.ADD_INTELLIGENCE)
                .withTier("of_the_pupil", ModValue.of(5), 1, 500)
                .withTier("of_the_student", ModValue.of(9, 12), 11, 500)
                .withTier("of_the_prodigy", ModValue.of(13, 16), 22, 500)
                .withTier("of_the_augur", ModValue.of(17, 20), 33, 500)
                .withTier("of_the_philosopher", ModValue.of(21, 24), 44, 500)
                .withTier("of_the_sage", ModValue.of(25, 27), 55, 500)
                .withTier("of_the_savant", ModValue.of(28, 30), 66, 500)
                .withTier("of_the_virtuoso", ModValue.of(31, 33), 74, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TALISMAN_MOD_SUFFIX, "Intelligence",
            ModPreset.builder(ModAdditions.ADD_INTELLIGENCE)
                .withTier("of_the_pupil", ModValue.of(5), 1, 400)
                .withTier("of_the_student", ModValue.of(9, 12), 11, 400)
                .withTier("of_the_prodigy", ModValue.of(13, 16), 22, 400)
                .withTier("of_the_augur", ModValue.of(17, 20), 33, 400)
                .withTier("of_the_philosopher", ModValue.of(21, 24), 44, 400)
                .withTier("of_the_sage", ModValue.of(25, 27), 55, 400)
                .withTier("of_the_savant", ModValue.of(28, 30), 66, 400)
                .withTier("of_the_virtuoso", ModValue.of(31, 33), 74, 400)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BELT_MOD_SUFFIX, "Strength",
            ModPreset.builder(ModAdditions.ADD_STRENGTH)
                .withTier("of_the_brute", ModValue.of(5), 1, 1000)
                .withTier("of_the_wrestler", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_bear", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_lion", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_gorilla", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_goliath", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_leviathan", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_titan", ModValue.of(31, 33), 74, 1000)
                .withTier("of_the_gods", ModValue.of(34, 36), 81, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CROSSBOW_MOD_SUFFIX, "Strength",
            ModPreset.builder(ModAdditions.ADD_STRENGTH)
                .withTier("of_the_brute", ModValue.of(5), 1, 500)
                .withTier("of_the_wrestler", ModValue.of(9, 12), 11, 500)
                .withTier("of_the_bear", ModValue.of(13, 16), 22, 500)
                .withTier("of_the_lion", ModValue.of(17, 20), 33, 500)
                .withTier("of_the_gorilla", ModValue.of(21, 24), 44, 500)
                .withTier("of_the_goliath", ModValue.of(25, 27), 55, 500)
                .withTier("of_the_leviathan", ModValue.of(28, 30), 66, 500)
                .withTier("of_the_titan", ModValue.of(31, 33), 74, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TALISMAN_MOD_SUFFIX, "Strength",
            ModPreset.builder(ModAdditions.ADD_STRENGTH)
                .withTier("of_the_brute", ModValue.of(5), 1, 600)
                .withTier("of_the_wrestler", ModValue.of(9, 12), 11, 600)
                .withTier("of_the_bear", ModValue.of(13, 16), 22, 600)
                .withTier("of_the_lion", ModValue.of(17, 20), 33, 600)
                .withTier("of_the_gorilla", ModValue.of(21, 24), 44, 600)
                .withTier("of_the_goliath", ModValue.of(25, 27), 55, 600)
                .withTier("of_the_leviathan", ModValue.of(28, 30), 66, 600)
                .withTier("of_the_titan", ModValue.of(31, 33), 74, 600)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_SUFFIX, "AllAttributes",
            ModPreset.builder(ModAdditions.ALL_ATTRIBUTES)
                .withTier("of_the_clouds", ModValue.of(2), 1, 800)
                .withTier("of_the_sky", ModValue.of(5, 7), 11, 800)
                .withTier("of_the_meteor", ModValue.of(8, 10), 22, 800)
                .withTier("of_the_comet", ModValue.of(11, 13), 33, 800)
                .withTier("of_the_heavens", ModValue.of(14, 16), 44, 800)
                .withTier("of_the_galaxy", ModValue.of(17, 18), 55, 800)
                .withTier("of_the_universe", ModValue.of(19, 20), 66, 800)
                .withTier("of_the_multiverse", ModValue.of(21, 22), 75, 800)
                .withTier("of_the_infinite", ModValue.of(23, 24), 82, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_SUFFIX, "AllAttributes",
            ModPreset.builder(ModAdditions.ALL_ATTRIBUTES)
                .withTier("of_the_clouds", ModValue.of(2), 1, 400)
                .withTier("of_the_sky", ModValue.of(5, 7), 11, 400)
                .withTier("of_the_meteor", ModValue.of(8, 10), 22, 400)
                .withTier("of_the_comet", ModValue.of(11, 13), 33, 400)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.AMULET_MOD_SUFFIX, "AllResistances",
            ModPreset.builder(ModAdditions.ALL_RESISTANCES)
                .withTier("of_the_crystal", ModValue.of(3), 12, 800)
                .withTier("of_the_prism", ModValue.of(6, 8), 26, 800)
                .withTier("of_the_kaleidoscope", ModValue.of(9, 11), 40, 800)
                .withTier("of_variegation", ModValue.of(12, 14), 54, 800)
                .withTier("of_the_rainbow", ModValue.of(15, 16), 68, 800)
                .withTier("of_the_span", ModValue.of(17, 18), 80, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "AllResistances",
            ModPreset.builder(ModAdditions.ALL_RESISTANCES)
                .withTier("of_the_crystal", ModValue.of(3), 12, 800)
                .withTier("of_the_prism", ModValue.of(6, 8), 26, 800)
                .withTier("of_the_kaleidoscope", ModValue.of(9, 11), 40, 800)
                .withTier("of_variegation", ModValue.of(12, 14), 54, 800)
                .withTier("of_the_rainbow", ModValue.of(15, 16), 68, 800)
                .withTier("of_the_span", ModValue.of(17, 18), 80, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_SUFFIX, "AllResistances",
            ModPreset.builder(ModAdditions.ALL_RESISTANCES)
                .withTier("of_the_crystal", ModValue.of(3), 12, 800)
                .withTier("of_the_prism", ModValue.of(6, 8), 26, 800)
                .withTier("of_the_kaleidoscope", ModValue.of(9, 11), 40, 800)
                .withTier("of_variegation", ModValue.of(12, 14), 54, 800)
                .withTier("of_the_rainbow", ModValue.of(15, 16), 68, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_SUFFIX, "AllResistances",
            ModPreset.builder(ModAdditions.ALL_RESISTANCES)
                .withTier("of_adjustment", ModValue.of(3), 12, 800)
                .withTier("of_acclimatisation", ModValue.of(6, 8), 26, 800)
                .withTier("of_adaptation", ModValue.of(9, 11), 40, 800)
                .withTier("of_evolution", ModValue.of(12, 14), 54, 800)
                .withTier("of_progression", ModValue.of(15, 16), 68, 800)
                .withTier("of_metamorphosis", ModValue.of(17, 18), 80, 800)
                .build());

    }

    private static void s2(BootstrapContext<ItemInstance> bootstrap) {
        EquipmentGen.registerMod(bootstrap, EquipmentGen.BELT_MOD_PREFIX, "Thorns",
            ModPreset.builder(ModAdditions.THORNS)
                .withTier("thorny", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 4)), 1, 1000)
                .withTier("spiny", Map.of("min", ModValue.of(5, 7), "max", ModValue.of(7, 10)), 10, 1000)
                .withTier("barbed", Map.of("min", ModValue.of(11, 16), "max", ModValue.of(17, 23)), 19, 1000)
                .withTier("pointed", Map.of("min", ModValue.of(24, 35), "max", ModValue.of(36, 53)), 38, 1000)
                .withTier("spiked", Map.of("min", ModValue.of(40, 60), "max", ModValue.of(61, 92)), 48, 1000)
                .withTier("edged", Map.of("min", ModValue.of(64, 97), "max", ModValue.of(98, 145)), 63, 1000)
                .withTier("jagged", Map.of("min", ModValue.of(101, 151), "max", ModValue.of(152, 220)), 74, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BELT_MOD_PREFIX, "BeltCharmDuration",
            ModPreset.builder(AdditionType.global(StatIds.BELT_CHARM_DURATION))
                .withTier("conservative", ModValue.of(4), 8, 800)
                .withTier("transformative", ModValue.of(10, 15), 33, 800)
                .withTier("progressive", ModValue.of(16, 21), 46, 800)
                .withTier("innovative", ModValue.of(22, 27), 60, 800)
                .withTier("revolutionary", ModValue.of(28, 33), 75, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BELT_MOD_PREFIX, "BeltCharmSlots",
            ModPreset.builder(AdditionType.global(StatIds.CHARM_SLOT))
                .withTier("conservative", ModValue.of(1), 8, 800)
                .withTier("transformative", ModValue.of(1), 33, 800)
                .withTier("progressive", ModValue.of(2), 46, 800)
                .withTier("innovative", ModValue.of(2), 60, 800)
                .withTier("revolutionary", ModValue.of(3), 75, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BELT_MOD_PREFIX, "BeltFlaskRecoveryRate",
            ModPreset.builder(AdditionType.global(StatIds.BELT_FLASK_LIFE_RECOVERY_RATE))
                .withTier("restoring", ModValue.of(5), 1, 800)
                .withTier("affecting", ModValue.of(5, 10), 5, 800)
                .withTier("stirring", ModValue.of(11, 16), 11, 800)
                .withTier("recovering", ModValue.of(11, 16), 16, 800)
                .withTier("heartening", ModValue.of(17, 22), 26, 800)
                .withTier("renewing", ModValue.of(17, 22), 33, 800)
                .withTier("exciting", ModValue.of(23, 28), 36, 800)
                .withTier("refreshing", ModValue.of(23, 28), 46, 800)
                .withTier("galvanizing", ModValue.of(29, 34), 54, 800)
                .withTier("rejuvenating", ModValue.of(29, 34), 60, 800)
                .withTier("inspiring", ModValue.of(35, 40), 63, 800)
                .withTier("regenerating", ModValue.of(35, 40), 75, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BELT_MOD_PREFIX, "IncreasedPhysicalDamageReductionRating",
            ModPreset.builder(ModAdditions.ADD_ARMOR_LOCAL)
                .withTier("lacquered", ModValue.of(12), 1, 1000)
                .withTier("studded", ModValue.of(23, 51), 11, 1000)
                .withTier("ribbed", ModValue.of(52, 68), 16, 1000)
                .withTier("fortified", ModValue.of(69, 107), 25, 1000)
                .withTier("plated", ModValue.of(108, 140), 33, 1000)
                .withTier("carapaced", ModValue.of(141, 186), 46, 1000)
                .withTier("encased", ModValue.of(187, 225), 54, 1000)
                .withTier("enveloped", ModValue.of(226, 267), 65, 1000)
                .withTier("abating", ModValue.of(268, 311), 70, 1000)
                .withTier("unmoving", ModValue.of(312, 351), 80, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BELT_MOD_SUFFIX, "BeltCharmChargesGained",
            ModPreset.builder(AdditionType.global(StatIds.BELT_CHARM_CHARGES_GAINED))
                .withTier("of_plenty", ModValue.of(5), 2, 800)
                .withTier("of_surplus", ModValue.of(11, 16), 16, 800)
                .withTier("of_fertility", ModValue.of(17, 22), 32, 800)
                .withTier("of_bounty", ModValue.of(23, 28), 48, 800)
                .withTier("of_the_harvest", ModValue.of(29, 34), 70, 800)
                .withTier("of_abundance", ModValue.of(35, 40), 81, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BELT_MOD_SUFFIX, "BeltFlaskChargesGained",
            ModPreset.builder(AdditionType.global(StatIds.BELT_FLASK_CHARGES_GAINED))
                .withTier("of_refilling", ModValue.of(5), 2, 800)
                .withTier("of_restocking", ModValue.of(11, 16), 16, 800)
                .withTier("of_replenishing", ModValue.of(17, 22), 32, 800)
                .withTier("of_pouring", ModValue.of(23, 28), 48, 800)
                .withTier("of_brimming", ModValue.of(29, 34), 70, 800)
                .withTier("of_overflowing", ModValue.of(35, 40), 81, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BELT_MOD_SUFFIX, "BeltCharmChargesUsed",
            ModPreset.builder(AdditionType.global(StatIds.BELT_CHARM_CHARGES_USED))
                .withTier("of_austerity", ModValue.of(8), 3, 800)
                .withTier("of_frugality", ModValue.of(11, 13), 18, 800)
                .withTier("of_temperance", ModValue.of(14, 16), 33, 800)
                .withTier("of_restraint", ModValue.of(17, 19), 50, 800)
                .withTier("of_economy", ModValue.of(20, 22), 72, 800)
                .withTier("of_scarcity", ModValue.of(23, 25), 81, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BELT_MOD_SUFFIX, "BeltFlaskChargesUsed",
            ModPreset.builder(AdditionType.global(StatIds.BELT_FLASK_CHARGES_USED))
                .withTier("of_sipping", ModValue.of(8), 3, 800)
                .withTier("of_imbibing", ModValue.of(11, 13), 18, 800)
                .withTier("of_relishing", ModValue.of(14, 16), 33, 800)
                .withTier("of_savouring", ModValue.of(17, 19), 50, 800)
                .withTier("of_reveling", ModValue.of(20, 22), 72, 800)
                .withTier("of_nourishing", ModValue.of(23, 25), 81, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BELT_MOD_SUFFIX, "StunThreshold",
            ModPreset.builder(ModAdditions.ADD_STUN_THRESHOLD)
                .withTier("of_thick_skin", ModValue.of(6), 1, 800)
                .withTier("of_reinforced_skin", ModValue.of(12, 29), 8, 800)
                .withTier("of_stone_skin", ModValue.of(30, 49), 15, 800)
                .withTier("of_iron_skin", ModValue.of(50, 72), 22, 800)
                .withTier("of_steel_skin", ModValue.of(73, 97), 29, 800)
                .withTier("of_granite_skin", ModValue.of(98, 124), 36, 800)
                .withTier("of_platinum_skin", ModValue.of(125, 163), 45, 800)
                .withTier("of_adamantite_skin", ModValue.of(164, 206), 54, 800)
                .withTier("of_corundum_skin", ModValue.of(207, 253), 63, 800)
                .withTier("of_obsidian_skin", ModValue.of(254, 304), 72, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FEET_MOD_SUFFIX, "StunThreshold",
            ModPreset.builder(ModAdditions.ADD_STUN_THRESHOLD)
                .withTier("of_thick_skin", ModValue.of(6), 1, 800)
                .withTier("of_reinforced_skin", ModValue.of(12, 29), 8, 800)
                .withTier("of_stone_skin", ModValue.of(30, 49), 15, 800)
                .withTier("of_iron_skin", ModValue.of(50, 72), 22, 800)
                .withTier("of_steel_skin", ModValue.of(73, 97), 29, 800)
                .withTier("of_granite_skin", ModValue.of(98, 124), 36, 800)
                .withTier("of_platinum_skin", ModValue.of(125, 163), 45, 800)
                .withTier("of_adamantite_skin", ModValue.of(164, 206), 54, 800)
                .withTier("of_corundum_skin", ModValue.of(207, 253), 63, 800)
                .withTier("of_obsidian_skin", ModValue.of(254, 304), 72, 800)
                .withTier("of_titanium_skin", ModValue.of(305, 352), 80, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_PREFIX, "DefencesPercentBuckler",
            ModPreset.builder(ModAdditions.ARMOR_INC_LOCAL)
                .withTier("shade_s", ModValue.of(15), 2, 1000)
                .withTier("ghost_s", ModValue.of(27, 42), 16, 1000)
                .withTier("spectre_s", ModValue.of(43, 55), 33, 1000)
                .withTier("wraith_s", ModValue.of(56, 67), 46, 1000)
                .withTier("phantasm_s", ModValue.of(68, 79), 54, 1000)
                .withTier("nightmare_s", ModValue.of(80, 91), 60, 1000)
                .withTier("mirage_s", ModValue.of(92, 100), 65, 1000)
                .withTier("illusion_s", ModValue.of(101, 110), 75, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHEST_MOD_PREFIX, "DefencesPercent",
            ModPreset.builder(ModAdditions.ARMOR_INC_LOCAL)
                .withTier("reinforced", ModValue.of(15), 2, 1000)
                .withTier("infixed", ModValue.of(15, 26), 2, 1000)
                .withTier("scrapper_s", ModValue.of(15, 26), 2, 1000)
                .withTier("shadowy", ModValue.of(15, 26), 2, 1)
                .withTier("protective", ModValue.of(15, 26), 2, 1000)
                .withTier("shade_s", ModValue.of(15, 26), 2, 1000)
                .withTier("shadowy", ModValue.of(15, 26), 2, 1000)
                .withTier("layered", ModValue.of(27, 42), 16, 1000)
                .withTier("ingrained", ModValue.of(27, 42), 16, 1000)
                .withTier("brawler_s", ModValue.of(27, 42), 16, 1000)
                .withTier("ethereal", ModValue.of(27, 42), 16, 1)
                .withTier("strong_willed", ModValue.of(27, 42), 16, 1000)
                .withTier("ghost_s", ModValue.of(27, 42), 16, 1000)
                .withTier("ethereal", ModValue.of(27, 42), 16, 1000)
                .withTier("instilled", ModValue.of(43, 55), 33, 1000)
                .withTier("fencer_s", ModValue.of(43, 55), 33, 1000)
                .withTier("unworldly", ModValue.of(43, 55), 33, 1)
                .withTier("resolute", ModValue.of(43, 55), 33, 1000)
                .withTier("spectre_s", ModValue.of(43, 55), 33, 1000)
                .withTier("unworldly", ModValue.of(43, 55), 33, 1000)
                .withTier("lobstered", ModValue.of(43, 55), 35, 1000)
                .withTier("buttressed", ModValue.of(56, 67), 46, 1000)
                .withTier("infused", ModValue.of(56, 67), 46, 1000)
                .withTier("gladiator_s", ModValue.of(56, 67), 46, 1000)
                .withTier("ephemeral", ModValue.of(56, 67), 46, 1)
                .withTier("fearless", ModValue.of(56, 67), 46, 1000)
                .withTier("wraith_s", ModValue.of(56, 67), 46, 1000)
                .withTier("ephemeral", ModValue.of(56, 67), 46, 1000)
                .withTier("thickened", ModValue.of(68, 79), 54, 1000)
                .withTier("inculcated", ModValue.of(68, 79), 54, 1000)
                .withTier("duelist_s", ModValue.of(68, 79), 54, 1000)
                .withTier("evanescent", ModValue.of(68, 79), 54, 1)
                .withTier("dauntless", ModValue.of(68, 79), 54, 1000)
                .withTier("phantasm_s", ModValue.of(68, 79), 54, 1000)
                .withTier("evanescent", ModValue.of(68, 79), 54, 1000)
                .withTier("girded", ModValue.of(80, 91), 60, 1000)
                .withTier("interpolated", ModValue.of(80, 91), 60, 1000)
                .withTier("hero_s", ModValue.of(80, 91), 60, 1000)
                .withTier("unreal", ModValue.of(80, 91), 60, 1)
                .withTier("indomitable", ModValue.of(80, 91), 60, 1000)
                .withTier("nightmare_s", ModValue.of(80, 91), 60, 1000)
                .withTier("unreal", ModValue.of(80, 91), 60, 1000)
                .withTier("impregnable", ModValue.of(92, 100), 65, 1000)
                .withTier("inspired", ModValue.of(92, 100), 65, 1000)
                .withTier("legend_s", ModValue.of(92, 100), 65, 1000)
                .withTier("incorporeal", ModValue.of(92, 100), 65, 1)
                .withTier("unassailable", ModValue.of(92, 100), 65, 1000)
                .withTier("mirage_s", ModValue.of(92, 100), 65, 1000)
                .withTier("illusory", ModValue.of(92, 100), 65, 1000)
                .withTier("impenetrable", ModValue.of(101, 110), 75, 1000)
                .withTier("interpermeated", ModValue.of(101, 110), 75, 1000)
                .withTier("victor_s", ModValue.of(101, 110), 75, 1000)
                .withTier("ascendant", ModValue.of(101, 110), 75, 1)
                .withTier("unfaltering", ModValue.of(101, 110), 75, 1000)
                .withTier("illusion_s", ModValue.of(101, 110), 75, 1000)
                .withTier("incorporeal", ModValue.of(101, 110), 75, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_PREFIX, "DefencesPercentFoci",
            ModPreset.builder(ModAdditions.ARMOR_INC_LOCAL)
                .withTier("protective", ModValue.of(15), 2, 1000)
                .withTier("strong_willed", ModValue.of(27, 42), 16, 1000)
                .withTier("resolute", ModValue.of(43, 55), 33, 1000)
                .withTier("fearless", ModValue.of(56, 67), 46, 1000)
                .withTier("dauntless", ModValue.of(68, 79), 54, 1000)
                .withTier("indomitable", ModValue.of(80, 91), 60, 1000)
                .withTier("unassailable", ModValue.of(92, 100), 65, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_PREFIX, "DefencesPercentShield",
            ModPreset.builder(ModAdditions.ARMOR_INC_LOCAL)
                .withTier("reinforced", ModValue.of(15), 2, 1000)
                .withTier("infixed", ModValue.of(15, 26), 2, 1000)
                .withTier("scrapper_s", ModValue.of(15, 26), 2, 1000)
                .withTier("layered", ModValue.of(27, 42), 16, 1000)
                .withTier("ingrained", ModValue.of(27, 42), 16, 1000)
                .withTier("brawler_s", ModValue.of(27, 42), 16, 1000)
                .withTier("instilled", ModValue.of(43, 55), 33, 1000)
                .withTier("fencer_s", ModValue.of(43, 55), 33, 1000)
                .withTier("lobstered", ModValue.of(43, 55), 35, 1000)
                .withTier("buttressed", ModValue.of(56, 67), 46, 1000)
                .withTier("infused", ModValue.of(56, 67), 46, 1000)
                .withTier("gladiator_s", ModValue.of(56, 67), 46, 1000)
                .withTier("thickened", ModValue.of(68, 79), 54, 1000)
                .withTier("inculcated", ModValue.of(68, 79), 54, 1000)
                .withTier("duelist_s", ModValue.of(68, 79), 54, 1000)
                .withTier("girded", ModValue.of(80, 91), 60, 1000)
                .withTier("interpolated", ModValue.of(80, 91), 60, 1000)
                .withTier("hero_s", ModValue.of(80, 91), 60, 1000)
                .withTier("impregnable", ModValue.of(92, 100), 65, 1000)
                .withTier("inspired", ModValue.of(92, 100), 65, 1000)
                .withTier("legend_s", ModValue.of(92, 100), 65, 1000)
                .withTier("impenetrable", ModValue.of(101, 110), 75, 1000)
                .withTier("interpermeated", ModValue.of(101, 110), 75, 1000)
                .withTier("victor_s", ModValue.of(101, 110), 75, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_PREFIX, "BaseLocalDefencesBuckler",
            ModPreset.builder(ModAdditions.ADD_ARMOR_LOCAL)
                .withTier("agile", ModValue.of(11), 1, 1000)
                .withTier("dancer_s", ModValue.of(19, 46), 8, 1000)
                .withTier("acrobat_s", ModValue.of(47, 66), 16, 1000)
                .withTier("fleet", ModValue.of(67, 87), 25, 1000)
                .withTier("blurred", ModValue.of(88, 116), 33, 1000)
                .withTier("phased", ModValue.of(117, 146), 46, 1000)
                .withTier("vaporous", ModValue.of(147, 176), 54, 1000)
                .withTier("elusory", ModValue.of(177, 207), 60, 1000)
                .withTier("adroit", ModValue.of(208, 234), 65, 1000)
                .withTier("lissome", ModValue.of(235, 261), 75, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHEST_MOD_PREFIX, "BaseLocalDefences",
            ModPreset.builder(ModAdditions.ADD_ARMOR_LOCAL)
                .withTier("lacquered", ModValue.of(16), 1, 1000)
                .withTier("supple", ModValue.of(9, 16), 1, 1000)
                .withTier("blessed", ModValue.of(9, 16), 1, 1000)
                .withTier("agile", ModValue.of(11, 18), 1, 1000)
                .withTier("will_o_wisp_s", ModValue.of(6, 10), 1, 1000)
                .withTier("shining", ModValue.of(10, 17), 1, 1000)
                .withTier("studded", ModValue.of(28, 56), 8, 1000)
                .withTier("dancer_s", ModValue.of(19, 46), 8, 1000)
                .withTier("glimmering", ModValue.of(18, 24), 8, 1000)
                .withTier("ribbed", ModValue.of(57, 77), 16, 1000)
                .withTier("pliant", ModValue.of(17, 46), 16, 1000)
                .withTier("anointed", ModValue.of(17, 46), 16, 1000)
                .withTier("acrobat_s", ModValue.of(47, 66), 16, 1000)
                .withTier("nymph_s", ModValue.of(11, 41), 16, 1000)
                .withTier("glittering", ModValue.of(25, 30), 16, 1000)
                .withTier("fortified", ModValue.of(78, 98), 25, 1000)
                .withTier("fleet", ModValue.of(67, 87), 25, 1000)
                .withTier("glowing", ModValue.of(31, 35), 25, 1000)
                .withTier("plated", ModValue.of(99, 127), 33, 1000)
                .withTier("flexible", ModValue.of(47, 71), 33, 1000)
                .withTier("sanctified", ModValue.of(47, 71), 33, 1000)
                .withTier("blurred", ModValue.of(88, 116), 33, 1000)
                .withTier("sylph_s", ModValue.of(42, 64), 33, 1000)
                .withTier("radiating", ModValue.of(36, 41), 33, 1000)
                .withTier("carapaced", ModValue.of(128, 159), 46, 1000)
                .withTier("durable", ModValue.of(72, 85), 46, 1000)
                .withTier("hallowed", ModValue.of(72, 85), 46, 1000)
                .withTier("phased", ModValue.of(117, 146), 46, 1000)
                .withTier("cherub_s", ModValue.of(65, 78), 46, 1000)
                .withTier("pulsing", ModValue.of(42, 47), 46, 1000)
                .withTier("encased", ModValue.of(160, 190), 54, 1000)
                .withTier("sturdy", ModValue.of(86, 102), 54, 1000)
                .withTier("beatified", ModValue.of(86, 102), 54, 1000)
                .withTier("vaporous", ModValue.of(147, 176), 54, 1000)
                .withTier("spirit_s", ModValue.of(79, 94), 54, 1000)
                .withTier("blazing", ModValue.of(48, 60), 54, 1000)
                .withTier("enveloped", ModValue.of(191, 221), 60, 1000)
                .withTier("resilient", ModValue.of(103, 127), 60, 1000)
                .withTier("consecrated", ModValue.of(103, 127), 60, 1000)
                .withTier("elusory", ModValue.of(177, 207), 60, 1000)
                .withTier("eidolon_s", ModValue.of(95, 119), 60, 1000)
                .withTier("dazzling", ModValue.of(61, 73), 60, 1000)
                .withTier("abating", ModValue.of(222, 248), 65, 1000)
                .withTier("adaptable", ModValue.of(128, 149), 65, 1000)
                .withTier("saintly", ModValue.of(128, 149), 65, 1000)
                .withTier("adroit", ModValue.of(208, 234), 65, 1000)
                .withTier("apparition_s", ModValue.of(120, 141), 65, 1000)
                .withTier("scintillating", ModValue.of(74, 80), 65, 1000)
                .withTier("incandescent", ModValue.of(81, 90), 70, 1000)
                .withTier("unmoving", ModValue.of(249, 277), 75, 1000)
                .withTier("versatile", ModValue.of(150, 170), 75, 1000)
                .withTier("godly", ModValue.of(150, 170), 75, 1000)
                .withTier("lissome", ModValue.of(235, 261), 75, 1000)
                .withTier("banshee_s", ModValue.of(142, 161), 75, 1000)
                .withTier("impervious", ModValue.of(278, 310), 79, 1000)
                .withTier("fugitive", ModValue.of(262, 300), 79, 1000)
                .withTier("resplendent", ModValue.of(91, 96), 79, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_PREFIX, "BaseLocalDefencesFoci",
            ModPreset.builder(ModAdditions.ADD_ARMOR_LOCAL)
                .withTier("shining", ModValue.of(10), 1, 1000)
                .withTier("glimmering", ModValue.of(18, 24), 8, 1000)
                .withTier("glittering", ModValue.of(25, 30), 16, 1000)
                .withTier("glowing", ModValue.of(31, 35), 25, 1000)
                .withTier("radiating", ModValue.of(36, 41), 33, 1000)
                .withTier("pulsing", ModValue.of(42, 47), 46, 1000)
                .withTier("blazing", ModValue.of(48, 60), 54, 1000)
                .withTier("dazzling", ModValue.of(61, 73), 60, 1000)
                .withTier("scintillating", ModValue.of(74, 80), 65, 1000)
                .withTier("incandescent", ModValue.of(81, 90), 70, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.HEAD_MOD_PREFIX, "BaseLocalDefences",
            ModPreset.builder(ModAdditions.ADD_ARMOR_LOCAL)
                .withTier("lacquered", ModValue.of(16), 1, 1000)
                .withTier("supple", ModValue.of(9, 16), 1, 1000)
                .withTier("blessed", ModValue.of(9, 16), 1, 1000)
                .withTier("agile", ModValue.of(11, 18), 1, 1000)
                .withTier("will_o_wisp_s", ModValue.of(6, 10), 1, 1000)
                .withTier("shining", ModValue.of(10, 17), 1, 1000)
                .withTier("studded", ModValue.of(28, 56), 8, 1000)
                .withTier("dancer_s", ModValue.of(19, 46), 8, 1000)
                .withTier("glimmering", ModValue.of(18, 24), 8, 1000)
                .withTier("ribbed", ModValue.of(57, 77), 16, 1000)
                .withTier("pliant", ModValue.of(17, 46), 16, 1000)
                .withTier("anointed", ModValue.of(17, 46), 16, 1000)
                .withTier("acrobat_s", ModValue.of(47, 66), 16, 1000)
                .withTier("nymph_s", ModValue.of(11, 41), 16, 1000)
                .withTier("glittering", ModValue.of(25, 30), 16, 1000)
                .withTier("fortified", ModValue.of(78, 98), 25, 1000)
                .withTier("fleet", ModValue.of(67, 87), 25, 1000)
                .withTier("glowing", ModValue.of(31, 35), 25, 1000)
                .withTier("plated", ModValue.of(99, 127), 33, 1000)
                .withTier("flexible", ModValue.of(47, 71), 33, 1000)
                .withTier("sanctified", ModValue.of(47, 71), 33, 1000)
                .withTier("blurred", ModValue.of(88, 116), 33, 1000)
                .withTier("sylph_s", ModValue.of(42, 64), 33, 1000)
                .withTier("radiating", ModValue.of(36, 41), 33, 1000)
                .withTier("carapaced", ModValue.of(128, 159), 46, 1000)
                .withTier("durable", ModValue.of(72, 85), 46, 1000)
                .withTier("hallowed", ModValue.of(72, 85), 46, 1000)
                .withTier("phased", ModValue.of(117, 146), 46, 1000)
                .withTier("cherub_s", ModValue.of(65, 78), 46, 1000)
                .withTier("pulsing", ModValue.of(42, 47), 46, 1000)
                .withTier("encased", ModValue.of(160, 190), 54, 1000)
                .withTier("sturdy", ModValue.of(86, 102), 54, 1000)
                .withTier("beatified", ModValue.of(86, 102), 54, 1000)
                .withTier("vaporous", ModValue.of(147, 176), 54, 1000)
                .withTier("spirit_s", ModValue.of(79, 94), 54, 1000)
                .withTier("blazing", ModValue.of(48, 60), 54, 1000)
                .withTier("enveloped", ModValue.of(191, 221), 60, 1000)
                .withTier("elusory", ModValue.of(177, 207), 60, 1000)
                .withTier("dazzling", ModValue.of(61, 73), 60, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_PREFIX, "BaseLocalDefencesShield",
            ModPreset.builder(ModAdditions.ADD_ARMOR_LOCAL)
                .withTier("lacquered", ModValue.of(16), 1, 1000)
                .withTier("supple", ModValue.of(9, 16), 1, 1000)
                .withTier("blessed", ModValue.of(9, 16), 1, 1000)
                .withTier("studded", ModValue.of(28, 56), 8, 1000)
                .withTier("ribbed", ModValue.of(57, 77), 16, 1000)
                .withTier("pliant", ModValue.of(17, 46), 16, 1000)
                .withTier("anointed", ModValue.of(17, 46), 16, 1000)
                .withTier("fortified", ModValue.of(78, 98), 25, 1000)
                .withTier("plated", ModValue.of(99, 127), 33, 1000)
                .withTier("flexible", ModValue.of(47, 71), 33, 1000)
                .withTier("sanctified", ModValue.of(47, 71), 33, 1000)
                .withTier("carapaced", ModValue.of(128, 159), 46, 1000)
                .withTier("durable", ModValue.of(72, 85), 46, 1000)
                .withTier("hallowed", ModValue.of(72, 85), 46, 1000)
                .withTier("encased", ModValue.of(160, 190), 54, 1000)
                .withTier("sturdy", ModValue.of(86, 102), 54, 1000)
                .withTier("beatified", ModValue.of(86, 102), 54, 1000)
                .withTier("enveloped", ModValue.of(191, 221), 60, 1000)
                .withTier("resilient", ModValue.of(103, 127), 60, 1000)
                .withTier("consecrated", ModValue.of(103, 127), 60, 1000)
                .withTier("abating", ModValue.of(222, 248), 65, 1000)
                .withTier("adaptable", ModValue.of(128, 149), 65, 1000)
                .withTier("saintly", ModValue.of(128, 149), 65, 1000)
                .withTier("unmoving", ModValue.of(249, 277), 75, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHEST_MOD_PREFIX, "BaseLocalDefencesAndDefencePercent",
            ModPreset.builder(MultiAddition.keyed(ModAdditions.ADD_ARMOR_LOCAL, ModAdditions.ARMOR_INC_LOCAL))
                .withTier("abalone_s", Map.of("value", ModValue.of(5, 9), "value_2", ModValue.of(6, 13)), 8, 1000)
                .withTier("swordsman_s", Map.of("value", ModValue.of(3, 5), "value_2", ModValue.of(2, 3)), 8, 1000)
                .withTier("faithful", Map.of("value", ModValue.of(3, 5), "value_2", ModValue.of(2, 4)), 8, 1000)
                .withTier("impala_s", Map.of("value", ModValue.of(4, 6), "value_2", ModValue.of(6, 13)), 8, 1000)
                .withTier("pursuer_s", Map.of("value", ModValue.of(2, 3), "value_2", ModValue.of(2, 4)), 8, 1000)
                .withTier("deacon_s", Map.of("value", ModValue.of(4, 7), "value_2", ModValue.of(6, 13)), 8, 1000)
                .withTier("snail_s", Map.of("value", ModValue.of(10, 29), "value_2", ModValue.of(14, 20)), 16, 1000)
                .withTier("fighter_s", Map.of("value", ModValue.of(6, 16), "value_2", ModValue.of(4, 14)), 16, 1000)
                .withTier("noble_s", Map.of("value", ModValue.of(6, 16), "value_2", ModValue.of(5, 6)), 16, 1000)
                .withTier("buck_s", Map.of("value", ModValue.of(7, 26), "value_2", ModValue.of(14, 20)), 16, 1000)
                .withTier("tracker_s", Map.of("value", ModValue.of(4, 14), "value_2", ModValue.of(5, 6)), 16, 1000)
                .withTier("cardinal_s", Map.of("value", ModValue.of(8, 13), "value_2", ModValue.of(14, 20)), 16, 1000)
                .withTier("tortoise_s", Map.of("value", ModValue.of(30, 41), "value_2", ModValue.of(21, 26)), 33, 1000)
                .withTier("veteran_s", Map.of("value", ModValue.of(17, 23), "value_2", ModValue.of(15, 21)), 33, 1000)
                .withTier("inquisitor_s", Map.of("value", ModValue.of(17, 23), "value_2", ModValue.of(7, 8)), 33, 1000)
                .withTier("moose_s", Map.of("value", ModValue.of(27, 38), "value_2", ModValue.of(21, 26)), 33, 1000)
                .withTier("chaser_s", Map.of("value", ModValue.of(15, 21), "value_2", ModValue.of(7, 8)), 33, 1000)
                .withTier("priest_s", Map.of("value", ModValue.of(14, 16), "value_2", ModValue.of(21, 26)), 33, 1000)
                .withTier("pangolin_s", Map.of("value", ModValue.of(42, 57), "value_2", ModValue.of(27, 32)), 46, 1000)
                .withTier("warrior_s", Map.of("value", ModValue.of(24, 32), "value_2", ModValue.of(22, 29)), 46, 1000)
                .withTier("crusader_s", Map.of("value", ModValue.of(24, 32), "value_2", ModValue.of(9, 10)), 46, 1000)
                .withTier("deer_s", Map.of("value", ModValue.of(39, 53), "value_2", ModValue.of(27, 32)), 46, 1000)
                .withTier("phantom_s", Map.of("value", ModValue.of(22, 29), "value_2", ModValue.of(9, 10)), 46, 1000)
                .withTier("high_priest_s", Map.of("value", ModValue.of(17, 20), "value_2", ModValue.of(27, 32)), 46, 1000)
                .withTier("shelled", Map.of("value", ModValue.of(58, 75), "value_2", ModValue.of(33, 38)), 60, 1000)
                .withTier("knight_s", Map.of("value", ModValue.of(33, 41), "value_2", ModValue.of(30, 39)), 60, 1000)
                .withTier("paladin_s", Map.of("value", ModValue.of(33, 41), "value_2", ModValue.of(11, 12)), 60, 1000)
                .withTier("caribou_s", Map.of("value", ModValue.of(54, 70), "value_2", ModValue.of(33, 38)), 60, 1000)
                .withTier("rogue_s", Map.of("value", ModValue.of(30, 39), "value_2", ModValue.of(11, 12)), 60, 1000)
                .withTier("archon_s", Map.of("value", ModValue.of(21, 25), "value_2", ModValue.of(33, 38)), 60, 1000)
                .withTier("hardened", Map.of("value", ModValue.of(76, 95), "value_2", ModValue.of(39, 42)), 78, 1000)
                .withTier("centurion_s", Map.of("value", ModValue.of(42, 52), "value_2", ModValue.of(40, 50)), 78, 1000)
                .withTier("grand", Map.of("value", ModValue.of(42, 52), "value_2", ModValue.of(13, 15)), 78, 1000)
                .withTier("antelope_s", Map.of("value", ModValue.of(71, 90), "value_2", ModValue.of(39, 42)), 78, 1000)
                .withTier("stalker_s", Map.of("value", ModValue.of(40, 50), "value_2", ModValue.of(13, 15)), 78, 1000)
                .withTier("divine", Map.of("value", ModValue.of(26, 30), "value_2", ModValue.of(39, 42)), 78, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHEST_MOD_SUFFIX, "LocalAttributeRequirements",
            ModPreset.builder(LocalAddition.of(StatIds.REDUCED_ATTRIBUTE_REQUIREMENTS))
                .withTier("of_the_worthy", ModValue.of(15), 24, 900)
                .withTier("of_the_apt", ModValue.of(20, 20), 32, 900)
                .withTier("of_the_talented", ModValue.of(25, 25), 40, 900)
                .withTier("of_the_skilled", ModValue.of(30, 30), 52, 900)
                .withTier("of_the_proficient", ModValue.of(35, 35), 60, 900)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FEET_MOD_SUFFIX, "LocalAttributeRequirements",
            ModPreset.builder(LocalAddition.of(StatIds.REDUCED_ATTRIBUTE_REQUIREMENTS))
                .withTier("of_the_worthy", ModValue.of(15), 24, 750)
                .withTier("of_the_apt", ModValue.of(20, 20), 32, 750)
                .withTier("of_the_talented", ModValue.of(25, 25), 40, 750)
                .withTier("of_the_skilled", ModValue.of(30, 30), 52, 750)
                .withTier("of_the_proficient", ModValue.of(35, 35), 60, 750)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.LEGS_MOD_SUFFIX, "LocalAttributeRequirements",
            ModPreset.builder(LocalAddition.of(StatIds.REDUCED_ATTRIBUTE_REQUIREMENTS))
                .withTier("of_the_worthy", ModValue.of(15), 24, 700)
                .withTier("of_the_apt", ModValue.of(20, 20), 32, 700)
                .withTier("of_the_talented", ModValue.of(25, 25), 40, 700)
                .withTier("of_the_skilled", ModValue.of(30, 30), 52, 700)
                .withTier("of_the_proficient", ModValue.of(35, 35), 60, 700)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHEST_MOD_SUFFIX, "EnergyShieldRegeneration",
            ModPreset.builder(AdditionType.global(StatIds.INC_ENERGY_SHIELD_RECHARGE_RATE))
                .withTier("of_buffering", ModValue.of(16), 48, 1)
                .withTier("of_ardour", ModValue.of(20, 23), 66, 1)
                .withTier("of_suffusion", ModValue.of(24, 27), 81, 1)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_SUFFIX, "EnergyShieldRegenerationFoci",
            ModPreset.builder(AdditionType.global(StatIds.INC_ENERGY_SHIELD_RECHARGE_RATE))
                .withTier("of_buffering", ModValue.of(16), 48, 1000)
                .withTier("of_ardour", ModValue.of(20, 23), 66, 1000)
                .withTier("of_suffusion", ModValue.of(24, 27), 81, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "EnergyShieldRegenerationShield",
            ModPreset.builder(AdditionType.global(StatIds.INC_ENERGY_SHIELD_RECHARGE_RATE))
                .withTier("of_buffering", ModValue.of(16), 48, 500)
                .withTier("of_ardour", ModValue.of(20, 23), 66, 500)
                .withTier("of_suffusion", ModValue.of(24, 27), 81, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHEST_MOD_SUFFIX, "ReducedAilmentDuration",
            ModPreset.builder(AdditionType.global(StatIds.REDUCED_BLEEDING_DURATION))
                .withTier("of_sealing", ModValue.of(36), 21, 500)
                .withTier("of_damping", ModValue.of(36, 40), 21, 500)
                .withTier("of_the_antitoxin", ModValue.of(36, 40), 21, 500)
                .withTier("of_alleviation", ModValue.of(41, 45), 37, 500)
                .withTier("of_quashing", ModValue.of(41, 45), 37, 500)
                .withTier("of_the_remedy", ModValue.of(41, 45), 37, 500)
                .withTier("of_allaying", ModValue.of(46, 50), 50, 500)
                .withTier("of_quelling", ModValue.of(46, 50), 50, 500)
                .withTier("of_the_cure", ModValue.of(46, 50), 50, 500)
                .withTier("of_assuaging", ModValue.of(51, 55), 64, 500)
                .withTier("of_quenching", ModValue.of(51, 55), 64, 500)
                .withTier("of_the_panacea", ModValue.of(51, 55), 64, 500)
                .withTier("of_staunching", ModValue.of(56, 60), 76, 500)
                .withTier("of_dousing", ModValue.of(56, 60), 76, 500)
                .withTier("of_the_antidote", ModValue.of(56, 60), 76, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FEET_MOD_SUFFIX, "ReducedAilmentDuration",
            ModPreset.builder(AdditionType.global(StatIds.REDUCED_BLEEDING_DURATION))
                .withTier("of_convection", ModValue.of(36), 20, 500)
                .withTier("of_heating", ModValue.of(36, 40), 20, 500)
                .withTier("of_earthing", ModValue.of(36, 40), 20, 500)
                .withTier("of_fluidity", ModValue.of(41, 45), 36, 500)
                .withTier("of_unfreezing", ModValue.of(41, 45), 36, 500)
                .withTier("of_insulation", ModValue.of(41, 45), 36, 500)
                .withTier("of_entropy", ModValue.of(46, 50), 49, 500)
                .withTier("of_defrosting", ModValue.of(46, 50), 49, 500)
                .withTier("of_the_impedance", ModValue.of(46, 50), 49, 500)
                .withTier("of_dissipation", ModValue.of(51, 55), 63, 500)
                .withTier("of_the_temperate", ModValue.of(51, 55), 63, 500)
                .withTier("of_the_dielectric", ModValue.of(51, 55), 63, 500)
                .withTier("of_the_reversal", ModValue.of(56, 60), 75, 500)
                .withTier("of_thawing", ModValue.of(56, 60), 75, 500)
                .withTier("of_grounding", ModValue.of(56, 60), 75, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "EvasionAppliesToDeflectionShield",
            ModPreset.builder(AdditionType.global(StatIds.EVASION_APPLIES_TO_DEFLECTION))
                .withTier("of_deflecting", ModValue.of(8), 1, 500)
                .withTier("of_bending", ModValue.of(12, 14), 16, 500)
                .withTier("of_curvation", ModValue.of(15, 17), 36, 500)
                .withTier("of_diversion", ModValue.of(18, 20), 48, 500)
                .withTier("of_flexure", ModValue.of(21, 23), 66, 500)
                .withTier("of_warping", ModValue.of(24, 26), 81, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FEET_MOD_PREFIX, "MovementVelocity",
            ModPreset.builder(ModAdditions.MOVEMENT_SPEED_INC)
                .withTier("runner_s", ModValue.of(10), 1, 1000)
                .withTier("sprinter_s", ModValue.of(15, 15), 16, 1000)
                .withTier("stallion_s", ModValue.of(20, 20), 33, 1000)
                .withTier("gazelle_s", ModValue.of(25, 25), 46, 1000)
                .withTier("cheetah_s", ModValue.of(30, 30), 65, 1000)
                .withTier("hellion_s", ModValue.of(35, 35), 82, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_PREFIX, "DefencesPercentAndStunThresholdBuckler",
            ModPreset.builder(MultiAddition.keyed(ModAdditions.ARMOR_ENERGY_INC_LOCAL, ModAdditions.STUN_THRESHOLD_INC))
                .withTier("mosquito_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(8, 13)), 10, 1000)
                .withTier("moth_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(14, 24)), 19, 1000)
                .withTier("butterfly_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(25, 40)), 38, 1000)
                .withTier("wasp_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(41, 63)), 48, 1000)
                .withTier("dragonfly_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(64, 94)), 63, 1000)
                .withTier("hummingbird_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(95, 136)), 74, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FEET_MOD_PREFIX, "DefencesPercentAndStunThreshold",
            ModPreset.builder(MultiAddition.keyed(ModAdditions.ARMOR_ENERGY_INC_LOCAL, ModAdditions.STUN_THRESHOLD_INC))
                .withTier("defender_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(8, 13)), 10, 1000)
                .withTier("captain_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(8, 13)), 10, 1000)
                .withTier("beetle_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(8, 13)), 10, 1000)
                .withTier("pixie_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(8, 13)), 10, 1000)
                .withTier("mosquito_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(8, 13)), 10, 1000)
                .withTier("intuitive", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(8, 13)), 10, 1000)
                .withTier("protector_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(14, 24)), 19, 1000)
                .withTier("commander_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(14, 24)), 19, 1000)
                .withTier("crab_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(14, 24)), 19, 1000)
                .withTier("gremlin_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(14, 24)), 19, 1000)
                .withTier("moth_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(14, 24)), 19, 1000)
                .withTier("psychic", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(14, 24)), 19, 1000)
                .withTier("keeper_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(25, 40)), 38, 1000)
                .withTier("magnate_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(25, 40)), 38, 1000)
                .withTier("armadillo_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(25, 40)), 38, 1000)
                .withTier("boggart_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(25, 40)), 38, 1000)
                .withTier("butterfly_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(25, 40)), 38, 1000)
                .withTier("telepath_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(25, 40)), 38, 1000)
                .withTier("guardian_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(41, 63)), 48, 1000)
                .withTier("marshal_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(41, 63)), 48, 1000)
                .withTier("rhino_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(41, 63)), 48, 1000)
                .withTier("naga_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(41, 63)), 48, 1000)
                .withTier("wasp_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(41, 63)), 48, 1000)
                .withTier("illusionist_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(41, 63)), 48, 1000)
                .withTier("warden_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(64, 94)), 63, 1000)
                .withTier("general_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(64, 94)), 63, 1000)
                .withTier("elephant_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(64, 94)), 63, 1000)
                .withTier("djinn_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(64, 94)), 63, 1000)
                .withTier("dragonfly_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(64, 94)), 63, 1000)
                .withTier("mentalist_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(64, 94)), 63, 1000)
                .withTier("sentinel_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(95, 136)), 74, 1000)
                .withTier("warlord_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(95, 136)), 74, 1000)
                .withTier("mammoth_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(95, 136)), 74, 1000)
                .withTier("seraphim_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(95, 136)), 74, 1000)
                .withTier("hummingbird_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(95, 136)), 74, 1000)
                .withTier("trickster_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(95, 136)), 74, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_PREFIX, "DefencesPercentAndStunThresholdShield",
            ModPreset.builder(MultiAddition.keyed(ModAdditions.ARMOR_ENERGY_INC_LOCAL, ModAdditions.STUN_THRESHOLD_INC))
                .withTier("defender_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(8, 13)), 10, 1000)
                .withTier("captain_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(8, 13)), 10, 1000)
                .withTier("beetle_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(8, 13)), 10, 1000)
                .withTier("protector_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(14, 24)), 19, 1000)
                .withTier("commander_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(14, 24)), 19, 1000)
                .withTier("crab_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(14, 24)), 19, 1000)
                .withTier("keeper_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(25, 40)), 38, 1000)
                .withTier("magnate_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(25, 40)), 38, 1000)
                .withTier("armadillo_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(25, 40)), 38, 1000)
                .withTier("guardian_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(41, 63)), 48, 1000)
                .withTier("marshal_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(41, 63)), 48, 1000)
                .withTier("rhino_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(41, 63)), 48, 1000)
                .withTier("warden_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(64, 94)), 63, 1000)
                .withTier("general_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(64, 94)), 63, 1000)
                .withTier("elephant_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(64, 94)), 63, 1000)
                .withTier("sentinel_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(95, 136)), 74, 1000)
                .withTier("warlord_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(95, 136)), 74, 1000)
                .withTier("mammoth_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(95, 136)), 74, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BOW_MOD_PREFIX, "LightningDamage",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("humming", Map.of("min", ModValue.of(1), "max", ModValue.of(4, 6)), 1, 1200)
                .withTier("buzzing", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(13, 19)), 8, 1200)
                .withTier("snapping", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(20, 30)), 16, 1200)
                .withTier("crackling", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(36, 52)), 33, 1200)
                .withTier("sparking", Map.of("min", ModValue.of(1, 3), "max", ModValue.of(55, 60)), 46, 1200)
                .withTier("arcing", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(63, 82)), 54, 1200)
                .withTier("shocking", Map.of("min", ModValue.of(1, 6), "max", ModValue.of(85, 107)), 60, 720)
                .withTier("discharging", Map.of("min", ModValue.of(1, 8), "max", ModValue.of(111, 152)), 65, 480)
                .withTier("electrocuting", Map.of("min", ModValue.of(1, 10), "max", ModValue.of(157, 196)), 75, 300)
                .withTier("vapourising", Map.of("min", ModValue.of(1, 12), "max", ModValue.of(202, 234)), 81, 120)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CROSSBOW_MOD_PREFIX, "LightningDamage",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("humming", Map.of("min", ModValue.of(1), "max", ModValue.of(7, 10)), 1, 1000)
                .withTier("buzzing", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(19, 27)), 8, 1000)
                .withTier("snapping", Map.of("min", ModValue.of(1, 3), "max", ModValue.of(31, 43)), 16, 1000)
                .withTier("crackling", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(53, 76)), 33, 1000)
                .withTier("sparking", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(80, 88)), 46, 1000)
                .withTier("arcing", Map.of("min", ModValue.of(1, 6), "max", ModValue.of(93, 122)), 54, 1000)
                .withTier("shocking", Map.of("min", ModValue.of(1, 8), "max", ModValue.of(128, 162)), 60, 600)
                .withTier("discharging", Map.of("min", ModValue.of(1, 13), "max", ModValue.of(168, 231)), 65, 400)
                .withTier("electrocuting", Map.of("min", ModValue.of(1, 16), "max", ModValue.of(239, 300)), 75, 250)
                .withTier("vapourising", Map.of("min", ModValue.of(1, 19), "max", ModValue.of(310, 358)), 81, 100)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.LEGS_MOD_PREFIX, "LightningDamage",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("humming", Map.of("min", ModValue.of(1), "max", ModValue.of(4, 6)), 1, 500)
                .withTier("buzzing", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(10, 15)), 8, 500)
                .withTier("snapping", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(16, 22)), 16, 500)
                .withTier("crackling", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(23, 27)), 33, 500)
                .withTier("sparking", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(28, 32)), 46, 500)
                .withTier("arcing", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(33, 40)), 54, 500)
                .withTier("shocking", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(41, 47)), 60, 400)
                .withTier("discharging", Map.of("min", ModValue.of(1, 3), "max", ModValue.of(48, 59)), 65, 300)
                .withTier("electrocuting", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(60, 71)), 75, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_PREFIX, "LightningDamage",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("humming", Map.of("min", ModValue.of(1), "max", ModValue.of(4, 6)), 1, 500)
                .withTier("buzzing", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(10, 15)), 8, 500)
                .withTier("snapping", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(16, 22)), 16, 500)
                .withTier("crackling", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(23, 27)), 33, 500)
                .withTier("sparking", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(28, 32)), 46, 500)
                .withTier("arcing", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(33, 40)), 54, 500)
                .withTier("shocking", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(41, 47)), 60, 400)
                .withTier("discharging", Map.of("min", ModValue.of(1, 3), "max", ModValue.of(48, 59)), 65, 300)
                .withTier("electrocuting", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(60, 71)), 75, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_PREFIX, "LightningDamage",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("humming", Map.of("min", ModValue.of(1), "max", ModValue.of(4, 6)), 1, 500)
                .withTier("buzzing", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(10, 15)), 8, 500)
                .withTier("snapping", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(16, 22)), 16, 500)
                .withTier("crackling", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(23, 27)), 33, 500)
                .withTier("sparking", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(28, 32)), 46, 500)
                .withTier("arcing", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(33, 40)), 54, 500)
                .withTier("shocking", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(41, 47)), 60, 400)
                .withTier("discharging", Map.of("min", ModValue.of(1, 3), "max", ModValue.of(48, 59)), 65, 300)
                .withTier("electrocuting", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(60, 71)), 75, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ONE_HAND_MACE_MOD_PREFIX, "LightningDamage",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("humming", Map.of("min", ModValue.of(1), "max", ModValue.of(4, 6)), 1, 800)
                .withTier("buzzing", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(13, 19)), 8, 800)
                .withTier("snapping", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(20, 30)), 16, 800)
                .withTier("crackling", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(36, 52)), 33, 800)
                .withTier("sparking", Map.of("min", ModValue.of(1, 3), "max", ModValue.of(55, 60)), 46, 800)
                .withTier("arcing", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(63, 82)), 54, 800)
                .withTier("shocking", Map.of("min", ModValue.of(1, 6), "max", ModValue.of(85, 107)), 60, 480)
                .withTier("discharging", Map.of("min", ModValue.of(1, 8), "max", ModValue.of(111, 152)), 65, 320)
                .withTier("electrocuting", Map.of("min", ModValue.of(1, 10), "max", ModValue.of(157, 196)), 75, 200)
                .withTier("vapourising", Map.of("min", ModValue.of(1, 12), "max", ModValue.of(202, 234)), 81, 80)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUARTERSTAFF_MOD_PREFIX, "LightningDamage",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("humming", Map.of("min", ModValue.of(1), "max", ModValue.of(7, 10)), 1, 1100)
                .withTier("buzzing", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(19, 27)), 8, 1100)
                .withTier("snapping", Map.of("min", ModValue.of(1, 3), "max", ModValue.of(31, 43)), 16, 1100)
                .withTier("crackling", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(53, 76)), 33, 1100)
                .withTier("sparking", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(80, 88)), 46, 1100)
                .withTier("arcing", Map.of("min", ModValue.of(1, 6), "max", ModValue.of(93, 122)), 54, 1100)
                .withTier("shocking", Map.of("min", ModValue.of(1, 8), "max", ModValue.of(128, 162)), 60, 660)
                .withTier("discharging", Map.of("min", ModValue.of(1, 13), "max", ModValue.of(168, 231)), 65, 440)
                .withTier("electrocuting", Map.of("min", ModValue.of(1, 16), "max", ModValue.of(239, 300)), 75, 275)
                .withTier("vapourising", Map.of("min", ModValue.of(1, 19), "max", ModValue.of(310, 358)), 81, 110)
                .build());

    }

    private static void s3(BootstrapContext<ItemInstance> bootstrap) {
        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_PREFIX, "LightningDamage",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("humming", Map.of("min", ModValue.of(1), "max", ModValue.of(5, 7)), 1, 500)
                .withTier("buzzing", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(10, 15)), 8, 500)
                .withTier("snapping", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(16, 22)), 16, 500)
                .withTier("crackling", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(23, 27)), 33, 500)
                .withTier("sparking", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(28, 32)), 46, 500)
                .withTier("arcing", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(33, 40)), 54, 500)
                .withTier("shocking", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(41, 47)), 60, 400)
                .withTier("discharging", Map.of("min", ModValue.of(1, 3), "max", ModValue.of(48, 59)), 65, 300)
                .withTier("electrocuting", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(60, 71)), 75, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SPEAR_MOD_PREFIX, "LightningDamage",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("humming", Map.of("min", ModValue.of(1), "max", ModValue.of(4, 6)), 1, 1100)
                .withTier("buzzing", Map.of("min", ModValue.of(1, 1), "max", ModValue.of(13, 19)), 8, 1100)
                .withTier("snapping", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(20, 30)), 16, 1100)
                .withTier("crackling", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(36, 52)), 33, 1100)
                .withTier("sparking", Map.of("min", ModValue.of(1, 3), "max", ModValue.of(55, 60)), 46, 1100)
                .withTier("arcing", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(63, 82)), 54, 1100)
                .withTier("shocking", Map.of("min", ModValue.of(1, 6), "max", ModValue.of(85, 107)), 60, 660)
                .withTier("discharging", Map.of("min", ModValue.of(1, 8), "max", ModValue.of(111, 152)), 65, 440)
                .withTier("electrocuting", Map.of("min", ModValue.of(1, 10), "max", ModValue.of(157, 196)), 75, 275)
                .withTier("vapourising", Map.of("min", ModValue.of(1, 12), "max", ModValue.of(202, 234)), 81, 110)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.STAFF_MOD_PREFIX, "LightningDamage",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("deadly", Map.of("min", ModValue.of(26), "max", ModValue.of(26, 30)), 5, 500)
                .withTier("lethal", Map.of("min", ModValue.of(31, 36), "max", ModValue.of(31, 36)), 16, 500)
                .withTier("fatal", Map.of("min", ModValue.of(37, 42), "max", ModValue.of(37, 42)), 33, 500)
                .withTier("vorpal", Map.of("min", ModValue.of(43, 48), "max", ModValue.of(43, 48)), 46, 500)
                .withTier("electrifying", Map.of("min", ModValue.of(49, 54), "max", ModValue.of(49, 54)), 60, 500)
                .withTier("stormbound", Map.of("min", ModValue.of(55, 60), "max", ModValue.of(55, 60)), 80, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TALISMAN_MOD_PREFIX, "LightningDamage",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("humming", Map.of("min", ModValue.of(1), "max", ModValue.of(7, 10)), 1, 800)
                .withTier("buzzing", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(19, 27)), 8, 800)
                .withTier("snapping", Map.of("min", ModValue.of(1, 3), "max", ModValue.of(31, 43)), 16, 800)
                .withTier("crackling", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(53, 76)), 33, 800)
                .withTier("sparking", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(80, 88)), 46, 800)
                .withTier("arcing", Map.of("min", ModValue.of(1, 6), "max", ModValue.of(93, 122)), 54, 800)
                .withTier("shocking", Map.of("min", ModValue.of(1, 8), "max", ModValue.of(128, 162)), 60, 480)
                .withTier("discharging", Map.of("min", ModValue.of(1, 13), "max", ModValue.of(168, 231)), 65, 360)
                .withTier("electrocuting", Map.of("min", ModValue.of(1, 16), "max", ModValue.of(239, 300)), 75, 200)
                .withTier("vapourising", Map.of("min", ModValue.of(1, 19), "max", ModValue.of(310, 358)), 81, 80)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TWO_HAND_MACE_MOD_PREFIX, "LightningDamage",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("humming", Map.of("min", ModValue.of(1), "max", ModValue.of(7, 10)), 1, 800)
                .withTier("buzzing", Map.of("min", ModValue.of(1, 2), "max", ModValue.of(19, 27)), 8, 800)
                .withTier("snapping", Map.of("min", ModValue.of(1, 3), "max", ModValue.of(31, 43)), 16, 800)
                .withTier("crackling", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(53, 76)), 33, 800)
                .withTier("sparking", Map.of("min", ModValue.of(1, 4), "max", ModValue.of(80, 88)), 46, 800)
                .withTier("arcing", Map.of("min", ModValue.of(1, 6), "max", ModValue.of(93, 122)), 54, 800)
                .withTier("shocking", Map.of("min", ModValue.of(1, 8), "max", ModValue.of(128, 162)), 60, 480)
                .withTier("discharging", Map.of("min", ModValue.of(1, 13), "max", ModValue.of(168, 231)), 65, 320)
                .withTier("electrocuting", Map.of("min", ModValue.of(1, 16), "max", ModValue.of(239, 300)), 75, 200)
                .withTier("vapourising", Map.of("min", ModValue.of(1, 19), "max", ModValue.of(310, 358)), 81, 80)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WAND_MOD_PREFIX, "LightningDamage",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX)
                .withTier("deadly", Map.of("min", ModValue.of(13), "max", ModValue.of(13, 15)), 5, 500)
                .withTier("lethal", Map.of("min", ModValue.of(16, 18), "max", ModValue.of(16, 18)), 16, 500)
                .withTier("fatal", Map.of("min", ModValue.of(19, 21), "max", ModValue.of(19, 21)), 33, 500)
                .withTier("vorpal", Map.of("min", ModValue.of(22, 24), "max", ModValue.of(22, 24)), 46, 500)
                .withTier("electrifying", Map.of("min", ModValue.of(25, 27), "max", ModValue.of(25, 27)), 60, 500)
                .withTier("stormbound", Map.of("min", ModValue.of(28, 30), "max", ModValue.of(28, 30)), 80, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.LEGS_MOD_PREFIX, "ColdDamage",
            ModPreset.builder(ModAdditions.ADD_COLD_DAMAGE_MINMAX)
                .withTier("frosted", Map.of("min", ModValue.of(1), "max", ModValue.of(2, 3)), 1, 500)
                .withTier("chilled", Map.of("min", ModValue.of(3, 4), "max", ModValue.of(5, 8)), 8, 500)
                .withTier("icy", Map.of("min", ModValue.of(5, 6), "max", ModValue.of(9, 11)), 16, 500)
                .withTier("frigid", Map.of("min", ModValue.of(7, 8), "max", ModValue.of(12, 14)), 33, 500)
                .withTier("freezing", Map.of("min", ModValue.of(9, 10), "max", ModValue.of(15, 17)), 46, 500)
                .withTier("frozen", Map.of("min", ModValue.of(11, 13), "max", ModValue.of(18, 21)), 54, 500)
                .withTier("glaciated", Map.of("min", ModValue.of(14, 15), "max", ModValue.of(22, 24)), 60, 400)
                .withTier("polar", Map.of("min", ModValue.of(16, 20), "max", ModValue.of(25, 31)), 65, 300)
                .withTier("entombing", Map.of("min", ModValue.of(21, 24), "max", ModValue.of(32, 37)), 75, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_PREFIX, "ColdDamage",
            ModPreset.builder(ModAdditions.ADD_COLD_DAMAGE_MINMAX)
                .withTier("frosted", Map.of("min", ModValue.of(1), "max", ModValue.of(2, 3)), 1, 500)
                .withTier("chilled", Map.of("min", ModValue.of(3, 4), "max", ModValue.of(5, 8)), 8, 500)
                .withTier("icy", Map.of("min", ModValue.of(5, 6), "max", ModValue.of(9, 11)), 16, 500)
                .withTier("frigid", Map.of("min", ModValue.of(7, 8), "max", ModValue.of(12, 14)), 33, 500)
                .withTier("freezing", Map.of("min", ModValue.of(9, 10), "max", ModValue.of(15, 17)), 46, 500)
                .withTier("frozen", Map.of("min", ModValue.of(11, 13), "max", ModValue.of(18, 21)), 54, 500)
                .withTier("glaciated", Map.of("min", ModValue.of(14, 15), "max", ModValue.of(22, 24)), 60, 400)
                .withTier("polar", Map.of("min", ModValue.of(16, 20), "max", ModValue.of(25, 31)), 65, 300)
                .withTier("entombing", Map.of("min", ModValue.of(21, 24), "max", ModValue.of(32, 37)), 75, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_PREFIX, "ColdDamage",
            ModPreset.builder(ModAdditions.ADD_COLD_DAMAGE_MINMAX)
                .withTier("frosted", Map.of("min", ModValue.of(1), "max", ModValue.of(2, 3)), 1, 500)
                .withTier("chilled", Map.of("min", ModValue.of(3, 4), "max", ModValue.of(5, 8)), 8, 500)
                .withTier("icy", Map.of("min", ModValue.of(5, 6), "max", ModValue.of(9, 11)), 16, 500)
                .withTier("frigid", Map.of("min", ModValue.of(7, 8), "max", ModValue.of(12, 14)), 33, 500)
                .withTier("freezing", Map.of("min", ModValue.of(9, 10), "max", ModValue.of(15, 17)), 46, 500)
                .withTier("frozen", Map.of("min", ModValue.of(11, 13), "max", ModValue.of(18, 21)), 54, 500)
                .withTier("glaciated", Map.of("min", ModValue.of(14, 15), "max", ModValue.of(22, 24)), 60, 400)
                .withTier("polar", Map.of("min", ModValue.of(16, 20), "max", ModValue.of(25, 31)), 65, 300)
                .withTier("entombing", Map.of("min", ModValue.of(21, 24), "max", ModValue.of(32, 37)), 75, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUARTERSTAFF_MOD_PREFIX, "ColdDamage",
            ModPreset.builder(ModAdditions.ADD_COLD_DAMAGE_MINMAX)
                .withTier("frosted", Map.of("min", ModValue.of(2), "max", ModValue.of(4, 6)), 1, 900)
                .withTier("chilled", Map.of("min", ModValue.of(5, 8), "max", ModValue.of(9, 14)), 8, 900)
                .withTier("icy", Map.of("min", ModValue.of(10, 14), "max", ModValue.of(15, 23)), 16, 900)
                .withTier("frigid", Map.of("min", ModValue.of(16, 23), "max", ModValue.of(25, 35)), 33, 900)
                .withTier("freezing", Map.of("min", ModValue.of(25, 30), "max", ModValue.of(38, 46)), 46, 900)
                .withTier("frozen", Map.of("min", ModValue.of(32, 43), "max", ModValue.of(49, 66)), 54, 900)
                .withTier("glaciated", Map.of("min", ModValue.of(46, 57), "max", ModValue.of(70, 88)), 60, 540)
                .withTier("polar", Map.of("min", ModValue.of(60, 80), "max", ModValue.of(92, 121)), 65, 360)
                .withTier("entombing", Map.of("min", ModValue.of(84, 107), "max", ModValue.of(126, 161)), 75, 225)
                .withTier("crystalising", Map.of("min", ModValue.of(112, 124), "max", ModValue.of(168, 189)), 81, 90)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_PREFIX, "ColdDamage",
            ModPreset.builder(ModAdditions.ADD_COLD_DAMAGE_MINMAX)
                .withTier("frosted", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 4)), 1, 500)
                .withTier("chilled", Map.of("min", ModValue.of(3, 4), "max", ModValue.of(5, 8)), 8, 500)
                .withTier("icy", Map.of("min", ModValue.of(5, 6), "max", ModValue.of(9, 11)), 16, 500)
                .withTier("frigid", Map.of("min", ModValue.of(7, 8), "max", ModValue.of(12, 14)), 33, 500)
                .withTier("freezing", Map.of("min", ModValue.of(9, 10), "max", ModValue.of(15, 17)), 46, 500)
                .withTier("frozen", Map.of("min", ModValue.of(11, 13), "max", ModValue.of(18, 21)), 54, 500)
                .withTier("glaciated", Map.of("min", ModValue.of(14, 15), "max", ModValue.of(22, 24)), 60, 400)
                .withTier("polar", Map.of("min", ModValue.of(16, 20), "max", ModValue.of(25, 31)), 65, 300)
                .withTier("entombing", Map.of("min", ModValue.of(21, 24), "max", ModValue.of(32, 37)), 75, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.STAFF_MOD_PREFIX, "ColdDamage",
            ModPreset.builder(ModAdditions.ADD_COLD_DAMAGE_MINMAX)
                .withTier("malignant", Map.of("min", ModValue.of(26), "max", ModValue.of(26, 30)), 5, 500)
                .withTier("pernicious", Map.of("min", ModValue.of(31, 36), "max", ModValue.of(31, 36)), 16, 500)
                .withTier("destructive", Map.of("min", ModValue.of(37, 42), "max", ModValue.of(37, 42)), 33, 500)
                .withTier("malicious", Map.of("min", ModValue.of(43, 48), "max", ModValue.of(43, 48)), 46, 500)
                .withTier("ruthless", Map.of("min", ModValue.of(49, 54), "max", ModValue.of(49, 54)), 60, 500)
                .withTier("frostbound", Map.of("min", ModValue.of(55, 60), "max", ModValue.of(55, 60)), 80, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TALISMAN_MOD_PREFIX, "ColdDamage",
            ModPreset.builder(ModAdditions.ADD_COLD_DAMAGE_MINMAX)
                .withTier("frosted", Map.of("min", ModValue.of(2), "max", ModValue.of(4, 6)), 1, 1000)
                .withTier("chilled", Map.of("min", ModValue.of(5, 8), "max", ModValue.of(9, 14)), 8, 1000)
                .withTier("icy", Map.of("min", ModValue.of(10, 14), "max", ModValue.of(15, 23)), 16, 1000)
                .withTier("frigid", Map.of("min", ModValue.of(16, 23), "max", ModValue.of(25, 35)), 33, 1000)
                .withTier("freezing", Map.of("min", ModValue.of(25, 30), "max", ModValue.of(38, 46)), 46, 1000)
                .withTier("frozen", Map.of("min", ModValue.of(32, 43), "max", ModValue.of(49, 66)), 54, 1000)
                .withTier("glaciated", Map.of("min", ModValue.of(46, 57), "max", ModValue.of(70, 88)), 60, 600)
                .withTier("polar", Map.of("min", ModValue.of(60, 80), "max", ModValue.of(92, 121)), 65, 400)
                .withTier("entombing", Map.of("min", ModValue.of(84, 107), "max", ModValue.of(126, 161)), 75, 250)
                .withTier("crystalising", Map.of("min", ModValue.of(112, 124), "max", ModValue.of(168, 189)), 81, 100)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WAND_MOD_PREFIX, "ColdDamage",
            ModPreset.builder(ModAdditions.ADD_COLD_DAMAGE_MINMAX)
                .withTier("malignant", Map.of("min", ModValue.of(13), "max", ModValue.of(13, 15)), 5, 500)
                .withTier("pernicious", Map.of("min", ModValue.of(16, 18), "max", ModValue.of(16, 18)), 16, 500)
                .withTier("destructive", Map.of("min", ModValue.of(19, 21), "max", ModValue.of(19, 21)), 33, 500)
                .withTier("malicious", Map.of("min", ModValue.of(22, 24), "max", ModValue.of(22, 24)), 46, 500)
                .withTier("ruthless", Map.of("min", ModValue.of(25, 27), "max", ModValue.of(25, 27)), 60, 500)
                .withTier("frostbound", Map.of("min", ModValue.of(28, 30), "max", ModValue.of(28, 30)), 80, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BOW_MOD_PREFIX, "FireDamage",
            ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("heated", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 5)), 1, 800)
                .withTier("smouldering", Map.of("min", ModValue.of(4, 6), "max", ModValue.of(7, 10)), 8, 800)
                .withTier("smoking", Map.of("min", ModValue.of(7, 11), "max", ModValue.of(13, 19)), 16, 800)
                .withTier("burning", Map.of("min", ModValue.of(13, 19), "max", ModValue.of(21, 29)), 33, 800)
                .withTier("flaming", Map.of("min", ModValue.of(20, 24), "max", ModValue.of(32, 37)), 46, 800)
                .withTier("scorching", Map.of("min", ModValue.of(25, 33), "max", ModValue.of(38, 54)), 54, 800)
                .withTier("incinerating", Map.of("min", ModValue.of(35, 44), "max", ModValue.of(56, 71)), 60, 480)
                .withTier("blasting", Map.of("min", ModValue.of(47, 59), "max", ModValue.of(74, 97)), 65, 320)
                .withTier("cremating", Map.of("min", ModValue.of(62, 85), "max", ModValue.of(101, 129)), 75, 200)
                .withTier("carbonising", Map.of("min", ModValue.of(88, 101), "max", ModValue.of(133, 154)), 81, 80)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CROSSBOW_MOD_PREFIX, "FireDamage",
            ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("heated", Map.of("min", ModValue.of(2), "max", ModValue.of(5, 7)), 1, 1000)
                .withTier("smouldering", Map.of("min", ModValue.of(6, 9), "max", ModValue.of(10, 16)), 8, 1000)
                .withTier("smoking", Map.of("min", ModValue.of(11, 17), "max", ModValue.of(19, 28)), 16, 1000)
                .withTier("burning", Map.of("min", ModValue.of(19, 27), "max", ModValue.of(30, 42)), 33, 1000)
                .withTier("flaming", Map.of("min", ModValue.of(30, 37), "max", ModValue.of(45, 56)), 46, 1000)
                .withTier("scorching", Map.of("min", ModValue.of(39, 53), "max", ModValue.of(59, 80)), 54, 1000)
                .withTier("incinerating", Map.of("min", ModValue.of(56, 70), "max", ModValue.of(84, 107)), 60, 600)
                .withTier("blasting", Map.of("min", ModValue.of(73, 97), "max", ModValue.of(112, 149)), 65, 400)
                .withTier("cremating", Map.of("min", ModValue.of(102, 130), "max", ModValue.of(155, 198)), 75, 250)
                .withTier("carbonising", Map.of("min", ModValue.of(135, 156), "max", ModValue.of(205, 236)), 81, 100)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.LEGS_MOD_PREFIX, "FireDamage",
            ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("heated", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 3)), 1, 500)
                .withTier("smouldering", Map.of("min", ModValue.of(3, 5), "max", ModValue.of(6, 9)), 8, 500)
                .withTier("smoking", Map.of("min", ModValue.of(6, 8), "max", ModValue.of(10, 13)), 16, 500)
                .withTier("burning", Map.of("min", ModValue.of(9, 11), "max", ModValue.of(14, 17)), 33, 500)
                .withTier("flaming", Map.of("min", ModValue.of(12, 13), "max", ModValue.of(18, 20)), 46, 500)
                .withTier("scorching", Map.of("min", ModValue.of(11, 16), "max", ModValue.of(21, 26)), 54, 500)
                .withTier("incinerating", Map.of("min", ModValue.of(13, 19), "max", ModValue.of(27, 32)), 60, 400)
                .withTier("blasting", Map.of("min", ModValue.of(20, 24), "max", ModValue.of(33, 36)), 65, 300)
                .withTier("cremating", Map.of("min", ModValue.of(25, 29), "max", ModValue.of(37, 45)), 75, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_PREFIX, "FireDamage",
            ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("heated", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 3)), 1, 500)
                .withTier("smouldering", Map.of("min", ModValue.of(3, 5), "max", ModValue.of(6, 9)), 8, 500)
                .withTier("smoking", Map.of("min", ModValue.of(6, 8), "max", ModValue.of(10, 13)), 16, 500)
                .withTier("burning", Map.of("min", ModValue.of(9, 11), "max", ModValue.of(14, 17)), 33, 500)
                .withTier("flaming", Map.of("min", ModValue.of(12, 13), "max", ModValue.of(18, 20)), 46, 500)
                .withTier("scorching", Map.of("min", ModValue.of(11, 16), "max", ModValue.of(21, 26)), 54, 500)
                .withTier("incinerating", Map.of("min", ModValue.of(13, 19), "max", ModValue.of(27, 32)), 60, 400)
                .withTier("blasting", Map.of("min", ModValue.of(20, 24), "max", ModValue.of(33, 36)), 65, 300)
                .withTier("cremating", Map.of("min", ModValue.of(25, 29), "max", ModValue.of(37, 45)), 75, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_PREFIX, "FireDamage",
            ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("heated", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 3)), 1, 500)
                .withTier("smouldering", Map.of("min", ModValue.of(3, 5), "max", ModValue.of(6, 9)), 8, 500)
                .withTier("smoking", Map.of("min", ModValue.of(6, 8), "max", ModValue.of(10, 13)), 16, 500)
                .withTier("burning", Map.of("min", ModValue.of(9, 11), "max", ModValue.of(14, 17)), 33, 500)
                .withTier("flaming", Map.of("min", ModValue.of(12, 13), "max", ModValue.of(18, 20)), 46, 500)
                .withTier("scorching", Map.of("min", ModValue.of(11, 16), "max", ModValue.of(21, 26)), 54, 500)
                .withTier("incinerating", Map.of("min", ModValue.of(13, 19), "max", ModValue.of(27, 32)), 60, 400)
                .withTier("blasting", Map.of("min", ModValue.of(20, 24), "max", ModValue.of(33, 36)), 65, 300)
                .withTier("cremating", Map.of("min", ModValue.of(25, 29), "max", ModValue.of(37, 45)), 75, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.ONE_HAND_MACE_MOD_PREFIX, "FireDamage",
            ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("heated", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 5)), 1, 1200)
                .withTier("smouldering", Map.of("min", ModValue.of(4, 6), "max", ModValue.of(7, 10)), 8, 1200)
                .withTier("smoking", Map.of("min", ModValue.of(7, 11), "max", ModValue.of(13, 19)), 16, 1200)
                .withTier("burning", Map.of("min", ModValue.of(13, 19), "max", ModValue.of(21, 29)), 33, 1200)
                .withTier("flaming", Map.of("min", ModValue.of(20, 24), "max", ModValue.of(32, 37)), 46, 1200)
                .withTier("scorching", Map.of("min", ModValue.of(25, 33), "max", ModValue.of(38, 54)), 54, 1200)
                .withTier("incinerating", Map.of("min", ModValue.of(35, 44), "max", ModValue.of(56, 71)), 60, 720)
                .withTier("blasting", Map.of("min", ModValue.of(47, 59), "max", ModValue.of(74, 97)), 65, 480)
                .withTier("cremating", Map.of("min", ModValue.of(62, 85), "max", ModValue.of(101, 129)), 75, 300)
                .withTier("carbonising", Map.of("min", ModValue.of(88, 101), "max", ModValue.of(133, 154)), 81, 120)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUARTERSTAFF_MOD_PREFIX, "FireDamage",
            ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("heated", Map.of("min", ModValue.of(2), "max", ModValue.of(5, 7)), 1, 800)
                .withTier("smouldering", Map.of("min", ModValue.of(6, 9), "max", ModValue.of(10, 16)), 8, 800)
                .withTier("smoking", Map.of("min", ModValue.of(11, 17), "max", ModValue.of(19, 28)), 16, 800)
                .withTier("burning", Map.of("min", ModValue.of(19, 27), "max", ModValue.of(30, 42)), 33, 800)
                .withTier("flaming", Map.of("min", ModValue.of(30, 37), "max", ModValue.of(45, 56)), 46, 800)
                .withTier("scorching", Map.of("min", ModValue.of(39, 53), "max", ModValue.of(59, 80)), 54, 800)
                .withTier("incinerating", Map.of("min", ModValue.of(56, 70), "max", ModValue.of(84, 107)), 60, 480)
                .withTier("blasting", Map.of("min", ModValue.of(73, 97), "max", ModValue.of(112, 149)), 65, 320)
                .withTier("cremating", Map.of("min", ModValue.of(102, 130), "max", ModValue.of(155, 198)), 75, 200)
                .withTier("carbonising", Map.of("min", ModValue.of(135, 156), "max", ModValue.of(205, 236)), 81, 80)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_PREFIX, "FireDamage",
            ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("heated", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 4)), 1, 500)
                .withTier("smouldering", Map.of("min", ModValue.of(3, 5), "max", ModValue.of(6, 9)), 8, 500)
                .withTier("smoking", Map.of("min", ModValue.of(6, 8), "max", ModValue.of(10, 13)), 16, 500)
                .withTier("burning", Map.of("min", ModValue.of(9, 11), "max", ModValue.of(14, 17)), 33, 500)
                .withTier("flaming", Map.of("min", ModValue.of(12, 13), "max", ModValue.of(18, 20)), 46, 500)
                .withTier("scorching", Map.of("min", ModValue.of(14, 16), "max", ModValue.of(21, 26)), 54, 500)
                .withTier("incinerating", Map.of("min", ModValue.of(17, 19), "max", ModValue.of(27, 30)), 60, 400)
                .withTier("blasting", Map.of("min", ModValue.of(20, 24), "max", ModValue.of(31, 38)), 65, 300)
                .withTier("cremating", Map.of("min", ModValue.of(25, 29), "max", ModValue.of(39, 45)), 75, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SPEAR_MOD_PREFIX, "FireDamage",
            ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("heated", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 5)), 1, 900)
                .withTier("smouldering", Map.of("min", ModValue.of(4, 6), "max", ModValue.of(7, 10)), 8, 900)
                .withTier("smoking", Map.of("min", ModValue.of(7, 11), "max", ModValue.of(13, 19)), 16, 900)
                .withTier("burning", Map.of("min", ModValue.of(13, 19), "max", ModValue.of(21, 29)), 33, 900)
                .withTier("flaming", Map.of("min", ModValue.of(20, 24), "max", ModValue.of(32, 37)), 46, 900)
                .withTier("scorching", Map.of("min", ModValue.of(25, 33), "max", ModValue.of(38, 54)), 54, 900)
                .withTier("incinerating", Map.of("min", ModValue.of(35, 44), "max", ModValue.of(56, 71)), 60, 540)
                .withTier("blasting", Map.of("min", ModValue.of(47, 59), "max", ModValue.of(74, 97)), 65, 360)
                .withTier("cremating", Map.of("min", ModValue.of(62, 85), "max", ModValue.of(101, 129)), 75, 225)
                .withTier("carbonising", Map.of("min", ModValue.of(88, 101), "max", ModValue.of(133, 154)), 81, 90)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.STAFF_MOD_PREFIX, "FireDamage",
            ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("fervent", Map.of("min", ModValue.of(26), "max", ModValue.of(26, 30)), 5, 500)
                .withTier("ardent", Map.of("min", ModValue.of(31, 36), "max", ModValue.of(31, 36)), 16, 500)
                .withTier("fanatic_s", Map.of("min", ModValue.of(37, 42), "max", ModValue.of(37, 42)), 33, 500)
                .withTier("zealot_s", Map.of("min", ModValue.of(43, 48), "max", ModValue.of(43, 48)), 46, 500)
                .withTier("infernal", Map.of("min", ModValue.of(49, 54), "max", ModValue.of(49, 54)), 60, 500)
                .withTier("flamebound", Map.of("min", ModValue.of(55, 60), "max", ModValue.of(55, 60)), 80, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TALISMAN_MOD_PREFIX, "FireDamage",
            ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("heated", Map.of("min", ModValue.of(2), "max", ModValue.of(5, 7)), 1, 1100)
                .withTier("smouldering", Map.of("min", ModValue.of(6, 9), "max", ModValue.of(10, 16)), 8, 1100)
                .withTier("smoking", Map.of("min", ModValue.of(11, 17), "max", ModValue.of(19, 28)), 16, 1100)
                .withTier("burning", Map.of("min", ModValue.of(19, 27), "max", ModValue.of(30, 42)), 33, 1100)
                .withTier("flaming", Map.of("min", ModValue.of(30, 37), "max", ModValue.of(45, 56)), 46, 1100)
                .withTier("scorching", Map.of("min", ModValue.of(39, 53), "max", ModValue.of(59, 80)), 54, 1100)
                .withTier("incinerating", Map.of("min", ModValue.of(56, 70), "max", ModValue.of(84, 107)), 60, 660)
                .withTier("blasting", Map.of("min", ModValue.of(73, 97), "max", ModValue.of(112, 149)), 65, 440)
                .withTier("cremating", Map.of("min", ModValue.of(102, 130), "max", ModValue.of(155, 198)), 75, 275)
                .withTier("carbonising", Map.of("min", ModValue.of(135, 156), "max", ModValue.of(205, 236)), 81, 110)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TWO_HAND_MACE_MOD_PREFIX, "FireDamage",
            ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("heated", Map.of("min", ModValue.of(2), "max", ModValue.of(5, 7)), 1, 1200)
                .withTier("smouldering", Map.of("min", ModValue.of(6, 9), "max", ModValue.of(10, 16)), 8, 1200)
                .withTier("smoking", Map.of("min", ModValue.of(11, 17), "max", ModValue.of(19, 28)), 16, 1200)
                .withTier("burning", Map.of("min", ModValue.of(19, 27), "max", ModValue.of(30, 42)), 33, 1200)
                .withTier("flaming", Map.of("min", ModValue.of(30, 37), "max", ModValue.of(45, 56)), 46, 1200)
                .withTier("scorching", Map.of("min", ModValue.of(39, 53), "max", ModValue.of(59, 80)), 54, 1200)
                .withTier("incinerating", Map.of("min", ModValue.of(56, 70), "max", ModValue.of(84, 107)), 60, 720)
                .withTier("blasting", Map.of("min", ModValue.of(73, 97), "max", ModValue.of(112, 149)), 65, 480)
                .withTier("cremating", Map.of("min", ModValue.of(102, 130), "max", ModValue.of(155, 198)), 75, 300)
                .withTier("carbonising", Map.of("min", ModValue.of(135, 156), "max", ModValue.of(205, 236)), 81, 120)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WAND_MOD_PREFIX, "FireDamage",
            ModPreset.builder(ModAdditions.ADD_FIRE_DAMAGE_MINMAX)
                .withTier("fervent", Map.of("min", ModValue.of(13), "max", ModValue.of(13, 15)), 5, 500)
                .withTier("ardent", Map.of("min", ModValue.of(16, 18), "max", ModValue.of(16, 18)), 16, 500)
                .withTier("fanatic_s", Map.of("min", ModValue.of(19, 21), "max", ModValue.of(19, 21)), 33, 500)
                .withTier("zealot_s", Map.of("min", ModValue.of(22, 24), "max", ModValue.of(22, 24)), 46, 500)
                .withTier("infernal", Map.of("min", ModValue.of(25, 27), "max", ModValue.of(25, 27)), 60, 500)
                .withTier("flamebound", Map.of("min", ModValue.of(28, 30), "max", ModValue.of(28, 30)), 80, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.LEGS_MOD_PREFIX, "PhysicalDamage",
            ModPreset.builder(ModAdditions.ADD_PHYSICAL_DAMAGE_MINMAX)
                .withTier("glinting", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 3)), 1, 1000)
                .withTier("burnished", Map.of("min", ModValue.of(2, 3), "max", ModValue.of(4, 6)), 8, 1000)
                .withTier("polished", Map.of("min", ModValue.of(2, 4), "max", ModValue.of(5, 8)), 16, 1000)
                .withTier("honed", Map.of("min", ModValue.of(4, 6), "max", ModValue.of(8, 11)), 33, 1000)
                .withTier("gleaming", Map.of("min", ModValue.of(5, 7), "max", ModValue.of(9, 13)), 46, 1000)
                .withTier("annealed", Map.of("min", ModValue.of(6, 10), "max", ModValue.of(12, 17)), 54, 1000)
                .withTier("razor_sharp", Map.of("min", ModValue.of(7, 11), "max", ModValue.of(14, 20)), 60, 800)
                .withTier("tempered", Map.of("min", ModValue.of(10, 15), "max", ModValue.of(18, 26)), 65, 600)
                .withTier("flaring", Map.of("min", ModValue.of(12, 19), "max", ModValue.of(22, 32)), 75, 400)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_PREFIX, "PhysicalDamage",
            ModPreset.builder(ModAdditions.ADD_PHYSICAL_DAMAGE_MINMAX)
                .withTier("glinting", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 3)), 1, 1000)
                .withTier("burnished", Map.of("min", ModValue.of(2, 3), "max", ModValue.of(4, 6)), 8, 1000)
                .withTier("polished", Map.of("min", ModValue.of(2, 4), "max", ModValue.of(5, 8)), 16, 1000)
                .withTier("honed", Map.of("min", ModValue.of(4, 6), "max", ModValue.of(8, 11)), 33, 1000)
                .withTier("gleaming", Map.of("min", ModValue.of(5, 7), "max", ModValue.of(9, 13)), 46, 1000)
                .withTier("annealed", Map.of("min", ModValue.of(6, 10), "max", ModValue.of(12, 17)), 54, 1000)
                .withTier("razor_sharp", Map.of("min", ModValue.of(7, 11), "max", ModValue.of(14, 20)), 60, 800)
                .withTier("tempered", Map.of("min", ModValue.of(10, 15), "max", ModValue.of(18, 26)), 65, 600)
                .withTier("flaring", Map.of("min", ModValue.of(12, 19), "max", ModValue.of(22, 32)), 75, 400)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_PREFIX, "PhysicalDamage",
            ModPreset.builder(ModAdditions.ADD_PHYSICAL_DAMAGE_MINMAX)
                .withTier("glinting", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 3)), 1, 1000)
                .withTier("burnished", Map.of("min", ModValue.of(2, 3), "max", ModValue.of(4, 6)), 8, 1000)
                .withTier("polished", Map.of("min", ModValue.of(2, 4), "max", ModValue.of(5, 8)), 16, 1000)
                .withTier("honed", Map.of("min", ModValue.of(4, 6), "max", ModValue.of(8, 11)), 33, 1000)
                .withTier("gleaming", Map.of("min", ModValue.of(5, 7), "max", ModValue.of(9, 13)), 46, 1000)
                .withTier("annealed", Map.of("min", ModValue.of(6, 10), "max", ModValue.of(12, 17)), 54, 1000)
                .withTier("razor_sharp", Map.of("min", ModValue.of(7, 11), "max", ModValue.of(14, 20)), 60, 800)
                .withTier("tempered", Map.of("min", ModValue.of(10, 15), "max", ModValue.of(18, 26)), 65, 600)
                .withTier("flaring", Map.of("min", ModValue.of(12, 19), "max", ModValue.of(22, 32)), 75, 400)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_PREFIX, "PhysicalDamage",
            ModPreset.builder(ModAdditions.ADD_PHYSICAL_DAMAGE_MINMAX)
                .withTier("glinting", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 4)), 1, 500)
                .withTier("burnished", Map.of("min", ModValue.of(2, 3), "max", ModValue.of(4, 6)), 8, 500)
                .withTier("polished", Map.of("min", ModValue.of(2, 4), "max", ModValue.of(5, 8)), 16, 500)
                .withTier("honed", Map.of("min", ModValue.of(4, 6), "max", ModValue.of(8, 11)), 33, 500)
                .withTier("gleaming", Map.of("min", ModValue.of(5, 7), "max", ModValue.of(9, 13)), 46, 500)
                .withTier("annealed", Map.of("min", ModValue.of(6, 10), "max", ModValue.of(12, 17)), 54, 500)
                .withTier("razor_sharp", Map.of("min", ModValue.of(7, 11), "max", ModValue.of(14, 20)), 60, 400)
                .withTier("tempered", Map.of("min", ModValue.of(10, 15), "max", ModValue.of(18, 26)), 65, 300)
                .withTier("flaring", Map.of("min", ModValue.of(12, 19), "max", ModValue.of(22, 32)), 75, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.LEGS_MOD_SUFFIX, "IncreasedAttackSpeed",
            ModPreset.builder(ModAdditions.ATTACK_SPEED_INC)
                .withTier("of_skill", ModValue.of(5), 1, 500)
                .withTier("of_ease", ModValue.of(8, 10), 22, 500)
                .withTier("of_mastery", ModValue.of(11, 13), 37, 500)
                .withTier("of_renown", ModValue.of(14, 16), 60, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_SUFFIX, "IncreasedAttackSpeed",
            ModPreset.builder(ModAdditions.ATTACK_SPEED_INC)
                .withTier("of_skill", ModValue.of(5), 1, 500)
                .withTier("of_ease", ModValue.of(8, 10), 22, 500)
                .withTier("of_mastery", ModValue.of(11, 13), 37, 500)
                .withTier("of_renown", ModValue.of(14, 16), 60, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_SUFFIX, "IncreasedAttackSpeed",
            ModPreset.builder(ModAdditions.ATTACK_SPEED_INC)
                .withTier("of_skill", ModValue.of(5), 5, 1000)
                .withTier("of_ease", ModValue.of(8, 10), 20, 1000)
                .withTier("of_mastery", ModValue.of(11, 13), 35, 1000)
                .withTier("of_renown", ModValue.of(14, 16), 55, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.HEAD_MOD_SUFFIX, "LightRadiusAndAccuracy",
            ModPreset.builder(MultiAddition.keyed(AdditionType.global(StatIds.LIGHT_RADIUS), ModAdditions.ADD_ACCURACY))
                .withTier("of_shining", Map.of("value", ModValue.of(10), "value_2", ModValue.of(5, 5)), 8, 1000)
                .withTier("of_light", Map.of("value", ModValue.of(21, 40), "value_2", ModValue.of(10, 10)), 15, 1000)
                .withTier("of_radiance", Map.of("value", ModValue.of(41, 60), "value_2", ModValue.of(15, 15)), 30, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BOW_MOD_SUFFIX, "AdditionalArrows",
            ModPreset.builder(AdditionType.global(StatIds.SURPASSING_CHANCE_ADDITIONAL_ARROW))
                .withTier("of_surplus", ModValue.of(25), 46, 400)
                .withTier("of_splintering", ModValue.of(75, 100), 55, 300)
                .withTier("of_shards", ModValue.of(125, 150), 66, 200)
                .withTier("of_many", ModValue.of(175, 200), 82, 125)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_SUFFIX, "AdditionalArrows",
            ModPreset.builder(AdditionType.global(StatIds.SURPASSING_CHANCE_ADDITIONAL_ARROW))
                .withTier("of_surplus", ModValue.of(25), 46, 1)
                .withTier("of_splintering", ModValue.of(41, 60), 80, 1)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.LEGS_MOD_SUFFIX, "LifeGainedFromEnemyDeath",
            ModPreset.builder(AdditionType.global(StatIds.LIFE_GAINED_FROM_ENEMY_DEATH))
                .withTier("of_success", ModValue.of(4), 1, 750)
                .withTier("of_victory", ModValue.of(7, 9), 11, 750)
                .withTier("of_triumph", ModValue.of(10, 18), 22, 750)
                .withTier("of_conquest", ModValue.of(19, 28), 33, 750)
                .withTier("of_vanquishing", ModValue.of(29, 40), 44, 750)
                .withTier("of_valour", ModValue.of(41, 53), 55, 750)
                .withTier("of_glory", ModValue.of(54, 68), 66, 750)
                .withTier("of_legend", ModValue.of(69, 84), 77, 750)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_SUFFIX, "LifeGainedFromEnemyDeath",
            ModPreset.builder(AdditionType.global(StatIds.LIFE_GAINED_FROM_ENEMY_DEATH))
                .withTier("of_success", ModValue.of(4), 1, 750)
                .withTier("of_victory", ModValue.of(7, 9), 11, 750)
                .withTier("of_triumph", ModValue.of(10, 18), 22, 750)
                .withTier("of_conquest", ModValue.of(19, 28), 33, 750)
                .withTier("of_vanquishing", ModValue.of(29, 40), 44, 750)
                .withTier("of_valour", ModValue.of(41, 53), 55, 750)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_SUFFIX, "LifeGainedFromEnemyDeath",
            ModPreset.builder(AdditionType.global(StatIds.LIFE_GAINED_FROM_ENEMY_DEATH))
                .withTier("of_success", ModValue.of(4), 1, 750)
                .withTier("of_victory", ModValue.of(7, 9), 11, 750)
                .withTier("of_triumph", ModValue.of(10, 18), 22, 750)
                .withTier("of_conquest", ModValue.of(19, 28), 33, 750)
                .withTier("of_vanquishing", ModValue.of(29, 40), 44, 750)
                .withTier("of_valour", ModValue.of(41, 53), 55, 750)
                .build());

    }

    private static void s4(BootstrapContext<ItemInstance> bootstrap) {
        EquipmentGen.registerMod(bootstrap, EquipmentGen.LEGS_MOD_SUFFIX, "ManaGainedFromEnemyDeath",
            ModPreset.builder(AdditionType.global(StatIds.MANA_GAINED_FROM_ENEMY_DEATH))
                .withTier("of_absorption", ModValue.of(2), 1, 750)
                .withTier("of_osmosis", ModValue.of(4, 5), 12, 750)
                .withTier("of_infusion", ModValue.of(6, 9), 23, 750)
                .withTier("of_enveloping", ModValue.of(10, 14), 34, 750)
                .withTier("of_consumption", ModValue.of(15, 20), 45, 750)
                .withTier("of_siphoning", ModValue.of(21, 27), 56, 750)
                .withTier("of_devouring", ModValue.of(28, 35), 67, 750)
                .withTier("of_assimilation", ModValue.of(36, 45), 78, 750)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_SUFFIX, "ManaGainedFromEnemyDeath",
            ModPreset.builder(AdditionType.global(StatIds.MANA_GAINED_FROM_ENEMY_DEATH))
                .withTier("of_absorption", ModValue.of(2), 1, 750)
                .withTier("of_osmosis", ModValue.of(4, 5), 12, 750)
                .withTier("of_infusion", ModValue.of(6, 9), 23, 750)
                .withTier("of_enveloping", ModValue.of(10, 14), 34, 750)
                .withTier("of_consumption", ModValue.of(15, 20), 45, 750)
                .withTier("of_siphoning", ModValue.of(21, 27), 56, 750)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_SUFFIX, "ManaGainedFromEnemyDeath",
            ModPreset.builder(AdditionType.global(StatIds.MANA_GAINED_FROM_ENEMY_DEATH))
                .withTier("of_absorption", ModValue.of(2), 1, 750)
                .withTier("of_osmosis", ModValue.of(4, 5), 12, 750)
                .withTier("of_infusion", ModValue.of(6, 9), 23, 750)
                .withTier("of_enveloping", ModValue.of(10, 14), 34, 750)
                .withTier("of_consumption", ModValue.of(15, 20), 45, 750)
                .withTier("of_siphoning", ModValue.of(21, 27), 56, 750)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.LEGS_MOD_SUFFIX, "LifeGainPerTarget",
            ModPreset.builder(AdditionType.global(StatIds.LIFE_GAIN_PER_TARGET))
                .withTier("of_rejuvenation", ModValue.of(2), 8, 1000)
                .withTier("of_restoration", ModValue.of(3, 3), 20, 1000)
                .withTier("of_regrowth", ModValue.of(4, 4), 30, 1000)
                .withTier("of_nourishment", ModValue.of(5, 5), 40, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.LEGS_MOD_SUFFIX, "LifeLeech",
            ModPreset.builder(AdditionType.global(StatIds.LEECH_PHYSICAL_DAMAGE_AS_LIFE))
                .withTier("of_the_locust", ModValue.of(6, 6.9), 21, 1000)
                .withTier("of_the_remora", ModValue.of(7, 7.9), 38, 1000)
                .withTier("of_the_lamprey", ModValue.of(8, 8.9), 54, 1000)
                .withTier("of_the_vampire", ModValue.of(9, 9.9), 65, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_SUFFIX, "LifeLeech",
            ModPreset.builder(AdditionType.global(StatIds.LEECH_PHYSICAL_DAMAGE_AS_LIFE))
                .withTier("of_the_locust", ModValue.of(6, 6.9), 21, 1000)
                .withTier("of_the_remora", ModValue.of(7, 7.9), 38, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.LEGS_MOD_SUFFIX, "ManaLeech",
            ModPreset.builder(AdditionType.global(StatIds.LEECH_PHYSICAL_DAMAGE_AS_MANA))
                .withTier("of_the_parched", ModValue.of(5, 5.9), 21, 1000)
                .withTier("of_the_arid", ModValue.of(6, 6.9), 38, 1000)
                .withTier("of_the_drought", ModValue.of(7, 7.9), 54, 1000)
                .withTier("of_the_desperate", ModValue.of(8, 8.9), 65, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_SUFFIX, "ManaLeech",
            ModPreset.builder(AdditionType.global(StatIds.LEECH_PHYSICAL_DAMAGE_AS_MANA))
                .withTier("of_the_parched", ModValue.of(5, 5.9), 21, 1000)
                .withTier("of_the_arid", ModValue.of(6, 6.9), 38, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.CROSSBOW_MOD_SUFFIX, "AdditionalAmmo",
            ModPreset.builder(ModAdditions.LAUNCH_ADDITIONAL_PROJECTILE_BOLT)
                .withTier("of_shelling", ModValue.of(1), 55, 250)
                .withTier("of_bursting", ModValue.of(2, 2), 82, 125)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_PREFIX, "WeaponDamageTypePrefix",
            ModPreset.builder(ModAdditions.CHAOS_DAMAGE_INC)
                .withTier("impure", ModValue.of(25), 2, 500)
                .withTier("bitter", ModValue.of(25, 34), 2, 500)
                .withTier("searing", ModValue.of(25, 34), 2, 500)
                .withTier("charged", ModValue.of(25, 34), 2, 500)
                .withTier("punishing", ModValue.of(25, 34), 2, 500)
                .withTier("tainted", ModValue.of(35, 44), 8, 500)
                .withTier("biting", ModValue.of(35, 44), 8, 500)
                .withTier("sizzling", ModValue.of(35, 44), 8, 500)
                .withTier("hissing", ModValue.of(35, 44), 8, 500)
                .withTier("unforgiving", ModValue.of(35, 44), 8, 500)
                .withTier("clouded", ModValue.of(45, 54), 16, 500)
                .withTier("alpine", ModValue.of(45, 54), 16, 500)
                .withTier("blistering", ModValue.of(45, 54), 16, 500)
                .withTier("bolting", ModValue.of(45, 54), 16, 500)
                .withTier("vengeful", ModValue.of(45, 54), 16, 500)
                .withTier("darkened", ModValue.of(55, 64), 33, 400)
                .withTier("snowy", ModValue.of(55, 64), 33, 400)
                .withTier("cauterising", ModValue.of(55, 64), 33, 400)
                .withTier("coursing", ModValue.of(55, 64), 33, 400)
                .withTier("sadistic", ModValue.of(55, 64), 33, 400)
                .withTier("malignant", ModValue.of(65, 74), 46, 300)
                .withTier("hailing", ModValue.of(65, 74), 46, 300)
                .withTier("smoldering", ModValue.of(65, 74), 46, 300)
                .withTier("striking", ModValue.of(65, 74), 46, 300)
                .withTier("pitiless", ModValue.of(65, 74), 46, 300)
                .withTier("vile", ModValue.of(75, 89), 60, 200)
                .withTier("arctic", ModValue.of(75, 89), 60, 200)
                .withTier("magmatic", ModValue.of(75, 89), 60, 200)
                .withTier("smiting", ModValue.of(75, 89), 60, 200)
                .withTier("agonising", ModValue.of(75, 89), 60, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.STAFF_MOD_PREFIX, "WeaponDamageTypePrefix",
            ModPreset.builder(ModAdditions.CHAOS_DAMAGE_INC)
                .withTier("impure", ModValue.of(50), 2, 500)
                .withTier("bitter", ModValue.of(50, 68), 2, 500)
                .withTier("searing", ModValue.of(50, 68), 2, 500)
                .withTier("charged", ModValue.of(50, 68), 2, 500)
                .withTier("punishing", ModValue.of(50, 68), 2, 500)
                .withTier("tainted", ModValue.of(69, 88), 8, 500)
                .withTier("biting", ModValue.of(69, 88), 8, 500)
                .withTier("sizzling", ModValue.of(69, 88), 8, 500)
                .withTier("hissing", ModValue.of(69, 88), 8, 500)
                .withTier("unforgiving", ModValue.of(69, 88), 8, 500)
                .withTier("clouded", ModValue.of(89, 108), 16, 500)
                .withTier("alpine", ModValue.of(89, 108), 16, 500)
                .withTier("blistering", ModValue.of(89, 108), 16, 500)
                .withTier("bolting", ModValue.of(89, 108), 16, 500)
                .withTier("vengeful", ModValue.of(89, 108), 16, 500)
                .withTier("darkened", ModValue.of(109, 128), 33, 400)
                .withTier("snowy", ModValue.of(109, 128), 33, 400)
                .withTier("cauterising", ModValue.of(109, 128), 33, 400)
                .withTier("coursing", ModValue.of(109, 128), 33, 400)
                .withTier("sadistic", ModValue.of(109, 128), 33, 400)
                .withTier("malignant", ModValue.of(129, 148), 46, 300)
                .withTier("hailing", ModValue.of(129, 148), 46, 300)
                .withTier("smoldering", ModValue.of(129, 148), 46, 300)
                .withTier("striking", ModValue.of(129, 148), 46, 300)
                .withTier("pitiless", ModValue.of(129, 148), 46, 300)
                .withTier("vile", ModValue.of(149, 188), 60, 200)
                .withTier("arctic", ModValue.of(149, 188), 60, 200)
                .withTier("magmatic", ModValue.of(149, 188), 60, 200)
                .withTier("smiting", ModValue.of(149, 188), 60, 200)
                .withTier("agonising", ModValue.of(149, 188), 60, 200)
                .withTier("twisted", ModValue.of(189, 208), 70, 100)
                .withTier("crystalline", ModValue.of(189, 208), 70, 100)
                .withTier("volcanic", ModValue.of(189, 208), 70, 100)
                .withTier("ionising", ModValue.of(189, 208), 70, 100)
                .withTier("oppressor_s", ModValue.of(189, 208), 70, 100)
                .withTier("malevolent", ModValue.of(209, 238), 81, 50)
                .withTier("cryomancer_s", ModValue.of(209, 238), 81, 50)
                .withTier("pyromancer_s", ModValue.of(209, 238), 81, 50)
                .withTier("electromancer_s", ModValue.of(209, 238), 81, 50)
                .withTier("torturer_s", ModValue.of(209, 238), 81, 50)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TRAP_MOD_PREFIX, "WeaponDamageTypePrefix",
            ModPreset.builder(ModAdditions.CHAOS_DAMAGE_INC)
                .withTier("impure", ModValue.of(25), 2, 1)
                .withTier("bitter", ModValue.of(25, 34), 2, 1)
                .withTier("searing", ModValue.of(25, 34), 2, 1)
                .withTier("charged", ModValue.of(25, 34), 2, 1)
                .withTier("punishing", ModValue.of(25, 34), 2, 1)
                .withTier("tainted", ModValue.of(35, 44), 8, 1)
                .withTier("biting", ModValue.of(35, 44), 8, 1)
                .withTier("sizzling", ModValue.of(35, 44), 8, 1)
                .withTier("hissing", ModValue.of(35, 44), 8, 1)
                .withTier("unforgiving", ModValue.of(35, 44), 8, 1)
                .withTier("clouded", ModValue.of(45, 54), 16, 1)
                .withTier("alpine", ModValue.of(45, 54), 16, 1)
                .withTier("blistering", ModValue.of(45, 54), 16, 1)
                .withTier("bolting", ModValue.of(45, 54), 16, 1)
                .withTier("vengeful", ModValue.of(45, 54), 16, 1)
                .withTier("darkened", ModValue.of(55, 64), 33, 1)
                .withTier("snowy", ModValue.of(55, 64), 33, 1)
                .withTier("cauterising", ModValue.of(55, 64), 33, 1)
                .withTier("coursing", ModValue.of(55, 64), 33, 1)
                .withTier("sadistic", ModValue.of(55, 64), 33, 1)
                .withTier("malignant", ModValue.of(65, 74), 46, 1)
                .withTier("hailing", ModValue.of(65, 74), 46, 1)
                .withTier("smoldering", ModValue.of(65, 74), 46, 1)
                .withTier("striking", ModValue.of(65, 74), 46, 1)
                .withTier("pitiless", ModValue.of(65, 74), 46, 1)
                .withTier("vile", ModValue.of(75, 89), 60, 1)
                .withTier("arctic", ModValue.of(75, 89), 60, 1)
                .withTier("magmatic", ModValue.of(75, 89), 60, 1)
                .withTier("smiting", ModValue.of(75, 89), 60, 1)
                .withTier("agonising", ModValue.of(75, 89), 60, 1)
                .withTier("twisted", ModValue.of(90, 104), 70, 1)
                .withTier("crystalline", ModValue.of(90, 104), 70, 1)
                .withTier("volcanic", ModValue.of(90, 104), 70, 1)
                .withTier("ionising", ModValue.of(90, 104), 70, 1)
                .withTier("oppressor_s", ModValue.of(90, 104), 70, 1)
                .withTier("malevolent", ModValue.of(105, 119), 81, 1)
                .withTier("cryomancer_s", ModValue.of(105, 119), 81, 1)
                .withTier("pyromancer_s", ModValue.of(105, 119), 81, 1)
                .withTier("electromancer_s", ModValue.of(105, 119), 81, 1)
                .withTier("torturer_s", ModValue.of(105, 119), 81, 1)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WAND_MOD_PREFIX, "WeaponDamageTypePrefix",
            ModPreset.builder(ModAdditions.CHAOS_DAMAGE_INC)
                .withTier("impure", ModValue.of(25), 2, 500)
                .withTier("bitter", ModValue.of(25, 34), 2, 500)
                .withTier("searing", ModValue.of(25, 34), 2, 500)
                .withTier("charged", ModValue.of(25, 34), 2, 500)
                .withTier("punishing", ModValue.of(25, 34), 2, 500)
                .withTier("tainted", ModValue.of(35, 44), 8, 500)
                .withTier("biting", ModValue.of(35, 44), 8, 500)
                .withTier("sizzling", ModValue.of(35, 44), 8, 500)
                .withTier("hissing", ModValue.of(35, 44), 8, 500)
                .withTier("unforgiving", ModValue.of(35, 44), 8, 500)
                .withTier("clouded", ModValue.of(45, 54), 16, 500)
                .withTier("alpine", ModValue.of(45, 54), 16, 500)
                .withTier("blistering", ModValue.of(45, 54), 16, 500)
                .withTier("bolting", ModValue.of(45, 54), 16, 500)
                .withTier("vengeful", ModValue.of(45, 54), 16, 500)
                .withTier("darkened", ModValue.of(55, 64), 33, 400)
                .withTier("snowy", ModValue.of(55, 64), 33, 400)
                .withTier("cauterising", ModValue.of(55, 64), 33, 400)
                .withTier("coursing", ModValue.of(55, 64), 33, 400)
                .withTier("sadistic", ModValue.of(55, 64), 33, 400)
                .withTier("malignant", ModValue.of(65, 74), 46, 300)
                .withTier("hailing", ModValue.of(65, 74), 46, 300)
                .withTier("smoldering", ModValue.of(65, 74), 46, 300)
                .withTier("striking", ModValue.of(65, 74), 46, 300)
                .withTier("pitiless", ModValue.of(65, 74), 46, 300)
                .withTier("vile", ModValue.of(75, 89), 60, 200)
                .withTier("arctic", ModValue.of(75, 89), 60, 200)
                .withTier("magmatic", ModValue.of(75, 89), 60, 200)
                .withTier("smiting", ModValue.of(75, 89), 60, 200)
                .withTier("agonising", ModValue.of(75, 89), 60, 200)
                .withTier("twisted", ModValue.of(90, 104), 70, 100)
                .withTier("crystalline", ModValue.of(90, 104), 70, 100)
                .withTier("volcanic", ModValue.of(90, 104), 70, 100)
                .withTier("ionising", ModValue.of(90, 104), 70, 100)
                .withTier("oppressor_s", ModValue.of(90, 104), 70, 100)
                .withTier("malevolent", ModValue.of(105, 119), 81, 50)
                .withTier("cryomancer_s", ModValue.of(105, 119), 81, 50)
                .withTier("pyromancer_s", ModValue.of(105, 119), 81, 50)
                .withTier("electromancer_s", ModValue.of(105, 119), 81, 50)
                .withTier("torturer_s", ModValue.of(105, 119), 81, 50)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_PREFIX, "BaseLocalDefencesAndMana",
            ModPreset.builder(MultiAddition.keyed(ModAdditions.ENERGY_INC_LOCAL, ModAdditions.ADD_MAX_MANA))
                .withTier("imbued", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(6, 8)), 8, 1000)
                .withTier("serene", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(9, 16)), 16, 1000)
                .withTier("sacred", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(17, 20)), 33, 1000)
                .withTier("celestial", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(21, 26)), 46, 1000)
                .withTier("heavenly", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(27, 32)), 60, 1000)
                .withTier("angel_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(33, 39)), 78, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.HEAD_MOD_PREFIX, "BaseLocalDefencesAndMana",
            ModPreset.builder(MultiAddition.keyed(ModAdditions.ENERGY_INC_LOCAL, ModAdditions.ADD_MAX_MANA))
                .withTier("coelacanth_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(6, 8)), 8, 1000)
                .withTier("rhoa_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(6, 8)), 8, 1000)
                .withTier("imposing", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(6, 8)), 8, 1000)
                .withTier("imbued", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(6, 8)), 8, 1000)
                .withTier("nomad_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(6, 8)), 8, 1000)
                .withTier("vulture_s", Map.of("value", ModValue.of(6, 13), "value_2", ModValue.of(6, 8)), 8, 1000)
                .withTier("swordfish_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(9, 16)), 16, 1000)
                .withTier("rhex_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(9, 16)), 16, 1000)
                .withTier("venerable", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(9, 16)), 16, 1000)
                .withTier("serene", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(9, 16)), 16, 1000)
                .withTier("drifter_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(9, 16)), 16, 1000)
                .withTier("kingfisher_s", Map.of("value", ModValue.of(14, 20), "value_2", ModValue.of(9, 16)), 16, 1000)
                .withTier("shark_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(17, 20)), 33, 1000)
                .withTier("chimeral_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(17, 20)), 33, 1000)
                .withTier("regal", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(17, 20)), 33, 1000)
                .withTier("sacred", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(17, 20)), 33, 1000)
                .withTier("traveller_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(17, 20)), 33, 1000)
                .withTier("owl_s", Map.of("value", ModValue.of(21, 26), "value_2", ModValue.of(17, 20)), 33, 1000)
                .withTier("dolphin_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(21, 26)), 46, 1000)
                .withTier("bull_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(21, 26)), 46, 1000)
                .withTier("colossal", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(21, 26)), 46, 1000)
                .withTier("celestial", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(21, 26)), 46, 1000)
                .withTier("explorer_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(21, 26)), 46, 1000)
                .withTier("hawk_s", Map.of("value", ModValue.of(27, 32), "value_2", ModValue.of(21, 26)), 46, 1000)
                .withTier("orca_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(27, 32)), 60, 1000)
                .withTier("minotaur_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(27, 32)), 60, 1000)
                .withTier("chieftain_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(27, 32)), 60, 1000)
                .withTier("heavenly", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(27, 32)), 60, 1000)
                .withTier("wayfarer_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(27, 32)), 60, 1000)
                .withTier("eagle_s", Map.of("value", ModValue.of(33, 38), "value_2", ModValue.of(27, 32)), 60, 1000)
                .withTier("whale_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(33, 39)), 78, 1000)
                .withTier("cerberus", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(33, 39)), 78, 1000)
                .withTier("ancestral", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(33, 39)), 78, 1000)
                .withTier("angel_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(33, 39)), 78, 1000)
                .withTier("wanderer_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(33, 39)), 78, 1000)
                .withTier("falcon_s", Map.of("value", ModValue.of(39, 42), "value_2", ModValue.of(33, 39)), 78, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_PREFIX, "WeaponCasterDamagePrefix",
            ModPreset.builder(AdditionType.global(StatIds.INC_SPELL_DAMAGE))
                .withTier("apprentice_s", ModValue.of(25), 1, 1000)
                .withTier("adept_s", ModValue.of(35, 44), 8, 1000)
                .withTier("scholar_s", ModValue.of(45, 54), 16, 1000)
                .withTier("professor_s", ModValue.of(55, 64), 33, 600)
                .withTier("occultist_s", ModValue.of(65, 74), 46, 400)
                .withTier("incanter_s", ModValue.of(75, 89), 60, 200)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.STAFF_MOD_PREFIX, "WeaponCasterDamagePrefix",
            ModPreset.builder(AdditionType.global(StatIds.INC_SPELL_DAMAGE))
                .withTier("apprentice_s", ModValue.of(50), 1, 1000)
                .withTier("adept_s", ModValue.of(69, 88), 8, 1000)
                .withTier("scholar_s", ModValue.of(89, 108), 16, 1000)
                .withTier("professor_s", ModValue.of(109, 128), 33, 600)
                .withTier("occultist_s", ModValue.of(129, 148), 46, 400)
                .withTier("incanter_s", ModValue.of(149, 188), 60, 200)
                .withTier("glyphic", ModValue.of(189, 208), 70, 100)
                .withTier("runic", ModValue.of(209, 238), 80, 50)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TRAP_MOD_PREFIX, "WeaponCasterDamagePrefix",
            ModPreset.builder(AdditionType.global(StatIds.INC_SPELL_DAMAGE))
                .withTier("explosive", ModValue.of(25), 2, 1)
                .withTier("eviscerating", ModValue.of(35, 44), 8, 1)
                .withTier("crippling", ModValue.of(45, 54), 16, 1)
                .withTier("disabling", ModValue.of(55, 64), 33, 1)
                .withTier("decimating", ModValue.of(65, 74), 46, 1)
                .withTier("demolishing", ModValue.of(75, 89), 60, 1)
                .withTier("obliterating", ModValue.of(90, 104), 70, 1)
                .withTier("shattering", ModValue.of(105, 119), 81, 1)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WAND_MOD_PREFIX, "WeaponCasterDamagePrefix",
            ModPreset.builder(AdditionType.global(StatIds.INC_SPELL_DAMAGE))
                .withTier("apprentice_s", ModValue.of(25), 1, 1000)
                .withTier("adept_s", ModValue.of(35, 44), 8, 1000)
                .withTier("scholar_s", ModValue.of(45, 54), 16, 1000)
                .withTier("professor_s", ModValue.of(55, 64), 33, 600)
                .withTier("occultist_s", ModValue.of(65, 74), 46, 400)
                .withTier("incanter_s", ModValue.of(75, 89), 60, 200)
                .withTier("glyphic", ModValue.of(90, 104), 70, 100)
                .withTier("runic", ModValue.of(105, 119), 80, 50)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_SUFFIX, "SpellCriticalStrikeChanceIncrease",
            ModPreset.builder(AdditionType.global(StatIds.INC_SPELL_CRIT_CHANCE))
                .withTier("of_menace", ModValue.of(27), 11, 1000)
                .withTier("of_havoc", ModValue.of(34, 39), 21, 1000)
                .withTier("of_disaster", ModValue.of(40, 46), 28, 1000)
                .withTier("of_calamity", ModValue.of(47, 53), 41, 500)
                .withTier("of_ruin", ModValue.of(54, 59), 59, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.STAFF_MOD_SUFFIX, "SpellCriticalStrikeChanceIncrease",
            ModPreset.builder(AdditionType.global(StatIds.INC_SPELL_CRIT_CHANCE))
                .withTier("of_menace", ModValue.of(40), 11, 1000)
                .withTier("of_havoc", ModValue.of(50, 59), 21, 1000)
                .withTier("of_disaster", ModValue.of(60, 69), 28, 1000)
                .withTier("of_calamity", ModValue.of(70, 79), 41, 500)
                .withTier("of_ruin", ModValue.of(80, 89), 59, 250)
                .withTier("of_unmaking", ModValue.of(90, 109), 76, 125)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WAND_MOD_SUFFIX, "SpellCriticalStrikeChanceIncrease",
            ModPreset.builder(AdditionType.global(StatIds.INC_SPELL_CRIT_CHANCE))
                .withTier("of_menace", ModValue.of(27), 11, 1000)
                .withTier("of_havoc", ModValue.of(34, 39), 21, 1000)
                .withTier("of_disaster", ModValue.of(40, 46), 28, 1000)
                .withTier("of_calamity", ModValue.of(47, 53), 41, 500)
                .withTier("of_ruin", ModValue.of(54, 59), 59, 250)
                .withTier("of_unmaking", ModValue.of(60, 73), 76, 125)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_PREFIX, "DamageWithWeaponTypeSkill",
            ModPreset.builder(AdditionType.global(StatIds.INC_DAMAGE_WITH_BOW_SKILLS))
                .withTier("acute", ModValue.of(11), 1, 500)
                .withTier("trenchant", ModValue.of(21, 30), 16, 500)
                .withTier("perforating", ModValue.of(31, 36), 33, 500)
                .withTier("incisive", ModValue.of(37, 42), 46, 500)
                .withTier("lacerating", ModValue.of(43, 50), 60, 500)
                .withTier("impaling", ModValue.of(51, 59), 81, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_PREFIX, "ProjectileSpeed",
            ModPreset.builder(ModAdditions.PROJECTILE_SPEED_INC)
                .withTier("darting", ModValue.of(10), 14, 1000)
                .withTier("brisk", ModValue.of(18, 25), 27, 1000)
                .withTier("quick", ModValue.of(26, 33), 41, 1000)
                .withTier("rapid", ModValue.of(34, 41), 55, 1000)
                .withTier("nimble", ModValue.of(42, 46), 82, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.QUIVER_MOD_SUFFIX, "ChanceToPierce",
            ModPreset.builder(ModAdditions.PROJECTILE_PIERCE)
                .withTier("of_piercing", ModValue.of(12), 11, 500)
                .withTier("of_drilling", ModValue.of(15, 17), 26, 500)
                .withTier("of_puncturing", ModValue.of(18, 20), 44, 500)
                .withTier("of_skewering", ModValue.of(21, 23), 61, 500)
                .withTier("of_penetrating", ModValue.of(24, 26), 77, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_PREFIX, "IncreasedChaosDamage",
            ModPreset.builder(ModAdditions.CHAOS_DAMAGE_INC)
                .withTier("impure", ModValue.of(3), 8, 500)
                .withTier("tainted", ModValue.of(8, 12), 16, 500)
                .withTier("clouded", ModValue.of(13, 17), 33, 500)
                .withTier("darkened", ModValue.of(18, 22), 46, 500)
                .withTier("malignant", ModValue.of(23, 26), 65, 500)
                .withTier("vile", ModValue.of(27, 30), 75, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_PREFIX, "ColdDamagePercentage",
            ModPreset.builder(ModAdditions.COLD_DAMAGE_INC)
                .withTier("bitter", ModValue.of(3), 8, 500)
                .withTier("biting", ModValue.of(8, 12), 16, 500)
                .withTier("alpine", ModValue.of(13, 17), 33, 500)
                .withTier("snowy", ModValue.of(18, 22), 46, 500)
                .withTier("hailing", ModValue.of(23, 26), 65, 500)
                .withTier("crystalline", ModValue.of(27, 30), 75, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_PREFIX, "FireDamagePercentage",
            ModPreset.builder(ModAdditions.FIRE_DAMAGE_INC)
                .withTier("searing", ModValue.of(3), 8, 500)
                .withTier("sizzling", ModValue.of(8, 12), 16, 500)
                .withTier("blistering", ModValue.of(13, 17), 33, 500)
                .withTier("cauterising", ModValue.of(18, 22), 46, 500)
                .withTier("volcanic", ModValue.of(23, 26), 65, 500)
                .withTier("magmatic", ModValue.of(27, 30), 75, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_PREFIX, "LightningDamagePercentage",
            ModPreset.builder(ModAdditions.LIGHTNING_DAMAGE_INC)
                .withTier("charged", ModValue.of(3), 8, 500)
                .withTier("hissing", ModValue.of(8, 12), 16, 500)
                .withTier("bolting", ModValue.of(13, 17), 33, 500)
                .withTier("coursing", ModValue.of(18, 22), 46, 500)
                .withTier("striking", ModValue.of(23, 26), 65, 500)
                .withTier("smiting", ModValue.of(27, 30), 75, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_PREFIX, "IncreasedEvasionRating",
            ModPreset.builder(ModAdditions.ADD_EVASION_LOCAL)
                .withTier("agile", ModValue.of(8), 1, 1000)
                .withTier("dancer_s", ModValue.of(18, 38), 11, 1000)
                .withTier("acrobat_s", ModValue.of(39, 51), 16, 1000)
                .withTier("fleet", ModValue.of(52, 79), 25, 1000)
                .withTier("blurred", ModValue.of(80, 107), 33, 1000)
                .withTier("phased", ModValue.of(108, 141), 46, 1000)
                .withTier("vaporous", ModValue.of(142, 174), 54, 1000)
                .withTier("elusory", ModValue.of(175, 202), 65, 1000)
                .withTier("adroit", ModValue.of(203, 233), 70, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.RING_MOD_SUFFIX, "LightRadiusAndManaRegeneration",
            ModPreset.builder(MultiAddition.keyed(AdditionType.global(StatIds.LIGHT_RADIUS), ModAdditions.MANA_REGENERATION_RATE_INC))
                .withTier("of_warmth", Map.of("value", ModValue.of(8), "value_2", ModValue.of(5, 5)), 8, 1000)
                .withTier("of_kindling", Map.of("value", ModValue.of(13, 17), "value_2", ModValue.of(10, 10)), 15, 1000)
                .withTier("of_the_hearth", Map.of("value", ModValue.of(18, 22), "value_2", ModValue.of(15, 15)), 30, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_PREFIX, "LocalIncreasedSpiritPercent",
            ModPreset.builder(LocalAddition.of(StatIds.INC_MAX_SPIRIT))
                .withTier("lord_s", ModValue.of(20), 1, 1000)
                .withTier("baron_s", ModValue.of(27, 32), 8, 1000)
                .withTier("viscount_s", ModValue.of(33, 38), 16, 1000)
                .withTier("marquess", ModValue.of(39, 44), 33, 600)
                .withTier("count_s", ModValue.of(45, 50), 46, 400)
                .withTier("duke_s", ModValue.of(51, 55), 60, 200)
                .withTier("prince_s", ModValue.of(56, 60), 75, 100)
                .withTier("king_s", ModValue.of(61, 65), 82, 50)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_PREFIX, "LocalIncreasedSpiritAndMana",
            ModPreset.builder(MultiAddition.keyed(ModAdditions.ADD_MAX_SPIRIT, ModAdditions.ADD_MAX_MANA))
                .withTier("advisor_s", Map.of("value", ModValue.of(10, 14), "value_2", ModValue.of(17, 20)), 2, 1000)
                .withTier("counselor_s", Map.of("value", ModValue.of(15, 18), "value_2", ModValue.of(21, 24)), 11, 1000)
                .withTier("emissary_s", Map.of("value", ModValue.of(19, 22), "value_2", ModValue.of(25, 28)), 26, 1000)
                .withTier("minister_s", Map.of("value", ModValue.of(23, 26), "value_2", ModValue.of(29, 33)), 36, 600)
                .withTier("envoy_s", Map.of("value", ModValue.of(27, 30), "value_2", ModValue.of(34, 37)), 48, 400)
                .withTier("diplomat_s", Map.of("value", ModValue.of(31, 34), "value_2", ModValue.of(38, 41)), 58, 200)
                .withTier("chancellor_s", Map.of("value", ModValue.of(35, 38), "value_2", ModValue.of(42, 45)), 70, 100)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_PREFIX, "AllDamage",
            ModPreset.builder(ModAdditions.PRESENCE_DAMAGE)
                .withTier("coercive", ModValue.of(25), 1, 1000)
                .withTier("agitative", ModValue.of(35, 44), 8, 1000)
                .withTier("instigative", ModValue.of(45, 54), 16, 1000)
                .withTier("provocative", ModValue.of(55, 64), 33, 600)
                .withTier("persuasive", ModValue.of(65, 74), 46, 400)
                .withTier("motivating", ModValue.of(75, 89), 60, 200)
                .withTier("inspirational", ModValue.of(90, 104), 70, 100)
                .withTier("empowering", ModValue.of(105, 119), 82, 50)
                .build());

    }

    private static void s5(BootstrapContext<ItemInstance> bootstrap) {
        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_SUFFIX, "PresenceRadius",
            ModPreset.builder(AdditionType.global(StatIds.PRESENCE_RADIUS))
                .withTier("of_direction", ModValue.of(36), 23, 500)
                .withTier("of_outreach", ModValue.of(46, 55), 40, 500)
                .withTier("of_guidance", ModValue.of(56, 65), 56, 500)
                .withTier("of_influence", ModValue.of(66, 80), 72, 500)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SCEPTRE_MOD_SUFFIX, "MinionLife",
            ModPreset.builder(AdditionType.global(StatIds.INC_MINION_MAX_LIFE))
                .withTier("of_the_mentor", ModValue.of(21), 2, 1000)
                .withTier("of_the_tutor", ModValue.of(26, 30), 16, 1000)
                .withTier("of_the_director", ModValue.of(31, 35), 32, 1000)
                .withTier("of_the_headmaster", ModValue.of(36, 40), 48, 1000)
                .withTier("of_the_administrator", ModValue.of(41, 45), 64, 1000)
                .withTier("of_the_rector", ModValue.of(46, 50), 80, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "ReducedPhysicalDamageTaken",
            ModPreset.builder(AdditionType.global(StatIds.ADD_PHYSICAL_DAMAGE_REDUCTION))
                .withTier("of_the_watchman", ModValue.of(4), 32, 800)
                .withTier("of_the_custodian", ModValue.of(5, 5), 41, 800)
                .withTier("of_the_sentry", ModValue.of(6, 6), 53, 800)
                .withTier("of_the_protector", ModValue.of(7, 7), 66, 800)
                .withTier("of_the_conservator", ModValue.of(8, 8), 77, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "ReducedExtraDamageFromCrits",
            ModPreset.builder(AdditionType.global(StatIds.REDUCED_EXTRA_DAMAGE_FROM_CRITS))
                .withTier("of_dulling", ModValue.of(21), 33, 800)
                .withTier("of_deadening", ModValue.of(28, 34), 45, 800)
                .withTier("of_interference", ModValue.of(35, 41), 58, 800)
                .withTier("of_obstruction", ModValue.of(42, 47), 69, 800)
                .withTier("of_the_bastion", ModValue.of(48, 54), 81, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.STAFF_MOD_PREFIX, "SpellDamageAndMana",
            ModPreset.builder(MultiAddition.keyed(ModAdditions.ADD_MAX_MANA))
                .withTier("caster_s", Map.of("value", ModValue.of(30), "value_2", ModValue.of(34, 40)), 2, 1000)
                .withTier("conjuror_s", Map.of("value", ModValue.of(39, 48), "value_2", ModValue.of(41, 48)), 11, 1000)
                .withTier("wizard_s", Map.of("value", ModValue.of(49, 58), "value_2", ModValue.of(49, 56)), 23, 1000)
                .withTier("warlock_s", Map.of("value", ModValue.of(59, 68), "value_2", ModValue.of(57, 66)), 38, 600)
                .withTier("mage_s", Map.of("value", ModValue.of(69, 78), "value_2", ModValue.of(67, 74)), 48, 400)
                .withTier("archmage_s", Map.of("value", ModValue.of(79, 88), "value_2", ModValue.of(75, 82)), 63, 200)
                .withTier("lich_s", Map.of("value", ModValue.of(89, 98), "value_2", ModValue.of(83, 90)), 79, 100)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.WAND_MOD_PREFIX, "SpellDamageAndMana",
            ModPreset.builder(MultiAddition.keyed(ModAdditions.ADD_MAX_MANA))
                .withTier("caster_s", Map.of("value", ModValue.of(15), "value_2", ModValue.of(17, 20)), 2, 1000)
                .withTier("conjuror_s", Map.of("value", ModValue.of(20, 24), "value_2", ModValue.of(21, 24)), 11, 1000)
                .withTier("wizard_s", Map.of("value", ModValue.of(25, 29), "value_2", ModValue.of(25, 28)), 23, 1000)
                .withTier("warlock_s", Map.of("value", ModValue.of(30, 34), "value_2", ModValue.of(29, 33)), 38, 600)
                .withTier("mage_s", Map.of("value", ModValue.of(35, 39), "value_2", ModValue.of(34, 37)), 46, 400)
                .withTier("archmage_s", Map.of("value", ModValue.of(40, 44), "value_2", ModValue.of(38, 41)), 60, 200)
                .withTier("lich_s", Map.of("value", ModValue.of(45, 49), "value_2", ModValue.of(42, 45)), 80, 100)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TRAP_MOD_SUFFIX, "IgniteChanceIncrease",
            ModPreset.builder(AdditionType.global(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_MAGNITUDE_CURSE_FIRE_TAKEN))
                .withTier("of_ignition", ModValue.of(51), 15, 1)
                .withTier("of_scorching", ModValue.of(61, 70), 30, 1)
                .withTier("of_incineration", ModValue.of(71, 80), 45, 1)
                .withTier("of_combustion", ModValue.of(81, 90), 60, 1)
                .withTier("of_conflagration", ModValue.of(91, 100), 75, 1)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TRAP_MOD_SUFFIX, "FreezeDamageIncrease",
            ModPreset.builder(ModAdditions.AILMENT_MAGNITUDE_FREEZE)
                .withTier("of_freezing", ModValue.of(31), 15, 1)
                .withTier("of_bleakness", ModValue.of(41, 50), 30, 1)
                .withTier("of_the_glacier", ModValue.of(51, 60), 45, 1)
                .withTier("of_the_hyperboreal", ModValue.of(61, 70), 60, 1)
                .withTier("of_the_arctic", ModValue.of(71, 80), 75, 1)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TRAP_MOD_SUFFIX, "ShockChanceIncrease",
            ModPreset.builder(AdditionType.global(StatIds.INC_CHANCE_TO_SHOCK))
                .withTier("of_shocking", ModValue.of(51), 15, 1)
                .withTier("of_zapping", ModValue.of(61, 70), 30, 1)
                .withTier("of_electrocution", ModValue.of(71, 80), 45, 1)
                .withTier("of_voltages", ModValue.of(81, 90), 60, 1)
                .withTier("of_the_thunderbolt", ModValue.of(91, 100), 75, 1)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TRAP_MOD_PREFIX, "TrapDamageAndMana",
            ModPreset.builder(MultiAddition.keyed(ModAdditions.ADD_MAX_MANA))
                .withTier("sapper_s", Map.of("value", ModValue.of(15), "value_2", ModValue.of(17, 20)), 2, 1)
                .withTier("saboteur_s", Map.of("value", ModValue.of(20, 24), "value_2", ModValue.of(21, 24)), 11, 1)
                .withTier("tinkerer_s", Map.of("value", ModValue.of(25, 29), "value_2", ModValue.of(25, 28)), 23, 1)
                .withTier("mechanic_s", Map.of("value", ModValue.of(30, 34), "value_2", ModValue.of(29, 33)), 35, 1)
                .withTier("engineer_s", Map.of("value", ModValue.of(35, 39), "value_2", ModValue.of(34, 37)), 48, 1)
                .withTier("inventor_s", Map.of("value", ModValue.of(40, 44), "value_2", ModValue.of(38, 41)), 63, 1)
                .withTier("artificer_s", Map.of("value", ModValue.of(45, 49), "value_2", ModValue.of(42, 45)), 78, 1)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.TRAP_MOD_SUFFIX, "TrapAndMineThrowSpeed",
            ModPreset.builder(AdditionType.global(StatIds.TRAP_AND_MINE_THROW_SPEED))
                .withTier("tbd", ModValue.of(1), 1, 1)
                .build());

    }

    /**
     * 副手（盾 / 小盾 / 法器）槽位层补齐（D1 语料缺口批次 1）。
     *
     * <p>背景：类型树把 {@code shield / buckler / foci} 挂在 {@code weapon/off_hand} 之下
     * （{@code CoeDataBootstrap.registerItemType}），故它们的 {@code types()} 里<b>没有</b> {@code armor}；
     * 而这一类共享词缀原先只注册在 {@code ARMOR_GROUP_MOD_*}（池 namespace {@code armor}），
     * {@code ModifierPipeline.matchesScope} 永不命中 —— 与之同构的 foci 死池见 {@link EquipmentGen#FOCI_PATH}。
     * 这里按「槽位层各注册一份」的既有写法（同 {@code SHIELD_MOD_SUFFIX} 的 Dexterity / Intelligence）落地，
     * 数值 / 等级 / 权重直接取自 {@code design/refactor/affix-data/crawled/*.json}（weight = DropChance）。
     *
     * <p><b>⚠️ 后续维护约束（#242 主理人裁决）：</b>本方法的存在前提是「off_hand 挂在 weapon 分支」。
     * 若将来把类型树的 {@code off_hand} 改归 {@code armor} 分支（{@code shield / buckler / foci} 的
     * {@code types()} 因此含 {@code armor}），则 {@code ARMOR_GROUP_MOD_*} 会重新对副手生效，
     * 本方法中的条目将与组层条目<b>重复入池、权重翻倍</b>。
     * <b>届时须同步删除本方法中对应的重复条目</b>（逐条比对 {@code ARMOR_GROUP_MOD_*} 的家族名 + 收窄类型即可），
     * 不要只删一半导致抗性 / 属性 / 最大抗性集合残缺。
     */
    private static void s6(BootstrapContext<ItemInstance> bootstrap) {
        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_PREFIX, "IncreasedLifeShield",
            ModPreset.builder(ModAdditions.ADD_MAX_HEALTH)
                .withTier("hale", ModValue.of(10), 1, 1000)
                .withTier("healthy", ModValue.of(20, 29), 6, 1000)
                .withTier("sanguine", ModValue.of(30, 39), 16, 1000)
                .withTier("stalwart", ModValue.of(40, 59), 24, 1000)
                .withTier("stout", ModValue.of(60, 69), 33, 1000)
                .withTier("robust", ModValue.of(70, 84), 38, 1000)
                .withTier("rotund", ModValue.of(85, 99), 46, 1000)
                .withTier("virile", ModValue.of(100, 119), 54, 1000)
                .withTier("athlete_s", ModValue.of(120, 149), 60, 1000)
                .withTier("fecund", ModValue.of(150, 174), 65, 1000)
                .withTier("vigorous", ModValue.of(175, 189), 70, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_PREFIX, "IncreasedShieldBlockPercentageShield",
            ModPreset.builder(ModAdditions.BLOCK_CHANCE_INC_LOCAL)
                .withTier("steadfast", ModValue.of(15), 1, 1000)
                .withTier("unrelenting", ModValue.of(20, 24), 33, 1000)
                .withTier("adamant", ModValue.of(25, 30), 65, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_PREFIX, "ThornsShield",
            ModPreset.builder(ModAdditions.THORNS)
                .withTier("thorny", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 4)), 1, 1000)
                .withTier("spiny", Map.of("min", ModValue.of(5, 7), "max", ModValue.of(7, 10)), 10, 1000)
                .withTier("barbed", Map.of("min", ModValue.of(11, 16), "max", ModValue.of(17, 23)), 19, 1000)
                .withTier("pointed", Map.of("min", ModValue.of(24, 35), "max", ModValue.of(36, 53)), 38, 1000)
                .withTier("spiked", Map.of("min", ModValue.of(40, 60), "max", ModValue.of(61, 92)), 48, 1000)
                .withTier("edged", Map.of("min", ModValue.of(64, 97), "max", ModValue.of(98, 145)), 63, 1000)
                .withTier("jagged", Map.of("min", ModValue.of(101, 151), "max", ModValue.of(152, 220)), 74, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "ArmourAppliesToElementalDamage",
            ModPreset.builder(AdditionType.global(StatIds.ARMOUR_APPLIES_ELEMENTAL_DAMAGE))
                .withTier("of_covering", ModValue.of(14), 1, 1000)
                .withTier("of_sheathing", ModValue.of(20, 25), 16, 1000)
                .withTier("of_lining", ModValue.of(26, 31), 36, 1000)
                .withTier("of_padding", ModValue.of(32, 37), 48, 1000)
                .withTier("of_furring", ModValue.of(38, 43), 66, 1000)
                .withTier("of_thermokryptance", ModValue.of(44, 50), 81, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "ChaosResistanceShield",
            ModPreset.builder(ModAdditions.ADD_CHAOS_RESISTANCE)
                .withTier("of_the_lost", ModValue.of(4), 16, 250)
                .withTier("of_banishment", ModValue.of(8, 11), 30, 250)
                .withTier("of_eviction", ModValue.of(12, 15), 44, 250)
                .withTier("of_expulsion", ModValue.of(16, 19), 56, 250)
                .withTier("of_exile", ModValue.of(20, 23), 68, 250)
                .withTier("of_bameth", ModValue.of(24, 27), 81, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "ColdResistanceShield",
            ModPreset.builder(ModAdditions.ADD_COLD_RESISTANCE)
                .withTier("of_the_seal", ModValue.of(6), 1, 1000)
                .withTier("of_the_penguin", ModValue.of(11, 15), 14, 1000)
                .withTier("of_the_narwhal", ModValue.of(16, 20), 26, 1000)
                .withTier("of_the_yeti", ModValue.of(21, 25), 38, 1000)
                .withTier("of_the_walrus", ModValue.of(26, 30), 50, 1000)
                .withTier("of_the_polar_bear", ModValue.of(31, 35), 60, 1000)
                .withTier("of_the_ice", ModValue.of(36, 40), 71, 1000)
                .withTier("of_haast", ModValue.of(41, 45), 82, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "FireResistanceShield",
            ModPreset.builder(ModAdditions.ADD_FIRE_RESISTANCE)
                .withTier("of_the_whelpling", ModValue.of(6), 1, 1000)
                .withTier("of_the_salamander", ModValue.of(11, 15), 12, 1000)
                .withTier("of_the_drake", ModValue.of(16, 20), 24, 1000)
                .withTier("of_the_kiln", ModValue.of(21, 25), 36, 1000)
                .withTier("of_the_furnace", ModValue.of(26, 30), 48, 1000)
                .withTier("of_the_volcano", ModValue.of(31, 35), 60, 1000)
                .withTier("of_magma", ModValue.of(36, 40), 71, 1000)
                .withTier("of_tzteosh", ModValue.of(41, 45), 82, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "LightningResistanceShield",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_RESISTANCE)
                .withTier("of_the_cloud", ModValue.of(6), 1, 1000)
                .withTier("of_the_squall", ModValue.of(11, 15), 13, 1000)
                .withTier("of_the_storm", ModValue.of(16, 20), 25, 1000)
                .withTier("of_the_thunderhead", ModValue.of(21, 25), 37, 1000)
                .withTier("of_the_tempest", ModValue.of(26, 30), 49, 1000)
                .withTier("of_the_maelstrom", ModValue.of(31, 35), 60, 1000)
                .withTier("of_the_lightning", ModValue.of(36, 40), 71, 1000)
                .withTier("of_ephij", ModValue.of(41, 45), 82, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "LocalAttributeRequirementsShield",
            ModPreset.builder(LocalAddition.of(StatIds.REDUCED_ATTRIBUTE_REQUIREMENTS))
                .withTier("of_the_worthy", ModValue.of(15), 24, 800)
                .withTier("of_the_apt", ModValue.of(20, 20), 32, 800)
                .withTier("of_the_talented", ModValue.of(25, 25), 40, 800)
                .withTier("of_the_skilled", ModValue.of(30, 30), 52, 800)
                .withTier("of_the_proficient", ModValue.of(35, 35), 60, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "MaximumColdResistShield",
            ModPreset.builder(ModAdditions.ADD_MAX_COLD_RESISTANCE)
                .withTier("of_furs", ModValue.of(1), 68, 250)
                .withTier("of_the_tundra", ModValue.of(2, 2), 75, 250)
                .withTier("of_the_mammoth", ModValue.of(3, 3), 81, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "MaximumLightningResistanceShield",
            ModPreset.builder(ModAdditions.ADD_MAX_LIGHTNING_RESISTANCE)
                .withTier("of_impedance", ModValue.of(1), 68, 250)
                .withTier("of_shockproofing", ModValue.of(2, 2), 75, 250)
                .withTier("of_the_lightning_rod", ModValue.of(3, 3), 81, 250)
                .build());

        // ⚠️ 以下 3 条在 d1-templates.final.json 中 stat_id=null（语料文本「+1% to Maximum … Resistance」
        // 无数值区间，映射表未解析）；但 ARMOR_GROUP_MOD_SUFFIX 中已存在<b>逐阶完全一致</b>的注册
        // （同 AdditionType / 同 id / 同阶名 / 同 Level / 同 weight），故此处是「把既有注册搬到可命中的池」，
        // 未新增任何 StatId、未编造任何数值。
        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "MaximumChaosResistanceShield",
            ModPreset.builder(ModAdditions.ADD_MAX_CHAOS_RESISTANCE)
                .withTier("of_regularity", ModValue.of(1), 68, 125)
                .withTier("of_concord", ModValue.of(2, 2), 75, 125)
                .withTier("of_harmony", ModValue.of(3, 3), 81, 125)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "MaximumFireResistShield",
            ModPreset.builder(ModAdditions.ADD_MAX_FIRE_RESISTANCE)
                .withTier("of_the_bushfire", ModValue.of(1), 68, 250)
                .withTier("of_the_molten_core", ModValue.of(2, 2), 75, 250)
                .withTier("of_the_solar_storm", ModValue.of(3, 3), 81, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "MaximumResistancesShield",
            ModPreset.builder(ModAdditions.ADD_MAX_ELEMENTAL_RESISTANCE)
                .withTier("of_the_deathless", ModValue.of(1), 75, 125)
                .withTier("of_the_everlasting", ModValue.of(2, 2), 81, 125)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "Strength",
            ModPreset.builder(ModAdditions.ADD_STRENGTH)
                .withTier("of_the_brute", ModValue.of(5), 1, 1000)
                .withTier("of_the_wrestler", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_bear", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_lion", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_gorilla", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_goliath", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_leviathan", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_titan", ModValue.of(31, 33), 74, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.SHIELD_MOD_SUFFIX, "StunThresholdShield",
            ModPreset.builder(ModAdditions.ADD_STUN_THRESHOLD)
                .withTier("of_thick_skin", ModValue.of(6), 1, 800)
                .withTier("of_reinforced_skin", ModValue.of(12, 29), 8, 800)
                .withTier("of_stone_skin", ModValue.of(30, 49), 15, 800)
                .withTier("of_iron_skin", ModValue.of(50, 72), 22, 800)
                .withTier("of_steel_skin", ModValue.of(73, 97), 29, 800)
                .withTier("of_granite_skin", ModValue.of(98, 124), 36, 800)
                .withTier("of_platinum_skin", ModValue.of(125, 163), 45, 800)
                .withTier("of_adamantite_skin", ModValue.of(164, 206), 54, 800)
                .withTier("of_corundum_skin", ModValue.of(207, 253), 63, 800)
                .withTier("of_obsidian_skin", ModValue.of(254, 304), 72, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_PREFIX, "IncreasedLifeBuckler",
            ModPreset.builder(ModAdditions.ADD_MAX_HEALTH)
                .withTier("hale", ModValue.of(10), 1, 1000)
                .withTier("healthy", ModValue.of(20, 29), 6, 1000)
                .withTier("sanguine", ModValue.of(30, 39), 16, 1000)
                .withTier("stalwart", ModValue.of(40, 59), 24, 1000)
                .withTier("stout", ModValue.of(60, 69), 33, 1000)
                .withTier("robust", ModValue.of(70, 84), 38, 1000)
                .withTier("rotund", ModValue.of(85, 99), 46, 1000)
                .withTier("virile", ModValue.of(100, 119), 54, 1000)
                .withTier("athlete_s", ModValue.of(120, 149), 60, 1000)
                .withTier("fecund", ModValue.of(150, 174), 65, 1000)
                .withTier("vigorous", ModValue.of(175, 189), 70, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_PREFIX, "IncreasedShieldBlockPercentageBuckler",
            ModPreset.builder(ModAdditions.BLOCK_CHANCE_INC_LOCAL)
                .withTier("steadfast", ModValue.of(15), 1, 1000)
                .withTier("unrelenting", ModValue.of(20, 24), 33, 1000)
                .withTier("adamant", ModValue.of(25, 30), 65, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_PREFIX, "ThornsBuckler",
            ModPreset.builder(ModAdditions.THORNS)
                .withTier("thorny", Map.of("min", ModValue.of(1), "max", ModValue.of(3, 4)), 1, 1000)
                .withTier("spiny", Map.of("min", ModValue.of(5, 7), "max", ModValue.of(7, 10)), 10, 1000)
                .withTier("barbed", Map.of("min", ModValue.of(11, 16), "max", ModValue.of(17, 23)), 19, 1000)
                .withTier("pointed", Map.of("min", ModValue.of(24, 35), "max", ModValue.of(36, 53)), 38, 1000)
                .withTier("spiked", Map.of("min", ModValue.of(40, 60), "max", ModValue.of(61, 92)), 48, 1000)
                .withTier("edged", Map.of("min", ModValue.of(64, 97), "max", ModValue.of(98, 145)), 63, 1000)
                .withTier("jagged", Map.of("min", ModValue.of(101, 151), "max", ModValue.of(152, 220)), 74, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_SUFFIX, "ChaosResistanceBuckler",
            ModPreset.builder(ModAdditions.ADD_CHAOS_RESISTANCE)
                .withTier("of_the_lost", ModValue.of(4), 16, 250)
                .withTier("of_banishment", ModValue.of(8, 11), 30, 250)
                .withTier("of_eviction", ModValue.of(12, 15), 44, 250)
                .withTier("of_expulsion", ModValue.of(16, 19), 56, 250)
                .withTier("of_exile", ModValue.of(20, 23), 68, 250)
                .withTier("of_bameth", ModValue.of(24, 27), 81, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_SUFFIX, "ColdResistanceBuckler",
            ModPreset.builder(ModAdditions.ADD_COLD_RESISTANCE)
                .withTier("of_the_seal", ModValue.of(6), 1, 1000)
                .withTier("of_the_penguin", ModValue.of(11, 15), 14, 1000)
                .withTier("of_the_narwhal", ModValue.of(16, 20), 26, 1000)
                .withTier("of_the_yeti", ModValue.of(21, 25), 38, 1000)
                .withTier("of_the_walrus", ModValue.of(26, 30), 50, 1000)
                .withTier("of_the_polar_bear", ModValue.of(31, 35), 60, 1000)
                .withTier("of_the_ice", ModValue.of(36, 40), 71, 1000)
                .withTier("of_haast", ModValue.of(41, 45), 82, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_SUFFIX, "DexterityBuckler",
            ModPreset.builder(ModAdditions.ADD_DEXTERITY)
                .withTier("of_the_mongoose", ModValue.of(5), 1, 1000)
                .withTier("of_the_lynx", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_fox", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_falcon", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_panther", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_leopard", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_jaguar", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_phantom", ModValue.of(31, 33), 74, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_SUFFIX, "EvasionAppliesToDeflectionBuckler",
            ModPreset.builder(AdditionType.global(StatIds.EVASION_APPLIES_TO_DEFLECTION))
                .withTier("of_deflecting", ModValue.of(8), 1, 1000)
                .withTier("of_bending", ModValue.of(12, 14), 16, 1000)
                .withTier("of_curvation", ModValue.of(15, 17), 36, 1000)
                .withTier("of_diversion", ModValue.of(18, 20), 48, 1000)
                .withTier("of_flexure", ModValue.of(21, 23), 66, 1000)
                .withTier("of_warping", ModValue.of(24, 26), 81, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_SUFFIX, "FireResistanceBuckler",
            ModPreset.builder(ModAdditions.ADD_FIRE_RESISTANCE)
                .withTier("of_the_whelpling", ModValue.of(6), 1, 1000)
                .withTier("of_the_salamander", ModValue.of(11, 15), 12, 1000)
                .withTier("of_the_drake", ModValue.of(16, 20), 24, 1000)
                .withTier("of_the_kiln", ModValue.of(21, 25), 36, 1000)
                .withTier("of_the_furnace", ModValue.of(26, 30), 48, 1000)
                .withTier("of_the_volcano", ModValue.of(31, 35), 60, 1000)
                .withTier("of_magma", ModValue.of(36, 40), 71, 1000)
                .withTier("of_tzteosh", ModValue.of(41, 45), 82, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_SUFFIX, "LightningResistanceBuckler",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_RESISTANCE)
                .withTier("of_the_cloud", ModValue.of(6), 1, 1000)
                .withTier("of_the_squall", ModValue.of(11, 15), 13, 1000)
                .withTier("of_the_storm", ModValue.of(16, 20), 25, 1000)
                .withTier("of_the_thunderhead", ModValue.of(21, 25), 37, 1000)
                .withTier("of_the_tempest", ModValue.of(26, 30), 49, 1000)
                .withTier("of_the_maelstrom", ModValue.of(31, 35), 60, 1000)
                .withTier("of_the_lightning", ModValue.of(36, 40), 71, 1000)
                .withTier("of_ephij", ModValue.of(41, 45), 82, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_SUFFIX, "LocalAttributeRequirementsBuckler",
            ModPreset.builder(LocalAddition.of(StatIds.REDUCED_ATTRIBUTE_REQUIREMENTS))
                .withTier("of_the_worthy", ModValue.of(15), 24, 800)
                .withTier("of_the_apt", ModValue.of(20, 20), 32, 800)
                .withTier("of_the_talented", ModValue.of(25, 25), 40, 800)
                .withTier("of_the_skilled", ModValue.of(30, 30), 52, 800)
                .withTier("of_the_proficient", ModValue.of(35, 35), 60, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_SUFFIX, "MaximumColdResistBuckler",
            ModPreset.builder(ModAdditions.ADD_MAX_COLD_RESISTANCE)
                .withTier("of_furs", ModValue.of(1), 68, 250)
                .withTier("of_the_tundra", ModValue.of(2, 2), 75, 250)
                .withTier("of_the_mammoth", ModValue.of(3, 3), 81, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_SUFFIX, "MaximumLightningResistanceBuckler",
            ModPreset.builder(ModAdditions.ADD_MAX_LIGHTNING_RESISTANCE)
                .withTier("of_impedance", ModValue.of(1), 68, 250)
                .withTier("of_shockproofing", ModValue.of(2, 2), 75, 250)
                .withTier("of_the_lightning_rod", ModValue.of(3, 3), 81, 250)
                .build());

        // 同 SHIELD 段注释：d1 stat_id=null，但 ARMOR_GROUP 侧已有逐阶一致的注册，此处仅搬到可命中的池。
        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_SUFFIX, "MaximumChaosResistanceBuckler",
            ModPreset.builder(ModAdditions.ADD_MAX_CHAOS_RESISTANCE)
                .withTier("of_regularity", ModValue.of(1), 68, 125)
                .withTier("of_concord", ModValue.of(2, 2), 75, 125)
                .withTier("of_harmony", ModValue.of(3, 3), 81, 125)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_SUFFIX, "MaximumFireResistBuckler",
            ModPreset.builder(ModAdditions.ADD_MAX_FIRE_RESISTANCE)
                .withTier("of_the_bushfire", ModValue.of(1), 68, 250)
                .withTier("of_the_molten_core", ModValue.of(2, 2), 75, 250)
                .withTier("of_the_solar_storm", ModValue.of(3, 3), 81, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_SUFFIX, "MaximumResistancesBuckler",
            ModPreset.builder(ModAdditions.ADD_MAX_ELEMENTAL_RESISTANCE)
                .withTier("of_the_deathless", ModValue.of(1), 75, 125)
                .withTier("of_the_everlasting", ModValue.of(2, 2), 81, 125)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.BUCKLER_MOD_SUFFIX, "StunThresholdBuckler",
            ModPreset.builder(ModAdditions.ADD_STUN_THRESHOLD)
                .withTier("of_thick_skin", ModValue.of(6), 1, 800)
                .withTier("of_reinforced_skin", ModValue.of(12, 29), 8, 800)
                .withTier("of_stone_skin", ModValue.of(30, 49), 15, 800)
                .withTier("of_iron_skin", ModValue.of(50, 72), 22, 800)
                .withTier("of_steel_skin", ModValue.of(73, 97), 29, 800)
                .withTier("of_granite_skin", ModValue.of(98, 124), 36, 800)
                .withTier("of_platinum_skin", ModValue.of(125, 163), 45, 800)
                .withTier("of_adamantite_skin", ModValue.of(164, 206), 54, 800)
                .withTier("of_corundum_skin", ModValue.of(207, 253), 63, 800)
                .withTier("of_obsidian_skin", ModValue.of(254, 304), 72, 800)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_SUFFIX, "ChaosResistanceFoci",
            ModPreset.builder(ModAdditions.ADD_CHAOS_RESISTANCE)
                .withTier("of_the_lost", ModValue.of(4), 16, 250)
                .withTier("of_banishment", ModValue.of(8, 11), 30, 250)
                .withTier("of_eviction", ModValue.of(12, 15), 44, 250)
                .withTier("of_expulsion", ModValue.of(16, 19), 56, 250)
                .withTier("of_exile", ModValue.of(20, 23), 68, 250)
                .withTier("of_bameth", ModValue.of(24, 27), 81, 250)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_SUFFIX, "ColdResistanceFoci",
            ModPreset.builder(ModAdditions.ADD_COLD_RESISTANCE)
                .withTier("of_the_seal", ModValue.of(6), 1, 1000)
                .withTier("of_the_penguin", ModValue.of(11, 15), 14, 1000)
                .withTier("of_the_narwhal", ModValue.of(16, 20), 26, 1000)
                .withTier("of_the_yeti", ModValue.of(21, 25), 38, 1000)
                .withTier("of_the_walrus", ModValue.of(26, 30), 50, 1000)
                .withTier("of_the_polar_bear", ModValue.of(31, 35), 60, 1000)
                .withTier("of_the_ice", ModValue.of(36, 40), 71, 1000)
                .withTier("of_haast", ModValue.of(41, 45), 82, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_SUFFIX, "FireResistanceFoci",
            ModPreset.builder(ModAdditions.ADD_FIRE_RESISTANCE)
                .withTier("of_the_whelpling", ModValue.of(6), 1, 1000)
                .withTier("of_the_salamander", ModValue.of(11, 15), 12, 1000)
                .withTier("of_the_drake", ModValue.of(16, 20), 24, 1000)
                .withTier("of_the_kiln", ModValue.of(21, 25), 36, 1000)
                .withTier("of_the_furnace", ModValue.of(26, 30), 48, 1000)
                .withTier("of_the_volcano", ModValue.of(31, 35), 60, 1000)
                .withTier("of_magma", ModValue.of(36, 40), 71, 1000)
                .withTier("of_tzteosh", ModValue.of(41, 45), 82, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_SUFFIX, "IntelligenceFoci",
            ModPreset.builder(ModAdditions.ADD_INTELLIGENCE)
                .withTier("of_the_pupil", ModValue.of(5), 1, 1000)
                .withTier("of_the_student", ModValue.of(9, 12), 11, 1000)
                .withTier("of_the_prodigy", ModValue.of(13, 16), 22, 1000)
                .withTier("of_the_augur", ModValue.of(17, 20), 33, 1000)
                .withTier("of_the_philosopher", ModValue.of(21, 24), 44, 1000)
                .withTier("of_the_sage", ModValue.of(25, 27), 55, 1000)
                .withTier("of_the_savant", ModValue.of(28, 30), 66, 1000)
                .withTier("of_the_virtuoso", ModValue.of(31, 33), 74, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_SUFFIX, "LightningResistanceFoci",
            ModPreset.builder(ModAdditions.ADD_LIGHTNING_RESISTANCE)
                .withTier("of_the_cloud", ModValue.of(6), 1, 1000)
                .withTier("of_the_squall", ModValue.of(11, 15), 13, 1000)
                .withTier("of_the_storm", ModValue.of(16, 20), 25, 1000)
                .withTier("of_the_thunderhead", ModValue.of(21, 25), 37, 1000)
                .withTier("of_the_tempest", ModValue.of(26, 30), 49, 1000)
                .withTier("of_the_maelstrom", ModValue.of(31, 35), 60, 1000)
                .withTier("of_the_lightning", ModValue.of(36, 40), 71, 1000)
                .withTier("of_ephij", ModValue.of(41, 45), 82, 1000)
                .build());

        EquipmentGen.registerMod(bootstrap, EquipmentGen.FOCI_MOD_SUFFIX, "LocalAttributeRequirementsFoci",
            ModPreset.builder(LocalAddition.of(StatIds.REDUCED_ATTRIBUTE_REQUIREMENTS))
                .withTier("of_the_worthy", ModValue.of(15), 24, 800)
                .withTier("of_the_apt", ModValue.of(20, 20), 32, 800)
                .withTier("of_the_talented", ModValue.of(25, 25), 40, 800)
                .withTier("of_the_skilled", ModValue.of(30, 30), 52, 800)
                .withTier("of_the_proficient", ModValue.of(35, 35), 60, 800)
                .build());
    }

    public static void register(BootstrapContext<ItemInstance> bootstrap) {
        g0(bootstrap);
        g1(bootstrap);
        g2(bootstrap);
        s0(bootstrap);
        s1(bootstrap);
        s2(bootstrap);
        s3(bootstrap);
        s4(bootstrap);
        s5(bootstrap);
        s6(bootstrap);
        // 药剂 / 护符词缀独立成表（按文本模板拆家族 + spawn_no 门控），见 ExplicitFlaskModGen。
        ExplicitFlaskModGen.register(bootstrap);
    }

    private ExplicitModGen() {
    }
}
