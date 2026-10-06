package com.cimdy.coe.data.codec.support.stat.effect.modifier;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision.Pierce;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.DeliveryMod;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 投送修饰效果（Delivery Modifier）：装备/天赋/珠宝词缀对技能<b>投送行为</b>的修饰——
 * 额外投射物 / 穿透 / 必定穿透 / 链 / 叉 / 弹幕 / 范围倍率 / 额外目标。
 *
 * <p><b>职责边界（用户原则）</b>：本效果是 <b>fold 型查询</b>（EffectKind.DELIVERY），
 * 只把「投送参数 → 数值」查成数据，交 {@code SupportProfile}（技能+辅助+装备词缀+天赋+珠宝
 * 同形状合并）在投送执行时应用——<b>不写任何副作用</b>，不直接改投射物/伤害。
 * POE2 实证：额外投射物可来自装备（The Unborn Lich「25% chance 2 additional projectiles」）、
 * 天赋（Surpassing chance to fire an additional Arrow）、穿透（Projectiles Pierce enemies with
 * Fully Broken Armor）——全部经此统一通道表达。
 *
 * <p><b>参数表</b>（{@code params} Map，数值用 {@link ExpressionCodec.Value} 支持表达式）：
 * <ul>
 *   <li>{@code additional_projectiles}（int，额外投射物数）</li>
 *   <li>{@code pierce_count}（int，穿透目标数）</li>
 *   <li>{@code pierce_all}（bool，必定穿透全部目标）</li>
 *   <li>{@code pierce_less_per_enemy}（double，穿透每敌减伤比例，Pierce I/III 20%/30% less）</li>
 *   <li>{@code chain_count}（int，额外链数）</li>
 *   <li>{@code fork_count}（int，分叉数）</li>
 *   <li>{@code barrage_count}（int，弹幕齐射次数）</li>
 *   <li>{@code radius_multiplier}（double，范围倍率）</li>
 *   <li>{@code additional_targets}（int，额外目标数）</li>
 * </ul>
 */
public class DeliveryStatEffect extends AbstractStatEffect {

    /** 投送类型 id（projectile / area / beam …，与 DeliveryMod.delivery 对齐）。 */
    private final String delivery;
    /** 投送参数表（参数名 → 数值表达式）。 */
    private final Map<String, ExpressionCodec.Value> params;

    public DeliveryStatEffect(String delivery, Map<String, ExpressionCodec.Value> params, GateExpr tags) {
        super(tags);
        this.delivery = delivery;
        this.params = Map.copyOf(params);
    }
    /** 无事件标签门控（{@link GateExpr#ALWAYS}）便捷构造。 */
    public DeliveryStatEffect(String delivery, Map<String, ExpressionCodec.Value> params) {
        this(delivery, params, GateExpr.ALWAYS);
    }

    @Override
    public String type() {
        return "delivery";
    }

    @Override
    public EffectKind indexKind() {
        return EffectKind.DELIVERY;
    }

    public String delivery() {
        return delivery;
    }

    public Map<String, ExpressionCodec.Value> params() {
        return params;
    }

    /** 读取参数：存在则按持有者表达式上下文求值返回，否则返回默认值。 */
    public double param(String name, ExpressionContext ctx, double def) {
        ExpressionCodec.Value v = params.get(name);
        if (v == null) return def;
        return v.getValue(ctx);
    }

    public static final MapCodec<DeliveryStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("delivery").forGetter(DeliveryStatEffect::delivery),
            Codec.unboundedMap(Codec.STRING, ExpressionCodec.VALUE_CODEC)
                    .optionalFieldOf("params", Map.of()).forGetter(DeliveryStatEffect::params),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, DeliveryStatEffect::new));

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
        return List.of(Component.translatable("stat_effect.delivery",
                StatValueKey.of("delivery", delivery),
                Component.literal(params.keySet().toString())));
    }

    /** 便捷构造：指定投送类型 + 参数表 + 作用域标签，产出一条完整条目（amount 恒 1，参数即数值）。 */
    public static StatRead entry(String delivery, Map<String, ExpressionCodec.Value> params, GateExpr tags) {
        return StatEntry.of(ExpressionCodec.of(1.0),
                List.of(),
                new DeliveryStatEffect(delivery, params, tags));
    }

    /** 便捷构造：单参数版本（如「必定穿透」= delivery=projectile, pierce_all=true）。 */
    public static StatRead entry(String delivery, String param, ExpressionCodec.Value value, GateExpr tags) {
        Map<String, ExpressionCodec.Value> params = new HashMap<>();
        params.put(param, value);
        return entry(delivery, params, tags);
    }
}
