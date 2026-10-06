package com.cimdy.coe.data.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * 本模组共用的编解码器。
 *
 * <p>这些原本挂在 UI 包的 {@code DataProvider} 接口上。它们编解码的是
 * {@link ItemStack} / {@link ItemResource} 这类<b>业务数据</b>，和界面没有关系，
 * 放在 UI 库里会让「界面库」反向依赖业务类型，所以搬到这里。
 */
public final class CoeCodecs {

    private CoeCodecs() {}

    /**
     * 存进 {@link CompoundTag} 时保留附魔等动态注册表内容的 ItemStack 编解码器。
     *
     * <p>{@link ItemStack#CODEC} 需要 {@link RegistryOps}；调用方常常只有一个裸
     * {@link NbtOps}，于是这里在编解码前按需补上当前连接的注册表。
     */
    public static final Codec<ItemStack> FLEXIBLE_STACK = new Codec<>() {
        @Override
        public <T> DataResult<Pair<ItemStack, T>> decode(DynamicOps<T> ops, T input) {
            return ItemStack.CODEC.decode(withRegistry(ops), input);
        }

        @Override
        public <T> DataResult<T> encode(ItemStack input, DynamicOps<T> ops, T prefix) {
            return ItemStack.CODEC.encode(input, withRegistry(ops), prefix);
        }

        private <T> DynamicOps<T> withRegistry(DynamicOps<T> ops) {
            return ops instanceof RegistryOps ? ops : RegistryOps.create(ops, clientRegistries());
        }
    };

    /** 网络传输用的 ItemStack 流式编解码器：客户端编码时主动带上当前世界的注册表。 */
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> ITEM_STACK_STREAM = new StreamCodec<>() {
        @Override
        public ItemStack decode(RegistryFriendlyByteBuf buf) {
            CompoundTag tag = buf.readNbt();
            if (tag == null || tag.isEmpty()) return ItemStack.EMPTY;
            // 服务端解码时 buf 自带完整注册表
            RegistryOps<net.minecraft.nbt.Tag> ops = RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess());
            return ItemStack.CODEC.decode(ops, tag).getOrThrow().getFirst();
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ItemStack stack) {
            if (stack.isEmpty()) {
                buf.writeNbt(null);
                return;
            }
            // 网络编码时 buf 已携带权威注册表（服务端/客户端通用），不要用依赖 Minecraft 客户端的 clientRegistries()
            RegistryOps<net.minecraft.nbt.Tag> ops = RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess());
            buf.writeNbt((CompoundTag) ItemStack.CODEC.encodeStart(ops, stack).getOrThrow());
        }
    };

    /** ItemResource 走 ItemStack 的可选流式编解码器，空资源编码为空栈。 */
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemResource> ITEM_RESOURCE_STREAM = new StreamCodec<>() {
        @Override
        public ItemResource decode(RegistryFriendlyByteBuf buf) {
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            return stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ItemResource resource) {
            ItemStack stack = resource.isEmpty() ? ItemStack.EMPTY : resource.toStack(1);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
        }
    };

    /**
     * 尽力取到一份带动态注册表的 {@link RegistryAccess}：
     * 当前世界 → 当前连接 → 内置注册表兜底。
     */
    private static RegistryAccess clientRegistries() {
        // 专用服务端没有 Minecraft 客户端类（不在 classpath），直接回退内置注册表，避免 NoClassDefFoundError
        if (FMLEnvironment.getDist() != Dist.CLIENT) {
            return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) return mc.level.registryAccess();
        if (mc.getConnection() != null) return mc.getConnection().registryAccess();
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }
}
