package com.cimdy.coe.data.codec.item.state;

import com.cimdy.coe.data.codec.support.rarity.Rarity;
import net.minecraft.resources.Identifier;

import java.util.List;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
/**
 * 物品状态通用契约 —— 所有「可被通货 / 词缀 / 品质机制作用」的物品载体共享的通用维度。
 *
 * <p>装备（{@code EquipmentState}）、药剂（{@code FlaskState}）、护符（{@code CharmState}）
 * 等具体状态均继承 {@link CommonModifiableState}，从而天然实现本接口。本接口声明五个通用维度
 * （物等 / 稀有度 / 品质 / 词缀 / 类型链）及其协变改写方法，使这套维度成为跨物品类型的通用设施。</p>
 *
 * <p><b>消费口径（POE2 实证，poe2db.tw）——「通用」不等于「每类都用满」：</b></p>
 * <ul>
 *   <li><b>装备</b>：四维全消费 —— 物等定词缀 tier，稀有度定词缀档位与通货给予量递减，
 *       品质换算局部属性，词缀直写 {@code EquipmentState.mods}。</li>
 *   <li><b>药剂</b>：四维全消费 —— 物等（基底 {@code Requires: Level N}）决定可 roll 词缀 tier；
 *       稀有度仅 Normal→Magic（炼金石对药剂不可用，故无 Rare 档）；品质走玻璃珠
 *       （{@code Glassblower's Bauble}）；词缀上限一前缀 + 一后缀。</li>
 *   <li><b>护符</b>：与药剂同口径（物等 / Magic 稀有度 / 前后缀各一）；品质无对应通货，
 *       为惰性缺省。</li>
 * </ul>
 *
 * <p>通货 / 词缀作用设施因此可针对 {@link ModifiableState} 表达，而非硬编码于装备，使通用真正成为通用。</p>
 */
public interface ModifiableState {

    /** 物品等级（PoE2：决定可 roll 词缀的 tier 上限、通货给予量递减档位）。 */
    int level();

    /** 稀有度（PoE2：NONE / COMMON / MAGIC / RARE / UNIQUE；药剂 / 护符上限 MAGIC）。 */
    Rarity rarity();

    /** 品质（0–{@code MAX}；PoE2 通用标量，具体效果由各读取点分化）。 */
    int quality();

    /**
     * 词缀列表（前缀 / 后缀 / 额外 / 附魔，位置由 {@code Location} 区分）。
     * 装备 / 药剂 / 护符共享此维度：写入点统一走 {@link #withMods}，读取点由各载体分化。
     */
    List<ModifierEntry> mods();

    /**
     * 类型链（由 {@code ItemWeight} 类型树解析后缓存进状态）。装备 / 药剂 / 护符共用此维度，
     * 是槽位判定与机制门控的统一权威来源——一切「按物品类别判定」的读点都须读此字段，
     * 而非物品标签。链中元素为类型树路径段（如 {@code weapon}/{@code one_hand_sword}、
     * {@code flask}、{@code accessory/charm}），由 {@code ModGenerator#getItemType} 在初始化时写入。
     */
    List<Identifier> types();

    /**
     * 协变改写品质，返回同类型新实例（品质自动钳制到 0–{@code MAX}）。
     * 具体载体语义见各自读取点（{@code StateCalculator} / {@code FlaskHelper} / 技能品质加成）。
     */
    ModifiableState withQuality(int quality);

    /** 协变改写稀有度，返回同类型新实例。 */
    ModifiableState withRarity(Rarity rarity);

    /** 协变改写物等，返回同类型新实例。 */
    ModifiableState withLevel(int level);

    /** 协变改写词缀列表，返回同类型新实例（写入点统一入口，替代散落的 mods 直接重建）。 */
    ModifiableState withMods(List<ModifierEntry> mods);

    /**
     * 协变改写类型链，返回同类型新实例。运行时由 {@code ModGenerator#getItemType} 解析自
     * {@code ItemWeight} 类型树后写入；具体载体读取点（槽位判定 / 机制门控）统一消费此字段。
     */
    ModifiableState withType(List<Identifier> types);

    /**
     * 多态初始化：写入类型链，并按载体需要完成专有初始化（装备：智慧标记 + 需求计算）。
     * 默认实现仅写入类型链（药剂 / 护符 / 宝石等非装备载体）。返回写入后的新状态，
     * 调用方负责经 {@link ModifiableStateOps#set} 写回对应组件。
     */
    default ModifiableState initialize(RandomSource random, ItemStack stack, int level, Rarity rarity, List<Identifier> types) {
        return withType(types);
    }
}
