package com.cimdy.coe.data.codec.stronghold.task.helper.claim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record MaterialClaim(ItemResource resource, int amount, BlockPos containerPos) {
        public static final Codec<MaterialClaim> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemResource.CODEC.fieldOf("resource").forGetter(MaterialClaim::resource),
                Codec.INT.fieldOf("amount").forGetter(MaterialClaim::amount),
                BlockPos.CODEC.fieldOf("containerPos").forGetter(MaterialClaim::containerPos)
        ).apply(instance, MaterialClaim::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MaterialClaim> STREAM_CODEC = StreamCodec.composite(
                ItemResource.STREAM_CODEC, MaterialClaim::resource,
                ByteBufCodecs.INT, MaterialClaim::amount,
                BlockPos.STREAM_CODEC, MaterialClaim::containerPos,
                MaterialClaim::new
        );
    }
