package com.cimdy.coe.data.codec.support.math.context.nodes.value.skill;

import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * 元技能能量读值节点：读取实体单例的元技能能量计数
 * （{@code ExpressionContext.metaEnergy}，由 {@code MetaSkillAction} 累加、满阈值触发后重置）。
 * <p>
 * 中缀：{@code meta_energy:<key>}——输出该 key 的当前能量计数（未积累 → 0），
 * 供「元技能能量 ≥ 阈值时…」类条件在 math 侧判定。
 */
public record MetaEnergyValue(String key) implements EntityNode {

    public static final Codec<MetaEnergyValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(MetaEnergyValue::key)
    ).apply(instance, MetaEnergyValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        return ExpressionContext.get(living).getMetaEnergy(key);
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("meta_energy", key));
    }
}
