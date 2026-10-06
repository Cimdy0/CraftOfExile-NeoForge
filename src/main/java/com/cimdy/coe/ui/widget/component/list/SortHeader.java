package com.cimdy.coe.ui.widget.component.list;

import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.ColorRendererI;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 可点击表头行（第 1 层功能组件，唯一实现点 —— 计划二·六「排序」）。
 *
 * <p>绑定一个 {@link Sortable}，渲染一行等宽列：每列 = 标题（SCALE_META）+ ▲▼ 指示。
 * 点击某列在该列上循环 <b>升序 → 降序 → 关闭</b>；点击其它列直接切到该列升序。
 * 指示用 {@link Theme#current()#accent()} 着色，列点击 Pulse 150ms + 指示淡切 150ms
 * （计划四·五动画规格表）。
 *
 * <p>冻结契约见《M1功能组件实现规格》§2。动画纯视觉（不动布局坐标），命中区仍按列布局矩形。
 */
public class SortHeader extends Node {

    /** 指示淡切 / 列点击 Pulse 时长（计划四·五：SortHeader 150ms）。 */
    private static final int MS = 150;

    private final Sortable<Object> sortable;
    private final List<SortKeyCell> columns = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private SortHeader(Sortable<?> sortable, List<Sortable.SortKey<?>> keys) {
        this.sortable = (Sortable<Object>) sortable;
        this.id = "sort-header";
        row();
        flex.height = 14;
        flex.alignItems = AlignItems.CENTER;
        flex.gapMain = 1;

        List<Sortable.SortKey<Object>> typed = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            Sortable.SortKey<Object> key = new Sortable.SortKey<>(keys.get(i).id(), keys.get(i).label(),
                    (java.util.Comparator<Object>) keys.get(i).comparator());
            typed.add(key);
            addColumn(i, key);
        }
        this.sortable.setSortKeys(typed);
        refresh();
    }

    /** 装配一行表头：绑定排序状态 + 列定义。 */
    public static <T> SortHeader of(Sortable<T> sortable, List<Sortable.SortKey<T>> keys) {
        return new SortHeader(sortable, new ArrayList<>(keys));
    }

    // ------------------------------------------------------------------
    // 装配
    // ------------------------------------------------------------------

    private void addColumn(int index, Sortable.SortKey<Object> key) {
        final int idx = index;

        AnimatedNode column = new AnimatedNode() {
            @Override
            protected boolean onMouseClicked(double lx, double ly, int button) {
                if (button != 0) return false;
                onClickColumn(idx);
                return true;
            }
        };
        column.id = "sort-col";
        column.row();
        column.grow(1);
        column.alignItems(AlignItems.CENTER);
        column.padding(0, 2);

        // 悬停高亮底（透明常态 + optionHoverBg 悬浮，smooth 缓动）
        ColorRendererI colBg = Renderers.color(Theme.current().surfaceTransparent())
                .backgroundHover(Theme.current().optionHoverBg())
                .rounding(Theme.current().radiusSm())
                .smooth(true);
        column.with(colBg);

        TextRendererI title = Renderers.text(key.label())
                .align(Align.LEFT)
                .vAlign(VAlign.MIDDLE)
                .fixedScale(CoeUi.SCALE_META)
                .color(Theme.current().textMuted());
        Node titleNode = new Node().with(title);
        titleNode.grow(1);

        // ▲▼ 指示：独立 AnimatedNode，淡切只作用于字形，不连标题
        AnimatedNode indicator = new AnimatedNode();
        indicator.id = "sort-indicator";
        indicator.anim().alpha = 0F;            // 未选中时不可见（不占布局，保持列宽稳定）
        indicator.flex.width = 8;
        TextRendererI glyph = Renderers.text(Component.empty())
                .align(Align.CENTER)
                .vAlign(VAlign.MIDDLE)
                .fixedScale(CoeUi.SCALE_META)
                .color(Theme.current().accent());
        indicator.with(glyph);

        column.add(titleNode);
        column.add(indicator);
        add(column);
        columns.add(new SortKeyCell(column, title, indicator, glyph));
    }

    // ------------------------------------------------------------------
    // 交互：点击列循环 升 → 降 → 关闭
    // ------------------------------------------------------------------

    private void onClickColumn(int i) {
        int prevIdx = sortable.sortIndex();
        Sortable.Direction prevDir = sortable.sortDirection();

        int newIdx;
        Sortable.Direction newDir;
        if (i == prevIdx) {
            // 同列循环：ASC → DESC → 关闭
            if (prevDir == Sortable.Direction.ASC) {
                newIdx = i; newDir = Sortable.Direction.DESC;
            } else if (prevDir == Sortable.Direction.DESC) {
                newIdx = -1; newDir = null;
            } else {
                newIdx = i; newDir = Sortable.Direction.ASC;
            }
        } else {
            // 点新列：直接升序
            newIdx = i; newDir = Sortable.Direction.ASC;
        }

        sortable.setSort(newIdx, newDir);
        refresh();
        columns.get(i).column.pulse(0.06F, MS);   // 列点击 Pulse 150ms
    }

    /** 按 Sortable 当前状态重绘各列标题色与指示字形 / 淡切。 */
    private void refresh() {
        int activeIdx = sortable.sortIndex();
        Sortable.Direction dir = sortable.sortDirection();
        for (int i = 0; i < columns.size(); i++) {
            SortKeyCell c = columns.get(i);
            boolean active = (i == activeIdx && dir != null);
            c.title.color(active ? Theme.current().accent() : Theme.current().textMuted());
            c.glyph.setLine(0, active
                    ? Component.literal(dir == Sortable.Direction.DESC ? "▼" : "▲")
                    : Component.empty());
            // 指示淡切 150ms：激活列淡入，其余淡出
            c.indicator.fadeTo(active ? 1F : 0F, MS);
        }
    }

    // ------------------------------------------------------------------

    /** 一列的渲染器句柄（重绘用）。 */
    private record SortKeyCell(AnimatedNode column, TextRendererI title,
                                AnimatedNode indicator, TextRendererI glyph) {}
}
