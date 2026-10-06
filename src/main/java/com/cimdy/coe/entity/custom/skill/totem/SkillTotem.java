package com.cimdy.coe.entity.custom.skill.totem;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.event.combat.DamageEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.cimdy.coe.data.helper.skill.SummonCountHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * 技能图腾 — 由 {@code SpawnAction} 生成的驻场施法体。
 *
 * <p>核心机制：图腾自身不携带伤害数据，而是持有 (casterId, phaseId)，
 * 周期性寻找射程内敌人后通过 {@link SkillPhaseInstanceTracker} 反查
 * {@link SkillPhaseInstance} 并点燃 {@code ON_TICK}（带命中上下文）。
 * 由此图腾的攻击完整复用技能阶段的 {@code DamageAction} 结算，
 * 天然<b>继承施法者的 SupportProfile</b>（辅助宝石 more/less/added/convert/穿透等全部生效）。
 *
 * <p><b>Living 化</b>：图腾基类是 {@link LivingEntity} 而非 {@link Entity}——
 * 生命走原版 {@link Attributes#MAX_HEALTH} 属性（configure 时由 SpawnAction 的 health 参数设定），
 * 受击经 {@link #hurtServer} → NeoForge 伤害事件 → {@code DamageEvent} 全链路（原版免疫/盔甲/死亡结算），
 * 从而接入既有 Living 通道：图腾承伤可触发 StatEngine.fire 主人链（target=TOTEMS 的条目，
 * 如 5580「Recoup X% of Damage taken by your Totems as Life」）、图腾生命可被
 * {@code target=TOTEMS} 的属性修饰（entityMod 通道）与 Recoup/Leech 等逐 tick 资源回复影响。
 *
 * <p>生命周期：到期或生命归零即销毁；到期时点燃 {@code ON_EXPIRE}。
 * 销毁时释放 tracker 引用计数（生成时由 SpawnAction retain）。
 * 世界重载后 phase 实例不存在，图腾静默驻场至到期自毁。
 */
public class SkillTotem extends LivingEntity implements OwnableEntity {
    @Nullable
    private EntityReference<LivingEntity> owner;
    @Nullable
    private UUID casterId;
    @Nullable
    private UUID phaseId;
    /** 存活时长（tick），到期自毁。 */
    private int lifetime = 200;
    /** 攻击间隔（tick）。 */
    private int attackInterval = 20;
    /** 索敌半径（格）。 */
    private float attackRange = 8.0f;

    public SkillTotem(EntityType<? extends SkillTotem> type, Level level) {
        super(type, level);
        // 图腾固定驻场：不参与重力/移动（对照 vanilla 静态实体范式，见 ArmorStand 的 travel 处理）
        this.setNoGravity(true);
    }

    /** 实体属性：基础生命由 SpawnAction 的 health 参数在 configure 时覆盖。 */
    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 20.0);
    }

    /** 由 SpawnAction 在生成时调用完成装配。 */
    public void configure(LivingEntity owner, @Nullable UUID phaseId,
                          int lifetime, int attackInterval, float attackRange, float health) {
        this.owner = EntityReference.of(owner);
        this.casterId = owner.getUUID();
        this.phaseId = phaseId;
        this.lifetime = lifetime;
        this.attackInterval = Math.max(1, attackInterval);
        this.attackRange = attackRange;
        // 生命走原版属性：MAX_HEALTH 基值 = SpawnAction 配置的 health，当前生命充满
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(1.0, health));
        }
        this.setHealth(Math.max(1.0f, health));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    /** 静态驻场：不执行任何 AI 移动逻辑（对照 vanilla {@code NoAiMob} 等静态 Living 实体）。 */
    @Override
    public void aiStep() {
    }

    /** 静态驻场：完全忽略移动输入（重力/击退/流体），保持图腾位置恒定。 */
    @Override
    public void travel(Vec3 movementInput) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // 到期 → 点燃 ON_EXPIRE 并自毁
        if (this.tickCount >= this.lifetime) {
            fireTrigger(LifecycleTrigger.ON_EXPIRE, null);
            // 图腾计数 −1：到期真销毁（helper 内做服务端侧与 owner 可解析性防护）
            SummonCountHelper.onMinionRemoved(this);
            this.discard();
            return;
        }

        // 可视化：驻场粒子柱（渲染体为 Noop，粒子提供存在感）
        if (this.tickCount % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + 0.2 + (this.tickCount % 20) * 0.08, this.getZ(),
                    2, 0.12, 0.05, 0.12, 0.0);
        }

        // 周期性索敌攻击 → 点燃技能阶段的 ON_TICK（带命中上下文）
        if (this.tickCount % this.attackInterval == 0) {
            LivingEntity target = findTarget();
            if (target != null) {
                Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
                Vec3 dir = targetPos.subtract(this.position());
                float distance = (float) dir.length();
                HitContext hit = HitContext.entityHit(target, targetPos,
                        distance > 1e-4 ? dir.normalize() : Vec3.ZERO, distance);
                fireTrigger(LifecycleTrigger.ON_TICK, hit);
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        targetPos.x, targetPos.y, targetPos.z, 6, 0.2, 0.2, 0.2, 0.05);
            }
        }
    }

    /** 点燃本图腾技能阶段的触发（子类如 {@link TrapEntity} 复用，故 protected）。 */
    protected void fireTrigger(LifecycleTrigger trigger, @Nullable HitContext hit) {
        if (this.casterId == null || this.phaseId == null) return;
        SkillPhaseInstance phase = SkillPhaseInstanceTracker.find(this.casterId, this.phaseId);
        if (phase != null) {
            phase.fireTrigger(trigger, hit);
        }
    }

    /** 就近索敌：仅锁定敌对生物（Enemy），排除主人及玩家阵营（召唤物/图腾）。
     *  获取实体统一经 Context 数据基站回调（EntityQuery 球形扫描 + 就近排序）。
     *  子类如 {@link TrapEntity} 复用（其邻近一次性触发也走同一索敌），故 protected。 */
    @Nullable
    protected LivingEntity findTarget() {
        LivingEntity ownerEntity = this.getOwner();
        return ExpressionContext.of(this.level(), this).queryEntity(
                        EntityQuery.sphere(this.position(), this.attackRange)
                                .exclude(ownerEntity).aliveOnly()
                                .filter(e -> e instanceof Enemy
                                        && !Camp.PLAYER.equals(e.getData(AttachmentRegistry.CAMP)))
                                .nearestTo(this.position()))
                .orElse(null);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // 主人及其召唤物不会损伤图腾（保留原守卫；其余伤害走原版 Living 结算，
        // 使图腾受击接入 DamageEvent → StatEngine.fire 主人链，见类注释）
        if (source.getEntity() == this.getOwner()) return false;
        return super.hurtServer(level, source, amount);
    }

    /** 图腾死亡不播原版死亡动画/不掉落/不产生经验：直接销毁（并释放 tracker）。 */
    @Override
    public void die(DamageSource damageSource) {
        // 图腾计数 −1：被打死即真销毁（无死亡动画，实体直接移除）
        SummonCountHelper.onMinionRemoved(this);
        this.discard();
    }

    /** 环境伤害免疫：图腾不受火焰（燃烧）（与静态驻场语义一致）。 */
    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    public net.minecraft.world.entity.HumanoidArm getMainArm() {
        return net.minecraft.world.entity.HumanoidArm.RIGHT;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public void onRemoval(Entity.RemovalReason reason) {
        if (this.casterId != null && this.phaseId != null) {
            SkillPhaseInstanceTracker.release(this.casterId, this.phaseId);
        }
        super.onRemoval(reason);
    }

    /** OwnableEntity 契约：主人引用（configure / 持久化后恒非 null；装配前为 null，
     *  仅 default {@code getOwner()} 解析路径调用，调用方均已判空）。 */
    @Override
    @Nullable
    public EntityReference<LivingEntity> getOwnerReference() {
        if (this.owner == null && this.casterId != null) {
            this.owner = EntityReference.of(this.casterId);
        }
        return this.owner;
    }

    @Override
    @Nullable
    public LivingEntity getOwner() {
        return EntityReference.getLivingEntity(this.owner, this.level());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.tickCount = valueInput.getIntOr("Age", 0);
        this.lifetime = valueInput.getIntOr("Lifetime", 200);
        this.attackInterval = valueInput.getIntOr("AttackInterval", 20);
        this.attackRange = valueInput.getFloatOr("AttackRange", 8.0f);
        this.owner = EntityReference.read(valueInput, "Owner");
        // casterId/phaseId 不持久化：重载后 phase 实例已不存在，图腾静默驻场至到期
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("Age", this.tickCount);
        valueOutput.putInt("Lifetime", this.lifetime);
        valueOutput.putInt("AttackInterval", this.attackInterval);
        valueOutput.putFloat("AttackRange", this.attackRange);
        EntityReference.store(this.owner, valueOutput, "Owner");
    }
}
