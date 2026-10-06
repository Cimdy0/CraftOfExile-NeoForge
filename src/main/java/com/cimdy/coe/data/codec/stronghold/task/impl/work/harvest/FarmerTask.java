package com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.harvest.AgricultureSite;
import com.cimdy.coe.data.codec.support.value.RangeAB;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Optional;
import java.util.function.Predicate;

public class FarmerTask extends WorkTask {
    public static final Codec<FarmerTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, FarmerTask> STREAM_CODEC;
    public static final Predicate<BlockState> BLOCK_PREDICATE = AgricultureSite.SITE_BLOCK_PREDICATE;

    @Override
    public Class<? extends Site> requiredSiteType() {
        return AgricultureSite.class;
    }
    public FarmerTask(BlockPos workPos, int pendingWorkers) {
        this(Optional.empty(), workPos, pendingWorkers);
    }

    public FarmerTask(Optional<Task> next, BlockPos workPos, int pendingWorkers) {
        super(next, workPos, pendingWorkers);
    }

    @Override
    public Predicate<BlockState> getPredicate() {
        return BLOCK_PREDICATE;
    }

    /** 生成任务的条件：工作范围内有成熟作物，且有空余工人 */
    public static Task generateTask(ServerLevel level, Site site) {
        AABB area = site.getWorkArea().map(RangeAB::toAABB).orElse(new AABB(site.getSitePos()));
        if (!hasMatureCrops(level, area)) return null;

        int max = site.getMaxWorkers();
        int current = site.getAssignedWorker();
        int needed = (max <= 0) ? 1 : (max - current);
        if (needed <= 0) return null;

        return new FarmerTask(site.getSitePos(), needed);
    }

    private static boolean hasMatureCrops(ServerLevel level, AABB area) {
        return BlockPos.betweenClosedStream(
                        (int) area.minX, (int) area.minY, (int) area.minZ,
                        (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)
                .anyMatch(pos -> isMatureCrop(level, pos, level.getBlockState(pos)));
    }

    /** 通用成熟判定 */
    public static boolean isMatureCrop(ServerLevel level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();

        // 常规作物 (小麦、胡萝卜、土豆、甜菜等)
        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }

        // 南瓜/西瓜果实
        if (block == Blocks.PUMPKIN || block == Blocks.MELON || block == Blocks.CACTUS_FLOWER) {
            return true;
        }

        // 甘蔗/竹子/仙人掌（只收割第二格及以上部分）
        if (block == Blocks.SUGAR_CANE || block == Blocks.BAMBOO || block == Blocks.CACTUS) {
            return level.getBlockState(pos.below()).is(block);
        }

        // 下界疣
        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= 3;
        }

        // 可可豆
        if (block instanceof CocoaBlock) {
            return state.getValue(CocoaBlock.AGE) >= 2;
        }

        // 发光浆果 (洞穴藤蔓)
        if (block instanceof CaveVinesBlock || block instanceof CaveVinesPlantBlock) {
            return state.getValue(CaveVinesBlock.BERRIES);
        }

        // 甜浆果丛
        if (block instanceof SweetBerryBushBlock) {
            return state.getValue(SweetBerryBushBlock.AGE) >= 2;
        }

        return false;
    }

    @Override public WorkTask createSubTask(int workers) {
        return new FarmerTask(workPos, workers);
    }
    @Override public Type<?> getType() { return TaskTypes.FARMER; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() { return STREAM_CODEC; }
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                BlockPos.CODEC.fieldOf("work_pos").forGetter(FarmerTask::getWorkPos),
                Codec.INT.fieldOf("pending_workers").forGetter(FarmerTask::getPendingWorkers)
        ).apply(instance, FarmerTask::new));
        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), FarmerTask::getOptionNext,
                BlockPos.STREAM_CODEC, FarmerTask::getWorkPos,
                ByteBufCodecs.INT, FarmerTask::getPendingWorkers,
                FarmerTask::new
        );
    }
}