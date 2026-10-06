package com.cimdy.coe.data.codec.talent.node;
import com.cimdy.coe.data.codec.talent.tree.TalentTreeModelLoader;
import com.cimdy.coe.data.gen.world.TalentNodeTypeProvider;
import com.cimdy.coe.CraftOfExile;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 天赋节点类型注册表（数据驱动）。
 *
 * <ul>
 *   <li>内置默认类型（travel / minor / notable / keystone / jewel）仅用于「数据生成」——
 *       由 {@link #builtinDefaults()} 提供给 {@code TalentNodeTypeProvider} 写出默认数据包，
 *       不在运行时注入到缓存；
 *   <li>运行时缓存完全来自数据包：自定义类型从 {@code data/coe/coe/talent/player_talent/type/<id>.json} 加载
 *       （Codec 驱动，位于本模组自定义数据根），可覆盖默认同名类型，亦可为新天赋树定义专属节点类型；
 *   <li>天赋树 JSON 中以字符串 id 引用节点类型（见 {@link #REFERENCE_CODEC}），
 *       未知 id 回退到 minor（仅作解析兜底，不写入缓存），保证容错。
 * </ul>
 *
 * <p>必须在天赋树模型之前加载（{@link TalentTreeModelLoader#load} 已保证此顺序）。
 */
public final class TalentNodeTypeRegistry {
    private static final String ROOT = "coe/talent/player_talent/type";
    private static final Map<String, TalentNodeType> TYPES = new HashMap<>();

    public static final TalentNodeType TRAVEL, MINOR, NOTABLE, KEYSTONE, JEWEL, ASCENDANCY, MASTERY;
    static {
        TRAVEL = def("travel", 20, 0xFF6E7B8B, false, false);
        MINOR = def("minor", 22, 0xFF9AA7B5, false, false);
        NOTABLE = def("notable", 36, 0xFFD4A23C, false, false);
        KEYSTONE = def("keystone", 44, 0xFFB23BD6, true, false);
        // 珠宝插槽：点亮后可插入珠宝（Jewel）获得额外加成，视觉上以醒目青色方框区分于圆形节点。
        JEWEL = def("jewel", 32, 0xFF33E0E0, false, true);
        // 升阶（Ascendancy）：职业进阶子图节点，青铜色显著节点。
        ASCENDANCY = def("ascendancy", 36, 0xFFCD853F, false, false);
        // Mastery：职业掌握节点（点亮后从多选项中择一），靛蓝显著节点。
        MASTERY = def("mastery", 34, 0xFF7B68EE, false, false);
    }

    private static TalentNodeType def(String id, int radius, int color,
                                      boolean keystone, boolean socket) {
        TalentNodeType t = new TalentNodeType(id, Component.translatable("talent.type.coe." + id), radius, color, keystone, socket);
        TYPES.put(id, t);
        return t;
    }

    /**
     * 从资源包加载全部节点类型（在数据 / 数据包重载时调用，需先于天赋树模型加载）。
     * 缓存完全遵守数据包内容：仅装入 {@code data/coe/coe/talent/player_talent/type/*.json} 中的定义，
     * 不再注入任何内置默认类型（默认类型只由生成器写出到数据包，再由本方法读回）。
     */
    public static void load(ResourceManager manager) {
        TYPES.clear();
        Map<Identifier, Resource> resources = manager.listResources(ROOT, rl -> rl.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            try (InputStream stream = entry.getValue().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream));
                DataResult<TalentNodeType> result = TalentNodeType.CODEC.parse(JsonOps.INSTANCE, json);
                result.result().ifPresent(t -> TYPES.put(t.id(), t));
                result.error().ifPresent(e ->
                        CraftOfExile.LOGGER.error("加载天赋节点类型失败: " + entry.getKey().getPath() + " - " + e.message()));
            } catch (IOException e) {
                CraftOfExile.LOGGER.error("Failed to read data resource stream", e);
            }
        }
    }

    public static TalentNodeType get(String id) {
        return TYPES.getOrDefault(id, MINOR);
    }

    /** 内置默认类型列表（供数据生成器写出，避免与上方常量重复定义）。 */
    public static java.util.List<TalentNodeType> builtinDefaults() {
        return java.util.List.of(TRAVEL, MINOR, NOTABLE, KEYSTONE, JEWEL, ASCENDANCY, MASTERY);
    }

    /** 解码用：按 id 解析节点类型，未知 id 回退到 minor。 */
    public static TalentNodeType require(String id) {
        return TYPES.getOrDefault(id, MINOR);
    }

    /** 引用编解码：天赋树 JSON 中以字符串 id 引用节点类型。 */
    public static final Codec<TalentNodeType> REFERENCE_CODEC =
            Codec.STRING.xmap(TalentNodeTypeRegistry::require, TalentNodeType::id);
}
