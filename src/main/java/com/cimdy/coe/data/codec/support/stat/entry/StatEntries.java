package com.cimdy.coe.data.codec.support.stat.entry;

import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * {@link StatEntry} 的分发注册表（写法与 math 的 {@code MathExpressions} 一致）：
 * 每个实现（内联 {@link Stat} / 引用 {@link StatRead}）在 {@link #REGISTRY} 登记一个
 * {@link StatEntry.Type}，JSON 按 {@code type} 字段（注册表名）派发到对应 Codec。
 */
public final class StatEntries {

    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "stat_entry_type");
    public static final ResourceKey<Registry<StatEntry.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<StatEntry.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(256).create();

    // 原 INLINE 型（内联定义 Stat 类）已移除：内联定义与引用统一收口到 StatRead。
    // 不保留 "inline" JSON 键别名——数据由 datagen 重新生成（统一 entry:"read"），无旧 JSON 兼容负担。
    public static final StatEntry.Type<StatRead> READ = register("read", StatRead.MAP_CODEC);
    public static final StatEntry.Type<StatGroup> GROUP = register("group", StatGroup.MAP_CODEC);
    // 「assemble」已移除：其唯一实现 StatAssemble 无任何构造点、零产物，
    // 且其 ref 形态依赖的 read 条目数为 0，整个组合是死路径。

    private static <T extends StatEntry> StatEntry.Type<T> register(String name, MapCodec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name),
                new StatEntry.Type<>(codec));
    }

    /** 按注册名取对应的 {@link StatEntry.Type}（解码派发用），缺失返回 null。 */
    public static StatEntry.Type<?> byName(String name) {
        return REGISTRY.get(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name))
                .map(net.minecraft.core.Holder.Reference::value).orElse(null);
    }

    /** 取 {@link StatEntry.Type} 的注册名（编码派发用，写进 JSON 的 {@code type} 字段，裸 path，不含 modid）。 */
    public static String nameOf(StatEntry.Type<?> type) {
        Identifier id = REGISTRY.getKey(type);
        return id != null ? id.getPath() : "";
    }

    /** 按名字返回对应实现的解码 Codec（供 {@link StatEntry#MAP_CODEC} 派发）。 */
    public static MapCodec<StatEntry> codecFor(String name) {
        return (MapCodec<StatEntry>) byName(name).codec();
    }
}
