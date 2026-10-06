package com.cimdy.coe.item.currency;

import com.cimdy.coe.registry.item.ItemRegistry;

import com.cimdy.coe.data.codec.item.currency.CurrencyOperation;
import com.cimdy.coe.item.currency.CurrencyApplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

/**
 * 通货物品基类。具体工艺完全由构造时传入的 {@link CurrencyOperation} 描述，
 * 样板（镜像/鉴定/稀有度/类型 校验 + 消耗 + 音效）统一在 {@link CurrencyApplier} 中实现。
 * 通货不再逐一定义子类——由 {@link CurrencyType} 枚举缓存数据、{@link ItemRegistry} 按枚举注册。
 */
public class BaseCurrency extends Item {
    private final CurrencyOperation operation;

    public BaseCurrency(Properties pProperties, CurrencyOperation operation) {
        super(pProperties);
        this.operation = operation;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack carriedItem, Slot slot, ClickAction action, Player player) {
        return CurrencyApplier.apply(operation, carriedItem, slot, action, player);
    }

    protected CurrencyOperation operation() {
        return operation;
    }

    @Override
    public void appendHoverText(
            ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> toolTip, TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, display, toolTip, flag);
        toolTip.accept(Component.translatable("item.coe.currency")
                .withStyle(ChatFormatting.DARK_GRAY));
        toolTip.accept(Component.translatable("item.coe.stack_size").append(": ")
                .withStyle(ChatFormatting.DARK_GRAY).append("§f" + stack.getCount() + "/" + stack.getMaxStackSize()));
        toolTip.accept(Component.literal(""));
        toolTip.accept(Component.translatable("item.currency." + stack.getItem() + ".tooltip1")
                .withStyle(ChatFormatting.BLUE));
        toolTip.accept(Component.literal(""));
        toolTip.accept(Component.translatable("item.currency." + stack.getItem() + ".tooltip2")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
