package com.cimdy.coe.data.codec.item.mod.state;

import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.mod.value.ModValue;
import com.cimdy.coe.data.codec.item.ItemInstance;
import com.cimdy.coe.data.codec.item.ItemInstances;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 装备模板（数据驱动 / 三层字段表）：声明该类别装备拥有的
 * 「属性字段（stats）」「需求字段（reqs）」「技能字段（skills）」三类字段，
 * <b>每张表的键即关联符（局部命名空间 String key）</b>——局部词缀 / read 靠同名键 join 到对应字段（见 plan §4.5）。
 *
 * <p>表值：stats = 属性 StatEntry 的 ref；reqs = 需求种类；skills = 技能 ref。模板可 {@code extend} 另一模板（子覆盖父）。
 *
 * <p>JSON 形态（外层为 {@link ItemInstance} 派发键 {@code coe:item_template}）：
 * <pre>{@code
 * {
 *   "extend": "coe:armor",
 *   "stats":  { "armor": { "entry": "read", "ref": "coe:add_armor" }, ... },
 *   "reqs":   { "l": "level", "dex": "dexterity" },
 *   "skills": { "skill": "coe:preset_x" },
 *   "bases":  { "<关联符>": { "effect": {...} } }   // 基底词缀（AdditionType；双变量 $min/$max 在 effect 的 stat 里，值由 read 供）
 * }
 * }</pre>
 * read 用同样的键填具体值（{@link EquipmentRead}）。
 */
public record EquipmentTemplate(
        Optional<Identifier> extend,
        Map<String, StatEntry> stats,
        Map<String, RequireKind> reqs,
        Map<String, Identifier> skills,
        Map<String, AdditionType.Effect> bases
) implements ItemInstance {

    public static final Codec<EquipmentTemplate> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.optionalFieldOf("extend").forGetter(EquipmentTemplate::extend),
            Codec.unboundedMap(Codec.STRING, StatEntry.CODEC)
                    .optionalFieldOf("stats", Map.of()).forGetter(EquipmentTemplate::stats),
            Codec.unboundedMap(Codec.STRING, RequireKind.CODEC)
                    .optionalFieldOf("reqs", Map.of()).forGetter(EquipmentTemplate::reqs),
            Codec.unboundedMap(Codec.STRING, Identifier.CODEC)
                    .optionalFieldOf("skills", Map.of()).forGetter(EquipmentTemplate::skills),
            Codec.unboundedMap(Codec.STRING, AdditionType.Effect.CODEC)
                    .optionalFieldOf("bases", Map.of()).forGetter(EquipmentTemplate::bases)
    ).apply(i, EquipmentTemplate::new));

    @Override
    public ItemInstance.Type<?> getType() {
        return ItemInstances.EQUIPMENT_TEMPLATE;
    }

    /** 空模板（无字段、无 extend）。 */
    public static EquipmentTemplate empty() {
        return new EquipmentTemplate(Optional.empty(), Map.of(), Map.of(), Map.of(), Map.of());
    }

    /**
     * 按 read 注入的值（键 = 关联符）展开 stats：值代入被引用 stat 的参数槽（{@code $value}）后
     * 得<b>真实 Stat</b>（{@link StatEntry#withParams} —— StatRead 并入 vars / Stat 替换 params）。
     */
    public List<StatEntry> baseStatEntries(Map<String, ExpressionCodec.Value> values) {
        List<StatEntry> out = new ArrayList<>(stats.size());
        for (Map.Entry<String, StatEntry> e : stats.entrySet()) {
            ExpressionCodec.Value v = values.get(e.getKey());
            if (v == null) continue;
            out.add(e.getValue().withParams(Map.of("value", v)));
        }
        return out;
    }

    /** 按 read 注入的值（键 = 关联符）取需求值：键 → 数值（未填的键不出现）。 */
    public Map<String, Double> reqValues(Map<String, ExpressionCodec.Value> values) {
        Map<String, Double> out = new java.util.LinkedHashMap<>();
        for (String k : reqs.keySet()) {
            ExpressionCodec.Value v = values.get(k);
            if (v != null) out.put(k, v.value().left().orElse(0.0));
        }
        return out;
    }

    /** 基底词缀展开：把 read 供的 {@link ModValue}（当前值 min / 范围上限 max）注入 effect 的数值槽，得真实 Stat。 */
    public static List<StatEntry> baseEntries(AdditionType.Effect effect, ModValue value) {
        ExpressionCodec.Value lo = value.min();
        ExpressionCodec.Value hi = value.max().orElse(value.min());
        if (lo.equals(hi)) {
            return List.of(effect.stat().withParams(Map.of(effect.valueVar().orElse("value"), lo)));
        }
        return List.of(effect.stat().withParams(Map.of("min", lo, "max", hi)));
    }

    /** 需求种类（字符串编解码，小写）。 */
    public enum RequireKind {
        LEVEL, STRENGTH, DEXTERITY, INTELLIGENCE;

        public static final Codec<RequireKind> CODEC = Codec.STRING.xmap(
                s -> RequireKind.valueOf(s.trim().toUpperCase(Locale.ROOT)),
                k -> k.name().toLowerCase(Locale.ROOT));
    }
    /** 流式建造器：以 extend 起手（{@link #builder()} 无 extend），逐字段声明，最终 {@link #build()} 生成 EquipmentTemplate。 */
    public static Builder builder(Identifier extend) { return new Builder(Optional.of(extend)); }
    public static Builder builder() { return new Builder(Optional.empty()); }

    public static final class Builder {
        private final Optional<Identifier> extend;
        private Map<String, StatEntry> stats = new LinkedHashMap<>();
        private Map<String, RequireKind> reqs = new LinkedHashMap<>();
        private Map<String, Identifier> skills = new LinkedHashMap<>();
        private Map<String, AdditionType.Effect> bases = new LinkedHashMap<>();

        private Builder(Optional<Identifier> extend) { this.extend = extend; }

        public Builder stat(String key, StatEntry e) { stats.put(key, e); return this; }
        public Builder req(String key, RequireKind k) { reqs.put(key, k); return this; }
        public Builder skill(String key, Identifier id) { skills.put(key, id); return this; }
        public Builder base(String key, AdditionType.Effect e) { bases.put(key, e); return this; }

        public Builder stats(Map<String, StatEntry> m) { stats.putAll(m); return this; }
        public Builder reqs(Map<String, RequireKind> m) { reqs.putAll(m); return this; }
        public Builder skills(Map<String, Identifier> m) { skills.putAll(m); return this; }
        public Builder bases(Map<String, AdditionType.Effect> m) { bases.putAll(m); return this; }

        public EquipmentTemplate build() {
            return new EquipmentTemplate(extend, Map.copyOf(stats), Map.copyOf(reqs), Map.copyOf(skills), Map.copyOf(bases));
        }
    }

}
