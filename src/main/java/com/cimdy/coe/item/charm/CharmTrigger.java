package com.cimdy.coe.item.charm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;

/**
 * Charm 触发条件（POE2 实证：护符在<b>特定条件被满足时自动激活</b>，不靠手动按键——
 * 这是与 Flask（手动按键）最根本的区别）。
 * <ul>
 *   <li>元素受击：{@code FIRE / COLD / LIGHTNING / CHAOS}（Ruby/Sapphire/Topaz/Amethyst）；</li>
 *   <li>异常/控制施加：{@code IGNITE / FREEZE / SHOCK / BLEED / POISON / STUN / SLOW}
 *       （Dousing/Thawing/Grounding/Staunching/Antidote/Stone/Silver）；</li>
 *   <li>击杀稀有/独特：{@code KILL_RARE_UNIQUE}（Golden）。</li>
 * </ul>
 * 触发器只描述「发生了什么」，具体的「授予什么效果」完全由 {@link com.cimdy.coe.item.charm.CharmItem#effects}（原引用的 {@code CharmItem#effectStatus} 字段已删除） 承载，
 * 经中央 {@code StatusEngine.apply} 落地（原则①：回调只记事件，Stat 读 context 中央入口）。
 */
public enum CharmTrigger {
    FIRE, COLD, LIGHTNING, CHAOS,
    IGNITE, FREEZE, SHOCK, BLEED, POISON, STUN, SLOW,
    KILL_RARE_UNIQUE;

    /** 数据驱动编解码（名称字符串 ↔ 枚举，大小写不敏感），供 {@code CharmState.CODEC} 复用。 */
    public static final Codec<CharmTrigger> CODEC = Codec.STRING.comapFlatMap(
            s -> Arrays.stream(CharmTrigger.values())
                    .filter(v -> v.name().equalsIgnoreCase(s))
                    .findFirst()
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown charm trigger: " + s)),
            CharmTrigger::name);
}
