package com.cimdy.coe.data.codec.skill.phase.event;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.data.gen.stat.wirings.wire.resource.WireResource;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.AilmentEvent;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.ChargeChangeEvent;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.ComboEvent;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.CustomSkillEvent;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.ReservationChangeEvent;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.SkillCastEvent;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.SkillExpireEvent;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.SkillHitEvent;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.SkillKillEvent;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.SkillTriggerEvent;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.StaggerEvent;
import com.cimdy.coe.data.helper.hub.SpiritManager;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.SkillStatScopes;
import com.cimdy.coe.data.codec.skill.runtime.SkillTriggerHelper;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.cimdy.coe.event.skill.SkillEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 事件监听阶段 — 被动技能的事件入口。
 * 技能通过它订阅其他技能的事件并做出响应。
 */
public record EventListenPhase(
        SkillEventType eventType,
        Optional<MathExpression> filter,
        List<TriggeredAction> actions,
        Optional<ExpressionCodec.Value> internalCooldown,
        Optional<ExpressionCodec.Value> maxTriggersPerCast,
        Optional<String> eventName,
        Optional<List<String>> requireTags
) {
    public static final Codec<EventListenPhase> CODEC = RecordCodecBuilder.create(i -> i.group(
            SkillEventType.CODEC.fieldOf("event_type").forGetter(EventListenPhase::eventType),
            MathExpression.CODEC.optionalFieldOf("filter").forGetter(EventListenPhase::filter),
            TriggeredAction.CODEC.listOf().fieldOf("actions").forGetter(EventListenPhase::actions),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("internal_cooldown")
                    .forGetter(EventListenPhase::internalCooldown),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("max_triggers_per_cast")
                    .forGetter(EventListenPhase::maxTriggersPerCast),
            Codec.STRING.optionalFieldOf("event_name").forGetter(EventListenPhase::eventName),
            Codec.STRING.listOf().optionalFieldOf("require_tags").forGetter(EventListenPhase::requireTags)
    ).apply(i, EventListenPhase::new));

    public EventListenPhase(SkillEventType eventType, Optional<MathExpression> filter,
                            List<TriggeredAction> actions, Optional<ExpressionCodec.Value> internalCooldown,
                            Optional<ExpressionCodec.Value> maxTriggersPerCast, Optional<String> eventName) {
        this(eventType, filter, actions, internalCooldown, maxTriggersPerCast, eventName, Optional.empty());
    }

    /**
     * 注册到指定实体的 EventBus。
     * 通过 registerIfAbsent 去重：同一「实体 + 事件类型 + 事件名 + 动作」终生只注册一次，
     * 避免技能反复施放导致监听器堆叠（元宝石是常驻插槽效果，监听本应持久）。
     */
    public void register(SkillInstance skill, Entity caster, SkillEventBus bus, SkillPhase host) {
        registerDetachable(skill, caster, bus, host);
    }

    /**
     * 可注销注册 —— 与 {@link #register} 同一口径，但返回 {@link SkillEventBus.ListenerHandle}，
     * 供<b>常驻</b>监听（精魂光环「While active, when X」）在停用时撤销。
     *
     * <p><b>为何原本不需要句柄</b>：施放期监听（{@code SkillPhase.initialize}）随插槽常驻，
     * 终生有效，故只需 {@code registerIfAbsent} 去重。常驻光环可被玩家随时开关，
     * 关闭后监听若不撤销会继续响应事件 —— 故此处必须交出句柄。
     *
     * @return 本次实际注册产生的句柄；若同键已注册（此前已激活过）则返回 {@code Optional.empty()}。
     */
    public Optional<SkillEventBus.ListenerHandle> registerDetachable(
            SkillInstance skill, Entity caster, SkillEventBus bus, SkillPhase host) {
        Class<? extends SkillTriggerEvent> eventClass = resolveEventClass(eventType);
        String key = caster.getUUID() + ":" + eventType.getSerializedName() + ":"
                + eventName.orElse("") + ":" + requireTags.orElse(List.of()) + ":" + Integer.toHexString(actions.hashCode());
        if (!bus.tryReserve(key)) return Optional.empty();
        return Optional.of(bus.register(eventClass, new SkillEventBus.EventListener() {
            private int lastTriggerTick = Integer.MIN_VALUE;
            private int triggerCount = 0;
            /**
             * 注册时刻的宝石等级快照：常驻监听（精魂「While active, when X」）的事件回调
             * 早已脱离施放路径，而 {@code skill.level} 读的是<b>实体级上下文字段</b>，
             * 会被此后任何技能的施放覆盖（等级张冠李戴，数值静默错误）。
             * 注册点（{@code SpiritManager#registerPersistentListens}）已注入本宝石等级，故在此快照，
             * 供本次事件回调与「本次产出的残片在之后被拾取」的回调共用。
             */
            private final double skillLevel = caster instanceof LivingEntity living
                    ? ExpressionContext.get(living).getSkillLevel() : 1.0;
            /** 单次构造的带事件上下文：tryConsume 解析后存入，filter 与 onEvent 共用，避免重复 withEvent 拷贝。 */
            private final AtomicReference<ExpressionContext> eventContextRef = new AtomicReference<>();

            @Override
            public boolean tryConsume(SkillTriggerEvent event) {
                String evName = (event instanceof CustomSkillEvent ce)
                        ? ce.eventName() : null;
                List<String> evTags = (event instanceof CustomSkillEvent ce)
                        ? ce.tags() : List.of();
                // 单点构造带事件上下文（供 filter 与后续 onEvent 的 MetaSkillAction 共用）
                eventContextRef.set(ExpressionContext.forSkill(skill, null).withEvent(evName, evTags));
                if (filter.isPresent() && !eventContextRef.get().test(filter.get()))
                    return false;
                // 触发免疫：被元技能触发出来的技能（bind == TRIGGER_BIND）自身产生的事件不再触发其它元技能，
                // 既符合 POE2「触发不能连环触发」规则，也避免无限递归（如暴击触发法术 → 法术暴击 → 再触发）。
                if (event.skill() != null
                        && event.skill().getContext().getBind() == SkillTriggerHelper.TRIGGER_BIND)
                    return false;
                // event_name 过滤（用于 CUSTOM 事件按名称区分 on_crit / on_ignite / ...）
                if (event instanceof CustomSkillEvent ce
                        && eventName.isPresent() && !ce.eventName().equals(eventName.get()))
                    return false;
                // require_tags 过滤：事件须携带全部指定标签（配合通用事件名 caused_ailment + 异常类型标签收敛）
                if (event instanceof CustomSkillEvent ce
                        && requireTags.isPresent()
                        && !ce.tags().containsAll(requireTags.get()))
                    return false;

                int currentTick = caster.level().getServer() != null
                        ? caster.level().getServer().getTickCount() : 0;
                if (internalCooldown.isPresent()
                        && currentTick - lastTriggerTick < internalCooldown.get().getInt())
                    return false;
                if (maxTriggersPerCast.isPresent()
                        && triggerCount >= maxTriggersPerCast.get().getInt())
                    return false;

                lastTriggerTick = currentTick;
                triggerCount++;
                return true;
            }

            @Override
            public void onEvent(SkillTriggerEvent event) {
                Vec3 pos = caster instanceof LivingEntity le
                        ? le.position() : caster.position();
                // 复用 tryConsume 单点构造的带事件上下文，传递给 MetaSkillAction 避免重复 withEvent 拷贝。
                ExpressionContext eventCtx = eventContextRef.get();
                // 每次触发新建相位实例：监听经 registerIfAbsent 终生常驻，而施放时生成的 phaseId 属
                // 单次施放、会随实例回收失效，故事件回调不能沿用旧 phaseId——必须现生成。
                // 有它，动作链才能生成投递物（残片/危害物等 SkillEntity 靠 (casterId, phaseId)
                // 反查本实例并在被触发时点燃 ON_HIT），从而支撑「击杀/暴击 → 生成残片」一类效果。
                UUID newPhaseId = UUID.randomUUID();
                // 宿主相位 + 本监听动作：投递物被触发时（残片拾取 / 铁蒺藜被踩中）经
                // (casterId, phaseId) 反查本实例并 fireTrigger，而 fireTrigger 只认
                // 「实例所属相位」的 actions —— 若直接拿 host 建实例，监听里声明的
                // ON_PICKUP / ON_HIT 动作将永不可达（Mana Remnants 的回魔即栽在这里）。
                SkillPhase hostPhase = effectiveHost(host);
                SkillPhaseInstance phaseInstance = new SkillPhaseInstance(hostPhase, newPhaseId);
                // 等级交给阶段实例携带：本次回调产出的投递物（残片）可能在若干 tick 后才被触发，
                // 那时已无从判断它属于哪颗宝石——由实例在 fireTrigger 前回填本快照。
                phaseInstance.setSkillLevel(skillLevel);
                SkillPhaseInstanceTracker.register(caster.getUUID(), newPhaseId, phaseInstance);
                if (caster instanceof LivingEntity casterLiving) {
                    ExpressionContext.get(casterLiving).setSkillLevel(skillLevel);
                }
                ActionContext ctx = new ActionContext(
                        skill, caster, Optional.empty(),
                        pos, Vec3.ZERO, 1.0f, 0, bus, newPhaseId, null, Optional.ofNullable(eventCtx));
                phaseInstance.setActionContext(ctx);
                try {
                    for (TriggeredAction ta : actions) {
                        if (!isListenTrigger(ta.trigger())) continue;
                        // 进入技能 stat 作用域：事件监听回调（on_crit / on_kill / on_hit 等）发生于宿主技能
                        // 作用域出栈之后（异步触发），若不重入作用域，MetaSkillAction 累积元能量时裸读
                        // META_ENERGY_MAX 将落在全局作用域而非 {meta}，导致 WireResource:105/107/109/295
                        // 「Meta Skills have +X% maximum Energy」类已接线条目恒不匹配、全为死线；
                        // 其它事件触发动作（伤害 / 附加 / 转伤等）亦丢失 attack/spell/meta/元素等技能标签。
                        // 与 SkillPhase.executeScoped / SkillPhaseInstance.fireTrigger 同源：以宿主 SkillInstance
                        // 全量标签为基底重入作用域（enterAction 内部按 ctx.skill() 重新并入，异步命中同源）。
                        try (var scope = SkillStatScopes.enterAction(ctx, hostPhase.delivery().orElse(null), ta.action())) {
                            ta.action().execute(ctx);
                        }
                    }
                } finally {
                    // 归还注册时的初始引用：投递物生成时各自 retain（+1），销毁时 release（-1）；
                    // 若本次未生成任何投递物，此处归零即从索引移除，避免常驻监听累积泄漏。
                    SkillPhaseInstanceTracker.release(caster.getUUID(), newPhaseId);
                }
            }
        }));
    }

    /**
     * 事件派生相位的宿主 —— 保留宿主的投递与子相位（命中判定 / 存活判定的既有语义不变），
     * 把<b>本监听声明的动作</b>并入动作表。
     *
     * <p><b>为什么必须合成</b>：本监听产出的投递物（残片 / 危害物）在若干 tick 后被触发时，
     * 回调经 {@code (casterId, phaseId)} 反查到的就是这里建的实例，而
     * {@code SkillPhaseInstance#fireTrigger} 只遍历该实例<b>所属相位</b>的 actions。
     * 常驻监听的动作写在 {@link EventListenPhase} 上、宿主相位（精魂的自施放相位）通常一条动作都没有，
     * 直接用 host 会让「拾取残片 → 回复魔力」这类动作<b>永远找不到发射点</b>。
     *
     * <p><b>eventListen 留空</b>：合成相位只作为 fireTrigger 的宿主，从不经
     * {@code SkillPhase#initialize} 初始化，带上监听会让它在别处被二次注册。
     */
    private SkillPhase effectiveHost(SkillPhase host) {
        if (host == null) return new SkillPhase(Optional.empty(), actions, List.of(), Optional.empty());
        List<TriggeredAction> merged = new ArrayList<>(host.actions());
        merged.addAll(actions);
        return new SkillPhase(host.delivery(), merged, host.children(), Optional.empty());
    }

    /**
     * 事件回调应执行哪些触发器：{@code ON_HIT}（既有语义——Siphon Elements 等以 ON_HIT 表示
     * 「事件触发即执行」）与 {@code ON_LISTEN}（新增的显式语义）。
     *
     * <p><b>{@code ON_PICKUP} 不在此列</b>：它属于「投递物被触发」的回调（残片被拾取），
     * 由 {@code SkillPhaseInstance.fireTrigger} 在拾取时刻点燃。若事件回调也一并执行，
     * 就会出现「击杀瞬间即回魔」，而真值要求<b>拾取残片</b>才回魔（且残片尚未生成）。
     */
    private static boolean isListenTrigger(LifecycleTrigger trigger) {
        return trigger == LifecycleTrigger.ON_HIT || trigger == LifecycleTrigger.ON_LISTEN;
    }

    /**
     * 将 SkillEventType 枚举映射到 SkillEvent sealed 类的具体子类型。
     */
    private static Class<? extends SkillTriggerEvent> resolveEventClass(SkillEventType eventType) {
        return switch (eventType) {
            case CAST -> SkillCastEvent.class;
            case HIT -> SkillHitEvent.class;
            case KILL -> SkillKillEvent.class;
            case EXPIRE -> SkillExpireEvent.class;
            case CUSTOM -> CustomSkillEvent.class;
            case STAGGER -> StaggerEvent.class;
            case AILMENT -> AilmentEvent.class;
            case COMBO -> ComboEvent.class;
            case CHARGE_CHANGE -> ChargeChangeEvent.class;
            case RESERVATION_CHANGE -> ReservationChangeEvent.class;
        };
    }
}
