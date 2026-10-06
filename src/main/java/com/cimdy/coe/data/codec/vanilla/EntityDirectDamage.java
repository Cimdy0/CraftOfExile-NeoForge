package com.cimdy.coe.data.codec.vanilla;

import com.cimdy.coe.data.codec.support.meta.DamageConvert;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityFlagValue;
import com.cimdy.coe.helper.EntityHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.Optional;
import java.util.function.IntFunction;

public class EntityDirectDamage {
    public record ArrowDamageConvert(
            Optional<Holder<MobEffect>> effect,
            Optional<ArrowState> state,
            DamageConvert convert
    ) {
        public static final Codec<ArrowDamageConvert> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MobEffect.CODEC.optionalFieldOf("additions").forGetter(ArrowDamageConvert::effect),
                ArrowState.CODEC.optionalFieldOf("state").forGetter(ArrowDamageConvert::state),
                DamageConvert.CODEC.fieldOf("convert").forGetter(ArrowDamageConvert::convert)
        ).apply(instance, ArrowDamageConvert::new));

        public static ArrowDamageConvert from(Holder<MobEffect> effect, DamageConvert convert) {
            return new ArrowDamageConvert(Optional.of(effect), Optional.empty(), convert);
        }

        public static ArrowDamageConvert from(ArrowState state, DamageConvert convert) {
            return new ArrowDamageConvert(Optional.empty(), Optional.of(state), convert);
        }

        public record EffectState(Optional<Holder<MobEffect>> effect, Optional<ArrowState> state) {
            public boolean matches(Arrow arrow) {
                if (effect.isPresent() && !EntityHelper.hasEffect(arrow.getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                        .getAllEffects(), effect.get())) {
                    return false;
                } else return state.isEmpty() || state.get().matches(arrow);
            }

            /**
             * <b>载体口径</b>：只评估 {@code state} 维度（水中 / 岩浆 / 着火）。
             *
             * <p>{@code effect} 维度读的是箭矢的 {@code pickupItemStackOrigin} 里的药剂内容
             * （{@link #matches(Arrow)}），那是 {@code AbstractArrow} 的专属数据；
             * 技能投射物由 {@code SkillEntity} 承载，<b>没有也不该有</b>「掉落物药瓶」这一属性，
             * {@code EntityDef} 里也没有对应字段可承载它。故载体侧无法评估该维度，
             * 由 {@link com.cimdy.coe.data.codec.support.meta.DamageConvert} 整条跳过
             * （而非视作匹配——那会给每个技能箭矢白送一份毒素 / 缓慢转换）。
             */
            public boolean matchesStateOnly(Entity entity) {
                return state.isEmpty() || state.get().matches(entity);
            }
        }

        public enum ArrowState implements StringRepresentable {
            IS_FIRE("is_fire", 0),
            IN_WATER("in_water", 1),
            IN_LAVA("in_lava", 2);

            public static final IntFunction<ArrowState> BY_ID =
                    ByIdMap.continuous(ArrowState::getId, ArrowState.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            public static final Codec<ArrowState> CODEC =
                    StringRepresentable.fromEnum(ArrowState::values);
            private final String name;
            private final int id;

            ArrowState(String name, int value) {
                this.name = name;
                this.id = value;
            }

            public int getId() {
                return this.id;
            }

            public String getSerializedName() {
                return this.name;
            }

            /**
             * 只依赖 {@link Entity} 基类的水 / 岩浆 / 着火状态，故参数取 {@code Entity} 而非 {@code Arrow}：
             * 技能投射物由 {@code SkillEntity} 承载（不是 {@code Arrow}），但「是否在水中 / 岩浆中 / 着火」
             * 对载体同样成立，收窄参数会把它挡在转换表之外。
             */
            public boolean matches(Entity entity) {
                if (entity.isInWater()) {
                    return this == IN_WATER;
                } else if (entity.isInLava()) {
                    return this == IN_LAVA;
                } else if (entity.isOnFire()) {
                    return this == IS_FIRE;
                } else {
                    return false;
                }
            }
        }
    }

    public record PotionDamageConvert(
            Holder<MobEffect> effect,
            DamageConvert convert
    ) {
        public static final Codec<PotionDamageConvert> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MobEffect.CODEC.fieldOf("additions").forGetter(PotionDamageConvert::effect),
                DamageConvert.CODEC.fieldOf("convert").forGetter(PotionDamageConvert::convert)
        ).apply(instance, PotionDamageConvert::new));

        public static PotionDamageConvert from(Holder<MobEffect> effect, DamageConvert convert) {
            return new PotionDamageConvert(effect, convert);
        }
    }

    public record OtherDamageConvert(
            EntityFlagValue predicate,
            DamageConvert convert
    ) {
        public static final Codec<OtherDamageConvert> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityFlagValue.codec().fieldOf("predicate").forGetter(OtherDamageConvert::predicate),
                DamageConvert.CODEC.fieldOf("convert").forGetter(OtherDamageConvert::convert)
        ).apply(instance, OtherDamageConvert::new));
    }
}
