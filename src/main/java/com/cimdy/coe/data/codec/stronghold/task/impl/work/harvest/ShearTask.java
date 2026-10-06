package com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.harvest.ShearSite;
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
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 牧养任务：在工作区域内为可剪毛的绵羊剪毛。
 */
public class ShearTask extends WorkTask {
    public static final Codec<ShearTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, ShearTask> STREAM_CODEC;
    public static final Predicate<BlockState> BLOCK_PREDICATE = ShearSite.SITE_BLOCK_PREDICATE;

    @Override
    public Class<? extends Site> requiredSiteType() {
        return ShearSite.class;
    }

    public ShearTask(BlockPos workPos, int pendingWorkers) {
        this(Optional.empty(), workPos, pendingWorkers);
    }
    public ShearTask(Optional<Task> next, BlockPos workPos, int pendingWorkers) {
        super(next, workPos, pendingWorkers);
    }

    @Override
    public Predicate<BlockState> getPredicate() {
        return BLOCK_PREDICATE;
    }

    public static Task generateTask(ServerLevel level, Site site) {
        AABB area = site.getWorkArea().map(RangeAB::toAABB).orElse(new AABB(site.getSitePos()));
        if (!hasShearableSheep(level, area)) return null;
        if (!hasShearsAvailable(level, site)) return null;
        int max = site.getMaxWorkers();
        int current = site.getAssignedWorker();
        int needed = (max <= 0) ? 1 : (max - current);
        if (needed <= 0) return null;
        return new ShearTask(site.getSitePos(), needed);
    }

    public static boolean hasShearableSheep(ServerLevel level, AABB area) {
        // 获取实体统一经 Context 数据基站回调（EntityQuery 包围盒扫描 + 条件过滤）
        return !ExpressionContext.of(level, null).queryEntities(
                EntityQuery.box(Sheep.class, area).filter(Sheep::readyForShearing)).isEmpty();
    }

    public static boolean hasShearsAvailable(ServerLevel level, Site site) {
        for (BlockPos containerPos : site.getStorages()) {
            if (!level.isLoaded(containerPos)) continue;
            ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, containerPos, null);
            if (handler == null) continue;
            for (int slot = 0; slot < handler.size(); slot++) {
                ItemResource resource = handler.getResource(slot);
                if (resource.toStack(1).is(net.neoforged.neoforge.common.Tags.Items.TOOLS_SHEAR)) return true;
            }
        }
        return false;
    }

    @Override public WorkTask createSubTask(int workers) {
        return new ShearTask(workPos, workers);
    }
    @Override public Type<?> getType() { return TaskTypes.SHEAR; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() { return STREAM_CODEC; }
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                BlockPos.CODEC.fieldOf("work_pos").forGetter(ShearTask::getWorkPos),
                Codec.INT.fieldOf("pending_workers").forGetter(ShearTask::getPendingWorkers)
        ).apply(instance, ShearTask::new));
        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), ShearTask::getOptionNext,
                BlockPos.STREAM_CODEC, ShearTask::getWorkPos,
                ByteBufCodecs.INT, ShearTask::getPendingWorkers,
                ShearTask::new
        );
    }
}
