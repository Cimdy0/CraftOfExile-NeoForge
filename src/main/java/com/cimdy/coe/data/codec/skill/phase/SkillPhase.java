package com.cimdy.coe.data.codec.skill.phase;

import com.cimdy.coe.data.codec.skill.phase.action.movement.MoveAction;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.SkillStatScopes;
import com.cimdy.coe.data.codec.skill.context.DamageContext;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutors;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;
import com.cimdy.coe.data.codec.skill.phase.event.EventListenPhase;
import com.cimdy.coe.data.codec.skill.phase.event.SkillEventBus;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * 技能阶段体 — 使用 Delivery + TriggeredAction 系统描述技能行为。
 * 是 {@link SkillPhasePlayer} 的核心部分，可包含子阶段（递归）。
 *
 * <p>PoE2 新增生命周期触发：
 * - ON_COMBO（连击步骤）
 * - ON_PERFECT_TIMING（完美释放窗口）
 * - ON_RELEASE（引导松开事件）
 */
public record SkillPhase(
        Optional<Delivery> delivery,
        List<TriggeredAction> actions,
        List<SkillPhasePlayer> children,
        Optional<EventListenPhase> eventListen,
        // 等级门槛（§8 #8，蓝图 P4）：技能有效等级低于此值则整阶段跳过；空=无门控。
        Optional<ExpressionCodec.Value> minLevel,
        // 锁定字段声明（§8 #14，蓝图 P4）：本阶段这些字段不可被修饰器修改（如武器维度锁定）；
        // 空=无锁定。声明经修饰器应用层消费，数据层先落字段。
        Optional<List<String>> lockedFields
) {
    public static final Codec<SkillPhase> CODEC = Codec.lazyInitialized(() ->
            RecordCodecBuilder.create(instance -> instance.group(
            Delivery.CODEC.optionalFieldOf("delivery").forGetter(SkillPhase::delivery),
            TriggeredAction.CODEC.listOf().optionalFieldOf("actions", List.of())
                    .forGetter(SkillPhase::actions),
            SkillPhasePlayer.CODEC.listOf().optionalFieldOf("children", List.of())
                    .forGetter(SkillPhase::children),
            EventListenPhase.CODEC.optionalFieldOf("event_listen")
                    .forGetter(SkillPhase::eventListen),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("min_level").forGetter(SkillPhase::minLevel),
            Codec.STRING.listOf().optionalFieldOf("locked_fields").forGetter(SkillPhase::lockedFields)
    ).apply(instance, SkillPhase::new)));

    /**
     * 便捷构造（4 参）：省略等级门槛 / 锁定字段 → 空（即无门控、无锁定声明）。
     * 保留全部既有调用点（SkillPresets / SkillFactory / SpiritGemFactory / SkillLinkResolver / EventListenPhase）不变。
     */
    public SkillPhase(Optional<Delivery> delivery, List<TriggeredAction> actions,
                      List<SkillPhasePlayer> children, Optional<EventListenPhase> eventListen) {
        this(delivery, actions, children, eventListen, Optional.empty(), Optional.empty());
    }

    // ===== 核心方法 =====

    private ActionContext createActionContext(
            SkillInstance skillInstance, SkillPhaseInstance phaseInstance, int tickCount) {
        SkillContext context = skillInstance.getContext();
        DamageContext damageContext = (phaseInstance != null)
                ? phaseInstance.getDamageContext() : null;
        return new ActionContext(
                skillInstance, context.getCaster(),
                Optional.empty(),
                // 施法方向 = 施法者视线：此前恒为 ZERO，令 ON_CAST 阶段一切读 direction 的动作
                // （MoveAction 位移 / 各 DeliveryExecutor 朝向）只能各自回退视线取值，
                // direction 字段在施放阶段形同死字段。视线即施放瞬间的朝向，语义与回退一致。
                context.getCaster().position(), context.getCaster().getLookAngle(), 1.0f,
                tickCount,
                SkillEventBus.forEntity(context.getCaster()),
                null, damageContext, Optional.empty()
        );
    }

    /**
     * 初始化阶段：执行 cast 动作并创建阶段实例。
     * 无论是否有 delivery，阶段都持有一个 {@link SkillPhaseInstance} 以驱动每 tick 的
     * on_tick / on_release 与子阶段初始化；仅当 delivery 存在时才生成投递物并注册到 tracker。
     */
    public List<SkillPhaseInstance> initialize(
            SkillInstance skillInstance, SkillPhaseInstance parentInstance) {
        SkillContext context = skillInstance.getContext();

        // 等级门槛（§8 #8，蓝图 P4）：技能有效等级低于门槛则整阶段跳过。
        // fail-open：未声明门槛、或等级尚未传播（sl<=0）时不跳过，保持既有行为。
        if (minLevel.isPresent()) {
            double sl = context.getSkillLevel();
            if (sl > 0 && sl < minLevel.get().getFloat(context)) return new ArrayList<>();
        }

        // 注册事件监听
        eventListen.ifPresent(listen -> {
            SkillEventBus bus = SkillEventBus.forEntity(context.getCaster());
            listen.register(skillInstance, context.getCaster(), bus, this);
        });

        List<SkillPhaseInstance> result = new ArrayList<>();

        // on_pre_cast：费用检查后、交付创建前（可中途取消）
        ActionContext preCtx = createActionContext(skillInstance, null, 0);
        boolean shouldProceed = actions.stream()
                .filter(ta -> ta.trigger() == LifecycleTrigger.ON_PRE_CAST)
                .map(ta -> {
                    executeScoped(ta, preCtx);
                    return true;
                })
                .reduce(true, (a, b) -> a && b);
        if (!shouldProceed) return result;

        // on_cast
        ActionContext actionCtx = createActionContext(skillInstance, null, 0);
        actions.stream()
                .filter(ta -> ta.trigger() == LifecycleTrigger.ON_CAST)
                .forEach(ta -> executeScoped(ta, actionCtx));

        // on_perfect_timing（如果在完美释放窗口内）
        float chargeAmount = skillInstance.getContext().getChargeAmount();
        actions.stream()
                .filter(ta -> ta.trigger() == LifecycleTrigger.ON_PERFECT_TIMING)
                .forEach(ta -> executeScoped(ta, actionCtx.withCharge(chargeAmount)));

        // 创建阶段实例（驱动每 tick 生命周期）；有 delivery 才生成投递物
        UUID phaseId = UUID.randomUUID();
        ActionContext deliveryCtx = createActionContext(skillInstance, null, 0).withPhaseId(phaseId);
        SkillPhaseInstance instance = new SkillPhaseInstance(this, phaseId);
        instance.setActionContext(deliveryCtx);
        // 回响范围载荷沿子阶段树传播：重放实例的子阶段也须带上同一次回响的范围倍率，
        // 否则多阶段技能只有首阶段吃到回响加成、后续子阶段退回原尺寸。
        if (parentInstance != null) {
            instance.setEchoArea(parentInstance.echoAreaInc(), parentInstance.echoAreaMore());
        }
        result.add(instance);

        delivery.ifPresent(del -> DeliveryExecutors.execute(del, deliveryCtx,
                context.getCaster().getUUID(), phaseId, instance));

        return result;
    }

    /**
     * 回响重放：只重做<b>「效果」</b>部分（创建阶段实例 + 生成投递物），不重做<b>「施放」</b>部分。
     *
     * <p>对齐 POE2 官方定义（poe2wiki「Spell Echo Support」Mechanics）：repeat 是原施放的一部分，
     * 不消耗资源、不走冷却、也不二次触发 <i>when you cast</i> 效果。故此处相对 {@link #initialize}
     * <b>刻意跳过</b>：事件监听注册、ON_PRE_CAST、ON_CAST、ON_PERFECT_TIMING。
     * 只保留「创建阶段实例 + 投递物执行」，子阶段由 {@link SkillPhaseInstance#tick} 的
     * {@code runChildren} 在重放实例的首 tick 自动展开，因此整棵阶段子树都会被原样重放。
     *
     * <p>重放全程处于 {@link SkillStatScopes#enterEcho} 作用域内（{@code echo} 标签的唯一发射点），
     * 使「Echoed Spells have X」类词缀只对重放出来的这一次生效、不串到原施放。
     *
     * @param areaInc  本次回响的「increased」范围增量（加法项，与 EFFECT_AREA 相加）；无则为 0.0
     * @param areaMore 本次回响的「more」范围倍率（乘法项）；无则为 1.0
     */
    public List<SkillPhaseInstance> repeat(SkillInstance skillInstance, double areaInc, double areaMore) {
        LivingEntity caster = skillInstance.getContext().getCaster();

        // 等级门槛（fail-open，同 initialize）
        if (minLevel.isPresent()) {
            double sl = skillInstance.getContext().getSkillLevel();
            if (sl > 0 && sl < minLevel.get().getFloat(skillInstance.getContext())) return new ArrayList<>();
        }

        UUID phaseId = UUID.randomUUID();
        ActionContext deliveryCtx = createActionContext(skillInstance, null, 0).withPhaseId(phaseId);
        SkillPhaseInstance instance = new SkillPhaseInstance(this, phaseId);
        instance.setActionContext(deliveryCtx).setEchoArea(areaInc, areaMore);

        try (var scope = SkillStatScopes.enterEcho(caster, skillInstance)) {
            delivery.ifPresent(del -> DeliveryExecutors.execute(del, deliveryCtx,
                    caster.getUUID(), phaseId, instance));
        }

        List<SkillPhaseInstance> result = new ArrayList<>();
        result.add(instance);
        return result;
    }

    /**
     * 初始化子阶段（仅一次，由 SkillPhaseInstance 的 childrenInitialized 守卫）。
     * 子阶段自身会生成独立投递物并注册进 SkillPhaseInstanceTracker，
     * 故父阶段实例的存活与否不影响子阶段。
     */
    public List<SkillPhaseInstance> runChildren(
            SkillInstance skillInstance, SkillPhaseInstance phaseInstance) {
        List<SkillPhaseInstance> list = new ArrayList<>();
        if (phaseInstance != null) {
            list.addAll(children.stream().flatMap(phase ->
                    phase.run(skillInstance, phaseInstance).stream()).toList());
        }
        return list;
    }

    /**
     * 每 tick 执行的持续动作：on_tick / on_combo / on_release。
     * 不依赖 delivery（通道型自指向 / 范围增益也需每 tick 生效）。
     */
    public void runRuntime(SkillInstance skillInstance, SkillPhaseInstance phaseInstance) {
        if (phaseInstance == null) return;
        int currentTime = phaseInstance.time();
        ActionContext actionCtx = createActionContext(skillInstance, phaseInstance, currentTime);

        actions.stream()
                .filter(ta -> ta.trigger() == LifecycleTrigger.ON_TICK)
                .forEach(ta -> executeScoped(ta, actionCtx));

        // ON_RELEASE：引导/持续技能松开时只触发一次。
        // 用 releaseFired 标志拦截，避免「有 delivery 的引导技能松开后投递物仍在飞、
        // 阶段实例仍存活」导致每 tick 重复触发。!isCharging() 等价于 !isPress()（松开）。
        if (!phaseInstance.isReleaseFired() && !skillInstance.getContext().isCharging()) {
            actions.stream()
                    .filter(ta -> ta.trigger() == LifecycleTrigger.ON_RELEASE)
                    .forEach(ta -> executeScoped(ta, actionCtx));
            phaseInstance.markReleaseFired();
        }
    }

    /**
     * 阶段实例是否仍存活（决定 SkillPhaseInstance.tick 是否保留该实例）。
     * - 有投递：由 SkillPhaseInstanceTracker 引用计数判定（投射物在飞 / 光束光环常驻）。
     * - 无投递：随技能是否仍被按住（isPress）存活，使纯动作 / 父阶段在引导期间持续 tick。
     */
    public boolean isAlive(SkillInstance skillInstance, SkillPhaseInstance phaseInstance) {
        if (delivery.isPresent()) {
            return SkillPhaseInstanceTracker.contains(
                    skillInstance.getContext().getCaster().getUUID(), phaseInstance.getPhaseID());
        }
        return skillInstance.getContext().isPress();
    }

    /**
     * 在动作执行前进入「投放 + 行为」的 stat 标签作用域（投射物→projectile、
     * 范围→area、伤害行为→元素标签 …），使带对应标签的天赋加成在该动作的属性读取内生效。
     * 与外层技能作用域（attack / spell / 技能类型 + 技能名）合并而非遮蔽，且按当前阶段隔离。
     */
    public void executeScoped(TriggeredAction ta, ActionContext ctx) {
        try (var scope = SkillStatScopes.enterAction(ctx, delivery.orElse(null), ta.action())) {
            ta.action().execute(ctx);
        }
    }

    public Set<String> getLabels(SkillContext context) {
        return new LinkedHashSet<>(children.stream()
                .flatMap(phase -> phase.getLabels(context).stream()).toList());
    }

    public void addComponent(List<Component> tooltip, SkillContext context) {
        children.forEach(phase -> phase.addComponent(tooltip, context));
    }

    /** 返回覆写投递（delivery）后的副本；其余字段（动作/子阶段/事件/门槛/锁定）原样保留。 */
    public SkillPhase withDelivery(Delivery delivery) {
        return new SkillPhase(Optional.of(delivery), actions, children, eventListen, minLevel, lockedFields);
    }
}
