package com.cimdy.coe.event;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.MovementValue;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import org.jspecify.annotations.Nullable;

/**
 * 简单事件回调收集器 + 坐标历史（纯存储层）。
 * <p>
 * 收集「不依赖其他因素」的瞬时事件（喝药剂 / 变形 / 战吼 等）时间戳，并维护实体一段时间内的坐标采样，
 * 用于判定站定 / 移动 / 移动距离。所有数据均为瞬态缓存（实体单例，随 GC 回收），不持久化存储——
 * 即文档所述「近期不需要存储，将其缓存到 context」。
 * <p>
 * 每实体 {@link State} 通过 {@link ExpressionContext#event()} 传入 Context，
 * 供 {@code MovementValue} / {@code EventHistoryValue} 节点读取。
 * <p>
 * 写入入口统一为 {@link #record(LivingEntity, String)}：既写入本类缓存，也同步刷新
 * {@link ExpressionContext} 的 timer（使既有 {@code time_X} 读值保持可用）。
 * 按需订阅门控：仅被 {@link EventSubscription} 订阅的事件名（stat 引用或机制白名单）
 * 才记录；实体行为 → 本存储的订阅与转发统一由 {@link EntityOperationEvent} 负责
 * （含移动 / 坐标采样 / Presence 逻辑），本类不再承载事件订阅。
 */
public final class Event {

    /** 坐标历史保留窗口（tick）：默认 1 秒（20 tick），足够判定站定 / 移动。 */
    private static final int HISTORY_TICKS = 20;
    /** 坐标历史最大采样数（每 tick 1 条，窗口外自动淘汰）。 */
    private static final int HISTORY_CAP = HISTORY_TICKS + 1;
    /** 移动判定阈值（方块）：窗口内位移小于此值视为站定。 */
    private static final double MOVE_THRESHOLD = 0.05;
    /** 触发记录历史最大保留条数（每事件名）：超出淘汰最旧，限制缓存规模。 */
    private static final int MAX_TRIGGER_HISTORY = 64;
    /** 触发记录最长记忆窗口（tick）：超出窗口的触发记录将被清理（供窗口内计数 / 求和使用）。 */
    private static final int MAX_TRIGGER_MEMORY = 600;

    /** 每实体瞬态状态（WeakHashMap，实体 GC 后自动回收，无内存泄漏）。 */
    private static final Map<LivingEntity, State> STATES = new WeakHashMap<>();

    private Event() {}

    /** 取得（或创建）实体的事件状态。 */
    public static State get(LivingEntity e) {
        // 实体构造期（id 未分配）不能作为 WeakHashMap 键（hashCode→getId 抛 IllegalStateException），
        // 与 ExpressionContext.get 同因；此时无事件可记录，返回临时非缓存 State。
        try {
            return STATES.computeIfAbsent(e, k -> new State(e));
        } catch (IllegalStateException idNotAssigned) {
            return new State(e);
        }
    }

    /**
     * 记录某事件在当前 tick 发生（无回传值，等价于回传 1 次触发计数）。
     * 同时刷新 {@link ExpressionContext} timer，使 {@code time_X} 读值一并可用。
     * <p>按需订阅门控：仅当事件名被 {@link EventSubscription} 订阅（stat 引用或机制白名单）
     * 时才写入 State 队列与 timer；未订阅事件直接跳过。
     */
    public static void record(LivingEntity e, String name) {
        record(e, name, 1);
    }

    /**
     * 记录某事件在当前 tick 发生，并携带一个资源/类型标签（如吸取的资源类型 leech_health）。
     * 标签维度独立存储，供 {@code recently(name, tag, window)} 按「事件 + 标签」精确判定；
     * 同时刷新按事件名的 timer（保留 {@code time_X} / 粗粒度最近发生判定）。
     */
    public static void record(LivingEntity e, String name, String tag) {
        record(e, name, Set.of(tag));
    }

    /**
     * 记录某事件在当前 tick 发生，并携带一组资源/类型标签（如 leech + leech_health）。单记录多标签，避免队列被形态标签挤占。
     * <p><b>双写</b>：本重载既写 {@code taggedTriggers}（分维 {@code recently:<k>:<tag>}），
     * 也写 {@code triggers} 无标签队列（{@code count:<k>} / {@code recently:<k>}）。
     * 只写带标签队列会让「带标签记录、无标签读取」的接线恒返 0 且无报错
     * （典型：<code>Event.record(target, FORM_CHANGED, form.tags())</code> 配
     * {@code recently:form_changed}）——与 {@link GameEvent#recordTaggedHit} 的双写对齐。
     */
    public static void record(LivingEntity e, String name, Set<String> tags) {
        record(e, name, tags, null);
    }

    /**
     * 带「去重身份标签」的扩展重载（distinct 原语）：在 {@code tags} 基础上再并入
     * {@code extraSubjectTag}（形如 {@code skill_<id>} / {@code victim_<ailment>:<uuid>}，由调用方构造），
     * 仅写入计数队列（供 {@code EventHistoryValue.mode=distinct} 去重聚合），<b>不</b>并入 stat 触发作用域
     * （{@link GameEvent#fire} 用独立 {@code tags} 写 {@code StatEngine}）。
     * <p>用途：让「每使用一种不同 X」族（different Warcry / Spell）与「per Enemy you've Ignited」族
     * 能按<b>对象身份</b>去重计数，而非按触发次数。{@code extraSubjectTag} 为 null 时退化为无额外标签。
     */
    public static void record(LivingEntity e, String name, Set<String> tags, @Nullable String extraSubjectTag) {
        // 按需订阅门控：未被任何 stat 条目引用且非机制必需的事件，不再写入 State 队列与 timer
        if (!EventSubscription.isSubscribed(name)) return;
        Set<String> withSubject = extraSubjectTag == null ? tags : new HashSet<>(tags) { { add(extraSubjectTag); } };
        State state = get(e);
        state.record(name, withSubject);
        state.record(name, 1);
        ExpressionContext.get(e).refreshTimer(name);
    }

    /** 记录一次回复数值（请求量 / 实际量 / 溢出量），写入对应实体 State 供 Stat 条件与数学节点检测。 */
    public static void recordHeal(LivingEntity e, String resource, float requested, float actual, float overflow) {
        get(e).recordHeal(resource, requested, actual, overflow);
    }

    /** 记录一次消耗数值（请求量 / 实际扣减 / 未能扣减部分），与 {@link #recordHeal} 对称。 */
    public static void recordSpend(LivingEntity e, String resource, float requested, float actual, float missing) {
        get(e).recordSpend(resource, requested, actual, missing);
    }

    /** 记录一次承受伤害数值（实际扣减 / 溢出量），供 Stat 条件与数学节点读取（受害者侧，与 {@link #recordHeal} 对称）。 */
    public static void recordDamageTaken(LivingEntity e, String resource, float actual, float overflow) {
        get(e).recordDamageTaken(resource, actual, overflow);
    }

    /** 记录一次造成伤害数值（实际造成 / 溢出量），供 Stat 条件与数学节点读取（攻击者侧，与 {@link #recordDamageTaken} 对称）。 */
    public static void recordDamageCaused(LivingEntity e, String resource, float actual, float overflow) {
        get(e).recordDamageCaused(resource, actual, overflow);
    }

    /**
     * 记录某事件在当前 tick 发生，并回传一个 int 值（如消耗充能球数量）。
     * 供本类回调及其他事件类 / skill 系统统一调用；同时刷新 {@link ExpressionContext} timer，
     * 使 {@code time_X} 读值一并可用。触发回传值由 {@code Event.State} 缓存，供
     * {@code EventHistoryValue} 等节点按窗口计数 / 求和 / 取最大值 / 取最近值。
     * <p>按需订阅门控：仅当事件名被 {@link EventSubscription} 订阅时才写入
     * State 队列与 timer；未订阅事件直接跳过。
     */
    public static void record(LivingEntity e, String name, int value) {
        // 按需订阅门控：未被任何 stat 条目引用且非机制必需的事件，不再写入 State 队列与 timer
        if (!EventSubscription.isSubscribed(name)) return;
        State state = get(e);
        state.record(name, value);
        ExpressionContext.get(e).refreshTimer(name);
    }

    /**
     * 清空某事件名的触发历史（无标签队列），把该键的窗口累计语义收敛为「最近一次写入」语义。
     * <p>供「按<b>当次</b>消耗量结算」的写入端在每次真实消耗前调用（典型：连击消耗，见
     * {@link com.cimdy.coe.data.helper.hub.ResourceRegistry.ComboSave#consume}），
     * 随后紧跟一次 {@link #record} 写入本次数量，使 {@code sum:<key>} 恒等于<b>本次</b>数量，
     * 避免 4 秒 recently 窗口跨次累加造成的过度授予。
     * <p>与 {@link #record} 同样受按需订阅门控：未订阅的事件名直接跳过，不为其实体凭空创建 State。
     */
    public static void clear(LivingEntity e, String name) {
        if (!EventSubscription.isSubscribed(name)) return;
        get(e).clear(name);
    }

    /** 最近一次指定事件触发的回传值（如 {@code gain_frenzy} 的本次获得量）；无记录返回 0。 */
    public static int lastValue(LivingEntity e, String name) {
        return get(e).lastValue(name);
    }

        /** 单次事件触发记录：触发时刻 tick + 触发方回传的 int 值（如消耗充能球数量）。 */
        private record TriggerRecord(int tick, int value) {}

        /** 带标签的触发记录：触发时刻 tick + 一组资源/类型标签（如 leech + leech_health）。单记录多标签。 */
        private record TaggedTrigger(int tick, Set<String> tags) {}

    /** 每实体瞬态状态：近期事件时间戳 + 坐标历史。 */
    public static final class State {
        /** 空状态（非 LivingEntity / 未初始化时返回，避免 NPE）。 */
        public static final State EMPTY = new State();

        /** Presence 记忆时长（tick）：实体离开范围后保留进/出记录的最长时间。 */
        public static final int PRESENCE_MEMORY_TICKS = 200;

        /** 近期事件触发记录：key=事件名，value=该事件每次触发的 {@link TriggerRecord} 历史（按时间顺序，新记录追加到队尾）。 */
        private final Map<String, Deque<TriggerRecord>> triggers = new HashMap<>();
        /** 带标签的近期事件触发记录：key=事件名，value=该事件每次触发的 {@link TaggedTrigger}(含标签) 历史。 */
        private final Map<String, Deque<TaggedTrigger>> taggedTriggers = new HashMap<>();
        private final Deque<Sample> history = new ArrayDeque<>();
        /** 追踪者附近实体的进/出 Presence 记录：key=被追踪实体 UUID（瞬态缓存，不持久化）。 */
        private final Map<UUID, PresenceEntry> presence = new HashMap<>();
        /**
         * 属主实体（弱引用，避免 WeakHashMap 值 → 键强引用导致键永不回收）。
         * 计时基准统一取 {@code owner.get().tickCount}，与 {@code sample()} 解耦——
         * 事件记录/读取不依赖「每 tick 采样」这个副作用。
         */
        private final java.lang.ref.WeakReference<LivingEntity> owner;

        /** 回复量检测（供 Stat 条件与数学节点读取）：最近一次回复的资源键 / 请求量 / 实际量 / 溢出量。 */
        private String lastHealResource = "";
        private float lastHealRequested = 0f;
        private float lastHealActual = 0f;
        private float lastHealOverflow = 0f;

        /** 消耗量检测（与回复对称）：最近一次消耗的资源键 / 请求量 / 实际扣减 / 未能扣减部分。 */
        private String lastSpendResource = "";
        private float lastSpendRequested = 0f;
        private float lastSpendActual = 0f;
        private float lastSpendMissing = 0f;

        /** 承受伤害数值检测（供 Stat 条件与数学节点读取）：最近一次承受伤害的资源键 / 实际扣减 / 溢出量。 */
        private String lastDamageTakenResource = "";
        private float lastDamageTakenActual = 0f;
        private float lastDamageTakenOverflow = 0f;

        /** 造成伤害数值检测（与承受对称）：最近一次造成伤害的资源键 / 实际造成 / 溢出量。 */
        private String lastDamageCausedResource = "";
        private float lastDamageCausedActual = 0f;
        private float lastDamageCausedOverflow = 0f;

        private State() {
            this.owner = null;
        }

        private State(LivingEntity e) {
            this.owner = new java.lang.ref.WeakReference<>(e);
        }

        /** 属主是否仍可达（弱引用未 GC）。owner 已 GC 时本 State 应随 WeakHashMap 回收。 */
        private boolean alive() {
            return owner != null && owner.get() != null;
        }

        /** 当前计时基准：属主实体实时 tick（无属主 / 已 GC = 空状态，恒 0）。 */
        private int now() {
            return alive() ? owner.get().tickCount : 0;
        }

        /**
         * 计算某事件名在属主实体上的记忆窗口（tick）：从属主 STAT_STORE 中查该事件名由 Stat 条件
         * 声明的最大窗口（取所有引用该事件的 stat 的最大值）；属主不可达 / 无 STAT_STORE / 该事件
         * 无声明窗口时回退固定常数 {@link #MAX_TRIGGER_MEMORY}。窗口为 per-entity 动态值——
         * 不同实体挂载不同 stat 即拥有各自正确的清理窗口。
         */
        private int computeWindow(String name) {
            LivingEntity e = owner.get();
            if (e == null) return MAX_TRIGGER_MEMORY;
            StatStore store = e.getData(AttachmentRegistry.STAT_STORE);
            return store != null ? store.eventMaxWindow().getOrDefault(name, MAX_TRIGGER_MEMORY) : MAX_TRIGGER_MEMORY;
        }

        /** 记录一次触发，并存入回传的 int 值（如消耗充能球数量）；同时维护历史长度与记忆窗口。 */
        void record(String name, int value) {
            int t = now();
            int window = computeWindow(name);
            Deque<TriggerRecord> dq = triggers.computeIfAbsent(name, k -> new ArrayDeque<>());
            dq.addLast(new TriggerRecord(t, value));
            while (dq.size() > MAX_TRIGGER_HISTORY) dq.removeFirst();
            while (!dq.isEmpty() && t - dq.peekFirst().tick() > window) dq.removeFirst();
        }

        /**
         * 清空某事件名的无标签触发队列（{@code triggers}），使该键的 {@code sum/count/recently} 读取
         * 在下次写入前恒为「无记录」。
         * <p><b>用途</b>：把「窗口内累计」语义<b>收敛为「最近一次」</b>语义。典型场景是连击消耗——
         * 语料「per Combo consumed」指<b>本次施放</b>消耗了多少层，而非 recently 窗口内累计消耗了多少层；
         * 若在 4 秒窗口内连续消耗两次，累加会导致第二次把第一次的量一并计入 → 静默过度授予。
         * 写入端在每次真实消耗前先清空再写一条，即可让 {@code sum:<key>} 恒等于本次消耗量。
         * <p>只清无标签队列（{@code sum:/count:/recently:} 读的就是它）；带标签队列（{@code taggedTriggers}）
         * 不受影响，故 {@code recently:<key>:<tag>} 的分维判定仍然保留完整历史。
         * 与 {@link #record} 一致，未订阅的事件名直接跳过（避免无谓的 map 写入）。
         */
        void clear(String name) {
            if (!EventSubscription.isSubscribed(name)) return;
            triggers.remove(name);
        }

        /** 记录一次带标签的触发（如 leech + leech_health），按事件名聚合、同步维护记忆窗口。 */
        void record(String name, String tag) {
            record(name, Set.of(tag));
        }

        /** 记录一次带一组标签的触发，单记录多标签，同步维护记忆窗口。 */
        public void record(String name, Set<String> tags) {
            int t = now();
            int window = computeWindow(name);
            Deque<TaggedTrigger> dq = taggedTriggers.computeIfAbsent(name, k -> new ArrayDeque<>());
            dq.addLast(new TaggedTrigger(t, tags));
            while (dq.size() > MAX_TRIGGER_HISTORY) dq.removeFirst();
            while (!dq.isEmpty() && t - dq.peekFirst().tick() > window) dq.removeFirst();
        }

        /** 标签匹配模式：{@code EXACT}=标签严格相等；{@code PREFIX}=记录标签以给定标签为前缀（含恰好相等）。 */
        public enum Match implements StringRepresentable {
            EXACT, PREFIX;
            @Override public String getSerializedName() { return name().toLowerCase(); }
            public static final Codec<Match> CODEC = StringRepresentable.fromEnum(Match::values);
        }

        /**
         * 事件是否在最近 {@code windowTicks} 内以指定标签发生过（最近一次匹配标签的触发落在窗口内）。
         */
        public boolean recently(String name, String tag, int windowTicks) {
            return recently(name, tag, windowTicks, Match.EXACT);
        }

        /**
         * 事件是否在最近 {@code windowTicks} 内以指定标签发生过，匹配模式可配。
         * <p>
         * 标签采用 {@code :} 分层的组合形式（如 {@code leech_health} 或 {@code leech_health:coe:fireball}）：
         * 用 {@link Match#PREFIX} 时，{@code leech_health} 可命中 {@code leech_health:coe:fireball}（资源类型维度
         * 覆盖任意来源），而 {@code leech_health:coe:fireball} 精确匹配（来源维度独立判定，支持不同来源不同速率）。
         * <p>
         * 倒序扫描队列：一次命中写入多个标签（如 melee + fire），任一标签匹配即视为命中；队列时间递增，
         * 越出窗口即提前终止（覆盖玩家重生 tick 归零导致的 {@code dt<0}）。
         */
        public boolean recently(String name, String tag, int windowTicks, Match match) {
            if (!alive()) return false;
            Deque<TaggedTrigger> dq = taggedTriggers.get(name);
            if (dq == null || dq.isEmpty()) return false;
            int nowTick = now();
            for (Iterator<TaggedTrigger> it = dq.descendingIterator(); it.hasNext(); ) {
                TaggedTrigger r = it.next();
                int dt = nowTick - r.tick();
                if (dt < 0 || dt > windowTicks) break;
                for (String t : r.tags()) {
                    boolean tagMatch = switch (match) {
                        case EXACT -> t.equals(tag);
                        case PREFIX -> t.equals(tag) || t.startsWith(tag + ":");
                    };
                    if (tagMatch) return true;
                }
            }
            return false;
        }

        public void sample(LivingEntity e) {
            history.addLast(new Sample(e.tickCount, e.position()));
            while (history.size() > HISTORY_CAP) history.removeFirst();
        }

        /** 事件是否在最近 {@code windowTicks} 内发生过（最近一次触发落在窗口内）。 */
        public boolean recently(String name, int windowTicks) {
            if (!alive()) return false;
            Deque<TriggerRecord> dq = triggers.get(name);
            return dq != null && !dq.isEmpty() && (now() - dq.peekLast().tick()) <= windowTicks;
        }

        /** 窗口内触发次数（count 类 per-X 计数，如近 8 秒每次命中）。 */
        public int count(String name, int windowTicks) {
            if (!alive()) return 0;
            Deque<TriggerRecord> dq = triggers.get(name);
            if (dq == null) return 0;
            int t = now();
            int c = 0;
            for (TriggerRecord r : dq) if (t - r.tick() <= windowTicks) c++;
            return c;
        }

        /**
         * 窗口内「带指定标签」的触发次数（count 的标签分维版）：查 {@code taggedTriggers} 队列。
         * <p>与 {@link #recently(String, String, int, Match)} 同一匹配口径（EXACT / PREFIX），
         * 差异仅在返回<b>次数</b>而非布尔。队列时间递增，越出窗口即提前终止。
         */
        public int countTagged(String name, String tag, int windowTicks, Match match) {
            return countTagged(name, tag, windowTicks, match, false);
        }

        /**
         * 窗口内「不同标签」的数量（distinct 计数）：供「每使用一种不同 X」一族缩放。
         * {@code unique=true} 时对窗口内全部命中标签按值去重后计数。
         */
        public int countTagged(String name, String tag, int windowTicks, Match match, boolean unique) {
            if (!alive()) return 0;
            Deque<TaggedTrigger> dq = taggedTriggers.get(name);
            if (dq == null || dq.isEmpty()) return 0;
            int t = now();
            Set<String> seen = unique ? new HashSet<>() : null;
            int c = 0;
            for (TaggedTrigger r : dq) {
                int dt = t - r.tick();
                if (dt < 0 || dt > windowTicks) continue;
                for (String s : r.tags()) {
                    boolean m = switch (match) {
                        case EXACT -> s.equals(tag);
                        case PREFIX -> s.equals(tag) || s.startsWith(tag + ":");
                    };
                    if (!m) continue;
                    if (unique) {
                        if (seen.add(s)) c++;
                    } else {
                        c++;
                    }
                    break; // 单次触发内同标签只计一次
                }
            }
            return c;
        }

        /** 最近一次触发回传的 int 值（如消耗充能球数量）；无记录返回 0。 */
        public int lastValue(String name) {
            Deque<TriggerRecord> dq = triggers.get(name);
            return (dq != null && !dq.isEmpty()) ? dq.peekLast().value() : 0;
        }

        /** 记录一次回复数值（请求量 / 实际量 / 溢出量），供 Stat 条件与数学节点检测。 */
        void recordHeal(String resource, float requested, float actual, float overflow) {
            this.lastHealResource = resource;
            this.lastHealRequested = requested;
            this.lastHealActual = actual;
            this.lastHealOverflow = overflow;
        }

        public String lastHealResource() { return lastHealResource; }
        public float lastHealRequested() { return lastHealRequested; }
        public float lastHealActual() { return lastHealActual; }
        public float lastHealOverflow() { return lastHealOverflow; }

        /** 记录一次消耗数值（请求量 / 实际扣减 / 资源不足未扣减部分），与 {@link #recordHeal} 对称。 */
        void recordSpend(String resource, float requested, float actual, float missing) {
            this.lastSpendResource = resource;
            this.lastSpendRequested = requested;
            this.lastSpendActual = actual;
            this.lastSpendMissing = missing;
        }

        public String lastSpendResource() { return lastSpendResource; }
        public float lastSpendRequested() { return lastSpendRequested; }
        public float lastSpendActual() { return lastSpendActual; }
        public float lastSpendMissing() { return lastSpendMissing; }

        /** 记录一次承受伤害数值（实际扣减 / 溢出量），供 Stat 条件与数学节点检测。 */
        void recordDamageTaken(String resource, float actual, float overflow) {
            this.lastDamageTakenResource = resource;
            this.lastDamageTakenActual = actual;
            this.lastDamageTakenOverflow = overflow;
        }

        public String lastDamageTakenResource() { return lastDamageTakenResource; }
        public float lastDamageTakenActual() { return lastDamageTakenActual; }
        public float lastDamageTakenOverflow() { return lastDamageTakenOverflow; }

        /** 记录一次造成伤害数值（实际造成 / 溢出量），与 {@link #recordDamageTaken} 对称。 */
        void recordDamageCaused(String resource, float actual, float overflow) {
            this.lastDamageCausedResource = resource;
            this.lastDamageCausedActual = actual;
            this.lastDamageCausedOverflow = overflow;
        }

        public String lastDamageCausedResource() { return lastDamageCausedResource; }
        public float lastDamageCausedActual() { return lastDamageCausedActual; }
        public float lastDamageCausedOverflow() { return lastDamageCausedOverflow; }

        /** 窗口内所有触发回传值之和（如窗口内累计消耗充能球数量）。 */
        public int sum(String name, int windowTicks) {
            if (!alive()) return 0;
            Deque<TriggerRecord> dq = triggers.get(name);
            if (dq == null) return 0;
            int t = now();
            int s = 0;
            for (TriggerRecord r : dq) if (t - r.tick() <= windowTicks) s += r.value();
            return s;
        }

        /** 窗口内所有触发回传值的平均值；无记录返回 0。 */
        public double average(String name, int windowTicks) {
            if (!alive()) return 0D;
            Deque<TriggerRecord> dq = triggers.get(name);
            if (dq == null) return 0D;
            int t = now();
            int s = 0, c = 0;
            for (TriggerRecord r : dq) {
                if (t - r.tick() <= windowTicks) { s += r.value(); c++; }
            }
            return c == 0 ? 0D : (double) s / c;
        }

        /** 窗口内所有触发回传值的最大值。 */
        public int max(String name, int windowTicks) {
            if (!alive()) return 0;
            Deque<TriggerRecord> dq = triggers.get(name);
            if (dq == null) return 0;
            int t = now();
            int m = 0;
            for (TriggerRecord r : dq) if (t - r.tick() <= windowTicks) m = Math.max(m, r.value());
            return m;
        }

        /** 距最近一次触发的 tick 数；未触发返回 {@link Integer#MAX_VALUE}（消费方：EventHistoryValue.mode=elapsed）。 */
        public int elapsedSinceLast(String name) {
            if (!alive()) return Integer.MAX_VALUE;
            Deque<TriggerRecord> dq = triggers.get(name);
            if (dq == null || dq.isEmpty()) return Integer.MAX_VALUE;
            return now() - dq.peekLast().tick();
        }

        /** 窗口内是否几乎未移动（站定）。 */
        public boolean isStationary(int windowTicks) {
            return movementDistance(windowTicks) < MOVE_THRESHOLD;
        }

        /** 窗口内是否发生移动。 */
        public boolean isMoving(int windowTicks) {
            return movementDistance(windowTicks) >= MOVE_THRESHOLD;
        }

        /** 窗口内累计移动距离（方块）。 */
        public double movementDistance(int windowTicks) {
            if (history.size() < 2) return 0D;
            Sample[] arr = history.toArray(new Sample[0]);
            int latest = arr[arr.length - 1].tick;
            double dist = 0D;
            for (int i = 1; i < arr.length; i++) {
                if (arr[i].tick >= latest - windowTicks) {
                    dist += arr[i - 1].pos.distanceTo(arr[i].pos);
                }
            }
            return dist;
        }

        /** 候选实体是否在最近 {@code windowTicks} 内进入追踪者 Presence 且当前仍在范围内。 */
        public boolean recentlyEntered(UUID id, int windowTicks) {
            PresenceEntry pe = presence.get(id);
            return pe != null && pe.inRange && (now() - pe.enterTick) <= windowTicks;
        }

        /** 候选实体是否在最近 {@code windowTicks} 内离开追踪者 Presence（当前已不在范围内）。 */
        public boolean recentlyLeft(UUID id, int windowTicks) {
            PresenceEntry pe = presence.get(id);
            return pe != null && !pe.inRange && pe.leaveTick >= 0 && (now() - pe.leaveTick) <= windowTicks;
        }

        /** 当前在追踪者 Presence 范围内的全部实体 UUID（供 PRESENCE_TARGETS 批量目标解析）。 */
        public java.util.Set<UUID> presenceMembers() {
            java.util.Set<UUID> out = new java.util.HashSet<>();
            presence.forEach((id, pe) -> { if (pe.inRange) out.add(id); });
            return out;
        }

        /**
         * 扫描追踪者附近实体，维护进/出 Presence 时间戳（瞬态缓存，随实体 GC 回收）。
         * 仅当实体在范围内才记录；离开且超过 {@code memoryTicks} 即剔除，保证缓存规模有界。
         */
        public void updatePresence(LivingEntity tracker, double radius, int memoryTicks) {
            int now = tracker.tickCount;
            AABB box = new AABB(
                    tracker.getX() - radius, tracker.getY() - radius, tracker.getZ() - radius,
                    tracker.getX() + radius, tracker.getY() + radius, tracker.getZ() + radius);
            Set<UUID> seen = new HashSet<>();
            // 获取实体统一经 Context 数据基站回调（EntityQuery 包围盒扫描 + 排除追踪者自身）
            for (LivingEntity e : ExpressionContext.get(tracker).queryEntities(
                    EntityQuery.box(box).exclude(tracker))) {
                UUID id = e.getUUID();
                seen.add(id);
                PresenceEntry pe = presence.computeIfAbsent(id, k -> new PresenceEntry());
                if (!pe.inRange) {
                    pe.enterTick = now;   // 跨入范围 → 记录进入 tick
                    pe.inRange = true;
                }
                pe.leaveTick = -1;        // 仍在范围内，清除离开标记
            }
            // 标记离开 + 按记忆窗口淘汰
            for (Iterator<Map.Entry<UUID, PresenceEntry>> it = presence.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<UUID, PresenceEntry> en = it.next();
                PresenceEntry pe = en.getValue();
                if (!seen.contains(en.getKey())) {
                    if (pe.inRange) {
                        pe.inRange = false;
                        pe.leaveTick = now;   // 跨出范围 → 记录离开 tick
                    } else if (now - pe.leaveTick > memoryTicks) {
                        it.remove();
                    }
                }
            }
        }

        private static final class PresenceEntry {
            int enterTick = 0;   // 最近一次进入范围 tick
            int leaveTick = -1;  // 最近一次离开范围 tick（-1 表示未知/仍在范围内）
            boolean inRange = false;
        }

        private record Sample(int tick, Vec3 pos) {}
    }
}
