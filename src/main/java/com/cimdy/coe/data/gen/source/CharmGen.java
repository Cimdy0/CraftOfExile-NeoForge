package com.cimdy.coe.data.gen.source;

import com.cimdy.coe.data.gen.stat.wirings.wire.attribute.WireDefense;
import com.cimdy.coe.data.gen.stat.wirings.wire.flow.WireMisc;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.state.CharmState;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.gen.stat.StatGen;
import com.cimdy.coe.item.charm.CharmTrigger;

import com.cimdy.coe.registry.item.ItemRegistry;
import java.util.List;

/**
 * 护符（Charm）数据驱动 CODEC 工厂 —— 镜像 {@code SkillFactory} / {@code EquipmentGen} 范式：
 * 以 Java 工厂方法<b>数据生成</b>各护符的完整 {@link CharmState}（触发条件 + 效果 {@link StatEntry} 列表
 * + 时长 + 消耗充能），由 {@code CharmState.CODEC} 序列化进护符物品的
 * {@code charm_item_state} 数据组件。
 *
 * <p>效果全部复用既有的 {@code coe:charm_*} 数据模板（在 {@code WireDefense}/{@code WireMisc} 注册，
 * 数值以 {@code $value} 占位）。本工厂<b>只做引用与幅度注入</b>（{@code StatGen.ref(id, value)}），
 * 不重复定义任何 Stat 条目、不注册任何 {@code StatusType}——护符由此彻底数据驱动，
 * 不再依赖硬编码的 {@code CharmStatusTypes} 常量。
 *
 * <p>幅度基准取 POE2 原值：抗性 +25%（混沌 +18%）、Golden 稀有度 +20%；免疫类无幅度（仅声明免疫）。
 * 运行时「Charms applied to you have X% increased Effect」由 {@code CharmEngine} 按玩家
 * {@code CHARM_EFFECT} 属性在触发时统一缩放（保留每实体 per-entity 真读，R3 安全），
 * 故此处只写<b>基准幅度</b>。
 */
public final class CharmGen {

    private CharmGen() {
    }

    // ───────── 抗性护符：受对应元素伤害时 +X% 该元素抗性（4 秒） ─────────

    public static final CharmState RUBY = CharmState.of(
            CharmTrigger.FIRE, List.of(StatGen.ref(StatIds.CHARM_RESIST_FIRE, 0.25)),
            "charm.coe.charm_ruby_effect", 80, 40);
    public static final CharmState SAPPHIRE = CharmState.of(
            CharmTrigger.COLD, List.of(StatGen.ref(StatIds.CHARM_RESIST_COLD, 0.25)),
            "charm.coe.charm_sapphire_effect", 80, 40);
    public static final CharmState TOPAZ = CharmState.of(
            CharmTrigger.LIGHTNING, List.of(StatGen.ref(StatIds.CHARM_RESIST_LIGHTNING, 0.25)),
            "charm.coe.charm_topaz_effect", 80, 40);
    public static final CharmState AMETHYST = CharmState.of(
            CharmTrigger.CHAOS, List.of(StatGen.ref(StatIds.CHARM_RESIST_CHAOS, 0.18)),
            "charm.coe.charm_amethyst_effect", 80, 60);

    // ───────── 免疫护符：触发时免疫对应异常/控制（3 秒级，无幅度） ─────────

    public static final CharmState DOUSING = CharmState.of(
            CharmTrigger.IGNITE, List.of(StatGen.ref(StatIds.CHARM_IMMUNE_DOUSING)),
            "charm.coe.charm_dousing_effect", 60, 60);
    public static final CharmState THAWING = CharmState.of(
            CharmTrigger.FREEZE, List.of(StatGen.ref(StatIds.CHARM_IMMUNE_THAWING)),
            "charm.coe.charm_thawing_effect", 60, 80);
    public static final CharmState GROUNDING = CharmState.of(
            CharmTrigger.SHOCK, List.of(StatGen.ref(StatIds.CHARM_IMMUNE_GROUNDING)),
            "charm.coe.charm_grounding_effect", 60, 50);
    public static final CharmState STAUNCHING = CharmState.of(
            CharmTrigger.BLEED, List.of(StatGen.ref(StatIds.CHARM_IMMUNE_STAUNCHING)),
            "charm.coe.charm_staunching_effect", 60, 60);
    public static final CharmState ANTIDOTE = CharmState.of(
            CharmTrigger.POISON, List.of(StatGen.ref(StatIds.CHARM_IMMUNE_ANTIDOTE)),
            "charm.coe.charm_antidote_effect", 60, 40);
    public static final CharmState STONE = CharmState.of(
            CharmTrigger.STUN, List.of(StatGen.ref(StatIds.CHARM_IMMUNE_STONE)),
            "charm.coe.charm_stone_effect", 50, 40);
    public static final CharmState SILVER = CharmState.of(
            CharmTrigger.SLOW, List.of(StatGen.ref(StatIds.CHARM_IMMUNE_SILVER)),
            "charm.coe.charm_silver_effect", 60, 40);

    // ───────── 功能护符：Golden（击杀稀有/独特敌人 +20% 掉落稀有度，1 秒） ─────────

    public static final CharmState GOLDEN = CharmState.of(
            CharmTrigger.KILL_RARE_UNIQUE, List.of(StatGen.ref(StatIds.CHARM_GOLDEN_RARITY, 0.20)),
            "charm.coe.charm_golden_effect", 20, 80);

    /** 全部护符状态（供 {@code ItemRegistry} 引用）。 */
    public static final List<CharmState> ALL = List.of(
            RUBY, SAPPHIRE, TOPAZ, AMETHYST,
            DOUSING, THAWING, GROUNDING, STAUNCHING, ANTIDOTE, STONE, SILVER,
            GOLDEN);
}
