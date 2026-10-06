package com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor;

import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area.AreaDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.PersistentDeliveryManager;
import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.HazardDelivery;
import com.cimdy.coe.data.codec.entity.EntityDef;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.entity.EntityDefCarrier;
import com.cimdy.coe.data.codec.entity.EntityDefs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 危害物执行器 — 在目标位置生成一个驻场 Hazard 实体。
 *
 * <p>与 {@link AreaDeliveryExecutor} 的关键差异：范围投递是<b>瞬时命中</b>（当场扫描实体并点燃 ON_HIT），
 * 危害物是<b>驻场物件</b>——本执行器只负责生成实体并把半径/存活时长装配进去，
 * 命中判定由 {@code SkillEntity} 在其存活期内周期触发、经
 * {@code SkillPhaseInstanceTracker} 反查阶段实例回调 {@code fireTrigger(ON_HIT)}。
 */
public class HazardDeliveryExecutor implements DeliveryExecutor<HazardDelivery> {

    @Override
    public void execute(HazardDelivery delivery, ActionContext context,
                        UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        // 施法者恒为 LivingEntity（图腾亦为 LivingEntity），此处 instanceof 仅作类型绑定。
        // 危害物的一切结算都以施法者为作用域主体（DamageAction 用 ActionContext.caster() 构造
        // SkillDamageSource；{hazard} 倍率读施法者属性），故施法者即危害物的归属者。
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel serverLevel)) return;

        SupportProfile profile = SupportProfile.from(context);
        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;

        // 中心点：Nova 用施法者位置，否则用上下文位置，缺省回退施法者位置（同 AreaDeliveryExecutor:47-49）
        Vec3 center = profile.nova(skillCtx) ? caster.position() : (
                context.position() != null && context.position() != Vec3.ZERO
                        ? context.position() : caster.position());

        // 基础半径：形状半径 × 辅助宝石/技能自身的 hazard 半径倍率
        double radius = delivery.shape().sampleRadius(skillCtx) * profile.radiusMultiplier("hazard", skillCtx);

        // 危害物范围加成：危害物有自己的范围词缀（inc_hazard_area_of_effect），
        // 对应 EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT_HAZARD（StatEffectGen:791），
        // 标签为纯 {hazard}，读 EFFECT_AREA。
        // withScope 内部走 StatScopes.enterMerged（AilmentHelper:443），会并入当前作用域栈，
        // 因此技能侧/动作侧既有标签不会被覆盖丢失。
        double areaMult = AilmentHelper.withScope(caster, Set.of(GateTag.HAZARD), AttributeRegistry.EFFECT_AREA);
        // 回响专属范围载荷（同 AreaDeliveryExecutor 口径：inc 加法、more 乘法），
        // 使「回响的法术造成的危害物」也吃到回响范围加成。
        if (instance != null) {
            areaMult += instance.echoAreaInc();
            radius *= instance.echoAreaMore();
        }
        radius *= areaMult;

        int lifetimeTicks = Math.max(1, delivery.lifetime().getInt(skillCtx));

        // 解析形态（引用 + 覆写渲染）：缺失则跳过生成（与既有行为一致，不静默生成无形态实体）。
        EntityRead read = delivery.ref();
        EntityDef def = read.resolve().orElse(null);
        if (def == null) {
            CraftOfExile.LOGGER.error("Hazard 投递：未找到 prop 定义 {}", read.ref());
            return;
        }

        // 生成数量（散布）：真值如铁蒺藜「翻滚时撒下 3 枚」。
        int total = Math.max(1, delivery.count().getInt(skillCtx));
        // 同时上限（真值「最多 20 枚同时存在」）：按「同施法者 + 同形态」统计在场危害物，
        // 达上限则本次不再生成（生成守卫，非枚举硬编码）；maxActive <= 0 视为不限。
        int maxActive = delivery.maxActive().getInt(skillCtx);
        int remaining = total;
        if (maxActive > 0) {
            int alive = 0;
            for (Entity e : serverLevel.getAllEntities()) {
                if (e instanceof EntityDefCarrier old
                        && old.entityRead() != null
                        && read.ref().equals(old.entityRead().ref())
                        && caster.getUUID().equals(ownerId(old))) {
                    alive++;
                }
            }
            remaining = Math.min(total, maxActive - alive);
        }
        if (remaining <= 0) return;

        // 多枚时绕中心均分一圈（真值「撒下 3 枚」，彼此不重叠）：散布半径取作用半径，最小 0.5 格。
        double scatter = Math.max(0.5, radius);
        // retain 阶段实例：驻场期间保证 tracker 可反查（SkillEntity 销毁时 release）。
        // 缺这一步，phase 实例会在危害物生成后被提前回收，危害物触发时反查不到阶段实例 → 命中静默失效。
        // 按 EntityDef.type 实例化承载实体 —— 投递不依赖任何具体实体类，
        // 换 type 即换载体（驻场道具 / 投射物 / 投掷物 …），只要实现 EntityDefCarrier 即可装配。
        for (int i = 0; i < remaining; i++) {
            Entity hazard = EntityDefs.create(serverLevel, def);
            if (!(hazard instanceof EntityDefCarrier carrier)) {
                CraftOfExile.LOGGER.error("Hazard 投递：实体类型 {} 未实现 EntityDefCarrier", def.type());
                return;
            }
            // retain 须在实体创建成功后再做，否则上面 return 会留下无法被 release 的计数
            if (casterId != null && phaseId != null) {
                SkillPhaseInstanceTracker.retain(casterId, phaseId);
            }
            Vec3 pos = remaining == 1 ? center : center.add(
                    Math.cos(2 * Math.PI * i / remaining) * scatter, 0,
                    Math.sin(2 * Math.PI * i / remaining) * scatter);
            hazard.setPos(pos.x, pos.y, pos.z);
            carrier.configure(caster, phaseId, lifetimeTicks, (float) radius, read);
            serverLevel.addFreshEntity(hazard);
        }
        // 发射音画提示（B4：危害物此前零音效）；中心点迸发一小簇能量 + 轻响，客户端自动渲染。
        PersistentDeliveryManager.launchCue(serverLevel, center);
    }

    /** 在场危害物的归属者 UUID（无法解析时返回 null，永不匹配）。 */
    private static java.util.UUID ownerId(EntityDefCarrier carrier) {
        return carrier instanceof com.cimdy.coe.entity.custom.skill.SkillEntity se && se.getOwner() != null
                ? se.getOwner().getUUID() : null;
    }
}
