package com.cimdy.coe.event.world;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.world.BlockRegistry;
import com.cimdy.coe.block.custom.TerminalBlock;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.stronghold.StrongholdGroup;
import com.cimdy.coe.data.codec.stronghold.SaveData;
import com.cimdy.coe.data.codec.stronghold.task.helper.claim.DataUpdate;
import com.cimdy.coe.data.codec.stronghold.task.helper.demand.DemandLedger;
import com.cimdy.coe.stronghold.transport.ChunkBindingHandler;
import com.cimdy.coe.stronghold.area.StrongholdArea;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = CraftOfExile.MODID)
public class StrongholdEvent {
    @SubscribeEvent
    public static void tick(ServerTickEvent.Pre event) {
        DataUpdate.tick(event.getServer().getLevel(Level.OVERWORLD));
    }

    @SubscribeEvent
    public static void addLoadChunk(ServerStartedEvent event) {
        Iterable<ServerLevel> levels = event.getServer().getAllLevels();
        SaveData data = levels.iterator().next().getData(AttachmentRegistry.STRONGHOLD_DATA);
        data.groups().forEach((groupName, group) -> {
            group.holds().forEach((holdName, pos) -> {
                ServerLevel serverLevel = event.getServer().getLevel(pos.dimension());
                if (serverLevel == null) return;
                // 启动只加载终端所在区块（radius 0），其余绑定区块由终端 BE 加载时经 StrongholdArea.forceLoadAll 加载，
                // 避免旧半径模型一次性强载 7×7 区块且票不对称泄漏。
                StrongholdArea.forceLoad(serverLevel, ChunkPos.containing(pos.pos()));
            });
        });
    }

    @SubscribeEvent
    public static void HoldEntityJoinLevelEvent(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Mob mob
                && mob.level() instanceof ServerLevel serverLevel
        ) {
            if (mob.hasData(AttachmentRegistry.ENTITY_DATA)) {
                // 盟友不放据点 AI（那套 AI 会把实体拴在据点里），只保证跟随目标在位
                StrongholdHelper.injectDeployAI(mob);
            }

        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void cancelPlaceEvent(BlockEvent.EntityPlaceEvent event) {
        BlockState placedState = event.getPlacedBlock();
        Entity entity = event.getEntity();
        BlockPos pos = event.getPos();
        boolean isCanceled = event.isCanceled();
        if (!isCanceled && placedState.is(BlockRegistry.TERMINAL)
                && entity instanceof Player player
                && entity.level() instanceof ServerLevel serverLevel) {
            if (!StrongholdHelper.isPlayerInHold(player)) {
                StrongholdHelper.createHold(serverLevel, player, pos);
            } else {
                isCanceled = true;
            }
        }

        // 界桩：放置即尝试把所在区块并入相邻基地。
        // 绑定失败（不连通 / 超预算 / 无权限）则连带取消放置——界桩不被白白消耗，
        // 玩家拿回材料，同时已由 ChunkBindingHandler 收到失败原因提示。
        if (!isCanceled && placedState.is(BlockRegistry.STRONGHOLD_MARKER)
                && entity instanceof Player player
                && entity.level() instanceof ServerLevel serverLevel) {
            if (!ChunkBindingHandler.tryBind(serverLevel, player, pos)) {
                isCanceled = true;
            }
        }

        event.setCanceled(isCanceled);
    }

    @SubscribeEvent
    public static void StrongHoldBreakEvent(BreakBlockEvent event) { // 注意事件类名可能需要调整
        BlockState breakState = event.getState();
        Player player = event.getPlayer();
        BlockPos pos = event.getPos();

        if (breakState.is(BlockRegistry.TERMINAL) && player.level() instanceof ServerLevel serverLevel) {
            StrongholdGroup group = StrongholdHelper.getHoldGroupAt(player.level(), pos);
            if (group == null || !group.isOwner(player)) {
                event.setCanceled(true); // 不是所有者，禁止破坏
            } else {
                // 所有者破坏终端，手动清理据点数据
                StrongholdHelper.deleteHold(serverLevel, player, pos);
            }
        }

        // 界桩被拆：解绑其所在区块并退还预算（中心区块不受影响，见 ChunkBindingHandler）
        if (breakState.is(BlockRegistry.STRONGHOLD_MARKER) && player.level() instanceof ServerLevel serverLevel) {
            ChunkBindingHandler.unbind(serverLevel, player, pos);
        }
    }

    @SubscribeEvent
    public static void StrongHoldDropEvent(BlockDropsEvent event) {
        BlockState dropState = event.getState();
        BlockPos pos = event.getPos();
        boolean isCanceled = event.isCanceled();
        Entity entity = event.getBreaker();
        if (!isCanceled && dropState.is(BlockRegistry.TERMINAL)
                && event.getLevel() instanceof ServerLevel serverLevel) {
            ItemStack stack = BlockRegistry.TERMINAL_ITEM.get().getDefaultInstance();
            stack.set(DataComponentRegistry.LEVEL, dropState.getValueOrElse(TerminalBlock.LEVEL, 1));

            List<ItemEntity> drops = event.getDrops();
            boolean hasTerminal = false;
            for (int i = 0, dropsSize = drops.size(); i < dropsSize; i++) {
                ItemEntity item = drops.get(i);
                if (item.getItem().is(BlockRegistry.TERMINAL_ITEM)) {
                    item.setItem(stack);
                    event.getDrops().set(i, item);
                    hasTerminal = true;
                    break;
                }
            }
            if (!hasTerminal) {
                drops.add(new ItemEntity(serverLevel, pos.getX(), pos.getY(), pos.getZ(), stack));
            }
        }
    }

    @SubscribeEvent
    public static void BlockEntityChangedEvent(BlockEntityChangedEvent event) {
        BlockEntity blockEntity = event.getBlockEntity();
        if (blockEntity == null) return;
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide()) return;
        BlockPos pos = blockEntity.getBlockPos();
        // 据点相关容器/站点方块变化（如玩家向箱子放入/取出物品、放置站点方块）→ 标记该据点组
        // 所有终端为脏，使下一轮 DataUpdate 立即重算供需，无需等待全量兜底扫描（FULL_SCAN_EVERY=5s）。
        StrongholdHelper.findHoldGroupContainingPos(level, pos).ifPresent(group -> {
            for (GlobalPos terminal : group.holds().values()) {
                DemandLedger.markHoldDirty(terminal);
            }
        });
    }
}
