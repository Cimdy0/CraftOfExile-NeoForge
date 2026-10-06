package com.cimdy.coe.entity.goal.stronghold.behavior;

import com.cimdy.coe.data.codec.stronghold.Stronghold;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

/**
 * 行为构建上下文，供所有 Helper 和 {@link StrongholdBehavior} 使用。
 * <p>
 * 原本在 {@code hold} 包中是一个标记接口，在 {@code helper} 包中是完整接口；
 * 现已统一为完整接口，消除双重定义带来的混淆。
 */
public interface IGoalContext {
    Mob getMob();

    ServerLevel getLevel();

    GlobalPos getTerminalPos();

    Stronghold getHold();

    boolean moveToInteractionBlock(BlockPos target);

    boolean moveToNearBlock(BlockPos target);

    void lookAt(BlockPos target);

    List<BlockPos> getStorageBlocks();

    void startContainerInteraction(BlockPos target, Consumer<ServerLevel> action);

    boolean tickContainerInteraction(ServerLevel level);

    default boolean moveTo(Mob mob, BlockPos target, double distance) {
        Vec3 center = Vec3.atCenterOf(target);
        mob.getLookControl().setLookAt(center);
        double distSqr = mob.getBoundingBox().distanceToSqr(center);
        if (distSqr <= distance) {
            mob.getNavigation().stop();
            return true;
        }
        mob.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.0);
        return false;
    }

    /** 空上下文：用于无需实际据点上下文的场景（如仅注入 AI 行为）。 */
    IGoalContext EMPTY = new IGoalContext() {
        @Override public Mob getMob() { return null; }
        @Override public ServerLevel getLevel() { return null; }
        @Override public GlobalPos getTerminalPos() { return null; }
        @Override public Stronghold getHold() { return null; }
        @Override public boolean moveToInteractionBlock(BlockPos target) { return false; }
        @Override public boolean moveToNearBlock(BlockPos target) { return false; }
        @Override public void lookAt(BlockPos target) {}
        @Override public List<BlockPos> getStorageBlocks() { return List.of(); }
        @Override public void startContainerInteraction(BlockPos target, Consumer<ServerLevel> action) {}
        @Override public boolean tickContainerInteraction(ServerLevel level) { return false; }
    };
}
