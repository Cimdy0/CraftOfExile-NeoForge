package com.cimdy.coe.data.codec.support.stat.duration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

/**
 * Stat 条目（{@link com.cimdy.coe.data.codec.support.stat.entry.StatEntry}）的
 * <b>第五部分 · 时长段</b>：本条目施加的效果「在时间上如何存续 / 复触发」。
 *
 * <h2>为什么上提到条目层</h2>
 * 时长不是某个效果的私有字段：状态施加、属性授予、周期光环、冷却触发……<b>所有</b>效果都需要
 * 「持续多久 / 多久能再来一次」的语义。把它塞进复合类或某个具体 Effect 会导致每个新效果都
 * 重新发明一遍时长字段，故与 amount / conditions / targets / effect 并列成为条目的第五部分，
 * 对所有 Effect 生效。<b>Effect 实现内部一律不再持有任何时长字段。</b>
 *
 * <h2>三种时间语义（{@link Kind}）</h2>
 * <ul>
 *   <li>{@link Kind#DURATION} —— 触发且持续一段时间：施加后持续 N tick 自然结束，不重复触发；</li>
 *   <li>{@link Kind#COOLDOWN} —— 触发后需要一段时间才能再次触发：冷却 N tick；</li>
 *   <li>{@link Kind#INTERVAL} —— 触发后需要持续时间结束后才能再次触发：周期节拍 N tick
 *       （每 N tick 施加一次、每次持续 N tick，无缝续期，即原「光环 interval + duration」合流）。</li>
 * </ul>
 *
 * <h2>扁平化（重要）</h2>
 * 本段在 JSON 里是一个<b>字符串</b>而非嵌套对象，形式足以自诠释：
 * <pre>{@code
 * "duration": "duration:120"      // 持续 120 tick
 * "duration": "cooldown:60"       // 冷却 60 tick
 * "duration": "interval:20"       // 每 20 tick 施加一次
 * "duration": "read:coe:my_aura"  // 引用落盘的时长定义
 * }</pre>
 *
 * <h2>可空</h2>
 * 条目未声明时长 → {@code null}（不计时、不冷却、不周期），由各效果自身默认语义决定
 * （如状态取 {@code StatusType.defaultDurationTicks}）。
 */
public interface StatDuration {

    /** 时间语义种类。 */
    enum Kind implements StringRepresentable {
        /** 触发且持续一段时间（施加后持续 N tick，不重复触发）。 */
        DURATION("duration"),
        /** 触发后需要一段时间才能再次触发（冷却 N tick）。 */
        COOLDOWN("cooldown"),
        /** 触发后需要持续时间结束后才能再次触发（周期节拍 N tick）。 */
        INTERVAL("interval");

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        /** 按扁平前缀反查；未知返回 null（由调用方转成显式错误）。 */
        public static Kind byName(String s) {
            for (Kind k : values()) {
                if (k.name.equals(s)) return k;
            }
            return null;
        }
    }

    /** 本段时间语义。 */
    Kind kind();

    /** 时间量（tick）。{@link #READ_PREFIX} 形态经引用解析后取得。 */
    int ticks();

    /** 分发类型 id（{@code fixed} / {@code read}），供 {@link StatDurationTypes} 派发。 */
    String type();

    /** 扁平化写出：{@code <kind>:<ticks>}（read 形态为 {@code read:<id>}）。 */
    default String flatten() {
        return kind().getSerializedName() + ":" + ticks();
    }

    /** 引用形态前缀。 */
    String READ_PREFIX = "read";

    // ---------------------------------------------------------------- 静态工厂

    /** 内联时长：{@code kind} + tick 数。 */
    static StatDuration of(Kind kind, int ticks) {
        return new StatDurationFixed(kind, ticks);
    }

    /**
     * 引用形态：按 id 构造一个指向落盘时长定义的 {@link StatDurationRead}
     * （{@link #of(Kind, int)} 的引用型对称入口）。
     *
     * <p><b>零调用方，且不被 JSON 解析使用</b>：{@link #parse(String)} 遇到 {@code read:} 前缀时
     * <b>直接</b> {@code new StatDurationRead(id)}，不经过本工厂；工程内亦无任何代码调用本方法。
     *
     * <p><b>为何保留</b>：这是引用形态唯一语义清晰的<b>对外构造入口</b>，删掉将来要用须重建，
     * 属「未接线的合理扩展点」而非「重复实现」（重复实现的 {@code resolve(StatDuration)} 已删除，
     * 活路径由 {@link StatDurationRead#kind()} / {@link StatDurationRead#ticks()} 内部的
     * {@code resolved()} 独占）。刻意<b>不</b>为「让工厂被调用」而改造既有产出逻辑
     * （那会改变运行时行为）。
     *
     * <p>接线的另一半已通：{@code stat/duration/<id>.json} 的落盘与运行期加载分别由
     * {@code StatDurationProvider} 与 {@code LoadEvent} 的 {@code coe/stat/duration} 监听器补齐
     * （见 {@link StatDurationTypes} 类注释末段）。
     */
    static StatDuration read(Identifier ref) {
        return new StatDurationRead(ref);
    }

    // ---------------------------------------------------------------- 编解码（扁平字符串）

    /**
     * 解析扁平字符串：{@code duration:120} / {@code cooldown:60} / {@code interval:20} /
     * {@code read:coe:xxx}。未知前缀或非法数值均<b>显式报错</b>（不静默回落）。
     */
    static DataResult<StatDuration> parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return DataResult.error(() -> "时长为空，应为 <duration|cooldown|interval>:<ticks> 或 read:<id>");
        }
        int i = raw.indexOf(':');
        if (i <= 0 || i == raw.length() - 1) {
            return DataResult.error(() -> "时长格式非法（" + raw + "），应为 <kind>:<ticks> 或 read:<id>");
        }
        String head = raw.substring(0, i);
        String rest = raw.substring(i + 1);
        if (READ_PREFIX.equals(head)) {
            Identifier id = Identifier.tryParse(rest);
            if (id == null) {
                return DataResult.error(() -> "时长 read 引用的 id 非法: " + rest);
            }
            return DataResult.success(new StatDurationRead(id));
        }
        Kind kind = Kind.byName(head);
        if (kind == null) {
            return DataResult.error(() -> "未知时长种类: " + head + "（应为 duration / cooldown / interval / read）");
        }
        int ticks;
        try {
            ticks = Integer.parseInt(rest.trim());
        } catch (NumberFormatException e) {
            return DataResult.error(() -> "时长 tick 数非法: " + rest);
        }
        if (ticks <= 0) {
            return DataResult.error(() -> "时长 tick 数须为正: " + raw);
        }
        return DataResult.success(new StatDurationFixed(kind, ticks));
    }

    /** 扁平字符串编解码（条目 {@code duration} 字段直接用它）。 */
    Codec<StatDuration> CODEC = Codec.STRING.comapFlatMap(StatDuration::parse, StatDuration::flatten);

    /** 可选字段形态（条目层挂 {@code duration}，缺省 = 未声明时长）。 */
    MapCodec<java.util.Optional<StatDuration>> OPTIONAL_MAP_CODEC = CODEC.optionalFieldOf("duration");
}
