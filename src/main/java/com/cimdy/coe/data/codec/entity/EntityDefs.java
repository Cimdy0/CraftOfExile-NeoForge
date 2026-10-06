package com.cimdy.coe.data.codec.entity;

import com.cimdy.coe.data.codec.entity.render.EntityRenderDefs;
import com.cimdy.coe.CraftOfExile;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 实体定义的运行时注册表 —— {@code data/<ns>/coe/entity/*.json} 全部条目。
 *
 * <p>这是「技能层找到实体」的入口：技能不内联定义实体，只通过 {@link EntityRead}
 * 按 id 引用本表的条目并覆写 render / name。同 {@link EntityRenderDefs} 范式：
 * datagen 生成、资源重载时整体读入、运行期只读。
 */
public final class EntityDefs {

    /** 数据根路径（位于 {@code data/<ns>/} 之下）。 */
    public static final String ROOT = "coe/entity";
    /** 资源重载监听器键（同 {@link EntityRenderDefs#KEY}）。 */
    public static final Identifier KEY =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "entity");

    private static final Map<Identifier, EntityDef> MAP = new HashMap<>();

    private EntityDefs() {
    }

    /** 按 id 取形态定义；缺失返回空（调用方应把「缺失」当作「该形态不可用」，跳过生成而非崩溃）。 */
    public static Optional<EntityDef> get(Identifier id) {
        return id == null ? Optional.empty() : Optional.ofNullable(MAP.get(id));
    }

    public static Map<Identifier, EntityDef> all() {
        return Map.copyOf(MAP);
    }

    /**
     * 按 {@link EntityDef#type()} 实例化承载实体 —— 投递生成实体的<b>唯一入口</b>。
     *
     * <p>投递因此不依赖任何具体实体类：换一个 {@code type} 即换一种载体
     * （驻场道具 / 投射物 / 投掷物 …），只要该实体实现 {@code EntityDefCarrier}
     * 就能被 {@link EntityDef} 装配。
     *
     * @return 实体实例；类型 id 未注册时返回 null（调用方应记日志并跳过，而非崩溃）
     */
    @Nullable
    public static Entity create(Level level, EntityDef def) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(def.type()).map(Holder.Reference::value).orElse(null);
        return type == null ? null : type.create(level, EntitySpawnReason.TRIGGERED);
    }

    /** 由资源管理器整体重载，返回成功载入条目数。 */
    public static int load(ResourceManager manager) {
        MAP.clear();
        Map<Identifier, Resource> resources = manager.listResources(
                ROOT, id -> id.getPath().endsWith(".json"));
        int loaded = 0;
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            String path = entry.getKey().getPath();
            try (InputStream stream = entry.getValue().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream));
                DataResult<EntityDef> result = EntityDef.CODEC.parse(JsonOps.INSTANCE, json);
                result.result().ifPresent(def -> {
                    String relative = path.substring(path.indexOf(ROOT) + ROOT.length() + 1);
                    String name = relative.substring(0, relative.lastIndexOf(".json"));
                    MAP.put(Identifier.fromNamespaceAndPath(entry.getKey().getNamespace(), name), def);
                });
                result.error().ifPresent(error -> CraftOfExile.LOGGER.error(
                        "Error loading entity {}: {}", path, error.message()));
                if (result.result().isPresent()) loaded++;
            } catch (IOException e) {
                CraftOfExile.LOGGER.error("Failed to read entity resource: {}", path, e);
            }
        }
        return loaded;
    }
}
