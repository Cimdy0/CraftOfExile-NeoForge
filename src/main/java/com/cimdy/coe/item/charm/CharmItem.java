package com.cimdy.coe.item.charm;

import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.state.CharmState;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

/**
 * Charm（护符，POE2 实证）：装备在背包 UI 的 Charm 槽中，<b>自动触发</b>——
 * 当满足 {@link #trigger} 条件时消耗 {@link #chargesUsed} 充能，经中央 {@link CharmEngine} 把
 * {@link #effects} 注入玩家属性层（抗性/免疫/稀有度增益），无需手动按键。
 * <p>
 * 数值取 POE2 基础规格：上限 80 充能（击杀怪物补充 / 井水充满），各护符按次消耗 40–80 充能。
 * 本类只承载静态数据（触发条件、效果条目、时长、充能规格），机制全部收敛在 {@link CharmEngine}。
 * 效果为纯数据（{@link StatEntry} 列表），由 {@link CharmEngine} 真读落地，无硬编码状态。
 */
public class CharmItem extends Item {

    /** 触发条件（何种事件自动激活）。 */
    public final CharmTrigger trigger;
    /** 激活时注入玩家属性层的效果条目（数据驱动，引用 coe:charm_* 模板）。 */
    public final List<StatEntry> effects;
    /** 效果展示用翻译键（tooltip 显示，不触发客户端模板解析，R3 安全）。 */
    public final String effectLabel;
    /** 效果持续 tick（POE2：3–4 秒级）。 */
    public final int durationTicks;
    /** 单次激活消耗充能数。 */
    public final int chargesUsed;
    /** 充能上限（POE2 固定 80）。 */
    public final int maxCharges;

    /** 工厂方法：从数据状态 {@link CharmState} 生成护符物品（CODEC 工厂方法数据生成）。 */
    public static CharmItem of(Properties p, CharmState state) {
        return new CharmItem(p, state);
    }

    public CharmItem(Properties p, CharmState state) {
        super(p.component(DataComponentRegistry.MODIFIABLE_STATE.get(), state));
        this.trigger = state.trigger();
        this.effects = state.effects();
        this.effectLabel = state.effectLabel();
        this.durationTicks = state.durationTicks();
        this.chargesUsed = state.chargesUsed();
        this.maxCharges = state.maxCharges();
    }

    /** 读充能次数（缺省 = 满充能，兼容未初始化栈）。 */
    public int charges(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.CHARM_CHARGES.get(), maxCharges);
    }

    /** 写充能次数（钳制到 [0, 有效上限]；有效上限含「#% increased Charges」词缀，由 CharmEngine.charmMaxCharges 计算）。 */
    public void setCharges(ItemStack stack, int v) {
        int cap = CharmEngine.charmMaxCharges(stack, this);
        stack.set(DataComponentRegistry.CHARM_CHARGES.get(), Math.max(0, Math.min(cap, v)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                               Consumer<Component> toolTip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, toolTip, flag);
        toolTip.accept(Component.translatable("item.coe.charm.trigger",
                Component.translatable("charm.trigger." + trigger.name().toLowerCase())));
        toolTip.accept(Component.translatable("item.coe.charm.effect",
                Component.translatable(effectLabel)));
        toolTip.accept(Component.translatable("item.coe.charm.charges",
                charges(stack) + "/" + CharmEngine.charmMaxCharges(stack, this)));
        toolTip.accept(Component.translatable("item.coe.charm.used", chargesUsed));
    }
}
