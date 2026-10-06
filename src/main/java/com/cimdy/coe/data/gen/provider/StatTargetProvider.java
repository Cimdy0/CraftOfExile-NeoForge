package com.cimdy.coe.data.gen.provider;

import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.cimdy.coe.event.data.LoadEvent;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.target.StatTargetsTypes;
import com.cimdy.coe.data.gen.stat.StatTargetGen;
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
 * Stat targets 子条目落盘生成器：把 {@link StatTargetGen#contents()} 的每一个具名
 * 选择器预设写入 {@code stat/targets/&lt;id&gt;.json}。
 *
 * <p>身份一律取生成期具名常量（{@code StatTargetIds.TGT_*}），不做内容哈希。
 * 运行时由 {@code LoadEvent} 按 {@code stat/targets} 目录扫描加载，
 * 供 {@code Stat} 条目以 {@code {"entry":"read","ref":"coe:…"}} 引用。
 */
public class StatTargetProvider implements DataProvider {
    private final PackOutput output;

    public StatTargetProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        // 对称预填具名 targets 到 STAT_TARGET_MAP（与 StatEffectProvider 填 effect 同理），
        // 供下游 datagen 消费方经 Stat.targets() 解析具名 ref 时命中。
        for (Map.Entry<Identifier, StatTargets> e : StatTargetGen.contents().entrySet()) {
            LoadEvent.STAT_TARGET_MAP.put(e.getKey(), e.getValue());
        }
        Path dir = CoeDataPaths.coeRoot(output).resolve("stat").resolve("targets");
        Map<Identifier, StatTargets> targets = StatTargetGen.contents();
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<Identifier, StatTargets> e : targets.entrySet()) {
            futures.add(DataProvider.saveStable(cache, StatTargetsTypes.MAP_CODEC.codec(), e.getValue(),
                    dir.resolve(e.getKey().getPath() + ".json")));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }

    @Override
    public String getName() {
        return "Stat Targets";
    }
}
