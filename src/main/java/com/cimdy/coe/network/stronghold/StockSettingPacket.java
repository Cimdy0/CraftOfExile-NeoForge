package com.cimdy.coe.network.stronghold;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record StockSettingPacket(BlockPos pos, StockSet stockSet) implements CustomPacketPayload {
        public static final Type<StockSettingPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "stock_setting"));

        public static final StreamCodec<RegistryFriendlyByteBuf, StockSettingPacket> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC, StockSettingPacket::pos,
                        StockSet.STREAM_CODEC, StockSettingPacket::stockSet,
                        StockSettingPacket::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
