package com.cimdy.coe.data.codec.item;

import com.cimdy.coe.registry.item.CharmSlotRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Charm（护符）槽位数据（POE2 实证：默认最多 3 个 Charm 槽，受腰带「Charm Slots」属性加成）。
 * 存于玩家附件 {@code charm_save}（服务端权威、同步客户端），由统一窗口
 * {@code ExtraSlotsMenu}/{@code ExtraSlotsScreen} 读写。列表为动态长度，
 * 实际槽数由 {@code coe:charm_slot} 数据包注册表决定（{@link com.cimdy.coe.registry.item.CharmSlotRegistry}）。
 */
public record CharmSave(List<ItemStack> slots) {

    public static final CharmSave EMPTY = new CharmSave(List.of());

    public static final Codec<CharmSave> CODEC = ItemStack.OPTIONAL_CODEC.listOf()
            .optionalFieldOf("slots", List.of()).codec()
            .xmap(CharmSave::new, CharmSave::slots);

    /**
     * 同步流编解码：ItemStack 需要注册表访问，故走 {@link ItemStack#OPTIONAL_STREAM_CODEC}
     * （不能用 {@code ByteBufCodecs.fromCodec}——后者用 NbtOps 无注册表上下文，ItemStack 会抛错）。
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, CharmSave> STREAM_CODEC =
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(CharmSave::new, CharmSave::slots);

    /** 读取槽位物品（越界返回空栈）。 */
    public ItemStack get(int index) {
        return index >= 0 && index < slots.size() ? slots.get(index) : ItemStack.EMPTY;
    }

    /** 槽位数（实际存储的列表长度；动态上限由 {@link com.cimdy.coe.item.charm.CharmSlots#count} 决定）。 */
    public int size() {
        return slots.size();
    }

    /** 写入槽位并返回新实例（动态补齐到 index+1，允许写入超出历史 3 槽的索引）。 */
    public CharmSave with(int index, ItemStack stack) {
        List<ItemStack> copy = new ArrayList<>(slots);
        while (copy.size() <= index) copy.add(ItemStack.EMPTY);
        if (index >= 0) copy.set(index, stack.copy());
        return new CharmSave(List.copyOf(copy));
    }
}
