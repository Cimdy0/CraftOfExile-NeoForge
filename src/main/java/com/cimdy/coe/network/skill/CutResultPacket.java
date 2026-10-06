package com.cimdy.coe.network.skill;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * 宝石切割结果（服务端 → 客户端）。
 *
 * <p>成功：{@code success=true}、{@code product} 为服务端重建的带等级产物（等级已灌输）；
 * 失败：{@code success=false}、未切割宝石已消耗、无产物，{@code failKey} 为翻译键
 * （如 {@code ui.coe.cut.fail}）。客户端按结果驱动 CutScreen 成功 / 失败双反馈。
 */
public record CutResultPacket(boolean success, int slotIndex, ItemStack product, String failKey) implements CustomPacketPayload {
    public static final Type<CutResultPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "cut_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CutResultPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, CutResultPacket::success,
            ByteBufCodecs.VAR_INT, CutResultPacket::slotIndex,
            ItemStack.OPTIONAL_STREAM_CODEC, CutResultPacket::product,
            ByteBufCodecs.STRING_UTF8, CutResultPacket::failKey,
            CutResultPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
