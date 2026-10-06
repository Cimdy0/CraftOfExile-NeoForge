package com.cimdy.coe.data.codec.item.mod.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;

public record ModTier(int max, int value) {
        public static final ModTier EMPTY = new ModTier(1, 1);

        public static final Codec<ModTier> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.fieldOf("max").forGetter(ModTier::max),
                        Codec.INT.fieldOf("value").forGetter(ModTier::value)
                ).apply(instance, ModTier::new)
        );

        public ChatFormatting getColor() {
            float f = (float) Math.max(max - value, 0.01) / max;
            return f > 0.7F ? ChatFormatting.YELLOW :
                    f > 0.3F ? ChatFormatting.BLUE :
                            ChatFormatting.WHITE;
        }
    }