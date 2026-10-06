package com.cimdy.coe.data.codec.skill.context;

import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.codec.support.stat.effect.damage.DamageStatEffect;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.event.combat.DamageEvent;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * 技能伤害源 — 在普通 {@link DamageSource} 之上附加「技能上下文」与「暴击结果」，
 * 使权威战斗链路（{@code DamageEvent}）能据此统一发布元事件（on_crit / on_kill / …），
 * 而无需在技能动作层重复推导暴击/异常/击杀。
 *
 * <p>深度合并要点：
 * <ul>
 *   <li>{@link #getSkill()} 返回造成此伤害的技能实例（无则 null，如原版攻击 / 异常 DoT）；</li>
 *   <li>{@link #isCrit()} 返回该次伤害是否暴击，由 {@code DamageAction} 在技能侧算定后透传，
 *       避免 {@code DamageEvent} 用攻击暴击属性重算导致双重暴击；</li>
 *   <li>原版攻击走 {@code SkillDamageSource(type, direct, causing, pos)}（skill=null, crit=false），
 *       仍由 {@code DamageEvent} 按 {@code ATTACK_CRIT_CHANCE} 判定暴击。</li>
 * </ul>
 */
public class SkillDamageSource extends DamageSource {
    private final UUID ID;
    @Nullable
    private final SkillInstance skill;
    private final boolean crit;
    /**
     * 造成此伤害的技能阶段实例 ID。用于把命中伤害按 (caster, phaseId) 索引进
     * {@code DamageContext.cacheDamageContext}，使该阶段的 {@code on_tick} 能读到本 tick 命中累计。
     * 近战 / 原版攻击等非阶段伤害源为 null。
     */
    @Nullable
    private final UUID phaseId;
    /**
     * 伤害性异常（DoT）的「异常名」标签（poison/ignite/bleed 等）。仅由拆分 DoT 伤害源携带，
     * 用于在伤害事件（减免前）经施法者 Stat 解析 ailment_output_convert 时定位作用域
     * {ailment, 异常名, 来源类型, 目标类型}。非异常伤害源为 null。
     */
    @Nullable
    private final String ailmentTag;
    /**
     * 由 Stat 触发型效果（{@code DamageStatEffect} 等）施加的伤害标记：携带该标记的伤害源
     * 进入 {@code DamagePipeline} 时，{@code stageTrigger} 会跳过整段 fire——
     * 防止「击中时造成伤害」这类条目自我重入触发管线（B-0.2）。Skill 侧 / 原版伤害恒为 false。
     */
    private final boolean statTriggered;
    /**
     * 合成伤害标记（heart_of_ice 等「视作造成 X 冰霜伤害」类）：携带该标记的伤害源进入
     * {@code DamagePipeline} 时，{@code stageReduction} 会把 newDamage 归零——不造成任何真实生命/资源
     * 丢失，仅借伤害管线触发冰缓（stageAilmentAndCrit 的 applyColdAilment 在 REDUCTION 之前已跑过）。
     * 与 statTriggered 正交：合成伤害同时也不二次触发 stat 效果（stageTrigger 跳过）。
     */
    private final boolean synthetic;

    public SkillDamageSource(
            Holder<DamageType> type,
            @Nullable Entity directEntity, @Nullable Entity causingEntity,
            @Nullable Vec3 damageSourcePosition
    ) {
        this(type, directEntity, causingEntity, damageSourcePosition, null, false, null, null, false, false);
    }

    /** 拆分 DoT 专用构造：携带异常名标签，供伤害事件减免前做 ailment_output_convert 类型重路由。 */
    public SkillDamageSource(
            Holder<DamageType> type,
            @Nullable Entity directEntity, @Nullable Entity causingEntity,
            @Nullable Vec3 damageSourcePosition,
            @Nullable String ailmentTag
    ) {
        this(type, directEntity, causingEntity, damageSourcePosition, null, false, null, ailmentTag, false, false);
    }

    public SkillDamageSource(
            Holder<DamageType> type,
            @Nullable Entity directEntity, @Nullable Entity causingEntity,
            @Nullable Vec3 damageSourcePosition,
            @Nullable SkillInstance skill, boolean crit
    ) {
        this(type, directEntity, causingEntity, damageSourcePosition, skill, crit, null, null, false, false);
    }

    public SkillDamageSource(
            Holder<DamageType> type,
            @Nullable Entity directEntity, @Nullable Entity causingEntity,
            @Nullable Vec3 damageSourcePosition,
            @Nullable SkillInstance skill, boolean crit, @Nullable UUID phaseId,
            @Nullable String ailmentTag
    ) {
        this(type, directEntity, causingEntity, damageSourcePosition, skill, crit, phaseId, ailmentTag, false, false);
    }

    public SkillDamageSource(
            Holder<DamageType> type,
            @Nullable Entity directEntity, @Nullable Entity causingEntity,
            @Nullable Vec3 damageSourcePosition,
            @Nullable SkillInstance skill, boolean crit, @Nullable UUID phaseId,
            @Nullable String ailmentTag, boolean statTriggered
    ) {
        this(type, directEntity, causingEntity, damageSourcePosition, skill, crit, phaseId, ailmentTag, statTriggered, false);
    }

    public SkillDamageSource(
            Holder<DamageType> type,
            @Nullable Entity directEntity, @Nullable Entity causingEntity,
            @Nullable Vec3 damageSourcePosition,
            @Nullable SkillInstance skill, boolean crit, @Nullable UUID phaseId,
            @Nullable String ailmentTag, boolean statTriggered, boolean synthetic
    ) {
        super(type, directEntity, causingEntity, damageSourcePosition);
        this.ID = UUID.randomUUID();
        this.skill = skill;
        this.crit = crit;
        this.phaseId = phaseId;
        this.ailmentTag = ailmentTag;
        this.statTriggered = statTriggered;
        this.synthetic = synthetic;
    }

    public SkillDamageSource(DamageSource source) {
        this(source, null, false, null);
    }

    public SkillDamageSource(DamageSource source, @Nullable SkillInstance skill, boolean crit) {
        this(source, skill, crit, null);
    }

    public SkillDamageSource(DamageSource source, @Nullable SkillInstance skill, boolean crit, @Nullable UUID phaseId) {
        this(source.typeHolder(), source.getDirectEntity(), source.getEntity(),
                source.getSourcePosition(), skill, crit, phaseId,
                source instanceof SkillDamageSource s ? s.getAilmentTag() : null,
                source instanceof SkillDamageSource s ? s.isStatTriggered() : false,
                source instanceof SkillDamageSource s ? s.isSynthetic() : false);
    }

    public UUID getUUID() {
        return ID;
    }

    @Nullable
    public SkillInstance getSkill() {
        return skill;
    }

    /**
     * 「技能标识」推导（全工程唯一实现）：仅当伤害来自某个技能（本类且携带 {@link SkillInstance}）
     * 时返回其基础技能石 id，使「对某个具体技能生效」的 stat 加成（如「对 [技能名] 的伤害」）
     * 在本次结算内精确匹配；原版攻击 / 异常 DoT 等无技能来源返回 null。
     * <p>{@code skillIdsOf(DamageSource)} 的逐字重复实现不散落在 {@code DamageEvent} /
     * {@code DamagePipeline} / {@code AilmentHelper} 各处，而收敛于本类单一来源。
     */
    @Nullable
    public static Identifier skillIdOf(@Nullable DamageSource source) {
        return source instanceof SkillDamageSource sds ? skillIdOf(sds.getSkill()) : null;
    }

    /** 「技能标识」推导（技能实例入口，与 {@link #skillIdOf(DamageSource)} 同源）：无技能或无 id 则 null。 */
    @Nullable
    public static Identifier skillIdOf(@Nullable SkillInstance skill) {
        return skill != null ? skill.getSkillId() : null;
    }

    public boolean isCrit() {
        return crit;
    }

    @Nullable
    public UUID getPhaseId() {
        return phaseId;
    }

    @Nullable
    public String getAilmentTag() {
        return ailmentTag;
    }

    /** 是否由 Stat 触发型效果施加（用于 DamagePipeline 跳过二次 fire，B-0.2）。 */
    public boolean isStatTriggered() {
        return statTriggered;
    }

    /** 是否合成伤害（用于 DamagePipeline 跳过真实生命/资源扣除，仅触发冰缓）。 */
    public boolean isSynthetic() {
        return synthetic;
    }

}
