package com.cimdy.coe.data.codec.stronghold.task.helper.supply;

import com.cimdy.coe.data.codec.stronghold.task.helper.claim.MaterialClaim;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.*;

public class DetailedSuppliesPool {
        private final Map<ItemResource, Map<Integer, AggregatedSupply>> storage;

        public DetailedSuppliesPool(Map<ItemResource, Map<Integer, AggregatedSupply>> supplyGroups) {
            this.storage = new HashMap<>();
            for (var entry : supplyGroups.entrySet()) {
                this.storage.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
            }
        }

        private DetailedSuppliesPool(DetailedSuppliesPool other) {
            this.storage = new HashMap<>();
            for (var entry : other.storage.entrySet()) {
                Map<Integer, AggregatedSupply> copy = new LinkedHashMap<>();
                for (var inner : entry.getValue().entrySet()) {
                    copy.put(inner.getKey(), inner.getValue().copyWithAmount(inner.getValue().totalAmount()));
                }
                this.storage.put(entry.getKey(), copy);
            }
        }

        public int getAvailable(ItemResource res) {
            Map<Integer, AggregatedSupply> priMap = storage.get(res);
            if (priMap == null) return 0;
            return priMap.values().stream().mapToInt(AggregatedSupply::totalAmount).sum();
        }

        public List<MaterialClaim> reserve(ItemResource res, int amount) {
            Map<Integer, AggregatedSupply> priMap = storage.get(res);
            if (priMap == null) return Collections.emptyList();

            List<MaterialClaim> claims = new ArrayList<>();
            int remaining = amount;

            List<AggregatedSupply> sorted = new ArrayList<>(priMap.values());
            sorted.sort(Comparator.comparingInt(AggregatedSupply::priority).reversed());

            for (AggregatedSupply agg : sorted) {
                if (remaining <= 0) break;
                if (agg.totalAmount() <= 0) continue;

                int fromThis = Math.min(remaining, agg.totalAmount());
                List<SupplyInfo> sourcesCopy = new ArrayList<>(agg.sources());
                int takenFromAgg = 0;

                for (int i = 0; i < sourcesCopy.size() && takenFromAgg < fromThis; i++) {
                    SupplyInfo src = sourcesCopy.get(i);
                    int fromSrc = Math.min(fromThis - takenFromAgg, src.remaining());
                    if (fromSrc > 0) {
                        claims.add(new MaterialClaim(res, fromSrc, src.containerPos()));
                        sourcesCopy.set(i, src.withRemaining(src.remaining() - fromSrc));
                        takenFromAgg += fromSrc;
                    }
                }

                AggregatedSupply updated = agg.copyWithAmount(agg.totalAmount() - takenFromAgg);
                updated = new AggregatedSupply(updated.resource(), updated.priority(),
                        updated.totalAmount(), sourcesCopy);
                priMap.put(agg.priority(), updated);
                remaining -= takenFromAgg;
            }

            priMap.values().removeIf(a -> a.totalAmount() <= 0);
            if (priMap.isEmpty()) storage.remove(res);
            return claims;
        }

        public DetailedSuppliesPool copy() {
            return new DetailedSuppliesPool(this);
        }

        public void apply(DetailedSuppliesPool snapshot) {
            this.storage.clear();
            for (var entry : snapshot.storage.entrySet()) {
                this.storage.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
            }
        }
    }
