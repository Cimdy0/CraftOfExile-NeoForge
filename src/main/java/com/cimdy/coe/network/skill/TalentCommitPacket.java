package com.cimdy.coe.network.skill;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 天赋提交保存（客户端 → 服务端）：把客户端本地预览的分配结果提交落库，或请求金币重置。
 *
 * <p>与 {@link TalentActionPacket}（单次加点 / 取消，即时落库）互补：本包是<b>显式「保存」</b>通道，
 * 一次提交即代表玩家确认。模式互斥——{@link #ALLOCATE} 与 {@link #RESET} 不可同时提交，
 * 由 {@code mode} 单值天然保证。
 *
 * <p><b>服务端权威</b>：客户端提交的只是一份「期望的分配集合」，节点存在性、连通性、互斥、
 * 点数上限全部在服务端按树模型重算；重置花费由服务端按角色等级查 {@code TalentRespecCost} 得出，
 * 客户端传入的任何数值都只作为期望值校验，绝不作为扣费依据。
 */
public record TalentCommitPacket(Identifier treeId, int mode, List<Entry> entries) implements CustomPacketPayload {
        /** 提交当前分配集合（加点 / 取消 / 换武器组的最终结果）。 */
        public static final int ALLOCATE = 0;
        /** 清空整棵树，花费按已分配点数 × 等级单价从金库中扣除。 */
        public static final int RESET = 1;

        /** 一条待提交分配：节点 id + 多选一选项下标（无选项为 -1）+ 绑定的武器组。 */
        public record Entry(String nodeId, int choice, int weaponSet) {
                public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, Entry::nodeId,
                        ByteBufCodecs.INT, Entry::choice,
                        ByteBufCodecs.INT, Entry::weaponSet,
                        Entry::new
                );

                /** 便捷构造：无选项、绑定全局武器组。 */
                public Entry(String nodeId) {
                        this(nodeId, -1, 0);
                }
        }

        /** 便捷构造：ALLOCATE 模式提交给定分配集合。 */
        public static TalentCommitPacket allocate(Identifier treeId, List<Entry> entries) {
                return new TalentCommitPacket(treeId, ALLOCATE, entries);
        }

        /** 便捷构造：RESET 模式（无需提交集合，服务端以自身存档为准清零）。 */
        public static TalentCommitPacket reset(Identifier treeId) {
                return new TalentCommitPacket(treeId, RESET, List.of());
        }

        public static final Type<TalentCommitPacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "talent_commit"));
        public static final StreamCodec<RegistryFriendlyByteBuf, TalentCommitPacket> STREAM_CODEC =
                StreamCodec.composite(
                        Identifier.STREAM_CODEC, TalentCommitPacket::treeId,
                        ByteBufCodecs.INT, TalentCommitPacket::mode,
                        Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), TalentCommitPacket::entries,
                        TalentCommitPacket::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
                return TYPE;
        }
    }
