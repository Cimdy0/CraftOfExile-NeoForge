package com.cimdy.coe.data.codec.support.math.context.nodes.value.combat;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.node.StatusNode;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Locale;

/**
 * 「被你点燃过的敌人」递增承火伤——<b>幅度值源</b>（原 {@code ignite_history_fire_taken} 效果的归位形态）。
 *
 * <p><b>为何是值节点而非效果</b>：本条目不执行任何动作，它只是伤害结算的一个幅度公式
 * （{@code min(累计点燃秒数 * rate, cap)}），按本模组的表达式代数属「值源」而非「效果」——
 * 效果是经通道派发的行为。故幅度参数 {@code rate}/{@code cap} 由本节点 CODEC 随表达式数据驱动给出，
 * 载体退化为 {@link com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect}（纯身份标记，
 * 消费端 {@code StatEngine#magnitudeOf} 按 kind 折叠各条目 amount）。
 *
 * <p><b>取值口径</b>（与 {@link IgnitedBySourceValue} 同源）：受害方 = 本次事件交互对方
 * （{@code scope.root().currentStatContext().sourceEntity()}），施加者 = 当前候选实体
 * （{@code scope.current()}，伤害作用域下即攻击者）；秒数取自受害方
 * {@link AttachmentRegistry#IGNITE_HISTORY} 附件中按施加者 UUID 累计的点燃时长。
 *
 * <p>缺省 {@code rate=0.01}（每秒 +1%）、{@code cap=0.10}（封顶 +10%），与迁移前
 * {@code DamagePipeline} 的硬编码常量同值，故接线数值不变。
 *
 * <p>JSON 全量形式 {@code {"type":"ignite_history_fire_taken","rate":0.01,"cap":0.1}}；
 * 中缀简写 {@code ignite_history_fire_taken[:<rate>_<cap>]}（两参数均取缺省时省略后缀，
 * 见 {@code ValueExprTypes} 注册处）。
 */
public record IgniteHistoryFireValue(double rate, double cap) implements ValueExpr {

    /** 缺省每秒递增比例（0.01 = 每秒 +1%）。 */
    public static final double DEFAULT_RATE = 0.01;
    /** 缺省递增上限（0.10 = 封顶 +10%）。 */
    public static final double DEFAULT_CAP = 0.10;

    public static final Codec<IgniteHistoryFireValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.optionalFieldOf("rate", DEFAULT_RATE).forGetter(IgniteHistoryFireValue::rate),
                    Codec.DOUBLE.optionalFieldOf("cap", DEFAULT_CAP).forGetter(IgniteHistoryFireValue::cap)
            ).apply(instance, IgniteHistoryFireValue::new));

    /** 缺省参数构造（供中缀简写 {@code ignite_history_fire_taken} 反解）。 */
    public IgniteHistoryFireValue() {
        this(DEFAULT_RATE, DEFAULT_CAP);
    }

    @Override
    public double evaluate(ValueScope scope) {
        if (scope == null || scope.root() == null) return 0D;
        // 受害方 = 本次事件交互对方（伤害作用域下即承伤的实体）。
        LivingEntity victim = StatusNode.interactionOther(scope);
        if (victim == null) return 0D;
        // 施加者 = 当前候选实体（伤害作用域下即攻击者）。
        LivingEntity attacker = scope.current();
        if (attacker == null) return 0D;
        Float secs = victim.getData(AttachmentRegistry.IGNITE_HISTORY).get(attacker.getUUID());
        if (secs == null || secs <= 0f) return 0D;
        return Math.min(secs * rate, cap);
    }

    /** 名词短语：复用既有值键 {@code stat.value.marker.ignite_history_fire_taken}（en = Ignite History Fire Taken）。 */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("marker", "ignite_history_fire_taken"));
    }
}
