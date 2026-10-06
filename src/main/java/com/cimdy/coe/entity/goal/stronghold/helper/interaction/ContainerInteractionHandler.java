package com.cimdy.coe.entity.goal.stronghold.helper.interaction;

import com.cimdy.coe.entity.goal.stronghold.behavior.IGoalContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Consumer;

/**
 * 单次容器交互处理器（承运等简单场景使用）。
 * 基于 {@link TimedInteraction}，仅需提供 {@link Consumer} 动作。
 */
public class ContainerInteractionHandler extends TimedInteraction<Void> {

    public enum InteractionState { IDLE, APPROACHING, OPENING, EXECUTING, CLOSING }

    private Consumer<ServerLevel> action;

    public ContainerInteractionHandler(IGoalContext context) {
        super(context);
    }

    public void start(BlockPos target, Consumer<ServerLevel> action) {
        this.action = action;
        super.start(target);
    }

    @Override
    protected boolean execute(ServerLevel level, BlockPos target) {
        if (action != null) {
            action.accept(level);
            return true;
        }
        return false;
    }

    @Override
    public Void buildResult() { return null; }

    /** 兼容旧 API：返回 {@link InteractionState} 枚举。 */
    public InteractionState getInteractionState() {
        return switch (state) {
            case IDLE -> InteractionState.IDLE;
            case MOVING -> InteractionState.APPROACHING;
            case OPENING -> InteractionState.OPENING;
            case EXECUTING -> InteractionState.EXECUTING;
            case CLOSING -> InteractionState.CLOSING;
        };
    }
}
