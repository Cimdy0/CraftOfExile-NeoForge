package com.cimdy.coe.data.codec.skill.gem;

import com.mojang.serialization.Codec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum GemColor implements StringRepresentable {
    RED("red", 0),
    GREEN("green", 1),
    BLUE("blue", 2);

    public static final IntFunction<GemColor> BY_ID =
            ByIdMap.continuous(GemColor::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<GemColor> CODEC =
            StringRepresentable.fromEnum(GemColor::values);
    private final String name;
    private final int id;

    GemColor(String name, int value) {
        this.name = name;
        this.id = value;
    }

    public int getId() {
        return this.id;
    }

    public String getSerializedName() {
        return this.name;
    }
}
