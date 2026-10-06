package com.cimdy.coe.registry.item;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.EquipmentValue;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.gen.equipment.EquipmentGen;
import com.cimdy.coe.data.codec.skill.gem.GemCategory;
import com.cimdy.coe.data.gen.source.CharmGen;
import com.cimdy.coe.data.codec.talent.data.JewelData;
import com.cimdy.coe.data.codec.talent.effect.StatTalentEffect;
import com.cimdy.coe.item.charm.CharmItem;
import com.cimdy.coe.data.codec.item.currency.CurrencyOperation;
import com.cimdy.coe.data.codec.item.flask.FlaskState;
import com.cimdy.coe.item.currency.BaseCurrency;
import com.cimdy.coe.item.currency.CurrencyType;
import com.cimdy.coe.item.accessory.Buckler;
import com.cimdy.coe.item.special.Foci;
import com.cimdy.coe.item.flask.FlaskItem;
import com.cimdy.coe.item.accessory.AmuletItem;
import com.cimdy.coe.item.accessory.QuiverItem;
import com.cimdy.coe.item.gem.Gem;
import com.cimdy.coe.item.gem.Jewel;
import com.cimdy.coe.item.gem.SkillGem;
import com.cimdy.coe.item.gem.UncutGem;
import com.cimdy.coe.item.special.EnderBallItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;

import com.cimdy.coe.data.codec.item.RuneRegistry;
import com.cimdy.coe.data.codec.item.state.RuneState;
import com.cimdy.coe.item.special.RuneItem;
import com.cimdy.coe.data.codec.item.essence.EssenceRegistry;
import com.cimdy.coe.data.codec.item.essence.EssenceState;
import com.cimdy.coe.item.special.EssenceItem;

public class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CraftOfExile.MODID);
    public static List<DeferredItem<Item>> allCurrencyItem = new ArrayList<>();

    /** 符文物品表：mod 初始化期由 RuneRegistry 数据驱动注册（64 实例）。 */
    public static final List<DeferredItem<Item>> RUNE_ITEMS = new ArrayList<>();

    /** 精髓物品表：mod 初始化期由 EssenceRegistry 数据驱动注册（26 实例）。 */
    public static final List<DeferredItem<Item>> ESSENCE_ITEMS = new ArrayList<>();

    /** 缓存的通货数据：按 {@link CurrencyType} 枚举逐条注册，创造物品栏 / 模型生成枚举消费本表。 */
    public static final Map<CurrencyType, DeferredItem<Item>> CURRENCY_ITEMS = new EnumMap<>(CurrencyType.class);
    static {
        for (CurrencyType t : CurrencyType.values()) {
            CURRENCY_ITEMS.put(t, register(t.id(), t.operation(), t.stackSize()));
        }
        // 符文：数据驱动注册 64 实例（RuneRegistry.init 读取自带 runes.json）。
        RuneRegistry.init();
        for (RuneState s : RuneRegistry.all()) {
            String regName = s.id().toLowerCase(Locale.ROOT);
            RUNE_ITEMS.add(ITEMS.registerItem(regName, p -> new RuneItem(s.id(), p), pro -> pro.stacksTo(64)));
        }
        // 精髓：数据驱动注册 26 实例（EssenceRegistry.init 读取自带 essences.json）。
        EssenceRegistry.init();
        for (EssenceState s : EssenceRegistry.all()) {
            String regName = s.id().toLowerCase(Locale.ROOT);
            ESSENCE_ITEMS.add(ITEMS.registerItem(regName, p -> new EssenceItem(s.id(), p), pro -> pro.stacksTo(64)));
        }
    }

    public static final DeferredItem<Item> WISDOM_SCROLL = CURRENCY_ITEMS.get(CurrencyType.WISDOM_SCROLL);
    public static final DeferredItem<Item> PORTAL_SCROLL = CURRENCY_ITEMS.get(CurrencyType.PORTAL_SCROLL);
    public static final DeferredItem<Item> KALANDRA_MIRROR = CURRENCY_ITEMS.get(CurrencyType.KALANDRA_MIRROR);
    public static final DeferredItem<Item> CHANCE_ORB = CURRENCY_ITEMS.get(CurrencyType.CHANCE_ORB);
    public static final DeferredItem<Item> SCOURING_ORB = CURRENCY_ITEMS.get(CurrencyType.SCOURING_ORB);
    public static final DeferredItem<Item> ALCHEMY_ORB = CURRENCY_ITEMS.get(CurrencyType.ALCHEMY_ORB);
    public static final DeferredItem<Item> ALTERATION_ORB = CURRENCY_ITEMS.get(CurrencyType.ALTERATION_ORB);
    public static final DeferredItem<Item> TRANSMUTATION_ORB = CURRENCY_ITEMS.get(CurrencyType.TRANSMUTATION_ORB);
    public static final DeferredItem<Item> CHAOS_ORB = CURRENCY_ITEMS.get(CurrencyType.CHAOS_ORB);
    public static final DeferredItem<Item> REGAL_ORB = CURRENCY_ITEMS.get(CurrencyType.REGAL_ORB);
    public static final DeferredItem<Item> AUGMENTATION_ORB = CURRENCY_ITEMS.get(CurrencyType.AUGMENTATION_ORB);
    public static final DeferredItem<Item> EXALTED_ORB = CURRENCY_ITEMS.get(CurrencyType.EXALTED_ORB);
    public static final DeferredItem<Item> DIVINE_ORB = CURRENCY_ITEMS.get(CurrencyType.DIVINE_ORB);
    public static final DeferredItem<Item> ANNULMENT_ORB = CURRENCY_ITEMS.get(CurrencyType.ANNULMENT_ORB);
    public static final DeferredItem<Item> BLESSED_ORB = CURRENCY_ITEMS.get(CurrencyType.BLESSED_ORB);
    // 品质通货
    public static final DeferredItem<Item> GEMCUTTER_PRISM = CURRENCY_ITEMS.get(CurrencyType.GEMCUTTER_PRISM);
    public static final DeferredItem<Item> BLACKSMITH_WHETSTONE = CURRENCY_ITEMS.get(CurrencyType.BLACKSMITH_WHETSTONE);
    public static final DeferredItem<Item> ARMOURER_SCRAP = CURRENCY_ITEMS.get(CurrencyType.ARMOURER_SCRAP);
    public static final DeferredItem<Item> GLASSBLOWER_BAUBLE = CURRENCY_ITEMS.get(CurrencyType.GLASSBLOWER_BAUBLE);
    // 孔通货（技能辅助槽分阶 + 装备物品孔）
    public static final DeferredItem<Item> LESSER_JEWELLER_ORB = CURRENCY_ITEMS.get(CurrencyType.LESSER_JEWELLER_ORB);
    public static final DeferredItem<Item> GREATER_JEWELLER_ORB = CURRENCY_ITEMS.get(CurrencyType.GREATER_JEWELLER_ORB);
    public static final DeferredItem<Item> PERFECT_JEWELLER_ORB = CURRENCY_ITEMS.get(CurrencyType.PERFECT_JEWELLER_ORB);
    public static final DeferredItem<Item> ARTIFICER_ORB = CURRENCY_ITEMS.get(CurrencyType.ARTIFICER_ORB);

    public static final DeferredItem<Item> ENDER_BALL = ITEMS.registerItem(
            "ender_ball", EnderBallItem::new, pro -> pro.stacksTo(16));

    // Flask（POE2 实证）：右键/按键消耗充能发动「持续恢复」，充能随时间/击杀恢复（机制见 FlaskHelper）。
    // 数值取 POE2 基础规格：Lesser Life Flask（50 生命/3 秒）、Lesser Mana Flask（50 魔力/2 秒），消耗 10/60 充能。
    public static final DeferredItem<Item> LIFE_FLASK = ITEMS.registerItem(
            "life_flask", p -> FlaskItem.of(p, FlaskState.LIFE),
            pro -> pro.stacksTo(1));
    public static final DeferredItem<Item> MANA_FLASK = ITEMS.registerItem(
            "mana_flask", p -> FlaskItem.of(p, FlaskState.MANA),
            pro -> pro.stacksTo(1));

    // Charm（护符，POE2 实证）：装备在背包 UI 的 Charm 槽，自动触发（不靠手动按键）。
    // 数据经 CharmGen（数据驱动 CODEC 工厂）生成 CharmState（效果为 StatEntry 列表，引用 coe:charm_* 模板）；
    // 上限 80 充能，各护符按次消耗 40–80 充能（抗性 40 / 免疫 50–80）。效果由 CharmEngine 注入属性层，无硬编码状态。
    public static final DeferredItem<Item> RUBY_CHARM = ITEMS.registerItem("ruby_charm",
            p -> CharmItem.of(p, CharmGen.RUBY), pro -> pro.stacksTo(1));
    public static final DeferredItem<Item> SAPPHIRE_CHARM = ITEMS.registerItem("sapphire_charm",
            p -> CharmItem.of(p, CharmGen.SAPPHIRE), pro -> pro.stacksTo(1));
    public static final DeferredItem<Item> TOPAZ_CHARM = ITEMS.registerItem("topaz_charm",
            p -> CharmItem.of(p, CharmGen.TOPAZ), pro -> pro.stacksTo(1));
    public static final DeferredItem<Item> AMETHYST_CHARM = ITEMS.registerItem("amethyst_charm",
            p -> CharmItem.of(p, CharmGen.AMETHYST), pro -> pro.stacksTo(1));
    public static final DeferredItem<Item> DOUSING_CHARM = ITEMS.registerItem("dousing_charm",
            p -> CharmItem.of(p, CharmGen.DOUSING), pro -> pro.stacksTo(1));
    public static final DeferredItem<Item> THAWING_CHARM = ITEMS.registerItem("thawing_charm",
            p -> CharmItem.of(p, CharmGen.THAWING), pro -> pro.stacksTo(1));
    public static final DeferredItem<Item> GROUNDING_CHARM = ITEMS.registerItem("grounding_charm",
            p -> CharmItem.of(p, CharmGen.GROUNDING), pro -> pro.stacksTo(1));
    public static final DeferredItem<Item> STAUNCHING_CHARM = ITEMS.registerItem("staunching_charm",
            p -> CharmItem.of(p, CharmGen.STAUNCHING), pro -> pro.stacksTo(1));
    public static final DeferredItem<Item> ANTIDOTE_CHARM = ITEMS.registerItem("antidote_charm",
            p -> CharmItem.of(p, CharmGen.ANTIDOTE), pro -> pro.stacksTo(1));
    public static final DeferredItem<Item> STONE_CHARM = ITEMS.registerItem("stone_charm",
            p -> CharmItem.of(p, CharmGen.STONE), pro -> pro.stacksTo(1));
    public static final DeferredItem<Item> SILVER_CHARM = ITEMS.registerItem("silver_charm",
            p -> CharmItem.of(p, CharmGen.SILVER), pro -> pro.stacksTo(1));
    public static final DeferredItem<Item> GOLDEN_CHARM = ITEMS.registerItem("golden_charm",
            p -> CharmItem.of(p, CharmGen.GOLDEN), pro -> pro.stacksTo(1));

    // 切割后的宝石产物必须在未切割宝石之前声明：
    // GemCategory 初始化时会引用 SKILL_GEM/SUPPORT_GEM/SPIRIT_GEM，
    // 而 UNCUT_GEM 的 SkillGem::new 会触发 GemCategory 类加载。
    public static final DeferredItem<Item> SKILL_GEM = ITEMS.registerItem(
            "skill_gem", Gem::new, pro -> pro.stacksTo(1));
    public static final DeferredItem<Item> SUPPORT_GEM = ITEMS.registerItem(
            "support_gem", Gem::new, pro -> pro.stacksTo(1));
    public static final DeferredItem<Item> SPIRIT_GEM = ITEMS.registerItem(
            "spirit_gem", Gem::new, pro -> pro.stacksTo(1));

    public static final DeferredItem<Item> UNCUT_GEM = ITEMS.registerItem(
            "uncut_gem", SkillGem::new, pro -> pro.stacksTo(10));

    public static final DeferredItem<Item> UNCUT_SUPPORT_GEM = ITEMS.registerItem(
            "uncut_support_gem", pro -> new UncutGem(pro, GemCategory.SUPPORT.key()), pro -> pro.stacksTo(10));
    public static final DeferredItem<Item> UNCUT_SPIRIT_GEM = ITEMS.registerItem(
            "uncut_spirit_gem", pro -> new UncutGem(pro, GemCategory.SPIRIT.key()), pro -> pro.stacksTo(10));

    /** 基础珠宝：可插入天赋树珠宝插槽的物品。默认样例珠宝仅授予 +20 最大生命（radius=0，不影响其它节点）。 */
    public static final DeferredItem<Item> JEWEL = ITEMS.registerItem(
            "jewel", Jewel::new, pro -> pro.stacksTo(1).component(
                    DataComponentRegistry.JEWEL_DATA.get(),
                    new JewelData(Component.translatable("item.coe.jewel"),
                            java.util.List.of(StatTalentEffect.of(Attributes.MAX_HEALTH, 20.0)),
                            0f, java.util.List.of())));

    /**
     * 辐射之珠宝（示范「半径内节点」）：插入任意珠宝插槽后，除自身效果（+15 护甲）外，
     * 还会在以插槽为中心的 {@code radius} 世界单位圆形范围内「辐射」——
     * 范围内的节点（如武器树中的孤立节点 {@code w_jewel_gift}）即便没有任何图连接，也可被点亮，
     * 且这些被辐射点亮的节点在界面上以专属描边区分（仿 PoE 珠宝辐射语义）。
     */
    public static final DeferredItem<Item> JEWEL_OF_RADIANCE = ITEMS.registerItem(
            "jewel_of_radiance", Jewel::new, pro -> pro.stacksTo(1).component(
                    DataComponentRegistry.JEWEL_DATA.get(),
                    new JewelData(Component.translatable("item.coe.radiation_jewel"),
                            java.util.List.of(StatTalentEffect.of(Attributes.ARMOR, 15.0)),
                            120f, java.util.List.of())));

    public static final DeferredItem<Item> FOCI = ITEMS.registerItem(
            "foci", Foci::new, pro -> pro
                    .durability(336)
                    .equippableUnswappable(EquipmentSlot.OFFHAND)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(), Foci.fociState())
                    .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK)
                    .delayedComponent(
                            DataComponents.BLOCKS_ATTACKS,
                            context -> new BlocksAttacks(
                                    0.25F,
                                    1.0F,
                                    List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                                    new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                                    Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                                    Optional.of(SoundEvents.SHIELD_BLOCK),
                                    Optional.of(SoundEvents.SHIELD_BREAK)
                            )
                    )
    );

    /**
     * 轻盾（Buckler）：POE2 中对应「buckler」类盾牌，专属「招架（Parry）」动作。
     * 继承 {@link ShieldItem} 复用原版盾牌渲染；
     * {@code EquipmentState.types} 含 {@code buckler}，由数据驱动判定触发招架行为（与 isShield/isBlock 同构）。
     */
    public static final DeferredItem<Item> BUCKLER = ITEMS.registerItem(
            "buckler", Buckler::new, pro -> pro
                    .durability(336)
                    .equippableUnswappable(EquipmentSlot.OFFHAND)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(), Buckler.bucklerState())
                    .component(DataComponentRegistry.CAN_PARRY.get(), true)
                    .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK)
                    .delayedComponent(
                            DataComponents.BLOCKS_ATTACKS,
                            context -> new BlocksAttacks(
                                    0.25F,
                                    1.0F,
                                    List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                                    new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                                    Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                                    Optional.of(SoundEvents.SHIELD_BLOCK),
                                    Optional.of(SoundEvents.SHIELD_BREAK)
                            )
                    )
    );

    /**
     * 腿甲（Legs）：POE2 中对应腿甲装备，专属 Legs 槽（背包 UI 右侧外置面板）。
     * 携带 {@code MODIFIABLE_STATE}(armor)，供后续「+X 闪避 per Y 腿甲护甲」(#88) 等派生词缀经 EquipmentValue 读取。
     */
    public static final DeferredItem<Item> LEGS = ITEMS.registerItem(
            "legs", Item::new, pro -> pro
                    .stacksTo(1)
                    .humanoidArmor(ArmorMaterials.LEATHER, ArmorType.LEGGINGS)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.LEGS)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /**
     * 箭袋（Quiver）：POE2 中对应箭袋装备，专属 Quiver 槽（背包 UI 右侧外置面板）。
     * 携带 {@code MODIFIABLE_STATE}(type=quiver)，供后续「装备箭袋加成」(#55) 等派生词缀引用。
     */
    public static final DeferredItem<Item> QUIVER = ITEMS.registerItem(
            "quiver", QuiverItem::new, pro -> pro
                    .stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(), QuiverItem.quiverState()));

    /**
     * 护身符（Amulet）：POE2 中对应护身符装备，专属 Amulet 槽（背包 UI 右侧外置面板）。
     * 携带 {@code MODIFIABLE_STATE}(type=amulet)，供后续「装备护身符加成」「护身符加成重定向至召唤物」
     * （ALL_BONUSES_FROM_EQUIPPED_AMULET_APPLY_TO_YOUR_MINIONS_INSTEAD_OF_YOU）等词缀引用。
     * 数值/词缀覆盖留待内容层。
     */
    public static final DeferredItem<Item> AMULET = ITEMS.registerItem(
            "amulet", AmuletItem::new, pro -> pro
                    .stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(), AmuletItem.amuletState()));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：bow 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> BOW = ITEMS.registerItem(
            "bow", BowItem::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.BOW)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：one_hand_sword 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> ONE_HAND_SWORD = ITEMS.registerItem(
            "one_hand_sword", Item::new, pro -> pro.stacksTo(1)
                    .sword(ToolMaterial.WOOD, 3.0F, -2.4F)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.ONE_HAND_SWORD)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：two_hand_sword 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> TWO_HAND_SWORD = ITEMS.registerItem(
            "two_hand_sword", Item::new, pro -> pro.stacksTo(1)
                    .sword(ToolMaterial.WOOD, 3.0F, -2.4F)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.TWO_HAND_SWORD)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：one_hand_axe 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> ONE_HAND_AXE = ITEMS.registerItem(
            "one_hand_axe", p -> new AxeItem(ToolMaterial.WOOD, 6.0F, -3.2F, p), pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.ONE_HAND_AXE)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：two_hand_axe 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> TWO_HAND_AXE = ITEMS.registerItem(
            "two_hand_axe", p -> new AxeItem(ToolMaterial.WOOD, 6.0F, -3.2F, p), pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.TWO_HAND_AXE)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：cross_bow 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> CROSS_BOW = ITEMS.registerItem(
            "cross_bow", CrossbowItem::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.CROSS_BOW)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：claw 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> CLAW = ITEMS.registerItem(
            "claw", Item::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.CLAW)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：dagger 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> DAGGER = ITEMS.registerItem(
            "dagger", Item::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.DAGGER)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：one_hand_mace 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> ONE_HAND_MACE = ITEMS.registerItem(
            "one_hand_mace", Item::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.ONE_HAND_MACE)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：two_hand_mace 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> TWO_HAND_MACE = ITEMS.registerItem(
            "two_hand_mace", Item::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.TWO_HAND_MACE)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：sceptre 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> SCEPTRE = ITEMS.registerItem(
            "sceptre", Item::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.SCEPTRE)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：spear 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> SPEAR = ITEMS.registerItem(
            "spear", Item::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.SPEAR)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：flail 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> FLAIL = ITEMS.registerItem(
            "flail", Item::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.FLAIL)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：wand 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> WAND = ITEMS.registerItem(
            "wand", Item::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.WAND)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：staff 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> STAFF = ITEMS.registerItem(
            "staff", Item::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.STAFF)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：quarterstaff 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> QUARTERSTAFF = ITEMS.registerItem(
            "quarterstaff", Item::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.QUARTERSTAFF)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：trap 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> TRAP = ITEMS.registerItem(
            "trap", Item::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.TRAP)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：talisman 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> TALISMAN = ITEMS.registerItem(
            "talisman", Item::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.TALISMAN)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：shield 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> SHIELD = ITEMS.registerItem(
            "shield", ShieldItem::new, pro -> pro.stacksTo(1)
                    .equippable(EquipmentSlot.OFFHAND)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.SHIELD)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：chest 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> CHEST = ITEMS.registerItem(
            "chest", Item::new, pro -> pro
                    .stacksTo(1)
                    .humanoidArmor(ArmorMaterials.LEATHER, ArmorType.CHESTPLATE)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.CHEST)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：head 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> HEAD = ITEMS.registerItem(
            "head", Item::new, pro -> pro
                    .stacksTo(1)
                    .humanoidArmor(ArmorMaterials.LEATHER, ArmorType.HELMET)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.HEAD)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：feet 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> FEET = ITEMS.registerItem(
            "feet", Item::new, pro -> pro
                    .stacksTo(1)
                    .humanoidArmor(ArmorMaterials.LEATHER, ArmorType.BOOTS)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.FEET)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：ring 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> RING = ITEMS.registerItem(
            "ring", Item::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.RING)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    /** 装备类型载体（数据驱动，默认等级 100、无额外词缀）：belt 仅承载 MODIFIABLE_STATE 类型标签，基础数值/需求/词缀由数据包经 EquipmentGen 注入。 */
    public static final DeferredItem<Item> BELT = ITEMS.registerItem(
            "belt", Item::new, pro -> pro.stacksTo(1)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.BELT)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    // ===================== PoE2 美术实装测试物品（图标 = poe2_assets 生成的 32x32 像素图标） =====================
    // 直接注册真实物品 + 对应贴图（assets/coe/textures/item/<snake>.png，由 poe2_assets/item_pixel 复制而来），
    // 供美术验收：在创造模式「CraftOfExile」页签（COE_TAB）取用，穿戴走原版 humanoidArmor 渲染。
    public static final DeferredItem<Item> SHABBY_HOOD = ITEMS.registerItem(
            "shabby_hood", Item::new, pro -> pro
                    .stacksTo(1)
                    .humanoidArmor(ArmorMaterials.LEATHER, ArmorType.HELMET)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.HEAD)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    public static final DeferredItem<Item> ABYSSAL_CUIRASS = ITEMS.registerItem(
            "abyssal_cuirass", Item::new, pro -> pro
                    .stacksTo(1)
                    .humanoidArmor(ArmorMaterials.LEATHER, ArmorType.CHESTPLATE)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.CHEST)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    public static final DeferredItem<Item> BRONZE_GREAVES = ITEMS.registerItem(
            "bronze_greaves", Item::new, pro -> pro
                    .stacksTo(1)
                    .humanoidArmor(ArmorMaterials.LEATHER, ArmorType.BOOTS)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.FEET)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    public static final DeferredItem<Item> BLACKSTEEL_GAUNTLETS = ITEMS.registerItem(
            "blacksteel_gauntlets", Item::new, pro -> pro
                    .stacksTo(1)
                    .humanoidArmor(ArmorMaterials.LEATHER, ArmorType.LEGGINGS)
                    .component(DataComponentRegistry.MODIFIABLE_STATE.get(),
                            new EquipmentState(100, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.LEGS)), Map.of(), Optional.empty(), List.of(), 0, List.of())));

    private static DeferredItem<Item> register(String name, CurrencyOperation op, int stackSize) {
        DeferredItem<Item> deferredItem = ITEMS.registerItem(name, p -> new BaseCurrency(p, op), pro -> pro.stacksTo(stackSize));
        ItemRegistry.allCurrencyItem.add(deferredItem);
        return deferredItem;
    }

    /**
     * 按装备类型（数据包分类键，与 EquipmentGen 常量一致）返回对应载体物品，
     * 供创造物品栏枚举缓存装备读取时构造 ItemStack。
     */
    public static Item carrierForType(String type) {
        return switch (type) {
            case "bow" -> BOW.get();
            case "one_hand_sword" -> ONE_HAND_SWORD.get();
            case "two_hand_sword" -> TWO_HAND_SWORD.get();
            case "one_hand_axe" -> ONE_HAND_AXE.get();
            case "two_hand_axe" -> TWO_HAND_AXE.get();
            case "cross_bow" -> CROSS_BOW.get();
            case "claw" -> CLAW.get();
            case "dagger" -> DAGGER.get();
            case "one_hand_mace" -> ONE_HAND_MACE.get();
            case "two_hand_mace" -> TWO_HAND_MACE.get();
            case "sceptre" -> SCEPTRE.get();
            case "spear" -> SPEAR.get();
            case "flail" -> FLAIL.get();
            case "wand" -> WAND.get();
            case "staff" -> STAFF.get();
            case "quarterstaff" -> QUARTERSTAFF.get();
            case "trap" -> TRAP.get();
            case "talisman" -> TALISMAN.get();
            case "shield" -> SHIELD.get();
            case "chest" -> CHEST.get();
            case "head" -> HEAD.get();
            case "feet" -> FEET.get();
            case "legs" -> LEGS.get();
            case "ring" -> RING.get();
            case "belt" -> BELT.get();
            case "quiver" -> QUIVER.get();
            case "amulet" -> AMULET.get();
            case "buckler" -> BUCKLER.get();
            // 类型键取数据包目录名（foci），与 EquipmentGen.FOCI 一致；focus 为旧写法，二者同指法器
            case "foci", "focus" -> FOCI.get();
            default -> null;
        };
    }

}
