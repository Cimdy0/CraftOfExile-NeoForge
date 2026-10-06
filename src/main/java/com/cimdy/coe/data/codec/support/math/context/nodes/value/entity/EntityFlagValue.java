package com.cimdy.coe.data.codec.support.math.context.nodes.value.entity;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;

import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Entity flag read-node (boolean hit -> 1 / else 0). Replaces the old EntityCondition.
 * <p>
 * Read-only pure function, consumed by the Stat fold channel (reads Context, never mutates it).
 * <p>
 * Type/biome/dimension checks are carried as registry-instance-agnostic keys
 * ({@link ResourceKey} for direct entries, {@link TagKey} for tags, {@link ResourceKey} for the dimension),
 * with no string parsing on the hot path and — crucially — no binding to a specific registry instance.
 * This lets the same {@link Codec} serialize identically under the datagen lookup snapshot and the live
 * server registry, avoiding the "Element Reference ... is not valid in current registry set" failure that
 * {@code HolderSet}-backed codecs hit when the holder's registry differs from the encoding {@code RegistryOps}.
 * <p>
 * Besides the fold channel, {@link #matches(Entity)} doubles as the standalone entity predicate used by
 * the direct-damage conversion table (which keys on projectile entity types).
 */
public record EntityFlagValue(
        Optional<List<ResourceKey<EntityType<?>>>> types,
        Optional<List<TagKey<EntityType<?>>>> typeTags,
        Optional<List<ResourceKey<Biome>>> biomes,
        Optional<List<TagKey<Biome>>> biomeTags,
        Optional<ResourceKey<Level>> dimension,
        Optional<Flag> flag
) implements EntityNode {

    /**
     * 懒初始化编解码器：构造链会触发 {@code AttachmentHolder} 等需要 FML Loader 的类静态初始化，
     * 若作为 {@code static final} 字段在类加载时立即构造，纯 JUnit 环境会因
     * {@code IllegalStateException: There is no current FML Loader} 导致整个 {@code EntityFlagValue} 类无法加载。
     * 改为惰性 holder（首次访问时才构造），与运行期解耦，datagen / 序列化路径照常可用。
     */
    public static Codec<EntityFlagValue> codec() {
        return CodecHolder.CODEC;
    }

    private static final class CodecHolder {
        private static final Codec<EntityFlagValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.ENTITY_TYPE).listOf().optionalFieldOf("types").forGetter(EntityFlagValue::types),
                TagKey.codec(Registries.ENTITY_TYPE).listOf().optionalFieldOf("type_tags").forGetter(EntityFlagValue::typeTags),
                ResourceKey.codec(Registries.BIOME).listOf().optionalFieldOf("biomes").forGetter(EntityFlagValue::biomes),
                TagKey.codec(Registries.BIOME).listOf().optionalFieldOf("biome_tags").forGetter(EntityFlagValue::biomeTags),
                Level.RESOURCE_KEY_CODEC.optionalFieldOf("dimension").forGetter(EntityFlagValue::dimension),
                Flag.CODEC.optionalFieldOf("flag").forGetter(EntityFlagValue::flag)
        ).apply(instance, EntityFlagValue::new));
    }

    public static final EntityFlagValue EMPTY =
            new EntityFlagValue(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    // ===== type-safe factories (datagen / data-driven) =====
    // 实体 / 生物群系以 ResourceKey / TagKey 存储（注册表实例无关），编解码不再依赖任意具体注册表实例，
    // datagen（lookup 快照）与运行期（活服务器注册表）可一致序列化 / 反序列化。

    public static EntityFlagValue type(EntityType<?> entityType) {
        return new EntityFlagValue(Optional.of(List.of(entityType.builtInRegistryHolder().key())),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static EntityFlagValue type(HolderGetter<EntityType<?>> lookup, TagKey<EntityType<?>> entityTag) {
        lookup.getOrThrow(entityTag);
        return new EntityFlagValue(Optional.empty(),
                Optional.of(List.of(entityTag)), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static EntityFlagValue biome(Holder<Biome> biome) {
        return new EntityFlagValue(Optional.empty(), Optional.empty(),
                Optional.of(List.of(biome.unwrapKey().orElseThrow())),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static EntityFlagValue biome(HolderGetter<Biome> lookup, TagKey<Biome> biomeTag) {
        lookup.getOrThrow(biomeTag);
        return new EntityFlagValue(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(List.of(biomeTag)), Optional.empty(), Optional.empty());
    }

    public static EntityFlagValue dimension(ResourceKey<Level> dimension) {
        return new EntityFlagValue(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(dimension), Optional.empty());
    }

    public static EntityFlagValue summoned() {
        return new EntityFlagValue(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(Flag.SUMMONED));
    }

    public static EntityFlagValue owner() {
        return new EntityFlagValue(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(Flag.OWNER));
    }

    public static EntityFlagValue level() {
        return new EntityFlagValue(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(Flag.LEVEL));
    }

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        return flag.map(value -> switch (value) {
            case SUMMONED -> AdditionData.getOrDefault(living, AdditionKeys.SUMMONED, 0.0) != 0.0 ? 1D : 0D;
            case OWNER -> living.getPersistentData().contains(OWNER_KEY) ? 1D : 0D;
            case LEVEL -> living.getPersistentData().getDouble(LEVEL_KEY).orElse(0D) >= 1D ? 1D : 0D;
        }).orElseGet(() -> matches(living) ? 1D : 0D);
    }

    /**
     * Registry-keyed type/biome/dimension match. Accepts any {@link Entity} (not just
     * {@link LivingEntity}) so direct-damage sources such as projectiles can be matched too.
     */
    public boolean matches(Entity entity) {
        if (types.isPresent()) {
            ResourceKey<EntityType<?>> key = entity.getType().builtInRegistryHolder().key();
            if (!types.get().contains(key)) return false;
        }
        if (typeTags.isPresent()) {
            Holder<EntityType<?>> eh = entity.getType().builtInRegistryHolder();
            if (typeTags.get().stream().noneMatch(eh::is)) return false;
        }
        Level level = entity.level();
        if (biomes.isPresent()) {
            Optional<ResourceKey<Biome>> bkey = level.getBiome(entity.blockPosition()).unwrapKey();
            if (bkey.isEmpty() || !biomes.get().contains(bkey.get())) return false;
        }
        if (biomeTags.isPresent()) {
            Holder<Biome> bh = level.getBiome(entity.blockPosition());
            if (biomeTags.get().stream().noneMatch(bh::is)) return false;
        }
        return dimension.isEmpty() || level.dimension() == dimension.get();
    }

    // ===== persistent keys (consistent with the Execution writer side) =====
    private static final String LEVEL_KEY = "coe:level";
    private static final String OWNER_KEY = "coe:owner";

    public enum Flag implements StringRepresentable {
        SUMMONED("summoned"),
        OWNER("owner"),
        LEVEL("level");

        private final String name;

        Flag(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public static final Codec<Flag> CODEC = StringRepresentable.fromEnum(Flag::values);
    }

    /**
     * 条件从句（P2/A）：把任意已填的「类型 / 生物群系 / 维度 / 标记」字段渲染成自包含分句，
     * 多个字段以 {@code stat.condition.join_and}（" and "）连接，单字段则直接返回该分句。
     * 各字段复用权威键（entity / biome / dimension 的 vanilla 描述键）或既有模板键（entity_type），
     * 标记（Flag）无权威键故铸 {@code stat.value.flag.<name>}。
     *
     * <p>降级铁律：全部字段为空时回落 {@code stat.condition.unknown}（可见 "[!] condition"），不静默丢弃。
     */
    @Override
    public List<Component> describeFragment() {
        List<Component> clauses = new ArrayList<>();
        types.ifPresent(ts -> {
            Component c = entityDirectClause(ts);
            if (c != null) clauses.add(c);
        });
        typeTags.ifPresent(tags -> {
            Component c = tagClause("entity_type", tags);
            if (c != null) clauses.add(c);
        });
        biomes.ifPresent(bs -> {
            Component c = biomeDirectClause(bs);
            if (c != null) clauses.add(c);
        });
        biomeTags.ifPresent(tags -> {
            Component c = tagClause("biome", tags);
            if (c != null) clauses.add(c);
        });
        dimension.ifPresent(d -> {
            Component c = dimensionClause(d);
            if (c != null) clauses.add(c);
        });
        flag.ifPresent(f -> {
            Component c = flagClause(f);
            if (c != null) clauses.add(c);
        });
        if (clauses.isEmpty()) return List.of(Component.translatable("stat.condition.unknown"));
        Component clause = clauses.get(0);
        for (int i = 1; i < clauses.size(); i++) {
            clause = clause.copy().append(Component.translatable("stat.condition.join_and")).append(clauses.get(i));
        }
        return List.of(clause);
    }

    /** 直接实体分句：逐个按 ResourceKey 取权威描述键。 */
    private Component entityDirectClause(List<ResourceKey<EntityType<?>>> ts) {
        List<Component> parts = new ArrayList<>();
        for (ResourceKey<EntityType<?>> k : ts) {
            BuiltInRegistries.ENTITY_TYPE.get(k).ifPresent(et -> {
                String desc = et.value().getDescriptionId();
                parts.add(StatValueKey.ofKey(desc, idOf(desc)));
            });
        }
        Component phrase = joinPhrases(parts);
        return phrase == null ? null : Component.translatable("stat.condition.entity_type", phrase);
    }

    /** 直接生物群系分句：逐个按 ResourceKey 取 location。 */
    private Component biomeDirectClause(List<ResourceKey<Biome>> bs) {
        List<Component> parts = new ArrayList<>();
        for (ResourceKey<Biome> k : bs) {
            Identifier loc = k.identifier();
            parts.add(StatValueKey.ofKey("biome." + loc.toLanguageKey(), loc.getPath()));
        }
        Component phrase = joinPhrases(parts);
        return phrase == null ? null : Component.translatable("stat.condition.biome", phrase);
    }

    /** 标签分句（实体 / 生物群系共用）：复用 {@code stat.value.<type>.<path>}。 */
    private <T> Component tagClause(String type, List<TagKey<T>> tags) {
        List<Component> parts = new ArrayList<>();
        for (TagKey<T> t : tags) {
            parts.add(StatValueKey.of(type, t.location().getPath()));
        }
        Component phrase = joinPhrases(parts);
        return phrase == null ? null : Component.translatable("stat.condition." + type, phrase);
    }

    /** 维度分句：复用 vanilla {@code dimension.<ns>.<id>} 权威键。 */
    private Component dimensionClause(ResourceKey<Level> d) {
        Identifier loc = d.identifier();
        return Component.translatable("stat.condition.dimension",
                StatValueKey.ofKey("dimension." + loc.toLanguageKey(), loc.getPath()));
    }

    /** 标记分句（COE 专属，无权威键）：铸 {@code stat.value.flag.<name>}。 */
    private Component flagClause(Flag f) {
        return Component.translatable("stat.condition.flag", StatValueKey.of("flag", f.getSerializedName()));
    }

    /** 把多个短语以 {@code stat.condition.join_and} 连接为单一组件；空则返回 null。 */
    private static Component joinPhrases(List<Component> parts) {
        if (parts.isEmpty()) return null;
        Component out = parts.get(0);
        for (int i = 1; i < parts.size(); i++) {
            out = out.copy().append(Component.translatable("stat.condition.join_and")).append(parts.get(i));
        }
        return out;
    }

    /** 从权威描述键（如 {@code entity.minecraft.zombie}）取末段作为 humanize 回落 id。 */
    private static String idOf(String descId) {
        int i = descId.lastIndexOf('.');
        return i >= 0 ? descId.substring(i + 1) : descId;
    }
}
