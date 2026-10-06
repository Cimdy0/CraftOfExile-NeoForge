package com.cimdy.coe.data.codec.support.math.context.nodes.value.combat;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.node.StatusNode;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Locale;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * 条件读值节点「目标被<b>你</b>点燃」（COND_TARGET_IGNITED_BY_YOU，Phase 1 标记系统重构附属基础设施）：
 * 驱动「对被你点燃的敌人 X」一族词缀的真值门控。
 *
 * <p><b>取值口径（与 DamagePipeline#applyStatusDamageTakenModifiers 的 ignite_history_fire_taken 读点同源）</b>：
 * 受害方（{@code scope.root().currentStatContext().sourceEntity()} = 本次事件交互对方）身上的
 * {@link AttachmentRegistry#IGNITE_HISTORY} 附件按施加者（{@code scope.current()} = 当前候选实体，
 * 伤害作用域下即攻击者）UUID 累计点燃秒数；该条目存在且 {@code > 0} 即返回 1.0，配合「求值 &gt;= 1.0」布尔判据。
 *
 * <p>交互对方 / 候选实体为空（非命中 / 未绑定对方的事件作用域）时返回 0——条目安全地不生效，不产生「恒真」误判。
 *
 * <p>本节点无状态（{@code CODEC = MapCodec.unit(...).codec()}），仅作条件谓词，不参与数值/选择器聚合。
 */
public record IgnitedBySourceValue() implements ValueExpr {

    public static final Codec<IgnitedBySourceValue> CODEC = MapCodec.unit(new IgnitedBySourceValue()).codec();

    @Override
    public double evaluate(ValueScope scope) {
        // 受害方 = 本次事件交互对方（如伤害作用域下被承伤的实体）。
        if (scope.root() == null) return 0D;
        LivingEntity victim = StatusNode.interactionOther(scope);
        if (victim == null) return 0D;
        // 施加者 = 当前候选实体（伤害作用域下即攻击者）。
        LivingEntity attacker = scope.current();
        if (attacker == null) return 0D;
        Float secs = victim.getData(AttachmentRegistry.IGNITE_HISTORY).get(attacker.getUUID());
        return (secs != null && secs > 0f) ? 1.0 : 0.0;
    }

    @Override
    public List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.ignited_by_you"));
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(Component.translatable("stat.condition.ignited_by_you"));
    }
}
