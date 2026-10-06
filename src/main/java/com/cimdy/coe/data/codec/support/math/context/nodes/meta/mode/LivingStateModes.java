package com.cimdy.coe.data.codec.support.math.context.nodes.meta.mode;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.LivingStateValue;

/**
 * 战斗状态读值节点模式常量（对应 {@link LivingStateValue#mode()} 的收口字符串）。
 * 与 {@code GateTag} / {@code EventName} 同风格：字段即 {@code String}，禁止在调用处手写字面量。
 */
public final class LivingStateModes {
    private LivingStateModes() {}

    public static final String CHANNELLING = "channelling";
    public static final String PARRYING = "parrying";
    public static final String LEECHING = "leeching";
    /** 仅「正在偷取法力」（{@link #LEECHING} 为生命/法力/能量三者的或，语义过宽）；镜像 LEECHING 的事件历史判定，只取 MANA 一支。 */
    public static final String LEECHING_MANA = "leeching_mana";
    public static final String DUAL_WIELD = "dual_wield";
    public static final String HOLDING_SHIELD = "holding_shield";
    public static final String SPRINTING = "sprinting";
    public static final String LOW_LIFE = "low_life";
    public static final String LOW_MANA = "low_mana";
    /** {@code !low_mana} 取反语义（LivingStateValue 通用解析 ! 前缀）。 */
    public static final String NOT_LOW_MANA = "!low_mana";
    /** 满魔力：当前魔力 ≥ 最大魔力（与 LOW_MANA 对称）。 */
    public static final String FULL_MANA = "full_mana";
    /** {@code !full_mana} 取反语义（LivingStateValue 通用解析 ! 前缀）。 */
    public static final String NOT_FULL_MANA = "!full_mana";
    public static final String BLOCKING = "blocking";
    public static final String SHAPESHIFTED = "shapeshifted";
    public static final String ARCHON = "archon";
    public static final String STATIONARY = "stationary";
    public static final String MOVING = "moving";
    public static final String SURROUNDED = "surrounded";
    public static final String FULL_LIFE = "full_life";
    public static final String OFF_HAND_EMPTY = "off_hand_empty";
    /** 双手皆空：主手与副手槽均无物品（{@link LivingStateValue} 中 = 主手空 且 副手空）。 */
    public static final String BOTH_HANDS_EMPTY = "both_hands_empty";
    public static final String ONE_HANDED_MARTIAL = "one_handed_martial";
    /** 主手持斧（斧类近战武器）；判定复用 {@link com.cimdy.coe.data.codec.item.mod.identity.ItemTypeChecker#isAxe}。 */
    public static final String WIELDING_AXE = "wielding_axe";
    /** 主手持长杖（quarterstaff 武器标签）；判定复用 {@link com.cimdy.coe.data.codec.support.stat.tag.GateTag#QUARTERSTAFF}。 */
    public static final String WIELDING_QUARTERSTAFF = "wielding_quarterstaff";
    /** 主手持近战武器（剑/斧/锤）；判定复用 {@link com.cimdy.coe.data.codec.item.mod.identity.ItemTypeChecker#isSword}/#isAxe/#isMace。 */
    public static final String WIELDING_MELEE_WEAPON = "wielding_melee_weapon";
    /** 主手持匕首（dagger 武器标签）；判定复用 {@link com.cimdy.coe.data.codec.support.stat.tag.GateTag#DAGGER}，与 {@code wielding_quarterstaff} 同口径（无 EquipmentGen 类型，仅以 ItemTag 标识）。 */
    public static final String WIELDING_DAGGER = "wielding_dagger";
    /** 主手持双手近战武器（coe:items/two_handed 标签，覆盖双手斧/锤/剑）；判定复用 {@link com.cimdy.coe.data.codec.support.stat.tag.GateTag#TWO_HANDED}，与 {@code wielding_dagger} 同口径。 */
    public static final String WIELDING_TWO_HANDED_MELEE = "wielding_two_handed_melee";
    public static final String DODGE_ROLLING = "dodge_rolling";
    public static final String RECENTLY_TOOK_DOT = "recently_took_dot";
    /** 近期获得过怒气：判定持有者 ExpressionContext 的 RAGE_GAINED timer 是否仍激活（获怒即 refreshTimer，等价「recently gained rage」）。 */
    public static final String RECENTLY_GAINED_RAGE = "recently_gained_rage";
    /**
     * 持有者的召唤物<b>全部</b>为同伴（Companion）：按 {@code TargetSelector.resolveMinions} 归属枚举「你的召唤物」，
     * 要求<b>至少一个</b>且每一个都带 {@code IS_COMPANION} 标记。
     * <p>注：Companion 在本模组是<b>召唤物上的标记</b>而非独立实体类，故不可按实体类型或
     * {@code presence_count:ally.summoned == ally.companion} 近似（SUMMONED 走 {@code coe:summoned} 持久键，
     * 与 IS_COMPANION 是两个独立标记、无子集关系）。无召唤物时判否，避免空集真空成立而白嫖加成。
     */
    public static final String ALL_MINIONS_ARE_COMPANIONS = "all_minions_are_companions";
    /** {@code !full_life} 的取反语义（站定/满血门控的相反分支）。 */
    public static final String NOT_FULL_LIFE = "!full_life";
}
