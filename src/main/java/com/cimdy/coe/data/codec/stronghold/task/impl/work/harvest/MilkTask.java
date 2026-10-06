package com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.harvest.MilkSite;
import com.cimdy.coe.data.codec.support.value.RangeAB;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 挤奶任务：在工作区域内为可挤奶的奶牛挤奶，获取奶桶。
 */
public class MilkTask extends WorkTask {
    public static final Codec<MilkTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, MilkTask> STREAM_CODEC;
    public static final Predicate<BlockState> BLOCK_PREDICATE = MilkSite.SITE_BLOCK_PREDICATE;

    @Override
    public Class<? extends Site> requiredSiteType() {
        return MilkSite.class;
    }

    public MilkTask(BlockPos workPos, int pendingWorkers) {
        this(Optional.empty(), workPos, pendingWorkers);
    }
    public MilkTask(Optional<Task> next, BlockPos workPos, int pendingWorkers) {
        super(next, workPos, pendingWorkers);
    }

    @Override
    public Predicate<BlockState> getPredicate() {
        return BLOCK_PREDICATE;
    }

    public static Task generateTask(ServerLevel level, Site site) {
        AABB area = site.getWorkArea().map(RangeAB::toAABB).orElse(new AABB(site.getSitePos()));
        if (!hasCow(level, area)) return null;
        if (!hasBucketAvailable(level, site)) return null;
        int max = site.getMaxWorkers();
        int current = site.getAssignedWorker();
        int needed = (max <= 0) ? 1 : (max - current);
        if (needed <= 0) return null;
        return new MilkTask(site.getSitePos(), needed);
    }

    public static boolean hasCow(ServerLevel level, AABB area) {
        // 获取实体统一经 Context 数据基站回调（EntityQuery 包围盒扫描 + 条件过滤）
        return !ExpressionContext.of(level, null).queryEntities(
                EntityQuery.box(Cow.class, area).filter(c -> c.isAlive() && !c.isBaby())).isEmpty();
    }

    public static boolean hasBucketAvailable(ServerLevel level, Site site) {
        for (BlockPos containerPos : site.getStorages()) {
            if (!level.isLoaded(containerPos)) continue;
            ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, containerPos, null);
            if (handler == null) continue;
            for (int slot = 0; slot < handler.size(); slot++) {
                ItemStack stack = handler.getResource(slot).toStack(1);
                if (stack.is(Items.BUCKET)) return true;
            }
        }
        return false;
    }

    @Override public WorkTask createSubTask(int workers) {
        return new MilkTask(workPos, workers);
    }
    @Override public Type<?> getType() { return TaskTypes.MILK; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() { return STREAM_CODEC; }
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                BlockPos.CODEC.fieldOf("work_pos").forGetter(MilkTask::getWorkPos),
                Codec.INT.fieldOf("pending_workers").forGetter(MilkTask::getPendingWorkers)
        ).apply(instance, MilkTask::new));
        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), MilkTask::getOptionNext,
                BlockPos.STREAM_CODEC, MilkTask::getWorkPos,
                ByteBufCodecs.INT, MilkTask::getPendingWorkers,
                MilkTask::new
        );
    }
}
