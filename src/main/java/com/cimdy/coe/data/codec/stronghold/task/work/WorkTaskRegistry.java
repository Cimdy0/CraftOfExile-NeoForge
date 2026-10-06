package com.cimdy.coe.data.codec.stronghold.task.work;

import com.cimdy.coe.entity.goal.stronghold.behavior.BehaviorRegistry;
import com.cimdy.coe.data.codec.stronghold.task.Task;

import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * 工作任务生成注册表（与 {@code BehaviorRegistry} 同构）。
 * <p>
 * 将「站点方块 → 具体 WorkTask 子类」的映射从 {@link WorkTask#generateTask} 的硬编码 if-else 链
 * 改为数据驱动：每个工种注册一个「方块谓词 + 任务生成器」条目即可。内建工种由
 * {@link WorkTypeRegistry} 统一提供（一处声明 Site+Task+Goal）；第三方模组可通过
 * {@code META-INF/services/com.cimdy.coe.data.codec.stronghold.task.work.WorkTaskFactory} 声明实现，
 * 实现「方块 → 工作任务」映射的零核心改动扩展。
 */
public final class WorkTaskRegistry {
    private static final List<WorkTaskFactory> FACTORIES = new ArrayList<>();
    private static boolean initialized = false;

    private WorkTaskRegistry() {}

    /** 注册一个「方块谓词 → 任务生成器」条目。 */
    public static synchronized void register(Predicate<BlockState> predicate,
                                             BiFunction<ServerLevel, Site, Task> generator) {
        FACTORIES.add(new WorkTaskFactory() {
            @Override
            public Predicate<BlockState> predicate() {
                return predicate;
            }

            @Override
            public BiFunction<ServerLevel, Site, Task> generator() {
                return generator;
            }
        });
    }

    /** 依据站点方块状态生成对应的工作任务；无匹配返回 null。 */
    public static Task generate(ServerLevel level, Site site) {
        ensureInitialized();
        BlockState state = level.getBlockState(site.getSitePos());
        for (WorkTaskFactory factory : FACTORIES) {
            if (factory.predicate().test(state)) {
                return factory.generator().apply(level, site);
            }
        }
        return null;
    }

    public static void ensureInitialized() {
        if (initialized) return;
        initialized = true;

        // 内建工种统一源自 WorkTypeRegistry（一处声明 Site+Task+Goal），消除平行清单
        for (WorkType type : WorkTypeRegistry.workTypes()) {
            register(type.blockPredicate(), type.generator());
        }

        // 第三方 / 外部模块通过 META-INF/services 声明的工作任务工厂（零核心改动扩展）
        discoverExternal();
    }

    /**
     * 通过 {@link ServiceLoader} 自动发现并注册所有声明在
     * {@code META-INF/services/com.cimdy.coe.data.codec.stronghold.task.work.WorkTaskFactory} 中的工作任务工厂。
     * <p>
     * 第三方模组只需在自己的 jar 中提供该 services 文件并列出 {@link WorkTaskFactory} 实现类，
     * 即可在不修改本核心注册表的前提下为「方块 → 工作任务」映射新增条目。无 services 文件时为空操作，安全无副作用。
     */
    private static void discoverExternal() {
        for (WorkTaskFactory factory : ServiceLoader.load(WorkTaskFactory.class, WorkTaskFactory.class.getClassLoader())) {
            register(factory.predicate(), factory.generator());
        }
    }
}
