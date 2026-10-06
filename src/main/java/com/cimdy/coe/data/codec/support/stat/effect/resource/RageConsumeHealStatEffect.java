package com.cimdy.coe.data.codec.support.stat.effect.resource;

import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.data.helper.status.EffectApplier;

import com.cimdy.coe.event.TriggerContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 触发亚型：变回人形（FORM_CLEARED 事件）时消耗全部怒气，并按「每点怒气恢复 X% 最大生命」回复生命。
 *
 * <p>复用 {@link com.cimdy.coe.data.codec.support.status.runtime.StatusEngine} 在形态清除/到期时真实发射的
 * {@code form_cleared} 事件（owner=玩家，与 ARCANE_SURGE 同通道），条目 amount = X（%），
 * 经 {@code WiringDsl.self(EFF)} 以 {@code $value} 透传。
 *
 * <p>读点全部真实（{@link ResourceRegistry#getRage} / {@link ResourceRegistry#spendRage} / {@link LivingEntity#getMaxHealth}），
 * 消耗额 = 当前怒气、回复额 = 消耗怒气 × (X/100) × 最大生命，无硬编码基线、无死线（R3 安全）。
 * 玩家已死亡时静默跳过（entity 死亡时清形态也会发射本事件，此时回复无意义）。
 */
public final class RageConsumeHealStatEffect extends AbstractStatEffect {

    public RageConsumeHealStatEffect(GateExpr tags) {
        super(tags);
    }

    public static final MapCodec<RageConsumeHealStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, RageConsumeHealStatEffect::new));

    @Override
    public String type() {
        return "rage_consume_heal";
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        LivingEntity owner = evalCtx.owner();
        if (owner == null || owner.isDeadOrDying()) return;
        double pct = entry.amount().getValue();   // X（%）
        if (pct <= 0) return;
        float rage = ResourceRegistry.getRage(owner);
        if (rage <= 0f) return;
        // 消耗全部怒气（交给统一资源枢纽，刷新固有流失宽限 + RESOURCE_SPEND 事件，与常规获怒同生命周期）
        ResourceRegistry.spendRage(owner, rage);
        // 每点怒气恢复 X% 最大生命
        float heal = (float) (rage * (pct / 100.0) * owner.getMaxHealth());
        if (heal > 0f) EffectApplier.heal(owner, ResourceType.HEALTH, heal);
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
        return List.of(Component.translatable("stat_effect.rage_consume_heal",
                Component.literal(Math.round(entry.amount().getValue()) + "%")));
    }
}
