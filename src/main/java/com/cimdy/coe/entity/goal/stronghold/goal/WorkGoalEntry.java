package com.cimdy.coe.entity.goal.stronghold.goal;

import com.cimdy.coe.entity.goal.stronghold.behavior.BehaviorRegistry;
import com.cimdy.coe.entity.goal.stronghold.behavior.IGoalContext;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdBehavior;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.impl.work.charge.EnderChargeGoal;
import com.cimdy.coe.entity.goal.stronghold.impl.work.block.FarmerGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 工作目标可发现条目。
 * <p>
 * 本接口继承自 {@link StrongholdBehavior}，因此一个 {@code WorkGoalEntry} 本身就是一个完整的行为契约，
 * 可直接通过 {@link BehaviorRegistry#register(StrongholdBehavior)} 注册，无需额外包装。
 * <p>
 * 实现此接口的工作目标类可通过以下两种方式注册，核心 {@link BehaviorRegistry} 均无需改动：
 * <ul>
 *   <li>程序化：{@code BehaviorRegistry.registerWorkGoal(MyGoal.ENTRY)}（或 {@code register(MyGoal.ENTRY)}）；</li>
 *   <li>声明式：在 {@code META-INF/services/com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdBehavior}
 *       中列出类名（{@code WorkGoalEntry} 继承自 {@link StrongholdBehavior}，同样会被发现），
 *       由 {@link BehaviorRegistry} 在 {@code ensureInitialized()} 时通过
 *       {@link java.util.ServiceLoader} 自动发现（第三方模组零核心改动扩展）。</li>
 * </ul>
 *
 * <h3>最简用法（配合 {@link #of(int, Function)} 工厂）</h3>
 * <pre>{@code
 * public class FarmerGoal extends WorkGoal {
 *     public FarmerGoal(Mob mob) { super(mob); }
 *     public static final WorkGoalEntry ENTRY =
 *             WorkGoalEntry.of(StrongholdPriority.WORK, FarmerGoal::new);
 * }
 * }</pre>
 *
 * <h3>需要 IGoalContext 的用法</h3>
 * <pre>{@code
 * public static final WorkGoalEntry ENTRY =
 *         WorkGoalEntry.of(StrongholdPriority.WORK, (mob, ctx) -> new FarmerGoal(mob));
 * }</pre>
 */
public interface WorkGoalEntry extends StrongholdBehavior {

    /**
     * 便捷工厂：用优先级与「仅依赖 Mob 的构造器」创建条目。
     * 大多数工作目标无需 {@link IGoalContext}，使用此重载最简洁。
     */
    static WorkGoalEntry of(int priority, Function<? super Mob, ? extends Goal> factory) {
        return of(priority, (mob, ctx) -> factory.apply(mob));
    }

    /**
     * 便捷工厂：用优先级与「依赖 Mob + IGoalContext 的构造器」创建条目。
     */
    static WorkGoalEntry of(int priority, BiFunction<? super Mob, ? super IGoalContext, ? extends Goal> factory) {
        return new WorkGoalEntry() {
            @Override
            public int priority() {
                return priority;
            }

            @Override
            public Goal create(Mob mob, IGoalContext ctx) {
                return factory.apply(mob, ctx);
            }
        };
    }

    /**
     * 便捷工厂：在 {@link #of(int, BiFunction)} 基础上额外声明「仅对满足条件的生物生效」。
     * 例如末影人专属工种：
     * <pre>{@code
     * WorkGoalEntry.of(StrongholdPriority.WORK, mob -> mob instanceof EnderMan, EnderChargeGoal::new);
     * }</pre>
     */
    static WorkGoalEntry of(int priority, Predicate<? super Mob> appliesTo, Function<? super Mob, ? extends Goal> factory) {
        return of(priority, appliesTo, (mob, ctx) -> factory.apply(mob));
    }

    /**
     * 便捷工厂：优先级 + 生物过滤 + 依赖 Mob/IGoalContext 的构造器。
     */
    static WorkGoalEntry of(int priority, Predicate<? super Mob> appliesTo, BiFunction<? super Mob, ? super IGoalContext, ? extends Goal> factory) {
        return new WorkGoalEntry() {
            @Override
            public int priority() {
                return priority;
            }

            @Override
            public boolean appliesTo(Mob mob) {
                return appliesTo.test(mob);
            }

            @Override
            public Goal create(Mob mob, IGoalContext ctx) {
                return factory.apply(mob, ctx);
            }
        };
    }
}
