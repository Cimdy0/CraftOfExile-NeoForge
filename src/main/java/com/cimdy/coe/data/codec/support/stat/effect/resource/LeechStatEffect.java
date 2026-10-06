package com.cimdy.coe.data.codec.support.stat.effect.resource;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.event.TriggerContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 触发亚型：偷取（Leech）——「击中时按造成伤害回复生命/魔力/能量」。
 * 复用从 {@code DamageHelper} 抽离的 {@link DamageHelper#applyLeechEffects} 原语（完整偷取模型：
 * 总量由伤害决定、速率决定计时），本效果只负责<b>在命中事件上「触发」偷取</b>，
 * 不再在伤害管线里硬编码副作用（管道返回值现在只修饰伤害）。
 *
 * <p>唯一身份字段：事件标签（默认 {@code caused_hit}）。偷取比率改由条目 {@code amount}（经 ExpressionCodec
 * 表达式系统，如 {@code attribute:coe:leech} 或 {@code $value}）驱动，在本效果触发时经
 * {@link DamageHelper#applyLeechEffects} 的 {资源, leeching} / {资源, leeching, physical} 作用域求值
 * ——与原「硬编码读取持有者 LEECH 属性」行为一致（仅变更数值来源，未变更数值本身）。
 *
 * <p><b>符号语义</b>：条目 {@code amount} 即偷取比率（占造成伤害比例），应为正；
 * 表达式 {@code attribute:coe:leech} 在作用域内解析为对应资源的 LEECH 属性总值。
 *
 * <p>守卫语义对齐原管线 stageHealth：被完全招架/格挡（最终承伤为 0）时不偷取。
 */
public class LeechStatEffect extends AbstractStatEffect {

    public LeechStatEffect(GateExpr tags) {
        super(tags);
    }

    public static final MapCodec<LeechStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, LeechStatEffect::new));

    /**
     * 便捷构造：产出一条触发型偷取条目（作用于自身，标签限定触发事件，如 {@code caused_hit}）。
     * <b>amount 即偷取比率</b>（占造成伤害比例），可为字面量或表达式（如 {@code attribute:coe:leech} /
     * {@code $value}），由 {@link #onTrigger} → {@link DamageHelper#applyLeechEffects} 在
     * {资源, leeching} 作用域求值驱动 health/mana/energy 各资源结算。
     */
    public static StatRead entry(double amount, Set<String> tags) {
        return StatEntry.of(ExpressionCodec.of(amount), List.of(), new LeechStatEffect(GateExpr.of(tags)));
    }

    @Override
    public String type() {
        return "leech";
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        if (tc == null || tc.damages() == null) return;
        // 对齐原 stageHealth 守卫：最终承伤为 0（被招架/格挡）时不偷取
        if (tc.takenPostBlock() <= 0) return;
        // 偷取属性与<b>受益人同为命中方</b>（POE2：偷取回复给造成伤害者）——
        // 修正前误把 triggerTarget（承伤方）当受益人写入 LeechValue，导致「击中偷取生命」反向给敌人回血。
        DamageHelper.applyLeechEffects(entry, tc.damages(), evalCtx.owner(), evalCtx.owner(), tc.skill());
    }

    @Override
    public List<Component> describe(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    @Override
    public List<Component> describeFragment(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    /**
     * 本地化效果片段：硬编码整句 {@code "leech life/mana/energy from dealt damage"} → 模板 {@code stat_effect.leech}。
     */
    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        return List.of(Component.translatable("stat_effect.leech"));
    }
}
