package com.cimdy.coe.data.helper.hub;

import com.cimdy.coe.data.codec.skill.helper.state.ResourcePool;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.ToDoubleFunction;

/**
 * 资源注册表条目 —— 资源键 → 状态池 + 元数据（上限读取 / 溢出模式 / 是否浮点 / 是否发事件）。
 * 由 {@link ResourceRegistry} 按资源键索引，是统一资源抽象的核心描述符。
 */
public final class ResourceEntry {
    private final String key;
    private final ResourcePool pool;
    private final boolean isFloat;
    private final Double fixedMax;
    private final ToDoubleFunction<LivingEntity> maxReader;
    private final OverflowMode overflow;
    private final boolean fireGain;
    private final boolean fireSpend;
    private final boolean recoverable;

    private ResourceEntry(Builder b) {
        this.key = b.key;
        this.pool = b.pool;
        this.isFloat = b.isFloat;
        this.fixedMax = b.fixedMax;
        this.maxReader = b.maxReader;
        this.overflow = b.overflow;
        this.fireGain = b.fireGain;
        this.fireSpend = b.fireSpend;
        this.recoverable = b.recoverable;
    }

    public String key() { return key; }
    public ResourcePool pool() { return pool; }
    public boolean isFloat() { return isFloat; }
    public OverflowMode overflow() { return overflow; }
    public boolean fireGain() { return fireGain; }
    public boolean fireSpend() { return fireSpend; }
    public boolean recoverable() { return recoverable; }

    /** 上限：fixedMax 优先，否则 maxReader（无 reader 返回 -1 表示无上限语义）。 */
    public double max(LivingEntity living) {
        if (fixedMax != null) return fixedMax;
        return maxReader == null ? -1.0 : maxReader.applyAsDouble(living);
    }

    public static Builder builder(String key, ResourcePool pool) {
        return new Builder(key, pool);
    }

    public static final class Builder {
        private final String key;
        private final ResourcePool pool;
        private boolean isFloat = true;
        private Double fixedMax = null;
        private ToDoubleFunction<LivingEntity> maxReader = null;
        private OverflowMode overflow = OverflowMode.REPLACE;
        private boolean fireGain = false;
        private boolean fireSpend = true;
        private boolean recoverable = false;

        private Builder(String key, ResourcePool pool) {
            this.key = key;
            this.pool = pool;
        }

        public Builder isFloat(boolean v) { this.isFloat = v; return this; }
        public Builder fixedMax(double v) { this.fixedMax = v; return this; }
        public Builder maxReader(ToDoubleFunction<LivingEntity> v) { this.maxReader = v; return this; }
        public Builder overflow(OverflowMode v) { this.overflow = v; return this; }
        public Builder fireGain(boolean v) { this.fireGain = v; return this; }
        public Builder fireSpend(boolean v) { this.fireSpend = v; return this; }
        public Builder recoverable(boolean v) { this.recoverable = v; return this; }

        public ResourceEntry build() { return new ResourceEntry(this); }
    }
}
