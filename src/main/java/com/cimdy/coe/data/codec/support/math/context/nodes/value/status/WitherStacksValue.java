package com.cimdy.coe.data.codec.support.math.context.nodes.value.status;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.AilmentNode;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

/**
 * 当前实体身上 Wither（凋零）状态的<b>层数</b>读值节点：驱动「for each stack of Wither on Enemy」一族线性缩放
 * （PoE2 实证：Wither 每层使目标承受混沌伤害 +6%，上限 10 层；与 {@link StatusDefs#WITHER} 的
 * {@code maxStacks(10)} 一致）。
 *
 * <p><b>取值实体</b>：本节点作用于当前候选实体（{@link AilmentNode} 继承 {@link StatusNode} 的
 * {@code current} 语义），读取其自身 WITHER 层数；与 {@code ElementalAilmentTypeCountValue} 读
 * 交互对方不同，凋零层数按「自身持有」口径。JSON 形式 {@code {"type":"wither_stacks"}}。
 */
public record WitherStacksValue() implements AilmentNode {

    public static final Codec<WitherStacksValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    // 无参数节点：保留一个 optional 哑字段以满足 DFU 10 RecordCodecBuilder（同 CharmActiveValue / ElementalAilmentTypeCountValue 空壳模式）
                    com.mojang.serialization.Codec.STRING.optionalFieldOf("kind", "any").forGetter(c -> "any")
            ).apply(instance, ignore -> new WitherStacksValue()));

    @Override
    public double evaluate(LivingEntity current, ValueScope scope) {
        return StatusEngine.stacks(current, StatusDefs.WITHER);
    }
}
