package com.cimdy.coe.data.gen.world;

import com.cimdy.coe.data.codec.stronghold.task.site.impl.process.RecipeSite;
import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.cimdy.coe.data.codec.stronghold.task.site.RecipeSiteBlocks;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 数据生成：将原版「配方类型 → 合成所需方块」映射写入
 * {@code data/coe/coe/recipe_site/*.json}（位于本模组自定义数据根 {@code data/coe/coe/}，非标准 datapack 的 data/coe/data/）。
 * 运行时由 {@code LoadEvent} 的重载监听器读取并填充
 * {@link RecipeSiteBlocks} 的运行时反查表，使通用 {@code RecipeSite} 能依据方块反查配方类型。
 */
public class RecipeSiteProvider implements DataProvider {
    private final PackOutput output;

    public RecipeSiteProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path root = CoeDataPaths.coeRoot(output);
        List<CompletableFuture<?>> futures = new java.util.ArrayList<>();
        for (RecipeSiteBlocks.RecipeSiteMapping mapping : vanillaMappings()) {
            DataResult<JsonElement> result = RecipeSiteBlocks.RecipeSiteMapping.CODEC
                    .encodeStart(JsonOps.INSTANCE, mapping);
            JsonElement json = result.result().orElseThrow();
            String name = BuiltInRegistries.RECIPE_TYPE.getKey(mapping.recipeType()).getPath();
            Path path = root
                    .resolve("recipe_site")
                    .resolve(name + ".json");
            futures.add(DataProvider.saveStable(cache, json, path));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /** 原版配方类型与其对应工作方块的默认映射。 */
    private static List<RecipeSiteBlocks.RecipeSiteMapping> vanillaMappings() {
        return List.of(
                new RecipeSiteBlocks.RecipeSiteMapping(RecipeType.CRAFTING, List.of(Blocks.CRAFTING_TABLE)),
                new RecipeSiteBlocks.RecipeSiteMapping(RecipeType.SMELTING, List.of(Blocks.FURNACE)),
                new RecipeSiteBlocks.RecipeSiteMapping(RecipeType.BLASTING, List.of(Blocks.BLAST_FURNACE)),
                new RecipeSiteBlocks.RecipeSiteMapping(RecipeType.SMOKING, List.of(Blocks.SMOKER)),
                new RecipeSiteBlocks.RecipeSiteMapping(RecipeType.CAMPFIRE_COOKING, List.of(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE)),
                new RecipeSiteBlocks.RecipeSiteMapping(RecipeType.STONECUTTING, List.of(Blocks.STONECUTTER)),
                new RecipeSiteBlocks.RecipeSiteMapping(RecipeType.SMITHING, List.of(Blocks.SMITHING_TABLE))
        );
    }

    @Override
    public String getName() {
        return "Recipe Site Mappings";
    }
}
