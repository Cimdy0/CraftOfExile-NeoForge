package com.cimdy.coe.data.codec.support.addition;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实体的标量附加数据容器。
 *
 * <p>分三类存储，避免把布尔 / 整数 / 字符串都塞进同一个 {@code Map<String, Double>}：
 * <ul>
 *   <li>{@link #flags()} —— 布尔标记（{@code true/false}），取代 {@code 1.0/0.0} 的隐式约定；</li>
 *   <li>{@link #strings()} —— 字符串（标签 / 名称 / 序列化标识等）；</li>
 *   <li>{@link #attribute()} —— 属性覆盖（{@code Pair<Holder<Attribute>, Double>}）。</li>
 * </ul>
 *
 * <p>新增的 {@code flags} / {@code strings} 在 CODEC 中为可选字段（默认空 Map），
 * 故约 30 处旧调用方无需改动。
 */
public record AdditionData(
        Map<String, Double> data,
        List<Pair<Holder<Attribute>, Double>> attribute,
        Map<String, Boolean> flags,
        Map<String, String> strings
) {
    public static final AdditionData EMPTY = new AdditionData(Map.of(), List.of(), Map.of(), Map.of());

    public static Codec<Map<String, Double>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE);
    public static Codec<Map<String, Boolean>> FLAG_CODEC = Codec.unboundedMap(Codec.STRING, Codec.BOOL);
    public static Codec<Map<String, String>> STRING_CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING);
    private static final Codec<List<Pair<Holder<Attribute>, Double>>> LIST_PAIR_CODEC =
            Codec.pair(Attribute.CODEC.fieldOf("id").codec(),
                    Codec.DOUBLE.fieldOf("value").codec()).listOf();
    public static final Codec<AdditionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MAP_CODEC.optionalFieldOf("data", Map.of()).forGetter(AdditionData::data),
            LIST_PAIR_CODEC.optionalFieldOf("attribute", List.of()).forGetter(AdditionData::attribute),
            FLAG_CODEC.optionalFieldOf("flags", Map.of()).forGetter(AdditionData::flags),
            STRING_CODEC.optionalFieldOf("strings", Map.of()).forGetter(AdditionData::strings)
    ).apply(instance, AdditionData::new));

    public AdditionData {
        data = Map.copyOf(data);
        attribute = List.copyOf(attribute);
        flags = Map.copyOf(flags);
        strings = Map.copyOf(strings);
    }

    public static void set(IAttachmentHolder entity, AdditionData data) {
        entity.setData(AttachmentRegistry.ADDITION_DATA, data);
    }

    public static double getOrDefault(IAttachmentHolder entity, String key, double defaultValue) {
        return entity.hasData(AttachmentRegistry.ADDITION_DATA)
                ? entity.getData(AttachmentRegistry.ADDITION_DATA).data().getOrDefault(key, defaultValue)
                : defaultValue;
    }

    public static boolean has(IAttachmentHolder entity, String key) {
        return entity.hasData(AttachmentRegistry.ADDITION_DATA)
                && entity.getData(AttachmentRegistry.ADDITION_DATA).data().containsKey(key);
    }

    public static double getAttributeValue(IAttachmentHolder entity, String key, double defaultValue) {
        return entity.hasData(AttachmentRegistry.ADDITION_DATA)
                ? entity.getData(AttachmentRegistry.ADDITION_DATA).data().getOrDefault(key, defaultValue)
                : defaultValue;
    }

    public static double getAttributeValue(IAttachmentHolder entity, Holder<Attribute> attribute, double defaultValue) {
        return entity.hasData(AttachmentRegistry.ADDITION_DATA)
                ? entity.getData(AttachmentRegistry.ADDITION_DATA).attribute().stream()
                .filter(pair -> pair.getFirst().is(attribute))
                .map(Pair::getSecond)
                .findFirst()
                .orElse(defaultValue)
                : defaultValue;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 用持有者当前属性快照刷新 {@code attribute} 段，并保留 {@code data / flags / strings}。
     * 须在修改 {@link AdditionData} 后调用以同步属性覆盖。
     */
    public static void applyOwnerAttribute(IAttachmentHolder entity, LivingEntity living) {
        AdditionData.Builder build = entity.hasData(AttachmentRegistry.ADDITION_DATA) ?
                AdditionData.builder()
                        .setData(entity.getData(AttachmentRegistry.ADDITION_DATA).data)
                        .setAttribute(entity.getData(AttachmentRegistry.ADDITION_DATA).attribute)
                        .setFlags(entity.getData(AttachmentRegistry.ADDITION_DATA).flags)
                        .setStrings(entity.getData(AttachmentRegistry.ADDITION_DATA).strings)
                : AdditionData.builder();
        entity.setData(AttachmentRegistry.ADDITION_DATA,
                build.applyOwnerAttribute(living).build());
    }

    public static void addOrReplaceData(IAttachmentHolder entity, String key, double value) {
        AdditionData.Builder build = entity.hasData(AttachmentRegistry.ADDITION_DATA) ?
                AdditionData.builder().setData(entity.getData(AttachmentRegistry.ADDITION_DATA).data)
                        .setAttribute(entity.getData(AttachmentRegistry.ADDITION_DATA).attribute)
                        .setFlags(entity.getData(AttachmentRegistry.ADDITION_DATA).flags)
                        .setStrings(entity.getData(AttachmentRegistry.ADDITION_DATA).strings)
                : AdditionData.builder();
        entity.setData(AttachmentRegistry.ADDITION_DATA,
                build.apply(key, value).build());
    }

    public static void remove(Entity entity, String key) {
        if (entity.hasData(AttachmentRegistry.ADDITION_DATA)) {
            AdditionData original = entity.getData(AttachmentRegistry.ADDITION_DATA);
            Map<String, Double> mutableData = new HashMap<>(original.data());
            mutableData.remove(key);
            entity.setData(AttachmentRegistry.ADDITION_DATA,
                    new AdditionData(mutableData, original.attribute(), original.flags(), original.strings()));
        }
    }

    public static AdditionData get(LivingEntity living, IAttachmentHolder directEntity) {
        if (directEntity != null && directEntity.hasData(AttachmentRegistry.ADDITION_DATA)) {
            return directEntity.getData(AttachmentRegistry.ADDITION_DATA);
        } else {
            return AdditionData.builder().applyOwnerAttribute(living).build();
        }
    }

    // ==================== 布尔标记（flags） ====================

    /** 读取布尔标记，缺省返回 {@code defaultValue}。 */
    public static boolean getFlag(IAttachmentHolder entity, String key, boolean defaultValue) {
        return entity.hasData(AttachmentRegistry.ADDITION_DATA)
                ? entity.getData(AttachmentRegistry.ADDITION_DATA).flags().getOrDefault(key, defaultValue)
                : defaultValue;
    }

    // ==================== 字符串（strings） ====================

    /** 读取字符串，缺省返回 {@code defaultValue}。 */
    public static String getString(IAttachmentHolder entity, String key, String defaultValue) {
        return entity.hasData(AttachmentRegistry.ADDITION_DATA)
                ? entity.getData(AttachmentRegistry.ADDITION_DATA).strings().getOrDefault(key, defaultValue)
                : defaultValue;
    }

    // ==================== 实例访问器 ====================

    public double getAttributeValue(Holder<Attribute> attribute) {
        return this.attribute().stream()
                .filter(pair -> pair.getFirst().is(attribute))
                .map(Pair::getSecond)
                .findFirst()
                .orElse(attribute.value().getDefaultValue());
    }

    public boolean getFlag(String key, boolean defaultValue) {
        return this.flags().getOrDefault(key, defaultValue);
    }

    public String getString(String key, String defaultValue) {
        return this.strings().getOrDefault(key, defaultValue);
    }

    public static class Builder {
        private Map<String, Double> data = new HashMap<>();
        private List<Pair<Holder<Attribute>, Double>> attribute = new ArrayList<>();
        private Map<String, Boolean> flags = new HashMap<>();
        private Map<String, String> strings = new HashMap<>();

        public Builder setData(Map<String, Double> data) {
            this.data = new HashMap<>(data);
            return this;
        }

        public Builder setAttribute(List<Pair<Holder<Attribute>, Double>> attribute) {
            this.attribute = new ArrayList<>(attribute);
            return this;
        }

        public Builder setFlags(Map<String, Boolean> flags) {
            this.flags = new HashMap<>(flags);
            return this;
        }

        public Builder setStrings(Map<String, String> strings) {
            this.strings = new HashMap<>(strings);
            return this;
        }

        public Builder apply(String key, double value) {
            this.data.put(key, value);
            return this;
        }

        public Builder apply(Holder<Attribute> attribute, double value) {
            this.attribute.add(Pair.of(attribute, value));
            return this;
        }

        public Builder applyFlag(String key, boolean value) {
            this.flags.put(key, value);
            return this;
        }

        public Builder applyString(String key, String value) {
            this.strings.put(key, value);
            return this;
        }

        public AdditionData build() {
            return new AdditionData(data, attribute, flags, strings); // 构造时会转为不可变集合
        }

        public Builder applyOwnerAttribute(LivingEntity owner) {
            BuiltInRegistries.ATTRIBUTE.forEach(attribute -> attribute.setSyncable(true));
            for (AttributeInstance instance : owner.getAttributes().getSyncableAttributes()) {
                Holder<Attribute> holder = instance.getAttribute();
                this.apply(holder, owner.getAttributeValue(holder));
            }
            return this;
        }
    }
}
