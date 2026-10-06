package com.cimdy.coe.data.codec.item.mod.identity;

import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import net.minecraft.world.entity.EquipmentSlotGroup;

public final class SlotResolver {

    private SlotResolver() {}

    public static EquipmentSlotGroup getSlot(EquipmentState state) {
        if (ItemTypeChecker.isMainHand(state)) {
            return EquipmentSlotGroup.MAINHAND;
        } else if (ItemTypeChecker.isOffHand(state)) {
            return EquipmentSlotGroup.OFFHAND;
        } else if (ItemTypeChecker.isHead(state)) {
            return EquipmentSlotGroup.HEAD;
        } else if (ItemTypeChecker.isChest(state)) {
            return EquipmentSlotGroup.CHEST;
        } else if (ItemTypeChecker.isLegs(state)) {
            return EquipmentSlotGroup.LEGS;
        } else if (ItemTypeChecker.isFeet(state)) {
            return EquipmentSlotGroup.FEET;
        }
        return EquipmentSlotGroup.SADDLE;
    }
}
