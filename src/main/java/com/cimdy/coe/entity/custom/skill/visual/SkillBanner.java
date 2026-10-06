package com.cimdy.coe.entity.custom.skill.visual;

import com.cimdy.coe.data.helper.hub.ResourceRegistry;

import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.data.codec.support.stat.condition.RadiusCondition;
import com.cimdy.coe.data.codec.support.stat.condition.StatCondition;
import com.cimdy.coe.data.codec.support.stat.duration.StatDuration;
import com.cimdy.coe.data.codec.support.stat.effect.status.StatusStatEffect;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.helper.engine.periodic.PeriodicDriver;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 战旗实体（POE2 Banner 子系统）— 由 {@code BannerAction} 在施法位置投放的<b>驻场光环体</b>。
 *
 * <p><b>光环机制迁移</b>：不再手写 XZ 圆柱扫描与 {@code StatusEngine.apply} 直施加，而是把「半径友方光环」
 * 表达为两条 {@link StatEntry}（aura 主增益 + 可选 elemental_res），统一复用 {@link StatusStatEffect} +
 * 新增的「半径友方」Target 源能力（{@code TargetSelector.Source#RADIUS_ALLIES} 枚举值已删除） + {@link StatEntry#interval()}/{@link StatEntry#duration()}
 * 时间层，由 {@link PeriodicDriver} 每 5 tick 驱动一次（圆心 = 本实体位置）。范围（半径）是 Target 职责，
 * 严禁做成 Effect 子类（铁律）。
 *
 * <p><b>双计量消除</b>：幅度在 {@code BannerAction} 已按 {@code {aura,banner}} 作用域烘焙进 entry amount，
 * 经 {@link StatusStatEffect} 落地时引擎侧 {@code effectMul} 的 aura 项不再二次乘（auraSourced=false），
 * 从而消除旧路径「烘焙幅度 × 引擎再乘 aura」的平方放大。
 *
 * <p><b>包围盒随半径缩放</b>：令 AABB 覆盖光环范围，保证渲染 / 半径源粗筛几何合理；半径精筛由 RADIUS_ALLIES 完成。
 *
 * <p><b>生命周期</b>：存活时长到期即自毁（不掉落、无死亡动画）。到期前按「放置时消耗的全部荣耀 × 回收比例」
 * 回收荣耀（#41 / GAP-FIX#17）。
 */
public class SkillBanner extends Entity {
    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(SkillBanner.class, EntityDataSerializers.FLOAT);

    /** 光环施效间隔（tick）。 */
    public static final int AURA_INTERVAL = 5;
    /** 粒子刷新间隔（tick）。 */
    private static final int PARTICLE_INTERVAL = 8;
    /** 光环边界圈的粒子采样点数。 */
    private static final int RING_SAMPLES = 16;
    /** 包围盒高度：覆盖站立实体的高度，确保 AABB 粗筛不漏。 */
    private static final float BOX_HEIGHT = 2.0F;

    /** 要施加的 aura 增益条目（null = 未装配的裸实体）。 */
    @Nullable
    private StatEntry auraEntry;
    /** 可选的元素抗性增益条目（null = 未授予）。 */
    @Nullable
    private StatEntry resEntry;
    /** 放置时消耗的全部荣耀（战旗到期回收用，#41 / GAP-FIX#17）。 */
    private int glorySpent;
    /** 存活时长（tick），到期自毁。 */
    private int lifetime = 600;
    /** 单次施加有效期（tick），落盘重建 entry 用。 */
    private int bannerLingerTicks;
    /** 烘焙后的 aura 幅度（落盘重建 entry 用）。 */
    private float auraMagnitude;
    /** 烘焙后的元素抗性幅度（0 = 未授予），落盘重建 entry 用。 */
    private float resMagnitude;
    @Nullable
    private EntityReference<LivingEntity> owner;

    public SkillBanner(EntityType<? extends SkillBanner> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_RADIUS, 4.0F);
    }

    /** 由 BannerAction 在投放时调用完成装配。半径源的圆心的圆心由驱动上下文（本实体位置）提供。 */
    public void configure(LivingEntity owner, StatEntry auraEntry, @Nullable StatEntry resEntry,
                          float radius, int lifetime, int glorySpent) {
        this.owner = EntityReference.of(owner);
        this.auraEntry = auraEntry;
        this.resEntry = resEntry;
        this.lifetime = Math.max(1, lifetime);
        this.glorySpent = glorySpent;
        this.bannerLingerTicks = auraEntry.duration() != null ? auraEntry.duration().ticks() : 1;
        this.auraMagnitude = (float) auraEntry.amount().getValue(ExpressionContext.get(owner));
        this.resMagnitude = resEntry != null
                ? (float) resEntry.amount().getValue(ExpressionContext.get(owner)) : 0.0F;
        this.setRadius(radius);
    }

    public float getRadius() {
        return this.getEntityData().get(DATA_RADIUS);
    }

    public void setRadius(float radius) {
        if (!this.level().isClientSide()) {
            this.getEntityData().set(DATA_RADIUS, Math.max(0.5F, radius));
            // 服务器侧：SynchedEntityData.set 不回调 onSyncedDataUpdated，故显式刷新 AABB，
            // 确保半径源粗筛覆盖真实半径（否则服务器侧 AABB 停在构造默认 4.0，圈外环内友方实体被漏筛）。
            this.refreshDimensions();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // 到期自毁：先按「放置时消耗的全部荣耀 × 回收比例」回收荣耀（#41 / GAP-FIX#17），再销毁。
        if (this.tickCount >= this.lifetime) {
            LivingEntity owner = this.getOwner();
            if (owner != null && this.glorySpent > 0) {
                double frac = StatEngine.magnitudeOf(owner, "banner_expire_glory_recover", Set.of());
                if (frac > 0.0) {
                    int recovered = (int) Math.round(frac * this.glorySpent);
                    if (recovered > 0) {
                        ResourceRegistry.recoverGlory(owner, ResourceRegistry.BANNER_GLORY, recovered, this.level().getGameTime());
                    }
                }
            }
            this.discard();
            return;
        }

        float radius = this.getRadius();
        if (this.tickCount % AURA_INTERVAL == 0) {
            LivingEntity owner = this.getOwner();
            if (owner != null) {
                // 半径光环：以自身位置为圆心，周期性驱动两条 entry（aura / 可选 elemental_res）。
                // center 传入本实体位置 → RADIUS_ALLIES 以战旗为圆心做 XZ 圆柱筛选（取代旧手写循环）。
                if (this.auraEntry != null) {
                    PeriodicDriver.drive(owner, this.auraEntry, this.tickCount, StatContext.EMPTY, null, this.position());
                }
                if (this.resEntry != null) {
                    PeriodicDriver.drive(owner, this.resEntry, this.tickCount, StatContext.EMPTY, null, this.position());
                }
            }
        }
        if (this.tickCount % PARTICLE_INTERVAL == 0) {
            this.spawnParticles(serverLevel, radius);
        }
    }

    /** 光环边界圈 + 旗杆柱粒子（提供存在感；渲染体为 Noop）。 */
    private void spawnParticles(ServerLevel level, float radius) {
        for (int i = 0; i < RING_SAMPLES; i++) {
            double angle = 2 * Math.PI * i / RING_SAMPLES;
            double px = this.getX() + Math.cos(angle) * radius;
            double pz = this.getZ() + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.END_ROD, px, this.getY() + 0.1, pz,
                    1, 0.0, 0.02, 0.0, 0.0);
        }
        level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.2, this.getZ(),
                1, 0.05, 0.05, 0.05, 0.0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_RADIUS.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    /** AABB 随半径缩放（×2 直径），高度固定覆盖站立实体 → 保证光环范围内实体可被粗筛命中。 */
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, BOX_HEIGHT);
    }

    @Override
    public void refreshDimensions() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        super.refreshDimensions();
        this.setPos(x, y, z);
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = EntityReference.of(owner);
    }

    @Nullable
    public LivingEntity getOwner() {
        return EntityReference.getLivingEntity(this.owner, this.level());
    }

    // 静态驻场：Entity 无 travel(Vec3)（那是 LivingEntity 的方法），
    // 恒定位置由构造函数 noPhysics=true + setNoGravity(true) 保证。

    @Override
    public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    public net.minecraft.world.level.material.PushReaction getPistonPushReaction() {
        return net.minecraft.world.level.material.PushReaction.IGNORE;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.tickCount = valueInput.getIntOr("Age", 0);
        this.lifetime = valueInput.getIntOr("Lifetime", 600);
        this.setRadius(valueInput.getFloatOr("Radius", 4.0F));
        this.glorySpent = valueInput.getIntOr("GlorySpent", 0);
        this.bannerLingerTicks = valueInput.getIntOr("BannerLingerTicks", 0);
        this.auraMagnitude = valueInput.getFloatOr("BannerMagnitude", 0.0F);
        this.resMagnitude = valueInput.getFloatOr("BannerResMagnitude", 0.0F);
        this.owner = EntityReference.read(valueInput, "Owner");
        // 重建运行时 entry：半径 / 时长 / 幅度从落盘字段重建（半径 Source 经 Codec 会丢失半径参数，故不走序列化）。
        if (this.auraMagnitude != 0.0F) {
            this.auraEntry = buildAuraEntry(this.auraMagnitude, this.getRadius(), this.bannerLingerTicks);
            this.resEntry = this.resMagnitude > 0.0F
                    ? buildResEntry(this.resMagnitude, this.getRadius(), this.bannerLingerTicks) : null;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.putInt("Age", this.tickCount);
        valueOutput.putInt("Lifetime", this.lifetime);
        valueOutput.putFloat("Radius", this.getRadius());
        valueOutput.putInt("GlorySpent", this.glorySpent);
        valueOutput.putInt("BannerLingerTicks", this.bannerLingerTicks);
        valueOutput.putFloat("BannerMagnitude", this.auraMagnitude);
        valueOutput.putFloat("BannerResMagnitude", this.resMagnitude);
        EntityReference.store(this.owner, valueOutput, "Owner");
    }

    /**
     * 构造 aura 增益 entry：半径友方光环，幅度已烘焙，时长 = lingerTicks（DURATION），
     * 节拍 = AURA_INTERVAL（INTERVAL）。
     *
     * <p>Phase 2b：范围不再由 Target 承担（Target 只锁 A / B），改挂
     * {@link RadiusCondition} 作为条目的实体获取条件；圆心不写死在条件里 —— 条件
     * {@code center} 缺省，由 {@link com.cimdy.coe.data.codec.support.stat.condition.ConditionScope#center()}
     * 解析（{@code PeriodicDriver.drive} 传入本实体位置），故仍是<b>以战旗为圆心</b>。
     */
    public static StatEntry buildAuraEntry(double bakedMagnitude, float radius, int lingerTicks) {
        StatusStatEffect fx = new StatusStatEffect(StatusDefs.BANNER_AURA,
                StatusStatEffect.Aspect.MAGNITUDE, GateExpr.ALWAYS);
        return StatEntry.of(ExpressionCodec.of(bakedMagnitude), List.of(),
                StatTargets.ofTarget(TargetSelector.SELF), fx)
                .withDuration(StatDuration.of(StatDuration.Kind.DURATION, lingerTicks))
                .withInterval(StatDuration.of(StatDuration.Kind.INTERVAL, AURA_INTERVAL))
                .withCondition(radiusAllies(radius));
    }

    /** 构造元素抗性增益 entry（与 aura 同源，仅 StatusType / 幅度不同）。 */
    public static StatEntry buildResEntry(double resMagnitude, float radius, int lingerTicks) {
        StatusStatEffect fx = new StatusStatEffect(StatusDefs.BANNER_ELEMENTAL_RES,
                StatusStatEffect.Aspect.MAGNITUDE, GateExpr.ALWAYS);
        return StatEntry.of(ExpressionCodec.of(resMagnitude), List.of(),
                StatTargets.ofTarget(TargetSelector.SELF), fx)
                .withDuration(StatDuration.of(StatDuration.Kind.DURATION, lingerTicks))
                .withInterval(StatDuration.of(StatDuration.Kind.INTERVAL, AURA_INTERVAL))
                .withCondition(radiusAllies(radius));
    }

    /** 「以作用域圆心为心的半径内盟友」获取条件（圆心由驱动上下文给出，见 {@link #buildAuraEntry}）。 */
    private static StatCondition radiusAllies(float radius) {
        return new RadiusCondition(ExpressionCodec.of((double) radius),
                PresenceCountValue.Relation.ALLY, Optional.empty());
    }
}
