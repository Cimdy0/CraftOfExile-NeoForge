package com.cimdy.coe.network.input;

import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record KeyPressPacket(int key, boolean released) implements CustomPacketPayload {
        public static final Type<KeyPressPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "key_press"));
        public static final KeyPressPacket EMPTY = new KeyPressPacket(-100, false);

        public static final Codec<KeyPressPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("key").forGetter(KeyPressPacket::key),
                Codec.BOOL.fieldOf("released").forGetter(KeyPressPacket::released)
        ).apply(instance, KeyPressPacket::new));

        public KeyPressPacket setReleased(boolean released) {
            return new KeyPressPacket(this.key, released);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
