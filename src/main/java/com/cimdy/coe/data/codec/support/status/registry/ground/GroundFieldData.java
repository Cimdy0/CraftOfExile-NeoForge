package com.cimdy.coe.data.codec.support.status.registry.ground;

import com.cimdy.coe.data.helper.engine.ground.GroundFieldManager;

import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * 单条地面场（Ground Field）实例的不可变数据载体。
 *
 * <p>既用于服务端 {@code Level} 附件的持久化（{@link #CODEC}，随维度存档、重启仍在），
 * 也用于客户端渲染同步（{@link #STREAM_CODEC}，经 {@code GroundFieldSync} 包下发）。
 *
 * <p><b>通用地面模型</b>：地面不再是固定的几种「存在性状态」，而是一块携带
 * {@link #stats()}（{@link StatEntry} 列表，即地面的效果载荷）、
 * {@link #camp()}（阵营，创建时从所有者取）、{@link #targeting()}（作用域门控）、
 * {@link #color()}（客户端渲染色）的区域内生体。每 tick 由 {@code GroundFieldManager}
 * 对半径内实体按阵营门控后，把 {@code stats} 注入其 {@code STAT_STORE}（镜像 PresenceAura 刷新范式），
 * 离开区域后由 {@code StatStore.tick()} 自动过期。
 *
 * <p>{@code ownerUuid} 永不 {@code null}：无施法者时填 {@link #NO_OWNER} 哨兵。
 */
public record GroundFieldData(
        UUID id,
        Vec3 center,
        float radius,
        int remainingTicks,
        UUID ownerUuid,
        Camp camp,
        GroundTargeting targeting,
        int color,
        List<StatEntry> stats
) {
    /** 无施法者哨兵（全零 UUID）。 */
    public static final UUID NO_OWNER = new UUID(0L, 0L);

    public static final Codec<GroundFieldData> CODEC = RecordCodecBuilder.create(i -> i.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(GroundFieldData::id),
            Vec3.CODEC.fieldOf("center").forGetter(GroundFieldData::center),
            Codec.FLOAT.fieldOf("radius").forGetter(GroundFieldData::radius),
            Codec.INT.fieldOf("remaining").forGetter(GroundFieldData::remainingTicks),
            UUIDUtil.CODEC.fieldOf("owner").forGetter(GroundFieldData::ownerUuid),
            Camp.CODEC.fieldOf("camp").forGetter(GroundFieldData::camp),
            GroundTargeting.CODEC.fieldOf("targeting").forGetter(GroundFieldData::targeting),
            Codec.INT.fieldOf("color").forGetter(GroundFieldData::color),
            StatEntry.CODEC.listOf().fieldOf("stats").forGetter(GroundFieldData::stats)
    ).apply(i, GroundFieldData::new));

    /**
     * 网络同步流编解码：客户端只需渲染所需字段（id/center/radius/color），
     * 无需 {@code stats}/{@code camp}/{@code targeting}（服务端运算用），故省略并以默认值补齐。
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, GroundFieldData> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, GroundFieldData::id,
                    Vec3.STREAM_CODEC, GroundFieldData::center,
                    ByteBufCodecs.FLOAT, GroundFieldData::radius,
                    ByteBufCodecs.INT, GroundFieldData::color,
                    (id, center, radius, color) -> new GroundFieldData(
                            id, center, radius, 0, NO_OWNER, Camp.NONE, GroundTargeting.ENEMIES, color, List.of())
            );
}
