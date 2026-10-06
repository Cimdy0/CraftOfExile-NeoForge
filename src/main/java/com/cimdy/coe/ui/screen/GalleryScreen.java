package com.cimdy.coe.ui.screen;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;

import com.cimdy.coe.ui.widget.component.badge.Badge;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.base.Scrollable;
import com.cimdy.coe.ui.widget.component.dnd.Draggable;
import com.cimdy.coe.ui.widget.component.dnd.DragRegistry;
import com.cimdy.coe.ui.widget.component.dnd.DropZone;
import com.cimdy.coe.ui.widget.component.dnd.NodeDropZone;
import com.cimdy.coe.ui.widget.component.panel.SplitPane;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.compose.composite.DetailPanel;
import com.cimdy.coe.ui.widget.compose.picker.SearchableList;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.StylePreset;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.screen.WidgetScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * UI 组件库浏览屏（G 键打开）：把本模组的 UI 组件以「可浏览目录 + 实况演示」方式呈现，
 * 直接兑现「ui 的 lib 库，包含的可以更全面」——既是组件清单，也是活的示例。
 *
 * <p>左栏为组件目录（可按名检索），右栏为选中项的说明；说明区下方有一个「实况演示」面板，
 * 含预设切换按钮、徽标、可拖拽双栏，直观展示库能力。全部走与 {@link CodexScreen} 相同的
 * 纯客户端只读打开路径（{@code setScreenAndShow}，免 Menu / 网络）。
 */
public class GalleryScreen extends WidgetScreen {

    private static final int WINDOW_WIDTH = 520;
    private static final int WINDOW_HEIGHT = 340;

    private record Entry(String name, String category, String description) {}

    private final List<Entry> catalog = new ArrayList<>();
    private final Node detailHost = new Node().column();
    private Button presetBtn;
    private int presetIdx = 0;
    /** 当前登记的拖拽放置区；切换目录条目时注销，避免注册表跨次累积。 */
    private DropZone dndZone;
    /** 跨窗口放置区（zone2）；与 dndZone 同理，切换条目时一并注销，避免 DragRegistry 累积失效 zone。 */
    private DropZone dndZone2;
    /** 拖拽演示里额外开出的「另一个窗口」，用于演示跨窗口拖拽；切换条目时移除，避免泄漏。 */
    private Window dndOtherWindow;

    private static final StylePreset[] PRESETS = {
            StylePreset.dark(), StylePreset.highContrast(), StylePreset.compact(),
            StylePreset.paper(), StylePreset.midnight()
    };

    /** Live Demo 里拖拽演示对应的目录条目名。 */
    private static final String DND_ENTRY_NAME = "Drag & Drop";

    public GalleryScreen() {
        super(Component.translatable("ui.coe.gallery"));
        buildCatalog();
    }

    private void buildCatalog() {
        catalog.add(new Entry("Button", "输入", "可点按钮，行为来自 Pressable；styled() 套中性默认皮肤。支持 onClick / toggle / glow / shimmer。"));
        catalog.add(new Entry("Toggle", "输入", "开关控件，封装选中态与开/关轨道配色。"));
        catalog.add(new Entry("Drag & Drop", "输入", "拖拽：按住并在移动阈值后进入拖拽，跟随光标的残影由 WindowLayer 的 GhostOverlay 绘制；松手命中 DropZone 派发 onDrop。"));
        catalog.add(new Entry("ValueBar / Slider", "输入", "数值条 / 滑块，支持方向键、Page、Home/End 与步进吸附。"));
        catalog.add(new Entry("Pressable", "输入", "所有可点控件的基类，提供 onClick / accept / only / active 等通用交互。"));
        catalog.add(new Entry("TextInput", "输入", "文本框，styled() 接输入皮肤；CoeUi.searchBox() 在其上叠加扫光。"));
        catalog.add(new Entry("ListView", "列表", "虚拟滚动列表，行模板 copy() 复用；支持键盘导航与选中态。"));
        catalog.add(new Entry("ScrollBar / Scrollable", "列表", "滚动条支持点击轨道跳转 + 拖拽 + 方向键/Page/Home/End；Scrollable 提供滚轮/聚焦/键盘滚动。"));
        catalog.add(new Entry("SearchableList", "列表", "ListView + 检索框组合，textOf / match 两种匹配，clearBeforeBind 防残留。"));
        catalog.add(new Entry("OptionListPopup", "列表", "下拉选项弹出层，popup 皮肤 + 键盘可选。"));
        catalog.add(new Entry("DetailPanel", "复合", "右侧详情面板，show(List<Component>) 渲染说明文本，随行悬浮刷新。"));
        catalog.add(new Entry("MasterDetail", "复合", "主从面板：rowTemplate()/bindRow()/onSelect() 构建左列表右详情，选中行联动详情面板；SkillScreen 技能列表在用。"));
        catalog.add(new Entry("Badge", "复合", "状态徽标，圆角药丸 + 可选文字，叠在其它组件上。"));
        catalog.add(new Entry("ContextMenu", "菜单", "右键上下文菜单，extends Window，外部点击 / ESC 关闭。"));
        catalog.add(new Entry("MenuBar", "菜单", "顶栏菜单，自动解析 WindowLayer 展开下拉 ContextMenu。"));
        catalog.add(new Entry("SplitPane", "面板", "可拖拽分隔双栏（横向 / 纵向），仿 Scrollable 的叶子 + 手动布局。"));
        catalog.add(new Entry("Window", "面板", "带标题栏的可拖拽窗口，styled() 接窗口皮肤 + 角点装饰 + 投影。"));
        catalog.add(new Entry("Node / Flexbox", "布局", "UI 树节点，row()/column() + flex 计量（grow/gap/align）做弹性布局。"));
        catalog.add(new Entry("NodeGraphController", "布局", "画布 + 顶层 UI 树 + 每帧 layout/render + 鼠标转发 + 相机 fit/center 的单一事实来源（天赋/研究树共用）。"));
        catalog.add(new Entry("ColorRenderer", "渲染", "颜色层渲染器：背景 / 边框 / 悬浮 / 按下 / 禁用 + 圆角 + 平滑过渡。"));
        catalog.add(new Entry("ShadowRenderer", "渲染", "柔和投影，让面板从背景浮起。"));
        catalog.add(new Entry("GlowBorderRenderer", "渲染", "脉冲发光边框，用于「被选中 / 需要留意」。"));
        catalog.add(new Entry("ShimmerRenderer", "渲染", "循环扫光带，给面板/按钮增加活气。"));
        catalog.add(new Entry("Theme", "调色板", "全库唯一的色值事实来源；皮肤方法为当前 StylePreset 的薄转发层。"));
        catalog.add(new Entry("StylePreset", "调色板", "可切换风格预设（Dark / HighContrast / Compact / Paper）；Theme.apply 全局即时换肤。"));
        catalog.add(new Entry("KeywordTooltipHost", "关键字", "PoE2 风格关键字系统宿主：悬停预览 / ALT 滞留无限钻取 / 左键钉住；位置无关、数据驱动。"));
    }

    @Override
    protected void buildUi(WindowLayer root) {
        Window window = CoeUi.panel(Component.translatable("ui.coe.gallery"), WINDOW_WIDTH, WINDOW_HEIGHT)
                .onClose(this::onClose)
                .center(this.width, this.height);

        Node body = new Node().row();
        body.flex.grow = 1;
        body.flex.gapMain = 6;

        SearchableList<Entry> nav = new SearchableList<>(CoeUi.listRow(), this::bindRow)
                .textOf(e -> e.name())
                .itemGap(2)
                .contentPadding(CoeUi.PAD)
                .emptyView(CoeUi.emptyHint())
                .scrollBar(true)
                .clearBeforeBind();
        nav.flex.width = 150;
        nav.list().items(catalog);
        nav.onSelect((e, idx) -> showEntry(e));

        detailHost.flex.grow = 1;
        detailHost.flex.gapMain = 6;

        body.add(nav);
        body.add(detailHost);
        window.add(body);
        root.add(window);

        showEntry(catalog.get(0));
    }

    /** 签名 / 能力说明：显示该组件的接口名与关键方法（计划 §3.3 微调；修复批 3 补全缺失条目）。 */
    private Component signatureOf(Entry e) {
        String cls = switch (e.name()) {
            case "Button" -> "component.button.Button · onClick(Runnable)/toggle()/glow()";
            case "Toggle" -> "component.button.Toggle · 选中态 + 开/关轨道配色";
            case "Drag & Drop" -> "dnd.Draggable · payload()/dragRegistry() + DropZone/NodeDropZone";
            case "ValueBar / Slider" -> "compose.ValueBar · 数值条/滑块，方向键/Page/Home/End + 步进吸附";
            case "Pressable" -> "component.base.Pressable · onClick()/accept()/only()/active() 交互基类";
            case "TextInput" -> "component.TextInput · styled() 输入皮肤 + CoeUi.searchBox() 扫光";
            case "ListView" -> "composite.ListView · row()/items()/onSelect()";
            case "ScrollBar / Scrollable" -> "component.base.Scrollable · 滚轮/聚焦/键盘滚动 + ScrollBar 轨道点击/拖拽";
            case "SearchableList" -> "compose.picker.SearchableList · textOf()/match()/onSelect()";
            case "OptionListPopup" -> "component.list.OptionListPopup · popup 皮肤 + 键盘可选";
            case "DetailPanel" -> "composite.DetailPanel · show(List<Component>)";
            case "MasterDetail" -> "composite.MasterDetail · rowTemplate()/bindRow()/onSelect()";
            case "Badge" -> "component.badge.Badge · 圆角药丸 + 可选文字";
            case "ContextMenu" -> "component.panel.ContextMenu · extends Window，右键菜单";
            case "MenuBar" -> "component.panel.MenuBar · 顶栏菜单 → WindowLayer 展开 ContextMenu";
            case "SplitPane" -> "component.panel.SplitPane · 可拖拽分隔双栏（横/纵）";
            case "Window" -> "panel.Window · panel()/closable()/glow()/attach()";
            case "Node / Flexbox" -> "core.Node · row()/column()/flex(grow,gap,align)";
            case "NodeGraphController" -> "screen.graph.NodeGraphController · 画布+UI 树+相机 fit/center";
            case "ColorRenderer" -> "render.ColorRenderer · 背景/边框/悬浮/按下/禁用 + 圆角 + 平滑过渡";
            case "ShadowRenderer" -> "render.ShadowRenderer · 柔和投影";
            case "GlowBorderRenderer" -> "render.GlowBorderRenderer · 脉冲发光边框";
            case "ShimmerRenderer" -> "render.ShimmerRenderer · 循环扫光带";
            case "Theme" -> "render.Theme · 全库色值单一事实来源";
            case "StylePreset" -> "render.StylePreset · 风格预设（Dark/HighContrast/Compact/Paper）";
            case "KeywordTooltipHost" -> "compose.keyword.KeywordTooltipHost · 悬停预览/ALT 钻取/左键钉住";
            default -> "widget · " + e.category() + " → " + e.name();
        };
        return Component.literal(sigKey() + cls);
    }

    private static String sigKey() {
        return Component.translatable("ui.coe.gallery.signature").getString() + " ";
    }

    private void bindRow(Node view, Entry e, int index) {
        if (!(view instanceof Button row)) return;
        row.label(Component.literal(e.name() + "  ·  " + e.category()));
    }

    private void showEntry(Entry e) {
        detailHost.clearChildren();
        // 切换条目时注销上一轮登记的 DropZone，避免注册表跨次累积造成泄漏 / 误命中
        if (dndZone != null) {
            ((WindowLayer) detailHost.root()).dragRegistry().unregister(dndZone);
            dndZone = null;
        }
        if (dndZone2 != null) {
            ((WindowLayer) detailHost.root()).dragRegistry().unregister(dndZone2);
            dndZone2 = null;
        }
        // 顺手移除上轮拖拽演示开出的「另一个窗口」，避免每次切回都叠加一个新窗口
        if (dndOtherWindow != null) {
            ((WindowLayer) detailHost.root()).remove(dndOtherWindow);
            dndOtherWindow = null;
        }

        DetailPanel detail = new DetailPanel();
        detail.flex.grow = 1;
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("[ " + e.category() + " ]  " + e.name()));
        lines.add(Component.literal(""));
        lines.add(Component.literal(e.description()));
        // 签名 / 能力说明行：显示该组件的接口名与关键方法（计划 §3.3）
        lines.add(Component.literal(""));
        lines.add(signatureOf(e));
        detail.show(lines);
        detailHost.add(detail);

        Node demoTitle = new Node().with(Renderers.text(Component.literal("Live Demo"))
                .align(Align.LEFT).vAlign(VAlign.MIDDLE));
        demoTitle.flex.height = 14;
        detailHost.add(demoTitle);

        if (DND_ENTRY_NAME.equals(e.name())) {
            buildDndDemo(detailHost);
        } else {
            buildGenericDemo(detailHost);
        }
    }

    /** 原 Live Demo：预设切换 / 徽标 / 确认弹窗 / 可拖拽双栏。 */
    private void buildGenericDemo(Node host) {
        Node demo = new Node().row();
        demo.flex.gapMain = 8;
        demo.flex.alignItems = AlignItems.CENTER;

        presetBtn = CoeUi.button(Component.literal("Preset: " + Theme.current().displayName()))
                .onClick(() -> cyclePreset());
        presetBtn.flex.size(120, 20);
        demo.add(presetBtn);

        Badge badge = CoeUi.badge(Component.literal("LIVE"));
        demo.add(badge);

        Button confirmDemo = CoeUi.button(Component.literal("Confirm Demo"))
                .onClick(() -> CoeUi.confirm(Component.translatable("ui.coe.gallery"),
                        Component.literal("This is a confirm dialog demo built via CoeUi.confirm()."),
                        () -> {}));
        confirmDemo.flex.size(110, 20);
        demo.add(confirmDemo);

        host.add(demo);

        Node splitA = new Node().with(Renderers.text(Component.literal("Left pane"))
                .align(Align.LEFT).vAlign(VAlign.MIDDLE));
        Node splitB = new Node().with(Renderers.text(Component.literal("Right pane (drag divider)"))
                .align(Align.LEFT).vAlign(VAlign.MIDDLE));
        SplitPane split = CoeUi.splitHorizontal(splitA, splitB);
        split.flex.grow = 1;
        split.flex.height = 40;
        host.add(split);
    }

    /**
     * 拖拽 Live Demo（生产化后的全套能力）：
     * <ul>
     *   <li>芯片放在一个<b>可滚动列表</b>里 —— 演示「从滚动 / 嵌套容器内发起拖拽」，残影用屏幕坐标绘制，不脱钩；</li>
     *   <li>主窗口放置区 + 另一个窗口放置区 —— 演示<b>跨窗口拖拽</b>（根注册表 DFS 聚合所有作用域 zone）；</li>
     *   <li>两个 zone 都 {@code relocateSource()} —— 松手时把芯片<b>物理搬进</b>目标节点（而非只画残影）；</li>
     *   <li>拖拽时把光标贴近滚动容器上 / 下边缘 —— 演示<b>自动滚动</b>。</li>
     * </ul>
     * 残影由 {@code WindowLayer} 的 GhostOverlay 绘制；作用域命中优先级（同容器 &gt; 父容器 &gt; 全局 &gt; 无关）由 DragRegistry 处理。
     */
    private void buildDndDemo(Node host) {
        Node status = new Node().id("dnd-status");
        TextRendererI statusText = Renderers.text(
                        Component.literal("Drag the chip (in the list) onto a zone, or click it."))
                .align(Align.LEFT).vAlign(VAlign.MIDDLE);
        status.with(statusText);
        status.flex.height = 16;

        // 嵌套滚动容器：芯片是其中一项，演示从滚动容器内发起拖拽 + 拖动到边缘自动滚动
        Scrollable scroll = Scrollable.vertical().scrollBar(true);
        scroll.id("dnd-scroll");
        scroll.flex.size(150, 72);
        for (int i = 0; i < 6; i++) {
            Node item = new Node().row();
            item.flex.height = 18;
            item.flex.gapMain = 4;
            Node dot = new Node().size(10, 10);
            dot.with(Theme.panel());
            Node label = new Node().with(Renderers.text(Component.literal("item " + i))
                    .align(Align.LEFT).vAlign(VAlign.MIDDLE));
            label.flex.grow = 1;
            item.add(dot);
            item.add(label);
            scroll.add(item);
        }

        Draggable chip = new Draggable();
        chip.id("dnd-chip");
        chip.with(Theme.button());
        chip.with(Renderers.text(Component.literal("Drag me"))
                .align(Align.CENTER).vAlign(VAlign.MIDDLE));
        chip.flex.size(96, 22);
        chip.dragLabel(Component.literal("Dragging"));
        chip.onClick(() -> statusText.setLine(0, Component.literal("Clicked the chip (no drag).")));
        chip.payload("chip-item");
        scroll.add(chip);

        // 主窗口放置区（与芯片同窗口作用域）
        Node zone = new Node().id("dnd-zone");
        zone.with(Theme.panel());
        zone.with(Renderers.text(Component.literal("Drop zone"))
                .align(Align.CENTER).vAlign(VAlign.MIDDLE));
        zone.flex.size(140, 44);

        // 第二个窗口放置区（演示跨窗口拖拽）
        Window other = CoeUi.panel(Component.literal("Other window"), 170, 92)
                .position(Math.max(8, this.width - 184), 12)
                .closable(false)
                .draggable(false);
        Node zone2 = new Node().id("dnd-zone2");
        zone2.with(Theme.panel());
        zone2.with(Renderers.text(Component.literal("Cross-window zone"))
                .align(Align.CENTER).vAlign(VAlign.MIDDLE));
        zone2.flex.size(150, 40);
        other.add(zone2);
        dndOtherWindow = other;

        WindowLayer layer = (WindowLayer) detailHost.root();
        DragRegistry reg = layer.dragRegistry();
        if (dndZone != null) reg.unregister(dndZone);
        dndZone = new NodeDropZone(zone, src -> statusText.setLine(0,
                Component.literal("Dropped into the MAIN-window zone! (physical relocation)")))
                .autoScope().relocateSource();
        reg.register(dndZone);
        dndZone2 = new NodeDropZone(zone2, src -> statusText.setLine(0,
                Component.literal("Dropped into the OTHER window! (cross-window + relocation)")))
                .autoScope().relocateSource();
        reg.register(dndZone2);
        chip.dragRegistry(reg);

        // 把第二个窗口挂到屏幕根层（与演示主窗口并列）
        layer.add(other);

        Node row = new Node().row();
        row.flex.gapMain = 12;
        row.flex.alignItems = AlignItems.CENTER;
        row.add(scroll);
        row.add(zone);
        host.add(row);
        host.add(status);
    }

    private void cyclePreset() {
        presetIdx = (presetIdx + 1) % PRESETS.length;
        Theme.apply(PRESETS[presetIdx]);
        if (presetBtn != null) {
            presetBtn.label(Component.literal("Preset: " + Theme.current().displayName()));
        }
    }
}
