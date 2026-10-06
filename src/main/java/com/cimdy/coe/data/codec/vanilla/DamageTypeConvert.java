package com.cimdy.coe.data.codec.vanilla;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.meta.DamageConvert;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.registry.AttributeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public record DamageTypeConvert(
        Optional<ResourceKey<DamageType>> type, Optional<TagKey<DamageType>> tag, DamageConvert convert
) {
    public static final Codec<DamageTypeConvert> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DAMAGE_TYPE).optionalFieldOf("type").forGetter(DamageTypeConvert::type),
            TagKey.codec(Registries.DAMAGE_TYPE).optionalFieldOf("tag").forGetter(DamageTypeConvert::tag),
            DamageConvert.CODEC.fieldOf("convert").forGetter(DamageTypeConvert::convert)
    ).apply(instance, DamageTypeConvert::new));

    public static DamageTypeConvert from(ResourceKey<DamageType> type, DamageConvert convert) {
        return new DamageTypeConvert(Optional.of(type), Optional.empty(), convert);
    }

    public static DamageTypeConvert from(TagKey<DamageType> tag, DamageConvert convert) {
        return new DamageTypeConvert(Optional.empty(), Optional.of(tag), convert);
    }

    public record Damage(Optional<ResourceKey<DamageType>> type, Optional<TagKey<DamageType>> tag) {
        public boolean matches(DamageSource source) {
            if (this.type.isPresent()) {
                Optional<ResourceKey<DamageType>> key = source.typeHolder().unwrapKey();
                if (key.isEmpty() || !key.get().equals(this.type.get())) {
                    return false;
                }
            }
            return this.tag.isEmpty() || source.is(this.tag.get());
        }
    }

    /**
     * 抗性取值模式（攻击方条目对「有效抗性」的改写方式）。
     * <ul>
     *   <li>{@link #NORMAL}：原行为（{@code max(0, min(res, max) - pen)}）。</li>
     *   <li>{@link #NON_NEGATIVE_ONLY}：忽略非负抗性（{@code min(eff, 0)}，负抗性仍生效）。</li>
     *   <li>{@link #INVERTED}：抗性取反（{@code -eff}）。</li>
     * </ul>
     */
    public enum ResMode { NORMAL, NON_NEGATIVE_ONLY, INVERTED }

    /**
     * 归一化抗性减免：在「元素标签作用域」内读取 {@code RESISTANCES} / {@code MAX_RESISTANCES}，
     * 一次遍历四种元素各走一遍流程（与 {@code AilmentHelper.withScope} 的「归一属性+标签」范式同源）。
     * 取代旧的 FIRE/COLD/LIGHT/CHAOS_RESISTANCE + MAX_*_RESISTANCE 8 个独立属性（绝对性完全更改，不并存）。
     *
     * <p>{@code against} 绑为作用域的<b>交互对方</b>（{@link StatContext#sourceEntity()}）：受害者读抗性时，
     * 引擎据此额外聚合攻击者挂载、target=EVENT_TARGET 的降抗条目（「你冻结/点燃/诅咒的敌人抗性降低 X%」），
     * 并按受害者（conditionTarget=EVENT_TARGET）求值其状态条件。未绑定（null）时跨实体分支整段跳过，
     * 行为与改动前逐字一致（DoT / 环境伤害路径不受影响）。标签集保持不变，仅追加 sourceEntity。
     *
     * @param living     受击方（抗性读取端，实时 + 元素作用域）
     * @param elementTag 元素标签（{@link GateTag#FIRE}/{@link GateTag#COLD}/{@link GateTag#LIGHTNING}/{@link GateTag#CHAOS}）
     * @param penetration 攻击方穿透值（该元素的抗性穿透，0 = 无穿透）
     * @param original   原始伤害
     * @param against    交互对方（攻击方，可为 null = 无跨实体条目生效）
     * @return 减免后伤害（POE2：穿透不能把抗性降到 0 以下，故有效抗性 = max(0, min(res, max) - pen)）
     */
    public static float applyReduction(LivingEntity living, String elementTag, float penetration, float original,
                                       @Nullable LivingEntity against) {
        return applyReduction(living, elementTag, penetration, original, against, ResMode.NORMAL);
    }

    /**
     * 抗性减免（带<b>抗性取值模式</b>）：在 {@link #applyReduction} 的四参版本之上，
     * 允许攻击方条目改写「有效抗性」的取值方式（忽略非负 / 取反），供命中结算一次性传入。
     *
     * @param mode 抗性取值模式（{@link ResMode#NORMAL} = 原行为，逐字不变）
     */
    public static float applyReduction(LivingEntity living, String elementTag, float penetration, float original,
                                       @Nullable LivingEntity against, ResMode mode) {
        AutoCloseable scope = StatScopes.enter(living, StatContext.of(Set.of(elementTag), null).withSourceEntity(against));
        try {
            double res = living.getAttributeValue(AttributeRegistry.RESISTANCES);
            double max = living.getAttributeValue(AttributeRegistry.MAX_RESISTANCES);
            double effective = Math.max(0.0, Math.min(res, max) - penetration);
            // 模式改写只在 NORMAL 之外生效：NON_NEGATIVE_ONLY 丢弃正抗性（负抗性仍生效），
            // INVERTED 把抗性取反（POE2「视为反转」：75% 抗性 → 承受伤害 ×1.75）。
            if (mode == ResMode.NON_NEGATIVE_ONLY) effective = Math.min(effective, 0.0);
            else if (mode == ResMode.INVERTED) effective = -effective;
            return original * (1 - (float) effective);
        } catch (Exception e) {
            CraftOfExile.LOGGER.warn("DamageTypeConvert.applyReduction failed for tag {} (returning original {}), reason: {}", elementTag, original, e.getMessage(), e);
            return original;
        } finally {
            try {
                scope.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static float applyArmorReduction(AdditionData data, float original) {
        double armor = data.getAttributeValue(Attributes.ARMOR);
        double armor_t = data.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float reduction = armor == 0 ? (float) Math.min(armor_t * 0.01F, 0.90F)
                : Math.min((float) (armor / (armor + original * 5) + armor_t * 0.01F), 0.90F);
        return original * (1 - reduction);
    }
}
