package com.cimdy.coe.data.codec.stronghold.task.impl;

import com.cimdy.coe.data.codec.stronghold.ItemCount;
import com.cimdy.coe.data.codec.stronghold.task.BasicTask;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftTask extends BasicTask {
    public static final Codec<CraftTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftTask> STREAM_CODEC;
    private final List<ItemCount> items;
    private final ResourceKey<Recipe<?>> recipe;
    private final ItemPredicate toolPredicate;
    private final BlockPos site;
    private final int times;
    private int remainingTimes;
    /** 并行后继（方案 C：DAG 分支）。产品搬运等可并行分发的任务放在这里，与单后继 next 并存。 */
    private final List<Task> nexts;

    public CraftTask(List<ItemCount> items, ResourceKey<Recipe<?>> recipe, BlockPos site, int times) {
        this(Optional.empty(), items, recipe, ItemPredicate.Builder.item().build(), site, times, 0, List.of());
    }

    /** 便捷构造：指定并行后继（产品搬运等）。 */
    public CraftTask(List<ItemCount> items, ResourceKey<Recipe<?>> recipe, BlockPos site, int times, List<Task> nexts) {
        this(Optional.empty(), items, recipe, ItemPredicate.Builder.item().build(), site, times, 0, nexts);
    }

    public CraftTask(Optional<Task> next, List<ItemCount> items, ResourceKey<Recipe<?>> recipe, ItemPredicate toolPredicate,
                     BlockPos site, int times, int remainingTimes, List<Task> nexts) {
        super(next);
        this.items = items;
        this.recipe = recipe;
        this.toolPredicate = toolPredicate;
        this.site = site;
        this.times = times;
        this.remainingTimes = remainingTimes;
        this.nexts = nexts;
    }

    public List<Task> getNexts() {
        return nexts;
    }

    /**
     * 方案 C：返回所有后继（并行分支 + 单后继）。
     * 使合成任务成为 DAG 节点——产品搬运可并行分发，调度器 advanceTaskChain 已支持多后继。
     */
    @Override
    public List<Task> getNextTasks() {
        List<Task> result = new ArrayList<>(nexts);
        getOptionNext().ifPresent(result::add);
        return result;
    }

    public ResourceKey<Recipe<?>> getRecipe() { return recipe; }
    public BlockPos getSite() {
        return site;
    }
    public int getTimes() {
        return times;
    }
    public int getRemainingTimes() { return remainingTimes; }
    public List<ItemCount> getItems() { return items; }
    public ItemPredicate getToolPredicate() { return toolPredicate; }
    public void addRemainingTime() {
        this.remainingTimes += 1;
    }
    public boolean isCompleted() {
        return remainingTimes >= times;
    }
    @Override public boolean validate(ServerLevel level) {
        return true;
    }
    @Override public Type<?> getType() {
        return TaskTypes.CRAFT;
    }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() {
        return STREAM_CODEC;
    }
    @Override public BlockPos getTargetPositionForEntity(LivingEntity entity) {
        return this.site;
    }
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                ItemCount.CODEC.listOf().fieldOf("items").forGetter(CraftTask::getItems),
                ResourceKey.codec(Registries.RECIPE).fieldOf("recipe").forGetter(CraftTask::getRecipe),
                ItemPredicate.CODEC.fieldOf("tool_predicate").forGetter(CraftTask::getToolPredicate),
                BlockPos.CODEC.fieldOf("site").forGetter(CraftTask::getSite),
                Codec.INT.fieldOf("times").forGetter(CraftTask::getTimes),
                Codec.INT.optionalFieldOf("remaining_times", 0).forGetter(CraftTask::getRemainingTimes),
                Codec.lazyInitialized(() -> Task.CODEC).listOf().optionalFieldOf("nexts", List.of())
                        .forGetter(CraftTask::getNexts)
        ).apply(instance, CraftTask::new));

        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), CraftTask::getOptionNext,
                ItemCount.STREAM_CODEC.apply(ByteBufCodecs.list()), CraftTask::getItems,
                ResourceKey.streamCodec(Registries.RECIPE), CraftTask::getRecipe,
                ByteBufCodecs.fromCodec(ItemPredicate.CODEC), CraftTask::getToolPredicate,
                BlockPos.STREAM_CODEC, CraftTask::getSite,
                ByteBufCodecs.INT, CraftTask::getTimes,
                ByteBufCodecs.INT, CraftTask::getRemainingTimes,
                Task.STREAM_CODEC.apply(ByteBufCodecs.list()), CraftTask::getNexts,
                CraftTask::new
        );
    }
}
