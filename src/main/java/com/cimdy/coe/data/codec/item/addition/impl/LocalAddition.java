package com.cimdy.coe.data.codec.item.addition.impl;

import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.addition.ContributionSink;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.stat.entry.ValueRange;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 局部词缀（{@code type = "local"}）—— 重构后与全局等价：直接以 {@link Identifier} 引用一条（或多条）
 * 局部 stat 条目（{@code coe:add_armor} / {@code coe:inc_armor} …），把 roll 值注入该条目的
 * {@code $value}（或指定 {@code vars}）变量后经 {@link ContributionSink#global} 进入角色 StatStore。
 *
 * <h2>单值多 stat 组合（本类核心职责）</h2>
 * 凡「一条词缀 = 一个 roll 值 X 同时驱动多个<b>局部</b> stat」（如 X% 提高 护甲/闪避/能量护盾、
 * X% 降低 属性需求、X% 提高 火焰/冰霜抗性），一律用<b>本类一个实例</b>承载多个局部 stat 引用
 * （共享同一 roll 值，{@code vars} 全为 {@code "value"}）；若需把多个 AdditionType 按独立值 / 跨作用域组合，用 {@link MultiAddition}。
 *
 * <h2>多值槽（独立 roll）</h2>
 * 当一条词缀的多个局部 stat 各自有<b>不同</b>数值区间（如 {@code +X to Armour, +Y to Evasion}），
 * 用 {@link #separate} / {@link #ofMinMax} 让每个 ref 带独立 {@code vars} 槽名，使各自独立 roll，
 * 与全局 {@link GlobalAddition} 的双占位语义对齐。
 *
 * <p>局部与全局的唯一实质区别在本类的 {@code type()} 标签（{@code "local"}）与引用的 stat 条目作用域：
 * 局部属性必须走本类（引用局部 stat 条目），绝不能塞进 {@link GlobalAddition}（其 {@code type()} 为
 * {@code "global"}、引用全局 stat 条目）。消费端（{@code AttributeApplicator}）不感知种类，统一经
 * sink.global 归集，故作用域差异完全由本类 / GlobalAddition 各自引用的 stat 条目承载。
 */
public record LocalAddition(List<Identifier> refs, List<String> vars) implements AdditionType {

    public static final MapCodec<LocalAddition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.listOf().fieldOf("refs").forGetter(LocalAddition::refs),
            Codec.STRING.listOf().optionalFieldOf("vars", List.of()).forGetter(LocalAddition::vars)
    ).apply(instance, (r, v) -> {
        List<String> vars = (v == null || v.isEmpty()) ? shared(r.size()) : v;
        if (vars.size() != r.size()) vars = shared(r.size());
        return new LocalAddition(List.copyOf(r), List.copyOf(vars));
    }));

    private static List<String> shared(int n) {
        List<String> s = new ArrayList<>(n);
        for (int i = 0; i < n; i++) s.add("value");
        return s;
    }

    /** 便捷构造（一个或多个局部 stat 条目，共享同一 roll 值 {@code "value"}）。 */
    public static LocalAddition of(Identifier... refs) {
        List<Identifier> r = List.copyOf(Arrays.asList(refs));
        return new LocalAddition(r, shared(r.size()));
    }

    public static LocalAddition of(Identifier ref) {
        return new LocalAddition(List.of(ref), List.of("value"));
    }

    /** 便捷构造（列表，共享同一 roll 值）。 */
    public static LocalAddition of(List<Identifier> refs) {
        List<Identifier> r = List.copyOf(refs);
        return new LocalAddition(r, shared(r.size()));
    }

    /** 单 ref 单 var（双占位之一，如 {@code "min"} / {@code "max"}）。 */
    public static LocalAddition ofVar(Identifier ref, String var) {
        return new LocalAddition(List.of(ref), List.of(var));
    }

    /** 单 ref 双占位（min / max）：两个效果各占一值槽、各自独立 roll。 */
    public static LocalAddition ofMinMax(Identifier ref) {
        return new LocalAddition(List.of(ref, ref), List.of("min", "max"));
    }

    /** 多 ref 各自独立值槽（{@code value} / {@code value1} / {@code value2} …），用于多 stat 不同区间。 */
    public static LocalAddition separate(Identifier... refs) {
        List<Identifier> r = List.copyOf(Arrays.asList(refs));
        List<String> slots = new ArrayList<>(r.size());
        for (int i = 0; i < r.size(); i++) slots.add(i == 0 ? "value" : "value" + i);
        return new LocalAddition(r, slots);
    }

    @Override
    public String type() {
        return "local";
    }

    @Override
    public void contribute(ContributionSink sink, double rolled) {
        for (int i = 0; i < refs.size(); i++) {
            sink.global(new StatRead(refs.get(i), Map.of(vars.get(i), ExpressionCodec.of(rolled))));
        }
    }

    @Override
    public List<String> valueSlots() {
        List<String> out = new ArrayList<>();
        for (String v : vars) if (!out.contains(v)) out.add(v);
        return out.isEmpty() ? List.of("value") : List.copyOf(out);
    }

    @Override
    public AdditionType forSlot(String slot) {
        List<Identifier> r = new ArrayList<>();
        List<String> v = new ArrayList<>();
        for (int i = 0; i < refs.size(); i++) {
            if (vars.get(i).equals(slot)) {
                r.add(refs.get(i));
                v.add(vars.get(i));
            }
        }
        if (r.isEmpty()) return this;
        return new LocalAddition(List.copyOf(r), List.copyOf(v));
    }

    @Override
    public List<Component> describe(double rolled, Locale locale) {
        return describe(rolled, locale, Optional.empty());
    }

    @Override
    public List<Component> describe(double rolled, Locale locale, Optional<ValueRange> range) {
        List<Component> out = new ArrayList<>();
        for (Identifier ref : refs) {
            StatEntry base = new StatRead(ref).resolve();
            out.addAll(base.withAmount(ExpressionCodec.of(rolled)).describe(locale, range));
        }
        return out;
    }

    @Override
    public List<Effect> effects() {
        List<Effect> out = new ArrayList<>();
        for (int i = 0; i < refs.size(); i++) out.add(Effect.read(refs.get(i), vars.get(i)));
        return out;
    }
}
