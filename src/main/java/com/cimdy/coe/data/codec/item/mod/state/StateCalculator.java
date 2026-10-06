package com.cimdy.coe.data.codec.item.mod.state;

import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeChecker;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.helper.item.QualityOps;

import java.util.Optional;

public final class StateCalculator {

    private StateCalculator() {}

    /**
     * 装备固有属性 -> 数值读取（来自 {@link EquipmentRead} 模板填充值）。
     * 键为模板字段键（armor / evasion / energy / block_chance / spirit / movement_penalty /
     * attack_damage / attack_speed / attack_crit / reload_time），与 EquipmentTemplate 声明一一对应。
     */
    private static double equipmentValue(EquipmentState state, String key) {
        return state.equipment()
                .flatMap(e -> Optional.ofNullable(
                        e.stats().containsKey(key) ? e.stats().get(key) : e.reqs().get(key)))
                .map(v -> v.value().left().orElse(0.0))
                .orElse(0.0);
    }

    // ---- 等级 / 需求（保留：重路由为不依赖已删除的 ItemBasic / ArmorBasic / WeaponBasic）----

    public static EquipmentState setLevelRequirement(EquipmentState state, int levelRequirement) {
        return new EquipmentState(state.level(), state.rarity(), state.types(), state.markers(),
                state.equipment().map(r -> r.withReq("l", ExpressionCodec.of((double) levelRequirement))),
                state.mods(), state.quality(), state.sockets());
    }

    public static int getBaseLevelRequirement(EquipmentState state) {
        return state.getRequireLevel();
    }

    /**
     * 重置为 COMMON。重构后等级需求由 EquipmentRead 模板静态派生、无运行时可变层，
     * 故 reset 仅重置稀有度（局部词缀修饰层已废除）。
     */
    public static EquipmentState reset(EquipmentState state) {
        return state.withRarity(Rarity.COMMON);
    }

    // ---- 品质（Quality）----

    /**
     * 品质的局部换算口径（POE2 wiki 实证）：
     * <ul>
     *   <li>武术武器：每 1% 品质 = 1% more 局部物理伤害（{@code attack_damage}）。</li>
     *   <li>护甲：每 1% 品质 = 1% more 局部防御（{@code armor} / {@code evasion} / {@code energy} / {@code runic_ward}）。</li>
     * </ul>
     * 品质本身只是 {@link EquipmentState} 上的一个通用标量，本方法是它唯一的装备侧消费点——
     * 新增受品质影响的局部属性只需在 {@code QUALITY_KEYS} 增键。
     **/
    /** 受品质影响的局部属性读取（品质为 0 时等价原值；换算口径见 {@link QualityOps#multiplier}）。 */
    private static double qualityValue(EquipmentState state, String key) {
        double raw = equipmentValue(state, key);
        int quality = state.quality();
        return quality <= 0 ? raw : raw * QualityOps.multiplier(quality);
    }

    // ---- 装备固有属性读取（重路由：直接读 EquipmentRead 模板填充值）----

    public static double getArmor(EquipmentState state) { return qualityValue(state, "armor"); }
    public static double getEvasion(EquipmentState state) { return qualityValue(state, "evasion"); }
    public static double getEnergy(EquipmentState state) { return qualityValue(state, "energy"); }
    public static double getAttackBlock(EquipmentState state) { return equipmentValue(state, "block_chance"); }
    public static double getSpirit(EquipmentState state) { return equipmentValue(state, "spirit"); }
    public static double getAttackDamage(EquipmentState state) { return qualityValue(state, "attack_damage"); }
    public static double getAttackCritChance(EquipmentState state) { return equipmentValue(state, "attack_crit"); }
    public static double getAttackSpeed(EquipmentState state) { return equipmentValue(state, "attack_speed"); }
    public static double getReloadTime(EquipmentState state) { return equipmentValue(state, "reload_time"); }
    public static double getMovementPenalty(EquipmentState state) { return equipmentValue(state, "movement_penalty"); }

    /** 基础值 = 未计入品质的模板原值（品质作为独立修饰层叠加在其上，真实聚合值由 StatEngine 提供）。 */
    public static double getBaseArmor(EquipmentState state) { return equipmentValue(state, "armor"); }
    public static double getBaseEvasion(EquipmentState state) { return equipmentValue(state, "evasion"); }
    public static double getBaseEnergy(EquipmentState state) { return equipmentValue(state, "energy"); }
    public static double getBaseAttackDamage(EquipmentState state) { return equipmentValue(state, "attack_damage"); }
    public static double getBaseAttackCritChance(EquipmentState state) { return getAttackCritChance(state); }
    public static double getBaseAttackSpeed(EquipmentState state) { return getAttackSpeed(state); }
    public static double getBaseReloadTime(EquipmentState state) { return getReloadTime(state); }

    public static boolean hasReloadTime(EquipmentState state) {
        return ItemTypeChecker.hasWeaponReloadTime(state);
    }

    // ---- 变更标记（重构后无修饰层，恒为 false；真实「加成前后」差异由 StatEngine 在 tooltip 侧重新路由）----

    // 品质作为独立局部修饰层：有品质时「当前值」与「模板原值」不再相同，变更标记据此给出。
    public static boolean armorChanged(EquipmentState state) { return state.quality() > 0; }
    public static boolean evasionChanged(EquipmentState state) { return state.quality() > 0; }
    public static boolean energyChanged(EquipmentState state) { return state.quality() > 0; }
    public static boolean attackDamageChanged(EquipmentState state) { return state.quality() > 0; }
    public static boolean attackSpeedChanged(EquipmentState state) { return false; }
    public static boolean attackCritChanceChanged(EquipmentState state) { return false; }
    public static boolean spiritChanged(EquipmentState state) { return false; }
    public static boolean reloadTimeChanged(EquipmentState state) { return false; }
}
