package com.cimdy.coe.network.menu;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenTalentMenuPacket(Identifier treeId) implements CustomPacketPayload {
        public static final Type<OpenTalentMenuPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "open_talent_menu"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenTalentMenuPacket> STREAM_CODEC =
                StreamCodec.composite(
                        Identifier.STREAM_CODEC, OpenTalentMenuPacket::treeId,
                        OpenTalentMenuPacket::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
