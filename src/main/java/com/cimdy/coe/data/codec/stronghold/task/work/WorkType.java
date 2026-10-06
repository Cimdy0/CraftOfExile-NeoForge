package com.cimdy.coe.data.codec.stronghold.task.work;

import com.cimdy.coe.entity.goal.stronghold.behavior.BehaviorRegistry;
import com.cimdy.coe.data.codec.stronghold.task.BasicTask;
import com.cimdy.coe.data.codec.stronghold.task.Task;

import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.SiteTypes;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * 单一工种声明：在一处绑定「站点类型 + 任务类 + 目标入口 + 任务生成器 + 方块谓词」，
 * 取代分散在 {@link com.cimdy.coe.data.codec.stronghold.task.site.SiteTypes}、
 * {@link WorkTaskRegistry}、{@link com.cimdy.coe.entity.goal.stronghold.behavior.BehaviorRegistry}
 * 三处的平行清单，确保 Site↔Task↔Goal 的对应在编译期即可校验（引用错字段即编译失败）。
 * <p>
 * 仅收纳「基于 {@link WorkTask} 的 8 个工种」；FE 充能 / 熔炉加热等任务为
 * {@code BasicTask}（无对应站点、不经由方块生成），其目标仍由 {@code BehaviorRegistry} 显式注册。
 */
public record WorkType(
        Site.Type<?> siteType,
        Class<? extends WorkTask> taskClass,
        WorkGoalEntry goalEntry,
        BiFunction<ServerLevel, Site, Task> generator,
        Predicate<BlockState> blockPredicate
) {
    public static WorkType of(Site.Type<?> siteType, Class<? extends WorkTask> taskClass,
                              WorkGoalEntry goalEntry,
                              BiFunction<ServerLevel, Site, Task> generator,
                              Predicate<BlockState> blockPredicate) {
        return new WorkType(siteType, taskClass, goalEntry, generator, blockPredicate);
    }

    /**
     * 该工种在进度系统里的名字（如 {@code mining}），供
     * {@code job_charter} 授权书、科技的 {@code unlock_work_types}、研究的效率加成三处引用。
     * 由站点注册 id 派生，因此新增工种无需再手写一份名字清单。
     */
    public String name() {
        return SiteTypes.workTypeName(siteType);
    }
}
