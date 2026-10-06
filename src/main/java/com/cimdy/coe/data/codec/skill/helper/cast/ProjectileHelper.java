package com.cimdy.coe.data.codec.skill.helper.cast;

import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.helper.affix.ModHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import com.cimdy.coe.data.codec.support.stat.StatIds;

public class ProjectileHelper {

    public static <T extends Projectile> void spawnProjectile(T projectile, ServerLevel serverLevel, Consumer<T> adapter) {
        adapter.accept(projectile);
        serverLevel.addFreshEntity(projectile);
    }

    protected static List<ItemStack> draw(ItemStack weapon, ItemStack ammo, LivingEntity shooter) {
        if (ammo.isEmpty()) {
            return List.of();
        } else {
            Level var5 = shooter.level();
            int var10000;
            if (var5 instanceof ServerLevel serverlevel) {
                var10000 = EnchantmentHelper.processProjectileCount(serverlevel, weapon, shooter, 1);
            } else {
                var10000 = 1;
            }

            int i = var10000;
            List<ItemStack> list = new ArrayList<>(i);
            ItemStack itemstack1 = ammo.copy();

            for (int j = 0; j < i; ++j) {
                ItemStack itemstack = useAmmo(weapon, j == 0 ? ammo : itemstack1, shooter, j > 0);
                if (!itemstack.isEmpty()) {
                    list.add(itemstack);
                }
            }

            return list;
        }
    }

    protected static ItemStack useAmmo(ItemStack weapon, ItemStack ammo, LivingEntity shooter, boolean intangable) {
        int var10000;
        label35:
        {
            ServerLevel serverlevel;
            label34:
            {
                if (!intangable) {
                    Level var7 = shooter.level();
                    if (var7 instanceof ServerLevel) {
                        serverlevel = (ServerLevel) var7;
                        if (!shooter.hasInfiniteMaterials()) {
                            Item var10 = ammo.getItem();
                            if (!(var10 instanceof ArrowItem ai)) {
                                break label34;
                            }

                            if (!ai.isInfinite(ammo, weapon, shooter)) {
                                break label34;
                            }
                        }
                    }
                }

                var10000 = 0;
                break label35;
            }

            var10000 = EnchantmentHelper.processAmmoUse(serverlevel, weapon, ammo, 1);
        }

        int i = var10000;
        // 弹药有概率不消耗（StatIds.BOLTS_FIRED_BY_CROSSBOW_...）：通用机制，不焊死弩种——
        // 按持有武器的类别构建作用域标签集（projectile 恒在；弩/弓各自追加，镜像 ProjectileInEnchant#modifyProjectileCount），
        // 数据侧标记实例自带 weapon-class 作用域（如 GateTag.CROSSBOW），「近期装填过」由条目条件 COND_RECENTLY_RELOADED 门控；
        // 掷骰命中 → 发射一枚不消耗弹药的投射物（镜像 i==0 分支：copyWithCount(1) + INTANGIBLE_PROJECTILE）。
        if (ammo.getCount() > 0) {
            Set<String> wpnTags = new HashSet<>(Set.of(GateTag.PROJECTILE));
            if (weapon.is(ItemTags.CROSSBOW_ENCHANTABLE)) wpnTags.add(GateTag.CROSSBOW);
            if (weapon.is(ItemTags.BOW_ENCHANTABLE)) wpnTags.add(GateTag.BOW);
            double saveChance = com.cimdy.coe.data.codec.support.stat.engine.StatEngine.rollChance(shooter, "ammo_not_expended_chance", wpnTags);
            if (saveChance > 0 && shooter.getRandom().nextDouble() < Math.min(1.0, saveChance)) {
                ItemStack kept = ammo.copyWithCount(1);
                kept.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
                return kept;
            }
        }
        if (i > ammo.getCount()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemstack;
            if (i == 0) {
                itemstack = ammo.copyWithCount(1);
                itemstack.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
                return itemstack;
            } else {
                itemstack = ammo.split(i);
                if (ammo.isEmpty() && shooter instanceof Player player) {
                    player.getInventory().removeItem(ammo);
                }

                return itemstack;
            }
        }
    }

    public static int getArrowCount(Entity entity, ItemStack tool) {
        int projectileCount = 1;
        if (entity instanceof LivingEntity shooter) {
            // 归一：LAUNCH_ADDITIONAL_PROJECTILE 单属性 + {projectile} 标签（武器类型不做作用域，弓/弩差异化由数据侧 condition 表达）
            Set<String> tags = Set.of(GateTag.PROJECTILE, GateTag.ARROW);
            projectileCount += (int) DamageHelper.readInScopePublic(shooter, tags, AttributeRegistry.LAUNCH_ADDITIONAL_PROJECTILE);
            if (tool.is(ItemTags.CROSSBOW_ENCHANTABLE)) {
                Holder<Enchantment> enchantmentHolder = entity.level().registryAccess().holderOrThrow(
                        Enchantments.MULTISHOT);
                projectileCount += EnchantmentHelper.getEnchantmentLevel(enchantmentHolder, shooter);
            }
        }
        return projectileCount;
    }

    public static int getBallCount(Entity entity) {
        return entity instanceof LivingEntity shooter ? (int) DamageHelper.readInScopePublic(
                shooter, Set.of(GateTag.PROJECTILE), AttributeRegistry.LAUNCH_ADDITIONAL_PROJECTILE) + 1 : 1;
    }
}
