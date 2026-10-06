package com.cimdy.coe.data.codec.item.mod.value;

import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.addition.impl.MultiAddition;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 词缀阶预设：一阶词缀的数值快照（原 {@code Instance}）。
 *
 * <h2>为什么 {@code values} 是 {@code Map} 而不是 {@code List}</h2>
 * 旧形态 {@code List<ModValue>} 只能与 {@link AdditionType} 按<b>位置</b>配对：一旦一条词缀声明多个
 * 数值槽（{@code Adds X to Y Damage} 的两条目、组合词缀的多子词缀），「第 i 个值到底给谁」没有权威答案。
 * 现改为按<b>槽名（关联符）</b>键控 —— 槽名即 {@link AdditionType.Effect#valueVar()}（缺省
 * {@link #DEFAULT_VALUE_VAR}），由 {@link AdditionType#valueSlots()} 权威声明，{@link #toAdditions}
 * 据此一一配对，消除歧义。
 *
 * <h2>单槽无需写键（默认不臃肿）</h2>
 * 绝大多数词缀只有一条值 → 槽名即缺省 {@code "value"}。此时用 {@link #of} 直接构造，<b>调用方 / 数据侧不出现任何键</b>；
 * 只有真正多槽的词缀才需要显式键名。
 */
public record TierPreset(String name, Map<String, ModValue> values, int tier, int level, int weight) {

    /** 单槽缺省槽名 —— 与 {@code AdditionType.Effect.toStatEntry} 的缺省 {@code valueVar} 一致。 */
    public static final String DEFAULT_VALUE_VAR = "value";

    public static final Codec<TierPreset> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(TierPreset::name),
                    Codec.unboundedMap(Codec.STRING, ModValue.CODEC).fieldOf("values").forGetter(TierPreset::values),
                    Codec.INT.fieldOf("tier").forGetter(TierPreset::tier),
                    Codec.INT.fieldOf("level").forGetter(TierPreset::level),
                    Codec.INT.fieldOf("weight").forGetter(TierPreset::weight)
            ).apply(instance, TierPreset::new));

    // ── 构造便捷：单槽（缺省槽名，零键负担）────────────────────────────

    /** 单槽单阶（阶号缺省 1）。 */
    public static TierPreset of(String name, ModValue value, int level, int weight) {
        return of(name, value, 1, level, weight);
    }

    /** 单槽单阶（显式阶号）。 */
    public static TierPreset of(String name, ModValue value, int tier, int level, int weight) {
        return new TierPreset(name, Map.of(DEFAULT_VALUE_VAR, value), tier, level, weight);
    }

    // ── 配对 / 展开 ──────────────────────────────────────────────────

    private static double asDouble(ExpressionCodec.Value v) {
        return v.value().left().orElse(0.0);
    }

    /** 单值 → 一条 {@link Addition}（min / max 取范围两端）。 */
    private static Addition toAddition(AdditionType type, ModValue value) {
        return Addition.from(type, asDouble(value.min()), asDouble(value.max().orElse(value.min())));
    }

    /**
     * 按<b>槽名</b>与 {@link AdditionType#valueSlots()} 一一配对，产出词缀实例。
     * {@code type.forSlot(slot)} 把多槽类型收窄到单槽，使每个值只驱动它自己的那部分效果。
     */
    public List<Addition> toAdditions(AdditionType type) {
        List<Addition> out = new ArrayList<>();
        for (String slot : type.valueSlots()) {
            ModValue value = values.get(slot);
            if (value != null) out.add(toAddition(type.forSlot(slot), value));
        }
        if (out.isEmpty() && values.size() == 1) {
            // 兜底：槽名与值键不匹配（如数据只给缺省键）→ 按共享单值处理，
            // 避免整条词缀被静默丢成 0 条效果。
            out.add(toAddition(type, values.values().iterator().next()));
        }
        return out;
    }

    /** 转换为运行时词缀（{@link CommonMod}）。 */
    public CommonMod toCommonMod(ModPreset mod) {
        // TODO(数据运算): ModTier.max（同组词缀的阶数上限）在「一阶一 ModPreset」模型下无本地来源，
        //  需由同组（modPool 装载时）补齐；暂以本阶为上限。
        // 复合词缀（MultiAddition）保持完整：type 携带全部 parts，由消费端
        // （AttributeApplicator / tooltip 累积）按 parts 拆开计算；不在此预拆（避免丢 parts、不动作用域）。
        // 值作透传（引用型 parts 取自身固定数值，忽略本值；按槽拆值属「作用域」范畴，暂不做）。
        AdditionType type = mod.type();
        Addition addition;
        if (type instanceof MultiAddition) {
            ModValue v = values.get(DEFAULT_VALUE_VAR);
            if (v == null) v = values.isEmpty() ? null : values.values().iterator().next();
            double min = v == null ? 0.0 : asDouble(v.min());
            double max = v == null ? 0.0 : asDouble(v.max().orElse(v.min()));
            addition = Addition.from(type, min, max);
        } else {
            // 单 Addition 扁平化：多值槽词缀经 forSlot 收窄为单槽，toAdditions 常态返回 1 条；
            // 极端多槽（数据上未出现）取首条，结构化拆分待「具体结构再简化」决定。
            List<Addition> additions = toAdditions(type);
            addition = additions.isEmpty() ? Addition.from(AdditionType.EMPTY, 0.0) : additions.get(0);
        }
        return new CommonMod(name, new ModTier(tier, tier), level, addition, Location.PREFIX);
    }
}
