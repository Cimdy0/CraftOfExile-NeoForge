package com.cimdy.coe.data.gen.source;

import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.helper.resource.HealthHelper;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.entity.LivingMod;
import com.cimdy.coe.data.codec.support.attribute.AttributeLimiters;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;
import java.util.Set;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

public class EntityGen {
    public static void registerEntityMod(BootstrapContext<LivingMod.Mod> bootstrap) {
        // 归一：抗性收口为 RESISTANCES / MAX_RESISTANCES 单一属性，按元素标签作用域区分火/寒/电/混沌，
        // 不再引用待删的 FIRE/COLD/LIGHT/CHAOS_RESISTANCE 旧属性。
        registerResistanceMod(bootstrap, "fire_resistant", GateTag.FIRE, 0.5, 0.1);
        registerResistanceMod(bootstrap, "cold_resistant", GateTag.COLD, 0.5, 0.1);
        registerResistanceMod(bootstrap, "light_resistant", GateTag.LIGHTNING, 0.5, 0.1);
        registerResistanceMod(bootstrap, "chaos_resistant", GateTag.CHAOS, 0.5, 0.0);

        // 归一：额外元素伤害收口为单一 DAMAGE 属性 + 元素标签作用域（{damage,fire} 等），
        // 不再引用待删的 EXTRA_*_DAMAGE 旧属性。
        registerTaggedMod(bootstrap, "extra_fire_damage", AttributeRegistry.DAMAGE, 0.4,
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.DAMAGE, GateTag.FIRE));
        registerTaggedMod(bootstrap, "extra_cold_damage", AttributeRegistry.DAMAGE, 0.4,
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.DAMAGE, GateTag.COLD));
        registerTaggedMod(bootstrap, "extra_light_damage", AttributeRegistry.DAMAGE, 0.4,
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.DAMAGE, GateTag.LIGHTNING));
        registerTaggedMod(bootstrap, "extra_chaos_damage", AttributeRegistry.DAMAGE, 0.4,
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.DAMAGE, GateTag.CHAOS));

        // 归一：AILMENT_* 属性按异常标签作用域读取，必须带对应异常标签，否则作用于全部异常。
        registerTaggedMod(bootstrap, "always_bleeds", AttributeRegistry.AILMENT_CHANCE, 1.0,
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.BLEED));
        registerTaggedMod(bootstrap, "always_poisons", AttributeRegistry.AILMENT_CHANCE, 1.0,
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.POISON));

        // 归一：破甲强度收口为 ARMOR_BREAK 单属性 + {armor_break} 标签作用域
        // （替代 PHYSICAL_DAMAGE_ARMOR_BREAK；运行时在 DamagePipeline.stagePostReduction 读攻击方该作用域，
        // 缩放施加到受击方的 ARMOUR_BREAK 状态幅度）。+0.10 = 额外 +10% 破甲幅度。
        registerTaggedMod(bootstrap, "breaks_armor", AttributeRegistry.ARMOR_BREAK, 0.10,
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.ARMOR_BREAK));

        // Stun mod group 1: stun buildup AILMENT_BUILDUP base +1.0 (read at runtime via buildupMultiplier {ailment,buildup,stun}).
        // Scope reads it; replaces old non-runtime CAUSES_STUN_BUILDUP.
        registerTaggedMod(bootstrap, "stuns", AttributeRegistry.AILMENT_BUILDUP, 1.0,
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.STUN));

        // 归一：暴击率收口为 CRIT_CHANCE 单属性 + {attack,crit} 标签作用域（替代 ATTACK_CRIT_CHANCE，见 DamageHelper.getCritChance）。
        // 旧 ATTACK_CRIT_CHANCE 基础 0.05 上 +3.0 倍 → 怪物 20% 暴击；归一下该 Mod 贡献暴击几率 +0.20。
        registerTaggedMod(bootstrap, "extra_crits", AttributeRegistry.CRIT_CHANCE, 0.20,
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.ATTACK, GateTag.CRIT));

        registerMod(bootstrap, "accurate", 1,
                AttributeRegistry.ACCURACY, 2.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        registerMod(bootstrap, "hasted", 1,
                Attributes.MOVEMENT_SPEED, 0.3,
                Attributes.ATTACK_SPEED, 0.3,
                AttributeRegistry.CAST_SPEED, 0.3);

        registerMod(bootstrap, "additional_projectiles", 1,
                EntityTypeTags.REDIRECTABLE_PROJECTILE,
                AttributeRegistry.LAUNCH_ADDITIONAL_PROJECTILE, 4);

        registerMod(bootstrap, "armored", 1,
                Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        // 归一：承受伤害由蓝量承担（Mind Over Matter）收口为 MANA_TAKE_OF_DAMAGE 单属性 + {mana_take} 标签作用域
        // （运行时在 DamagePipeline.stageMana 读受击方该作用域，把承伤的该比例从 mana 扣，剩余走生命）。
        // 0.30 = 承受伤害的 30% 由蓝量承担。
        registerTaggedMod(bootstrap, "mind_over_matter", AttributeRegistry.MANA_TAKE_OF_DAMAGE, 0.30,
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.MANA_TAKE));

        registerMod(bootstrap, "increased_life", 1,
                Attributes.MAX_HEALTH, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        // 归一：生命回复上限收口为 MAX_REGENERATION 单属性 + {health,regen} 标签作用域
        // （替代 MAX_HEALTH_REGENERATION，见 HealthHelper.getHealthRegeneration）。保留怪物额外 +0.02 回血上限。
        registerTaggedMod(bootstrap, "regenerates_life", AttributeRegistry.MAX_REGENERATION, 0.02,
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.HEALTH, "regen"));

        registerMod(bootstrap, "crit_resistant", 1,
                AttributeRegistry.CRIT_DAMAGE, -0.8, AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.TAKEN));

        // Stun-resistant mod group 1: stun threshold AILMENT_THRESHOLD base +1.5 (read at runtime via ailmentThresholdMul
        // {ailment,threshold,stun}); scope read replaces old non-runtime STUN_THRESHOLD_PERCENTAGE.
        registerTaggedMod(bootstrap, "stun_resistant", AttributeRegistry.AILMENT_THRESHOLD, 1.5,
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.STUN));

        // —— 词缀赋予技能（固定 / 随机）：写入实体 SKILL_ASSEMBLY，由 MobSkillCastGoal 驱动施放 ——
        // 固定技能：火法词缀 —— 必定获得 fireball（5 级），并带施法速度加成。
        // 权重低于纯属性词缀（600 < 1000），使「会放技能的怪」是稀有变体。
        registerSkillMod(bootstrap, "pyromancer", 600,
                List.of(LivingMod.fixed(skillId("fireball"), 5, 0)),
                AttributeRegistry.CAST_SPEED, 0.2);
        // 固定技能：兽性词缀 —— 必定获得 wolf_rake（3 级，0 消耗）。
        registerSkillMod(bootstrap, "feral_slasher", 600,
                List.of(LivingMod.fixed(skillId("wolf_rake"), 3, 0)),
                Attributes.ATTACK_SPEED, 0.2);
        // 随机技能：元素亲和 —— 从雷 / 冰 / 骨三系法术中按权重随机抽 1（4 级）。
        registerSkillMod(bootstrap, "elemental_adept", 600,
                List.of(LivingMod.random(List.of(
                        LivingMod.pool(skillId("lightning_bolt"), 2),
                        LivingMod.pool(skillId("freezing_shards"), 2),
                        LivingMod.pool(skillId("bone_blast"), 1)), 4, 0)),
                AttributeRegistry.CAST_SPEED, 0.15);
        // 随机技能 + 随机强度：双技能词缀（一固定一随机），演示同一词缀可给多个技能。
        registerSkillMod(bootstrap, "warlock", 400,
                List.of(
                        LivingMod.fixed(skillId("bone_blast"), 4, 0),
                        LivingMod.random(List.of(
                                LivingMod.pool(skillId("decompose"), 1),
                                LivingMod.pool(skillId("fulmination"), 1)), 3, 0)),
                AttributeRegistry.CAST_SPEED, 0.25);
    }

    /** 技能 id（技能 JSON 位于 {@code data/<ns>/coe/item/gem/skill/<color>/<name>.json}，id 不含颜色段）。 */
    private static Identifier skillId(String name) {
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name);
    }

    /**
     * 带技能的词缀：{@code skills} 固化进实体装配表（{@link LivingMod#grantSkills}），
     * 另附一条属性加成使词缀在技能之外也有意义（走既有 Stat 通道，由 StatEngine.fold 聚合）。
     */
    private static void registerSkillMod(BootstrapContext<LivingMod.Mod> bootstrap, String name, int weight,
                                         List<LivingMod.ModSkill> skills,
                                         Holder<Attribute> attribute, double amount) {
        LivingMod.Builder builder = LivingMod.builder(weight)
                .addModifier(attribute, createIdentifier(name), amount,
                        AttributeModifier.Operation.ADD_VALUE);
        skills.forEach(builder::addSkill);
        bootstrap.register(createResourceKey(name), new LivingMod.Mod(name, builder.build()));
    }

    private static void registerMod(
            BootstrapContext<LivingMod.Mod> bootstrap, String name, int level,
            Holder<Attribute> attribute1, double amount1, AttributeLimiters.Operation operation1,
            Holder<Attribute> attribute2, double amount2, AttributeLimiters.Operation operation2) {
        Identifier id = createIdentifier(name);
        bootstrap.register(createResourceKey(name),
                new LivingMod.Mod(name, LivingMod.builder(1000)
                        .addModifier(attribute1, id, amount1, operation1)
                        .addModifier(attribute2, id, amount2, operation2)
                        .build()));
    }

    /**
     * 归一抗性 Mod：RESISTANCES（当前抗性）与 MAX_RESISTANCES（上限）按元素标签作用域加成。
     * resAmount / maxAmount 为百分比比例值（0.5 = 50%）。
     */
    private static void registerResistanceMod(
            BootstrapContext<LivingMod.Mod> bootstrap, String name, String elemTag,
            double resAmount, double maxAmount) {
        Identifier id = createIdentifier(name);
        LivingMod.Builder builder = LivingMod.builder(1000)
                .addModifier(AttributeRegistry.RESISTANCES, id, resAmount,
                        AttributeModifier.Operation.ADD_VALUE, Set.of(elemTag));
        if (maxAmount > 0)
            builder.addModifier(AttributeRegistry.MAX_RESISTANCES, id, maxAmount,
                    AttributeModifier.Operation.ADD_VALUE, Set.of(elemTag));
        bootstrap.register(createResourceKey(name), new LivingMod.Mod(name, builder.build()));
    }

    /**
     * 归一带标签 Mod：属性按标签作用域（如 DAMAGE+{damage,fire} 或 AILMENT_CHANCE+{bleed}）生效。
     * amount 为百分比属性比例值（0.4 = +40%）。
     */
    private static void registerTaggedMod(
            BootstrapContext<LivingMod.Mod> bootstrap, String name, Holder<Attribute> attribute,
            double amount, AttributeModifier.Operation operation, Set<String> tags) {
        Identifier id = createIdentifier(name);
        bootstrap.register(createResourceKey(name), new LivingMod.Mod(name,
                LivingMod.builder(1000)
                        .addModifier(attribute, id, amount, operation, tags)
                        .build()));
    }

    private static void registerMod(
            BootstrapContext<LivingMod.Mod> bootstrap, String name, int level,
            Holder<Attribute> attribute1, double amount1, AttributeLimiters.Operation operation1,
            Holder<Attribute> attribute2, double amount2, AttributeModifier.Operation operation2) {
        Identifier id = createIdentifier(name);
        bootstrap.register(createResourceKey(name),
                new LivingMod.Mod(name, LivingMod.builder(1000)
                        .addModifier(attribute1, id, amount1, operation1)
                        .addModifier(attribute2, id, amount2, operation2)
                        .build()));
    }

    private static void registerMod(
            BootstrapContext<LivingMod.Mod> bootstrap, String name, int level,
            Holder<Attribute> attribute, double amount, AttributeLimiters.Operation operation) {
        Identifier id = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "entity.mod." + name);
        bootstrap.register(createResourceKey(name),
                new LivingMod.Mod(name, LivingMod.builder(1000)
                        .addModifier(attribute, id, amount, operation)
                        .build()));
    }

    private static void registerMod(
            BootstrapContext<LivingMod.Mod> bootstrap, String name, int level, TagKey<EntityType<?>> tag,
            Holder<Attribute> attribute, int amount) {
        Identifier id = createIdentifier(name);
        bootstrap.register(createResourceKey(name),
                new LivingMod.Mod(name, LivingMod.builder(1000)
                        .addModifier(attribute, id, amount, AttributeModifier.Operation.ADD_VALUE)
                        .build()));
    }

    private static void registerMod(
            BootstrapContext<LivingMod.Mod> bootstrap, String name, int level,
            Holder<Attribute> attribute, double amount) {
        registerMod(bootstrap, name, level, attribute, amount, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void registerMod(
            BootstrapContext<LivingMod.Mod> bootstrap, String name, int level,
            Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
        Identifier id = createIdentifier(name);
        bootstrap.register(createResourceKey(name),
                new LivingMod.Mod(name, LivingMod.builder(1000)
                        .addModifier(attribute, id, amount, operation)
                        .build()));
    }
    private static void registerMod(
            BootstrapContext<LivingMod.Mod> bootstrap, String name, int level,
            Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation, Set<String> tags) {
        Identifier id = createIdentifier(name);
        bootstrap.register(createResourceKey(name),
                new LivingMod.Mod(name, LivingMod.builder(1000)
                        .addModifier(attribute, id, amount, operation, tags)
                        .build()));
    }

    private static void registerMod(
            BootstrapContext<LivingMod.Mod> bootstrap, String name, int level,
            Holder<Attribute> attribute1, double v1,
            Holder<Attribute> attribute2, double v2) {
        registerMod(bootstrap, name, level,
                attribute1, v1, AttributeModifier.Operation.ADD_VALUE,
                attribute2, v2, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void registerMod(
            BootstrapContext<LivingMod.Mod> bootstrap, String name, int level,
            Holder<Attribute> attribute1, double v1, AttributeModifier.Operation operation1,
            Holder<Attribute> attribute2, double v2, AttributeModifier.Operation operation2) {
        Identifier id = createIdentifier(name);
        bootstrap.register(createResourceKey(name),
                new LivingMod.Mod(name, LivingMod.builder(1000)
                        .addModifier(attribute1, id, v1, operation1)
                        .addModifier(attribute2, id, v2, operation2)
                        .build()));
    }

    private static void registerMod(
            BootstrapContext<LivingMod.Mod> bootstrap, String name, int level,
            Holder<Attribute> attribute1, double v1, Holder<Attribute> attribute2, double v2,
            Holder<Attribute> attribute3, double v3) {
        registerMod(bootstrap, name, level, attribute1, v1, attribute2, v2, attribute3, v3, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void registerMod(
            BootstrapContext<LivingMod.Mod> bootstrap, String name, int level,
            Holder<Attribute> attribute1, double v1, Holder<Attribute> attribute2, double v2,
            Holder<Attribute> attribute3, double v3, AttributeModifier.Operation operation) {
        Identifier id = createIdentifier(name);
        bootstrap.register(createResourceKey(name),
                new LivingMod.Mod(name, LivingMod.builder(1000)
                        .addModifier(attribute1, id, v1, operation)
                        .addModifier(attribute2, id, v2, operation)
                        .addModifier(attribute3, id, v3, operation)
                        .build()));
    }

    private static Identifier createIdentifier(String name) {
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name);
    }

    private static ResourceKey<LivingMod.Mod> createResourceKey(String name) {
        return ResourceKey.create(LoadEvent.LIVING_MOD_KEY, createIdentifier(name));
    }
}
