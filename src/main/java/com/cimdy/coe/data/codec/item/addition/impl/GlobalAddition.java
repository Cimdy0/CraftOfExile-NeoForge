package com.cimdy.coe.data.codec.item.addition.impl;

import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.addition.ContributionSink;

import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.ValueRange;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 全局词缀（{@code type = "global"}）—— 把 roll 值注入到引用的 {@link StatEntry}，
 * 直接经 {@link ContributionSink#global} 进入角色 {@code StatStore}（由 {@code StatEngine.fold} 聚合）。
 *
 * <p>支持一条词缀引用<b>多个</b> stat（如 {@code #% increased Fire/Cold/Lightning Damage} 三条目共享一个 roll 值）。
 */
public record GlobalAddition(List<AdditionType.Effect> effects) implements AdditionType {

    public static final GlobalAddition EMPTY = new GlobalAddition(List.of());

    public static final MapCodec<GlobalAddition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            AdditionType.Effect.CODEC.listOf().fieldOf("stats").forGetter(GlobalAddition::effects)
    ).apply(instance, GlobalAddition::new));

    public static GlobalAddition ofRefs(Identifier... refs) {
        List<AdditionType.Effect> list = new ArrayList<>(refs.length);
        for (Identifier ref : refs) {
            list.add(AdditionType.Effect.read(ref));
        }
        return new GlobalAddition(list);
    }

    public static GlobalAddition ofVar(Identifier ref, String valueVar) {
        return new GlobalAddition(List.of(AdditionType.Effect.read(ref, valueVar)));
    }

    public static GlobalAddition of(AdditionType.Effect... effects) {
        return new GlobalAddition(List.copyOf(Arrays.asList(effects)));
    }

    public static GlobalAddition of(Optional<AdditionType.Effect> effect) {
        return effect.map(e -> new GlobalAddition(List.of(e))).orElse(EMPTY);
    }

    @Override
    public String type() {
        return "global";
    }

    @Override
    public void contribute(ContributionSink sink, double rolled) {
        for (AdditionType.Effect effect : effects) {
            sink.global(effect.toStatEntry(rolled));
        }
    }

    @Override
    public List<Component> describe(double rolled, Locale locale) {
        return describe(rolled, locale, Optional.empty());
    }

    @Override
    public List<Component> describe(double rolled, Locale locale, Optional<ValueRange> range) {
        List<Component> out = new ArrayList<>();
        for (AdditionType.Effect effect : effects) {
            out.addAll(effect.toStatEntry(rolled).describe(locale, range));
        }
        return out;
    }

    /** 领域标签派生 / 校验用：本词缀引用的全部效果。 */
    @Override
    public List<AdditionType.Effect> effects() {
        return effects;
    }

    /** 多槽拆分：只保留 {@code valueVar == slot} 的效果，使其独占一个 roll 值。 */
    @Override
    public AdditionType forSlot(String slot) {
        List<AdditionType.Effect> matched = new ArrayList<>();
        for (AdditionType.Effect effect : effects) {
            if (effect.valueVar().orElse("value").equals(slot)) matched.add(effect);
        }
        if (matched.isEmpty() || matched.size() == effects.size()) return this;
        return new GlobalAddition(matched);
    }
}
