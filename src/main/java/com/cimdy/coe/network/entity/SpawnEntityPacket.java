package com.cimdy.coe.network.entity;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.CustomData;

public record SpawnEntityPacket(CustomData data, BlockPos pos) implements CustomPacketPayload {
        public static final Type<SpawnEntityPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "spawn_entity"));

        public static final StreamCodec<RegistryFriendlyByteBuf, SpawnEntityPacket> STREAM_CODEC = StreamCodec.composite(
                CustomData.STREAM_CODEC, SpawnEntityPacket::data,
                BlockPos.STREAM_CODEC, SpawnEntityPacket::pos,
                SpawnEntityPacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
