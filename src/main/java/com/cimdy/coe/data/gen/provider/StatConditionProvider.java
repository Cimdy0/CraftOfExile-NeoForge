package com.cimdy.coe.data.gen.provider;

import com.cimdy.coe.data.gen.stat.StatEntityConditionGen;
import com.cimdy.coe.event.data.LoadEvent;
import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.cimdy.coe.data.codec.support.stat.condition.StatCondition;
import com.cimdy.coe.data.codec.support.stat.condition.StatConditionTypes;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Stat condition 子条目落盘生成器：把 {@link StatEntityConditionGen#contents()} 的每一个<b>具名</b>
 * 实体获取条件写入 {@code stat/condition/<id>.json}。
 *
 * <p>完全镜像 {@link StatEffectProvider} / {@link StatTargetProvider}：身份取具名常量
 * （{@code StatConditionIds.CAC_*}），不做内容哈希；运行时由 {@code LoadEvent} 按
 * {@code stat/condition} 目录扫描加载，供 {@code StatRead} 条目以
 * {@code "condition": <id>} 引用。
 *
 * <p>此前 condition 段仅由 {@code StatEntry#internCondition} 在内存登记（anon id），既无落盘也无
 * 运行期加载通道，导致任何持久化条目引用 condition 在运行期 {@code StatRead#condition()} 抛
 * 「Stat 引用了未注册的 condition」。本类补齐与 effect / targets 对称的运行期加载机制。
 */
public class StatConditionProvider implements DataProvider {
    private final PackOutput output;

    public StatConditionProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        // datagen 期预填具名 condition 到 STAT_CONDITION_MAP：与运行期 LoadEvent 扫描
        // stat/condition JSON 行为一致，使下游 StatEntryProvider 经 StatRead.condition() 解析具名 ref
        // 时能命中，避免预扫抛「Stat 引用了未注册的 condition」。
        for (Map.Entry<Identifier, StatCondition> e : StatEntityConditionGen.contents().entrySet()) {
            LoadEvent.STAT_CONDITION_MAP.put(e.getKey(), e.getValue());
        }
        Path dir = CoeDataPaths.coeRoot(output).resolve("stat").resolve("condition");
        return saveAll(cache, dir, StatEntityConditionGen.contents());
    }

    private static CompletableFuture<?> saveAll(CachedOutput cache, Path dir,
                                                Map<Identifier, ? extends StatCondition> conditions) {
        // 落盘前先校验每个具名 condition 都能被 StatConditionTypes 反查编码，否则运行时解码会把
        // 未注册的 type 当成未知节点，且在「子部件外置」形态下要等到读取时才炸。
        List<String> unregistered = new ArrayList<>();
        for (Map.Entry<Identifier, ? extends StatCondition> e : conditions.entrySet()) {
            if (StatConditionTypes.typeOf(e.getValue()) == null) {
                unregistered.add(e.getKey() + " -> " + e.getValue().getClass().getSimpleName());
            }
        }
        if (!unregistered.isEmpty()) {
            throw new IllegalStateException("unregistered stat condition types (missing register(...) in StatConditionTypes):\n  "
                    + String.join("\n  ", unregistered));
        }

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<Identifier, ? extends StatCondition> e :
                new LinkedHashMap<>(conditions).entrySet()) {
            futures.add(DataProvider.saveStable(cache, StatConditionTypes.CODEC, e.getValue(),
                    dir.resolve(e.getKey().getPath() + ".json")));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }

    @Override
    public String getName() {
        return "Stat Conditions";
    }
}