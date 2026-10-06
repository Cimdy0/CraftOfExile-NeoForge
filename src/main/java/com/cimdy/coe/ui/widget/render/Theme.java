package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.component.assoc.Association;
import com.cimdy.coe.ui.widget.component.assoc.ChangeListener;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.list.OptionListPopup;
import com.cimdy.coe.ui.widget.component.panel.SidePanel;
import com.cimdy.coe.ui.widget.component.TextInput;
import com.cimdy.coe.ui.widget.component.panel.Window;

/**
 * 全库唯一的视觉调色板与皮肤预设。
 *
 * <p>之前每一处「中性默认皮肤」都把同一组 ARGB 字面量抄了一遍：
 * {@code Button.styled()}/{@code Window.styled()}/{@code TextInput.styled()}/
 * {@code OptionListPopup.popupStyle()}/{@code SidePanel.styled()}/
 * {@code ScrollBar.styled()}/{@code Slot.styledSlot()} 各自硬编码 {@code 0xFF6BA8D6}、
 * {@code 0xFF3A3A3A}、{@code 0x60000000}…… 改一个颜色要搜七处，且极易抄错。
 *
 * <p>这里把它们收成<b>单一事实来源</b>：颜色常量就是规范，皮肤方法就是约定的装配方式。
 * 各组件的 {@code styled()} 只负责「加哪几层、文字怎么配色」，具体色值一律从这里取。
 * 这样既<b>合并了重复</b>（一份定义），也<b>约束了视觉语言</b>（全库同一套强调色/面板色）。
 *
 * <p>皮肤的可切换版本见 {@link StylePreset}（工作流 B2）：本类的静态 {@code button()}/{@code window()}/
 * 等皮肤方法现在只是<b>当前预设的薄转发层</b>，默认预设 {@code StylePreset.dark()} 的色值即本文件的
 * 规范常量，因此行为与重构前完全一致（零回归）。要整体换肤调用 {@link #apply(StylePreset)}。
 */
public final class Theme {

    private Theme() {}

    // ------------------------------------------------------------------
    // 全局动画开关（附录 D.0 纪律）
    // ------------------------------------------------------------------

    /**
     * 全局动画开关（默认开）。低配机器 / 排障时一键整体关闭：关闭后所有补间瞬时完成
     * （等价于「无动画 = 静止终帧」，见 {@code Animator} 经 {@code AnimRegistry.animationsActive()} 的单点退化）。
     * reduced-motion（MC 辅助设置）由 {@code AnimRegistry} 另行检测，无需在此设置。
     */
    public static boolean ANIMATION_ENABLED = true;

    /** 开 / 关全局动画。 */
    public static void setAnimationEnabled(boolean b) { ANIMATION_ENABLED = b; }

    // ------------------------------------------------------------------
    // 颜色常量（全库唯一的色值事实来源）
    // ------------------------------------------------------------------

    /** 强调色：边框高亮、选中、滚动条滑块。全库只有这一种「亮蓝」（暗底上提亮一档的雾蓝）。 */
    public static final int ACCENT        = 0xFF7FB3D8;
    /** 强调色的更亮一档（滚动条滑块悬浮）。 */
    public static final int ACCENT_BRIGHT = 0xFFA8D4EC;

    /** 面板主体色（常态底）：冷调蓝黑灰，与窗口底色拉开层次。 */
    public static final int PANEL         = 0xFF262B33;
    /** 半透明面板底（按钮常态）：蓝黑半透，替代原纯黑。 */
    public static final int PANEL_DIM     = 0x65101820;
    /** 窗口底（不透明蓝黑基底）。 */
    public static final int WINDOW_BG     = 0xFF14171C;
    /** 输入框底。 */
    public static final int INPUT_BG      = 0xB00A0F16;
    /** 侧边抽屉底。 */
    public static final int PANEL_SIDE    = 0xC00E1218;
    /** 弹出层底（不透明，保证盖住背后）。 */
    public static final int POPUP_BG      = 0xF014181F;
    /** 弹出层边框。 */
    public static final int POPUP_BORDER  = 0xFF3A4654;

    /** 滚动条轨道（暗色）。 */
    public static final int TRACK         = 0x500A0E14;
    /** 物品槽底（深色半透 + 紫调边）。 */
    public static final int SLOT_BG       = 0x461B2633;
    public static final int SLOT_BORDER   = 0x6645658F;

    /** 悬浮态底色（按钮）：蓝黑提亮档。 */
    public static final int HOVER_BG      = 0x90202A38;
    /** 选中态底色（按钮）：明确的深蓝。 */
    public static final int ACTIVE_BG     = 0x90163A5E;
    /** 按下态底色（按钮）。 */
    public static final int PRESSED_BG    = 0xC0080C10;
    /** 禁用态底色。 */
    public static final int DISABLED_BG   = 0x4010151C;
    /** 输入框悬浮边框（蓝灰）。 */
    public static final int INPUT_BORDER_HOVER = 0xFF4E6E94;
    /** 按钮禁用边框。 */
    public static final int BORDER_DISABLED    = 0xFF232A33;

    /** 侧边抽屉折叠把手常态底（蓝黑半透）。 */
    public static final int HANDLE_BG     = 0x60101820;
    /** 折叠把手悬浮底（冷白，避免纯白刺眼）。 */
    public static final int HANDLE_HOVER  = 0x90B8D8F0;

    /** 标题栏底（深蓝半透明，替代原刺眼半透白）。 */
    public static final int TITLE_BAR_BG  = 0x501A2632;
    /** 角点点缀（窗口/按钮同款）。 */
    public static final int CORNER_DOTS   = 0xFF8FB8D8;
    /** 角括号装饰（窗口）。 */
    public static final int CORNER_TICKS  = 0x807FB3D8;

    /** 列表选项悬浮底（蓝灰半透，替代原半透白）。 */
    public static final int OPTION_HOVER_BG = 0x6028384C;
    /** 文字悬浮色（标签 / 选项）：暖米白。 */
    public static final int TEXT_HOVER      = 0xFFFFF0C0;
    /** 禁用文字色。 */
    public static final int TEXT_DISABLED   = 0xFF6A7280;
    /** 正文文字色（提示盒 / 一般文本）：微暖白。 */
    public static final int TEXT            = 0xFFE6EAF0;

    /** 物品槽默认尺寸，多处复用。 */
    public static final int SLOT_SIZE = 18;

    /** 分页工具条底（{@code pagerBar()} 使用的凹槽色，之前是散字面量）。 */
    public static final int PAGER_BAR_BG = 0x50182028;

    /** 开关轨道（关态）。 */
    public static final int TOGGLE_TRACK_OFF = 0x500A0E14;
    /** 开关轨道（开态）。 */
    public static final int TOGGLE_TRACK_ON  = 0xFF336B96;
    /** 开关滑块（旋钮）。 */
    public static final int TOGGLE_THUMB     = 0xFFDCE4EC;

    /** 进度条轨道。 */
    public static final int PROGRESS_TRACK = 0x40182028;
    /** 进度条填充。 */
    public static final int PROGRESS_FILL  = ACCENT;

    /** 标签栏底（未选中）。 */
    public static final int TAB_BG      = 0x40182028;
    /** 标签栏底（选中态）。 */
    public static final int TAB_ACTIVE  = 0x90163A5E;
    /** 标签栏边框。 */
    public static final int TAB_BORDER  = 0x40263040;

    /** 提示盒底。 */
    public static final int TOAST_BG     = 0xF014181F;
    /** 提示盒边框。 */
    public static final int TOAST_BORDER = 0xFF3A4654;

    /** 窗口 / 面板常态强边框（钢蓝）：面板轮廓的立体感来源。 */
    public static final int BORDER_STRONG = 0xFF3A4A5C;
    /** 窗口顶部渐变带顶色（配合 {@link ColorRenderer#gradient} 的蓝黑亮档）。 */
    public static final int WINDOW_TOP    = 0xFF1B2330;

    /** 提示语义色（标题 / 左侧强调条）。 */
    public static final int TOAST_INFO    = ACCENT;
    public static final int TOAST_SUCCESS = 0xFF5BA85B;
    public static final int TOAST_WARN    = 0xFFD9A23B;
    public static final int TOAST_ERROR   = 0xFFD05A5A;


    // ------------------------------------------------------------------
    // 风格预设（工作流 B2）：可切换的 StylePreset 机制
    // ------------------------------------------------------------------

    /**
     * 具名关联承载当前预设（E1：把全局广播内部委托给 assoc 关联层，消除手动 Runnable 回调）。
     * 默认 {@code enabled=true}、{@code strength=1.0} → 足额传播，等价于原 fire。
     */
    private static final Association<StylePreset> CURRENT_PRESET =
            new Association<>("theme:current", StylePreset.dark());

    /** 当前生效预设。屏幕/组件需要读取令牌或换肤时从这里取。 */
    public static StylePreset current() { return CURRENT_PRESET.get(); }

    /**
     * 切换全局风格预设，立即对所有使用该机制的组件生效。
     * 收敛点（F1/F2）：只经 CURRENT_PRESET 广播，并回写 StylePresetRegistry 的 active 索引 + 持久化，
     * 保证 Theme.current() 与 StylePresetRegistry.active() 始终一致、换肤高亮/持久化不再错乱。
     */
    public static void apply(StylePreset preset) {
        CURRENT_PRESET.set(preset);
        StylePresetRegistry.syncActiveById(preset.id());
    }

    /** 当前注册的「预设变更桥接监听者」：保存以便重新设置时移除，保留「单一活动监听者」语义。 */
    private static ChangeListener<? super StylePreset> presetChangeBridge;

    /**
     * 预设变更监听器（当前打开的 WidgetScreen 借此重建以热刷新构造期缓存的色值）。
     * 内部委托给 {@link #CURRENT_PRESET}：注册为命名关联的监听者后，预设切换经
     * {@code CURRENT_PRESET.set} 自动通知，无需手动 fire。每次设置移除上一个桥接监听者，
     * 与原 {@code Runnable} 字段「覆盖式赋值」行为一致。
     */
    public static void setPresetChangeListener(Runnable r) {
        if (presetChangeBridge != null) CURRENT_PRESET.removeListener(presetChangeBridge);
        presetChangeBridge = (o, n) -> r.run();
        CURRENT_PRESET.addListener(presetChangeBridge);
    }

    /** 循环切换到下一个已加载预设（全局键触发）。 */
    public static StylePreset cyclePreset() { return StylePresetRegistry.cycle(); }

    /** 按 id 切换到指定预设（选择 UI 触发）。 */
    public static void setPresetById(String id) { StylePresetRegistry.setActiveById(id); }

    // ------------------------------------------------------------------
    // 皮肤预设：返回配置好的 ColorRenderer，组件直接叠到 visuals 最底层
    // ------------------------------------------------------------------

    /**
     * 按钮底：常态半透 + 悬浮/选中/按下/禁用各态色 + 圆角 + 状态平滑过渡
     * + 悬停顶亮渐变 / 按下压暗渐变（对齐旧库按钮的招牌手感）。
     */
    public static ColorRendererI button() { return current().button(); }

    /** 窗口底：不透明深灰实色 + 全边框 + 圆角；标题栏/角点另外由 Window.styled() 叠加。 */
    public static ColorRendererI window() { return current().window(); }

    /** 输入框底：不透明深底 + 全边框 + 悬浮提亮边框 + 圆角。 */
    public static Renderer input() { return current().input(); }

    /** 侧边抽屉底。 */
    public static Renderer panel() { return current().panel(); }

    /** 弹出层底：不透明 + 细边框 + 圆角，保证盖住背后内容。 */
    public static Renderer popup() { return current().popup(); }

    /** 物品槽底：深色半透 + 紫调边。 */
    public static Renderer slot() { return current().slot(); }

    /** 滚动条轨道。 */
    public static Renderer scrollTrack() { return current().scrollTrack(); }

    /** 滚动条滑块（常态 + 悬浮）。 */
    public static Renderer scrollThumb() { return current().scrollThumb(); }

    /** 分页工具条底：暗色凹槽，让「PageBar」先有一条可见的 Bar，页码/按钮浮在上面。 */
    public static Renderer pagerBar() { return current().pagerBar(); }

    /** 侧边抽屉折叠把手（常态半透黑 + 悬浮半透白）。 */
    public static Renderer handle() { return current().handle(); }

    /** 进度条轨道。 */
    public static Renderer progressTrack() { return current().progressTrack(); }

    /** 标签页底：一层覆盖常态/悬浮/选中/禁用四态，选中时换强调色边框 + 圆角 + 平滑过渡。 */
    public static Renderer tabBg() { return current().tabBg(); }

    // ------------------------------------------------------------------
    // 动态效果预设：直接挂到节点 visuals 上即可
    // ------------------------------------------------------------------

    /** 柔和投影：让面板从背景里浮起来。窗口 {@code styled()} 默认挂一层。 */
    public static ShadowRendererI shadow() { return current().shadow(); }

    /** 脉冲发光边框：描边 alpha 呼吸，用于「被选中 / 需要留意」。 */
    public static GlowBorderRendererI glow() { return current().glow(); }

    /** 扫光带：半透亮带循环掠过，给面板/按钮一点活气。 */
    public static ShimmerRendererI shimmer() { return current().shimmer(); }

    /**
     * 皮肤音效 pitch 偏移（附录 D.3，与音效特效 shard 的接口契约）。
     * 默认 1.0；Paper 0.95 / Midnight 1.1，其余 1.0。音效层播放时读这里。
     */
    public static float sfxPitch() { return current().sfxPitch(); }

    /** 窗口 / 面板背景纹理叠加层（附录 D.2）：按当前皮肤程序化手绘。 */
    public static Renderer backgroundTexture() { return current().backgroundTexture(); }

    // ------------------------------------------------------------------
    // 颜色工具：在已有色值上做派生，避免组件里硬编码近似色
    // ------------------------------------------------------------------

    /** 向白色提亮（f 取 0~1）。 */
    public static int lighten(int argb, float f) {
        f = clamp01(f);
        int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
        r = (int) (r + (255 - r) * f);
        g = (int) (g + (255 - g) * f);
        b = (int) (b + (255 - b) * f);
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    /** 向黑色压暗（f 取 0~1）。 */
    public static int darken(int argb, float f) {
        f = clamp01(f);
        int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
        r = (int) (r * (1 - f));
        g = (int) (g * (1 - f));
        b = (int) (b * (1 - f));
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    /** 两色线性插值（t 取 0~1）。委托 {@link Draw#blend}，避免重复实现（F7）。 */
    public static int blend(int a, int b, float t) {
        return Draw.blend(a, b, t);
    }

    private static float clamp01(float v) { return Math.max(0F, Math.min(1F, v)); }
}
