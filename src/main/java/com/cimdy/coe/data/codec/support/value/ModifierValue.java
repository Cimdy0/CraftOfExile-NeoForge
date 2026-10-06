package com.cimdy.coe.data.codec.support.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ModifierValue {
    public record Int(int original, int change) {
        public static Int EMPTY = new Int(0, 0);
        public static Codec<Int> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("original", 0).forGetter(Int::original),
                Codec.INT.optionalFieldOf("change", 0).forGetter(Int::change)
        ).apply(instance, Int::new));

        public static Int create(int original, int change) {
            return new Int(original, change);
        }

        public static Int create(int original) {
            return new Int(original, 0);
        }

        public int get() {
            return original + change;
        }

        public Int setChange(int change) {
            return new Int(original, change);
        }

        public Int setOriginal(int original) {
            return new Int(original, change);
        }

        public Int addOriginal(int quality) {
            return new Int(original + quality, change);
        }
    }
}
