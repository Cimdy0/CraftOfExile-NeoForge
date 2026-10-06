package com.cimdy.coe.data.codec.support.stat.effect.resource;

import com.cimdy.coe.data.codec.skill.phase.action.combo.ComboAction;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffectTypes;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.helper.status.EffectApplier;
import com.cimdy.coe.event.TriggerContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import com.cimdy.coe.data.codec.support.stat.entry.describer.StatNumberFormat;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * 触发亚型：资源即时结算 —— 任意事件上的<b>即时回复（HEAL，缺省）或即时扣除（COST）</b>。
 * 复用 {@link EffectApplier#heal} / {@link EffectApplier#cost} 原语（对称双向）。
 * <p>
 * 唯一身份字段 {@code resource} = 资源类型（限角色级：生命/魔力/能量/怒气）；{@code direction} =
 * 施加方向；<b>数值（量）是条目（{@link StatEntry}）的唯一数值</b>，本类不持有任何数值。
 * 需要「最大生命与最大魔力之和的 5%」这类计算时写进 math 表达式即可，例：
 * {@code {"type":"heal","resource":"health","amount":"(attribute:coe:max_health + attribute:coe:max_mana) * 0.05","tags":["kill"]}}。
 * <p>
 * 简单示例：
 * <ul>
 *   <li>击杀回血 5 点：{@code {"type":"heal","resource":"health","amount":5.0,"tags":["kill"]}}</li>
 *   <li><b>受击即时回能量</b>（即时通道，区别于 recoup 的 8 秒延迟补给）：
 *       {@code {"type":"heal","resource":"energy","amount":"(attribute:coe:max_energy) * 0.05","tags":["taken_hit"]}}</li>
 *   <li>每次攻击扣除 5 生命（血魔法式即时扣费）：{@code {"type":"heal","resource":"health","direction":"cost","amount":5.0,"tags":["attack"]}}</li>
 * </ul>
 *
 * <p><b>事件任意</b>：触发由条目 {@code tags}（标签布尔表达式）限定，任何事件（击杀/击中/受击/暴击/施放/攻击起手…）皆可。
 * <b>资源限制</b>：仅角色级直接结算资源（{@link #VALID_RESOURCES}）；技能级资源（连击/荣耀）须绑定单技能上下文
 * （{@code ComboAction}/{@code ResourceRegistry.GlorySave} 按技能 id 累积），本通道无技能实例，构造器校验拒绝（加载期显式暴露）。
 *
 * <p>作用于谁由条目的 {@code target} 决定（默认效果持有者自身）。
 */
public class ResourceStatEffect extends AbstractStatEffect {

    /** 合法恢复/扣除资源：EffectApplier.heal/cost 实际支持的角色级资源（HEAL 与 COST 同集合）。 */
    public static final Set<ResourceType> VALID_RESOURCES = Set.of(
            ResourceType.HEALTH, ResourceType.MANA, ResourceType.ENERGY, ResourceType.RAGE);

    /** 施加方向：HEAL 即时回复（缺省）/ COST 即时扣除。 */
    public enum Direction implements net.minecraft.util.StringRepresentable {
        HEAL("heal"),
        COST("cost");

        public static final com.mojang.serialization.Codec<Direction> CODEC =
                net.minecraft.util.StringRepresentable.fromEnum(Direction::values);

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    private final ResourceType resource;
    private final Direction direction;
    private final boolean overflow;

    /** 规范构造：身份（资源类型）+ 方向（回复/扣除）+ 是否可溢出上限 + 事件标签（布尔表达式）。 */
    public ResourceStatEffect(ResourceType resource, Direction direction, boolean overflow, GateExpr tags) {
        super(tags);
        if (!VALID_RESOURCES.contains(resource)) {
            throw new IllegalArgumentException("ResourceStatEffect.resource 仅支持角色级资源 "
                    + VALID_RESOURCES + "（技能级资源 combo/glory 须经技能上下文、spirit 为保留资源），收到: " + resource);
        }
        this.resource = resource;
        this.direction = direction == null ? Direction.HEAL : direction;
        this.overflow = overflow;
    }
    /** 无溢出便捷构造。 */
    public ResourceStatEffect(ResourceType resource, Direction direction, GateExpr tags) {
        this(resource, direction, false, tags);
    }
    /** 无事件标签门控（{@link GateExpr#ALWAYS}）便捷构造。 */
    public ResourceStatEffect(ResourceType resource, Direction direction) {
        this(resource, direction, false, GateExpr.ALWAYS);
    }

    public ResourceType resource() {
        return resource;
    }

    /** 施加方向（HEAL 即时回复 / COST 即时扣除）。 */
    public Direction direction() {
        return direction;
    }

    /** 是否允许回复超过资源上限（真值「Mana Remnants can Overflow maximum Mana」；仅对 mana 生效）。 */
    public boolean overflow() {
        return overflow;
    }

    /** 便捷构造：常量 / 表达式恢复量，产出一条完整条目（作用于自身，HEAL 方向，不溢出）。 */
    public static StatEntry entry(ResourceType resource, ExpressionCodec.Value amount,
                                  Set<String> tags, List<ValueExpr> conditions) {
        return StatEntry.of(amount, conditions, new ResourceStatEffect(resource, Direction.HEAL, false, GateExpr.of(tags)));
    }

    /** 便捷构造：指定方向（HEAL 即时回复 / COST 即时扣除，不溢出）。 */
    public static StatEntry entry(ResourceType resource, Direction direction, ExpressionCodec.Value amount,
                                  Set<String> tags, List<ValueExpr> conditions) {
        return StatEntry.of(amount, conditions, new ResourceStatEffect(resource, direction, false, GateExpr.of(tags)));
    }

    /** 便捷构造：指定方向 + 是否可溢出上限（仅对 mana 生效，真值「can Overflow maximum Mana」）。 */
    public static StatEntry entry(ResourceType resource, Direction direction, boolean overflow, ExpressionCodec.Value amount,
                                  Set<String> tags, List<ValueExpr> conditions) {
        return StatEntry.of(amount, conditions, new ResourceStatEffect(resource, direction, overflow, GateExpr.of(tags)));
    }

    public static final MapCodec<ResourceStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceType.CODEC.fieldOf("resource").forGetter(ResourceStatEffect::resource),
            Direction.CODEC.optionalFieldOf("direction", Direction.HEAL).forGetter(ResourceStatEffect::direction),
            Codec.BOOL.optionalFieldOf("overflow", false).forGetter(ResourceStatEffect::overflow),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, ResourceStatEffect::new));

    /** 与 {@code StatEffectTypes} 的注册 id 一致（"heal"），保证序列化往返。 */
    @Override
    public String type() {
        return "heal";
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        // 唯一数值统一取数（按 valueTarget 取被取值实体；算式内 EACH 会在求值期一对一拆分）
        List<StatEntry.AmountSplit> splits = entry.evaluateAmountSplit(evalCtx, scope);
        if (splits.isEmpty()) return;
        StatEntry.AmountSplit single = splits.size() == 1 ? splits.getFirst() : null;
        if (single != null && single.receiver() == null) {
            float amt = (float) single.value();
            if (amt == 0) return;
            entry.forEachTarget(evalCtx, scope, recipient ->
                    apply(recipient, evalCtx.owner(), amt));
            return;
        }
        for (StatEntry.AmountSplit split : splits) {
            if (split.receiver() != null && split.value() != 0) {
                apply(split.receiver(), evalCtx.owner(), (float) split.value());
            }
        }
    }

    /** 按符号 + 方向施加：正数按声明方向（HEAL 回复 / COST 扣除），<b>负数 = 反方向</b>
     *  （HEAL 负数 = 扣除，COST 负数 = 回复），零 = 无效果。 */
    private void apply(LivingEntity recipient, LivingEntity owner, float amount) {
        boolean cost = direction == Direction.COST;
        if (amount < 0) cost = !cost;   // 负数 = 反方向（符号优先于 direction 字段）
        float v = Math.abs(amount);
        if (cost) {
            EffectApplier.cost(recipient, resource, v, owner);
        } else {
            EffectApplier.heal(recipient, resource, v, overflow);
        }
    }

    @Override
    public List<Component> describe(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    @Override
    public List<Component> describeFragment(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    /**
     * 本地化效果片段：{@code +5.0 Health} / {@code -5.0 Health} → 模板 {@code stat_effect.heal} / {@code stat_effect.cost}
     * + 资源组件（{@code ResourceType#getComponent} 已用 {@code stat.<resource>} 自带译文）。
     * 数值走 {@link StatNumberFormat}，符号由模板承载（与旧 describe 逐字一致）。
     */
    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        double v = entry.amount().getValue();
        boolean cost = direction == Direction.COST;
        if (v < 0) cost = !cost;        // 负数 = 反方向（与施加逻辑一致）
        Component amount = resource.getComponent(Math.abs(v));
        return List.of(Component.translatable(cost ? "stat_effect.cost" : "stat_effect.heal", amount));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceStatEffect that)) return false;
        return resource == that.resource && direction == that.direction && Objects.equals(tags(), that.tags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, direction, tags());
    }

    @Override
    public String toString() {
        return "ResourceStatEffect[" + resource + "," + direction + ", tags=" + tags() + "]";
    }
}
