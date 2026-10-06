package com.cimdy.coe.data.codec.support.stat.entry.describer;

import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.ValueRange;

import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import net.minecraft.network.chat.Component;

import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Stat 文本装配器（P1 原型，方案 A 的窄切片）——所有 Stat 的 UI 文本唯一出口。
 *
 * <p>本类是所有 Stat 的 UI 文本唯一出口，不直接交给效果 describe，而是：
 * <ol>
 *   <li>取<b>效果片段</b>（{@link StatEffect#describeFragment}）——只含「做什么」，不含数值/目标/条件；</li>
 *   <li>取<b>目标片段</b>（{@link StatTargets#describeFragment}）——三段选择器融合短语，缺省/SELF 时空；</li>
 *   <li>追加<b>条件片段</b>（{@code entry.conditions()}）与<b>作用域片段</b>
 *       （{@link StatScopeDescriber}——外溢 / 转换两个条目层字段）；</li>
 *   <li>P1 直接英文拼接成<b>一行</b> {@code [effectFragment][ + targetFragment][ + condition][ + scope]}。</li>
 * </ol>
 *
 * <p>设计铁律（见 {@code research/stat_translation_plan.md} §2/§3）：
 * <ul>
 *   <li>四段访问器全部走 {@link StatEntry} 接口，inline / read 两形态输出一致（引用解析在访问器内部完成）；</li>
 *   <li>P1 暂不构建 per-locale 外层模板 {@code stat.fmt.<mask>}（那是 P4），用直连英文拼接保证零回归；</li>
 *   <li>仅把 {@code ModifierStatEffect} / {@code DamageStatEffect} 与 14 个 {@code Source} 名改为数据驱动键，
 *       其余 26 个效果子类的 {@code describeFragment} 默认委托旧 {@code describe}，逐字不变。</li>
 * </ul>
 */
public final class StatDescriber {

    private StatDescriber() {
    }

    /**
     * 装配一条 Stat 的 tooltip 文本（P1：效果片段 + 目标片段，合成一行）。
     *
     * @return 行列表（典型为 1 行；目标片段非空时拼接到效果片段末行之后，用空格连接）。
     */
    public static List<Component> assemble(StatEntry entry) {
        return assemble(entry, Locale.ROOT);
    }

    /**
     * 装配一条 Stat 的 tooltip 文本（P1：效果片段 + 目标片段 + 条件片段，合成一行）。
     *
     * @param locale 数字本地化 locale（P3）：经效果片段下传，用于千分位 / 符号本地化。
     * @return 行列表（典型为 1 行；目标片段非空时拼接到效果片段末行之后，用空格连接）。
     */
    public static List<Component> assemble(StatEntry entry, Locale locale) {
        return assemble(entry, locale, Optional.empty());
    }

    /**
     * 装配（本地化 + 范围值，重中之重）：效果片段经
     * {@link StatEffect#describeFragment(StatEntry, Locale, Optional)} 下传区间，
     * 目标 / 条件片段不随范围变化。
     */
    public static List<Component> assemble(StatEntry entry, Locale locale, Optional<ValueRange> range) {
        List<Component> effectLines = entry.effect().describeFragment(entry, locale, range);
        List<Component> targetLines = entry.targets().describeFragment();
        Component targetFrag = targetLines.isEmpty() ? null : targetLines.get(targetLines.size() - 1);

        List<Component> out = new ArrayList<>(effectLines);
        if (targetFrag != null) {
            if (out.isEmpty()) {
                out.add(targetFrag);
            } else {
                int last = out.size() - 1;
                out.set(last, out.get(last).copy()
                        .append(Component.literal(" "))
                        .append(targetFrag));
            }
        }
        // P2：条件（何时）片段 —— 收集每条 condition 的 describeFragment，丢弃空，以 " and " 连接，追加到末行（前导空格）。
        // 未覆盖节点类型经默认实现回退到 stat.condition.unknown（"[!] condition"），保证不静默丢弃。
        List<Component> condFrags = new ArrayList<>();
        for (ValueExpr c : entry.conditions()) {
            if (c == null) continue;
            List<Component> f = c.describeFragment();
            if (f != null) condFrags.addAll(f);
        }
        if (!condFrags.isEmpty()) {
            Component clause = condFrags.get(0);
            for (int i = 1; i < condFrags.size(); i++) {
                clause = clause.copy()
                        .append(Component.translatable("stat.condition.join_and"))
                        .append(condFrags.get(i));
            }
            if (out.isEmpty()) {
                out.add(clause);
            } else {
                int last = out.size() - 1;
                out.set(last, out.get(last).copy()
                        .append(Component.literal(" "))
                        .append(clause));
            }
        }

        // 作用域从句（外溢 / 转换）——放最后：它限定的是整条数值计入哪些作用域，
        // 须在效果 / 目标 / 条件全部就位后才能作为一句完整的话挂在行尾。
        if (!out.isEmpty()) {
            int last = out.size() - 1;
            Component scoped = StatScopeDescriber.wrap(out.get(last), entry.spillTo(), entry.convertTo());
            if (scoped != out.get(last)) {
                out.set(last, scoped);
            }
        }

        return out;
    }
}
