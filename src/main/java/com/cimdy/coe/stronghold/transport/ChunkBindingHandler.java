package com.cimdy.coe.stronghold.transport;

import com.cimdy.coe.stronghold.area.StrongholdRepository;
import com.cimdy.coe.data.codec.progression.PlayerProgress;
import com.cimdy.coe.data.codec.progression.ProgressionService;
import com.cimdy.coe.data.codec.progression.quest.QuestService;
import com.cimdy.coe.data.codec.progression.quest.QuestType;
import com.cimdy.coe.data.codec.stronghold.Stronghold;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import java.util.List;
import java.util.UUID;

/**
 * 区块绑定业务：玩家用界桩把一个区块并入基地，从而<b>建设出不规则的基地形状</b>。
 *
 * <p>三重约束共同决定「基地能长多大、长成什么形状」：
 * <ol>
 *   <li><b>连通性</b>——新区块必须与基地已有区块四邻相接，基地不能是散落孤岛；</li>
 *   <li><b>区块预算</b>——由玩家等级与科技共同决定（见 {@link ProgressionService#chunkBudget}）；</li>
 *   <li><b>归属权</b>——只有据点所有者可以扩张。</li>
 * </ol>
 * 这三条把「基建扩张」变成需要长期投入的进度目标，而不是开局即可铺满地图。
 */
public final class ChunkBindingHandler {

    private ChunkBindingHandler() {
    }

    /**
     * 尝试把界桩所在区块绑定到相邻基地。
     *
     * @return 绑定成功返回 true；失败时已向玩家发送原因提示
     */
    public static boolean tryBind(ServerLevel level, Player player, BlockPos markerPos) {
        ChunkPos chunk = new ChunkPos(markerPos.getX() >> 4, markerPos.getZ() >> 4);

        // 1. 该区块是否已被占用
        BlockPos owner = StrongholdRepository.findTerminalOwningChunk(level, chunk);
        if (owner != null) {
            fail(player, "hold.coe.bind.already_owned");
            return false;
        }

        // 2. 必须与已有基地区块相邻（保证连通）
        BlockPos terminal = StrongholdRepository.findAdjacentHoldTerminal(level, chunk);
        if (terminal == null) {
            fail(player, "hold.coe.bind.not_adjacent");
            return false;
        }

        // 3. 权限
        if (!StrongholdRepository.isInHoldOwner(player, terminal)) {
            fail(player, "hold.coe.bind.no_permission");
            return false;
        }

        // 4. 区块预算
        PlayerProgress progress = PlayerProgress.get(player);
        if (ProgressionService.remainingChunks(progress) <= 0) {
            player.sendOverlayMessage(Component.translatable("hold.coe.bind.no_budget",
                    progress.chunksUsed(), ProgressionService.chunkBudget(progress))
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        // 5. 落地：写入据点 + 增量申请区块票 + 记账 + 推进任务
        if (!StrongholdRepository.bindChunk(level, terminal, chunk)) {
            fail(player, "hold.coe.bind.failed");
            return false;
        }
        PlayerProgress.set(player, progress.withChunksUsed(progress.chunksUsed() + 1));
        QuestService.onEvent(player, QuestType.BIND_CHUNK,
                Identifier.fromNamespaceAndPath("coe", "chunk"), 1);

        PlayerProgress after = PlayerProgress.get(player);
        player.sendOverlayMessage(Component.translatable("hold.coe.bind.success",
                after.chunksUsed(), ProgressionService.chunkBudget(after))
                .withStyle(ChatFormatting.GREEN));
        return true;
    }

    /**
     * 界桩被移除：解绑其所在区块并退还预算。中心区块（终端所在）不可解绑。
     */
    public static void unbind(ServerLevel level, Player player, BlockPos markerPos) {
        ChunkPos chunk = new ChunkPos(markerPos.getX() >> 4, markerPos.getZ() >> 4);
        BlockPos terminal = StrongholdRepository.findTerminalOwningChunk(level, chunk);
        if (terminal == null) return;

        // 中心区块是据点存在的前提，不随界桩消失
        if (new ChunkPos(terminal.getX() >> 4, terminal.getZ() >> 4).equals(chunk)) return;

        // 权限：只有据点所有者可解绑（避免任何人拆他人界桩腾出区块）
        if (player != null && !StrongholdRepository.isInHoldOwner(player, terminal)) {
            fail(player, "hold.coe.bind.no_permission");
            return;
        }

        if (!StrongholdRepository.unbindChunk(level, terminal, chunk)) return;

        // 预算退还给据点所有者（按 first owner；所有者离线则跳过退还）
        if (player != null) {
            List<UUID> owners = StrongholdRepository.getHoldOwners(level, terminal);
            ServerPlayer owner = owners.isEmpty() ? null
                    : level.getServer().getPlayerList().getPlayer(owners.get(0));
            if (owner != null) {
                PlayerProgress op = PlayerProgress.get(owner);
                PlayerProgress.set(owner, op.withChunksUsed(Math.max(0, op.chunksUsed() - 1)));
                owner.sendOverlayMessage(Component.translatable("hold.coe.bind.removed")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    /** 该区块当前是否属于任一基地。 */
    public static boolean isBound(ServerLevel level, BlockPos pos) {
        return StrongholdRepository.findTerminalOwningChunk(level, new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4)) != null;
    }

    private static void fail(Player player, String key) {
        player.sendOverlayMessage(Component.translatable(key).withStyle(ChatFormatting.RED));
    }
}
