package com.cimdy.coe.data.codec.item.flask;

import com.cimdy.coe.registry.item.FlaskSlotRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Flask（药剂）槽位数据（POE2 实证：默认两个 Flask 槽——生命药剂槽 + 魔力药剂槽）。
 * 存于玩家附件 {@code flask_save}（服务端权威、同步客户端），由统一窗口
 * {@code ExtraSlotsMenu}/{@code ExtraSlotsScreen} 读写。列表为动态长度，
 * 实际槽数由服务端配置 {@code config/coe/slots.json} 的 flask_slots 决定（无配置时走内置兜底）
 * （{@link com.cimdy.coe.registry.item.FlaskSlotRegistry}）。
 */
public record FlaskSave(List<ItemStack> slots) {

    public static final FlaskSave EMPTY = new FlaskSave(List.of());

    public static final Codec<FlaskSave> CODEC = ItemStack.OPTIONAL_CODEC.listOf()
            .optionalFieldOf("slots", List.of()).codec()
            .xmap(FlaskSave::new, FlaskSave::slots);

    /**
     * 同步流编解码：ItemStack 需要注册表访问，故走 {@link ItemStack#OPTIONAL_STREAM_CODEC}
     * （不能用 {@code ByteBufCodecs.fromCodec}——后者用 NbtOps 无注册表上下文，ItemStack 会抛错）。
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FlaskSave> STREAM_CODEC =
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(FlaskSave::new, FlaskSave::slots);

    /** 读取槽位物品（越界返回空栈）。 */
    public ItemStack get(int index) {
        return index >= 0 && index < slots.size() ? slots.get(index) : ItemStack.EMPTY;
    }

    /** 槽位数（= 实际存储列表长度；动态槽数由 {@link com.cimdy.coe.registry.item.FlaskSlotRegistry} 决定）。 */
    public int size() {
        return slots.size();
    }

    /** 写入槽位并返回新实例（自动补齐到 index+1，兼容数据驱动槽数变化）。 */
    public FlaskSave with(int index, ItemStack stack) {
        List<ItemStack> copy = new ArrayList<>(slots);
        while (copy.size() <= index) copy.add(ItemStack.EMPTY);
        if (index >= 0) copy.set(index, stack.copy());
        return new FlaskSave(List.copyOf(copy));
    }
}
