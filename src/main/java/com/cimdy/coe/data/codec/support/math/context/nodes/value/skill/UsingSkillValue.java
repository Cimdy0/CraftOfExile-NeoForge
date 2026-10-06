package com.cimdy.coe.data.codec.support.math.context.nodes.value.skill;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.LivingStateValue;
import com.cimdy.coe.data.helper.skill.PlayerCastHelper;
import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.SkillPhasePlayer;
import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.event.skill.SkillEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * 实体当前是否正在使用「某元素」技能读值节点（与 {@link SkillUsingValue} 按 id 精确匹配互补：本节点按元素聚合）。
 * <ul>
 *   <li>{@code element = "any"}：只要 {@code SkillEvent.activeSkills} 中存在 {@code caster} 为本实体的技能实例即满足；</li>
 *   <li>{@code element = "cold"/"fire"/...}：还要求该技能定义（递归遍历 {@code phases→actions}）含对应伤害类型的
 *       {@link DamageAction}（即「正在施放以该元素伤害为核心的技能」）。</li>
 * </ul>
 * 数据源 {@link SkillEvent#activeSkills} 在施放时 add（{@code PlayerCastHelper.playerCast}）、{@code isStop()} 时移除，
 * 是「当前正在使用技能」的实时真读点（非近似、非轮询缓存），符合 R3 实现即引擎真读。
 * <p>
 * 与 {@link LivingStateValue} 等战斗状态节点同源：实现 {@link EntityNode}，由 {@code ValueScope.current()} 取当前实体。
 */
public record UsingSkillValue(String element) implements EntityNode {
    public static final Codec<UsingSkillValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("element").forGetter(UsingSkillValue::element)
            ).apply(instance, UsingSkillValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        for (SkillInstance si : SkillEvent.activeSkills) {
            if (!si.getContext().getCaster().getUUID().equals(living.getUUID())) continue;
            if (matchesElement(si, element)) return 1D;
        }
        return 0D;
    }

    private static boolean matchesElement(SkillInstance si, String element) {
        if ("any".equalsIgnoreCase(element)) return true;
        Skill skill = si.getSkill();
        if (skill == null) return false;
        return hasElement(skill.phases(), element);
    }

    /** 递归遍历技能阶段树，凡遇 damageType 匹配（不区分大小写）的 DamageAction 即视为该元素技能。 */
    private static boolean hasElement(List<SkillPhasePlayer> players, String element) {
        for (SkillPhasePlayer p : players) {
            SkillPhase phase = p.phase();
            for (TriggeredAction ta : phase.actions()) {
                if (ta.action() instanceof DamageAction da
                        && da.damageType().equalsIgnoreCase(element)) {
                    return true;
                }
            }
            if (hasElement(phase.children(), element)) return true;
        }
        return false;
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("using_skill", element));
    }

    @Override
    public List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.using_skill", describeQuantity(ClientLocale.get()).get(0)));
    }
}
