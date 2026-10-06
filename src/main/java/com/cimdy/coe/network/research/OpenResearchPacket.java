package com.cimdy.coe.network.research;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenResearchPacket(BlockPos benchPos) implements CustomPacketPayload {
        public static final Type<OpenResearchPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "open_research"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenResearchPacket> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC, OpenResearchPacket::benchPos,
                        OpenResearchPacket::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
