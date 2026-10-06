package com.cimdy.coe.data.codec.item.mod.value;

import com.cimdy.coe.data.codec.item.mod.identity.Location;

import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ModNumber(List<Number> numbers) {
    public static final Codec<ModNumber> CODEC =
            Number.CODEC.listOf().xmap(ModNumber::new, ModNumber::numbers);

    public record Number(
            Optional<Rarity> rarity, Optional<Integer> level,
            Map<String, Integer> max, int min) {
        public static final Codec<Number> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Rarity.CODEC.optionalFieldOf("rarity").forGetter(Number::rarity),
                        Codec.INT.optionalFieldOf("level").forGetter(Number::level),
                        Codec.unboundedMap(Codec.STRING, Codec.INT)
                                .fieldOf("max").forGetter(Number::max),
                        Codec.INT.fieldOf("min").forGetter(Number::min)
                ).apply(instance, Number::new));

        public static Number from(Rarity rarity, Location name1, int max1,
                                  Location name2, int max2, int min) {
            return new Number(Optional.of(rarity), Optional.empty(),
                    Map.of(name1.getName(), max1, name2.getName(), max2), min);
        }

        public boolean matches(int level, Rarity rarity) {
            if (this.rarity.isPresent() && this.rarity.get() != rarity) {
                return false;
            }
            return this.level.isEmpty() || this.level.get() >= level;
        }

        public int getLocationNumber(Location type) {
            return max.getOrDefault(type.getName(), 0);
        }
    }
}
