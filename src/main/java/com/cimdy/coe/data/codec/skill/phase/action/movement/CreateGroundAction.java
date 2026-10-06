package com.cimdy.coe.data.codec.skill.phase.action.movement;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.status.registry.ground.GroundTypes;
import com.cimdy.coe.data.helper.engine.ground.GroundFieldManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

/**
 * 创建地面动作（POE2 Ground 子系统）— 在目标位置投放一片<b>地面</b>。
 *
 * <p>与 {@link SpawnAction} 同构（配置 → 投放），但产出的是<b>地面场</b>——
 * 经 {@code GroundFieldManager} 在 {@code Level} 维度子系统运算（随维度存档、客户端简单渲染），
 * 每 5 tick 对半径内实体按阵营门控刷新施加 {@code stats}（id 经 {@link GroundTypes} 定义解析，
 * 效果直接注入目标 {@code STAT_STORE}，无静态注册、无存在性状态层）。
 *
 * <p><b>Stat 缩放</b>：投放前按地面自带标签（{@link GroundTypes#def} 的 {@code tags}）进入 Stat 作用域，
 * 读取施法者的 {@link AttributeRegistry#AILMENT_DURATION} 作为时长倍率——
 * 于是天赋条目 {@code inc_jagged_ground_duration}（作用域 {@code {jagged_ground, !taken}}）在此真正生效，
 * 与 {@code StatusEngine.archonBuffEffectMultiplier} 的作用域读值范式一致。
 * （幅度倍率随通用模型退役：地面效果由 {@code stats} 直接承载，不再有单一 magnitude。）
 */
public record CreateGroundAction(
        String groundId,
        ExpressionCodec.Value radius,
        ExpressionCodec.Value duration
) implements Action {

    public static final Codec<CreateGroundAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("ground_status").forGetter(CreateGroundAction::groundId),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("radius", ExpressionCodec.of(3.0))
                    .forGetter(CreateGroundAction::radius),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("duration", ExpressionCodec.of(100.0))
                    .forGetter(CreateGroundAction::duration)
    ).apply(i, CreateGroundAction::new));

    @Override public Action.Type<?> getType() { return ActionTypes.CREATE_GROUND; }

    @Override
    public void execute(ActionContext context) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel serverLevel)) return;

        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;
        float baseRadius = Math.max(0.5f, radius.getFloat(skillCtx));
        float baseDuration = Math.max(1f, duration.getFloat(skillCtx));

        // 地面标签作用域：使 inc_<ground>_duration 成为活消费者。
        GroundTypes.GroundDef def = GroundTypes.def(groundId);
        Set<String> scope = def.tags();
        double durationMult = scopedMultiplier(caster, AttributeRegistry.GROUND_DURATION, scope);

        // 改走 GroundFieldManager：地面脱离实体、随维度附件存档，服务端运算、客户端简单渲染。
        Vec3 pos = context.position();
        GroundFieldManager.createField(serverLevel, pos, baseRadius,
                (int) Math.max(1, baseDuration * durationMult),
                caster.getUUID(), def.targeting(), def.color(), def.stats());
    }

    /**
     * 在指定标签作用域下读取施法者属性（倍率型百分比属性，基值 1.0），收口至 {@link ActionContext#stat}。
     */
    private static double scopedMultiplier(LivingEntity caster, Holder<Attribute> attribute, Set<String> scope) {
        return ActionContext.stat(caster, attribute, scope, null);
    }
}
