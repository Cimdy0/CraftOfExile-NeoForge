package com.cimdy.coe.data.codec.stronghold.task.helper.result;

import com.cimdy.coe.data.codec.stronghold.task.helper.demand.DemandInfo;
import com.cimdy.coe.data.codec.stronghold.task.helper.supply.SupplyInfo;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record TransferResult(
        List<DemandInfo> promotableDemands,
        List<SupplyInfo> promotableSupplies
) {
    public static final TransferResult EMPTY = new TransferResult(List.of(), List.of());

    public static final Codec<TransferResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DemandInfo.CODEC.listOf().fieldOf("promotable_demands").forGetter(TransferResult::promotableDemands),
            SupplyInfo.CODEC.listOf().fieldOf("promotable_supplies").forGetter(TransferResult::promotableSupplies)
    ).apply(instance, TransferResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TransferResult> STREAM_CODEC = StreamCodec.composite(
            DemandInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), TransferResult::promotableDemands,
            SupplyInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), TransferResult::promotableSupplies,
            TransferResult::new
    );
}