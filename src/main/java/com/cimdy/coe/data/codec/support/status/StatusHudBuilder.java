package com.cimdy.coe.data.codec.support.status;

import com.cimdy.coe.event.combat.status.StatusEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.status.runtime.StatusInstance;
import com.cimdy.coe.data.codec.support.status.StatusHudEntry;
import com.cimdy.coe.data.codec.support.status.runtime.StatusStore;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 由 {@code STATUS_STORE} 汇总出客户端 HUD 所需的轻量状态快照（原 {@code event.StatusEvent#rebuildHud}，铁律 #56 迁出）。
 */
public final class StatusHudBuilder {
    private StatusHudBuilder() {
    }

    /** 重建并写回玩家的 {@code STATUS_HUD} 附件。 */
    public static void rebuild(Player player) {
        StatusStore store = player.getData(AttachmentRegistry.STATUS_STORE);
        List<StatusHudEntry> list = new ArrayList<>();
        if (store != StatusStore.EMPTY) {
            for (StatusInstance inst : store.instances.values()) {
                StatusType.StatusDisplayAction da = inst.type.displayAction;
                String text = da != null ? da.text(inst, player) : "";
                boolean showTime = da != null ? da.showTime(inst, player) : true;
                list.add(new StatusHudEntry(
                        inst.type.id,
                        inst.iconOverride != null ? inst.iconOverride : inst.type.icon(),
                        inst.durationTicks,
                        inst.magnitude,
                        inst.stacks,
                        text,
                        showTime));
            }
        }
        player.setData(AttachmentRegistry.STATUS_HUD, list);
    }
}
