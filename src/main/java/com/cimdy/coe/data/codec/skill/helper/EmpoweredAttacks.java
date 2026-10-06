package com.cimdy.coe.data.codec.skill.helper;

import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 战吼强化攻击（Warcry Empower）运行时计数器 — 存于 {@link AdditionData}（key = {@link AdditionKeys#EMPOWERED_ATTACKS}）。
 *
 * <p>POE2 机制参考：战吼（[Command]/warcry 类技能）施放时「强化(Empower/Exert)」施法者的接下来 N 次攻击，
 * 被强化的攻击携带 {@code empowered} 标签，使 {@code inc_empowered_damage}(DAMAGE+EMPOWERED) 等条目生效。
 * 每次攻击消耗一次计数（在 {@code DamageHelper.applyDamageMultipliers} 的伤害倍率中央点注入标签）。
 */
public final class EmpoweredAttacks {

    /**
     * 「本次攻击是否为强化攻击」瞬态标记（按攻击者 UUID）。
     * 必要性：{@link #consumeOne} 在伤害倍率中央点即扣减计数，等击杀事件（{@code LivingDeathEvent}）
     * 触发时计数已归零，无法再用 {@link #get} 判定「本次击杀是否来自强化攻击」。
     * 故在倍率点把本次判定结果落到此处，供击杀读点（如「以强化攻击击杀的敌人爆炸」）读取。
     * 瞬态、不持久化：仅存活于同一次攻击→击杀的同 tick 因果链内。
     */
    private static final Map<UUID, Boolean> LAST_HIT_EMPOWERED = new ConcurrentHashMap<>();

    private EmpoweredAttacks() {}

    /** 记录本次攻击的强化判定（由伤害倍率中央点调用）。 */
    public static void markLastHit(LivingEntity living, boolean empowered) {
        if (living == null) return;
        LAST_HIT_EMPOWERED.put(living.getUUID(), empowered);
    }

    /** 本次（最近一次）攻击是否为强化攻击；从未攻击过恒 false。 */
    public static boolean lastHitEmpowered(LivingEntity living) {
        return living != null && Boolean.TRUE.equals(LAST_HIT_EMPOWERED.get(living.getUUID()));
    }

    /** 读取当前剩余强化攻击次数（0 = 无）。 */
    public static int get(LivingEntity living) {
        // 原 AttachmentRegistry.EMPOWERED_ATTACKS 已迁入 AdditionData（key=empowered_attacks）
        return (int) AdditionData.getOrDefault(living, AdditionKeys.EMPOWERED_ATTACKS, 0.0);
    }

    /** 授予强化攻击次数（叠加）。 */
    public static void grant(LivingEntity living, int count) {
        if (count <= 0) return;
        // 原 AttachmentRegistry.EMPOWERED_ATTACKS 已迁入 AdditionData（key=empowered_attacks）
        AdditionData.addOrReplaceData(living, AdditionKeys.EMPOWERED_ATTACKS, get(living) + count);
    }

    /**
     * 消耗一次强化攻击，返回本次攻击是否被强化（即是否应携带 {@code empowered} 标签）。
     * 在伤害倍率读取前调用一次（每次攻击事件仅一次），避免按伤害类型重复扣减。
     */
    public static boolean consumeOne(LivingEntity living) {
        int c = get(living);
        if (c <= 0) return false;
        // 原 AttachmentRegistry.EMPOWERED_ATTACKS 已迁入 AdditionData（key=empowered_attacks）
        AdditionData.addOrReplaceData(living, AdditionKeys.EMPOWERED_ATTACKS, c - 1);
        return true;
    }
}
