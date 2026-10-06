package com.cimdy.coe.item.charm;

import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import net.minecraft.world.entity.player.Player;

/**
 * 护符槽位动态上限：POE2 默认 3 槽，受「+X Charm Slot」属性（{@code AttributeRegistry#CHARM_SLOT}）加成。
 * 容量改为直接读 {@link StatEngine#foldDelta} 经 {@code AttributeRegistry#CHARM_SLOT} 的累加值，
 * 基础值 3（默认）由天赋 / 腰带词缀叠加，并钳制在 [0,5]。
 * 读点真实（{@link StatEngine#fold} 经 StatStore），非死线（R3）。
 */
public final class CharmSlots {
    private CharmSlots() {
    }

    /** 玩家当前可用护符槽数 = clamp(fold(charm_slot), 0, 5)。基础 3，天赋 / 腰带再加到 5。 */
    public static int count(Player player) {
        double extra = StatEngine.foldDelta(player, AttributeRegistry.CHARM_SLOT);
        return (int) Math.min(5, Math.max(0, Math.floor(extra)));
    }
}
