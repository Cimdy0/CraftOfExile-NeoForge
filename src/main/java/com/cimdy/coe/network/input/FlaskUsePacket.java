package com.cimdy.coe.network.input;

import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Flask 使用请求（客户端 → 服务端）：按键触发。
 * {@code slotIndex}：0 = 生命药剂槽，1 = 魔力药剂槽，-1 = 全部使用。
 */
public record FlaskUsePacket(int slotIndex) implements CustomPacketPayload {

    public static final Type<FlaskUsePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "flask_use"));

    public static final Codec<FlaskUsePacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("slot").forGetter(FlaskUsePacket::slotIndex)
    ).apply(instance, FlaskUsePacket::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlaskUsePacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, FlaskUsePacket::slotIndex, FlaskUsePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
