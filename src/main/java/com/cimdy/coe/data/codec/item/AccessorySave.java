package com.cimdy.coe.data.codec.item;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 饰品栏存档：单个玩家附件，扁平存储全部饰品槽物品（下标 = 全局槽位下标）。
 * 槽数由服务端配置 {@code config/coe/slots.json} 的 accessory_slots 决定（无配置时走内置兜底），
 * 故附件列表长度可能在
 * 重载后变化——{@link #get(int)} 越界返回空栈、{@link #with(int, ItemStack)} 自动补齐，
 * 使读写对长度漂移健壮。
 *
 * <p>已同步（客户端 UI 需渲染槽内物品）、<b>死亡保留</b>（与 Flask/Charm 同策略）。
 */
public record AccessorySave(List<ItemStack> slots) {

    public static final AccessorySave EMPTY = new AccessorySave(List.of());

    public static final Codec<AccessorySave> CODEC = ItemStack.OPTIONAL_CODEC.listOf()
            .optionalFieldOf("slots", List.of()).codec()
            .xmap(AccessorySave::new, AccessorySave::slots);

    /**
     * 同步流编解码：ItemStack 需要注册表访问，故走 {@link ItemStack#OPTIONAL_STREAM_CODEC}
     * （不能用 {@code ByteBufCodecs.fromCodec}——后者用 NbtOps 无注册表上下文，ItemStack 会抛错）。
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, AccessorySave> STREAM_CODEC =
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(AccessorySave::new, AccessorySave::slots);

    /** 读取槽位物品（越界返回空栈）。 */
    public ItemStack get(int index) {
        return index >= 0 && index < slots.size() ? slots.get(index) : ItemStack.EMPTY;
    }

    /** 写入槽位并返回新实例（自动补齐到 index+1，避免长度漂移导致越界）。 */
    public AccessorySave with(int index, ItemStack stack) {
        List<ItemStack> copy = new ArrayList<>(slots);
        while (copy.size() <= index) copy.add(ItemStack.EMPTY);
        if (index >= 0) copy.set(index, stack.copy());
        return new AccessorySave(List.copyOf(copy));
    }
}
