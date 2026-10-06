package com.cimdy.coe.event.stat;

import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/**
 * 事件作用域进入 / 退出事件：由 {@link com.cimdy.coe.data.codec.support.stat.engine.StatScopes#enter}
 * 在 push / pop 作用域栈时发布。
 *
 * <p>意义：当前 stat 读取是「读时按作用域即时计算」（lazy），但某些系统（UI 实时显示、条件状态机）
 * 需要以「事件驱动」方式感知作用域切换并主动刷新。本事件把一次行为的开始 / 结束暴露成可被订阅的事件，
 * 使 Stat 系统（{@link StatEvent}）能在进入作用域时二次转发并主动更新，而非仅被动等待读取。
 */
public sealed class StatContextEvent extends Event
        permits StatContextEvent.Enter, StatContextEvent.Exit {

    private final LivingEntity entity;
    private final StatContext context;

    private StatContextEvent(LivingEntity entity, StatContext context) {
        this.entity = entity;
        this.context = context;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public StatContext getContext() {
        return context;
    }

    /** 作用域进入：本次行为开始，带相应标签的 stat 加成从此生效。 */
    public static final class Enter extends StatContextEvent {
        public Enter(LivingEntity entity, StatContext context) {
            super(entity, context);
        }
    }

    /** 作用域退出：本次行为结束，栈回到外层作用域。 */
    public static final class Exit extends StatContextEvent {
        public Exit(LivingEntity entity, StatContext context) {
            super(entity, context);
        }
    }
}
