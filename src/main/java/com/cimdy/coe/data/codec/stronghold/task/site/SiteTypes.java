package com.cimdy.coe.data.codec.stronghold.task.site;

import com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant.ConvertType;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant.EnchantType;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.harvest.*;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.process.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class SiteTypes {
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "site");
    static final Map<Identifier, StreamCodec<RegistryFriendlyByteBuf, ? extends Site>> STREAM_CODECS = new HashMap<>();
    static final Map<Site.Type<?>, Identifier> TYPE_TO_ID = new HashMap<>();

    public static final ResourceKey<Registry<Site.Type<?>>> KEY = ResourceKey.createRegistryKey(IDENTIFIER);
    public static final Registry<Site.Type<?>> REGISTRY = new RegistryBuilder<>(KEY).sync(true).defaultKey(IDENTIFIER).maxId(256).create();

    // ---------- 注册站点类型 ----------
    public static final Site.Type<ForestrySite> FORESTRY_SITE = register("forestry_site",
            ForestrySite.CODEC, ForestrySite.STREAM_CODEC, ForestrySite.SITE_BLOCK_PREDICATE, ForestrySite::create);

    public static final Site.Type<AgricultureSite> AGRICULTURE_SITE = register("agriculture_site",
            AgricultureSite.CODEC, AgricultureSite.STREAM_CODEC, AgricultureSite.SITE_BLOCK_PREDICATE, AgricultureSite::create);

    public static final Site.Type<RecipeSite> RECIPE_SITE = register("recipe_site",
            RecipeSite.CODEC, RecipeSite.STREAM_CODEC, RecipeSite.SITE_BLOCK_PREDICATE, RecipeSite::create);

    public static final Site.Type<EnderChargeSite> ENDER_CHARGE_SITE = register("ender_charge_site",
            EnderChargeSite.codec(), EnderChargeSite.streamCodec(), EnderChargeSite.predicate(), EnderChargeSite::create);

    // ---------- 扩展站点类型（参考模拟殖民地 / 幻兽帕鲁的基建工作类型） ----------
    public static final Site.Type<MiningSite> MINING_SITE = register("mining_site",
            MiningSite.CODEC, MiningSite.STREAM_CODEC, MiningSite.SITE_BLOCK_PREDICATE, MiningSite::create);

    public static final Site.Type<ShearSite> SHEAR_SITE = register("shear_site",
            ShearSite.CODEC, ShearSite.STREAM_CODEC, ShearSite.SITE_BLOCK_PREDICATE, ShearSite::create);

    public static final Site.Type<HuntSite> HUNT_SITE = register("hunt_site",
            HuntSite.CODEC, HuntSite.STREAM_CODEC, HuntSite.SITE_BLOCK_PREDICATE, HuntSite::create);

    public static final Site.Type<ComposterSite> COMPOSTER_SITE = register("composter_site",
            ComposterSite.CODEC, ComposterSite.STREAM_CODEC, ComposterSite.SITE_BLOCK_PREDICATE, ComposterSite::create);

    public static final Site.Type<MilkSite> MILK_SITE = register("milk_site",
            MilkSite.CODEC, MilkSite.STREAM_CODEC, MilkSite.SITE_BLOCK_PREDICATE, MilkSite::create);

    public static final Site.Type<FisherSite> FISHER_SITE = register("fisher_site",
            FisherSite.CODEC, FisherSite.STREAM_CODEC, FisherSite.SITE_BLOCK_PREDICATE, FisherSite::create);

    public static final Site.Type<MillSite> MILL_SITE = register("mill_site",
            MillSite.CODEC, MillSite.STREAM_CODEC, MillSite.SITE_BLOCK_PREDICATE, MillSite::create);

    /**
     * 粉碎站（无原版配方的石料加工）：以炼药锅为标记方块，将圆石/沙子/石料逐级粉碎或回收为构成物。
     * 走 {@code ConvertType.CRUSHER} 管线，与堆肥/研磨同构。
     */
    public static final Site.Type<CrusherSite> CRUSHER_SITE = register("crusher_site",
            CrusherSite.CODEC, CrusherSite.STREAM_CODEC, CrusherSite.SITE_BLOCK_PREDICATE, CrusherSite::create);

    /**
     * 附魔站（使用附魔书给物品附魔，消耗青金石与末影能量）：以附魔台为标记方块。
     * 走 {@code EnchantType} 管线，与堆肥/研磨/粉碎同构。
     */
    public static final Site.Type<EnchanterSite> ENCHANTER_SITE = register("enchanter_site",
            EnchanterSite.CODEC, EnchanterSite.STREAM_CODEC, EnchanterSite.SITE_BLOCK_PREDICATE, EnchanterSite::create);

    public static <T extends Site> Site.Type<T> register(
            String name,
            Codec<T> codec,
            StreamCodec<RegistryFriendlyByteBuf, T> streamCodec,
            Predicate<BlockState> predicate,
            Function<BlockPos, T> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "site_" + name);
        Site.Type<T> type = Registry.register(REGISTRY, id, new Site.Type<>(codec, streamCodec, predicate, factory));
        STREAM_CODECS.put(id, streamCodec);
        TYPE_TO_ID.put(type, id);
        return type;
    }

    // ---------- 辅助查询 ----------

    /**
     * 站点类型 → 工种名（进度系统的唯一工种标识）。
     *
     * <p>注册 id 形如 {@code coe:site_mining_site}，两端的 {@code site} 只是注册表命名习惯，
     * 对玩家和数据包作者都是噪音。此处剥离成 {@code mining}，使
     * {@code job_charter} / {@code tech_node} / {@code research_node} 三处 JSON
     * 里的 {@code work_type} 字段可以写人话，并与 {@code work_type.coe.<名>} 语言键一一对应。
     */
    public static String workTypeName(Site.Type<?> type) {
        Identifier id = REGISTRY.getKey(type);
        if (id == null) return "";
        String path = id.getPath();
        if (path.startsWith("site_")) path = path.substring("site_".length());
        if (path.endsWith("_site")) path = path.substring(0, path.length() - "_site".length());
        return path;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Site> StreamCodec<RegistryFriendlyByteBuf, T> getStreamCodec(Site.Type<T> type) {
        Identifier id = REGISTRY.getKey(type);
        return (StreamCodec<RegistryFriendlyByteBuf, T>) STREAM_CODECS.get(id);
    }

    public static boolean isSiteBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        for (Site.Type<?> type : REGISTRY) {
            Predicate<BlockState> pred = type.blockPredicate();
            if (pred != null && pred.test(state)) {
                return true;
            }
        }
        return false;
    }

    /** 根据方块状态自动寻找匹配的站点类型并创建站点 */
    public static Site createSiteForBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // 数据驱动的配方站点：先依据方块反查配方类型，由通用 RecipeSite 接管，
        // 从而无需为每种配方类型都注册一个专用站点类。
        RecipeType<?> recipeType = RecipeSiteBlocks.getRecipeType(state);
        if (recipeType != null) {
            // 配方站点优先级最高，会把其它专用站点「遮蔽」掉 —— 被遮蔽者将永远无法创建
            // （历史上 MiningSite 正是被锻造台的配方站点遮蔽，导致该站点完全不可用）。
            // 此处收集被遮蔽者并告警，避免同类冲突再次静默发生。
            List<String> shadowed = new ArrayList<>();
            for (Site.Type<?> type : SiteTypes.REGISTRY) {
                if (type == RECIPE_SITE) continue;
                if (type.blockPredicate().test(state)) shadowed.add(idOf(type));
            }
            if (!shadowed.isEmpty()) {
                CraftOfExile.LOGGER.warn("站点标记方块冲突：{} 已被配方站点接管，以下专用类型永远无法创建：{}",
                        BuiltInRegistries.BLOCK.getKey(state.getBlock()), shadowed);
            }
            return RecipeSite.create(pos, recipeType);
        }
        Site created = null;
        List<String> hits = new ArrayList<>();
        for (Site.Type<?> type : SiteTypes.REGISTRY) {
            // 配方站点已由上方特殊分支（依据方块反查配方类型）创建，其工厂为「无配方类型」占位，
            // 绝不可经此循环调用，否则会生成 recipeType=null 的损坏站点。故显式跳过。
            if (type == RECIPE_SITE) continue;
            if (type.blockPredicate().test(state)) {
                // 仅首个命中者真正创建站点，其余只登记用于冲突告警。
                if (created == null) created = type.factory().apply(pos);
                hits.add(idOf(type));
            }
        }
        if (hits.size() > 1) {
            CraftOfExile.LOGGER.warn("站点标记方块冲突：{} 同时匹配 {}，实际只会创建 {}",
                    BuiltInRegistries.BLOCK.getKey(state.getBlock()), hits, hits.get(0));
        }
        return created;
    }

    private static String idOf(Site.Type<?> type) {
        Identifier id = TYPE_TO_ID.get(type);
        return id == null ? "unknown" : id.getPath();
    }
}