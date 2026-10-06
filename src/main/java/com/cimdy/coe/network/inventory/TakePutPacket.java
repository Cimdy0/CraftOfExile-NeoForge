package com.cimdy.coe.network.inventory;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * 背包浮窗槽位点击（客户端 → 服务端）。
 *
 * <p>{@code slotIndex} 为真实 MC 槽位号（0..8 热键栏 / 9..35 主背包，与
 * {@code InventoryFloatWindow#onMiniSlot} 上抛口径一致）；
 * {@code button} 0=左键（拿起/放下/合并/交换），当前仅支持左键语义。
 */
public record TakePutPacket(int slotIndex, int button) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TakePutPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "take_put"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TakePutPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TakePutPacket::slotIndex,
            ByteBufCodecs.VAR_INT, TakePutPacket::button,
            TakePutPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
