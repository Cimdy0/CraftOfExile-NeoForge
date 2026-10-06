package com.cimdy.coe.data.codec.skill.helper.cast;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.helper.MathHelper;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;
import java.util.Set;

public record SkillCast(
        Either<Attack, Spell> cast,
        double speedMultiplier
) {
    public static final Codec<Either<Attack, Spell>> EITHER_CODEC =
            Codec.either(Attack.CODEC, Spell.CODEC);

    /**
     * 序列化保持扁平（直接是 Attack/Spell），与历史技能 JSON 兼容。
     * speedMultiplier 仅在运行时由技能链解析产生，不参与序列化（解码时默认为 1.0）。
     */
    public static Codec<SkillCast> CODEC = EITHER_CODEC.xmap(
            either -> new SkillCast(either, 1.0),
            SkillCast::cast);

    public static SkillCast of(Attack attack) {
        return new SkillCast(Either.left(attack), 1.0);
    }

    public static SkillCast of(Spell spell) {
        return new SkillCast(Either.right(spell), 1.0);
    }

    /** 返回应用了速度倍率的新 SkillCast（辅助宝石加速施法时使用）。 */
    public SkillCast withSpeedMultiplier(double multiplier) {
        return new SkillCast(this.cast, multiplier);
    }

    public boolean matches(SkillContext context) {
        if (cast.left().isPresent()) {
            return cast.left().get().matches(context);
        }
        return cast.right().get().matches(context);
    }

    public Set<String> getLabels(SkillContext context) {
        if (cast.left().isPresent()) {
            return cast.left().get().getLabels(context);
        }
        return cast.right().get().getLabels(context);
    }

    public void setCooldown(SkillContext context) {
        if (cast.left().isPresent()) {
            cast.left().get().setCooldown(context);
        } else {
            cast.right().get().setCooldown(context);
        }
    }

    public void addComponent(List<Component> tooltip, SkillContext context) {
        if (cast.left().isPresent()) {
            cast.left().get().addDataComponent(tooltip, context);
        } else {
            cast.right().get().addDataComponent(tooltip, context);
        }
    }

    public int getCastTime(SkillContext context) {
        double base = cast.left().isPresent()
                ? cast.left().get().getCastTime(context)
                : cast.right().get().getCastTime(context);
        return (int) Math.max(1, Math.round(base / Math.max(0.0001, speedMultiplier)));
    }

    /** 返回当前技能的基础冷却（tick），由 Attack / Spell 提供，未应用辅助宝石速度倍率。 */
    public double getCooldown(SkillContext context) {
        if (cast.left().isPresent()) {
            return cast.left().get().getCooldown(context);
        }
        return cast.right().get().getCooldown(context);
    }

    /** 基础暴击率（由 Attack / Spell 提供，受辅助宝石倍率影响前的值）。 */
    public double getCrit(SkillContext context) {
        if (cast.left().isPresent()) {
            return cast.left().get().getCrit(context);
        }
        return cast.right().get().getCrit(context);
    }

    public record Attack(
            Either<WeaponAttack, OtherAttack> attack,
            ExpressionCodec.Value cooldown
    ) implements Caster {
        public static Codec<Either<WeaponAttack, OtherAttack>> EITHER_CODEC =
                Codec.either(WeaponAttack.CODEC, OtherAttack.CODEC);

        public static final Codec<Attack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EITHER_CODEC.optionalFieldOf("attack",
                                Either.left(new WeaponAttack(ExpressionCodec.of(1.0))))
                        .forGetter(Attack::attack),
                ExpressionCodec.VALUE_CODEC.optionalFieldOf("cooldown", ExpressionCodec.of(0.0))
                        .forGetter(sc -> sc.cooldown)
        ).apply(instance, Attack::new));

        @Override
        public Set<String> getLabels(SkillContext context) {
            return Set.of(GateTag.ATTACK);
        }

        @Override
        public String getType() {
            return GateTag.ATTACK;
        }

        @Override
        public double getCooldown(SkillContext context) {
            return this.cooldown.getValue(context);
        }

        @Override
        public void addCastTimeComponent(List<Component> tooltip, SkillContext context) {
            if (attack.left().isPresent()) {
                double d = this.attack.left().get().speed.getValue(context) * 100;
                Component component = Component.translatable("stat." + getType())
                        .append(Component.translatable("stat.speed"))
                        .append(": ").withStyle(ChatFormatting.DARK_AQUA)
                        .append(Component.translatable("stat.base")
                                .append(Component.translatable("stat.s")
                                        .append(MathHelper.df2.format(d) + "%")).withStyle(ChatFormatting.WHITE));
                tooltip.add(component);
            } else {
                Caster.super.addCastTimeComponent(tooltip, context);
            }
        }

        @Override
        public double getCastTime(SkillContext context) {
            double attack_speed = context.getCaster().getAttributeValue(Attributes.ATTACK_SPEED);
            if (this.attack.left().isPresent()) {
                double d = this.attack.left().get().speed.getValue(context);
                return 20 / attack_speed * d;
            }
            double d = attack.right().get().time.getValue(context);
            return d / attack_speed;
        }

        @Override
        public boolean hasSkillCrit() {
            if (this.attack.left().isPresent()) {
                return false;
            }
            return Caster.super.hasSkillCrit();
        }

        @Override
        public double getCrit(SkillContext context) {
            if (this.attack.left().isPresent()) {
                return Caster.super.getCrit(context);
            }
            return this.attack.right().get().crit.getValue(context);
        }

        public record WeaponAttack(
                ExpressionCodec.Value speed
        ) {
            public static Codec<WeaponAttack> CODEC =
                    ExpressionCodec.VALUE_CODEC.optionalFieldOf("speed", ExpressionCodec.of(1.0)).codec()
                            .xmap(WeaponAttack::new, WeaponAttack::speed);
        }

        public record OtherAttack(
                ExpressionCodec.Value time,
                ExpressionCodec.Value crit
        ) {
            public static Codec<OtherAttack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ExpressionCodec.VALUE_CODEC.optionalFieldOf("time",
                            ExpressionCodec.of(20.0)).forGetter(OtherAttack::time),
                    ExpressionCodec.VALUE_CODEC.optionalFieldOf("crit",
                            ExpressionCodec.of(0.05)).forGetter(OtherAttack::crit)
            ).apply(instance, OtherAttack::new));
        }
    }

    public record Spell(
            ExpressionCodec.Value time,
            ExpressionCodec.Value cooldown,
            ExpressionCodec.Value crit
    ) implements Caster {

        public static final Codec<Spell> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExpressionCodec.VALUE_CODEC.optionalFieldOf("time", ExpressionCodec.of(20.0))
                        .forGetter(sc -> sc.time),
                ExpressionCodec.VALUE_CODEC.optionalFieldOf("cooldown", ExpressionCodec.of(0.0))
                        .forGetter(sc -> sc.cooldown),
                ExpressionCodec.VALUE_CODEC.optionalFieldOf("crit", ExpressionCodec.of(0.05))
                        .forGetter(sc -> sc.crit)
        ).apply(instance, Spell::new));

        public Set<String> getLabels(SkillContext context) {
            return Set.of(GateTag.SPELL);
        }

        @Override
        public double getCooldown(SkillContext context) {
            return this.cooldown.getValue(context);
        }

        @Override
        public double getCastTime(SkillContext context) {
            double d = this.time.getValue(context);
            double spell_speed = context.getCaster().getAttributeValue(AttributeRegistry.CAST_SPEED);
            return d / spell_speed;
        }

        @Override
        public double getCrit(SkillContext context) {
            return this.crit.getValue(context);
        }

        @Override
        public String getType() {
            return GateTag.SPELL;
        }
    }

    public interface Caster {

        default boolean matches(SkillContext context) {
            return enoughCastTime(context);
        }

        default boolean enoughCastTime(SkillContext context) {
            return context.getPressTime() >= this.getCastTime(context);
        }

        default void setCooldown(SkillContext context) {
            context.setCooldown(this.getCooldown(context));
        }

        Set<String> getLabels(SkillContext context);

        double getCooldown(SkillContext context);

        double getCastTime(SkillContext context);

        default boolean hasSkillCrit() {
            return true;
        }

        default double getCrit(SkillContext context) {
            return 0;
        }

        default double getDamage(SkillContext context) {
            return 0;
        }

        String getType();

        default void addDataComponent(List<Component> tooltip, SkillContext context) {
            this.addCastTimeComponent(tooltip, context);
            this.addCooldownComponent(tooltip, context);
            this.addCritComponent(tooltip, context);
        }

        default void addCritComponent(List<Component> tooltip, SkillContext context) {
            double crit = this.getCrit(context) * 100;
            if (crit > 0) {
                Component component = Component.translatable("stat.crit")
                        .append(Component.translatable("stat.chance"))
                        .append(": ").withStyle(ChatFormatting.DARK_AQUA)
                        .append(Component.literal(MathHelper.df2.format(crit) + "%").withStyle(ChatFormatting.WHITE));
                tooltip.add(component);
            }
        }

        default void addCastTimeComponent(List<Component> tooltip, SkillContext context) {
            double castTime = this.getCastTime(context);
            if (castTime > 0) {
                Component component = Component.translatable("stat." + getType())
                        .append(Component.translatable("stat.time"))
                        .append(": ").withStyle(ChatFormatting.DARK_AQUA)
                        .append(Component.literal(MathHelper.df2.format(castTime) + " tickStatus").withStyle(ChatFormatting.WHITE));
                tooltip.add(component);
            }
        }

        default void addCooldownComponent(List<Component> tooltip, SkillContext context) {
            double cooldown = this.getCooldown(context);
            if (cooldown > 0) {
                Component component = Component.translatable("stat.cooldown")
                        .append(Component.translatable("stat.time"))
                        .append(": ").withStyle(ChatFormatting.DARK_AQUA)
                        .append(Component.literal(MathHelper.df2.format(cooldown) + " tickStatus").withStyle(ChatFormatting.WHITE));
                tooltip.add(component);
            }
        }
    }
}
