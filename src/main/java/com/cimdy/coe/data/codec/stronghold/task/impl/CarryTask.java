package com.cimdy.coe.data.codec.stronghold.task.impl;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.ItemCount;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockEntry;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockSet;
import com.cimdy.coe.data.codec.stronghold.task.BasicTask;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.cimdy.coe.data.codec.stronghold.task.TransportTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.CarryStage;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Optional;

public class CarryTask extends BasicTask.Move implements TransportTask {
    public static final Codec<CarryTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, CarryTask> STREAM_CODEC;
    private final GlobalPos sourceTerminal;
    private final GlobalPos targetTerminal;
    private CarryStage stage;

    private ItemResource resource;
    private int amount;
    private boolean extracted;
    private boolean started;

    // ==================== 构造器 ====================

    public CarryTask(ItemCount item, BlockPos sourceContainer, BlockPos targetContainer,
                     GlobalPos sourceTerminal, GlobalPos targetTerminal) {
        this(Optional.empty(), item, false, false, sourceContainer, targetContainer, sourceTerminal, targetTerminal, CarryStage.GO_TO_SOURCE);
    }

    public CarryTask(Optional<Task> next, ItemCount item, boolean extracted, boolean started,
                     BlockPos sourceContainer, BlockPos targetContainer,
                     GlobalPos sourceTerminal, GlobalPos targetTerminal, CarryStage stage) {
        super(next, item, sourceContainer, targetContainer);
        this.resource = item.resource();
        this.amount = item.amount();
        this.extracted = extracted;
        this.started = started;
        this.sourceTerminal = sourceTerminal;
        this.targetTerminal = targetTerminal;
        this.stage = stage;
    }

    @Override
    public Task split(int take) {
        if (take <= 0 || amount <= 0) return null;
        int taken = Math.min(take, amount);
        amount -= taken;
        return new CarryTask(getOptionNext(), new ItemCount(resource, taken), false, false,
                getSourcePos(), getTargetPos(), sourceTerminal, targetTerminal, this.stage);
    }

    @Override public boolean canFallback() { return isBeforePickup(); }

    // ==================== 有效坐标解析（生产站点支持） ====================

    /**
     * 如果源坐标是生产站点，返回其第一个已加载的存储容器，否则返回原坐标。
     */
    public BlockPos getResolvedSourcePos(ServerLevel level) {
        return StrongholdHelper.resolveStorageContainer(level, getSourcePos());
    }

    /**
     * 如果目标坐标是生产站点，返回其第一个已加载的存储容器，否则返回原坐标。
     */
    public BlockPos getResolvedTargetPos(ServerLevel level) {
        return StrongholdHelper.resolveStorageContainer(level, getTargetPos());
    }

    /**
     * 根据当前阶段，返回实体应该前往的目标位置。
     * 生产站点在取/送货阶段会被解析为实际容器坐标。
     */
    @Override
    public BlockPos getTargetPositionForEntity(LivingEntity entity) {
        ServerLevel level = (ServerLevel) entity.level();
        return switch (stage) {
            case GO_TO_SOURCE -> getResolvedSourcePos(level);
            case GO_TO_TARGET -> getResolvedTargetPos(level);
            case GO_TO_OTHER_TERMINAL -> sourceTerminal.pos();
            case RETURN_OTHER_TERMINAL -> targetTerminal.pos();
        };
    }

    private ServerLevel getSourceLevel(ServerLevel currentLevel) {
        return StrongholdHelper.getLevelForDimension(currentLevel, sourceTerminal.dimension());
    }

    private ServerLevel getTargetLevel(ServerLevel currentLevel) {
        return StrongholdHelper.getLevelForDimension(currentLevel, targetTerminal.dimension());
    }

    @Override
    public boolean validate(ServerLevel level) {
        int requiredAmount = this.getAmount();
        ItemResource resource = this.getResource();

        // 传送阶段检查能量
        if (stage == CarryStage.GO_TO_OTHER_TERMINAL) {
            if (!StrongholdHelper.hasEnderEnergy(level, getSourceTerminal().pos(), getEnergyCost() * 2)) {
                return false;
            }
        }

        // 取货前检查
        if (!this.isExtracted() && (stage == CarryStage.GO_TO_SOURCE || stage == CarryStage.GO_TO_OTHER_TERMINAL)) {
            ServerLevel sourceLevel = getSourceLevel(level);
            if (sourceLevel == null) return false;
            BlockPos sourcePos = getResolvedSourcePos(sourceLevel);
            return canExtract(sourceLevel, sourcePos, resource, requiredAmount);
        }

        // 送货前检查
        if (this.isExtracted() && (stage == CarryStage.GO_TO_TARGET || stage == CarryStage.RETURN_OTHER_TERMINAL)) {
            ServerLevel targetLevel = getTargetLevel(level);
            if (targetLevel == null) return false;
            BlockPos targetPos = getResolvedTargetPos(targetLevel);
            return canInsert(targetLevel, targetPos, resource, requiredAmount);
        }

        return true;
    }

    private boolean canExtract(ServerLevel level, BlockPos pos, ItemResource resource, int amount) {
        return StrongholdHelper.getProductionSite(level, pos).isPresent()
                || hasEnoughForPickup(level, pos, resource, amount);
    }

    private boolean canInsert(ServerLevel level, BlockPos pos, ItemResource resource, int amount) {
        return StrongholdHelper.getProductionSite(level, pos).isPresent()
                ? checkSiteStorages(level, pos, resource, amount, false)
                : validateTargetContainer(level, pos, resource, amount);
    }

    private boolean checkSiteStorages(ServerLevel level, BlockPos pos, ItemResource resource, int amount, boolean forPickup) {
        return StrongholdHelper.getProductionSite(level, pos)
                .map(site -> site.getStorages().stream()
                        .filter(level::isLoaded)
                        .anyMatch(storagePos -> forPickup
                                ? hasEnoughForPickup(level, storagePos, resource, amount)
                                : hasSpaceForDelivery(level, storagePos, resource, amount)))
                .orElse(false);
    }

    // ==================== 库存检查 ====================

    private boolean hasEnoughForPickup(ServerLevel level, BlockPos pos, ItemResource resource, int amount) {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, null);
        if (handler == null) return false;

        int physical = StrongholdHelper.countItemsInContainer(handler, resource);
        StrongholdHelper.TransportInfo info = StrongholdHelper.getTransportInfo(level, pos, resource);
        int available = physical - Math.max(0, info.outgoing() - amount) + info.incoming();
        return available >= amount;
    }

    private boolean validateTargetContainer(ServerLevel level, BlockPos pos, ItemResource resource, int amount) {
        BlockEntity rep = StrongholdHelper.getRepresentativeBlockEntity(level, pos);
        if (rep == null) return false;

        StockSet stockSet = rep.getData(AttachmentRegistry.STOCK_SET);
        if (stockSet.isEmpty()) return false;

        StockEntry entry = stockSet.entries().stream()
                .filter(e -> e.resource().equals(resource))
                .findFirst().orElse(null);
        if (entry == null || !entry.isDemand()) return false;

        int max = entry.max();
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, null);
        if (handler == null) return false;

        int physical = StrongholdHelper.countItemsInContainer(handler, resource);
        StrongholdHelper.TransportInfo info = StrongholdHelper.getTransportInfo(level, pos, resource);
        int effective = physical + Math.max(0, info.incoming() - amount) - info.outgoing();
        return effective + amount <= max;
    }

    private boolean hasSpaceForDelivery(ServerLevel level, BlockPos pos, ItemResource resource, int amount) {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, null);
        if (handler == null) return false;

        int stackLimit = resource.toStack().getMaxStackSize();
        int totalSpace = 0;
        for (int slot = 0; slot < handler.size(); slot++) {
            ItemResource slotRes = handler.getResource(slot);
            if (slotRes.isEmpty()) {
                totalSpace += stackLimit;
            } else if (slotRes.equals(resource)) {
                totalSpace += stackLimit - handler.getAmountAsInt(slot);
            }
        }

        StrongholdHelper.TransportInfo info = StrongholdHelper.getTransportInfo(level, pos, resource);
        int availableSpace = totalSpace - Math.max(0, info.incoming() - amount) + info.outgoing();
        return availableSpace >= amount;
    }

    @Override
    public Optional<?> getGroupingKey() {
        return Optional.of(new CarryGroupKey(resource, getSourcePos(), getTargetPos(), sourceTerminal, targetTerminal));
    }
    @Override
    public boolean canBeAssignedTo(LivingEntity entity) {
        return super.canBeAssignedTo(entity) && CapacityHelper.getRemainingCapacity(entity) >= getMinTransfer();
    }
    public GlobalPos getSourceTerminal() { return sourceTerminal; }
    public GlobalPos getTargetTerminal() { return targetTerminal; }
    public CarryStage getStage() { return stage; }
    public void setStage(CarryStage stage) { this.stage = stage; }
    public boolean isExtracted() { return extracted; }
    public void setExtracted(boolean extracted) { this.extracted = extracted; }
    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }
    @Override public BlockPos getSourceContainerPos() { return getSourcePos(); }
    @Override public BlockPos getTargetContainerPos() { return getTargetPos(); }
    @Override
    public boolean isBeforePickup() {
        return this.stage == CarryStage.GO_TO_SOURCE || this.stage == CarryStage.GO_TO_OTHER_TERMINAL;
    }
    @Override public ItemResource getResource() { return resource; }
    @Override public int getAmount() { return amount; }
    public void setAmount(int newAmount) { this.amount = newAmount; }
    @Override public ItemCount getItem() { return new ItemCount(resource, amount); }
    @Override public int getVolume() { return amount; }
    @Override public boolean isSplittable() { return true; }
    @Override public double getHungerCostPerTick() { return 0.001; }
    @Override public Type<?> getType() { return TaskTypes.CARRY; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() { return STREAM_CODEC; }
    public double getEnergyCost() { return StrongholdHelper.getEnergyCost(sourceTerminal, targetTerminal); }
    private record CarryGroupKey(ItemResource resource, BlockPos source, BlockPos target, GlobalPos sourceTerminal, GlobalPos targetTerminal) {}
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                ItemResource.CODEC.fieldOf("resource").forGetter(CarryTask::getResource),
                Codec.INT.fieldOf("amount").forGetter(CarryTask::getAmount),
                Codec.BOOL.fieldOf("extracted").forGetter(CarryTask::isExtracted),
                Codec.BOOL.fieldOf("started").forGetter(CarryTask::isStarted),
                sourceCodec(), targetCodec(),
                GlobalPos.CODEC.fieldOf("source_terminal").forGetter(CarryTask::getSourceTerminal),
                GlobalPos.CODEC.fieldOf("target_terminal").forGetter(CarryTask::getTargetTerminal),
                CarryStage.CODEC.fieldOf("stage").forGetter(CarryTask::getStage)
        ).apply(instance, (next, resource, amount, extracted, started, source, target, srcTerm, tgtTerm, stage) ->
                new CarryTask(next, new ItemCount(resource, amount), extracted, started, source, target, srcTerm, tgtTerm, stage)));
        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), CarryTask::getOptionNext,
                ItemResource.STREAM_CODEC, CarryTask::getResource,
                ByteBufCodecs.INT, CarryTask::getAmount,
                ByteBufCodecs.BOOL, CarryTask::isExtracted,
                ByteBufCodecs.BOOL, CarryTask::isStarted,
                sourceStreamCodec(), CarryTask::getSourcePos,
                targetStreamCodec(), CarryTask::getTargetPos,
                GlobalPos.STREAM_CODEC, CarryTask::getSourceTerminal,
                GlobalPos.STREAM_CODEC, CarryTask::getTargetTerminal,
                CarryStage.STREAM_CODEC, CarryTask::getStage,
                (next, resource, amount, extracted, started, source, target, srcTerm, tgtTerm, stage) ->
                        new CarryTask(next, new ItemCount(resource, amount), extracted, started, source, target, srcTerm, tgtTerm, stage)
        );
    }
}