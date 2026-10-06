package com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.target;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceRarityValue;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.node.StatusNode;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import net.minecraft.network.chat.Component;
import java.util.Locale;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * 事件「<b>触发目标</b>」稀有度读值节点：驱动「当你重晕<b>稀有 / 传奇敌人</b>时 X」一族词缀的真值门控。
 *
 * <p><b>与 {@link PresenceRarityValue} 的语义分工（核心）</b>：
 * {@code PresenceRarityValue} 是<b>持有者 Presence 范围内</b>成员的稀有度<b>聚合</b>判定——回答
 * 「我身边<i>是否存在任一</i>稀有 / 传奇敌人」。那是「身边有稀有敌人时」的字面机制，但对
 * 「重晕稀有敌人时 X」只是<b>近似</b>：重晕的那一只不一定是身边那只稀有敌。
 * 本节点回答的才是语料原义——「<i>本次事件所作用的那一个</i>敌人是否稀有 / 传奇」，
 * 即 {@link com.cimdy.coe.data.codec.support.stat.engine.StatContext#sourceEntity()}（事件交互对方）
 * 的稀有度。二者模型不可互相替代：聚合判定在「身边有稀有敌、却重晕了普通敌」时误命中。
 *
 * <p><b>取值口径</b>：与 {@code BindExpr(SOURCE)} 完全同源——从求值基座实体（
 * {@code scope.root().currentStatContext()}）的<b>交互对方</b>读取。在 {@code caused_ailment}
 * +{@code heavy_stun} 事件（由 {@code AilmentHelper.applyHeavyStun} 经 {@code fireAilment} 发射）里，
 * 该交互对方被显式绑定为<b>被重晕的敌人</b>（见 {@code AilmentHelper.applyHeavyStun} 的
 * {@code StatScopes.enterMerged(caster, …withSourceEntity(victim))} 包裹），故本节点读到的是
 * 「被你重晕的那只敌」的 {@link AttachmentRegistry#RARITY}（怪物生成时由 {@code GenEvent} 写入，
 * 缺省 {@link Rarity#NONE}），而非持有者身边任意敌。
 *
 * <p>交互对方为空（非命中 / 未绑定对方的事件作用域）时返回 0——条目安全地不生效，
 * 不产生「恒真」误判（与 {@code BindExpr(SOURCE)} 的兜底语义一致）。
 *
 * <p>输出 1.0 / 0.0（稀有度落于 {@link #rarities} 即 1），配合条件「求值 &gt;= 1.0」即布尔判据。
 */
public record TargetRarityValue(List<Rarity> rarities) implements ValueExpr {

    public static final Codec<TargetRarityValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Rarity.CODEC.listOf().fieldOf("rarities").forGetter(TargetRarityValue::rarities)
    ).apply(instance, TargetRarityValue::new));

    /** 「重晕稀有或传奇敌人」：本次事件的触发目标为 RARE / UNIQUE 即 1。 */
    public static TargetRarityValue rareOrUniqueTarget() {
        return new TargetRarityValue(List.of(Rarity.RARE, Rarity.UNIQUE));
    }

    @Override
    public double evaluate(ValueScope scope) {
        // 触发目标 = 事件作用域绑定的交互对方（caused_ailment 路径下即被重晕的敌人）。
        if (scope.root() == null) return 0D;
        LivingEntity target = StatusNode.interactionOther(scope);
        if (target == null) return 0D;
        return rarities.contains(target.getData(AttachmentRegistry.RARITY)) ? 1D : 0D;
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rarities.size(); i++) {
            if (i > 0) sb.append('_');
            sb.append(rarities.get(i).getSerializedName());
        }
        return List.of(StatValueKey.of("target_rarity.rarity", sb.toString()));
    }

    @Override
    public List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.target_rarity", describeQuantity(ClientLocale.get()).get(0)));
    }
}
