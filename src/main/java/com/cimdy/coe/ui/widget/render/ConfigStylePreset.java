package com.cimdy.coe.ui.widget.render;

/**
 * 配置驱动的级联风格预设。
 *
 * <p>每个颜色 / 几何令牌都走「屏幕覆盖 → 角色 → 全局 → 内置 Dark 默认」的级联解析
 * （见 {@link StylePresetDef#lookup}）；皮肤层按组件角色（button/window/...）解析令牌，
 * 从而做到「某组件设了、另一个没设就用那一个」。未配置任何令牌时与内置 Dark 外观完全一致（零回归）。
 *
 * <p>构造器为 {@code protected}，本类与 {@link StylePreset} 的内置子类共享同一构造入口。
 */
final class ConfigStylePreset extends StylePreset {

    private final StylePresetDef def;

    public ConfigStylePreset(StylePresetDef def) {
        this.def = def;
    }

    @Override
    public String id() { return def.id(); }

    @Override
    public String displayName() { return def.label(); }

    /** 级联解析：屏幕覆盖 -> 角色 -> 全局 -> 内置 Dark 默认（super 即 Dark 默认）。 */
    private int tok(String name, int fallback) {
        return tok(name, null, fallback);
    }

    private int tok(String name, String role, int fallback) {
        Integer v = def.lookup(name, role, StylePresetContext.currentScreen());
        return v != null ? v : fallback;
    }

    /** boolean 令牌：复用既有 int 级联（配置里写 1 / 0，非 0 视为 true）。
     * <p>刻意<b>不改动</b>既有 int {@code tok} 重载，也不动 {@code StylePresetDef} / 编解码 / 保存路径 ——
     * 那些属于「批次A 配置驱动预设」的共享设施。原生 boolean 支持需扩 StylePresetDef.CODEC，另案处理。 */
    private boolean flag(String name, boolean fallback) {
        Integer v = def.lookup(name, null, StylePresetContext.currentScreen());
        return v != null ? v != 0 : fallback;
    }

    // ==================================================================
    // 颜色 / 几何令牌：全部走级联，未配置则回退 Dark 默认
    // ==================================================================
    @Override public int accent() { return tok("accent", super.accent()); }
    @Override public int accentBright() { return tok("accentBright", super.accentBright()); }
    @Override public int panelColor() { return tok("panelColor", super.panelColor()); }
    @Override public int panelDim() { return tok("panelDim", super.panelDim()); }
    @Override public int windowBg() { return tok("windowBg", super.windowBg()); }
    @Override public int inputBg() { return tok("inputBg", super.inputBg()); }
    @Override public int panelSide() { return tok("panelSide", super.panelSide()); }
    @Override public int popupBg() { return tok("popupBg", super.popupBg()); }
    @Override public int popupBorder() { return tok("popupBorder", super.popupBorder()); }
    @Override public int track() { return tok("track", super.track()); }
    @Override public int slotBg() { return tok("slotBg", super.slotBg()); }
    @Override public int slotBorder() { return tok("slotBorder", super.slotBorder()); }
    @Override public int hoverBg() { return tok("hoverBg", super.hoverBg()); }
    @Override public int activeBg() { return tok("activeBg", super.activeBg()); }
    @Override public int pressedBg() { return tok("pressedBg", super.pressedBg()); }
    @Override public int disabledBg() { return tok("disabledBg", super.disabledBg()); }
    @Override public int inputBorderHover() { return tok("inputBorderHover", super.inputBorderHover()); }
    @Override public int borderDisabled() { return tok("borderDisabled", super.borderDisabled()); }
    @Override public int handleBg() { return tok("handleBg", super.handleBg()); }
    @Override public int handleHover() { return tok("handleHover", super.handleHover()); }
    @Override public int titleBarBg() { return tok("titleBarBg", super.titleBarBg()); }
    @Override public int cornerDots() { return tok("cornerDots", super.cornerDots()); }
    @Override public int cornerTicks() { return tok("cornerTicks", super.cornerTicks()); }
    @Override public int optionHoverBg() { return tok("optionHoverBg", super.optionHoverBg()); }
    @Override public int textHover() { return tok("textHover", super.textHover()); }
    @Override public int textDisabled() { return tok("textDisabled", super.textDisabled()); }
    @Override public int text() { return tok("text", super.text()); }
    @Override public int toggleTrackOff() { return tok("toggleTrackOff", super.toggleTrackOff()); }
    @Override public int toggleTrackOn() { return tok("toggleTrackOn", super.toggleTrackOn()); }
    @Override public int toggleThumb() { return tok("toggleThumb", super.toggleThumb()); }
    @Override public int progressTrackColor() { return tok("progressTrackColor", super.progressTrackColor()); }
    @Override public int progressFill() { return tok("progressFill", super.progressFill()); }
    @Override public int tabBgColor() { return tok("tabBgColor", super.tabBgColor()); }
    @Override public int tabActive() { return tok("tabActive", super.tabActive()); }
    @Override public int tabBorder() { return tok("tabBorder", super.tabBorder()); }
    @Override public int toastBg() { return tok("toastBg", super.toastBg()); }
    @Override public int toastBorder() { return tok("toastBorder", super.toastBorder()); }
    @Override public int toastInfo() { return tok("toastInfo", super.toastInfo()); }
    @Override public int toastSuccess() { return tok("toastSuccess", super.toastSuccess()); }
    @Override public int toastWarn() { return tok("toastWarn", super.toastWarn()); }
    @Override public int toastError() { return tok("toastError", super.toastError()); }
    @Override public int pagerBarBg() { return tok("pagerBarBg", super.pagerBarBg()); }
    @Override public int textBright() { return tok("textBright", super.textBright()); }
    @Override public int surfaceTransparent() { return tok("surfaceTransparent", super.surfaceTransparent()); }
    @Override public int scrim() { return tok("scrim", super.scrim()); }
    @Override public int textMuted() { return tok("textMuted", super.textMuted()); }
    @Override public int textDanger() { return tok("textDanger", super.textDanger()); }
    @Override public int goldAccent() { return tok("goldAccent", super.goldAccent()); }
    @Override public int treePanelBg() { return tok("treePanelBg", super.treePanelBg()); }
    @Override public int treePanelBorder() { return tok("treePanelBorder", super.treePanelBorder()); }
    @Override public int textBody() { return tok("textBody", super.textBody()); }
    @Override public int statusOk() { return tok("statusOk", super.statusOk()); }
    @Override public int statusAllocated() { return tok("statusAllocated", super.statusAllocated()); }
    @Override public int talentScreenBg() { return tok("talentScreenBg", super.talentScreenBg()); }
    @Override public int talentPanelBg() { return tok("talentPanelBg", super.talentPanelBg()); }
    @Override public int talentPanelBorder() { return tok("talentPanelBorder", super.talentPanelBorder()); }
    @Override public int talentSummaryBg() { return tok("talentSummaryBg", super.talentSummaryBg()); }
    @Override public int talentControlTrackBg() { return tok("talentControlTrackBg", super.talentControlTrackBg()); }
    @Override public int talentScrollTrackBg() { return tok("talentScrollTrackBg", super.talentScrollTrackBg()); }
    @Override public int talentBorderLocked() { return tok("talentBorderLocked", super.talentBorderLocked()); }
    @Override public int talentBorderAvailable() { return tok("talentBorderAvailable", super.talentBorderAvailable()); }
    @Override public int talentPreview() { return tok("talentPreview", super.talentPreview()); }
    @Override public int talentEdgeDim() { return tok("talentEdgeDim", super.talentEdgeDim()); }
    @Override public int talentEdgeLit() { return tok("talentEdgeLit", super.talentEdgeLit()); }
    @Override public int talentJewelZone() { return tok("talentJewelZone", super.talentJewelZone()); }
    @Override public int talentJewelLit() { return tok("talentJewelLit", super.talentJewelLit()); }
    @Override public int talentSocketEmpty() { return tok("talentSocketEmpty", super.talentSocketEmpty()); }
    @Override public int talentSocketFilled() { return tok("talentSocketFilled", super.talentSocketFilled()); }
    @Override public int talentBindingGlobal() { return tok("talentBindingGlobal", super.talentBindingGlobal()); }
    @Override public int talentBinding1() { return tok("talentBinding1", super.talentBinding1()); }
    @Override public int talentBinding2() { return tok("talentBinding2", super.talentBinding2()); }
    @Override public int talentBindingDefault() { return tok("talentBindingDefault", super.talentBindingDefault()); }
    @Override public int talentAccent() { return tok("talentAccent", super.talentAccent()); }
    @Override public int talentAccentDim() { return tok("talentAccentDim", super.talentAccentDim()); }
    @Override public int talentKeystoneRingDim() { return tok("talentKeystoneRingDim", super.talentKeystoneRingDim()); }
    @Override public int talentTabActiveBg() { return tok("talentTabActiveBg", super.talentTabActiveBg()); }
    @Override public int talentTabBg() { return tok("talentTabBg", super.talentTabBg()); }
    @Override public int talentTabBorder() { return tok("talentTabBorder", super.talentTabBorder()); }
    @Override public int talentTabIdleBg() { return tok("talentTabIdleBg", super.talentTabIdleBg()); }
    @Override public int talentArrowIdleBg() { return tok("talentArrowIdleBg", super.talentArrowIdleBg()); }
    @Override public int talentControlBg() { return tok("talentControlBg", super.talentControlBg()); }
    @Override public int talentControlHoverBg() { return tok("talentControlHoverBg", super.talentControlHoverBg()); }
    @Override public int talentPrimaryHoverBg() { return tok("talentPrimaryHoverBg", super.talentPrimaryHoverBg()); }
    @Override public int talentPrimaryTextHot() { return tok("talentPrimaryTextHot", super.talentPrimaryTextHot()); }
    @Override public int talentDangerBg() { return tok("talentDangerBg", super.talentDangerBg()); }
    @Override public int talentDangerHoverBg() { return tok("talentDangerHoverBg", super.talentDangerHoverBg()); }
    @Override public int talentDangerBorder() { return tok("talentDangerBorder", super.talentDangerBorder()); }
    @Override public int talentDangerText() { return tok("talentDangerText", super.talentDangerText()); }
    @Override public int talentTextIdle() { return tok("talentTextIdle", super.talentTextIdle()); }
    @Override public int talentTextDim() { return tok("talentTextDim", super.talentTextDim()); }
    @Override public int talentTextNeutral() { return tok("talentTextNeutral", super.talentTextNeutral()); }
    @Override public int talentTextSoft() { return tok("talentTextSoft", super.talentTextSoft()); }
    @Override public int talentTextPoints() { return tok("talentTextPoints", super.talentTextPoints()); }
    @Override public int talentTextSummary() { return tok("talentTextSummary", super.talentTextSummary()); }
    @Override public int talentNumActive() { return tok("talentNumActive", super.talentNumActive()); }
    @Override public int talentPointsText() { return tok("talentPointsText", super.talentPointsText()); }
    @Override public int talentOk() { return tok("talentOk", super.talentOk()); }
    @Override public int talentNodeBg() { return tok("talentNodeBg", super.talentNodeBg()); }
    @Override public int talentNodeBgAllocated() { return tok("talentNodeBgAllocated", super.talentNodeBgAllocated()); }
    @Override public int talentBadgeText() { return tok("talentBadgeText", super.talentBadgeText()); }
    @Override public int talentFillSafeFloor() { return tok("talentFillSafeFloor", super.talentFillSafeFloor()); }
    @Override public int focusRing() { return tok("focusRing", super.focusRing()); }
    @Override public int progressionScreenBg() { return tok("progressionScreenBg", super.progressionScreenBg()); }
    @Override public int progressionTabBg() { return tok("progressionTabBg", super.progressionTabBg()); }
    @Override public int progressionTabHoverBg() { return tok("progressionTabHoverBg", super.progressionTabHoverBg()); }
    @Override public int progressionTabBorder() { return tok("progressionTabBorder", super.progressionTabBorder()); }
    @Override public int progressionTabText() { return tok("progressionTabText", super.progressionTabText()); }
    @Override public int progressionInfoText() { return tok("progressionInfoText", super.progressionInfoText()); }
    @Override public int progressionHint() { return tok("progressionHint", super.progressionHint()); }
    @Override public int progressionTechAccentBg() { return tok("progressionTechAccentBg", super.progressionTechAccentBg()); }
    @Override public int progressionTechAccentText() { return tok("progressionTechAccentText", super.progressionTechAccentText()); }
    @Override public int progressionSectionSepReached() { return tok("progressionSectionSepReached", super.progressionSectionSepReached()); }
    @Override public int progressionSectionSepLocked() { return tok("progressionSectionSepLocked", super.progressionSectionSepLocked()); }
    @Override public int progressionTextDim() { return tok("progressionTextDim", super.progressionTextDim()); }
    @Override public int progressionScrollTrack() { return tok("progressionScrollTrack", super.progressionScrollTrack()); }
    @Override public int progressionCellUnlockedBg() { return tok("progressionCellUnlockedBg", super.progressionCellUnlockedBg()); }
    @Override public int progressionCellUnlockedBgHover() { return tok("progressionCellUnlockedBgHover", super.progressionCellUnlockedBgHover()); }
    @Override public int progressionCellAvailableBg() { return tok("progressionCellAvailableBg", super.progressionCellAvailableBg()); }
    @Override public int progressionCellAvailableBgHover() { return tok("progressionCellAvailableBgHover", super.progressionCellAvailableBgHover()); }
    @Override public int progressionCellLockedBg() { return tok("progressionCellLockedBg", super.progressionCellLockedBg()); }
    @Override public int progressionCellLockedBorder() { return tok("progressionCellLockedBorder", super.progressionCellLockedBorder()); }
    @Override public int progressionCellLockedText() { return tok("progressionCellLockedText", super.progressionCellLockedText()); }
    @Override public int progressionCellAvailableText() { return tok("progressionCellAvailableText", super.progressionCellAvailableText()); }
    @Override public int progressionCostShort() { return tok("progressionCostShort", super.progressionCostShort()); }
    @Override public int progressionLevelOkText() { return tok("progressionLevelOkText", super.progressionLevelOkText()); }
    @Override public int progressionTooltipBg() { return tok("progressionTooltipBg", super.progressionTooltipBg()); }
    @Override public int progressionTooltipBorder() { return tok("progressionTooltipBorder", super.progressionTooltipBorder()); }
    @Override public int progressionBudgetText() { return tok("progressionBudgetText", super.progressionBudgetText()); }
    @Override public int progressionAccentCyan() { return tok("progressionAccentCyan", super.progressionAccentCyan()); }
    @Override public int progressionResearchAccentBg() { return tok("progressionResearchAccentBg", super.progressionResearchAccentBg()); }
    @Override public int progressionTextSoft() { return tok("progressionTextSoft", super.progressionTextSoft()); }
    @Override public int progressionBenchRunningText() { return tok("progressionBenchRunningText", super.progressionBenchRunningText()); }
    @Override public int progressionBarTrack() { return tok("progressionBarTrack", super.progressionBarTrack()); }
    @Override public int progressionEdgeLit() { return tok("progressionEdgeLit", super.progressionEdgeLit()); }
    @Override public int progressionTextSecondary() { return tok("progressionTextSecondary", super.progressionTextSecondary()); }
    @Override public int progressionTextHint() { return tok("progressionTextHint", super.progressionTextHint()); }
    @Override public int progressionBenchAbsent() { return tok("progressionBenchAbsent", super.progressionBenchAbsent()); }
    @Override public int progressionDanger() { return tok("progressionDanger", super.progressionDanger()); }
    @Override public int progressionBorderAllocatedInactive() { return tok("progressionBorderAllocatedInactive", super.progressionBorderAllocatedInactive()); }
    @Override public int progressionEdgeDim() { return tok("progressionEdgeDim", super.progressionEdgeDim()); }
    @Override public int progressionNodeFill() { return tok("progressionNodeFill", super.progressionNodeFill()); }
    @Override public int progressionNodeFillBroken() { return tok("progressionNodeFillBroken", super.progressionNodeFillBroken()); }
    @Override public int progressionTechAccent() { return tok("progressionTechAccent", super.progressionTechAccent()); }
    @Override public int progressionPanelBg() { return tok("progressionPanelBg", super.progressionPanelBg()); }
    @Override public int progressionPanelBorder() { return tok("progressionPanelBorder", super.progressionPanelBorder()); }
    @Override public int progressionTextBody() { return tok("progressionTextBody", super.progressionTextBody()); }
    @Override public int progressionResearchAccent() { return tok("progressionResearchAccent", super.progressionResearchAccent()); }
    @Override public int progressionBorderAllocated() { return tok("progressionBorderAllocated", super.progressionBorderAllocated()); }
    @Override public int progressionLevelText() { return tok("progressionLevelText", super.progressionLevelText()); }
    @Override public int progressionCostReady() { return tok("progressionCostReady", super.progressionCostReady()); }
    @Override public int progressionBorderAvailable() { return tok("progressionBorderAvailable", super.progressionBorderAvailable()); }
    @Override public int progressionBarBorder() { return tok("progressionBarBorder", super.progressionBarBorder()); }
    @Override public int progressionTechPointsText() { return tok("progressionTechPointsText", super.progressionTechPointsText()); }
    @Override public int progressionCellUnlockedText() { return tok("progressionCellUnlockedText", super.progressionCellUnlockedText()); }
    @Override public int progressionResearchAccentText() { return tok("progressionResearchAccentText", super.progressionResearchAccentText()); }
    @Override public int progressionBarFill() { return tok("progressionBarFill", super.progressionBarFill()); }
    @Override public int progressionBorderLocked() { return tok("progressionBorderLocked", super.progressionBorderLocked()); }
    @Override public int threatTier1() { return tok("threatTier1", super.threatTier1()); }
    @Override public int threatTier2() { return tok("threatTier2", super.threatTier2()); }
    @Override public int threatTier3() { return tok("threatTier3", super.threatTier3()); }
    @Override public int threatTier4() { return tok("threatTier4", super.threatTier4()); }
    @Override public int threatTier5() { return tok("threatTier5", super.threatTier5()); }
    @Override public int statusOn() { return tok("statusOn", super.statusOn()); }
    @Override public int statusOffDim() { return tok("statusOffDim", super.statusOffDim()); }
    @Override public int placeholderText() { return tok("placeholderText", super.placeholderText()); }
    @Override public int stockSliderFill() { return tok("stockSliderFill", super.stockSliderFill()); }
    @Override public int stockTransitBand() { return tok("stockTransitBand", super.stockTransitBand()); }
    @Override public int stockStoredBand() { return tok("stockStoredBand", super.stockStoredBand()); }
    @Override public int stockQtyLabel() { return tok("stockQtyLabel", super.stockQtyLabel()); }
    @Override public int panelSlotFill() { return tok("panelSlotFill", super.panelSlotFill()); }
    @Override public int chargeTrackBg() { return tok("chargeTrackBg", super.chargeTrackBg()); }
    @Override public int shadowSoft() { return tok("shadowSoft", super.shadowSoft()); }
    @Override public int statusOff() { return tok("statusOff", super.statusOff()); }
    @Override public int stockQtyValue() { return tok("stockQtyValue", super.stockQtyValue()); }
    @Override public int graphNodeBorder() { return tok("graphNodeBorder", super.graphNodeBorder()); }
    @Override public int graphHoverRing() { return tok("graphHoverRing", super.graphHoverRing()); }
    @Override public int graphNodeActiveFill() { return tok("graphNodeActiveFill", super.graphNodeActiveFill()); }
    @Override public int graphNodeDisabledFill() { return tok("graphNodeDisabledFill", super.graphNodeDisabledFill()); }
    @Override public int graphNodeDisabledBorder() { return tok("graphNodeDisabledBorder", super.graphNodeDisabledBorder()); }
    @Override public int graphEdgeHover() { return tok("graphEdgeHover", super.graphEdgeHover()); }
    @Override public int radialBackdrop() { return tok("radialBackdrop", super.radialBackdrop()); }
    @Override public int radialHubFill() { return tok("radialHubFill", super.radialHubFill()); }
    @Override public int radialBorderHot() { return tok("radialBorderHot", super.radialBorderHot()); }
    @Override public int radialBorderIdle() { return tok("radialBorderIdle", super.radialBorderIdle()); }
    @Override public int radialFillHot() { return tok("radialFillHot", super.radialFillHot()); }
    @Override public int radialFillIdle() { return tok("radialFillIdle", super.radialFillIdle()); }
    @Override public int radialTextHot() { return tok("radialTextHot", super.radialTextHot()); }
    @Override public int radialTextIdle() { return tok("radialTextIdle", super.radialTextIdle()); }
    @Override public int gemGold() { return tok("gemGold", super.gemGold()); }
    @Override public int gemWindowBg() { return tok("gemWindowBg", super.gemWindowBg()); }
    @Override public int gemPanelBg() { return tok("gemPanelBg", super.gemPanelBg()); }
    @Override public int gemViewBg() { return tok("gemViewBg", super.gemViewBg()); }
    @Override public int gemBackdropTop() { return tok("gemBackdropTop", super.gemBackdropTop()); }
    @Override public int gemBackdropBottom() { return tok("gemBackdropBottom", super.gemBackdropBottom()); }
    @Override public int gemTextMuted() { return tok("gemTextMuted", super.gemTextMuted()); }
    @Override public int gemTextPositive() { return tok("gemTextPositive", super.gemTextPositive()); }
    @Override public int gemDesaturate() { return tok("gemDesaturate", super.gemDesaturate()); }
    @Override public int gemAccentSkill() { return tok("gemAccentSkill", super.gemAccentSkill()); }
    @Override public int gemAccentSupport() { return tok("gemAccentSupport", super.gemAccentSupport()); }
    @Override public int gemAccentSpirit() { return tok("gemAccentSpirit", super.gemAccentSpirit()); }
    @Override public int gemAccentFallback() { return tok("gemAccentFallback", super.gemAccentFallback()); }
    @Override public boolean lightSurface() { return flag("lightSurface", super.lightSurface()); }
    @Override public int radiusSm() { return tok("radiusSm", super.radiusSm()); }
    @Override public int radiusMd() { return tok("radiusMd", super.radiusMd()); }
    @Override public int shadowOffsetX() { return tok("shadowOffsetX", super.shadowOffsetX()); }
    @Override public int shadowOffsetY() { return tok("shadowOffsetY", super.shadowOffsetY()); }
    @Override public int shadowBlur() { return tok("shadowBlur", super.shadowBlur()); }
    @Override public int shadowColor() { return tok("shadowColor", super.shadowColor()); }
    @Override public int shimmerColor() { return tok("shimmerColor", super.shimmerColor()); }

    // ==================================================================
    // 皮肤层：按角色解析令牌，使「某组件设了某没设就用那一个」
    // ==================================================================

    @Override
    public ColorRendererI button() {
        return buttonSkin(tok("panelDim", "button", super.panelDim()), tok("panelColor", "button", super.panelColor()),
                tok("hoverBg", "button", super.hoverBg()), tok("activeBg", "button", super.activeBg()),
                tok("pressedBg", "button", super.pressedBg()), tok("disabledBg", "button", super.disabledBg()),
                tok("borderDisabled", "button", super.borderDisabled()), tok("accent", "button", super.accent()));
    }

    @Override
    public ColorRendererI window() {
        return windowSkin(tok("windowBg", "window", super.windowBg()), tok("panelColor", "window", super.panelColor()),
                tok("cornerDots", "window", super.cornerDots()));
    }

    @Override
    public Renderer input() {
        return inputSkin(tok("inputBg", "input", super.inputBg()), tok("panelColor", "input", super.panelColor()),
                tok("inputBorderHover", "input", super.inputBorderHover()));
    }

    @Override
    public Renderer panel() {
        return panelSkin(tok("panelSide", "panel", super.panelSide()), tok("panelColor", "panel", super.panelColor()));
    }

    @Override
    public Renderer popup() {
        return popupSkin(tok("popupBg", "popup", super.popupBg()), tok("popupBorder", "popup", super.popupBorder()));
    }

    @Override
    public Renderer slot() {
        return slotSkin(tok("slotBg", "slot", super.slotBg()), tok("slotBorder", "slot", super.slotBorder()));
    }

    @Override
    public Renderer scrollTrack() {
        return scrollTrackSkin(tok("track", "scrollTrack", super.track()));
    }

    @Override
    public Renderer scrollThumb() {
        return scrollThumbSkin(tok("accent", "scrollThumb", super.accent()), tok("accentBright", "scrollThumb", super.accentBright()));
    }

    @Override
    public Renderer pagerBar() {
        return pagerBarSkin(tok("pagerBarBg", "pagerBar", super.pagerBarBg()));
    }

    @Override
    public Renderer handle() {
        return handleSkin(tok("handleBg", "handle", super.handleBg()), tok("handleHover", "handle", super.handleHover()));
    }

    @Override
    public Renderer progressTrack() {
        return progressTrackSkin(tok("progressTrackColor", "progressTrack", super.progressTrackColor()));
    }

    @Override
    public Renderer tabBg() {
        return tabBgSkin(tok("tabBgColor", "tabBg", super.tabBgColor()), tok("tabBorder", "tabBg", super.tabBorder()),
                tok("hoverBg", "tabBg", super.hoverBg()), tok("tabActive", "tabBg", super.tabActive()),
                tok("disabledBg", "tabBg", super.disabledBg()), tok("accent", "tabBg", super.accent()),
                tok("borderDisabled", "tabBg", super.borderDisabled()));
    }

}
