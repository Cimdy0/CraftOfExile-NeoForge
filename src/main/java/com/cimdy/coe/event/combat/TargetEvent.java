package com.cimdy.coe.event.combat;

import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.entity.goal.MobSkillCastGoal;
import com.cimdy.coe.entity.goal.TauntTargetGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

@EventBusSubscriber(modid = CraftOfExile.MODID)
public class TargetEvent {
    /**
     * 实体加入世界时：① 分配阵营（玩家=PLAYER；野生 Enemy=MOB；被拥有实体=主人阵营；
     * 召唤物/图腾由 SpawnAction 在加入前已写入 PLAYER，此处跳过），② 仅增量注入技能施法 Goal。
     * <p>「以阵营为原则的相互敌对」不再靠移除原版 TargetGoal 实现，而是由
     * {@code CampAllianceMixin}（基类 {@code LivingEntity#canAttack} 覆写，按 Camp 过滤友敌）统一驱动，
     * 从而保留实体原有攻击 goal / Brain behavior，且对两套 AI 系统、所有非模组实体通用。
     */
    @SubscribeEvent
    public static void addTargetGoalEvent(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (!(event.getLevel() instanceof ServerLevel)) return;

        // 玩家作为 PLAYER 阵营基准：与玩家召唤物（同 PLAYER）互为友方，与野生敌对生物（MOB）相互敌对。
        if (living instanceof Player) {
            living.setData(AttachmentRegistry.CAMP, Camp.PLAYER);
            return;
        }
        if (!(living instanceof Mob mob)) return;

        // 野生/被拥有实体的阵营分配（SpawnAction 生成的召唤物/图腾在加入世界前已写入 Camp.PLAYER，此处跳过）。
        if (mob.getData(AttachmentRegistry.CAMP) == Camp.NONE) {
            if (mob instanceof OwnableEntity own && own.getOwner() instanceof LivingEntity owner) {
                // 被拥有实体（驯服狼等）继承主人阵营；主人概念上为 PLAYER，或回落为敌对 MOB。
                Camp ownerCamp = owner instanceof Player ? Camp.PLAYER : owner.getData(AttachmentRegistry.CAMP);
                mob.setData(AttachmentRegistry.CAMP, ownerCamp != Camp.NONE ? ownerCamp : Camp.MOB);
            } else if (mob instanceof Enemy) {
                mob.setData(AttachmentRegistry.CAMP, Camp.MOB);
            }
            // 其余（NONE 且非敌人、非拥有）→ 保持中立，不参与阵营敌对。
        }

        // 阵营战斗的「主动索敌」不再移除原版 TargetGoal：原版索敌 goal 经 CampAllianceMixin
        // （canAttack 基类覆写）已自动按 Camp 过滤友敌，保留实体原有攻击行为。
        // 技能化战斗：装配了技能（词缀赋予 / 召唤物继承）的实体改用 CoE 技能系统输出，
        // 未装配技能时该 Goal 恒不激活，实体行为完全退回原版 goalSelector。
        mob.goalSelector.addGoal(1, new MobSkillCastGoal(mob));

        // 嘲讽（Taunt）：持有 taunt 状态的怪物强制锁定嘲讽者为攻击目标（见 TauntTargetGoal，最高优先级槽位 0）。
        // taunt 的施加由技能 / 词缀数据驱动，本处仅注入消费机制；taunt 消失后怪物退回原版索敌。
        mob.targetSelector.addGoal(0, new TauntTargetGoal(mob));
    }

    @SubscribeEvent
    public static void onTargetChangeEvent(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        LivingEntity living = event.getEntity();
        boolean change = true;
        if (newTarget != null && event.getEntity().level() instanceof ServerLevel serverLevel) {
            Camp camp = living.getData(AttachmentRegistry.CAMP);
            Camp newTargetCamp = newTarget.getData(AttachmentRegistry.CAMP);
            if (newTargetCamp.equals(Camp.PLAYER)) {
                // 中立攻击玩家
                if (camp.equals(Camp.NONE)) {
                    living.setData(AttachmentRegistry.CAMP, Camp.MOB);
                }
                if (camp.equals(Camp.PLAYER)) {
                    change = false;
                }
            }
            //召唤生物仅和敌对生物相互敌对
            if (!camp.equals(Camp.NONE) && newTargetCamp.equals(camp)) {
                change = false;
            }
            //召唤生物不主动攻击非敌对生物
            if (camp.equals(Camp.PLAYER) && newTargetCamp.equals(Camp.NONE)) {
                change = false;
            }
        }

        if (!change) {
            // P2-2 激活：玩家阵营召唤物不与玩家/同阵营互相为敌（SpawnAction 写入 Camp.PLAYER）
            event.setCanceled(true);
        }

    }
}
