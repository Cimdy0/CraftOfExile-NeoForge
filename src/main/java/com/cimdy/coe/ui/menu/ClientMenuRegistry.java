package com.cimdy.coe.ui.menu;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.ui.screen.ExtraSlotsScreen;
import com.cimdy.coe.ui.screen.TestPanelScreen;
import com.cimdy.coe.ui.screen.gem.SkillScreen;
import com.cimdy.coe.ui.screen.talent.TalentTreeScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** 客户端：将菜单绑定到对应界面。 */
@EventBusSubscriber(modid = CraftOfExile.MODID, value = Dist.CLIENT)
public class ClientMenuRegistry {
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.SKILL_ASSEMBLY_MENU.get(), SkillScreen::new);
        event.register(MenuRegistry.TALENT_MENU.get(), TalentTreeScreen::new);
        event.register(MenuRegistry.TEST_PANEL_MENU.get(), TestPanelScreen::new);
        event.register(MenuRegistry.EXTRA_SLOTS_MENU.get(), ExtraSlotsScreen::new);
    }
}
