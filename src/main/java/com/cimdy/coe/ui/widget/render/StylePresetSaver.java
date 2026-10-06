package com.cimdy.coe.ui.widget.render;

import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 回写当前激活预设 id 到 {@code config/coe/styles.active.json}，使下次启动恢复上次选择。
 */
final class StylePresetSaver {

    private static final Logger LOGGER = LoggerFactory.getLogger(StylePresetSaver.class);

    /**
     * 回写基目录。生产环境取 FML 启动期的 {@code config/} 目录；单元测试通过
     * {@link #setConfigBaseDirForTest(Path)} 指向临时目录，使运行时重载链路在纯 JUnit 环境下
     * 也能把激活预设 id 写回临时 {@code styles.active.json}，不污染真实配置。预设感知、不引入色值。
     */
    private static Path configBaseDir = null;

    private static Path file() {
        Path base = configBaseDir != null ? configBaseDir : FMLPaths.CONFIGDIR.get();
        return base.resolve("coe").resolve("styles.active.json");
    }

    /** 测试桩：把回写基目录重定向到临时目录（仅测试使用，生产路径不变）。 */
    static void setConfigBaseDirForTest(Path dir) { configBaseDir = dir; }

    private StylePresetSaver() {}

    public static void saveActive(String id) {
        try {
            Path dir = file().getParent();
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Files.writeString(file(), "{\"active\":\"" + id + "\"}");
        } catch (Exception e) {
            LOGGER.error("failed writing styles.active.json", e);
        }
    }
}
