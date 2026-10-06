package com.cimdy.coe.data.codec.skill.modifier.impl.control;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area.AreaDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.modifier.SkillModifierTypes;

import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;

import com.cimdy.coe.data.codec.skill.helper.state.EchoState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * 回响效果 — 让技能（或其某个阶段）在施放后<b>重复一次效果</b>。技能与辅助宝石共用本类。
 *
 * <p>语义严格对齐 POE2 官方定义（poe2wiki「Spell Echo Support」 Mechanics）：
 * <blockquote>
 * A repeat is part of the original cast. It does not require a cost or a cooldown,
 * normally has the same critical strike roll, and does not cause <i>when you cast</i>
 * effects to occur a second time.
 * </blockquote>
 * 因此重放只重做「效果」部分（投递物 / 范围 / 光束 …），
 * <b>不重放</b> ON_PRE_CAST / ON_CAST / ON_PERFECT_TIMING 与事件监听注册
 * （见 {@code SkillPhase#repeat}）：不二次消耗、不二次冷却、不二次触发「施放时」效果。
 *
 * <p><b>定向（scope）</b>决定「重复什么」：
 * <ul>
 *   <li>{@code skill} —— 整个技能：重放全部顶层阶段（如 Echoing Cry「Supported Skills Echo 2 times」）。</li>
 *   <li>{@code tag} —— 按技能标签定向：仅当技能携带 {@code target} 标签时重放（如 Spell Echo → {@code spell}）。</li>
 *   <li>{@code phase} —— 按顶层阶段序号定向：只重放第 {@code phase_index} 个顶层阶段。</li>
 * </ul>
 *
 * <p><b>延迟（delay_ticks）</b>：每次回响相对上一次的间隔（tick）。
 * POE2 官方数值：Spell Echo / Zarokh's Refrain 的 {@code support_spell_echo_cascade_delay_ms = 500}
 * = <b>10 tick</b>；Echoing Cry 明确「1.3 second delay」= <b>26 tick</b>。
 *
 * <p><b>回响专属范围载荷</b>（二者皆<b>只作用于重放那一次</b>，不串到原施放）：
 * <ul>
 *   <li>{@code area_inc} —— POE2「Echoes from Supported Spells have <b>40% increased</b>
 *       Area of Effect」（Spell Echo / Zarokh's Refrain 同为 0.40）。
 *       <b>increased = 加法</b>，与 {@code EFFECT_AREA} 属性（基 1.0，inc 走 ADD_MULTIPLIED_BASE）
 *       <b>相加</b>后再乘半径，故与其他「increased 范围」来源正确叠加，而非各自相乘。</li>
 *   <li>{@code area_more_per_echo} —— POE2「Supported Skills have <b>100% more</b> area of effect
 *       <b>per time they have Echoed</b>」（Echoing Cry = 1.00）。
 *       <b>more = 乘法</b>，且<b>按回响序号递增</b>：第 i 次回响倍率 {@code 1 + area_more_per_echo * i}
 *       （Echoing Cry 两次回响 → ×2、×3；原施放视作已回响 0 次 → ×1）。</li>
 * </ul>
 * 落点：{@code EchoState} 按回响序号算出这两个值 → {@code SkillPhase#repeat} 写入
 * {@code SkillPhaseInstance} → {@code AreaDeliveryExecutor} 读并缩放半径（R3 真读）。
 *
 * <p>示例：
 * <pre>
 * // 法术回响（Spell Echo）：1 次、500ms 延迟、回响 +40% increased 范围
 * { "coe:echo_mod": { "count":1, "delay_ticks":10, "scope":"tag", "target":"spell", "area_inc":0.40 } }
 * // 泽洛克的克制：回响 3 次、500ms 延迟、回响 +40% increased 范围
 * { "coe:echo_mod": { "count":3, "delay_ticks":10, "scope":"tag", "target":"spell", "area_inc":0.40 } }
 * // 回响战吼：全技能回响 2 次、1.3 秒间隔、每次回响 +100% more 范围（×2 → ×3）
 * { "coe:echo_mod": { "count":2, "delay_ticks":26, "scope":"skill", "area_more_per_echo":1.0 } }
 * // 只让第二段劈砍重复一次
 * { "coe:echo_mod": { "count":1, "scope":"phase", "phase_index":1 } }
 * </pre>
 */
public record EchoMod(
        int count,
        int delayTicks,
        Scope scope,
        Optional<String> target,
        Optional<Integer> phaseIndex,
        double areaInc,
        double areaMorePerEcho
) implements SkillModifier {

    /** 回响定向维度。 */
    public enum Scope {
        /** 整个技能：重放全部顶层阶段。 */
        SKILL("skill"),
        /** 按技能标签定向：技能携带 {@code target} 标签才回响。 */
        TAG("tag"),
        /** 按顶层阶段序号定向：只回响第 {@code phase_index} 个阶段。 */
        PHASE("phase");

        public static final Codec<Scope> CODEC = Codec.STRING.xmap(Scope::from, Scope::id);

        private final String id;

        Scope(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        /** 未知取值回退 {@link #SKILL}（数据容错，避免单条坏数据阻断整颗技能树解析）。 */
        public static Scope from(String id) {
            for (Scope value : values()) {
                if (value.id.equals(id)) return value;
            }
            return SKILL;
        }
    }

    public static final Codec<EchoMod> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("count", 1).forGetter(EchoMod::count),
            Codec.INT.optionalFieldOf("delay_ticks", 0).forGetter(EchoMod::delayTicks),
            Scope.CODEC.optionalFieldOf("scope", Scope.SKILL).forGetter(EchoMod::scope),
            Codec.STRING.optionalFieldOf("target").forGetter(EchoMod::target),
            Codec.INT.optionalFieldOf("phase_index").forGetter(EchoMod::phaseIndex),
            Codec.DOUBLE.optionalFieldOf("area_inc", 0.0).forGetter(EchoMod::areaInc),
            Codec.DOUBLE.optionalFieldOf("area_more_per_echo", 0.0).forGetter(EchoMod::areaMorePerEcho)
    ).apply(i, EchoMod::new));

    /** 整技能重复若干次（无延迟、无范围载荷）。 */
    public EchoMod(int count) {
        this(count, 0, Scope.SKILL, Optional.empty(), Optional.empty(), 0.0, 0.0);
    }

    /** 按标签定向重复若干次（无范围载荷）。 */
    public EchoMod(int count, int delayTicks, String tag) {
        this(count, delayTicks, Scope.TAG, Optional.of(tag), Optional.empty(), 0.0, 0.0);
    }

    /**
     * 第 {@code echoIndex} 次回响（<b>1-based</b>）的<b>乘法</b>范围倍率。
     *
     * <p>POE2「100% more area of effect per time they have Echoed」：
     * 第 1 次回响视作「已回响 1 次」→ {@code 1 + 1.00 * 1 = 2.0}；第 2 次 → {@code 3.0}。
     * 原施放（index 0）→ {@code 1.0}，即不受影响。
     */
    public double areaMoreFor(int echoIndex) {
        return 1.0 + areaMorePerEcho * Math.max(0, echoIndex);
    }

    /** 本次回响的目标标签（仅 {@code scope == TAG} 有意义）。 */
    public String tag() {
        return target.orElse("");
    }

    @Override public SkillModifier.Type<?> getType() { return SkillModifierTypes.ECHO_MOD; }
    @Override public String descriptionKey() { return "skill.coe.echo_mod"; }
}
