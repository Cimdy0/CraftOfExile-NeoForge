package com.cimdy.coe.data.codec.item.mod.core;
import com.cimdy.coe.data.codec.item.mod.core.impl.BaseMod;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.mod.core.impl.EssenceMod;
import com.cimdy.coe.data.codec.item.mod.core.impl.RuneMod;
import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;
import com.cimdy.coe.event.registry.RegisterEvent;

/**
 * {@link ModifierEntry} 的分发注册表（写法与 math 的 {@code MathExpressions} / stat 的 {@code StatEntries} 完全一致）：

 * 每个可序列化臂（{@link CommonMod} / {@link RuneMod} / {@link EssenceMod} / {@link BaseMod}）在 {@link #REGISTRY}
 * 登记一个 {@link ModifierEntry.Type}，JSON 按 {@code type} 字段（注册表名 path）经
 * {@link ModifierEntry#MAP_CODEC} 派发到对应 Codec。
 *
 * <p>注册本身在 {@code RegisterEvent#NewRegistryEvent} 中完成（与 {@code StatEntries} 同处一处），
 * 否则首次取 Type 时（如 datagen dump 阶段）会因注册表尚未装载而抛 {@code IllegalStateException}。</p>
 */
public final class ModifierEntries {

    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "modifier_entry_type");
    public static final ResourceKey<Registry<ModifierEntry.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<ModifierEntry.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(256).create();

    public static final ModifierEntry.Type<CommonMod> COMMON = register("name", CommonMod.MAP_CODEC);
    public static final ModifierEntry.Type<RuneMod> RUNE = register("rune", RuneMod.MAP_CODEC);
    public static final ModifierEntry.Type<EssenceMod> ESSENCE = register("essence", EssenceMod.MAP_CODEC);
    public static final ModifierEntry.Type<BaseMod> BASE = register("base", BaseMod.MAP_CODEC);

    private static <T extends ModifierEntry> ModifierEntry.Type<T> register(String name, MapCodec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name),
                new ModifierEntry.Type<>(codec));
    }

    /** 按注册名取对应的 {@link ModifierEntry.Type}（解码派发用），缺失返回 null。 */
    public static ModifierEntry.Type<?> byName(String name) {
        return REGISTRY.get(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name))
                .map(net.minecraft.core.Holder.Reference::value).orElse(null);
    }

    /** 取 {@link ModifierEntry.Type} 的注册名（编码派发用，写进 JSON 的 {@code type} 字段，裸 path，不含 modid）。 */
    public static String nameOf(ModifierEntry.Type<?> type) {
        Identifier id = REGISTRY.getKey(type);
        return id != null ? id.getPath() : "";
    }

    /** 按名字返回对应臂的解码 MapCodec（供 {@link ModifierEntry#MAP_CODEC} 派发）。 */
    public static MapCodec<ModifierEntry> codecFor(String name) {
        return (MapCodec<ModifierEntry>) byName(name).codec();
    }
}
