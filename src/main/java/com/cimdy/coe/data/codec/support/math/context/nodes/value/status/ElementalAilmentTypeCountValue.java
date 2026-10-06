package com.cimdy.coe.data.codec.support.math.context.nodes.value.status;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.CharmActiveValue;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;

import com.cimdy.coe.data.codec.support.math.context.node.AilmentNode;
import com.cimdy.coe.data.codec.support.math.context.node.StatusNode;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * 目标身上<b>不同元素异常种类的数量</b>读值节点：驱动「for each type of Elemental Ailment on Enemy」一族线性缩放
 * （如 Yoke of Suffering「Enemies take (15—20)% increased Damage for each Elemental Ailment type among your
 * Ailments on them」；poe2db 实证 Elemental Ailments = Ignite / Chill / Freeze / Shock / Electrocute 共 5 种，
 * 与模块 {@link AilmentStatusTypes} 中携带 {@code GateTag.ELEMENTAL} 的成员一一对应）。
 *
 * <p><b>取值实体</b>：本节点服务于 onTarget 条目（effect 落持有者自身、数值在攻击者作用域求值），
 * 故「目标」经 {@link com.cimdy.coe.data.codec.support.stat.engine.StatContext#sourceEntity()} 取交互对方
 * （命中结算时攻击者作用域持受害者，与 {@code BindExpr(SOURCE)} 同源）。
 * 非命中场景交互对方为 {@code null} → 返回 0，条目安全不生效（与既有跨实体条件兜底语义一致）。
 *
 * <p><b>口径</b>：仅统计携带 {@code GateTag.ELEMENTAL} 的异常（ignite / chill / freeze / shock / electrocute），
 * 不含物理 / 混沌类异常（bleed / poison）；同种状态多次施加不重复计，返回不同种类数（0—5）。
 * 逐成员以 {@link StatusEngine#has} 判定（零枚举 / 零反射），R3 逐节点可由 {@code StatusEngine.apply} 真施加对照。
 *
 * <p>JSON 形式 {@code {"type":"elemental_ailment_type_count"}}；中缀 {@code elemental_ailment_type_count}。
 */
public record ElementalAilmentTypeCountValue() implements AilmentNode {

    public static final Codec<ElementalAilmentTypeCountValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    // 无参数节点：保留一个 optional 哑字段以满足 DFU 10 RecordCodecBuilder（同 CharmActiveValue / StatusValue 空壳模式）
                    com.mojang.serialization.Codec.STRING.optionalFieldOf("kind", "any").forGetter(c -> "any")
            ).apply(instance, ignore -> new ElementalAilmentTypeCountValue()));

    @Override
    public double evaluate(LivingEntity current, ValueScope scope) {
        LivingEntity target = StatusNode.interactionOther(scope);
        if (target == null) return 0D;
        int count = 0;
        for (StatusType t : StatusType.all()) {
            if (t.tags().contains(GateTag.ELEMENTAL) && StatusEngine.has(target, t)) count++;
        }
        return count;
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("elemental_ailment_type_count", "types"));
    }
}
