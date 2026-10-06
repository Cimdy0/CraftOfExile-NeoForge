package com.cimdy.coe.data.codec.stronghold.task.helper.supply;

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

public record SupplyInfo(
        ItemResource resource,
        BlockPos containerPos,
        StockRange range,
        int remaining,
        int minTransfer,
        int priority
) implements ScopedInfo {
    public static final Codec<SupplyInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemResource.CODEC.fieldOf("resource").forGetter(SupplyInfo::resource),
            BlockPos.CODEC.fieldOf("pos").forGetter(SupplyInfo::containerPos),
            StockRange.CODEC.fieldOf("range").forGetter(SupplyInfo::range),
            Codec.INT.fieldOf("remaining").forGetter(SupplyInfo::remaining),
            Codec.INT.fieldOf("min_transfer").forGetter(SupplyInfo::minTransfer),
            Codec.INT.fieldOf("priority").forGetter(SupplyInfo::priority)
    ).apply(instance, SupplyInfo::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SupplyInfo> STREAM_CODEC = StreamCodec.composite(
            CoeCodecs.ITEM_RESOURCE_STREAM, SupplyInfo::resource,
            BlockPos.STREAM_CODEC, SupplyInfo::containerPos,
            StockRange.STREAM_CODEC, SupplyInfo::range,
            ByteBufCodecs.INT, SupplyInfo::remaining,
            ByteBufCodecs.INT, SupplyInfo::minTransfer,
            ByteBufCodecs.INT, SupplyInfo::priority,
            SupplyInfo::new
    );

    @Override
    public StockRange range() {
        return range;
    }

    @Override
    public int priority() {
        return priority;
    }

    public SupplyInfo withRemaining(int newRemaining) {
        return new SupplyInfo(resource, containerPos, range, newRemaining, minTransfer, priority);
    }

    public boolean canSupplyTo(StockRange demandRange) {
        return this.range.ordinal() <= demandRange.ordinal();
    }
}