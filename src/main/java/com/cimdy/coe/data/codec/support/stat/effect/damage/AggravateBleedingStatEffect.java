package com.cimdy.coe.data.codec.support.stat.effect.damage;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.status.runtime.StatusInstance;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.event.TriggerContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * 加重流血（Aggravate Bleeding）：事件触发时把目标身上的流血实例标记为「加重」——
 * 在其 DoT 结算（{@link com.cimdy.coe.data.codec.support.status.runtime.StatusBehavior#dealSplitDoT}）时
 * 按 (1 + aggravatedFraction) 放大，而非原独立 AGGRAVATED 状态（后者可脱离异常独立存在，
 * 违反 POE2「加重是异常的修饰态」语义，见 PLAN.md S1）。
 *
 * <p>触发概率 = 条目 amount（如「X% 几率加剧流血」）；加重幅度固定 +100%
 * （POE2 aggravated bleeding/ignite/poison 均 = 原伤害 ×2）。仅作用于<b>流血</b>实例——
 * 当前 9 条接线均为「加剧流血」语义（poison/ignite 加剧留作后续扩展）。
 */
public final class AggravateBleedingStatEffect extends AbstractStatEffect {

    /** 加重幅度（流血 DoT ×(1+aggravatedFraction)，默认 1.0 = +100%）。 */
    private final double fraction;

    public AggravateBleedingStatEffect(GateExpr tags) {
        this(tags, 1.0);
    }

    public AggravateBleedingStatEffect(GateExpr tags, double fraction) {
        super(tags);
        this.fraction = fraction;
    }

    public static final MapCodec<AggravateBleedingStatEffect> CODEC =
            RecordCodecBuilder.mapCodec(i -> i.group(
                    AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags),
                    Codec.DOUBLE.optionalFieldOf("fraction", 1.0).forGetter(e -> e.fraction)
            ).apply(i, AggravateBleedingStatEffect::new));

    @Override
    public String type() {
        return "aggravate_bleeding";
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        double chance = entry.amount().getValue();
        if (chance <= 0) return;
        if (evalCtx.owner().getRandom().nextDouble() >= chance) return;
        LivingEntity target = evalCtx.triggerTarget();
        if (target == null) return;
        StatusInstance inst = StatusEngine.query(target, StatusDefs.BLEED);
        if (inst == null) return;
        inst.aggravatedFraction = fraction;
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
        return List.of(Component.translatable("stat_effect.aggravate_bleeding",
                Component.literal(String.valueOf(entry.amount()))));
    }
}
