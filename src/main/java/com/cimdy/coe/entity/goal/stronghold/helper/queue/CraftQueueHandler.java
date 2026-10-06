package com.cimdy.coe.entity.goal.stronghold.helper.queue;

import com.cimdy.coe.entity.goal.stronghold.helper.Result;

import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.entity.goal.stronghold.behavior.IGoalContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.ArrayList;
import java.util.List;

public class CraftQueueHandler {

    private enum State { IDLE, MOVING, CRAFTING, DONE }

    private State state = State.IDLE;
    private final IGoalContext context;
    private BlockPos site;
    private List<Ingredient> ingredients;
    private ItemStack resultTemplate;
    private boolean lastCraftSuccess = false;

    public CraftQueueHandler(IGoalContext context) {
        this.context = context;
    }

    public void startCraftQueue(BlockPos site, Recipe<?> recipe) {
        this.site = site;
        this.ingredients = recipe.placementInfo().ingredients();

        ServerLevel level = context.getLevel();
        this.resultTemplate = getRecipeResult(level, recipe);
        this.lastCraftSuccess = false;
        this.state = State.MOVING;
    }

    /**
     * @return CONTINUE 表示流程未结束；SKIP 表示单次合成成功完成；TERMINATE 表示合成失败。
     */
    public Result tick() {
        switch (state) {
            case IDLE, DONE -> { return lastCraftSuccess ? Result.SKIP : Result.TERMINATE; }
            case MOVING -> tickMoving();
            case CRAFTING -> tickCrafting();
        }
        if (state == State.DONE) {
            return lastCraftSuccess ? Result.SKIP : Result.TERMINATE;
        }
        return Result.CONTINUE;
    }

    private void tickMoving() {
        if (!context.moveToNearBlock(site)) return;
        state = State.CRAFTING;
    }

    private void tickCrafting() {
        lastCraftSuccess = tryCraftOnce();
        state = State.DONE;
    }

    private boolean tryCraftOnce() {
        if (resultTemplate == null || resultTemplate.isEmpty()) return false;

        List<ItemStack> requiredStacks = new ArrayList<>();
        for (Ingredient ing : ingredients) {
            if (ing.isEmpty()) continue;
            ItemStack found = null;
            for (ItemStack stack : CapacityHelper.getStacks(context.getMob())) {
                if (ing.test(stack)) {
                    found = stack.copy();
                    found.setCount(1);
                    break;
                }
            }
            if (found == null) return false;
            requiredStacks.add(found);
        }

        ItemStack product = resultTemplate.copy();
        if (CapacityHelper.tryAddItem(context.getMob(), product) != 0) {
            return false;
        }

        for (ItemStack req : requiredStacks) {
            CapacityHelper.removeItem(context.getMob(), req);
        }
        return true;
    }

    private ItemStack getRecipeResult(ServerLevel level, Recipe<?> recipe) {
        for (RecipeDisplay display : recipe.display()) {
            SlotDisplay resultDisplay = display.result();
            ContextKeySet keySet = new ContextKeySet.Builder()
                    .required(SlotDisplayContext.REGISTRIES)
                    .build();
            ContextMap ctx = new ContextMap.Builder()
                    .withParameter(SlotDisplayContext.REGISTRIES, level.registryAccess())
                    .create(keySet);
            ItemStack stack = resultDisplay.resolveForFirstStack(ctx);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    public boolean isActive() { return state != State.IDLE && state != State.DONE; }
    public void reset() { state = State.IDLE; lastCraftSuccess = false; }
}