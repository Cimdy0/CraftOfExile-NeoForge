package com.cimdy.coe.data.codec.support.attribute;

import com.cimdy.coe.data.helper.stat.AttributeLimiterHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.ArrayList;
import java.util.List;

public record EntityAttributeLimiter(List<Modifier> modifiers) {
    public static final EntityAttributeLimiter EMPTY = new EntityAttributeLimiter(List.of());
    public static final Codec<EntityAttributeLimiter> CODEC =
            EntityAttributeLimiter.Modifier.CODEC.listOf()
                    .xmap(EntityAttributeLimiter::new, EntityAttributeLimiter::modifiers);

    public record Modifier(Holder<Attribute> attribute, int duration, AttributeLimiters.Modifier modifier) {
        public static final Codec<Modifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Attribute.CODEC.fieldOf("attribute").forGetter(Modifier::attribute),
                Codec.INT.optionalFieldOf("duration", -1).forGetter(Modifier::duration),
                AttributeLimiters.Modifier.CODEC.fieldOf("modifier").forGetter(Modifier::modifier)
        ).apply(instance, Modifier::new));
    }

    public record Modifiers(List<Modifier> states) {
        public static final Modifiers EMPTY = new Modifiers(new ArrayList<>());

        public static final Codec<Modifiers> CODEC =
                Codec.list(Modifier.CODEC).xmap(Modifiers::new, Modifiers::states);

        /** 附加带持续时间的限制器（duration < 0 为永久）。 */
        public static void apply(LivingEntity living, Holder<Attribute> attribute, int duration,
                                 AttributeLimiters.Modifier modifier) {
            AttributeLimiterHelper.applyDurationLimiter(living, attribute, duration, modifier);
        }

        /** 附加永久限制器。 */
        public static void apply(LivingEntity living, Holder<Attribute> attribute,
                                 AttributeLimiters.Modifier modifier) {
            AttributeLimiterHelper.applyDurationLimiter(living, attribute, -1, modifier);
        }

    }
}
