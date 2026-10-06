package com.cimdy.coe.data.codec.item;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.state.RuneState;
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
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 符文注册表（单例）：条目 id（{@code coe:<RuneState.id>}）→ {@link RuneState}。
 *
 * <p><b>一个 JSON 文件 = 一族</b>（{@code data/coe/coe/item/rune/*.json}）：文件名即族名
 * （{@code Desert_Rune.json → Desert}），文件内 {@code tiers} 声明该族支持的阶、
 * {@code branches} 给出各阶数值；加载时<b>按阶展开</b>为多条 {@link RuneState}（一族 × 一阶）。
 * id / 需求等级 / 名称 / 模型路径全部由「族名 + 阶」派生，不落 JSON。</p>
 *
 * <p>双来源加载（二者写同一张表，datapack 覆盖 bundled 默认值）：</p>
 * <ul>
 *   <li>{@link #init()} —— mod 初始化期经 ClassLoader 枚举自带（classpath）的族文件，
 *     供物品注册阶段枚举全部符文条目；</li>
 *   <li>{@link #load(ResourceManager)} —— 服务端资源重载监听器，读取 datapack 族文件（整表替换）。</li>
 * </ul>
 */
public final class RuneRegistry {

    private RuneRegistry() {
    }

    /** 条目 id（{@code coe:<RuneState.id>}）→ 符文条目。 */
    private static final Map<Identifier, RuneState> RUNES = new HashMap<>();

    /** 数据资源路径（bundled 与 datapack 同根）：{@code data/coe/coe/item/rune}。 */
    public static final Identifier RUNE_PATH = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "coe/item/rune");

    /** 族后缀：文件名去掉它即为符文名（{@code adept_rune.json → adept}）。 */
    private static final String FAMILY_SUFFIX = "_rune";

    private static Identifier keyOf(String id) {
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, id);
    }

    /** mod 初始化期：枚举自带（classpath）的族文件，展开为全部符文条目。 */
    public static void init() {
        for (FamilyFile file : listBundledFamilies()) {
            loadFamily(file.fileName(), file.json());
        }
    }

    /**
     * 解析一个族文件并展开为其全部阶的条目（文件名即族名）。
     * 无加成族（Charging 占位 / Masterwork 升级符文）同样按 {@code tiers} 展开，保证条目齐全。
     */
    private static void loadFamily(String fileName, JsonElement json) {
        String path = fileName.endsWith(".json")
                ? fileName.substring(0, fileName.length() - ".json".length())
                : fileName;
        Identifier name = Identifier.fromNamespaceAndPath(
                CraftOfExile.MODID, path.substring(0, Math.max(0, path.length() - FAMILY_SUFFIX.length())).toLowerCase(Locale.ROOT));
        com.mojang.serialization.DataResult<BonusFamily> parsed = BonusFamily.CODEC.parse(JsonOps.INSTANCE, json);
        parsed.result().ifPresent(fam -> {
            for (Tier tier : fam.tiers()) {
                RuneState state = new RuneState(name, tier, fam.resolve(tier));
                RUNES.put(keyOf(state.id()), state);
            }
        });
        parsed.error().ifPresent(error ->
                CraftOfExile.LOGGER.error("RuneRegistry: failed to parse '{}' - {}", fileName, error.message()));
    }

    /** 族文件（文件名 + 内容）：文件名承载族名，故枚举时必须带上。 */
    private record FamilyFile(String fileName, JsonElement json) {
    }

    /** 枚举 classpath 下 {@code data/coe/coe/item/rune/*.json}（jar / 目录双兼容）。 */
    private static List<FamilyFile> listBundledFamilies() {
        List<FamilyFile> result = new ArrayList<>();
        String dir = RUNE_PATH.getNamespace() + "/" + RUNE_PATH.getPath(); // "coe/item/rune"
        try {
            Enumeration<URL> urls = RuneRegistry.class.getClassLoader().getResources(dir);
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
            CraftOfExile.LOGGER.error("RuneRegistry.init: failed to enumerate bundled rune families", e);
        }
        return result;
    }

    /** 服务端资源重载：扫描 datapack 族文件，整表替换（datapack 覆盖 bundled）。 */
    public static void load(ResourceManager manager) {
        RUNES.clear();
        Map<Identifier, Resource> files = manager.listResources(
                RUNE_PATH.getPath(), id -> id.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : files.entrySet()) {
            try (InputStream stream = entry.getValue().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                loadFamily(fileNameOf(entry.getKey().getPath()), json);
            } catch (IOException e) {
                CraftOfExile.LOGGER.error("RuneRegistry.load: failed to read " + entry.getKey().getPath(), e);
            }
        }
    }

    /** 取路径末段文件名（{@code item/rune/Desert_Rune.json → Desert_Rune.json}）。 */
    private static String fileNameOf(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    /** 按符文条目 id（如 {@code Lesser_Adept_Rune}）取定义。 */
    public static RuneState getById(String id) {
        return RUNES.get(keyOf(id));
    }

    /** 按完整标识符取定义。 */
    public static RuneState get(Identifier id) {
        return RUNES.get(id);
    }

    public static Collection<RuneState> all() {
        return RUNES.values();
    }

    public static int size() {
        return RUNES.size();
    }
}
