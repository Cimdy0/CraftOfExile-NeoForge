package com.cimdy.coe.data.helper.impl;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 诅咒激活延迟机制（POE2：诅咒施放后并非立即生效，而是经过一段「激活延迟」后才对周围敌人施加）。
 *
 * <p>施放诅咒技能时不再立即 {@link StatusEngine#applyCurseToNearby}，而是入队一个延迟施放项；
 * 本助手在施法者每 tick 的驱动下，于计划时刻重放 {@code applyCurseToNearby}（按当前位置重新判定周围敌人，
 * 与「施放即施加」语义同源，仅延迟了生效时刻）。
 *
 * <p>延迟计算（读取施法者属性，R3 安全读点）：
 * <pre>{@code
 *   delay = BASE_DELAY_TICKS * (1 + CURSE_ACTIVATION_DELAY_MORE) / (1 + max(0, CURSE_ACTIVATION_FREQUENCY))
 * }</pre>
 * 其中 {@code DOUBLE_ACTIVATION_DELAY_OF_CURSES} 经接线使 {@code CURSE_ACTIVATION_DELAY_MORE}=+1.0（×2），
 * {@code FASTER_CURSE_ACTIVATION} 经接线向 {@code CURSE_ACTIVATION_FREQUENCY} 加百分比（延迟缩短）。
 */
public final class CurseActivation {

    private CurseActivation() {}

    /** 基础诅咒激活延迟（tick）：POE2 诅咒固有基准延迟量级（0.6s），可调。 */
    private static final int BASE_DELAY_TICKS = 12;

    /** 施放诅咒技能时调用：按施法者属性计算延迟后入队（延迟≤0 则立即施加）。 */
    public static void enqueue(LivingEntity caster, Identifier skillId, double radius, int duration) {
        double more = caster.getAttributeValue(AttributeRegistry.CURSE_ACTIVATION_DELAY_MORE);
        double freq = caster.getAttributeValue(AttributeRegistry.CURSE_ACTIVATION_FREQUENCY);
        double delay = BASE_DELAY_TICKS * (1.0 + more) / (1.0 + Math.max(0.0, freq));
        if (delay <= 0) {
            StatusEngine.applyCurseToNearby(caster, skillId, radius, duration);
            return;
        }
        long scheduled = caster.level().getGameTime() + Math.round(delay);
        List<CursePending> list = caster.getData(AttachmentRegistry.CURSE_PENDING);
        List<CursePending> next = new ArrayList<>(list);
        next.add(new CursePending(skillId, radius, duration, scheduled));
        caster.setData(AttachmentRegistry.CURSE_PENDING, next);
    }

    /** 施法者每 tick 驱动：消费到期的延迟诅咒施放项。 */
    public static void tick(LivingEntity caster) {
        List<CursePending> list = caster.getData(AttachmentRegistry.CURSE_PENDING);
        if (list.isEmpty()) return;
        long now = caster.level().getGameTime();
        List<CursePending> remaining = new ArrayList<>();
        boolean changed = false;
        for (CursePending p : list) {
            if (now >= p.scheduledTick()) {
                if (!caster.isDeadOrDying() && caster.level() instanceof ServerLevel) {
                    StatusEngine.applyCurseToNearby(caster, p.skillId(), p.radius(), p.duration());
                }
                changed = true;
            } else {
                remaining.add(p);
            }
        }
        if (changed) caster.setData(AttachmentRegistry.CURSE_PENDING, remaining);
    }
}
