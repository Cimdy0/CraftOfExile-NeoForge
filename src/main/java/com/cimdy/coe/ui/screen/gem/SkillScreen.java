package com.cimdy.coe.ui.screen.gem;
import com.cimdy.coe.ui.widget.render.Renderers;

import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.helper.skill.EquipmentSkillHelper;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.gem.GemCategory;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.gem.SupportData;
import com.cimdy.coe.data.codec.skill.gem.KeyBind;
import com.cimdy.coe.data.codec.skill.gem.SkillAssembly;
import com.cimdy.coe.data.codec.support.meta.Labels;
import com.cimdy.coe.data.codec.skill.support.SupportGem;
import com.cimdy.coe.network.skill.SkillAssemblyActionPacket;
import com.cimdy.coe.network.skill.SkillSetupPacket;
import com.cimdy.coe.ui.menu.SkillAssemblyMenu;
import com.cimdy.coe.ui.screen.AbstractPanelScreen;
import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.attach.AttachmentPresets;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.base.MouseButton;
import com.cimdy.coe.ui.widget.component.base.Scrollable;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.compose.business.KeyBindBar;
import com.cimdy.coe.ui.widget.compose.composite.MasterDetail;
import com.cimdy.coe.ui.widget.compose.picker.SearchableList;
import com.cimdy.coe.ui.widget.compose.Slot;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.BorderStyle;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.screen.WidgetHost;
import com.cimdy.coe.ui.widget.component.dnd.Draggable;
import com.cimdy.coe.ui.widget.component.dnd.DropZone;
import com.cimdy.coe.ui.widget.component.dnd.NodeDropZone;
import java.util.function.BiConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;

/**
 * 技能装配界面：左侧纵向列出所有技能槽位，右侧内嵌背包（原版槽位交互）。
 *
 * <p>每行 = 技能信息（图标/名称/等级/光环标记/按键标签）+ 主动技能石槽 + 辅助技能石槽。
 * 槽位是独立按钮：点击判定按「当前鼠标拖的物品是什么」分发到服务端
 * （装备/卸下/切换光环/装辅助/卸辅助），不再有「整行点击后手动算命中哪个槽」的坐标数学。
 *
 * <p>本类保留 {@link AbstractPanelScreen} 基类（背包由原版容器机制处理），
 * UI 部分通过 {@link WidgetHost} 组合接入，两者互不干扰。
 */
public class SkillScreen extends AbstractPanelScreen<SkillAssemblyMenu> {

    /** 装备只读渲染段的高度上限（px）；超出部分由内部滚动消化，不与手动列表争高度。 */
    private static final int MAX_EQUIPMENT_SECTION_H = 120;

    private final WidgetHost ui = new WidgetHost();

    private SkillAssembly assembly;
    private int assemblySize;
    private MasterDetail<SkillData> master;
    /** 只读渲染段容器：装备赋予的技能（含辅助宝石），置于手动网格下方，不可拖拽 / 不发射动作包。 */
    private Scrollable equipmentSection;
    /** 只读段高度上限：由 init 按窗口高度推算，保证手动列表（master, grow=1）至少占 2/3。 */
    private int equipmentSectionMaxH;

    /** HUD 同款按键模拟条（5 槽，辅助键 R 切第二套 6-10）。拖技能图标 → 写 KeyBind 并发包。 */
    private KeyBindBar keyBar;
    /** 拖拽 payload（sd.toItemStack() 同一实例）→ 装配槽索引；IdentityHashMap 按实例匹配，dropHandler 只给 ItemStack。 */
    private final IdentityHashMap<ItemStack, Integer> bindSources = new IdentityHashMap<>();
    /** 上一帧辅助键状态（R），变化时重注入槽位标签。 */
    private boolean lastAux = false;
    /** 点槽弹出的技能绑定选择弹层（兜底）；同一时刻仅一个，点技能 / Esc / X 关闭。 */
    private Window bindPopup;

    // ---- 拖放（模型级搬迁）----
    /** 每次重建行时登记的槽位放置区，重建前统一注销避免重复命中 / 泄漏。 */
    private final List<DropZone> slotZones = new ArrayList<>();
    /** 内嵌背包面板放置区（拖出卸下）；全屏仅一个，init 时登记一次。 */
    private DropZone inventoryDropZone;
    /** 统一落点处理：读源 payload + 目标 zone，发对应服务端动作。 */
    private final BiConsumer<Draggable, DropZone> onAssemblyDrop = (src, zone) -> {
        if (!(src.payload() instanceof DragTarget from)) return;
        DragTarget to = (zone instanceof SocketDropZone sd) ? sd.target : DragTarget.INVENTORY;
        applyDrop(from, to);
    };

    public SkillScreen(SkillAssemblyMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    // ------------------------------------------------------------------
    // 构建
    // ------------------------------------------------------------------

    @Override
    protected void init() {
        super.init();
        this.assembly = this.menu.getAssembly();
        // 可用槽数 = clamp(floor(skill_slot_count), 0, SIZE)。base 9；**stat 通道完整**
        // （StatGen/StatEffectGen 的 ADD_VALUE/GAIN 作用于 SKILL_SLOT_COUNT，读点经 StatEngine.fold），
        // 但 POE2 语料无「+# 技能槽」生产者，按铁律不自创内容，故实际恒为 base 9；
        // 属性上限 [0,99] 不构成约束，由本行 clamp 到 SkillAssembly.SIZE 收口。
        this.assemblySize = Mth.clamp(
                (int) this.player.getAttributeValue(AttributeRegistry.SKILL_SLOT_COUNT),
                0, SkillAssembly.SIZE);

        int invW = 180, gap = 8, listW = 132, keyBarW = 30;
        int asmH = Math.max(120, Math.min(248, this.height - 60));
        // 装备只读段最多占窗口 1/3（且不超过 MAX_EQUIPMENT_SECTION_H），保证手动列表至少保留 2/3；
        // 无装备技能时该段高度归 0 并隐藏（见 renderEquipmentRows），不占用手动列表任何空间。
        this.equipmentSectionMaxH = Math.min(MAX_EQUIPMENT_SECTION_H, asmH / 3);
        // 装配窗 = 左侧按键模拟条 + 槽位列表 + 右侧描述面板；屏幕窄时先压缩描述面板，整体不越界
        int asmW = Math.min(keyBarW + gap + listW + gap + 186,
                Math.max(keyBarW + gap + listW + gap + 80, this.width - invW - gap * 3));
        int detailW = asmW - keyBarW - gap - listW - gap;
        int winX = (this.width - (asmW + gap + invW)) / 2;
        int winY = (this.height - asmH) / 2;

        // 装配窗口：只覆盖装配区，不覆盖背包 → 背包点击归原版容器处理
        Window window = Window.of(Component.translatable("stat.skill.title"))
                .closable(true).glow()
                // 点「x」必须真正关掉整个界面（含内嵌背包），否则只剩空白壳看起来像卡死
                .onClose(this::onClose);
        window.flex.size(asmW, asmH);
        window.background(Renderers.color(Theme.current().gemPanelBg(), Theme.current().gemGold())
                .borderStyle(BorderStyle.FULL)
                .cornerDots(Theme.current().gemGold()));
        window.position(winX, winY);
        // D.5 默认态：本屏窗走自定义 gem 背景（未走 styled()），补挂 D.2 背景纹理覆盖层 + 标准音效族。
        // 装饰已由自定义 gemGold 边框/角点承担，不再挂 decorFor（避免双渲染）；
        // 特效按 D.5 默认关（KeyBindBar 内既有 vfx.burst 已接线，受全局 MASTER_ENABLED=false 约束）。
        window.with(Theme.backgroundTexture());
        window.attach(AttachmentPresets.sfxFor(AttachmentPresets.defaults(), AttachmentPresets.Target.SKILL));

        // 主从布局：左侧槽位列表选中 / 点击 → 右侧 PoE2 风格描述面板
        master = new MasterDetail<>(rowTemplate(), this::bindRow);
        master.flex.grow = 1;
        master.flex.gapMain = gap;
        master.listGap(2).listPadding(2).selectable(true);
        master.emptyView(CoeUi.emptyHint());
        master.list().flex.grow = 0;
        master.list().flex.width = listW;
        master.detailPanel().flex.grow = 0;
        master.detailPanel().flex.width = detailW;
        master.onSelect((sd, i) -> master.detailPanel().show(describe(sd, i)));

        // HUD 同款按键模拟条（左侧纵列 5 槽；辅助键 R 切第二套 6-10）：拖技能图标到槽位即绑定。
        keyBar = KeyBindBar.of()
                .slots(5)
                .dropHandler(this::onBindDrop)
                .onSlotClick(this::onSlotClicked);
        keyBar.flex.width = keyBarW;
        keyBar.flex.grow = 0;
        refreshKeyLabels();

        Node leftCol = new Node().column();
        leftCol.flex.gapMain = 4;
        leftCol.flex.width = keyBarW;
        leftCol.add(keyBar);
        // 背包开关按钮（基类已有 B 键 + toggleInventoryPanel；这里给显式按钮，关闭后装配窗可显得更宽）
        Button invToggle = CoeUi.button(Component.translatable("ui.coe.skill.inv_toggle"))
                .onClick(this::toggleInventoryPanel);
        invToggle.flex.grow = 0;
        leftCol.add(invToggle);
        // 武器套装切换（套 I/II）：UI 骨架 —— 连线待技能执行侧（使用技能时自动切快捷栏热选定槽再触发使用）。
        // WeaponSetPacket 仅用于天赋树激活组切换，与技能绑定解耦；此处仅置灰占位，不挂 onClick、不发任何包。
        Button weaponSetBtn = CoeUi.button(Component.translatable("ui.coe.skill.weapon_set"));
        weaponSetBtn.flex.grow = 0;
        if (weaponSetBtn.labelRenderer() != null) weaponSetBtn.labelRenderer().color(Theme.current().textDisabled());
        leftCol.add(weaponSetBtn);

        Node body = new Node().row();
        body.flex.grow = 1;
        body.flex.gapMain = gap;
        body.flex.alignItems = AlignItems.STRETCH;
        body.add(leftCol);
        body.add(master);
        window.add(body);

        // 只读渲染段：装备赋予的技能（含辅助宝石），置于手动网格下方。
        // 用 Scrollable 而非裸 Node：Scrollable.toFlex() 对父级伪装成叶子，内容再高也不参与
        // 父容器分配，配合下方固定的 flex.height（上限 equipmentSectionMaxH）即可做到
        // 「不挤压 master 且内容不被压扁」，溢出部分在内部滚动。
        equipmentSection = new Scrollable().scrollBar(true);
        equipmentSection.itemGap(2);
        equipmentSection.contentPadding(2);
        equipmentSection.flex.grow = 0;     // 明确不参与 master 对剩余高度的争夺
        equipmentSection.flex.height = 0;   // 初始 0：无装备技能时完全不占位
        equipmentSection.visible = false;
        window.add(equipmentSection);
        renderEquipmentRows();

        ui.clear();
        ui.layer().add(window);
        ui.resize(this.width, this.height);

        refreshRows();
        registerInventoryDropZone();
        // 首次打开即同步按键绑定（syncKeyBinds 仅随装配「变化」触发，防止存档/重载后绑定缺失）
        syncKeyBinds();

        // 内嵌背包面板：装配窗口右侧（基类统一面板渲染 + 原版槽位交互）
        enableInventoryPanel(winX + asmW + gap, winY + 22);
    }

    /** 行模板：信息行 + 槽位行。结构固定，内容由 binder 每行重建。 */
    private static Node rowTemplate() {
        Node row = new Node().column();
        row.flex.height = 56;
        row.padding(0, 4);

        Node info = new Node().row();
        info.id = "asm-info";
        info.flex.height = 24;
        info.flex.alignItems = com.cimdy.coe.ui.widget.flex.enums.AlignItems.CENTER;
        info.gap(4);

        Node slots = new Node().row();
        slots.id = "asm-slots";
        slots.flex.height = 30;
        slots.flex.alignItems = com.cimdy.coe.ui.widget.flex.enums.AlignItems.CENTER;
        slots.gap(2);

        row.add(info);
        row.add(slots);
        return row;
    }

    private void bindRow(Node view, SkillData sd, int index) {
        Node info = view.children.get(0);
        Node slots = view.children.get(1);
        boolean empty = sd.isEmpty();
        // 不满足施法条件（精魂不足 / 属性需求不足 / 等级不足 / 武器不符）：图标灰化 + 红色感叹号
        boolean usable = empty || isUsable(sd, index);
        // 装备授予技能只读：不可拖到按键栏（提前判定，供图标拖拽分支使用）
        boolean equipment = !empty && isEquipmentOrigin(index);

        // ---- 信息行 ----
        info.clearChildren();

        Draggable icon = new Draggable();
        icon.flex.size(16, 16);
        icon.with(new GemIconRender(empty ? GemIconRender.EMPTY : sd.getIcon(), 14, 14));
        if (!usable) icon.with(new UnusableMark(14));
        // 拖到左侧 KeyBindBar 槽位 → 写 KeyBind + 发 SkillSetupPacket。payload 必须是 ItemStack
        // （KeyBindBar.onZoneDrop 只从 payload 取 ItemStack），故用 sd.toItemStack() 同一实例登记 bindSources。
        if (!empty && !equipment) {
            ItemStack st = sd.toItemStack();
            icon.payload(st);
            icon.dragRegistry(ui.layer().dragRegistry());
            icon.dragLabel(sd.getName());
            bindSources.put(st, index);
        }

        Node name = new Node().with(Renderers.text(empty ? Component.empty() : sd.getName().copy())
                .align(Align.LEFT)
                .vAlign(VAlign.MIDDLE));
        name.flex.grow = 1;

        Node level = new Node().with(Renderers.text(empty ? Component.empty()
                        : Component.literal("Lv" + sd.getEffectiveSkillLevel()))
                .align(Align.LEFT)
                .vAlign(VAlign.MIDDLE)
                .color(Theme.current().gemTextMuted()));

        Node aura = new Node();
        if (!empty && sd.isSpirit() && assembly.isAuraEnabled(index)) {
            aura.with(Renderers.text(Component.translatable("stat.skill.aura_on"))
                    .align(Align.LEFT)
                    .vAlign(VAlign.MIDDLE)
                    .color(Theme.current().gemTextPositive()));
        }

        // 行内角标：取实际绑定到本装配槽的按键标签（SKILL_SAVE 反向查找），而非装配槽 index（A.2 #2）。
        Node key = new Node().with(Renderers.text(skillKeyLabelFor(index))
                .align(Align.RIGHT)
                .vAlign(VAlign.MIDDLE)
                .color(Theme.current().gemGold()));

        info.add(icon);
        info.add(name);
        info.add(level);
        info.add(aura);
        info.add(key);

        // ---- 槽位行 ----
        slots.clearChildren();

        // 主技能槽：外层 Draggable（左键起拖 / 点击）+ 内层 Slot（渲染 + 右键卸下 / 开关）。
        // 内层 Slot.only(RIGHT) 把左键让给外层 Draggable，避免二者抢同一个按下事件。
        Node mainSlot = socketCell(18, empty ? ItemStack.EMPTY : sd.toItemStack(),
                new DragTarget(DragKind.MAIN, index, -1), sd, !usable, equipment);
        slots.add(mainSlot);

        if (!empty) {
            List<SupportData> supports = sd.supports();
            // 辅助空槽数：数据驱动（sd.getSockets()）并钳到 [3, MAX_SOCKETS=5]（C.6）。
            int supportSlots = supportSlotCount(sd);
            for (int j = 0; j < supportSlots; j++) {
                SupportData spd = j < supports.size() ? supports.get(j) : null;
                ItemStack item = spd != null && !spd.isEmpty() ? supportItem(spd) : ItemStack.EMPTY;
                slots.add(socketCell(18, item, new DragTarget(DragKind.SUPPORT, index, j), sd, false, equipment));
            }
        }
    }

    /** 每行辅助空槽数：数据驱动（{@link SkillData#getSockets()}）并钳到 [3, {@link SkillData#MAX_SOCKETS}=5]。
     *  POE2 每主动 1-5 辅助 socket；UI 至少补 3 个固定空槽占位。超出数据 sockets 的格为视觉占位，
     *  拖拽投递由服务端 {@code SkillAssemblyMenu} 按 socket 范围越界防御，越界即 no-op（A.2 #3）。 */
    private static int supportSlotCount(SkillData sd) {
        return Math.clamp(Math.max(3, sd.getSockets()), 0, SkillData.MAX_SOCKETS);
    }

    /**
     * 技能槽单元格：外层 {@link Draggable}（左键起拖 / 左键点击）+ 内层 {@link Slot}（渲染 + 右键卸下 / 开关）。
     * 内层 Slot 用 {@code only(RIGHT)} 把左键让给外层 Draggable，避免二者抢同一个按下事件；
     * 同时把内层 Slot 包成放置区（autoScope 继承窗口作用域），落点统一由 {@link #onAssemblyDrop} 处理。
     */
    /** 该装配槽在手动装配表里是否为空（被装备授予技能占位）——装备技能只读、不可拖拽/卸下。 */
    private boolean isEquipmentOrigin(int index) {
        var manual = this.minecraft.player.getData(AttachmentRegistry.SKILL_ASSEMBLY);
        List<SkillData> slots = manual.slots();
        return index >= 0 && index < slots.size() && slots.get(index).isEmpty();
    }

    private Node socketCell(int size, ItemStack stack, DragTarget payload, SkillData sd, boolean unusable, boolean equipment) {
        final int index = payload.slot;
        final int socket = payload.socket;

        Slot slot = new Slot().size(size).styledSlot();
        if (stack != null && !stack.isEmpty()) slot.item(stack);

        // 装备授予技能：来源是 EQUIPMENT_SKILLS 附件而非手动装配槽，按设计只读——
        // 不挂 Draggable / DropZone / 右键卸下，避免把派生技能误当可搬运单元（拖到背包是服务端 no-op、视觉上「弹回」）。
        if (equipment) {
            return slot;
        }

        slot.only(MouseButton.RIGHT);                 // 左键交给外层 Draggable；右键在此卸下 / 开关
        slot.onClick((src, btn) -> {
            if (payload.kind == DragKind.MAIN) onMainSlotClick(index, sd, 1);
            else onSupportSlotClick(index, socket, 1);
        });
        if (unusable) slot.with(new UnusableMark(size));

        Draggable drag = new Draggable();
        drag.size(size, size);
        drag.add(slot);
        drag.payload(payload);
        drag.dragLabel(stack.isEmpty() ? Component.empty() : stack.getHoverName());
        drag.onClick(() -> {                          // 左键（未形成拖拽）= 点击：装备 / 卸下
            if (payload.kind == DragKind.MAIN) onMainSlotClick(index, sd, 0);
            else onSupportSlotClick(index, socket, 0);
        });
        drag.onDropInto(onAssemblyDrop);              // 拖拽落点 = 模型级搬迁

        // 放置区：包住内层 Slot，登记到窗口作用域（与拖拽源同作用域 → 命中优先级最高）。
        // 注意：register(zone) 会把 zone 当全局（忽略 autoScope），必须显式传作用域节点。
        SocketDropZone zone = new SocketDropZone(slot, payload);
        slotZones.add(zone);
        ui.layer().dragRegistry().register(zone, slot.findDragScopeNode());
        return drag;
    }

    /** 内嵌背包面板作为「拖出卸下」放置区：覆盖面板矩形，落点映射到 UNEQUIP / REMOVE_SUPPORT。 */
    private void registerInventoryDropZone() {
        if (inventoryDropZone != null) return;        // 仅登记一次（dragRegistry 跨 init 持久）
        inventoryDropZone = new DropZone() {
            @Override
            public boolean acceptDrop(double mx, double my) {
                return isInventoryPanelVisible()
                        && mx >= leftPos && mx < leftPos + INVENTORY_WIDTH
                        && my >= topPos && my < topPos + INVENTORY_HEIGHT;
            }
            @Override
            public void onDrop(Draggable source) { /* 真正搬数据在 onDropInto（onAssemblyDrop） */ }
            @Override
            public Node scope() { return null; }       // 全局 zone：仅当光标在面板区且无更优命中时兜底
        };
        ui.layer().dragRegistry().register(inventoryDropZone);
    }

    private static ItemStack supportItem(SupportData spd) {
        ItemStack item = new ItemStack(GemCategory.SUPPORT.resultItem());
        item.set(DataComponents.ITEM_MODEL, spd.id());
        item.set(DataComponents.ITEM_NAME, SupportGem.getDisplayName(spd.id()));
        item.set(DataComponentRegistry.SUPPORT_DATA, spd);
        return item;
    }

    // ------------------------------------------------------------------
    // 交互
    // ------------------------------------------------------------------

    /**
     * 该槽位技能当前是否满足施放条件（客户端估算，与 HUD 的红色遮罩同源：
     * {@code skill.canCast} 覆盖精魂不足 / 属性需求不足 / 等级不足 / 武器不符）。
     * 取不到技能数据时按可用处理，避免误标红。
     */
    private boolean isUsable(SkillData sd, int index) {
        Skill skill = sd.getSkill();
        return skill == null || skill.canCast(context(sd, index));
    }

    /** 求值上下文：按键槽与装配槽一一对应（见 {@link KeyBind#autoBind}）。 */
    private SkillContext context(SkillData sd, int index) {
        SkillContext context = new SkillContext(this.player, index);
        context.skillLevel(sd.getEffectiveSkillLevel());
        context.quality(sd.getEffectiveQuality());
        return context;
    }

    private void onMainSlotClick(int index, SkillData sd, int button) {
        // 槽位按钮会吃掉点击，行选中不会自动发生；这里补一次，点宝石即看描述
        master.list().select(index);
        ItemStack carried = this.menu.getCarried();
        Optional<GemCategory> carriedCat = GemCategory.categorize(carried);
        if (carriedCat.isPresent() && carriedCat.get() != GemCategory.SUPPORT) {
            sendAction(SkillAssemblyActionPacket.EQUIP_SKILL, index, -1, -1, -1);
        } else if (!sd.isEmpty() && sd.isSpirit() && button == 1) {
            sendAction(SkillAssemblyActionPacket.TOGGLE_AURA, index, -1, -1, -1);
        } else if (!sd.isEmpty()) {
            sendAction(SkillAssemblyActionPacket.UNEQUIP_SKILL, index, -1, -1, -1);
        }
    }

    private void onSupportSlotClick(int index, int socket, int button) {
        SkillData sd = assembly.get(index);
        if (sd.isEmpty() || socket >= sd.getSockets()) return;
        ItemStack carried = this.menu.getCarried();
        Optional<GemCategory> carriedCat = GemCategory.categorize(carried);
        if (button == 1) {
            sendAction(SkillAssemblyActionPacket.REMOVE_SUPPORT, index, socket, -1, -1);
        } else if (carriedCat.isPresent() && carriedCat.get() == GemCategory.SUPPORT) {
            sendAction(SkillAssemblyActionPacket.EQUIP_SUPPORT, index, socket, -1, -1);
        }
    }

    private void sendAction(int action, int slot, int socket, int fromSlot, int fromSocket) {
        ClientPacketDistributor.sendToServer(
                new SkillAssemblyActionPacket(action, slot, socket, fromSlot, fromSocket));
    }

    /**
     * 拖放落点 → 模型级搬迁：读源 payload + 目标 zone，发对应服务端动作。
     * 卸下 / 重排都走服务端权威（SkillAssemblyMenu.handleAssemblyAction），
     * 结果经附件同步回读后由 {@link #refreshRows()} 重建节点。
     */
    private void applyDrop(DragTarget from, DragTarget to) {
        if (to.kind == DragKind.INVENTORY) {
            // 拖出卸下：源是主技能 → 卸下主技能；源是辅助 → 卸下该辅助
            if (from.kind == DragKind.MAIN) sendAction(SkillAssemblyActionPacket.UNEQUIP_SKILL, from.slot, -1, -1, -1);
            else if (from.kind == DragKind.SUPPORT) sendAction(SkillAssemblyActionPacket.REMOVE_SUPPORT, from.slot, from.socket, -1, -1);
            return;
        }
        // 槽位间重排 / 交换
        if (from.kind == DragKind.MAIN && to.kind == DragKind.MAIN) {
            if (from.slot != to.slot) sendAction(SkillAssemblyActionPacket.MOVE_SKILL, to.slot, -1, from.slot, -1);
        } else if (from.kind == DragKind.SUPPORT && to.kind == DragKind.SUPPORT) {
            if (from.slot != to.slot || from.socket != to.socket)
                sendAction(SkillAssemblyActionPacket.MOVE_SUPPORT, to.slot, to.socket, from.slot, from.socket);
        }
        // 主技能 ↔ 辅助槽 互拖：类型不兼容，忽略
    }

    /** 装配数据变化后，由 Screen 负责把按键绑定发往服务端。绑定基于「手动装配 + 装备授予」合并视图，
     *  使装备授予的技能也被自动绑定到空闲按键。
     *  <p>修复（A.2 #1）：<b>仅对空闲按键按装配槽顺序补绑，保留玩家手动拖放 / 点槽设定</b>
     *  （此前全量 {@link KeyBind#autoBind} 会覆盖手动绑定）。 */
    private void syncKeyBinds() {
        KeyBind.SkillSave next = this.player.getData(AttachmentRegistry.SKILL_SAVE);
        var slots = EquipmentSkillHelper.mergedAssembly(this.minecraft.player).slots();
        int asmSlot = 0;
        int size = next.save().size();
        for (int i = 0; i < size; i++) {
            if (!next.save().get(i).isEmpty()) continue; // 已有绑定（手动 / 先前设定）保留
            while (asmSlot < slots.size() && slots.get(asmSlot).isEmpty()) asmSlot++;
            if (asmSlot >= slots.size()) break;
            next = next.set(i, asmSlot);
            asmSlot++;
        }
        ClientPacketDistributor.sendToServer(new SkillSetupPacket(next));
    }

    /**
     * KeyBindBar 拖放落点：把技能（payload=sd.toItemStack() 对应装配槽）绑到指定按键。
     * 辅助键按下时 bar 槽位映射到 6-10（keyIndex 5-9）。
     */
    private void onBindDrop(int barSlot, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        Integer asmSlot = bindSources.get(stack);
        if (asmSlot == null) return;
        int keyIndex = (com.cimdy.coe.client.InputState.isAuxDown() ? 5 : 0) + barSlot;
        applyBind(keyIndex, asmSlot);
    }

    /**
     * 兜底：点槽 → 弹层选技能绑定。弹层列出当前装配里所有非空槽，选中即绑到该 keyIndex。
     */
    private void onSlotClicked(int barSlot) {
        int keyIndex = (com.cimdy.coe.client.InputState.isAuxDown() ? 5 : 0) + barSlot;
        openBindPopup(keyIndex);
    }

    /** 写 KeyBind.SkillSave.set(keyIndex, asmSlot) + 发 SkillSetupPacket 全量表（拖放 / 点槽共用）。 */
    private void applyBind(int keyIndex, int asmSlot) {
        KeyBind.SkillSave current = this.player.getData(AttachmentRegistry.SKILL_SAVE);
        ClientPacketDistributor.sendToServer(new SkillSetupPacket(current.set(keyIndex, asmSlot)));
    }

    /** 技能绑定选择弹层：列出装配非空槽（按索引），选中即绑 + 关弹层。 */
    private void openBindPopup(int keyIndex) {
        closeBindPopup();
        List<Integer> options = new ArrayList<>();
        for (int i = 0; i < assemblySize; i++) {
            if (!assembly.get(i).isEmpty()) options.add(i);
        }
        if (options.isEmpty()) return;

        Window w = CoeUi.panel(Component.translatable("ui.coe.skill.bind_picker.title"), 160, 180)
                .closable(true)
                .center(this.width, this.height);
        SearchableList<Integer> list = new SearchableList<Integer>(CoeUi.listRow(), (view, asm, idx) -> {
            if (view instanceof Button row) row.label(assembly.get(asm).getName().copy());
        }).textOf(asm -> assembly.get(asm).getName().getString())
                .emptyView(CoeUi.emptyHint())
                .scrollBar(true)
                .clearBeforeBind();
        list.flex.grow = 1;
        list.list().items(options);
        list.onSelect((asm, idx) -> {
            applyBind(keyIndex, asm);
            closeBindPopup();
        });
        w.add(list);
        ui.layer().add(w);
        bindPopup = w;
    }

    /** 关闭绑定选择弹层（若开着）。 */
    private void closeBindPopup() {
        if (bindPopup != null) {
            ui.layer().remove(bindPopup);
            bindPopup = null;
        }
    }

    /** 按辅助键状态重注入 5 个槽位的按键标签（1-5 或 A+1..A+5）。 */
    private void refreshKeyLabels() {
        if (keyBar == null) return;
        boolean aux = com.cimdy.coe.client.InputState.isAuxDown();
        for (int i = 0; i < 5; i++) {
            keyBar.keyLabel(i, com.cimdy.coe.client.InputState.getSkillKeyLabel(aux ? 5 + i : i));
        }
    }

    /** 行内角标：取「实际绑定到本装配槽」的按键标签（SKILL_SAVE 反向查找 keyIndex），而非装配槽 index。
     *  修复（A.2 #2）：autoBind 时 key i→assembly i 巧合正确，手动拖放绑定后行内标签须随实际映射走。
     *  {@link com.cimdy.coe.client.InputState#getSkillKeyLabel} 只读调用；未绑定（如精魂光环）返回空。 */
    private Component skillKeyLabelFor(int asmSlot) {
        KeyBind.SkillSave save = this.player.getData(AttachmentRegistry.SKILL_SAVE);
        if (save == null) return Component.empty();
        for (int k = 0; k < save.save().size(); k++) {
            KeyBind.EntitySkill es = save.save().get(k);
            if (!es.isEmpty() && es.assemblySlot().getInt() == asmSlot) {
                return com.cimdy.coe.client.InputState.getSkillKeyLabel(k);
            }
        }
        return Component.empty();
    }

    // ------------------------------------------------------------------
    // 描述面板
    // ------------------------------------------------------------------

    /**
     * 描述面板内容：名称 → 等级 → 分类标签 → 需求/消耗 → 描述 → 逐阶段效果行。
     * 与宝石 tooltip 同源（{@link Skill#addDataComponent} / {@link Skill#addPhaseDataComponent}）。
     */
    private List<Component> describe(SkillData sd, int index) {
        if (sd.isEmpty()) return List.of();

        List<Component> lines = new ArrayList<>();
        lines.add(sd.getName().copy().withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("stat.level")
                .append(": ").withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.literal(String.valueOf(sd.getEffectiveSkillLevel()))
                        .withStyle(ChatFormatting.WHITE)));

        Skill skill = sd.getSkill();
        if (skill == null) return lines;
        SkillContext context = context(sd, index);

        // 分类：攻击/法术 + 技能类型（warcry / curse / herald …）
        MutableComponent labels = Component.empty();
        skill.getLabels(context).forEach(label ->
                labels.append("'").append(Labels.getNameComponent(label)).append("'"));
        lines.add(labels.withStyle(ChatFormatting.WHITE));

        // 需求 / 消耗 / 施放：无消耗时 addDataComponent 会塞入 null，过滤掉
        List<Component> data = new ArrayList<>();
        skill.addDataComponent(data, context);
        for (Component c : data) if (c != null) lines.add(c);

        lines.add(Component.empty());
        lines.add(Component.translatable(
                (sd.spirit() ? "gem.spirit.description." : "gem.skill.description.") + sd.id().getNamespace() + "." + sd.getNameString()));

        // 逐阶段效果行
        lines.add(Component.empty());
        skill.addPhaseDataComponent(lines, context);
        return lines;
    }

    // ------------------------------------------------------------------
    // 数据
    // ------------------------------------------------------------------

    private void refreshRows() {
        // 旧行的槽位放置区先注销，避免跨次累积重复命中
        for (DropZone z : slotZones) ui.layer().dragRegistry().unregister(z);
        slotZones.clear();

        List<SkillData> rows = new ArrayList<>();
        for (int i = 0; i < assemblySize; i++) rows.add(assembly.get(i));
        master.items(rows);
        // 重建后选中行若已不存在，清掉残留的描述
        if (master.list().selectedIndex() < 0) master.detailPanel().clear();
        // 装备赋予的技能（只读段）随装配表一并刷新
        renderEquipmentRows();
    }

    /**
     * 只读渲染段：把装备赋予的技能（含辅助宝石）画在手动网格下方。
     * 与手动行共用同一套图标渲染路径（{@link GemIconRender} / {@link Slot#item} / {@link #supportItem}），
     * 但<b>不挂载 Draggable / DropZone / 右键逻辑、不发射 {@link SkillAssemblyActionPacket}</b>，
     * 仅作展示（装备技能已通过 {@link EquipmentSkillHelper#mergedAssembly} 进入可施放通道）。
     *
     * <p><b>高度策略</b>：容器是 {@link Scrollable}，固定 {@code flex.height = equipmentSectionMaxH}（≤ 窗口 1/3）
     * 且 {@code grow = 0}，因此对父布局而言是一个定高叶子 —— 既不会反过来压缩 master，自身内容也不会被
     * flex shrink 压扁（内容以「视口宽 × 无限高」独立排版，行高恒定 30px），超出部分由内置滚动条消化。
     * 无装备技能时高度归 0 并隐藏，手动列表恢复满高。
     */
    private void renderEquipmentRows() {
        if (equipmentSection == null) return;
        equipmentSection.clearChildren();
        List<SkillData> eq = EquipmentSkillHelper.grantedSkills(this.player).stream()
                .filter(sd -> !sd.isEmpty()).toList();
        if (eq.isEmpty()) {
            equipmentSection.flex.height = 0;
            equipmentSection.visible = false;
            return;
        }
        equipmentSection.visible = true;
        equipmentSection.flex.height = equipmentSectionMaxH;
        Node header = new Node().with(Renderers.text(Component.literal("装备技能"))
                .align(Align.LEFT).vAlign(VAlign.MIDDLE).color(Theme.current().gemGold()));
        header.flex.height = 16;
        equipmentSection.add(header);
        for (SkillData sd : eq) equipmentSection.add(buildEquipmentRow(sd));
        // 内容刚整体替换，立即重排一次（不等下一帧），滚动范围与滚动条同步生效
        equipmentSection.layoutContent();
    }

    /** 单行只读渲染：图标 + 名称 + 等级 + 辅助技能格（纯展示）。 */
    private Node buildEquipmentRow(SkillData sd) {
        Node row = new Node().row();
        row.flex.height = 30;
        row.flex.alignItems = AlignItems.CENTER;
        row.gap(4);

        Node icon = new Node();
        icon.flex.size(16, 16);
        icon.with(new GemIconRender(sd.getIcon(), 14, 14));
        row.add(icon);

        Node name = new Node().with(Renderers.text(sd.getName().copy())
                .align(Align.LEFT).vAlign(VAlign.MIDDLE));
        name.flex.grow = 1;
        row.add(name);

        Node level = new Node().with(Renderers.text(Component.literal("Lv" + sd.getEffectiveSkillLevel()))
                .align(Align.LEFT).vAlign(VAlign.MIDDLE).color(Theme.current().gemTextMuted()));
        row.add(level);

        // 辅助技能格：仅渲染 Slot + 物品，不挂 Draggable / DropZone / 点击事件
        List<SupportData> supports = sd.supports();
        int sockets = sd.getSockets();
        for (int j = 0; j < sockets; j++) {
            SupportData spd = j < supports.size() ? supports.get(j) : null;
            Slot cell = new Slot().size(18).styledSlot();
            if (spd != null && !spd.isEmpty()) cell.item(supportItem(spd));
            row.add(cell);
        }
        return row;
    }

    // ------------------------------------------------------------------
    // 渲染与事件：WidgetHost 与基类原版链路并存
    // ------------------------------------------------------------------

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // AbstractContainerScreen.tick() 是 final，动画推进挪到每帧渲染入口
        ui.tick();

        // 辅助键（R）按下 / 松开 → 切换 KeyBindBar 展示第二套（6-10）标签
        boolean aux = com.cimdy.coe.client.InputState.isAuxDown();
        if (aux != lastAux) {
            lastAux = aux;
            refreshKeyLabels();
        }

        // 从菜单（双端同步）回读服务端权威状态，变化时重建列表并同步按键绑定
        SkillAssembly latestAsm = this.menu.getAssembly();
        if (!latestAsm.equals(this.assembly)) {
            this.assembly = latestAsm;
            this.refreshRows();
            this.syncKeyBinds();
        }

        // 先原版渲染菜单槽位/物品/tooltip，再叠 UI
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        ui.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return ui.mouseClicked(event) || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return ui.mouseReleased(event) || super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return ui.mouseDragged(event, dx, dy) || super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return ui.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
                || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return ui.keyPressed(event) || super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return ui.charTyped(event) || super.charTyped(event);
    }

    /** 原版标题/背包标签与自定义标题重复且错位，整体屏蔽，改由本 Screen 自绘。 */
    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    // ============================ 拖放辅助类型 ============================

    /** 拖拽源 / 目标定位：主技能槽、辅助槽、或内嵌背包（卸下）。 */
    private enum DragKind { MAIN, SUPPORT, INVENTORY }

    /** 携带在 {@link Draggable#payload()} 上的定位信息，落点时由 {@link #applyDrop} 读取。 */
    private static final class DragTarget {
        final DragKind kind;
        final int slot;     // 技能槽索引（MAIN / SUPPORT）
        final int socket;   // 辅助槽索引（SUPPORT）
        static final DragTarget INVENTORY = new DragTarget(DragKind.INVENTORY, -1, -1);

        DragTarget(DragKind kind, int slot, int socket) {
            this.kind = kind;
            this.slot = slot;
            this.socket = socket;
        }
    }

    /** 包住技能槽 Slot 的放置区；携带目标定位，autoScope 继承窗口作用域。 */
    private static final class SocketDropZone extends NodeDropZone {
        final DragTarget target;

        SocketDropZone(Node node, DragTarget target) {
            super(node, s -> { /* 真正搬数据在 onDropInto（onAssemblyDrop） */ });
            this.target = target;
            autoScope();
        }
    }
}
