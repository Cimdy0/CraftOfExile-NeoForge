package com.cimdy.coe.data.codec.stronghold.task.site.impl.process;

import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.SiteTypes;
import com.cimdy.coe.data.codec.stronghold.task.site.basic.SinglePointSite;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class EnderChargeSite extends SinglePointSite {
    private static volatile Codec<EnderChargeSite> CODEC;
    private static volatile StreamCodec<RegistryFriendlyByteBuf, EnderChargeSite> STREAM_CODEC;
    private static volatile Predicate<BlockState> SITE_BLOCK_PREDICATE;

    public EnderChargeSite(
            BlockPos workPos, int maxWorkers, boolean autoAssign, int priority, List<UUID> workers) {
        super(workPos, maxWorkers, autoAssign, priority, workers);
    }

    public static EnderChargeSite create(BlockPos workPos) {
        return new EnderChargeSite(workPos, 1, true, 5, List.of());
    }

    @Override public Component getName() { return Component.translatable("hold.site.coe.ender_charge"); }
    @Override public Component getDescription() { return Component.translatable("hold.site.coe.ender_charge.description"); }
    @Override public boolean canSetAssign() {
        return true;
    }
    @Override public Predicate<BlockState> getBlockPredicate() {
        return SITE_BLOCK_PREDICATE;
    }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Site> getStreamCodec() {
        return STREAM_CODEC;
    }
    @Override public Type<?> getType() {
        return SiteTypes.ENDER_CHARGE_SITE;
    }
    public static Predicate<BlockState> predicate() {
        if (SITE_BLOCK_PREDICATE == null) {
            SITE_BLOCK_PREDICATE = state -> state.is(Blocks.DRAGON_HEAD);
        }
        return SITE_BLOCK_PREDICATE;
    }

    public static Codec<EnderChargeSite> codec() {
        if (CODEC == null) {
            synchronized (EnderChargeSite.class) {
                if (CODEC == null) {
                    CODEC = RecordCodecBuilder.create(instance -> instance.group(
                            sitePosCodec(), maxWorkersCodec(), autoAssignCodec(), priorityCodec(), workersCodec()
                    ).apply(instance, EnderChargeSite::new));
                }
            }
        }
        return CODEC;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, EnderChargeSite> streamCodec() {
        if (STREAM_CODEC == null) {
            synchronized (EnderChargeSite.class) {
                if (STREAM_CODEC == null) {
                    STREAM_CODEC = StreamCodec.composite(
                            sitePosStreamCodec(), EnderChargeSite::getSitePos,
                            maxWorkersStreamCodec(), EnderChargeSite::getMaxWorkers,
                            autoAssignStreamCodec(), EnderChargeSite::isAutoAssign,
                            priorityStreamCodec(), EnderChargeSite::getPriority,
                            workersStreamCodec(), EnderChargeSite::getWorkers,
                            EnderChargeSite::new
                    );
                }
            }
        }
        return STREAM_CODEC;
    }
}