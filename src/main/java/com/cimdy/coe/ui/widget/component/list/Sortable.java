package com.cimdy.coe.ui.widget.component.list;

import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 排序能力（第 1 层功能组件，唯一实现点 —— 计划二·六「排序」）。
 *
 * <p>把「可排序的列表」需要的状态与比较器收敛成一个接口：消费方（{@code SortableList} /
 * {@code SortHeader}）只面对本接口，不认识具体业务数据。方向三态循环
 * （升序 → 降序 → 关闭）由 {@code SortHeader} 驱动，本接口只负责持有状态并推导
 * {@link Comparator}。
 *
 * <p>冻结契约见《M1功能组件实现规格》§2 —— 跨 Subagent 引用以本签名为准。
 *
 * @param <T> 被排序的数据条目类型
 */
public interface Sortable<T> {

    /** 排序方向。{@code null} 表示「关闭排序」（不排序）。 */
    enum Direction { ASC, DESC }

    /**
     * 一个排序键：列 id + 表头文案 + 该列的比较器。
     *
     * @param id         列标识（稳定字符串，持久化 / 调试用）
     * @param label      表头显示文案（{@code SortHeader} 直接渲染）
     * @param comparator 该列的自然比较器；DESC 方向由 {@link #comparator()} 自动取反
     */
    record SortKey<T>(String id, Component label, Comparator<T> comparator) {}

    /** 注册可选排序键（表头据此渲染列）。 */
    void setSortKeys(List<SortKey<T>> keys);

    /** 当前排序键列表。 */
    List<SortKey<T>> sortKeys();

    /** 当前排序键下标；{@code -1} = 关闭排序。 */
    int sortIndex();

    /** 当前方向；{@code null} = 关闭排序。 */
    Direction sortDirection();

    /**
     * 设置排序状态。
     *
     * @param keyIndex 键下标；{@code -1} 表示关闭排序
     * @param dir      方向；{@code keyIndex=-1} 时忽略
     */
    void setSort(int keyIndex, Direction dir);

    /**
     * 由当前状态推导比较器（唯一实现点，消费方不再自己写方向取反）。
     *
     * <p>关闭排序（{@code sortIndex()<0} 或 {@code sortDirection()==null}）时返回
     * {@code null} —— 消费方约定 {@code null} = 保持原序，不排序。
     * DESC 方向在键比较器上自动 {@link Comparator#reversed()}。
     */
    default Comparator<T> comparator() {
        int idx = sortIndex();
        Direction dir = sortDirection();
        if (idx < 0 || dir == null) return null;
        List<SortKey<T>> keys = sortKeys();
        if (keys == null || idx >= keys.size()) return null;
        Comparator<T> c = keys.get(idx).comparator();
        if (c == null) return null;
        return dir == Direction.DESC ? c.reversed() : c;
    }
}
