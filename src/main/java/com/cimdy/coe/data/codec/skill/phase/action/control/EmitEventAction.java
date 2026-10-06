package com.cimdy.coe.data.codec.skill.phase.action.control;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.CustomSkillEvent;
import com.cimdy.coe.data.codec.skill.phase.event.EventListenPhase;
import com.cimdy.coe.data.codec.skill.phase.event.SkillEventType;
import com.cimdy.coe.data.codec.skill.context.SkillDamageSource;
import com.cimdy.coe.event.combat.GameEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.Set;

/**
 * 发射事件动作 — 向 SkillEventBus 发送事件，<b>同时接入 Context / Stat 通道</b>。
 * <p>
 * 三写范式（与 {@code AilmentHelper.fireAilmentCaused}、{@code MetaSkillAction} 同源）：
 * <ol>
 *   <li>{@code SkillEventBus.publish}——技能内部事件总线，供 {@code EventListenPhase} 消费；</li>
 *   <li>{@code ExpressionContext.refreshTimer}——写入 Context 计时事件，供 {@code recently()} / {@code count()} 查询；</li>
 *   <li>{@code StatEngine.fire}——通知 Stat 触发通道，使数据侧自定义事件可驱动 on-cause Stat 效果。</li>
 * </ol>
 * 事件名收口为 {@code skill_event:<type>}（CUSTOM 亚型附带 data 中的 {@code name} 以便细分）。
 */
public record EmitEventAction(
        SkillEventType eventType,
        Optional<CompoundTag> data
) implements Action {
    public static final Codec<EmitEventAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            SkillEventType.CODEC.fieldOf("event_type").forGetter(EmitEventAction::eventType),
            CompoundTag.CODEC.optionalFieldOf("data").forGetter(EmitEventAction::data)
    ).apply(i, EmitEventAction::new));

    @Override public Action.Type<?> getType() { return ActionTypes.EMIT_EVENT; }

    /** 事件名前缀：技能自定义事件统一命名空间，避免与 {@code EventName} 内置事件名冲突。 */
    public static final String EVENT_PREFIX = "skill_event:";

    /**
     * 总线事件名 —— 取 {@link SkillEventType#getSerializedName()}（小写短横线形态）。
     * <p>
     * <b>不可用 {@code eventType.name()}</b>：{@link EventListenPhase} 的 {@code event_name}
     * 是数据包自由书写的 {@code Codec.STRING}，与总线名做<b>精确字符串相等</b>比较，
     * 而既有发布方 {@code SkillEventBus.publishMeta} 一律传 {@code on_crit} / {@code on_ignite}
     * 这类小写序列化名。若此处发大写枚举名（{@code "CUSTOM"}），数据包侧永远无法命中。
     */
    private String busEventName() {
        return eventType.getSerializedName();
    }

    /** Context / Stat 通道事件名（加前缀避免与内置 {@code EventName} 常量撞名）。 */
    private String scopedEventName() {
        return EVENT_PREFIX + eventType.getSerializedName();
    }

    @Override
    public void execute(ActionContext context) {
        context.eventBus().publish(new CustomSkillEvent(
                context.caster(), context.skill(),
                busEventName(), data
        ));

        if (!(context.caster() instanceof LivingEntity owner)) return;
        LivingEntity target = context.target().orElse(null) instanceof LivingEntity l ? l : null;

        // ②③ Context 计时 + Stat 触发通道：统一走 GameEvent.emit 通用发射原语（与全工程其它事件点同源），
        // 携带「带前缀事件名 + 裸事件类型名」两个粒度标签与技能作用域，不再在此手写展开。
        GameEvent.emit(scopedEventName(), owner, target,
                Set.of(busEventName()), SkillDamageSource.skillIdOf(context.skill()), 1);
    }
}
