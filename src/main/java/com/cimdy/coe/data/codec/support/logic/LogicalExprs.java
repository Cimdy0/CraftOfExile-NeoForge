package com.cimdy.coe.data.codec.support.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.cimdy.coe.data.codec.support.meta.TagHierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

/**
 * {@link LogicalExpr} 的构造 / 解析 / 编解码工厂 —— 「与 / 或 / 非」机制的数据驱动入口。
 *
 * <p>与「数据驱动」<b>并不冲突</b>：本类负责的是<b>组合机制本身</b>能被数据描述
 * （{@link #codec}）——谓词树可任意层递归；而叶子读什么表（Stat 的 {@code GateTag} 层级、
 * 部位的 {@code ItemTypeHierarchy}）仍由 {@link LeafMatcher} 决定，二者正交。
 *
 * <h2>提供的能力</h2>
 * <ul>
 *   <li><b>委托求值</b>：{@link #allOf} / {@link #anyOf} —— 让已有 sealed 层级的维度（{@code GateExpr} /
 *       {@code SlotExpr}）只写一行转发即可复用本接口的算子语义，<b>且不产生额外对象分配</b>
 *       （签名对同构子项列表做有界泛型，避免逐项转型包装）；</li>
 *   <li><b>字符串 DSL</b>：{@link #parse} / {@link #parseItems} —— 输入 {@code "armor, !shield"} 这样的
 *       {@link String} 直接得到谓词，与 {@link #codec} 的对象形态<b>可以混用</b>（同一棵树里
 *       某处写字符串、某处写 JSON 对象均可），从而让数据侧与代码侧共用同一套判定；</li>
 *   <li><b>扁平降级</b>：{@link #flatAll} —— 把「一个字符串集合 + {@code !} 前缀」这种旧形态
 *       （{@code GateTag.match} 的入参形状）转成标准谓词，使旧调用点零改动接入新机制；</li>
 *   <li><b>递归编解码</b>：{@link #codec(LeafMatcher)} —— JSON 形态（{@code type} / {@code any} /
 *       {@code all} / {@code not} 四字段互斥、全缺省即恒真），可任意层递归嵌套，
 *       与 {@link #parse} 的字符串形态产出同一批算子。</li>
 * </ul>
 *
 * <p><b>数据驱动的归属</b>：本类<b>不参与</b>任何 JSON 反序列化——「从数据读」是叶子判定器
 * 的事（{@link LeafMatcher} 读的那张表，Stat 侧就是 {@code GateTag} 的数据驱动层级：
 * 「元素」名下写「火/电/冰」，传 {@code "fire"} 依然被「元素」命中；部位侧就是
 * {@code ItemTypeHierarchy}）。组合算子只管把叶子按 与 / 或 / 非 合成，与数据层完全解耦，
 * 故同一套算子可同时驱动 {@code GateExpr} 与 {@code SlotExpr}。</p>
 *
 * <h2>DSL 语法（与 {@code GateExpr} 既有语法一致）</h2>
 * <pre>
 *   "fire"                 → 火焰（单键）
 *   "melee", "fire"        → 数组多项 = 与（旧格式）
 *   "fire|cold"            → 或
 *   "!fire"                → 非（排除）
 *   "armor/chest"          → 路径：每段都必须命中（递归子目录语义，如部位 / 装备类型路径）
 *   "(!fire)", "fire|cold" → 任意括号嵌套
 * </pre>
 * 运算符优先级：{@code !} &gt; {@code ,}(与) &gt; {@code |}(或)；全角字符自动归一。
 */
public final class LogicalExprs {

    private LogicalExprs() {}

    // ── 叶子组合器 ──

    /** 直接命中：把键交给 {@link LeafMatcher} 判定（该接口内可自行做层级展开 / 别名词展开 / 路径分段）。 */
    public record Direct<C>(String key, LeafMatcher<C> matcher) implements LogicalExpr<C> {
        @Override
        public boolean matches(C carrier) {
            return this.matcher.matches(carrier, this.key);
        }
    }

    // ── 委托求值（语义唯一真相源，零额外分配）──

    /**
     * 与语义的通用实现（{@link LogicalExpr.All#matches} 的本体）。
     *
     * <p>签名对<b>同构子项列表</b>做有界泛型（{@code E extends LogicalExpr<C>}），调用方直接传入
     * 自身的 {@code List<Node>} 即可，无需逐项转型或包成 {@code List<LogicalExpr<C>>}——
     * 既不破坏已有 sealed 层级，也不引入过渡集合。
     */
    public static <E extends LogicalExpr<C>, C> boolean allOf(List<E> parts, C carrier) {
        for (E part : parts) {
            if (!part.matches(carrier)) return false;
        }
        return true;
    }

    /** 或语义的通用实现（{@link LogicalExpr.Any#matches} 的本体），签名同 {@link #allOf}。 */
    public static <E extends LogicalExpr<C>, C> boolean anyOf(List<E> parts, C carrier) {
        for (E part : parts) {
            if (part.matches(carrier)) return true;
        }
        return false;
    }

    // ── 叶子构造 ──

    /** 单键命中。 */
    public static <C> LogicalExpr<C> leaf(String key, LeafMatcher<C> matcher) {
        return new Direct<>(key, matcher);
    }

    /** 排除（{@code !key}）：该键命中即取反失败。 */
    public static <C> LogicalExpr<C> excluded(String key, LeafMatcher<C> matcher) {
        return LogicalExpr.not(leaf(key, matcher));
    }

    /**
     * 扁平项集合 → 与（legacy 形态通用化）。
     *
     * <p>入参形状即 {@code GateTag.match(scope, required)} 的 {@code required}：
     * 常量项直接命中，{@code !x} 前缀项转为排除，整体为「与」。</p>
     *
     * <p>排除项可用独立的 {@code negated} 判定器（某些维度对「取反」不启用通配/前缀语义，
     * 见 {@code GateTag} 对 {@code !proj:xxx} 的历史处理）。</p>
     *
     * <p><b>热路径</b>：本方法在 Stat 匹配链路上每条目调用一次，故对空集与单元素直接返回，
     * 不构造中间列表；单元素（实际数据中最常见的形态）零分配。
     */
    public static <C> LogicalExpr<C> flatAll(Collection<String> items, LeafMatcher<C> matcher) {
        return flatAll(items, matcher, matcher);
    }

    /** 扁平项集合 → 与（排除项使用独立判定器）。 */
    public static <C> LogicalExpr<C> flatAll(Collection<String> items, LeafMatcher<C> matcher, LeafMatcher<C> negated) {
        if (items == null || items.isEmpty()) return LogicalExpr.always();
        if (items.size() == 1) {
            String only = items.iterator().next();
            return only != null && only.startsWith("!")
                    ? new LogicalExpr.Not<>(new LogicalExprs.Direct<>(only.substring(1), negated))
                    : new LogicalExprs.Direct<>(only, matcher);
        }
        List<LogicalExpr<C>> parts = new ArrayList<>(items.size());
        for (String item : items) {
            parts.add(item != null && item.startsWith("!") ? excluded(item.substring(1), negated) : leaf(item, matcher));
        }
        return LogicalExpr.all(parts);
    }

    // ── 层级解析（叶子判定的通用逻辑，收在本帮助类；接口只留契约） ──

    /**
     * 叶子判定：{@code tag} 是否被作用域 {@code scope} 命中，含其<b>全部后代</b>（深度不限）。
     *
     * <p>本体放在这里而不在 {@link TagHierarchy} 里：「怎么算命中」是与维度无关的通用逻辑，
     * 收在帮助类，接口就只剩「关系数据从哪来」这一份契约；同时任何想用层级解析的维度
     * 都可直接复用，不必各自复制一份递归。
     *
     * <p><b>终止性 = 重复查询检查</b>：{@code path} 记录当前递归路径，回指即停——
     * 环（{@code A→B→A}）不死循环，深链也只走一遍而非指数级重复展开。
     * 因此<b>不需要深度上限</b>：层级本就可以无限往下分。
     */
    public static boolean matches(TagHierarchy hierarchy, String tag, Set<String> scope) {
        return matches(hierarchy, tag, scope, new HashSet<>());
    }

    /** {@link #matches(TagHierarchy, String, Set)} 的递归实现（{@code path} 兼作重复查询检查）。 */
    public static boolean matches(TagHierarchy hierarchy, String tag, Set<String> scope, Set<String> path) {
        if (!path.add(tag)) return false; // 重复查询 / 回指祖先 → 不再展开
        if (scope.contains(tag)) return true; // 直接命中
        // proj: 前缀通配：任何投射物实例标签满足（仅 Stat 作用域启用，物品类型键不启用）
        if (hierarchy.supportsProjectionWildcard() && tag.startsWith("proj:")) {
            for (String t : scope) if (t.startsWith("proj:")) return true;
            return false;
        }
        for (String child : hierarchy.childrenOf(tag)) {
            if (!scope.contains(child) && matches(hierarchy, child, scope, path)) return true;
        }
        return false;
    }

    // ── 字符串 DSL 解析 ──

    /** 由单个表达式字符串构造（等价于 {@code parseItems(List.of(expr), matcher)}）。 */
    public static <C> LogicalExpr<C> parse(String expr, LeafMatcher<C> matcher) {
        return parseItems(List.of(expr), matcher);
    }

    /** 由表达式字符串数组构造：数组整体 = 与，每项解析为片段（旧格式零改动兼容）。 */
    public static <C> LogicalExpr<C> parseItems(Collection<String> items, LeafMatcher<C> matcher) {
        if (items == null || items.isEmpty()) return LogicalExpr.always();
        List<LogicalExpr<C>> parts = new ArrayList<>(items.size());
        for (String item : items) {
            parts.add(parseNode(item, matcher));
        }
        return LogicalExpr.all(parts);
    }

    // ── 编解码（递归 JSON：type / any / all / not 四字段互斥，全缺省即恒真）──

    /**
     * 递归 JSON codec —— 谓词树的可数据描述形态。
     *
     * <p>字段语义：{@code type} 单键叶子 / {@code any}（或）/ {@code all}（与）/ {@code not}（非），
     * 四字段互斥、全缺省即 {@link LogicalExpr#always()}；任意字段内仍可嵌一棵子树，故可任意层递归。
     *
     * <p>与 {@link #parse} 的字符串形态<b>产出同一批算子</b>：数据侧写对象、代码侧写字符串，
     * 最终都落在 {@link LogicalExpr} 的同一套记录上，判定完全一致。
     *
     * <p>本实现是工程内<b>唯一</b>的「与 / 或 / 非 JSON 递归 codec」——维度侧（如 {@code SlotExpr}）
     * 一律经本方法构造自己的 codec，不再各写一份同 schema 的递归解码器。
     */
    public static <C> Codec<LogicalExpr<C>> codec(LeafMatcher<C> matcher) {
        return Codec.recursive("logical_expr", self ->
                RecordCodecBuilder.<LogicalExpr<C>>create(instance -> instance.group(
                        Codec.STRING.optionalFieldOf("type").forGetter(LogicalExprs::typeOf),
                        self.listOf().optionalFieldOf("any").forGetter(LogicalExprs::anyParts),
                        self.listOf().optionalFieldOf("all").forGetter(LogicalExprs::allParts),
                        self.optionalFieldOf("not").forGetter(LogicalExprs::notPart)
                ).apply(instance, (type, any, all, not) -> {
                    if (type.isPresent()) {
                        return leaf(type.get(), matcher);
                    }
                    if (any.isPresent()) {
                        return LogicalExpr.any(any.get());
                    }
                    if (all.isPresent()) {
                        return LogicalExpr.all(all.get());
                    }
                    return not.map(LogicalExpr::not).orElseGet(LogicalExpr::always);
                })));
    }

    // ── codec getter（按类型取样，非该类型即恒空）──

    @SuppressWarnings("unchecked")
    private static <C> Optional<String> typeOf(LogicalExpr<C> expr) {
        return expr instanceof Direct<C> direct ? Optional.of(direct.key()) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static <C> Optional<List<LogicalExpr<C>>> anyParts(LogicalExpr<C> expr) {
        return expr instanceof LogicalExpr.Any<C> any ? Optional.of(any.parts()) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static <C> Optional<List<LogicalExpr<C>>> allParts(LogicalExpr<C> expr) {
        return expr instanceof LogicalExpr.All<C> all ? Optional.of(all.parts()) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static <C> Optional<LogicalExpr<C>> notPart(LogicalExpr<C> expr) {
        return expr instanceof LogicalExpr.Not<C> not ? Optional.of(not.part()) : Optional.empty();
    }

    // ── 解析器（递归下降：! > ,(与) > |(或)）──

    private static <C> LogicalExpr<C> parseNode(String item, LeafMatcher<C> matcher) {
        String normalized = item.trim()
                .replace('，', ',')
                .replace('！', '!')
                .replace('（', '(')
                .replace('）', ')');
        if (normalized.isEmpty()) throw new IllegalArgumentException("empty logical expression: \"" + item + "\"");
        Parser p = new Parser(normalized);
        LogicalExpr<C> node = p.parseAnd(matcher);
        if (!p.atEnd()) {
            throw new IllegalArgumentException("unparsed logical expression tail: \"" + normalized + "\"");
        }
        return node;
    }

    /** 递归下降解析器。 */
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

        <C> LogicalExpr<C> parseAnd(LeafMatcher<C> matcher) {
            List<LogicalExpr<C>> parts = new ArrayList<>();
            parts.add(parseOr(matcher));
            while (true) {
                skipWs();
                if (i >= s.length() || s.charAt(i) != ',') break;
                i++;
                parts.add(parseOr(matcher));
            }
            return LogicalExpr.all(parts);
        }

        <C> LogicalExpr<C> parseOr(LeafMatcher<C> matcher) {
            List<LogicalExpr<C>> parts = new ArrayList<>();
            parts.add(parseNot(matcher));
            while (true) {
                skipWs();
                if (i >= s.length() || s.charAt(i) != '|') break;
                i++;
                parts.add(parseNot(matcher));
            }
            return LogicalExpr.any(parts);
        }

        <C> LogicalExpr<C> parseNot(LeafMatcher<C> matcher) {
            skipWs();
            if (i < s.length() && s.charAt(i) == '!') {
                i++;
                return LogicalExpr.not(parseNot(matcher));
            }
            return parseAtom(matcher);
        }

        <C> LogicalExpr<C> parseAtom(LeafMatcher<C> matcher) {
            skipWs();
            if (i < s.length() && s.charAt(i) == '(') {
                i++;
                LogicalExpr<C> node = parseAnd(matcher);
                skipWs();
                if (i >= s.length() || s.charAt(i) != ')') {
                    throw new IllegalArgumentException("missing ')' in logical expression: \"" + s + "\"");
                }
                i++;
                return node;
            }
            int start = i;
            while (i < s.length() && s.charAt(i) != ',' && s.charAt(i) != '|'
                    && s.charAt(i) != '(' && s.charAt(i) != ')' && s.charAt(i) != '!') {
                i++;
            }
            String key = s.substring(start, i).trim();
            if (key.isEmpty()) throw new IllegalArgumentException("empty key in logical expression: \"" + s + "\"");
            return leaf(key, matcher);
        }
    }
}
