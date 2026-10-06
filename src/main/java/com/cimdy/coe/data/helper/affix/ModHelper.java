package com.cimdy.coe.data.helper.affix;

import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import com.cimdy.coe.data.codec.item.mod.core.ModLayer;
import com.cimdy.coe.data.helper.mod.ModGenerator;
import com.cimdy.coe.data.helper.mod.ModPoolBuilder;
import com.cimdy.coe.data.helper.mod.AttributeApplicator;
import com.cimdy.coe.data.helper.mod.ModOps;
import com.cimdy.coe.data.helper.mod.EnchantIntegrator;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.cimdy.coe.data.codec.support.rarity.Rarity;

/**
 * 门面（Facade）：保留原有公开静态 API，内部委托给 engine 包下的拆分模块。
 * 拆分目标见 item-system-refactor-plan.md Phase 5。
 */
public class ModHelper {

    public static boolean currentEquipment(LivingEntity living, ItemStack stack, @Nullable String type) {
        return ModGenerator.currentEquipment(living, stack, type);
    }

    public static void setType(ItemStack stack, List<Identifier> types) {
        ModGenerator.setType(stack, types);
    }

    public static List<Identifier> getItemType(ItemStack stack) {
        return ModGenerator.getItemType(stack);
    }

    public static Identifier getFirstItemType(ItemStack stack) {
        return ModGenerator.getFirstItemType(stack);
    }

    public static Map<Location, WeightedList.Builder<CommonMod>> getModBuilder(ItemStack stack, List<Location> types) {
        return ModPoolBuilder.getModBuilder(stack, types);
    }

    /** 词条按层分桶（下缀 / 符文 / 附魔）；只有「前后缀档位计数」才需要按落位看的视图。 */
    public static Map<ModLayer, List<ModifierEntry>> combineModsAndEnchantments(
            ItemStack stack) {
        return AttributeApplicator.combineModsAndEnchantments(stack);
    }

    /** 只含前后缀落位桶的视图（符文 / 附魔不计入前后缀档位）。 */
    public static Map<Location, List<ModifierEntry>> locationBuckets(ItemStack stack) {
        return AttributeApplicator.locationBuckets(stack);
    }

    /** vanilla 附魔条目（平铺列表）：附魔不是词缀，不占前后缀落位。 */
    public static List<ModifierEntry> getEnchantMods(ItemEnchantments enchantments) {
        return EnchantIntegrator.getEnchantMods(enchantments);
    }

    public static boolean isDownNotMaxed(ItemStack stack) {
        return ModOps.isDownNotMaxed(stack);
    }

    public static void applyForceInitializationWithRarity(RandomSource random, ItemStack stack, int level, com.cimdy.coe.data.codec.support.rarity.Rarity rarity) {
        ModGenerator.applyForceInitializationWithRarity(random, stack, level, rarity);
    }

    public static void applyInitialization(RandomSource random, ItemStack stack, int level) {
        ModOps.applyInitialization(random, stack, level);
    }

    public static void applyRegenRandomDownWithRarity(RandomSource random, ItemStack stack, com.cimdy.coe.data.codec.support.rarity.Rarity rarity) {
        ModOps.regenRandomDownWithRarity(random, stack, rarity);
    }

    public static void applyRegenRandomDown(RandomSource random, ItemStack stack) {
        ModOps.regenRandomDown(random, stack);
    }

    public static void applyRegenRandomDown(RandomSource random, ItemStack stack, int minTier) {
        ModOps.regenRandomDown(random, stack, minTier);
    }

    public static boolean applyAddRandomDownMod(RandomSource random, ItemStack stack) {
        return ModOps.addRandomDownMod(random, stack);
    }

    public static boolean applyAddRandomDownMod(RandomSource random, ItemStack stack, int minTier) {
        return ModOps.addRandomDownMod(random, stack, minTier);
    }

    public static void applyRemoveRandomDownMod(Level level, ItemStack stack) {
        ModOps.removeRandomDownMod(level, stack);
    }

    public static void resetItemToNormal(ItemStack stack) {
        ModOps.resetItemToNormal(stack);
    }

    public static boolean hasMods(ItemStack stack) {
        return ModOps.hasMods(stack);
    }

    public static void applyRandomModValue(RandomSource random, ItemStack stack) {
        ModOps.randomModValue(random, stack);
    }

    public static void applyModValue(ItemStack stack) {
        AttributeApplicator.applyModValue(stack);
    }

    /**
     * 卡兰德拉之镜（MIRROR）：克隆目标物品，标记副本为镜像（受 {@code blockMirror} 守卫拦截，
     * 防止对已被镜像物品无限复制），把副本送入玩家背包；原位物品保持不变。
     */
    public static void applyMirror(Player player, ItemStack target) {
        ModOps.applyMirror(player, target);
    }

    public static boolean hasThisEnchantModifier(Holder<Enchantment> enchantment) {
        return EnchantIntegrator.hasThisEnchantModifier(enchantment);
    }
}
