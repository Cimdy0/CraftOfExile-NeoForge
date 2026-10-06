package com.cimdy.coe.data.gen.equipment;

import com.cimdy.coe.data.codec.item.ItemInstance;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentRead;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentTemplate;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.value.Value;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.cimdy.coe.data.gen.equipment.EquipmentGen.*;

/**
 * 显式装备数据（离线烘焙）：每条装备 = 一条直接工厂调用（tpl 注册模板 / equip 注册读取），
 * 无运行时解析，无中央 Map 聚合。模板按需求签名 per-sig 拆分（与旧 registerEquipmentTemplate 行为一致），
 * 读取经 builder 直接构造并引用对应模板。
 */
public final class ExplicitEquipmentGen {

    public static void register(BootstrapContext<ItemInstance> bootstrap) {
        registerTemplates(bootstrap);
        registerReads(bootstrap);
    }

    // ===================== 模板注册（父类 + 各子类 per-sig） =====================

    public static void registerTemplates(BootstrapContext<ItemInstance> bootstrap) {
        // 父类模板
        EquipmentGen.registerEquipmentTemplate(bootstrap, BASE,
            EquipmentTemplate.builder().req("l", EquipmentTemplate.RequireKind.LEVEL).build());
        EquipmentGen.registerEquipmentTemplate(bootstrap, WEAPON,
            EquipmentTemplate.builder(id(BASE)).stats(weaponStats()).build());
        EquipmentGen.registerEquipmentTemplate(bootstrap, ARMOR,
            EquipmentTemplate.builder(id(BASE)).stats(armorStats()).build());
        EquipmentGen.registerEquipmentTemplate(bootstrap, ACCESSORY,
            EquipmentTemplate.builder(id(BASE)).stats(accessoryStats()).build());
        EquipmentGen.registerEquipmentTemplate(bootstrap, FOCUS_STATS,
            EquipmentTemplate.builder(id(BASE)).stats(focusStats()).build());
        EquipmentGen.registerEquipmentTemplate(bootstrap, FLASK,
            EquipmentTemplate.builder(id(BASE)).stats(flaskStats()).build());
        EquipmentGen.registerEquipmentTemplate(bootstrap, GEM,
            EquipmentTemplate.builder(id(BASE)).stats(gemStats()).build());

        // CLAW (weapon/main_hand/one_hand/claw)
        tpl(bootstrap, TP_CLAW, SIG_DEXTERITY, WEAPON);;

        // DAGGER (weapon/main_hand/one_hand/dagger)
        tpl(bootstrap, TP_DAGGER, SIG_DEXTERITY_INTELLIGENCE, WEAPON);;

        // ONE_HAND_SWORD (weapon/main_hand/one_hand/sword)
        tpl(bootstrap, TP_ONE_HAND_SWORD, SIG_NONE, WEAPON);;
        tpl(bootstrap, TP_ONE_HAND_SWORD, SIG_STRENGTH_DEXTERITY, WEAPON);;

        // ONE_HAND_AXE (weapon/main_hand/one_hand/axe)
        tpl(bootstrap, TP_ONE_HAND_AXE, SIG_STRENGTH, WEAPON);;
        tpl(bootstrap, TP_ONE_HAND_AXE, SIG_STRENGTH_DEXTERITY, WEAPON);;

        // ONE_HAND_MACE (weapon/main_hand/one_hand/mace)
        tpl(bootstrap, TP_ONE_HAND_MACE, SIG_STRENGTH, WEAPON);;
        tpl(bootstrap, TP_ONE_HAND_MACE, SIG_NONE, WEAPON);;

        // SCEPTRE (weapon/main_hand/one_hand/sceptre)
        tpl(bootstrap, TP_SCEPTRE, SIG_INTELLIGENCE, WEAPON);;
        tpl(bootstrap, TP_SCEPTRE, SIG_STRENGTH_INTELLIGENCE, WEAPON);;

        // SPEAR (weapon/main_hand/one_hand/spear)
        tpl(bootstrap, TP_SPEAR, SIG_DEXTERITY, WEAPON);;
        tpl(bootstrap, TP_SPEAR, SIG_STRENGTH_DEXTERITY, WEAPON);;
        tpl(bootstrap, TP_SPEAR, SIG_STRENGTH_DEXTERITY_INTELLIGENCE, WEAPON);;

        // FLAIL (weapon/main_hand/one_hand/flail)
        tpl(bootstrap, TP_FLAIL, SIG_STRENGTH, WEAPON);;
        tpl(bootstrap, TP_FLAIL, SIG_STRENGTH_INTELLIGENCE, WEAPON);;

        // WAND (weapon/main_hand/one_hand/wand)
        tpl(bootstrap, TP_WAND, SIG_NONE, WEAPON);;
        tpl(bootstrap, TP_WAND, SIG_INTELLIGENCE, WEAPON);;

        // BOW (weapon/main_hand/two_hand/bow)
        tpl(bootstrap, TP_BOW, SIG_DEXTERITY, WEAPON);;
        tpl(bootstrap, TP_BOW, SIG_STRENGTH_DEXTERITY, WEAPON);;

        // STAFF (weapon/main_hand/two_hand/staff)
        tpl(bootstrap, TP_STAFF, SIG_NONE, WEAPON);;
        tpl(bootstrap, TP_STAFF, SIG_INTELLIGENCE, WEAPON);;

        // TWO_HAND_SWORD (weapon/main_hand/two_hand/sword)
        tpl(bootstrap, TP_TWO_HAND_SWORD, SIG_STRENGTH_DEXTERITY, WEAPON);;

        // TWO_HAND_AXE (weapon/main_hand/two_hand/axe)
        tpl(bootstrap, TP_TWO_HAND_AXE, SIG_STRENGTH, WEAPON);;
        tpl(bootstrap, TP_TWO_HAND_AXE, SIG_STRENGTH_DEXTERITY, WEAPON);;

        // TWO_HAND_MACE (weapon/main_hand/two_hand/mace)
        tpl(bootstrap, TP_TWO_HAND_MACE, SIG_STRENGTH, WEAPON);;
        tpl(bootstrap, TP_TWO_HAND_MACE, SIG_STRENGTH_DEXTERITY, WEAPON);;
        tpl(bootstrap, TP_TWO_HAND_MACE, SIG_STRENGTH_INTELLIGENCE, WEAPON);;

        // QUARTERSTAFF (weapon/main_hand/two_hand/quarterstaff)
        tpl(bootstrap, TP_QUARTERSTAFF, SIG_DEXTERITY, WEAPON);;
        tpl(bootstrap, TP_QUARTERSTAFF, SIG_DEXTERITY_INTELLIGENCE, WEAPON);;

        // CROSSBOW (weapon/main_hand/two_hand/cross_bow)
        tpl(bootstrap, TP_CROSS_BOW, SIG_STRENGTH_DEXTERITY, WEAPON);;
        tpl(bootstrap, TP_CROSS_BOW, SIG_NONE, WEAPON);;

        // TRAP (weapon/main_hand/two_hand/trap)
        tpl(bootstrap, TP_TRAP, SIG_NONE, BASE);;

        // TALISMAN (weapon/main_hand/two_hand/talisman)
        tpl(bootstrap, TP_TALISMAN, SIG_STRENGTH_INTELLIGENCE, ACCESSORY);;

        // QUIVER (weapon/off_hand/quiver)
        tpl(bootstrap, TP_QUIVER, SIG_NONE, ARMOR);;

        // SHIELD (weapon/off_hand/shield)
        tpl(bootstrap, TP_SHIELD, SIG_STRENGTH, ARMOR);;
        tpl(bootstrap, TP_SHIELD, SIG_DEXTERITY, ARMOR);;
        tpl(bootstrap, TP_SHIELD, SIG_STRENGTH_DEXTERITY, ARMOR);;
        tpl(bootstrap, TP_SHIELD, SIG_STRENGTH_INTELLIGENCE, ARMOR);;

        // BUCKLER (weapon/off_hand/buckler)
        tpl(bootstrap, TP_BUCKLER, SIG_DEXTERITY, ARMOR);;

        // FOCUS (weapon/off_hand/foci)
        tpl(bootstrap, TP_FOCI, SIG_INTELLIGENCE, FOCUS_STATS);;

        // BODY_ARMOUR (armor/chest)
        tpl(bootstrap, TP_CHEST, SIG_STRENGTH, ARMOR);;
        tpl(bootstrap, TP_CHEST, SIG_DEXTERITY, ARMOR);;
        tpl(bootstrap, TP_CHEST, SIG_INTELLIGENCE, ARMOR);;
        tpl(bootstrap, TP_CHEST, SIG_STRENGTH_DEXTERITY, ARMOR);;
        tpl(bootstrap, TP_CHEST, SIG_STRENGTH_DEXTERITY_INTELLIGENCE, ARMOR);;
        tpl(bootstrap, TP_CHEST, SIG_STRENGTH_INTELLIGENCE, ARMOR);;
        tpl(bootstrap, TP_CHEST, SIG_DEXTERITY_INTELLIGENCE, ARMOR);;

        // BOOTS (armor/feet)
        tpl(bootstrap, TP_FEET, SIG_STRENGTH, ARMOR);;
        tpl(bootstrap, TP_FEET, SIG_DEXTERITY, ARMOR);;
        tpl(bootstrap, TP_FEET, SIG_INTELLIGENCE, ARMOR);;
        tpl(bootstrap, TP_FEET, SIG_STRENGTH_DEXTERITY, ARMOR);;
        tpl(bootstrap, TP_FEET, SIG_STRENGTH_INTELLIGENCE, ARMOR);;
        tpl(bootstrap, TP_FEET, SIG_DEXTERITY_INTELLIGENCE, ARMOR);;

        // HELMET (armor/head)
        tpl(bootstrap, TP_HEAD, SIG_STRENGTH, ARMOR);;
        tpl(bootstrap, TP_HEAD, SIG_DEXTERITY, ARMOR);;
        tpl(bootstrap, TP_HEAD, SIG_INTELLIGENCE, ARMOR);;
        tpl(bootstrap, TP_HEAD, SIG_STRENGTH_DEXTERITY, ARMOR);;
        tpl(bootstrap, TP_HEAD, SIG_STRENGTH_INTELLIGENCE, ARMOR);;
        tpl(bootstrap, TP_HEAD, SIG_DEXTERITY_INTELLIGENCE, ARMOR);;

        // LEGS (armor/legs)
        tpl(bootstrap, TP_LEGS, SIG_STRENGTH, ARMOR);;
        tpl(bootstrap, TP_LEGS, SIG_DEXTERITY, ARMOR);;
        tpl(bootstrap, TP_LEGS, SIG_INTELLIGENCE, ARMOR);;
        tpl(bootstrap, TP_LEGS, SIG_STRENGTH_DEXTERITY, ARMOR);;
        tpl(bootstrap, TP_LEGS, SIG_STRENGTH_INTELLIGENCE, ARMOR);;
        tpl(bootstrap, TP_LEGS, SIG_DEXTERITY_INTELLIGENCE, ARMOR);;

        // AMULET (accessory/amulet)
        tpl(bootstrap, TP_AMULET, SIG_NONE, ACCESSORY);;

        // RING (accessory/ring)
        tpl(bootstrap, TP_RING, SIG_NONE, ACCESSORY);;

        // BELT (accessory/belt)
        tpl(bootstrap, TP_BELT, SIG_NONE, ACCESSORY);;

        // FLASK (flask)
        tpl(bootstrap, FLASK + "/" + LIFE_FLASK, SIG_NONE, FLASK);
        tpl(bootstrap, FLASK + "/" + MANA_FLASK, SIG_NONE, FLASK);

        // GEM (gem)
        tpl(bootstrap, GEM + "/diamond", SIG_NONE, GEM);
        tpl(bootstrap, GEM + "/emerald", SIG_NONE, GEM);
        tpl(bootstrap, GEM + "/ruby", SIG_NONE, GEM);
        tpl(bootstrap, GEM + "/sapphire", SIG_NONE, GEM);

    }

    // ===================== 读取注册（具体装备） =====================

    public static void registerReads(BootstrapContext<ItemInstance> bootstrap) {
        registerReads1(bootstrap);
        registerReads2(bootstrap);
        registerReads3(bootstrap);
        registerReads4(bootstrap);
        registerReads5(bootstrap);
    }

    private static void registerReads1(BootstrapContext<ItemInstance> bootstrap) {
        // CLAW (weapon/main_hand/one_hand/claw)
        equip(bootstrap, TP_CLAW, "crude_claw", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(6), "dex", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_CLAW, "pict_claw", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(6), "dex", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_CLAW, "wolfbone_claw", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(12), "dex", ExpressionCodec.of(24)), Map.of(), List.of());
        equip(bootstrap, TP_CLAW, "forked_claw", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_CLAW, "plated_claw", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(22), "dex", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_CLAW, "edged_claw", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(28), "dex", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_CLAW, "arced_claw", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_CLAW, "hooked_claw", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(36), "dex", ExpressionCodec.of(65)), Map.of(), List.of());
        equip(bootstrap, TP_CLAW, "razorglass_claw", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_CLAW, "sharktooth_claw", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(49), "dex", ExpressionCodec.of(87)), Map.of(), List.of());
        equip(bootstrap, TP_CLAW, "armoured_claw", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "dex", ExpressionCodec.of(92)), Map.of(), List.of());
        equip(bootstrap, TP_CLAW, "piercing_claw", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(97)), Map.of(), List.of());
        equip(bootstrap, TP_CLAW, "talon_claw", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(114)), Map.of(), List.of());

        // DAGGER (weapon/main_hand/one_hand/dagger)
        equip(bootstrap, TP_DAGGER, "ethereal_blade", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(6), "dex", ExpressionCodec.of(9), "int", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_DAGGER, "glass_shank", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(6), "dex", ExpressionCodec.of(9), "int", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_DAGGER, "crone_knife", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(6), "dex", ExpressionCodec.of(9), "int", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_DAGGER, "simple_dagger", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(12), "dex", ExpressionCodec.of(15), "int", ExpressionCodec.of(15)), Map.of(), List.of());
        equip(bootstrap, TP_DAGGER, "skinning_knife", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(19), "int", ExpressionCodec.of(19)), Map.of(), List.of());
        equip(bootstrap, TP_DAGGER, "moon_dagger", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(20), "dex", ExpressionCodec.of(22), "int", ExpressionCodec.of(22)), Map.of(), List.of());
        equip(bootstrap, TP_DAGGER, "engraved_knife", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(27), "dex", ExpressionCodec.of(29), "int", ExpressionCodec.of(29)), Map.of(), List.of());
        equip(bootstrap, TP_DAGGER, "obsidian_dagger", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(34), "int", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_DAGGER, "bloodletting_dagger", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(39), "dex", ExpressionCodec.of(39), "int", ExpressionCodec.of(39)), Map.of(), List.of());
        equip(bootstrap, TP_DAGGER, "mail_breaker", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(45), "int", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_DAGGER, "kris_knife", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(49), "dex", ExpressionCodec.of(49), "int", ExpressionCodec.of(49)), Map.of(), List.of());
        equip(bootstrap, TP_DAGGER, "parrying_dagger", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "dex", ExpressionCodec.of(51), "int", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_DAGGER, "arcane_dirk", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(56), "dex", ExpressionCodec.of(55), "int", ExpressionCodec.of(55)), Map.of(), List.of());
        equip(bootstrap, TP_DAGGER, "cinquedea", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(63), "int", ExpressionCodec.of(63)), Map.of(), List.of());

        // ONE_HAND_SWORD (weapon/main_hand/one_hand/sword)
        equip(bootstrap, TP_ONE_HAND_SWORD, "golden_blade", SIG_NONE, Map.of("l", ExpressionCodec.of(1)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_SWORD, "energy_blade", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(9), "dex", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_SWORD, "shortsword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(9), "dex", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_SWORD, "broadsword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(9), "dex", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_SWORD, "vampiric_blade", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(14), "dex", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_SWORD, "scimitar", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(19), "dex", ExpressionCodec.of(19)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_SWORD, "charred_shortsword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(21), "str", ExpressionCodec.of(23), "dex", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_SWORD, "sickle_sword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(28), "str", ExpressionCodec.of(29), "dex", ExpressionCodec.of(29)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_SWORD, "falchion", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(34), "dex", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_SWORD, "treasured_blade", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(40), "dex", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_SWORD, "cutlass", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(45), "dex", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_SWORD, "runic_shortsword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(50), "str", ExpressionCodec.of(49), "dex", ExpressionCodec.of(49)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_SWORD, "messer", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(51), "dex", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_SWORD, "commander_sword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(53), "dex", ExpressionCodec.of(53)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_SWORD, "dark_blade", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(63), "dex", ExpressionCodec.of(63)), Map.of(), List.of());

        // ONE_HAND_AXE (weapon/main_hand/one_hand/axe)
        equip(bootstrap, TP_ONE_HAND_AXE, "dull_hatchet", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(4), "str", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_AXE, "hook_axe", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(4), "str", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_AXE, "bearded_axe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(10), "str", ExpressionCodec.of(17), "dex", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_AXE, "extended_cleaver", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(25), "dex", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_AXE, "bandit_hatchet", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(20), "str", ExpressionCodec.of(30), "dex", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_AXE, "crescent_axe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "str", ExpressionCodec.of(38), "dex", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_AXE, "carving_hatchet", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(47), "dex", ExpressionCodec.of(20)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_AXE, "sacrificial_axe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(37), "str", ExpressionCodec.of(52), "dex", ExpressionCodec.of(22)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_AXE, "boarding_hatchet", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(63), "dex", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_AXE, "fury_cleaver", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(67), "dex", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_AXE, "battle_axe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(72), "dex", ExpressionCodec.of(29)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_AXE, "profane_cleaver", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(75), "dex", ExpressionCodec.of(30)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_AXE, "dread_hatchet", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(89), "dex", ExpressionCodec.of(36)), Map.of(), List.of());

        // ONE_HAND_MACE (weapon/main_hand/one_hand/mace)
        equip(bootstrap, TP_ONE_HAND_MACE, "wooden_club", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(4), "str", ExpressionCodec.of(11)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "smithing_hammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(4), "str", ExpressionCodec.of(11)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "slim_mace", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(10), "str", ExpressionCodec.of(21)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "spiked_club", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "warpick", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(22), "str", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "plated_mace", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(26), "str", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "brigand_mace", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "construct_hammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(36), "str", ExpressionCodec.of(65)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "morning_star", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "jade_club", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(49), "str", ExpressionCodec.of(87)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "lumen_mace", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(92)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "execratus_hammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(97)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "torment_club", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(114)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "runemastered_torment_club", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(114)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "kalguuran_forgehammer", SIG_NONE, Map.of("l", ExpressionCodec.of(47)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "calescent_hammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "flared_mace", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "battle_pick", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "marching_mace", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(96)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "runeforged_marching_mace", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(114)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "bandit_mace", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(104)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "structured_hammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(109)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "runeforged_wooden_club", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "runemastered_wooden_club", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(97)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "runeforged_slim_mace", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "runeforged_spiked_club", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "runeforged_warpick", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "runeforged_kalguuran_forgehammer", SIG_NONE, Map.of("l", ExpressionCodec.of(65)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "runeforged_morning_star", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(114)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "flanged_mace", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(67), "str", ExpressionCodec.of(134)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "crown_mace", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(72), "str", ExpressionCodec.of(149)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "molten_hammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(77), "str", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "strife_pick", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(78), "str", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "fortified_hammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(79), "str", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "marauding_mace", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(77), "str", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_ONE_HAND_MACE, "akoyan_club", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(78), "str", ExpressionCodec.of(163)), Map.of(), List.of());

        // SCEPTRE (weapon/main_hand/one_hand/sceptre)
        equip(bootstrap, TP_SCEPTRE, "rattling_sceptre", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(6), "int", ExpressionCodec.of(12)), Map.of(), List.of(id("preset_skeletal_warrior")));
        equip(bootstrap, TP_SCEPTRE, "stoic_sceptre", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(6), "int", ExpressionCodec.of(12)), Map.of(), List.of(id("preset_discipline")));
        equip(bootstrap, TP_SCEPTRE, "lupine_sceptre", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(12), "int", ExpressionCodec.of(24)), Map.of(), List.of(id("preset_skeletal_warrior")));
        equip(bootstrap, TP_SCEPTRE, "omen_sceptre", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(12), "int", ExpressionCodec.of(25)), Map.of(), List.of(id("preset_malice")));
        equip(bootstrap, TP_SCEPTRE, "ochre_sceptre", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(21), "int", ExpressionCodec.of(40)), Map.of(), List.of(id("preset_skeletal_warrior")));
        equip(bootstrap, TP_SCEPTRE, "shrine_sceptre", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(26), "str", ExpressionCodec.of(17), "int", ExpressionCodec.of(38)), Map.of(), List.of(id("preset_impurity")));
        equip(bootstrap, TP_SCEPTRE, "devouring_sceptre", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "int", ExpressionCodec.of(60)), Map.of(), List.of(id("preset_skeletal_warrior")));
        equip(bootstrap, TP_SCEPTRE, "clasped_sceptre", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(36), "int", ExpressionCodec.of(65)), Map.of(), List.of(id("preset_heart_of_ice")));
        equip(bootstrap, TP_SCEPTRE, "devotional_sceptre", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(26), "int", ExpressionCodec.of(63)), Map.of(), List.of(id("preset_skeletal_warrior")));
        equip(bootstrap, TP_SCEPTRE, "wrath_sceptre", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(49), "int", ExpressionCodec.of(87)), Map.of(), List.of(id("preset_fulmination")));
        equip(bootstrap, TP_SCEPTRE, "aromatic_sceptre", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "int", ExpressionCodec.of(92)), Map.of(), List.of(id("preset_skeletal_warrior")));
        equip(bootstrap, TP_SCEPTRE, "pious_sceptre", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(58), "int", ExpressionCodec.of(102)), Map.of(), List.of(id("preset_skeletal_warrior")));
        equip(bootstrap, TP_SCEPTRE, "hallowed_sceptre", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(114)), Map.of(), List.of(id("preset_skeletal_warrior")));

        // SPEAR (weapon/main_hand/one_hand/spear)
        equip(bootstrap, TP_SPEAR, "hardwood_spear", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(5), "dex", ExpressionCodec.of(10)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "ironhead_spear", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(5), "dex", ExpressionCodec.of(10)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "hunting_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(10), "str", ExpressionCodec.of(9), "dex", ExpressionCodec.of(17)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "winged_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(12), "dex", ExpressionCodec.of(25)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "war_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(21), "str", ExpressionCodec.of(14), "dex", ExpressionCodec.of(31)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "forked_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "str", ExpressionCodec.of(17), "dex", ExpressionCodec.of(38)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "barbed_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(20), "dex", ExpressionCodec.of(47)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "broad_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(26), "dex", ExpressionCodec.of(55)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "crossblade_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(26), "dex", ExpressionCodec.of(63)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "seaglass_spear", SIG_STRENGTH_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(29), "dex", ExpressionCodec.of(71), "int", ExpressionCodec.of(50)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "sword_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(29), "dex", ExpressionCodec.of(72)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "striking_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(31), "dex", ExpressionCodec.of(76)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "helix_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(36), "dex", ExpressionCodec.of(89)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "steelhead_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(26), "dex", ExpressionCodec.of(63)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "coursing_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(27), "dex", ExpressionCodec.of(67)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "swift_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(29), "dex", ExpressionCodec.of(71)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "branched_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(30), "dex", ExpressionCodec.of(75)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "jagged_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(33), "dex", ExpressionCodec.of(81)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "massive_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(40), "dex", ExpressionCodec.of(80)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "runeforged_hardwood_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(23), "dex", ExpressionCodec.of(56)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "runeforged_ironhead_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(23), "dex", ExpressionCodec.of(54)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "runemastered_ironhead_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(31), "dex", ExpressionCodec.of(76)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "runeforged_hunting_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(23), "dex", ExpressionCodec.of(54)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "runeforged_winged_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(23), "dex", ExpressionCodec.of(56)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "runeforged_war_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(23), "dex", ExpressionCodec.of(56)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "runeforged_forked_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(31), "dex", ExpressionCodec.of(76)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "runeforged_barbed_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(31), "dex", ExpressionCodec.of(76)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "orichalcum_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(67), "str", ExpressionCodec.of(41), "dex", ExpressionCodec.of(104)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "soaring_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(41), "dex", ExpressionCodec.of(115)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "pronged_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(72), "str", ExpressionCodec.of(46), "dex", ExpressionCodec.of(115)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "stalking_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(77), "str", ExpressionCodec.of(50), "dex", ExpressionCodec.of(127)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "flying_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(78), "str", ExpressionCodec.of(50), "dex", ExpressionCodec.of(127)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "grand_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(79), "str", ExpressionCodec.of(68), "dex", ExpressionCodec.of(109)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "spiked_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(77), "str", ExpressionCodec.of(50), "dex", ExpressionCodec.of(127)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "guardian_spear", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(50), "dex", ExpressionCodec.of(127)), Map.of(), List.of(id("preset_spear_throw")));
        equip(bootstrap, TP_SPEAR, "akoyan_spear", SIG_STRENGTH_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(78), "str", ExpressionCodec.of(50), "dex", ExpressionCodec.of(127), "int", ExpressionCodec.of(90)), Map.of(), List.of(id("preset_spear_throw")));

        // FLAIL (weapon/main_hand/one_hand/flail)
        equip(bootstrap, TP_FLAIL, "splintered_flail", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_FLAIL, "chain_flail", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_FLAIL, "holy_flail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(18), "int", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_FLAIL, "iron_flail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(25), "int", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_FLAIL, "twin_flail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(20), "str", ExpressionCodec.of(30), "int", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_FLAIL, "slender_flail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(26), "str", ExpressionCodec.of(38), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_FLAIL, "stone_flail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(47), "int", ExpressionCodec.of(20)), Map.of(), List.of());
        equip(bootstrap, TP_FLAIL, "ring_flail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(54), "int", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_FLAIL, "guarded_flail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(63), "int", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_FLAIL, "icicle_flail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(47), "str", ExpressionCodec.of(66), "int", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_FLAIL, "tearing_flail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(72), "int", ExpressionCodec.of(29)), Map.of(), List.of());
        equip(bootstrap, TP_FLAIL, "great_flail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(58), "str", ExpressionCodec.of(80), "int", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_FLAIL, "abyssal_flail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(89), "int", ExpressionCodec.of(36)), Map.of(), List.of());

        // WAND (weapon/main_hand/one_hand/wand)
        equip(bootstrap, TP_WAND, "withered_wand", SIG_NONE, Map.of("l", ExpressionCodec.of(2)), Map.of(), List.of(id("preset_chaos_bolt")));
        equip(bootstrap, TP_WAND, "bone_wand", SIG_NONE, Map.of("l", ExpressionCodec.of(2)), Map.of(), List.of(id("preset_bone_blast")));
        equip(bootstrap, TP_WAND, "attuned_wand", SIG_NONE, Map.of("l", ExpressionCodec.of(2)), Map.of(), List.of(id("preset_mana_drain")));
        equip(bootstrap, TP_WAND, "siphoning_wand", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(11), "int", ExpressionCodec.of(23)), Map.of(), List.of(id("preset_power_siphon")));
        equip(bootstrap, TP_WAND, "volatile_wand", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "int", ExpressionCodec.of(31)), Map.of(), List.of(id("preset_volatile_dead")));
        equip(bootstrap, TP_WAND, "galvanic_wand", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(25), "int", ExpressionCodec.of(46)), Map.of(), List.of(id("preset_galvanic_field")));
        equip(bootstrap, TP_WAND, "acrid_wand", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "int", ExpressionCodec.of(60)), Map.of(), List.of(id("preset_decompose")));
        equip(bootstrap, TP_WAND, "offering_wand", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(68)), Map.of(), List.of(id("preset_exsanguinate")));
        equip(bootstrap, TP_WAND, "frigid_wand", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(80)), Map.of(), List.of(id("preset_chaos_bolt")));
        equip(bootstrap, TP_WAND, "torture_wand", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(49), "int", ExpressionCodec.of(87)), Map.of(), List.of(id("preset_chaos_bolt")));
        equip(bootstrap, TP_WAND, "critical_wand", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "int", ExpressionCodec.of(92)), Map.of(), List.of(id("preset_chaos_bolt")));
        equip(bootstrap, TP_WAND, "primordial_wand", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(56), "int", ExpressionCodec.of(99)), Map.of(), List.of(id("preset_wither")));
        equip(bootstrap, TP_WAND, "dueling_wand", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(114)), Map.of(), List.of(id("preset_spellslinger")));
        equip(bootstrap, TP_WAND, "twisted_wand", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(114)), Map.of(), List.of(id("preset_coiling_bolts")));
        equip(bootstrap, TP_WAND, "runic_fork", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(114)), Map.of(), List.of());
        equip(bootstrap, TP_WAND, "runemastered_runic_fork", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(114)), Map.of(), List.of());

        // BOW (weapon/main_hand/two_hand/bow)
        equip(bootstrap, TP_BOW, "crude_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(5), "dex", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "shortbow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(5), "dex", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "warden_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "recurve_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "composite_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(22), "dex", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "dualstring_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(28), "dex", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "cultist_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "zealot_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(39), "dex", ExpressionCodec.of(70)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "artillery_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "tribal_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(50), "dex", ExpressionCodec.of(89)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "greatbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(92), "dex", ExpressionCodec.of(92)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "double_limb_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(56), "dex", ExpressionCodec.of(99)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "heavy_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(114)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "snakewood_shortbow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "protector_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(48), "dex", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "rider_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "twin_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(96)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "adherent_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(104)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "militant_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(62), "dex", ExpressionCodec.of(109)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "runeforged_crude_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(97)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "runeforged_shortbow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "runeforged_warden_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "dex", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "runeforged_recurve_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "dex", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "runeforged_composite_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(97)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "runeforged_dualstring_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(97)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "runeforged_zealot_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(97)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "ironwood_shortbow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(67), "dex", ExpressionCodec.of(134)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "cavalry_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(72), "dex", ExpressionCodec.of(149)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "guardian_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(77), "dex", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "gemini_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(78), "dex", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "fanatic_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(79), "dex", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "warmonger_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(77), "dex", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "obliterator_bow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(78), "dex", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_BOW, "heartwood_shortbow", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(67), "dex", ExpressionCodec.of(134)), Map.of(), List.of());

        // STAFF (weapon/main_hand/two_hand/staff)
        equip(bootstrap, TP_STAFF, "ashen_staff", SIG_NONE, Map.of("l", ExpressionCodec.of(2)), Map.of(), List.of(id("preset_firebolt")));
        equip(bootstrap, TP_STAFF, "gelid_staff", SIG_NONE, Map.of("l", ExpressionCodec.of(2)), Map.of(), List.of(id("preset_freezing_shards")));
        equip(bootstrap, TP_STAFF, "voltaic_staff", SIG_NONE, Map.of("l", ExpressionCodec.of(2)), Map.of(), List.of(id("preset_lightning_bolt")));
        equip(bootstrap, TP_STAFF, "spriggan_staff", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(11), "int", ExpressionCodec.of(23)), Map.of(), List.of(id("preset_firebolt")));
        equip(bootstrap, TP_STAFF, "pyrophyte_staff", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "int", ExpressionCodec.of(31)), Map.of(), List.of(id("preset_solar_orb")));
        equip(bootstrap, TP_STAFF, "chiming_staff", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(25), "int", ExpressionCodec.of(46)), Map.of(), List.of(id("preset_sigil_of_power")));
        equip(bootstrap, TP_STAFF, "rending_staff", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "int", ExpressionCodec.of(60)), Map.of(), List.of(id("preset_soulrend")));
        equip(bootstrap, TP_STAFF, "reaping_staff", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(68)), Map.of(), List.of(id("preset_reap")));
        equip(bootstrap, TP_STAFF, "icicle_staff", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(80)), Map.of(), List.of(id("preset_firebolt")));
        equip(bootstrap, TP_STAFF, "roaring_staff", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(49), "int", ExpressionCodec.of(87)), Map.of(), List.of(id("preset_unleash")));
        equip(bootstrap, TP_STAFF, "paralysing_staff", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "int", ExpressionCodec.of(92)), Map.of(), List.of(id("preset_enervating_nova")));
        equip(bootstrap, TP_STAFF, "sanctified_staff", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(56), "int", ExpressionCodec.of(99)), Map.of(), List.of(id("preset_consecrate")));
        equip(bootstrap, TP_STAFF, "dark_staff", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(114)), Map.of(), List.of(id("preset_dark_pact")));
        equip(bootstrap, TP_STAFF, "ravenous_staff", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(114)), Map.of(), List.of(id("preset_feast_of_flesh")));
        equip(bootstrap, TP_STAFF, "permafrost_staff", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "int", ExpressionCodec.of(114)), Map.of(), List.of(id("preset_heart_of_ice")));
        equip(bootstrap, TP_STAFF, "reflecting_staff", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "int", ExpressionCodec.of(114)), Map.of(), List.of(id("preset_mirror_of_refraction")));
        equip(bootstrap, TP_STAFF, "perching_staff", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(114)), Map.of(), List.of(id("preset_spiraling_conspiracy")));

        // TWO_HAND_SWORD (weapon/main_hand/two_hand/sword)
        equip(bootstrap, TP_TWO_HAND_SWORD, "energy_blade", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(9), "dex", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_SWORD, "corroded_longsword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(9), "dex", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_SWORD, "iron_greatsword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(9), "dex", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_SWORD, "blessed_claymore", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(14), "dex", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_SWORD, "broad_greatsword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(19), "dex", ExpressionCodec.of(19)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_SWORD, "rippled_greatsword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(22), "str", ExpressionCodec.of(24), "dex", ExpressionCodec.of(24)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_SWORD, "arced_longsword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(28), "str", ExpressionCodec.of(29), "dex", ExpressionCodec.of(29)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_SWORD, "stone_greatsword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(34), "dex", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_SWORD, "obsidian_greatsword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(36), "str", ExpressionCodec.of(37), "dex", ExpressionCodec.of(37)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_SWORD, "keen_greatsword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(45), "dex", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_SWORD, "ancient_greatblade", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(49), "str", ExpressionCodec.of(49), "dex", ExpressionCodec.of(49)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_SWORD, "flanged_greatblade", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(51), "dex", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_SWORD, "regalia_longsword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(53), "dex", ExpressionCodec.of(53)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_SWORD, "ultra_greatsword", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(63), "dex", ExpressionCodec.of(63)), Map.of(), List.of());

        // TWO_HAND_AXE (weapon/main_hand/two_hand/axe)
        equip(bootstrap, TP_TWO_HAND_AXE, "splitting_greataxe", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(4), "str", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_AXE, "light_halberd", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(4), "str", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_AXE, "executioner_greataxe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(10), "str", ExpressionCodec.of(17), "dex", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_AXE, "arched_greataxe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(25), "dex", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_AXE, "elegant_glaive", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(20), "str", ExpressionCodec.of(30), "dex", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_AXE, "savage_greataxe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(27), "str", ExpressionCodec.of(39), "dex", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_AXE, "rending_halberd", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(47), "dex", ExpressionCodec.of(20)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_AXE, "jagged_greataxe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(56), "dex", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_AXE, "reaver_glaive", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(63), "dex", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_AXE, "ember_greataxe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(50), "str", ExpressionCodec.of(70), "dex", ExpressionCodec.of(28)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_AXE, "ceremonial_halberd", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(72), "dex", ExpressionCodec.of(29)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_AXE, "monument_greataxe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(57), "str", ExpressionCodec.of(79), "dex", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_AXE, "vile_greataxe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(89), "dex", ExpressionCodec.of(36)), Map.of(), List.of());

        // TWO_HAND_MACE (weapon/main_hand/two_hand/mace)
        equip(bootstrap, TP_TWO_HAND_MACE, "felled_greatclub", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(4), "str", ExpressionCodec.of(11)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "oak_greathammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(4), "str", ExpressionCodec.of(11)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "forge_maul", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "studded_greatclub", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "cultist_greathammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(22), "str", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "temple_maul", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(28), "str", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "leaden_greathammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "crumbling_maul", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "pointed_maul", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "totemic_greatclub", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(50), "str", ExpressionCodec.of(89)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "greatmace", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(92)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "precise_greathammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(96)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "giant_maul", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(114)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "snakewood_greathammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "blacksmith_maul", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "zealot_greathammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "solemn_maul", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(96)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "heavy_greathammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(104)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "disintegrating_maul", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(109)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "runeforged_felled_greatclub", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "runemastered_felled_greatclub", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(97)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "runeforged_oak_greathammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "runemastered_oak_greathammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(97)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "runeforged_forge_maul", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "runeforged_studded_greatclub", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "runemastered_studded_greatclub", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(97)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "runeforged_cultist_greathammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "runeforged_temple_maul", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(76), "dex", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "runeforged_leaden_greathammer", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(60), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "runeforged_crumbling_maul", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(97)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "runeforged_pointed_maul", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(114)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "anvil_maul", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(67), "str", ExpressionCodec.of(134)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "sacred_maul", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(72), "str", ExpressionCodec.of(149)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "ironwood_greathammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(77), "str", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "fanatic_greathammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(78), "str", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "ruination_maul", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(79), "str", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "massive_greathammer", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(77), "str", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "tawhoan_greatclub", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(78), "str", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "aberrant_sledge", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(163)), Map.of(), List.of());
        equip(bootstrap, TP_TWO_HAND_MACE, "runemastered_aberrant_sledge", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(163)), Map.of(), List.of());

        // QUARTERSTAFF (weapon/main_hand/two_hand/quarterstaff)
        equip(bootstrap, TP_QUARTERSTAFF, "wrapped_quarterstaff", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(4), "dex", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "long_quarterstaff", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(4), "dex", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "gothic_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(18), "int", ExpressionCodec.of(9)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "crackling_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(25), "int", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "crescent_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(20), "dex", ExpressionCodec.of(30), "int", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "steelpoint_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(28), "dex", ExpressionCodec.of(41), "int", ExpressionCodec.of(18)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "slicing_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(47), "int", ExpressionCodec.of(20)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "barrier_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(37), "dex", ExpressionCodec.of(52), "int", ExpressionCodec.of(22)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "hefty_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(63), "int", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "smooth_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(49), "dex", ExpressionCodec.of(68), "int", ExpressionCodec.of(28)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "anima_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "dex", ExpressionCodec.of(72), "int", ExpressionCodec.of(29)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "graceful_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(56), "dex", ExpressionCodec.of(77), "int", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "wyrm_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(89), "int", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "reaching_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(63), "int", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "barbarous_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(48), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "arcing_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(71), "int", ExpressionCodec.of(29)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "waxing_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(75), "int", ExpressionCodec.of(30)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "bladed_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(81), "int", ExpressionCodec.of(33)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "guardian_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(62), "dex", ExpressionCodec.of(85), "int", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "runeforged_wrapped_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(54), "int", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "runeforged_long_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(54), "int", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "runemastered_long_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(76), "int", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "runeforged_gothic_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(54), "int", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "runemastered_gothic_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(76), "int", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "runeforged_crescent_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "dex", ExpressionCodec.of(56), "int", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "runeforged_steelpoint_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "dex", ExpressionCodec.of(56), "int", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "sinister_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(67), "dex", ExpressionCodec.of(104), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "lunar_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(72), "dex", ExpressionCodec.of(115), "int", ExpressionCodec.of(46)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "striking_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(77), "dex", ExpressionCodec.of(127), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "bolting_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(78), "dex", ExpressionCodec.of(127), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "aegis_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(79), "dex", ExpressionCodec.of(127), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "razor_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(77), "dex", ExpressionCodec.of(127), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "skullcrusher_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(127), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "dreaming_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(78), "dex", ExpressionCodec.of(127), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "warding_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(127), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_QUARTERSTAFF, "runemastered_warding_quarterstaff", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(127), "int", ExpressionCodec.of(50)), Map.of(), List.of());

    }

    private static void registerReads2(BootstrapContext<ItemInstance> bootstrap) {
        // CROSSBOW (weapon/main_hand/two_hand/cross_bow)
        equip(bootstrap, TP_CROSS_BOW, "makeshift_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(4), "str", ExpressionCodec.of(8), "dex", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "tense_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(4), "str", ExpressionCodec.of(8), "dex", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "sturdy_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(10), "str", ExpressionCodec.of(13), "dex", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "varnished_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(19), "dex", ExpressionCodec.of(19)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "dyad_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(20), "str", ExpressionCodec.of(22), "dex", ExpressionCodec.of(22)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "alloy_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "str", ExpressionCodec.of(28), "dex", ExpressionCodec.of(28)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "bombard_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(34), "dex", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "construct_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(39), "dex", ExpressionCodec.of(39)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "blackfire_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(45), "dex", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "piercing_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(50), "str", ExpressionCodec.of(49), "dex", ExpressionCodec.of(49)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "cumbrous_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(51), "dex", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "dedalian_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(56), "str", ExpressionCodec.of(55), "dex", ExpressionCodec.of(55)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "esoteric_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(63), "dex", ExpressionCodec.of(63)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "taut_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(45), "dex", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "robust_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(48), "dex", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "painted_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(50), "dex", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "twin_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(53), "dex", ExpressionCodec.of(53)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "cannonade_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(58), "dex", ExpressionCodec.of(58)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "bleak_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(60), "dex", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "runeforged_makeshift_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(39), "dex", ExpressionCodec.of(39)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "runeforged_tense_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(39), "dex", ExpressionCodec.of(39)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "runemastered_tense_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(54), "dex", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "runeforged_sturdy_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(39), "dex", ExpressionCodec.of(39)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "runeforged_dyad_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(40), "dex", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "runemastered_dyad_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(63), "dex", ExpressionCodec.of(63)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "runeforged_bombard_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(54), "dex", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "stout_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(67), "str", ExpressionCodec.of(74), "dex", ExpressionCodec.of(74)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "engraved_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(72), "str", ExpressionCodec.of(82), "dex", ExpressionCodec.of(82)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "flexed_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(77), "str", ExpressionCodec.of(89), "dex", ExpressionCodec.of(89)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "gemini_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(78), "str", ExpressionCodec.of(89), "dex", ExpressionCodec.of(89)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "siege_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(79), "str", ExpressionCodec.of(89), "dex", ExpressionCodec.of(89)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "desolate_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(77), "str", ExpressionCodec.of(89), "dex", ExpressionCodec.of(89)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "elegant_crossbow", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(78), "str", ExpressionCodec.of(89), "dex", ExpressionCodec.of(89)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "trarthan_cannon", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(114), "dex", ExpressionCodec.of(63)), Map.of(), List.of());
        equip(bootstrap, TP_CROSS_BOW, "ancient_rune_of_detonation", SIG_NONE, Map.of("l", ExpressionCodec.of(30)), Map.of(), List.of());

        // TRAP (weapon/main_hand/two_hand/trap)
        equip(bootstrap, TP_TRAP, "clay_trap", SIG_NONE, Map.of("l", ExpressionCodec.of(2)), Map.of(), List.of());
        equip(bootstrap, TP_TRAP, "clamping_trap", SIG_NONE, Map.of("l", ExpressionCodec.of(6)), Map.of(), List.of());
        equip(bootstrap, TP_TRAP, "shrapnel_trap", SIG_NONE, Map.of("l", ExpressionCodec.of(10)), Map.of(), List.of());
        equip(bootstrap, TP_TRAP, "urn_trap", SIG_NONE, Map.of("l", ExpressionCodec.of(16)), Map.of(), List.of());
        equip(bootstrap, TP_TRAP, "incense_trap", SIG_NONE, Map.of("l", ExpressionCodec.of(22)), Map.of(), List.of());
        equip(bootstrap, TP_TRAP, "bladed_trap", SIG_NONE, Map.of("l", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_TRAP, "dart_trap", SIG_NONE, Map.of("l", ExpressionCodec.of(33)), Map.of(), List.of());
        equip(bootstrap, TP_TRAP, "spike_trap", SIG_NONE, Map.of("l", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_TRAP, "coiled_trap", SIG_NONE, Map.of("l", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_TRAP, "lead_trap", SIG_NONE, Map.of("l", ExpressionCodec.of(49)), Map.of(), List.of());
        equip(bootstrap, TP_TRAP, "medallion_trap", SIG_NONE, Map.of("l", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_TRAP, "intricate_trap", SIG_NONE, Map.of("l", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_TRAP, "refined_trap", SIG_NONE, Map.of("l", ExpressionCodec.of(65)), Map.of(), List.of());

        // TALISMAN (weapon/main_hand/two_hand/talisman)
        equip(bootstrap, TP_TALISMAN, "changeling_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "str", ExpressionCodec.of(9), "int", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "nettle_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "str", ExpressionCodec.of(9), "int", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "cinderbark_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "str", ExpressionCodec.of(15), "int", ExpressionCodec.of(11)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "familial_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(21), "int", ExpressionCodec.of(16)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "frenzied_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(22), "str", ExpressionCodec.of(28), "int", ExpressionCodec.of(20)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "primal_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(28), "str", ExpressionCodec.of(34), "int", ExpressionCodec.of(25)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "rabid_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(34), "str", ExpressionCodec.of(40), "int", ExpressionCodec.of(29)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "vicious_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(47), "int", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "voltfang_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(46), "str", ExpressionCodec.of(53), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "lumbering_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(60), "int", ExpressionCodec.of(43)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "roaring_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(58), "str", ExpressionCodec.of(66), "int", ExpressionCodec.of(47)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "wingbeat_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(74), "int", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "condemned_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(74), "int", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "howling_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(63), "int", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "fury_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "cruel_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(63), "str", ExpressionCodec.of(72), "int", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "runeforged_changeling_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(45), "int", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "runeforged_nettle_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(45), "int", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "runemastered_nettle_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(63), "int", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "runeforged_familial_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(45), "int", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "runemastered_familial_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(63), "int", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "runeforged_vicious_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(74), "int", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "spiny_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(67), "str", ExpressionCodec.of(86), "int", ExpressionCodec.of(65)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "ashbark_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(72), "str", ExpressionCodec.of(94), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "fang_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(77), "str", ExpressionCodec.of(89), "int", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "fungal_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(78), "str", ExpressionCodec.of(96), "int", ExpressionCodec.of(70)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "alpha_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(98), "int", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "maji_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(79), "str", ExpressionCodec.of(100), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "wildwood_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(98), "int", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "thunder_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(77), "str", ExpressionCodec.of(102), "int", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_TALISMAN, "jade_talisman", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(78), "str", ExpressionCodec.of(109), "int", ExpressionCodec.of(65)), Map.of(), List.of());

        // QUIVER (weapon/off_hand/quiver)
        equip(bootstrap, TP_QUIVER, "broadhead_quiver", SIG_NONE, Map.of("l", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_QUIVER, "fire_quiver", SIG_NONE, Map.of("l", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_QUIVER, "sacral_quiver", SIG_NONE, Map.of("l", ExpressionCodec.of(16)), Map.of(), List.of());
        equip(bootstrap, TP_QUIVER, "two-point_quiver", SIG_NONE, Map.of("l", ExpressionCodec.of(24)), Map.of(), List.of());
        equip(bootstrap, TP_QUIVER, "blunt_quiver", SIG_NONE, Map.of("l", ExpressionCodec.of(33)), Map.of(), List.of());
        equip(bootstrap, TP_QUIVER, "toxic_quiver", SIG_NONE, Map.of("l", ExpressionCodec.of(39)), Map.of(), List.of());
        equip(bootstrap, TP_QUIVER, "serrated_quiver", SIG_NONE, Map.of("l", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_QUIVER, "primed_quiver", SIG_NONE, Map.of("l", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_QUIVER, "penetrating_quiver", SIG_NONE, Map.of("l", ExpressionCodec.of(55)), Map.of(), List.of());
        equip(bootstrap, TP_QUIVER, "volant_quiver", SIG_NONE, Map.of("l", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_QUIVER, "visceral_quiver", SIG_NONE, Map.of("l", ExpressionCodec.of(65)), Map.of(), List.of());

        // SHIELD (weapon/off_hand/shield)
        equip(bootstrap, TP_SHIELD, "splintered_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(11)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "painted_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(11)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "braced_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(12), "str", ExpressionCodec.of(19)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "barricade_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(25)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "effigial_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(21), "str", ExpressionCodec.of(32)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "rampart_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(28), "str", ExpressionCodec.of(42)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "heraldric_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(48)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "stone_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(36), "str", ExpressionCodec.of(52)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "crucible_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(64)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "ancestor_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(50), "str", ExpressionCodec.of(71)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "phalanx_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(74)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "defiant_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(58), "str", ExpressionCodec.of(82)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "blacksteel_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(91)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "aged_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(64)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "metalworked_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(69)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "cultist_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(73)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "bulwark_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(77)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "noble_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(83)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "goldworked_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(61), "str", ExpressionCodec.of(86)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "royal_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(91)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "fortress_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(99)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "vaal_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(107)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "tawhoan_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(115)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "glacial_fortress", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(99)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_splintered_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(11)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_painted_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(11)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_braced_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(12), "str", ExpressionCodec.of(19)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_barricade_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(25)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_effigial_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(21), "str", ExpressionCodec.of(32)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_rampart_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(28), "str", ExpressionCodec.of(42)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_heraldric_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(48)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_stone_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(36), "str", ExpressionCodec.of(52)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_crucible_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(64)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_bulwark_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(77)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_noble_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(83)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_goldworked_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(61), "str", ExpressionCodec.of(86)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_royal_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(91)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_fortress_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(99)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_vaal_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(107)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_tawhoan_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(115)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_splintered_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(54)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_painted_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(54)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_braced_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(54)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_barricade_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(54)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_rampart_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(58)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_heraldric_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(50), "str", ExpressionCodec.of(71)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_crucible_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(91)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_blacksteel_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(91)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_vaal_tower_shield", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(107)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_glacial_fortress", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(99)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_venerable_defender", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(107)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "hardwood_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(8), "str", ExpressionCodec.of(9), "dex", ExpressionCodec.of(9)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "pelage_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(8), "str", ExpressionCodec.of(9), "dex", ExpressionCodec.of(9)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "studded_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(15), "dex", ExpressionCodec.of(15)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "crescent_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "str", ExpressionCodec.of(22), "dex", ExpressionCodec.of(22)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "chiseled_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(27), "dex", ExpressionCodec.of(27)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "feathered_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(37), "str", ExpressionCodec.of(30), "dex", ExpressionCodec.of(30)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "stratified_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(46), "str", ExpressionCodec.of(37), "dex", ExpressionCodec.of(37)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "carved_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(40), "dex", ExpressionCodec.of(40)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "mosaic_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(41), "dex", ExpressionCodec.of(41)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "aureate_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(57), "str", ExpressionCodec.of(45), "dex", ExpressionCodec.of(45)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "grand_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "dex", ExpressionCodec.of(50)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "ironwood_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(36), "dex", ExpressionCodec.of(36)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "fur-lined_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(38), "dex", ExpressionCodec.of(38)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "mercenary_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(40), "dex", ExpressionCodec.of(40)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "polished_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(42), "dex", ExpressionCodec.of(42)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "stone_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(46), "dex", ExpressionCodec.of(46)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "avian_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(48), "dex", ExpressionCodec.of(48)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "mammoth_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "dex", ExpressionCodec.of(50)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "baroque_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(54), "dex", ExpressionCodec.of(54)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "soaring_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(59), "dex", ExpressionCodec.of(59)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "golden_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(63), "dex", ExpressionCodec.of(63)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "venerable_defender", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(64), "dex", ExpressionCodec.of(64)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_hardwood_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(8), "str", ExpressionCodec.of(9), "dex", ExpressionCodec.of(9)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_pelage_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(8), "str", ExpressionCodec.of(9), "dex", ExpressionCodec.of(9)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_studded_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(15), "dex", ExpressionCodec.of(15)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_crescent_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "str", ExpressionCodec.of(22), "dex", ExpressionCodec.of(22)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_chiseled_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(27), "dex", ExpressionCodec.of(27)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_feathered_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(37), "str", ExpressionCodec.of(30), "dex", ExpressionCodec.of(30)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_stratified_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(46), "str", ExpressionCodec.of(37), "dex", ExpressionCodec.of(37)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_carved_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(40), "dex", ExpressionCodec.of(40)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_polished_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(42), "dex", ExpressionCodec.of(42)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_stone_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(46), "dex", ExpressionCodec.of(46)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_avian_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(48), "dex", ExpressionCodec.of(48)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_mammoth_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "dex", ExpressionCodec.of(50)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_baroque_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(54), "dex", ExpressionCodec.of(54)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_soaring_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(59), "dex", ExpressionCodec.of(59)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_golden_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(63), "dex", ExpressionCodec.of(63)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_hardwood_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(31), "dex", ExpressionCodec.of(31)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_crescent_targe", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(31), "dex", ExpressionCodec.of(31)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "blazon_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(7), "str", ExpressionCodec.of(8), "int", ExpressionCodec.of(8)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "sigil_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(7), "str", ExpressionCodec.of(8), "int", ExpressionCodec.of(8)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "emblem_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(15), "int", ExpressionCodec.of(15)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "jingling_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(28), "str", ExpressionCodec.of(23), "int", ExpressionCodec.of(23)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "sectarian_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(27), "int", ExpressionCodec.of(27)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "omen_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(36), "str", ExpressionCodec.of(30), "int", ExpressionCodec.of(30)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "wayward_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(36), "int", ExpressionCodec.of(36)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "seer_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(50), "str", ExpressionCodec.of(39), "int", ExpressionCodec.of(39)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "stoic_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "empyreal_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(57), "str", ExpressionCodec.of(45), "int", ExpressionCodec.of(45)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "deified_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "int", ExpressionCodec.of(50)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "painted_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(36), "int", ExpressionCodec.of(36)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "engraved_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(38), "int", ExpressionCodec.of(38)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "descry_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(40), "int", ExpressionCodec.of(40)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "dekharan_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(42), "int", ExpressionCodec.of(42)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "quartered_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(46), "int", ExpressionCodec.of(46)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "glowering_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(48), "int", ExpressionCodec.of(48)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "intricate_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "int", ExpressionCodec.of(50)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "sekheman_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(54), "int", ExpressionCodec.of(54)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "vaal_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(59), "int", ExpressionCodec.of(59)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "blacksteel_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(63), "int", ExpressionCodec.of(63)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_blazon_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(7), "str", ExpressionCodec.of(8), "int", ExpressionCodec.of(8)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_sigil_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(7), "str", ExpressionCodec.of(8), "int", ExpressionCodec.of(8)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_emblem_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(15), "int", ExpressionCodec.of(15)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_jingling_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(28), "str", ExpressionCodec.of(23), "int", ExpressionCodec.of(23)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_sectarian_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(27), "int", ExpressionCodec.of(27)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_omen_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(36), "str", ExpressionCodec.of(30), "int", ExpressionCodec.of(30)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_wayward_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(36), "int", ExpressionCodec.of(36)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_seer_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(50), "str", ExpressionCodec.of(39), "int", ExpressionCodec.of(39)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_dekharan_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(42), "int", ExpressionCodec.of(42)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_quartered_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(46), "int", ExpressionCodec.of(46)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_glowering_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(48), "int", ExpressionCodec.of(48)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_intricate_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "int", ExpressionCodec.of(50)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_sekheman_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(54), "int", ExpressionCodec.of(54)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_vaal_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(59), "int", ExpressionCodec.of(59)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runeforged_blacksteel_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(63), "int", ExpressionCodec.of(63)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_blazon_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(32), "int", ExpressionCodec.of(32)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_sigil_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(32), "int", ExpressionCodec.of(32)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_emblem_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(31), "int", ExpressionCodec.of(31)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_jingling_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(31), "int", ExpressionCodec.of(31)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_omen_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(38), "int", ExpressionCodec.of(38)), Map.of(), List.of(id("preset_raise_shield")));
        equip(bootstrap, TP_SHIELD, "runemastered_intricate_crest_shield", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "int", ExpressionCodec.of(50)), Map.of(), List.of(id("preset_raise_shield")));

        // BUCKLER (weapon/off_hand/buckler)
        equip(bootstrap, TP_BUCKLER, "leather_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(5), "dex", ExpressionCodec.of(10)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "wooden_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(5), "dex", ExpressionCodec.of(10)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "plated_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(18)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "iron_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(25)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "ridged_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(22), "dex", ExpressionCodec.of(33)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "spiked_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "dex", ExpressionCodec.of(38)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "ringed_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(48)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "edged_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(39), "dex", ExpressionCodec.of(56)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "laminate_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(64)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "pearl_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(50), "dex", ExpressionCodec.of(71)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "ornate_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "dex", ExpressionCodec.of(74)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "array_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(78)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "aegis_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(91)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "oak_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(64)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "painted_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(48), "dex", ExpressionCodec.of(69)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "coiled_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(73)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "spikeward_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(77)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "jingling_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(83)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "bladeguard_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(61), "dex", ExpressionCodec.of(86)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "gutspike_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(99)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "ancient_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(107)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "desert_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(115)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_leather_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(5), "dex", ExpressionCodec.of(10)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_wooden_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(5), "dex", ExpressionCodec.of(10)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_plated_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(18)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_iron_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(25)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_ridged_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(22), "dex", ExpressionCodec.of(33)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_spiked_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "dex", ExpressionCodec.of(38)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_ringed_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(48)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_edged_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(39), "dex", ExpressionCodec.of(56)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_laminate_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(64)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_pearl_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(50), "dex", ExpressionCodec.of(71)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_ornate_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "dex", ExpressionCodec.of(74)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_array_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(78)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_aegis_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(91)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_spikeward_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(77)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_jingling_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(83)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_bladeguard_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(61), "dex", ExpressionCodec.of(86)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_gutspike_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(99)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_ancient_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(107)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runeforged_desert_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(115)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runemastered_leather_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(54)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runemastered_wooden_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(54)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runemastered_plated_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(54)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runemastered_iron_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(54)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runemastered_ridged_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "dex", ExpressionCodec.of(58)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runemastered_spiked_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "dex", ExpressionCodec.of(58)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runemastered_ornate_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(78)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runemastered_array_buckler", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(60), "dex", ExpressionCodec.of(85)), Map.of(), List.of(id("preset_parry")));
        equip(bootstrap, TP_BUCKLER, "runemastered_venerable_defender", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(107)), Map.of(), List.of(id("preset_raise_shield")));

        // FOCUS (weapon/off_hand/foci)
        equip(bootstrap, TP_FOCI, "twig_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(6), "int", ExpressionCodec.of(11)), Map.of("energy", ExpressionCodec.of(12)), List.of());
        equip(bootstrap, TP_FOCI, "woven_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(6), "int", ExpressionCodec.of(11)), Map.of("energy", ExpressionCodec.of(15)), List.of());
        equip(bootstrap, TP_FOCI, "antler_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "int", ExpressionCodec.of(17)), Map.of("energy", ExpressionCodec.of(18)), List.of());
        equip(bootstrap, TP_FOCI, "engraved_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "int", ExpressionCodec.of(25)), Map.of("energy", ExpressionCodec.of(22)), List.of());
        equip(bootstrap, TP_FOCI, "tonal_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(22), "int", ExpressionCodec.of(33)), Map.of("energy", ExpressionCodec.of(26)), List.of());
        equip(bootstrap, TP_FOCI, "crystal_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(26), "int", ExpressionCodec.of(38)), Map.of("energy", ExpressionCodec.of(29)), List.of());
        equip(bootstrap, TP_FOCI, "voodoo_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "int", ExpressionCodec.of(48)), Map.of("energy", ExpressionCodec.of(34)), List.of());
        equip(bootstrap, TP_FOCI, "plumed_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(36), "int", ExpressionCodec.of(52)), Map.of("energy", ExpressionCodec.of(36)), List.of());
        equip(bootstrap, TP_FOCI, "runed_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(64)), Map.of("energy", ExpressionCodec.of(42)), List.of());
        equip(bootstrap, TP_FOCI, "whorl_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "int", ExpressionCodec.of(73)), Map.of("energy", ExpressionCodec.of(47)), List.of());
        equip(bootstrap, TP_FOCI, "elegant_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "int", ExpressionCodec.of(74)), Map.of("energy", ExpressionCodec.of(47)), List.of());
        equip(bootstrap, TP_FOCI, "attuned_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(57), "int", ExpressionCodec.of(81)), Map.of("energy", ExpressionCodec.of(51)), List.of());
        equip(bootstrap, TP_FOCI, "magus_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(91)), Map.of("energy", ExpressionCodec.of(56)), List.of());
        equip(bootstrap, TP_FOCI, "wreath_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(64)), Map.of("energy", ExpressionCodec.of(42)), List.of());
        equip(bootstrap, TP_FOCI, "staghorn_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(48), "int", ExpressionCodec.of(69)), Map.of("energy", ExpressionCodec.of(45)), List.of());
        equip(bootstrap, TP_FOCI, "jingling_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "int", ExpressionCodec.of(73)), Map.of("energy", ExpressionCodec.of(47)), List.of());
        equip(bootstrap, TP_FOCI, "arrayed_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "int", ExpressionCodec.of(77)), Map.of("energy", ExpressionCodec.of(49)), List.of());
        equip(bootstrap, TP_FOCI, "cultist_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "int", ExpressionCodec.of(83)), Map.of("energy", ExpressionCodec.of(52)), List.of());
        equip(bootstrap, TP_FOCI, "hallowed_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(61), "int", ExpressionCodec.of(86)), Map.of("energy", ExpressionCodec.of(57)), List.of());
        equip(bootstrap, TP_FOCI, "druidic_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(91)), Map.of("energy", ExpressionCodec.of(62)), List.of());
        equip(bootstrap, TP_FOCI, "leyline_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "int", ExpressionCodec.of(99)), Map.of("energy", ExpressionCodec.of(71)), List.of());
        equip(bootstrap, TP_FOCI, "sacred_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "int", ExpressionCodec.of(107)), Map.of("energy", ExpressionCodec.of(81)), List.of());
        equip(bootstrap, TP_FOCI, "tasalian_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "int", ExpressionCodec.of(115)), Map.of("energy", ExpressionCodec.of(91)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_twig_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(6), "int", ExpressionCodec.of(11)), Map.of("energy", ExpressionCodec.of(12), "runic_ward", ExpressionCodec.of(12)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_woven_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(6), "int", ExpressionCodec.of(11)), Map.of("energy", ExpressionCodec.of(15), "runic_ward", ExpressionCodec.of(15)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_antler_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "int", ExpressionCodec.of(17)), Map.of("energy", ExpressionCodec.of(18), "runic_ward", ExpressionCodec.of(17)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_engraved_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "int", ExpressionCodec.of(25)), Map.of("energy", ExpressionCodec.of(22), "runic_ward", ExpressionCodec.of(22)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_tonal_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(22), "int", ExpressionCodec.of(33)), Map.of("energy", ExpressionCodec.of(26), "runic_ward", ExpressionCodec.of(24)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_crystal_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(26), "int", ExpressionCodec.of(38)), Map.of("energy", ExpressionCodec.of(29), "runic_ward", ExpressionCodec.of(25)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_voodoo_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "int", ExpressionCodec.of(48)), Map.of("energy", ExpressionCodec.of(34), "runic_ward", ExpressionCodec.of(28)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_plumed_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(36), "int", ExpressionCodec.of(52)), Map.of("energy", ExpressionCodec.of(36), "runic_ward", ExpressionCodec.of(30)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_runed_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(64)), Map.of("energy", ExpressionCodec.of(42), "runic_ward", ExpressionCodec.of(32)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_whorl_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "int", ExpressionCodec.of(73)), Map.of("energy", ExpressionCodec.of(47), "runic_ward", ExpressionCodec.of(34)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_elegant_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "int", ExpressionCodec.of(74)), Map.of("runic_ward", ExpressionCodec.of(146)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_attuned_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(57), "int", ExpressionCodec.of(81)), Map.of("runic_ward", ExpressionCodec.of(157)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_magus_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(91)), Map.of("runic_ward", ExpressionCodec.of(176)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_arrayed_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "int", ExpressionCodec.of(77)), Map.of("energy", ExpressionCodec.of(43), "runic_ward", ExpressionCodec.of(45)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_cultist_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "int", ExpressionCodec.of(83)), Map.of("energy", ExpressionCodec.of(50), "runic_ward", ExpressionCodec.of(32)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_hallowed_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(61), "int", ExpressionCodec.of(86)), Map.of("energy", ExpressionCodec.of(43), "runic_ward", ExpressionCodec.of(58)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_druidic_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(91)), Map.of("energy", ExpressionCodec.of(55), "runic_ward", ExpressionCodec.of(62)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_leyline_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "int", ExpressionCodec.of(99)), Map.of("energy", ExpressionCodec.of(47), "runic_ward", ExpressionCodec.of(114)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_sacred_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "int", ExpressionCodec.of(107)), Map.of("energy", ExpressionCodec.of(68), "runic_ward", ExpressionCodec.of(53)), List.of());
        equip(bootstrap, TP_FOCI, "runeforged_tasalian_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "int", ExpressionCodec.of(115)), Map.of("energy", ExpressionCodec.of(36), "runic_ward", ExpressionCodec.of(181)), List.of());
        equip(bootstrap, TP_FOCI, "runemastered_twig_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(54)), Map.of("energy", ExpressionCodec.of(38), "runic_ward", ExpressionCodec.of(23)), List.of());
        equip(bootstrap, TP_FOCI, "runemastered_woven_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(54)), Map.of("energy", ExpressionCodec.of(23), "runic_ward", ExpressionCodec.of(45)), List.of());
        equip(bootstrap, TP_FOCI, "runemastered_antler_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(54)), Map.of("energy", ExpressionCodec.of(23), "runic_ward", ExpressionCodec.of(34)), List.of());
        equip(bootstrap, TP_FOCI, "runemastered_engraved_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(54)), Map.of("energy", ExpressionCodec.of(38), "runic_ward", ExpressionCodec.of(45)), List.of());
        equip(bootstrap, TP_FOCI, "runemastered_tonal_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(54)), Map.of("energy", ExpressionCodec.of(19), "runic_ward", ExpressionCodec.of(57)), List.of());
        equip(bootstrap, TP_FOCI, "runemastered_crystal_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "int", ExpressionCodec.of(58)), Map.of("energy", ExpressionCodec.of(23), "runic_ward", ExpressionCodec.of(47)), List.of());
        equip(bootstrap, TP_FOCI, "runemastered_voodoo_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "int", ExpressionCodec.of(78)), Map.of("energy", ExpressionCodec.of(40), "runic_ward", ExpressionCodec.of(31)), List.of());
        equip(bootstrap, TP_FOCI, "runemastered_plumed_focus", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "int", ExpressionCodec.of(91)), Map.of("energy", ExpressionCodec.of(23), "runic_ward", ExpressionCodec.of(106)), List.of());

    }

    private static void registerReads3(BootstrapContext<ItemInstance> bootstrap) {
        // BODY_ARMOUR (armor/chest)
        equip(bootstrap, TP_CHEST, "rusted_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(4), "str", ExpressionCodec.of(10)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "fur_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(4), "str", ExpressionCodec.of(10)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "iron_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(21)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "raider_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(28)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "maraketh_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(20), "str", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "steel_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(27), "str", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "full_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "vaal_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(37), "str", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "juggernaut_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "chieftain_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(50), "str", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "colosseum_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(83)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "champion_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(58), "str", ExpressionCodec.of(92)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "glorious_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "conqueror_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(68), "str", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "abyssal_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(73), "str", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_grasping_mail", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(112)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "barbarian_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "rugged_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(77)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "sandsworn_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(82)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "elegant_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "heavy_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(94)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "stone_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(98)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "soldier_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "ornate_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "utzaal_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "warlord_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_rusted_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(4), "str", ExpressionCodec.of(10)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_fur_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(4), "str", ExpressionCodec.of(10)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_iron_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(21)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_raider_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(28)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_maraketh_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(20), "str", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_steel_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(27), "str", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_full_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_vaal_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(37), "str", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_juggernaut_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_chieftain_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(50), "str", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_conqueror_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(68), "str", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_elegant_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_heavy_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(94)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_stone_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(98)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_soldier_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_ornate_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_utzaal_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_warlord_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_rusted_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_iron_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_raider_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_maraketh_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_steel_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(65)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_full_plate", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_vaal_cuirass", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(87)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "leather_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(4), "dex", ExpressionCodec.of(10)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "quilted_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(4), "dex", ExpressionCodec.of(10)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "pathfinder_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(21)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "shrouded_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(28)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "rhoahide_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(22), "dex", ExpressionCodec.of(37)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "studded_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "dex", ExpressionCodec.of(43)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "scouts_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "serpentscale_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(36), "dex", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "corsair_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "smuggler_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(82)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "strider_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "dex", ExpressionCodec.of(83)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "hardleather_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(56), "dex", ExpressionCodec.of(89)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "exquisite_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "mail_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(68), "dex", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "armoured_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(73), "dex", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "patchwork_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "hunting_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(48), "dex", ExpressionCodec.of(77)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "riding_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(82)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "layered_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runner_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(94)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "lizardscale_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(62), "dex", ExpressionCodec.of(98)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "swiftstalker_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "slipstrike_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "wyrmscale_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "corsair_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_leather_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(4), "dex", ExpressionCodec.of(10)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_quilted_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(4), "dex", ExpressionCodec.of(10)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_pathfinder_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(21)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_shrouded_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(28)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_rhoahide_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(22), "dex", ExpressionCodec.of(37)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_studded_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "dex", ExpressionCodec.of(43)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_scouts_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_serpentscale_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(36), "dex", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_corsair_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_smuggler_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(82)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_layered_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_runner_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(94)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_lizardscale_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(62), "dex", ExpressionCodec.of(98)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_swiftstalker_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_slipstrike_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_wyrmscale_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_corsair_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_leather_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_quilted_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_pathfinder_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_shrouded_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_rhoahide_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_studded_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "dex", ExpressionCodec.of(65)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_scouts_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(87)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_serpentscale_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(87)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_smuggler_coat", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(103)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_strider_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(103)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_exquisite_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_armoured_vest", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "tattered_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "int", ExpressionCodec.of(11)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "feathered_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "int", ExpressionCodec.of(11)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "hexers_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(11), "int", ExpressionCodec.of(21)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "bone_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "int", ExpressionCodec.of(28)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "silk_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(22), "int", ExpressionCodec.of(37)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "keth_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(28), "int", ExpressionCodec.of(47)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "votive_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "int", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "altar_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "int", ExpressionCodec.of(65)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "elementalist_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "mystic_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(49), "int", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "imperial_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "int", ExpressionCodec.of(83)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "plated_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(58), "int", ExpressionCodec.of(92)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "havoc_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "enlightened_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(68), "int", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "arcane_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(73), "int", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "avian_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "cursespeakers_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(48), "int", ExpressionCodec.of(77)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "luxurious_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "int", ExpressionCodec.of(82)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "river_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "int", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "adherents_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "int", ExpressionCodec.of(94)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "ceremonial_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(62), "int", ExpressionCodec.of(98)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "vile_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "flowing_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "int", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "sacramental_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "int", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "feathered_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "int", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_tattered_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "int", ExpressionCodec.of(11)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_feathered_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "int", ExpressionCodec.of(11)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_hexers_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(11), "int", ExpressionCodec.of(21)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_bone_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "int", ExpressionCodec.of(28)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_silk_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(22), "int", ExpressionCodec.of(37)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_keth_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(28), "int", ExpressionCodec.of(47)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_votive_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "int", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_altar_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "int", ExpressionCodec.of(65)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_elementalist_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_mystic_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(49), "int", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_river_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "int", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_adherents_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "int", ExpressionCodec.of(94)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_ceremonial_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(62), "int", ExpressionCodec.of(98)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_vile_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_flowing_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "int", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_sacramental_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "int", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_feathered_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "int", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_tattered_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_feathered_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_hexers_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_bone_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_silk_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_keth_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_votive_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "int", ExpressionCodec.of(87)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_altar_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "int", ExpressionCodec.of(87)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_elementalist_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "int", ExpressionCodec.of(87)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_plated_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(103)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_havoc_raiment", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "int", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_enlightened_robe", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "int", ExpressionCodec.of(121)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "chain_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(13), "dex", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "rogue_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(13), "dex", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "vagabond_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(17), "dex", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "cloaked_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "str", ExpressionCodec.of(25), "dex", ExpressionCodec.of(25)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "explorer_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(31), "dex", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "scale_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(37), "str", ExpressionCodec.of(34), "dex", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "knight_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(41), "dex", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "ancestral_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(50), "str", ExpressionCodec.of(44), "dex", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "lamellar_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(46), "dex", ExpressionCodec.of(46)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "gladiator_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(58), "str", ExpressionCodec.of(51), "dex", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "heroic_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(67), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "tournament_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(68), "str", ExpressionCodec.of(67), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "slayer_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(73), "str", ExpressionCodec.of(67), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_grasping_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(61), "dex", ExpressionCodec.of(61)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "ring_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(41), "dex", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "scoundrel_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(43), "dex", ExpressionCodec.of(43)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "wanderer_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(45), "dex", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "mantled_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(48), "dex", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "trailblazer_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(52), "dex", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "golden_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(54), "dex", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "dastard_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(67), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "shrouded_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(67), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "death_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(67), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "thane_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(67), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_chain_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(13), "dex", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_rogue_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(13), "dex", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_vagabond_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(17), "dex", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_cloaked_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "str", ExpressionCodec.of(25), "dex", ExpressionCodec.of(25)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_explorer_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(31), "dex", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_scale_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(37), "str", ExpressionCodec.of(34), "dex", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_knight_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(41), "dex", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_ancestral_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(50), "str", ExpressionCodec.of(44), "dex", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_slayer_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(73), "str", ExpressionCodec.of(67), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_sacrificial_regalia", SIG_STRENGTH_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(72), "dex", ExpressionCodec.of(72), "int", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_mantled_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(48), "dex", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_trailblazer_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(52), "dex", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_golden_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(54), "dex", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_dastard_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(67), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_shrouded_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(67), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_death_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(67), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_thane_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(67), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_chain_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(35), "dex", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_rogue_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(35), "dex", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_vagabond_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(35), "dex", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_cloaked_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(36), "dex", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_explorer_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(49), "dex", ExpressionCodec.of(49)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_scale_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(49), "dex", ExpressionCodec.of(49)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_knight_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(49), "dex", ExpressionCodec.of(49)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_ancestral_mail", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(57), "dex", ExpressionCodec.of(57)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_heroic_armour", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(67), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "pilgrim_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "str", ExpressionCodec.of(12), "int", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "pelt_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "str", ExpressionCodec.of(12), "int", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "mail_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(17), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "shaman_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(28), "str", ExpressionCodec.of(26), "int", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "ironclad_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(31), "int", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "sacrificial_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(36), "str", ExpressionCodec.of(33), "int", ExpressionCodec.of(33)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "cleric_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "tideseer_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(45), "int", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "gilded_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(46), "int", ExpressionCodec.of(46)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "venerated_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(48), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "revered_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "corvus_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(68), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "zenith_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(73), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "ornate_ringmail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(43), "str", ExpressionCodec.of(45), "int", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "ancient_mail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "templar_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "bearskin_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(43), "int", ExpressionCodec.of(43)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "chain_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(45), "int", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "occultist_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(48), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "plated_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(52), "int", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "heartcarver_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(54), "int", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "wolfskin_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "conjurer_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "death_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "seastorm_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_pilgrim_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "str", ExpressionCodec.of(12), "int", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_pelt_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "str", ExpressionCodec.of(12), "int", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_mail_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(17), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_shaman_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(28), "str", ExpressionCodec.of(26), "int", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_ironclad_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(31), "int", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_sacrificial_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(36), "str", ExpressionCodec.of(33), "int", ExpressionCodec.of(33)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_cleric_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_tideseer_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(45), "int", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_occultist_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(48), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_plated_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(52), "int", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_heartcarver_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(62), "str", ExpressionCodec.of(54), "int", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_wolfskin_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_conjurer_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_death_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_seastorm_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_pilgrim_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(35), "int", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_mail_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(35), "int", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_shaman_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(35), "int", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_ironclad_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(49), "int", ExpressionCodec.of(49)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_sacrificial_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(49), "int", ExpressionCodec.of(49)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_cleric_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(49), "int", ExpressionCodec.of(49)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_tideseer_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(57), "int", ExpressionCodec.of(57)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_gilded_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(57), "int", ExpressionCodec.of(57)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_revered_vestments", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_corvus_mantle", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_ornate_ringmail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(57), "int", ExpressionCodec.of(57)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_ancient_mail", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "hermit_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(13), "int", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "waxed_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(13), "int", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "marabout_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(17), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "wayfarer_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(28), "dex", ExpressionCodec.of(26), "int", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "anchorite_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(31), "int", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "scalpers_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(39), "dex", ExpressionCodec.of(35), "int", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "scoundrel_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "ascetic_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(45), "int", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "clandestine_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "dex", ExpressionCodec.of(46), "int", ExpressionCodec.of(46)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "monastic_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(56), "dex", ExpressionCodec.of(50), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "torment_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "devout_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(68), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "assassin_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(73), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "oiled_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(48), "dex", ExpressionCodec.of(43), "int", ExpressionCodec.of(43)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "evangelist_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(45), "int", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "itinerant_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(48), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "hatungo_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(52), "int", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "hawkers_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(62), "dex", ExpressionCodec.of(54), "int", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "sleek_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "rambler_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "falconers_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "austere_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "primal_markings", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_hermit_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(13), "int", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_waxed_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(13), "int", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_marabout_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(17), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_wayfarer_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(28), "dex", ExpressionCodec.of(26), "int", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_anchorite_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(31), "int", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_scalpers_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(39), "dex", ExpressionCodec.of(35), "int", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_scoundrel_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_ascetic_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(45), "int", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_itinerant_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(48), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_hatungo_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(52), "int", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_hawkers_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(62), "dex", ExpressionCodec.of(54), "int", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_sleek_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_rambler_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_falconers_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_austere_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_primal_markings", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_hermit_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(35), "int", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_waxed_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(35), "int", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_marabout_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(35), "int", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_wayfarer_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "dex", ExpressionCodec.of(36), "int", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_anchorite_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(49), "int", ExpressionCodec.of(49)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_scalpers_jacket", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(49), "int", ExpressionCodec.of(49)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_assassin_garb", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(73), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_primal_markings", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(67), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "golden_mantle", SIG_STRENGTH_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(20), "str", ExpressionCodec.of(7), "dex", ExpressionCodec.of(7), "int", ExpressionCodec.of(7)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "grand_regalia", SIG_STRENGTH_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(41), "dex", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "sacrificial_regalia", SIG_STRENGTH_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(72), "dex", ExpressionCodec.of(72), "int", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "grasping_mail", SIG_STRENGTH_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(41), "dex", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runeforged_grand_regalia", SIG_STRENGTH_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(41), "dex", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_grand_regalia", SIG_STRENGTH_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(41), "dex", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_CHEST, "runemastered_sacrificial_regalia", SIG_STRENGTH_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(72), "dex", ExpressionCodec.of(72), "int", ExpressionCodec.of(72)), Map.of(), List.of());

    }

    private static void registerReads4(BootstrapContext<ItemInstance> bootstrap) {
        // BOOTS (armor/feet)
        equip(bootstrap, TP_FEET, "rough_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "iron_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "bronze_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "trimmed_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(27), "str", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "stone_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "reefsteel_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "monument_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(69)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "totemic_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "plated_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "lionheart_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "elegant_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "carved_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "bulwark_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "ornate_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(93)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "vaal_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(101)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "tasalian_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(108)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_rough_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_iron_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_bronze_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_trimmed_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(27), "str", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_stone_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_reefsteel_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_elegant_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_carved_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_bulwark_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_ornate_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(93)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_vaal_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(101)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_tasalian_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(108)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_rough_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_iron_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_bronze_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_trimmed_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_stone_greaves", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(73)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "rawhide_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "laced_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "embossed_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "steeltoe_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(28), "dex", ExpressionCodec.of(39)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "lizardscale_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "flared_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "leatherplate_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "dex", ExpressionCodec.of(69)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "embroidered_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "bound_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "sleek_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "studded_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "serpentscale_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "cinched_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "cavalry_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(93)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "dragonscale_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(101)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "drakeskin_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(108)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_rawhide_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_laced_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_embossed_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_steeltoe_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(28), "dex", ExpressionCodec.of(39)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_lizardscale_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_flared_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_studded_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_serpentscale_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_cinched_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_cavalry_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(93)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_dragonscale_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(101)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_drakeskin_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(108)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_laced_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_embossed_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_steeltoe_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "dex", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_lizardscale_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "dex", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_cinched_boots", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "straw_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(11), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "wrapped_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(11), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "lattice_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "int", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "silk_slippers", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(27), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "feathered_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "int", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "flax_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "studded_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "int", ExpressionCodec.of(69)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "elaborate_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "laced_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "bangled_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "int", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "elegant_slippers", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "int", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "dunerunner_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "int", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "bound_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "luxurious_slippers", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "int", ExpressionCodec.of(93)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "sandsworn_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "int", ExpressionCodec.of(101)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "sekhema_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "int", ExpressionCodec.of(108)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_straw_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(11), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_wrapped_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(11), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_lattice_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "int", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_silk_slippers", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(27), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_feathered_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "int", ExpressionCodec.of(45)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_flax_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(60)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_elegant_slippers", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "int", ExpressionCodec.of(72)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_dunerunner_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "int", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_bound_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(86)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_luxurious_slippers", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "int", ExpressionCodec.of(93)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_sandsworn_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "int", ExpressionCodec.of(101)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_sekhema_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "int", ExpressionCodec.of(108)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_straw_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_wrapped_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_lattice_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_silk_slippers", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_feathered_sandals", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "int", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "mail_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(7), "dex", ExpressionCodec.of(7)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "braced_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(14), "dex", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "stacked_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(26), "dex", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "covered_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(34), "dex", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "flexile_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(38), "dex", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "bold_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(47), "dex", ExpressionCodec.of(47)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "soldiering_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(34), "dex", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "goldwork_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(38), "dex", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "bastion_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(44), "dex", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "veteran_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(47), "dex", ExpressionCodec.of(47)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "noble_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(51), "dex", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "fortress_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(56), "dex", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "blacksteel_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(59), "dex", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_mail_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(7), "dex", ExpressionCodec.of(7)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_braced_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(14), "dex", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_stacked_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(26), "dex", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_covered_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(34), "dex", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_bastion_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(44), "dex", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_veteran_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(47), "dex", ExpressionCodec.of(47)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_noble_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(51), "dex", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_fortress_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(56), "dex", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_blacksteel_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(59), "dex", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_mail_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(29), "dex", ExpressionCodec.of(29)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_braced_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(29), "dex", ExpressionCodec.of(29)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_stacked_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(30), "dex", ExpressionCodec.of(30)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_covered_sabatons", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(41), "dex", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "padded_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "str", ExpressionCodec.of(7), "int", ExpressionCodec.of(7)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "secured_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(14), "int", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "pelt_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(26), "int", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "weaver_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(34), "int", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "gilt_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(38), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "pious_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(47), "int", ExpressionCodec.of(47)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "ancient_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(56), "int", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "adherent_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(34), "int", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "bound_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(38), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "shamanistic_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(44), "int", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "faithful_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(47), "int", ExpressionCodec.of(47)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "apostle_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(51), "int", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "warlock_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(56), "int", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "cryptic_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(59), "int", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_padded_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "str", ExpressionCodec.of(7), "int", ExpressionCodec.of(7)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_secured_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(14), "int", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_pelt_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(26), "int", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_weaver_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(34), "int", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_shamanistic_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(44), "int", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_faithful_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(47), "int", ExpressionCodec.of(47)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_apostle_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(51), "int", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_warlock_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(56), "int", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_cryptic_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(59), "int", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_secured_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(29), "int", ExpressionCodec.of(29)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_ancient_leggings", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(56), "int", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "frayed_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "dex", ExpressionCodec.of(7), "int", ExpressionCodec.of(7)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "threaded_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(14), "int", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "hunting_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(26), "int", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "steelpoint_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(34), "int", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "velour_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "dex", ExpressionCodec.of(38), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "bladed_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(47), "int", ExpressionCodec.of(47)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "wayfarer_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(34), "int", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "silverbuckled_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(38), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "treerunner_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(44), "int", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "wanderer_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(47), "int", ExpressionCodec.of(47)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "charmed_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(51), "int", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "quickslip_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(56), "int", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "daggerfoot_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(59), "int", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_frayed_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "dex", ExpressionCodec.of(7), "int", ExpressionCodec.of(7)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_threaded_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(14), "int", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_hunting_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(26), "int", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_steelpoint_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(34), "int", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_treerunner_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(44), "int", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_wanderer_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(47), "int", ExpressionCodec.of(47)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_charmed_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(51), "int", ExpressionCodec.of(51)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_quickslip_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(56), "int", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runeforged_daggerfoot_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(59), "int", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_threaded_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(29), "int", ExpressionCodec.of(29)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_hunting_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "dex", ExpressionCodec.of(30), "int", ExpressionCodec.of(30)), Map.of(), List.of());
        equip(bootstrap, TP_FEET, "runemastered_velour_shoes", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(47), "int", ExpressionCodec.of(47)), Map.of(), List.of());

        // HELMET (armor/head)
        equip(bootstrap, TP_HEAD, "rusted_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(12), "str", ExpressionCodec.of(19)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "soldier_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(12), "str", ExpressionCodec.of(19)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "wrapped_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(25)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "spired_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(27), "str", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "elite_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "warrior_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(36), "str", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "commander_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "fierce_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(73)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "sentinel_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(74)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "goliath_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(57), "str", ExpressionCodec.of(81)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "guardian_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "corroded_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "mercenary_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(69)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "homeguard_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(73)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "elegant_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(77)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "noble_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(83)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "warmonger_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "masked_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(99)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "paragon_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(107)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "imperial_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(115)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_rusted_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(12), "str", ExpressionCodec.of(19)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_soldier_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(12), "str", ExpressionCodec.of(19)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_wrapped_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(25)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_spired_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(27), "str", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_elite_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_warrior_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(36), "str", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_commander_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_fierce_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(73)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_elegant_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(77)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_noble_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(83)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_warmonger_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_masked_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(99)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_paragon_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(107)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_imperial_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(115)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_rusted_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_soldier_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_wrapped_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_spired_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(58)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_elite_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_warrior_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_fierce_greathelm", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "shabby_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(10), "dex", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "felt_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(10), "dex", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "lace_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(25)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "swathed_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "dex", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "hunter_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "viper_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "corsair_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "leatherbound_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(50), "dex", ExpressionCodec.of(71)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "velvet_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "dex", ExpressionCodec.of(74)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "covert_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(56), "dex", ExpressionCodec.of(79)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "armoured_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "rotted_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "wool_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(48), "dex", ExpressionCodec.of(69)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "narrow_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(73)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "wrapped_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(77)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "deerstalker_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(83)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "woven_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "desert_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(99)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "trapper_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(107)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "freebooter_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(115)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_shabby_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(10), "dex", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_felt_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(10), "dex", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_lace_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(25)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_swathed_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "dex", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_hunter_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_viper_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_corsair_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_leatherbound_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(50), "dex", ExpressionCodec.of(71)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_wrapped_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(77)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_deerstalker_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(83)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_woven_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_desert_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(99)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_trapper_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(107)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_freebooter_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(115)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_felt_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_hunter_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "dex", ExpressionCodec.of(58)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_viper_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_corsair_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_leatherbound_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_velvet_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_covert_hood", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(56), "dex", ExpressionCodec.of(79)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_armoured_cap", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "twig_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "wicker_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "beaded_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "int", ExpressionCodec.of(25)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "chain_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(26), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "feathered_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "gold_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "int", ExpressionCodec.of(58)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "vermeil_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "jade_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(50), "int", ExpressionCodec.of(71)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "noble_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "int", ExpressionCodec.of(74)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "twilight_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(58), "int", ExpressionCodec.of(82)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "magus_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "druidic_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "avian_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(48), "int", ExpressionCodec.of(69)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "desert_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "int", ExpressionCodec.of(73)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "sandsworn_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "int", ExpressionCodec.of(77)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "jungle_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "int", ExpressionCodec.of(83)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "skycrown_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "sorcerous_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "int", ExpressionCodec.of(99)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "kamasan_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "int", ExpressionCodec.of(107)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "ancestral_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "int", ExpressionCodec.of(115)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_twig_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_wicker_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_beaded_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "int", ExpressionCodec.of(25)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_chain_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(26), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_feathered_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_gold_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "int", ExpressionCodec.of(58)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_vermeil_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_jade_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(50), "int", ExpressionCodec.of(71)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_sandsworn_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "int", ExpressionCodec.of(77)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_jungle_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "int", ExpressionCodec.of(83)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_skycrown_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_sorcerous_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "int", ExpressionCodec.of(99)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_kamasan_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "int", ExpressionCodec.of(107)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_ancestral_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "int", ExpressionCodec.of(115)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_twig_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_wicker_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_beaded_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_chain_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "int", ExpressionCodec.of(58)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_feathered_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "int", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_gold_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "int", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_vermeil_circlet", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "int", ExpressionCodec.of(78)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_jade_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_twilight_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_magus_tiara", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(91)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "brimmed_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(5), "str", ExpressionCodec.of(7), "dex", ExpressionCodec.of(7)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "guarded_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(11), "dex", ExpressionCodec.of(11)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "visored_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(15), "dex", ExpressionCodec.of(15)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "cowled_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "str", ExpressionCodec.of(22), "dex", ExpressionCodec.of(22)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "shielded_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(27), "dex", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "closed_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(36), "dex", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "decorated_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(41), "dex", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "gallant_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "dex", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "ancient_visor", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(59), "dex", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "domed_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(36), "dex", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "engraved_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(38), "dex", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "soldier_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(40), "dex", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "cabalist_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(42), "dex", ExpressionCodec.of(42)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "cassis_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(46), "dex", ExpressionCodec.of(46)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "warded_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "dex", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "cryptic_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(54), "dex", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "champion_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(59), "dex", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "gladiatorial_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(63), "dex", ExpressionCodec.of(63)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_brimmed_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(5), "str", ExpressionCodec.of(7), "dex", ExpressionCodec.of(7)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_guarded_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(11), "dex", ExpressionCodec.of(11)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_visored_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(15), "dex", ExpressionCodec.of(15)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_cowled_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(26), "str", ExpressionCodec.of(22), "dex", ExpressionCodec.of(22)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_shielded_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(27), "dex", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_closed_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(36), "dex", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_cabalist_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(42), "dex", ExpressionCodec.of(42)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_gladiatorial_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(46), "dex", ExpressionCodec.of(46)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_warded_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "dex", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_cryptic_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(54), "dex", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_champion_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(59), "dex", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_brimmed_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(31), "dex", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_guarded_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(31), "dex", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_visored_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(31), "dex", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_cowled_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(32), "dex", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_shielded_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(43), "dex", ExpressionCodec.of(43)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_decorated_helm", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "dex", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_ancient_visor", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(59), "dex", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "iron_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "str", ExpressionCodec.of(7), "int", ExpressionCodec.of(7)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "horned_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "str", ExpressionCodec.of(10), "int", ExpressionCodec.of(10)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "cultist_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(15), "int", ExpressionCodec.of(15)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "martyr_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(28), "str", ExpressionCodec.of(23), "int", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "heavy_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(27), "int", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "spiritbone_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(36), "int", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "lavish_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "archon_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "tenebrous_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(59), "int", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "mailed_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(36), "int", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "forest_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(48), "str", ExpressionCodec.of(38), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "zealot_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(40), "int", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "hallowed_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(42), "int", ExpressionCodec.of(42)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "inquisitor_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(46), "int", ExpressionCodec.of(46)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "druidic_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "saintly_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(54), "int", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "divine_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(59), "int", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "cryptic_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(63), "int", ExpressionCodec.of(63)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_iron_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "str", ExpressionCodec.of(7), "int", ExpressionCodec.of(7)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_horned_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "str", ExpressionCodec.of(10), "int", ExpressionCodec.of(10)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_cultist_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(15), "int", ExpressionCodec.of(15)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_martyr_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(28), "str", ExpressionCodec.of(23), "int", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_heavy_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(27), "int", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_spiritbone_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(36), "int", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_hallowed_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(42), "int", ExpressionCodec.of(42)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_inquisitor_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(46), "int", ExpressionCodec.of(46)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_druidic_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_saintly_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(54), "int", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_divine_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(59), "int", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_cryptic_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(63), "int", ExpressionCodec.of(63)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_iron_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(31), "int", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_horned_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(31), "int", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_cultist_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(31), "int", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_martyr_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "str", ExpressionCodec.of(32), "int", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_heavy_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(43), "int", ExpressionCodec.of(43)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_spiritbone_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(50), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_tenebrous_crown", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(59), "int", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "hewn_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "dex", ExpressionCodec.of(7), "int", ExpressionCodec.of(7)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "face_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "dex", ExpressionCodec.of(10), "int", ExpressionCodec.of(10)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "hooded_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(15), "int", ExpressionCodec.of(15)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "veiled_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(28), "dex", ExpressionCodec.of(23), "int", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "tribal_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(27), "int", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "solid_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(36), "int", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "shaded_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "dex", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "death_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(50), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "oak_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(36), "int", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "bandit_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(48), "dex", ExpressionCodec.of(38), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "skulking_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(40), "int", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "pariah_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(42), "int", ExpressionCodec.of(42)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "avian_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(46), "int", ExpressionCodec.of(46)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "brigand_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(50), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "faridun_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(54), "int", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "soaring_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(59), "int", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "grinning_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(63), "int", ExpressionCodec.of(63)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_hewn_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "dex", ExpressionCodec.of(7), "int", ExpressionCodec.of(7)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_face_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(10), "dex", ExpressionCodec.of(10), "int", ExpressionCodec.of(10)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_hooded_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(15), "int", ExpressionCodec.of(15)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_veiled_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(28), "dex", ExpressionCodec.of(23), "int", ExpressionCodec.of(23)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_tribal_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(27), "int", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_solid_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(36), "int", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_pariah_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(42), "int", ExpressionCodec.of(42)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_avian_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(46), "int", ExpressionCodec.of(46)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_brigand_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(50), "int", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_faridun_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(54), "int", ExpressionCodec.of(54)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_soaring_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(59), "int", ExpressionCodec.of(59)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runeforged_grinning_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(63), "int", ExpressionCodec.of(63)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_hewn_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(31), "int", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_face_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(31), "int", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_hooded_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(31), "int", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_veiled_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(40), "dex", ExpressionCodec.of(32), "int", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_tribal_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "dex", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_solid_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(43), "int", ExpressionCodec.of(43)), Map.of(), List.of());
        equip(bootstrap, TP_HEAD, "runemastered_death_mask", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(50), "int", ExpressionCodec.of(50)), Map.of(), List.of());

    }

    private static void registerReads5(BootstrapContext<ItemInstance> bootstrap) {
        // LEGS (armor/legs)
        equip(bootstrap, TP_LEGS, "stocky_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(16)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "riveted_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(16)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "tempered_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(22)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "bolstered_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(27), "str", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "moulded_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(42)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "detailed_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "titan_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "grand_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "plated_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "elegant_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "ancient_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "feathered_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(73)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "knightly_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "ornate_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(87)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "vaal_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(94)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "massive_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(101)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_stocky_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(16)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_riveted_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(11), "str", ExpressionCodec.of(16)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_tempered_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(22)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_bolstered_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(27), "str", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_moulded_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(42)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_detailed_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_ancient_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(54), "str", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_feathered_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(73)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_knightly_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_ornate_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(87)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_vaal_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(94)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_massive_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(101)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_stocky_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_riveted_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_tempered_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_bolstered_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_moulded_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_titan_mitts", SIG_STRENGTH, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "suede_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(16)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "firm_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(16)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "bound_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(22)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "sectioned_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(28), "dex", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "spined_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(42)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "fine_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "hardened_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "dex", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "engraved_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "hunting_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "swift_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "refined_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "spiked_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(73)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "stalking_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "grand_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(87)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "barbed_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(94)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "polished_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(101)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_suede_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(16)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_firm_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(11), "dex", ExpressionCodec.of(16)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_bound_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(22)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_sectioned_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(28), "dex", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_spined_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(42)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_fine_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_engraved_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_refined_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(54), "dex", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_spiked_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(73)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_stalking_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_grand_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(87)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_barbed_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(94)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_polished_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(101)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_suede_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_firm_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_sectioned_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_spined_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_fine_bracers", SIG_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "torn_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(12), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "sombre_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(12), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "stitched_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "int", ExpressionCodec.of(22)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "jewelled_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(26), "int", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "intricate_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "int", ExpressionCodec.of(42)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "pauascale_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "embroidered_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "int", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "adorned_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "ominous_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "embellished_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "int", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "baroque_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "gold_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "int", ExpressionCodec.of(73)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "grim_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "opulent_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "int", ExpressionCodec.of(87)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "vaal_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "int", ExpressionCodec.of(94)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "sirenscale_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "int", ExpressionCodec.of(101)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_torn_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(12), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_sombre_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(12), "int", ExpressionCodec.of(17)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_stitched_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "int", ExpressionCodec.of(22)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_jewelled_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(26), "int", ExpressionCodec.of(34)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_intricate_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "int", ExpressionCodec.of(42)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_pauascale_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "int", ExpressionCodec.of(56)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_baroque_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(54), "int", ExpressionCodec.of(67)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_gold_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "int", ExpressionCodec.of(73)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_grim_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "int", ExpressionCodec.of(80)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_opulent_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "int", ExpressionCodec.of(87)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_vaal_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "int", ExpressionCodec.of(94)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_sirenscale_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "int", ExpressionCodec.of(101)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_torn_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_sombre_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(39), "int", ExpressionCodec.of(49)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_stitched_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_jewelled_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_intricate_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "int", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_pauascale_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "int", ExpressionCodec.of(68)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_embroidered_gloves", SIG_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "int", ExpressionCodec.of(64)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "ringmail_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(6), "dex", ExpressionCodec.of(6)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "layered_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(13), "dex", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "doubled_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(24), "dex", ExpressionCodec.of(24)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "plate_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(32), "dex", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "burnished_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(36), "dex", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "ornate_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(44), "dex", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "ancient_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(52), "dex", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "ironmail_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(32), "dex", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "captain_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(35), "dex", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "zealot_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(41), "dex", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "steelmail_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(44), "dex", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "commander_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(48), "dex", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "cultist_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(52), "dex", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "blacksteel_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(55), "dex", ExpressionCodec.of(55)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_ringmail_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(6), "str", ExpressionCodec.of(6), "dex", ExpressionCodec.of(6)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_layered_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(13), "dex", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_doubled_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(24), "dex", ExpressionCodec.of(24)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_plate_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(32), "dex", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_zealot_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(41), "dex", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_steelmail_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(44), "dex", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_commander_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(48), "dex", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_cultist_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(52), "dex", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_blacksteel_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(55), "dex", ExpressionCodec.of(55)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_ringmail_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(27), "dex", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_layered_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(27), "dex", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_doubled_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(38), "dex", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_plate_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(38), "dex", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_burnished_gauntlets", SIG_STRENGTH_DEXTERITY, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(36), "dex", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "rope_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "str", ExpressionCodec.of(6), "int", ExpressionCodec.of(6)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "aged_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(13), "int", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "goldcast_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(24), "int", ExpressionCodec.of(24)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "kalguuran_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(32), "int", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "righteous_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "str", ExpressionCodec.of(36), "int", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "signet_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(44), "int", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "braided_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(32), "int", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "heirloom_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "str", ExpressionCodec.of(35), "int", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "ornate_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "bound_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(44), "int", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "ancient_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(48), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "gleaming_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(52), "int", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "adherent_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(55), "int", ExpressionCodec.of(55)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "tethering_bands", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(55), "int", ExpressionCodec.of(55)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_rope_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(5), "str", ExpressionCodec.of(6), "int", ExpressionCodec.of(6)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_aged_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "str", ExpressionCodec.of(13), "int", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_goldcast_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "str", ExpressionCodec.of(24), "int", ExpressionCodec.of(24)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_kalguuran_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "str", ExpressionCodec.of(32), "int", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_ornate_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "str", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_bound_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "str", ExpressionCodec.of(44), "int", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_ancient_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "str", ExpressionCodec.of(48), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_gleaming_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "str", ExpressionCodec.of(52), "int", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_adherent_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "str", ExpressionCodec.of(55), "int", ExpressionCodec.of(55)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_rope_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(27), "int", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_aged_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "str", ExpressionCodec.of(27), "int", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_goldcast_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(38), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_verisium_cuffs", SIG_STRENGTH_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "str", ExpressionCodec.of(38), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "gauze_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(4), "dex", ExpressionCodec.of(6), "int", ExpressionCodec.of(6)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "linen_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(13), "int", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "spiral_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(24), "int", ExpressionCodec.of(24)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "buckled_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(32), "int", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "furtive_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(52), "dex", ExpressionCodec.of(36), "int", ExpressionCodec.of(36)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "utility_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(44), "int", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "bandage_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(32), "int", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "cambric_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(51), "dex", ExpressionCodec.of(35), "int", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "adorned_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "war_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(44), "int", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "elegant_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(48), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "vaal_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(52), "int", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "secured_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(55), "int", ExpressionCodec.of(55)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_gauze_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(4), "dex", ExpressionCodec.of(6), "int", ExpressionCodec.of(6)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_linen_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(16), "dex", ExpressionCodec.of(13), "int", ExpressionCodec.of(13)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_spiral_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(33), "dex", ExpressionCodec.of(24), "int", ExpressionCodec.of(24)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_buckled_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(45), "dex", ExpressionCodec.of(32), "int", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_adorned_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(59), "dex", ExpressionCodec.of(41), "int", ExpressionCodec.of(41)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_war_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(44), "int", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_elegant_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(70), "dex", ExpressionCodec.of(48), "int", ExpressionCodec.of(48)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_vaal_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(75), "dex", ExpressionCodec.of(52), "int", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runeforged_secured_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(80), "dex", ExpressionCodec.of(55), "int", ExpressionCodec.of(55)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_linen_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(38), "dex", ExpressionCodec.of(27), "int", ExpressionCodec.of(27)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_spiral_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(38), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_furtive_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(55), "dex", ExpressionCodec.of(38), "int", ExpressionCodec.of(38)), Map.of(), List.of());
        equip(bootstrap, TP_LEGS, "runemastered_utility_wraps", SIG_DEXTERITY_INTELLIGENCE, Map.of("l", ExpressionCodec.of(65), "dex", ExpressionCodec.of(44), "int", ExpressionCodec.of(44)), Map.of(), List.of());

        // AMULET (accessory/amulet)
        equip(bootstrap, TP_AMULET, "crimson_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "azure_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "amber_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "jade_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "lapis_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "lunar_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "bloodstone_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(18)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "stellar_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(25)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "solar_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(30)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "gold_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "pearlescent_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "veridical_chain", SIG_NONE, Map.of("l", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "runemastered_veridical_chain", SIG_NONE, Map.of("l", ExpressionCodec.of(65)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "lament_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(42)), Map.of(), List.of(id("preset_arctic_armour"), id("preset_herald_of_ash"), id("preset_herald_of_ice"), id("preset_herald_of_thunder"), id("preset_plague_bearer"), id("preset_barrier_invocation"), id("preset_lingering_illusion"), id("preset_ghost_dance"), id("preset_mana_remnants"), id("preset_magma_barrier"), id("preset_raging_spirits"), id("preset_wind_dancer"), id("preset_cast_on_minion_death"), id("preset_scavenged_plating"), id("preset_war_banner"), id("preset_defiance_banner"), id("preset_dread_banner"), id("preset_time_of_need"), id("preset_attrition"), id("preset_charge_regulation"), id("preset_shard_scavenger"), id("preset_overwhelming_presence"), id("preset_alchemists_boon"), id("preset_reapers_invocation"), id("preset_berserk"), id("preset_herald_of_plague"), id("preset_herald_of_blood"), id("preset_withering_presence"), id("preset_ravenous_swarm"), id("preset_combat_frenzy"), id("preset_trail_of_caltrops"), id("preset_convalescence"), id("preset_iron_ward"), id("preset_siphon_elements"), id("preset_briarpatch"), id("preset_savage_fury"), id("preset_barkskin")));
        equip(bootstrap, TP_AMULET, "portent_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(42)), Map.of(), List.of(id("preset_wolf_pack"), id("preset_blink"), id("preset_elemental_invocation"), id("preset_elemental_conflux"), id("preset_sacrifice"), id("preset_mirage_archer"), id("preset_feral_invocation")));
        equip(bootstrap, TP_AMULET, "absent_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(50)), Map.of(), List.of(id("preset_cast_on_elemental_ailment"), id("preset_cast_on_critical"), id("preset_cast_on_dodge"), id("preset_rhoa_mount"), id("preset_archmage"), id("preset_trinity"), id("preset_eternal_rage")));
        equip(bootstrap, TP_AMULET, "corona_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "dusk_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(1)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "gloam_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(1)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "penumbra_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(1)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "tenebrous_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(1)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "twisted_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(1)), Map.of(), List.of());
        equip(bootstrap, TP_AMULET, "distorted_amulet", SIG_NONE, Map.of("l", ExpressionCodec.of(1)), Map.of(), List.of());

        // RING (accessory/ring)
        equip(bootstrap, TP_RING, "golden_hoop", SIG_NONE, Map.of("l", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "iron_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "lazuli_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "ruby_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(8)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "sapphire_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "topaz_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(16)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "amethyst_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(20)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "emerald_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(26)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "pearl_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "prismatic_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "gold_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "unset_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "abyssal_signet", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "two-stone_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(35)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "biostatic_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(52)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "vitalic_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "mnemonic_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "kinetic_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "oneiric_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(47)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "grasping_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "ring", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "dusk_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "gloam_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "penumbra_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "tenebrous_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "breach_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_RING, "refined_breach_ring", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());

        // BELT (accessory/belt)
        equip(bootstrap, TP_BELT, "golden_obi", SIG_NONE, Map.of("l", ExpressionCodec.of(12)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "rawhide_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "linen_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "wide_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(14)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "long_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(20)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "plate_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(25)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "ornate_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(31)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "mail_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(40)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "double_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(44)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "heavy_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "runemastered_heavy_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(65)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "utility_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(55)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "fine_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(62)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "stalking_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(50)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "invoking_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "sinew_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(32)), Map.of(), List.of());
        equip(bootstrap, TP_BELT, "forking_belt", SIG_NONE, Map.of("l", ExpressionCodec.of(32)), Map.of(), List.of());

    }

    // ===================== 模板 / 读取构造助手 =====================

    private static void tpl(BootstrapContext<ItemInstance> bootstrap, String tp, String sig, String parent) {
        EquipmentTemplate.Builder b = EquipmentTemplate.builder(id(parent));
        for (var e : sigRequirements(sig).entrySet()) b.req(e.getKey(), e.getValue());
        EquipmentGen.registerEquipmentTemplate(bootstrap, tp + TEMPLATE_SUFFIX + sig, b.build());
    }

    private static void equip(BootstrapContext<ItemInstance> bootstrap, String tp, String slug, String sig,
            Map<String, ExpressionCodec.Value> reqs, Map<String, ExpressionCodec.Value> stats, List<Identifier> skills) {
        EquipmentRead.Builder b = EquipmentRead.builder(id(tp + TEMPLATE_SUFFIX + sig));
        reqs.forEach(b::req);
        stats.forEach(b::stat);
        skills.forEach(b::skill);
        EquipmentGen.registerEquipmentRead(bootstrap, tp + ITEM_SUFFIX + slug, b.build());
    }

    // ===================== 父类模板字段 =====================

    private static Map<String, StatEntry> weaponStats() {
        Map<String, StatEntry> m = new LinkedHashMap<>();
        m.put("attack_damage", new StatRead(id("add_attack_damage")));
        m.put("attack_speed", new StatRead(id("add_inc_attack_speed")));
        m.put("attack_crit", new StatRead(id("add_crit_chance")));
        m.put("spirit", new StatRead(id("add_max_spirit")));
        m.put("reload_time", new StatRead(id("add_reload_time")));
        return m;
    }

    private static Map<String, StatEntry> armorStats() {
        Map<String, StatEntry> m = new LinkedHashMap<>();
        m.put("armor", new StatRead(id("add_armor")));
        m.put("evasion", new StatRead(id("add_evasion")));
        m.put("energy", new StatRead(id("add_max_energy_shield")));
        m.put("block_chance", new StatRead(id("add_block_chance")));
        return m;
    }

    private static Map<String, StatEntry> accessoryStats() {
        Map<String, StatEntry> m = new LinkedHashMap<>();
        m.put("life", new StatRead(id("add_max_health")));
        m.put("mana", new StatRead(id("add_max_mana")));
        m.put("strength", new StatRead(id("add_strength")));
        m.put("dexterity", new StatRead(id("add_dexterity")));
        m.put("intelligence", new StatRead(id("add_intelligence")));
        m.put("fire_res", new StatRead(id("add_max_fire_resistance")));
        m.put("cold_res", new StatRead(id("add_max_cold_resistance")));
        m.put("lightning_res", new StatRead(id("add_max_lightning_resistance")));
        m.put("chaos_res", new StatRead(id("add_max_chaos_resistance")));
        m.put("rarity", new StatRead(id("add_found_item_rarity")));
        return m;
    }

    private static Map<String, StatEntry> focusStats() {
        Map<String, StatEntry> m = new LinkedHashMap<>();
        m.put("runic_ward", new StatRead(id("add_max_runic_ward")));
        return m;
    }

    private static Map<String, StatEntry> flaskStats() {
        Map<String, StatEntry> m = new LinkedHashMap<>();
        return m;
    }

    private static Map<String, StatEntry> gemStats() {
        Map<String, StatEntry> m = new LinkedHashMap<>();
        return m;
    }
}

