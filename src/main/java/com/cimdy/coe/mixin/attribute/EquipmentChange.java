package com.cimdy.coe.mixin.attribute;

import com.cimdy.coe.data.helper.affix.ModHelper;
import com.cimdy.coe.data.helper.engine.EquipRules;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class EquipmentChange {
    //每Tick获取装备 穿戴条件不满足时 返回为空
    @WrapOperation(method = "collectEquipmentChanges", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack getUseEquipment(LivingEntity living, EquipmentSlot equipmentSlot, Operation<ItemStack> original) {
        ItemStack stack = original.call(living, equipmentSlot);
        if (stack.isEmpty()) {
            return stack;
        }
        // 通用「装备 / 持握规则」引擎（数据驱动 coe:equip_rule）：规则表为空 / 无规则命中 → 恒放行，
        // 未配置时与既有行为完全一致。命中且未被 stat 豁免 → 按「未持握」处理（视同该槽为空）。
        // 生效时机：实体每次收集装备变更时逐槽求值，故换手后下一 tick 即生效/失效。
        if (EquipRules.blocked(living, stack, equipmentSlot)) {
            return ItemStack.EMPTY;
        }
        return ModHelper.currentEquipment(living, stack, null) ? stack : ItemStack.EMPTY;
    }
}
