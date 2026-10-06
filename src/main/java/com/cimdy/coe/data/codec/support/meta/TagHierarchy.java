package com.cimdy.coe.data.codec.support.meta;

import java.util.Set;

/**
 * 标签层级解析器：把「父标签辖制子标签」的判定从 Stat 作用域抽象为通用接口，
 * 供 {@link GateExpr} 在任意字符串标签维度上复用同一套「与 / 或 / 非 + 父辖子」表达式引擎。
 *
 * <p>两个实现：
 * <ul>
 *   <li>{@code GateTag.HIERARCHY}：Stat 作用域层级，启用 {@code proj:} 前缀通配；</li>
 *   <li>{@code ItemTypeHierarchy.resolver()}：装备类型树层级，不启用 {@code proj:} 通配。</li>
 * </ul>
 *
 * <p><b>适用范围（重要）</b>：本接口只描述<b>父子级（分类 / 归属）关系</b>——「元素」辖「火」。
 * <b>不适用于功能关系</b>：像「火能造成灼烧」「冰缓会拖慢」这类因果 / 能力关系不是父子级关系，
 * <b>不能</b>硬挂进层级表来表达，那属于伤害转换、异常挂载等别的机制；硬塞进来会让
 * 「上位包含」的语义失真（祖先被下位因果反向污染）。
 *
 * <p><b>契约：全部后代（传递闭包），深度不限。</b>
 * 一个父类别名下挂的子标签自身还可以继续当父类别，往下分任意层，问祖先依然命中深层后代。
 * 终止性由「重复查询检查」保证（同一标签至多展开一次），故链再长也只走一遍；
 * 只有<b>环</b>（{@code A→B→A}）才是有害形态，由 {@code GateTag} 在层级加载期拦下。
 */
public interface TagHierarchy {

    /**
     * 返回 {@code tag} 的<b>全部后代</b>（传递闭包，含深层；深度不限，无后代返回空集合，不得为 null）。
     *
     * <p>「怎样算命中」<b>不在本接口里实现</b>，而是收在帮助类
     * {@code com.cimdy.coe.data.codec.support.logic.LogicalExprs#matches}——那是与维度无关的
     * 通用逻辑；本接口只保留「关系数据从哪来」这一份契约，保持纯净。
     */
    Set<String> childrenOf(String tag);

    /**
     * 是否启用 {@code proj:} 前缀通配（任何 {@code proj:xxx} 实例标签满足 {@code proj:} 要求）。
     * Stat 作用域启用（投射物实例动态生成）；物品类型键不启用。
     */
    default boolean supportsProjectionWildcard() {
        return false;
    }
}
