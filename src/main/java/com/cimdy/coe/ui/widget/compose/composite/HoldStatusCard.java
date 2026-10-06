package com.cimdy.coe.ui.widget.compose.composite;

import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.component.TextInput;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 据点状态卡（第 2 层复合组件，计划 §二 通用复合组件表 / §3.1 SiteScreen 右栏）。
 *
 * <p>包装功能组件 {@link StatusCard}（= {@code Section} + {@code KeyValue} 的只读状态表），
 * 并在其下方叠加「改名输入框」与「升级按钮」两个<b>待服务端</b>控件：
 *
 * <ul>
 *   <li>{@link #renameBox(TextInput)}：传入的输入框被强制禁用、不可编辑，
 *       并附「待服务端」标注（计划 §六：据点改名写回包尚未就绪）。</li>
 *   <li>{@link #upgradeButton(Button)}：传入的升级按钮被强制禁用，附「待服务端」标注
 *       （计划 §六：{@code renameHold/updateHoldLevel} 服务端方法在，但无写回包）。</li>
 * </ul>
 *
 * <p>组件只做展示骨架 + 禁用态 + 标注，<b>不发任何包</b>；Screen 注入已构造好的输入框/按钮，
 * 待服务端包就绪后只需去掉禁用态即可启用。{@link #set} 每次重建状态卡，避免行累积。
 *
 * <pre>{@code
 * HoldStatusCard card = HoldStatusCard.of()
 *     .set(holdName, List.of(tierLine, energyLine, modeLine))
 *     .renameBox(renameInput)
 *     .upgradeButton(upgradeBtn);
 * }</pre>
 *
 * <p><b>潜在复用方</b>：SiteScreen 右栏、TerminalScreen 据点总览、未来任何「只读实体状态 +
 * 待开通写回」的卡片。
 */
public class HoldStatusCard extends Node {

    private final Node cardMount = new Node().id("hold-status-card-body");
    private final Node actions = new Node().column().gap(2).id("hold-status-actions");
    private final Node pendingNote;

    public HoldStatusCard() {
        this.id = "hold-status-card";
        column().gap(2);

        // 「待服务端」标注小字（placeholder 色，SCALE_META）
        TextRendererI note = Renderers.text(Component.translatable("ui.coe.pending_server"))
                .color(Theme.current().placeholderText())
                .align(Align.LEFT)
                .vAlign(VAlign.MIDDLE);
        note.fixedScale(CoeUi.SCALE_META);
        pendingNote = new Node().id("hold-status-pending").with(note);
        pendingNote.visible(false);

        add(cardMount);
        add(actions);
        add(pendingNote);
    }

    public static HoldStatusCard of() {
        return new HoldStatusCard();
    }

    // ------------------------------------------------------------------
    // 装配（规格 §2 签名）
    // ------------------------------------------------------------------

    /**
     * 填状态：状态卡标题 = 据点名；{@code values} 为一组展示行（每行一条整文案，
     * 如「等级 3」「能量 120/200」「工作模式：自动」）。每次调用重建 {@link StatusCard}。
     */
    public HoldStatusCard set(Component name, List<Component> values) {
        cardMount.clearChildren();
        StatusCard card = StatusCard.of();
        card.title(name == null ? Component.empty() : name);
        if (values != null) {
            for (Component v : values) {
                if (v == null) continue;
                card.add(Component.empty(), v);
            }
        }
        cardMount.add(card);
        return this;
    }

    /**
     * 改名输入框（待服务端）：强制禁用 + 不可编辑，并展示「待服务端」标注。
     * 包就绪后由 Screen 去掉禁用态即可。
     */
    public HoldStatusCard renameBox(TextInput box) {
        if (box == null) return this;
        box.disabled(true);
        box.editable(false);
        actions.add(CoeUi.settingRow(Component.translatable("hold.coe.name"), box));
        pendingNote.visible(true);
        return this;
    }

    /**
     * 升级按钮（待服务端）：强制禁用，并与改名输入框共用「待服务端」标注。
     */
    public HoldStatusCard upgradeButton(Button b) {
        if (b == null) return this;
        b.disabled(true);
        actions.add(CoeUi.settingRow(Component.translatable("hold.coe.upgrade"), b));
        pendingNote.visible(true);
        return this;
    }
}
