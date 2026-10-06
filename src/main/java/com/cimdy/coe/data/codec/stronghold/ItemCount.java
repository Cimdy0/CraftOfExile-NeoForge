package com.cimdy.coe.data.codec.stronghold;

import com.cimdy.coe.data.codec.CoeCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record ItemCount(ItemResource resource, int amount) {
    public static Codec<ItemCount> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemResource.CODEC.fieldOf("resource").forGetter(ItemCount::resource),
            Codec.INT.fieldOf("amount").forGetter(ItemCount::amount)
    ).apply(instance, ItemCount::new));

    public static StreamCodec<RegistryFriendlyByteBuf, ItemCount> STREAM_CODEC = StreamCodec.composite(
            CoeCodecs.ITEM_RESOURCE_STREAM, ItemCount::resource,
            ByteBufCodecs.INT, ItemCount::amount,
            ItemCount::new
    );

    public ItemCount add(int add) {
        return new ItemCount(resource, amount + add);
    }
}
