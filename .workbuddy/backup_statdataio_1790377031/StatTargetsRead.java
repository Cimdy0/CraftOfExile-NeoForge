package com.cimdy.coe.data.codec.support.stat.effect;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.gen.StatDataIO;
import com.cimdy.coe.data.gen.stat.StatTargetGen;
import com.cimdy.coe.data.gen.stat.StatTargetIds;
import com.cimdy.coe.event.data.LoadEvent;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 引用型三段选择器（{@code type:"read"}）——仅持有路径 {@code ref}，调用时从数据包加载的
 * {@link LoadEvent#STAT_TARGETS_MAP} 取对应 {@link StatTargetsInline} 并转发全部行为
 * （与 {@link com.cimdy.coe.data.codec.support.stat.entry.StatRead} /
 * {@link StatEffectRead} 同源）。
 *
 * <p>取代 {@code Either<StatTargets, Identifier>} 外层包裹：引用能力内聚在 {@link StatTargets}
 * 类型内部（节点自身实现 read 形式），运行期消费方调用 {@link #target()} 等访问器时懒委托解析，
 * 无需感知引用形态。悬空引用在首次访问时显式报错（取代静默 null 被吞）。
 *
 * <p>缓存字段类：生成期经 {@link #ofInline} 单独生产、内容命名、去重并缓存为路径键；内联 targets 若与
 * {@link #registerPresets} 登记的具名预设（由 {@link StatTargetGen#contents()} 展开）等价则优先复用
 * 预设 id（如 {@code coe:self_only}），避免重复文件。
 *
 * <p><b>落盘责任</b>：{@code StatTargetsProvider} 只落盘 {@link StatTargetGen#contents()} 的具名预设与
 * {@link #extras()}；{@link #all()} 仅是生成期的去重查询视图，<b>没有任何 Provider 写出</b>，
 * 故经 {@link #of} 产生的非预设项在运行期 {@link #resolve()} 取不到资源（死线）。通用场景一律用
 * {@link #ofInline}（其登记项必落盘）。
 */
public record StatTargetsRead(Identifier ref) implements StatTargets {

    // 内容去重缓存：规范化 JSON → 路径键 / 已分配文件名 / 内容（键即落盘文件名）
    private static final Map<String, Identifier> JSON_TO_ID = new LinkedHashMap<>();
    private static final Map<String, String> NAMES = new LinkedHashMap<>();
    private static final Map<Identifier, StatTargets> BY_ID = new LinkedHashMap<>();

    // 具名预设：规范化 JSON → 预设 id（StatGen.targets()），优先命中以避免重复文件
    private static Map<String, Identifier> PRESET_JSON_TO_ID;

    // 生成期组合出的内联 targets（DSL 三段组合产物）：id → 内容，由 StatTargetsProvider 一并落盘
    private static final Map<Identifier, StatTargets> EXTRA = new LinkedHashMap<>();

    /** 登记具名预设（首次 {@link #ofInline} 时惰性调用；重算整表，幂等）。 */
    public static synchronized void registerPresets(Map<String, StatTargets> presets) {
        Map<String, Identifier> m = new LinkedHashMap<>();
        for (Map.Entry<String, StatTargets> e : presets.entrySet()) {
            m.putIfAbsent(StatDataIO.canonicalTargetJson(e.getValue()),
                    Identifier.fromNamespaceAndPath(CraftOfExile.MODID, e.getKey()));
        }
        PRESET_JSON_TO_ID = m;
    }

    /**
     * targets 部件生产者：优先命中具名预设（按规范化 JSON），否则内容命名；返回路径引用。
     *
     * <p><b>仅登记进 {@link #all()}，不落盘</b>：非预设内容在运行期会成为悬空引用（详见 {@link #ofInline}
     * 的落盘说明）。三段组合等通用场景请改用 {@link #ofInline}；本方法仅适用于「内容必为具名预设」的
     * 确定性调用点。
     */
    public static StatTargetsRead of(StatTargets tgt) {
        if (PRESET_JSON_TO_ID != null) {
            Identifier preset = PRESET_JSON_TO_ID.get(StatDataIO.canonicalTargetJson(tgt));
            if (preset != null) return new StatTargetsRead(preset);
        }
        Identifier id = StatDataIO.nameId(JSON_TO_ID, NAMES,
                StatDataIO.canonicalTargetJson(tgt), BY_ID, tgt, CraftOfExile.MODID);
        return new StatTargetsRead(id);
    }

    /**
     * 去重后的内联 targets 部件（不含具名预设）。<b>仅供生成期查询，不落盘</b>——{@code StatTargetsProvider}
     * 只写出 {@link StatTargetGen#contents()} 与 {@link #extras()}；需要落盘的组合项请用 {@link #ofInline}。
     */
    public static Map<Identifier, StatTargets> all() {
        return Map.copyOf(BY_ID);
    }

    /**
     * 生成期反查：把引用 id 解成三段内容（组合项 + 具名预设表）。
     * 仅在生成期可用——运行期解析走 {@link #resolve()}（读数据包）。
     */
    public static @Nullable StatTargets contentOf(@Nullable Identifier ref) {
        if (ref == null) return null;
        StatTargets extra = EXTRA.get(ref);
        return extra != null ? extra : StatTargetGen.contents().get(ref);
    }

    /**
     * 内联 targets 生产者（生成期组合三段时使用）：内容等价的具名预设直接复用其 id（零新增文件），
     * 否则内容寻址命名并登记进 {@link #extras()}。
     *
     * <p>与 {@link #of} 的差别在于<b>登记项必定落盘</b>：{@link #of} 只把内容收进 {@link #all()}，
     * 而 {@code StatTargetsProvider} 仅写出 {@link StatTargetGen#contents()} 的具名预设，故走
     * {@link #of} 产生的非预设项没有任何 Provider 写出 → 运行期 {@link #resolve()} 取不到资源，
     * 条目静默成死线。三段组合场景必须使用本方法。
     */
    public static synchronized StatTargetsRead ofInline(@Nullable StatTargets tgt) {
        if (tgt == null || tgt.isEmpty()) return new StatTargetsRead(StatTargetIds.TGT_SELF_ONLY);
        if (PRESET_JSON_TO_ID == null) registerPresets(presetNames());
        Identifier preset = PRESET_JSON_TO_ID.get(StatDataIO.canonicalTargetJson(tgt));
        if (preset != null) return new StatTargetsRead(preset);
        Identifier id = StatDataIO.nameId(JSON_TO_ID, NAMES,
                StatDataIO.canonicalTargetJson(tgt), BY_ID, tgt, CraftOfExile.MODID);
        EXTRA.putIfAbsent(id, tgt);
        return new StatTargetsRead(id);
    }

    /** 具名预设（名字 → 内容）：由 {@link StatTargetGen#contents()} 按路径名展开。 */
    private static Map<String, StatTargets> presetNames() {
        Map<String, StatTargets> m = new LinkedHashMap<>();
        StatTargetGen.contents().forEach((id, t) -> m.putIfAbsent(id.getPath(), t));
        return m;
    }

    /** 生成期组合出的内联 targets（不含具名预设；由 StatTargetsProvider 与预设一并落盘）。 */
    public static Map<Identifier, StatTargets> extras() {
        return Map.copyOf(EXTRA);
    }

    @Override
    public String type() {
        return "read";
    }

    /** 从数据包加载的预设表解析本引用指向的 {@link StatTargetsInline}（缺失则报错）。 */
    public StatTargets resolve() {
        StatTargets t = LoadEvent.STAT_TARGETS_MAP.get(this.ref);
        if (t == null) {
            throw new IllegalStateException("StatTargetsRead 引用了未加载的 stat_targets: " + this.ref);
        }
        return t;
    }

    @Override
    public @Nullable TargetSelector target() {
        return resolve().target();
    }

    @Override
    public @Nullable TargetSelector conditionTarget() {
        return resolve().conditionTarget();
    }

    @Override
    public @Nullable TargetSelector valueTarget() {
        return resolve().valueTarget();
    }

    /** 引用型 MapCodec：仅含 {@code ref}。 */
    public static final MapCodec<StatTargetsRead> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf("ref").forGetter(StatTargetsRead::ref)
    ).apply(i, StatTargetsRead::new));

    /** 引用实现对应的完整 Codec（供 {@link StatTargetsTypes} 注册表登记）。 */
    public static final com.mojang.serialization.Codec<StatTargetsRead> CODEC = MAP_CODEC.codec();
}
