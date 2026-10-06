package com.cimdy.coe.item.gem;

import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.state.GemState;
import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.ui.screen.gem.CutScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 未切割宝石（技能 / 辅助 / 精魂）。右键打开对应的切割界面，
 * 切割界面默认按本宝石的类型与等阶过滤可切割的宝石。
 *
 * <p>等阶（tier）存储在统一组件 {@link DataComponentRegistry#MODIFIABLE_STATE} 内的 {@link GemState#tier()}，
 * 不同等阶的未切割宝石只能切出对应等阶的宝石（POE2 规则）。
 */
public class UncutGem extends Gem {
    /** 未切割宝石等阶范围：主动/精魂 1–20，辅助 1–5。 */
    public static final int SKILL_TIER_MAX = 20;
    public static final int SUPPORT_TIER_MAX = 5;

    private final String cutType;

    public UncutGem(Properties properties, String cutType) {
        super(properties);
        this.cutType = cutType;
    }

    public String getCutType() {
        return cutType;
    }

    /** 读取未切割宝石的等阶（{@link GemState#tier()}），缺省为 1。等阶 ≠ 物等。 */
    public static int getTier(ItemStack stack) {
        ModifiableState s = stack.get(DataComponentRegistry.MODIFIABLE_STATE);
        return s instanceof GemState g ? g.tier() : 1;
    }

    /** 名称标注等阶（罗马数字），便于在创造物品栏 / 背包中区分不同等阶的未切割宝石。 */
    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(stack.getItem().getDescriptionId())
                .append(Component.literal(" "))
                .append(Component.translatable("item.coe.uncut_gem.tier", toRoman(getTier(stack))));
    }

    /** 将 1–3999 的正整数转换为罗马数字（未切割宝石等阶最高 20，足够覆盖）。 */
    public static String toRoman(int n) {
        if (n <= 0) return String.valueOf(n);
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length && n > 0; i++) {
            while (n >= values[i]) {
                sb.append(symbols[i]);
                n -= values[i];
            }
        }
        return sb.toString();
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (!other.isEmpty() || action != ClickAction.SECONDARY) {
            return false;
        }
        // 切割界面是客户端 UI：仅客户端打开；服务端同样会走本钩子（容器点击双端一致），
        // 直接引用 Minecraft 客户端类会崩溃，故加 side 守卫。
        if (player.level().isClientSide()) {
            Minecraft.getInstance().setScreenAndShow(new CutScreen(player, slot, cutType, getTier(stack)));
        }
        return true;
    }
}
