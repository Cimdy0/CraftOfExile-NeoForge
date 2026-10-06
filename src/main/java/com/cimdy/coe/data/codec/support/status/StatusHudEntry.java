package com.cimdy.coe.data.codec.support.status;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * HUD 渲染用的状态快照条目。仅携带渲染所需的最小信息，由服务端周期性从
 * {@link StatusStore} 汇总后写入同步附件 {@code status_hud}，供客户端绘制状态图标。
 *
 * <p>图标优先取 {@link StatusInstance#iconOverride}（技能施加的状态直接用技能图标），
 * 否则回落 {@link StatusType#icon()}（assets/coe/textures/gui/status/&lt;id&gt;.png，
 * 图标资源由 poe2db 爬取获得）。
 */
public record StatusHudEntry(
        String id,
        Identifier icon,
        int durationTicks,
        double magnitude,
        int stacks,
        String text,
        boolean showTimeBelow
) {
    public static final Codec<StatusHudEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("id").forGetter(StatusHudEntry::id),
            Identifier.CODEC.fieldOf("icon").forGetter(StatusHudEntry::icon),
            Codec.INT.fieldOf("d").forGetter(StatusHudEntry::durationTicks),
            Codec.DOUBLE.fieldOf("m").forGetter(StatusHudEntry::magnitude),
            Codec.INT.fieldOf("s").forGetter(StatusHudEntry::stacks),
            Codec.STRING.optionalFieldOf("t", "").forGetter(StatusHudEntry::text),
            Codec.BOOL.optionalFieldOf("st", true).forGetter(StatusHudEntry::showTimeBelow)
    ).apply(i, StatusHudEntry::new));

    public static final List<StatusHudEntry> EMPTY = List.of();
}
