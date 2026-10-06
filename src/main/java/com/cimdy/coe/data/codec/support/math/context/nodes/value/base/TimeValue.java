package com.cimdy.coe.data.codec.support.math.context.nodes.value.base;

import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.Level;

import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.Locale;

/**
 * 游戏内时间读值节点（context_dsl_design.md §7，与事件 timer {@code time_} 区分）。
 * {@code time.day} 输出游戏内天数（gameTime / 24000）。无 level 时返回 0。
 */
public record TimeValue(String type) implements ValueExpr {
    public static final Codec<TimeValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("type").forGetter(TimeValue::type)
            ).apply(instance, TimeValue::new));

    @Override
    public double evaluate(ValueScope scope) {
        Level level = scope.root().getResolvedLevel().orElse(null);
        if (level == null) return 0D;
        return switch (type) {
            case "time.day" -> (double) level.getGameTime() / 24000L;
            default -> 0D;
        };
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("time", type.replace("time.", "")));
    }
}
