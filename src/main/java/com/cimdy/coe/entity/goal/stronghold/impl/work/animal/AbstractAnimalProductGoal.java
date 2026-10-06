package com.cimdy.coe.entity.goal.stronghold.impl.work.animal;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.entity.StrongholdEntity;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 动物产物类工作行为模板（方案 D 的进一步抽象）。
 * <p>
 * 将「在工作区域内寻找目标动物 → 交互产出/击杀 → 收集/存放」这一高频循环抽取为模板，
 * 剪毛 / 挤奶 / 狩猎等「对动物交互获取产物或击杀」工种共用此基类。新增同类工种只需提供：
 * <ul>
 *   <li>{@link #taskType()} —— 该行为对应的任务类型；</li>
 *   <li>{@link #findTarget(ServerLevel)} —— 在工作区域内寻找可交互/可狩猎的目标动物；</li>
 *   <li>{@link #interact(Animal)} —— 对目标动物执行交互或攻击；返回 true 表示目标已处理完毕。</li>
 * </ul>
 * 从而使新增一个动物产物/狩猎工种从「复制约 50 行循环」降为「三个方法 + 一行注册」。
 * <p>
 * 扩展点：{@link #toolPredicate()} 声明所需工具（如剪刀/空桶），默认无需工具；
 * {@link #useNearBlockMovement()} 声明是否贴身移动（狩猎等近战需求）。
 */
public abstract class AbstractAnimalProductGoal<E extends Animal> extends WorkGoal {
    @Nullable
    private E target;
    private boolean startDeposit = false;

    public AbstractAnimalProductGoal(Mob mob) {
        super(mob);
    }

    /** 该行为对应的任务类型（用于通用 canUse 匹配）。 */
    protected abstract Class<? extends Task> taskType();

    /** 在工作区域内寻找一个可交互的目标动物（已过滤存活/非幼体）。 */
    protected abstract Optional<E> findTarget(ServerLevel level);

    /**
     * 对目标动物执行交互并产出产物（产物进入世界掉落或工人背包均可）。
     * 返回 true 表示目标已处理完毕（如剪毛/挤奶完成、或猎物已死亡），模板将清除目标并进入存款；
     * 返回 false 表示需继续交互（如猎物尚未死亡，下一 tick 继续攻击）。
     */
    protected abstract boolean interact(E target);

    /** 扩展点：所需工具谓词；返回 null 表示无需工具。默认无需工具。 */
    protected Predicate<ItemStack> toolPredicate() {
        return null;
    }

    /**
     * 扩展点：移动到目标时是否使用「近距离」(3.0) 而非交互距离。
     * 近战狩猎等需要贴身攻击的工种可返回 true。默认 false（剪毛/挤奶等远程交互）。
     */
    protected boolean useNearBlockMovement() {
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        target = null;
        startDeposit = false;
    }

    @Override
    public void tick() {
        super.tick();
        ServerLevel level = (ServerLevel) mob.level();

        // 1. 工具管理
        Predicate<ItemStack> tool = toolPredicate();
        if (runToolPhase(tool)) return;

        // 2. 存款阶段（顺带拾取掉落的产物）
        if (startDeposit) {
            collectNearbyItems(3.0);
            if (tickDeposit()) startDeposit = false;
            return;
        }

        // 3. 交互
        if (target != null) {
            if (!target.isAlive()) {
                target = null;
                startDeposit = true;   // 猎物已死亡，拾取掉落物后继续
                return;
            }
            boolean arrived = useNearBlockMovement()
                    ? moveToNearBlock(target.blockPosition())
                    : moveToInteractionBlock(target.blockPosition());
            if (arrived) {
                lookAt(target.blockPosition());
                boolean done = interact(target);
                if (done) {
                    target = null;
                    startDeposit = true;
                }
            }
            return;
        }

        // 4. 寻找下一个目标
        Optional<E> next = findTarget(level);
        if (next.isPresent()) {
            target = next.get();
        } else {
            StrongholdEntity data = mob.getData(AttachmentRegistry.ENTITY_DATA);
            if (!data.stacks().isEmpty()) {
                startDeposit = true;
            } else if (tool == null || returnToolTick(EquipmentSlot.MAINHAND)) {
                setTaskCompleted();
            }
        }
    }
}
