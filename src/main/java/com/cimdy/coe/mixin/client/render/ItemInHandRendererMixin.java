package com.cimdy.coe.mixin.client.render;

import com.cimdy.coe.item.accessory.Buckler;
import com.cimdy.coe.item.special.Foci;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 第一人称手持渲染：为 Foci（法器）与 Buckler（轻盾）注入独特姿势。
 * <p>
 * 原版 {@code ItemInHandRenderer#submitArmWithItem} 中，{@code ShieldItem} 子类在 {@code BLOCK} 动画分支
 * 下（第 504 行 {@code case BLOCK}）走原版持盾姿势，已能呈现举起动作。本 Mixin 在其之前拦截，
 * 为 Buckler 复刻并强化「举盾 → 招架姿态」与「反击 quick sweep」动画：
 * <ul>
 *   <li>Foci：维持原版 BLOCK 持盾姿势（不拦截，直接走原版）。</li>
 *   <li>Buckler 招架姿态：玩家右键举起轻盾（{@code isUsingItem} + 副手）时，采用更靠前、手臂横张的独特招架姿势。</li>
 *   <li>反击 sweep：检测到招架刚进入（边沿）时，播放一段短暂的横向挥扫动画（本地 client tick 计时，不依赖服务端 PARRY_CONSUMED 同步）。</li>
 * </ul>
 * 通过 {@code cancellable} + {@code ci.cancel()} 在命中自定义分支时完全接管手臂与物品变换，避免与原版 BLOCK 分支叠加。
 * 其中 {@code renderItem} 为 public 方法，可直接 {@code this.renderItem(...)} 调用，无需 shadow；
 * 仅 private 的 {@code renderPlayerArm} 需要 {@link Shadow}。
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow
    private void renderPlayerArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, float attack, HumanoidArm arm) {
    }

    @Shadow
    public void renderItem(LivingEntity mob, ItemStack itemStack, ItemDisplayContext type, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
    }

    /** 每个玩家上一帧是否处于招架姿态，用于检测「进入招架」边沿以触发反击 sweep 动画。 */
    private static final Map<UUID, Boolean> LAST_PARRYING = new HashMap<>();
    /** 每个玩家反击 sweep 动画起始 client tick。 */
    private static final Map<UUID, Long> PARRY_SWEEP_START = new HashMap<>();
    /** 反击 sweep 动画总时长（client tick）。 */
    private static final long PARRY_SWEEP_DURATION = 8L;

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true,
            // 注入点待实机验证：require=0 使该注入可选，避免 defaultRequire:1 + required:true 下注入点不匹配直接崩溃。
            // 实机验证通过后再移除（恢复全局 defaultRequire:1 的严格校验）。
            require = 0)
    private void submitArmWithItem(AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand,
                                   float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack,
                                   SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        // 仅处理副手（offhand）——Foci 与 Buckler 均为副手装备。
        if (hand != InteractionHand.OFF_HAND) return;

        boolean isFoci = itemStack.getItem() instanceof Foci;
        boolean isBuckler = itemStack.getItem() instanceof Buckler;
        if (!isFoci && !isBuckler) return;

        // 招架/举盾姿态：玩家正持续使用副手物品（右键举起）。
        boolean parrying = player.isUsingItem()
                && player.getUsedItemHand() == InteractionHand.OFF_HAND
                && itemStack.getItem().getUseAnimation(itemStack) == ItemUseAnimation.BLOCK;

        UUID id = player.getUUID();

        if (!parrying) {
            // 离开招架姿态：清理本地状态。
            LAST_PARRYING.put(id, false);
            PARRY_SWEEP_START.remove(id);
            return; // 走原版（Foci/Buckler 非招架时由原版 BLOCK 分支渲染持盾姿势）
        }

        // 检测「进入招架」边沿 → 触发反击 sweep 动画。
        boolean wasParrying = LAST_PARRYING.getOrDefault(id, false);
        if (!wasParrying) {
            PARRY_SWEEP_START.put(id, player.level().getGameTime());
        }
        LAST_PARRYING.put(id, true);

        HumanoidArm arm = player.getMainArm().getOpposite(); // 副手对应的手臂
        int invert = arm == HumanoidArm.RIGHT ? 1 : -1;

        poseStack.pushPose();

        // 手臂基础变换（手臂抬到身前，近似原版 applyItemArmTransform）。
        this.renderPlayerArm(poseStack, submitNodeCollector, lightCoords, inverseArmHeight, attack, arm);

        // 招架独特物品姿势：前臂横张、盾面略微朝向正前方偏侧。
        poseStack.translate(invert * -0.28F, 0.22F, 0.32F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-35.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * 28.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(invert * 62.0F));

        // 反击 quick sweep 动画：进入招架后短暂横向挥扫。
        Long sweepStart = PARRY_SWEEP_START.get(id);
        if (sweepStart != null) {
            long elapsed = player.level().getGameTime() - sweepStart;
            if (elapsed >= 0 && elapsed <= PARRY_SWEEP_DURATION) {
                float t = (float) elapsed / (float) PARRY_SWEEP_DURATION; // 0..1
                float sweep = Mth.sin(t * (float) Math.PI); // 0→1→0 的挥扫包络
                poseStack.mulPose(Axis.YP.rotationDegrees(invert * sweep * 55.0F));
                poseStack.translate(invert * sweep * 0.18F, 0.0F, 0.0F);
            } else {
                PARRY_SWEEP_START.remove(id);
            }
        }

        // 渲染物品（第一人称副手上下文）。
        this.renderItem(
                player,
                itemStack,
                arm == HumanoidArm.RIGHT ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                poseStack,
                submitNodeCollector,
                lightCoords
        );

        poseStack.popPose();
        ci.cancel(); // 完全接管，避免与原版 BLOCK 分支叠加
    }
}
