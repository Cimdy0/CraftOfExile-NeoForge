package com.cimdy.coe.item.special;

import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ShieldItem;

import java.util.List;
import java.util.Map;

/**
 * 法器（Foci）。
 * <p>
 * 继承 {@link ShieldItem} 以复用原版盾牌渲染（第一/第三人称持盾姿势），
 * 但 {@code EquipmentState.types} 不含 {@code shield}/{@code buckler}，
 * 因此不会触发举盾/招架行为事件（仅「持盾渲染」）。
 */
public class Foci extends ShieldItem {
    public Foci(Properties p_43089_) {
        super(p_43089_);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    public Component getName(ItemStack p_371901_) {
        DyeColor dyecolor = p_371901_.get(DataComponents.BASE_COLOR);
        Object var3;
        if (dyecolor != null) {
            String var10000 = this.descriptionId;
            var3 = Component.translatable(var10000 + "." + dyecolor.getName());
        } else {
            var3 = super.getName(p_371901_);
        }

        return (Component) var3;
    }

    /** 法器挂一个空类型 {@link EquipmentState}，使数据驱动判定（isShield/isBuckler）对其返回 false。 */
    public static EquipmentState fociState() {
        return new EquipmentState(100, Rarity.COMMON, List.of(), Map.of(),
                java.util.Optional.empty(), List.of(), 0, List.of());
    }
}
