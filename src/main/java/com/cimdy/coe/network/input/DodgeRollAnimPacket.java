package com.cimdy.coe.network.input;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * 翻滚动画通知包（服务端 → 客户端）：携带翻滚玩家的 UUID、动画种类与时长，
 * 各客户端据此在对应玩家实体上播放翻滚 / 调整动作动画。
 */
public record DodgeRollAnimPacket(UUID playerUuid, int kind, int rollTicks) implements CustomPacketPayload {
    /** 动画种类：0 = 翻滚，1 = 被击中断后的调整动作，2 = 空翻（翻滚+空格）。 */
    public static final int KIND_ROLL = 0;
    public static final int KIND_RECOVERY = 1;
    public static final int KIND_FLIP = 2;

    public static final Type<DodgeRollAnimPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "dodge_roll_anim"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DodgeRollAnimPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, DodgeRollAnimPacket::playerUuid,
            ByteBufCodecs.VAR_INT, DodgeRollAnimPacket::kind,
            ByteBufCodecs.VAR_INT, DodgeRollAnimPacket::rollTicks,
            DodgeRollAnimPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
