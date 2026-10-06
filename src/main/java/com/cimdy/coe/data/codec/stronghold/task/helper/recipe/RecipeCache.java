package com.cimdy.coe.data.codec.stronghold.task.helper.recipe;

import com.cimdy.coe.data.codec.stronghold.task.helper.supply.DetailedSuppliesPool;
import com.cimdy.coe.data.codec.stronghold.task.helper.claim.MaterialClaim;
import com.cimdy.coe.data.codec.stronghold.task.helper.result.PlanResult;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.lang.ref.WeakReference;
import java.util.*;

public class RecipeCache {
        private Map<Item, List<RecipeHolder<?>>> productToRecipes = new HashMap<>();
        private Map<Item, List<RecipeHolder<?>>> ingredientToRecipes = new HashMap<>();
        private WeakReference<RecipeManager> lastRecipeManager = new WeakReference<>(null);

        synchronized void rebuildIfNeeded(MinecraftServer server) {
            ServerLevel overworld = server.getLevel(Level.OVERWORLD);
            if (overworld == null) return;
            RecipeManager current = overworld.getServer().getRecipeManager();
            if (current != lastRecipeManager.get()) {
                rebuild(current, overworld);
            }
        }

        private void rebuild(RecipeManager recipeManager, Level level) {
            Map<Item, List<RecipeHolder<?>>> newProduct = new HashMap<>();
            Map<Item, List<RecipeHolder<?>>> newIngredient = new HashMap<>();
            for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
                Recipe<?> recipe = holder.value();
                if (recipe.isSpecial()) continue;
                ItemStack result = getRecipeResult(recipe, level);
                if (result.isEmpty()) continue;
                Item productItem = result.getItem();
                newProduct.computeIfAbsent(productItem, k -> new ArrayList<>()).add(holder);
                List<Ingredient> ingredients = recipe.placementInfo().ingredients();
                for (Ingredient ingredient : ingredients) {
                    if (ingredient.isEmpty()) continue;
                    ingredient.items().forEach(itemHolder -> {
                        Item item = itemHolder.value();
                        newIngredient.computeIfAbsent(item, k -> new ArrayList<>()).add(holder);
                    });
                }
            }
            this.productToRecipes = newProduct;
            this.ingredientToRecipes = newIngredient;
            this.lastRecipeManager = new WeakReference<>(recipeManager);
        }

        public List<RecipeHolder<?>> getRecipesFor(Item product, RecipeType<?> type) {
            List<RecipeHolder<?>> list = productToRecipes.getOrDefault(product, List.of());
            if (type == null) return list;
            return list.stream().filter(h -> h.value().getType() == type).toList();
        }

        /** 该产品是否已有原版配方（任意类型）。用于避免自定义变换与注册表驱动管线重复产出。 */
        public boolean hasProductRecipe(Item product) {
            return productToRecipes.containsKey(product);
        }

        public static ItemStack getRecipeResult(Recipe<?> recipe, Level level) {
            for (RecipeDisplay display : recipe.display()) {
                SlotDisplay resultDisplay = display.result();
                ItemStack stack = resolveSlotDisplay(resultDisplay, level);
                if (!stack.isEmpty()) return stack;
            }
            return ItemStack.EMPTY;
        }

        private static ItemStack resolveSlotDisplay(SlotDisplay display, Level level) {
            if (display instanceof SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate stack)) {
                return stack.create();
            }
            ContextKeySet keySet = new ContextKeySet.Builder()
                    .required(SlotDisplayContext.REGISTRIES)
                    .build();
            ContextMap context = new ContextMap.Builder()
                    .withParameter(SlotDisplayContext.REGISTRIES, level.registryAccess())
                    .create(keySet);
            return display.resolveForFirstStack(context);
        }

        // --------- 递归合成规划（携带 visited 防止循环） ---------
        PlanResult planSingleDetailed(ItemResource target, int need,
                                              DetailedSuppliesPool pool,
                                              Map<BlockPos, RecipeType<?>> sites,
                                              ServerLevel level,
                                              Set<Item> visited) {
            if (need <= 0) return new PlanResult(0, List.of(), null, null, List.of());

            // 防止循环合成（如铁粒⇄铁锭）
            if (!visited.add(target.getItem())) {
                return null;
            }

            // 1. 直接供应
            int available = pool.getAvailable(target);
            int take = Math.min(need, available);
            if (take > 0) {
                List<MaterialClaim> claims = pool.reserve(target, take);
                visited.remove(target.getItem()); // 直接供应后移除标记，允许其他路径使用
                return new PlanResult(take, claims);
            }

            // 2. 合成规划
            List<RecipeHolder<?>> recipes = getRecipesFor(target.getItem(), null);
            PlanResult bestResult = null;
            int bestCost = Integer.MAX_VALUE;

            for (RecipeHolder<?> holder : recipes) {
                Recipe<?> recipe = holder.value();
                RecipeType<?> type = recipe.getType();
                BlockPos site = sites.entrySet().stream()
                        .filter(e -> e.getValue() == type)
                        .map(Map.Entry::getKey).findFirst().orElse(null);
                if (site == null) continue;

                ItemStack resultStack = getRecipeResult(recipe, level);
                if (resultStack.isEmpty()) continue;
                int perCraft = resultStack.getCount();

                int idealTimes = (need + perCraft - 1) / perCraft;
                int bestTimes = 0, bestProduce = 0;
                DetailedSuppliesPool bestSnapshot = null;
                List<CraftStepDetail> bestSteps = null;
                List<MaterialClaim> bestMaterials = null;

                for (int tryTimes = idealTimes; tryTimes >= 1; tryTimes--) {
                    DetailedSuppliesPool snapshot = pool.copy();
                    List<CraftStepDetail> subSteps = new ArrayList<>();
                    List<MaterialClaim> directMaterials = new ArrayList<>();
                    boolean possible = true;

                    Map<ItemResource, Integer> ingredientMap = new LinkedHashMap<>();
                    // 为每个原料计算成本时，也需要传递 visited 防止循环评估
                    Set<Item> costVisited = new HashSet<>(visited);

                    for (Ingredient ing : recipe.placementInfo().ingredients()) {
                        if (ing.isEmpty()) continue;
                        ItemResource chosen = ing.items()
                                .map(h -> ItemResource.of(h.value()))
                                .min(Comparator.comparingInt(res -> getTotalCost(res, snapshot, sites, level, costVisited)))
                                .orElse(null);
                        if (chosen == null) { possible = false; break; }
                        ingredientMap.merge(chosen, tryTimes, Integer::sum);
                    }
                    if (!possible) continue;

                    // 递归获取原料，必须传入同一个 visited
                    for (Map.Entry<ItemResource, Integer> entry : ingredientMap.entrySet()) {
                        ItemResource ingRes = entry.getKey();
                        int totalNeed = entry.getValue();
                        PlanResult sub = planSingleDetailed(ingRes, totalNeed, snapshot, sites, level, visited);
                        if (sub == null || sub.producedAmount() < totalNeed) {
                            possible = false; break;
                        }

                        subSteps.addAll(sub.steps());

                        // 正确区分合成产出与直接供应
                        if (sub.producedResource() != null && sub.outputSite() != null) {
                            directMaterials.add(new MaterialClaim(sub.producedResource(), totalNeed, sub.outputSite()));
                        } else if (!sub.materialClaims().isEmpty()) {
                            directMaterials.addAll(sub.materialClaims());
                        } else {
                            possible = false; break;
                        }
                    }
                    if (!possible) continue;

                    bestTimes = tryTimes;
                    bestProduce = tryTimes * perCraft;
                    bestSnapshot = snapshot;
                    bestSteps = subSteps;
                    bestMaterials = directMaterials;
                    if (bestProduce >= need) break;
                }

                if (bestTimes == 0) continue;

                CraftStepDetail thisStep = new CraftStepDetail(holder.id(), site, bestTimes, bestMaterials, List.of());
                List<CraftStepDetail> allSteps = new ArrayList<>(bestSteps);
                allSteps.add(thisStep);

                int totalCost = bestTimes + bestSteps.size();
                if (totalCost < bestCost) {
                    bestCost = totalCost;
                    pool.apply(bestSnapshot);
                    bestResult = new PlanResult(Math.min(need, bestProduce), allSteps, target, site);
                }
            }

            visited.remove(target.getItem()); // 回溯
            return bestResult;
        }

        private int getTotalCost(ItemResource item, DetailedSuppliesPool pool,
                                 Map<BlockPos, RecipeType<?>> sites, ServerLevel level,
                                 Set<Item> visited) {
            if (pool.getAvailable(item) > 0) return 0;
            if (!visited.add(item.getItem())) return Integer.MAX_VALUE;
            List<RecipeHolder<?>> recipes = getRecipesFor(item.getItem(), null);
            if (recipes.isEmpty()) {
                visited.remove(item.getItem());
                return Integer.MAX_VALUE;
            }
            int bestCost = Integer.MAX_VALUE;
            for (RecipeHolder<?> holder : recipes) {
                Recipe<?> recipe = holder.value();
                if (sites.values().stream().noneMatch(t -> t == recipe.getType())) continue;
                int cost = 1;
                boolean possible = true;
                for (Ingredient ing : recipe.placementInfo().ingredients()) {
                    if (ing.isEmpty()) continue;
                    int minIngCost = ing.items()
                            .mapToInt(h -> getTotalCost(ItemResource.of(h.value()), pool, sites, level, visited))
                            .min().orElse(Integer.MAX_VALUE);
                    if (minIngCost == Integer.MAX_VALUE) { possible = false; break; }
                    cost += minIngCost;
                }
                if (possible && cost < bestCost) bestCost = cost;
            }
            visited.remove(item.getItem());
            return bestCost;
        }
    }
