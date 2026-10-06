package com.cimdy.coe.ui.widget.component.button;

import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.anim.Animator;
import com.cimdy.coe.ui.widget.anim.Easing;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * 分段互斥开关（第 1 层功能组件，唯一实现点 —— 计划一 SegmentToggle）：一排互斥档位。
 *
 * <p>选中态不靠给每个按钮套 skin，而是一个整块背景（{@code activeBg}）随选中项
 * 120ms 横向滑到新档位下方（计划四·五：选中块滑动 120ms）。单元格只画文字。
 *
 * <pre>{@code
 * SegmentToggle seg = SegmentToggle.of(
 *         Component.literal("简单"), Component.literal("详细"), Component.literal("自定义"))
 *         .onSelect(i -> mode = i);
 * }</pre>
 *
 * <p>冻结契约见《M1功能组件实现规格》§2。
 */
public class SegmentToggle extends Node {

    /** 选中块滑动时长（计划四·五：SegmentToggle 120ms）。 */
    private static final int MS = 120;

    private final List<Button> cells = new ArrayList<>();
    /** 选中块横向偏移（px，相对本节点左边），120ms 补间。 */
    private final Animator blockAnim = new Animator(0F, 0F, MS, Easing.EASE_IN_OUT_CUBIC);

    private int selectedIndex = 0;
    private IntConsumer onSelect;

    private SegmentToggle(List<Component> labels) {
        this.id = "segment-toggle";
        row();
        flex.height = CoeUi.BAR_HEIGHT;
        flex.gapMain = 0;

        for (int i = 0; i < labels.size(); i++) {
            final int idx = i;
            Button cell = Button.of(labels.get(i));
            cell.id = "seg-cell";
            cell.grow(1);
            cell.padding(0, 4);
            TextRendererI t = cell.labelRenderer();
            if (t != null) {
                t.align(Align.CENTER)
                 .vAlign(VAlign.MIDDLE)
                 .fixedScale(CoeUi.SCALE_BTN)
                 .color(Theme.current().textMuted())
                 .colorHover(Theme.current().textHover());
            }
            cell.onClick(() -> setSelected(idx, true));
            add(cell);
            cells.add(cell);
        }
        updateCellColors();
    }

    /** 一排互斥档位。 */
    public static SegmentToggle of(Component... labels) {
        return new SegmentToggle(new ArrayList<>(java.util.Arrays.asList(labels)));
    }

    // ------------------------------------------------------------------
    // 行为
    // ------------------------------------------------------------------

    /** 选中回调，参数是新档位下标。 */
    public SegmentToggle onSelect(IntConsumer c) {
        this.onSelect = c;
        return this;
    }

    /** 以代码方式选档（不触发 {@link #onSelect}）。 */
    public void select(int index) {
        setSelected(index, false);
    }

    public int selectedIndex() { return selectedIndex; }

    private void setSelected(int index, boolean fire) {
        if (index < 0 || index >= cells.size()) return;
        selectedIndex = index;
        updateCellColors();
        if (fire && onSelect != null) onSelect.accept(index);
    }

    private void updateCellColors() {
        for (int i = 0; i < cells.size(); i++) {
            TextRendererI t = cells.get(i).labelRenderer();
            if (t != null) t.color(i == selectedIndex
                    ? Theme.current().textBright()
                    : Theme.current().textMuted());
        }
    }

    // ------------------------------------------------------------------
    // 渲染：选中块画在单元格文字下方
    // ------------------------------------------------------------------

    @Override
    public void render(GuiGraphicsExtractor g, RenderContext ctx, boolean parentDisabled) {
        if (!visible) return;
        if (selectedIndex >= 0 && selectedIndex < cells.size()) {
            Button sel = cells.get(selectedIndex);
            int bw = sel.width;
            if (bw > 0) {
                int bx = Math.round(absX + blockAnim.value());
                Draw.roundRect(g, bx, absY, bw, height,
                        Theme.current().radiusSm(), Theme.current().activeBg());
            }
        }
        super.render(g, ctx, parentDisabled);
    }

    @Override
    public void tick() {
        // 把选中块吸附到当前档位左边（布局稳定后即静止，无空转）
        if (selectedIndex >= 0 && selectedIndex < cells.size()) {
            Button sel = cells.get(selectedIndex);
            blockAnim.retarget(sel.absX - absX);
        }
        blockAnim.tick();
        super.tick();
    }
}
