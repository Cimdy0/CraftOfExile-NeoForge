package com.cimdy.coe.data.gen.stat.duration;

import com.cimdy.coe.data.codec.support.stat.duration.StatDuration;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Stat duration 段（条目第五部分 · 时长段）具名内容表：每个具名 duration 一条，
 * 由 {@link com.cimdy.coe.data.gen.provider.StatDurationProvider} 落盘为
 * {@code stat/duration/<id>.json}，运行期由 {@code LoadEvent} 扫描 {@code coe/stat/duration}
 * 装入 {@code STAT_DURATION_MAP}，供条目的 {@code "duration": "read:coe:<id>"} 解析。
 *
 * <p>与 {@link com.cimdy.coe.data.gen.stat.effect.StatEffectGen} /
 * {@link com.cimdy.coe.data.gen.stat.StatEntityConditionGen} 同构：内容表在此、落盘在 Provider、
 * 身份取 {@link StatDurationIds} 的具名常量，全程 name-keyed 引用，不做内容哈希。
 *
 * <h2>登记依据：按「{@code Kind} 有无读点」，而非按这些具名 id 有无消费者</h2>
 * 两件事必须分开，本表只对前者有发言权：
 * <ul>
 *   <li><b>{@code Kind}（时间语义标签）确有读点</b>——「某个 kind 的数字有没有被消费」取决于
 *       <b>读 kind 的通用读点</b>，而不是读某个具名 id 的消费者。按读点判定：
 *       <ul>
 *         <li>{@link StatDuration.Kind#DURATION} ——读点 {@code StatusStatEffect#durationFallback}
 *             （StatusStatEffect.java:283：只有 DURATION 才覆盖状态自带有效期），故登记。</li>
 *         <li>{@link StatDuration.Kind#INTERVAL} ——读点 {@code PeriodicDriver#intervalOf}
 *             （PeriodicDriver.java:63-70），故登记。</li>
 *         <li>{@link StatDuration.Kind#COOLDOWN} ——<b>无读点，故不登记</b>：
 *             {@code PeriodicDriver#intervalOf} 只认 INTERVAL、
 *             {@code StatusStatEffect#durationFallback} 只认 DURATION，二者都显式把它排除
 *             （见 PeriodicDriver.java:58 与 StatusStatEffect.java:276-279）。
 *             项目纪律禁止「为点亮造词缀」，故不为其硬造具名项。</li>
 *       </ul></li>
 *   <li><b>但这 5 个具名 id 当前无 in-repo 消费者</b>——{@code STAT_DURATION_MAP} 目前<b>没有任何
 *       活跃读点</b>：工程内没有任何条目以 {@code "duration": "read:coe:<id>"} 引用它们，
 *       落盘的 5 个 JSON 属<b>预留扩展点（只写不读）</b>；工程内既有时长全部走内联形态
 *       （{@code StatBuilder#duration(int)}、{@code SkillBanner.buildAuraEntry} 等）。
 *       因此下述「工程内既有真实取值」只表示<b>数字本身有出处</b>，
 *       <b>不表示这些具名 id 已被引用</b>；登记它们是为预留扩展点，不是为点亮消费者。</li>
 * </ul>
 *
 * <p><b>各值的数字出处</b>（仅表示数字有出处，<b>不表示对应具名 id 已有消费者</b>）：
 * {@link StatDurationIds#DUR_MAIM_4S} ← SpiritGreenGen.java:189（POE2 真值「Maim lasts for
 * 4 seconds」）、{@link StatDurationIds#DUR_WITHERED_8S} ← WireAilment.java:386（注释 :379
 * 「8 秒 = 160 tick」）、{@link StatDurationIds#INT_AURA_5T} ← SkillBanner.java:63
 * {@code AURA_INTERVAL = 5}、{@link StatDurationIds#INT_CRIT_WEAKNESS_14T} ← SpiritBlueGen.java:208
 * （malice「每 (0.7—0.51) 秒」）、{@link StatDurationIds#INT_WITHERED_36T} ← SpiritBlueGen.java:163
 * （withering_presence「每 (1.8—1.42) 秒」）。
 * <br><b>其中一处取证跨字段</b>：{@link StatDurationIds#INT_AURA_5T} 的真实生产者是条目的
 * <b>{@code interval} 字段</b>——SkillBanner.java:277 与 :288 都是
 * {@code withInterval(StatDuration.of(Kind.INTERVAL, AURA_INTERVAL))}，<b>不是</b>
 * {@code withDuration(...)}；而 {@code PeriodicDriver#intervalOf} <b>优先读
 * {@code entry.interval()}</b>（:65-66），仅在 interval 缺失时才回落 {@code entry.duration()}
 * （:67-69）。故该值属跨字段借用：kind 语义能工作，但取证链不精确；
 * {@code AURA_INTERVAL} 同时也用于 SkillBanner.java:151 的实体自身节拍
 * （{@code this.tickCount % AURA_INTERVAL == 0}）。
 *
 * <p><b>完备性的确切边界</b>：本表的 key 集合与 {@link StatDurationIds#idsOf(StatDuration.Kind)}
 * 的全域并集<b>由 {@link #verifyCoverage} 在每次 {@code contents()} 调用时核对</b>，不一致即抛
 * {@link IllegalStateException}（运行期，双向报缺失）；{@code idsOf} 本身是<b>穷尽无 default</b>的
 * switch，{@code Kind} 新增取值即编译失败（编译期）。两者合起来保证的只是「{@code Kind} 加值 →
 * 两张表同步」：编译期保证 {@code idsOf} 不漏新 {@code Kind}，运行期保证 {@code contents()}
 * 与 {@code idsOf} 不漂移。<b>但二者都不保证「每个 {@code Kind} 至少登记一项」</b>——
 * {@code idsOf} 写 {@code case X -> List.of()} 会静默通过（{@code COOLDOWN} 正是如此，且系刻意），
 * 故「某 kind 其实有读点却漏登记」不在本自检的覆盖范围内。
 */
public final class StatDurationGen {
    private StatDurationGen() {
    }

    /**
     * 具名 duration 内容表（key为 {@code coe:<path>}，value 为 {@link StatDuration}）。
     *
     * <p>只收 {@link StatDuration.Kind#DURATION} 与 {@link StatDuration.Kind#INTERVAL}
     * 两个「kind 层有读点」的语义；COOLDOWN 无读点故为空（依据见类注释）。
     *
     * <p>再次提醒：本表产出的 5 个 id <b>当前无 in-repo 消费者</b>（无任何条目以 {@code read:}
     * 引用），落盘文件属预留扩展点。
     */
    public static Map<Identifier, StatDuration> contents() {
        Map<Identifier, StatDuration> m = new LinkedHashMap<>();

        // ── DURATION（读点：StatusStatEffect#durationFallback，覆盖状态自带有效期）──
        // 4 秒：SpiritGreenGen caltropMaim —— POE2 真值「Maim lasts for 4 seconds」= 80 tick。
        m.put(StatDurationIds.DUR_MAIM_4S, StatDuration.of(StatDuration.Kind.DURATION, 80));
        // 8 秒：WireAilment 凋零光环 —— 注释「8 秒：StatDuration(Kind.DURATION, 160)」= 160 tick。
        m.put(StatDurationIds.DUR_WITHERED_8S, StatDuration.of(StatDuration.Kind.DURATION, 160));

        // ── INTERVAL（读点：PeriodicDriver#intervalOf，每 N tick 重新施加一次）──
        // 战旗光环刷新节拍 = 5：SkillBanner.AURA_INTERVAL。
        // 取证跨字段：真实生产者是条目的 interval 字段（:277 / :288 用 withInterval(...)
        // 而非 withDuration(...)），而 intervalOf 优先读 entry.interval()（PeriodicDriver:65-66）。
        // 该常量同时用于 SkillBanner:151 的实体自身节拍；详见类注释「各值的数字出处」。
        m.put(StatDurationIds.INT_AURA_5T, StatDuration.of(StatDuration.Kind.INTERVAL, 5));
        // 0.7 秒节拍：SpiritBlueGen malice —— POE2 真值「Applies Critical Weakness every (0.7—0.51) seconds」。
        m.put(StatDurationIds.INT_CRIT_WEAKNESS_14T, StatDuration.of(StatDuration.Kind.INTERVAL, 14));
        // 1.8 秒节拍：SpiritBlueGen withering_presence —— POE2 真值「Withers ... every (1.8—1.42) seconds」。
        m.put(StatDurationIds.INT_WITHERED_36T, StatDuration.of(StatDuration.Kind.INTERVAL, 36));

        verifyCoverage(m);
        return m;
    }

    /**
     * 完备性自检：{@code contents()} 的 key 集合必须与 {@link StatDurationIds#idsOf(StatDuration.Kind)}
     * 的全域并集<b>逐字相等</b>。双向报缺失，避免「加了内容忘了更新 idsOf 的穷尽 switch」
     * 与「idsOf 列了名字但 contents 没造值」两类漂移都只在运行期才暴露。
     */
    private static void verifyCoverage(Map<Identifier, StatDuration> m) {
        Set<Identifier> declared = new LinkedHashSet<>();
        for (StatDuration.Kind kind : StatDuration.Kind.values()) {
            declared.addAll(StatDurationIds.idsOf(kind));
        }
        Set<Identifier> actual = new LinkedHashSet<>(m.keySet());
        if (declared.equals(actual)) return;
        Set<Identifier> missingInContents = new LinkedHashSet<>(declared);
        missingInContents.removeAll(actual);
        Set<Identifier> missingInIds = new LinkedHashSet<>(actual);
        missingInIds.removeAll(declared);
        throw new IllegalStateException("StatDurationGen.contents() 与 StatDurationIds.idsOf 漂移：\n  "
                + "idsOf 已列但 contents() 未登记: " + missingInContents + "\n  "
                + "contents() 已登记但 idsOf 未列: " + missingInIds);
    }
}