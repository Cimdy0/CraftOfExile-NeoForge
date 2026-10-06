package com.cimdy.coe.event.data;


import com.cimdy.coe.CoeConfig;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.entity.LivingMod;
import com.cimdy.coe.data.codec.entity.LivingModifier;
import com.cimdy.coe.data.codec.item.ItemInstance;
import com.cimdy.coe.data.codec.item.ItemWeight;
import com.cimdy.coe.data.codec.item.equipment.EquipRuleDef;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.skill.support.SupportGem;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityFlagValue;
import com.cimdy.coe.data.codec.support.math.context.query.Conversions;
import com.cimdy.coe.data.codec.support.meta.DamageConvert;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.support.stat.effect.convert.DamageConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.vanilla.DamageTypeConvert;
import com.cimdy.coe.data.codec.vanilla.EnchantModifier;
import com.cimdy.coe.data.codec.vanilla.EntityDirectDamage;
import com.cimdy.coe.data.gen.asset.EnchantModifierGen;
import com.cimdy.coe.data.gen.equipment.EquipmentGen;
import com.cimdy.coe.data.gen.gem.GemGen;
import com.cimdy.coe.data.gen.gem.skill.SkillPresets;
import com.cimdy.coe.data.gen.source.EntityGen;
import com.cimdy.coe.data.gen.stat.StatGen;
import com.cimdy.coe.data.helper.engine.EquipRules;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DamageTypeRegistry;
import com.cimdy.coe.registry.item.ItemRegistry;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CoeDataBootstrap {
    // Phase 4 / Task #391：19 类 datapack 注册表已全部转 Provider（注册表层经 Phase 3 审计判定对客户端为死代码，
    // 移除不影响同步）；各类型改由 JSON_CATEGORIES 经 CoeDataProvider 写 JSON，运行期由 LoadEvent 同前缀路径扫描填充 *_MAP 缓存消费。

    /**
     * 已转 Provider 的数据类别：不再声明 datapack 注册表，改由 {@link CoeDataProvider} 经 codec 把每条
     * 数据写成 {@code data/coe/<path>/<id>.json}（路径与 LoadEvent 扫描前缀同源，均取自对应 ResourceKey
     * 的 identifier path），客户端 / 运行期取数不变。注册表层对客户端为死代码（Phase 3 审计），移除不影响同步。
     */
    public static final List<JsonCategory<?>> JSON_CATEGORIES = List.of(
            new JsonCategory<>("damage_convert/direct_entity/potion", EntityDirectDamage.PotionDamageConvert.CODEC, CoeDataBootstrap::genPotionDamageConvert),
            new JsonCategory<>("damage_convert/direct_entity/arrow", EntityDirectDamage.ArrowDamageConvert.CODEC, CoeDataBootstrap::genArrowDamageConvert),
            new JsonCategory<>("damage_convert/direct_entity/other", EntityDirectDamage.OtherDamageConvert.CODEC, CoeDataBootstrap::genDirectDamageConvert),
            new JsonCategory<>("living/rarity", LivingModifier.CODEC, CoeDataBootstrap::genRarity),
            new JsonCategory<>("living/global", LivingModifier.CODEC, CoeDataBootstrap::genModifier),
            new JsonCategory<>("damage_convert/damage_type", DamageTypeConvert.CODEC, CoeDataBootstrap::genDamageConvert),
            new JsonCategory<>("expression", MathExpression.CODEC, CoeDataBootstrap::genExpression),
            new JsonCategory<>("item/equipment", ItemInstance.CODEC, CoeDataBootstrap::genItemType),
            new JsonCategory<>("item/gem/skill", SkillEntry.CODEC, CoeDataBootstrap::genSkill),
            new JsonCategory<>("item/gem/support", SupportGem.CODEC, CoeDataBootstrap::genSupportGem),
            new JsonCategory<>("item/gem/spirit", SkillEntry.CODEC, CoeDataBootstrap::genSpirit),
            new JsonCategory<>("skill/template", SkillEntry.CODEC, CoeDataBootstrap::genSkillTemplate),
            new JsonCategory<>("skill/preset", SkillEntry.CODEC, CoeDataBootstrap::genSkillPreset),
            new JsonCategory<>("living/mod", LivingMod.Mod.CODEC, CoeDataBootstrap::genEntityMod),
            new JsonCategory<>("enchant", EnchantModifier.CODEC, CoeDataBootstrap::genEnchantModifier),
            new JsonCategory<>("equip_rule", EquipRuleDef.CODEC, CoeDataBootstrap::genEquipRule)
    );

    public static void registerDamageType(BootstrapContext<DamageType> bootstrap) {
        DamageTypeRegistry.DAMAGE_TYPES.forEach(type -> {
            bootstrap.register(type,
                    new DamageType(type.registry().getPath(), 0.1f));
        });
    }

    public static void genDirectDamageConvert(HolderLookup.Provider lookup, Map<Identifier, EntityDirectDamage.OtherDamageConvert> out) {
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "fireball_convert_fire"),
                new EntityDirectDamage.OtherDamageConvert(EntityFlagValue.type(EntityTypes.FIREBALL),
                        DamageConvert.fromFire(false, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "small_fireball_convert_fire"),
                new EntityDirectDamage.OtherDamageConvert(EntityFlagValue.type(EntityTypes.SMALL_FIREBALL),
                        DamageConvert.fromFire(false, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "snowball_convert_fire"),
                new EntityDirectDamage.OtherDamageConvert(EntityFlagValue.type(EntityTypes.SNOWBALL),
                        DamageConvert.fromCold(false, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "dragon_fireball_convert_fire"),
                new EntityDirectDamage.OtherDamageConvert(EntityFlagValue.type(EntityTypes.DRAGON_FIREBALL),
                        DamageConvert.fromChaos(false, 1)));
    }

    /**
     * 装备类型权重树（{@code ItemWeight}）注册。
     *
     * <p>契约（与 {@code LoadEvent} 的加载口径一一对应）：</p>
     * <ul>
     *   <li><b>父节点</b>：用「类型键」声明直接子类别（{@link ItemWeight#from(String, int)}）——
     *       解析时由子类别键反查父节点，逐层向上。</li>
     *   <li><b>叶节点</b>：用<b>显式载体物品</b>声明本类别成员（{@link ItemWeight#from(net.minecraft.core.Holder, int)}）。
     *       不用物品标签声明——标签由内容侧逐物品打标，易漏（多个类别标签当前为空），
     *       且同一标签常横跨多个类别（如 {@code swords} 同时落在单手 / 双手），解析不可确定。</li>
     *   <li><b>类型键唯一</b>：类型键取 {@code type.json} 的父目录名，故单手 / 双手下的同名子类
     *       （sword / axe / mace）必须写成 {@code one_hand_sword} / {@code two_hand_sword} 等，
     *       否则两者互相覆盖。</li>
     * </ul>
     *
     * <p>解析结果（{@code ModGenerator#getItemType}）被写入 {@code EquipmentState.types}，
     * 之后的一切类别判定（是否武器 / 护甲 / 双手 / 胸甲、词缀池路径、基底读取分类）都读这份缓存，
     * 不再回查标签——即「类型由 ItemWeight 声明、在缓存时一并解析并存入 State，取代标签的地位」。</p>
     */
    public static void registerItemType(BootstrapContext<ItemInstance> bootstrap) {
        ItemGenHelper.type(bootstrap, EquipmentGen.EQUIPMENT,
                ItemGenHelper.children(EquipmentGen.WEAPON, EquipmentGen.ARMOR, EquipmentGen.ACCESSORY,
                        EquipmentGen.FLASK));
        ItemGenHelper.type(bootstrap, EquipmentGen.WEAPON,
                ItemGenHelper.children(EquipmentGen.MAIN_HAND, EquipmentGen.OFF_HAND));
        ItemGenHelper.type(bootstrap, EquipmentGen.ARMOR,
                ItemGenHelper.children(EquipmentGen.CHEST, EquipmentGen.FEET, EquipmentGen.HEAD, EquipmentGen.LEGS));
        ItemGenHelper.type(bootstrap, EquipmentGen.ACCESSORY,
                ItemGenHelper.children(EquipmentGen.AMULET, EquipmentGen.RING, EquipmentGen.BELT,
                        EquipmentGen.CHARM));
        // 药剂族群：类别层（flask）→ 具体类别（life_flask / mana_flask）。
        // 与装备同构，使词缀池按 item class 门控（池身份 = type.json 的父目录名）对药剂同样成立。
        ItemGenHelper.type(bootstrap, EquipmentGen.FLASK_PATH,
                ItemGenHelper.children(EquipmentGen.LIFE_FLASK, EquipmentGen.MANA_FLASK));
        ItemGenHelper.type(bootstrap, EquipmentGen.WEAPON + EquipmentGen.PATH_SEP + EquipmentGen.MAIN_HAND,
                ItemGenHelper.children(EquipmentGen.ONE_HAND, EquipmentGen.TWO_HAND));
        ItemGenHelper.type(bootstrap, EquipmentGen.WEAPON + EquipmentGen.PATH_SEP + EquipmentGen.OFF_HAND,
                ItemGenHelper.children(EquipmentGen.QUIVER, EquipmentGen.SHIELD, EquipmentGen.BUCKLER, EquipmentGen.FOCI));
        // 单手 / 双手类别：声明其下的具体武器子类（类型键，非标签）
        ItemGenHelper.type(bootstrap, EquipmentGen.WEAPON + EquipmentGen.PATH_SEP + EquipmentGen.MAIN_HAND
                        + EquipmentGen.PATH_SEP + EquipmentGen.ONE_HAND,
                ItemGenHelper.children(EquipmentGen.CLAW, EquipmentGen.DAGGER, EquipmentGen.ONE_HAND_SWORD,
                        EquipmentGen.ONE_HAND_AXE, EquipmentGen.ONE_HAND_MACE, EquipmentGen.SCEPTRE,
                        EquipmentGen.SPEAR, EquipmentGen.FLAIL, EquipmentGen.WAND));
        ItemGenHelper.type(bootstrap, EquipmentGen.WEAPON + EquipmentGen.PATH_SEP + EquipmentGen.MAIN_HAND
                        + EquipmentGen.PATH_SEP + EquipmentGen.TWO_HAND,
                ItemGenHelper.children(EquipmentGen.BOW, EquipmentGen.STAFF, EquipmentGen.TWO_HAND_SWORD,
                        EquipmentGen.TWO_HAND_AXE, EquipmentGen.TWO_HAND_MACE, EquipmentGen.QUARTERSTAFF,
                        EquipmentGen.CROSS_BOW, EquipmentGen.TRAP, EquipmentGen.TALISMAN));

        // ---- 叶类别：一类别 = 一载体物品（显式声明，取代标签）----
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_CLAW, ItemGenHelper.items(ItemRegistry.CLAW));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_DAGGER, ItemGenHelper.items(ItemRegistry.DAGGER));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_ONE_HAND_SWORD, ItemGenHelper.items(ItemRegistry.ONE_HAND_SWORD));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_ONE_HAND_AXE, ItemGenHelper.items(ItemRegistry.ONE_HAND_AXE));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_ONE_HAND_MACE, ItemGenHelper.items(ItemRegistry.ONE_HAND_MACE));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_SCEPTRE, ItemGenHelper.items(ItemRegistry.SCEPTRE));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_SPEAR, ItemGenHelper.items(ItemRegistry.SPEAR));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_FLAIL, ItemGenHelper.items(ItemRegistry.FLAIL));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_WAND, ItemGenHelper.items(ItemRegistry.WAND));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_BOW, ItemGenHelper.items(ItemRegistry.BOW));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_STAFF, ItemGenHelper.items(ItemRegistry.STAFF));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_TWO_HAND_SWORD, ItemGenHelper.items(ItemRegistry.TWO_HAND_SWORD));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_TWO_HAND_AXE, ItemGenHelper.items(ItemRegistry.TWO_HAND_AXE));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_TWO_HAND_MACE, ItemGenHelper.items(ItemRegistry.TWO_HAND_MACE));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_QUARTERSTAFF, ItemGenHelper.items(ItemRegistry.QUARTERSTAFF));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_CROSS_BOW, ItemGenHelper.items(ItemRegistry.CROSS_BOW));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_TRAP, ItemGenHelper.items(ItemRegistry.TRAP));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_TALISMAN, ItemGenHelper.items(ItemRegistry.TALISMAN));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_QUIVER, ItemGenHelper.items(ItemRegistry.QUIVER));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_SHIELD, ItemGenHelper.items(ItemRegistry.SHIELD));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_BUCKLER, ItemGenHelper.items(ItemRegistry.BUCKLER));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_FOCI, ItemGenHelper.items(ItemRegistry.FOCI));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_CHEST, ItemGenHelper.items(ItemRegistry.CHEST));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_LEGS, ItemGenHelper.items(ItemRegistry.LEGS));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_FEET, ItemGenHelper.items(ItemRegistry.FEET));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_HEAD, ItemGenHelper.items(ItemRegistry.HEAD));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_AMULET, ItemGenHelper.items(ItemRegistry.AMULET));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_RING, ItemGenHelper.items(ItemRegistry.RING));
        ItemGenHelper.type(bootstrap, EquipmentGen.TP_BELT, ItemGenHelper.items(ItemRegistry.BELT));
        // 药剂 / 护符叶类别：一类别 = 一载体物品（与装备叶类别同口径）。
        ItemGenHelper.type(bootstrap, EquipmentGen.LIFE_FLASK_PATH, ItemGenHelper.items(ItemRegistry.LIFE_FLASK));
        ItemGenHelper.type(bootstrap, EquipmentGen.MANA_FLASK_PATH, ItemGenHelper.items(ItemRegistry.MANA_FLASK));
        ItemGenHelper.type(bootstrap, EquipmentGen.CHARM_PATH, ItemGenHelper.items(
                ItemRegistry.RUBY_CHARM, ItemRegistry.SAPPHIRE_CHARM, ItemRegistry.TOPAZ_CHARM,
                ItemRegistry.AMETHYST_CHARM, ItemRegistry.DOUSING_CHARM, ItemRegistry.THAWING_CHARM,
                ItemRegistry.GROUNDING_CHARM, ItemRegistry.STAUNCHING_CHARM, ItemRegistry.ANTIDOTE_CHARM,
                ItemRegistry.STONE_CHARM, ItemRegistry.SILVER_CHARM, ItemRegistry.GOLDEN_CHARM));
        EquipmentGen.registerEquipment(bootstrap);
        EquipmentGen.registerEquipmentTemplate(bootstrap);
        EquipmentGen.registerMods(bootstrap);
    }

    /**
     * 装备 / 持握规则（{@code EquipRuleDef}）注册：
     * <ul>
     *   <li>{@code other_hand_conflict}：持 sceptre 时另一手不可持 talisman（反之亦然）。</li>
     *   <li>{@code both_hands_occupancy}：双手武器（类型键 {@code two_hand}）占双槽。</li>
     * </ul>
     * 类别身份一律为 <b>类型键</b>（{@code ModifiableState.types()} 中的叶类型键，见 {@link EquipmentGen}），
     * 由 {@link EquipRules} 引擎经 {@code ItemTypeChecker} 求值；不再依赖任何 item tag，
     * 与「类型由 ItemWeight 声明、缓存入 State、取代标签」的迁移口径一致。
     */
    public static void registerEquipRule(BootstrapContext<EquipRuleDef> bootstrap) {
        bootstrap.register(ResourceKey.create(LoadEvent.EQUIP_RULE_KEY,
                        Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "other_hand_conflict")),
                EquipRuleDef.otherHandConflict(
                        EquipmentGen.SCEPTRE,
                        EquipmentGen.TALISMAN));
        bootstrap.register(ResourceKey.create(LoadEvent.EQUIP_RULE_KEY,
                        Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "both_hands_occupancy")),
                EquipRuleDef.occupiesBothHands(
                        EquipmentGen.TWO_HAND,
                        List.of()));
    }

    /**
     * 生成 fire_resistance / resistance 的抗性加成条目。
     *
     * <p><b>这是产物生成，不是运行时注册。</b>下面的 {@code StatGen.modRead} 只负责把条目写进
     * {@code stat/entry/effectmod_*.json} 产物，供 {@code data/coe/coe/effect/*.json} 里的
     * {@code "ref": "coe:effectmod_*} 解析 —— 该目录由手工维护，没有 provider 写它。
     *
     * <p>运行时真正生效的是 {@link LoadEvent#EFFECT_MODIFIER_MAP}：它由 {@code LoadEvent.loadEffectModifier}
     * 按资源前缀 {@code coe/effect} <b>目录扫描</b>填充，消费方是
     * {@code EffectEvent.LivingMobEffectAddedEvent}。因此这里<b>不能</b>把条目省掉，否则
     * {@code effect/*.json} 的 ref 解析失败、抗性加成静默失效。
     *
     * <p>原先此处另有两处 {@code bootstrap.register(EffectModifier...)}，把同一份数据又塞进
     * {@link LoadEvent#EFFECT_MODIFIER_KEY} 注册表。该注册表全树零读取方（无任何
     * {@code RegistryHolder.get} / {@code getOrThrow}），是死数据，已移除；注册表声明本身保留。
     */
    public static void registerEffectModifier() {
        // RESISTANCES 为 0~1 比例属性（缝#39）：30 指 30%，须写 0.30，否则被 MAX_RESISTANCES 钳到 0.75。
        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "effectmod_fire_resist_provide_30"),
                AttributeRegistry.RESISTANCES, 0.30, AttributeModifier.Operation.ADD_VALUE, GateTag.FIRE);
        // 同上（缝#39）：5 指 5%，须写 0.05。
        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "effectmod_resist_provide_fire_5"), AttributeRegistry.RESISTANCES, 0.05, AttributeModifier.Operation.ADD_VALUE, GateTag.FIRE);
        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "effectmod_resist_provide_cold_5"), AttributeRegistry.RESISTANCES, 0.05, AttributeModifier.Operation.ADD_VALUE, GateTag.COLD);
        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "effectmod_resist_provide_lightning_5"), AttributeRegistry.RESISTANCES, 0.05, AttributeModifier.Operation.ADD_VALUE, GateTag.LIGHTNING);
    }

    public static void genRarity(HolderLookup.Provider lookup, Map<Identifier, LivingModifier> out) {
        // 稀有度属性加成数据（由 RARITY_DATA 附件持有）：统一走 Stat 路径（与 LivingModifier 同构）。
        // 已转 Provider：原 datapack 注册表被移除，改由 CoeDataProvider 写 data/coe/living/rarity/<id>.json，
        // LoadEvent.loadRarityData 按同前缀目录扫描填充（路径与 KEY.identifier().getPath() 同源）。
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, Rarity.COMMON.getSerializedName()),
                LivingModifier.from(List.of(), List.of()));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, Rarity.MAGIC.getSerializedName()),
                LivingModifier.from(List.of(
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_magic_max_health"), Attributes.MAX_HEALTH, 0.87, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_magic_scale"), Attributes.SCALE, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_magic_movement_speed"), Attributes.MOVEMENT_SPEED, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_magic_attack_speed"), Attributes.ATTACK_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_magic_cast_speed"), AttributeRegistry.CAST_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                ), List.of()));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, Rarity.RARE.getSerializedName()),
                LivingModifier.from(List.of(
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_rare_max_health"), Attributes.MAX_HEALTH, 3.63, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_rare_scale"), Attributes.SCALE, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_rare_movement_speed"), Attributes.MOVEMENT_SPEED, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_rare_attack_speed"), Attributes.ATTACK_SPEED, 0.33, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_rare_cast_speed"), AttributeRegistry.CAST_SPEED, 0.33, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                ), List.of()));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, Rarity.UNIQUE.getSerializedName()),
                LivingModifier.from(List.of(
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_unique_max_health"), Attributes.MAX_HEALTH, 5.98, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_unique_scale"), Attributes.SCALE, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_unique_movement_speed"), Attributes.MOVEMENT_SPEED, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_unique_attack_speed"), Attributes.ATTACK_SPEED, 0.33, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rarity_unique_cast_speed"), AttributeRegistry.CAST_SPEED, 0.33, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                ), List.of()));
    }

    public static void genModifier(HolderLookup.Provider lookup, Map<Identifier, LivingModifier> out) {
        // 无标签属性加成（原版 / 通用属性，如护甲、攻击伤害）经 StatGen.modRead 注册，此处仅 Read 引用。
        // 元素抗性：归一化 RESISTANCES 属性 + 元素作用域标签（fire/cold/lightning/chaos），与词缀系统同源。
        // amt 入参为**百分点**（20.0 = +20%）；RESISTANCES 是 0~1 比例属性（MAX_RESISTANCES=0.75，
        // DamageAction 以 denom=1-r 计算减伤），故写入前必须 /100。
        // 缝#39：此前直接写 amt（20.0）→ r=20 被 Math.min(r, max) 钳到 0.75，所有带此修饰的怪物
        // 抗性恒顶格 75%（设计值 10%/20%），元素流派伤害被系统性压低。
        // 已转 Provider：原 datapack 注册表移除，改由 CoeDataProvider 写 data/coe/living/global/<id>.json；
        // LoadEvent.loadModifier 按同前缀目录扫描填充。biomeLookup 改从传入的 Provider 取。
        java.util.function.BiFunction<Double, String, StatEntry> resist = (amt, tag) -> {
            int pct = (int) Math.round(amt);
            return StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "livingmod_resist_" + tag + "_" + pct),
                    AttributeRegistry.RESISTANCES, amt / 100.0, AttributeModifier.Operation.ADD_VALUE, tag);
        };
        // 类型安全 lookup（EntityFlagValue 的 tag 持有集需从 Provider 取注册表查找，不再走字符串解析）。
        // 注意：HolderLookup.Provider.lookup 返回 Optional（注册表可能缺席），需解包；RegistryLookup<T> 即 HolderGetter<T> 子型。
        HolderGetter<Biome> biomeLookup = lookup.lookup(Registries.BIOME)
                .orElseThrow(() -> new IllegalStateException("Biome registry unavailable in lookup provider"));
        // 转伤：原 DamageConvert 的「物理 + 各元素权重混合」语义，收编为 Stat 的 damage_convert 条目——
        // 以目标元素占总权重的份额作为「把 physical 转为该元素」的比例。
        // 运行期 DamagePipeline#applyDamageTypeConversion 仅在 REPLACE 下扣减来源槽位；
        // 若用 GAIN 则不扣源、只叠加目标 → 总伤变成 1+ratio 倍（100% 条目即双倍伤害），双重计入。
        java.util.function.Function<DamageConvert, StatEntry> convert = dc -> {
            int phys = dc.physical().orElse(0);
            String to;
            int target;
            if (dc.fire().isPresent()) { to = GateTag.FIRE; target = dc.fire().get(); }
            else if (dc.cold().isPresent()) { to = GateTag.COLD; target = dc.cold().get(); }
            else if (dc.chaos().isPresent()) { to = GateTag.CHAOS; target = dc.chaos().get(); }
            else if (dc.light().isPresent()) { to = GateTag.LIGHTNING; target = dc.light().get(); }
            else { to = GateTag.PHYSICAL; target = phys; }
            double ratio = (phys + target) == 0 ? 0.0 : (double) target / (phys + target);
            int pct = (int) Math.round(ratio * 100);
            return StatGen.read(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "livingmod_convert_phys_to_" + to + "_" + pct),
                    DamageConvertStatEffect.entry(GateTag.PHYSICAL, to, DamageConvertStatEffect.Mode.REPLACE, ratio));
        };

        // Dimension
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "dimension_nether_fire_resistance"),
                LivingModifier.from(
                        List.of(resist.apply(20.0, GateTag.FIRE),
                                convert.apply(DamageConvert.fromFire(false, 3, 7))),
                        List.of(EntityFlagValue.dimension(Level.NETHER))
                ));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "dimension_end_element_resistance"),
                LivingModifier.from(
                        List.of(
                                resist.apply(10.0, GateTag.ELEMENTAL),
                                convert.apply(DamageConvert.fromChaos(false, 3, 7))),
                        List.of(EntityFlagValue.dimension(Level.END))
                ));
        // Biome
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "biome_hot_fire_resistance"),
                LivingModifier.from(
                        List.of(resist.apply(20.0, GateTag.FIRE),
                                convert.apply(DamageConvert.fromFire(false, 4, 6))),
                        List.of(EntityFlagValue.biome(biomeLookup, Tags.Biomes.IS_HOT)))
        );

        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "biome_cold_cold_resistance"),
                LivingModifier.from(
                        List.of(resist.apply(20.0, GateTag.COLD),
                                convert.apply(DamageConvert.fromCold(false, 4, 6))),
                        List.of(EntityFlagValue.biome(biomeLookup, Tags.Biomes.IS_COLD)))
        );
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "biome_swamp_chaos_resistance"),
                LivingModifier.from(
                        List.of(resist.apply(20.0, GateTag.CHAOS),
                                convert.apply(DamageConvert.fromChaos(false, 4, 6))),
                        List.of(EntityFlagValue.biome(biomeLookup, Tags.Biomes.IS_SWAMP)))
        );
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "biome_forest_armor"),
                LivingModifier.from(
                        List.of(StatGen.modRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "livingmod_armor_4"), Attributes.ARMOR, 4.0, AttributeModifier.Operation.ADD_VALUE)),
                        List.of(EntityFlagValue.biome(biomeLookup, Tags.Biomes.IS_FOREST)))
        );
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "biome_cave_attack_damage"),
                LivingModifier.from(
                        // 攻击伤害加成需带作用域标签（此处为近战 melee），与 RESISTANCES 等"属性+Stat标签"范式一致
                        List.of(StatGen.read(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "livingmod_cave_attack_damage"),
                                StatEntry.of(new ExpressionCodec.Value(Either.left(3.0)), java.util.List.of(),
                                        new ModifierStatEffect(Attributes.ATTACK_DAMAGE,
                                                Either.left(AttributeModifier.Operation.ADD_VALUE),
                                                ModifierStatEffect.Mode.GAIN, GateExpr.of(Set.of(GateTag.MELEE)))))),
                        List.of(EntityFlagValue.biome(biomeLookup, Tags.Biomes.IS_CAVE)))
        );
        // EntityType
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "entity_stray_cold_resistance"),
                LivingModifier.from(
                        List.of(resist.apply(10.0, GateTag.COLD),
                                convert.apply(DamageConvert.fromCold(true, 1))),
                        List.of(EntityFlagValue.type(EntityTypes.STRAY))
                ));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "entity_bogged_chaos_resistance"),
                LivingModifier.from(
                        List.of(resist.apply(10.0, GateTag.CHAOS),
                                convert.apply(DamageConvert.fromChaos(true, 1))),
                        List.of(EntityFlagValue.type(EntityTypes.BOGGED))
                ));
    }

    public static void genPotionDamageConvert(HolderLookup.Provider lookup, Map<Identifier, EntityDirectDamage.PotionDamageConvert> out) {
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "chaos_convert_poison"),
                EntityDirectDamage.PotionDamageConvert.from(MobEffects.POISON,
                        DamageConvert.fromChaos(false, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "cold_convert_slowness"),
                EntityDirectDamage.PotionDamageConvert.from(MobEffects.SLOWNESS,
                        DamageConvert.fromCold(false, 1)));
    }

    public static void genArrowDamageConvert(HolderLookup.Provider lookup, Map<Identifier, EntityDirectDamage.ArrowDamageConvert> out) {
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "chaos_convert_poison"),
                EntityDirectDamage.ArrowDamageConvert.from(MobEffects.POISON,
                        DamageConvert.fromChaos(false, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "cold_convert_slowness"),
                EntityDirectDamage.ArrowDamageConvert.from(MobEffects.SLOWNESS,
                        DamageConvert.fromCold(false, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "fire_convert_is_fire"),
                EntityDirectDamage.ArrowDamageConvert.from(EntityDirectDamage.ArrowDamageConvert.ArrowState.IS_FIRE,
                        DamageConvert.fromFire(false, 3, 7)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "fire_convert_in_lava"),
                EntityDirectDamage.ArrowDamageConvert.from(EntityDirectDamage.ArrowDamageConvert.ArrowState.IN_LAVA,
                        DamageConvert.fromFire(false, 2, 8)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "cold_convert_in_water"),
                EntityDirectDamage.ArrowDamageConvert.from(EntityDirectDamage.ArrowDamageConvert.ArrowState.IN_WATER,
                        DamageConvert.fromCold(false, 4, 6)));
    }

    public static void genDamageConvert(HolderLookup.Provider lookup, Map<Identifier, DamageTypeConvert> out) {
        // 已转 Provider：原 datapack 注册表移除，改由 CoeDataProvider 写 data/coe/damage_convert/damage_type/<id>.json；
        // LoadEvent 按 DAMAGE_CONVERT_KEY.identifier().getPath() 同前缀扫描填充。damageTypeHolderGetter 改从 Provider 解包取。
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "physical_convert_is_physical"),
                DamageTypeConvert.from(Tags.DamageTypes.IS_PHYSICAL,
                        DamageConvert.fromPhysical(false, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "chaos_convert_is_poison"),
                DamageTypeConvert.from(Tags.DamageTypes.IS_POISON,
                        DamageConvert.fromChaos(false, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "fire_convert_is_fire"),
                DamageTypeConvert.from(DamageTypeTags.IS_FIRE,
                        DamageConvert.fromFire(false, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "cold_convert_is_freezing"),
                DamageTypeConvert.from(DamageTypeTags.IS_FREEZING,
                        DamageConvert.fromCold(false, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "light_convert_is_light"),
                DamageTypeConvert.from(DamageTypeTags.IS_LIGHTNING,
                        DamageConvert.fromLight(false, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "physical_convert_bleeding"),
                DamageTypeConvert.from(DamageTypeRegistry.BLEEDING,
                        DamageConvert.fromPhysical(true, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "fire_convert_ignite"),
                DamageTypeConvert.from(DamageTypeRegistry.IGNITE,
                        DamageConvert.fromFire(true, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "chaos_convert_poision"),
                DamageTypeConvert.from(DamageTypeRegistry.POISON,
                        DamageConvert.fromChaos(true, 1)));
        // 拆分 DoT 输出伤害类型 → 各自来源伤害槽位（物理/混沌/火/冰/闪电），使每份额外伤害被对应抗性/护甲减免。
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "dot_physical_convert"),
                DamageTypeConvert.from(DamageTypeRegistry.DOT_PHYSICAL,
                        DamageConvert.fromPhysical(true, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "dot_chaos_convert"),
                DamageTypeConvert.from(DamageTypeRegistry.DOT_CHAOS,
                        DamageConvert.fromChaos(true, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "dot_fire_convert"),
                DamageTypeConvert.from(DamageTypeRegistry.DOT_FIRE,
                        DamageConvert.fromFire(true, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "dot_cold_convert"),
                DamageTypeConvert.from(DamageTypeRegistry.DOT_COLD,
                        DamageConvert.fromCold(true, 1)));
        out.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "dot_light_convert"),
                DamageTypeConvert.from(DamageTypeRegistry.DOT_LIGHT,
                        DamageConvert.fromLight(true, 1)));
    }

    public static void registerExpression(BootstrapContext<MathExpression> bootstrap) {
        bootstrap.register(ResourceKey.create(LoadEvent.EXPRESSION_KEY,
                        Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "per_level_add_value")),
                new ExpressionCodec.BinaryOperation.Add(ExpressionCodec.Variable.local("base"),
                        new ExpressionCodec.BinaryOperation.Multiply(
                                new ExpressionCodec.BinaryOperation.Divide(ExpressionCodec.Variable.from("skill.level"),
                                        ExpressionCodec.Variable.local("per")),
                                ExpressionCodec.Variable.local("add"))));
        bootstrap.register(ResourceKey.create(LoadEvent.EXPRESSION_KEY,
                        Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "per_quality_add_value")),
                new ExpressionCodec.BinaryOperation.Add(ExpressionCodec.Variable.local("base"),
                        new ExpressionCodec.BinaryOperation.Multiply(
                                new ExpressionCodec.BinaryOperation.Divide(ExpressionCodec.Variable.from("quality"),
                                        ExpressionCodec.Variable.local("per")),
                                ExpressionCodec.Variable.local("add"))));
        // 蓄力缩放模板：base * (min + (1 - min) * charge)。
        // charge 为上下文变量（0~1 蓄力比例），min 为最低倍率（0 蓄力时的倍率下限）。
        bootstrap.register(ResourceKey.create(LoadEvent.EXPRESSION_KEY,
                        Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "charge_scale")),
                new ExpressionCodec.BinaryOperation.Multiply(ExpressionCodec.Variable.local("base"),
                        new ExpressionCodec.BinaryOperation.Add(ExpressionCodec.Variable.local("min"),
                                new ExpressionCodec.BinaryOperation.Multiply(
                                        new ExpressionCodec.BinaryOperation.Subtract(
                                                new ExpressionCodec.Constant(1),
                                                ExpressionCodec.Variable.local("min")),
                                        ExpressionCodec.Variable.local("charge")))));
        Conversions.register(bootstrap,LoadEvent.EXPRESSION_KEY);
        // 等级查表模板：变量固定为 skill.level，运行时按 Read 注入的各等级值查表。
        // 每级的值通过 Read 的 values map 注入（键 "1"~"40"），未命中则取 def。
        {
            List<ExpressionCodec.LevelTable.TableEntry> table = new ArrayList<>();
            for (int i = 1; i <= 40; i++) {
                table.add(new ExpressionCodec.LevelTable.TableEntry(
                        new ExpressionCodec.Constant(i),
                        ExpressionCodec.Variable.local(String.valueOf(i))));
            }
            bootstrap.register(ResourceKey.create(LoadEvent.EXPRESSION_KEY,
                            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "level_table")),
                    new ExpressionCodec.LevelTable(ExpressionCodec.Variable.from("skill.level"),
                            table, ExpressionCodec.Variable.local("def")));

        }
    }

    // ---- 以下 genXxx 为已转 Provider 的薄包装：复用既有 registerXxx(BootstrapContext<T>) 生成器，
    // 经 CapturingBootstrap 把注册调用捕获进 Map，落盘路径由 CoeDataProvider 按 registryPath 对齐。

    public static void genExpression(HolderLookup.Provider lookup, Map<Identifier, MathExpression> out) {
        registerExpression(new CapturingBootstrap<>(lookup, LoadEvent.EXPRESSION_KEY, out));
    }

    public static void genItemType(HolderLookup.Provider lookup, Map<Identifier, ItemInstance> out) {
        registerItemType(new CapturingBootstrap<>(lookup, LoadEvent.ITEM_EQUIPMENT_KEY, out));
    }

    public static void genSkill(HolderLookup.Provider lookup, Map<Identifier, SkillEntry> out) {
        GemGen.registerSkill(new CapturingBootstrap<>(lookup, LoadEvent.ITEM_KEY, out));
    }

    public static void genSupportGem(HolderLookup.Provider lookup, Map<Identifier, SupportGem> out) {
        GemGen.registerSupportGem(new CapturingBootstrap<>(lookup, LoadEvent.ITEM_SUPPORT_GEM_KEY, out));
    }

    public static void genSpirit(HolderLookup.Provider lookup, Map<Identifier, SkillEntry> out) {
        GemGen.registerSpirit(new CapturingBootstrap<>(lookup, LoadEvent.ITEM_SPIRIT_KEY, out));
    }

    /** 共享通用预设（generic_*）：落 data/coe/skill/template/，与 LoadEvent 的 coe/skill/template 扫描同源。 */
    public static void genSkillTemplate(HolderLookup.Provider lookup, Map<Identifier, SkillEntry> out) {
        SkillPresets.registerTemplates(new CapturingBootstrap<>(lookup, SkillPresets.ITEM_TEMPLATE_KEY, out));
    }

    /** 宝石专属预设（preset_*）：落 data/coe/skill/preset/，与 LoadEvent 的 coe/skill/preset 扫描同源。 */
    public static void genSkillPreset(HolderLookup.Provider lookup, Map<Identifier, SkillEntry> out) {
        SkillPresets.registerPresets(new CapturingBootstrap<>(lookup, SkillPresets.ITEM_PRESET_KEY, out));
    }

    public static void genEntityMod(HolderLookup.Provider lookup, Map<Identifier, LivingMod.Mod> out) {
        EntityGen.registerEntityMod(new CapturingBootstrap<>(lookup, LoadEvent.LIVING_MOD_KEY, out));
    }

    public static void genEnchantModifier(HolderLookup.Provider lookup, Map<Identifier, EnchantModifier> out) {
        EnchantModifierGen.registerEnchantModifier(new CapturingBootstrap<>(lookup, LoadEvent.ENCHANT_MODIFIER_KEY, out));
    }

    // ---- Task #391：装备规则（equip_rule）仍走 Provider；饰品 / 药剂 / 护符槽的 datagen 已停用（相关 register*/gen* 已删除）----

    public static void genEquipRule(HolderLookup.Provider lookup, Map<Identifier, EquipRuleDef> out) {
        registerEquipRule(new CapturingBootstrap<>(lookup, LoadEvent.EQUIP_RULE_KEY, out));
    }

}