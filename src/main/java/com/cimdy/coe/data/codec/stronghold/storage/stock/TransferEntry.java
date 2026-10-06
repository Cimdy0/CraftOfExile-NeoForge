package com.cimdy.coe.data.codec.stronghold.storage.stock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record TransferEntry(ItemResource resource, StockRange range, StockMode mode, int amount, int minTransfer) {
    public static final Codec<TransferEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemResource.CODEC.fieldOf("resource").forGetter(TransferEntry::resource),
            StockRange.CODEC.fieldOf("range").forGetter(TransferEntry::range),
            StockMode.CODEC.fieldOf("mode").forGetter(TransferEntry::mode),
            Codec.INT.fieldOf("amount").forGetter(TransferEntry::amount),
            Codec.INT.fieldOf("min_transfer").forGetter(TransferEntry::minTransfer)
    ).apply(instance, TransferEntry::new));
}
