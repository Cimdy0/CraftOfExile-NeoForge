package com.cimdy.coe.data.codec.support.stat.engine;

import com.cimdy.coe.data.codec.support.stat.condition.ConditionBridge;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionScope;
import com.cimdy.coe.data.codec.support.stat.condition.RelationCondition;
import com.cimdy.coe.data.codec.support.stat.condition.StatCondition;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.support.attribute.AttributeLimiters;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.IgniteHistoryFireValue;
import com.cimdy.coe.data.codec.support.stat.effect.base.*;
import com.cimdy.coe.data.codec.support.stat.effect.convert.*;
import com.cimdy.coe.data.codec.support.stat.effect.damage.*;
import com.cimdy.coe.data.codec.support.stat.effect.grant.*;
import com.cimdy.coe.data.codec.support.stat.effect.marker.*;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.*;
import com.cimdy.coe.data.codec.support.stat.effect.status.*;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.event.TriggerContext;
import com.cimdy.coe.helper.OwnerHelper;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * 真实瞬间 stat 引擎：纯函数式地把天赋 {@link ModifierStatEffect} 聚合成某属性在某事件作用域下的贡献，
 * <b>不向原版属性写入任何全局修饰符</b>，因此并发事件各自独立求值、互不串味。
 *
 * <p>聚合语义对齐原版属性修饰符：
 * <pre>
 *   out = original + Σadd
 *         + attributeBaseValue * ΣmultBase
 *   out *= (1 + ΣmultTotal)
 *   out = clamp(out, limiters)   // SET_MIN / SET_MAX / SET_VALUE（按作用域）
 * </pre>
 *
 * <p>读取路径（{@link #fold}）由 {@code LivingAttributeValue} mixin 在 {@code getAttributeValue} 返回时调用，
 * 因此任何读属性的代码（伤害计算、math 表达式、状态判定）都自动获得事件作用域内的 stat 加成。
 */
public final class StatEngine {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("StatEngine");

    /** COMPUTING 重入已告警的属性（同属性全局仅告警一次，防每帧刷屏；WeakHashMap 防泄漏）。 */
    private static final Set<Holder<Attribute>> COMPUTE_CYCLE_WARNED =
            Collections.newSetFromMap(new java.util.WeakHashMap<>());

    private StatEngine() {
    }

    /** 防递归：stat 公式可能再读属性（嵌套 mixin → 本引擎），同属性重入时返回空贡献。 */
    private static final ThreadLocal<Set<Holder<Attribute>>> COMPUTING = ThreadLocal.withInitial(HashSet::new);

    /**
     * 防递归（触发通道）：正在执行 {@link StatEffect#onTrigger} 的条目集合（按实例身份）。
     * <p>
     * 触发型效果会造成真实副作用（伤害 / 资源 / 异常），这些副作用又会回流伤害管线并再次
     * {@link #fire}，故必须守卫，否则「击中时造成伤害」这类条目会自我重入至 StackOverflow：
     * <pre>
     *   DamageStatEffect.onTrigger → EffectAppliers.damage → target.hurt
     *     → DamagePipeline.stageTrigger → StatEngine.fire(HIT) → 同一条目 → …
     * </pre>
     * <b>粒度取条目实例</b>而非整个 fire 调用：只阻断「同一条目自我重入」，
     * 保留「条目 A 的伤害触发条目 B」这类合法跨条目连锁。
     */
    private static final ThreadLocal<Set<StatEntry>> FIRING =
            ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

    /** 触发通道总递归深度上限：防 A→B→A→B 双条目互激环（单条目自激已由 {@link #FIRING} 挡住）。 */
    private static final int MAX_FIRE_DEPTH = 8;

    /** 当前触发通道递归深度。 */
    private static final ThreadLocal<int[]> FIRE_DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    /** 某属性在某作用域下的聚合结果（纯内存，无全局副作用）。 */
    public record StatResult(double add, double multBase, double multTotal, AttributeLimiters limiters, double replace,
                             double penaltyMult, double penaltyReduce) {
        public static final StatResult EMPTY = new StatResult(0, 0, 0, AttributeLimiters.EMPTY, Double.NaN, 0.0, 0.0);

        public boolean isEmpty() {
            return this == EMPTY;
        }
    }

    /**
     * 计算实体在某作用域下某属性的 stat 贡献。无数据 / 无相关条目 / 重入 → 返回 {@link StatResult#EMPTY}。
     * <p>
     * 聚合维度：
 * <ul>
 *   <li><b>自身条目</b>：来自 {@code living} 自身挂载、且 {@code resolveTargets} 含 {@code living} 的条目
 *       （SELF 选择器 → {@code [owner]}，含被动与命中标签匹配项）。</li>
 *   <li><b>敌方条目</b>：当作用域携带 {@code sourceEntity}（命中受害者作用域持有攻击者）时，
 *       额外聚合攻击者挂载、且 {@code resolveTargets} 含 {@code living}（受害者）的条目
 *       （EVENT_TARGET 选择器 → {@code [受害者]}），但<b>取数与条件按攻击者上下文求值</b>
 *       （如「被攻击的敌人抗性降低」由受害者读取自身抗性时生效，数值来自攻击者天赋）。</li>
 * </ul>
     */
    public static StatResult compute(LivingEntity living, Holder<Attribute> attribute, StatContext scope) {
        if (!living.hasData(AttachmentRegistry.STAT_STORE)) return StatResult.EMPTY;
        Set<Holder<Attribute>> inProgress = COMPUTING.get();
        if (inProgress.contains(attribute)) {
            // 重入即断环（读 A 时算 B、B 又读 A）：告警一次使行为可观测，防静默退化
            if (COMPUTE_CYCLE_WARNED.add(attribute)) {
                LOGGER.warn("StatEngine cyclic compute detected on attribute {} (chain={}); returning EMPTY",
                        attribute, inProgress);
            }
            return StatResult.EMPTY;
        }

        inProgress.add(attribute);
        try {
            // 读取作用域并入「属性语义标签」：读点只带本次行为的下位标签（如 {fire}），
            // 属性自带的语义标签（如 {resistance}）由引擎统一补。否则要求 {resistance, fire}
            // 这类细分条目在所有抗性/穿透读点永不命中（已接线却静默失效）。
            StatContext readScope = withAttributeTags(scope, attribute);
            Acc acc = new Acc();
            LivingEntity src = readScope.sourceEntity();
            // 自身存储内的条目（self 维度）：作用于 living 自身读取，取值/条件按 living 自身，触发目标=src
            aggregate(acc, living, living, attribute, readScope, new StatEvalContext(living, src));
            // 命中结算：攻击者 src 存储内「作用于敌方」的条目作用于受害者 living 的属性读取，
            // 取值/条件按攻击者（storeOwner=src）上下文，触发目标=受害者 living
            if (src != null && src != living && src.hasData(AttachmentRegistry.STAT_STORE)) {
                aggregate(acc, src, living, attribute, readScope, new StatEvalContext(src, living));
            }
            // 召唤/图腾等作用域维（scope 轴）：reader 若为某 master 的召唤体，聚合 master 存储中
            // Condition 取「该 master 的召唤体集合」且 resolveCondition 含 reader 的条目；
            // 数值/条件按 master 上下文求值。使 entityMod(base_attr, scope) 在 fold 路径生效
            // （「你的随从造成 +X% 伤害」「图腾 +X% 护甲」等 owner→召唤体 作用域归一条目）。
            LivingEntity master = OwnerHelper.getOwner(living) instanceof LivingEntity le ? le : null;
            if (master != null && master != living && master.hasData(AttachmentRegistry.STAT_STORE)) {
                aggregate(acc, master, living, attribute, readScope, new StatEvalContext(master, living));
            }

            AttributeLimiters limiters = acc.limiterBuilder != null
                    ? acc.limiterBuilder.build() : AttributeLimiters.EMPTY;
            return new StatResult(acc.sumAdd, acc.sumMultBase, acc.sumMultTotal, limiters, acc.replace,
                    acc.penaltyMult, acc.penaltyReduce);
        } finally {
            inProgress.remove(attribute);
        }
    }

    /**
     * 把「属性语义标签」并入读取作用域（见 {@code AttributeRegistry#scopeTagsOf}）。
     * 标签表达式为<b>与</b>语义，并集只会让更多细分条目命中、不会使既有命中失效。
     */
    private static StatContext withAttributeTags(StatContext scope, Holder<Attribute> attribute) {
        Set<String> implicit = AttributeRegistry.scopeTagsOf(attribute);
        if (implicit.isEmpty() || scope.tags().containsAll(implicit)) return scope;
        Set<String> merged = new HashSet<>(scope.tags());
        merged.addAll(implicit);
        return new StatContext(merged, scope.skill(), scope.sourceEntity(), scope.eventName(), scope.hitContext());
    }

    /**
     * 召唤类作用域标签（{@code minion} / {@code totem} / {@code companion} / {@code ally}）注入：
     * 条目的 Condition 已声明「作用于持有者的该类召唤体」，且门控 {@code resolveCondition} 已确认
     * reader 就是解析集成员，故 {@code {minion}} 这类召唤类别门控在此成立。缺此注入时读取端只带
     * 本次行为标签（如 {@code {fire}}），要求 {@code {minion, fire}} 的随从细分条目永不命中。
     *
     * <p>Phase 2b：Target 收敛为 SELF / TRIGGER_TARGET 两态后，召唤类别不再是 Target 的属性，
     * 改由 {@link RelationCondition} 这一 Condition 表达，故本注入改从条目的 Condition 读取类别名
     * （{@link RelationCondition.Relation#getSerializedName()} 与旧 Source 序列化名逐字一致）。
     */
    private static StatContext withSummonTag(StatContext scope, StatEntry e) {
        String tag = summonRelationTag(e);
        if (tag == null || scope.tags().contains(tag)) return scope;
        Set<String> merged = new HashSet<>(scope.tags());
        merged.add(tag);
        return new StatContext(merged, scope.skill(), scope.sourceEntity(), scope.eventName(), scope.hitContext());
    }

    /** 条目的 Condition 是否为「持有者的某类召唤体」（随从 / 图腾 / 同伴 / 盟友）；是则返回其类别标签，否则 null。 */
    @Nullable
    private static String summonRelationTag(StatEntry e) {
        StatCondition c = e.condition();
        if (!(c instanceof RelationCondition rc) || rc.relation() == null) return null;
        return switch (rc.relation()) {
            case MINION, TOTEM, COMPANION, ALLY -> rc.relation().getSerializedName();
            default -> null;
        };
    }

    // ──────────────────────── Condition 缝：布尔门控 ↔ 实体解析（Phase 2a 接线） ────────────────────────

    /**
     * <b>事件上下文 → 条件作用域</b>（新 {@link ConditionScope} 的唯一组装点）。
     * <p>
     * 事件回调的 Context 本身携带两方：{@code owner} = A（"你"）、{@code triggerTarget} = B（Target 锁定的承受者），
     * 而<b>本次事件的对象</b>（被杀 / 被命中实体）由 {@link StatEvalContext#eventEntity()} 单独携带。
     * 三者一并交给 {@link ConditionScope#of}，其 {@code base} 按事件语义推导
     * （{@code eventEntity → triggerTarget → owner}），即「Target 只负责最基本的锁定、具体落到哪些实体由 Condition 决定」。
     */
    public static ConditionScope conditionScope(StatEvalContext evalCtx, @Nullable StatContext scope) {
        return ConditionScope.of(evalCtx.owner(), evalCtx.triggerTarget(), evalCtx.eventEntity(),
                scope != null ? scope.eventName() : null,
                scope != null ? scope.hitContext() : null,
                evalCtx.center(), scope);
    }

    /**
     * <b>缝一（布尔门控）</b>：既有「条件成立才生效」的读口统一经本缝判定。
     * <p>
     * 条目内联的谓词列表经 {@link ConditionBridge#of} 提升为一个 {@code filter} 条件节点
     * （<b>能力复用而非兼容 shim</b>：486 条 datagen 条件一个都不重写，原样作为过滤器），
     * 在「条件判定实体」{@code condEntity} 上解析，<b>解析结果非空 = 条件成立</b>
     * ——模型本身不产布尔，布尔只是「集合非空」的一种读法。
     *
     * <p>作用域的两个轴刻意与既有 {@link GateExpr#matches} / {@link StatEngine#conditionsHold} 逐字对齐，使迁移不产生语义漂移：
     * <ul>
     *   <li><b>参照者</b>（{@code ValueScope#current()}，裸读读它）= {@code condEntity}；</li>
     *   <li><b>入参集</b>（同时经 {@code withSourceEntity} 成为 {@code source.} / {@code SourceScopeNode}
     *       读到的「交互对方」）= {@code condEntity} 实体单例上当前事件作用域的对方
     *       （命中结算双向注入，见 {@code HitScopeHelper#enterHitScopes}），
     *       与既有跨实体条件（{@code ignited_by_source} 等）完全同源。</li>
     * </ul>
     *
     * <p><b>标签门控（{@link GateExpr}）不在本缝内</b>：标签是事件作用域的属性，不是 Condition 的一部分，
     * 仍由调用点用 {@code gate.matches(scope.tags())} 判定。
     *
     * @param condEntity 条件判定实体（由 {@code conditionTarget} 钥匙解析出的「被判定实体」）
     */
    public static boolean conditionsHold(StatEntry e, StatEvalContext evalCtx,
                                         @Nullable StatContext scope, LivingEntity condEntity) {
        List<ValueExpr> cs = e.conditions();
        if (cs.isEmpty()) return true;
        return ConditionBridge.any(ConditionBridge.of(cs), gateScope(evalCtx, scope, condEntity));
    }

    /**
     * 门控作用域：在完整事件上下文（携带 {@code eventEntity}）之上，把参照者换成条件判定实体。
     * <p>
     * 入参集取「条件判定实体的交互对方」：既有跨实体谓词（{@code ignited_by_source} /
     * {@code SourceScopeNode}）读的 {@code source} 正是对方位，与既有求值对齐后语义逐字不变。
     */
    private static ConditionScope gateScope(StatEvalContext evalCtx, @Nullable StatContext scope,
                                            LivingEntity condEntity) {
        StatContext statBase = scope != null ? scope : StatContext.EMPTY;
        LivingEntity other = ExpressionContext.get(condEntity).currentStatContext().sourceEntity();
        return ConditionScope.of(condEntity, evalCtx.triggerTarget(), evalCtx.eventEntity(),
                        statBase.eventName(), statBase.hitContext(), evalCtx.center(), statBase)
                .withBase(List.of(other != null && other != condEntity ? other : condEntity));
    }

    /**
     * <b>缝二（实体解析）</b>：按条目条件从事件上下文解析出「某种判断下的实体集」——
     * 半径内、友方、被你点燃的那个、或其它判断下的那些。返回空集 = 无实体落在判断内。
     * <p>
     * 这是 Condition 的<b>本义</b>（获取实体，而非求布尔）；效果应作用在这个集合本身。
     * 无条件条目时返回事件语义锁定的基本集（{@code eventEntity → triggerTarget → owner}）。
     */
    public static List<LivingEntity> resolveCondition(StatEntry e, StatEvalContext evalCtx,
                                                      @Nullable StatContext scope) {
        ConditionScope cs = conditionScope(evalCtx, scope);
        StatCondition acquisition = e.condition();
        List<LivingEntity> base = acquisition == null ? cs.base() : acquisition.resolve(cs);
        List<ValueExpr> predicates = e.conditions();
        return predicates.isEmpty() ? base : ConditionBridge.of(predicates).resolve(cs.withBase(base));
    }

    /**
     * 条目门控（{@link #aggregate} 与 {@link #accumulateGroupedStat} 共用的<b>唯一</b>实现）：
     * 判定条目 {@code e} 是否应计入 {@code reader} 的本次聚合。
     *
     * <p>三道门（顺序即短路顺序）：
     * <ol>
     *   <li><b>作用目标</b>：{@code evalCtx.resolveTargets(e.target())} 含 {@code reader}
     *       （按 target 钥匙从本次作用域缓存取候选，含 reader 才贡献）。</li>
     *   <li><b>条件实体存在性</b>：按 {@code conditionTarget} 钥匙解析「被判定实体」（缺省持有者自身，
     *       见 {@link StatEvalContext#resolve}）；解析不出实体 → 不计入。</li>
     *   <li><b>作用域条件</b>：{@link GateExpr#matches} / {@link StatEngine#conditionsHold} 为假 → 不计入。
     *       条件求值上下文经 {@code e.bindParams} 注入（{@code $name} 在条件里可用）。</li>
     * </ol>
     *
     * <p>门控标签取 {@link StatEntry#effectiveTags()}（<b>不是</b> {@code tags()}）：
     * <ul>
     *   <li>声明 convertTo → 门控被【重映射】为 convertTo（转换 / MOVE）：仅目标作用域命中，
     *       来源作用域不再计入，故「X 的增加值转换为 Y」由数据表达；</li>
     *   <li>未声明 → 门控 = tags ∪ spillTo（外溢 / COPY）：两侧各命中一次。</li>
     * </ul>
     * 两种语义都只改写这一个布尔门控，{@link #aggregate} 的累加与 fold 的折叠运算完全不变。
     *
     * <p><b>被动快径</b>：门控恒真（{@link GateExpr#isAlwaysTrue()}）且无条件列表为空时一次放行。
     * 它同时短路第 3 门（作用域条件求值）<b>与</b>第 2 门的 {@code condEntity == null} 跳过，
     * <b>不是纯优化</b>：一条无标签、无 conditions 而 conditionTarget=trigger_target 的被动 FOLD
     * 条目，在触发目标为 null 的作用域（纯被动读取）下走快径 → <b>计入</b>；删掉快径落到第 2 门则
     * condEntity 解析为 null → <b>静默丢弃</b>。故本行<b>不得</b>被当作冗余化简掉——那会无声改变
     * 被动条目的计入集合。
     *
     * <p><b>为何无任何行为开关</b>：两个调用点的这三门<b>逐字相同</b>（同序、同谓词、同 helper 调用），
     * 且各自所需的上下文（{@code evalCtx} / {@code scope} / {@code e} / {@code reader}）在两处语义一致，
     * 故共享化<b>不需要</b>参数化任何差异。两处真实差异（{@link #aggregate} 无 {@code instanceof} 守卫、
     * {@link #accumulateGroupedStat} 不解析 {@code valueTarget} 取数实体）均<b>在</b>本方法之外，
     * 刻意留在各自调用点。原两份手抄的真实代价不是行数：只改其一时
     * <b>UI 展示值与运行时实际生效值静默分叉</b>且无告警；本方法使任何门控修正必然落在唯一一处。
     *
     * <p><b>无分配</b>：返回原始 {@code boolean}，无 lambda / 装箱 / 函数式接口；方法体内的
     * {@code withSummonTag} / {@code evalBase} / {@code bindParams} 分配在两个原调用点中本就存在
     * 且触发条件逐条相同，提取后完全一致（{@code aggregate} 每次属性读取的每条目只多一次
     * 静态调用帧，无堆分配）。
     */
    private static boolean passesEntryGates(StatEvalContext evalCtx, StatContext scope,
                                            StatEntry e, LivingEntity reader) {
        // 门控 1：显式 Condition 的解析集就是效果落点；否则按 Target 基础锁定。
        if (e.condition() != null) {
            if (!resolveCondition(e, evalCtx, scope).contains(reader)) return false;
        } else if (!evalCtx.resolveTargets(e.target()).contains(reader)) {
            return false;
        }
        GateExpr gate = e.effectiveTags();
        if (!gate.matches(scope.tags())) return false;
        if (e.condition() != null) return true;
        // 门控 2/3：作用域条件（恒真且无条件 → 被动快径，直接放行）
        if (e.conditions().isEmpty()) return true;
        LivingEntity condEntity = evalCtx.resolve(e.conditionTarget());
        if (condEntity == null) return false;
        return conditionsHold(e, evalCtx, withSummonTag(scope, e), condEntity);
    }

    /** 聚合累加器：收集某挂载源下「指定维度（自身/敌方）」的全部条目贡献。 */
    private static final class Acc {
        double sumAdd = 0, sumMultBase = 0, sumMultTotal = 0;
        double replace = Double.NaN; // REPLACE 模式覆盖值（取最大），非 REPLACE 时为 NaN
        double penaltyMult = 0;   // 惩罚子池：PENALTY 角色条目的 ADD_MULTIPLIED_BASE 贡献（均为负），与正向 multBase 分池
        double penaltyReduce = 0; // 惩罚削减因子：REDUCTION 角色条目按作用域累加（仅缩放惩罚子池）
        AttributeLimiters.Builder limiterBuilder = null;
    }

    /**
     * 把 {@code storeOwner} 存储、属性为 {@code attribute} 的条目中、其 {@link StatEvalContext#resolveTargets}
     * 包含 {@code reader}（被读取属性的实体）的那部分，按 {@code evalCtx} 钥匙解析出的上下文（取数 / 条件）聚合进 {@code acc}。
     * <p>
     * 统一由 {@code evalCtx.resolveTargets(e.target())} 决定「作用于谁」，取代原 {@code affectsTarget} 布尔维度：
     * <ul>
     *   <li>self 维度条目（缓存 owner=自身）：只在被自身读取时贡献；</li>
     *   <li>target 维度条目（缓存 owner=攻击者、triggerTarget=受害者）：只在被受害者读取时贡献。</li>
     * </ul>
     * 条件判定 / 数值求值各自再按其 {@code conditionTarget} / {@code valueTarget} 钥匙从同一缓存取实体。
     *
     * @param storeOwner 条目挂载者（self 维度为自身，target 维度为攻击者）
     * @param reader     正在读取本属性的实体（即 {@code compute} 的 {@code living}）
     * @param evalCtx    本次判定作用域内的实体缓存（钥匙即各阶段的 {@link TargetSelector}）
     */
    private static void aggregate(Acc acc, LivingEntity storeOwner, LivingEntity reader,
                                  Holder<Attribute> attribute, StatContext scope, StatEvalContext evalCtx) {
        StatStore store = storeOwner.getData(AttachmentRegistry.STAT_STORE);
        List<StatStore.IndexedEntry> entries = store.keyedIndex().getOrDefault(attribute, List.of());
        if (entries.isEmpty()) return;
        for (StatStore.IndexedEntry ie : entries) {
            StatEntry e = ie.entry();
            ModifierStatEffect effect = (ModifierStatEffect) e.effect();
            // 门控 1/2/3（作用目标 / 条件实体 / 作用域条件）：与 accumulateGroupedStat 共用唯一实现
            // passesEntryGates，语义与门控标签口径（effectiveTags）全部集中于该方法一处。
            // 本路径刻意【不】加 instanceof ModifierStatEffect 守卫（与 accumulateGroupedStat 的差异）：
            // store.keyedIndex() 的 FOLD 桶按 indexKind 归类，ModifierStatEffect 是唯一落入 FOLD
            // 的效果类型，故上面的直转在本路径恒成立。
            if (!passesEntryGates(evalCtx, scope, e, reader)) continue;
            // 数值求值：按 valueTarget 钥匙取「被取值实体」（缺省持有者自身）
            LivingEntity valEntity = evalCtx.resolve(e.valueTarget());
            if (valEntity == null) continue;
            // 唯一数值：一条词缀只有一个数。需要「按来源属性折算」时写在 math 表达式内
            // （如 attribute:coe:evasion * 0.08），防递归由本引擎 COMPUTING 守卫保证。
            double amt = e.evaluateAmount(ExpressionContext.get(valEntity));
            accumulate(acc, effect, amt, attribute, ie.key());
        }
    }

    /** 把一条目数值按正规运算累加进 Acc（仅 {@link #aggregate} 调用；{@link #accumulateGroupedStat} 走 Map.merge 求和、不经此方法）。 */
    private static void accumulate(Acc acc, ModifierStatEffect effect, double amt, Holder<Attribute> attribute, String key) {
        // 削减惩罚（REDUCTION 角色）：本条数值（正）累加为当前作用域的惩罚削减因子，
        // 不参与普通加值池；fold 阶段仅对独立惩罚子池（penaltyMult）做 (1 - 因子) 二次缩放。
        if (effect.penaltyRole() == ModifierStatEffect.PenaltyRole.REDUCTION) {
            acc.penaltyReduce += amt;
            return;
        }
        if (effect.mode() == ModifierStatEffect.Mode.REPLACE) {
            // 覆盖模式：本条数值直接替代属性原值，取最大值；忽略普通叠加与运算
            if (Double.isNaN(acc.replace) || amt > acc.replace) acc.replace = amt;
            return;
        }
        if (effect.operation().left().isPresent()) {
            AttributeModifier.Operation op = effect.operation().left().get();
            switch (op) {
                case ADD_VALUE -> acc.sumAdd += amt;
                case ADD_MULTIPLIED_BASE -> {
                    // 方案 A：PENALTY 标签的 ADD_MULTIPLIED_BASE 落入独立惩罚子池，
                    // 与正向 multBase 分池，仅受 REDUCTION 因子缩放，绝不污染正向侧。
                    if (effect.penaltyRole() == ModifierStatEffect.PenaltyRole.PENALTY) {
                        acc.penaltyMult += amt;
                    } else {
                        acc.sumMultBase += amt;
                    }
                }
                case ADD_MULTIPLIED_TOTAL -> acc.sumMultTotal += amt;
            }
        } else {
            AttributeLimiters.Operation lop = effect.operation().right().get();
            if (acc.limiterBuilder == null) acc.limiterBuilder = AttributeLimiters.builder();
            // 稳定 id（B-1.8）：条目级唯一且跨求值稳定（来源键派生），替代随机 UUID——
            // 保证 AttributeLimiters.addPersistentModifier 的同 id 去重语义真实生效。
            Identifier id = Identifier.fromNamespaceAndPath(CraftOfExile.MODID,
                    "stat." + attribute.getKey().toString().replace(":", ".") + "." + lop + "." + key);
            acc.limiterBuilder.addPersistentModifier(attribute, new AttributeLimiters.Modifier(id, amt, lop));
        }
    }

    /**
     * 聚合（UI 一览用）：按「除数值(amount)外的全部字段」分组（即 Stat 身份：属性 / 运算 / 模式 /
     * 惩罚角色 / 标签 + 选择器 + 条件），同身份条目的数值（常量或表达式经 evaluateAmount 解析）
     * 直接相加，尊重 tag / condition 门控（不满足 reader 的条目不计入）。
     * <p>
     * 语义要点（用户定调）：
     * <ul>
     *   <li>非 fold：不做 add / multBase / multTotal 复合折叠，也不减属性基准；结果就是「同身份数值之和」。</li>
     *   <li>不忽略门控：是否计入某条 / 某身份按 tag / condition 是否对 reader 成立判定。</li>
     *   <li>身份 = 除 amount 外的全部字段（effect / targets / conditions）；数值(roll 值)经 params / vars
     *       绑定，不计入身份，故「相同 Stat 不同数值」自然聚合成一行。</li>
     * </ul>
     * 返回 Map<StatIdentity, 聚合值>，调用方按身份渲染（属性名 + 聚合值）。
     */
    public static Map<StatIdentity, Double> accumulateGroupedStat(StatStore store, LivingEntity reader) {
        Map<StatIdentity, Double> out = new LinkedHashMap<>();
        ExpressionContext ec = ExpressionContext.get(reader);
        StatEvalContext evalCtx = new StatEvalContext(reader, null);
        for (Map.Entry<Holder<Attribute>, List<StatStore.IndexedEntry>> en : store.keyedIndex().entrySet()) {
            Holder<Attribute> attribute = en.getKey();
            StatContext scope = withAttributeTags(ec.currentStatContext(), attribute);
            for (StatStore.IndexedEntry ie : en.getValue()) {
                StatEntry e = ie.entry();
                if (!(e.effect() instanceof ModifierStatEffect effect)) continue;
                // 门控 1/2/3：与 aggregate 共用唯一实现（passesEntryGates）——这正是 UI 展示值
                // 与运行时实际生效值不致分叉的结构保证：两处门控此后只可能同时改。
                // 本路径刻意【不】解析 valueTarget 取数实体（与 aggregate 的差异）：UI 一览按
                // reader 自身求值聚合，故下方直接用 reader 的 ec。
                if (!passesEntryGates(evalCtx, scope, e, reader)) continue;
                double v = e.evaluateAmount(ec);
                out.merge(new StatIdentity(effect, e.targets(), e.conditions(), e.condition(),
                        e.spillTo(), e.convertTo()), v, Double::sum);
            }
        }
        return out;
    }

    /**
     * 聚合身份键：除数值(amount)外的全部字段（属性 / 运算 / 模式 / 惩罚角色 / 标签 + 选择器 + 条件
     * + 外溢作用域 + 转换作用域）。
     * params / id 是实例化层（roll 值）或来源地址，不计入身份：否则「相同 Stat 不同数值」会因 roll
     * 值不同而拆成多行、无法聚合。
     * <p>
     * 注：ModifierStatEffect 自带值相等（属性 / 运算 / 模式 / 惩罚角色 / 标签）；选择器 / 条件在实际
     * 数据中恒为 StatTargets.EMPTY 与空列表（同一常量实例），引用相等即值相等，满足身份判定。
     * <p>
     * 外溢 / 转换作用域各自成列：两者都改变「本条目计入哪些作用域」，而该维度<b>不体现在
     * effect.tags() 里</b>——若并入身份键，同一效果下「计入 Y」与「转换为 Y」会被并成一行，
     * 把只在 Y 生效的条目与在 X、Y 两侧都生效的条目显示成同一个数字。
     */
    public record StatIdentity(ModifierStatEffect effect, StatTargets targets, List<ValueExpr> conditions,
                               StatCondition condition, GateExpr spillTo, GateExpr convertTo) {
    }

    /**
     * 把 stat 贡献折叠进属性当前值（纯函数，供 mixin 调用）。
     * <p>
     * <b>语义</b>：{@code original} 是属性<b>当前真实值</b>，返回值是「基数 + 增量」，供
     * {@code LivingAttributeValue} mixin 在每次属性读取时使用。结果参与共享读缓存
     * （{@code ExpressionContext#statCache}），因此缓存条目隐含「以真实当前值为 original 算出」。
     * <p>
     * <b>不要</b>用本方法做「只要 stat 增量」的读取——那必须用 {@link #foldDelta}：两者在同一属性、
     * 同一作用域版本 + 数据版本下数值不同，共用缓存条目会互相污染（本方法缓存的是含基数的结果，
     * 会被增量语义读到；反之则会让 mixin 把增量当完整属性值返回）。
     */
    public static double fold(LivingEntity living, Holder<Attribute> attribute, double original) {
        ExpressionContext ec = ExpressionContext.get(living);
        // 读时缓存：同一作用域栈 + 同一数据版本下，属性值只折叠一次，后续读取 O(1) 返回。
        // 双键失效（B-0.1）：作用域栈变化（statScopeVersion）与数据内容变化（StatStore.dataVersion）
        // 是正交失效维度——换装 / 药水 / 状态 / 天赋变更 store 后，同一作用域内也立即重算。
        // 注：跨实体贡献（compute 聚合攻击者 src 条目）为文档化延迟失效——缓存 key 仅含
        // "读取者 + 属性"，攻击者 store 变更至多延迟一个作用域栈周期（push/pop 自增）收敛。
        int dataVersion = 0;
        if (living.hasData(AttachmentRegistry.STAT_STORE)) {
            dataVersion = living.getData(AttachmentRegistry.STAT_STORE).dataVersion();
        }
        Double cached = ec.getCachedStat(attribute, dataVersion);
        if (cached != null) return cached;

        double out = computeFolded(ec, living, attribute, original);
        ec.putCachedStat(attribute, out, dataVersion);
        return out;
    }

    /**
     * 折叠「只含 stat 增量」的结果（等价于以 {@code original = 0.0} 折叠），供把属性当
     * <b>缩放系数 / 概率 / 额外层数</b>用的调用方——这类语义要的是 stat 派生量本身，不要属性基数。
     * <p>
     * 与 {@link #fold} 分用<b>独立</b>的缓存条目（{@code ExpressionContext#statDeltaCache}）：
     * 本方法与 mixin 读路径可能针对<b>同一属性</b>、在<b>同一作用域版本 + 数据版本</b>内先后执行，
     * 而两者算出的值不同（后者含基数），故绝不能共享条目。
     * <p>
     * 注意：基数非 0 的属性（如 base=1.0 的 IMPALE_MAGNITUDE / AILMENT_MAGNITUDE）不要用本方法——
     * 丢掉的 1.0 会让倍率退化为 0，见 {@code GrantOnEventStatEffect} 的同族说明。
     *
     * @param living   属性持有者
     * @param attribute 目标属性
     * @return stat 增量（含 REPLACE 覆盖值与钳制）
     */
    public static double foldDelta(LivingEntity living, Holder<Attribute> attribute) {
        ExpressionContext ec = ExpressionContext.get(living);
        int dataVersion = 0;
        if (living.hasData(AttachmentRegistry.STAT_STORE)) {
            dataVersion = living.getData(AttachmentRegistry.STAT_STORE).dataVersion();
        }
        Double cached = ec.getCachedStatDelta(attribute, dataVersion);
        if (cached != null) return cached;
        double out = computeFolded(ec, living, attribute, 0.0);
        ec.putCachedStatDelta(attribute, out, dataVersion);
        return out;
    }

    /**
     * 折叠算术（无缓存、无副作用）：按当前作用域聚合 stat 条目，产出「{@code original + 增量}」并钳制。
     * {@link #fold}（读路径，缓存基数 + 增量）与 {@link #foldDelta}（增量路径，缓存纯增量）共用本实现，
     * 保证两种语义的唯一差异就是 {@code original}，其余（REPLACE / 惩罚子池 / multTotal / 钳制）完全一致。
     */
    private static double computeFolded(ExpressionContext ec, LivingEntity living,
                                         Holder<Attribute> attribute, double original) {
        StatContext scope = ec.currentStatContext();
        StatResult r = compute(living, attribute, scope);
        double out;
        if (r.isEmpty()) {
            out = original;
        } else if (!Double.isNaN(r.replace())) {
            // 覆盖模式：属性最终值被 REPLACE 条目替代（取最大者），忽略原值与 GAIN 叠加
            out = r.limiters().getCache().clampValue(attribute, r.replace());
        } else {
            // 方案 A：独立惩罚子池 + 削减因子。
            // penaltyMult = Σ 标签 PENALTY 的 ADD_MULTIPLIED_BASE 贡献（均为负，落入独立惩罚子池）；
            // penaltyReduce = Σ 标签 REDUCTION 的削减因子；
            // 最终倍率 = positiveMult + penaltyMult × (1 − penaltyReduce)，仅惩罚被削减，正向池不受影响。
            // penaltyReduce==0 时惩罚照常整池相减，正向池不受影响。
            double red = Math.max(0.0, Math.min(1.0, r.penaltyReduce()));
            double baseValue = living.getAttributeBaseValue(attribute);
            out = original + r.add() + baseValue * (r.multBase() + r.penaltyMult() * (1.0 - red));
            out *= (1.0 + r.multTotal());
            out = r.limiters().getCache().clampValue(attribute, out);
        }
        return out;
    }

    /**
     * 触发型 stat 效果执行入口（携带伤害上下文）：事件（击中 / 暴击 / 击杀 …）发生时，从效果持有者的
     * STAT_STORE 取出触发亚型效果，用既有 {@link GateExpr#matches} / {@link StatEngine#conditionsHold}
     * （事件标签子集包含 + {@code ValueExpr} 条件实时求值）过筛后执行。
     * <p>
     * 纯读取、无全局副作用——效果本体的施加走 {@code EffectAppliers} 原语。
     * 接线点：{@code DamageEvent}（hit / crit / kill 标签）。取代原分立的 StatTriggerEngine。
     *
     * @param owner  效果持有者（事件主语：击中 / 击杀者）
     * @param target 事件目标（被击中 / 被击杀者，可为 null）
     * @param scope  事件作用域（须含事件标签，如 {@code caused_hit} / {@code taken_hit} / {@code kill} / {@code crit}）
     * @param tc     本次伤害结算上下文（可为 null：非伤害事件如击杀 / 攻击起手 / 抬盾无需数值）
     */
    public static void fire(LivingEntity owner, LivingEntity target, StatContext scope, @Nullable TriggerContext tc) {
        fire(owner, target, null, scope, tc, null);
    }

    /** 事件携带运行时数值载荷版本：{@code eventValue} 经 {@link StatEvalContext#eventValue()} 透传给效果（如 RESOURCE_SPEND 真实消耗量）。 */
    public static void fire(LivingEntity owner, LivingEntity target, StatContext scope, @Nullable TriggerContext tc, @Nullable Double eventValue) {
        fire(owner, target, null, scope, tc, eventValue);
    }

    /**
     * 击杀 / 命中链路专用：<b>显式携带事件对象</b>（被杀 / 被命中实体）。
     * <p>
     * 事件回调的 Context 本身携带两方，KILL 传入（你，被杀实体）、HIT 传入（你，被命中实体）——
     * 该「本次事件的对象」即 {@link StatEvalContext#eventEntity()}，是
     * {@link ConditionScope#eventEntity()} 的唯一来源，使 {@code anchor:killed} / {@code anchor:hit}
     * 无需在消费端硬编码即可解析。
     */
    public static void fire(LivingEntity owner, LivingEntity target, @Nullable LivingEntity eventEntity,
                            StatContext scope, @Nullable TriggerContext tc) {
        fire(owner, target, eventEntity, scope, tc, null);
    }

    /** {@link #fire(LivingEntity, LivingEntity, LivingEntity, StatContext, TriggerContext)} 的事件数值载荷版本。 */
    public static void fire(LivingEntity owner, LivingEntity target, @Nullable LivingEntity eventEntity,
                            StatContext scope, @Nullable TriggerContext tc, @Nullable Double eventValue) {
        if (owner == null) return;
        if (!owner.hasData(AttachmentRegistry.STAT_STORE)) return;
        StatStore store = owner.getData(AttachmentRegistry.STAT_STORE);
        // 反向索引候选（B-2.7）：按事件标签取候选触发条目，不再全量遍历
        List<StatEntry> candidates = store.triggersFor(scope.tags());
        // 随从/图腾等召唤体受击时补充主人 store 中
        // Condition 取「召唤体集合」的触发条目（「Minions Recoup X% of Damage taken as Life」53910、
        // 「Recoup 5% of Damage taken by your Totems as Life」5580——条目挂玩家、Condition=relation(minion/totem)，
        // 召唤体承伤即触发）。
        // 主人链经 OwnerHelper 解析；仅 owner 自己是召唤体且主人存在时查主人 store，避免全量连锁。
        LivingEntity master = OwnerHelper.getOwner(owner) instanceof LivingEntity le ? le : null;
        if (master != null && master != owner && master.hasData(AttachmentRegistry.STAT_STORE)) {
            StatStore masterStore = master.getData(AttachmentRegistry.STAT_STORE);
            List<StatEntry> merged = null;
            for (StatEntry e : masterStore.triggersFor(scope.tags())) {
                // 只取「作用于召唤体」（target 为召唤类实体源）的条目；其余主人条目由主人自身事件触发
                if (!isSummonScope(e)) continue;
                if (merged == null) {
                    merged = new ArrayList<>(candidates);
                }
                merged.add(e);
            }
            if (merged != null) candidates = merged;
        }
        if (candidates.isEmpty()) return;

        // 深度闸：超过上限直接放弃本次触发（双条目互激环的兜底），告警使被静默丢弃的效果可观测
        int[] depth = FIRE_DEPTH.get();
        if (depth[0] >= MAX_FIRE_DEPTH) {
            LOGGER.warn("StatEngine fire depth exhausted (max={}) at event tags {}; dropping trigger",
                    MAX_FIRE_DEPTH, scope.tags());
            return;
        }

        Set<StatEntry> firing = FIRING.get();
        // 触发侧 evalCtx。全类仅两处「对方实体（TRIGGER_TARGET 槽位）」来源：本构造与
        // 下方 evalCtx(owner, scope)（fold 侧）。二者刻意不合并——来源本就不同：本路径取
        // 事件 target 形参，因 fire 不把 scope 压入 StatScopes 栈、其 scope 的 sourceEntity
        // 在多数发起点（GameEvent.emit / fireCaused / fireTaken 构造的 scope）为 null，
        // 对方实体只由本形参携带；fold 侧无事件形参，只能取 scope.sourceEntity()。
        //
        // 不变式「同一条目不会被两条路径各判一次」（改此构造前请先读 evalCtx 的同名段落）：
        // 本方法只遍历 triggersFor()（FIRE 桶），fold/foldDelta 只遍历 keyedIndex()（FOLD 桶），
        // 而 StatStore 构造器的 indexKind() switch 把每个条目投入恰好一个桶
        //（ModifierStatEffect 按 duration != null 二分，见其类注释「两种形态」）。两桶互斥，
        // 故 triggerTarget 取值不同只可能落在不同条目上，不会让同一条目的门控结论因路径分叉。
        // 两侧的 conditionTarget=trigger_target 各自语义完整：本行 evalCtx.resolve() 按事件
        // target 解析，fold 侧按 scope.sourceEntity() 解析。
        StatEvalContext evalCtx = new StatEvalContext(owner, target, eventEntity, eventValue);
        depth[0]++;
        try {
            for (StatEntry entry : candidates) {
                // 自激守卫：本条目已在执行栈上 → 跳过，避免副作用回流后自我重入
                if (firing.contains(entry)) continue;
                if (!entry.tags().matches(scope.tags())) continue;
                if (entry.condition() != null) {
                    if (resolveCondition(entry, evalCtx, scope).isEmpty()) continue;
                } else {
                    LivingEntity condEntity = evalCtx.resolve(entry.conditionTarget());
                    if (condEntity == null) continue;
                    if (!conditionsHold(entry, evalCtx, scope, condEntity)) continue;
                }
                StatEffect effect = entry.effect();
                if (effect == null) continue;
                firing.add(entry);
                try {
                    effect.onTrigger(entry, scope, evalCtx, tc);
                } finally {
                    firing.remove(entry);
                }
            }
        } finally {
            depth[0]--;
        }
    }

    // ───────────────────────── fold 型查询的作用域实体缓存（统一入口） ─────────────────────────

    /**
     * 召唤类作用域判定：条目的 Condition 是否取「持有者的召唤体」集合（随从 / 图腾 / 同伴 / 盟友）。
     * 用于 {@link #fire} 主人链——召唤体受击时补充主人 store 中作用于这些实体的触发条目。
     *
     * <p>Phase 2b：判定依据由 {@code target} 的召唤类 Source 改为条目的 {@link RelationCondition}
     * （归属获取是 Condition 的实体获取职责，Target 只锁 A / B）。
     */
    private static boolean isSummonScope(StatEntry e) {
        return summonRelationTag(e) != null;
    }

    /**
     * fold 型查询（{@link #convert} / {@link #damageConvert} / {@link #damageTakenConvert} /
     * {@link #isImmune} / {@link #rollModifier} / {@link #hasHitFlag} / {@link #delivery}）的
     * 作用域实体缓存：{@code owner} = 被查询实体（SELF 槽位），{@code TRIGGER_TARGET} 槽位取当前
     * 作用域绑定的「交互对方」（{@link StatContext#sourceEntity()}）。
     *
     * <p>与 {@link #compute} 的属性读取路径<b>同源约定</b>（见 L116/L120 及
     * {@link TargetSelector.Source#TRIGGER_TARGET}
     * 「属性读取路径下 TRIGGER_TARGET 即 sourceEntity」）：命中结算时受害者作用域持攻击者、
     * 攻击者作用域持受害者（{@code DamageEvent.enterHitScopes} 双向注入）。
     *
     * <p><b>约束</b>：七条 fold 通道不得一律硬写 {@code StatEvalContext.self(x)}
     * （TRIGGER_TARGET 恒为 {@code null}），导致条目上 {@code conditionTarget=trigger_target} 的
     * <b>跨实体条件在这些通道恒解析为 null 而被静默跳过</b>——条目永不生效且无任何告警。
     * 典型语料：「Your Hits cannot be Evaded by Pinned Enemies」（攻击方持 UNEVADABLE 标志，
     * 但条件须判定<b>受击方</b>是否被钉刺）。作用域未绑定对方（纯被动读取）时仍为 {@code null}，
     * 跨实体条目安全地不生效。
     *
     * <p><b>与 {@link #fire} 的分工：全类仅两处构造，条目集互斥，故无须合并</b>：
     * 本方法服务 FOLD 桶（{@link #compute} 另有三处按维度分别构造，语义逐条对应其注释），
     * {@link #fire} 服务 FIRE 桶；{@link StatStore} 按 {@code indexKind()} 把每个条目投入
     * 恰好一个桶，两桶互斥。于是本路径取 {@code scope.sourceEntity()}、{@link #fire} 取事件
     * {@code target} 这一差异<b>只会落在不同条目上</b>，不会让同一条目的门控结论因路径而异——
     * 且二者的 TRIGGER_TARGET 来源本就不同（环境作用域 vs 事件形参），合并反而要参数化来源。
     */
    private static StatEvalContext evalCtx(LivingEntity owner, StatContext scope) {
        return new StatEvalContext(owner, scope.sourceEntity());
    }

    /**
     * fold 通道统一条件过滤：解析候选条目 {@code e} 的「条件判定实体」（按 {@code conditionTarget} 钥匙），
     * 求值作用域条件（tags + conditions，经 {@code e.bindParams} 注入参数）。
     * <p>封装 {@link #convert} / {@link #damageConvert} / {@link #damageTakenConvert} / {@link #isImmune} /
     * {@link #rollModifier} / {@link #hasHitFlag} / {@link #delivery} 七条 fold 查询中重复的
     * 「{@code evalCtx} + {@code conditionTarget.resolve} + {@code conditionsHold} + {@code bindParams}」样板，
     * 消除跨通道手抄漂移（尤其 {@code damageTakenConvert} 的 {@code GateExpr.ALWAYS} 变体）。
     * 不通过 / 实体为 null 时返回 null，调用方 {@code continue} 跳过；通过时返回条件实体（供后续数值取值）。
     *
     * @param owner       持有者（SELF 槽位，{@link #evalCtx} 的 self 侧）
     * @param scope       当前作用域
     * @param e           候选条目
     * @param ignoreTags  为 true 时 tags 强制按 {@link GateExpr#ALWAYS} 判定（{@code damageTakenConvert}
     *                   维度由 tag 表达、作用域条件不判 tags）
     */
    @Nullable
    private static LivingEntity passingCondEntity(LivingEntity owner, StatContext scope, StatEntry e, boolean ignoreTags) {
        StatEvalContext ctx = evalCtx(owner, scope);
        LivingEntity condEntity = ctx.resolve(e.conditionTarget());
        if (condEntity == null) return null;
        if (!ignoreTags && !e.tags().matches(scope.tags())) return null;
        // 缝一：条件门控（Condition 解析非空即满足）；标签门控由上一行的 GateExpr 判定
        if (!conditionsHold(e, ctx, scope, condEntity)) return null;
        return condEntity;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 惰性标记扫描统一内核：10 处「遍历 STAT_STORE 折叠 INERT 标记」读口共用
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 惰性标记扫描的<b>标签门控口径</b>（{@link #scanMarkerEntries} 的 tagGate 轴）。
     * <p>三者的差别来自 {@link MarkerStatEffect} 的两个正交标签集：{@code conditionTags} 由
     * MARKER 变体消费，{@code identityTags} 由 ROLLED / GATE / MAGNITUDE 变体消费。
     */
    private enum TagGate {
        /** 不判任何 tags —— {@code DamagePipeline} 三处 marker 口径（既有行为，本次不统一）。 */
        NONE,
        /** {@code cond ⊇ m.conditionTags()} —— 「条目条件变体 ⊆ 命中条件集即折叠」（{@code defend}）。 */
        CONDITION_TAGS,
        /** {@code queryTags ⊇ m.identityTags()} —— 「作用域身份匹配」（掷骰 / 门控 / 幅度变体）。 */
        IDENTITY_TAGS
    }

    /**
     * 惰性标记扫描的<b>累加口径</b>（{@link #scanMarkerEntries} 的 fold 轴）。
     * <p>三者对命中条目的取值动作不同，故差异必须是显式参数而非各自手抄。
     */
    private enum MarkerFold {
        /** 累加 {@code scaledMagnitude}（{@code scale_attr} 一并应用）。 */
        SUM,
        /** 任一条目幅度 &gt; 0 即短路返回 {@code 1.0}（布尔门控「presence + amount&gt;0」语义）。 */
        ANY_POSITIVE,
        /** 并入 {@code m.identityTags()}，<b>不求值 amount</b>；写入 sink 后返回 {@code 0}。 */
        TAG_UNION
    }

    /**
     * 惰性标记（{@link MarkerStatEffect}，{@code EffectKind.INERT}）全表扫描的统一内核。
     * <p>取代此前分散在 {@code markerMagnitude} / {@code rollChance} / {@code rollCount} / {@code gateOf} /
     * {@code magnitudeOf} / {@code collectGateTags}（本类）与 {@code chanceDefendMagnitude} /
     * {@code corruptionEnduresChance} / {@code defendMagnitude}（{@code DamagePipeline}）的 9 份手抄循环。
     * {@code StatStore} 对 INERT 型<b>不建任何索引</b>（见 {@code StatStore} 的 {@code case INERT}），
     * 故本循环本就是全表扫描；统一后每条目成本与原循环逐项相同，仅多两个循环不变式枚举比较。
     *
     * <p><b>刻意保留的差异（勿在后续重构中顺手抹平）</b>：
     * <ol>
     *   <li><b>条件求值</b>：{@code evalConditions=true} 时经 {@link #passingCondEntity} 求值
     *       {@code e.conditions()}；{@code false} 时<b>完全不判 conditions</b>。{@code DamagePipeline} 三处
     *       传 {@code false}（沿用其既有口径，与本类六处不一致属已知历史差异，非 bug 修复范围）。</li>
     *   <li><b>变体校验</b>：{@code expectedVariant == null} 表示<b>不</b>校验 {@code m.slot().variant()}
     *       （{@code DamagePipeline} 三处原本就只看 kind 字面量，不校验变体）。</li>
     *   <li><b>标签门控</b>：见 {@link TagGate}。</li>
     * </ol>
     * 上述三点均为<b>纯参数化</b>：任何一处在本方法内的取值动作与其原循环逐字等价，无隐式新增/移除判定。
     *
     * <p><b>热路径开销说明</b>：{@link ExpressionContext#get} 与 {@code currentStatContext()} 由「每命中条目一次」
     * 提升为「每次扫描一次」；{@code get} 本身按实体 memoize（{@code WeakHashMap.computeIfAbsent}）、幂等无副作用，
     * {@code currentStatContext()} 为纯读（栈顶为空回退 {@link StatContext#EMPTY}），故为净减少而非行为变化。
     *
     * @param owner         持有者（条件实体亦按此解析，见 {@link #passingCondEntity}）
     * @param expectedVariant 期望的 {@code Variant}；{@code null} = 不校验变体
     * @param kind          标记身份串（{@code m.kind()} 精确匹配）
     * @param tagGate       标签门控口径
     * @param tagQuery      标签查询集；{@code tagGate == NONE} 时忽略（可传 {@code null}）
     * @param evalConditions 是否经 {@link #passingCondEntity} 求值条目条件
     * @param fold          累加口径
     * @param tagSink       {@link MarkerFold#TAG_UNION} 的输出集合；其余口径忽略（可传 {@code null}）
     * @return {@code SUM} 返回幅度和；{@code ANY_POSITIVE} 返回 {@code 0} 或 {@code 1}；
     *         {@code TAG_UNION} 返回 {@code 0}（结果在 {@code tagSink}）
     */
    private static double scanMarkerEntries(LivingEntity owner,
                                           MarkerStatEffect.@Nullable Variant expectedVariant,
                                           String kind,
                                           TagGate tagGate,
                                           @Nullable Set<String> tagQuery,
                                           boolean evalConditions,
                                           MarkerFold fold,
                                           @Nullable Set<String> tagSink) {
        if (owner == null || !owner.hasData(AttachmentRegistry.STAT_STORE)) return 0.0;
        StatStore store = owner.getData(AttachmentRegistry.STAT_STORE);
        ExpressionContext ec = ExpressionContext.get(owner);
        StatContext scope = evalConditions ? ec.currentStatContext() : null;
        double sum = 0.0;
        for (StatEntry e : store.entries()) {
            if (!(e.effect() instanceof MarkerStatEffect m)) continue;
            if (expectedVariant != null && m.slot().variant() != expectedVariant) continue;
            if (!kind.equals(m.kind())) continue;
            if (tagGate == TagGate.CONDITION_TAGS) {
                if (!tagQuery.containsAll(m.conditionTags())) continue;
            } else if (tagGate == TagGate.IDENTITY_TAGS) {
                if (!tagQuery.containsAll(m.identityTags())) continue;
            }
            if (evalConditions && passingCondEntity(owner, scope, e, false) == null) continue;
            if (fold == MarkerFold.TAG_UNION) {
                tagSink.addAll(m.identityTags());
            } else {
                double mag = scaledMagnitude(owner, e.evaluateAmount(ec), m.scaleAttr().orElse(null));
                if (fold == MarkerFold.ANY_POSITIVE) {
                    if (mag > 0.0) return 1.0;
                } else {
                    sum += mag;
                }
            }
        }
        return sum;
    }

    /**
     * 惰性标记幅度和（<b>无变体校验 / 无标签门控 / 不判 conditions</b> 口径）。
     * <p>{@code DamagePipeline} 的 {@code CHANCE_DEFEND} / {@code CORRUPTION_ENDURES} 两处经此折叠：
     * 二者原循环只看 {@code m.kind()} 字面量，既不校验 {@code m.slot().variant()}、不判任何 tags、
     * 也不求值 {@code e.conditions()}。本方法把这三点固化为签名（而非隐含约定），使日后修正
     * 只能显式发生，不会被一次「顺手统一」悄悄改掉。
     */
    public static double markerMagnitudeUngated(LivingEntity living, String kind) {
        return scanMarkerEntries(living, null, kind, TagGate.NONE, null, false, MarkerFold.SUM, null);
    }

    /**
     * 惰性标记幅度和（<b>按 conditionTags 门控</b>、无变体校验、<b>不判 conditions</b> 口径）。
     * <p>{@code DamagePipeline#defendMagnitude} 经此折叠：「条目条件变体 ⊆ 本次命中条件集」即参与聚合
     * （暴击 + 投射物同时命中则叠加两变体幅度），但同样不求值 {@code e.conditions()}（沿用其既有口径）。
     */
    public static double markerMagnitudeByConditionTags(LivingEntity living, String kind, Set<String> cond) {
        return scanMarkerEntries(living, null, kind, TagGate.CONDITION_TAGS, cond, false, MarkerFold.SUM, null);
    }

    /**
     * 资源转换查询（fold 型、纯读取，无副作用）：资源扣费点调用，询问「这笔
     * {@code from} 资源的 {@code amount} 量，是否应改从另一资源扣除、扣多少」。
     * <p>
     * 与 {@link #fire} 对称但服务于「扣费前重定向」而非「事件触发副作用」：
     * 遍历持有者 STAT_STORE 中 {@code from} 匹配的资源转换条目（身份字段指定目标资源），
     * 按当前作用域（事件标签 + 条件）过筛，取第一个命中条目的 {@code (to, amount * 比例)}。
     * <p>
     * 纯读取，因此可同时被「能否承受」({@code matches}) 与「实际扣除」({@code runCost}) 调用，
     * 两者查同一规则，结果天然一致，不重复扣费。
     *
     * @param living 扣费方（效果持有者，通常为施法者）
     * @param from   原本要扣除的资源类型
     * @param amount 原本要扣除的量
     * @return 命中转换规则时返回目标资源与转换后的量；无规则命中返回空
     */
    public static Optional<ResourceConversion> convert(LivingEntity living, ResourceType from, float amount,
                                                        @Nullable String sourceTag) {
        if (living == null || !living.hasData(AttachmentRegistry.STAT_STORE)) return Optional.empty();
        StatStore store = living.getData(AttachmentRegistry.STAT_STORE);
        StatContext scope = ExpressionContext.get(living).currentStatContext();
        // 正向候选：convertIndex 按 from 键控，规则 from == 查询源
        List<StatEntry> candidates = store.convertIndex().get(from);
        if (candidates != null) {
            for (StatEntry e : candidates) {
                ResourceConvertStatEffect effect =
                        (ResourceConvertStatEffect) e.effect();
                if (effect == null) continue;
                // EXCESS 只在「已发生溢出」后由 excessConversions 单独查询——此处是回复前的
                // 分流阶段，若在此命中会提前把量挪走，溢出转移语义（先喂饱 A）即被破坏。
                if (effect.mode() == ResourceConvertStatEffect.Mode.EXCESS) continue;
                if (!effect.matchesSource(sourceTag)) continue;
                double ratio = e.evaluateAmount(ExpressionContext.get(living));
                if (ratio <= 0) continue;   // 负比例在反向扫描中命中（本键无反向语义）
                LivingEntity condEntity = passingCondEntity(living, scope, e, false);
                if (condEntity == null) continue;
                return Optional.of(new ResourceConversion(effect.to(),
                        effect.mode() == ResourceConvertStatEffect.Mode.GAIN
                                ? ResourceConvertStatEffect.Mode.GAIN : ResourceConvertStatEffect.Mode.REPLACE,
                        (float) (amount * ratio)));
            }
        }
        // 反向候选：负比例规则 = to→from——查询源 == effect.to()，转换目标 = effect.from()，
        // 比例取绝对值。此类条目键为 effect.from()（非查询源），需全表扫描。
        for (List<StatEntry> list : store.convertIndex().values()) {
            for (StatEntry e : list) {
                ResourceConvertStatEffect effect =
                        (ResourceConvertStatEffect) e.effect();
                if (effect == null) continue;
                if (effect.from() == from) continue;    // 正向键，上一循环已处理
                if (effect.to() != from) continue;
                if (effect.mode() == ResourceConvertStatEffect.Mode.EXCESS) continue;
                if (!effect.matchesSource(sourceTag)) continue;
                double ratio = e.evaluateAmount(ExpressionContext.get(living));
                if (ratio >= 0) continue;
                LivingEntity condEntity = passingCondEntity(living, scope, e, false);
                if (condEntity == null) continue;
                return Optional.of(new ResourceConversion(effect.from(),
                        effect.mode() == ResourceConvertStatEffect.Mode.GAIN
                                ? ResourceConvertStatEffect.Mode.GAIN : ResourceConvertStatEffect.Mode.REPLACE,
                        (float) (amount * -ratio)));
            }
        }
        return Optional.empty();
    }

    /**
     * 溢出转移查询（fold 型，无副作用）：资源回复已满、产生溢出后，返回全部 {@code EXCESS} 模式、
     * 来源与条件均命中的转移规则（{@code amount} = 溢出量 × 比例）。
     *
     * <p>与 {@link #convert} 互补：后者在回复<b>前</b>分流，本方法在溢出<b>后</b>搬运，
     * 供「Excess Health Recovery from Regeneration is applied to Energy Shield」类词条使用。
     * 转移以原 sourceTag 记入目标资源，故来源门控（如「不能从再生回复 ES」）仍能正确拦下它。
     */
    public static List<ResourceConversion> excessConversions(LivingEntity living, @Nullable ResourceType from,
                                                             float overflow, @Nullable String sourceTag) {
        if (living == null || from == null || overflow <= 0
                || !living.hasData(AttachmentRegistry.STAT_STORE)) return List.of();
        StatStore store = living.getData(AttachmentRegistry.STAT_STORE);
        StatContext scope = ExpressionContext.get(living).currentStatContext();
        List<StatEntry> candidates = store.convertIndex().get(from);
        if (candidates == null || candidates.isEmpty()) return List.of();
        List<ResourceConversion> out = new ArrayList<>();
        for (StatEntry e : candidates) {
            ResourceConvertStatEffect effect =
                    e.effect() instanceof ResourceConvertStatEffect r ? r : null;
            if (effect == null || effect.mode() != ResourceConvertStatEffect.Mode.EXCESS) continue;
            if (!effect.matchesSource(sourceTag)) continue;
            double ratio = e.evaluateAmount(ExpressionContext.get(living));
            if (ratio <= 0) continue;
            LivingEntity condEntity = passingCondEntity(living, scope, e, false);
            if (condEntity == null) continue;
            out.add(new ResourceConversion(effect.to(), ResourceConvertStatEffect.Mode.EXCESS,
                    (float) (overflow * ratio)));
        }
        return out;
    }

    /** 资源转换查询结果：目标资源、模式（REPLACE 去A取B / GAIN 共存也获得B）、转换后的量。 */
    public record ResourceConversion(ResourceType to, ResourceConvertStatEffect.Mode mode, float amount) {
    }

    /**
     * 伤害类型转换查询（fold 型，无副作用）：在 living 的当前 StatContext 作用域下，返回全部满足
     * 作用域条件的「伤害类型重路由」规则（来源为 tag 组合，运行时由 DamagePipeline 对每一伤害槽位
     * 用 {@code fromTags.contains(slotTag)} 判定该槽位是否命中来源，再把命中份额按 mode 重路由到
     * {@code to} 对应槽位，减免前执行）。
     *
     * <p><b>随机类型解析</b>：{@link DamageConvertStatEffect} 条目的
     * {@link DamageConvertStatEffect#toMode()} 为 RANDOM/CYCLE 时，在此查询层按模式当场解析出确定
     * {@code to}（消费端零改动）：
     * <ul>
     *   <li>{@code RANDOM}：经 {@code living.getRandom()} 从候选 tagList 等概率掷骰——每次结算调用
     *       独立掷骰，即 POE2「random Element」逐击随机语义（一次命中结算内至多调用一次本查询，
     *       同次命中各槽位共享同一次解析结果，保证同次命中元素一致）。</li>
     *   <li>{@code CYCLE}：读持有者 {@code AttachmentRegistry.RANDOM_CONVERT_CYCLE}（{@code Map<String,Integer>}，
     *       键 = 条目来源键）取当前索引，按候选顺序轮转并写回 +1——命中间轮流切换、可预期。</li>
     * </ul>
     * CYCLE 的计数写回打破严格纯函数性，但状态仅限持有者自身的计数器，无跨实体副作用，属文档化例外。
     *
     * @param living 转换规则的归属实体（命中伤害=承受者；异常输出=施法者）
     * @return 命中规则列表（每条含来源组合、目标类型、模式、比例）；无命中返回空列表
     */
    public static List<DamageConversion> damageConvert(LivingEntity living) {
        return damageConvert(living, false);
    }

    /** 维度B 进项转伤用：onlyTaken=true 时仅返回 {@code taken} 标记的转类型规则（外发规则不作用于承受伤害）。 */
    public static List<DamageConversion> damageConvert(LivingEntity living, boolean onlyTaken) {
        List<DamageConversion> out = new ArrayList<>();
        if (living == null || !living.hasData(AttachmentRegistry.STAT_STORE)) return out;
        StatStore store = living.getData(AttachmentRegistry.STAT_STORE);
        List<StatStore.IndexedEntry> candidates = store.damageConvertIndexed();
        if (candidates.isEmpty()) return out;

        StatContext scope = ExpressionContext.get(living).currentStatContext();
        for (StatStore.IndexedEntry ie : candidates) {
            StatEntry e = ie.entry();
            LivingEntity condEntity = passingCondEntity(living, scope, e, false);
            if (condEntity == null) continue;
            double ratio = e.evaluateAmount(ExpressionContext.get(living));
            if (ratio == 0) continue;
            StatEffect fx = e.effect();
            if (!(fx instanceof DamageConvertStatEffect effect)) continue;
            if (onlyTaken && !effect.taken()) continue;
            if (effect.toMode() == DamageConvertStatEffect.ToMode.FIXED) {
                // 负比例 = 反向转换（from↔to 互换，比例取绝对值）：仅单来源可逆
                //（多来源对单目标的反向无唯一解，跳过并视为无效）
                if (ratio < 0) {
                    if (effect.fromTags().size() != 1) continue;
                    out.add(new DamageConversion(Set.of(effect.to()), effect.fromTags().iterator().next(),
                            effect.mode() == DamageConvertStatEffect.Mode.GAIN
                                    ? DamageConvertStatEffect.Mode.GAIN : DamageConvertStatEffect.Mode.REPLACE,
                            (float) -ratio, effect.hitsOnly(), effect.fromContext()));
                    continue;
                }
                if (effect.fromTags().contains(effect.to())) continue;
                out.add(new DamageConversion(Set.copyOf(effect.fromTags()), effect.to(),
                        effect.mode() == DamageConvertStatEffect.Mode.GAIN
                                ? DamageConvertStatEffect.Mode.GAIN : DamageConvertStatEffect.Mode.REPLACE,
                        (float) ratio, effect.hitsOnly(), effect.fromContext()));
            } else {
                // 随机类型通道（原 RandomConvertStatEffect）：目标类型按 toMode 解析后以 GAIN 共存语义输出
                //（负比例无随机语义，跳过）
                if (ratio < 0) continue;
                String to = effect.resolvedTo(living, ie.key());
                if (to == null || effect.fromTags().contains(to)) continue;
                out.add(new DamageConversion(Set.copyOf(effect.fromTags()), to,
                        DamageConvertStatEffect.Mode.GAIN, (float) ratio, effect.hitsOnly(), effect.fromContext()));
            }
        }
        return out;
    }

    /**
     * 随机类型通道的目标类型解析：RANDOM 掷骰 / CYCLE 轮流。攻击侧（{@link DamageConvertStatEffect}）
     * 与承伤侧（{@link DamageTakenConvertStatEffect}）共用——由 {@link DamageConvertStatEffect.ToMode}
     * 选择模式、{@code candidates} 提供候选标签。CYCLE 计数器统一存于
     * {@code AttachmentRegistry.RANDOM_CONVERT_CYCLE}（键 = 条目来源键），攻击/承伤随机条目各自独立轮换。
     *
     * @return 候选列表中的一个 {@link GateTag} 类型；候选空（数据异常）时返回 null（条目安全跳过）
     */
    public static String resolveRandomTo(LivingEntity living, String entryKey,
                                          DamageConvertStatEffect.ToMode toMode, List<String> candidates) {
        if (candidates.isEmpty()) return null;
        if (toMode == DamageConvertStatEffect.ToMode.RANDOM) {
            return candidates.get(living.getRandom().nextInt(candidates.size()));
        }
        // CYCLE：按条目来源键独立轮转（键缺失时按 0 起算，写回后后续命中轮换）
        Map<String, Integer> cycle = living.getData(AttachmentRegistry.RANDOM_CONVERT_CYCLE);
        int idx = cycle.getOrDefault(entryKey, 0);
        Map<String, Integer> next = new HashMap<>(cycle);
        next.put(entryKey, idx + 1);
        living.setData(AttachmentRegistry.RANDOM_CONVERT_CYCLE, Map.copyOf(next));
        return candidates.get(idx % candidates.size());
    }

    /**
     * 属性缩放原语（拓展方向 D，参照 {@link GrantOnEventStatEffect} 的 {@code durationAttr} 缩放范式）：
     * 把 {@code base} 按 {@code scaleAttr} 折叠值缩放，返回 {@code base * (1 + max(0, fold(scaleAttr)))}。
     * <p>{@code scaleAttr} 为 null 时原样返回 {@code base}（未配置缩放的属性零改动）。
     * 供 {@link GrantOnEventStatEffect}（状态强度/层数）与 {@link MarkerStatEffect}（标记幅度）复用，
     * 避免各自手抄 {@link #foldDelta} 调用、保证缩放语义（下界钳制 + 缺省透传）在跨效应间一致。
     *
     * @param living    缩放属性持有者
     * @param base      未缩放基准值（如标记幅度 / 状态强度基座）
     * @param scaleAttr 缩放属性（null = 不缩放）
     */
    public static double scaledMagnitude(LivingEntity living, double base, @Nullable Holder<Attribute> scaleAttr) {
        if (scaleAttr == null) return base;
        double s = StatEngine.foldDelta(living, scaleAttr);
        return base * (1.0 + Math.max(0.0, s));
    }

    /** 伤害类型转换查询结果：来源标签组合、目标类型、模式（REPLACE/GAIN）、转换比例（0~1）、是否仅命中、命中上下文门控。 */
    public record DamageConversion(Set<String> fromTags, String to, DamageConvertStatEffect.Mode mode, float ratio, boolean hitsOnly, GateExpr fromContext) {
    }

    /**
     * 承伤转化查询（fold 型，无副作用）：在伤害减免（抗性 / 护甲）计算前，返回 living 身上
     * 全部「满足作用域条件」的承伤转化规则（维度已由 tag 组合表达，运行时由 stage 逐伤害槽位用
     * {@code slotTags.containsAll(entry.tags())} 判定是否命中该槽位）。
     *
     * <p>与 {@link #convertDamage}（伤害类型重路由）不同：本查询不改变伤害类型，只扩展「减免手段的作用域」。
     * 例：「20% 的护甲也作用于火焰伤害」= {@code {reduction: armor, tags: {fire}, mode: extra, ratio: 0.2}}，
     * 在火抗减免之后额外按护甲公式再减一次；而「20% 的火焰伤害由护甲承受」=
     * {@code {reduction: armor, tags: {fire}, mode: reroute, ratio: 0.2}}，该份额绕过火抗改由护甲减免。
     * 二者是同一减免手段 + 维度、但 mode 不同的两条规则（EXTRA 二次减免 / REROUTE 重路由）。
     *
     * @param living 规则归属实体（承受伤害者，持有「护甲也作用于某伤害」等效果）
     * @return 命中作用域条件的规则列表（每条含减免手段、伤害维度标签、比例）；无命中返回空列表
     */
    public static List<DamageTakenConvert> damageTakenConvert(LivingEntity living) {
        List<DamageTakenConvert> out = new ArrayList<>();
        if (living == null || !living.hasData(AttachmentRegistry.STAT_STORE)) return out;
        StatStore store = living.getData(AttachmentRegistry.STAT_STORE);
        List<StatStore.IndexedEntry> candidates = store.damageTakenConvertIndexed();
        if (candidates.isEmpty()) return out;

        StatContext scope = ExpressionContext.get(living).currentStatContext();
        for (StatStore.IndexedEntry ie : candidates) {
            StatEntry e = ie.entry();
            LivingEntity condEntity = passingCondEntity(living, scope, e, true);
            if (condEntity == null) continue;
            double ratio = e.evaluateAmount(ExpressionContext.get(living));
            if (ratio == 0) continue;
            StatEffect fx = e.effect();
            if (!(fx instanceof DamageTakenConvertStatEffect effect)) continue;
            // 减免手段：FIXED 用 reduction()；RANDOM/CYCLE 按 toMode 解析为具名元素（解析复用攻击侧计数器）。
            // 随机通道恒为 REROUTE 语义；负比例无随机语义，resolvedReduction 返回 null 时跳过。
            String reduction = effect.resolvedReduction(living, ie.key());
            if (reduction == null) continue;
            DamageTakenConvertStatEffect.Mode outMode = effect.toMode() == DamageConvertStatEffect.ToMode.FIXED
                    ? effect.mode() : DamageTakenConvertStatEffect.Mode.REROUTE;
            out.add(new DamageTakenConvert(reduction, outMode, e.tags(), (float) ratio));
        }
        return out;
    }

    /** 承伤转化查询结果：减免手段（GateTag 字符串）、作用方式、伤害维度标签表达式、扩展比例（0~1）。 */
    public record DamageTakenConvert(String reduction,
                                     DamageTakenConvertStatEffect.Mode mode,
                                     GateExpr tags, float ratio) {
    }

    /**
     * 状态免疫查询（fold 型、纯读取，无副作用）：{@code StatusEngine.apply} 施加入口调用，
     * 询问「目标实体是否对某状态免疫」。
     * <p>
     * 遍历目标自身 STAT_STORE 的 {@code immune} 索引：效果身份字段包含该状态类型
     * （{@code ImmuneStatEffect.statuses().contains(type)}）且条目条件通过即判免疫。
     * 免疫条目通常极少，全量遍历开销可忽略；{@code target} 无 STAT_STORE / 无条件命中返回 false。
     *
     * @param target 被判定免疫的目标（通常即承受状态的实体）
     * @param type   将被施加的状态类型
     */
    public static boolean isImmune(LivingEntity target, StatusType type) {
        if (target == null || !target.hasData(AttachmentRegistry.STAT_STORE)) return false;
        StatStore store = target.getData(AttachmentRegistry.STAT_STORE);
        List<StatEntry> candidates = store.immuneIndex();
        if (candidates.isEmpty()) return false;

        StatContext scope = ExpressionContext.get(target).currentStatContext();
        for (StatEntry e : candidates) {
            ImmuneStatEffect effect = (ImmuneStatEffect) e.effect();
            if (effect == null || !effect.statusIds().contains(type.id)) continue;
            LivingEntity condEntity = passingCondEntity(target, scope, e, false);
            if (condEntity == null) continue;
            return true;
        }
        return false;
    }

    /**
     * 惰性标记幅度查询（INERT 型、纯读取、无副作用）：累计持有者 STAT_STORE 中
     * {@code kind} 匹配、且 {@code cond} 包含其 conditionTags 的 MarkerStatEffect 条目幅度之和。
     * <p>
     * 与 {@code DamagePipeline} 的三处 marker 消费端（:688-725）同构，但额外求值<b>条目条件</b>
     * （那三处不判 conditions，属既有隐患，本次不动它们）。条目条件经 {@link #passingCondEntity} 求值，
     * 故「长杖限定」这类 {@code StatCondition} 门控在此生效。
     */
    public static double markerMagnitude(LivingEntity living, String kind, Set<String> cond) {
        return markerMagnitude(living, MarkerStatEffect.slotOf(kind, MarkerStatEffect.Variant.MARKER), kind, cond);
    }

    /**
     * {@link MarkerStatEffect.Slot} 口径的惰性标记幅度查询（推荐入口）。
     *
     * <p><b>为什么按 Slot 过滤而不是按 kind 字符串</b>：kind 是不透明身份串，把它当消费键就要求
     * 消费端硬写 {@code m.kind().equals("<字面量>")}，新增 kind 必改 Java。Slot 是 kind 的语义声明
     * （见 {@code MarkerStatEffect.KIND_SLOTS}），本方法只认「该 Slot 所属的 Variant」，
     * 故同族 kind（{@code defend} / {@code corruption_endures} …）全部命中，无需逐个登记。</p>
     *
     * <p><b>幅度缩放</b>：命中条目的 {@code scale_attr} 经 {@link #scaledMagnitude} 应用，
     * 缩放属性为负时会反向放大为负幅度。</p>
     */
    public static double markerMagnitude(LivingEntity living, MarkerStatEffect.Slot slot, String kind, Set<String> cond) {
        return scanMarkerEntries(living, slot.variant(), kind,
                TagGate.CONDITION_TAGS, cond, true, MarkerFold.SUM, null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 动态幅度读口：递增承火伤（ignite_history_fire_taken）——幅度依赖受害方 IGNITE_HISTORY 累计秒数，
    // 由条目 amount 内的读值节点 IgniteHistoryFireValue 求值，本方法只做 kind 折叠转发。
    // 静态 amount / 门控 / 掷骰类读口见 magnitudeOf / gateOf / rollChance。
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 被你点燃过的敌人递增承火伤加成（{@link MarkerStatEffect.Kind#IGNITE_HISTORY_FIRE_TAKEN}）：
     * 遍历攻击方 STAT_STORE，命中该 kind 标记的条目，条件通过者按其 <b>amount 表达式</b>求值折叠。
     *
     * <p><b>幅度来源为读值节点</b>：kind 为 {@code ignite_history_fire_taken} 的效果是纯身份标记，
     * 幅度公式由 {@link IgniteHistoryFireValue} 节点承担——由接线写进条目 {@code amount}，在
     * {@code min(受害方 IGNITE_HISTORY[攻击方 UUID] 累计秒数 * rate, cap)} 上求值。
     * 聚合口径为「每条目各自 min 后求和」，多来源叠加。
     *
     * <p>求值作用域：本方法在命中结算内被调用（{@code DamagePipeline#applyStatusDamageTakenModifiers}），
     * 攻击者当前 StatContext 已由 {@code HitScopeHelper#enterHitScopes} 绑定 sourceEntity=受害方，
     * 故节点读到的施加者/受害方与调用点一致。
     */
    public static double igniteHistoryFireTakenBonus(LivingEntity attacker, LivingEntity living) {
        if (attacker == null || living == null) return 0.0;
        // 快速剪枝：从未点燃过则各条目节点均返回 0，无需遍历 store 与求值条件。
        Float secs = living.getData(AttachmentRegistry.IGNITE_HISTORY).get(attacker.getUUID());
        if (secs == null || secs <= 0f) return 0.0;
        return magnitudeOf(attacker, MarkerStatEffect.Kind.IGNITE_HISTORY_FIRE_TAKEN, Set.of());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 惰性标记的一等公民查询：26 个 MarkerStatEffect.Kind 的读口。
    // 各方法仿 markerMagnitude 遍历 STAT_STORE 条目 + instanceof 新类 + passingCondEntity 门控。
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 概率掷骰查询（推荐入口为下方 {@link MarkerStatEffect.Slot} 口径重载）：遍历持有者 STAT_STORE，
     * 命中 {@link MarkerStatEffect} 且 kind 身份匹配、且 queryTags 包含其 identityTags 的条目，
     * 累加其「概率」幅度（amount，经 {@code scale_attr} 缩放）。调用方持总和 P 掷一次：
     * {@code rnd < min(1, P)} 即命中（SIMPLE 语义）。
     */
    public static double rollChance(LivingEntity living, String type, Set<String> queryTags) {
        return rollChance(living, MarkerStatEffect.slotOf(type, MarkerStatEffect.Variant.ROLLED), type, queryTags, null);
    }

    /**
     * {@link MarkerStatEffect.Slot} 口径的概率掷骰查询（推荐入口）。
     *
     * <p>命中条件：Slot 所属 Variant == {@code slot.variant()}（即 ROLLED）、kind 身份匹配、
     * {@code queryTags ⊇ identityTags}、条目条件通过。调用方持总和 P 掷一次：
     * {@code rnd < min(1, P)} 即命中（SIMPLE 语义）。</p>
     *
     * <p><b>幅度缩放</b>：命中条目的 {@code scale_attr} 一并经 {@link #scaledMagnitude} 应用
     * （JSON 写的 {@code scale_attr} 必须真正参与掷骰，否则缩放静默失效）。</p>
     *
     * @param expectedSlot 调用方期望的语义槽位；与数据声明的 variant 错配时返回 0（构造期已抛异常拦截落盘）
     */
    public static double rollChance(LivingEntity living, MarkerStatEffect.Slot expectedSlot, String type,
                                    Set<String> queryTags, @Nullable RandomSource rng) {
        return scanMarkerEntries(living, expectedSlot.variant(), type,
                TagGate.IDENTITY_TAGS, queryTags, true, MarkerFold.SUM, null);
    }

    /**
     * 次数掷骰查询（MarkerStatEffect，COUNT 语义）：与 {@link #rollChance} 同口径累加概率 P，
     * 再按 PoE2 语义拆为「保底次数 + 余概率」返回命中次数：
     * {@code floor(P) + (rest>0 && rng.nextDouble()<rest ? 1 : 0)}。rng 为 null 时回退 owner 随机源。
     */
    public static int rollCount(LivingEntity living, String type, Set<String> queryTags, RandomSource rng) {
        return rollCount(living, MarkerStatEffect.slotOf(type, MarkerStatEffect.Variant.ROLLED), type, queryTags, rng);
    }

    /**
     * {@link MarkerStatEffect.Slot} 口径的次数掷骰查询（推荐入口）。
     *
     * <p>与 {@link #rollChance} 同口径累加概率 P（同样应用 {@code scale_attr}），
     * 再按 PoE2 语义拆为「保底次数 + 余概率」返回命中次数：
     * {@code floor(P) + (rest>0 && rng.nextDouble()<rest ? 1 : 0)}。rng 为 null 时回退 owner 随机源。</p>
     *
     * <p><b>注意（{@code RollMode.COUNT} 退化）</b>：当前三个 COUNT 生产者
     * （{@code additional_spawn_chance} / {@code entity_stage_skip_chance} / {@code entity_stage_advance_chance}）
     * 均为单条目单分配，{@code floor(P)} 恒为 0，实际退化为「一次余概率掷骰」。这不是 bug，
     * 但意味着 COUNT 与 SIMPLE 行为等价；待多来源叠加（多条目同 kind 生效）后 COUNT 才显出价值。</p>
     */
    public static int rollCount(LivingEntity living, MarkerStatEffect.Slot expectedSlot, String type,
                                Set<String> queryTags, @Nullable RandomSource rng) {
        double sum = scanMarkerEntries(living, expectedSlot.variant(), type,
                TagGate.IDENTITY_TAGS, queryTags, true, MarkerFold.SUM, null);
        if (sum <= 0.0) return 0;
        RandomSource random = rng != null ? rng : living.getRandom();
        int guaranteed = (int) Math.floor(sum);
        double rest = sum - guaranteed;
        return Math.max(0, guaranteed + (rest > 0.0 && random.nextDouble() < rest ? 1 : 0));
    }

    /**
     * 门控查询：遍历持有者 STAT_STORE，命中
     * {@link MarkerStatEffect} 且 kind 身份匹配、且 queryTags 包含其 identityTags 的条目，
     * 任一命中条目幅度 > 0 即返回 true（presence + amount>0）。
     */
    public static boolean gateOf(LivingEntity living, String type, Set<String> queryTags) {
        return gateOf(living, MarkerStatEffect.slotOf(type, MarkerStatEffect.Variant.GATE), type, queryTags);
    }

    /**
     * {@link MarkerStatEffect.Slot} 口径的门控查询（推荐入口）。
     *
     * <p>任一命中条目（Slot 所属 Variant == GATE、kind 身份匹配、{@code queryTags ⊇ identityTags}、
     * 条目条件通过）经 {@link #scaledMagnitude} 缩放后幅度 > 0 即返回 true（presence + amount>0）。
     * 注意 {@code scale_attr} 参与判定：缩放属性为负时门控会被误判为开。</p>
     */
    public static boolean gateOf(LivingEntity living, MarkerStatEffect.Slot expectedSlot, String type, Set<String> queryTags) {
        return scanMarkerEntries(living, expectedSlot.variant(), type,
                TagGate.IDENTITY_TAGS, queryTags, true, MarkerFold.ANY_POSITIVE, null) > 0.0;
    }

    /**
     * 幅度查询：遍历持有者 STAT_STORE，命中
     * {@link MarkerStatEffect} 且 kind 身份匹配、且 queryTags 包含其 identityTags 的条目，
     * 累加其幅度（amount）后返回（与 {@link #markerMagnitude} 同口径）。
     */
    public static double magnitudeOf(LivingEntity living, String type, Set<String> queryTags) {
        return magnitudeOf(living, MarkerStatEffect.slotOf(type, MarkerStatEffect.Variant.MAGNITUDE), type, queryTags);
    }

    /**
     * {@link MarkerStatEffect.Slot} 口径的幅度查询（推荐入口）。
     *
     * <p>命中条目（Slot 所属 Variant == MAGNITUDE、kind 身份匹配、{@code queryTags ⊇ identityTags}、
     * 条目条件通过）经 {@link #scaledMagnitude} 应用 {@code scale_attr} 后累加返回。注意
     * {@code scale_attr} 必须真读：{@code shield_front_debilitate}（米）/
     * {@code invocation_trigger_interval}（秒）等 45+ kind 均依赖它缩放幅度。</p>
     */
    public static double magnitudeOf(LivingEntity living, MarkerStatEffect.Slot expectedSlot, String type, Set<String> queryTags) {
        return scanMarkerEntries(living, expectedSlot.variant(), type,
                TagGate.IDENTITY_TAGS, queryTags, true, MarkerFold.SUM, null);
    }

    /**
     * 收集门控身份标签集合：遍历持有者 STAT_STORE，
     * 命中 {@link MarkerStatEffect} 且 kind 身份匹配、且 passingCondEntity 通过的条目，
     * 将其 identityTags 并入返回集合（不查 amount）。
     * 消费端 HitScopeHelper#addWeaponTags 取并集后按标签门控。
     */
    public static Set<String> collectGateTags(LivingEntity living, String type) {
        return collectGateTags(living, MarkerStatEffect.slotOf(type, MarkerStatEffect.Variant.GATE), type);
    }

    /**
     * {@link MarkerStatEffect.Slot} 口径的门控身份标签收集（推荐入口）。
     * 命中条目（Slot 所属 Variant == GATE、kind 身份匹配、条目条件通过）的 identityTags 并入返回集合
     * （不查 amount，忠实原 weaponScopeTags 语义）。消费端 HitScopeHelper#addWeaponTags 取并集后按标签门控。
     */
    public static Set<String> collectGateTags(LivingEntity living, MarkerStatEffect.Slot expectedSlot, String type) {
        Set<String> tags = new HashSet<>();
        scanMarkerEntries(living, expectedSlot.variant(), type,
                TagGate.NONE, null, true, MarkerFold.TAG_UNION, tags);
        return tags;
    }

    /**
     * 「防止 X% 偏转命中伤害」幅度和（{@link PreventStatEffect}，DEFLECT 簇 #32/#33）。
     * <p><b>为何不复用 {@link #scanMarkerEntries}</b>：本读口扫描的是 {@link PreventStatEffect}
     * 而非 {@link MarkerStatEffect} —— 它没有 kind 身份串、没有 {@code slot()/variant()} 维度，
     * 身份由两个布尔（{@code deflected} / {@code crit}）承载，且 {@code scale_attr} 恒为 {@code null}
     * （本效果不参与属性缩放）。把它硬塞进标记内核需要再加一层与 marker 无关的分支，
     * 反而使标记热路径多承担一个永不命中的判定。此处保留独立循环，使
     * 「INERT 效果的 STAT_STORE 折叠口径」同样收拢在本类内（消费端不再各自手抄）。
     */
    public static double preventDeflectedMagnitude(LivingEntity living, boolean crit) {
        if (living == null || !living.hasData(AttachmentRegistry.STAT_STORE)) return 0.0;
        ExpressionContext ec = ExpressionContext.get(living);
        double sum = 0.0;
        for (StatEntry e : living.getData(AttachmentRegistry.STAT_STORE).entries()) {
            if (e.effect() instanceof PreventStatEffect p) {
                if (!p.deflected()) continue;
                if (p.crit() && !crit) continue;
                sum += scaledMagnitude(living, e.evaluateAmount(ec), null);
            }
        }
        return sum;
    }

    /**
     * 掷骰修饰查询（fold 型、纯读取，无副作用）：判定点（伤害偏差 / 暴击 / 闪避 / 格挡）调用，
     * 询问「实体在某掷骰维度上是否持有 LUCKY / UNLUCKY 修饰」。
     * <p>
     * 遍历实体 STAT_STORE 的 {@code lucky} 索引，效果身份字段（{@link LuckyStatEffect#subject()}）
     * 匹配且条目条件通过的条目决定结果；多条目冲突时<b>任一 LUCKY 即优</b>（增益语义不被减益压过），
     * 否则任一 UNLUCKY 即劣；无命中返回 null。
     *
     * @param entity  被判定实体（按维度归属：伤害/暴击取攻击方，闪避/格挡取受击方）
     * @param subject 掷骰维度
     * @return {@link LuckyStatEffect.RollMode#LUCKY} / {@link LuckyStatEffect.RollMode#UNLUCKY} / null（无修饰）
     */
    /**
     * 掷骰修饰查询（fold 型、纯读取，无副作用）：判定点（伤害偏差 / 暴击 / 闪避 / 格挡）调用，
     * 询问「实体在某掷骰维度上是否持有 LUCKY / UNLUCKY 修饰」。
     * <p>
     * 遍历实体 STAT_STORE 的 {@code lucky} 索引，效果身份字段（{@link LuckyStatEffect#subject()}）
     * 匹配且条目条件通过的条目决定结果；多条目冲突时<b>任一 LUCKY 即优</b>（增益语义不被减益压过），
     * 否则任一 UNLUCKY 即劣；无命中返回 null。
     *
     * @param entity    被判定实体（按维度归属：伤害/暴击取攻击方，闪避/格挡取受击方）
     * @param subject   掷骰维度
     * @param scopeTags 作用域标签集合（伤害类型分维，#5）：非空时仅标签命中或无标签的条目参与；
     *                 空集合 = 不按类型过滤（crit/evade/deflect 等均走此路径）
     * @return {@link LuckyStatEffect.RollMode#LUCKY} / {@link LuckyStatEffect.RollMode#UNLUCKY} / null（无修饰）
     */
    public static LuckyStatEffect.@Nullable RollMode rollModifier(LivingEntity entity,
                                                                  LuckyStatEffect.RollSubject subject,
                                                                  Set<String> scopeTags) {
        if (entity == null || !entity.hasData(AttachmentRegistry.STAT_STORE)) return null;
        StatStore store = entity.getData(AttachmentRegistry.STAT_STORE);
        List<StatEntry> candidates = store.luckyIndex();
        if (candidates.isEmpty()) return null;

        StatContext scope = ExpressionContext.get(entity).currentStatContext();
        boolean unlucky = false;
        for (StatEntry e : candidates) {
            LuckyStatEffect effect = (LuckyStatEffect) e.effect();
            if (effect == null || effect.subject() != subject) continue;
            // 伤害类型分维（#5）：scopeTags 非空时，仅标签命中（或无标签/恒真）的条目参与；
            // 带具体类型标签（如 {lightning}）的条目在其它类型伤害上被正确排除。
            if (!scopeTags.isEmpty() && !effect.tags().isAlwaysTrue() && !effect.tags().matches(scopeTags)) continue;
            LivingEntity condEntity = passingCondEntity(entity, scope, e, false);
            if (condEntity == null) continue;
            // 几率门控（R3）：词条 amount = 触发概率（0~1），逐次掷骰命中才生效。binary 幸运（amount=1.0）恒生效，
            // 与既有 CHANCE_TO_DEFLECT_IS_LUCKY / CHANCE_TO_EVASION_IS_UNLUCKY 同源：amount 即真实命中概率。
            double chance = Math.max(0.0, Math.min(1.0, e.amount().getValue()));
            if (chance < 1.0 && entity.getRandom().nextDouble() >= chance) continue;
            if (effect.mode() == LuckyStatEffect.RollMode.LUCKY) return LuckyStatEffect.RollMode.LUCKY;
            unlucky = true;
        }
        return unlucky ? LuckyStatEffect.RollMode.UNLUCKY : null;
    }

    /** 原 2 参（不按伤害类型分维，行为不变）：委托空作用域标签集合。 */
    public static LuckyStatEffect.@Nullable RollMode rollModifier(LivingEntity entity,
                                                                  LuckyStatEffect.RollSubject subject) {
        return rollModifier(entity, subject, Set.of());
    }

    /**
     * 命中标志查询（fold 型、纯读取，无副作用）：判定点（闪避 / 暴击短路）调用，
     * 询问「实体是否持有指定命中标志」（如攻击方 UNEVADABLE → 受击方不可闪避）。
     * <p>
     * 遍历实体 STAT_STORE 的 {@code hit_flag} 索引，效果身份字段（{@link HitFlagStatEffect#flag()}）
     * 匹配且条目条件通过的条目即判持有。
     *
     * <p><b>条件可跨实体</b>：条目 {@code conditionTarget=trigger_target} 时按当前命中作用域的
     * 交互对方求值（见 {@link #evalCtx}）——UNEVADABLE 查询方为攻击方，则对方即受击方，
     * 从而支持「Your Hits cannot be Evaded by Pinned Enemies」（条件 = 受击方持有钉刺状态）；
     * UNCRITTABLE 查询方为受击方，则对方即攻击方（「不会被 X 类攻击者暴击」类语料的通道）。
     * 条件落在自身状态时（如「格挡姿态中不会被暴击」）用缺省 SELF 即可。
     *
     * @param entity 被判定实体（按标志语义：UNEVADABLE 取攻击方，UNCRITTABLE 取受击方）
     * @param flag   命中标志
     */
    public static boolean hasHitFlag(LivingEntity entity, HitFlagStatEffect.HitFlag flag) {
        if (entity == null || !entity.hasData(AttachmentRegistry.STAT_STORE)) return false;
        StatStore store = entity.getData(AttachmentRegistry.STAT_STORE);
        List<StatEntry> candidates = store.hitFlagIndex();
        if (candidates.isEmpty()) return false;

        StatContext scope = ExpressionContext.get(entity).currentStatContext();
        for (StatEntry e : candidates) {
            HitFlagStatEffect effect = (HitFlagStatEffect) e.effect();
            if (effect == null || effect.flag() != flag) continue;
            LivingEntity condEntity = passingCondEntity(entity, scope, e, false);
            if (condEntity == null) continue;
            return true;
        }
        return false;
    }

    // ───────────────────────────── 投送修饰（Delivery）查询 ─────────────────────────────

    /** 一条投送修正（fold 查询结果）：投送类型 + 参数表（调用方按投送类型读取对应参数）。 */
    public record DeliveryResult(String delivery, java.util.Map<String, Double> params) {}

    /**
     * 投送修饰查询（fold 型，无副作用）：在 entity 当前 StatContext 作用域下，返回全部满足
     * 作用域条件的「投送参数修正」规则（聚合后供 SupportProfile 投送执行时应用）。
     * 参数值经 {@code e.evaluateAmount} 求值（amount 即该参数的数值）。
     *
     * @param entity 投送修饰的归属实体（技能施放者 / 装备持有者）
     * @return 命中规则列表（投送类型 → 参数名 → 数值）；无命中返回空列表
     */
    public static List<DeliveryResult> delivery(LivingEntity entity) {
        List<DeliveryResult> out = new ArrayList<>();
        if (entity == null || !entity.hasData(AttachmentRegistry.STAT_STORE)) return out;
        StatStore store = entity.getData(AttachmentRegistry.STAT_STORE);
        List<StatEntry> candidates = store.deliveryIndex();
        if (candidates.isEmpty()) return out;

        StatContext scope = ExpressionContext.get(entity).currentStatContext();
        for (StatEntry e : candidates) {
            DeliveryStatEffect effect = (DeliveryStatEffect) e.effect();
            if (effect == null) continue;
            LivingEntity condEntity = passingCondEntity(entity, scope, e, false);
            if (condEntity == null) continue;
            double amount = e.evaluateAmount(ExpressionContext.get(entity));
            if (amount == 0) continue;
            java.util.Map<String, Double> params = new java.util.HashMap<>();
            effect.params().forEach((k, v) -> params.put(k, v.getValue(ExpressionContext.get(entity))));
            // 单参数便捷形态：amount 即参数值（entry 的 amount 语义 = 参数数值）
            if (effect.params().isEmpty()) params.put("amount", amount);
            out.add(new DeliveryResult(effect.delivery(), params));
        }
        return out;
    }
}
