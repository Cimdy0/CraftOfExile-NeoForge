package com.cimdy.coe.data.gen.provider;

import com.cimdy.coe.data.codec.item.addition.impl.MultiAddition;
import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.cimdy.coe.data.codec.item.addition.ModAdditions;
import com.cimdy.coe.data.codec.item.mod.identity.ModConflict;
import com.cimdy.coe.data.codec.item.mod.identity.SlotExpr;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 数据生成：把词缀冲突组表写出为 {@code data/coe/coe/affix/conflict/<group>.json}
 * （运行时由 {@code LoadEvent#loadAdditionConflict} 从数据包读取并注入 {@link ModConflict} 缓存）。
 *
 * <h2>成员来源</h2>
 * 成员一律用 {@link ModAdditions} 常量经 {@code ModConflict.of(slot, AdditionRead...)} 声明
 * （内部取 {@code ref}），不手搓字符串、不逐项写 {@code .ref()}。
 *
 * <h2>部位约束（回源核验）</h2>
 * 各组的 {@code slot} 取自源库（poe2db 基底页 {@code tp_*.html}）中该 {@code ModFamilyList}
 * 词缀族<b>实际出现的基底类型</b>，非臆测。武器族用 {@code sword / axe / mace / crossbow}
 * （经 {@code ItemTypeChecker} 族别名自动展开单手 / 双手），故不必罗列子类。
 *
 * <h2>回源核验结论（2026-09-26）</h2>
 * <ul>
 *   <li>3377 条 mod、178 个源库词缀族；本表 27 组中 <b>23 组名</b>与源库族名一致。</li>
 *   <li>{@code spell_damage_and_mana} <b>已剔除</b>——源库无此族，它是复缀
 *       （应建模 MultiAddition、身份取主缀）；源库只有 {@code SpellDamage}（amulet / focus）。</li>
 *   <li>{@code BaseLocalDefencesAndMana} 源库<b>确实存在</b>（focus）——早先按「命名要素」
 *       把它改名成 {@code defences_percent_and_mana} 是<b>误判</b>，已回退原名。
 *       教训：族名是源库字面量，不可用自定的要素词表去反推。</li>
 *   <li>无同名源库族的三组已按「成员所属真族的部位」定案：
 *       {@code base_local_defences_and_defence_percent} → buckler, foci；
 *       {@code base_local_defences_and_life} → 仅 buckler（防御% 与平坦生命在源库<b>只在 buckler 共现</b>）；
 *       {@code physical_damage_percent} <b>删除</b>（与 {@code attack_damage} 成员完全相同的纯重复）。</li>
 *   <li><b>待核（需 mod→Addition 映射）</b>：{@code attack_damage} 含 {@code PHYSICAL_DAMAGE_INC_LOCAL}，
 *       但源库 {@code AttackDamage} 只在 amulet/ring（全局），局部物理% 属武器族的
 *       {@code LocalPhysicalDamagePercent}；该成员疑为错配，待映射建立后校正。</li>
 *   <li><b>已证伪的局部/全局混同</b>：源库 {@code AttackDamage} 只在 amulet / ring（全局），
 *       而 {@code LocalPhysicalDamagePercent} 只在武器（局部），部位<b>完全不相交</b>；
 *       现由各自 {@code slot} 分开，不再并为一类。</li>
 * </ul>
 */
public class ConflictTableProvider implements DataProvider {

    /** 便捷：多个类型键 / 族名任一命中（或）。 */
    private static SlotExpr slots(String... types) {
        return SlotExpr.anyTypes(List.of(types));
    }

    /** 14 类武器（源库武器族全集）。 */
    private static final String[] WEAPONS = {
            "sword", "axe", "mace", "dagger", "claw", "flail",
            "spear", "quarterstaff", "bow", "crossbow", "talisman"
    };

    private static SlotExpr weaponsPlus(String... extra) {
        List<String> all = new java.util.ArrayList<>(List.of(WEAPONS));
        all.addAll(List.of(extra));
        return SlotExpr.anyTypes(all);
    }

    private static final Map<String, ModConflict> GROUPS = new LinkedHashMap<>();
    static {
        // —— 武器：局部物理 / 攻速 / 元素伤害 ——
        GROUPS.put("local_physical_damage_percent", ModConflict.of(
                weaponsPlus(), ModAdditions.ATTACK_DAMAGE_INC, ModAdditions.PHYSICAL_DAMAGE_INC_LOCAL));
        GROUPS.put("local_increased_physical_damage_percent_and_accuracy_rating", ModConflict.of(
                weaponsPlus(), ModAdditions.ADD_ACCURACY, ModAdditions.ATTACK_DAMAGE_INC,
                ModAdditions.PHYSICAL_DAMAGE_INC_LOCAL));
        GROUPS.put("increased_attack_speed", ModConflict.of(
                weaponsPlus(), ModAdditions.ATTACK_SPEED_INC, ModAdditions.ATTACK_SPEED_INC_LOCAL));
        GROUPS.put("cold_damage", ModConflict.of(
                weaponsPlus("ring"), ModAdditions.ADD_COLD_DAMAGE_MINMAX, ModAdditions.ADD_COLD_DAMAGE));
        GROUPS.put("fire_damage", ModConflict.of(
                weaponsPlus("ring"), ModAdditions.ADD_FIRE_DAMAGE_MINMAX, ModAdditions.ADD_FIRE_DAMAGE,
                ModAdditions.EXTRA_FIRE_DAMAGE));
        GROUPS.put("lightning_damage", ModConflict.of(
                weaponsPlus("ring"), ModAdditions.ADD_LIGHTNING_DAMAGE_MINMAX,
                ModAdditions.ADD_LIGHTNING_DAMAGE, ModAdditions.EXTRA_LIGHTNING_DAMAGE));
        GROUPS.put("physical_damage", ModConflict.of(
                weaponsPlus("ring"), ModAdditions.ADD_ATTACK_DAMAGE));
        // —— 首饰：全局攻击伤害 / 元素伤害% ——
        GROUPS.put("attack_damage", ModConflict.of(
                slots("amulet", "ring"), ModAdditions.ATTACK_DAMAGE_INC, ModAdditions.PHYSICAL_DAMAGE_INC_LOCAL));
        GROUPS.put("cold_damage_percentage", ModConflict.of(
                slots("ring"), ModAdditions.ATTACK_ELEMENTAL_DAMAGE_INC, ModAdditions.COLD_DAMAGE_INC,
                ModAdditions.WEAPON_ELEMENTAL_DAMAGE_PERCENT));
        GROUPS.put("fire_damage_percentage", ModConflict.of(
                slots("ring"), ModAdditions.ATTACK_ELEMENTAL_DAMAGE_INC, ModAdditions.FIRE_DAMAGE_INC,
                ModAdditions.WEAPON_ELEMENTAL_DAMAGE_PERCENT));
        GROUPS.put("lightning_damage_percentage", ModConflict.of(
                slots("ring"), ModAdditions.ATTACK_ELEMENTAL_DAMAGE_INC, ModAdditions.LIGHTNING_DAMAGE_INC,
                ModAdditions.WEAPON_ELEMENTAL_DAMAGE_PERCENT));
        GROUPS.put("weapon_damage_type_prefix", ModConflict.of(
                slots("foci"), ModAdditions.ATTACK_ELEMENTAL_DAMAGE_INC, ModAdditions.CHAOS_DAMAGE_INC,
                ModAdditions.COLD_DAMAGE_INC, ModAdditions.FIRE_DAMAGE_INC,
                ModAdditions.LIGHTNING_DAMAGE_INC, ModAdditions.WEAPON_ELEMENTAL_DAMAGE_PERCENT));
        // —— 盾 / focus：局部防御 ——
        GROUPS.put("base_local_defences", ModConflict.of(
                slots("buckler", "foci"), ModAdditions.ADD_ARMOR_LOCAL, ModAdditions.ADD_EVASION_LOCAL));
        GROUPS.put("defences_percent", ModConflict.of(
                slots("buckler", "foci"), ModAdditions.ARMOR_EVASION_INC_LOCAL,
                ModAdditions.ENERGY_EVASION_INC_LOCAL, ModAdditions.EVASION_INC_LOCAL));
        GROUPS.put("defences_percent_and_stun_threshold", ModConflict.of(
                slots("buckler"), ModAdditions.ADD_STUN_THRESHOLD, ModAdditions.ARMOR_EVASION_INC_LOCAL,
                ModAdditions.ENERGY_EVASION_INC_LOCAL, ModAdditions.EVASION_INC_LOCAL));
        GROUPS.put("base_local_defences_and_mana", ModConflict.of(
                slots("foci"), ModAdditions.ADD_MAX_MANA, ModAdditions.ARMOR_EVASION_INC_LOCAL,
                ModAdditions.ENERGY_EVASION_INC_LOCAL, ModAdditions.EVASION_INC_LOCAL));
        // —— 抗性（首饰 / 腰带 / 盾 / focus）——
        GROUPS.put("chaos_resistance", ModConflict.of(
                slots("amulet", "belt", "buckler", "foci", "ring"),
                ModAdditions.ADD_CHAOS_RESISTANCE, ModAdditions.ALL_RESISTANCES));
        GROUPS.put("cold_resistance", ModConflict.of(
                slots("amulet", "belt", "buckler", "foci", "ring"),
                ModAdditions.ADD_COLD_RESISTANCE, ModAdditions.ADD_ELEMENTAL_RESISTANCE,
                ModAdditions.ALL_RESISTANCES));
        GROUPS.put("fire_resistance", ModConflict.of(
                slots("amulet", "belt", "buckler", "foci", "ring"),
                ModAdditions.ADD_ELEMENTAL_RESISTANCE, ModAdditions.ADD_FIRE_RESISTANCE,
                ModAdditions.ALL_RESISTANCES));
        GROUPS.put("lightning_resistance", ModConflict.of(
                slots("amulet", "belt", "buckler", "foci", "ring"),
                ModAdditions.ADD_ELEMENTAL_RESISTANCE, ModAdditions.ADD_LIGHTNING_RESISTANCE,
                ModAdditions.ALL_RESISTANCES));
        // —— 属性 ——
        GROUPS.put("dexterity", ModConflict.of(
                slots("amulet", "belt", "bow", "buckler", "claw", "crossbow", "dagger", "foci",
                        "sword", "quarterstaff", "ring", "spear"),
                ModAdditions.ADD_DEXTERITY, ModAdditions.ALL_ATTRIBUTES));
        GROUPS.put("intelligence", ModConflict.of(
                slots("amulet", "belt", "buckler", "dagger", "flail", "foci", "quarterstaff",
                        "ring", "talisman"),
                ModAdditions.ADD_INTELLIGENCE, ModAdditions.ALL_ATTRIBUTES));
        GROUPS.put("strength", ModConflict.of(
                slots("amulet", "belt", "buckler", "crossbow", "flail", "foci", "axe", "mace",
                        "sword", "ring", "spear", "talisman"),
                ModAdditions.ADD_STRENGTH, ModAdditions.ALL_ATTRIBUTES));
        GROUPS.put("evasion_rating_percent", ModConflict.of(
                slots("amulet"), ModAdditions.ARMOR_EVASION_INC_LOCAL,
                ModAdditions.ENERGY_EVASION_INC_LOCAL, ModAdditions.EVASION_INC_LOCAL));
        // —— 源库无同名族（本表自造的并集组）：部位按「各成员所属真族」的部位取交集/并集 ——
        // base_local_defences(buckler,foci) ∪ defences_percent(buckler,foci) → 两组部位相同，取 buckler,foci
        GROUPS.put("base_local_defences_and_defence_percent", ModConflict.of(
                slots("buckler", "foci"), ModAdditions.ADD_ARMOR_LOCAL, ModAdditions.ADD_EVASION_LOCAL,
                ModAdditions.ARMOR_EVASION_INC_LOCAL, ModAdditions.ENERGY_EVASION_INC_LOCAL,
                ModAdditions.EVASION_INC_LOCAL));
        // defences_percent(buckler,foci) ∩ IncreasedLife(amulet,belt,buckler,ring) → 仅 buckler
        // （源库 buckler 上 DefencesPercent 与 IncreasedLife 确实同时出现，二者可共现）
        GROUPS.put("base_local_defences_and_life", ModConflict.of(
                slots("buckler"), ModAdditions.ADD_MAX_HEALTH, ModAdditions.ARMOR_EVASION_INC_LOCAL,
                ModAdditions.ENERGY_EVASION_INC_LOCAL, ModAdditions.EVASION_INC_LOCAL));
        // physical_damage_percent 已删除：源库无此族，且成员与 attack_damage 完全相同（纯重复）。
        // 真实覆盖由两个真族承担：AttackDamage(amulet,ring) 与 LocalPhysicalDamagePercent(武器)。
    }

    private final PackOutput output;

    public ConflictTableProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path dir = CoeDataPaths.coeRoot(output).resolve("affix/conflict");
        CompletableFuture<?>[] futures = new CompletableFuture[GROUPS.size()];
        int i = 0;
        for (Map.Entry<String, ModConflict> e : GROUPS.entrySet()) {
            DataResult<JsonElement> result = ModConflict.CODEC.encodeStart(JsonOps.INSTANCE, e.getValue());
            JsonElement json = result.result().orElseThrow(() ->
                    new IllegalStateException("无法编码冲突组: " + e.getKey()));
            futures[i++] = DataProvider.saveStable(cache, json, dir.resolve(e.getKey() + ".json"));
        }
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "Conflict Table";
    }
}
