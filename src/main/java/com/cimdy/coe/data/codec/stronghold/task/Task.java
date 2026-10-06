package com.cimdy.coe.data.codec.stronghold.task;

import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface Task {
    @Nullable Task getNext();
    void setNext(Task carry);

    /**
     * 该任务完成后的后继任务（可多个，支持 DAG 并行分支）。
     * 默认返回单一 {@code next}（兼容线性链）；分支任务重写此方法返回多个后继即可接入 DAG。
     */
    default List<Task> getNextTasks() {
        Task next = getNext();
        return next == null ? List.of() : List.of(next);
    }

    /** 任务开始执行时的回调 */
    default void onStart(ServerLevel level) {}

    /** 任务结束或取消时的回调 */
    default void onStop(ServerLevel level) {}

    /** 任务分配给工人时的回调 */
    default void onWorkerAssigned(ServerLevel level, GlobalPos terminalPos, UUID entityId, UUID taskId) {}

    /** Goal任务数据更新返回到终端 */
    default void updateTaskAtTerminal(ServerLevel level, GlobalPos terminalPos, UUID entityId, UUID taskId) {
        if (terminalPos == null) return;
        StrongholdHelper.updateHoldAtPos(level, terminalPos, hold -> hold.updateTask(entityId, taskId, this));
    }

    default void setTaskFailedAtTerminal(ServerLevel serverLevel, GlobalPos terminalPos, UUID uuid, UUID taskId) {
        if (terminalPos == null) return;
        StrongholdHelper.updateHoldAtPos(serverLevel, terminalPos, hold -> hold.setTaskFailed(uuid, taskId));
    }

    /** Goal任务结束返回到终端 */
    default void setTaskCompletedAtTerminal(ServerLevel level, GlobalPos terminalPos, UUID entityId, UUID taskId) {
        if (terminalPos == null) return;
        StrongholdHelper.updateHoldAtPos(level, terminalPos, hold -> hold.setTaskCompleted(entityId, taskId));
    }

    /** 获取此任务的“工作量”，例如搬运物品的数量。非搬运类任务可根据含义重写，默认返回0。 */
    default int getVolume() {
        return 0;
    }

    /** 获取执行此任务所需的最小批量。如搬运的 minTransfer，默认返回1。 */
    default int getMinTransfer(){
        return 1;
    }

    /** 尝试将此任务拆分为一个能完成指定量工作的子任务。不可拆分的任务返回自身或 null。 */
    default Task split(int amount) {
        return this;
    }

    /** 获取此任务对于给定实体而言的“目标位置”，用于计算距离和路径规划。 */
     BlockPos getTargetPositionForEntity(LivingEntity entity);

    /** 获取用于合并相同逻辑任务的唯一键。如果任务不支持合并，返回 Optional.empty()。 */
    default Optional<?> getGroupingKey() {
        return Optional.empty();
    }

    /** 验证任务当前是否仍然有效（例如源容器库存足够）。 */
    boolean validate(ServerLevel level);

    /** 判断此任务是否能分配给指定实体。 */
    default boolean canBeAssignedTo(LivingEntity entity) {
        return StrongholdHelper.getHunger(entity) > 0 && StrongholdHelper.getSan(entity) > 0;
    }

    /** 是否可被拆分。 */
    default boolean isSplittable() { return false; }

    default double getHungerCostPerTick() {
        return 0.01;
    }

    default boolean canFallback() {
        return true;
    }

    StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec();
    Task.Type<?> getType();
    record Type<T extends Task>(Codec<T> codec) {}

    // 注意：必须在 MAP_CODEC 之前初始化，因为 MAP_CODEC 会触发 TaskTypes 类加载，
    // 而 TaskTypes 注册具体任务类（如 CraftTask）时其静态块会引用 Task.STREAM_CODEC。
    StreamCodec<RegistryFriendlyByteBuf, Task> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Task decode(RegistryFriendlyByteBuf buf) {
            Identifier id = Identifier.STREAM_CODEC.decode(buf);
            StreamCodec<RegistryFriendlyByteBuf, ? extends Task> codec = TaskTypes.STREAM_CODECS.get(id);
            if (codec == null) {
                throw new IllegalArgumentException("Unknown task type: " + id);
            }
            return codec.decode(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, Task task) {
            // 从 Task.Type 获取 ResourceLocation（需要反向映射）
            Identifier id = TaskTypes.TYPE_TO_ID.get(task.getType());
            if (id == null) {
                throw new IllegalArgumentException("Unregistered task type: " + task.getType());
            }
            Identifier.STREAM_CODEC.encode(buf, id);
            @SuppressWarnings("unchecked")
            StreamCodec<RegistryFriendlyByteBuf, Task> codec = (StreamCodec<RegistryFriendlyByteBuf, Task>) TaskTypes.getStreamCodec(task.getType());
            codec.encode(buf, task);
        }
    };

    Codec<Map<Type<?>, Task>> MAP_CODEC = Codec.dispatchedMap(
            TaskTypes.REGISTRY.byNameCodec(),
            Type::codec);

    Codec<Task> CODEC = MAP_CODEC.flatXmap(
            map -> {
                if (map.size() != 1) {
                    return DataResult.error(() -> "表达式必须为单键结构");
                }
                return DataResult.success(map.values().iterator().next());
            },
            expr -> {
                Task.Type<?> type = expr.getType();
                return DataResult.success(ImmutableMap.of(type, expr));
            }
    );
}
