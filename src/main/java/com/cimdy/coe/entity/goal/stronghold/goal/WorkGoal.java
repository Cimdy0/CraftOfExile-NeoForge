package com.cimdy.coe.entity.goal.stronghold.goal;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.basic.AreaSite;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.cimdy.coe.entity.goal.stronghold.helper.Result;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class WorkGoal extends TaskGoal {
    protected WorkTask workTask;
    protected Site site;

    public WorkGoal(Mob mob) {
        super(mob);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) return false;
        if (!(this.firstTask instanceof WorkTask workTask)) return false;
        if (!taskType().isInstance(workTask)) return false;
        Optional<Site> siteOpt = this.hold.holdState().getSite(workTask.getWorkPos());
        if (siteOpt.isEmpty()) return false;
        Site site = siteOpt.get();
        if (!workTask.matchesSite(site)) return false;
        this.workTask = workTask;
        this.site = site;
        onWorkResolved(workTask, site);
        return true;
    }

    /** 该行为处理的任务类型（用于通用 canUse 匹配）。 */
    protected abstract Class<? extends Task> taskType();

    /**
     * 通用 canUse 通过 {@link #taskType()} 与任务的 {@code requiredSiteType()} 校验后回调，
     * 子类可在此缓存类型化的任务/站点字段，避免在各处重复强制转换。
     */
    protected void onWorkResolved(WorkTask task, Site site) {
    }

    @Override
    public List<BlockPos> getStorageBlocks() {
        return site.getStorages();
    }

    protected void releaseWorkPosition() {
        this.workTask.releaseWorkPosition((ServerLevel) mob.level(),
                terminalPos, mob.getUUID());
    }

    protected void occupyWorkPosition(BlockPos pos) {
        this.workTask.occupyWorkPosition((ServerLevel) mob.level(),
                terminalPos, mob.getUUID(), pos);
    }

    /**
     * 工具管理阶段：若声明了所需工具则尝试获取，已持工具时 {@link #fetchToolTick} 返回
     * {@link Result#SKIP} 并继续后续阶段。返回 {@code true} 表示调用方应立即 {@code return}
     * （工具阶段已接管流程，或任务已因无可用工具而结束）。
     * <p>
     * 供各工作模板复用，消除「获取工具 → 判定 CONTINUE/TERMINATE」的重复样板；
     * 新增模板只需在 {@code tick()} 开头写 {@code if (runToolPhase(toolPredicate())) return;}。
     */
    protected boolean runToolPhase(@Nullable Predicate<ItemStack> tool) {
        if (tool == null) return false;
        Result r = fetchToolTick(tool);
        if (r == Result.CONTINUE) return true;
        if (r == Result.TERMINATE) {
            setTaskCompleted();
            return true;
        }
        return false;
    }

    /**
     * 驱动一轮「存入站点绑定容器」流程：首次调用自动启动智能存款，之后每 tick 推进。
     * 返回 true 表示本轮存款已完成（背包清空或无可存物），调用方可清除存款标志进入下一阶段。
     * 供采矿 / 农耕 / 林业 / 狩猎等「作业后集中存款」工种复用，消除各自重复的三行状态机样板。
     */
    protected boolean tickDeposit() {
        if (!isDepositing()) {
            startPriorityDeposit(site.getStorages());
        }
        return tickPriorityDeposit();
    }

    /**
     * 在工作区域内按 {@code predicate} 扫描，返回最近（由 {@code comparator} 决定）且未被其他工人占用的方块。
     * 供「区域方块作业」类工种（采矿 / 农耕 / 林业）复用，避免各自重复 betweenClosedStream 样板。
     * <p>
     * 候选方块流由 {@link #workAreaPositions()} 提供；默认基于 {@link AreaSite}。
     * 若新增自定义站点类型（如半径站点 / 线性站点），只需覆盖 {@link #workAreaPositions()}
     * 即可让所有依赖 {@code scanWorkArea} 的工种立即生效，无需改动本方法。
     */
    protected Optional<BlockPos> scanWorkArea(Predicate<BlockPos> predicate, Comparator<BlockPos> comparator) {
        return workAreaPositions()
                .filter(predicate)
                .filter(pos -> !(site instanceof AreaSite as) || !as.isPositionOccupied(pos))
                .min(comparator);
    }

    /**
     * 返回工作区域中所有候选方块位置（不可变）的流。
     * 默认实现从 {@link AreaSite} 的工作区域构造；自定义站点类型可覆盖此方法以支持其他形状。
     */
    protected Stream<BlockPos> workAreaPositions() {
        if (!(site instanceof AreaSite areaSite) || !site.hasWorkArea()) return Stream.empty();
        AABB area = site.getWorkArea().get().toAABB();
        return BlockPos.betweenClosedStream(
                        (int) area.minX, (int) area.minY, (int) area.minZ,
                        (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)
                .map(BlockPos::immutable);
    }

    /**
     * 在工作区域内寻找距离本生物最近、且通过 {@code filter} 的某类实体。
     * 供「动物产物 / 狩猎」等需在区域内定位目标实体的工种复用，消除重复的
     * 「取 {@link AreaSite} 区域 → 实体扫描 → 过滤 → 按距离取最近」样板。
     * 实体获取统一经 Context 数据基站回调（{@code EntityQuery.box(type, area)}），
     * 与全局所有检测共用同一套写法。
     * <p>
     * 新增动物类工种只需在 {@link #findTarget(ServerLevel)} 中写一行
     * {@code return findNearestInArea(level, X.class, pred);}。
     */
    protected <T extends Entity> Optional<T> findNearestInArea(ServerLevel level, Class<T> type, Predicate<T> filter) {
        if (!(site instanceof AreaSite as) || !as.hasWorkArea()) return Optional.empty();
        AABB area = as.getWorkArea().get().toAABB();
        // 获取实体统一经 Context 数据基站回调（EntityQuery 包围盒扫描 + 就近排序）
        return ExpressionContext.of(level, mob).queryEntity(
                EntityQuery.box(type, area).filter(filter)
                        .nearestTo(Vec3.atCenterOf(mob.blockPosition())));
    }
}