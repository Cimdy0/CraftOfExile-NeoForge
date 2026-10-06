package com.cimdy.coe.data.codec.item.addition;

import com.cimdy.coe.data.codec.item.addition.impl.GlobalAddition;
import com.cimdy.coe.data.codec.item.addition.impl.LocalAddition;
import com.cimdy.coe.data.codec.item.addition.impl.VeiledAddition;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec.Value;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.stat.entry.ValueRange;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.*;

/**
 * 词缀（mod）语义接口 —— <b>非枚举、可无限扩展的种类体系</b>。
 *
 * 本接口把「种类」交给<b>实现类型（多态）</b>：新增一种语义 = 新增一个实现类
 * + 在 {@link AdditionTypes} 登记一行，<b>核心消费者零改动</b>。
 *
 * <ul>
 *   <li>{@link GlobalAddition}（{@code "global"}）——全局：向角色注入全局 stat；</li>
 *   <li>{@link LocalAddition}（{@code "local"}）——局部：经关联符 key 修饰装备自身属性，自带运算口径；</li>
 *   <li>{@link VeiledAddition}（{@code "veiled"}）——隐匿：未揭示前隐藏真实加成、装备不可用；</li>
 * </ul>
 *
 * <h2>消费端唯一入口</h2>
 * {@link #contribute(ContributionSink, double)} —— 消费者（装备属性应用器）只把它自己的 sink 传进来，
 * 由实现类多态写入口，<b>不读取运行时类型</b>。
 */
public interface AdditionType {

    /** 种类分发键（写入 JSON 的 {@code "type"} 字段，供 {@link AdditionTypes} 分派）。 */
    String type();

    /**
     * 消费端唯一入口：把本词缀（给定 roll 值）多态写入 {@code sink}。
     *
     * @param sink   贡献接收端（全局注入 / 局部结算 / 需求层的具体落点由消费者提供）
     * @param rolled 本词缀本条目的 roll 数值
     */
    void contribute(ContributionSink sink, double rolled);

    /**
     * 值<b>槽名</b>（关联符）—— 本词缀消耗几个 roll 值、各叫什么。缺省单槽 {@code "value"}
     * （与 {@link Effect#valueVar()} 的缺省一致）。多槽词缀（如 {@code Adds X to Y Damage} =
     * 两条目 {@code min}/{@code max}）由实现类覆写给出槽名顺序。
     */
    default List<String> valueSlots() {
        List<String> out = new ArrayList<>();
        for (Effect effect : effects()) {
            String slot = effect.valueVar().orElse("value");
            if (!out.contains(slot)) out.add(slot);
        }
        return out.isEmpty() ? List.of("value") : List.copyOf(out);
    }

    /**
     * 取「只含指定值槽」的子类型 —— 多槽词缀按槽拆成单槽实例，使「一个值 ↔ 一条词缀效果」在
     * {@code contribute(sink, rolled)} 中重新成立（缺省返回自身，即单槽无需拆）。
     */
    default AdditionType forSlot(String slot) {
        return this;
    }

    /** tooltip 片段：局部经「关联符 key → 真实属性」再渲染；全局直接走 {@code StatEntry.describe}。 */
    List<Component> describe(double rolled, Locale locale);

    /**
     * tooltip 片段（范围模式，重中之重）：默认委托 {@link #describe(double, Locale)}（零回归）；
     * 全局 / 局部实现覆写为把 roll 区间注入 {@code StatEntry.describe(locale, range)}。
     */
    default List<Component> describe(double rolled, Locale locale, Optional<ValueRange> range) {
        return describe(rolled, locale);
    }

    /**
     * 本词缀引用的全部 {@link Effect}（供领域标签派生 / 校验遍历）。
     * 默认空 —— 非全局语义（局部 / 隐匿载体）可不含 StatEntry 效果。
     */
    default List<Effect> effects() {
        return List.of();
    }

    // ── 分发编解码 ────────────────────────────────────────────────────────

    /** 按 JSON {@code "type"} 分发到 {@link AdditionTypes} 中登记的实现 MapCodec。 */
    MapCodec<AdditionType> MAP_CODEC = Codec.STRING.dispatchMap(
            "type",
            AdditionType::type,
            AdditionTypes::codecFor
    );

    Codec<AdditionType> CODEC = MAP_CODEC.codec();

    // ── 构造便捷（静态工厂）──────────────────────────────────────────────

    /** 全局：引用一个或多个已落盘的 stat 条目（roll 值注入缺省变量 {@code "value"}）。 */
    static GlobalAddition global(Identifier... refs) {
        return GlobalAddition.ofRefs(refs);
    }

    /** 全局：引用型 + 指定注入变量名（双占位词缀拆成两条目时分别用 {@code "min"}/{@code "max"}）。 */
    static GlobalAddition globalVar(Identifier ref, String valueVar) {
        return GlobalAddition.ofVar(ref, valueVar);
    }

    /** 全局：直接包装效果（内联 StatEntry，如任意属性动态加值场景）。 */
    static GlobalAddition global(StatEffect effect) {
        return GlobalAddition.of(Effect.of(effect));
    }

    /** 空词缀常量（占位 / 无效果）；零值入口，供 Addition.from(EMPTY, 0.0) 等使用，保留在 AdditionType。 */
    AdditionType EMPTY = GlobalAddition.EMPTY;

    /**
     * 一条词缀效果：包装一个 {@link StatEntry}，并在实例化（roll）时把数值注入为参数变量。
     *
     * <p>{@code valueVar} 为注入变量名（缺省 {@code "value"}）。双占位词缀（如
     * {@code Adds X to Y Damage}）由生成器拆成两条 {@link AdditionType}（两条目 ≡ 两个数值），
     * 分别把 {@code valueVar} 设为 {@code "min"} / {@code "max"}，从而支持多值注入而不改单值语义。
     */
    record Effect(StatEntry stat, Optional<String> valueVar) {

        public static final MapCodec<AdditionType.Effect> MAP_CODEC =
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                        StatEntry.MAP_CODEC.forGetter(AdditionType.Effect::stat),
                        Codec.STRING.optionalFieldOf("value_var").forGetter(AdditionType.Effect::valueVar)
                ).apply(instance, AdditionType.Effect::new));

        public static final Codec<AdditionType.Effect> CODEC = MAP_CODEC.codec();

        public static AdditionType.Effect read(Identifier path) {
            return new AdditionType.Effect(new StatRead(path), Optional.empty());
        }

        public static AdditionType.Effect read(Identifier path, String valueVar) {
            return new AdditionType.Effect(new StatRead(path), Optional.of(valueVar));
        }

        public static AdditionType.Effect read(Identifier path, Map<String, Value> vars) {
            return new AdditionType.Effect(new StatRead(path, vars), Optional.empty());
        }

        public static AdditionType.Effect of(StatEntry stat) {
            return new AdditionType.Effect(stat, Optional.empty());
        }

        public static AdditionType.Effect of(StatEffect effect) {
            // 内联 Stat 例外路径（自定义效果、无法转为 StatRead 引用）也必须「roll 值直接代入」：
            // amount 用 $value 模板，toStatEntry(rolled) 注入的 params["value"]=rolled 即驱动 amount，
            // 与全局 ref 路径（StatRead(ref,{value:rolled})）语义对齐。
            return new AdditionType.Effect(new StatRead(ExpressionCodec.VALUE, List.of(), StatTargets.EMPTY, effect),
                    Optional.empty());
        }

        public StatEffect effect() {
            return this.stat.effect();
        }

        /** 单值路径（既有行为零回归）：注入到 {@code valueVar}（缺省 {@code "value"}）。 */
        public StatEntry toStatEntry(double rolled) {
            return toStatEntry(rolled, this.valueVar.orElse("value"));
        }

        /** 把 {@code rolled} 注入到指定变量名。 */
        public StatEntry toStatEntry(double rolled, String varName) {
            // 统一经 StatEntry.withParams：StatRead 并入 vars（引用形态），Stat 覆写参数槽（内联形态）。
            // 不再 instanceof 具体形态——两种形态的「注入」语义已在 StatEntry 接口内收敛。
            return this.stat.withParams(Map.of(varName, ExpressionCodec.of(rolled)));
        }
    }
}
