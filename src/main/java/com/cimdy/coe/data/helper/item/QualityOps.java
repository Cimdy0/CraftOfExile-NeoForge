package com.cimdy.coe.data.helper.item;

import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.support.value.ModifierValue;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.CoeConfig;
import com.cimdy.coe.data.helper.mod.AttributeApplicator;import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * 品质（Quality）通用设施 —— 装备 / 药剂 / 护符 / 技能石四类载体共用一套读写口径。
 * 前三者经 {@link ModifiableStateOps} 按组件分派（共享 {@link ModifiableState} 契约），技能石走 {@code SKILL_DATA}。
 *
 * <p>POE2 实证（wiki Quality 页）：</p>
 * <ul>
 *   <li>品质上限：绝大多数物品 20%（Breach Ring 等例外由数据侧另行声明）。</li>
 *   <li>每 1% 品质的效果按物品类型分化：武术武器 = 1% more 物理伤害；护甲 = 1% more 局部防御；
 *       药剂 = 1% more 局部生命 / 魔力回复；技能石 = 随技能而异（由技能数据自身承载）。</li>
 *   <li>棱光石（Gemcutter's Prism）恒给技能石 5% 品质。</li>
 *   <li>装备 / 药剂的品质通货给予量随物品等级升高而递减。</li>
 * </ul>
 *
 * <p>效果侧（伤害 / 防御 / 回复量）不在本类计算——品质只作为一个标量写入载体，
 * 由各自的读取点（{@code StateCalculator} / {@code FlaskHelper} / 技能品质加成）统一消费，
 * 保证「品质 = 一个通用标量 + 各域自己的消费口径」，而非散落的一次性改写。</p>
 */
public final class QualityOps {

    private QualityOps() {
    }

    /** 品质上限（POE2 实证：20%）。 */

    /** 棱光石恒给技能石的品质增量（POE2 wiki：Gemcutter's Prisms always add 5%）。 */

    /**
     * 受品质影响的装备局部属性键（POE2：武术武器 = 局部物理伤害，护甲 = 局部防御）。
     * 单一权威来源：tooltip 读取点（{@code StateCalculator}）与 Stat 注入点
     * （{@code EquipmentRead#baseStatEntries}）共用，新增受品质影响的键只需在此加一次。
     */
    public static final Set<String> EQUIPMENT_KEYS =
            Set.of("attack_damage", "armor", "evasion", "energy", "runic_ward");

    /** 品质换算系数：每 1% 品质 = 1% more 局部属性。 */
    public static double multiplier(int quality) {
        return 1.0 + Math.max(0, quality) / 100.0;
    }

    /**
     * 品质通货的「按稀有度递减」给予量表（poewiki Quality 页实证）：
     * <b>普通 +5%、魔法 +2%、稀有与传奇 +1%</b>，上限同为 20%。
     * 因此稀有度越高、单次给得越少（越难堆满），这与「稀有以上最低 1%」一致。
     */

    /** 按稀有度查出本次品质通货的给予量。 */
    public static int grantForRarity(Rarity rarity) {
        return switch (rarity) {
            case NONE, COMMON -> CoeConfig.qualityGrantCommon;
            case MAGIC -> CoeConfig.qualityGrantMagic;
            case RARE, UNIQUE -> CoeConfig.qualityGrantRare;
        };
    }

    /**
     * 读取任意载体的当前品质（非对应载体恒为 0）。
     * 技能石走 {@code SKILL_DATA}；装备 / 药剂 / 护符统一走 {@link ModifiableStateOps} 按组件分派。
     */
    public static int of(ItemStack stack) {
        SkillData skill = stack.getOrDefault(DataComponentRegistry.SKILL_DATA, SkillData.EMPTY);
        if (!skill.isEmpty()) {
            return skill.quality().get();
        }
        return ModifiableStateOps.quality(stack);
    }

    /** 品质是否已达上限（品质通货的守卫）。 */
    public static boolean isMaxed(ItemStack stack) {
        return of(stack) >= CoeConfig.maxQuality;
    }

    /**
     * 给任意载体增加品质（按载体类型分派写入）。
     *
     * @param amount 计划增量；实际增量会自动钳制到 {@link CoeConfig#maxQuality} 上限内。
     * @return 实际增加的数值（0 表示已满 / 非品质载体）。
     */
    public static int add(ItemStack stack, int amount) {
        SkillData skill = stack.getOrDefault(DataComponentRegistry.SKILL_DATA, SkillData.EMPTY);
        if (!skill.isEmpty()) {
            int next = clamp(skill.quality().get() + amount);
            int delta = next - skill.quality().get();
            stack.set(DataComponentRegistry.SKILL_DATA, withQuality(skill, next));
            return delta;
        }
        // 装备 / 药剂 / 护符：统一经通用载体分派写入（不再对具体组件做 if 分支）
        ModifiableState state = ModifiableStateOps.get(stack).orElse(null);
        if (state == null) {
            return 0;
        }
        int next = clamp(state.quality() + amount);
        int delta = next - state.quality();
        if (delta != 0) {
            ModifiableStateOps.set(stack, state.withQuality(next));
            // 装备：与词缀链路同口径，改写状态后重烤 Stat 条目，品质换算才真正落到注入的基底条目上；
            // 药剂 / 护符的品质由各自读取点消费（FlaskHelper.qualityScaledRecovery 等）。
            if (state instanceof EquipmentState) {
                AttributeApplicator.applyModValue(stack);
            }
        }
        return delta;
    }

    /**
     * 本次品质通货的给予量：技能石恒 5%（棱光石），其余载体按稀有度查表。
     * 药剂 / 护符的稀有度上限为 Magic（炼金石不可用），正常态为 {@link Rarity#COMMON}。
     */
    public static int grantFor(ItemStack stack) {
        SkillData skill = stack.getOrDefault(DataComponentRegistry.SKILL_DATA, SkillData.EMPTY);
        if (!skill.isEmpty()) {
            return CoeConfig.qualityGemGrant;
        }
        return grantForRarity(ModifiableStateOps.rarity(stack));
    }

    public static int clamp(int quality) {
        return Math.clamp(quality, 0, CoeConfig.maxQuality);
    }

    /** 技能石品质写入（保留 change 分量：外部「+% 品质」加成不被通货覆盖）。 */
    private static SkillData withQuality(SkillData data, int quality) {
        return new SkillData(
                data.id(),
                data.status(),
                data.level(),
                ModifierValue.Int.create(quality, data.quality().change()),
                data.sockets(),
                data.supports(),
                data.spirit());
    }
}
