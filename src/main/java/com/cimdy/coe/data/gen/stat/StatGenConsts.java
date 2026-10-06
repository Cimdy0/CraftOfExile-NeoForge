package com.cimdy.coe.data.gen.stat;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.CompareValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.EquipmentValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.GemValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.LivingStateValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.MovementValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceRarityValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.status.StatusValue;
import com.cimdy.coe.data.codec.support.math.ComparisonOperator;

import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.event.EventName;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 数据生成共享缓存字段：消除 {@code gen/stat} 内裸数字 / 字符串字面量。
 *
 * <p>设计原则（对齐「常量→变量」收尾）：
 * <ul>
 *   <li>百分比 / 比例统一用命名字段（{@link #FULL}=100%、{@link #ZERO}=0%、{@link #CONVERT_FULL}=转换 100%），
 *       避免为 100% / 0% / 50% 各立一条 StatGen 条目；</li>
 *   <li>状态 / 事件 / 移动条件（{@code record} 不可变）抽为缓存字段，同时消去配对的字符串键与阈值；</li>
 *   <li>标签 / 事件键字符串抽为命名字段，消除裸字符串字面量。</li>
 * </ul>
 * 所有字段同包可见（gen/stat 域内文件直接 {@code StatGenConsts.X} 引用，无需新增 import）。
 */
public final class StatGenConsts {
    private StatGenConsts() {}

    // ═══ 幅度 / 比例常量 ═══
    /** 100%（REPLACE=1 / ADD 满额 / 转换满额）。 */
    public static final double FULL = 1.0;
    /** 0%（SET_VALUE 钉死 / 触发概率不触发 / 转换 0 份额）。 */
    public static final double ZERO = 0.0;
    /** 转换源值占比 100%（{@code Conversions.convertPercent(attr, 100.0)}）。 */
    public static final double CONVERT_FULL = 1.0;
    /** 傀儡师层数授予时长基数（tick）。 */
    public static final double PUPPET_MASTER_DURATION = 160.0;
    /** 移动条件窗口阈值（tick）。 */
    public static final double MOVEMENT_STATIONARY_WINDOW = 20.0;
    /** 状态 / 事件「近期」条件窗口阈值（tick，占时间比例）。 */
    public static final double LIVING_WINDOW = 80.0;

    // ═══ 标签 / 事件键字符串常量 ═══
    public static final String TAG_COMMAND = "command";
    public static final String TAG_PUPPET_MASTER = "puppet_master";
    public static final String TAG_GAIN_FRENZY = "gain_frenzy";
    public static final String TAG_GAIN_ENDURANCE = "gain_endurance";
    public static final String TAG_GAIN_POWER = "gain_power";
    public static final String TAG_GAIN_CHARGE = "gain_charge";
    public static final String TAG_VOLATILITY_DETONATED = "volatility_detonated";
    public static final String COMBO_DEFAULT = "default";

    // ═══ 状态条件缓存字段（LivingStateValue：mode + 窗口阈值） ═══
    public static final LivingStateValue LIVING_SHAPESHIFTED = new LivingStateValue("shapeshifted", LIVING_WINDOW);
    public static final LivingStateValue LIVING_LOW_LIFE = new LivingStateValue("low_life", LIVING_WINDOW);
    public static final LivingStateValue LIVING_BLOCKING = new LivingStateValue("blocking", LIVING_WINDOW);
    public static final LivingStateValue LIVING_PARRYING = new LivingStateValue("parrying", LIVING_WINDOW);
    public static final LivingStateValue LIVING_SURROUNDED = new LivingStateValue("surrounded", LIVING_WINDOW);
    public static final LivingStateValue LIVING_RECENTLY_TOOK_DOT = new LivingStateValue("recently_took_dot", LIVING_WINDOW);
    public static final LivingStateValue LIVING_STATIONARY = new LivingStateValue("stationary", LIVING_WINDOW);
    public static final LivingStateValue LIVING_ARCHON = new LivingStateValue("archon", LIVING_WINDOW);
    public static final LivingStateValue LIVING_NOT_FULL_LIFE = new LivingStateValue("!full_life", LIVING_WINDOW);
    /** 满血：当前生命 ≥ 最大生命（{@code LivingStateValue} case "full_life"），门控「满血时不被致盲」一族。 */
    public static final LivingStateValue LIVING_FULL_LIFE = new LivingStateValue("full_life", LIVING_WINDOW);
    /** 窗口为 0：当前 tick 即处于 surrounded 状态（区别于 80% 窗口的「近期」判定）。 */
    public static final LivingStateValue LIVING_SURROUNDED_NOW = new LivingStateValue("surrounded", ZERO);
    /** 副手空（无武器 / 盾 / 护符等）。 */
    public static final LivingStateValue LIVING_OFF_HAND_EMPTY = new LivingStateValue("off_hand_empty", LIVING_WINDOW);
    /** 主手持单手 martial 武器（剑 / 斧 / 锤）。 */
    public static final LivingStateValue LIVING_ONE_HANDED_MARTIAL = new LivingStateValue("one_handed_martial", LIVING_WINDOW);

    // ═══ 装备比较条件（CompareValue：头盔护甲 / 闪避互比） ═══
    /** 装备头盔护甲 > 闪避时输出 1（「头盔护甲高于闪避」免疫门控）。 */
    public static final CompareValue EQUIP_HELMET_ARMOR_GT_EVASION = new CompareValue(
            new EquipmentValue(Optional.of(List.of(EquipmentSlot.HEAD)), Optional.empty(), Optional.of("armor"), EquipmentValue.Aggregate.FIRST),
            new EquipmentValue(Optional.of(List.of(EquipmentSlot.HEAD)), Optional.empty(), Optional.of("evasion"), EquipmentValue.Aggregate.FIRST),
            1.0, 1.0, ComparisonOperator.GREATER_THAN);
    /** 装备头盔闪避 > 护甲时输出 1（「头盔闪避高于护甲」免疫门控）。 */
    public static final CompareValue EQUIP_HELMET_EVASION_GT_ARMOR = new CompareValue(
            new EquipmentValue(Optional.of(List.of(EquipmentSlot.HEAD)), Optional.empty(), Optional.of("evasion"), EquipmentValue.Aggregate.FIRST),
            new EquipmentValue(Optional.of(List.of(EquipmentSlot.HEAD)), Optional.empty(), Optional.of("armor"), EquipmentValue.Aggregate.FIRST),
            1.0, 1.0, ComparisonOperator.GREATER_THAN);

    // ═══ 镶嵌辅助宝石多数色条件（CompareValue：2×某色辅助宝石数 > 辅助宝石总数） ═══
    /** 镶嵌的蓝色辅助宝石占多数（2×蓝 > 辅助宝石总数）时输出 1。 */
    public static final CompareValue SOCKET_MAJORITY_BLUE = new CompareValue(
            new GemValue(Optional.empty(), Optional.empty(), Optional.of("blue"), "support", Optional.empty(), "count"),
            new GemValue(Optional.empty(), Optional.empty(), Optional.empty(), "support", Optional.empty(), "count"),
            2.0, 1.0, ComparisonOperator.GREATER_THAN);
    /** 镶嵌的红色辅助宝石占多数（2×红 > 辅助宝石总数）时输出 1。 */
    public static final CompareValue SOCKET_MAJORITY_RED = new CompareValue(
            new GemValue(Optional.empty(), Optional.empty(), Optional.of("red"), "support", Optional.empty(), "count"),
            new GemValue(Optional.empty(), Optional.empty(), Optional.empty(), "support", Optional.empty(), "count"),
            2.0, 1.0, ComparisonOperator.GREATER_THAN);
    /** 镶嵌的绿色辅助宝石占多数（2×绿 > 辅助宝石总数）时输出 1。 */
    public static final CompareValue SOCKET_MAJORITY_GREEN = new CompareValue(
            new GemValue(Optional.empty(), Optional.empty(), Optional.of("green"), "support", Optional.empty(), "count"),
            new GemValue(Optional.empty(), Optional.empty(), Optional.empty(), "support", Optional.empty(), "count"),
            2.0, 1.0, ComparisonOperator.GREATER_THAN);

    // ═══ 事件历史条件缓存字段（EventHistoryValue：事件名 + 模式 + 窗口阈值） ═══
    public static final EventHistoryValue EVENTHIST_KILL_RECENTLY =
            new EventHistoryValue(EventName.KILL, "recently", LIVING_WINDOW);
    public static final EventHistoryValue EVENTHIST_USE_ITEM_RECENTLY =
            new EventHistoryValue(EventName.USE_ITEM, "recently", LIVING_WINDOW);
    public static final EventHistoryValue EVENTHIST_TAKEN_HIT_RECENTLY =
            new EventHistoryValue(EventName.TAKEN_HIT, "recently", LIVING_WINDOW);
    public static final EventHistoryValue EVENTHIST_USE_TOTEM_RECENTLY =
            new EventHistoryValue(EventName.USE_TOTEM, "recently", LIVING_WINDOW);
    public static final EventHistoryValue EVENTHIST_TAKEN_STUN_RECENTLY =
            new EventHistoryValue(EventName.TAKEN_STUN, "recently", LIVING_WINDOW);
    /**
     * 近期消耗过力量球（{@code consume_power}）。
     *
     * <p>事件键<b>必须</b>经 {@link EventName#consumeCharge(String)} 生成而非写字面量：
     * 该 helper 与 {@code ChargeHelper} 的发布端共用同一拼接规则，写死字面量会在充能键命名调整时
     * 两端静默漂移（{@link com.cimdy.coe.event.EventCatalog#known} 仅校验 {@code consume_} 前缀，
     * 拼错后半段不会报错，只会永不命中）。
     */
    public static final EventHistoryValue EVENTHIST_CONSUME_POWER_RECENTLY =
            new EventHistoryValue(EventName.consumeCharge("power"), "recently", LIVING_WINDOW);

    // ═══ Presence 聚合条件缓存字段 ═══
    /**
     * 身边（Presence 范围内）存在稀有或传奇<b>敌方</b>实体时输出 1。
     *
     * <p>敌方判定复用 {@code TargetSelector.resolvePresenceEnemies} 的阵营规则（成员阵营非 NONE 且异于持有者）；
     * 稀有度读 {@code AttachmentRegistry.RARITY}（{@code GenEvent} 于怪物生成时写入）。
     * 节点实现 {@code PresenceDependent}，被引用即自动开启 Presence 全域扫描。
     */
    public static final PresenceRarityValue PRESENCE_RARE_OR_UNIQUE_ENEMY = PresenceRarityValue.rareOrUniqueEnemy();

    // ═══ 状态年龄 / 移动条件缓存字段 ═══
    public static final StatusValue STATUS_AGE_BLEED = StatusValue.byAgeThreshold("bleed", LIVING_WINDOW / 20.0);
    public static final MovementValue MOVEMENT_STATIONARY = new MovementValue("stationary", MOVEMENT_STATIONARY_WINDOW);

    // ═══ 子数据：伤害类型集合（转伤工厂 / 多源合并条目复用的「来源伤害类型」事实来源） ═══
    /**
     * 全部直接伤害类型（物理 / 混沌 / 火 / 冰 / 雷）。
     *
     * <p>转伤类条目的「来源类型」子数据统一收口此处，避免五类型组合在散落点重复硬编码、
     * 且任一处增删类型时静默漂移。注意：刻意排除 lightning 的 4 类型变体（EXTRA_FIRE/LIGHTNING_DAMAGE）
     * 不归一于此，保留各自内联集合以忠实原语义。
     */
    public static final Set<String> ALL_DAMAGE_TYPES = Set.of(
            GateTag.PHYSICAL, GateTag.CHAOS, GateTag.FIRE, GateTag.COLD, GateTag.LIGHTNING);

    // ═══ 子数据：scope 字符串键（消除裸字面量，复用 StatIds 式字段化引用） ═══
    /** 战吼技能类型 scope 键（余震 / 强化攻击族多条目复用）。 */
    public static final String TAG_SLAM = "slam";
    /** 偷取量作用域键（leech 的 amount 维度，{资源, leeching, amount} 作用域）。 */
    public static final String TAG_AMOUNT = "amount";

    // ═══ 子数据：投送（Delivery）通道 / 键（DeliveryStatEffect.entry 字符串参数） ═══
    /** 投送通道类型：投射物。 */
    public static final String DELIVERY_CHANNEL_PROJECTILE = "projectile";
    /** 投送键：额外连锁次数。 */
    public static final String DELIVERY_KEY_CHAIN = "chain_count";
    /** 投送键：额外投射物数。 */
    public static final String DELIVERY_KEY_ADDITIONAL_PROJECTILES = "additional_projectiles";
    /** 投送键：额外分裂次数。 */
    public static final String DELIVERY_KEY_FORK = "fork_count";
}
