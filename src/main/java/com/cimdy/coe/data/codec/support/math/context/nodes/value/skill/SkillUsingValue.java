package com.cimdy.coe.data.codec.support.math.context.nodes.value.skill;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.cimdy.coe.helper.EntityHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.Locale;

/**
 * 实体当前正在使用的技能读值节点（context_dsl_design.md §5 SkillUsingValue）。
 * 输出 1（实体当前 activeSkills 中存在 caster 为其自身、且技能 id 匹配的实例）/ 0（否则）。
 * 与 {@code SkillStateValue} 的 {@code press/released} 同为 1/0 布尔式读值，可同时服务于
 * math 求值（如 {@code skill_using:coe:fireball >= 1}）与 predicate 检测（{@code skill} 字段），
 * 单一实现被两处复用。
 */
public record SkillUsingValue(Identifier id) implements EntityNode {
    public static final Codec<SkillUsingValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("id").forGetter(SkillUsingValue::id)
            ).apply(instance, SkillUsingValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        return EntityHelper.isUsingSkill(living, id) ? 1D : 0D;
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("using_skill", id.getPath()));
    }
}
