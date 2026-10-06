package com.cimdy.coe.data.codec.item.mod.value;

import com.cimdy.coe.data.codec.item.addition.impl.MultiAddition;
import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.ItemInstance;
import com.cimdy.coe.data.codec.item.ItemInstances;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 词缀预设（原 {@code Mod}）：<b>一条 {@link AdditionType} + 其阶列表 {@code tiers}</b>。
 *
 * <p>「一条 AdditionType」即可承载多条词缀 —— 需要多条时用 {@link com.cimdy.coe.data.codec.item.addition.impl.MultiAddition}
 * 组合（其 {@code valueSlots} 声明各子词缀的值槽，值侧 {@link TierPreset} 按同槽名供值）。
 *
 * <p>冲突由运行时按 Addition 注册身份（或显式 {@code conflict} 路径）派生，本预设不再持有任何 id。</p>
 *
 * <p>构造仅经建造者：{@link #builder(AdditionType)}（{@link AdditionType}）起手，
 * {@link Builder#withTier(String, Map, int, int)} 用原始参数（名称 / 槽名→值映射 / 需求等级 / 权重）直接累加一阶，
 * 阶号从 T1 起自动递增。
 * 不经 {@code WithoutTier} 等中间层，也不另设 {@code from(...)} 静态工厂（避免多包一层）。
 */
public record ModPreset(AdditionType type, List<TierPreset> tiers) implements ItemInstance {

    public static final Codec<ModPreset> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AdditionType.CODEC.fieldOf("type").forGetter(ModPreset::type),
                    TierPreset.CODEC.listOf().fieldOf("tiers").forGetter(ModPreset::tiers)
            ).apply(instance, ModPreset::new));

    /** 建造者起点：以 {@link AdditionType} 创建一个空白词缀预设。 */
    public static Builder builder(AdditionType type) {
        return new Builder(type);
    }

    @Override
    public Type<?> getType() {
        return ItemInstances.ITEM_MOD;
    }

    /**
     * 建造者：{@link AdditionType} 起手，{@link #withTier} 用原始参数累加阶（从 T1 起）。
     */
    public static final class Builder {
        private final AdditionType type;
        private final List<TierPreset> tiers = new ArrayList<>();

        private Builder(AdditionType type) {
            this.type = type;
        }

        /** 增加一个阶：直接用名称、槽名→值映射、需求等级、权重（阶号从 T1 起自动递增）。 */
        public Builder withTier(String name, Map<String, ModValue> values, int level, int weight) {
            int tierIndex = tiers.size() + 1;
            tiers.add(new TierPreset(name, values, tierIndex, level, weight));
            return this;
        }

        /**
         * 增加一个阶（单值）：值槽名缺省为 {@code "value"} —— 绝大多数词缀只有一个值槽，
         * 无需每次手写 {@code Map.of("value", ...)}。多值槽（min/max 等）走 {@link #withTier(String, Map, int, int)}。
         */
        public Builder withTier(String name, ModValue value, int level, int weight) {
            return withTier(name, Map.of("value", value), level, weight);
        }

        /** 增加一个已构造的阶（阶号沿用 {@link TierPreset} 自带的 tier 字段）。 */
        public Builder withTier(TierPreset tier) {
            tiers.add(tier);
            return this;
        }

        public ModPreset build() {
            return new ModPreset(type, List.copyOf(tiers));
        }
    }
}
