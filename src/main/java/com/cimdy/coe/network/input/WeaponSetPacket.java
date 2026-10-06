package com.cimdy.coe.network.input;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record WeaponSetPacket(int set) implements CustomPacketPayload {
        public static final Type<WeaponSetPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "weapon_set"));
        public static final StreamCodec<RegistryFriendlyByteBuf, WeaponSetPacket> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT, WeaponSetPacket::set,
                        WeaponSetPacket::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
