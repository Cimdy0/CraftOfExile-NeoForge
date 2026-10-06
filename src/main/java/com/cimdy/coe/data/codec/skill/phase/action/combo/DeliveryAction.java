package com.cimdy.coe.data.codec.skill.phase.action.combo;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutors;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.UUID;

/**
 * 投递动作 — 在动作执行点按需生成一次投递物（投射物 / 范围 / 残片 / 危害物 / 光环 …）。
 *
 * <p><b>为什么需要它</b>：投递物平时由 {@code SkillPhase.initialize} 在阶段初始化时按
 * {@code SkillPhase#delivery} 生成，<b>一次施放只有一次机会</b>。而「击杀时生成残片」
 * 「暴击时落下陨石」这类效果的生成时机是<b>事件回调</b>（{@code EventListenPhase}），
 * 不在施放路径上——此前事件回调侧完全无法触达 {@link DeliveryExecutors}，残片等形态
 * 在事件驱动场景下无生产者。本动作即为该通路：把「投递形态」包装成一次可执行的动作。
 *
 * <p><b>相位实例契约</b>：投递物（尤其 {@code SkillEntity} / {@code SkillEntity} 一类
 * {@code SkillEntity}）被触发时需经 {@code (casterId, phaseId)} 反查
 * {@link SkillPhaseInstance} 并点燃 {@code ON_HIT}，故本动作<b>必须</b>运行在带
 * {@code phaseId} 的 {@link ActionContext} 上：
 * <ul>
 *   <li>常规阶段路径：{@code SkillPhase.initialize} 生成 phaseId 并注入（{@code withPhaseId}）；</li>
 *   <li>事件回调路径：{@code EventListenPhase.onEvent} 新建相位实例并注入（见其实现）。</li>
 * </ul>
 * 两条路径同一口径，本动作不区分。
 *
 * <p><b>效果与形态解耦</b>：本动作只负责「把投递物放到世界里」，投递物被触发后执行哪些动作
 * 由所属阶段的 {@code actions}（如 {@code ON_HIT}）在数据层定义，本动作不解释效果内容。
 *
 * @param delivery 待生成的投递形态。
 */
public record DeliveryAction(Delivery delivery) implements Action {
    public static final Codec<DeliveryAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            Delivery.CODEC.fieldOf("delivery").forGetter(DeliveryAction::delivery)
    ).apply(i, DeliveryAction::new));

    @Override
    public Action.Type<?> getType() {
        return ActionTypes.DELIVERY;
    }

    @Override
    public void execute(ActionContext context) {
        UUID phaseId = context.phaseId();
        if (phaseId == null) return;
        UUID casterId = context.caster().getUUID();
        SkillPhaseInstance instance = SkillPhaseInstanceTracker.find(casterId, phaseId);
        DeliveryExecutors.execute(delivery, context, casterId, phaseId, instance);
        // 登记「本实例实际生成的投递物」：事件派生的投递物在宿主相位里没有声明，
        // 其回调（残片拾取 ON_PICKUP / 危害物踩中 ON_HIT）的命中判定与标签作用域须按它求值。
        if (instance != null) instance.setSpawnedDelivery(delivery);
    }
}
