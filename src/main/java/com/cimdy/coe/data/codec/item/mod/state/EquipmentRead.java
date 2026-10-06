package com.cimdy.coe.data.codec.item.mod.state;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.mod.value.ModValue;
import com.cimdy.coe.data.codec.item.ItemInstance;
import com.cimdy.coe.data.codec.item.ItemInstances;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.helper.skill.EquipmentSkillHelper;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.helper.item.QualityOps;
import com.cimdy.coe.event.data.LoadEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 具体装备读取（数据形态，实现 {@link ItemInstance} 由数据包直接加载）：
 * 三张与模板同键（<b>关联符</b>）的表 —— {@code stats} / {@code reqs} / {@code skills}（值换成<b>具体数值</b> / 技能 ref），
 * 外加可选 {@code ref}（引用一个 {@link EquipmentTemplate}，可基于它再补值、加新键对）与 {@code bases}（基底 / implicit）。
 *
 * <p>JSON 形态（外层为派发键 {@code coe:item_equipment}）：
 * <pre>{@code
 * {
 *   "ref": "coe:weapon/main_hand/one_hand/axe/template/strength_dexterity",
 *   "stats":  { "armor": 51, "attack_crit": 1.0 },
 *   "reqs":   { "l": 12, "dex": 10 },
 *   "skills": { "skill": "coe:preset_x" },
 *   "template": { ... },  // 可选：read 再补的模板（延迟注册，最具体）
 *   "bases": { "<关联符>": { "min": 10, "max": 15 } }   // 基底词缀两值（同键绑定模板的 bases）
 * }
 * }</pre>
 * 展开：ref 指向的模板链（父→子）合并后，用本 read 的 values（键=关联符）逐字段展开为 StatRead / 需求值；
 * 模板未声明的新键按「键即 stat ref」处理（{@code coe:<key>}）。
 */
public record EquipmentRead(
        Optional<Identifier> ref,
        Optional<EquipmentTemplate> template,
        Map<String, ExpressionCodec.Value> stats,
        Map<String, ExpressionCodec.Value> reqs,
        Map<String, EquipmentSkillEntry> skills,
        Map<String, ModValue> bases
) implements ItemInstance {

    public EquipmentRead(Identifier ref) {
        this(Optional.of(ref), Optional.empty(), Map.of(), Map.of(), Map.of(), Map.of());
    }

    public EquipmentRead(Identifier ref, Map<String, ExpressionCodec.Value> stats) {
        this(Optional.of(ref), Optional.empty(), stats, Map.of(), Map.of(), Map.of());
    }

    public EquipmentRead(Identifier ref, Map<String, ExpressionCodec.Value> stats, Map<String, ExpressionCodec.Value> reqs) {
        this(Optional.of(ref), Optional.empty(), stats, reqs, Map.of(), Map.of());
    }

    /** 引用型 MapCodec：ref 可选，stats/reqs/skills/bases 均缺省为空。 */
    public static final MapCodec<EquipmentRead> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.optionalFieldOf("ref").forGetter(EquipmentRead::ref),
            EquipmentTemplate.CODEC.optionalFieldOf("template").forGetter(EquipmentRead::template),
            Codec.unboundedMap(Codec.STRING, ExpressionCodec.VALUE_CODEC)
                    .optionalFieldOf("stats", Map.of()).forGetter(EquipmentRead::stats),
            Codec.unboundedMap(Codec.STRING, ExpressionCodec.VALUE_CODEC)
                    .optionalFieldOf("reqs", Map.of()).forGetter(EquipmentRead::reqs),
            Codec.unboundedMap(Codec.STRING, EquipmentSkillEntry.CODEC)
                    .optionalFieldOf("skills", Map.of()).forGetter(EquipmentRead::skills),
            Codec.unboundedMap(Codec.STRING, ModValue.CODEC)
                    .optionalFieldOf("bases", Map.of()).forGetter(EquipmentRead::bases)
    ).apply(i, EquipmentRead::new));

    public static final Codec<EquipmentRead> CODEC = MAP_CODEC.codec();

    @Override
    public Type<?> getType() {
        return ItemInstances.EQUIPMENT_READ;
    }

    /** 从数据生成缓存解析本引用指向的 EquipmentTemplate（ref 缺省则空模板）。 */
    public EquipmentTemplate resolve() {
        if (ref.isEmpty()) return EquipmentTemplate.empty();
        EquipmentTemplate t = LoadEvent.EQUIPMENT_TEMPLATE_MAP.get(ref.get());
        if (t == null) {
            throw new IllegalStateException("EquipmentRead 引用了未注册的 EquipmentTemplate: " + ref.get());
        }
        return t;
    }

    /** 展开 extend 链（父→子覆盖）得到合并模板（键 = 关联符）。 */
    private EquipmentTemplate resolveMerged() {
        Map<String, StatEntry> statsMap = new LinkedHashMap<>();
        Map<String, EquipmentTemplate.RequireKind> reqMap = new LinkedHashMap<>();
        Map<String, Identifier> skillMap = new LinkedHashMap<>();
        Map<String, AdditionType.Effect> baseMap = new LinkedHashMap<>();
        Set<Identifier> seen = new HashSet<>();
        List<EquipmentTemplate> chain = new ArrayList<>();
        Identifier cur = ref.orElse(null);
        EquipmentTemplate t = cur == null ? null : LoadEvent.EQUIPMENT_TEMPLATE_MAP.get(cur);
        while (t != null && seen.add(cur)) {
            chain.add(t);
            Identifier parent = t.extend().orElse(null);
            if (parent == null || seen.contains(parent)) break;
            cur = parent;
            t = LoadEvent.EQUIPMENT_TEMPLATE_MAP.get(cur);
        }
        for (int i = chain.size() - 1; i >= 0; i--) {
            EquipmentTemplate tmpl = chain.get(i);
            statsMap.putAll(tmpl.stats());
            reqMap.putAll(tmpl.reqs());
            skillMap.putAll(tmpl.skills());
            baseMap.putAll(tmpl.bases());
        }
        // Read 再补的模板（延迟注册：仅在本 read 解析时并入，最具体、覆盖在前）
        template.ifPresent(inline -> {
            statsMap.putAll(inline.stats());
            reqMap.putAll(inline.reqs());
            skillMap.putAll(inline.skills());
            baseMap.putAll(inline.bases());
        });
        return new EquipmentTemplate(Optional.empty(), statsMap, reqMap, skillMap, baseMap);
    }

    /** read 的三张表键并集（含模板声明的键），供消费端判断「是否拥有某属性字段」。 */
    public boolean hasStat(String key) {
        return stats.containsKey(key) || resolveMerged().stats().containsKey(key);
    }

    /** 模板 + 本 read 结合解析 → <b>装备基础属性</b>（{@link EquipmentState.Base}）：只留「关联符 → 值」。 */
    public EquipmentState.Base base() {
        EquipmentTemplate merged = resolveMerged();
        Map<String, ExpressionCodec.Value> statsOut = new LinkedHashMap<>();
        for (String k : merged.stats().keySet()) {
            ExpressionCodec.Value v = stats.get(k);
            if (v != null) statsOut.put(k, v);
        }
        stats.forEach(statsOut::putIfAbsent);
        Map<String, ExpressionCodec.Value> reqsOut = new LinkedHashMap<>();
        for (String k : merged.reqs().keySet()) {
            ExpressionCodec.Value v = reqs.get(k);
            if (v != null) reqsOut.put(k, v);
        }
        reqs.forEach(reqsOut::putIfAbsent);
        Map<String, ModValue> implicitOut = new LinkedHashMap<>();
        for (String k : merged.bases().keySet()) {
            ModValue v = bases.get(k);
            if (v != null) implicitOut.put(k, v);
        }
        bases.forEach(implicitOut::putIfAbsent);
        // 装备赋予技能（模板 + 本 read 合并，含辅助技能）→ SkillData
        Map<String, EquipmentSkillEntry> skillEntries = new LinkedHashMap<>();
        merged.skills().forEach((k, id) -> skillEntries.put(k, new EquipmentSkillEntry(id, List.of())));
        skillEntries.putAll(skills);
        List<SkillData> skillData = new ArrayList<>();
        for (EquipmentSkillEntry entry : skillEntries.values()) {
            skillData.add(EquipmentSkillHelper.resolve(entry));
        }
        return new EquipmentState.Base(statsOut, reqsOut, implicitOut, skillData);
    }

    /** 展开为「基础属性 + 基底 Stat」条目列表（引擎形态；基底随基础属性一并注入 StatEngine）。 */
    public List<StatEntry> baseStatEntries() {
        return baseStatEntries(0);
    }

    /**
     * 展开为基础 Stat 条目列表，并按<b>品质</b>换算局部属性（POE2：每 1% 品质 = 1% more 局部物理伤害 / 防御）。
     * 品质是 {@link EquipmentState} 上的标量、不在本 read 内，故由调用方传入——
     * 这样「tooltip 读取」与「Stat 注入」两条链路共用同一换算，品质才不会只改显示不改战力。
     */
    public List<StatEntry> baseStatEntries(int quality) {
        EquipmentTemplate merged = resolveMerged();
        Map<String, ExpressionCodec.Value> stats = qualityScaledStats(quality);
        List<StatEntry> out = new ArrayList<>(merged.baseStatEntries(stats));
        for (Map.Entry<String, ExpressionCodec.Value> e : stats.entrySet()) {
            if (!merged.stats().containsKey(e.getKey())) {
                out.add(new StatRead(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, e.getKey()),
                        Map.of("value", e.getValue())));
            }
        }
        out.addAll(implicitEntries());
        return out;
    }

    /**
     * 品质换算后的基础属性表：仅 {@link QualityOps#EQUIPMENT_KEYS} 内的键参与换算，
     * 且只换算可求值的字面量（表达式形态无法安全缩放，保持原样）。
     */
    public Map<String, ExpressionCodec.Value> qualityScaledStats(int quality) {
        Map<String, ExpressionCodec.Value> out = new LinkedHashMap<>(base().stats());
        if (quality <= 0) {
            return out;
        }
        double mult = QualityOps.multiplier(quality);
        for (String key : QualityOps.EQUIPMENT_KEYS) {
            ExpressionCodec.Value v = out.get(key);
            if (v == null) continue;
            Optional<Double> literal = v.value().left();
            if (literal.isPresent()) {
                out.put(key, ExpressionCodec.of(literal.get() * mult));
            }
        }
        return out;
    }

    /** 展开为基底（implicit）真实 Stat 条目列表（供 tooltip 单独展示）。 */
    public List<StatEntry> implicitEntries() {
        EquipmentTemplate merged = resolveMerged();
        List<StatEntry> out = new ArrayList<>();
        for (Map.Entry<String, AdditionType.Effect> e : merged.bases().entrySet()) {
            ModValue v = base().implicit().get(e.getKey());
            if (v != null) out.addAll(EquipmentTemplate.baseEntries(e.getValue(), v));
        }
        return out;
    }

    /** 替换/新增单个需求键后的副本（键 = 关联符）。 */
    public EquipmentRead withReq(String key, ExpressionCodec.Value value) {
        Map<String, ExpressionCodec.Value> reqs2 = new LinkedHashMap<>(reqs);
        reqs2.put(key, value);
        return new EquipmentRead(ref, template, stats, reqs2, skills, bases);
    }

    /** 需求值（键 = 关联符 l/str/dex/int）。 */
    public java.util.Map<String, Double> requirements() {
        return resolveMerged().reqValues(base().reqs());
    }

    /** 展开为装备基础属性（{@link #base()} 的别名）。 */
    public EquipmentState.Base expand() {
        return base();
    }
    /** 流式建造器：以模板 ref 起手，逐字段填值，最终 {@link #build()} 生成 EquipmentRead（template 缺省、bases 缺省）。 */
    public static Builder builder(Identifier ref) {
        return new Builder(ref);
    }

    public static final class Builder {
        private final Identifier ref;
        private Map<String, ExpressionCodec.Value> stats = new LinkedHashMap<>();
        private Map<String, ExpressionCodec.Value> reqs = new LinkedHashMap<>();
        private Map<String, EquipmentSkillEntry> skills = new LinkedHashMap<>();
        private Map<String, ModValue> bases = new LinkedHashMap<>();

        private Builder(Identifier ref) { this.ref = ref; }

        public Builder stat(String key, ExpressionCodec.Value v) { stats.put(key, v); return this; }
        public Builder req(String key, ExpressionCodec.Value v) { reqs.put(key, v); return this; }
        public Builder skill(Identifier id) { return skill(id, List.of()); }
        public Builder skill(Identifier id, List<Identifier> supports) {
            // 用完整 id（含命名空间）作 key，避免 coe:fireball 与 other:fireball 因只取 path 而静默互覆盖
            skills.put(id.toString(), new EquipmentSkillEntry(id, supports));
            return this;
        }
        public Builder base(String key, ModValue v) { bases.put(key, v); return this; }

        public EquipmentRead build() {
            return new EquipmentRead(Optional.of(ref), Optional.empty(),
                    Map.copyOf(stats), Map.copyOf(reqs), Map.copyOf(skills), Map.copyOf(bases));
        }
    }

}
