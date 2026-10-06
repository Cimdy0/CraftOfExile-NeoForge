package com.cimdy.coe.data.codec.support.stat.effect.modifier;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 掷骰修饰（fold 型，非触发）：声明持有者在某个掷骰维度上「双掷取优（LUCKY）」或「双掷取劣（UNLUCKY）」。
 *
 * <p>覆盖 POE 基石 / 词缀的「X is Lucky / Unlucky」家族
 * （如 Damage is Lucky against Heavy Stunned Enemies / Chance to Evade is Unlucky）。
 * 运行时由 {@link StatEngine#rollModifier} 查询（攻击方 / 受击方按维度归属），
 * 在 {@code DamageHelper} 的伤害偏差（{@link RollSubject#DAMAGE}）、暴击判定（{@link RollSubject#CRIT}）、
 * 闪避判定（{@link RollSubject#EVADE}）、格挡判定（{@link RollSubject#DEFLECT}）四处接入。
 *
 * <p>数值 / 条件 / 选择器由外层条目持有；本类只携语义（{@link #subject()} + {@link #mode()}）。
 * 数值字段为占位常量（掷骰修饰只问「有没有、是什么」）。
 *
 * <p>多条目冲突时 {@link StatEngine#rollModifier} 约定：任一 LUCKY 即优（增益语义不被减益压过），
 * 否则任一 UNLUCKY 即劣。
 */
public final class LuckyStatEffect extends AbstractStatEffect {

    /** 掷骰维度：修饰作用于哪一次判定。 */
    public enum RollSubject implements StringRepresentable {
        /** 伤害偏差（攻击方持有：双掷伤害区间取优/劣）。 */
        DAMAGE,
        /** 暴击判定（攻击方持有：双掷暴击率取优/劣）。 */
        CRIT,
        /** 闪避判定（受击方持有：双掷闪避取优/劣，劣=更容易被打中）。 */
        EVADE,
        /** 格挡判定（受击方持有：双掷格挡取优/劣，劣=更容易被格挡）。 */
        DEFLECT,
        /**
         * 受害方反向伤害修饰（受击方持有：使攻击方对己伤害 UNLUCKY）。
         * 与 {@link #DAMAGE} 方向相反（DAMAGE 由攻击方持有），独立维度以避免与持有者的进攻性
         * 「Damage is Lucky」碰撞（受害方同时持有进攻幸运时不会误将敌方伤害变为幸运）。
         */
        VICTIM_DAMAGE;

        public static final Codec<RollSubject> CODEC = StringRepresentable.fromEnum(RollSubject::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /** 掷骰模式：LUCKY 双掷取优 / UNLUCKY 双掷取劣。 */
    public enum RollMode implements StringRepresentable {
        LUCKY,
        UNLUCKY;

        public static final Codec<RollMode> CODEC = StringRepresentable.fromEnum(RollMode::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private final RollSubject subject;
    private final RollMode mode;

    public LuckyStatEffect(RollSubject subject, RollMode mode, GateExpr tags) {
        super(tags);
        this.subject = subject;
        this.mode = mode;
    }
    /** 无事件标签门控（{@link GateExpr#ALWAYS}）便捷构造。 */
    public LuckyStatEffect(RollSubject subject, RollMode mode) {
        this(subject, mode, GateExpr.ALWAYS);
    }

    /** 掷骰维度。 */
    public RollSubject subject() {
        return subject;
    }

    /** 掷骰模式。 */
    public RollMode mode() {
        return mode;
    }

    /** 便捷构造：维度 + 模式条目（作用于持有者自身，无条件无标签）。 */
    public static StatRead entry(RollSubject subject, RollMode mode) {
        return StatEntry.of(ExpressionCodec.VALUE,
                new LuckyStatEffect(subject, mode, GateExpr.ALWAYS));
    }

    public static final MapCodec<LuckyStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RollSubject.CODEC.fieldOf("subject").forGetter(LuckyStatEffect::subject),
            RollMode.CODEC.fieldOf("mode").forGetter(LuckyStatEffect::mode),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, LuckyStatEffect::new));

    @Override
    public String type() {
        return "lucky";
    }

    /** 掷骰修饰为查询型（非事件触发），由 DamageHelper 判定点读取。 */
    @Override
    public boolean isTrigger() {
        return false;
    }

    @Override
    public EffectKind indexKind() {
        return EffectKind.LUCKY;
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
        return List.of(Component.translatable("stat_effect.lucky",
                StatValueKey.of("roll_mode", mode.name().toLowerCase(Locale.ROOT)),
                StatValueKey.of("roll_subject", subject.name().toLowerCase(Locale.ROOT))));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LuckyStatEffect e)) return false;
        return subject == e.subject && mode == e.mode && Objects.equals(tags(), e.tags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, mode, tags());
    }

    @Override
    public String toString() {
        return "LuckyStatEffect{" + mode.name().toLowerCase(Locale.ROOT) + ":" +
                subject.name().toLowerCase(Locale.ROOT) + ", tags=" + tags() + "}";
    }
}
