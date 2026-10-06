package com.cimdy.coe.data.gen.gem.skill;

import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.AreaDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.HazardDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.RemnantDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.BallDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.BeamDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.OrbDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.ProjectileDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.ThrownDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.SelfDelivery;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.AdditionalActionEffect;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.DeliveryMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EffectMod;
import com.cimdy.coe.data.gen.asset.EntityProvider;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.codec.support.stat.effect.resource.ResourceStatEffect;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceUsage;
import com.cimdy.coe.data.codec.skill.helper.cast.SkillCast;
import com.cimdy.coe.data.codec.skill.input.Click;
import com.cimdy.coe.data.codec.skill.input.Continuous;
import com.cimdy.coe.data.codec.skill.input.HoldInput;
import com.cimdy.coe.data.codec.skill.input.InputMode;
import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.SkillPhasePlayer;
import com.cimdy.coe.data.codec.skill.phase.action.*;
import com.cimdy.coe.data.codec.skill.phase.action.combo.*;
import com.cimdy.coe.data.codec.skill.phase.action.control.*;
import com.cimdy.coe.data.codec.skill.phase.action.damage.*;
import com.cimdy.coe.data.codec.skill.phase.action.movement.*;
import com.cimdy.coe.data.codec.skill.phase.action.support.*;
import com.cimdy.coe.data.codec.skill.phase.delivery.*;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.InstantDuration;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.SphereShape;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.flight.LinearFlight;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision.Stop;
import com.cimdy.coe.data.codec.support.require.Require;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.MathParser;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.stat.entry.StatGroup;
import com.cimdy.coe.data.codec.support.status.def.AilmentData;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.data.gen.stat.StatGen;
import com.mojang.datafixers.util.Either;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 技能构造工厂 — 用现有可用的 Delivery / Action 组合出合法 {@link Skill}。
 * 仅覆盖"可完全实现"的技能形态（投射物 / 范围 / 光束 / 自身增益 / 位移 / 击退），
 * 复杂形态（依赖异常累积 AilmentAction、召唤 SpawnAction 等尚未实现的动作）见 TODO 文档。
 *
 * <p>技能可携带自身 {@code modifiers}（DeliveryMod / EffectMod / AdditionalActionEffect），
 * 与辅助宝石的 {@code effects} 共用同一套 {@link SkillModifier} 形状——
 * 因此技能本身也能做到斩杀、附加伤害、得球等辅助宝石能做的事。
 */
public final class SkillFactory {

    private SkillFactory() {
    }

    // ===================== 资源消耗辅助 =====================

    public static ResourceCost mana(double value) {
        return new ResourceCost(ResourceType.MANA, ResourceUsage.CONSUME, ExpressionCodec.of(value));
    }

    /** 魔力消耗（按等级查表，严格还原非线性消耗）。 */
    public static ResourceCost mana(ExpressionCodec.Value value) {
        return new ResourceCost(ResourceType.MANA, ResourceUsage.CONSUME, value);
    }

    public static ResourceCost life(double value) {
        return new ResourceCost(ResourceType.HEALTH, ResourceUsage.CONSUME, ExpressionCodec.of(value));
    }

    /** 生命消耗（按等级查表）。 */
    public static ResourceCost life(ExpressionCodec.Value value) {
        return new ResourceCost(ResourceType.HEALTH, ResourceUsage.CONSUME, value);
    }

    // ===================== 投递方式辅助 =====================

    /**
     * 按伤害类型派生外观的投射物 —— 「新实体错觉」的真正落地点。
     *
     * <p><b>约定式派生（禁硬编码 switch）</b>：渲染 id = {@code coe:entity_render/<damageType>}
     * （{@code fire} / {@code cold} / {@code lightning} / {@code chaos} …），
     * 故新增一种元素只需在 {@code EntityProvider} 加一份同名渲染数据并登记进
     * {@link EntityProvider#ELEMENT_RENDER_NAMES}，本类<b>零改动</b>。
     *
     * <p><b>无对应数据者（{@code physical} 等非元素伤害类型）不覆写 render</b>，
     * 而非覆写一个查不到数据的 id：{@code EntityRead} 无 render 时由实体定义自带外观
     * （{@code coe:arrow}）兜底，实体照常可见；一旦覆写成不存在的 id，
     * {@code EntityRenderDefs} 查不到 → 渲染层 model 为 null → <b>实体彻底不可见</b>，
     * 且该 id 会被技能皮肤如实记录进数据包，使悬空引用固化。
     *
     * <p>底层仍是同一个 {@code coe:skill_arrow} 实体与 {@code coe:entity/arrow} 预设，
     * 只是经 {@link EntityRead#withRender} 覆写了 render —— 玩家看到的是完全不同的弹体。
     */
    private static ProjectileDelivery projectile(String damageType) {
        Identifier base = ProjectileDelivery.DEFAULT_REF.ref();
        if (!EntityProvider.ELEMENT_RENDER_NAMES.contains(damageType)) {
            return projectile(EntityRead.of(base));
        }
        return projectile(EntityRead.withRender(
                base,
                Identifier.fromNamespaceAndPath("coe", damageType)));
    }

    /**
     * 球投递 — 渲染 id 按 {@code ball_<damageType>} 约定式派生。
     *
     * <p><b>为什么是 {@code ball_} 前缀而不是复用投射物的 {@code coe:<damageType>}</b>：
     * 投射物那套渲染数据是<b>元素配色的箭矢贴图</b>（{@code SkillArrowRender} 用真 3D 箭矢模型），
     * 而球是球状贴图 —— 形状不同，混用会让球长成一支箭。故球用独立命名空间，
     * 与投射物同构（约定式派生、互不借用）但名单各自独立。
     *
     * <p><b>为什么不复用 {@link EntityProvider#ELEMENT_RENDER_NAMES}</b>：
     * 投射物族有四件元素配色箭矢，球族只生成确有实例的电球一种（{@code coe:ball_lightning}）。
     * 共用名单会把 {@code fire/cold/chaos} 也放进来，于是 {@code ball_fire} 等 id 被派生出来
     * 却查不到任何渲染数据 —— 与本次修掉的 {@code physical} 悬空引用同一类缺陷，只是换个前缀。
     * 故球族另有 {@link EntityProvider#BALL_RENDER_NAMES}，由 datagen 硬校验与真实数据同步。
     *
     * <p><b>未匹配者不覆写 render</b>（与 {@link #projectile(String)} 同理）：
     * {@code EntityRead} 无 render 时由 {@code coe:entity/ball} 自带的 {@code coe:ball} 兜底，
     * 球照常可见；覆写一个不存在的 id 则会让它彻底不可见，且悬空 id 会固化进数据包。
     *
     * <p>底层是同一个 {@code coe:skill_ball} 实体与 {@code coe:entity/ball} 预设，
     * 只是经 {@link EntityRead#withRender} 覆写了 render —— 玩家看到的是不同的球。
     */
    private static BallDelivery ball(String damageType) {
        if (!EntityProvider.BALL_RENDER_NAMES.contains(damageType)) {
            return ball(EntityRead.of(BallDelivery.DEFAULT.ref().ref()));
        }
        return ball(EntityRead.withRender(
                BallDelivery.DEFAULT.ref().ref(),
                Identifier.fromNamespaceAndPath("coe", "ball_" + damageType)));
    }

    /** 指定形态引用的球 —— 技能侧「新实体错觉」的入口（换 render 即换球外观）。 */
    private static BallDelivery ball(EntityRead ref) {
        return new BallDelivery(
                BallDelivery.DEFAULT.speed(),
                BallDelivery.DEFAULT.radius(),
                BallDelivery.DEFAULT.lifetime(),
                BallDelivery.DEFAULT.tags(),
                ref,
                Optional.empty());
    }

    /** 指定形态引用的投射物 —— 技能侧「新实体错觉」的入口（换 render 即换箭矢外观）。 */
    private static ProjectileDelivery projectile(EntityRead ref) {
        return new ProjectileDelivery(
                new LinearFlight(),
                new Stop(),
                ExpressionCodec.of(100.0),
                ExpressionCodec.of(1.5),
                Optional.empty(),
                ref,
                Optional.empty());
    }

    private static AreaDelivery area(double radius) {
        return new AreaDelivery(
                new SphereShape(ExpressionCodec.of((float) radius)),
                new InstantDuration(),
                ExpressionCodec.of(1.0));
    }

    /**
     * 危害物投递（驻场物件，非瞬时命中）。
     * 与 {@link #area(double)} 的区别：不引入 {@code AreaDuration}——危害物由 SkillEntity 在
     * {@code lifetime}（tick 表达式）内存续并在敌人穿越时触发，AreaDelivery 的 Instant/Lingering 语义不适用。
     *
     * <p>危害物的伤害/效果来自技能阶段自身的 {@code actions}（由 SkillEntity 点燃 {@code ON_HIT} 驱动），
     * 因此接入方式与 {@link #area(double)} 完全一致，仅替换 delivery。
     *
     * <p><b>当前无技能使用</b>（Hazard 平台已就绪，待后续技能/宝石接入）——此为数据侧接入入口。
     */
    public static HazardDelivery hazard(double radius, double lifetime) {
        return new HazardDelivery(
                new SphereShape(ExpressionCodec.of((float) radius)),
                ExpressionCodec.of(lifetime),
                ExpressionCodec.of(1.0),
                HazardDelivery.DEFAULT.ref(),
                ExpressionCodec.of(1.0),
                ExpressionCodec.of(0.0));
    }

    /**
     * 残片投递（驻场可拾取物件，非瞬时命中）。
     * 与 {@link #hazard(double, double)} 形态同级：本工厂只装配「把残片放到世界里」所需的半径与存活时长，
     * <b>残片的具体效果不在投递层定义</b>——由调用方技能阶段的 {@code actions}（残片被施放者拾取时
     * 经 {@code ON_HIT} 驱动）决定。因此本工厂与数据侧接入入口一致，仅替换 delivery。
     *
     * <p><b>当前无技能使用</b>（Remnant 平台已就绪，待后续技能/宝石接入）——此为数据侧接入入口。
     */
    public static RemnantDelivery remnant(double radius, double lifetime) {
        return new RemnantDelivery(
                new SphereShape(ExpressionCodec.of((float) radius)),
                ExpressionCodec.of(lifetime),
                ExpressionCodec.of(1.0),
                RemnantDelivery.DEFAULT.ref());
    }

    /**
     * 投掷物投递（扔出去的弹体：抛物线飞出 → 落地 → 引信 → 范围引爆）。
     *
     * <p>与 {@link #hazard(double, double)} / {@link #remnant(double, double)} 同范式：
     * 本工厂只装配「把弹体扔出去」所需的半径与存活上限，<b>爆炸的效果不在投递层定义</b>——
     * 由调用方技能阶段的 {@code actions} 决定（弹体引爆时经 {@code ON_HIT} 驱动）。
     * 初速 / 抛射弧 / 标签 / 形态一律沿用 {@code ThrownDelivery} 默认值
     * （手雷口径：{@code coe:entity/grenade}，stat 标签 {@code grenade}）。
     *
     * <p><b>当前无技能使用</b>（投掷物平台已就绪，待手雷类技能/宝石接入）——此为数据侧接入入口。
     */
    public static ThrownDelivery thrown(double radius, double lifetime) {
        return new ThrownDelivery(
                ThrownDelivery.DEFAULT.speed(),
                ExpressionCodec.of((float) radius),
                ExpressionCodec.of(lifetime),
                ThrownDelivery.DEFAULT.arc(),
                ThrownDelivery.DEFAULT.tags(),
                ThrownDelivery.DEFAULT.ref(),
                Optional.empty());
    }

    /**
     * Orb 投递 — 渲染 id 按 {@code orb_<damageType>} 约定式派生（同 {@link #ball(String)}）。
     *
     * <p><b>为什么是 {@code orb_} 前缀而不是复用球的 {@code ball_}</b>：飞行球是「小而拖尾的弹体」，
     * Orb 是「大而悬停的领域球」—— 贴图气质与尺寸都不同（见 {@code EntityProvider#orbElementRender}）。
     *
     * <p>底层是 {@code coe:orb} 实体类型与 {@code coe:entity/orb} 形态（{@code static + pulse}），
     * 只是经 {@link EntityRead#withRender} 覆写了 render —— 玩家看到的是不同的球。
     */
    /** 通用 Orb 投递（驻留球）：以给定实体引用（可覆写渲染）在目标位置生成原地周期脉冲的球。 */
    private static OrbDelivery orb(EntityRead ref, double radius, int lifetimeTicks, int pulseIntervalTicks) {
        return new OrbDelivery(
                ExpressionCodec.of((float) radius),
                ExpressionCodec.of((float) lifetimeTicks),
                ExpressionCodec.of((float) pulseIntervalTicks),
                OrbDelivery.DEFAULT.tags(),
                ref);
    }

    /**
     * Orb 投递（元素派生渲染）：render 按 {@code orb_<damageType>} 约定式派生（同 {@link #ball(String)}）。
     *
     * <p>与球同理，Orb 族<b>不共用</b> {@link EntityProvider#ELEMENT_RENDER_NAMES}：
     * Orb 实际只有 {@code orb_lightning}（风暴漩涡）与 {@code orb_fire}（日曜球）两份数据，
     * 名单见 {@link EntityProvider#ORB_RENDER_NAMES}，由 datagen 硬校验与真实数据同步。
     * 未匹配者不覆写 render，由 {@code coe:entity/orb} 自带的 {@code coe:orb} 兜底（可见）。
     */
    private static OrbDelivery orb(String damageType, double radius, int lifetimeTicks, int pulseIntervalTicks) {
        if (!EntityProvider.ORB_RENDER_NAMES.contains(damageType)) {
            return orb(EntityRead.of(OrbDelivery.DEFAULT.ref().ref()),
                    radius, lifetimeTicks, pulseIntervalTicks);
        }
        return orb(EntityRead.withRender(OrbDelivery.DEFAULT.ref().ref(),
                Identifier.fromNamespaceAndPath("coe", "orb_" + damageType)),
                radius, lifetimeTicks, pulseIntervalTicks);
    }

    private static BeamDelivery beam(double range, double width) {
        return new BeamDelivery(
                ExpressionCodec.of(range),
                ExpressionCodec.of(width),
                ExpressionCodec.of(0.0),
                ExpressionCodec.of(1.0),
                Optional.empty());
    }

    private static SelfDelivery self() {
        return new SelfDelivery(ExpressionCodec.of(1.0));
    }

    /** {@code $槽} 数值：经 MathParser 生成 VarRef 节点，运行期由注入变量真读（与 SkillPresets.slot 同义）。 */
    private static ExpressionCodec.Value slot(String name) {
        return ExpressionCodec.of(MathParser.parse("$" + name));
    }

    // ===================== 动作辅助 =====================

    /** 命中伤害动作（ON_HIT），伤害值直接由表达式给出（可为 LevelTable 以严格还原）。 */
    private static TriggeredAction hitDamage(String damageType, ExpressionCodec.Value amount) {
        return new TriggeredAction(LifecycleTrigger.ON_HIT,
                new DamageAction(damageType, amount, Optional.empty(), ExpressionCodec.of(1.0)));
    }

    /** 命中伤害动作（ON_HIT），线性估算版（兼容旧生成代码）。 */
    private static TriggeredAction hitDamage(String damageType, double dmgBase, double dmgPer, double dmgAdd) {
        return hitDamage(damageType, ExpressionCodec.ofSkillLevel(dmgBase, dmgPer, dmgAdd));
    }

    /** 命中伤害 + 击退动作（ON_HIT），伤害值直接由表达式给出。 */
    private static TriggeredAction hitDamageKnockback(String damageType, ExpressionCodec.Value amount,
                                                      double knockback) {
        return new TriggeredAction(LifecycleTrigger.ON_HIT,
                new DamageAction(damageType, amount,
                        Optional.of(ExpressionCodec.of(knockback)), ExpressionCodec.of(1.0)));
    }

    /** 命中伤害 + 击退动作（ON_HIT），线性估算版（兼容旧生成代码）。 */
    private static TriggeredAction hitDamageKnockback(String damageType, double dmgBase, double dmgPer,
                                                      double dmgAdd, double knockback) {
        return hitDamageKnockback(damageType, ExpressionCodec.ofSkillLevel(dmgBase, dmgPer, dmgAdd), knockback);
    }

    /** 命中伤害 + 数据驱动异常（ON_HIT）：伤害与异常施加共用同一命中上下文。 */
    private static List<TriggeredAction> hitDamageWithAilment(String damageType, ExpressionCodec.Value amount,
                                                              List<AilmentData> ailments) {
        List<StatEntry> entries = new ArrayList<>();
        for (AilmentData a : ailments) entries.addAll(ailmentEntries(a));
        return List.of(
                hitDamage(damageType, amount),
                new TriggeredAction(LifecycleTrigger.ON_HIT, new StatAction(new StatGroup(entries), false, Optional.empty(), ExpressionCodec.of(0.0))));
    }

    /**
     * 把一份 {@link AilmentData}（内含多个并列数值）展开为若干<b>单数值条目</b>：
     * 每个语义面（积蓄 / 时长 / 强度 / 层数）各成一条，符合「一条词缀恰好一个数值」的约束。
     */
    /**
     * 把一份 {@link AilmentData}（内含多个并列数值）展开为若干<b>单数值条目</b>：
     * 每个语义面（积蓄 / 时长 / 强度 / 层数）各成一条，符合「一条词缀恰好一个数值」的约束。
     * 各面改由数据生成模板（见 {@code StatGen.registerAilmentFaces}）以 read 模式引用，
     * 运行时经 AilmentHelper 按「异常名 + 分类标签」作用域聚合读取。
     */
    private static List<StatEntry> ailmentEntries(AilmentData a) {
        StatusType t = a.type();
        List<StatEntry> out = new ArrayList<>();
        out.add(readAilmentStat(FACE_BUILDUP, t, a.fixedBuildup().orElse(a.buildupMultiplier())));
        a.overrideDurationTicks().ifPresent(v -> out.add(readAilmentStat(FACE_DURATION, t, v)));
        a.effectMagnitude().ifPresent(v -> out.add(readAilmentStat(FACE_MAGNITUDE, t, v)));
        a.maxStacks().ifPresent(v -> out.add(readAilmentStat(FACE_MAX_STACKS, t, v)));
        a.statusExtraStacks().ifPresent(v -> out.add(readAilmentStat(FACE_EXTRA_STACKS, t, v)));
        return out;
    }

    // ===================== POE2「Consume X」消费通道（P6 消费侧闭合） =====================

    /**
     * 消费异常回报（POE2「Consume X」原语，ON_HIT）：命中时消费命中目标身上的指定异常，
     * 被消费强度注入本次施放参数槽 {@code $consumed_<id>}，随后执行回报动作链（payoff）。
     *
     * <p>POE2 实证：{@code blood_hunt}（Consume Bleeding → 血爆，敌人失去等同<b>被消耗失血量 25%</b>的生命）、
     * {@code elemental_sundering}（Consumes Freeze/Shock/Ignite → 对应元素爆炸）。
     * 这是 P2-3 转存通道的真实闭合点：{@code StatusEngine.consumeAilment} 写记账 →
     * {@link com.cimdy.coe.data.codec.skill.phase.action.damage.ConsumeAilmentAction} 取回并清除 →
     * 回报动作按被消费量结算。
     */
    public static TriggeredAction consumeAilmentOnHit(StatusType type, Action... payoff) {
        return new TriggeredAction(LifecycleTrigger.ON_HIT,
                new ConsumeAilmentAction(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, type.id),
                        true, List.of(payoff)));
    }

    private static final String FACE_BUILDUP = "ailment_buildup";
    private static final String FACE_DURATION = "ailment_duration";
    private static final String FACE_MAGNITUDE = "ailment_magnitude";
    private static final String FACE_MAX_STACKS = "status_max_stacks";
    private static final String FACE_EXTRA_STACKS = "status_extra_stacks";

    /** 引用数据生成模板（{@code coe:<face>_<statusId>}），把本实例数值经 {@code vars} 注入 $value。 */
    private static StatEntry readAilmentStat(String face, StatusType type, ExpressionCodec.Value value) {
        return new StatRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, face + "_" + type.id),
                Map.of("value", value));
    }

    // ===================== 核心组装 =====================

    private static Skill build(Require require, ResourceCost cost, Delivery delivery,
                               List<TriggeredAction> actions, SkillCast cast, List<SkillModifier> modifiers) {
        return build(require, cost, delivery, actions, cast, modifiers, new Click());
    }

    /** 带输入模式的组装（Continuous=引导脉冲 / HoldInput=长按蓄力）。 */
    private static Skill build(Require require, ResourceCost cost, Delivery delivery,
                               List<TriggeredAction> actions, SkillCast cast, List<SkillModifier> modifiers,
                               InputMode input) {
        SkillPhase phase = new SkillPhase(Optional.of(delivery), actions, List.of(), Optional.empty());
        SkillPhasePlayer player = new SkillPhasePlayer(Optional.empty(), phase);
        return new Skill(require, "", Optional.empty(), cast, input, List.of(cost), List.of(player), modifiers, 1, List.of(),
                Optional.empty(), Optional.empty(), Map.of(),
                Optional.empty(), Optional.empty());
    }

    /** 带技能级 stat 标签的组装（tags 进入 Skill.getLabels/getStatTags，供「对 [X] 类技能」加成命中）。 */
    private static Skill build(Require require, ResourceCost cost, Delivery delivery,
                               List<TriggeredAction> actions, SkillCast cast, List<SkillModifier> modifiers,
                               InputMode input, List<String> tags) {
        SkillPhase phase = new SkillPhase(Optional.of(delivery), actions, List.of(), Optional.empty());
        SkillPhasePlayer player = new SkillPhasePlayer(Optional.empty(), phase);
        return new Skill(require, "", Optional.empty(), cast, input, List.of(cost), List.of(player), modifiers, 1, tags,
                Optional.empty(), Optional.empty(), Map.of(),
                Optional.empty(), Optional.empty());
    }

    /** 带技能级标签 + $槽参数的组装（墙体等驻场实体技能用：参数经 MathParser 生成 VarRef，运行期由注入变量真读）。 */
    private static Skill build(Require require, ResourceCost cost, Delivery delivery,
                              List<TriggeredAction> actions, SkillCast cast,
                              List<SkillModifier> modifiers, List<String> tags,
                              Map<String, ExpressionCodec.Value> params) {
        SkillPhase phase = new SkillPhase(Optional.of(delivery), actions, List.of(), Optional.empty());
        SkillPhasePlayer player = new SkillPhasePlayer(Optional.empty(), phase);
        return new Skill(require, "", Optional.empty(), cast, new Click(), List.of(cost), List.of(player),
                modifiers, 1, tags, Optional.empty(), Optional.empty(), params, Optional.empty(), Optional.empty());
    }

    /**
     * 墙体技能（冰墙 / 火墙 / 盾墙真实机制）。
     *
     * <p>投递用 {@link #self()}：施法瞬间以施法者自身为目标触发 ON_HIT =
     * {@link SpawnAction}（生成 {@code coe:wall[_fire|_cold]} 墙体实体）；
     * 墙的 {@code pulse} 行为周期点燃技能阶段的 ON_TICK = {@link DamageAction}
     * （对范围内敌人造成伤害，可附 {@code ailments} 异常如点燃 / 冰冻）。此结构复刻通用裂隙（generic_fissure），
     * 区别在于墙开启<b>真实碰撞</b>（Wall 运动）——除持续伤害外还能「阻挡技能 / 弹幕 / 移动」，
     * 这正是冰墙/火墙/盾墙作为「建筑实体」的本质，而非退化为地面云或直接冰冻。
     *
     * @param damageType 墙脉冲伤害类型（fire / cold / physical）
     * @param damage     单次脉冲伤害（等级查表表达式）
     * @param defRef     墙体形态 id（wall / wall_fire / wall_cold）
     * @param ailments   可选异常（点燃 / 冰冻等），无则留空
     */
    public static Skill wall(Require require, ResourceCost cost, String damageType,
                             ExpressionCodec.Value damage, String defRef, AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = new ArrayList<>();
        // ON_HIT：在施法者处生成墙体（self 投递只触发一次，安全无递归）
        actions.add(new TriggeredAction(LifecycleTrigger.ON_HIT,
                new SpawnAction(Identifier.fromNamespaceAndPath("coe", defRef),
                        ExpressionCodec.of(1.0), Optional.empty(),
                        slot("wall_duration"), slot("wall_interval"),
                        slot("wall_range"), slot("wall_health"), Optional.empty())));
        // ON_TICK：墙体脉冲对范围内敌人造成持续伤害（及可选异常）
        actions.add(new TriggeredAction(LifecycleTrigger.ON_TICK,
                new DamageAction(damageType, damage, Optional.empty(), ExpressionCodec.of(1.0))));
        for (AilmentData a : ailments) {
            actions.add(new TriggeredAction(LifecycleTrigger.ON_TICK,
                    new StatAction(new StatGroup(ailmentEntries(a)), false, Optional.empty(), ExpressionCodec.of(0.0))));
        }
        Map<String, ExpressionCodec.Value> params = Map.of(
                "wall_duration", ExpressionCodec.of(200.0),
                "wall_interval", ExpressionCodec.of(20.0),
                "wall_range", ExpressionCodec.of(2.0),
                "wall_health", ExpressionCodec.of(0.0),
                "wall_damage", ExpressionCodec.of(0.0));
        return build(require, cost, self(), actions, cast, List.of(), List.of("wall"), params);
    }

    /** 带技能级标签 + 诅咒范围/时长的组装（诅咒技能数据驱动施加范围/时长）。 */
    private static Skill build(Require require, ResourceCost cost, Delivery delivery,
                               List<TriggeredAction> actions, SkillCast cast, List<SkillModifier> modifiers,
                               InputMode input, List<String> tags,
                               Optional<ExpressionCodec.Value> curseRange,
                               Optional<ExpressionCodec.Value> curseDuration) {
        SkillPhase phase = new SkillPhase(Optional.of(delivery), actions, List.of(), Optional.empty());
        SkillPhasePlayer player = new SkillPhasePlayer(Optional.empty(), phase);
        return new Skill(require, "", Optional.empty(), cast, input, List.of(cost), List.of(player), modifiers, 1, tags,
                Optional.empty(), Skill.AoeRanges.of(curseRange, curseDuration, Optional.empty(), Optional.empty()), Map.of(),
                Optional.empty(), Optional.empty());
    }

    // ===================== 形态构造（无自身修正） =====================

    /** 法术投射物（智力系）：默认施法时间 20 tick、冷却 0、暴击 5%。 */
    public static Skill projectileSpell(Require require, ResourceCost cost,
                                        String damageType, double dmgBase, double dmgPer, double dmgAdd) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, projectile(damageType), List.of(hitDamage(damageType, dmgBase, dmgPer, dmgAdd)), cast, List.of());
    }

    /** 法术投射物（智力系），伤害按等级查表严格还原（替代线性估算）。 */
    public static Skill projectileSpell(Require require, ResourceCost cost,
                                        String damageType, ExpressionCodec.Value damage) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, projectile(damageType), List.of(hitDamage(damageType, damage)), cast, List.of());
    }

    /** 法术投射物（智力系）+ 数据驱动异常（如冰冻）：命中时按 {@code ailments} 施加异常。 */
    public static Skill projectileSpell(Require require, ResourceCost cost,
                                        String damageType, double dmgBase, double dmgPer, double dmgAdd,
                                        AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, projectile(damageType),
                hitDamageWithAilment(damageType, ExpressionCodec.ofSkillLevel(dmgBase, dmgPer, dmgAdd),
                        java.util.Arrays.asList(ailments)), cast, List.of());
    }

    /** 法术投射物（智力系），伤害按等级查表严格还原 + 数据驱动异常。 */
    public static Skill projectileSpell(Require require, ResourceCost cost,
                                        String damageType, ExpressionCodec.Value damage,
                                        AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, projectile(damageType),
                hitDamageWithAilment(damageType, damage, java.util.Arrays.asList(ailments)), cast, List.of());
    }

    /**
     * 法术球（智力系）：缓慢飞行、本体不命中、周期性对周围放电 —— POE2 天雷之珠一类。
     *
     * <p><b>与 {@link #projectileSpell} 的差别只在投递</b>：伤害 / 状态 / 事件依旧由
     * {@code actions} 定义（经 {@code ON_HIT} 点燃），只是触发者从「命中实体」变成
     * 「球每 {@code pulse_interval} tick 对半径内每个目标各放一次」。
     *
     * <p><b>伤害口径注意</b>：这里给的是<b>单次脉冲</b>的伤害，而球在其存活期内会脉冲多次，
     * 故同等数值下球的总输出远高于一次性命中的投射物 —— 接技能时必须按 POE2 真值的
     * 「单次伤害低、频率高」配比给值，不可直接沿用投射物的数值。
     */
    public static Skill ballSpell(Require require, ResourceCost cost,
                                  String damageType, double dmgBase, double dmgPer, double dmgAdd) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, ball(damageType), List.of(hitDamage(damageType, dmgBase, dmgPer, dmgAdd)), cast, List.of());
    }

    /**
     * 法术球（智力系），伤害按等级查表严格还原（替代线性估算）。
     *
     * <p><b>球技能必须走查表</b>：伤害是<b>单次脉冲</b>的值，而球会脉冲很多次，
     * 线性估算的误差会被脉冲次数成倍放大；POE2 球类技能的等级曲线又明显非线性，
     * 故必须逐等级还原，不能用 {@code base + per × level} 近似。
     */
    public static Skill ballSpell(Require require, ResourceCost cost,
                                  String damageType, ExpressionCodec.Value damage) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, ball(damageType), List.of(hitDamage(damageType, damage)), cast, List.of());
    }

    /** 法术球（智力系）+ 数据驱动异常（如点燃），伤害按等级查表：周期脉冲命中时施加异常。 */
    public static Skill ballSpell(Require require, ResourceCost cost,
                                  String damageType, ExpressionCodec.Value damage,
                                  AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, ball(damageType),
                hitDamageWithAilment(damageType, damage, java.util.Arrays.asList(ailments)), cast, List.of());
    }

    /** 法术球（智力系）+ 投放地面云：球落点留下指定地面云（如点燃地面）。 */
    public static Skill ballSpell(Require require, ResourceCost cost,
                                  String damageType, ExpressionCodec.Value damage,
                                  CreateGroundAction ground, AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.addAll(hitDamageWithAilment(damageType, damage, java.util.Arrays.asList(ailments)));
        actions.add(new TriggeredAction(LifecycleTrigger.ON_CAST, ground));
        return build(require, cost, ball(damageType), actions, cast, List.of());
    }

    /**
     * 危害物法术（智力系）：在目标位置生成一块<b>原地驻留的混沌地面</b>，敌人踏入即受混沌伤害
     * —— POE2 Blight 一类（地面持续危害，非瞬时命中）。
     *
     * <p><b>与 {@link #ballSpell}/{@link #orbSpell} 同构</b>：伤害 / 事件由 {@code actions} 定义，
     * 经实体 {@code on_trigger}=ON_HIT（敌人踏入 {@code contact(enemy)} 触发）点燃。区别在实体形态
     * （{@code static + contact(enemy)}，而非 pulse / ballistic），且<b>无脉冲节奏、靠敌人踏入触发</b>——
     * 这正是 {@code coe:hazard} 平台的设计意图，也是 {@link #hazard(double, double)} 的首个真实技能消费方。
     *
     * <p><b>真值口径</b>：Blight 为混沌地面、低费、短时（~4s）持续危害；半径 ~2m。
     * 伤害取自等级表（混沌中值曲线；真值待 poe2db 精确化，沿用同系混沌技能预算）。
     *
     * @param radius        作用半径（格）：~2.0
     * @param lifetimeTicks 存活时长（tick）：~80（4s）
     */
    public static Skill hazardSpell(Require require, ResourceCost cost, String damageType,
                                   ExpressionCodec.Value damage, double radius, int lifetimeTicks) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, hazard(radius, lifetimeTicks),
                List.of(hitDamage(damageType, damage)), cast, List.of());
    }

    /**
     * 法术 Orb（智力系）：在目标位置生成一个<b>原地驻场、按固定节奏周期放能</b>的球
     * —— POE2 风暴漩涡 / 日曜球一类。
     *
     * <p><b>与 {@link #ballSpell} 的差别只在运动</b>：伤害 / 状态 / 事件依旧由 {@code actions}
     * 定义（经 {@code ON_HIT} 点燃），触发者也同样是 {@code coe:pulse} 行为组件；
     * 区别是 Orb <b>不移动</b>（{@code static}），球<b>边飞边放</b>（{@code ballistic}）。
     *
     * <p><b>伤害口径（关键）</b>：这里给的必须是<b>单次脉冲</b>的伤害 —— POE2 真值表里的
     * 「Deals X to Y」正是每次放电的伤害（风暴漩涡 Lv20 = 14~260 → 中值 137）。
     * 球在其存活期内会放电多次，故总输出 = 单次 × 次数，
     * <b>不可</b>按「一次性范围伤害」的量级给值，也不可用 {@code base + per × level} 近似
     * （球类等级曲线明显非线性，必须 {@code ofLevelTable} 逐等级还原）。
     *
     * @param radius            作用半径（格）：POE2 真值 风暴漩涡 3.6 / 日曜球 2.5
     * @param lifetimeTicks     存活时长（tick）：POE2 真值 12 秒 = 240 / 10 秒 = 200
     * @param pulseIntervalTicks 脉冲间隔（tick）：POE2 真值 每 4 秒 = 80 / 每 1.4 秒 = 28
     * @param critChance        基础暴击率：POE2 真值 风暴漩涡 10% / 日曜球 7%
     */
    public static Skill orbSpell(Require require, ResourceCost cost, String damageType,
                                 ExpressionCodec.Value damage, double radius,
                                 int lifetimeTicks, int pulseIntervalTicks, double critChance) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(16.0), ExpressionCodec.of(0.0), ExpressionCodec.of(critChance)));
        // ON_PICKUP：球到期留下的残迹被拾取时授予闪电灌入 —— POE2 真值（Orb of Storms）
        // 「The Orb leaves behind a Lightning Infusion Remnant when it expires」。
        // 残迹由 coe:entity/orb 的 on_expire = Deliver(RemnantDelivery) 生成，拾取时经
        // (casterId, phaseId) 反查本阶段实例并点燃 ON_PICKUP（残片形态的拾取通道，与 ON_HIT 分离）。
        // 分离前拾取残迹会误点燃球的 ON_HIT（在玩家脚下再放一次电），既非真值也不合直觉。
        return build(require, cost, orb(damageType, radius, lifetimeTicks, pulseIntervalTicks),
                List.of(hitDamage(damageType, damage),
                        new TriggeredAction(LifecycleTrigger.ON_PICKUP, new InfusionRemnantAction("lightning"))),
                cast, List.of());
    }

    /**
     * 寒霜爆 Frost Bomb（智力系）：在目标位置生成一个<b>原地驻留</b>的寒霜球，
     * 周期内对半径内敌人施加冰霜曝曬（{@code on_trigger}=ON_HIT，脉冲冷伤），
     * 到期时引爆造成冰霜爆炸伤害（{@code on_expire}=ON_EXPIRE）。
     *
     * <p><b>通用接线</b>：复用 {@link #orb(EntityRead, double, int, int)} 投递平台（与风暴漩涡 / 日曜球同源），
     * 仅把 {@code ref} 指向 {@code coe:frost_bomb}（其形态 = {@code static ⊕ [pulse(20), detonate(200)]}），
     * 故无需任何新实体类 / 新投递 —— 完全遵循「通用接线而非各自单独」原则。
     *
     * <p><b>双效果通道</b>：{@code actions} 同时含 ON_HIT（周期冰霜曝曬）与 ON_EXPIRE（引爆伤害）两条，
     * 分别由 Pulse 行为（点燃 ON_HIT）与 Detonate 行为（点燃 ON_EXPIRE）驱动 ——
     * 这是 {@code on_expire} 第二效果通道的首个真实技能消费方。
     *
     * @param pulseDamage       单次脉冲冷伤（ON_HIT）；POE2 真值待精确化，暂沿用原 areaSpell 冷伤预算
     * @param detonateDamage    引爆冷伤（ON_EXPIRE）；同上
     * @param radius            作用半径（格）：POE2 真值 ~2.5
     * @param lifetimeTicks     存活时长（tick）：须 ≥ detonate 引信(200) → 取 200（同时到点引爆）
     * @param pulseIntervalTicks 脉冲间隔（tick）：实体形态自带 20（1 次/秒），此处显式注入保持一致
     * @param critChance        基础暴击率
     */
    public static Skill frostBombSpell(Require require, ResourceCost cost, String damageType,
                                       ExpressionCodec.Value pulseDamage, ExpressionCodec.Value detonateDamage,
                                       double radius, int lifetimeTicks, int pulseIntervalTicks, double critChance) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(16.0), ExpressionCodec.of(0.0), ExpressionCodec.of(critChance)));
        EntityRead ref = EntityRead.withRender(Identifier.parse("coe:frost_bomb"), Identifier.parse("coe:frost_bomb"));
        return build(require, cost, orb(ref, radius, lifetimeTicks, pulseIntervalTicks),
                List.of(
                        hitDamage(damageType, pulseDamage),                                // ON_HIT：周期冰霜曝曬
                        new TriggeredAction(LifecycleTrigger.ON_EXPIRE,
                                new DamageAction(damageType, detonateDamage,
                                        Optional.empty(), ExpressionCodec.of(1.0)))),     // ON_EXPIRE：引爆冰霜伤害
                cast, List.of());
    }

    /** 寒霜爆 + 数据驱动异常（如冰缓）：周期脉冲对命中敌人施加异常。 */
    public static Skill frostBombSpell(Require require, ResourceCost cost, String damageType,
                                       ExpressionCodec.Value pulseDamage, ExpressionCodec.Value detonateDamage,
                                       double radius, int lifetimeTicks, int pulseIntervalTicks, double critChance,
                                       AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(16.0), ExpressionCodec.of(0.0), ExpressionCodec.of(critChance)));
        EntityRead ref = EntityRead.withRender(Identifier.parse("coe:frost_bomb"), Identifier.parse("coe:frost_bomb"));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.add(hitDamage(damageType, pulseDamage));
        actions.add(new TriggeredAction(LifecycleTrigger.ON_EXPIRE,
                new DamageAction(damageType, detonateDamage, Optional.empty(), ExpressionCodec.of(1.0))));
        for (AilmentData a : ailments) {
            actions.add(new TriggeredAction(LifecycleTrigger.ON_HIT, new StatAction(new StatGroup(ailmentEntries(a)), false, Optional.empty(), ExpressionCodec.of(0.0))));
        }
        return build(require, cost, orb(ref, radius, lifetimeTicks, pulseIntervalTicks), actions, cast, List.of());
    }

    /**
     * 日曜球 Solar Orb（智力系）：原地悬停的火焰球，周期性释放火焰脉冲，
     * 到期时引爆并对附近敌人施加点燃（1 分钟）。
     *
     * <p><b>通用接线</b>：与 {@link #frostBombSpell} 同源——复用 {@link #orb(EntityRead, double, int, int)}
     * 投递平台，仅把 {@code ref} 指向 {@code coe:sun_orb}（形态 = {@code static ⊕ [pulse(28), detonate(200), aura(1m)]}，
     * 火元素）。双效果通道：ON_HIT 周期火焰脉冲 + ON_EXPIRE 烈焰爆破；逐目标点燃改由实体 {@code on_aura} 第四通道
     * （{@code aura} 行为，1m 半径周期施加 {@code coe:ignite}，1200t）真实落地，非 detonate 近似。
     *
     * <p><b>真值口径</b>：pulse 节奏 1.4s=28 tick（POE2 真值），存活 10s=200 tick，半径 2.5m，暴击 7%；
     * 单次脉冲伤害取自等级表（Lv20 中值 226，与 {@code orbSpell} 同源真值）。
     * 引爆伤害沿用同表（POE2 未单列 detonate 表，按「引爆 = 一次脉冲量级的火伤」还原）；
     * 点燃时长 1200 tick = 1 分钟（POE2 点燃基础 4s，此处为「持续点燃」强化——真值待精确化）。
     *
     * <p><b>真值闭合</b>：POE2 原文「极近敌人（1 米内）被点燃」由 {@code aura} 行为自带的独立 1m 半径真实还原，
     * 不再依赖 detonate 的 2.5m 作用域近似——第四效果通道 {@code on_aura} 由 {@code Aura} 行为周期驱动，
     * 数据驱动、无硬编码死值。
     *
     * @param pulseDamage       单次脉冲火伤（ON_HIT）
     * @param detonateDamage    引爆火伤（ON_EXPIRE）
     * @param radius            作用半径（格）：2.5
     * @param lifetimeTicks     存活时长（tick）：200（与 detonate 引信同时到点引爆）
     * @param pulseIntervalTicks 脉冲间隔（tick）：实体形态自带 28
     * @param critChance        基础暴击率：0.07
     */
    public static Skill sunOrbSpell(Require require, ResourceCost cost, String damageType,
                                    ExpressionCodec.Value pulseDamage, ExpressionCodec.Value detonateDamage,
                                    double radius, int lifetimeTicks, int pulseIntervalTicks, double critChance) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(16.0), ExpressionCodec.of(0.0), ExpressionCodec.of(critChance)));
        EntityRead ref = EntityRead.withRender(Identifier.parse("coe:sun_orb"), Identifier.parse("coe:orb_fire"));
        return build(require, cost, orb(ref, radius, lifetimeTicks, pulseIntervalTicks),
                List.of(
                        hitDamage(damageType, pulseDamage),                                // ON_HIT：周期火焰脉冲
                        new TriggeredAction(LifecycleTrigger.ON_EXPIRE,
                                new DamageAction(damageType, detonateDamage,
                                        Optional.empty(), ExpressionCodec.of(1.0)))),     // ON_EXPIRE：烈焰爆破（点燃改由实体 on_aura 光环持续施加）
                cast, List.of());
    }

    /** 日曜球 + 数据驱动异常（如点燃）：脉冲/引爆命中施加异常（光环点燃仍由实体 on_aura 通道处理）。 */
    public static Skill sunOrbSpell(Require require, ResourceCost cost, String damageType,
                                    ExpressionCodec.Value pulseDamage, ExpressionCodec.Value detonateDamage,
                                    double radius, int lifetimeTicks, int pulseIntervalTicks, double critChance,
                                    AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(16.0), ExpressionCodec.of(0.0), ExpressionCodec.of(critChance)));
        EntityRead ref = EntityRead.withRender(Identifier.parse("coe:sun_orb"), Identifier.parse("coe:orb_fire"));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.add(hitDamage(damageType, pulseDamage));
        actions.add(new TriggeredAction(LifecycleTrigger.ON_EXPIRE,
                new DamageAction(damageType, detonateDamage, Optional.empty(), ExpressionCodec.of(1.0))));
        for (AilmentData a : ailments) {
            actions.add(new TriggeredAction(LifecycleTrigger.ON_HIT, new StatAction(new StatGroup(ailmentEntries(a)), false, Optional.empty(), ExpressionCodec.of(0.0))));
        }
        return build(require, cost, orb(ref, radius, lifetimeTicks, pulseIntervalTicks), actions, cast, List.of());
    }

    /**
     * 投掷手雷（敏捷/力量系攻击）：抛物线飞出 → 落地 → 引信到期 → 范围引爆。
     * 伤害按武器攻击力 % 缩放（POE2 手雷真值：Attack Damage (215—878)% of base 等），
     * 故走攻击通道（WeaponAttack）而非法术固定值；半径与存活时长由投递装配（同其他形态）。
     *
     * <p>真值（poe2db Grenade_Skills）：手雷是 Attack，引信（Detonation Time）到期引爆，
     * 不参与 Fork/Chain；冷却 4~5s。本方法只覆盖「投掷 → 引爆」核心——
     * 弹跳 / 热度高伤 / 感电积蓄 / 多重投掷等修饰属辅助宝石或后续机制，不在首版接线内。
     *
     * @param radius        爆炸半径（格）：POE2 真值 2 米
     * @param lifetimeTicks 实体存活上限（tick）：须 &gt; 引信(60) + 飞行时间，留余量取 120
     * @param cooldownTicks 技能冷却（tick）：POE2 真值 5s=100 / 4s=80
     */
    public static Skill thrownAttack(Require require, ResourceCost cost, String damageType,
                                    double dmgBase, double dmgPer, double dmgAdd,
                                    double radius, int lifetimeTicks, double cooldownTicks) {
        SkillCast cast = SkillCast.of(new SkillCast.Attack(
                Either.left(new SkillCast.Attack.WeaponAttack(ExpressionCodec.of(1.0))),
                ExpressionCodec.of(cooldownTicks)));
        return build(require, cost, thrown(radius, lifetimeTicks),
                List.of(hitDamage(damageType, dmgBase, dmgPer, dmgAdd)), cast, List.of(),
                new Click(), List.of(GateTag.GRENADE));
    }

    /** 投掷攻击（敏捷系）+ 击退：手雷 / 标枪落地爆炸时击退敌人。 */
    public static Skill thrownAttack(Require require, ResourceCost cost, String damageType,
                                    double dmgBase, double dmgPer, double dmgAdd,
                                    double radius, int lifetimeTicks, double cooldownTicks, double knockback) {
        SkillCast cast = SkillCast.of(new SkillCast.Attack(
                Either.left(new SkillCast.Attack.WeaponAttack(ExpressionCodec.of(1.0))),
                ExpressionCodec.of(cooldownTicks)));
        return build(require, cost, thrown(radius, lifetimeTicks),
                List.of(hitDamageKnockback(damageType, dmgBase, dmgPer, dmgAdd, knockback)), cast, List.of(),
                new Click(), List.of(GateTag.GRENADE));
    }

    /** 投掷手雷 + 数据驱动异常（如感电）：引爆范围命中时施加异常。 */
    public static Skill thrownAttack(Require require, ResourceCost cost, String damageType,
                                    double dmgBase, double dmgPer, double dmgAdd,
                                    double radius, int lifetimeTicks, double cooldownTicks,
                                    AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Attack(
                Either.left(new SkillCast.Attack.WeaponAttack(ExpressionCodec.of(1.0))),
                ExpressionCodec.of(cooldownTicks)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.addAll(hitDamageWithAilment(damageType, ExpressionCodec.ofSkillLevel(dmgBase, dmgPer, dmgAdd),
                java.util.Arrays.asList(ailments)));
        return build(require, cost, thrown(radius, lifetimeTicks), actions, cast, List.of(),
                new Click(), List.of(GateTag.GRENADE));
    }

    /** 投掷手雷 + 击退 + 数据驱动异常（如感电）：引爆范围命中时击退并施加异常。 */
    public static Skill thrownAttack(Require require, ResourceCost cost, String damageType,
                                    double dmgBase, double dmgPer, double dmgAdd,
                                    double radius, int lifetimeTicks, double cooldownTicks,
                                    double knockback, AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Attack(
                Either.left(new SkillCast.Attack.WeaponAttack(ExpressionCodec.of(1.0))),
                ExpressionCodec.of(cooldownTicks)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.add(hitDamageKnockback(damageType, dmgBase, dmgPer, dmgAdd, knockback));
        actions.addAll(hitDamageWithAilment(damageType, ExpressionCodec.ofSkillLevel(dmgBase, dmgPer, dmgAdd),
                java.util.Arrays.asList(ailments)));
        return build(require, cost, thrown(radius, lifetimeTicks), actions, cast, List.of(),
                new Click(), List.of(GateTag.GRENADE));
    }

    /** 武器投射物（敏捷系攻击）：默认武器攻速、冷却 0。 */
    public static Skill projectileAttack(Require require, ResourceCost cost,
                                         String damageType, double dmgBase, double dmgPer, double dmgAdd) {
        SkillCast cast = SkillCast.of(new SkillCast.Attack(
                Either.left(new SkillCast.Attack.WeaponAttack(ExpressionCodec.of(1.0))),
                ExpressionCodec.of(0.0)));
        return build(require, cost, projectile(damageType), List.of(hitDamage(damageType, dmgBase, dmgPer, dmgAdd)), cast, List.of());
    }

    /**
     * 形态基础攻击（POE2 Druid 实证）：使用即变身。技能标签含 {@code shapeshifted} + 形态名
     * （bear/wolf/wyvern），经 {@code SkillInstance} 构造器回调自动 {@code StatusEngine.applyForm}
     * （施放法术则由同回调 {@code clearForms} 变回人类）。近战范围投送、武器攻击（非瞬发）。
     */
    public static Skill formAttack(Require require, ResourceCost cost, String damageType,
                                   double dmgBase, double dmgPer, double dmgAdd, String formTag) {
        return formAttack(require, cost, damageType, dmgBase, dmgPer, dmgAdd, formTag, 0);
    }

    /**
     * 形态基础攻击（带命中资源获得）：{@code rageOnHit > 0} 时每命中即时获得对应怒气
     * （POE2 熊形 Maul「Gain 5 Rage on Hit」，经 {@link StatGen} 中 {@code rage_on_hit}
     * 具体条目 → {@code ResourceStatEffect} HEAL 通道落地，遵循「引用侧 Read、条目入 StatGen」）。
     */
    public static Skill formAttack(Require require, ResourceCost cost, String damageType,
                                   double dmgBase, double dmgPer, double dmgAdd, String formTag, int rageOnHit) {
        SkillCast cast = SkillCast.of(new SkillCast.Attack(
                Either.left(new SkillCast.Attack.WeaponAttack(ExpressionCodec.of(1.0))),
                ExpressionCodec.of(0.0)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.add(hitDamage(damageType, dmgBase, dmgPer, dmgAdd));
        if (rageOnHit > 0) {
            // 模板条目必须经 ref 第二参注入 $value；否则 $value 未定义会静默回落 0.0，怒气恒为 0。
            actions.add(new TriggeredAction(LifecycleTrigger.ON_HIT,
                    new StatAction(new StatGroup(List.of(StatGen.ref(StatIds.RAGE_ON_HIT, rageOnHit))), false, Optional.empty(), ExpressionCodec.of(0.0))));
        }
        return build(require, cost, area(2.5), actions, cast, List.of(), new Click(),
                List.of(GateTag.SHAPESHIFTED, formTag));
    }

    /** 武器投射物（敏捷系攻击）+ 击退：命中时击退敌人。 */
    public static Skill projectileAttack(Require require, ResourceCost cost,
                                         String damageType, double dmgBase, double dmgPer, double dmgAdd,
                                         double knockback) {
        SkillCast cast = SkillCast.of(new SkillCast.Attack(
                Either.left(new SkillCast.Attack.WeaponAttack(ExpressionCodec.of(1.0))),
                ExpressionCodec.of(0.0)));
        return build(require, cost, projectile(damageType),
                List.of(hitDamageKnockback(damageType, dmgBase, dmgPer, dmgAdd, knockback)), cast, List.of());
    }

    /** 武器投射物（敏捷系攻击），伤害按等级查表严格还原。 */
    public static Skill projectileAttack(Require require, ResourceCost cost,
                                         String damageType, ExpressionCodec.Value damage) {
        SkillCast cast = SkillCast.of(new SkillCast.Attack(
                Either.left(new SkillCast.Attack.WeaponAttack(ExpressionCodec.of(1.0))),
                ExpressionCodec.of(0.0)));
        return build(require, cost, projectile(damageType), List.of(hitDamage(damageType, damage)), cast, List.of());
    }

    /** 武器投射物（敏捷系攻击）+ 数据驱动异常（如中毒）：命中时按 {@code ailments} 施加异常。 */
    public static Skill projectileAttack(Require require, ResourceCost cost,
                                         String damageType, double dmgBase, double dmgPer, double dmgAdd,
                                         AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Attack(
                Either.left(new SkillCast.Attack.WeaponAttack(ExpressionCodec.of(1.0))),
                ExpressionCodec.of(0.0)));
        return build(require, cost, projectile(damageType),
                hitDamageWithAilment(damageType, ExpressionCodec.ofSkillLevel(dmgBase, dmgPer, dmgAdd),
                        java.util.Arrays.asList(ailments)), cast, List.of());
    }

    /** 武器投射物（敏捷系攻击）+ 投放地面云：落点留下指定地面云（如点燃地面）。 */
    public static Skill projectileAttack(Require require, ResourceCost cost,
                                        String damageType, double dmgBase, double dmgPer, double dmgAdd,
                                        CreateGroundAction ground, AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Attack(
                Either.left(new SkillCast.Attack.WeaponAttack(ExpressionCodec.of(1.0))),
                ExpressionCodec.of(0.0)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.addAll(hitDamageWithAilment(damageType, ExpressionCodec.ofSkillLevel(dmgBase, dmgPer, dmgAdd),
                java.util.Arrays.asList(ailments)));
        actions.add(new TriggeredAction(LifecycleTrigger.ON_CAST, ground));
        return build(require, cost, projectile(damageType), actions, cast, List.of());
    }

    /** 武器投射物（敏捷系攻击），伤害按等级查表严格还原 + 数据驱动异常。 */
    public static Skill projectileAttack(Require require, ResourceCost cost,
                                         String damageType, ExpressionCodec.Value damage,
                                         AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Attack(
                Either.left(new SkillCast.Attack.WeaponAttack(ExpressionCodec.of(1.0))),
                ExpressionCodec.of(0.0)));
        return build(require, cost, projectile(damageType),
                hitDamageWithAilment(damageType, damage, java.util.Arrays.asList(ailments)), cast, List.of());
    }

    /** 范围/猛击（力量系）：默认施法时间 20 tick、冷却 0、暴击 5%。 */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, double dmgBase, double dmgPer, double dmgAdd, double radius) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, area(radius), List.of(hitDamage(damageType, dmgBase, dmgPer, dmgAdd)), cast, List.of());
    }

    /**
     * 范围猛击 + 投放地面（POE2 Ground 子系统）：在技能落点留下指定地面云。
     * 地面经 {@link CreateGroundAction} 投放，其 {@code inc_<ground>_magnitude/duration}
     * 作用域缩放在此真正生效（见该动作注释）。地面标签已进入 GroundTypes 描述符 的
     * 存在性状态定义，因此节点只需引用对应的 {@code damage_taken_on_*} / {@code chance_aggravate_*} 条目。
     */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, double dmgBase, double dmgPer, double dmgAdd, double radius,
                                  CreateGroundAction ground) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.add(hitDamage(damageType, dmgBase, dmgPer, dmgAdd));
        actions.add(new TriggeredAction(LifecycleTrigger.ON_CAST, ground));
        return build(require, cost, area(radius), actions, cast, List.of());
    }

    /** 范围猛击 + 投放战旗（POE2 Banner 子系统）：在技能落点放置战旗云，对区域内友方刷新增益。 */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, double dmgBase, double dmgPer, double dmgAdd, double radius,
                                  BannerAction banner) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.add(hitDamage(damageType, dmgBase, dmgPer, dmgAdd));
        actions.add(new TriggeredAction(LifecycleTrigger.ON_CAST, banner));
        return build(require, cost, area(radius), actions, cast, List.of());
    }

    /** 范围/猛击（力量系），伤害按等级查表严格还原。 */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, ExpressionCodec.Value damage, double radius) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, area(radius), List.of(hitDamage(damageType, damage)), cast, List.of());
    }

    // ===================== 形态构造（携带自身修正） =====================

    /** 法术投射物（智力系），携带技能自身 modifiers（与辅助宝石共用同一套形状）。 */
    public static Skill projectileSpell(Require require, ResourceCost cost,
                                        String damageType, double dmgBase, double dmgPer, double dmgAdd,
                                        List<SkillModifier> modifiers) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, projectile(damageType), List.of(hitDamage(damageType, dmgBase, dmgPer, dmgAdd)), cast, modifiers);
    }

    /** 法术投射物（智力系），携带技能自身 modifiers，伤害按等级查表严格还原。 */
    public static Skill projectileSpell(Require require, ResourceCost cost,
                                        String damageType, ExpressionCodec.Value damage,
                                        List<SkillModifier> modifiers) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, projectile(damageType), List.of(hitDamage(damageType, damage)), cast, modifiers);
    }

    /** 范围/猛击（力量系），携带技能自身 modifiers（与辅助宝石共用同一套形状）。 */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, double dmgBase, double dmgPer, double dmgAdd, double radius,
                                  List<SkillModifier> modifiers) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, area(radius), List.of(hitDamage(damageType, dmgBase, dmgPer, dmgAdd)), cast, modifiers);
    }

    /**
     * 范围猛击 + 额外 ON_HIT 动作链（如消费异常 payoff）：落点命中造成伤害后，追加给定 ON_HIT 动作
     * （典型为 {@link #consumeAilmentOnHit} 接 Consume Bleeding → 血爆）。
     * 用于把「命中触发」型回报动作挂到范围技能上，而不改其命中伤害形态。
     */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, double dmgBase, double dmgPer, double dmgAdd,
                                  double radius, TriggeredAction... extraActions) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.add(hitDamage(damageType, dmgBase, dmgPer, dmgAdd));
        for (TriggeredAction a : extraActions) actions.add(a);
        return build(require, cost, area(radius), actions, cast, List.of());
    }

    /** 范围/猛击（力量系），携带技能自身 modifiers，伤害按等级查表严格还原。 */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, ExpressionCodec.Value damage, double radius,
                                  List<SkillModifier> modifiers) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, area(radius), List.of(hitDamage(damageType, damage)), cast, modifiers);
    }

    // ===================== 扩展形态（可完整实现） =====================

    /** 范围猛击 + 击退（力量系）。 */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, double dmgBase, double dmgPer, double dmgAdd,
                                  double radius, double knockback) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, area(radius),
                List.of(hitDamageKnockback(damageType, dmgBase, dmgPer, dmgAdd, knockback)), cast, List.of());
    }

    /** 范围猛击 + 击退（力量系），伤害按等级查表严格还原。 */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, ExpressionCodec.Value damage,
                                  double radius, double knockback) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, area(radius),
                List.of(hitDamageKnockback(damageType, damage, knockback)), cast, List.of());
    }

    /** 范围猛击 + 数据驱动异常（力量系）：伤害命中时按 {@code ailments} 施加异常（如击晕 / 重晕）。 */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, double dmgBase, double dmgPer, double dmgAdd,
                                  double radius, AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, area(radius),
                hitDamageWithAilment(damageType, ExpressionCodec.ofSkillLevel(dmgBase, dmgPer, dmgAdd),
                        java.util.Arrays.asList(ailments)),
                cast, List.of());
    }

    /** 范围猛击 + 数据驱动异常（力量系），伤害按等级查表严格还原：命中时按 {@code ailments} 施加异常。 */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, ExpressionCodec.Value damage,
                                  double radius, AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, area(radius),
                hitDamageWithAilment(damageType, damage, java.util.Arrays.asList(ailments)),
                cast, List.of());
    }

    /** 范围猛击 + 击退 + 数据驱动异常（力量系）：落点命中同时击退并施加异常（如冰冻+冻结）。 */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, double dmgBase, double dmgPer, double dmgAdd,
                                  double radius, double knockback, AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.add(hitDamageKnockback(damageType, dmgBase, dmgPer, dmgAdd, knockback));
        actions.addAll(hitDamageWithAilment(damageType, ExpressionCodec.ofSkillLevel(dmgBase, dmgPer, dmgAdd),
                java.util.Arrays.asList(ailments)));
        return build(require, cost, area(radius), actions, cast, List.of());
    }

    /** 范围猛击 + 击退 + 数据驱动异常（力量系），伤害按等级查表：命中时击退并施加异常。 */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, ExpressionCodec.Value damage,
                                  double radius, double knockback, AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.add(hitDamageKnockback(damageType, damage, knockback));
        actions.addAll(hitDamageWithAilment(damageType, damage, java.util.Arrays.asList(ailments)));
        return build(require, cost, area(radius), actions, cast, List.of());
    }

    /** 范围猛击 + 数据驱动异常 + 投放地面云：落点命中施加异常并留下指定地面云（如冰缓地面）。 */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, double dmgBase, double dmgPer, double dmgAdd,
                                  double radius, CreateGroundAction ground, AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.addAll(hitDamageWithAilment(damageType, ExpressionCodec.ofSkillLevel(dmgBase, dmgPer, dmgAdd),
                java.util.Arrays.asList(ailments)));
        actions.add(new TriggeredAction(LifecycleTrigger.ON_CAST, ground));
        return build(require, cost, area(radius), actions, cast, List.of());
    }

    /** 范围猛击 + 投放地面云：落点留下指定地面云（如感电地面），伤害按等级查表（无击退）。 */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, ExpressionCodec.Value damage,
                                  double radius, CreateGroundAction ground, AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.addAll(hitDamageWithAilment(damageType, damage, java.util.Arrays.asList(ailments)));
        actions.add(new TriggeredAction(LifecycleTrigger.ON_CAST, ground));
        return build(require, cost, area(radius), actions, cast, List.of());
    }

    /** 范围猛击 + 击退 + 数据驱动异常 + 投放地面云：落点命中击退、施加异常并留下指定地面云。 */
    public static Skill areaSpell(Require require, ResourceCost cost,
                                  String damageType, ExpressionCodec.Value damage,
                                  double radius, double knockback, CreateGroundAction ground,
                                  AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.add(hitDamageKnockback(damageType, damage, knockback));
        actions.addAll(hitDamageWithAilment(damageType, damage, java.util.Arrays.asList(ailments)));
        actions.add(new TriggeredAction(LifecycleTrigger.ON_CAST, ground));
        return build(require, cost, area(radius), actions, cast, List.of());
    }

    /** 点燃异常构造器（数据驱动）：buildup 为单次命中累积量。 */
    public static AilmentData ignite(double buildup) { return ailment(StatusDefs.IGNITE, buildup); }

    /** 流血异常构造器（数据驱动）。 */
    public static AilmentData bleed(double buildup) { return ailment(StatusDefs.BLEED, buildup); }

    /** 感电（Electrocute）异常构造器（数据驱动）。 */
    public static AilmentData electrocute(double buildup) { return ailment(StatusDefs.ELECTROCUTE, buildup); }

    /** 中毒（Poison）异常构造器（数据驱动）。 */
    public static AilmentData poison(double buildup) { return ailment(StatusDefs.POISON, buildup); }

    /** 冰冻（Chill）异常构造器（数据驱动）：缓速，PARTIAL 累积。 */
    public static AilmentData chill(double buildup) { return ailment(StatusDefs.CHILL, buildup); }

    /** 冻结（Freeze）异常构造器（数据驱动）：全缓速（重 chill），THRESHOLD 累积。 */
    public static AilmentData freeze(double buildup) { return ailment(StatusDefs.FREEZE, buildup); }

    /** 晕眩（Stun）异常构造器（数据驱动）：与红系私有 stun 同义，提为工厂公开以便任意 gen 复用。 */
    public static AilmentData stun(double buildup) { return ailment(StatusDefs.STUN, buildup); }

    private static AilmentData ailment(StatusType type, double buildup) {
        return new AilmentData(type, ExpressionCodec.of(buildup),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** 光束法术（智力系）：沿施法者朝向发射能量束，瞬发命中范围内敌人。 */
    public static Skill beamSpell(Require require, ResourceCost cost,
                                  String damageType, double dmgBase, double dmgPer, double dmgAdd,
                                  double range, double width) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, beam(range, width),
                List.of(hitDamage(damageType, dmgBase, dmgPer, dmgAdd)), cast, List.of());
    }

    /** 光束法术（智力系）+ 数据驱动异常（如感电）：命中时按 {@code ailments} 施加异常。 */
    public static Skill beamSpell(Require require, ResourceCost cost,
                                  String damageType, double dmgBase, double dmgPer, double dmgAdd,
                                  double range, double width, AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, beam(range, width),
                hitDamageWithAilment(damageType, ExpressionCodec.ofSkillLevel(dmgBase, dmgPer, dmgAdd),
                        java.util.Arrays.asList(ailments)), cast, List.of());
    }

    /** 光束法术（智力系）+ 投放地面云：落点留下指定地面云（如点燃地面）。 */
    public static Skill beamSpell(Require require, ResourceCost cost,
                                 String damageType, double dmgBase, double dmgPer, double dmgAdd,
                                 double range, double width, CreateGroundAction ground, AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.addAll(hitDamageWithAilment(damageType, ExpressionCodec.ofSkillLevel(dmgBase, dmgPer, dmgAdd),
                java.util.Arrays.asList(ailments)));
        actions.add(new TriggeredAction(LifecycleTrigger.ON_CAST, ground));
        return build(require, cost, beam(range, width), actions, cast, List.of());
    }

    /** 光束法术（智力系），伤害按等级查表严格还原。 */
    public static Skill beamSpell(Require require, ResourceCost cost,
                                  String damageType, ExpressionCodec.Value damage,
                                  double range, double width) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(20.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        return build(require, cost, beam(range, width),
                List.of(hitDamage(damageType, damage)), cast, List.of());
    }

    /** 位移技能（敏捷/力量系）：瞬发位移（冲刺/传送）+ 落点范围伤害。 */
    public static Skill movementSkill(Require require, ResourceCost cost, MoveAction.MoveType moveType, double force,
                                      String damageType, double dmgBase, double dmgPer, double dmgAdd, double radius) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(10.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = List.of(
                new TriggeredAction(LifecycleTrigger.ON_CAST,
                        new MoveAction(moveType, ExpressionCodec.of(force), Optional.empty())),
                hitDamage(damageType, dmgBase, dmgPer, dmgAdd));
        return build(require, cost, area(radius), actions, cast, List.of());
    }

    /** 位移技能（敏捷/力量系），伤害按等级查表严格还原。 */
    public static Skill movementSkill(Require require, ResourceCost cost, MoveAction.MoveType moveType, double force,
                                      String damageType, ExpressionCodec.Value damage, double radius) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(10.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = List.of(
                new TriggeredAction(LifecycleTrigger.ON_CAST,
                        new MoveAction(moveType, ExpressionCodec.of(force), Optional.empty())),
                hitDamage(damageType, damage));
        return build(require, cost, area(radius), actions, cast, List.of());
    }

    /** 位移技能（敏捷/力量系）+ 击退：落点范围命中附带击退。 */
    public static Skill movementSkill(Require require, ResourceCost cost, MoveAction.MoveType moveType, double force,
                                      String damageType, double dmgBase, double dmgPer, double dmgAdd, double radius,
                                      double knockback) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(10.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = List.of(
                new TriggeredAction(LifecycleTrigger.ON_CAST,
                        new MoveAction(moveType, ExpressionCodec.of(force), Optional.empty())),
                hitDamageKnockback(damageType, dmgBase, dmgPer, dmgAdd, knockback));
        return build(require, cost, area(radius), actions, cast, List.of());
    }

    /** 位移技能（敏捷/力量系）+ 数据驱动异常：落点范围命中时按 {@code ailments} 施加异常。 */
    public static Skill movementSkill(Require require, ResourceCost cost, MoveAction.MoveType moveType, double force,
                                      String damageType, double dmgBase, double dmgPer, double dmgAdd, double radius,
                                      AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(10.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.add(new TriggeredAction(LifecycleTrigger.ON_CAST,
                new MoveAction(moveType, ExpressionCodec.of(force), Optional.empty())));
        actions.addAll(hitDamageWithAilment(damageType, ExpressionCodec.ofSkillLevel(dmgBase, dmgPer, dmgAdd),
                java.util.Arrays.asList(ailments)));
        return build(require, cost, area(radius), actions, cast, List.of());
    }

    /** 位移技能（敏捷/力量系）+ 击退 + 数据驱动异常：落点命中同时击退并施加异常（如晕眩）。 */
    public static Skill movementSkill(Require require, ResourceCost cost, MoveAction.MoveType moveType, double force,
                                      String damageType, double dmgBase, double dmgPer, double dmgAdd, double radius,
                                      double knockback, AilmentData... ailments) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(10.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.05)));
        List<TriggeredAction> actions = new ArrayList<>();
        actions.add(new TriggeredAction(LifecycleTrigger.ON_CAST,
                new MoveAction(moveType, ExpressionCodec.of(force), Optional.empty())));
        actions.add(hitDamageKnockback(damageType, dmgBase, dmgPer, dmgAdd, knockback));
        actions.addAll(hitDamageWithAilment(damageType, ExpressionCodec.ofSkillLevel(dmgBase, dmgPer, dmgAdd),
                java.util.Arrays.asList(ailments)));
        return build(require, cost, area(radius), actions, cast, List.of());
    }

    /** 自身增益技能（守护/捷类）：对自身施加属性修改器（ON_CAST），持续 duration tick。 */
    public static Skill selfBuff(Require require, ResourceCost cost, List<ModifierAction> mods, double duration) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(0.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.0)));
        List<TriggeredAction> actions = mods.stream()
                .map(m -> new TriggeredAction(LifecycleTrigger.ON_CAST, m))
                .toList();
        return build(require, cost, self(), actions, cast, List.of());
    }

    /**
     * 自身增益技能（守护/捷类），经 Stat 引擎节点施加（ON_CAST），持续 duration tick。
     * 区别于 {@link #selfBuff(Require, ResourceCost, List, double)} 直连 Attribute，本方法走
     * {@link StatAction} -> StatStore 管线（与天赋/精魂同源），幅度由传入的 Stat 节点（如
     * {@code StatGen.ref(StatIds.X, value)}）决定。
     */
    public static Skill selfStatBuff(Require require, ResourceCost cost, List<StatEntry> stats, double duration) {
        SkillCast cast = SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(0.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.0)));
        List<TriggeredAction> actions = List.of(
                new TriggeredAction(LifecycleTrigger.ON_CAST,
                        new StatAction(new StatGroup(stats), false, Optional.empty(), ExpressionCodec.of(duration))));
        return build(require, cost, self(), actions, cast, List.of());
    }

    /**
     * 自身增益（通用便捷版）：对自身施加某属性的持续修改器（默认常驻 200 tick）。
     * 供批量生成「自身增益(selfBuff)」类技能使用。
     * @param attrName 属性注册名（如 {@code "fire_damage"}，对应 {@code coe:fire_damage}）
     * @param operation 0=加法 / 1=乘算总计 / 2=乘算基础
     * @param value 修改量（乘算时为比例，如 0.20 = +20%）
     */
    public static Skill selfBuff(Require require, ResourceCost cost, String attrName, int operation, double value) {
        List<ModifierAction> mods = List.of(new ModifierAction(
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, attrName),
                ExpressionCodec.of((double) operation),
                ExpressionCodec.of(value),
                ExpressionCodec.of(200.0)));
        return selfBuff(require, cost, mods, 200.0);
    }

    /**
     * 施放声画身份默认注入：对未显式声明 {@code castSound}/{@code castParticle} 的技能，按宝石配色与技能
     * 路径家族推导主题化施放音效 + 粒子（经 {@code SkillInstance#emitReleaseBeat} 在服务端释放节拍广播，
     * 客户端自动渲染，与肢体动画严格对齐）。
     * <p>已显式声明任一项的技能（如 {@code generic_wave} 预设）原样返回，绝不覆盖手工设定。
     * 仅使用原版 Minecraft 音效/粒子标识符——标识符在客户端不存在时消费端静默降级（{@code instanceof ParticleOptions}
     * 守卫），零崩溃风险。属 Task #377「客户端效果匹配」的批量基线补齐。</p>
     */
    public static Skill withDefaultCastFx(String path, Skill skill) {
        if (skill.castSound().isPresent() || skill.castParticle().isPresent()) return skill;
        String color = path.contains("/") ? path.substring(0, path.indexOf('/')) : "";
        String p = path.toLowerCase();
        Identifier sound;
        Identifier particle;
        if (p.contains("warcry") || p.contains("cry") || p.contains("banner")) {
            sound = Identifier.fromNamespaceAndPath("minecraft", "entity.ravager.roar");
            particle = Identifier.fromNamespaceAndPath("minecraft", "angry_villager");
        } else if (p.contains("totem")) {
            sound = Identifier.fromNamespaceAndPath("minecraft", "block.bell.use");
            particle = Identifier.fromNamespaceAndPath("minecraft", "note");
        } else if (p.contains("curse") || p.contains("contagion") || p.contains("despair")
                || p.contains("conductivity") || p.contains("weakness") || p.contains("vulnerability")
                || p.contains("enfeeble") || p.contains("snap") || p.contains("mark") || p.contains("hex")) {
            sound = Identifier.fromNamespaceAndPath("minecraft", "block.beacon.activate");
            particle = Identifier.fromNamespaceAndPath("minecraft", "reverse_portal");
        } else if (p.contains("summon") || p.contains("companion") || p.contains("minion")) {
            sound = Identifier.fromNamespaceAndPath("minecraft", "item.totem.use");
            particle = Identifier.fromNamespaceAndPath("minecraft", "totem_of_undying");
        } else if (p.contains("aura") || p.contains("plague") || p.contains("bearer")
                || p.contains("storm") || p.contains("blight") || p.contains("herald")) {
            sound = Identifier.fromNamespaceAndPath("minecraft", "block.beacon.activate");
            particle = Identifier.fromNamespaceAndPath("minecraft", "cloud");
        } else if ("blue".equals(color)) {
            sound = Identifier.fromNamespaceAndPath("minecraft", "entity.shulker.shoot");
            particle = Identifier.fromNamespaceAndPath("minecraft", "small_flame");
        } else if ("green".equals(color)) {
            sound = Identifier.fromNamespaceAndPath("minecraft", "entity.witch.throw");
            particle = Identifier.fromNamespaceAndPath("minecraft", "witch");
        } else {
            sound = Identifier.fromNamespaceAndPath("minecraft", "block.anvil.land");
            particle = Identifier.fromNamespaceAndPath("minecraft", "poof");
        }
        return skill.withCastFx(Optional.of(sound), Optional.of(particle));
    }

}
