package com.cimdy.coe.data.codec.support.status.def;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * 异常施加配置（数据驱动）。取代旧的 {@code AilmentType} 枚举：
 * 异常种类现在直接引用统一状态系统 {@link StatusType}（按 id 编解码），实现状态体系统一。
 * <p>字段语义：
 * <ul>
 *   <li>buildupMultiplier / fixedBuildup：异常累积值（命中时累积，达到阈值后施加）。</li>
 *   <li>overrideDurationTicks：可选，覆盖 {@link StatusType#defaultDurationTicks()} 的缺省时长。</li>
 *   <li>effectMagnitude：施加后的强度（如点燃的每秒伤害百分比、冰冻/减速的减速百分比）。</li>
 *   <li>maxStacks：最大叠层上限加算（属性驱动，{status, 异常名} 作用域，对齐 status_max_stacks）。</li>
 *   <li>statusExtraStacks：每次施加额外层数（如「造成额外一层中毒」，属性驱动，对齐 status_extra_stacks）。</li>
 *   <li>thresholdRatio：累积阈值系数。</li>
 *   <li>canBeRemoved：是否可被净化/移除。</li>
 * </ul>
 */
public class AilmentData {
    private final StatusType type;
    private final ExpressionCodec.Value buildupMultiplier;
    private final Optional<ExpressionCodec.Value> fixedBuildup;
    private final Optional<ExpressionCodec.Value> overrideDurationTicks;
    private final Optional<ExpressionCodec.Value> effectMagnitude;
    private final Optional<ExpressionCodec.Value> maxStacks;
    private final Optional<ExpressionCodec.Value> statusExtraStacks;
    private final Optional<ExpressionCodec.Value> thresholdRatio;
    private final Optional<Boolean> canBeRemoved;

    public AilmentData(StatusType type, ExpressionCodec.Value buildupMultiplier,
                       Optional<ExpressionCodec.Value> fixedBuildup,
                       Optional<ExpressionCodec.Value> overrideDurationTicks,
                       Optional<ExpressionCodec.Value> effectMagnitude,
                       Optional<ExpressionCodec.Value> maxStacks,
                       Optional<ExpressionCodec.Value> statusExtraStacks,
                       Optional<ExpressionCodec.Value> thresholdRatio,
                       Optional<Boolean> canBeRemoved) {
        this.type = type;
        this.buildupMultiplier = buildupMultiplier;
        this.fixedBuildup = fixedBuildup;
        this.overrideDurationTicks = overrideDurationTicks;
        this.effectMagnitude = effectMagnitude;
        this.maxStacks = maxStacks;
        this.statusExtraStacks = statusExtraStacks;
        this.thresholdRatio = thresholdRatio;
        this.canBeRemoved = canBeRemoved;
    }

    public StatusType type() { return type; }
    public ExpressionCodec.Value buildupMultiplier() { return buildupMultiplier; }
    public Optional<ExpressionCodec.Value> fixedBuildup() { return fixedBuildup; }
    public Optional<ExpressionCodec.Value> overrideDurationTicks() { return overrideDurationTicks; }
    public Optional<ExpressionCodec.Value> effectMagnitude() { return effectMagnitude; }
    public Optional<ExpressionCodec.Value> maxStacks() { return maxStacks; }
    public Optional<ExpressionCodec.Value> statusExtraStacks() { return statusExtraStacks; }
    public Optional<ExpressionCodec.Value> thresholdRatio() { return thresholdRatio; }
    public Optional<Boolean> canBeRemoved() { return canBeRemoved; }

    public int getDurationTicks() {
        int base = type.defaultDurationTicks;
        return overrideDurationTicks
                .map(v -> (int) v.getFloat())
                .orElse(base);
    }

    public static final Codec<AilmentData> CODEC = RecordCodecBuilder.create(i -> i.group(
            StatusType.CODEC.fieldOf("type").forGetter(AilmentData::type),
            ExpressionCodec.VALUE_CODEC.fieldOf("buildup_multiplier").forGetter(AilmentData::buildupMultiplier),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("fixed_buildup").forGetter(AilmentData::fixedBuildup),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("override_duration_ticks").forGetter(AilmentData::overrideDurationTicks),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("effect_magnitude").forGetter(AilmentData::effectMagnitude),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("max_stacks").forGetter(AilmentData::maxStacks),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("status_extra_stacks").forGetter(AilmentData::statusExtraStacks),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("threshold_ratio").forGetter(AilmentData::thresholdRatio),
            Codec.BOOL.optionalFieldOf("can_be_removed").forGetter(AilmentData::canBeRemoved)
    ).apply(i, AilmentData::new));

    @Override
    public String toString() {
        return "AilmentData{" + type.id + ", buildup=" + buildupMultiplier + ", dur=" + getDurationTicks() + "}";
    }
}
