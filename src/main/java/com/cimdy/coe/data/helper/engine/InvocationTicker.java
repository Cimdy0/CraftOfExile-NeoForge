package com.cimdy.coe.data.helper.engine;

import com.cimdy.coe.data.helper.hub.SpiritManager;
import com.cimdy.coe.event.skill.SkillEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.gem.KeyBind;
import com.cimdy.coe.data.codec.skill.gem.SkillAssembly;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.SkillPhasePlayer;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.skill.phase.action.MetaSkillAction;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 调用（Invocation）技能周期触发器 — 让持有标记 {@link com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect}
 * 的玩家，其装配中的元技能（阶段树含 {@link MetaSkillAction} 的宿主技能）每隔固定秒数自动触发链接法术。
 *
 * <p>PoE2 对应：某类「Metacarpal / Invocation」技能不再是「由某个事件（暴击/闪避…）触发」，
 * 而是改为<b>周期性</b>触发其链接的法术，触发间隔由天赋节点给出
 * （StatId {@code INVOCATION_SKILLS_INSTEAD_TRIGGER_SPELLS_EVERY_SECONDS}，node 11230 = 2.0 秒）。
 *
 * <p><b>接线方式</b>：
 * <ol>
 *   <li>标记幅度 = 触发间隔秒数（{@link StatEngine#markerMagnitude}，缺省/无标记时为 0 → 整体跳过）；</li>
 *   <li>每 tick 遍历在线玩家装配槽，解析每个元技能宿主，[深度优先] 遍历其阶段树找出全部
 *       {@link MetaSkillAction}；</li>
 *   <li>按 {@code 技能id#动作序号} 维护倒计时（{@link AttachmentRegistry#INVOCATION_TIMERS}），
 *       归零即调用 {@link MetaSkillAction#triggerLinked} 触发链接法术并重置为周期；</li>
 *   <li>本轮未使用的计时键随 {@link ServerPlayer#setData} 覆盖被剪除，技能摘除后不留残留。</li>
 * </ol>
 *
 * <p>与 {@code SpiritManager} 同构（同一倒计时/剪除范式），但作用于「元技能触发」而非「常驻光环」：
 * 这里只触发链接法术，不重放阶段动作。
 */
public final class InvocationTicker {

    private InvocationTicker() {
    }

    /** 每 tick 入口：遍历在线玩家，推进其调用技能周期触发（由 {@code SkillEvent#onSkillEvent} 调用）。 */
    public static void tickAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickPlayer(player);
        }
    }

    private static void tickPlayer(ServerPlayer player) {
        double secs = StatEngine.magnitudeOf(player, "invocation_trigger_interval", Set.of());
        if (secs <= 0.0) {
            // 未持标记（节点未授 / 被摘除）：清空计时表，避免残留键导致摘除后仍触发。
            if (!player.getData(AttachmentRegistry.INVOCATION_TIMERS).isEmpty()) {
                player.setData(AttachmentRegistry.INVOCATION_TIMERS, Map.of());
            }
            return;
        }
        int periodTicks = Math.max(1, (int) Math.round(secs * 20.0));
        SkillAssembly assembly = KeyBind.getMergedAssembly(player);
        Map<String, Integer> prev = player.getData(AttachmentRegistry.INVOCATION_TIMERS);
        Map<String, Integer> next = new HashMap<>();
        // ActionContext 构造与 SpiritManager#applyAura 同款（无命中目标的自施法上下文）。
        ActionContext actionCtx = new ActionContext(
                null, player, Optional.empty(),
                player.position(), Vec3.ZERO, 1.0f, 0,
                null, null, null, Optional.empty());
        ExpressionContext ctx = ExpressionContext.get(player);
        for (SkillData sd : assembly.slots()) {
            if (sd.isEmpty()) continue;
            Skill skill = sd.getSkill();
            if (skill == null || !skill.isMetaSkill()) continue;
            String skillId = sd.id().toString();
            // 动作序号（深度优先遍历序，技能数据运行期不变 ⇒ 键稳定），用于周期计时器寻址。
            int[] actionIndex = {0};
            for (SkillPhasePlayer root : skill.phases()) {
                tickPhase(skillId, root.phase(), actionIndex, periodTicks, prev, next, actionCtx, ctx);
            }
        }
        player.setData(AttachmentRegistry.INVOCATION_TIMERS, Map.copyOf(next));
    }

    /** 深度优先遍历阶段树（含 event_listen 动作），对每个 MetaSkillAction 推进倒计时/到点触发。 */
    private static void tickPhase(String skillId, SkillPhase phase, int[] actionIndex, int periodTicks,
                                  Map<String, Integer> prev, Map<String, Integer> next,
                                  ActionContext actionCtx, ExpressionContext ctx) {
        if (phase == null) return;
        for (TriggeredAction ta : phase.actions()) {
            triggerIfDue(ta, skillId, actionIndex[0]++, periodTicks, prev, next, actionCtx, ctx);
        }
        phase.eventListen().ifPresent(listen -> {
            for (TriggeredAction ta : listen.actions()) {
                triggerIfDue(ta, skillId, actionIndex[0]++, periodTicks, prev, next, actionCtx, ctx);
            }
        });
        for (SkillPhasePlayer child : phase.children()) {
            tickPhase(skillId, child.phase(), actionIndex, periodTicks, prev, next, actionCtx, ctx);
        }
    }

    /** 若该动作是元技能动作，则推进其周期倒计时（归零 → 触发链接法术并重置）。 */
    private static void triggerIfDue(TriggeredAction ta, String skillId, int index, int periodTicks,
                                     Map<String, Integer> prev, Map<String, Integer> next,
                                     ActionContext actionCtx, ExpressionContext ctx) {
        if (!(ta.action() instanceof MetaSkillAction meta)) return;
        String key = skillId + "#" + index;
        int left = prev.getOrDefault(key, periodTicks) - 1;
        if (left <= 0) {
            meta.triggerLinked(ctx, actionCtx);
            left = periodTicks;
        }
        next.put(key, left);
    }
}
