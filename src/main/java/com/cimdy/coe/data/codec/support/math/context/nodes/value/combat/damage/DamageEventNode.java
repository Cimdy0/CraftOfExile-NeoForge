package com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.damage;

import com.cimdy.coe.data.codec.support.math.context.node.EventNode;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.event.Event;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 伤害事件读值节点（EventNode 家族成员，从 {@link DamageValue} 的 EVENT 分支抽取）：
 * 读实体单例的事件历史（{@link Event.State}，经 {@link ValueScope#event()} 候选归一解析）。
 * <p>
 * 同源另有 {@code damage_caused.<mode>}（最近一次造成量）、{@code heal.<mode>}（最近一次回复量）、
 * {@code spend.<mode>}（最近一次消耗量）四组 mode 集；{@code mode} 缺省 {@code "actual"}。
 * 对应数据源无记录时一律返回 0。本节点是 {@link EventNode} 子类，仅承载 EVENT 分支语义，
 * {@link DamageValue} 经 {@code new DamageEventNode(mode).evaluate(scope)} 委派到此。
 */
public record DamageEventNode(String mode) implements EventNode {

    public static final Codec<DamageEventNode> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("mode", "actual").forGetter(DamageEventNode::mode)
            ).apply(instance, DamageEventNode::new));

    @Override
    public double evaluate(Event.State state, ValueScope scope) {
        if (state == null) return 0D;
        // caused 侧（damage_caused.<mode>）：读攻击者最近一次「造成」量（Event.State.lastDamageCaused*）。
        // 与 taken 侧对称，闭合「攻击者 fire 读到自身承受历史」的读源错位
        if (mode.startsWith("caused.")) {
            return evalResourceSnapshot(mode.substring("caused.".length()),
                    state.lastDamageCausedResource(), 0f,
                    state.lastDamageCausedActual(), state.lastDamageCausedOverflow(), 0f);
        }
        // heal 侧（heal.<mode>）：读最近一次回复量（lastHeal*）
        if (mode.startsWith("heal.")) {
            return evalResourceSnapshot(mode.substring("heal.".length()),
                    state.lastHealResource(), state.lastHealRequested(),
                    state.lastHealActual(), state.lastHealOverflow(), 0f);
        }
        // spend 侧（spend.<mode>）：读最近一次消耗量（lastSpend*）
        if (mode.startsWith("spend.")) {
            return evalResourceSnapshot(mode.substring("spend.".length()),
                    state.lastSpendResource(), state.lastSpendRequested(),
                    state.lastSpendActual(), 0f, state.lastSpendMissing());
        }
        if (mode.equals("actual")) return state.lastDamageTakenActual();
        if (mode.equals("overflow")) return state.lastDamageTakenOverflow();
        if (mode.startsWith("resource:")) {
            return state.lastDamageTakenResource().equals(mode.substring("resource:".length())) ? 1D : 0D;
        }
        return 0D;
    }

    /** 数值快照 mode 求值：requested / actual / overflow / missing / resource:<r>（按快照字段形态取数）。 */
    private static double evalResourceSnapshot(String m, String resource, float requested,
                                               float actual, float overflow, float missing) {
        if (m.equals("requested")) return requested;
        if (m.equals("actual")) return actual;
        if (m.equals("overflow")) return overflow;
        if (m.equals("missing")) return missing;
        if (m.startsWith("resource:")) {
            return resource.equals(m.substring("resource:".length())) ? 1D : 0D;
        }
        return 0D;
    }
}
