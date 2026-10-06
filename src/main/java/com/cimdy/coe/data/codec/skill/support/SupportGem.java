package com.cimdy.coe.data.codec.skill.support;

import com.cimdy.coe.data.codec.skill.modifier.impl.effect.AdditionalActionEffect;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.DeliveryMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EffectMod;
import com.cimdy.coe.data.codec.skill.gem.SupportRequire;
import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * 辅助宝石 — 定义辅助宝石的完整数据。
 * 一个辅助宝石包含多个「共享修正」（effects：DeliveryMod / EffectMod /
 * AdditionalActionEffect，与技能本身共用同一套形状），以及辅助宝石独有的「特殊修正」
 * （special：等级/品质/速度，装配期生效）、给被辅助技能宝石增加的额外需求与消耗倍率。
 *
 * <p>PoE2 对应：一颗 Support Gem 物品的数据。
 */
public record SupportGem(
        SupportRequire require,
        Optional<String> tag,
        Optional<List<String>> allowType,
        List<SkillModifier> effects,
        Optional<SpecialMod> special,
        Optional<ExpressionCodec.Value> manaCostMultiplier,
        Optional<ExpressionCodec.Value> reservationMultiplier,
        Optional<ExpressionCodec.Value> maxLevel,
        Optional<Boolean> lineage,
        int tier,
        int level,
        Optional<List<String>> forbids,
        Optional<List<String>> requires,
        Optional<List<String>> consumes,
        Optional<List<String>> produces
) {
    public static final Codec<SupportGem> CODEC = RecordCodecBuilder.create(i -> i.group(
            SupportRequire.CODEC.fieldOf("require").forGetter(SupportGem::require),
            Codec.STRING.optionalFieldOf("tag").forGetter(SupportGem::tag),
            Codec.STRING.listOf().optionalFieldOf("allow_type").forGetter(SupportGem::allowType),
            SkillModifier.CODEC.listOf().fieldOf("effects").forGetter(SupportGem::effects),
            SpecialMod.CODEC.optionalFieldOf("special").forGetter(SupportGem::special),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("mana_cost_multiplier")
                    .forGetter(SupportGem::manaCostMultiplier),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("reservation_multiplier")
                    .forGetter(SupportGem::reservationMultiplier),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("max_level")
                    .forGetter(SupportGem::maxLevel),
            Codec.BOOL.optionalFieldOf("lineage")
                    .forGetter(SupportGem::lineage),
            Codec.INT.optionalFieldOf("tier", 1).forGetter(SupportGem::tier),
            Codec.INT.optionalFieldOf("level", 1).forGetter(SupportGem::level),
            Codec.STRING.listOf().optionalFieldOf("forbids").forGetter(SupportGem::forbids),
            Codec.STRING.listOf().optionalFieldOf("requires").forGetter(SupportGem::requires),
            Codec.STRING.listOf().optionalFieldOf("consumes").forGetter(SupportGem::consumes),
            Codec.STRING.listOf().optionalFieldOf("produces").forGetter(SupportGem::produces)
    ).apply(i, SupportGem::new));

    /**
     * 去掉宝石 ID 路径中的颜色段（首段 red/green/blue），返回末段统一名称。
     * 例如 {@code coe:red/brutality -> brutality}，与 SkillData.getNameString 一致。
     */
    private static String baseName(Identifier id) {
        String path = id.getPath();
        int slash = path.indexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    /**
     * 从 Identifier 推导展示名称，与 SkillData 模式一致。
     */
    public static Component getDisplayName(Identifier id) {
        return Component.translatable("gem.support.name." + id.getNamespace() + "." + baseName(id));
    }

    /**
     * 从 Identifier 推导技能图标路径，与 SkillData 模式一致（统一名称，无 _icon 后缀）。
     */
    public static Identifier getIcon(Identifier id) {
        return Identifier.fromNamespaceAndPath(id.getNamespace(),
                "textures/gui/gem/" + baseName(id) + ".png");
    }

    /**
     * 计算本辅助宝石的魔力消耗倍率（宝石级属性，装配期由 SkillLinkResolver 汇总）。
     * 作用于 MANA / ENERGY / HEALTH 等「消耗型」资源，不影响精魂保留。
     */
    public float getManaCostMultiplier() {
        return manaCostMultiplier.map(v -> (float) v.getValue()).orElse(1.0f);
    }

    /**
     * 计算本辅助宝石的精魂保留倍率（宝石级属性，装配期由 SkillLinkResolver 汇总）。
     * 仅作用于 SPIRIT 的 PRESERVE（保留）资源，不影响普通魔力消耗。
     * <p>POE2 中「Cost &amp; Reservation Multiplier」是同一个数值，同时作用于二者；
     * 但本模型将二者拆分为独立字段，以支持「仅降低消耗」或「仅降低保留」的辅助宝石。
     */
    public float getReservationMultiplier() {
        return reservationMultiplier.map(v -> (float) v.getValue()).orElse(1.0f);
    }

    /** 等阶（未切割宝石筛选用，不影响运行时结算）。 */
    public int tier() {
        return tier;
    }

    /** 等级（由等阶推导，影响辅助宝石的等级上限/装配等级）。 */
    public int level() {
        return level;
    }

    /** 返回带指定等阶/等级的副本。 */
    public SupportGem withTier(int tier) {
        return new SupportGem(require, tag, allowType, effects, special, manaCostMultiplier,
                reservationMultiplier, maxLevel, lineage, tier, level,
                forbids, requires, consumes, produces);
    }

    /** 返回带指定等级的副本。 */
    public SupportGem withLevel(int level) {
        return new SupportGem(require, tag, allowType, effects, special, manaCostMultiplier,
                reservationMultiplier, maxLevel, lineage, tier, level,
                forbids, requires, consumes, produces);
    }
}
