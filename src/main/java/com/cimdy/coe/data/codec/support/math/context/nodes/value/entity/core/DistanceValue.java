package com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.support.math.context.query.Candidate;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * 距离读值节点（context_dsl_design.md §5 DistanceValue）。
 * 统一三种距离语义，由 {@link Mode} 区分；原 {@code dist()} 函数与 {@code hit_state:distance} 经别名并入本节点：
 * <ul>
 *   <li>{@code TO_ANCHOR}（默认，中缀 {@code distance}）—— 候选（或根实体）到根锚点的距离；</li>
 *   <li>{@code REFERRER_TO_CANDIDATE}（中缀 {@code dist()}）—— 参照实体到被判断对象的相对距离；</li>
 *   <li>{@code HIT}（中缀 {@code hit_state:distance}）—— 本次命中距离。</li>
 * </ul>
 * 无实体 / 无 position / 无命中 时返回 0。
 */
public record DistanceValue(Mode mode) implements ValueExpr {

    public DistanceValue() {
        this(Mode.TO_ANCHOR);
    }

    public enum Mode implements StringRepresentable {
        TO_ANCHOR("to_anchor"),
        REFERRER_TO_CANDIDATE("referrer_to_candidate"),
        HIT("hit");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public static Mode fromName(String n) {
            for (Mode m : values()) if (m.name.equals(n)) return m;
            return null;
        }
    }

    public static final Codec<DistanceValue> CODEC = RecordCodecBuilder.create(i -> i.group(
            Mode.CODEC.optionalFieldOf("mode", Mode.TO_ANCHOR).forGetter(DistanceValue::mode)
    ).apply(i, DistanceValue::new));

    @Override
    public double evaluate(ValueScope scope) {
        return switch (mode) {
            case TO_ANCHOR -> toAnchor(scope);
            case REFERRER_TO_CANDIDATE -> referrerToCandidate(scope);
            case HIT -> hitDistance(scope);
        };
    }

    // 候选（任意候选域）或根实体到根锚点的距离；无候选时回落根持有者实体
    private double toAnchor(ValueScope scope) {
        Vec3 position = scope.root().getPosition().orElse(null);
        if (position == null) return 0D;
        Candidate candidate = scope.candidate();
        if (candidate != null) return candidate.position().distanceTo(position);
        Entity entity = scope.root().getEntity();
        if (entity == null) return 0D;
        return entity.position().distanceTo(position);
    }

    // 参照实体（referrer）到被判断对象（candidate）的相对距离（filter / sort 表达式常用）
    private double referrerToCandidate(ValueScope scope) {
        Candidate ref = scope.candidate();
        Candidate cand = scope.candidateEntity();
        if (ref == null || cand == null) return 0D;
        return ref.position().distanceTo(cand.position());
    }

    // 本次命中的距离（原 hit_state:distance）
    private double hitDistance(ValueScope scope) {
        HitContext hit = scope.root().getHitContext();
        return hit != null ? hit.distance() : 0D;
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("distance", mode.getSerializedName()));
    }
}
