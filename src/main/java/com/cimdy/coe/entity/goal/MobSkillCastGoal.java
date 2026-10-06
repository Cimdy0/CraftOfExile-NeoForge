package com.cimdy.coe.entity.goal;

import com.cimdy.coe.data.codec.skill.helper.cast.SkillCast;
import com.cimdy.coe.data.helper.hub.SpiritManager;
import com.cimdy.coe.data.helper.skill.EquipmentSkillHelper;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillCaster;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.gem.SkillAssembly;
import com.cimdy.coe.event.combat.TargetEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.geckolib.animatable.GeoEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * 让持有技能的非玩家实体（野生怪物 / 召唤生物 / 图腾）真正使用技能，而不是走原版攻击伤害。
 *
 * <p>技能来源统一为实体的 {@link AttachmentRegistry#SKILL_ASSEMBLY}
 * （由词缀 {@code LivingMod.ModSkill} 固化写入，见 {@code LivingMod#grantSkills}）；
 * 装配表为空的实体本 Goal 恒不激活，行为完全退回原版，不影响任何既有怪物。
 *
 * <p>施放走统一入口 {@link SkillCaster#castGranted}，因此：
 * <ul>
 *   <li>伤害 / 暴击 / 施法速度 / 消耗等全部按<b>施法实体自身</b>的属性与 Stat 词条结算，
 *       与玩家同源——词缀的属性加成、召唤物继承的属性都会真实作用到技能上；</li>
 *   <li>冷却统一存于 {@link AttachmentRegistry#SKILL_COOLDOWN}（bind = 装配槽位），
 *       由 {@link SkillCaster#tickAll} 每 tick 推进，不另建冷却系统；</li>
 *   <li>技能自带的冷却（{@code SkillCast#getCooldown}）照常生效，本 Goal 的间隔仅作
 *       「技能自身无冷却」时的兜底节奏。</li>
 * </ul>
 *
 * <p>注入点：{@code TargetEvent.addTargetGoalEvent}（实体加入世界时，与阵营系统同处）。
 * 施法动作经 GeckoLib 钩子 {@code mob instanceof GeoEntity} 触发，未接 GeckoLib 的实体静默跳过。
 */
public class MobSkillCastGoal extends Goal {
    /** 主动施法距离（格）：超出则只追击不施放。 */
    private static final double CAST_RANGE = 16.0;
    /** 近身距离（格）：进入后停止移动，原地施放。 */
    private static final double KEEP_RANGE = 4.0;
    /** 技能自身无冷却时的兜底施法间隔（tick）。 */
    public static final int FALLBACK_INTERVAL = 40;
    /** 施放条件不满足（魔力不足等）时的重试间隔（tick）。 */
    private static final int RETRY_INTERVAL = 10;

    private final Mob mob;
    /** 轮询游标：多技能时轮转施放，避免只放第 0 槽。 */
    private int cursor = 0;

    public MobSkillCastGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive()
                && !EquipmentSkillHelper.mergedAssembly(mob).isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return;
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distSq = mob.distanceToSqr(target);
        if (distSq > CAST_RANGE * CAST_RANGE) return;
        if (distSq > KEEP_RANGE * KEEP_RANGE) {
            mob.getNavigation().moveTo(target, 1.0);
        } else {
            mob.getNavigation().stop();
        }

        SkillAssembly assembly = EquipmentSkillHelper.mergedAssembly(mob);
        SkillCaster caster = SkillCaster.of(mob);
        for (int i = 0; i < SkillAssembly.SIZE; i++) {
            int slot = (cursor + i) % SkillAssembly.SIZE;
            SkillData gem = assembly.get(slot);
            // 精魂宝石为常驻光环，由 SpiritManager 按开关激活，不参与 AI 主动施放
            if (gem.isEmpty() || gem.isSpirit()) continue;
            if (!caster.canCast(slot)) continue;
            Skill skill = gem.getSkill();
            if (skill == null) {
                caster.setCooldown(slot, RETRY_INTERVAL);
                continue;
            }
            // 技能自身冷却（cast.setCooldown）在施放成功时写入；此处兜底节奏仅在
            // 施放失败 / 技能无冷却时生效，且必须在 castGranted 之后写入，
            // 否则会被技能自身的 setCooldown(0) 清掉。
            boolean casted = caster.castGranted(skill, slot, gem.getEffectiveSkillLevel(),
                    gem.getEffectiveQuality(), gem.buildSupportProfile(), gem.id()) != null;
            // GeckoLib 动作接线：实体实现 GeoEntity 时播放施法动作；非 GeckoLib 实体安全 no-op。
            // 用户要求「所有实体都要 + 模型可空 + 只接线不写资源」——泛型 instanceof 钩子覆盖全部技能施法实体，
            // 未接 GeckoLib 的实体（含全部原版生物）静默跳过，不引入任何资源依赖。
            if (casted && mob instanceof GeoEntity ge) {
                ge.triggerAnim("skill_cast", "cast");
            }
            if (!caster.canCast(slot)) break;
            caster.setCooldown(slot, casted ? FALLBACK_INTERVAL : RETRY_INTERVAL);
            cursor = (slot + 1) % SkillAssembly.SIZE;
            break;
        }
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }
}
