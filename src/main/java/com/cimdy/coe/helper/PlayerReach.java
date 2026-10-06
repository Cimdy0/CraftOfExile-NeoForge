package com.cimdy.coe.helper;

import com.cimdy.coe.network.stronghold.BindSite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/**
 * 统一的「玩家是否够得着某坐标」判定：区块已加载且距离在交互范围内（5 格，平方距离 25）。
 *
 * <p>多处（BindSite 网络包、研究台启动防作弊、界桩绑定）都需要这条校验，集中在此避免重复与口径不一。
 */
public final class PlayerReach {

    private PlayerReach() {
    }

    /** 玩家是否能交互到该坐标（已加载 + 5 格内）。 */
    public static boolean canReach(Player player, BlockPos pos) {
        return player.level().isLoaded(pos)
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 25.0;
    }
}
