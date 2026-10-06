package com.cimdy.coe.data.codec.support.stat.effect.marker;

import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;
import java.util.Locale;
import com.mojang.serialization.Codec;

/**
 * 需求亚型（inert）：装备「属性需求」修正 —— 只参与装备层 ItemRequire 结算，<b>不进 StatStore</b>。
 *
 * <p>为什么它仍是 {@code StatEffect} 而非自造类型：词缀效果统一承载在 {@code StatEffect} 上
 * （复用 Stat 体系的 codec 分发 / describe / 生成期求值），但本类：
 * <ul>
 *   <li>不是 {@code ModifierStatEffect}——{@code StatStore} 构造时按
 *       {@code instanceof ModifierStatEffect} 收集 index，天然被排除；</li>
 *   <li>{@link #isTrigger()} 返回 false——不进 triggers 触发通道；</li>
 *   <li>{@link #onTrigger} 保持空实现——即使被 fire 也零副作用。</li>
 * </ul>
 * 因此需求效果在 Stat 体系里是「惰性标记」，仅词缀生成器读取
 * {@code instanceof RequirementStatEffect} 时把数值写入装备的 ItemRequire。
 *
 * <p>数值语义：条目唯一数值 = 需求修正量，{@code operation} 决定运算——
 * {@code ADD_VALUE}（加算，如「需求 +10」）/ {@code ADD_MULTIPLIED_BASE}（增幅，如「需求 +20%」）。
 */
public class RequirementStatEffect extends AbstractStatEffect {

    private final AttributeModifier.Operation operation;
    private final double martialMultiplier;

    public RequirementStatEffect(AttributeModifier.Operation operation) {
        this(operation, 1.0);
    }

    public RequirementStatEffect(AttributeModifier.Operation operation, double martialMultiplier) {
        super(GateExpr.ALWAYS);
        this.operation = operation;
        this.martialMultiplier = martialMultiplier;
    }

    public AttributeModifier.Operation operation() {
        return operation;
    }

    /** 武术武器属性需求倍率（默认 1.0 = 不修改；node 32349 MARTIAL_REQUIREMENTS_TRIPLED 置 3.0）。 */
    public double martialMultiplier() {
        return martialMultiplier;
    }

    @Override
    public String type() {
        return "requirement";
    }

    @Override
    public boolean isTrigger() {
        return false;
    }

    /** 惰性效果：不进 StatStore（仅词缀生成器读取写 ItemRequire）。 */
    @Override
    public EffectKind indexKind() {
        return EffectKind.INERT;
    }

    public static final MapCodec<RequirementStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(RequirementStatEffect::operation),
            Codec.DOUBLE.optionalFieldOf("martial_multiplier", 1.0).forGetter(RequirementStatEffect::martialMultiplier)
    ).apply(i, RequirementStatEffect::new));

    @Override
    public List<Component> describe(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    @Override
    public List<Component> describeFragment(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        return List.of(Component.translatable("stat_effect.requirement",
                Component.literal(switch (operation) {
                    case ADD_VALUE -> "+";
                    case ADD_MULTIPLIED_BASE -> "×";
                    default -> "?";
                })));
    }
}
