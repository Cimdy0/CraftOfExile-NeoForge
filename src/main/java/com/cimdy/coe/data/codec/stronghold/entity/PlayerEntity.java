package com.cimdy.coe.data.codec.stronghold.entity;

import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record PlayerEntity(List<SaveEntity> entities) {
    public static final PlayerEntity EMPTY = new PlayerEntity(List.of());

    public static final Codec<PlayerEntity> CODEC = SaveEntity.CODEC.listOf()
            .xmap(PlayerEntity::new, PlayerEntity::entities);

    public PlayerEntity addEntity(LivingEntity living) {
        List<SaveEntity> list = new ArrayList<>(entities);
        list.add(SaveEntity.of(living));
        living.discard();
        return new PlayerEntity(list);
    }

    public PlayerEntity releaseEntity(ServerLevel serverLevel, UUID uuid) {
        List<SaveEntity> list = new ArrayList<>(entities);
        list.stream().filter(d ->
                        StrongholdHelper.createEntityFromData(serverLevel, d.data()).getUUID().equals(uuid))
                .findFirst()
                .ifPresent(list::remove);
        return new PlayerEntity(list);
    }

    // ==================== 盟友（随行伙伴）查询 ====================

    /** 按存档 NBT 内的 UUID 定位一条记录。 */
    public Optional<SaveEntity> findByUuid(UUID uuid) {
        if (uuid == null) return Optional.empty();
        return entities.stream().filter(d -> uuid.equals(d.uuid().orElse(null))).findFirst();
    }

    /** 该实体是否已被标记为盟友。 */
    public boolean isAlly(UUID uuid) {
        return findByUuid(uuid).map(SaveEntity::isAlly).orElse(false);
    }

    /** 该实体是否参与战斗（帕鲁式「工作 ↔ 战斗」开关，默认 true）。 */
    public boolean isCombatant(UUID uuid) {
        return findByUuid(uuid).map(SaveEntity::isCombatant).orElse(true);
    }

    /** 列出全部已标记为盟友的已存实体（供 UI 与后续随行出战批次使用）。 */
    public List<SaveEntity> allies() {
        return entities.stream().filter(SaveEntity::isAlly).toList();
    }

    /** 翻转/设置某条记录的盟友标记；找不到则返回原对象。 */
    public PlayerEntity withAlly(UUID uuid, boolean ally) {
        List<SaveEntity> list = new ArrayList<>(entities);
        for (int i = 0; i < list.size(); i++) {
            SaveEntity se = list.get(i);
            if (uuid.equals(se.uuid().orElse(null))) {
                list.set(i, se.withAlly(ally));
                return new PlayerEntity(list);
            }
        }
        return this;
    }

    /** 翻转/设置某条记录的战斗开关；找不到则返回原对象。 */
    public PlayerEntity withCombatant(UUID uuid, boolean combatant) {
        List<SaveEntity> list = new ArrayList<>(entities);
        for (int i = 0; i < list.size(); i++) {
            SaveEntity se = list.get(i);
            if (uuid.equals(se.uuid().orElse(null))) {
                list.set(i, se.withCombatant(combatant));
                return new PlayerEntity(list);
            }
        }
        return this;
    }
}
