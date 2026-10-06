package com.cimdy.coe.data.codec.support.math;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.skill.MetaEnergyValue;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.DamageContext;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.cimdy.coe.data.codec.support.math.context.node.ExprValue;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.combat.AttributeEvent;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;

public class ExpressionContext {
    /**
     * 实体级 Context 单例缓存：每个 LivingEntity 对应一个全局 ExpressionContext，
     * 由其事件回调实时写入、math 求值实时读取。用 WeakHashMap 随实体 GC 自动回收，无内存泄漏。
     */
    private static final Map<LivingEntity, ExpressionContext> ENTITY_SINGLETONS = new WeakHashMap<>();

    /**
     * 计时器 / 槽位实体的统一最大存活 tick（§7：20 秒 = 400 tick）。
     * 超过即从共享 Map（{@link #store}）移除，读取回落到 math 的 {@code default} 兜底。
     */
    private static final int TIMER_MAX_LIFETIME_TICKS = 20 * 20;

    /**
     * 计时器无限上限：用于「持续型姿态」（如招架 Parry）——
     * 该 timer 仅在进入时写入一次、无续期机制，若用默认 20s 上限会在长时间保持后被 {@link #tickStore} 移除，
     * 导致姿态判定失效。以 {@link Integer#MAX_VALUE} 作为存活上限，实际游戏内 tick 不会逼近，等效「不过期」。
     */
    private static final int TIMER_INFINITE = Integer.MAX_VALUE;

    /**
     * 全局获取方法：仅需传入实体即可取得其 Context 单例（不存在则创建并缓存）。
     * 事件回调写入实时信息、math 求值从此读取，统一了事件驱动的实体状态访问入口
     * （SkillContext 实时状态仍经其委托读取，见 {@link #forSkill}）。
     */
    public static ExpressionContext get(LivingEntity entity) {
        // 实体构造期（LivingEntity.<init> → getMaxHealth → getAttributeValue → StatEngine.fold）：
        // entity.getId() 尚未分配，WeakHashMap 键的 hashCode() 会抛 IllegalStateException
        // （"Tried to access entity ID before ID assignment"），导致登录创建玩家崩溃。
        // 此时 stat 修正本无意义（装备 / 附件未装配）→ 返回临时非缓存上下文（不入表、不触发实体键哈希）。
        try {
            return ENTITY_SINGLETONS.computeIfAbsent(entity,
                    e -> new ExpressionContext.Builder().entity(e).eventState(Event.get(e)).build());
        } catch (IllegalStateException idNotAssigned) {
            return new ExpressionContext.Builder().entity(entity).build();
        }
    }

    /**
     * 数据基站获取（任意实体）：LivingEntity 走单例缓存 {@link #get}；
     * 非生物实体（图腾 / 云体 / 箭矢等）或实体为 null 时以 level 构造轻量上下文。
     * 使实体侧检测（索敌 / 范围扫描）也能统一经 Context 回调。
     */
    public static ExpressionContext of(Level level, Entity entity) {
        if (entity instanceof LivingEntity living) return get(living);
        return builder().level(level).entity(entity).build();
    }

    /**
     * 记录一次回复数值（请求量 / 实际量 / 溢出量）到实体单例，供 Stat 条件与数学节点检测
     * （实现「回复量 > X 触发」「生命偷取转化」等判定）。委托 {@link Event#recordHeal}。
     */
    public static void recordHeal(LivingEntity living, String resource, float requested, float actual, float overflow) {
        Event.recordHeal(living, resource, requested, actual, overflow);
    }

    /**
     * 记录一次消耗数值（请求量 / 实际扣减 / 未能扣减部分）到实体单例，与 {@link #recordHeal} 对称，
     * 供「消耗 X 资源时触发」「怒气消耗回血」等判定。委托 {@link Event#recordSpend}。
     */
    public static void recordSpend(LivingEntity living, String resource, float requested, float actual, float missing) {
        Event.recordSpend(living, resource, requested, actual, missing);
    }

    /**
     * 技能判定基站组装：以施法者单例为底座（实体 / Level / 计时 / 槽位子站），
     * 叠加技能子站（{@link SkillContext} 实时状态）与伤害子站（阶段实例的 {@link DamageContext}，可缺省）。
     * 返回求值副本，不污染施法者单例——并发事件各自组装、互不串联。
     * 阶段激活 / 事件过滤 / 条件分支等一切判定统一经此组装后 {@link #test(MathExpression)} 完成。
     */
    public static ExpressionContext forSkill(SkillInstance skillInstance, SkillPhaseInstance phaseInstance) {
        SkillContext sc = skillInstance.getContext();
        LivingEntity caster = sc.getCaster();
        ExpressionContext base = get(caster);
        DamageContext dc = phaseInstance != null ? phaseInstance.getDamageContext() : null;
        Map<String, ExpressionCodec.Value> vars = sc.getVariables();
        ExpressionContext built = new Builder()
                .level(caster.level())
                .position(caster.position())
                .skillContext(sc)
                .entity(caster)
                .eventState(Event.get(caster))
                .evaluating(base.evaluating)
                .statScopeStack(base.statScopeStack)
                .damage(dc)
                .build();
        return vars == null || vars.isEmpty() ? built : built.withVariables(vars);
    }

    /**
     * 技能求值基座（对齐 {@link #forSkill} 的核心字段，无需阶段实例）：以施法者单例为底座，
     * 注入 entity / eventState / statScopeStack / evaluating，使技能动作 amount 里的
     * {@code attribute:} / {@code recently:} / {@code count:} 等节点真实可读。
     * 供 {@link ExpressionCodec.Value#getValue(SkillContext)} 使用；{@code damage} 子站保持空
     * （调用点无阶段实例，skill.level / charge 等经 skillContext 委托读取不受影响）。
     */
    public static ExpressionContext forSkillContext(SkillContext context) {
        LivingEntity caster = context.getCaster();
        if (caster == null) {
            Map<String, ExpressionCodec.Value> vars = context.getVariables();
            ExpressionContext built = builder().skillContext(context).build();
            return vars == null || vars.isEmpty() ? built : built.withVariables(vars);
        }
        // 技能参数槽（$name）：由 SkillCaster 从 Skill.params() 注入，使预设模板里的
        // $reserve / $radius 等槽位在本次施放中真实求值（SkillRead 模型的运行期落点）。
        // base = 施法者单例，提供 evaluating / statScopeStack 底座（对齐 forSkill）。
        ExpressionContext base = get(caster);
        Map<String, ExpressionCodec.Value> vars = context.getVariables();
        ExpressionContext built = new Builder()
                .level(caster.level())
                .position(caster.position())
                .skillContext(context)
                .entity(caster)
                .eventState(Event.get(caster))
                .evaluating(base.evaluating)
                .statScopeStack(base.statScopeStack)
                .build();
        return vars == null || vars.isEmpty() ? built : built.withVariables(vars);
    }

    /**
     * 统一判定入口：math 表达式在本基站上求值，<b>非零即真</b>；空表达式恒通过。
     * 「获取」由表达式内读值节点（属性 / 效果 / 技能状态 / 伤害子站 / 装备 / 计时…）完成，
     * 「求值」由 math 比较节点完成，本方法只集中所有子站信息给出最终布尔。
     * <p>
     * 判定口径为「非零」（{@link ExprValue#truth(double)}），<b>不是</b> {@code >= 1}：
     * 后者会把 {@code 0.5} 这类有效小数、以及负数误判为假，且与 math 层
     * {@code Logical} / {@code Not} 节点自身的 {@code != 0} 判定相矛盾。
     * math 比较节点产出严格 {@code 1.0 / 0.0}，故常规比较条件的行为不变。
     */
    public boolean test(MathExpression expression) {
        return expression == null || ExprValue.truth(expression.evaluate(this));
    }

    // ---- 数据基站：实体获取统一回调（获取实体与获取数值拆分，见 EntityQuery）----

    /**
     * 统一实体获取回调：调用方描述「要哪些实体」（{@link EntityQuery}），
     * Context 解析 level 并执行查询，把实体集合二次分发回调用方；
     * 调用方随后自行完成「获取数值」（读属性 / 点燃 ON_HIT / 施加效果等）。
     * level 无法解析时返回空列表。
     */
    public <T extends Entity> List<T> queryEntities(EntityQuery<T> query) {
        return getResolvedLevel().map(query::collect).orElseGet(ArrayList::new);
    }

    /** 统一实体获取回调（单实体版）：取查询结果第一个（配合 nearestTo 即「就近索敌」）。 */
    public <T extends Entity> Optional<T> queryEntity(EntityQuery<T> query) {
        return getResolvedLevel().flatMap(query::first);
    }

    private final Level level;
    private final Vec3 position;
    private final SkillContext skillContext;
    /** 伤害子站：本次命中的伤害数值与结算状态（{@code damage.*} 读值节点数据源），可为 null（非命中流程）。 */
    private final DamageContext damageContext;
    /** 命中子站：本次命中的实体/方块/位置等信息（{@code hit.*} 读值节点与 HitTest 判定数据源），可为 null（非命中流程）。 */
    private final HitContext hitContext;
    private final Entity entity;
    /** 实体近期事件 + 坐标历史（来自 {@link Event}，瞬态缓存，不持久化）。 */
    private final Event.State eventState;
    /** 当前触发事件名（元事件经 SkillEventBus 发布，求值前由 MetaSkillAction / EventListenPhase 注入），供 {@code event:<name>} 节点判定。 */
    private final String currentEventName;
    /** 当前触发事件附带的作用域标签（如异常类型 chill/ignite），供 {@code event_tag:<name>:<tag>} 节点判定，
     * 实现「同一通用事件名（caused_ailment）+ 具体类型标签」的收敛判定。 */
    private final List<String> currentEventTags;
    private final Map<String, ExpressionCodec.Value> data = new HashMap<>();

    /**
     * 共享 Map：同时承载「槽位实体」与「事件 timer」（§7）。
     * 键为槽位名（如 hit/killed/attacker）或 timer 名（如 time_kill/time_crit）；
     * 值为带经过 tick 的条目，每 tick 由 {@link #tickStore()} 递增，超 {@link #TIMER_MAX_LIFETIME_TICKS} 移除。
     * 注意：求值副本（{@link #withVariables}）不复制此 Map，读取始终走实体单例（{@link #store()}），
     * 以保证 Read 内嵌套表达式也能访问同一实体的实时 timer。
     */
    private final Map<String, ScopedEntry> store = new HashMap<>();

    /**
     * 元技能能量存储（POE2 机制）：每（实体，元技能宝石）一个能量计数。
     * 事件满足触发条件时累加、满阈值后消耗能量触发链接技能并重置。
     * 独立于 {@link #store}（不随 {@link #tickStore} 递增/移除，能量保持至触发），
     * 由 {@link #energyStore()} 反查实体单例共享。
     */
    private final Map<String, Integer> metaEnergy = new HashMap<>();

    // ---- 事件按需订阅缓存：记录本实体 StatStore 上次对全局订阅表的贡献，
    // 供 AttributeEvent tick 对齐点做增量 diff（增订/退订 + presence 开关）----
    private Set<String> subscribedEvents = Set.of();
    private boolean presenceSubscribed = false;

    /** 本实体上次已订阅的事件名集合（tick 对齐用）。 */
    public Set<String> getSubscribedEvents() {
        return subscribedEvents;
    }

    /** 更新本实体已订阅集合（tick 对齐调用）。 */
    public void setSubscribedEvents(Set<String> subscribedEvents) {
        this.subscribedEvents = subscribedEvents == null ? Set.of() : Set.copyOf(subscribedEvents);
    }

    /** 本实体上次是否已置位 presence 需求（tick 对齐用）。 */
    public boolean isPresenceSubscribed() {
        return presenceSubscribed;
    }

    /** 更新本实体 presence 需求位（tick 对齐调用）。 */
    public void setPresenceSubscribed(boolean presenceSubscribed) {
        this.presenceSubscribed = presenceSubscribed;
    }

    // 记录当前求值链中正在计算的 Read 表达式 ID，用于循环引用检测
    private final Set<Identifier> evaluating;
    // 事件作用域栈（stat 系统）：读属性时按栈顶作用域即时计算加成，避免全局修饰符并发串联。
    // 以引用形式在求值副本间共享，确保嵌套 math 表达式仍位于同一作用域。
    private final Deque<StatContext> statScopeStack;

    /** 作用域栈版本号：每次 push/pop 自增，作为 stat 结果缓存的失效标记（O(1) 失效，无需清 Map）。 */
    private int statScopeVersion = 0;
    /** 读时 stat 结果缓存：key=属性，value 携带计算时刻的作用域版本；版本不符即视为失效重算。 */
    private final Map<Holder<Attribute>, StatCacheEntry> statCache = new HashMap<>();
    /**
     * 读时 stat 结果缓存（仅增量语义专用）：key=属性。
     * <p>
     * 与 {@link #statCache} <b>必须分开</b>：{@code statCache} 存的是「基数 + 增量」折叠值（供 mixin 读属性），
     * 本 Map 存的是「只含 stat 增量」的折叠值（供把属性当缩放系数 / 概率 / 额外层数用的调用方）。
     * 两者对同一属性在同一作用域版本 + 数据版本下会得出不同的数，共用一个条目必然互相污染。
     */
    private final Map<Holder<Attribute>, StatCacheEntry> statDeltaCache = new HashMap<>();
    /** 各属性最近一次广播给 UI 的值，用于事件驱动更新时跳过无变化的重广播，降低开销。 */
    private final Map<Holder<Attribute>, Double> statLastBroadcast = new HashMap<>();

    // ---- 实体实时技能状态（唯一存储：实体单例；求值副本不再拷贝快照）----
    // 由输入/施法事件回调经 SkillContext 委托写入单例字段；
    // 副本读取时优先经 skillContext 委托（其内部回落到实体单例），消除每次组装副本的 7 值重复缓存。
    private double skillLevel = 1;
    private double quality = 0;
    private int pressTime = 0;
    private float chargeAmount = 0;
    private boolean isPress = false;
    private boolean isJustReleased = false;
    private int bind = -1;

    private ExpressionContext(Builder builder) {
        this.level = builder.level;
        this.position = builder.position;
        this.skillContext = builder.skillContext;
        this.damageContext = builder.damageContext;
        this.hitContext = builder.hitContext;
        this.entity = builder.entity;
        this.eventState = builder.eventState != null ? builder.eventState : Event.State.EMPTY;
        this.currentEventName = builder.currentEventName;
        this.currentEventTags = builder.currentEventTags != null ? builder.currentEventTags : List.of();
        this.evaluating = builder.evaluating != null ? builder.evaluating : new HashSet<>();
        this.statScopeStack = builder.statScopeStack != null ? builder.statScopeStack : new ArrayDeque<>();
    }

    public ExpressionCodec.Value getInjectedVar(String name) {
        return data.get(name);
    }

    private static Level getRelevantLevel(Level level, Entity entity) {
        return level != null ? level : entity != null ? entity.level() : null;
    }

    // ---- 循环引用检测 ----
    public boolean isEvaluating(Identifier id) {
        return evaluating.contains(id);
    }

    public void pushEvaluation(Identifier id) {
        evaluating.add(id);
    }

    public void popEvaluation(Identifier id) {
        evaluating.remove(id);
    }

    /** 解析出的实体（供 ValueExpr 读取；可能非 LivingEntity）。 */
    public Entity getEntity() {
        return entity;
    }

    /** 实体近期事件 + 坐标历史（来自 {@link Event}，瞬态缓存）。 */
    public Event.State event() {
        return eventState;
    }

    /** 当前触发事件名（元事件名，如 on_crit / on_ignite），供 {@code event:<name>} 节点判定；非事件流程为 null。 */
    public String getCurrentEventName() {
        return currentEventName;
    }

    /** 当前触发事件附带标签（如异常类型 chill/ignite），供 {@code event_tag:<name>:<tag>} 节点判定；非事件流程为空。 */
    public List<String> getCurrentEventTags() {
        return currentEventTags;
    }

    /** 伤害子站（{@code damage.*} 读值节点数据源）；非命中流程为 null。 */
    public DamageContext getDamageContext() {
        return damageContext;
    }

    /** 命中子站（{@code hit.*} 读值节点与 HitTest 判定数据源）；非命中流程为 null。 */
    public HitContext getHitContext() {
        return hitContext;
    }

    /** 回退链 level → entity.level() 解析出的 level（time.day 等用）。 */
    public Optional<Level> getResolvedLevel() {
        return Optional.ofNullable(getRelevantLevel(level, entity));
    }

    // 候选切换统一走 ValueScope.swap 指针交换（方案四），root 数据基站在候选循环内固定不重建。

    public Optional<Vec3> getPosition() {
        return Optional.ofNullable(position);
    }

    // ---- 计时器 / 槽位实体共享 Map（§7）----

    /**
     * 共享 Map 条目：同时承载槽位实体与 timer。
     * {@code entity} 非空表示槽位实体；{@code entity} 为 null 表示纯 timer（仅 elapsed 计时）。
     * {@code elapsed} 自写入（{@link #setSlot}/{@link #refreshTimer}）起每 tick 递增，达到 {@code maxTicks} 移除。
     */
    private record ScopedEntry(LivingEntity entity, int elapsed, int maxTicks) {
        ScopedEntry tick() {
            return new ScopedEntry(entity, elapsed + 1, maxTicks);
        }
    }

    /**
     * 解析实体单例的存储 Map：求值副本（{@link #withVariables}）通过 entity 反查单例，
     * 使嵌套 Read 表达式也能读到同一实体的实时 timer；无实体场景退回本地空 store。
     */
    private Map<String, ScopedEntry> store() {
        if (this.entity instanceof LivingEntity living) {
            return ExpressionContext.get(living).store;
        }
        return this.store;
    }

    /** 写入 / 刷新槽位实体（elapsed 归零）。 */
    public void setSlot(String name, LivingEntity e) {
        store().put(name, new ScopedEntry(e, 0, TIMER_MAX_LIFETIME_TICKS));
    }

    /** 刷新 timer：事件发生归零 elapsed（统一 20s 存活上限）。 */
    public void refreshTimer(String name) {
        refreshTimer(name, TIMER_MAX_LIFETIME_TICKS);
    }

    /** 刷新 timer 并覆盖存活上限（非窗口宽度，仅控制 Map 留存时长）。 */
    public void refreshTimer(String name, int maxTicks) {
        store().put(name, new ScopedEntry(null, 0, maxTicks));
    }

    /** 以无限上限刷新 timer（仅用于持续型姿态，如招架 Parry，详见 {@link #TIMER_INFINITE}）。 */
    public void refreshTimerInfinite(String name) {
        store().put(name, new ScopedEntry(null, 0, TIMER_INFINITE));
    }

    /** 清除 timer（如松开右键结束招架姿态时移除 PARRY，避免残留误判）。 */
    public void clearTimer(String name) {
        store().remove(name);
    }

    /** 每 tick 递增所有条目，超过存活上限移除。 */
    public void tickStore() {
        Map<String, ScopedEntry> s = store();
        if (s.isEmpty()) return;
        s.replaceAll((k, v) -> v.tick());
        s.values().removeIf(v -> v.elapsed >= v.maxTicks);
    }

    /** 读取 timer 经过的 tick；不存在 / 已移除返回 {@code def}。 */
    public double getTimerElapsed(String name, double def) {
        ScopedEntry e = store().get(name);
        return e != null ? e.elapsed : def;
    }

    // ---- 元技能能量（POE2 能量积累机制）----

    private Map<String, Integer> energyStore() {
        if (this.entity instanceof LivingEntity living) {
            return ExpressionContext.get(living).metaEnergy;
        }
        return this.metaEnergy;
    }

    /** 读取元技能能量（无则 0）；供 {@code MetaEnergyValue} 节点表达式判定。 */
    public int getMetaEnergy(String key) {
        return energyStore().getOrDefault(key, 0);
    }

    /** 判断 timer 是否仍活跃（已写入且未超存活上限）；不存在 / 已移除返回 false（供 isFight 等判定）。 */
    public boolean isTimerActive(String name) {
        ScopedEntry e = store().get(name);
        return e != null && e.elapsed() < e.maxTicks();
    }

    /** 读取槽位实体（无则返回空）。 */
    public Optional<LivingEntity> getSlotEntity(String name) {
        ScopedEntry e = store().get(name);
        return e != null ? Optional.ofNullable(e.entity) : Optional.empty();
    }

    // ---- 实体实时技能状态访问（事件回调写入单例 / math 读取）----
    // 读取规则：带 skillContext 的求值副本经其委托（内部回落实体单例，bind 为每次施放的最终值）；
    // 无 skillContext 时读自身字段（即实体单例存储本身，或轻量上下文的默认值）。单一数据源，无副本缓存。
    public double getSkillLevel() { return skillContext != null ? skillContext.getSkillLevel() : skillLevel; }
    public void setSkillLevel(double v) { this.skillLevel = v; }
    public double getQuality() { return skillContext != null ? skillContext.getQuality() : quality; }
    public void setQuality(double v) { this.quality = v; }
    public int getPressTime() { return skillContext != null ? skillContext.getPressTime() : pressTime; }
    public void setPressTime(int v) { this.pressTime = v; }
    public float getChargeAmount() { return skillContext != null ? skillContext.getChargeAmount() : chargeAmount; }
    public void setChargeAmount(float v) { this.chargeAmount = v; }
    public boolean isPress() { return skillContext != null ? skillContext.isPress() : isPress; }
    public void setPress(boolean v) { this.isPress = v; }
    public boolean isJustReleased() { return skillContext != null ? skillContext.isJustReleased() : isJustReleased; }
    public void setJustReleased(boolean v) { this.isJustReleased = v; }
    public int getBind() { return skillContext != null ? skillContext.getBind() : bind; }

    public static Builder builder() {
        return new Builder();
    }

    // ---- 事件作用域栈（stat 系统：读时按事件作用域即时计算，避免全局修饰符串联）----
    public void pushStatScope(StatContext context) {
        statScopeStack.push(context);
        statScopeVersion++;   // 作用域变化：使缓存失效（版本不符即重算）
    }

    public void popStatScope() {
        if (!statScopeStack.isEmpty()) statScopeStack.pop();
        statScopeVersion++;   // 作用域变化：使缓存失效
    }

    public StatContext currentStatContext() {
        return statScopeStack.peek() != null ? statScopeStack.peek() : StatContext.EMPTY;
    }

    /** 读取缓存的 stat 折叠结果；仅当版本与当前作用域栈一致、且数据版本与当前 StatStore 一致时有效（作用域栈版本 + StatStore 数据版本双键失效）。 */
    public Double getCachedStat(Holder<Attribute> attr, int dataVersion) {
        StatCacheEntry e = statCache.get(attr);
        return e != null && e.version() == statScopeVersion && e.dataVersion() == dataVersion ? e.value() : null;
    }

    /** 写入本次作用域下的 stat 折叠结果（携带当前作用域版本 + 数据版本）。 */
    public void putCachedStat(Holder<Attribute> attr, double value, int dataVersion) {
        statCache.put(attr, new StatCacheEntry(value, statScopeVersion, dataVersion));
    }

    /**
     * 读取「仅 stat 增量」语义的折叠缓存；失效条件同 {@link #getCachedStat}，但条目独立存放，
     * 不会读到「基数 + 增量」语义的 mixin 读值（反之亦然）。
     */
    public Double getCachedStatDelta(Holder<Attribute> attr, int dataVersion) {
        StatCacheEntry e = statDeltaCache.get(attr);
        return e != null && e.version() == statScopeVersion && e.dataVersion() == dataVersion ? e.value() : null;
    }

    /** 写入「仅 stat 增量」语义的折叠结果（携带当前作用域版本 + 数据版本）。 */
    public void putCachedStatDelta(Holder<Attribute> attr, double value, int dataVersion) {
        statDeltaCache.put(attr, new StatCacheEntry(value, statScopeVersion, dataVersion));
    }

    /** 读取该属性最近一次广播给 UI 的值（无则 null）。 */
    public Double getLastBroadcastStat(Holder<Attribute> attr) {
        return statLastBroadcast.get(attr);
    }

    /** 记录该属性最近一次广播给 UI 的值。 */
    public void setLastBroadcastStat(Holder<Attribute> attr, double value) {
        statLastBroadcast.put(attr, value);
    }

    /** 读时 stat 缓存条目：携带计算时刻的作用域栈版本 + StatStore 数据版本（作用域栈版本 + StatStore 数据版本双键失效）。 */
    private record StatCacheEntry(double value, int version, int dataVersion) {}

    // 复制全部字段到新 Builder，供 withVariables/withEvent 增量覆写，避免新增字段漏改副本组装点
    private Builder copyBuilder() {
        return new Builder()
                .level(this.level)
                .position(this.position)
                .skillContext(this.skillContext)
                .damage(this.damageContext)
                .hitContext(this.hitContext)
                .entity(this.entity)
                .eventState(this.eventState)
                .eventName(this.currentEventName)
                .eventTags(this.currentEventTags)
                .evaluating(this.evaluating)
                .statScopeStack(this.statScopeStack);
    }

    // 返回带新变量的副本，不修改原 context（避免可变状态泄漏）
    public ExpressionContext withVariables(Map<String, ExpressionCodec.Value> value) {
        ExpressionContext copy = copyBuilder().build();
        copy.data.putAll(this.data);   // 复制已有变量
        copy.data.putAll(value);        // 叠加新变量
        return copy;
    }

    /** 返回带当前触发事件名 + 标签的新上下文副本（供 {@code event:<name>} / {@code event_tag:<name>:<tag>} 判定）。 */
    public ExpressionContext withEvent(String eventName, List<String> tags) {
        return copyBuilder().eventName(eventName).eventTags(tags).build();
    }

    /** 返回带命中上下文的新上下文副本（供 {@code hit_state:*} 节点判定；与 {@link #withEvent} 可叠加）。 */
    public ExpressionContext withHit(HitContext hitContext) {
        return copyBuilder().hitContext(hitContext).build();
    }

    public static class Builder {
        private Level level;
        private Vec3 position;
        private SkillContext skillContext;
        private DamageContext damageContext;
        private HitContext hitContext;
        private Entity entity;
        private Event.State eventState;
        private String currentEventName;
        private List<String> currentEventTags;
        private Set<Identifier> evaluating;
        private Deque<StatContext> statScopeStack;

        public Builder level(Level level) {
            this.level = level;
            return this;
        }

        public Builder position(Vec3 position) {
            this.position = position;
            return this;
        }

        public Builder skillContext(SkillContext context) {
            this.skillContext = context;
            return this;
        }

        public Builder damage(DamageContext damageContext) {
            this.damageContext = damageContext;
            return this;
        }

        public Builder hitContext(HitContext hitContext) {
            this.hitContext = hitContext;
            return this;
        }

        public Builder entity(Entity entity) {
            this.entity = entity;
            return this;
        }

        public Builder eventState(Event.State eventState) {
            this.eventState = eventState;
            return this;
        }

        public Builder eventName(String currentEventName) {
            this.currentEventName = currentEventName;
            return this;
        }

        public Builder eventTags(List<String> currentEventTags) {
            this.currentEventTags = currentEventTags;
            return this;
        }

        public Builder evaluating(Set<Identifier> evaluating) {
            this.evaluating = evaluating;
            return this;
        }

        public Builder statScopeStack(Deque<StatContext> statScopeStack) {
            this.statScopeStack = statScopeStack;
            return this;
        }

        public ExpressionContext build() {
            return new ExpressionContext(this);
        }
    }
}
