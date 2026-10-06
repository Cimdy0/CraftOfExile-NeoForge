package com.cimdy.coe.data.codec.skill.phase.action.charge;

import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;

import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 充能球动作 — 生成或消耗充能球。
 * PoE2: Enduring Cry, Frenzy, Power Siphon 等技能的效果。
 *
 * 模式:
 * - GENERATE: 生成充能球（上限受 maxCharges 控制）
 * - CONSUME: 消耗充能球并触发效果（最小消费 minConsume，超过时每个充能球提供 multiplier）
 * - REFRESH: 刷新所有充能球持续时间
 */
public record ChargeAction(
        ChargeType chargeType,
        ChargeActionMode mode,
        ExpressionCodec.Value amount,
        Optional<ExpressionCodec.Value> minConsume,
        Optional<ExpressionCodec.Value> effectPerCharge,
        Optional<ExpressionCodec.Value> grantDurationTicks,
        boolean bypassCooldown,
        /**
         * 本次充能操作的<b>上下文标签</b>（数据侧携带，如 {@code ["totem"]}）。
         * <p>仅供「充能消耗豁免」类标记做交集判定用（{@code EFF_CHARGE_COST_EXEMPT}）；
         * 缺省空列表 → 旧数据零破坏、行为与既有完全一致。
         */
        List<String> tags
) implements Action {
    public static final Codec<ChargeAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            ChargeType.CODEC.fieldOf("charge_type").forGetter(ChargeAction::chargeType),
            ChargeActionMode.CODEC.fieldOf("mode").forGetter(ChargeAction::mode),
            ExpressionCodec.VALUE_CODEC.fieldOf("amount").forGetter(ChargeAction::amount),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("min_consume")
                    .forGetter(ChargeAction::minConsume),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("effect_per_charge")
                    .forGetter(ChargeAction::effectPerCharge),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("grant_duration_ticks")
                    .forGetter(ChargeAction::grantDurationTicks),
            Codec.BOOL.optionalFieldOf("bypass_cooldown", false)
                    .forGetter(ChargeAction::bypassCooldown),
            Codec.STRING.listOf().optionalFieldOf("tags", List.of())
                    .forGetter(ChargeAction::tags)
    ).apply(i, ChargeAction::new));

    @Override public Action.Type<?> getType() { return ActionTypes.CHARGE; }

    @Override
    public void execute(ActionContext context) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;
        long gameTime = caster.level().getGameTime();
        int amt = (int) Math.max(0, amount.getValue(skillCtx));

        switch (mode) {
            case GENERATE -> {
                int max = chargeType.defaultMaxCharges();
                long expire = grantDurationTicks.map(v -> gameTime + Math.max(0, v.getInt(skillCtx))).orElse(-1L);
                ResourceRegistry.generateCharge(caster, chargeType, amt, max, expire, context.eventBus(), context.skill());
            }
            case CONSUME -> {
                // minConsume：现有充能不足最小消耗数时不消耗、不给效果（POE2 战吼/连击消费的最低门槛语义）
                int have = ResourceRegistry.getChargeCount(caster, chargeType);
                int minReq = minConsume.map(v -> v.getInt(skillCtx)).orElse(0);
                if (have < minReq) return;
                // 上下文标签透传：由数据侧 tags 携带（如放置图腾 = ["totem"]），供「充能消耗豁免」求交；
                // 空集时 ChargedState 直接短路，行为与既有完全一致。
                Set<String> contextTags = tags == null || tags.isEmpty() ? Set.of() : Set.copyOf(tags);
                int consumed = ResourceRegistry.consumeCharge(caster, chargeType, amt, context.eventBus(), context.skill(),
                        contextTags);
                // 「消耗充能球时视为额外多消耗 N 个」：归一通道 —— 读 INERT 标记 MarkerStatEffect
                // (Kind.EXTRA_CHARGE_CONSUMED) 存于持有者 STAT_STORE 的幅度之和，球种由条目的
                // conditionTags（power/frenzy/endurance）承载，长杖等限定由条目条件门控，不经属性层。
                // 影响范围严格限于「每球效果」的计数（counted）——不实际多扣球，也不影响下方 bypassCooldown 判定。
                int counted = consumed;
                if (consumed > 0) {
                    counted += (int) StatEngine.markerMagnitude(caster,
                            MarkerStatEffect.Kind.EXTRA_CHARGE_CONSUMED,
                            Set.of(chargeType.getSerializedName()));
                }
                // effectPerCharge：每消耗 1 球提供的伤害倍率增量，乘入本次施放的临时伤害倍率
                // （由 DamageAction 统一消费；SkillContext 每次施放新建，无残留）
                // 注：此处用 counted 而非 consumed，使「视为多消耗」只放大每球效果，不改变真实扣球数。
                if (counted > 0 && effectPerCharge.isPresent() && skillCtx != null) {
                    float per = effectPerCharge.get().getFloat(skillCtx);
                    if (per != 0) skillCtx.multiplyTransientDamage(1f + per * counted);
                }
                // bypassCooldown：战吼语义（POE2 Warcry）——消耗充能成功时本次施放跳过冷却
                // （ON_CAST 动作先于 Skill.run 的 cast.setCooldown 执行，置位后由 setCooldown 消费）
                if (consumed > 0 && bypassCooldown && skillCtx != null) {
                    skillCtx.requestCooldownBypass();
                }
            }
            case REFRESH -> {
                long expire = grantDurationTicks.map(v -> gameTime + Math.max(0, v.getInt(skillCtx))).orElse(-1L);
                ResourceRegistry.refreshCharge(caster, chargeType, expire, context.eventBus(), context.skill());
            }
        }
    }

    public enum ChargeActionMode implements StringRepresentable {
        GENERATE("generate"),
        CONSUME("consume"),
        REFRESH("refresh");

        public static final Codec<ChargeActionMode> CODEC =
                StringRepresentable.fromEnum(ChargeActionMode::values);

        private final String name;
        ChargeActionMode(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }
}
