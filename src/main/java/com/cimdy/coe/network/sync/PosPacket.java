package com.cimdy.coe.network.sync;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record PosPacket(UUID uuid, Vec3 position, Vec3 ron, Vec3 de) implements CustomPacketPayload {
        public static final Type<PosPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "pos"));
        public static final StreamCodec<RegistryFriendlyByteBuf, PosPacket> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, PosPacket::uuid,
                Vec3.STREAM_CODEC, PosPacket::position,
                Vec3.STREAM_CODEC, PosPacket::ron,
                Vec3.STREAM_CODEC, PosPacket::de,
                PosPacket::new
        );

        public static PosPacket of(Entity entity) {
            return new PosPacket(
                    entity.getUUID(), entity.position(),
                    Vec3.directionFromRotation(entity.getXRot(), entity.getYRot()),
                    entity.getDeltaMovement());
        }

        public void set(Level level) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) {
                entity.setPos(position);
                entity.setXRot((float) ron.x);
                entity.setYRot((float) ron.y);
                entity.setDeltaMovement(de);
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
