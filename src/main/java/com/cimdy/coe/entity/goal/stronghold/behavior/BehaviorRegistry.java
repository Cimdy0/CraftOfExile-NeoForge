package com.cimdy.coe.entity.goal.stronghold.behavior;

import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.TaskGenerator;
import com.cimdy.coe.data.codec.stronghold.task.work.WorkTypeRegistry;
import com.cimdy.coe.entity.goal.stronghold.impl.*;
import com.cimdy.coe.entity.goal.stronghold.impl.work.misc.ConvertGoal;
import com.cimdy.coe.entity.goal.stronghold.impl.work.misc.EnchantGoal;
import com.cimdy.coe.entity.goal.stronghold.impl.work.charge.FEChargeGoal;
import com.cimdy.coe.entity.goal.stronghold.impl.work.charge.HeaterGoal;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 工人行为注册表（方案 D 增强版）。
 * <p>
 * 内建行为通过 {@link #ensureInitialized()} 自动注册。
 * 新增第三方工作目标请使用 {@link #registerWorkGoal(WorkGoalEntry)} 自注册。
 * <p>
 * 行为按 {@link StrongholdBehavior#priority()} 升序加入目标选择器。
 */
public final class BehaviorRegistry {
    private static final List<StrongholdBehavior> BEHAVIORS = new ArrayList<>();
    private static boolean initialized = false;

    private BehaviorRegistry() {}

    // ========== 公共 API ==========

    /** 注册一个行为（所有生物生效）。 */
    public static synchronized void register(StrongholdBehavior behavior) {
        BEHAVIORS.add(behavior);
    }

    /**
     * 以工作目标条目形式注册一个行为。
     * <p>
     * 由于 {@link WorkGoalEntry} 继承自 {@link StrongholdBehavior}，此处直接复用 {@link #register(StrongholdBehavior)}，
     * 不再额外包装。新增工种（如你的自定义冶炼/酿造 Goal）可在类的静态初始化块中调用：
     * <pre>{@code
     * BehaviorRegistry.registerWorkGoal(MyBrewGoal.ENTRY);
     * }</pre>
     * 或等价地 {@code BehaviorRegistry.register(MyBrewGoal.ENTRY)}。核心 BehaviorRegistry 无需任何改动。
     */
    public static synchronized void registerWorkGoal(WorkGoalEntry entry) {
        register(entry);
    }

    /** 将适用行为按优先级注入到指定生物的 goalSelector。 */
    public static void applyTo(Mob mob, IGoalContext ctx) {
        ensureInitialized();
        BEHAVIORS.stream()
                .filter(b -> b.appliesTo(mob))
                .sorted(Comparator.comparingInt(StrongholdBehavior::priority))
                .forEach(b -> mob.goalSelector.addGoal(b.priority(), b.create(mob, ctx)));
    }

    // ========== 内建行为注册（仅首次执行） ==========

    public static void ensureInitialized() {
        if (initialized) return;
        initialized = true;

        // 生存行为
        register(EatGoal.ENTRY);
        register(CarryGoal.ENTRY);

        // 工作行为：基于 WorkTask 的 8 个工种统一源自 WorkTypeRegistry（一处声明即绑定 Site+Task+Goal）
        WorkTypeRegistry.workTypes().forEach(type -> registerWorkGoal(type.goalEntry()));
        // 特例：FE 充能 / 熔炉加热的任务为 BasicTask（无对应站点、不经由方块生成），目标仍显式注册
        registerWorkGoal(FEChargeGoal.ENTRY);
        registerWorkGoal(HeaterGoal.ENTRY);
        register(CraftGoal.ENTRY);
        // 转化（烹饪/冶炼/堆肥/研磨）与合成同构，由 TaskGenerator 生成 DAG 链，执行器为 ConvertGoal
        registerWorkGoal(ConvertGoal.ENTRY);
        // 附魔应用（使用附魔书给物品附魔，消耗青金石与末影能量），由 TaskGenerator 生成链，执行器为 EnchantGoal
        registerWorkGoal(EnchantGoal.ENTRY);

        // 通用行为
        registerWorkGoal(StoreExcessGoal.ENTRY);
        register(WanderingGoal.ENTRY);

        // 第三方 / 外部模块通过 META-INF/services 声明的工作目标（零核心改动扩展）
        discoverExternalWorkGoals();
    }

    /**
     * 通过 {@link ServiceLoader} 自动发现并注册所有声明在
     * {@code META-INF/services/com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdBehavior} 中的据点行为。
     * <p>
     * 以 {@link StrongholdBehavior}（而非其子接口 {@link WorkGoalEntry}）作为服务基类，使第三方模组既能注册
     * 工作目标（{@code WorkGoalEntry} 实现类，因其继承自 {@link StrongholdBehavior} 同样会被发现），
     * 也能注册任意非工作类据点行为（如自定义的进食/搬运/闲逛行为），实现「零核心改动」的全面扩展。
     * 无 services 文件时为空操作，安全无副作用。
     */
    private static void discoverExternalWorkGoals() {
        for (StrongholdBehavior behavior : ServiceLoader.load(StrongholdBehavior.class, StrongholdBehavior.class.getClassLoader())) {
            register(behavior);
        }
    }
}
