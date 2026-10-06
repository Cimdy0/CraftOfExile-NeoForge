package com.cimdy.coe.network.sync;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.equipment.AccessorySlotDef;
import com.cimdy.coe.data.codec.item.equipment.FlaskSlotDef;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 槽位定义（服务端 -> 客户端）整表同步包。
 *
 * <p>饰品 / 药剂槽定义来自服务端配置文件 config/coe/slots.json（SlotConfigLoader），客户端自身读不到；
 * 登录、打开饰品/药剂/护符面板、以及 /reload 后各推一次，使客户端 AccessorySlotRegistry / FlaskSlotRegistry
 * 不再读到空表。客户端收到后经 LoadEvent.applySlotSync 无条件整表覆盖本地快照。
 */
public record SlotSyncPacket(List<AccessorySlotDef> accessorySlots, List<FlaskSlotDef> flaskSlots)
        implements CustomPacketPayload {
    public static final Type<SlotSyncPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "slot_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SlotSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.fromCodec(AccessorySlotDef.CODEC.listOf()), SlotSyncPacket::accessorySlots,
                    ByteBufCodecs.fromCodec(FlaskSlotDef.CODEC.listOf()), SlotSyncPacket::flaskSlots,
                    SlotSyncPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
