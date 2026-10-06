package com.cimdy.coe.mixin.attribute.projectile;

import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.entity.EntityDefCarrier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.HashSet;
import java.util.Set;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileInShoot {
    @ModifyArgs(method = "lambda$shoot$0",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ProjectileWeaponItem;shootProjectile(Lnet/minecraft/world/entity/LivingEntity;" +
                            "Lnet/minecraft/world/entity/projectile/Projectile;IFFFLnet/minecraft/world/entity/LivingEntity;)V"))
    private void modifyArgs(Args args) {
        LivingEntity shooter = args.get(0);
        Projectile projectile = args.get(1);
        float speed = args.get(3);
        float inaccuracy = args.get(4);

        // 归一：PROJECTILE_SPEED/PROJECTILE_RANDOM_FIRE 单属性 + {projectile} 标签作用域；
        // 箭矢作用域 {arrow}：仅由数据驱动载体（如 coe:skill_arrow 指向的 SkillEntity）经 EntityDef
        // 标签集判定（tagSet 须含 GateTag.ARROW）赋予，使技能箭矢吃到 {arrow} 单属性（替代原
        // ARROW_SPEED/ARROW_RANDOM_FIRE 独立属性）。全量坍塌后原版 AbstractArrow 不再经 instanceof 判定——
        // 本 Mixin 仅挂钩原版 ProjectileWeaponItem.shoot，技能投射物不从此路径发射，故 {arrow} 作用域
        // 仅覆盖带 arrow 标签的 CoE 实体（原版箭矢无 CoE EntityDef，自然不进入此分支）。
        Set<String> tags = new HashSet<>(Set.of(GateTag.PROJECTILE));
        if (projectile instanceof EntityDefCarrier carrier && carrier.def() != null
                && carrier.def().tagSet().contains(GateTag.ARROW)) {
            tags.add(GateTag.ARROW);
        }
        speed *= (float) DamageHelper.readInScopePublic(shooter, tags, AttributeRegistry.PROJECTILE_SPEED);
        boolean isRandom = StatEngine.gateOf(shooter, "projectile_random_fire", tags);
        inaccuracy = isRandom ? inaccuracy * 1000.0F : inaccuracy;

        args.set(3, speed);
        args.set(4, inaccuracy);
    }

}
