package com.cimdy.coe.data.gen.source;

import com.cimdy.coe.data.gen.asset.RuneFamilyProvider;
import com.cimdy.coe.data.codec.item.addition.ModAdditions;
import com.cimdy.coe.data.codec.item.tiered.BonusFamily;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.cimdy.coe.data.codec.item.tiered.BonusFamily.bonus;

/**
 * 符文族<b>数据声明</b>（生成期的「存储类」）：一族一条 {@link BonusFamily}，
 * <b>含该族全部阶</b>（{@code LESSER → NORMAL → GREATER → PERFECT} 或单阶 {@code SPECIAL}）。
 *
 * <h2>与运行时存储类 {@code RuneState} 的分工</h2>
 * <ul>
 *   <li>本类（生成侧）：<b>同族所有阶</b>集中在一条 {@link BonusFamily} 里 —— 一族一个 JSON 文件
 *       （{@code data/coe/item/rune/<族名>.json}），表体是
 *       {@code "ladders": { "<部位>~<词缀>~<落位>": [ {"min","max","tier"}, … ] }}：
 *       <b>位置键（部位 × 落位）只写一次</b>，该位置各阶的<b>词缀与数值</b>作为 {@link TierBonus} 阶梯挂在值里；
 *       符文无落位，键第三段恒 {@code -}；</li>
 *   <li>{@code RuneState}（运行侧）：<b>只含对应那一阶</b>的数据 —— 由 {@code RuneRegistry}
 *       按阶展开时经 {@link BonusFamily#resolve} 抽出一阶的 {@code Map<String, List<Bonus>>}
 *       （已无 {@code tier} 字段），族名 / 阶 / 分支即该条目全部属性。</li>
 * </ul>
 * 二者因此<b>彼此分离</b>：本类不被任何运行时代码引用，只被 {@code RuneFamilyProvider} 读取落盘。
 *
 * <h2>建造者：按部位逐阶，部位键与词缀各写一次</h2>
 * 每次 {@code .tier(...)} 给出<b>本部位的下一阶</b>（从低往高）的多条加成；只调用一次即 {@code SPECIAL}。
 * 部位键 {@code .branch(...)} 只写一次，阶不必手写（由调用次序注入）。
 * 加成 {@code type} 直接给 <b>{@code ModAdditions} 常量</b>（{@code AdditionRead}），不写字符串 id —— 键写错在编译期即暴露；生成期只编码 ref、不解析。
 *
 * <p>族名（文件名）与条目 id 不入 JSON：族名由文件名承载，id / 需求等级 / 名称 / 模型路径
 * 全部由「族名 + 阶」派生（见 {@link com.cimdy.coe.data.codec.item.tiered.Tier}）。</p>
 */
public final class RuneFamilies {

    private RuneFamilies() {
    }

    /** 族名（键即文件名）→ 该族全部分阶加成。 */
    private static final Map<String, BonusFamily> FAMILIES = new LinkedHashMap<>();

    static {
        family("adept_rune", BonusFamily.builder()
                .branch("*")
                    .tier(bonus(ModAdditions.ADD_DEXTERITY, 6.0, 6.0))
                    .tier(bonus(ModAdditions.ADD_DEXTERITY, 9.0, 9.0))
                    .tier(bonus(ModAdditions.ADD_DEXTERITY, 12.0, 12.0))
                    .tier(bonus(ModAdditions.ADD_DEXTERITY, 15.0, 15.0))
                .branch("weapon, !wand, !staff, !off_hand")
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 6.0, 10.0), bonus(ModAdditions.EXTRA_LIGHTNING_DAMAGE, 1.0, 16.0))
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 6.0, 10.0), bonus(ModAdditions.EXTRA_LIGHTNING_DAMAGE, 1.0, 16.0))
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 6.0, 10.0), bonus(ModAdditions.EXTRA_LIGHTNING_DAMAGE, 1.0, 16.0))
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 6.0, 10.0), bonus(ModAdditions.EXTRA_LIGHTNING_DAMAGE, 1.0, 16.0))
                .branch("wand|staff")
                    .tier(bonus(ModAdditions.ADD_EVASION_LOCAL, 80.0, 80.0))
                    .tier(bonus(ModAdditions.ADD_EVASION_LOCAL, 80.0, 80.0))
                    .tier(bonus(ModAdditions.ADD_EVASION_LOCAL, 80.0, 80.0))
                    .tier(bonus(ModAdditions.ADD_EVASION_LOCAL, 80.0, 80.0))
                .branch("armor")
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                .build());

        family("body_rune", BonusFamily.builder()
                .branch("weapon, !wand, !staff, !off_hand")
                    .tier(bonus(ModAdditions.LEECH_HEALTH_INC, 3.0, 3.0), bonus(ModAdditions.MAX_HEALTH_INC, 5.0, 5.0))
                    .tier(bonus(ModAdditions.LEECH_HEALTH_INC, 4.0, 4.0), bonus(ModAdditions.MAX_HEALTH_INC, 5.0, 5.0))
                    .tier(bonus(ModAdditions.LEECH_HEALTH_INC, 5.0, 5.0), bonus(ModAdditions.MAX_HEALTH_INC, 5.0, 5.0))
                    .tier(bonus(ModAdditions.LEECH_HEALTH_INC, 6.0, 6.0), bonus(ModAdditions.MAX_HEALTH_INC, 5.0, 5.0))
                .branch("wand|staff")
                    .tier(bonus(ModAdditions.ADD_ENERGY_LOCAL, 30.0, 30.0), bonus(ModAdditions.MAX_HEALTH_INC, 5.0, 5.0))
                    .tier(bonus(ModAdditions.ADD_ENERGY_LOCAL, 40.0, 40.0), bonus(ModAdditions.MAX_HEALTH_INC, 5.0, 5.0))
                    .tier(bonus(ModAdditions.ADD_ENERGY_LOCAL, 50.0, 50.0), bonus(ModAdditions.MAX_HEALTH_INC, 5.0, 5.0))
                    .tier(bonus(ModAdditions.ADD_ENERGY_LOCAL, 60.0, 60.0), bonus(ModAdditions.MAX_HEALTH_INC, 5.0, 5.0))
                .branch("armor")
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 30.0, 30.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 45.0, 45.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 60.0, 60.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 75.0, 75.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                .build());

        family("charging_rune", BonusFamily.builder().build());

        family("desert_rune", BonusFamily.builder()
                .branch("weapon, !wand, !staff, !off_hand")
                    .tier(bonus(ModAdditions.ADD_FIRE_DAMAGE, 4.0, 6.0))
                    .tier(bonus(ModAdditions.ADD_FIRE_DAMAGE, 7.0, 11.0))
                    .tier(bonus(ModAdditions.ADD_FIRE_DAMAGE, 13.0, 16.0))
                    .tier(bonus(ModAdditions.ADD_FIRE_DAMAGE, 17.0, 20.0))
                .branch("wand|staff")
                    .tier(bonus(ModAdditions.EXTRA_FIRE_DAMAGE, 6.0, 6.0))
                    .tier(bonus(ModAdditions.EXTRA_FIRE_DAMAGE, 8.0, 8.0))
                    .tier(bonus(ModAdditions.EXTRA_FIRE_DAMAGE, 10.0, 10.0))
                    .tier(bonus(ModAdditions.EXTRA_FIRE_DAMAGE, 12.0, 12.0))
                .branch("armor")
                    .tier(bonus(ModAdditions.ADD_FIRE_RESISTANCE, 10.0, 10.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_FIRE_RESISTANCE, 14.0, 14.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_FIRE_RESISTANCE, 18.0, 18.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_FIRE_RESISTANCE, 22.0, 22.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                .build());

        family("glacial_rune", BonusFamily.builder()
                .branch("weapon, !wand, !staff, !off_hand")
                    .tier(bonus(ModAdditions.ADD_COLD_DAMAGE, 3.0, 5.0))
                    .tier(bonus(ModAdditions.ADD_COLD_DAMAGE, 6.0, 10.0))
                    .tier(bonus(ModAdditions.ADD_COLD_DAMAGE, 9.0, 15.0))
                    .tier(bonus(ModAdditions.ADD_COLD_DAMAGE, 16.0, 20.0))
                .branch("wand|staff")
                    .tier(bonus(ModAdditions.EXTRA_COLD_DAMAGE, 6.0, 6.0))
                    .tier(bonus(ModAdditions.EXTRA_COLD_DAMAGE, 8.0, 8.0))
                    .tier(bonus(ModAdditions.EXTRA_COLD_DAMAGE, 10.0, 10.0))
                    .tier(bonus(ModAdditions.EXTRA_COLD_DAMAGE, 12.0, 12.0))
                .branch("armor")
                    .tier(bonus(ModAdditions.ADD_COLD_RESISTANCE, 10.0, 10.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_COLD_RESISTANCE, 14.0, 14.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_COLD_RESISTANCE, 18.0, 18.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_COLD_RESISTANCE, 22.0, 22.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                .build());

        family("inspiration_rune", BonusFamily.builder()
                .branch("wand|staff")
                    .tier(bonus(ModAdditions.MANA_REGENERATION_RATE_INC, 20.0, 20.0))
                    .tier(bonus(ModAdditions.MANA_REGENERATION_RATE_INC, 25.0, 25.0))
                    .tier(bonus(ModAdditions.MANA_REGENERATION_RATE_INC, 30.0, 30.0))
                    .tier(bonus(ModAdditions.MANA_REGENERATION_RATE_INC, 35.0, 35.0))
                .branch("armor")
                    .tier(bonus(ModAdditions.MANA_REGENERATION_RATE_INC, 12.0, 12.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.MANA_REGENERATION_RATE_INC, 15.0, 15.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.MANA_REGENERATION_RATE_INC, 18.0, 18.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.MANA_REGENERATION_RATE_INC, 21.0, 21.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                .build());

        family("iron_rune", BonusFamily.builder()
                .branch("weapon, !wand, !staff, !off_hand")
                    .tier(bonus(ModAdditions.ATTACK_PHYSICAL_DAMAGE_INC, 14.0, 14.0))
                    .tier(bonus(ModAdditions.ATTACK_PHYSICAL_DAMAGE_INC, 16.0, 16.0))
                    .tier(bonus(ModAdditions.ATTACK_PHYSICAL_DAMAGE_INC, 18.0, 18.0))
                    .tier(bonus(ModAdditions.ATTACK_PHYSICAL_DAMAGE_INC, 20.0, 20.0))
                .branch("wand|staff")
                    .tier(bonus(ModAdditions.SPELL_DAMAGE_INC, 20.0, 20.0))
                    .tier(bonus(ModAdditions.SPELL_DAMAGE_INC, 25.0, 25.0))
                    .tier(bonus(ModAdditions.SPELL_DAMAGE_INC, 30.0, 30.0))
                    .tier(bonus(ModAdditions.SPELL_DAMAGE_INC, 35.0, 35.0))
                .branch("armor")
                    .tier(bonus(ModAdditions.ARMOR_INC_LOCAL, 14.0, 14.0), bonus(ModAdditions.EVASION_INC_LOCAL, 14.0, 14.0), bonus(ModAdditions.ENERGY_INC_LOCAL, 14.0, 14.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ARMOR_INC_LOCAL, 16.0, 16.0), bonus(ModAdditions.EVASION_INC_LOCAL, 16.0, 16.0), bonus(ModAdditions.ENERGY_INC_LOCAL, 16.0, 16.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ARMOR_INC_LOCAL, 18.0, 18.0), bonus(ModAdditions.EVASION_INC_LOCAL, 18.0, 18.0), bonus(ModAdditions.ENERGY_INC_LOCAL, 18.0, 18.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ARMOR_INC_LOCAL, 20.0, 20.0), bonus(ModAdditions.EVASION_INC_LOCAL, 20.0, 20.0), bonus(ModAdditions.ENERGY_INC_LOCAL, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                .build());

        family("masterwork_rune", BonusFamily.builder().build());

        family("mind_rune", BonusFamily.builder()
                .branch("weapon, !wand, !staff, !off_hand")
                    .tier(bonus(ModAdditions.LEECH_MANA_INC, 2.0, 2.0), bonus(ModAdditions.MAX_MANA_INC, 5.0, 5.0))
                    .tier(bonus(ModAdditions.LEECH_MANA_INC, 3.0, 3.0), bonus(ModAdditions.MAX_MANA_INC, 5.0, 5.0))
                    .tier(bonus(ModAdditions.LEECH_MANA_INC, 4.0, 4.0), bonus(ModAdditions.MAX_MANA_INC, 5.0, 5.0))
                    .tier(bonus(ModAdditions.LEECH_MANA_INC, 5.0, 5.0), bonus(ModAdditions.MAX_MANA_INC, 5.0, 5.0))
                .branch("wand|staff")
                    .tier(bonus(ModAdditions.ADD_MAX_MANA, 45.0, 45.0), bonus(ModAdditions.MAX_MANA_INC, 5.0, 5.0))
                    .tier(bonus(ModAdditions.ADD_MAX_MANA, 60.0, 60.0), bonus(ModAdditions.MAX_MANA_INC, 5.0, 5.0))
                    .tier(bonus(ModAdditions.ADD_MAX_MANA, 75.0, 75.0), bonus(ModAdditions.MAX_MANA_INC, 5.0, 5.0))
                    .tier(bonus(ModAdditions.ADD_MAX_MANA, 90.0, 90.0), bonus(ModAdditions.MAX_MANA_INC, 5.0, 5.0))
                .branch("armor")
                    .tier(bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_MANA, 30.0, 30.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_MANA, 40.0, 40.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_MANA, 50.0, 50.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0))
                .build());

        family("rebirth_rune", BonusFamily.builder()
                .branch("weapon, !wand, !staff, !off_hand")
                    .tier(bonus(ModAdditions.HEALTH_REGENERATION_PERCENTAGE_PER_SECOND, 0.4, 0.4))
                    .tier(bonus(ModAdditions.HEALTH_REGENERATION_PERCENTAGE_PER_SECOND, 0.4, 0.4))
                    .tier(bonus(ModAdditions.HEALTH_REGENERATION_PERCENTAGE_PER_SECOND, 0.4, 0.4))
                    .tier(bonus(ModAdditions.HEALTH_REGENERATION_PERCENTAGE_PER_SECOND, 0.4, 0.4))
                .branch("wand|staff")
                    .tier(bonus(ModAdditions.ENERGY_RECHARGE_RATE_INC, 6.0, 6.0))
                    .tier(bonus(ModAdditions.ENERGY_RECHARGE_RATE_INC, 8.0, 8.0))
                    .tier(bonus(ModAdditions.ENERGY_RECHARGE_RATE_INC, 10.0, 10.0))
                    .tier(bonus(ModAdditions.ENERGY_RECHARGE_RATE_INC, 12.0, 12.0))
                .branch("armor")
                    .tier(bonus(ModAdditions.HEALTH_REGENERATION_PERCENTAGE_PER_SECOND, 0.35, 0.35), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.HEALTH_REGENERATION_PERCENTAGE_PER_SECOND, 0.4, 0.4), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.HEALTH_REGENERATION_PERCENTAGE_PER_SECOND, 0.45, 0.45), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.HEALTH_REGENERATION_PERCENTAGE_PER_SECOND, 0.5, 0.5), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                .build());

        family("resolve_rune", BonusFamily.builder()
                .branch("*")
                    .tier(bonus(ModAdditions.ADD_INTELLIGENCE, 6.0, 6.0))
                    .tier(bonus(ModAdditions.ADD_INTELLIGENCE, 9.0, 9.0))
                    .tier(bonus(ModAdditions.ADD_INTELLIGENCE, 12.0, 12.0))
                    .tier(bonus(ModAdditions.ADD_INTELLIGENCE, 15.0, 15.0))
                .branch("weapon, !wand, !staff, !off_hand")
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 6.0, 10.0), bonus(ModAdditions.ADD_COLD_DAMAGE, 5.0, 8.0))
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 6.0, 10.0), bonus(ModAdditions.ADD_COLD_DAMAGE, 5.0, 8.0))
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 6.0, 10.0), bonus(ModAdditions.ADD_COLD_DAMAGE, 5.0, 8.0))
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 6.0, 10.0), bonus(ModAdditions.ADD_COLD_DAMAGE, 5.0, 8.0))
                .branch("wand|staff")
                    .tier(bonus(ModAdditions.ADD_ENERGY_LOCAL, 50.0, 50.0))
                    .tier(bonus(ModAdditions.ADD_ENERGY_LOCAL, 50.0, 50.0))
                    .tier(bonus(ModAdditions.ADD_ENERGY_LOCAL, 50.0, 50.0))
                    .tier(bonus(ModAdditions.ADD_ENERGY_LOCAL, 50.0, 50.0))
                .branch("armor")
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                .build());

        family("robust_rune", BonusFamily.builder()
                .branch("*")
                    .tier(bonus(ModAdditions.ADD_STRENGTH, 6.0, 6.0))
                    .tier(bonus(ModAdditions.ADD_STRENGTH, 9.0, 9.0))
                    .tier(bonus(ModAdditions.ADD_STRENGTH, 12.0, 12.0))
                    .tier(bonus(ModAdditions.ADD_STRENGTH, 15.0, 15.0))
                .branch("weapon, !wand, !staff, !off_hand")
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 6.0, 10.0), bonus(ModAdditions.ADD_FIRE_DAMAGE, 6.0, 10.0))
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 6.0, 10.0), bonus(ModAdditions.ADD_FIRE_DAMAGE, 6.0, 10.0))
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 6.0, 10.0), bonus(ModAdditions.ADD_FIRE_DAMAGE, 6.0, 10.0))
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 6.0, 10.0), bonus(ModAdditions.ADD_FIRE_DAMAGE, 6.0, 10.0))
                .branch("wand|staff")
                    .tier(bonus(ModAdditions.ADD_ARMOR_LOCAL, 100.0, 100.0))
                    .tier(bonus(ModAdditions.ADD_ARMOR_LOCAL, 100.0, 100.0))
                    .tier(bonus(ModAdditions.ADD_ARMOR_LOCAL, 100.0, 100.0))
                    .tier(bonus(ModAdditions.ADD_ARMOR_LOCAL, 100.0, 100.0))
                .branch("armor")
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                .build());

        family("stone_rune", BonusFamily.builder()
                .branch("weapon, !wand, !staff, !off_hand")
                    .tier(bonus(ModAdditions.CAUSES_STUN_BUILDUP_INC, 20.0, 20.0))
                    .tier(bonus(ModAdditions.CAUSES_STUN_BUILDUP_INC, 30.0, 30.0))
                    .tier(bonus(ModAdditions.CAUSES_STUN_BUILDUP_INC, 40.0, 40.0))
                    .tier(bonus(ModAdditions.CAUSES_STUN_BUILDUP_INC, 50.0, 50.0))
                .branch("armor")
                    .tier(bonus(ModAdditions.ADD_STUN_THRESHOLD, 50.0, 50.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_STUN_THRESHOLD, 75.0, 75.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_STUN_THRESHOLD, 100.0, 100.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_STUN_THRESHOLD, 125.0, 125.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                .build());

        family("storm_rune", BonusFamily.builder()
                .branch("weapon, !wand, !staff, !off_hand")
                    .tier(bonus(ModAdditions.EXTRA_LIGHTNING_DAMAGE, 1.0, 10.0))
                    .tier(bonus(ModAdditions.EXTRA_LIGHTNING_DAMAGE, 1.0, 20.0))
                    .tier(bonus(ModAdditions.EXTRA_LIGHTNING_DAMAGE, 1.0, 30.0))
                    .tier(bonus(ModAdditions.EXTRA_LIGHTNING_DAMAGE, 1.0, 40.0))
                .branch("wand|staff")
                    .tier(bonus(ModAdditions.EXTRA_LIGHTNING_DAMAGE, 6.0, 6.0))
                    .tier(bonus(ModAdditions.EXTRA_LIGHTNING_DAMAGE, 8.0, 8.0))
                    .tier(bonus(ModAdditions.EXTRA_LIGHTNING_DAMAGE, 10.0, 10.0))
                    .tier(bonus(ModAdditions.EXTRA_LIGHTNING_DAMAGE, 12.0, 12.0))
                .branch("armor")
                    .tier(bonus(ModAdditions.ADD_LIGHTNING_RESISTANCE, 10.0, 10.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_LIGHTNING_RESISTANCE, 14.0, 14.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_LIGHTNING_RESISTANCE, 18.0, 18.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_LIGHTNING_RESISTANCE, 22.0, 22.0), bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                .build());

        family("tempered_rune", BonusFamily.builder()
                .branch("weapon, !wand, !staff, !off_hand")
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 3.0, 4.0))
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 6.0, 9.0))
                    .tier(bonus(ModAdditions.ADD_PHYSICAL_DAMAGE, 9.0, 12.0))
                .build());

        family("vision_rune", BonusFamily.builder()
                .branch("weapon, !wand, !staff, !off_hand")
                    .tier(bonus(ModAdditions.ADD_ACCURACY, 60.0, 60.0), bonus(ModAdditions.CRIT_CHANCE_INC, 1.0, 1.0))
                    .tier(bonus(ModAdditions.ADD_ACCURACY, 90.0, 90.0), bonus(ModAdditions.CRIT_CHANCE_INC, 1.0, 1.0))
                    .tier(bonus(ModAdditions.ADD_ACCURACY, 120.0, 120.0), bonus(ModAdditions.CRIT_CHANCE_INC, 1.0, 1.0))
                    .tier(bonus(ModAdditions.ADD_ACCURACY, 150.0, 150.0), bonus(ModAdditions.CRIT_CHANCE_INC, 1.0, 1.0))
                .branch("wand|staff")
                    .tier(bonus(ModAdditions.ADD_CRIT_DAMAGE, 25.0, 25.0))
                    .tier(bonus(ModAdditions.ADD_CRIT_DAMAGE, 25.0, 25.0))
                    .tier(bonus(ModAdditions.ADD_CRIT_DAMAGE, 25.0, 25.0))
                    .tier(bonus(ModAdditions.ADD_CRIT_DAMAGE, 25.0, 25.0))
                .branch("armor")
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                    .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 20.0, 20.0), bonus(ModAdditions.ADD_MAX_MANA, 20.0, 20.0))
                .build());

        family("ward_rune", BonusFamily.builder()
                .branch("armor")
                    .tier(bonus(ModAdditions.ADD_MAX_RUNIC_WARD, 15.0, 15.0), bonus(ModAdditions.ARMOR_INC_LOCAL, 15.0, 15.0), bonus(ModAdditions.EVASION_INC_LOCAL, 15.0, 15.0), bonus(ModAdditions.ENERGY_INC_LOCAL, 15.0, 15.0))
                    .tier(bonus(ModAdditions.ADD_MAX_RUNIC_WARD, 20.0, 20.0), bonus(ModAdditions.ARMOR_INC_LOCAL, 15.0, 15.0), bonus(ModAdditions.EVASION_INC_LOCAL, 15.0, 15.0), bonus(ModAdditions.ENERGY_INC_LOCAL, 15.0, 15.0))
                    .tier(bonus(ModAdditions.ADD_MAX_RUNIC_WARD, 25.0, 25.0), bonus(ModAdditions.ARMOR_INC_LOCAL, 15.0, 15.0), bonus(ModAdditions.EVASION_INC_LOCAL, 15.0, 15.0), bonus(ModAdditions.ENERGY_INC_LOCAL, 15.0, 15.0))
                    .tier(bonus(ModAdditions.ADD_MAX_RUNIC_WARD, 30.0, 30.0), bonus(ModAdditions.ARMOR_INC_LOCAL, 15.0, 15.0), bonus(ModAdditions.EVASION_INC_LOCAL, 15.0, 15.0), bonus(ModAdditions.ENERGY_INC_LOCAL, 15.0, 15.0))
                .build());
    }

    private static void family(String family, BonusFamily definition) {
        FAMILIES.put(family, definition);
    }

    /** 全部符文族（生成期只读）。 */
    public static Map<String, BonusFamily> families() {
        return FAMILIES;
    }
}
