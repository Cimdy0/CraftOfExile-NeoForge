package com.cimdy.coe.network.sync;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.status.registry.ground.GroundFieldData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 地面场全量同步包（服务端 → 客户端）：周期性把某维度的全部活动地面场推给该维度玩家，
 * 供客户端渲染（粒子 + 贴地网格）。客户端以整表替换自身镜像，保证迟到/登录客户端也能拿到完整数据。
 */
public record GroundFieldSyncPacket(List<GroundFieldData> fields) implements CustomPacketPayload {
    public static final Type<GroundFieldSyncPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "ground_field_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GroundFieldSyncPacket> STREAM_CODEC =
            StreamCodec.composite(GroundFieldData.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    GroundFieldSyncPacket::fields, GroundFieldSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
