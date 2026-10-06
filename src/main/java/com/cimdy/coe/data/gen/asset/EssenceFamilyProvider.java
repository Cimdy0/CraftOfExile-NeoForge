package com.cimdy.coe.data.gen.asset;

import com.cimdy.coe.data.gen.source.EssenceFamilies;
import com.cimdy.coe.data.codec.item.essence.EssenceFamily;
import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 数据生成（<b>生成类</b>）：把 {@link EssenceFamilies} 声明的精髓族逐个编码落盘为
 * {@code data/coe/coe/item/essence/<族名>.json}。
 *
 * <p>本类<b>只负责编码与落盘</b>，不含任何族数据 —— 族数据（同族全阶）集中在
 * {@link EssenceFamilies}（生成侧存储类），与运行时只含单阶的 {@code EssenceState} 彼此分离。</p>
 *
 * <p>运行时由 {@code EssenceRegistry} 从 classpath / datapack 读取这些族文件，
 * 按 {@link com.cimdy.coe.data.codec.item.tiered.Tier} <b>展开为「一族 × 一阶」多条条目</b>。</p>
 */
public class EssenceFamilyProvider implements DataProvider {

    private final PackOutput output;

    public EssenceFamilyProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path dir = CoeDataPaths.coeRoot(output).resolve("item/essence");
        Map<String, EssenceFamily> families = EssenceFamilies.families();
        CompletableFuture<?>[] futures = new CompletableFuture[families.size()];
        int i = 0;
        for (Map.Entry<String, EssenceFamily> e : families.entrySet()) {
            DataResult<JsonElement> result = EssenceFamily.CODEC.encodeStart(JsonOps.INSTANCE, e.getValue());
            JsonElement json = result.result().orElseThrow(() ->
                    new IllegalStateException("无法编码精髓族: " + e.getKey()));
            futures[i++] = DataProvider.saveStable(cache, json, dir.resolve(e.getKey() + ".json"));
        }
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "EssenceFamilyProvider";
    }
}
