package com.cimdy.coe.ui.menu;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.ui.menu.ExtraSlotsMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** 菜单（MenuType）注册表。 */
public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, CraftOfExile.MODID);

    public static final Supplier<MenuType<SkillAssemblyMenu>> SKILL_ASSEMBLY_MENU =
            MENUS.register("skill_assembly",
                    () -> new MenuType<>(SkillAssemblyMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<TalentMenu>> TALENT_MENU =
            MENUS.register("talent",
                    () -> new MenuType<>(TalentMenu::new, FeatureFlags.DEFAULT_FLAGS));

    /** 测试面板（基类链路验证：Menu 操控物品 + 内嵌背包）。 */
    public static final Supplier<MenuType<TestPanelMenu>> TEST_PANEL_MENU =
            MENUS.register("test_panel",
                    () -> new MenuType<>(TestPanelMenu::new, FeatureFlags.DEFAULT_FLAGS));

    /** 药剂 / 护符 / 饰品 统一面板（单按钮点开的窗口，集中渲染三类槽位）。 */
    public static final Supplier<MenuType<ExtraSlotsMenu>> EXTRA_SLOTS_MENU =
            MENUS.register("extra_slots",
                    () -> new MenuType<>(ExtraSlotsMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
