package com.cimdy.coe.data.codec.support.stat.duration;

import com.cimdy.coe.event.data.LoadEvent;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Objects;

/**
 * 时长的引用形态：仅持有 {@code ref}，经 {@link LoadEvent#STAT_DURATION_MAP} 解析真实时长
 * （与 {@link com.cimdy.coe.data.codec.support.stat.entry.StatRead} 同源思路，
 * 但引用能力内聚在本类型内部）。扁平写出形如 {@code read:coe:my_aura}。
 *
 * @param ref 落盘时长定义的 id
 */
public record StatDurationRead(Identifier ref) implements StatDuration {

    public StatDurationRead {
        Objects.requireNonNull(ref, "StatDurationRead.ref 不能为 null");
    }

    /** 解析后的目标（fail-fast：未注册即抛错）。 */
    private StatDuration resolved() {
        StatDuration got = LoadEvent.STAT_DURATION_MAP.get(ref);
        if (got == null) {
            throw new IllegalStateException("StatDuration 引用了未注册的 duration: " + ref);
        }
        return got;
    }

    @Override
    public StatDuration.Kind kind() {
        return resolved().kind();
    }

    @Override
    public int ticks() {
        return resolved().ticks();
    }

    @Override
    public String type() {
        return "read";
    }

    /** 引用形态不内联展开：始终写出 {@code read:<id>}。 */
    @Override
    public String flatten() {
        return READ_PREFIX + ":" + ref;
    }

    public static final MapCodec<StatDurationRead> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf("ref").forGetter(StatDurationRead::ref)
    ).apply(i, StatDurationRead::new));

    public static final com.mojang.serialization.Codec<StatDurationRead> CODEC = MAP_CODEC.codec();

    @Override
    public String toString() {
        return flatten();
    }
}
