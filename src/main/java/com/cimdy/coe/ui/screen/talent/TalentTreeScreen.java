package com.cimdy.coe.ui.screen.talent;

import com.cimdy.coe.ui.menu.TalentMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * 主天赋树界面（已注册到 {@code TALENT_MENU}）。
 *
 * <p>本类仅是 {@link AbstractTalentTreeScreen} 的薄封装：驱动的树 id 取自菜单标题
 * （服务端经 {@code OpenTalentMenu(treeId)} 将树 id 的字符串形式写入菜单标题），
 * 此处由 {@link Identifier#parse} 还原为 {@link Identifier}，由此自动渲染对应树。
 * 要新增一棵树，无需改动本类——见 {@link AbstractTalentTreeScreen} 的类注释。
 */
public class TalentTreeScreen extends AbstractTalentTreeScreen {
    public TalentTreeScreen(TalentMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, Identifier.parse(title.getString()));
    }
}
