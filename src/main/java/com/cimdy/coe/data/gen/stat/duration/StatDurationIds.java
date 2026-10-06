package com.cimdy.coe.data.gen.stat.duration;

import com.cimdy.coe.data.codec.support.stat.duration.StatDuration;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 具名时长段（Stat 第五部分 · duration 段）的<b>引用身份</b>表：每个常量一个
 * {@code coe:<path>} id，供条目以 {@code "duration": "read:coe:<path>"} 引用。
 *
 * <p>与 {@link com.cimdy.coe.data.gen.stat.effect.StatEffectIds} / {@link com.cimdy.coe.data.gen.stat.StatConditionIds}
 * 同构：具名常量 + 私有 {@link #id(String)} helper（仅叶名，命名空间固定 {@code coe}），
 * 落盘文件名即 id path，运行期由 {@code LoadEvent} 经 {@code relId} 去前缀还原成同一个 key。
 *
 * <p><b>为什么按 {@link StatDuration.Kind} 分组</b>：{@code Kind} 是本段唯一的类型维度
 * （{@code StatDurationFixed} 只有 kind + ticks 两个字段，没有别的分型轴），故常量按时间语义
 * 分三组；{@link #idsOf(StatDuration.Kind)} 给出<b>穷尽无 default</b>的
 * 「枚举取值 → 具名 id 列表」映射，新增 {@code Kind} 取值即编译失败，
 * 杜绝「新增语义忘了登记名字、运行期才抛未注册」。
 *
 * <p><b>分组依据是「{@code Kind} 有无通用读点」，不是「本表的 id 有无消费者」</b>：
 * 前者可判，后者当前为<b>零</b>——工程内没有任何条目以 {@code "duration": "read:coe:<path>"}
 * 引用本表任一常量，{@code LoadEvent#STAT_DURATION_MAP} 无活跃读点，落盘文件属预留扩展点
 * （只写不读），工程内既有时长全部走内联形态。故本表登记只承诺「该时间语义在工程里有人读」，
 * <b>不承诺「本表的 id 已被引用」</b>。逐 kind 依据与各值数字出处见 {@link StatDurationGen} 类注释。
 *
 * <p><b>COOLDOWN 组为空是刻意的</b>：{@code Kind#COOLDOWN}（「再次触发前的最短间隔」）在当前
 * 工程<b>没有任何读点</b>——{@code PeriodicDriver#intervalOf}（读点：{@code interval:60}→ 空，
 * 见 PeriodicDriver.java:57-59）与 {@code StatusStatEffect#durationFallback}
 * （读点：{@code cooldown:60} → 回落状态自带时长，见 StatusStatEffect.java:276-284）
 * 都显式把它排除在各自语义之外。项目纪律禁止「为点亮造词缀」，故本组不登记任何具名项，
 * {@link #idsOf} 的 {@code COOLDOWN} 分支返回空列表而非编造一个无消费者的名字。
 */
public final class StatDurationIds {

    // ── DURATION：施加后持续多久。Kind 的读点：StatusStatEffect#durationFallback（状态有效期覆盖通道）。

    /** 4 秒 = 80 tick。依据 SpiritGreenGen.java:189（caltropMaim，POE2 真值「Maim lasts for 4 seconds」）。 */
    public static final Identifier DUR_MAIM_4S = id("dur_maim_4s");

    /** 8 秒 = 160 tick。依据 WireAilment.java:386（凋零光环，注释 :379「8 秒 = 160 tick」）。 */
    public static final Identifier DUR_WITHERED_8S = id("dur_withered_8s");

    // ── INTERVAL：周期节拍，每 N tick 重新施加一次。Kind 的读点：PeriodicDriver#intervalOf。

    /** 5 tick 光环刷新节拍。依据 SkillBanner.java:63 {@code AURA_INTERVAL = 5}；:277 / :288 实际挂在战旗光环条目的 {@code interval} 字段（{@code withInterval}），<b>非</b> {@code duration} 字段——跨字段取证，详见 {@link StatDurationGen} 类注释。 */
    public static final Identifier INT_AURA_5T = id("int_aura_5t");

    /** 14 tick = 0.7 秒节拍。依据 SpiritBlueGen.java:208（malice，POE2 真值「every (0.7—0.51) seconds」）。 */
    public static final Identifier INT_CRIT_WEAKNESS_14T = id("int_crit_weakness_14t");

    /** 36 tick = 1.8 秒节拍。依据 SpiritBlueGen.java:163（withering_presence，POE2 真值「every (1.8—1.42) seconds」）。 */
    public static final Identifier INT_WITHERED_36T = id("int_withered_36t");

    // ── COOLDOWN：Kind 层无读点（见类注释），本组不登记具名项。

    private StatDurationIds() {
    }

    /**
     * {@link StatDuration.Kind} → 该时间语义下登记的全部具名 id（登记顺序即落盘顺序）。
     *
     * <p>穷尽 switch 无 {@code default}：{@code Kind} 新增取值即编译失败。
     * {@code COOLDOWN} 分支返回空列表——该语义当前无消费读点，不造无消费者的名字。
     *
     * <p>本方法是 {@link StatDurationGen#contents()} 的<b>唯一登记真源</b>：
     * {@code contents()} 里出现而本方法未列出的 id（或反之）都会在
     * {@code StatDurationGen} 的完备性自检中抛 {@link IllegalStateException}。
     */
    public static List<Identifier> idsOf(StatDuration.Kind kind) {
        return switch (kind) {
            case DURATION -> List.of(DUR_MAIM_4S, DUR_WITHERED_8S);
            case COOLDOWN -> List.of();
            case INTERVAL -> List.of(INT_AURA_5T, INT_CRIT_WEAKNESS_14T, INT_WITHERED_36T);
        };
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("coe", path);
    }
}