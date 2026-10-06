package com.cimdy.coe.ui.screen.progression;
import com.cimdy.coe.ui.widget.render.Theme;

import com.cimdy.coe.data.codec.progression.PlayerProgress;
import com.cimdy.coe.data.codec.progression.ProgressionData;
import com.cimdy.coe.data.codec.progression.ProgressionService;
import com.cimdy.coe.data.codec.progression.TechEffect;
import com.cimdy.coe.data.codec.progression.TechNodeDef;
import com.cimdy.coe.network.research.UnlockTechPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 科技树界面：<b>按等级分段的按钮网格</b>，刻意不做成网状/树状。
 *
 * <p>设计取舍：科技树的门槛就是「玩家等级」这一条主线，没有前置 DAG。
 * 若也画成连线图，玩家会误以为存在路径依赖而反复找路；改用「等级段 → 该段按钮」的
 * 分栏网格后，一屏就能回答唯一重要的问题——<b>我这一级能开什么、还差几点</b>。
 *
 * <p>状态三分：已解锁（绿）/ 可解锁（青，等级达标且点数足够）/ 灰显（等级未到或点数不足）。
 * 判定统一走 {@link ProgressionService#canUnlockTech}，与服务端同一份逻辑。
 *
 * <p>外壳（背景 / 右侧面板 / 页签 / 裁剪）由 {@link ProgressionTreeScreen} 提供。
 */
public class TechTreeScreen extends ProgressionTreeScreen {

    private static final int CELL_W = 132;
    private static final int CELL_H = 30;
    private static final int CELL_GAP = 6;
    private static final int SECTION_HEADER_H = 16;
    private static final int SECTION_GAP = 8;
    private static final int SCROLL_STEP = 24;

    /** 等级段 → 该段科技节点。 */
    private final Map<Integer, List<TechNodeDef>> sections = new LinkedHashMap<>();
    /** 每个按钮的屏幕矩形（滚动后实时重算）：节点 → [x, y, w, h]。 */
    private final Map<TechNodeDef, int[]> cellRects = new LinkedHashMap<>();
    /** 每个等级段标题的屏幕 y（与 cellRects 同批算出，渲染时不再重复推导）。 */
    private final Map<Integer, Integer> sectionHeaderY = new LinkedHashMap<>();

    private int scroll = 0;
    private int contentHeight = 0;
    /** 选中节点：点击格子即记录，右面板底部展示「需求 / 效果」小结（未做项 #2 关闭）。 */
    private TechNodeDef selected = null;

    public TechTreeScreen(BlockPos benchPos) {
        super(Component.translatable("screen.coe.tech_tree"), benchPos);
    }

    @Override
    protected TabAccent accent() {
        // 不能再是 static final：那会在类加载时冻结，换肤对它无效（与 TalentTheme 同一个坑）
        return new TabAccent(Theme.current().progressionTechAccent(),
                Theme.current().progressionTechAccentBg(),
                Theme.current().progressionTechAccentText());
    }

    @Override
    protected boolean isTechScreen() {
        return true;
    }

    @Override
    protected void init() {
        initPanel();

        sections.clear();
        for (TechNodeDef def : ProgressionData.techSorted()) {
            sections.computeIfAbsent(def.minLevel(), k -> new ArrayList<>()).add(def);
        }
        scroll = 0;
        // 必须在这里先算一次：contentHeight 只在渲染里赋值的话，界面打开后、首帧绘制前
        // 滚轮拿到的 maxScroll 恒为 0，表现为「刚打开时滚不动」。
        contentHeight = layoutCells();
    }

    // ====== 布局 ======

    /** 一行能放几个按钮。布局与渲染共用，避免两处各算一遍后悄悄算歪。 */
    private int perRow() {
        int areaW = contentAreaWidth() - PAD * 8;
        return Math.max(1, (areaW + CELL_GAP) / (CELL_W + CELL_GAP));
    }

    /** 按当前滚动量重算所有按钮矩形与分段标题位置，并返回内容总高度。 */
    private int layoutCells() {
        cellRects.clear();
        sectionHeaderY.clear();
        int areaX = PAD * 4;
        int perRow = perRow();
        int y = CONTENT_TOP - scroll;
        int total = CONTENT_TOP;
        for (Map.Entry<Integer, List<TechNodeDef>> e : sections.entrySet()) {
            sectionHeaderY.put(e.getKey(), y);
            y += SECTION_HEADER_H;
            total += SECTION_HEADER_H;
            List<TechNodeDef> list = e.getValue();
            int rows = (list.size() + perRow - 1) / perRow;
            for (int i = 0; i < list.size(); i++) {
                cellRects.put(list.get(i), new int[]{
                        areaX + (i % perRow) * (CELL_W + CELL_GAP),
                        y + (i / perRow) * (CELL_H + CELL_GAP),
                        CELL_W, CELL_H});
            }
            int block = rows * (CELL_H + CELL_GAP) + SECTION_GAP;
            y += block;
            total += block;
        }
        return total;
    }

    // ====== 渲染 ======

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        super.extractRenderState(g, mx, my, partialTick);

        PlayerProgress p = PlayerProgress.get(player);
        contentHeight = layoutCells();
        drawTitle(g, getTitle());

        // 只有落在内容区内才算悬浮：否则鼠标停在标题带上，底下滚过去的按钮会「隔空高亮」
        boolean canHover = inContentArea(mx, my);
        TechNodeDef hovered = null;
        int areaX = PAD * 4;

        pushContentClip(g);
        for (Map.Entry<Integer, List<TechNodeDef>> e : sections.entrySet()) {
            int lv = e.getKey();
            boolean reached = p.level() >= lv;
            int hy = sectionHeaderY.getOrDefault(lv, Integer.MIN_VALUE);
            if (hy + SECTION_HEADER_H > CONTENT_TOP && hy < height) {
                g.text(font(), Component.translatable("tech.coe.section", lv),
                        areaX, hy + 3, reached ? Theme.current().progressionTechAccent() : Theme.current().progressionTextDim(), true);
                g.fill(areaX, hy + SECTION_HEADER_H - 3, contentAreaWidth() - PAD * 4, hy + SECTION_HEADER_H - 2,
                        reached ? Theme.current().progressionSectionSepReached() : Theme.current().progressionSectionSepLocked());
            }
            for (TechNodeDef def : e.getValue()) {
                int[] r = cellRects.get(def);
                if (r == null || r[1] + r[3] < CONTENT_TOP || r[1] > height) continue;
                boolean isHover = canHover && hovering(mx, my, r);
                if (isHover) hovered = def;
                drawCell(g, r, def, p, isHover);
            }
        }
        drawScrollbar(g);
        popContentClip(g);

        drawRightPanel(g, mx, my, p);
        // tooltip 画在裁剪之外：它允许溢出内容区（贴着光标走，靠边时自行回折）
        if (hovered != null) drawTooltip(g, hovered, p, mx, my);
    }

    private void drawScrollbar(GuiGraphicsExtractor g) {
        int viewH = contentViewHeight();
        if (contentHeight <= viewH) return;
        int sbX = contentAreaWidth() - PAD * 3;
        g.fill(sbX, CONTENT_TOP, sbX + 4, height - 4, Theme.current().progressionScrollTrack());
        int knobH = Math.max(12, (height - CONTENT_TOP - 4) * viewH / contentHeight);
        int maxScroll = contentHeight - viewH;
        int knobY = CONTENT_TOP + (maxScroll > 0 ? (height - CONTENT_TOP - 4 - knobH) * scroll / maxScroll : 0);
        g.fill(sbX, knobY, sbX + 4, knobY + knobH, Theme.current().progressionTechAccent());
    }

    private void drawCell(GuiGraphicsExtractor g, int[] r, TechNodeDef def, PlayerProgress p, boolean hover) {
        boolean unlocked = p.unlockedTech().contains(def.id());
        boolean levelOk = p.level() >= def.minLevel();
        boolean affordable = p.techPoints() >= def.cost();
        boolean canUnlock = !unlocked && levelOk && affordable;

        int bg, border, textColor;
        if (unlocked) {
            bg = hover ? Theme.current().progressionCellUnlockedBgHover() : Theme.current().progressionCellUnlockedBg();
            border = Theme.current().progressionBorderAllocated();
            textColor = Theme.current().progressionCellUnlockedText();
        } else if (canUnlock) {
            bg = hover ? Theme.current().progressionCellAvailableBgHover() : Theme.current().progressionCellAvailableBg();
            border = Theme.current().progressionAccentCyan();
            textColor = Theme.current().progressionCellAvailableText();
        } else {
            bg = Theme.current().progressionCellLockedBg();
            border = Theme.current().progressionCellLockedBorder();
            textColor = Theme.current().progressionCellLockedText();
        }
        g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], bg);
        g.outline(r[0], r[1], r[2], r[3], border);

        g.text(font(), techName(def), r[0] + 5, r[1] + 5, textColor, true);

        // 右下角：花费 / 已解锁标记
        Component right = unlocked
                ? Component.translatable("tech.coe.cell.unlocked")
                : Component.translatable("tech.coe.cell.cost", def.cost());
        g.text(font(), right, r[0] + r[2] - font().width(right) - 5, r[1] + r[3] - 11,
                unlocked ? Theme.current().progressionBorderAllocated()
                : (affordable ? Theme.current().progressionCostReady() : Theme.current().progressionCostShort()), true);

        // 左下角：等级门槛（未达标时飘红）
        g.text(font(), Component.translatable("tech.coe.cell.level", def.minLevel()),
                r[0] + 5, r[1] + r[3] - 11,
                levelOk ? Theme.current().progressionLevelOkText() : Theme.current().progressionCostShort(), true);
    }

    private void drawRightPanel(GuiGraphicsExtractor g, int mx, int my, PlayerProgress p) {
        drawPanelChrome(g, mx, my);

        int y = PAD + TAB_H + 6;
        y = line(g, y, Component.translatable("progression.coe.level", p.level()), Theme.current().progressionLevelText());
        y = line(g, y, Component.translatable("progression.coe.xp", p.xp(), PlayerProgress.xpToNext(p.level())), Theme.current().progressionInfoText());
        y = line(g, y, Component.translatable("progression.coe.tech_points", p.techPoints()),
                Theme.current().progressionTechPointsText());
        y = line(g, y, Component.translatable("progression.coe.unlocked_tech",
                p.unlockedTech().size(), ProgressionData.TECH_NODES.size()), Theme.current().progressionInfoText());

        // ---- 当前配额（科技的实际收益，放在最显眼处便于权衡） ----
        y += 6;
        y = line(g, y, Component.translatable("progression.coe.chunk_budget",
                p.chunksUsed(), ProgressionService.chunkBudget(p)), Theme.current().progressionBudgetText());
        y = line(g, y, Component.translatable("progression.coe.entity_cap", ProgressionService.entityCap(p)), Theme.current().progressionBudgetText());
        y = line(g, y, Component.translatable("progression.coe.site_cap", ProgressionService.siteCap(p)), Theme.current().progressionBudgetText());

        // ---- 选中节点小结（需求 / 效果；已点 x/y 见上方 unlocked_tech 行，不重复） ----
        if (selected != null) {
            y += 6;
            y = line(g, y, techName(selected), Theme.current().progressionTechAccent());
            if (p.unlockedTech().contains(selected.id())) {
                y = line(g, y, Component.translatable("tech.coe.cell.unlocked"), Theme.current().progressionBorderAllocated());
            } else {
                boolean levelOk = p.level() >= selected.minLevel();
                boolean affordable = p.techPoints() >= selected.cost();
                y = line(g, y, Component.translatable("tech.coe.cell.level", selected.minLevel()),
                        levelOk ? Theme.current().progressionLevelOkText() : Theme.current().progressionCostShort());
                y = line(g, y, Component.translatable("tech.coe.cell.cost", selected.cost()),
                        affordable ? Theme.current().progressionCostReady() : Theme.current().progressionCostShort());
            }
            List<Component> eff = describe(selected.effect());
            if (eff.isEmpty()) {
                y = line(g, y, Component.translatable("screen.coe.hint.scroll"), Theme.current().progressionHint());
            } else {
                int shown = 0;
                for (Component c : eff) {
                    if (shown >= 6) {
                        y = line(g, y, Component.literal("…"), Theme.current().progressionHint());
                        break;
                    }
                    y = line(g, y, c, Theme.current().progressionTextBody());
                    shown++;
                }
            }
        }

        y += 6;
        line(g, y, Component.translatable("screen.coe.hint.scroll"), Theme.current().progressionHint());
    }

    private void drawTooltip(GuiGraphicsExtractor g, TechNodeDef def, PlayerProgress p, int mx, int my) {
        List<Component> lines = new ArrayList<>();
        lines.add(techName(def));
        lines.addAll(describe(def.effect()));
        if (!p.unlockedTech().contains(def.id())) {
            // 三条提示的实参个数必须与语言文件里的占位符个数一致，否则整行会退化成带 %s 的模板原文
            if (p.level() < def.minLevel()) {
                lines.add(Component.translatable("tech.coe.tooltip.need_level", p.level(), def.minLevel()));
            } else if (p.techPoints() < def.cost()) {
                lines.add(Component.translatable("tech.coe.tooltip.need_points", p.techPoints(), def.cost()));
            } else {
                lines.add(Component.translatable("tech.coe.tooltip.click_unlock", def.cost()));
            }
        }
        int pad = 4, lineH = 12;
        int boxW = 0;
        for (Component l : lines) boxW = Math.max(boxW, font().width(l));
        int boxH = lines.size() * lineH + pad * 2;
        int bx = Math.min(mx + 14, contentAreaWidth() - boxW - pad * 2 - 2);
        int by = Math.min(my + 14, height - boxH - 2);
        g.fill(bx, by, bx + boxW + pad * 2, by + boxH, Theme.current().progressionTooltipBg());
        g.outline(bx, by, boxW + pad * 2, boxH, Theme.current().progressionTooltipBorder());
        for (int i = 0; i < lines.size(); i++) {
            g.text(font(), lines.get(i), bx + pad, by + pad + i * lineH,
                    i == 0 ? Theme.current().progressionTechAccent() : Theme.current().progressionTextBody(), false);
        }
    }

    /** 把科技效果翻译成人话——只列非零/非空项，避免 tooltip 被一堆「+0」淹没。 */
    private static List<Component> describe(TechEffect eff) {
        List<Component> lines = new ArrayList<>();
        if (eff.chunkBudgetAdd() != 0)
            lines.add(Component.translatable("tech.coe.effect.chunk_budget", eff.chunkBudgetAdd()));
        if (eff.entityCapAdd() != 0)
            lines.add(Component.translatable("tech.coe.effect.entity_cap", eff.entityCapAdd()));
        if (eff.siteCapAdd() != 0)
            lines.add(Component.translatable("tech.coe.effect.site_cap", eff.siteCapAdd()));
        for (String wt : eff.unlockWorkTypes())
            lines.add(Component.translatable("tech.coe.effect.work_type",
                    Component.translatable("work_type.coe." + wt)));
        for (Identifier id : eff.unlockBlocks())
            lines.add(Component.translatable("tech.coe.effect.block", id.getPath()));
        for (Identifier id : eff.unlockItems())
            lines.add(Component.translatable("tech.coe.effect.item", id.getPath()));
        for (Identifier id : eff.unlockResearch())
            lines.add(Component.translatable("tech.coe.effect.research", id.getPath()));
        for (Identifier id : eff.unlockQuests())
            lines.add(Component.translatable("tech.coe.effect.quest", id.getPath()));
        return lines;
    }

    private static Component techName(TechNodeDef def) {
        if (!def.name().isEmpty()) return Component.translatable(def.name());
        return Component.translatable("tech." + def.id().getNamespace() + "." + def.id().getPath());
    }

    // ====== 鼠标 ======

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        int mx = (int) event.x();
        int my = (int) event.y();

        if (consumedByPanel(mx, my)) return true;
        // 与裁剪同一条边界：标题带下面滚过去的按钮不该被点到
        if (!inContentArea(mx, my)) return super.mouseClicked(event, bl);

        PlayerProgress p = PlayerProgress.get(player);
        for (Map.Entry<TechNodeDef, int[]> e : cellRects.entrySet()) {
            if (!hovering(mx, my, e.getValue())) continue;
            TechNodeDef def = e.getKey();
            // 点击即选中（详情进右面板小结），可解锁时另发包解锁
            selected = def;
            // 与服务端同一份判定：不满足就静默忽略，不发无效包
            if (!ProgressionService.canUnlockTech(p, def)) return true;
            ClientPacketDistributor.sendToServer(new UnlockTechPacket(def.id()));
            return true;
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX < rightPanelX) {
            int maxScroll = Math.max(0, contentHeight - contentViewHeight());
            scroll = Math.max(0, Math.min(maxScroll, scroll - (int) Math.signum(scrollY) * SCROLL_STEP));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
