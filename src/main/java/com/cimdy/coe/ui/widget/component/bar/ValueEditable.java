package com.cimdy.coe.ui.widget.component.bar;

import com.cimdy.coe.ui.widget.component.assoc.Property;

/**
 * 能力接口：具备「轨道 + 填充 + 可拖拽把手编辑数值」的组件。
 *
 * <p>同样是「功能性组件」基石：复合体（如 {@code Bar} / {@code ScrollBar}）只认这个能力，
 * 因此同一个实现了它的原语（{@code Slider}）既能被当「数值条」也能被当「滚动条」复用——
 * 原语与名字无关，可跨复合体复用。复合体负责把它接到某个具名关联（数值 / 滚动偏移）上。
 */
public interface ValueEditable {

    /** 当前数值（落在 {@link #range()} 之内）。 */
    Property<Double> value();

    /** 当前取值范围。 */
    Property<Range> range();

    /** 切换竖直方向。 */
    void vertical();

    /** 条厚（像素）。 */
    void thickness(int px);
}
