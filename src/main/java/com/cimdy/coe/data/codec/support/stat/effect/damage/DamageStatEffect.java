package com.cimdy.coe.data.codec.support.stat.effect.damage;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;

import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.describer.StatAmountDescriber;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.helper.status.EffectApplier;
import com.cimdy.coe.event.TriggerContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 触发亚型：对目标造成伤害 —— 「击中使目标损失它自身最大生命值的 5%」等。
 * <p>
 * 本类<b>没有任何字段</b>（除基类的事件标签）：伤害量就是条目（{@link StatEntry}）的唯一数值。
 * <p>
 * 数据示例（击中目标损失其自身最大生命 5%）：
 * <pre>{@code
 * {
 *   "type": "damage",
 *   "amount": {"math": "*", "args": [{"type": "attribute", "attribute": "max_health"}, 0.05]},
 *   "value_target": "trigger_target",
 *   "target": "trigger_target",
 *   "tags": ["hit"]
 * }}</pre>
 * 此例「被判定实体 / 被取值实体 / 被作用实体」三者同指事件目标（{@code trigger_target}），
 * 但三阶段各自通过条目 {@link StatTargets} 的钥匙经 {@link StatEvalContext} 取同一实体
 * ——无需在三处重复写实体查找逻辑。
 * <p>
 * 缺省时默认「作用于事件目标、按事件目标自身属性取值」（见 {@link #defaultTargets()}）。
 * <p>
 * <b>符号语义</b>：正数 = 对目标造成伤害；<b>负数 = 对目标回复生命</b>（与
 * {@code heal} 效果对称——「对目标生命」的正负号统一：正值扣生命、负值加生命），
 * 零 = 无效果。例如 {@code {"type":"damage","amount":-50,"tags":["hit"]}} 对命中目标治疗 50 点生命。
 * <p>
 * <b>求值期一对一拆分</b>（击中使被击中实体附近每个敌人损失<b>其自身</b>最大生命 5%）：
 * {@code amount} 用 EACH 选择器——以 {@code value_target=trigger_target} 为参照（{@code dist()}
 * 即以被击中实体为中心），逐候选在其作用域内求 {@code then}（该候选自身 max_health × 5%），
 * 求值过程经 {@link com.cimdy.coe.data.codec.support.math.context.query.SplitSink} 逐对发射
 * {@code (候选实体, 值)}；施加阶段对每对单独作用——目标即被求值实体本身，无需钥匙：
 * <pre>{@code
 * {
 *   "type": "damage",
 *   "amount": {"type": "selector", "mode": "each",
 *              "where": {"math": "<=", "args": [{"type": "dist"}, 8]},
 *              "then": {"math": "*", "args": [{"type": "attribute", "attribute": "max_health"}, 0.05]}},
 *   "value_target": "trigger_target",
 *   "tags": ["hit"]
 * }}</pre>
 */
public class DamageStatEffect extends AbstractStatEffect {

    /** 规范构造：仅事件标签。 */
    public DamageStatEffect(GateExpr tags) {
        super(tags);
    }

    /** 便捷构造：常量 / 表达式伤害量，产出一条完整条目。 */
    public static StatRead entry(ExpressionCodec.Value amount, Set<String> tags, List<ValueExpr> conditions) {
        return StatEntry.of(amount, conditions, new DamageStatEffect(GateExpr.of(tags)));
    }

    public static final MapCodec<DamageStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, DamageStatEffect::new));

    @Override
    public String type() {
        return "damage";
    }

    /** 伤害亚型默认「作用于事件目标、按事件目标自身属性取值」。 */
    @Override
    public StatTargets defaultTargets() {
        return StatTargets.of(TargetSelector.EVENT_TARGET, TargetSelector.EVENT_TARGET);
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        // 唯一数值统一取数：按 valueTarget 钥匙取实体（本例=事件目标）读取其属性（如自身最大生命）；
        // amount 内含 EACH 选择器时，求值期已逐候选拆分为 (实体, 值) 对——保留实体↔值对应关系
        List<StatEntry.AmountSplit> splits = entry.evaluateAmountSplit(evalCtx, scope);
        if (splits.isEmpty()) return;
        StatEntry.AmountSplit single = splits.size() == 1 ? splits.getFirst() : null;
        if (single != null && single.receiver() == null) {
            // 无拆分（普通表达式 / 常量）：单值路径，按 target 钥匙施加
            double amt = single.value();
            if (amt == 0) return;
            entry.forEachTarget(evalCtx, scope, recipient -> {
                if (!recipient.isAlive()) return;
                if (amt > 0) EffectApplier.damage(recipient, evalCtx.owner(), (float) amt, entry.tags());
                else EffectApplier.heal(recipient, ResourceType.HEALTH, (float) -amt);   // 负数 = 治疗目标
            });
            return;
        }
        // 求值期一对一拆分：每个被求值实体单独承受「以它自身算出的值」
        for (StatEntry.AmountSplit split : splits) {
            LivingEntity recipient = split.receiver();
            if (recipient == null || !recipient.isAlive() || split.value() == 0) continue;
            double v = split.value();
            if (v > 0) EffectApplier.damage(recipient, evalCtx.owner(), (float) v, entry.tags());
            else EffectApplier.heal(recipient, ResourceType.HEALTH, (float) -v);         // 负数 = 治疗目标
        }
    }

    @Override
    public List<Component> describeFragment(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    /**
     * 本地化效果片段（P3）：常量 / 未识别表达式沿用旧 "deal %s"（与旧 describe 逐字一致，零回归）；
     * 表达式且能识别为 per-X 时，渲染 "deal X per N 来源"（deal %s + per 子句）。
     */
    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        Optional<Double> constant = entry.amount().value().left();
        if (constant.isPresent()) {
            // stat_effect.deal = "deal %s"，参数：数值（String.valueOf 与旧 describe 逐字一致）。
            return List.of(Component.translatable("stat_effect.deal",
                    Component.literal(String.valueOf(entry.amount()))));
        }
        // 表达式：尝试 per-X 反汇编（deal %s + per 子句）。
        if (entry.amount().value().right().isPresent()) {
            StatAmountDescriber.PerXInfo per =
                    StatAmountDescriber.detectPerX(entry.amount().value().right().get());
            if (per != null) {
                StatAmountDescriber.PerXInfo info = StatAmountDescriber.resolveValues(per, entry.params());
                return List.of(StatAmountDescriber.describeDamage(info, locale));
            }
        }
        // 回落：与旧 describe 逐字一致。
        return List.of(Component.translatable("stat_effect.deal",
                Component.literal(String.valueOf(entry.amount()))));
    }

    /** UI 展示文本（兼容旧调用点，统一经 {@link #describeFragment}）。 */
    @Override
    public List<Component> describe(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DamageStatEffect that)) return false;
        return Objects.equals(tags(), that.tags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(DamageStatEffect.class, tags());
    }

    @Override
    public String toString() {
        return "DamageStatEffect[tags=" + tags() + "]";
    }
}
