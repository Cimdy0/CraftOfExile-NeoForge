package com.cimdy.coe.data.gen.provider;

import com.cimdy.coe.data.codec.support.damage.DamageTypeDef;
import com.cimdy.coe.data.codec.support.damage.DamageTypes;
import com.cimdy.coe.data.gen.source.CoeDataPaths;
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
 * 数据生成：把内置伤害类型定义（{@link DamageTypes#builtinDefaults()}）写出为
 * {@code data/coe/coe/damage_type/<id>.json}（ADR-003 阶段1）。
 *
 * <p>与 {@link GateTagCategoryProvider} 同一范式：内置默认值只在 Java 侧定义一处
 * （{@link DamageTypes#builtinDefaults()}），datagen 负责导出为数据包内容，从而既保证"无数据包也能跑"
 * （{@code DamageTypes} 内置兜底），又让伤害类型可被数据包覆盖或扩展——新增一个伤害类型不再需要改 Java 代码。
 *
 * <p>运行时由 {@code LoadEvent} 的 {@code coe/damage_type} 监听调用 {@link DamageTypes#load} 装载，
 * 槽位冲突在装载期硬失败（不再静默错位）。
 */
public class DamageTypeProvider implements DataProvider {
    private final PackOutput output;

    public DamageTypeProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path dir = CoeDataPaths.coeRoot(output).resolve("damage_type");

        List<DamageTypeDef> defaults = DamageTypes.all();
        CompletableFuture<?>[] futures = new CompletableFuture[defaults.size()];
        for (int i = 0; i < defaults.size(); i++) {
            DamageTypeDef def = defaults.get(i);
            DataResult<JsonElement> result = DamageTypeDef.CODEC.encodeStart(JsonOps.INSTANCE, def);
            JsonElement json = result.result().orElseThrow(() ->
                    new IllegalStateException("无法编码伤害类型: " + def.id()));
            futures[i] = DataProvider.saveStable(cache, json, dir.resolve(def.id() + ".json"));
        }
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "Damage Type";
    }
}
