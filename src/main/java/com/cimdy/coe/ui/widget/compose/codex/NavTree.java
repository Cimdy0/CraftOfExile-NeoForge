package com.cimdy.coe.ui.widget.compose.codex;

import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.component.badge.Badge;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.core.FlexStyle;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.screen.CoeUi;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 可折叠二级分类导航（计划 §3.3 / 规格 §1）：一级组行点击展开/收起，
 * 子区展开 {@code fadeIn(150)} + slide ±6px，收起 150ms 后隐藏。
 *
 * <p><b>数据无关</b>：只吃 {@link Component} / {@link Leaf}，不认识装备 / 技能 / 词缀等任何业务字段。
 * 潜在复用方：未来任何分类浏览。
 *
 * <p>动画只用 {@link AnimatedNode} 原语，时长 150ms（规格表 NavTree 行）。
 * 取色一律走 {@link Theme} 令牌，不写死色值。
 *
 * <p><b>布局修正（先读后写）</b>：本库 Flex 引擎不看 {@code visible}（见 {@code Node.toFlex} /
 * {@code Collapsible} 注释），仅 {@code visible=false} 收起后子区仍占位、下面的行不会顶上来。
 * 故收起在 150ms 淡出结束时同时把 {@code sub.flex.height=0} 回收占位，展开恢复
 * {@link FlexStyle#SIZE_UNSPECIFIED}（按内容高）；展开/收起的视觉仍是规格要求的 Slide+Fade。
 */
public class NavTree extends AnimatedNode {

    /** 一级组行高（与 {@link CoeUi#ROW_HEIGHT} 一致 = 18）。 */
    public static final int ROW_HEIGHT = CoeUi.ROW_HEIGHT;
    /** 展开/收起 / pulse 动画时长（规格表 NavTree = 150ms）。 */
    private static final int ANIM_MS = 150;
    /** 展开/收起滑动量（px，规格 ±6px，方向由实现定）。 */
    private static final int SLIDE_PX = 6;

    private final List<Group> groups = new ArrayList<>();
    private Consumer<Leaf> onSelect;
    private LeafRow selectedRow;

    public NavTree() {
        this.id = "nav-tree";
        column();
        flex.grow = 1;
    }

    public static NavTree of() { return new NavTree(); }

    /** 注册一个一级分组及其二级叶子（可多次调用；重调同名组替换）。 */
    public NavTree addGroup(Component title, List<Leaf> leaves) {
        Component t = title == null ? Component.empty() : title;
        // 重调同名组替换：从树中摘掉旧组的标题行与子区
        for (int i = groups.size() - 1; i >= 0; i--) {
            Group g = groups.get(i);
            if (Objects.equals(g.title, t)) {
                remove(g.header);
                remove(g.sub);
                groups.remove(i);
                if (selectedRow != null && g.rows.contains(selectedRow)) setSelected(null);
            }
        }

        // 一级组行：Button.styled，ROW_HEIGHT=18，左对齐标题
        Button header = Button.of(t).styled();
        header.flex.height = ROW_HEIGHT;
        header.padding(0, 4);
        TextRendererI hl = header.labelRenderer();
        if (hl != null) hl.align(Align.LEFT).vAlign(VAlign.MIDDLE);

        // 二级子区：AnimatedNode，默认收起（高 0 不占位 + 不可见）
        AnimatedNode sub = new AnimatedNode();
        sub.id = "nav-tree-sub";
        sub.column();
        sub.flex.padding.set(0, 0, 0, 8);   // 二级行相对一级缩进
        sub.flex.height = 0;
        sub.visible(false);

        Group g = new Group(t, header, sub);
        List<Leaf> src = leaves == null ? List.of() : leaves;
        for (Leaf leaf : src) {
            LeafRow row = new LeafRow(leaf);
            sub.add(row);
            g.rows.add(row);
        }
        header.onClick(() -> toggle(g));

        add(header);
        add(sub);
        groups.add(g);
        return this;
    }

    /** 清空全部组（重建数据时用）。 */
    public NavTree clear() {
        groups.clear();
        selectedRow = null;
        clearChildren();
        return this;
    }

    /** 二级叶子点击上抛。 */
    public NavTree onSelect(Consumer<Leaf> cb) { this.onSelect = cb; return this; }

    /** 程序化选中：展开所在组并高亮该叶（不触发 {@link #onSelect}）。 */
    public NavTree select(String leafId) {
        if (leafId == null) return this;
        for (Group g : groups) {
            for (LeafRow row : g.rows) {
                if (leafId.equals(row.leaf.id())) {
                    if (!g.expanded) { g.expanded = true; expand(g); }
                    setSelected(row);
                    return this;
                }
            }
        }
        return this;
    }

    // ------------------------------------------------------------------
    // 展开 / 收起
    // ------------------------------------------------------------------

    private void toggle(Group g) {
        g.expanded = !g.expanded;
        if (g.expanded) expand(g); else collapse(g);
    }

    private void expand(Group g) {
        AnimatedNode sub = g.sub;
        g.hideAt = 0;
        sub.flex.height = FlexStyle.SIZE_UNSPECIFIED;   // 恢复占位（按内容高）
        sub.visible(true);
        sub.anim().alpha = 0F;
        sub.fadeIn(ANIM_MS);
        sub.anim().offsetY = -SLIDE_PX;
        sub.slideTo(0, 0, ANIM_MS);                    // -6px → 0
    }

    private void collapse(Group g) {
        AnimatedNode sub = g.sub;
        sub.fadeOut(ANIM_MS);
        sub.slideTo(0, SLIDE_PX, ANIM_MS);             // 0 → +6px，与淡出同步
        g.hideAt = Util.getMillis() + ANIM_MS;         // 150ms 后真正隐藏并回收占位
    }

    @Override
    public void tick() {
        super.tick();
        long now = Util.getMillis();
        for (Group g : groups) {
            if (g.hideAt != 0 && now >= g.hideAt) {
                g.sub.visible(false);
                g.sub.flex.height = 0;                 // 回收占位（Flex 不看 visible）
                g.sub.anim().offsetY = 0;
                g.sub.anim().alpha = 0F;
                g.hideAt = 0;
            }
        }
    }

    private void setSelected(LeafRow row) {
        if (selectedRow != null) selectedRow.setHighlighted(false);
        selectedRow = row;
        if (selectedRow != null) selectedRow.setHighlighted(true);
    }

    // ------------------------------------------------------------------
    // 内部持有
    // ------------------------------------------------------------------

    /** 一个一级组 = 标题行按钮 + 可折叠子区 + 其二级行列表。 */
    private static final class Group {
        final Component title;
        final Button header;
        final AnimatedNode sub;
        final List<LeafRow> rows = new ArrayList<>();
        boolean expanded;
        /** 收起后的「真正隐藏」时刻（ms 时间戳）；0 = 无待隐藏。 */
        long hideAt;

        Group(Component title, Button header, AnimatedNode sub) {
            this.title = title;
            this.header = header;
            this.sub = sub;
        }
    }

    /**
     * 二级行：名称（grow）+ 右侧计数徽标；点击上抛 {@code onSelect} + pulse。
     * 选中行叠加一层选中底色（{@code activeBg()} 令牌）。
     */
    private final class LeafRow extends AnimatedNode {
        private final Leaf leaf;
        private Renderer selBg;

        LeafRow(Leaf leaf) {
            this.leaf = leaf;
            this.id = "nav-leaf";
            flex.height = ROW_HEIGHT;
            row();
            alignItems(AlignItems.CENTER);
            padding(0, 4);

            // 名称：吃掉剩余宽度（grow），左对齐
            Node name = new Node().id("nav-leaf-name")
                    .with(Renderers.text(leaf.name() == null ? Component.empty() : leaf.name())
                            .align(Align.LEFT)
                            .vAlign(VAlign.MIDDLE));
            name.flex.grow = 1;
            add(name);

            // 右侧计数徽标：取色走 Theme.accent()
            if (leaf.count() >= 0) {
                Badge badge = Badge.of(Component.literal(Integer.toString(leaf.count())))
                        .color(Theme.current().accent());
                add(badge);
            }
        }

        /** 叠加/移除选中底色层（取色走 Theme.activeBg() 令牌）。 */
        void setHighlighted(boolean on) {
            if (on && selBg == null) {
                selBg = Renderers.color(Theme.current().activeBg())
                        .rounding(Theme.current().radiusSm());
                visuals.add(0, selBg);
            } else if (!on && selBg != null) {
                visuals.remove(selBg);
                selBg = null;
            }
        }

        @Override
        protected boolean onMouseClicked(double lx, double ly, int button) {
            if (button != 0) return false;
            setSelected(this);
            pulse(0.05F, ANIM_MS);
            if (NavTree.this.onSelect != null) NavTree.this.onSelect.accept(leaf);
            return true;
        }
    }

    /** 叶子：id / 显示名 / 计数（徽标）。 */
    public record Leaf(String id, Component name, int count) {}
}
