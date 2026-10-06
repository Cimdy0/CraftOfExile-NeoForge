package com.cimdy.coe.ui.widget.compose.business;

import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.compose.LabeledSlider;
import com.cimdy.coe.ui.widget.compose.WorkerRow;
import com.cimdy.coe.ui.widget.compose.badge.StatusGlyph;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.button.ToggleWidget;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.flex.enums.JustifyContent;
import com.cimdy.coe.ui.widget.render.Draw;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * 站点设置表单（业务复合组件，计划 §二 业务复合组件表 / §3.1 SiteScreen 左栏）。
 *
 * <p>把「站点」这一类实体的配置表单固化为一个可复用组件：
 *
 * <ul>
 *   <li><b>优先级</b>：5 档离散按钮组（Timberborn 工作站点参考，选中态用明显底色，不用滑条）。
 *       档位标签由 {@link #priorityOptions} 注入，选中序号经 {@code IntConsumer} 上抛。</li>
 *   <li><b>自动派遣总闸</b>：{@link ToggleWidget}（注入）。开启时工人步进 {@link #workerStepper}
 *       自动灰显（Timberborn 原版行为：自动派遣接管，手动步进失效）。</li>
 *   <li><b>工人步进</b>：{@link LabeledSlider}（注入），最小/最大范围由 Screen 定。</li>
 *   <li><b>已指派工人</b>：{@link WorkerRow} 只读列表（注入）。</li>
 *   <li><b>存储摘要</b>：一行「已绑定 x/y 容器」（注入文案）。</li>
 *   <li><b>保存按钮</b>：{@link Button}（注入）。组件<b>不发包</b>——合并 {@code SiteSettingPacket}
 *       由 Screen 在按钮回调里完成，组件只负责布局与内置的「自动派遣→灰显步进」联动。</li>
 * </ul>
 *
 * <pre>{@code
 * SiteForm form = SiteForm.of()
 *     .priorityOptions(priorityBars, priorityTips, currentIdx, idx -> site.setPriority(idx + 1))
 *     .autoDispatch(autoToggle)
 *     .workerStepper(workerSlider)
 *     .workers(workerRows)
 *     .storageSummary(Component.translatable("hold.coe.storages_bound", bound, max));
 * // 四·七 B：不再 .saveButton(...) —— 改即同步，发包由 Screen 在各控件回调里完成。
 * }</pre>
 *
 * <p><b>待服务端</b>：手动指派/移除工人（{@code SiteSetting} 无字段）——工人区只放只读 {@link WorkerRow}，
 * 「指派」按钮由 Screen 以禁用态注入并标注，本组件不编造写回。
 *
 * <p><b>潜在复用方</b>：SiteScreen / 未来任何「单实体配置表单」（优先级 + 总闸 + 步进 + 指派列表 + 保存）。
 */
public class SiteForm extends Node {

    private final Node priorityMount = new Node().id("site-form-priority");
    private final Node autoMount = new Node().id("site-form-auto");
    private final Node stepperMount = new Node().id("site-form-stepper");
    private final Node workersMount = new Node().column().gap(1).id("site-form-workers");
    private final Node summaryMount = new Node().id("site-form-summary");
    private final Node saveMount = new Node().id("site-form-save");

    private final List<Button> priorityButtons = new ArrayList<>();
    private LabeledSlider workerStepper;

    public SiteForm() {
        this.id = "site-form";
        column().gap(2);

        add(priorityMount);
        add(autoMount);
        add(stepperMount);
        add(workersMount);
        add(summaryMount);
        add(saveMount);
    }

    public static SiteForm of() {
        return new SiteForm();
    }

    // ------------------------------------------------------------------
    // 装配（规格 §2 签名）
    // ------------------------------------------------------------------

    /**
     * 优先级档位按钮组（四·七 A：标签已是档位格 bars，文字说明走 {@code tooltips}）。
     * {@code labels} 为每档图标（由 Screen 现取以防换肤冻死）；{@code tooltips} 为每档文字提示（可空）；
     * {@code selectedIndex} 为初始高亮档（0 基，<0 不预选）。点击某档 → 高亮该档并上抛其 0 基序号。
     */
    public SiteForm priorityOptions(List<Component> labels, List<Component> tooltips, int selectedIndex,
                                   IntConsumer onPriority) {
        priorityMount.clearChildren();
        priorityButtons.clear();
        if (labels == null || labels.isEmpty()) return this;

        Node group = new Node().row().gap(2).id("site-form-priority-group");
        for (int i = 0; i < labels.size(); i++) {
            final int index = i;
            Button b = Button.of(labels.get(i)).styled();
            b.flex.height = 12;
            if (b.labelRenderer() != null) b.labelRenderer().fixedScale(CoeUi.SCALE_BTN);
            if (tooltips != null && index < tooltips.size() && tooltips.get(index) != null) {
                b.tooltip(tooltips.get(index));
            }
            b.onClick(() -> {
                for (int k = 0; k < priorityButtons.size(); k++) {
                    priorityButtons.get(k).active(k == index);
                }
                if (onPriority != null) onPriority.accept(index);
            });
            priorityButtons.add(b);
            group.add(b);
        }
        if (selectedIndex >= 0 && selectedIndex < priorityButtons.size()) {
            priorityButtons.get(selectedIndex).active(true);
        }
        priorityMount.add(CoeUi.settingRow(Component.translatable("hold.coe.priority"), group));
        return this;
    }

    /**
     * 优先级<b>档位格</b>组（计划 §四·七 A 加法式，与 {@link #priorityOptions} 并存、不改其签名与行为）。
     *
     * <p>{@code levelCount} 自动夹到 3~5；每档是一个可点单元——按钮皮肤（hover/pressed/active 高亮）
     * 内嵌 {@link StatusGlyph#bars(int, int, StatusGlyph.Tint)}（C.3 分歧式：左半红系/中心中性/右半绿系，亮格数=|i-center|）。
     * 点击某档 → 高亮该档并经 {@code onPriority} 上抛 0 基序号（由调用方映射到实际优先级值，如 idx-2）。
     * 组件本体不渲染文字说明，tooltip 文案由调用方经 {@code StatusGlyph.tooltip(...)} 注入。
     */
    public SiteForm priorityBars(int levelCount, IntConsumer onPriority) {
        priorityMount.clearChildren();
        priorityButtons.clear();
        int n = Draw.clamp(levelCount, 3, 5);
        int center = (n - 1) / 2;          // C.3：中心档 = 优先级 0（中性）
        int magMax = Math.max(1, center);

        Node group = new Node().row().gap(2).id("site-form-priority-bars");
        for (int i = 0; i < n; i++) {
            final int index = i;
            // C.3 分歧色：i<center 红系（负优先级）/ i==center 中性（零）/ i>center 绿系（正优先级）；
            // 亮格数 = |i-center|（红2→红1→中性→绿1→绿2）。
            int off = i - center;
            StatusGlyph.Tint tint = off < 0 ? StatusGlyph.Tint.RED
                    : off > 0 ? StatusGlyph.Tint.GREEN : StatusGlyph.Tint.NEUTRAL;
            StatusGlyph glyph = StatusGlyph.bars(Math.abs(off), magMax, tint);
            Button b = Button.of(Component.empty()).styled();
            b.row();
            b.flex.alignItems = AlignItems.CENTER;
            b.flex.justifyContent = JustifyContent.CENTER;
            b.flex.width = glyph.flex.width + 6;
            b.flex.height = 12;
            b.add(glyph);
            b.onClick(() -> {
                for (int k = 0; k < priorityButtons.size(); k++) {
                    priorityButtons.get(k).active(k == index);
                }
                if (onPriority != null) onPriority.accept(index);
            });
            priorityButtons.add(b);
            group.add(b);
        }
        priorityMount.add(CoeUi.settingRow(Component.translatable("hold.coe.priority"), group));
        return this;
    }

    /**
     * 自动派遣总闸（注入）。开启时把工人步进置灰（Timberborn：自动接管 → 手动步进失效）；
     * 回调与 Screen 自己的 onChange 并存（同一 Property 多监听器），不覆盖 Screen 的发包逻辑。
     */
    public SiteForm autoDispatch(ToggleWidget toggle) {
        autoMount.clearChildren();
        if (toggle == null) return this;
        toggle.onChange(on -> { if (workerStepper != null) workerStepper.disabled(on); });
        autoMount.add(CoeUi.settingRow(Component.translatable("hold.coe.auto_assign"), toggle));
        return this;
    }

    /** 工人步进滑杆（注入）。 */
    public SiteForm workerStepper(LabeledSlider slider) {
        stepperMount.clearChildren();
        this.workerStepper = slider;
        if (slider == null) return this;
        stepperMount.add(CoeUi.settingRow(Component.translatable("hold.coe.max_workers"), slider));
        return this;
    }

    /** 已指派工人只读列表（注入）。 */
    public SiteForm workers(List<WorkerRow> rows) {
        workersMount.clearChildren();
        if (rows != null) {
            for (WorkerRow r : rows) workersMount.add(r);
        }
        return this;
    }

    /** 存储摘要文案（如「已绑定 x/y 容器」）。 */
    public SiteForm storageSummary(Component summary) {
        summaryMount.clearChildren();
        if (summary == null) return this;
        summaryMount.add(CoeUi.keyValue(Component.translatable("hold.coe.storages"), summary));
        return this;
    }

    /**
     * 显式保存按钮（注入）。组件不发包，发包逻辑在 Screen 的按钮回调里。
     * <p><b>四·七 B</b>：{@link SiteScreen} 已<b>不再注入</b>——低代价设置改即同步，本挂载点留空。
     * 方法保留给未来「高代价提交」类表单复用。
     */
    public SiteForm saveButton(Button b) {
        saveMount.clearChildren();
        if (b == null) return this;
        saveMount.add(b);
        return this;
    }
}
