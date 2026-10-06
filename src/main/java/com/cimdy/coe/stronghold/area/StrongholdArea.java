package com.cimdy.coe.stronghold.area;

import com.cimdy.coe.data.codec.stronghold.Stronghold;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 据点范围服务（不规则区块集合模型）。
 *
 * <p>取代旧的「以终端为中心的方形区块半径」判定：据点范围由玩家用界桩逐块绑定而成，
 * 存于 {@link Stronghold#boundChunks()}，终端所在区块恒含其中。本类是范围判定、区块遍历、
 * 强制加载（ticket）的<b>唯一入口</b>，所有旧的 {@code isInArea(center, target, radius)}
 * 调用点均已改道至此。
 */
public final class StrongholdArea {

    private StrongholdArea() {
    }

    // ==================== 范围判定 ====================

    /** 目标坐标是否落在据点的绑定区块内。 */
    public static boolean contains(Stronghold hold, BlockPos terminalPos, BlockPos target) {
        if (hold == null || terminalPos == null || target == null) return false;
        return hold.isChunkBound(terminalPos, new ChunkPos(target.getX() >> 4, target.getZ() >> 4));
    }

    /** 跨维度安全版：维度不一致直接判否。 */
    public static boolean contains(Stronghold hold, GlobalPos terminalPos, GlobalPos target) {
        if (hold == null || terminalPos == null || target == null) return false;
        if (!terminalPos.dimension().equals(target.dimension())) return false;
        return contains(hold, terminalPos.pos(), target.pos());
    }

    // ==================== 区块 / 容器遍历 ====================

    /** 据点占据的全部区块坐标（含中心）。 */
    public static Set<ChunkPos> chunks(Stronghold hold, BlockPos terminalPos) {
        return hold.chunksWithCenter(terminalPos);
    }

    /**
     * 取据点内已加载的区块。绑定区块通常已被 ticket 强制加载，
     * 这里仍只取<b>已加载</b>的，避免遍历时触发同步区块生成造成卡顿。
     */
    public static List<LevelChunk> loadedChunks(Level level, Stronghold hold, BlockPos terminalPos) {
        List<LevelChunk> list = new ArrayList<>();
        for (ChunkPos cp : chunks(hold, terminalPos)) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(cp.x(), cp.z());
            if (chunk != null) list.add(chunk);
        }
        return list;
    }

    /** 据点内所有容器方块实体。 */
    public static List<BaseContainerBlockEntity> containers(Level level, Stronghold hold, BlockPos terminalPos) {
        List<BaseContainerBlockEntity> result = new ArrayList<>();
        for (LevelChunk chunk : loadedChunks(level, hold, terminalPos))
            for (BlockEntity be : chunk.getBlockEntities().values())
                if (be instanceof BaseContainerBlockEntity container)
                    result.add(container);
        return result;
    }

    // ==================== 强制加载（区块票） ====================

    /**
     * 为据点的全部绑定区块申请常驻票，使基地在玩家离线/远离时仍能运转。
     * 半径取 0 —— 逐块精确加载，正是「不规则范围」相对旧半径模型的关键收益。
     */
    public static void forceLoadAll(ServerLevel level, Stronghold hold, BlockPos terminalPos) {
        for (ChunkPos cp : chunks(hold, terminalPos)) forceLoad(level, cp);
    }

    /** 释放据点全部绑定区块的常驻票。 */
    public static void releaseAll(ServerLevel level, Stronghold hold, BlockPos terminalPos) {
        for (ChunkPos cp : chunks(hold, terminalPos)) release(level, cp);
    }

    public static void forceLoad(ServerLevel level, ChunkPos chunk) {
        level.getChunkSource().addTicketWithRadius(StrongholdRepository.TICKET, chunk, 0);
    }

    public static void release(ServerLevel level, ChunkPos chunk) {
        level.getChunkSource().removeTicketWithRadius(StrongholdRepository.TICKET, chunk, 0);
    }

    /**
     * 增量同步区块票：只为新增区块申请、只为移除区块释放。
     * 绑定/解绑单个区块时调用，避免整片区块反复卸载再加载。
     */
    public static void syncTickets(ServerLevel level, Stronghold before, Stronghold after, BlockPos terminalPos) {
        Set<ChunkPos> old = chunks(before, terminalPos);
        Set<ChunkPos> now = chunks(after, terminalPos);
        for (ChunkPos cp : now) if (!old.contains(cp)) forceLoad(level, cp);
        for (ChunkPos cp : old) if (!now.contains(cp)) release(level, cp);
    }
}
