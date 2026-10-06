package com.cimdy.coe.data.helper.mod;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.cimdy.coe.data.codec.item.mod.pipeline.BenchStage;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.cimdy.coe.data.codec.item.mod.pipeline.ModifierContext;
import com.cimdy.coe.data.codec.item.mod.pipeline.ModifierPipeline;
import com.cimdy.coe.data.codec.item.mod.pipeline.ModifierStage;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import com.cimdy.coe.data.codec.vanilla.EnchantModifier;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 词缀候选池构建：把「物品栈」翻译为一条 {@link ModifierContext} 并跑 {@link ModifierPipeline}。
 *
 * <p>载体无关：状态经 {@link ModifiableStateOps} 读取（装备 / 药剂 / 护符共享 {@link ModifiableState} 契约），
 * 故三类载体走同一条池构建路径；无状态载体的栈直接返回空池（不构造上下文）。</p>
 */
public final class ModPoolBuilder {

    private ModPoolBuilder() {}

    public static Map<Location, WeightedList.Builder<CommonMod>> getModBuilder(ItemStack stack, List<Location> types) {
        ModifiableState state = ModifiableStateOps.get(stack).orElse(null);
        if (state == null) {
            return Map.of();
        }
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        List<String> excludedList = stack.getOrDefault(DataComponentRegistry.ITEM_CRAFT_TAGS, List.of());
        Set<String> excludedTags = new HashSet<>(excludedList);
        List<Location> location = ModGenerator.getNotMaxedLocations(ModGenerator.findMatchedNumber(stack), types, stack);
        return finish(ModGenerator.getItemType(stack), location, state, enchantments, excludedTags);
    }

    /**
     * 分阶下限重载：在基础候选池上剔除 tier 低于 {@code minTier} 的词缀（{@code minTier<=0} 不生效）。
     * 用于「保障词条不低于某阶」的通货（如崇高石保底高档、改造石下限），不影响共享管线。
     */
    public static Map<Location, WeightedList.Builder<CommonMod>> getModBuilder(ItemStack stack, List<Location> types, int minTier) {
        Map<Location, WeightedList.Builder<CommonMod>> builders = getModBuilder(stack, types);
        if (minTier > 0) {
            builders.forEach((loc, builder) -> builder.removeIf(m -> m.value().level() < minTier));
        }
        return builders;
    }

    /** 组装修饰管线并去重附魔，返回以 location 为键的候选词缀池。 */
    private static Map<Location, WeightedList.Builder<CommonMod>> finish(
            List<Identifier> types, List<Location> location, ModifiableState state,
            ItemEnchantments enchantments, Set<String> excludedTags) {
        ModifierContext ctx = new ModifierContext(types, location, state, excludedTags);
        List<ModifierStage> stages = new ArrayList<>();
        stages.add(new BenchStage());
        Map<Location, WeightedList.Builder<CommonMod>> builders = ModifierPipeline.build(ctx, stages);

        // 移除与附魔重复的词缀：经 Addition 身份（注册路径 AdditionRead.ref）对照 —— 同一 AdditionRead 引用即同效，剔除候选。
        // 附魔不是词缀、不占前后缀落位，故这里对<b>候选池自身的落位</b>（PREFIX / SUFFIX）逐个剔除，
        // 而非去附魔侧找一个「落位桶」（附魔没有落位，那样的查找恒为空、剔除也就恒不生效）。
        Set<Identifier> enchIds = enchantmentIds(EnchantIntegrator.getEnchantMods(enchantments));
        if (!enchIds.isEmpty()) {
            for (Location type : location) {
                WeightedList.Builder<CommonMod> builder = builders.get(type);
                if (builder != null) {
                    builder.removeIf(m -> m.value().addition().identity().map(enchIds::contains).orElse(false));
                    if (builder.build().isEmpty()) {
                        builders.remove(type);
                    }
                }
            }
        }
        return builders;
    }

    /** 附魔效果的全部 Addition 身份（注册路径 {@code AdditionRead.ref}）：同一引用即同一效果。 */
    private static Set<Identifier> enchantmentIds(List<ModifierEntry> enchantMods) {
        Set<Identifier> ids = new HashSet<>();
        for (ModifierEntry mod : enchantMods) {
            if (mod instanceof EnchantModifier.Value value) {
                value.effects().stream()
                        .map(Addition::identity)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(ids::add);
            }
        }
        return ids;
    }
}
