package com.cimdy.coe.ui.screen;
import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.attach.AttachmentPresets;
import com.cimdy.coe.ui.widget.render.Renderers;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.flex.enums.JustifyContent;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.screen.WidgetScreen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 通用模态对话框屏：标题 + 多行信息 + 底部按钮行（确认 / 可选取消）。
 * 经 {@link CoeUi#confirm} 一行调用即可打开，把「弹一个确认框」收敛进库，简化 Screen 构建。
 *
 * <p>纯客户端只读式弹层，走与 {@link CodexScreen} 相同的 {@code setScreenAndShow} 路径（免 Menu / 网络）。
 */
public class DialogScreen extends WidgetScreen {

    /** 对话框按钮回调。 */
    public interface Handler {
        /** 点击确认。 */
        void onConfirm();
        /** 点击取消（仅 cancelable 时出现）。 */
        void onCancel();
    }

    private final Component title;
    private final List<Component> message;
    private final Handler handler;
    private final boolean cancelable;
    private Window window;

    public DialogScreen(Component title, List<Component> message, Handler handler, boolean cancelable) {
        super(title);
        this.title = title;
        this.message = message;
        this.handler = handler;
        this.cancelable = cancelable;
    }

    @Override
    protected void buildUi(WindowLayer root) {
        window = CoeUi.dialog(title, 300, 150).onClose(this::onClose);
        window.center(this.width, this.height);
        // 弹窗默认附件（STANDARD）：标题栏高光带 + 标准音效族（打开/确认/取消）。
        // CoeUi.dialog 已走 styled() → D.2 背景纹理在。特效按 D.5 默认关（Vfx.MASTER_ENABLED=false），不挂 vfx 工厂。
        window.attach(
                AttachmentPresets.decorFor(AttachmentPresets.defaults(), AttachmentPresets.Target.DIALOG),
                AttachmentPresets.sfxFor(AttachmentPresets.defaults(), AttachmentPresets.Target.DIALOG));

        Node body = new Node().column();
        body.flex.grow = 1;
        body.flex.gapMain = CoeUi.PAD;

        Node msg = new Node().column();
        msg.flex.grow = 1;
        msg.flex.gapMain = 2;
        for (Component line : message) {
            Node row = new Node().with(Renderers.text(line)
                    .align(Align.LEFT)
                    .vAlign(VAlign.TOP)
                    .wrap(true));
            row.flex.height = 14;
            msg.add(row);
        }
        body.add(msg);

        Node buttons = new Node().row();
        buttons.flex.justifyContent = JustifyContent.FLEX_END;
        buttons.flex.gapMain = CoeUi.PAD;

        Button ok = CoeUi.button(Component.translatable("ui.coe.ok"))
                .onClick(() -> { if (handler != null) handler.onConfirm(); window.close(); });
        ok.flex.size(60, 20);
        buttons.add(ok);

        if (cancelable) {
            Button cancel = CoeUi.button(Component.translatable("ui.coe.cancel"))
                    .onClick(() -> { if (handler != null) handler.onCancel(); window.close(); });
            cancel.flex.size(60, 20);
            buttons.add(cancel);
        }

        body.add(buttons);
        window.add(body);

        // 窗口淡入 + 微缩放 0.96→1（160ms）。Window 非 AnimatedNode，套一层铺满屏幕的包装节点，
        // 把窗口 flex 居中；变换围绕屏幕中心（即居中窗口的中心）进行。命中/关闭链路不变：
        // 包装层全屏接收点击后分发窗口，ESC/关闭按钮仍走 Screen.onClose 关闭整个界面。
        AnimatedNode stage = new AnimatedNode();
        stage.id("dialog-stage");
        stage.justify(JustifyContent.CENTER).alignItems(AlignItems.CENTER);
        stage.add(window);
        stage.anim().scale = 0.96F;
        stage.anim().alpha = 0F;
        stage.scaleTo(1F, 160).fadeIn(160);
        root.add(stage);
    }
}
