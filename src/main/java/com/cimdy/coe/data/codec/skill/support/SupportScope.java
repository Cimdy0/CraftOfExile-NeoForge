package com.cimdy.coe.data.codec.skill.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 辅助宝石兼容性作用域求值器 —— PoE2 详情页「Allow Type」字段的<b>后缀（逆波兰）布尔表达式</b>。
 *
 * <p><b>数据形态</b>（源自 {@code design/refactor/skill-data/crawled/support_scope.json}，1046 条全量抓取）：
 * Allow Type 是一串 token，形如：
 * <pre>
 *   unleash          = [Unleashable, Spell, AND]                  -> (Unleashable AND Spell)
 *   opening_move     = [Attack, Damage, OR, Travel, AND]          -> ((Attack OR Damage) AND Travel)
 *   refraction_i     = [Banner, Persistent, Buff, AND, AND]       -> ((Banner AND Persistent) AND Buff)
 *   zenith_i         = [Spell, Damage, AND, DegenOnlySpellDamage] -> ((Spell AND Damage) OR DegenOnlySpellDamage)
 *   auto_reload      = [CrossbowAmmoSkill, CrossbowSkill]         -> (CrossbowAmmoSkill OR CrossbowSkill)
 * </pre>
 *
 * <p><b>三条语法事实（已用全量数据实测，见 ADR-008）</b>：
 * <ol>
 *   <li>操作符只有 {@code AND} / {@code OR}，<b>没有 NOT</b>（全量 594 条零命中）。</li>
 *   <li>显式操作符是二元后缀：消费栈顶两个操作数。</li>
 *   <li><b>隐式连接符是 {@code OR} 而不是 AND</b>。多词且无操作符时表示"命中任一即可"。
 *       决定性证据：{@code auto_reload = [CrossbowAmmoSkill, CrossbowSkill]}，
 *       权威 {@code Supported Gem /17}，而权威 type_tags 命中数为 16 + 1 = 17 且交集为 0 ——
 *       AND 语义会得到 0，只有 OR 得到 17。另见 {@code zerphis_infamy = [Attack, Warcry]}
 *       （描述原文 "Supports Attacks and Warcries"，OR=174 ≈ 声明 182，AND=0）、
 *       {@code impact_shockwave = [MeleeSingleTarget, Wind, Attack, AND]}
 *       （描述原文 "Supports Melee Strike Skills <b>and</b> Wind Attack Skills"，
 *        即 {@code MeleeSingleTarget OR (Wind AND Attack)}）。</li>
 * </ol>
 *
 * <p><b>隐式连接符的插入位置</b>：一律<b>尾部补齐</b>（标准后缀求值，扫完后把栈降到 1），
 * 而不是"一遇到第二个词就立刻插入"。二者仅在"显式操作符与多词混排"时有区别，
 * 实测 91 条含显式操作符的表达式里只有 {@code voranas_siege}、
 * {@code impact_shockwave}、{@code zenith_i/ii} 四条受影响，且尾部补齐的拟合度远优于提前插入
 * （平均绝对误差 8.7 vs 60.3，对 91 条）。
 *
 * <p><b>本类是纯函数工具，不依赖 Minecraft / NeoForge 的任何类</b>，可脱离游戏环境做单元测试。
 * 本轮（ADR-008 阶段 1）<b>不修改</b> {@link SupportGem}、{@link SkillLinkResolver} 与任何 UI 代码，
 * 仅为后续阶段提供可独立验证的求值内核。
 */
public final class SupportScope {

    /** 二元与。 */
    public static final String AND = "AND";
    /** 二元或。 */
    public static final String OR = "OR";

    private SupportScope() {
    }

    // ------------------------------------------------------------------ 枚举

    /**
     * 隐式连接符：多词无操作符时用什么把栈降到 1。
     * <b>默认 {@link #OR}</b> —— 由全量权威数据实测确定，见类注释第 3 条。
     */
    public enum Connector {
        AND, OR;

        /** 与 {@link SupportScope#AND} / {@link SupportScope#OR} 同形的 token 名。 */
        public String token() {
            return name();
        }
    }

    /**
     * 未知词（不在技能 type_tags 词表里的词）的求值策略。
     */
    public enum UnknownPolicy {
        /** 未知词按 false 计（默认）。保守：宁可判不可装配，也不误放行。 */
        FALSE,
        /** 未知词按 true 计。宽松：用于"词表缺口不应影响玩家"的降级场景。 */
        TRUE
    }

    /**
     * 三态结果。{@link #UNKNOWN} 用于"该技能根本没有 type_tags 数据"——
     * 此时既不能说允许也不能说拒绝，交由调用方按迁移阶段决定放行还是拦截。
     */
    public enum Result {
        ALLOWED, DENIED, UNKNOWN;

    }

    // -------------------------------------------------------------- 基础判定

    /** 是否为二元操作符 token（严格大写 {@code AND} / {@code OR}）。 */
    public static boolean isOperator(String token) {
        return AND.equals(token) || OR.equals(token);
    }

    // -------------------------------------------------------------- 规范化

    /**
     * 把原始 token 列表规范化为<b>完全显式</b>的后缀表达式：在<b>尾部</b>补齐隐式连接符，
     * 使结果可被纯后缀求值器直接消费。
     *
     * <p>补齐个数 = {@code 扫完后的栈深 - 1}（栈深 &lt;= 1 时不补）。
     *
     * @param raw 原始 token 列表（可为 null / 空，空表示"无约束、支持一切"）
     * @return 规范化后的不可变列表
     */
    public static List<String> normalize(List<String> raw) {
        return normalize(raw, Connector.OR);
    }

    /**
     * @param raw       原始 token 列表
     * @param implicit  隐式连接符（默认 {@link Connector#OR}）
     * @see #normalize(List)
     */
    public static List<String> normalize(List<String> raw, Connector implicit) {
        if (raw == null || raw.isEmpty()) return List.of();
        int depth = 0;
        for (String t : raw) depth += isOperator(t) ? -1 : 1;
        if (depth <= 1) return List.copyOf(raw);
        List<String> out = new ArrayList<>(raw);
        String fill = (implicit == null ? Connector.OR : implicit).token();
        for (int i = 1; i < depth; i++) out.add(fill);
        return List.copyOf(out);
    }

    // -------------------------------------------------------------- 装载期校验

    /**
     * 装载期校验：对原始 token 列表做规范化后检查栈深合法性。
     *
     * <p>规则：
     * <ul>
     *   <li>空列表 = 无约束，合法（PoE2 里 20 条辅助宝石没有 Allow Type 行，语义是"支持一切"）。</li>
     *   <li>任一位置的二元操作符，其出现时栈深必须 {@code >= 2}。</li>
     *   <li>扫描结束时栈深必须 {@code == 1}。</li>
     * </ul>
     *
     * @return 校验结果；失败时带出错 token 的下标、内容与当时栈深
     */
    public static Validation validate(List<String> raw) {
        return validate(raw, Connector.OR);
    }

    /**
     * @see #validate(List)
     */
    public static Validation validate(List<String> raw, Connector implicit) {
        if (raw == null) {
            return new Validation(false, -1, "<null>", 0,
                    "表达式为 null");
        }
        if (raw.isEmpty()) {
            return Validation.OK; // 无约束
        }
        List<String> n = normalize(raw, implicit);
        int depth = 0;
        for (int i = 0; i < n.size(); i++) {
            String t = n.get(i);
            if (isOperator(t)) {
                if (depth < 2) {
                    return new Validation(false, i, t, depth,
                            "二元操作符 '" + t + "' 出现在 token[" + i + "]，此时栈深=" + depth + "（需要 >= 2）；"
                                    + "规范化表达式=" + n);
                }
                depth--;
            } else {
                if (t == null || t.isEmpty()) {
                    return new Validation(false, i, String.valueOf(t), depth,
                            "token[" + i + "] 是空词；规范化表达式=" + n);
                }
                depth++;
            }
        }
        if (depth != 1) {
            return new Validation(false, n.size(), "<end>", depth,
                    "表达式结束时栈深=" + depth + "（应为 1）；规范化表达式=" + n);
        }
        return Validation.OK;
    }

    /**
     * 校验结果。{@link #valid()} 为 true 时其余字段无意义。
     *
     * @param tokenIndex 出错 token 在<b>规范化后</b>列表中的下标（-1 = 表达式本身为 null；{@code size()} = 末尾）
     * @param token      出错 token 内容
     * @param stackDepth 出错时（或结束时）的栈深
     * @param message    可读错误
     */
    public record Validation(boolean valid, int tokenIndex, String token, int stackDepth, String message) {
        static final Validation OK = new Validation(true, -1, "", 1, "OK");

        /** 是否合法。 */
        public boolean valid() {
            return valid;
        }

        /** 单行可读描述，便于日志与 datagen 审计直接打印。 */
        public String describe() {
            return valid ? "OK" : ("非法 Allow Type 表达式：" + message);
        }
    }

    // -------------------------------------------------------------- 预编译

    /** 无任何约束（对应没有 Allow Type 行的辅助宝石）。 */
    public static final Compiled UNCONSTRAINED = new Compiled(List.of(), new String[0], new int[0]);

    /**
     * 预编译一个表达式：校验 + 把 token 折叠成 {@code int[]} 程序（正数 = 词下标，-1 = AND，-2 = OR），
     * 求值时零字符串比较、零装箱。
     *
     * @throws IllegalArgumentException 表达式非法（消息取自 {@link Validation#describe()}）
     */
    public static Compiled compile(List<String> raw) {
        return compile(raw, Connector.OR);
    }

    /**
     * @see #compile(List)
     */
    public static Compiled compile(List<String> raw, Connector implicit) {
        Validation v = validate(raw, implicit);
        if (!v.valid()) throw new IllegalArgumentException(v.describe());
        List<String> n = normalize(raw, implicit);
        if (n.isEmpty()) return UNCONSTRAINED;

        List<String> words = new ArrayList<>();
        int[] prog = new int[n.size()];
        for (int i = 0; i < n.size(); i++) {
            String t = n.get(i);
            if (isOperator(t)) {
                prog[i] = OR.equals(t) ? Compiled.OP_OR : Compiled.OP_AND;
            } else {
                int idx = words.indexOf(t);
                if (idx < 0) {
                    idx = words.size();
                    words.add(t);
                }
                prog[i] = idx;
            }
        }
        return new Compiled(n, words.toArray(new String[0]), prog);
    }

    /**
     * 已编译的表达式。不可变、线程安全（每次 {@code matches} 只分配一个 {@code boolean[]} 栈）。
     */
    public static final class Compiled {
        static final int OP_AND = -1;
        static final int OP_OR = -2;

        private final List<String> normalized;
        private final String[] words;
        private final int[] program;

        private Compiled(List<String> normalized, String[] words, int[] program) {
            this.normalized = normalized;
            this.words = words;
            this.program = program;
        }

        /** 是否无约束（空表达式）。 */
        public boolean unconstrained() {
            return program.length == 0;
        }

        /** 规范化后的 token 列表（可原样写回 datagen JSON）。 */
        public List<String> normalized() {
            return normalized;
        }

        /** 表达式用到的全部词（去重，按出现顺序）—— 用于装载期词表审计。 */
        public List<String> words() {
            return List.of(words);
        }

        /** 未知词策略 = {@link UnknownPolicy#FALSE} 的布尔求值。 */
        public boolean matches(Set<String> typeTags) {
            return matches(typeTags, UnknownPolicy.FALSE, null);
        }

        /**
         * 布尔求值。
         *
         * @param typeTags   技能的 type_tags 集合；{@code null} 视为"无数据"按 {@link UnknownPolicy#FALSE} 处理
         * @param unknownAs  未知词策略
         * @param unknownOut 可选：收集本次遇到的未知词（便于发现词表缺口）；可为 null
         */
        public boolean matches(Set<String> typeTags, UnknownPolicy unknownAs, Set<String> unknownOut) {
            if (unconstrained()) return true;
            boolean unknownAsTrue = unknownAs == UnknownPolicy.TRUE;
            boolean[] stack = new boolean[program.length];
            int sp = 0;
            for (int code : program) {
                if (code == OP_AND) {
                    boolean b = stack[--sp];
                    stack[sp - 1] &= b;
                } else if (code == OP_OR) {
                    boolean b = stack[--sp];
                    stack[sp - 1] |= b;
                } else {
                    String w = words[code];
                    boolean hit = typeTags != null && typeTags.contains(w);
                    if (!hit) {
                        // 仅在"技能确有 type_tags 数据但仍未命中"时记账；
                        // typeTags == null 属于整条数据缺失，交给 evaluate(...) 的 UNKNOWN 分支处理，
                        // 不在此污染未知词表。
                        if (typeTags != null && unknownOut != null) unknownOut.add(w);
                        stack[sp++] = unknownAsTrue;
                    } else {
                        stack[sp++] = true;
                    }
                }
            }
            return stack[0];
        }

        /**
         * 三态求值：{@code typeTags == null} 时返回 {@link Result#UNKNOWN}，
         * 便于调用方区分"明确拒绝"与"数据缺失"。
         */
        public Result evaluate(Set<String> typeTags, Set<String> unknownOut) {
            if (typeTags == null) return Result.UNKNOWN;
            return matches(typeTags, UnknownPolicy.FALSE, unknownOut) ? Result.ALLOWED : Result.DENIED;
        }

        /** 还原成中缀可读形式（调试 / UI 提示用）。 */
        public String toInfix() {
            if (unconstrained()) return "*";
            String[] st = new String[program.length];
            int sp = 0;
            for (int code : program) {
                if (code == OP_AND || code == OP_OR) {
                    String b = st[--sp];
                    String a = st[sp - 1];
                    st[sp - 1] = "(" + a + (code == OP_AND ? " AND " : " OR ") + b + ")";
                } else {
                    st[sp++] = words[code];
                }
            }
            return st[0];
        }

        @Override
        public String toString() {
            return unconstrained() ? "SupportScope[*]" : ("SupportScope[" + toInfix() + "]");
        }
    }

    // -------------------------------------------------------------- 便捷入口

    /** 一次性布尔求值（不缓存编译结果，仅供测试 / 一次性脚本使用）。 */
    public static boolean test(List<String> raw, Set<String> typeTags) {
        return compile(raw).matches(typeTags);
    }

    /** @see #test(List, Set) */
    public static boolean test(List<String> raw, Set<String> typeTags, UnknownPolicy unknownAs, Set<String> unknownOut) {
        return compile(raw).matches(typeTags, unknownAs, unknownOut);
    }
}
