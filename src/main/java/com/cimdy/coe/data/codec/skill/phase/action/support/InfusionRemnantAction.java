package com.cimdy.coe.data.codec.skill.phase.action.support;

import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.data.codec.skill.phase.action.control.ChanceAction;
import com.cimdy.coe.data.codec.skill.phase.action.combo.DeliveryAction;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 灌入残迹生成动作 — 在施法者处留下一枚指定元素的<b>灌入残迹</b>（Infusion Remnant）。
 *
 * <p><b>与「实体残片投递（统一 {@code AreaDelivery} 的 {@code REMNANT} 几何）」的分工</b>：二者是<b>两套并存的残片体系</b>，不可混用——
 * <ul>
 *   <li>实体残片投递：投递形态，产出<b>实体残片</b>（{@code coe:remnant} 落世界、
 *       由施法者走过拾取、经 {@code ON_HIT} 回调阶段 actions 产生效果），用于「残片作为技能载体」的技能
 *       （如 Mana Remnants 类）。</li>
 *   <li>本动作：产出<b>灌入残迹</b>（{@link ResourceRegistry} 的瞬态标记），拾取即回补对应元素灌入
 *       并授予短时强化，用于「元素灌入」一族（Siphon Elements）。</li>
 * </ul>
 * 故本动作<b>不接</b> {@code DeliveryAction}：灌入残迹无实体形态，走投递会凭空多出一个无效果实体。
 *
 * <p><b>真值落点</b>：Siphon Elements「has a chance to create an Infusion Remnant when you Freeze,
 * Shock or Ignite a target」——三条常驻监听各按几率（{@code ChanceAction}）后调本动作，
 * 元素由异常类型决定（Freeze→cold / Ignite→fire / Shock→lightning）。
 *
 * <p><b>门控</b>：残迹数量 / 幅度 / 双倍几率 / 元素互换均在 {@link ResourceRegistry#dropInfusionRemnant} 内
 * 按创建者属性（{@code REMNANT_EXTRA_CHANCE} / {@code REMNANT_MAGNITUDE} / 元素互换族）真读，
 * 本动作只负责指定元素种类，不重复实现。
 *
 * @param kind 灌入元素：{@code fire} / {@code cold} / {@code lightning}。
 */
public record InfusionRemnantAction(String kind) implements Action {

    public static final Codec<InfusionRemnantAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("kind").forGetter(InfusionRemnantAction::kind)
    ).apply(i, InfusionRemnantAction::new));

    @Override
    public Action.Type<?> getType() {
        return ActionTypes.INFUSION_REMNANT;
    }

    @Override
    public void execute(ActionContext context) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        ResourceRegistry.InfusionKind k = switch (kind) {
            case "fire" -> ResourceRegistry.InfusionKind.FIRE;
            case "cold" -> ResourceRegistry.InfusionKind.COLD;
            case "lightning" -> ResourceRegistry.InfusionKind.LIGHTNING;
            default -> null;
        };
        if (k == null) return;
        ResourceRegistry.dropInfusionRemnant(caster, k);
    }

    /** 形态标签：{@code remnant} + 元素，使「残片效果提高 / 元素残片」一族词条在本动作作用域内生效。 */
    @Override
    public Set<String> statTags() {
        return Set.of(GateTag.REMNANT, kind);
    }
}
