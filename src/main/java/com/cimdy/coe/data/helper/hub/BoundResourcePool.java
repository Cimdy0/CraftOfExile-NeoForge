package com.cimdy.coe.data.helper.hub;

import com.cimdy.coe.data.codec.skill.helper.state.ResourcePool;
import net.minecraft.world.entity.LivingEntity;

/**
 * 绑定型资源池 —— 把 {@link ResourcePool} 接口透传到某个 {@link ResourceSource}（既有的可修改数值）。
 * 自身<b>不持有任何存储</b>：get/generate/consume/tick 全部委托给数据源，符合
 * 「资源 = 对既有数据的统一视图，而非新建存储」的设计约束。
 */
public final class BoundResourcePool implements ResourcePool {
    private final ResourceSource source;
    private final double max;
    private final boolean perSlot;
    private final double regenPerSecond;

    public BoundResourcePool(ResourceSource source, double max, boolean perSlot, double regenPerSecond) {
        this.source = source;
        this.max = max;
        this.perSlot = perSlot;
        this.regenPerSecond = regenPerSecond;
    }

    @Override
    public double get(LivingEntity l, String id) {
        return source.get(l);
    }

    @Override
    public void generate(LivingEntity l, String id, double inc, Double cap, long gameTime) {
        double capV = cap != null ? cap : (max > 0 ? max : Double.MAX_VALUE);
        double next = Math.min(source.get(l) + inc, capV);
        source.set(l, Math.max(0.0, next));
    }

    @Override
    public double consume(LivingEntity l, String id, double amount) {
        double cur = source.get(l);
        double real = Math.min(Math.max(0.0, amount), cur);
        source.set(l, cur - real);
        return real;
    }

    @Override
    public void clear(LivingEntity l, String id) {
        source.set(l, 0.0);
    }

    @Override
    public void tick(LivingEntity l, long gameTime) {
        if (regenPerSecond <= 0) return;
        double capV = max > 0 ? max : Double.MAX_VALUE;
        double cur = source.get(l);
        source.set(l, Math.min(cur + regenPerSecond / 20.0, capV));
    }
}
