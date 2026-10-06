package com.cimdy.coe.data.helper.resource;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.helper.hub.ResourceEventHelper;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public class HealthHelper {
    public static boolean isFullHealth(LivingEntity living) {
        return living.getHealth() == living.getMaxHealth();
    }

    public static float getHealthRegeneration(LivingEntity living) {
        // 完全破甲禁再生：受害方持 CANNOT_REGEN_HEALTH 时归零生命再生（经 ARMOUR_BROKEN_FULLY 行为真施加，R3 安全，非死线）
        if (StatusEngine.has(living, StatusDefs.CANNOT_REGEN_HEALTH)) return 0;
        // 归一：REGENERATION_RATE/MAX_REGENERATION 单属性 + {health, regen} 标签作用域（替代 HEALTH_REGENERATION_RATE/MAX_HEALTH_REGENERATION）
        double rate = DamageHelper.readInScopePublic(living, Set.of(GateTag.HEALTH, "regen"), AttributeRegistry.REGENERATION_RATE);
        double pct = DamageHelper.readInScopePublic(living, Set.of(GateTag.HEALTH, "regen"), AttributeRegistry.MAX_REGENERATION);
        // 跨来源恢复率统一倍率（POE2 Recovery Rate：Regen/Leech/Flask/Recoup 同乘）
        double recovery = DamageHelper.readInScopePublic(living, Set.of(GateTag.HEALTH, "regen"), AttributeRegistry.RECOVERY_RATE);
        return (float) ((rate + living.getMaxHealth() * pct) * 0.05F * recovery);
    }

    public static Pair<Float, Float> value(LivingEntity living, float amount) {
        // 返回Pair 预计扣除的生命值和溢出的伤害
        float health = living.getHealth();
        if (health > amount) {
            return Pair.of(amount, 0F);
        } else {
            return Pair.of(health, amount - health);
        }
    }

    public static void tickHealthRegeneration(LivingEntity living) {
        // 自然回复也走统一资源回调枢纽（数值/溢出/检测/转化一致），使「生命回复转能量护盾」等转化生效
        ResourceEventHelper.onHeal(living, GateTag.HEALTH, getHealthRegeneration(living), "regen", null);
    }
}
