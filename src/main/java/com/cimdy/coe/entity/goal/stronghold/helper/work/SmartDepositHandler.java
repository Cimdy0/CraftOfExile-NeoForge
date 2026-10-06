package com.cimdy.coe.entity.goal.stronghold.helper.work;

import com.cimdy.coe.entity.goal.stronghold.helper.Result;

import com.cimdy.coe.entity.goal.stronghold.helper.interaction.ContainerSelector;
import com.cimdy.coe.entity.goal.stronghold.helper.interaction.TimedInteraction;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.entity.StrongholdEntity;
import com.cimdy.coe.entity.goal.stronghold.behavior.IGoalContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * 智能存款处理器。
 * <p>
 * 将工人背包中的所有物品按「最佳容器」分组后依次存入。
 * 每个容器的 MOVING→OPENING→DEPOSITING→CLOSING 阶段由 {@link TimedInteraction} 代理。
 */
public class SmartDepositHandler {

    private enum State { IDLE, MOVING, FETCHING }

    private final IGoalContext context;
    private State handlerState = State.IDLE;
    private List<BlockPos> containers;
    private Map<BlockPos, List<ItemStack>> containerItemsMap;
    private int currentContainerIndex;

    // 委托：单个容器存储交互
    private final SingleDepositInteraction depositInteraction;

    public SmartDepositHandler(IGoalContext context) {
        this.context = context;
        this.depositInteraction = new SingleDepositInteraction(context);
    }

    public void start(List<BlockPos> candidates) {
        reset();
        if (candidates == null || candidates.isEmpty()) return;
        StrongholdEntity data = context.getMob().getData(AttachmentRegistry.ENTITY_DATA);
        List<ItemStack> stacks = new ArrayList<>(data.stacks());
        if (stacks.isEmpty()) return;

        containerItemsMap = new HashMap<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;
            net.neoforged.neoforge.transfer.item.ItemResource resource =
                    net.neoforged.neoforge.transfer.item.ItemResource.of(stack);
            BlockPos best = ContainerSelector.findBestContainerForItem(context, resource);
            containerItemsMap.computeIfAbsent(best, k -> new ArrayList<>()).add(stack.copy());
        }

        containers = new ArrayList<>(containerItemsMap.keySet());
        containers.sort(Comparator.comparingDouble(pos ->
                context.getMob().distanceToSqr(Vec3.atCenterOf(pos))));
        handlerState = State.MOVING;
        currentContainerIndex = 0;
    }

    public boolean tick() {
        ServerLevel level = context.getLevel();
        if (handlerState == State.IDLE) return true;
        if (containers == null || currentContainerIndex >= containers.size()) {
            reset();
            return true;
        }
        BlockPos currentContainer = containers.get(currentContainerIndex);
        List<ItemStack> items = containerItemsMap.get(currentContainer);
        if (items == null || items.isEmpty()) {
            currentContainerIndex++;
            return false;
        }

        switch (handlerState) {
            case MOVING:
                depositInteraction.setItems(items);
                depositInteraction.start(currentContainer);
                handlerState = State.FETCHING;
                return false;

            case FETCHING:
                Result r = depositInteraction.tick();
                if (r == Result.SKIP || r == Result.TERMINATE) {
                    depositInteraction.reset();
                    currentContainerIndex++;
                    handlerState = State.MOVING;
                }
                return false;
        }
        return false;
    }

    public boolean isActive() {
        return handlerState != State.IDLE;
    }

    public void reset() {
        handlerState = State.IDLE;
        containers = null;
        containerItemsMap = null;
        currentContainerIndex = 0;
        depositInteraction.reset();
    }

    // ---- 内部委托：单容器存款 ----

    private static class SingleDepositInteraction extends TimedInteraction<Void> {
        private List<ItemStack> items;

        SingleDepositInteraction(IGoalContext context) { super(context); }

        void setItems(List<ItemStack> items) { this.items = items; }

        @Override
        protected boolean execute(ServerLevel level, BlockPos target) {
            if (items == null) return false;
            for (ItemStack stack : items) {
                if (stack.isEmpty()) continue;
                int leftover = CapacityHelper.insertIntoContainer(level, target, stack);
                if (leftover < stack.getCount()) {
                    ItemStack removed = stack.copy();
                    removed.setCount(stack.getCount() - leftover);
                    CapacityHelper.removeItem(context.getMob(), removed);
                }
            }
            return true;
        }

        @Override
        public Void buildResult() { return null; }

        @Override
        public void reset() {
            super.reset();
            items = null;
        }
    }
}
