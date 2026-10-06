package com.cimdy.coe.data.codec.item.mod.identity;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.gen.equipment.EquipmentGen;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public final class ItemTypeChecker {

    private ItemTypeChecker() {}

    /**
     * <b>族名 → 具体类型键</b>：一个族名可能横跨多片叶类别（单手 / 双手），
     * 或数据侧写法与类型键不同名（{@code crossbow} ↔ {@code cross_bow}）。
     *
     * <p>存在原因：类型键取 {@code type.json} 的父目录名，必须全局唯一，
     * 故单手 / 双手同名子类写成 {@code one_hand_sword} / {@code two_hand_sword}；
     * 而语义侧（Stat 作用域标签、技能 {@code equipment} 需求）只认族名 {@code sword}。
     * 这张表就是二者的唯一桥，任何「按族名查类型」都必须走 {@link #hasType}，
     * 不可再直接 {@code types().contains(...)}。</p>
     */
    private static final Map<String, List<String>> FAMILY_ALIASES = Map.of(
            EquipmentGen.SWORD, List.of(EquipmentGen.ONE_HAND_SWORD, EquipmentGen.TWO_HAND_SWORD),
            EquipmentGen.AXE, List.of(EquipmentGen.ONE_HAND_AXE, EquipmentGen.TWO_HAND_AXE),
            EquipmentGen.MACE, List.of(EquipmentGen.ONE_HAND_MACE, EquipmentGen.TWO_HAND_MACE),
            EquipmentGen.CROSSBOW, List.of(EquipmentGen.CROSS_BOW)
    );

    /**
     * 武器族名有序表（Stat 作用域标签 / 技能装备需求共用同一套字面量）。
     *
     * <p>不含 {@code trap}：{@code trap} 已被「陷阱实体」主语标签占用（{@code GateTag.TRAP}），
     * 若武器族再发射同一字面量会与陷阱召唤物伤害作用域串味；待二者字面量分流后再补入。</p>
     */
    public static final List<String> WEAPON_FAMILIES = List.of(
            EquipmentGen.SWORD, EquipmentGen.AXE, EquipmentGen.MACE,
            EquipmentGen.DAGGER, EquipmentGen.CLAW, EquipmentGen.FLAIL,
            EquipmentGen.SPEAR, EquipmentGen.QUARTERSTAFF, EquipmentGen.STAFF,
            EquipmentGen.WAND, EquipmentGen.SCEPTRE,
            EquipmentGen.BOW, EquipmentGen.CROSSBOW, EquipmentGen.TALISMAN
    );

    /**
     * 由物品叶类型派生其完整类型作用域（叶本身 + 全部祖先类别），供所有「按类型判定」复用。
     * 物品状态只存叶类型键（{@code ModifiableState#types()}），祖先由 {@link ItemTypeHierarchy} 派生，
     * 不再把整条类型链冗余写入状态。
     */
    private static Set<Identifier> scope(ModifiableState state) {
        Set<Identifier> s = new HashSet<>();
        for (Identifier t : state.types()) {
            s.add(t);
            s.addAll(ItemTypeHierarchy.ancestorsOf(t));
        }
        return s;
    }

    /** 检查给定裸类别名（无命名空间）是否落在物品作用域内（走 {@code coe} 命名空间）。 */
    private static boolean inScope(ModifiableState state, String name) {
        return scope(state).contains(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name));
    }

    /**
     * 通用类型命中：先按字面类型键命中，未命中再按族别名展开。
     * 一切「按类型字符串判定」的读点（词缀池作用域、技能装备需求、掉落分类）都须走此入口。
     *
     * <p>本方法仅依赖 {@link ModifiableState#types()}（叶类型键），祖先类别经
     * {@link ItemTypeHierarchy} 派生，故装备 / 药剂 / 护符共用同一判定逻辑。</p>
     */
    public static boolean hasType(ModifiableState state, String type) {
        if (type == null || type.isEmpty()) return false;
        return hasType(state, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, type));
    }

    /** {@link Identifier} 形态的类型命中（命名空间敏感的精确匹配 + 族别名展开）。 */
    public static boolean hasType(ModifiableState state, Identifier type) {
        Set<Identifier> s = scope(state);
        if (s.contains(type)) return true;
        List<String> members = FAMILY_ALIASES.get(type.getPath());
        if (members == null) return false;
        for (String member : members) {
            if (s.contains(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, member))) return true;
        }
        return false;
    }

    /** 批量版本（符号型类别名，无命名空间）：任一命中即真（{@code types} 为空视为不限制，交由调用方判断）。 */
    public static boolean hasAnyTypeName(ModifiableState state, Collection<String> types) {
        for (String type : types) {
            if (hasType(state, type)) return true;
        }
        return false;
    }

    /** {@link Identifier} 形态的批量类型命中（供 {@code EquipmentModPreset.Condition} 等消费）。 */
    public static boolean hasAnyType(ModifiableState state, Collection<Identifier> types) {
        for (Identifier type : types) {
            if (hasType(state, type)) return true;
        }
        return false;
    }

    // ---- 物品大类判定（仅依赖 types()，对装备 / 药剂 / 护符通用）----

    public static boolean isWeapon(ModifiableState state) {
        return inScope(state, EquipmentGen.WEAPON);
    }

    public static boolean isArmor(ModifiableState state) {
        return inScope(state, EquipmentGen.ARMOR);
    }

    /** 药剂（POE2：生命 / 魔力药剂，主动使用，独立库存槽）。类型树根节点为 {@code flask}。 */
    public static boolean isFlask(ModifiableState state) {
        return inScope(state, EquipmentGen.FLASK);
    }

    /** 护符（POE2：自动触发，独立库存槽）。类型树节点为 {@code accessory/charm}。 */
    public static boolean isCharm(ModifiableState state) {
        return ItemTypeHierarchy.isWithin(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, EquipmentGen.CHARM_PATH), state.types());
    }

    public static boolean isSword(ModifiableState state) {
        return hasType(state, EquipmentGen.SWORD);
    }

    public static boolean isAxe(ModifiableState state) {
        return hasType(state, EquipmentGen.AXE);
    }

    public static boolean isMace(ModifiableState state) {
        return hasType(state, EquipmentGen.MACE);
    }

    public static boolean isBow(ModifiableState state) {
        return inScope(state, EquipmentGen.BOW);
    }

    public static boolean isCrossBow(ModifiableState state) {
        return hasType(state, EquipmentGen.CROSSBOW);
    }

    public static boolean isMainHand(ModifiableState state) {
        return inScope(state, EquipmentGen.MAIN_HAND);
    }

    public static boolean isOffHand(ModifiableState state) {
        return inScope(state, EquipmentGen.OFF_HAND);
    }

    public static boolean isShield(ModifiableState state) {
        return inScope(state, EquipmentGen.SHIELD);
    }

    public static boolean isBuckler(ModifiableState state) {
        return inScope(state, EquipmentGen.BUCKLER);
    }

    public static boolean isHead(ModifiableState state) {
        return inScope(state, EquipmentGen.HEAD);
    }

    public static boolean isChest(ModifiableState state) {
        return inScope(state, EquipmentGen.CHEST);
    }

    public static boolean isLegs(ModifiableState state) {
        return inScope(state, EquipmentGen.LEGS);
    }

    public static boolean isFeet(ModifiableState state) {
        return inScope(state, EquipmentGen.FEET);
    }

    // ---- 装备专属维度判定（依赖 EquipmentState 的基底读取与属性需求，仅对装备有意义）----

    public static boolean hasAttackDamage(EquipmentState state) {
        return hasEquipmentValue(state, "attack_damage");
    }

    public static boolean hasAttackSpeed(EquipmentState state) {
        return hasEquipmentValue(state, "attack_speed");
    }

    public static boolean hasAttackCritChance(EquipmentState state) {
        return hasEquipmentValue(state, "attack_crit");
    }

    public static boolean hasWeaponSpirit(EquipmentState state) {
        return hasEquipmentValue(state, "spirit");
    }

    public static boolean hasWeaponReloadTime(EquipmentState state) {
        return hasEquipmentValue(state, "reload_time");
    }

    public static boolean hasArmor(EquipmentState state) {
        return hasEquipmentValue(state, "armor")
                && state.hasStrength();
    }

    public static boolean hasEvasion(EquipmentState state) {
        return hasEquipmentValue(state, "evasion")
                && state.hasDexterity();
    }

    public static boolean hasEnergy(EquipmentState state) {
        return hasEquipmentValue(state, "energy")
                && state.hasIntelligence();
    }

    public static boolean hasArmorAndEvasion(EquipmentState state) {
        return hasEquipmentValue(state, "armor") && hasEquipmentValue(state, "evasion")
                && state.hasStrength() && state.hasDexterity();
    }

    public static boolean hasArmorAndEnergy(EquipmentState state) {
        return hasEquipmentValue(state, "armor") && hasEquipmentValue(state, "energy")
                && state.hasStrength() && state.hasIntelligence();
    }

    public static boolean hasEvasionAndEnergy(EquipmentState state) {
        return hasEquipmentValue(state, "evasion") && hasEquipmentValue(state, "energy")
                && state.hasDexterity() && state.hasIntelligence();
    }

    public static boolean hasBlockChance(EquipmentState state) {
        return hasEquipmentValue(state, "block_chance");
    }

    public static boolean hasArmorSpirit(EquipmentState state) {
        return hasEquipmentValue(state, "spirit");
    }

    /**
     * 结界（Runic Ward）：与 armor/evasion/energy 同属「基底防御属性分层」，
     * 判定口径一致 —— 模板声明 {@code runic_ward} 值键且基底需求含智慧。
     */
    public static boolean hasWard(EquipmentState state) {
        return hasEquipmentValue(state, "runic_ward")
                && state.hasIntelligence();
    }

    private static boolean hasEquipmentValue(EquipmentState state, String key) {
        return state.equipment().map(e -> e.hasStat(key)).orElse(false);
    }
}
