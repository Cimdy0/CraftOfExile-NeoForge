package com.cimdy.coe.data.helper.skill;

import com.cimdy.coe.event.skill.TalentPerkEvent;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 天赋「工具能力」的机制实现（原 {@code event.TalentPerkEvent} 内联逻辑，铁律 #56 迁出）。
 *
 * <p>「能力(Perk)」机制统一走属性系统：天赋节点通过属性修正写入 {@code TALENT_*} 属性，
 * 此处按属性值真实改变游戏行为。全部逻辑仅在服务端执行。
 */
public final class TalentPerkHelper {
    private TalentPerkHelper() {
    }

    /** 读取玩家某属性的值（属性不存在或玩家为空时返回 0）。 */
    private static double attrVal(Player player, Holder<Attribute> attr) {
        if (player == null || !player.getAttributes().hasAttribute(attr)) return 0;
        return player.getAttributeValue(attr);
    }

    /** FOUND_ITEM_NUMBER / FOUND_ITEM_RARITY（基 0）：掉落数量/稀有度倍率（FOLD-KEEP），读击杀者属性。 */
    private static double readFoundItemMult(Player player, Holder<Attribute> attr) {
        if (player == null) return 0.0;
        try (var scope = StatScopes.enter(player, StatContext.of(Set.of(), null))) {
            return player.getAttributeValue(attr);
        } catch (Exception ignored) {
        }
        return 0.0;
    }

    /** 破坏方块掉落：自动熔炼（{@code TALENT_AUTO_SMELT}）+ 每种掉落额外 +N（{@code TALENT_FORTUNE}）。 */
    public static void onBlockDrops(Entity breaker, ServerLevel level, Collection<ItemEntity> drops) {
        if (!(breaker instanceof Player player)) return;
        int autoSmelt = (int) Math.floor(attrVal(player, AttributeRegistry.TALENT_AUTO_SMELT));
        int fortune = (int) Math.floor(attrVal(player, AttributeRegistry.TALENT_FORTUNE));
        if (autoSmelt <= 0 && fortune <= 0) return;

        List<ItemEntity> extra = new ArrayList<>();
        for (ItemEntity ie : drops) {
            ItemStack stack = ie.getItem();
            if (stack.isEmpty()) continue;
            if (autoSmelt > 0) {
                ItemStack smelted = smelt(level, stack);
                if (!smelted.isEmpty()) {
                    ie.setItem(smelted.copyWithCount(stack.getCount()));
                }
            }
            if (fortune > 0) {
                int add = Math.min(fortune, stack.getMaxStackSize() - stack.getCount());
                if (add > 0) {
                    ItemEntity e = new ItemEntity(level, ie.getX(), ie.getY(), ie.getZ(), stack.copyWithCount(add));
                    extra.add(e);
                }
            }
        }
        drops.addAll(extra);
    }

    /** 击杀生物掉落：{@code TALENT_LOOTING} + FOUND_ITEM_NUMBER / FOUND_ITEM_RARITY。 */
    public static void onLivingDrops(Entity killer, Collection<ItemEntity> drops) {
        if (!(killer instanceof Player player)) return;

        // FOUND_ITEM_NUMBER / FOUND_ITEM_RARITY（基 0）：掉落数量/稀有度倍率（FOLD-KEEP，读击杀者属性）。
        // NUMBER 为掉落数量倍率（规范效果）；RARITY 暂以「额外掉落数量」近似（掉落稀有度重投需 loot-rarity 钩子，
        // 见 ATTRIBUTE_FOLD_VS_FIRE_AUDIT §5.4，转 LootStatEffect 后再落实），保持属性被消费、不污染既有物品。
        double foundNumber = readFoundItemMult(player, AttributeRegistry.FOUND_ITEM_NUMBER);
        double foundRarity = readFoundItemMult(player, AttributeRegistry.FOUND_ITEM_RARITY);

        int looting = (int) Math.floor(attrVal(player, AttributeRegistry.TALENT_LOOTING));
        if (looting <= 0 && foundNumber <= 0 && foundRarity <= 0) return;

        List<ItemEntity> extra = new ArrayList<>();
        for (ItemEntity ie : drops) {
            ItemStack stack = ie.getItem();
            if (stack.isEmpty()) continue;
            int max = stack.getMaxStackSize() - stack.getCount();
            if (max <= 0) continue;
            int add = 0;
            if (looting > 0) add += Math.min(looting, max);
            if (foundNumber > 0) add += (int) Math.floor(stack.getCount() * foundNumber);
            if (foundRarity > 0) add += (int) Math.floor(stack.getCount() * foundRarity); // 临时近似：待 loot-rarity 钩子
            add = Math.min(add, max);
            if (add > 0) {
                extra.add(new ItemEntity(player.level(), ie.getX(), ie.getY(), ie.getZ(), stack.copyWithCount(add)));
            }
        }
        drops.addAll(extra);
    }

    /** 拾取经验球：{@code TALENT_EXTRA_XP} 额外经验 + {@code TALENT_MENDING} 经验修补。 */
    public static void onPickupXp(Player player, ExperienceOrb orb) {
        int extraXp = (int) Math.floor(attrVal(player, AttributeRegistry.TALENT_EXTRA_XP));
        int mending = (int) Math.floor(attrVal(player, AttributeRegistry.TALENT_MENDING));
        if (extraXp <= 0 && mending <= 0) return;

        int xp = orb.getValue();
        if (extraXp > 0) {
            int bonus = (int) Math.round(xp * 0.25 * extraXp);
            if (bonus > 0) orb.setValue(xp + bonus);
        }
        if (mending > 0) {
            repair(player, xp * mending);
        }
    }

    /**
     * 天赋「磁铁」：玩家每 tick 把周围一定半径内的掉落物与经验球吸向自己（半径随等级增大）。
     * 仅拉近、不强制入包，真正拾取仍由 MC 原生逻辑完成，避免背包满时吞物。
     */
    public static void onPlayerTick(Player player) {
        int magnet = (int) Math.floor(attrVal(player, AttributeRegistry.TALENT_MAGNET));
        if (magnet <= 0) return;

        double radius = 4.0 + magnet * 2.0;
        AABB box = player.getBoundingBox().inflate(radius);
        Vec3 target = player.position().add(0, 0.6, 0);

        // 获取实体统一经 Context 数据基站回调（EntityQuery 包围盒扫描；非生物实体亦支持）
        ExpressionContext base = ExpressionContext.get(player);
        for (ItemEntity ie : base.queryEntities(EntityQuery.box(ItemEntity.class, box))) {
            if (ie.isRemoved()) continue;
            ie.setNoPickUpDelay();
            Vec3 dir = target.subtract(ie.position());
            double d = dir.length();
            if (d > 0.1) {
                double speed = Math.min(0.6, 0.05 + d * 0.08);
                ie.setDeltaMovement(dir.normalize().scale(speed));
            }
        }
        for (ExperienceOrb orb : base.queryEntities(EntityQuery.box(ExperienceOrb.class, box))) {
            Vec3 dir = target.subtract(orb.position());
            double d = dir.length();
            if (d > 0.1) {
                double speed = Math.min(0.6, 0.05 + d * 0.08);
                orb.setDeltaMovement(dir.normalize().scale(speed));
            }
        }
    }

    private static ItemStack smelt(ServerLevel level, ItemStack input) {
        Optional<RecipeHolder<SmeltingRecipe>> recipe =
                level.getServer().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
        if (recipe.isEmpty()) return ItemStack.EMPTY;
        return recipe.get().value().assemble(new SingleRecipeInput(input));
    }

    /** 用给定经验量优先修复主手 → 副手 → 护甲（类似经验修补）。 */
    private static void repair(Player player, int amount) {
        amount = repairStack(player.getMainHandItem(), amount);
        amount = repairStack(player.getOffhandItem(), amount);
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (amount <= 0) break;
            ItemStack stack = player.getItemBySlot(slot);
            int before = amount;
            amount = repairStack(stack, amount);
            if (amount != before) player.setItemSlot(slot, stack);
        }
    }

    private static int repairStack(ItemStack stack, int amount) {
        if (stack.isEmpty() || !stack.isDamageableItem() || !stack.isDamaged()) return amount;
        int dmg = stack.getDamageValue();
        int repair = Math.min(dmg, amount);
        stack.setDamageValue(dmg - repair);
        return amount - repair;
    }
}
