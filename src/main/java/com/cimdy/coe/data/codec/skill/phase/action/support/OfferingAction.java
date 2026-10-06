package com.cimdy.coe.data.codec.skill.phase.action.support;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.stat.effect.status.StatusStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.duration.StatDuration;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.status.def.StatusType;

import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.stat.condition.RelationCondition;import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import com.cimdy.coe.data.helper.skill.SummonCountHelper;
import com.cimdy.coe.registry.world.EntityRegistry;
import com.cimdy.coe.entity.custom.skill.OfferingSpike;
import com.cimdy.coe.entity.custom.skill.OfferingType;
import com.cimdy.coe.helper.OwnerHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import java.util.Set;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;

/**
 * 奉献动作（Offer an Offering）— 技能施放时（{@code on_cast}）执行的复合逻辑。
 *
 * <p>对齐用户修正的 PoE2 奉献机制：
 * <ol>
 *   <li><b>牺牲骷髅</b>：从施法者召唤物中找一只骷髅（{@link EntityType#SKELETON}），找不到则放弃施放；
 *       找到则 {@code onMinionRemoved + discard} 牺牲。</li>
 *   <li><b>Limit（默认 1，可放宽）</b>：施放前若已达上限，先剥夺并移除最旧的奉献尖刺（留出空位），
 *       保证同一时刻至多 {@code limit} 个；技能数据设 {@code limit ≥ 2} 即激活「双奉献加伤」。</li>
 *   <li><b>生成尖刺</b>：在施法者身边生成 {@link OfferingSpike}（伤害型随从，走 {@code onSummoned} 计数）。</li>
 *   <li><b>施加增益</b>：把对应奉献状态施加给施法者（及其召唤物，经状态条目的 minion 作用域）。</li>
 * </ol>
 *
 * <p>尖刺存活期间增益生效；尖刺死亡/到期由 {@link OfferingSpike#dropBuff} 立即剥夺。
 * 双奉献加伤（≥2 种不同奉献）经 {@link OfferingSpike#countDistinctTypes} 前瞻接线，
 * 上限放宽至 ≥2 即激活。
 */
public record OfferingAction(
        OfferingType offerType,
        double duration,
        double health,
        double recentlyCreated,
        double radius,
        double limit
) implements Action {

    public static final Codec<OfferingAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            OfferingType.CODEC.fieldOf("offer_type").forGetter(OfferingAction::offerType),
            Codec.DOUBLE.optionalFieldOf("duration", 200.0).forGetter(OfferingAction::duration),
            Codec.DOUBLE.optionalFieldOf("health", 30.0).forGetter(OfferingAction::health),
            Codec.DOUBLE.optionalFieldOf("recently_created", 30.0).forGetter(OfferingAction::recentlyCreated),
            Codec.DOUBLE.optionalFieldOf("radius", 6.0).forGetter(OfferingAction::radius),
            Codec.DOUBLE.optionalFieldOf("limit", 1.0).forGetter(OfferingAction::limit)
    ).apply(i, OfferingAction::new));

    @Override
    public Action.Type<?> getType() {
        return ActionTypes.OFFERING;
    }

    @Override
    public void execute(ActionContext context) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel serverLevel)) return;

        // (a) 牺牲一只骷髅召唤物：找不到则放弃整个施放
        LivingEntity skeleton = findSkeleton(caster);
        if (skeleton == null) return;
        SummonCountHelper.onMinionRemoved(skeleton);
        skeleton.discard();

        // (b) 数量上限（默认 1，可由技能数据 limit 字段放宽至 ≥2 以激活「双奉献加伤」）：
        //     若已达上限，移除最旧的尖刺直至留出空位，再放置新尖刺。
        int lim = (int) Math.max(1, Math.floor(limit));
        List<OfferingSpike> existing = findSpikes(caster);
        int over = existing.size() - lim + 1;
        for (int i = 0; i < over; i++) {
            OfferingSpike s = existing.get(i);
            s.dropBuff();
            SummonCountHelper.onMinionRemoved(s);
            s.discard();
        }

        // (c) 生成尖刺（伤害型随从，靠近施法者）
        int lifetime = Math.max(1, (int) duration);
        int recently = Math.max(0, (int) recentlyCreated);
        OfferingSpike spike = new OfferingSpike(EntityRegistry.OFFERING_SPIKE.get(), serverLevel);
        spike.setPos(caster.getX(), caster.getY(), caster.getZ());
        spike.configure(caster, offerType, lifetime, (float) health, recently);
        // 阵营 PLAYER（与主人同阵营，被敌方视为攻击目标）；LEVEL=1 避免被误标 MOB 阵营 + 词缀
        spike.setData(com.cimdy.coe.registry.world.AttachmentRegistry.CAMP, com.cimdy.coe.data.codec.support.meta.Camp.PLAYER);
        spike.setData(com.cimdy.coe.registry.world.AttachmentRegistry.LEVEL, 1);
        // 归属标记：令 SummonCountHelper / TargetSelector 以 owner 解析到本尖刺（Tracking 为随从）
        OwnerHelper.setOwner(spike, caster);
        serverLevel.addFreshEntity(spike);
        // 随从计数 +1：实体已成功入世界
        SummonCountHelper.onSummoned(caster);

        // (d) 施加对应奉献增益给施法者（随从增益经状态条目的 minion 作用域生效）        applyOfferingStatus(caster, StatusDefs.forType(offerType), 1.0, lifetime);

        // 「≥2 种不同活跃奉献时 +X% 随从伤害」：幅度由 stat 经 OFFERING_DAMAGE_BONUS 属性（{minion} 作用域）给出
        // （零基，未授予该 stat 则不施加，替代原硬编码 +50%；limit 放宽至 ≥2 即激活，countDistinctTypes 真实求值）。
        if (OfferingSpike.countDistinctTypes(caster) >= 2) {
            double bonus = DamageHelper.readInScopePublic(caster, Set.of(GateTag.MINION), AttributeRegistry.OFFERING_DAMAGE_BONUS);            if (bonus > 0) {
                applyOfferingStatus(caster, StatusDefs.OFFERING_MINION_DAMAGE_BONUS, bonus, lifetime);
            }
        }
    }    /** 经 StatusStatEffect 触发通道施加奉献状态（与直接 StatusEngine.apply 等价，闭合 Stat 效果路径）。 */
    private static void applyOfferingStatus(LivingEntity caster, StatusType type, double magnitude, int durationTicks) {
        StatusStatEffect effect = new StatusStatEffect(type, StatusStatEffect.Aspect.MAGNITUDE, GateExpr.ALWAYS);
        StatEntry entry = StatEntry.of(ExpressionCodec.of(magnitude), java.util.List.of(),
                        StatTargets.ofTarget(TargetSelector.SELF), effect)
                .withDuration(StatDuration.of(StatDuration.Kind.DURATION, durationTicks));
        StatEvalContext evalCtx = new StatEvalContext(caster, null);
        StatContext scope = StatScopes.current(caster);
        effect.onTrigger(entry, scope, evalCtx, null);
    }

    /** 在施法者召唤物中找第一只骷髅（minecraft:skeletons 标签）。 */
    private static LivingEntity findSkeleton(LivingEntity caster) {
        for (LivingEntity minion : RelationCondition.resolveMinions(caster)) {
            if (minion.is(EntityTypeTags.SKELETONS)) return minion;
        }
        return null;
    }

    /** 枚举施法者当前拥有的全部奉献尖刺，按生成顺序（实体 id 升序，旧→新）排列，使超额时优先驱逐最旧。 */
    private static List<OfferingSpike> findSpikes(LivingEntity caster) {
        List<OfferingSpike> out = new ArrayList<>();
        for (Entity e : caster.level().getEntities(caster, caster.getBoundingBox().inflate(64.0))) {
            if (e instanceof OfferingSpike spike && caster.equals(spike.getOwner())) {
                out.add(spike);
            }
        }
        // 实体 id 单调递增分配：升序即「先生成者在前」，保证 (b) 驱逐最旧而非任意尖刺
        out.sort(java.util.Comparator.comparingInt(Entity::getId));
        return out;
    }
}
