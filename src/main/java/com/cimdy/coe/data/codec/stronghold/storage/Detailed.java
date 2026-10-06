package com.cimdy.coe.data.codec.stronghold.storage;

import com.cimdy.coe.data.codec.CoeCodecs;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record Detailed(List<ContainerInfo> info) {
    public static final Detailed EMPTY = new Detailed(List.of());

    public static final Codec<Detailed> CODEC = ContainerInfo.CODEC.listOf()
            .xmap(Detailed::new, Detailed::info);

    public static final StreamCodec<RegistryFriendlyByteBuf, Detailed> STREAM_CODEC = StreamCodec.composite(
            ContainerInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), Detailed::info, Detailed::new
    );

    public Detailed add(ItemResource resource, BlockPos pos, int slot, int amount) {
        List<ContainerInfo> list = new ArrayList<>(info);
        for (int i = 0; i < list.size(); i++) {
            ContainerInfo ci = list.get(i);
            if (ci.pos().equals(pos)) {
                list.set(i, ci.add(resource, slot, amount));
                return new Detailed(list);
            }
        }
        list.add(new ContainerInfo(pos, List.of(new ItemInfo(resource, Pair.of(slot, amount)))));
        return new Detailed(list);
    }

    public Detailed merge(Detailed other) {
        Detailed result = this;
        for (ContainerInfo ci : other.info()) {
            for (ItemInfo ii : ci.items()) {
                result = result.add(ii.resource(), ci.pos(), ii.info().getFirst(), ii.info().getSecond());
            }
        }
        return result;
    }

    public Summary toSummary() {
        Summary summary = Summary.EMPTY;
        for (ContainerInfo ci : info()) {
            for (ItemInfo ii : ci.items()) {
                summary = summary.add(ii.resource(), ii.info().getSecond());
            }
        }
        return summary;
    }

    public Set<BlockPos> getResourceInfo(ItemResource resource) {
        Set<BlockPos> posSet = new HashSet<>();
        for (ContainerInfo ci : info()) {
            for (ItemInfo ii : ci.items()) {
                if (ii.resource().is(resource.getItem())) {
                    posSet.add(ci.pos);
                    break;
                }
            }
        }
        return posSet;
    }

    public Summary getContainerSummary(BlockPos blockPos) {
        for (ContainerInfo ci : info()) {
            if (ci.pos().equals(blockPos)) {
                return ci.toSummary();
            }
        }
        return Summary.EMPTY;
    }

    public record ContainerInfo(BlockPos pos, List<ItemInfo> items) {
        public static Codec<ContainerInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(ContainerInfo::pos),
                ItemInfo.CODEC.listOf().fieldOf("items").forGetter(ContainerInfo::items)
        ).apply(instance, ContainerInfo::new));

        public static StreamCodec<RegistryFriendlyByteBuf, ContainerInfo> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, ContainerInfo::pos,
                ItemInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), ContainerInfo::items,
                ContainerInfo::new
        );

        public ContainerInfo add(ItemResource resource, int slot, int amount) {
            List<ItemInfo> list = new ArrayList<>(items);
            for (int i = 0; i < list.size(); i++) {
                ItemInfo ii = list.get(i);
                if (ii.resource().equals(resource)) {
                    list.set(i, ii.add(slot, amount));
                    return new ContainerInfo(pos, list);
                }
            }
            list.add(new ItemInfo(resource, Pair.of(slot, amount)));
            return new ContainerInfo(pos, list);
        }

        public Summary toSummary() {
            Summary summary = Summary.EMPTY;
            for (ItemInfo ii : items()) {
                summary = summary.add(ii.resource(), ii.info().getSecond());
            }
            return summary;
        }
    }

    public record ItemInfo(ItemResource resource, Pair<Integer, Integer> info) {
        public static final Codec<Pair<Integer, Integer>> PAIR_CODEC = Codec.pair(
                Codec.INT.fieldOf("slot").codec(), Codec.INT.fieldOf("amount").codec());
        public static Codec<ItemInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemResource.CODEC.fieldOf("resource").forGetter(ItemInfo::resource),
                PAIR_CODEC.fieldOf("slot").forGetter(ItemInfo::info)
        ).apply(instance, ItemInfo::new));

        public static StreamCodec<RegistryFriendlyByteBuf, ItemInfo> STREAM_CODEC = StreamCodec.composite(
                CoeCodecs.ITEM_RESOURCE_STREAM, ItemInfo::resource,
                ByteBufCodecs.fromCodec(PAIR_CODEC), ItemInfo::info,
                ItemInfo::new
        );

        public ItemInfo add(int slot, int amount) {
            return new ItemInfo(resource, Pair.of(slot, info.getSecond() + amount));
        }
    }
}
