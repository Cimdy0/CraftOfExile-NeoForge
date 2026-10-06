package com.cimdy.coe.data.codec.support.stat.effect.grant;

import com.cimdy.coe.data.codec.skill.phase.action.movement.CreateGroundAction;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.event.combat.GameEvent;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.status.registry.ground.GroundTypes;
import com.cimdy.coe.data.helper.engine.ground.GroundFieldManager;
import com.cimdy.coe.event.TriggerContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 触发亚型：造成异常时于承受方位置投放地面（Create Ground on Ailment）。
 * <p>与 {@link GrantOnEventStatEffect} 同通道（{@code StatEngine.fire} → {@code onTrigger}），
 * 但命中后不施加状态，而是经 {@code GroundFieldManager} 创建一片地面场（随维度附件存档、
 * 服务端运算、客户端简单渲染），复用 {@link CreateGroundAction} 的投放逻辑：
 * 该地面场每 5 tick 对半径内实体按阵营门控刷新施加 {@code stats}（id 经 {@link GroundTypes} 定义解析，
 * 效果直接注入目标 {@code STAT_STORE}，无存在性状态层）。
 *
 * <p><b>事件钩子</b>：{@code AilmentHelper.fireAilment} 经 {@code GameEvent.ailmentCaused}
 * 以作用域标签 {@code {caused_ailment, <ailmentId>}} 触发本效果；
 * 数据侧以 {@code tags = [caused_ailment, shock]} / {@code [caused_ailment, freeze]} 命中
 * 「造成感电/冰冻」事件（{@code evalCtx.owner()} = 造成者，{@code evalCtx.triggerTarget()} = 承受者=落点）。
 *
 * <p><b>数值语义</b>：条目 {@code amount} = 触发概率（0~1，如 0.25 = 25%；必定则 1.0）。
 * 云半径/时长沿用 {@code CreateGroundAction} 默认呈现参数（4.0 / 100tick），
 * 并继承地面标签作用域下的 {@code AILMENT_DURATION} 缩放（使未来 {@code inc_shocked_ground_duration} 等条目成为活消费者）。
 * 具体地面出现的条件（感电/冰冻几率为多少才落地面）由语料数据化于条目 amount，不硬编码在效果类。
 */
public class CreateGroundOnAilmentEffect extends AbstractStatEffect {

    private final String groundId;

    public CreateGroundOnAilmentEffect(String groundId, GateExpr tags) {
        super(tags);
        this.groundId = groundId;
    }

    public static final MapCodec<CreateGroundOnAilmentEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("ground_status").forGetter(CreateGroundOnAilmentEffect::groundId),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, CreateGroundOnAilmentEffect::new));

    public String groundId() {
        return groundId;
    }

    @Override
    public String type() {
        return "create_ground_on_ailment";
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        // 概率判定：entry.amount = 0~1（常量）
        double chance = entry.amount().getValue();
        if (chance <= 0) return;
        LivingEntity caster = evalCtx.owner();
        if (caster == null) return;
        if (caster.getRandom().nextDouble() >= chance) return;
        LivingEntity target = evalCtx.triggerTarget();
        if (target == null) return;
        if (!(target.level() instanceof ServerLevel serverLevel)) return;

        Vec3 pos = target.position();
        float baseRadius = 4.0f;
        float baseDuration = 100f;

        // 地面标签作用域：与 CreateGroundAction 对称，使 inc_<ground>_duration 成为活消费者。
        GroundTypes.GroundDef def = GroundTypes.def(groundId);
        Set<String> gscope = def.tags();
        double durationMult = scopedMultiplier(caster, AttributeRegistry.GROUND_DURATION, gscope);

        // 改走 GroundFieldManager：地面脱离实体、随维度附件存档，服务端运算、客户端简单渲染。
        GroundFieldManager.createField(serverLevel, pos, baseRadius,
                (int) Math.max(1, baseDuration * durationMult),
                caster.getUUID(), def.targeting(), def.color(), def.stats());
    }

    /**
     * 在指定标签作用域下读取施法者属性（倍率型百分比属性，基值 1.0）。
     * 与 {@code StatusEngine.archonBuffEffectMultiplier} 同范式：enter → 读值 → finally close。
     */
    private static double scopedMultiplier(LivingEntity caster, Holder<Attribute> attribute, Set<String> scope) {
        if (scope.isEmpty()) return caster.getAttributeValue(attribute);
        AutoCloseable s = StatScopes.enter(caster, StatContext.of(scope, null));
        try {
            return caster.getAttributeValue(attribute);
        } finally {
            try { s.close(); } catch (Exception ignored) {}
        }
    }

    @Override
    public List<Component> describe(StatEntry entry) {
        return List.of(Component.literal(type() + " -> " + groundId + " @" + entry.amount()));
    }
}
