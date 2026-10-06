package com.cimdy.coe.data.codec.stronghold.storage.stock;

import com.cimdy.coe.data.codec.stronghold.ItemCount;
import com.cimdy.coe.data.codec.stronghold.storage.Summary;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * 一条库存的展示快照：配置项 {@link StockEntry} + 当前实际库存。
 *
 * <p>只提供<b>数值</b>，怎么画由界面决定（列表行模板从这里读，不再往 NBT 里塞渲染键）。
 */
public record StockDisplay(StockEntry entry, int currentStock) {

    public int max() { return entry.max(); }

    public ItemResource resource() { return entry.resource(); }

    public TransferMode supply() { return entry.supply(); }

    /** 实际已有库存。 */
    public int stored() { return currentStock; }

    /** 在途数量（运输中）。 */
    public int transit() { return entry.transit(); }

    /** 已有库存色带的起点与长度：在途为正时压在已有库存之上，为负时从已有库存里扣掉。 */
    public double storedBand() {
        return entry.transit() > 0 ? stored() : stored() - entry.transit();
    }

    /** 在途色带的长度（可为负，表示待发出）。 */
    public double transitBand() {
        return entry.transit();
    }

    // ---------- 工厂方法：从 StockEntry 和 Summary 生成 ----------
    public static StockDisplay from(StockEntry entry, Summary containerSummary) {
        int current = containerSummary.summary().stream()
                .filter(item -> item.resource().equals(entry.resource()))
                .findFirst()
                .map(ItemCount::amount)
                .orElse(0);
        return new StockDisplay(entry, current);
    }
}
