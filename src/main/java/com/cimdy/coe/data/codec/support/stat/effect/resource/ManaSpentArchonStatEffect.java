package com.cimdy.coe.data.codec.support.stat.effect.resource;

import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;

import com.cimdy.coe.event.TriggerContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 累计消耗 X% 最大魔力 → 获得元素执政官（Elemental Archon）。
 *
 * <p>复用 {@link com.cimdy.coe.event.EventName#RESOURCE_SPEND} 火（{@code ResourceEventHub.onSpend} 真实发射，
 * owner=玩家、事件名 {@code resource_spend} 自动并入 fireTags、资源键 {@code mana}（或 {@code mana:<来源>}）作标签），
 * 并经 {@link StatEvalContext#eventValue()} 读取本次真实消耗量（{@code actual}）。
 *
 * <p><b>累计语义</b>：消耗量累加在实体附件 {@link AttachmentRegistry#ARCHON_MANA_PROGRESS} 上，
 * 累计达 {@code X% × 最大魔力} 阈值时按中央 {@link StatusEngine#apply} 施加既有 {@link StatusDefs#ELEMENTAL_ARCHON} 状态，
 * 并<b>扣减阈值、余量结转</b>（与「ES 充能→执政官」同源状态，非死线）。
 * 采用累计而非单次判定是因为 X 通常为 100——单次耗魔几乎不可能达到整个魔力池。
 *
 * <p>仅魔力支出计入（条目门控 [resource_spend, mana]：裸资源键标签由 ResourceEventHelper 在发射时并入），生命 / 怒气 / 能量护盾消耗不计。
 */
public final class ManaSpentArchonStatEffect extends AbstractStatEffect {

    public ManaSpentArchonStatEffect(GateExpr tags) {
        super(tags);
    }

    public static final MapCodec<ManaSpentArchonStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, ManaSpentArchonStatEffect::new));

    @Override
    public String type() {
        return "mana_spent_archon";
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx, @Nullable TriggerContext tc) {
        LivingEntity owner = evalCtx.owner();
        if (owner == null || owner.isDeadOrDying() || !owner.hasData(AttachmentRegistry.STAT_STORE)) return;
        // 门控已上移至条目层 [resource_spend, mana]（见 StatEffectGen 注册点：裸资源键标签由 ResourceEventHelper 在发射时并入），此处仅消费真实耗魔量。
        // 事件携带的真实耗魔量（RESOURCE_SPEND 发射时透传的 actual）；无载荷（非资源消耗事件）静默跳过。
        Double spentValue = evalCtx.eventValue();
        if (spentValue == null) return;
        float spent = spentValue.floatValue();
        if (spent <= 0f) return;
        double pct = entry.amount().getValue();   // X（%）
        if (pct <= 0) return;
        float maxMana = ResourceRegistry.getMaxMana(owner);
        if (maxMana <= 0f) return;
        double threshold = (pct / 100.0) * maxMana;
        double progress = owner.getData(AttachmentRegistry.ARCHON_MANA_PROGRESS) + spent;
        if (progress < threshold) {
            owner.setData(AttachmentRegistry.ARCHON_MANA_PROGRESS, (float) progress);
            return;
        }
        StatusEngine.apply(owner, StatusDefs.ELEMENTAL_ARCHON, owner,
                1.0, StatusDefs.ELEMENTAL_ARCHON.defaultDurationTicks, 1);
        // 授予后扣减阈值、余量结转（多份同名词条不重复触发：首个授予即结转，后续条目本轮不再达标）。
        owner.setData(AttachmentRegistry.ARCHON_MANA_PROGRESS, (float) Math.max(0.0, progress - threshold));
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
        return List.of(Component.translatable("stat_effect.mana_spent_archon",
                StatValueKey.of("status", "elemental_archon"),
                Component.literal(Math.round(entry.amount().getValue()) + "%"),
                Component.literal("Max Mana")));
    }
}
