package com.cimdy.coe.item.gem;

import com.cimdy.coe.data.codec.skill.gem.GemCategory;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 技能宝石（未切割宝石切割后的产物）。
 * 与未切割宝石同物品类（共享切割界面 / MODIFIABLE_STATE（GemState）存储），但<b>已切割的宝石不允许再次切割</b>，
 * 故覆写右键交互返回 false（POE2 规则：重切需用奥门，非右键直接重切）。
 */
public class SkillGem extends UncutGem {
    private static final String CUT_TYPE = GemCategory.SKILL.key();

    public SkillGem(Properties properties) {
        super(properties.stacksTo(10), CUT_TYPE);
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        // 已切割的技能宝石禁止右键重切（未切割宝石仍走父类打开切割界面）
        return false;
    }
}
