package com.cimdy.coe.network.stronghold;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SiteSetting(BlockPos terminalPos, BlockPos sitePos,
                              int priority, boolean autoAssign, int maxWorkers) implements CustomPacketPayload {
        public static final Type<SiteSetting> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "site_setting"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SiteSetting> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, SiteSetting::terminalPos,
                BlockPos.STREAM_CODEC, SiteSetting::sitePos,
                ByteBufCodecs.INT, SiteSetting::priority,
                ByteBufCodecs.BOOL, SiteSetting::autoAssign,
                ByteBufCodecs.INT, SiteSetting::maxWorkers,
                SiteSetting::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
