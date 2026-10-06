package com.cimdy.coe.data.gen.world;

import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.gen.talent.TalentNodeGroups;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TalentTreeProvider implements DataProvider {
    private final PackOutput output;

    public TalentTreeProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path root = CoeDataPaths.coeRoot(output).resolve("talent").resolve("player_talent");
        Path nodeDir = root.resolve("node");
        Path treeInfo = root.resolve("tree").resolve("info.json");
        List<CompletableFuture<?>> futures = new ArrayList<>();
        // 收集：type -> name -> 该名下的全部节点。
        // 文件名即节点 name（JSON 不再写入 name，改由加载时从路径文件名注入）；
        // 同名节点（跨类）合并进同一 JSON，与生成类如何拆分无关。
        Map<String, Map<String, List<TalentNodeDef>>> byTypeThenName = new LinkedHashMap<>();
        for (Map.Entry<String, List<TalentNodeDef>> g : TalentNodeGroups.GROUPS.entrySet()) {
            String type = g.getKey().split("/")[0];
            Map<String, List<TalentNodeDef>> byName = byTypeThenName
                    .computeIfAbsent(type, k -> new LinkedHashMap<>());
            for (TalentNodeDef def : g.getValue()) {
                String nm = def.name();
                if (nm == null || nm.isBlank()) {
                    throw new IllegalStateException(
                            "节点缺少 name，无法作为 JSON 文件名: id=" + def.id() + " (key=" + g.getKey() + ")");
                }
                byName.computeIfAbsent(nm, k -> new ArrayList<>()).add(def);
            }
        }
        for (Map.Entry<String, Map<String, List<TalentNodeDef>>> typeEntry : byTypeThenName.entrySet()) {
            for (Map.Entry<String, List<TalentNodeDef>> nameEntry : typeEntry.getValue().entrySet()) {
                DataResult<JsonElement> res = TalentNodeDef.CODEC.listOf()
                        .encodeStart(JsonOps.INSTANCE, nameEntry.getValue());
                JsonElement json = res.result().orElseThrow(() ->
                        new IllegalStateException("Failed to encode talent nodes: "
                                + typeEntry.getKey() + "/" + nameEntry.getKey()));
                Path file = nodeDir.resolve(typeEntry.getKey()).resolve(nameEntry.getKey() + ".json");
                futures.add(DataProvider.saveStable(cache, json, file));
            }
        }
        futures.add(DataProvider.saveStable(cache, JsonParser.parseString(TREE_INFO), treeInfo));
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }

    @Override
    public String getName() {
        return "Talent Tree";
    }

    private static final String TREE_INFO = """

    {
      "start": [
        "47175",
        "61525",
        "54447",
        "50986",
        "44683",
        "50459"
      ],
      "total_points": 120,
      "version": 1,
      "reset_on_update": true
    }
    
    
    """;
}
