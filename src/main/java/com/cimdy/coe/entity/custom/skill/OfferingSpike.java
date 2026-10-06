package com.cimdy.coe.entity.custom.skill;
import com.cimdy.coe.entity.custom.skill.totem.SkillTotem;
import com.cimdy.coe.data.codec.skill.phase.action.support.OfferingAction;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.data.helper.skill.SummonCountHelper;
import com.cimdy.coe.helper.OwnerHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.cimdy.coe.event.registry.RegisterEvent;

/**
 * 奉献尖刺（Offering Spike）— 由 {@code OfferingAction} 生成的伤害型随从实体。
 *
 * <p>PoE2「奉献」机制核心载体：施放奉献时牺牲一只骷髅召唤物，在施法者身边生成此尖刺。
 * 尖刺<b>存活期间</b>施法者持有对应奉献增益（见 {@link OfferingStatusTypes}）；
 * 尖刺<b>死亡或被移除</b>时立即剥夺该增益（{@link #dropBuff}）。
 *
 * <p>机制落地（对齐用户修正）：
 * <ul>
 *   <li>生命走原版 {@link Attributes#MAX_HEALTH}（configure 时由 {@code health} 覆盖；
 *       受「Offerings have X% increased Max Life」经由生成侧读取施法者属性缩放）。</li>
 *   <li>「近期创造的祭品无法被伤害」→ {@link #recentlyCreatedTicks} 窗口内 {@link #hurtServer} 直接忽略全部伤害。</li>
 *   <li>Limit 1 由生成侧（{@code OfferingAction}）保证：施放前先剥夺并移除已有的尖刺。</li>
 * </ul>
 *
 * <p>渲染：与 {@link SkillTotem} 同范式——渲染体为 Noop（{@code RegisterEvent} 注册），
 * 可见性由本实体自发射的粒子提供（技能 VFX 另计）。
 */
public class OfferingSpike extends LivingEntity implements OwnableEntity {
    @Nullable
    private EntityReference<LivingEntity> owner;
    private OfferingType type = OfferingType.BONE;
    /** 存活时长（tick），到期自毁。 */
    private int lifetime = 200;
    /** 创建时的游戏刻（用于「近期创造」无敌窗口判定）。 */
    private long createdAtTick = 0;
    /** 「近期创造」无敌窗口（tick）；此窗口内尖刺无法被伤害。默认 1.5 秒。 */
    private int recentlyCreatedTicks = 30;

    public OfferingSpike(EntityType<? extends OfferingSpike> type, Level level) {
        super(type, level);
        // 静态驻场：不参与重力/移动
        this.setNoGravity(true);
    }

    /** 实体属性：基础生命由生成侧 configure 时的 health 参数覆盖。 */
    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 1.0);
    }

    /** 由 OfferingAction 在生成时调用完成装配。 */
    public void configure(LivingEntity owner, OfferingType type, int lifetime,
                          float health, int recentlyCreatedTicks) {
        this.owner = EntityReference.of(owner);
        this.type = type;
        this.lifetime = Math.max(1, lifetime);
        this.recentlyCreatedTicks = Math.max(0, recentlyCreatedTicks);
        this.createdAtTick = owner.level().getGameTime();
        // 生命走原版属性：MAX_HEALTH 基值 = health，当前生命充满
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(1.0, health));
        }
        this.setHealth(Math.max(1.0f, health));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    /** 静态驻场：不执行任何 AI 移动逻辑。 */
    @Override
    public void aiStep() {
    }

    /** 静态驻场：完全忽略移动输入。 */
    @Override
    public void travel(Vec3 movementInput) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // 到期 → 剥夺增益并自毁
        if (this.tickCount >= this.lifetime) {
            dropBuff();
            SummonCountHelper.onMinionRemoved(this);
            this.discard();
            return;
        }

        // 可视化：尖刺粒子（渲染体为 Noop，粒子提供存在感）
        if (this.tickCount % 5 == 0) {
            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    this.getX(), this.getY() + 0.3 + (this.tickCount % 20) * 0.05, this.getZ(),
                    2, 0.1, 0.05, 0.1, 0.0);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // 近期创造无敌窗口：仅当主人持有 OFFERING_RECENT_IMMUNE 标记（经 EFF 平加，归一 #55 转 INERT MarkerStatEffect，无属性）时生效
        // （POE2「近期创造的祭品无法被伤害」由该被动节点授权，而非硬编码常驻；StatEngine.markerMagnitude 真读 STAT_STORE 标记，非死线）
        LivingEntity owner = this.getOwner();
        if (owner != null
                && StatEngine.markerMagnitude(owner, MarkerStatEffect.Kind.OFFERING_RECENT_IMMUNE, Set.of()) >= 1.0
                && level.getGameTime() - this.createdAtTick < this.recentlyCreatedTicks) {
            return false;
        }
        // 主人不会损伤尖刺
        if (source.getEntity() == this.getOwner()) return false;
        return super.hurtServer(level, source, amount);
    }

    /** 尖刺死亡不播原版死亡动画/不掉落/不产生经验：直接剥夺增益并销毁。 */
    @Override
    public void die(DamageSource damageSource) {
        dropBuff();
        SummonCountHelper.onMinionRemoved(this);
        this.discard();
    }

    /** 尖刺移除时剥夺对应奉献增益（从主人身上清除该奉献状态 + 可能的双奉献加伤）。 */
    public void dropBuff() {
        LivingEntity owner = this.getOwner();
        if (owner == null) return;
        StatusEngine.clear(owner, StatusDefs.forType(this.type));
        // 不同奉献计数回落到 <2，清除双奉献加伤（即便它自身恰好是第 2 个，移除后亦不再满足）
        StatusEngine.clear(owner, StatusDefs.OFFERING_MINION_DAMAGE_BONUS);
    }

    @Override
    public boolean fireImmune() {
        return false;
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

    @Nullable
    @Override
    public EntityReference<LivingEntity> getOwnerReference() {
        return this.owner;
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        return EntityReference.getLivingEntity(this.owner, this.level());
    }

    /** 统计主人当前存活的、不同类型奉献尖刺数量（用于「≥2 种不同奉献」加伤判定）。 */
    public static int countDistinctTypes(LivingEntity owner) {
        if (owner == null) return 0;
        Set<OfferingType> types = new HashSet<>();
        for (Entity e : owner.level().getEntities(owner, owner.getBoundingBox().inflate(64.0))) {
            if (e instanceof OfferingSpike spike && owner.equals(spike.getOwner())) {
                types.add(spike.type);
            }
        }
        return types.size();
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.tickCount = valueInput.getIntOr("Age", 0);
        this.lifetime = valueInput.getIntOr("Lifetime", 200);
        this.recentlyCreatedTicks = valueInput.getIntOr("RecentlyCreated", 30);
        this.type = OfferingType.byName(valueInput.getStringOr("OfferingType", "bone")).orElse(OfferingType.BONE);
        this.owner = EntityReference.read(valueInput, "Owner");
        // 重载后「近期创造」窗口已无意义：直接按已创建（早退）处理，避免无限无敌
        this.createdAtTick = this.level().getGameTime() - this.recentlyCreatedTicks - 1;
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("Age", this.tickCount);
        valueOutput.putInt("Lifetime", this.lifetime);
        valueOutput.putInt("RecentlyCreated", this.recentlyCreatedTicks);
        valueOutput.putString("OfferingType", this.type.getSerializedName());
        EntityReference.store(this.owner, valueOutput, "Owner");
    }
}
