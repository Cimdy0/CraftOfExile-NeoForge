package com.cimdy.coe.network.skill;

import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CutGemPacket(int slotIndex, Identifier gemId) implements CustomPacketPayload {
        public static final Type<CutGemPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "cut_gem"));
        public static final Codec<CutGemPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("slot").forGetter(CutGemPacket::slotIndex),
                Identifier.CODEC.fieldOf("gem_id").forGetter(CutGemPacket::gemId)
        ).apply(instance, CutGemPacket::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, CutGemPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, CutGemPacket::slotIndex,
                Identifier.STREAM_CODEC, CutGemPacket::gemId,
                CutGemPacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
