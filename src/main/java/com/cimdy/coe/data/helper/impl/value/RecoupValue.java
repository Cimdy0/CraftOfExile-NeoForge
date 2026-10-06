package com.cimdy.coe.data.helper.impl.value;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;
import com.cimdy.coe.event.combat.AttributeEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;

import com.cimdy.coe.data.helper.hub.ResourceEventHelper;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.registry.AttributeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 补给（Recoup）实体级存储：受到伤害后按比例随时间恢复生命/魔力/能量护盾。
 * <p>
 * POE2 补给机制：受到伤害时，按 {@code RECOUP_HEALTH} / {@code RECOUP_MANA} / {@code RECOUP_ENERGY}
 * 属性比例将部分伤害转化为补给值，在随后 8 秒（160 tick）内均匀恢复。
 * <p>
 * 固定补给持续时间 = 8 秒（POE2 默认）。若后续需要变量化可通过 {@code RECOUP_DURATION} 属性扩展。
 *
 * @see com.cimdy.coe.registry.AttributeRegistry#RECOUP_HEALTH
 * @see com.cimdy.coe.registry.AttributeRegistry#RECOUP_MANA
 */
public record RecoupValue(List<Value> health, List<Value> mana, List<Value> energy) {

    /** 补给固定持续时间（tick）：8 秒。 */
    public static final int RECOUP_DURATION = 8 * 20;

    public static final RecoupValue EMPTY = new RecoupValue(List.of(), List.of(), List.of());

    public static final Codec<RecoupValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Value.CODEC.listOf().fieldOf("health").forGetter(RecoupValue::health),
            Value.CODEC.listOf().fieldOf("mana").forGetter(RecoupValue::mana),
            Value.CODEC.listOf().fieldOf("energy").forGetter(RecoupValue::energy)
    ).apply(instance, RecoupValue::new));

    /**
     * 将受到的部分伤害注册为补给值（每资源类型独立调用一次：资源键 → 对应列表追加）。
     * <p>
     * 旧值若耗尽（duration ≤ 0）则移除；新值以 {@link Value} 形式追加到对应资源列表。
     * 资源键与 {@link GateTag} 资源标签同源（{@code health}/{@code mana}/{@code energy}），
     * 新增资源只需在此补一个分支 + record 字段，不新增整段流程。
     */
    public static void addRecoup(LivingEntity living, String resource, float amount) {
        RecoupValue recoup = living.getData(AttachmentRegistry.RECOUP);
        List<Value> newHealth = recoup.health;
        List<Value> newMana = recoup.mana;
        List<Value> newEnergy = recoup.energy;
        switch (resource) {
            case GateTag.HEALTH -> newHealth = fromExisting(recoup.health, amount, RECOUP_DURATION);
            case GateTag.MANA -> newMana = fromExisting(recoup.mana, amount, RECOUP_DURATION);
            case GateTag.ENERGY -> newEnergy = fromExisting(recoup.energy, amount, RECOUP_DURATION);
            default -> {
                return;
            }
        }
        living.setData(AttachmentRegistry.RECOUP, new RecoupValue(newHealth, newMana, newEnergy));
        // 落 Context 事件存储：使「受击补给」可被 EventHistoryValue（recently:recoup / count:recoup）读取，
        // 与 leech（Event.record(LEECH)）对称；资源类型即标签（health/mana/energy）
        Event.record(living, EventName.RECOUP, resource);
    }

    /** 每 tick 回收补给值。由 {@code AttributeEvent.EntityTickEvent.Pre} 在 regeneration 作用域内调用。 */
    public static void calculateAndUpdateValues(LivingEntity living) {
        if (!living.hasData(AttachmentRegistry.RECOUP)) return;
        RecoupValue recoup = living.getData(AttachmentRegistry.RECOUP);

        float[] recover = new float[3]; // [health, mana, energy_shield]

        List<Value> newHealth = tickAndAccumulate(recoup.health, living.getHealth() < living.getMaxHealth(), recover, 0);
        List<Value> newMana = tickAndAccumulate(recoup.mana, !ResourceRegistry.isFullMana(living), recover, 1);
        // recoup energy 语义 = 能量护盾补给
        List<Value> newEnergy = tickAndAccumulate(recoup.energy, !ResourceRegistry.isFullEnergyShield(living), recover, 2);

        if (recover[0] > 0) ResourceEventHelper.onHeal(living, GateTag.HEALTH, recover[0], "recoup", null);
        if (recover[1] > 0) ResourceEventHelper.onHeal(living, GateTag.MANA, recover[1], "recoup", null);
        if (recover[2] > 0) ResourceEventHelper.onHeal(living, GateTag.ENERGY_SHIELD, recover[2], "recoup", null);

        if (newHealth.isEmpty() && newMana.isEmpty() && newEnergy.isEmpty()) {
            living.removeData(AttachmentRegistry.RECOUP);
        } else {
            living.setData(AttachmentRegistry.RECOUP, new RecoupValue(newHealth, newMana, newEnergy));
        }
    }

    // ---- 内部辅助 ----

    /** 从既存列表 + 新增补给量构造新列表。 */
    private static List<Value> fromExisting(List<Value> existing, float amount, int duration) {
        List<Value> list = new ArrayList<>();
        for (Value v : existing) {
            if (v.duration() > 0) list.add(v);
        }
        if (amount > 0) list.add(new Value(amount, duration));
        return list;
    }

    /**
     * 递减时长并累计本 tick 回复量。
     *
     * @param list      补给条目列表
     * @param canRecover 目标资源是否未满（满则丢弃剩余补给）
     * @param recover   累计回复量数组（索引 0=health, 1=mana, 2=energy）
     * @param idx       recover 数组索引
     * @return 新列表（移除已耗尽条目）
     */
    private static List<Value> tickAndAccumulate(List<Value> list, boolean canRecover, float[] recover, int idx) {
        List<Value> result = new ArrayList<>();
        for (Value v : list) {
            int newDuration = v.duration() - 1;
            // 剩余量 = 原始量 × (剩余tick / 总tick)；最后一 tick 剩余为 0
            float remaining = newDuration > 0 ? v.original() * ((float) newDuration / v.duration()) : 0f;
            // 本 tick 回复 = 差分（总量 - 剩余量）：每 tick 恒定 = 总量 / 总持续 tick，
            // 不受「剩余量缩水为新 original」影响，累计回复 = 原始补给总量（否则会衰减到仅回复约一半）
            float tickRecover = v.original() - remaining;
            if (canRecover && tickRecover > 0) {
                recover[idx] += tickRecover;
            }
            if (newDuration > 0 && remaining > 0) {
                result.add(new Value(remaining, newDuration));
            }
        }
        return result;
    }
}
