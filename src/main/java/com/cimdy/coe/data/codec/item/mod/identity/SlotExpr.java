package com.cimdy.coe.data.codec.item.mod.identity;

import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.support.logic.LeafMatcher;
import com.cimdy.coe.data.codec.support.logic.LogicalExpr;
import com.cimdy.coe.data.codec.support.logic.LogicalExprs;
import com.mojang.serialization.Codec;
import com.cimdy.coe.CraftOfExile;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.List;

/**
 * 装备部位谓词（可组合的与 / 或 / 非）—— 供冲突表声明「本组只在哪些部位上成立」。
 *
 * <p><b>机制来源</b>：与 / 或 / 非 的组合语义不在本类内定义，而是复用于
 * {@link LogicalExpr}（通用谓词接口）——本类只负责「部位这一维度」的<b>叶子判定</b>
 * （经 {@link #LEAF} 委托 {@code ItemTypeChecker} 的族别名展开与 {@code ItemTypeHierarchy} 的祖先展开）。
 * 故部位谓词与 Stat 标签作用域谓词（{@code GateExpr}）本质是同一套机制的两个维度。
 *
 * <p>存在理由：源库（poe2db）的词缀互斥是按<b>部位分别列出</b>的，同一对词缀可能「胸甲上互斥、
 * 头盔上可共存」。若冲突表不带部位约束而全局生效，就会<b>过度约束</b>（在头盔上错误地禁掉
 * 合法组合）。本谓词让一张表自带作用域，从而还原源库设定、消除过度约束。</p>
 *
 * <h2>判定基底</h2>
 * 叶类型判定一律走 {@link ItemTypeChecker#hasType}（<b>不</b>直接 {@code types().contains(...)}）——
 * 唯有它会展开族别名（{@code sword → one_hand_sword / two_hand_sword}）并经
 * {@code ItemTypeHierarchy} 派生祖先类别，故写祖先名（{@code armor} / {@code weapon}）即可覆盖全部子类。
 *
 * <h2>组合语义</h2>
 * <ul>
 *   <li>{@link Type}——单个类型键 / 族名命中；</li>
 *   <li>{@link Any}——任一子谓词成立（或）；</li>
 *   <li>{@link All}——全部子谓词成立（与）；</li>
 *   <li>{@link Not}——子谓词取反（非）；</li>
 *   <li>{@link Always}——无约束（缺省），等价于旧的「全局表」。</li>
 * </ul>
 *
 * <h2>数据形态</h2>
 * <pre>{ "type": "chest" }                      单部位
 * { "any": [ {"type":"chest"}, {"type":"legs"} ] }   或
 * { "all": [ {"type":"armor"}, {"not":{"type":"shield"}} ] }  与 + 非</pre>
 * 字段全缺省（或空对象 {@code {}}）即 {@link #ALWAYS}。
 *
 * <h2>字符串形态（等价写法）</h2>
 * 同一棵谓词树也可用 {@link String} 表达（{@link #parse}），二者产出<b>完全等价</b>的判定：
 * <pre>{ "type": "chest" }                    ≡  "chest"
 * { "any": [ {"type":"chest"}, {"type":"legs"} ] }  ≡  "chest|legs"
 * { "all": [ {"type":"armor"}, {"not":{"type":"shield"}} ] }  ≡  "armor, !shield"</pre>
 * 路径型键（{@code armor/chest}，递归子目录语义）只能在字符串形态里书写，
 * 由 {@link #LEAF} 按「每段都必须落在（叶 ∪ 祖先）中」判定。
 */
public sealed interface SlotExpr extends LogicalExpr<ModifiableState> {

    /**
     * 部位维度唯一的叶子判定器：单键走 {@code ItemTypeChecker.hasType}（族别名 + 祖先展开），
     * 含 {@code /} 的路径键改走 {@code ItemTypeHierarchy.isWithin}（递归子目录语义）。
     */
    LeafMatcher<ModifiableState> LEAF = (state, key) -> key.indexOf('/') >= 0
            ? ItemTypeHierarchy.isWithin(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, key), state.types())
            : ItemTypeChecker.hasType(state, key);

    /**
     * 对给定载体求谓词；空载体视为「无约束可命中」（仅用于无载体场景，如图鉴展示）。
     * 组合语义（与 / 或 / 非）见 {@link LogicalExpr}。
     */
    @Override
    boolean matches(ModifiableState state);

    /** 无约束常量：任何载体都成立（缺省值，等价于旧的全局表行为）。 */
    SlotExpr ALWAYS = new Always();

    // ── 字符串 DSL 入口（与 {@link #CODEC} 的对象形态等价）──

    /**
     * 由部位表达式字符串构造（运算符：{@code ,} 与 / {@code |} 或 / {@code !} 非 / 括号嵌套）。
     *
     * <p>例：{@code parse("armor/chest, !shield")} = 胸甲类装备 且 非盾牌。</p>
     */
    static SlotExpr parse(String expr) {
        return from(LogicalExprs.parse(expr, LEAF));
    }

    /** 由表达式字符串数组构造：数组整体 = 与（旧 {@code types} 数组形态的直接迁移路径）。 */
    static SlotExpr parseAll(Collection<String> items) {
        return from(LogicalExprs.parseItems(items, LEAF));
    }

    /**
     * 通用谓词 → 部位谓词：把 {@link LogicalExpr} 树落到本 sealed 层级，
     * 使字符串 DSL 与 {@link #CODEC} 的对象形态产出同一批类型（保持 equals / 序列化 round-trip 稳定）。
     */
    static SlotExpr from(LogicalExpr<ModifiableState> expr) {
        if (expr instanceof LogicalExpr.Always<ModifiableState>) {
            return ALWAYS;
        }
        if (expr instanceof LogicalExpr.All<ModifiableState> all) {
            return new All(all.parts().stream().map(SlotExpr::from).toList());
        }
        if (expr instanceof LogicalExpr.Any<ModifiableState> any) {
            return new Any(any.parts().stream().map(SlotExpr::from).toList());
        }
        if (expr instanceof LogicalExpr.Not<ModifiableState> not) {
            return new Not(SlotExpr.from(not.part()));
        }
        if (expr instanceof LogicalExprs.Direct<ModifiableState> direct) {
            return new Type(direct.key());
        }
        throw new IllegalArgumentException("unsupported logical expr for slots: " + expr);
    }

    // ── 构造便捷 ──────────────────────────────────────────────────────────

    static SlotExpr type(String type) {
        return new Type(type);
    }

    /** 或：多个类型键 / 族名任一命中（与预设 {@code types} 数组同义）。 */
    static SlotExpr anyTypes(List<String> types) {
        return new Any(types.stream().map(Type::new).map(SlotExpr.class::cast).toList());
    }

    static SlotExpr any(List<SlotExpr> parts) {
        return parts.isEmpty() ? ALWAYS : new Any(List.copyOf(parts));
    }

    static SlotExpr all(List<SlotExpr> parts) {
        return parts.isEmpty() ? ALWAYS : new All(List.copyOf(parts));
    }

    static SlotExpr not(SlotExpr part) {
        return new Not(part);
    }

    // ── 实现 ─────────────────────────────────────────────────────────────

    /** 恒真（无部位约束）。 */
    record Always() implements SlotExpr {
        @Override
        public boolean matches(ModifiableState state) {
            return true;
        }
    }

    /** 单类型键 / 族名 / 路径命中（经 {@link #LEAF} 展开别名、祖先，路径则逐段判定）。 */
    record Type(String type) implements SlotExpr {
        @Override
        public boolean matches(ModifiableState state) {
            return LEAF.matches(state, this.type);
        }
    }

    /** 或：任一子谓词成立即真（语义见 {@link LogicalExpr#Any}）。 */
    record Any(List<SlotExpr> parts) implements SlotExpr {
        @Override
        public boolean matches(ModifiableState state) {
            return LogicalExprs.anyOf(this.parts, state);
        }
    }

    /** 与：全部子谓词成立才真（语义见 {@link LogicalExpr#All}）。 */
    record All(List<SlotExpr> parts) implements SlotExpr {
        @Override
        public boolean matches(ModifiableState state) {
            return LogicalExprs.allOf(this.parts, state);
        }
    }

    /** 非：子谓词取反。 */
    record Not(SlotExpr part) implements SlotExpr {
        @Override
        public boolean matches(ModifiableState state) {
            return !this.part.matches(state);
        }
    }

    // ── 编解码（复用通用递归 codec，四个字段互斥、全缺省即 ALWAYS）────────

    /**
     * 递归 JSON codec —— 复用 {@link LogicalExprs#codec(LeafMatcher)}（工程内唯一的
     * 「与 / 或 / 非」递归解码器），仅在本维度上做一次类型的收/放：
     * 解码经 {@link #from} 落到本 sealed 层级、编码直接以本维度身份写回
     * （{@code SlotExpr} 本身即 {@link LogicalExpr}）。
     *
     * <p>故字段与通用实现完全一致（{@code type} / {@code any} / {@code all} / {@code not}，
     * 全缺省即 {@link #ALWAYS}），且 round-trip 稳定（产物与本类型构造物 {@code equals}）。
     */
    Codec<SlotExpr> CODEC = LogicalExprs.codec(LEAF).xmap(SlotExpr::from, expr -> expr);
}
