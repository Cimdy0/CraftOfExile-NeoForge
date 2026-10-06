package com.cimdy.coe.data.codec.entity.render;

import com.cimdy.coe.data.codec.talent.tree.TalentTreeModelLoader;
import com.cimdy.coe.CraftOfExile;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 道具渲染定义的运行时注册表 —— {@code data/<ns>/coe/prop_render/*.json} 全部条目。
 *
 * <p>与 {@code SKILL_ENTRY_MAP} / {@code StatusType} 同范式：datagen 生成 JSON，
 * 由 {@code LoadEvent} 在服务端资源重载时整体读入内存 Map；定义<b>只读</b>，
 * 运行期不修改，故无需同步。
 *
 * <p>解析失败仅记日志并跳过该条目（不让单个坏数据拖垮整个资源重载）——
 * 与技能 / 精魂的加载口径一致。
 */
public final class EntityRenderDefs {

    /** 数据根路径（位于 {@code data/<ns>/} 之下）。 */
    public static final String ROOT = "coe/entity_render";
    /** 资源重载监听器键（{@code AddServerReloadListenersEvent#addListener} 取 Identifier 而非路径字符串）。 */
    public static final Identifier KEY =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "entity_render");

    private static final Map<Identifier, EntityRenderDef> MAP = new HashMap<>();

    private EntityRenderDefs() {
    }

    /** 按 id 取渲染定义；缺失返回空（调用方应把「缺失」当作「无渲染」而非报错）。 */
    public static Optional<EntityRenderDef> get(@Nullable Identifier id) {
        return id == null ? Optional.empty() : Optional.ofNullable(MAP.get(id));
    }

    public static Map<Identifier, EntityRenderDef> all() {
        return Map.copyOf(MAP);
    }

    /**
     * 客户端兜底加载：仅在服务端重载尚未填充时（专用服务器端的客户端）从客户端资源包加载。
     *
     * <p>同 {@code TalentTreeModelLoader#ensureClientLoaded} 范式：集成服务端（单人游戏）的
     * {@code AddServerReloadListenersEvent} 已把整表写入本共享静态 {@link #MAP}；而客户端自身的
     * 资源包通常并不包含该数据包，故此处绝不可用客户端的空加载去覆盖服务端已填充的数据，
     * 因此仅在 {@link #MAP} 为空时才回退加载。
     */
    public static void ensureClientLoaded() {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        if (MAP.isEmpty()) {
            load(Minecraft.getInstance().getResourceManager());
        }
    }

    /**
     * 由资源管理器整体重载（服务端资源包 / datapack 重载时调用）。
     *
     * @return 成功载入的条目数
     */
    public static int load(ResourceManager manager) {
        MAP.clear();
        Map<Identifier, Resource> resources = manager.listResources(
                ROOT, id -> id.getPath().endsWith(".json"));
        int loaded = 0;
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            String path = entry.getKey().getPath();
            try (InputStream stream = entry.getValue().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream));
                DataResult<EntityRenderDef> result =
                        EntityRenderDef.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json);
                result.result().ifPresent(def -> {
                    String relative = path.substring(path.indexOf(ROOT) + ROOT.length() + 1);
                    String name = relative.substring(0, relative.lastIndexOf(".json"));
                    MAP.put(Identifier.fromNamespaceAndPath(entry.getKey().getNamespace(), name), def);
                });
                result.error().ifPresent(error -> CraftOfExile.LOGGER.error(
                        "Error loading entity render {}: {}", path, error.message()));
                if (result.result().isPresent()) loaded++;
            } catch (IOException e) {
                CraftOfExile.LOGGER.error("Failed to read entity render resource: {}", path, e);
            }
        }
        return loaded;
    }
}
