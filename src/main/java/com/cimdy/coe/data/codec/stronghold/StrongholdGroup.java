package com.cimdy.coe.data.codec.stronghold;

import com.cimdy.coe.block.entity.TerminalBlockEntity;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.entity.Owner;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record StrongholdGroup(List<Owner> owners, Map<String, GlobalPos> holds, double enderEnergy) {
    public static final Codec<StrongholdGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Owner.CODEC.listOf().fieldOf("owners").forGetter(StrongholdGroup::owners),
            Codec.unboundedMap(Codec.STRING, GlobalPos.CODEC).fieldOf("holds").forGetter(StrongholdGroup::holds),
            Codec.DOUBLE.optionalFieldOf("ender_energy", 0.0D).forGetter(StrongholdGroup::enderEnergy)
    ).apply(instance, StrongholdGroup::new));

    public static StrongholdGroup of(Player player, GlobalPos pos) {
        return new StrongholdGroup(
                List.of(new Owner(player.getUUID(), Map.of())),
                Map.of(player.getScoreboardName() + " Team", pos),
                0.0D
        );
    }

    /** 玩家是否是队长 */
    public boolean isOwner(Player player) {
        return owners.stream().anyMatch(o -> o.uuid().equals(player.getUUID()));
    }

    /**
     * 坐标是否在本组某个据点的<b>绑定区块</b>内（不规则范围）。
     * 需要 level 以便读取终端方块实体上的 {@link Stronghold#boundChunks()}。
     */
    public boolean isPosInArea(Level level, GlobalPos target) {
        for (GlobalPos holdPos : holds.values()) {
            if (!holdPos.dimension().equals(target.dimension())) continue;
            BlockEntity be = level.getBlockEntity(holdPos.pos());
            if (!(be instanceof TerminalBlockEntity)) continue;
            Stronghold hold = be.getData(AttachmentRegistry.STRONGHOLD);
            if (hold.isChunkBound(holdPos.pos(), new ChunkPos(target.pos().getX() >> 4, target.pos().getZ() >> 4))) return true;
        }
        return false;
    }

    /** 判断该坐标是否为本组某个据点的核心（精确匹配） */
    public boolean hasHoldAt(GlobalPos pos) {
        return holds.containsValue(pos);
    }

    public StrongholdGroup addHold(Player player, GlobalPos pos) {
        Map<String, GlobalPos> newHolds = new HashMap<>(holds);
        // 生成一个唯一的据点名
        String baseName = player.getScoreboardName() + " Team";
        String holdName = baseName;
        int counter = 1;
        while (newHolds.containsKey(holdName)) {
            holdName = baseName + " (" + counter + ")";
            counter++;
        }
        newHolds.put(holdName, pos);
        return new StrongholdGroup(owners, newHolds, enderEnergy);
    }

    /** 移除指定坐标的据点 */
    public StrongholdGroup removeHold(GlobalPos pos) {
        Map<String, GlobalPos> newHolds = new HashMap<>(holds);
        newHolds.values().remove(pos);
        return new StrongholdGroup(owners, newHolds, enderEnergy);
    }

    /** 重命名据点 */
    public StrongholdGroup renameHold(String oldName, String newName) {
        Map<String, GlobalPos> newHolds = new HashMap<>(holds);
        newHolds.put(newName, newHolds.remove(oldName));
        return new StrongholdGroup(owners, newHolds, enderEnergy);
    }

}