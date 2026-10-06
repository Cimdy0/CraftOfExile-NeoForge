package com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.harvest.HuntSite;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 狩猎任务：在工作区域内狩猎可掉落物的被动动物（牛、猪、鸡、羊、兔、山羊）。
 */
public class HuntTask extends WorkTask {
    public static final Codec<HuntTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, HuntTask> STREAM_CODEC;
    public static final Predicate<BlockState> BLOCK_PREDICATE = HuntSite.SITE_BLOCK_PREDICATE;

    @Override
    public Class<? extends Site> requiredSiteType() {
        return HuntSite.class;
    }

    public HuntTask(BlockPos workPos, int pendingWorkers) {
        this(Optional.empty(), workPos, pendingWorkers);
    }
    public HuntTask(Optional<Task> next, BlockPos workPos, int pendingWorkers) {
        super(next, workPos, pendingWorkers);
    }

    @Override
    public Predicate<BlockState> getPredicate() {
        return BLOCK_PREDICATE;
    }

    public static Task generateTask(ServerLevel level, Site site) {
        AABB area = site.getWorkArea().map(RangeAB::toAABB).orElse(new AABB(site.getSitePos()));
        if (!hasHuntable(level, area)) return null;
        int max = site.getMaxWorkers();
        int current = site.getAssignedWorker();
        int needed = (max <= 0) ? 1 : (max - current);
        if (needed <= 0) return null;
        return new HuntTask(site.getSitePos(), needed);
    }

    public static boolean isHuntable(Animal animal) {
        return animal instanceof Cow
                || animal instanceof Pig
                || animal instanceof Chicken
                || animal instanceof Sheep
                || animal instanceof Rabbit
                || animal instanceof Goat;
    }

    public static boolean hasHuntable(ServerLevel level, AABB area) {
        // 获取实体统一经 Context 数据基站回调（EntityQuery 包围盒扫描 + 条件过滤）
        return !ExpressionContext.of(level, null).queryEntities(
                EntityQuery.box(Animal.class, area)
                        .filter(a -> isHuntable(a) && a.isAlive() && !a.isBaby())).isEmpty();
    }

    @Override public WorkTask createSubTask(int workers) {
        return new HuntTask(workPos, workers);
    }
    @Override public Type<?> getType() { return TaskTypes.HUNT; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() { return STREAM_CODEC; }
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                BlockPos.CODEC.fieldOf("work_pos").forGetter(HuntTask::getWorkPos),
                Codec.INT.fieldOf("pending_workers").forGetter(HuntTask::getPendingWorkers)
        ).apply(instance, HuntTask::new));
        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), HuntTask::getOptionNext,
                BlockPos.STREAM_CODEC, HuntTask::getWorkPos,
                ByteBufCodecs.INT, HuntTask::getPendingWorkers,
                HuntTask::new
        );
    }
}
