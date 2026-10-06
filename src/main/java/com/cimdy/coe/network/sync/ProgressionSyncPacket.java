package com.cimdy.coe.network.sync;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.progression.JobCharterDef;
import com.cimdy.coe.data.codec.progression.quest.QuestDef;
import com.cimdy.coe.data.codec.progression.research.ResearchNodeDef;
import com.cimdy.coe.data.codec.progression.TechNodeDef;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record ProgressionSyncPacket(List<QuestDef> quests, List<TechNodeDef> tech,
                                  List<ResearchNodeDef> research, List<JobCharterDef> jobCharters) implements CustomPacketPayload {
        public static final Type<ProgressionSyncPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "progression_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ProgressionSyncPacket> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.fromCodec(QuestDef.NETWORK_CODEC.listOf()), ProgressionSyncPacket::quests,
                        ByteBufCodecs.fromCodec(TechNodeDef.NETWORK_CODEC.listOf()), ProgressionSyncPacket::tech,
                        ByteBufCodecs.fromCodec(ResearchNodeDef.NETWORK_CODEC.listOf()), ProgressionSyncPacket::research,
                        ByteBufCodecs.fromCodec(JobCharterDef.NETWORK_CODEC.listOf()), ProgressionSyncPacket::jobCharters,
                        ProgressionSyncPacket::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
