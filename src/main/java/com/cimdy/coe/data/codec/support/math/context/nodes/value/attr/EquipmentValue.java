package com.cimdy.coe.data.codec.support.math.context.nodes.value.attr;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.CompareValue;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.network.chat.Component;
import java.util.Locale;
import com.cimdy.coe.data.codec.support.math.context.query.AmountSourceNamer;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;

/**
 * 装备读值节点（合并组 C：原 {@code EquipmentValue} + {@code EquipmentStatValue} + {@code EquipmentMatchValue}）。
 * 在限定（或全部）装备槽内按可选 {@link ItemPredicate} 过滤，由 {@code aggregate} 区分输出：
 * <ul>
 *   <li>{@code FIRST}（默认，原 equipment_match，仅 JSON 形式）—— 首个命中物品，按 {@code output} 读出数值
 *       （count / level / mods / item_level / require_level / armor / evasion / energy / *\_resistance），未指定 output → 1.0；</li>
 *   <li>{@code COUNT}（原 equipment，中缀 {@code equipment:<slot>}）—— 命中物品数量（单槽 → 1/0，无槽 → 已装备计数）；</li>
 *   <li>{@code SUM}（原 equipment_stat，中缀 {@code equipment_stat:<slot>.<stat>}）—— 命中物品的 {@code output} 数值求和
 *       （单槽 → 该槽值，无槽 → 全槽求和）。</li>
 * </ul>
 * 槽名统一按 {@link EquipmentSlot#getName()}（如 {@code head}）；中缀用字符串槽名，未知槽 → 0（兼容旧行为）。
 */
public record EquipmentValue(
        Optional<List<EquipmentSlot>> slots,
        Optional<ItemPredicate> item,
        Optional<String> output,
        Aggregate aggregate
) implements EntityNode {

    public EquipmentValue {
        if (aggregate == null) aggregate = Aggregate.FIRST;
    }

    /** 缺省检查的装备槽集合（全部 6 槽，与原三节点一致）。 */
    public static final List<EquipmentSlot> ALL_SLOTS = List.of(
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    /**
     * 从物品读出数值的取值器表（key → 从 {@link ItemStack} 提取 double）。
     * 合并自原 EquipmentMatchValue.EQUIPMENT_OUTPUTS 与 EquipmentStatValue.readStat；
     * 凡新增装备存储数值，直接在此追加一行即可。
     */
    private static final Map<String, Function<ItemStack, Double>> EQUIPMENT_OUTPUTS = Map.ofEntries(
            Map.entry("count", s -> (double) s.getCount()),
            Map.entry("level", s -> (double) s.getOrDefault(DataComponentRegistry.LEVEL.get(), 0)),
            Map.entry("mods", s -> {
                EquipmentState st =ModifiableStateOps.equipment(s);
                return st == null ? 0.0
                        : (double) st.mods().size();
            }),
            Map.entry("item_level", s -> {
                EquipmentState st =ModifiableStateOps.equipment(s);
                return st == null ? 0.0 : (double) st.level();
            }),
            Map.entry("require_level", s -> {
                EquipmentState st =ModifiableStateOps.equipment(s);
                return st == null ? 0.0 : (double) st.getRequireLevel();
            }),
            Map.entry("armor", s -> itemState(s).getArmor()),
            Map.entry("evasion", s -> itemState(s).getEvasion()),
            Map.entry("energy", s -> itemState(s).getEnergy()),
            // 归一：抗性统一在 RESISTANCES 单一属性（元素区分靠 Stat 标签作用域，不在此 EquipmentState 路径暴露）
            Map.entry("fire_resistance", s -> sumResistance(s, AttributeRegistry.RESISTANCES)),
            Map.entry("cold_resistance", s -> sumResistance(s, AttributeRegistry.RESISTANCES)),
            Map.entry("lightning_resistance", s -> sumResistance(s, AttributeRegistry.RESISTANCES)),
            Map.entry("light_resistance", s -> sumResistance(s, AttributeRegistry.RESISTANCES)),
            Map.entry("chaos_resistance", s -> sumResistance(s, AttributeRegistry.RESISTANCES)),
            Map.entry("strength_requirement", s -> itemRequire(s, "str")),
            Map.entry("dexterity_requirement", s -> itemRequire(s, "dex")),
            Map.entry("intelligence_requirement", s -> itemRequire(s, "int"))
    );

    /**
     * 懒初始化编解码器：{@link ItemPredicate#CODEC} 的静态初始化需要 Minecraft 注册表 bootstrap，
     * 若作为 {@code static final} 字段在类加载时立即构造，纯 JUnit 环境（未 bootstrap）会因
     * {@code IllegalArgumentException: Not bootstrapped} 导致整个 {@code EquipmentValue} 类无法加载。
     * 改为惰性 holder（首次访问时才构造），与运行期解耦，datagen / 序列化路径照常可用。
     */
    public static Codec<EquipmentValue> codec() {
        return CodecHolder.CODEC;
    }

    private static final class CodecHolder {
        private static final Codec<EquipmentValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EquipmentSlot.CODEC.listOf().optionalFieldOf("slots").forGetter(EquipmentValue::slots),
                ItemPredicate.CODEC.optionalFieldOf("item").forGetter(EquipmentValue::item),
                Codec.STRING.optionalFieldOf("output").forGetter(EquipmentValue::output),
                Aggregate.CODEC.optionalFieldOf("aggregate", Aggregate.FIRST).forGetter(EquipmentValue::aggregate)
        ).apply(instance, EquipmentValue::new));
    }

    public enum Aggregate implements StringRepresentable {
        FIRST("first"), COUNT("count"), SUM("sum");

        public static final Codec<Aggregate> CODEC = StringRepresentable.fromEnum(Aggregate::values);

        private final String name;

        Aggregate(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        List<EquipmentSlot> toCheck = slots.orElse(ALL_SLOTS);
        return switch (aggregate) {
            case SUM -> {
                double total = 0D;
                for (EquipmentSlot s : toCheck) {
                    ItemStack stack = living.getItemBySlot(s);
                    if (stack.isEmpty() || (item.isPresent() && !item.get().test(stack))) continue;
                    total += outputValue(stack, output.orElse(""));
                }
                yield total;
            }
            case COUNT -> {
                int n = 0;
                for (EquipmentSlot s : toCheck) {
                    ItemStack stack = living.getItemBySlot(s);
                    if (!stack.isEmpty() && (item.isEmpty() || item.get().test(stack))) n++;
                }
                yield n;
            }
            default -> { // FIRST
                for (EquipmentSlot s : toCheck) {
                    ItemStack stack = living.getItemBySlot(s);
                    if (stack.isEmpty() || (item.isPresent() && !item.get().test(stack))) continue;
                    if (output.isEmpty()) yield 1.0;
                    Function<ItemStack, Double> fn = EQUIPMENT_OUTPUTS.get(output.get().toLowerCase());
                    yield fn != null ? fn.apply(stack) : 0.0;
                }
                yield 0.0;
            }
        };
    }

    private static double outputValue(ItemStack stack, String key) {
        if (key.isEmpty()) return 0D;
        Function<ItemStack, Double> fn = EQUIPMENT_OUTPUTS.get(key.toLowerCase());
        return fn != null ? fn.apply(stack) : 0D;
    }

    /** 读物品某装备扁平值（armor/evasion/energy/.../strength_requirement 等），供饰品栏读点复用（R3：同一组真实读点）。 */
    public static double readOutput(ItemStack stack, String key) {
        return outputValue(stack, key);
    }

    /** 读物品属性需求（strength/dexterity/intelligence 需求修饰的最终值）。 */
    private static double itemRequire(ItemStack stack, String key) {
        EquipmentState st =ModifiableStateOps.equipment(stack);
        ExpressionCodec.Value v = st.base().reqs().get(key);
        return v == null ? 0.0 : v.value().left().orElse(0.0);
    }

    private static EquipmentState itemState(ItemStack stack) { return ModifiableStateOps.equipment(stack);
    }

    /** 元素抗性：物品 ATTRIBUTE_MODIFIERS 中对应属性所有修饰 amount 之和（不过滤槽位，单物品仅作用于自身槽）。 */
    private static double sumResistance(ItemStack stack, Holder<Attribute> attribute) {
        ItemAttributeModifiers mods = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        double sum = 0D;
        for (var e : mods.modifiers()) {
            if (e.attribute().is(attribute)) sum += e.modifier().amount();
        }
        return sum;
    }

    /** 大小写不敏感槽名解析（兼容旧 equipment/equipment_stat 的字符串槽名）；未知返回空。 */
    public static Optional<EquipmentSlot> parseSlot(String name) {
        for (EquipmentSlot s : EquipmentSlot.values()) {
            if (s.getName().equalsIgnoreCase(name)) return Optional.of(s);
        }
        return Optional.empty();
    }

    /** {@code equipment:<slot>} 中缀构造：空路径 → 全槽计数；已知槽 → 单槽；未知槽 → 空列表（求值恒 0，兼容旧行为）。 */
    public static EquipmentValue fromInfixSlot(String name) {
        if (name.isEmpty()) return new EquipmentValue(Optional.empty(), Optional.empty(), Optional.empty(), Aggregate.COUNT);
        return parseSlot(name)
                .<EquipmentValue>map(s -> new EquipmentValue(Optional.of(List.of(s)), Optional.empty(), Optional.empty(), Aggregate.COUNT))
                .orElseGet(() -> new EquipmentValue(Optional.of(List.of()), Optional.empty(), Optional.empty(), Aggregate.COUNT));
    }

    /**
     * 名词短语（P4）：作为 {@link CompareValue} 操作数时，产出「槽位 + 输出项」可读名词短语
     * （如 "Head Armor"），而非可见降级到 "[?]"。复用 {@link AmountSourceNamer} 已建立的键化映射。
     */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(AmountSourceNamer.name(this));
    }
}
