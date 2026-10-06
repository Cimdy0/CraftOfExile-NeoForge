package com.cimdy.coe.data.codec.support.stat.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * 交集节点（{@code type:"and"}）：各子条件解析结果的交集。
 * <p>
 * <b>递归</b>：{@code parts} 的类型即 {@code Codec.recursive} 的自引用 {@code self}，
 * 子条件可以是任意节点（含 {@code and} / {@code or} / {@code not} / {@code chain}），嵌套深度不受限。
 * 空 parts = 入参集（恒等）。
 *
 * <pre>
 * {"type":"and","parts":[{"type":"radius","radius":2.5,"camp":"enemy"},
 *                        {"type":"filter","where":{"type":"ignited_by_source"}}]}
 * </pre>
 */
public record AndCondition(List<StatCondition> parts) implements StatCondition {

    public static Codec<AndCondition> codec(Codec<StatCondition> self) {
        return RecordCodecBuilder.create(instance -> instance.group(
                self.listOf().fieldOf("parts").forGetter(AndCondition::parts)
        ).apply(instance, AndCondition::new));
    }

    @Override
    public List<LivingEntity> resolve(ConditionScope scope) {
        if (parts == null || parts.isEmpty()) return scope.base();
        List<LivingEntity> acc = parts.get(0).resolve(scope);
        for (int i = 1; i < parts.size() && !acc.isEmpty(); i++) {
            acc = ConditionSets.intersect(acc, parts.get(i).resolve(scope));
        }
        return acc;
    }
}
