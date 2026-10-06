package com.cimdy.coe.stronghold.area;

import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.cimdy.coe.stronghold.energy.WorkerAIInjector;
import com.cimdy.coe.block.entity.TerminalBlockEntity;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.data.codec.stronghold.StrongholdGroup;
import com.cimdy.coe.data.codec.stronghold.SaveData;
import com.cimdy.coe.data.codec.stronghold.entity.StrongholdEntity;
import com.cimdy.coe.data.codec.stronghold.entity.Owner;
import com.cimdy.coe.data.codec.stronghold.entity.PlayerEntity;
import com.cimdy.coe.data.codec.stronghold.entity.SaveEntity;
import com.cimdy.coe.data.codec.stronghold.storage.TeleportTarget;
import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.ActiveTask;
import com.cimdy.coe.data.codec.stronghold.task.helper.state.StrongholdState;
import com.cimdy.coe.data.codec.stronghold.task.impl.CarryTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.stronghold.energy.StrongholdHelper.HoldRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.NeoForgeMod;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.UnaryOperator;

/**
 * 据点全局数据读写服务（SaveData / StrongholdGroup / Stronghold 的跨维度读写与查询）。
 * 从原 God Class {@link StrongholdHelper} 拆分而来，逻辑与原实现完全一致，仅做职责归并。
 */
public final class StrongholdRepository {
    public static final TicketType TICKET = NeoForgeMod.BLOCK_WITH_NATURAL_SPAWNING_TICKET.value();
    private static final String NO_NAME = "null";

    private StrongholdRepository() {
    }

    private static MinecraftServer getServer(ServerLevel level) {
        return level.getServer();
    }

    public static ServerLevel getLevelForDimension(ServerLevel currentLevel, ResourceKey<Level> dimension) {
        return getServer(currentLevel).getLevel(dimension);
    }

    // ==================== 全局数据读写（全维度同步） ====================

    public static SaveData getHoldData(Level level) {
        return level.getData(AttachmentRegistry.STRONGHOLD_DATA);
    }

    public static void setHoldData(ServerLevel level, SaveData data) {
        getServer(level).getAllLevels().forEach(l -> l.setData(AttachmentRegistry.STRONGHOLD_DATA, data));
    }

    // ==================== 终端方块实体访问 ====================

    @Nullable
    public static Stronghold getHoldFromBlockEntity(ServerLevel level, GlobalPos pos) {
        ServerLevel target = getLevelForDimension(level, pos.dimension());
        return target == null ? null : getHoldFromBlockEntity(target, pos.pos());
    }

    @Nullable
    public static Stronghold getHoldFromBlockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TerminalBlockEntity terminal)
            return terminal.getData(AttachmentRegistry.STRONGHOLD);
        return null;
    }

    public static void setHoldToBlockEntity(ServerLevel level, GlobalPos pos, Stronghold hold) {
        ServerLevel target = getLevelForDimension(level, pos.dimension());
        if (target == null) return;
        BlockEntity be = target.getBlockEntity(pos.pos());
        if (be instanceof TerminalBlockEntity terminal)
            terminal.setData(AttachmentRegistry.STRONGHOLD, hold);
    }

    public static void updateHoldAtPos(ServerLevel level, GlobalPos pos, UnaryOperator<Stronghold> updater) {
        Stronghold hold = getHoldFromBlockEntity(level, pos);
        if (hold != null) setHoldToBlockEntity(level, pos, updater.apply(hold));
    }

    public static void updateHoldAtPos(ServerLevel level, BlockPos pos, UnaryOperator<Stronghold> updater) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TerminalBlockEntity terminal) {
            Stronghold hold = terminal.getData(AttachmentRegistry.STRONGHOLD);
            terminal.setData(AttachmentRegistry.STRONGHOLD, updater.apply(hold));
        }
    }

    public static void updateHold(ServerLevel level, GlobalPos pos, Stronghold newHold) {
        updateHoldAtPos(level, pos, h -> newHold);
    }

    // ==================== 据点组查找 ====================

    public static Optional<StrongholdGroup> findHoldGroupContainingPos(Level level, GlobalPos pos) {
        SaveData data = getHoldData(level);
        for (StrongholdGroup group : data.groups().values())
            if (group.isPosInArea(level, pos)) return Optional.of(group);
        return Optional.empty();
    }

    public static Optional<StrongholdGroup> findHoldGroupContainingPos(Level level, BlockPos pos) {
        return findHoldGroupContainingPos(level, new GlobalPos(level.dimension(), pos));
    }

    // ==================== 据点查询 ====================

    public static Stronghold getHoldByEntity(ServerLevel level, Entity entity) {
        SaveData data = getHoldData(level);
        for (StrongholdGroup group : data.groups().values())
            for (GlobalPos pos : group.holds().values()) {
                Stronghold hold = getHoldFromBlockEntity(level, pos);
                if (hold != null && hold.entities().contains(entity.getUUID())) return hold;
            }
        return null;
    }

    public static Stronghold getHoldByEntity(Entity entity) {
        if (entity.level().isClientSide()) return null;
        return getHoldByEntity((ServerLevel) entity.level(), entity);
    }

    public static Stronghold getHoldAt(Level level, BlockPos pos) {
        return getHoldFromBlockEntity(level, pos);
    }

    @Nullable
    public static Stronghold getPosInHold(Level level, BlockPos pos) {
        SaveData data = getHoldData(level);
        for (StrongholdGroup group : data.groups().values()) {
            for (Map.Entry<String, GlobalPos> entry : group.holds().entrySet()) {
                GlobalPos holdPos = entry.getValue();
                if (!holdPos.dimension().equals(level.dimension())) continue;
                Stronghold hold = getHoldFromBlockEntity(level, holdPos.pos());
                if (hold == null) continue;
                if (StrongholdArea.contains(hold, holdPos.pos(), pos)) {
                    return hold;
                }
            }
        }
        return null;
    }

    @Nullable
    public static Stronghold getPosInHold(ServerLevel level, BlockPos pos) {
        SaveData data = getHoldData(level);
        for (StrongholdGroup group : data.groups().values()) {
            for (Map.Entry<String, GlobalPos> entry : group.holds().entrySet()) {
                GlobalPos holdPos = entry.getValue();
                ServerLevel level1 = getLevelForDimension(level, holdPos.dimension());
                if (level1 == null) continue;
                Stronghold hold = getHoldFromBlockEntity(level, holdPos);
                if (hold == null) continue;
                if (holdPos.dimension().equals(level.dimension())
                        && StrongholdArea.contains(hold, holdPos.pos(), pos)) {
                    return hold;
                }
            }
        }
        return null;
    }

    public static int getHoldLevel(Level level, BlockPos pos) {
        Stronghold hold = getHoldAt(level, pos);
        return hold != null ? hold.level() : 0;
    }

    public static String getHoldName(Level level, BlockPos pos) {
        GlobalPos gp = new GlobalPos(level.dimension(), pos);
        for (StrongholdGroup group : getHoldData(level).groups().values())
            for (var entry : group.holds().entrySet())
                if (entry.getValue().equals(gp)) return entry.getKey();
        return NO_NAME;
    }

    public static String getHoldGroupName(Level level, BlockPos pos) {
        GlobalPos gp = new GlobalPos(level.dimension(), pos);
        for (var entry : getHoldData(level).groups().entrySet())
            if (entry.getValue().holds().containsValue(gp)) return entry.getKey();
        return NO_NAME;
    }

    public static StrongholdGroup getHoldGroupAt(Level level, BlockPos pos) {
        GlobalPos gp = new GlobalPos(level.dimension(), pos);
        for (StrongholdGroup group : getHoldData(level).groups().values())
            if (group.holds().containsValue(gp)) return group;
        return null;
    }

    // ==================== 权限判断 ====================

    public static boolean isInHoldOwner(Player player, BlockPos terminalPos) {
        SaveData data = getHoldData(player.level());
        GlobalPos gp = new GlobalPos(player.level().dimension(), terminalPos);
        return data.isOwnerAtPos(player, gp);
    }

    public static boolean isPlayerInHold(Player player) {
        SaveData data = getHoldData(player.level());
        GlobalPos playerPos = new GlobalPos(player.level().dimension(), player.blockPosition());
        for (StrongholdGroup group : data.groups().values())
            if (group.isPosInArea(player.level(), playerPos)) return true;
        return false;
    }

    // ==================== 据点增删改 ====================

    public static void createHold(ServerLevel level, Player player, BlockPos pos) {
        GlobalPos gp = new GlobalPos(level.dimension(), pos);
        SaveData data = getHoldData(level).addHold(player, gp);
        setHoldData(level, data);
        // 新据点仅拥有终端所在的那一个区块，其余靠玩家用界桩逐块绑定拓展
        setHoldToBlockEntity(level, gp, Stronghold.of(0));
        StrongholdArea.forceLoad(level, new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
    }

    public static void deleteHold(ServerLevel level, Player player, BlockPos pos) {
        GlobalPos gp = new GlobalPos(level.dimension(), pos);
        Stronghold hold = getHoldAt(level, pos);
        if (hold != null) StrongholdArea.releaseAll(level, hold, pos);
        if (level.getBlockEntity(pos) instanceof TerminalBlockEntity terminal)
            terminal.removeData(AttachmentRegistry.STRONGHOLD);
        SaveData data = getHoldData(level).removeHold(player, gp);
        setHoldData(level, data);
    }

    /**
     * 据点等级变更。范围已与等级解耦（改由绑定区块决定），故不再增删区块票。
     */
    public static void updateHoldLevel(ServerLevel level, BlockPos pos, int newLevel) {
        updateHoldAtPos(level, new GlobalPos(level.dimension(), pos), h -> h.withLevel(newLevel));
    }

    /**
     * 绑定一个区块到据点（界桩放置时调用）。返回是否成功（重复绑定返回 false）。
     */
    public static boolean bindChunk(ServerLevel level, BlockPos terminalPos, ChunkPos chunk) {
        Stronghold hold = getHoldAt(level, terminalPos);
        if (hold == null || hold.isChunkBound(terminalPos, chunk)) return false;
        Stronghold updated = hold.withChunkBound(chunk);
        setHoldToBlockEntity(level, new GlobalPos(level.dimension(), terminalPos), updated);
        StrongholdArea.syncTickets(level, hold, updated, terminalPos);
        return true;
    }

    /**
     * 解绑一个区块（界桩被破坏时调用）。中心区块不可解绑。
     */
    public static boolean unbindChunk(ServerLevel level, BlockPos terminalPos, ChunkPos chunk) {
        Stronghold hold = getHoldAt(level, terminalPos);
        if (hold == null || !hold.boundChunks().contains(chunk)) return false;
        Stronghold updated = hold.withChunkUnbound(chunk);
        setHoldToBlockEntity(level, new GlobalPos(level.dimension(), terminalPos), updated);
        StrongholdArea.syncTickets(level, hold, updated, terminalPos);
        return true;
    }

    /**
     * 找出「该区块可并入」的据点：本维度内、且该区块与据点已有区块四邻相接。
     * 这保证基地始终是<b>连通</b>的不规则形状，而不是散落的孤岛。
     */
    @Nullable
    public static BlockPos findAdjacentHoldTerminal(ServerLevel level, ChunkPos chunk) {
        SaveData data = getHoldData(level);
        for (StrongholdGroup group : data.groups().values()) {
            for (GlobalPos holdPos : group.holds().values()) {
                if (!holdPos.dimension().equals(level.dimension())) continue;
                Stronghold hold = getHoldFromBlockEntity(level, holdPos.pos());
                if (hold == null) continue;
                for (ChunkPos owned : hold.chunksWithCenter(holdPos.pos())) {
                    int dx = Math.abs(owned.x() - chunk.x()), dz = Math.abs(owned.z() - chunk.z());
                    if (dx + dz == 1 || (dx == 0 && dz == 0)) return holdPos.pos();
                }
            }
        }
        return null;
    }

    /** 找出拥有该区块的据点终端坐标。 */
    @Nullable
    public static BlockPos findTerminalOwningChunk(ServerLevel level, ChunkPos chunk) {
        SaveData data = getHoldData(level);
        for (StrongholdGroup group : data.groups().values()) {
            for (GlobalPos holdPos : group.holds().values()) {
                if (!holdPos.dimension().equals(level.dimension())) continue;
                Stronghold hold = getHoldFromBlockEntity(level, holdPos.pos());
                if (hold != null && hold.isChunkBound(holdPos.pos(), chunk)) return holdPos.pos();
            }
        }
        return null;
    }

    public static void renameHold(ServerLevel level, BlockPos pos, String newName) {
        GlobalPos gp = new GlobalPos(level.dimension(), pos);
        SaveData data = getHoldData(level).updateHoldName(gp, newName);
        setHoldData(level, data);
    }

    // ==================== 实体管理 ====================

    public static void addEntityToHold(ServerLevel level, BlockPos pos, Entity entity) {
        updateHoldAtPos(level, new GlobalPos(level.dimension(), pos), h -> h.withEntityAdded(entity.getUUID()));
    }

    public static void removeEntityFromHold(ServerLevel level, BlockPos pos, UUID uuid) {
        updateHoldAtPos(level, new GlobalPos(level.dimension(), pos), h -> h.withEntityRemoved(uuid));
    }

    public static void transferEntity(Player player, BlockPos holdPos, LivingEntity living) {
        removeEntityFromHold((ServerLevel) player.level(), holdPos, living.getUUID());
        living.setData(AttachmentRegistry.ENTITY_DATA,
                living.getData(AttachmentRegistry.ENTITY_DATA).dropItem(living.level(), living.position()));
        storeEntityForPlayer(player, living);
    }

    public static List<UUID> getHoldOwners(Level level, BlockPos pos) {
        GlobalPos gp = new GlobalPos(level.dimension(), pos);
        for (StrongholdGroup group : getHoldData(level).groups().values())
            if (group.hasHoldAt(gp)) return group.owners().stream().map(Owner::uuid).toList();
        return List.of();
    }

    public static List<UUID> getSaveEntity(Level level, BlockPos pos) {
        Stronghold hold = getHoldAt(level, pos);
        return hold != null ? List.copyOf(hold.entities()) : List.of();
    }

    public static void storeEntityForPlayer(Player player, LivingEntity entity) {
        entity.setData(AttachmentRegistry.ENTITY_DATA, StrongholdEntity.of(player.getUUID()));
        player.setData(AttachmentRegistry.PLAYER_ENTITY,
                player.getData(AttachmentRegistry.PLAYER_ENTITY).addEntity(entity));
    }

    public static void releaseEntityForPlayer(ServerLevel serverLevel, Player player, UUID uuid) {
        player.setData(AttachmentRegistry.PLAYER_ENTITY,
                player.getData(AttachmentRegistry.PLAYER_ENTITY).releaseEntity(serverLevel, uuid));
    }

    public static List<SaveEntity> getEntityData(Player player) {
        return player.getData(AttachmentRegistry.PLAYER_ENTITY).entities();
    }

    /** 列出该玩家全部已标记为盟友的已存实体。 */
    public static List<SaveEntity> getAllies(Player player) {
        return player.getData(AttachmentRegistry.PLAYER_ENTITY).allies();
    }

    /** 该存档条目（按 NBT 内 UUID 定位）是否为盟友。 */
    public static boolean isStoredAlly(Player player, UUID uuid) {
        return player.getData(AttachmentRegistry.PLAYER_ENTITY).isAlly(uuid);
    }

    /** 该存档条目（按 NBT 内 UUID 定位）是否参与战斗（帕鲁式「工作 ↔ 战斗」开关，默认 true）。 */
    public static boolean isStoredCombatant(Player player, UUID uuid) {
        return player.getData(AttachmentRegistry.PLAYER_ENTITY).isCombatant(uuid);
    }

    public static boolean isAlly(Entity entity) {
        // 原 AttachmentRegistry.ALLY 已迁入 AdditionData（key=ally）
        return entity != null && AdditionData.getOrDefault(entity, AdditionKeys.ALLY, 0.0) != 0.0;
    }

    /**
     * 翻转「盟友」标记。作用对象按优先级二选一，二者互斥（同一实体不会既在据点里又在存档里），
     * 两条分支的<b>权限模型不同</b>：
     * <ol>
     *   <li><b>已部署</b>：UUID 在该终端 {@link Stronghold#entities()} 中且实体确实在世界里 → 改的是
     *       <b>据点共有的实体</b>，故要求操作者是本据点所有者，非所有者直接拒绝（否则任何人走到
     *       别人终端 5 格内就能把对方工人标记为盟友 → {@code canWork} 恒 false → 劳动力静默停摆）。
     *       通过校验后改实体附件 {@link AdditionKeys#ALLY}：下一轮派工因 {@code canWork=false}
     *       立即停止派新工，进行中的任务由 {@code validateTasks} 回退 PENDING；同时就地切换 AI
     *       （盟友→{@code WorkerAIInjector#toCompanion}、工人→{@code toWorker}），
     *       避免出现「不派工也不跟随」的死状态。</li>
     *   <li><b>已存</b>：实体在<b>操作者自己的</b>存档列表里，天然只能改自己的记录，无需所属校验
     *       —— 玩家站在谁的终端前管理自己的仓库都不该被拦。</li>
     * </ol>
     *
     * @return 是否命中并发生了改动（false = 非本据点所有者，或该 UUID 既不在此据点也不在此玩家存档里）
     */
    public static boolean toggleAlly(ServerLevel level, Player player, BlockPos terminalPos, UUID entityUuid) {
        Stronghold hold = getHoldAt(level, terminalPos);
        if (hold != null && hold.entities().contains(entityUuid)
                && level.getEntity(entityUuid) instanceof LivingEntity living) {
            if (!getHoldData(level).isOwnerAtPos(player, new GlobalPos(level.dimension(), terminalPos))) {
                return false;
            }
            // 原 AttachmentRegistry.ALLY 已迁入 AdditionData（key=ally）
            boolean nowAlly = AdditionData.getOrDefault(living, AdditionKeys.ALLY, 0.0) == 0.0;
            AdditionData.addOrReplaceData(living, AdditionKeys.ALLY, nowAlly ? 1.0 : 0.0);
            // 就地同步 AI：不跟随的盟友 = 死状态（不派工也不跟随，只在据点里游荡）。
            // 非 Mob 的 LivingEntity 跳过转换，保持现状（仅标记生效，仍被 canWork 排除）。
            if (living instanceof Mob mob) {
                if (nowAlly) WorkerAIInjector.toCompanion(mob, player);
                else WorkerAIInjector.toWorker(mob);
            }
            return true;
        }
        PlayerEntity store = player.getData(AttachmentRegistry.PLAYER_ENTITY);
        if (store.findByUuid(entityUuid).isEmpty()) return false;
        player.setData(AttachmentRegistry.PLAYER_ENTITY,
                store.withAlly(entityUuid, !store.isAlly(entityUuid)));
        return true;
    }

    /**
     * 翻转「战斗开关」（帕鲁式「工作 ↔ 战斗」）。权限模型与 {@link #toggleAlly} 完全一致：
     * 已部署分支校验据点所有权、就地重注入 AI；已存分支只动自身存档。
     *
     * @return 是否命中并发生了改动（false = 非本据点所有者，或该 UUID 既不在此据点也不在此玩家存档里）
     */
    public static boolean toggleCombatant(ServerLevel level, Player player, BlockPos terminalPos, UUID entityUuid) {
        Stronghold hold = getHoldAt(level, terminalPos);
        if (hold != null && hold.entities().contains(entityUuid)
                && level.getEntity(entityUuid) instanceof LivingEntity living) {
            if (!getHoldData(level).isOwnerAtPos(player, new GlobalPos(level.dimension(), terminalPos))) {
                return false;
            }
            // 原 AttachmentRegistry.COMBATANT 已迁入 AdditionData（key=combatant，默认 true）
            boolean nowCombatant = AdditionData.getOrDefault(living, AdditionKeys.COMBATANT, 1.0) == 0.0;
            AdditionData.addOrReplaceData(living, AdditionKeys.COMBATANT, nowCombatant ? 1.0 : 0.0);
            // 重新落地 AI：依当前 ALLY / COMBATANT 状态重注入（FactionDefendGoal 随 COMBATANT 增删）
            if (living instanceof Mob mob) {
                // 原 AttachmentRegistry.ALLY 已迁入 AdditionData（key=ally）
                if (AdditionData.getOrDefault(living, AdditionKeys.ALLY, 0.0) != 0.0)
                    WorkerAIInjector.injectDeployAI(mob);
                else WorkerAIInjector.injectStrongholdAI(mob);
            }
            return true;
        }
        PlayerEntity store = player.getData(AttachmentRegistry.PLAYER_ENTITY);
        if (store.findByUuid(entityUuid).isEmpty()) return false;
        player.setData(AttachmentRegistry.PLAYER_ENTITY,
                store.withCombatant(entityUuid, !store.isCombatant(entityUuid)));
        return true;
    }

    // ==================== 传送目标 / 生产站点 ====================

    public static List<TeleportTarget> getTeleportTargets(Level level, BlockPos pos) {
        List<TeleportTarget> targets = new ArrayList<>();
        GlobalPos current = new GlobalPos(level.dimension(), pos);
        StrongholdGroup group = getHoldGroupAt(level, pos);
        if (group == null) return targets;
        for (var entry : group.holds().entrySet()) {
            GlobalPos gp = entry.getValue();
            if (gp.dimension().equals(current.dimension()) && gp.pos().equals(current.pos())) continue;
            targets.add(new TeleportTarget(entry.getKey(), gp));
        }
        return targets;
    }

    public static Optional<Site> getProductionSite(ServerLevel level, BlockPos pos) {
        Stronghold hold = getPosInHold(level, pos);
        if (hold == null) return Optional.empty();
        return hold.holdState().getSite(pos);
    }

    // ==================== 实体据点引用 ====================

    public static HoldRef getHoldRefForEntity(Entity entity) {
        Level level = entity.level();
        BlockPos entityPos = entity.blockPosition();
        SaveData data = getHoldData(level);
        for (var group : data.groups().values()) {
            for (var entry : group.holds().entrySet()) {
                GlobalPos holdPos = entry.getValue();
                if (!holdPos.dimension().equals(level.dimension())) continue;
                Stronghold hold = getHoldFromBlockEntity(level, holdPos.pos());
                if (hold != null && StrongholdArea.contains(hold, holdPos.pos(), entityPos)) {
                    return new HoldRef(holdPos, hold);
                }
            }
        }
        return null;
    }

    public static GlobalPos getEntityHoldPos(Entity entity) {
        if (!entity.hasData(AttachmentRegistry.ENTITY_DATA)) return null;
        return entity.getData(AttachmentRegistry.ENTITY_DATA).pos();
    }

    /**
     * 跨维度查找活着的 LivingEntity。
     * 先在当前维度查找，找不到则根据活跃的 CarryTask 任务推断其可能出现的另一维度继续查找。
     */
    @Nullable
    public static LivingEntity findLivingEntity(ServerLevel level, UUID entityId) {
        Entity entity = level.getEntity(entityId);
        if (entity instanceof LivingEntity living && living.isAlive()) {
            return living;
        }

        SaveData data = getHoldData(level);
        for (StrongholdGroup group : data.groups().values()) {
            for (GlobalPos holdPos : group.holds().values()) {
                Stronghold hold = getHoldFromBlockEntity(level, holdPos);
                if (hold == null) continue;
                StrongholdState state = hold.holdState();

                List<ActiveTask> allActive = new ArrayList<>();
                for (List<ActiveTask> list : state.getActiveTasks().values()) {
                    allActive.addAll(list);
                }

                for (ActiveTask at : allActive) {
                    if (!(at.task() instanceof CarryTask carryTask)) continue;
                    ResourceKey<Level> sourceDimension = carryTask.getSourceTerminal().dimension();
                    ResourceKey<Level> targetDimension = carryTask.getTargetTerminal().dimension();
                    ResourceKey<Level> dimension = sourceDimension.equals(level.dimension())
                            ? targetDimension : sourceDimension;
                    ServerLevel otherLevel = level.getServer().getLevel(dimension);
                    if (otherLevel == null) continue;

                    Entity e = otherLevel.getEntity(entityId);
                    if (e instanceof LivingEntity living && living.isAlive()) {
                        return living;
                    }
                }
            }
        }

        return null;
    }
}
