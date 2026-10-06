package com.cimdy.coe.data.codec.item.mod.pipeline;

import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.tag.GateTagRegistry;
import net.minecraft.util.random.WeightedList;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;

/**
 * 工作台（crafted-tag）排除阶段：Phase 2 的唯一真实实现（概念验证）。
 * 读取 {@link ModifierContext#excludedTags()}（来自 {@code ITEM_CRAFT_TAGS} 组件），
 * 剔除其领域标签（经 {@link GateTagRegistry} 派生）与排除集相交的候选位置。
 * 仅影响后续词缀 roll，不改动基础 Stat。
 */
public final class BenchStage implements ModifierStage.Filter {

    @Override
    public Set<Location> filterLocations(ModifierContext ctx, Map<Location, WeightedList.Builder<CommonMod>> pool) {
        Set<Location> removed = new HashSet<>();
        Set<String> excluded = ctx.excludedTags();
        if (excluded.isEmpty()) {
            return removed;
        }
        for (Map.Entry<Location, WeightedList.Builder<CommonMod>> entry : pool.entrySet()) {
            Set<String> domain = new HashSet<>();
            for (var element : entry.getValue().getList()) {
                domain.addAll(GateTagRegistry.lookup(element.value()));
            }
            // 任一排除标签被 mod 领域满足即剔除该位置；GateTag.match 支持 defence 父标签
            // 覆盖 armor/evasion/energy_shield/block/resistance/deflect/parry/guard（D8）。
            for (String ex : excluded) {
                if (GateTag.match(domain, Set.of(ex))) {
                    removed.add(entry.getKey());
                    break;
                }
            }
        }
        return removed;
    }
}
