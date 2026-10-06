package com.cimdy.coe.data.codec.support.stat.entry;

import com.cimdy.coe.data.codec.support.stat.condition.StatCondition;
import com.cimdy.coe.data.codec.support.stat.entry.describer.StatDescriber;
import com.cimdy.coe.data.codec.support.stat.target.StatTargetsInline;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffectTypes;
import com.cimdy.coe.data.codec.support.stat.duration.StatDuration;
import com.cimdy.coe.data.codec.support.stat.duration.StatDurationRead;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import com.cimdy.coe.event.data.LoadEvent;
import com.cimdy.coe.CraftOfExile;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * <b>Stat 条目接口</b>——一条词缀的完整形态：{@code 数值 + 条件 + 选择器 + 效果}。
 *
 * <h2>两种实现可互换地存入 {@link StatStore}</h2>
 * <ul>
 *   <li>{@link StatRead}：唯一具体条目类，同时承载「内联定义」与「引用」两种形态——
 *       内联定义时数值 / 条件 / 选择器 / 效果全部平铺在本对象（条件段为命名 ref，mirror effect/targets）；
 *       引用形态仅持 {@code ref}（+ 实例覆盖层 {@code vars}），调用时从
 *       {@link com.cimdy.coe.event.data.LoadEvent#STAT_ENTRY_MAP} 转发全部行为。</li>
 *   <li>{@link StatGroup}：组合型（多个条目按组语义聚合，只用接口不构造 {@link StatRead}）。</li>
 * </ul>
 *
 * <h2>职责拆分</h2>
 * <ul>
 *   <li>{@link #amount()}：本词缀<b>唯一</b>的数值（算式 + 局部变量）；</li>
 *   <li>{@link #conditions()}：内联条件（实时求值，值非 0 即通过）；</li>
 *   <li>{@link #targets()}：三段选择器（条件判定 / 数值求值 / 作用目标）；</li>
 *   <li>{@link #effect()}：做什么（{@link StatEffect}，只带身份/语义字段与事件标签）。</li>
 * </ul>
 *
 * <h2>唯一数值原则</h2>
 * <b>一个条目 ≡ 一条词缀 ≡ 恰好一个数值。</b> 需要综合多个来源时在 math 表达式内部组合；
 * 确需两个数即为两条词缀，拆成两个条目。
 *
 * <h2>JSON 形态（自动派发）</h2>
 * 含 {@code ref} 字段 → 解码为 {@link StatRead}（引用缓存）；否则解码为 {@link Stat}（内联定义）。
 * 内联定义全部字段平铺在同一对象：{@code amount / conditions /
 * target / condition_target / value_target / type + 效果自身字段}。
 */
public interface StatEntry {

    /**
     * 分发式 MapCodec：按 {@code entry} 字段（注册表名字符串）派发到 {@link StatEntries} 中登记的
     * {@link Stat}（内联）或 {@link StatRead}（引用）或 {@link StatGroup}（组合）。
     *
     * <p><b>派发键为 {@code entry} 而非 {@code type}</b>——后者与平铺的
     * {@link StatEffectTypes#MAP_CODEC}（同为 {@code type} 键，值如 {@code attribute}/{@code damage}）
     * 在同一 JSON 对象层级冲突：encode 时 effect 的 type 覆盖条目 type，decode 时按错的 type 派发。
     * 条目层用 {@code entry}、效果层保留 {@code type}，两个键并存不冲突。
     *
     * <p><b>注意</b>：此处必须用 {@link com.mojang.serialization.Codec#STRING} 做纯字符串派发，
     * 不可用 {@code StatEntries.REGISTRY.byNameCodec().dispatchMap(...)}——
     * {@code byNameCodec} 在 {@code DatapackBuiltinEntriesProvider}（RegistryOps）序列化
     * {@code coe/stat_entry} 注册表时，会把 {@code entry} 字段值当成该 Forge 注册表
     * （{@code coe:stat_entry_type}）的 <b>holder 引用</b>，而该注册表在 RegistryOps 的 lookup
     * 中不存在，从而报 {@code Unregistered holder}。纯字符串派发（同 {@code StatEffectTypes}）则无此问题。
     */
    MapCodec<StatEntry> MAP_CODEC = Codec.STRING.dispatchMap("entry",
            e -> StatEntries.nameOf(e.getType()),
            StatEntries::codecFor);

    /** 扁平 Codec：内联 / 引用统一形态，由 {@link #MAP_CODEC} 派发。 */
    Codec<StatEntry> CODEC = MAP_CODEC.codec();

    /** 分发类型：每个实现在 {@link StatEntries} 注册表登记自己的 {@link #CODEC}。 */
    record Type<T extends StatEntry>(MapCodec<T> codec) {
    }

    // ---------------------------------------------------------------- 四段数据（抽象）

    /** 本词缀唯一数值（常量或 math 表达式）。 */
    ExpressionCodec.Value amount();

    /** 内联谓词（由 {@link com.cimdy.coe.data.codec.support.stat.condition.FilterCondition} 过滤实体集）。 */
    List<ValueExpr> conditions();

    /** 实体获取条件；{@code null} 表示直接使用 Target 锁定的基础实体。 */
    @Nullable
    default StatCondition condition() {
        return null;
    }

    /** 条件引用（命名 id）；null = 无。StatRead 持字段覆写，其余实现回落 null。 */
    @Nullable
    default Identifier conditionRef() {
        return null;
    }

    /**
     * 三段选择器（条件判定 / 数值求值 / 作用目标）。自带 read 形态：
     * 内联由 {@link StatTargetsInline}（{@code type:"inline"}）承载，共享预设以
     * 内联 {@code StatTargets} 对象（无引用型 targets），
     * 引用能力内聚在 {@link StatTargets} 类型内部（节点自身实现 read 形式），
     * 而非外层 {@code Either<StatTargets, Identifier>} 包裹。
     */
    StatTargets targets();

    /**
     * 做什么（只带身份/语义字段与事件标签）。
     * <p>效果经 {@link StatEffectTypes} 按 {@code type} 分派；共享效果直接内联复用同一 {@link StatEffect}
     * （{@code type:"read"}）引用落盘的 {@code stat_effect} 资源——引用能力内聚在 {@link StatEffect}
     * 类型内部（节点自身实现 read 形式），而非外层 {@code Either<StatEffect, Identifier>} 包裹。
     */
    StatEffect effect();

    /**
     * <b>第五部分 · 时长段</b>（可空）：本条目施加的效果「在时间上如何存续 / 复触发」。
     *
     * <p>时长不是某个效果的私有字段——状态施加、属性授予、周期光环、冷却触发……<b>所有</b>效果
     * 都需要「持续多久 / 多久能再来一次」的语义，故与 amount / conditions / targets / effect 并列
     * 成为条目的第五部分，<b>对所有 Effect 生效</b>；Effect 实现内部一律不再持有自己的时长字段。
     *
     * <p>三种时间语义见 {@link StatDuration.Kind}：
     * <ul>
     *   <li>{@code duration:120} —— 触发且持续一段时间（施加后持续 N tick 自然结束）；</li>
     *   <li>{@code cooldown:60} —— 触发后需要一段时间才能再次触发（冷却）；</li>
     *   <li>{@code interval:20} —— 触发后需要持续时间结束后才能再次触发（周期节拍）。</li>
     * </ul>
     * 引用形态 {@code read:coe:xxx} 见 {@link StatDurationRead}。
     *
     * <p><b>可空</b>：未声明（{@code null}）= 不计时、不冷却、不周期，由各效果自身默认语义决定
     * （如状态取 {@code StatusType.defaultDurationTicks}）。
     */
    @Nullable
    default StatDuration duration() {
        return null;
    }

    /**
     * 本条目施加效果的<b>节拍</b>（tick）：{@code interval:N} 表示每隔 N tick 重新施加一次
     * （每次持续 N tick 无缝续期）。与 {@link #duration()}（DURATION 存续时长）并列、互不替代：
     * 半径光环等周期性条目同时声明 {@code duration}（单次施加有效期）与 {@code interval}（节拍）。
     * <p>缺省 {@code null} = 未声明节拍，由 {@link PeriodicDriver} 回落到 {@code duration} 的 INTERVAL
     * 语义（向后兼容既有周期条目）。</p>
     */
    @Nullable
    default StatDuration interval() {
        return null;
    }

    /**
     * 本条目的时长（tick）。未声明时长（{@link #duration()} 为 {@code null}）返回 {@code -1}，
     * 由调用方回落到自身默认语义——<b>不可</b>把 -1 当作「时长为 -1 tick」直接使用。
     */
    default int durationTicks() {
        StatDuration d = duration();
        return d == null ? -1 : d.ticks();
    }

    /**
     * 本条目的时间语义种类；未声明时长返回 {@code null}。
     */
    default StatDuration.@Nullable Kind durationKind() {
        StatDuration d = duration();
        return d == null ? null : d.kind();
    }

    /** 返回替换时长段后的副本（默认返回自身——无时长语义的条目类型不受影响）。 */
    default StatEntry withDuration(@Nullable StatDuration duration) {
        return this;
    }

    /** 返回替换节拍（interval）后的副本（默认返回自身——无周期语义的条目类型不受影响）。 */
    default StatEntry withInterval(@Nullable StatDuration interval) {
        return this;
    }

    /**
：{@code $name} 变量在该词缀 amount / conditions / describe
     * 中的默认值。实例化时由 {@code vars}（引用形态）或 roll 值覆盖。缺省空 Map。
     *
     * <p>六系统统一参数表的落点：Stat / StatRead / StatGroup / Skill / Item roll / Talent 全部经此
     * 通道注入。语法与 math 系统 {@code $name} 一致（MathParser），不发明新语法。
     */
    default Map<String, ExpressionCodec.Value> params() {
        return Map.of();
    }

    /**
     * 有效参数：定义 params ⊕ 实例覆盖层（vars / roll，由实现决定）的合并结果。
     * 求值期统一经 {@link #bindParams} 注入；{@link StatRead} 覆写为 params⊕vars（实例值优先于模板默认值），
     * {@link StatGroup} 覆写为组级⊕首 member（摊平语义）。
     */
    default Map<String, ExpressionCodec.Value> effectiveParams() {
        return params();
    }

    /**
     * 把本词缀的有效参数注入上下文：{@code ec.withVariables(有效参数)}。
     * 求值路径（amount / conditions / describe）统一经此入口绑参
     * （vars 只注入 amount、conditions 无通道）。
     */
    default ExpressionContext bindParams(ExpressionContext ec) {
        return ec.withVariables(effectiveParams());
    }

    /**
     * 返回本条目的「内联形态」副本：默认自身即为内联返回 {@code this}；
     * {@link StatRead} 覆写为「解析引用后构造内联 {@link Stat}」——取代在 withAmount/withEffect
     * 里的隐式形态降级（引用语义丢失的魔法），使降级显式化、可审计。
     */
    default StatEntry toInline() {
        return this;
    }

    /**
     * 替换参数槽后的副本（用于 item roll 延迟绑定 / 引用覆盖）。默认实现：仅当无参数时原样返回，
     * 有参数时由实现决定（{@link Stat} 返回带新 params 的内联副本、{@link StatRead} 并入 vars）。
     */
    default StatEntry withParams(Map<String, ExpressionCodec.Value> newParams) {
        return this;
    }

    /** 分发类型标记：决定 JSON 经 {@link StatEntries} 派发到哪个实现。 */
    Type<?> getType();

    /**
     * 本条目的来源 id（用于 {@link StatStore} 的 {@code Map<String, StatEntry>} 键）。
     * <ul>
     *   <li>默认 {@code ""}：由 {@link StatStore} 构造时自适应分配（{@code auto_n}），避免破坏现有内联/引用条目；</li>
     *   <li>装备修饰符等需稳定寻址的来源应覆盖此值（约定前缀 {@code "equip:" + slot + ":" + itemUuid}），
     *       以便经 {@link StatStore#addModifier(String, StatEntry)} / {@link StatStore#removeModifier(String)} 精确增删。</li>
     * </ul>
     */
    default String id() {
        return "";
    }

    // ---------------------------------------------------------------- 转发访问（默认实现）

    /** 事件标签（挂在效果上，标签布尔表达式）。 */
    default GateExpr tags() {
        StatEffect e = effect();
        return e != null ? e.tags() : GateExpr.ALWAYS;
    }

    /**
     * 外溢作用域：本条目的唯一数值<b>额外</b>计入的作用域（数值维度，非条件 / 非目标选择）。
     *
     * <p>缺省 {@link GateExpr#NEVER}（不外溢）。语义为「本条目在其 {@link #tags()} 作用域生效，
     * 并且同样在这些作用域生效」——两段是<b>并集</b>关系，故本条目在两段作用域下<b>各计一次</b>，
     * 来源维度的贡献不被扣减。
     *
     * <p>与 {@link #convertTo()}（转换 / MOVE）是两个<b>不可互换</b>的机制：本字段两侧都算，
     * 转换只算目标侧（见 {@link #effectiveTags()}）。
     */
    default GateExpr spillTo() {
        return GateExpr.NEVER;
    }

    /**
     * 转换作用域（<b>转换</b> / MOVE）：本条目的唯一数值<b>取代</b>自身 {@link #tags()} 作用域、
     * 只计入这些作用域（数值维度，非条件 / 非目标选择）。
     *
     * <p>缺省 {@link GateExpr#NEVER}（不转换）。与 {@link #spillTo()} 的关键差别是
     * <b>并集 → 替代</b>：外溢下来源与目标两侧各完整生效一次（COPY），转换下来源作用域
     * <b>不再计入</b>（MOVE）——由 {@link #effectiveTags()} 的门控重映射实现。
     *
     * <p><b>方向性由数据形状保证</b>：与 {@link #spillTo()} 同构，本字段是挂在<b>贡献方</b>条目上的
     * 一元声明（「我的数值改计入这些作用域」），不是 A 与 B 之间的成对关系。转换在聚合侧只做
     * <b>一次门控表达式替换</b>，不沿目标再解析、不递归、不做不动点迭代，故数据里即便同时写出
     * 「A 的条目转换到 B」与「B 的条目转换到 A」，两条声明也各自独立求值、互不触发，
     * 求和仍在有限条目集上收敛。
     *
     * <p>与 {@code DamageConvertStatEffect} 的分工：后者在命中结算层按 ratio 重路由
     * <b>伤害值本身</b>的构成（from 扣除 / to 增加，目标类型可 FIXED/RANDOM/CYCLE）；本字段只声明
     * <b>属性增量计入哪个倍率池</b>，是纯作用域重映射，故目标恒为 {@link #convertTo()} 给出的
     * 唯一表达式，无需模式字段与候选列表。
     */
    default GateExpr convertTo() {
        return GateExpr.NEVER;
    }

    /**
     * 本条目的实际作用域门控。<b>转换优先于外溢</b>：
     *
     * <ul>
     *   <li>声明 {@link #convertTo()} → 门控<b>重映射</b>为 {@code convertTo}（MOVE：来源作用域不再计入）；</li>
     *   <li>未声明 → 门控 = {@link #tags()} ∪ {@link #spillTo()}（COPY：两侧各计一次）。</li>
     * </ul>
     *
     * <p>两条分支都只在<b>同一趟</b>单次遍历里改写布尔门控表达式，
     * {@code StatEngine.aggregate} 的累加与 {@code fold} 的折叠运算完全不变、不复制。
     *
     * <p>缺省外溢时 {@code tags().or(NEVER) == tags()}（同一实例，见 {@link GateExpr#or} 吸收律），
     * 故未声明外溢 / 转换的条目在门控与分配上零开销不变。
     */
    default GateExpr effectiveTags() {
        GateExpr to = convertTo();
        if (!to.isNever()) return to;
        return tags().or(spillTo());
    }

    /**
     * 是否为触发亚型（由 {@link StatEngine#fire} 驱动）。判据在<b>条目层</b>，不在 Effect 内：
     * <ul>
     *   <li>Effect 自报触发型（{@link StatEffect#isTrigger()}，默认 {@code true}）→ 触发型；</li>
     *   <li>Effect 是<b>折叠型</b>（{@link StatEffect.EffectKind#FOLD}，如属性亚型）但本条目
     *       <b>声明了时长</b>（{@link #duration()}）→ 也判为触发型：折叠语义是「每次读取重估」，
     *       无法表达「持续 N tick 后消失」，故必须走「事件时刻物化 + 计时域到期」通道。</li>
     * </ul>
     * {@link com.cimdy.coe.data.codec.support.stat.engine.StatStore} 的索引构建用<b>同一判据</b>
     * 把条目投入 FIRE 桶，两处必须保持一致。
     */
    default boolean isTrigger() {
        StatEffect e = effect();
        if (e == null) return false;
        if (e.isTrigger()) return true;
        return e.indexKind() == StatEffect.EffectKind.FOLD && duration() != null;
    }

    /** 作用阶段选择器；{@code null} = 持有者自身。 */
    @Nullable
    default TargetSelector target() {
        return targets().target();
    }

    /** 条件阶段「被判定实体」；{@code null} = 持有者自身。 */
    @Nullable
    default TargetSelector conditionTarget() {
        return targets().conditionTarget();
    }

    /** 数值阶段「被取值实体」；{@code null} = 持有者自身。 */
    @Nullable
    default TargetSelector valueTarget() {
        return targets().valueTarget();
    }

    /** UI 展示文本：统一经 {@link StatDescriber} 装配（P1 反向拼装翻译入口）；默认以 {@link Locale#ROOT} 回落。 */
    default List<Component> describe() {
        return describe(Locale.ROOT);
    }

    /** UI 展示文本（本地化）：经 {@link StatDescriber} 装配，按 locale 做数字本地化（P3）。 */
    default List<Component> describe(Locale locale) {
        return StatDescriber.assemble(this, locale);
    }

    /** UI 展示文本（本地化 + 范围值，重中之重）：把词缀 roll 区间(min~max)注入描述链。 */
    default List<Component> describe(Locale locale, Optional<ValueRange> range) {
        return StatDescriber.assemble(this, locale, range);
    }

    // ---------------------------------------------------------------- 求值 / 目标

    /** 唯一取数入口：常量分支零开销，表达式分支经 vars 注入后求值（统一经 {@link #bindParams}）。 */
    default double evaluateAmount(ExpressionContext ec) {
        return amount().getValue(bindParams(ec));
    }

    /**
     * 唯一数值的一次拆分结果。
     *
     * @param receiver 该数值的接收者；{@code null} 表示「未拆分」——应施加到
     *                 {@link #resolveTargets} 选出的全部目标
     */
    record AmountSplit(@Nullable LivingEntity receiver, double value) {
    }

    /**
     * <b>统一取数</b>：按 {@link #valueTarget()} 取「被取值实体」求出本词缀的唯一数值，
     * 并支持求值期 EACH 拆分。{@code scope} 提供事件名/命中上下文注入（经 {@code scope.evalBase}）；
     * 为 null 时回落裸单例基座（非事件场景）。
     *
     * @return 空列表 = 被取值实体缺失，调用方应直接跳过；<br>
     * 单条且 {@code receiver == null} = 未拆分，该数值施加到 {@link #resolveTargets} 的全部目标；<br>
     * 其余 = 求值期已按候选一对一拆分，逐条施加到各自 {@code receiver}
     */
    default List<AmountSplit> evaluateAmountSplit(StatEvalContext evalCtx, @Nullable StatContext scope) {
        LivingEntity valueEntity = evalCtx.resolve(valueTarget());
        if (valueEntity == null) return List.of();
        List<AmountSplit> pairs = new ArrayList<>();
        ExpressionContext base = scope != null ? scope.evalBase(valueEntity) : ExpressionContext.get(valueEntity);
        double total = ExpressionCodec.getValue(amount().value(), bindParams(base),
                (e, v) -> pairs.add(new AmountSplit(e, v)));
        return pairs.isEmpty() ? List.of(new AmountSplit(null, total)) : pairs;
    }

    /** 本条目应作用于的所有实体（默认持有者自身）。 */
    default List<LivingEntity> resolveTargets(LivingEntity owner, @Nullable LivingEntity triggerTarget) {
        return resolveTargets(owner, triggerTarget, null);
    }

    /** 带几何圆心重载：供半径 Target 源使用，center 缺省回退 owner 位置。 */
    default List<LivingEntity> resolveTargets(LivingEntity owner, @Nullable LivingEntity triggerTarget,
                                             @Nullable Vec3 center) {
        TargetSelector t = target();
        return t != null ? t.resolve(owner, triggerTarget) : List.of(owner);
    }

    /**
     * 对本条目选中的每个目标执行 action（null 目标自动跳过）——
     * <b>效果落在「Condition 解析出的实体集」上</b>（缝二）。
     *
     * <p>当 {@link #conditionTarget()} 非 null 时，逐候选经条件缝解析：候选自身即「条件判定实体」，
     * <b>解析结果含该候选（非空）才落点</b>。模型本身不求布尔——「落在哪些实体上」由解析结果直接给出。
     * 这是「仅对范围内满足某状态的敌人施加」类光环（如「仅对未凋零敌人施加凋零」）的正确语义。
     * 此前条件仅在持有者身上判一次（{@link PeriodicDriver#drive} 的持有者门控），
     * 多目标场景下 gate 恒过、效果灌满所有目标，违背 per-target 语义。
     *
     * <p>求值基座由 {@code scope} 经 {@link StatContext#evalBase} 注入事件名 / 命中上下文，
     * 取代原先裸 {@code ExpressionContext.get(t)}——后者让 {@code event:} / {@code hit_state:}
     * 在 Stat 通道恒 0（旧缺陷）。
     *
     * <p>conditionTarget() 为 null 时回落旧行为：无条件遍历全部目标（持有者门控已处理
     * owner-gated 条目；FIRE 通道非周期条目也依赖此既有流转），字节级不变。
     */
    default void forEachTarget(StatEvalContext evalCtx, @Nullable StatContext scope,
                               Consumer<LivingEntity> action) {
        if (condition() != null) {
            for (LivingEntity t : StatEngine.resolveCondition(this, evalCtx, scope)) {
                if (t != null) action.accept(t);
            }
            return;
        }
        TargetSelector ct = conditionTarget();
        for (LivingEntity t : resolveTargets(evalCtx.owner(), evalCtx.triggerTarget(), evalCtx.center())) {
            if (t == null) continue;
            if (ct != null && !StatEngine.conditionsHold(this, evalCtx, scope, t)) continue;
            action.accept(t);
        }
    }

    // ---------------------------------------------------------------- 构造便捷（静态工厂）

    // ---------------------------------------------------------------- 内联对象 → 引用 的登记
    //
    // 选择器 / 效果两段外置为引用后，{@link Stat} 只持有 {@code Identifier}；而 wiring / 便捷构造处
    // 拿到的往往是刚 new 出来的内联对象（无具名 id）。这里就地登记到 {@code LoadEvent} 的两段加载表
    // 并分配一个运行时内部 id（按对象 identity 去重，非内容寻址、不做哈希），返回引用。
    // 这些条目只在内存中使用、不落盘，故 id 无需跨会话稳定。
    //
    // 注意：接口字段隐含 public static final，不可加 private（Java 不允许），故此处无修饰符。
    // 内容寻址：相等（equals/hashCode）的 effect 共享同一 anon 引用，使「同内容不同对象实例」
    // 的条目（如 nether / hot 两处同参复用的 livingmod_resist_fire_20）得到一致 effectRef，
    // 内容签名去重才能正确归并。未覆写 equals 的子类回退到身份相等，语义无损。
    static final Map<StatEffect, Identifier> ANON_EFFECT_IDS = new java.util.HashMap<>();
    static final Map<StatTargets, Identifier> ANON_TARGET_IDS = new java.util.IdentityHashMap<>();
    /**
     * 自增序号。刻意不用裸 {@code int} 计数器：接口字段隐含 {@code public static final}，
     * 可自增写法 {@code ++anonSeq} 会直接编译失败；{@code AtomicInteger} 既绕开该限制又天然线程安全。
     */
    static final java.util.concurrent.atomic.AtomicInteger ANON_SEQ = new java.util.concurrent.atomic.AtomicInteger();

    private static Identifier anonId(String dir, String type) {
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID,
                "stat/" + dir + "/inline_" + type + "_" + ANON_SEQ.incrementAndGet());
    }

    /** 登记内联 effect，返回其引用。 */
    static Identifier internEffect(StatEffect e) {
        if (e == null) return null;
        Identifier id = ANON_EFFECT_IDS.get(e);
        if (id == null) {
            id = anonId("effect", e.type());
            ANON_EFFECT_IDS.put(e, id);
            LoadEvent.STAT_EFFECT_MAP.put(id, e);
        }
        return id;
    }

    /** 登记内联选择器，返回其引用。 */
    static Identifier internTargets(StatTargets t) {
        if (t == null) return null;
        Identifier id = ANON_TARGET_IDS.get(t);
        if (id == null) {
            id = anonId("targets", "inline");
            ANON_TARGET_IDS.put(t, id);
            LoadEvent.STAT_TARGET_MAP.put(id, t);
        }
        return id;
    }

    /** 登记内联 condition，返回其引用（mirror internEffect / internTargets：身份去重，非内容寻址落盘）。 */
    static final Map<StatCondition, Identifier> ANON_CONDITION_IDS = new java.util.HashMap<>();
    static Identifier internCondition(@Nullable StatCondition c) {
        if (c == null) return null;
        Identifier id = ANON_CONDITION_IDS.get(c);
        if (id == null) {
            id = anonId("condition", c.getClass().getSimpleName());
            ANON_CONDITION_IDS.put(c, id);
            LoadEvent.STAT_CONDITION_MAP.put(id, c);
        }
        return id;
    }

    /** 仅数值 + 效果（无条件、选择器为空）。 */
    static StatRead of(ExpressionCodec.Value amount, StatEffect effect) {
        return of(amount, List.of(), effect);
    }

    /** 数值 + 条件 + 效果。 */
    static StatRead of(ExpressionCodec.Value amount, List<ValueExpr> conditions, StatEffect effect) {
        return of(amount, conditions, null, effect);
    }

    /**
     * 数值 + 条件 + 选择器 + 效果（内联条件段）。
     *
     * <p>条件直接以 {@code List<ValueExpr>} 传入，在此经 {@link ValueExpr#of} 压成<b>单个</b>
     * 复式节点（空 → 空组 / 单条 → 该节点 / 多条 → 与组），故 {@code Stat} 的条件段是一个字段而非 ref 列表。
     *
     * <p>选择器可为 {@code null}：此处放宽仅为便捷构造方便；产物侧所有 Stat 均显式指定 targets
     * （由 {@code StatTargetGen.targets(id)} 给默认值）。
     */
    static StatRead of(ExpressionCodec.Value amount, List<ValueExpr> conditions,
                   StatTargets targets, StatEffect effect) {
        return new StatRead(amount, conditions, targets, effect);
    }

    // ---------------------------------------------------------------- 副本构造（默认内联）

    /** 返回替换数值后的副本（条件 / 选择器 / 效果 / 外溢与转换作用域原样保留）。 */
    default StatEntry withAmount(ExpressionCodec.Value newAmount) {
        if (this instanceof StatRead s) {
            return new StatRead(newAmount, s.conditionsNode(), s.targetsRef(), s.effectRef(), s.params(),
                    s.spillTo(), s.convertTo()).withDuration(s.duration()).withInterval(s.interval())
                    .withCondition(s.condition());
        }
        return of(newAmount, conditions(), targets(), effect())
                .withSpillTo(spillTo()).withConvertTo(convertTo()).withCondition(condition());
    }

    /**
     * 返回替换外溢作用域后的副本（数值 / 条件 / 选择器 / 效果不变）。默认实现返回自身——
     * 无外溢语义的条目类型（引用型在 {@link StatRead} 覆写为解析后内联、组合型在 {@link StatGroup}
     * 逐成员改写）不受影响。
     */
    default StatEntry withSpillTo(GateExpr spill) {
        return this;
    }

    /**
     * 返回替换转换作用域后的副本（数值 / 条件 / 选择器 / 效果 / 外溢作用域不变）。默认实现返回自身——
     * 无转换语义的条目类型（引用型在 {@link StatRead} 覆写为解析后内联、组合型在 {@link StatGroup}
     * 逐成员改写）不受影响。
     *
     * <p>置入非 {@link GateExpr#NEVER} 的转换作用域时，若本条目已声明外溢则抛错：
     * 「并集」与「替代」两种门控语义不可合成一个表达式（见 {@link Stat} 规范构造的互斥校验）。
     */
    default StatEntry withConvertTo(GateExpr to) {
        return this;
    }

    /**
     * 按倍率生成数值缩放后的副本：常量分支直接乘（仍保持常量，零表达式开销），
     * 表达式分支用 {@code ($) * mult} 包裹（Multiply 节点），运行时由 Stat 自动求值。
     * 用于药水效果按等级 {@code (amplifier + 1)} 缩放等场景——
     * 缩放发生在「应用」期（事件读取等级后），存入 StatStore 的已是倍率后的 StatEntry。
     */
    default StatEntry scaled(double multiplier) {
        Either<Double, MathExpression> v = amount().value();
        if (v.left().isPresent()) {
            return withAmount(ExpressionCodec.of(v.left().get() * multiplier));
        }
        MathExpression expr = v.right().get();
        return withAmount(new ExpressionCodec.Value(Either.right(
                new ExpressionCodec.BinaryOperation.Multiply(expr, new ExpressionCodec.Constant(multiplier)))));
    }

    /** 返回替换效果后的副本（数值 / 条件 / 选择器 / 外溢与转换作用域不变）。 */
    default StatEntry withEffect(StatEffect newEffect) {
        if (this instanceof StatRead s) {
            return new StatRead(s.amount(), s.conditionsNode(), s.targetsRef(), internEffect(newEffect),
                    s.params(), s.spillTo(), s.convertTo()).withDuration(s.duration()).withInterval(s.interval())
                    .withCondition(s.condition());
        }
        return of(amount(), conditions(), targets(), newEffect)
                .withSpillTo(spillTo()).withConvertTo(convertTo()).withCondition(condition());
    }

    /**
     * 返回替换三段选择器后的副本（内联形态；数值 / 条件 / 效果 / 有效参数不变）。
     *
     * <p>用途：<b>作用目标重定向</b>——把既有条目的收件人整体改到别处，而不必重新构造条目。
     * 典型场景「已装备 X 槽位的全部加成改为作用于你的随从」：条目原样保留（数值 / 条件 / 效果
     * 全部不变），仅把 {@code targets} 换成 {@code new StatTargetsInline(<目标选择器>, null, null)}
     * （原举例的 {@code TargetSelector.MINIONS} 枚举值已删除；现行 {@code TargetSelector.Source} 仅
     * {@code SELF} / {@code TRIGGER_TARGET}，半径友方筛选另由 {@code RadiusCondition} 承担），下游由 StatEngine 的
     * 「master → 召唤体」作用域维聚合到随从实体上。
     *
     * <p>与 {@link #withAmount}/{@link #withEffect} 同源：默认降级为内联 {@link Stat}。
     * 注意 {@link StatRead} 与 {@link StatGroup} 均已各自覆写本方法（分别透传 conditionRef
     * / 递归改写成员），本默认实现仅作其余实现的兜底路径。
     */
    default StatEntry withTargets(StatTargets newTargets) {
        return of(amount(), conditions(), newTargets, effect())
                .withSpillTo(spillTo()).withConvertTo(convertTo()).withCondition(condition());
    }

    /** 返回替换实体获取条件后的副本（条件经 intern 登记为命名引用，mirror effect/targets 的内联 intern 机制）。 */
    default StatEntry withCondition(@Nullable StatCondition newCondition) {
        return withConditionRef(internCondition(newCondition));
    }

    /** 以命名引用替换条件段后的副本（默认自身；StatRead 覆写为换 ref）。 */
    default StatEntry withConditionRef(@Nullable Identifier condition) {
        return this;
    }
}
