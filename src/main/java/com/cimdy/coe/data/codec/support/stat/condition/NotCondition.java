package com.cimdy.coe.data.codec.support.stat.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * 补集节点（{@code type:"not"}）：入参集减去子条件解析结果。
 * <p>
 * <b>递归</b>：{@code inner} 的类型即 {@code Codec.recursive} 的自引用 {@code self}。
 * 补集<b>相对入参集</b>（{@link ConditionScope#base()}）求值 —— 与布尔取反不同，
 * 「不满足某判断的实体」必须有一个论域，论域即入参集。
 *
 * <pre>
 * {"type":"not","inner":{"type":"relation","relation":"minion"}}
 * </pre>
 */
public record NotCondition(StatCondition inner) implements StatCondition {

    public static Codec<NotCondition> codec(Codec<StatCondition> self) {
        return RecordCodecBuilder.create(instance -> instance.group(
                self.fieldOf("inner").forGetter(NotCondition::inner)
        ).apply(instance, NotCondition::new));
    }

    @Override
    public List<LivingEntity> resolve(ConditionScope scope) {
        if (inner == null) return scope.base();
        return ConditionSets.subtract(scope.base(), inner.resolve(scope));
    }
}
