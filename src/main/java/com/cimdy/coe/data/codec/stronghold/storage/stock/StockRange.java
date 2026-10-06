package com.cimdy.coe.data.codec.stronghold.storage.stock;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum StockRange implements StringRepresentable {
    LOCAL("local", 0),
    DIMENSION("dimension", 1),
    GLOBAL("global", 2);

    public static final IntFunction<StockRange> BY_ID =
            ByIdMap.continuous(StockRange::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<StockRange> CODEC =
            StringRepresentable.fromEnum(StockRange::values);
    public static final StreamCodec<ByteBuf, StockRange> STREAM_CODEC =
            ByteBufCodecs.idMapper(BY_ID, StockRange::getId);
    private final String name;
    private final int id;

    StockRange(String name, int value) {
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
        return Component.translatable("hold.stock.coe.range."
                + this.name).withStyle(ChatFormatting.WHITE);
    }
}
