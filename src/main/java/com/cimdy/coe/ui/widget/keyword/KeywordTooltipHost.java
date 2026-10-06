package com.cimdy.coe.ui.widget.keyword;

import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.render.Draw;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关键字 tooltip 宿主：常驻于 {@link WindowLayer}，负责每帧渲染与全部交互。
 *
 * <h3>三层交互（对齐 PoE2 关键字系统，并修复其「第三层断裂」回归）</h3>
 * <ol>
 *   <li><b>悬停预览</b>：光标悬停 tooltip 中的关键字，侧旁弹出该关键字的描述盒子（一级）。</li>
 *   <li><b>按键滞留（ALT 按住）</b>：锁定当前层级目标，即使鼠标移出关键字也不收起，
 *       于是可以把鼠标挪进子盒、再去悬停其中的次级关键字 —— 由此可<b>无限递归</b>钻取，
 *       这正是 PoE2 原生在第三层断裂、我们做对的那个点。</li>
 *   <li><b>点击钉住（左键）</b>：把关键字描述钉成一个可拖拽 / 可关闭的持久窗口；
 *       钉住的窗口自身也是关键字高亮文本，故同样支持悬停子级与继续点击钉更深一层。</li>
 * </ol>
 *
 * <p>匹配源：仅依赖 tooltip 的展示文本（或数据文件里的翻译键别名），因此天然覆盖
 * 物品栏 / 任意未知 tooltip 位置，无需业务方逐一标注。
 */
public class KeywordTooltipHost {

    private final WindowLayer layer;
    private final KeywordRegistry reg = KeywordRegistry.instance();

    private int screenW, screenH;
    private boolean altDown = false;
    private boolean ctrlDown = false;
    /** 由 Ctrl 长按自动钉住、松开即塌缩的长期窗口（区别于左键手动 pin）；同一时刻最多一个。 */
    private Pinned ctrlPinned = null;
    /** 各层级被 ALT 锁定的目标关键字，离开层级即失效。 */
    private final Map<Integer, KeywordEntry> sticky = new HashMap<>();
    /** 本帧绘制出的全部盒子（含瞬态链 + 各钉住窗口的子级），供点击命中测试。 */
    private final List<BoxLayout> frameBoxes = new ArrayList<>();
    /** 钉在 HUD 上的持久窗口。 */
    private final List<Pinned> pinned = new ArrayList<>();

    public KeywordTooltipHost(WindowLayer layer) {
        this.layer = layer;
    }

    public void setScreenSize(int w, int h) { this.screenW = w; this.screenH = h; }

    /** 每帧由 WindowLayer 在渲染前调用：清掉上一帧的盒子缓存。 */
    public void beginFrame() { frameBoxes.clear(); }

    /** 由 WindowLayer 每帧轮询 GLFW 得到 ALT 实时状态（不依赖 keyReleased）。 */
    public void setAltDown(boolean down) {
        this.altDown = down;
        if (!down) sticky.clear();
    }

    /**
     * 由 WindowLayer 每帧轮询 GLFW 得到 CTRL 实时状态（不依赖 keyReleased）。
     * Ctrl 期间等价于「把悬停关键字自动钉成长期窗口」；松开则塌缩回瞬态。ALT 与左键 pin 行为不受影响。
     */
    public void setCtrlDown(boolean down) {
        if (this.ctrlDown == down) return; // 仅在状态翻转时处理，避免每帧抖动
        this.ctrlDown = down;
        if (!down && ctrlPinned != null) {
            ctrlPinned.window.close(); // 置不可见并触发 onClose（从 pinned 列表移除）
            ctrlPinned = null;
        }
    }

    // ------------------------------------------------------------------
    // 渲染
    // ------------------------------------------------------------------

    /**
     * 渲染「跟随光标的瞬态关键字链」。无关键字时返回 false（WindowLayer 回退原生 tooltip）。
     *
     * @param alpha 淡入量 0..1（WindowLayer 的 delay-fade；1 = 不透明现状）。盒子按该系数整体透明，
     *              纯视觉，不改几何 / 命中矩形。
     */
    public boolean renderTransient(GuiGraphicsExtractor g, int mx, int my, float pt, List<Component> baseLines, float alpha) {
        if (baseLines == null || baseLines.isEmpty()) return false;
        if (!reg.hasAnyKeyword(baseLines)) return false;

        List<ParsedLine> parsed = new ArrayList<>();
        for (Component c : baseLines) parsed.add(reg.parse(c));

        BoxLayout head = KeywordTooltipBox.layout(parsed, mx + 14, my + 14, screenW, screenH);
        if (head == null) return false;
        KeywordTooltipBox.draw(g, head, alpha);
        frameBoxes.add(head);
        drawChain(g, mx, my, head, 0, alpha);

        // Ctrl 长按：把当前悬停的关键字自动转成可拖拽/可关闭的长期窗口；松开在 setCtrlDown 中塌缩。
        if (ctrlDown) {
            // 玩家在按住 Ctrl 期间手动点关闭按钮：清掉陈旧引用，否则下一帧会拿已关窗口的 entry 比较而漏钉
            if (ctrlPinned != null && !ctrlPinned.window.visible) ctrlPinned = null;

            KeywordSpan hov = KeywordTooltipBox.hit(frameBoxes, mx, my);
            KeywordEntry hoverEntry = (hov != null) ? hov.entry : null;
            if (hoverEntry != null) {
                if (ctrlPinned == null || ctrlPinned.entry != hoverEntry) {
                    if (ctrlPinned != null) ctrlPinned.window.close();
                    ctrlPinned = pinInternal(hoverEntry, mx, my);
                }
            }
            // hoverEntry == null：保持已钉窗口，直至 Ctrl 松开
        }
        return true;
    }

    /** 为所有钉住的窗口绘制它们的子级盒子（钉住窗口正文已由其自身 Node 渲染）。 */
    public void renderPinnedChildren(GuiGraphicsExtractor g, int mx, int my, float pt) {
        for (Pinned p : pinned) {
            if (!p.window.visible) continue;
            BoxLayout head = p.rich.lastLayout();
            if (head == null) continue;
            frameBoxes.add(head);
            drawChain(g, mx, my, head, 0, 1F); // 钉住窗为持久态，不随瞬态淡入淡出
        }
    }

    /**
     * 从某个头部盒子递归画子级。头部盒子已绘制（瞬态）或已由钉住窗口自身绘制（钉住），
     * 本方法只负责头部<b>之内</b>被悬停关键字所引出的下一级、再下一级……
     *
     * @param alpha 瞬态淡入量（钉住窗链路恒传 1）。
     */
    private void drawChain(GuiGraphicsExtractor g, int mx, int my, BoxLayout head, int level, float alpha) {
        KeywordSpan hovered = hoverIn(head, mx, my);
        KeywordEntry target = hovered != null ? hovered.entry : (altDown ? sticky.get(level) : null);
        if (hovered != null && altDown) sticky.put(level, hovered.entry);
        if (target == null) return;

        List<ParsedLine> childLines = reg.compileDescription(target);
        int childW = KeywordTooltipBox.measureW(childLines);

        int cx = hovered != null ? hovered.absX + hovered.w + 8 : head.absX + head.width + 8;
        int cy = hovered != null ? hovered.absY : head.absY;
        if (cx + childW > screenW) cx = (hovered != null ? hovered.absX : head.absX) - childW - 8;
        if (cx < 0) cx = 0;

        BoxLayout child = KeywordTooltipBox.layout(childLines, cx, cy, screenW, screenH);
        if (child == null) return;
        KeywordTooltipBox.draw(g, child, alpha);
        frameBoxes.add(child);
        drawChain(g, mx, my, child, level + 1, alpha);
    }

    private static KeywordSpan hoverIn(BoxLayout box, int mx, int my) {
        for (KeywordSpan s : box.spans) {
            if (mx >= s.absX && mx < s.absX + s.w && my >= s.absY && my < s.absY + s.h) return s;
        }
        return null;
    }

    // ------------------------------------------------------------------
    // 交互：点击钉住
    // ------------------------------------------------------------------

    /** 左键点击命中某关键字 → 钉成持久窗口。返回 true 表示已消费该点击。 */
    public boolean handleClick(int mx, int my, int button) {
        if (button != 0) return false;
        KeywordSpan hit = KeywordTooltipBox.hit(frameBoxes, mx, my);
        if (hit == null) return false;
        pin(hit.entry, mx, my);
        return true;
    }

    /** 把某个关键字钉成持久窗口（运行期点击与展示屏预置共用此入口）。 */
    public void pin(KeywordEntry e, int mx, int my) {
        pinInternal(e, mx, my);
    }

    /** 钉窗核心逻辑；返回所创建的钉住记录，供 Ctrl 自动钉路径持有引用。 */
    private Pinned pinInternal(KeywordEntry e, int mx, int my) {
        Window w = Window.of(e.title).styled().closable(true);
        // C.9：正文直接吃片段数组编译结果（引用片段结构化可点），与瞬态链同源
        List<ParsedLine> descLines = reg.compileDescription(e);
        KeywordRichText rt = new KeywordRichText().parsed(descLines);
        w.body().add(rt);

        int[] d = KeywordTooltipBox.measureParsed(descLines);
        int winW = Math.max(120, d[0]);
        int winH = d[1] + Window.TITLE_BAR_HEIGHT + 2;
        w.flex.size(winW, winH);

        int px = mx + 12, py = my + 12;
        if (px + winW > screenW) px = screenW - winW - 4;
        if (py + winH > screenH) py = screenH - winH - 4;
        if (px < 0) px = 0;
        if (py < 0) py = 0;
        w.position(px, py);
        w.layoutSelf(); // 立即定位，避免首帧画在 (0,0)

        Pinned p = new Pinned(w, rt, e);
        w.onClose(() -> pinned.remove(p));
        layer.add(w);
        layer.bringToFront(w);
        pinned.add(p);
        return p;
    }

    private static final class Pinned {
        final Window window;
        final KeywordRichText rich;
        final KeywordEntry entry;
        Pinned(Window window, KeywordRichText rich, KeywordEntry entry) { this.window = window; this.rich = rich; this.entry = entry; }
    }
}
