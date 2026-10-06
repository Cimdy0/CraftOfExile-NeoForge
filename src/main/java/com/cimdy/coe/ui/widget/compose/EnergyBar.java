package com.cimdy.coe.ui.widget.compose;

import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.anim.Animator;
import com.cimdy.coe.ui.widget.anim.Easing;
import com.cimdy.coe.ui.widget.attach.Vfx;
import com.cimdy.coe.ui.widget.component.bar.Slider;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import net.minecraft.network.chat.Component;

/**
 * 数值 / 能量余额条（第 1 层功能组件，唯一实现点 —— 计划一 EnergyBar）。
 *
 * <p>复用 {@link Slider}（即 ValueBar 渲染）做只读进度条 + 顶部标签与数值。
 *
 * <p>动画（计划四·五）：
 * <ul>
 *   <li>数值变化 250ms 平滑 lerp（EASE_IN_OUT）；</li>
 *   <li>跌破低阈值时 Pulse 300ms 警告 + {@code Vfx.glow} 低能量光带（挂 Vfx 附件）。</li>
 * </ul>
 *
 * <p>颜色全部走 {@link Theme#current()}。低值光带为一次性特效（边缘触发），不空转。
 */
public class EnergyBar extends AnimatedNode {

    /** 数值 lerp 时长（计划四·五：EnergyBar 数值 250ms）。 */
    private static final int VALUE_MS = 250;
    /** 低值 Pulse 时长（计划四·五：低值 Pulse 300ms）。 */
    private static final int PULSE_MS = 300;

    private final Slider bar;
    private final TextRendererI labelText;
    private final TextRendererI valueText;

    /** 数值补间：target 变化时 retarget，tick 内写回 bar。 */
    private final Animator valueAnim = new Animator(0F, 0F, VALUE_MS, Easing.EASE_IN_OUT_CUBIC);
    private double target = 0;
    private double min = 0, max = 100;
    /** 低阈值（归一化进度 0..1）。 */
    private double lowThreshold = 0.25;
    private boolean wasLow = false;

    /** 低能量光带特效附件（边缘触发一次性发光，失败静默）。 */
    private final Vfx vfx = new Vfx();

    private EnergyBar() {
        this.id = "energy-bar";
        column();
        flex.gapMain = 2;
        attach(vfx);

        // ---- 顶部行：标签 + 数值 ----
        Node top = new Node().row();
        top.alignItems(AlignItems.CENTER);
        top.flex.gapMain = 4;

        labelText = Renderers.text(Component.empty())
                .align(Align.LEFT)
                .vAlign(VAlign.MIDDLE)
                .fixedScale(CoeUi.SCALE_META)
                .color(Theme.current().textMuted());
        Node labelNode = new Node().with(labelText);
        labelNode.grow(1);
        top.add(labelNode);

        valueText = Renderers.text(Component.literal("0"))
                .align(Align.RIGHT)
                .vAlign(VAlign.MIDDLE)
                .fixedScale(CoeUi.SCALE_META)
                .color(Theme.current().textBright());
        Node valueNode = new Node().with(valueText);
        top.add(valueNode);
        add(top);

        // ---- 进度条（只读 ValueBar 复用）----
        bar = Slider.of(min, max);
        bar.interactive(false);          // 只读：禁拖拽
        bar.showHandle(false);           // 不画把手
        bar.trackColor(Theme.current().progressTrackColor());
        bar.fill(Theme.current().progressFill());
        bar.flex.height = 6;
        add(bar);
    }

    public static EnergyBar of() { return new EnergyBar(); }

    // ------------------------------------------------------------------
    // 数据
    // ------------------------------------------------------------------

    /** 顶部左侧标签。 */
    public EnergyBar label(Component text) {
        labelText.setLine(0, text == null ? Component.empty() : text);
        return this;
    }

    /** 量程。 */
    public EnergyBar range(double lo, double hi) {
        this.min = lo;
        this.max = hi;
        bar.range(lo, hi);
        return this;
    }

    /** 设当前值（目标值，250ms 平滑逼近）。 */
    public EnergyBar value(double v) {
        this.target = v;
        valueAnim.retarget((float) v);
        valueText.setLine(0, Component.literal(String.valueOf(Math.round(v))));
        return this;
    }

    /** 低阈值（归一化进度 0..1，跌破即告警）。 */
    public EnergyBar lowThreshold(double frac) {
        this.lowThreshold = Math.max(0F, Math.min(1F, (float) frac));
        return this;
    }

    // ------------------------------------------------------------------
    // tick：推进数值补间 + 低值告警边缘检测
    // ------------------------------------------------------------------

    @Override
    public void tick() {
        valueAnim.tick();
        bar.value(valueAnim.value());

        double span = max - min;
        double progress = span <= 0 ? 0 : (valueAnim.value() - min) / span;
        boolean low = progress < lowThreshold;

        if (low && !wasLow) {
            // 刚跌破阈值：Pulse 300ms + 顶部低能量光带（一次性）
            pulse(0.05F, PULSE_MS);
            int warn = Theme.current().toastWarn();
            vfx.glow(contentWidth() / 2F, 2F, 6F, 30, warn);
        }
        wasLow = low;
        super.tick();
    }
}
