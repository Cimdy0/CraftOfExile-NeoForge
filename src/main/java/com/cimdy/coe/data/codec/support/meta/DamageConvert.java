package com.cimdy.coe.data.codec.support.meta;

import com.cimdy.coe.data.codec.vanilla.DamageTypeConvert;
import com.cimdy.coe.data.codec.vanilla.EntityDirectDamage;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityFlagValue;
import com.cimdy.coe.event.data.LoadEvent;
import com.cimdy.coe.helper.EntityHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record DamageConvert(
        Optional<Boolean> replace, Optional<Integer> physical,
        Optional<Integer> chaos, Optional<Integer> fire,
        Optional<Integer> cold, Optional<Integer> light
) {
    public static final DamageConvert EMPTY = new DamageConvert(
            Optional.empty(), Optional.of(1), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty());

    public static final Codec<DamageConvert> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("replace").forGetter(DamageConvert::replace),
            Codec.INT.optionalFieldOf("physical").forGetter(DamageConvert::physical),
            Codec.INT.optionalFieldOf("chaos").forGetter(DamageConvert::chaos),
            Codec.INT.optionalFieldOf("fire").forGetter(DamageConvert::fire),
            Codec.INT.optionalFieldOf("cold").forGetter(DamageConvert::cold),
            Codec.INT.optionalFieldOf("light").forGetter(DamageConvert::light)
    ).apply(instance, DamageConvert::new));

    public static DamageConvert fromFire(boolean replace, int physical, int fire) {
        return builder().replace(replace)
                .physical(physical)
                .fire(fire)
                .build();
    }

    public static DamageConvert fromCold(boolean replace, int physical, int cold) {
        return builder().replace(replace)
                .physical(physical)
                .cold(cold)
                .build();
    }

    public static DamageConvert fromChaos(boolean replace, int physical, int chaos) {
        return builder().replace(replace)
                .physical(physical)
                .chaos(chaos)
                .build();
    }

    public static DamageConvert fromPhysical(boolean replace, int physical) {
        return builder().replace(replace)
                .physical(physical)
                .build();
    }

    public static DamageConvert fromChaos(boolean replace, int chaos) {
        return builder().replace(replace)
                .chaos(chaos)
                .build();
    }

    public static DamageConvert fromCold(boolean replace, int cold) {
        return builder().replace(replace)
                .cold(cold)
                .build();
    }

    public static DamageConvert fromFire(boolean replace, int fire) {
        return builder().replace(replace)
                .fire(fire)
                .build();
    }

    public static DamageConvert fromLight(boolean replace, int light) {
        return builder().replace(replace)
                .light(light)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Optional<Boolean> replace = Optional.empty();
        private Optional<Integer> physical = Optional.empty();
        private Optional<Integer> chaos = Optional.empty();
        private Optional<Integer> fire = Optional.empty();
        private Optional<Integer> cold = Optional.empty();
        private Optional<Integer> light = Optional.empty();

        public Builder replace(boolean replace) {
            this.replace = Optional.of(replace);
            return this;
        }

        public Builder physical(int physical) {
            this.physical = Optional.of(physical);
            return this;
        }

        public Builder chaos(int chaos) {
            this.chaos = Optional.of(chaos);
            return this;
        }

        public Builder fire(int fire) {
            this.fire = Optional.of(fire);
            return this;
        }

        public Builder cold(int cold) {
            this.cold = Optional.of(cold);
            return this;
        }

        public Builder light(int light) {
            this.light = Optional.of(light);
            return this;
        }

        public DamageConvert build() {
            return new DamageConvert(replace, physical, chaos, fire, cold, light);
        }
    }

    public MixedDamage getMixedDamage(float original) {
        float physicalWeight = this.physical().orElse(0);
        float chaosWeight = this.chaos().orElse(0);
        float fireWeight = this.fire().orElse(0);
        float coldWeight = this.cold().orElse(0);
        float lightWeight = this.light().orElse(0);
        float weightSum = physicalWeight + chaosWeight + fireWeight + coldWeight + lightWeight;

        return new MixedDamage(physicalWeight / weightSum * original, chaosWeight / weightSum * original,
                fireWeight / weightSum * original, coldWeight / weightSum * original,
                lightWeight / weightSum * original);
    }

    public record MixedDamage(float physical, float chaos, float fire, float cold, float light) {
        public static MixedDamage from(Arrow arrow, float original) {
            float physicalWeight = 0;
            float chaosWeight = 0;
            float fireWeight = 0;
            float coldWeight = 0;
            float lightWeight = 0;
            for (Map.Entry<EntityDirectDamage.ArrowDamageConvert.EffectState, DamageConvert> entry : LoadEvent.ARROW_DAMAGE_CONVERT_MAP.entrySet()) {
                EntityDirectDamage.ArrowDamageConvert.EffectState effectState = entry.getKey();
                DamageConvert damageConvert = entry.getValue();
                if (effectState.matches(arrow)) {
                    boolean replace = damageConvert.replace.isEmpty() || damageConvert.replace.get();
                    physicalWeight = replace ? physicalWeight : physicalWeight + damageConvert.physical().orElse(0);
                    chaosWeight = replace ? chaosWeight : chaosWeight + damageConvert.chaos().orElse(0);
                    fireWeight = replace ? fireWeight : fireWeight + damageConvert.fire().orElse(0);
                    coldWeight = replace ? coldWeight : coldWeight + damageConvert.cold().orElse(0);
                    lightWeight = replace ? lightWeight : lightWeight + damageConvert.light().orElse(0);
                }
            }
            float weightSum = physicalWeight + chaosWeight + fireWeight + coldWeight + lightWeight;
            return weightSum > 0 ? new MixedDamage(physicalWeight / weightSum * original, chaosWeight / weightSum * original,
                    fireWeight / weightSum * original, coldWeight / weightSum * original,
                    lightWeight / weightSum * original) : new MixedDamage(original, 0, 0, 0, 0);
        }

        /**
         * <b>载体口径</b>的箭矢混合转换：宿主只传形态标签，不传实体类。
         *
         * <p>技能在世界里放置出来的一切载体都是同一个 {@code SkillEntity}（靠 {@code EntityDef} 区分形态），
         * 它不是 {@code Arrow}，故 {@link #from(Arrow, float)} 永远接不到技能箭矢。
         * 本重载让「取哪份转换配方」由<b>获取方按形态标签声明</b>，与 {@code GameEvent.formTagsOf}
         * 同一口径（同一份 {@code EntityDef.tagSet()}），宿主不再 {@code instanceof} 猜形态。
         *
         * <p><b>effect 维度为何整条跳过</b>：箭矢转换表有两个键维度 ——
         * {@code state}（在水中 / 岩浆中 / 着火，读 {@link net.minecraft.world.entity.Entity} 基类状态）与
         * {@code effect}（箭矢「掉落物药瓶」里的药剂内容，读 {@code AbstractArrow} 的专属数据）。
         * 后者在 {@code SkillEntity} 上<b>无数据源</b>，{@code EntityDef} 也没有字段承载它；
         * 若强行视作匹配，等于给每一发技能箭矢白送毒素 / 缓慢转换。故只评估 {@code state} 维度，
         * {@code effect} 维度的条目按「无法评估」跳过 —— 此处如实收窄，而非假装等价。
         */
        public static MixedDamage fromCarrier(Entity entity, Set<String> formTags, float original) {
            float physicalWeight = 0;
            float chaosWeight = 0;
            float fireWeight = 0;
            float coldWeight = 0;
            float lightWeight = 0;
            for (Map.Entry<EntityDirectDamage.ArrowDamageConvert.EffectState, DamageConvert> entry : LoadEvent.ARROW_DAMAGE_CONVERT_MAP.entrySet()) {
                EntityDirectDamage.ArrowDamageConvert.EffectState effectState = entry.getKey();
                // effect 维度对载体不可评估 → 跳过该条（而非视作匹配）。
                if (effectState.effect().isPresent()) continue;
                DamageConvert damageConvert = entry.getValue();
                if (effectState.matchesStateOnly(entity)) {
                    boolean replace = damageConvert.replace.isEmpty() || damageConvert.replace.get();
                    physicalWeight = replace ? physicalWeight : physicalWeight + damageConvert.physical().orElse(0);
                    chaosWeight = replace ? chaosWeight : chaosWeight + damageConvert.chaos().orElse(0);
                    fireWeight = replace ? fireWeight : fireWeight + damageConvert.fire().orElse(0);
                    coldWeight = replace ? coldWeight : coldWeight + damageConvert.cold().orElse(0);
                    lightWeight = replace ? lightWeight : lightWeight + damageConvert.light().orElse(0);
                }
            }
            float weightSum = physicalWeight + chaosWeight + fireWeight + coldWeight + lightWeight;
            return weightSum > 0 ? new MixedDamage(physicalWeight / weightSum * original, chaosWeight / weightSum * original,
                    fireWeight / weightSum * original, coldWeight / weightSum * original,
                    lightWeight / weightSum * original) : new MixedDamage(original, 0, 0, 0, 0);
        }

        public static MixedDamage from(DamageSource source, float original) {
            float physicalWeight = 0;
            float chaosWeight = 0;
            float fireWeight = 0;
            float coldWeight = 0;
            float lightWeight = 0;
            for (Map.Entry<DamageTypeConvert.Damage, DamageConvert> entry : LoadEvent.LIVING_DAMAGE_CONVERT_MAP.entrySet()) {
                DamageTypeConvert.Damage damage = entry.getKey();
                DamageConvert damageConvert = entry.getValue();
                if (damage.matches(source)) {
                    boolean replace = damageConvert.replace.isEmpty() || damageConvert.replace.get();
                    physicalWeight = replace ? physicalWeight : physicalWeight + damageConvert.physical().orElse(0);
                    chaosWeight = replace ? chaosWeight : chaosWeight + damageConvert.chaos().orElse(0);
                    fireWeight = replace ? fireWeight : fireWeight + damageConvert.fire().orElse(0);
                    coldWeight = replace ? coldWeight : coldWeight + damageConvert.cold().orElse(0);
                    lightWeight = replace ? lightWeight : lightWeight + damageConvert.light().orElse(0);
                }
            }
            float weightSum = physicalWeight + chaosWeight + fireWeight + coldWeight + lightWeight;
            return weightSum > 0 ? new MixedDamage(physicalWeight / weightSum * original, chaosWeight / weightSum * original,
                    fireWeight / weightSum * original, coldWeight / weightSum * original,
                    lightWeight / weightSum * original) : new MixedDamage(original, 0, 0, 0, 0);
        }

        public static MixedDamage from(AbstractThrownPotion potion, float original) {
            float physicalWeight = 0;
            float chaosWeight = 0;
            float fireWeight = 0;
            float coldWeight = 0;
            float lightWeight = 0;
            for (Map.Entry<Holder<MobEffect>, DamageConvert> entry : LoadEvent.POTION_DAMAGE_CONVERT_MAP.entrySet()) {
                Holder<MobEffect> mobEffect = entry.getKey();
                DamageConvert damageConvert = entry.getValue();
                if (!EntityHelper.hasEffect(potion.getItem().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                        .getAllEffects(), mobEffect)) {
                    boolean replace = damageConvert.replace.isEmpty() || damageConvert.replace.get();
                    physicalWeight = replace ? physicalWeight : physicalWeight + damageConvert.physical().orElse(0);
                    chaosWeight = replace ? chaosWeight : chaosWeight + damageConvert.chaos().orElse(0);
                    fireWeight = replace ? fireWeight : fireWeight + damageConvert.fire().orElse(0);
                    coldWeight = replace ? coldWeight : coldWeight + damageConvert.cold().orElse(0);
                    lightWeight = replace ? lightWeight : lightWeight + damageConvert.light().orElse(0);
                }
            }
            float weightSum = physicalWeight + chaosWeight + fireWeight + coldWeight + lightWeight;
            return weightSum > 0 ? new MixedDamage(physicalWeight / weightSum * original, chaosWeight / weightSum * original,
                    fireWeight / weightSum * original, coldWeight / weightSum * original,
                    lightWeight / weightSum * original) : new MixedDamage(original, 0, 0, 0, 0);
        }

        public static MixedDamage from(Entity entity, float original) {
            float physicalWeight = 0;
            float chaosWeight = 0;
            float fireWeight = 0;
            float coldWeight = 0;
            float lightWeight = 0;
            for (Map.Entry<EntityFlagValue, DamageConvert> entry : LoadEvent.OTHER_DAMAGE_CONVERT_MAP.entrySet()) {
                EntityFlagValue predicate = entry.getKey();
                DamageConvert damageConvert = entry.getValue();
                if (predicate.matches(entity)) {
                    boolean replace = damageConvert.replace.isEmpty() || damageConvert.replace.get();
                    physicalWeight = replace ? physicalWeight : physicalWeight + damageConvert.physical().orElse(0);
                    chaosWeight = replace ? chaosWeight : chaosWeight + damageConvert.chaos().orElse(0);
                    fireWeight = replace ? fireWeight : fireWeight + damageConvert.fire().orElse(0);
                    coldWeight = replace ? coldWeight : coldWeight + damageConvert.cold().orElse(0);
                    lightWeight = replace ? lightWeight : lightWeight + damageConvert.light().orElse(0);
                }
            }
            float weightSum = physicalWeight + chaosWeight + fireWeight + coldWeight + lightWeight;
            return weightSum > 0 ? new MixedDamage(physicalWeight / weightSum * original, chaosWeight / weightSum * original,
                    fireWeight / weightSum * original, coldWeight / weightSum * original,
                    lightWeight / weightSum * original) : new MixedDamage(original, 0, 0, 0, 0);
        }
    }
}
