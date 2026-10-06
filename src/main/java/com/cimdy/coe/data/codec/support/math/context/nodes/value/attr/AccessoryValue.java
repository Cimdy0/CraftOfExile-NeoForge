package com.cimdy.coe.data.codec.support.math.context.nodes.value.attr;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.CompareValue;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.item.AccessorySlotRegistry;
import com.cimdy.coe.data.codec.item.AccessorySave;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import net.minecraft.network.chat.Component;
import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.support.math.context.query.AmountSourceNamer;

/**
 * 饰品栏读值节点（{@code accessory_stat:<group>.<stat>}）。
 *
 * <p>读取饰品栏某分组（legs / quiver / amulet 等，由 {@code config/coe/slots.json} 定义，无配置走内置兜底）内
 * 物品的数值，复用 {@link EquipmentValue#readOutput} 的同一组真实读点（armor / evasion / energy /
 * strength_requirement …），因此落点与装备读值完全一致，不造死线（R3）。
 *
 * <p>聚合（沿用 {@link EquipmentValue.Aggregate}，与装备读值同构）：
 * <ul>
 *   <li>{@code SUM}（中缀 {@code accessory_stat:<group>.<stat>}）—— 该分组全部物品对应数值求和；</li>
 *   <li>{@code COUNT} —— 该分组内非空物品数量；</li>
 *   <li>{@code FIRST}（默认）—— 该分组首个非空物品的对应数值。</li>
 * </ul>
 *
 * <p>仅玩家持有饰品栏附件（非 EquipmentSlot），非玩家实体（敌人等）恒读 0。
 */
public record AccessoryValue(
        Optional<String> group,
        Optional<String> output,
        EquipmentValue.Aggregate aggregate
) implements EntityNode {

    public AccessoryValue {
        if (aggregate == null) aggregate = EquipmentValue.Aggregate.SUM;
    }

    public static final Codec<AccessoryValue> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("group").forGetter(AccessoryValue::group),
            Codec.STRING.optionalFieldOf("output").forGetter(AccessoryValue::output),
            EquipmentValue.Aggregate.CODEC.optionalFieldOf("aggregate", EquipmentValue.Aggregate.SUM)
                    .forGetter(AccessoryValue::aggregate)
    ).apply(i, AccessoryValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        if (!(living instanceof Player player) || !player.hasData(AttachmentRegistry.ACCESSORY_SAVE)) {
            return 0D;
        }
        AccessorySave save = player.getData(AttachmentRegistry.ACCESSORY_SAVE);
        List<AccessorySlotRegistry.SlotDescriptor> descriptors =
                AccessorySlotRegistry.groupSlots(player.level().registryAccess(), group.orElse(""));
        String key = output.orElse("");
        return switch (aggregate) {
            case COUNT -> {
                int n = 0;
                for (AccessorySlotRegistry.SlotDescriptor d : descriptors) {
                    if (!save.get(d.globalIndex()).isEmpty()) n++;
                }
                yield n;
            }
            case SUM -> {
                double total = 0D;
                for (AccessorySlotRegistry.SlotDescriptor d : descriptors) {
                    ItemStack stack = save.get(d.globalIndex());
                    if (stack.isEmpty()) continue;
                    total += EquipmentValue.readOutput(stack, key);
                }
                yield total;
            }
            default -> { // FIRST
                for (AccessorySlotRegistry.SlotDescriptor d : descriptors) {
                    ItemStack stack = save.get(d.globalIndex());
                    if (stack.isEmpty()) continue;
                    if (key.isEmpty()) yield 1.0;
                    yield EquipmentValue.readOutput(stack, key);
                }
                yield 0D;
            }
        };
    }

    /**
     * 名词短语（P4）：作为 {@link CompareValue} 操作数时产出「分组 + 输出项」可读名词短语（如 "Legs Evasion"）。
     * 复用 {@link AmountSourceNamer} 已建立的键化映射。
     */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(AmountSourceNamer.name(this));
    }

    /**
     * 条件从句（P2/A）：装备饰品分组时渲染「while %s is equipped」（%s 为名词短语）。
     * 布尔/计数门控节点，非 {@link CompareValue} 操作数，故无需额外的数量处理。
     */
    @Override
    public List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.accessory", describeQuantity(ClientLocale.get()).get(0)));
    }
}
