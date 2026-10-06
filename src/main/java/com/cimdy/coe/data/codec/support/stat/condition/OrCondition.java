package com.cimdy.coe.data.codec.support.stat.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 并集节点（{@code type:"or"}）：各子条件解析结果的并集。
 * <p>
 * <b>递归</b>：{@code parts} 的类型即 {@code Codec.recursive} 的自引用 {@code self}。
 * 空 parts = 入参集（恒等）。
 *
 * <pre>
 * {"type":"or","parts":[{"type":"anchor","anchor":"killed"},
 *                       {"type":"anchor","anchor":"hit"}]}
 * </pre>
 */
public record OrCondition(List<StatCondition> parts) implements StatCondition {

    public static Codec<OrCondition> codec(Codec<StatCondition> self) {
        return RecordCodecBuilder.create(instance -> instance.group(
                self.listOf().fieldOf("parts").forGetter(OrCondition::parts)
        ).apply(instance, OrCondition::new));
    }

    @Override
    public List<LivingEntity> resolve(ConditionScope scope) {
        if (parts == null || parts.isEmpty()) return scope.base();
        List<LivingEntity> acc = new ArrayList<>();
        for (StatCondition part : parts) {
            acc = ConditionSets.union(acc, part.resolve(scope));
        }
        return acc;
    }
}
