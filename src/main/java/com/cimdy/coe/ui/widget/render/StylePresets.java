package com.cimdy.coe.ui.widget.render;

import net.neoforged.bus.api.IEventBus;

import java.util.List;

/**
 * 风格预设门面：外部一律从本类访问预设注册表 / 加载器 / 上下文能力，
 * 具体实现类（{@code StylePresetRegistry} / {@code StylePresetLoader} / {@code StylePresetContext}）包私有不可见。
 *
 * <p>与 {@code Renderers} / {@code NodePainters} 同形 —— lib 公开面只暴露能力接口与原语公共用法。
 */
public final class StylePresets {

    private StylePresets() {}

    public static List<StylePreset> all() {
        return StylePresetRegistry.all();
    }

    public static StylePreset active() {
        return StylePresetRegistry.active();
    }

    public static void register(IEventBus modEventBus) {
        StylePresetLoader.register(modEventBus);
    }

    public static void reload() {
        StylePresetLoader.reload();
    }

    public static void setScreen(String id) {
        StylePresetContext.setScreen(id);
    }
}
