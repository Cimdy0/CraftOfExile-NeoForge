package com.cimdy.coe.mixin;

import com.cimdy.coe.data.codec.ChunkSpawners;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin {
    @Shadow
    @Final
    public ServerLevel level;

    @Shadow
    protected abstract void tickSpawningChunk(LevelChunk chunk, long timeInhabited, List<MobCategory> spawnCategories, NaturalSpawner.SpawnState spawnState);

    @ModifyExpressionValue(method = "tickChunks(Lnet/minecraft/util/profiling/ProfilerFiller;J)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner;getFilteredSpawningCategories(Lnet/minecraft/world/level/NaturalSpawner$SpawnState;ZZ)Ljava/util/List;"))
    private List<MobCategory> getList(
            List<MobCategory> original,
            @Share("list") LocalRef<List<MobCategory>> list
    ) {
        list.set(original);
        return original;
    }

    @ModifyArgs(method = "tickChunks(Lnet/minecraft/util/profiling/ProfilerFiller;J)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner;getFilteredSpawningCategories(Lnet/minecraft/world/level/NaturalSpawner$SpawnState;ZZ)Ljava/util/List;"))
    private void getState(
            Args args,
            @Share("state") LocalRef<NaturalSpawner.SpawnState> state
    ) {
        state.set(args.get(0));
    }

    @Inject(method = "tickChunks(Lnet/minecraft/util/profiling/ProfilerFiller;J)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"))
    private void tickSpawningChunk(
            ProfilerFiller profiler, long timeInhabited, CallbackInfo ci,
            @Share("list") LocalRef<List<MobCategory>> list,
            @Share("state") LocalRef<NaturalSpawner.SpawnState> state) {
        List<MobCategory> categories = list.get();
        NaturalSpawner.SpawnState spawnState = state.get();
        if (categories != null && spawnState != null) {
            ChunkSpawners.getChunk(this.level).forEach(chunk -> {
                NaturalSpawner.spawnForChunk(this.level, chunk, spawnState, categories);
            });
        }
    }
}
