package com.cimdy.coe.data.codec.stronghold;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum StrongholdWorkMode implements StringRepresentable {
    FOCUS_WORK("focus_work", 0),
    DEFEND_ENEMY("defend_enemy", 1);

    private final String mode;
    private final int id;

    StrongholdWorkMode(String mode, int id) {
        this.mode = mode;
        this.id = id;
    }

    public static final IntFunction<StrongholdWorkMode> BY_ID =
            ByIdMap.continuous(StrongholdWorkMode::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<StrongholdWorkMode> CODEC =
            StringRepresentable.fromEnum(StrongholdWorkMode::values);
    public static final StreamCodec<ByteBuf, StrongholdWorkMode> STREAM_CODEC =
            ByteBufCodecs.idMapper(BY_ID, StrongholdWorkMode::getId);

    public String getSerializedName() {
        return this.mode;
    }

    public int getId() {
        return id;
    }
}
