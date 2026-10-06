package com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant;

import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.process.RecipeSite;
import com.cimdy.coe.entity.goal.stronghold.impl.work.misc.EnchantGoal;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.process.EnchanterSite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;
import java.util.function.Predicate;

/**
 * 附魔应用类型注册表（与 {@link ConvertType} 同构，但产物带附魔 NBT）。
 * <p>
 * 附魔不属于原版「配方」类型（用户明确：附魔站点使用附魔书生成附魔物品），故不走
 * {@code RecipeSite} 通用管线，也不走 {@code ConvertType}（其 Goal 会丢弃产物组件），
 * 而由专门的 {@code EnchantType} + {@code EnchantTask} + {@code EnchantGoal} 承载。
 * <p>
 * 每条规格描述一次「附魔书应用」：消耗 <b>附魔书 + 目标物品 + 青金石 × lapisCost + 末影能量</b>，
 * 产出附魔后的目标物品。末影能量消耗由据点账本（{@code StrongholdHelper}）按终端坐标结算。
 * <p>
 * 附魔书的生成（产生附魔书本身）本版暂不实现——本注册表只负责「用已有附魔书给物品附魔」。
 */
public enum EnchantType {
    APPLIER("applier", List.of(EnchanterSite.class), EnchanterSite.SITE_BLOCK_PREDICATE, List.of(
            EnchantSpec.create(Items.DIAMOND_PICKAXE, Enchantments.EFFICIENCY, 1, 1, 50),
            EnchantSpec.create(Items.DIAMOND_PICKAXE, Enchantments.UNBREAKING, 1, 1, 50),
            EnchantSpec.create(Items.DIAMOND_PICKAXE, Enchantments.FORTUNE, 1, 1, 50),
            EnchantSpec.create(Items.DIAMOND_PICKAXE, Enchantments.SILK_TOUCH, 1, 1, 60),
            EnchantSpec.create(Items.NETHERITE_PICKAXE, Enchantments.EFFICIENCY, 1, 1, 50),
            EnchantSpec.create(Items.NETHERITE_SWORD, Enchantments.SHARPNESS, 1, 1, 50),
            EnchantSpec.create(Items.NETHERITE_SWORD, Enchantments.UNBREAKING, 1, 1, 50),
            EnchantSpec.create(Items.DIAMOND_CHESTPLATE, Enchantments.PROTECTION, 1, 1, 50),
            EnchantSpec.create(Items.DIAMOND_BOOTS, Enchantments.FEATHER_FALLING, 1, 1, 50),
            EnchantSpec.create(Items.BOW, Enchantments.POWER, 1, 1, 50),
            EnchantSpec.create(Items.FISHING_ROD, Enchantments.LUCK_OF_THE_SEA, 1, 1, 50)
    ));

    private final String name;
    private final List<Class<? extends Site>> siteTypes;
    private final Predicate<BlockState> blockPredicate;
    private final List<EnchantSpec> specs;

    EnchantType(String name, List<Class<? extends Site>> siteTypes, Predicate<BlockState> blockPredicate, List<EnchantSpec> specs) {
        this.name = name;
        this.siteTypes = siteTypes;
        this.blockPredicate = blockPredicate;
        this.specs = specs;
    }

    public String getName() { return name; }
    /** 该附魔类型可发生的所有站点类型（一个类型可对应多个站点）。 */
    public List<Class<? extends Site>> getSiteTypes() { return siteTypes; }
    /** 兼容旧调用：返回首个站点类型。 */
    public Class<? extends Site> getSiteType() { return siteTypes.get(0); }
    public Predicate<BlockState> getBlockPredicate() { return blockPredicate; }
    public List<EnchantSpec> getSpecs() { return specs; }

    /** 站点是否可被本附魔类型使用（匹配任一声明站点类型）。 */
    public boolean matchesSite(Site site) {
        for (Class<? extends Site> t : siteTypes) if (t.isInstance(site)) return true;
        return false;
    }

    public static EnchantType byName(String name) {
        for (EnchantType t : values()) if (t.name.equals(name)) return t;
        throw new IllegalArgumentException("Unknown enchant type: " + name);
    }

    public static List<EnchantType> all() { return List.of(values()); }

    /**
     * 单条附魔应用规格：消耗「附魔书 + 目标物品 + 青金石 × lapisCost + 末影能量」→ 产出附魔后的目标物品。
     * <p>
     * 解析需要 {@link Level}（据注册表把 {@code ResourceKey<Enchantment>} 还原为 {@code Holder<Enchantment>}），
     * 故所有派生资源都在生成期（持有 ServerLevel）计算好作为 {@link ItemResource} 交给任务，任务本身无需注册表访问。
     */
    public record EnchantSpec(
            Item target,
            ResourceKey<Enchantment> enchantmentKey,
            int level,
            int lapisCost,
            int enderEnergyCost) {
        public static EnchantSpec create(Item target, ResourceKey<Enchantment> enchantmentKey, int level, int lapisCost, int enderEnergyCost) {
            return new EnchantSpec(target, enchantmentKey, level, lapisCost, enderEnergyCost);
        }

        private net.minecraft.core.Holder<Enchantment> holder(Level world) {
            return world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantmentKey);
        }

        /** 所需的附魔书资源（与规格完全一致：物品=书、STORED_ENCHANTMENTS=指定附魔@等级）。 */
        public ItemResource inputBookRes(Level world) {
            return ItemResource.of(EnchantmentHelper.createBook(new EnchantmentInstance(holder(world), level)));
        }

        /** 目标基础物品资源（未附魔）。 */
        public ItemResource baseTargetRes() { return ItemResource.of(new ItemStack(target)); }

        /** 产出资源：目标物品附上指定附魔@等级。 */
        public ItemResource outputRes(Level world) { return ItemResource.of(output(world)); }

        private ItemStack output(Level world) {
            ItemStack out = new ItemStack(target);
            ItemEnchantments.Mutable m = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            m.set(holder(world), level);
            out.set(DataComponents.ENCHANTMENTS, m.toImmutable());
            return out;
        }
    }
}
