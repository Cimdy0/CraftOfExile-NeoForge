package com.cimdy.coe.ui.widget.compose.business;

import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.anim.HoverLerp;
import com.cimdy.coe.ui.widget.attach.Sfx;
import com.cimdy.coe.ui.widget.attach.Vfx;
import com.cimdy.coe.ui.widget.component.dnd.DragRegistry;
import com.cimdy.coe.ui.widget.component.dnd.Draggable;
import com.cimdy.coe.ui.widget.component.dnd.NodeDropZone;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 按键设定条：HUD 技能栏（左排纵列）的同款模拟条，用于 {@code SkillScreen} 的按键设定。
 *
 * <p>装配：一列纵排 {@code count} 个 20px 槽位（HUD 槽位尺寸），每槽右侧显示按键标签
 * （文案由 Screen 通过 {@link #keyLabel} 注入，Screen 内部用 {@code InputState.getSkillKeyLabel} 取）。
 * 辅助键（{@code InputState.isAuxDown}）切换时，Screen 把第 6–9 槽作为第二套重新注入标签。
 *
 * <p>拖放基建复用 {@link NodeDropZone} + {@link DragRegistry}（计划五·拖放）：每个槽即一个 DropZone，
 * Screen 把技能图标（{@link ItemStack}）从左排行拖入槽位时，组件<b>只把「槽位索引 + 物品」上抛</b>
 * 给 {@link #dropHandler}；<b>不写 KeyBind、不发包</b>——真正的 {@code KeyBind} 写入与
 * {@code SkillSetupPacket} 由 Screen 的回调完成（纪律：组件不越权触网）。
 *
 * <p>动画（规格 §3）：
 * <ul>
 *   <li>拖拽悬停高亮 150ms（{@link HoverLerp}，槽框从 slotBorder 渐染到 accent）；</li>
 *   <li>绑定成功槽位 {@code pulse} 回弹；</li>
 *   <li>绑定成功挂 {@link Vfx#burst} 光点（Vfx.cap 默认 64，同屏活跃特效 &lt;10）。</li>
 * </ul>
 *
 * <p>潜在复用方：SkillScreen（唯一当前消费者）；未来任何「按键 / 施法槽位」设定界面。
 */
public class KeyBindBar extends AnimatedNode {

    /** HUD 技能槽尺寸（与 HudRenderer 左排 5×20px 槽一致）。 */
    private static final int SLOT = 20;

    public static KeyBindBar of() { return new KeyBindBar(); }

    /** 单个槽位运行态。 */
    private static final class Slot {
        final int index;
        final AnimatedNode cell;            // 20×20 槽
        final TextRendererI label;         // 按键标签
        final HoverLerp dropHover = new HoverLerp(150); // 拖拽悬停高亮 150ms
        NodeDropZone zone;

        Slot(int index, AnimatedNode cell, TextRendererI label) {
            this.index = index;
            this.cell = cell;
            this.label = label;
        }
    }

    private final List<Slot> slots = new ArrayList<>();
    private BiConsumer<Integer, ItemStack> dropHandler;
    /** 槽位点击回调（兜底：点槽弹技能列表）。默认 null = no-op，与 dropHandler 共存。 */
    private Consumer<Integer> slotClickHandler;
    private final Vfx vfx = new Vfx().cap(64);

    /** 懒注册：首次入树后才把槽位 zone 挂到根 DragRegistry（幂等）。 */
    private DragRegistry registeredReg;
    private boolean zonesDirty;

    private KeyBindBar() {
        id("keybind-bar");
        column().gap(2);
        padding(2);
        with(Theme.panel());
        attach(vfx);
        slots(5); // 默认 HUD 左排 5 槽
    }

    // ------------------------------------------------------------------
    // 冻结签名 §2
    // ------------------------------------------------------------------

    /** 设置槽位数量（默认 5；辅助键第二套由 Screen 切标签，不增减槽）。 */
    public KeyBindBar slots(int count) {
        unregisterZones();
        slots.clear();
        clearChildren();
        int n = Math.max(0, count);
        for (int i = 0; i < n; i++) slots.add(buildSlot(i));
        zonesDirty = true;
        return this;
    }

    /** 注入某槽位的按键标签（Screen 用 InputState.getSkillKeyLabel 取文案）。 */
    public KeyBindBar keyLabel(int slot, Component label) {
        if (slot >= 0 && slot < slots.size()) {
            slots.get(slot).label.setLine(0, label == null ? Component.empty() : label);
        }
        return this;
    }

    /** 拖放回调上抛：(槽位索引, 被拖入物品)。Screen 在回调里写 KeyBind 并发 SkillSetupPacket。 */
    public KeyBindBar dropHandler(BiConsumer<Integer, ItemStack> onDrop) {
        this.dropHandler = onDrop;
        return this;
    }

    /** 槽位左键点击回调上抛（兜底：点槽弹技能选择列表）。与 {@link #dropHandler} 共存，默认 no-op。 */
    public KeyBindBar onSlotClick(Consumer<Integer> onSlot) {
        this.slotClickHandler = onSlot;
        return this;
    }

    // ------------------------------------------------------------------
    // 装配
    // ------------------------------------------------------------------

    private Slot buildSlot(int i) {
        Node row = new Node().row().gap(4).alignItems(AlignItems.CENTER);
        row.height(SLOT);

        // 槽位格子：覆写 onMouseClicked 上抛左键点击（兜底弹列表）；有回调则吞掉事件防穿透。
        final int slotIdx = i;
        AnimatedNode cell = new AnimatedNode() {
            @Override protected boolean onMouseClicked(double lx, double ly, int button) {
                if (slotClickHandler != null && button == 0) {
                    slotClickHandler.accept(slotIdx);
                    return true;
                }
                return false;
            }
        };
        cell.size(SLOT, SLOT);
        cell.with(Theme.slot());

        TextRendererI label = Renderers.text(Component.translatable("ui.coe.keybind.empty_slot"))
                .align(Align.LEFT)
                .vAlign(VAlign.MIDDLE)
                .color(Theme.current().gemGold());
        Node labelNode = new Node();
        labelNode.grow(1);
        labelNode.with(label);

        row.add(cell);
        row.add(labelNode);
        add(row);

        Slot s = new Slot(i, cell, label);
        // 拖拽悬停高亮渲染器：读 HoverLerp 量，把槽框从 slotBorder 染到 accent
        cell.with(new Renderer() {
            @Override public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
                float a = s.dropHover.amount();
                if (a <= 0.02F) return;
                int c = Draw.blend(Theme.current().slotBorder(), Theme.current().accent(), a);
                Draw.outline(g, x, y, w, h, c);
            }
            @Override public Renderer copy() { return this; }
        });

        s.zone = new NodeDropZone(cell, src -> onZoneDrop(i, src));
        return s;
    }

    private void onZoneDrop(int i, Draggable src) {
        Object p = src.payload();
        ItemStack stack = (p instanceof ItemStack is) ? is : ItemStack.EMPTY;
        // 上抛（Screen 写 KeyBind + 发包）；组件不越权
        if (dropHandler != null) dropHandler.accept(i, stack);
        // 绑定成功反馈：Pulse + Vfx.burst + Sfx.DROP
        Slot s = slots.get(i);
        s.cell.pulse(0.1F, 300);
        vfx.burst(s.cell.width / 2.0F, s.cell.height / 2.0F, 6, Theme.current().accent());
        Sfx.emit(this, Sfx.Kind.DROP);
    }

    // ------------------------------------------------------------------
    // 拖拽悬停命中 + 懒注册
    // ------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (zonesDirty) ensureRegistered();
    }

    @Override
    public void render(GuiGraphicsExtractor g, RenderContext ctx, boolean parentDisabled) {
        DragRegistry reg = registeredReg != null ? registeredReg : findDragRegistry();
        DragRegistry.ActiveDrag active = reg != null ? reg.activeDrag() : null;
        for (Slot s : slots) {
            boolean over = active != null && s.cell.contains(active.x, active.y);
            s.dropHover.update(over); // 150ms 过渡
        }
        super.render(g, ctx, parentDisabled);
    }

    /** 首次入树后把槽位 zone 挂到根 DragRegistry；幂等，槽位重建时先反注册旧 zone。 */
    private void ensureRegistered() {
        DragRegistry reg = (root() instanceof com.cimdy.coe.ui.widget.component.panel.WindowLayer wl)
                ? wl.dragRegistry() : findDragRegistry();
        if (reg == null) return; // 尚未入树，下帧再试
        registeredReg = reg;
        for (Slot s : slots) {
            if (s.zone != null) reg.register(s.zone, s.cell.findDragScopeNode());
        }
        zonesDirty = false;
    }

    private void unregisterZones() {
        if (registeredReg != null) {
            for (Slot s : slots) {
                if (s.zone != null) registeredReg.unregister(s.zone);
            }
        }
    }
}
