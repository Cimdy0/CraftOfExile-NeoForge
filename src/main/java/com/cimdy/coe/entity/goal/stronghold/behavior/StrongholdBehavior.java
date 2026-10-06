package com.cimdy.coe.entity.goal.stronghold.behavior;

import com.cimdy.coe.stronghold.energy.WorkerAIInjector;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 据点工人行为契约（方案 D）。
 * <p>
 * 将原本硬编码在 {@code WorkerAIInjector.injectStrongholdAI} 中的「优先级表 + 目标构造」解耦为可注册行为。
 * 第三方 / 新生物只需注册一个 {@link StrongholdBehavior} 即可扩展据点 AI，核心零改动
 * （与现有 TaskTypes / SiteTypes 注册表哲学一致）。
 */
public interface StrongholdBehavior {
    /** 行为优先级，数值越小越先被加入目标选择器（0=进食 … 3=闲逛）。 */
    int priority();

    /**
     * 该行为是否适用于指定生物。默认对所有生物生效；可重写为按种类过滤
     * （例如末影人→EnderCharge，炽足兽→Heater）。
     */
    default boolean appliesTo(Mob mob) {
        return true;
    }

    /** 为该生物创建一个目标（Goal）。 */
    Goal create(Mob mob, IGoalContext ctx);

    /**
     * 便捷工厂：用优先级与「仅依赖 Mob 的构造器」创建行为。
     */
    static StrongholdBehavior of(int priority, Function<? super Mob, ? extends Goal> factory) {
        return of(priority, (mob, ctx) -> factory.apply(mob));
    }

    /**
     * 便捷工厂：用优先级与「依赖 Mob + IGoalContext 的构造器」创建行为。
     */
    static StrongholdBehavior of(int priority, BiFunction<? super Mob, ? super IGoalContext, ? extends Goal> factory) {
        return new StrongholdBehavior() {
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
     */
    static StrongholdBehavior of(int priority, Predicate<? super Mob> appliesTo, Function<? super Mob, ? extends Goal> factory) {
        return of(priority, appliesTo, (mob, ctx) -> factory.apply(mob));
    }

    /**
     * 便捷工厂：优先级 + 生物过滤 + 依赖 Mob/IGoalContext 的构造器。
     */
    static StrongholdBehavior of(int priority, Predicate<? super Mob> appliesTo, BiFunction<? super Mob, ? super IGoalContext, ? extends Goal> factory) {
        return new StrongholdBehavior() {
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
