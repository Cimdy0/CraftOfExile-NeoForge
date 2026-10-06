package com.cimdy.coe.mixin.attribute.projectile;

import com.cimdy.coe.data.codec.skill.helper.cast.ProjectileHelper;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 弩装填完成点 → 记录 {@link EventName#RELOADED} 事件。
 *
 * <p>注入 {@code CrossbowItem#tryLoadProjectiles} 的 RETURN：该方法在 {@code CrossbowItem#onUseTick}
 * 中当充能进度达 100%、弩尚未上膛时被调用，返回 {@code true} 即代表弹药成功上膛（装填完成）——
 * 这是「你已装填」的最精确语义点（比 {@code performShooting} 更贴近装填时刻）。
 *
 * <p>仅服务端记录（{@code onUseTick} 内已包在 {@code !level.isClientSide()} 分支中，此处再防御性判断），
 * owner=装填者。RELOADED 已登记为机制必需事件（{@link com.cimdy.coe.event.EventSubscription}），
 * 故 Java 读点 {@code ProjectileHelper#useAmmo} 经 {@code Event.get(shooter).recently(RELOADED, 80)} 真实可读。
 */
@Mixin(net.minecraft.world.item.CrossbowItem.class)
public class CrossbowReload {

    @Inject(method = "tryLoadProjectiles", at = @At("RETURN"))
    private static void coe$recordReload(LivingEntity shooter, ItemStack heldItem, CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue()) && !shooter.level().isClientSide()) {
            Event.record(shooter, EventName.RELOADED);
        }
    }
}
