package com.cimdy.coe.data.codec.skill.phase.action.combo;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Optional;

/**
 * 连击动作 — 构建或消耗连击点数。
 */
public record ComboAction(
        ComboMode mode,
        String comboId,
        Optional<ExpressionCodec.Value> maxStacks,
        ExpressionCodec.Value consumeAmount,
        Optional<ExpressionCodec.Value> effectMultiplierPerStack,
        Optional<List<Action>> onMaxStackActions,
        Optional<List<Action>> onConsumeActions,
        Optional<ExpressionCodec.Value> comboResetDelayTicks
) implements Action {
    public static final Codec<ComboAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            ComboMode.CODEC.fieldOf("mode").forGetter(ComboAction::mode),
            Codec.STRING.fieldOf("combo_id").forGetter(ComboAction::comboId),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("max_stacks").forGetter(ComboAction::maxStacks),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("consume_amount", ExpressionCodec.of(0.0))
                    .forGetter(ComboAction::consumeAmount),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("effect_multiplier_per_stack")
                    .forGetter(ComboAction::effectMultiplierPerStack),
            Action.CODEC.listOf().optionalFieldOf("on_max_stack_actions")
                    .forGetter(ComboAction::onMaxStackActions),
            Action.CODEC.listOf().optionalFieldOf("on_consume_actions")
                    .forGetter(ComboAction::onConsumeActions),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("combo_reset_delay_ticks")
                    .forGetter(ComboAction::comboResetDelayTicks)
    ).apply(i, ComboAction::new));

    @Override public Action.Type<?> getType() { return ActionTypes.COMBO; }

    @Override
    public void execute(ActionContext context) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;
        int max = maxStacks.map(v -> Math.max(1, v.getInt(skillCtx))).orElse(3);
        int resetDelay = comboResetDelayTicks.map(v -> v.getInt(skillCtx)).orElse(-1);
        long gameTime = caster.level().getGameTime();
        // 将当前连击计数值缓存到 SkillContext（供 ResourceType.COMBO 读取和消耗）
        if (skillCtx != null) {
            int cur = ResourceRegistry.getCombo(caster, comboId).count();
            skillCtx.combo(comboId, cur);
        }
        switch (mode) {
            case BUILD -> ResourceRegistry.buildCombo(caster, comboId, max, resetDelay,
                    onMaxStackActions.orElse(List.of()), context, context.eventBus(), context.skill(), gameTime);
            case CONSUME -> {
                int consumed = ResourceRegistry.consumeCombo(caster, comboId,
                        (int) Math.max(0, consumeAmount.getValue(skillCtx)),
                        onConsumeActions.orElse(List.of()), context, context.eventBus(), context.skill());
                // effectMultiplierPerStack：每消耗 1 层提供的伤害倍率增量，乘入本次施放的临时伤害倍率
                // （连击终结技按消耗层数增益；由 DamageAction 统一消费）
                if (consumed > 0 && effectMultiplierPerStack.isPresent() && skillCtx != null) {
                    float per = effectMultiplierPerStack.get().getFloat(skillCtx);
                    if (per != 0) skillCtx.multiplyTransientDamage(1f + per * consumed);
                }
                // 同步 SkillContext 缓存的连击计数（消费后的残余值）
                if (skillCtx != null) {
                    int remaining = ResourceRegistry.getCombo(caster, comboId).count();
                    skillCtx.combo(comboId, remaining);
                }
            }
            case CHECK -> {
                // 仅做存在性校验：到顶状态由 BUILD 阶段维护，事件亦在 BUILD 时发布
                ResourceRegistry.isComboMax(caster, comboId, max);
            }
        }
    }

    public enum ComboMode implements StringRepresentable {
        BUILD("build"),
        CONSUME("consume"),
        CHECK("check");

        public static final Codec<ComboMode> CODEC =
                StringRepresentable.fromEnum(ComboMode::values);

        private final String name;
        ComboMode(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }
}
