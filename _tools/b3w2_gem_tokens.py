# -*- coding: utf-8 -*-
"""
B3 收尾：CutScreen gem 类别强调色令牌化（设计见
E:/McModding/.workbuddy/CoE/.workbuddy/memory/UI_GEM_ACCENT_TOKENS.md）

纪律：
  * 基于**当前磁盘内容**编辑，绝不整段写回旧版本（art-director-1 已在
    StylePreset / ConfigStylePreset 新增 talentFillSafeFloor，必须保留）。
  * 保持各文件原有换行符（本项目这三个文件均为 CRLF）。
  * 每个锚点必须命中且仅命中 1 次，否则直接失败。
  * 幂等：已应用则跳过（避免重跑重复插入）。
  * 末尾统一做一次「落地校验」，无论是否跳过都跑。
"""
import sys

ROOT = "src/main/java/com/cimdy/coe/ui/"
SP = ROOT + "widget/render/StylePreset.java"
CP = ROOT + "widget/render/ConfigStylePreset.java"
CS = ROOT + "screen/gem/CutScreen.java"

MARKER = "gemAccentSkill"


def read(p):
    with open(p, "r", encoding="utf-8", newline="") as f:
        return f.read()


def write(p, s):
    with open(p, "w", encoding="utf-8", newline="") as f:
        f.write(s)


def nl_of(s):
    return "\r\n" if "\r\n" in s else "\n"


def sub_once(text, old, new, label):
    n = text.count(old)
    if n != 1:
        sys.exit("FAIL[%s]: expected exactly 1 occurrence, got %d" % (label, n))
    return text.replace(old, new)


def block(lines, nl):
    return nl.join(lines)


# ---------------------------------------------------------------- StylePreset
sp = read(SP)
if MARKER in sp:
    print("StylePreset.java: gem accents already present -> skip")
else:
    nl = nl_of(sp)

    # --- base（Dark 默认 = 迁移前裸字面量，零回归）---
    A = "    public int gemDesaturate()     { return 0xB08C8C8C; }"
    sp = sub_once(sp, A, A + nl + block([
        "",
        "    /** gem 类别强调色：技能（红橙）。 */",
        "    public int gemAccentSkill()    { return 0xFFE0573F; }",
        "    /** gem 类别强调色：辅助（绿）。与 {@code statusOk} 刻意区分：",
        "     * 那个是「成功/已完成」状态色，这是类别身份色，值也不同，不可复用。 */",
        "    public int gemAccentSupport()  { return 0xFF3CC56E; }",
        "    /** gem 类别强调色：精魂（紫）。 */",
        "    public int gemAccentSpirit()   { return 0xFFA86FE0; }",
        "    /** gem 类别强调色<b>兜底</b>：{@code null} 类别或任何未注册类别（蓝）。",
        "     * GemCategory 是可扩展注册表，新类别注册后未加入映射即落到此兜底色。 */",
        "    public int gemAccentFallback() { return 0xFF4FA3E0; }",
        "    /** 表面明暗方向：{@code true} = 当前预设是<b>浅底</b>（如 Paper）。",
        "     * 派生运算据此决定「提亮还是压暗」—— 无条件提亮会在浅底上洗出浅字（不可读）。",
        "     * 非颜色令牌，故不进 ConfigStylePreset 的 int {@code tok(...)} 体系。 */",
        "    public boolean lightSurface()  { return false; }",
    ], nl), "StylePreset base gem")

    # --- HighContrast：朝白混 f=0.26 ---
    A = "        @Override public int gemDesaturate()     { return 0xB0A8A8A8; }"
    sp = sub_once(sp, A, A + nl + block([
        "        @Override public int gemAccentSkill()    { return 0xFFE88270; }",
        "        @Override public int gemAccentSupport()  { return 0xFF6ED493; }",
        "        @Override public int gemAccentSpirit()   { return 0xFFBE94E8; }",
        "        @Override public int gemAccentFallback() { return 0xFF7CBAE8; }",
    ], nl), "StylePreset HighContrast gem")

    # --- Paper：朝黑压 x0.70 + lightSurface=true ---
    A = "        @Override public int gemDesaturate()     { return 0xB0B0B0B0; }"
    sp = sub_once(sp, A, A + nl + block([
        "        @Override public int gemAccentSkill()    { return 0xFF9C3C2C; }",
        "        @Override public int gemAccentSupport()  { return 0xFF2A894D; }",
        "        @Override public int gemAccentSpirit()   { return 0xFF754D9C; }",
        "        @Override public int gemAccentFallback() { return 0xFF37729C; }",
        "        @Override public boolean lightSurface()  { return true; }",
    ], nl), "StylePreset Paper gem")

    # --- Midnight：朝中灰压缩 x0.66（近似值，见设计文档诚实标注）---
    A = "        @Override public int gemDesaturate()     { return 0xB0556478; }"
    sp = sub_once(sp, A, A + nl + block([
        "        @Override public int gemAccentSkill()    { return 0xFFBF6455; }",
        "        @Override public int gemAccentSupport()  { return 0xFF53AD74; }",
        "        @Override public int gemAccentSpirit()   { return 0xFF9A74BF; }",
        "        @Override public int gemAccentFallback() { return 0xFF5F97BF; }",
    ], nl), "StylePreset Midnight gem")

    # Compact 刻意不覆盖（继承 base）—— 与设计文档一致
    # 守护：art-director-1 的 talentFillSafeFloor 必须仍在（4 处：base/HC/Paper/Midnight）
    assert sp.count("talentFillSafeFloor") >= 4, "talentFillSafeFloor lost in StylePreset!"
    write(SP, sp)
    print("StylePreset.java OK (written)")

# ---------------------------------------------------------- ConfigStylePreset
cp = read(CP)
if MARKER in cp:
    print("ConfigStylePreset.java: gem accents already present -> skip")
else:
    nlc = nl_of(cp)
    A = '    @Override public int gemDesaturate() { return tok("gemDesaturate", super.gemDesaturate()); }'
    cp = sub_once(cp, A, A + nlc + block([
        '    @Override public int gemAccentSkill() { return tok("gemAccentSkill", super.gemAccentSkill()); }',
        '    @Override public int gemAccentSupport() { return tok("gemAccentSupport", super.gemAccentSupport()); }',
        '    @Override public int gemAccentSpirit() { return tok("gemAccentSpirit", super.gemAccentSpirit()); }',
        '    @Override public int gemAccentFallback() { return tok("gemAccentFallback", super.gemAccentFallback()); }',
    ], nlc), "ConfigStylePreset gem")
    # lightSurface() 是 boolean 而 tok() 只支持 int，故不加转发（加了编译不过）。
    # 该行含 3 次子串（声明 / tok("...") / super...），grep -c 数行=1，count() 数子串=3
    assert cp.count("talentFillSafeFloor") >= 3, "talentFillSafeFloor lost in ConfigStylePreset!"
    write(CP, cp)
    print("ConfigStylePreset.java OK (written)")

# ------------------------------------------------------------------- CutScreen
cs = read(CS)
if MARKER in cs:
    print("CutScreen.java: gem accents already present -> skip")
else:
    nlx = nl_of(cs)

    cs = sub_once(cs,
                  "        if (cat == GemCategory.SKILL) return 0xFFE0573F;",
                  "        if (cat == GemCategory.SKILL) return Theme.current().gemAccentSkill();",
                  "CutScreen SKILL")
    cs = sub_once(cs,
                  "        if (cat == GemCategory.SUPPORT) return 0xFF3CC56E;",
                  "        if (cat == GemCategory.SUPPORT) return Theme.current().gemAccentSupport();",
                  "CutScreen SUPPORT")
    cs = sub_once(cs,
                  "        if (cat == GemCategory.SPIRIT) return 0xFFA86FE0;",
                  "        if (cat == GemCategory.SPIRIT) return Theme.current().gemAccentSpirit();",
                  "CutScreen SPIRIT")
    cs = sub_once(cs,
                  "        return 0xFF4FA3E0;",
                  "        return Theme.current().gemAccentFallback();",
                  "CutScreen fallback")

    # 派生：方向随预设（浅底压暗 / 深底提亮）
    OLD_DOC = ("    /** 在强调色基础上压暗，用于浅色背景上的名称文字着色。 */" + nlx
               + "    private static int lighten(int color) {")
    NEW_DOC = block([
        "    /** 强调色的派生变体（hover 边框 / 名称文字着色）：<b>方向随预设</b> ——",
        "     * 浅底预设压暗、深底预设提亮。旧实现无条件提亮，在 Paper 浅底上会洗出浅字（不可读），",
        "     * 故改为按 StylePreset.lightSurface() 取方向。 */",
        "    private static int emphasis(int accent) {",
        "        return Theme.current().lightSurface()",
        "                ? Theme.darken(accent, 0.35F)",
        "                : lighten(accent);",
        "    }",
        "",
        "    /** 在强调色基础上<b>提亮</b>（深色预设上的 hover 边框与名称文字）。",
        "     * 注意是提亮不是压暗；浅底预设请走 {@link #emphasis} 的压暗分支。 */",
        "    private static int lighten(int color) {",
    ], nlx)
    cs = sub_once(cs, OLD_DOC, NEW_DOC, "CutScreen lighten doc + emphasis")

    # 删除私有 blend()：改为 Theme.blend（委托 Draw.blend，F7 去重）
    OLD_BLEND = block([
        "    /** 把强调色按比例混进底色。 */",
        "    private static int blend(int fg, int bg, float ratio) {",
        "        int r = (int) (((fg >> 16) & 0xFF) * ratio + ((bg >> 16) & 0xFF) * (1 - ratio));",
        "        int g = (int) (((fg >> 8) & 0xFF) * ratio + ((bg >> 8) & 0xFF) * (1 - ratio));",
        "        int b = (int) ((fg & 0xFF) * ratio + (bg & 0xFF) * (1 - ratio));",
        "        return 0xFF000000 | (r << 16) | (g << 8) | b;",
        "    }",
        "",
    ], nlx)
    cs = sub_once(cs, OLD_BLEND, "", "CutScreen remove private blend")

    # 调用点：blend 参数顺序必须反转（bg 在前），否则 35% -> 65%
    cs = sub_once(cs,
                  "                .backgroundHover(blend(accent, Theme.current().gemViewBg(), 0.35F))",
                  "                .backgroundHover(Theme.blend(Theme.current().gemViewBg(), accent, 0.35F))",
                  "CutScreen backgroundHover blend")
    cs = sub_once(cs,
                  "                .borderHover(lighten(accent))",
                  "                .borderHover(emphasis(accent))",
                  "CutScreen borderHover")
    cs = sub_once(cs,
                  "        name.with(Renderers.text(gemName.copy().withColor(lighten(accent)))",
                  "        name.with(Renderers.text(gemName.copy().withColor(emphasis(accent)))",
                  "CutScreen name text")
    write(CS, cs)
    print("CutScreen.java OK (written)")

# ------------------------------------------- ConfigStylePreset：boolean 令牌转发
# 背景：StylePresetDef 的级联存储是 Map<String,Integer>、Codec<Integer> ARGB，
#      原生 boolean 需改 StylePresetDef/编解码 —— 那是「批次A」共享设施，不在本次授权内。
# 做法：只在 ConfigStylePreset 内新增一个 private boolean 辅助方法，复用既有 int 级联
#      （配置写 1/0，非 0 视为 true）。不改动既有 int tok 重载，不碰序列化/保存路径。
cp = read(CP)
if 'flag("lightSurface"' in cp:
    print("ConfigStylePreset.java: boolean lightSurface already present -> skip")
else:
    nlf = nl_of(cp)
    A = block([
        "    private int tok(String name, String role, int fallback) {",
        "        Integer v = def.lookup(name, role, StylePresetContext.currentScreen());",
        "        return v != null ? v : fallback;",
        "    }",
    ], nlf)
    ADD = block([
        "",
        "    /** boolean 令牌：复用既有 int 级联（配置里写 1 / 0，非 0 视为 true）。",
        "     * <p>刻意<b>不改动</b>既有 int {@code tok} 重载，也不动 {@code StylePresetDef} / 编解码 / 保存路径 ——",
        "     * 那些属于「批次A 配置驱动预设」的共享设施。原生 boolean 支持需扩 StylePresetDef.CODEC，另案处理。 */",
        "    private boolean flag(String name, boolean fallback) {",
        "        Integer v = def.lookup(name, null, StylePresetContext.currentScreen());",
        "        return v != null ? v != 0 : fallback;",
        "    }",
    ], nlf)
    cp = sub_once(cp, A, A + nlf + ADD, "ConfigStylePreset flag helper")

    B = '    @Override public int gemAccentFallback() { return tok("gemAccentFallback", super.gemAccentFallback()); }'
    cp = sub_once(cp, B, B + nlf + block([
        "    @Override public boolean lightSurface() { return flag(\"lightSurface\", super.lightSurface()); }",
    ], nlf), "ConfigStylePreset lightSurface forwarder")
    write(CP, cp)
    print("ConfigStylePreset.java: boolean lightSurface added")

# ------------------------------------------------------------------- 收尾润色
cs = read(CS)
nlp = nl_of(cs)
changed = False

# 1) accent() javadoc：兜底语义（GemCategory 可扩展，不只是 null）
OLD = "    /** 类别强调色：技能=红橙、辅助=绿、精魂=紫，未知=蓝。 */"
if OLD in cs:
    cs = sub_once(cs, OLD, block([
        "    /** 类别强调色：技能=红橙、辅助=绿、精魂=紫，未注册 / null 类别=蓝兜底。",
        "     *  GemCategory 是可扩展注册表，新类别注册后若未加入映射即落到兜底色。 */",
    ], nlp), "polish accent javadoc")
    changed = True

# 2) 删掉私有 blend() 后残留的双空行
DBL = "    }" + nlp + nlp + nlp + "    /** POE2"
if DBL in cs:
    cs = sub_once(cs, DBL, "    }" + nlp + nlp + "    /** POE2", "polish collapse blank")
    changed = True

if changed:
    write(CS, cs)
    print("CutScreen.java: polished")
else:
    print("CutScreen.java: polish already applied -> skip")

# 3) StylePreset：lightSurface 的 javadoc 需反映「已可经 ConfigStylePreset.flag 覆盖」
sp = read(SP)
nls = nl_of(sp)
OLD_LS = block([
    "    /** 表面明暗方向：{@code true} = 当前预设是<b>浅底</b>（如 Paper）。",
    "     * 派生运算据此决定「提亮还是压暗」—— 无条件提亮会在浅底上洗出浅字（不可读）。",
    "     * 非颜色令牌，故不进 ConfigStylePreset 的 int {@code tok(...)} 体系。 */",
], nls)
if OLD_LS in sp:
    sp = sub_once(sp, OLD_LS, block([
        "    /** 表面明暗方向：{@code true} = 当前预设是<b>浅底</b>（如 Paper）。",
        "     * 派生运算据此决定「提亮还是压暗」—— 无条件提亮会在浅底上洗出浅字（不可读）。",
        "     * 非颜色令牌：ConfigStylePreset 走 boolean {@code flag(...)} 转发（配置写 1 / 0），",
        "     * 而不是颜色的 int {@code tok(...)}。 */",
    ], nls), "polish lightSurface javadoc")
    write(SP, sp)
    print("StylePreset.java: lightSurface javadoc polished")
else:
    print("StylePreset.java: lightSurface javadoc already polished -> skip")

# ------------------------------------------------------------ 落地校验（必跑）
print("=== VERIFY ===")
sp, cp, cs = read(SP), read(CP), read(CS)

for name in ("gemAccentSkill", "gemAccentSupport", "gemAccentSpirit", "gemAccentFallback"):
    # base 用 4 空格缩进精确匹配（@Override 行前面是 "@Override "，不会被误计）
    assert sp.count("    public int %s()" % name) == 1, "StylePreset missing base %s" % name
    assert sp.count("@Override public int %s()" % name) == 3, "StylePreset overrides for %s != 3" % name
    assert cp.count('tok("%s"' % name) == 1, "ConfigStylePreset missing tok forwarder %s" % name
assert sp.count("    public boolean lightSurface()") == 1, "missing base lightSurface()"
assert sp.count("@Override public boolean lightSurface()") == 1, "missing Paper lightSurface() override"
assert sp.count("talentFillSafeFloor") >= 4, "talentFillSafeFloor lost in StylePreset!"
assert cp.count("talentFillSafeFloor") >= 3, "talentFillSafeFloor lost in ConfigStylePreset!"

# boolean 令牌转发 + 既有 int tok 重载未被改动
assert cp.count('flag("lightSurface"') == 1, "missing lightSurface flag forwarder"
assert cp.count("    @Override public boolean lightSurface()") == 1, "missing lightSurface override"
assert cp.count("private int tok(") == 2, "int tok overloads changed (must stay 2)"
assert cp.count("private boolean flag(") == 1, "missing private boolean flag helper"

for lit in ("0xFFE0573F", "0xFF3CC56E", "0xFFA86FE0", "0xFF4FA3E0"):
    assert lit not in cs, "inline literal %s still in CutScreen" % lit
assert "private static int blend(" not in cs, "private blend() still in CutScreen"
assert "Theme.blend(Theme.current().gemViewBg(), accent, 0.35F)" in cs, "blend call missing/wrong order"
assert cs.count("emphasis(accent)") == 2, "emphasis() not wired to both usages"
assert "Theme.current().lightSurface()" in cs, "lightSurface branch missing"

print("  StylePreset       : 4 base + 3x4 overrides + lightSurface(base+Paper)")
print("  ConfigStylePreset : 4 tok() forwarders")
print("  CutScreen         : 0 inline ARGB, blend order correct, emphasis x2")
print("  talentFillSafeFloor preserved in both files")
print("ALL CHECKS PASSED")
