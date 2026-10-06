package com.cimdy.coe.ui.screen;

import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.screen.WidgetScreen;
import com.cimdy.coe.ui.widget.keyword.KeywordEntry;
import com.cimdy.coe.ui.widget.keyword.KeywordRegistry;
import com.cimdy.coe.ui.widget.keyword.KeywordTooltipBox;
import com.cimdy.coe.ui.widget.keyword.KeywordTooltipHost;
import net.minecraft.network.chat.Component;

/**
 * 关键字参考展示屏（K 键打开）：预置若干代表性关键字为已钉住窗口，
 * 直观演示关键字系统的三层交互——悬停子术语弹出下级、按住 ALT 滞留并可继续钻取（无限递归）、
 * 左键点击子术语钉成更深一层窗口。窗口正文即 {@link com.cimdy.coe.ui.widget.keyword.KeywordRichText}，
 * 由 {@link KeywordTooltipHost} 驱动递归，故本屏是系统端到端可用性的联调 / 展示面。
 *
 * <p>预置窗口经由 {@link KeywordTooltipHost#pin} 登记进 host 的 pinned 列表，
 * 因而其内部的悬停 / ALT 钻取 / 点击钉深一层都能正常触发（与运行期点击钉住走同一路径）。
 */
public class KeywordShowcaseScreen extends WidgetScreen {

    private static final String[] DEMO = {
            "ailment", "critical", "armour", "damage", "skill", "resistance"
    };

    public KeywordShowcaseScreen() {
        super(Component.literal(
                "Keyword Reference — hover sub-terms · hold ALT to drill · left-click to pin"));
    }

    @Override
    protected void buildUi(WindowLayer root) {
        KeywordTooltipHost host = root.keywordHost();
        KeywordRegistry reg = KeywordRegistry.instance();
        int x = 40, y = 40;
        for (String id : DEMO) {
            KeywordEntry e = reg.get(id);
            if (e == null) continue;
            // 用 host.pin 预置窗口：自动登记进 pinned 列表，递归 / ALT / 点击才生效
            host.pin(e, x - 12, y - 12);
            // C.9：尺寸按片段数组编译结果量算（引用片段已结构化登记）
            int[] d = KeywordTooltipBox.measureParsed(reg.compileDescription(e));
            int winW = Math.max(180, d[0]);
            int winH = d[1] + Window.TITLE_BAR_HEIGHT + 2;
            x += winW + 30;
            if (x > this.width - winW) {
                x = 40;
                y += winH + 30;
            }
        }
    }
}
