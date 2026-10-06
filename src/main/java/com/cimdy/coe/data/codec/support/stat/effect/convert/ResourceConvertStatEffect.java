package com.cimdy.coe.data.codec.support.stat.effect.convert;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.resource.ResourceStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;

import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 资源转换（fold 型，非触发）：声明「从某资源扣除的量，改从另一资源扣除」。
 * 身份字段 {@code from}/{@code to} 指定资源对；<b>转换比例（0~1）是条目唯一数值</b>，
 * 本类不持有任何数值——需要「部分转换」（如心智之上有物 30%）就把比例写进 amount，
 * 需要「全额转换」（如血魔法 100%）就写 1.0。
 * <p>
 * 与 {@link ResourceStatEffect}（触发型「资源恢复」）对称：后者在事件 fire 时回资源，
 * 本类在资源扣费点经 {@link com.cimdy.coe.data.codec.support.stat.engine.StatEngine#convert} 折叠查询，
 * 由扣费方（{@code ResourceCost} 等）按查询结果改从 {@code to} 资源扣除。
 * <p>
 * 纯读取、无副作用，因此可同时服务于「能否承受」({@code matches}) 与「实际扣除」({@code runCost})，
 * 两者都查同一转换规则，结果天然一致，不会出现重复扣费。
 * <p>
 * <h2>两种模式（{@link #mode()}）</h2>
 * <ul>
 *   <li>{@code REPLACE}（去A取B）：原本扣 {@code from} 的量，改为（部分/全额）从 {@code to} 扣除，
 *       {@code from} 不再被扣。例：血魔法（mana 消耗转生命）。</li>
 *   <li>{@code GAIN}（共存 / 也获得B）：{@code from} 照常扣，额外再以 {@code to} 扣除 ratio 份额。
 *       例：护盾同时消耗魔力（能量护盾被击中时，一部分伤害也消耗魔力）。</li>
 *   <li>{@code EXCESS}（溢出转移）：{@code from} 照常回复，<b>仅把满溢出的部分</b>按 ratio 份额转给
 *       {@code to}。例：生命再生的溢出部分转给能量护盾。与 GAIN 的区别在于<b>不预先分流</b>——
 *       先喂饱 {@code from}，只有装不下的量才溢出，故未满时 {@code to} 分毫不得。</li>
 * </ul>
 * <p>
 * 例（血魔法：技能 mana 消耗转生命）：
 * {@code {"type":"resource_convert","from":"mana","to":"health","mode":"replace","amount":1.0}}。
 */
public class ResourceConvertStatEffect extends AbstractStatEffect {

    private final ResourceType from;
    private final ResourceType to;
    private final Mode mode;
    /** 来源过滤：仅当资源流动的 sourceTag 命中其中之一才生效（前缀包含匹配，如 {@code "leech"} 匹配 {@code "leech:fireball"}）；空集=不限来源。 */
    private final Set<String> sourceTags;

    /** 规范构造：身份（源资源 + 目标资源）+ 语义（模式）+ 作用域标签（布尔表达式）+ 来源过滤。 */
    public ResourceConvertStatEffect(ResourceType from, ResourceType to, Mode mode, GateExpr tags, Set<String> sourceTags) {
        super(tags);
        this.from = from;
        this.to = to;
        this.mode = mode;
        this.sourceTags = sourceTags;
    }
    /** 无事件标签门控（{@link GateExpr#ALWAYS}）便捷构造。 */
    public ResourceConvertStatEffect(ResourceType from, ResourceType to, Mode mode, Set<String> sourceTags) {
        this(from, to, mode, GateExpr.ALWAYS, sourceTags);
    }

    /** 来源过滤集合（只读）。 */
    public Set<String> sourceTags() {
        return sourceTags;
    }

    /** 判断给定来源标签是否命中本规则的来源过滤（空集放行；否则要求 sourceTag 以某个 sourceTag 为前缀）。 */
    public boolean matchesSource(@Nullable String sourceTag) {
        if (sourceTags.isEmpty()) return true;
        if (sourceTag == null) return false;
        for (String s : sourceTags) {
            if (sourceTag.equals(s) || sourceTag.startsWith(s + ":")) return true;
        }
        return false;
    }

    public ResourceType from() {
        return from;
    }

    public ResourceType to() {
        return to;
    }

    public Mode mode() {
        return mode;
    }

    /** 转换模式：REPLACE 去A取B，GAIN 共存也获得B，EXCESS 仅转溢出部分。 */
    public enum Mode implements StringRepresentable {
        REPLACE,
        GAIN,
        EXCESS;

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /** 便捷构造：常量转换比例（REPLACE 模式），产出一条完整条目（作用于自身，无标签无条件不限来源）。 */
    public static StatRead entry(ResourceType from, ResourceType to, double ratio) {
        return entry(from, to, Mode.REPLACE, ratio);
    }

    /** 便捷构造：常量转换比例 + 模式，产出一条无标签无条件的条目。 */
    public static StatRead entry(ResourceType from, ResourceType to, Mode mode, double ratio) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(ratio)),
                new ResourceConvertStatEffect(from, to, mode, GateExpr.ALWAYS, Set.of()));
    }

    /** 便捷构造：常量转换比例 + 模式 + 来源过滤，产出一条无标签条目。 */
    public static StatRead entry(ResourceType from, ResourceType to, Mode mode, double ratio, Set<String> sourceTags) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(ratio)),
                new ResourceConvertStatEffect(from, to, mode, GateExpr.ALWAYS, sourceTags));
    }

    /** 便捷构造：amount 默认 $value 模板（转换比例由词缀 vars 注入），REPLACE 模式，无标签无条件不限来源。 */
    public static StatRead entry(ResourceType from, ResourceType to) {
        return StatEntry.of(ExpressionCodec.VALUE,
                new ResourceConvertStatEffect(from, to, Mode.REPLACE, GateExpr.ALWAYS, Set.of()));
    }

    /** 便捷构造：常量转换比例 + 条件（如依赖某 keystone 属性激活）。 */
    public static StatRead entry(ResourceType from, ResourceType to, double ratio,
                                  List<ValueExpr> conditions) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(ratio)), conditions,
                new ResourceConvertStatEffect(from, to, Mode.REPLACE, GateExpr.ALWAYS, Set.of()));
    }

    public static final MapCodec<ResourceConvertStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceType.CODEC.fieldOf("from").forGetter(ResourceConvertStatEffect::from),
            ResourceType.CODEC.fieldOf("to").forGetter(ResourceConvertStatEffect::to),
            Mode.CODEC.fieldOf("mode").forGetter(ResourceConvertStatEffect::mode),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags),
            Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).optionalFieldOf("source_tags", Set.of())
                    .forGetter(ResourceConvertStatEffect::sourceTags)
    ).apply(i, (from, to, mode, tags, sourceTags) -> new ResourceConvertStatEffect(from, to, mode, tags, sourceTags)));

    @Override
    public String type() {
        return "resource_convert";
    }

    /** 资源转换是 fold 型（扣费点查询），非触发型。 */
    @Override
    public boolean isTrigger() {
        return false;
    }

    @Override
    public EffectKind indexKind() {
        return EffectKind.RESOURCE_CONVERT;
    }

    @Override
    public List<Component> describe(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    @Override
    public List<Component> describeFragment(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    /**
     * 本地化效果片段：硬编码 {@code "X Health → Y Mana (only from physical)"} → 模板
     * {@code stat_effect.resource_convert} / {@code stat_effect.resource_convert_gain}
     * + 来源/目标资源组件（{@code ResourceType#getComponent} 已用 {@code stat.<resource>} 自带译文）
     * + 可选 {@code stat_effect.resource_convert_src} + 来源过滤值键 {@code stat.value.damage.<tag>}。
     */
    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        double v = entry.amount().getValue();
        MutableComponent base = Component.translatable(
                mode == Mode.GAIN ? "stat_effect.resource_convert_gain" : "stat_effect.resource_convert",
                from.getComponent(v), to.getComponent(v));
        if (!sourceTags.isEmpty()) {
            MutableComponent src = Component.empty();
            List<String> tags = List.copyOf(sourceTags);
            for (int i = 0; i < tags.size(); i++) {
                if (i > 0) src.append("/");
                src.append(StatValueKey.of("damage", tags.get(i)));
            }
            base = Component.translatable("stat_effect.resource_convert_src", base, src);
        }
        return List.of(base);
    }

    @Override
    public String toString() {
        return "ResourceConvertStatEffect[" + from + (mode == Mode.GAIN ? "+" : "→") + to
                + ", tags=" + tags() + (sourceTags.isEmpty() ? "" : ", sourceTags=" + sourceTags) + "]";
    }
}
