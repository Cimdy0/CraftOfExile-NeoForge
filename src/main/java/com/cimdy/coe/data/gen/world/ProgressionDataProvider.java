package com.cimdy.coe.data.gen.world;

import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.cimdy.coe.data.codec.progression.JobCharterDef;
import com.cimdy.coe.data.codec.progression.quest.*;
import com.cimdy.coe.data.codec.progression.research.EfficiencyBonus;
import com.cimdy.coe.data.codec.progression.research.ResearchEffect;
import com.cimdy.coe.data.codec.progression.research.ResearchNodeDef;
import com.cimdy.coe.data.codec.progression.research.WorkerPolicy;
import com.cimdy.coe.data.codec.progression.TechEffect;
import com.cimdy.coe.data.codec.progression.TechNodeDef;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 进度系统数据生成器（数据驱动：CODEC 工厂构造 → encode 成 JSON，取代手写资源文件）。
 *
 * <p>生成的 4 类 JSON 落在 {@code data/coe/coe/{job_charter,quest,tech_node,research_node}/<名>.json}，
 * 与 {@code ProgressionData} 的运行时扫描路径一一对应；id 一律不写入 JSON（由文件名注入），
 * 因此这里的「文件名的文件名」= 加载后的 id。删除旧手写文件后数据来源唯一化。
 */
public class ProgressionDataProvider implements DataProvider {
    private final PackOutput output;

    public ProgressionDataProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path root = CoeDataPaths.coeRoot(output);
        List<CompletableFuture<?>> futures = new ArrayList<>();

        // ---------- 工种授权（job_charter） ----------
        Path jobDir = root.resolve("job_charter");
        futures.add(save(cache, jobDir, "farming",
                JobCharterDef.CODEC,
                JobCharterDef.of("farming", 3, List.of(id("coe", "unlock_farming")))));
        futures.add(save(cache, jobDir, "mining",
                JobCharterDef.CODEC,
                JobCharterDef.of("mining", 5, List.of(id("coe", "unlock_mining")))));

        // ---------- 玩家任务（quest） ----------
        Path questDir = root.resolve("quest");
        futures.add(save(cache, questDir, "gather_iron",
                QuestDef.CODEC,
                new QuestDef(null, "采集铁矿",
                        List.of(QuestObjective.of(QuestType.GATHER, id("minecraft", "iron_ore"), 16)),
                        QuestPrerequisite.of(1, List.of(id("coe", "hunt_zombie")), List.of()),
                        QuestReward.of(300, 1, 3), 20)));
        futures.add(save(cache, questDir, "hunt_zombie",
                QuestDef.CODEC,
                new QuestDef(null, "清剿亡灵",
                        List.of(QuestObjective.of(QuestType.KILL, id("minecraft", "zombie"), 10)),
                        QuestPrerequisite.of(1, List.of(), List.of()),
                        QuestReward.of(200, 1, 2), 10)));

        // ---------- 科技节点（tech_node） ----------
        Path techDir = root.resolve("tech_node");
        futures.add(save(cache, techDir, "unlock_farming",
                TechNodeDef.CODEC,
                new TechNodeDef(null, "农业生产", 3, 1,
                        new TechEffect(List.of("farming"), List.of(), List.of(), 0, 1, 0, List.of(), List.of()),
                        5, "")));
        futures.add(save(cache, techDir, "unlock_mining",
                TechNodeDef.CODEC,
                new TechNodeDef(null, "采矿作业", 5, 1,
                        new TechEffect(List.of("mining"), List.of(), List.of(), 2, 2, 0, List.of(), List.of()),
                        10, "")));

        // ---------- 研究节点（research_node） ----------
        Path researchDir = root.resolve("research_node");
        futures.add(save(cache, researchDir, "base_research",
                ResearchNodeDef.CODEC,
                new ResearchNodeDef(null, "基础钻研", 0f, 0f, List.of(), 600, 1,
                        new ResearchEffect(List.of(new EfficiencyBonus("farming", 1.1f)), WorkerPolicy.EMPTY), "")));
        futures.add(save(cache, researchDir, "mining_speed_1",
                ResearchNodeDef.CODEC,
                new ResearchNodeDef(null, "采矿效率 I", 0f, 2f, List.of(id("coe", "base_research")), 1200, 2,
                        new ResearchEffect(List.of(new EfficiencyBonus("mining", 1.25f)), WorkerPolicy.EMPTY), "")));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }

    /** 通用保存：CODEC 工厂构造的对象 → encode 为 JSON → 写入 <dir>/<name>.json。 */
    private static <T> CompletableFuture<?> save(CachedOutput cache, Path dir, String name,
                                                 com.mojang.serialization.Codec<T> codec, T def) {
        DataResult<JsonElement> res = codec.encodeStart(JsonOps.INSTANCE, def);
        JsonElement json = res.result().orElseThrow(() ->
                new IllegalStateException("Failed to encode progression def " + name + ": " + res.error().orElse(null)));
        return DataProvider.saveStable(cache, json, dir.resolve(name + ".json"));
    }

    private static Identifier id(String ns, String path) {
        return Identifier.fromNamespaceAndPath(ns, path);
    }

    @Override
    public String getName() {
        return "Progression Data";
    }
}
