package com.cimdy.coe.data.codec.support.stat.engine;

import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.event.combat.DamageEvent;
import com.cimdy.coe.event.stat.StatContextEvent;
import com.cimdy.coe.event.stat.StatEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 事件作用域的进入/退出辅助。消费端（伤害计算、技能结算等）应在一次行为解析期间
 * 用 try-with-resources 进入对应作用域，使该行为内所有 stat 读取都按同一作用域即时求值：
 *
 * <pre>{@code
 * try (var s = StatScopes.enter(living, StatContext.of(GateTag.ATTACK, GateTag.ELEMENT_FIRE))) {
 *     // ... 本次攻击的伤害/暴击等结算，读取的属性自动带对应标签的 stat 加成 ...
 * }
 * }</pre>
 *
 * 作用域栈存于实体级 {@link ExpressionContext}，作用域内嵌套读取（含 math 表达式副本）共享同一栈。
 */
public final class StatScopes {

    private StatScopes() {
    }

    /**
     * 进入作用域，返回 AutoCloseable 以便 try-with-resources 自动退出。
     * 同时发布 {@link StatContextEvent}（enter / exit），把本次行为作用域「二次转发」成事件，
     * 供 Stat 系统（{@link com.cimdy.coe.event.stat.StatEvent}）以事件驱动方式响应（主动刷新 / 广播）。
     */
    public static AutoCloseable enter(LivingEntity living, StatContext context) {
        ExpressionContext ctx = ExpressionContext.get(living);
        ctx.pushStatScope(context);
        // push 与返回的 AutoCloseable 保持原子：post 若抛异常，此时调用方尚未拿到 closeable，
        // 不能负责出栈——若不回滚，本体永久泄漏一层（peek() 持续返回脏顶，
        // 其 tags 被合并进后续每次读取，静默命中本不应命中的条目）。
        // 因此此处必须回滚：保证「进入者拿到了 closeable」与「堆多了一层」互排。
        try {
            NeoForge.EVENT_BUS.post(new StatContextEvent.Enter(living, context));
        } catch (RuntimeException | Error e) {
            ctx.popStatScope();
            throw e;
        }
        return new StatScope(living, ctx, context);
    }

    /** 作用域 AutoCloseable：退出时出栈并发布 {@link StatContextEvent.Exit}。close() 不抛受检异常。 */
    private static final class StatScope implements AutoCloseable {
        private final LivingEntity living;
        private final ExpressionContext ctx;
        private final StatContext context;

        StatScope(LivingEntity living, ExpressionContext ctx, StatContext context) {
            this.living = living;
            this.ctx = ctx;
            this.context = context;
        }

        @Override
        public void close() {
            ctx.popStatScope();
            NeoForge.EVENT_BUS.post(new StatContextEvent.Exit(living, context));
        }
    }

    /** 读取当前（最内层）作用域。 */
    public static StatContext current(LivingEntity living) {
        return ExpressionContext.get(living).currentStatContext();
    }

    /**
     * 把 {@code ctx} 并入当前栈顶外层作用域（读值类嵌套作用域用，如 AilmentHelper.withScope /
     * DamageHelper.readInScopePublic / 命中结算）——保留外层行为标签（spell / attack / channeling / 技能名等）。
     * 栈顶读取只取栈顶，若不并入，压入的子作用域会遮蔽外层标签，导致「with Spells」类条目不命中。
     * 与 {@code DamageEvent.mergeWithCurrent} / {@code SkillStatScopes.enterAction} 的合并策略同源
     * （本方法为其统一收敛点）。sourceEntity 一并并入（优先采用本层传入值）。
     */
    public static StatContext mergeWithCurrent(LivingEntity living, StatContext ctx) {
        StatContext cur = current(living);
        java.util.Set<String> tags = new java.util.HashSet<>(cur.tags());
        tags.addAll(ctx.tags());
        Identifier skill = ctx.skill() != null ? ctx.skill() : cur.skill();
        LivingEntity source = ctx.sourceEntity() != null ? ctx.sourceEntity() : cur.sourceEntity();
        return StatContext.of(tags, skill).withSourceEntity(source);
    }

    /**
     * 进入「并入当前栈顶外层标签」的作用域：{@link #enter} + {@link #mergeWithCurrent} 的组合，
     * 供读值类作用域使用（保留 spell / attack 等外层行为标签），行为与 {@link #enter} 完全对称。
     */
    public static AutoCloseable enterMerged(LivingEntity living, StatContext ctx) {
        return enter(living, mergeWithCurrent(living, ctx));
    }
}
