package com.cimdy.coe.data.codec.support.stat.effect.grant;

import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.skill.context.SkillDamageSource;
import com.cimdy.coe.event.TriggerContext;
import com.cimdy.coe.registry.DamageTypeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 合成冰霜伤害冰缓（PERIODIC 复合条目的子效果，非直接索引）：heart_of_ice 等
 * 「视作造成 X 冰霜伤害」类机制——不对目标造成任何真实生命/资源丢失，也不触发命中/暴击/伤害事件，
 * 仅借伤害管线触发冰缓（applyColdAilment）。
 *
 * <p>实现：每节拍经 {@code entry.forEachTarget} 解析出每个目标，构造一条<b>合成冰霜</b>
 * {@link SkillDamageSource}（cold 伤害类型 + {@code statTriggered} + {@code synthetic} 标记），
 * 对目标调用 {@code hurt} 走完整结算：
 * <ul>
 *   <li>{@code stageAilmentAndCrit}（REDUCTION 之前）跑 {@code applyColdAilment} → 按冰霜伤害值推导冰缓百分比；</li>
 *   <li>{@code stageTrigger} 见 {@code statTriggered} 跳过整段 fire（不触发命中/暴击/伤害事件）；</li>
 *   <li>{@code stageReduction} 见 {@code synthetic} 把 newDamage 归零 → 不扣生命/护盾/蓝量。</li>
 * </ul>
 * 冰霜伤害量由本效果的 {@code coldDamage} 表达式在<b>持有者（施法者）</b>上下文求值（随宝石等级/属性缩放）。
 */
public final class SyntheticChillStatEffect extends AbstractStatEffect {

    public static final MapCodec<SyntheticChillStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("cold_damage").forGetter(SyntheticChillStatEffect::coldDamage),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, SyntheticChillStatEffect::new));

    private final ExpressionCodec.Value coldDamage;
    /** 合成伤害的伤害类型（默认 {@link DamageTypeRegistry#COLD}，作为参数保留，去除硬编码锁）。 */
    private final ResourceKey<DamageType> damageType = DamageTypeRegistry.COLD;

    public SyntheticChillStatEffect(ExpressionCodec.Value coldDamage, GateExpr tags) {
        super(tags);
        this.coldDamage = coldDamage;
    }

    public ExpressionCodec.Value coldDamage() {
        return coldDamage;
    }

    @Override
    public String type() {
        return "synthetic_chill";
    }

    @Override
    public boolean isTrigger() {
        return false;
    }

    @Override
    public StatEffect.EffectKind indexKind() {
        return StatEffect.EffectKind.FOLD;
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        LivingEntity owner = evalCtx.owner();
        if (owner.level().isClientSide()) return;
        double cold = coldDamage.getValue(ExpressionContext.get(owner));
        if (cold <= 0) return;
        ServerLevel level = (ServerLevel) owner.level();
        Holder<DamageType> coldType = level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(this.damageType);
        entry.forEachTarget(evalCtx, scope, target -> {
            if (target == owner || !target.isAlive()) return;
            // 合成冰霜伤害源：cold 类型 + statTriggered（不二次触发 stat）+ synthetic（不造成真实伤害）。
            SkillDamageSource src = new SkillDamageSource(coldType, owner, owner, null,
                    null, false, null, null, true, true);
            target.hurt(src, (float) cold);
        });
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
        return List.of(Component.literal("Chills as though dealing " + coldDamage + " Cold damage"));
    }
}
