package com.cimdy.coe.data.codec.support.math.context.nodes.value.base;

import com.cimdy.coe.data.codec.support.math.context.nodes.meta.mode.EventHistoryModes;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EventNode;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventCatalog;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import net.minecraft.network.chat.Component;

/**
 * 事件历史读值节点（合并原 {@code CountValue} + {@code RecentlyValue} + {@code TimerValue.mode=count}）。
 * 读取 {@link Event} 缓存的触发历史（含触发方回传的 int 值），由 {@code mode} 区分聚合方式：
 * <ul>
 *   <li>{@code count}（默认，中缀 {@code count:<name>}）—— 窗口内触发次数；</li>
 *   <li>{@code distinct}（中缀 {@code distinct:<name>[.<secs>]:<tagPrefix>}）—— 窗口内匹配标签的
 *       <b>去重</b>数量（同一对象反复触发只算 1），供「每个不同的 X」族缩放；</li>
 *   <li>{@code sum} / {@code avg} / {@code max} / {@code last} —— 窗口内触发值之和 / 均值 / 最大值 / 最近值；</li>
 *   <li>{@code recently}（中缀 {@code recently:<name>} / {@code recently:<ns>:<tag>}）—— 窗口内是否发生过（1/0），
 *       支持可选 {@code tag} + {@code match}（exact / prefix，如 {@code melee} / {@code proj:} 前缀匹配）；</li>
 *   <li>{@code elapsed} —— 距最近一次触发的 tick 数（未触发返回 {@link Integer#MAX_VALUE}）。</li>
 * </ul>
 * 事件名由 {@link com.cimdy.coe.event.Event#record(LivingEntity, String, int)} 写入方约定
 * （如 {@code consume_charge} / {@code gain_charge} / {@code kill}）。无记录 / 窗口外 → 0。
 */
public record EventHistoryValue(
        String name,
        Optional<String> tag,
        Event.State.Match match,
        String mode,
        double window
) implements EventNode {

    /** 便捷构造（无 tag / 默认 EXACT）。 */
    public EventHistoryValue(String name, String mode, double window) {
        this(name, Optional.empty(), Event.State.Match.EXACT, mode, window);
    }

    // ==================== 常用时间窗辅助方法 ====================
    // 目的：杜绝 `..., 80.0)` 这类裸魔数散落在生成代码里；窗口收敛为具名常量，
    // 后续若要改为「由 Stat 注入」（数据驱动）只需改这一处。

    /** tick / 秒（Minecraft 20 tick/s）。 */
    public static final double TICKS_PER_SECOND = 20.0;

    /** 近期窗口默认长度：PoE2 `RECENTLY` 语义 = 4 秒 = 80 tick。 */
    public static final double RECENTLY_WINDOW_TICKS = 4.0 * TICKS_PER_SECOND;

    /** 秒 → tick。 */
    public static double ticks(double seconds) {
        return seconds * TICKS_PER_SECOND;
    }

    /** 近期是否发生过该事件（默认 4 秒窗口）。 */
    public static EventHistoryValue recently(String name) {
        return new EventHistoryValue(name, EventHistoryModes.RECENTLY, RECENTLY_WINDOW_TICKS);
    }

    /** 近期是否发生过该事件（自定义窗口，单位 tick）。 */
    public static EventHistoryValue recently(String name, double windowTicks) {
        return new EventHistoryValue(name, EventHistoryModes.RECENTLY, windowTicks);
    }

    /** 近期是否发生过带标签的事件（默认 4 秒窗口）。 */
    public static EventHistoryValue recently(String name, String tag) {
        return new EventHistoryValue(name, Optional.of(tag), Event.State.Match.EXACT,
                EventHistoryModes.RECENTLY, RECENTLY_WINDOW_TICKS);
    }

    /** 近期是否「未」发生过该事件（默认 4 秒窗口）。用于「近期未受击」类门控。 */
    public static EventHistoryValue notRecently(String name) {
        return new EventHistoryValue(name, "not_recently", RECENTLY_WINDOW_TICKS);
    }

    /** 窗口内触发次数（默认 4 秒窗口）。 */
    public static EventHistoryValue count(String name) {
        return new EventHistoryValue(name, EventHistoryModes.COUNT, RECENTLY_WINDOW_TICKS);
    }

    /**
     * 窗口内「不同对象」数量（去重计数，PREFIX 匹配）。
     * {@code tagPrefix} 须为身份标签前缀（如 {@code victim_ignite}），实际记录形如
     * {@code victim_ignite:<uuid>}——PREFIX 匹配聚合同族、去重键为完整标签（即对象 UUID）。
     */
    public static EventHistoryValue distinct(String name, String tagPrefix, double windowTicks) {
        return new EventHistoryValue(name, Optional.of(tagPrefix), Event.State.Match.PREFIX,
                EventHistoryModes.DISTINCT, windowTicks);
    }

    public static final Codec<EventHistoryValue> CODEC = RecordCodecBuilder.<EventHistoryValue>create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(EventHistoryValue::name),
            Codec.STRING.optionalFieldOf("tag").forGetter(EventHistoryValue::tag),
            Event.State.Match.CODEC.optionalFieldOf("match", Event.State.Match.EXACT).forGetter(EventHistoryValue::match),
            Codec.STRING.optionalFieldOf("mode", "count").forGetter(EventHistoryValue::mode),
            Codec.DOUBLE.optionalFieldOf("window", 80.0).forGetter(EventHistoryValue::window)
    ).apply(instance, EventHistoryValue::new)).flatXmap(
            (EventHistoryValue v) -> EventCatalog.checkEventKey(v, v.name()),
            (EventHistoryValue v) -> DataResult.success(v));

    @Override
    public double evaluate(Event.State state, ValueScope scope) {
        int w = (int) window;
        return switch (mode) {
            case "sum" -> state.sum(name, w);
            case "avg" -> state.average(name, w);
            case "max" -> state.max(name, w);
            case "last" -> state.lastValue(name);
            case "recently" -> tag.isEmpty()
                    ? (state.recently(name, w) ? 1D : 0D)
                    : (state.recently(name, tag.get(), w, match) ? 1D : 0D);
            case "not_recently" -> !(tag.isEmpty()
                    ? state.recently(name, w)
                    : state.recently(name, tag.get(), w, match)) ? 1D : 0D;
            case "elapsed" -> state.elapsedSinceLast(name);
            // distinct：窗口内匹配标签的去重数量（同一对象反复触发只算 1）。
            // 语义要求标签承载「对象身份」（如 victim_ignite:<uuid>），故必须带 tag；
            // 无 tag 时无身份可去重，退回普通次数计数（与 count 一致，不静默返 0）。
            case EventHistoryModes.DISTINCT -> tag.isEmpty()
                    ? state.count(name, w)
                    : state.countTagged(name, tag.get(), w, match, true);
            // count 带标签时转查 taggedTriggers（分维计数，如「近 N 秒火系命中次数」）；
            default -> tag.isEmpty()
                    ? state.count(name, w)
                    : state.countTagged(name, tag.get(), w, match);
        };
    }

    /**
     * 条件段：{@code stat.condition.event[.not_recently]}，
     * 参数为 {@code stat.value.event.<name>}（值键化：事件名自身带键，两种模板复用同一键，
     * 见架构裁定 #3）；键缺失时回落到 humanize 英文，英文输出逐字不变。
     */
    @Override
    public List<Component> describeFragment() {
        Component name = StatValueKey.of("event", name());
        if ("not_recently".equals(mode)) {
            return List.of(Component.translatable("stat.condition.event.not_recently", name));
        }
        return List.of(Component.translatable("stat.condition.event", name));
    }

    /**
     * 名词短语：事件名自身带键（{@code stat.value.event.<name>}，与条件段共用同一键，见架构裁定 #3），
     * 供 CompareValue 等操作数位置渲染，避免落入 stat.quantity.unknown（[?]）。
     */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("event", name()));
    }
}
