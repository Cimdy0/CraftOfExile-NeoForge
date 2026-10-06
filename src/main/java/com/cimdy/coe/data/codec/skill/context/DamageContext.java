package com.cimdy.coe.data.codec.skill.context;

import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryRuntimeContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryTracker;
import com.cimdy.coe.event.skill.SkillEvent;
import com.cimdy.coe.entity.EntityDefCarrier;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class DamageContext {
    private HitResult hitResult;
    private boolean isBlock;
    private boolean isEvasion;
    private boolean isParry;
    private boolean isCrit;
    private boolean[] isAliment = new boolean[8];
    private float[] damages = new float[0];
    private float damageEnergy;
    private float damageMana;
    private float damageHealth;
    private float damageRunicWard;
    private float overflowDamage;

    public DamageContext() {
    }

    /**
     * 合并另一次命中的伤害上下文。用于把同一阶段实例在本 tick 的多次命中累计到一处，
     * 供 {@code on_tick} 读取：
     * - 数值字段（能量盾/生命/溢出伤害）累加；
     * - 布尔状态（格挡/闪避/暴击）与异常标志按 OR 合并（任一命中成立即成立）。
     */
    public DamageContext merge(DamageContext other) {
        if (other.hitResult != null) this.hitResult = other.hitResult;
        this.isBlock = this.isBlock || other.isBlock;
        this.isEvasion = this.isEvasion || other.isEvasion;
        this.isCrit = this.isCrit || other.isCrit;
        // 招架标志按 OR 合并（任一命中成立即成立）；原实现遗漏，导致同 tick 多次命中时 parry 标志被丢弃
        this.isParry = this.isParry || other.isParry;
        if (other.isAliment != null) {
            for (int i = 0; i < this.isAliment.length && i < other.isAliment.length; i++) {
                this.isAliment[i] = this.isAliment[i] || other.isAliment[i];
            }
        }
        if (other.damages != null && other.damages.length > 0) {
            if (this.damages == null || this.damages.length != other.damages.length) {
                this.damages = other.damages.clone();
            } else {
                for (int i = 0; i < this.damages.length; i++) {
                    this.damages[i] += other.damages[i];
                }
            }
        }
        this.damageEnergy += other.damageEnergy;
        this.damageMana += other.damageMana;
        this.damageHealth += other.damageHealth;
        this.damageRunicWard += other.damageRunicWard;
        this.overflowDamage += other.overflowDamage;
        return this;
    }

    public DamageContext initialize() {
        return new DamageContext();
    }

    public float[] getDamages() {
        return this.damages;
    }

    public boolean isHit() {
        return this.hitResult != null;
    }

    public boolean isHitBlock() {
        return this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK;
    }

    public boolean isHitEntity() {
        return this.hitResult != null && this.hitResult.getType() == HitResult.Type.ENTITY;
    }

    public boolean isBlock() {
        return this.isBlock;
    }

    public boolean isParry() {
        return this.isParry;
    }

    public boolean isEvasion() {
        return this.isEvasion;
    }

    public boolean isCrit() {
        return this.isCrit;
    }

    public boolean isChill() {
        return this.isAliment != null && this.isAliment[0];
    }

    public boolean isFreeze() {
        return this.isAliment != null && this.isAliment[1];
    }

    public boolean isShock() {
        return this.isAliment != null && this.isAliment[2];
    }

    public boolean isShock2() {
        return this.isAliment != null && this.isAliment[3];
    }

    public boolean isBleeding() {
        return this.isAliment != null && this.isAliment[4];
    }

    public boolean isIgnite() {
        return this.isAliment != null && this.isAliment[5];
    }

    public boolean isPoison() {
        return this.isAliment != null && this.isAliment[6];
    }

    public boolean isStun() {
        return this.isAliment != null && this.isAliment[7];
    }

    public float getDamageEnergy() {
        return this.damageEnergy;
    }

    public float getDamageMana() {
        return this.damageMana;
    }

    public float getDamageHealth() {
        return this.damageHealth;
    }

    public float getOverflowDamage() {
        return this.overflowDamage;
    }

    public DamageContext Hit(HitResult hitResult) {
        this.hitResult = hitResult;
        return this;
    }

    public DamageContext Evasion(boolean isEvasion) {
        this.isEvasion = isEvasion;
        return this;
    }

    public DamageContext Block(boolean isBlock) {
        this.isBlock = isBlock;
        return this;
    }

    public DamageContext Parry(boolean isParry) {
        this.isParry = isParry;
        return this;
    }

    public DamageContext Crit(boolean isCrit) {
        this.isCrit = isCrit;
        return this;
    }

    public DamageContext damages(float[] damages) {
        this.damages = damages;
        return this;
    }

    public DamageContext damageEnergy(float damageEnergy) {
        this.damageEnergy = damageEnergy;
        return this;
    }

    public DamageContext damageMana(float damageMana) {
        this.damageMana = damageMana;
        return this;
    }

    public DamageContext damageHealth(float damageHealth) {
        this.damageHealth = damageHealth;
        return this;
    }

    public DamageContext damageRunicWard(float damageRunicWard) {
        this.damageRunicWard = damageRunicWard;
        return this;
    }

    public DamageContext overflowDamage(float overflowDamage) {
        this.overflowDamage = overflowDamage;
        return this;
    }

    public DamageContext causeChill(boolean chill) {
        this.isAliment[0] = chill;
        return this;
    }

    public DamageContext causeFreeze(boolean freeze) {
        this.isAliment[1] = freeze;
        return this;
    }

    public DamageContext causeShock(boolean shock) {
        this.isAliment[2] = shock;
        return this;
    }

    public DamageContext causeShock2(boolean shock) {
        this.isAliment[3] = shock;
        return this;
    }

    public DamageContext causeStun(boolean stun) {
        this.isAliment[7] = stun;
        return this;
    }

    /**
     * 缓存一次命中产生的伤害上下文。索引键统一为 {@code (casterUUID, phaseId)}：
     * - 技能直接伤害（{@code DamageAction} 经 {@code SkillDamageSource} 携带 phaseId）→ 由 skill 反查 caster；
     * - 投射物原生命中伤害（伤害源 directEntity 为 {@code SkillArrow}）→ 由 {@link DeliveryTracker} 反查阶段归属。
     * 这样 {@code SkillPhaseInstance.tick} 用 {@code (caster, phaseId)} 即可在本 tick 末汇总该阶段全部命中伤害，
     * 供 {@code on_tick} 读取。非阶段伤害源退化为旧索引（无读取方，仅保兼容）。
     */
    public static void cacheDamageContext(DamageSource source, LivingEntity living, DamageContext damageContext) {
        UUID casterId = null;
        UUID phaseId = null;
        if (source instanceof SkillDamageSource sds && sds.getPhaseId() != null && sds.getSkill() != null) {
            casterId = sds.getSkill().getContext().getCaster().getUUID();
            phaseId = sds.getPhaseId();
        } else if (source.getDirectEntity() != null
                && DeliveryTracker.get(source.getDirectEntity().getUUID()) != null) {
            // 投射物原生命中伤害（伤害源 directEntity 持有 DeliveryRuntimeContext）→ 由 DeliveryTracker 反查阶段归属；
            // 不再依赖 instanceof SkillArrow，任意承载实体（含 coe:skill_arrow 指向的 SkillEntity）均可反查。
            DeliveryRuntimeContext ctx = DeliveryTracker.get(source.getDirectEntity().getUUID());
            if (ctx != null) {
                casterId = ctx.casterId();
                phaseId = ctx.phaseId();
            }
        }
        if (casterId != null && phaseId != null) {
            SkillEvent.cacheDamageContext.merge(Pair.of(casterId, phaseId), damageContext, DamageContext::merge);
        } else {
            // 与上方主路径同口径：能识别「谁打的」时用伤害载体的 UUID。
            // 投射物分支不能只认 Projectile ——技能投射物由 SkillEntity
            // 承载（非 Projectile），否则落到 living.getUUID() 与承伤者共用同一索引键，
            // 异步投射物的伤害上下文会串到承伤者键上。
            UUID uuid = source instanceof SkillDamageSource skillSource ? skillSource.getUUID() :
                    carrierOrProjectileId(source.getDirectEntity()) != null
                            ? carrierOrProjectileId(source.getDirectEntity()) : living.getUUID();
            SkillEvent.cacheDamageContext.merge(Pair.of(living.getUUID(), uuid), damageContext, DamageContext::merge);
        }
    }

    /**
     * 伤害载体的索引键：投射物 / 载体用自身 UUID，否则返回 null。
     *
     * <p>与 {@code GameEvent.formTagsOf} / {@code DamageHelper.getMixedDamage} 同一口径：
     * 能承载投射物的实体（{@code EntityDefCarrier}）与原版投射物（{@code Projectile}）
     * 均算「独立的伤害来源」；两者都不是承伤者时才回退承伤者键。
     */
    @Nullable
    private static UUID carrierOrProjectileId(@Nullable Entity direct) {
        if (direct instanceof EntityDefCarrier || direct instanceof Projectile) return direct.getUUID();
        return null;
    }

    public LivingEntity getTarget() {
        if (this.hitResult != null) {
            if (this.hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) this.hitResult;
                Entity entity = entityHitResult.getEntity();
                if (entity instanceof LivingEntity living) {
                    return living;
                }
            }
        }
        return null;
    }
}