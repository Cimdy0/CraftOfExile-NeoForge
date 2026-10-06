package com.cimdy.coe.stronghold.area;

import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.entity.StrongholdEntity;
import com.cimdy.coe.helper.LookAtHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;

/**
 * 区域扫描与实体状态服务：负责区块/容器扫描、坐标区域判定、玩家视线容器检测、工人 SAN/饥饿读写。
 * 从原 God Class {@link com.cimdy.coe.stronghold.energy.StrongholdHelper} 拆分而来，逻辑完全一致。
 */
public final class AreaScanner {
    private AreaScanner() {
    }

    public static boolean isLookAtContainer(Player player) {
        BlockEntity be = LookAtHelper.getBlockEntityLookAt(player,
                player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE));
        if (be == null || be.getLevel() == null) return false;
        return be.getLevel().getCapability(Capabilities.Item.BLOCK, be.getBlockPos(), null) != null;
    }

    // ==================== SAN / 饥饿 ====================

    public static double getSan(Entity entity) {
        return entity.getData(AttachmentRegistry.ENTITY_DATA).san();
    }

    public static void modifySan(Entity entity, double delta) {
        StrongholdEntity data = entity.getData(AttachmentRegistry.ENTITY_DATA);
        double newSan = Math.clamp(data.san() + delta, 0, 100);
        entity.setData(AttachmentRegistry.ENTITY_DATA, data.withSan(newSan));
    }

    public static double getHunger(Entity entity) {
        return entity.getData(AttachmentRegistry.ENTITY_DATA).hunger();
    }

    public static void modifyHunger(Entity entity, double delta) {
        StrongholdEntity data = entity.getData(AttachmentRegistry.ENTITY_DATA);
        double newHunger = Math.clamp(data.hunger() + delta, 0, 40);
        entity.setData(AttachmentRegistry.ENTITY_DATA, data.withHunger(newHunger));
    }
}
