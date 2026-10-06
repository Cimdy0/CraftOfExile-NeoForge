package com.cimdy.coe.data.codec.progression;

import com.cimdy.coe.data.codec.talent.tree.TalentTreeModelLoader;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.progression.JobCharterDef;
import com.cimdy.coe.data.codec.progression.quest.QuestDef;
import com.cimdy.coe.data.codec.progression.research.ResearchNodeDef;
import com.cimdy.coe.data.codec.progression.TechNodeDef;
import com.cimdy.coe.event.net.NetEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 进度系统静态数据容器（原版数据驱动：数据包 JSON + Codec + 重载监听）。
 *
 * <p>四个子系统（任务 / 科技树 / 研究树 / 工种授权）的数据均从 {@code data/<ns>/coe/<kind>/<id>.json}
 * 加载，与天赋树 / recipe_site 同一套 vanilla 数据驱动范式。id 由文件名注入（JSON 不含 id）。
 * 服务端重载填充，客户端 UI 通过同一资源包懒加载（参照 {@code TalentTreeModelLoader}）。
 */
public final class ProgressionData {

    public static final Identifier QUEST_PATH = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "coe/quest");
    public static final Identifier TECH_PATH = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "coe/tech_node");
    public static final Identifier RESEARCH_PATH = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "coe/research_node");
    public static final Identifier JOB_PATH = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "coe/job_charter");

    public static final Map<Identifier, QuestDef> QUESTS = new HashMap<>();
    public static final Map<Identifier, TechNodeDef> TECH_NODES = new HashMap<>();
    public static final Map<Identifier, ResearchNodeDef> RESEARCH_NODES = new HashMap<>();
    public static final Map<Identifier, JobCharterDef> JOB_CHARTERS = new HashMap<>();

    private ProgressionData() {
    }

    /** 一次性加载全部进度数据（由 {@code LoadEvent} 的重载监听器调用）。 */
    public static void load(ResourceManager manager) {
        loadQuests(manager);
        loadTech(manager);
        loadResearch(manager);
        loadJobs(manager);
    }

    /**
     * 客户端接收服务端同步：整表替换四张界面需要的定义表。
     *
     * <p>为什么必须同步而不是让客户端自己读资源包：这些定义在 {@code data/} 下，
     * 属于<b>数据包</b>而非资源包。单人游戏靠集成服务端共享同一份静态表侥幸能用，
     * 但专用服务器上客户端根本读不到——科技树 / 研究树 / 任务 HUD / 工种授权会全空。
     * 因此在玩家登录与数据包重载时由服务端推送一次（见 {@code NetEvent}）。
     */
    public static void applyNetworkSync(List<QuestDef> quests, List<TechNodeDef> tech,
                                       List<ResearchNodeDef> research, List<JobCharterDef> jobCharters) {
        QUESTS.clear();
        for (QuestDef def : quests) if (def.id() != null) QUESTS.put(def.id(), def);
        TECH_NODES.clear();
        for (TechNodeDef def : tech) if (def.id() != null) TECH_NODES.put(def.id(), def);
        RESEARCH_NODES.clear();
        for (ResearchNodeDef def : research) if (def.id() != null) RESEARCH_NODES.put(def.id(), def);
        JOB_CHARTERS.clear();
        for (JobCharterDef def : jobCharters) if (def.id() != null) JOB_CHARTERS.put(def.id(), def);
    }

    /** 按科技节点的 {@code min_level} 升序、同级按 {@code sort_order} 排序（科技树界面按等级分段用）。 */
    public static List<TechNodeDef> techSorted() {
        List<TechNodeDef> list = new ArrayList<>(TECH_NODES.values());
        list.sort(Comparator.comparingInt(TechNodeDef::minLevel).thenComparingInt(TechNodeDef::sortOrder));
        return list;
    }

    /** 研究节点按 id 稳定排序（画布布局靠节点自带 x/y，此处只保证遍历顺序确定）。 */
    public static List<ResearchNodeDef> researchSorted() {
        List<ResearchNodeDef> list = new ArrayList<>(RESEARCH_NODES.values());
        list.sort(Comparator.comparing(def -> def.id().toString()));
        return list;
    }

    private static void loadQuests(ResourceManager manager) {
        QUESTS.clear();
        for (Map.Entry<Identifier, Resource> e : manager.listResources(QUEST_PATH.getPath(),
                rl -> rl.getPath().endsWith(".json")).entrySet()) {
            parse(e, QuestDef.CODEC, def -> def.withId(idOf(e.getKey(), fileName(e.getKey().getPath()))),
                    "任务", QUESTS);
        }
    }

    private static void loadTech(ResourceManager manager) {
        TECH_NODES.clear();
        for (Map.Entry<Identifier, Resource> e : manager.listResources(TECH_PATH.getPath(),
                rl -> rl.getPath().endsWith(".json")).entrySet()) {
            parse(e, TechNodeDef.CODEC, def -> def.withId(idOf(e.getKey(), fileName(e.getKey().getPath()))),
                    "科技节点", TECH_NODES);
        }
    }

    private static void loadResearch(ResourceManager manager) {
        RESEARCH_NODES.clear();
        for (Map.Entry<Identifier, Resource> e : manager.listResources(RESEARCH_PATH.getPath(),
                rl -> rl.getPath().endsWith(".json")).entrySet()) {
            parse(e, ResearchNodeDef.CODEC, def -> def.withId(idOf(e.getKey(), fileName(e.getKey().getPath()))),
                    "研究节点", RESEARCH_NODES);
        }
    }

    private static void loadJobs(ResourceManager manager) {
        JOB_CHARTERS.clear();
        for (Map.Entry<Identifier, Resource> e : manager.listResources(JOB_PATH.getPath(),
                rl -> rl.getPath().endsWith(".json")).entrySet()) {
            parse(e, JobCharterDef.CODEC, def -> def.withId(idOf(e.getKey(), fileName(e.getKey().getPath()))),
                    "工种授权", JOB_CHARTERS);
        }
    }

    /**
     * 通用解析：读 JSON → Codec 解码 → 注入 id → 写入目标 Map。
     * 解耦四个加载器重复的 try/parse/error 样板。
     */
    private static <T> void parse(Map.Entry<Identifier, Resource> entry,
                                  com.mojang.serialization.Codec<T> codec,
                                  java.util.function.Function<T, T> withId,
                                  String kind, Map<Identifier, T> sink) {
        try (InputStream stream = entry.getValue().open()) {
            JsonElement json = JsonParser.parseReader(new InputStreamReader(stream));
            DataResult<T> result = codec.parse(JsonOps.INSTANCE, json);
            result.result().ifPresent(def -> {
                Identifier id = idOf(entry.getKey(), fileName(entry.getKey().getPath()));
                sink.put(id, withId.apply(def));
            });
            result.error().ifPresent(error ->
                    CraftOfExile.LOGGER.error("加载" + kind + "失败: " + entry.getKey().getPath() + " " + error.message()));
        } catch (IOException ex) {
            CraftOfExile.LOGGER.error("读取" + kind + "资源失败: " + entry.getKey().getPath() + " " + ex.getMessage());
        }
    }

    /** 由资源 key 的命名空间 + 文件名（去 .json）拼出 Identifier id。 */
    private static Identifier idOf(Identifier resourceKey, String fileName) {
        return Identifier.fromNamespaceAndPath(resourceKey.getNamespace(), fileName);
    }

    /** 从路径取文件名（去扩展名）。 */
    private static String fileName(String path) {
        String f = path.substring(path.lastIndexOf('/') + 1);
        int dot = f.lastIndexOf('.');
        return dot > 0 ? f.substring(0, dot) : f;
    }
}
