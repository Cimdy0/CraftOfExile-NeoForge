package com.cimdy.coe.data.codec.skill.helper.resource;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum ResourceUsage implements StringRepresentable {
    CONSUME("cost", 0),
    PRESERVE("preserve", 1);

    public static final IntFunction<ResourceUsage> BY_ID =
            ByIdMap.continuous(ResourceUsage::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<ResourceUsage> CODEC =
            StringRepresentable.fromEnum(ResourceUsage::values);
    private final String name;
    private final int id;

    ResourceUsage(String name, int value) {
        this.name = name;
        this.id = value;
    }

    public int getId() {
        return this.id;
    }

    public String getSerializedName() {
        return this.name;
    }

    public MutableComponent getComponent() {
        return Component.translatable("stat." + this.name);
    }
}
