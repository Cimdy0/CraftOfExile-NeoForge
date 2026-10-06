package com.cimdy.coe.data.codec.item.addition.impl;

import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.addition.ContributionSink;

import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 隐匿词缀（{@code type = "veiled"}）—— 只露前后缀（词缀位置）、隐藏真实加成。
 *
 * <p>语义：
 * <ul>
 *   <li><b>已揭示</b>（{@link ContributionSink#revealed()} == {@code true}）：把隐藏载荷
 *       {@link #hidden()} 正常写入 sink（真实加成生效）；</li>
 *   <li><b>未揭示</b>：不写入任何加成，改为 {@link ContributionSink#markVeiled()} 标记
 *       「本装备存在未揭示隐匿词缀」→ 装备视为不可穿戴 / 不可用。</li>
 * </ul>
 *
 * <p>载体 {@code hidden} 可为任意 {@link AdditionType}（全局 / 局部 / 组合），
 * 故隐匿层与其它种类正交、可无限嵌套。
 */
public record VeiledAddition(AdditionType hidden) implements AdditionType {

    public static final MapCodec<VeiledAddition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            AdditionType.MAP_CODEC.forGetter(VeiledAddition::hidden)
    ).apply(instance, VeiledAddition::new));

    @Override
    public String type() {
        return "veiled";
    }

    @Override
    public void contribute(ContributionSink sink, double rolled) {
        if (sink.revealed()) {
            hidden.contribute(sink, rolled);
        } else {
            sink.markVeiled();
        }
    }

    @Override
    public List<Component> describe(double rolled, Locale locale) {
        // 揭示前后由 sink 上下文决定；此处以「是否含可渲染载荷」启发式给出片段。
        List<Component> revealed = hidden.describe(rolled, locale);
        if (revealed.isEmpty()) {
            return List.of(Component.translatable("item.mod.coe.veiled"));
        }
        return revealed;
    }

    @Override
    public List<Effect> effects() {
        return hidden.effects();
    }
}
