package com.cimdy.coe.block.entity;

import com.cimdy.coe.registry.world.BlockEntityRegistry;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.data.codec.stronghold.storage.Detailed;
import com.cimdy.coe.data.codec.stronghold.storage.Summary;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockEntry;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockMode;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockSet;
import com.cimdy.coe.data.codec.stronghold.storage.stock.TransferEntry;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.helper.claim.*;
import com.cimdy.coe.data.codec.stronghold.task.helper.demand.*;
import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.*;
import com.cimdy.coe.data.codec.stronghold.task.helper.recipe.*;
import com.cimdy.coe.data.codec.stronghold.task.helper.result.*;
import com.cimdy.coe.data.codec.stronghold.task.helper.state.*;
import com.cimdy.coe.data.codec.stronghold.task.helper.supply.*;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.charge.FEChargeTask;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.charge.HeaterTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.stronghold.area.StrongholdArea;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.cimdy.coe.stronghold.energy.StrongholdHelper.TransportInfo;
import com.cimdy.coe.mixin.AbstractFurnaceBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.*;

public class TerminalBlockEntity extends BlockEntity {

    /**
     * 每轮扫描的区块数上限。据点范围已改为玩家绑定的不规则区块集合，
     * 不再有「半径」概念；此处仅作性能护栏，防止超大基地每 5 tick 全量扫描造成卡顿。
     * 超出部分按轮次滚动扫描（见 {@link #scanCursor}），保证所有区块最终都被覆盖。
     */
    private static final int MAX_CHUNKS_PER_SCAN = 16;
    private int tickCounter = 0;
    /** 滚动扫描游标：大型基地分多轮扫完全部绑定区块。 */
    private int scanCursor = 0;
    private static final int SCAN_INTERVAL = 5;

    /** 跨滚动窗口的扫描累加器：只有完成一整轮扫描才统一落盘，避免大基地面板抖动与跨窗供需丢失。 */
    private Detailed detailedAcc = Detailed.EMPTY;
    private final List<DemandInfo> demandAcc = new ArrayList<>();
    private final List<SupplyInfo> supplyAcc = new ArrayList<>();
    private final List<Task> posTaskAcc = new ArrayList<>();
    private int scanChunksScanned = 0;

    public TerminalBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.TERMINAL_BLOCK_ENTITY.get(), pos, blockState);
    }

    /** 终端 BE 加载时，把其绑定的全部区块（含中心）常驻加载，保障离线/远离时基地运转。 */
    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            Stronghold hold = getData(AttachmentRegistry.STRONGHOLD);
            if (hold != null) StrongholdArea.forceLoadAll(serverLevel, hold, this.worldPosition);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TerminalBlockEntity terminal) {
        if (level instanceof ServerLevel serverLevel) {
            terminal.tickCounter++;
            if (terminal.tickCounter >= SCAN_INTERVAL) {
                terminal.tickCounter = 0;
                terminal.scanAndUpdateInventories(serverLevel);
            }
        }
    }

    private void scanAndUpdateInventories(ServerLevel serverLevel) {
        // 更新实体列表、工作站点（方案 B 延伸：仅重算脏站点，全量兜底由 DemandLedger 控制）
        GlobalPos terminalPos = new GlobalPos(serverLevel.dimension(), this.worldPosition);
        this.setData(AttachmentRegistry.STRONGHOLD, this.getData(AttachmentRegistry.STRONGHOLD)
                .updateEntity(serverLevel).balanceAllSites(serverLevel, terminalPos));

        // 扫描范围 = 据点绑定的不规则区块集合（含中心区块），与等级完全解耦。
        // 大基地分多轮滚动扫描；本轮只扫一窗，但把结果累加进持久累加器，
        // 只有完成一整轮扫描后才统一落盘库存摘要并生成搬运任务，避免面板抖动与跨窗供需丢失。
        List<ChunkPos> owned = new ArrayList<>(
                this.getData(AttachmentRegistry.STRONGHOLD).chunksWithCenter(this.worldPosition));

        int total = owned.size();
        int count = Math.min(total, MAX_CHUNKS_PER_SCAN);
        boolean fullPassThisTick = total <= MAX_CHUNKS_PER_SCAN;

        // 新一轮完整扫描开始：重置累加器
        if (fullPassThisTick || this.scanChunksScanned == 0) {
            this.detailedAcc = Detailed.EMPTY;
            this.demandAcc.clear();
            this.supplyAcc.clear();
            this.posTaskAcc.clear();
            this.scanChunksScanned = 0;
        }

        for (int i = 0; i < count; i++) {
            ChunkPos cp = owned.get((this.scanCursor + i) % total);
            // 只扫已加载区块：绑定区块已由 ticket 常驻加载，未加载的说明尚未就绪，跳过即可
            LevelChunk chunk = serverLevel.getChunkSource().getChunkNow(cp.x(), cp.z());
            if (chunk == null) continue;
            ScanChunkResult result = scanChunk(chunk, serverLevel);
            this.detailedAcc = this.detailedAcc.merge(result.detailed());
            this.demandAcc.addAll(result.demands());
            this.supplyAcc.addAll(result.supplies());
            this.posTaskAcc.addAll(result.tasks());
        }
        this.scanChunksScanned += count;

        // 滚动游标：大型基地分多轮扫完全部绑定区块
        if (total > MAX_CHUNKS_PER_SCAN) {
            this.scanCursor = (this.scanCursor + count) % total;
        } else {
            this.scanCursor = 0;
        }

        // 未完成一整轮扫描则跳过库存落盘与任务生成（仍继续下方派发与平衡）
        boolean cycleComplete = fullPassThisTick || this.scanChunksScanned >= total;
        if (cycleComplete) {
        this.setData(AttachmentRegistry.DETAILED, this.detailedAcc);

        // 记录各站点的需求清单，供转化类工种（cook/smelt/compost/mill）按「产物需求」门控生成
        Stronghold demandHold = this.getData(AttachmentRegistry.STRONGHOLD);
        for (Site site : demandHold.holdState().sites()) {
            DemandLedger.setSiteDemands(site.getSitePos(), this.demandAcc);
        }

        // 生成搬运任务并保存到据点
        if (!this.demandAcc.isEmpty() || !this.supplyAcc.isEmpty() || !this.posTaskAcc.isEmpty()) {
            Stronghold hold = this.getData(AttachmentRegistry.STRONGHOLD);

            // 1. 先生成进食任务
            Map<UUID, List<Task>> eatAssignments = TaskGenerator.generateEatTasks(serverLevel, hold, this.supplyAcc);

            // 2. 用剩余供需生成搬运任务
            MatchResultWithTask result = TaskGenerator.matchAll(this.demandAcc, this.supplyAcc, terminalPos);
            StrongholdState newState = hold.holdState().withMatchResult(serverLevel, result);

            // 3. 生成合成/处理链任务（注册表驱动，覆盖所有原版配方类型）
            List<Task> craftTasks = TaskGenerator.generateCraftTasks(serverLevel, terminalPos, hold, this.demandAcc, this.supplyAcc);

            // 3b. 生成转化链任务（烹饪/冶炼/堆肥/研磨，与合成同构：搬运原料→站点转化→搬运产物）
            List<Task> convertTasks = TaskGenerator.generateConvertTasks(serverLevel, terminalPos, hold, this.demandAcc, this.supplyAcc);

            // 3c. 生成附魔应用链任务（使用附魔书给物品附魔，消耗青金石与末影能量）
            List<Task> enchantTasks = TaskGenerator.generateEnchantTasks(serverLevel, terminalPos, hold, this.demandAcc, this.supplyAcc);

            // 4. 追加 进食任务 去重任务 链任务
            newState = newState.appendActiveTasks(eatAssignments)
                    .appendPosTasks(serverLevel, this.posTaskAcc)
                    .appendNewTasks(serverLevel, craftTasks)
                    .appendNewTasks(serverLevel, convertTasks)
                    .appendNewTasks(serverLevel, enchantTasks);

            // 5. 写入终端
            Stronghold updatedHold = hold.withHoldState(newState);
            this.setData(AttachmentRegistry.STRONGHOLD, updatedHold);

            // 库存变化 → 标记本终端所有站点为脏，下一轮 balanceAllSites 重算工人分配
            for (Site site : updatedHold.holdState().sites()) {
                DemandLedger.markSiteDirty(terminalPos, site.getSitePos());
            }
        }
        }

        // 分发和更新已分发任务
        Stronghold hold = StrongholdHelper.getHoldAt(serverLevel, this.worldPosition);
        if (hold != null && hold.hasTask()) {
            TaskDispatcher.dispatchTasks(serverLevel,
                    new GlobalPos(serverLevel.dimension(), this.worldPosition), hold);
        }

        // 推进第五点：分配后再次平衡工人，消除「生成轨A在扫描前、分配在扫描后」带来的 ≤1 轮延迟。
        // dispatch 已将新分配的工人写入站点并标记站点脏，此处立即重算待处理任务量，使下一 tick 即可精准派工。
        Stronghold afterDispatch = StrongholdHelper.getHoldAt(serverLevel, this.worldPosition);
        if (afterDispatch != null) {
            this.setData(AttachmentRegistry.STRONGHOLD,
                    afterDispatch.balanceAllSites(serverLevel, terminalPos));
        }
    }

    private ScanChunkResult scanChunk(LevelChunk chunk, ServerLevel level) {
        Detailed detailed = Detailed.EMPTY;
        List<DemandInfo> demands = new ArrayList<>();
        List<SupplyInfo> supplies = new ArrayList<>();
        List<Task> tasks = new ArrayList<>();

        for (BlockPos pos : chunk.getBlockEntitiesPos()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) continue;

            // ---------- 物品处理 ----------
            ResourceHandler<ItemResource> itemHandler = level.getCapability(Capabilities.Item.BLOCK, pos, null);
            if (itemHandler != null) {
                BlockState state = chunk.getBlockState(pos);

                // 详细物品统计（左右分割显示）
                int detailStartSlot = 0;
                int detailEndSlot = itemHandler.size();

                if (be instanceof ChestBlockEntity chest && state.hasProperty(ChestBlock.TYPE)) {
                    ChestType type = state.getValue(ChestBlock.TYPE);
                    if (type == ChestType.LEFT) {
                        detailStartSlot = itemHandler.size() - chest.getContainerSize();
                        detailEndSlot = itemHandler.size();
                    } else if (type == ChestType.RIGHT) {
                        detailEndSlot = chest.getContainerSize();
                    }
                }

                for (int slot = detailStartSlot; slot < detailEndSlot; slot++) {
                    ItemResource resource = itemHandler.getResource(slot);
                    if (resource.isEmpty()) continue;
                    int amount = itemHandler.getAmountAsInt(slot);
                    detailed = detailed.add(resource, pos, slot, amount);
                }

                // 供需生成（跳过双箱子右侧）
                if (!(be instanceof ChestBlockEntity && state.hasProperty(ChestBlock.TYPE)
                        && state.getValue(ChestBlock.TYPE) == ChestType.RIGHT)) {
                    BlockEntity targetBe = StrongholdHelper.getCurrentBlockEntity(level, be);
                    StockSet stockSet = targetBe.getData(AttachmentRegistry.STOCK_SET);
                    targetBe.setData(AttachmentRegistry.STOCK_SET, stockSet.update(itemHandler));
                    collectEntries(itemHandler, stockSet, pos, demands, supplies, level);
                }
            }

            // ---------- 方块实体任务处理 ----------
            EnergyHandler energyHandler = level.getCapability(Capabilities.Energy.BLOCK, pos, null);
            if (energyHandler != null && energyHandler.getAmountAsInt() < energyHandler.getCapacityAsInt()) {
                tasks.add(new FEChargeTask(pos.immutable()));
            }

            if (be instanceof AbstractFurnaceBlockEntity furnace) {
                AbstractFurnaceBlockEntityAccessor accessor = (AbstractFurnaceBlockEntityAccessor) furnace;
                if (accessor.getLitTimeRemaining() == 0)
                    tasks.add(new HeaterTask(pos.immutable()));
            }
        }

        return new ScanChunkResult(detailed, demands, supplies, tasks);
    }

    private void collectEntries(ResourceHandler<ItemResource> handler, StockSet stockSet,
                                BlockPos containerPos, List<DemandInfo> demands, List<SupplyInfo> supplies,
                                ServerLevel level) {
        Map<ItemResource, StrongholdHelper.TransportInfo> transportMap = getTransportMap(level, containerPos, stockSet);

        Summary summary = Summary.EMPTY;
        for (int slot = 0; slot < handler.size(); slot++) {
            ItemResource resource = handler.getResource(slot);
            if (resource.isEmpty()) continue;
            int amount = handler.getAmountAsInt(slot);
            summary = summary.add(resource, amount);
        }

        List<TransferEntry> transferEntries = summary.getTransfer(stockSet, transportMap);

        for (TransferEntry entry : transferEntries) {
            if (entry.mode() == StockMode.DEMAND) {
                demands.add(new DemandInfo(entry.resource(), containerPos, entry.range(),
                        entry.amount(), entry.minTransfer(), stockSet.priority()));
            } else if (entry.mode() == StockMode.SUPPLY) {
                supplies.add(new SupplyInfo(entry.resource(), containerPos, entry.range(),
                        entry.amount(), entry.minTransfer(), stockSet.priority()));
            }
        }
    }

    private Map<ItemResource, StrongholdHelper.TransportInfo> getTransportMap(ServerLevel level, BlockPos containerPos, StockSet stockSet) {
        Map<ItemResource, StrongholdHelper.TransportInfo> map = new HashMap<>();
        for (StockEntry entry : stockSet.entries()) {
            if (!entry.resource().isEmpty()) {
                TransportInfo info = StrongholdHelper.getTransportInfo(level, containerPos, entry.resource());
                map.put(entry.resource(), info);
            }
        }
        return map;
    }

    private record ScanChunkResult(Detailed detailed, List<DemandInfo> demands, List<SupplyInfo> supplies, List<Task> tasks) {}
}