package com.cimdy.coe.data.codec.support.math.context.node;

import com.cimdy.coe.data.codec.support.math.context.ValueLib;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Locale;

/**
 * 命名值引用节点（值模型统一内核 · 随意引用）：以 {@code ref:<id>} 引用 {@link ValueLib} 中注册的命名值。
 * <p>
 * 命名值由数据包经数据生成写入缓存、运行期读回（键为 {@link Identifier}）；本节点在其作用域上
 * 委托求值，使一段复杂计算（如「被你点燃过的敌人递增承火伤幅度」「击杀爆炸伤害」）一处定义、处处复用。
 * 键缺失（命名值未注册）时回落 0，不产生「恒真 / 崩溃」。
 */
public record RefExpr(Identifier key) implements ValueExpr {

    public static final Codec<RefExpr> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Identifier.CODEC.fieldOf("key").forGetter(RefExpr::key))
                    .apply(instance, RefExpr::new));

    @Override
    public double evaluate(ValueScope scope) {
        ValueExpr e = ValueLib.get(key);
        return e != null ? e.evaluate(scope) : 0D;
    }

    @Override
    public List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.ref", key.toString()));
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(Component.translatable("stat.value.ref", key.toString()));
    }
}
