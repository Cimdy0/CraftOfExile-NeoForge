package com.cimdy.coe.data.codec.skill.runtime;

import com.cimdy.coe.data.codec.skill.context.SkillStatScopes;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area.AreaDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.action.combo.DeliveryAction;
import com.cimdy.coe.data.codec.skill.context.DamageContext;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.event.skill.SkillEvent;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SkillPhaseInstance {
    /** 本阶段本 tick 累计的伤害上下文（原 SkillPhaseContext 仅为其空壳包装，已去除该层）。 */
    private DamageContext damageContext = new DamageContext();
    private final SkillPhase phase;
    private final UUID phaseID;
    private int time = 0;
    private ActionContext actionContext;
    /** 子阶段是否已初始化（仅首次 tick 初始化一次，避免每 tick 重复生成投递物）。 */
    private boolean childrenInitialized = false;

    /** 本阶段实例是否已触发过 ON_RELEASE（确保引导松开只生效一次，避免每 tick 重复触发）。 */
    private boolean releaseFired = false;

    /**
     * 回响重放的<b>范围载荷</b>：{@code echoAreaInc} 为 POE2「increased」项（与 EFFECT_AREA 相加），
     * {@code echoAreaMore} 为「more」项（乘在结果上）。原施放恒为 {@code 0.0 / 1.0}，故不受影响。
     * 由 {@code SkillPhase#repeat} 写入，经 {@code SkillPhase#initialize} 传给整棵子阶段树，
     * 消费端为 {@code AreaDeliveryExecutor}（含范围 / 危害 / 残留 / 光束 / 波 全部几何）。
     */
    private double echoAreaInc = 0.0;
    private double echoAreaMore = 1.0;

    /**
     * 本实例<b>由动作生成</b>的投递物（{@code DeliveryAction} 在事件回调等路径上放出的残片 / 危害物 …）。
     *
     * <p>常态为 null —— 常规路径下投递物由 {@code SkillPhase#initialize} 按 {@code phase.delivery()} 生成，
     * 相位自己声明了它。而<b>事件派生</b>的投递物（「击杀生成残片」「翻滚撒铁蒺藜」）在宿主相位里
     * <b>没有任何声明</b>：它只存在于执行那一次的动作实参里。等它若干 tick 后被触发（残片拾取 /
     * 铁蒺藜被踩中），回调要按「真正生成它的那个投递」取命中判定与 stat 标签作用域
     * （危害物 → {@code hazard}），缺此字段这些标签就永不生效（死标签）。
     */
    private Delivery spawnedDelivery = null;

    public SkillPhaseInstance(SkillPhase phase, UUID phaseID) {
        this.phase = phase;
        this.phaseID = phaseID;
    }

    /** 登记本实例实际生成的投递物（由 {@code DeliveryAction} 调用）。 */
    public void setSpawnedDelivery(Delivery delivery) {
        this.spawnedDelivery = delivery;
    }

    /**
     * 本实例回调时应按哪个投递求值：优先「由动作生成的那个」，否则回退相位声明的投递。
     * 事件派生投递物（无相位声明）由此拿到自己的命中判定与标签作用域。
     */
    public Optional<Delivery> effectiveDelivery() {
        return this.spawnedDelivery != null
                ? Optional.of(this.spawnedDelivery)
                : this.phase.delivery();
    }

    public SkillPhaseInstance setActionContext(ActionContext actionContext) {
        this.actionContext = actionContext;
        return this;
    }

    /**
     * 本阶段所属技能的施放上下文（无则 null）。
     *
     * <p>存在意义：跨 tick 常驻投递（{@code PersistentDeliveryManager} 驱动的光环 / 光束）
     * 在 tick 循环里只拿得到 {@link SkillPhaseInstance}，若用无参 {@code Value#getFloat()}
     * 求值半径，预设声明的 {@code $radius} 参数槽<b>永远读不到</b>（死线）。
     * 本访问器把 {@code fireTrigger} 内已有的取法（{@code actionContext.skill().getContext()}）
     * 提为公开单一来源，使常驻投递也能带 context 真读。
     */
    @Nullable
    public SkillContext skillContext() {
        return (this.actionContext != null && this.actionContext.skill() != null)
                ? this.actionContext.skill().getContext() : null;
    }

    /**
     * 上溯到所属技能实例（贯穿时间轴源头）。
     * 常驻投递运行时（{@code PersistentDeliveryManager}）据此读取 {@code SkillInstance.getTimeline()}，
     * 使「效果视觉 / 音效」与客户端动画（CastAnimationPacket 同源 startTime / castTimeTicks）
     * 对齐到同一条世界时钟 —— 攻击 / 施法速度经 SkillTimeline 已纳入，动画与效果同步伸缩。
     */
    @Nullable
    public SkillInstance skill() {
        return (this.actionContext != null) ? this.actionContext.skill() : null;
    }

    public List<SkillPhaseInstance> tick(SkillInstance skillInstance) {
        this.time++;
        List<SkillPhaseInstance> list = new ArrayList<>();

        // 机会性合并本 phase 本 tick 累计的伤害上下文（键不匹配时为 no-op）
        Pair<UUID, UUID> pair = Pair.of(skillInstance.getContext().getCaster().getUUID(), this.phaseID);
        if (SkillEvent.cacheDamageContext.containsKey(pair)) {
            this.damageContext = SkillEvent.cacheDamageContext.get(pair);
            SkillEvent.cacheDamageContext.remove(pair);
        }

        // 子阶段只在首次 tick 初始化一次，避免每 tick 重复生成投递物
        if (!this.childrenInitialized) {
            list.addAll(phase.runChildren(skillInstance, this));
            this.childrenInitialized = true;
        }

        // 每 tick 执行持续动作（on_tick / on_combo / on_release）
        phase.runRuntime(skillInstance, this);

        // 生命周期判定：仍活跃则保留，否则丢弃
        if (phase.isAlive(skillInstance, this)) {
            list.add(this);
        }

        this.damageContext = new DamageContext();
        return list;
    }

    public int time() {
        return time;
    }

    public UUID getPhaseID() {
        return phaseID;
    }

    public SkillPhase getPhase() {
        return phase;
    }

    public Projectile getProjectile(Level level) {
        return (Projectile) level.getEntity(this.getPhaseID());
    }

    /** 本阶段本 tick 累计的伤害上下文（供 on_tick 判定/damage.* 读值）。 */
    public DamageContext getDamageContext() {
        return damageContext;
    }

    /** ON_RELEASE 是否已触发过（引导松开一次性语义）。 */
    public boolean isReleaseFired() {
        return releaseFired;
    }

    /** 标记 ON_RELEASE 已触发，防止后续 tick 重复触发。 */
    public void markReleaseFired() {
        this.releaseFired = true;
    }

    /** 写入回响范围载荷（仅 {@code SkillPhase#repeat} 调用）。 */
    public SkillPhaseInstance setEchoArea(double areaInc, double areaMore) {
        this.echoAreaInc = areaInc;
        this.echoAreaMore = areaMore;
        return this;
    }

    /** 回响的「increased」范围增量（加法项，与 EFFECT_AREA 相加）；非回响为 0.0。 */
    public double echoAreaInc() {
        return echoAreaInc;
    }

    /** 回响的「more」范围倍率（乘法项）；非回响为 1.0。 */
    public double echoAreaMore() {
        return echoAreaMore;
    }

    /** 本阶段求值所用的宝石等级快照（{@code < 0} = 不注入）。 */
    private double skillLevel = -1;

    /**
     * 写入本阶段求值所用的宝石等级（由 {@code EventListenPhase} 在注册时快照）；{@code < 0} 表示不注入。
     *
     * <p>常驻监听产出的投递物（残片等）在被触发时已脱离施放路径，而 {@code skill.level}
     * 读的是<b>实体级上下文字段</b>，会被其它技能的施放覆盖。携带快照后在回调求值前回填，
     * 保证「每枚残片回复 (28—281) 魔力」按<b>产出它的那颗宝石</b>的等级成长。
     */
    public SkillPhaseInstance setSkillLevel(double v) {
        this.skillLevel = v;
        return this;
    }

    /**
     * 点燃所有匹配 trigger 的 TriggeredAction。
     * 被 entity 命中回调、伤害击杀检测、到期检测调用。
     *
     * @param trigger 生命周期触发器 (ON_HIT / ON_KILL / ON_EXPIRE 等)
     * @param hit     命中上下文，ON_EXPIRE / ON_KILL 时可为 null
     */
    public void fireTrigger(LifecycleTrigger trigger, @Nullable HitContext hit) {
        if (phase.actions() == null || phase.actions().isEmpty()) return;

        // 命中过滤：仅当 delivery 的 hit_test 经统一 ExContext 判定通过（值 ≥ 1），才点燃对应的 ON_HIT。
        // 命中信息（hit_state:* 读值节点）与技能状态统一从 ExpressionContext 获取。
        if (hit != null) {
            Delivery delivery = effectiveDelivery().orElse(null);
            if (delivery != null) {
                SkillContext sc = (this.actionContext != null && this.actionContext.skill() != null)
                        ? this.actionContext.skill().getContext() : null;
                ExpressionContext.Builder hb = ExpressionContext.builder().hitContext(hit);
                if (sc != null) hb.skillContext(sc);
                if (this.actionContext != null && this.actionContext.caster() instanceof LivingEntity liv) {
                    hb.entity(liv).level(liv.level());
                }
                ExpressionContext hitEval = hb.build();
                if (delivery.hitTest().getValue(hitEval) < 1.0) {
                    return;
                }
            }
        }

        // 宝石等级回填（见 setSkillLevel）：投递物回调（残片拾取等）可能晚于事件若干 tick，
        // 此刻实体级上下文的等级早已被其它技能覆盖，须按本阶段快照还原后再求值。
        if (this.skillLevel >= 0 && this.actionContext != null
                && this.actionContext.caster() instanceof LivingEntity casterLiving) {
            ExpressionContext.get(casterLiving).setSkillLevel(this.skillLevel);
        }

        ActionContext base = this.actionContext;
        for (TriggeredAction ta : phase.actions()) {
            if (ta.trigger() != trigger) continue;

            ActionContext ctx = base;
            if (ctx == null) {
                ctx = new ActionContext(null, null, Optional.empty(),
                        Vec3.ZERO, Vec3.ZERO, 1.0f, 0, null, this.phaseID, null, Optional.empty());
            }
            if (hit != null) {
                // 命中数据单一来源：withHit 已同步位置/方向，目标/降伤倍率经 ActionContext 派生视图可见
                ctx = ctx.withHit(hit).withPhaseId(this.phaseID);
            }
            // 进入「投放 + 行为」标签作用域：命中/击杀等触发的动作也按投放形态（投射物/范围…）
            // 与行为自述标签（伤害元素等）即时求值，与阶段初始化/每 tick 执行同源。
            try (var scope = SkillStatScopes.enterAction(ctx, effectiveDelivery().orElse(null), ta.action())) {
                ta.action().execute(ctx);
            }
        }
    }

    /** 无命中上下文的重载（ON_EXPIRE / ON_KILL） */
    public void fireTrigger(LifecycleTrigger trigger) {
        fireTrigger(trigger, null);
    }

    public enum State {
        START, END
    }
}
