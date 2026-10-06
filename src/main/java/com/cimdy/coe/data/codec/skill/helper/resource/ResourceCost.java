package com.cimdy.coe.data.codec.skill.helper.resource;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.helper.hub.SpiritManager;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.effect.convert.ResourceConvertStatEffect;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.ReservationChangeEvent;
import com.cimdy.coe.data.codec.skill.phase.event.SkillEventBus;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;
import com.cimdy.coe.data.helper.hub.ReservationManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.Set;

public record ResourceCost(
        ResourceType type,
        ResourceUsage usage,
        ExpressionCodec.Value value
) {
    public static final Codec<ResourceCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceType.CODEC.optionalFieldOf("type", ResourceType.MANA).forGetter(ResourceCost::type),
            ResourceUsage.CODEC.optionalFieldOf("usage", ResourceUsage.CONSUME).forGetter(ResourceCost::usage),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("value", ExpressionCodec.of(0.0))
                    .forGetter(ResourceCost::value)
    ).apply(instance, ResourceCost::new));

    /**
     * 技能扣费转换查询的来源标签（ResourceConvertStatEffect.matchesSource 前缀匹配）：
     * 携带技能 id（{@code "skill:<id>"}）使 source_tags 可精确过滤单技能；无 id 时回退 {@code "skill"}。
     */
    private static String sourceTagOf(SkillContext context) {
        String id = context != null ? context.getSkillId() : null;
        return id == null || id.isEmpty() ? "skill" : "skill:" + id;
    }

    public boolean matches(SkillContext context) {
        if (usage == ResourceUsage.PRESERVE) {
            // 保留型消耗：要求仍有足够“未保留”资源可供保留
            return type.match(context, value.getValue(context));
        }
        double amount = effectiveCost(context);
        // 资源转换（fold 型）：keystone（如血魔法）声明「扣 A 转扣 B」，由 Stat 效果实现，
        // 此处仅查询——命中则按模式判定是否可承受。
        Optional<StatEngine.ResourceConversion> conv =
                StatEngine.convert(context.getCaster(), type, (float) amount, sourceTagOf(context));
        if (conv.isPresent()) {
            if (conv.get().mode() == ResourceConvertStatEffect.Mode.GAIN) {
                // 共存：源资源照常需可承受，目标资源也需可承受转换份额
                return type.match(context, amount)
                        && conv.get().to().match(context, conv.get().amount());
            }
            return conv.get().to().match(context, conv.get().amount());
        }
        return type.match(context, amount);
    }

    public void runCost(SkillContext context) {
        if (usage == ResourceUsage.PRESERVE) {
            // 精魂保留由 SpiritManager 独占结算（AURA/TOTEM 组件），避免与下方 CAST 累加路径双计 + 永久泄漏。
            if (type == ResourceType.SPIRIT) {
                return;
            }
            // 非精魂保留型（未来扩展点）：登记 CAST 组件，技能结束时经 SkillInstance.teardown 释放。
            LivingEntity caster = context.getCaster();
            float amount = (float) value.getValue(context);
            // 资源键由 ResourceType 自身承载（"mana"/"health"…），保留池按资源分池，加一种可保留资源零改动。
            String resource = type.getSerializedName();
            float old = ReservationManager.getReserved(caster.getUUID(), resource);
            ReservationManager.reserve(caster.getUUID(), resource, amount);
            float now = ReservationManager.getReserved(caster.getUUID(), resource);
            if (now != old && caster instanceof ServerPlayer sp) {
                SkillEventBus.forEntity(sp).publish(
                        new ReservationChangeEvent(sp, null, old, now, (float) ResourceRegistry.current(sp, "spirit")));
            }
            return;
        }
        LivingEntity caster = context.getCaster();
        double amount = effectiveCost(context);
        // 资源转换（fold 型）：命中则按模式消费，逻辑实现在 Stat 效果内，此处仅消费查询结果。
        Optional<StatEngine.ResourceConversion> conv = StatEngine.convert(caster, type, (float) amount, sourceTagOf(context));
        if (conv.isPresent()) {
            if (conv.get().mode() == ResourceConvertStatEffect.Mode.GAIN) {
                // 共存：源资源照常扣，目标资源也扣转换份额
                type.cost(context, amount);
                conv.get().to().cost(context, conv.get().amount());
            } else {
                conv.get().to().cost(context, conv.get().amount());
            }
            return;
        }
        type.cost(context, amount);
    }

    public MutableComponent getCostComponent(SkillContext context) {
        return usage.getComponent().append(": ").withStyle(ChatFormatting.DARK_AQUA)
                .append(type.getComponent(effectiveCost(context)).withStyle(ChatFormatting.WHITE));
    }

    /**
     * 经「消耗效率」缩放后的实际消耗量：对 MANA / HEALTH 消耗按 {@code (1 - 效率)} 缩放（POE2 Cost Efficiency 仅降对应资源）。
     * 效率取自单一属性 {@link AttributeRegistry#COST_EFFICIENCY}（base=0，ADD_VALUE 加成池），
     * 于当前技能作用域内按资源语义标签（{@link GateTag#MANA}/{@link GateTag#HEALTH}）读取（{@link StatScopes#mergeWithCurrent}），
     * 故 command/mark/low_resource 等变体即时生效——接线即真实降低施放消耗（R3 非死线）。其余资源（能量/精魂保留）不受影响。
     */
    private double effectiveCost(SkillContext context) {
        double raw = value.getValue(context);
        LivingEntity caster = context.getCaster();
        if (caster == null) return raw;
        if (type == ResourceType.MANA) {
            StatContext s = StatScopes.mergeWithCurrent(caster, StatContext.of(Set.of(GateTag.MANA), null));
            double eff = StatEngine.compute(caster, AttributeRegistry.COST_EFFICIENCY, s).add();
            return raw * Math.max(0.0, 1.0 - eff);
        }
        if (type == ResourceType.HEALTH) {
            StatContext s = StatScopes.mergeWithCurrent(caster, StatContext.of(Set.of(GateTag.HEALTH), null));
            double eff = StatEngine.compute(caster, AttributeRegistry.COST_EFFICIENCY, s).add();
            return raw * Math.max(0.0, 1.0 - eff);
        }
        return raw;
    }
}