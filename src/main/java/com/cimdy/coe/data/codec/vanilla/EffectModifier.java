package com.cimdy.coe.data.codec.vanilla;

import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;

public record EffectModifier(Holder<MobEffect> effect, List<StatEntry> modifiers) {

    public static final Codec<EffectModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MobEffect.CODEC.fieldOf("effect").forGetter(EffectModifier::effect),
            StatEntry.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(EffectModifier::modifiers)
    ).apply(instance, EffectModifier::new));

    public record Modifiers(List<EffectModifier> modifiers) {
        public static Modifiers EMPTY = new Modifiers(List.of());

        public static final Codec<Modifiers> CODEC =
                EffectModifier.CODEC.listOf().xmap(Modifiers::new, Modifiers::modifiers);

        public Modifiers add(EffectModifier effectModifier) {
            List<EffectModifier> list = new ArrayList<>(this.modifiers);
            list.add(effectModifier);
            return new Modifiers(list);
        }
    }
}
