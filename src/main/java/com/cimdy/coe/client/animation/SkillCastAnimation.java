package com.cimdy.coe.client.animation;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.skill.runtime.SkillCastAnimator;
import com.cimdy.coe.ui.widget.render.GuiSkin;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Optional;

/**
 * 技能施放动画（客户端）：注册 {@link SkillCastAnimator} 动画层，并承接服务端
 * {@code CastAnimation} payload —— 施放成功时对本地玩家播放施法动画（随施放速度变速），
 * 施放打断时停止。
 * <p>注册入口在 {@link CraftOfExile} 构造器（MOD 总线）；播放处理器仅客户端执行。
 */
public class SkillCastAnimation {

    private SkillCastAnimation() {
    }

    /** 在 MOD 总线注册客户端设置回调（工厂注册须早于任何玩家实体创建）。 */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(SkillCastAnimation::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        // 注册施法动画层：每个 Avatar（玩家）创建时 PAL 调用工厂生成控制器并挂到动画栈。
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                SkillCastAnimator.LAYER_ID, SkillCastAnimator.LAYER_PRIORITY, SkillCastAnimator::new);
        // 资源包重载（F3+T）后清空 GUI 贴图存在性缓存，新贴图立即生效，无需重启游戏
        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager())
                .registerReloadListener(
                        (ResourceManagerReloadListener) resourceManager -> GuiSkin.invalidateAll());
    }

    /**
     * 施放动画入口（服务端 CastAnimation payload 处理器调用，仅客户端主线程执行）。
     *
     * @param skillId       本次施放的技能 id
     * @param requested     技能显式声明的动画（可为空）
     * @param castTimeTicks 施放时长（game tick）；{@code <=0} 表示打断/停止
     */
    public static void onCast(Identifier skillId, Optional<Identifier> requested, int castTimeTicks, long startTime) {
        if (Minecraft.getInstance().player == null) return;
        if (PlayerAnimationAccess.getPlayerAnimationLayer(
                Minecraft.getInstance().player, SkillCastAnimator.LAYER_ID) instanceof SkillCastAnimator animator) {
            if (castTimeTicks <= 0) {
                animator.stopCast();
            } else {
                animator.playCast(requested.orElse(null), skillId, castTimeTicks, startTime);
            }
        }
    }
}
