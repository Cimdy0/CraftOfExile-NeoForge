package com.cimdy.coe.network.skill;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * 单次天赋操作（客户端 ↔ 服务端）：加点 / 取消。
 *
 * <p><b>整树重置不再走本包</b>：POE2 语义下重置须按角色等级单价付金币，改由
 * {@link TalentCommitPacket} 的 {@code RESET} 模式显式提交（先重置预览、再保存）。
 */
public record TalentActionPacket(Identifier treeId, String nodeId, int action, int choice, int weaponSet) implements CustomPacketPayload {
        public static final int ALLOCATE = 0;
        public static final int DEALLOCATE = 1;

        /** 便捷构造：用于取消等无需选项与武器组的请求（默认 player_talent 树）。 */
        public TalentActionPacket(String nodeId, int action) {
            this(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "player_talent"), nodeId, action, -1, 0);
        }

        /** 便捷构造：指定树、无需选项与武器组（加点 / 取消）。 */
        public TalentActionPacket(Identifier treeId, String nodeId, int action) {
            this(treeId, nodeId, action, -1, 0);
        }

        /** 加点（含多选一选项与武器组绑定）。 */
        public TalentActionPacket(Identifier treeId, String nodeId, int action, int choice, int weaponSet) {
            this.treeId = treeId;
            this.nodeId = nodeId;
            this.action = action;
            this.choice = choice;
            this.weaponSet = weaponSet;
        }

        public static final Type<TalentActionPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "talent_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, TalentActionPacket> STREAM_CODEC =
                StreamCodec.composite(
                        Identifier.STREAM_CODEC, TalentActionPacket::treeId,
                        ByteBufCodecs.STRING_UTF8, TalentActionPacket::nodeId,
                        ByteBufCodecs.INT, TalentActionPacket::action,
                        ByteBufCodecs.INT, TalentActionPacket::choice,
                        ByteBufCodecs.INT, TalentActionPacket::weaponSet,
                        TalentActionPacket::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
