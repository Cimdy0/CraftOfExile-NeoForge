package com.cimdy.coe.ui.widget.render;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;
import java.util.Optional;

/**
 * 一套风格预设的配置文件模型（落 {@code config/coe/styles.json}）。
 *
 * <p>设计目标（对齐用户意图）：
 * <ul>
 *   <li><b>不必写满</b>：所有令牌都是 optional，没写的就回退到内置 Dark 默认；</li>
 *   <li><b>可以写满</b>：想精细控制就逐令牌覆盖，做到「局部可控」；</li>
 *   <li><b>大体统一、局部可控</b>：级联顺序为 屏幕覆盖 → 角色 → 全局 → 内置 Dark 默认，
 *       因此「某个组件设了、另一个没设就用那一个」。</li>
 * </ul>
 *
 * <p>令牌名即 {@link StylePreset} 的颜色/几何 getter 名（如 {@code accent}、{@code panelColor}、{@code radiusSm}）。
 * 颜色值支持十进制 ARGB（如 {@code -16699546}）或十六进制字符串（如 {@code "#6BA8D6"}、{@code "0xFF1A1A1A"}、
 * {@code "6BA8D6"，缺 alpha 时视为不透明）。
 */
public record StylePresetDef(
        String id,
        String displayName,
        Map<String, Integer> global,
        Map<String, Map<String, Integer>> roles,
        Map<String, Map<String, Integer>> screens
) {

    /** 颜色编解码：十进制 int 或十六进制字符串，写出时统一为 {@code #RRGGBBAA}。 */
    public static final Codec<Integer> ARGB = Codec.either(
            Codec.INT,
            Codec.STRING.flatXmap(
                    s -> {
                        try {
                            return DataResult.success(parseColor(s));
                        } catch (Exception e) {
                            return DataResult.error(() -> "invalid color: " + s);
                        }
                    },
                    v -> DataResult.success(toHex(v))
            )
    ).xmap(
            e -> e.left().isPresent() ? e.left().get() : e.right().orElse(0),
            v -> Either.right(v)
    );

    public static final Codec<StylePresetDef> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("id").forGetter(StylePresetDef::id),
            Codec.STRING.optionalFieldOf("displayName").forGetter(d -> Optional.ofNullable(d.displayName())),
            Codec.unboundedMap(Codec.STRING, ARGB).optionalFieldOf("global", Map.of()).forGetter(StylePresetDef::global),
            Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, ARGB)).optionalFieldOf("roles", Map.of()).forGetter(StylePresetDef::roles),
            Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, ARGB)).optionalFieldOf("screens", Map.of()).forGetter(StylePresetDef::screens)
    ).apply(i, (id, dn, g, r, s) -> new StylePresetDef(id, dn.orElse(null), g, r, s)));

    /** 级联解析单个令牌：屏幕覆盖 → 角色 → 全局 → 未命中返回 null（调用方回退 Dark 默认）。 */
    public Integer lookup(String name, String role, String screen) {
        if (screen != null) {
            Map<String, Integer> m = screens.get(screen);
            if (m != null && m.containsKey(name)) return m.get(name);
        }
        if (role != null) {
            Map<String, Integer> m = roles.get(role);
            if (m != null && m.containsKey(name)) return m.get(name);
        }
        if (global.containsKey(name)) return global.get(name);
        return null;
    }

    public boolean isEmpty() {
        return global.isEmpty() && roles.isEmpty() && screens.isEmpty();
    }

    public String label() {
        return displayName != null && !displayName.isEmpty() ? displayName : id;
    }

    public static int parseColor(String s) {
        String t = s.trim();
        if (t.startsWith("0x") || t.startsWith("0X")) t = t.substring(2);
        if (t.startsWith("#")) t = t.substring(1);
        if (t.isEmpty()) throw new IllegalArgumentException("empty color");
        long v = Long.parseLong(t, 16);
        if (t.length() == 6) v |= 0xFF000000L;
        if (v < 0 || v > 0xFFFFFFFFL) throw new IllegalArgumentException("color out of range: " + s);
        return (int) v;
    }

    public static String toHex(int v) {
        return String.format("#%08X", v);
    }
}
