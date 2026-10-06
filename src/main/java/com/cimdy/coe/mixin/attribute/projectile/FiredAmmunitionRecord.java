package com.cimdy.coe.mixin.attribute.projectile;

import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Set;

/**
 * 原版箭矢「发射弹药身份」记录钩子（与投射物全量坍塌彻底分离的独立接线）。
 *
 * <p><b>为何独立成文件</b>：投射物体系已全量坍塌为数据驱动基类 {@code SkillEntity}，
 * 箭矢加成（{@code {arrow}} 标签作用域）改由 {@code EntityDef} 的 {@code arrow} 标签
 * 经 {@code EntityDefCarrier} 判定，不再依赖 {@code instanceof AbstractArrow}。
 * 但「过去 N 秒内每发射过一种不同弹药，弩伤 +%」词条（StatId
 * {@code INC_DAMAGE_WITH_CROSSBOWS_FOR_EACH_TYPE_OF_AMMUNITION_FIRED_IN_THE_PAST_SECONDS}）
 * 读的是原版箭矢携带的发射物品（{@link AbstractArrow#getPickupItemStackOrigin()}），
 * 与坍塌体系无关。此钩子只负责恢复该事件的唯一写方，不触碰任何坍塌逻辑。
 *
 * <p><b>作用域严格限定</b>：仅当发射物是原版 {@link AbstractArrow} 时记录。
 * CoE 的技能投射物宿主是 {@code SkillEntity}（继承 {@code Entity}，非 {@code AbstractArrow}），
 * 永不进入本分支；且技能投射物也不经 {@code ProjectileWeaponItem.shootProjectile} 发射。
 * <b>{@code {arrow}} 标签作用域不受本文件任何影响</b>（仍只由 EntityDef 标签决定）。
 *
 * <p><b>记录时机</b>：<b>发射瞬间</b>（{@code shootProjectile} 调用点，箭矢刚由发射物品构造、
 * {@code getPickupItemStackOrigin()} 已就绪时）。不选命中时刻——命中时箭矢可能已被回收/销毁，
 * 且语义是「发射过几种弹药」而非「命中过」。
 *
 * <p><b>按需订阅</b>：本写点发出的 {@code fired_ammunition} 由 {@code WireDamage} 的
 * {@code distinct:fired_ammunition.<secs>:ammo} 读方经 {@code EventRefCollector} 动态订阅
 * （{@code EventSubscription} 的 {@code COUNTS}），故无需加入 MECHANICAL 白名单
 * ——它与「纯 Java 读点」的 {@code RELOADED} 不同类，后者才必须恒记录。
 */
@Mixin(ProjectileWeaponItem.class)
public class FiredAmmunitionRecord {
    @ModifyArgs(method = "lambda$shoot$0",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ProjectileWeaponItem;shootProjectile(Lnet/minecraft/world/entity/LivingEntity;" +
                            "Lnet/minecraft/world/entity/projectile/Projectile;IFFFLnet/minecraft/world/entity/LivingEntity;)V"))
    private void recordFiredAmmunition(Args args) {
        LivingEntity shooter = args.get(0);
        Projectile projectile = args.get(1);

        // 只记原版箭矢：技能投射物（SkillEntity）不是 AbstractArrow，永不进入本分支。
        if (!(projectile instanceof AbstractArrow arrow) || arrow.level().isClientSide()) return;
        // 弹药身份 = 箭矢携带的发射物品；仅进计数队列（Event.record 的 extraSubjectTag），
        // 供「过去 N 秒内发射过几种不同弹药」去重统计读取。
        ItemStack origin = arrow.getPickupItemStackOrigin();
        if (origin.isEmpty()) return;
        String typeId = BuiltInRegistries.ITEM.getKey(origin.getItem()).toString();
        Event.record(shooter, EventName.FIRED_AMMUNITION, Set.of(), "ammo:" + typeId);
    }
}
