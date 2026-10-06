package com.cimdy.coe.data.codec.vanilla;

import com.cimdy.coe.client.ClientLocale;

import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import com.cimdy.coe.data.codec.support.meta.Labels;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public record EnchantModifier(
        ResourceKey<Enchantment> enchant, Optional<List<Addition>> additions,
        Optional<List<String>> labels) {
    public static final Codec<EnchantModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.ENCHANTMENT).fieldOf("enchant").forGetter(EnchantModifier::enchant),
            Addition.CODEC.listOf().optionalFieldOf("additions").forGetter(EnchantModifier::additions),
            Labels.CODEC.listOf().optionalFieldOf("labels").forGetter(EnchantModifier::labels)
    ).apply(instance, EnchantModifier::new));

    public static EnchantModifier from(ResourceKey<Enchantment> enchant, List<Addition> effect, List<String> labels) {
        return new EnchantModifier(enchant, Optional.of(effect), Optional.of(labels));
    }

    public static EnchantModifier from(ResourceKey<Enchantment> enchant, List<Addition> effect) {
        return new EnchantModifier(enchant, Optional.of(effect), Optional.empty());
    }

    public Builder builder() {
        return new Builder(additions.orElse(Builder.DEFAULT.effect()),
                labels.orElse(List.of()));
    }

    public record Builder(List<Addition> effect, List<String> labels) {
        public static final Builder DEFAULT = new Builder(List.of(), List.of());

        public Value build(ResourceKey<Enchantment> enchant, int level) {
            return new Value(enchant, level, effect, labels);
        }
    }

    public record Value(ResourceKey<Enchantment> enchant, int level,
                        List<Addition> effects, List<String> labels) implements ModifierEntry {

        /** 多态暴露附魔词缀列表（{@link ModifierEntry#additions()}），消费端无需 {@code instanceof} 本臂。 */
        @Override
        public List<Addition> additions() {
            return effects;
        }

        public void getComponent(List<Component> tooltip, int level, ChatFormatting color) {
            // 经 AdditionType.describe 单轨输出效果文本（附魔按等级缩放 roll 值）
            effects.forEach(addition -> {
                MutableComponent comp = Component.literal("").withStyle(ChatFormatting.BLUE);
                addition.type().describe(addition.getValue() * level, ClientLocale.get()).forEach(comp::append);
                tooltip.add(comp);
            });
        }

        public void applyTo(Level level, ItemEnchantments.Mutable mutable) {
            HolderGetter<Enchantment> getter = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            mutable.set(getter.getOrThrow(enchant), this.level);
        }
    }
}
