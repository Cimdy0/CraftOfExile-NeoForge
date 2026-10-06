package com.cimdy.coe.data.codec.talent.data;

import com.cimdy.coe.data.codec.talent.data.TalentWeaponSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

/**
 * 单个已分配节点：节点 id + 所选选项下标（无选项时为 -1）+ 已插入的珠宝（无珠宝时为 {@link ItemStack#EMPTY}）
 * + 绑定的武器组 {@code weaponSet}（{@link TalentWeaponSet#GLOBAL} 为全局，{@code TalentWeaponSet#SET_1}/{@code TalentWeaponSet#SET_2}（两常量已删除；现行绑定值为 GLOBAL=0、栏位 s 取 s+1，见 {@link TalentWeaponSet#MAX_BINDING}） 为武器组专属）。
 * 作为 {@link TalentState} 的存储单元，使「多选一」节点的玩家选择、「珠宝插槽」节点插入的珠宝、
 * 以及「武器专精」绑定的武器组都能被持久化与同步。
 */
public record TalentAllocation(String id, int choice, ItemStack socket, int weaponSet) {
    public static final Codec<TalentAllocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(TalentAllocation::id),
            Codec.INT.optionalFieldOf("choice", -1).forGetter(TalentAllocation::choice),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("socket", ItemStack.EMPTY).forGetter(TalentAllocation::socket),
            Codec.INT.optionalFieldOf("weaponSet", 0).forGetter(TalentAllocation::weaponSet)
    ).apply(instance, TalentAllocation::new));

    /** 便捷构造：无珠宝插槽（保持旧调用点不变），默认全局绑定。 */
    public TalentAllocation(String id, int choice) {
        this(id, choice, ItemStack.EMPTY, 0);
    }

    /** 便捷构造：带珠宝插槽（保持旧调用点不变），默认全局绑定。 */
    public TalentAllocation(String id, int choice, ItemStack socket) {
        this(id, choice, socket, 0);
    }

    /** 该分配是否已插入珠宝。 */
    public boolean hasSocket() {
        return socket != null && !socket.isEmpty();
    }
}
