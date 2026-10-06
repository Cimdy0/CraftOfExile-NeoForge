package com.cimdy.coe.data.codec.support.stat.effect.damage;

import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.target.NearbyEnemyCountValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionAnchor;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionChains;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionScope;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.event.TriggerContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 电击回响（Shocking Hit → also Shock nearby）：造成电击（{@code caused_ailment + shock} 火，owner=攻击者）
 * 时，按条目数值（0~1 概率）将 {@link StatusDefs#SHOCK} 施加到被电击敌人周围 {@link #RADIUS} 格内的其他敌方实体。
 *
 * <p>R3 安全：完全复用既有链路——
 * <ul>
 *     <li>触发源 = {@code StatusEngine.fire} 经 {@code AilmentHelper.publishAilment} 真实发射的
 *         {@code caused_ailment}+{@code shock} 火（owner=攻击者，other=被电击敌人），与重晕/钉刺破甲同通道，非死线；</li>
 *     <li>施加 = 中央 {@link StatusEngine#apply}（与 MARK/MAIM/PIN 同路径），SHOCK 为既有 CONTROL 状态，真实生效；</li>
 *     <li>邻域目标获取 = {@link ConditionChains#aroundAnchor}（锚点 = 被电击敌人 → 半径内敌方），
 *         不再手写 {@code getEntitiesOfClass} + 阵营/自排除谓词；</li>
 *     <li>无新属性原语、无新发射点、无新状态。</li>
 * </ul>
 *
 * <p>回响防自激：本效果在 {@code onTrigger} 内施加 SHOCK 会同步再触发 {@code caused_ailment+shock} 火，
 * 但 {@link StatEngine#fire} 自带「自激守卫」({@code firing.contains(entry)} 跳过) 与 {@code MAX_FIRE_DEPTH} 限深，
 * 嵌套火对本条目直接跳过，故仅对<b>原始被电击敌人</b>的邻域形成单环电击、不会无限扩散。</p>
 */
public final class ShockEchoStatEffect extends AbstractStatEffect {

    /** 回响半径（方块单位）。与 {@code NearbyEnemyCountValue.DEFAULT_RADIUS}=2.0 同尺度，约等于语料「a metre radius」。 */
    private static final double RADIUS = 2.0;
    /** 回响施加的 SHOCK 时长（tick），复用 {@link StatusDefs#SHOCK} 自身默认 80 tick（4s）。 */
    private static final int SHOCK_DURATION = 80;
    /** 回响施加的状态类型（默认 {@link StatusDefs#SHOCK}，作为参数保留，去除硬编码锁）。 */
    private final StatusType status = StatusDefs.SHOCK;

    public ShockEchoStatEffect(GateExpr tags) {
        super(tags);
    }

    public static final MapCodec<ShockEchoStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, ShockEchoStatEffect::new));

    @Override
    public String type() {
        return "shock_echo";
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx, @Nullable TriggerContext tc) {
        LivingEntity owner = evalCtx.owner();
        if (owner == null || owner.isDeadOrDying()) return;
        // 概率掷骰：条目数值 = 0~1 触发概率（与 AggravateBleeding / CreateGroundOnAilment 同约定）。
        double chance = entry.amount().getValue();
        if (chance <= 0) return;
        if (owner.getRandom().nextDouble() >= chance) return;

        // 中心 = 被电击的敌人（造成型事件的 other/triggerTarget），对其邻域的其他敌人补加 SHOCK。
        LivingEntity center = evalCtx.triggerTarget();
        if (center == null || center.isDeadOrDying()) return;

        Camp ownerCamp = owner.getData(AttachmentRegistry.CAMP);
        if (ownerCamp == Camp.NONE) return;

        // 多跳目标获取：锚点 TRIGGER（被电击的敌人）→ 以它为锚点、半径 RADIUS 内的敌方集合。
        // 原手写的 AABB 膨胀遍历 + 阵营过滤 + 排除自身/中心，现由 Condition 模型表达：
        // chain{ inner: anchor(trigger), then: radius(center=current, camp=enemy) }，
        // 解析结果即目标集（空集 = 不满足），本类只负责向其中每个实体投递 SHOCK。
        ConditionScope condScope = ConditionScope.of(owner, center, center, scope.eventName(),
                scope.hitContext(), center.position(), scope);
        for (LivingEntity e : ConditionChains.aroundAnchor(ConditionAnchor.TRIGGER, RADIUS,
                PresenceCountValue.Relation.ENEMY).resolve(condScope)) {
            if (e.isDeadOrDying()) continue;
            StatusEngine.apply(e, this.status, owner, 1.0, SHOCK_DURATION, 1);
        }
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
        return List.of(Component.translatable("stat_effect.shock_echo",
                Component.literal(Math.round(entry.amount().getValue() * 100) + "%"),
                StatValueKey.of("damage", "shock"),
                Component.literal(RADIUS + "m"),
                StatValueKey.of("status", "shocked")));
    }
}
