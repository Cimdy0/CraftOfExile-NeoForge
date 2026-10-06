package com.cimdy.coe.data.codec.item.essence;

import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.tiered.BonusFamily;
import com.cimdy.coe.data.codec.item.tiered.Tier;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 精髓注册表（单例）：条目 id（{@code coe:<EssenceState.id>}）→ {@link EssenceState}。
 *
 * <p><b>一个 JSON 文件 = 一族</b>（{@code data/coe/coe/item/essence/*.json}）：文件名即族名
 * （{@code body_essence.json → Body}），文件内 {@code tiers} 声明该族支持的阶、{@code branches} 给出各阶数值，
 * 加载时<b>按阶展开</b>为多条 {@link EssenceState}。精髓第四阶若为全新词缀，则在 {@code branches} 中
 * 另起一条 {@code type} 只覆盖 perfect 阶（如 {@code ADD_MAX_HEALTH → MAX_HEALTH_INC}）。</p>
 *
 * <p>双来源加载（二者写同一张表，datapack 覆盖 bundled 默认值）：{@link #init()} 枚举 classpath 族文件，
 * {@link #load(ResourceManager)} 为服务端资源重载（整表替换）。</p>
 *
 * <p>精髓的<b>族不在这里判定</b>：{@link EssenceState} 的名称（{@code Identifier}）只作条目身份，
 * 互斥一律由词缀身份（{@code AdditionType} 自带的 Identifier）判定 —— 与 {@code CommonMod} 同路径。</p>
 */
public final class EssenceRegistry {

    private EssenceRegistry() {
    }

    /** 条目 id（{@code coe:<EssenceState.id>}）→ 精髓条目。 */
    private static final Map<Identifier, EssenceState> ESSENCES = new HashMap<>();

    /** 数据资源路径（bundled 与 datapack 同根）：{@code data/coe/coe/item/essence}。 */
    public static final Identifier ESSENCE_PATH = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "coe/item/essence");

    /** 族后缀：文件名去掉它即为精髓名（{@code abrasion_essence.json → Abrasion}）。 */
    private static final String FAMILY_SUFFIX = "_essence";

    private static Identifier keyOf(String id) {
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, id);
    }

    /** mod 初始化期：枚举自带（classpath）的族文件，展开为全部精髓条目。 */
    public static void init() {
        for (FamilyFile file : listBundledFamilies()) {
            loadFamily(file.fileName(), file.json());
        }
    }

    /** 解析一个族文件并展开为其全部阶的条目（文件名即族名）。 */
    private static void loadFamily(String fileName, JsonElement json) {
        String path = BonusFamily.familyOfFileName(fileName, FAMILY_SUFFIX);
        Identifier name = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, path);
        com.mojang.serialization.DataResult<EssenceFamily> parsed = EssenceFamily.CODEC.parse(JsonOps.INSTANCE, json);
        parsed.result().ifPresent(def -> {
            for (Tier tier : def.bonuses().tiers()) {
                EssenceState state = new EssenceState(name, tier, def.bonuses().resolve(tier));
                ESSENCES.put(keyOf(state.id()), state);
            }
        });
        parsed.error().ifPresent(error ->
                CraftOfExile.LOGGER.error("EssenceRegistry: failed to parse '{}' - {}", fileName, error.message()));
    }

    /** 族文件（文件名 + 内容）：文件名承载族名，故枚举时必须带上。 */
    private record FamilyFile(String fileName, JsonElement json) {
    }

    /** 枚举 classpath 下 {@code data/coe/coe/item/essence/*.json}（jar / 目录双兼容）。 */
    private static List<FamilyFile> listBundledFamilies() {
        List<FamilyFile> result = new ArrayList<>();
        String dir = ESSENCE_PATH.getNamespace() + "/" + ESSENCE_PATH.getPath(); // "coe/item/essence"
        try {
            Enumeration<URL> urls = EssenceRegistry.class.getClassLoader().getResources(dir);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if ("jar".equals(url.getProtocol())) {
                    String jarPath = url.getPath();
                    int bang = jarPath.indexOf('!');
                    if (bang >= 0) {
                        jarPath = jarPath.substring(0, bang);
                    }
                    try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry e = entries.nextElement();
                            String name = e.getName();
                            if (name.startsWith(dir + "/") && name.endsWith(".json")) {
                                try (InputStream s = jar.getInputStream(e)) {
                                    result.add(new FamilyFile(fileNameOf(name),
                                            JsonParser.parseReader(new InputStreamReader(s, StandardCharsets.UTF_8))));
                                }
                            }
                        }
                    }
                } else if ("file".equals(url.getProtocol())) {
                    Path path = Paths.get(url.toURI());
                    if (Files.isDirectory(path)) {
                        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.json")) {
                            for (Path p : stream) {
                                try (InputStream s = Files.newInputStream(p)) {
                                    result.add(new FamilyFile(p.getFileName().toString(),
                                            JsonParser.parseReader(new InputStreamReader(s, StandardCharsets.UTF_8))));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            CraftOfExile.LOGGER.error("EssenceRegistry.init: failed to enumerate bundled essence families", e);
        }
        return result;
    }

    /** 服务端资源重载：扫描 datapack 族文件，整表替换（datapack 覆盖 bundled）。 */
    public static void load(ResourceManager manager) {
        ESSENCES.clear();
        Map<Identifier, Resource> files = manager.listResources(
                ESSENCE_PATH.getPath(), id -> id.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : files.entrySet()) {
            try (InputStream stream = entry.getValue().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                loadFamily(fileNameOf(entry.getKey().getPath()), json);
            } catch (IOException e) {
                CraftOfExile.LOGGER.error("EssenceRegistry.load: failed to read " + entry.getKey().getPath(), e);
            }
        }
    }

    /** 取路径末段文件名（{@code item/essence/body_essence.json → body_essence.json}）。 */
    private static String fileNameOf(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    /** 按精髓条目 id（如 {@code perfect_body_essence}）取定义。 */
    public static EssenceState getById(String id) {
        return ESSENCES.get(keyOf(id));
    }

    /** 按完整标识符取定义。 */
    public static EssenceState get(Identifier id) {
        return ESSENCES.get(id);
    }

    public static Collection<EssenceState> all() {
        return ESSENCES.values();
    }

    public static int size() {
        return ESSENCES.size();
    }
}
