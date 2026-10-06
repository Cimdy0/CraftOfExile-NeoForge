package com.cimdy.coe.data.codec.item;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.addition.ContributionSink;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.cimdy.coe.data.codec.item.mod.core.impl.BaseMod;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import com.cimdy.coe.data.codec.item.mod.core.ModLayer;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.mod.value.ModTier;
import com.cimdy.coe.data.codec.support.stat.entry.ValueRange;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.describer.StatScopeDescriber;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.vanilla.EnchantModifier;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.meta.Labels;
import com.cimdy.coe.data.helper.affix.ModHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.*;

import static com.cimdy.coe.client.InputState.isHoldingCtrl;
import static com.cimdy.coe.client.InputState.isHoldingShift;
import static com.cimdy.coe.helper.MathHelper.df0;
import static com.cimdy.coe.helper.MathHelper.df1;
import static com.cimdy.coe.helper.MathHelper.df2;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;

/**
 * 物品 tooltip 内容拼装（原 {@code event.item.TooltipEvent} 的内联机制，铁律 #56 迁出）。
 *
 * <p>事件类只负责派发，全部渲染/文本拼装在此完成；{@link Minecraft} 引用仅出现在 tooltip 方法体内，
 * 服务端不会走到这些分支。
 */
public class ItemTooltipAssembler {
    /** 事件入口：向 tooltip 追加本模组的全部自定义内容。 */
    public static void append(ItemStack stack, Player player, List<Component> tooltip) {
        addItemModTooltip(stack, player, tooltip);
        addGemDataToolTip(stack, player, tooltip);
    }

    private static void addGemDataToolTip(ItemStack stack, Player player, List<Component> tooltip) {
        if (stack.has(DataComponentRegistry.SKILL_DATA)) {
            var gemData = stack.get(DataComponentRegistry.SKILL_DATA);
            if (gemData != null) {
                Skill skill = gemData.getSkill();
                int skillLevel = gemData.getEffectiveSkillLevel();
                if (skill != null) {
                    SkillContext context = new SkillContext(player, -1).skillLevel(skillLevel)
                            .quality(gemData.getEffectiveQuality());
                    // Labels
                    tooltip.add(Component.empty());
                    Set<String> labels = skill.getLabels(context);
                    MutableComponent labelComponent = Component.empty();
                    labels.forEach(label -> {
                        labelComponent.append("'").append(
                                Labels.getNameComponent(label)).append("'");
                    });
                    tooltip.add(labelComponent.withStyle(ChatFormatting.WHITE));
                    if (!labels.isEmpty()) tooltip.add(Component.empty());

                    // 技能等级
                    MutableComponent levelComponent =
                            Component.translatable("stat.level")
                                    .append(": ").withStyle(ChatFormatting.DARK_AQUA)
                                    .append(Component.literal(String.valueOf(skillLevel)).withStyle(ChatFormatting.WHITE));
                    if (gemData.hasExtraLevel()) {
                        levelComponent.append(Component.literal("(+" + gemData.getChangeLevel() + ")")
                                .withStyle(ChatFormatting.BLUE));
                    }
                    tooltip.add(levelComponent);

                    // 基础数据
                    skill.addDataComponent(tooltip, context);

                    List<Component> center = new ArrayList<>();

                    // 描述
                    center.add(Component.empty());
                    center.add(Component.translatable(
                            (gemData.spirit() ? "gem.spirit.description." : "gem.skill.description.") + gemData.id().getNamespace() + "." + gemData.getNameString()));

                    // 阶段数据
                    center.add(Component.empty());
                    skill.addPhaseDataComponent(center, context);

                    centerTooltips(center);
                    tooltip.addAll(center);
                }
            }
        }
    }

    private static void addItemModTooltip(ItemStack stack, Player player, List<Component> tooltip) {
        boolean showRange = isHoldingShift();
        Identifier type = ModHelper.getFirstItemType(stack);
        if (type != null && player != null) {
            Level level = player.level();
            if (stack.has(DataComponentRegistry.MODIFIABLE_STATE)) {
                EquipmentState state =ModifiableStateOps.equipment(stack);
                tooltip.add(Component.translatable("item.coe." + type.getPath())
                        .withStyle(state.rarity().getColor()));
                tooltip.add(Component.empty());
                List<ModifierEntry> mods = state.mods();
                if (state.isWeapon()) {
                    if (state.getAttackDamage() > 0) {
                        Component attackDamage = (state.attackDamageChanged() ?
                                Component.literal(df1.format(state.getAttackDamage())).withStyle(ChatFormatting.BLUE) :
                                Component.literal(df1.format(state.getAttackDamage())).withStyle(ChatFormatting.WHITE));
                        tooltip.add(Component.translatable("stat.physical")
                                .append(Component.translatable("stat.damage"))
                                .withStyle(ChatFormatting.DARK_GRAY).append(": ").append(attackDamage));
                    }
                    if (state.getAttackCritChance() > 0) {
                        Component attackCrit = (state.attackCritChanceChanged() ?
                                Component.literal(df2.format(state.getAttackCritChance() * 100) + "%").withStyle(ChatFormatting.BLUE) :
                                Component.literal(df2.format(state.getAttackCritChance() * 100) + "%").withStyle(ChatFormatting.WHITE));
                        tooltip.add(Component.translatable("stat.attack")
                                .append(Component.translatable("stat.crit"))
                                .withStyle(ChatFormatting.DARK_GRAY).append(": ").append(attackCrit));
                    }
                    if (state.getAttackSpeed() > 0) {
                        Component attackSpeed = (state.attackSpeedChanged() ?
                                Component.literal(df2.format(state.getAttackSpeed())).withStyle(ChatFormatting.BLUE) :
                                Component.literal(df2.format(state.getAttackSpeed())).withStyle(ChatFormatting.WHITE));
                        tooltip.add(Component.translatable("stat.attack")
                                .append(Component.translatable("stat.speed"))
                                .withStyle(ChatFormatting.DARK_GRAY).append(": ").append(attackSpeed));
                    }
                    if (state.getSpirit() > 0) {
                        Component mana = (state.spiritChanged() ?
                                Component.literal(df0.format(state.getSpirit())).withStyle(ChatFormatting.BLUE) :
                                Component.literal(df0.format(state.getSpirit())).withStyle(ChatFormatting.WHITE));
                        tooltip.add(Component.translatable("stat.spirit")
                                .withStyle(ChatFormatting.DARK_GRAY).append(": ").append(mana));
                    }
                    if (state.getReloadTime() > 0) {
                        Component reloadTime = (state.reloadTimeChanged() ?
                                Component.literal(df2.format(state.getReloadTime())).withStyle(ChatFormatting.BLUE) :
                                Component.literal(df2.format(state.getReloadTime())).withStyle(ChatFormatting.WHITE));
                        tooltip.add(Component.translatable("stat.reload")
                                .append(Component.translatable("stat.time"))
                                .withStyle(ChatFormatting.DARK_GRAY).append(": ").append(reloadTime));
                    }
                } else if (state.isArmor()) {
                    if (state.getArmor() > 0) {
                        Component armor = (state.armorChanged() ?
                                Component.literal(df0.format(state.getArmor())).withStyle(ChatFormatting.BLUE) :
                                Component.literal(df0.format(state.getArmor())).withStyle(ChatFormatting.WHITE));
                        tooltip.add(Component.translatable("stat.armor")
                                .withStyle(ChatFormatting.DARK_GRAY).append(": ").append(armor));
                    }
                    if (state.getEnergy() > 0) {
                        Component shield = (state.energyChanged() ?
                                Component.literal(df0.format(state.getEnergy())).withStyle(ChatFormatting.BLUE) :
                                Component.literal(df0.format(state.getEnergy())).withStyle(ChatFormatting.WHITE));
                        tooltip.add(Component.translatable("stat.energy")
                                .withStyle(ChatFormatting.DARK_GRAY).append(": ").append(shield));
                    }
                    if (state.getEvasion() > 0) {
                        Component evasion = (state.evasionChanged() ?
                                Component.literal(df0.format(state.getEvasion())).withStyle(ChatFormatting.BLUE) :
                                Component.literal(df0.format(state.getEvasion())).withStyle(ChatFormatting.WHITE));
                        tooltip.add(Component.translatable("stat.evasion")
                                .withStyle(ChatFormatting.DARK_GRAY).append(": ").append(evasion));
                    }
                }

                // 品质 / 物品孔（通用维度：品质换算在读取点，孔承载符文 / 魂核 / 其它 augment）
                if (state.quality() > 0) {
                    tooltip.add(Component.translatable("item.coe.quality")
                            .withStyle(ChatFormatting.DARK_GRAY).append(": ")
                            .append(Component.literal("+" + state.quality() + "%").withStyle(ChatFormatting.BLUE)));
                }
                if (!state.sockets().isEmpty()) {
                    tooltip.add(Component.translatable("item.coe.sockets")
                            .withStyle(ChatFormatting.DARK_GRAY).append(": ")
                            .append(Component.literal(String.valueOf(state.sockets().size()))
                                    .withStyle(ChatFormatting.WHITE)));
                }

                tooltip.add(Component.empty());
                // 等级需求显示
                if (isHoldingCtrl()) {
                    tooltip.add(Component.translatable("item.coe.level")
                            .withStyle(ChatFormatting.DARK_GRAY).append(": ").append(String.valueOf(state.level())));
                }

                state.addRequireComponent(tooltip, player);

                // 词缀 + 附魔统一渲染：按 ModLayer 分组（BASE → PREFIX → SUFFIX → RUNE），附魔独立层沉在词缀下方单独成段
                ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                renderModLayers(tooltip, mods, enchantments, level, player, isHoldingCtrl());
                centerTooltips(tooltip);
            }
        }
    }

    private static void centerTooltips(List<Component> tooltip) {
        // 计算最大宽度
        int maxWidth = tooltip.stream()
                .mapToInt(component -> Minecraft.getInstance().font.width(component))
                .max()
                .orElse(0);

        // 计算空格宽度
        int spaceWidth = Minecraft.getInstance().font.width(" ");

        // 创建新的居中对齐的tooltip列表
        List<Component> centeredTooltip = new ArrayList<>();
        for (Component component : tooltip) {
            int width = Minecraft.getInstance().font.width(component);
            int spacesNeeded = (maxWidth - width) / (2 * spaceWidth);
            if (spacesNeeded > 0) {
                centeredTooltip.add(Component.literal(" ".repeat(spacesNeeded)).append(component));
            } else {
                centeredTooltip.add(component);
            }
        }

        // 替换原有tooltip
        tooltip.clear();
        tooltip.addAll(centeredTooltip);
    }

    /**
     * 统一渲染路径：词缀按 {@link ModLayer} 分组（枚举序即渲染序：BASE → PREFIX → SUFFIX → RUNE），
     * 名称行由 {@link ModifierEntry#name()} 驱动（普通词缀出阶名词、精髓出「精髓」、基底出「基底」、符文不出名）；
     * 其后附魔作为独立层沉在词缀下方，以金色「附魔」表头区分。默认视图与 Ctrl 视图共用此路径，
     * 仅 {@code showTier} 控制普通词缀是否附带阶数。
     */
    private static void renderModLayers(List<Component> tooltip, List<ModifierEntry> mods,
                                        ItemEnchantments enchantments, Level level, Player player, boolean showTier) {
        boolean showRange = isHoldingShift();
        HolderGetter<Enchantment> getter = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        tooltip.add(Component.empty());

        // 按词缀层归类（枚举序即渲染序：BASE → PREFIX → SUFFIX → RUNE；附魔走 ENCHANTMENTS 组件独立层）
        Map<ModLayer, List<ModifierEntry>> byLayer = new LinkedHashMap<>();
        for (ModifierEntry mod : mods) {
            byLayer.computeIfAbsent(mod.layer(), k -> new ArrayList<>()).add(mod);
        }
        // 区域词缀聚合：收集每层词缀产出的 StatEntry（经 ContributionSink，与运行时 AttributeApplicator 同入口），
        // 复用 StatEngine 正规聚合求纯数值贡献（同 Stat 不同数值由引擎自动合并）。
        Map<ModLayer, StatStore> layerStores = new LinkedHashMap<>();
        for (Map.Entry<ModLayer, List<ModifierEntry>> en : byLayer.entrySet()) {
            List<StatEntry> collected = new ArrayList<>();
            CollectorSink sink = new CollectorSink(collected);
            for (ModifierEntry entry : en.getValue()) {
                for (Addition addition : entry.additions()) {
                    addition.type().contribute(sink, addition.getValue());
                }
            }
            if (!collected.isEmpty()) layerStores.put(en.getKey(), new StatStore(collected));
        }
        for (ModLayer layer : List.of(ModLayer.BASE, ModLayer.PREFIX, ModLayer.SUFFIX, ModLayer.RUNE)) {
            List<ModifierEntry> entries = byLayer.get(layer);
            if (entries == null || entries.isEmpty()) {
                continue;
            }
            for (ModifierEntry entry : entries) {
                renderEntry(tooltip, entry, showRange, showTier, getter);
            }
            // regional numeric contribution overview (grouped by Stat identity; same Stat different values summed; tag/condition gating respected; not fold)
            StatStore store = layerStores.get(layer);
            if (store != null && player != null) {
                Map<StatEngine.StatIdentity, Double> contrib = StatEngine.accumulateGroupedStat(store, player);
                if (!contrib.isEmpty()) {
                    tooltip.add(Component.translatable("item.coe.contrib.header").withStyle(ChatFormatting.GRAY));
                    for (Map.Entry<StatEngine.StatIdentity, Double> c : contrib.entrySet()) {
                        double v = c.getValue();
                        if (Math.abs(v) < 1e-9) continue;
                        tooltip.add(Component.literal("  ").append(
                                formatContribution(c.getKey(), v)).withStyle(ChatFormatting.BLUE));
                    }
                }
            }
        }

        // 附魔独立层：与原生前后缀词缀经济脱钩，始终渲染在词缀下方，附金色表头
        List<ModifierEntry> enchantMods = ModHelper.getEnchantMods(enchantments);
        if (!enchantMods.isEmpty()) {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.coe.enchantments.header").withStyle(ChatFormatting.GOLD));
            enchantMods.forEach(mod -> {
                if (mod instanceof EnchantModifier.Value enchant) {
                    if (showTier) {
                        addEnchantmentTooltip(tooltip, enchant, getter);
                    } else {
                        tooltip.add(Enchantment.getFullname(getter.getOrThrow(enchant.enchant()), enchant.level()));
                    }
                }
            });
        }
    }

    /** 渲染单条词缀：普通词缀保留阶名 + 阶数着色；其余臂名称行由 {@link ModifierEntry#name()} 驱动。 */
    private static void renderEntry(List<Component> tooltip, ModifierEntry entry,
                                    boolean showRange, boolean showTier, HolderGetter<Enchantment> getter) {
        // 基底属性由 stat.* 行与 StatEngine 折叠条目展示，不在 ModLayer 段渲染裸「基底」头。
        if (entry instanceof BaseMod) {
            return;
        }
        if (entry instanceof CommonMod nm) {
            tooltip.add(modNameComponent(nm, showTier));
            nm.additions().forEach(addition -> tooltip.add(nm.getComponent(showRange)));
            return;
        }
        // 精髓（「精髓」）/ 基底（「基底」）/ 符文（不出名）等：名称行由 name() 提供，其后逐行效果
        entry.name().ifPresent(tooltip::add);
        for (Addition addition : entry.additions()) {
            tooltip.add(describeAddition(addition, showRange));
        }
    }

    /** 通用加成描述：统一经 {@link AdditionType#describe} 单轨输出（普通词缀的着色在 {@link #modNameComponent} 处理）。 */
    private static MutableComponent describeAddition(Addition addition, boolean showRange) {
        MutableComponent comp = Component.literal("");
        Optional<ValueRange> range = showRange && addition.max().isPresent()
                ? Optional.of(new ValueRange(addition.min(), addition.max().get()))
                : Optional.empty();
        addition.type().describe(addition.getValue(), ClientLocale.get(), range).forEach(comp::append);
        return comp;
    }

    // POE 风格的词缀头：翻译键 item.mod.coe.<阶名词>，按阶数着色；Ctrl 视图附带 ModTier 数字
    private static MutableComponent modNameComponent(CommonMod itemMod, boolean showTier) {
        MutableComponent tip = Component.translatable("item.mod.coe." + itemMod.mod())
                .withStyle(itemMod.tier().getColor());
        if (showTier) {
            tip.append(" ").append(Component.translatable("item.mod.coe.tier")
                    .append(Component.literal(String.valueOf(itemMod.tier().value())))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        return tip;
    }

    /**
     * 聚合身份 -> 一行贡献文本：复用 ModifierStatEffect 的本地化效果片段（±号 / 运算口径 / 属性名）。
     * 非 fold，不渲染相反数。
     *
     * <p>外溢 / 转换作用域经 {@link StatEngine.StatIdentity} 的同名字段传入 {@link StatScopeDescriber}：
     * 聚合身份本身就把这两个字段各自成列（否则「计入 Y」与「转换为 Y」会被并成同一行），
     * 若在此处另造一个 {@code StatEntry.of(...)}，这两个字段会被丢掉、文本回退成与来源条目逐字相同。
     */
    private static Component formatContribution(StatEngine.StatIdentity identity, double value) {
        ModifierStatEffect effect = identity.effect();
        StatEntry tmp = StatEntry.of(ExpressionCodec.of(value), List.of(), effect);
        List<Component> frags = effect.describeFragment(tmp);
        if (frags.isEmpty()) return Component.empty();
        return StatScopeDescriber.wrap(frags.get(0), identity.spillTo(), identity.convertTo());
    }

    /** 预览收集端：把词缀贡献的全局 StatEntry 收集进列表（供 StatEngine 预览聚合）。 */
    private static final class CollectorSink implements ContributionSink {
        private final List<StatEntry> out;
        CollectorSink(List<StatEntry> out) { this.out = out; }
        @Override public void global(StatEntry entry) { out.add(entry); }
    }

    private static void addEnchantmentTooltip(List<Component> tooltip, EnchantModifier.Value enchantment,
                                              HolderGetter<Enchantment> getter) {
        Holder<Enchantment> holder = getter.getOrThrow(enchantment.enchant());
        int max = holder.value().getMaxLevel();
        int lv = enchantment.level();
        int tier = max + 1 - lv;
        MutableComponent enchant = holder.value().description().copy();
        if (holder.is(EnchantmentTags.CURSE)) {
            ComponentUtils.mergeStyles(enchant, Style.EMPTY.withColor(ChatFormatting.RED));
        } else {
            ComponentUtils.mergeStyles(enchant, Style.EMPTY.withColor(ChatFormatting.GRAY));
        }
        enchant = enchant.append(" ")
                .append(Component.translatable("item.mod.coe.tier")
                        .append(Component.literal(String.valueOf(tier))).withStyle(ChatFormatting.DARK_GRAY));

        List<String> labels = enchantment.labels();
        if (!labels.isEmpty()) {
            enchant.append(" ");
            for (String label : labels) {
                enchant.append("'").append(
                                Labels.getNameComponent(label)).append("'")
                        .withStyle(ChatFormatting.DARK_GRAY);
            }
        }
        tooltip.add(enchant);
        if (ModHelper.hasThisEnchantModifier(holder)) {
            enchantment.getComponent(tooltip, lv, new ModTier(max, tier).getColor());
        }
    }

    /** 追加「需求」行（等级 / 力量 / 敏捷 / 智慧），不足为红、被改动为蓝。 */
    public static void addAttributeRequirementTooltip(List<Component> toolTip, Player player, int[] reqs, boolean[] changed) {
        // 创造模式物品栏构建 tooltip 时无玩家上下文（player 为 null），跳过需求着色避免 NPE
        if (player == null) {
            return;
        }
        double[] att = getPlayerAttributes(player);
        ChatFormatting[] colors = calculateRequirementColors(reqs, att, changed);

        boolean hasRequirements = reqs[3] > 0 || reqs[0] > 0 || reqs[1] > 0 || reqs[2] > 0;
        if (!hasRequirements) {
            return;
        }

        MutableComponent component = Component.translatable("stat.require")
                .append(": ").withStyle(ChatFormatting.DARK_GRAY);

        // 添加等级需求
        component.append(Component.translatable("stat.level"))
                .append(" ")
                .append(Component.literal(String.valueOf(reqs[3])).withStyle(colors[3]));

        // 添加属性需求
        String[] attrs = {"strength", "dexterity", "intelligence"};
        for (int i = 0; i < 3; i++) {
            if (reqs[i] > 0) {
                component.append(", ")
                        .append(Component.literal(String.valueOf(reqs[i])).withStyle(colors[i]))
                        .append(" ")
                        .append(Component.translatable("stat." + attrs[i]));
            }
        }

        toolTip.add(Component.empty());
        toolTip.add(component);
    }

    private static double[] getPlayerAttributes(Player player) {
        return new double[]{
                player.getAttributeValue(AttributeRegistry.STRENGTH),
                player.getAttributeValue(AttributeRegistry.DEXTERITY),
                player.getAttributeValue(AttributeRegistry.INTELLIGENCE),
                player.getData(AttachmentRegistry.LEVEL)
        };
    }

    private static ChatFormatting[] calculateRequirementColors(int[] reqs, double[] attr, boolean[] attrChanged) {
        ChatFormatting[] colors = new ChatFormatting[4];
        for (int i = 0; i < 4; i++) {
            if (attr[i] < reqs[i]) {
                colors[i] = ChatFormatting.RED;
            } else {
                colors[i] = attrChanged[i] ? ChatFormatting.BLUE : ChatFormatting.WHITE;
            }
        }
        return colors;
    }
}
