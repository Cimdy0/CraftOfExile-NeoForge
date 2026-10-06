package com.cimdy.coe.network.skill;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SkillAssemblyActionPacket(int action, int slot, int socket, int fromSlot, int fromSocket) implements CustomPacketPayload {
        public static final int EQUIP_SKILL = 0;
        public static final int UNEQUIP_SKILL = 1;
        public static final int EQUIP_SUPPORT = 2;
        public static final int REMOVE_SUPPORT = 3;
        public static final int TOGGLE_AURA = 4;
        /** 主技能石槽位间交换：fromSlot = 源槽，slot = 目标槽（辅助随技能走）。 */
        public static final int MOVE_SKILL = 5;
        /** 辅助宝石槽位间交换：fromSlot/fromSocket = 源，slot/socket = 目标，可跨技能。 */
        public static final int MOVE_SUPPORT = 6;

        public static final Type<SkillAssemblyActionPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "skill_assembly_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SkillAssemblyActionPacket> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT, SkillAssemblyActionPacket::action,
                        ByteBufCodecs.INT, SkillAssemblyActionPacket::slot,
                        ByteBufCodecs.INT, SkillAssemblyActionPacket::socket,
                        ByteBufCodecs.INT, SkillAssemblyActionPacket::fromSlot,
                        ByteBufCodecs.INT, SkillAssemblyActionPacket::fromSocket,
                        SkillAssemblyActionPacket::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
                return TYPE;
        }
}
