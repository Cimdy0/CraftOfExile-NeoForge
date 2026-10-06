package com.cimdy.coe.ui.widget.compose.filter;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Set;

/**
 * 单个筛选项（第 1 层能力接口，唯一实现点 —— 计划二·六「分类/品质/词缀筛选」）。
 *
 * <p><b>筛选项即数据</b>：具体类别 / 品质 / 词缀的候选与判定全部由实现方提供，
 * {@link FilterBar} 只负责把它渲染成一排可点的 chip 并组合谓词，绝不把候选写死在组件里。
 *
 * <p>多选语义：{@link #selected()} 为空集 = <b>全选</b>（不过滤），
 * 此时 {@link #test(Object)} 恒为 {@code true}。
 *
 * @param <T> 被筛选的数据条目类型
 */
public interface Filter<T> {

    /** 候选选项（chip 渲染源）。 */
    record Option(String id, Component label) {}

    /** 筛选项稳定 id（调试 / 持久化用）。 */
    String id();

    /** 筛选项标题（FilterBar 每行左侧的小字）。 */
    Component label();

    /** 候选值列表。 */
    List<Option> options();

    /** 当前选中的选项 id 集合；空 = 全选。 */
    Set<String> selected();

    /** 切换某个选项的选中态（多选）。 */
    void toggle(String optionId);

    /** 清空全部选中（= 全选）。 */
    void clear();

    /**
     * 判定一条数据是否命中本筛选项。
     *
     * <p>约定：{@code selected()} 为空时恒返回 {@code true}（全选不过滤）。
     */
    boolean test(T item);
}
