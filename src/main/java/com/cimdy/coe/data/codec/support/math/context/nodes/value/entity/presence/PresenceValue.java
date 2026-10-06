package com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.CompareValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.PresenceDependent;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.world.EntityOperationEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * Presence（实体进/出范围）读值节点。
 * <p>
 * 读取追踪者（{@code ExpressionContext} 属主）的 Presence 记录，判定被判断实体（当前候选）
 * 是否在窗口内进入 / 离开追踪者范围：
 * <ul>
 *   <li>{@code mode = "entered"}（中缀 {@code recently_entered:<window>}）—— 候选在窗口内进入 → 1，否则 0；</li>
 *   <li>{@code mode = "left"}（中缀 {@code recently_left:<window>}）—— 候选在窗口内离开 → 1，否则 0。</li>
 * </ul>
 * <p>数据由 {@code EntityOperationEvent} 的每 tick 全域扫描维护——该扫描<b>按需启用</b>：
 * 仅当有 stat 条目引用本节点（经 {@code EventRefCollector} 置位 presence 需求）时才执行，
 * 无引用时扫描恒关闭（零开销）。
 */
public record PresenceValue(String mode, double window) implements EntityNode, PresenceDependent {

    public static final Codec<PresenceValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("mode").forGetter(PresenceValue::mode),
            Codec.DOUBLE.optionalFieldOf("window", 200.0).forGetter(PresenceValue::window)
    ).apply(instance, PresenceValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        if (!(scope.root().getEntity() instanceof LivingEntity tracker)) return 0D;
        Event.State state = Event.get(tracker);
        if ("left".equals(mode)) {
            return state.recentlyLeft(living.getUUID(), (int) window) ? 1D : 0D;
        }
        return state.recentlyEntered(living.getUUID(), (int) window) ? 1D : 0D;
    }

    /** 名词短语：在场模式 → 值键 {@code stat.value.presence.<mode>}（CompareValue 操作数）。 */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("presence", mode));
    }

    /** 条件从句：{@code stat.condition.presence.<mode>}（entered / left 整句键）。 */
    @Override
    public List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.presence." + mode));
    }
}
