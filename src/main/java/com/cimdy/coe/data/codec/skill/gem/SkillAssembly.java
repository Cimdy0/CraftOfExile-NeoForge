package com.cimdy.coe.data.codec.skill.gem;

import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.gem.SupportData;
import com.cimdy.coe.registry.AttributeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 技能装配数据 — 存储玩家的技能装配槽位。
 * <p>{@link #SIZE} 为底层存储的最大容量（上限），实际可用槽数在打开界面时由玩家属性
 * {@code AttributeRegistry.SKILL_SLOT_COUNT}（默认 9）决定（属性范围 [0,99]，本处再 clamp 到 {@link #SIZE}）。
 * 每个槽位可空（{@link SkillData#EMPTY}），装配了技能的槽位才能被绑定到按键槽。
 * 辅助宝石信息存储在 SkillData.supports 中，由本装配表的每个槽位各自持有。
 */
public record SkillAssembly(List<SkillData> slots, List<Boolean> auraEnabled) {
    /** 底层存储的最大槽位容量（上限）；实际可用槽数由玩家属性决定。 */
    public static final int SIZE = 12;

    private static List<SkillData> createEmptySlots() {
        List<SkillData> list = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) list.add(SkillData.EMPTY);
        return List.copyOf(list);
    }

    /** 默认全部光环关闭（精魂需手动开启）。 */
    private static List<Boolean> createFalseAura() {
        List<Boolean> list = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) list.add(false);
        return List.copyOf(list);
    }

    public static final SkillAssembly EMPTY = new SkillAssembly(createEmptySlots(), createFalseAura());

    public static final Codec<SkillAssembly> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SkillData.CODEC.listOf(0, SIZE).optionalFieldOf("slots", createEmptySlots())
                    .forGetter(SkillAssembly::slots),
            Codec.BOOL.listOf(0, SIZE).optionalFieldOf("aura_enabled", createFalseAura())
                    .forGetter(SkillAssembly::auraEnabled)
    ).apply(instance, SkillAssembly::new));

    /** 规范化：确保 auraEnabled 长度恒为 SIZE，缺失补 false（兼容旧存档）。 */
    public SkillAssembly {
        if (auraEnabled == null || auraEnabled.size() < SIZE) {
            List<Boolean> norm = new ArrayList<>(SIZE);
            int srcSize = auraEnabled == null ? 0 : auraEnabled.size();
            for (int i = 0; i < SIZE; i++) {
                norm.add(i < srcSize ? auraEnabled.get(i) : false);
            }
            auraEnabled = List.copyOf(norm);
        }
    }

    /** 获取指定槽位的技能数据（空槽返回 SkillData.EMPTY）。 */
    public SkillData get(int index) {
        return index >= 0 && index < slots.size() ? slots.get(index) : SkillData.EMPTY;
    }

    /** 在指定槽位装配技能石，返回新的 SkillAssembly（保留光环开关状态）。 */
    public SkillAssembly set(int index, SkillData skill) {
        List<SkillData> copy = new ArrayList<>(slots);
        while (copy.size() <= index) copy.add(SkillData.EMPTY);
        // 装配新技能石时清空该槽的辅助宝石
        copy.set(index, skill != null ? skill : SkillData.EMPTY);
        return new SkillAssembly(List.copyOf(copy), this.auraEnabled);
    }

    /** 设置指定槽位的光环开关（仅对精魂宝石有意义）。 */
    public SkillAssembly setAuraEnabled(int index, boolean enabled) {
        if (index < 0 || index >= SIZE) return this;
        List<Boolean> copy = new ArrayList<>(this.auraEnabled);
        while (copy.size() <= index) copy.add(false);
        copy.set(index, enabled);
        return new SkillAssembly(this.slots, List.copyOf(copy));
    }

    /** 查询指定槽位的光环开关状态。 */
    public boolean isAuraEnabled(int index) {
        return index >= 0 && index < auraEnabled.size() && auraEnabled.get(index);
    }

    /** 为第 slot 个装配槽的第 supportIndex 个辅助槽装配辅助宝石。 */
    public SkillAssembly setSupport(int slot, int supportIndex, SupportData supportData) {
        SkillData current = get(slot);
        if (current.isEmpty()) return this;
        List<SupportData> list = new ArrayList<>(current.supports());
        while (list.size() <= supportIndex) list.add(SupportData.EMPTY);
        list.set(supportIndex, supportData != null ? supportData : SupportData.EMPTY);
        SkillData updated = new SkillData(current.id(), current.status(), current.level(),
                current.quality(), current.sockets(), List.copyOf(list), current.spirit());
        return set(slot, updated);
    }

    /** 清空第 slot 个装配槽的第 supportIndex 个辅助槽。 */
    public SkillAssembly clearSupport(int slot, int supportIndex) {
        SkillData current = get(slot);
        if (current.isEmpty() || supportIndex >= current.supports().size()) return this;
        List<SupportData> list = new ArrayList<>(current.supports());
        list.set(supportIndex, SupportData.EMPTY);
        SkillData updated = new SkillData(current.id(), current.status(), current.level(),
                current.quality(), current.sockets(), List.copyOf(list), current.spirit());
        return set(slot, updated);
    }

    public boolean isEmpty(int index) {
        return get(index).isEmpty();
    }

    public boolean isEmpty() {
        return slots.stream().allMatch(SkillData::isEmpty);
    }
}
