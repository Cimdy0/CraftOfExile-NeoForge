package com.cimdy.coe.data.codec.support.math;

import com.cimdy.coe.data.codec.support.math.context.nodes.scope.BindExpr;
import com.cimdy.coe.data.codec.support.math.context.nodes.scope.ValueExprGroup;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.CompareValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.damage.DamageValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.MovementValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.PresenceDependent;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceRarityValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.scope.SelectorNode;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.TimerValue;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;

import com.cimdy.coe.data.codec.support.stat.condition.AndCondition;
import com.cimdy.coe.data.codec.support.stat.condition.ChainCondition;
import com.cimdy.coe.data.codec.support.stat.condition.FilterCondition;
import com.cimdy.coe.data.codec.support.stat.condition.NotCondition;
import com.cimdy.coe.data.codec.support.stat.condition.OrCondition;
import com.cimdy.coe.data.codec.support.stat.condition.PresenceCondition;
import com.cimdy.coe.data.codec.support.stat.condition.RadiusCondition;
import com.cimdy.coe.data.codec.support.stat.condition.StatCondition;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatGroup;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.event.data.LoadEvent;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stat 条目 → 事件缓存需求提取器（按需订阅的静态侧）。
 * <p>
 * 遍历 entry 的三处来源：conditions（{@link ValueExpr} 谓词树）、condition（{@link StatCondition}
 * 实体获取树）、amount（{@link MathExpression} 树），收集：
 * <ul>
 *   <li>引用的事件名（{@link EventHistoryValue#name()} / {@link TimerValue#name()} 去 {@code time_} 前缀）
 *       → 供 {@link com.cimdy.coe.event.EventSubscription} 订阅；</li>
 *   <li>presence 维度需求（任一实现 {@link PresenceDependent} 的节点，如 {@link PresenceValue} /
 *       {@link PresenceRarityValue}）→ 启用每 tick 全域进/出扫描。</li>
 * </ul>
 * <p>不订阅（有意排除）：{@code event:}/{@code event_tag:}（EventValue）只比对当前 fire 上下文、不驱动
 * State 队列记录；{@code damage_taken/damage_caused} 读数值快照、movement 读坐标采样——均由各自写入方无条件维护。
 * <p>遍历规则：conditions 全量；condition 按 {@link StatCondition} 九实现递归（见 {@link #collectCondition}）；
 * amount 递归（照抄 MathParser.write 形态），{@code Read} 模板引用跟进
 * {@link LoadEvent#EXPRESSION_MAP}，带防环 visited。
 */
public final class EventRefCollector {

    /** 提取结果：引用的事件名集合 + 是否需 presence 扫描 + 各事件名的最大记忆窗口（tick）。 */
    public record Result(Set<String> events, boolean presence, Map<String, Integer> maxWindow) {
        public static final Result EMPTY = new Result(Set.of(), false, Map.of());
    }

    private EventRefCollector() {
    }

    /** 提取一条 stat 条目的事件缓存需求。 */
    public static Result collect(StatEntry entry) {
        Set<String> events = new HashSet<>();
        Map<String, Integer> maxWindow = new HashMap<>();
        boolean[] presence = {false};
        IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();
        collectEntry(entry, events, maxWindow, presence, visited);
        return events.isEmpty() && !presence[0] ? Result.EMPTY : new Result(Set.copyOf(events), presence[0], Map.copyOf(maxWindow));
    }

    /**
     * 递归收集条目：StatGroup 摊平遍历全 members；StatRead 解析目标后递归（目标可能再是 StatGroup——
     * StatGroup.conditions()/amount() 只转发首 member，直接转发会漏组内其余 member 的事件引用，M3）。
     */
    private static void collectEntry(StatEntry entry, Set<String> events, Map<String, Integer> maxWindow,
                                     boolean[] presence, IdentityHashMap<Object, Boolean> visited) {
        if (entry == null || visited.put(entry, Boolean.TRUE) != null) return;
        if (entry instanceof StatGroup g) {
            for (StatEntry m : g.flatten()) collectEntry(m, events, maxWindow, presence, visited);
            return;
        }
        // 仅引用态才做法级递归：ref == null 是内联定义，自身就是完整条目，
        // 应自然落到下方通用字段扫描——不能走 resolve()（内联态会抛「未注册的 StatEntry: null」），
        // 且内联形态下 conditions() / condition() 本身就是它的取值口，无需解引用。
        if (entry instanceof StatRead sr && sr.ref() != null) {
            collectEntry(sr.resolve(), events, maxWindow, presence, visited);
            return;
        }
        for (ValueExpr c : entry.conditions()) {
            collectNode(c, events, maxWindow, presence, visited);
        }
        // D2：condition（实体获取，StatCondition 树）与 conditions（谓词，ValueExpr 树）是两条互不相通的
        // 求值路径，只扫后者会让「纯 presence condition 条目」的 presence 需求永不登记 →
        // Result.presence 恒 false → LoadEvent 不开 Presence 扫描 → Event.State.presenceMembers() 恒空 →
        // PresenceCondition.resolve 恒返空集 → 条目永不生效且无任何异常（哑失效）。故必须补扫本段。
        collectCondition(entry.condition(), events, maxWindow, presence, visited);
        entry.amount().value().right().ifPresent(e -> collectExpr(e, events, maxWindow, presence, visited));
    }

    /**
     * <b>presence 判定的唯一收口</b>：本 condition 节点的 resolve 是否依赖 {@code Event.State} 的
     * presence 成员集（需要每 tick 全域进/出扫描）。
     * <p>
     * 判据优先级：<b>① 标记接口 {@link PresenceDependent} 优先</b> —— 与 {@link #collectNode} 对
     * ValueExpr 侧的判定同构（见其 {@code :96-98} 注释）：新增「读 presence 的获取型节点」只要实现该
     * 接口即自动接线，无需回头补分支。<b>② {@link PresenceCondition} 作为过渡兜底</b>：它当前尚未实现
     * 该接口，但 resolve 直接消费 {@code Event.State#presenceMembers()}。
     * <p>
     * <b>为何不把某一个具体子类钉死为唯一依据</b>：S4 架构会把 anchor / radius / presence / relation
     * 迁成 ValueExpr 的 {@code Entities} 视图节点，届时刻意点从「某个 condition 子类」迁移到
     * 「节点是否实现 {@code PresenceDependent}」。本方法已把两条判据并成单一落点，S4 到位时只需删掉
     * 分支 ②，全部调用点（本类 collect 侧与 {@code StatStore#needsPresenceTracking} 侧）无需改动。
     */
    private static boolean presenceDependent(StatCondition node) {
        return node instanceof PresenceDependent || node instanceof PresenceCondition;
    }

    /**
     * 递归收集<b>实体获取条件</b>（{@link StatCondition} 树）的 presence 需求。
     * <p>
     * 九个实现按「有无 condition 子树」分两类，逐一处理：
     * <ul>
     *   <li><b>需递归</b>：{@code and} / {@code or}（parts 列表）、{@code not}（inner）、
     *       {@code chain}（inner + then 两跳）、{@code filter}（{@code of} 为 condition 子树，
     *       且 {@code where} 是 ValueExpr 谓词树须回灌 {@link #collectNode}）、
     *       {@code radius}（{@code radius} 是 {@link MathExpression}，内含 Variable→ValueExpr 可读 presence）；</li>
     *   <li><b>叶子</b>：{@code anchor}（仅 {@link com.cimdy.coe.data.codec.support.stat.condition.ConditionAnchor} 枚举）、
     *       {@code relation}（枚举 + {@code of} 枚举锚点）、{@code presence}
     *       （{@code of} 是 {@code Optional<ConditionAnchor>} 枚举锚点，<b>并非</b>嵌套 condition，无子树可递归；
     *       其 presence 需求已由 {@link #presenceDependent} 置位）。</li>
     * </ul>
     * 未知新实现走 default 分支不置位——但 {@code StatConditionTypes#bootstrap} 是唯一登记点，
     * 新增实现必然在此显式登记，故漏分支只可能是有意为之的无 presence 语义节点。
     */
    private static void collectCondition(@Nullable StatCondition node, Set<String> events,
                                         Map<String, Integer> maxWindow, boolean[] presence,
                                         IdentityHashMap<Object, Boolean> visited) {
        if (node == null || visited.put(node, Boolean.TRUE) != null) return;
        if (presenceDependent(node)) presence[0] = true;
        if (node instanceof AndCondition a) {
            if (a.parts() != null) {
                for (StatCondition part : a.parts()) {
                    collectCondition(part, events, maxWindow, presence, visited);
                }
            }
        } else if (node instanceof OrCondition o) {
            if (o.parts() != null) {
                for (StatCondition part : o.parts()) {
                    collectCondition(part, events, maxWindow, presence, visited);
                }
            }
        } else if (node instanceof NotCondition n) {
            collectCondition(n.inner(), events, maxWindow, presence, visited);
        } else if (node instanceof ChainCondition c) {
            // 多跳原语：inner 与 then 都是完整 condition 子树，两跳都要扫（漏 then 即漏「第二跳的 presence」）
            collectCondition(c.inner(), events, maxWindow, presence, visited);
            collectCondition(c.then(), events, maxWindow, presence, visited);
        } else if (node instanceof FilterCondition f) {
            // where 是 ValueExpr 谓词树：与 condition() 同为可读 presence 的通道（如 presence_count 谓词），
            // 必须回灌 collectNode，否则「用 presence 谓词过滤实体」的条目同样漏登记。
            collectNode(f.where(), events, maxWindow, presence, visited);
            collectCondition(f.of() == null ? null : f.of().orElse(null), events, maxWindow, presence, visited);
        } else if (node instanceof RadiusCondition r) {
            // radius 是 MathExpression（Variable→ValueExpr），半径本身可由 presence 派生，须跟进 collectExpr
            if (r.radius() != null) {
                r.radius().value().right().ifPresent(e -> collectExpr(e, events, maxWindow, presence, visited));
            }
        }
    }

    /**
     * 条目的 {@link StatEntry#condition()} 实体获取条件树是否依赖 presence 成员集。
     * <p>
     * 与 {@link #collect(StatEntry)} 的 presence 提取<b>同一口径</b>（共用 {@link #collectCondition}
     * 与 {@link #presenceDependent}）：供 {@code StatStore#needsPresenceTracking()} 判定，
     * 避免该处另写一套「像不像 presence」的启发式而与实际求值路径漂移。
     */
    public static boolean conditionNeedsPresence(@Nullable StatCondition root) {
        if (root == null) return false;
        boolean[] presence = {false};
        collectCondition(root, new HashSet<>(), new HashMap<>(), presence, new IdentityHashMap<>());
        return presence[0];
    }

    private static void collectNode(ValueExpr node, Set<String> events, Map<String, Integer> maxWindow,
                                    boolean[] presence, IdentityHashMap<Object, Boolean> visited) {
        if (node == null || visited.put(node, Boolean.TRUE) != null) return;
        if (node instanceof EventHistoryValue eh) {
            events.add(eh.name());
            // 捕获该事件名在 Stat 条件中声明的最大记忆窗口（Math.ceil 向上取整为 tick）：
            // 供 Event.State 按事件名做差异化 TTL 清理，无需全局常数。TimerValue 无窗口概念，不动。
            maxWindow.merge(eh.name(), (int) Math.ceil(eh.window()), Integer::max);
        } else if (node instanceof TimerValue tv) {
            String n = tv.name();
            events.add(n.startsWith("time_") ? n.substring("time_".length()) : n);
        } else if (node instanceof PresenceDependent) {
            // 按接口而非具体类判定：新增读 Presence 的节点只要实现 PresenceDependent 即自动开启扫描，
            // 不存在「漏补 instanceof 分支 → 扫描不启用 → 节点静默恒返 0」的哑失效路径。
            presence[0] = true;
        } else if (node instanceof SelectorNode sel) {
            sel.where().ifPresent(w -> collectExpr(w, events, maxWindow, presence, visited));
            sel.sort().ifPresent(s -> collectExpr(s, events, maxWindow, presence, visited));
            sel.then().ifPresent(t -> collectNode(t, events, maxWindow, presence, visited));
        } else if (node instanceof BindExpr b) {
            // 跨实体绑定（BindExpr：SELF/SOURCE/CANDIDATE/SLOT）统一穿透外壳，递归收集 inner 的事件 / presence 需求。
            collectNode(b.inner(), events, maxWindow, presence, visited);
        } else if (node instanceof ValueExprGroup grp) {
            // M1：既有条件大量以「组」形式登记（ValueExpr.of(list) 压成的 ValueExprGroup），
            // 组外壳本身不是 PresenceDependent —— 不递归就永远看不到组内的 presence_count，
            // 于是 COND_PRESENCE_TOTEM/ALLY/ALLY_SUMMONED 一类条件的 presence 需求不会登记、
            // PresenceCountValue 恒返 0（哑失效：无异常、无告警、条件永不成立）。
            for (ValueExpr inner : grp.nodes()) collectNode(inner, events, maxWindow, presence, visited);
        } else if (node instanceof CompareValue cmp) {
            // M1 同族：presence_count 通常作为比较运算的操作数（CompareValue 左右两侧）出现。
            collectNode(cmp.left(), events, maxWindow, presence, visited);
            collectNode(cmp.right(), events, maxWindow, presence, visited);
        }
        // EventValue（event:/event_tag:）不驱动 State；DamageValue/MovementValue 由数值快照/采样维护
    }

    private static void collectExpr(MathExpression expr, Set<String> events, Map<String, Integer> maxWindow,
                                    boolean[] presence, IdentityHashMap<Object, Boolean> visited) {
        if (expr == null || visited.put(expr, Boolean.TRUE) != null) return;
        switch (expr) {
            case ExpressionCodec.Constant ignored -> { }
            case ExpressionCodec.Variable v -> collectNode(v.node(), events, maxWindow, presence, visited);
            case ExpressionCodec.Read r -> {
                MathExpression def = LoadEvent.EXPRESSION_MAP.get(r.id());
                if (def != null) collectExpr(def, events, maxWindow, presence, visited);
            }
            case ExpressionCodec.UnaryOperation u -> collectExpr(u.expression(), events, maxWindow, presence, visited);
            case ExpressionCodec.BinaryOperation b -> {
                collectExpr(b.left(), events, maxWindow, presence, visited);
                collectExpr(b.right(), events, maxWindow, presence, visited);
            }
            case ExpressionCodec.Logical l -> {
                collectExpr(l.left(), events, maxWindow, presence, visited);
                collectExpr(l.right(), events, maxWindow, presence, visited);
            }
            case ExpressionCodec.Not n -> collectExpr(n.expression(), events, maxWindow, presence, visited);
            case ExpressionCodec.ConditionOperation co -> {
                for (ExpressionCodec.ConditionOperation.Condition c : co.conditions()) {
                    collectExpr(c.left(), events, maxWindow, presence, visited);
                    collectExpr(c.right(), events, maxWindow, presence, visited);
                    collectExpr(c.value(), events, maxWindow, presence, visited);
                }
                collectExpr(co.def(), events, maxWindow, presence, visited);
            }
            case ExpressionCodec.LevelTable lt -> {
                collectExpr(lt.variable(), events, maxWindow, presence, visited);
                collectExpr(lt.def(), events, maxWindow, presence, visited);
            }
            default -> { }
        }
    }
}
