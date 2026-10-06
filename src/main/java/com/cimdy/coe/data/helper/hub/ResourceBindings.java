package com.cimdy.coe.data.helper.hub;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 既有附件计数访问器表 —— 把「数据包资源可引用的既有数据源名」映射到真实的附件读写器。
 * <b>不引入任何新存储</b>：所有条目都指向 {@link AttachmentRegistry} 中已存在的附件。
 *
 * <p>当前登记：{@code summoned_count} → {@link AttachmentRegistry#SUMMONED_COUNT}（召唤实体数）。
 * 新增「可被资源引用的既有附件」只需在此追加一条，无需改动统一层其他代码。
 */
public final class ResourceBindings {
    private interface Accessor {
        double get(LivingEntity e);
        void set(LivingEntity e, double v);
    }

    private static final Map<String, Accessor> MAP = new HashMap<>();
    static {
        MAP.put("summoned_count", new Accessor() {
            @Override public double get(LivingEntity e) { return e.getData(AttachmentRegistry.SUMMONED_COUNT); }
            @Override public void set(LivingEntity e, double v) {
                e.setData(AttachmentRegistry.SUMMONED_COUNT, (int) Math.round(v));
            }
        });
        // 波动：VOLATILITY 浮点附件（VolatilityHelper 已钳制上限）。
        MAP.put("volatility", new Accessor() {
            @Override public double get(LivingEntity e) { return e.getData(AttachmentRegistry.VOLATILITY); }
            @Override public void set(LivingEntity e, double v) {
                e.setData(AttachmentRegistry.VOLATILITY, (float) v);
            }
        });
    }

    public static double get(LivingEntity e, String name) {
        Accessor a = MAP.get(name);
        return a == null ? 0.0 : a.get(e);
    }

    public static void set(LivingEntity e, String name, double v) {
        Accessor a = MAP.get(name);
        if (a != null) a.set(e, v);
    }

    private ResourceBindings() {
    }
}
