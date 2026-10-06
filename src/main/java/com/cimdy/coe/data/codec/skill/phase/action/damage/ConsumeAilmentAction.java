package com.cimdy.coe.data.codec.skill.phase.action.damage;

import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.data.codec.support.status.runtime.TransferStore;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Set;

/**
 * 消费异常动作 —— 技能动作「消费目标/自身异常」的通用入口：把指定 {@link StatusType} 从
 * 命中目标（或施法者自身）消费掉，并将其数值经 P2-3 转存通道（AilmentTransfer）记账到施法者，
 * 再<b>取回并清除</b>该记账（一次性消费语义），最后执行 {@code on_consumed} 回报动作链。
 *
 * <p>POE2 实证（语料库 {@code poe2_assets/authoritative/all_skills.json}）：本动作对应 POE2 的
 * 「Consume X」原语族——
 * <ul>
 *   <li>{@code blood_hunt}：Hitting an enemy that has suffered Blood Loss will cause a blood explosion
 *       and also <b>Consume Bleeding</b>；Enemies lose Life equal to <b>25% of Consumed Blood Loss</b>；</li>
 *   <li>{@code elemental_sundering}：a pulse that <b>Consumes Freeze, Shock and Ignite</b> … each Ailment
 *       Consumed causes an explosion of the corresponding elemental damage type；</li>
 *   <li>同族：{@code shockchain_arrow}（Consumes Shock）、{@code primal_strikes}（Consume Shock）、
 *       {@code skeletal_brute}（Consumes Freeze）、{@code fragmentation_rounds}（Consume the Freeze）、
 *       {@code acidic_concoction}（Consumes Poison on Hit）。</li>
 * </ul>
 * <b>更正</b>：{@code iron_ward}（储存被护甲减免的物理伤害 → 尖刺新星）与 {@code plague_bearer}
 * （储存自身造成的中毒伤害 → 释放）在 POE2 中<b>与「消耗异常」无关</b>，并非本通道的消费方
 * （旧注释归属有误，已按语料库更正）。
 *
 * <p>闭合链路：{@link SkillContext#consumeAilmentOn}（生产：{@code StatusEngine.consumeAilment} 写入
 * TransferStore）→ {@link SkillContext#consumeTransferredAilment}（消费：取回并清除记账）→
 * 被消费强度注入参数槽 {@code $consumed_<id>} → 执行 {@code on_consumed} 回报动作。
 *
 * @param ailment    要消费的异常 id（{@link StatusType#byId}，取 path 段）；未登记时静默跳过。
 * @param onTarget   true = 消费命中目标身上的异常（如从敌人身上剥取）；false = 消费施法者自身异常。
 * @param onConsumed 消费成功时执行的回报动作链（POE2「Consume X → payoff」）；为空则只消费、不产生回报。
 */
public record ConsumeAilmentAction(Identifier ailment, boolean onTarget, List<Action> onConsumed) implements Action {

    public static final Codec<ConsumeAilmentAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("ailment").forGetter(ConsumeAilmentAction::ailment),
            Codec.BOOL.optionalFieldOf("on_target", false).forGetter(ConsumeAilmentAction::onTarget),
            Action.CODEC.listOf().optionalFieldOf("on_consumed", List.of())
                    .forGetter(ConsumeAilmentAction::onConsumed)
    ).apply(i, ConsumeAilmentAction::new));

    @Override
    public Action.Type<?> getType() {
        return ActionTypes.CONSUME_AILMENT;
    }

    @Override
    public void execute(ActionContext context) {
        StatusType type = StatusType.byId(ailment.getPath()).orElse(null);
        if (type == null) return;
        if (!(context.caster() instanceof LivingEntity caster)) return;
        LivingEntity holder = onTarget
                ? (context.target().orElse(null) instanceof LivingEntity l ? l : null)
                : caster;
        if (holder == null) return;
        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;
        if (skillCtx == null) return;
        // ── 生产侧：触发消费 + 记账到施法者 TransferStore（目标未持有该异常则无消费、不放回报）──
        if (!skillCtx.consumeAilmentOn(holder, type)) return;
        // ── 消费侧：取回并清除该记账记录（一次性语义，避免跨施放残留）──
        // 这是 P2-3 转存通道此前缺失的读方：写方（StatusEngine.consumeAilment）早已就位，
        // 本处真正「取走」记账数值，通道方才闭合。
        TransferStore.TransferRecord rec = skillCtx.consumeTransferredAilment(type);
        if (rec == null) return;
        // 被消费强度注入本次施放参数槽 $consumed_<id>，供回报动作按「被消费量」缩放：
        // POE2 blood_hunt「Enemies lose Life equal to 25% of Consumed Blood Loss」即以此结算。
        skillCtx.putVariable("consumed_" + type.id, ExpressionCodec.of((double) rec.intensity()));
        for (Action a : onConsumed) a.execute(context);
    }

    /** 透传异常的登记标签，使带标签的天赋加成在消费判定内即时生效。 */
    @Override
    public Set<String> statTags() {
        return StatusType.tagsOf(ailment.getPath());
    }
}
