package com.cimdy.coe.data.codec.support.attribute;

import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;

public record AttributeLimiters(List<AttributeLimiters.Limiter> limiters) {
    public static final AttributeLimiters EMPTY = new AttributeLimiters(List.of());
    public static final Codec<AttributeLimiters> CODEC =
            AttributeLimiters.Limiter.CODEC.listOf().xmap(AttributeLimiters::new, AttributeLimiters::limiters);

    public AttributeLimiters {
        limiters = List.copyOf(limiters);
    }

    // 缓存按实例记忆化：limiters 是不可变记录，每次 add/remove 都会产生新实例，
    // 因此以实例为 key 缓存。limiters 变更后旧实例不再被持有，会被 GC 回收，
    // WeakHashMap 自动释放对应缓存，不会造成内存泄漏。
    private static final Map<AttributeLimiters, Cache> CACHE = new WeakHashMap<>();

    public Cache getCache() {
        Cache cache = CACHE.get(this);
        if (cache == null) {
            synchronized (CACHE) {
                cache = CACHE.get(this);
                if (cache == null) {
                    cache = new Cache(limiters);
                    CACHE.put(this, cache);
                }
            }
        }
        return cache;
    }

    public static final class Cache {
        private final Map<Holder<Attribute>, LimiterMap> limiterMapCache = new ConcurrentHashMap<>();

        public Cache(List<AttributeLimiters.Limiter> limiters) {
            rebuildCache(limiters);
        }

        private void rebuildCache(List<AttributeLimiters.Limiter> limiters) {
            limiterMapCache.clear();
            for (Limiter limiter : limiters) {
                // transient 与 persistent 都只影响最终取值，合并进同一张表即可。
                limiterMapCache.put(limiter.attribute(),
                        new LimiterMap(buildOperationMap(limiter.transients(), limiter.persistents())));
            }
        }

        private static Map<Operation, List<Modifier>> buildOperationMap(List<Modifier> transients, List<Modifier> persistents) {
            List<Modifier> all = new ArrayList<>(transients.size() + persistents.size());
            all.addAll(transients);
            all.addAll(persistents);
            return buildOperationMap(all);
        }

        private static Map<Operation, List<Modifier>> buildOperationMap(List<Modifier> modifiers) {
            Map<Operation, List<Modifier>> map = new EnumMap<>(Operation.class);
            for (Operation op : Operation.values()) {
                map.put(op, new ArrayList<>());
            }
            for (Modifier m : modifiers) {
                map.get(m.operation()).add(m);
            }
            for (Operation op : Operation.values()) {
                map.put(op, List.copyOf(map.get(op)));
            }
            return map;
        }

        public double clampValue(Holder<Attribute> attribute, double value) {
            LimiterMap limiterMap = limiterMapCache.getOrDefault(attribute, LimiterMap.EMPTY);
            Map<Operation, List<Modifier>> modifiers = limiterMap.modifiers();

            double min = modifiers.getOrDefault(Operation.SET_MIN, List.of()).stream()
                    .mapToDouble(Modifier::amount)
                    .min()
                    .orElse(Double.NaN);

            double max = modifiers.getOrDefault(Operation.SET_MAX, List.of()).stream()
                    .mapToDouble(Modifier::amount)
                    .max()
                    .orElse(Double.NaN);

            double setValue = modifiers.getOrDefault(Operation.SET_VALUE, List.of())
                    .stream()
                    .reduce((first, second) -> second)
                    .map(Modifier::amount)
                    .orElse(Double.NaN);

            double result = value;
            if (!Double.isNaN(min)) result = Math.max(min, result);
            if (!Double.isNaN(max)) result = Math.min(max, result);
            if (!Double.isNaN(setValue)) result = setValue;

            return attribute.value().sanitizeValue(result);
        }
    }

    public record LimiterMap(Map<Operation, List<Modifier>> modifiers) {
        private static final LimiterMap EMPTY = new LimiterMap(Map.of());

        public LimiterMap(Map<Operation, List<Modifier>> modifiers){
            this.modifiers = modifiers;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Limiter get(Holder<Attribute> attribute) {
        return limiters.stream().filter(limiter -> limiter.attribute().equals(attribute)).findFirst().orElse(null);
    }

    private List<Limiter> createNewLimiters() {
        return new ArrayList<>(this.limiters);
    }

    private AttributeLimiters updateLimiter(Holder<Attribute> attribute, Limiter newLimiter) {
        List<Limiter> newLimiters = createNewLimiters();
        newLimiters.removeIf(l -> l.attribute().equals(attribute));
        newLimiters.add(newLimiter);
        return new AttributeLimiters(List.copyOf(newLimiters));
    }

    public AttributeLimiters addTransientModifier(Holder<Attribute> attribute, Modifier modifier) {
        Limiter existing = get(attribute);
        List<Modifier> newTransients = existing != null ?
                new ArrayList<>(existing.transients()) : new ArrayList<>();
        newTransients.removeIf(m -> m.id().equals(modifier.id()));
        newTransients.add(modifier);
        return updateLimiter(attribute, new Limiter(
                attribute,
                List.copyOf(newTransients),
                existing != null ? existing.persistents() : List.of()
        ));
    }

    public AttributeLimiters addPersistentModifier(Holder<Attribute> attribute, Modifier modifier) {
        Limiter existing = get(attribute);
        List<Modifier> newPersistents = existing != null ?
                new ArrayList<>(existing.persistents()) : new ArrayList<>();
        newPersistents.removeIf(m -> m.id().equals(modifier.id()));
        newPersistents.add(modifier);
        return updateLimiter(attribute, new Limiter(
                attribute,
                existing != null ? existing.transients() : List.of(),
                List.copyOf(newPersistents)
        ));
    }

    public AttributeLimiters removeModifier(Holder<Attribute> attribute, Identifier modifierId) {
        Limiter existing = get(attribute);
        if (existing == null) return this;

        List<Modifier> newTransients = new ArrayList<>(existing.transients());
        List<Modifier> newPersistents = new ArrayList<>(existing.persistents());
        newTransients.removeIf(m -> m.id().equals(modifierId));
        newPersistents.removeIf(m -> m.id().equals(modifierId));

        if (newTransients.isEmpty() && newPersistents.isEmpty()) {
            List<Limiter> newLimiters = createNewLimiters();
            newLimiters.removeIf(l -> l.attribute().equals(attribute));
            return new AttributeLimiters(List.copyOf(newLimiters));
        }
        return updateLimiter(attribute, new Limiter(
                attribute,
                List.copyOf(newTransients),
                List.copyOf(newPersistents)
        ));
    }

    public static final class Builder {
        private final List<Limiter> limiters = new ArrayList<>();

        public Builder addLimiter(Limiter limiter) {
            limiters.add(limiter);
            return this;
        }

        public Builder removeLimiter(Holder<Attribute> attribute) {
            limiters.removeIf(l -> l.attribute().equals(attribute));
            return this;
        }

        public Builder addPersistentModifier(Holder<Attribute> attribute, Modifier modifier) {
            Limiter existing = limiters.stream()
                    .filter(l -> l.attribute().equals(attribute))
                    .findFirst()
                    .orElse(null);

            List<Modifier> newPersistents = existing != null ?
                    new ArrayList<>(existing.persistents()) : new ArrayList<>();
            newPersistents.removeIf(m -> m.id().equals(modifier.id()));
            newPersistents.add(modifier);

            List<Modifier> transients = existing != null ? existing.transients() : List.of();
            Limiter newLimiter = new Limiter(attribute, transients, List.copyOf(newPersistents));

            if (existing != null) {
                limiters.remove(existing);
            }
            limiters.add(newLimiter);
            return this;
        }

        public AttributeLimiters build() {
            return new AttributeLimiters(List.copyOf(limiters));
        }
    }

    public record Limiter(Holder<Attribute> attribute, List<Modifier> transients, List<Modifier> persistents) {
        public static final Codec<Limiter> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Attribute.CODEC.fieldOf("attribute").forGetter(Limiter::attribute),
                        Modifier.CODEC.listOf().fieldOf("transients").forGetter(Limiter::transients),
                        Modifier.CODEC.listOf().fieldOf("persistents").forGetter(Limiter::persistents)
                ).apply(instance, Limiter::new));
    }

    public record Modifier(Identifier id, double amount, AttributeLimiters.Operation operation) {
        public static final Codec<Modifier> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Identifier.CODEC.fieldOf("id").forGetter(Modifier::id),
                        Codec.DOUBLE.fieldOf("amount").forGetter(Modifier::amount),
                        AttributeLimiters.Operation.CODEC.fieldOf("operation").forGetter(Modifier::operation)
                ).apply(instance, Modifier::new));
    }

    public record Save(Map<String, AttributeLimiters> limiter) implements CustomPacketPayload {
        public static final Type<Save> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "save"));
        public static final Save EMPTY = new Save(new HashMap<>());

        public static final Codec<Save> CODEC = Codec.unboundedMap(Codec.STRING, AttributeLimiters.CODEC)
                .xmap(Save::new, Save::limiter);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public enum Operation implements StringRepresentable {
        SET_MIN("set_min", 0),
        SET_MAX("set_max", 1),
        SET_VALUE("set_value", 2);

        public static final IntFunction<Operation> BY_ID =
                ByIdMap.continuous(Operation::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final Codec<Operation> CODEC =
                StringRepresentable.fromEnum(Operation::values);
        private final String name;
        private final int id;

        Operation(String name, int value) {
            this.name = name;
            this.id = value;
        }

        public int getId() {
            return this.id;
        }

        public String getSerializedName() {
            return this.name;
        }
    }
}