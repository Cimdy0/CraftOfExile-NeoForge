package com.cimdy.coe.network.skill;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.skill.gem.KeyBind;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SkillSetupPacket(KeyBind.SkillSave skillSave) implements CustomPacketPayload {
        public static final Type<SkillSetupPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "skill_setup"));
        public static final Codec<SkillSetupPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                KeyBind.SkillSave.CODEC.fieldOf("skill_save").forGetter(SkillSetupPacket::skillSave)
        ).apply(instance, SkillSetupPacket::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, SkillSetupPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.fromCodec(KeyBind.SkillSave.CODEC), SkillSetupPacket::skillSave,
                SkillSetupPacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
