package com.cimdy.coe.data.codec.support.stat.engine;

import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 单次 stat 判定作用域内的实体缓存（cache + key）。
 * <p>
 * 一次「条件 → 数值 → 目标」的瞬间判定过程中，持有者自身（{@link TargetSelector.Source#SELF}）
 * 与触发目标（{@link TargetSelector.Source#TRIGGER_TARGET}）两个候选实体在作用域入口预填一次，
 * 后续三阶段各自以 {@link TargetSelector}（即钥匙）从本缓存取对应实体，从而令「被判定实体 /
 * 被取值实体 / 被作用实体」在属于同一作用域、且本就指向同一目标时，无需在三处重复写实体查找逻辑。
 *
 * <p>钥匙即 {@link TargetSelector}：
 * <ul>
 *   <li>{@code null} 或缺省 → 取持有者自身（SELF）；</li>
 *   <li>否则按其 {@code source} 槽位取对应实体（TRIGGER_TARGET 槽位为空时返回 {@code null}，
 *       由调用方判空跳过——跨实体条目在无交互对方时安全不生效）。</li>
 * </ul>
 *
 * <h2>构造约定：TRIGGER_TARGET 槽位必须填入交互对方</h2>
 * <b>属性读取 / fold 查询路径下，{@code triggerTarget} 恒取 {@link StatContext#sourceEntity()}</b>
 * （当前作用域绑定的交互对方，命中结算时由 {@code DamageEvent.enterHitScopes} 双向注入），
 * 统一经 {@link StatEngine#compute} 与 {@code StatEngine.evalCtx} 两处入口构造；
 * 触发型路径（{@link StatEngine#fire}）下直接为事件 target。
 *
 * <p><b>事件对象槽 {@code eventEntity}</b>（Phase 2a）：一次事件回调的 Context 本身就携带两方
 * （KILL =（你，被杀实体）、HIT =（你，被命中实体）），故「本次事件的对象」必须由作用域显式绑定，
 * 不能靠调用点临时约定。{@code triggerTarget} 是 Target 锁定的「承受者 B」，而 {@code eventEntity}
 * 是<b>事件语义上的对象</b>（被杀 / 被命中者）；二者在多数事件下同指一个实体，但在
 * 「承受侧」事件（{@code taken_hit} 的主语是承受者本人）下并不同源，故分列两槽。
 * 该槽是 {@link com.cimdy.coe.data.codec.support.stat.condition.ConditionScope#eventEntity()} 的
 * 唯一来源，使 {@code anchor:killed} / {@code anchor:hit} 无需硬编码即可解析。
 *
 * <p><b>不提供 {@code self(owner)} 式便捷构造</b>：该工厂会把 TRIGGER_TARGET 槽位硬写为
 * {@code null}，使七条 fold 通道上 {@code conditionTarget=trigger_target} 的跨实体条件恒解析为
 * {@code null} 而被静默跳过（条目永不生效且无告警）。移除该工厂即从类型层面迫使调用方显式交出
 * 作用域对方实体，避免同类缺陷复发。确需「无对方」语义时显式写
 * {@code new StatEvalContext(owner, null)} 并说明理由。
 */
    public record StatEvalContext(LivingEntity owner, @Nullable LivingEntity triggerTarget,
                                 @Nullable LivingEntity eventEntity,
                                 @Nullable Double eventValue, @Nullable Vec3 center) {

    /** 便捷构造：无事件对象、无事件数值载荷、无几何圆心。 */
    public StatEvalContext(LivingEntity owner, @Nullable LivingEntity triggerTarget) {
        this(owner, triggerTarget, null, null, null);
    }

    /** 便捷构造：无事件对象、无事件数值载荷，携带几何圆心（半径 Target 源用）。 */
    public StatEvalContext(LivingEntity owner, @Nullable LivingEntity triggerTarget, @Nullable Vec3 center) {
        this(owner, triggerTarget, null, null, center);
    }

    /** 便捷构造：无事件对象，携带事件数值载荷、无几何圆心（属性读取 / fold 查询路径）。 */
    public StatEvalContext(LivingEntity owner, @Nullable LivingEntity triggerTarget, @Nullable Double eventValue) {
        this(owner, triggerTarget, null, eventValue, null);
    }

    /**
     * 事件构造（触发型路径主入口）：携带<b>事件对象</b>（被杀 / 被命中实体）与事件数值载荷。
     * 仅 {@code StatEngine#fire} 的击杀 / 命中链路使用；其余事件无「事件对象」语义，走上面的便捷构造
     * （{@code eventEntity} 为 null → {@code anchor:killed} / {@code anchor:hit} 解析为空集，符合语义）。
     */
    public StatEvalContext(LivingEntity owner, @Nullable LivingEntity triggerTarget,
                           @Nullable LivingEntity eventEntity, @Nullable Double eventValue) {
        this(owner, triggerTarget, eventEntity, eventValue, null);
    }

    /**
     * 持有者自身（SELF 槽位，恒非 null）。
     */
    @Override
    public LivingEntity owner() {
        return owner;
    }

    /**
     * 触发目标（TRIGGER_TARGET 槽位，可能为 null）。
     */
    @Override
    @Nullable
    public LivingEntity triggerTarget() {
        return triggerTarget;
    }

    /**
     * 本次事件的对象（KILL = 被杀实体；HIT = 被命中实体；非击杀 / 命中事件为 null）。
     * <p>即新条件模型 {@link com.cimdy.coe.data.codec.support.stat.condition.ConditionScope#eventEntity()}
     * 的来源，供 {@code anchor:killed} / {@code anchor:hit} 锚点解析。
     */
    @Override
    @Nullable
    public LivingEntity eventEntity() {
        return eventEntity;
    }

    /**
     * 按钥匙取单一实体（条件判定 / 数值求值阶段用）：selector 为 null → 持有者自身；
     * 否则按 source 槽位取（TRIGGER_TARGET 为空时返回 null；PRESENCE_TARGETS 多目标场景
     * 在单实体取用路径下退化为持有者自身，批量应用应走 {@link #resolveTargets}）。
     */
    @Nullable
    public LivingEntity resolve(@Nullable TargetSelector selector) {
        if (selector == null) return owner;
        return switch (selector.source()) {
            case SELF -> owner;
            case TRIGGER_TARGET -> triggerTarget;
        };
    }

    /**
     * 按钥匙取目标实体列表（作用阶段 / 聚合门控用），委托 {@link TargetSelector#resolve}。
     */
    public List<LivingEntity> resolveTargets(@Nullable TargetSelector selector) {
        return selector != null
                ? selector.resolve(owner, triggerTarget)
                : List.of(owner);
    }
}
