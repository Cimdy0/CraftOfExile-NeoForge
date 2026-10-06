package com.cimdy.coe.data.codec.support.stat.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * 锚点节点（{@code type:"anchor"}）：命名锚点 → 0/1 元素集。
 * <p>
 * 即 <b>Target 的两个基本项</b>：{@code killed} = 本次被杀实体、{@code hit} = 本次被命中实体、
 * {@code self} = 你、{@code trigger} = 承受者、{@code source} = 对方、{@code current} = 当前锚点。
 * 未绑定即空集 = 不满足。
 *
 * <pre>
 * {"type":"anchor","anchor":"killed"}
 * </pre>
 */
public record AnchorCondition(ConditionAnchor anchor) implements StatCondition {

    public static final Codec<AnchorCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConditionAnchor.CODEC.fieldOf("anchor").forGetter(AnchorCondition::anchor)
    ).apply(instance, AnchorCondition::new));

    @Override
    public List<LivingEntity> resolve(ConditionScope scope) {
        return anchor == null ? List.of() : anchor.resolve(scope);
    }
}
