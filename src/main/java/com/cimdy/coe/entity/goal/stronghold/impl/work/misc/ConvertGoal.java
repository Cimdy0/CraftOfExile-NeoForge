package com.cimdy.coe.entity.goal.stronghold.impl.work.misc;

import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.TaskGenerator;
import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.ItemCount;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant.ConvertTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoal;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import com.cimdy.coe.entity.goal.stronghold.helper.Result;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

/**
 * 转化行为（合成链中的「转化」节点执行器，与 {@link com.cimdy.coe.entity.goal.stronghold.impl.CraftGoal} 同构）。
 * <p>
 * 取代 {@code AbstractConvertGoal} + 4 个子类（Cook/Smelt/Composter/Mill）。本类不再自行判断需求，
 * 而是由 {@code TaskGenerator} 在「需求/供应」判断后生成的链驱动：移动到站点 → 取原料 → 转为产物
 * （进度 {@code produced} 持久化于任务数据，重启可无缝续作）→ 存产物到站点存储，由后继 CarryTask 搬走。
 * 目标数量由任务 {@code amount}（生成时按需求缺口确定）限制。
 */
public class ConvertGoal extends WorkGoal {
    public static final WorkGoalEntry ENTRY = WorkGoalEntry.of(StrongholdPriority.WORK, ConvertGoal::new);

    private ConvertTask convertTask;

    public ConvertGoal(Mob mob) {
        super(mob);
    }

    @Override
    protected Class<? extends Task> taskType() {
        return ConvertTask.class;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) return false;
        if (!(this.firstTask instanceof ConvertTask ct)) return false;
        this.convertTask = ct;
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        ServerLevel level = (ServerLevel) mob.level();
        List<BlockPos> storages = site.getStorages();

        // 1. 存款阶段（把上一轮转化的产物存入站点存储，供后继 CarryTask 搬走）
        if (isDepositing()) {
            tickPriorityDeposit();
            return;
        }

        // 2. 持有原料 → 在站点转为产物（严格按 amount 限量，杜绝超额转化）
        ItemStack heldRaw = findHeldRaw();
        if (!heldRaw.isEmpty()) {
            int remaining = convertTask.getAmount() - convertTask.getProduced();
            if (remaining > 0) {
                int take = Math.min(heldRaw.getCount(), remaining);
                Item product = convertTask.getProduct().getItem();
                // 容量守卫：转化会先移除原料、再装入产物。考虑「移除原料释放的容量」，
                // 既避免背包溢出导致产物丢失，也避免严格判定（未计入即将释放的原料容量）引发的死锁。
                int rawVolFreed = take * (64 / heldRaw.getMaxStackSize());
                int prodVolNeeded = take * (64 / new ItemStack(product).getMaxStackSize());
                if (prodVolNeeded > CapacityHelper.getRemainingCapacity(mob) + rawVolFreed) {
                    return; // 容量仍不足：原料留在背包等待，下 tick 重试
                }
                ItemStack before = heldRaw.copy();
                before.setCount(take);
                CapacityHelper.removeItem(mob, before);
                ItemStack after = new ItemStack(product, take);
                CapacityHelper.addItem(mob, after);
                // 进度持久化到任务数据：本工人已转化产物数随任务（WorkTask.produced）序列化，
                // 服务器重启后可无缝续作；Goal 跨任务复用时各任务自带 produced，无需 start() 归零。
                convertTask.setProduced(convertTask.getProduced() + take);
                updateTask();
                return;
            }
            // 已达目标数量却仍持有原料（如续作时背包残留 / 单次取料多于所需）：
            // 把多余原料存回站点存储，避免卡在背包导致任务无法结束。
            startPriorityDeposit(storages);
            return;
        }

        // 3. 持有产物 → 优先存入站点存储
        if (holdsProduct()) {
            startPriorityDeposit(storages);
            return;
        }

        // 4. 已达目标数量 → 结束（由任务 amount 限量，避免超额转化）
        if (convertTask.getProduced() >= convertTask.getAmount()) {
            if (returnToolTick(EquipmentSlot.MAINHAND)) setTaskCompleted();
            return;
        }

        // 5. 从站点存储取原料
        if (isFetchQueueActive()) {
            Result r = tickFetchQueue(level);
            if (r == Result.TERMINATE) {
                setTaskFailed();
                return;
            }
            if (r == Result.CONTINUE) return;
            return;
        }
        ItemResource raw = findRawInStorage(level, storages);
        if (raw == null) {
            if (returnToolTick(EquipmentSlot.MAINHAND)) setTaskCompleted();
            return;
        }
        startFetchQueue(storages, List.of(new ItemCount(raw, 1)));
    }

    private ItemStack findHeldRaw() {
        ItemResource raw = convertTask.getRaw();
        for (ItemStack stack : CapacityHelper.getStacks(mob)) {
            if (raw.matches(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    private boolean holdsProduct() {
        ItemResource product = convertTask.getProduct();
        for (ItemStack stack : CapacityHelper.getStacks(mob)) {
            if (product.matches(stack)) return true;
        }
        return false;
    }

    private ItemResource findRawInStorage(ServerLevel level, List<BlockPos> storages) {
        ItemResource raw = convertTask.getRaw();
        for (BlockPos containerPos : storages) {
            if (!level.isLoaded(containerPos)) continue;
            ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, containerPos, null);
            if (handler == null) continue;
            for (int slot = 0; slot < handler.size(); slot++) {
                ItemResource res = handler.getResource(slot);
                if (!res.isEmpty() && raw.matches(res.toStack(1))) {
                    return res;
                }
            }
        }
        return null;
    }
}
