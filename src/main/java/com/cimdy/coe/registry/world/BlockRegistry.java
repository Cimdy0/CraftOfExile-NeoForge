package com.cimdy.coe.registry.world;

import com.cimdy.coe.event.world.StrongholdEvent;
import com.cimdy.coe.stronghold.transport.ChunkBindingHandler;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.block.custom.StrongholdMarkerBlock;
import com.cimdy.coe.block.custom.ResearchBenchBlock;
import com.cimdy.coe.block.custom.TerminalBlock;
import com.cimdy.coe.block.custom.WellBlock;
import com.cimdy.coe.registry.item.ItemRegistry;
import com.cimdy.coe.WellVillageSpawner;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CraftOfExile.MODID);

    public static final DeferredBlock<TerminalBlock> TERMINAL =
            BLOCKS.registerBlock("terminal", TerminalBlock::new,
                    properties -> properties.strength(50.0F, 1200.0F));
    public static final DeferredItem<BlockItem> TERMINAL_ITEM = ItemRegistry.ITEMS.registerSimpleBlockItem(TERMINAL);

    /**
     * 界桩：把一个区块并入基地的唯一手段。放置即尝试绑定、破坏即解绑
     * （见 {@code StrongholdEvent} / {@code ChunkBindingHandler}），受连通性 / 预算 / 归属三重约束。
     * 强度刻意压低——它是「规划工具」而非「防御工事」。
     */
    public static final DeferredBlock<StrongholdMarkerBlock> STRONGHOLD_MARKER =
            BLOCKS.registerBlock("hold_marker", StrongholdMarkerBlock::new,
                    properties -> properties
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(1.5F, 6.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion());
    public static final DeferredItem<BlockItem> STRONGHOLD_MARKER_ITEM = ItemRegistry.ITEMS.registerSimpleBlockItem(STRONGHOLD_MARKER);

    /** 研究台：研究树的执行入口，右键打开研究树界面并逐秒推进研究计时。 */
    public static final DeferredBlock<ResearchBenchBlock> RESEARCH_BENCH =
            BLOCKS.registerBlock("research_bench", ResearchBenchBlock::new,
                    properties -> properties
                            .mapColor(MapColor.COLOR_BLUE)
                            .strength(3.5F, 12.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion());
    public static final DeferredItem<BlockItem> RESEARCH_BENCH_ITEM = ItemRegistry.ITEMS.registerSimpleBlockItem(RESEARCH_BENCH);

    /**
     * 城镇水井（POE2 实证）：右键补满玩家所有药剂（Flask）与护符（Charm）充能，对齐 POE2 井水机制。
     * 由 {@link WellVillageSpawner} 保证村庄内必定生成，并由数据包（biome_modifier）在非村庄区域随机生成。
     */
    public static final DeferredBlock<WellBlock> WELL =
            BLOCKS.registerBlock("well", WellBlock::new,
                    properties -> properties
                            .mapColor(MapColor.COLOR_BLUE)
                            .strength(3.0F, 12.0F)
                            .sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> WELL_ITEM = ItemRegistry.ITEMS.registerSimpleBlockItem(WELL);
}
