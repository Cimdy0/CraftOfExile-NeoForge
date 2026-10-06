package com.cimdy.coe.event.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 数据类别的「JSON 落盘」描述符：把原 datapack 注册表序列化的内容改由 {@link CoeDataProvider}
 * 直接写出，路径与 {@code LoadEvent} 的扫描前缀同源（均取自对应 {@code ResourceKey} 的 identifier path），
 * 故客户端 / 运行期取数不变。
 *
 * @param path 资源路径前缀（如 {@code coe/mod_number}），与 LoadEvent 的 scanResources 前缀一致
 * @param codec 该类别数据的编解码器
 * @param gen 生成器：接收 lookup（生成期注册表查找，无需求可忽略）与输出收集 Map，把每条 (id, value) 填入
 */
public record JsonCategory<T>(String path, Codec<T> codec,
        BiConsumer<HolderLookup.Provider, Map<Identifier, T>> gen) {
}
