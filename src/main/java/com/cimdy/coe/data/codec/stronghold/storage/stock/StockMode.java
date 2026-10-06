package com.cimdy.coe.data.codec.stronghold.storage.stock;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum StockMode implements StringRepresentable {
    SUPPLY("supply", 0x400000FF, 0),
    DEMAND("demand", 0x40FFFF00, 1),
    STORAGE("storage", 0x40808080, 2);
    public static final IntFunction<StockMode> BY_ID =
            ByIdMap.continuous(StockMode::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<StockMode> CODEC =
            StringRepresentable.fromEnum(StockMode::values);
    private final String name;
    private final int color;
    private final int id;

    StockMode(String name, int color, int value) {
        this.name = name;
        this.color = color;
        this.id = value;
    }

    public int getId() {
        return this.id;
    }

    public int getColor() {
        return this.color;
    }

    public String getSerializedName() {
        return this.name;
    }

    public Component getComponentName() {
        return Component.translatable("hold.stock.coe.mode."
                + this.name).withStyle(ChatFormatting.WHITE);
    }
}
