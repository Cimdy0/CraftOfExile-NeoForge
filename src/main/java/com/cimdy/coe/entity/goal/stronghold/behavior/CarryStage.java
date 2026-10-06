package com.cimdy.coe.entity.goal.stronghold.behavior;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum CarryStage implements StringRepresentable {
    GO_TO_SOURCE("go_to_source", 0),
    GO_TO_TARGET("go_to_target", 1),
    GO_TO_OTHER_TERMINAL("go_to_other_terminal", 2),
    RETURN_OTHER_TERMINAL("return_other_terminal", 3);  // 新增返回阶段

    private final String name;
    private final int id;

    CarryStage(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public static final IntFunction<CarryStage> BY_ID =
            ByIdMap.continuous(CarryStage::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<CarryStage> CODEC =
            StringRepresentable.fromEnum(CarryStage::values);
    public static final StreamCodec<ByteBuf, CarryStage> STREAM_CODEC =
            ByteBufCodecs.idMapper(BY_ID, CarryStage::getId);

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }
}