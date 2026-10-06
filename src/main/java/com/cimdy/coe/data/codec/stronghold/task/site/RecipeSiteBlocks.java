package com.cimdy.coe.data.codec.stronghold.task.site;

import com.cimdy.coe.data.codec.stronghold.task.site.impl.process.RecipeSite;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 「配方类型 → 合成所需方块」的数据驱动映射表。
 * <p>
 * 无需为每种配方类型各注册一个专用站点类：只需在数据包中声明
 * {@code recipe_type → blocks} 的映射（见 {@code data/coe/coe/recipe_site/*.json}），
 * 由 {@link com.cimdy.coe.data.codec.stronghold.task.site.impl.process.RecipeSite} 这一个通用站点
 * 依据方块反查出对应 {@link RecipeType}，即可让工人系统在该站点执行对应类型的全部配方。
 * <p>
 * 运行时表由 {@code LoadEvent} 的重载监听器（{@code AddServerReloadListenersEvent}）填充；
 * 原版映射由 {@code LoadEvent} 的数据生成（{@code registerRecipeSite}）产出。
 */
public final class RecipeSiteBlocks {
    private RecipeSiteBlocks() {}

    /** 运行时反查表：方块 → 配方类型。由数据包重载填充。 */
    private static final Map<Block, RecipeType<?>> BLOCK_TO_TYPE = new HashMap<>();

    public static synchronized void clear() {
        BLOCK_TO_TYPE.clear();
    }

    public static synchronized void register(RecipeSiteMapping mapping) {
        for (Block block : mapping.blocks()) {
            BLOCK_TO_TYPE.put(block, mapping.recipeType());
        }
    }

    /** 该方块是否为任一已声明的「配方站点方块」。 */
    public static boolean isRecipeBlock(Block block) {
        return BLOCK_TO_TYPE.containsKey(block);
    }

    public static boolean isRecipeBlock(BlockState state) {
        return isRecipeBlock(state.getBlock());
    }

    /** 反查该方块对应的配方类型；未声明时返回 null。 */
    public static RecipeType<?> getRecipeType(Block block) {
        return BLOCK_TO_TYPE.get(block);
    }

    public static RecipeType<?> getRecipeType(BlockState state) {
        return getRecipeType(state.getBlock());
    }

    /**
     * 单条「配方类型 → 方块列表」映射（数据驱动条目）。
     * 一种配方类型可对应多个方块（如营火烹饪对应营火与灵魂营火）。
     */
    public record RecipeSiteMapping(RecipeType<?> recipeType, List<Block> blocks) {
        public static final Codec<RecipeSiteMapping> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.RECIPE_TYPE.byNameCodec().fieldOf("recipe_type").forGetter(RecipeSiteMapping::recipeType),
                BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("blocks").forGetter(RecipeSiteMapping::blocks)
        ).apply(instance, RecipeSiteMapping::new));
    }
}
