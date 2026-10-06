package com.cimdy.coe.item.gem;

import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.skill.gem.SupportData;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * 辅助宝石（Support Gem）：插入技能宝石插槽、修改被辅助技能行为的物品。
 *
 * <p>辅助宝石的元数据存储在 {@link DataComponentRegistry#SUPPORT_DATA} 组件中（{@link SupportData}），
 * 悬浮提示会在物品名之后追加该辅助宝石的效果描述（{@code gem.support.description.coe.&lt;id&gt;}）。
 */
public class SupportGemItem extends Gem {
    public SupportGemItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> toolTip, TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, display, toolTip, flag);
        SupportData data = stack.get(DataComponentRegistry.SUPPORT_DATA.get());
        if (data != null && !data.isEmpty() && data.id() != null) {
            toolTip.accept(Component.translatable("gem.support.description.coe." + data.id().getPath()));
        }
    }
}
