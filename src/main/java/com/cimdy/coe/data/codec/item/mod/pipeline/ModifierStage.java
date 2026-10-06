package com.cimdy.coe.data.codec.item.mod.pipeline;

import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import net.minecraft.util.random.WeightedList;

import java.util.Map;
import java.util.Set;

/**
 * 修饰阶段：解耦后的统一扩展钩子。每个修饰系统实现一个阶段臂，由调用方（{@code ModPoolBuilder}）
 * 收集后传入 {@link ModifierPipeline#build}，无需改动核心管线。
 *
 * <p>密封接口：直接子类型为四种操作类别（FILTER / FORCE / BIAS / TRANSFORM），
 * 各系统（BenchStage / …）实现对应的类别接口。
 *
 * <p>{@link ModifierPipeline#build} 按 FILTER → FORCE → BIAS → TRANSFORM 顺序统一消费各阶段臂。
 */
public sealed interface ModifierStage
        permits ModifierStage.Filter, ModifierStage.Force, ModifierStage.Bias, ModifierStage.Transform {

    enum StageKind { FILTER, FORCE, BIAS, TRANSFORM }

    StageKind kind();

    /** 过滤：剔除不应出现的候选词缀（位置级或条目级）。 */
    non-sealed interface Filter extends ModifierStage {
        /** 返回需整体剔除的 location 键集合（如工作台排除标签命中整个位置）。默认无剔除。 */
        default Set<Location> filterLocations(ModifierContext ctx, Map<Location, WeightedList.Builder<CommonMod>> pool) {
            return Set.of();
        }

        /** 条目级过滤：就地移除池中不合规的单个词缀（如影响门控：非当前影响的词缀）。默认无操作。 */
        default void filter(ModifierContext ctx, Map<Location, WeightedList.Builder<CommonMod>> pool) {}

        @Override
        default StageKind kind() {
            return StageKind.FILTER;
        }
    }

    /** 强制：向候选池追加 / 覆盖被强制写入的词缀条目（essence / beast / veiled force）。 */
    non-sealed interface Force extends ModifierStage {
        @Override
        default StageKind kind() {
            return StageKind.FORCE;
        }

        /** 把强制词缀写入候选池（以唯一候选覆盖对应 location，使其成为必中项）。默认无操作。 */
        default void apply(ModifierContext ctx, Map<Location, WeightedList.Builder<CommonMod>> pool) {}
    }

    /** 偏置：按领域标签调整候选权重（fossil）。 */
    non-sealed interface Bias extends ModifierStage {
        @Override
        default StageKind kind() {
            return StageKind.BIAS;
        }

        /** 按领域标签偏置各 location 候选权重。默认无操作。 */
        default void bias(ModifierContext ctx, Map<Location, WeightedList.Builder<CommonMod>> pool) {}
    }

    /** 变换：后处理最终池（harvest reforge、influence→T0 升级）。 */
    non-sealed interface Transform extends ModifierStage {
        @Override
        default StageKind kind() {
            return StageKind.TRANSFORM;
        }

        /** 对最终候选池做后处理（重铸 / 增删 / 升 T0）。默认无操作。 */
        default void transform(ModifierContext ctx, Map<Location, WeightedList.Builder<CommonMod>> pool) {}
    }
}
