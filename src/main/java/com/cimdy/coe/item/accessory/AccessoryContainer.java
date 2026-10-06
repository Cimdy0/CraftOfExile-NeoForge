package com.cimdy.coe.item.accessory;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.item.AccessorySave;
import com.cimdy.coe.data.helper.stat.AccessoryStatHelper;
import com.cimdy.coe.data.helper.engine.EquipRules;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 饰品栏容器：把玩家的 {@code accessory_save} 附件（扁平的全局槽列表）暴露为 {@link Container}，
 * 供 {@link AccessorySlot} 与背包菜单统一读写。数据落点 = 玩家附件（服务端权威、registerSync 同步客户端），
 * Slot 只是视图。
 *
 * <p>槽位读写会同步驱动 Stat 注入/移除：入槽时把物品 {@code ITEM_STAT_ENTRIES} 重烤为
 * {@code accessory:<globalIndex>} 前缀并注入 StatStore；脱槽时按前缀整体移除。
 */
public class AccessoryContainer implements Container {

    private final Player player;
    private final int size;

    public AccessoryContainer(Player player, int size) {
        this.player = player;
        this.size = size;
    }

    private AccessorySave save() {
        return player.getData(AttachmentRegistry.ACCESSORY_SAVE);
    }

    @Override
    public int getContainerSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        AccessorySave s = save();
        for (int i = 0; i < size; i++) {
            if (!s.get(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return save().get(index);
    }

    @Override
    public ItemStack removeItem(int index, int amount) {
        ItemStack stack = getItem(index);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack removed = stack.split(amount);
        setItem(index, stack);
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = getItem(index);
        if (!stack.isEmpty()) setItem(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        ItemStack old = getItem(index);
        if (!old.isEmpty()) AccessoryStatHelper.remove(player, index);
        if (!stack.isEmpty()) AccessoryStatHelper.apply(player, stack, index);
        player.setData(AttachmentRegistry.ACCESSORY_SAVE, save().with(index, stack));
    }

    @Override
    public void setChanged() {
        // 附件即时写入，无额外变更通知
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        // 通用「装备 / 持握规则」引擎：饰品槽以 "accessory" 槽位上下文参与求值；
        // 未配置任何 slot=accessory 的规则时恒放行（与既有行为一致）。
        // 未来「某槽位只接受某标签 / 与另一手互斥」只加一条数据规则即可，零 Java 改动。
        return !EquipRules.blocked(player, stack, EquipRules.ACCESSORY_CONTEXT);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < size; i++) AccessoryStatHelper.remove(player, i);
        player.setData(AttachmentRegistry.ACCESSORY_SAVE, AccessorySave.EMPTY);
    }
}
