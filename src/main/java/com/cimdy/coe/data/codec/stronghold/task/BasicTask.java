package com.cimdy.coe.data.codec.stronghold.task;

import com.cimdy.coe.data.codec.stronghold.ItemCount;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class BasicTask implements Task {
    protected Optional<Task> next;

    public BasicTask(Optional<Task> next) {
        this.next = next;
    }

    public Optional<Task> getOptionNext() {
        return next;
    }

    @Override
    public @Nullable Task getNext() {
        return next.orElse(null);
    }

    @Override
    public void setNext(@Nullable Task next) {
        this.next = Optional.ofNullable(next);
    }

    protected static <T extends BasicTask> RecordCodecBuilder<T, Optional<Task>> nextCodec() {
        return Codec.lazyInitialized(() -> Task.CODEC).optionalFieldOf("next").forGetter(BasicTask::getOptionNext);
    }

    protected static StreamCodec<ByteBuf, Optional<Task>> nextStreamCodec() {
        return ByteBufCodecs.optional(ByteBufCodecs.fromCodec(Codec.lazyInitialized(() -> Task.CODEC)));
    }

    public abstract static class ItemTask extends BasicTask {

        protected ItemCount item;

        public ItemTask(Optional<Task> next, ItemCount item) {
            super(next);
            this.item = item;
        }

        public ItemCount getItem() {
            return item;
        }

        protected static <T extends ItemTask> RecordCodecBuilder<T, ItemCount> itemCodec() {
            return ItemCount.CODEC.fieldOf("item").forGetter(ItemTask::getItem);
        }

        protected static StreamCodec<RegistryFriendlyByteBuf, ItemCount> itemStreamCodec() {
            return ItemCount.STREAM_CODEC;
        }
    }

    public abstract static class Move extends ItemTask {
        protected BlockPos sourcePos;
        protected BlockPos targetPos;

        public Move(Optional<Task> next, ItemCount item, BlockPos sourcePos, BlockPos targetPos) {
            super(next, item);
            this.sourcePos = sourcePos;
            this.targetPos = targetPos;
        }

        public BlockPos getSourcePos() { return sourcePos; }
        public BlockPos getTargetPos() { return targetPos; }

        protected static <T extends Move> RecordCodecBuilder<T, BlockPos> sourceCodec() {
            return BlockPos.CODEC.fieldOf("source").forGetter(Move::getSourcePos);
        }

        protected static <T extends Move> RecordCodecBuilder<T, BlockPos> targetCodec() {
            return BlockPos.CODEC.fieldOf("target").forGetter(Move::getTargetPos);
        }

        protected static StreamCodec<ByteBuf, BlockPos> sourceStreamCodec() {
            return BlockPos.STREAM_CODEC;
        }

        protected static StreamCodec<ByteBuf, BlockPos> targetStreamCodec() {
            return BlockPos.STREAM_CODEC;
        }
    }

    public static abstract class Pos extends BasicTask {
        private final BlockPos workPos;

        public Pos(Optional<Task> next, BlockPos workPos) {
            super(next);
            this.workPos = workPos;
        }

        public BlockPos getWorkPos() {
            return workPos;
        }

        @Override public Optional<?> getGroupingKey() { return Optional.of(workPos); }
        @Override public BlockPos getTargetPositionForEntity(LivingEntity entity) { return workPos; }
    }
}