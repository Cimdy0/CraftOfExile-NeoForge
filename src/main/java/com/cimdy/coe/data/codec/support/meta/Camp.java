package com.cimdy.coe.data.codec.support.meta;

import com.mojang.serialization.Codec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum Camp implements StringRepresentable {
    NONE("none", 0),
    PLAYER("player", 1),
    MOB("mob", 2);

    public static final IntFunction<Camp> BY_ID =
            ByIdMap.continuous(Camp::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<Camp> CODEC =
            StringRepresentable.fromEnum(Camp::values);
    private final String name;
    private final int id;

    Camp(String name, int value) {
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
