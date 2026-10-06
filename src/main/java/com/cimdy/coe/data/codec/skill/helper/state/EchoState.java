package com.cimdy.coe.data.codec.skill.helper.state;

import com.cimdy.coe.data.codec.skill.modifier.impl.control.EchoMod;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.SkillPhasePlayer;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 回响调度器 — 让技能（或其某个阶段）在施放后重复一次<b>效果</b>。
 *
 * <p>语义对齐 POE2 官方定义（poe2wiki「Spell Echo Support」Mechanics）：
 * <blockquote>
 * A repeat is part of the original cast. It does not require a cost or a cooldown,
 * normally has the same critical strike roll, and does not cause <i>when you cast</i>
 * effects to occur a second time.
 * </blockquote>
 * 重放经由 {@link SkillPhase#repeat}（而非 {@link SkillPhase#initialize}）完成：
 * 只重做投递物与阶段实例，不重做 ON_CAST / ON_PRE_CAST / ON_PERFECT_TIMING 与事件监听注册，
 * 因此不二次消耗资源、不二次走冷却、不二次触发「施放时」效果。
 *
 * <p><b>两条次数来源叠加</b>：
 * <ul>
 *   <li><b>数据通道</b> —— {@link EchoMod}（技能自身 modifiers 或辅助宝石 effects，经
 *       {@link SupportProfile#echoMods()} 聚合），携带定向（skill / tag / phase）、延迟
 *       与<b>回响专属范围载荷</b>；</li>
 *   <li><b>属性通道</b> —— {@link AttributeRegistry#ECHO_COUNT}（base 0，ADD_VALUE 累加），
 *       在技能作用域内读取，故「对 [某标签] 类技能 +1 次回响」「[某技能] 额外回响一次」类
 *       词缀/天赋可直接复用既有 GateTag 定向链路，无需新机制。整技能重放、无延迟、无范围载荷
 *       （词缀只给「多一次」，不给「那一次更大」）。</li>
 * </ul>
 *
 * <p>延迟以 tick 计，第 i 次回响落在 {@code max(1, delay) * i} tick 后 —— 下限 1 tick，
 * 避免同一帧内堆叠多份投递物（POE2 的多次回响是依次发生、视觉上可分辨的）。
 *
 * <p><b>范围载荷按回响序号求值</b>：登记时就地算好（而非重放时回查），因为序号只取决于
 * 「这是第几次回响」，与重放时刻的运行时状态无关。
 *
 * <p><b>不实现 {@code ResourcePool}</b>：EchoState 为回响<b>调度器</b>（schedule / tick / hasPending），
 * 非计数型资源槽位，不构成资源池。
 */
public class EchoState {

    /**
     * 一条待重放的回响：延迟（tick）归零时重跑对应阶段一次。
     * 范围载荷在登记时按回响序号算定，随条目一起传递。
     */
    private record Pending(SkillPhase phase, int delay, double areaInc, double areaMore) {}

    private final List<Pending> pending = new ArrayList<>();

    /**
     * 登记回响。仅在本次确实生成了新的顶层阶段时调用（{@code Skill.run} 返回非空），
     * 因此「一次施放只登记一次」；引导脉冲（Continuous）重新生成阶段时会自然重新登记。
     *
     * @param fresh 本次 {@code Skill.run} 生成的顶层阶段实例
     */
    public void schedule(SkillInstance si, List<SkillPhaseInstance> fresh) {
        if (fresh == null || fresh.isEmpty()) return;

        SupportProfile profile = si.getContext().getSupportProfile();
        if (profile != null) {
            for (EchoMod mod : profile.echoMods()) {
                List<SkillPhase> targets = select(si, fresh, mod);
                if (targets.isEmpty() || mod.count() <= 0) continue;
                int step = Math.max(1, mod.delayTicks());
                for (int i = 1; i <= mod.count(); i++) {
                    for (SkillPhase phase : targets) {
                        pending.add(new Pending(phase, step * i, mod.areaInc(), mod.areaMoreFor(i)));
                    }
                }
            }
        }

        // 属性通道：整技能重放，无额外延迟（词缀给的额外回响紧跟原施放），无范围载荷。
        int fromStat = statEchoCount(si);
        if (fromStat > 0) {
            List<SkillPhase> all = new ArrayList<>();
            for (SkillPhaseInstance inst : fresh) all.add(inst.getPhase());
            for (int i = 1; i <= fromStat; i++) {
                for (SkillPhase phase : all) pending.add(new Pending(phase, i, 0.0, 1.0));
            }
        }
    }

    /** 每 tick 递减延迟，返回本 tick 到期的重放阶段实例。 */
    public List<SkillPhaseInstance> tick(SkillInstance si) {
        if (pending.isEmpty()) return List.of();
        List<SkillPhaseInstance> out = new ArrayList<>();
        List<Pending> carried = new ArrayList<>();
        for (Pending p : pending) {
            if (p.delay() <= 0) {
                out.addAll(p.phase().repeat(si, p.areaInc(), p.areaMore()));
                recordEchoedSpell(si);
            } else {
                carried.add(new Pending(p.phase(), p.delay() - 1, p.areaInc(), p.areaMore()));
            }
        }
        pending.clear();
        pending.addAll(carried);
        return out;
    }

    /** 是否仍有未到期的回响（供宿主判断技能是否应继续存活）。 */
    public boolean hasPending() {
        return !pending.isEmpty();
    }

    /**
     * 每次回响重放<b>法术</b>时记录 {@link EventName#ECHOED_SPELL}（驱动「per Spell Echoed Recently」）。
     * 原施放不记录——一次回响算一次，故 Echo 3 次的技能每次施放累计 3 条。
     */
    private static void recordEchoedSpell(SkillInstance si) {
        LivingEntity caster = si.getContext().getCaster();
        if (caster == null) return;
        if (!si.getSkill().getStatTags(si.getContext(), si.getSkillId()).contains(GateTag.SPELL)) return;
        Event.record(caster, EventName.ECHOED_SPELL);
    }

    /** 定向选择：本次回响应重放哪些顶层阶段。 */
    private static List<SkillPhase> select(SkillInstance si, List<SkillPhaseInstance> fresh, EchoMod mod) {
        List<SkillPhase> out = new ArrayList<>();
        switch (mod.scope()) {
            case SKILL -> {
                for (SkillPhaseInstance inst : fresh) out.add(inst.getPhase());
            }
            case TAG -> {
                // 技能级 GateTag 定向：仅当技能本次携带该标签（spell / warcry / 技能名 …）
                if (si.getSkill().getStatTags(si.getContext(), si.getSkillId()).contains(mod.tag())) {
                    for (SkillPhaseInstance inst : fresh) out.add(inst.getPhase());
                }
            }
            case PHASE -> {
                int idx = mod.phaseIndex().orElse(0);
                List<SkillPhasePlayer> players = si.getSkill().phases();
                if (idx >= 0 && idx < players.size()) {
                    SkillPhase target = players.get(idx).phase();
                    // 仅当该阶段本次确实被生成（激活条件命中）才重放——不凭空补放未激活的阶段
                    for (SkillPhaseInstance inst : fresh) {
                        if (inst.getPhase() == target) {
                            out.add(target);
                            break;
                        }
                    }
                }
            }
        }
        return out;
    }

    /**
     * 读施法者 {@link AttributeRegistry#ECHO_COUNT}。
     * 调用点已处于 {@code SkillStatScopes.enter} 的技能作用域内，栈顶携带本次技能的全部标签，
     * 故「对 [某标签]/[某技能]」类定向由既有 GateTag 链路天然完成。
     *
     * <p><b>生产源</b>：POE2 语料无「+# 次回响」词缀/天赋（回响次数来源仅 4 个辅助宝石，
     * 已全部走 {@link EchoMod} 数据通道），无装备词缀/天赋生产者 —— 按铁律不自创内容，故本属性恒为 0。
     * <p>stat 通道本身完整：{@code StatIds.ECHO_COUNT} + {@code StatGen} 登记
     * + {@code EFF_ADD_VALUE_ATTRIBUTE_COE_ECHO_COUNT}（ModifierStatEffect ADD_VALUE/GAIN），
     * 经 LivingAttributeValue mixin -&gt; {@code StatEngine.fold} 真读。
     */
    private static int statEchoCount(SkillInstance si) {
        LivingEntity caster = si.getContext().getCaster();
        if (caster == null) return 0;
        return (int) caster.getAttributeValue(AttributeRegistry.ECHO_COUNT);
    }
}
