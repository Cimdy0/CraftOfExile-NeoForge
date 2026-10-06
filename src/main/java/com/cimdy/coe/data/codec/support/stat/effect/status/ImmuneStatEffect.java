package com.cimdy.coe.data.codec.support.stat.effect.status;

import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 状态免疫（fold 型，非触发）：声明持有者对一组状态（{@link StatusType}）免疫。
 *
 * <p>覆盖 POE 基石 / 词缀的「Immune to X」「Unaffected by X」「Cannot be X」家族
 * （如 Immune to Poison / Cannot be Blinded / Unaffected by Chill / Cannot be Light Stunned）。
 * 运行时由 {@link StatusEngine#apply} 施加入口查询 {@link StatEngine#isImmune}，命中即跳过施加。
 *
 * <p>作用目标由外层条目 {@link StatEntry} 的三段选择器决定：缺省作用于持有者自身（SELF 免疫）；
 * 携带 {@code trigger_target} 选择器时即「对指定目标免疫」（如「被你诅咒的敌人不能回复能量护盾」
 * 经事件触发把免疫条目施加到敌人身上，此后该敌人对 {@code energy_recharge} 类状态免疫）。
 *
 * <p>数值 / 条件 / 选择器由外层条目持有；本类只携身份（{@link #statuses()}）。
 * 数值字段为占位常量（免疫只问「有没有」），条件由条目承载（如「满血时免疫」）。
 *
 * <p><b>命名与匹配</b>：免疫按状态 <b>id 字符串</b>匹配（{@link #statusIds()} +
 * {@link StatEngine#isImmune} 按 {@code type.id()} 判定），<b>不要求被引用状态已注册</b>。
 * 故地面状态（{@code ON_*_GROUND}）等「不静态注册、仅经 {@link StatusType#dynamic} 在施放/应用时
 * 动态注册」的状态族，也可被免疫 EFF 按 id 直接引用，无需在此预注册（遵循用户裁定：地面状态可附加、无需注册）。
 */
public final class ImmuneStatEffect extends AbstractStatEffect {

    private final Set<StatusType> statuses;

    public ImmuneStatEffect(Set<StatusType> statuses, GateExpr tags) {
        super(tags);
        this.statuses = statuses.isEmpty() ? Set.of() : Set.copyOf(statuses);
    }
    /** 无事件标签门控（{@link GateExpr#ALWAYS}）便捷构造。 */
    public ImmuneStatEffect(Set<StatusType> statuses) {
        this(statuses, GateExpr.ALWAYS);
    }

    /** 免疫的状态集合（身份字段；运行时以 {@code type.id} 匹配）。 */
    public Set<StatusType> statuses() {
        return statuses;
    }

    /** 免疫的状态 id 集合（用于运行时按 id 身份匹配，免注册；与 {@link StatEngine#isImmune} 同口径）。 */
    public Set<String> statusIds() {
        return statuses.stream().map(s -> s.id).collect(Collectors.toSet());
    }

    /** 便捷构造：单状态免疫条目（作用于持有者自身，无条件无标签）。 */
    public static StatEntry entry(StatusType status) {
        return entry(Set.of(status), Set.of());
    }

    /** 便捷构造：多状态免疫条目（作用于持有者自身）。 */
    public static StatEntry entry(Set<StatusType> statuses, Set<String> tags) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(1.0)),
                new ImmuneStatEffect(statuses, GateExpr.of(tags)));
    }

    /** 便捷构造：单状态免疫条目（带条件门控，如满血 / 变形 / 受 Archon 影响时）。 */
    public static StatEntry entry(StatusType status, List<ValueExpr> conditions) {
        return entry(Set.of(status), Set.of(), conditions);
    }

    /**
     * 便捷构造：多状态免疫条目（带条件门控）。条件全部非 0 时才对持有者生效（读者自判自身状态，机制完全可达）。
     * 用于「变形时免疫」「受 Archon 影响时免疫」「装备头盔护甲 &gt; 闪避时免疫」等忠实门控。
     */
    public static StatEntry entry(Set<StatusType> statuses, Set<String> tags, List<ValueExpr> conditions) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(1.0)), conditions,
                new ImmuneStatEffect(statuses, GateExpr.of(tags)));
    }

    /**
     * 状态编解码（严格）：仅接受已注册状态 id（{@link com.cimdy.coe.data.codec.support.status.def.StatusDefs}
     * 静态声明、或运行时 {@link StatusType#dynamic} 注册的诅咒/标记类 id），按 {@code type.id} 参与
     * 免疫身份匹配（与 {@link StatEngine#isImmune} 同口径）。未知 id 直接报错，不再构造未注册占位——
     * 地面状态（{@code on_*_ground}）统一在 {@link com.cimdy.coe.data.codec.support.status.def.StatusDefs}
     * 静态注册，可按 id 直接解析。
     */
    private static final Codec<StatusType> STATUS_CODEC = Codec.STRING.comapFlatMap(
            id -> StatusType.byId(id)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(
                            () -> "ImmuneStatEffect: unknown status id '" + id + "'")),
            t -> t.id);

    public static final MapCodec<ImmuneStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            STATUS_CODEC.listOf().xmap(Set::copyOf, List::copyOf)
                    .fieldOf("statuses").forGetter(ImmuneStatEffect::statuses),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, ImmuneStatEffect::new));

    @Override
    public String type() {
        return "immune";
    }

    /** 免疫为查询型（非事件触发），由 StatusEngine.apply 入口读取判定。 */
    @Override
    public boolean isTrigger() {
        return false;
    }

    @Override
    public EffectKind indexKind() {
        return EffectKind.IMMUNE;
    }

    @Override
    public List<Component> describe(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    @Override
    public List<Component> describeFragment(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        List<Component> out = new java.util.ArrayList<>();
        for (StatusType s : statuses) {
            out.add(Component.translatable("stat_effect.immune", StatValueKey.of("status", s.id)));
        }
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmuneStatEffect e)) return false;
        return Objects.equals(statusIds(), e.statusIds()) && Objects.equals(tags(), e.tags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusIds(), tags());
    }

    @Override
    public String toString() {
        return "ImmuneStatEffect{statuses=" + statuses + ", tags=" + tags() + "}";
    }
}
