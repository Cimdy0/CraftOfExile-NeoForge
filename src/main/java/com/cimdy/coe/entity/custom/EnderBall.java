package com.cimdy.coe.entity.custom;

import com.cimdy.coe.registry.world.EntityRegistry;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.cimdy.coe.registry.item.ItemRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class EnderBall extends ThrowableItemProjectile {
    public EnderBall(EntityType<? extends EnderBall> entityType, Level level) {
        super(entityType, level);
    }

    public EnderBall(Level level, LivingEntity owner, ItemStack item) {
        super(EntityRegistry.ENDER_BALL.get(), owner, level, item);
    }

    public EnderBall(Level level, double x, double y, double z, ItemStack item) {
        super(EntityRegistry.ENDER_BALL.get(), x, y, z, level, item);
    }

    @Override
    protected Item getDefaultItem() {
        return ItemRegistry.ENDER_BALL.asItem();
    }

    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        if (entity instanceof LivingEntity living
                && !(living instanceof Player)
                && this.level() instanceof ServerLevel
                && this.getOwner() instanceof Player player) {
            StrongholdHelper.storeEntityForPlayer(player, living);
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel serverLevel
                && this.getOwner() instanceof Player player) {
//            StrongholdHelper.releaseFirstEntity(serverLevel, player, result.getBlockPos());
            this.discard();
        }
    }
}
