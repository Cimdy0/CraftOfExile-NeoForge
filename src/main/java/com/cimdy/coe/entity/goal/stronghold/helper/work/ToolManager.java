package com.cimdy.coe.entity.goal.stronghold.helper.work;

import com.cimdy.coe.entity.goal.stronghold.helper.Result;

import com.cimdy.coe.entity.goal.stronghold.helper.interaction.ContainerSelector;
import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.entity.goal.stronghold.behavior.IGoalContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.Predicate;

public class ToolManager {
    private enum FetchState { IDLE, MOVING, OPENING, EXTRACTING, CLOSING }
    private FetchState fetchState = FetchState.IDLE;
    private BlockPos fetchTarget;
    private int fetchTimer;
    private Predicate<ItemStack> toolPredicate;
    private int fetchSlot;
    private ItemResource fetchResource;

    private enum ReturnState { IDLE, MOVING, OPENING, INSERTING }
    private ReturnState returnState = ReturnState.IDLE;
    private BlockPos returnTarget;
    private int returnTimer;
    private EquipmentSlot returnSlot;

    private final IGoalContext context;
    private static final int OPEN_DURATION = 10;
    private static final int CLOSE_DURATION = 10;

    public ToolManager(IGoalContext context) {
        this.context = context;
    }

    public Result fetchToolTick(Predicate<ItemStack> isTool) {
        ServerLevel level = context.getLevel();
        Mob mob = context.getMob();

        if (isTool.test(mob.getMainHandItem())) {
            cancelFetch();
            return Result.SKIP;
        }

        if (fetchState == FetchState.IDLE) {
            ToolInfo best = findBestTool(level, isTool);
            if (best == null) {
                cancelFetch();
                return Result.TERMINATE;
            }
            fetchTarget = best.container;
            fetchSlot = best.slot;
            fetchResource = best.resource;
            toolPredicate = isTool;
            fetchState = FetchState.MOVING;
            return Result.CONTINUE;
        }

        switch (fetchState) {
            case MOVING:
                if (!context.moveToInteractionBlock(fetchTarget)) return Result.CONTINUE;
                fetchState = FetchState.OPENING;
                fetchTimer = OPEN_DURATION;
                level.playSound(null, fetchTarget, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
                return Result.CONTINUE;
            case OPENING:
                if (--fetchTimer > 0) return Result.CONTINUE;
                fetchState = FetchState.EXTRACTING;
                return Result.CONTINUE;
            case EXTRACTING:
                if (extractTool(level)) {
                    fetchState = FetchState.CLOSING;
                    fetchTimer = CLOSE_DURATION;
                    level.playSound(null, fetchTarget, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 1.0f, 0.8f);
                } else {
                    cancelFetch();
                    return Result.TERMINATE;
                }
                return Result.CONTINUE;
            case CLOSING:
                if (--fetchTimer > 0) return Result.CONTINUE;
                boolean hasTool = toolPredicate != null && toolPredicate.test(mob.getMainHandItem());
                cancelFetch();
                return hasTool ? Result.SKIP : Result.TERMINATE;
        }
        return Result.CONTINUE;
    }

    private boolean extractTool(ServerLevel level) {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, fetchTarget, null);
        if (handler == null) return false;
        try (Transaction txn = Transaction.openRoot()) {
            int extracted = handler.extract(fetchSlot, fetchResource, 1, txn);
            if (extracted > 0) {
                txn.commit();
                ItemStack taken = fetchResource.toStack(1);
                CapacityHelper.addItem(context.getMob(), taken);
                if (context.getMob().getMainHandItem().isEmpty()) {
                    context.getMob().setItemSlot(EquipmentSlot.MAINHAND, taken.copy());
                }
                return true;
            }
        }
        return false;
    }

    private void cancelFetch() {
        fetchState = FetchState.IDLE;
        fetchTarget = null;
        fetchTimer = 0;
        toolPredicate = null;
        fetchSlot = -1;
        fetchResource = null;
    }

    public boolean returnToolTick(EquipmentSlot slot) {
        ServerLevel level = context.getLevel();
        Mob mob = context.getMob();
        ItemStack stack = mob.getItemBySlot(slot);
        if (stack.isEmpty()) {
            resetReturn();
            return true;
        }
        if (returnState == ReturnState.IDLE) {
            ItemResource resource = ItemResource.of(stack);
            BlockPos target = ContainerSelector.findBestContainerForItem(context, resource);
            if (target == null) {
                mob.spawnAtLocation(level, stack.copy());
                mob.setItemSlot(slot, ItemStack.EMPTY);
                resetReturn();
                return true;
            }
            returnTarget = target;
            returnSlot = slot;
            returnState = ReturnState.MOVING;
            return false;
        }
        switch (returnState) {
            case MOVING:
                if (!context.moveToInteractionBlock(returnTarget)) return false;
                returnState = ReturnState.OPENING;
                returnTimer = OPEN_DURATION;
                level.playSound(null, returnTarget, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
                return false;
            case OPENING:
                if (--returnTimer > 0) return false;
                returnState = ReturnState.INSERTING;
                return false;
            case INSERTING:
                stack = mob.getItemBySlot(returnSlot);
                if (stack.isEmpty()) {
                    resetReturn();
                    return true;
                }
                int leftover = CapacityHelper.insertIntoContainer(level, returnTarget, stack);
                if (leftover == 0) {
                    mob.setItemSlot(returnSlot, ItemStack.EMPTY);
                } else {
                    mob.spawnAtLocation(level, stack.copy());
                    mob.setItemSlot(returnSlot, ItemStack.EMPTY);
                }
                level.playSound(null, returnTarget, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 1.0f, 0.8f);
                resetReturn();
                return true;
        }
        return false;
    }

    private void resetReturn() {
        returnState = ReturnState.IDLE;
        returnTarget = null;
        returnTimer = 0;
        returnSlot = null;
    }

    private ToolInfo findBestTool(ServerLevel level, Predicate<ItemStack> isTool) {
        BlockState logState = Blocks.OAK_LOG.defaultBlockState();
        ToolInfo best = null;
        float bestSpeed = -1f;
        int bestRemaining = Integer.MAX_VALUE;
        for (BlockPos pos : context.getStorageBlocks()) {
            if (!level.isLoaded(pos)) continue;
            ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, null);
            if (handler == null) continue;
            for (int slot = 0; slot < handler.size(); slot++) {
                ItemResource res = handler.getResource(slot);
                ItemStack stack = res.toStack(1);
                if (!isTool.test(stack)) continue;
                float speed = stack.getDestroySpeed(logState);
                int remaining = stack.getMaxDamage() - stack.getDamageValue();
                if (speed > bestSpeed || (speed == bestSpeed && remaining < bestRemaining)) {
                    bestSpeed = speed;
                    bestRemaining = remaining;
                    best = new ToolInfo(pos, slot, res);
                }
            }
        }
        return best;
    }

    private record ToolInfo(BlockPos container, int slot, ItemResource resource) {}
}