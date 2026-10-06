package com.cimdy.coe.data.helper.combat;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision.Explode;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionAnchor;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionChains;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionScope;
import com.cimdy.coe.data.codec.support.stat.condition.StatCondition;

import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DamageTypeRegistry;
import com.cimdy.coe.data.codec.skill.helper.EmpoweredAttacks;
import com.cimdy.coe.event.EventName;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

/**
 * 击杀爆炸机制（POE2 Explode）：击杀时按条件的百分比掷骰，命中则对死者周围敌人造成火焰爆发。
 *
 * <p><b>目标获取不再手写</b>：曾被硬编码的多跳（死者 → 燃烧判定 → 以死者为心的范围敌方遍历）
 * 现由 Condition 模型表达 —— {@link ConditionChains#ignitedKillSpreadToNearbyEnemies(double)} /
 * {@link ConditionChains#aroundAnchor(ConditionAnchor, double, PresenceCountValue.Relation)}
 * 经 {@link StatCondition#resolve} 直接给出「该判断下的实体集」（空集 = 不满足），
 * 本类只保留三件事：<b>属性掷骰、数值（最大生命 × 系数）、火焰投递</b>。
 *
 * <p>两个通道（燃烧中 / 强化攻击）由本类按既有条件选中后调用 {@link #tryBurst}，事件端只调用 {@link #onKill}。
 */
public final class KillBurstHelper {
    private KillBurstHelper() {}

    /** 击杀爆炸伤害系数：语料「a tenth of their Max Health」= 死者最大生命 × 1/10。 */
    private static final double EXPLODE_BURST_LIFE_FRACTION = 0.1;
    /** 击杀爆炸半径（方块）：以死者为心的球形范围。现为 Condition {@code radius} 节点的入参。 */
    private static final double EXPLODE_BURST_RADIUS = 2.5;

    /**
     * 击杀入口：① 死者处于燃烧中 → 燃烧爆炸通道；② 本次击杀来自强化攻击 → 强化爆炸通道。
     *
     * <p>通道条件本身即条件链的第一层：① 的「被你点燃」判定是
     * {@code filter{of: anchor(killed), where: ignited_by_source}}，故本处不再单独读 StatusEngine；
     * ② 的强化判定用 {@link EmpoweredAttacks#lastHitEmpowered} 标记（由伤害倍率中央点写入）：
     * {@code consumeOne()} 在伤害结算时已扣减计数，击杀时再用 {@code get()} 判不出来。
     */
    public static void onKill(LivingEntity killer, LivingEntity victim) {
        if (killer == null || victim == null) return;
        // 通道①（验收链）：anchor(killed) → filter(被你点燃) → chain → radius(锚点=当前元素, 敌方)
        tryBurst(killer, victim, AttributeRegistry.BURNING_EXPLODE_ON_KILL_CHANCE,
                ConditionChains.ignitedKillSpreadToNearbyEnemies(EXPLODE_BURST_RADIUS));
        // 通道②：同一条链去掉「被你点燃」那一层 —— anchor(killed) → radius(敌方)
        if (EmpoweredAttacks.lastHitEmpowered(killer)) {
            tryBurst(killer, victim, AttributeRegistry.EMPOWERED_EXPLODE_ON_KILL_CHANCE,
                    ConditionChains.aroundAnchor(ConditionAnchor.KILLED, EXPLODE_BURST_RADIUS,
                            PresenceCountValue.Relation.ENEMY));
        }
    }

    /**
     * 击杀爆炸投递：按 {@code chanceAttr} 的<b>百分比</b>掷骰，命中则解析 {@code condition}
     * 得到待作用实体集，对其中每个实体造成「死者最大生命 × {@link #EXPLODE_BURST_LIFE_FRACTION}」火焰伤害。
     *
     * <p>{@code condition} 解析出的集合<b>就是目标</b>（空集 = 条件不满足，自然不投递），
     * 半径 / 阵营 / 排除自身与死者均由模型节点承担，不再在此手写 AABB 与遍历谓词。
     */
    private static void tryBurst(LivingEntity caster, LivingEntity victim, Holder<Attribute> chanceAttr,
                                 StatCondition condition) {
        if (!(caster.level() instanceof ServerLevel serverLevel)) return;
        double chance = Math.max(0.0, Math.min(1.0, caster.getAttributeValue(chanceAttr)));
        if (chance <= 0) return;
        if (caster.getRandom().nextDouble() >= chance) return;
        float amount = (float) (victim.getMaxHealth() * EXPLODE_BURST_LIFE_FRACTION);
        if (amount <= 0) return;
        // 事件上下文（KILL 事件自带两方）：A = 击杀者，B = 承受者，eventEntity = 本次被杀实体
        ConditionScope scope = ConditionScope.of(caster, victim, victim, EventName.KILL, null,
                victim.position(), null);
        for (LivingEntity target : condition.resolve(scope)) {
            if (target == null || target.isDeadOrDying()) continue;
            target.hurtServer(serverLevel, serverLevel.damageSources().source(DamageTypeRegistry.FIRE, caster), amount);
        }
    }
}
