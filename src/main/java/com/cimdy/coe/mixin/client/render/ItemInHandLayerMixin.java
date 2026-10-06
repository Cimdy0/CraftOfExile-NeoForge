package com.cimdy.coe.mixin.client.render;

import com.cimdy.coe.item.accessory.Buckler;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 第三人称手持渲染：为 Buckler（轻盾）招架姿态注入独特姿势与反击 quick sweep 动画。
 * <p>
 * 第三人称手持物品由 {@code ItemInHandLayer#submitArmWithItem} 渲染：
 * 先在手臂末端（{@code translateToHand}）摆放物品，再调用 {@code armPose.animateUseItem} 处理使用动画
 * （举盾 BLOCK 姿势即在此呈现），最后 {@code ItemStackRenderState#submit} 提交渲染。
 * 本 Mixin 在 {@code submit} 调用之前注入，于已完成的手臂变换基础上叠加招架独特偏移/旋转，
 * 不接管、不复制原版逻辑，仅追加变换，最大限度降低冲突风险。
 * <p>
 * 招架姿态判定基于客户端 {@code ArmedEntityRenderState} 快照字段（{@code isUsingItem} + 副手 + BLOCK 动画），
 * 不依赖 {@code Event}/{@code ExpressionContext} 实时读取，因此对所有玩家（本地与其他客户端实体）一致生效。
 * 反击 sweep 动画进度直接取自 {@code state.ticksUsingItem(arm)}（进入招架后从 0 增长），在 0~8 tick 内按正弦包络播摆一次。
 * <p>
 * 注意：未实机验证，参数（偏移/旋转角度、sweep 幅度）与注入点（{@code ItemStackRenderState#submit} 描述符）待实测修正。
 */
@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {

    @Inject(
        method = "submitArmWithItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
        ),
        // 注入点待实机验证：require=0 使该注入可选，避免 defaultRequire:1 + required:true 下注入点不匹配直接崩溃。
        // 实机验证通过后再移除（恢复全局 defaultRequire:1 的严格校验）。
        require = 0
    )
    private void onThirdPersonSubmit(
        ArmedEntityRenderState state, ItemStackRenderState item, ItemStack itemStack, HumanoidArm arm,
        com.mojang.blaze3d.vertex.PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
        CallbackInfo ci
    ) {
        // Buckler 仅可装备副手（equippableUnswappable(OFF_HAND)），故 instanceof 即代表副手
        if (!(itemStack.getItem() instanceof Buckler)) return;

        // 招架/举盾姿态：Buckler 为 BLOCK 使用动画且正被使用（ticksUsingItem > 0）
        boolean parrying = itemStack.getItem().getUseAnimation(itemStack) == ItemUseAnimation.BLOCK
                && state.ticksUsingItem(arm) > 0.0F;
        if (!parrying) return;

        int invert = arm == HumanoidArm.RIGHT ? 1 : -1;

        // 招架独特姿势：在已完成的手臂变换基础上叠加偏移/旋转（盾面略偏前、手臂横张）
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * 18.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-12.0F));
        poseStack.translate(invert * -0.12F, 0.08F, 0.12F);

        // 反击 quick sweep：进入招架后基于 ticksUsingItem 在 0~8 tick 内播摆一次
        float ticks = state.ticksUsingItem(arm);
        if (ticks >= 0.0F && ticks <= 8.0F) {
            float sweep = Mth.sin((ticks / 8.0F) * (float) Math.PI); // 0→1→0 包络
            poseStack.mulPose(Axis.YP.rotationDegrees(invert * sweep * 40.0F));
        }
    }
}
