package com.cimdy.coe.entity.goal.stronghold.impl.work.misc;

import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.ItemCount;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant.EnchantTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoal;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import com.cimdy.coe.entity.goal.stronghold.helper.Result;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;

/**
 * 附魔应用行为（合成链中的「附魔」节点执行器，与 {@link com.cimdy.coe.entity.goal.stronghold.impl.work.misc.ConvertGoal} 同构）。
 * <p>
 * 不采用「附魔台随机附魔」，改为数据驱动的「使用附魔书给物品附魔」：
 * 工人把<b>附魔书 + 目标物品 + 青金石</b>搬到站点存储后，在站点消耗这三者与<b>末影能量</b>（按终端坐标从据点账本结算），
 * 产出附魔后的目标物品（进度 {@code produced} 持久化于任务数据，重启可无缝续作），由后继 CarryTask 搬走。
 * <p>
 * 产物 {@code ItemStack} 携带 {@code ENCHANTMENTS} 组件——务必用 {@code ItemResource.toStack(n)} 保留组件，
 * 否则 {@code holdsOutput()} 会因组件不匹配而卡死。
 */
public class EnchantGoal extends WorkGoal {
    public static final WorkGoalEntry ENTRY = WorkGoalEntry.of(StrongholdPriority.WORK, EnchantGoal::new);

    private EnchantTask enchantTask;

    public EnchantGoal(Mob mob) {
        super(mob);
    }

    @Override
    protected Class<? extends Task> taskType() {
        return EnchantTask.class;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) return false;
        if (!(this.firstTask instanceof EnchantTask et)) return false;
        this.enchantTask = et;
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        ServerLevel level = (ServerLevel) mob.level();
        List<BlockPos> storages = site.getStorages();

        // 1. 存款阶段（把上一轮附魔产物存入站点存储，供后继 CarryTask 搬走）
        if (isDepositing()) {
            tickPriorityDeposit();
            return;
        }

        // 2. 持有产物 → 优先存入站点存储
        if (holdsOutput()) {
            startPriorityDeposit(storages);
            return;
        }

        int remaining = enchantTask.getAmount() - enchantTask.getProduced();
        int heldBook = held(enchantTask.getBookRes());
        int heldTarget = held(enchantTask.getTargetRes());
        int heldLapis = held(enchantTask.getLapisRes());

        // 3. 持有全部输入 → 执行附魔（消耗 附魔书 + 目标 + 青金石 + 末影能量 → 产出附魔物品）
        if (remaining > 0) {
            int byLapis = heldLapis / Math.max(1, enchantTask.getLapisCost());
            int take = Math.min(remaining, Math.min(heldBook, Math.min(heldTarget, byLapis)));
            if (take > 0) {
                double cost = (double) enchantTask.getEnderEnergyCost() * take;
                if (!StrongholdHelper.hasEnderEnergy(level, terminalPos, cost)) {
                    // 末影能量不足：无可继续输入则结束，避免卡死
                    if (returnToolTick(EquipmentSlot.MAINHAND)) setTaskCompleted();
                    return;
                }
                ItemStack out = enchantTask.getOutputRes().toStack(take);
                // 容量守卫：先移除输入释放的容量，再装入产物
                int rawVolFreed = take * (64 / enchantTask.getBookRes().toStack(1).getMaxStackSize())
                        + take * (64 / enchantTask.getTargetRes().toStack(1).getMaxStackSize())
                        + take * enchantTask.getLapisCost() * (64 / enchantTask.getLapisRes().toStack(1).getMaxStackSize());
                int prodVolNeeded = take * (64 / out.getMaxStackSize());
                if (prodVolNeeded > CapacityHelper.getRemainingCapacity(mob) + rawVolFreed) {
                    return; // 容量仍不足：输入留在背包等待，下 tick 重试
                }
                CapacityHelper.removeItem(mob, enchantTask.getBookRes().toStack(take));
                CapacityHelper.removeItem(mob, enchantTask.getTargetRes().toStack(take));
                CapacityHelper.removeItem(mob, enchantTask.getLapisRes().toStack(take * enchantTask.getLapisCost()));
                CapacityHelper.addItem(mob, out);
                // 结算末影能量（按终端坐标从据点账本扣除）
                StrongholdHelper.reduceEnderEnergy(level, terminalPos.pos(), cost);
                enchantTask.setProduced(enchantTask.getProduced() + take);
                updateTask();
                return;
            }
        }

        // 4. 已达目标数量 → 结束
        if (enchantTask.getProduced() >= enchantTask.getAmount()) {
            if (returnToolTick(EquipmentSlot.MAINHAND)) setTaskCompleted();
            return;
        }

        // 5. 从站点存储取缺失输入
        if (isFetchQueueActive()) {
            Result r = tickFetchQueue(level);
            if (r == Result.TERMINATE) {
                setTaskFailed();
                return;
            }
            if (r == Result.CONTINUE) return;
            return;
        }
        List<ItemCount> needed = new ArrayList<>();
        int needBook = Math.max(0, remaining - heldBook);
        int needTarget = Math.max(0, remaining - heldTarget);
        int needLapis = Math.max(0, remaining * enchantTask.getLapisCost() - heldLapis);
        if (needBook > 0) needed.add(new ItemCount(enchantTask.getBookRes(), needBook));
        if (needTarget > 0) needed.add(new ItemCount(enchantTask.getTargetRes(), needTarget));
        if (needLapis > 0) needed.add(new ItemCount(enchantTask.getLapisRes(), needLapis));
        if (needed.isEmpty()) {
            if (returnToolTick(EquipmentSlot.MAINHAND)) setTaskCompleted();
            return;
        }
        startFetchQueue(storages, needed);
    }

    private int held(ItemResource res) {
        int n = 0;
        for (ItemStack s : CapacityHelper.getStacks(mob)) {
            if (res.matches(s)) n += s.getCount();
        }
        return n;
    }

    private boolean holdsOutput() {
        for (ItemStack s : CapacityHelper.getStacks(mob)) {
            if (enchantTask.getOutputRes().matches(s)) return true;
        }
        return false;
    }
}
