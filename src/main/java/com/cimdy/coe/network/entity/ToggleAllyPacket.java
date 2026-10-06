package com.cimdy.coe.network.entity;

import com.cimdy.coe.stronghold.area.StrongholdRepository;
import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * 客户端 → 服务端：翻转某实体的「盟友（随行伙伴）」标记。
 *
 * <p>{@code pos} 是发出请求的那个终端方块坐标，服务端据此做可达性校验，并决定优先作用于
 * 「该据点内已部署的实体」还是「玩家存档里的未放出实体」（见
 * {@code StrongholdRepository#toggleAlly}）。
 */
public record ToggleAllyPacket(UUID uuid, BlockPos pos) implements CustomPacketPayload {
    public static final Type<ToggleAllyPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "toggle_ally"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleAllyPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ToggleAllyPacket::uuid,
            BlockPos.STREAM_CODEC, ToggleAllyPacket::pos,
            ToggleAllyPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
