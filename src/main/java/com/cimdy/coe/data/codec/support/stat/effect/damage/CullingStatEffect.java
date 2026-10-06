package com.cimdy.coe.data.codec.support.stat.effect.damage;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.grant.GrantOnEventStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.event.TriggerContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import com.cimdy.coe.data.codec.support.stat.entry.describer.StatNumberFormat;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * 触发亚型：斩杀阈值（Culling Strike）——「命中使目标损失其最大生命的 [X%] 时，立即击杀」。
 * <p>事件标签（{@code caused_hit}）由 {@link GateExpr} 限定，经 {@link GateExpr#matches} / {@link StatEngine#conditionsHold} 求值
 * （与 {@link GrantOnEventStatEffect} 同构触发通道）。
 * <p><b>数值语义</b>：条目 {@code amount} = 斩杀阈值（0~1 比例，如 0.10 = 10% 最大生命）。
 * 命中伤害（{@link TriggerContext#takenPreBlock()}）≥ {@code amount × 目标最大生命} 时，对目标施加致死伤害。
 * <p><b>与属性解耦</b>：阈值数值由 {@code StatEntry.amount} 持有（FIRE 效果自带数值，无需 FOLD 属性），
 * 契合「并非所有 StatEffect 都要使用属性」——斩杀是事件驱动的一次性判定，不应另建 FOLD 属性承载倍率。
 */
public class CullingStatEffect extends AbstractStatEffect {

    public CullingStatEffect(GateExpr tags) {
        super(tags);
    }

    /** 便捷构造：斩杀阈值条目（amount = 阈值比例 0~1，tags 限定触发事件）。 */
    public static StatRead entry(double threshold, Set<String> tags) {
        return StatEntry.of(ExpressionCodec.of(threshold), List.of(), new CullingStatEffect(GateExpr.of(tags)));
    }

    /** 便捷构造：阈值由数据层 {@code $value} 模板注入（与 {@code inc} 同款接线范式）。 */
    public static StatRead entry(Set<String> tags) {
        return StatEntry.of(ExpressionCodec.VALUE, List.of(), new CullingStatEffect(GateExpr.of(tags)));
    }

    public static final MapCodec<CullingStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, CullingStatEffect::new));

    @Override
    public String type() {
        return "culling";
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        double threshold = entry.amount().getValue();
        if (threshold <= 0) return;
        LivingEntity victim = evalCtx.triggerTarget();
        if (victim == null || !victim.isAlive()) return;
        if (tc == null) return;
        // 本次命中对受害者造成的伤害（减免后、招架/格挡前），取自触发通道载体
        double dealt = tc.takenPreBlock();
        if (dealt < threshold * victim.getMaxHealth()) return;
        // 斩杀：对受害者施加致死伤害（触发其正式死亡流程，含掉落/击杀事件）
        victim.hurt(victim.damageSources().generic(), (float) (victim.getHealth() + 1));
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
     * 本地化效果片段：硬编码 {@code "culling >= 10% max life"} → 模板 {@code stat_effect.culling}
     * + 百分比数值（走 {@link StatNumberFormat}，整数截断与旧 describe 逐字一致）。
     */
    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        Component pct = Component.literal(
                StatNumberFormat.format((int) (entry.amount().getValue() * 100), locale));
        return List.of(Component.translatable("stat_effect.culling", pct));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CullingStatEffect that)) return false;
        return Objects.equals(tags(), that.tags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(CullingStatEffect.class, tags());
    }

    @Override
    public String toString() {
        return "CullingStatEffect[tags=" + tags() + "]";
    }
}
