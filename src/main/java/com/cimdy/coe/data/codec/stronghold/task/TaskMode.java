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

public enum TaskMode implements StringRepresentable {
    SINGLE_CHAIN("single_chain", 0),
    MULTI_SUPPLY_CHAIN("multi_supply_chain", 1),
    BALANCED("balanced", 2),
    NEAREST_FIRST("nearest_first", 3);

    public static final IntFunction<TaskMode> BY_ID =
            ByIdMap.continuous(TaskMode::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<TaskMode> CODEC =
            StringRepresentable.fromEnum(TaskMode::values);
    public static final StreamCodec<ByteBuf, TaskMode> STREAM_CODEC =
            ByteBufCodecs.idMapper(BY_ID, TaskMode::getId);
    private final String name;
    private final int id;

    TaskMode(String name, int value) {
        this.name = name;
        this.id = value;
    }

    public int getId() { return this.id; }
    public String getSerializedName() { return this.name; }

    public Component getComponentName() {
        return Component.translatable("task.coe.mode." + this.name)
                .withStyle(ChatFormatting.WHITE);
    }
}