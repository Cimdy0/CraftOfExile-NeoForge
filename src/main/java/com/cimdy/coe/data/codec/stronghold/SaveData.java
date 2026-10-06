package com.cimdy.coe.data.codec.stronghold;

import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public record SaveData(Map<String, StrongholdGroup> groups) {
    public static final SaveData EMPTY = new SaveData(Map.of());

    public static final Codec<SaveData> CODEC = Codec.unboundedMap(Codec.STRING, StrongholdGroup.CODEC)
            .xmap(SaveData::new, SaveData::groups);

    /**
     * 判断玩家是否为给定坐标所在据点的所有者。
     */
    public boolean isOwnerAtPos(Player player, GlobalPos pos) {
        for (StrongholdGroup group : groups.values()) {
            if (group.hasHoldAt(pos) && group.isOwner(player)) return true;
        }
        return false;
    }

    // ======================== 服务端业务操作 ========================

    public SaveData addHold(Player player, GlobalPos pos) {
        // 1. 先查找玩家是否已经在一个队伍里
        String existingGroup = null;
        for (var entry : groups.entrySet()) {
            if (entry.getValue().isOwner(player)) {
                existingGroup = entry.getKey();
                break;
            }
        }

        // 2. 如果找到了，将新据点添加进该队伍
        if (existingGroup != null) {
            StrongholdGroup group = groups.get(existingGroup);
            StrongholdGroup newGroup = group.addHold(player, pos); // 需要 StrongholdGroup 提供 addHold 方法
            Map<String, StrongholdGroup> newGroups = new HashMap<>(groups);
            newGroups.put(existingGroup, newGroup);
            return new SaveData(newGroups);
        }

        // 3. 否则创建新队伍
        Map<String, StrongholdGroup> newGroups = new HashMap<>(groups);
        newGroups.put(player.getScoreboardName() + " Team", StrongholdGroup.of(player, pos));
        return new SaveData(newGroups);
    }
    public SaveData removeHold(Player player, GlobalPos pos) {
        for (var entry : groups.entrySet()) {
            StrongholdGroup group = entry.getValue();
            if (group.isOwner(player) && group.hasHoldAt(pos)) {
                Map<String, StrongholdGroup> newGroups = new HashMap<>(groups);
                newGroups.put(entry.getKey(), group.removeHold(pos));
                return new SaveData(newGroups);
            }
        }
        return this;
    }

    public SaveData updateHoldName(GlobalPos pos, String newName) {
        for (var gEntry : groups.entrySet()) {
            StrongholdGroup group = gEntry.getValue();
            for (var hEntry : group.holds().entrySet()) {
                if (hEntry.getValue().equals(pos)) {
                    StrongholdGroup newGroup = group.renameHold(hEntry.getKey(), newName);
                    Map<String, StrongholdGroup> newGroups = new HashMap<>(groups);
                    newGroups.put(gEntry.getKey(), newGroup);
                    return new SaveData(newGroups);
                }
            }
        }
        return this;
    }

    public SaveData setGroup(String groupName, StrongholdGroup group) {
        Map<String, StrongholdGroup> newGroups = new HashMap<>(this.groups);
        newGroups.put(groupName, group);
        return new SaveData(newGroups);
    }
}