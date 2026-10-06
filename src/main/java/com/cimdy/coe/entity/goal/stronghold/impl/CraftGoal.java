package com.cimdy.coe.entity.goal.stronghold.impl;

import com.cimdy.coe.entity.goal.stronghold.helper.work.SmartDepositHandler;
import com.cimdy.coe.entity.goal.stronghold.behavior.BehaviorRegistry;
import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.ItemCount;
import com.cimdy.coe.data.codec.stronghold.task.impl.CraftTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdBehavior;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.TaskGoal;
import com.cimdy.coe.entity.goal.stronghold.helper.Result;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CraftGoal extends TaskGoal {
    /** 合成工种注册条目：处理 {@link CraftTask}，与其他工种一致地纳入 {@link com.cimdy.coe.entity.goal.stronghold.behavior.BehaviorRegistry}。 */
    public static final StrongholdBehavior ENTRY = StrongholdBehavior.of(StrongholdPriority.WORK, CraftGoal::new);

    private CraftTask craftTask;
    private Recipe<?> recipe;
    private BlockPos site;
    private Predicate<ItemStack> toolPredicate;

    public CraftGoal(Mob mob) { super(mob); }

    @Override
    public boolean canUse() {
        if (!super.canUse()) return false;
        if (!(this.firstTask instanceof CraftTask craftTask)) return false;
        this.craftTask = craftTask;
        this.site = craftTask.getSite();
        this.toolPredicate = itemStack -> craftTask.getToolPredicate().test(itemStack);

        ServerLevel level = (ServerLevel) mob.level();
        RecipeHolder<?> holder = level.getServer().getRecipeManager().byKey(craftTask.getRecipe()).orElse(null);
        this.recipe = holder != null ? holder.value() : null;
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (firstTask == null) return;
        ServerLevel level = (ServerLevel) mob.level();

        // 1. 工具处理
        if (toolPredicate != null && !toolPredicate.test(mob.getMainHandItem())) {
            Result toolResult = fetchToolTick(toolPredicate);
            if (toolResult == Result.TERMINATE) {
                this.setTaskCompleted();
                return;
            }
            // CONTINUE 或 SKIP：等待下一 tick
            return;
        }

        // 2. 移动到合成站
        if (!moveToNearBlock(site)) return;

        // 3. 如果还有剩余合成次数
        if (!craftTask.isCompleted()) {
            // 3a. 材料拿取
            if (isFetchQueueActive()) {
                Result fetchResult = tickFetchQueue(level);
                if (fetchResult == Result.TERMINATE) {
                    this.setTaskFailed();
                    return;
                }
                if (fetchResult == Result.CONTINUE) return;
                // SKIP 后继续
            } else {
                List<ItemCount> needed = calculateMissingMaterials();
                if (!needed.isEmpty()) {
                    List<BlockPos> storages = getSiteStorages();
                    if (storages.isEmpty()) { this.setTaskFailed();; return; }
                    startFetchQueue(storages, needed);
                    return;
                }
            }

            // 3b. 单次合成
            if (isCraftQueueActive()) {
                Result craftResult = tickCraftQueue();
                if (craftResult == Result.TERMINATE) {
                    this.setTaskFailed();
                    return;
                }
                if (craftResult == Result.CONTINUE) return;
                // SKIP：合成成功，更新剩余次数
                this.craftTask.addRemainingTime();
                this.updateTask();
            } else {
                startCraftQueue(site, recipe);
                return;
            }
        }

        // 4. 全部合成完成，存放产物
        if (!isDepositing()) {
            List<BlockPos> storages = getSiteStorages();
            if (storages.isEmpty()) { this.setTaskFailed();; return; }
            startPriorityDeposit(storages);
        } else {
            if (tickPriorityDeposit()) {   // SmartDepositHandler tick 仍返回 boolean，可保持
                if (toolPredicate != null) {
                    if (returnToolTick(EquipmentSlot.MAINHAND)) {
                        this.setTaskCompleted();
                    }
                } else {
                    this.setTaskCompleted();
                }
            }
        }
    }

    private List<ItemCount> calculateMissingMaterials() {
        List<ItemCount> needed = new ArrayList<>();
        if (craftTask.getItems() == null) return needed;
        List<ItemStack> carried = CapacityHelper.getStacks(mob);
        for (ItemCount required : craftTask.getItems()) {
            int carriedAmount = carried.stream()
                    .filter(s -> required.resource().matches(s))
                    .mapToInt(ItemStack::getCount)
                    .sum();
            int missing = required.amount() - carriedAmount;
            if (missing > 0) needed.add(new ItemCount(required.resource(), missing));
        }
        return needed;
    }

    private List<BlockPos> getSiteStorages() {
        return hold.getSite(site)
                .map(Site::getStorages)
                .orElse(List.of());
    }
}