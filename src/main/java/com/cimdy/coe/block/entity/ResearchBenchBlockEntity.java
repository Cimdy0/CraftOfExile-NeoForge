package com.cimdy.coe.block.entity;

import com.cimdy.coe.registry.world.BlockEntityRegistry;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.progression.ProgressionData;
import com.cimdy.coe.data.codec.progression.ProgressionService;
import com.cimdy.coe.data.codec.progression.research.ResearchBench;
import com.cimdy.coe.data.codec.progression.research.ResearchNodeDef;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * 研究台方块实体：推进 {@link ResearchBench} 的计时，到点结算研究成果。
 *
 * <p>这是研究树与「基建要花时间」之间的桥：研究点是资源门槛，
 * 研究台的 tick 则是<b>时间门槛</b>——两者叠加，才让研究树成为需要规划的长期投入，
 * 而不是攒够点数一键刷完。
 */
public class ResearchBenchBlockEntity extends BlockEntity {

    /** 每 20 tick（1 秒）推进一次，计时按秒粒度累加，避免每 tick 写附件。 */
    private static final int TICK_INTERVAL = 20;
    private int tickCounter = 0;

    public ResearchBenchBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.RESEARCH_BENCH_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ResearchBenchBlockEntity bench) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        bench.tickCounter++;
        if (bench.tickCounter < TICK_INTERVAL) return;
        bench.tickCounter = 0;
        bench.advance(serverLevel);
    }

    private void advance(ServerLevel level) {
        ResearchBench bench = this.getData(AttachmentRegistry.RESEARCH_BENCH);
        if (!bench.isRunning()) return;

        Identifier id = bench.research().orElseThrow();
        ResearchNodeDef def = ProgressionData.RESEARCH_NODES.get(id);
        if (def == null) {
            // 数据包被移除 / 重载后节点消失：直接清空，避免研究台永久卡死
            this.setData(AttachmentRegistry.RESEARCH_BENCH, bench.cleared());
            setChanged();
            return;
        }

        ResearchBench next = bench.withTicks(bench.ticks() + TICK_INTERVAL);
        if (next.ticks() >= def.durationTicks()) {
            UUID owner = bench.owner().orElseThrow();
            Player player = level.getPlayerByUUID(owner);
            if (player != null) {
                ProgressionService.completeResearch(player, id);
                this.setData(AttachmentRegistry.RESEARCH_BENCH, bench.cleared());
            } else {
                // 发起者离线：保持满进度挂起，等其上线再结算（不浪费已投入的研究点）
                this.setData(AttachmentRegistry.RESEARCH_BENCH, next.withTicks(def.durationTicks()));
            }
        } else {
            this.setData(AttachmentRegistry.RESEARCH_BENCH, next);
        }
        setChanged();
    }

    /** 在此研究台启动一项研究（研究点已由 {@link ProgressionService#beginResearch} 扣除）。 */
    public boolean start(Player player, Identifier research) {
        ResearchBench bench = this.getData(AttachmentRegistry.RESEARCH_BENCH);
        if (bench.isRunning()) return false;
        if (!ProgressionService.beginResearch(player, research)) return false;
        this.setData(AttachmentRegistry.RESEARCH_BENCH, ResearchBench.of(player.getUUID(), research));
        setChanged();
        return true;
    }

    public ResearchBench state() {
        return this.getData(AttachmentRegistry.RESEARCH_BENCH);
    }
}
