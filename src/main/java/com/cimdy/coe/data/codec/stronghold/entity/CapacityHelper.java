package com.cimdy.coe.data.codec.stronghold.entity;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CapacityHelper {
    // 根据实体标签返回基础搬运力
    public static int getBaseCapacity(LivingEntity entity) {
        if (entity.is(Tags.EntityTypes.BOSSES)) return 256;
        if (entity.is(EntityTypeTags.CAN_EQUIP_SADDLE) || entity.is(EntityTypeTags.BOAT)) return 128;
        if (entity.is(EntityTypeTags.ZOMBIES) || entity.is(EntityTypeTags.SKELETONS) ||
                entity.is(EntityTypeTags.UNDEAD) || entity.is(EntityTypeTags.ILLAGER) ||
                entity.is(EntityTypeTags.RAIDERS)) return 64;
        if (entity.is(EntityTypeTags.FOLLOWABLE_FRIENDLY_MOBS)) return 32;
        return 16; // 微型
    }

    // 计算单个物品堆占用容量 (SU)
    public static int getStackVolume(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return stack.getCount() * (64 / stack.getMaxStackSize());
    }

    // 列表总容量
    public static int getTotalVolume(List<ItemStack> stacks) {
        int total = 0;
        for (ItemStack s : stacks) total += getStackVolume(s);
        return total;
    }

    // 实体当前已用容量
    public static int getUsedCapacity(LivingEntity entity) {
        StrongholdEntity data = entity.getData(AttachmentRegistry.ENTITY_DATA);
        return getTotalVolume(data.stacks());
    }

    // 最大搬运容量（来自属性）
    public static int getMaxCapacity(LivingEntity entity) {
        return (int) entity.getAttributeValue(AttributeRegistry.ITEM_CAPACITY);
    }

    // 剩余容量
    public static int getRemainingCapacity(LivingEntity entity) {
        return Math.max(0, getMaxCapacity(entity) - getUsedCapacity(entity));
    }

    /** 获取实体当前携带的所有物品（可变副本） */
    public static List<ItemStack> getStacks(LivingEntity entity) {
        StrongholdEntity data = entity.getData(AttachmentRegistry.ENTITY_DATA);
        return new ArrayList<>(data.stacks());
    }

    /**
     * 尝试将物品堆加入实体背包，会优先与已有同种物品堆合并。
     * 返回实际放入的数量（会修改传入的 stack）。
     */
    public static int addItem(LivingEntity entity, ItemStack stack) {
        if (stack.isEmpty()) return 0;

        int maxStackSize = stack.getMaxStackSize();
        int needed = stack.getCount();
        StrongholdEntity data = entity.getData(AttachmentRegistry.ENTITY_DATA);
        List<ItemStack> carrying = new ArrayList<>(data.stacks());

        // 1. 尝试与已有同种物品堆合并
        for (ItemStack held : carrying) {
            if (ItemStack.isSameItemSameComponents(held, stack)) {
                int space = maxStackSize - held.getCount();
                if (space > 0) {
                    int add = Math.min(needed, space);
                    held.grow(add);
                    needed -= add;
                    if (needed == 0) {
                        entity.setData(AttachmentRegistry.ENTITY_DATA, data.set(carrying));
                        int added = stack.getCount(); // 全部放入
                        stack.setCount(0);
                        return added;
                    }
                }
            }
        }

        // 2. 剩余部分需要创建新堆，检查容量
        int volPerItem = 64 / maxStackSize;
        int used = getTotalVolume(carrying);
        int maxCap = getMaxCapacity(entity);
        int freeVol = Math.max(0, maxCap - used);
        int totalAddedToNewStacks = 0;

        while (needed > 0 && freeVol > 0) {
            int canAddCount = Math.min(needed, freeVol / volPerItem);
            if (canAddCount == 0) break;
            int stackCount = Math.min(canAddCount, maxStackSize);
            ItemStack newStack = stack.copy();
            newStack.setCount(stackCount);
            carrying.add(newStack);
            totalAddedToNewStacks += stackCount;
            needed -= stackCount;
            freeVol -= stackCount * volPerItem;
        }

        entity.setData(AttachmentRegistry.ENTITY_DATA, data.set(carrying));
        int totalAdded = stack.getCount() - needed;
        stack.shrink(totalAdded);
        return totalAdded;
    }

    /** 尝试将整个 ItemStack 加入实体背包，返回未加入的数量（0 表示全部加入），不修改传入的 stack */
    public static int tryAddItem(LivingEntity entity, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        ItemStack copy = stack.copy();
        int added = addItem(entity, copy);
        return stack.getCount() - added;
    }

    /** 从实体背包中移除与 target 完全匹配的物品堆，返回实际移除的数量 */
    public static int removeItem(LivingEntity entity, ItemStack target) {
        StrongholdEntity data = entity.getData(AttachmentRegistry.ENTITY_DATA);
        List<ItemStack> carrying = new ArrayList<>(data.stacks());

        int removed = 0;
        for (Iterator<ItemStack> it = carrying.iterator(); it.hasNext() && removed < target.getCount();) {
            ItemStack held = it.next();
            if (ItemStack.isSameItem(held, target)) {
                int toRemove = Math.min(held.getCount(), target.getCount() - removed);
                held.shrink(toRemove);
                removed += toRemove;
                if (held.isEmpty()) it.remove();
            }
        }

        entity.setData(AttachmentRegistry.ENTITY_DATA, data.set(carrying));
        return removed;
    }

    /** 尝试移除指定数量的某种物品（仅匹配物品类型），返回未能移除的数量 */
    public static int removeItems(LivingEntity entity, Item item, int count) {
        StrongholdEntity data = entity.getData(AttachmentRegistry.ENTITY_DATA);
        List<ItemStack> carrying = new ArrayList<>(data.stacks());
        int toRemove = count;
        Iterator<ItemStack> it = carrying.iterator();
        while (it.hasNext() && toRemove > 0) {
            ItemStack held = it.next();
            if (held.getItem() == item) {
                int take = Math.min(held.getCount(), toRemove);
                held.shrink(take);
                toRemove -= take;
                if (held.isEmpty()) it.remove();
            }
        }
        entity.setData(AttachmentRegistry.ENTITY_DATA, data.set(carrying));
        return toRemove;
    }

    /** 从实体背包中提取指定数量的物品（仅匹配物品类型），返回实际提取到的物品堆 */
    public static ItemStack extractItems(LivingEntity entity, Item item, int count) {
        int removed = removeItems(entity, item, count);
        return new ItemStack(item, count - removed);
    }

    /** 尝试移除指定数量的物品（使用 ItemResource 匹配），返回未能移除的数量 */
    public static int removeItems(LivingEntity entity, ItemResource resource, int count) {
        StrongholdEntity data = entity.getData(AttachmentRegistry.ENTITY_DATA);
        List<ItemStack> carrying = new ArrayList<>(data.stacks());
        int toRemove = count;
        Iterator<ItemStack> it = carrying.iterator();
        while (it.hasNext() && toRemove > 0) {
            ItemStack held = it.next();
            if (resource.matches(held)) {
                int take = Math.min(held.getCount(), toRemove);
                held.shrink(take);
                toRemove -= take;
                if (held.isEmpty()) it.remove();
            }
        }
        entity.setData(AttachmentRegistry.ENTITY_DATA, data.set(carrying));
        return toRemove;
    }

    /** 从实体背包中提取指定数量的物品（使用 ItemResource 匹配），返回实际提取到的物品堆 */
    public static ItemStack extractItems(LivingEntity entity, ItemResource resource, int count) {
        int removed = removeItems(entity, resource, count);
        return resource.toStack(count - removed);
    }

    // ================= 容器交互（ItemStack 版本） =================

    /**
     * 从给定坐标的容器中提取与 stack 匹配的物品，返回实际提取到的 ItemStack。
     * 如果坐标未加载或容器不可用，返回空物品堆。
     */
    public static ItemStack extractFromContainer(ServerLevel level, BlockPos containerPos, ItemStack stack) {
        if (!level.isLoaded(containerPos) || stack.isEmpty()) return ItemStack.EMPTY;
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, containerPos, null);
        if (handler == null) return ItemStack.EMPTY;
        return extractFromContainer(handler, stack);
    }

    /**
     * 向给定坐标的容器插入物品，返回未能插入的数量（0 表示全部插入成功）。
     */
    public static int insertIntoContainer(ServerLevel level, BlockPos containerPos, ItemStack stack) {
        if (!level.isLoaded(containerPos) || stack.isEmpty()) return stack.getCount();
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, containerPos, null);
        if (handler == null) return stack.getCount();
        return insertIntoContainer(handler, stack);
    }

    // ================= 已有的 ResourceHandler 版本 =================

    /**
     * 从 ResourceHandler 中提取与给定 ItemStack 完全匹配的物品，返回实际提取到的物品堆（数量可能少于请求）。
     */
    public static ItemStack extractFromContainer(ResourceHandler<ItemResource> handler, ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemResource resource = ItemResource.of(stack);
        int amount = stack.getCount();

        int extractedTotal = 0;
        try (Transaction txn = Transaction.openRoot()) {
            int remaining = amount;
            for (int slot = 0; slot < handler.size() && remaining > 0; slot++) {
                ItemResource res = handler.getResource(slot);
                if (res.equals(resource)) {
                    int available = handler.getAmountAsInt(slot);
                    int take = Math.min(remaining, available);
                    int extracted = handler.extract(slot, resource, take, txn);
                    if (extracted > 0) {
                        remaining -= extracted;
                        extractedTotal += extracted;
                    }
                }
            }
            if (extractedTotal > 0) txn.commit();
        }
        return resource.toStack(extractedTotal);
    }

    /**
     * 尝试将给定的 ItemStack 插入 ResourceHandler，返回未能插入的数量（0 表示全部插入成功）。
     */
    public static int insertIntoContainer(ResourceHandler<ItemResource> handler, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        ItemResource resource = ItemResource.of(stack);
        int amount = stack.getCount();

        int insertedTotal = 0;
        try (Transaction txn = Transaction.openRoot()) {
            int remaining = amount;
            for (int slot = 0; slot < handler.size() && remaining > 0; slot++) {
                int inserted = handler.insert(slot, resource, remaining, txn);
                if (inserted > 0) {
                    remaining -= inserted;
                    insertedTotal += inserted;
                }
            }
            if (insertedTotal > 0) txn.commit();
        }
        return amount - insertedTotal;
    }

    /**
     * 在候选容器中选择最适合提取指定数量物品的容器。
     * 优先选择能提供至少 requiredAmount 的容器，若都不够则选数量最多的。
     * 在同等数量下，选择距离最近的。
     */
    public static BlockPos findBestContainerForResource(LivingEntity entity, List<BlockPos> candidates,
                                                        ItemResource resource, int requiredAmount) {
        ServerLevel level = (ServerLevel) entity.level();
        BlockPos bestPos = null;
        int bestAmount = -1;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : candidates) {
            if (!level.isLoaded(pos)) continue;
            ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, null);
            if (handler == null) continue;

            // 统计该容器中目标物品的总量
            int total = 0;
            for (int slot = 0; slot < handler.size(); slot++) {
                ItemResource res = handler.getResource(slot);
                if (res.equals(resource)) {
                    total += handler.getAmountAsInt(slot);
                }
            }
            if (total == 0) continue;

            double dist = entity.distanceToSqr(Vec3.atCenterOf(pos));

            if (bestPos == null) {
                bestPos = pos;
                bestAmount = total;
                bestDist = dist;
                continue;
            }

            boolean currentEnough = total >= requiredAmount;
            boolean bestEnough = bestAmount >= requiredAmount;

            if (currentEnough && !bestEnough) {
                // 当前容器够，之前的不够 → 换为当前
                bestPos = pos;
                bestAmount = total;
                bestDist = dist;
            } else if (currentEnough) {
                // 都够：选距离近的
                if (dist < bestDist) {
                    bestPos = pos;
                    bestAmount = total;
                    bestDist = dist;
                }
            } else {
                // 都不够：选数量多的；数量相同选距离近的
                if (total > bestAmount || (total == bestAmount && dist < bestDist)) {
                    bestPos = pos;
                    bestAmount = total;
                    bestDist = dist;
                }
            }
        }
        return bestPos;
    }
}