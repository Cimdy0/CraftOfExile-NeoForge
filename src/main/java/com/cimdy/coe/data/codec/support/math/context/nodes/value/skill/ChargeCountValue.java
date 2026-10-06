package com.cimdy.coe.data.codec.support.math.context.nodes.value.skill;

import com.cimdy.coe.data.codec.skill.phase.action.charge.ChargeType;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;

import com.cimdy.coe.registry.AttributeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 当前充能数读值节点：驱动「每 1 枚 X 球」一族线性缩放条目。
 *
 * <p><b>典型真值</b>：精魂宝石 Siphon Elements「5% chance <i>per Power</i> to spawn a Cold Remnant
 * on Freezing a target」——几率本身随暴击球（Power Charge）数量线性缩放，
 * 须在几率表达式中读取施法者当前充能数（{@code charge:power * 0.05}）。
 *
 * <p><b>为何不能走属性</b>：{@code AttributeRegistry} 只有 {@code max_*_charges}（上限）一族，
 * 上限是常量型属性、不随当前持有数变化；当前数属运行时瞬态，直接读 {@link ResourceRegistry} 是唯一真值源。
 *
 * <p><b>取值实体</b>：经 {@link EntityNode} 取当前候选实体（施法者视角）；非生物或未知充能类型返回 0。
 *
 * <p>JSON 形式 {@code {"type":"charge","charge":"power"}}；中缀 {@code charge:power}。
 */
public record ChargeCountValue(String charge) implements EntityNode {

    public static final Codec<ChargeCountValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("charge").forGetter(ChargeCountValue::charge)
    ).apply(instance, ChargeCountValue::new));

    @Override
    public double evaluate(LivingEntity current, ValueScope scope) {
        ChargeType type = switch (charge) {
            case "power" -> ChargeType.POWER;
            case "frenzy" -> ChargeType.FRENZY;
            case "endurance" -> ChargeType.ENDURANCE;
            default -> null;
        };
        return type == null ? 0D : ResourceRegistry.getChargeCount(current, type);
    }

    /** 名词短语：复用状态权威键 {@code stat.value.status.<charge>_charge}（不铸 charge 孪生键）。 */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.ofKey("stat.value.charge." + charge + "_charge", charge + "_charge"));
    }

    /** 条件从句：{@code stat.condition.charge} = "while you have %s"。 */
    @Override
    public List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.charge", describeQuantity(ClientLocale.get()).get(0)));
    }
}
