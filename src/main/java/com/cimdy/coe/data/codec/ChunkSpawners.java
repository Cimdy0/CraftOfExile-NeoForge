package com.cimdy.coe.data.codec;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ChunkSpawners(List<BlockSpawnPos> spawners) {
    public static final ChunkSpawners EMPTY = new ChunkSpawners(List.of());
    public static final Codec<ChunkSpawners> CODEC =
            BlockSpawnPos.CODEC.listOf().xmap(ChunkSpawners::new, ChunkSpawners::spawners);

    public static List<LevelChunk> getChunk(Level level) {
        List<BlockSpawnPos> spawners = level.getData(AttachmentRegistry.CHUNK_SPAWNERS).spawners;
        List<LevelChunk> chunks = new ArrayList<>();
        for (BlockSpawnPos blockSpawnPos : spawners) {
            for (ChunkSpawnPos chunkSpawnPos : blockSpawnPos.radius) {
                chunks.add(level.getChunk(chunkSpawnPos.chunkPos.x(), chunkSpawnPos.chunkPos.z()));
            }
        }
        return chunks;
    }

    public static boolean matches(LevelChunk chunk) {
        Level level = chunk.getLevel();
        List<BlockSpawnPos> spawners = level.getData(AttachmentRegistry.CHUNK_SPAWNERS).spawners;
        ChunkPos targetPos = chunk.getPos();
        for (BlockSpawnPos blockSpawnPos : spawners) {
            for (ChunkSpawnPos chunkSpawnPos : blockSpawnPos.radius) {
                if (chunkSpawnPos.chunkPos.equals(targetPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean contains(ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = ChunkPos.containing(pos);
        List<BlockSpawnPos> spawners = level.getData(AttachmentRegistry.CHUNK_SPAWNERS).spawners;
        for (BlockSpawnPos blockSpawnPos : spawners) {
            for (ChunkSpawnPos chunkSpawnPos : blockSpawnPos.radius) {
                if (chunkSpawnPos.chunkPos.equals(chunkPos) && chunkSpawnPos.spawnPos.contains(pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ChunkSpawners add(BlockPos pos, int radius) {
        List<BlockSpawnPos> spawners = new ArrayList<>(this.spawners);
        spawners.removeIf(blockSpawnPos -> blockSpawnPos.pos.equals(pos));
        List<ChunkSpawnPos> chunkSpawnPosList = BlockSpawnPos.getSpawnPos(pos, radius);
        spawners.add(new BlockSpawnPos(pos, chunkSpawnPosList));
        return new ChunkSpawners(spawners);
    }

    public ChunkSpawners remove(BlockPos pos) {
        List<BlockSpawnPos> spawners = new ArrayList<>(this.spawners);
        spawners.removeIf(blockSpawnPos -> blockSpawnPos.pos.equals(pos));
        return new ChunkSpawners(spawners);
    }

    public record BlockSpawnPos(BlockPos pos, List<ChunkSpawnPos> radius) {
        public static final Codec<BlockSpawnPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(BlockSpawnPos::pos),
                ChunkSpawnPos.CODEC.listOf().fieldOf("radius").forGetter(BlockSpawnPos::radius)
        ).apply(instance, BlockSpawnPos::new));

        public static List<ChunkSpawnPos> getSpawnPos(BlockPos pos, int radius) {
            List<ChunkSpawnPos> chunkSpawnPosList = new ArrayList<>();
            for (int i = -radius; i <= radius; i++) {
                for (int j = -radius; j <= radius; j++) {
                    for (int k = -3; k <= 3; k++) {
                        BlockPos spawnPos = pos.offset(i, k, j);
                        Optional<ChunkSpawnPos> existing = chunkSpawnPosList.stream()
                                .filter(csp -> csp.chunkPos().equals(ChunkPos.containing(spawnPos)))
                                .findFirst();
                        if (existing.isPresent()) {
                            List<BlockPos> newPosList = new ArrayList<>(existing.get().spawnPos());
                            newPosList.add(spawnPos);
                            chunkSpawnPosList.remove(existing.get());
                            chunkSpawnPosList.add(new ChunkSpawnPos(ChunkPos.containing(spawnPos), newPosList));
                        } else {
                            chunkSpawnPosList.add(new ChunkSpawnPos(ChunkPos.containing(spawnPos), List.of(spawnPos)));
                        }
                    }
                }
            }
            return chunkSpawnPosList;
        }
    }

    public record ChunkSpawnPos(ChunkPos chunkPos, List<BlockPos> spawnPos) {
        public static final Codec<ChunkSpawnPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ChunkPos.CODEC.fieldOf("chunk_pos").forGetter(ChunkSpawnPos::chunkPos),
                BlockPos.CODEC.listOf().fieldOf("spawn_pos").forGetter(ChunkSpawnPos::spawnPos)
        ).apply(instance, ChunkSpawnPos::new));
    }
}
