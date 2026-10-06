package com.cimdy.coe.data.codec.stronghold.task.site.impl.process;

import com.cimdy.coe.data.codec.stronghold.task.site.RecipeSiteBlocks;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.SiteTypes;
import com.cimdy.coe.data.codec.stronghold.task.site.basic.SinglePointStorageSite;
import com.cimdy.coe.data.codec.stronghold.task.site.basic.StorageSite;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * 数据驱动的通用配方站点。
 * <p>
 * 无需为每种配方类型各注册一个专用站点类（{@code CraftSite}/{@code FurnaceSite}/
 * {@code SmeltSite}/{@code CookSite}/{@code SmokerSite}/{@code StonecutterSite}/{@code SmithingSite}）。
 * 本站点本身不绑定任何具体配方类型：其对应的 {@link RecipeType} 由数据驱动的
 * {@link RecipeSiteBlocks}（「配方类型 → 方块」映射表）在创建时依据方块反查得到。
 * 因此只需在数据包中声明 {@code recipe_type → blocks}（原版映射由 {@code LoadEvent} 的数据生成产出），
 * 即可让工人系统在对应原版工作站执行该类型的全部配方，而无需为每种配方类型注册一个站点类。
 */
public class RecipeSite extends SinglePointStorageSite {
    public static final Codec<RecipeSite> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeSite> STREAM_CODEC;
    /** 该站点是否为「数据驱动的配方站点方块」：由运行时映射表决定。 */
    public static final Predicate<BlockState> SITE_BLOCK_PREDICATE = RecipeSiteBlocks::isRecipeBlock;

    private final RecipeType<?> recipeType;

    public RecipeSite(
            BlockPos workPos, int maxWorkers, boolean autoAssign, int priority,
            List<UUID> workers, int maxStorageBlocks, List<BlockPos> storageBlocks, RecipeType<?> recipeType) {
        super(workPos, maxWorkers, autoAssign, priority, workers, maxStorageBlocks, storageBlocks);
        this.recipeType = recipeType;
    }

    /** 依据方块反查得到的配方类型创建站点（由 {@code SiteTypes.createSiteForBlock} 调用）。 */
    public static RecipeSite create(BlockPos workPos, RecipeType<?> recipeType) {
        return new RecipeSite(workPos, 1, true, 5, List.of(), 1, List.of(), recipeType);
    }

    /**
     * 仅供 {@code SiteTypes.RECIPE_SITE} 注册签名使用（{@code Function<BlockPos, Site>} 所必需）的占位工厂。
     * 其生成的站点 {@code recipeType == null}，属损坏状态；实际配方站点一律由
     * {@code SiteTypes.createSiteForBlock} 的特殊分支经 {@link #create(BlockPos, RecipeType)} 创建，
     * 且该循环已显式跳过 {@code RECIPE_SITE}，故本方法在运行时永不应被调用。
     */
    public static RecipeSite create(BlockPos workPos) {
        return new RecipeSite(workPos, 1, true, 5, List.of(), 1, List.of(), null);
    }

    @Override public Component getName() { return Component.translatable("hold.site.coe.recipe"); }
    @Override public Component getDescription() {
        return Component.translatable("hold.site.coe.recipe.description");
    }
    @Override public boolean canSetAssign() {
        return false;
    }
    @Override public Predicate<BlockState> getBlockPredicate() {
        return SITE_BLOCK_PREDICATE;
    }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Site> getStreamCodec() {
        return STREAM_CODEC;
    }
    @Override public Type<?> getType() {
        return SiteTypes.RECIPE_SITE;
    }
    @Override public RecipeType<?> getRecipeType() { return recipeType; }

    private static final StreamCodec<ByteBuf, RecipeType<?>> RECIPE_TYPE_STREAM_CODEC =
            ByteBufCodecs.fromCodec(BuiltInRegistries.RECIPE_TYPE.byNameCodec());

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                sitePosCodec(), maxWorkersCodec(), autoAssignCodec(), priorityCodec(), workersCodec(),
                StorageSite.maxStorageBlocksCodec(), StorageSite.storageBlocksCodec(),
                BuiltInRegistries.RECIPE_TYPE.byNameCodec().fieldOf("recipe_type").forGetter(RecipeSite::getRecipeType)
        ).apply(instance, RecipeSite::new));
        STREAM_CODEC = StreamCodec.composite(
                sitePosStreamCodec(), RecipeSite::getSitePos,
                maxWorkersStreamCodec(), RecipeSite::getMaxWorkers,
                autoAssignStreamCodec(), RecipeSite::isAutoAssign,
                priorityStreamCodec(), RecipeSite::getPriority,
                workersStreamCodec(), RecipeSite::getWorkers,
                StorageSite.maxStorageBlocksStreamCodec(), RecipeSite::getMaxStorage,
                StorageSite.storageStreamCodec(), RecipeSite::getStorages,
                RECIPE_TYPE_STREAM_CODEC, RecipeSite::getRecipeType,
                RecipeSite::new
        );
    }
}
