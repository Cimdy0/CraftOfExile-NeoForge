package com.cimdy.coe.data.codec.talent.tree;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.codec.talent.node.TalentNodeType;
import com.cimdy.coe.data.codec.talent.node.TalentNodeTypeRegistry;
import com.cimdy.coe.CraftOfExile;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 天赋树数据加载器：从本模组自定义数据根 {@code data/coe/coe/talent/<id>/} 读取分散文件
 * （位于标准 datapack 命名空间 {@code data/coe/} 下、取代原 {@code data/coe/data/} 的自定义子根 {@code coe/}）。
 *
 * <p><b>目录结构</b>：
 * <pre>{@code
 * data/coe/coe/talent/<tree_id>/
 *   tree/info.json          # 树元数据
 *   node/<nodeId>.json      # 各节点独立文件（不含 id）
 *   type/<typeId>.json      # 节点类型定义
 * }</pre>
 *
 * <p><b>路径说明</b>：资源以「命名空间:路径」形式存在，{@code data/coe/coe/talent/...} 经 ResourceLocation
 * 解析后命名空间为 {@code coe}、路径为 {@code coe/talent/...}。故此处 {@code listResources} 的
 * 前缀匹配的是「路径」部分（{@code coe/talent} / {@code coe/talent/player_talent/type} 等），而非完整文件路径。
 *
 * <ul>
 *   <li>服务端：由 {@code LoadEvent} 的 {@code AddServerReloadListenersEvent} 在服务器/数据包重载时填充。</li>
 *   <li>客户端：界面打开时经 {@link #ensureClientLoaded()} 从客户端资源包懒加载。</li>
 * </ul>
 *
 * <p>若指定 id 未加载到，则回退到该树种类自带的默认模型。
 *
 * <p><b>布局缓存</b>：每个节点在数据中已携带显式世界坐标，加载阶段 O(n) 提取为
 * id → {x, y} 映射并缓存于 {@link #LAYOUTS}。
 */
public final class TalentTreeModelLoader {
    /** 新格式路径前缀（匹配 ResourceLocation 的 getPath()，即 coe/talent/<tree_id>/...）。 */
    private static final String NEW_ROOT = "coe/talent";
    /**
     * 天赋树 id（{@link Identifier}）→ 静态模型。键取自资源命名空间 + 树目录名（如 {@code coe:player_talent}）。
     * 该缓存为内存态、不参与序列化，故可安全以 {@link Identifier} 为键。
     */
    private static final Map<Identifier, TalentTreeModel> MODELS = new LinkedHashMap<>();
    /** 天赋树 id（Identifier）→（节点 id → 世界坐标）。由各节点显式 x/y 直接取得，于加载 / 重载时算好并缓存。 */
    private static final Map<Identifier, Map<String, float[]>> LAYOUTS = new HashMap<>();

    private TalentTreeModelLoader() {
    }

    /** 从资源包加载全部天赋树（服务端重载与客户端懒加载共用）。 */
    public static void load(ResourceManager manager) {
        TalentNodeTypeRegistry.load(manager); // 必须先于天赋树模型：树 JSON 以 id 引用节点类型
        MODELS.clear();
        LAYOUTS.clear();

        // 加载新格式：talent/<tree_id>/tree/info.json
        Map<Identifier, Resource> treeInfos = manager.listResources(NEW_ROOT,
                rl -> rl.getPath().endsWith("/tree/info.json"));
        for (Map.Entry<Identifier, Resource> entry : treeInfos.entrySet()) {
            String treeFolder = extractTreeId(entry.getKey().getPath(), "/tree/info.json");
            if (treeFolder == null) continue;
            // 树 id = 资源命名空间（data/<ns>/... 的 <ns>）+ 树目录名，如 coe:main
            Identifier treeId = Identifier.fromNamespaceAndPath(entry.getKey().getNamespace(), treeFolder);
            try (InputStream stream = entry.getValue().open()) {
                JsonObject info = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                List<String> startNodeIds = new ArrayList<>();
                if (info.has("start") && info.get("start").isJsonArray()) {
                    for (JsonElement e : info.getAsJsonArray("start")) {
                        startNodeIds.add(e.getAsString());
                    }
                }
                int totalPoints = info.has("total_points") ? info.get("total_points").getAsInt() : 20;
                int version = info.has("version") ? info.get("version").getAsInt() : 0;
                boolean resetOnUpdate = info.has("reset_on_update") && info.get("reset_on_update").getAsBoolean();

                // 加载节点：data/coe/coe/talent/<tree_id>/node/*.json
                List<TalentNodeDef> nodes = loadNodes(manager, treeFolder);

                TalentTreeModel model = new TalentTreeModel(nodes, startNodeIds, totalPoints, version, resetOnUpdate);
                MODELS.put(treeId, model);
                LAYOUTS.put(treeId, computeLayout(model));
            } catch (IOException e) {
                CraftOfExile.LOGGER.error("加载天赋树失败: " + entry.getKey() + " - " + e.getMessage());
            }
        }
    }

    /** 从路径 "coe/talent/<treeId>/tree/info.json" 提取 treeId（匹配 ResourceLocation 的 getPath()）。 */
    private static String extractTreeId(String path, String suffix) {
        if (!path.endsWith(suffix)) return null;
        String prefix = "coe/talent/";
        if (!path.startsWith(prefix)) return null;
        String sub = path.substring(prefix.length(), path.length() - suffix.length());
        return sub.isEmpty() ? null : sub;
    }

    /**
     * 加载某棵树的全部节点文件（{@code node/<type>/<name>.json}，每文件为节点数组）。
     *
     * <p>节点类型与名均不再存于 JSON：{@code <type>} 取自 {@code node/} 后的第一层目录名，
     * {@code <name>} 取自文件名（不含扩展名）；解析后分别经 {@link TalentNodeDef#withType}
     * 与 {@link TalentNodeDef#withName} 为该文件的每个节点注入并随模型一并缓存。
     */
    private static List<TalentNodeDef> loadNodes(ResourceManager manager, String treeId) throws IOException {
        String nodePath = NEW_ROOT + "/" + treeId + "/node";
        Map<Identifier, Resource> nodeResources = manager.listResources(
                nodePath, rl -> rl.getPath().endsWith(".json"));
        List<TalentNodeDef> nodes = new ArrayList<>();
        for (Map.Entry<Identifier, Resource> entry : nodeResources.entrySet()) {
            TalentNodeType type = TalentNodeTypeRegistry.require(
                    extractNodeType(entry.getKey().getPath()));
            String name = extractNodeName(entry.getKey().getPath());
            try (InputStream stream = entry.getValue().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream));
                DataResult<List<TalentNodeDef>> result = TalentNodeDef.CODEC.listOf().parse(JsonOps.INSTANCE, json);
                result.result().ifPresent(list -> {
                    for (TalentNodeDef def : list) nodes.add(def.withType(type).withName(name));
                });
                result.error().ifPresent(error ->
                        CraftOfExile.LOGGER.error("加载节点失败: " + entry.getKey() + " - " + error.message()));
            }
        }
        return nodes;
    }

    /** 从 "coe/talent/<treeId>/node/<type>/<name>.json" 提取 {@code <type>}（node 后的第一层目录名）。 */
    private static String extractNodeType(String fullPath) {
        String marker = "/node/";
        int idx = fullPath.indexOf(marker);
        if (idx < 0) return "";
        String sub = fullPath.substring(idx + marker.length());
        int slash = sub.indexOf('/');
        return slash > 0 ? sub.substring(0, slash) : "";
    }

    /** 从 "coe/talent/<treeId>/node/<type>/<name>.json" 提取 {@code <name>}（文件名，不含 .json 扩展名）。 */
    private static String extractNodeName(String fullPath) {
        String file = fullPath.substring(fullPath.lastIndexOf('/') + 1);
        int dot = file.lastIndexOf('.');
        return dot > 0 ? file.substring(0, dot) : file;
    }

    /**
     * 客户端兜底加载：仅在服务端重载尚未填充时（如专用服务器端客户端）从客户端资源包加载。
     *
     * <p>集成服务端（单人游戏）的 {@code AddServerReloadListenersEvent} 监听器已将整棵天赋树写入本共享静态
     * {@link #MODELS}；而客户端自身的资源包通常并不包含该数据包，故此处绝不可用客户端的一次性空加载去
     * 覆盖服务端已填充的数据，否则树会在首次打开时为空，必须手动 {@code /reload} 后才正常。
     * 因此仅在 {@link #MODELS} 为空时才回退到客户端资源包加载，且不做「只加载一次」的粘性缓存。
     */
    public static void ensureClientLoaded() {
        // 客户端兜底加载：专用服务端不应进入（无 Minecraft 资源包），提前返回避免 NoClassDefFoundError
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        if (MODELS.isEmpty()) {
            load(Minecraft.getInstance().getResourceManager());
        }
    }

    /**
     * 按树 id（{@link Identifier}）取模型。模型由数据包（{@code data/coe/coe/talent/<树名>/...}）解析后缓存于
     * {@link #MODELS}，直接按 id 取用；未加载到时返回空模型（不再回退到默认模型）。
     */
    public static TalentTreeModel get(Identifier id) {
        return MODELS.getOrDefault(id, TalentTreeModel.EMPTY);
    }

    /** 已加载的全部天赋树 id（按数据包加载顺序）。用于径向菜单等数据驱动地列出所有树。 */
    public static List<Identifier> treeIds() {
        return new ArrayList<>(MODELS.keySet());
    }

    /**
     * 天赋树显示名：固定翻译键格式 {@code talent.<namespace>.<路径>}
     * （由树 id 的命名空间与路径拼接），对应 {@code lang} 中的本地化条目。
     */
    public static Component displayName(Identifier treeId) {
        return Component.translatable("talent." + treeId.getNamespace() + "." + treeId.getPath());
    }

    /**
     * 取已缓存的布局坐标（节点 id → {x, y}）。布局在加载阶段已算好并缓存；
     * 若缺失则即时提取并缓存。
     */
    public static Map<String, float[]> getLayout(Identifier id) {
        return LAYOUTS.computeIfAbsent(id, k -> computeLayout(get(k)));
    }

    /** 由各节点显式坐标构建 id → {x, y} 映射（O(n)）。 */
    private static Map<String, float[]> computeLayout(TalentTreeModel model) {
        Map<String, float[]> pos = new LinkedHashMap<>();
        for (TalentNodeDef d : model.nodes()) {
            pos.put(d.id(), new float[]{d.x(), d.y()});
        }
        return pos;
    }
}
