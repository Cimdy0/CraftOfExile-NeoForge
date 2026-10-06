package com.cimdy.coe.data.codec.stronghold.storage.stock;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record TransferMode(
        Pair<StockRange, StockMode> first, Pair<StockRange, StockMode> second) {

    public static final TransferMode DEFAULT = new TransferMode(
            of(StockRange.LOCAL, StockMode.STORAGE), of(StockRange.GLOBAL, StockMode.DEMAND));

    private static Pair<StockRange, StockMode> of(StockRange stockRange, StockMode stockMode) {
        return Pair.of(stockRange, stockMode);
    }

    public static final Codec<Pair<StockRange, StockMode>> PAIR_CODEC = Codec.pair(
            StockRange.CODEC.fieldOf("range").codec(),
            StockMode.CODEC.fieldOf("mode").codec());

    public static final Codec<TransferMode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PAIR_CODEC.fieldOf("first").forGetter(TransferMode::first),
            PAIR_CODEC.fieldOf("second").forGetter(TransferMode::second)
    ).apply(instance, TransferMode::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TransferMode> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(PAIR_CODEC), TransferMode::first,
            ByteBufCodecs.fromCodec(PAIR_CODEC), TransferMode::second,
            TransferMode::new
    );

    public Component getFirstComponentName() {
        return first.getFirst().getComponentName().copy().append(first.getSecond().getComponentName());
    }

    public Component getSecondComponentName() {
        return second.getFirst().getComponentName().copy().append(second.getSecond().getComponentName());
    }

    // 根据当前 supply 获取可替换指定插槽的所有候选 (range, mode) 组合
    public List<Pair<StockRange, StockMode>> possibleReplacements(boolean forFirst) {
        StockRange oldRange = forFirst ? this.first.getFirst() : this.second.getFirst();
        StockRange otherRange = forFirst ? this.second.getFirst() : this.first.getFirst();
        StockMode oldMode = forFirst ? this.second.getSecond() : this.first.getSecond();
        StockMode otherMode = forFirst ? this.first.getSecond() : this.second.getSecond();

        List<Pair<StockRange, StockMode>> candidates = new ArrayList<>();
        // 1. 相同 range 的其他 mode
        for (StockMode mode : StockMode.values()) {
            if (mode != oldMode && mode != otherMode) {
                candidates.add(of(oldRange, mode));
            }
        }

        // 2. 剩下的那个 range（既不是当前也不是另一个）的所有 mode
        for (StockRange range : StockRange.values()) {
            if (range != oldRange && range != otherRange) {
                for (StockMode mode : StockMode.values()) {
                    if (mode != oldMode) {
                        candidates.add(of(range, mode));
                    }
                }
            }
        }

        return candidates;
    }

    public TransferMode set(Pair<StockRange, StockMode> supply, boolean forFirst) {
        if(forFirst) {
            return new TransferMode(supply, this.second);
        } else {
            return new TransferMode(this.first, supply);
        }
    }
}
