package com.cimdy.coe.data.helper.mod;

import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import com.cimdy.coe.data.codec.item.registry.ItemDataRegistry;
import com.cimdy.coe.data.codec.vanilla.EnchantModifier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EnchantIntegrator {

    private EnchantIntegrator() {}

    /**
     * 把 vanilla 附魔翻译成 {@link ModifierEntry}：<b>附魔不是词缀</b>（不入 {@code Location} 的前后缀桶），
     * 故这里返回<b>平铺列表</b>而非「以落位为键的桶」—— 附魔恒落 {@code ModLayer.ENCHANT} 层，
     * 只有一个桶，用不上键。调用方自行决定如何与词缀并列（渲染 / 贡献 / 判定各取所需）。
     */
    public static List<ModifierEntry> getEnchantMods(ItemEnchantments enchantments) {
        List<ModifierEntry> mods = new ArrayList<>();
        if (!enchantments.isEmpty()) {
            enchantments.keySet().forEach(enchantment -> {
                EnchantModifier.Builder builder = getEnchantLocation(enchantment);
                mods.add(builder.build(enchantment.getKey(), enchantments.getLevel(enchantment)));
            });
        }
        return mods;
    }

    public static EnchantModifier.Builder getEnchantLocation(Holder<Enchantment> enchantment) {
        for (Map.Entry<ResourceKey<Enchantment>, EnchantModifier.Builder> entry : ItemDataRegistry.getInstance().getEnchantModifiers().entrySet()) {
            if (enchantment.is(entry.getKey())) {
                return entry.getValue();
            }
        }
        return EnchantModifier.Builder.DEFAULT;
    }

    public static boolean hasThisEnchantModifier(Holder<Enchantment> enchantment) {
        return ItemDataRegistry.getInstance().getEnchantModifiers().containsKey(enchantment.getKey());
    }
}
