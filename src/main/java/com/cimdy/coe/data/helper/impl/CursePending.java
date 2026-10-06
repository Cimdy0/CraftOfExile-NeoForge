package com.cimdy.coe.data.helper.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/**
 * 诅咒激活延迟队列的单项：记录一次「延迟施放诅咒」的元数据与计划触发时刻。
 * 存于施法者 {@code CURSE_PENDING} 附件，由 {@link CurseActivation#tick} 每轮消费。
 */
public record CursePending(Identifier skillId, double radius, int duration, long scheduledTick) {
    public static final Codec<CursePending> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("skill").forGetter(CursePending::skillId),
            Codec.DOUBLE.fieldOf("radius").forGetter(CursePending::radius),
            Codec.INT.fieldOf("duration").forGetter(CursePending::duration),
            Codec.LONG.fieldOf("tick").forGetter(CursePending::scheduledTick)
    ).apply(i, CursePending::new));
}
