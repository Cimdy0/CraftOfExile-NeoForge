package com.cimdy.coe.data.codec.stronghold.task.helper.recipe;

import com.cimdy.coe.data.codec.stronghold.task.helper.demand.AggregatedDemand;
import com.cimdy.coe.data.codec.stronghold.task.helper.supply.AggregatedSupply;
import com.cimdy.coe.data.codec.stronghold.task.helper.demand.DemandInfo;
import com.cimdy.coe.data.codec.stronghold.task.helper.supply.DetailedSuppliesPool;
import com.cimdy.coe.data.codec.stronghold.task.helper.claim.MaterialClaim;
import com.cimdy.coe.data.codec.stronghold.task.helper.result.PlanResult;
import com.cimdy.coe.data.codec.stronghold.task.helper.claim.ProductClaim;
import com.cimdy.coe.data.codec.stronghold.task.helper.supply.SupplyInfo;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.*;

/**
 * 合成缓存管理器，负责根据需求和供应生成详细的合成计划。
 * 计划中包含每个步骤所需的具体材料来源容器和产物去向容器。
 */
public class RecipeCacheManager {
    public static final Codec<List<CraftPlanDetail>> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, List<CraftPlanDetail>> STREAM_CODEC;
    private static final RecipeCache CACHE = new RecipeCache();

    public static RecipeCache get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            CACHE.rebuildIfNeeded(serverLevel.getServer());
        }
        return CACHE;
    }

    // ================= 核心数据结构 =================

    static {
        CODEC = CraftPlanDetail.CODEC.listOf();
        STREAM_CODEC = CraftPlanDetail.STREAM_CODEC.apply(ByteBufCodecs.list());
    }

    // ================= 公共入口 =================

    public static List<CraftPlanDetail> planAll(Collection<DemandInfo> demands,
                                                List<SupplyInfo> supplies,
                                                Map<BlockPos, RecipeType<?>> sites,
                                                ServerLevel level) {
        RecipeCache cache = get(level);

        // 1. 聚合需求
        Map<ItemResource, Map<Integer, AggregatedDemand>> demandGroups = new LinkedHashMap<>();
        for (DemandInfo d : demands) {
            demandGroups.computeIfAbsent(d.resource(), k -> new LinkedHashMap<>())
                    .compute(d.priority(), (pri, existing) -> {
                        if (existing == null) {
                            List<DemandInfo> targets = new ArrayList<>();
                            targets.add(d);
                            return new AggregatedDemand(d.resource(), pri, d.remaining(), targets);
                        } else {
                            List<DemandInfo> targets = new ArrayList<>(existing.targets());
                            targets.add(d);
                            return new AggregatedDemand(d.resource(), pri,
                                    existing.totalAmount() + d.remaining(), targets);
                        }
                    });
        }

        // 2. 聚合供应
        Map<ItemResource, Map<Integer, AggregatedSupply>> supplyGroups = new LinkedHashMap<>();
        for (SupplyInfo s : supplies) {
            supplyGroups.computeIfAbsent(s.resource(), k -> new LinkedHashMap<>())
                    .compute(s.priority(), (pri, existing) -> {
                        if (existing == null) {
                            List<SupplyInfo> sources = new ArrayList<>();
                            sources.add(s);
                            return new AggregatedSupply(s.resource(), pri, s.remaining(), sources);
                        } else {
                            List<SupplyInfo> sources = new ArrayList<>(existing.sources());
                            sources.add(s);
                            return new AggregatedSupply(s.resource(), pri,
                                    existing.totalAmount() + s.remaining(), sources);
                        }
                    });
        }

        // 3. 详细供应池
        DetailedSuppliesPool pool = new DetailedSuppliesPool(supplyGroups);

        // 4. 为每个聚合需求生成详细计划
        List<CraftPlanDetail> allPlans = new ArrayList<>();
        for (Map<Integer, AggregatedDemand> priMap : demandGroups.values()) {
            for (AggregatedDemand ad : priMap.values()) {
                PlanResult result = cache.planSingleDetailed(ad.resource(), ad.totalAmount(), pool, sites, level, new HashSet<>());
                if (result != null && !result.steps().isEmpty()) {
                    List<CraftStepDetail> finalSteps = assignProducts(result.steps(), ad.targets(), level);
                    allPlans.add(new CraftPlanDetail(ad.resource(), result.producedAmount(), finalSteps));
                }
            }
        }

        return mergePlans(allPlans);
    }

    private static List<CraftStepDetail> assignProducts(List<CraftStepDetail> steps,
                                                        List<DemandInfo> targets,
                                                        ServerLevel level) {
        if (steps.isEmpty()) return steps;

        // 只取最后一个步骤的产量
        CraftStepDetail lastStep = steps.get(steps.size() - 1);
        int totalProduced = lastStep.times() * getRecipeYield(lastStep.recipe(), level);
        if (totalProduced <= 0) return steps;

        List<ProductClaim> claims = new ArrayList<>();
        int remaining = totalProduced;
        for (DemandInfo target : targets) {
            int give = Math.min(remaining, target.remaining());
            if (give > 0) {
                claims.add(new ProductClaim(target.resource(), give, target.containerPos()));
                remaining -= give;
            }
            if (remaining <= 0) break;
        }

        List<CraftStepDetail> newSteps = new ArrayList<>(steps);
        newSteps.set(newSteps.size() - 1, new CraftStepDetail(
                lastStep.recipe(), lastStep.site(), lastStep.times(), lastStep.materials(), claims));
        return newSteps;
    }

    private static int getRecipeYield(ResourceKey<Recipe<?>> recipeKey, ServerLevel level) {
        Optional<RecipeHolder<?>> holder = level.getServer().getRecipeManager().byKey(recipeKey);
        if (holder.isEmpty()) return 1;
        Recipe<?> recipe = holder.get().value();
        ItemStack result = RecipeCache.getRecipeResult(recipe, level);
        return result.isEmpty() ? 1 : result.getCount();
    }

    private static List<CraftPlanDetail> mergePlans(List<CraftPlanDetail> plans) {
        record StepKey(BlockPos site, ResourceKey<Recipe<?>> recipe) {}

        List<CraftPlanDetail> mergedPlans = new ArrayList<>();
        for (CraftPlanDetail plan : plans) {
            Map<StepKey, List<CraftStepDetail>> grouped = new LinkedHashMap<>();
            for (CraftStepDetail step : plan.steps()) {
                StepKey key = new StepKey(step.site(), step.recipe());
                grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(step);
            }

            List<CraftStepDetail> newSteps = new ArrayList<>();
            for (List<CraftStepDetail> group : grouped.values()) {
                if (group.size() == 1) {
                    newSteps.add(group.get(0));
                    continue;
                }
                int totalTimes = group.stream().mapToInt(CraftStepDetail::times).sum();

                // 合并 MaterialClaim
                Map<String, Integer> materialMap = new LinkedHashMap<>();
                Map<String, MaterialClaim> materialClaimMap = new HashMap<>();
                for (CraftStepDetail step : group) {
                    for (MaterialClaim mc : step.materials()) {
                        String key = mc.resource() + ":" + mc.containerPos();
                        materialMap.merge(key, mc.amount(), Integer::sum);
                        materialClaimMap.putIfAbsent(key, mc);
                    }
                }
                List<MaterialClaim> mergedMaterials = new ArrayList<>();
                for (var entry : materialMap.entrySet()) {
                    MaterialClaim template = materialClaimMap.get(entry.getKey());
                    mergedMaterials.add(new MaterialClaim(template.resource(), entry.getValue(), template.containerPos()));
                }

                // 合并 ProductClaim
                Map<String, Integer> productMap = new LinkedHashMap<>();
                Map<String, ProductClaim> productClaimMap = new HashMap<>();
                for (CraftStepDetail step : group) {
                    for (ProductClaim pc : step.products()) {
                        String key = pc.resource() + ":" + pc.containerPos();
                        productMap.merge(key, pc.amount(), Integer::sum);
                        productClaimMap.putIfAbsent(key, pc);
                    }
                }
                List<ProductClaim> mergedProducts = new ArrayList<>();
                for (var entry : productMap.entrySet()) {
                    ProductClaim template = productClaimMap.get(entry.getKey());
                    mergedProducts.add(new ProductClaim(template.resource(), entry.getValue(), template.containerPos()));
                }

                CraftStepDetail merged = new CraftStepDetail(
                        group.get(0).recipe(), group.get(0).site(), totalTimes,
                        mergedMaterials, mergedProducts);
                newSteps.add(merged);
            }

            mergedPlans.add(new CraftPlanDetail(plan.result(), plan.amount(), newSteps));
        }

        return mergedPlans;
    }
}