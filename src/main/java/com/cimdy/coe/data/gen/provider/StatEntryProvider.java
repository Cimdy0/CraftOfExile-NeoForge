package com.cimdy.coe.data.gen.provider;

import com.cimdy.coe.data.gen.stat.StatConditionIds;
import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import com.cimdy.coe.data.gen.stat.StatTargetIds;
import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffectTypes;
import com.cimdy.coe.data.gen.stat.StatGen;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Stat 条目数据生成器：把 {@link StatGen#entries()} 的全部 {@link StatEntry} 经 {@code StatEntry.CODEC}
 * 编码落盘为 {@code coe/stat/entry/&lt;path&gt;.json}。
 *
 * <p>条目内 effect / targets / conditions 三段<b>全部内联</b>，不再外置为独立资源文件。此前为复用而做的
 * 「内容寻址」（按内容算哈希/词元拼文件名、再写出独立 JSON 供引用来回跳转）已整体移除：
 * 身份一律取自生成期具名常量（{@code StatEffectIds} / {@code StatTargetIds} / {@code StatConditionIds}），
 * 内容哈希只会抹掉这些可读身份。
 *
 * <p>落盘统一走 vanilla {@code DataProvider.saveStable(cache, codec, value, path)}——其内部已含
 * GSON 写出 + SHA-1 稳定哈希 + {@code writeIfNeeded} 去重，故本类不自建任何 IO 与编码流程。
 */
public class StatEntryProvider implements DataProvider {
    private final PackOutput output;

    public StatEntryProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        // 落盘前预先扫一遍条目树：任何 effect 的 type 未注册都会在编码期落 UNKNOWN_TYPE 并炸掉整个
        // runData。与其让 saveStable 在半个 runData 之后才抛（且一次只能暴露一个），不如在这里把
        // <b>全部</b>缺失项一次性列出来。
        List<String> unregistered = findUnregisteredEffects(StatGen.entries());
        if (!unregistered.isEmpty()) {
            throw new IllegalStateException("unregistered stat effect types (missing register(...) in StatEffectTypes):\n  "
                    + String.join("\n  ", unregistered));
        }

        Path entryDir = CoeDataPaths.coeRoot(output).resolve("stat").resolve("entry");
        Map<Identifier, StatEntry> entries = StatGen.entries();
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<Identifier, StatEntry> e : entries.entrySet()) {
            futures.add(DataProvider.saveStable(cache, StatEntry.CODEC, e.getValue(),
                    entryDir.resolve(e.getKey().getPath() + ".json")));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }

    /**
     * 扫描条目树里所有 {@link com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect} 的 type，
     * 列出未在 {@code StatEffectTypes} 注册的那些（带所在条目 id，便于直接定位生成点）。
     */
    private static List<String> findUnregisteredEffects(Map<Identifier, StatEntry> entries) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<Identifier, StatEntry> e : entries.entrySet()) {
            // 别名条目（StatRead）的 effect 与被引用 canonical 完全一致，canonical 已在同轮被单独校验；
            // 且 StatRead.effect() 在 datagen 阶段会经 resolve() 查 STAT_ENTRY_MAP（此时为空）而抛错，
            // 故此处跳过别名，只校验具体条目。
            if (e.getValue() instanceof StatRead) continue;
            StatEffect effect = e.getValue().effect();
            String type = effect.type();
            if (!StatEffectTypes.isRegistered(type)) {
                out.add(e.getKey() + " -> " + type + "  (" + effect.getClass().getSimpleName() + ")");
            }
        }
        return out;
    }

    @Override
    public String getName() {
        return "Stat Entries";
    }
}
