package com.cimdy.coe.data.codec.stronghold.storage.stock;

import com.cimdy.coe.data.codec.CoeCodecs;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;

public record StockEntry(ItemResource resource, TransferMode supply, int max, int transit, int total) {

    public static final Codec<StockEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemResource.OPTIONAL_CODEC.fieldOf("resource").forGetter(StockEntry::resource),
            TransferMode.CODEC.fieldOf("supply").forGetter(StockEntry::supply),
            Codec.INT.fieldOf("max").forGetter(StockEntry::max),
            Codec.INT.fieldOf("transit").forGetter(StockEntry::transit),
            Codec.INT.fieldOf("total").forGetter(StockEntry::total)
    ).apply(instance, StockEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StockEntry> STREAM_CODEC = StreamCodec.composite(
            CoeCodecs.ITEM_RESOURCE_STREAM, StockEntry::resource,
            TransferMode.STREAM_CODEC, StockEntry::supply,
            ByteBufCodecs.INT, StockEntry::max,
            ByteBufCodecs.INT, StockEntry::transit,
            ByteBufCodecs.INT, StockEntry::total,
            StockEntry::new
    );

    /**
     * 根据当前物理库存（amount）和容器设定，生成待搬运的需求/供应条目。
     * @param amount 容器中该物品的实际数量
     * @param minRatio 最低转运比例（0~1），用于计算 minTransfer
     */
    public List<TransferEntry> calculate(int amount, float minRatio, StrongholdHelper.TransportInfo info) {
        List<TransferEntry> entries = new ArrayList<>();
        int max = this.max();
        int minTransfer = (int) (max * minRatio);

        int out = info.outgoing();
        int in = info.incoming();

        // 净库存 = 实际数量 + 即将到货，但不低于0  - 即将取走不参与
        int netStock = Math.max(0, amount + in);

        // 有效供应 = 实际数量 - 已承诺取走的量，但不低于0 -即将到货不参与
        int effectiveSupply = Math.max(0, amount - out);

        Pair<StockRange, StockMode> first = supply.first();
        Pair<StockRange, StockMode> second = supply.second();

        // 需求：净库存不足 max 时，向网络声明缺口
        int demandShortage = Math.max(0, max - netStock);
        if (demandShortage > 0
                && (first.getSecond() == StockMode.DEMAND || second.getSecond() == StockMode.DEMAND)) {
            Pair<StockRange, StockMode> transferMode = first.getSecond() == StockMode.DEMAND ? first : second;
            entries.add(new TransferEntry(this.resource,
                    transferMode.getFirst(), transferMode.getSecond(), demandShortage, minTransfer));
        }

        // 供应：有效供应大于0时，上报全部可供应量
        if (effectiveSupply > 0
                && (first.getSecond() == StockMode.SUPPLY || second.getSecond() == StockMode.SUPPLY)) {
            Pair<StockRange, StockMode> transferMode = first.getSecond() == StockMode.SUPPLY ? first : second;
            entries.add(new TransferEntry(this.resource,
                    transferMode.getFirst(), transferMode.getSecond(), effectiveSupply, minTransfer));
        }

        return entries;
    }

    /** 是否存在非仓储的搬运行为 */
    public boolean hasCarry() {
        return this.supply.first().getSecond() != StockMode.STORAGE
                || this.supply.second().getSecond() != StockMode.STORAGE;
    }

    public static StockEntry empty() {
        return new StockEntry(ItemResource.EMPTY, TransferMode.DEFAULT, 64, 64, 64);
    }

    public boolean isEmpty() {
        return this.resource.isEmpty();
    }

    // ---------- 修改器方法（返回新实例） ----------
    public StockEntry setItem(ItemStack stack) {
        return new StockEntry(ItemResource.of(stack), this.supply, this.max,0, this.total);
    }

    public StockEntry setMax(int max) {
        return new StockEntry(this.resource, this.supply, max, this.transit, this.total);
    }

    public StockEntry setSupply(Pair<StockRange, StockMode> supply, boolean forFirst) {
        return new StockEntry(this.resource, this.supply.set(supply, forFirst), this.max, this.transit, this.total);
    }

    public StockEntry setTransit(int transit) {
        return new StockEntry(this.resource, this.supply, this.max, transit, this.total);
    }

    // 计算该物品在容器中的占用情况
    public static SlotRequirement calculateRequirement(ResourceHandler<ItemResource> handler, ItemResource resource, int max) {
        int alreadyStored = 0;
        int occupiedSlots = 0;
        int partialSlotRemaining = 0;
        int stackLimit = resource.toStack().getMaxStackSize();

        for (int slot = 0; slot < handler.size(); slot++) {
            ItemResource res = handler.getResource(slot);
            if (res.equals(resource)) {
                int amount = handler.getAmountAsInt(slot);
                alreadyStored += amount;
                occupiedSlots++;
                if (amount < stackLimit) {
                    partialSlotRemaining += (stackLimit - amount);
                }
            }
        }

        int needToStore = Math.max(0, max - alreadyStored);
        // 先用未满槽位填充
        int fromPartial = Math.min(needToStore, partialSlotRemaining);
        needToStore -= fromPartial;
        // 还需要多少个空槽位
        int needEmptySlots = (needToStore + stackLimit - 1) / stackLimit;

        // 该物品总共需要占用的槽位数 = 已占用槽位 + 额外需要的空槽位
        int desiredSlots = occupiedSlots + needEmptySlots;

        return new SlotRequirement(desiredSlots, stackLimit);
    }

    public boolean isDemand() {
        return this.supply().first().getSecond() == StockMode.DEMAND
                || this.supply().second().getSecond() == StockMode.DEMAND;
    }

    public record SlotRequirement(int desiredSlots, int stackLimit) {}
}