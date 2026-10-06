package com.cimdy.coe.data.codec.skill.core;

import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * {@link SkillEntry} 的分发注册表（写法与 Stat 侧的 {@code StatEntries} 对齐）：
 * 每个形态（内联 {@link Skill} = 一个完整技能定义；引用 {@link SkillRead} = 从预设构造）
 * 在 {@link #REGISTRY} 登记一个 {@link SkillEntry.Type}，JSON 按 {@code entry} 字段
 * （注册表名）派发到对应 Codec。
 *
 * <p><b>派发键为何是 {@code entry}</b>：与 Stat 侧同一取舍——阶段树内 {@code delivery} /
 * {@code action} 的子类型已占用 {@code type} 语义；而历史 246 个技能 JSON 顶层从未出现
 * {@code entry}，故它对既有数据是「新增可选键」，不会撞字段。
 */
public final class SkillEntries {

    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "skill_entry_type");
    public static final ResourceKey<Registry<SkillEntry.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<SkillEntry.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(256).create();

    public static final SkillEntry.Type<Skill> INLINE = register("inline", Skill.MAP_CODEC);
    public static final SkillEntry.Type<SkillRead> READ = register("read", SkillRead.MAP_CODEC);

    private static <T extends SkillEntry> SkillEntry.Type<T> register(String name, MapCodec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name),
                new SkillEntry.Type<>(codec));
    }

    /** 按注册名取对应的 {@link SkillEntry.Type}（解码派发用），缺失返回 null。 */
    public static SkillEntry.Type<?> byName(String name) {
        return REGISTRY.get(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name))
                .map(net.minecraft.core.Holder.Reference::value).orElse(null);
    }

    /** 取 {@link SkillEntry.Type} 的注册名（编码派发用，写进 JSON 的 {@code entry} 字段，裸 path）。 */
    public static String nameOf(SkillEntry.Type<?> type) {
        Identifier id = REGISTRY.getKey(type);
        return id != null ? id.getPath() : "";
    }

    /** 按名字返回对应实现的解码 Codec（供 {@link SkillEntry#MAP_CODEC} 派发）。 */
    public static MapCodec<SkillEntry> codecFor(String name) {
        SkillEntry.Type<?> type = byName(name);
        return type == null ? null : (MapCodec<SkillEntry>) type.codec();
    }
}
