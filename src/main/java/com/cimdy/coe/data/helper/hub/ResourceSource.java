package com.cimdy.coe.data.helper.hub;

import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

/**
 * 资源<b>数据源</b> —— 声明一个资源绑定到<b>既有的、可增可减的</b>数值存储位置，
 * 而非新建存储。统一层只透过本源做 get/set，不引入任何新 Map/附件。
 *
 * <p>支持两类既有数据源（随需求可在此扩展）：
 * <ul>
 *   <li>{@code status_stacks} —— 某状态的层数（StatusStore 既有存储），
 *       经 {@link StatusEngine#stacks}/{@link StatusEngine#setStacks} 读写，id 经 {@link StatusType#byId} 解析为已注册实例。</li>
 *   <li>{@code attachment} —— 某既有附件里的整型/浮点计数（如召唤实体数 {@code summoned_count}），
 *       经 {@link ResourceBindings} 中的既有访问器读写。</li>
 * </ul>
 */
public record ResourceSource(String type, String status, String attachment) {

    public static final Codec<ResourceSource> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("type").forGetter(ResourceSource::type),
            Codec.STRING.optionalFieldOf("status").forGetter(s -> Optional.ofNullable(s.status)),
            Codec.STRING.optionalFieldOf("attachment").forGetter(s -> Optional.ofNullable(s.attachment))
    ).apply(i, (t, st, at) -> new ResourceSource(t, st.orElse(null), at.orElse(null))));

    public double get(LivingEntity e) {
        if ("status_stacks".equals(type) && status != null) {
            return StatusType.byId(status).map(t -> (double) StatusEngine.stacks(e, t)).orElse(0.0);
        }
        if ("attachment".equals(type) && attachment != null) {
            return ResourceBindings.get(e, attachment);
        }
        return 0.0;
    }

    public void set(LivingEntity e, double v) {
        if ("status_stacks".equals(type) && status != null) {
            StatusType.byId(status).ifPresent(t -> StatusEngine.setStacks(e, t, (int) Math.round(v)));
            return;
        }
        if ("attachment".equals(type) && attachment != null) {
            ResourceBindings.set(e, attachment, v);
        }
    }
}
