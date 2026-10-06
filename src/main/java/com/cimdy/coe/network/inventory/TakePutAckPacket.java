package com.cimdy.coe.network.inventory;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * 背包取放结果（服务端 → 客户端）。
 *
 * <p>携带被点击槽位的最新物品栈与客户端镜像的「手持」栈：客户端据此把服务端权威值
 * 写入本地 {@code player.getInventory()}（后续任何 Menu 打开时同步不会冲突），
 * 并更新背包浮窗的 carried 渲染。
 */
public record TakePutAckPacket(int slotIndex, ItemStack stack, ItemStack carried) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TakePutAckPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "take_put_ack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TakePutAckPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TakePutAckPacket::slotIndex,
            ItemStack.OPTIONAL_STREAM_CODEC, TakePutAckPacket::stack,
            ItemStack.OPTIONAL_STREAM_CODEC, TakePutAckPacket::carried,
            TakePutAckPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
