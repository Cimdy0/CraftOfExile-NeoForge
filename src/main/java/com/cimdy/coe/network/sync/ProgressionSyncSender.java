package com.cimdy.coe.network.sync;

import com.cimdy.coe.event.net.NetEvent;
import com.cimdy.coe.network.research.OpenResearchPacket;
import com.cimdy.coe.data.codec.progression.ProgressionData;
import com.cimdy.coe.data.codec.progression.ProgressionService;
import com.cimdy.coe.stronghold.area.StrongholdRepository;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.UUID;

/**
 * 进度系统「服务端 → 客户端」推送入口（原 {@code event.NetEvent} 内联方法，铁律 #56 迁出）。
 *
 * <p>任务 / 科技 / 研究三张定义在 {@code data/}（数据包）下，客户端自身读不到——专用服务器上
 * 若不同步，科技树与研究树界面会全空。故登录、数据包重载、打开界面前各推一次。
 */
public final class ProgressionSyncSender {
    private ProgressionSyncSender() {
    }

    /** 右键空闲研究台：让该玩家客户端打开研究树界面，并把研究台坐标带过去。 */
    public static void openResearchScreen(ServerPlayer player, BlockPos benchPos) {
        syncProgression(player);
        PacketDistributor.sendToPlayer(player, new OpenResearchPacket(benchPos));
    }

    /** 把任务 / 科技 / 研究 / 职业特许四张定义表整表推给某玩家。 */
    public static void syncProgression(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ProgressionSyncPacket(
                List.copyOf(ProgressionData.QUESTS.values()),
                List.copyOf(ProgressionData.TECH_NODES.values()),
                List.copyOf(ProgressionData.RESEARCH_NODES.values()),
                List.copyOf(ProgressionData.JOB_CHARTERS.values())));
    }

    /** {@code /reload} 后重新推送给全体在线玩家（由 {@code LoadEvent} 的重载监听器在主线程调用）。 */
    public static void broadcastProgression() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) syncProgression(player);
    }

    /** 据点所有者进度对应的站点上限（所有者离线时退回基础值）。 */
    public static int ownerSiteCap(ServerLevel level, GlobalPos terminalPos) {
        List<UUID> owners = StrongholdRepository.getHoldOwners(level, terminalPos.pos());
        if (!owners.isEmpty()) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(owners.get(0));
            if (owner != null) return ProgressionService.siteCap(owner);
        }
        return ProgressionService.BASE_SITE_CAP;
    }
}
