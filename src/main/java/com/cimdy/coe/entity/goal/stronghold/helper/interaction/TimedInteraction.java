package com.cimdy.coe.entity.goal.stronghold.helper.interaction;

import com.cimdy.coe.entity.goal.stronghold.helper.Result;

import com.cimdy.coe.entity.goal.stronghold.helper.queue.FetchQueueHandler;
import com.cimdy.coe.entity.goal.stronghold.helper.work.SmartDepositHandler;
import com.cimdy.coe.entity.goal.stronghold.helper.work.ToolManager;
import com.cimdy.coe.entity.goal.stronghold.behavior.IGoalContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * 可复用的「走近→开箱→执行→关箱」状态机基类。
 * <p>
 * {@code ContainerInteractionHandler}/{@code FetchQueueHandler}/
 * {@code SmartDepositHandler}/{@code ToolManager} 各有独立但几乎相同的 MOVING→OPENING→EXECUTING→CLOSING
 * 状态机实现，约 200 行重复代码；故收敛为基类，子类只需实现 {@link #execute(ServerLevel, BlockPos)}
 * 和 {@link #onComplete()} 即可。
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * public class MyHandler extends TimedInteraction<Void> {
 *     public MyHandler(IGoalContext ctx) { super(ctx); }
 *
 *     @Override protected boolean execute(ServerLevel level, BlockPos target) {
 *         // 执行实际的容器操作...
 *         return true; // true=成功，推进到 CLOSING；false=终止
 *     }
 *
 *     @Override protected Void buildResult() {
 *         return null;
 *     }
 * }
 * }</pre>
 *
 * @param <R> 整个交互完成后的结果类型；不需结果时用 {@code Void} 并返回 {@code null}
 */
public abstract class TimedInteraction<R> {

    protected enum State { IDLE, MOVING, OPENING, EXECUTING, CLOSING }

    protected final IGoalContext context;
    protected State state = State.IDLE;
    protected BlockPos target;
    protected int timer;
    protected int openDuration = 10;
    protected int closeDuration = 10;
    protected boolean success = true;

    protected TimedInteraction(IGoalContext context) {
        this.context = context;
    }

    // ---- lifecycle ----

    /**
     * 启动交互。子类可在覆盖中通过 {@link #setDurations(int, int)} 自定义计时。
     */
    public void start(BlockPos target) {
        reset();
        this.target = target;
        this.state = State.MOVING;
    }

    /**
     * 每 tick 调用一次，返回 {@link Result}：
     * <ul>
     *   <li>{@code CONTINUE} — 仍在进行中，下一 tick 继续调用</li>
     *   <li>{@code SKIP} — 成功完成，调用方通过 {@link #buildResult()} 获取结果</li>
     *   <li>{@code TERMINATE} — 失败终止</li>
     * </ul>
     */
    public Result tick() {
        if (state == State.IDLE) return Result.SKIP;
        ServerLevel level = context.getLevel();

        switch (state) {
            case MOVING:
                if (!context.moveToInteractionBlock(target)) return Result.CONTINUE;
                state = State.OPENING;
                timer = openDuration;
                onOpenSound(level, target);
                return Result.CONTINUE;

            case OPENING:
                if (--timer > 0) return Result.CONTINUE;
                state = State.EXECUTING;
                onEnterExecute(level, target);
                return Result.CONTINUE;

            case EXECUTING:
                success = execute(level, target);
                state = State.CLOSING;
                timer = closeDuration;
                onCloseSound(level, target);
                if (!success) {
                    reset();
                    return Result.TERMINATE;
                }
                return Result.CONTINUE;

            case CLOSING:
                if (--timer > 0) return Result.CONTINUE;
                Result result = success ? Result.SKIP : Result.TERMINATE;
                reset();
                return result;
        }
        return Result.CONTINUE;
    }

    /**
     * 是否可继续（用于 {@code canContinueToUse} 开关判断）。
     * 默认仅检查是否处于活跃状态。
     */
    public boolean canContinue() {
        return state != State.IDLE;
    }

    /** 交互是否活跃中。 */
    public boolean isActive() {
        return state != State.IDLE;
    }

    /** 重置到初始状态。 */
    public void reset() {
        state = State.IDLE;
        target = null;
        timer = 0;
        success = true;
    }

    // ---- configuration ----

    /** 自定义开关计时（tick）。必须在 {@link #start} 之前调用。 */
    protected void setDurations(int openTicks, int closeTicks) {
        this.openDuration = openTicks;
        this.closeDuration = closeTicks;
    }

    // ---- extension points ----

    /**
     * 在抵达目标、即将开始执行时回调。默认无操作。
     * 子类可在此初始化执行所需的状态。
     */
    protected void onEnterExecute(ServerLevel level, BlockPos target) {}

    /**
     * 核心：在容器就绪后执行的实际操作。
     * @return true 表示操作成功，推进到 CLOSING；false 表示失败，立即终止。
     */
    protected abstract boolean execute(ServerLevel level, BlockPos target);

    /**
     * 交互成功完成后构建结果对象。
     * 调用方在收到 {@code SKIP} 后通过此方法获取返回值。
     */
    public abstract R buildResult();

    // ---- state accessors ----

    public BlockPos getTarget() { return target; }
    public State getState() { return state; }

    // ---- sound defaults (override as needed) ----

    protected void onOpenSound(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    protected void onCloseSound(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 1.0f, 0.8f);
    }
}
