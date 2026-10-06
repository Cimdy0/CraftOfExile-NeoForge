package com.cimdy.coe.data.codec.stronghold.storage.stock;

import com.cimdy.coe.data.codec.stronghold.storage.CapacityCalculator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record StockSet(List<StockEntry> entries, float minTransfer, int priority) {

    public static final Codec<StockSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StockEntry.CODEC.listOf().fieldOf("entries").forGetter(StockSet::entries),
            Codec.FLOAT.fieldOf("min_transfer").forGetter(StockSet::minTransfer),
            Codec.INT.fieldOf("priority").forGetter(StockSet::priority)
    ).apply(instance, StockSet::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StockSet> STREAM_CODEC = StreamCodec.composite(
            StockEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), StockSet::entries,
            ByteBufCodecs.FLOAT, StockSet::minTransfer,
            ByteBufCodecs.INT, StockSet::priority,
            StockSet::new
    );

    public boolean isEmpty() {
        return this.entries.stream().allMatch(StockEntry::isEmpty);
    }
    public static StockSet empty(int number) {
        StockSet entry = new StockSet(List.of(),0.20F, 5);
        for (int i = 0; i < number; i++) {
            entry = entry.addSupply(StockEntry.empty());
        }
        return entry;
    }

    public StockSet addSupply(StockEntry supply) {
        List<StockEntry> list = new ArrayList<>(this.entries);
        list.add(supply);
        return new StockSet(list, this.minTransfer, this.priority);
    }

    public StockSet setTransit(ItemResource resource, int transit) {
        List<StockEntry> list = new ArrayList<>(this.entries);
        for (int i = 0; i < list.size(); i++) {
            StockEntry entry = list.get(i);
            if (entry.resource().equals(resource)) {
                list.set(i, entry.setTransit(transit));
                break;
            }
        }
        return new StockSet(list, this.minTransfer, this.priority);
    }

    public StockSet update(ResourceHandler<ItemResource> handler) {
        List<StockEntry> nonEmpty = new ArrayList<>();
        for (StockEntry e : this.entries()) {
            if (!e.resource().isEmpty()) nonEmpty.add(e);
        }

        CapacityCalculator calculator = new CapacityCalculator(handler.size());
        Map<ItemResource, Integer> originalMax = new HashMap<>();

        for (StockEntry e : nonEmpty) {
            StockEntry.SlotRequirement req = StockEntry.calculateRequirement(handler, e.resource(), e.max());
            calculator.addRequirement(e.resource(), req);
            originalMax.put(e.resource(), e.max());
        }

        Map<ItemResource, int[]> allocation = calculator.allocate(originalMax);
        List<StockEntry> updated = new ArrayList<>();
        for (StockEntry e : nonEmpty) {
            int[] result = allocation.get(e.resource());
            int newMax = result[0];
            int total = result[1];
            updated.add(new StockEntry(e.resource(), e.supply(), newMax, e.transit(), total));
        }
        if(calculator.hasEmptySlot()) updated.add(StockEntry.empty());
        return new StockSet(updated, this.minTransfer, this.priority);
    }
}
