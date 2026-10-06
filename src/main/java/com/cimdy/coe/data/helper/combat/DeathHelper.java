package com.cimdy.coe.data.helper.combat;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;
import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.data.helper.hub.SpiritManager;
import com.cimdy.coe.data.helper.skill.SummonCountHelper;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;


import com.cimdy.coe.data.codec.skill.phase.event.SkillEventBus;
import com.cimdy.coe.data.codec.skill.context.SkillDamageSource;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.event.combat.DamageEvent;
import com.cimdy.coe.event.combat.GameEvent;
import com.cimdy.coe.helper.OwnerHelper;
import com.cimdy.coe.item.charm.CharmEngine;
import com.cimdy.coe.item.flask.FlaskHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * 死亡机制：击杀侧（充能 / 触发型 stat / 元宝石 / 捷爆发 / 击杀爆炸）、
 * 死亡受害侧（{@code TAKEN_DEATH} / 召唤物死亡）、尸体累积与死亡时状态清理。
 *
 * <p>事件类只做派发，全部机制收口于本类（{@code DamageEvent} 仅调用）。
 */
public final class DeathHelper {
    private DeathHelper() {}

    /** 怪物死亡 → 在击杀玩家处累积 1 具尸体（尸体计在消耗者身上）。 */
    public static void onCorpse(LivingEntity dead, DamageSource source) {
        if (!(dead.level() instanceof ServerLevel)) return;
        if (!(dead instanceof Mob)) return;
        if (source.getEntity() instanceof ServerPlayer player) {
            ResourceRegistry.addCorpse(player, 1);
        }
    }

    /**
     * 死亡统一入口（服务端）。
     *
     * <p>击杀侧 owner=击杀者：计时 timer（{@code time_kill} 窗口）、{@code kill} 事件记录、
     * Flask/Charm 充能、触发型 stat（并入受害者状态维，支撑「击杀中毒/点燃/冰缓敌人」族）、
     * 元宝石发布、捷爆发、击杀爆炸。
     *
     * <p>受害侧 owner=死者：{@code TAKEN_DEATH} 与召唤物死亡（{@code MINION_DEATH}，记在主人名下）。
     * 环境/DoT/虚空致死（无直接攻击实体）同样触发受害侧。
     */
    public static void onDeath(LivingEntity living, DamageSource source) {
        if (!(living.level() instanceof ServerLevel)) return;
        Entity entity = source.getEntity();
        if (entity instanceof LivingEntity target) {
            // 计时 timer：击杀者最近击杀（递增经过 tick，math 用 time_kill 读取窗口）
            ExpressionContext.get(target).refreshTimer(EventName.KILL);
            // 触发历史（统一事件名 kill）：供 recently("kill") 条件读取。
            Event.record(target, EventName.KILL);
            // Flask / Charm 击杀充能（POE2 Monster Power：击杀怪物补充充能；仅玩家击杀）。
            if (target instanceof Player killerPlayer) {
                FlaskHelper.onKill(killerPlayer, living.getMaxHealth());
                CharmEngine.onKillCharge(killerPlayer, living.getMaxHealth());
                if (living instanceof Monster) {
                    CharmEngine.onKillMonster(killerPlayer);
                }
            }
            // 触发型 stat 效果：击杀事件。作用域并入「受害者状态维」（StatusEngine.activeTags），
            // 语料成族的「on Killing a Poisoned/Ignited/Frozen Enemy」由 tags [kill, poison] 组合命中。
            Set<String> killTags = new HashSet<>();
            killTags.add(GateTag.KILL);
            killTags.addAll(StatusEngine.activeTags(living));
            // 事件对象 = 本次被杀实体（living）：KILL 回调的 Context 携带两方（你 = target，被杀 = living），
            // 该实体即 StatEvalContext.eventEntity → ConditionScope.eventEntity，使 anchor:killed 可解析。
            StatEngine.fire(target, living, living,
                    StatContext.of(killTags, SkillDamageSource.skillIdOf(source), EventName.KILL, null), null);
            // 深度合并：击杀由本入口统一发布 on_kill 元事件（单一权威源）。
            // 标签并入 killTags（= kill + 受害者活跃状态），与紧邻的 StatEngine.fire 同源——
            // 此前元事件侧不携带标签，导致「击杀带点燃/感电的敌人」类 EventListenPhase 条件
            // 无法用 require_tags 表达（stat 通道能命中、元宝石通道不能，两侧不一致）。
            if (source instanceof SkillDamageSource sds && sds.getSkill() != null) {
                SkillEventBus.publishMeta(entity, sds.getSkill(), EventName.KILL,
                        killTags.toArray(new String[0]));
            }
            // 捷的击杀爆发（PoE2：捷的伤害全部来自触发效果）。捷是常驻光环、无 SkillInstance，
            // 不会进上面的元宝石分支；缺此调用时 herald 作用域词条无真实读点（静默死线）。
            if (entity instanceof ServerPlayer killer) {
                SpiritManager.triggerHeraldProcs(killer, living);
            }
            // 击杀爆炸两兄弟（条件与数值均在 KillBurstHelper 内收口）。
            KillBurstHelper.onKill(target, living);
            // 荣耀累积：ResourceRegistry.generateGlory 此前零调用点 → 荣耀恒为 0，
            // {glory} 作用域读点与战旗放置时的「消耗全部荣耀」皆为空转（静默死线）。
            // 最小实现：来源恒定为 1 点/击杀，实际生成量由 GLORY_GENERATION 倍率在 generate 内缩放
            // （数据驱动：语料「X% increased Glory Generation」经该倍率生效）。
            // 【与 PoE2 0.3.0 真值的已知差距 —— 未实现，待授权】
            //   真值（poe2db 戰爭之旗 / poe2wiki War Banner 0.3.0）：
            //   · 生成源是「攻击击中」而非击杀，生成量 = 100% Monster Power；
            //   · 放置门槛为 200 荣耀（"They all require 200 Glory to place"），非这里的 CAP=25；
            //   · 战旗放置后仍可继续累积荣耀；光环为 25% more 攻击伤害 / +50% 命中 / +25% 攻击速度，半径 6 米。
            // 项目内尚无 Monster Power 概念（CharmEngine 以 victimMaxHealth 近似），故不臆造数值，
            // 维持「恒定来源 + 词条倍率」的最小闭环，真值深化需新增 Monster Power 与放置门槛检查。
            ResourceRegistry.generateGlory(target, ResourceRegistry.BANNER_GLORY, 1, ResourceRegistry.GLORY_CAP,
                    living.level().getGameTime());
        }
        // 死亡受害侧事件——owner=死者。KILL 事件的 owner 为击杀者，死者自身无回调，
        // 「死亡时触发 X」「近期死亡」类词缀（死亡惩罚/死亡触发护符）由本入口表达。
        LivingEntity killer = (entity instanceof LivingEntity le) ? le : null;
        // 死亡事件的对象恒为死者本人（TAKEN_DEATH 的 owner 即死者，MINION_DEATH 的 other 即死的召唤物），
        // 显式传入使两侧 StatEvalContext.eventEntity 都指向 living（anchor:killed 可解析）。
        GameEvent.emit(EventName.TAKEN_DEATH, living, killer, living, Set.of(GateTag.TAKEN));
        // 召唤物死亡 → 记在主人名下（owner=主人，other=死者）：驱动「if one of your Minions has died Recently」。
        // 判据用 OwnerHelper.getOwner（与 SummonCountHelper.onMinionRemoved / SpawnAction 同口径）。
        if (OwnerHelper.getOwner(living) instanceof LivingEntity minionOwner) {
            GameEvent.emit(EventName.MINION_DEATH, minionOwner, living, living, Set.of());
        }
        // 死亡时清除轻晕/重晕并解除 NoAi（防止实体死亡行为卡顿）；玩家硬控由 StunScreen 表现。
        if (StatusEngine.has(living, StatusDefs.STUN)) {
            StatusEngine.clear(living, StatusDefs.STUN);
            if (living instanceof Mob mob) {
                mob.setNoAi(false);
            }
        }
    }
}
