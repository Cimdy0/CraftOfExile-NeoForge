package com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.CompareValue;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EventNode;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.event.Event;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * 移动状态读值节点。基于 {@link Event} 维护的一段时间内坐标历史，判定站定 / 移动 / 移动距离 / 速度。
 * <ul>
 *   <li>{@code mode=stationary} → 窗口内几乎未移动返回 1，否则 0</li>
 *   <li>{@code mode=moving} → 窗口内发生移动返回 1，否则 0</li>
 *   <li>{@code mode=distance} → 窗口内累计移动距离（方块）</li>
 *   <li>{@code mode=speed} → 移动速度（方块/秒）</li>
 * </ul>
 * {@code window} 为采样窗口 tick（默认 20，约 1 秒）。无实体 / 无坐标历史时返回 0。
 */
public record MovementValue(String mode, double window) implements EventNode {
    public static final Codec<MovementValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("mode").forGetter(MovementValue::mode),
            Codec.DOUBLE.optionalFieldOf("window", 20.0).forGetter(MovementValue::window)
    ).apply(instance, MovementValue::new));

    @Override
    public double evaluate(Event.State state, ValueScope scope) {
        int w = (int) window;
        return switch (mode) {
            case "stationary" -> state.isStationary(w) ? 1D : 0D;
            case "moving" -> state.isMoving(w) ? 1D : 0D;
            case "distance" -> state.movementDistance(w);
            case "speed" -> state.movementDistance(w) / Math.max(1, w) * 20D;
            default -> 0D;
        };
    }

    @Override
    public List<Component> describeFragment() {
        return switch (mode) {
            case "stationary" -> List.of(Component.translatable("stat.condition.movement.stationary"));
            case "moving" -> List.of(Component.translatable("stat.condition.movement.moving"));
            default -> List.of(Component.translatable("stat.condition.movement",
                    StatValueKey.of("movement", mode)));
        };
    }

    /**
     * 名词短语：{@code stat.value.movement.<mode>}（distance/speed 等数值模式），
     * 供 CompareValue 等操作数位置渲染。stationary/moving 为布尔条件，不会作为操作数名词短语。
     */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("movement", mode));
    }
}
