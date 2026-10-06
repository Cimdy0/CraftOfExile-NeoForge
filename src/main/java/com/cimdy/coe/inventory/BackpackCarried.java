package com.cimdy.coe.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.WeakHashMap;

/**
 * 背包浮窗取放的服务端权威执行（非容器屏，不走 Menu）。
 *
 * <p>语义与 MC 标准背包拾放一致（左键）：空手持 → 拿起整叠；手持且槽空 → 放下；
 * 同类可堆叠 → 合并（超出的留在手持）；不同物品 → 交换。手持状态按玩家维度
 * 以弱引用保存（玩家下线自动回收）。所有修改直接作用于服务端 {@code PlayerInventory}，
 * 权威性由服务端保证，客户端只按回包镜像。
 */
public final class BackpackCarried {
    private static final WeakHashMap<ServerPlayer, ItemStack> CARRIED = new WeakHashMap<>();

    private BackpackCarried() {
    }

    /** 执行一次槽位点击（服务端主线程调用），返回点击后的手持栈（可能为空）。 */
    public static ItemStack handleTakePut(ServerPlayer player, int slotIndex, int button) {
        Inventory inv = player.getInventory();
        ItemStack carried = CARRIED.getOrDefault(player, ItemStack.EMPTY);
        if (slotIndex < 0 || slotIndex >= inv.getContainerSize()) {
            return carried;
        }
        ItemStack inSlot = inv.getItem(slotIndex);

        if (button == 0) {
            if (carried.isEmpty()) {
                // 拿起整叠
                inv.setItem(slotIndex, ItemStack.EMPTY);
                carried = inSlot;
            } else if (inSlot.isEmpty()) {
                // 放下
                inv.setItem(slotIndex, carried);
                carried = ItemStack.EMPTY;
            } else if (ItemStack.isSameItemSameComponents(carried, inSlot)
                    && inSlot.getCount() < inSlot.getMaxStackSize()) {
                // 合并（超出部分留在手持）
                int move = Math.min(inSlot.getMaxStackSize() - inSlot.getCount(), carried.getCount());
                inSlot.grow(move);
                carried.shrink(move);
            } else {
                // 交换
                inv.setItem(slotIndex, carried);
                carried = inSlot;
            }
        }

        if (carried.isEmpty()) {
            CARRIED.remove(player);
        } else {
            CARRIED.put(player, carried);
        }
        return CARRIED.getOrDefault(player, ItemStack.EMPTY);
    }

    public static ItemStack carriedOf(ServerPlayer player) {
        return CARRIED.getOrDefault(player, ItemStack.EMPTY);
    }
}
