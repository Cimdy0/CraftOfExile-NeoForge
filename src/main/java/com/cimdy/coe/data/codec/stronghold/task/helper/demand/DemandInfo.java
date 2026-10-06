package com.cimdy.coe.data.codec.stronghold.task.helper.demand;

import com.cimdy.coe.data.codec.stronghold.task.helper.claim.ScopedInfo;
import com.cimdy.coe.data.codec.CoeCodecs;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockRange;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record DemandInfo(
        ItemResource resource,
        BlockPos containerPos,
        StockRange range,
        int remaining,
        int minTransfer,
        int priority
) implements ScopedInfo {
    public static final Codec<DemandInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemResource.CODEC.fieldOf("resource").forGetter(DemandInfo::resource),
            BlockPos.CODEC.fieldOf("pos").forGetter(DemandInfo::containerPos),
            StockRange.CODEC.fieldOf("range").forGetter(DemandInfo::range),
            Codec.INT.fieldOf("remaining").forGetter(DemandInfo::remaining),
            Codec.INT.fieldOf("min_transfer").forGetter(DemandInfo::minTransfer),
            Codec.INT.fieldOf("priority").forGetter(DemandInfo::priority)
    ).apply(instance, DemandInfo::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DemandInfo> STREAM_CODEC = StreamCodec.composite(
            CoeCodecs.ITEM_RESOURCE_STREAM, DemandInfo::resource,
            BlockPos.STREAM_CODEC, DemandInfo::containerPos,
            StockRange.STREAM_CODEC, DemandInfo::range,
            ByteBufCodecs.INT, DemandInfo::remaining,
            ByteBufCodecs.INT, DemandInfo::minTransfer,
            ByteBufCodecs.INT, DemandInfo::priority,
            DemandInfo::new
    );

    @Override
    public StockRange range() {
        return range;
    }

    @Override
    public int priority() {
        return priority;
    }

    public DemandInfo withRemaining(int newRemaining) {
        return new DemandInfo(resource, containerPos, range, newRemaining, minTransfer, priority);
    }
}