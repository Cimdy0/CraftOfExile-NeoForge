package com.cimdy.coe.block.custom;

import com.cimdy.coe.item.charm.CharmEngine;
import com.cimdy.coe.item.flask.FlaskHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 城镇水井（POE2 实证，严格还原）：右键补满玩家所有药剂（Flask）与护符（Charm）充能，对齐 POE2 井水机制。
 * 充能置满为幂等操作；仅在确有未满物品被补满时才播报，避免空点刷屏。
 */
public class WellBlock extends Block {
    public WellBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;

        boolean flask = FlaskHelper.refillAll(player);
        boolean charm = CharmEngine.refillAll(player);

        if (flask || charm) {
            serverPlayer.sendOverlayMessage(
                    Component.translatable("block.coe.well.refilled").withStyle(ChatFormatting.AQUA));
        }
        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.6F, 1.0F);
        return InteractionResult.SUCCESS;
    }
}
