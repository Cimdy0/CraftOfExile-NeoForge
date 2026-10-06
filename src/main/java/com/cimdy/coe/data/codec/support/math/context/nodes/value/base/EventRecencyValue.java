package com.cimdy.coe.data.codec.support.math.context.nodes.value.base;

import com.cimdy.coe.data.codec.support.math.context.node.EventNode;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

/**
 * 事件近期状态读值节点（从 {@link LivingStateValue} 的 5 个事件模式抽取，归并于 EventNode）：
 * parrying / leeching / leeching_mana / recently_took_dot / recently_gained_rage。
 * <p>语义与 {@link LivingStateValue} 原内联逻辑逐字一致（见 LivingStateValue 旧 case 分支）。
 * 经 {@link #evaluate(Event.State, ValueScope)} 在解析出的事件状态上读值；
 * recently_gained_rage 走 {@link ExpressionContext} 的 timer 通道（scope.current() 实体），不需 state。
 * JSON 形式 {"type":"event_recency","mode":"<mode>","window":<ticks>}，window 缺省 80.0（与 LivingStateValue 一致）。
 */
public record EventRecencyValue(String mode, double window) implements EventNode {

    public static final Codec<EventRecencyValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("mode", "parrying").forGetter(EventRecencyValue::mode),
            Codec.DOUBLE.optionalFieldOf("window", 80.0).forGetter(EventRecencyValue::window)
    ).apply(instance, EventRecencyValue::new));

    @Override
    public double evaluate(Event.State state, ValueScope scope) {
        return switch (mode) {
            case "parrying" -> state != null && state.recently(EventName.PARRY, (int) window) ? 1D : 0D;
            case "leeching" -> state != null && (state.recently(EventName.LEECH, GateTag.HEALTH, (int) window, Event.State.Match.PREFIX)
                    || state.recently(EventName.LEECH, GateTag.MANA, (int) window, Event.State.Match.PREFIX)
                    || state.recently(EventName.LEECH, GateTag.ENERGY, (int) window, Event.State.Match.PREFIX)) ? 1D : 0D;
            case "leeching_mana" -> state != null && state.recently(EventName.LEECH, GateTag.MANA, (int) window, Event.State.Match.PREFIX) ? 1D : 0D;
            case "recently_took_dot" -> state != null && state.recently(EventName.TAKEN_DOT, (int) window) ? 1D : 0D;
            case "recently_gained_rage" -> {
                LivingEntity current = scope.current();
                yield current != null && ExpressionContext.get(current).isTimerActive(EventName.RAGE_GAINED) ? 1D : 0D;
            }
            default -> 0D;
        };
    }
}
