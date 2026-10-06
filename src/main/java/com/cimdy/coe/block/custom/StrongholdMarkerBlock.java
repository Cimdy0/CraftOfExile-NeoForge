package com.cimdy.coe.block.custom;

import com.cimdy.coe.data.codec.progression.PlayerProgress;
import com.cimdy.coe.data.codec.progression.ProgressionService;
import com.cimdy.coe.event.world.StrongholdEvent;
import com.cimdy.coe.stronghold.transport.ChunkBindingHandler;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 界桩：玩家把一个区块并入基地的<b>唯一手段</b>。
 *
 * <p>基地范围不再是「以终端为中心的半径」，而是由玩家一根一根界桩<b>建设</b>出来的
 * 不规则区块集合。每根界桩代表一个区块，受连通性、区块预算、归属权三重约束
 * （见 {@link ChunkBindingHandler}），因此「基地变大」本身就是一条需要长期投入的进度线。
 *
 * <p>放置 / 破坏的绑定与解绑在 {@code StrongholdEvent} 中挂钩（实际由 {@code ChunkBindingHandler} 执行），本类只负责
 * 右键查询当前区块归属与预算余量，让玩家随时知道「还能扩几块」。
 */
public class StrongholdMarkerBlock extends Block {
    public static final MapCodec<StrongholdMarkerBlock> CODEC = simpleCodec(StrongholdMarkerBlock::new);

    private static final VoxelShape SHAPE = makeShape();

    public StrongholdMarkerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel) {
            ChunkPos chunk = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
            PlayerProgress progress = PlayerProgress.get(player);
            boolean bound = ChunkBindingHandler.isBound(serverLevel, pos);

            player.sendSystemMessage(Component.translatable(
                            bound ? "hold.coe.marker.bound" : "hold.coe.marker.unbound",
                            chunk.x(), chunk.z())
                    .withStyle(bound ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            player.sendSystemMessage(Component.translatable("hold.coe.marker.budget",
                            progress.chunksUsed(), ProgressionService.chunkBudget(progress), progress.level())
                    .withStyle(ChatFormatting.AQUA));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /** 细柱 + 顶部旗标，视觉上像一根边界桩。 */
    public static VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.375, 0, 0.375, 0.625, 1.0, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.75, 0.25, 0.75, 1.0, 0.75), BooleanOp.OR);
        return shape;
    }
}
