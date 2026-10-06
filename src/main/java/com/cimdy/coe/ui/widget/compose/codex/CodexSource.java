package com.cimdy.coe.ui.widget.compose.codex;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import com.cimdy.coe.ui.widget.compose.search.SearchLevel;

/**
 * 可查询数据源（CodexDB v2 游戏内数据库的<b>类型注册单元</b>，冻结契约 §4）。
 *
 * <p>每一种可浏览的数据类型（装备条目 / 技能条目 / 附魔 / 掉落权重……）实现一个本接口：
 * <b>Schema 固定一次</b>（这一类型长什么样，由 {@link #schema()} 描述），
 * <b>条目集合运行时动态</b>（{@link #entries()} 每屏/每次刷新重新枚举，数据包改了即变）。
 * 新增数据源 = 新写一个实现类并注册，不改任何布局/列表/详情代码。
 *
 * <p><b>两级分类键（v2）</b>：
 * <ul>
 *   <li>{@link #group()} —— 一级分类键，即左侧 {@link com.cimdy.coe.ui.widget.compose.codex.NavTree}
 *       一级组 id（如 {@code "equipment"}/{@code "skill"}/{@code "enchant"}/{@code "drop"}）；</li>
 *   <li>{@link #subGroupKey(Object)} —— 二级分类键，即 NavTree 二级叶子 id
 *       （无二级分类返回 {@link Optional#empty()}）。</li>
 * </ul>
 * 二级键取代了 M1 旧签名 {@code categoryKey(T)}（本次按新计划升级删除）。
 *
 * <p><b>数据无关</b>：本接口只声明能力，不关心 {@code T} 是 Item、SkillEntry 还是 EnchantModifier。
 * 渲染与搜索由 {@link CodexSchema} + 消费方（CodexBrowser / SortableList）完成。
 *
 * @param <T> 该数据源的条目类型
 */
public interface CodexSource<T> {

    /** 类型唯一 id（注册键 / 程序化切换用，如 {@code "item"}、{@code "skill"}）。 */
    String id();

    /** 类型显示名（调用方给 {@code Component.translatable}）。 */
    Component displayName();

    /** 当前可枚举的全部条目（运行时动态；纯客户端只读，不回写）。 */
    List<T> entries();

    /**
     * 分层搜索匹配：判断单条目在指定 {@link SearchLevel} 下是否命中用户输入（计划 979-991）。
     * 实现方按层给出判定——
     * <ul>
     *   <li>{@link SearchLevel#CATEGORY}：匹配该条目所属分类键（{@link #group()} /
     *       {@link #subGroupKey(Object)} / 类型名）；</li>
     *   <li>{@link SearchLevel#ENTRY}：匹配条目名 / 列表行字段（既有按名匹配逻辑）；</li>
     *   <li>{@link SearchLevel#DETAIL}：匹配详情 / tooltip 文本（schema fields 取值或源各自详情文本）。</li>
     * </ul>
     *
     * @param entry 一条目
     * @param query 用户输入的原始串（非空；空串由消费方视为命中全部）
     * @param level 本次判定的层
     */
    boolean matches(T entry, String query, SearchLevel level);

    /** 这一类型的渲染描述：label / 图标 / 列表列 / 详情字段。 */
    CodexSchema<T> schema();

    /**
     * 一级分类键（NavTree 一级组 id，如 {@code "equipment"}/{@code "skill"}/{@code "enchant"}/{@code "drop"}）。
     * 同一一级组下可注册多个源；CodexScreen 据此把源挂进 NavTree 对应组。
     */
    String group();

    /**
     * 二级分类键（NavTree 二级叶子 id；无二级分类返回 {@link Optional#empty()}）。
     * 取代原 M1 的 {@code categoryKey(T)}。{@code empty()} 表示该类型不细分——
     * 消费方（CodexBrowser）据此把该源条目在对应一级组下直接全列（不编造映射）。
     */
    Optional<String> subGroupKey(T entry);
}
