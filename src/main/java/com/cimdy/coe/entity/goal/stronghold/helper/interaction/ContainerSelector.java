package com.cimdy.coe.entity.goal.stronghold.helper.interaction;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockEntry;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockMode;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockSet;
import com.cimdy.coe.entity.goal.stronghold.behavior.IGoalContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;
import java.util.function.BiFunction;

/**
 * 容器选择策略：从候选容器中为指定物品挑选最佳存放/取出容器。
 * <p>
 * 默认策略见 {@link ContainerSelector#DEFAULT_STRATEGY}（需求 > 存储 > 已有 > 最近）。
 * 新增自定义选择逻辑（如按容器剩余容量、按标签过滤）只需调用
 * {@link ContainerSelector#setStrategy(ContainerSelectionStrategy)} 替换，无需改动任何调用方。
 */
@FunctionalInterface
interface ContainerSelectionStrategy extends BiFunction<IGoalContext, ItemResource, BlockPos> {
    /** 从候选容器中为 resource 选择最佳容器；无合适容器时返回 null。 */
    @Override
    BlockPos apply(IGoalContext context, ItemResource resource);
}

public class ContainerSelector {

    /** 默认选择策略：需求容器 > 存储容器 > 已有该物品容器 > 最近容器。 */
    public static final ContainerSelectionStrategy DEFAULT_STRATEGY = ContainerSelector::defaultSelect;

    private static ContainerSelectionStrategy strategy = DEFAULT_STRATEGY;

    /** 替换全局容器选择策略。 */
    public static synchronized void setStrategy(ContainerSelectionStrategy s) {
        strategy = s != null ? s : DEFAULT_STRATEGY;
    }

    public static ContainerSelectionStrategy getStrategy() {
        return strategy;
    }

    public static BlockPos findBestContainerForItem(IGoalContext context, ItemResource resource) {
        List<BlockPos> candidates = context.getStorageBlocks();
        if (candidates.isEmpty()) return null;
        return strategy.apply(context, resource);
    }

    private static BlockPos defaultSelect(IGoalContext context, ItemResource resource) {
        ServerLevel level = context.getLevel();
        List<BlockPos> candidates = context.getStorageBlocks();
        if (candidates.isEmpty()) return null;

        BlockPos demandContainer = null;
        BlockPos storageContainer = null;
        BlockPos existingContainer = null;
        BlockPos nearestContainer = null;
        double nearestDist = Double.MAX_VALUE;

        for (BlockPos pos : candidates) {
            if (!level.isLoaded(pos)) continue;
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) continue;

            double dist = context.getMob().distanceToSqr(Vec3.atCenterOf(pos));

            StockSet stockSet = be.getData(AttachmentRegistry.STOCK_SET);
            if (stockSet != null) {
                for (StockEntry entry : stockSet.entries()) {
                    if (!entry.resource().is(resource.getItem())) continue;
                    if (isDemandMode(entry)) {
                        if (demandContainer == null) demandContainer = pos;
                    } else if (isStorageMode(entry)) {
                        if (storageContainer == null) storageContainer = pos;
                    } else {
                        if (existingContainer == null) existingContainer = pos;
                    }
                    break;
                }
            }

            if (existingContainer == null) {
                ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, null);
                if (handler != null) {
                    for (int slot = 0; slot < handler.size(); slot++) {
                        if (handler.getResource(slot).equals(resource)) {
                            existingContainer = pos;
                            break;
                        }
                    }
                }
            }

            if (dist < nearestDist) {
                nearestDist = dist;
                nearestContainer = pos;
            }
        }

        if (demandContainer != null) return demandContainer;
        if (storageContainer != null) return storageContainer;
        if (existingContainer != null) return existingContainer;
        return nearestContainer != null ? nearestContainer : candidates.get(0);
    }

    private static boolean isDemandMode(StockEntry entry) {
        return entry.supply().first().getSecond() == StockMode.DEMAND
                || entry.supply().second().getSecond() == StockMode.DEMAND;
    }

    private static boolean isStorageMode(StockEntry entry) {
        return entry.supply().first().getSecond() == StockMode.STORAGE
                || entry.supply().second().getSecond() == StockMode.STORAGE;
    }
}