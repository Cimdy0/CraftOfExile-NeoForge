package com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile.ProjectileDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.PersistentDeliveryManager;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.entity.EntityDef;
import com.cimdy.coe.data.codec.entity.EntityDefs;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.BallDelivery;
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
 * 球执行器 — 生成一个 {@code coe:skill_ball} 球并把它推出去。
 *
 * <p><b>本执行器只放球，不定义任何效果。</b>放电的伤害 / 状态 / 事件完全由技能数据里
 * 该 phase 的 {@code actions} 定义；球每 {@code pulse_interval} tick 由 {@code coe:skill_ball}
 * 对半径内目标逐一执行 {@link EntityDef#onTrigger()}（默认点燃 ON_HIT）回调那些 actions。
 *
 * <p><b>为何必须独立于 {@link ProjectileDeliveryExecutor}</b>：投射物在飞行途中命中实体即结算并消失，
 * 而球必须<b>穿过实体、持续放能</b>——本体不产生任何命中。两者是不同的命中语义，
 * 不是同一投递的两个参数。
 *
 * <p><b>投递不依赖具体实体类</b>：按 {@code EntityDef#type()} 经 {@link EntityDefs#create} 实例化，
 * 只要该实体实现 {@link EntityDefCarrier} 即可装配；换一份数据即可换载体。
 */
public class BallDeliveryExecutor implements DeliveryExecutor<BallDelivery> {

    @Override
    public void execute(BallDelivery delivery, ActionContext context,
                        UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        // 球必有施放者（同残片/危害物/手雷：一切结算以施放者为作用域主体，施放者即球的 owner）。
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

        // 解析形态（引用 + 覆写渲染 / 名称）：缺失则跳过生成，不静默产出无形态的球
        EntityRead read = delivery.ref();
        EntityDef def = read.resolve().orElse(null);
        if (def == null) {
            CraftOfExile.LOGGER.error("Ball 投递：未找到实体定义 {}", read.ref());
            return;
        }

        // 脉冲半径：基础半径 × 辅助宝石的投射物范围倍率，再经 {projectile} 作用域的 EFFECT_AREA 放大。
        // POE2 电球真值：品质词缀「弩箭以 +(0—0.4) 公尺内的敌人为目标」即范围可被放大。
        double radius = delivery.radius().getFloat(skillCtx)
                * profile.radiusMultiplier("projectile", skillCtx);
        double areaMult = AilmentHelper.withScope(caster, Set.of(GateTag.PROJECTILE), AttributeRegistry.EFFECT_AREA);
        if (instance != null) {
            areaMult += instance.echoAreaInc();
            radius *= instance.echoAreaMore();
        }
        radius *= areaMult;

        int lifetimeTicks = Math.max(1, delivery.lifetime().getInt(skillCtx));
        float speed = (float) delivery.speed().getFloat(skillCtx) * profile.projectileSpeedMultiplier(skillCtx);

        spawnBall(level, caster, casterId, phaseId, origin, direction,
                (float) radius, lifetimeTicks, speed, read, def);
    }

    /**
     * 生成一个球并装配（owner / 阶段 / 存活上限 / 脉冲半径 / 形态），同时 retain 阶段实例引用计数。
     * 球销毁时各 {@code release} 一次（在 {@code onRemoval}），保证引用计数平衡。
     */
    private static void spawnBall(ServerLevel level, LivingEntity caster, UUID casterId, UUID phaseId,
                                  Vec3 origin, Vec3 direction, float radius, int lifetimeTicks,
                                  float speed, EntityRead read, EntityDef def) {
        // 按 EntityDef.type 实例化承载实体 —— 换 type 即换载体，只要实现 EntityDefCarrier 即可装配
        Entity ball = EntityDefs.create(level, def);
        if (!(ball instanceof EntityDefCarrier carrier)) {
            CraftOfExile.LOGGER.error("Ball 投递：实体类型 {} 未实现 EntityDefCarrier", def.type());
            return;
        }
        // retain 须在实体创建成功后再做，否则上面 return 会留下无法被 release 的计数
        if (casterId != null && phaseId != null) {
            SkillPhaseInstanceTracker.retain(casterId, phaseId);
        }
        ball.setPos(origin.x, origin.y, origin.z);
        // 装配只传「实体契约」：owner / 阶段 / 存活上限 / 脉冲半径 / 形态定义；
        // 初速度不属实体契约（由本投递的 speed 给出），故单独设置。
        carrier.configure(caster, phaseId, lifetimeTicks, radius, read);
        ball.setDeltaMovement(direction.scale(speed));
        level.addFreshEntity(ball);
        // 发射音画提示（B4：球此前零音效）；生成点迸发一小簇能量 + 轻响，客户端自动渲染。
        PersistentDeliveryManager.launchCue(level, origin);
    }
}
