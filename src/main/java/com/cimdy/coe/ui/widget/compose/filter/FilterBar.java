package com.cimdy.coe.ui.widget.compose.filter;

import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.anim.Animator;
import com.cimdy.coe.ui.widget.anim.Easing;
import com.cimdy.coe.ui.widget.attach.Sfx;
import com.cimdy.coe.ui.widget.component.badge.Badge;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.flex.enums.FlexWrap;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 通用筛选栏（第 1 层功能组件，唯一实现点 —— 计划二·六「分类/品质/词缀筛选」）。
 *
 * <p>渲染一组 {@link Filter}：每个 Filter 一行 = 标题小字（SCALE_META）+ 一排选项 chip
 * （{@link Button} toggle active 态，多选）。筛选条件 AND 组合成 {@link Predicate}。
 *
 * <p>行为：
 * <ul>
 *   <li>点 chip → {@link Filter#toggle(String)} 该选项，立即重绘 active 态；</li>
 *   <li>「清除全部」→ 每个 Filter {@link Filter#clear()}，并发 {@code Sfx.emit CLEAR}；</li>
 *   <li>标题行可点击展开 / 收起筛选项，展开收起 Slide+Fade 150ms（计划四·五）。</li>
 * </ul>
 *
 * <p>冻结契约见《M1功能组件实现规格》§2。动画纯视觉，不改布局坐标与命中区。
 */
public class FilterBar<T> extends Node {

    /** 展开 / 收起时长（计划四·五：FilterBar 150ms）。 */
    private static final int MS = 150;

    private final List<Filter<T>> filters = new ArrayList<>();
    private final List<FilterRow<T>> rows = new ArrayList<>();

    private final Button toggleBtn;
    private final Badge countBadge;
    private final AnimatedNode body;
    private boolean expanded = true;
    /** 收起动画结束后隐藏 body 的一次性计时器（无回调原语，自管 tick）。 */
    private Animator pendingHide;

    private FilterBar() {
        this.id = "filter-bar";
        column();
        flex.gapMain = 2;

        // ---- 标题行：展开开关 + 计数徽章 + 清除全部 ----
        Node header = new Node().row();
        header.alignItems(AlignItems.CENTER);
        header.flex.gapMain = 4;

        toggleBtn = Button.of(headerLabel()).styled();
        toggleBtn.flex.height = CoeUi.BAR_HEIGHT;
        toggleBtn.onClick(this::toggleExpanded);
        header.add(toggleBtn);

        Node spacer = new Node().grow(1);
        header.add(spacer);

        countBadge = Badge.of(Component.literal("0"));
        countBadge.visible = false;
        header.add(countBadge);

        Button clearBtn = Button.of(Component.translatable("ui.coe.filter.clear_all")).styled();
        clearBtn.flex.height = CoeUi.BAR_HEIGHT;
        if (clearBtn.labelRenderer() != null) clearBtn.labelRenderer().fixedScale(CoeUi.SCALE_META);
        clearBtn.onClick(this::clearAll);
        header.add(clearBtn);

        add(header);

        // ---- 筛选项主体（展开 / 收起目标）----
        body = new AnimatedNode();
        body.id = "filter-body";
        body.column();
        body.flex.gapMain = 2;
        add(body);
    }

    /** 空筛选栏（随后链式 {@link #filter(Filter)} 追加筛选项）。 */
    public static <T> FilterBar<T> of() { return new FilterBar<>(); }

    // ------------------------------------------------------------------
    // 装配
    // ------------------------------------------------------------------

    /** 追加一个筛选项（标题 + 一排选项 chip）。 */
    public FilterBar<T> filter(Filter<T> f) {
        filters.add(f);

        Node row = new Node().row();
        row.alignItems(AlignItems.CENTER);
        row.flex.gapMain = 4;

        TextRendererI label = Renderers.text(f.label())
                .color(Theme.current().textMuted())
                .fixedScale(CoeUi.SCALE_META);
        Node labelNode = new Node().with(label);
        labelNode.flex.width = 40;
        row.add(labelNode);

        Node chips = new Node().row();
        chips.flex.wrap = FlexWrap.WRAP;
        chips.flex.gapMain = 2;
        chips.flex.gapCross = 2;
        chips.grow(1);

        List<Button> chipList = new ArrayList<>();
        for (Filter.Option opt : f.options()) {
            Button chip = Button.of(opt.label()).styled();
            chip.flex.height = 12;
            chip.padding(0, 3);
            if (chip.labelRenderer() != null) chip.labelRenderer().fixedScale(CoeUi.SCALE_BTN);
            chip.onClick(() -> {
                f.toggle(opt.id());
                refresh();
            });
            chips.add(chip);
            chipList.add(chip);
        }
        row.add(chips);
        body.add(row);
        rows.add(new FilterRow<>(f, chipList));
        refresh();
        return this;
    }

    public List<Filter<T>> filters() { return filters; }

    // ------------------------------------------------------------------
    // 状态
    // ------------------------------------------------------------------

    /** 任一筛选项有选中（= 正在过滤）。 */
    public boolean isActive() {
        for (Filter<T> f : filters) if (!f.selected().isEmpty()) return true;
        return false;
    }

    /** 清空全部筛选项（发 Sfx CLEAR）。 */
    public void clearAll() {
        for (Filter<T> f : filters) f.clear();
        Sfx.emit(this, Sfx.Kind.CLEAR);
        refresh();
    }

    /** AND 组合谓词：所有 Filter 都命中才保留；无 Filter 时恒真。 */
    public Predicate<T> predicate() {
        return item -> {
            for (Filter<T> f : filters) if (!f.test(item)) return false;
            return true;
        };
    }

    // ------------------------------------------------------------------
    // 展开 / 收起（Slide + Fade 150ms）
    // ------------------------------------------------------------------

    private void toggleExpanded() {
        expanded = !expanded;
        toggleBtn.label(headerLabel());
        if (expanded) {
            body.visible = true;
            body.fadeTo(1F, MS);
            body.slideTo(0, 0, MS);
        } else {
            body.fadeTo(0F, MS);
            body.slideTo(0, -6, MS);
            if (pendingHide == null) {
                pendingHide = new Animator(1F, 0F, MS, Easing.EASE_OUT_CUBIC);
                pendingHide.onComplete(() -> body.visible = false);
            }
        }
    }

    private Component headerLabel() {
        return Component.translatable("ui.coe.filter.title")
                .append(Component.literal(expanded ? " ▲" : " ▼"));
    }

    // ------------------------------------------------------------------

    /** 重绘每个 chip 的 active 态与计数徽章（chip active 由 Filter.selected() 驱动，不靠 toggle 模式）。 */
    private void refresh() {
        int total = 0;
        for (FilterRow<T> row : rows) {
            Set<String> sel = row.filter.selected();
            total += sel.size();
            List<Filter.Option> opts = row.filter.options();
            for (int i = 0; i < row.chips.size() && i < opts.size(); i++) {
                row.chips.get(i).active(sel.contains(opts.get(i).id()));
            }
        }
        countBadge.text(Component.literal(String.valueOf(total)));
        countBadge.visible = total > 0;
    }

    @Override
    public void tick() {
        if (pendingHide != null) {
            pendingHide.tick();
            if (!pendingHide.isRunning()) pendingHide = null;
        }
        super.tick();
    }

    /** 一行筛选：Filter 数据 + 对应的 chip 按钮。 */
    private record FilterRow<T>(Filter<T> filter, List<Button> chips) {}
}
