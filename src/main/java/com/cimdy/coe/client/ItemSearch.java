package com.cimdy.coe.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 物品搜索（从旧 {@code AdaptiveInputBox} 迁出的业务逻辑）。
 *
 * <p>语法：{@code @命名空间 关键词1 关键词2...}。
 * 命名空间为部分匹配过滤；关键词之间是 AND 关系，每个关键词按「注册路径 / 显示名 / tooltip 文本」
 * 正则匹配（正则非法时降级为普通包含）。
 */
public final class ItemSearch {

    private ItemSearch() {}

    public static boolean matchesInput(String input, ItemStack stack) {
        if (input == null || input.isBlank() || stack.isEmpty()) return false;

        String namespaceFilter = null;
        String rest = input;
        if (input.startsWith("@")) {
            int spaceIdx = input.indexOf(' ');
            if (spaceIdx > 0) {
                namespaceFilter = input.substring(1, spaceIdx);
                rest = input.substring(spaceIdx + 1).trim();
            } else {
                namespaceFilter = input.substring(1);
                rest = "";
            }
        }
        if (namespaceFilter != null) {
            String itemNs = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
            if (!itemNs.toLowerCase().contains(namespaceFilter.toLowerCase())) return false;
        }
        if (rest.isEmpty()) return true;

        String itemPath = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        String displayName = stack.getHoverName().getString();
        List<String> tooltipLines = new ArrayList<>();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            try {
                List<Component> lines = stack.getTooltipLines(
                        Item.TooltipContext.EMPTY,
                        Minecraft.getInstance().player,
                        TooltipFlag.Default.NORMAL);
                for (Component c : lines) tooltipLines.add(c.getString());
            } catch (Exception ignored) { /* 某些物品在客户端渲染 tooltip 会抛异常，忽略 */ }
        }

        for (String kw : rest.split("\\s+")) {
            if (kw.isEmpty()) continue;
            Pattern p;
            try {
                p = Pattern.compile(kw, Pattern.CASE_INSENSITIVE);
            } catch (Exception e) {
                p = Pattern.compile(Pattern.quote(kw), Pattern.CASE_INSENSITIVE);
            }
            boolean found = p.matcher(itemPath).find() || p.matcher(displayName).find();
            if (!found) {
                for (String line : tooltipLines) {
                    if (p.matcher(line).find()) { found = true; break; }
                }
            }
            if (!found) return false;
        }
        return true;
    }
}
