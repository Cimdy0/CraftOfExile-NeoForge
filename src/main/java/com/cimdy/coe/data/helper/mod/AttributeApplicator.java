package com.cimdy.coe.data.helper.mod;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.helper.stat.AttributeLimiterHelper;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.addition.ContributionSink;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.cimdy.coe.data.codec.item.mod.core.impl.BaseMod;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.vanilla.EnchantModifier;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.cimdy.coe.data.codec.item.mod.core.impl.AffixMod;
import com.cimdy.coe.data.codec.item.mod.core.ModLayer;
/**
 * 装备属性应用器：把全部词缀效果统一产出为 {@code Map<String, StatEntry>}（key = sourcePrefix + "#" + 序号），
 * 存入组件 {@code ITEM_STAT_ENTRIES}；穿戴时由 AttributeLimiterHelper 注入实体 StatStore，
 * 由 StatEngine.fold 聚合（读取端零改动）。
 *
 * <p>消费端唯一入口：多态分派。本消费器不反推 AdditionType 的运行时种类，也不对具体 StatEffect 子类
 * 做 instanceof 判别——语义完全由 AdditionType 实现类承载。</p>
 *
 * <p>装备固有基础属性由 {@code EquipmentRead} 模板驱动：{@code calculateStatEntries} 在词缀条目之外，额外把
 * {@code state.equipment().baseStatEntries()}（每个模板字段 → 一条 StatRead(ref, value)）一并注入
 * {@code ITEM_STAT_ENTRIES}。装备固有属性与词缀统一以引用型 StatEntry 落 StatStore。</p>
 *
 * <p>载体通用性：词缀读取 / 写回一律经 {@link ModifiableStateOps} 按组件分派，故装备 / 药剂 / 护符
 * 共享同一条词缀 → StatEntry 管线；仅「基底属性条目」一项为装备专有（药剂 / 护符无 EquipmentRead）。</p>
 */
public final class AttributeApplicator {

    private AttributeApplicator() {}

    // ------------------------------------------------------------ 读取端（tooltip 等复用）

    /**
     * 词条按<b>层</b>（{@link ModLayer}）分桶：下缀归 {@code PREFIX} / {@code SUFFIX}，符文归 {@code RUNE}，
     * vanilla 附魔归 {@code ENCHANT}，基底 {@code BASE} 由 {@link #baseEntriesOf} 单独取（本表不收）。
     *
     * <p><b>为什么键是 {@code ModLayer} 而不是 {@link Location}</b>：早期这张表的键是「落位」，
     * 而附魔不是词缀、占不了前后缀档位，于是借一个 {@code Location.ENCHANT} 常量当键把它塞进来 ——
     * 「层」与「位置」两个语义从此共用一个类型，符文加成（{@code RUNE}）与附魔也被混为一谈。
     * 现在 {@link Location} 只剩两个前后缀常量，分层归 {@link ModLayer}，本表自然改用层做键：
     * 键类型与桶里装的的东西终于同构。</p>
     *
     * <p>本表是<b>全集</b>（含符文与附魔）—— 写回（{@link #setAllMods}）要从这里还原全部条目，
     * 漏掉一层就是丢数据；只有「前后缀档位计数」才需要 {@link #locationBuckets} 那种按落位看的视图。</p>
     */
    public static Map<ModLayer, List<ModifierEntry>> combineModsAndEnchantments(ItemStack stack) {
        // 词缀为共享维度：装备 / 药剂 / 护符经通用载体分派读取，不再硬编码 MODIFIABLE_STATE 组件。
        Map<ModLayer, List<ModifierEntry>> byLayer = new LinkedHashMap<>();
        for (ModifierEntry mod : ModifiableStateOps.mods(stack)) {
            ModLayer layer = layerOf(mod);
            if (layer == null) {
                continue; // 无层的条目（基底等）不占档位，由 baseEntriesOf 单独处理
            }
            byLayer.computeIfAbsent(layer, k -> new ArrayList<>()).add(mod);
        }
        List<ModifierEntry> enchantMods = EnchantIntegrator.getEnchantMods(
                stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
        if (!enchantMods.isEmpty()) {
            byLayer.put(ModLayer.ENCHANT, new ArrayList<>(enchantMods));
        }
        return byLayer;
    }

    /** vanilla 附魔条目：<b>平铺列表</b>（附魔只有一个层 {@code ModLayer.ENCHANT}，用不上键）。 */
    public static List<ModifierEntry> enchantMods(ItemStack stack) {
        return EnchantIntegrator.getEnchantMods(
                stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
    }

    /**
     * 只含前后缀落位桶的<b>视图</b>（键仍是 {@link Location}，与 {@link com.cimdy.coe.data.codec.item.mod.value.ModNumber}
     * 的「档位上限」口径一致）：符文 {@code RUNE} 与附魔 {@code ENCHANT} 层<b>不计入</b>前后缀档位。
     */
    public static Map<Location, List<ModifierEntry>> locationBuckets(ItemStack stack) {
        Map<Location, List<ModifierEntry>> out = new LinkedHashMap<>();
        out.put(Location.PREFIX, new ArrayList<>());
        out.put(Location.SUFFIX, new ArrayList<>());
        combineModsAndEnchantments(stack).forEach((layer, mods) -> {
            Location loc = locationOf(layer);
            if (loc != null && !mods.isEmpty()) {
                out.get(loc).addAll(mods);
            }
        });
        return out;
    }

    /** 层 → 落位：只有下缀两层有落位；其余层（符文 / 附魔 / 基底）无落位。 */
    private static Location locationOf(ModLayer layer) {
        return switch (layer) {
            case ModLayer.PREFIX -> Location.PREFIX;
            case ModLayer.SUFFIX -> Location.SUFFIX;
            default -> null;
        };
    }

    /** 条目 → 层：下缀取自身落位，其余取 {@link ModifierEntry#layer()}；无层（基底）返回 {@code null}。 */
    private static ModLayer layerOf(ModifierEntry mod) {
        if (mod instanceof AffixMod affix) {
            return switch (affix.location()) {
                case Location.SUFFIX -> ModLayer.SUFFIX;
                default -> ModLayer.PREFIX;
            };
        }
        if (mod.layer() == ModLayer.BASE || mod.layer() == ModLayer.ENCHANT) {
            return null;
        }
        return mod.layer();
    }

    // ------------------------------------------------------------ 词缀生成：产出 StatEntry

    public static void applyModValue(ItemStack stack, String sourcePrefix) {
        Map<String, StatEntry> entries = calculateStatEntries(stack, sourcePrefix);
        stack.set(DataComponentRegistry.ITEM_STAT_ENTRIES, entries);
        hideVanillaTooltip(stack);
    }

    /**
     * 兼容旧签名：无显式来源前缀时按载体推导（仅保留 tooltip 等非注入用途）。
     * 装备用槽位名（与 EquipGuard / StatStore 源前缀口径一致）；药剂 / 护符等用载体类名。
     */
    public static void applyModValue(ItemStack stack) {
        ModifiableState state = ModifiableStateOps.get(stack).orElse(null);
        String carrier = state instanceof EquipmentState equipment
                ? equipment.getSlot().getSerializedName()
                : (state == null ? "none" : state.getClass().getSimpleName().toLowerCase(Locale.ROOT));
        applyModValue(stack, "equip:" + carrier + ":" + System.identityHashCode(stack));
    }

    /**
     * 核心：把全部词缀效果产出为 Map<String, StatEntry>（key = sourcePrefix + "#" + 序号）。
     * 遍历全部 ModifierEntry，把每条 Addition 的 AdditionType 交给 sink 多态写入——本方法
     * 不读取任何运行时种类标记（无 kind() / 无 instanceof StatEffect），语义完全由实现类承载。
     * 装备固有基础属性（EquipmentRead 模板展开）作为 StatRead 条目一并注入，与词缀统一落 StatStore。
     */
    private static Map<String, StatEntry> calculateStatEntries(
            ItemStack stack, String sourcePrefix) {
        Map<String, StatEntry> result = new LinkedHashMap<>();
        // 基底属性仅装备具备（EquipmentRead 模板展开）；药剂 / 护符只有词缀条目。
        EquipmentState equipment = ModifiableStateOps.equipment(stack);

        EquipmentSink sink = new EquipmentSink();

        // 多态贡献：全局/局部（现已等价全局）全部经 AdditionType 实现类分派到 sink.global。
        // 词缀与附魔在这里是「并列的两串」—— 附魔不是词缀，只是同样要贡献效果。
        for (List<ModifierEntry> mods : combineModsAndEnchantments(stack).values()) {
            for (ModifierEntry entry : mods) {
                for (Addition addition : entry.additions()) {
                    addition.type().contribute(sink, addition.getValue());
                }
            }
        }

        int idx = 0;
        // 词缀条目：按收集顺序编号落 result。
        for (StatEntry e : sink.globals()) {
            result.put(sourcePrefix + "#" + (idx++), e);
        }
        // 装备固有基础条目：优先从 mods 中的 BaseMod 取（phase 3a 后基底作为 BaseMod 入 mods），
        // 旧存档无 BaseMod 时回退到 EquipmentRead 模板展开，保证兼容。
        List<StatEntry> baseEntries = baseEntriesOf(stack, equipment);
        for (StatEntry e : baseEntries) {
            result.put(sourcePrefix + "#" + (idx++), e);
        }
        return result;
    }

    /** 从 mods 中的 {@link BaseMod} 取基底条目；无则回退 EquipmentRead 模板展开（旧存档兼容）。 */
    private static List<StatEntry> baseEntriesOf(ItemStack stack, EquipmentState equipment) {
        for (ModifierEntry entry : ModifiableStateOps.mods(stack)) {
            if (entry instanceof BaseMod base) {
                return base.entries();
            }
        }
        return equipment == null
                ? List.of()
                : equipment.equipment().map(eq -> eq.baseStatEntries(equipment.quality())).orElse(List.of());
    }

    private static void hideVanillaTooltip(ItemStack stack) {
        TooltipDisplay tooltipDisplay = TooltipDisplay.DEFAULT
                .withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true)
                .withHidden(DataComponents.ENCHANTMENTS, true);
        stack.set(DataComponents.TOOLTIP_DISPLAY, tooltipDisplay);
    }

    /** 词缀重设：拆回词缀列表 + 附魔，写回组件后重算 Stat 条目。 */
    static void setAllMods(
            net.minecraft.world.level.Level level, ItemStack stack,
            Map<ModLayer, List<ModifierEntry>> allMods) {
        List<ModifierEntry> newMods = new ArrayList<>();
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        allMods.forEach((k, v) -> v.forEach(entry -> {
            // 附魔（vanilla 共生层）转回 ENCHANTMENTS 组件；其余臂一律按多态词缀写回 mods。
            if (entry instanceof EnchantModifier.Value value) {
                value.applyTo(level, mutable);
            } else {
                newMods.add(entry);
            }
        }));
        ModifiableStateOps.setMods(stack, newMods);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        applyModValue(stack);
    }

    // ------------------------------------------------------------ 贡献接收端（多态落点）

    /**
     * 装备贡献接收端：仅收集全局条目（局部词缀现已等价全局，统一经 sink.global 进入角色 StatStore）。
     * 这是「消费者不感知种类标签」的落点——AdditionType 实现类决定调用 global，本类只负责归集。
     */
    private static final class EquipmentSink implements ContributionSink {

        private final List<StatEntry> globals = new ArrayList<>();
        private boolean veiled;

        @Override
        public void global(StatEntry entry) {
            globals.add(entry);
        }

        @Override
        public boolean revealed() {
            return true;
        }

        @Override
        public void markVeiled() {
            veiled = true;
        }

        List<StatEntry> globals() {
            return globals;
        }

        boolean veiled() {
            return veiled;
        }
    }
}
