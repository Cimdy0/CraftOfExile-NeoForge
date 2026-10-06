package com.cimdy.coe.ui.screen.radial;
import com.cimdy.coe.ui.widget.render.Theme;

import com.cimdy.coe.client.InputState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * FPS 风格的径向（轮盘）菜单。
 *
 * <p>交互方式：
 * <ul>
 *   <li><b>按住</b>绑定键时显示菜单；</li>
 *   <li>移动鼠标，指针所在扇区高亮（拖动选择）；</li>
 *   <li><b>左键点击</b>触发高亮项：分支节点进入其子菜单，叶子节点执行动作并关闭；</li>
 *   <li><b>右键</b>或点击中心枢纽：返回上一级（根级则关闭）；</li>
 *   <li><b>松开</b>绑定键或按 <b>ESC</b>：关闭菜单（取消）。</li>
 * </ul>
 *
 * <p>菜单结构完全由 {@link RadialNode} 树驱动，本类不硬编码任何条目；
 * 天赋树列表等动态内容在打开时才从数据包解析，新增天赋树零代码。
 *
 * <p>注意：本类刻意不使用 {@code @Override} 标注 {@code extractBackground} / {@code extractRenderState}——
 * 它们由模组渲染框架按名调用，并非 Screen 的超类方法。
 *
 * <p>26.2 起，Screen 的输入回调改用事件 record（{@link MouseButtonEvent} / {@link KeyEvent}），
 * 旧的 {@code (double,double,int)} 系列签名已全部移除——若仍按旧签名写，方法不会进入多态链路而<b>静默失效</b>
 * （轮盘不可点、ESC 无响应）。下列 mouseClicked / mouseReleased / mouseDragged / keyPressed 已迁到新签名并补
 * {@code @Override}，坐标与按键位从事件 record 取（{@code event.x()/y()/button()/key()}）。
 */
public class RadialMenuScreen extends Screen {
    private final RadialNode root;
    private final Deque<RadialNode> stack = new ArrayDeque<>();
    private RadialNode current;
    private int highlighted = -1;
    private boolean firstTick = true;

    public RadialMenuScreen(RadialNode root) {
        super(Component.literal("radial_menu"));
        this.root = root;
        this.current = root;
    }

    /** 不暂停游戏（轮盘菜单是悬浮操作层）。 */
    public boolean isPauseScreen() {
        return false;
    }

    /** 松开绑定键即关闭（取消）。首 tick 放宽，避免按下瞬间的误判。 */
    public void tick() {
        if (firstTick) {
            firstTick = false;
        } else if (!InputState.OPEN_MENU.isDown()) {
            Minecraft.getInstance().setScreenAndShow(null);
        }
    }

    public void extractBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        gui.fill(0, 0, this.width, this.height, Theme.current().radialBackdrop());
    }

    public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int outerR = Math.min(this.width, this.height) / 3;
        int innerR = outerR * 5 / 12;
        int midR = (outerR + innerR) / 2;
        int nodeR = (outerR - innerR) / 2 - 2;

        List<RadialNode> items = current.children();
        int n = items.size();
        highlighted = computeHighlight(mouseX, mouseY, cx, cy, innerR, outerR, n);

        // 中心枢纽：有父级则显示「返回」，否则「关闭」
        boolean hasParent = !stack.isEmpty();
        fillCircle(gui, cx, cy, innerR - 2, hasParent ? Theme.current().radialHubFill() : Theme.current().borderDisabled());
        gui.centeredText(Minecraft.getInstance().font,
                hasParent ? Component.translatable("ui.coe.back") : Component.translatable("ui.coe.close"),
                cx, cy - 4, Theme.current().radialTextIdle());

        if (n == 0) return;

        double step = 360.0 / n;
        for (int i = 0; i < n; i++) {
            double ang = Math.toRadians(-90 + i * step);
            int x = (int) (cx + midR * Math.cos(ang));
            int y = (int) (cy + midR * Math.sin(ang));
            boolean hot = i == highlighted;
            int border = hot ? Theme.current().radialBorderHot() : Theme.current().radialBorderIdle();
            int fill = hot ? Theme.current().radialFillHot() : Theme.current().radialFillIdle();
            fillCircle(gui, x, y, nodeR + 2, border);
            fillCircle(gui, x, y, nodeR, fill);
            ItemStack icon = items.get(i).icon;
            if (icon != null && !icon.isEmpty()) {
                gui.item(icon, x - 8, y - 8);
            }
            gui.centeredText(Minecraft.getInstance().font, items.get(i).title, x, y + nodeR + 10,
                    hot ? Theme.current().radialTextHot() : Theme.current().radialTextIdle());
        }
    }

    private int computeHighlight(int mx, int my, int cx, int cy, int innerR, int outerR, int n) {
        if (n == 0) return -1;
        double dx = mx - cx, dy = my - cy;
        double dist = Math.hypot(dx, dy);
        if (dist < innerR || dist > outerR * 1.15) return -1;
        double deg = Math.toDegrees(Math.atan2(dy, dx));
        double rel = (deg + 90 + 360) % 360;
        int idx = (int) Math.round(rel / (360.0 / n)) % n;
        if (idx < 0) idx += n;
        return idx;
    }

    /** 用横向扫描线绘制实心圆（仅依赖 GuiGraphics 的 fill，无需自定义 GL）。 */
    private void fillCircle(GuiGraphicsExtractor gui, int cx, int cy, int r, int color) {
        if (r <= 0) return;
        long r2 = (long) r * r;
        for (int yy = -r; yy <= r; yy++) {
            int hw = (int) Math.sqrt(r2 - (long) yy * yy);
            gui.fill(cx - hw, cy + yy, cx + hw + 1, cy + yy + 1, color);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        int button = event.button();
        int cx = this.width / 2, cy = this.height / 2;
        double dist = Math.hypot(mx - cx, my - cy);
        int innerR = Math.min(this.width, this.height) / 3 * 5 / 12;

        if (button == 1) { // 右键：返回 / 关闭
            backOrClose();
            return true;
        }
        if (button != 0) return true;

        if (dist < innerR) { // 点击中心枢纽
            backOrClose();
            return true;
        }
        int outerR = Math.min(this.width, this.height) / 3;
        int n = current.children().size();
        int idx = computeHighlight((int) mx, (int) my, cx, cy, innerR, outerR, n);
        if (idx < 0 || idx >= n) return true;
        RadialNode node = current.children().get(idx);
        if (node.hasChildren()) {
            stack.push(current);
            current = node;
        } else if (node.action != null) {
            node.action.run();
            // C.7：动作可能已直接切到新屏（如「按键设置」叶打开 KeyBindsScreen）；
            // 仅当仍停留在本径向屏时才关闭回游戏，否则保留新切到的屏。
            // 发包型叶（技能装配/天赋）由服务端稍后异步开屏，此时本屏仍是自己 → 正常关闭。
            if (Minecraft.getInstance().gui.screen() == this) {
                Minecraft.getInstance().setScreenAndShow(null);
            }
        }
        return true;
    }

    private void backOrClose() {
        if (!stack.isEmpty()) {
            current = stack.pop();
        } else {
            Minecraft.getInstance().setScreenAndShow(null);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreenAndShow(null);
            return true;
        }
        return true; // 吞掉其余按键，避免误触底层游戏
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return true;
    }
}
