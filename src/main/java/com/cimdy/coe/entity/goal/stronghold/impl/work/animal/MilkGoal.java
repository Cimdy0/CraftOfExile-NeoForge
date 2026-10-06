package com.cimdy.coe.entity.goal.stronghold.impl.work.animal;

import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest.MilkTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 挤奶行为：在工作区域内为奶牛挤奶，获取奶桶。
 * 找动物→交互→收集→存放 循环由 {@link AbstractAnimalProductGoal} 提供，本类仅声明任务类型、目标查找与交互。
 */
public class MilkGoal extends AbstractAnimalProductGoal<Cow> {
    public static final WorkGoalEntry ENTRY = WorkGoalEntry.of(StrongholdPriority.WORK, MilkGoal::new);

    public MilkGoal(Mob mob) {
        super(mob);
    }

    @Override
    protected Class<? extends Task> taskType() {
        return MilkTask.class;
    }

    @Override
    protected Optional<Cow> findTarget(ServerLevel level) {
        return findNearestInArea(level, Cow.class,
                c -> c.isAlive() && !c.isBaby());
    }

    @Override
    protected boolean interact(Cow target) {
        CapacityHelper.removeItem(mob, new ItemStack(Items.BUCKET, 1));
        CapacityHelper.addItem(mob, new ItemStack(Items.MILK_BUCKET, 1));
        return true;
    }

    @Override
    protected Predicate<ItemStack> toolPredicate() {
        return s -> s.getItem() == Items.BUCKET;
    }
}
