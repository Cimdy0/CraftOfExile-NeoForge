package com.cimdy.coe.mixin.attribute;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.attribute.AttributeLimiters;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingAttributeValue extends Entity {
    public LivingAttributeValue(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "getAttributeValue", at = @At(value = "RETURN"))
    private double modifyReturnValue(double original, @Local(argsOnly = true) Holder<Attribute> attribute) {
        if (this.hasData(AttachmentRegistry.ATTRIBUTE_LIMITERS)) {
            AttributeLimiters limiters = this.getData(AttachmentRegistry.ATTRIBUTE_LIMITERS);
            if (limiters != AttributeLimiters.EMPTY) {
                original = limiters.getCache().clampValue(attribute, original);
            }
        }
        // 真实瞬间 stat：按当前事件作用域即时计算天赋加成，不写全局修饰符（消除并发串联 bug）。
        // mixin 作用于 LivingEntity，但本类静态类型为 Entity，需经 Object 下转型。
        LivingEntity living = (LivingEntity) (Object) this;
        original = StatEngine.fold(living, attribute, original);
        return original;
    }
}
