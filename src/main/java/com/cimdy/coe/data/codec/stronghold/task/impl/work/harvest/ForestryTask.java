package com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.harvest.ForestrySite;
import com.cimdy.coe.data.codec.support.value.RangeAB;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Optional;
import java.util.function.Predicate;

public class ForestryTask extends WorkTask {
    public static final Codec<ForestryTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, ForestryTask> STREAM_CODEC;
    public static final Predicate<BlockState> BLOCK_PREDICATE = ForestrySite.SITE_BLOCK_PREDICATE;

    @Override
    public Class<? extends Site> requiredSiteType() {
        return ForestrySite.class;
    }
    public ForestryTask(BlockPos workPos, int pendingWorkers) {
        this(Optional.empty(), workPos, pendingWorkers);
    }
    public ForestryTask(Optional<Task> next, BlockPos workPos, int pendingWorkers) {
        super(next, workPos, pendingWorkers);
    }

    @Override
    public Predicate<BlockState> getPredicate() {
        return BLOCK_PREDICATE;
    }

    /**
     * 生成伐木任务的条件：
     * 1. 工作范围内有可砍伐的树木（原木或蘑菇梗）
     * 2. 站点容器中有斧头可用
     * 3. 有空余工人
     */
    public static Task generateTask(ServerLevel level, Site site) {
        // 检查是否有可砍伐的目标
        AABB area = site.getWorkArea().map(RangeAB::toAABB).orElse(new AABB(site.getSitePos()));
        if (!hasLogsOrMushrooms(level, area)) return null;
        // 检查站点容器是否提供斧头
        if (!hasAxeAvailable(level, site)) return null;
        int max = site.getMaxWorkers();
        int current = site.getAssignedWorker();
        int needed = (max <= 0) ? 1 : (max - current);  // 无限制时至少生成1个工人
        if (needed <= 0) return null;

        return new ForestryTask(site.getSitePos(), needed);
    }

    /** 检查区域内是否存在可砍伐的树木（原木、蘑菇梗） */
    public static boolean hasLogsOrMushrooms(ServerLevel level, AABB area) {
        return BlockPos.betweenClosedStream(
                        (int) area.minX, (int) area.minY, (int) area.minZ,
                        (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)
                .anyMatch(pos -> isMatureWood(level, pos, level.getBlockState(pos)));
    }

    /** 判定目标是否为可砍伐的成熟树木方块（原木、蘑菇梗） */
    public static boolean isMatureWood(ServerLevel level, BlockPos pos, BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(Blocks.MUSHROOM_STEM);
    }

    /** 检查站点所有存储容器中是否存在斧头 */
    public static boolean hasAxeAvailable(ServerLevel level, Site site) {
        for (BlockPos containerPos : site.getStorages()) {
            if (!level.isLoaded(containerPos)) continue;
            ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, containerPos, null);
            if (handler == null) continue;
            for (int slot = 0; slot < handler.size(); slot++) {
                ItemResource resource = handler.getResource(slot);
                ItemStack stack = resource.toStack(1);
                // 使用原版斧头标签判断任意斧头
                if (stack.is(ItemTags.AXES)) return true;
            }
        }
        return false;
    }

    @Override public WorkTask createSubTask(int workers) {
        return new ForestryTask(workPos, workers);
    }
    @Override public Type<?> getType() { return TaskTypes.LOGGER; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() { return STREAM_CODEC; }
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                BlockPos.CODEC.fieldOf("work_pos").forGetter(ForestryTask::getWorkPos),
                Codec.INT.fieldOf("pending_workers").forGetter(ForestryTask::getPendingWorkers  )
        ).apply(instance, ForestryTask::new));
        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), ForestryTask::getOptionNext,
                BlockPos.STREAM_CODEC, ForestryTask::getWorkPos,
                ByteBufCodecs.INT, ForestryTask::getPendingWorkers,
                ForestryTask::new
        );
    }
}