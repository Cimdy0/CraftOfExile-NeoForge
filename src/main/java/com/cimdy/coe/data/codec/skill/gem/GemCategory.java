package com.cimdy.coe.data.codec.skill.gem;

import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.state.GemState;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.skill.support.SupportGem;
import com.cimdy.coe.data.codec.support.value.ModifierValue;
import com.cimdy.coe.event.data.LoadEvent;
import com.cimdy.coe.registry.item.ItemRegistry;
import com.cimdy.coe.item.gem.UncutGem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 宝石类别抽象 — 技能 / 辅助 / 精魂 等类型的统一注册入口。
 *
 * <p>空槽图标常量与 {@code GemIconRenderer.EMPTY} 保持一致。
 *
 * <p>新增宝石类别仅需调用 {@link #register} 注册即可，
 * {@link com.cimdy.coe.ui.screen.gem.CutScreen} 与
 * {@link com.cimdy.coe.ui.screen.gem.SkillScreen} 会自动适配。
 */
public final class GemCategory {
    private static final Identifier EMPTY_ICON = Identifier.fromNamespaceAndPath("minecraft", "air");
    private static final Map<String, GemCategory> BY_KEY = new LinkedHashMap<>();

    // ===================== 已注册类别 =====================
    // 注意：SPIRIT 必须在 SKILL 之前注册，因为精魂宝石也携带 SKILL_DATA 组件，
    // categorize() 按 LinkedHashMap 插入顺序匹配，避免 SPIRIT_GEM 被误判为 SKILL。

    public static final GemCategory SPIRIT = register(
            "spirit",
            ItemRegistry.SPIRIT_GEM,
            Component.translatable("gem.category.spirit"),
            GemCategory::buildSpiritList,
            GemCategory::isSpiritStack,
            GemCategory::spiritName,
            GemCategory::spiritIcon,
            ItemRegistry.UNCUT_SPIRIT_GEM,
            UncutGem.SKILL_TIER_MAX
    );

    public static final GemCategory SUPPORT = register(
            "support",
            ItemRegistry.SUPPORT_GEM,
            Component.translatable("gem.category.support"),
            GemCategory::buildSupportList,
            GemCategory::isSupportStack,
            GemCategory::supportName,
            GemCategory::supportIcon,
            ItemRegistry.UNCUT_SUPPORT_GEM,
            UncutGem.SUPPORT_TIER_MAX
    );

    public static final GemCategory SKILL = register(
            "skill",
            ItemRegistry.SKILL_GEM,
            Component.translatable("gem.category.skill"),
            GemCategory::buildSkillList,
            GemCategory::isSkillStack,
            GemCategory::skillName,
            GemCategory::skillIcon,
            ItemRegistry.UNCUT_GEM,
            UncutGem.SKILL_TIER_MAX
    );

    // ===================== 字段 =====================

    private final String key;
    private final Supplier<Item> resultItem;
    private final Component displayName;
    private final BiFunction<GemCategory, Integer, List<ItemStack>> listBuilder;
    private final Predicate<ItemStack> stackMatcher;
    private final Function<ItemStack, Component> nameExtractor;
    private final Function<ItemStack, Identifier> iconExtractor;
    /** 该类别对应的「未切割宝石」物品（右键打开切割界面）。 */
    private final Supplier<Item> uncutItem;
    /** 该类别未切割宝石的等阶上限（技能/精魂 1–20，辅助 1–5）。 */
    private final int maxTier;

    private GemCategory(String key, Supplier<Item> resultItem, Component displayName,
                        BiFunction<GemCategory, Integer, List<ItemStack>> listBuilder,
                        Predicate<ItemStack> stackMatcher,
                        Function<ItemStack, Component> nameExtractor,
                        Function<ItemStack, Identifier> iconExtractor,
                        Supplier<Item> uncutItem, int maxTier) {
        this.key = key;
        this.resultItem = resultItem;
        this.displayName = displayName;
        this.listBuilder = listBuilder;
        this.stackMatcher = stackMatcher;
        this.nameExtractor = nameExtractor;
        this.iconExtractor = iconExtractor;
        this.uncutItem = uncutItem;
        this.maxTier = maxTier;
    }

    // ===================== 注册 API =====================

    /**
     * 注册一个新的宝石类别。
     *
     * @param key            唯一标识（同时作为 UncutGem 的 cutType）
     * @param resultItem     切割后的产物 Item
     * @param displayName    类别显示名
     * @param listBuilder    生成可切割宝石列表
     * @param stackMatcher   判断 ItemStack 是否属于该类别
     * @param nameExtractor  从 ItemStack 提取展示名
     * @param iconExtractor  从 ItemStack 提取图标 ID
     */
    public static GemCategory register(String key, Supplier<Item> resultItem,
                                        Component displayName,
                                        BiFunction<GemCategory, Integer, List<ItemStack>> listBuilder,
                                        Predicate<ItemStack> stackMatcher,
                                        Function<ItemStack, Component> nameExtractor,
                                        Function<ItemStack, Identifier> iconExtractor,
                                        Supplier<Item> uncutItem, int maxTier) {
        GemCategory cat = new GemCategory(key, resultItem, displayName,
                listBuilder, stackMatcher, nameExtractor, iconExtractor, uncutItem, maxTier);
        BY_KEY.put(key, cat);
        return cat;
    }

    // ===================== 访问器 =====================

    public String key() { return key; }
    public Item resultItem() { return resultItem.get(); }
    public Component displayName() { return displayName; }

    /** 哨兵：传入此值表示不过滤等阶（用于创造栏 / 兼容方法）。 */
    public static final int ALL_TIERS = -1;

    /** 构建该类别下、指定等阶可切割宝石的 ItemStack 列表。 */
    public List<ItemStack> buildCutList(int tier) { return listBuilder.apply(this, tier); }

    /** 构建该类别下全部可切割宝石（不过滤等阶）。 */
    public List<ItemStack> buildCutListAll() { return listBuilder.apply(this, ALL_TIERS); }

    /** 该类别未切割宝石的等阶上限（技能/精魂 1–20，辅助 1–5）。 */
    public int maxTier() { return maxTier; }

    /** 判断 ItemStack 是否属于该类别（任何非空的此类宝石）。 */
    public boolean matches(ItemStack stack) { return stackMatcher.test(stack); }

    /** 从 ItemStack 提取展示名称（假定 stack 已通过 {@link #matches} 检查）。 */
    public Component extractName(ItemStack stack) { return nameExtractor.apply(stack); }

    /** 从 ItemStack 提取图标 ID（假定 stack 已通过 {@link #matches} 检查）。 */
    public Identifier extractIcon(ItemStack stack) { return iconExtractor.apply(stack); }

    // ===================== 静态工具方法 =====================

    public static GemCategory byKey(String key) {
        return BY_KEY.getOrDefault(key, SKILL);
    }

    /** 所有已注册类别（按注册顺序排列）。 */
    public static List<GemCategory> values() {
        return List.copyOf(BY_KEY.values());
    }

    /** 判断 ItemStack 是否属于任一宝石类别。 */
    public static Optional<GemCategory> categorize(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        for (GemCategory cat : BY_KEY.values()) {
            if (cat.matches(stack)) return Optional.of(cat);
        }
        return Optional.empty();
    }

    /**
     * 从宝石物品提取 gem id（SKILL_DATA / SUPPORT_DATA 组件的 {@code id()}；非宝石返回 null）。
     * 供切割协议按 id 传递目标（服务端按 id 在可切割列表中反查重建，避免客户端传索引在搜索态下错位）。
     */
    public static @org.jspecify.annotations.Nullable Identifier extractGemId(ItemStack stack) {
        if (stack.isEmpty()) return null;
        SkillData sd = stack.get(DataComponentRegistry.SKILL_DATA);
        if (sd != null) return sd.id();
        SupportData spd = stack.get(DataComponentRegistry.SUPPORT_DATA);
        return spd != null ? spd.id() : null;
    }

    /**
     * 跨颜色在 {@link LoadEvent#ITEM_SUPPORT_GEM_MAP} 中按 id 反查辅助宝石定义；未找到返回 null。
     * 供 {@link SupportData#toItemStack()} 等运行时点取辅助宝石等阶（tier）以写入统一物品状态。
     */
    public static @org.jspecify.annotations.Nullable SupportGem getSupportGem(Identifier id) {
        for (Map<Identifier, SupportGem> inner : LoadEvent.ITEM_SUPPORT_GEM_MAP.values()) {
            SupportGem g = inner.get(id);
            if (g != null) return g;
        }
        return null;
    }

    // ===================== 各类型的内部实现 =====================

    // --- Skill ---
    private static List<ItemStack> buildSkillList(GemCategory self, int tier) {
        List<ItemStack> list = new ArrayList<>();
        for (Map<Identifier, SkillEntry> inner : LoadEvent.ITEM_MAP.values())
            for (Map.Entry<Identifier, SkillEntry> e : inner.entrySet()) {
            Identifier id = e.getKey();
            Skill skill = e.getValue().resolve();
            // 等阶过滤：未切割 N 阶只能切出等阶 N 的技能；结果等级 = N（技能最小等级）
            if (tier != ALL_TIERS && skill.tier() != tier) continue;
            SkillData sd = SkillData.builder(id)
                    .level(ModifierValue.Int.create(tier))
                    .quality(ModifierValue.Int.create(0))
                    .spirit(false)
                    .build();
            ItemStack stack = new ItemStack(self.resultItem());
            stack.set(DataComponents.ITEM_MODEL, sd.getItemModel());
            stack.set(DataComponents.ITEM_NAME, sd.getName());
            stack.set(DataComponentRegistry.SKILL_DATA, sd);
            list.add(stack);
        }
        return list;
    }

    private static boolean isSkillStack(ItemStack stack) {
        SkillData sd = stack.get(DataComponentRegistry.SKILL_DATA);
        return sd != null && !sd.isEmpty();
    }

    private static Component skillName(ItemStack stack) {
        SkillData sd = stack.get(DataComponentRegistry.SKILL_DATA);
        return sd != null ? sd.getName() : Component.literal("?");
    }

    private static Identifier skillIcon(ItemStack stack) {
        SkillData sd = stack.get(DataComponentRegistry.SKILL_DATA);
        return sd != null ? sd.getIcon() : EMPTY_ICON;
    }

    // --- Support ---
    private static List<ItemStack> buildSupportList(GemCategory self, int tier) {
        List<ItemStack> list = new ArrayList<>();
        for (Map<Identifier, SupportGem> inner : LoadEvent.ITEM_SUPPORT_GEM_MAP.values())
            for (Map.Entry<Identifier, SupportGem> e : inner.entrySet()) {
            Identifier supId = e.getKey();
            SupportGem supportGem = e.getValue();
            // 等阶过滤：未切割 N 阶只能切出等阶 N 的辅助；等级由等阶->等级映射推导
            if (tier != ALL_TIERS && supportGem.tier() != tier) continue;
            int level = supportGem.level();
            SupportData spd = SupportData.builder(supId)
                    .level(ModifierValue.Int.create(level))
                    .build();
            ItemStack stack = new ItemStack(self.resultItem());
            stack.set(DataComponents.ITEM_MODEL, spd.id());
            stack.set(DataComponents.ITEM_NAME, SupportGem.getDisplayName(spd.id()));
            stack.set(DataComponentRegistry.SUPPORT_DATA, spd);
            // 同时写入统一物品状态（itemdata），与运行时 toItemStack 一致
            stack.set(DataComponentRegistry.MODIFIABLE_STATE.get(), GemState.create(List.of(), supportGem.tier(), 0, List.of()));
            list.add(stack);
        }
        return list;
    }

    private static boolean isSupportStack(ItemStack stack) {
        SupportData spd = stack.get(DataComponentRegistry.SUPPORT_DATA);
        return spd != null && !spd.isEmpty();
    }

    private static Component supportName(ItemStack stack) {
        SupportData spd = stack.get(DataComponentRegistry.SUPPORT_DATA);
        return spd != null && !spd.isEmpty()
                ? SupportGem.getDisplayName(spd.id())
                : Component.literal("?");
    }

    private static Identifier supportIcon(ItemStack stack) {
        SupportData spd = stack.get(DataComponentRegistry.SUPPORT_DATA);
        return spd != null && !spd.isEmpty()
                ? SupportGem.getIcon(spd.id())
                : EMPTY_ICON;
    }

    // --- Spirit ---
    // 精魂宝石使用独立的 ITEM_SPIRIT_MAP，存储 SKILL_DATA 组件（与技能宝石共用同一数据格式）。
    private static List<ItemStack> buildSpiritList(GemCategory self, int tier) {
        List<ItemStack> list = new ArrayList<>();
        for (Map<Identifier, SkillEntry> inner : LoadEvent.ITEM_SPIRIT_MAP.values())
            for (Map.Entry<Identifier, SkillEntry> e : inner.entrySet()) {
            Identifier id = e.getKey();
            Skill skill = e.getValue().resolve();
            // 等阶过滤：未切割 N 阶只能切出等阶 N 的精魂；结果等级 = N
            if (tier != ALL_TIERS && skill.tier() != tier) continue;
            SkillData sd = SkillData.builder(id)
                    .level(ModifierValue.Int.create(tier))
                    .quality(ModifierValue.Int.create(0))
                    .spirit(true)
                    .build();
            ItemStack stack = new ItemStack(self.resultItem());
            stack.set(DataComponents.ITEM_MODEL, sd.getItemModel());
            stack.set(DataComponents.ITEM_NAME, sd.getName());
            stack.set(DataComponentRegistry.SKILL_DATA, sd);
            list.add(stack);
        }
        return list;
    }

    private static boolean isSpiritStack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        // 检查物品本体是否为 SPIRIT_GEM，并且携带有效的 SKILL_DATA
        Item spiritItem = ItemRegistry.SPIRIT_GEM.get();
        if (spiritItem == null || !stack.is(spiritItem)) return false;
        SkillData sd = stack.get(DataComponentRegistry.SKILL_DATA);
        return sd != null && !sd.isEmpty();
    }

    private static Component spiritName(ItemStack stack) {
        SkillData sd = stack.get(DataComponentRegistry.SKILL_DATA);
        return sd != null && !sd.isEmpty()
                ? sd.getName()
                : Component.literal("?");
    }

    private static Identifier spiritIcon(ItemStack stack) {
        SkillData sd = stack.get(DataComponentRegistry.SKILL_DATA);
        return sd != null && !sd.isEmpty() ? sd.getIcon() : EMPTY_ICON;
    }
}
