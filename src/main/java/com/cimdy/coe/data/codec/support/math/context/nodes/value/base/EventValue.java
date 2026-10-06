package com.cimdy.coe.data.codec.support.math.context.nodes.value.base;

import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.EventNode;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventCatalog;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 当前触发事件读值节点：
 * <ul>
 *   <li>{@code event:<name>} —— 返回 1.0 当本上下文当前事件名等于 {@code <name>}，否则 0.0。</li>
 *   <li>{@code event_tag:<name>:<tag>} —— 返回 1.0 当当前事件名等于 {@code <name>} 且事件标签含 {@code <tag>}，否则 0.0。
 *        用于「同一通用事件名 + 具体类型标签」收敛判定（如 {@code event_tag:caused_ailment:ignite} 命中造成点燃）。</li>
 * </ul>
 * 事件名 / 标签由 {@link ExpressionContext#getCurrentEventName()} / {@link ExpressionContext#getCurrentEventTags()} 提供
 * （元事件经 SkillEventBus 发布携带 tags，在 MetaSkillAction.execute 求值前注入）。
 */
public record EventValue(String eventName, @Nullable String tag) implements EventNode {

    /** 仅按事件名判定（{@code event:<name>}）。 */
    public EventValue(String eventName) {
        this(eventName, null);
    }

    public static final Codec<EventValue> CODEC = RecordCodecBuilder.<EventValue>create(i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(EventValue::eventName),
            Codec.STRING.optionalFieldOf("tag").forGetter(v -> Optional.ofNullable(v.tag))
    ).apply(i, (n, t) -> new EventValue(n, t.orElse(null)))
    ).flatXmap(
            (EventValue v) -> EventCatalog.checkEventKey(v, v.eventName()),
            (EventValue v) -> DataResult.success(v));

    @Override
    public double evaluate(ValueScope scope) {
        return EventNode.super.evaluate(scope);
    }

    @Override
    public double evaluate(Event.State state, ValueScope scope) {
        ExpressionContext ctx = scope.root();
        String cur = ctx.getCurrentEventName();
        if (cur == null || !cur.equals(eventName)) return 0.0;
        if (tag != null) return ctx.getCurrentEventTags().contains(tag) ? 1.0 : 0.0;
        return 1.0;
    }

    /** 名词短语：事件名 → 值键 {@code stat.value.event.<name>}（复用 P2 event 命名空间）。 */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("event", eventName));
    }

    /** 条件从句：当前事件名等于 {@code <name>}（可带 tag）时。 */
    @Override
    public List<Component> describeFragment() {
        Component name = describeQuantity(ClientLocale.get()).get(0);
        if (tag != null) {
            return List.of(Component.translatable("stat.condition.current_event_tag", name, StatValueKey.of("event_tag", tag)));
        }
        return List.of(Component.translatable("stat.condition.current_event", name));
    }
}
