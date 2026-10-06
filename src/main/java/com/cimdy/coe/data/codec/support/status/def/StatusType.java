package com.cimdy.coe.data.codec.support.status.def;

import com.cimdy.coe.data.codec.support.status.runtime.StatusBehavior;
import com.cimdy.coe.data.codec.support.status.runtime.StatusInstance;
import com.cimdy.coe.data.codec.support.status.runtime.StatusStatProvider;
import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;

import java.util.*;

/**
 * 统一状态类型（取代 MobEffect 特例 + 空 StatusEffectCategory）。
 *
 * <p>每个 POE2 状态 = 一个独立 {@code StatusType}。其<b>类型定义</b>（元数据 + 行为引用）来自
 * 数据记录 {@link StatusTypeDef}（仿 AffixGen / StatEffectGen 的类型安全数据驱动范式），
 * 行为（tick / stat 贡献）按 {@code tickBehaviorId} / {@code statBehaviorId} 引用
 * {@link StatusBehavior} 中有限命名的实现——不再把行为 lambda 硬编码进各分类注册表。
 *
 * <p>注册采用静态常量 + 注册表；全部定义集中于 {@link StatusDefs}（合并原 8 个分类注册表），
 * 其静态字段经 {@link #register(StatusTypeDef)} 登记，确保全部定义入库。
 *
 * 即时注册）仍由 {@link #dynamic} 承载——其 id 由技能数据提供、无法静态枚举，属新系统运行时机制。
 */
public final class StatusType {

    private static final List<StatusType> REGISTRY = new ArrayList<>();
    private static final Map<String, StatusType> BY_ID = new HashMap<>();

    // ── 元数据 ──
    public final String id;
    public final StatusKind kind;
    public final boolean stackable;
    public final int maxStacks;
    public final MagnitudeMeaning magnitudeMeaning;
    public final StatusTickLogic tickLogic;
    public final StatusStatProvider statProvider;
    /**
     * 可选：将本状态「XX 时提高某属性/造成某效果」经 Stat 系统实现（而非事件回调）。
     * 一旦 set，{@link StatusStatBridge} 在状态激活期间把返回的 {@code ModifierStatEffect}
     * 注入实体的 STAT_STORE，使对应属性在读值时按 StatContext 作用域即时生效；
     * 状态到期自动移除。条件「XX 时」应落成 StatContext 的 tag（行为发生时 enter），
     * 而非此处单独的监听器。
     */
    public final AilmentSpec ailment;
    /** 缺省持续时间（ticks）——技能显式施加状态时的兜底时长。 */
    public final int defaultDurationTicks;
    /** 分类标签：在<b>注册阶段</b>随状态一并声明；Stat 侧仅调用 {@link #tagsOf(String)} 即可取得。 */
    public final Set<String> tags;
    /** HUD 显示行为（可选，null = 默认呈现：空文本 + 显示剩余时间）。 */
    public final StatusDisplayAction displayAction;

    private StatusType(StatusTypeDef d) {
        this.id = d.id();
        this.kind = d.kind();
        this.stackable = d.stackable();
        this.maxStacks = d.maxStacks();
        this.magnitudeMeaning = d.magnitudeMeaning();
        this.tickLogic = StatusBehavior.tickProvider(d.tickBehaviorId());
        this.statProvider = StatusBehavior.statProvider(d.statBehaviorId());
        this.ailment = d.ailment();
        this.defaultDurationTicks = d.defaultDurationTicks();
        this.tags = d.tags();
        String dt = d.displayText();
        this.displayAction = dt != null
                ? new StatusDisplayAction() {
                    @Override
                    public String text(StatusInstance instance, LivingEntity owner) { return dt; }
                    @Override
                    public boolean showTime(StatusInstance instance, LivingEntity owner) {
                        return d.showTime() != null ? d.showTime() : true;
                    }
                }
                : null;
    }

    /** 由数据定义登记一个状态类型（各定义集中于 {@link StatusDefs} 静态调用）。 */
    public static StatusType register(StatusTypeDef def) {
        StatusType t = new StatusType(def);
        REGISTRY.add(t);
        BY_ID.put(t.id, t);
        return t;
    }

    /**
     * 动态状态注册：技能等直接附加自身为状态——状态 id = 技能 id（数据驱动），
     * 由技能系统在运行时按需注册（幂等：已注册直接返回既有实例）。kind 由调用方声明
     * （诅咒技能 = {@link StatusKind#CURSE}）；纯标记（无数值语义、不叠层），
     * HUD 图标经 {@code StatusInstance#iconOverride} 使用技能图标。
     *
     * <p>仅用于运行时由技能数据驱动、无法静态枚举的 id（诅咒 / 标记等）；固定 id 的状态
     * 应在 {@link StatusDefs} 中静态声明。
     *
     * @param id            状态 id（技能 id 的字符串形式）
     * @param kind          状态大类（诅咒 / 标记 / 捷 …）
     * @param durationTicks 缺省持续时间（技能未显式给定时兜底）
     */
    public static StatusType dynamic(String id, StatusKind kind, int durationTicks) {
        return dynamic(id, kind, durationTicks, new String[0]);
    }

    /**
     * 动态状态注册（带标签重载）：技能/地面等可直接附加自身为状态——状态 id 即字符串，
     * 运行时按需注册（幂等），并携带分类标签供 Stat 作用域构建使用。
     *
     * @param id            状态 id（如 {@code on_jagged_ground}）
     * @param kind          状态大类（GROUND / CURSE / …）
     * @param durationTicks 缺省持续时间（技能未显式给定时兜底）
     * @param tags          分类标签（如 {@code jagged_ground}）
     */
    /**
     * 动态状态的行为接线表（table-driven）：大类 → 接线逻辑。新增大类时<b>仅</b>在此表追加一条，
     * 无需改动 {@link #dynamic} 的分支逻辑——精确保留原
     * {@code CURSE→curseTick/curseStatProviderId}、{@code AURA/HERALD→none/auraStatProviderId}、
     * 其余 → {@code none/none} 的语义。
     */
    private interface DynamicWiring {
        void apply(StatusTypeDef.Builder b, String id);
    }

    private static final Map<StatusKind, DynamicWiring> DYNAMIC_WIRING = new EnumMap<>(StatusKind.class);
    static {
        DYNAMIC_WIRING.put(StatusKind.CURSE,  (b, id) -> b.tick("curseTick").stat(StatusBehavior.curseStatProviderId(id)));
        DYNAMIC_WIRING.put(StatusKind.AURA,   (b, id) -> b.tick("none").stat(StatusBehavior.auraStatProviderId(id)));
        DYNAMIC_WIRING.put(StatusKind.HERALD, (b, id) -> b.tick("none").stat(StatusBehavior.auraStatProviderId(id)));
        // 未登记大类 → none/none（原 else 分支）
    }

    public static StatusType dynamic(String id, StatusKind kind, int durationTicks, String... tags) {
        StatusType existing = BY_ID.get(id);
        if (existing != null) return existing;
        StatusTypeDef.Builder b = StatusTypeDef.def(id, kind).duration(Math.max(1, durationTicks)).tags(tags);
        DYNAMIC_WIRING.getOrDefault(kind, (bb, ignored) -> bb.tick("none").stat("none")).apply(b, id);
        StatusType t = new StatusType(b.build());
        REGISTRY.add(t);
        BY_ID.put(id, t);
        return t;
    }

    public static List<StatusType> all() {
        return List.copyOf(REGISTRY);
    }

    public static Optional<StatusType> byId(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    /** 取状态类型自带分类标签（不可变）。 */
    public Set<String> tags() {
        return tags;
    }

    /**
     * Status 帮助方法：按 id 取分类标签，供 Stat 作用域构建使用（缺失返回空集）。
     */
    public static Set<String> tagsOf(String id) {
        return byId(id).map(t -> t.tags).orElse(Set.of());
    }

    /** 按 id 编解码（数据驱动：JSON 字符串即状态类型 id；未知 id 直接报错，无兼容兜底）。 */
    public static final Codec<StatusType> CODEC = Codec.STRING.comapFlatMap(
            id -> byId(id).map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown status type id: " + id)),
            t -> t.id);

    /**
     * HUD 渲染图标路径：gui/status/ + 效果注册名（id）。
     * 图标资源由 poe2db 爬取获得，存放于 assets/coe/textures/gui/status/&lt;id&gt;.png；
     * 由技能施加的状态可经 {@link StatusInstance#iconOverride} 改用技能图标。
     */
    public Identifier icon() {
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/status/" + id + ".png");
    }

    // ── 强度语义（供 StatusEngine / 属性层消费） ──
    public enum MagnitudeMeaning {
        /** 无数值（纯标记，如相位 / 圣洁神龛） */
        NONE,
        /** 每秒伤害百分比（点燃 / 流血 / 中毒 DoT 系数） */
        DAMAGE_PERCENT_PER_SECOND,
        /** 减速百分比（冰缓 / 嘲讽 / 阻碍） */
        SLOW_PERCENT,
        /** 强度百分比（感电增伤 / 冰缓减速 / 曝露抗性削减 / 强度可缩放） */
        MAGNITUDE_PERCENT,
        /** 抗性削减百分比（诅咒 / 曝露） */
        RESISTANCE_REDUCTION,
        /** 按层数承载（凋零 / 破甲 / 完全破甲 / 猛击尾流） */
        STACK_BASED,
        /** 自定义（由 tickLogic 自行解释 payload / magnitude） */
        CUSTOM
    }

    /** 每 tick 状态逻辑（DoT / 控制衰减 / 增益持续 / 神龛脉冲）。 */
    @FunctionalInterface
    public interface StatusTickLogic {
        void tick(StatusInstance instance, LivingEntity target);
    }

    /**
     * HUD 显示行为：允许状态类型声明自定义 HUD 呈现（覆盖文本 / 是否显示剩余时间）。
     * 默认实现（空文本 + 显示时间）对绝大多数状态透明；仅需在 HUD 上做特殊呈现的状态
     * （如常驻标记、需要固定文案的状态）覆盖对应方法即可。
     */
    public interface StatusDisplayAction {
        /** HUD 文本覆盖；返回非空字符串则在图标上渲染该文本（如「复活中」）。 */
        default String text(StatusInstance instance, LivingEntity owner) { return ""; }
        /** 是否在图标下显示剩余时间；常驻 / 无意义时长的标记状态应返回 false。 */
        default boolean showTime(StatusInstance instance, LivingEntity owner) { return true; }
    }

    // ── 强制加载状态定义（确保静态常量登记） ──
    static {
        // 全部状态定义集中于 StatusDefs（合并原 8 个分类注册表）。
        try {
            Class.forName(StatusDefs.class.getName());
        } catch (ClassNotFoundException ignored) {
        }
    }
}
