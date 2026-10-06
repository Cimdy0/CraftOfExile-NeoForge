package com.cimdy.coe.data.helper.stat;



import com.cimdy.coe.event.combat.AttributeEvent;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.attribute.EntityAttributeLimiter;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 属性修正器（{@link AttributeModifier} / 限制器）的<b>唯一写入口</b>。
 *
 * <p>原实现内联于 {@code event.AttributeEvent}（7 个非监听 static 方法）：修正器的增删改属于
 * 「实体属性机制」，不是事件派发；按铁律 #56（事件端只派发、机制入 helper/engine）整体迁出。
 *
 * <p>语义要点：
 * <ul>
 *   <li>{@link #addModifier} 幂等——同 id 已存在则跳过（重复叠加会把上限/抗性翻倍）；</li>
 *   <li>{@link #removeModifier} 仅在修正器存在时移除，避免无谓的属性重建；</li>
 *   <li>{@link #modifierFix} / {@link #changeModifierFix}：上限类属性变化后把当前值夹回新上限
 *       （生命 / 能量护盾 / 魔力三者，其余属性无「当前值」语义）。</li>
 * </ul>
 */
public final class AttributeModifierHelper {

    private AttributeModifierHelper() {
    }

    /** 为多个属性加成同一修正器（逐个走 {@link #addModifier(LivingEntity, Holder, AttributeModifier)}）。 */
    public static void addModifier(LivingEntity living, List<Holder<Attribute>> attributes, AttributeModifier modifier) {
        attributes.forEach(attribute -> addModifier(living, attribute, modifier));
    }

    /** 加成一个常驻修正器；实体无该属性或同 id 已存在时跳过（幂等）。 */
    public static void addModifier(LivingEntity living, Holder<Attribute> attribute, AttributeModifier modifier) {
        if (living.getAttributes().hasAttribute(attribute)
                && !living.getAttribute(attribute).hasModifier(modifier.id())) {
            living.getAttribute(attribute).addPermanentModifier(modifier);
        }
    }

    /** 按 id 移除修正器；存在才移除，{@code fix=true} 时随后夹回当前值。 */
    public static void removeModifier(LivingEntity living, Holder<Attribute> attribute, Identifier id, boolean fix) {
        if (living.getAttributes().hasModifier(attribute, id)) {
            living.getAttribute(attribute).removeModifier(id);
            if (fix) {
                modifierFix(living, attribute);
            }
        }
    }

    /** 装载限制器（{@code fix=true}：无条件夹回当前值；false：仅在越界时夹回）。 */
    public static void addLimiter(LivingEntity living, EntityAttributeLimiter.Modifier limiter, boolean fix) {
        if (living.getAttributes().hasAttribute(limiter.attribute())) {
            AttributeLimiterHelper.addPersistentLimiter(living, limiter.attribute(), limiter.modifier());
            if (fix) {
                modifierFix(living, limiter.attribute());
            } else {
                changeModifierFix(living, limiter.attribute());
            }
        }
    }

    /** 卸载限制器；{@code fix=true} 时随后夹回当前值。 */
    public static void removeLimiter(LivingEntity living, EntityAttributeLimiter.Modifier limiter, boolean fix) {
        AttributeLimiterHelper.removeLimiter(living, limiter.attribute(), limiter.modifier().id());
        if (fix) {
            modifierFix(living, limiter.attribute());
        }
    }

    /** 上限变化后无条件把当前值设为新上限（生命 / 能量护盾 / 魔力）。 */
    public static void modifierFix(LivingEntity living, Holder<Attribute> attribute) {
        if (attribute.is(Attributes.MAX_HEALTH)) {
            living.setHealth(living.getMaxHealth());
        } else if (attribute.is(AttributeRegistry.MAX_ENERGY_SHIELD)) {
            ResourceRegistry.setEnergyShield(living, ResourceRegistry.getMaxEnergyShield(living));
        } else if (attribute.is(AttributeRegistry.MAX_RUNIC_WARD)) {
            ResourceRegistry.setRunicWard(living, ResourceRegistry.getMaxRunicWard(living));
        } else if (attribute.is(AttributeRegistry.MAX_MANA)) {
            ResourceRegistry.setMana(living, ResourceRegistry.getMaxMana(living));
        }
    }

    /** 上限变化后仅在「当前值 &gt; 新上限」时夹回（不回满，只截断越界部分）。 */
    public static void changeModifierFix(LivingEntity living, Holder<Attribute> attribute) {
        if (attribute.is(Attributes.MAX_HEALTH) && living.getHealth() > living.getMaxHealth()) {
            living.setHealth(living.getMaxHealth());
        } else if (attribute.is(AttributeRegistry.MAX_ENERGY_SHIELD)
                && ResourceRegistry.getEnergyShield(living) > ResourceRegistry.getMaxEnergyShield(living)) {
            ResourceRegistry.setEnergyShield(living, ResourceRegistry.getMaxEnergyShield(living));
        } else if (attribute.is(AttributeRegistry.MAX_RUNIC_WARD)
                && ResourceRegistry.getRunicWard(living) > ResourceRegistry.getMaxRunicWard(living)) {
            ResourceRegistry.setRunicWard(living, ResourceRegistry.getMaxRunicWard(living));
        } else if (attribute.is(AttributeRegistry.MAX_MANA) && ResourceRegistry.getMana(living) > ResourceRegistry.getMaxMana(living)) {
            ResourceRegistry.setMana(living, ResourceRegistry.getMaxMana(living));
        }
    }
}
