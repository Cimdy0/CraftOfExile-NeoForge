package com.cimdy.coe.data.codec.support.math.context.nodes.value.skill;

import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.Locale;

/**
 * 实体实时技能状态读值节点（context_dsl_design.md §5 SkillStateValue）。
 * {@code mode} 对应旧 {@code skill.level} / {@code quality} / {@code charge} / {@code press} /
 * {@code released} / {@code press.time} / {@code bind}，由 {@link ValueScope#root()} 的技能状态字段读取。
 */
public record SkillStateValue(String mode) implements ValueExpr {
    public static final Codec<SkillStateValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("mode").forGetter(SkillStateValue::mode)
            ).apply(instance, SkillStateValue::new));

    public static boolean isSkillState(String type) {
        return switch (type) {
            case "skill.level", "quality", "charge", "press", "released", "press.time", "bind" -> true;
            default -> false;
        };
    }

    @Override
    public double evaluate(ValueScope scope) {
        return switch (mode) {
            case "skill.level" -> scope.root().getSkillLevel();
            case "quality" -> scope.root().getQuality();
            case "charge" -> scope.root().getChargeAmount();
            case "press" -> scope.root().isPress() ? 1 : 0;
            case "released" -> scope.root().isJustReleased() ? 1 : 0;
            case "press.time" -> scope.root().getPressTime();
            case "bind" -> scope.root().getBind();
            default -> 0D;
        };
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("skill_state", mode));
    }
}
