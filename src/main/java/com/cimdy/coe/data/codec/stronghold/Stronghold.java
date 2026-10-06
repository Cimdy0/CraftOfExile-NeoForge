package com.cimdy.coe.data.codec.stronghold;

import com.cimdy.coe.stronghold.area.StrongholdArea;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskMode;
import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.ActiveTask;
import com.cimdy.coe.data.codec.stronghold.task.helper.demand.DemandLedger;
import com.cimdy.coe.data.codec.stronghold.task.helper.state.StrongholdState;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.SiteManager;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 据点数据（存于终端方块实体的附件）。
 *
 * <p><b>范围模型</b>：据点范围<b>不是</b>以终端为中心的半径，而是玩家通过建设（界桩方块）
 * 逐块绑定出来的<b>不规则区块集合</b> {@link #boundChunks()}。终端所在区块恒属于据点
 * （见 {@link #chunksWithCenter(net.minecraft.core.BlockPos)}），因此新建据点即可运转。
 * 统一判定与强制加载入口见 {@code stronghold.area.StrongholdArea}。
 */
public record Stronghold(
        int level, StrongholdWorkMode workMode, TaskMode taskMode,
        List<UUID> entities, StrongholdState holdState, Set<ChunkPos> boundChunks
) {
    public static final Stronghold EMPTY = of(0);

    /** 区块集合的持久化编解码：以 long 打包，兼容旧存档（缺字段 = 空集合 = 仅中心区块）。 */
    public static final Codec<Set<ChunkPos>> CHUNKS_CODEC = Codec.LONG.listOf().xmap(
            list -> list.stream().map(ChunkPos::unpack).collect(Collectors.toCollection(LinkedHashSet::new)),
            set -> set.stream().map(c -> ChunkPos.pack(c.x(), c.z())).toList());

    public static final StreamCodec<ByteBuf, Set<ChunkPos>> CHUNKS_STREAM_CODEC =
            ByteBufCodecs.VAR_LONG.map(ChunkPos::unpack, c -> ChunkPos.pack(c.x(), c.z()))
                    .apply(ByteBufCodecs.<ByteBuf, ChunkPos, Set<ChunkPos>>collection(LinkedHashSet::new));

    public static final Codec<Stronghold> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(Stronghold::level),
            StrongholdWorkMode.CODEC.fieldOf("work_mode").forGetter(Stronghold::workMode),
            TaskMode.CODEC.fieldOf("task_mode").forGetter(Stronghold::taskMode),
            UUIDUtil.CODEC.listOf().fieldOf("entities").forGetter(Stronghold::entities),
            StrongholdState.CODEC.fieldOf("transfer").forGetter(Stronghold::holdState),
            CHUNKS_CODEC.optionalFieldOf("bound_chunks", Set.of()).forGetter(Stronghold::boundChunks)
    ).apply(instance, Stronghold::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Stronghold> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, Stronghold::level,
            StrongholdWorkMode.STREAM_CODEC, Stronghold::workMode,
            TaskMode.STREAM_CODEC, Stronghold::taskMode,
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list()), Stronghold::entities,
            StrongholdState.STREAM_CODEC, Stronghold::holdState,
            CHUNKS_STREAM_CODEC, Stronghold::boundChunks,
            Stronghold::new
    );

    public static Stronghold of(int level) {
        return new Stronghold(level, StrongholdWorkMode.DEFEND_ENEMY, TaskMode.NEAREST_FIRST, List.of(), StrongholdState.EMPTY, Set.of());
    }

    // ======================== 区块绑定（不规则范围） ========================

    /** 终端所在区块恒属于据点，故对外暴露「绑定集合 ∪ 中心区块」。 */
    public Set<ChunkPos> chunksWithCenter(BlockPos terminalPos) {
        Set<ChunkPos> all = new LinkedHashSet<>(boundChunks);
        all.add(new ChunkPos(terminalPos.getX() >> 4, terminalPos.getZ() >> 4));
        return all;
    }

    /** 该区块是否属于本据点（含中心区块）。 */
    public boolean isChunkBound(BlockPos terminalPos, ChunkPos chunk) {
        return boundChunks.contains(chunk) || new ChunkPos(terminalPos.getX() >> 4, terminalPos.getZ() >> 4).equals(chunk);
    }

    public Stronghold withBoundChunks(Set<ChunkPos> chunks) {
        return new Stronghold(level, workMode, taskMode, entities, holdState, chunks);
    }

    public Stronghold withChunkBound(ChunkPos chunk) {
        Set<ChunkPos> next = new LinkedHashSet<>(boundChunks);
        next.add(chunk);
        return withBoundChunks(next);
    }

    public Stronghold withChunkUnbound(ChunkPos chunk) {
        Set<ChunkPos> next = new LinkedHashSet<>(boundChunks);
        next.remove(chunk);
        return withBoundChunks(next);
    }

    // ======================== 不可变更新 ========================
    public Stronghold withHoldState(StrongholdState state) {
        return new Stronghold(level, workMode, taskMode, entities, state, boundChunks);
    }

    public Stronghold withLevel(int newLevel) {
        return new Stronghold(newLevel, workMode, taskMode, entities, holdState, boundChunks);
    }

    public Stronghold withEntityAdded(UUID uuid) {
        List<UUID> newList = new ArrayList<>(entities);
        newList.add(uuid);
        return new Stronghold(level, workMode, taskMode, newList, holdState, boundChunks);
    }

    public Stronghold withEntityRemoved(UUID uuid) {
        List<UUID> newList = new ArrayList<>(entities);
        newList.remove(uuid);
        return new Stronghold(level, workMode, taskMode, newList, holdState, boundChunks);
    }

    public Stronghold updateTask(UUID entityId, UUID taskId, Task task) {
        return withHoldState(holdState.updateTask(entityId, taskId, task));
    }

    public Stronghold setTaskFailed(UUID uuid, UUID taskId) {
        return withHoldState(holdState.setTaskFailed(uuid, taskId));
    }

    public Stronghold setTaskCompleted(UUID entityId, UUID taskId) {
        return withHoldState(holdState.setTaskCompleted(entityId, taskId));
    }

    public Stronghold updateEntity(ServerLevel serverLevel) {
        List<UUID> newList = new ArrayList<>(entities);
        newList.removeIf(uuid -> !(StrongholdHelper.findLivingEntity(serverLevel, uuid)
                instanceof LivingEntity living && living.isAlive()));
        return new Stronghold(level, workMode, taskMode, newList, holdState, boundChunks);
    }

    // 查询站点（方便外部使用）
    public Optional<Site> getSite(BlockPos workBlock) {
        return holdState.getSite(workBlock);
    }

    // 添加/更新站点
    public Stronghold withSite(Site site) {
        return withHoldState(holdState.withSite(site));
    }

    // 移除站点
    public Stronghold withoutSite(BlockPos workBlock) {
        return withHoldState(holdState.withoutSite(workBlock));
    }

    // 更新站点（函数式更新）
    public Stronghold updateSite(BlockPos workBlock, java.util.function.UnaryOperator<Site> updater) {
        return withHoldState(holdState.updateSite(workBlock, updater));
    }

    /**
     * 遍历所有站点执行平衡操作，并清理无主待处理任务，返回更新后的 Stronghold。
     * 方案 B 延伸：仅重算「脏站点」或全量兜底 tick 的站点，避免每轮扫描全量重算（病灶 2）。
     *
     * @param terminalPos 本终端的全局坐标，用于站点级脏标记定位
     */
    public Stronghold balanceAllSites(ServerLevel level, GlobalPos terminalPos) {
        Stronghold current = this;
        boolean forceAll = DemandLedger.isFullSiteScanTick();

        // 1. 仅对脏站点（或全量兜底 tick）执行常规平衡（检查站点有效性、平衡工人数等）
        for (Site site : current.holdState.sites()) {
            BlockPos sitePos = site.getSitePos();
            if (forceAll || DemandLedger.isSiteDirty(terminalPos, sitePos)) {
                current = SiteManager.balanceWorkers(level, current, site);
                DemandLedger.markSiteClean(terminalPos, sitePos);
            }
        }

        // 2. 清理所有工作方块已无效的待处理工作任务（防止站点被移除后残留，全量轻量校验）
        StrongholdState state = current.holdState();
        List<ActiveTask> newPending = new ArrayList<>(state.pendingTasks());
        boolean changed = false;

        Iterator<ActiveTask> iterator = newPending.iterator();
        while (iterator.hasNext()) {
            ActiveTask task = iterator.next();
            if (task.task() instanceof WorkTask workTask) {
                BlockPos sitePos = workTask.getWorkPos();
                // 检查该 sitePos 对应的站点是否仍然存在
                if (state.getSite(sitePos).isEmpty()) {
                    // 站点已消失，移除该任务
                    iterator.remove();
                    changed = true;
                }
            }
        }

        if (changed) {
            current = current.withHoldState(state.withPendingTasks(newPending));
        }

        return current;
    }

    public boolean hasTask() {
        return !holdState.pendingTasks().isEmpty() || !holdState.activeTasks().isEmpty();
    }
}