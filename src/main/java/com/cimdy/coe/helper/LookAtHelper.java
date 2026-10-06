package com.cimdy.coe.helper;

import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class LookAtHelper {
    public static HitResult getLookAt(Entity entity, Double maxDistance) {
        Vec3 eyePosition = entity.getEyePosition();
        Vec3 lookAngle = entity.getLookAngle();
        Vec3 Max = eyePosition.add(lookAngle.normalize().scale(maxDistance));
        return entity.level().clip(new ClipContext(eyePosition, Max, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, entity));
    }

    public static Entity getEntityLookedAt(Entity entity, Double maxDistance) {
        HitResult pos = getLookAt(entity, maxDistance);
        Entity foundEntity = null;
        Vec3 positionVector = entity.getEyePosition();
        double distance = pos.getLocation().distanceTo(positionVector);
        Vec3 lookVector = entity.getLookAngle();
        Vec3 reachVector = positionVector.add(lookVector.x * maxDistance, lookVector.y * maxDistance, lookVector.z * maxDistance);

        List<Entity> entitiesInBoundingBox = entity.level().getEntities(entity,
                entity.getBoundingBox().inflate(lookVector.x * maxDistance, lookVector.y * maxDistance, lookVector.z * maxDistance)
                        .expandTowards(1F, 1F, 1F));
        double minDistance = distance;
        for (Entity entity2 : entitiesInBoundingBox) {
            Entity lookedEntity = null;
            if (entity2.isPickable()) {
                AABB collisionBox = entity2.getBoundingBox();
                Optional<Vec3> interceptPosition = collisionBox.clip(positionVector, reachVector);

                if (collisionBox.contains(positionVector)) {
                    if (0.0D < minDistance || minDistance == 0.0D) {
                        lookedEntity = entity2;
                        minDistance = 0.0D;
                    }
                } else if (interceptPosition.isPresent()) {
                    double distanceToEntity = positionVector.distanceTo(interceptPosition.get());

                    if (distanceToEntity < minDistance || minDistance == 0.0D) {
                        lookedEntity = entity2;
                        minDistance = distanceToEntity;
                    }
                }
            }
            if (lookedEntity != null && minDistance < distance) {
                foundEntity = lookedEntity;
            }
        }

        return foundEntity;
    }

    public static BlockPos getBlockPosLookAt(LivingEntity livingEntity, Double maxDistance) {
        HitResult hitResult = getLookAt(livingEntity, maxDistance);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockhitresult = (BlockHitResult) hitResult;
            return blockhitresult.getBlockPos();
        }
        return null;
    }

    public static Vec3 getPosLookAt(LivingEntity livingEntity, Double maxDistance) {
        HitResult hitResult = getLookAt(livingEntity, maxDistance);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockhitresult = (BlockHitResult) hitResult;
            return blockhitresult.getLocation();
        }
        return null;
    }

    public static Vec3 getPosOrEntityPos(LivingEntity living, MinMaxBounds.Doubles distance) {
        Vec3 look = getPosOrEntityPos(living, 128.0);
        Vec3 pos = living.position();
        if (look == null) {
            return pos;
        }
        double distanceTo = look.distanceTo(pos);
        if (distance.max().isPresent() && distanceTo <= distance.max().get()) {
            return look;
        } else if (distance.min().isPresent() && distanceTo >= distance.min().get()) {
            return look;
        }
        return pos;
    }

    public static Vec3 getEntityOrLookAtEntityEyePos(LivingEntity living, MinMaxBounds.Doubles distance) {
        Vec3 look_entity = getEntityOrLookAtEntityEyePos(living, 128.0);
        Vec3 pos = living.getEyePosition();
        double distanceTo = look_entity.distanceTo(pos);
        if (distance.max().isPresent() && distanceTo <= distance.max().get()) {
            return look_entity;
        } else if (distance.min().isPresent() && distanceTo >= distance.min().get()) {
            return look_entity;
        }
        return pos;
    }

    public static Vec3 getEntityOrLookAtEntityEyePos(LivingEntity livingEntity, Double maxDistance) {
        Entity entity = getEntityLookedAt(livingEntity, maxDistance);
        return entity != null ? entity.getEyePosition() : livingEntity.getEyePosition();
    }

    public static Vec3 getPosOrEntityPos(LivingEntity livingEntity, Double maxDistance) {
        Vec3 pos;
        Entity entity = getEntityLookedAt(livingEntity, maxDistance);
        if (entity != null) {
            pos = entity.position();
        } else {
            pos = getPosLookAt(livingEntity, maxDistance);
        }
        return pos;
    }

    public static BlockEntity getBlockEntityLookAt(LivingEntity livingEntity, Double maxDistance) {
        BlockPos blockPos = getBlockPosLookAt(livingEntity, maxDistance);
        if (blockPos != null) {
            return livingEntity.level().getBlockEntity(blockPos);
        }
        return null;
    }

    /**
     * 获取玩家看向的方块坐标（不要求有 BlockEntity）
     * @return 看向的 BlockPos，若没看到方块则返回 null
     */
    public static BlockPos getBlockPosLookingAt(LivingEntity entity, double maxDistance) {
        HitResult hitResult = getLookAt(entity, maxDistance);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return ((BlockHitResult) hitResult).getBlockPos();
        }
        return null;
    }

    /**
     * 获取玩家看向的方块实体（原有方法保留，但基于上面的新方法）
     */
    public static BlockEntity getBlockEntityLookAt(LivingEntity entity, double maxDistance) {
        BlockPos pos = getBlockPosLookingAt(entity, maxDistance);
        if (pos != null) {
            return entity.level().getBlockEntity(pos);
        }
        return null;
    }
}
