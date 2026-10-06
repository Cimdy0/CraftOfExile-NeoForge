package com.cimdy.coe.block.custom;

import com.cimdy.coe.network.sync.ProgressionSyncSender;
import com.cimdy.coe.registry.world.BlockEntityRegistry;
import com.cimdy.coe.block.entity.ResearchBenchBlockEntity;
import com.cimdy.coe.data.codec.progression.ProgressionData;
import com.cimdy.coe.data.codec.progression.research.ResearchBench;
import com.cimdy.coe.data.codec.progression.research.ResearchNodeDef;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 研究台：研究树的<b>唯一执行入口</b>。
 *
 * <p>研究点是资源门槛，研究台的 tick 是时间门槛。玩家右键打开研究树界面挑一个节点，
 * 服务端扣点并把节点写入本方块的 {@link ResearchBench} 附件，
 * 由 {@link ResearchBenchBlockEntity} 逐秒推进直到结算。
 *
 * <p>正在研究时右键则播报当前进度，不打断——避免误触取消掉长时间投入。
 */
public class ResearchBenchBlock extends BaseEntityBlock {
    public static final MapCodec<ResearchBenchBlock> CODEC = simpleCodec(ResearchBenchBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE = makeShape();

    public ResearchBenchBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof ResearchBenchBlockEntity bench)) {
            return InteractionResult.SUCCESS;
        }

        ResearchBench current = bench.state();
        if (current.isRunning()) {
            Identifier id = current.research().orElseThrow();
            ResearchNodeDef def = ProgressionData.RESEARCH_NODES.get(id);
            int duration = def == null ? 0 : def.durationTicks();
            int percent = Math.round(current.progress(duration) * 100F);
            String name = def == null || def.name().isEmpty() ? id.toString() : def.name();
            serverPlayer.sendOverlayMessage(
                    Component.translatable("research.coe.in_progress", name, percent)
                            .withStyle(ChatFormatting.AQUA));
            return InteractionResult.SUCCESS;
        }

        // 空闲：打开研究树界面，由客户端选节点后回传 StartResearch 包
        ProgressionSyncSender.openResearchScreen(serverPlayer, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == BlockEntityRegistry.RESEARCH_BENCH_BLOCK_ENTITY.get()
                ? createTickerHelper(type, BlockEntityRegistry.RESEARCH_BENCH_BLOCK_ENTITY.get(),
                ResearchBenchBlockEntity::tick)
                : null;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResearchBenchBlockEntity(pos, state);
    }

    /** 桌面 + 四条腿 + 桌上一叠图纸。 */
    public static VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0.75, 0, 1, 0.9375, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.0625, 0.1875, 0.75, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0, 0.0625, 0.9375, 0.75, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.8125, 0.1875, 0.75, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0, 0.8125, 0.9375, 0.75, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.9375, 0.25, 0.75, 1.0, 0.75), BooleanOp.OR);
        return shape;
    }
}
