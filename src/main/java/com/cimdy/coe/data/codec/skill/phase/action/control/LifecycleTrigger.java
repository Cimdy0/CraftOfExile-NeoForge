package com.cimdy.coe.data.codec.skill.phase.action.control;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * 生命周期触发器 — 技能执行的各个阶段。
 * 回答"技能执行到哪个阶段"（与回答"游戏发生了什么"的事件条件互补）。
 *
 * PoE2 新增：
 * - ON_PRE_CAST: 条件判断后、施放前（可中途取消）
 * - ON_COMBO: 连击序列的每一步
 * - ON_PERFECT_TIMING: 完美释放窗口（PoE2 的完美施放）
 * - ON_RELEASE: 松开按键时（引导型技能结束）
 */
public enum LifecycleTrigger implements StringRepresentable {
    ON_PRE_CAST("on_pre_cast"),      // 施法前 (费用检查后、动作执行前)
    ON_CAST("on_cast"),              // 施法时 (Delivery 创建前)
    ON_HIT("on_hit"),                // 投递命中实体/方块时
    ON_TICK("on_tick"),              // 持续型投递每 tick
    ON_EXPIRE("on_expire"),          // 投递到期消失时
    ON_KILL("on_kill"),              // 动作击杀实体时
    ON_COMBO("on_combo"),            // PoE2: 连击序列步骤
    ON_PERFECT_TIMING("on_perfect_timing"), // PoE2: 完美释放窗口命中
    ON_RELEASE("on_release"),        // PoE2: 引导/持续技能松开时
    /** 事件监听触发时（{@code EventListenPhase} 回调）。
     *  与 {@link #ON_PICKUP} 的区分是必要的：残片拾取等「投递物回调」不应在事件触发的同一时刻执行。 */
    ON_LISTEN("on_listen"),
    /** 残片被拾取时（残片形态的 {@code PhaseOnHit} 点燃本触发器）。 */
    ON_PICKUP("on_pickup");

    public static final Codec<LifecycleTrigger> CODEC = StringRepresentable.fromEnum(LifecycleTrigger::values);

    private final String name;

    LifecycleTrigger(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
