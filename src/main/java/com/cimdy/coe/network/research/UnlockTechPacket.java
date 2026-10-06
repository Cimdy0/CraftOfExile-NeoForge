package com.cimdy.coe.network.research;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UnlockTechPacket(Identifier techId) implements CustomPacketPayload {
        public static final Type<UnlockTechPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "unlock_tech"));
        public static final StreamCodec<RegistryFriendlyByteBuf, UnlockTechPacket> STREAM_CODEC =
                StreamCodec.composite(
                        Identifier.STREAM_CODEC, UnlockTechPacket::techId,
                        UnlockTechPacket::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
