package com.cimdy.coe.ui.widget.compose.item;

import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 物品 tooltip 混淆渲染（计划 §3.3 / 规格 §2）：未识别 / 未鉴定态——
 * 名称遮蔽、词缀行占位灰显（暗黑 / POE 风格）。
 *
 * <p>纯函数工具类，不持 UI 状态。只负责把 tooltip 行列表改写成「遮蔽样式」的行列表；
 * 行内灰显颜色由渲染层（行内颜色 / DetailPanel）按 {@code Theme.current().textDisabled()} 处理，
 * 本类不写死色值。
 *
 * <p>遮蔽名走单一翻译键 {@code ui.coe.obfuscated.name}（带 {@code %s} = 原物品名）；
 * 词缀占位走 {@code ui.coe.obfuscated.mods}。
 */
public final class ObfuscatedTooltip {

    private ObfuscatedTooltip() {}

    /**
     * 改写 tooltip 为未鉴定样式：首行 → 遮蔽名（{@code ui.coe.obfuscated.name}，{@code %s}=原首行），
     * 其余行 → 灰显占位 {@code ui.coe.obfuscated.mods}。
     *
     * <p>传入 {@code null} / 空列表原样返回（不包装）。
     */
    public static List<Component> obfuscate(List<Component> baseLines) {
        if (baseLines == null || baseLines.isEmpty()) return baseLines;
        Component originalName = baseLines.get(0);
        Component maskedName = Component.translatable("ui.coe.obfuscated.name", originalName);
        Component mods = Component.translatable("ui.coe.obfuscated.mods");
        return List.of(maskedName, mods);
    }

    /**
     * 混淆判定：装备载体未鉴定（{@code EquipmentState.wisdom()==false}）即为未识别态。
     *
     * <p>已接入真实判定（修复批 3）：客户端 ItemStack 上可读 {@link EquipmentState} 的
     * 「已鉴定」标记（与服务端同一份状态组件）；非装备载体（药剂 / 护符等无 wisdom 标记）
     * 视作已识别、不混淆。异常安全（任何读取失败回退不混淆）。
     */
    public static boolean isObfuscated(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        try {
            return ModifiableStateOps.get(stack).orElse(null) instanceof EquipmentState eq && !eq.wisdom();
        } catch (Exception e) {
            return false;
        }
    }
}
