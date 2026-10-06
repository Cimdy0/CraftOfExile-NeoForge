package com.cimdy.coe.data.codec.item.mod.pipeline;

import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeHierarchy;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.cimdy.coe.data.codec.item.registry.ItemDataRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 修饰管线：按固定顺序 FILTER → FORCE → BIAS → TRANSFORM 运行各阶段。
 * R2 起真实消费全部四臂；R3 起六个工艺系统全部以真实阶段臂接入。
 */
public final class ModifierPipeline {

    private ModifierPipeline() {}

    /**
     * 作用域命中：物品类型含该池路径的「类别段」，且位置（prefix/suffix）未被收窄排除。
     * 池键以 {@code <namespace>/<category>/<location>} 编码（{@code location} 即 subtype）。
     */
    private static boolean matchesScope(ModifierContext ctx, Identifier key) {
        String path = key.getPath();
        int slash = path.lastIndexOf('/');
        Identifier category = slash < 0
                ? key
                : Identifier.fromNamespaceAndPath(key.getNamespace(), path.substring(0, slash));
        String subtype = slash < 0 ? path : path.substring(slash + 1);
        return ItemTypeHierarchy.isWithin(category, ctx.types())
                && (ctx.locations().isEmpty() || ctx.locations().contains(Location.byName(subtype)));
    }

    /** 池键路径末段（prefix/suffix）。 */
    private static String locationOf(Identifier key) {
        String path = key.getPath();
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }

    /** 把一批 builder 并入以 subtype 为键的候选池（累加，不覆盖）。 */
    private static void merge(Map<Location, WeightedList.Builder<CommonMod>> builders,
                              Location subtype, List<WeightedList.Builder<CommonMod>> incoming) {
        WeightedList.Builder<CommonMod> target =
                builders.computeIfAbsent(subtype, k -> new WeightedList.Builder<>());
        incoming.forEach(b -> target.addAll(b.getList()));
    }

    public static Map<Location, WeightedList.Builder<CommonMod>> build(ModifierContext ctx, List<ModifierStage> stages) {
        Map<Location, WeightedList.Builder<CommonMod>> builders = new HashMap<>();
        int level = ctx.state().level();

        // 普通词缀（槽位层 EquipmentModPreset）：按类型 / 位置过滤后「并入」候选池
        ItemDataRegistry.getInstance().getModPools().forEach((key, builderList) -> {
            if (matchesScope(ctx, key)) {
                merge(builders, Location.byName(locationOf(key)), builderList);
            }
        });

        // 预设词缀（大类层 EquipmentModPreset）：额外按 EquipmentModPreset.Condition 匹配后「并入」同一候选池
        // ⚠️ 两层共用同一 subtype 键（prefix/suffix），必须合并而非 put ——
        //    否则大类层会把槽位层整池顶掉，导致槽位专有词缀全部失效。
        ItemDataRegistry.getInstance().getPresetModPools().forEach((key, builderMap) -> {
            if (matchesScope(ctx, key)) {
                builderMap.forEach((condition, builderList) -> {
                    if (condition.matches(ctx.state())) {
                        merge(builders, Location.byName(locationOf(key)), builderList);
                    }
                });
            }
        });

        // 等级过滤与空池剔除统一在合并后做一次（等价于分层各自过滤）
        builders.values().forEach(b -> b.removeIf(m -> level < m.value().level()));
        builders.entrySet().removeIf(e -> e.getValue().build().isEmpty());

        // FILTER 阶段：位置级剔除 + 条目级过滤
        stages.stream()
                .filter(s -> s.kind() == ModifierStage.StageKind.FILTER)
                .map(s -> (ModifierStage.Filter) s)
                .forEach(f -> {
                    f.filterLocations(ctx, builders).forEach(builders::remove);
                    f.filter(ctx, builders);
                });

        // FORCE 阶段：强制写入词缀（essence / beast / veiled force）
        stages.stream()
                .filter(s -> s.kind() == ModifierStage.StageKind.FORCE)
                .map(s -> (ModifierStage.Force) s)
                .forEach(f -> f.apply(ctx, builders));

        // BIAS 阶段：按领域标签偏置候选权重（fossil）
        stages.stream()
                .filter(s -> s.kind() == ModifierStage.StageKind.BIAS)
                .map(s -> (ModifierStage.Bias) s)
                .forEach(b -> b.bias(ctx, builders));

        // TRANSFORM 阶段：后处理最终池（harvest / influence→T0）
        stages.stream()
                .filter(s -> s.kind() == ModifierStage.StageKind.TRANSFORM)
                .map(s -> (ModifierStage.Transform) s)
                .forEach(t -> t.transform(ctx, builders));

        return builders;
    }
}
