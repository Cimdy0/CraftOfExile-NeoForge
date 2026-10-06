package com.cimdy.coe.network.entity;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record RecycleEntityPacket(UUID uuid, BlockPos pos) implements CustomPacketPayload {
        public static final Type<RecycleEntityPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "recycle_entity"));

        public static final StreamCodec<RegistryFriendlyByteBuf, RecycleEntityPacket> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, RecycleEntityPacket::uuid,
                BlockPos.STREAM_CODEC, RecycleEntityPacket::pos,
                RecycleEntityPacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
