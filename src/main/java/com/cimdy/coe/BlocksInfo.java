package com.cimdy.coe;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.block.entity.TerminalBlockEntity;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum BlocksInfo implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final Identifier BLOCKS_INFO = Identifier.fromNamespaceAndPath(
            CraftOfExile.MODID, "blocks_info");

    @Override
    public Identifier getUid() {
        return BLOCKS_INFO;
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {}

    public static class Client implements IBlockComponentProvider {
        public static final BlocksInfo.Client INSTANCE = new BlocksInfo.Client();

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig iPluginConfig) {
            if (accessor.getBlockEntity() instanceof TerminalBlockEntity terminal) {
                tooltip.add(Component.translatable("hold.coe.name")
                        .append(": " + StrongholdHelper.getHoldName(accessor.getLevel(), terminal.getBlockPos())));
                tooltip.add(Component.translatable("hold.coe.team")
                        .append(": " + StrongholdHelper.getHoldGroupName(accessor.getLevel(), terminal.getBlockPos())));
                tooltip.add(Component.translatable("hold.coe.level")
                        .append(": " + StrongholdHelper.getHoldLevel(accessor.getLevel(), terminal.getBlockPos())));
                tooltip.add(Component.translatable("hold.coe.energy")
                        .append(": " + StrongholdHelper.getHoldEnergy(accessor.getLevel(), terminal.getBlockPos())));
            }
        }

        public Client() {
        }

        @Override
        public Identifier getUid() {
            return BLOCKS_INFO;
        }
    }
}