package com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.damage;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.CompareValue;
import com.cimdy.coe.data.codec.skill.context.DamageContext;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.damage.DamageEventNode;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.event.combat.DamageEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * 伤害读值节点（合并组 F：原 {@code DamageValue} + {@code DamageTakenValue}）。
 * 由 {@code source} 区分数据生命周期：
 * <ul>
 *   <li>{@code CONTEXT}（默认，中缀 {@code damage.<mode>}）—— 读 {@link ValueScope#root()} 的伤害子站
 *       （{@link DamageContext}，由技能阶段在判定前写入），仅本技能阶段 on_tick 可用；</li>
 *   <li>{@code EVENT}（中缀 {@code damage_taken.<mode>}）—— 读 {@link Event.State} 的最近一次承受伤害
 *       （跨技能、跨作用域持久于实体单例，经 {@link ValueScope#event()} 候选归一解析）；
 *       同源另有 {@code damage_caused.<mode>}（最近一次造成量）、{@code heal.<mode>}（最近一次回复量）、
 *       {@code spend.<mode>}（最近一次消耗量）四组 mode 集。</li>
 * </ul>
 * 统一输出 double（布尔 → 1/0）；判断（比较 / 占比）完全在 math 侧进行。
 * <p>
 * math 变量形式：{@code damage.<mode>}（如 {@code damage.crit == 1}、{@code damage.fire / damage.total >= 0.5}）、
 * {@code damage_taken.<mode>}（{@code actual} / {@code overflow} / {@code resource:<r>}）、
 * {@code damage_caused.<mode>}（同 taken mode 集，读攻击者最近一次造成量）、
 * {@code heal.<mode>}（{@code requested} / {@code actual} / {@code overflow} / {@code resource:<r>}）、
 * {@code spend.<mode>}（{@code requested} / {@code actual} / {@code missing} / {@code resource:<r>}）。
 * 对应数据源无记录时一律返回 0。
 */
public record DamageValue(Source source, String mode) implements ValueExpr {

    public DamageValue(String mode) {
        this(Source.CONTEXT, mode);
    }

    public enum Source implements StringRepresentable {
        CONTEXT("context"), EVENT("event");

        public static final Codec<Source> CODEC = StringRepresentable.fromEnum(Source::values);

        private final String name;

        Source(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final Codec<DamageValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Source.CODEC.optionalFieldOf("source", Source.CONTEXT).forGetter(DamageValue::source),
                    Codec.STRING.fieldOf("mode").forGetter(DamageValue::mode)
            ).apply(instance, DamageValue::new));

    /** 变量前缀（{@code damage.}）后的合法 mode 集判定。 */
    public static boolean isDamageMode(String type) {
        return type.startsWith("damage.");
    }

    /** 变量前缀（{@code damage_taken.}）后的合法 mode 集判定（合并自原 DamageTakenValue）。 */
    public static boolean isDamageTakenMode(String type) {
        return type.startsWith("damage_taken.");
    }

    @Override
    public double evaluate(ValueScope scope) {
        if (source == Source.EVENT) return new DamageEventNode(mode).evaluate(scope);
        return evalContext(scope);
    }

    /** 名词短语：伤害 mode → 值键 {@code stat.value.damage.<mode>}（CompareValue 操作数）。 */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("damage", mode));
    }

    /** 条件从句：{@code stat.condition.damage} = "when dealing %s"。 */
    @Override
    public List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.damage", describeQuantity(ClientLocale.get()).get(0)));
    }

    // —— CONTEXT 源：读技能阶段伤害子站（原 DamageValue 行为）——
    private double evalContext(ValueScope scope) {
        DamageContext dc = scope.root().getDamageContext();
        if (dc == null) return 0D;
        return switch (mode) {
            // —— 伤害数值（damages 数组序：物理/火/冰/电/混沌，同 DamageEvent 权威链路）——
            case "physical" -> damageAt(dc, 0);
            case "fire" -> damageAt(dc, 1);
            case "cold" -> damageAt(dc, 2);
            case "light" -> damageAt(dc, 3);
            case "chaos" -> damageAt(dc, 4);
            case "total" -> total(dc);
            case "energy" -> dc.getDamageEnergy();
            case "health" -> dc.getDamageHealth();
            case "overflow" -> dc.getOverflowDamage();
            case "mana" -> dc.getDamageMana();
            // —— 命中 / 结算状态（布尔 → 1/0）——
            case "hit" -> dc.isHit() ? 1 : 0;
            case "hit_block" -> dc.isHitBlock() ? 1 : 0;
            case "hit_entity" -> dc.isHitEntity() ? 1 : 0;
            case "crit" -> dc.isCrit() ? 1 : 0;
            case "evasion" -> dc.isEvasion() ? 1 : 0;
            case "block" -> dc.isBlock() ? 1 : 0;
            case "parry" -> dc.isParry() ? 1 : 0;
            // —— 本次命中衍生异常标志 ——
            case "chill" -> dc.isChill() ? 1 : 0;
            case "freeze" -> dc.isFreeze() ? 1 : 0;
            case "shock" -> dc.isShock() ? 1 : 0;
            case "shock2" -> dc.isShock2() ? 1 : 0;
            case "bleeding" -> dc.isBleeding() ? 1 : 0;
            case "ignite" -> dc.isIgnite() ? 1 : 0;
            case "poison" -> dc.isPoison() ? 1 : 0;
            case "stun" -> dc.isStun() ? 1 : 0;
            default -> 0D;
        };
    }

    private static double damageAt(DamageContext dc, int index) {
        float[] damages = dc.getDamages();
        return damages != null && damages.length > index ? damages[index] : 0D;
    }

    private static double total(DamageContext dc) {
        float[] damages = dc.getDamages();
        if (damages == null) return 0D;
        double sum = 0;
        for (float d : damages) sum += d;
        return sum;
    }
}
