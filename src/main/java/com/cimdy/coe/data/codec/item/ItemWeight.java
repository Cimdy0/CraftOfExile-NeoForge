package com.cimdy.coe.data.codec.item;

import com.cimdy.coe.data.codec.item.ItemInstance;
import com.cimdy.coe.data.codec.item.ItemInstances;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public record ItemWeight(
        Optional<Identifier> type, Optional<String> name,
        Optional<Holder<Item>> item, Optional<TagKey<Item>> tag,
        int weight
) {
    public static final Codec<ItemWeight> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("type").forGetter(ItemWeight::type),
            Codec.STRING.optionalFieldOf("name").forGetter(ItemWeight::name),
            Item.CODEC.optionalFieldOf("item").forGetter(ItemWeight::item),
            TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(ItemWeight::tag),
            Codec.INT.fieldOf("weight").forGetter(ItemWeight::weight)
    ).apply(instance, ItemWeight::new));

    public static ItemWeight from(Identifier type, int weight) {
        return new ItemWeight(Optional.of(type), Optional.empty(), Optional.empty(), Optional.empty(), weight);
    }

    public static ItemWeight from(Holder<Item> item, int weight) {
        return new ItemWeight(Optional.empty(), Optional.empty(), Optional.of(item), Optional.empty(), weight);
    }

    public static ItemWeight from(TagKey<Item> tag, int weight) {
        return new ItemWeight(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(tag), weight);
    }

    public record Weights(List<ItemWeight> weights) implements ItemInstance {
        public static final Codec<Weights> CODEC = ItemWeight.CODEC.listOf().xmap(Weights::new, Weights::weights);

        public static ItemInstance from(List<ItemWeight> weight) {
            return new Weights(weight);
        }

        @Override
        public Type<?> getType() {
            return ItemInstances.ITEM_WEIGHT;
        }
    }

    public record Type(Optional<Identifier> type, Optional<Holder<Item>> item, Optional<TagKey<Item>> tag) {
        public static Type from(ItemWeight weight) {
            return new Type(weight.type, weight.item, weight.tag);
        }

        public boolean matches(ItemStack stack) {
            if (item.isPresent() && !stack.is(item.get())) return false;
            if (tag.isPresent() && !stack.is(tag.get())) return false;
            return item.isPresent() || tag.isPresent();
        }

        public boolean matches(Identifier type) {
            return this.type.isPresent() && this.type.get().equals(type);
        }
    }
}