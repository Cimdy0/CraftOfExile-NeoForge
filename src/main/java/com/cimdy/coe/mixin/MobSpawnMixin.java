package com.cimdy.coe.mixin;

import com.cimdy.coe.data.codec.ChunkSpawners;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(NaturalSpawner.class)
public abstract class MobSpawnMixin {
    @Inject(method = "spawnForChunk",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;canSpawnForCategoryLocal(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/ChunkPos;)Z"))
    private static void getCanSpawnChunk(
            ServerLevel level, LevelChunk chunk, NaturalSpawner.SpawnState spawnState, List<MobCategory> categories,
            CallbackInfo ci, @Share("levelChunk") LocalRef<LevelChunk> levelChunk,
            @Share("countMap") LocalRef<Map<MobCategory, Integer>> countMap,
            @Share("playerCount") LocalRef<Integer> playerCount
    ) {
        levelChunk.set(chunk);
        Map<MobCategory, Integer> count = new HashMap<>();
        level.getAllEntities().forEach(entity -> {
            count.compute(entity.getType().getCategory(), (k, v) -> v == null ? 1 : v + 1);
        });
        int player = Math.max(level.getPlayers(p -> true).size(), 1);
        countMap.set(count);
        playerCount.set(player);
    }

    @ModifyArgs(method = "spawnForChunk",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;canSpawnForCategoryLocal(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/ChunkPos;)Z"))
    private static void getCanSpawnMobCategory(
            Args args, @Share("mobCategory") LocalRef<MobCategory> mobCategory) {
        mobCategory.set(args.get(0));
    }

    @ModifyExpressionValue(method = "spawnForChunk",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;canSpawnForCategoryLocal(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/ChunkPos;)Z"))
    private static boolean canSpawnForCategoryLocal(
            boolean original, @Share("levelChunk") LocalRef<LevelChunk> levelChunk,
            @Share("countMap") LocalRef<Map<MobCategory, Integer>> countMap,
            @Share("playerCount") LocalRef<Integer> playerCount,
            @Share("mobCategory") LocalRef<MobCategory> mobCategory) {
        LevelChunk chunk = levelChunk.get();
        return original || (ChunkSpawners.matches(chunk)
                && mobCategory.get().getMaxInstancesPerChunk() * playerCount.get()
                > countMap.get().getOrDefault(mobCategory.get(), 0));
    }

    // 捕获 level + 生成坐标，供下方两个 ModifyExpressionValue 以 ChunkSpawners.contains 判断「坐标是否在自定义生成范围」
    @Inject(method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;" +
            "Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;" +
            "Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "HEAD"))
    private static void noPlayerSpawnMob(
            MobCategory category, ServerLevel level, ChunkAccess chunk, BlockPos pos,
            NaturalSpawner.SpawnPredicate filter, NaturalSpawner.AfterSpawnCallback callback,
            CallbackInfo ci,
            @Share("ServerLevel") LocalRef<ServerLevel> levelRef,
            @Share("spawnPos") LocalRef<BlockPos> spawnPosRef
    ) {
        levelRef.set(level);
        spawnPosRef.set(pos);
    }

    @ModifyExpressionValue(method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;" +
            "Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;" +
            "Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target =
                    "Lnet/minecraft/server/level/ServerLevel;getNearestPlayer(DDDDZ)Lnet/minecraft/world/entity/player/Player;"))
    private static Player noPlayerSpawnMob(
            Player original,
            @Share("ServerLevel") LocalRef<ServerLevel> levelRef,
            @Share("spawnPos") LocalRef<BlockPos> spawnPosRef
    ) {
        ServerLevel level = levelRef.get();
        BlockPos spawnPos = spawnPosRef.get();
        return original == null ? ChunkSpawners.contains(level, spawnPos) ?
                Minecraft.getInstance().player : null : original;
    }

    @ModifyExpressionValue(method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;" +
            "Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;" +
            "Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target =
                    "Lnet/minecraft/world/level/NaturalSpawner;isRightDistanceToPlayerAndSpawnPoint(Lnet/minecraft/server/level/ServerLevel;" +
                            "Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos$MutableBlockPos;D)Z"))
    private static boolean isRightDistanceToPlayerAndSpawnPoint(
            boolean original,
            @Share("ServerLevel") LocalRef<ServerLevel> levelRef,
            @Share("spawnPos") LocalRef<BlockPos> spawnPosRef) {
        ServerLevel level = levelRef.get();
        BlockPos spawnPos = spawnPosRef.get();
        return original || ChunkSpawners.contains(level, spawnPos);
    }
}
