#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""UI-B3-002 子项 3/4 调用点迁移：StunScreen alpha 缺陷 + stronghold / screen / widget-keyword 收尾。"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply, read_text, write_text

UI = "src/main/java/com/cimdy/coe/ui/"
THEME_IMPORT = "import com.cimdy.coe.ui.widget.render.Theme;\n"


def ensure_theme_import(rel):
    """若文件还没 import Theme，插到第一个 import 之前。"""
    text, nl = read_text(rel)
    if THEME_IMPORT.strip() in text:
        print(f"  ..  {rel}: Theme 已导入")
        return
    idx = text.find("\nimport ")
    assert idx != -1, rel
    text = text[:idx] + "\n" + THEME_IMPORT.strip() + text[idx:]
    write_text(rel, text, nl)
    print(f"  ok  {rel}: 补 import Theme")


# ======================================================================
# 子项 3：StunScreen 0xFFFFFF（6 位 → alpha=0，文字本应不可见）
# ======================================================================
ensure_theme_import(UI + "screen/StunScreen.java")
apply(UI + "screen/StunScreen.java", [
    # 6 位字面量按 ARGB int 解析 = 0x00FFFFFF（全透明）。补成不透明白。
    ("0xFFFFFF, 3.0F)", "Theme.current().textBright(), 3.0F)"),
])

# ======================================================================
# 子项 4-a：screen/stronghold
# ======================================================================
ensure_theme_import(UI + "screen/stronghold/SiteScreen.java")
apply(UI + "screen/stronghold/SiteScreen.java", [
    ("""    /** 五档优先级的显示符号，索引 0 对应优先级 1。 */
    private static final Component[] PRIORITY_LABELS = {
            Component.literal("<<").withColor(0xFF8B0000),
            Component.literal("<").withColor(0xFFCD5C00),
            Component.literal("=").withColor(0xFF9ACD32),
            Component.literal(">").withColor(0xFF32CD32),
            Component.literal(">>").withColor(0xFF006400)
    };
""",
     """    /** 五档优先级档数（索引 0 对应优先级 1）。 */
    private static final int PRIORITY_COUNT = 5;

    /**
     * 五档优先级的显示符号，索引 0 对应优先级 1。
     *
     * <p>刻意<b>不用</b> {@code static final Component[]}：那会在类加载时把颜色冻死，
     * 换肤对这五个色完全无效（与 TalentTheme / TabAccent 同一个坑）。改成每帧现取。
     */
    private static Component priorityLabel(int i) {
        int color = switch (i) {
            case 0 -> Theme.current().threatTier1();
            case 1 -> Theme.current().threatTier2();
            case 2 -> Theme.current().threatTier3();
            case 3 -> Theme.current().threatTier4();
            default -> Theme.current().threatTier5();
        };
        String glyph = switch (i) {
            case 0 -> "<<";
            case 1 -> "<";
            case 2 -> "=";
            case 3 -> ">";
            default -> ">>";
        };
        return Component.literal(glyph).withColor(color);
    }
"""),
    ("PRIORITY_LABELS.length", "PRIORITY_COUNT"),
    ("Button.of(PRIORITY_LABELS[i])", "Button.of(priorityLabel(i))"),
    (".withColor(on ? 0xFF00FF00 : 0xFFAAAAAA);",
     ".withColor(on ? Theme.current().statusOn() : Theme.current().statusOff());"),
])

apply(UI + "screen/stronghold/StockScreen.java", [
    (".fill(0x80ADD8E6)", ".fill(Theme.current().stockSliderFill())"),
    (".withStyle(s -> s.withColor(0xFF808080))",
     ".withStyle(s -> s.withColor(Theme.current().placeholderText()))"),
    ("Math.max(0, Math.min(1, s1 / MAX_CAP)), 0x80FF6666);",
     "Math.max(0, Math.min(1, s1 / MAX_CAP)), Theme.current().stockTransitBand());"),
    ("bar.segment(0, Math.max(0, Math.min(1, stored / MAX_CAP)), 0x8090EE90);",
     "bar.segment(0, Math.max(0, Math.min(1, stored / MAX_CAP)), Theme.current().stockStoredBand());"),
    (".fixedScale(0.7F)\n                .color(0xFFB0B0B0));",
     ".fixedScale(0.7F)\n                .color(Theme.current().stockQtyLabel()));"),
    (".fixedScale(0.7F)\n                .color(0xFFFFFFFF));",
     ".fixedScale(0.7F)\n                .color(Theme.current().stockQtyValue()));"),
])

apply(UI + "screen/stronghold/TerminalScreen.java", [
    (".color(combatant ? Theme.current().toastWarn() : 0xFF9A9A9A)",
     ".color(combatant ? Theme.current().toastWarn() : Theme.current().statusOffDim())"),
    ("HighlightRenderer.addHighlightedPositions(posList, 0xFF00FF00, 100);",
     "HighlightRenderer.addHighlightedPositions(posList, Theme.current().statusOn(), 100);"),
])

# ======================================================================
# 子项 4-b：screen 根目录
# ======================================================================
ensure_theme_import(UI + "screen/AbstractPanelScreen.java")
apply(UI + "screen/AbstractPanelScreen.java", [
    ("""    /** 槽位格子底色（深色填充；不画 1px 边框以避免与原版 extractSlot 物品 1px 偏移重叠产生错位）。 */
    private static final int SLOT_FILL = 0xFF1E2735;
""",
     """    /**
     * 槽位格子底色（深色填充；不画 1px 边框以避免与原版 extractSlot 物品 1px 偏移重叠产生错位）。
     *
     * <p>原来是 {@code private static final int}：类加载时即冻结，换肤对它无效。改为每次现取。
     */
    private static int slotFill() {
        return Theme.current().panelSlotFill();
    }
"""),
    ("graphics.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, SLOT_FILL);",
     "graphics.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, slotFill());"),
])

apply(UI + "screen/CoeUi.java", [
    ("        n.with(TextRenderer.of(text)\n                .color(0xFF808080)",
     "        n.with(TextRenderer.of(text)\n                .color(Theme.current().placeholderText())"),
])

ensure_theme_import(UI + "screen/ExtraSlotsScreen.java")
apply(UI + "screen/ExtraSlotsScreen.java", [
    ("""    /** 充能条：满格金黄，底槽深灰（取原版 GUI 调色）。 */
    private static final int CHARGE = 0xFFFFD24A;
    private static final int CHARGE_BG = 0xFF2B2B2B;
""",
     """    /**
     * 充能条：满格金黄，底槽深灰（取原版 GUI 调色）。
     *
     * <p>原来是 {@code private static final int}：类加载时即冻结，换肤对它无效。改为每次现取；
     * 满格金与全局金色强调同源（{@code goldAccent()}），换肤后两处仍会一起变。
     */
    private static int chargeColor() {
        return Theme.current().goldAccent();
    }

    private static int chargeTrackColor() {
        return Theme.current().chargeTrackBg();
    }
"""),
    ("graphics.fill(x, y, x + 3, y + 16, CHARGE_BG);",
     "graphics.fill(x, y, x + 3, y + 16, chargeTrackColor());"),
    ("graphics.fill(x, y + 16 - filled, x + 3, y + 16, CHARGE);",
     "graphics.fill(x, y + 16 - filled, x + 3, y + 16, chargeColor());"),
    ("leftPos + ExtraSlotsMenu.FLASK_X, y, 0xFFFFFFFF, false);",
     "leftPos + ExtraSlotsMenu.FLASK_X, y, Theme.current().textBright(), false);"),
    ("leftPos + ExtraSlotsMenu.CHARM_X, y, 0xFFFFFFFF, false);",
     "leftPos + ExtraSlotsMenu.CHARM_X, y, Theme.current().textBright(), false);"),
    ("leftPos + ExtraSlotsMenu.ACCESSORY_X, y, 0xFFFFFFFF, false);",
     "leftPos + ExtraSlotsMenu.ACCESSORY_X, y, Theme.current().textBright(), false);"),
])

# ======================================================================
# 子项 4-c：widget/keyword
# ======================================================================
apply(UI + "widget/keyword/KeywordTooltipBox.java", [
    ("Draw.shadow(g, box.absX, box.absY, box.width, box.height, RADIUS, 4, 0x4D000000);",
     "Draw.shadow(g, box.absX, box.absY, box.width, box.height, RADIUS, 4, Theme.current().shadowSoft());"),
])

print("\n[misc] 调用点迁移完成")
