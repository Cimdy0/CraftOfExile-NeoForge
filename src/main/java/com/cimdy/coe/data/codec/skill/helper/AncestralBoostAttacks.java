package com.cimdy.coe.data.codec.skill.helper;

import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import net.minecraft.world.entity.LivingEntity;

/**
 * 先祖强化攻击（Ancestral Boost）运行时计数器 — 存于 {@link AdditionData}。
 *
 * <p>POE2 机制参考：Warrior 的 slam 系技能完整执行后，强化施法者的「接下来 1 次攻击」，
 * 被强化的攻击携带 {@code ancestral_boost} 标签，使
 * {@code ANCESTRALLY_BOOSTED_ATTACKS_DEAL_INC_DAMAGE}(DAMAGE+ANCESTRAL_BOOST+ATTACK) 与
 * {@code INC_AREA_OF_EFFECT_OF_ANCESTRALLY_BOOSTED_ATTACKS}(EFFECT_AREA+ANCESTRAL_BOOST+ATTACK) 两条
 * 此前零发射点、恒不匹配的死线生效（详见 {@link com.cimdy.coe.data.codec.support.stat.tag.GateTag#ANCESTRAL_BOOST} 的惰性注释）。
 *
 * <p><b>双池模型</b>：先祖强化攻击有两个独立来源，互不污染：
 * <ul>
 *   <li>{@code ANCESTRAL_BOOST_ATTACKS}（slam 池）：slam 技能完整执行退出时授予，纯「下一次攻击」语义、<b>不过期</b>；</li>
 *   <li>{@code ANCESTRAL_BOOST_HEAVY_STUN_ATTACKS}（重晕池）：「重晕稀有/独特敌人」路径授予，受
 *       Event.State 的 {@code caused_ailment}+{@code heavy_stun} 计时窗口（重晕后 4 秒 / 80 ticks）约束，过期即失效。</li>
 * </ul>
 * 两池合计为总剩余次数；消耗时优先扣 slam 池（不过期），再扣重晕池（窗口过期则跳过、绝不污染 slam 池）。
 *
 * <p>完全镜像 {@link EmpoweredAttacks}（战吼强化攻击）的「次数计数 + 命中消耗」范式：
 * <ul>
 *   <li>授予：slam 技能在 {@code SkillStatScopes#enter} 作用域退出调用 {@link #grant}（slam 池）；
 *       重晕事件经 {@link #grantFromHeavyStun} 写入重晕池 + 戳记；</li>
 *   <li>消耗与发射：被强化的攻击在 {@code DamageHelper#applyDamageMultipliers} 的伤害倍率中央点经 {@link #consumeOne}
 *       扣减一次，并把 {@code ancestral_boost} 标签并入本次命中的读取作用域，使命中伤害 / 面积加成即时生效；</li>
 *   <li>作用域发射的另一次机会：施放下一次攻击技能时，{@code SkillStatScopes#enter} 若检测到总计数 > 0 且本次为攻击，
 *       提前把 {@code ancestral_boost} 并入技能作用域，使面积加成本就在施放期间（面积结算早于命中）被正确求值。</li>
 * </ul>
 *
 * <p>刻意不新增任何「全局机制」：复用 {@link EmpoweredAttacks} 同款的 AdditionData 计数原语与
 * StatContext 标签发射口径，仅新增计数键与标签，范围严格限定于「slam 退出后 / 重晕后 4 秒内、下一次攻击」——
 * 绝不把加成下放到 {@code melee}（会把增益放大到全部近战攻击，语义越界，与 GateTag 注释口径一致）。
 */
public final class AncestralBoostAttacks {

    private AncestralBoostAttacks() {}

    /** 重晕授予池过期窗（ticks）：4 秒 × 20 ticks。仅重晕池受此约束；窗口读 Event.State 的 caused_ailment+heavy_stun 计时，不另立戳记。 */
    private static final int HEAVY_STUN_WINDOW_TICKS = 80;

    /** 重晕授予池剩余次数（受 4 秒窗口约束）；窗口过期返回 0。 */
    private static int heavyStunCount(LivingEntity living) {
        int count = (int) AdditionData.getOrDefault(living, AdditionKeys.ANCESTRAL_BOOST_HEAVY_STUN_ATTACKS, 0.0);
        if (count <= 0) return 0;
        // 窗口 = 重晕后 4 秒：直接读 Event.State 的 caused_ailment+heavy_stun 计时（引擎权威事件计时源），
        // 不另立 getGameTime() 戳记（绕开事件计时机制的反模式，见用户裁定）。
        if (!Event.get(living).recently(EventName.CAUSED_AILMENT, GateTag.HEAVY_STUN, HEAVY_STUN_WINDOW_TICKS)) return 0;
        return count;
    }

    /** slam 授予池剩余次数（纯「下一次攻击」语义，无过期）。 */
    private static int slamCount(LivingEntity living) {
        return (int) AdditionData.getOrDefault(living, AdditionKeys.ANCESTRAL_BOOST_ATTACKS, 0.0);
    }

    /** 读取总剩余先祖强化攻击次数（0 = 无）。 */
    public static int get(LivingEntity living) {
        return slamCount(living) + heavyStunCount(living);
    }

    /** slam 路径授予（纯「下一次攻击」语义，无过期戳记）。 */
    public static void grant(LivingEntity living, int count) {
        if (count <= 0) return;
        AdditionData.addOrReplaceData(living, AdditionKeys.ANCESTRAL_BOOST_ATTACKS, slamCount(living) + count);
    }

    /** 重晕路径授予：写入独立池，受「重晕后 4 秒」窗口约束（窗口由 Event.State 计时，见 {@link #heavyStunCount}）。 */
    public static void grantFromHeavyStun(LivingEntity living, int count) {
        if (count <= 0) return;
        // 以 liveCount 为基准（过期窗口内 heavyStunCount 已归零，避免把失效残留叠入）。
        AdditionData.addOrReplaceData(living, AdditionKeys.ANCESTRAL_BOOST_HEAVY_STUN_ATTACKS,
                heavyStunCount(living) + count);
    }

    /**
     * 消耗一次先祖强化攻击，返回本次攻击是否被强化（即是否应携带 {@code ancestral_boost} 标签）。
     * 优先消耗 slam 池（不过期），再消耗重晕池（窗口过期则跳过、不污染 slam 池）。
     * 在伤害倍率读取前调用一次（每次攻击事件仅一次），避免按伤害类型重复扣减。
     */
    public static boolean consumeOne(LivingEntity living) {
        int slam = slamCount(living);
        if (slam > 0) {
            AdditionData.addOrReplaceData(living, AdditionKeys.ANCESTRAL_BOOST_ATTACKS, slam - 1);
            return true;
        }
        int hs = heavyStunCount(living);
        if (hs > 0) {
            int next = hs - 1;
            AdditionData.addOrReplaceData(living, AdditionKeys.ANCESTRAL_BOOST_HEAVY_STUN_ATTACKS, next);
            return true;
        }
        return false;
    }
}
