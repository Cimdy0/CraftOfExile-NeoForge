package com.cimdy.coe.data.codec.stronghold.storage;

import com.cimdy.coe.data.codec.stronghold.ItemCount;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockEntry;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockSet;
import com.cimdy.coe.data.codec.stronghold.storage.stock.TransferEntry;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Summary(List<ItemCount> summary) {
    public static final Summary EMPTY = new Summary(List.of());
    public static final Codec<Summary> CODEC =
            ItemCount.CODEC.listOf().xmap(Summary::new, Summary::summary);

    public static final StreamCodec<RegistryFriendlyByteBuf, Summary> STREAM_CODEC = StreamCodec.composite(
            ItemCount.STREAM_CODEC.apply(ByteBufCodecs.list()), Summary::summary, Summary::new
    );

    public Summary add(ItemResource resource, int amount) {
        List<ItemCount> list = new ArrayList<>(summary);
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            ItemCount itemCount = list.get(i);
            if (itemCount.resource().equals(resource)) {
                list.set(i, itemCount.add(amount));
                found = true;
                break;
            }
        }
        if (!found) {
            list.add(new ItemCount(resource, amount));
        }
        return new Summary(list);
    }

    public List<TransferEntry> getTransfer(StockSet set, Map<ItemResource, StrongholdHelper.TransportInfo> transportMap) {
        List<StockEntry> stockEntries = set.entries();
        List<TransferEntry> transferEntries = new ArrayList<>();
        float minTransfer = set.minTransfer();

        Map<ItemResource, Integer> stockMap = new HashMap<>();
        for (ItemCount item : this.summary) {
            stockMap.put(item.resource(), item.amount());
        }

        for (StockEntry stock : stockEntries) {
            if (stock.resource().isEmpty() || !stock.hasCarry()) continue;

            int amount = stockMap.getOrDefault(stock.resource(), 0);
            StrongholdHelper.TransportInfo info = transportMap.getOrDefault(stock.resource(), StrongholdHelper.TransportInfo.EMPTY);
            transferEntries.addAll(stock.calculate(amount, minTransfer, info));
        }
        return transferEntries;
    }
}
