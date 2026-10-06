package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.render.BorderStyle;

/**
 * 可切换的「风格预设」——UI 大改（工作流 B）的核心机制。
 *
 * <p>之前 {@link Theme} 把调色板收敛成了单一事实来源，但「皮肤」仍只有一套写死的默认
 * 配色，无法按玩家偏好（高对比 / 紧凑）整体切换。本类把<b>调色板 + 设计令牌 + 皮肤装配</b>
 * 三者都收进可替换的预设里：
 *
 * <ul>
 *   <li><b>颜色层</b>：每个色值都是可覆盖的 getter，默认取 {@link Theme} 的规范常量
 *       （即「Dark」外观与旧库完全一致，保证零回归）。</li>
 *   <li><b>令牌层</b>：间距 / 圆角 / 字号 / 阴影层级，作为布局的统一计量单位，屏幕与组件
 *       应读取令牌而非散字面量（见工作流 B3 迁移）。</li>
 *   <li><b>皮肤层</b>：{@code button()}/{@code window()}/… 各返回一个按当前令牌装配好的
 *       {@link ColorRenderer}，覆盖某颜色 getter 后整套皮肤自动跟随。</li>
 * </ul>
 *
 * <p>切换方式：{@code Theme.apply(StylePreset.highContrast())}，全局即时生效；
 * 默认预设为 {@link #dark()}（与重构前完全一致）。所有 getter 在构建期不被求值，
 * 仅在组件实际调用时才读取，因此类加载无循环依赖风险。
 */
public abstract class StylePreset {

    protected StylePreset() { /* 仅允许内部子类与配置驱动预设实例化 */ }

    // ==================================================================
    // 颜色层：默认取 Theme 规范常量（Dark 外观 = 旧库默认，零回归）
    // ==================================================================

    public int accent()          { return Theme.ACCENT; }
    public int accentBright()    { return Theme.ACCENT_BRIGHT; }
    public int panelColor()      { return Theme.PANEL; }
    public int panelDim()        { return Theme.PANEL_DIM; }
    public int windowBg()        { return Theme.WINDOW_BG; }
    public int inputBg()         { return Theme.INPUT_BG; }
    public int panelSide()       { return Theme.PANEL_SIDE; }
    public int popupBg()         { return Theme.POPUP_BG; }
    public int popupBorder()     { return Theme.POPUP_BORDER; }
    public int track()           { return Theme.TRACK; }
    public int slotBg()          { return Theme.SLOT_BG; }
    public int slotBorder()      { return Theme.SLOT_BORDER; }
    public int hoverBg()         { return Theme.HOVER_BG; }
    public int activeBg()        { return Theme.ACTIVE_BG; }
    public int pressedBg()       { return Theme.PRESSED_BG; }
    public int disabledBg()      { return Theme.DISABLED_BG; }
    public int inputBorderHover() { return Theme.INPUT_BORDER_HOVER; }
    public int borderDisabled()  { return Theme.BORDER_DISABLED; }
    /** 窗口 / 面板常态强边框（钢蓝），比 panelColor 亮一档，轮廓更立体。 */
    public int borderStrong()    { return Theme.BORDER_STRONG; }
    public int handleBg()        { return Theme.HANDLE_BG; }
    public int handleHover()     { return Theme.HANDLE_HOVER; }
    public int titleBarBg()      { return Theme.TITLE_BAR_BG; }
    public int cornerDots()      { return Theme.CORNER_DOTS; }
    public int cornerTicks()     { return Theme.CORNER_TICKS; }
    public int optionHoverBg()   { return Theme.OPTION_HOVER_BG; }
    public int textHover()       { return Theme.TEXT_HOVER; }
    public int textDisabled()    { return Theme.TEXT_DISABLED; }
    public int text()            { return Theme.TEXT; }
    public int toggleTrackOff()  { return Theme.TOGGLE_TRACK_OFF; }
    public int toggleTrackOn()   { return Theme.TOGGLE_TRACK_ON; }
    public int toggleThumb()     { return Theme.TOGGLE_THUMB; }
    public int progressTrackColor() { return Theme.PROGRESS_TRACK; }
    public int progressFill()    { return Theme.PROGRESS_FILL; }
    public int tabBgColor()      { return Theme.TAB_BG; }
    public int tabActive()       { return Theme.TAB_ACTIVE; }
    public int tabBorder()       { return Theme.TAB_BORDER; }
    public int toastBg()         { return Theme.TOAST_BG; }
    public int toastBorder()     { return Theme.TOAST_BORDER; }
    // 这四个语义色刻意<b>不再</b>镜像 Theme.TOAST_*：Theme 那组值在 Dark 底上
    // Success/Error 亮度比只有 1.36，deuteranopia 下 ΔE 4.3 会坍缩成同一种土黄色
    // （可访问性 P0-1）。这里把亮度差拉到 2.31；Dark 与 Compact 同时受益，
    // HighContrast / Paper / Midnight 各自覆盖。
    public int toastInfo()       { return 0xFF6FD3F7; }
    public int toastSuccess()    { return 0xFF9AE89A; }
    public int toastWarn()       { return 0xFFF0B429; }
    public int toastError()      { return 0xFFE76060; }
    public int pagerBarBg()      { return Theme.PAGER_BAR_BG; }

    // ==================================================================
    // UI 独立色令牌（工作流 B3 迁移）
    // ------------------------------------------------------------------
    // 这些色既不是 {@link Theme} 规范常量、又在 UI 里被反复硬编码，换肤时它们
    // 「纹丝不动」正是预设覆盖不全的根因。这里给它们语义化名字与默认值：
    // 默认值 = 迁移前的裸字面量，因此 Dark 预设零视觉回归；其余预设覆盖后，
    // 这批组件才真正跟随换肤。
    // ==================================================================

    /** 纯白高亮文字（徽标 / 强调数值）。 */
    public int textBright()         { return 0xFFFFFFFF; }
    /** 全透明底：只想保留状态色（悬浮/选中）而不要常态底的按钮 / 列表行。 */
    public int surfaceTransparent() { return 0x00000000; }
    /** 模态遮罩（{@code WindowLayer} 的 scrim）；0 表示不画遮罩。 */
    public int scrim()              { return 0x80000000; }
    /** 次级文字（窗口关闭按钮常态等），明度介于 {@code text()} 与 {@code textDisabled()} 之间。 */
    public int textMuted()          { return 0xFFAAAAAA; }
    /** 危险 / 破坏性语义文字（关闭按钮悬浮、删除确认）。 */
    public int textDanger()         { return 0xFFFF6666; }

    /** 金色强调：天赋树 / 科技树的标题、页签、基点环、预览边框。 */
    public int goldAccent() { return 0xFFFFD24A; }
    /** 树界面右侧信息面板底（天赋树 / 科技树 / 研究树同一套外壳）。 */
    public int treePanelBg() { return 0xCC0A0E14; }
    /** 树界面右侧面板左分隔线。 */
    public int treePanelBorder() { return 0xFF2A3340; }
    /** 正文文字（比 text() 暗一档的常态正文 / tooltip 正文）。 */
    public int textBody() { return 0xFFCCCCCC; }
    /** 正向状态绿：已点亮 / 已完成 / 可解锁。 */
    public int statusOk() { return 0xFF6FE0A0; }
    /** 已点亮高亮黄：等级 / 花费 / 计数强调。 */
    public int statusAllocated() { return 0xFFFFE066; }

    // --- 天赋树（talent tree）：原本是 TalentTheme 里的静态常量 + 屏幕内散字面量，
    //     StylePreset 完全触达不到；整族默认值 = 迁移前的值，Dark 零回归 ---
    /** 整屏底。 */
    public int talentScreenBg() { return 0xFF0A0A0A; }
    /** 右侧详情面板底。 */
    public int talentPanelBg() { return treePanelBg(); }
    /** 右侧面板左分隔线。 */
    public int talentPanelBorder() { return treePanelBorder(); }
    /** 悬浮汇总卡底。 */
    public int talentSummaryBg() { return 0xEE0A0E14; }
    /** 通用控件凹槽底。 */
    public int talentControlTrackBg() { return 0xFF333333; }
    /** 滚动条轨道底。 */
    public int talentScrollTrackBg() { return 0xFF1A1F26; }
    /** 节点边框：锁定。 */
    public int talentBorderLocked() { return 0xFF706A62; }
    /** 节点边框：可点。 */
    public int talentBorderAvailable() { return 0xFF6FE06F; }
    /** 预览态（边框 / 连线 / 文字）。 */
    public int talentPreview() { return 0xFF66CCFF; }
    /** 连线暗态 + 次级描边。 */
    public int talentEdgeDim() { return 0xFF555555; }
    /** 连线亮态。 */
    public int talentEdgeLit() { return 0xFFC8A04A; }
    /** 珠宝辐射范围圈。 */
    public int talentJewelZone() { return 0x40C77DFF; }
    /** 被辐射节点外框。 */
    public int talentJewelLit() { return 0xFFC77DFF; }
    /** 珠宝插槽：空槽青框。 */
    public int talentSocketEmpty() { return 0xFF2A6E6E; }
    /** 珠宝插槽：已插金框。 */
    public int talentSocketFilled() { return 0xFFFFD700; }
    /** 武器组边框：全局（青）。 */
    public int talentBindingGlobal() { return 0xFF4AC8C8; }
    /** 武器组边框：I（红）。 */
    public int talentBinding1() { return 0xFFFF6B6B; }
    /** 武器组边框：II（绿）。 */
    public int talentBinding2() { return 0xFF6BE06B; }
    /** 武器组边框：其余（灰蓝）。 */
    public int talentBindingDefault() { return 0xFFA8B8C8; }
    /** 金色强调：标题 / 页签 / 基点环 / 搜索高亮。 */
    public int talentAccent() { return goldAccent(); }
    /** 金色强调（非激活档）。 */
    public int talentAccentDim() { return 0xFFE0C060; }
    /** notable 装饰环。 */
    public int talentKeystoneRingDim() { return 0x80C8C8C8; }
    /** 页签底：选中。 */
    public int talentTabActiveBg() { return 0xFF7A4A22; }
    /** 页签底：常态。 */
    public int talentTabBg() { return 0xFF2A4F70; }
    /** 页签 / 主按钮边框。 */
    public int talentTabBorder() { return 0xFF88CCFF; }
    /** 页签 / 按钮底：常态。 */
    public int talentTabIdleBg() { return 0xFF1A2E40; }
    /** 翻页箭头底：常态。 */
    public int talentArrowIdleBg() { return 0xFF223040; }
    /** 小按钮底：常态。 */
    public int talentControlBg() { return 0xFF1A2A38; }
    /** 小按钮底：悬浮。 */
    public int talentControlHoverBg() { return 0xFF334455; }
    /** 主按钮底：悬浮。 */
    public int talentPrimaryHoverBg() { return 0xFF336699; }
    /** 主按钮文字：悬浮。 */
    public int talentPrimaryTextHot() { return 0xFFAAEEFF; }
    /** 删除按钮底：常态。 */
    public int talentDangerBg() { return 0xFF401E1E; }
    /** 删除按钮底：悬浮。 */
    public int talentDangerHoverBg() { return 0xFF6E2A2A; }
    /** 删除按钮边框。 */
    public int talentDangerBorder() { return 0xFFAA4444; }
    /** 删除按钮文字。 */
    public int talentDangerText() { return 0xFFFF8888; }
    /** 文字：常态。 */
    public int talentTextIdle() { return textBody(); }
    /** 文字：次级。 */
    public int talentTextDim() { return 0xFF888888; }
    /** 文字：中性高亮。 */
    public int talentTextNeutral() { return 0xFFEEEEEE; }
    /** 文字：柔和绿。 */
    public int talentTextSoft() { return 0xFFCCFFCC; }
    /** 文字：点数。 */
    public int talentTextPoints() { return 0xFF88FF88; }
    /** 文字：汇总 / 滚动条滑块。 */
    public int talentTextSummary() { return 0xFF88FFAA; }
    /** 数字：激活档。 */
    public int talentNumActive() { return 0xFFF6F2E4; }
    public int talentPointsText() { return 0xFFF6F2E4; }
    /** 正向态绿（按钮边框 / 面板描边 / 滚动条）。 */
    public int talentOk() { return statusOk(); }
    /** 插槽底：未点亮。 */
    public int talentNodeBg() { return 0xCC101418; }
    /** 插槽底：已点亮。 */
    public int talentNodeBgAllocated() { return 0xCC1A2630; }

    public int talentBadgeText() { return textBright(); }

    /**
     * 节点类型填充对画布底的<b>最低对比度下限</b>（×100，如 {@code 300} = 3.0:1）。
     *
     * <p>类型色硬编码于 {@code TalentNodeTypeRegistry}、<b>数据包可覆盖</b>且
     * {@code lint_colors} 扫不到（它只解析 Theme / StylePreset）。极端类型色（近黑或近白）
     * 会让节点在画布上不可见，C4 的明暗差保证随之失效。画师按此下限就地抬升
     * 「已点亮填充」对画布底的对比度，作为抗数据包的安全网（UI-ACC-005 §5-Q3）。
     *
     * <p>注意：只钳制<b>已点亮</b>填充。未分配填充随后仍按 C4 乘 0.70 压暗，
     * 它的可见性由边框承担；若也钳制会反向削弱 C4 的明暗差。
     */
    public int talentFillSafeFloor() { return 300; }

    /** 键盘焦点环色。刻意与 {@code accent()} 的冷蓝<b>色相分离</b>成暖金：
     * accent 同时用于「鼠标悬浮边框」与「选中边框」，若焦点环也用 accent，
     * 键盘焦点与鼠标悬浮在视觉上完全无法区分（可访问性 P0-3）。
     */
    public int focusRing()          { return 0xFFFFD24A; }

    // --- 进度树（progression）：科技树 / 研究树。原本是 ProgressionTheme 静态常量 + 屏幕内散字面量，
    //     与天赋树刻意不同色系（天赋金橙战斗成长 / 研究青蓝基建投入），但共用同一套外壳 ---
    /** 整屏底（科技树 / 研究树一致）。 */
    public int progressionScreenBg() { return 0xFF070B0E; }
    /** 页签底：未选中常态。 */
    public int progressionTabBg() { return 0xFF141C24; }
    /** 页签底：未选中悬浮。 */
    public int progressionTabHoverBg() { return 0xFF24343F; }
    /** 页签边框：未选中。 */
    public int progressionTabBorder() { return 0xFF3A4652; }
    /** 页签文字：未选中。 */
    public int progressionTabText() { return 0xFFAABBCC; }
    /** 面板信息（经验 / 已完成数）。 */
    public int progressionInfoText() { return 0xFFAACCDD; }
    /** 底部操作提示。 */
    public int progressionHint() { return 0xFF556677; }
    /** 科技树页签底：选中。 */
    public int progressionTechAccentBg() { return 0xFF4A3E1E; }
    /** 科技树页签文字：选中。 */
    public int progressionTechAccentText() { return 0xFFFFF0C0; }
    /** 等级段分隔线：已达成。 */
    public int progressionSectionSepReached() { return 0xFF4A4230; }
    /** 等级段分隔线：未达成。 */
    public int progressionSectionSepLocked() { return 0xFF23272B; }
    /** 未达成段标题灰。 */
    public int progressionTextDim() { return 0xFF6A6A6A; }
    /** 科技树滚动条轨道。 */
    public int progressionScrollTrack() { return 0xFF161C22; }
    /** 单元格底：已解锁。 */
    public int progressionCellUnlockedBg() { return 0xFF16382E; }
    /** 单元格底：已解锁·悬浮。 */
    public int progressionCellUnlockedBgHover() { return 0xFF1E5040; }
    /** 单元格底：可解锁。 */
    public int progressionCellAvailableBg() { return 0xFF14303D; }
    /** 单元格底：可解锁·悬浮。 */
    public int progressionCellAvailableBgHover() { return 0xFF1F4A5E; }
    /** 单元格底：锁定。 */
    public int progressionCellLockedBg() { return 0xFF14181C; }
    /** 单元格边框：锁定。 */
    public int progressionCellLockedBorder() { return 0xFF33383D; }
    /** 单元格文字：锁定。 */
    public int progressionCellLockedText() { return 0xFF6A7078; }
    /** 单元格文字：可解锁。 */
    public int progressionCellAvailableText() { return 0xFFDDF4FF; }
    /** 花费 / 等级不足红。 */
    public int progressionCostShort() { return 0xFFAA5555; }
    /** 等级门槛：已达标（次级）。 */
    public int progressionLevelOkText() { return 0xFF778899; }
    /** tooltip 底。 */
    public int progressionTooltipBg() { return 0xE0101010; }
    /** tooltip 边框。 */
    public int progressionTooltipBorder() { return 0xFF808080; }
    /** 配额绿（区块 / 实体 / 站点上限）。 */
    public int progressionBudgetText() { return 0xFF9FD8A0; }
    /** 青色强调：科技点 / 进度条边框 / 可研究边框。 */
    public int progressionAccentCyan() { return 0xFF5FD8FF; }
    /** 研究树页签底：选中。 */
    public int progressionResearchAccentBg() { return 0xFF1E4A44; }
    /** 柔和青绿文字：页签选中 / 进度百分比 / 已解锁单元格。 */
    public int progressionTextSoft() { return 0xFFCCFFEE; }
    /** 研究台运行中。 */
    public int progressionBenchRunningText() { return 0xFF88CCFF; }
    /** 研究进度条轨道。 */
    public int progressionBarTrack() { return 0xFF1A2630; }
    /** 连线亮态 / 进度条填充。 */
    public int progressionEdgeLit() { return 0xFF4AC8A0; }
    /** 次级信息（剩余时间 / 空闲）。 */
    public int progressionTextSecondary() { return 0xFF99AABB; }
    /** 次级提示。 */
    public int progressionTextHint() { return 0xFF667788; }
    /** 无研究台告警橙。 */
    public int progressionBenchAbsent() { return 0xFFCC8855; }
    /** 数据包损坏红（前置缺失）。 */
    public int progressionDanger() { return 0xFFFF6B6B; }
    /** 节点边框：研究进行中（暗青）。 */
    public int progressionBorderAllocatedInactive() { return 0xFF2A7A88; }
    /** 连线暗态。 */
    public int progressionEdgeDim() { return 0xFF3F4A52; }
    /** 节点填充。 */
    public int progressionNodeFill() { return 0xFF1E3A46; }
    /** 节点填充：前置缺失。 */
    public int progressionNodeFillBroken() { return 0xFF5A1E1E; }

    /** 科技树主色（标题 / 页签 / 段标题 / 滚动条 / tooltip 标题）。转发已有令牌，不另抄一份字面量。 */
    public int progressionTechAccent() { return goldAccent(); }
    /** 右侧面板底。转发已有令牌，不另抄一份字面量。 */
    public int progressionPanelBg() { return treePanelBg(); }
    /** 右侧面板分隔线。转发已有令牌，不另抄一份字面量。 */
    public int progressionPanelBorder() { return treePanelBorder(); }
    /** tooltip 正文。转发已有令牌，不另抄一份字面量。 */
    public int progressionTextBody() { return textBody(); }
    /** 研究树主色（已完成 / 已解锁绿）。转发已有令牌，不另抄一份字面量。 */
    public int progressionResearchAccent() { return progressionAccentCyan(); }  // ③ 拆离 statusOk：主色独立为青蓝，契合「研究走青蓝」意图，不再与已完成色撞车
    /** 节点边框：已完成。转发已有令牌，不另抄一份字面量。 */
    public int progressionBorderAllocated() { return 0xFFE6A92E; }  // ② 跨树语义统一：已完成节点改金，消除与 talent 可点绿 ΔE0.9 撞色
    /** 等级数字。转发已有令牌，不另抄一份字面量。 */
    public int progressionLevelText() { return statusAllocated(); }
    /** 花费充足高亮。转发已有令牌，不另抄一份字面量。 */
    public int progressionCostReady() { return statusAllocated(); }
    /** 节点边框：可研究。转发已有令牌，不另抄一份字面量。 */
    public int progressionBorderAvailable() { return progressionAccentCyan(); }
    /** 研究进度条边框。转发已有令牌，不另抄一份字面量。 */
    public int progressionBarBorder() { return progressionAccentCyan(); }
    /** 科技点数字。转发已有令牌，不另抄一份字面量。 */
    public int progressionTechPointsText() { return progressionAccentCyan(); }
    /** 单元格文字：已解锁。转发已有令牌，不另抄一份字面量。 */
    public int progressionCellUnlockedText() { return progressionTextSoft(); }
    /** 研究树页签选中文字 / 进度百分比。转发已有令牌，不另抄一份字面量。 */
    public int progressionResearchAccentText() { return progressionTextSoft(); }
    /** 研究进度条填充。转发已有令牌，不另抄一份字面量。 */
    public int progressionBarFill() { return progressionEdgeLit(); }
    /** 节点边框：锁定（原 = Theme.PANEL）。转发已有令牌，不另抄一份字面量。 */
    public int progressionBorderLocked() { return panelColor(); }

    // --- 据点（stronghold）/ 通用屏幕收尾：B3 第二波的剩余散字面量 ---
    /** 优先级 / 威胁档 1（深红，符号 <<）。 */
    public int threatTier1() { return 0xFF8B0000; }
    /** 优先级 / 威胁档 2（橙，符号 <）。 */
    public int threatTier2() { return 0xFFCD5C00; }
    /** 优先级 / 威胁档 3（黄绿，符号 =）。 */
    public int threatTier3() { return 0xFF9ACD32; }
    /** 优先级 / 威胁档 4（绿，符号 >）。 */
    public int threatTier4() { return 0xFF32CD32; }
    /** 优先级 / 威胁档 5（深绿，符号 >>）。 */
    public int threatTier5() { return 0xFF006400; }
    /** 开关「开」绿 / 资源世界高亮绿。 */
    public int statusOn() { return 0xFF00FF00; }
    /** 开关「关」灰（比 statusOff() 更深一档）。 */
    public int statusOffDim() { return 0xFF9A9A9A; }
    /** 占位提示灰（空槽「+」/ 空列表提示）。 */
    public int placeholderText() { return 0xFF808080; }
    /** 库存滑块填充（半透明淡蓝）。 */
    public int stockSliderFill() { return 0x80ADD8E6; }
    /** 库存容量条：在途段（半透明红）。 */
    public int stockTransitBand() { return 0x80FF6666; }
    /** 库存容量条：已存段（半透明绿）。 */
    public int stockStoredBand() { return 0x8090EE90; }
    /** 库存数量行：标签灰。 */
    public int stockQtyLabel() { return 0xFFB0B0B0; }
    /** 面板槽位格子底色（原 AbstractPanelScreen.SLOT_FILL）。 */
    public int panelSlotFill() { return 0xFF1E2735; }
    /** 充能条底槽（原 ExtraSlotsScreen.CHARGE_BG）。 */
    public int chargeTrackBg() { return 0xFF2B2B2B; }
    /** 柔和阴影（tooltip 等小卡片；比 shadowColor() 更淡）。 */
    public int shadowSoft() { return 0x4D000000; }
    /** 开关「关」灰（= textMuted()）。转发已有令牌，不另抄一份字面量。 */
    public int statusOff() { return textMuted(); }
    /** 库存数量行：数值白（= textBright()）。转发已有令牌，不另抄一份字面量。 */
    public int stockQtyValue() { return textBright(); }

    // --- 图（graph）画笔：天赋树 / 科技树 / 研究树共用的默认节点与连线色 ---
    /** 节点描边（常态）。 */
    public int graphNodeBorder()          { return 0xFF6A6A6A; }
    /** 搜索命中（focused）时额外套的一圈。 */
    /** 悬浮时额外套的一圈。 */
    public int graphHoverRing()           { return 0xFFFFFFFF; }
    /** 节点填充（active = 已激活 / 已点亮的节点）。 */
    public int graphNodeActiveFill()      { return 0xFF2E5C8A; }
    /** 节点填充（disabled = 未解锁）。 */
    public int graphNodeDisabledFill()    { return 0xFF202020; }
    /** 节点描边（disabled = 未解锁）。 */
    public int graphNodeDisabledBorder()  { return 0xFF303030; }
    /** 连线（hovered）。 */
    public int graphEdgeHover()           { return 0xFF9AC8E8; }

    // --- 径向菜单（radial menu）---
    /** 径向菜单整屏底色（打开时压暗世界）。 */
    public int radialBackdrop()   { return 0x90000000; }
    /** 中心枢纽填充（有父级 = 「返回」态）。 */
    public int radialHubFill()    { return 0xFF3A4A5A; }
    /** 扇区描边（高亮 / 常态）。 */
    public int radialBorderHot()  { return 0xFFF2C94C; }
    public int radialBorderIdle() { return 0xFF6E7B8B; }
    /** 扇区填充（高亮 / 常态）。 */
    public int radialFillHot()    { return 0xFF2E3A48; }
    public int radialFillIdle()   { return 0xFF1C2530; }
    /** 扇区与枢纽文字（高亮 / 常态）。 */
    public int radialTextHot()    { return 0xFFF2C94C; }
    /**
     * 常态文字色。迁移前这里是 6 位字面量 {@code 0xCFCFCF}，按 ARGB int 解析即
     * {@code 0x00CFCFCF} —— alpha 为 0。同分支的高亮色写的是 {@code 0xFFF2C94C}，
     * 可见作者意图是「不透明白 / 不透明白灰」，只是漏写了 alpha 通道。
     * 这里补成 {@code 0xFFCFCFCF}：若底层渲染对 alpha=0 另有兜底，则视觉零变化；
     * 否则就是修复「文字完全不可见」。两种情况下都是正确值。
     */
    public int radialTextIdle()   { return 0xFFCFCFCF; }

    // --- 宝石（gem）业务界面：切割 / 装配 ---
    /** 宝石金：窗口边框与角点、技能键位文字 —— gem 界面唯一的「品牌色」。 */
    public int gemGold()           { return 0xFFC9A227; }
    /** gem 窗口底（切割界面）。 */
    public int gemWindowBg()       { return 0xE8141A26; }
    /** gem 面板底（装配界面，比窗口更透一档）。 */
    public int gemPanelBg()        { return 0xB8111826; }
    /** gem 卡片 / 视图底。 */
    public int gemViewBg()         { return 0xFF202B40; }
    /** gem 整屏渐变背景（上 / 下）。 */
    public int gemBackdropTop()    { return 0xEE0C1120; }
    public int gemBackdropBottom() { return 0xEE04060B; }
    /** gem 次级文字（等级等）。 */
    public int gemTextMuted()      { return 0xFF9FB4CC; }
    /** gem 正向语义文字（光环已开启等）。 */
    public int gemTextPositive()   { return 0xFF7CFF7C; }
    /** 「不可用」图标的去色遮罩。 */
    public int gemDesaturate()     { return 0xB08C8C8C; }

    /** gem 类别强调色：技能（红橙）。 */
    public int gemAccentSkill()    { return 0xFFE0573F; }
    /** gem 类别强调色：辅助（绿）。与 {@code statusOk} 刻意区分：
     * 那个是「成功/已完成」状态色，这是类别身份色，值也不同，不可复用。 */
    public int gemAccentSupport()  { return 0xFF3CC56E; }
    /** gem 类别强调色：精魂（紫）。 */
    public int gemAccentSpirit()   { return 0xFFA86FE0; }
    /** gem 类别强调色<b>兜底</b>：{@code null} 类别或任何未注册类别（蓝）。
     * GemCategory 是可扩展注册表，新类别注册后未加入映射即落到此兜底色。 */
    public int gemAccentFallback() { return 0xFF4FA3E0; }
    /** 表面明暗方向：{@code true} = 当前预设是<b>浅底</b>（如 Paper）。
     * 派生运算据此决定「提亮还是压暗」—— 无条件提亮会在浅底上洗出浅字（不可读）。
     * 非颜色令牌：ConfigStylePreset 走 boolean {@code flag(...)} 转发（配置写 1 / 0），
     * 而不是颜色的 int {@code tok(...)}。 */
    public boolean lightSurface()  { return false; }

    // ==================================================================
    // 令牌层：间距 / 圆角 / 字号 / 阴影 —— 布局的统一计量单位
    // ==================================================================


    public int radiusSm()   { return 2; }
    public int radiusMd()   { return 3; }


    /** 阴影令牌：偏移 / 模糊 / 颜色，{@code shadow()} 据此装配。 */
    public int shadowOffsetX() { return 2; }
    public int shadowOffsetY() { return 4; }
    public int shadowBlur()    { return 3; }
    public int shadowColor()   { return 0x66000000; }

    /** 扫光带颜色。 */
    public int shimmerColor()  { return 0x40FFFFFF; }

    // ==================================================================
    // 动画令牌组（附录 D.1）：六类触发时长 / 强度，皮肤可覆写
    // ------------------------------------------------------------------
    // 默认值对齐计划 D.1 表（全部 90–200ms、ease-out-cubic 为主）；
    // 组件一律经 {@code AnimRegistry} 读这里，不散写秒表。曲线仍用既有 {@code Easing} 枚举，
    // 本类只承载时长 / 强度等数据令牌。
    // ==================================================================

    /** 六类动画触发（与 {@code AnimRegistry.Trigger} 一一对应）。 */
    public enum AnimKind { MOUNT, UNMOUNT, HOVER, PRESSED, SELECTED, VALUE }

    /** 各类触发时长（ms）。 */
    public int animMs(AnimKind kind) {
        return switch (kind) {
            case MOUNT    -> 130;  // 淡入 + 上滑 8px（列表 stagger 20ms/项）
            case UNMOUNT  -> 90;   // 淡出 + 下滑 6px
            case HOVER    -> 120;  // 亮度 / 辉光渐变（tooltip 延迟淡入 150ms）
            case PRESSED  -> 120;  // 缩放 0.94 即时 + 回弹
            case SELECTED -> 160;  // 指示条滑动 / 卡片边框光晕
            case VALUE    -> 200;  // 数值滚动 / 进度流动
        };
    }

    /** mount 进场上滑距离（px）。 */
    public int animMountSlidePx()   { return 8; }
    /** unmount 退场下滑距离（px）。 */
    public int animUnmountSlidePx() { return 6; }
    /** pressed 按压缩放倍率（<1，回弹到 1）。 */
    public float animPressedScale()  { return 0.94F; }
    /** 窗口进场起始缩放（<1，open 时 0.98→1）。 */
    public float animOpenStartScale() { return 0.98F; }

    /**
     * 皮肤音效 pitch 偏移（附录 D.3，与音效特效 shard 的接口契约）。
     * 默认 1.0；各皮肤在 0.95–1.1 内取值（Dark/HighContrast/Compact 1.0、Paper 0.95、Midnight 1.1）。
     */
    public float sfxPitch() { return 1.0F; }

    // ==================================================================
    // 背景纹理令牌（附录 D.2）：每皮肤一档，程序化绘制（无贴图资源依赖）
    // ==================================================================

    /** 窗口 / 面板背景纹理风格。 */
    public enum BackgroundKind {
        /** 无装饰纹理（高对比 / 紧凑等中性档）。 */
        NONE,
        /** 蓝钢：细网格线 + 底部暗蓝辉光。 */
        BLUE_STEEL,
        /** 羊皮纸：纤维噪点 + 边缘渐暗。 */
        PARCHMENT,
        /** 霓虹：细扫描线 + 角部光晕。 */
        NEON,
        /** 绯红：织物斜纹 + 顶部暗红渐变。 */
        CRIMSON,
        /** 极光：竖向渐变带（青-紫-粉，低透明度）。 */
        AURORA
    }

    /** 本皮肤的窗口 / 面板背景纹理风格。默认蓝钢（= Dark）。 */
    public BackgroundKind backgroundKind() { return BackgroundKind.BLUE_STEEL; }

    /** 纹理描线色（网格 / 扫描线 / 斜纹），默认随 accent 派生为极低 alpha，避免喧宾夺主。 */
    public int backgroundLineColor() { return (accent() & 0x00FFFFFF) | 0x14000000; }

    /** 纹理辉光带色（蓝钢底部 / 绯红顶部渐变），默认随 accent 派生为低 alpha。 */
    public int backgroundGlowColor() { return (accent() & 0x00FFFFFF) | 0x28000000; }

    /** 极光竖向渐变带三色（青-紫-粉，低透明度）。 */
    public int[] backgroundBands() {
        return new int[] { 0x284FE0D0, 0x289A6FE0, 0x28E07AB0 };
    }

    /** 预设标识（持久化 / 日志用）。 */
    public String id() { return "dark"; }

    /** 预设展示名（设置界面用）。 */
    public String displayName() { return "Dark"; }

    // ==================================================================
    // 皮肤层：按当前令牌装配 ColorRenderer，组件直接叠到 visuals 最底层
    // ==================================================================

    public ColorRendererI button() {
        return buttonSkin(panelDim(), panelColor(), hoverBg(), activeBg(), pressedBg(), disabledBg(), borderDisabled(), accent());
    }

    public ColorRendererI window() {
        return windowSkin(windowBg(), borderStrong(), cornerDots());
    }

    public Renderer input() {
        return inputSkin(inputBg(), panelColor(), inputBorderHover());
    }

    public Renderer panel() {
        return panelSkin(panelSide(), panelColor());
    }

    public Renderer popup() {
        return popupSkin(popupBg(), popupBorder());
    }

    public Renderer slot() {
        return slotSkin(slotBg(), slotBorder());
    }

    public Renderer scrollTrack() {
        return scrollTrackSkin(track());
    }

    public Renderer scrollThumb() {
        return scrollThumbSkin(accent(), accentBright());
    }

    public Renderer pagerBar() {
        return pagerBarSkin(pagerBarBg());
    }

    public Renderer handle() {
        return handleSkin(handleBg(), handleHover());
    }

    public Renderer progressTrack() {
        return progressTrackSkin(progressTrackColor());
    }

    public Renderer tabBg() {
        return tabBgSkin(tabBgColor(), tabBorder(), hoverBg(), tabActive(), disabledBg(), accent(), borderDisabled());
    }

    // ==================================================================
    // 共享皮肤装配原语：base 与 ConfigStylePreset 共用，仅取色来源不同
    // （构造逻辑逐字节一致；radius* 经虚分派取色，base 走 Theme 默认、
    //  Config 走 tok 级联）
    // ==================================================================

    protected ColorRendererI buttonSkin(int panelDim, int panelColor, int hoverBg, int activeBg,
            int pressedBg, int disabledBg, int borderDisabled, int accent) {
        return Renderers.color(panelDim, panelColor)
                .backgroundHover(hoverBg)
                .backgroundActive(activeBg)
                .backgroundPressed(pressedBg)
                .backgroundDisabled(disabledBg)
                .borderHover(accent)
                .borderActive(accent)
                .borderDisabled(borderDisabled)
                .hoverGradient(0x40264A72)
                .pressedGradient(0x2A000000)
                .borderStyle(BorderStyle.FULL)
                .rounding(radiusSm())
                .smooth(true);
    }

    protected ColorRendererI windowSkin(int windowBg, int border, int cornerDots) {
        return Renderers.color(windowBg, border)
                .borderStyle(BorderStyle.FULL)
                .rounding(radiusMd())
                .cornerDots(cornerDots);
    }

    protected ColorRendererI inputSkin(int inputBg, int panelColor, int inputBorderHover) {
        return Renderers.color(inputBg, panelColor)
                .borderHover(inputBorderHover)
                .borderStyle(BorderStyle.FULL)
                .rounding(radiusSm());
    }

    protected ColorRendererI panelSkin(int panelSide, int panelColor) {
        return Renderers.color(panelSide, panelColor)
                .borderStyle(BorderStyle.FULL);
    }

    protected ColorRendererI popupSkin(int popupBg, int popupBorder) {
        return Renderers.color(popupBg, popupBorder)
                .borderStyle(BorderStyle.FULL)
                .rounding(radiusMd());
    }

    protected ColorRendererI slotSkin(int slotBg, int slotBorder) {
        return Renderers.color(slotBg, slotBorder)
                .borderStyle(BorderStyle.FULL)
                .rounding(radiusSm());
    }

    protected ColorRendererI scrollTrackSkin(int track) {
        return Renderers.color(track);
    }

    protected ColorRendererI scrollThumbSkin(int accent, int accentBright) {
        return Renderers.color(accent)
                .backgroundHover(accentBright)
                .rounding(radiusSm());
    }

    protected ColorRendererI pagerBarSkin(int pagerBarBg) {
        return Renderers.color(pagerBarBg)
                .borderStyle(BorderStyle.FULL)
                .rounding(radiusSm());
    }

    protected ColorRendererI handleSkin(int handleBg, int handleHover) {
        return Renderers.color(handleBg).backgroundHover(handleHover);
    }

    protected ColorRendererI progressTrackSkin(int progressTrackColor) {
        return Renderers.color(progressTrackColor);
    }

    protected ColorRendererI tabBgSkin(int tabBgColor, int tabBorder, int hoverBg, int tabActive,
            int disabledBg, int accent, int borderDisabled) {
        return Renderers.color(tabBgColor, tabBorder)
                .backgroundHover(hoverBg)
                .backgroundActive(tabActive)
                .backgroundDisabled(disabledBg)
                .borderHover(accent)
                .borderActive(accent)
                .borderDisabled(borderDisabled)
                .hoverGradient(0x35264A72)
                .borderStyle(BorderStyle.FULL)
                .rounding(radiusSm())
                .smooth(true);
    }

    // ==================================================================
    // 动态效果层：直接挂到节点 visuals 上
    // ==================================================================

    public ShadowRendererI shadow() {
        return Renderers.shadow(shadowOffsetX(), shadowOffsetY(), shadowBlur(), shadowColor());
    }

    public GlowBorderRendererI glow() {
        return Renderers.glow(accent());
    }

    public ShimmerRendererI shimmer() {
        return Renderers.shimmer(shimmerColor());
    }

    /**
     * 窗口 / 面板背景纹理叠加层（D.2）。按当前皮肤 {@link #backgroundKind()} 程序化手绘，
     * 无贴图资源依赖；取色渲染时读 {@link Theme#current()}，换肤即时跟随。
     * 默认档 {@link BackgroundKind#BLUE_STEEL} 对 Dark 为极淡网格，视觉回归可忽略。
     */
    public Renderer backgroundTexture() {
        return new BackgroundTextureRenderer();
    }

    // ==================================================================
    // 内置预设
    // ==================================================================

    private static final StylePreset DARK          = new Dark();
    private static final StylePreset HIGH_CONTRAST = new HighContrast();
    private static final StylePreset COMPACT       = new Compact();
    private static final StylePreset PAPER         = new Paper();
    private static final StylePreset MIDNIGHT      = new Midnight();

    public static StylePreset dark()          { return DARK; }
    public static StylePreset highContrast()  { return HIGH_CONTRAST; }
    public static StylePreset compact()       { return COMPACT; }
    public static StylePreset paper()         { return PAPER; }
    public static StylePreset midnight()      { return MIDNIGHT; }

    /** 按 id 取预设；未知 id 回退 Dark。 */
    public static StylePreset fromId(String id) {
        if (id == null) return DARK;
        return switch (id) {
            case "high_contrast" -> HIGH_CONTRAST;
            case "compact"       -> COMPACT;
            case "paper"         -> PAPER;
            case "midnight"      -> MIDNIGHT;
            default              -> DARK;
        };
    }

    /** Dark：与重构前默认外观完全一致，作为零回归基准。 */
    public static final class Dark extends StylePreset {}

    /** HighContrast：提亮强调色与正文、加粗边框与阴影，服务于弱光 / 弱视场景。 */
    public static final class HighContrast extends StylePreset {
        @Override public int accent()         { return 0xFF8FD0FF; }
        @Override public int accentBright()   { return 0xFFBFE4F5; }
        @Override public int text()           { return 0xFFFFFFFF; }
        @Override public int textDisabled()   { return 0xFFAAAAAA; }
        @Override public int textHover()      { return 0xFFFFFFDD; }
        @Override public int goldAccent() { return 0xFFFFDE79; }
        @Override public int treePanelBg() { return 0xCC090C12; }
        @Override public int treePanelBorder() { return 0xFF717B88; }
        @Override public int textBody() { return 0xFFDBDBDB; }
        @Override public int statusOk() { return 0xFF94E8B9; }
        @Override public int statusAllocated() { return 0xFFFFE88E; }
        @Override public int progressionScreenBg() { return 0xFF070A0D; }
        @Override public int progressionTabBg() { return 0xFF11171E; }
        @Override public int progressionTabHoverBg() { return 0xFF1D2A33; }
        @Override public int progressionTabBorder() { return 0xFF7A8692; }
        @Override public int progressionTabText() { return 0xFFC4CFDB; }
        @Override public int progressionInfoText() { return 0xFFC4DBE7; }
        @Override public int progressionHint() { return 0xFF8894A0; }
        @Override public int progressionTechAccentBg() { return 0xFF3B321A; }
        @Override public int progressionTechAccentText() { return 0xFFFFF4D3; }
        @Override public int progressionSectionSepReached() { return 0xFF83847F; }
        @Override public int progressionSectionSepLocked() { return 0xFF6D757C; }
        @Override public int progressionTextDim() { return 0xFF979797; }
        @Override public int progressionScrollTrack() { return 0xFF12171D; }
        @Override public int progressionCellUnlockedBg() { return 0xFF122D26; }
        @Override public int progressionCellUnlockedBgHover() { return 0xFF184034; }
        @Override public int progressionCellAvailableBg() { return 0xFF112732; }
        @Override public int progressionCellAvailableBgHover() { return 0xFF193B4C; }
        @Override public int progressionCellLockedBg() { return 0xFF111418; }
        @Override public int progressionCellLockedBorder() { return 0xFF767E86; }
        @Override public int progressionCellLockedText() { return 0xFF979BA0; }
        @Override public int progressionCellAvailableText() { return 0xFFE7F7FF; }
        @Override public int progressionCostShort() { return 0xFFC08181; }
        @Override public int progressionLevelOkText() { return 0xFFA0ACB8; }
        @Override public int progressionTooltipBg() { return 0xE00E0E0F; }
        @Override public int progressionTooltipBorder() { return 0xFFA0A6AB; }
        @Override public int progressionBudgetText() { return 0xFFBCE4BC; }
        @Override public int progressionAccentCyan() { return 0xFF89E2FF; }
        @Override public int progressionResearchAccentBg() { return 0xFF183B37; }
        @Override public int progressionTextSoft() { return 0xFFDBFFF3; }
        @Override public int progressionBenchRunningText() { return 0xFFACDBFF; }
        @Override public int progressionBarTrack() { return 0xFF151F28; }
        @Override public int progressionEdgeLit() { return 0xFF79D6B9; }
        @Override public int progressionTextSecondary() { return 0xFFB8C4CF; }
        @Override public int progressionTextHint() { return 0xFF94A0AC; }
        @Override public int progressionBenchAbsent() { return 0xFFD9A781; }
        @Override public int progressionDanger() { return 0xFFFF9191; }
        @Override public int progressionBorderAllocatedInactive() { return 0xFF71A2B0; }
        @Override public int progressionEdgeDim() { return 0xFF7D8892; }
        @Override public int progressionNodeFill() { return 0xFF182F39; }
        @Override public int progressionNodeFillBroken() { return 0xFF47191A; }
        @Override public int threatTier1() { return 0xFFA94242; }
        @Override public int threatTier2() { return 0xFFDA8642; }
        @Override public int threatTier3() { return 0xFFB4DA67; }
        @Override public int threatTier4() { return 0xFF67DA67; }
        @Override public int threatTier5() { return 0xFF428C42; }
        @Override public int statusOn() { return 0xFF42FF42; }
        @Override public int statusOffDim() { return 0xFFB8B8B8; }
        @Override public int placeholderText() { return 0xFFA6A6A6; }
        @Override public int stockSliderFill() { return 0x80C2E2EC; }
        @Override public int stockTransitBand() { return 0x80FF8E8E; }
        @Override public int stockStoredBand() { return 0x80ADF2AD; }
        @Override public int stockQtyLabel() { return 0xFFC8C8C8; }
        @Override public int panelSlotFill() { return 0xFF18202C; }
        @Override public int chargeTrackBg() { return 0xFF232324; }
        @Override public int shadowSoft() { return 0x4D000000; }
        @Override public int panelColor()      { return 0xFF404040; }
        @Override public int windowBg()       { return 0xFF242424; }
        @Override public int borderDisabled() { return 0xFF555555; }
        @Override public int cornerTicks()    { return 0xFF8FD0FF; }
        @Override public int optionHoverBg()  { return 0x80FFFFFF; }
        @Override public int tabActive()      { return 0xFF2A5A7A; }
        @Override public int progressFill()   { return 0xFF8FD0FF; }
        @Override public int toastInfo()      { return 0xFF8FD0FF; }
        @Override public int toastSuccess()   { return 0xFF7BD07B; }
        @Override public int toastWarn()      { return 0xFFE8B45A; }
        @Override public int toastError()     { return 0xFFE87878; }
        @Override public int shadowColor()    { return 0x90000000; }
        @Override public int shadowOffsetY()  { return 5; }
        @Override public int gemGold()           { return 0xFFFFD54A; }
        @Override public int gemWindowBg()       { return 0xEE1E2436; }
        @Override public int gemPanelBg()        { return 0xC01E2436; }
        @Override public int gemViewBg()         { return 0xFF2C3A52; }
        @Override public int gemBackdropTop()    { return 0xEE141C2C; }
        @Override public int gemBackdropBottom() { return 0xEE080A12; }
        @Override public int gemTextMuted()      { return 0xFFC3D2E2; }
        @Override public int gemTextPositive()   { return 0xFF9EFF9E; }
        @Override public int gemDesaturate()     { return 0xB0A8A8A8; }
        @Override public int gemAccentSkill()    { return 0xFFE88270; }
        @Override public int gemAccentSupport()  { return 0xFF6ED493; }
        @Override public int gemAccentSpirit()   { return 0xFFBE94E8; }
        @Override public int gemAccentFallback() { return 0xFF7CBAE8; }
        @Override public int radialBackdrop()   { return 0xA8000000; }
        @Override public int radialHubFill()    { return 0xFF4C5F72; }
        @Override public int radialBorderHot()  { return 0xFFFFDC5C; }
        @Override public int radialBorderIdle() { return 0xFF8A99AC; }
        @Override public int radialFillHot()    { return 0xFF3D4E60; }
        @Override public int radialFillIdle()   { return 0xFF26313E; }
        @Override public int radialTextHot()    { return 0xFFFFDC5C; }
        @Override public int graphNodeBorder()         { return 0xFF9A9A9A; }
        @Override public int graphNodeActiveFill()     { return 0xFF3A76AE; }
        @Override public int graphNodeDisabledFill()   { return 0xFF2C2C2C; }
        @Override public int graphNodeDisabledBorder() { return 0xFF404040; }
        @Override public int graphEdgeHover()          { return 0xFFB8DFF5; }
        @Override public int radialTextIdle() { return 0xFFDCDCDC; }
        @Override public int talentScreenBg() { return 0xFF09090A; }
        @Override public int talentSummaryBg() { return 0xEE090C12; }
        @Override public int talentControlTrackBg() { return 0xFF29292A; }
        @Override public int talentScrollTrackBg() { return 0xFF151A20; }
        @Override public int talentBorderLocked() { return 0xFF7D8388; }
        @Override public int talentBadgeText() { return 0xFFF2F2F2; }
        /** 高对比预设收紧到 4.0:1（当前 7 个内置类型色实测 4.25–13+，均不触发调整）。 */
        @Override public int talentFillSafeFloor() { return 400; }
        @Override public int talentBorderAvailable() { return 0xFF94E894; }
        @Override public int talentPreview() { return 0xFF8ED9FF; }
        @Override public int talentEdgeDim() { return 0xFF898E94; }
        @Override public int talentEdgeLit() { return 0xFFD6B979; }
        @Override public int talentJewelZone() { return 0x40D69FFF; }
        @Override public int talentJewelLit() { return 0xFFD69FFF; }
        @Override public int talentSocketEmpty() { return 0xFF619494; }
        @Override public int talentSocketFilled() { return 0xFFFFE142; }
        @Override public int talentBindingGlobal() { return 0xFF79D6D6; }
        @Override public int talentBinding1() { return 0xFFFF9191; }
        @Override public int talentBinding2() { return 0xFF91E891; }
        @Override public int talentBindingDefault() { return 0xFFBFCAD6; }
        @Override public int talentAccentDim() { return 0xFFE8D089; }
        @Override public int talentKeystoneRingDim() { return 0x80C8CDD3; }
        @Override public int talentTabActiveBg() { return 0xFF8A4C1B; }
        @Override public int talentTabBg() { return 0xFF223F5A; }
        @Override public int talentTabBorder() { return 0xFFA7D9FF; }
        @Override public int talentTabIdleBg() { return 0xFF152534; }
        @Override public int talentArrowIdleBg() { return 0xFF1C2734; }
        @Override public int talentControlBg() { return 0xFF15222E; }
        @Override public int talentControlHoverBg() { return 0xFF293744; }
        @Override public int talentPrimaryHoverBg() { return 0xFF29517A; }
        @Override public int talentPrimaryTextHot() { return 0xFFC4F3FF; }
        @Override public int talentDangerBg() { return 0xFF33191A; }
        @Override public int talentDangerHoverBg() { return 0xFF572223; }
        @Override public int talentDangerBorder() { return 0xFFC07575; }
        @Override public int talentDangerText() { return 0xFFFFA7A7; }
        @Override public int talentTextDim() { return 0xFFACACAC; }
        @Override public int talentTextNeutral() { return 0xFFF3F3F3; }
        @Override public int talentTextSoft() { return 0xFFDBFFDB; }
        @Override public int talentTextPoints() { return 0xFFACFFAC; }
        @Override public int talentTextSummary() { return 0xFFACFFC4; }
        @Override public int talentNumActive() { return 0xFFFF7575; }
        @Override public int talentPointsText() { return 0xFFF6F2E4; }
        @Override public int talentNodeBg() { return 0xCC0E1115; }
        @Override public int talentNodeBgAllocated() { return 0xCC151F28; }
        @Override public int focusRing()       { return 0xFFFFF07A; }
        @Override public int scrim()          { return 0xA5000000; }
        @Override public int textMuted()      { return 0xFFDDDDDD; }
        @Override public int textDanger()     { return 0xFFFF8A8A; }
        /** 高对比档不开装饰纹理：任何叠加纹理都会侵蚀对比度，退化为中性无纹理。 */
        @Override public BackgroundKind backgroundKind() { return BackgroundKind.NONE; }
        @Override public String id()          { return "high_contrast"; }
        @Override public String displayName() { return "High Contrast"; }
    }

    /** Compact：同等配色，但更紧的间距与更小的圆角，屏幕里塞下更多内容。 */
    public static final class Compact extends StylePreset {
        @Override public int radiusSm()   { return 1; }
        @Override public int radiusMd()   { return 2; }
        /** 紧凑档追求高密度，纹理留白会挤内容，退化为中性无纹理。 */
        @Override public BackgroundKind backgroundKind() { return BackgroundKind.NONE; }
        @Override public String id()      { return "compact"; }
        @Override public String displayName() { return "Compact"; }
    }

    /** Paper：浅色预设，亮面背景 + 深字 + 蓝强调，作为高对比浅色环境的零回归备选。所有色值均与 {@link Theme} 常量不冲突（lint_colors 0 回归）。 */
    public static final class Paper extends StylePreset {
        @Override public int accent()         { return 0xFF1565C0; }
        @Override public int accentBright()   { return 0xFF5E9BD6; }
        @Override public int goldAccent() { return 0xFFAC913C; }
        @Override public int treePanelBg() { return 0xCCD8DBDE; }
        @Override public int treePanelBorder() { return 0xFF303B49; }
        @Override public int textBody() { return 0xFF2F3841; }
        @Override public int statusOk() { return 0xFF4D9B74; }
        @Override public int statusAllocated() { return 0xFFAC9B4E; }
        @Override public int progressionScreenBg() { return 0xFFD8DBDE; }
        @Override public int progressionTabBg() { return 0xFFD9DDE0; }
        @Override public int progressionTabHoverBg() { return 0xFFDBE0E4; }
        @Override public int progressionTabBorder() { return 0xFF364250; }
        @Override public int progressionTabText() { return 0xFF2B3641; }
        @Override public int progressionInfoText() { return 0xFF2B3843; }
        @Override public int progressionHint() { return 0xFF1F2A35; }
        @Override public int progressionTechAccentBg() { return 0xFFE0E1E0; }
        @Override public int progressionTechAccentText() { return 0xFF373D3F; }
        @Override public int progressionSectionSepReached() { return 0xFF3C4143; }
        @Override public int progressionSectionSepLocked() { return 0xFF2D3641; }
        @Override public int progressionTextDim() { return 0xFF222A33; }
        @Override public int progressionScrollTrack() { return 0xFFD9DDE0; }
        @Override public int progressionCellUnlockedBg() { return 0xFFD9E0E2; }
        @Override public int progressionCellUnlockedBgHover() { return 0xFFDAE3E4; }
        @Override public int progressionCellAvailableBg() { return 0xFFD9DFE3; }
        @Override public int progressionCellAvailableBgHover() { return 0xFFDAE2E7; }
        @Override public int progressionCellLockedBg() { return 0xFFD9DCDF; }
        @Override public int progressionCellLockedBorder() { return 0xFF333D48; }
        @Override public int progressionCellLockedText() { return 0xFF222B35; }
        @Override public int progressionCellAvailableText() { return 0xFF323E48; }
        @Override public int progressionCostShort() { return 0xFF743F43; }
        @Override public int progressionLevelOkText() { return 0xFF242F3A; }
        @Override public int progressionTooltipBg() { return 0xE0D9DBDE; }
        @Override public int progressionTooltipBorder() { return 0xFF505861; }
        @Override public int progressionBudgetText() { return 0xFF293A3B; }
        @Override public int progressionAccentCyan() { return 0xFF4295B3; }
        @Override public int progressionResearchAccentBg() { return 0xFFDAE2E4; }
        @Override public int progressionTextSoft() { return 0xFF2F3F45; }
        @Override public int progressionBenchRunningText() { return 0xFF263848; }
        @Override public int progressionBarTrack() { return 0xFFDADEE2; }
        @Override public int progressionEdgeLit() { return 0xFF348B74; }
        @Override public int progressionTextSecondary() { return 0xFF28333E; }
        @Override public int progressionTextHint() { return 0xFF212C37; }
        @Override public int progressionBenchAbsent() { return 0xFF8A6143; }
        @Override public int progressionDanger() { return 0xFFAC4D52; }
        @Override public int progressionBorderAllocatedInactive() { return 0xFF305664; }
        @Override public int progressionEdgeDim() { return 0xFF384450; }
        @Override public int progressionNodeFill() { return 0xFFDAE0E4; }
        @Override public int progressionNodeFillBroken() { return 0xFFE2DDE0; }
        @Override public int threatTier1() { return 0xFF5F070B; }
        @Override public int threatTier2() { return 0xFF8B440B; }
        @Override public int threatTier3() { return 0xFF698E2C; }
        @Override public int threatTier4() { return 0xFF248E2C; }
        @Override public int threatTier5() { return 0xFF03490B; }
        @Override public int statusOn() { return 0xFF03AF0B; }
        @Override public int statusOffDim() { return 0xFF28313A; }
        @Override public int placeholderText() { return 0xFF252D36; }
        @Override public int stockSliderFill() { return 0x807695A3; }
        @Override public int stockTransitBand() { return 0x80AC4A4E; }
        @Override public int stockStoredBand() { return 0x8062A46A; }
        @Override public int stockQtyLabel() { return 0xFF2C343D; }
        @Override public int panelSlotFill() { return 0xFFDADEE2; }
        @Override public int chargeTrackBg() { return 0xFFDCDFE1; }
        @Override public int shadowSoft() { return 0x4D000000; }
        @Override public int panelColor()     { return 0xFFF5F5F5; }
        @Override public int panelDim()       { return 0xFFECECEC; }
        @Override public int windowBg()       { return 0xFFFFFFFF; }
        @Override public int inputBg()        { return 0xFFFAFAFA; }
        @Override public int panelSide()      { return 0xFFE0E0E0; }
        @Override public int popupBg()        { return 0xFFFBFBFB; }
        @Override public int popupBorder()    { return 0xFFBDBDBD; }
        @Override public int track()          { return 0xFFD6D6D6; }
        @Override public int slotBg()         { return 0xFFF0F0F0; }
        @Override public int slotBorder()     { return 0xFFC8C8C8; }
        @Override public int hoverBg()        { return 0xFFE3F2FD; }
        @Override public int activeBg()       { return 0xFFBBDEFB; }
        @Override public int pressedBg()      { return 0xFF90CAF9; }
        @Override public int disabledBg()     { return 0xFFEEEEEE; }
        @Override public int inputBorderHover(){ return 0xFF90CAF9; }
        @Override public int borderDisabled() { return 0xFFCCCCCC; }
        @Override public int handleBg()       { return 0xFFB0B0B0; }
        @Override public int handleHover()    { return 0xFF90CAF9; }
        @Override public int titleBarBg()     { return 0xFFECEFF1; }
        @Override public int cornerDots()     { return 0xFF1565C0; }
        @Override public int cornerTicks()    { return 0x801565C0; }
        @Override public int optionHoverBg()  { return 0x1A000000; }
        @Override public int textHover()      { return 0xFF0D47A1; }
        @Override public int textDisabled()   { return 0xFF9E9E9E; }
        @Override public int text()           { return 0xFF212121; }
        @Override public int pagerBarBg()     { return 0xFFE0E0E0; }
        @Override public int toggleTrackOff() { return 0xFFBDBDBD; }
        @Override public int toggleTrackOn()  { return 0xFF90CAF9; }
        @Override public int toggleThumb()    { return 0xFFFFFFFF; }
        @Override public int progressTrackColor() { return 0xFFD6D6D6; }
        @Override public int progressFill()   { return 0xFF1565C0; }
        @Override public int tabBgColor()     { return 0xFFECECEC; }
        @Override public int tabActive()      { return 0xFFBBDEFB; }
        @Override public int tabBorder()      { return 0xFFBDBDBD; }
        // Paper 是浅色预设：toast 底必须跟着变浅，否则 text()=0xFF212121 近黑正文
        // 压在 0xF0121212 深底上只有 1.26:1，完全不可读（可访问性 P0-2）
        @Override public int toastBg()        { return 0xFFF7F7F7; }
        @Override public int toastBorder()    { return 0xFF8F8F8F; }
        // 浅底上语义色改用深色调，保证与 0xFFF7F7F7 底仍有足够对比
        @Override public int toastInfo()      { return 0xFF0277BD; }
        @Override public int toastSuccess()   { return 0xFF1B7A32; }
        @Override public int toastWarn()      { return 0xFF8A5A00; }
        @Override public int toastError()     { return 0xFF7F1410; }
        @Override public int shimmerColor()   { return 0x33000000; }
        @Override public int shadowColor()    { return 0x30000000; }
        @Override public int shadowOffsetY()  { return 2; }
        @Override public int gemGold()           { return 0xFFF9A825; }
        @Override public int gemWindowBg()       { return 0xF2FAFAFA; }
        @Override public int gemPanelBg()        { return 0xE8F5F5F5; }
        @Override public int gemViewBg()         { return 0xFFECEFF1; }
        @Override public int gemBackdropTop()    { return 0xE8ECEFF1; }
        @Override public int gemBackdropBottom() { return 0xE8DCE1E8; }
        @Override public int gemTextMuted()      { return 0xFF546E7A; }
        @Override public int gemTextPositive()   { return 0xFF2E7D32; }
        @Override public int gemDesaturate()     { return 0xB0B0B0B0; }
        @Override public int gemAccentSkill()    { return 0xFF9C3C2C; }
        @Override public int gemAccentSupport()  { return 0xFF2A894D; }
        @Override public int gemAccentSpirit()   { return 0xFF754D9C; }
        @Override public int gemAccentFallback() { return 0xFF37729C; }
        @Override public boolean lightSurface()  { return true; }
        @Override public int radialBackdrop()   { return 0x35000000; }
        @Override public int radialHubFill()    { return 0xFFCFD8DC; }
        @Override public int radialBorderHot()  { return 0xFFF9A825; }
        @Override public int radialBorderIdle() { return 0xFF90A4AE; }
        @Override public int radialFillHot()    { return 0xFFE3F2FD; }
        @Override public int radialFillIdle()   { return 0xFFECEFF1; }
        @Override public int radialTextHot()    { return 0xFFF57F17; }
        @Override public int graphNodeBorder()         { return 0xFFB0B0B0; }
        @Override public int graphHoverRing()          { return 0xFF455A64; }
        @Override public int graphNodeActiveFill()     { return 0xFF90CAF9; }
        @Override public int graphNodeDisabledFill()   { return 0xFFE4E4E4; }
        @Override public int graphNodeDisabledBorder() { return 0xFFC0C0C0; }
        @Override public int graphEdgeHover()          { return 0xFF42A5F5; }
        @Override public int radialTextIdle() { return 0xFFDCDCDC; }
        @Override public int talentScreenBg() { return 0xFFD8DBDD; }
        @Override public int talentSummaryBg() { return 0xEED8DBDE; }
        @Override public int talentControlTrackBg() { return 0xFFDDDFE2; }
        @Override public int talentScrollTrackBg() { return 0xFFDADDE1; }
        @Override public int talentBorderLocked() { return 0xFF384049; }
        @Override public int talentBorderAvailable() { return 0xFF384049; }
        @Override public int talentBadgeText() { return 0xFF1A1A1A; }
        /**
         * Paper 特例：画布底 #D8DBDD（L=0.705）极浅，若也按 3.0:1 钳制，
         * 7 个内置类型色里有 5 个会被大幅压暗（jewel 青实测仅 1.17:1、notable 金 1.67:1），
         * 类型配色身份会被抹掉 —— 与「Paper 颜色不承载语义、由形状线索区分」的策略冲突。
         * Paper 上节点可见性由<b>单墨边框</b>（#384049，对浅底 7.56:1）承担，
         * 故此处只保留 1.0:1，仅兜住「填充与画布几乎同色」的病态情况。
         */
        @Override public int talentFillSafeFloor() { return 100; }
        @Override public int talentPreview() { return 0xFF384049; }
        @Override public int talentEdgeDim() { return 0xFF404851; }
        @Override public int talentEdgeLit() { return 0xFF87703C; }
        @Override public int talentJewelZone() { return 0x408759B3; }
        @Override public int talentJewelLit() { return 0xFF384049; }
        @Override public int talentSocketEmpty() { return 0xFF1F4F53; }
        @Override public int talentSocketFilled() { return 0xFFAC950B; }
        @Override public int talentBindingGlobal() { return 0xFF384049; }
        @Override public int talentBinding1() { return 0xFF384049; }
        @Override public int talentBinding2() { return 0xFF384049; }
        @Override public int talentBindingDefault() { return 0xFF384049; }
        @Override public int talentAccentDim() { return 0xFF97864A; }
        @Override public int talentKeystoneRingDim() { return 0x806C747C; }
        @Override public int talentTabActiveBg() { return 0xFFECE5E0; }
        @Override public int talentTabBg() { return 0xFFDCE3E9; }
        @Override public int talentTabBorder() { return 0xFF5D8DB3; }
        @Override public int talentTabIdleBg() { return 0xFFDADFE4; }
        @Override public int talentArrowIdleBg() { return 0xFFDBDFE4; }
        @Override public int talentControlBg() { return 0xFFDADEE3; }
        @Override public int talentControlHoverBg() { return 0xFFDDE2E6; }
        @Override public int talentPrimaryHoverBg() { return 0xFFDDE6EE; }
        @Override public int talentPrimaryTextHot() { return 0xFF2B3D48; }
        @Override public int talentDangerBg() { return 0xFFDEDDE0; }
        @Override public int talentDangerHoverBg() { return 0xFFE4DEE1; }
        @Override public int talentDangerBorder() { return 0xFF743438; }
        @Override public int talentDangerText() { return 0xFFAC6165; }
        @Override public int talentTextDim() { return 0xFF262F37; }
        @Override public int talentTextNeutral() { return 0xFF343D45; }
        @Override public int talentTextSoft() { return 0xFF2F3F41; }
        @Override public int talentTextPoints() { return 0xFF263F37; }
        @Override public int talentTextSummary() { return 0xFF263F3C; }
        @Override public int talentNumActive() { return 0xFFAC3438; }
        @Override public int talentPointsText() { return 0xFF4A3F14; }
        @Override public int talentNodeBg() { return 0xCCD9DCDF; }
        @Override public int talentNodeBgAllocated() { return 0xCCDADEE2; }
        @Override public int focusRing()       { return 0xFF0A3D91; }
        @Override public int scrim()          { return 0x38000000; }
        @Override public int textMuted()      { return 0xFF757575; }
        @Override public int textDanger()     { return 0xFFC62828; }
        /** Paper = 浅色羊皮纸：纤维噪点 + 边缘渐暗。 */
        @Override public BackgroundKind backgroundKind() { return BackgroundKind.PARCHMENT; }
        /** 羊皮纸档音效偏沉稳（低半档）。 */
        @Override public float sfxPitch()      { return 0.95F; }
        @Override public String id()          { return "paper"; }
        @Override public String displayName() { return "Paper"; }
    }

    /**
     * Midnight：深蓝调暗色变体。与 Dark 同结构、同令牌，仅把所有色值整体往「深夜蓝」偏移，
     * 作为暗色环境下的另一种协调风格。所有色值均与 {@link Theme} 常量不冲突（lint_colors 0 回归）。
     */
    public static final class Midnight extends StylePreset {
        @Override public int accent()         { return 0xFF4F7FC0; }
        @Override public int accentBright()   { return 0xFF8FB0E0; }
        @Override public int goldAccent() { return 0xFFCDBA64; }
        @Override public int treePanelBg() { return 0xCC0B1119; }
        @Override public int treePanelBorder() { return 0xFF324257; }
        @Override public int textBody() { return 0xFFCACED3; }
        @Override public int statusOk() { return 0xFF66C4A2; }
        @Override public int statusAllocated() { return 0xFFCDC478; }
        @Override public int progressionScreenBg() { return 0xFF090F16; }
        @Override public int progressionTabBg() { return 0xFF111922; }
        @Override public int progressionTabHoverBg() { return 0xFF1A2732; }
        @Override public int progressionTabBorder() { return 0xFF3A4D61; }
        @Override public int progressionTabText() { return 0xFFB0C1D3; }
        @Override public int progressionInfoText() { return 0xFFB0CEE0; }
        @Override public int progressionHint() { return 0xFF6D7F91; }
        @Override public int progressionTechAccentBg() { return 0xFF302C1F; }
        @Override public int progressionTechAccentText() { return 0xFFF2EACA; }
        @Override public int progressionSectionSepReached() { return 0xFF434B4E; }
        @Override public int progressionSectionSepLocked() { return 0xFF2E3C4B; }
        @Override public int progressionTextDim() { return 0xFF7E8287; }
        @Override public int progressionScrollTrack() { return 0xFF121921; }
        @Override public int progressionCellUnlockedBg() { return 0xFF122928; }
        @Override public int progressionCellUnlockedBgHover() { return 0xFF163733; }
        @Override public int progressionCellAvailableBg() { return 0xFF112431; }
        @Override public int progressionCellAvailableBgHover() { return 0xFF173344; }
        @Override public int progressionCellLockedBg() { return 0xFF11161E; }
        @Override public int progressionCellLockedBorder() { return 0xFF374555; }
        @Override public int progressionCellLockedText() { return 0xFF7E8692; }
        @Override public int progressionCellAvailableText() { return 0xFFD8EDFB; }
        @Override public int progressionCostShort() { return 0xFF90606C; }
        @Override public int progressionLevelOkText() { return 0xFF8899AB; }
        @Override public int progressionTooltipBg() { return 0xE00E1217; }
        @Override public int progressionTooltipBorder() { return 0xFF616D7A; }
        @Override public int progressionBudgetText() { return 0xFFA7D8B1; }
        @Override public int progressionAccentCyan() { return 0xFF5ABEE7; }
        @Override public int progressionResearchAccentBg() { return 0xFF163335; }
        @Override public int progressionTextSoft() { return 0xFFCAF6EE; }
        @Override public int progressionBenchRunningText() { return 0xFF95CEFB; }
        @Override public int progressionBarTrack() { return 0xFF141E29; }
        @Override public int progressionEdgeLit() { return 0xFF4BB2A2; }
        @Override public int progressionTextSecondary() { return 0xFFA2B4C6; }
        @Override public int progressionTextHint() { return 0xFF7B8C9E; }
        @Override public int progressionBenchAbsent() { return 0xFFA9846C; }
        @Override public int progressionDanger() { return 0xFFCD6F7C; }
        @Override public int progressionBorderAllocatedInactive() { return 0xFF32697F; }
        @Override public int progressionEdgeDim() { return 0xFF3D4F61; }
        @Override public int progressionNodeFill() { return 0xFF162A36; }
        @Override public int progressionNodeFillBroken() { return 0xFF391A1F; }
        @Override public int threatTier1() { return 0xFF7A222F; }
        @Override public int threatTier2() { return 0xFFA9652F; }
        @Override public int threatTier3() { return 0xFF85B653; }
        @Override public int threatTier4() { return 0xFF3AB653; }
        @Override public int threatTier5() { return 0xFF166A2F; }
        @Override public int statusOn() { return 0xFF16DA2F; }
        @Override public int statusOffDim() { return 0xFFA3A7AC; }
        @Override public int placeholderText() { return 0xFF8F9398; }
        @Override public int stockSliderFill() { return 0x8092BED5; }
        @Override public int stockTransitBand() { return 0x80CD6C78; }
        @Override public int stockStoredBand() { return 0x807ECE97; }
        @Override public int stockQtyLabel() { return 0xFFB4B8BD; }
        @Override public int panelSlotFill() { return 0xFF161F2C; }
        @Override public int chargeTrackBg() { return 0xFF1E2126; }
        @Override public int shadowSoft() { return 0x4D000000; }
        @Override public int panelColor()     { return 0xFF1B2230; }
        @Override public int panelDim()       { return 0x40182030; }
        @Override public int windowBg()       { return 0xFF0C121E; }
        @Override public int inputBg()        { return 0x900A1018; }
        @Override public int panelSide()       { return 0xB00E1622; }
        @Override public int popupBg()        { return 0xEF0E141F; }
        @Override public int popupBorder()    { return 0xFF2E3A4E; }
        @Override public int track()          { return 0x300A1018; }
        @Override public int slotBg()         { return 0x30162233; }
        @Override public int slotBorder()     { return 0x55406E9C; }
        @Override public int hoverBg()        { return 0x8020283C; }
        @Override public int activeBg()       { return 0x90102C44; }
        @Override public int pressedBg()      { return 0xA8000000; }
        @Override public int disabledBg()     { return 0x30060810; }
        @Override public int inputBorderHover(){ return 0xFF3A5274; }
        @Override public int borderDisabled() { return 0xFF26303F; }
        @Override public int handleBg()       { return 0x500A1018; }
        @Override public int handleHover()    { return 0x80A6C6E6; }
        @Override public int titleBarBg()     { return 0x30A6C6E6; }
        @Override public int cornerDots()     { return 0xFF4F7FC0; }
        @Override public int cornerTicks()    { return 0x804F7FC0; }
        @Override public int optionHoverBg()  { return 0x40283854; }
        @Override public int textHover()      { return 0xFFD6E2FF; }
        @Override public int textDisabled()   { return 0xFF6A7280; }
        @Override public int text()           { return 0xFFD8E0EC; }
        @Override public int pagerBarBg()     { return 0x4020283C; }
        @Override public int toggleTrackOff() { return 0x400A1018; }
        @Override public int toggleTrackOn()  { return 0xFF33607F; }
        @Override public int toggleThumb()    { return 0xFFC6D2E2; }
        @Override public int progressTrackColor() { return 0x400A1018; }
        @Override public int progressFill()   { return 0xFF4F7FC0; }
        @Override public int tabBgColor()     { return 0x4018222E; }
        @Override public int tabActive()      { return 0x90102C44; }
        @Override public int tabBorder()      { return 0x40304058; }
        @Override public int toastBg()        { return 0xEF0E141F; }
        @Override public int toastBorder()    { return 0xFF2E3A4E; }
        @Override public int toastInfo()      { return 0xFF4F7FC0; }
        @Override public int toastSuccess()   { return 0xFF6FB56F; }
        @Override public int toastWarn()      { return 0xFFD89A33; }
        @Override public int toastError()     { return 0xFFCF6A6A; }
        @Override public int shimmerColor()   { return 0x30FFFFFF; }
        @Override public int shadowColor()    { return 0x70000000; }
        @Override public int shadowOffsetY()  { return 3; }
        @Override public int textBright()     { return 0xFFEAF2FF; }
        @Override public int talentScreenBg() { return 0xFF0B0E13; }
        @Override public int talentSummaryBg() { return 0xEE0B1119; }
        @Override public int talentControlTrackBg() { return 0xFF23262B; }
        @Override public int talentScrollTrackBg() { return 0xFF141A23; }
        @Override public int talentBorderLocked() { return 0xFF706A62; }
        @Override public int talentBadgeText() { return 0xFFE6ECF5; }
        @Override public int talentFillSafeFloor() { return 300; }
        @Override public int talentBorderAvailable() { return 0xFF66C47F; }
        @Override public int talentPreview() { return 0xFF5FB5E7; }
        @Override public int talentEdgeDim() { return 0xFF495562; }
        @Override public int talentEdgeLit() { return 0xFFA69664; }
        @Override public int talentJewelZone() { return 0x40A57CE7; }
        @Override public int talentJewelLit() { return 0xFFA57CE7; }
        @Override public int talentSocketEmpty() { return 0xFF34727E; }
        @Override public int talentSocketFilled() { return 0xFFCDBD2F; }
        @Override public int talentBindingGlobal() { return 0xFF4BB2BF; }
        @Override public int talentBinding1() { return 0xFFCD6F7C; }
        @Override public int talentBinding2() { return 0xFF63C47C; }
        @Override public int talentBindingDefault() { return 0xFF8FA7BF; }
        @Override public int talentAccentDim() { return 0xFFB7AD74; }
        @Override public int talentKeystoneRingDim() { return 0x808994A2; }
        @Override public int talentTabActiveBg() { return 0xFF6B4020; }
        @Override public int talentTabBg() { return 0xFF1D364E; }
        @Override public int talentTabBorder() { return 0xFF78B5E7; }
        @Override public int talentTabIdleBg() { return 0xFF142333; }
        @Override public int talentArrowIdleBg() { return 0xFF192433; }
        @Override public int talentControlBg() { return 0xFF14212E; }
        @Override public int talentControlHoverBg() { return 0xFF23303F; }
        @Override public int talentPrimaryHoverBg() { return 0xFF234466; }
        @Override public int talentPrimaryTextHot() { return 0xFFB0E9FB; }
        @Override public int talentDangerBg() { return 0xFF2A1A1F; }
        @Override public int talentDangerHoverBg() { return 0xFF452126; }
        @Override public int talentDangerBorder() { return 0xFF905360; }
        @Override public int talentDangerText() { return 0xFFCD8491; }
        @Override public int talentTextDim() { return 0xFF95999E; }
        @Override public int talentTextNeutral() { return 0xFFE5E9EE; }
        @Override public int talentTextSoft() { return 0xFFCAF6D3; }
        @Override public int talentTextPoints() { return 0xFF95F69E; }
        @Override public int talentTextSummary() { return 0xFF95F6B9; }
        @Override public int talentNumActive() { return 0xFFCD5360; }
        @Override public int talentPointsText() { return 0xFFF6F2E4; }
        @Override public int talentNodeBg() { return 0xCC0E141B; }
        @Override public int talentNodeBgAllocated() { return 0xCC141E29; }
        @Override public int gemGold()           { return 0xFFD8A93A; }
        @Override public int gemWindowBg()       { return 0xE8121A2A; }
        @Override public int gemPanelBg()        { return 0xB80E1420; }
        @Override public int gemViewBg()         { return 0xFF1E2A3C; }
        @Override public int gemBackdropTop()    { return 0xEE0A1018; }
        @Override public int gemBackdropBottom() { return 0xEE04080E; }
        @Override public int gemTextMuted()      { return 0xFF8FA6BF; }
        @Override public int gemTextPositive()   { return 0xFF7FD07F; }
        @Override public int gemDesaturate()     { return 0xB0556478; }
        @Override public int gemAccentSkill()    { return 0xFFBF6455; }
        @Override public int gemAccentSupport()  { return 0xFF53AD74; }
        @Override public int gemAccentSpirit()   { return 0xFF9A74BF; }
        @Override public int gemAccentFallback() { return 0xFF5F97BF; }
        @Override public int radialBackdrop()   { return 0x8A000000; }
        @Override public int radialHubFill()    { return 0xFF263344; }
        @Override public int radialBorderHot()  { return 0xFFE8B84B; }
        @Override public int radialBorderIdle() { return 0xFF54708C; }
        @Override public int radialFillHot()    { return 0xFF243349; }
        @Override public int radialFillIdle()   { return 0xFF161F2C; }
        @Override public int radialTextHot()    { return 0xFFE8B84B; }
        @Override public int graphNodeBorder()         { return 0xFF4A5A72; }
        @Override public int graphHoverRing()          { return 0xFFD6E2FF; }
        @Override public int graphNodeActiveFill()     { return 0xFF28496E; }
        @Override public int graphNodeDisabledFill()   { return 0xFF141A26; }
        @Override public int graphNodeDisabledBorder() { return 0xFF202836; }
        @Override public int graphEdgeHover()          { return 0xFF7FA8D8; }
        @Override public int radialTextIdle() { return 0xFFDCDCDC; }
        @Override public int scrim()          { return 0x8A000000; }
        @Override public int textMuted()      { return 0xFF8A94A6; }
        @Override public int textDanger()     { return 0xFFE8736A; }
        /** Midnight = 深夜霓虹：细扫描线 + 角部光晕。 */
        @Override public BackgroundKind backgroundKind() { return BackgroundKind.NEON; }
        /** 霓虹档音效偏清亮（高半档）。 */
        @Override public float sfxPitch()      { return 1.1F; }
        @Override public String id()          { return "midnight"; }
        @Override public String displayName() { return "Midnight"; }
    }
}
