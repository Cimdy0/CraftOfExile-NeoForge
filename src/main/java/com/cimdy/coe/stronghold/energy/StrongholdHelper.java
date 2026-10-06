package com.cimdy.coe.stronghold.energy;

import com.cimdy.coe.stronghold.area.AreaScanner;
import com.cimdy.coe.stronghold.area.StrongholdRepository;
import com.cimdy.coe.stronghold.transport.TeleportCoordinator;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.data.codec.stronghold.StrongholdGroup;
import com.cimdy.coe.data.codec.stronghold.SaveData;
import com.cimdy.coe.data.codec.stronghold.entity.SaveEntity;
import com.cimdy.coe.data.codec.stronghold.storage.TeleportTarget;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockEntry;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockSet;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * 据点系统门面（Facade）。
 * <p>
 * 原 {@code StrongholdHelper} 是一个混杂 5 类职责、约 700 行的 God Class。现已拆分为 5 个单一职责服务：
 * <ul>
 *   <li>{@link StrongholdRepository} — 全局数据读写（SaveData / StrongholdGroup / Stronghold）</li>
 *   <li>{@link EnderEnergyLedger} — 末影能量账本</li>
 *   <li>{@link TeleportCoordinator} — 跨维传送与容器几何</li>
 *   <li>{@link WorkerAIInjector} — 工人实体生成与 AI 注入</li>
 *   <li>{@link AreaScanner} — 区域扫描与实体状态（SAN / 饥饿）</li>
 * </ul>
 * 本类仅保留旧静态 API 并委托给上述服务，行为与原实现完全一致，供渐进式迁移期间调用方零改动使用。
 */
public final class StrongholdHelper {
    public static final TicketType TICKET = StrongholdRepository.TICKET;
    public static final int ENERGY_PER_DIMENSION = EnderEnergyLedger.ENERGY_PER_DIMENSION;
    public static final int ENERGY_PER_GLOBAL = EnderEnergyLedger.ENERGY_PER_GLOBAL;

    private StrongholdHelper() {
    }

    // ==================== 跨维度 Level 获取 ====================

    public static ServerLevel getLevelForDimension(ServerLevel currentLevel, ResourceKey<Level> dimension) {
        return StrongholdRepository.getLevelForDimension(currentLevel, dimension);
    }

    // ==================== 全局数据读写 ====================

    public static SaveData getHoldData(Level level) {
        return StrongholdRepository.getHoldData(level);
    }

    public static void setHoldData(ServerLevel level, SaveData data) {
        StrongholdRepository.setHoldData(level, data);
    }

    // ==================== 终端方块实体访问 ====================

    @Nullable
    public static Stronghold getHoldFromBlockEntity(ServerLevel level, GlobalPos pos) {
        return StrongholdRepository.getHoldFromBlockEntity(level, pos);
    }

    @Nullable
    public static Stronghold getHoldFromBlockEntity(Level level, BlockPos pos) {
        return StrongholdRepository.getHoldFromBlockEntity(level, pos);
    }

    public static void setHoldToBlockEntity(ServerLevel level, GlobalPos pos, Stronghold hold) {
        StrongholdRepository.setHoldToBlockEntity(level, pos, hold);
    }

    public static void updateHoldAtPos(ServerLevel level, GlobalPos pos, UnaryOperator<Stronghold> updater) {
        StrongholdRepository.updateHoldAtPos(level, pos, updater);
    }

    public static void updateHoldAtPos(ServerLevel level, BlockPos pos, UnaryOperator<Stronghold> updater) {
        StrongholdRepository.updateHoldAtPos(level, pos, updater);
    }

    public static void updateHold(ServerLevel level, GlobalPos pos, Stronghold newHold) {
        StrongholdRepository.updateHold(level, pos, newHold);
    }

    // ==================== 据点组查找 ====================

    public static Optional<StrongholdGroup> findHoldGroupContainingPos(Level level, GlobalPos pos) {
        return StrongholdRepository.findHoldGroupContainingPos(level, pos);
    }

    public static Optional<StrongholdGroup> findHoldGroupContainingPos(Level level, BlockPos pos) {
        return StrongholdRepository.findHoldGroupContainingPos(level, pos);
    }

    // ==================== 末影能量管理 ====================

    public static boolean addEnderEnergy(ServerLevel level, BlockPos pos, double amount) {
        return EnderEnergyLedger.addEnderEnergy(level, pos, amount);
    }

    public static boolean reduceEnderEnergy(ServerLevel level, BlockPos pos, double amount) {
        return EnderEnergyLedger.reduceEnderEnergy(level, pos, amount);
    }

    public static boolean modifyEnderEnergy(ServerLevel level, GlobalPos pos, double delta) {
        return EnderEnergyLedger.modifyEnderEnergy(level, pos, delta);
    }

    public static double getHoldEnergy(Level level, GlobalPos pos) {
        return EnderEnergyLedger.getHoldEnergy(level, pos);
    }

    public static double getHoldEnergy(Level level, BlockPos pos) {
        return EnderEnergyLedger.getHoldEnergy(level, pos);
    }

    public static boolean hasEnderEnergy(Level level, GlobalPos pos, double amount) {
        return EnderEnergyLedger.hasEnderEnergy(level, pos, amount);
    }

    public static boolean hasEnderEnergy(Level level, BlockPos pos, double amount) {
        return EnderEnergyLedger.hasEnderEnergy(level, pos, amount);
    }

    public static double getEnergyCost(ResourceKey<Level> dim1, ResourceKey<Level> dim2) {
        return EnderEnergyLedger.getEnergyCost(dim1, dim2);
    }

    /** 距离相关计费（带坐标；显示与扣费必须同源用此签名）。 */
    public static double getEnergyCost(GlobalPos from, GlobalPos to) {
        return EnderEnergyLedger.getEnergyCost(from, to);
    }

    // ==================== 传送目标列表 ====================

    public static List<TeleportTarget> getTeleportTargets(Level level, BlockPos pos) {
        return StrongholdRepository.getTeleportTargets(level, pos);
    }

    // ==================== 据点查询 ====================

    public static Stronghold getHoldByEntity(ServerLevel level, Entity entity) {
        return StrongholdRepository.getHoldByEntity(level, entity);
    }

    public static Stronghold getHoldByEntity(Entity entity) {
        return StrongholdRepository.getHoldByEntity(entity);
    }

    public static Stronghold getHoldAt(Level level, BlockPos pos) {
        return StrongholdRepository.getHoldAt(level, pos);
    }

    @Nullable
    public static Stronghold getPosInHold(Level level, BlockPos pos) {
        return StrongholdRepository.getPosInHold(level, pos);
    }

    @Nullable
    public static Stronghold getPosInHold(ServerLevel level, BlockPos pos) {
        return StrongholdRepository.getPosInHold(level, pos);
    }

    public static int getHoldLevel(Level level, BlockPos pos) {
        return StrongholdRepository.getHoldLevel(level, pos);
    }

    public static String getHoldName(Level level, BlockPos pos) {
        return StrongholdRepository.getHoldName(level, pos);
    }

    public static String getHoldGroupName(Level level, BlockPos pos) {
        return StrongholdRepository.getHoldGroupName(level, pos);
    }

    public static StrongholdGroup getHoldGroupAt(Level level, BlockPos pos) {
        return StrongholdRepository.getHoldGroupAt(level, pos);
    }

    // ==================== 权限判断 ====================

    public static boolean isInHoldOwner(Player player, BlockPos terminalPos) {
        return StrongholdRepository.isInHoldOwner(player, terminalPos);
    }

    public static boolean isPlayerInHold(Player player) {
        return StrongholdRepository.isPlayerInHold(player);
    }

    public static boolean isLookAtContainer(Player player) {
        return AreaScanner.isLookAtContainer(player);
    }

    // ==================== 据点增删改 ====================

    public static void createHold(ServerLevel level, Player player, BlockPos pos) {
        StrongholdRepository.createHold(level, player, pos);
    }

    public static void deleteHold(ServerLevel level, Player player, BlockPos pos) {
        StrongholdRepository.deleteHold(level, player, pos);
    }

    public static void updateHoldLevel(ServerLevel level, BlockPos pos, int newLevel) {
        StrongholdRepository.updateHoldLevel(level, pos, newLevel);
    }

    public static void renameHold(ServerLevel level, BlockPos pos, String newName) {
        StrongholdRepository.renameHold(level, pos, newName);
    }

    // ==================== 实体管理 ====================

    public static void addEntityToHold(ServerLevel level, BlockPos pos, Entity entity) {
        StrongholdRepository.addEntityToHold(level, pos, entity);
    }

    public static void removeEntityFromHold(ServerLevel level, BlockPos pos, UUID uuid) {
        StrongholdRepository.removeEntityFromHold(level, pos, uuid);
    }

    public static void transferEntity(Player player, BlockPos holdPos, LivingEntity living) {
        StrongholdRepository.transferEntity(player, holdPos, living);
    }

    public static List<UUID> getHoldOwners(Level level, BlockPos pos) {
        return StrongholdRepository.getHoldOwners(level, pos);
    }

    public static List<UUID> getSaveEntity(Level level, BlockPos pos) {
        return StrongholdRepository.getSaveEntity(level, pos);
    }

    public static void storeEntityForPlayer(Player player, LivingEntity entity) {
        StrongholdRepository.storeEntityForPlayer(player, entity);
    }

    public static void releaseEntityForPlayer(ServerLevel serverLevel, Player player, UUID uuid) {
        StrongholdRepository.releaseEntityForPlayer(serverLevel, player, uuid);
    }

    public static List<SaveEntity> getEntityData(Player player) {
        return StrongholdRepository.getEntityData(player);
    }

    // ==================== 盟友（随行伙伴） ====================

    public static List<SaveEntity> getAllies(Player player) {
        return StrongholdRepository.getAllies(player);
    }

    public static boolean isStoredAlly(Player player, UUID uuid) {
        return StrongholdRepository.isStoredAlly(player, uuid);
    }

    public static boolean isStoredCombatant(Player player, UUID uuid) {
        return StrongholdRepository.isStoredCombatant(player, uuid);
    }

    public static boolean isAlly(Entity entity) {
        return StrongholdRepository.isAlly(entity);
    }

    public static boolean toggleAlly(ServerLevel level, Player player, BlockPos terminalPos, UUID entityUuid) {
        return StrongholdRepository.toggleAlly(level, player, terminalPos, entityUuid);
    }

    // ==================== 实体生成与 AI 注入 ====================

    public static CustomData entityToCustomData(Entity entity) {
        return WorkerAIInjector.entityToCustomData(entity);
    }

    public static Entity createEntityFromData(Level level, CustomData data) {
        return WorkerAIInjector.createEntityFromData(level, data);
    }

    public static void spawnPlayerEntity(CustomData data, ServerLevel serverLevel, Player player, BlockPos pos) {
        WorkerAIInjector.spawnPlayerEntity(data, serverLevel, player, pos);
    }

    /** 已部署实体加入世界时的 AI 落地（盟友 → 跟随，工人 → 据点 AI），见 {@link WorkerAIInjector#injectDeployAI}。 */
    public static void injectDeployAI(Mob mob) {
        WorkerAIInjector.injectDeployAI(mob);
    }

    /**
     * 判定给定存储容器列表是否「还能继续存储物品」。
     * <p>
     * 基于站点容器的 {@link StockSet} 配置做容量门控：任一已配置物品 {@code total < max}（尚有剩余上限）
     * 即视为有空间；此外若容器本身存在空槽位（可承接未显式配置的新物品）也视为有空间。
     * 无任何物品能力（如纯能量容器）的方块直接跳过，不阻塞物品采集任务。
     * 用于采集类工作任务的「存储容器满则停摆」判定，避免产物滞留工人背包。
     */
    public static boolean storageHasRoom(ServerLevel level, List<BlockPos> storages) {
        if (storages == null || storages.isEmpty()) return true; // 无存储约束，视为可继续
        for (BlockPos pos : storages) {
            if (!level.isLoaded(pos)) continue;
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) continue;
            ResourceHandler<ItemResource> handler =
                    level.getCapability(Capabilities.Item.BLOCK, pos, null);
            if (handler == null) continue; // 无物品能力（能量容器等），跳过

            StockSet stockSet = be.getData(AttachmentRegistry.STOCK_SET);
            // 任一已配置物品未达上限 → 有空间
            for (StockEntry e : stockSet.entries()) {
                if (!e.resource().isEmpty() && e.total() < e.max()) return true;
            }
            // 容器存在空槽 → 可承接未配置的新物品 → 有空间
            for (int slot = 0; slot < handler.size(); slot++) {
                if (handler.getResource(slot).isEmpty()) return true;
            }
        }
        return false; // 全部容器均满
    }

    // ==================== SAN / 饥饿 ====================

    public static double getSan(Entity entity) {
        return AreaScanner.getSan(entity);
    }

    public static void modifySan(Entity entity, double delta) {
        AreaScanner.modifySan(entity, delta);
    }

    public static double getHunger(Entity entity) {
        return AreaScanner.getHunger(entity);
    }

    public static void modifyHunger(Entity entity, double delta) {
        AreaScanner.modifyHunger(entity, delta);
    }

    // ==================== 容器坐标辅助 ====================

    public static Optional<Site> getProductionSite(ServerLevel level, BlockPos pos) {
        return StrongholdRepository.getProductionSite(level, pos);
    }

    public static int countItemsInContainer(ResourceHandler<ItemResource> handler, ItemResource target) {
        return TeleportCoordinator.countItemsInContainer(handler, target);
    }

    public static BlockEntity getRepresentativeBlockEntity(ServerLevel level, BlockPos pos) {
        return TeleportCoordinator.getRepresentativeBlockEntity(level, pos);
    }

    public static BlockPos getEffectiveContainerPos(Level level, BlockPos pos) {
        return TeleportCoordinator.getEffectiveContainerPos(level, pos);
    }

    public static BlockEntity getCurrentBlockEntity(Level level, BlockEntity blockEntity) {
        return TeleportCoordinator.getCurrentBlockEntity(level, blockEntity);
    }

    public static TransportInfo getTransportInfo(ServerLevel level, BlockPos containerPos, ItemResource resource) {
        return TeleportCoordinator.getTransportInfo(level, containerPos, resource);
    }

    public static BlockPos resolveStorageContainer(ServerLevel level, BlockPos pos) {
        return TeleportCoordinator.resolveStorageContainer(level, pos);
    }

    // ==================== 实体据点引用 ====================

    @Nullable
    public static LivingEntity findLivingEntity(ServerLevel level, UUID entityId) {
        return StrongholdRepository.findLivingEntity(level, entityId);
    }

    public static GlobalPos getEntityHoldPos(Entity entity) {
        return StrongholdRepository.getEntityHoldPos(entity);
    }

    public static HoldRef getHoldRefForEntity(Entity entity) {
        return StrongholdRepository.getHoldRefForEntity(entity);
    }

    /** 传送信息：某容器的在途流出 / 流入量。 */
    public record TransportInfo(int outgoing, int incoming) {
        public static final TransportInfo EMPTY = new TransportInfo(0, 0);
    }

    /** 实体与其所属据点坐标的轻量引用（供 UI / 交互层使用）。 */
    public record HoldRef(GlobalPos pos, Stronghold hold) {
    }
}
