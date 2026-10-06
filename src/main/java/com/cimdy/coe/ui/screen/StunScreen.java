package com.cimdy.coe.ui.screen;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.status.StatusHudEntry;
import com.cimdy.coe.client.render.RenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * 玩家硬控（眩晕）表现：无渲染 Screen——打开即拦截玩家输入/移动（简单控制），
 * 屏幕中央仅显示「眩晕」大字；眩晕结束（轻晕/重晕均从同步快照 STATUS_HUD 消失）自动关闭。
 *
 * <p>数据来源说明：{@code STATUS_STORE} 不同步客户端，故关闭判定读同步附件
 * {@link com.cimdy.coe.registry.world.AttachmentRegistry#STATUS_HUD}（服务端周期性汇总），
 * 而非 {@code StatusEngine.has}（客户端恒为 false）。
 */
public class StunScreen extends Screen {
    private final Player player;

    public StunScreen(Player player, Component title) {
        super(title);
        this.player = player;
    }

    @Override
    public void tick() {
        if (!isPlayerStunned()) {
            this.onClose();
        }
    }

    /** 轻晕（STUN）/重晕（HEAVY_STUN）任一生效即视为被眩晕。 */
    public boolean isPlayerStunned() {
        List<StatusHudEntry> effects = this.player.getData(AttachmentRegistry.STATUS_HUD);
        if (effects == null || effects.isEmpty()) return false;
        return effects.stream().anyMatch(e ->
                e.id().equals(StatusDefs.STUN.id) || e.icon().equals(StatusDefs.HEAVY_STUN_ICON));
    }

    // ── 输入拦截：眩晕期间消费所有输入事件，阻断穿透到游戏（WASD 移动 / 点击世界 / 滚轮等）──
    // 26.2 起 Screen 输入回调改用事件 record（MouseButtonEvent/KeyEvent/CharacterEvent）；
    // 旧的 (double,double,int) 系列签名已移除，不迁过来则拦截静默失效（眩晕期间仍可操作）。

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int p_281550_, int p_282878_, float p_282465_) {
        Component component = Component.translatable("stat.stun");
        int i = this.width / 2;
        int j = this.height / 2;
        RenderHelper.renderCenteredStringWithScale(guiGraphics, component, i, j, Theme.current().textBright(), 3.0F);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

}
