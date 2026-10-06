package com.cimdy.coe.stronghold.energy;

import com.cimdy.coe.stronghold.area.StrongholdRepository;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.entity.SaveEntity;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.entity.goal.FactionDefendGoal;
import com.cimdy.coe.entity.goal.FollowOwnerGoal;
import com.cimdy.coe.entity.goal.stronghold.behavior.BehaviorRegistry;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.behavior.IGoalContext;
import com.cimdy.coe.helper.OwnerHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.UUID;

/**
 * 工人实体生成与 AI 注入服务：负责将收编实体序列化为数据、从数据重建实体、注入据点行为目标。
 * 从原 God Class {@link com.cimdy.coe.stronghold.energy.StrongholdHelper} 拆分而来，逻辑完全一致。
 * 注：AI 优先级表已通过 {@link com.cimdy.coe.entity.goal.stronghold.behavior.BehaviorRegistry}（方案 D）注册表化，
 * 本类仅做职责归并并委托注册表注入行为。
 */
public final class WorkerAIInjector {
    /**
     * 盟友跟随目标的注入优先级：原版 {@code MeleeAttackGoal} 落在 2–4、{@code RandomStrollGoal} 落在 7，
     * 取 5 使「有敌人时战斗接管、空闲时跟随压过游走」，且不动 {@code MobSkillCastGoal}（1）的施法优先。
     */
    public static final int FOLLOW_PRIORITY = 5;

    private WorkerAIInjector() {
    }

    public static CustomData entityToCustomData(Entity entity) {
        TagValueOutput output = TagValueOutput.createWithContext(
                new ProblemReporter.ScopedCollector(entity.problemPath(), LogUtils.getLogger()),
                entity.registryAccess());
        entity.save(output);
        return CustomData.of(output.buildResult());
    }

    public static Entity createEntityFromData(Level level, CustomData data) {
        // ignoreChecks=true：预览/据点/收编实体的重建都不应受难度(和平模式)或特性开关的 canSpawn 检查限制，
        // 否则客户端预览时 slime 等实体会因 canSpawn 返回 false 而创建失败（日志 "Skipping Entity with id ..."），导致不渲染。
        return EntityType.loadEntityRecursive(data.copyTag(), level, new EntitySpawnRequest(EntitySpawnReason.LOAD, true), e -> e);
    }

    public static void spawnPlayerEntity(CustomData data, ServerLevel serverLevel, Player player, BlockPos pos) {
        Entity entity = createEntityFromData(serverLevel, data);
        if (entity != null) {
            // 盟友标记从存档记录搬到实体附件：放出后实体不再在存档列表里，
            // 派工侧（TaskDispatcher#canWork）只看得到实体附件，回收时再由 SaveEntity.of 反向写回。
            // 原 AttachmentRegistry.ALLY / COMBATANT 已迁入 AdditionData（key=ally / combatant，combatant 默认 true）
            AdditionData.addOrReplaceData(entity, AdditionKeys.ALLY,
                    StrongholdRepository.isStoredAlly(player, SaveEntity.uuidOf(data).orElse(null)) ? 1.0 : 0.0);
            // 帕鲁式「工作 ↔ 战斗」开关：从存档记录恢复，缺字段默认 true（遭入侵须防御）。
            AdditionData.addOrReplaceData(entity, AdditionKeys.COMBATANT,
                    StrongholdRepository.isStoredCombatant(player, SaveEntity.uuidOf(data).orElse(null)) ? 1.0 : 0.0);
            entity.setData(AttachmentRegistry.ENTITY_DATA,
                    entity.getData(AttachmentRegistry.ENTITY_DATA).set(player.getUUID(),
                            new GlobalPos(serverLevel.dimension(), pos)));
            if (entity instanceof Mob mob) {
                mob.setPersistenceRequired();
                // 原 AttachmentRegistry.ALLY 已迁入 AdditionData（key=ally）
                if (AdditionData.getOrDefault(entity, AdditionKeys.ALLY, 0.0) != 0.0) {
                    // 盟友：认主后补一个跟随目标，保留原版 goalSelector（战斗 / 游走）与 TargetEvent
                    // 注入的阵营索敌——它不参与据点工作，据点 AI 的拴绳语义（StrongholdGoal / WanderingGoal）不适用。
                    // 帕鲁式 per-entity 开关 COMBATANT（默认 true）为真时，再叠加据点防御：据点遭入侵仍须反击。
                    OwnerHelper.setOwner(mob, player);
                    mob.goalSelector.addGoal(FOLLOW_PRIORITY, new FollowOwnerGoal(mob));
                    // 原 AttachmentRegistry.COMBATANT 已迁入 AdditionData（key=combatant，默认 true）
                    if (AdditionData.getOrDefault(mob, AdditionKeys.COMBATANT, 1.0) != 0.0) {
                        mob.goalSelector.addGoal(StrongholdPriority.DEFEND, new FactionDefendGoal(mob));
                    }
                } else {
                    injectStrongholdAI(mob);
                }
            }
            entity.setData(AttachmentRegistry.CAMP, Camp.PLAYER);
            entity.setPos(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
            serverLevel.addFreshEntity(entity);
            StrongholdRepository.addEntityToHold(serverLevel, pos, entity);
            StrongholdRepository.releaseEntityForPlayer(serverLevel, player, entity.getUUID());
        }
    }

    /**
     * 已部署实体在「加入世界」时刻的 AI 落地入口：盟友只保证有一个跟随目标，其余照旧注入据点 AI。
     *
     * <p>必须走这一个入口：{@code EntityJoinLevelEvent} 在<b>每次</b>加入世界时都会触发（生成、区块加载、
     * 跨维度），而 {@link #injectStrongholdAI(Mob)} 会清空 goalSelector —— 若此处不区分盟友，
     * {@link #spawnPlayerEntity} 预置的 {@link FollowOwnerGoal} 会在实体真正进入世界的那一刻被抹掉。
     * 对盟友分支只移除同类型旧目标再补一个，因此重复调用是幂等的（跟随目标恒为 1 个）。
     */
    public static void injectDeployAI(Mob mob) {
        // 原 AttachmentRegistry.ALLY 已迁入 AdditionData（key=ally）
        if (AdditionData.getOrDefault(mob, AdditionKeys.ALLY, 0.0) != 0.0) {
            // 每次加入世界都会触发本入口：先清掉上一轮注入的「跟随 + 防御」目标，再按当前 COMBATANT 重新落地，
            // 保证幂等且不累积重复目标（原版目标保留不动）。
            mob.goalSelector.removeAllGoals(g -> g instanceof FollowOwnerGoal || g instanceof FactionDefendGoal);
            mob.goalSelector.addGoal(FOLLOW_PRIORITY, new FollowOwnerGoal(mob));
            // 原 AttachmentRegistry.COMBATANT 已迁入 AdditionData（key=combatant，默认 true）
            if (AdditionData.getOrDefault(mob, AdditionKeys.COMBATANT, 1.0) != 0.0) {
                mob.goalSelector.addGoal(StrongholdPriority.DEFEND, new FactionDefendGoal(mob));
            }
            return;
        }
        injectStrongholdAI(mob);
    }

    /**
     * 已部署实体就地转为盟友：卸掉据点 AI，改用跟随。
     *
     * <p>已知限制（无法规避，不要另想办法）：该实体当初部署时被 {@link #injectStrongholdAI(Mob)} 的
     * {@code removeAllGoals(g -> true)} 抹掉了全部原版目标，{@code MeleeAttackGoal} 早已不在，
     * 现场转换不可能凭空恢复它 —— 就地转换得到的盟友<b>只跟随、不主动攻击</b>
     * （阵营敌对现由 {@code CampAllianceMixin} 在基类 {@code canAttack} 门控，不依赖任何 targetSelector 目标；
     * 装备了技能的实体仍由 {@code MobSkillCastGoal} 施法）。要获得「跟随 + 战斗」的完整随行伙伴，
     * 应在仓库区先标记再放出
     * （走 {@link #spawnPlayerEntity} 路径，原版 goalSelector 得以保留）。
     *
     * <p>幂等：先清空再补，反复调用不会累积目标。
     */
    public static void toCompanion(Mob mob, Player player) {
        mob.goalSelector.removeAllGoals(g -> true);
        OwnerHelper.setOwner(mob, player);
        mob.goalSelector.addGoal(FOLLOW_PRIORITY, new FollowOwnerGoal(mob));
        // 帕鲁式 per-entity 开关：转为随行伙伴后默认仍参与防御（COMBATANT 默认 true）；
        // 玩家在终端将其设为 false 的实体只跟随、不主动出击。
        // 原 AttachmentRegistry.COMBATANT 已迁入 AdditionData（key=combatant，默认 true）
        if (AdditionData.getOrDefault(mob, AdditionKeys.COMBATANT, 1.0) != 0.0) {
            mob.goalSelector.addGoal(StrongholdPriority.DEFEND, new FactionDefendGoal(mob));
        }
    }

    /**
     * 已部署实体就地转回工人：卸掉跟随目标，重新注入据点 AI。幂等：先清空再注入。
     *
     * <p>必须一并清掉 {@link AttachmentRegistry#OWNER_UUID}：该附件一旦非空，实体就会进入
     * {@code StatEngine} 的「master→召唤体」作用域（吃到玩家「随从 +X%」类词条）并成为
     * {@code TargetSelector} 的 MINION 源。若转回工人时不清，「工人 → 盟友 → 转回工人」之后
     * 该工人会永久保留这份待遇，而<b>从未被标记过的工人没有</b> → 来回切一次即产生行为不一致。
     * 还原成 {@code UUID.randomUUID()}（与附件默认值同型，查不到任何实体）即等价于「无主」。
     */
    public static void toWorker(Mob mob) {
        mob.goalSelector.removeAllGoals(g -> true);
        mob.setData(AttachmentRegistry.OWNER_UUID, UUID.randomUUID());
        injectStrongholdAI(mob);
    }

    public static void injectStrongholdAI(Mob mob) {
        mob.targetSelector.removeAllGoals(g -> true);
        mob.goalSelector.removeAllGoals(g -> true);
        mob.getBrain().removeAllBehaviors();

        // 方案 D：通过行为注册表按优先级注入工人 AI，行为可扩展、核心零改动。
        BehaviorRegistry.applyTo(mob, IGoalContext.EMPTY);

        // 帕鲁式 per-entity 开关：COMBATANT 默认 true —— 据点遭入侵时工人仍须防御（增量叠加，不替换据点 AI），
        // 呼应「据点实体当据点内存在敌对实体时仍然需要战斗，不是彻底分离」。设为 false 的实体专注工作、不主动出击。
        // 原 AttachmentRegistry.COMBATANT 已迁入 AdditionData（key=combatant，默认 true）
        if (AdditionData.getOrDefault(mob, AdditionKeys.COMBATANT, 1.0) != 0.0) {
            mob.goalSelector.addGoal(StrongholdPriority.DEFEND, new FactionDefendGoal(mob));
        }
    }
}
