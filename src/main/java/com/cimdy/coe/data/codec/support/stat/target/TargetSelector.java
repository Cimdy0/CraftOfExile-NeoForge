package com.cimdy.coe.data.codec.support.stat.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stat 的基础目标锁定：只区分持有者 A（SELF）与事件另一方 B（TRIGGER_TARGET）。
 * 范围、阵营和归属集合由 Condition 获取，不属于 Target。
 */
public class TargetSelector {

    public enum Source {
        /** 效果持有者自身（A）。 */
        SELF,
        /** 事件上下文中的另一方（B）。 */
        TRIGGER_TARGET;

        public String serializedName() {
            return SOURCE_SERIALIZED.get(this);
        }

        /** 基础 Target 均为单实体锁定。 */
        public boolean isMulti() {
            return false;
        }

        public Component describeName() {
            return Component.translatable("stat.target." + serializedName());
        }
    }

    private static final Map<String, Source> SOURCE_NAMES = buildSourceNames();
    private static final Map<Source, String> SOURCE_SERIALIZED = buildSourceSerialized();

    private static Map<Source, String> buildSourceSerialized() {
        Map<Source, String> m = new EnumMap<>(Source.class);
        SOURCE_NAMES.forEach((name, src) -> m.put(src, name));
        return Map.copyOf(m);
    }

    private static Map<String, Source> buildSourceNames() {
        Map<String, Source> m = new LinkedHashMap<>();
        m.put("self", Source.SELF);
        m.put("trigger_target", Source.TRIGGER_TARGET);
        return Map.copyOf(m);
    }

    private final Source source;

    public TargetSelector(Source source) {
        this.source = source;
    }

    /** SELF 常量（作用于持有者自身）。 */
    public static final TargetSelector SELF = new TargetSelector(Source.SELF);
    /** EVENT_TARGET 常量（作用于触发目标）。 */
    public static final TargetSelector EVENT_TARGET = new TargetSelector(Source.TRIGGER_TARGET);

    public Source source() {
        return source;
    }

    /** 按事件上下文锁定 A 或 B。 */
    public List<LivingEntity> resolve(LivingEntity owner, LivingEntity triggerTarget) {
        return switch (source) {
            case SELF -> owner == null ? List.of() : List.of(owner);
            case TRIGGER_TARGET -> triggerTarget == null ? List.of() : List.of(triggerTarget);
        };
    }

    /** 序列化：仅接受 "self" / "trigger_target"。 */
    public static final Codec<TargetSelector> CODEC = Codec.STRING.comapFlatMap(
            s -> {
                Source src = SOURCE_NAMES.get(s);
                return src != null ? DataResult.success(new TargetSelector(src))
                        : DataResult.error(() -> "unknown target source: " + s);
            },
            sel -> sel.source.serializedName());
}
