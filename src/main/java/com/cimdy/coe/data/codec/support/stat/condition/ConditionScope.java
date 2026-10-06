package com.cimdy.coe.data.codec.support.stat.condition;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 条件解析作用域（Condition 模型的唯一入参）。
 * <p>
 * <b>职责</b>：一次事件回调中「你 / 我 / 它 / 它们」的全部绑定。Target 负责锁定最基本的两个项
 * （{@link #owner} = A「你」，{@link #triggerTarget} = B「承受者」），而<b>事件回调的 Context 本身携带两方</b>
 * —— KILL 事件传入（你，被杀实体）、HIT 事件传入（你，被命中实体）。因此本作用域额外携带
 * {@link #eventEntity}：<b>本次事件的对象</b>（被杀 / 被命中实体）。
 * <p>
 * <b>为什么必须有 {@code eventEntity} 这一槽</b>：旧的
 * {@link com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext} 只有
 * {@code owner / triggerTarget / eventValue / center} 四槽，没有任何「本次被杀实体」的绑定，
 * 于是「你杀死的那个敌人」只能硬编码（{@code KillBurstHelper} 的半径 2.5 / 10% 最大生命 / 火类型）。
 * 本槽就是为消除该硬编码而存在。
 *
 * <ul>
 *   <li>{@code owner}：A —— stat 持有者（"你"）。条件中裸读的参照者，也是 {@code filter} 中谓词的
 *       {@code ValueScope#current()}（与既有 {@code GateExpr#matches / StatEngine#conditionsHold} 口径一致：裸读 = 自侧）。</li>
 *   <li>{@code triggerTarget}：B —— 事件的另一方（命中结算中双方互持的「交互对方」）。</li>
 *   <li>{@code eventEntity}：本次事件的对象（KILL = 被杀实体；HIT = 被命中实体）。与 {@code triggerTarget}
 *       在多数事件下同源，但事件语义是<b>显式的</b>，不再靠调用点约定。</li>
 *   <li>{@code anchor}：当前锚点（{@link ChainCondition} 逐元素换锚点时被覆写）。半径 / 归属类节点的默认中心。</li>
 *   <li>{@code base}：入参实体集（集合运算与 {@code filter} 的作用对象）。默认 = 事件语义下的「目标锁定」结果
 *       （{@code eventEntity → triggerTarget → owner} 取第一个非空者），即 <b>Target 的两个基本项由事件上下文决定</b>。</li>
 *   <li>{@code center}：几何中心覆写（命中点 / 施法点等）。</li>
 *   <li>{@code statContext}：触发型路径下的事件作用域（标签 / 技能 / 事件名 / 命中上下文），
 *       供 {@link FilterCondition} 复用既有 {@code ValueExpr} 谓词时原样注入，避免语义漂移。</li>
 * </ul>
 */
public record ConditionScope(
        @Nullable LivingEntity owner,
        @Nullable LivingEntity triggerTarget,
        @Nullable LivingEntity eventEntity,
        @Nullable LivingEntity anchor,
        @Nullable String eventName,
        SkillContext.@Nullable HitContext hitContext,
        @Nullable Vec3 center,
        @Nullable StatContext statContext,
        List<LivingEntity> base
) {

    /** 最小构造：只有 A（被动 / 自侧读取场景）。 */
    public static ConditionScope of(LivingEntity owner) {
        return of(owner, null, null, null, null, null, null);
    }

    /**
     * 事件构造（触发型路径主入口）：A + B + 事件对象 + 事件名 + 命中上下文 + 几何中心。
     * {@code base} 由事件语义推导（{@code eventEntity → triggerTarget → owner}），即 Target 的锁定结果。
     */
    public static ConditionScope of(@Nullable LivingEntity owner,
                                    @Nullable LivingEntity triggerTarget,
                                    @Nullable LivingEntity eventEntity,
                                    @Nullable String eventName,
                                    SkillContext.@Nullable HitContext hitContext,
                                    @Nullable Vec3 center,
                                    @Nullable StatContext statContext) {
        List<LivingEntity> base;
        if (eventEntity != null) base = List.of(eventEntity);
        else if (triggerTarget != null) base = List.of(triggerTarget);
        else if (owner != null) base = List.of(owner);
        else base = List.of();
        return new ConditionScope(owner, triggerTarget, eventEntity, null,
                eventName, hitContext, center, statContext, base);
    }

    /**
     * 换锚点（{@link ChainCondition} 的多跳原语）：把元素 e 置为当前锚点，
     * 并把入参集收敛为 {@code [e]}、几何中心收敛为 e 的位置 —— 即「以它为锚点重新解析子条件」。
     */
    public ConditionScope withAnchor(LivingEntity element) {
        if (element == null) return this;
        return new ConditionScope(owner, triggerTarget, eventEntity, element,
                eventName, hitContext, element.position(), statContext, List.of(element));
    }

    /** 换入参集（集合运算 / 包装节点内部使用）。 */
    public ConditionScope withBase(List<LivingEntity> newBase) {
        return new ConditionScope(owner, triggerTarget, eventEntity, anchor,
                eventName, hitContext, center, statContext, newBase == null ? List.of() : newBase);
    }

    /** 换几何中心。 */
    public ConditionScope withCenter(@Nullable Vec3 newCenter) {
        return new ConditionScope(owner, triggerTarget, eventEntity, anchor,
                eventName, hitContext, newCenter, statContext, base);
    }

    /** 换事件作用域（{@code filter} 谓词复用既有 {@code ValueExpr} 时注入）。 */
    public ConditionScope withStatContext(@Nullable StatContext newStatContext) {
        return new ConditionScope(owner, triggerTarget, eventEntity, anchor,
                eventName, hitContext, center, newStatContext, base);
    }

    /**
     * 几何中心解析（半径类节点共用，避免各节点各写一套优先级）：
     * {@code 显式锚点 → 当前锚点 → 作用域 center → A 的位置}。
     */
    public Vec3 centerPosition(@Nullable LivingEntity explicit) {
        if (explicit != null) return explicit.position();
        if (anchor != null) return anchor.position();
        if (center != null) return center;
        return owner == null ? Vec3.ZERO : owner.position();
    }
}
