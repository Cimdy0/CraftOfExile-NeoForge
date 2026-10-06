package com.cimdy.coe.network.input;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * 翻滚发起包（客户端 → 服务端）：携带玩家当前移动输入（左右 / 前后）与是否空翻（翻滚键 + 空格），
 * 服务端据此解析翻滚方向（无输入则取视线前方）并决定普通翻滚 / 空翻变体。
 */
public record DodgeRollPacket(float moveX, float moveZ, boolean flip) implements CustomPacketPayload {
    public static final Type<DodgeRollPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "dodge_roll"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DodgeRollPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, DodgeRollPacket::moveX,
            ByteBufCodecs.FLOAT, DodgeRollPacket::moveZ,
            ByteBufCodecs.BOOL, DodgeRollPacket::flip,
            DodgeRollPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
