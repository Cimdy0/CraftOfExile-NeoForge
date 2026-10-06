package com.cimdy.coe.item.currency;

import com.cimdy.coe.registry.item.ItemRegistry;

import com.cimdy.coe.data.codec.item.currency.CurrencyOperation;
import com.cimdy.coe.data.codec.item.currency.Operation;
import com.cimdy.coe.data.codec.item.currency.Precondition;
import com.cimdy.coe.data.codec.item.currency.TargetDomain;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.CoeConfig;
import com.cimdy.coe.data.helper.item.QualityOps;
import java.util.List;
import java.util.Set;

/**
 * 通货类型枚举（缓存的通货数据）。每个常量携带：注册名、堆叠上限、前置条件 + 操作，
 * 由 {@link ItemRegistry} 按枚举逐条注册 {@link BaseCurrency}，并由创造物品栏 / 模型生成枚举消费。
 *
 * <p>新增一种通货 = 在此增一个常量，无需再建子类文件；操作逻辑由 {@link OpType} 常量按数据驱动方式提供。</p>
 */
public enum CurrencyType {
    WISDOM_SCROLL("wisdom_scroll", 40,
            new CurrencyOperation(Precondition.ALLOW_ALL, Operation.generate())),
    PORTAL_SCROLL("portal_scroll", 40, CurrencyOperation.NONE),
    KALANDRA_MIRROR("kalandra_mirror", 10,
            new CurrencyOperation(Precondition.of(List.of(Rarity.MAGIC, Rarity.RARE), true, false, false), Operation.mirror())),
    CHANCE_ORB("chance_orb", 20,
            new CurrencyOperation(Precondition.of(Rarity.COMMON), Operation.chance())),
    SCOURING_ORB("scouring_orb", 30,
            new CurrencyOperation(new Precondition(Set.of(Rarity.COMMON, Rarity.MAGIC, Rarity.RARE), true, true, true), Operation.scrub())),
    // 炼金石：POE2 原文「Cannot be used on flasks / charms / relics」——故<b>刻意</b>留在装备域，
    // 不可放宽为 AFFIX_ITEM（这是药剂 / 护符永远到不了 Rare 的根因，须由数据侧守住）。
    ALCHEMY_ORB("alchemy_orb", 20,
            new CurrencyOperation(Precondition.of(Rarity.COMMON), Operation.upgrade(Rarity.RARE))),
    ALTERATION_ORB("alteration_orb", 20,
            new CurrencyOperation(Precondition.of(TargetDomain.AFFIX_ITEM, Rarity.MAGIC), Operation.rerollMods())),
    TRANSMUTATION_ORB("transmutation_orb", 40,
            new CurrencyOperation(Precondition.of(TargetDomain.AFFIX_ITEM, Rarity.COMMON), Operation.upgrade(Rarity.MAGIC))),
    // POE2 语义：移除一条词缀 + 新增一条词缀（非 POE1 全重 roll）。
    // 稀有度门槛按 poe2wiki 修正为「magic or rare」（原仅 RARE，会使 Magic 药剂 / 护符无法使用混沌石）。
    CHAOS_ORB("chaos_orb", 20,
            new CurrencyOperation(Precondition.of(TargetDomain.AFFIX_ITEM, Rarity.MAGIC, Rarity.RARE), Operation.swapMod())),
    // 富豪石：magic → rare。药剂 / 护符词缀上限为 2、无法成为 Rare，故留在装备域。
    REGAL_ORB("regal_orb", 20,
            new CurrencyOperation(Precondition.of(Rarity.MAGIC), Operation.upgrade(Rarity.RARE))),
    AUGMENTATION_ORB("augmentation_orb", 30,
            new CurrencyOperation(Precondition.of(TargetDomain.AFFIX_ITEM, Rarity.MAGIC), Operation.addMod())),
    // 崇高石：仅 rare——药剂 / 护符不可达，故留在装备域。
    EXALTED_ORB("exalted_orb", 20,
            new CurrencyOperation(Precondition.of(Rarity.RARE), Operation.addMod())),
    // 神圣石：poe2wiki 限制栏为空（仅「Randomises the values of explicit modifiers」），
    // 故门槛修正为 magic or rare，使 Magic 药剂 / 护符的词缀数值也可重掷。
    DIVINE_ORB("divine_orb", 20,
            new CurrencyOperation(Precondition.of(TargetDomain.AFFIX_ITEM, Rarity.MAGIC, Rarity.RARE), Operation.rerollValues())),
    ANNULMENT_ORB("annulment_orb", 20,
            new CurrencyOperation(Precondition.of(TargetDomain.AFFIX_ITEM, Rarity.MAGIC, Rarity.RARE), Operation.removeMod())),
    BLESSED_ORB("blessed_orb", 40,
            new CurrencyOperation(Precondition.of(List.of(Rarity.MAGIC, Rarity.RARE), true, false, true), Operation.rerollValues())),

    // ===================== 分阶通货（Greater / Perfect）=====================
    // POE2 机制「Minimum Modifier Level（最低词缀等级）」：每一阶动作与其基础通货完全一致，
    // 唯一区别是「新增」的词缀受等级下限约束（不保证词缀类型）；下限不提升已有词缀。
    // 下限表（POE2 0.5 实测）：蜕变 / 增幅 / 改造(伪造) = Greater 44 / Perfect 70；
    // 富豪 / 崇高 / 混沌 = Greater 35 / Perfect 50。核心家族无 Lesser 阶。
    // 分阶通货的域与基础阶严格一致（「每一阶动作相同，唯一区别是新词缀的等级下限」）。
    GREATER_TRANSMUTATION_ORB("greater_transmutation_orb", 40,
            new CurrencyOperation(Precondition.of(TargetDomain.AFFIX_ITEM, Rarity.COMMON), Operation.upgrade(Rarity.MAGIC, 44))),
    PERFECT_TRANSMUTATION_ORB("perfect_transmutation_orb", 40,
            new CurrencyOperation(Precondition.of(TargetDomain.AFFIX_ITEM, Rarity.COMMON), Operation.upgrade(Rarity.MAGIC, 70))),
    GREATER_AUGMENTATION_ORB("greater_augmentation_orb", 30,
            new CurrencyOperation(Precondition.of(TargetDomain.AFFIX_ITEM, Rarity.MAGIC), Operation.addMod(44))),
    PERFECT_AUGMENTATION_ORB("perfect_augmentation_orb", 30,
            new CurrencyOperation(Precondition.of(TargetDomain.AFFIX_ITEM, Rarity.MAGIC), Operation.addMod(70))),
    // 改造石：POE2 无此通货，本模组「伪造」分阶，行为为「增一减一」（移除 1 条 + 新增 1 条）
    GREATER_ALTERATION_ORB("greater_alteration_orb", 20,
            new CurrencyOperation(Precondition.of(TargetDomain.AFFIX_ITEM, Rarity.MAGIC), Operation.swapMod(44))),
    PERFECT_ALTERATION_ORB("perfect_alteration_orb", 20,
            new CurrencyOperation(Precondition.of(TargetDomain.AFFIX_ITEM, Rarity.MAGIC), Operation.swapMod(70))),
    GREATER_REGAL_ORB("greater_regal_orb", 20,
            new CurrencyOperation(Precondition.of(Rarity.MAGIC), Operation.upgrade(Rarity.RARE, 35))),
    PERFECT_REGAL_ORB("perfect_regal_orb", 20,
            new CurrencyOperation(Precondition.of(Rarity.MAGIC), Operation.upgrade(Rarity.RARE, 50))),
    GREATER_EXALTED_ORB("greater_exalted_orb", 20,
            new CurrencyOperation(Precondition.of(Rarity.RARE), Operation.addMod(35))),
    PERFECT_EXALTED_ORB("perfect_exalted_orb", 20,
            new CurrencyOperation(Precondition.of(Rarity.RARE), Operation.addMod(50))),
    GREATER_CHAOS_ORB("greater_chaos_orb", 20,
            new CurrencyOperation(Precondition.of(TargetDomain.AFFIX_ITEM, Rarity.MAGIC, Rarity.RARE), Operation.swapMod(35))),
    PERFECT_CHAOS_ORB("perfect_chaos_orb", 20,
            new CurrencyOperation(Precondition.of(TargetDomain.AFFIX_ITEM, Rarity.MAGIC, Rarity.RARE), Operation.swapMod(50))),

    // ===================== 品质通货（POE2 实证）=====================
    // 品质上限 20%；效果按载体分化：武术武器 = 1% more 局部物理伤害（磨刀石）、
    // 护甲 = 1% more 局部防御（护甲碎片）、药剂 = 1% more 局部生命/魔力回复（玻璃珠）、
    // 技能石 = 随技能而异（棱光石）。装备 / 药剂的单次给予量随物品等级递减（QualityOps 策略表）。
    GEMCUTTER_PRISM("gemcutter_prism", 20,
            new CurrencyOperation(Precondition.ofDomain(TargetDomain.SKILL_GEM),
                    Operation.addQuality(CoeConfig.qualityGemGrant))),
    BLACKSMITH_WHETSTONE("blacksmith_whetstone", 20,
            new CurrencyOperation(Precondition.ofDomain(TargetDomain.WEAPON), Operation.addQuality())),
    ARMOURER_SCRAP("armourer_scrap", 20,
            new CurrencyOperation(Precondition.ofDomain(TargetDomain.ARMOUR), Operation.addQuality())),
    GLASSBLOWER_BAUBLE("glassblower_bauble", 20,
            new CurrencyOperation(Precondition.ofDomain(TargetDomain.FLASK), Operation.addQuality())),

    // ===================== 孔通货（POE2 实证）=====================
    // 技能石辅助槽：初始 2 槽、上限 5 槽；次级 / 高阶 / 完美工匠石分别「置为」3 / 4 / 5 槽
    // （置位而非累加，且不可降级：当前槽数已达或超过目标则不可用）。POE2 无基准阶工匠石。
    LESSER_JEWELLER_ORB("lesser_jeweller_orb", 20,
            new CurrencyOperation(Precondition.ofDomain(TargetDomain.SKILL_GEM), Operation.setSkillSockets(3))),
    GREATER_JEWELLER_ORB("greater_jeweller_orb", 20,
            new CurrencyOperation(Precondition.ofDomain(TargetDomain.SKILL_GEM), Operation.setSkillSockets(4))),
    PERFECT_JEWELLER_ORB("perfect_jeweller_orb", 20,
            new CurrencyOperation(Precondition.ofDomain(TargetDomain.SKILL_GEM), Operation.setSkillSockets(5))),
    // 工匠石（Artificer's Orb）：给武器 / 护甲开一个物品孔（符文 / 魂核 / 其它 augment 共用），
    // 上限 = 双手武器与胸甲 2 个、单手武器与其它护甲 1 个。
    ARTIFICER_ORB("artificer_orb", 20,
            new CurrencyOperation(Precondition.ofDomain(TargetDomain.WEAPON_OR_ARMOUR), Operation.addSocket()));

    private final String id;
    private final int stackSize;
    private final CurrencyOperation operation;

    CurrencyType(String id, int stackSize, CurrencyOperation operation) {
        this.id = id;
        this.stackSize = stackSize;
        this.operation = operation;
    }

    public String id() {
        return id;
    }

    public int stackSize() {
        return stackSize;
    }

    public CurrencyOperation operation() {
        return operation;
    }
}
