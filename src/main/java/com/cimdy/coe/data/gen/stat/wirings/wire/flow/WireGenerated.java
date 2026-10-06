package com.cimdy.coe.data.gen.stat.wirings.wire.flow;

import com.cimdy.coe.data.gen.stat.StatGen;

import com.cimdy.coe.data.gen.stat.wirings.WiringDsl;

import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EffectMod;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import net.minecraft.resources.Identifier;

import java.util.Map;

import static com.cimdy.coe.data.gen.stat.wirings.WiringDsl.*;
import com.cimdy.coe.data.codec.support.rarity.Rarity;

/**
 * WireGenerated 域接线：<b>自描述生成型 id 簇</b>（EFFECTMOD / LIVINGMOD / TALENT / RARITY）。
 *
 * <p><b>命名即规格</b>：{@code LIVINGMOD_COE_DAMAGE_OP_ADD_VALUE_0_4_CHAOS_DAMAGE} 读作
 * 「属性 COE_DAMAGE · op ADD_VALUE · 名义量 0.4 · 标签 {CHAOS, DAMAGE}」——
 * id 中的量值即各条注释记录的「名义量」，二者必须一致。
 *
 * <p><b>为何 amount 统一用 $value 而非固定值</b>：与既有 {@code LIVINGMOD_CONVERT_*}
 * （WireDamage）保持同一约定——常量只描述「怎么算」，具体数值由词条数据注入；
 * 门禁 {@code all entry amounts use $value/value} 亦强制此形态。名义量仅记录在各条注释中备查。
 *
 * <h3>唯一未接（有意排除，勿补）</h3>
 * <ul>
 *   <li>{@code LIVINGMOD_MINECRAFT_ARMOR_OP_ADD_MULTIPLIED_BASE_1}：该 id 指向
 *       {@code coe:add_multiplied_base_attribute_minecraft_armor}，但该 EFF <b>不存在</b>——
 *       vanilla {@code Attributes.ARMOR} 的 base = 0，按<b>三定律 #3 零基禁 inc</b>，
 *       对零基属性做 ADD_MULTIPLIED_BASE 恒为 0（死接线），故不复建、不接线。</li>
 * </ul>
 */
public final class WireGenerated {

    private WireGenerated() {
    }

    public static void build(Map<Identifier, StatEntry> map) {
        // ===================== Effect 修饰符：抗性供给（EffectMod） =====================
        // 名义量 30.0
        map.put(StatIds.EFFECTMOD_FIRE_RESIST_PROVIDE_30,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_FIRE));

        // 名义量 5.0
        map.put(StatIds.EFFECTMOD_RESIST_PROVIDE_COLD_5,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_COLD));

        // 名义量 5.0
        map.put(StatIds.EFFECTMOD_RESIST_PROVIDE_FIRE_5,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_FIRE));

        // 名义量 5.0
        map.put(StatIds.EFFECTMOD_RESIST_PROVIDE_LIGHTNING_5,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_LIGHTNING));

        // ===================== 生物修饰符（LivingMod）——怪物/生物侧词条，自描述 id 已编码 属性+op+标签 =====================
        // 名义量 4.0
        map.put(StatIds.LIVINGMOD_ARMOR_4,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ARMOR));

        // 名义量 3.0
        map.put(StatIds.LIVINGMOD_CAVE_ATTACK_DAMAGE,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MELEE_MINECRAFT_ATTACK_DAMAGE));

        // 名义量 2.0
        map.put(StatIds.LIVINGMOD_COE_ACCURACY_OP_ADD_MULTIPLIED_BASE_2,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY));

        // 名义量 1.0
        map.put(StatIds.LIVINGMOD_COE_AILMENT_BUILDUP_OP_ADD_VALUE_1_STUN,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_BUILDUP_STUN));

        // 名义量 1.0
        map.put(StatIds.LIVINGMOD_COE_AILMENT_CHANCE_OP_ADD_VALUE_1_BLEED,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_BLEED_COE_AILMENT_CHANCE));

        // 名义量 1.0
        map.put(StatIds.LIVINGMOD_COE_AILMENT_CHANCE_OP_ADD_VALUE_1_POISON,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_CHANCE_POISON));

        // 名义量 1.5
        map.put(StatIds.LIVINGMOD_COE_AILMENT_THRESHOLD_OP_ADD_VALUE_1_5_STUN,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_THRESHOLD_STUN));

        // 名义量 0.1
        map.put(StatIds.LIVINGMOD_COE_ARMOR_BREAK_OP_ADD_VALUE_0_1_ARMOR_BREAK,
                self(StatEffectIds.EFF_ADD_VALUE_ARMOR_BREAK_ATTRIBUTE_COE_ARMOR_BREAK));

        // 名义量 0.3
        map.put(StatIds.LIVINGMOD_COE_CAST_SPEED_OP_ADD_VALUE_0_3,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CAST_SPEED));

        // 名义量 0.2
        map.put(StatIds.LIVINGMOD_COE_CRIT_CHANCE_OP_ADD_VALUE_0_2_ATTACK_CRIT,
                self(StatEffectIds.EFF_ADD_VALUE_ATTACK_ATTRIBUTE_COE_CRIT_CHANCE_CRIT));

        // 名义量 0.4
        map.put(StatIds.LIVINGMOD_COE_DAMAGE_OP_ADD_VALUE_0_4_CHAOS_DAMAGE,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHAOS_COE_DAMAGE_DAMAGE));

        // 名义量 0.4
        map.put(StatIds.LIVINGMOD_COE_DAMAGE_OP_ADD_VALUE_0_4_COLD_DAMAGE,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DAMAGE_COLD_DAMAGE));

        // 名义量 0.4
        map.put(StatIds.LIVINGMOD_COE_DAMAGE_OP_ADD_VALUE_0_4_DAMAGE_FIRE,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DAMAGE_DAMAGE_FIRE));

        // 名义量 0.4
        map.put(StatIds.LIVINGMOD_COE_DAMAGE_OP_ADD_VALUE_0_4_DAMAGE_LIGHTNING,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DAMAGE_DAMAGE_LIGHTNING));

        // 名义量 4.0
        map.put(StatIds.LIVINGMOD_COE_LAUNCH_ADDITIONAL_PROJECTILE_OP_ADD_VALUE_4,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_LAUNCH_ADDITIONAL_PROJECTILE));

        // 名义量 0.3
        map.put(StatIds.LIVINGMOD_COE_MANA_TAKE_OF_DAMAGE_OP_ADD_VALUE_0_3_MANA_TAKE,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_TAKE_OF_DAMAGE_MANA_TAKE));

        // 名义量 0.02
        map.put(StatIds.LIVINGMOD_COE_MAX_REGENERATION_OP_ADD_VALUE_0_02_HEALTH_REGEN,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));

        // 名义量 0.1
        map.put(StatIds.LIVINGMOD_COE_MAX_RESISTANCES_OP_ADD_VALUE_0_1_COLD,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_COLD));

        // 名义量 0.1
        map.put(StatIds.LIVINGMOD_COE_MAX_RESISTANCES_OP_ADD_VALUE_0_1_FIRE,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_FIRE));

        // 名义量 0.1
        map.put(StatIds.LIVINGMOD_COE_MAX_RESISTANCES_OP_ADD_VALUE_0_1_LIGHTNING,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_LIGHTNING));

        // 名义量 0.5
        map.put(StatIds.LIVINGMOD_COE_RESISTANCES_OP_ADD_VALUE_0_5_CHAOS,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHAOS_COE_RESISTANCES));

        // 名义量 0.5
        map.put(StatIds.LIVINGMOD_COE_RESISTANCES_OP_ADD_VALUE_0_5_COLD,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_COLD));

        // 名义量 0.5
        map.put(StatIds.LIVINGMOD_COE_RESISTANCES_OP_ADD_VALUE_0_5_FIRE,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_FIRE));

        // 名义量 0.5
        map.put(StatIds.LIVINGMOD_COE_RESISTANCES_OP_ADD_VALUE_0_5_LIGHTNING,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_LIGHTNING));

        // 名义量 -0.8
        map.put(StatIds.LIVINGMOD_COE_TAKEN_CRIT_DAMAGE_OP_ADD_VALUE__0_8,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_TAKEN_CRIT_DAMAGE));

        // 名义量 0.3
        map.put(StatIds.LIVINGMOD_MINECRAFT_ATTACK_SPEED_OP_ADD_VALUE_0_3,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED));

        // 名义量 0.5
        map.put(StatIds.LIVINGMOD_MINECRAFT_MAX_HEALTH_OP_ADD_MULTIPLIED_BASE_0_5,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MAX_HEALTH));

        // 名义量 0.3
        map.put(StatIds.LIVINGMOD_MINECRAFT_MOVEMENT_SPEED_OP_ADD_VALUE_0_3,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED));

        // 名义量 10.0
        map.put(StatIds.LIVINGMOD_RESIST_CHAOS_10,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHAOS_COE_RESISTANCES));

        // 名义量 20.0
        map.put(StatIds.LIVINGMOD_RESIST_CHAOS_20,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHAOS_COE_RESISTANCES));

        // 名义量 10.0
        map.put(StatIds.LIVINGMOD_RESIST_COLD_10,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_COLD));

        // 名义量 20.0
        map.put(StatIds.LIVINGMOD_RESIST_COLD_20,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_COLD));

        // 名义量 10.0
        map.put(StatIds.LIVINGMOD_RESIST_ELEMENTAL_10,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_ELEMENTAL));

        // 名义量 20.0
        map.put(StatIds.LIVINGMOD_RESIST_FIRE_20,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_FIRE));

        // ===================== 天赋固定值加成（Talent） =====================
        // 名义量 5.0
        map.put(StatIds.TALENT_COE_DEXTERITY_OP_ADD_VALUE_5,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DEXTERITY));

        // 名义量 5.0
        map.put(StatIds.TALENT_COE_INTELLIGENCE_OP_ADD_VALUE_5,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_INTELLIGENCE));

        // 名义量 5.0
        map.put(StatIds.TALENT_COE_STRENGTH_OP_ADD_VALUE_5,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STRENGTH));

        // 名义量 15.0
        map.put(StatIds.TALENT_MINECRAFT_ARMOR_OP_ADD_VALUE_15,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ARMOR));

        // 名义量 20.0
        map.put(StatIds.TALENT_MINECRAFT_MAX_HEALTH_OP_ADD_VALUE_20,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_MAX_HEALTH));

        // ===================== 稀有度修饰符（Rarity）——怪物稀有度 Magic/Rare/Unique 的百分比加成 =====================
        // 名义量 0.2
        map.put(StatIds.RARITY_MAGIC_ATTACK_SPEED,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED));

        // 名义量 0.2
        map.put(StatIds.RARITY_MAGIC_CAST_SPEED,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CAST_SPEED));

        // 名义量 0.87
        map.put(StatIds.RARITY_MAGIC_MAX_HEALTH,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MAX_HEALTH));

        // 名义量 0.1
        map.put(StatIds.RARITY_MAGIC_MOVEMENT_SPEED,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED));

        // 名义量 0.1
        map.put(StatIds.RARITY_MAGIC_SCALE,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_SCALE));

        // 名义量 0.33
        map.put(StatIds.RARITY_RARE_ATTACK_SPEED,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED));

        // 名义量 0.33
        map.put(StatIds.RARITY_RARE_CAST_SPEED,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CAST_SPEED));

        // 名义量 3.63
        map.put(StatIds.RARITY_RARE_MAX_HEALTH,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MAX_HEALTH));

        // 名义量 0.1
        map.put(StatIds.RARITY_RARE_MOVEMENT_SPEED,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED));

        // 名义量 0.2
        map.put(StatIds.RARITY_RARE_SCALE,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_SCALE));

        // 名义量 0.33
        map.put(StatIds.RARITY_UNIQUE_ATTACK_SPEED,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED));

        // 名义量 0.33
        map.put(StatIds.RARITY_UNIQUE_CAST_SPEED,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CAST_SPEED));

        // 名义量 5.98
        map.put(StatIds.RARITY_UNIQUE_MAX_HEALTH,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MAX_HEALTH));

        // 名义量 0.3
        map.put(StatIds.RARITY_UNIQUE_MOVEMENT_SPEED,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED));

        // 名义量 0.3
        map.put(StatIds.RARITY_UNIQUE_SCALE,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_SCALE));

    }
}
