package com.cimdy.coe.item.flask;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.item.flask.FlaskSave;
import com.cimdy.coe.registry.item.FlaskSlotRegistry;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Flask 槽位容器：把玩家的 {@code flask_save} 附件（2 槽）暴露为 {@link Container}，
 * 供 {@link FlaskSlot} 与背包菜单（InventoryMenu）统一读写。数据落点 = 玩家附件
 * （服务端权威、registerSync 同步客户端），Slot 只是视图。
 */
public class FlaskContainer implements Container {

    private final Player player;

    public FlaskContainer(Player player) {
        this.player = player;
    }

    private FlaskSave save() {
        return player.getData(AttachmentRegistry.FLASK_SAVE);
    }

    @Override
    public int getContainerSize() {
        return FlaskSlotRegistry.totalSlots(player.level().registryAccess());
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!save().get(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return save().get(index);
    }

    @Override
    public ItemStack removeItem(int index, int amount) {
        ItemStack stack = save().get(index);
        ItemStack removed = stack.isEmpty() ? ItemStack.EMPTY : stack.split(amount);
        player.setData(AttachmentRegistry.FLASK_SAVE, save().with(index, stack));
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = save().get(index);
        if (!stack.isEmpty()) {
            player.setData(AttachmentRegistry.FLASK_SAVE, save().with(index, ItemStack.EMPTY));
        }
        return stack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        player.setData(AttachmentRegistry.FLASK_SAVE, save().with(index, stack));
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
        // 统一类别判定：物品携带药剂状态即属药剂类别，取代 instanceof FlaskItem。
        return !stack.isEmpty() && ModifiableStateOps.flask(stack) != null;
    }

    @Override
    public void clearContent() {
        player.setData(AttachmentRegistry.FLASK_SAVE, FlaskSave.EMPTY);
    }
}
