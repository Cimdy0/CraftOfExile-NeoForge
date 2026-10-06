package com.cimdy.coe.data.codec.support.stat.engine;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.event.combat.DamageEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * 事件作用域：描述「当前这次行为」的上下文，用于过滤 {@link ModifierStatEffect}。
 * <p>
 * 一条 stat entry 只有在它的标签/条件要求是当前作用域满足时才生效——
 * 这把原本「全局瞬态写入、所有并发行为共享同一份增益」的错误模型，
 * 改成「读时按本次事件作用域即时计算」，一次事件中多个并发行为各自独立求值，互不串味。
 * 条目的所需条件挂在 entry 上、由 {@link ExpressionContext} 实时判定（事件/计时/实体状态），
 * 经 {@link GateExpr#matches} / {@link StatEngine#conditionsHold} 与标签一并求值，并非本作用域的存储字段。
 *
 * <ul>
 *   <li>{@code tags}：本次行为携带的标签（"attack" / "spell" / "element_fire" / "weapon_bow" …，直接为字符串，
 *       也包括技能名、技能作者标签、武器需求、消耗类型等任意数据驱动值），静态作用域，
 *       由条目侧 {@link GateExpr}（标签布尔表达式：与/或/非/嵌套）对之求值判定。
 *       技能标识在传入时即 {@code toString()} 并入本集合，使「技能专属效果」经标签表达式判定自然隔离。</li>
 *   <li>{@code skill}：本次行为激活的技能标识（{@link net.minecraft.resources.Identifier}，数据驱动、名字任意，可为空），
 *       作为技能身份元数据保留（其字符串形式已并入 {@code tags} 参与匹配，本字段不再单独作为匹配要求）。</li>
 * </ul>
 *
 * 空作用域（{@link #EMPTY}）下只有「无标签、无条件」的被动条目生效，相当于常驻天赋。
 *
 * <p>{@code sourceEntity}：命中结算时双方作用域互持「交互对方」引用——受害者持攻击者、攻击者持受害者。
 * 一方读取属性时：① 额外聚合对方挂载的 {@code target}（敌方维度）条目（见 {@link StatEngine#compute} /
 * {@link StatEvalContext#resolveTargets}）；② 自身条目上 {@code conditionTarget=TRIGGER_TARGET}
 * 的条件按对方求值（如「对冰缓敌人伤害提高」按受害者的 StatusEngine 状态判定）。
 * 非命中场景恒为 {@code null}，不影响既有被动/自作用域逻辑。运行时字段、不参与序列化。
 */
public record StatContext(Set<String> tags, @Nullable Identifier skill, LivingEntity sourceEntity,
                          @Nullable String eventName, @Nullable HitContext hitContext) {

    public static final StatContext EMPTY = new StatContext(Set.of(), null, null, null, null);

    /** 便捷构造：仅标签。 */
    public static StatContext of(String... tags) {
        return new StatContext(Set.of(tags), null, null, null, null);
    }

    /**
     * 便捷构造（唯一 2 参入口）：标签 + 技能。技能名 {@code toString()} 一并写入标签集，
     * 使「技能专属效果」经标签子集判定自然隔离——调用方无需再手动 {@code tags.add(skill.toString())}
     * （{@code SkillStatScopes} / {@code DamageEvent} / {@code MetaSkillAction} 的逐字重复实现已收敛于此）。
     * <p>传入集合不被修改（内部复制），调用方可安全复用同一份标签集发多次事件；{@code skill} 为 null 时不并入。
     */
    public static StatContext of(Set<String> tags, @Nullable Identifier skill) {
        return of(tags, skill, null, null);
    }

    /**
     * 事件 fire 用完整构造：标签 + 技能 + 触发事件名 + 命中上下文（均可为空）。
     * {@code eventName} 供 {@code event:}/{@code event_tag:} 节点经 {@link #evalBase} 注入求值基座判定；
     * {@code hitContext} 供 {@code hit_state:*} 节点判定。技能名同 {@link #of(Set, Identifier)} 并入标签集。
     */
    public static StatContext of(Set<String> tags, @Nullable Identifier skill,
                                 @Nullable String eventName, @Nullable HitContext hitContext) {
        if (skill == null) return new StatContext(tags, null, null, eventName, hitContext);
        Set<String> merged = new HashSet<>(tags);
        merged.add(skill.toString());
        return new StatContext(merged, skill, null, eventName, hitContext);
    }

    /** 返回带 sourceEntity 的副本（用于命中受害者作用域挂载攻击者）。 */
    public StatContext withSourceEntity(LivingEntity sourceEntity) {
        return new StatContext(tags, skill, sourceEntity, eventName, hitContext);
    }

    /**
     * 构造本次作用域下的求值基座：实体单例 + 事件名/事件标签（{@code event:}/{@code event_tag:} 节点）
     * + 命中上下文（{@code hit_state:*} 节点）。供 StatEngine.fire 的条件/数值求值统一使用——
     * 求值基座须为完整 {@code ExpressionContext}；裸 {@code ExpressionContext.get()} 会让 event:/hit_state 在 Stat 通道恒 0。
     */
    public ExpressionContext evalBase(LivingEntity e) {
        if (e == null) return null;
        ExpressionContext ec = ExpressionContext.get(e);
        if (eventName != null) ec = ec.withEvent(eventName, new ArrayList<>(tags));
        if (hitContext != null) ec = ec.withHit(hitContext);
        return ec;
    }

    @Override
    public String toString() {
        return "StatContext{tags=" + Arrays.toString(tags.toArray())
                + ", skill=" + skill + ", sourceEntity=" + sourceEntity + '}';
    }
}
