package com.cimdy.coe.data.codec.support.math.context.nodes.value.combat;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.DistanceValue;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * 命中状态读值节点：从 ExpressionContext 的 HitContext 读取命中信息，
 * 替代旧 {@code HitTestType} 体系（HitTypeTest / DistanceTest）。
 * <p>
 * 各 mode 语义：
 * <ul>
 *   <li>{@code is_entity} —— 命中实体 → 1.0；非命中/非实体 → 0.0</li>
 *   <li>{@code is_block} —— 命中方块 → 1.0；非命中/非方块 → 0.0</li>
 *   <li>{@code is_miss} —— 落空 → 1.0；否则 → 0.0</li>
 * </ul>
 * <p>
 * JSON 形式：{@code {"type": "hit_state", "mode": "is_entity"}}；变量中缀：{@code hit_state:is_entity}。
 * 命中距离（原 {@code hit_state:distance}）已并入 {@code distance} 节点（{@code DistanceValue.Mode.HIT}），
 * 中缀 {@code hit_state:distance} 仍可作为别名解析。
 */
public record HitStateValue(Mode mode) implements com.cimdy.coe.data.codec.support.math.context.node.ValueExpr {

    public enum Mode implements StringRepresentable {
        IS_ENTITY("is_entity"), IS_BLOCK("is_block"), IS_MISS("is_miss");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        private final String name;
        Mode(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }

        /** 按序列化名称查找；未找到返回 null。 */
        public static Mode fromName(String name) {
            for (Mode m : values()) {
                if (m.name.equals(name)) return m;
            }
            return null;
        }
    }

    public static final Codec<HitStateValue> CODEC = RecordCodecBuilder.create(i -> i.group(
            Mode.CODEC.fieldOf("mode").forGetter(HitStateValue::mode)
    ).apply(i, HitStateValue::new));

    @Override
    public double evaluate(ValueScope scope) {
        ExpressionContext ctx = scope.root();
        HitContext hit = ctx.getHitContext();
        if (hit == null) return 0D;

        return switch (mode) {
            case IS_ENTITY -> hit.isEntity() ? 1D : 0D;
            case IS_BLOCK -> hit.isBlock() ? 1D : 0D;
            case IS_MISS -> hit.isMiss() ? 1D : 0D;
        };
    }

    /** 名词短语：命中状态 mode → 值键 {@code stat.value.hit_state.<mode>}（P3/A）。 */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("hit_state", mode.getSerializedName()));
    }

    /** 条件从句：{@code stat.condition.hit_state} = "when you %s"（%s = 名词短语）。 */
    @Override
    public List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.hit_state", describeQuantity(ClientLocale.get()).get(0)));
    }
}
