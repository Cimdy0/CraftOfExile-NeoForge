package com.cimdy.coe.data.codec.skill.runtime;

import com.cimdy.coe.data.codec.skill.core.SkillCaster;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggerSkillAction;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/**
 * 技能触发助手 — 供 {@code TriggerSkillAction} / {@code MetaSkillAction} 复用：按 id 触发另一个技能。
 *
 * <p>设计要点：
 * <ul>
 *   <li>被触发技能继承触发者（caster）的支撑档案（SupportProfile），使其同样获得辅助宝石修正；
 *       这与 POE2「被触发的法术继承同一连线内的辅助宝石」一致。</li>
 *   <li>powerMultiplier 以「等效技能等级」近似（等级越高，随等级成长的数值越大）。</li>
 *   <li>触发技能使用 bind = {@code Integer.MAX_VALUE}，其冷却存于统一附件 {@code SKILL_COOLDOWN}，
 *       与玩家按键冷却共用同一存储与运算（见 {@link SkillCaster}），不会占用玩家按键冷却。</li>
 * </ul>
 */
public final class SkillTriggerHelper {
    /** 触发技能使用的虚拟绑定槽（无任何按键冷却副作用）。 */
    public static final int TRIGGER_BIND = Integer.MAX_VALUE;

    /** 位移替身技能（Travel，如 Blink 由翻滚键触发）专用冷却槽：与通用触发槽分开，
     *  避免与 Cast on X 类触发技能共用 {@link #TRIGGER_BIND} 而互相串冷却。 */
    public static final int TRAVEL_BIND = Integer.MAX_VALUE - 1;

    private SkillTriggerHelper() {}

    /**
     * 触发一个技能并将其加入活动技能列表。
     *
     * @param trigger   触发源技能实例（可为 null，用于取等级/品质/支撑档案）
     * @param context   触发时的动作上下文（提供 caster / eventBus）
     * @param skillId   被触发技能 id（含颜色段，如 coe:blue/chaos_bolt）
     * @param powerMultiplier 威力倍率（映射到等效技能等级）
     * @return 新创建的技能实例（已加入活动技能列表），技能不存在或无合法施法者时返回 null
     */
    public static SkillInstance trigger(SkillInstance trigger, ActionContext context,
                                         Identifier skillId, float powerMultiplier) {
        if (!(context.caster() instanceof LivingEntity caster)) return null;

        SkillContext triggerCtx = trigger != null ? trigger.getContext() : null;
        int level = triggerCtx != null
                ? (int) Math.max(1, Math.round(triggerCtx.getSkillLevel() * powerMultiplier)) : 1;
        double quality = triggerCtx != null ? triggerCtx.getQuality() : 0;
        SupportProfile profile = triggerCtx != null ? triggerCtx.getSupportProfile() : SupportProfile.EMPTY;

        // 复用统一施放 API（hold=true：被触发技能持续激活，由自身生命周期结束；
        // 非玩家施法者同样可用，冷却走 SkillCaster 分发）。
        return SkillCaster.cast(caster, skillId, TRIGGER_BIND, level, quality, profile, true);
    }
}
