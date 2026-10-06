package com.cimdy.coe.data.codec.support.addition;

import net.minecraft.world.entity.LivingEntity;

/**
 * 金币（Gold）余额 — 存于 {@link AdditionData}（key = {@link AdditionKeys#GOLD}）。
 *
 * <p>POE2 机制参考：金币是唯一通用货币，同时承担「重置天赋点」的花费（POE2 已移除 Orb of Regret，
 * 重置 100% 走金币）。本类只做<b>余额存取</b>这一层基础设施，不关心产出与消费的业务来源。
 *
 * <p>存储形式沿用 {@code Map<String, Double>}（{@code double} 可精确表示 {@code 2^53} 以内整数，
 * 覆盖任何合理金币量）。
 *
 * <p><b>死亡保留</b>：{@code AttachmentRegistry.ADDITION_DATA} 走 {@code registerSync}，
 * 即 {@code copyOnDeath = false}，死亡后整个附件清零。因此金币在
 * {@code AttributeEvent#PlayerCloneEvent} 的 {@code isWasDeath()} 分支内单独搬运，
 * 而不改注册为 {@code copyOnDeath = true}（那会让 corpse_count / empowered_attacks /
 * active_weapon_set 等全部键跨死亡保留，副作用过大）。
 */
public final class GoldState {
    private GoldState() {
    }

    /** 读取当前金币余额（缺省 0）。 */
    public static long get(LivingEntity living) {
        return (long) AdditionData.getOrDefault(living, AdditionKeys.GOLD, 0.0);
    }

    /**
     * 增减金币，{@code delta} 可为负。若结果 {@code < 0} 则<b>不改动</b>余额并返回 {@code false}；
     * 否则写入新余额并返回 {@code true}。
     */
    public static boolean add(LivingEntity living, long delta) {
        long next = get(living) + delta;
        if (next < 0) return false;
        set(living, next);
        return true;
    }

    /**
     * 支付 {@code cost} 金币。余额不足则返回 {@code false} 且<b>不扣减</b>；成功扣减返回 {@code true}。
     * {@code cost <= 0} 视为无需支付，返回 {@code true}。
     */
    public static boolean pay(LivingEntity living, long cost) {
        long cur = get(living);
        if (cost <= 0) return true;
        if (cur < cost) return false;
        set(living, cur - cost);
        return true;
    }

    /** 直接设置余额，负数钳到 0。 */
    public static void set(LivingEntity living, long value) {
        AdditionData.addOrReplaceData(living, AdditionKeys.GOLD, (double) Math.max(0L, value));
    }
}
