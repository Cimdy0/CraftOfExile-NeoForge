package com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile;

import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.data.gen.stat.wirings.wire.flow.WireMisc;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.HazardDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.PersistentDeliveryManager;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.RemnantDelivery;
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
 * 残片执行器 —— 在目标位置生成一个可拾取的驻场 Remnant 实体。
 *
 * <p><b>本执行器只放残片，不定义任何效果。</b>效果（伤害 / 状态 / 增益）完全由技能数据里
 * 该 phase 的 {@code actions} 定义；残片被其施放者（owner）走过拾取时，由 {@code SkillEntity}
 * 经 {@code SkillPhaseInstanceTracker} 反查阶段实例并回调 {@code fireTrigger(ON_HIT)} 点燃那些 actions。
 *
 * <p>与 {@link HazardDeliveryExecutor} 的关键差异：
 * <ul>
 *   <li>残片必有施放者，故 caster 恒为 {@link LivingEntity} 并作类型绑定；</li>
 *   <li>半径倍率 key 用 {@code "remnant"}（对应辅助宝石/技能自身的 remnant 范围倍率）；</li>
 *   <li>叠加残片专属的「更远收集」词缀（{@code REMNANTS_COLLECTED_FURTHER_AWAY}）；</li>
 *   <li>叠加「额外残片」几率（{@code REMNANT_EXTRA_CHANCE}），命中则多生成一个偏移残片。</li>
 * </ul>
 */
public class RemnantDeliveryExecutor implements DeliveryExecutor<RemnantDelivery> {

    @Override
    public void execute(RemnantDelivery delivery, ActionContext context,
                        UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        // 残片必有施放者（残片/危害物必定有 caster）；此处 instanceof 仅作类型绑定。
        // 残片的一切结算都以施放者为作用域主体，故施放者即残片的归属者（owner）。
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel serverLevel)) return;

        SupportProfile profile = SupportProfile.from(context);
        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;

        // 中心点：Nova 用施放者位置，否则用上下文位置，缺省回退施放者位置（同 HazardDeliveryExecutor）。
        Vec3 center = profile.nova(skillCtx) ? caster.position() : (
                context.position() != null && context.position() != Vec3.ZERO
                        ? context.position() : caster.position());

        // 基础半径：形状半径 × 辅助宝石/技能自身的 remnant 半径倍率
        double radius = delivery.shape().sampleRadius(skillCtx) * profile.radiusMultiplier("remnant", skillCtx);

        // 残片「更远收集」词缀：REMNANTS_CAN_BE_COLLECTED_FROM_FURTHER_AWAY
        // （WireMisc:570 → EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REMNANTS_COLLECTED_FURTHER_AWAY_REMNANT），
        // 标签为纯 {remnant}，读 REMNANTS_COLLECTED_FURTHER_AWAY。
        // withScope 内部走 StatScopes.enterMerged（AilmentHelper:441），会并入当前作用域栈，
        // 因此技能侧/动作侧既有标签不会被覆盖丢失。
        double pickupMult = AilmentHelper.withScope(caster, Set.of(GateTag.REMNANT),
                AttributeRegistry.REMNANTS_COLLECTED_FURTHER_AWAY);
        radius *= pickupMult;

        int lifetimeTicks = Math.max(1, delivery.lifetime().getInt(skillCtx));

        // 解析形态（引用 + 覆写渲染）：缺失则跳过生成（与既有行为一致，不静默生成无形态实体）。
        EntityRead read = delivery.ref();
        EntityDef def = read.resolve().orElse(null);
        if (def == null) {
            CraftOfExile.LOGGER.error("Remnant 投递：未找到 prop 定义 {}", read.ref());
            return;
        }

        // 主残片：retain 阶段实例保证 tracker 可反查（SkillEntity 销毁时 release）。
        // 缺这一步，phase 实例会在残片生成后被提前回收，残片被拾取时反查不到阶段实例 → 命中静默失效。
        spawnRemnant(serverLevel, center, caster, casterId, phaseId, lifetimeTicks, (float) radius, read, def);
        // 发射音画提示（B4：残片此前零音效）；生成点迸发一小簇能量 + 轻响，客户端自动渲染。
        PersistentDeliveryManager.launchCue(serverLevel, center);

        // 残片「额外生成」几率：CHANCE_TO_CREATE_AN_ADDITIONAL_REMNANT
        // （WireMisc:155 → EFF_ADD_VALUE_ATTRIBUTE_COE_REMNANT_EXTRA_CHANCE_REMNANT）。
        // REMNANT_EXTRA_CHANCE 为 registerPercentage（0~1 小数，默认 0）。属性为 0 时零开销、行为不变。
        double extraChance = AilmentHelper.withScope(caster, Set.of(GateTag.REMNANT),
                AttributeRegistry.REMNANT_EXTRA_CHANCE);
        if (extraChance > 0.0 && caster.getRandom().nextDouble() < extraChance) {
            // 中心附近小幅偏移（0.6~1.2 格），避免与主残片完全重叠。
            double ang = caster.getRandom().nextDouble() * Math.PI * 2.0;
            double dist = 0.6 + caster.getRandom().nextDouble() * 0.6;
            Vec3 extraPos = center.add(Math.cos(ang) * dist, 0.0, Math.sin(ang) * dist);
            spawnRemnant(serverLevel, extraPos, caster, casterId, phaseId, lifetimeTicks, (float) radius, read, def);
        }
    }

    /**
     * 生成一个残片实体并装配（owner / phaseId / 存活时长 / 触发半径），同时 retain 阶段实例引用计数。
     * 每个残片实体销毁时各 {@code release} 一次，保证引用计数平衡。
     */
    private static void spawnRemnant(ServerLevel serverLevel, Vec3 pos, LivingEntity caster,
                                     UUID casterId, UUID phaseId, int lifetimeTicks, float radius,
                                     EntityRead read, EntityDef def) {
        // 按 EntityDef.type 实例化承载实体 —— 投递不依赖任何具体实体类，
        // 换 type 即换载体（驻场道具 / 投射物 / 投掷物 …），只要实现 EntityDefCarrier 即可装配。
        Entity remnant = EntityDefs.create(serverLevel, def);
        if (!(remnant instanceof EntityDefCarrier carrier)) {
            CraftOfExile.LOGGER.error("Remnant 投递：实体类型 {} 未实现 EntityDefCarrier", def.type());
            return;
        }
        // retain 须在实体创建成功后再做，否则上面 return 会留下无法被 release 的计数
        if (casterId != null && phaseId != null) {
            SkillPhaseInstanceTracker.retain(casterId, phaseId);
        }
        remnant.setPos(pos.x, pos.y, pos.z);
        carrier.configure(caster, phaseId, lifetimeTicks, radius, read);
        serverLevel.addFreshEntity(remnant);
    }
}
