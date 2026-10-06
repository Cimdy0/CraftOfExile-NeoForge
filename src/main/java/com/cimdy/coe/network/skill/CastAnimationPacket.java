package com.cimdy.coe.network.skill;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record CastAnimationPacket(Optional<Identifier> animation, Identifier skillId, int castTimeTicks, long startTime) implements CustomPacketPayload {
        public static final Type<CastAnimationPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "cast_animation"));
        public static final StreamCodec<RegistryFriendlyByteBuf, CastAnimationPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(Identifier.STREAM_CODEC), CastAnimationPacket::animation,
                Identifier.STREAM_CODEC, CastAnimationPacket::skillId,
                ByteBufCodecs.VAR_INT, CastAnimationPacket::castTimeTicks,
                ByteBufCodecs.VAR_LONG, CastAnimationPacket::startTime,
                CastAnimationPacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
