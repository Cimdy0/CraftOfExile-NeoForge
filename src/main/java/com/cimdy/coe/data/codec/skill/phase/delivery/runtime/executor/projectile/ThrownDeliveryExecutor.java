package com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile;

import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area.AreaDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.PersistentDeliveryManager;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.entity.EntityDef;
import com.cimdy.coe.data.codec.entity.EntityDefs;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.ThrownDelivery;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.entity.EntityDefCarrier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

/**
 * 投掷物执行器 — 生成一个或多个 {@code coe:grenade} 弹体（手雷）并把它扔出去。
 *
 * <p><b>本执行器只放弹体，不定义任何效果。</b>爆炸的伤害 / 状态 / 事件完全由技能数据里
 * 该 phase 的 {@code actions} 定义；弹体落地并走完引信后，由 {@code coe:grenade}
 * 对半径内目标逐一执行 {@link EntityDef#onTrigger()}（默认点燃 ON_HIT）回调那些 actions。
 *
 * <p><b>为什么必须独立于 {@link ProjectileDeliveryExecutor}</b>：投射物在飞行途中
 * 命中实体即触发，而投掷物必须穿过实体、落地才起爆——两者是<b>不同的命中语义</b>，
 * 不是同一投递的两个参数。故本执行器不与实体碰撞，只给初速度与重力（重力来自 {@link EntityDef}）。
 *
 * <p><b>投递不依赖具体实体类</b>：按 {@code EntityDef#type()} 经 {@link EntityDefs#create} 实例化，
 * 只要该实体实现 {@link EntityDefCarrier} 即可装配；换一份数据即可换载体。
 */
public class ThrownDeliveryExecutor implements DeliveryExecutor<ThrownDelivery> {

    /** 额外弹体之间的扇形展开总角度（弧度），与投射物齐射同口径。 */
    private static final double SPREAD_TOTAL = Math.toRadians(20);

    @Override
    public void execute(ThrownDelivery delivery, ActionContext context,
                        UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        // 投掷物必有施放者（同残片/危害物：一切结算以施放者为作用域主体，施放者即弹体的 owner）。
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel level)) return;

        SupportProfile profile = SupportProfile.from(context);
        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;

        Vec3 direction = (context.direction() != null && context.direction() != Vec3.ZERO
                ? context.direction()
                : caster.getLookAngle()).normalize();
        // origin：优先用 delivery 覆盖（标量值解释为沿发射方向前移的格数）；否则回退眼睛位置（原行为）。
        Vec3 origin = delivery.origin()
                .map(v -> caster.getEyePosition().add(direction.scale(v.getValue(skillCtx))))
                .orElseGet(caster::getEyePosition);

        // 引爆半径：基础半径 × 辅助宝石/技能自身的 grenade 半径倍率
        double radius = delivery.radius().getFloat(skillCtx)
                * profile.radiusMultiplier("grenade", skillCtx);

        // 手雷范围加成：INC_GRENADE_AREA_OF_EFFECT
        // （StatEffectGen:797 → EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT_GRENADE），
        // 标签为纯 {grenade}，读 EFFECT_AREA。withScope 内部走 StatScopes.enterMerged，
        // 会并入当前作用域栈，故技能侧/动作侧既有标签不会被覆盖丢失。
        double areaMult = AilmentHelper.withScope(caster, Set.of(GateTag.GRENADE), AttributeRegistry.EFFECT_AREA);
        // 回响专属范围载荷（同 AreaDeliveryExecutor 口径：inc 加法、more 乘法）
        if (instance != null) {
            areaMult += instance.echoAreaInc();
            radius *= instance.echoAreaMore();
        }
        radius *= areaMult;

        int lifetimeTicks = Math.max(1, delivery.lifetime().getInt(skillCtx));

        // 解析形态（引用 + 覆写渲染 / 名称）：缺失则跳过生成，不静默产出无形态的弹体
        EntityRead read = delivery.ref();
        EntityDef def = read.resolve().orElse(null);
        if (def == null) {
            CraftOfExile.LOGGER.error("Thrown 投递：未找到实体定义 {}", read.ref());
            return;
        }

        float speed = (float) delivery.speed().getFloat(skillCtx) * profile.projectileSpeedMultiplier(skillCtx);
        float arc = (float) delivery.arc().getFloat(skillCtx);
        // 「额外投掷」：辅助宝石（多重投射一类）与技能自身的 additional_projectiles
        int count = 1 + profile.additionalProjectiles(skillCtx);

        // 扇形展开：以投掷方向为轴，绕垂直轴均分（count=1 时退化为单发，与旧行为一致）
        Vec3 axis = Math.abs(direction.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 perp = axis.cross(direction).normalize();
        for (int i = 0; i < count; i++) {
            double angle = count == 1 ? 0.0 : -SPREAD_TOTAL / 2 + SPREAD_TOTAL * i / (count - 1);
            spawnThrown(level, caster, casterId, phaseId, origin,
                    count == 1 ? direction : rotateAround(direction, perp, angle),
                    (float) radius, lifetimeTicks, speed, arc, read, def);
        }
        // 发射音画提示（B4：投掷物此前零音效）；施放点迸发一小簇能量 + 轻响，客户端自动渲染。
        PersistentDeliveryManager.launchCue(level, origin);
    }

    /** 绕任意单位轴旋转向量（Rodrigues 旋转公式），与投射物齐射同实现。 */
    private static Vec3 rotateAround(Vec3 v, Vec3 axis, double angle) {
        double cos = Math.cos(angle), sin = Math.sin(angle);
        Vec3 cross = axis.cross(v);
        double dot = axis.dot(v);
        return v.scale(cos).add(cross.scale(sin)).add(axis.scale(dot * (1 - cos))).normalize();
    }

    /**
     * 生成一个弹体并装配（owner / 阶段 / 存活上限 / 引爆半径 / 形态），同时 retain 阶段实例引用计数。
     * 每个弹体销毁时各 {@code release} 一次，保证引用计数平衡。
     */
    private static void spawnThrown(ServerLevel level, LivingEntity caster, UUID casterId, UUID phaseId,
                                    Vec3 origin, Vec3 direction, float radius, int lifetimeTicks,
                                    float speed, float arc, EntityRead read, EntityDef def) {
        // 按 EntityDef.type 实例化承载实体 —— 换 type 即换载体（驻场道具 / 投射物 / 投掷物 …），
        // 只要实现 EntityDefCarrier 即可装配。
        Entity thrown = EntityDefs.create(level, def);
        if (!(thrown instanceof EntityDefCarrier carrier)) {
            CraftOfExile.LOGGER.error("Thrown 投递：实体类型 {} 未实现 EntityDefCarrier", def.type());
            return;
        }
        // retain 须在实体创建成功后再做，否则上面 return 会留下无法被 release 的计数
        if (casterId != null && phaseId != null) {
            SkillPhaseInstanceTracker.retain(casterId, phaseId);
        }
        thrown.setPos(origin.x, origin.y, origin.z);
        // 装配只传「实体契约」：owner / 阶段 / 存活上限 / 引爆半径 / 形态定义；
        // 初速度不属实体契约（由本投递的 speed × arc 给出），故单独设置。
        carrier.configure(caster, phaseId, lifetimeTicks, radius, read);
        thrown.setDeltaMovement(direction.scale(speed).add(0.0, speed * arc, 0.0));
        level.addFreshEntity(thrown);
    }
}
