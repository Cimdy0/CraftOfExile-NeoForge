package com.cimdy.coe;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.world.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 井的世界生成 —— 村庄内必定生成（对齐用户需求「在村庄必定生成」）。
 * <p>
 * <b>为何不用 {@code StructureModifier}：</b>NeoForge 的 {@code StructureModifier} 只能改写结构的
 * 生物群系 / 刷怪覆盖 / 生成阶段 / 地形适应等「设置」，<b>无法向村庄模板注入方块</b>，因此无法用它把井塞进村庄。
 * 这里改用服务端区块生成完成后的回调：扫描村庄中心，在其附近空地<b>幂等</b>地放置一眼城镇水井。
 * <p>
 * <b>非村庄的随机生成</b>由数据包负责（configured_feature + placed_feature + biome_modifier → {@code neoforge:add_features}）。
 * <p>
 * 实现要点：{@link ChunkEvent.Load} 服务端早于区块达到 FULL 状态（此时结构 START 尚未就绪），
 * 故通过 {@code MinecraftServer#executeIfPossible} 把放置逻辑延到下一 tick 执行。
 *
 * <p>归属：本类是<b>世界生成机制</b>，不是事件派发层——按分层纪律（事件类只做派发，
 * 机制归 {@code data/helper/**} 或对应领域包）从 {@code event} 包迁出至 {@code world} 包。
 */
@EventBusSubscriber(modid = CraftOfExile.MODID)
public class WellVillageSpawner {

    /** 以村庄边界盒中心为起点，由内向外在多大半径内查找可放置的空地。 */
    private static final int PLACE_SEARCH_RADIUS = 5;
    /** 幂等判定：中心周围多大半径内已存在井则整座村庄跳过（避免跨区块重复放置）。 */
    private static final int IDEMPOTENT_RADIUS = 6;

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;
        if (serverLevel.dimension() != Level.OVERWORLD) return;
        // 仅处理新生成的区块，避免对已从磁盘加载的区块反复扫描
        if (!event.isNewChunk()) return;

        ChunkPos center = event.getChunk().getPos();
        // 延到下一 tick（结构 START 此时已就绪），再扫描并放置井
        serverLevel.getServer().executeIfPossible(() -> placeWellsNearVillages(serverLevel, center));
    }

    private static void placeWellsNearVillages(ServerLevel level, ChunkPos center) {
        StructureManager manager = level.structureManager();
        Registry<Structure> structureRegistry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);

        // 村庄边界盒可能横跨多个区块；用 Set 去重，保证每座村庄只尝试放置一次
        Set<BoundingBox> handled = new HashSet<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos cp = new ChunkPos(center.x() + dx, center.z() + dz);
                List<StructureStart> starts = manager.startsForStructure(cp, s -> true);
                for (StructureStart start : starts) {
                    if (!start.isValid()) continue;
                    BoundingBox box = start.getBoundingBox();
                    if (!handled.add(box)) continue;
                    if (!isVillage(structureRegistry, start.getStructure())) continue;
                    tryPlaceWell(level, box);
                }
            }
        }
    }

    private static boolean isVillage(Registry<Structure> structureRegistry, Structure structure) {
        Identifier key = structureRegistry.getKey(structure);
        return key != null
                && key.getNamespace().equals("minecraft")
                && key.getPath().startsWith("village");
    }

    private static void tryPlaceWell(ServerLevel level, BoundingBox box) {
        int cx = (box.minX() + box.maxX()) / 2;
        int cz = (box.minZ() + box.maxZ()) / 2;

        // 幂等：该村庄中心附近已存在井则跳过
        if (alreadyHasWellNear(level, cx, cz, IDEMPOTENT_RADIUS)) return;

        BlockState well = BlockRegistry.WELL.get().defaultBlockState();
        // 由内向外螺旋查找：地表坚实、上方为空、且非水面的空地
        for (int r = 0; r <= PLACE_SEARCH_RADIUS; r++) {
            for (int ox = -r; ox <= r; ox++) {
                for (int oz = -r; oz <= r; oz++) {
                    if (Math.max(Math.abs(ox), Math.abs(oz)) != r) continue; // 仅环边
                    if (tryPlaceAt(level, cx + ox, cz + oz, well)) return;
                }
            }
        }
    }

    private static boolean tryPlaceAt(ServerLevel level, int x, int z, BlockState well) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        // 向下找到首个非空气方块作为地面
        while (y > level.getMinY() && level.getBlockState(new BlockPos(x, y, z)).isAir()) y--;
        BlockPos ground = new BlockPos(x, y, z);
        BlockPos wellPos = ground.above();
        if (wellPos.getY() >= level.getMaxY()) return false;
        if (!level.getBlockState(ground).isSolid()) return false;
        if (!level.getBlockState(wellPos).isAir()) return false;
        if (!level.getFluidState(wellPos).isEmpty()) return false;
        level.setBlock(wellPos, well, 3);
        return true;
    }

    private static boolean alreadyHasWellNear(ServerLevel level, int cx, int cz, int radius) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, cx, cz);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos p = new BlockPos(cx + dx, y, cz + dz);
                if (level.getBlockState(p).getBlock() == BlockRegistry.WELL.get()) return true;
            }
        }
        return false;
    }
}
