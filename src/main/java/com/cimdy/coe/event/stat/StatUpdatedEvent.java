package com.cimdy.coe.event.stat;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.Event;

/**
 * 作用域内某属性的 stat 值已被主动重算（事件驱动更新）后广播，
 * 供 UI / 条件系统等订阅者实时刷新，而无需每次主动读属性。
 *
 * <p>{@code value} 为当前作用域下经 {@code StatEngine.fold} 后的值（mixin 已按栈顶作用域聚合）。
 */
public final class StatUpdatedEvent extends Event {
    private final LivingEntity entity;
    private final Holder<Attribute> attribute;
    private final double value;

    public StatUpdatedEvent(LivingEntity entity, Holder<Attribute> attribute, double value) {
        this.entity = entity;
        this.attribute = attribute;
        this.value = value;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public Holder<Attribute> getAttribute() {
        return attribute;
    }

    public double getValue() {
        return value;
    }
}
