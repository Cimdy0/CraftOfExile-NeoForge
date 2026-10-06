package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.attach.Sfx;

import java.util.ArrayList;
import java.util.List;

/**
 * 已加载风格预设的有序注册表。
 *
 * <p>同时容纳内置预设（{@link StylePreset#dark()} 等，配置为空时直接用其外观）与
 * 配置驱动预设（{@link ConfigStylePreset}）。任意位置切换预设都会通过 {@link Theme#apply} 立即生效。
 */
final class StylePresetRegistry {

    private static final List<StylePreset> PRESETS = new ArrayList<>();
    private static int active = 0;

    private StylePresetRegistry() {}

    public static void set(List<StylePreset> list) {
        PRESETS.clear();
        PRESETS.addAll(list);
        if (active >= PRESETS.size()) active = 0;
    }

    public static List<StylePreset> all() {
        return List.copyOf(PRESETS);
    }

    public static StylePreset active() {
        return PRESETS.isEmpty() ? StylePreset.dark() : PRESETS.get(active);
    }

    static void setActive(int idx) {
        if (idx < 0 || idx >= PRESETS.size()) return;
        StylePreset preset = PRESETS.get(idx);
        // D.3 桥接收尾：把当前皮肤的音效 pitch（Paper 0.95 / Midnight 1.1，其余 1.0）注入音效层。
        // 唯一收敛点——启动加载（StylePresetLoader）/ reload / 选择器点选（setPresetById）/
        // 全局循环键（cycle）全部经此 setActive → Theme.apply，故只在此注入一次即全量生效。
        // Sfx.skinPitch 内部 clamp 0.5~2.0，启动期调用亦安全（仅写静态浮点，不触发播音）。
        Sfx.skinPitch(preset.sfxPitch());
        Theme.apply(preset);
    }

    static void setActiveById(String id) {
        for (int i = 0; i < PRESETS.size(); i++) {
            if (PRESETS.get(i).id().equals(id)) {
                setActive(i);
                return;
            }
        }
    }

    static StylePreset cycle() {
        if (PRESETS.isEmpty()) return StylePreset.dark();
        setActive((active + 1) % PRESETS.size());
        return PRESETS.get(active);
    }

    /**
     * 由 {@link Theme#apply} 调用的单一回写点（F1）：按 id 在 PRESETS 中定位索引、设 {@code active}
     * 并持久化激活项。不得回调 setActive/apply，避免无限递归。包级私有。
     */
    static void syncActiveById(String id) {
        for (int i = 0; i < PRESETS.size(); i++) {
            if (PRESETS.get(i).id().equals(id)) {
                active = i;
                StylePresetSaver.saveActive(PRESETS.get(active).id());
                return;
            }
        }
    }
}
