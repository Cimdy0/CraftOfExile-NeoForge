package com.cimdy.coe.network.menu;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenSkillMenuPacket() implements CustomPacketPayload {
        public static final Type<OpenSkillMenuPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "open_menu"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenSkillMenuPacket> STREAM_CODEC =
                StreamCodec.unit(new OpenSkillMenuPacket());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
