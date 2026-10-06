package com.cimdy.coe.helper;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class OwnerHelper {
    public static void setOwner(LivingEntity living, LivingEntity owner) {
        living.setData(AttachmentRegistry.OWNER_UUID, owner.getUUID());
    }

    public static void setOwner(LivingEntity living, UUID owner) {
        living.setData(AttachmentRegistry.OWNER_UUID, owner);
    }

    public static Entity getOwner(LivingEntity living) {
        return living.level().getEntity(living.getData(AttachmentRegistry.OWNER_UUID));
    }
}
