package com.cimdy.coe.data.gen;

import com.cimdy.coe.data.codec.support.math.context.ContextNode;
import com.cimdy.coe.data.gen.stat.StatConditionGen;
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
 * Stat condition 独立数据生成器（CODEC 工厂：内容由 {@link StatConditionGen} 构造为真实
 * {@code List<ContextNode>} 对象，本类经 {@link ContextNode#CODEC} 列表编码落盘）。
 *
 * <p>每个唯一 condition 子条目经 {@code StatConditionIds} 持有内容寻址路径，内容（真实 condition 对象列表）由
 * {@link StatConditionGen#contents()} 提供，本类逐条写入 {@code coe/stat/condition/&lt;name&gt;.json}。
 * 运行时由 {@code LoadEvent#STAT_CONDITION_MAP} 按 id 加载缓存，供 {@code ContextNodeRead} 解析。
 */
public class StatConditionProvider implements DataProvider {
    private final PackOutput output;

    public StatConditionProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path dir = CoeDataPaths.coeRoot(output).resolve("stat").resolve("condition");
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<Identifier, List<ContextNode>> e : StatConditionGen.contents().entrySet()) {
            futures.add(StatDataIO.save(cache, dir, e.getKey().getPath(), ContextNode.CODEC.listOf(), e.getValue()));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }

    @Override
    public String getName() {
        return "Stat Conditions";
    }
}
