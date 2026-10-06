package com.cimdy.coe.data.codec.item.mod.identity;

import com.cimdy.coe.data.codec.item.addition.AdditionRead;
import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.addition.impl.MultiAddition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

import java.util.Optional;

/**
 * 词缀实例 —— 一条已 roll 出数值的词缀。
 *
 * <p>重构后（去掉展示文本 description，去掉旧 {@code Either<List<LocalMod>, GlobalMod>} type）：
 * <ul>
 *   <li>{@code type}——引用 {@link AdditionType}（kind + effects 静态定义）；</li>
 *   <li>{@code value}——roll 出的具体数值（{@code min}~{@code max} 之间随机）；</li>
 *   <li>{@code min}/{@code max}——数值范围。</li>
 * </ul>
 * 词缀效果（StatEntry）由 {@code AttributeApplicator} 在词缀生成时经 {@code AdditionType} 产出。
 */
public record Addition(AdditionType type, Optional<Double> value, double min, Optional<Double> max) {
        public static final Codec<Addition> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        AdditionType.CODEC.fieldOf("type").forGetter(Addition::type),
                        Codec.DOUBLE.optionalFieldOf("value").forGetter(Addition::value),
                        Codec.DOUBLE.fieldOf("min").forGetter(Addition::min),
                        Codec.DOUBLE.optionalFieldOf("max").forGetter(Addition::max)
                ).apply(instance, Addition::new));

        public static Addition from(AdditionType type, double min) {
            return new Addition(type, Optional.empty(), min, Optional.empty());
        }

        public static Addition from(AdditionType type, double min, double max) {
            return new Addition(type, Optional.empty(), min, Optional.of(max));
        }

        public Addition random(RandomSource random) {
            if (max.isPresent()) {
                double value = random.nextDouble() * (max.get() - min) + min;
                return new Addition(type, Optional.of(value), min, max);
            }
            return this;
        }

    public double getValue() {
        return value.orElse(min);
    }

    /**
     * 冲突判定用的稳定身份标识：取本词缀类型携带的注册路径。
     * <p>本 Addition 由输入方（生成管线 / 符文 / 精髓）以一条已注册的 {@link AdditionRead} 构造，
     * 其身份即该引用器的注册路径 {@code ref}（{@code ADDITION_MAP} 键）—— 同一引用 = 同一身份 = 同一冲突组。
     * 组合类型 {@link MultiAddition} 以<b>主词（首 Part）</b>递归派生身份：主词是已注册 AdditionRead 则取其 ref，
     * 否则（主词亦无注册身份、或组合为空）无稳定身份、返回空。无身份且未显式填 conflict 路径的 Addition
     * 不参与冲突排除。</p>
     */
    public Optional<Identifier> identity() {
        return identityOf(type);
    }

    /** 身份派生（递归）：AdditionRead → 其 ref；MultiAddition → 主词（首 Part）身份；其余无身份。 */
    private static Optional<Identifier> identityOf(AdditionType type) {
        if (type instanceof AdditionRead(Identifier ref)) {
            return Optional.of(ref);
        }
        if (type instanceof MultiAddition(java.util.List<MultiAddition.Part> parts)) {
            return parts.isEmpty()
                    ? Optional.empty()
                    : identityOf(parts.getFirst().addition());
        }
        return Optional.empty();
    }
}
