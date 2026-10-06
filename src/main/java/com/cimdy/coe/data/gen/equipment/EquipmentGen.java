package com.cimdy.coe.data.gen.equipment;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.mod.value.ModPreset;
import com.cimdy.coe.data.codec.item.ItemInstance;
import com.cimdy.coe.data.codec.item.equipment.EquipmentModPreset;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentRead;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentTemplate;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.gen.equipment.ExplicitModGen;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 装备全局字段与预设词缀注册：类别 / 子类 / 类型 / 作用路径常量 + preset 注册。
 * 装备模板与具体装备读取经 Codec 工厂注册进 ITEM_EQUIPMENT_KEY（GenEvent bootstrap → 数据包 → LoadEvent 装载）。
 */
public class EquipmentGen {
    public static final String EQUIPMENT = "equipment";
    public static final String WEAPON = "weapon";
    public static final String OFF_HAND = "off_hand";
    public static final String ARMOR = "armor";
    public static final String ACCESSORY = "accessory";
    public static final String ONE_HAND = "one_hand";
    public static final String TWO_HAND = "two_hand";

    public static final String CLAW = "claw";
    public static final String DAGGER = "dagger";
    public static final String ONE_HAND_SWORD = "one_hand_sword";
    public static final String ONE_HAND_AXE = "one_hand_axe";
    public static final String ONE_HAND_MACE = "one_hand_mace";
    public static final String SCEPTRE = "sceptre";
    public static final String SPEAR = "spear";
    public static final String FLAIL = "flail";
    public static final String WAND = "wand";
    public static final String BOW = "bow";
    public static final String STAFF = "staff";
    public static final String TWO_HAND_SWORD = "two_hand_sword";
    public static final String TWO_HAND_AXE = "two_hand_axe";
    public static final String TWO_HAND_MACE = "two_hand_mace";
    public static final String QUARTERSTAFF = "quarterstaff";
    public static final String CROSSBOW = "crossbow";
    public static final String TRAP = "trap";
    public static final String TALISMAN = "talisman";
    public static final String QUIVER = "quiver";
    public static final String SHIELD = "shield";
    public static final String BUCKLER = "buckler";
    /**
     * POE2 语料 / poe2db 侧的类别字面量（{@code crawled/focus.json}、{@code spawn_no=focus}）。
     *
     * <p>⚠️ 它<b>不是</b>运行时类型键：类型树（{@code CoeDataBootstrap.registerItemType}）为保证类型键全局唯一，
     * 把法器注册为 {@link #FOCI}（{@code weapon/off_hand/foci}）。而 {@code ModifierPipeline.matchesScope}
     * 只做 {@code ctx.types().contains(key.namespace())}（不走 {@code ItemTypeChecker} 族别名），
     * 故<b>词缀池路径末段与类型收窄表都必须写 {@link #FOCI}</b>；写 {@code focus} 会静默变成死池。
     */
    public static final String FOCUS = "focus";
    public static final String BODY_ARMOUR = "body_armour";
    public static final String BOOTS = "boots";
    public static final String HELMET = "helmet";
    public static final String LEGS = "legs";
    public static final String AMULET = "amulet";
    public static final String RING = "ring";
    public static final String BELT = "belt";
    public static final String CHARM = "charm";
    /**
     * Flask 族群（POE2 三类药剂：生命 / 魔力 / 功能药剂）。
     * 参与词缀池的键为「物品类别」而非槽位——spawn_no 实证：功能药剂（utility_flask）
     * 与护符共用同一词缀池，故 charm 的类型链亦接入 {@code utility_flask} 之上的家族层。
     */
    public static final String FLASK = "flask";
    public static final String LIFE_FLASK = "life_flask";
    public static final String MANA_FLASK = "mana_flask";
    public static final String UTILITY_FLASK = "utility_flask";
    public static final String GEM = "gem";

    /** 父类模板标识（base / weapon / armor / accessory / focus_stats）。 */
    public static final String BASE = "base";
    public static final String FOCUS_STATS = "focus_stats";

    // ===================== 需求签名（sig）常量 =====================
    // 与 sigRequirements / sigOf 对齐，避免签名硬编码。
    public static final String SIG_NONE = "none";
    public static final String SIG_STRENGTH = "strength";
    public static final String SIG_DEXTERITY = "dexterity";
    public static final String SIG_INTELLIGENCE = "intelligence";
    public static final String SIG_STRENGTH_DEXTERITY = "strength_dexterity";
    public static final String SIG_STRENGTH_INTELLIGENCE = "strength_intelligence";
    public static final String SIG_DEXTERITY_INTELLIGENCE = "dexterity_intelligence";
    public static final String SIG_STRENGTH_DEXTERITY_INTELLIGENCE = "strength_dexterity_intelligence";

    // ===================== 装备读取路径分隔符 =====================
    public static final String TEMPLATE_SUFFIX = "/template/";
    public static final String ITEM_SUFFIX = "/item/";
    public static final String PATH_SEP = "/";
    public static final String TYPE_SUFFIX = "/type";
    public static final String PRESET_SUFFIX = "/preset/";

    // ---- 装备类型（type）标识：类型层级注册与类型判定共用。 ----
    public static final String MAIN_HAND = "main_hand";
    public static final String CHEST = "chest";
    public static final String HEAD = "head";
    public static final String FEET = "feet";
    public static final String SWORD = "sword";
    public static final String AXE = "axe";
    public static final String MACE = "mace";
    public static final String CROSS_BOW = "cross_bow";
    public static final String FOCI = "foci";

    // ---- 词缀作用路径（path）：类别 / 槽位 / 类型字段拼接，供 PrefixMod / SuffixMod 注册 preset。 ----
    public static final String EQUIPMENT_PATH = "";
    public static final String ARMOR_PATH = ARMOR;
    public static final String CHEST_PATH = ARMOR + "/" + CHEST;
    public static final String LEGS_PATH = ARMOR + "/" + LEGS;
    public static final String FEET_PATH = ARMOR + "/" + FEET;
    public static final String HEAD_PATH = ARMOR + "/" + HEAD;
    /**
     * 法器词缀池路径。末段必须是<b>运行时类型键</b> {@link #FOCI}：池身份取路径末段往前第 3 段
     * （{@code LoadEvent#MOD_IDENTITY_NAMESPACE_FROM_END}），{@code matchesScope} 拿它去 {@code types()} 里查，
     * 因此前导的 {@code armor/off_hand} 只是历史命名、不影响命中，末段写错则整池失效。
     */
    public static final String FOCI_PATH = ARMOR + "/" + OFF_HAND + "/" + FOCI;
    public static final String SHIELD_PATH = ARMOR + "/" + OFF_HAND + "/" + SHIELD;
    public static final String MAIN_HAND_PATH = WEAPON + "/" + MAIN_HAND;
    public static final String BOW_PATH = MAIN_HAND_PATH + "/" + BOW;
    public static final String CROSSBOW_PATH = MAIN_HAND_PATH + "/" + CROSS_BOW;

    // ---- 词缀挂载目录（mod/<prefix|suffix>）----
    public static final String MOD_PREFIX = "mod/prefix";
    public static final String MOD_SUFFIX = "mod/suffix";

    // ---- 槽位路径（词缀注册路径 = <槽位路径>/mod/<prefix|suffix>）----
    public static final String AMULET_PATH = ACCESSORY + "/" + AMULET;
    public static final String BELT_PATH = ACCESSORY + "/" + BELT;
    public static final String RING_PATH = ACCESSORY + "/" + RING;
    public static final String CHARM_PATH = ACCESSORY + "/" + CHARM;
    public static final String BUCKLER_PATH = ARMOR + "/" + OFF_HAND + "/" + BUCKLER;
    public static final String CLAW_PATH = MAIN_HAND_PATH + "/" + CLAW;
    public static final String DAGGER_PATH = MAIN_HAND_PATH + "/" + DAGGER;
    public static final String FLAIL_PATH = MAIN_HAND_PATH + "/" + FLAIL;
    public static final String ONE_HAND_AXE_PATH = MAIN_HAND_PATH + "/" + ONE_HAND_AXE;
    public static final String ONE_HAND_MACE_PATH = MAIN_HAND_PATH + "/" + ONE_HAND_MACE;
    public static final String ONE_HAND_SWORD_PATH = MAIN_HAND_PATH + "/" + ONE_HAND_SWORD;
    public static final String QUARTERSTAFF_PATH = MAIN_HAND_PATH + "/" + QUARTERSTAFF;
    public static final String SCEPTRE_PATH = MAIN_HAND_PATH + "/" + SCEPTRE;
    public static final String SPEAR_PATH = MAIN_HAND_PATH + "/" + SPEAR;
    public static final String STAFF_PATH = MAIN_HAND_PATH + "/" + STAFF;
    public static final String TALISMAN_PATH = MAIN_HAND_PATH + "/" + TALISMAN;
    public static final String TRAP_PATH = MAIN_HAND_PATH + "/" + TRAP;
    public static final String TWO_HAND_AXE_PATH = MAIN_HAND_PATH + "/" + TWO_HAND_AXE;
    public static final String TWO_HAND_MACE_PATH = MAIN_HAND_PATH + "/" + TWO_HAND_MACE;
    public static final String TWO_HAND_SWORD_PATH = MAIN_HAND_PATH + "/" + TWO_HAND_SWORD;
    public static final String WAND_PATH = MAIN_HAND_PATH + "/" + WAND;
    public static final String QUIVER_PATH = MAIN_HAND_PATH + "/" + QUIVER;
    // ---- 药剂路径（类型树位置，非装备槽位）：<flask>/<类别> ----
    public static final String FLASK_PATH = FLASK;
    public static final String LIFE_FLASK_PATH = FLASK + "/" + LIFE_FLASK;
    public static final String MANA_FLASK_PATH = FLASK + "/" + MANA_FLASK;

    // ---- 大类层词缀路径（<大类>/mod/<prefix|suffix>）：跨槽位共享家族下沉于此，按基底属性条件命中 ----
    public static final String ARMOR_GROUP_MOD_PREFIX = ARMOR + "/" + MOD_PREFIX;
    public static final String ARMOR_GROUP_MOD_SUFFIX = ARMOR + "/" + MOD_SUFFIX;
    public static final String WEAPON_GROUP_MOD_PREFIX = WEAPON + "/" + MOD_PREFIX;
    public static final String WEAPON_GROUP_MOD_SUFFIX = WEAPON + "/" + MOD_SUFFIX;
    public static final String ACCESSORY_GROUP_MOD_PREFIX = ACCESSORY + "/" + MOD_PREFIX;
    public static final String ACCESSORY_GROUP_MOD_SUFFIX = ACCESSORY + "/" + MOD_SUFFIX;

    // ---- 词缀注册完整路径（<槽位>/mod/<prefix|suffix>）：产物直接引用，不在生成处拼接 ----
    public static final String AMULET_MOD_PREFIX = AMULET_PATH + "/" + MOD_PREFIX;
    public static final String AMULET_MOD_SUFFIX = AMULET_PATH + "/" + MOD_SUFFIX;
    public static final String BELT_MOD_PREFIX = BELT_PATH + "/" + MOD_PREFIX;
    public static final String BELT_MOD_SUFFIX = BELT_PATH + "/" + MOD_SUFFIX;
    public static final String RING_MOD_PREFIX = RING_PATH + "/" + MOD_PREFIX;
    public static final String RING_MOD_SUFFIX = RING_PATH + "/" + MOD_SUFFIX;
    public static final String CHARM_MOD_PREFIX = CHARM_PATH + "/" + MOD_PREFIX;
    public static final String CHARM_MOD_SUFFIX = CHARM_PATH + "/" + MOD_SUFFIX;
    /** 药剂词缀路径（每类一份池，键 = item class；共享家族在各类池中各自登记）。 */
    public static final String LIFE_FLASK_MOD_PREFIX = LIFE_FLASK_PATH + "/" + MOD_PREFIX;
    public static final String LIFE_FLASK_MOD_SUFFIX = LIFE_FLASK_PATH + "/" + MOD_SUFFIX;
    public static final String MANA_FLASK_MOD_PREFIX = MANA_FLASK_PATH + "/" + MOD_PREFIX;
    public static final String MANA_FLASK_MOD_SUFFIX = MANA_FLASK_PATH + "/" + MOD_SUFFIX;
    /**
     * 药剂<b>父类层</b>词缀路径：生命 / 魔力药剂共有的词缀只注册一份，靠 type 树
     * （{@code flask/type.json} 声明 life_flask / mana_flask 为子类别）在解析时同时命中两者，
     * 与装备的 {@code weapon/mod/*} 等「组层」常量同构。
     */
    public static final String FLASK_MOD_PREFIX = FLASK_PATH + "/" + MOD_PREFIX;
    public static final String FLASK_MOD_SUFFIX = FLASK_PATH + "/" + MOD_SUFFIX;
    public static final String CHEST_MOD_PREFIX = CHEST_PATH + "/" + MOD_PREFIX;
    public static final String CHEST_MOD_SUFFIX = CHEST_PATH + "/" + MOD_SUFFIX;
    public static final String LEGS_MOD_PREFIX = LEGS_PATH + "/" + MOD_PREFIX;
    public static final String LEGS_MOD_SUFFIX = LEGS_PATH + "/" + MOD_SUFFIX;
    public static final String FEET_MOD_PREFIX = FEET_PATH + "/" + MOD_PREFIX;
    public static final String FEET_MOD_SUFFIX = FEET_PATH + "/" + MOD_SUFFIX;
    public static final String HEAD_MOD_PREFIX = HEAD_PATH + "/" + MOD_PREFIX;
    public static final String HEAD_MOD_SUFFIX = HEAD_PATH + "/" + MOD_SUFFIX;
    public static final String SHIELD_MOD_PREFIX = SHIELD_PATH + "/" + MOD_PREFIX;
    public static final String SHIELD_MOD_SUFFIX = SHIELD_PATH + "/" + MOD_SUFFIX;
    public static final String BUCKLER_MOD_PREFIX = BUCKLER_PATH + "/" + MOD_PREFIX;
    public static final String BUCKLER_MOD_SUFFIX = BUCKLER_PATH + "/" + MOD_SUFFIX;
    /** 法器词缀注册路径（末段 = 类型键 {@code foci}，见 {@link #FOCI_PATH}）。 */
    public static final String FOCI_MOD_PREFIX = FOCI_PATH + "/" + MOD_PREFIX;
    public static final String FOCI_MOD_SUFFIX = FOCI_PATH + "/" + MOD_SUFFIX;
    public static final String BOW_MOD_PREFIX = BOW_PATH + "/" + MOD_PREFIX;
    public static final String BOW_MOD_SUFFIX = BOW_PATH + "/" + MOD_SUFFIX;
    public static final String CROSSBOW_MOD_PREFIX = CROSSBOW_PATH + "/" + MOD_PREFIX;
    public static final String CROSSBOW_MOD_SUFFIX = CROSSBOW_PATH + "/" + MOD_SUFFIX;
    public static final String CLAW_MOD_PREFIX = CLAW_PATH + "/" + MOD_PREFIX;
    public static final String CLAW_MOD_SUFFIX = CLAW_PATH + "/" + MOD_SUFFIX;
    public static final String DAGGER_MOD_PREFIX = DAGGER_PATH + "/" + MOD_PREFIX;
    public static final String DAGGER_MOD_SUFFIX = DAGGER_PATH + "/" + MOD_SUFFIX;
    public static final String FLAIL_MOD_PREFIX = FLAIL_PATH + "/" + MOD_PREFIX;
    public static final String FLAIL_MOD_SUFFIX = FLAIL_PATH + "/" + MOD_SUFFIX;
    public static final String ONE_HAND_AXE_MOD_PREFIX = ONE_HAND_AXE_PATH + "/" + MOD_PREFIX;
    public static final String ONE_HAND_AXE_MOD_SUFFIX = ONE_HAND_AXE_PATH + "/" + MOD_SUFFIX;
    public static final String ONE_HAND_MACE_MOD_PREFIX = ONE_HAND_MACE_PATH + "/" + MOD_PREFIX;
    public static final String ONE_HAND_MACE_MOD_SUFFIX = ONE_HAND_MACE_PATH + "/" + MOD_SUFFIX;
    public static final String ONE_HAND_SWORD_MOD_PREFIX = ONE_HAND_SWORD_PATH + "/" + MOD_PREFIX;
    public static final String ONE_HAND_SWORD_MOD_SUFFIX = ONE_HAND_SWORD_PATH + "/" + MOD_SUFFIX;
    public static final String QUARTERSTAFF_MOD_PREFIX = QUARTERSTAFF_PATH + "/" + MOD_PREFIX;
    public static final String QUARTERSTAFF_MOD_SUFFIX = QUARTERSTAFF_PATH + "/" + MOD_SUFFIX;
    public static final String SCEPTRE_MOD_PREFIX = SCEPTRE_PATH + "/" + MOD_PREFIX;
    public static final String SCEPTRE_MOD_SUFFIX = SCEPTRE_PATH + "/" + MOD_SUFFIX;
    public static final String SPEAR_MOD_PREFIX = SPEAR_PATH + "/" + MOD_PREFIX;
    public static final String SPEAR_MOD_SUFFIX = SPEAR_PATH + "/" + MOD_SUFFIX;
    public static final String STAFF_MOD_PREFIX = STAFF_PATH + "/" + MOD_PREFIX;
    public static final String STAFF_MOD_SUFFIX = STAFF_PATH + "/" + MOD_SUFFIX;
    public static final String TALISMAN_MOD_PREFIX = TALISMAN_PATH + "/" + MOD_PREFIX;
    public static final String TALISMAN_MOD_SUFFIX = TALISMAN_PATH + "/" + MOD_SUFFIX;
    public static final String TRAP_MOD_PREFIX = TRAP_PATH + "/" + MOD_PREFIX;
    public static final String TRAP_MOD_SUFFIX = TRAP_PATH + "/" + MOD_SUFFIX;
    public static final String TWO_HAND_AXE_MOD_PREFIX = TWO_HAND_AXE_PATH + "/" + MOD_PREFIX;
    public static final String TWO_HAND_AXE_MOD_SUFFIX = TWO_HAND_AXE_PATH + "/" + MOD_SUFFIX;
    public static final String TWO_HAND_MACE_MOD_PREFIX = TWO_HAND_MACE_PATH + "/" + MOD_PREFIX;
    public static final String TWO_HAND_MACE_MOD_SUFFIX = TWO_HAND_MACE_PATH + "/" + MOD_SUFFIX;
    public static final String TWO_HAND_SWORD_MOD_PREFIX = TWO_HAND_SWORD_PATH + "/" + MOD_PREFIX;
    public static final String TWO_HAND_SWORD_MOD_SUFFIX = TWO_HAND_SWORD_PATH + "/" + MOD_SUFFIX;
    public static final String WAND_MOD_PREFIX = WAND_PATH + "/" + MOD_PREFIX;
    public static final String WAND_MOD_SUFFIX = WAND_PATH + "/" + MOD_SUFFIX;
    public static final String QUIVER_MOD_PREFIX = QUIVER_PATH + "/" + MOD_PREFIX;
    public static final String QUIVER_MOD_SUFFIX = QUIVER_PATH + "/" + MOD_SUFFIX;

    // ===================== 装备读取类别路径（tp）常量 =====================
    // 由类别字段拼接，供 ExplicitEquipmentGen 的 equip/tpl 调用引用，避免路径硬编码。
    public static final String TP_CLAW = WEAPON + "/" + MAIN_HAND + "/" + ONE_HAND + "/" + CLAW;
    public static final String TP_DAGGER = WEAPON + "/" + MAIN_HAND + "/" + ONE_HAND + "/" + DAGGER;
    public static final String TP_ONE_HAND_SWORD = WEAPON + "/" + MAIN_HAND + "/" + ONE_HAND + "/" + ONE_HAND_SWORD;
    public static final String TP_ONE_HAND_AXE = WEAPON + "/" + MAIN_HAND + "/" + ONE_HAND + "/" + ONE_HAND_AXE;
    public static final String TP_ONE_HAND_MACE = WEAPON + "/" + MAIN_HAND + "/" + ONE_HAND + "/" + ONE_HAND_MACE;
    public static final String TP_SCEPTRE = WEAPON + "/" + MAIN_HAND + "/" + ONE_HAND + "/" + SCEPTRE;
    public static final String TP_SPEAR = WEAPON + "/" + MAIN_HAND + "/" + ONE_HAND + "/" + SPEAR;
    public static final String TP_FLAIL = WEAPON + "/" + MAIN_HAND + "/" + ONE_HAND + "/" + FLAIL;
    public static final String TP_WAND = WEAPON + "/" + MAIN_HAND + "/" + ONE_HAND + "/" + WAND;
    public static final String TP_BOW = WEAPON + "/" + MAIN_HAND + "/" + TWO_HAND + "/" + BOW;
    public static final String TP_STAFF = WEAPON + "/" + MAIN_HAND + "/" + TWO_HAND + "/" + STAFF;
    public static final String TP_TWO_HAND_SWORD = WEAPON + "/" + MAIN_HAND + "/" + TWO_HAND + "/" + TWO_HAND_SWORD;
    public static final String TP_TWO_HAND_AXE = WEAPON + "/" + MAIN_HAND + "/" + TWO_HAND + "/" + TWO_HAND_AXE;
    public static final String TP_TWO_HAND_MACE = WEAPON + "/" + MAIN_HAND + "/" + TWO_HAND + "/" + TWO_HAND_MACE;
    public static final String TP_QUARTERSTAFF = WEAPON + "/" + MAIN_HAND + "/" + TWO_HAND + "/" + QUARTERSTAFF;
    public static final String TP_CROSS_BOW = WEAPON + "/" + MAIN_HAND + "/" + TWO_HAND + "/" + CROSS_BOW;
    public static final String TP_TRAP = WEAPON + "/" + MAIN_HAND + "/" + TWO_HAND + "/" + TRAP;
    public static final String TP_TALISMAN = WEAPON + "/" + MAIN_HAND + "/" + TWO_HAND + "/" + TALISMAN;
    public static final String TP_QUIVER = WEAPON + "/" + OFF_HAND + "/" + QUIVER;
    public static final String TP_SHIELD = WEAPON + "/" + OFF_HAND + "/" + SHIELD;
    public static final String TP_BUCKLER = WEAPON + "/" + OFF_HAND + "/" + BUCKLER;
    public static final String TP_FOCI = WEAPON + "/" + OFF_HAND + "/" + FOCI;
    public static final String TP_CHEST = ARMOR + "/" + CHEST;
    public static final String TP_LEGS = ARMOR + "/" + LEGS;
    public static final String TP_FEET = ARMOR + "/" + FEET;
    public static final String TP_HEAD = ARMOR + "/" + HEAD;
    public static final String TP_AMULET = ACCESSORY + "/" + AMULET;
    public static final String TP_RING = ACCESSORY + "/" + RING;
    public static final String TP_BELT = ACCESSORY + "/" + BELT;

    /** 构造引用 Identifier（coe:<path>）。 */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, path);
    }

    private static ResourceKey<ItemInstance> key(String path) {
        return ResourceKey.create(LoadEvent.ITEM_EQUIPMENT_KEY, id(path));
    }
    // ===================== 装备数据注册（委托给显式生成器） =====================

    /** 注册全部具体装备读取（EquipmentRead）：委托 ExplicitEquipmentGen.registerReads。 */
    public static void registerEquipment(BootstrapContext<ItemInstance> bootstrap) {
        ExplicitEquipmentGen.registerReads(bootstrap);
    }

    /** 注册装备模板（父类 + 各子类 per-sig）：委托 ExplicitEquipmentGen.registerTemplates。 */
    public static void registerEquipmentTemplate(BootstrapContext<ItemInstance> bootstrap) {
        ExplicitEquipmentGen.registerTemplates(bootstrap);
    }

    /** 直接按路径注册一条 EquipmentRead（供 ExplicitEquipmentGen 使用）。 */
    public static void registerEquipmentRead(BootstrapContext<ItemInstance> bootstrap, String path, EquipmentRead read) {
        bootstrap.register(key(path), read);
    }

    /** 直接按路径注册一条 EquipmentTemplate（供 ExplicitEquipmentGen 使用）。 */
    public static void registerEquipmentTemplate(BootstrapContext<ItemInstance> bootstrap, String path, EquipmentTemplate tpl) {
        bootstrap.register(key(path), tpl);
    }

    /** 需求属性签名：按 str/dex/int 顺序以 "_" 连接（如 strength_dexterity），无属性为 none。 */
    public static String sigOf(Map<String, ExpressionCodec.Value> req) {
        StringBuilder sb = new StringBuilder();
        if (req.containsKey("req_strength")) sb.append("strength");
        if (req.containsKey("req_dexterity")) {
            if (sb.length() > 0) sb.append("_");
            sb.append("dexterity");
        }
        if (req.containsKey("req_intelligence")) {
            if (sb.length() > 0) sb.append("_");
            sb.append("intelligence");
        }
        return sb.length() == 0 ? "none" : sb.toString();
    }

    /** 由签名生成需求字段表（键 = 关联符 l/str/dex/int）。 */
    public static Map<String, EquipmentTemplate.RequireKind> sigRequirements(String sig) {
        Map<String, EquipmentTemplate.RequireKind> m = new LinkedHashMap<>();
        m.put("l", EquipmentTemplate.RequireKind.LEVEL);
        if (!"none".equals(sig)) {
            for (String tok : sig.split("_")) {
                switch (tok) {
                    case "strength" -> m.put("str", EquipmentTemplate.RequireKind.STRENGTH);
                    case "dexterity" -> m.put("dex", EquipmentTemplate.RequireKind.DEXTERITY);
                    case "intelligence" -> m.put("int", EquipmentTemplate.RequireKind.INTELLIGENCE);
                    default -> throw new IllegalArgumentException("unknown sig token: " + tok);
                }
            }
        }
        return m;
    }

    /** 注册预设词缀（前缀/后缀）。具体装备模板与读取改由 ExplicitEquipmentGen 注册。 */
    public static void registerMods(BootstrapContext<ItemInstance> bootstrap) {
        ExplicitModGen.register(bootstrap);
    }

    /**
     * 资源键去重护栏（{@code registerMod} 两个重载共用）。
     *
     * <p>键由 {@code path + fileName} 直接拼出、<b>无任何消歧后缀</b>，故同一 (path, 键位, fileName)
     * 重复注册会写进同一个资源键 → 后注册覆盖先注册，整条词缀变体<b>静默丢失</b>（既不报错也不出现在数据包里）。
     * 历史坑：#243 前共有 27 组、68 条词缀因此被吞（如 {@code ColdDamage} 按武器族拆成 4 条却同名，
     * 组内 id 相同但 types 收窄 / 阶数 / 权重各异，无法合并）。
     * 这里改为 fail-fast：重复即抛，把静默丢数据变成构建期硬错误。
     *
     * <p>用 {@link Identifier}（值语义 equals）而非 {@link ResourceKey}，避免引用语义导致去重失效。
     */
    private static final java.util.Set<Identifier> REGISTERED_MOD_KEYS = new java.util.HashSet<>();

    /** 校验并登记资源键；重复键抛 {@link IllegalStateException}。 */
    private static ResourceKey<ItemInstance> modKey(String path, String sep, String fileName) {
        // 资源路径必须符合 [a-z0-9/._-]（Minecraft Identifier.assertValidPath）；显式词缀 fileName 沿用驼峰
        // 命名（如 IncreasedLife），必须在此归一为小写，否则注册/生成数据包时 assertValidPath 直接抛异常。
        // fileName 仅作注册键与数据包路径段使用，运行时由 LoadEvent 按同一路径回读，无任何硬编码大写查找，小写化安全。
        Identifier id = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, path + sep + fileName.toLowerCase(java.util.Locale.ROOT));
        if (!REGISTERED_MOD_KEYS.add(id)) {
            throw new IllegalStateException("duplicate mod preset resource key: " + id
                    + " (path=" + path + ", fileName=" + fileName + ")"
                    + " —— 同一 (path, fileName) 会互相覆盖、静默丢词条；请给 fileName 加类型族后缀消歧。");
        }
        return ResourceKey.create(LoadEvent.ITEM_EQUIPMENT_KEY, id);
    }

    public static void registerMod(
            BootstrapContext<ItemInstance> bootstrap, String path, String fileName, EquipmentModPreset modPreset) {
        bootstrap.register(modKey(path, PRESET_SUFFIX, fileName), modPreset);
    }

    public static void registerMod(
            BootstrapContext<ItemInstance> bootstrap, String path, String fileName, ModPreset mod) {
        bootstrap.register(modKey(path, PATH_SEP, fileName), mod);
    }
}
