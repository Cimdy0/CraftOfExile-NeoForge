package com.cimdy.coe.data.codec.support.require;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

public record Require(
        ExpressionCodec.Value level,
        ExpressionCodec.Value strength,
        ExpressionCodec.Value dexterity,
        ExpressionCodec.Value intelligence
) {
    public static final Codec<Require> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("level", ExpressionCodec.of(0.0))
                    .forGetter(Require::level),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("strength", ExpressionCodec.of(0.0))
                    .forGetter(Require::strength),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("dexterity", ExpressionCodec.of(0.0))
                    .forGetter(Require::dexterity),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("intelligence", ExpressionCodec.of(0.0))
                    .forGetter(Require::intelligence)
    ).apply(instance, Require::new));

    public static Builder builder() {
        return new Builder();
    }

    public boolean matches(SkillContext context) {
        LivingEntity caster = context.getCaster();
        boolean levelMatch = caster.getData(
                AttachmentRegistry.LEVEL) >= level.getValue(context);
        boolean strengthMatch = caster.getAttributeValue(
                AttributeRegistry.STRENGTH) >= strength.getValue(context);
        boolean dexterityMatch = caster.getAttributeValue(
                AttributeRegistry.DEXTERITY) >= dexterity.getValue(context);
        boolean intelligenceMatch = caster.getAttributeValue(
                AttributeRegistry.INTELLIGENCE) >= intelligence.getValue(context);
        return levelMatch && strengthMatch && dexterityMatch && intelligenceMatch;
    }

    public int[] getAll(SkillContext context) {
        return new int[]{
                this.getStrength(context),
                this.getDexterity(context),
                this.getIntelligence(context),
                this.getLevel(context)
        };
    }

    private int getStrength(SkillContext context) {
        return (int) this.strength.getValue(context);
    }

    private int getDexterity(SkillContext context) {
        return (int) this.dexterity.getValue(context);
    }

    private int getIntelligence(SkillContext context) {
        return (int) this.intelligence.getValue(context);
    }

    private int getLevel(SkillContext context) {
        return (int) this.level.getValue(context);
    }

    public static class Builder {
        private ExpressionCodec.Value level = ExpressionCodec.of(0.0);
        private ExpressionCodec.Value strength = ExpressionCodec.of(0.0);
        private ExpressionCodec.Value dexterity = ExpressionCodec.of(0.0);
        private ExpressionCodec.Value intelligence = ExpressionCodec.of(0.0);

        public Builder level(ExpressionCodec.Value level) {
            this.level = level;
            return this;
        }

        public Builder strength(ExpressionCodec.Value strength) {
            this.strength = strength;
            return this;
        }

        public Builder dexterity(ExpressionCodec.Value dexterity) {
            this.dexterity = dexterity;
            return this;
        }

        public Builder intelligence(ExpressionCodec.Value intelligence) {
            this.intelligence = intelligence;
            return this;
        }

        public Require build() {
            return new Require(level, strength, dexterity, intelligence);
        }
    }
}