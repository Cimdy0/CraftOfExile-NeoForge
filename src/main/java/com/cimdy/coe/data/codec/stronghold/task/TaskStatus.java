package com.cimdy.coe.data.codec.stronghold.task;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum TaskStatus implements StringRepresentable {
    PENDING("pending", 0),
    ASSIGNED("assigned", 1),
    IN_PROGRESS("in_progress", 2),
    COMPLETED("completed", 3),
    FAILED("failed", 4);

    public static final IntFunction<TaskStatus> BY_ID =
            ByIdMap.continuous(TaskStatus::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<TaskStatus> CODEC =
            StringRepresentable.fromEnum(TaskStatus::values);
    public static final StreamCodec<ByteBuf, TaskStatus> STREAM_CODEC =
            ByteBufCodecs.idMapper(BY_ID, TaskStatus::getId);
    private final String name;
    private final int id;

    TaskStatus(String name, int value) {
        this.name = name;
        this.id = value;
    }

    public int getId() {
        return this.id;
    }

    public String getSerializedName() {
        return this.name;
    }

    public Component getComponentName() {
        return Component.translatable("task.coe.status."
                + this.name).withStyle(ChatFormatting.WHITE);
    }
}
