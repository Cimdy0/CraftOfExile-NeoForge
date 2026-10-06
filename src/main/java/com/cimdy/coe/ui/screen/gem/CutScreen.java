package com.cimdy.coe.ui.screen.gem;
import com.cimdy.coe.data.codec.skill.gem.GemCategory;
import com.cimdy.coe.network.skill.CutGemPacket;
import com.cimdy.coe.network.skill.CutResultPacket;
import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.attach.AttachmentPresets;
import com.cimdy.coe.ui.widget.attach.Sfx;
import com.cimdy.coe.ui.widget.attach.Vfx;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.panel.ToastHost;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.compose.CostLine;
import com.cimdy.coe.ui.widget.compose.business.CutPreview;
import com.cimdy.coe.ui.widget.compose.semantic.Section;
import com.cimdy.coe.ui.widget.compose.picker.SearchableList;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.screen.WidgetScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * 宝石切割（M3 分层重构，POE2 参考 #02）：240×200。
 *
 * <p><b>首层分类 = 主动 / 辅助 / 精魂</b>（C.5：三分类 Tab，复用既有纵列按钮组）：
 * <ul>
 *   <li><b>主动</b> → {@link GemCategory#SKILL}（SkillData，主动技能宝石）；</li>
 *   <li><b>辅助</b> → {@link GemCategory#SUPPORT}（SupportData，辅助宝石）——与主动<b>独立分段，不共用档位分组</b>；</li>
 *   <li><b>精魂</b> → {@link GemCategory#SPIRIT}（spirit=true 的 SkillData，光环类）。</li>
 * </ul>
 * defaultTypeKey 为 "support"/"spirit" 时分别切到辅助/精魂 Tab，否则默认主动。
 *
 * <p><b>档位分组骨架（待数据层）</b>：主动 ≈ 20 档（T1–T20），按玩家等级切分；
 * 辅助 = 独立等级分段（低/中/高，不同层级）。档位表 / 等级门槛为客户端<b>占位骨架</b>，
 * 真实档位表待数据层确认（见 {@link #ACTIVE_TIER_COUNT} / {@link #activeTierLevelReq}）。
 *
 * <p><b>切割 = 等级赋予 / 灌输到技能石</b>：产物 Gem 实例携带等级。等级注入由<b>服务端</b>
 * 按「原槽未切割宝石的等阶」在重建产物时完成（{@code GemCategory#buildCutList(tier)}
 * → SkillData.level / SupportData.level），{@link CutGemPacket} 仅需 (slotIndex, gemId)。
 *
 * <p><b>成功 / 失败双反馈</b>：切割 <b>100% 确定</b>（用户"别失败"；与 POE2 参考一致），服务端只发成功回执；
 * 成功 → 产物 tier 角标亮显 + 品质 pop（{@link #showCutSuccess}）。{@link #showCutFailure} 保留为兜底
 * （防御未来成功率引入，当前服务端永不发失败）。
 * 由 {@link CutResultPacket}（服务端回执）经 {@link #handleCutResult} 驱动，UI 不写死概率。
 */
public class CutScreen extends WidgetScreen {

    private static final int WINDOW_WIDTH = 240;
    private static final int WINDOW_HEIGHT = 200;

    /** 失败 toast 驻留 tick（规格 4 tick；ToastHost 入场 6 + 淡出 12，取可见窗口 ~40≈2s，调参待数据层）。 */
    private static final int CUT_FAIL_TOAST_LIFE = 40;

    /** 主动切割档位数：T1–T20（与 {@code UncutGem.SKILL_TIER_MAX} 对齐）。 */
    private static final int ACTIVE_TIER_COUNT = 20;
    /** 辅助切割独立分段骨架：低 / 中 / 高（辅助等阶上限 {@code UncutGem.SUPPORT_TIER_MAX}=5）。 */
    private static final String[] SUPPORT_SEGMENTS = {"low", "mid", "high"};

    /** 成功率显示：切割 100% 确定（服务端永不发失败回执；此常量保留供未来调参展示）。 */
    private static final int CUT_SUCCESS_CHANCE_DISPLAY = 100;

    /** 当前打开中的切割屏（26.2 无 Minecraft.screen 访问器；init 注册 / removed 注销，回执只发本人）。 */
    private static CutScreen current;

    public static CutScreen current() {
        return current;
    }

    /** 首层分类：主动 / 辅助 / 精魂（天然分离自 SkillEntries：主动=SkillData / 辅助=SupportData / 精魂=spirit SkillData）。 */
    private enum CutMode {
        ACTIVE(GemCategory.SKILL),
        SUPPORT(GemCategory.SUPPORT),
        SPIRIT(GemCategory.SPIRIT);
        final GemCategory category;
        CutMode(GemCategory c) { category = c; }
    }

    private final Slot slot;
    private final int tier;
    private CutMode mode;

    private SearchableList<ItemStack> gemList;
    private Section tierHeader;
    private CutPreview preview;
    private Button confirmBtn;
    /** 三分类 Tab 按钮（与 {@link CutMode#values()} 同序），用于同步选中态高亮。 */
    private Button[] tabButtons;
    /** 兼容性「仅显示兼容 / 显示全部」开关（POE2 Show Recommended 折中；判定结果待数据层包，当前仅骨架）。 */
    private boolean recommendedOnly = true;
    private Button compatToggle;
    /** 当前选中的待切产物（null = 空态）。 */
    private ItemStack selected = ItemStack.EMPTY;
    /** 反馈音效锚点节点。 */
    private Node sfxAnchor;
    /** D.5 cut_success 刻印闪光特效（环形 1 圈）；挂到窗根，切割成功回执时触发。 */
    private Vfx cutVfx;

    public CutScreen(Player player, Slot slot, String defaultTypeKey, int tier) {
        super(Component.translatable("screen.coe.gem_cut.title", player.getName()));
        this.slot = slot;
        this.tier = tier;
        // defaultTypeKey 为 "support" → 辅助首层；"spirit" → 精魂首层；其余默认主动（SKILL）。
        String key = defaultTypeKey == null ? "" : defaultTypeKey.toLowerCase(java.util.Locale.ROOT);
        this.mode = switch (key) {
            case "support" -> CutMode.SUPPORT;
            case "spirit" -> CutMode.SPIRIT;
            default -> CutMode.ACTIVE;
        };
    }

    @Override
    public void removed() {
        super.removed();
        if (current == this) current = null;
    }

    /** 主动档等级门槛占位：tier*4（如 T1=4、T20=80）——<b>占位常量，待数据层确认，勿当真值</b>。 */
    private static int activeTierLevelReq(int tier) { return tier * 4; }

    /** 辅助分段名（独立层级，不共用主动 20 档）。 */
    private static String supportSegmentOf(int tier) {
        return SUPPORT_SEGMENTS[Math.min(Math.max(tier - 1, 0), SUPPORT_SEGMENTS.length - 1)];
    }

    @Override
    protected void buildUi(WindowLayer root) {
        current = this;
        AttachmentPresets preset = AttachmentPresets.defaults();
        Window window = CoeUi.panel(title, WINDOW_WIDTH, WINDOW_HEIGHT)
                .onClose(this::onClose)
                .center(this.width, this.height);

        Node body = new Node().row();
        body.flex.grow = 1;
        body.flex.gapMain = 1;
        body.flex.alignItems = AlignItems.STRETCH;

        // ---- 左分类栏：首层 = 主动 / 辅助 / 精魂（纵列三 Tab，复用既有按钮组）----
        Node catBar = new Node().column();
        catBar.flex.gapMain = 2;
        catBar.flex.width = 52;
        CutMode[] modes = CutMode.values();
        tabButtons = new Button[modes.length];
        for (int i = 0; i < modes.length; i++) {
            CutMode m = modes[i];
            Button b = CoeUi.button(m.category.displayName())
                    .onClick(() -> selectMode(m));
            b.flex.grow = 0;
            b.flex.height = CoeUi.ROW_HEIGHT;
            tabButtons[i] = b;
            catBar.add(b);
        }
        body.add(catBar);

        // ---- 中：档位分组段头 + 可切列表 ----
        Node mid = new Node().column();
        mid.flex.grow = 1;
        mid.flex.width = 96;
        mid.flex.gapMain = 1;

        tierHeader = CoeUi.section(Component.empty());
        mid.add(tierHeader);

        // 兼容性开关（POE2 Show Recommended 折中）：默认仅显兼容、开关显全部。
        // 挂载判定结果待数据层包下发，当前仅骨架——翻转本地布尔，不过滤任何条目。
        compatToggle = CoeUi.button(Component.translatable("ui.coe.cut.compat_recommended"))
                .onClick(this::toggleRecommended);
        compatToggle.flex.grow = 0;
        compatToggle.flex.height = CoeUi.ROW_HEIGHT;
        mid.add(compatToggle);

        gemList = new SearchableList<ItemStack>(CoeUi.listRow(), this::bindRow)
                .textOf(this::stackName)
                .itemGap(1)
                .contentPadding(2)
                .emptyView(CoeUi.emptyHint())
                .scrollBar(true)
                .clearBeforeBind();
        gemList.flex.grow = 1;
        mid.add(gemList);
        body.add(mid);

        // ---- 右预览区（CutPreview）----
        preview = CutPreview.of()
                .empty(Component.translatable("ui.coe.cut_preview.empty"));
        preview.flex.width = 76;
        preview.flex.grow = 0;

        confirmBtn = CoeUi.button(Component.translatable("screen.coe.gem_cut.cut"))
                .onClick(this::confirmCut);
        confirmBtn.flex.height = CoeUi.ROW_HEIGHT;
        preview.confirmButton(confirmBtn);
        body.add(preview);

        sfxAnchor = body;
        window.add(body);
        // CUT 档默认附件：标题带/角点装饰 + 标准音效族（沿 sfxAnchor 链发声）+ cut_success 特效。
        // 特效实际渲染仍受全局 Vfx.MASTER_ENABLED（默认关）约束——这里只完成接线，不翻全局开关。
        cutVfx = AttachmentPresets.vfxFor(preset, AttachmentPresets.Target.CUT);
        window.attach(
                AttachmentPresets.decorFor(preset, AttachmentPresets.Target.CUT),
                AttachmentPresets.sfxFor(preset, AttachmentPresets.Target.CUT),
                cutVfx);
        root.add(window);

        rebindList();
    }

    private void selectMode(CutMode m) {
        if (m == null || m == mode) return;
        this.mode = m;
        rebindList();
    }

    /** 统一需求等级门槛占位：tier*4（主动/精魂共用；辅助亦暂用同一占位，真实口径待数据层）。 */
    private int reqLevel() { return activeTierLevelReq(tier); }

    private void rebindList() {
        // 切换首层分类重建各自网格（主动/精魂 20 档 / 辅助独立分段）。
        GemCategory cat = mode.category;
        List<ItemStack> gems = cat != null ? cat.buildCutList(tier) : new ArrayList<>();
        gemList.list().items(gems);

        // 档位分组段头：主动 / 精魂 → T{tier}·需求等级{req}（待数据层）；辅助 → 独立分段。
        if (mode == CutMode.ACTIVE || mode == CutMode.SPIRIT) {
            tierHeader.title(Component.translatable("ui.coe.cut.tier_header_active",
                    Math.min(tier, ACTIVE_TIER_COUNT), reqLevel()));
        } else {
            tierHeader.title(Component.translatable("ui.coe.cut.tier_header_support",
                    Component.translatable("ui.coe.cut.seg_" + supportSegmentOf(tier))));
        }

        refreshTabActive();
        // 兼容开关仅辅助段相关（主动/精魂无兼容概念），隐藏避免误导。
        compatToggle.visible = (mode == CutMode.SUPPORT);
        selected = ItemStack.EMPTY;
        preview.product(ItemStack.EMPTY);
        preview.costLines(new ArrayList<>());
        confirmBtn.active(false);
    }

    /** 同步三分类 Tab 的选中高亮（当前 mode 对应按钮 active）。 */
    private void refreshTabActive() {
        CutMode[] modes = CutMode.values();
        for (int i = 0; i < modes.length; i++) {
            if (tabButtons != null && i < tabButtons.length) tabButtons[i].active(modes[i] == mode);
        }
    }

    /** 兼容性开关翻转（骨架）：切换本地布尔并刷新按钮文案；实际过滤待数据层挂载判定包。 */
    private void toggleRecommended() {
        recommendedOnly = !recommendedOnly;
        compatToggle.label(Component.translatable(recommendedOnly
                ? "ui.coe.cut.compat_recommended" : "ui.coe.cut.compat_all"));
        // 待数据层：无挂载判定结果时不做过滤，列表保持全显（避免误空）。
    }

    private void bindRow(Node view, ItemStack stack, int index) {
        if (!(view instanceof Button row)) return;
        boolean reachable = playerLevel() >= reqLevel();
        Component name = Component.literal(stackName(stack));
        if (!reachable) {
            // 等级门槛不达标：红字 + 置灰 + 禁点（不挂 onClick）。
            row.label(name.copy().withStyle(ChatFormatting.DARK_RED));
            if (row.labelRenderer() != null) row.labelRenderer().color(Theme.current().textDisabled());
            return;
        }
        // 辅助段：行尾补「与已装备技能兼容」徽标位（POE2 Show Recommended）。
        // 挂载判定结果待数据层包下发——当前仅渲染空心占位 ◇，不做真实兼容过滤。
        if (mode == CutMode.SUPPORT) {
            name = name.copy().append(" ⟡"); // 待数据层：兼容徽标（默认仅显兼容 / 开关显全部）
        }
        row.label(name);
        // 修复（A.1 #5）：列表行挂点击 → 选中预览联动（此前 selectGem 无调用方 = 死代码）。
        row.onClick(() -> selectGem(stack));
    }

    private String stackName(ItemStack stack) {
        GemCategory cat = mode.category;
        return cat != null ? cat.extractName(stack).getString() : stack.getHoverName().getString();
    }

    private void selectGem(ItemStack stack) {
        selected = stack;
        // 等级门槛（客户端估算，用 reqLevel()=tier*4 占位代理产物需求级）：不达标 → 预览给原因、确认置灰。
        boolean canCut = playerLevel() >= reqLevel();
        preview.product(stack);

        List<CostLine> lines = new ArrayList<>();
        // 成功率（镜像服务端常量）+ 产物等级 = tier + 材料 = 源槽未切宝石
        lines.add(CostLine.of().cost(CUT_SUCCESS_CHANCE_DISPLAY).affordable(true)); // 成功率（服务端同源镜像）
        lines.add(CostLine.of().cost(tier).affordable(canCut)); // 产物等级 = tier
        lines.add(CostLine.of().icon(slot.getItem()).cost(1).affordable(canCut)); // 材料 = 源槽未切宝石
        preview.costLines(lines);

        confirmBtn.active(canCut);
    }

    private int playerLevel() {
        LocalPlayer lp = Minecraft.getInstance().player;
        return lp == null ? 0 : lp.experienceLevel;
    }

    private void confirmCut() {
        if (selected == null || selected.isEmpty()) return;
        if (playerLevel() < reqLevel()) return; // 置灰态不响应（与段头需求等级一致）
        GemCategory cat = mode.category;
        if (cat == null) return;
        Identifier gemId = cat.extractGemId(selected);
        if (gemId == null) return;
        // 确认弹窗防误触后再发包（CoeUi.confirm 替换本屏，确认后切包）。
        CoeUi.confirm(Component.translatable("screen.coe.gem_cut.title"),
                Component.translatable("screen.coe.gem_cut.confirm_msg", stackName(selected)),
                () -> {
                    // 切割 = 等级灌输到技能石：服务端按「原槽未切宝石等阶」重建产物时完成等级注入，
                    // 客户端仅提供目标宝石 id（等级字段无需入包）。
                    ClientPacketDistributor.sendToServer(new CutGemPacket(slot.getSlotIndex(), gemId));
                    onClose();
                });
    }

    // ------------------------------------------------------------------
    // 成功 / 失败双反馈（服务端 CutResultPacket 驱动；UI 不写死概率）
    // ------------------------------------------------------------------

    /**
     * 服务端切割结果回执入口（{@code CutResultPacket} 到达，主线程）。
     * 成功 → 产物亮显 + 成功音效 + 关屏；失败 → ERROR toast + 重建列表（兜底骨架）。
     * 注：切割 100% 确定，服务端永不发失败回执，else 分支当前不可达（死代码兜底）；
     * cut_fail 已废除，失败路径不发射任何失败音效。
     */
    public void handleCutResult(CutResultPacket packet) {
        if (packet == null) return;
        if (packet.success()) {
            showCutSuccess(packet.product());
            onClose();
        } else {
            showCutFailure();
            if (selected != null && !selected.isEmpty()) {
                selected = ItemStack.EMPTY;
                preview.product(ItemStack.EMPTY);
                confirmBtn.active(false);
            }
        }
    }

    /**
     * 切割成功反馈：产物 tier 角标亮显 + 品质 pop + 成功音效。
     * 产物由服务端重建（等级已灌输），直接渲染即亮显。
     */
    private void showCutSuccess(ItemStack product) {
        if (preview != null) preview.product(product); // 产物渲染即 tier 角标亮显
        if (sfxAnchor != null) Sfx.emit(sfxAnchor, Sfx.Kind.SUCCESS); // 成功音效位（已接原版占位，待音效资源）
        // D.5 cut_success：刻印闪光（环形 1 圈）。切割 100% 确定无失败分支，只挂成功路径；
        // 坐标相对窗根取窗口中心。渲染仍受 Vfx.MASTER_ENABLED 全局默认关约束。
        if (cutVfx != null) cutVfx.cutSuccess(WINDOW_WIDTH / 2F, WINDOW_HEIGHT / 2F, Theme.current().gemGold());
    }

    /**
     * 切割失败反馈兜底骨架：ERROR toast（短驻留）+ 行灰闪（200ms 淡出）。
     *
     * <p><b>死代码（2026-10-05 清理）</b>：切割 100% 确定，服务端 {@link CutResultPacket} 永不发失败回执，
     * 本方法当前不可达；{@code cut_fail} 音效已废除（不注册失败音效）。保留骨架仅为防御未来成功率
     * 引入时快速接回 —— 现不再发射任何 {@code Sfx.Kind.FAIL}。
     */
    private void showCutFailure() {
        ui.toasts().push(Component.translatable("ui.coe.cut.fail"), ToastHost.ToastStyle.ERROR, CUT_FAIL_TOAST_LIFE);
        // 行灰闪：选中格 ~200ms 淡出（AnimatedNode fadeTo 待接选中格节点，骨架位）。
        // （已移除 Sfx.emit(..., Kind.FAIL)：cut_fail 废除，死代码兜底不发射失败音效。）
    }
}
