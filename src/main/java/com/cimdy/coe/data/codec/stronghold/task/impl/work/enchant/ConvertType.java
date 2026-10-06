package com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant;

import com.cimdy.coe.entity.goal.stronghold.impl.work.misc.ConvertGoal;
import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.TaskGenerator;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.process.ComposterSite;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.process.CrusherSite;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.process.MillSite;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 自定义转化类型注册表（仅保留<b>无原版配方</b>的「站点原料→产物」变换）。
 * <p>
 * 所有拥有原版配方注册表的站点变换（合成 / 冶炼 / 高炉 / 烟熏 / 营火 / 切石 / 锻造）
 * 已统一交给 {@code TaskGenerator.generateCraftTasks} 依据原版配方注册表驱动——
 * 各站点通过 {@code Site.getRecipeType()} 声明自身对应的 {@code RecipeType}，
 * 由通用管线「搬运原料 → 站点处理 → 搬运产物」生成链，执行器复用 {@code CraftGoal}。
 * <p>
 * 本注册表只保留两类<b>自定义</b>变换（原版不存在对应配方，必须硬编码映射）：
 * <ul>
 *   <li>{@code COMPOSTER}：可堆肥物 → 骨粉</li>
 *   <li>{@code MILL}：染料类原料 → 对应染料</li>
 *   <li>{@code CRUSHER}：石料逐级粉碎 / 回收为构成物（圆石→沙砾→沙子、石→圆石、砂岩→沙等）</li>
 * </ul>
 * 二者仍走与合成同构的「需求/供应 → 生成链 → 搬运/转化/搬运」管线，执行器为 {@code ConvertGoal}。
 */
public enum ConvertType {
    COMPOSTER("composter", List.of(ComposterSite.class), ComposterSite.SITE_BLOCK_PREDICATE, Map.ofEntries(
            Map.entry(Items.WHEAT, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.WHEAT_SEEDS, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.BREAD, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.CARROT, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.GOLDEN_CARROT, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.POTATO, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.BEETROOT, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.BEETROOT_SEEDS, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.APPLE, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.MELON, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.MELON_SLICE, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.MELON_SEEDS, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.PUMPKIN, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.PUMPKIN_SEEDS, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.SWEET_BERRIES, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.GLOW_BERRIES, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.SUGAR_CANE, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.KELP, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.DRIED_KELP, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.BAMBOO, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.OAK_SAPLING, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.SPRUCE_SAPLING, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.BIRCH_SAPLING, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.JUNGLE_SAPLING, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.ACACIA_SAPLING, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.DARK_OAK_SAPLING, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.DANDELION, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.POPPY, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.BLUE_ORCHID, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.ALLIUM, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.AZURE_BLUET, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.OXEYE_DAISY, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.CORNFLOWER, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.LILY_OF_THE_VALLEY, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.FERN, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.SHORT_GRASS, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.TALL_GRASS, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.VINE, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.LILY_PAD, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.CACTUS, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.ROTTEN_FLESH, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.SPIDER_EYE, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.FEATHER, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.EGG, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.LEATHER, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.RABBIT_HIDE, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.STRING, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.BONE, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.GUNPOWDER, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.ROSE_BUSH, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.SUNFLOWER, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.LILAC, new ItemStack(Items.BONE_MEAL)),
            Map.entry(Items.PEONY, new ItemStack(Items.BONE_MEAL))
    )),
    MILL("mill", List.of(MillSite.class), MillSite.SITE_BLOCK_PREDICATE, Map.ofEntries(
            Map.entry(Items.INK_SAC, dye(DyeColor.BLACK)),
            Map.entry(Items.COCOA_BEANS, dye(DyeColor.BROWN)),
            Map.entry(Items.LAPIS_LAZULI, dye(DyeColor.BLUE)),
            Map.entry(Items.BONE_MEAL, dye(DyeColor.WHITE)),
            Map.entry(Items.DANDELION, dye(DyeColor.YELLOW)),
            Map.entry(Items.POPPY, dye(DyeColor.RED)),
            Map.entry(Items.ALLIUM, dye(DyeColor.MAGENTA)),
            Map.entry(Items.AZURE_BLUET, dye(DyeColor.LIGHT_BLUE)),
            Map.entry(Items.ORANGE_TULIP, dye(DyeColor.ORANGE)),
            Map.entry(Items.PINK_TULIP, dye(DyeColor.PINK)),
            Map.entry(Items.OXEYE_DAISY, dye(DyeColor.LIGHT_GRAY)),
            Map.entry(Items.CORNFLOWER, dye(DyeColor.BLUE))
    )),
    /**
     * 粉碎：将石料逐级粉碎或回收为构成物（全部为无原版配方的自定义变换）。
     * 圆石 → 沙砾 → 沙子、石头 → 圆石；砂岩/红砂岩 → 对应沙子；黏土块 → 黏土球；
     * 砖块/地狱砖块 → 对应砖；石英块 → 石英；棱柱石 → 棱柱石碎片。
     */
    CRUSHER("crusher", List.of(CrusherSite.class), CrusherSite.SITE_BLOCK_PREDICATE, Map.ofEntries(
            Map.entry(Items.COBBLESTONE, new ItemStack(Items.GRAVEL)),
            Map.entry(Items.STONE, new ItemStack(Items.COBBLESTONE)),
            Map.entry(Items.GRAVEL, new ItemStack(Items.SAND)),
            Map.entry(Items.SANDSTONE, new ItemStack(Items.SAND)),
            Map.entry(Items.RED_SANDSTONE, new ItemStack(Items.RED_SAND)),
            Map.entry(Items.CLAY, new ItemStack(Items.CLAY_BALL)),
            Map.entry(Items.BRICKS, new ItemStack(Items.BRICK)),
            Map.entry(Items.NETHER_BRICKS, new ItemStack(Items.NETHER_BRICK)),
            Map.entry(Items.QUARTZ_BLOCK, new ItemStack(Items.QUARTZ)),
            Map.entry(Items.PRISMARINE, new ItemStack(Items.PRISMARINE_SHARD)),
            Map.entry(Items.DARK_PRISMARINE, new ItemStack(Items.PRISMARINE_SHARD))
    ));

    private final String name;
    private final List<Class<? extends Site>> siteTypes;
    private final Predicate<BlockState> blockPredicate;
    private final Map<Item, ItemStack> conversionMap;

    ConvertType(String name, List<Class<? extends Site>> siteTypes, Predicate<BlockState> blockPredicate, Map<Item, ItemStack> conversionMap) {
        this.name = name;
        this.siteTypes = siteTypes;
        this.blockPredicate = blockPredicate;
        this.conversionMap = conversionMap;
    }

    public String getName() { return name; }
    /** 该转化类型可发生的所有站点类型（一个转化类型可对应多个站点）。 */
    public List<Class<? extends Site>> getSiteTypes() { return siteTypes; }
    /** 兼容旧调用：返回首个站点类型。 */
    public Class<? extends Site> getSiteType() { return siteTypes.get(0); }
    public Predicate<BlockState> getBlockPredicate() { return blockPredicate; }
    public Map<Item, ItemStack> getConversionMap() { return conversionMap; }

    /** 站点是否可被本转化类型使用（匹配任一声明站点类型）。 */
    public boolean matchesSite(Site site) {
        for (Class<? extends Site> t : siteTypes) if (t.isInstance(site)) return true;
        return false;
    }

    public static ConvertType byName(String name) {
        for (ConvertType t : values()) if (t.name.equals(name)) return t;
        throw new IllegalArgumentException("Unknown convert type: " + name);
    }

    public static List<ConvertType> all() { return List.of(values()); }

    /**
     * 生成带指定颜色的染料物品堆。
     * MC 1.26 起染料合并为单一 {@code Items.DYE}，颜色由 {@code DataComponents.DYE} 组件区分，
     * 故研磨原料 → 对应颜色染料的映射需用带组件的 ItemStack 表达。
     */
    private static ItemStack dye(DyeColor color) {
        return new ItemStack(Items.DYE.pick(color));
    }
}
