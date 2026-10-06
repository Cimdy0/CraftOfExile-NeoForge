package com.cimdy.coe.data.codec.support.stat.effect.modifier;
import com.cimdy.coe.data.codec.support.stat.entry.describer.StatDescriber;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffectTypes;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.stat.entry.ValueRange;
import com.cimdy.coe.data.codec.support.stat.entry.describer.StatAmountDescriber;

import com.cimdy.coe.data.codec.support.attribute.AttributeLimiters;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.tag.DomainTags;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.event.TriggerContext;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * 属性亚型：把条目（{@link StatEntry}）的<b>唯一数值</b>以 {@code operation} 的方式加到 {@code attribute} 上。
 * <p>
 * 本类保留的非数值字段：
 * <ul>
 *   <li>{@code attribute}——<b>身份</b>：这一个数加到哪个属性上；</li>
 *   <li>{@code operation}——<b>运算</b>：这一个数怎么加（加法 / 增幅 / 更多 / 限制器运算）；</li>
 *   <li>{@code mode}——<b>模式</b>：这条修饰是「叠加(GAIN)」还是「覆盖(REPLACE)」属性原值。</li>
 *   <li>{@code penaltyRole}——<b>惩罚角色</b>：本条在聚合中是加值 / 惩罚本体 / 惩罚削减。</li>
 *   <li>（无 duration 字段：时长是 Stat 第五部分 {@link StatEntry#duration()}，对本类同样生效，见下。）</li>
 * </ul>
 * 「A 属性按系数转化为 B 属性」这类需求<b>不</b>由额外的 {@code source_attribute} 字段承担，
 * 而是直接写进 math 表达式：{@code attribute:coe:evasion * 0.08}
 * （见 {@link com.cimdy.coe.data.codec.support.math.context.query.Conversions#attributeRef}）。
 * 这样「多个变量参与计算、最终只有一个数值」由 math 统一表达，Effect 不承担计算职责。
 * <p>
 * <h2>两种模式（{@link #mode()}）</h2>
 * <ul>
 *   <li>{@code GAIN}（叠加，默认）：以 {@code operation} 把数值并入属性（加算 / 增幅 / 更多 / 限制器），
 *       与现有属性系统完全一致。</li>
 *   <li>{@code REPLACE}（覆盖）：该属性最终值被本条数值<b>替代</b>（忽略原值与其它 GAIN 条目），
 *       对应 POE 的「属性替代」语义（如「生命替代」、混沌 inoculation 把最大生命设为 1）。
 *       多条 REPLACE 取数值最大者；REPLACE 优先于 GAIN。</li>
 * </ul>
 * <p>
 * 数值 / 条件 / 选择器由外层条目 {@link StatEntry} 持有。
 *
 * <h2>两种形态（唯一区别：所属条目是否声明时长）</h2>
 * 本类同时承载「常驻属性加成」与「事件触发的临时属性授予」，二者的唯一区别是<b>所属条目是否声明时长</b>
 * （{@link StatEntry#duration()}，Stat 第五部分）：前者是纯粹的读取时折叠（fold），后者在事件时刻把数值
 * <b>物化</b>为一条无条件常驻条目写入计时域。
 * <ul>
 *   <li><b>常驻（条目无 duration）</b>：条目由 {@link StatStore} 归入
 *       {@link StatEffect.EffectKind#FOLD} 属性索引；每次 {@code getAttributeValue} 都经
 *       {@link StatEngine#fold} → {@code aggregate} <b>重新评估</b>标签门控与条件。</li>
 *   <li><b>授予（条目有 duration）</b>：条目由 {@link StatStore} 归入
 *       {@link StatEffect.EffectKind#FIRE} 触发索引；事件经 {@link GateExpr#matches} / {@link StatEngine#conditionsHold} 过筛后由
 *       {@link #onTrigger} 在<b>事件作用域</b>内求值条目数值（可引用事件参数，如 {@code $damage}），
 *       再<b>物化</b>为一条无条件常驻条目（{@link GateExpr#ALWAYS}、无 conditions、无 duration），
 *       经 {@link StatStore#addModifier(String, StatEntry, int)} 写入持有者 STAT_STORE 的计时域。
 *       该物化条目是<b>另一条</b> FOLD 条目：<b>读取时刻不再重新评估条件</b>，
 *       {@code getAttributeValue} 拿到的就是事件时刻的过筛值；{@link StatStore#tick()} 到期自动移除。</li>
 * </ul>
 *
 * <p><b>索引归属由条目层决定，不由 Effect 决定</b>：本类恒为折叠亚型（{@link #isTrigger()} 恒
 * {@code false}、{@link #indexKind()} 恒 {@link StatEffect.EffectKind#FOLD}）——Effect 只表达
 * 「赋予什么」。是否为触发型由 {@link StatStore} 构建索引时按 {@code entry.duration() != null}
 * 重分类（与 {@link StatEntry#isTrigger()} 同一判据）：折叠语义是「每次读取重估」，无法表达
 * 「持续 N tick 后消失」，故带时长的折叠条目必须走物化通道。
 *
 * <p><b>授予形态的语义细节</b>：
 * <ul>
 *   <li><b>刷新而非叠加</b>：同一来源条目重复触发经同一来源键（{@code "grant:" + store.keyOf(entry)}，
 *       条目不在 owner store 时回退 {@code identityHashCode}）覆盖——重置时长与数值，不叠层。
 *       同一条目被同一 tick 的多个事件反复触发仍只保留一份（PoE「gain buff」语义）。</li>
 *   <li><b>来源键跨 store 重建稳定</b>：键由来源规则决定（装备槽 uuid / 天赋键 / 词缀 auto_n），
 *       换装 / 天赋重载 / 状态重同步重建的是条目对象而非来源键，同一词缀重建后再次触发仍命中同一
 *       grant 键（刷新），不会产生双份叠加。</li>
 *   <li><b>无自激环</b>：物化条目不带 duration，是 FOLD 型，不回流 {@code triggersByTag}；
 *       fire 侧另有深度闸与 FIRING 栈守卫兜底。</li>
 *   <li><b>授予目标</b>：持有者自身（物化条目 targets 取空 → 默认 self）；事件目标授予不在本效果语义内。</li>
 *   <li><b>持久化</b>：物化条目与其剩余时长随 {@link StatStore#CODEC} 的 {@code effects} /
 *       {@code expiries} 字段序列化，存档重载后继续倒数（不会退化为永久加成）。</li>
 * </ul>
 *
 * <h2>{@code tags} 的分层（易误解，务必注意）</h2>
 * 本类携带的 {@link #tags()} <b>不是</b>「效果自身的触发条件」，而是<b>所属 stat 条目的作用域门控</b>——
 * 即「这条词缀在哪些作用域下计入」。同一份门控经 {@link StatEntry#tags()}（默认方法，直接转发到
 * {@code effect().tags()}）读取，故本字段是该条目<b>唯一的门控存储</b>：
 * <ul>
 *   <li>常驻形态：{@code StatEngine.aggregate} 经 {@link StatEntry#effectiveTags()} 求值，每次读取都判；</li>
 *   <li>授予形态：{@code StatEngine.fire} 经 {@link StatEntry#tags()} 过筛是否触发。</li>
 * </ul>
 * 字段的物理存放位置（效果上）与逻辑归属（条目上）不一致，是已知的分层遗留：把字段从效果移到条目
 * 需改动全部效果子类并重生成全部 stat JSON，超出本类改造范围。读取门控请一律经
 * {@link StatEntry#tags()} / {@link StatEntry#effectiveTags()}，不要把 {@code effect().tags()}
 * 当作独立于条目的门控来读。
 */
public class ModifierStatEffect extends AbstractStatEffect {

    /**
     * <b>无时长声明时的刷新窗口</b>（tick）：Presence 光环是<b>集合关系而非倒计时</b>——
     * 驱动每刻施加一次并按此窗口写入目标 store，靠不断刷新维持存在；
     * 目标离开范围（或光环关闭）后停止刷新，1~2 tick 内自然过期。
     * 窗口必须取小值：若用大时长，目标「出了范围还顶着加成」——那正是本通道要避免的残留 bug。
     */
    private static final int REFRESH_WINDOW_TICKS = 2;

    private final Holder<Attribute> attribute;
    private final Either<AttributeModifier.Operation, AttributeLimiters.Operation> operation;
    private final Mode mode;
    private final PenaltyRole penaltyRole;
    /**
     * 规范构造：身份（属性）+ 运算 + 模式 + 惩罚角色 + 作用域标签。
     * <p>本类不持有时长：时长是 Stat 第五部分（{@link StatEntry#duration()}），
     * 由条目层决定本条是常驻折叠还是事件物化（见类注释「两种形态」）。
     * <p>{@code penaltyRole} 标记本条在 {@link com.cimdy.coe.data.codec.support.stat.engine.StatEngine} 聚合中的语义：
     * {@link PenaltyRole#REDUCTION} 条目不进入普通加值池，而是被累加为当前作用域的「惩罚削减因子」，
     * 仅对同属性下的<b>负值</b>（惩罚）贡献做 {@code (1 - 因子)} 二次缩放（详见 {@code StatEngine#fold}）；
     * 正向池与无惩罚时均不受影响。普通条目用 {@link PenaltyRole#NONE}。</p>
     * <p><b>语义自洽校验</b>：{@link PenaltyRole#PENALTY} 仅允许配 {@code ADD_MULTIPLIED_BASE}——
     * 这是 {@code StatEngine#accumulate} 中唯一识别惩罚角色的分支，与 {@link PenaltyRole}的契约一致。
     * 若搭配其他运算，则本条会被静默计入正向池（负贡献反成增益），
     * 故在构造期即拒绝（参 {@link MarkerStatEffect} 的 KIND_SLOTS 同类前例）。</p>
     */
    public ModifierStatEffect(Holder<Attribute> attribute,
                              Either<AttributeModifier.Operation, AttributeLimiters.Operation> operation,
                              Mode mode,
                              PenaltyRole penaltyRole,
                              GateExpr tags) {
        super(tags);
        this.attribute = attribute;
        this.operation = operation;
        this.mode = mode;
        this.penaltyRole = penaltyRole;
        boolean penaltyOnUnsupportedOp = penaltyRole == PenaltyRole.PENALTY
                && !operation.left().map(AttributeModifier.Operation.ADD_MULTIPLIED_BASE::equals).orElse(Boolean.FALSE);
        if (penaltyOnUnsupportedOp) {
            throw new IllegalArgumentException("ModifierStatEffect 声明 penaltyRole=\"penalty\" 但 operation=" + operation
                    + "；惩罚本体仅在 add_multiplied_base 下才落入独立惩罚子池，其余运算 "
                    + "（add_value / add_multiplied_total / 限制器）在 StatEngine.accumulate 中不识别惩罚角色， "
                    + "会把本条静默计入正向加值池（负贡献反成增益）。");
        }
    }

    /** 带作用域标签，默认 {@link PenaltyRole#NONE}。 */
    public ModifierStatEffect(Holder<Attribute> attribute,
                              Either<AttributeModifier.Operation, AttributeLimiters.Operation> operation,
                              Mode mode,
                              GateExpr tags) {
        this(attribute, operation, mode, PenaltyRole.NONE, tags);
    }

    /** 无事件标签门控（{@link GateExpr#ALWAYS}）便捷构造，默认 {@link PenaltyRole#NONE}。 */
    public ModifierStatEffect(Holder<Attribute> attribute,
                              Either<AttributeModifier.Operation, AttributeLimiters.Operation> operation,
                              Mode mode) {
        this(attribute, operation, mode, PenaltyRole.NONE, GateExpr.ALWAYS);
    }

    public Holder<Attribute> attribute() {
        return attribute;
    }

    /**
     * 领域标签（D6 终态）：显式字段优先；否则先试事件标签派生（{@link AbstractStatEffect#domainTags}），
     * 仍为空则按<b>修饰的属性</b>派生域（+X 闪避 / 护盾 / 格挡 / 抗性 / 魔力等 FOLD 效果其事件标签通常为
     * {@link GateExpr#ALWAYS}，无域 token，但属性本身即域信号）。详见 {@link DomainTags#fromAttribute}。
     */
    @Override
    public Set<String> domainTags() {
        Set<String> base = super.domainTags();
        return base.isEmpty() ? DomainTags.fromAttribute(attribute) : base;
    }

    public Either<AttributeModifier.Operation, AttributeLimiters.Operation> operation() {
        return operation;
    }

    public Mode mode() {
        return mode;
    }

    /** 惩罚角色：{@link PenaltyRole#NONE} 普通加值；{@link PenaltyRole#PENALTY} 惩罚本体（落入独立惩罚子池）；{@link PenaltyRole#REDUCTION} 削减惩罚（仅缩放惩罚子池）。 */
    public PenaltyRole penaltyRole() {
        return penaltyRole;
    }

    /**
     * 返回仅替换作用域门控（{@code tags}）的副本，<b>其余字段逐一原样保留</b>——
     * 含 {@code mode} / {@code penaltyRole}。供需要「给已有效果加一个标签」的调用方使用
     * （如技能 action 的去重标记 tag），避免手工重建时丢字段。
     */
    public ModifierStatEffect withTags(GateExpr newTags) {
        return new ModifierStatEffect(attribute, operation, mode, penaltyRole, newTags);
    }

    /** 修饰模式：GAIN 叠加（默认）/ REPLACE 覆盖原值。 */
    public enum Mode implements StringRepresentable {
        GAIN,
        REPLACE;

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * 惩罚角色：标记本条 modifier 在 {@link com.cimdy.coe.data.codec.support.stat.engine.StatEngine} 聚合中的语义。
     * <ul>
     *   <li>{@code NONE}（默认）：普通加值，按 {@code operation} 进入对应加值池。</li>
     *   <li>{@code PENALTY}：惩罚本体。本条 {@code ADD_MULTIPLIED_BASE} 贡献（值已取负）不进入正向
     *       {@code multBase} 池，而是落入独立的<b>惩罚子池</b>（{@code StatEngine.StatResult#penaltyMult}），
     *       仅受 {@code REDUCTION} 因子的二次缩放，永不污染正向加值侧（方案 A）。</li>
     *   <li>{@code REDUCTION}：削减惩罚。本条数值（应为正，代表「减少 X% 惩罚」）不进入普通加值池，
     *       而在 {@code StatEngine.aggregate} 阶段被累加为当前作用域的惩罚削减因子（{@code penaltyReduce}），
     *       仅对独立惩罚子池（{@code penaltyMult}）做 {@code (1 - 因子)} 二次缩放，
     *       使惩罚变轻而绝不反向增益正向池（杜绝「无惩罚时持有削减天赋白送增益」的泄漏）。</li>
     * </ul>
     */
    public enum PenaltyRole implements StringRepresentable {
        NONE,
        PENALTY,
        REDUCTION;

        public static final Codec<PenaltyRole> CODEC = StringRepresentable.fromEnum(PenaltyRole::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    // ---------------------------------------------------------------- 条目便捷构造

    /** 便捷构造：常量 / 表达式数值，产出一条完整条目（作用于自身）。 */
    public static StatRead entry(Holder<Attribute> attribute, ExpressionCodec.Value amount,
                                  Either<AttributeModifier.Operation, AttributeLimiters.Operation> operation,
                                  Set<String> tags, List<ValueExpr> conditions) {
        return StatEntry.of(amount, conditions, new ModifierStatEffect(attribute, operation, Mode.GAIN, GateExpr.of(tags)));
    }

    /** 便捷构造：常量数值 + 原版运算，产出一条无标签无条件的条目。 */
    public static StatRead entry(Holder<Attribute> attribute, double amount,
                                  AttributeModifier.Operation operation) {
        return entry(attribute, new ExpressionCodec.Value(Either.left(amount)),
                Either.left(operation), Set.of(), List.of());
    }

    /** 便捷构造：常量数值 + 原版运算 + 标签作用域，产出一条无条件的条目。 */
    public static StatRead entry(Holder<Attribute> attribute, double amount,
                                  AttributeModifier.Operation operation, Set<String> tags) {
        return entry(attribute, new ExpressionCodec.Value(Either.left(amount)),
                Either.left(operation), tags, List.of());
    }

    // ---------------------------------------------------------------- 编解码 / 元信息

    public static final Codec<Either<AttributeModifier.Operation, AttributeLimiters.Operation>> EITHER_CODEC =
            Codec.either(AttributeModifier.Operation.CODEC, AttributeLimiters.Operation.CODEC);

    public static final MapCodec<ModifierStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(ModifierStatEffect::attribute),
            EITHER_CODEC.fieldOf("operation").forGetter(ModifierStatEffect::operation),
            Mode.CODEC.optionalFieldOf("mode", Mode.GAIN).forGetter(ModifierStatEffect::mode),
            PenaltyRole.CODEC.optionalFieldOf("penalty_role", PenaltyRole.NONE).forGetter(ModifierStatEffect::penaltyRole),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, ModifierStatEffect::decode));

    private static ModifierStatEffect decode(Holder<Attribute> attribute,
                                             Either<AttributeModifier.Operation, AttributeLimiters.Operation> operation,
                                             Mode mode,
                                             PenaltyRole penaltyRole,
                                             GateExpr tags) {
        return new ModifierStatEffect(attribute, operation, mode, penaltyRole, tags);
    }

    /** 属性亚型效果类型 id（写入 JSON 的 "type" 字段，供 {@link StatEffectTypes} 分发）。 */
    @Override
    public String type() {
        return "attribute";
    }

    /**
     * 恒 {@code false}：本类是<b>折叠亚型</b>，Effect 只表达「赋予什么」。
     * <p>是否为触发型由<b>条目层</b>决定——{@link StatStore} 构建索引时按
     * {@code entry.duration() != null} 把带时长的本类条目重分类进
     * {@link StatEffect.EffectKind#FIRE}（见 {@link StatEntry#isTrigger()}，两处同一判据）。
     */
    @Override
    public boolean isTrigger() {
        return false;
    }

    /**
     * 触发型执行体（仅「条目声明了时长」的条目会到达这里，由 {@link StatStore} 索引重分类保证）：
     * 把条目数值在<b>事件作用域</b>内求值（fire 已过筛门控，表达式可引用事件参数如 {@code $damage}），
     * <b>物化</b>为一条无条件的常驻形态条目（{@link GateExpr#ALWAYS}、无 conditions、无 duration
     * → FOLD 型），带时长写入持有者 STAT_STORE 的计时域，由 {@link StatStore#tick()} 到期移除。
     * <p>时长取条目第五部分 {@link StatEntry#durationTicks()}；未声明时长则拒绝物化——
     * 若改写入「常驻」条目，则授予永不衰减（静默 bug），故宁可不施加。
     * <p>同来源键覆盖 → <b>刷新而非叠加</b>（重置时长与数值）。物化条目不带 duration，
     * 故不再回流触发索引，无自激环。
     */
    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        int ticks = entry.durationTicks();
        if (ticks <= 0) return;
        LivingEntity owner = evalCtx.owner();
        LivingEntity valEntity = evalCtx.resolve(entry.valueTarget());
        if (valEntity == null) return;
        double value = entry.evaluateAmount(scope.evalBase(valEntity));
        // 物化：无门控的常驻形态条目（读取端直读，不重估条件），带时长入计时域。
        // mode / penaltyRole 随源条目一并继承——授予的是「同一条修饰，只是有时限」。
        StatEntry granted = StatEntry.of(ExpressionCodec.of(value),
                new ModifierStatEffect(attribute, operation, mode, penaltyRole, GateExpr.ALWAYS));
        StatStore store = owner.getData(AttachmentRegistry.STAT_STORE);
        String src = store.keyOf(entry);
        String key = src != null ? "grant:" + src
                : "grant:" + Integer.toHexString(System.identityHashCode(entry));
        owner.setData(AttachmentRegistry.STAT_STORE, store.addModifier(key, granted, ticks));
    }

    /**
     * 周期授予（由 {@link com.cimdy.coe.data.codec.support.stat.effect.grant.MultiStatEffect} 调用）：
     * 把本修饰物化为一条<b>无条件常驻（FOLD）</b>条目，写入 {@code entry.target()} 解析出的
     * <b>每个目标自身的</b> STAT_STORE 计时域，时长取条目第五部分 {@link StatEntry#durationTicks()}；
     * 条目未声明时长时按刷新窗口 {@link #REFRESH_WINDOW_TICKS} 写入（每刻刷新，离开范围即过期）。
     * 键含 owner+target+attr+tags，刷新而非叠加。与旧 {@code PeriodicDriver.grantAttribute} /
     * {@code PresenceAura.grantAttribute} 同语义：目标离开 Presence 后不再刷新，由
     * {@code StatStore.tick()} 计时域到期自灭。
     * <p>仅作用于在场目标（跳过 owner 与死亡目标）；自授予（target=SELF）经 {@link StatEntry#forEachTarget}
     * 自然落到 owner 自身 store，零回归。
     */
    public void grantPeriodic(StatEntry entry, StatContext scope, StatEvalContext evalCtx) {
        // 时长取条目第五部分（Stat 统一提供）：已声明时长 → 沿用其 tick 数（原路径不变，
        // INTERVAL 语义下「每次持续 = 节拍」，与 StatDuration.Kind#INTERVAL 的无缝续期一致）；
        // 未声明时长 → 取刷新窗口，由驱动的逐刻刷新维持存在，目标离开范围后随即过期。
        // 不可在此处「拒绝授予」：那样所有未声明时长的 Presence 光环条目会静默失效。
        int declared = entry.durationTicks();
        int durationTicks = declared > 0 ? declared : REFRESH_WINDOW_TICKS;
        LivingEntity owner = evalCtx.owner();
        double value = entry.evaluateAmount(scope.evalBase(owner));
        // 作用域取自<b>本子效果自身</b>的 tags（不是外层复合条目的）：复合容器已不持 tags，
        // 而「授予什么作用域的属性」本来就是这条修饰自己的语义（如 {damage} / {health,regen}）。
        StatEntry granted = StatEntry.of(ExpressionCodec.of(value),
                new ModifierStatEffect(attribute, operation, mode, penaltyRole, tags()));
        Set<String> tags = tags().leaves();
        String sorted = String.join(",", new TreeSet<>(tags));
        entry.forEachTarget(evalCtx, scope, t -> {
            if (t == owner || t.isDeadOrDying()) return;
            String key = "periodic:" + owner.getUUID() + ":" + t.getUUID() + ":"
                    + attribute.value().getDescriptionId() + ":" + sorted;
            StatStore tstore = t.hasData(AttachmentRegistry.STAT_STORE)
                    ? t.getData(AttachmentRegistry.STAT_STORE) : StatStore.empty();
            t.setData(AttachmentRegistry.STAT_STORE, tstore.addModifier(key, granted, durationTicks));
        });
    }

    /** UI 展示文本（单条属性加成描述，数值取自所属条目）。经 {@link StatDescriber} 装配，本方法只产出「效果片段」。 */
    @Override
    public List<Component> describeFragment(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    /**
     * 本地化效果片段（P3）：REPLACE / 常量 / limiter 分支逐字保留（零回归）；
     * 表达式数值（非常量、原版运算）分支尝试 per-X 反汇编，让幅度可见，失败回落 {@code stat.amount.unknown}。
     */
    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        // 授予形态（条目带时长）：描述「临时获得 X，持续 N tick」——含时长，与常驻形态的纯属性加成文案不同。
        if (entry.duration() != null) {
            return List.of(Component.translatable("stat_effect.grant_stat",
                    StatValueKey.ofKey(attribute.value().getDescriptionId(), attribute.getKey().identifier().getPath()),
                    Component.literal(operation.toString()),
                    Component.literal(String.valueOf(entry.amount())),
                    Component.literal(String.valueOf(entry.durationTicks()))));
        }
        // 属性名键 = Attribute 自带的 descriptionId（"attribute.name." + 注册名，见 AttributeRegistry）。
        // 陷阱：不可用 "attribute.name." + attribute.getKey()，那是 ResourceKey#toString()
        // （"ResourceKey[minecraft:attribute / coe:strength]"），恒不命中任何键 → 渲染裸 key；
        // 且与 AmountSourceNamer 的 per-X 来源名不一致。getDescriptionId() 与后者逐字一致。
        Component name = Component.translatable(attribute.value().getDescriptionId());
        Optional<Double> constant = entry.amount().value().left();
        Component line;
        if (mode == Mode.REPLACE) {
            // stat.op.replace = "replaces %s (%s)"，参数：属性名 / 数值（已 trim）。
            line = Component.translatable("stat.op.replace", name,
                    Component.literal(trim(entry.amount().getValue())));
        } else if (constant.isPresent() && operation.left().isPresent()) {
            double v = constant.get();
            AttributeModifier.Operation op = operation.left().get();
            line = switch (op) {
                // 运算片段键只含数值（符号已内联进参数），属性名在片段后追加（" 属性名"）。
                case ADD_VALUE -> Component.translatable("stat.op.add_value", signedNumber(v), name);
                case ADD_MULTIPLIED_BASE ->
                        Component.translatable("stat.op.add_multiplied_base", signedNumber(v * 100), name);
                case ADD_MULTIPLIED_TOTAL ->
                        Component.translatable("stat.op.add_multiplied_total", Component.literal(trim(v * 100)), name);
            };
        } else if (operation.left().isPresent()) {
            // 表达式数值（非常量）的修饰符：尝试 per-X 反汇编，让幅度可见（P3）。
            AttributeModifier.Operation op = operation.left().get();
            StatAmountDescriber.PerXInfo raw = entry.amount().value().right().isPresent()
                    ? StatAmountDescriber.detectPerX(entry.amount().value().right().get())
                    : null;
            if (raw != null) {
                StatAmountDescriber.PerXInfo info = StatAmountDescriber.resolveValues(raw, entry.params());
                Component value = StatAmountDescriber.valueFragment(info, locale, op);
                Component per = StatAmountDescriber.perClause(info, locale,
                        op != AttributeModifier.Operation.ADD_VALUE);
                // 语序交给 stat.fmt.modifier：英文「+1% Damage per 15 Strength」/ 中文「每 15 力量，伤害 +1%」。
                line = Component.translatable("stat.fmt.modifier",
                        Component.translatable(opKey(op), value, name), per);
            } else {
                line = withName(Component.translatable("stat.amount.unknown"), name);
            }
        } else if (operation.right().isPresent()) {
            // 限制器运算：stat.op.limiter = "%s (%s)"，参数：属性名 / 序列化运算名。
            line = Component.translatable("stat.op.limiter", name,
                    Component.literal(operation.right().get().getSerializedName()));
        } else {
            line = name;
        }
        if (penaltyRole == PenaltyRole.REDUCTION) {
            // 削减惩罚：本条数值为「减少 X% 惩罚」，仅缩同属性负值贡献，UI 须与正向增益区分。
            line = Component.translatable("stat.op.reduced_penalty", line);
        }
        return List.of(line);
    }

    /**
     * 本地化效果片段（范围模式，重中之重）：仅「单值常量 + 原版运算」分支注入区间，
     * 渲染 {@code value(min-max)}（如 {@code +4(3-5)}）；其余分支（REPLACE / limiter / 表达式 per-X /
     * 削减惩罚）回落 2 参数版本，零回归。
     *
     * <p>区间与数值「同口径」缩放：常量(ADD_VALUE)区间因子 1，百分比(ADD_MULTIPLIED_*)区间×100，
     * 与 {@code signedNumber(v*100)} 等数值渲染口径严格对齐（计算方法核心）。
     */
    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale, Optional<ValueRange> range) {
        // 范围模式仅在单值常量 + 原版运算分支有意义；其余一律旧行为。
        // 授予形态（条目带时长）回落 2 参数版本——其片段自带时长，与区间无关。
        if (entry.duration() != null || range.isEmpty() || mode == Mode.REPLACE || penaltyRole == PenaltyRole.REDUCTION
                || operation.right().isPresent() || entry.amount().value().left().isEmpty()
                || operation.left().isEmpty()) {
            return describeFragment(entry, locale);
        }
        Component name = Component.translatable(attribute.value().getDescriptionId());
        double v = entry.amount().value().left().get();
        AttributeModifier.Operation op = operation.left().get();
        Component line = switch (op) {
            // ADD_VALUE：数值×1，区间因子 1；带符号前缀。
            case ADD_VALUE ->
                    Component.translatable("stat.op.add_value", valuePart(v, true, locale, range, 1.0), name);
            // ADD_MULTIPLIED_BASE：数值×100，区间因子 100；带符号前缀。
            case ADD_MULTIPLIED_BASE ->
                    Component.translatable("stat.op.add_multiplied_base", valuePart(v * 100, true, locale, range, 100.0), name);
            // ADD_MULTIPLIED_TOTAL：数值×100，区间因子 100；不带符号前缀（与 2 参数 trim 一致）。
            case ADD_MULTIPLIED_TOTAL ->
                    Component.translatable("stat.op.add_multiplied_total", valuePart(v * 100, false, locale, range, 100.0), name);
        };
        return List.of(line);
    }

    /**
     * 值渲染拆离（重中之重）：产出最终数值片段。
     * 范围模式渲染 {@code value(min-max)}；单值模式退回裸 {@code value}。
     *
     * @param scaledValue 已按运算口径缩放后的数值（ADD_VALUE=原值 / 百分比=×100）
     * @param signed      是否带符号前缀（+ 加 / - 沿用数值自带）
     * @param locale      数字本地化 locale
     * @param range       词缀 roll 区间（来自 Addition.min~max），空=聚合模式
     * @param scaleFactor 区间缩放因子（与数值同口径：百分比=100，常量=1）
     */
    private Component valuePart(double scaledValue, boolean signed, Locale locale,
                                Optional<ValueRange> range, double scaleFactor) {
        String valueStr = signed ? signedTrim(scaledValue, locale) : formatNumber(scaledValue, locale);
        if (range.isEmpty()) return Component.literal(valueStr);
        ValueRange r = range.get();
        String rangeStr = rangeString(r.min() * scaleFactor, r.max() * scaleFactor, locale);
        return Component.literal(valueStr + "(" + rangeStr + ")");
    }

    /** 带符号数值片段（正加 "+"，负沿用 formatNumber 自带 "-"，与旧 signedNumber 一致）。 */
    private static String signedTrim(double v, Locale locale) {
        return (v >= 0 ? "+" : "") + formatNumber(v, locale);
    }

    /** locale 感知数字格式：整数走紧凑 long 串（POE 风格无千分位），小数走 locale 小数分隔符。 */
    private static String formatNumber(double v, Locale locale) {
        if (v == Math.rint(v)) return String.valueOf((long) v);
        java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(locale);
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(6);
        return nf.format(v);
    }

    /** 区间串：任一端为负时用空格隔离分隔符，避免 "-3-5" 歧义（negative-range 防御）。 */
    private static String rangeString(double min, double max, Locale locale) {
        String a = formatNumber(min, locale);
        String b = formatNumber(max, locale);
        return (min < 0 || max < 0) ? a + " - " + b : a + "-" + b;
    }

    /** 效果片段后追加属性名（" 属性名"）。 */
    /** 运算片段键：统一为「%1$s=数值（已含正负号） / %2$s=属性名」两参数，语序由各 locale 决定。 */
    private static String opKey(AttributeModifier.Operation op) {
        return switch (op) {
            case ADD_VALUE -> "stat.op.add_value";
            case ADD_MULTIPLIED_BASE -> "stat.op.add_multiplied_base";
            case ADD_MULTIPLIED_TOTAL -> "stat.op.add_multiplied_total";
        };
    }

    private static Component withName(MutableComponent opFragment, Component name) {
        return opFragment.append(Component.literal(" ")).append(name);
    }

    /** 带符号数值片段（正加 "+"，负沿用 trim 自带 "-"），与旧 describe 的 (v>=0?"+":"")+trim(v) 一致。 */
    private static Component signedNumber(double v) {
        return Component.literal((v >= 0 ? "+" : "") + trim(v));
    }

    /** UI 展示文本（兼容旧调用点，统一经 {@link #describeFragment}）。 */
    @Override
    public List<Component> describe(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    private static String trim(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    /**
     * 显式比较 {@code operation}：{@link Either} 默认 equals 是身份级（同值不同实例判不等），
     * 会导致「同内容不同对象」的两条 effect 被认为不等。这里按左/右分支解包后比较枚举引用（枚举单例，
     * 引用相等即语义相等），使内容寻址（intern / 内容签名去重）能正确归并共享条目
     * （如 {@code livingmod_resist_fire_20} 被 nether / hot 两处同参复用）。
     */
    private boolean opEquals(Either<AttributeModifier.Operation, AttributeLimiters.Operation> other) {
        if (operation.left().isPresent() && other.left().isPresent())
            return operation.left().get() == other.left().get();
        if (operation.right().isPresent() && other.right().isPresent())
            return operation.right().get() == other.right().get();
        return false; // 一侧 left、一侧 right：运算不同
    }

    private int opHash() {
        return operation.left().<Integer>map(Object::hashCode)
                .orElseGet(() -> operation.right().map(Object::hashCode).orElse(0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModifierStatEffect that)) return false;
        return Objects.equals(attribute, that.attribute)
                && opEquals(that.operation)
                && mode == that.mode
                && penaltyRole == that.penaltyRole
                && Objects.equals(tags(), that.tags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(attribute, opHash(), mode, penaltyRole, tags());
    }

    @Override
    public String toString() {
        return "ModifierStatEffect[" + attribute.getKey() + ", " + operation + ", " + mode + ", penaltyRole=" + penaltyRole + ", tags=" + tags() + "]";
    }
}
