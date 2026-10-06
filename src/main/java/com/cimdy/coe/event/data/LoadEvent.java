package com.cimdy.coe.event.data;

import com.cimdy.coe.network.sync.ProgressionSyncSender;
import com.cimdy.coe.network.sync.SlotSyncSender;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.entity.LivingMod;
import com.cimdy.coe.data.codec.entity.LivingModifier;
import com.cimdy.coe.data.codec.item.ItemWeight;
import com.cimdy.coe.data.codec.item.mod.value.ModPreset;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.mod.value.TierPreset;
import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.mod.identity.ModConflict;
import com.cimdy.coe.data.codec.item.ItemInstance;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentRead;
import com.cimdy.coe.data.codec.item.equipment.EquipmentModPreset;
import com.cimdy.coe.data.codec.item.registry.ItemDataRegistry;
import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeHierarchy;
import com.cimdy.coe.data.codec.item.RuneRegistry;
import com.cimdy.coe.data.codec.item.essence.EssenceRegistry;
import com.cimdy.coe.data.codec.support.stat.tag.GateTagRegistry;
import com.cimdy.coe.data.codec.support.stat.condition.StatCondition;
import com.cimdy.coe.data.codec.support.stat.condition.StatConditionTypes;
import com.cimdy.coe.data.codec.progression.ProgressionData;
import com.cimdy.coe.data.codec.vanilla.EffectModifier;
import com.cimdy.coe.data.codec.vanilla.EnchantModifier;
import com.cimdy.coe.data.codec.vanilla.DamageTypeConvert;
import com.cimdy.coe.data.codec.vanilla.EntityDirectDamage;
import com.cimdy.coe.data.codec.entity.EntityDefs;
import com.cimdy.coe.data.codec.entity.render.EntityRenderDefs;
import com.cimdy.coe.data.codec.entity.render.SkillSkinDefs;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.skill.core.SkillForms;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.SkillPhasePlayer;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.support.damage.DamageTypes;
import com.cimdy.coe.data.codec.skill.gem.GemColor;
import com.cimdy.coe.data.codec.skill.support.SupportGem;
import com.cimdy.coe.data.codec.stronghold.task.site.RecipeSiteBlocks;
import com.cimdy.coe.data.codec.support.meta.DamageConvert;
import com.cimdy.coe.data.codec.support.meta.DataSourceContext;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityFlagValue;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.tag.StatCategory;
import com.cimdy.coe.data.codec.item.equipment.AccessorySlotDef;
import com.cimdy.coe.data.codec.item.equipment.FlaskSlotDef;
import com.cimdy.coe.data.codec.item.equipment.EquipRuleDef;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffectTypes;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.duration.StatDuration;
import com.cimdy.coe.data.codec.support.stat.duration.StatDurationTypes;
import com.cimdy.coe.data.codec.support.stat.target.StatTargetsTypes;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentTemplate;
import com.cimdy.coe.data.codec.item.equipment.UniqueDef;
import com.cimdy.coe.data.codec.talent.tree.TalentTreeModelLoader;
import com.cimdy.coe.data.gen.provider.StatEntryProvider;
import com.cimdy.coe.data.gen.stat.StatGen;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = CraftOfExile.MODID)
public class LoadEvent {
    public static final ResourceKey<Registry<MathExpression>> EXPRESSION_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/expression"));
    /**
     * 技能宝石注册表：元素为 {@link SkillEntry} 而非 {@link Skill}——宝石既可写成
     * 内联完整定义，也可写成 {@code SkillRead(ref, vars)} 引用某个通用预设。
     * datagen 侧（{@code Skill*Gen}）因此能直接产出引用型 JSON。
     */
    public static final ResourceKey<Registry<SkillEntry>> ITEM_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/item/gem/skill"));
    public static final ResourceKey<Registry<SupportGem>> ITEM_SUPPORT_GEM_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/item/gem/support"));
    /** 精魂宝石注册表：同 {@link #ITEM_KEY}，元素为 {@link SkillEntry}（内联或引用形态）。 */
    public static final ResourceKey<Registry<SkillEntry>> ITEM_SPIRIT_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/item/gem/spirit"));
    public static final ResourceKey<Registry<ItemInstance>> ITEM_EQUIPMENT_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/item/equipment"));
    public static final ResourceKey<Registry<LivingModifier>> LIVING_RARITY_DATA_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/living/rarity"));
    public static final ResourceKey<Registry<LivingModifier>> LIVING_MODIFIER_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/living/global"));
    public static final ResourceKey<Registry<LivingMod.Mod>> LIVING_MOD_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/living/mod"));
    public static final ResourceKey<Registry<DamageTypeConvert>> DAMAGE_CONVERT_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/damage_convert/damage_type"));
    public static final ResourceKey<Registry<EntityDirectDamage.OtherDamageConvert>> OTHER_DAMAGE_CONVERT_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/damage_convert/direct_entity/other"));
    public static final ResourceKey<Registry<EntityDirectDamage.ArrowDamageConvert>> ARROW_DAMAGE_CONVERT_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/damage_convert/direct_entity/arrow"));
    public static final ResourceKey<Registry<EntityDirectDamage.PotionDamageConvert>> POTION_DAMAGE_CONVERT_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/damage_convert/direct_entity/potion"));
    public static final ResourceKey<Registry<EffectModifier>> EFFECT_MODIFIER_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/effect"));
    public static final ResourceKey<Registry<EnchantModifier>> ENCHANT_MODIFIER_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/enchant"));

    /**
     * 饰品槽定义的 reload 监听器标识。数据<b>不再</b>来自数据包
     * （原 {@code data/<ns>/coe/accessory_slot/<id>.json} 已停用并删除），改由服务端配置文件
     * {@code config/coe/slots.json} 经 {@code SlotConfigLoader} 加载；此处仅作为
     * {@code AddServerReloadListenersEvent.addListener} 的 key（用于排序 / 查重，与数据包路径无关）。
     */
    public static final ResourceKey<Registry<AccessorySlotDef>> ACCESSORY_SLOT_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/coe/accessory_slot"));
    /** 药剂槽定义的 reload 监听器标识（同 ACCESSORY_SLOT_KEY 口径，数据源为同一份 slots.json）。 */
    public static final ResourceKey<Registry<AccessorySlotDef>> FLASK_SLOT_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/coe/flask_slot"));
    /** 装备 / 持握规则表路径：{@code data/<ns>/coe/equip_rule/<id>.json}（同 ACCESSORY_SLOT_KEY 口径）。 */
    public static final ResourceKey<Registry<EquipRuleDef>> EQUIP_RULE_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/equip_rule"));

    /**
     * 完整技能定义（预设）重载监听器路径：{@code data/<ns>/coe/skill/<id>.json}。
     *
     * <p>每个文件是一份<b>完整的技能定义</b>（内联形态）或一条 {@link SkillRead} 引用；
     * 装载后按 id 进 {@link #SKILL_ENTRY_MAP}，供 {@code SkillRead(ref, vars)} 解析。
     * 「预设 / 技能类型」不是独立的一类注册——它就是一个完整技能，注册后拿到 id。
     */
    public static final Identifier SKILL_ENTRY_PATH = Identifier.parse("coe/skill");
    /** 共享通用预设目录（generic_*）：data/<ns>/coe/skill/template/<id>.json。 */
    public static final Identifier SKILL_TEMPLATE_PATH = Identifier.parse("coe/skill/template");
    /** 宝石专属预设目录（preset_*）：data/<ns>/coe/skill/preset/<id>.json。 */
    public static final Identifier SKILL_PRESET_PATH = Identifier.parse("coe/skill/preset");

    /** 完整技能定义表：id → {@link SkillEntry}（含内联定义与引用形态，供 ref 解析）。 */
    public static Map<Identifier, SkillEntry> SKILL_ENTRY_MAP = new HashMap<>();

    /** stat_entry 重载监听器标识（纯代码内建：数据不再从数据包读取，此处仅作监听器键名，对应路径无实际文件）。 */
    public static final Identifier STAT_ENTRY_PATH = Identifier.parse("coe/stat/entry");

    public static Map<Identifier, StatEntry> STAT_ENTRY_MAP = new HashMap<>();

    /** 装备基础模板表：id -> EquipmentTemplate（按类别声明基础属性/需求 ref，无默认值）。 */
    public static Map<Identifier, EquipmentTemplate> EQUIPMENT_TEMPLATE_MAP = new HashMap<>();

    /** 传奇装备（唯一）重载监听器标识（数据驱动：coe/unique/<baseItem>/<name>.json）。 */
    public static final Identifier UNIQUE_DEF_PATH = Identifier.parse("coe/unique");

    /** 传奇装备表：<baseItem>/<name> -> UniqueDef（读取 base 装备为自身基础，叠加词缀/签名/模型）。 */
    public static Map<Identifier, UniqueDef> UNIQUE_DEF_MAP = new HashMap<>();

    /** 装备读取表：<slot>/item/<name> -> EquipmentRead（路径 key 与 UniqueDef 的 baseItem 对齐，供传奇预设按路径解析具体基础装备）。 */
    public static Map<Identifier, EquipmentRead> EQUIPMENT_READ_MAP = new HashMap<>();

    /**
     * 标签层级数据包路径（重构 3-4）：父类别标签 → 子标签集合，供 {@link GateTag} 的上位包含匹配使用。
     * 内容缺失时 {@code GateTag} 自动回落内置默认表，故此数据为"可选覆盖"而非必需。
     */
    public static final Identifier STAT_TAG_CATEGORY_PATH = Identifier.parse("coe/stat_category");
    /** ADR-003 阶段1：伤害类型定义表路径（coe/damage_type/<id>.json）。 */
    public static final Identifier DAMAGE_TYPE_PATH = DamageTypes.PATH;

    /** 词缀领域标签映射重载监听器标识（D7-b 数据驱动来源：coe/stat_tag/*.json）。 */
    public static final Identifier STAT_TAG_PATH = Identifier.parse("coe/stat_tag");

    /** 词缀读取重载监听器标识（数据驱动：coe/affix/addition/<name>.json）。 */
    public static final Identifier ADDITION_PATH = Identifier.parse("coe/affix/addition");

    /** 词缀读取表：<name> -> AdditionType（按文件名注册，供 AdditionRead(ref) 惰性解析）。 */
    public static Map<Identifier, AdditionType> ADDITION_MAP = new HashMap<>();

    /** 词缀冲突组重载监听器标识（数据驱动：coe/affix/conflict/*.json）。 */
    public static final Identifier ADDITION_CONFLICT_PATH = Identifier.parse("coe/affix/conflict");

    // 物品相关数据已迁移至 ItemDataRegistry（可注入、可测试），此处仅保留非物品类静态 Map。
    public static Map<Identifier, MathExpression> EXPRESSION_MAP = new HashMap<>();
    
    /** 技能宝石注册表：按颜色分组的 id -> 技能条目（id 不含颜色段，颜色由外层 GemColor 承载）。 */
    public static Map<GemColor, Map<Identifier, SkillEntry>> ITEM_MAP = new HashMap<>();
    /** 辅助宝石注册表：按颜色分组的 id -> 辅助宝石定义（id 不含颜色段）。 */
    public static Map<GemColor, Map<Identifier, SupportGem>> ITEM_SUPPORT_GEM_MAP = new HashMap<>();
    /** 精魂宝石注册表：按颜色分组的 id -> 精魂技能条目（id 不含颜色段）。 */
    public static Map<GemColor, Map<Identifier, SkillEntry>> ITEM_SPIRIT_MAP = new HashMap<>();

    /** 跨颜色查找技能条目（id 不含颜色段）；返回 {@link SkillEntry}，消费方经 {@code resolve()} 取完整技能。 */
    public static SkillEntry getSkill(Identifier id) {
        for (Map<Identifier, SkillEntry> inner : ITEM_MAP.values())
            if (inner.containsKey(id)) return inner.get(id);
        return null;
    }
    /** 跨颜色查找辅助宝石定义（id 不含颜色段）。 */
    public static SupportGem getSupportGem(Identifier id) {
        for (Map<Identifier, SupportGem> inner : ITEM_SUPPORT_GEM_MAP.values())
            if (inner.containsKey(id)) return inner.get(id);
        return null;
    }
    /** 跨颜色查找精魂技能条目（id 不含颜色段）；返回 {@link SkillEntry}。 */
    public static SkillEntry getSpirit(Identifier id) {
        for (Map<Identifier, SkillEntry> inner : ITEM_SPIRIT_MAP.values())
            if (inner.containsKey(id)) return inner.get(id);
        return null;
    }

    /** 从含颜色段的路径名（如 red/culling_slam）解析 GemColor。 */
    private static GemColor parseColor(String name) {
        String colorSeg = name.contains("/") ? name.substring(0, name.indexOf("/")) : "red";
        return switch (colorSeg) {
            case "green" -> GemColor.GREEN;
            case "blue" -> GemColor.BLUE;
            default -> GemColor.RED;
        };
    }
    /** 从含颜色段的路径名（如 red/culling_slam）去掉颜色段，返回末段（culling_slam）。 */
    private static String stripColor(String name) {
        return name.contains("/") ? name.substring(name.indexOf("/") + 1) : name;
    }
    public static Map<Rarity, LivingModifier> LIVING_RARITY_DATA_MAP = new HashMap<>();
    public static Map<DamageTypeConvert.Damage, DamageConvert> LIVING_DAMAGE_CONVERT_MAP = new HashMap<>();
    public static List<LivingModifier> LIVING_MODIFIER_LIST = new ArrayList<>();
    public static Map<String, LivingMod.Mod> LIVING_MOD_MAP = new HashMap<>();
    public static Map<Holder<MobEffect>, List<StatEntry>> EFFECT_MODIFIER_MAP = new HashMap<>();
    /**
     * 外置 effect 段：{@code stat/effect/<id>.json} → {@link StatEffect} 实体。
     * 由 {@link com.cimdy.coe.data.gen.provider.StatEffectProvider} 落盘、此处按目录扫描加载，
     * 供 {@code Stat} 条目以 ref 引用解析。
     */
    public static Map<Identifier, StatEffect> STAT_EFFECT_MAP = new HashMap<>();
    /** 外置 targets 段：{@code stat/targets/<id>.json} → 选择器预设。 */
    public static Map<Identifier, StatTargets> STAT_TARGET_MAP = new HashMap<>();
    /**
     * 条件段（实体获取条件，{@link StatCondition}）：由 {@link com.cimdy.coe.data.codec.support.stat.entry.StatEntry#internCondition}
     * 在数据生成期登记内联条件为命名引用（mirror STAT_EFFECT_MAP / STAT_TARGET_MAP 的 anon intern 路径，
     * 非内容寻址落盘），运行时供 {@link com.cimdy.coe.data.codec.support.stat.entry.StatRead#condition()} 以 ref 解析。
     */
    public static Map<Identifier, StatCondition> STAT_CONDITION_MAP = new HashMap<>();

    /**
     * 外置 duration 段（Stat 条目第五部分 · 时长段）：{@code stat/duration/<id>.json} → 时长定义。
     * 由 {@link com.cimdy.coe.data.gen.provider.StatDurationProvider} 落盘、此处按目录扫描加载，
     * 供条目 {@code "duration": "read:coe:xxx"} 引用解析（与 STAT_EFFECT_MAP / STAT_TARGET_MAP /
     * STAT_CONDITION_MAP 同族：同为name-keyed 外置段，key 经 {@link #relId} 去前缀还原为
     * {@code coe:<path>}，与生成期 {@code StatDurationIds.id("<path>")} 字面一致）。
     */
    public static Map<Identifier, StatDuration> STAT_DURATION_MAP = new HashMap<>();
    public static Map<EntityDirectDamage.ArrowDamageConvert.EffectState, DamageConvert> ARROW_DAMAGE_CONVERT_MAP = new HashMap<>();
    public static Map<Holder<MobEffect>, DamageConvert> POTION_DAMAGE_CONVERT_MAP = new HashMap<>();
    public static Map<EntityFlagValue, DamageConvert> OTHER_DAMAGE_CONVERT_MAP = new HashMap<>();

    // ---- 饰品 / 药剂 / 护符槽 & 装备规则（Task #391 已转 Provider，由 CoeDataProvider 写 JSON，此处路径扫描填充）----
    /**
     * 饰品槽定义：groupId -> AccessorySlotDef（数据来自 config/coe/slots.json，经 SlotConfigLoader 加载）。
     * <b>volatile 快照</b>：由 {@code SlotConfigLoader.publish} 整体替换为不可变副本，绝不在原地
     * {@code clear()+putAll()}——读侧（含主线程 tick 路径）无锁且永远看到一份完整表，不会观察到中间态。
     */
    public static volatile Map<Identifier, AccessorySlotDef> ACCESSORY_SLOT_MAP = Map.of();
    /** 药剂槽定义：groupId -> FlaskSlotDef（同 ACCESSORY_SLOT_MAP 的 volatile 快照口径）。 */
    public static volatile Map<Identifier, FlaskSlotDef> FLASK_SLOT_MAP = Map.of();
    /** 装备 / 持握规则：id -> EquipRuleDef。 */
    public static Map<Identifier, EquipRuleDef> EQUIP_RULE_MAP = new HashMap<>();

    public static boolean RELOAD = false;

    @SubscribeEvent
    public static void AddServerReloadListenersEvent(AddServerReloadListenersEvent event) {
        RELOAD = true;
        // 标签层级最先加载：stat_entry 的父类别匹配（GateTag.match）与 fire 触发索引都依赖它，
        // 早于其余数据就位可避免首次重载期间用旧表建索引。缺失时 GateTag 自动回落内置表。
        event.addListener(STAT_TAG_CATEGORY_PATH,
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadStatCategory(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        // ADR-003 阶段1：伤害类型定义表（coe/damage_type/*.json）先于技能装载就位，
        // 使 loadSkill 的 damage_type 校验基于最新表；无数据包则回落内置默认（DamageTypes 静态初始化）。
        event.addListener(DAMAGE_TYPE_PATH,
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> DamageTypes.load(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(EXPRESSION_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadExpression(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        // 实体渲染定义必须先于实体定义加载：EntityDef 只是引用 render id，解析时按 id 查表。
        event.addListener(EntityRenderDefs.KEY,
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadEntityRender(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        // 技能皮肤：按技能 id 索引的外观覆盖（render/tint/particle/sound），先于技能装载就位。
        event.addListener(SkillSkinDefs.KEY,
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadSkillSkin(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(EntityDefs.KEY,
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadEntityDef(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(STAT_ENTRY_PATH,
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadStatEntry(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        // 词缀读取（AdditionRead 解析依赖）：须早于物品 mod 装载（ModPreset 内 Addition 经 AdditionRead 解析）。
        event.addListener(ADDITION_PATH,
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadAddition(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        // 词缀冲突组（AffixMod#group 依赖）：须早于词缀冲突判定，与 addition 同批就位即可。
        event.addListener(ADDITION_CONFLICT_PATH,
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadAdditionConflict(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(UNIQUE_DEF_PATH,
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadUniqueDef(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        // 词缀领域标签映射（D7-b）：coe/stat_tag/*.json 叠加 / 扩展到程序化注册表。
        event.addListener(STAT_TAG_PATH,
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> GateTagRegistry.loadFrom(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(DAMAGE_CONVERT_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadDamageConvert(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(LIVING_RARITY_DATA_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadRarityData(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(LIVING_MODIFIER_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadModifier(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(LIVING_MOD_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadEntityMod(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        // 完整技能定义（预设）必须先于宝石装载：宝石 JSON 可为 SkillRead 引用这些预设。
        event.addListener(SKILL_ENTRY_PATH,
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadSkillEntry(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(ITEM_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadSkill(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(ITEM_SUPPORT_GEM_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadSupportGem(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(ITEM_SPIRIT_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadSpirit(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(ITEM_EQUIPMENT_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadItemMod(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(EFFECT_MODIFIER_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadEffectModifier(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        // 四段子部件（effect / targets / condition / duration）：按资源前缀目录扫描加载，
        // 供 Stat 条目的 ref 引用解析。duration 段用 StatDurationTypes.CODEC（dispatch 型，
        // 形如 {"type":"fixed","kind":…,"ticks":…}），不是条目字段用的扁平字符串 StatDuration.CODEC。
        event.addListener(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "coe/stat/effect"),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadStatParts(state.resourceManager(), "coe/stat/effect",
                                StatEffectTypes.MAP_CODEC.codec(), STAT_EFFECT_MAP), bgExecutor).thenCompose(barrier::wait));
        event.addListener(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "coe/stat/targets"),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadStatParts(state.resourceManager(), "coe/stat/targets",
                                StatTargetsTypes.MAP_CODEC.codec(), STAT_TARGET_MAP), bgExecutor).thenCompose(barrier::wait));
        event.addListener(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "coe/stat/condition"),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadStatParts(state.resourceManager(), "coe/stat/condition",
                                StatConditionTypes.CODEC, STAT_CONDITION_MAP), bgExecutor).thenCompose(barrier::wait));
        event.addListener(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "coe/stat/duration"),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadStatParts(state.resourceManager(), "coe/stat/duration",
                                StatDurationTypes.CODEC, STAT_DURATION_MAP), bgExecutor).thenCompose(barrier::wait));
        event.addListener(ENCHANT_MODIFIER_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadEnchantModifier(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(ARROW_DAMAGE_CONVERT_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadArrowDamageConvert(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(POTION_DAMAGE_CONVERT_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadPotionDamageConvert(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(OTHER_DAMAGE_CONVERT_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadOtherDamageConvert(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        // 饰品 / 药剂槽：自服务端配置文件 config/coe/slots.json 经 SlotConfigLoader 读取解码，
        // 不再扫 ResourceManager 的 data/coe/coe/accessory_slot|flask_slot；装备规则仍按前缀目录扫描（见下）。
        // 消费方（*SlotRegistry.slots）经 ACCESSORY_SLOT_MAP / FLASK_SLOT_MAP 取数。
        // 两个监听器经 bgExecutor 并行执行，统一走 SlotConfigLoader.publish（持类锁的原子「读盘 + 快照发布」
        // 入口）：整体替换两张 volatile MAP 的引用为不可变副本，避免 bg 线程之间、以及 bg 写 / 主线程读的并发中间态。
        // 二者现共用同一私有方法 loadSlots()，保证两侧对两张表的写入完全一致。
        event.addListener(ACCESSORY_SLOT_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadSlots(), bgExecutor).thenCompose(barrier::wait));
        event.addListener(FLASK_SLOT_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadSlots(), bgExecutor).thenCompose(barrier::wait)
                        .thenRunAsync(SlotSyncSender::broadcastSlots, fgExecutor));
        event.addListener(EQUIP_RULE_KEY.identifier(),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadEquipRules(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "coe/recipe_site"),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> loadRecipeSite(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        event.addListener(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "talent_tree"),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> TalentTreeModelLoader.load(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        // 进度系统：任务 / 科技树 / 研究树 / 工种授权（原版数据驱动：数据包 JSON + Codec）
        // 加载完成后在主线程把定义表整表推给全体在线玩家——这些定义在 data/ 下，
        // 客户端自身读不到，不推则专用服务器上科技树 / 研究树界面全空。
        event.addListener(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "coe/progression"),
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> ProgressionData.load(state.resourceManager()), bgExecutor)
                        .thenCompose(barrier::wait)
                        .thenRunAsync(ProgressionSyncSender::broadcastProgression, fgExecutor));
        // 符文：数据驱动加载（datapack 覆盖 bundled 默认值），镶嵌时由 RuneSocketOps 翻译为原生 CommonMod。
        event.addListener(RuneRegistry.RUNE_PATH,
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> RuneRegistry.load(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
        // 精髓：数据驱动加载（datapack 覆盖 bundled 默认值），作用时由 EssenceSocketOps 翻译为原生 CommonMod。
        event.addListener(EssenceRegistry.ESSENCE_PATH,
                (state, bgExecutor, barrier, fgExecutor) -> CompletableFuture.runAsync(()
                        -> EssenceRegistry.load(state.resourceManager()), bgExecutor).thenCompose(barrier::wait));
    }

    /**
     * 带来源上下文的解码入口（重构 3-5）。
     *
     * <p>统一经 {@link RegistryOps} 解码——{@code EntityFlagValue} 等含
     * {@code RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE/BIOME)} 的 codec 需注册表访问才能把
     * JSON 中的 id / 数据包 tag 解析回 Holder / HolderSet；纯 {@code JsonOps} 会抛
     * {@code Can't access registry}。运行期注册表取自活服务器（含数据包 tag），
     * 仅在取不到时回落内置注册表（专用服务端 / 极端时序）。
     *
     * <p>所有加载器统一走此方法，禁止再直接调用 {@code XXX.CODEC.parse(JsonOps.INSTANCE, json)}。
     */
    private static <T> DataResult<T> parseIn(Identifier source, Codec<T> codec, JsonElement json) {
        return DataSourceContext.with(source, () -> codec.parse(registryOps(), json));
    }

    /** 运行期解码用的 RegistryOps：优先活服务器 RegistryAccess（含数据包 tag），回落内置注册表。 */
    private static DynamicOps<JsonElement> registryOps() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        RegistryAccess access = server != null ? server.registryAccess()
                : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        return RegistryOps.create(JsonOps.INSTANCE, access);
    }

    /**
     * 通用「扫描资源 → 解码 → 回调」加载骨架：消除各 loadXxx 重复的 listResources/parseIn/异常日志样板。
     * 默认仅匹配 .json；需要额外过滤（如排除根目录直属文件）时用带 filter 的重载。
     */
    private static <T> void scanResources(ResourceManager manager, String prefix, Codec<T> codec,
            BiConsumer<Identifier, T> handler) {
        scanResources(manager, prefix, id -> id.getPath().endsWith(".json"), codec, handler);
    }

    private static <T> void scanResources(ResourceManager manager, String prefix,
            Predicate<Identifier> filter, Codec<T> codec, BiConsumer<Identifier, T> handler) {
        Map<Identifier, Resource> resources = manager.listResources(prefix, filter);
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            try (InputStream stream = entry.getValue().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream));
                DataResult<T> result = parseIn(entry.getKey(), codec, json);
                result.result().ifPresent(v -> handler.accept(entry.getKey(), v));
                result.error().ifPresent(err ->
                        CraftOfExile.LOGGER.error("Error loading " + prefix + ": " + entry.getKey().getPath() + " " + err.message()));
            } catch (IOException e) {
                CraftOfExile.LOGGER.error("Failed to read data resource stream", e);
            }
        }
    }

    /** 资源路径去掉前缀与 .json 后缀后的相对名（保留嵌套目录）。 */
    private static String relName(Identifier key, String prefix) {
        String p = prefix.endsWith("/") ? prefix : prefix + "/";
        return key.getPath().substring(p.length(), key.getPath().lastIndexOf(".json"));
    }

    /** relName 对应的 Identifier（沿用资源命名空间）。 */
    private static Identifier relId(Identifier key, String prefix) {
        return Identifier.fromNamespaceAndPath(key.getNamespace(), relName(key, prefix));
    }

    /**
     * 取条目路径中距末尾第 {@code fromEnd} 段的原始字符串（1 = 文件名）。这是「路径段下标数学」的
     * 唯一落点；越界返回空串（现行 datagen 条目路径均 ≥ 4 段，不会触发；与旧内联守卫语义一致）。
     * 原 {@code ItemTypeId.pathSegment} 的等价本地实现（ItemTypeId 已收敛为 Identifier，此处为等价内联）。
     */
    private static String pathSegment(String filePath, int fromEnd) {
        String[] segments = filePath.split("/");
        int index = segments.length - fromEnd;
        return index >= 0 ? segments[index] : "";
    }


    /**
     * 加载实体<b>渲染</b>定义（{@code data/<ns>/coe/entity_render/*.json}）。
     * 与技能 / stat 同口径：解析失败仅记日志并跳过该条目，不让单个坏数据拖垮资源重载。
     */
    private static void loadEntityRender(ResourceManager manager) {
        int n = EntityRenderDefs.load(manager);
        CraftOfExile.LOGGER.debug("Loaded {} entity render definitions", n);
    }

    /** 加载技能皮肤定义（{@code data/<ns>/coe/skill_skin/*.json}），按技能 id 索引。 */
    private static void loadSkillSkin(ResourceManager manager) {
        int n = SkillSkinDefs.load(manager);
        CraftOfExile.LOGGER.debug("Loaded {} skill skins", n);
    }

    /** 加载实体定义（{@code data/<ns>/coe/entity/*.json}）。 */
    private static void loadEntityDef(ResourceManager manager) {
        int n = EntityDefs.load(manager);
        CraftOfExile.LOGGER.debug("Loaded {} entity definitions", n);
    }

    private static void loadExpression(ResourceManager manager) {
        EXPRESSION_MAP.clear();
        String prefix = EXPRESSION_KEY.identifier().getPath();
        scanResources(manager, prefix, MathExpression.CODEC,
                (key, expr) -> EXPRESSION_MAP.put(relId(key, prefix), expr));
    }

    private static void loadSupportGem(ResourceManager manager) {
        ITEM_SUPPORT_GEM_MAP.clear();
        String prefix = ITEM_SUPPORT_GEM_KEY.identifier().getPath();
        scanResources(manager, prefix, SupportGem.CODEC, (key, gem) -> {
            String name = relName(key, prefix);
            GemColor color = parseColor(name);
            String idPath = stripColor(name);
            Identifier id = Identifier.fromNamespaceAndPath(key.getNamespace(), idPath);
            ITEM_SUPPORT_GEM_MAP.computeIfAbsent(color, k -> new HashMap<>()).put(id, gem);
        });
    }

    /**
     * 数据包装载：完整技能定义（预设）——{@code coe/skill/<id>.json}。
     *
     * <p>与 Stat 侧 {@link #STAT_ENTRY_PATH} 同款：每个文件一份 {@link SkillEntry}
     * （内联完整技能 或 {@link SkillRead} 引用），按文件名注册进 {@link #SKILL_ENTRY_MAP}，
     * 供 {@code SkillRead.ref} 解析。须<b>早于</b>宝石装载（{@link #ITEM_KEY}）—
     * 宝石可引用这些预设，反向引用无意义。
     */
    public static void loadSkillEntry(ResourceManager manager) {
        SKILL_ENTRY_MAP.clear();
        String base = SKILL_ENTRY_PATH.getPath();
        // 顶层手工预设（仅直接子项；排除 template/ preset/ 子目录以免以错误 id 重复装载）
        scanResources(manager, base,
                id -> id.getPath().endsWith(".json") && id.getPath().indexOf('/', base.length() + 1) < 0,
                SkillEntry.CODEC,
                (key, skill) -> SKILL_ENTRY_MAP.putIfAbsent(relId(key, base), skill));
        // 共享通用预设（generic_*）：data/coe/skill/template/，剥离前缀后 id = coe/generic_xxx
        loadSkillPresetDir(manager, SKILL_TEMPLATE_PATH.getPath());
        // 宝石专属预设（preset_*）：data/coe/skill/preset/，剥离前缀后 id = coe/preset_xxx
        loadSkillPresetDir(manager, SKILL_PRESET_PATH.getPath());
    }

    /** 扫描预设子目录（template / preset）并把剥离前缀后的 id 注册进 {@link #SKILL_ENTRY_MAP}。 */
    private static void loadSkillPresetDir(ResourceManager manager, String prefix) {
        scanResources(manager, prefix, SkillEntry.CODEC,
                (key, skill) -> SKILL_ENTRY_MAP.putIfAbsent(relId(key, prefix), skill));
    }

    private static void loadSpirit(ResourceManager manager) {
        ITEM_SPIRIT_MAP.clear();
        String prefix = ITEM_SPIRIT_KEY.identifier().getPath();
        scanResources(manager, prefix, SkillEntry.CODEC, (key, skill) -> {
            String name = relName(key, prefix);
            boolean isTemplate = !name.contains("/") && (name.startsWith("preset_") || name.startsWith("generic_"));
            GemColor color = parseColor(name);
            String idPath = stripColor(name);
            Identifier id = Identifier.fromNamespaceAndPath(key.getNamespace(), idPath);
            if (!isTemplate) ITEM_SPIRIT_MAP.computeIfAbsent(color, k -> new HashMap<>()).put(id, skill);
            SKILL_ENTRY_MAP.putIfAbsent(id, skill);
        });
    }

    private static void loadSkill(ResourceManager manager) {
        ITEM_MAP.clear();
        SkillLoadReporter.begin();
        String prefix = ITEM_KEY.identifier().getPath();
        scanResources(manager, prefix, SkillEntry.CODEC, (key, skill) -> {
            String name = relName(key, prefix);
            boolean isTemplate = !name.contains("/") && (name.startsWith("preset_") || name.startsWith("generic_"));
            GemColor color = parseColor(name);
            String idPath = stripColor(name);
            Identifier id = Identifier.fromNamespaceAndPath(key.getNamespace(), idPath);
            skill.resolve().form().ifPresent(f -> {
                if (!SkillForms.isForm(f)) {
                    SkillLoadReporter.error("技能 " + id + " 引用了未登记的形态 form=" + f
                            + "（已登记：totem/trap/mine）");
                }
            });
            validateSkillDamageTypes(id, skill.resolve());
            if (!isTemplate) ITEM_MAP.computeIfAbsent(color, k -> new HashMap<>()).put(id, skill);
            SKILL_ENTRY_MAP.putIfAbsent(id, skill);
        });
        SkillLoadReporter.end();
    }

    /**
     * ADR-003 阶段1 · 装载期伤害类型校验（M8 护栏同源）。
     * 遍历技能的阶段树，任何 {@link DamageAction} 引用的 {@code damage_type} 若未在
     * {@link DamageTypes} 登记，经 {@link SkillLoadReporter#error} 暴露——fail-open，不抛异常、不阻断装载。
     * 这样拼错的 damage_type 不再静默按通用伤害结算（对抗「推论 B 静默失效」）。
     */
    private static void validateSkillDamageTypes(Identifier id, Skill skill) {
        for (SkillPhasePlayer player : skill.phases()) {
            validatePhaseDamageTypes(id, player.phase());
        }
    }

    private static void validatePhaseDamageTypes(Identifier id, SkillPhase phase) {
        for (TriggeredAction ta : phase.actions()) {
            if (ta.action() instanceof DamageAction da) {
                if (DamageTypes.defOf(da.damageType()) == null) {
                    SkillLoadReporter.error("技能 " + id + " 的阶段引用了未登记的伤害类型 damage_type=" + da.damageType()
                            + "（已登记见 coe/damage_type/*.json）");
                }
            }
        }
        for (SkillPhasePlayer child : phase.children()) {
            validatePhaseDamageTypes(id, child.phase());
        }
    }

    private static void loadOtherDamageConvert(ResourceManager manager) {
        OTHER_DAMAGE_CONVERT_MAP.clear();
        scanResources(manager, OTHER_DAMAGE_CONVERT_KEY.identifier().getPath(),
                EntityDirectDamage.OtherDamageConvert.CODEC,
                (key, convert) -> OTHER_DAMAGE_CONVERT_MAP.put(convert.predicate(), convert.convert()));
    }

    // ---- 饰品 / 药剂槽（自 config/coe/slots.json 经 SlotConfigLoader 取数，不再扫 ResourceManager）----

    // 两个监听器共用同一同步入口：一次 publish 原子地整体替换两张表的字段引用为不可变快照，
    // bg 写 / 主线程读之间无中间态（publish 内部持类锁串行化饰品 / 药剂两个并行监听器）。
    private static void loadSlots() {
        SlotConfigLoader.publish((acc, flask) -> {
            ACCESSORY_SLOT_MAP = acc;
            FLASK_SLOT_MAP = flask;
        });
    }

    /**
     * 客户端收到 {@link com.cimdy.coe.network.sync.SlotSyncPacket} 后整表覆盖本地 volatile 快照。
     *
     * <p>无条件重建两张表（非「仅非空才覆盖」）：服务端改表后必须让客户端旧表整体失效，
     * 否则客户端按旧分组下标渲染会与新定义错位（{@code AccessorySlotRegistry} 按 keySet 顺序扁平化）。
     * key 与服务端一致，取 category 末段经 {@link com.cimdy.coe.event.data.SlotConfigLoader#groupId} 投影。
     */
    public static void applySlotSync(List<AccessorySlotDef> acc, List<FlaskSlotDef> flask) {
        Map<Identifier, AccessorySlotDef> a = new HashMap<>();
        if (acc != null) for (AccessorySlotDef d : acc) {
            if (d == null || d.category() == null) continue;
            a.put(SlotConfigLoader.groupId(d.category()), d);
        }
        Map<Identifier, FlaskSlotDef> f = new HashMap<>();
        if (flask != null) for (FlaskSlotDef d : flask) {
            if (d == null || d.category() == null) continue;
            f.put(SlotConfigLoader.groupId(d.category()), d);
        }
        ACCESSORY_SLOT_MAP = Map.copyOf(a);
        FLASK_SLOT_MAP = Map.copyOf(f);
    }

    private static void loadEquipRules(ResourceManager manager) {
        EQUIP_RULE_MAP.clear();
        String prefix = EQUIP_RULE_KEY.identifier().getPath();
        scanResources(manager, prefix, EquipRuleDef.CODEC,
                (key, def) -> EQUIP_RULE_MAP.put(relId(key, prefix), def));
    }

    private static void loadPotionDamageConvert(ResourceManager manager) {
        POTION_DAMAGE_CONVERT_MAP.clear();
        scanResources(manager, POTION_DAMAGE_CONVERT_KEY.identifier().getPath(),
                EntityDirectDamage.PotionDamageConvert.CODEC,
                (key, convert) -> POTION_DAMAGE_CONVERT_MAP.put(convert.effect(), convert.convert()));
    }

    private static void loadArrowDamageConvert(ResourceManager manager) {
        ARROW_DAMAGE_CONVERT_MAP.clear();
        scanResources(manager, ARROW_DAMAGE_CONVERT_KEY.identifier().getPath(),
                EntityDirectDamage.ArrowDamageConvert.CODEC,
                (key, convert) -> ARROW_DAMAGE_CONVERT_MAP.put(
                        new EntityDirectDamage.ArrowDamageConvert.EffectState(convert.effect(), convert.state()),
                        convert.convert()));
    }

    private static void loadDamageConvert(ResourceManager manager) {
        LIVING_DAMAGE_CONVERT_MAP.clear();
        scanResources(manager, DAMAGE_CONVERT_KEY.identifier().getPath(), DamageTypeConvert.CODEC,
                (key, convert) -> LIVING_DAMAGE_CONVERT_MAP.put(
                        new DamageTypeConvert.Damage(convert.type(), convert.tag()), convert.convert()));
    }

    private static void loadEnchantModifier(ResourceManager manager) {
        var enchants = ItemDataRegistry.getInstance().getEnchantModifiers();
        enchants.clear();
        scanResources(manager, ENCHANT_MODIFIER_KEY.identifier().getPath(), EnchantModifier.CODEC,
                (key, modifier) -> enchants.put(modifier.enchant(), modifier.builder()));
    }

    /**
     * 通用「外置子部件」加载（effect / targets / condition / duration 四段共用）：按资源前缀扫描目录，
     * 把每个 JSON 解析成对应部件实体填入 map。
     * 供 {@code Stat} 条目以 {@code ref} 引用解析（外置化形态，实体不在条目内）。
     * <p>填表 key 为 {@link #relId} 去前缀后的相对 id（{@code coe:<path>}），
     * 与生成期 {@code Stat*Ids.id("<path>")} 字面一致——否则条目 ref 永远命中不了。
     *
     * @param prefix 资源前缀（如 {@code coe/stat/effect}）
     * @param codec  该段部件的 Codec
     * @param into   填充目标（加载前先 clear）
     */
    private static <T> void loadStatParts(ResourceManager manager, String prefix,
                                          Codec<T> codec, Map<Identifier, T> into) {
        into.clear();
        // key 必须经 relId 去前缀：资源 id 形如 coe:coe/stat/effect/<path>.json，
        // 而条目里的 ref 取自 StatEffectIds/StatTargetIds/StatConditionIds/StatDurationIds 的 id(<path>)
        // （即 coe:<path>）—— 与 STAT_ENTRY_MAP / SKILL_ENTRY_MAP / ADDITION_MAP 同一约定。
        scanResources(manager, prefix, codec, (key, v) -> into.put(relId(key, prefix), v));
    }

    private static void loadEffectModifier(ResourceManager manager) {
        EFFECT_MODIFIER_MAP.clear();
        scanResources(manager, EFFECT_MODIFIER_KEY.identifier().getPath(), EffectModifier.CODEC,
                (key, modifier) -> EFFECT_MODIFIER_MAP.put(modifier.effect(), modifier.modifiers()));
    }

    /**
     * 数据包加载（第六十节：Stat 恢复 datagen 管线）。
     * stat 条目由 {@link StatEntryProvider} 在 runData 时经 {@link StatGen#entries()} 编码落盘为
     * {@code coe/stat/entry/*.json}（文件名即 id path，扁平目录）；此处从数据包按同路径扫描读取，
     * 不走「纯内存 encode→decode 往返」——数据包可覆盖/新增条目，加载失败 fail-fast 暴露。
     */
    public static void loadStatEntry(ResourceManager manager) {
        STAT_ENTRY_MAP.clear();
        String prefix = STAT_ENTRY_PATH.getPath();
        scanResources(manager, prefix, StatEntry.CODEC,
                (key, stat) -> STAT_ENTRY_MAP.put(relId(key, prefix), stat));
    }

    /**
     * 数据包加载：传奇装备由 coe/unique/<baseItem>/<name>.json 扫描读取，
     * 以父目录 <baseItem> 作为基础装备 id，注册进 {@link #UNIQUE_DEF_MAP}（key = <baseItem>/<name>）。
     */
    public static void loadUniqueDef(ResourceManager manager) {
        UNIQUE_DEF_MAP.clear();
        String prefix = UNIQUE_DEF_PATH.getPath();
        scanResources(manager, prefix, UniqueDef.CODEC, (key, u) -> {
            String rel = key.getPath().substring(prefix.length() + 1);
            int slash = rel.indexOf("/");
            if (slash < 0) return;
            String base = rel.substring(0, slash);
            String fname = rel.substring(slash + 1);
            String name = fname.substring(0, fname.lastIndexOf(".json"));
            UNIQUE_DEF_MAP.put(Identifier.fromNamespaceAndPath(key.getNamespace(), base + "/" + name), u);
        });
    }

    /**
     * 加载标签层级（重构 3-4）。
     *
     * <p>逐文件解析为 {@link StatCategory} 后一次性交给 {@link GateTag#applyHierarchy}——
     * 整体替换而非逐条写入，避免加载中途出现"半新半旧"的层级表被匹配逻辑读到。
     * 单个文件解析失败只跳过该文件并打印错误，不影响其余条目；全部缺失则回落内置默认表。
     */
    public static void loadStatCategory(ResourceManager manager) {
        List<StatCategory> loaded = new ArrayList<>();
        scanResources(manager, STAT_TAG_CATEGORY_PATH.getPath(), StatCategory.CODEC,
                (key, h) -> loaded.add(h));
        GateTag.applyHierarchy(loaded);
    }

    /**
     * 数据包装载（词缀读取）：coe/affix/addition/<name>.json 逐文件解码为 {@link AdditionType}
     * 并按文件名注册进 {@link #ADDITION_MAP}，供 AdditionRead(ref) 惰性解析。须在消费者
     * （装备属性应用 / AffixMod 冲突判定）之前就位。
     */
    public static void loadAddition(ResourceManager manager) {
        ADDITION_MAP.clear();
        String prefix = ADDITION_PATH.getPath();
        scanResources(manager, prefix, AdditionType.CODEC,
                (key, addition) -> ADDITION_MAP.put(relId(key, prefix), addition));
    }

    /**
     * 数据包装载（词缀冲突组）：coe/affix/conflict/*.json，每个文件即一个冲突表——
     * 以「表路径」为键、表本身为值缓存于 {@link ModConflict}。表路径 = 命名空间限定路径
     * {@code coe:affix/conflict/<文件名>}（去 .json），文件名即组名。须在 AffixMod 冲突判定之前就位。
     */
    public static void loadAdditionConflict(ResourceManager manager) {
        ModConflict.clear();
        String prefix = ADDITION_CONFLICT_PATH.getPath();
        scanResources(manager, prefix, ModConflict.CODEC, (key, group) -> {
            String rawPath = key.getPath();
            String fileName = rawPath.substring(rawPath.lastIndexOf("/") + 1);
            String name = fileName.endsWith(".json")
                    ? fileName.substring(0, fileName.length() - ".json".length())
                    : fileName;
            Identifier tablePath = Identifier.fromNamespaceAndPath(key.getNamespace(), prefix + "/" + name);
            ModConflict.register(tablePath, group);
        });
    }

    // ── 「路径段 → 身份」段位表（距条目路径末尾的段号，1 = 文件名）─────────────────────────────
    // 条目身份由数据包 key 路径段派生（路径即数据标签，用户明确要求保留；使用 Identifier 承载 modid+name）。
    // 目录文法（仅列出可由 CoeDataBootstrap.registerItemType / EquipmentGen 实际 key 证实的布局）：
    //   <owner>/type/<file>                        ItemWeight.Weights → owner
    //   <slot>/mod/<prefix|suffix>/<file>          ModPreset                → (slot, prefix|suffix)
    //   <group>/mod/<prefix|suffix>/preset/<file>  ModPreset          → (group, prefix|suffix)
    //   <slot>/item/<file>                         EquipmentRead      → slot
    // 其中 <slot> 如 chest/main_hand/sword/foci，<group> 如 armor/weapon，
    // <owner> 为 type 文件的父目录（注册表根的 type.json 落到 "equipment"）。
    /** ModPreset 身份：父目录的父目录（"mod" 之上）+ 父目录（prefix/suffix）。 */
    private static final int MOD_IDENTITY_NAMESPACE_FROM_END = 4;
    private static final int MOD_IDENTITY_SUBTYPE_FROM_END = 2;
    /** ModPreset 身份：较 ModPreset 多一层 "preset" 段，故段号整体 +1。 */
    private static final int MOD_PRESET_IDENTITY_NAMESPACE_FROM_END = 5;
    private static final int MOD_PRESET_IDENTITY_SUBTYPE_FROM_END = 3;
    /** ItemWeight 类型：type 文件的父目录。 */
    private static final int ITEM_WEIGHT_TYPE_FROM_END = 2;
    /** EquipmentRead 类型：item 文件的祖父目录（物品槽）。 */
    private static final int EQUIPMENT_READ_TYPE_FROM_END = 3;

    private static void loadItemMod(ResourceManager manager) {
        var registry = ItemDataRegistry.getInstance();
        registry.getModPools().clear();
        registry.getPresetModPools().clear();
        registry.getDropWeights().clear();
        registry.getClassification().clear();
        EQUIPMENT_READ_MAP.clear();
        String itemsRoot = ITEM_EQUIPMENT_KEY.identifier().getPath();
        scanResources(manager, itemsRoot,
                rl -> rl.getPath().endsWith(".json") && rl.getPath().startsWith(itemsRoot + "/"),
                ItemInstance.CODEC, (key, result) -> {
                    String filePath = key.getPath();
                    switch (result) {
                        case ModPreset mod -> {
                            var builder = new WeightedList.Builder<CommonMod>();
                            for (TierPreset tier : mod.tiers()) {
                                builder.add(tier.toCommonMod(mod), tier.weight());
                            }
                            registry.loadModPool(
                                    Identifier.fromNamespaceAndPath(key.getNamespace(),
                                            pathSegment(filePath, MOD_IDENTITY_NAMESPACE_FROM_END)
                                                    + "/" + pathSegment(filePath, MOD_IDENTITY_SUBTYPE_FROM_END)),
                                    builder);
                        }
                        case EquipmentModPreset preset -> {
                            ModPreset mod = preset.mod();
                            var builder = new WeightedList.Builder<CommonMod>();
                            for (TierPreset tier : mod.tiers()) {
                                builder.add(tier.toCommonMod(mod), tier.weight());
                            }
                            registry.loadPresetModPool(
                                    Identifier.fromNamespaceAndPath(key.getNamespace(),
                                            pathSegment(filePath, MOD_PRESET_IDENTITY_NAMESPACE_FROM_END)
                                                    + "/" + pathSegment(filePath, MOD_PRESET_IDENTITY_SUBTYPE_FROM_END)),
                                    preset.condition(), builder);
                        }
                        case ItemWeight.Weights weights -> {
                            Map<ItemWeight.Type, Integer> typeMap = new HashMap<>();
                            weights.weights().forEach(i -> typeMap.put(ItemWeight.Type.from(i), i.weight()));
                            registry.getDropWeights().loadTypeWeight(
                                    Identifier.fromNamespaceAndPath(key.getNamespace(),
                                            pathSegment(filePath, ITEM_WEIGHT_TYPE_FROM_END)), typeMap);
                        }
                        case EquipmentRead read -> {
                            String relPath = filePath.substring(itemsRoot.length());
                            if (relPath.startsWith("/")) relPath = relPath.substring(1);
                            relPath = relPath.substring(0, relPath.length() - ".json".length());
                            EQUIPMENT_READ_MAP.put(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, relPath), read);
                            registry.getClassification().loadEquipmentRead(
                                    Identifier.fromNamespaceAndPath(key.getNamespace(),
                                            pathSegment(filePath, EQUIPMENT_READ_TYPE_FROM_END)), read);
                        }
                        case EquipmentTemplate template -> {
                            String relPath = filePath.substring(itemsRoot.length());
                            if (relPath.startsWith("/")) relPath = relPath.substring(1);
                            relPath = relPath.substring(0, relPath.length() - ".json".length());
                            EQUIPMENT_TEMPLATE_MAP.put(
                                    Identifier.fromNamespaceAndPath(CraftOfExile.MODID, relPath), template);
                        }
                        default -> {
                        }
                    }
                });
        ItemTypeHierarchy.rebuild();
    }

    private static void loadEntityMod(ResourceManager manager) {
        LIVING_MOD_MAP.clear();
        scanResources(manager, LIVING_MOD_KEY.identifier().getPath(), LivingMod.Mod.CODEC,
                (key, mod) -> LIVING_MOD_MAP.put(mod.name(), mod));
    }

    private static void loadModifier(ResourceManager manager) {
        LIVING_MODIFIER_LIST.clear();
        scanResources(manager, LIVING_MODIFIER_KEY.identifier().getPath(), LivingModifier.CODEC,
                (key, modifier) -> LIVING_MODIFIER_LIST.add(modifier));
    }

    private static void loadRecipeSite(ResourceManager manager) {
        RecipeSiteBlocks.clear();
        scanResources(manager, "coe/recipe_site", RecipeSiteBlocks.RecipeSiteMapping.CODEC,
                (key, mapping) -> RecipeSiteBlocks.register(mapping));
    }

    public static void loadRarityData(ResourceManager manager) {
        LIVING_RARITY_DATA_MAP.clear();
        String prefix = LIVING_RARITY_DATA_KEY.identifier().getPath();
        scanResources(manager, prefix, LivingModifier.CODEC, (key, data) -> {
            String fileName = key.getPath().substring(key.getPath().lastIndexOf("/") + 1,
                    key.getPath().lastIndexOf(".json"));
            LIVING_RARITY_DATA_MAP.put(Rarity.get(fileName), data);
        });
    }
}
