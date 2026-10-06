package com.cimdy.coe.data.codec.stronghold.entity;

import com.cimdy.coe.data.codec.CoeCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public record StrongholdEntity(UUID owner, GlobalPos pos, List<ItemStack> stacks, double hunger, double san) {
    public static final StrongholdEntity EMPTY = new StrongholdEntity(UUID.randomUUID(), GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO),
            List.of(), 20.0, 100.0);
    public static final Codec<StrongholdEntity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(StrongholdEntity::owner),
            GlobalPos.CODEC.fieldOf("pos").forGetter(StrongholdEntity::pos),
            CoeCodecs.FLEXIBLE_STACK.listOf().fieldOf("items").forGetter(StrongholdEntity::stacks),
            Codec.DOUBLE.fieldOf("hunger").forGetter(StrongholdEntity::hunger),
            Codec.DOUBLE.fieldOf("san").forGetter(StrongholdEntity::san)
    ).apply(instance, StrongholdEntity::new));

    public static StrongholdEntity of(UUID uuid) {
        return new StrongholdEntity(uuid, GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO), List.of(), 20.0, 100.0);
    }

    public StrongholdEntity set(List<ItemStack> stacks) {
        return new StrongholdEntity(owner, pos, stacks, hunger, san);
    }

    public StrongholdEntity set(UUID uuid, GlobalPos globalPos) {
        return new StrongholdEntity(uuid, globalPos, stacks, hunger, san);
    }

    public StrongholdEntity withHunger(double hunger) {
        return new StrongholdEntity(owner, pos, stacks, hunger, san);
    }

    public StrongholdEntity withSan(double san) {
        return new StrongholdEntity(owner, pos, stacks, hunger, san);
    }

    public StrongholdEntity dropItem(Level level, Vec3 position) {
        if(this.stacks.isEmpty()) return this;
        this.stacks.forEach(s -> {
            double deltaX = Mth.randomBetween(level.getRandom(), -0.2F, 0.2F);
            double deltaY = Mth.randomBetween(level.getRandom(), 0.3F, 0.7F);
            double deltaZ = Mth.randomBetween(level.getRandom(), -0.2F, 0.2F);
            ItemEntity i = new ItemEntity(level, position.x, position.y, position.z, s, deltaX, deltaY, deltaZ);
            level.addFreshEntity(i);
        });
        return new StrongholdEntity(owner, pos, List.of(), hunger, san);
    }
}
