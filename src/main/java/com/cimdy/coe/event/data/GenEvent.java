package com.cimdy.coe.event.data;


import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityTypeValue;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.event.combat.status.EffectEvent;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DamageTypeRegistry;
import com.cimdy.coe.registry.DamageTypeTagRegistry;
import com.cimdy.coe.data.codec.item.equipment.AccessorySlotDef;
import com.cimdy.coe.registry.item.AccessorySlotRegistry;
import com.cimdy.coe.registry.item.CharmSlotRegistry;
import com.cimdy.coe.data.codec.entity.LivingModifier;
import com.cimdy.coe.data.codec.entity.LivingMod;
import com.cimdy.coe.data.codec.item.ItemWeight;
import com.cimdy.coe.data.codec.item.ItemInstance;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.cimdy.coe.data.codec.item.mod.value.ModNumber;
import com.cimdy.coe.data.codec.vanilla.DamageTypeConvert;
import com.cimdy.coe.data.codec.vanilla.EntityDirectDamage;
import com.cimdy.coe.data.codec.support.meta.DamageConvert;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.cimdy.coe.data.codec.support.math.context.query.Conversions;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityFlagValue;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.effect.convert.DamageConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.item.equipment.EquipRuleDef;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.skill.support.SupportGem;
import com.cimdy.coe.data.codec.vanilla.EffectModifier;
import com.cimdy.coe.data.codec.vanilla.EnchantModifier;
import com.cimdy.coe.registry.item.FlaskSlotRegistry;
import com.cimdy.coe.data.gen.asset.*;
import com.cimdy.coe.data.gen.equipment.*;
import com.cimdy.coe.data.gen.gem.*;
import com.cimdy.coe.data.gen.lang.*;
import com.cimdy.coe.data.gen.provider.*;
import com.cimdy.coe.data.gen.source.*;
import com.cimdy.coe.data.gen.stat.*;
import com.cimdy.coe.data.gen.stat.*;
import com.cimdy.coe.data.gen.stat.effect.*;
import com.cimdy.coe.data.gen.stat.*;
import com.cimdy.coe.data.gen.talent.*;
import com.cimdy.coe.data.gen.world.*;
import com.cimdy.coe.data.gen.asset.EnchantModifierGen;
import com.cimdy.coe.data.gen.source.EntityGen;
import com.cimdy.coe.data.gen.equipment.EquipmentGen;
import com.cimdy.coe.data.gen.asset.EssenceFamilyProvider;
import com.cimdy.coe.data.gen.gem.GemGen;
import com.cimdy.coe.data.gen.asset.RuneFamilyProvider;
import com.cimdy.coe.data.gen.stat.StatGen;
import com.cimdy.coe.data.helper.engine.EquipRules;
import com.cimdy.coe.registry.item.ItemRegistry;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.cimdy.coe.data.codec.support.damage.DamageTypes;


@EventBusSubscriber(modid = CraftOfExile.MODID)
public class GenEvent {
    @SubscribeEvent
    public static void ServerGatherDataEvent(GatherDataEvent.Client event) {
        RegistrySetBuilder builder = new RegistrySetBuilder();
        builder.add(Registries.DAMAGE_TYPE, CoeDataBootstrap::registerDamageType);
        DatapackBuiltinEntriesProvider datapackProvider = new DatapackBuiltinEntriesProvider(
                event.getGenerator().getPackOutput(), event.getLookupProvider(),
                builder,
                Set.of(CraftOfExile.MODID));
        event.getGenerator().addProvider(true, datapackProvider);

        // 已转 Provider 的数据类别（CoeDataBootstrap.JSON_CATEGORIES）：直接经 codec 写出 JSON，
        // 路径与 LoadEvent 扫描前缀同源，取代原 datapack 注册表的序列化（Phase 3 审计：注册表层为客户端死代码）。
        event.getGenerator().addProvider(true,
                new CoeDataProvider(event.getGenerator().getPackOutput(), datapackProvider.getRegistryProvider()));

        // EFFECT_MODIFIER 注册表为死数据（全树零读取方），已移出 CATEGORIES；但其 body 经 StatGen.modRead
        // 写出 fire/chaos 抗性加成 stat 条目（被 data/coe/coe/effect/*.json 的 ref 引用），须在数据生成期执行。
        CoeDataBootstrap.registerEffectModifier();

        event.getGenerator().addProvider(true, new RecipeSiteProvider(event.getGenerator().getPackOutput()));

        event.getGenerator().addProvider(true,
                new DamageTypeTagsProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), CraftOfExile.MODID) {
                    @Override
                    protected void addTags(HolderLookup.Provider provider) {
                        DamageTypeRegistry.BYPASS_ENERGY_TYPE.forEach(type ->
                                this.tag(DamageTypeTagRegistry.BYPASS_ENERGY).addOptional(type));
                        DamageTypeRegistry.AILMENT_TYPE.forEach(type -> {
                            this.tag(DamageTypeTagRegistry.IS_AILMENT).addOptional(type);
                            this.tag(Tags.DamageTypes.NO_FLINCH).addOptional(type);
                            this.tag(DamageTypeTags.NO_IMPACT).addOptional(type);
                            this.tag(DamageTypeTags.NO_KNOCKBACK).addOptional(type);
                        });

                    }
                });

        // 实体类别标签（beast / construct / demon / humanoid / undead）：由 datapack entity_type tag 定义，
        // 取代手写 data/coe/tags/entity_type/*.json，运行时经 EntityTypeValue 消费（COND_ENEMY_TYPE_* 等）。
        event.getGenerator().addProvider(true,
                new EntityTypeTagsProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), CraftOfExile.MODID) {
                    @Override
                    protected void addTags(HolderLookup.Provider provider) {
                        TagKey<EntityType<?>> beast = TagKey.create(Registries.ENTITY_TYPE,
                                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "beast"));
                        TagKey<EntityType<?>> construct = TagKey.create(Registries.ENTITY_TYPE,
                                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "construct"));
                        TagKey<EntityType<?>> demon = TagKey.create(Registries.ENTITY_TYPE,
                                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "demon"));
                        TagKey<EntityType<?>> humanoid = TagKey.create(Registries.ENTITY_TYPE,
                                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "humanoid"));
                        TagKey<EntityType<?>> undead = TagKey.create(Registries.ENTITY_TYPE,
                                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "undead"));

                        tag(beast)
                                .addOptional(id("minecraft", "spider")).addOptional(id("minecraft", "cave_spider"))
                                .addOptional(id("minecraft", "wolf")).addOptional(id("minecraft", "bee"))
                                .addOptional(id("minecraft", "endermite")).addOptional(id("minecraft", "silverfish"))
                                .addOptional(id("minecraft", "polar_bear")).addOptional(id("minecraft", "ravager"))
                                .addOptional(id("minecraft", "axolotl")).addOptional(id("minecraft", "goat"))
                                .addOptional(id("minecraft", "camel")).addOptional(id("minecraft", "fox"))
                                .addOptional(id("minecraft", "panda")).addOptional(id("minecraft", "cat"))
                                .addOptional(id("minecraft", "ocelot")).addOptional(id("minecraft", "rabbit"))
                                .addOptional(id("minecraft", "strider"));
                        tag(construct)
                                .addOptional(id("minecraft", "iron_golem")).addOptional(id("minecraft", "snow_golem"))
                                .addOptional(id("minecraft", "shulker")).addOptional(id("minecraft", "guardian"))
                                .addOptional(id("minecraft", "elder_guardian"));
                        tag(demon)
                                .addOptional(id("minecraft", "blaze")).addOptional(id("minecraft", "ghast"))
                                .addOptional(id("minecraft", "magma_cube")).addOptional(id("minecraft", "hoglin"));
                        tag(humanoid)
                                .addOptional(id("minecraft", "player")).addOptional(id("minecraft", "villager"))
                                .addOptional(id("minecraft", "pillager")).addOptional(id("minecraft", "vindicator"))
                                .addOptional(id("minecraft", "evoker")).addOptional(id("minecraft", "illusioner"))
                                .addOptional(id("minecraft", "witch")).addOptional(id("minecraft", "piglin"))
                                .addOptional(id("minecraft", "piglin_brute"));
                        tag(undead)
                                .addOptional(id("minecraft", "zombie")).addOptional(id("minecraft", "zombie_villager"))
                                .addOptional(id("minecraft", "husk")).addOptional(id("minecraft", "drowned"))
                                .addOptional(id("minecraft", "skeleton")).addOptional(id("minecraft", "stray"))
                                .addOptional(id("minecraft", "wither_skeleton")).addOptional(id("minecraft", "skeleton_horse"))
                                .addOptional(id("minecraft", "zombie_horse")).addOptional(id("minecraft", "wither"))
                                .addOptional(id("minecraft", "phantom")).addOptional(id("minecraft", "zoglin"))
                                .addOptional(id("minecraft", "zombified_piglin"));
                    }

                    private static ResourceKey<EntityType<?>> id(String ns, String path) {
                        return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(ns, path));
                    }
                });

        event.getGenerator().addProvider(true, new CoeModelProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(true, new LangProvider(event.getGenerator().getPackOutput(), "zh_cn"));
        event.getGenerator().addProvider(true, new LangProvider(event.getGenerator().getPackOutput(), "en_us"));
        event.getGenerator().addProvider(true, new TalentNodeTypeProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(true, new ProgressionDataProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(true, new GateTagCategoryProvider(event.getGenerator().getPackOutput()));
        // 伤害类型定义表（ADR-003 阶段1）：内置默认（coe/damage_type/*.json）由 datagen 导出，
        // 运行时经 LoadEvent 的 coe/damage_type 监听装载，可被数据包覆盖/扩展。
        event.getGenerator().addProvider(true, new DamageTypeProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(true, new TalentTreeProvider(event.getGenerator().getPackOutput()));
        // 四段子部件先落盘（stat/effect、stat/targets、stat/condition、stat/duration），供 StatEntryProvider
        // 的条目以 read 引用指向它们；顺序不可颠倒（条目编码期即解析 ref，晚于四者会抛未注册）。
        // 实体获取条件走 StatConditionProvider 具名落盘（name-keyed，mirror effect/targets）；
        // 谓词条件段（COND_*）由 StatConditionGen 直接内联进条目，不单独落盘。
        // 时长段（stat/duration）同样 name-keyed 具名落盘，条目以 "duration": "read:coe:<id>" 引用。
        event.getGenerator().addProvider(true, new StatEffectProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(true, new StatTargetProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(true, new StatConditionProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(true, new StatDurationProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(true, new StatEntryProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(true, new EntityProvider(event.getGenerator().getPackOutput()));
        // 技能皮肤（skill-skin 系统）：为每个主动技能生成 coe/skill_skin/<path>.json，
        // 把「外观（实体渲染/描边/染色/粒子/音效）」按技能 id 锁定，取代旧时由伤害类型派生的机制。
        event.getGenerator().addProvider(true, new SkillSkinProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(true, new ConflictTableProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(true, new AdditionProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(true, new EssenceFamilyProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(true, new RuneFamilyProvider(event.getGenerator().getPackOutput()));
        // 关键字系统数据生成（C.9）：把 KeywordSource 按 id 分文件写出为 data/coe/keywords/<id>.json
        // （片段行数组 schema），运行时经 KeywordRegistry 枚举该目录装载；lang 键由 LangProvider 经 KeywordLang 一并生成。
        event.getGenerator().addProvider(true, new KeywordProvider(event.getGenerator().getPackOutput()));
    }

}