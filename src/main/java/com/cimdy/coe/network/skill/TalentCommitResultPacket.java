package com.cimdy.coe.network.skill;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * 天赋提交结果回执（服务端 → 客户端）：{@link TalentCommitPacket} 的服务端裁决结果。
 *
 * <p>失败时客户端应据 {@code reason} 提示玩家并保持本地预览不变（服务端未改动任何存档与金币），
 * 成功时以 {@code points} / {@code goldCost} 刷新顶部显示。
 *
 * <p>回执只发提交者本人（{@code PacketDistributor.sendToPlayer}），不广播。
 */
public record TalentCommitResultPacket(
        Identifier treeId,
        int mode,
        boolean success,
        int reason,
        long goldCost,
        int points
) implements CustomPacketPayload {
        /** 成功。 */
        public static final int REASON_OK = 0;
        /** 金币不足：未扣费、未清空。 */
        public static final int REASON_NOT_ENOUGH_GOLD = 1;
        /** 提交点数超过该树总点数上限。 */
        public static final int REASON_TOO_MANY_POINTS = 2;
        /** 提交了树模型中不存在的节点。 */
        public static final int REASON_UNKNOWN_NODE = 3;
        /** 存在无法连通（不与任何已点亮节点相邻）的提交节点。 */
        public static final int REASON_UNREACHABLE_NODE = 4;
        /** 未知提交模式（协议不匹配）。 */
        public static final int REASON_BAD_MODE = 5;

        public static final Type<TalentCommitResultPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "talent_commit_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf, TalentCommitResultPacket> STREAM_CODEC =
                StreamCodec.composite(
                        Identifier.STREAM_CODEC, TalentCommitResultPacket::treeId,
                        ByteBufCodecs.INT, TalentCommitResultPacket::mode,
                        ByteBufCodecs.BOOL, TalentCommitResultPacket::success,
                        ByteBufCodecs.INT, TalentCommitResultPacket::reason,
                        ByteBufCodecs.VAR_LONG, TalentCommitResultPacket::goldCost,
                        ByteBufCodecs.INT, TalentCommitResultPacket::points,
                        TalentCommitResultPacket::new
                );

        /** 便捷构造：成功回执。 */
        public static TalentCommitResultPacket ok(Identifier treeId, int mode, long goldCost, int points) {
                return new TalentCommitResultPacket(treeId, mode, true, REASON_OK, goldCost, points);
        }

        /** 便捷构造：失败回执（{@code goldCost} 传本次应扣/已算出的花费，便于 UI 提示差额）。 */
        public static TalentCommitResultPacket fail(Identifier treeId, int mode, int reason, long goldCost, int points) {
                return new TalentCommitResultPacket(treeId, mode, false, reason, goldCost, points);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
                return TYPE;
        }
    }
