package com.cimdy.coe.client.animation;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.network.input.DodgeRollAnimPacket;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.UUID;

/**
 * 翻滚动画客户端入口：注册动画层工厂 + 提供按 UUID 播放翻滚动画的入口。
 * 服务端把翻滚玩家的 UUID 经 {@code DodgeRollAnimPacket} 广播，本类在每个客户端
 * 找到对应玩家实体并播放，确保本人与他者都能看到翻滚动画。
 * 注册入口在 {@link CraftOfExile} 构造器（MOD 总线）。
 */
public class DodgeRollAnimation {

    private DodgeRollAnimation() {
    }

    /** 客户端翻滚进行中的截止游戏时刻（SprintingMixin 据此在翻滚期间禁止奔跑）。 */
    private static long clientRollUntil = 0;
    /** 客户端被击倒地调整动作的截止游戏时刻（窗口内 SprintingMixin 禁止立即重新奔跑）。 */
    private static long clientDownedUntil = 0;

    /** 在 MOD 总线注册客户端设置回调（工厂注册须早于任何玩家实体创建）。 */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(DodgeRollAnimation::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        // 注册翻滚动画层：每个 Avatar（玩家）创建时 PAL 调用工厂生成控制器并挂到动画栈。
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                DodgeRollAnimator.LAYER_ID, DodgeRollAnimator.LAYER_PRIORITY, DodgeRollAnimator::new);
    }

    /** 在指定玩家实体上播放翻滚 / 调整动作动画（由其 UUID 定位，本人/他者通用）。 */
    public static void onRoll(UUID playerUuid, int kind, int rollTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (mc.level.getEntity(playerUuid) instanceof Player player) {
            // 记录客户端窗口：翻滚中 / 空翻中 / 被击倒地调整中，供 SprintingMixin 抑制原版 sprint 触发
            if (kind == DodgeRollAnimPacket.KIND_RECOVERY) {
                clientDownedUntil = mc.level.getGameTime() + rollTicks;
            } else {
                clientRollUntil = mc.level.getGameTime() + rollTicks; // 普通翻滚与空翻均抑制 sprint
            }
            if (PlayerAnimationAccess.getPlayerAnimationLayer(player, DodgeRollAnimator.LAYER_ID)
                    instanceof DodgeRollAnimator animator) {
                if (kind == DodgeRollAnimPacket.KIND_RECOVERY) animator.playRecovery(rollTicks);
                else if (kind == DodgeRollAnimPacket.KIND_FLIP) animator.playFlip(rollTicks);
                else animator.playRoll(rollTicks);
            }
        }
    }

    /** 客户端：本地玩家是否正处于翻滚动画窗口（SprintingMixin 据此禁止奔跑）。 */
    public static boolean isClientRolling(Player player) {
        return player.level().getGameTime() < clientRollUntil;
    }

    /** 客户端：本地玩家是否正处于被击倒地调整窗口（SprintingMixin 据此禁止立即重新奔跑）。 */
    public static boolean isClientDowned(Player player) {
        return player.level().getGameTime() < clientDownedUntil;
    }
}
