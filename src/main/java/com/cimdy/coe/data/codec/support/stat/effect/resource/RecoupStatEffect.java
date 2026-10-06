package com.cimdy.coe.data.codec.support.stat.effect.resource;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.helper.impl.value.RecoupValue;
import com.cimdy.coe.event.TriggerContext;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * 触发亚型：补给（Recoup）——「受到伤害后按比例（8 秒）恢复所选资源」。
 * 复用 {@link RecoupValue#addRecoup} 原语（延迟均匀回复）；本效果只负责在承受伤害事件上触发补给，
 * 不再在伤害管线里硬编码副作用（管道返回值现在只修饰伤害）。
 *
 * <p>唯一数值 = 补给比例（条目 amount）；承伤基数取 {@link TriggerContext#takenPreBlock()}
 * （减免后、招架/格挡前的承伤总额，对齐原 stageRecoup 的 {@code totalDamageTaken} 守卫）。
 *
 * <p><b>资源可多选</b>：身份字段 {@link #resources()} 声明本条补给作用于哪些资源
 * （生命 {@link GateTag#HEALTH} / 魔力 {@link GateTag#MANA} / 能量护盾 {@link GateTag#ENERGY}，
 * 可多选，<b>每个选中资源各获得 amount 比例的补给</b>，未选中资源不受补给）。
 * 缺省 = 三者全给（与描述文件「X% of Damage Taken Recouped as Life, Mana and Energy」旧口径一致）。
 * 例如 {@code "resources": ["health", "energy"]} 表示「伤害的 X% 补给生命、X% 补给能量护盾」，
 * 魔力和未列出资源不获得补给。
 *
 * <p><b>符号语义</b>：补给比例是「比例型」数值，须为正——<b>正数 = 按比例补给</b>；
 * <b>负数与零 = 无补给效果</b>（补给无「反向扣除」语义，非正数直接短路跳过）。
 */
public class RecoupStatEffect extends AbstractStatEffect {

    /** 合法补给资源（与 {@link RecoupValue#addRecoup} 支持的资源键一致）。 */
    public static final Set<String> VALID_RESOURCES = Set.of(GateTag.HEALTH, GateTag.MANA, GateTag.ENERGY);

    /** 缺省资源：三者全给（保持旧口径，旧数据不写 resources 时行为不变）。 */
    public static final Set<String> DEFAULT_RESOURCES = VALID_RESOURCES;

    private final Set<String> resources;
    private final GateExpr fromType;
    private final Basis basis;

    /**
     * 补给基数来源：语料里「Damage <b>taken</b>」与「Damage <b>prevented</b>」是两族不同词条，
     * 基数不同（实际承受量 / 被格挡招架掉的量），不可互相近似。
     */
    public enum Basis implements net.minecraft.util.StringRepresentable {
        /** 实际承受量（{@link TriggerContext#takenPreBlock()}：减免后、招架/格挡前）。 */
        TAKEN,
        /** 被格挡 / 招架掉的量（{@code preBlock - postBlock}）。 */
        PREVENTED;

        public static final Codec<Basis> CODEC = net.minecraft.util.StringRepresentable.fromEnum(Basis::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public RecoupStatEffect(Set<String> resources, GateExpr fromType, GateExpr tags) {
        this(resources, fromType, tags, Basis.TAKEN);
    }

    public RecoupStatEffect(Set<String> resources, GateExpr fromType, GateExpr tags, Basis basis) {
        super(tags);
        for (String r : resources) {
            if (!VALID_RESOURCES.contains(r)) {
                throw new IllegalArgumentException("RecoupStatEffect.resources 每个元素必须是 health/mana/energy 之一，收到: " + r);
            }
        }
        this.resources = resources.isEmpty() ? DEFAULT_RESOURCES : Set.copyOf(resources);
        this.fromType = fromType == null ? GateExpr.ALWAYS : fromType;
        this.basis = basis == null ? Basis.TAKEN : basis;
    }

    /** 无伤害类型门控、无事件标签门控便捷构造。 */
    public RecoupStatEffect(Set<String> resources) {
        this(resources, GateExpr.ALWAYS, GateExpr.ALWAYS);
    }

    /** 无事件标签门控（{@link GateExpr#ALWAYS}）便捷构造。 */
    public RecoupStatEffect(Set<String> resources, GateExpr fromType) {
        this(resources, fromType, GateExpr.ALWAYS);
    }

    /** 补给基数来源（默认 {@link Basis#TAKEN}）。 */
    public Basis basis() {
        return basis;
    }


    /** 补给资源集合（可多选；每个选中资源各获得条目 amount 比例的补给）。 */
    public Set<String> resources() {
        return resources;
    }

    /** 补给基数来源的伤害类型过滤（{@link GateExpr}）：缺省恒真 = 全部伤害（按承伤总量）。
     *  声明类型时（如 {@code physical} / {@code fire|cold}）基数替换为「命中的伤害类型分量之和」——
     *  即「仅物理伤害的 X% 补给」= 物理分量 × X%，而非总量 × X%（对齐 scarred_faith 等基石语义）。 */
    public GateExpr fromType() {
        return fromType;
    }

    /** 便捷构造：指定资源 + 比例（常量），产出一条无条件条目（作用于持有者自身）。 */
    public static StatRead entry(Set<String> resources, double ratio) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(ratio)),
                new RecoupStatEffect(resources, GateExpr.ALWAYS, GateExpr.ALWAYS));
    }

    /** 便捷构造：指定资源，amount 默认 $value 模板（比例由词缀 vars 注入）。 */
    public static StatRead entry(Set<String> resources) {
        return StatEntry.of(ExpressionCodec.VALUE,
                new RecoupStatEffect(resources, GateExpr.ALWAYS, GateExpr.ALWAYS));
    }

    /** 便捷构造：指定资源 + 类型过滤（fromType）+ 比例（常量）。 */
    public static StatRead entry(Set<String> resources, GateExpr fromType, double ratio) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(ratio)),
                new RecoupStatEffect(resources, fromType, GateExpr.ALWAYS));
    }

    /** 便捷构造：指定资源 + 类型过滤（fromType），amount 默认 $value 模板（比例由词缀 vars 注入）。 */
    public static StatRead entry(Set<String> resources, GateExpr fromType) {
        return StatEntry.of(ExpressionCodec.VALUE,
                new RecoupStatEffect(resources, fromType, GateExpr.ALWAYS));
    }

    public static final MapCodec<RecoupStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf)
                    .optionalFieldOf("resources", DEFAULT_RESOURCES)
                    .forGetter(RecoupStatEffect::resources),
            AbstractStatEffect.TAGS_CODEC.optionalFieldOf("from_type", GateExpr.ALWAYS)
                    .forGetter(RecoupStatEffect::fromType),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags),
            Basis.CODEC.optionalFieldOf("basis", Basis.TAKEN).forGetter(RecoupStatEffect::basis)
    ).apply(i, RecoupStatEffect::new));

    @Override
    public String type() {
        return "recoup";
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        if (tc == null || tc.takenPreBlock() <= 0) return;
        double ratio = entry.evaluateAmount(scope.evalBase(evalCtx.owner()));
        if (ratio <= 0) return;
        float base = basis == Basis.PREVENTED ? preventedBase(tc)
                : (fromType.isAlwaysTrue() ? tc.takenPreBlock() : typeBase(tc));
        if (base <= 0) return;
        float amount = (float) (base * ratio);
        // 只补给选中的资源：每个选中资源各获得 amount 比例的补给（未选中资源不受补给）
        for (String r : resources) {
            RecoupValue.addRecoup(evalCtx.owner(), r, amount);
        }
    }

    /**
     * PREVENTED 基数：被格挡 / 招架掉的量（{@code preBlock - postBlock}）。
     *
     * <p>格挡与招架作用于<b>整次命中</b>、不区分伤害类型，故声明了类型过滤时，被格挡量按各类型
     * 在本次伤害中的占比分摊（{@code prevented × 该类型分量 / 总量}）——这是精确分配而非近似：
     * 整次命中被挡下的量天然按构成比例分摊到各伤害类型上。
     */
    private float preventedBase(TriggerContext tc) {
        float prevented = tc.takenPreBlock() - tc.takenPostBlock();
        if (prevented <= 0 || fromType.isAlwaysTrue()) return Math.max(0, prevented);
        float[] damages = tc.damages();
        if (damages == null) return 0;
        float total = 0;
        for (float d : damages) total += d;
        if (total <= 0) return 0;
        return prevented * (typeBase(tc) / total);
    }

    /** 类型过滤基数：对每一伤害槽位（标签见 {@link com.cimdy.coe.data.helper.combat.DamageHelper#slotDamageTags}）
     *  用 {@code fromType} 求值，命中的槽位伤害分量求和——「仅该类型伤害的补给比例」按类型分量计。 */
    private float typeBase(TriggerContext tc) {
        float sum = 0;
        float[] damages = tc.damages();
        if (damages == null) return 0;
        for (int slot = 0; slot < damages.length; slot++) {
            if (fromType.matches(com.cimdy.coe.data.helper.combat.DamageHelper.slotDamageTags(slot))) {
                sum += damages[slot];
            }
        }
        return sum;
    }

    @Override
    public List<Component> describe(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    @Override
    public List<Component> describeFragment(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    /**
     * 本地化效果片段：硬编码 {@code "recoup a portion of damage taken as health/mana/energy from physical"}
     * → 模板 {@code stat_effect.recoup} + 资源值键 {@code stat.value.status.<resource>}（复用既有）
     * + 可选 {@code stat_effect.recoup_from} + 伤害类型值键 {@code stat.value.damage.<type>}。
     */
    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        MutableComponent resComp = Component.empty();
        List<String> res = List.copyOf(resources);
        for (int i = 0; i < res.size(); i++) {
            if (i > 0) resComp.append("/");
            resComp.append(StatValueKey.of("status", res.get(i)));
        }
        MutableComponent line = Component.translatable("stat_effect.recoup", resComp);
        if (!fromType.isAlwaysTrue()) {
            MutableComponent fromComp = Component.empty();
            boolean first = true;
            for (String leaf : fromType.leaves()) {
                if (!first) fromComp.append("/");
                first = false;
                fromComp.append(StatValueKey.of("damage", leaf));
            }
            line = Component.translatable("stat_effect.recoup_from", line, fromComp);
        }
        return List.of(line);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecoupStatEffect e)) return false;
        return Objects.equals(resources, e.resources) && Objects.equals(fromType, e.fromType)
                && Objects.equals(tags(), e.tags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(resources, fromType, tags());
    }

    @Override
    public String toString() {
        return "RecoupStatEffect{resources=" + resources + ", from=" + fromType + ", tags=" + tags() + "}";
    }
}
