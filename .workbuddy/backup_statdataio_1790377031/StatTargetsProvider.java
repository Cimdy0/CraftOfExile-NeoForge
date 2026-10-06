package com.cimdy.coe.data.gen;

import com.cimdy.coe.data.codec.support.stat.effect.StatTargets;
import com.cimdy.coe.data.codec.support.stat.effect.StatTargetsRead;
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
 * Stat targets 独立数据生成器（CODEC 工厂：内容由 {@link StatTargetGen} 构造为真实 StatTargets 对象，
 * 本类经 {@link StatTargets#CODEC} 编码落盘）。
 *
 * <p>每个唯一 targets 子条目经 {@code StatTargetIds} 持有内容寻址路径，内容（真实 StatTargets 对象）由
 * {@link StatTargetGen#contents()} 提供，本类逐条写入 {@code coe/stat/targets/&lt;name&gt;.json}。
 * 运行时由 {@code LoadEvent#STAT_TARGET_MAP} 按 id 加载缓存，供 {@code StatTargetsRead} 解析。
 */
public class StatTargetsProvider implements DataProvider {
    private final PackOutput output;

    public StatTargetsProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path dir = CoeDataPaths.coeRoot(output).resolve("stat").resolve("targets");
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<Identifier, StatTargets> e : StatTargetGen.contents().entrySet()) {
            futures.add(StatDataIO.save(cache, dir, e.getKey().getPath(), StatTargets.CODEC, e.getValue()));
        }
        // 接线期组合出的三段（如 target=minion + value=minion）：经 ofInline 内容寻址登记，
        // 由本 Provider 一并落盘，否则 stat_entry 会引用不存在的 stat_targets 资源。
        for (Map.Entry<Identifier, StatTargets> e : StatTargetsRead.extras().entrySet()) {
            futures.add(StatDataIO.save(cache, dir, e.getKey().getPath(), StatTargets.CODEC, e.getValue()));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }

    @Override
    public String getName() {
        return "Stat Targets";
    }
}
