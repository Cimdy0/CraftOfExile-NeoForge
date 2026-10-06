package com.cimdy.coe.network.sync;

import com.cimdy.coe.data.codec.item.equipment.AccessorySlotDef;
import com.cimdy.coe.data.codec.item.equipment.FlaskSlotDef;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

/**
 * 槽位定义（服务端 -> 客户端）推送入口（仿 ProgressionSyncSender）。
 *
 * <p>把服务端权威的 LoadEvent.ACCESSORY_SLOT_MAP / LoadEvent.FLASK_SLOT_MAP 整表推给客户端，
 * 登录、打开面板前、/reload 后各推一次。客户端收到后经 LoadEvent.applySlotSync 无条件整表覆盖。
 */
public final class SlotSyncSender {
    private SlotSyncSender() {
    }

    /** 把当前服务端槽位定义表推给某玩家。 */
    public static void syncSlots(ServerPlayer player) {
        List<AccessorySlotDef> acc = new ArrayList<>(LoadEvent.ACCESSORY_SLOT_MAP.values());
        List<FlaskSlotDef> fl = new ArrayList<>(LoadEvent.FLASK_SLOT_MAP.values());
        PacketDistributor.sendToPlayer(player, new SlotSyncPacket(acc, fl));
    }

    /** /reload 后推送给全体在线玩家（由 LoadEvent 的 FLASK 重载监听器在主线程调用）。 */
    public static void broadcastSlots() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) syncSlots(player);
    }
}
