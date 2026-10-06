package com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.cimdy.coe.data.codec.stronghold.task.helper.demand.DemandInfo;
import com.cimdy.coe.data.codec.stronghold.task.helper.demand.DemandLedger;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 转化任务（合成链中的「转化」节点，与 {@link com.cimdy.coe.data.codec.stronghold.task.impl.CraftTask} 同构）。
 * <p>
 * 不作为独立工种「原地取料→转化→存回」，而是作为 DAG 链的一环：
 * 前驱 {@code CarryTask} 把原料搬运到站点存储，本任务在站点把原料转为产物（进度 {@code produced} 持久化于任务数据），
 * 后继 {@code CarryTask} 把产物搬往需求方。这样烹饪 / 冶炼 / 堆肥 / 研磨与合成走同一条
 * 「需求驱动 → 生成链 → 搬运/转化/搬运」管线。
 * <p>
 * 因处于链中，本任务不参与 pending 合并 / 拆分（否则会破坏 carry→convert→carry 链路），
 * 故 {@link #getGroupingKey()} 返回空、{@link #isSplittable()} 为 false（与 CraftTask 一致）。
 */
public class ConvertTask extends WorkTask {
    public static final Codec<ConvertTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, ConvertTask> STREAM_CODEC;

    private final ConvertType convertType;
    private final ItemResource raw;
    private final ItemResource product;
    /** 本任务目标转化数量（由生成时按需求缺口确定）。 */
    private final int amount;

    public ConvertTask(ConvertType convertType, BlockPos workPos, ItemResource raw, ItemResource product, int amount) {
        this(Optional.empty(), convertType, workPos, raw, product, amount, 0);
    }

    public ConvertTask(Optional<Task> next, ConvertType convertType, BlockPos workPos,
                       ItemResource raw, ItemResource product, int amount, int produced) {
        super(next, workPos, 1);
        this.convertType = convertType;
        this.raw = raw;
        this.product = product;
        this.amount = amount;
        this.setProduced(produced);
    }

    @Override
    public Class<? extends Site> requiredSiteType() {
        return convertType.getSiteType();
    }

    @Override
    public boolean matchesSite(Site site) {
        return convertType.matchesSite(site);
    }

    @Override
    public Predicate<BlockState> getPredicate() {
        return convertType.getBlockPredicate();
    }

    public ConvertType getConvertType() { return convertType; }
    public ItemResource getRaw() { return raw; }
    public ItemResource getProduct() { return product; }
    public int getAmount() { return amount; }

    @Override
    public WorkTask createSubTask(int workers) {
        return new ConvertTask(convertType, workPos, raw, product, amount);
    }

    // ---------- 链节点约定：不参与合并 / 拆分 ----------
    @Override public boolean isSplittable() { return false; }
    @Override public Task split(int amount) { return this; }
    @Override public Optional<?> getGroupingKey() { return Optional.empty(); }
    @Override public int getVolume() { return amount; }

    @Override
    public boolean validate(ServerLevel level) {
        if (!super.validate(level)) return false;
        // 产物需求消失则失效，避免无需求时继续转化导致过剩
        if (!hasOutputDemand(DemandLedger.getSiteDemands(getWorkPos()))) return false;
        return true;
    }

    /** 是否存在对产物的需求（供需求门控）。 */
    public boolean hasOutputDemand(List<DemandInfo> demands) {
        for (DemandInfo d : demands) {
            if (d.resource().equals(product)) return true;
        }
        return false;
    }

    @Override public Type<?> getType() { return TaskTypes.CONVERT; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() { return STREAM_CODEC; }

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                Codec.STRING.xmap(ConvertType::byName, ConvertType::getName)
                        .fieldOf("convert_type").forGetter(ConvertTask::getConvertType),
                BlockPos.CODEC.fieldOf("work_pos").forGetter(ConvertTask::getWorkPos),
                ItemResource.CODEC.fieldOf("raw").forGetter(ConvertTask::getRaw),
                ItemResource.CODEC.fieldOf("product").forGetter(ConvertTask::getProduct),
                Codec.INT.fieldOf("amount").forGetter(ConvertTask::getAmount),
                Codec.INT.optionalFieldOf("produced", 0).forGetter(ConvertTask::getProduced)
        ).apply(instance, (next, ct, workPos, raw, product, amount, produced) ->
                new ConvertTask(next, ct, workPos, raw, product, amount, produced)));

        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), ConvertTask::getOptionNext,
                ByteBufCodecs.STRING_UTF8.map(ConvertType::byName, ConvertType::getName), ConvertTask::getConvertType,
                BlockPos.STREAM_CODEC, ConvertTask::getWorkPos,
                ItemResource.STREAM_CODEC, ConvertTask::getRaw,
                ItemResource.STREAM_CODEC, ConvertTask::getProduct,
                ByteBufCodecs.INT, ConvertTask::getAmount,
                ByteBufCodecs.INT, ConvertTask::getProduced,
                ConvertTask::new
        );
    }
}
