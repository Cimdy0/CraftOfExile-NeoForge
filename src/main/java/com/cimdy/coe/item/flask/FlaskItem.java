package com.cimdy.coe.item.flask;

import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.flask.FlaskState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;

/**
 * Flask（药剂，POE2 实证）：可重复使用的消耗品，右键使用消耗充能（charges）发动<b>持续恢复</b>效果
 * （总量在 duration 内每 tick 回复，满资源时效果移除），充能随时间/击杀恢复。
 * <p>
 * 机制全部收敛在 {@link FlaskHelper}（充能/豁免/持续恢复/击杀充能/回复通道），本类只承载
 * 物品静态数据（资源键、恢复量、时长、充能规格）与右键入口（手持时饮用）。
 * 槽位使用（按键/背包 UI）直接调 {@link FlaskHelper#useSlot} / {@link FlaskHelper#useAll}。
 * <p>
 * 数值取 POE2 基础规格：Lesser Life Flask（50 生命 / 3 秒，消耗 10/60 充能）、
 * Lesser Mana Flask（50 魔力 / 2 秒，消耗 10/60 充能）。
 */
public class FlaskItem extends Item {

    /** 资源键（GateTag 资源标签：{@code health} / {@code mana}）。 */
    final String resource;
    /** 单次使用的基础恢复总量（再乘 FLASK_RECOVERY_RATE）。 */
    final float recoveryAmount;
    /** 恢复持续时间（tick；POE2 基础 2–5 秒）。 */
    final int durationTicks;
    /** 最大充能数。 */
    final int maxCharges;
    /** 单次使用消耗充能数。 */
    final int chargesPerUse;

    /** 工厂方法：从数据状态 {@link FlaskState} 生成药剂物品（CODEC 工厂方法数据生成）。 */
    public static FlaskItem of(Properties pProperties, FlaskState state) {
        return new FlaskItem(pProperties, state);
    }

    public FlaskItem(Properties pProperties, FlaskState state) {
        super(pProperties.component(DataComponentRegistry.MODIFIABLE_STATE.get(), state));
        this.resource = state.resource();
        this.recoveryAmount = state.recoveryAmount();
        this.durationTicks = state.durationTicks();
        this.maxCharges = state.maxCharges();
        this.chargesPerUse = state.chargesPerUse();
    }

    /** 读充能次数（缺省 = 满充能，兼容未初始化栈）。 */
    public int charges(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.FLASK_CHARGES.get(), maxCharges);
    }

    /** 单次使用消耗的充能数（渲染侧画「可用次数」刻度需要）。 */
    public int chargesPerUse() {
        return chargesPerUse;
    }

    /** 该药剂回复的资源键（{@code health} / {@code mana} / …）。 */
    public String resource() {
        return resource;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        FlaskHelper.drinkInHand(player, player.getItemInHand(hand), this);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> toolTip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, toolTip, flag);
        // 品质为逐栈数据（玻璃珠），展示值须按栈读取、而非物品静态字段
        FlaskState perStack = ModifiableStateOps.flask(stack);
        float shownRecovery = perStack != null ? perStack.qualityScaledRecovery() : recoveryAmount;
        toolTip.accept(Component.translatable("item.coe.flask.recovery",
                String.format("%.1f", durationTicks / 20.0),
                String.format("%.0f", shownRecovery),
                Component.translatable("stat." + resource)));
        if (perStack != null && perStack.quality() > 0) {
            toolTip.accept(Component.translatable("item.coe.quality")
                    .withStyle(ChatFormatting.DARK_GRAY).append(": ")
                    .append(Component.literal("+" + perStack.quality() + "%").withStyle(ChatFormatting.BLUE)));
        }
        toolTip.accept(Component.translatable("item.coe.flask.charges",
                charges(stack) + "/" + FlaskHelper.flaskMaxCharges(stack, this)));
        toolTip.accept(Component.translatable("item.coe.flask.per_use", chargesPerUse));
    }
}
