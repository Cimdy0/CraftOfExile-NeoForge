package com.cimdy.coe.data.gen.world;

import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.cimdy.coe.data.codec.talent.node.TalentNodeType;
import com.cimdy.coe.data.codec.talent.node.TalentNodeTypeRegistry;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * 数据生成：将内置天赋节点类型（取自 {@link TalentNodeTypeRegistry#builtinDefaults}）经 Codec 序列化为
 * {@code data/coe/coe/talent/player_talent/type/<id>.json}（位于本模组自定义数据根 {@code data/coe/coe/}，与天赋树同属 player_talent），
 * 使节点类型数据可经数据包覆盖或扩展，与天赋树一致的数据驱动范式。
 * 自定义节点类型只需在 {@code data/coe/coe/talent/player_talent/type/} 下新增同名 JSON 即可生效。
 *
 * <p>内置类型的定义集中在 {@link TalentNodeTypeRegistry}，本类不再重复写死，避免两处不同步。
 */
public class TalentNodeTypeProvider implements DataProvider {
    private final PackOutput output;

    public TalentNodeTypeProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path dir = CoeDataPaths.coeRoot(output)
                .resolve("talent").resolve("player_talent").resolve("type");

        TalentNodeType[] defaults = TalentNodeTypeRegistry.builtinDefaults()
                .toArray(new TalentNodeType[0]);

        CompletableFuture<?>[] futures = new CompletableFuture[defaults.length];
        for (int i = 0; i < defaults.length; i++) {
            TalentNodeType t = defaults[i];
            DataResult<JsonElement> result = TalentNodeType.CODEC.encodeStart(JsonOps.INSTANCE, t);
            JsonElement json = result.result().orElseThrow(() ->
                    new IllegalStateException("无法编码节点类型: " + t.id()));
            futures[i] = DataProvider.saveStable(cache, json, dir.resolve(t.id() + ".json"));
        }
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "Talent Node Type";
    }
}
