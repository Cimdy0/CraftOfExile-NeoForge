package com.cimdy.coe.network.stronghold;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TeleportRequestPacket(BlockPos terminalPos, GlobalPos targetPos) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<TeleportRequestPacket> TYPE =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "teleport_request"));

        public static final StreamCodec<RegistryFriendlyByteBuf, TeleportRequestPacket> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, TeleportRequestPacket::terminalPos,
                GlobalPos.STREAM_CODEC, TeleportRequestPacket::targetPos,
                TeleportRequestPacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
