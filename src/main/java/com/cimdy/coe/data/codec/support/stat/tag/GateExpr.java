package com.cimdy.coe.data.codec.support.stat.tag;

import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import com.cimdy.coe.data.codec.support.meta.TagHierarchy;
import com.cimdy.coe.data.codec.support.logic.LeafMatcher;
import com.cimdy.coe.data.codec.support.logic.LogicalExpr;
import com.cimdy.coe.data.codec.support.logic.LogicalExprs;

/**
 * 标签布尔表达式：把效果条目（{@code StatEffect#tags()}）的「作用域过滤条件」从「全满足集合」
 * 升级为支持 <b>与 / 或 / 非 / 嵌套括号</b> 的表达式。
 *
 * <p>与 / 或 / 非 的组合语义复用通用谓词接口 {@link LogicalExpr}（与装备部位维度
 * {@code SlotExpr} 同一套机制），本类只保留标签维度的<b>叶子判定</b>（{@link #DEFAULT_LEAF}）。
 *
 * <p>叶子判定读的是 {@link GateTag} 那张<b>数据驱动</b>的层级表：JSON 里在「元素」名下写
 * 火 / 电 / 冰，传 {@code "fire"} 仍被「元素」命中（{@link GateTag#childrenOf}）；
 * 该表可由数据包整体替换（{@code replace}），故组合机制本身不绑定任何硬编码标签。
 *
 * <h2>语法（JSON 数组项字符串，数组整体 = 与）</h2>
 * 每个数组项是一个表达式片段，多项之间是<b>与</b>（AND）：
 * <pre>
 *   "fire"                 → 火焰（单标签）
 *   "melee", "fire"        → 近战 与 火焰（数组多项 = AND）
 *   "fire|cold"            → 火焰 或 冰霜（| = OR）
 *   "!fire"                → 非火焰（! = NOT）
 *   "melee", "!elemental"  → 近战 且 非元素
 *   "bow|crossbow", "lightning|cold"  → (弓 或 弩) 且 (闪电 或 冰霜)
 *   "(!fire)", "fire|cold" 等任意括号嵌套
 * </pre>
 * 运算符优先级：{@code !} > {@code ,}(AND) > {@code |}(OR)；
 * 全角字符自动归一（{@code ，！（）} → {@code ,!()}）。
 *
 * <h2>求值语义</h2>
 * {@link #matches(Set)} 对「作用域标签集合」（事件标签 / 伤害槽位标签）求值，语义同 {@link GateTag#match}：
 * <ul>
 *   <li><b>父标签上位包含</b>：叶子为父类别（如 {@code elemental}）时，作用域含其任一子标签
 *       （{@code fire}/{@code cold}/{@code lightning}…）即命中（经 {@link GateTag#childrenOf}）；</li>
 *   <li><b>{@code proj:} 前缀通配</b>：叶子为 {@code proj:} 前缀时，作用域含任意 {@code proj:xxx} 实例即命中。</li>
 * </ul>
 *
 * <h2>索引配合</h2>
 * {@link #leaves()} 返回全部叶子标签（含 NOT 内部），供 {@link StatStore} 反向索引建候选键；
 * {@link #containsNot()} 为真时索引降级为无条件候选（NOT 表达式对几乎任何作用域都可能命中，保守不漏）。
 *
 * <p>不可变；{@link #ALWAYS} 为恒真（无条件，空数组）。
 */
public final class GateExpr {

    /** 恒真表达式（无条件：空 AND）。 */
    public static final GateExpr ALWAYS = new GateExpr(new And(List.of()));

    /** 恒假表达式（永不命中：非(空 AND)）。作「或」运算的单位元——{@code X.or(NEVER) == X}。 */
    public static final GateExpr NEVER = new GateExpr(new Not(new And(List.of())));

    // ── 表达式树节点（均实现通用谓词接口：组合语义复用 {@link LogicalExpr}）──

    /** 表达式树节点。 */
    public sealed interface Node extends LogicalExpr<Set<String>> permits Leaf, And, Or, Not {
    }

    /** 叶子：单个标签（或父类别 / proj: 前缀）。 */
    public record Leaf(String tag) implements Node {
        @Override
        public boolean matches(Set<String> scope) {
            return DEFAULT_LEAF.matches(scope, this.tag);
        }
    }

    /** 与：全部子节点命中（语义见 {@link LogicalExpr#All}）。 */
    public record And(List<Node> parts) implements Node {
        @Override
        public boolean matches(Set<String> scope) {
            return LogicalExprs.allOf(this.parts, scope);
        }
    }

    /** 或：任一子节点命中（语义见 {@link LogicalExpr#Any}）。 */
    public record Or(List<Node> parts) implements Node {
        @Override
        public boolean matches(Set<String> scope) {
            return LogicalExprs.anyOf(this.parts, scope);
        }
    }

    /** 非：子节点取反。 */
    public record Not(Node inner) implements Node {
        @Override
        public boolean matches(Set<String> scope) {
            return !this.inner.matches(scope);
        }
    }

    private final Node root;

    private GateExpr(Node root) {
        this.root = root;
    }

    // ── 构造 ──

    /** 由字符串数组构造：每项解析为表达式片段，整体 = AND；空数组 = {@link #ALWAYS}。 */
    public static GateExpr of(Collection<String> items) {
        if (items == null || items.isEmpty()) return ALWAYS;
        List<Node> nodes = new ArrayList<>(items.size());
        for (String s : items) nodes.add(parseNode(s));
        return new GateExpr(nodes.size() == 1 ? nodes.get(0) : new And(nodes));
    }

    /** 由单个表达式字符串构造（等价于 {@code of(List.of(s))}）。 */
    public static GateExpr of(String item) {
        return of(List.of(item));
    }

    /** 追加一个与（AND）叶子，返回新表达式（用于去重标记并入等程序化扩展，不经由 List 字符串包装）。 */
    public GateExpr and(String additional) {
        Node leaf = new Leaf(additional);
        if (root instanceof And a) {
            List<Node> parts = new ArrayList<>(a.parts());
            parts.add(leaf);
            return new GateExpr(new And(parts));
        }
        return new GateExpr(new And(List.of(root, leaf)));
    }

    /**
     * 或（OR）：返回「本表达式 ∪ {@code other}」的新表达式——任一分支命中即命中。
     *
     * <p>吸收律：任一侧为 {@link #ALWAYS} 时结果恒为 {@code ALWAYS}；任一侧为 {@link #NEVER} 时
     * 结果为另一侧。故未声明外溢作用域的条目取 {@code tags().or(NEVER) == tags()}（同一实例），
     * 门控语义与零分配开销均不变。
     *
     * <p>两侧同为 {@link Or} 时扁平合并（{@link #flattenOr}），使表达式树深度不随组合次数增长——
     * 「某条目的作用域 ∪ 另一条目的作用域」这类逐步并入因此保持 O(叶子数) 的判定开销。
     */
    public GateExpr or(GateExpr other) {
        if (other == null || other.isNever()) return this;
        if (isNever()) return other;
        if (isAlwaysTrue() || other.isAlwaysTrue()) return ALWAYS;
        if (this.equals(other)) return this;
        List<Node> parts = new ArrayList<>();
        flattenOr(root, parts);
        flattenOr(other.root, parts);
        return parts.size() == 1 ? new GateExpr(parts.get(0)) : new GateExpr(new Or(List.copyOf(parts)));
    }

    /** 是否恒假（永不命中）：即 {@code !(空 AND)}。 */
    public boolean isNever() {
        return root instanceof Not n && n.inner() instanceof And a && a.parts().isEmpty();
    }

    /** 把 Or 节点的叶子摊平进 {@code out}（非 Or 节点原样加入），供 {@link #or} 扁平合并。 */
    private static void flattenOr(Node n, List<Node> out) {
        if (n instanceof Or o) o.parts().forEach(p -> flattenOr(p, out));
        else out.add(n);
    }

    // ── 求值 ──

    /**
     * 对作用域标签集合求值（默认复用 {@link GateTag#HIERARCHY} 的父标签上位包含 + proj 通配）。
     *
     * @param scope 作用域标签集合（事件标签 / 伤害槽位标签等，保持 Set<String> 不变）
     */
    /** 默认叶子判定器：沿用 Stat 内置层级 + {@code proj:} 通配。 */
    private static final LeafMatcher<Set<String>> DEFAULT_LEAF =
            (scope, tag) -> matchLeaf(tag, scope, GateTag.HIERARCHY);

    public boolean matches(Set<String> scope) {
        return this.root.matches(scope);
    }

    /**
     * 对作用域标签集合求值，使用指定的 {@link TagHierarchy} 解析器。
     * 使 Stat 作用域与装备类型键（{@code ItemTypeHierarchy.resolver()}）共用同一套表达式引擎——
     * 二者本质都是「字符串集合判定 + 父辖子上位包含」。
     *
     * <p>此路径需要把层级解析器下发到叶子，而 {@link LogicalExpr} 的算子不携带求值期上下文，
     * 故把解析器编成 {@link LeafMatcher} 后<b>编译</b>一棵通用谓词树再求值。
     * 仅用于非 Stat 维度的复用入口（当前仅 {@code BranchExpr} 一处），<b>不在热路径上</b>；
     * 热路径（{@link #matches(Set)}）走算子委托、零分配。
     *
     * @param scope    作用域标签集合
     * @param resolver 标签层级解析器（{@code GateTag.HIERARCHY} 沿用 Stat 内置层级；
     *                {@code ItemTypeHierarchy.resolver()} 用物品类型树层级）
     */
    public boolean matches(Set<String> scope, TagHierarchy resolver) {
        return matches(scope, (s, tag) -> matchLeaf(tag, s, resolver));
    }

    /**
     * 用给定叶子判定器求值（等价于 {@link #matches(Set, TagHierarchy)}，但允许直接注入判定器）。
     */
    public boolean matches(Set<String> scope, LeafMatcher<Set<String>> leaf) {
        return compile(this.root, leaf).matches(scope);
    }

    /** 把本维度的 {@link Node} 树编译成 {@link LogicalExpr} 通用谓词树（叶子绑定判定器）。 */
    private static LogicalExpr<Set<String>> compile(Node n, LeafMatcher<Set<String>> leaf) {
        return switch (n) {
            case Leaf l -> LogicalExprs.leaf(l.tag(), leaf);
            case And a -> LogicalExpr.all(a.parts().stream().map(p -> compile(p, leaf)).toList());
            case Or o -> LogicalExpr.any(o.parts().stream().map(p -> compile(p, leaf)).toList());
            case Not t -> LogicalExpr.not(compile(t.inner(), leaf));
        };
    }

    /**
     * 叶子判定 —— 判定本体在 {@link com.cimdy.coe.data.codec.support.logic.LogicalExprs#matches(TagHierarchy, String, Set)}
     * （{@code TagHierarchy} 只提供「关系数据从哪来」，判定逻辑统一收在 LogicalExprs 里），
     * 本类只做转发，不复制一份判定逻辑。
     */
    private static boolean matchLeaf(String tag, Set<String> scope, TagHierarchy resolver) {
        return LogicalExprs.matches(resolver, tag, scope);
    }

    // ── 索引辅助 ──

    /** 全部叶子标签（含 NOT 内部），供反向索引建候选键。 */
    public Set<String> leaves() {
        Set<String> out = new HashSet<>();
        collectLeaves(root, out);
        return Set.copyOf(out);
    }

    private static void collectLeaves(Node n, Set<String> out) {
        switch (n) {
            case Leaf l -> out.add(l.tag());
            case And a -> a.parts().forEach(p -> collectLeaves(p, out));
            case Or o -> o.parts().forEach(p -> collectLeaves(p, out));
            case Not t -> collectLeaves(t.inner(), out);
        }
    }

    /** 是否含 NOT 节点：为真时索引需降级（NOT 表达式对几乎任何作用域都可能命中）。 */
    public boolean containsNot() {
        return containsNot(root);
    }

    private static boolean containsNot(Node n) {
        return switch (n) {
            case Leaf l -> false;
            case And a -> a.parts().stream().anyMatch(GateExpr::containsNot);
            case Or o -> o.parts().stream().anyMatch(GateExpr::containsNot);
            case Not t -> true;
        };
    }

    /** 是否恒真（无条件）：空数组 / 空 AND。 */
    public boolean isAlwaysTrue() {
        return root instanceof And a && a.parts().isEmpty();
    }

    // ── 解析器 ──

    private static Node parseNode(String s) {
        String normalized = s.trim()
                .replace('，', ',').replace('！', '!')
                .replace('（', '(').replace('）', ')');
        if (normalized.isEmpty()) throw new IllegalArgumentException("empty tag expression");
        Parser p = new Parser(normalized);
        Node n = p.parseAnd();
        if (!p.atEnd()) throw new IllegalArgumentException("unparsed tag expression tail: \"" + normalized + "\"");
        return n;
    }

    /** 递归下降解析器：优先级 {@code !} > {@code ,}(AND) > {@code |}(OR)。 */
    private static final class Parser {
        private final String s;
        private int i;

        Parser(String s) {
            this.s = s;
        }

        boolean atEnd() {
            skipWs();
            return i >= s.length();
        }

        private void skipWs() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        Node parseAnd() {
            List<Node> parts = new ArrayList<>();
            parts.add(parseOr());
            while (true) {
                skipWs();
                if (i >= s.length() || s.charAt(i) != ',') break;
                i++;
                parts.add(parseOr());
            }
            return parts.size() == 1 ? parts.get(0) : new And(parts);
        }

        Node parseOr() {
            List<Node> parts = new ArrayList<>();
            parts.add(parseNot());
            while (true) {
                skipWs();
                if (i >= s.length() || s.charAt(i) != '|') break;
                i++;
                parts.add(parseNot());
            }
            return parts.size() == 1 ? parts.get(0) : new Or(parts);
        }

        Node parseNot() {
            skipWs();
            if (i < s.length() && s.charAt(i) == '!') {
                i++;
                return new Not(parseNot());
            }
            return parseAtom();
        }

        Node parseAtom() {
            skipWs();
            if (i < s.length() && s.charAt(i) == '(') {
                i++;
                Node n = parseAnd();
                skipWs();
                if (i >= s.length() || s.charAt(i) != ')')
                    throw new IllegalArgumentException("missing ')' in tag expression: \"" + s + "\"");
                i++;
                return n;
            }
            int start = i;
            while (i < s.length() && s.charAt(i) != ',' && s.charAt(i) != '|'
                    && s.charAt(i) != '(' && s.charAt(i) != ')' && s.charAt(i) != '!') {
                i++;
            }
            String tag = s.substring(start, i).trim();
            if (tag.isEmpty()) throw new IllegalArgumentException("empty tag in expression: \"" + s + "\"");
            return new Leaf(tag);
        }
    }

    // ── 编解码（结构化布尔树，直接序列化 Node） ──

    /** 结构化递归 codec：{@code tag} 单键 / {@code and} / {@code or} / {@code not} 四字段互斥，全缺省即恒真（ALWAYS）。 */
    public static final Codec<GateExpr> CODEC = Codec.recursive("tag_expr", self ->
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.optionalFieldOf("tag").forGetter(GateExpr::leafTag),
                    self.listOf().optionalFieldOf("and").forGetter(GateExpr::andParts),
                    self.listOf().optionalFieldOf("or").forGetter(GateExpr::orParts),
                    self.optionalFieldOf("not").forGetter(GateExpr::notPart)
            ).apply(instance, (tag, and, or, not) -> {
                if (tag.isPresent()) return new GateExpr(new Leaf(tag.get()));
                if (and.isPresent()) return new GateExpr(new And(and.get().stream().map(GateExpr::root).toList()));
                if (or.isPresent()) return new GateExpr(new Or(or.get().stream().map(GateExpr::root).toList()));
                return not.map(n -> new GateExpr(new Not(n.root()))).orElse(ALWAYS);
            })));

    private static Optional<String> leafTag(GateExpr e) {
        return e.root instanceof Leaf l ? Optional.of(l.tag()) : Optional.empty();
    }

    private static Optional<List<GateExpr>> andParts(GateExpr e) {
        return e.root instanceof And a ? Optional.of(a.parts().stream().map(GateExpr::of).toList()) : Optional.empty();
    }

    private static Optional<List<GateExpr>> orParts(GateExpr e) {
        return e.root instanceof Or o ? Optional.of(o.parts().stream().map(GateExpr::of).toList()) : Optional.empty();
    }

    private static Optional<GateExpr> notPart(GateExpr e) {
        return e.root instanceof Not n ? Optional.of(new GateExpr(n.inner())) : Optional.empty();
    }

    /**
     * 内部节点的公开只读入口（表达式树本身，节点为不可变 record）。
     *
     * <p>UI 文本渲染需要按节点种类分派（叶子查值键 / 与或各用自己的连接符 / 非加否定前缀），
     * 只读 {@link #leaves()} 会丢掉布尔结构（{@code !fire} 与 {@code fire|cold} 都摊成同一组叶子）。
     */
    public Node root() {
        return root;
    }

    /** 由内部节点构造（codec / 组合用）。 */
    public static GateExpr of(Node node) {
        return new GateExpr(node);
    }

    /** 人类可读描述（DSL 形态，仅用于日志 / 调试）。 */
    public String describe() {
        if (isAlwaysTrue()) return "";
        if (root instanceof And a) {
            return a.parts().stream().map(GateExpr::nodeToString).collect(java.util.stream.Collectors.joining(", "));
        }
        return nodeToString(root);
    }

    private static String nodeToString(Node n) {
        return switch (n) {
            case Leaf l -> l.tag();
            case Or o -> String.join("|", o.parts().stream().map(GateExpr::nodeToString).toList());
            case And a -> "(" + String.join(", ", a.parts().stream().map(GateExpr::nodeToString).toList()) + ")";
            case Not t -> "!" + nodeToString(t.inner());
        };
    }

    @Override
    public String toString() {
        return describe();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GateExpr e)) return false;
        return Objects.equals(root, e.root);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(root);
    }
}
