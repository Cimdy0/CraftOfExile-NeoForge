package com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.status.StatusValue;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Locale;

/**
 * 资源读值节点（context_dsl_design.md §5）。
 * 输出五类资源（生命 / 魔力 / 能量护盾 / 精魂 / 狂怒）的当前值与最大值，mode 取值：
 * <ul>
 *   <li>{@code health} 当前生命 / {@code max_health} 最大生命</li>
 *   <li>{@code mana} 当前魔力 / {@code max_mana} 最大魔力</li>
 *   <li>{@code energy} 当前能量护盾 / {@code max_energy} 最大能量护盾</li>
 *   <li>{@code spirit} 当前精魂 / {@code max_spirit} 最大精魂</li>
 *   <li>{@code rage} 当前狂怒</li>
 * </ul>
 * 非 LivingEntity 作用域返回 0。JSON 类型键 {@code health} 与 {@code resource} 等价。
 * <p>
 * 统一资源读取器 {@link #readResource} 亦被 {@link StatusValue} 的资源 kind 委托共享
 * （合并实现、不合并类，避免 {@code status:rage} 等中缀回写漂移）。
 */
public record HealthValue(String mode) implements EntityNode {
    public static final Codec<HealthValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("mode").forGetter(HealthValue::mode)
            ).apply(instance, HealthValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        return readResource(mode, living);
    }

    /** 名词短语：资源 mode → 值键 {@code stat.value.resource.<mode>}（P3/A）。 */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("resource", mode));
    }

    /** 统一资源读取器：供 HealthValue 与 StatusValue 的资源 kind 共享（合并实现、不合并类）。 */
    public static double readResource(String mode, LivingEntity living) {
        return switch (mode) {
            case "health" -> living.getHealth();
            case "max_health" -> living.getMaxHealth();
            case "mana" -> ResourceRegistry.current(living, "mana");
            case "max_mana" -> ResourceRegistry.max(living, "mana");
            case "energy" -> ResourceRegistry.totalMetaEnergy(living);
            case "max_energy" -> living.getAttributeValue(AttributeRegistry.META_ENERGY_MAX);
            case "spirit" -> ResourceRegistry.current(living, "spirit");
            case "max_spirit" -> living.getAttributeValue(AttributeRegistry.MAX_SPIRIT);
            case "rage" -> ResourceRegistry.current(living, "rage");
            default -> 0D;
        };
    }
}
