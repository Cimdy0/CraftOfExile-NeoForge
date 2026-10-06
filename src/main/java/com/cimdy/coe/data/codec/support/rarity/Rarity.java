package com.cimdy.coe.data.codec.support.rarity;

import com.cimdy.coe.CoeConfig;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum Rarity implements StringRepresentable {
    NONE("none", ChatFormatting.DARK_GRAY, 0),
    COMMON("common", ChatFormatting.WHITE, 1),
    MAGIC("magic", ChatFormatting.DARK_AQUA, 2),
    RARE("rare", ChatFormatting.YELLOW, 3),
    UNIQUE("unique", ChatFormatting.GOLD, 4);

    public static final IntFunction<Rarity> BY_ID =
            ByIdMap.continuous(Rarity::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<Rarity> CODEC =
            StringRepresentable.fromEnum(Rarity::values);
    private final String name;
    private final ChatFormatting color;
    private final int id;

    Rarity(String name, ChatFormatting color, int value) {
        this.name = name;
        this.id = value;
        this.color = color;
    }

    public static Rarity get(String rarityString) {
        for (Rarity rarity : values()) {
            if (rarity.getSerializedName().equalsIgnoreCase(rarityString)) {
                return rarity;
            }
        }
        throw new IllegalArgumentException("Invalid rarity: " + rarityString);
    }

    public Pair<Integer, Integer> get() {
        int min = 0;
        int max = 0;
        if (this == Rarity.COMMON) {
            min = CoeConfig.commonMinMod;
            max = CoeConfig.commonMaxMod;
        } else if (this == Rarity.MAGIC) {
            min = CoeConfig.magicMinMod;
            max = CoeConfig.magicMaxMod;
        } else if (this == Rarity.RARE) {
            min = CoeConfig.rareMinMod;
            max = CoeConfig.rareMaxMod;
        } else if (this == Rarity.UNIQUE) {
            min = CoeConfig.uniqueMinMod;
            max = CoeConfig.uniqueMaxMod;
        }
        return Pair.of(min, max);
    }

    public int getId() {
        return this.id;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public String getSerializedName() {
        return this.name;
    }
}
