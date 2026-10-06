package com.cimdy.coe.data.codec.stronghold.task.site.basic;

import com.cimdy.coe.data.codec.stronghold.task.site.BasicSite;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class SinglePointSite extends BasicSite {
    protected List<UUID> workers;

    public SinglePointSite(
            BlockPos workPos, int maxWorkers, boolean autoAssign, int priority,
            List<UUID> workers) {
        super(workPos, maxWorkers, autoAssign, priority);
        this.workers = workers;
    }

    @Override public List<UUID> getWorkers() {
        return workers;
    }
    @Override public void addWorker(UUID uuid) {
        List<UUID> newWorkers = new ArrayList<>(workers);
        newWorkers.add(uuid);
        workers = newWorkers;
    }
    @Override public void removeWorker(UUID uuid) {
        List<UUID> newWorkers = new ArrayList<>(workers);
        newWorkers.remove(uuid);
        workers = newWorkers;
    }

    protected static <T extends SinglePointSite> RecordCodecBuilder<T, List<UUID>> workersCodec() {
        return UUIDUtil.CODEC.listOf().fieldOf("workers").forGetter(T::getWorkers);
    }

    protected static <T extends SinglePointSite> StreamCodec<ByteBuf, List<UUID>> workersStreamCodec() {
        return UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list());
    }
}
