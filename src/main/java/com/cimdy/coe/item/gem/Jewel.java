package com.cimdy.coe.item.gem;

import com.cimdy.coe.client.ClientLocale;

import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.talent.data.JewelData;
import com.cimdy.coe.data.codec.talent.effect.TalentEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * 珠宝（Jewel）：可插入天赋树上「珠宝插槽」节点的物品，授予额外加成。
 *
 * <p>珠宝授予的效果存储在 {@link DataComponentRegistry#JEWEL_DATA} 组件中（{@link JewelData}），
 * 因此不同珠宝可携带不同加成，且与天赋 {@link TalentEffect} 体系共享施加管线。
 * 悬浮提示会展示珠宝名称与其全部效果说明。
 */
public class Jewel extends Item {
    public Jewel(Properties properties) {
        super(properties);
    }

    /** 读取该珠宝物品上的珠宝数据（无组件时返回 {@link JewelData#EMPTY}）。 */
    public static JewelData dataOf(ItemStack stack) {
        JewelData data = stack.get(DataComponentRegistry.JEWEL_DATA.get());
        return data == null ? JewelData.EMPTY : data;
    }

    @Override
    public void appendHoverText(
            ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> toolTip, TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, display, toolTip, flag);
        JewelData data = dataOf(stack);
        toolTip.accept(Component.translatable("item.coe.jewel.hint").withStyle(ChatFormatting.DARK_GRAY));
        for (TalentEffect eff : data.effects()) {
            for (Component line : eff.describe(ClientLocale.get())) {
                toolTip.accept(line.copy().withStyle(ChatFormatting.AQUA));
            }
        }
        if (data.radiates()) {
            toolTip.accept(Component.translatable("jewel.coe.radius", (int) data.radius()).withStyle(ChatFormatting.GOLD));
        } else {
            toolTip.accept(Component.translatable("jewel.coe.base").withStyle(ChatFormatting.GRAY));
        }
    }
}
