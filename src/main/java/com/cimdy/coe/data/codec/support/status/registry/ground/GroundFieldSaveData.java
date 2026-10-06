package com.cimdy.coe.data.codec.support.status.registry.ground;

import com.cimdy.coe.data.helper.engine.ground.GroundFieldManager;

import com.mojang.serialization.Codec;

import java.util.List;

/**
 * 单维度（{@code Level}）的全部活动地面场集合——作为 {@code AttachmentType} 的持久化载体。
 *
 * <p>服务端权威、不随客户端同步（客户端经专用 {@code GroundFieldSync} 包渲染），
 * 不随死亡复制。由 {@code GroundFieldManager} 每 tick 读写此附件推进生命周期。
 */
public record GroundFieldSaveData(List<GroundFieldData> fields) {
    public static final GroundFieldSaveData EMPTY = new GroundFieldSaveData(List.of());

    public static final Codec<GroundFieldSaveData> CODEC =
            GroundFieldData.CODEC.listOf().xmap(GroundFieldSaveData::new, GroundFieldSaveData::fields);
}
