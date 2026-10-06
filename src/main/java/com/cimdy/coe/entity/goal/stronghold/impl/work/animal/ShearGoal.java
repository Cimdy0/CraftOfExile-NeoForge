package com.cimdy.coe.entity.goal.stronghold.impl.work.animal;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest.ShearTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 牧养行为：在工作区域内为可剪毛的绵羊剪毛，收集羊毛。
 * 找动物→交互→收集→存放 循环由 {@link AbstractAnimalProductGoal} 提供，本类仅声明任务类型、目标查找与交互。
 */
public class ShearGoal extends AbstractAnimalProductGoal<Sheep> {
    public static final WorkGoalEntry ENTRY = WorkGoalEntry.of(StrongholdPriority.WORK, ShearGoal::new);

    public ShearGoal(Mob mob) {
        super(mob);
    }

    @Override
    protected Class<? extends Task> taskType() {
        return ShearTask.class;
    }

    @Override
    protected Optional<Sheep> findTarget(ServerLevel level) {
        return findNearestInArea(level, Sheep.class,
                s -> s.readyForShearing() && s.isAlive());
    }

    @Override
    protected boolean interact(Sheep target) {
        target.shear((ServerLevel) mob.level(), SoundSource.BLOCKS, mob.getMainHandItem());
        return true;
    }

    @Override
    protected Predicate<ItemStack> toolPredicate() {
        return s -> s.getItem() instanceof ShearsItem;
    }
}
