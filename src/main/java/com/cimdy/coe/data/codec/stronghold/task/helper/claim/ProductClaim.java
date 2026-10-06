package com.cimdy.coe.data.codec.stronghold.task.helper.claim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record ProductClaim(ItemResource resource, int amount, BlockPos containerPos) {
        public static final Codec<ProductClaim> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemResource.CODEC.fieldOf("resource").forGetter(ProductClaim::resource),
                Codec.INT.fieldOf("amount").forGetter(ProductClaim::amount),
                BlockPos.CODEC.fieldOf("containerPos").forGetter(ProductClaim::containerPos)
        ).apply(instance, ProductClaim::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ProductClaim> STREAM_CODEC = StreamCodec.composite(
                ItemResource.STREAM_CODEC, ProductClaim::resource,
                ByteBufCodecs.INT, ProductClaim::amount,
                BlockPos.STREAM_CODEC, ProductClaim::containerPos,
                ProductClaim::new
        );
    }
