package com.cimdy.coe.ui.screen;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;

import com.cimdy.coe.ui.widget.component.list.ListView;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.TextInput;
import com.cimdy.coe.ui.widget.component.badge.Badge;
import com.cimdy.coe.ui.widget.component.menu.ContextMenu;
import com.cimdy.coe.ui.widget.component.menu.MenuBar;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.SplitPane;
import com.cimdy.coe.ui.widget.compose.semantic.ButtonBar;
import com.cimdy.coe.ui.widget.compose.semantic.KeyValue;
import com.cimdy.coe.ui.widget.compose.semantic.Section;
import com.cimdy.coe.ui.widget.compose.semantic.Tabs;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 本模组界面的<b>装配约定</b>。
 *
 * <p>它不属于 UI 库 —— 库刻意不替业务决定视觉风格。这里把「本模组的界面长什么样」
 * 集中成几个工厂方法，12 个 Screen 共用同一套间距、行高与配色，
 * 想整体换风格只改这一个文件。
 */
public final class CoeUi {

    /** 列表行的统一高度。 */
    public static final int ROW_HEIGHT = 18;
    /** 窗口内容区统一留白。 */
    public static final int PAD = 3;
    /** 底部工具条（搜索框、按钮条）的统一高度。 */
    public static final int BAR_HEIGHT = 12;

    // 字号规范（计划四·4）：全模组统一收敛，组件不得散写魔法缩放值
    /** 按钮字号。 */
    public static final float SCALE_BTN = 0.8F;
    /** 行内小字（说明 / 元信息）。 */
    public static final float SCALE_META = 0.7F;
    /** 角标字号。 */
    public static final float SCALE_BADGE = 0.5F;

    private CoeUi() {}

    // ------------------------------------------------------------------
    // 窗口
    // ------------------------------------------------------------------

    /** 标准面板窗口：默认皮肤 + 关闭按钮 + 固定尺寸。位置仍需调用方指定。 */
    public static Window panel(Component title, int width, int height) {
        Window w = Window.of(title).styled().closable(true).glow();
        w.flex.size(width, height);
        return w;
    }

    // ------------------------------------------------------------------
    // 列表
    // ------------------------------------------------------------------

    /**
     * 列表行模板：左对齐文字的可点按钮。
     *
     * <p>只是模板 —— {@code ListView} 会 {@code copy()} 出 N 份，
     * 具体文案与点击行为由 binder 逐行赋。
     */
    public static Button listRow() {
        return listRow(ROW_HEIGHT);
    }

    public static Button listRow(int height) {
        Button row = Button.of(Component.empty()).styled();
        row.flex.height = height;
        row.padding(0, 4);
        TextRendererI label = row.labelRenderer();
        if (label != null) {
            label.align(Align.LEFT).vAlign(VAlign.MIDDLE);
        }
        return row;
    }

    /** 列表为空时的占位提示。 */
    public static Node emptyHint() {
        return emptyHint(Component.translatable("ui.coe.empty"));
    }

    public static Node emptyHint(Component text) {
        Node n = new Node().id("empty-hint");
        n.flex.height = ROW_HEIGHT;
        n.with(Renderers.text(text)
                .color(Theme.current().placeholderText())
                .align(Align.CENTER)
                .vAlign(VAlign.MIDDLE));
        return n;
    }

    // ------------------------------------------------------------------
    // 设置项
    // ------------------------------------------------------------------

    /**
     * 一条设置项：左边标题占满剩余宽度，右边是控件。
     *
     * <p>旧库里这类排版要手算「控件放在 contentWidth - 60 处」，窗口一变宽就错位；
     * 这里交给 Flexbox 的 {@code grow}，标题自动吃掉剩余空间。
     */
    public static Node settingRow(Component title, Node control) {
        Node row = new Node().row().id("setting-row");
        row.flex.height = 14;
        row.flex.alignItems = AlignItems.CENTER;

        Node label = new Node().id("setting-label")
                .with(Renderers.text(title)
                        .align(Align.LEFT)
                        .vAlign(VAlign.MIDDLE));
        label.flex.grow = 1;

        row.add(label);
        row.add(control);
        return row;
    }

    /** 只读的一行说明文字，可自动折行。 */
    public static Node paragraph(Component text, int height) {
        Node n = new Node().id("paragraph");
        n.flex.height = height;
        n.with(Renderers.text(text)
                .align(Align.LEFT)
                .vAlign(VAlign.TOP)
                .wrap(true)
                .autoScale(false));
        return n;
    }

    // ------------------------------------------------------------------
    // 输入
    // ------------------------------------------------------------------

    /** 底部搜索框。 */
    public static TextInput searchBox() {
        TextInput t = new TextInput()
                .styled()
                .placeholder(Component.translatable("input_box.coe.input"));
        t.with(Theme.shimmer().maxAlpha(0.18F));
        t.flex.height = BAR_HEIGHT;
        t.flex.margin.set(1, PAD, PAD, PAD);
        return t;
    }

    // ------------------------------------------------------------------
    // 补充组件（让「当成 UI 库用」时屏幕装配更顺手）
    // ------------------------------------------------------------------

    /** 状态徽标：圆角药丸 + 可选文字，叠在其它组件上。 */
    public static Badge badge(Component text) { return Badge.of(text); }

    /** 顶栏菜单：每个条目点击展开一个下拉 {@link ContextMenu}。 */
    public static MenuBar menuBar() { return new MenuBar(); }

    /** 左右可拖拽双栏。 */
    public static SplitPane splitHorizontal(Node a, Node b) { return SplitPane.horizontal(a, b); }

    /** 上下可拖拽双栏。 */
    public static SplitPane splitVertical(Node a, Node b) { return SplitPane.vertical(a, b); }

    /** 右键式上下文菜单构造器。 */
    public static ContextMenu contextMenu() { return ContextMenu.of(); }

    // ------------------------------------------------------------------
    // 高层语义部件（让屏幕用「部件」而非裸 Node 组合；颜色全部来自 Theme）
    // ------------------------------------------------------------------

    /** 带标题的主题卡片容器：标题栏 + 隔线 + 可加内容的主体。 */
    public static Section section(Component title) { return new Section().title(title); }

    /** 只读的「标签 : 值」一行（设置项的展示版，对应 {@link #settingRow}）。 */
    public static KeyValue keyValue(Component label, Component value) {
        return new KeyValue().label(label).value(value);
    }

    /** 一排统一样式的按钮（横向 / 纵向、等宽可选）。 */
    public static ButtonBar buttonBar() { return new ButtonBar(); }

    /** 标签页容器：标签栏 + 随选中切换的内容面板。 */
    public static Tabs tabs() { return new Tabs(); }

    // ------------------------------------------------------------------
    // 屏幕构建助手
    // ------------------------------------------------------------------

    /** 一个已套默认皮肤的按钮（等价于 {@code Button.of(label).styled()}）。 */
    public static Button button(Component label) { return Button.of(label).styled(); }

    /** 扁平模态窗口（不像 {@link #panel} 带辉光，用于对话框 / 临时弹层）。调用方负责 add 到 WindowLayer 并居中。 */
    public static Window dialog(Component title, int width, int height) {
        Window w = Window.of(title).styled().closable(true);
        w.flex.size(width, height);
        return w;
    }

    /** 弹出确认对话框（确认 / 取消），确认回调里写后续动作；调用方无需自建 Screen。 */
    public static void confirm(Component title, Component message, Runnable onConfirm) {
        confirm(title, List.of(message), onConfirm);
    }

    /** 弹出确认对话框（多行信息）。 */
    public static void confirm(Component title, List<Component> message, Runnable onConfirm) {
        Minecraft.getInstance().setScreenAndShow(new DialogScreen(title, message, new DialogScreen.Handler() {
            @Override public void onConfirm() { if (onConfirm != null) onConfirm.run(); }
            @Override public void onCancel() {}
        }, true));
    }

    // ------------------------------------------------------------------
    // 关键字系统
    // ------------------------------------------------------------------

    /** 数据驱动的关键字注册表单例（已装载 {@code data/coe/keywords/<id>.json}）。 */
    public static com.cimdy.coe.ui.widget.keyword.KeywordRegistry keywordRegistry() {
        return com.cimdy.coe.ui.widget.keyword.KeywordRegistry.instance();
    }
}
