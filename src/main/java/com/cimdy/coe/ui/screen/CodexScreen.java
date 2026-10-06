package com.cimdy.coe.ui.screen;
import com.cimdy.coe.client.ItemSearch;
import com.cimdy.coe.data.codec.item.ItemWeight;
import com.cimdy.coe.data.codec.item.equipment.EquipmentModPreset;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeHierarchy;
import com.cimdy.coe.data.codec.item.registry.ItemDataRegistry;
import com.cimdy.coe.data.codec.skill.core.SkillEntries;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.vanilla.EnchantModifier;
import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.attach.AttachmentPresets;
import com.cimdy.coe.ui.widget.attach.Vfx;
import com.cimdy.coe.ui.widget.compose.business.CodexBrowser;
import com.cimdy.coe.ui.widget.compose.codex.CodexSchema;
import com.cimdy.coe.ui.widget.compose.codex.CodexSource;
import com.cimdy.coe.ui.widget.compose.codex.NavTree;
import com.cimdy.coe.ui.widget.compose.search.SearchLevel;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.list.OptionListPopup;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.screen.WidgetScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentRead;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 图鉴 → CodexDB 游戏内数据库 v2（计划 §3.3，冻结契约 §6 源重构重写）。
 *
 * <p>三栏 560×320：左 {@link NavTree}（二级折叠分类树 140px）+ 中 {@link com.cimdy.coe.ui.widget.compose.composite.SortableList}
 * （搜索）+ 右 SegmentToggle tab 条（详情/词缀）+ {@link com.cimdy.coe.ui.widget.compose.composite.DetailPanel} /
 * 词缀分组视图，全部由 {@link CodexBrowser} 复合组件装配。
 *
 * <p><b>v2 变更</b>：
 * <ul>
 *   <li><b>删除</b> stat（属性）与 keyword（关键字）两个源；<b>词缀不再注册为顶层源</b>，挂在装备类型下（右栏词缀 tab）；</li>
 *   <li>源：装备条目（group=equipment）/ 技能条目（group=skill，二级=inline/read）/ 附魔（group=enchant）/ 掉落权重（group=drop）；</li>
 *   <li>NavTree 由本类装配（一级组 = group，二级叶 = 装备类型 / inline/read / 各组 all 叶）；</li>
 *   <li>词缀 tab：选中装备类型叶后，右栏按 poe2db 风格分组渲染该类型 mod pool（普通 + 预设 Condition 展开），标注「简化版」。</li>
 * </ul>
 *
 * <p><b>数据纪律（C.5b / A.3#1）</b>：装备类型叶选中 → 切到 {@code EquipDefSource}（其 {@code subGroupKey}
 * 非空 = {@code "eq:"+type}，CodexBrowser 按二级键精确过滤中列表，修复「叶选中不过滤」的语义割裂）；
 * 装备类型叶按 {@link ItemTypeHierarchy}（只读 ancestorsOf）的顶层父类分组成大类组。
 * 原版物品全列叶（{@code ItemSource}）与装备 type 无现成映射，其 {@code subGroupKey} 恒 empty（不编造映射）。
 * 纯客户端只读，反映当前存档经 {@code LoadEvent} 填充的内容。
 */
public class CodexScreen extends WidgetScreen {

    private static final int WINDOW_WIDTH = 560;
    private static final int WINDOW_HEIGHT = 320;

    private final List<ItemStack> allItems = new ArrayList<>();
    private final List<SkillEntryTypeView> skillTypes = new ArrayList<>();
    private final List<Map.Entry<ResourceKey<Enchantment>, EnchantModifier.Builder>> enchantEntries = new ArrayList<>();
    private final List<Map.Entry<Identifier, Map<ItemWeight.Type, Integer>>> dropEntries = new ArrayList<>();

    // ---- 装配态（buildUi 后由回调驱动）----
    private CodexBrowser browser;
    private AnimatedNode affixHost;
    /** D.5 图鉴「搜索命中星点」特效；挂窗根，叶选中解析出结果列表时触发。 */
    private Vfx codexVfx;
    /** 词缀跳转用的 NavTree 引用（词缀行点击 → 程序化选中装备类型叶）。 */
    private NavTree treeRef;
    private int currentTab;
    /** 当前选中的装备类型叶（词缀 tab 的数据键）；非装备类型叶时为 null。 */
    private Identifier currentType;

    public CodexScreen() {
        super(Component.translatable("ui.coe.codex"));
        collectItems();
        collectSkills();
        collectEnchants();
        collectDrops();
    }

    // ------------------------------------------------------------------
    // 数据收集（构造期一次性完成；数据 codec 只读）
    // ------------------------------------------------------------------

    private void collectItems() {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR) allItems.add(new ItemStack(item));
        }
    }

    private void collectSkills() {
        for (SkillEntry.Type<?> type : SkillEntries.REGISTRY) {
            skillTypes.add(new SkillEntryTypeView(type));
        }
    }

    private void collectEnchants() {
        for (Map.Entry<ResourceKey<Enchantment>, EnchantModifier.Builder> e :
                ItemDataRegistry.getInstance().getEnchantModifiers().entrySet()) {
            enchantEntries.add(e);
        }
    }

    private void collectDrops() {
        for (Map.Entry<Identifier, Map<ItemWeight.Type, Integer>> e :
                ItemDataRegistry.getInstance().getTypeWeights().entrySet()) {
            dropEntries.add(e);
        }
    }

    // ------------------------------------------------------------------
    // 构建：CodexBrowser 三栏 + NavTree 装配
    // ------------------------------------------------------------------

    @Override
    protected void buildUi(WindowLayer root) {
        Window window = CoeUi.panel(Component.translatable("ui.coe.codex"), WINDOW_WIDTH, WINDOW_HEIGHT)
                .onClose(this::onClose)
                .center(this.width, this.height);

        Node body = new Node().row();
        body.flex.grow = 1;

        // 词缀分组视图宿主（唯一实现点：poe2db 风格 mod pool 分组渲染，见 renderAffixView）
        affixHost = new AnimatedNode();
        affixHost.column().padding(4);
        affixHost.grow(1);

        browser = CodexBrowser.of()
                .registerSource(new ItemSource())
                .registerSource(new EquipDefSource())
                .registerSource(new SkillSource())
                .registerSource(new EnchantSource())
                .registerSource(new DropSource())
                .onSourceSelect(s -> { /* 源切换上抛点；标题栏随窗口，无需联动 */ });
        browser.flex.grow = 1;

        treeRef = buildNavTree();
        browser.navTree(treeRef)
                .tabs(Component.translatable("ui.coe.codex.tab_detail"),
                        Component.translatable("ui.coe.codex.tab_affix"))
                .affixView(affixHost)
                .onTabSelect(i -> { currentTab = i; if (i == 1) renderAffixView(); })
                .onNavSelect(this::onNavLeaf);

        body.add(browser);
        window.add(body);
        // CODEX 档默认附件：标题带/角点徽章 + 标准音效族 + 搜索命中星点特效（克制，同屏特效 <10）。
        // 特效实际渲染仍受全局 Vfx.MASTER_ENABLED（默认关）约束——这里只完成接线。
        AttachmentPresets preset = AttachmentPresets.defaults();
        codexVfx = AttachmentPresets.vfxFor(preset, AttachmentPresets.Target.CODEX);
        window.attach(
                AttachmentPresets.decorFor(preset, AttachmentPresets.Target.CODEX),
                AttachmentPresets.sfxFor(preset, AttachmentPresets.Target.CODEX),
                codexVfx);
        root.add(window);
    }

    /**
     * NavTree 组装（Screen 内，冻结契约 §6；C.5b）：
     * 一级「装备」基础组 = 装备条目 all 叶（{@code BuiltInRegistries.ITEM} 全列原版物品）；
     * 装备类型叶按 {@link ItemTypeHierarchy}（只读 ancestorsOf 传递闭包）的<b>顶层父类</b>分组成若干大类组
     * （武器 / 护甲 / 饰品…），组内为该大类下的具体类型叶 —— 形成「装备大类 → 具体类型 → 中列表物品」
     * 的递归分类树，取代旧的类型叶平铺在单一「装备」组下。
     * 技能组 = inline/read 叶 + all 叶；一级「附魔」「掉落权重」= 各自条目叶。
     *
     * <p>叶子 id 约定：装备类型叶 = {@code "eq:" + type}（onNavLeaf 据此还原 Identifier 驱动词缀 tab，
     * 并由 EquipDefSource.subGroupKey 精确过滤中列表 = A.3#1 修复）；其余叶 id = 源 id（「全部」叶）。
     */
    private NavTree buildNavTree() {
        ItemDataRegistry reg = ItemDataRegistry.getInstance();
        NavTree tree = NavTree.of();

        // 装备基础组：原版物品全列叶（ItemSource，不细分）
        List<NavTree.Leaf> baseEq = new ArrayList<>();
        baseEq.add(new NavTree.Leaf("item", Component.translatable("ui.coe.items"), allItems.size()));
        tree.addGroup(Component.translatable("ui.coe.codex.group_equipment"), baseEq);

        // 装备类型叶：用 ItemTypeHierarchy 按顶层父类分组（只读，不改数据层）
        Map<Identifier, List<EquipmentRead>> reads = reg.getEquipmentReads();
        Map<Identifier, List<NavTree.Leaf>> byRoot = new LinkedHashMap<>();
        for (Map.Entry<Identifier, List<EquipmentRead>> e : reads.entrySet()) {
            Identifier type = e.getKey();
            Identifier root = rootOfType(type);
            byRoot.computeIfAbsent(root, k -> new ArrayList<>())
                    .add(new NavTree.Leaf("eq:" + type,
                            Component.literal(type.getPath()), e.getValue().size()));
        }
        for (Map.Entry<Identifier, List<NavTree.Leaf>> g : byRoot.entrySet()) {
            tree.addGroup(Component.literal(g.getKey().getPath()), g.getValue());
        }

        // 技能组
        // 待数据层：规格 C.5b 期望技能叶 = 主动/辅助/精魂（GemCategory.SKILL/SUPPORT/SPIRIT），
        // 但 SkillEntry.Type → GemCategory 的只读映射访问器在数据层尚未提供，本 shard 不臆造映射；
        // 维持既有 inline/read 二级分类（SkillEntries.nameOf），不回退现有行为。
        List<NavTree.Leaf> skLeaves = new ArrayList<>();
        int inline = 0, read = 0;
        for (SkillEntryTypeView v : skillTypes) {
            if ("inline".equals(v.sub())) inline++; else read++;
        }
        skLeaves.add(new NavTree.Leaf("inline", Component.literal("inline"), inline));
        skLeaves.add(new NavTree.Leaf("read", Component.literal("read"), read));
        skLeaves.add(new NavTree.Leaf("skill", Component.translatable("ui.coe.codex.group_skill"), skillTypes.size()));
        tree.addGroup(Component.translatable("ui.coe.codex.group_skill"), skLeaves);

        // 附魔组
        List<NavTree.Leaf> enLeaves = new ArrayList<>();
        enLeaves.add(new NavTree.Leaf("enchant", Component.translatable("ui.coe.codex.group_enchant"), enchantEntries.size()));
        tree.addGroup(Component.translatable("ui.coe.codex.group_enchant"), enLeaves);

        // 掉落权重组
        List<NavTree.Leaf> drLeaves = new ArrayList<>();
        drLeaves.add(new NavTree.Leaf("drop", Component.translatable("ui.coe.codex.group_drop"), dropEntries.size()));
        tree.addGroup(Component.translatable("ui.coe.codex.group_drop"), drLeaves);

        return tree;
    }

    /**
     * C.5b：由 {@link ItemTypeHierarchy}（只读）求某装备类型的<b>顶层父类</b>（祖先闭包中自身无祖先者），
     * 用于把装备类型叶按大类分组；类型本身无层级时返回其自身（退化为每类一组，优雅降级）。
     */
    private static Identifier rootOfType(Identifier type) {
        Set<Identifier> anc = ItemTypeHierarchy.ancestorsOf(type);
        if (anc.isEmpty()) return type;
        for (Identifier a : anc) {
            if (ItemTypeHierarchy.ancestorsOf(a).isEmpty()) return a;
        }
        return anc.iterator().next();
    }

    /**
     * 二级叶子选中（CodexBrowser 已先按二级键过滤中列表，这里解释语义）：
     * 切到对应源；装备类型叶额外记录 type key 供词缀 tab。
     */
    private void onNavLeaf(NavTree.Leaf leaf) {
        String id = leaf.id();
        currentType = null;
        if (id.startsWith("eq:")) {
            // 装备类型叶：切到「装备定义源」——中列表显示该类型的模组装备定义（真实数据，按 subGroupKey 过滤），
            // 词缀 tab 仍由 currentType 驱动；不再是误导性的全列原版物品。
            browser.selectSource("equipdef");
            currentType = Identifier.tryParse(id.substring(3));
        } else if (id.equals("item") || id.equals("enchant") || id.equals("drop")) {
            browser.selectSource(id);
        } else {
            // 技能二级叶（inline/read）或 skill all 叶
            browser.selectSource("skill");
        }
        // D.5 搜索命中星点：叶选中解析出结果列表时爆一簇星点。搜索框封装在 CodexBrowser 内、
        // 未对屏暴露 onSearch 回调，故挂在屏可观测到的「叶选中 → 切源 → 结果列表解析」点（按实际 API 定）。
        // 坐标相对窗根取窗口中心；渲染受 Vfx.MASTER_ENABLED 全局默认关约束。
        if (codexVfx != null) {
            codexVfx.pressedStars(WINDOW_WIDTH / 2F, WINDOW_HEIGHT / 2F, Theme.current().accent());
        }
        if (currentTab == 1) renderAffixView();
    }

    // ------------------------------------------------------------------
    // 词缀分组视图（唯一实现点；潜在复用方：未来「某装备类型的 mod pool 浏览」界面）
    // ------------------------------------------------------------------

    /**
     * 词缀 tab 内容：按 poe2db 风格分组渲染当前装备类型的 mod pool（冻结契约 §6）。
     * 数据源：{@code getModPools().get(type)}（普通池）+ {@code getPresetModPools().get(type)}（按 Condition 展开）。
     * 每条 = 词缀名（{@code item.mod.coe.<mod>}，随 tier 着色）+ tier/level/加权 + effect（{@code getComponent()}）。
     * 界面标注「简化版」；未选装备类型时空态提示。
     */
    private void renderAffixView() {
        affixHost.clearChildren();
        affixHost.add(textNode(Component.translatable("ui.coe.codex.simplified"), Theme.current().goldAccent()));
        if (currentType == null) {
            affixHost.add(linesNode(List.of(Component.translatable("ui.coe.codex.affix_hint")),
                    Theme.current().textDisabled()));
            return;
        }

        ItemDataRegistry reg = ItemDataRegistry.getInstance();
        List<Node> rows = new ArrayList<>();

        // 普通词缀池
        List<WeightedList.Builder<CommonMod>> pools = reg.getModPools().get(currentType);
        if (pools != null) {
            for (WeightedList.Builder<CommonMod> b : pools) appendModRow(rows, b);
        }
        // 预设词缀池：按 Condition 分组展开
        Map<EquipmentModPreset.Condition, List<WeightedList.Builder<CommonMod>>> presets =
                reg.getPresetModPools().get(currentType);
        if (presets != null) {
            for (Map.Entry<EquipmentModPreset.Condition, List<WeightedList.Builder<CommonMod>>> e : presets.entrySet()) {
                rows.add(linesNode(List.of(describeCondition(e.getKey())), Theme.current().text()));
                for (WeightedList.Builder<CommonMod> b : e.getValue()) appendModRow(rows, b);
            }
        }
        if (rows.isEmpty()) {
            affixHost.add(linesNode(List.of(Component.translatable("ui.coe.codex.affix_hint")),
                    Theme.current().textDisabled()));
            return;
        }
        Node col = new Node().column();
        col.flex.gapMain = 1;
        for (Node r : rows) col.add(r);
        affixHost.add(col);
    }

    /** 词缀行（可点击，跨源跳转：反查挂载该词缀的首个装备类型 → 树选中 + 装备定义浏览）。 */
    private void appendModRow(List<Node> out, WeightedList.Builder<CommonMod> builder) {
        WeightedList<CommonMod> wl = builder.build();
        for (Weighted<CommonMod> w : wl.unwrap()) {
            CommonMod m = w.value();
            MutableComponent name = m.name().orElse(Component.literal(m.mod()));
            MutableComponent line = name.copy()
                    .append(Component.literal(" [T" + String.valueOf(m.tier()) + " L" + m.level() + " w" + w.weight() + "] "))
                    .append(m.getComponent());
            Button row = new Button();
            row.label(line);
            row.onClick(() -> jumpToTypeForMod(m));
            row.flex.height = CoeUi.ROW_HEIGHT;
            out.add(row);
        }
    }

    /** 词缀 → 挂载类型跳转（修复批 3 增强）：反查含该词缀的全部装备类型（普通池 → 预设池，去重保序）；
     *  单类型直接跳转，多类型弹出选择（poe2db 式），选中后树高亮 + 中列表刷新到装备定义源。 */
    private void jumpToTypeForMod(CommonMod mod) {
        List<Identifier> types = allTypesHolding(mod);
        if (types.isEmpty() || treeRef == null) return;
        if (types.size() == 1) {
            jumpToType(types.get(0));
            return;
        }
        OptionListPopup<Identifier> popup = OptionListPopup.of(types,
                t -> Component.literal(t.getPath()))
                .onPick(this::jumpToType);
        popup.popupStyle();
        popup.showIn(ui.layer());
    }

    private void jumpToType(Identifier type) {
        treeRef.select("eq:" + type);
        onNavLeaf(new NavTree.Leaf("eq:" + type, Component.literal(type.getPath()), 0));
    }

    private static List<Identifier> allTypesHolding(CommonMod mod) {
        LinkedHashSet<Identifier> seen = new LinkedHashSet<>();
        ItemDataRegistry reg = ItemDataRegistry.getInstance();
        for (Map.Entry<Identifier, List<WeightedList.Builder<CommonMod>>> e : reg.getModPools().entrySet()) {
            for (WeightedList.Builder<CommonMod> b : e.getValue()) {
                for (Weighted<CommonMod> w : b.build().unwrap()) {
                    if (w.value().mod().equals(mod.mod())) seen.add(e.getKey());
                }
            }
        }
        for (Map.Entry<Identifier, Map<EquipmentModPreset.Condition, List<WeightedList.Builder<CommonMod>>>> e
                : reg.getPresetModPools().entrySet()) {
            for (List<WeightedList.Builder<CommonMod>> bl : e.getValue().values()) {
                for (WeightedList.Builder<CommonMod> b : bl) {
                    for (Weighted<CommonMod> w : b.build().unwrap()) {
                        if (w.value().mod().equals(mod.mod())) seen.add(e.getKey());
                    }
                }
            }
        }
        return new ArrayList<>(seen);
    }

    /** 预设条件可读摘要（local/require/types 任一）；全空 = 无条件。 */
    private Component describeCondition(EquipmentModPreset.Condition c) {
        StringBuilder sb = new StringBuilder();
        c.local().ifPresent(l -> sb.append("·").append(l.getSerializedName()));
        c.require().ifPresent(r -> sb.append("·").append(r.getSerializedName()));
        c.types().ifPresent(ts -> ts.forEach(t -> sb.append("·").append(t.getPath())));
        return sb.length() == 0 ? Component.literal("(unconditional)") : Component.literal(sb.toString());
    }

    private Node textNode(Component c, int color) {
        Node n = new Node();
        n.with(Renderers.text(c).align(Align.LEFT).vAlign(VAlign.MIDDLE).color(color));
        return n;
    }

    private Node linesNode(List<Component> lines, int color) {
        Node n = new Node();
        TextRendererI t = Renderers.textLines(lines)
                .align(Align.LEFT).vAlign(VAlign.TOP)
                .lineSpacing(3).wrap(true).autoScale(false).color(color);
        n.with(t);
        return n;
    }

    // ------------------------------------------------------------------
    // 详情辅助（物品 tooltip 摘要）
    // ------------------------------------------------------------------

    private String idOf(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null ? id.toString() : "?";
    }

    /** 物品 tooltip 摘要：折叠成单行 Component，异常时给可读占位（不静默吞）。 */
    private Component tooltipSummary(ItemStack stack) {
        try {
            List<Component> tip = stack.getTooltipLines(
                    Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.Default.NORMAL);
            MutableJoiner joiner = new MutableJoiner();
            for (Component c : tip) joiner.add(c);
            return joiner.isEmpty() ? Component.empty() : joiner.build();
        } catch (Exception e) {
            return Component.translatable("ui.coe.codex.tooltip_unavailable");
        }
    }

    /** 轻量 Component 拼接器（tooltip 多行压成一行，用换行符分隔）。 */
    private static final class MutableJoiner {
        private final net.minecraft.network.chat.MutableComponent m = Component.empty();
        private boolean first = true;
        void add(Component c) {
            if (c == null) return;
            if (!first) m.append(Component.literal("\n"));
            m.append(c);
            first = false;
        }
        boolean isEmpty() { return first; }
        Component build() { return m; }
    }

    // ------------------------------------------------------------------
    // CodexSource 实现（数据源注册；Schema 固定一次，条目运行时动态枚举）
    // ------------------------------------------------------------------

    /**
     * 装备定义源（修复 ⚠️：装备条目 subGroupKey 恒 empty 造成的「树 / 列表语义割裂」）：
     * 装备类型叶（eq:type）选中后，中列表显示<b>该类型下的模组装备定义</b>（真实数据：
     * {@link ItemDataRegistry#getEquipmentReads()} 按类型展平，每条 read 一行），
     * 替代此前误导性的「装备组下全列原版物品」；原版物品全列保留给「物品」全部叶。
     */
    private final class EquipDefSource implements CodexSource<Map.Entry<Identifier, EquipmentRead>> {
        @Override public String id() { return "equipdef"; }
        @Override public Component displayName() { return Component.translatable("ui.coe.codex.group_equipment"); }
        @Override public String group() { return "equipment"; }
        @Override public List<Map.Entry<Identifier, EquipmentRead>> entries() {
            List<Map.Entry<Identifier, EquipmentRead>> out = new ArrayList<>();
            ItemDataRegistry reg = ItemDataRegistry.getInstance();
            for (Map.Entry<Identifier, List<EquipmentRead>> e : reg.getEquipmentReads().entrySet()) {
                for (EquipmentRead r : e.getValue()) out.add(Map.entry(e.getKey(), r));
            }
            return out;
        }
        @Override public boolean matches(Map.Entry<Identifier, EquipmentRead> entry, String query, SearchLevel level) {
            String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
            if (q.isEmpty()) return true;
            String typePath = entry.getKey().getPath().toLowerCase(Locale.ROOT);
            return switch (level) {
                case CATEGORY -> group().toLowerCase(Locale.ROOT).contains(q);
                case ENTRY -> typePath.contains(q);
                case DETAIL -> typePath.contains(q) || refOf(entry.getValue()).toLowerCase(Locale.ROOT).contains(q);
            };
        }
        @Override public Optional<String> subGroupKey(Map.Entry<Identifier, EquipmentRead> entry) {
            return Optional.of("eq:" + entry.getKey());
        }
        @Override public CodexSchema<Map.Entry<Identifier, EquipmentRead>> schema() {
            return CodexSchema.<Map.Entry<Identifier, EquipmentRead>>of()
                    .label(e -> readLabel(e.getValue()))
                    .field(Component.translatable("ui.coe.codex.col.type"),
                            e -> Component.literal(e.getKey().toString()))
                    .field(Component.translatable("ui.coe.codex.col.tip"),
                            e -> Component.literal(refOf(e.getValue())));
        }
    }

    private static String refOf(EquipmentRead r) {
        return r.ref().map(Identifier::toString).orElse("inline");
    }

    private static Component readLabel(EquipmentRead r) {
        return r.ref().map(id -> (Component) Component.literal(id.getPath()))
                .orElseGet(() -> Component.literal("inline"));
    }

    /** 装备条目源：{@link BuiltInRegistries#ITEM}（注册 id + 原生 tooltip）。二级键恒 empty（无 type 映射，不编造）。 */
    private final class ItemSource implements CodexSource<ItemStack> {
        @Override public String id() { return "item"; }
        @Override public Component displayName() { return Component.translatable("ui.coe.items"); }
        @Override public String group() { return "equipment"; }
        @Override public List<ItemStack> entries() { return allItems; }
        @Override public boolean matches(ItemStack entry, String query, SearchLevel level) {
            String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
            if (q.isEmpty()) return true;
            return switch (level) {
                case CATEGORY -> group().toLowerCase(Locale.ROOT).contains(q);
                case ENTRY -> ItemSearch.matchesInput(query, entry);
                // 内容层：原生 tooltip 摘要文本 + 注册 id
                case DETAIL -> tooltipSummary(entry).getString().toLowerCase(Locale.ROOT).contains(q)
                        || idOf(entry).contains(q);
            };
        }
        @Override public Optional<String> subGroupKey(ItemStack entry) { return Optional.empty(); }
        @Override public CodexSchema<ItemStack> schema() {
            return CodexSchema.<ItemStack>of()
                    .label(ItemStack::getHoverName)
                    .icon(st -> st)
                    .field(Component.translatable("ui.coe.codex.col.id"), st -> Component.literal(idOf(st)))
                    .field(Component.translatable("ui.coe.codex.col.tip"), CodexScreen.this::tooltipSummary);
        }
    }

    /** 技能条目源：{@link SkillEntries#REGISTRY}（inline / read 派发类型）；二级键 = 形态名。 */
    private final class SkillSource implements CodexSource<SkillEntryTypeView> {
        @Override public String id() { return "skill"; }
        @Override public Component displayName() { return Component.translatable("ui.coe.codex.skill_entries"); }
        @Override public String group() { return "skill"; }
        @Override public List<SkillEntryTypeView> entries() { return skillTypes; }
        @Override public boolean matches(SkillEntryTypeView entry, String query, SearchLevel level) {
            String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
            if (q.isEmpty()) return true;
            return switch (level) {
                // 类别层：group + 二级键（inline/read）
                case CATEGORY -> group().toLowerCase(Locale.ROOT).contains(q)
                        || subGroupKey(entry).orElse("").toLowerCase(Locale.ROOT).contains(q);
                case ENTRY -> entry.name().toLowerCase(Locale.ROOT).contains(q);
                // 内容层：注册表名 + 类型类名（既有详情字段）
                case DETAIL -> (entry.name() + " " + entry.type().getClass().getSimpleName())
                        .toLowerCase(Locale.ROOT).contains(q);
            };
        }
        @Override public Optional<String> subGroupKey(SkillEntryTypeView entry) { return Optional.of(entry.sub()); }
        @Override public CodexSchema<SkillEntryTypeView> schema() {
            return CodexSchema.<SkillEntryTypeView>of()
                    .label(v -> Component.literal(v.name()))
                    .field(Component.translatable("ui.coe.codex.col.registry"), v -> Component.literal(v.name()))
                    .field(Component.translatable("ui.coe.codex.col.clazz"),
                            v -> Component.literal(v.type().getClass().getSimpleName()));
        }
    }

    /** 附魔源（按需）：{@link ItemDataRegistry#getEnchantModifiers()}（附魔修正注册键 → Builder）。 */
    private final class EnchantSource implements CodexSource<Map.Entry<ResourceKey<Enchantment>, EnchantModifier.Builder>> {
        @Override public String id() { return "enchant"; }
        @Override public Component displayName() { return Component.translatable("ui.coe.codex.group_enchant"); }
        @Override public String group() { return "enchant"; }
        @Override public List<Map.Entry<ResourceKey<Enchantment>, EnchantModifier.Builder>> entries() { return enchantEntries; }
        @Override public boolean matches(Map.Entry<ResourceKey<Enchantment>, EnchantModifier.Builder> entry,
                                         String query, SearchLevel level) {
            String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
            if (q.isEmpty()) return true;
            String loc = entry.getKey().identifier().toString().toLowerCase(Locale.ROOT);
            return switch (level) {
                case CATEGORY -> group().toLowerCase(Locale.ROOT).contains(q);
                case ENTRY -> loc.contains(q);
                // 内容层：注册 id + 效果条数（既有详情字段）
                case DETAIL -> (loc + " additions=" + entry.getValue().effect().size()).contains(q);
            };
        }
        @Override public Optional<String> subGroupKey(Map.Entry<ResourceKey<Enchantment>, EnchantModifier.Builder> entry) {
            return Optional.empty();
        }
        @Override public CodexSchema<Map.Entry<ResourceKey<Enchantment>, EnchantModifier.Builder>> schema() {
            return CodexSchema.<Map.Entry<ResourceKey<Enchantment>, EnchantModifier.Builder>>of()
                    .label(e -> Component.literal(e.getKey().identifier().toString()))
                    .field(Component.translatable("ui.coe.codex.col.registry"),
                            e -> Component.literal(e.getKey().identifier().toString()))
                    .field(Component.translatable("ui.coe.codex.col.effect"),
                            e -> Component.literal(e.getValue().effect().size() + " additions"));
        }
    }

    /** 掉落权重源（按需）：{@link ItemDataRegistry#getTypeWeights()}（type → 权重表）。 */
    private final class DropSource implements CodexSource<Map.Entry<Identifier, Map<ItemWeight.Type, Integer>>> {
        @Override public String id() { return "drop"; }
        @Override public Component displayName() { return Component.translatable("ui.coe.codex.group_drop"); }
        @Override public String group() { return "drop"; }
        @Override public List<Map.Entry<Identifier, Map<ItemWeight.Type, Integer>>> entries() { return dropEntries; }
        @Override public boolean matches(Map.Entry<Identifier, Map<ItemWeight.Type, Integer>> entry,
                                         String query, SearchLevel level) {
            String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
            if (q.isEmpty()) return true;
            String key = entry.getKey().toString().toLowerCase(Locale.ROOT);
            return switch (level) {
                case CATEGORY -> group().toLowerCase(Locale.ROOT).contains(q);
                case ENTRY -> key.contains(q);
                // 内容层：type + 权重表（既有详情字段）
                case DETAIL -> (key + " " + entry.getValue()).contains(q);
            };
        }
        @Override public Optional<String> subGroupKey(Map.Entry<Identifier, Map<ItemWeight.Type, Integer>> entry) {
            return Optional.empty();
        }
        @Override public CodexSchema<Map.Entry<Identifier, Map<ItemWeight.Type, Integer>>> schema() {
            return CodexSchema.<Map.Entry<Identifier, Map<ItemWeight.Type, Integer>>>of()
                    .label(e -> Component.literal(e.getKey().toString()))
                    .field(Component.translatable("ui.coe.codex.col.registry"),
                            e -> Component.literal(e.getKey().toString()))
                    .field(Component.translatable("ui.coe.codex.col.effect"),
                            e -> Component.literal(e.getValue().toString()));
        }
    }

    /** SkillEntry.Type 的轻量视图（持有注册表键名，避免在泛型通配上反复取 key）。 */
    private static final class SkillEntryTypeView {
        private final SkillEntry.Type<?> type;
        private final String name;
        SkillEntryTypeView(SkillEntry.Type<?> type) {
            this.type = type;
            Identifier id = SkillEntries.REGISTRY.getKey(type);
            this.name = id != null ? id.toString() : String.valueOf(type);
        }
        SkillEntry.Type<?> type() { return type; }
        String name() { return name; }
        /** 二级分类键：{@link SkillEntries#nameOf(SkillEntry.Type)}（inline/read）。 */
        String sub() { return SkillEntries.nameOf(type); }
    }
}
