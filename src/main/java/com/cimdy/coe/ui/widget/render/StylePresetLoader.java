package com.cimdy.coe.ui.widget.render;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 客户端启动时从 {@code config/coe/styles.json} 加载风格预设。
 *
 * <p>文件不存在时写入 5 套内置预设作为种子（空令牌 = 沿用内置外观），用户随后可自由增删改。
 * 配置为空（无令牌）的预设直接复用对应的内置 {@link StylePreset}，因此切换即见差异；
 * 一旦用户填入令牌则升级为 {@link ConfigStylePreset} 走级联解析。
 */
final class StylePresetLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(StylePresetLoader.class);
    /**
     * 配置基目录。生产环境取 FML 启动期的 {@code config/} 目录；单元测试通过
     * {@link #setConfigBaseDirForTest(Path)} 指向临时目录，从而在无 FML 启动的纯 JUnit 环境下
     * 也能跑通运行时重载链路（构造临时 styles.json → 改动 → 重载 → 断言注册表）。
     * 预设感知、不引入任何色值；默认路径行为与改造前完全一致。
     */
    private static Path configBaseDir = null;

    private static Path dir() {
        Path base = configBaseDir != null ? configBaseDir : FMLPaths.CONFIGDIR.get();
        return base.resolve("coe");
    }

    private static Path file() { return dir().resolve("styles.json"); }

    private static Path activeFile() { return dir().resolve("styles.active.json"); }

    /** 测试桩：把加载/写入基目录重定向到临时目录（仅测试使用，生产路径不变）。 */
    static void setConfigBaseDirForTest(Path dir) { configBaseDir = dir; }

    private StylePresetLoader() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(StylePresetLoader::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        load();
    }

    public static void load() {
        List<StylePresetDef> defs;
        if (!Files.exists(file())) {
            defs = seed();
            writeDefs(defs);
        } else {
            defs = readDefs();
            if (defs == null) defs = seed();
        }
        List<StylePreset> presets = new ArrayList<>();
        for (StylePresetDef d : defs) presets.add(instantiate(d));
        StylePresetRegistry.set(presets);
        String activeId = readActive();
        if (activeId != null) StylePresetRegistry.setActiveById(activeId);
        else StylePresetRegistry.setActive(0);
        LOGGER.info("Loaded {} style presets (active: {})", presets.size(), StylePresetRegistry.active().id());
    }

    /**
     * 运行时重新加载 {@code config/coe/styles.json}，使编辑后的风格配置立即生效，无需重启游戏。
     *
     * <p>与 {@link #load()} 的关键区别：
     * <ul>
     *   <li>{@code styles.json} 不存在时仍按启动语义补种 5 套预设（保持首次体验一致）；</li>
     *   <li>替换注册表前先记住<b>当前生效</b>的预设 id（取自内存中的 {@link StylePresetRegistry}，
     *       而非磁盘上的 {@code styles.active.json}），换表后尽量沿用同一预设，缺失则回退到首个预设；</li>
     *   <li>重新应用生效预设会触发 {@link Theme#apply} → 已注册的变更监听器，从而热刷新当前打开的界面。</li>
     * </ul>
     *
     * <p>若文件存在但解析失败，仅记错误日志并保留当前注册表（不覆盖用户当前选择），避免运行时配置损坏导致界面丢失。
     */
    public static void reload() {
        List<StylePresetDef> defs;
        if (!Files.exists(file())) {
            defs = seed();
            writeDefs(defs);
        } else {
            defs = readDefs();
            if (defs == null) {
                LOGGER.error("styles.json reload aborted: parse failed; keeping current presets");
                return;
            }
        }
        List<StylePreset> presets = new ArrayList<>();
        for (StylePresetDef d : defs) presets.add(instantiate(d));

        // 记住当前生效预设 id，换表后尽量沿用
        String saved = StylePresetRegistry.active().id();
        StylePresetRegistry.set(presets);
        StylePresetRegistry.setActiveById(saved);
        if (!StylePresetRegistry.active().id().equals(saved)) {
            StylePresetRegistry.setActive(0);
        }
        LOGGER.info("Reloaded {} style presets (active: {})", presets.size(), StylePresetRegistry.active().id());
    }

    private static StylePreset instantiate(StylePresetDef d) {
        if (d.isEmpty()) return StylePreset.fromId(d.id());
        return new ConfigStylePreset(d);
    }

    private static List<StylePresetDef> seed() {
        List<StylePresetDef> list = new ArrayList<>();
        list.add(new StylePresetDef("dark", "Dark", Map.of(), Map.of(), Map.of()));
        list.add(new StylePresetDef("high_contrast", "High Contrast", Map.of(), Map.of(), Map.of()));
        list.add(new StylePresetDef("compact", "Compact", Map.of(), Map.of(), Map.of()));
        list.add(new StylePresetDef("paper", "Paper", Map.of(), Map.of(), Map.of()));
        list.add(new StylePresetDef("midnight", "Midnight", Map.of(), Map.of(), Map.of()));
        return list;
    }

    private static List<StylePresetDef> readDefs() {
        try {
            String text = Files.readString(file());
            JsonElement el = JsonParser.parseString(text);
            var res = Codec.list(StylePresetDef.CODEC).parse(JsonOps.INSTANCE, el);
            if (res.result().isPresent()) return res.result().get();
            res.error().ifPresent(e -> LOGGER.error("styles.json parse error: {}", e.message()));
            return null;
        } catch (Exception e) {
            LOGGER.error("failed reading styles.json", e);
            return null;
        }
    }

    private static void writeDefs(List<StylePresetDef> defs) {
        try {
            if (!Files.exists(dir())) Files.createDirectories(dir());
            var res = Codec.list(StylePresetDef.CODEC).encodeStart(JsonOps.INSTANCE, defs);
            if (res.result().isPresent()) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Files.writeString(file(), gson.toJson(res.result().get()));
            }
        } catch (Exception e) {
            LOGGER.error("failed writing styles.json", e);
        }
    }

    private static String readActive() {
        try {
            if (!Files.exists(activeFile())) return null;
            String text = Files.readString(activeFile()).trim();
            if (text.isEmpty()) return null;
            JsonElement el = JsonParser.parseString(text);
            var obj = el.getAsJsonObject();
            return obj.has("active") ? obj.get("active").getAsString() : null;
        } catch (Exception e) {
            LOGGER.error("failed reading styles.active.json", e);
            return null;
        }
    }
}
