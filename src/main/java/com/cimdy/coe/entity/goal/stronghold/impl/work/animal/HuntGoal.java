package com.cimdy.coe.entity.goal.stronghold.impl.work.animal;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest.HuntTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 狩猎行为：在工作区域内寻找可狩猎的被动动物，击杀后收集掉落物并存入绑定容器。
 * 基于 {@link AbstractAnimalProductGoal}（击杀模式）实现，仅提供四个扩展点。
 */
public class HuntGoal extends AbstractAnimalProductGoal<Animal> {
    public static final WorkGoalEntry ENTRY = WorkGoalEntry.of(StrongholdPriority.WORK, HuntGoal::new);

    /** 基础攻击间隔（tick）：对齐原版 MeleeAttackGoal 默认 attackInterval=20。 */
    private static final int BASE_ATTACK_INTERVAL = 20;
    /** 攻击速度下限保护。 */
    private static final double MIN_ATTACK_SPEED = 0.01;

    private final Mob mob;
    /** 近战攻击冷却计时（tick），由实体攻速缩放。 */
    private int attackTimer = 0;

    public HuntGoal(Mob mob) {
        super(mob);
        this.mob = mob;
    }

    @Override
    protected Class<? extends Task> taskType() {
        return HuntTask.class;
    }

    @Override
    protected boolean useNearBlockMovement() {
        return true; // 贴身近战攻击
    }

    @Override
    protected Predicate<ItemStack> toolPredicate() {
        return null; // 徒手狩猎
    }

    @Override
    protected Optional<Animal> findTarget(ServerLevel level) {
        return findNearestInArea(level, Animal.class,
                a -> HuntTask.isHuntable(a) && a.isAlive() && !a.isBaby());
    }

    @Override
    protected boolean interact(Animal target) {
        if (this.attackTimer > 0) {
            this.attackTimer--;
            return false; // 冷却中：仍视为「未完成」，基类下一 tick 继续交互
        }
        // 攻速缩放：复用原版 ATTACK_SPEED（CoE 词缀/天赋均写入此属性），与原版近战目标一致，
        // 避免猎物每次受击都触发、使攻速词条对据点工人同样生效。
        double speed = mob.getAttributes().hasAttribute(Attributes.ATTACK_SPEED)
                ? Math.max(MIN_ATTACK_SPEED, mob.getAttributeValue(Attributes.ATTACK_SPEED))
                : 4.0;
        this.attackTimer = Math.max(1, (int) (BASE_ATTACK_INTERVAL / speed));
        mob.doHurtTarget((ServerLevel) mob.level(), target);
        return !target.isAlive();
    }
}
