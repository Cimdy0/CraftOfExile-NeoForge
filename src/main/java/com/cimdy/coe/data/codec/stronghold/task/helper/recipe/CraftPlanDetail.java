package com.cimdy.coe.data.codec.stronghold.task.helper.recipe;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

public record CraftPlanDetail(ItemResource result, int amount, List<CraftStepDetail> steps) {
        public static final Codec<CraftPlanDetail> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemResource.CODEC.fieldOf("result").forGetter(CraftPlanDetail::result),
                Codec.INT.fieldOf("amount").forGetter(CraftPlanDetail::amount),
                CraftStepDetail.CODEC.listOf().fieldOf("steps").forGetter(CraftPlanDetail::steps)
        ).apply(instance, CraftPlanDetail::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CraftPlanDetail> STREAM_CODEC = StreamCodec.composite(
                ItemResource.STREAM_CODEC, CraftPlanDetail::result,
                ByteBufCodecs.INT, CraftPlanDetail::amount,
                CraftStepDetail.STREAM_CODEC.apply(ByteBufCodecs.list()), CraftPlanDetail::steps,
                CraftPlanDetail::new
        );

        public Task createTaskChainForPlan(ServerLevel level, GlobalPos terminalPos) {
            Task head = null, tail = null;
            for (CraftStepDetail step : this.steps()) {
                Task stepChain = step.createTaskChainForStep(terminalPos);
                if (head == null) head = stepChain;
                else {
                    Task lastOfStep = stepChain;
                    while (lastOfStep.getNext() != null) lastOfStep = lastOfStep.getNext();
                    tail.setNext(stepChain);
                }
                tail = stepChain;
                while (tail.getNext() != null) tail = tail.getNext();
            }
            return head;
        }
    }
