package com.cimdy.coe.data.codec.progression.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.UUID;

/**
 * 研究台运行时状态（存于研究台方块实体的附件）。
 *
 * <p>研究「点了就等」——扣研究点在 {@link com.cimdy.coe.data.codec.progression.ProgressionService#beginResearch}
 * 时已完成，本记录只负责推进 {@link #ticks} 计时；计时满 {@link ResearchNodeDef#durationTicks} 后
 * 由方块实体调用 {@code completeResearch} 结算。
 *
 * <p>持有 {@link #owner}（发起研究的玩家 UUID）是必要的：研究成果记在玩家进度上而不是方块上，
 * 因此结算时需要把这台机器"还给"发起者。
 */
public record ResearchBench(Optional<UUID> owner, Optional<Identifier> research, int ticks) {

    public static final ResearchBench EMPTY = new ResearchBench(Optional.empty(), Optional.empty(), 0);

    public static final Codec<ResearchBench> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(ResearchBench::owner),
            Identifier.CODEC.optionalFieldOf("research").forGetter(ResearchBench::research),
            Codec.INT.optionalFieldOf("ticks", 0).forGetter(ResearchBench::ticks)
    ).apply(instance, ResearchBench::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchBench> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ResearchBench::owner,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), ResearchBench::research,
            ByteBufCodecs.VAR_INT, ResearchBench::ticks,
            ResearchBench::new);

    /** 开始一项研究。 */
    public static ResearchBench of(UUID owner, Identifier research) {
        return new ResearchBench(Optional.of(owner), Optional.of(research), 0);
    }

    public ResearchBench withTicks(int t) {
        return new ResearchBench(owner, research, t);
    }

    public ResearchBench advance() {
        return withTicks(ticks + 1);
    }

    /** 研究结束 / 取消：清空为待机。 */
    public ResearchBench cleared() {
        return EMPTY;
    }

    public boolean isRunning() {
        return research.isPresent() && owner.isPresent();
    }

    /** 当前进度 0.0–1.0（无任务时为 0）。 */
    public float progress(int durationTicks) {
        if (!isRunning() || durationTicks <= 0) return 0f;
        return Math.min(1f, (float) ticks / durationTicks);
    }
}
