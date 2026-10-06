package com.cimdy.coe.data.codec.support.math.context.nodes.value.status;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.StatusNode;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Locale;

/**
 * 药水效果读值节点（context_dsl_design.md §5 EffectValue）。
 * 前缀 {@code effect.level} 输出等级（amplifier+1），{@code effect.duration} 输出剩余 tick。
 * 实体无该效果时返回 0；非 LivingEntity 作用域返回 0。
 */
public record EffectValue(Identifier id, String mode) implements StatusNode {
    public static final Codec<EffectValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("id").forGetter(EffectValue::id),
                    Codec.STRING.fieldOf("mode").forGetter(EffectValue::mode)
            ).apply(instance, EffectValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        return BuiltInRegistries.MOB_EFFECT.get(id)
                .map(Holder.Reference::getDelegate)
                .map(effect -> {
                    MobEffectInstance inst = living.getEffect(effect);
                    if (inst == null) return 0D;
                    return switch (mode) {
                        case "effect.level" -> (double) (inst.getAmplifier() + 1);
                        case "effect.duration" -> (double) inst.getDuration();
                        default -> 0D;
                    };
                })
                .orElseThrow(() -> new IllegalArgumentException("未知药水效果: " + id));
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("mob_effect", id.getPath()));
    }

    @Override
    public List<Component> describeFragment() {
        Component effect = StatValueKey.of("mob_effect", id.getPath());
        return switch (mode) {
            case "effect.level" -> List.of(Component.translatable("stat.condition.effect_level", effect));
            case "effect.duration" -> List.of(Component.translatable("stat.condition.effect_duration", effect));
            default -> List.of(Component.translatable("stat.condition.unknown"));
        };
    }
}
