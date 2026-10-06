package com.cimdy.coe.data.codec.support.status.runtime;

import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * P2-3 异常转存存储（store/transfer 通道的持久载体）。
 *
 * <p>承载被消费的异常之「记账」数值（强度/剩余时长/层数），由
 * {@link AilmentTransfer} 在 {@code StatusEngine.consumeAilment} 的 clear 之后写入、
 * 供 store/transfer 消费方（blood_hunt / elemental_sundering 等 POE2「Consume X」族）取用。本类仅持有数据，
 * 语义由 {@link AilmentTransfer} 驱动，与 {@link StatusStore} 同源（Map 以 {@link StatusType} 为键）。
 *
 * <p>作为附件挂在 LivingEntity 上（{@code AttachmentRegistry.TRANSFER_STORE}），
 * 复用 {@code StatusStore} 的非同步附件风格：服务端权威、不随死亡复制、不随客户端同步
 * （战斗期瞬时记账，重启/重生后清空）。
 */
public final class TransferStore {

    public static final TransferStore EMPTY = new TransferStore();

    /** type -> 被消费异常的记账数值（覆盖式写入：同一异常仅保留最新一次消费记录）。 */
    public final Map<StatusType, TransferRecord> records = new LinkedHashMap<>();

    public TransferRecord get(StatusType type) {
        return records.get(type);
    }

    public boolean has(StatusType type) {
        return records.containsKey(type);
    }

    public void put(TransferRecord rec) {
        records.put(rec.type, rec);
    }

    public void remove(StatusType type) {
        records.remove(type);
    }

    // ── Codec：以 type.id -> record 序列化（与 StatusStore 同口径） ──
    public static final Codec<TransferStore> CODEC = Codec.unboundedMap(Codec.STRING, TransferRecord.CODEC)
            .xmap(
                    map -> {
                        TransferStore s = new TransferStore();
                        map.forEach((id, rec) -> {
                            if (rec != null && rec.type != null) s.records.put(rec.type, rec);
                        });
                        return s;
                    },
                    s -> {
                        Map<String, TransferRecord> m = new HashMap<>();
                        s.records.forEach((t, r) -> m.put(t.id, r));
                        return m;
                    });

    /**
     * 一条被消费异常的记账记录：原始数值快照（强度/剩余时长/层数）。
     * 仅作数值搬运载体，不含任何施加/重施语义。
     */
    public record TransferRecord(StatusType type, int intensity, int remaining, int stacks) {
        public static final Codec<TransferRecord> CODEC = RecordCodecBuilder.create(i -> i.group(
                StatusType.CODEC.fieldOf("type").forGetter(TransferRecord::type),
                Codec.INT.fieldOf("intensity").forGetter(TransferRecord::intensity),
                Codec.INT.fieldOf("remaining").forGetter(TransferRecord::remaining),
                Codec.INT.fieldOf("stacks").forGetter(TransferRecord::stacks)
        ).apply(i, TransferRecord::new));
    }
}
