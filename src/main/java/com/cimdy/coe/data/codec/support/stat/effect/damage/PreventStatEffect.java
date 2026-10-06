package com.cimdy.coe.data.codec.support.stat.effect.damage;

import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * 偏转命中减伤（INERT 标记型，不 fire、不进 StatStore 索引，仅由 {@code DamagePipeline#stageBlock} 直接读）：
 * 「防止 X% 偏转命中伤害」（DEFLECT 簇 #32/#33，PoE2 Deflect 部分减伤层）。
 *
 * <p><b>本仓约束</b>：偏转 == 满挡（{@code DamagePipeline#stageBlock} 对偏转命中直接 {@code newDamage = 0}），
 * 无 PoE2 基础 40% 偏转减伤层；故本效果把「满挡」改写为「偏转（部分承伤）」——持有词条且命中被偏转时
 * {@code newDamage = preBlock × (1 − prevent%)}，无词条仍满挡 0。prevent% 由条目 amount 承载
 * （节点 +3%/+6%/+15% 可叠加），消费端 {@code stageBlock} 经 {@link com.cimdy.coe.data.codec.support.stat.engine.StatEngine#scaledMagnitude}
 * 真读 STAT_STORE 条目 amount 折叠。
 *
 * <p><b>门控</b>：{@code deflected=true} 仅对偏转/格挡命中（== {@code stageBlock} 的 {@code s.blocked}）生效；
 * {@code crit=true} 仅对暴击偏转命中生效（#32 Deflected Critical Hit），{@code crit=false} 对所有偏转命中生效
 * （#33 Deflected Hit），二者在暴击偏转命中上叠加。
 */
public final class PreventStatEffect extends AbstractStatEffect {

    private final boolean deflected;
    private final boolean crit;

    public PreventStatEffect(boolean deflected, boolean crit) {
        this(deflected, crit, GateExpr.ALWAYS);
    }

    public PreventStatEffect(boolean deflected, boolean crit, GateExpr tags) {
        super(tags);
        this.deflected = deflected;
        this.crit = crit;
    }

    /** 仅对偏转/格挡命中生效（缺省 true）。 */
    public boolean deflected() {
        return deflected;
    }

    /** 仅对暴击偏转命中生效（#32）；false = 所有偏转命中（#33）。 */
    public boolean crit() {
        return crit;
    }

    public static final MapCodec<PreventStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.BOOL.optionalFieldOf("deflected", true).forGetter(PreventStatEffect::deflected),
            Codec.BOOL.optionalFieldOf("crit", false).forGetter(PreventStatEffect::crit),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, PreventStatEffect::new));

    @Override
    public String type() {
        return "prevent";
    }

    @Override
    public boolean isTrigger() {
        return false;
    }

    @Override
    public EffectKind indexKind() {
        return EffectKind.INERT;
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
        return List.of(Component.translatable("stat_effect.prevent",
                Component.literal(Math.round(entry.amount().getValue() * 100.0) + "%"),
                Component.literal(crit ? "critical hits" : "hits")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PreventStatEffect e)) return false;
        return deflected == e.deflected && crit == e.crit && Objects.equals(tags(), e.tags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(deflected, crit, tags());
    }

    @Override
    public String toString() {
        return "PreventStatEffect{deflected=" + deflected + ", crit=" + crit + ", tags=" + tags() + "}";
    }
}
