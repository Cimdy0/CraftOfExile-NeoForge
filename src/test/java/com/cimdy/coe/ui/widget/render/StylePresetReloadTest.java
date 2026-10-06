package com.cimdy.coe.ui.widget.render;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 运行时风格重载链路集成测试（纯 JUnit，无 FML 启动、无真游戏客户端）。
 *
 * <p>覆盖 {@code StylePresets.reload()} → {@code StylePresetLoader.reload()} 的真实链路：
 * 构造临时 {@code styles.json}（seed）→ 改动后重载 → 断言 {@link StylePresetRegistry} 反映新的激活预设。
 * 断言只用公开读取路径 {@link StylePresetRegistry#active()} / {@link StylePresetRegistry#all()}。
 *
 * <p><b>脆弱点与测试桩</b>：{@code StylePresetLoader} 与 {@code StylePresetSaver} 原本把文件路径
 * 写死为 {@code FMLPaths.CONFIGDIR.get().resolve("coe")}。{@code CONFIGDIR} 是 FML 启动期才填充的
 * 记忆化路径持有者（{@code Lazy<Path>}，由 {@code FMLConfig} 计算），纯 JUnit 环境下为 {@code null}，
 * 直接调用 {@code reload()} 会在类初始化阶段 NPE。本测试通过生产代码里预设感知、不引入色值的测试桩
 * {@code setConfigBaseDirForTest(Path)} 把基目录重定向到 {@link TempDir}，从而在无 FML 启动下跑通整条链路。
 * 生产默认路径行为不变（基目录为 null 时仍走 {@code FMLPaths.CONFIGDIR}）。
 */
class StylePresetReloadTest {

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        // 重定向加载器与回写器到临时目录（生产路径不变），使 reload() 不再依赖 FMLPaths.CONFIGDIR。
        StylePresetLoader.setConfigBaseDirForTest(tempDir);
        StylePresetSaver.setConfigBaseDirForTest(tempDir);
        // 重新播种一份干净注册表并归一化 active=dark，保证用例间隔离（注册表/Theme 为静态全局状态）。
        StylePresets.reload();
        StylePresetRegistry.setActiveById("dark");
    }

    @Test
    void seedThenReloadProducesFivePresetsWithDarkActive() {
        assertEquals(5, StylePresetRegistry.all().size(), "seed 应写出 5 套内置预设");
        assertEquals("dark", StylePresetRegistry.active().id());
        assertTrue(Files.exists(tempDir.resolve("coe").resolve("styles.json")),
                "reload 应把种子写入临时 styles.json");
    }

    @Test
    void editStylesJsonThenReloadReflectsNewActivePreset() {
        // 激活 paper，再改动磁盘上的 styles.json 给 paper 注入一个令牌。
        StylePresetRegistry.setActiveById("paper");
        assertEquals("paper", StylePresetRegistry.active().id());

        String edited = "["
                + "{\"id\":\"dark\",\"displayName\":\"Dark\"},"
                + "{\"id\":\"high_contrast\",\"displayName\":\"High Contrast\"},"
                + "{\"id\":\"compact\",\"displayName\":\"Compact\"},"
                + "{\"id\":\"paper\",\"displayName\":\"Paper\",\"global\":{\"accent\":\"#FF123456\"}},"
                + "{\"id\":\"midnight\",\"displayName\":\"Midnight\"}"
                + "]";
        writeStylesJson(edited);

        StylePresets.reload();

        // 重载后应保持同一激活预设（按 id 跟踪，而非索引）。
        assertEquals("paper", StylePresetRegistry.active().id());
        // 应反映编辑后的令牌：paper 从空令牌升级为 ConfigStylePreset，accent 取配置值。
        assertTrue(StylePresetRegistry.active() instanceof ConfigStylePreset,
                "编辑后的 paper 应成为 ConfigStylePreset");
        assertEquals(0xFF123456, StylePresetRegistry.active().accent(),
                "重载应重新解析 styles.json 并把注入的 accent 令牌应用到激活预设");
        assertEquals(5, StylePresetRegistry.all().size(), "预设数量不应因重载而增减");
        // 热刷新广播路径（Theme.apply）应在无客户端环境下执行，使 Theme.current() 同步。
        assertEquals("paper", Theme.current().id());
    }

    @Test
    void reloadTracksActiveByIdNotIndexWhenListReordered() {
        StylePresetRegistry.setActiveById("midnight");
        String reordered = "["
                + "{\"id\":\"midnight\",\"displayName\":\"Midnight\"},"
                + "{\"id\":\"dark\",\"displayName\":\"Dark\"},"
                + "{\"id\":\"high_contrast\",\"displayName\":\"High Contrast\"},"
                + "{\"id\":\"compact\",\"displayName\":\"Compact\"},"
                + "{\"id\":\"paper\",\"displayName\":\"Paper\"}"
                + "]";
        writeStylesJson(reordered);
        StylePresets.reload();
        assertEquals("midnight", StylePresetRegistry.active().id(),
                "激活预设应按 id 跟踪，列表重排后仍指向 midnight");
    }

    @Test
    void reloadFallsBackToFirstPresetWhenActiveIdMissing() {
        StylePresetRegistry.setActiveById("midnight");
        // 删除 midnight，使激活 id 在重载后缺失 → 回退到首个预设。
        String withoutMidnight = "["
                + "{\"id\":\"dark\",\"displayName\":\"Dark\"},"
                + "{\"id\":\"high_contrast\",\"displayName\":\"High Contrast\"},"
                + "{\"id\":\"compact\",\"displayName\":\"Compact\"},"
                + "{\"id\":\"paper\",\"displayName\":\"Paper\"},"
                + "{\"id\":\"midnight_extra\",\"displayName\":\"Midnight Extra\"}"
                + "]";
        writeStylesJson(withoutMidnight);
        StylePresets.reload();
        assertEquals("dark", StylePresetRegistry.active().id(),
                "激活 id 缺失时应回退到首个预设（dark）");
    }

    @Test
    void reloadAbortsAndKeepsPresetsOnParseFailure() {
        StylePresetRegistry.setActiveById("compact");
        writeStylesJson("{ this is not valid json ");
        StylePresets.reload();
        assertEquals(5, StylePresetRegistry.all().size(), "解析失败应保留当前 5 套预设");
        assertEquals("compact", StylePresetRegistry.active().id(), "解析失败应保留当前激活预设");
    }

    private void writeStylesJson(String json) throws Exception {
        Path f = tempDir.resolve("coe").resolve("styles.json");
        Files.createDirectories(f.getParent());
        Files.writeString(f, json);
    }
}
