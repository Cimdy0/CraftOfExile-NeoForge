package com.cimdy.coe.ui.menu;

import com.cimdy.coe.registry.item.AccessorySlotRegistry;
import com.cimdy.coe.registry.item.CharmSlotRegistry;
import com.cimdy.coe.registry.item.FlaskSlotRegistry;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import com.cimdy.coe.item.accessory.AccessoryContainer;
import com.cimdy.coe.item.accessory.AccessorySlot;
import com.cimdy.coe.item.charm.CharmContainer;
import com.cimdy.coe.item.charm.CharmSlot;
import com.cimdy.coe.item.flask.FlaskContainer;
import com.cimdy.coe.item.flask.FlaskSlot;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 药剂 / 护符 / 饰品 统一面板菜单：把三类专用槽位集中到同一个窗口渲染与交互。
 * 背包 36 槽由基类 {@link AbstractPanelMenu} 提供；三类专用槽（数据驱动、来自各自的注册表）
 * 追加在背包之后，由单一「打开」按钮从背包界面点开。
 *
 * <p>药剂 / 饰品槽位数据来自服务端配置 {@code config/coe/slots.json}（无配置时走内置兜底），
 * 护符槽另有其来源；均不再硬编码槽数，与装备物品数据驱动范式一致。
 */
public class ExtraSlotsMenu extends AbstractPanelMenu {

    // 三类专用槽在菜单中的下标区间（背包 36 槽之后依次排列）
    private final int flaskStart, flaskEnd;
    private final int charmStart, charmEnd;
    private final int accessoryStart, accessoryEnd;

    // 统一窗口内的列布局（相对 leftPos/topPos；背包占左侧 176 宽，专用槽置于其右侧三列）
    public static final int FLASK_X = 190;
    public static final int CHARM_X = 212;
    public static final int ACCESSORY_X = 234;
    public static final int COL_Y0 = 18;
    public static final int COL_STEP = 20;

    public ExtraSlotsMenu(int containerId, Inventory playerInventory) {
        super(MenuRegistry.EXTRA_SLOTS_MENU.get(), containerId, playerInventory);
        RegistryAccess access = player.level().registryAccess();
        Player player = playerInventory.player;

        FlaskContainer flaskContainer = new FlaskContainer(player);
        CharmContainer charmContainer = new CharmContainer(player);
        List<AccessorySlotRegistry.SlotDescriptor> accDescs = AccessorySlotRegistry.slots(access);
        AccessoryContainer accessoryContainer = new AccessoryContainer(player, accDescs.size());

        int idx = 36;
        flaskStart = idx;
        for (AccessorySlotRegistry.SlotDescriptor d : FlaskSlotRegistry.slots(access)) {
            this.addSlot(new FlaskSlot(flaskContainer, d.globalIndex(), FLASK_X, COL_Y0 + d.globalIndex() * COL_STEP));
            idx++;
        }
        flaskEnd = idx;
        charmStart = idx;
        for (AccessorySlotRegistry.SlotDescriptor d : CharmSlotRegistry.slots(player)) {
            this.addSlot(new CharmSlot(charmContainer, d.globalIndex(), CHARM_X, COL_Y0 + d.globalIndex() * COL_STEP));
            idx++;
        }
        charmEnd = idx;
        accessoryStart = idx;
        for (AccessorySlotRegistry.SlotDescriptor d : accDescs) {
            this.addSlot(new AccessorySlot(accessoryContainer, d.globalIndex(), d.category(), ACCESSORY_X, COL_Y0 + d.globalIndex() * COL_STEP));
            idx++;
        }
        accessoryEnd = idx;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        // 专用槽 → 背包（0..36）
        if (index >= flaskStart) {
            moveInto(slot, 0, 36);
            return ItemStack.EMPTY;
        }
        // 背包来源 → 对应专用槽（统一类别判定：按物品状态类别路由，取代 instanceof）
        ItemStack stack = slot.getItem();
        if (ModifiableStateOps.flask(stack) != null) {
            moveIntoFirstEmpty(slot, flaskStart, flaskEnd);
        } else if (ModifiableStateOps.charm(stack) != null) {
            moveIntoFirstEmpty(slot, charmStart, charmEnd);
        } else {
            for (int i = accessoryStart; i < accessoryEnd; i++) {
                Slot s = this.slots.get(i);
                if (s instanceof AccessorySlot as && s.getItem().isEmpty() && as.mayPlace(stack)) {
                    moveInto(slot, i, i + 1);
                    break;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /** 把槽内物品整体移入 [start, end) 区间（写回剩余量，空则清空）。 */
    private void moveInto(Slot slot, int start, int end) {
        ItemStack moving = slot.getItem().copy();
        this.moveItemStackTo(moving, start, end, false);
        slot.set(moving.isEmpty() ? ItemStack.EMPTY : moving);
        slot.setChanged();
    }

    /** 把槽内物品移入 [start, end) 区间内的首个空且允许放入的槽。 */
    private void moveIntoFirstEmpty(Slot slot, int start, int end) {
        ItemStack moving = slot.getItem().copy();
        for (int i = start; i < end; i++) {
            Slot target = this.slots.get(i);
            if (target.getItem().isEmpty() && target.mayPlace(moving)) {
                target.set(moving);
                target.setChanged();
                slot.set(ItemStack.EMPTY);
                slot.setChanged();
                return;
            }
        }
    }
}
