package com.cimdy.coe.data.codec.support.math.context.nodes.value.attr;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Locale;

/**
 * 属性读值节点（context_dsl_design.md §5 AttributeValue）。
 * 输出属性当前值；实体无该属性时返回 0。非 LivingEntity 作用域返回 0。
 */
public record AttributeValue(Identifier id) implements EntityNode {
    public static final Codec<AttributeValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("id").forGetter(AttributeValue::id)
            ).apply(instance, AttributeValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        return BuiltInRegistries.ATTRIBUTE.get(id)
                .map(Holder.Reference::getDelegate)
                .map(attribute -> living.getAttributes().hasAttribute(attribute)
                        ? living.getAttributeValue(attribute) : 0D)
                .orElseThrow(() -> new IllegalArgumentException("未知实体属性: " + id));
    }

    /** 名词短语：复用属性自带的权威键 {@code attribute.name.<path>}（不铸 {@code stat.value.attribute.*} 孪生键）。 */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.ofKey("attribute.name." + id.getPath(), id.getPath()));
    }
}
