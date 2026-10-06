package com.cimdy.coe.registry.world;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.block.entity.ResearchBenchBlockEntity;
import com.cimdy.coe.block.entity.TerminalBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CraftOfExile.MODID);

    public static final Supplier<BlockEntityType<TerminalBlockEntity>> TERMINAL_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("terminal",
                    () -> new BlockEntityType<>(TerminalBlockEntity::new, BlockRegistry.TERMINAL.get()));

    /** 研究台：推进研究计时的方块实体（研究树的时间门槛载体）。 */
    public static final Supplier<BlockEntityType<ResearchBenchBlockEntity>> RESEARCH_BENCH_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("research_bench",
                    () -> new BlockEntityType<>(ResearchBenchBlockEntity::new, BlockRegistry.RESEARCH_BENCH.get()));
}
