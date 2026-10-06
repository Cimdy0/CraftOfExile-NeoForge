package com.cimdy.coe.data.codec.support.stat.tag;

import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 词缀领域标签注册表（D6 / D7）：词缀的领域标签 =
 * Σ(其全部引用效果的 {@code domainTags()})（由 {@link com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect#domainTags()}
 * 声明，在效果构造处一次性给定，所有引用该效果的词缀自动继承）
 * ∪ 数据包 {@code coe/stat_tag/*.json} 逃生舱（D7-b，供内容作者补标签其效果未隐含的维度）。
 *
 * <p><b>设计要点（team-lead 2026-09-19 裁定，D6 终态）：</b>标签的<b>唯一权威来源是 StatEffect</b>——
 * 不再维护「逐 stat 手工登记」的精选表（旧 {@code seedStatic} 已删除），从而新增一个 fire 类效果
 * 只需在其构造处声明 {@code domainTags}，无需在别处逐词缀簿记；其余 ~1700 个 stat 经其效果自动继承，
 * 零手工登记。基础 Stat 流（StatRead→StatEntry）保持神圣，本注册表只读派生，不存储 stat 值。
 */
public final class GateTagRegistry {

    /** 数据包目录：{@code data/<namespace>/stat_tag/*.json}（D7-b 逃生舱）。 */
    private static final Identifier PATH = Identifier.parse("coe/stat_tag");

    /** 单文件格式：stat id → 标签列表。 */
    private static final Codec<Map<Identifier, List<String>>> FILE_CODEC =
            Codec.unboundedMap(Identifier.CODEC, Codec.STRING.listOf());

    /** 数据包驱动层（D7-b）：重载时整体替换。 */
    private static volatile Map<Identifier, Set<String>> DATA = Map.of();

    private GateTagRegistry() {}

    // ───────────────────────── 查询 ─────────────────────────

    /** 单 stat 的数据包逃生舱标签（D7-b）。 */
    public static Set<String> lookup(Identifier id) {
        Set<String> d = DATA.get(id);
        return d == null ? Set.of() : d;
    }

    /**
     * 词缀的领域标签 = Σ(其全部引用效果的 {@code domainTags()}) ∪ 数据包逃生舱（D6）。
     * 内联效果（无 ref）同样贡献其 {@code domainTags()}，覆盖比旧「仅 StatRead」实现更完整。
     */
    public static Set<String> lookup(CommonMod mod) {
        Set<String> tags = new HashSet<>();
        for (Addition a : mod.additions()) {
            AdditionType t = a.type();
            for (AdditionType.Effect e : t.effects()) {
                tags.addAll(e.effect().domainTags());
                if (e.stat() instanceof StatRead sr) {
                    tags.addAll(lookup(sr.ref()));
                }
            }
        }
        return tags;
    }

    // ───────────────────────── 数据驱动加载（D7-b，逃生舱） ─────────────────────────

    /**
     * 从数据包加载 {@code coe/stat_tag/*.json}，整体替换 DATA 层（叠加 / 扩展到 StatEffect 声明的领域）。
     * 文件缺失或解析失败均安全回落为「空 DATA」——不影响 StatEffect 已声明的领域标签。
     */
    public static void loadFrom(ResourceManager manager) {
        Map<Identifier, Set<String>> merged = new HashMap<>();
        try {
            Map<Identifier, Resource> resources = manager.listResources(
                    PATH.getPath(), id -> id.getPath().endsWith(".json"));
            for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
                try (InputStream stream = entry.getValue().open()) {
                    JsonElement json = JsonParser.parseReader(new InputStreamReader(stream));
                    DataResult<Map<Identifier, List<String>>> result = FILE_CODEC.parse(JsonOps.INSTANCE, json);
                    result.result().ifPresent(file -> file.forEach((k, v) ->
                            merged.computeIfAbsent(k, x -> new HashSet<>()).addAll(v)));
                    result.error().ifPresent(err -> System.err.println(
                            "Error loading stat_tag: " + entry.getKey() + " " + err.message()));
                } catch (Exception e) {
                    System.err.println("Failed to read stat_tag resource: " + entry.getKey());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to list stat_tag resources");
            e.printStackTrace();
        }
        DATA = Map.copyOf(merged);
    }
}
