package com.cimdy.coe.network.sync;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record MobEffectPacket(
            Optional<UUID> uuid, Optional<List<MobEffectInstance>> effects) implements CustomPacketPayload {
        public static final Type<MobEffectPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "mob_effect"));
        public static final StreamCodec<RegistryFriendlyByteBuf, MobEffectPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), MobEffectPacket::uuid,
                ByteBufCodecs.optional(MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list())), MobEffectPacket::effects,
                MobEffectPacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
