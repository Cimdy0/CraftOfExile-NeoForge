package com.cimdy.coe.data.codec.support.stat.effect.convert;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.grant.GrantOnEventStatEffect;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionAnchor;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionScope;
import com.cimdy.coe.data.codec.support.stat.condition.PresenceCondition;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import java.util.Optional;

import com.cimdy.coe.data.codec.skill.phase.action.charge.ChargeType;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;

import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.TriggerContext;
import com.cimdy.coe.event.EventName;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;
import java.util.Set;

/**
 * 触发亚型：充能获得事件改写（Charge Convert On Event）——「获得充能时改为 / 转换 / 盟友同获」。
 * <p>与 {@link GrantOnEventStatEffect} 同构触发通道（{@link GateExpr#matches} / {@link StatEngine#conditionsHold} + {@code StatEngine.fire}），
 * 但语义是<b>改写</b>而非单纯授予：
 * <ul>
 *   <li>{@link ConvertMode#CONVERT}：获得 {@code from} 球时扣除该球（本次获得量，缺省回退当前存量），
 *       改授 {@code to} 球（受目标上限）——POE2 resonance「Gain Power Charges instead of Frenzy Charges」；</li>
 *   <li>{@link ConvertMode#TO_MAX}：获得 {@code to} 球时补至满层——POE2 gain_max_*「you instead gain max
 *       Frenzy/Power Charges」；</li>
 *   <li>{@link ConvertMode#ALLY_GRANT}：获得任意充能时，Presence 内盟友同获 1 层同类型球——POE2 conduit
 *       「Allies in your Presence gain the same」。</li>
 * </ul>
 * 条目 {@code amount} = 触发概率（0~1）。本次获得量经 {@link Event#lastValue} 读取（事件 record 时回传）。
 * 扣源经 {@link ResourceRegistry#consumeCharge} 公开通道（含事件/总线一致性），授予经 {@link ResourceRegistry#generateCharge}
 * 中央入口；级联 fire（gain_<to> 再触发）由 {@code StatEngine.fire} 的 FIRE_DEPTH 深度闸兜底。
 */
public final class ChargeConvertStatEffect extends AbstractStatEffect {

    /** 改写模式。 */
    public enum ConvertMode implements StringRepresentable {
        CONVERT,
        TO_MAX,
        ALLY_GRANT;

        public static final Codec<ConvertMode> CODEC = StringRepresentable.fromEnum(ConvertMode::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    private final ConvertMode mode;
    private final String from;   // CONVERT: 源 charge type；其余模式留空
    private final String to;     // CONVERT/TO_MAX: 目标 charge type；ALLY_GRANT 留空（按事件类型）

    public ChargeConvertStatEffect(ConvertMode mode, String from, String to, GateExpr tags) {
        super(tags);
        this.mode = mode;
        this.from = from;
        this.to = to;
    }

    public static final MapCodec<ChargeConvertStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ConvertMode.CODEC.fieldOf("mode").forGetter(ChargeConvertStatEffect::mode),
            Codec.STRING.optionalFieldOf("from", "").forGetter(ChargeConvertStatEffect::from),
            Codec.STRING.optionalFieldOf("to", "").forGetter(ChargeConvertStatEffect::to),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, ChargeConvertStatEffect::new));

    public ConvertMode mode() {
        return mode;
    }

    public String from() {
        return from;
    }

    public String to() {
        return to;
    }

    @Override
    public String type() {
        return "charge_convert";
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        double chance = entry.amount().getValue();
        if (chance <= 0) return;
        if (evalCtx.owner().getRandom().nextDouble() >= chance) return;
        LivingEntity owner = evalCtx.owner();
        switch (mode) {
            case CONVERT -> convert(owner);
            case TO_MAX -> toMax(owner);
            case ALLY_GRANT -> allyGrant(owner, scope);
        }
    }

    /** CONVERT：扣源球（本次获得量，缺省回退存量）→ 授目标球（受上限）。 */
    private void convert(LivingEntity owner) {
        ChargeType fromT = chargeType(from);
        ChargeType toT = chargeType(to);
        int n = Event.lastValue(owner, EventName.gainCharge(from));
        if (n <= 0) n = StatusEngine.stacks(owner, fromT.statusType());
        if (n <= 0) return;
        int real = ResourceRegistry.consumeCharge(owner, fromT, n, null, null, Set.of());
        if (real <= 0) return;
        ResourceRegistry.generateCharge(owner, toT, real, 3, -1, null, null);
    }

    /** TO_MAX：把 {@code to} 球补至满层（上限 - 当前）。 */
    private void toMax(LivingEntity owner) {
        ChargeType t = chargeType(to);
        int cur = StatusEngine.stacks(owner, t.statusType());
        int max = ResourceRegistry.maxCharges(owner, t);
        int need = Math.max(0, max - cur);
        if (need <= 0) return;
        ResourceRegistry.generateCharge(owner, t, need, 3, -1, null, null);
    }

    /** ALLY_GRANT：Presence 内盟友获得同类型 1 层（类型从事件标签 {@code gain_<type>} 推断）。 */
    private void allyGrant(LivingEntity owner, StatContext scope) {
        ChargeType t = chargeTypeFromEvent(scope);
        if (t == null) return;
        ConditionScope cs = ConditionScope.of(owner);
        for (LivingEntity ally : PresenceCondition.of(ConditionAnchor.SELF, PresenceCountValue.Relation.ALLY).resolve(cs)) {
            ResourceRegistry.generateCharge(ally, t, 1, 3, -1, null, null);
        }
    }

    /** 从作用域标签提取 {@code gain_<type>} 前缀（排除通用 {@code gain_charge}）。 */
    private static ChargeType chargeTypeFromEvent(StatContext scope) {
        for (String tag : scope.tags()) {
            if (tag.startsWith("gain_") && !tag.equals("gain_charge")) {
                return chargeTypeOf(tag.substring("gain_".length()));
            }
        }
        return null;
    }

    private ChargeType chargeType(String name) {
        return chargeTypeOf(name);
    }

    private static ChargeType chargeTypeOf(String name) {
        for (ChargeType c : ChargeType.values()) {
            if (c.getSerializedName().equals(name)) return c;
        }
        throw new IllegalStateException("Unknown charge type: " + name);
    }

    @Override
    public List<Component> describe(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    @Override
    public List<Component> describeFragment(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        return List.of(Component.translatable("stat_effect.charge_convert",
                StatValueKey.of("charge_mode", mode.name().toLowerCase(Locale.ROOT)),
                StatValueKey.of("charge", from),
                StatValueKey.of("charge", to),
                Component.literal(String.valueOf(entry.amount()))));
    }
}
