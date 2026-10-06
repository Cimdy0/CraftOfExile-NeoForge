package com.cimdy.coe.network.entity;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * 客户端 → 服务端：翻转某实体的「战斗开关」（帕鲁式「专注工作 ↔ 也参与战斗」）。
 *
 * <p>与 {@link ToggleAllyPacket} 同源同权限模型：{@code pos} 为发出请求的终端方块坐标，服务端据此做可达性校验，
 * 并决定优先作用于「该据点内已部署的实体」还是「玩家存档里的未放出实体」。翻转后服务端就地重注入 AI，
 * 使 {@code FactionDefendGoal} 随 {@code COMBATANT} 即时增删。
 */
public record ToggleCombatantPacket(UUID uuid, BlockPos pos) implements CustomPacketPayload {
    public static final Type<ToggleCombatantPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "toggle_combatant"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCombatantPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ToggleCombatantPacket::uuid,
            BlockPos.STREAM_CODEC, ToggleCombatantPacket::pos,
            ToggleCombatantPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
