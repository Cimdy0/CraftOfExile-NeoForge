package com.cimdy.coe.mixin.client.screen;

import com.cimdy.coe.ui.widget.compose.inventory.ExtraSlotsFloatWindow;
import com.cimdy.coe.ui.widget.screen.WidgetHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 原版背包界面注入一个「药剂 / 护符 / 饰品」按钮（C.10）。
 *
 * <p><b>设计演进</b>：早期该按钮发送 {@code OpenExtraSlotsMenuPacket}，由服务端 {@code openMenu}
 * 另起一个独立容器屏 {@code ExtraSlotsScreen}（MC 单屏栈下会顶掉当前背包屏）。C.10 要求
 * <b>非单独 Screen</b>：改为在原版背包界面上叠一个 {@link ExtraSlotsFloatWindow}（mod 自研
 * WindowLayer 浮窗），背包屏不被替换。
 *
 * <p><b>当前为 UI 骨架</b>：本 mixin 在原版 {@link InventoryScreen} 内持有一个 {@link WidgetHost}
 * （WindowLayer 宿主），按钮切换浮窗显隐；浮窗每帧在 {@code extractRenderState} TAIL 叠加渲染。
 * 浮窗内三列槽数实时只读自 Flask/Charm/Accessory 注册表（见 {@link ExtraSlotsFloatWindow}）。
 *
 * <p><b>待服务端</b>：浮窗内槽位物品渲染与「取/放」交互尚未接入——需复用 {@code ExtraSlotsMenu}
 * 服务端权威链路（只读调用本 shard 不代写服务端逻辑），待浮窗宿主接管容器后在此接线。
 * 包就绪前浮窗只画空槽框，按钮切换显隐，不拦截背包原生槽位交互。
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {

    /** 背包界面左上角坐标（来自目标 {@code AbstractContainerScreen}，经 @Shadow 接入）。 */
    protected int leftPos;
    protected int topPos;

    /** 阴影接入原版 {@code Screen#addRenderableWidget}（protected 泛型方法，需经 @Shadow 才能从 mixin 调用）。
     *  真实签名为 {@code <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T)}，
     *  擦除后描述符为 {@code (GuiEventListener)GuiEventListener}；声明为泛型 {@code <T extends GuiEventListener>}
     *  即可使擦除描述符匹配、正确定位目标方法（旧写法 (Object)Object 擦除不符，导致 mixin 应用失败）。 */
    protected abstract <T extends GuiEventListener> T addRenderableWidget(T widget);

    /** C.10：浮窗宿主（mod 自研 WindowLayer 体系），由本 mixin 在原版背包屏内持有点亮渲染。 */
    @Unique private WidgetHost coe$extraHost;
    /** C.10：药剂/护符/饰品三列浮窗（只读骨架）。 */
    @Unique private ExtraSlotsFloatWindow coe$extraWindow;

    @Unique
    private void coe$ensureExtraHost() {
        if (coe$extraHost != null) return;
        coe$extraHost = new WidgetHost();
        coe$extraWindow = new ExtraSlotsFloatWindow();
        Minecraft mc = Minecraft.getInstance();
        coe$extraHost.resize(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
    }

    /** 在背包界面注入「药剂 / 护符 / 饰品」按钮（背包面板右侧空白处，原 Flask 面板位置附近）。 */
    @Inject(method = "init", at = @At("TAIL"))
    private void coe$addExtraSlotsButton(CallbackInfo ci) {
        coe$ensureExtraHost();
        int bx = this.leftPos + 178;
        int by = this.topPos + 70;
        Button button = Button.builder(Component.translatable("ui.coe.flask_charm_trinket"),
                        b -> {
                            coe$ensureExtraHost();
                            coe$extraWindow.toggle(coe$extraHost.layer());
                        })
                .pos(bx, by).size(72, 18).build();
        this.addRenderableWidget(button);
    }

    /** C.10：每帧在原版背包渲染之后叠加绘制浮窗（视觉骨架；不拦截鼠标/键盘）。
     *  目标 {@code InventoryScreen#extractRenderState(GuiGraphicsExtractor,int,int,float)} 已确认重写；
     *  写全描述符以与同类中 private static {@code extractRenderState(LivingEntity)} 重载消歧。 */
    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("TAIL"))
    private void coe$renderExtraOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (coe$extraHost == null) return;
        coe$extraHost.render(graphics, mouseX, mouseY, partialTick);
    }
}
