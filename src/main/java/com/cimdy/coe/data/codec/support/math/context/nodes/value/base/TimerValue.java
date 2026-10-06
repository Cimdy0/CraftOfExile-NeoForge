package com.cimdy.coe.data.codec.support.math.context.nodes.value.base;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EventNode;
import com.cimdy.coe.event.Event;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.Locale;

/**
 * 计时读值节点（context_dsl_design.md §5 / §7）：自事件发生起经过的 tick（递增），原始 double；
 * math 判 {@code X <= 80} 即"4 秒内"。未触发 / 已超存活上限 → 返回 {@code def}
 * （递增计时变量须声明大 default 防误判，如 999999）。数据来自 {@code ExpressionContext} timer 缓存。
 * <p>
 * 按次计时（窗口内触发次数 / 聚合）已并入 {@link EventHistoryValue}（mode=count / sum / recently 等）。
 */
public record TimerValue(String name, double def) implements EventNode {
    public static final Codec<TimerValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(TimerValue::name),
                    Codec.DOUBLE.optionalFieldOf("def", 0.0).forGetter(TimerValue::def)
            ).apply(instance, TimerValue::new));

    @Override
    public double evaluate(Event.State state, ValueScope scope) {
        // 默认 time 模式：自事件发生起经过的 tick（递增；timer 存储同样按候选归一解析）
        return scope.timerElapsed(name, def);
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("timer", name));
    }
}
