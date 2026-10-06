package com.cimdy.coe.stronghold.transport;

import com.cimdy.coe.stronghold.area.StrongholdArea;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.cimdy.coe.stronghold.area.StrongholdRepository;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.data.codec.stronghold.StrongholdGroup;
import com.cimdy.coe.data.codec.stronghold.SaveData;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskStatus;
import com.cimdy.coe.data.codec.stronghold.task.TransportTask;
import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.ActiveTask;
import com.cimdy.coe.data.codec.stronghold.task.helper.state.StrongholdState;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 跨维传送与容器几何服务：负责传送信息计算、有效容器坐标解析、箱子双块合并等。
 * 从原 God Class {@link StrongholdHelper} 拆分而来，逻辑完全一致。
 */
public final class TeleportCoordinator {
    private TeleportCoordinator() {
    }

    public static StrongholdHelper.TransportInfo getTransportInfo(ServerLevel level, BlockPos containerPos, ItemResource resource) {
        BlockPos effectivePos = getEffectiveContainerPos(level, containerPos);
        if (effectivePos == null) return StrongholdHelper.TransportInfo.EMPTY;

        StrongholdGroup group = findHoldGroupForContainer(level, effectivePos);
        if (group == null) return StrongholdHelper.TransportInfo.EMPTY;

        List<ActiveTask> allTasks = new ArrayList<>();
        for (GlobalPos tp : group.holds().values()) {
            Stronghold hold = StrongholdRepository.getHoldFromBlockEntity(level, tp);
            if (hold == null) continue;
            StrongholdState state = hold.holdState();
            allTasks.addAll(state.pendingTasks());
            for (List<ActiveTask> list : state.getActiveTasks().values())
                allTasks.addAll(list);
        }

        int outgoing = 0, incoming = 0;
        for (ActiveTask at : allTasks) {
            if (at.status() == TaskStatus.COMPLETED || at.status() == TaskStatus.FAILED) continue;

            Task current = at.task();
            while (current != null) {
                if (current instanceof TransportTask tt) {
                    if (!tt.getResource().equals(resource)) {
                        current = current.getNext();
                        continue;
                    }
                    if (effectivePos.equals(tt.getEffectiveSourceContainerPos(level))) {
                        if (tt.isBeforePickup()) outgoing += tt.getAmount();
                    }
                    if (tt.getEffectiveTargetContainerPos(level) != null && effectivePos.equals(tt.getEffectiveTargetContainerPos(level))) {
                        incoming += tt.getAmount();
                    }
                }
                current = current.getNext();
            }
        }
        updateContainerStock(level, effectivePos, resource, incoming - outgoing);
        return new StrongholdHelper.TransportInfo(outgoing, incoming);
    }

    /**
     * 根据坐标获取有效的存储容器坐标。
     * 如果该坐标是 WorkSite 的 workBlock，则返回其第一个已加载的存储容器；
     * 否则直接返回原坐标。
     */
    public static BlockPos resolveStorageContainer(ServerLevel level, BlockPos pos) {
        Optional<Site> site = StrongholdRepository.getProductionSite(level, pos);
        if (site.isPresent()) {
            for (BlockPos storagePos : site.get().getStorages()) {
                if (level.isLoaded(storagePos)) {
                    return storagePos;
                }
            }
        }
        return pos;
    }

    public static BlockEntity getRepresentativeBlockEntity(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) return null;
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return null;
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(ChestBlock.TYPE) && be instanceof ChestBlockEntity) {
            ChestType type = state.getValue(ChestBlock.TYPE);
            if (type == ChestType.RIGHT) {
                Direction connected = ChestBlock.getConnectedDirection(state);
                BlockPos leftPos = pos.relative(connected);
                if (!level.isLoaded(leftPos)) return null;
                return level.getBlockEntity(leftPos);
            }
        }
        return be;
    }

    public static BlockPos getEffectiveContainerPos(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            ChestType type = chest.getBlockState().getValue(ChestBlock.TYPE);
            if (type == ChestType.LEFT || type == ChestType.RIGHT) {
                Direction connected = ChestBlock.getConnectedDirection(chest.getBlockState());
                BlockEntity otherBe = level.getBlockEntity(pos.relative(connected));
                if (!chest.hasData(AttachmentRegistry.STOCK_SET) &&
                        otherBe instanceof ChestBlockEntity otherChest &&
                        otherChest.hasData(AttachmentRegistry.STOCK_SET))
                    return otherBe.getBlockPos();
            }
        }
        return pos;
    }

    public static BlockEntity getCurrentBlockEntity(Level level, BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity chest) {
            ChestType type = chest.getBlockState().getValue(ChestBlock.TYPE);
            if (type == ChestType.LEFT || type == ChestType.RIGHT) {
                Direction connected = ChestBlock.getConnectedDirection(chest.getBlockState());
                BlockEntity otherBe = level.getBlockEntity(chest.getBlockPos().relative(connected));
                if (!chest.hasData(AttachmentRegistry.STOCK_SET) &&
                        otherBe instanceof ChestBlockEntity otherChest &&
                        otherChest.hasData(AttachmentRegistry.STOCK_SET))
                    return otherBe;
            }
        }
        return blockEntity;
    }

    public static int countItemsInContainer(ResourceHandler<ItemResource> handler, ItemResource target) {
        int count = 0;
        for (int slot = 0; slot < handler.size(); slot++) {
            var stack = handler.getResource(slot);
            if (stack.equals(target)) {
                count += handler.getAmountAsInt(slot);
            }
        }
        return count;
    }

    private static void updateContainerStock(ServerLevel level, BlockPos containerPos,
                                             ItemResource resource, int transit) {
        BlockEntity be = level.getBlockEntity(containerPos);
        if (be == null) return;
        var stockSet = be.getData(AttachmentRegistry.STOCK_SET);
        be.setData(AttachmentRegistry.STOCK_SET, stockSet.setTransit(resource, transit));
    }

    @Nullable
    private static StrongholdGroup findHoldGroupForContainer(ServerLevel level, BlockPos containerPos) {
        SaveData data = StrongholdRepository.getHoldData(level);
        for (StrongholdGroup group : data.groups().values()) {
            for (GlobalPos holdPos : group.holds().values()) {
                if (!holdPos.dimension().equals(level.dimension())) continue;
                Stronghold hold = StrongholdRepository.getHoldFromBlockEntity(level, holdPos.pos());
                if (hold != null && StrongholdArea.contains(hold, holdPos.pos(), containerPos))
                    return group;
            }
        }
        return null;
    }
}
