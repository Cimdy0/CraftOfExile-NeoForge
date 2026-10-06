package com.cimdy.coe.data.gen.provider;

import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.cimdy.coe.data.gen.world.TalentNodeTypeProvider;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.tag.StatCategory;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 数据生成：把内置标签层级（{@link GateTag#builtinDefaults()}）写出为
 * {@code data/coe/coe/stat_category/<parent>.json}（重构 3-4）。
 *
 * <p>与 {@link TalentNodeTypeProvider} 同一范式：内置默认值只在 Java 侧定义一处，
 * datagen 负责导出为数据包内容，从而既保证"无数据包也能跑"（{@code GateTag} 内置兜底），
 * 又让父类别层级可被数据包覆盖或扩展——新增一个元素/异常类目不再需要改 Java 代码。
 */
public class GateTagCategoryProvider implements DataProvider {
    private final PackOutput output;

    public GateTagCategoryProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path dir = CoeDataPaths.coeRoot(output).resolve("stat_category");

        List<StatCategory> defaults = GateTag.builtinDefaults();
        CompletableFuture<?>[] futures = new CompletableFuture[defaults.size()];
        for (int i = 0; i < defaults.size(); i++) {
            StatCategory h = defaults.get(i);
            DataResult<JsonElement> result = StatCategory.CODEC.encodeStart(JsonOps.INSTANCE, h);
            JsonElement json = result.result().orElseThrow(() ->
                    new IllegalStateException("无法编码标签层级: " + h.parent()));
            futures[i] = DataProvider.saveStable(cache, json, dir.resolve(h.parent() + ".json"));
        }
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "Stat Tag Hierarchy";
    }
}
