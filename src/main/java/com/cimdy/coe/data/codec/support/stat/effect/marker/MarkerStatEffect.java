package com.cimdy.coe.data.codec.support.stat.effect.marker;

import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.stat.effect.GateTypeIds;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jspecify.annotations.Nullable;

import com.cimdy.coe.data.codec.support.stat.entry.describer.StatNumberFormat;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 统一惰性标记效果（INERT 型，不进 StatStore 索引）：收敛 8 个结构近同、仅消费语义
 * （rolled-chance / boolean-gate / summed-magnitude / 惰性标记）不同的标记类——
 * {@code CorruptionEnduresStatEffect} / {@code DefendStatEffect} / {@code ChanceDefendStatEffect} /
 * {@code ConsumedCorpseDamageStatEffect} / {@code TakeNoVolatilityDamageStatEffect} /
 * {@code ChanceStatEffect} / {@code TaggedGateStatEffect} / {@code TaggedMagnitudeStatEffect}。
 *
 * <h2>kind 的职责已被解耦为三件互不相干的事</h2>
 * <ol>
 *   <li><b>身份</b>：{@link #kind()} / {@link #type()} 仍是唯一身份串，供 JSON {@code type} 分发与
 *       {@code StatEntry} 引用；开放 String，新增 kind 无需改 Java 枚举。</li>
 *   <li><b>消费语义</b>：由 {@link #variant()}（粗轴：MARKER / ROLLED / GATE / MAGNITUDE）与
 *       {@link #slot()}（细轴：{@link Slot} = variant + 渲染单位 + 读口）承担，
 *       <b>由 {@link #KIND_SLOTS} 表按 kind 推出</b>，消费端不再对 63 个不透明串逐个写 if。</li>
 *   <li><b>渲染单位</b>：由 {@link #unit()} 承担（百分比 / 纯数 / 秒 / 米 / 无数字的布尔门控），
 *       {@link #describeFragment} 只按 {@link Unit} 分派，对任何 kind 都不写 if。</li>
 * </ol>
 *
 * <p><b>不变式</b>：kind 已登记于 {@link #KIND_SLOTS} 时，构造期校验 JSON 声明的 {@code variant}
 * 必须等于表推出的 variant，否则抛 {@link IllegalArgumentException}——把「消费端读口与数据声明语义错配」
 * 从「静默返回 0 / 永久失效」提前到 datagen 落盘期崩溃。未登记的开放 kind 只校验读口一致性。</p>
 *
 * <p><b>幅度缩放</b>：{@code scale_attr} 经 {@link #scaleAttr()} 暴露，由 StatEngine 四个读口
 * （{@code markerMagnitude} / {@code rollChance} / {@code rollCount} / {@code gateOf} / {@code magnitudeOf}）
 * 一律经 {@code scaledMagnitude} 应用——JSON 里写的 {@code scale_attr} 必须真正参与判定，
 * 否则缩放静默失效（波及 45+ kind）。</p>
 *
 * <p>所有 kind 均为「对承伤/机制的离散修饰」：节点取消分配即 STAT_STORE 条目移除、机制立即失效，无 Buff 残留泄漏。</p>
 */
public final class MarkerStatEffect extends AbstractStatEffect {

    /** 标记 kind 字符串常量（<b>身份串，非语义</b>；语义见 {@link #KIND_SLOTS}）。 */
    public static final class Kind {
        public static final String CORRUPTION_ENDURES = "corruption_endures";
        public static final String DEFEND = "defend";
        public static final String CHANCE_DEFEND = "chance_defend";
        public static final String TAKE_NO_VOLATILITY_DAMAGE = "take_no_volatility_damage";
        public static final String EXTRA_CHARGE_CONSUMED = "extra_charge_consumed";
        public static final String OFFERING_RECENT_IMMUNE = "offering_recent_immune";
        public static final String FLASK_NUM_CHARGES = "flask_num_charges";
        public static final String FLASK_CHANCE_GAIN_CHARGE_ON_KILL = "flask_chance_gain_charge_on_kill";

        /**
         * 「被你点燃过的敌人」递增承火伤（ENEMIES_IGNITED_BY_YOU_PERMANENTLY_TAKE_INC_FIRE_DAMAGE...）。
         * <p>纯幅度载体：幅度公式（{@code min(累计点燃秒数*rate, cap)}）已归位为 context 读值节点
         * {@code ignite_history_fire_taken}，由条目 amount 表达式承载；本 kind 只作身份标记，
         * 供消费端 {@code StatEngine#magnitudeOf} 按 kind 折叠各条目幅度。
         */
        public static final String IGNITE_HISTORY_FIRE_TAKEN = "ignite_history_fire_taken";

        /** kind 字段编解码（开放 String）。 */
        public static final Codec<String> CODEC = Codec.STRING;

        private Kind() {
        }
    }

    /** 消费变体（粗语义轴）：决定运行时读取口径（惰性标记 / 概率掷骰 / 布尔门控 / 求和幅度）。 */
    public enum Variant implements StringRepresentable {
        MARKER("marker"),
        ROLLED("rolled"),
        GATE("gate"),
        MAGNITUDE("magnitude");

        public static final Codec<Variant> CODEC = StringRepresentable.fromEnum(Variant::values);

        private final String id;

        Variant(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return id;
        }
    }

    /**
     * 渲染单位：{@link #describeFragment} 的<b>唯一</b>分派依据。
     *
     * <p>单位一律按各自量纲渲染：不可对所有 kind 一律用 {@code amount*100 + "%"}，
     * 否则以米为单位的 {@code shield_front_debilitate}、以秒为单位的
     * {@code invocation_trigger_interval} 单位错误。单位由
     * {@link #KIND_SLOTS} 声明，且可被 JSON 的 {@code unit} 字段逐条覆盖（数据侧即可改单位，
     * 无需改 Java）。</p>
     */
    public enum Unit implements StringRepresentable {
        /** 比例：按 {@code amount * 100} 加 {@code %} 渲染。 */
        PERCENT("percent"),
        /** 纯数：原值四舍五入渲染，无单位后缀。 */
        NUMBER("number"),
        /** 秒：原值 + 可译单位词（{@code stat.value.unit.seconds}，缺键回落英文 Seconds）。 */
        SECONDS("seconds"),
        /** 米：原值 + 可译单位词（{@code stat.value.unit.metres}，缺键回落英文 Metres）。 */
        METRES("metres"),
        /** 布尔门控：无数值可渲染，只渲染效果名（{@code stat_effect.marker} 数值位留空）。 */
        FLAG("flag");

        private final String id;

        Unit(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return id;
        }

        public static final Codec<Unit> CODEC = StringRepresentable.fromEnum(Unit::values);

        /**
         * 按本单位渲染条目幅度。
         *
         * @param raw    条目原始幅度（比例类为分数 0~1，其余为原值）
         * @param locale 语言环境（千分位 / trim 语义由 {@link StatNumberFormat} 统一）
         * @return 可直接拼进 {@code stat_effect.marker} 模板数值位的组件；{@link #FLAG} 返回空组件
         */
        public Component format(double raw, Locale locale) {
            return switch (this) {
                case PERCENT -> Component.literal(StatNumberFormat.format(Math.round(raw * 100.0), locale) + "%");
                case NUMBER -> Component.literal(StatNumberFormat.format(Math.round(raw), locale));
                case SECONDS -> withUnit(raw, locale, "seconds");
                case METRES -> withUnit(raw, locale, "metres");
                case FLAG -> Component.empty();
            };
        }

        /** 数值 + 空格 + 可译单位词（缺键时按 {@link StatValueKey} 铁律回落为可见英文，不漏裸键）。 */
        private static Component withUnit(double raw, Locale locale, String unitNs) {
            return Component.literal(StatNumberFormat.format(Math.round(raw), locale) + " ")
                    .append(StatValueKey.of("unit", unitNs));
        }
    }

    /**
     * 消费槽位（细语义轴 = {@link Variant} + {@link Unit} + 归属读口）：<b>消费端应按本轴而非 kind 字符串分派</b>。
     *
     * <p>同一 {@link Variant} 下的多个 {@code Slot} 仅因渲染单位不同而分开（秒 / 米 / 纯数），
     * 故 Slot 数量远小于 kind 数量；同 Slot 的多个 kind 即「形状相同、参数不同」族。</p>
     */
    public enum Slot {
        /** 惰性标记幅度（比例）：{@code markerMagnitude} 读口。 */
        MARKER_AMOUNT(Variant.MARKER, Unit.PERCENT),
        /** 惰性标记幅度（纯数，如额外消耗球数 / 药剂槽位数）：{@code markerMagnitude} 读口。 */
        MARKER_AMOUNT_NUMBER(Variant.MARKER, Unit.NUMBER),
        /** 惰性标记阈值（{@code >= 1.0} 即持有，如近期献祭免疫）：{@code markerMagnitude} 读口。 */
        MARKER_THRESHOLD(Variant.MARKER, Unit.NUMBER),
        /** 单次概率掷骰：{@code rollChance} 读口。 */
        ROLLED_CHANCE(Variant.ROLLED, Unit.PERCENT),
        /** 保底次数 + 余概率掷骰：{@code rollCount} 读口。 */
        ROLLED_COUNT(Variant.ROLLED, Unit.NUMBER),
        /** 布尔门控：{@code gateOf} 读口（无数字可渲染）。 */
        GATE(Variant.GATE, Unit.FLAG),
        /** 布尔门控 + 身份标签外露：{@code gateOf} / {@code collectGateTags} 读口。 */
        GATE_TAGGED(Variant.GATE, Unit.FLAG),
        /** 幅度（比例）：{@code magnitudeOf} 读口。 */
        MAGNITUDE(Variant.MAGNITUDE, Unit.PERCENT),
        /** 幅度（纯数，如每次命中的额外个数 / 实体数量上限）：{@code magnitudeOf} 读口。 */
        MAGNITUDE_NUMBER(Variant.MAGNITUDE, Unit.NUMBER),
        /** 幅度（秒，如元技能触发间隔）：{@code magnitudeOf} 读口。 */
        MAGNITUDE_SECONDS(Variant.MAGNITUDE, Unit.SECONDS),
        /** 幅度（米，如举盾前方锥形半径）：{@code magnitudeOf} 读口。 */
        MAGNITUDE_METRES(Variant.MAGNITUDE, Unit.METRES);

        private final Variant variant;
        private final Unit unit;

        Slot(Variant variant, Unit unit) {
            this.variant = variant;
            this.unit = unit;
        }

        public Variant variant() {
            return variant;
        }

        public Unit unit() {
            return unit;
        }

        /**
         * 未登记 kind 的默认槽位：按其 variant 取该 variant 的<b>基准</b>槽位（显式 switch，
         * 不依赖 {@code values()} 声明顺序——在枚举头部插入新成员不得静默改变未登记 kind 的默认槽位）。
         *
         * <p>各 variant 的基准槽位：
         * {@code MARKER → MARKER_AMOUNT}、{@code ROLLED → ROLLED_CHANCE}、
         * {@code GATE → GATE}、{@code MAGNITUDE → MAGNITUDE}。
         * 变体新增时此switch 编译期强制补齐分支，不会静默兜底到某个既有分支。</p>
         */
        static Slot defaultFor(Variant variant) {
            return switch (variant) {
                case MARKER -> MARKER_AMOUNT;
                case ROLLED -> ROLLED_CHANCE;
                case GATE -> GATE;
                case MAGNITUDE -> MAGNITUDE;
            };
        }
    }

    /** 掷骰口径：SIMPLE = 单次概率；COUNT = 保底次数 + 余概率。 */
    public enum RollMode implements StringRepresentable {
        SIMPLE("simple"),
        COUNT("count");

        public static final Codec<RollMode> CODEC = StringRepresentable.fromEnum(RollMode::values);

        private final String id;

        RollMode(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return id;
        }
    }

    public static final MapCodec<MarkerStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("kind").forGetter(MarkerStatEffect::kind),
            Variant.CODEC.optionalFieldOf("variant", Variant.MARKER).forGetter(MarkerStatEffect::variant),
            RollMode.CODEC.optionalFieldOf("roll_mode", RollMode.SIMPLE).forGetter(MarkerStatEffect::rollMode),
            Unit.CODEC.optionalFieldOf("unit").forGetter(e -> e.unitOverride),
            Codec.STRING.listOf().optionalFieldOf("condition_tags", List.of()).forGetter(MarkerStatEffect::conditionTags),
            Codec.STRING.listOf().optionalFieldOf("identity_tags", List.of()).forGetter(e -> List.copyOf(e.identityTags)),
            Attribute.CODEC.optionalFieldOf("scale_attr").forGetter(e -> e.scaleAttr),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, (k, v, rm, u, ct, it, sa, tg) -> new MarkerStatEffect(k, v, Set.copyOf(it), rm, ct, sa.orElse(null), u.orElse(null), tg)));

    private final String kind;
    /** 消费变体（MARKER / ROLLED / GATE / MAGNITUDE）。 */
    private final Variant variant;
    /** 消费槽位（由 {@link #KIND_SLOTS} 按 kind 推出；JSON {@code unit} 可覆盖其单位）。 */
    private final Slot slot;
    /** 掷骰口径（仅 ROLLED 使用；其余恒 SIMPLE）。 */
    private final RollMode rollMode;
    /** JSON {@code unit} 显式覆盖（null = 用 {@link #slot()} 的默认单位）。 */
    private final Optional<Unit> unitOverride;
    /** 命中条件作用域（由 MARKER 的 DEFEND / EXTRA_CHARGE_CONSUMED 使用；须全部命中才参与折叠）。 */
    private final List<String> conditionTags;
    /** 作用域身份标签（由 ROLLED / GATE / MAGNITUDE 使用；查询集须 containsAll 才命中）。 */
    private final Set<String> identityTags;
    /** 幅度属性缩放（null = 不缩放）；消费端经 StatEngine#scaledMagnitude 应用。 */
    private final Optional<Holder<Attribute>> scaleAttr;

    /** 惰性标记便捷构造（MARKER 变体，无变体字段）。 */
    public MarkerStatEffect(String kind) {
        this(kind, Variant.MARKER, Set.of(), RollMode.SIMPLE, List.of(), null, null, GateExpr.ALWAYS);
    }

    /** MARKER 变体 + 命中条件作用域（DEFEND / EXTRA_CHARGE_CONSUMED）。 */
    public MarkerStatEffect(String kind, List<String> conditionTags) {
        this(kind, Variant.MARKER, Set.of(), RollMode.SIMPLE, conditionTags, null, null, GateExpr.ALWAYS);
    }

    /** MARKER 变体 + 命中条件作用域 + 自定义 tags。 */
    public MarkerStatEffect(String kind, List<String> conditionTags, GateExpr tags) {
        this(kind, Variant.MARKER, Set.of(), RollMode.SIMPLE, conditionTags, null, null, tags);
    }

    /** 门控 / 幅度 / 掷骰通用构造（identityTags 承载作用域身份）。 */
    public MarkerStatEffect(String kind, Variant variant, Set<String> identityTags) {
        this(kind, variant, identityTags, RollMode.SIMPLE, List.of(), null, null, GateExpr.ALWAYS);
    }

    /** 掷骰构造（ROLLED 变体，rollMode 决定 SIMPLE / COUNT）。 */
    public MarkerStatEffect(String kind, Variant variant, Set<String> identityTags, RollMode rollMode) {
        this(kind, variant, identityTags, rollMode, List.of(), null, null, GateExpr.ALWAYS);
    }

    public MarkerStatEffect(String kind, Variant variant, Set<String> identityTags, RollMode rollMode,
            List<String> conditionTags, @Nullable Holder<Attribute> scaleAttr, GateExpr tags) {
        this(kind, variant, identityTags, rollMode, conditionTags, scaleAttr, null, tags);
    }

    /**
     * 全参构造：<b>唯一做语义自洽校验的入口</b>。
     *
     * <p>kind 已登记于 {@link #KIND_SLOTS} 时，JSON 声明的 {@code variant} 与表推出的 variant
     * 不一致即抛异常——这正是「消费端读口按 variant 过滤、数据却声明了别的 variant → 永久静默失效」
     * 那类 bug 的根因，现提前到落盘期暴露。</p>
     */
    public MarkerStatEffect(String kind, Variant variant, Set<String> identityTags, RollMode rollMode,
            List<String> conditionTags, @Nullable Holder<Attribute> scaleAttr,
            @Nullable Unit unitOverride, GateExpr tags) {
        super(tags);
        this.kind = kind;
        this.variant = variant == null ? Variant.MARKER : variant;
        this.slot = slotOf(kind, this.variant);
        Slot declared = KIND_SLOTS.get(kind);
        if (declared != null && declared.variant() != this.variant) {
            throw new IllegalArgumentException("MarkerStatEffect kind=\"" + kind + "\" 声明 variant="
                    + this.variant + "，但 KIND_SLOTS 登记为 " + declared.variant()
                    + "（slot=" + declared + "）；消费端读口按 variant 过滤，不一致将永久静默失效");
        }
        this.rollMode = rollMode == null ? RollMode.SIMPLE : rollMode;
        this.identityTags = Set.copyOf(identityTags);
        this.conditionTags = List.copyOf(conditionTags);
        this.scaleAttr = Optional.ofNullable(scaleAttr);
        this.unitOverride = Optional.ofNullable(unitOverride);
    }

    public String kind() {
        return kind;
    }

    public Variant variant() {
        return variant;
    }

    /** 消费槽位：kind 的消费语义（variant + 单位）真源，见 {@link #KIND_SLOTS}。 */
    public Slot slot() {
        return slot;
    }

    public RollMode rollMode() {
        return rollMode;
    }

    /** 实际渲染单位：JSON {@code unit} 覆盖优先，否则取 {@link #slot()} 的默认单位。 */
    public Unit unit() {
        return unitOverride.orElseGet(slot::unit);
    }

    /** 命中条件作用域；由 MARKER 变体（DEFEND / EXTRA_CHARGE_CONSUMED）使用。 */
    public List<String> conditionTags() {
        return conditionTags;
    }

    /** 作用域身份标签；由 ROLLED / GATE / MAGNITUDE 使用，查询集 containsAll 本集合才命中。 */
    public Set<String> identityTags() {
        return identityTags;
    }

    /** 幅度属性缩放（null = 不缩放）；StatEngine 五个读口一律经 scaledMagnitude 应用。 */
    public Optional<Holder<Attribute>> scaleAttr() {
        return scaleAttr;
    }

    /** 惰性标记便捷构造（StatEntry 载体）；identityTags 为空、amount 直出。 */
    public static StatEntry entry(String kind, double amount, GateExpr tags) {
        return entry(kind, List.of(), amount, tags);
    }

    public static StatEntry entry(String kind, List<String> conditionTags, double amount, GateExpr tags) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(amount)),
                new MarkerStatEffect(kind, conditionTags, tags));
    }

    /** 门控 stat 便捷构造（Stat 载体，amount=0）：以 {@code Variant.GATE} 落一个门控标记条目。 */
    public static StatRead entry(String typeId, Set<String> identityTags) {
        return new StatRead(ExpressionCodec.of(0.0), List.of(), (StatTargets) null,
                new MarkerStatEffect(typeId, Variant.GATE, identityTags, RollMode.SIMPLE));
    }

    @Override
    public String type() {
        return kind;
    }

    @Override
    public boolean isTrigger() {
        return false;
    }

    @Override
    public EffectKind indexKind() {
        return EffectKind.INERT;
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
     * 本地化效果片段：值键 {@code stat.value.marker.<kind>}（en = humanize 英文，zh 自译）
     * + <b>按 {@link #unit()} 格式化</b>的数值（走 {@link StatNumberFormat} / {@link Unit}）。
     * 命中上下文门控（conditionTags 非空）追加 {@code against <labels>} 子句，
     * 标签各自经 {@code stat.value.cond_tag.<tag>}（缺则 humanize 英文回落）。
     *
     * <p>本方法只按 {@link Unit} 分派、<b>不对任何 kind 写 if</b>：单位来自
     * {@link #KIND_SLOTS} 表或 JSON {@code unit} 覆盖。布尔门控（{@link Unit#FLAG}）无数值，
     * 只渲染效果名（否则会渲染成「无法使用护符 100%」）。</p>
     */
    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        Component name = StatValueKey.of("marker", kind);
        Unit unit = unit();
        MutableComponent line = unit == Unit.FLAG
                ? Component.empty().append(name)
                : Component.translatable("stat_effect.marker", name, unit.format(entry.amount().getValue(), locale));
        if (!conditionTags.isEmpty()) {
            line = Component.translatable("stat_effect.marker_against", line, conditionComponent());
        }
        return List.of(line);
    }

    /** conditionTags 拼接为「/」分隔的可译组件（每标签走 {@code stat.value.cond_tag.<tag>}）。 */
    private Component conditionComponent() {
        MutableComponent c = Component.empty();
        for (int i = 0; i < conditionTags.size(); i++) {
            if (i > 0) c.append("/");
            c.append(StatValueKey.of("cond_tag", conditionTags.get(i)));
        }
        return c;
    }

    @Override
    public String toString() {
        return "MarkerStatEffect{" + kind + ", variant=" + variant + ", slot=" + slot + ", unit=" + unit()
                + ", conditions=" + conditionTags + ", identity=" + identityTags + ", tags=" + tags() + "}";
    }

    /**
     * kind → {@link Slot} 单一真源表（<b>本类唯一的「kind 语义声明处</b>）。
     *
     * <p>本表是「每个 kind 是什么语义」的唯一声明处：每个条目在此声明该 kind 落在哪个 {@link Slot}
     * （= Variant 消费语义 + Unit 渲染单位），于是
     * <ul>
     *   <li>渲染单位（{@code shield_front_debilitate} = 米 / {@code invocation_trigger_interval} = 秒）
     *       由表决定，{@link #describeFragment} 不再对任何 kind 写 if；</li>
     *   <li>{@link StatEngine} 四个读口按 <b>表推出的 Slot</b> 校验命中条目，
     *       消费端调用与数据声明的语义错配在构造期即暴露，而非静默返回 0。</li>
     * </ul>
     *
     * <p>同 Slot 的多个 kind 即「形状相同、参数不同」的族（如 ROLLED_COUNT 三个实体阶段 / 额外生成
     * 概率共用同一消费形状），是后续合并为「单 kind + 参数」的候选集——合并时只需改本表 + JSON，
     * 消费端按 Slot 读，无需再改 Java 的 if。
     */
    private static final Map<String, Slot> KIND_SLOTS = buildSlots();

    private static Map<String, Slot> buildSlots() {
        Map<String, Slot> m = new HashMap<>();
        // ── Variant.MARKER：惰性标记，按 conditionTags 折叠的幅度之和 ──────────────
        for (String k : List.of("corruption_endures", "defend", "chance_defend",
                "take_no_volatility_damage",
                "flask_chance_gain_charge_on_kill")) {
            m.put(k, Slot.MARKER_AMOUNT);
        }
        // flask_num_charges 的 amount 经 ModStatReader#readRolledPercent 归一为分数（0.23~0.70）后按 (1+inc) 乘算，
        // 与 extra_charge_consumed（裸球数，消费端 (int) 强转）量纲不同，故不复用 MARKER_AMOUNT_NUMBER。
        m.put("flask_num_charges", Slot.MARKER_AMOUNT);
        m.put("extra_charge_consumed", Slot.MARKER_AMOUNT_NUMBER);
        m.put("offering_recent_immune", Slot.MARKER_THRESHOLD);
        // ── Variant.ROLLED：概率掷骰（SIMPLE 单次 / COUNT 保底+余概率）──────────────
        for (String k : List.of("thorns_ignore_armor", "grenade_second_activation",
                "ammo_not_expended_chance", "ailment_reapply_on_consume")) {
            m.put(k, Slot.ROLLED_CHANCE);
        }
        for (String k : List.of("additional_spawn_chance", "entity_stage_skip_chance",
                "entity_stage_advance_chance")) {
            m.put(k, Slot.ROLLED_COUNT);
        }
        // ── Variant.GATE：布尔门控（amount>0 即生效；identityTags 承载作用域身份）──────
        for (String k : List.of("invocation_energy_gate",
                "shapeshift_human_form_spell_dmg", "unarmed_quarterstaff_skill_damage",
                "flask_recovery_not_removed_when_full", "flask_recovery_can_overflow",
                "curse_target_es_recharge_disabled", "cannot_recover_health_other_than_from_health_leech",
                "energy_shield_recharge_disabled", GateTypeIds.ENERGY_SHIELD_REGEN_DISABLED,
                GateTypeIds.RUNIC_WARD_RECHARGE_DISABLED,
                GateTypeIds.ENERGY_SHIELD_CAPPED_BY_ARMOR, "arcane_surge_grants_health_regen",
                "ignore_non_negative_elemental_resistance", "ignore_equipped_movement_penalty",
                "cannot_use_charms", "cannot_dodge_roll_or_sprint", "dodge_roll_cannot_avoid_damage",
                "no_rage_effect", "blind_on_stun_taken", "deflected_cannot_inflict",
                "aura_buffs_exclude_allies", "leech_share", "ally_damage_applies_to_self",
                "flask_life_recovery_share", "projectile_random_fire", "leech_retain_while_unreserved_full",
                "charge_cost_exempt", "equip_rule_exempt", "equipment_bonus_to_minions")) {
            m.put(k, Slot.GATE);
        }
        m.put("quarterstaff_both_hands_empty", Slot.GATE_TAGGED);
        // ── Variant.MAGNITUDE：按 identityTags 折叠的幅度之和 ──────────────────────
        for (String k : List.of("thorns_damage_leeched_as_health", "arrow_travel_crit",
                "banner_expire_glory_recover", "banner_ele_res", "accuracy_penalty_at_range",
                "ignite_reflect", "flask_guard", "charm_guard", "chaos_skill_life_es_loss",
                "chaos_fire_spell_ignite_magnitude", "more_max_lightning_damage",
                "equipment_bonus_mult", Kind.IGNITE_HISTORY_FIRE_TAKEN)) {
            m.put(k, Slot.MAGNITUDE);
        }
        for (String k : List.of("additional_spawn_count", "entity_category_limit")) {
            m.put(k, Slot.MAGNITUDE_NUMBER);
        }
        m.put("invocation_trigger_interval", Slot.MAGNITUDE_SECONDS);
        m.put("shield_front_debilitate", Slot.MAGNITUDE_METRES);
        return Map.copyOf(m);
    }

    /** 某 kind 声明的 {@link Slot}；未登记的 kind（开放扩展）按其 Variant 落默认 Slot。 */
    public static Slot slotOf(String kind, Variant variant) {
        Slot slot = KIND_SLOTS.get(kind);
        return slot != null ? slot : Slot.defaultFor(variant);
    }

    /**
     * 全部已登记的 kind 身份串（= 应注册为 {@code MarkerStatEffect.CODEC} 的 JSON {@code type} id 全集）。
     *
     * <p>{@code StatEffectTypes} 据此<b>一次性循环注册</b>（循环体即
     * {@code register(markerKind, MarkerStatEffect.CODEC)}）：kind 的「语义声明」与「codec 注册」
     * 共用 {@link #KIND_SLOTS} 一张表，新增 kind 不可能只改一处而漏另一处。</p>
     */
    public static Set<String> registeredKinds() {
        return KIND_SLOTS.keySet();
    }
}
