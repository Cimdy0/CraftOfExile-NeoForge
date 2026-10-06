package com.cimdy.coe.data.gen.gem.support;

import com.cimdy.coe.data.codec.skill.modifier.impl.damage.AddedDamage;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.AdditionalActionEffect;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.Convert;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.DeliveryMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.EchoMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EffectMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EventListenEffect;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.Exposure;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.Penetration;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.SealMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.Slow;
import com.cimdy.coe.data.codec.skill.helper.state.EchoState;
import com.cimdy.coe.data.codec.skill.helper.cast.SealRelease;
import com.cimdy.coe.data.codec.skill.helper.cast.SkillCast;
import com.cimdy.coe.data.codec.skill.gem.SupportRequire;
import com.cimdy.coe.data.codec.skill.modifier.*;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.*;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.*;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.*;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.action.damage.StatAction;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.skill.phase.action.MetaSkillAction;
import com.cimdy.coe.data.codec.skill.phase.event.EventListenPhase;
import com.cimdy.coe.data.codec.skill.phase.event.SkillEventType;
import com.cimdy.coe.data.codec.skill.support.SpecialMod;
import com.cimdy.coe.data.codec.skill.support.SupportGem;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.effect.status.StatusStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatGroup;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.event.EventName;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 辅助宝石构造工厂 — 用共享的 {@link SkillModifier} 形状（DeliveryMod / EffectMod / AdditionalActionEffect）
 * 与辅助独有的 {@link SpecialMod} 组合出合法 {@link SupportGem}。
 *
 * <p>关键点：这里构造的「运行时修正」（effects）与技能自身的 modifiers 完全等价——
 * 二者共用同一套 {@code SkillModifier} 形状，因此辅助宝石能做的事（斩杀 / 附加伤害 / 增伤 / 穿透 /
 * 额外投射物 / 范围扩大），技能本身也能通过自身 modifiers 做到，反之亦然。
 *
 * <p>数值对齐 {@code poe2wiki.net}（0.3 / Third Edict）：
 * <ul>
 *   <li>普通辅助宝石<b>不</b>给链接技能加等级/品质（那是 Enhance 类专属），故 {@code special} 多为空；</li>
 *   <li>消耗倍率与保留倍率为两个独立字段（manaCostMultiplier / reservationMultiplier）；
 *       POE2 中「Cost &amp; Reservation Multiplier」是同一数值，普通宝石两者同值（默认 1.3），
 *       但本模型允许「仅降低消耗」或「仅降低保留」的宝石分别设置；</li>
 *   <li>属性需求为对应颜色 +5（红=力量 / 绿=敏捷 / 蓝=智慧）。</li>
 * </ul>
 */
public final class SupportGemFactory {
    private SupportGemFactory() {}

    /**
     * M5 数据填充：辅助宝石 Allow Type 查表（tag → 经 {@link #POE2_TO_MOD_TAG} 归一后的 mod 标签列表）。
     * 数据派生自 {@code design/refactor/skill-data/crawled/support_scope.json}（594 条全量抓取，
     * 后缀布尔表达式 AND/OR，由 gen 脚本生成）；装配期由 {@link #of} 按 tag 自动注入，
     * 零改动 78 处调用点。未登记 tag（含 34 个无 crawl allow_type 的辅助宝石）→ 空（fail-open 支持一切）。
     */
    private static final Map<String, List<String>> SUPPORT_ALLOW_TYPES = Map.ofEntries(
            Map.entry("ambush", List.of("attack", "damage", "crossbow")),
            Map.entry("astral_projection", List.of("nova")),
            Map.entry("battershout", List.of("warcry")),
            Map.entry("blindside", List.of("attack", "damage", "crossbow")),
            Map.entry("bursting_plague", List.of("damage", "attack", "crossbow")),
            Map.entry("cirels_cultivation", List.of("summonstotem", "usedbytotem")),
            Map.entry("clash", List.of("attack", "melee", "AND")),
            Map.entry("cold_penetration", List.of("damage", "attack", "crossbow")),
            Map.entry("concentrated_area", List.of("area", "minionscanexplode")),
            Map.entry("echoing_cry", List.of("warcry")),
            Map.entry("elemental_focus", List.of("damage", "attack", "crossbow")),
            Map.entry("encroaching_ground", List.of("createsgroundeffect")),
            Map.entry("expand", List.of("spell", "area", "AND")),
            Map.entry("expanse", List.of("area", "minionscanexplode")),
            Map.entry("fork", List.of("projectile")),
            Map.entry("freezefork", List.of("projectile")),
            Map.entry("gambleshot", List.of("projectile")),
            Map.entry("garukhans_resolve", List.of("attack")),
            Map.entry("heavy_swing", List.of("melee")),
            Map.entry("ixchels_torment", List.of("triggers")),
            Map.entry("kaoms_madness", List.of("createsfissure")),
            Map.entry("lightning_penetration", List.of("damage", "attack", "crossbow")),
            Map.entry("morganas_tempest", List.of("storm")),
            Map.entry("murderous_intent", List.of("empowersotherskill")),
            Map.entry("overextend", List.of("attack")),
            Map.entry("overreach", List.of("attack", "area", "AND")),
            Map.entry("pinpoint_critical", List.of("damage", "attack", "crossbow")),
            Map.entry("rigwalds_ferocity", List.of("attack")),
            Map.entry("rising_tempest", List.of("damage", "attack", "crossbow")),
            Map.entry("ritualistic_curse", List.of("appliescurse")),
            Map.entry("spectral_volley", List.of("bow", "attack", "AND", "spear", "projectile", "OR")),
            Map.entry("spell_cascade", List.of("cascadable")),
            Map.entry("spell_echo", List.of("cascadable")),
            Map.entry("streamlined_rounds", List.of("crossbow")),
            Map.entry("supercritical", List.of("damage", "attack", "crossbow")),
            Map.entry("tectonic_slams", List.of("slam")),
            Map.entry("unleash", List.of("unleashable", "spell", "AND")),
            Map.entry("vruuns_inevitability", List.of("attack", "melee", "AND")),
            Map.entry("withering_touch", List.of("damage", "attack", "crossbow")),
            Map.entry("zarokhs_refrain", List.of("cascadable")),
            Map.entry("zarokhs_revolt", List.of("unleashable", "spell", "AND")),
            Map.entry("zerphis_infamy", List.of("attack", "warcry"))
    );

    /** 取辅助宝石的 allow_type（无则空，fail-open 支持一切）。 */
    private static Optional<List<String>> allowTypesFor(String tag) {
        return Optional.ofNullable(SUPPORT_ALLOW_TYPES.get(tag));
    }

    /**
     * 完整构造一个辅助宝石（显式拆分消耗与保留倍率）。
     * @param require              链接时累加到技能上的额外属性需求（POE2 辅助宝石自身无需求，仅向技能追加 +5 属性）
     * @param tag                  辅助宝石标签（用于 UI / 分类）
     * @param effects              运行时修正（DeliveryMod / EffectMod / AdditionalActionEffect），与技能共用形状
     * @param special              装配期特殊修正（等级/品质/速度）；普通辅助宝石传 null（POE2 中仅 Enhance 类才有）
     * @param manaCostMultiplier   魔力消耗倍率（作用于 MANA/ENERGY/HEALTH 消耗）
     * @param reservationMultiplier 精魂保留倍率（仅作用于 SPIRIT 保留）
     * @param maxLevel             最大等级（POE2 辅助宝石 20 级）
     * @param lineage              是否为传承（lineage）辅助
     */
    public static SupportGem of(SupportRequire require, String tag, List<SkillModifier> effects,
                                SpecialMod special, double manaCostMultiplier, double reservationMultiplier,
                                double maxLevel, boolean lineage) {
        return new SupportGem(require, Optional.of(tag), allowTypesFor(tag), effects,
                Optional.ofNullable(special),
                Optional.of(ExpressionCodec.of(manaCostMultiplier)),
                Optional.of(ExpressionCodec.of(reservationMultiplier)),
                Optional.of(ExpressionCodec.of(maxLevel)),
                Optional.of(lineage), 1, 1, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    /**
     * 便捷构造：POE2 中「Cost &amp; Reservation Multiplier」是同一个数值，同时作用于魔力消耗与精魂保留。
     * 本重载将该数值同时填入 {@code manaCostMultiplier} 与 {@code reservationMultiplier}。
     * @param costMultiplier 消耗 & 保留倍率（POE2 默认 1.3）
     */
    public static SupportGem of(SupportRequire require, String tag, List<SkillModifier> effects,
                                SpecialMod special, double costMultiplier, double maxLevel, boolean lineage) {
        return of(require, tag, effects, special, costMultiplier, costMultiplier, maxLevel, lineage);
    }

    // ===================== 运行时修正（与技能共用 SkillModifier） =====================

    /** 全局 more%（不限定伤害类型）。 */
    public static EffectMod more(double more) {
        return effectMod(Optional.empty(), Optional.empty(), Optional.of(more), Optional.empty());
    }

    /** 指定伤害类型的 more%（如火焰 +25%）。 */
    public static EffectMod more(String damageType, double more) {
        return effectMod(Optional.empty(), Optional.of(damageType), Optional.of(more), Optional.empty());
    }

    /** 指定投递类型的 more%（如仅投射物 +25%）。 */
    public static EffectMod moreDelivery(String delivery, double more) {
        return effectMod(Optional.of(delivery), Optional.empty(), Optional.of(more), Optional.empty());
    }

    /** 全局 less%（如连锁 30% less → less(0.7)）。 */
    public static EffectMod less(double less) {
        return effectMod(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(less));
    }

    /** 禁止造成指定类型的伤害（Brutality 的「无元素伤害」对每个元素类型各加一条）。 */
    public static EffectMod suppress(String damageType) {
        return new EffectMod(Optional.empty(), Optional.of(damageType), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                true, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** Brutality 专用：禁止造成全部元素伤害（火/冰/雷/混沌），保留物理。 */
    public static List<EffectMod> suppressElemental() {
        return List.of(suppress("fire"), suppress("cold"), suppress("lightning"), suppress("chaos"));
    }

    /** 转伤（结构化：from → to，ratio 比例；added_as_extra=false 时原类型被替换）。 */
    public static EffectMod convert(String from, String to, double ratio) {
        Convert c = new Convert(from, to, ExpressionCodec.of(ratio), Optional.empty());
        return new EffectMod(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                false, Optional.empty(), Optional.of(c), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    /**
     * 斩杀阈值（Culling Strike）。POE2 实际为按怪物稀有度分级（普通 35% / 魔法 20% / 稀有 10% / 传奇 5%），
     * 本模型仅支持单阈值，此处取稀有怪阈值 10%（最常见的「斩杀」参考值）。
     * 技能与辅助共用同一 EffectMod 形状。
     */
    public static EffectMod culling(double threshold) {
        return new EffectMod(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                false, Optional.empty(), Optional.empty(), Optional.of(ExpressionCodec.of(threshold)),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** 附加伤害（结构化：伤害类型 + 随机区间 [min, max]）。 */
    public static EffectMod addedDamage(String damageType, double min, double max) {
        AddedDamage ad = new AddedDamage(damageType, ExpressionCodec.of(min), ExpressionCodec.of(max));
        return new EffectMod(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                false, Optional.of(ad), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** 附加伤害（按等级查表/表达式缩放，用于 Added X Damage 类辅助宝石随等级成长）。 */
    public static EffectMod addedDamage(String damageType, ExpressionCodec.Value min, ExpressionCodec.Value max) {
        AddedDamage ad = new AddedDamage(damageType, min, max);
        return new EffectMod(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                false, Optional.of(ad), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** 投射物分裂后伤害倍率（Fork 的「分叉后 30% less」近似为 0.7）。 */
    public static EffectMod splitMultiplier(double mult) {
        return new EffectMod(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.of(ExpressionCodec.of(mult)),
                Optional.empty(), Optional.empty(), false, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** 暴击率倍率（more% 形式，如 +100% → 2.0）。 */
    public static EffectMod critChance(double more) {
        return new EffectMod(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.of(ExpressionCodec.of(more)), Optional.empty(),
                false, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** 暴击伤害倍率（more% 形式，如 +50% → 1.5）。 */
    public static EffectMod critDamage(double more) {
        return new EffectMod(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(ExpressionCodec.of(more)),
                false, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** 范围效果倍率（area 投递的 radius_multiplier，如 +30% → 1.3）。 */
    public static DeliveryMod areaAoe(double more) {
        return new DeliveryMod("area", Map.of("radius_multiplier", ExpressionCodec.of(more)));
    }

    // ==================== 装配期特殊修正（SpecialMod） ====================
    // 与运行时修正（EffectMod / DeliveryMod）不同：这些在「技能链接装配时」一次性汇总后写入技能数据
    // （SkillLinkResolver#applyAll → getTotalSpeedMultiplier → SkillCast#withSpeedMultiplier），
    // 而非每次施放时查询，故不进入运行时 SupportProfile。
    //
    // 速度倍率作用于链接技能整体的施法时间：getCastTime = base / multiplier，
    // 故「30% less Cast Speed」= 0.7（施法时间变长为 1/0.7 ≈ 1.43 倍）。

    /**
     * 施法速度倍率（{@code 0.7} = 30% less Cast Speed）——<b>只对法术技能生效</b>（见 skillSpeed 说明）。
     * <p>供只辅助<b>法术</b>的宝石使用（如 spell_echo）。
     */
    public static SpecialMod castSpeed(double multiplier) {
        return new SpecialMod(Optional.empty(), Optional.empty(),
                Optional.of(ExpressionCodec.of(multiplier)), Optional.empty(), Optional.empty());
    }

    /**
     * 攻击速度倍率（{@code 0.85} = 15% less，{@code 1.5} = 50% more）——<b>只对攻击技能生效</b>。
     * <p>供只辅助<b>攻击</b>的宝石使用（如 tectonic_slams / streamlined_rounds）。
     */
    public static SpecialMod attackSpeed(double multiplier) {
        return new SpecialMod(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(ExpressionCodec.of(multiplier)), Optional.empty());
    }

    /**
     * 技能速度倍率（{@code 0.8} = 20% less Skill Speed）——<b>对攻击与法术皆生效</b>。
     * <p>POE2 的 Skill Speed 是<b>单一</b>速度维度：对攻击表现为攻击速度、对法术表现为施法速度，
     * 但数值只有一份。聚合时无条件连乘（{@code SkillLinkResolver#getTotalSpeedMultiplier}）。
     * <p>注意不可拆成 cast×attack 两通道表达——0.8 拆开会变成 0.8×0.8=0.64（−36%），语义错误。
     */
    public static SpecialMod skillSpeed(double multiplier) {
        return new SpecialMod(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(ExpressionCodec.of(multiplier)));
    }

    // ==================== 回响（Echo） ====================
    // POE2 语义：repeat 是原施放的一部分——不二次消耗、不二次冷却、不二次触发「施放时」效果，
    // 只把「效果」（投递物/范围/光束 …）重做一遍（见 SkillPhase#repeat 与 EchoState）。
    //
    // 「Echoes have +X% AoE」类加成是<b>回响专属</b>的，绝不能建模成全局 areaAoe 倍率
    // （那会把加成错误地施加到原施放上——旧建模正是这个错）。它由 EchoMod 的范围载荷承载，
    // 随回响实例传递，只作用于重放的那一次：
    //   areaInc         —— POE2「increased」，加法项，与 EFFECT_AREA 相加；
    //   areaMorePerEcho —— POE2「more」，乘法项，按回响序号递增（第 i 次 → 1 + v*i）。

    /** 回响（整技能）：整个技能重复 {@code count} 次效果，每次间隔 {@code delayTicks} tick。 */
    public static EchoMod echoSkill(int count, int delayTicks) {
        return echoSkill(count, delayTicks, 0.0, 0.0);
    }

    /** 回响（整技能）+ 回响专属范围载荷。 */
    public static EchoMod echoSkill(int count, int delayTicks, double areaInc, double areaMorePerEcho) {
        return new EchoMod(count, delayTicks, EchoMod.Scope.SKILL, Optional.empty(), Optional.empty(),
                areaInc, areaMorePerEcho);
    }

    /** 回响（按技能标签定向）：仅当技能携带 {@code tag} 标签（spell / warcry / 技能名 …）时重复。 */
    public static EchoMod echoTag(int count, int delayTicks, String tag) {
        return echoTag(count, delayTicks, tag, 0.0, 0.0);
    }

    /** 回响（按技能标签定向）+ 回响专属范围载荷。 */
    public static EchoMod echoTag(int count, int delayTicks, String tag, double areaInc, double areaMorePerEcho) {
        return new EchoMod(count, delayTicks, EchoMod.Scope.TAG, Optional.of(tag), Optional.empty(),
                areaInc, areaMorePerEcho);
    }

    /** 回响（按顶层阶段序号定向）：只重复第 {@code phaseIndex} 个顶层阶段。 */
    public static EchoMod echoPhase(int count, int delayTicks, int phaseIndex) {
        return echoPhase(count, delayTicks, phaseIndex, 0.0, 0.0);
    }

    /** 回响（按顶层阶段序号定向）+ 回响专属范围载荷。 */
    public static EchoMod echoPhase(int count, int delayTicks, int phaseIndex, double areaInc, double areaMorePerEcho) {
        return new EchoMod(count, delayTicks, EchoMod.Scope.PHASE, Optional.empty(), Optional.of(phaseIndex),
                areaInc, areaMorePerEcho);
    }

    // ==================== 封印（Seal / Unleash） ====================
    // POE2 语义：被辅助技能每次施放累积封印；持有 ≥1 封印期间施放获 cast speed 加成
    // （EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CAST_SPEED_SEAL，seal 作用域，见 SkillStatScopes#enter）；
    // 存满后释放重放（自动释放回退模式，见 SkillInstance#handleSeal / SealRelease）。
    // 数值为临时占位（provisional），待平衡。

    /**
     * 封印（Seal）— 让被辅助技能获得 POE2「Unleash / 封印」机制。
     *
     * @param maxSeals                      封印上限
     * @param gainEveryCastTimeMultiplier   每次施放获得的充能 = 100 / 该值（越大越慢攒印）
     * @param repeatPerSealBroken           每破碎（释放）1 枚封印额外重放的次数
     * @param areaIncPerSealBroken          每破碎 1 枚封印，重放那次 +X% 范围（increased，加法）
     */
    public static SealMod sealGem(int maxSeals, double gainEveryCastTimeMultiplier,
                                  int repeatPerSealBroken, double areaIncPerSealBroken) {
        return new SealMod(maxSeals, gainEveryCastTimeMultiplier, repeatPerSealBroken, areaIncPerSealBroken);
    }

    /** 投射物参数修正（additional_projectiles / pierce_count / chain_count / fork_count / barrage_count 等）。 */
    public static DeliveryMod projectile(String param, double value) {
        return new DeliveryMod("projectile", Map.of(param, ExpressionCodec.of(value)));
    }

    /** 范围参数修正（radius_multiplier / additional_targets 等）。 */
    public static DeliveryMod area(String param, double value) {
        return new DeliveryMod("area", Map.of(param, ExpressionCodec.of(value)));
    }

    private static EffectMod effectMod(Optional<String> delivery, Optional<String> damageType,
                                       Optional<Double> more, Optional<Double> less) {
        return new EffectMod(delivery, damageType, Optional.empty(),
                more.map(ExpressionCodec::of), less.map(ExpressionCodec::of),
                Optional.empty(), Optional.empty(), Optional.empty(),
                false, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** 抗性穿透（damage_type=fire/cold/lightning/chaos/elemental，amount=穿透比例，0.3=穿透30%）。 */
    public static EffectMod penetration(String damageType, double amount) {
        Penetration p = new Penetration(damageType, ExpressionCodec.of(amount));
        return new EffectMod(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                false, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(p), Optional.empty(), Optional.empty());
    }

    /** 元素曝露（命中后压低目标对应元素抗性 amount，持续 durationTicks）。 */
    public static EffectMod exposure(String damageType, double amount, int durationTicks) {
        Exposure e = new Exposure(damageType,
                ExpressionCodec.of(amount), ExpressionCodec.of(durationTicks));
        return new EffectMod(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                false, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(e), Optional.empty());
    }

    /** 减速（命中后降低目标行动速度 amount，持续 durationTicks；Maim/Hinder 类辅助）。 */
    public static EffectMod slow(double amount, int durationTicks) {
        Slow s = new Slow(ExpressionCodec.of(amount), ExpressionCodec.of(durationTicks));
        return new EffectMod(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                false, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.of(s));
    }

    /**
     * 元宝石（Cast on X）工厂 — 为被辅助技能注入一个事件监听，当指定条件（暴击/点燃/感电…）
     * 发生时触发被链接的技能。被触发的技能继承本插槽的辅助宝石修正（见 SkillTriggerHelper）。
     *
     * @param condition       触发条件（on_crit / on_ignite / on_shock …）
     * @param linkedIds       被触发技能 id 列表（含颜色段，如 coe:blue/chaos_bolt）
     * @param cooldownTicks    内部冷却（秒→tick，0.5 = 0.5 秒）
     * @param maxTriggers      每次施放最多触发次数（1 = 每次攻击仅触发一次）
     * @param costMultiplier   消耗 & 保留倍率（POE2 元宝石约 1.3）
     * @param maxLevel         最大等级
     */
    /**
     * 元宝石（Cast on X）工厂 — 为被辅助技能注入一个事件监听，当指定条件（ExContext 全域表达式）满足时
     * 触发被链接的技能。被触发的技能继承本插槽的辅助宝石修正（见 SkillTriggerHelper）。
     *
     * @param eventName       路由事件名（CUSTOM 事件预过滤用，如 on_crit / on_ignite / on_shock）
     * @param condition       触发条件（ExContext 全域 math 表达式，非零即触发，如 event:on_crit >= 1）
     * @param linkedIds       被触发技能 id 列表（含颜色段，如 coe:blue/chaos_bolt）
     * @param cooldownTicks    内部冷却（秒→tick，0.5 = 0.5 秒）
     * @param maxTriggers      每次施放最多触发次数（1 = 每次攻击仅触发一次）
     * @param costMultiplier   消耗 & 保留倍率（POE2 元宝石约 1.3）
     * @param maxLevel         最大等级
     */
    public static SupportGem castOn(SupportRequire require, String tag, String eventName,
                                    ExpressionCodec.Value condition, List<String> linkedIds,
                                    double cooldownTicks, int maxTriggers,
                                    double costMultiplier, double maxLevel,
                                    double energyThreshold) {
        EventListenPhase listen = new EventListenPhase(
                SkillEventType.CUSTOM,
                Optional.empty(),
                List.of(new TriggeredAction(LifecycleTrigger.ON_HIT,
                        new MetaSkillAction(condition, linkedIds, Optional.empty(),
                                Optional.of(ExpressionCodec.of(cooldownTicks)),
                                Optional.of(ExpressionCodec.of((double) maxTriggers)),
                                Optional.empty(),
                                energyThreshold > 0
                                        ? Optional.of(ExpressionCodec.of(energyThreshold))
                                        : Optional.empty()))),
                Optional.of(ExpressionCodec.of(cooldownTicks)),
                Optional.of(ExpressionCodec.of((double) maxTriggers)),
                Optional.of(eventName));
        return new SupportGem(require, Optional.of(tag), allowTypesFor(tag), List.of(new EventListenEffect(listen)),
                Optional.empty(),
                Optional.of(ExpressionCodec.of(costMultiplier)),
                Optional.of(ExpressionCodec.of(costMultiplier)),
                Optional.of(ExpressionCodec.of(maxLevel)),
                Optional.of(false), 1, 1, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** 收口重载：通用事件名（如 caused_ailment）+ 具体类型标签（如 ignite/shock）过滤，
     * 条件为 {@code event:<eventName> >= 1}。用于「造成异常时施放」类宝石按异常类型精确触发。 */
    public static SupportGem castOn(SupportRequire require, String tag, String eventName,
                                    List<String> requireTags, List<String> linkedIds,
                                    double cooldownTicks, int maxTriggers,
                                    double costMultiplier, double maxLevel, double energyThreshold) {
        ExpressionCodec.Value cond = MetaSkillAction.eventCondition(eventName);
        // 事件名条件 + 类型标签条件（requireTags 经 EventListenPhase.requireTags 过滤）
        EventListenPhase listen = new EventListenPhase(
                SkillEventType.CUSTOM,
                Optional.empty(),
                List.of(new TriggeredAction(LifecycleTrigger.ON_HIT,
                        new MetaSkillAction(cond, linkedIds, Optional.empty(),
                                Optional.of(ExpressionCodec.of(cooldownTicks)),
                                Optional.of(ExpressionCodec.of((double) maxTriggers)),
                                Optional.empty(),
                                energyThreshold > 0
                                        ? Optional.of(ExpressionCodec.of(energyThreshold))
                                        : Optional.empty()))),
                Optional.of(ExpressionCodec.of(cooldownTicks)),
                Optional.of(ExpressionCodec.of((double) maxTriggers)),
                Optional.of(eventName),
                Optional.of(requireTags));
        return new SupportGem(require, Optional.of(tag), allowTypesFor(tag), List.of(new EventListenEffect(listen)),
                Optional.empty(),
                Optional.of(ExpressionCodec.of(costMultiplier)),
                Optional.of(ExpressionCodec.of(costMultiplier)),
                Optional.of(ExpressionCodec.of(maxLevel)),
                Optional.of(false), 1, 1, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** 元宝石（Cast on X）— eventName 自动生成 condition，并指定 POE2 能量阈值（>0 时能量积累满阈值才触发；0 即时触发）。 */
    public static SupportGem castOn(SupportRequire require, String tag, String eventName,
                                    List<String> linkedIds, double cooldownTicks, int maxTriggers,
                                    double costMultiplier, double maxLevel, double energyThreshold) {
        return castOn(require, tag, eventName, MetaSkillAction.eventCondition(eventName),
                linkedIds, cooldownTicks, maxTriggers, costMultiplier, maxLevel, energyThreshold);
    }

    /**
     * Archon 授予宝石工厂（POE2 Arbiter's Ignition 等「造成某异常时获得 Archon」类辅助的数据驱动通道）：
     * 监听 {@code caused_ailment}（requireTags 指定异常类型，如 ignite），触发 StatAction 经中央状态引擎
     * {@link com.cimdy.coe.data.codec.support.status.runtime.StatusEngine#apply} 将对应 Archon 授予施法者自身
     * （条目目标选择器 SELF）。互斥 + 恢复守卫由中央引擎按 archon 标签统一判定，无需本处硬编码——
     * 多条渠道（辅助宝石 / 天赋 / 升华）各自以同型数据定义触发「获得 Archon」，实现多渠道数据驱动。
     *
     * @param status        被授予的 Archon 状态（须带 archon 标签）
     * @param requireTags   触发所需的异常类型标签（如 ["ignite"]）
     * @param cooldownTicks 内部冷却（秒→tick）
     * @param maxTriggers   每次施放最多触发次数
     */
    public static SupportGem gainArchonOnAilment(SupportRequire require, String tag, StatusType status,
                                                 List<String> requireTags,
                                                 double cooldownTicks, int maxTriggers,
                                                 double costMultiplier, double maxLevel) {
        StatEntry grant = StatusStatEffect.selfEntry(status, ExpressionCodec.of(1.0));
        StatAction grantAction = new StatAction(new StatGroup(List.of(grant)), false, java.util.Optional.empty(), ExpressionCodec.of(0.0));
        EventListenPhase listen = new EventListenPhase(
                SkillEventType.CUSTOM,
                Optional.empty(),
                List.of(new TriggeredAction(LifecycleTrigger.ON_HIT, grantAction)),
                Optional.of(ExpressionCodec.of(cooldownTicks)),
                Optional.of(ExpressionCodec.of((double) maxTriggers)),
                Optional.of(EventName.CAUSED_AILMENT),
                Optional.of(requireTags));
        return new SupportGem(require, Optional.of(tag), allowTypesFor(tag), List.of(new EventListenEffect(listen)),
                Optional.empty(),
                Optional.of(ExpressionCodec.of(costMultiplier)),
                Optional.of(ExpressionCodec.of(costMultiplier)),
                Optional.of(ExpressionCodec.of(maxLevel)),
                Optional.of(false), 1, 1, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }
}
