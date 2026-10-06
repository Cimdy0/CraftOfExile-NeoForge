package com.cimdy.coe.entity.goal.stronghold.impl.work.block;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.entity.StrongholdEntity;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.work.WorkType;
import com.cimdy.coe.data.codec.stronghold.task.work.WorkTypeRegistry;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoal;
import com.cimdy.coe.entity.work.WorkSpeed;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 区域方块作业模板（破坏队列模式，方案 D 的进一步抽象）。
 * <p>
 * 将 Mining / Forestry 等「扫描区域 → 定位目标 → 破坏队列处理 → 存放」这一高频循环抽取为模板，
 * 使新增同类工种从「复制约 60 行循环」降为「五个小方法 + 一行注册」。
 * 新增一个「采矿式」工种（如采石 / 挖矿 / 采伐下界木）只需提供：
 * <ul>
 *   <li>{@link #taskType()} —— 该行为对应的任务类型；</li>
 *   <li>{@link #toolPredicate()} —— 所需工具（如镐/斧），无工具返回 {@code null}；</li>
 *   <li>{@link #isTarget(ServerLevel, BlockPos)} —— 判断某方块是否为待处理目标；</li>
 *   <li>{@link #targetComparator()} —— 多目标时的优选比较器；</li>
 *   <li>{@link #collectBlocks(ServerLevel, BlockPos)} —— 目标根方块对应的待破坏方块列表（单块或整棵树）；</li>
 *   <li>{@link #onBlocksBroken(ServerLevel, BlockPos)} —— 破坏完成后的回调（如处理树叶），默认无操作。</li>
 * </ul>
 *
 * <p>扩展点说明：{@link #toolPredicate()} 返回 {@code null} 时跳过工具管理（如徒手采集）；
 * {@link #onBlocksBroken} 默认无操作，子类可在此做补种 / 树叶处理等收尾。
 */
public abstract class AbstractBlockWorkGoal extends WorkGoal {
    @Nullable
    protected BlockPos currentTarget;
    private boolean startDeposit = false;

    public AbstractBlockWorkGoal(Mob mob) {
        super(mob);
    }

    /** 该行为对应的任务类型（用于通用 canUse 匹配）。 */
    protected abstract Class<? extends Task> taskType();

    /** 扩展点：所需工具谓词；返回 null 表示无需工具。默认无需工具。 */
    @Nullable
    protected abstract Predicate<ItemStack> toolPredicate();

    /** 扩展点：判断工作区域内某方块是否为待处理目标（已自动排除被占用的位置）。 */
    protected abstract boolean isTarget(ServerLevel level, BlockPos pos);

    /** 扩展点：多目标时的优选比较器（传给 {@link #scanWorkArea} 的 min）。 */
    protected abstract Comparator<BlockPos> targetComparator();

    /** 扩展点：给定目标根方块，返回需要破坏的方块列表（单块或整棵树）。 */
    protected abstract List<BlockPos> collectBlocks(ServerLevel level, BlockPos root);

    /** 扩展点：破坏队列完成后回调（如处理树叶 / 补种）。默认无操作。 */
    protected void onBlocksBroken(ServerLevel level, BlockPos root) {
    }

    /**
     * 扩展点：是否使用破坏队列异步处理目标。默认 {@code true}（采矿 / 林业）；
     * 返回 {@code false} 时由 {@link #processSynchronously(ServerLevel, BlockPos)} 同步处理
     * （如农耕收割：移动到方块即当场收割并补种，无需排队）。
     */
    protected boolean useBreakingQueue() {
        return true;
    }

    /**
     * 扩展点：同步处理目标（仅当 {@link #useBreakingQueue()} 返回 {@code false} 时调用）。
     * 子类应在此完成对目标方块的实际操作（收割 / 补种 / 入背包等），释放工作位置由本方法自行负责。
     */
    protected void processSynchronously(ServerLevel level, BlockPos root) {
    }

    @Override
    public void stop() {
        super.stop();
        if (currentTarget != null) {
            releaseWorkPosition();
            currentTarget = null;
        }
        startDeposit = false;
        cancelBreakingQueue();
    }

    @Override
    public void tick() {
        super.tick();
        ServerLevel level = (ServerLevel) mob.level();

        // 依当前任务推导工种，将实体有效工作速度写入破坏处理器（使采矿/农耕/林业/剪毛等方块作业随之加速）。
        if (firstTask != null) {
            String workType = WorkTypeRegistry.workTypes().stream()
                    .filter(t -> t.taskClass().isInstance(firstTask))
                    .map(WorkType::name)
                    .findFirst().orElse("");
            breakingHandler.setWorkSpeed(WorkSpeed.effectiveSpeed(mob, workType));
        }

        // 1. 工具管理（已持工具时 fetchToolTick 返回 SKIP，继续后续阶段）
        Predicate<ItemStack> tool = toolPredicate();
        if (runToolPhase(tool)) return;

        // 2. 存款阶段
        if (startDeposit) {
            if (tickDeposit()) startDeposit = false;
            return;
        }

        // 3. 处理当前目标（移动到根方块 → 破坏队列或同步处理 → 驱动至完成）
        if (currentTarget != null) {
            if (!hasBreakingQueue()) {
                if (moveToInteractionBlock(currentTarget)) {
                    if (useBreakingQueue()) {
                        startBreakingQueue(collectBlocks(level, currentTarget), mob.getMainHandItem());
                    } else {
                        processSynchronously(level, currentTarget);
                        currentTarget = null;
                        startDeposit = true;
                    }
                }
                return;
            }
            if (tickBreakingQueue()) {
                onBlocksBroken(level, currentTarget);
                releaseWorkPosition();
                currentTarget = null;
                startDeposit = true;
            }
            return;
        }

        // 4. 寻找下一个目标
        Optional<BlockPos> next = scanWorkArea(
                pos -> isTarget(level, pos),
                targetComparator());
        if (next.isPresent()) {
            currentTarget = next.get();
            occupyWorkPosition(currentTarget);
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
