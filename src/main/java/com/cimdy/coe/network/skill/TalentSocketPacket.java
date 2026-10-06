package com.cimdy.coe.network.skill;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TalentSocketPacket(Identifier treeId, String nodeId, int action, int invSlot)
            implements CustomPacketPayload {
        public static final int IN = 0;
        public static final int OUT = 1;

        public static final Type<TalentSocketPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "talent_socket"));
        public static final StreamCodec<RegistryFriendlyByteBuf, TalentSocketPacket> STREAM_CODEC =
                StreamCodec.composite(
                        Identifier.STREAM_CODEC, TalentSocketPacket::treeId,
                        ByteBufCodecs.STRING_UTF8, TalentSocketPacket::nodeId,
                        ByteBufCodecs.INT, TalentSocketPacket::action,
                        ByteBufCodecs.INT, TalentSocketPacket::invSlot,
                        TalentSocketPacket::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
