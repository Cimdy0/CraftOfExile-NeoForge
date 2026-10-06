package com.cimdy.coe.data.codec.stronghold.task;

import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;

public interface TransportTask {
        ItemResource getResource();
        int getAmount();
        BlockPos getSourceContainerPos();
        @Nullable
        BlockPos getTargetContainerPos();
        boolean isBeforePickup();       // 货物是否尚未被提取
        /** 获取有效的源容器坐标（若为生产站点，则返回第一个已加载的存储容器） */
        default BlockPos getEffectiveSourceContainerPos(ServerLevel level) {
            return StrongholdHelper.resolveStorageContainer(level, getSourceContainerPos());
        }

        default BlockPos getEffectiveTargetContainerPos(ServerLevel level) {
            return StrongholdHelper.resolveStorageContainer(level, getTargetContainerPos());
        }
    }
