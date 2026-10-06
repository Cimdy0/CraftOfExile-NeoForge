package com.cimdy.coe.network.research;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record StartResearchPacket(BlockPos benchPos, Identifier researchId) implements CustomPacketPayload {
        public static final Type<StartResearchPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "start_research"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StartResearchPacket> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC, StartResearchPacket::benchPos,
                        Identifier.STREAM_CODEC, StartResearchPacket::researchId,
                        StartResearchPacket::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
