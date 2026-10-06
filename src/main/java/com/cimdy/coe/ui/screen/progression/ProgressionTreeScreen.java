package com.cimdy.coe.ui.screen.progression;
import com.cimdy.coe.ui.widget.render.Theme;

import com.cimdy.coe.client.render.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * 科技树 / 研究树的共同外壳（chrome）基类。
 *
 * <p>两棵树的<b>内容</b>差别很大——科技树是等级分段的按钮网格，研究树是可平移缩放的 DAG 画布——
 * 但<b>外壳</b>完全一致：全屏深色底 + 右侧 1/7 信息面板 + 面板顶部两个互切页签 + 居中标题。
 * 这层外壳若在两类里各写一遍（约 90 行），任何一处调整都要改两个文件且极易漂移。
 * 本类把它收成唯一实现，子类只需给出主色（{@link #accent()}）与自己是哪一页（{@link #isTechScreen()}）。
 *
 * <p>另一个统一点是<b>坐标契约</b>：{@link #CONTENT_TOP} 之上属于标题带，之下属于可滚动内容区。
 * 子类绘制滚动内容前必须用 {@link #pushContentClip}/{@link #popContentClip} 裁剪，
 * 命中判定则用 {@link #inContentArea}——两者共用同一条线，避免「画出去了」或「点到了看不见的东西」。
 */
public abstract class ProgressionTreeScreen extends Screen {

    protected static final float RIGHT_PANEL_RATIO = 1f / 7f;
    protected static final int PAD = 4;
    protected static final int TAB_H = 18;
    protected static final int ROW_H = 12;

    /** 标题带高度：其上是不参与滚动的标题，其下是可滚动内容区。 */
    protected static final int CONTENT_TOP = 28;

    /** 页签配色：选中态的边框 / 底色 / 文字色。未选中态两棵树相同，写死在 {@link #drawTab}。 */
    protected record TabAccent(int border, int background, int text) {
    }

    /** 可为 null：从背包 / 指令直接打开时没有研究台，此时不能切到研究树。 */
    protected final BlockPos benchPos;
    protected final Player player;

    protected int rightPanelX, rightPanelW, contentX, contentW;
    protected int[] tabResearchRect, tabTechRect;

    private boolean clipPushed;

    protected ProgressionTreeScreen(Component title, BlockPos benchPos) {
        super(title);
        this.benchPos = benchPos;
        this.player = Minecraft.getInstance().player;
    }

    /** 本屏主色，用于标题与选中页签。 */
    protected abstract TabAccent accent();

    /** 本屏是不是科技树页（决定点自己那个页签时不做任何事）。 */
    protected abstract boolean isTechScreen();

    // ====== 布局 ======

    /** 右侧信息面板几何 + 两个页签矩形。子类 {@code init()} 的第一件事就该调用它。 */
    protected void initPanel() {
        rightPanelW = Math.max(140, (int) (width * RIGHT_PANEL_RATIO));
        rightPanelX = width - rightPanelW;
        contentX = rightPanelX + PAD;
        contentW = rightPanelW - 2 * PAD;
        tabResearchRect = new int[]{contentX, PAD, contentW / 2 - 1, TAB_H};
        tabTechRect = new int[]{contentX + contentW / 2 + 1, PAD, contentW - contentW / 2 - 1, TAB_H};
    }

    /** 左侧内容区宽度（右侧面板之外的部分）。 */
    protected int contentAreaWidth() {
        return rightPanelX;
    }

    /** 内容区可视高度（扣掉标题带）。 */
    protected int contentViewHeight() {
        return height - CONTENT_TOP;
    }

    // ====== 渲染基元 ======

    protected static Font font() {
        return Minecraft.getInstance().font;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        g.fill(0, 0, width, height, Theme.current().progressionScreenBg());
    }

    /** 内容区居中标题（画在标题带里，不参与滚动）。 */
    protected void drawTitle(GuiGraphicsExtractor g, Component title) {
        String s = title.getString();
        g.text(font(), Component.literal(s), (rightPanelX - font().width(s)) / 2, 6, accent().border(), true);
    }

    /** 右侧面板底板 + 分隔线 + 两个页签。子类接着往下画自己的信息行即可。 */
    protected void drawPanelChrome(GuiGraphicsExtractor g, int mx, int my) {
        g.fill(rightPanelX, 0, width, height, Theme.current().progressionPanelBg());
        g.fill(rightPanelX - 1, 0, rightPanelX, height, Theme.current().progressionPanelBorder());

        drawTab(g, tabResearchRect, Component.translatable("screen.coe.tab.research"),
                !isTechScreen(), hovering(mx, my, tabResearchRect));
        drawTab(g, tabTechRect, Component.translatable("screen.coe.tab.tech"),
                isTechScreen(), hovering(mx, my, tabTechRect));
    }

    /** 面板里的一行文字，返回下一行的 y。 */
    protected int line(GuiGraphicsExtractor g, int y, Component text, int color) {
        g.text(font(), text, contentX, y, color, true);
        return y + ROW_H;
    }

    protected void drawTab(GuiGraphicsExtractor g, int[] r, Component label, boolean selected, boolean hover) {
        TabAccent a = accent();
        int bg = selected ? a.background()
                : (hover ? Theme.current().progressionTabHoverBg() : Theme.current().progressionTabBg());
        int border = selected ? a.border() : Theme.current().progressionTabBorder();
        g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], bg);
        g.outline(r[0], r[1], r[2], r[3], border);
        g.centeredText(font(), label, r[0] + r[2] / 2, r[1] + (r[3] - 8) / 2,
                selected ? a.text() : Theme.current().progressionTabText());
    }

    // ====== 内容区裁剪 ======

    /**
     * 把后续绘制裁到内容区 {@code [0, CONTENT_TOP) → [rightPanelX, height)} 之内。
     *
     * <p>没有这层裁剪，向下滚动时内容会画到标题带甚至右侧面板上。必须与
     * {@link #popContentClip} 成对调用（内部做了重入保护，不会造成 scissor 栈失衡）。
     */
    protected void pushContentClip(GuiGraphicsExtractor g) {
        if (clipPushed) return;
        g.enableScissor(0, CONTENT_TOP, rightPanelX, height);
        clipPushed = true;
    }

    protected void popContentClip(GuiGraphicsExtractor g) {
        if (!clipPushed) return;
        g.disableScissor();
        clipPushed = false;
    }

    /** 鼠标是否落在可滚动内容区内（与 {@link #pushContentClip} 用同一条边界，杜绝「点到看不见的元素」）。 */
    protected boolean inContentArea(int mx, int my) {
        return mx >= 0 && mx < rightPanelX && my >= CONTENT_TOP && my < height;
    }

    // ====== 交互 ======

    protected static boolean hovering(int mx, int my, int[] r) {
        return r != null && RenderHelper.isHoverArea(mx, my, r[0], r[1], r[2], r[3]);
    }

    /**
     * 处理两个页签的点击；返回 true 表示已消费。
     *
     * <p>切页统一走 {@link ProgressionScreens}，而不是各自 {@code new XxxScreen(...)}，
     * 保证「打开进度界面」只有一个入口。研究树必须由研究台发起，故 {@code benchPos == null} 时不切。
     */
    protected boolean handleTabClick(int mx, int my) {
        if (hovering(mx, my, tabTechRect)) {
            if (!isTechScreen()) ProgressionScreens.openTech(benchPos);
            return true;
        }
        if (hovering(mx, my, tabResearchRect)) {
            if (isTechScreen() && benchPos != null) ProgressionScreens.openResearch(benchPos);
            return true;
        }
        return false;
    }

    /** 页签点击 + 面板区吞点击（避免误触到底下的画布 / 网格）。 */
    protected boolean consumedByPanel(int mx, int my) {
        return handleTabClick(mx, my) || mx >= rightPanelX;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= rightPanelX) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
