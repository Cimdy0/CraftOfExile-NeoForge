package com.cimdy.coe;

import com.cimdy.coe.data.codec.support.rarity.Rarity;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

@EventBusSubscriber(modid = CraftOfExile.MODID)
public class CoeConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.IntValue RARITY_WEIGHT_UNIQUE = BUILDER
            .comment("Weight for naturally-spawned entities of Unique rarity")
            .defineInRange("UniquePreset", 5, 0, 10000);
    private static final ModConfigSpec.IntValue UNIQUE_MIN_MOD = BUILDER
            .comment("Minimum number of modifiers a naturally-spawned Unique entity can have")
            .defineInRange("UniqueModMin", 0, 0, 10);
    private static final ModConfigSpec.IntValue UNIQUE_MAX_MOD = BUILDER
            .comment("Maximum number of modifiers a naturally-spawned Unique entity can have")
            .defineInRange("UniqueModMax", 0, 0, 10);

    private static final ModConfigSpec.IntValue RARITY_WEIGHT_RARE = BUILDER
            .comment("Weight for naturally-spawned entities of Rare rarity")
            .defineInRange("Rare", 15, 0, 10000);
    private static final ModConfigSpec.IntValue RARE_MIN_MOD = BUILDER
            .comment("Minimum number of modifiers a naturally-spawned Rare entity can have")
            .defineInRange("RareModMin", 2, 0, 10);
    private static final ModConfigSpec.IntValue RARE_MAX_MOD = BUILDER
            .comment("Maximum number of modifiers a naturally-spawned Rare entity can have")
            .defineInRange("RareModMax", 4, 0, 10);

    private static final ModConfigSpec.IntValue RARITY_WEIGHT_MAGIC = BUILDER
            .comment("Weight for naturally-spawned entities of Magic rarity")
            .defineInRange("Magic", 30, 0, 10000);
    private static final ModConfigSpec.IntValue MAGIC_MIN_MOD = BUILDER
            .comment("Minimum number of modifiers a naturally-spawned Magic entity can have")
            .defineInRange("MagicModMin", 1, 0, 10);
    private static final ModConfigSpec.IntValue MAGIC_MAX_MOD = BUILDER
            .comment("Maximum number of modifiers a naturally-spawned Magic entity can have")
            .defineInRange("MagicModMax", 1, 0, 10);

    private static final ModConfigSpec.IntValue RARITY_WEIGHT_COMMON = BUILDER
            .comment("Weight for naturally-spawned entities of Common rarity")
            .defineInRange("Common", 50, 0, 10000);
    private static final ModConfigSpec.IntValue COMMON_MIN_MOD = BUILDER
            .comment("Minimum number of modifiers a naturally-spawned Common entity can have")
            .defineInRange("CommonModMin", 0, 0, 10);
    private static final ModConfigSpec.IntValue COMMON_MAX_MOD = BUILDER
            .comment("Maximum number of modifiers a naturally-spawned Common entity can have")
            .defineInRange("CommonModMax", 0, 0, 10);

    private static final ModConfigSpec.DoubleValue PHYSICAL_DEVIATION = BUILDER
            .comment("Deviation of physical damage")
            .defineInRange("PhysicalDeviation", 0.3, 0, 1);
    private static final ModConfigSpec.DoubleValue CHAOS_DEVIATION = BUILDER
            .comment("Deviation of chaos damage")
            .defineInRange("ChaosDeviation", 0.4, 0, 1);
    private static final ModConfigSpec.DoubleValue FIRE_DEVIATION = BUILDER
            .comment("Deviation of fire damage")
            .defineInRange("FireDeviation", 0.6, 0, 1);
    private static final ModConfigSpec.DoubleValue COLD_DEVIATION = BUILDER
            .comment("Deviation of cold damage")
            .defineInRange("ColdDeviation", 0.6, 0, 1);
    private static final ModConfigSpec.DoubleValue LIGHT_DEVIATION = BUILDER
            .comment("Deviation of lightning damage")
            .defineInRange("LightDeviation", 0.9, 0, 1);

    private static final ModConfigSpec.IntValue RENDER_RESISTANCE_MAX = BUILDER
            .comment("Resistance value above which an entity is flagged")
            .defineInRange("max_value", 30, -200, 90);
    private static final ModConfigSpec.IntValue RENDER_RESISTANCE_MIN = BUILDER
            .comment("Resistance value below which an entity is flagged")
            .defineInRange("min_value", -10, -200, 90);

    private static final ModConfigSpec.BooleanValue ENTITY_MODIFIER_OVERLOAD = BUILDER
            .comment("Enable entity modifier reload; when on, entity modifiers update with datapacks")
            .define("entityModifierOverload", true);

    private static final ModConfigSpec.IntValue ITEM_RARITY_WEIGHT_UNIQUE = BUILDER
            .comment("Weight for generated items of Unique rarity")
            .defineInRange("UniquePreset", 10, 0, 10000);
    private static final ModConfigSpec.IntValue ITEM_RARITY_WEIGHT_RARE = BUILDER
            .comment("Weight for generated items of Rare rarity")
            .defineInRange("Rare", 490, 0, 10000);
    private static final ModConfigSpec.IntValue ITEM_RARITY_WEIGHT_MAGIC = BUILDER
            .comment("Weight for generated items of Magic rarity")
            .defineInRange("Magic", 3500, 0, 10000);
    private static final ModConfigSpec.IntValue ITEM_RARITY_WEIGHT_COMMON = BUILDER
            .comment("Weight for generated items of Common rarity")
            .defineInRange("Common", 6000, 0, 10000);

    // -- Quality system (global config-nature tunables; see QualityOps) --
    private static final ModConfigSpec.IntValue MAX_QUALITY = BUILDER
            .comment("Quality cap for items (POE2: 20%)")
            .defineInRange("maxQuality", 20, 0, 1000);
    private static final ModConfigSpec.IntValue QUALITY_GEM_GRANT = BUILDER
            .comment("Quality granted to skill gems by Gemcutter's Prism (POE2: 5%)")
            .defineInRange("qualityGemGrant", 5, 0, 1000);
    private static final ModConfigSpec.IntValue QUALITY_GRANT_COMMON = BUILDER
            .comment("Quality granted to normal/common items by quality currency (POE2: 5%)")
            .defineInRange("qualityGrantCommon", 5, 0, 1000);
    private static final ModConfigSpec.IntValue QUALITY_GRANT_MAGIC = BUILDER
            .comment("Quality granted to magic items by quality currency (POE2: 2%)")
            .defineInRange("qualityGrantMagic", 2, 0, 1000);
    private static final ModConfigSpec.IntValue QUALITY_GRANT_RARE = BUILDER
            .comment("Quality granted to rare/unique items by quality currency (POE2: 1%)")
            .defineInRange("qualityGrantRare", 1, 0, 1000);

    // -- Item affix-count tiers (global config-nature tunables; drive ModNumber default, see ModGenerator) --
    private static final ModConfigSpec.IntValue ITEM_MAGIC_MOD_PREFIX_MAX = BUILDER
            .comment("Max prefix affixes for Magic-rarity generated items (POE2: 1)")
            .defineInRange("itemMagicModPrefixMax", 1, 0, 32);
    private static final ModConfigSpec.IntValue ITEM_MAGIC_MOD_SUFFIX_MAX = BUILDER
            .comment("Max suffix affixes for Magic-rarity generated items (POE2: 1)")
            .defineInRange("itemMagicModSuffixMax", 1, 0, 32);
    private static final ModConfigSpec.IntValue ITEM_MAGIC_MOD_MIN = BUILDER
            .comment("Minimum total affixes for Magic-rarity generated items (POE2: 1)")
            .defineInRange("itemMagicModMin", 1, 0, 32);
    private static final ModConfigSpec.IntValue ITEM_RARE_MOD_PREFIX_MAX = BUILDER
            .comment("Max prefix affixes for Rare-rarity generated items (POE2: 3)")
            .defineInRange("itemRareModPrefixMax", 3, 0, 32);
    private static final ModConfigSpec.IntValue ITEM_RARE_MOD_SUFFIX_MAX = BUILDER
            .comment("Max suffix affixes for Rare-rarity generated items (POE2: 3)")
            .defineInRange("itemRareModSuffixMax", 3, 0, 32);
    private static final ModConfigSpec.IntValue ITEM_RARE_MOD_MIN = BUILDER
            .comment("Minimum total affixes for Rare-rarity generated items (POE2: 4)")
            .defineInRange("itemRareModMin", 4, 0, 32);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static WeightedList<Rarity> rarityWeights;
    public static WeightedList<Rarity> itemRarityWeights;
    public static WeightedList<Rarity> itemRarityWeightsNotCommon;
    public static boolean entityModifierOverload;
    public static int renderResistanceMax;
    public static int renderResistanceMin;
    public static int uniqueMinMod;
    public static int uniqueMaxMod;
    public static int rareMinMod;
    public static int rareMaxMod;
    public static int magicMinMod;
    public static int magicMaxMod;
    public static int commonMinMod;
    public static int commonMaxMod;
    public static double physicalDeviation;
    public static double chaosDeviation;
    public static double fireDeviation;
    public static double coldDeviation;
    public static double lightDeviation;
    public static int maxQuality = 20;
    public static int qualityGemGrant = 5;
    public static int qualityGrantCommon = 5;
    public static int qualityGrantMagic = 2;
    public static int qualityGrantRare = 1;
    public static int itemMagicModPrefixMax = 1;
    public static int itemMagicModSuffixMax = 1;
    public static int itemMagicModMin = 1;
    public static int itemRareModPrefixMax = 3;
    public static int itemRareModSuffixMax = 3;
    public static int itemRareModMin = 4;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        rarityWeights = WeightedList.of(List.of(
                new Weighted<>(Rarity.COMMON, RARITY_WEIGHT_COMMON.get()),
                new Weighted<>(Rarity.MAGIC, RARITY_WEIGHT_MAGIC.get()),
                new Weighted<>(Rarity.RARE, RARITY_WEIGHT_RARE.get()),
                new Weighted<>(Rarity.UNIQUE, RARITY_WEIGHT_UNIQUE.get())));
        itemRarityWeights = WeightedList.of(List.of(
                new Weighted<>(Rarity.COMMON, RARITY_WEIGHT_COMMON.get()),
                new Weighted<>(Rarity.MAGIC, RARITY_WEIGHT_MAGIC.get()),
                new Weighted<>(Rarity.RARE, RARITY_WEIGHT_RARE.get()),
                new Weighted<>(Rarity.UNIQUE, RARITY_WEIGHT_UNIQUE.get())));
        itemRarityWeightsNotCommon = WeightedList.of(List.of(
                new Weighted<>(Rarity.COMMON, RARITY_WEIGHT_COMMON.get()),
                new Weighted<>(Rarity.MAGIC, RARITY_WEIGHT_MAGIC.get()),
                new Weighted<>(Rarity.RARE, RARITY_WEIGHT_RARE.get()),
                new Weighted<>(Rarity.UNIQUE, RARITY_WEIGHT_UNIQUE.get())));
        entityModifierOverload = ENTITY_MODIFIER_OVERLOAD.get();
        renderResistanceMax = RENDER_RESISTANCE_MAX.get();
        renderResistanceMin = RENDER_RESISTANCE_MIN.get();
        uniqueMinMod = UNIQUE_MIN_MOD.get();
        uniqueMaxMod = UNIQUE_MAX_MOD.get();
        rareMinMod = RARE_MIN_MOD.get();
        rareMaxMod = RARE_MAX_MOD.get();
        magicMinMod = MAGIC_MIN_MOD.get();
        magicMaxMod = MAGIC_MAX_MOD.get();
        commonMinMod = COMMON_MIN_MOD.get();
        commonMaxMod = COMMON_MAX_MOD.get();
        physicalDeviation = PHYSICAL_DEVIATION.get();
        chaosDeviation = CHAOS_DEVIATION.get();
        fireDeviation = FIRE_DEVIATION.get();
        coldDeviation = COLD_DEVIATION.get();
        lightDeviation = LIGHT_DEVIATION.get();
        maxQuality = MAX_QUALITY.get();
        qualityGemGrant = QUALITY_GEM_GRANT.get();
        qualityGrantCommon = QUALITY_GRANT_COMMON.get();
        qualityGrantMagic = QUALITY_GRANT_MAGIC.get();
        qualityGrantRare = QUALITY_GRANT_RARE.get();
        itemMagicModPrefixMax = ITEM_MAGIC_MOD_PREFIX_MAX.get();
        itemMagicModSuffixMax = ITEM_MAGIC_MOD_SUFFIX_MAX.get();
        itemMagicModMin = ITEM_MAGIC_MOD_MIN.get();
        itemRareModPrefixMax = ITEM_RARE_MOD_PREFIX_MAX.get();
        itemRareModSuffixMax = ITEM_RARE_MOD_SUFFIX_MAX.get();
        itemRareModMin = ITEM_RARE_MOD_MIN.get();
    }
}
