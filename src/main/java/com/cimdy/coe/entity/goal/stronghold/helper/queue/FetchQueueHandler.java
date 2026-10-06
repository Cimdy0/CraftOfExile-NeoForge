package com.cimdy.coe.entity.goal.stronghold.helper.queue;

import com.cimdy.coe.entity.goal.stronghold.helper.Result;

import com.cimdy.coe.entity.goal.stronghold.helper.interaction.TimedInteraction;
import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.ItemCount;
import com.cimdy.coe.entity.goal.stronghold.behavior.IGoalContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 物品获取队列处理器。
 * 管理「扫描容器定位物品 → 依次前往拿取」的序列交互。
 * 每个容器的 MOVING→OPENING→EXTRACTING→CLOSING 阶段由 {@link TimedInteraction} 代理。
 */
public class FetchQueueHandler {

    private enum State { IDLE, SCANNING, FETCHING, DONE }

    private final IGoalContext context;
    private State queueState = State.IDLE;
    private List<BlockPos> storageCandidates;
    private Queue<ItemCount> pendingItems;
    private ItemCount currentItem;
    private int currentNeeded;

    // 委托：单次容器交互
    private final SingleFetchInteraction fetchInteraction;
    private Result lastFetchResult = Result.SKIP;

    public FetchQueueHandler(IGoalContext context) {
        this.context = context;
        this.fetchInteraction = new SingleFetchInteraction(context);
    }

    public void startFetchQueue(List<BlockPos> candidates, List<ItemCount> items) {
        this.storageCandidates = new ArrayList<>(candidates);
        this.pendingItems = new LinkedList<>();
        for (ItemCount item : items) {
            if (item.amount() > 0) pendingItems.add(item);
        }
        resetInternal();
        if (!this.pendingItems.isEmpty()) this.queueState = State.SCANNING;
    }

    public Result tick() {
        if (queueState == State.IDLE || queueState == State.DONE) return Result.SKIP;
        ServerLevel level = context.getLevel();

        switch (queueState) {
            case SCANNING -> tickScanning();
            case FETCHING -> {
                lastFetchResult = fetchInteraction.tick();
                if (lastFetchResult == Result.SKIP) {
                    // 单次取货完成，减少需求
                    if (currentNeeded <= 0) currentItem = null;
                    fetchInteraction.reset();
                    queueState = State.SCANNING;
                } else if (lastFetchResult == Result.TERMINATE) {
                    fetchInteraction.reset();
                    queueState = State.DONE;
                }
            }
        }

        if (queueState == State.IDLE) {
            return (pendingItems.isEmpty() && currentItem == null) ? Result.SKIP : Result.TERMINATE;
        }
        return lastFetchResult == Result.TERMINATE ? Result.TERMINATE : Result.CONTINUE;
    }

    private void tickScanning() {
        if (pendingItems.isEmpty()) {
            queueState = State.IDLE;
            return;
        }
        if (currentItem == null || currentNeeded <= 0) {
            currentItem = pendingItems.poll();
            if (currentItem == null) {
                queueState = State.IDLE;
                return;
            }
            currentNeeded = currentItem.amount();
        }

        BlockPos container = CapacityHelper.findBestContainerForResource(
                context.getMob(), storageCandidates, currentItem.resource(), currentNeeded);
        if (container == null) {
            queueState = State.IDLE;
            return;
        }

        fetchInteraction.setItem(currentItem.resource(), currentNeeded);
        fetchInteraction.start(container);
        queueState = State.FETCHING;
    }

    private void resetInternal() {
        queueState = State.IDLE;
        currentItem = null;
        currentNeeded = 0;
        fetchInteraction.reset();
    }

    public boolean isActive() {
        return queueState != State.IDLE && queueState != State.DONE;
    }

    // ---- 内部委托：单次容器取货 ----

    private class SingleFetchInteraction extends TimedInteraction<Void> {
        private ItemResource itemResource;
        private int needed;

        SingleFetchInteraction(IGoalContext context) { super(context); }

        void setItem(ItemResource resource, int amount) {
            this.itemResource = resource;
            this.needed = amount;
        }

        @Override
        protected boolean execute(ServerLevel level, BlockPos target) {
            ItemStack stackToExtract = itemResource.toStack(needed);
            ItemStack extracted = CapacityHelper.extractFromContainer(level, target, stackToExtract);
            if (!extracted.isEmpty()) {
                CapacityHelper.addItem(context.getMob(), extracted);
                currentNeeded -= extracted.getCount();
                return true;
            }
            return false;
        }

        @Override
        public Void buildResult() { return null; }
    }
}
