package com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.HazardDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.PersistentDeliveryManager;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.entity.EntityDef;
import com.cimdy.coe.data.codec.entity.EntityDefs;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.entity.behavior.EntityBehavior;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.OrbDelivery;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.helper.engine.entity.EntityCategoryLimit;
import com.cimdy.coe.entity.EntityDefCarrier;
import com.cimdy.coe.registry.world.EntityRegistry;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

/**
 * Orb 执行器 — 生成一个 {@code coe:orb} 驻场球，并把它放在目标位置。
 *
 * <p><b>本执行器只放球，不定义任何效果。</b>放电的伤害 / 状态 / 事件完全由技能数据里
 * 该 phase 的 {@code actions} 定义；球每 {@code pulse_interval} tick 由行为组件
 * {@code coe:pulse} 对半径内目标逐一执行 {@link EntityDef#onTrigger()}（默认点燃 ON_HIT）。
 *
 * <p><b>投递不依赖具体实体类</b>：按 {@link EntityDef#type()} 经 {@link EntityDefs#create} 实例化，
 * 只要该实体实现 {@link EntityDefCarrier} 即可装配；换一份数据即可换载体。
 *
 * <p><b>节奏（脉冲间隔）由本执行器注入</b>：它是技能层参数（风暴漩涡 4 秒 / 日曜球 1.4 秒），
 * 与「初速度由执行器设置」同一口径 —— 都不属实体形态契约，故不入 {@code EntityDef}。
 */
public class OrbDeliveryExecutor implements DeliveryExecutor<OrbDelivery> {

    @Override
    public void execute(OrbDelivery delivery, ActionContext context,
                        UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        // Orb 必有施放者（同残片/危害物/手雷：一切结算以施放者为作用域主体，施放者即球的 owner）。
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel level)) return;

        SupportProfile profile = SupportProfile.from(context);
        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;

        // 落点：取上下文位置（瞄准点/地面目标），缺省回退施法者位置（同 HazardDeliveryExecutor 口径）
        Vec3 center = context.position() != null && context.position() != Vec3.ZERO
                ? context.position() : caster.position();

        // 作用半径：基础半径 × 辅助宝石范围倍率 × {area} 作用域的 EFFECT_AREA × 回响加成
        double radius = delivery.radius().getFloat(skillCtx)
                * profile.radiusMultiplier("area", skillCtx);
        double areaMult = AilmentHelper.withScope(caster, Set.of(GateTag.AREA), AttributeRegistry.EFFECT_AREA);
        if (instance != null) {
            areaMult += instance.echoAreaInc();
            radius *= instance.echoAreaMore();
        }
        radius *= areaMult;

        // 首类「法术时长」：驻场球存在时长按 SPELL_DURATION_MULT 缩放（{spell,storm} 的 X% less Duration，
        // 如风暴漩涡 orb_of_storms；作用域已并入技能标签，非施法场景恒 1.0）。
        double spellDurationMult = AilmentHelper.spellDurationMultiplier(caster);
        int lifetimeTicks = Math.max(1, (int) (delivery.lifetime().getInt(skillCtx) * spellDurationMult));
        int pulseTicks = Math.max(0, delivery.pulseInterval().getInt(skillCtx));

        // 解析形态（引用 + 覆写渲染 / 名称）：缺失则跳过生成，不静默产出无形态的球
        EntityRead read = delivery.ref();
        EntityDef def = read.resolve().orElse(null);
        if (def == null) {
            CraftOfExile.LOGGER.error("Orb 投递：未找到实体定义 {}", read.ref());
            return;
        }

        // 数量上限走通用设施「按实体类别的数量上限」：以形态 id（def.type()）为类别，
        // 已登记类别（coe:orb → MAX_ORB）按池计数，未登记形态（残片 / 危害物等）恒放行（UNLIMITED）。
        // 语义不变：全局池、base=1 硬上限 —— 在身同类别数达上限则跳过本次生成。
        // 注：原「Limit 1」无条件清旧球的写法会令 orb 计数恒坍缩为 ≤1、使 +1 天赋失效，故改为上限守卫（不再无条件替换旧球）。
        if (!EntityCategoryLimit.canSpawn(caster, level, def.type())) return;

        Entity orb = EntityDefs.create(level, def);
        if (!(orb instanceof EntityDefCarrier carrier)) {
            CraftOfExile.LOGGER.error("Orb 投递：实体类型 {} 未实现 EntityDefCarrier", def.type());
            return;
        }
        // retain 须在实体创建成功后再做，否则上面 return 会留下无法被 release 的计数
        if (casterId != null && phaseId != null) {
            SkillPhaseInstanceTracker.retain(casterId, phaseId);
        }
        orb.setPos(center.x, center.y, center.z);
        carrier.configure(caster, phaseId, lifetimeTicks, (float) radius, read);
        // 脉冲节奏：技能层参数（同初速度），经全局运行参数注入 —— 投递不关心它落在第几个行为槽
        if (orb instanceof SkillEntity entity && pulseTicks > 0) {
            entity.putParam(EntityBehavior.PARAM_PULSE_INTERVAL, pulseTicks);
        }
        level.addFreshEntity(orb);
        // 发射音画提示（B4：Orb 此前零音效）；生成点迸发一小簇能量 + 轻响，客户端自动渲染。
        PersistentDeliveryManager.launchCue(level, center);
    }

}
