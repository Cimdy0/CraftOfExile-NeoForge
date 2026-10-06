package com.cimdy.coe.network.menu;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenExtraSlotsMenuPacket() implements CustomPacketPayload {
        public static final Type<OpenExtraSlotsMenuPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "open_extra_slots"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenExtraSlotsMenuPacket> STREAM_CODEC =
                StreamCodec.unit(new OpenExtraSlotsMenuPacket());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
