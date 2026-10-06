package com.cimdy.coe.data.codec.support.stat.effect.status;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import com.cimdy.coe.data.codec.support.attribute.AttributeLimiters;

/**
 * 命中标志（fold 型，非触发）：声明持有者在命中结算上的<b>绝对短路</b>标志。
 *
 * <p>覆盖 POE 基石 / 词缀的「Your Hits cannot be Evaded」「Cannot be Critically Hit」家族。
 * 运行时由 {@link StatEngine#hasHitFlag} 查询，
 * 在 {@code DamageHelper} 的闪避判定（{@link HitFlag#UNEVADABLE}）、暴击判定
 * （{@link HitFlag#UNCRITTABLE} 受击方）处短路。
 *
 * <p><b>「Never deal Critical Hits」不在此建模</b>：该绝对语义与「把暴击率钉死为 0」
 * 完全等价，已收敛为属性通道——{@code CRIT_CHANCE} 属性 + {@code set_value}（
 * {@link com.cimdy.coe.data.codec.support.attribute.AttributeLimiters.Operation#SET_VALUE}）表达，
 * fold 的 clamp 对 REPLACE 亦生效（绝对压制一切暴击来源）。数据写法：
 * <pre>{@code {"type":"attribute","attribute":"crit_chance","operation":"set_value","amount":0.0}}</pre>
 *
 * <p>数值 / 条件 / 选择器由外层条目持有；本类只携身份（{@link #flag()}）。
 * 数值字段为占位常量（标志只问「有没有」），条件由条目承载（如「对眩晕敌人命中不可闪避」）。
 */
public final class HitFlagStatEffect extends AbstractStatEffect {

    /** 命中结算标志。 */
    public enum HitFlag implements StringRepresentable {
        /** 你的命中不可被闪避（攻击方持有：受击方闪避判定整体短路为命中）。 */
        UNEVADABLE("unevadable"),
        /** 你不可被暴击（受击方持有：攻击方的暴击强制失败）。 */
        UNCRITTABLE("uncrittable");

        public static final Codec<HitFlag> CODEC = StringRepresentable.fromEnum(HitFlag::values);

        private final String name;

        HitFlag(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    private final HitFlag flag;

    public HitFlagStatEffect(HitFlag flag, GateExpr tags) {
        super(tags);
        this.flag = flag;
    }
    /** 无事件标签门控（{@link GateExpr#ALWAYS}）便捷构造。 */
    public HitFlagStatEffect(HitFlag flag) {
        this(flag, GateExpr.ALWAYS);
    }

    /** 命中标志（身份字段）。 */
    public HitFlag flag() {
        return flag;
    }

    /** 便捷构造：命中标志条目（作用于持有者自身，无条件无标签）。 */
    public static StatRead entry(HitFlag flag) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(1.0)),
                new HitFlagStatEffect(flag, GateExpr.ALWAYS));
    }

    /**
     * 便捷构造：带条件的命中标志条目，条件按<b>持有者自身</b>求值（conditionTarget 缺省 SELF）。
     * 覆盖「格挡 / 招架 / 引导等自身姿态期间获得该标志」类语料，
     * 如「Cannot be Critically Hit while Parrying」= {@code UNCRITTABLE + living_state:parrying}。
     */
    public static StatRead entry(HitFlag flag, List<ValueExpr> conditions) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(1.0)), conditions,
                new HitFlagStatEffect(flag, GateExpr.ALWAYS));
    }

    public static final MapCodec<HitFlagStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            HitFlag.CODEC.fieldOf("flag").forGetter(HitFlagStatEffect::flag),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, HitFlagStatEffect::new));

    @Override
    public String type() {
        return "hit_flag";
    }

    /** 命中标志为查询型（非事件触发），由 DamageHelper 判定点读取。 */
    @Override
    public boolean isTrigger() {
        return false;
    }

    @Override
    public EffectKind indexKind() {
        return EffectKind.HIT_FLAG;
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
        return List.of(Component.translatable("stat_effect.hit_flag",
                StatValueKey.of("hit_flag", flag.getSerializedName())));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HitFlagStatEffect e)) return false;
        return flag == e.flag && Objects.equals(tags(), e.tags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(flag, tags());
    }

    @Override
    public String toString() {
        return "HitFlagStatEffect{" + flag.getSerializedName() + ", tags=" + tags() + "}";
    }
}
