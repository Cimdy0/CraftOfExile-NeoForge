package com.cimdy.coe.data.codec.item.equipment;

import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.item.mod.value.ModPreset;
import com.cimdy.coe.data.codec.item.ItemInstance;
import com.cimdy.coe.data.codec.item.ItemInstances;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeChecker;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import com.cimdy.coe.data.codec.support.require.Require;
import net.minecraft.resources.Identifier;

public record EquipmentModPreset(
        Optional<Local> local, Optional<Require> require,
        Optional<List<Identifier>> types, ModPreset mod) implements ItemInstance {
    public static final Codec<EquipmentModPreset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Local.CODEC.optionalFieldOf("local").forGetter(EquipmentModPreset::local),
            Require.CODEC.optionalFieldOf("require").forGetter(EquipmentModPreset::require),
            Identifier.CODEC.listOf().optionalFieldOf("types").forGetter(EquipmentModPreset::types),
            ModPreset.CODEC.fieldOf("mod").forGetter(EquipmentModPreset::mod)
    ).apply(instance, EquipmentModPreset::new));

    /** 无条件：该大类下任意基底都生效（局部条件由 {@link #from(Local, ModPreset)} 挂接）。 */
    public static EquipmentModPreset of(ModPreset mod) {
        return new EquipmentModPreset(Optional.empty(), Optional.empty(), Optional.empty(), mod);
    }

    public static EquipmentModPreset from(Local local, ModPreset mod) {
        return new EquipmentModPreset(Optional.of(local), Optional.empty(), Optional.empty(), mod);
    }

    public static EquipmentModPreset from(Require require, ModPreset from) {
        return new EquipmentModPreset(Optional.empty(), Optional.of(require), Optional.empty(), from);
    }

    public static EquipmentModPreset from(List<Identifier> types, ModPreset from) {
        return new EquipmentModPreset(Optional.empty(), Optional.empty(), Optional.of(types), from);
    }

    public static EquipmentModPreset from(Local local, List<Identifier> types, ModPreset from) {
        return new EquipmentModPreset(Optional.of(local), Optional.empty(), Optional.of(types), from);
    }

    public static EquipmentModPreset from(Require require, List<Identifier> types, ModPreset from) {
        return new EquipmentModPreset(Optional.empty(), Optional.of(require), Optional.of(types), from);
    }

    public Condition condition() {
        return new Condition(local, require, types);
    }

    public record Condition(Optional<Local> local, Optional<Require> require, Optional<List<Identifier>> types) {
        /**
         * 预设条件命中判定。局部属性 / 属性需求 / 类型链三者皆为<b>装备语义</b>：
         * 非装备载体（药剂 / 护符）只有「三个条件全空」的无条件预设可命中，
         * 从而使同一管线对三类载体保持同构而不误命中装备专有词缀。
         */
        public boolean matches(ModifiableState carrier) {
            if (!(carrier instanceof EquipmentState state)) {
                return local.isEmpty() && require.isEmpty() && types.isEmpty();
            }
            boolean localMatch = local.isEmpty() || local.get().matches(state);
            boolean requireMatch = require.isEmpty() || require.get().matches(state);
            boolean typeMatch = types.isEmpty() || ItemTypeChecker.hasAnyType(state, types.get());
            return localMatch && requireMatch && typeMatch;
        }
    }

    @Override
    public Type<?> getType() {
        return ItemInstances.MOD_PRESET;
    }

    public enum Local implements StringRepresentable {
        ATTACK_CRIT_CHANCE("attack_crit_chance", 0),
        ATTACK_SPEED("attack_speed", 1),
        PHYSICAL_DAMAGE("physical_damage", 2),
        ARMOR("armor", 3),
        EVASION("evasion", 4),
        ENERGY("energy", 5),
        ARMOR_EVASION("armor_evasion", 8),
        ARMOR_ENERGY("armor_energy", 9),
        EVASION_WHILE_ENERGY("evasion_energy", 10),
        BLOCK_CHANCE("attack_block", 6),
        SPIRIT("spirit", 7),
        WARD("runic_ward", 11);

        public static final IntFunction<Local> BY_ID =
                ByIdMap.continuous(Local::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final Codec<Local> CODEC =
                StringRepresentable.fromEnum(Local::values);
        private final String name;
        private final int id;

        Local(String name, int value) {
            this.name = name;
            this.id = value;
        }

        public int getId() {
            return this.id;
        }

        public String getSerializedName() {
            return this.name;
        }

        public boolean matches(EquipmentState state) {
            return switch (this) {
                case ATTACK_CRIT_CHANCE -> state.hasAttackCritChance();
                case ATTACK_SPEED -> state.hasAttackSpeed();
                case PHYSICAL_DAMAGE -> state.hasAttackDamage();
                case ARMOR -> state.hasArmor();
                case EVASION -> state.hasEvasion();
                case ENERGY -> state.hasEnergy();
                case ARMOR_EVASION -> state.hasArmorAndEvasion();
                case ARMOR_ENERGY -> state.hasArmorAndEnergy();
                case EVASION_WHILE_ENERGY -> state.hasEvasionAndEnergy();
                case BLOCK_CHANCE -> state.hasBlockChance();
                case SPIRIT -> state.hasArmorSpirit() && state.hasWeaponSpirit();
                case WARD -> state.hasWard();
            };
        }
    }

    public enum Require implements StringRepresentable {
        ONLY_STRENGTH("only_str", 0),
        HAS_STRENGTH("str", 1),
        ONLY_INTELLIGENCE("only_int", 2),
        HAS_INTELLIGENCE("int", 3),
        ONLY_DEXTERITY("only_dex", 4),
        HAS_DEXTERITY("dex", 5),
        STRENGTH_INTELLIGENCE("str_int", 6),
        STRENGTH_DEXTERITY("str_dex", 7),
        DEXTERITY_INTELLIGENCE("dex_int", 8),
        STRENGTH_DEXTERITY_INTELLIGENCE("str_dex_int", 9),
        ANY("any", 10);

        public static final IntFunction<Require> BY_ID =
                ByIdMap.continuous(Require::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final Codec<Require> CODEC =
                StringRepresentable.fromEnum(Require::values);
        private final String name;
        private final int id;

        Require(String name, int value) {
            this.name = name;
            this.id = value;
        }

        public int getId() {
            return this.id;
        }

        public String getSerializedName() {
            return this.name;
        }

        public boolean matches(EquipmentState state) {
            return switch (this) {
                case ONLY_STRENGTH -> state.hasOnlyStrength();
                case HAS_STRENGTH -> state.hasStrength();
                case ONLY_INTELLIGENCE -> state.hasOnlyIntelligence();
                case HAS_INTELLIGENCE -> state.hasIntelligence();
                case ONLY_DEXTERITY -> state.hasOnlyDexterity();
                case HAS_DEXTERITY -> state.hasDexterity();
                case STRENGTH_INTELLIGENCE -> state.hasStrengthAndIntelligence();
                case STRENGTH_DEXTERITY -> state.hasStrengthAndDexterity();
                case DEXTERITY_INTELLIGENCE -> state.hasDexterityAndIntelligence();
                case STRENGTH_DEXTERITY_INTELLIGENCE -> state.hasAllAttributes();
                case ANY -> state.hasAnyAttributes();
            };
        }
    }
}
