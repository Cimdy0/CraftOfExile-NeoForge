package com.cimdy.coe.item.accessory;

import com.cimdy.coe.event.world.EntityOperationEvent;
import com.cimdy.coe.registry.item.ItemRegistry;

import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.gen.equipment.EquipmentGen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ShieldItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * 轻盾（Buckler）：POE2 中对应「buckler」类盾牌，专属「招架（Parry）」动作。
 * <p>
 * 继承 {@link ShieldItem} 复用原版盾牌渲染（第一/第三人称持盾姿势）。
 * 招架行为由数据驱动判定：其 {@code EquipmentState.types} 含 {@code buckler}，
 * 在 {@code EntityOperationEvent.onUseItemStart} / {@code DamagePipeline.stageParry} 中据该标签触发，
 * 与 isShield / isBlock 同构，不写死在类里。
 */
public class Buckler extends ShieldItem {
    public Buckler(Properties p_43089_) {
        super(p_43089_);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    public Component getName(ItemStack p_371901_) {
        DyeColor dyecolor = p_371901_.get(DataComponents.BASE_COLOR);
        Object var3;
        if (dyecolor != null) {
            String var10000 = this.descriptionId;
            var3 = Component.translatable(var10000 + "." + dyecolor.getName());
        } else {
            var3 = super.getName(p_371901_);
        }

        return (Component) var3;
    }

    /** 轻盾挂含 {@code buckler} 类型的 {@link EquipmentState}，供数据驱动招架判定使用。可招架性由 ItemRegistry 挂 CAN_PARRY 组件声明。 */
    public static EquipmentState bucklerState() {
        return new EquipmentState(100, Rarity.COMMON,
                List.of(EquipmentGen.id(EquipmentGen.BUCKLER)), Map.of(),
                Optional.empty(), List.of(), 0, List.of());
    }

    /** 读取物品是否可招架（权威来源为 ItemStack 上的 CAN_PARRY 组件，回落到 EquipmentState.buckler 标签）。 */
    public static boolean canParry(ItemStack stack) {
        Boolean v = stack.get(DataComponentRegistry.CAN_PARRY.get());
        if (v != null) return v;
        EquipmentState state =ModifiableStateOps.equipment(stack);
        return state != null && state.isBuckler();
    }
}
