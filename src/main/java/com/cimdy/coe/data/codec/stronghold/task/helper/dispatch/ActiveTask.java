package com.cimdy.coe.data.codec.stronghold.task.helper.dispatch;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskStatus;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record ActiveTask(UUID taskId, Task task, TaskStatus status) {
    public static final Codec<ActiveTask> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("task_id").forGetter(ActiveTask::taskId),
            Task.CODEC.fieldOf("task").forGetter(ActiveTask::task),
            TaskStatus.CODEC.fieldOf("status").forGetter(ActiveTask::status)
    ).apply(instance, ActiveTask::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ActiveTask> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ActiveTask::taskId,
            Task.STREAM_CODEC, ActiveTask::task,
            TaskStatus.STREAM_CODEC, ActiveTask::status,
            ActiveTask::new
    );

    public static ActiveTask of(Task task) {
        return new ActiveTask(UUID.randomUUID(), task, TaskStatus.PENDING);
    }

    public static ActiveTask of(Task task, TaskStatus taskStatus) {
        return new ActiveTask(UUID.randomUUID(), task, taskStatus);
    }

    public ActiveTask updateStatus(TaskStatus newStatus) {
        return new ActiveTask(this.taskId, this.task, newStatus);
    }

    public ActiveTask updateTask(Task task) {
        return new ActiveTask(this.taskId, task, this.status);
    }

    @Override
    public String toString() {
        return "ActiveTask[" +
                "taskId=" + taskId + ", " +
                "task=" + task + ", " +
                "status=" + status + ']';
    }

}
