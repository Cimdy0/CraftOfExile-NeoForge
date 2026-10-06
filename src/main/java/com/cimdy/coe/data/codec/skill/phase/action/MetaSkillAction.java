package com.cimdy.coe.data.codec.skill.phase.action;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.runtime.SkillTriggerHelper;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.MathParser;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 元技能动作 — 按条件触发链接的技能。
 * PoE2 对应：Cast on Critical Strike, Cast on Freeze, Cast on Ignite,
 *           Cast on Shock, Cast on Kill, Cast on Dodge 等。
 *
 * 此 Action 应放入 EventListenPhase 的 actions 列表中，
 * 当满足条件时自动触发链接的技能。
 *
 * <p>触发条件 {@code condition} 为 ExContext 全域 math 表达式（非零即触发），
 * 替代旧 {@code MetaSkillCondition} 封闭枚举——可表达任意组合，如
 * {@code event:on_crit >= 1}、
 * {@code event:on_ignite >= 1 and health:ratio <= 0.35}。
 * 求值前由 {@code MetaSkillAction.execute} 将当前事件名注入 ExpressionContext，
 * 供 {@code event:<name>} 节点判定。
 */
public record MetaSkillAction(
        ExpressionCodec.Value condition,
        List<String> linkedSkillIds,
        Optional<ExpressionCodec.Value> powerMultiplier,
        Optional<ExpressionCodec.Value> internalCooldown,
        Optional<ExpressionCodec.Value> maxTriggersPerCast,
        Optional<ExpressionCodec.Value> linkSkillCostMultiplier,
        Optional<ExpressionCodec.Value> energyThreshold
) implements Action {
    public static final Codec<MetaSkillAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("condition").forGetter(MetaSkillAction::condition),
            Codec.STRING.listOf().fieldOf("linked_ids").forGetter(MetaSkillAction::linkedSkillIds),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("power_multiplier")
                    .forGetter(MetaSkillAction::powerMultiplier),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("internal_cooldown")
                    .forGetter(MetaSkillAction::internalCooldown),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("max_triggers_per_cast")
                    .forGetter(MetaSkillAction::maxTriggersPerCast),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("link_cost_multiplier")
                    .forGetter(MetaSkillAction::linkSkillCostMultiplier),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("energy_threshold")
                    .forGetter(MetaSkillAction::energyThreshold)
    ).apply(i, MetaSkillAction::new));

    @Override public Action.Type<?> getType() { return ActionTypes.META_SKILL; }

    /** 构造「事件名精确匹配」的 ExContext 条件（等价旧 MetaSkillCondition 枚举）。 */
    public static ExpressionCodec.Value eventCondition(String eventName) {
        return new ExpressionCodec.Value(Either.right(MathParser.parse("event:" + eventName + " >= 1")));
    }

    @Override
    public void execute(ActionContext context) {
        // 自校验触发条件：优先复用事件监听阶段单点构造的带事件上下文（eventContext），
        // 避免重复 withEvent 拷贝；非事件路径（如直接施放）回退到从总线取最近事件。
        if (context.skill() == null) return;
        ExpressionContext ctx = context.eventContext().orElseGet(() -> {
            String evName = context.eventBus() != null ? context.eventBus().getLastEventName() : null;
            List<String> evTags = context.eventBus() != null ? context.eventBus().getLastEventTags() : List.of();
            return ExpressionContext.forSkill(context.skill(), null).withEvent(evName, evTags);
        });
        if (condition.getValue(ctx) < 1.0) return;

        // POE2 元技能能量机制：energy_threshold 缺省/≤1 → 即时触发（事件满足即触发链接技能）；
        // 否则每次事件满足条件时积累 1 点能量，达到阈值后消耗能量一次性触发链接技能。
        // 能量存于 ResourceRegistry（参考荣耀，持久资源：按技能 id 分槽 +
        // META_ENERGY_MAX 属性上限 + 宽限期衰减）——替代 SkillContext 瞬时计数器（不跨施放保留）。
        double threshold = energyThreshold.map(v -> v.getValue(ctx)).orElse(1.0);
        if (threshold <= 1.0) {
            triggerLinked(ctx, context);
            return;
        }
        if (!(context.caster() instanceof LivingEntity le)) return;
        Identifier metaSkill = context.skill().getSkillId();
        String energyKey = metaSkill.toString();
        long gameTime = le.level().getGameTime();
        // 「被调用法术能量消耗倍率」：缩放本次触发的能量消耗（默认 1.0 = 100%；读点即元技能能量消耗，真实生效）。
        double costMult = invokedEnergyCostMult(le);
        int drain = Math.max(1, (int) Math.round(threshold * costMult));
        // #23 INVOCATION_SKILLS_CANNOT_GAIN_ENERGY_WHILE_TRIGGERING_SPELLS（GAP-FIX#18）：
        // 标记存在且本次事件将使能量达阈值并触发链接技能时，跳过本次能量获取（避免自循环供能），仍执行下方触发。
        boolean gateEnergy = StatEngine.gateOf(le, "invocation_energy_gate", Set.of());
        boolean willTrigger = ResourceRegistry.getMetaEnergy(le, energyKey) + 1 >= drain;
        if (!(gateEnergy && willTrigger)) {
            ResourceRegistry.generateMetaEnergy(le, energyKey, 1, gameTime);
            // Stat/Context 接线：能量充能事件——StatEngine.fire 驱动 on-cause 效果
            // （数据侧写 tags [caused_meta_energy] 命中「元技能充能时触发 X」，消费方为 StatEffectGen 的
            //  GrantOnEventStatEffect.RESOURCE 条目）。
            // 不写 Event 计数队列：全仓无 recently:/count:/distinct:caused_meta_energy 消费者，该键永不被
            // EventSubscription 订阅（非机制白名单），Event.record 会被门控静默丢弃（死写入）。
            Set<String> tags = new HashSet<>(Set.of(EventName.CAUSED_META_ENERGY));
            StatEngine.fire(le, null, StatContext.of(tags, metaSkill, EventName.CAUSED_META_ENERGY, null), null);
        }
        // 「被调用法术能量消耗几率」：掷骰失败则本次触发免耗（drain=0，技能仍触发，仅不扣能量）。
        if (!invokedEnergyCostChance(le)) drain = 0;
        if (ResourceRegistry.getMetaEnergy(le, energyKey) >= drain) {
            int spent = ResourceRegistry.consumeMetaEnergy(le, energyKey, drain);
            triggerLinked(ctx, context);
            // 「触发技能有几率返还一半已耗能量」：按实际扣除量取半回充（受 META_ENERGY_MAX 封顶）。
            if (spent > 0 && rollChance(le, AttributeRegistry.TRIGGER_ENERGY_REFUND_CHANCE)) {
                ResourceRegistry.generateMetaEnergy(le, energyKey, Math.max(1, (int) Math.round(spent * 0.5)), gameTime);
            }
            // Stat/Context 接线：满能量触发事件——Event.record 存 Context + StatEngine.fire 驱动「元技能触发时 X」。
            Event.record(le, EventName.META_SKILL_TRIGGERED);
            StatEngine.fire(le, null, StatContext.of(Set.of(EventName.META_SKILL_TRIGGERED),
                    metaSkill, EventName.META_SKILL_TRIGGERED, null), null);
        }
    }

    public void triggerLinked(ExpressionContext ctx, ActionContext context) {
        float mult = powerMultiplier.map(v -> (float) v.getValue(ctx)).orElse(1.0f);
        for (String idStr : linkedSkillIds) {
            Identifier id = Identifier.tryParse(idStr);
            if (id == null) continue;
            SkillTriggerHelper.trigger(context.skill(), context, id, mult);
        }
    }

    /** 按百分比属性掷骰（0~1 夹取）；属性缺省 0 时恒不命中。 */
    private static boolean rollChance(LivingEntity le, Holder<Attribute> attr) {
        double chance = Math.max(0.0, Math.min(1.0, le.getAttributeValue(attr)));
        return chance > 0 && le.getRandom().nextDouble() < chance;
    }

    /** 读取「被调用法术能量消耗倍率」（默认 1.0 = 100%；经 StatEngine 读取以并入 {invoked} 语义作用域）。无此属性返回 1.0（保持原行为）。 */
    private static double invokedEnergyCostMult(LivingEntity le) {
        try (var scope = StatScopes.enter(le, StatContext.of(Set.of(), null))) {
            return StatEngine.compute(le, AttributeRegistry.INVOKED_ENERGY_COST_MULT, StatScopes.current(le)).add();
        } catch (Exception ignored) { return 1.0; }
    }

    /** 读取「被调用法术能量消耗几率」（[0,1]；默认 1.0 = 必消耗）。掷骰失败则本次免耗。经 StatEngine 读取以并入 {invoked} 语义作用域。 */
    private static boolean invokedEnergyCostChance(LivingEntity le) {
        try (var scope = StatScopes.enter(le, StatContext.of(Set.of(), null))) {
            double chance = StatEngine.compute(le, AttributeRegistry.INVOKED_ENERGY_COST_CHANCE, StatScopes.current(le)).add();
            return chance > 0 && le.getRandom().nextDouble() < Math.max(0.0, Math.min(1.0, chance));
        } catch (Exception ignored) { return true; }
    }
}
