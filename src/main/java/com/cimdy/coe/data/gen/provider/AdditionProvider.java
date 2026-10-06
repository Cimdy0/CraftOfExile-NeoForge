package com.cimdy.coe.data.gen.provider;

import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.addition.impl.GlobalAddition;
import com.cimdy.coe.data.codec.item.addition.impl.LocalAddition;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 数据生成：把全部 {@link AdditionType} 声明写出为 {@code data/coe/coe/affix/addition/<name>.json}
 * （运行时由 {@code LoadEvent#loadAddition} 读取并注入 {@code ADDITION_MAP}）。
 * 迁移自原手写 {@code src/main/resources/data/coe/coe/affix/addition}（172 条）。
 *
 * <h2>生成期无需解析</h2>
 * 全部条目经 {@link AdditionType#global} / {@link GlobalAddition} / {@link LocalAddition#ofMinMax}
 * 等工厂方法构造，内部只产出持 {@code ref} 的引用型结构（{@code StatRead}）——
 * <b>编码只落 {@code ref} 字段、不触发任何解析</b>。故即便
 * {@code AdditionRead#resolve()} 依赖的 {@code ADDITION_MAP} 在生成期为空也毫无影响：
 * 引用器此时只是被写出的字段，不是被求值的对象。
 *
 * <h2>声明即唯一来源</h2>
 * {@link #ENTRIES}（{@link LinkedHashMap}）按声明顺序保存；键即落盘文件名（= 词缀身份路径名）。
 */
public class AdditionProvider implements DataProvider {

    /** 词缀名 → 声明（键即文件名，写入 {@code affix/addition/<name>.json}）。 */
    private static final Map<String, AdditionType> ENTRIES = new LinkedHashMap<>();

    static {
        // ── 全局：单 stat ──
        global("accuracy_inc", "inc_accuracy");
        global("add_accuracy", "add_accuracy");
        global("add_attack_damage", "add_attack_damage");
        global("add_chaos_damage", "add_chaos_damage");
        global("add_chaos_resistance", "add_chaos_resistance");
        global("add_cold_damage", "add_cold_damage");
        global("add_cold_resistance", "add_cold_resistance");
        global("add_crit_damage", "add_crit_damage");
        global("add_dexterity", "to_dexterity");
        global("add_elemental_damage", "add_elemental_damage");
        global("add_fire_damage", "add_fire_damage");
        global("add_fire_resistance", "add_fire_resistance");
        global("add_health_regeneration_per_second", "add_health_regeneration_rate");
        global("add_intelligence", "to_intelligence");
        global("add_lightning_damage", "add_light_damage");
        global("add_lightning_resistance", "add_lightning_resistance");
        global("add_max_chaos_resistance", "add_max_chaos_resistance");
        global("add_max_cold_resistance", "add_max_cold_resistance");
        global("add_max_fire_resistance", "add_max_fire_resistance");
        global("add_max_frenzy_charges", "to_max_frenzy_charges");
        global("add_max_health", "add_max_health");
        global("add_max_lightning_resistance", "add_max_lightning_resistance");
        global("add_max_mana", "add_max_mana");
        global("add_max_power_charges", "to_max_power_charges");
        global("add_max_runic_ward", "add_max_runic_ward");
        global("add_physical_damage", "add_physical_damage");
        global("add_strength", "to_strength");
        global("add_stun_threshold", "add_stun_threshold");
        global("ailment_magnitude_freeze", "ailment_magnitude_freeze");
        global("ailment_magnitude_inc", "inc_ailment_magnitude");
        global("attack_damage_inc", "inc_attack_damage");
        global("attack_physical_damage_inc", "inc_attack_damage_physical");
        global("attack_speed_inc", "inc_attack_speed");
        global("attribute_requirements", "triple_attributes_requirements_of_martial_weapons");
        global("banner_damage_inc", "inc_banner_damage");
        global("bleeding_chance_on_attack_hit", "chance_to_inflict_bleeding_on_hit_damage");
        global("bleeding_damage_inc", "inc_ailment_damage_rate_bleed");
        global("bleeding_duration_on_you_inc", "inc_bleeding_duration_taken");
        global("buff_effect_taken_inc", "inc_buff_effect_taken");
        global("buff_magnitude_inc", "aura_skills_have_inc_buff_magnitude");
        global("causes_stun_buildup_inc", "inc_stun_buildup");
        global("chance_to_chill_on_hit", "chance_chill");
        global("chance_to_dodge", "chance_dodge");
        global("chance_to_freeze_on_hit", "chance_freeze");
        global("chance_to_ignite_on_hit", "chance_ignite");
        global("chance_to_maim_on_hit", "attacks_have_chance_to_maim_on_hit_damage");
        global("chance_to_poison_on_hit", "chance_to_poison_on_hit_damage");
        global("chaos_damage_inc", "inc_chaos_damage");
        global("chill_duration_on_you_inc", "inc_chill_duration_on_enemies");
        global("cold_damage_inc", "inc_cold_damage");
        global("convert_extra_phys_to_fire", "convert_extra_phys_to_fire");
        global("crit_chance_inc", "inc_critical_hit_chance_for_attacks");
        global("curse_damage_inc", "inc_curse_damage");
        global("damage_blocking_inc", "inc_damage_blocking");
        global("damage_low_life_inc", "inc_damage_low_life");
        global("damage_over_time_inc", "inc_damage_over_time");
        global("damage_parrying_inc", "inc_damage_parrying");
        global("damage_presence_allies_inc", "inc_damage_presence_allies");
        global("damage_presence_enemies_inc", "inc_damage_presence_enemies");
        global("damage_recently_killed_inc", "inc_damage_recently_killed");
        global("damage_shapeshifted_inc", "inc_damage_shapeshifted");
        global("dexterity_inc", "inc_dexterity");
        global("elemental_ailment_threshold_inc", "inc_elemental_ailment_threshold");
        global("energy_recharge_rate_inc", "inc_energy_regeneration_rate");
        global("energy_recharge_start_faster", "add_energy_recharge");
        global("extra_chaos_damage", "extra_chaos_from_chaos");
        global("extra_cold_damage", "extra_cold_from_chaos");
        global("extra_fire_damage", "extra_fire_damage");
        global("extra_lightning_damage", "extra_lightning_damage");
        global("extra_physical_damage", "extra_physical_damage");
        global("fire_damage_inc", "inc_fire_damage");
        global("flask_mana_recovery", "inc_mana_recovery_from_flasks");
        global("found_item_number_inc", "add_found_item_number");
        global("found_item_quantity", "add_found_item_number");
        global("found_item_rarity", "add_found_item_rarity");
        global("found_item_rarity_inc", "add_found_item_rarity");
        global("freeze_duration_on_you_inc", "inc_freeze_duration_on_enemies");
        global("gain_cold_damage_as_extra", "gain_cold_damage_as_extra");
        global("gain_fire_damage_as_extra", "gain_fire_damage_as_extra");
        global("gain_lightning_damage_as_extra", "gain_lightning_damage_as_extra");
        global("gain_physical_damage_as_extra", "gain_physical_damage_as_extra");
        global("glory_generation_inc", "inc_glory_generation");
        global("grenade_damage_inc", "inc_grenade_damage");
        global("hazard_damage_inc", "inc_hazard_damage");
        global("health_regeneration_percentage_per_second", "add_max_health_regeneration");
        global("herald_damage_inc", "herald_skills_deal_inc_damage");
        global("ignite_duration_on_you_inc", "inc_ignite_duration_taken");
        global("infusion_magnitude_inc", "inc_infusion_magnitude");
        global("intelligence_inc", "inc_intelligence");
        global("launch_additional_projectile_bolt", "add_launch_additional_projectile_bolt");
        global("launch_additional_projectile_bow", "add_launch_additional_projectile_bow");
        global("leech_health_inc", "leech_health");
        global("leech_mana_inc", "leech_mana");
        global("leech_trigger", "leech_trigger");
        global("lightning_damage_inc", "inc_lightning_damage");
        global("mana_regeneration_rate_inc", "inc_mana_regeneration_rate");
        global("mark_damage_inc", "inc_mark_damage");
        global("max_health_inc", "inc_max_health");
        global("max_infusion_inc", "inc_elemental_infusion_duration");
        global("max_mana_inc", "max_mana_inc");
        global("max_volatility_inc", "inc_max_volatility");
        global("meta_damage_inc", "inc_meta_damage");
        global("minion_attack_speed", "minions_have_inc_attack_speed");
        global("minion_attack_speed_inc", "inc_minion_attack_speed");
        global("minion_damage_inc", "inc_minion_damage");
        global("minion_elemental_resist_inc", "minions_have_to_all_elemental_resistances");
        global("minion_max_health_inc", "inc_minion_max_health");
        global("minion_movement_speed_inc", "inc_minion_movement_speed");
        global("movement_speed_inc", "inc_movement_speed");
        global("movement_speed_stationary_inc", "inc_movement_speed_stationary");
        global("non_damaging_ailment_magnitude_inc", "inc_non_damaging_ailment_magnitude");
        global("penetrate_chaos", "penetrate_chaos");
        global("physical_taken_as_chaos", "physical_taken_as_chaos");
        global("physical_taken_as_cold", "physical_taken_as_cold");
        global("physical_taken_as_lightning", "physical_taken_as_lightning");
        global("poison_damage_inc", "inc_ailment_damage_rate_poison");
        global("poison_duration_on_you_inc", "inc_poison_duration_taken");
        global("presence_all_elemental_resistances", "presence_all_elemental_resistances");
        global("presence_attack_speed", "presence_attack_speed");
        global("presence_cast_speed", "presence_cast_speed");
        global("presence_crit_chance", "presence_crit_chance");
        global("presence_crit_damage", "presence_crit_damage");
        global("presence_damage", "presence_damage");
        global("presence_life_regen", "presence_life_regen");
        global("projectile_pierce", "add_projectile_pierce");
        global("projectile_speed_inc", "inc_projectile_speed");
        global("reservation_efficiency_inc", "inc_reservation_efficiency");
        global("shock_duration_on_you_inc", "inc_shock_duration");
        global("spell_damage_inc", "spell_damage_inc");
        global("strength_inc", "inc_strength");
        global("stun_duration_inc", "inc_stun_duration");
        global("stun_threshold_inc", "inc_stun_threshold");
        global("taken_crit_damage_inc", "inc_taken_crit_damage");
        global("taken_damage_inc", "inc_taken_damage");
        global("volatility_generation_inc", "inc_volatility_generation");
        global("warcry_damage_inc", "inc_damage_with_warcries");

        // ── 全局：一条词缀驱动多 stat（共享同一 roll 值） ──
        global("add_elemental_resistance", "add_fire_resistance", "add_cold_resistance", "add_lightning_resistance");
        global("add_max_elemental_resistance", "add_max_fire_resistance", "add_max_cold_resistance", "add_max_lightning_resistance");
        global("all_attributes", "to_strength", "to_dexterity", "to_intelligence");
        global("all_resistances", "add_fire_resistance", "add_cold_resistance", "add_lightning_resistance", "add_chaos_resistance");
        global("attack_elemental_damage_inc", "inc_fire_damage", "inc_cold_damage", "inc_lightning_damage");
        global("attack_elemental_skill_damage_inc", "inc_fire_damage_spell", "inc_cold_damage_spell", "inc_light_damage_spell");
        global("crit_damage", "add_crit_damage", "add_cast_crit_damage");
        global("max_elemental_resistances", "add_max_fire_resistance", "add_max_cold_resistance", "add_max_lightning_resistance");
        global("weapon_elemental_damage_percent", "inc_fire_damage", "inc_cold_damage", "inc_lightning_damage");

        // ── 全局：双占位 min / max（Adds X to Y Damage） ──
        globalMinMax("add_cold_damage_minmax", "add_cold_damage");
        globalMinMax("add_fire_damage_minmax", "add_fire_damage");
        globalMinMax("add_lightning_damage_minmax", "add_light_damage");
        globalMinMax("add_physical_damage_minmax", "add_physical_damage");
        globalMinMax("thorns", "thorns");

        // ── 局部：共享同一 roll 值 ──
        local("add_armor_local", "add_armor");
        local("add_attack_block_local", "inc_block_chance");
        local("add_attack_damage_local", "add_attack_damage");
        local("add_crit_chance_local", "add_crit_chance");
        local("add_energy_local", "add_energy_shield");
        local("add_evasion_local", "add_evasion");
        local("add_max_spirit", "add_max_spirit");
        local("armor_energy_inc_local", "inc_armor", "inc_energy_shield");
        local("armor_evasion_inc_local", "inc_armor", "inc_evasion");
        local("armor_inc_local", "inc_armor");
        local("attack_speed_inc_local", "inc_attack_speed");
        local("block_chance_inc_local", "inc_block_chance");
        local("energy_evasion_inc_local", "inc_evasion", "inc_energy_shield");
        local("energy_inc_local", "inc_energy_shield");
        local("evasion_inc_local", "inc_evasion");
        local("physical_damage_inc_local", "inc_attack_damage");

        // ── 局部：双占位 min / max ──
        localMinMax("add_cold_damage_to_attacks", "add_cold_damage_to_attacks");
        localMinMax("add_fire_damage_to_attacks", "add_fire_damage_to_attacks");
        localMinMax("add_lightning_damage_to_attacks", "add_lightning_damage_to_attacks");
        localMinMax("add_physical_damage_to_attacks", "add_physical_damage_to_attacks");
    }

    // ── 声明便捷（一律经工厂方法构造，不手搓 JSON）──────────────────────────

    /** 全局：一条词缀引用一个或多个全局 stat 条目，共享同一 roll 值。 */
    private static void global(String name, String... statPaths) {
        ENTRIES.put(name, AdditionType.global(refs(statPaths)));
    }

    /** 全局：双占位（{@code Adds X to Y Damage}）—— 同一条目分别占 {@code min} / {@code max} 两槽。 */
    private static void globalMinMax(String name, String statPath) {
        Identifier ref = StatIds.id(statPath);
        ENTRIES.put(name, new GlobalAddition(List.of(
                AdditionType.Effect.read(ref, "min"),
                AdditionType.Effect.read(ref, "max"))));
    }

    /** 局部：一条词缀引用一个或多个局部 stat 条目，共享同一 roll 值。 */
    private static void local(String name, String... statPaths) {
        ENTRIES.put(name, LocalAddition.of(refs(statPaths)));
    }

    /** 局部：双占位（{@code Adds X to Y Damage to Attacks}）—— 两槽各自独立 roll。 */
    private static void localMinMax(String name, String statPath) {
        ENTRIES.put(name, LocalAddition.ofMinMax(StatIds.id(statPath)));
    }

    private static Identifier[] refs(String... paths) {
        Identifier[] out = new Identifier[paths.length];
        for (int i = 0; i < paths.length; i++) {
            out[i] = StatIds.id(paths[i]);
        }
        return out;
    }

    private final PackOutput output;

    public AdditionProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path dir = CoeDataPaths.coeRoot(output).resolve("affix/addition");
        CompletableFuture<?>[] futures = new CompletableFuture[ENTRIES.size()];
        int i = 0;
        for (Map.Entry<String, AdditionType> e : ENTRIES.entrySet()) {
            DataResult<JsonElement> result = AdditionType.CODEC.encodeStart(JsonOps.INSTANCE, e.getValue());
            JsonElement json = result.result().orElseThrow(() ->
                    new IllegalStateException("无法编码词缀: " + e.getKey()));
            futures[i++] = DataProvider.saveStable(cache, json, dir.resolve(e.getKey() + ".json"));
        }
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "Addition";
    }
}
