package com.cimdy.coe.data.codec.support.math.context.nodes.value.status;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;
import com.cimdy.coe.data.codec.skill.phase.action.charge.ChargeType;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.StatusNode;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.HealthValue;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.status.def.StatusKind;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.status.runtime.StatusInstance;
import com.cimdy.coe.data.codec.support.status.runtime.StatusStore;
import com.cimdy.coe.data.helper.impl.aura.MarkHelper;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.support.stat.entry.describer.StatNumberFormat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 统一状态读值 / 判定节点。
 * <p>
 * 合并原七类近重复节点（行为保持完全一致，仅表达收敛为一棵参数化的 Match 规格）：
 * <ul>
 *   <li>{@code StatusValue}      —— 单状态 / 资源标量读值（kind=能量/狂怒/魔力/精魂/充能球/连击/荣耀/标记 或 具体状态 id，输出层数/强度/1）。</li>
 *   <li>{@code StatusKindValue}  —— 状态大类在身判定（kind=CURSE 等 → 1/0）。</li>
 *   <li>{@code StatusTagValue}   —— 分类标签在身判定（tag=exposure 等 → 1/0，支持 {@code "!"} 取反）。</li>
 *   <li>{@code StatusAgeValue}   —— 单状态存续时长门控（kind=bleed 且 age ≥ 阈值 → 1/0）。</li>
 *   <li>{@code HeavyStunValue}   —— 重晕档（STUN && heavy）在身判定 → 1/0。</li>
 *   <li>{@code FullyBrokenArmorValue} —— 完全破甲档（ARMOUR_BREAK && fullyBroken）在身判定 → 1/0。</li>
 *   <li>{@code CurseDurationExpiredValue} —— 任意 CURSE 状态存续比例 ≥ fraction → 1/0。</li>
 * </ul>
 *
 * <p><b>JSON 形态</b>（单一 {@code "status"} 键，参数均为可选）：
 * <pre>{@code
 * { "type":"status", "id":"chill", "output":"intensity" }          // 原 StatusValue：读 chill 标量
 * { "type":"status", "kind":"curse", "output":"flag" }             // 原 StatusKindValue
 * { "type":"status", "tag":"exposure", "output":"flag" }           // 原 StatusTagValue
 * { "type":"status", "id":"bleed", "age_threshold":4.0, "output":"flag" } // 原 StatusAgeValue(80 tick)
 * { "type":"status", "flag":"heavy", "output":"flag" }             // 原 HeavyStunValue
 * { "type":"status", "flag":"fully_broken", "output":"flag" }      // 原 FullyBrokenArmorValue
 * { "type":"status", "kind":"curse", "fraction":0.5, "output":"flag" } // 原 CurseDurationExpiredValue
 * { "type":"status", "id":"!wither", "output":"flag" }          // 未持有 wither 状态 → 1/0
 * }</pre>
 *
 * 参数：{@code id}(状态 id 或资源 kind，可选) / {@code kind}(StatusKind 大类名，可选) / {@code tag}(分类标签，可选) /
 * {@code age_threshold}(秒，可选，需配合 id) / {@code fraction}(0..1 阈值，配合 kind=curse 时门控) /
 * {@code flag}(heavy / fully_broken，可选) / {@code output}(flag/stacks/intensity/fraction/age，缺省 intensity)。
 *
 * <p><b>「包含 / 不包含某状态」原语</b>：{@code id} 支持 {@code "!"} 前缀取反（与 {@code tag} 既有约定完全同构），
 * 且<b>只有 {@code output:"flag"}</b> 时才输出 1/0 —— 未取反 = 持有该状态 → 1、否则 0；
 * 取反 = 未持有 → 1、持有 → 0；id 未注册 → 恒 0（未取反）/ 恒 1（取反）。
 * 其余 output（stacks / intensity / fraction / age）下 {@code "!"} 不生效，仍按 {@link #statusScalar}
 * 的「层数 / 强度(0→1) / 1」语义读值。便捷工厂见 {@link #hasStatus} / {@link #lacksStatus}。
 *
 * <p>除 {@code output:flag}（七类原节点的实际输出）外，其余 output 模式为新增能力；所有既有接线经 {@code output:"flag"}
 * 复现原 1/0 语义，标量读值经 {@code output:"intensity"} 复现原 StatusValue 的「层数 / 强度(0→1) / 1」兜底。
 */
public record StatusValue(
        Optional<String> id,
        Optional<String> kind,
        Optional<String> tag,
        Optional<Double> ageThreshold,
        Optional<Double> fraction,
        Optional<Flag> flag,
        Output output
) implements StatusNode {

    /**
     * 兼容旧 2 参构造（{@code new StatusValue("chill", EMPTY_LOCATION)}）。
     * 旧第二参为资源子 id（充能球/连击/荣耀 的具体 id），实际调用方一律传 {@code EMPTY_LOCATION}，
     * 故此处忽略之、恒按「全体聚合」语义读值，与原行为一致。
     */
    public StatusValue(String status, Identifier subId) {
        this(Optional.of(status), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Output.INTENSITY);
    }

    /** 原 StatusKindValue：任意该大类状态在身 → 1/0。 */
    public static StatusValue byKind(String kind) {
        return new StatusValue(Optional.empty(), Optional.of(kind), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Output.FLAG);
    }

    /** 原 StatusTagValue：带该标签状态在身 → 1/0（tag 以 {@code "!"} 前缀表示取反）。 */
    public static StatusValue byTag(String tag) {
        return new StatusValue(Optional.empty(), Optional.empty(), Optional.of(tag), Optional.empty(),
                Optional.empty(), Optional.empty(), Output.FLAG);
    }

    /**
     * 「持有该状态」→ 1/0（{@code output:"flag"}）。
     *
     * <p>id 未注册<b>直接抛异常</b>——数据驱动基底：静态定义由 {@code StatusDefs} 静态块强制加载，
     * 引用不存在的 id 属接线错误，必须快速失败而非静默恒 0（恒 0 会让「包含」永不成立、
     * 「不包含」恒真，是极难发现的静默语义错误）。
     */
    public static StatusValue hasStatus(Identifier statusId) {
        String p = statusId.getPath();
        if (StatusType.byId(p).isEmpty()) throw new IllegalStateException("Unknown status id: " + statusId);
        return new StatusValue(Optional.of(p), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Output.FLAG);
    }

    /**
     * 「未持有该状态」→ 1/0：{@link #hasStatus} 的 {@code "!"} 前缀取反形态（同 {@link #byTag} 的取反约定）。
     * id 未注册同样快速失败（理由见 {@link #hasStatus}）。
     */
    public static StatusValue lacksStatus(Identifier statusId) {
        String p = statusId.getPath();
        if (StatusType.byId(p).isEmpty()) throw new IllegalStateException("Unknown status id: " + statusId);
        return new StatusValue(Optional.of("!" + p), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Output.FLAG);
    }

    /** 原 HeavyStunValue / FullyBrokenArmorValue。 */
    public static StatusValue byFlag(Flag flag) {
        return new StatusValue(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(flag), Output.FLAG);
    }

    /** 原 StatusAgeValue：单状态存续时长 ≥ ageThreshold(秒) → 1/0。 */
    public static StatusValue byAgeThreshold(String statusId, double ageThresholdSeconds) {
        return new StatusValue(Optional.of(statusId), Optional.empty(), Optional.empty(),
                Optional.of(ageThresholdSeconds), Optional.empty(), Optional.empty(), Output.FLAG);
    }

    /** 原 CurseDurationExpiredValue：任意 CURSE 状态存续比例 ≥ fraction → 1/0。 */
    public static StatusValue byCurseFraction(double fraction) {
        return new StatusValue(Optional.empty(), Optional.of(StatusKind.CURSE.name().toLowerCase(Locale.ROOT)),
                Optional.empty(), Optional.empty(), Optional.of(fraction), Optional.empty(), Output.FLAG);
    }

    public static final Codec<StatusValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("id").forGetter(StatusValue::id),
            Codec.STRING.optionalFieldOf("kind").forGetter(StatusValue::kind),
            Codec.STRING.optionalFieldOf("tag").forGetter(StatusValue::tag),
            Codec.DOUBLE.optionalFieldOf("age_threshold").forGetter(StatusValue::ageThreshold),
            Codec.DOUBLE.optionalFieldOf("fraction").forGetter(StatusValue::fraction),
            Flag.CODEC.optionalFieldOf("flag").forGetter(StatusValue::flag),
            Output.CODEC.optionalFieldOf("output", Output.INTENSITY).forGetter(StatusValue::output)
    ).apply(instance, StatusValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        // ── 单状态 / 资源标量读值（原 StatusValue）──
        if (id.isPresent()) {
            String k = id.get();
            // ── 「包含 / 不包含某状态」原语（hasStatus / lacksStatus）：仅 output:flag 生效。
            // 声明了 age_threshold 的走下面的原 StatusAgeValue 存续门控（语义不同，优先）。
            if (output == Output.FLAG && ageThreshold.isEmpty()) {
                boolean negated = k.startsWith("!");
                String target = negated ? k.substring(1) : k;
                boolean held = StatusType.byId(target)
                        .map(type -> StatusEngine.has(living, type))
                        .orElse(false);
                return negated != held ? 1.0 : 0.0;
            }
            return switch (k) {
                case "energy", "rage", "mana", "spirit" -> HealthValue.readResource(k, living);
                case "charges" -> charges(living);
                case "combo" -> combo(living);
                case "glory" -> glory(living);
                case "mark" -> markScalar(living);
                default -> {
                    double v = statusScalar(living, k);
                    if (ageThreshold.isPresent()) {
                        // 原 StatusAgeValue：特定状态存续时长 ≥ 阈值输出 1（output:flag），否则返回存续秒数
                        StatusType type = StatusType.byId(k).orElse(null);
                        if (type == null) yield 0D;
                        StatusInstance inst = StatusEngine.query(living, type);
                        if (inst == null) yield 0D;
                        long age = living.level().getGameTime() - inst.appliedTick;
                        boolean ok = age >= Math.round(ageThreshold.get() * 20.0);
                        yield output == Output.FLAG ? (ok ? 1.0 : 0.0) : age / 20.0;
                    }
                    yield v;
                }
            };
        }
        // ── 谓词判定（原其余六类）──
        if (living == null || !living.hasData(AttachmentRegistry.STATUS_STORE)) return 0;
        StatusStore store = living.getData(AttachmentRegistry.STATUS_STORE);

        // 重晕 / 完全破甲档：类型等值匹配（原 HeavyStunValue / FullyBrokenArmorValue，恒输出 1/0）
        if (flag.isPresent()) {
            boolean hit = false;
            for (var inst : store.instances.values()) {
                if (flag.get() == Flag.HEAVY && inst.type == StatusDefs.STUN && inst.heavy) hit = true;
                if (flag.get() == Flag.FULLY_BROKEN && inst.type == StatusDefs.ARMOUR_BREAK && inst.fullyBroken) hit = true;
            }
            return hit ? 1.0 : 0.0;
        }

        // 大类 / 标签匹配（原 StatusKindValue / StatusTagValue / CurseDurationExpiredValue）
        List<StatusInstance> hits = new ArrayList<>();
        boolean matched = false;
        if (tag.isPresent()) {
            // 与原 StatusTagValue 同构：标签命中即满足，"!" 前缀取反
            boolean negated = tag.get().startsWith("!");
            String target = negated ? tag.get().substring(1) : tag.get();
            boolean m = false;
            for (var inst : store.instances.values()) {
                if (inst.type.tags().contains(target)) {
                    m = true;
                    if (!negated) hits.add(inst);
                }
            }
            matched = negated ? !m : m;
        } else if (kind.isPresent()) {
            String tgt = kind.get();
            StatusKind kk = null;
            for (StatusKind k : StatusKind.values()) {
                if (k.name().equalsIgnoreCase(tgt)) { kk = k; break; }
            }
            if (kk != null) {
                for (var inst : store.instances.values()) {
                    if (inst.type.kind == kk) {
                        if (fraction.isPresent()) {
                            // 原 CurseDurationExpiredValue：存续比例 = age / (age + remaining) ≥ fraction → 命中
                            long age = living.level().getGameTime() - inst.appliedTick;
                            double total = age + inst.durationTicks;
                            if (total > 0 && age >= fraction.get() * total) {
                                matched = true;
                                hits.add(inst);
                            }
                        } else {
                            matched = true;
                            hits.add(inst);
                        }
                    }
                }
            }
        }

        if (output == Output.FLAG) return matched ? 1.0 : 0.0;
        if (hits.isEmpty()) return 0.0;
        StatusInstance first = hits.get(0);
        return switch (output) {
            case STACKS -> first.stacks;
            case INTENSITY -> first.magnitude != 0 ? first.magnitude : 1.0;
            case AGE -> (living.level().getGameTime() - first.appliedTick) / 20.0;
            case FRACTION -> {
                long age = living.level().getGameTime() - first.appliedTick;
                double total = age + first.durationTicks;
                yield total > 0 ? age / total : 0.0;
            }
            default -> matched ? 1.0 : 0.0;
        };
    }

    /**
     * 统一状态标量（原 StatusValue.statusScalar）：kind 即 {@link StatusType} id。
     * 可叠层 → 层数；不可叠层 → 强度（无强度语义输出 1）；未持有 / 未注册 → 0。
     */
    private double statusScalar(LivingEntity living, String k) {
        return StatusType.byId(k).map(type -> {
            if (!StatusEngine.has(living, type)) return 0D;
            if (type.stackable) return (double) StatusEngine.stacks(living, type);
            double magnitude = StatusEngine.magnitude(living, type);
            return magnitude != 0 ? magnitude : 1.0;
        }).orElse(0D);
    }

    /**
     * 标记（Mark）技能型状态：读实体身上由 Mark 技能经 {@link MarkHelper} 施加的
     * {@link StatusKind#MARK} 类状态，有效期内返回 1，否则 0（R3 安全）。
     */
    private double markScalar(LivingEntity living) {
        return MarkHelper.isMarked(living, living.level().getGameTime()) ? 1.0 : 0.0;
    }

    private double charges(LivingEntity living) {
        int sum = 0;
        for (ChargeType t : ChargeType.values()) sum += StatusEngine.stacks(living, t.statusType());
        return sum;
    }

    private double glory(LivingEntity living) {
        return living.getData(AttachmentRegistry.GLORY_TRACKER).tracks().values().stream()
                .mapToInt(ResourceRegistry.GloryData::amount).sum();
    }

    private double combo(LivingEntity living) {
        return living.getData(AttachmentRegistry.COMBO_TRACKER).combos().values().stream()
                .mapToInt(ResourceRegistry.ComboData::count).sum();
    }

    // ===== describe（逐字保留七类原节点的翻译键，按配置分派） =====

    @Override
    public List<Component> describeFragment() {
        if (flag.isPresent()) {
            return List.of(Component.translatable(flag.get() == Flag.HEAVY
                    ? "stat.condition.heavy_stun" : "stat.condition.fully_broken_armor"));
        }
        if (id.isPresent()) {
            if (ageThreshold.isPresent()) {
                Locale loc = ClientLocale.get();
                String dur = StatNumberFormat.format(ageThreshold.get(), loc) + "s";
                return List.of(Component.translatable("stat.condition.status_age",
                        describeQuantity(loc).get(0), Component.literal(dur)));
            }
            boolean neg = id.get().startsWith("!");
            String base = neg ? id.get().substring(1) : id.get();
            Component value = StatValueKey.of("status", base);
            // 与 tag 取反同构：取反时前置 StatValueKey.not（「不含 X」），翻译键仍复用 stat.condition.status
            Component arg = neg
                    ? Component.empty().append(StatValueKey.not()).append(Component.literal(" ")).append(value)
                    : value;
            return List.of(Component.translatable("stat.condition.status", arg));
        }
        if (tag.isPresent()) {
            boolean neg = tag.get().startsWith("!");
            String base = neg ? tag.get().substring(1) : tag.get();
            Component value = StatValueKey.of("status_tag", base);
            Component arg = neg
                    ? Component.empty().append(StatValueKey.not()).append(Component.literal(" ")).append(value)
                    : value;
            return List.of(Component.translatable("stat.condition.status_tag", arg));
        }
        if (kind.isPresent()) {
            if (fraction.isPresent()) {
                return List.of(Component.translatable("stat.condition.curse_duration"));
            }
            return List.of(Component.translatable("stat.condition.status_kind",
                    StatValueKey.of("status_kind", kind.get())));
        }
        return List.of(Component.translatable("stat.condition.unknown"));
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        if (flag.isPresent()) {
            return List.of(Component.translatable(flag.get() == Flag.HEAVY
                    ? "stat.condition.heavy_stun" : "stat.condition.fully_broken_armor"));
        }
        if (id.isPresent()) {
            boolean neg = id.get().startsWith("!");
            String base = neg ? id.get().substring(1) : id.get();
            return List.of(StatValueKey.of("status", base));
        }
        if (tag.isPresent()) {
            boolean neg = tag.get().startsWith("!");
            String base = neg ? tag.get().substring(1) : tag.get();
            return List.of(StatValueKey.of("status_tag", base));
        }
        if (kind.isPresent()) {
            if (fraction.isPresent()) return List.of(StatValueKey.of("curse_duration_expired", "expired"));
            return List.of(StatValueKey.of("status_kind", kind.get()));
        }
        return List.of(StatValueKey.of("status", "unknown"));
    }

    /** 重晕 / 完全破甲档标记（原 HeavyStunValue / FullyBrokenArmorValue 的等价判定）。 */
    public enum Flag implements net.minecraft.util.StringRepresentable {
        HEAVY("heavy"),
        FULLY_BROKEN("fully_broken");

        private final String name;

        Flag(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public static final Codec<Flag> CODEC = net.minecraft.util.StringRepresentable.fromEnum(Flag::values);
    }

    /** 输出模式：flag(1/0) / stacks(层数) / intensity(强度,0→1) / fraction(存续比例) / age(存续秒数)。 */
    public enum Output implements net.minecraft.util.StringRepresentable {
        FLAG("flag"),
        STACKS("stacks"),
        INTENSITY("intensity"),
        FRACTION("fraction"),
        AGE("age");

        private final String name;

        Output(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public static final Codec<Output> CODEC = net.minecraft.util.StringRepresentable.fromEnum(Output::values);
    }
}
