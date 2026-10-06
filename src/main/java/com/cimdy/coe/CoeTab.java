package com.cimdy.coe;

import com.cimdy.coe.registry.world.BlockRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentRead;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.item.registry.ItemDataRegistry;
import com.cimdy.coe.data.codec.skill.gem.GemCategory;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.helper.affix.ModHelper;
import com.cimdy.coe.registry.item.ItemRegistry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CoeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CraftOfExile.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COE_TAB =
            CREATIVE_MODE_TABS.register("coe_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("group.item.coe.def"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ItemRegistry.WISDOM_SCROLL.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        ItemRegistry.allCurrencyItem.forEach(item -> output.accept(item.get()));
                        // 符文：数据驱动注册（64 实例），排在通货之后，便于调试与获取
                        ItemRegistry.RUNE_ITEMS.forEach(item -> output.accept(item.get()));
                        output.accept(ItemRegistry.FOCI.get());
                        output.accept(BlockRegistry.TERMINAL_ITEM.get());
                        output.accept(ItemRegistry.ENDER_BALL.get());
                        // 技能/辅助/精魂宝石已迁移至「技能缓存」物品栏（COE_SKILL_TAB）
                        // 药剂 / 护符：加入创造物品栏（与装备物品一致，便于调试与获取）
                        output.accept(ItemRegistry.LIFE_FLASK.get());
                        output.accept(ItemRegistry.MANA_FLASK.get());
                        output.accept(ItemRegistry.RUBY_CHARM.get());
                        output.accept(ItemRegistry.SAPPHIRE_CHARM.get());
                        output.accept(ItemRegistry.TOPAZ_CHARM.get());
                        output.accept(ItemRegistry.AMETHYST_CHARM.get());
                        output.accept(ItemRegistry.DOUSING_CHARM.get());
                        output.accept(ItemRegistry.THAWING_CHARM.get());
                        output.accept(ItemRegistry.GROUNDING_CHARM.get());
                        output.accept(ItemRegistry.STAUNCHING_CHARM.get());
                        output.accept(ItemRegistry.ANTIDOTE_CHARM.get());
                        output.accept(ItemRegistry.STONE_CHARM.get());
                        output.accept(ItemRegistry.SILVER_CHARM.get());
                        output.accept(ItemRegistry.GOLDEN_CHARM.get());
                        // PoE2 美术实装测试物品：直接取用验收贴图效果
                        output.accept(ItemRegistry.SHABBY_HOOD.get());
                        output.accept(ItemRegistry.ABYSSAL_CUIRASS.get());
                        output.accept(ItemRegistry.BRONZE_GREAVES.get());
                        output.accept(ItemRegistry.BLACKSTEEL_GAUNTLETS.get());
                    }).build());

    /** 装备缓存物品栏：枚举数据包加载后缓存的全部装备读取（EquipmentRead），按类型选用载体物品构造 ItemStack。
     * 默认等级 100、无额外词缀，便于调试与获取。 */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COE_EQUIPMENT_TAB =
            CREATIVE_MODE_TABS.register("coe_equipment_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("group.item.coe.equipment"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ItemRegistry.CHEST.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        ItemDataRegistry.getInstance().getEquipmentReads().forEach((type, reads) -> {
                            Item carrier = ItemRegistry.carrierForType(type.getPath());
                            if (carrier == null) return;
                            for (EquipmentRead read : reads) {
                                ItemStack stack = new ItemStack(carrier);
                                stack.set(DataComponentRegistry.MODIFIABLE_STATE.get(),
                                        new EquipmentState(100, Rarity.COMMON, List.of(type), Map.of(),
                                                Optional.of(read), List.of(), 0, List.of()));
                                // 类型须写完整类型链（叶类别 → 父类别 → … → equipment），
                                // 否则 isWeapon / isArmor / 双手 / 胸甲 等按 types 的判定全为 false
                                ModHelper.setType(stack, ModHelper.getItemType(stack));
                                output.accept(stack);
                            }
                        });
                    }).build());

    /** 技能缓存物品栏：枚举所有已加载的技能/辅助/精魂宝石（SkillEntry / SupportGem），按类别构造宝石 ItemStack。 */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COE_SKILL_TAB =
            CREATIVE_MODE_TABS.register("coe_skill_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("group.item.coe.skill"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ItemRegistry.SKILL_GEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        for (GemCategory cat : GemCategory.values()) {
                            cat.buildCutListAll().forEach(output::accept);
                        }
                    }).build());

}
