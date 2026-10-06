package com.cimdy.coe.data.gen.stat;

import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.attribute.AttributeLimiters;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.query.Conversions;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import com.cimdy.coe.data.gen.stat.wirings.wire.attribute.WireAilment;
import com.cimdy.coe.data.gen.stat.wirings.wire.attribute.WireCrit;
import com.cimdy.coe.data.gen.stat.wirings.wire.attribute.WireDamage;
import com.cimdy.coe.data.gen.stat.wirings.wire.attribute.WireDefense;
import com.cimdy.coe.data.gen.stat.wirings.wire.flow.WireGenerated;
import com.cimdy.coe.data.gen.stat.wirings.wire.flow.WireMisc;
import com.cimdy.coe.data.gen.stat.wirings.wire.resource.WirePerScaling;
import com.cimdy.coe.data.gen.stat.wirings.wire.resource.WireRecent;
import com.cimdy.coe.data.gen.stat.wirings.wire.resource.WireResource;
import com.cimdy.coe.data.gen.stat.wirings.wire.flow.WireSocket;
import com.cimdy.coe.data.gen.stat.wirings.wire.attribute.WireSpeed;
import com.cimdy.coe.data.gen.stat.wirings.wire.flow.WireWhile;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stat 条目注册中心（facade）：Stat 三段内联组装 + 条目引用句柄。
 */
public class StatGen {
    private static final Map<Identifier, StatEntry> ENTRIES = buildEntries();

    private static final Map<Identifier, StatEntry> EXTERNAL = new LinkedHashMap<>();

    /**
     * 注册一条具体条目；key 冲突且内容不同则报错（避免静默覆盖导致数值漂移）。
     * <p>内容比较基于 JSON 序列化签名（与 {@link #entries()} 去重口径一致），而非 {@link StatEntry}/{@link
     * com.cimdy.coe.data.codec.support.math.ExpressionCodec.Value} 等的 {@code equals}——后者多未覆写
     * 值相等语义，会使「同内容重复注册」（如 {@code livingmod_resist_fire_20} 同时被 nether / hot 两个
     * LivingModifier 复用）被误判为冲突而抛错。共享条目重复注册应幂等通过；仅当 JSON 真不同时才报错。
     */
    public static void register(Identifier id, StatEntry entry) {
        StatEntry prev = EXTERNAL.get(id);
        if (prev == null) {
            EXTERNAL.put(id, entry);
            return;
        }
        String prevJson = jsonOf(prev);
        String nextJson = jsonOf(entry);
        boolean same = prevJson != null && nextJson != null
                ? prevJson.equals(nextJson)
                : prev.equals(entry);
        if (!same) {
            throw new IllegalStateException("StatGen 条目键冲突且内容不同: " + id);
        }
    }

    /** 内容寻址签名：序列化 JSON 字符串；编码失败返回 null（交由调用方回退 equals）。 */
    private static String jsonOf(StatEntry e) {
        return StatEntry.CODEC.encodeStart(JsonOps.INSTANCE, e)
                .result().map(Object::toString).orElse(null);
    }

    /**
     * 注册具体条目并返回其引用句柄（{@link StatRead}）。
     *
     * <p>effect / targets / conditions 全部内联为真实对象，无「四部件路径化 → 运行期再解析」这一步；
     * 故 {@code Stat} 与其读回结果恒等，{@code StatTargets} 也无 {@code of} / {@code ofInline} 之分。
     * 非 {@code Stat} 的条目（{@code StatGroup} / 组合等）原样注册。
     */
    public static StatEntry read(Identifier id, StatEntry entry) {
        register(id, entry);
        return new StatRead(id);
    }

    /** 便捷助手：注册一条属性修饰条目（常量 amount）并返回 Read 引用。 */
    public static StatEntry modRead(Identifier id, Holder<Attribute> attribute, double amount,
                                   AttributeModifier.Operation op, String... tags) {
        return read(id, ModifierStatEffect.entry(attribute, ExpressionCodec.of(amount),
                Either.left(op), Set.of(tags), List.of()));
    }

    /** 按名称引用 StatGen 中已注册的具体条目（写数据生成用 Read，不重复注册）。 */
    public static StatEntry ref(Identifier id) {
        return new StatRead(id);
    }

    /** 按名称引用 StatGen 中已注册的 $value 模板条目，并注入实际幅度（经 vars 的 "value" 键）。 */
    public static StatEntry ref(Identifier id, double value) {
        return new StatRead(id, Map.of("value", ExpressionCodec.of(value)));
    }

    /**
     * 天赋属性授予：写数据生成用 Read，具体条目在此注册为 concrete 条目。
     */
    public static StatEntry talentAttr(Holder<Attribute> attribute, double amount,
            Either<AttributeModifier.Operation, AttributeLimiters.Operation> op, Set<String> tags) {
        Identifier id = attrId("talent", attribute, op, tags, null, amount);
        registerModifier(id, attribute, ExpressionCodec.of(amount), op, tags);
        return new StatRead(id);
    }

    /** 天赋属性授予（含来源属性乘子）：数值 = {@code attribute:<source> * amount}（固定表达式，烘进条目）。 */
    public static StatEntry talentAttr(Holder<Attribute> attribute, double amount,
            Either<AttributeModifier.Operation, AttributeLimiters.Operation> op,
            Holder<Attribute> sourceAttribute, Set<String> tags) {
        MathExpression source = ExpressionCodec.Variable.from("attribute",
                BuiltInRegistries.ATTRIBUTE.getKey(sourceAttribute.value()), StatGenConsts.ZERO);
        ExpressionCodec.Value value = ExpressionCodec.of(Conversions.convertScale(source, amount));
        Identifier id = attrId("talent", attribute, op, tags, sourceAttribute, amount);
        registerModifier(id, attribute, value, op, tags);
        return new StatRead(id);
    }

    /** 实体 LivingMod 属性授予（数据生成）：同 {@link #talentAttr} 范式，前缀 {@code livingmod_}。 */
    public static StatEntry livingModAttr(Holder<Attribute> attribute, double amount,
            Either<AttributeModifier.Operation, AttributeLimiters.Operation> op, Set<String> tags) {
        Identifier id = attrId("livingmod", attribute, op, tags, null, amount);
        registerModifier(id, attribute, ExpressionCodec.of(amount), op, tags);
        return new StatRead(id);
    }

    /** 技能 ModifierAction 运行时属性授予：$value 模板，幅度经 {@link StatRead#vars} 注入。 */
    public static StatEntry modifierAttr(Holder<Attribute> attribute, double amount,
            Either<AttributeModifier.Operation, AttributeLimiters.Operation> op, Set<String> tags) {
        Identifier id = attrId("modifier", attribute, op, tags, null, StatGenConsts.ZERO);
        registerModifier(id, attribute, ExpressionCodec.VALUE, op, tags);
        return new StatRead(id, Map.of("value", ExpressionCodec.of(amount)));
    }

    /** 属性授予类条目稳定键：{@code <prefix>_} + attribute + op + source? + amount(规范化) + 排序后 tags。 */
    private static Identifier attrId(String prefix, Holder<Attribute> attribute,
            Either<AttributeModifier.Operation, AttributeLimiters.Operation> op,
            Set<String> tags, Holder<Attribute> source, double amount) {
        StringBuilder sb = new StringBuilder(prefix).append('_');
        sb.append(BuiltInRegistries.ATTRIBUTE.getKey(attribute.value()).toString().replace(':', '_'));
        sb.append('_').append(opKey(op));
        if (source != null) {
            sb.append("_src_").append(BuiltInRegistries.ATTRIBUTE.getKey(source.value()).toString().replace(':', '_'));
        }
        sb.append('_').append(amountKey(amount));
        if (!tags.isEmpty()) {
            sb.append('_').append(String.join("_", tags.stream().sorted().toList()));
        }
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, sb.toString().toLowerCase());
    }

    /** op 的稳定字符串（小写）：op_add_value / lim_set_value 等。 */
    private static String opKey(Either<AttributeModifier.Operation, AttributeLimiters.Operation> op) {
        return op.map(o -> "op_" + o.name().toLowerCase(), l -> "lim_" + l.name().toLowerCase());
    }

    /** amount 的规范化键（去尾零，避免浮点表示不稳定）。 */
    private static String amountKey(double amount) {
        return java.math.BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString().replace('.', '_');
    }

    /**
     * 全部可复用 StatEntry（id → 实例）= 内建核心条目 ⊕ 外部注册条目；运行时由 LoadEvent 直接查表。
     *
     * <p><b>内容寻址去重（Stat 构造期重复创建修复）</b>：同一份 StatEntry 内容（effect / targets /
     * condition / amount / params）常被多个语义不同的 StatId 注册（如 {@code INC_CAST_SPEED} 与
     * {@code RARITY_MAGIC_CAST_SPEED} 同落 {@code EFF_CAST_SPEED}）。{@link WireEntryMap} 的键冲突守卫
     * 只拒绝「同 id 不同内容」，<b>不捕获「不同 id 同内容」</b>——这正是冗余条目（runData 时生成重复 JSON）
     * 的根源。此处按<b>序列化 JSON 签名</b>归并：首个具体条目保留为权威，其余改写为 {@link StatRead}
     * 别名（运行期经 LoadEvent 两轮解析透明还原为同一内容）。{@link StatRead} 本身已是别名，原样保留、
     * 不入权威池，避免二次跳转。</p>
     */
    public static Map<Identifier, StatEntry> entries() {
        Map<Identifier, StatEntry> all = new LinkedHashMap<>(ENTRIES);
        all.putAll(EXTERNAL);
        Map<String, Identifier> canonByJson = new HashMap<>();
        Map<Identifier, StatEntry> out = new LinkedHashMap<>();
        for (Map.Entry<Identifier, StatEntry> e : all.entrySet()) {
            StatEntry v = e.getValue();
            if (v instanceof StatRead) {                 // 已是别名：原样保留，不入权威池
                out.put(e.getKey(), v);
                continue;
            }
            String json = StatEntry.CODEC.encodeStart(JsonOps.INSTANCE, v)
                    .result().map(Object::toString).orElse(null);
            if (json == null) {                          // 编码失败则保守保留原条目
                out.put(e.getKey(), v);
                continue;
            }
            Identifier canon = canonByJson.get(json);
            if (canon == null) {
                canonByJson.put(json, e.getKey());
                out.put(e.getKey(), v);
            } else {
                out.put(e.getKey(), new StatRead(canon));
            }
        }
        return Map.copyOf(out);
    }

    private static Map<Identifier, StatEntry> buildEntries() {
        Map<Identifier, StatEntry> map = new WireEntryMap();
        WireAilment.build(map);
        WireCrit.build(map);
        WireDamage.build(map);
        WireDefense.build(map);
        WireGenerated.build(map);
        WireMisc.build(map);
        WirePerScaling.build(map);
        WireRecent.build(map);
        WireResource.build(map);
        WireSocket.build(map);
        WireSpeed.build(map);
        WireWhile.build(map);
        // StatGen-only (generated from entry JSON, not yet partitioned into a Wire):
        map.put(StatIds.ADD_RECENTLY_CONSUMED_DAMAGE, stat(StatTargetIds.TGT_SELF_ONLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_TAKEN));
        // 技能槽数量：金额走 $value 模板，供天赋/装备/涂油经 StatGen.ref(id, N) 注入幅度。
        map.put(StatIds.SKILL_SLOT_COUNT, stat(StatTargetIds.TGT_SELF_ONLY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_SKILL_SLOT_COUNT));
        // 回响次数：金额走 $value 模板，供天赋/装备/涂油经 StatGen.ref(id, N) 注入幅度。
        // 消费端 EchoState#statEchoCount 在技能作用域内读 getAttributeValue(ECHO_COUNT)，
        // 经 LivingAttributeValue mixin → StatEngine.fold 拿到本条 GAIN 累加值。
        map.put(StatIds.ECHO_COUNT, stat(StatTargetIds.TGT_SELF_ONLY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ECHO_COUNT));
        return map;
    }

    /**
     * Wire 聚合专用映射：为各 {@code Wire*.build(map)} 的 {@code map.put(StatIds.X, ...)} 提供与
     * {@link #register} 外部通道等价的<b>键冲突守卫</b>——同一 StatId 被两个 Wire 以不同内容写入时立即
     * 报错，取代 {@link LinkedHashMap} 的静默「后者覆盖」（last-wins）所导致的数值漂移。
     *
     * <p>内容相同（{@link StatEntry#equals}）时按 {@code super.put} 覆写等值内容、<b>不改变已有键的插入
     * 内联 {@code map.put(StatIds.ADD_RECENTLY_CONSUMED_DAMAGE, ...)}。
     */
    private static final class WireEntryMap extends LinkedHashMap<Identifier, StatEntry> {
        @Override
        public StatEntry put(Identifier key, StatEntry value) {
            StatEntry prev = super.put(key, value);
            if (prev != null && !prev.equals(value)) {
                throw new IllegalStateException("StatGen 条目键冲突且内容不同: " + key);
            }
            return prev;
        }
    }

    /** 纯路径组装（无条件 / 已解成节点的条件）：内联进条目，交给 {@link ValueExpr#of} 压成单个复式节点。 */
    private static StatRead stat(List<ValueExpr> conds, Identifier targetsRef, Identifier effectRef) {
        return new StatRead(ExpressionCodec.VALUE, conds, targetsRef, effectRef);
    }

    /** 纯路径组装（无条件）。 */
    private static StatRead stat(Identifier targetsRef, Identifier effectRef) {
        return stat(List.of(), targetsRef, effectRef);
    }

    /** 动态 concrete 属性修饰条目（talent/livingmod/modifier 系）：三段全部内联，无内容寻址、无落盘悬空风险。 */
    private static void registerModifier(Identifier id, Holder<Attribute> attribute, ExpressionCodec.Value amount,
            Either<AttributeModifier.Operation, AttributeLimiters.Operation> op, Set<String> tags) {
        register(id, new StatRead(amount, List.of(), StatTargets.EMPTY,
                new ModifierStatEffect(attribute, op, ModifierStatEffect.Mode.GAIN, GateExpr.of(tags))));
    }

}
