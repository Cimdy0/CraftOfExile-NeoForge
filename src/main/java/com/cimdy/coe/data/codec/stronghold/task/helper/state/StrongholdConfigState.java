package com.cimdy.coe.data.codec.stronghold.task.helper.state;

import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

/**
 * 据点配置态（方案 E）。
 * <p>
 * 低频变化的状态：生产站点定义等。与 {@link StrongholdRuntimeState}（高频运行时态）分离，
 * 走 Codec 全量持久化。当前序列化仍由 {@link StrongholdState} 统一扁平承载，保持兼容。
 */
public record StrongholdConfigState(List<Site> sites) {
    public static final StrongholdConfigState EMPTY = new StrongholdConfigState(List.of());

    public static final Codec<StrongholdConfigState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Site.CODEC.listOf().fieldOf("sites").forGetter(StrongholdConfigState::sites)
    ).apply(instance, StrongholdConfigState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StrongholdConfigState> STREAM_CODEC = StreamCodec.composite(
            Site.STREAM_CODEC.apply(ByteBufCodecs.list()), StrongholdConfigState::sites,
            StrongholdConfigState::new
    );
}
