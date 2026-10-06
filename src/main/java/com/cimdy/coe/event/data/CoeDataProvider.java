package com.cimdy.coe.event.data;

import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 把 {@link CoeDataBootstrap#JSON_CATEGORIES} 描述的已转 Provider 数据类别，经 codec 编码落盘为
 * {@code data/coe/coe/<path>/<id>.json}，路径与 LoadEvent 的 scanResources 前缀同源（取自对应 ResourceKey
 * 的 identifier path），故客户端 / 运行期取数不变。取代原 datapack 注册表的 JSON 序列化职责。
 *
 * <p>lookup 来自 {@code GatherDataEvent.getLookupProvider()} 的异步结果（与 DatapackBuiltinEntriesProvider 同约），
 * 在 {@link #run(CachedOutput)} 内 {@code thenCompose} 解析后传入各生成器——供需引用注册表（标签 / 实体类型）的
 * 类别（如伤害转换、生物修饰）做 HolderGetter 查找。
 */
public class CoeDataProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookupFuture;

    public CoeDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupFuture) {
        this.output = output;
        this.lookupFuture = lookupFuture;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return lookupFuture.thenCompose(lookup -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (JsonCategory<?> c : CoeDataBootstrap.JSON_CATEGORIES) {
                futures.add(writeCategory(cache, c, lookup));
            }
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
    }

    private <T> CompletableFuture<?> writeCategory(CachedOutput cache, JsonCategory<T> c, HolderLookup.Provider lookup) {
        Map<Identifier, T> out = new LinkedHashMap<>();
        c.gen().accept(lookup, out);
        Path dir = CoeDataPaths.coeRoot(output).resolve(c.path());
        List<CompletableFuture<?>> futures = new ArrayList<>();
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, lookup);
        for (Map.Entry<Identifier, T> e : out.entrySet()) {
            JsonElement json = c.codec().encodeStart(ops, e.getValue()).getOrThrow();
            futures.add(DataProvider.saveStable(cache, json, dir.resolve(e.getKey().getPath() + ".json")));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public String getName() {
        return "Coe Data (Provider)";
    }
}
