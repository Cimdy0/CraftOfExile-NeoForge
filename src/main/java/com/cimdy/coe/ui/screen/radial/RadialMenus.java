package com.cimdy.coe.ui.screen.radial;

import com.cimdy.coe.data.codec.talent.tree.TalentTreeModelLoader;
import com.cimdy.coe.registry.item.ItemRegistry;
import com.cimdy.coe.network.menu.OpenSkillMenuPacket;
import com.cimdy.coe.network.menu.OpenTalentMenuPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * 构建径向轮盘菜单的内容。
 *
 * <p>主菜单目前包含两项：<b>技能装配</b>与<b>天赋</b>。天赋项为分支节点，
 * 其子菜单（全部天赋树）在打开时才从 {@link TalentTreeModelLoader#treeIds()} 读取——
 * 完全数据驱动，新增天赋树无需任何代码改动。
 */
public final class RadialMenus {
    private RadialMenus() {
    }

    public static RadialNode buildMainMenu() {
        ItemStack skillIcon = new ItemStack(ItemRegistry.SKILL_GEM.get());
        ItemStack talentIcon = new ItemStack(ItemRegistry.JEWEL.get());

        RadialNode skill = RadialNode.action("skill_assembly",
                Component.translatable("radial.coe.skill_assembly"),
                skillIcon,
                () -> ClientPacketDistributor.sendToServer(new OpenSkillMenuPacket()));

        RadialNode talent = RadialNode.submenu("talent",
                Component.translatable("radial.coe.talent"),
                talentIcon,
                RadialMenus::buildTalentSubmenu);

        // C.7 重绑入口（UI 内可见操作）：打开原版「按键控制」屏，玩家可在此重绑 OPEN_MENU /
        // OPEN_SKILL / OPEN_STYLE_PICKER 等本模组热键。直接切屏（非发包），故 RadialMenuScreen
        // 在 run() 后需判断「是否仍停留在自身」再决定是否关闭（见 RadialMenuScreen.mouseClicked）。
        RadialNode keybinds = RadialNode.action("keybinds",
                Component.translatable("radial.coe.keybinds"),
                new ItemStack(Items.NAME_TAG),
                () -> Minecraft.getInstance().setScreenAndShow(
                        new KeyBindsScreen(Minecraft.getInstance().gui.screen(), Minecraft.getInstance().options)));

        return RadialNode.submenu("root", Component.literal("menu"), ItemStack.EMPTY,
                List.of(skill, talent, keybinds));
    }

    private static List<RadialNode> buildTalentSubmenu() {
        List<RadialNode> list = new ArrayList<>();
        ItemStack icon = new ItemStack(ItemRegistry.JEWEL.get());
        for (Identifier treeId : TalentTreeModelLoader.treeIds()) {
            list.add(RadialNode.action(treeId.toString(),
                    TalentTreeModelLoader.displayName(treeId),
                    icon,
                    () -> ClientPacketDistributor.sendToServer(new OpenTalentMenuPacket(treeId))));
        }
        return list;
    }
}
