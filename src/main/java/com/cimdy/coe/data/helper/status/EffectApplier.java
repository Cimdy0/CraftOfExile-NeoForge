package com.cimdy.coe.data.helper.status;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;


import com.cimdy.coe.data.helper.hub.ResourceEventHelper;
import com.cimdy.coe.data.codec.support.stat.effect.resource.ResourceStatEffect;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.skill.context.SkillDamageSource;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * 非技能效果施加原语 — 从 Skill Action（{@code HealAction} / {@code AilmentAction}）抽离的
 * 「与技能执行无关」的效果施加逻辑，作为 Skill 与 Stat 两套体系的共同底座：
 * <ul>
 *   <li>Skill 侧：{@code HealAction} / {@code AilmentAction} 委托本类施加效果；</li>
 *   <li>Stat 侧：{@code ResourceStatEffect} 等触发型效果在事件触发（击杀回血 / 击中回蓝 / 击中施加异常）时
 *       复用同一原语，保证两侧行为一致。异常修正（积蓄/时长/强度）已归一为属性亚型，不再走本类。</li>
 * </ul>
 * 本类只做「施加」，不做目标选择与条件过滤——目标由调用方（ActionContext 或 Stat 事件作用域）给出，
 * 条件由调用方（Skill Condition 或 GateExpr#matches / StatEngine#conditionsHold）过滤。
 */
public final class EffectApplier {
    private EffectApplier() {
    }

    /**
     * 资源恢复原语：按资源类型恢复目标生命 / 魔力 / 能量护盾（SPIRIT 为保留资源，暂不支持直接治疗）。
     * amount &le; 0 时不生效。
     */
    public static void heal(LivingEntity target, ResourceType resource, float amount) {
        heal(target, resource, amount, false);
    }

    /**
     * 资源恢复原语（可指定溢出上限）：{@code allowOverflow=true} 时魔力回复可超过上限
     * （真值「Mana Remnants can Overflow maximum Mana」），其余资源与常规钳制通道一致。
     * amount &le; 0 时不生效。
     */
    public static void heal(LivingEntity target, ResourceType resource, float amount, boolean allowOverflow) {
        if (amount <= 0) return;
        // 经统一资源回调枢纽（数值/溢出/检测/转化一致）；SPIRIT 保留不动
        switch (resource) {
            case HEALTH, MANA, ENERGY -> ResourceEventHelper.onHeal(target, resource.getSerializedName(), amount, "stat", null, allowOverflow);
            case RAGE -> ResourceEventHelper.onHeal(target, GateTag.RAGE, amount, "stat", null, allowOverflow);
            default -> { /* SPIRIT 保留资源，暂不支持直接治疗 */ }
        }
    }

    /**
     * 资源扣除原语（与 {@link #heal} 对称的即时扣除方向）：按资源类型即时扣除目标资源。
     * <ul>
     *   <li>生命：走伤害管线（statTriggered，不再二次触发 stat fire）——POE「技能扣血」即伤害；</li>
     *   <li>魔力 / 能量护盾 / 怒气：直接扣除（ManaHelper / EnergyHelper / RageHelper）。</li>
     * </ul>
     * SPIRIT（保留）/ COMBO / GLORY（技能级）不在此通道——{@code ResourceStatEffect} 构造器已校验拒绝。
     *
     * @param attacker 伤害来源（仅生命扣除使用；其余资源可 null）
     */
    public static void cost(LivingEntity target, ResourceType resource, float amount, @Nullable LivingEntity attacker) {
        if (amount <= 0) return;
        switch (resource) {
            case HEALTH -> damage(target, attacker, amount, GateExpr.ALWAYS);
            case MANA -> ResourceEventHelper.onSpend(target, GateTag.MANA, amount, "stat", null);
            // 元技能能量（参考荣耀；无技能上下文 → "default" 槽）
            case ENERGY -> ResourceRegistry.consumeMetaEnergy(target, "default", (int) amount);
            case RAGE -> ResourceRegistry.spendRage(target, amount);
            default -> { /* SPIRIT/COMBO/GLORY：构造器已拒绝，防御性跳过 */ }
        }
    }

    /**
     * 伤害施加原语：经原版伤害管线对目标造成伤害，受护甲 / 魔抗影响。
     * <b>注意</b>：本方法走 {@code target.hurt} 会触发模组 {@code LivingDamageEventPre}
     * → {@code DamagePipeline} 完整结算（暴击 / 异常 / 减免 / 触发）——并非"不接入模组伤害机制"。
     * 若需带 Stat 触发语义（不二次触发 stat 效果），请用带 {@code tags} 的重载。
     *
     * @param target   受击实体
     * @param attacker 伤害来源（用于伤害来源判定）
     * @param amount   伤害量
     */
    public static void damage(LivingEntity target, LivingEntity attacker, float amount) {
        if (amount <= 0) return;
        target.hurt(attacker.level().damageSources().magic(), amount);
    }

    /**
     * Stat 触发伤害原语（B-0.2）：与 {@link #damage(LivingEntity, LivingEntity, float)} 相同走原版
     * 伤害管线，但伤害源携带 {@code statTriggered} 标记——{@code DamagePipeline.stageTrigger} 检测到
     * 该标记后跳过整段 fire，使「击中时造成伤害」等 Stat 条目不会自我重入触发管线。
     * {@code tags} 参数当前用于占位声明（词缀事件标签表达式，后续随 DamageSource 标签通道扩展），
     * 语义收敛见 3-3 事件键统一。
     *
     * @param target   受击实体
     * @param attacker 伤害来源（用于伤害来源判定）
     * @param amount   伤害量
     * @param tags     本词缀事件标签表达式（hit / crit 等，当前仅文档化）
     */
    public static void damage(LivingEntity target, LivingEntity attacker, float amount, GateExpr tags) {
        if (amount <= 0) return;
        SkillDamageSource src = new SkillDamageSource(
                attacker.level().damageSources().magic().typeHolder(),
                attacker, attacker, null, null, false, null, null, true);
        target.hurt(src, amount);
    }
}
