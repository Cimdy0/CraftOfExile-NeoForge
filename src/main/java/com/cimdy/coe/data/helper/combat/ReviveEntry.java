package com.cimdy.coe.data.helper.combat;

import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 单条「待复活」记录：挂在持有者（owner）附件 {@code PENDING_REVIVES} 上，描述一只被击败的
 * 可复生召唤物应在多久后被重生。
 *
 * <ul>
 *   <li>{@code entityId}：重生时查 {@code BuiltInRegistries.ENTITY_TYPE} 重建实体（与
 *       {@code SpawnAction.spawnMinionEntity} 同源，保证阵营 / 归属 / AI 注入一致）。</li>
 *   <li>{@code companion}：是否为盟友型召唤物（决定 {@code IS_COMPANION} 标记与随行 AI）。</li>
 *   <li>{@code ticksLeft}：剩余倒计时（tick）；由持有者每 tick 递减，归零即重生。</li>
 * </ul>
 * 不可变（倒计时重置时整体重建）。
 */
public record ReviveEntry(Identifier entityId, boolean companion, int ticksLeft) {
    public static final Codec<ReviveEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("entity_id").forGetter(ReviveEntry::entityId),
            Codec.BOOL.fieldOf("companion").forGetter(ReviveEntry::companion),
            Codec.INT.fieldOf("ticks_left").forGetter(ReviveEntry::ticksLeft)
    ).apply(i, ReviveEntry::new));
}
