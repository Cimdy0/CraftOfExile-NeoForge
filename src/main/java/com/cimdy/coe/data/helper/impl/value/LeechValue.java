package com.cimdy.coe.data.helper.impl.value;

import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.helper.hub.ResourceEventHelper;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源偷取存储：资源类型作为键（{@link GateTag#HEALTH}/{@link GateTag#MANA}/{@link GateTag#ENERGY} 的字符串值，
 * 即 {@code "health"/"mana"/"energy"}）。所有资源使用方法一致、计算方法一致，故不按资源拆分字段/分支，
 * 而是以资源类型字符串为键统一驱动：实例按资源键聚合、回复按资源键查表，事件标签亦用同一资源键。
 * <p>
 * 不使用技能体系的 {@code ResourceType} 枚举——偷取只需朴素资源键，无需理会其技能侧语义。
 */
public record LeechValue(List<LeechInstance> instances) {
    public static final LeechValue EMPTY = new LeechValue(List.of());

    public static final Codec<LeechValue> CODEC =  LeechInstance.CODEC.listOf()
            .xmap(LeechValue::new, LeechValue::instances);

    /** 资源标签 + 来源标签组合：{@code health} 或 {@code health:<sourceId>}，{@code sourceId} 为 null 时不带来源。 */
    private static String tagWithSource(String resource, Identifier sourceId) {
        return sourceId == null ? resource : resource + ":" + sourceId;
    }

    /** 移除某资源类型的所有偷取实例（供 ResourceEventHelper.onResourceFull 满移除回调调用）。 */
    public static void removeResource(LivingEntity living, String resource) {
        LeechValue leech = living.getData(AttachmentRegistry.LEECH);
        if (leech.instances.isEmpty()) return;
        List<LeechInstance> remain = new ArrayList<>();
        for (LeechInstance inst : leech.instances) {
            if (!inst.resource().equals(resource)) remain.add(inst);
        }
        if (remain.isEmpty()) living.removeData(AttachmentRegistry.LEECH);
        else living.setData(AttachmentRegistry.LEECH, new LeechValue(remain));
    }

    /** 通用写入：向指定资源类型追加一次偷取（{@code perTick}=每 tick 回复量，{@code remaining}=剩余 tick）。 */
    public static void addValue(LivingEntity living, String resource, float perTick, int remaining, Identifier sourceId) {
        LeechValue leech = living.getData(AttachmentRegistry.LEECH);
        List<LeechInstance> next = new ArrayList<>(leech.instances);
        next.add(new LeechInstance(resource, perTick, remaining));
        living.setData(AttachmentRegistry.LEECH, new LeechValue(next));
        // 单一 LEECH 事件 + 资源类型标签(+来源) 回调近期状态；来源维度使不同来源偷取可独立判定速率
        Event.record(living, EventName.LEECH, tagWithSource(resource, sourceId));
    }

    public static void addHealthValue(LivingEntity living, float perTick, int remaining) {
        addValue(living, GateTag.HEALTH, perTick, remaining, null);
    }

    public static void addHealthValue(LivingEntity living, float perTick, int remaining, Identifier sourceId) {
        addValue(living, GateTag.HEALTH, perTick, remaining, sourceId);
    }

    public static void addManaValue(LivingEntity living, float perTick, int remaining) {
        addValue(living, GateTag.MANA, perTick, remaining, null);
    }

    public static void addManaValue(LivingEntity living, float perTick, int remaining, Identifier sourceId) {
        addValue(living, GateTag.MANA, perTick, remaining, sourceId);
    }

    public static void addEnergyValue(LivingEntity living, float perTick, int remaining) {
        addValue(living, GateTag.ENERGY, perTick, remaining, null);
    }

    public static void addEnergyValue(LivingEntity living, float perTick, int remaining, Identifier sourceId) {
        addValue(living, GateTag.ENERGY, perTick, remaining, sourceId);
    }

    /** 每 tick 结算所有偷取实例：按资源键聚合各自取最大流率回复（非叠加），各实例独立计时。 */
    public static void calculateAndUpdateValues(LivingEntity living) {
        LeechValue leech = living.getData(AttachmentRegistry.LEECH);
        if (leech.instances.isEmpty()) return;

        // 按资源键聚合：每个资源取最大流率（Q0：全部存储、取最大）
        java.util.Map<String, Float> maxPerTick = new java.util.HashMap<>();
        List<LeechInstance> remain = new ArrayList<>();
        for (LeechInstance inst : leech.instances) {
            if (inst.remaining() > 0) {
                maxPerTick.merge(inst.resource(), inst.perTick(), Float::max);
                remain.add(new LeechInstance(inst.resource(), inst.perTick(), inst.remaining() - 1)); // 各自独立计时
            }
        }
        remain.removeIf(inst -> inst.remaining() <= 0); // 移除已结束实例

        // 统一回复通道：经 ResourceEventHelper（处理 actual/overflow/转化/满移除回调/Context 检测）
        maxPerTick.forEach((resource, amount) -> ResourceEventHelper.onHeal(living, resource, amount, "leech", null));

        if (remain.isEmpty()) {
            living.removeData(AttachmentRegistry.LEECH);
        } else {
            living.setData(AttachmentRegistry.LEECH, new LeechValue(remain));
        }
    }

    /** 单次偷取实例：资源键 + 每 tick 回复量 + 剩余 tick。 */
    public record LeechInstance(String resource, float perTick, int remaining) {
        public static final Codec<LeechInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("resource").forGetter(LeechInstance::resource),
                Codec.FLOAT.fieldOf("perTick").forGetter(LeechInstance::perTick),
                Codec.INT.fieldOf("remaining").forGetter(LeechInstance::remaining)
        ).apply(instance, LeechInstance::new));
    }
}
