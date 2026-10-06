package com.cimdy.coe.data.codec.support.stat.effect.base;

import com.cimdy.coe.data.codec.support.stat.entry.describer.StatDescriber;
import com.cimdy.coe.data.codec.support.stat.effect.damage.DamageStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.resource.ResourceStatEffect;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;

import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.tag.DomainTags;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.ValueRange;
import com.cimdy.coe.event.TriggerContext;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Stat 效果 —— 一条 stat 条目（{@link StatEntry}）的「做什么」部分。
 *
 * <h2>职责边界（重要）</h2>
 * 效果<b>只描述语义，不携带数值、条件与选择器</b>：
 * <ul>
 *   <li><b>数值</b>（{@code amount}）、<b>条件</b>（{@code conditions}）、
 *       <b>三段选择器</b>（{@link StatTargets}）一律由外层条目 {@link StatEntry} 持有；</li>
 *   <li>效果自身只能携带<b>身份/语义字段</b>：属性 Holder、资源类型、状态类型、异常类型、
 *       运算方式、语义面（Aspect）之类；</li>
 *   <li>效果里<b>不允许出现第二个数值字段</b>——需要两个数就是两条词缀，拆成两个效果。</li>
 * </ul>
 * 唯一的公共字段是 {@link #tags()}（事件标签），统一实现见 {@link AbstractStatEffect}。
 *
 * <h2>两种亚型</h2>
 * <ul>
 *   <li><b>属性亚型</b>（{@link ModifierStatEffect}）：由 {@link StatEngine#fold} 在读属性时按作用域即时聚合；</li>
 *   <li><b>触发亚型</b>（{@link ResourceStatEffect} / 异常类 / {@link DamageStatEffect} …）：
 *       由 {@link StatEngine#fire} 在事件（击杀 / 击中 / 暴击 …）过筛后执行。</li>
 * </ul>
 * 新增效果类型 = 实现本接口 + 在 {@link StatEffectTypes} 注册一行。
 */
public interface StatEffect {
    /**
     * 效果索引归属（开闭原则）：效果类型 → StatStore 归入哪个索引 / StatEngine 哪个通道消费。
     * <b>StatStore 构造器不再用 instanceof 硬编码分类</b>——新增效果类型只需实现本接口 +
     * {@link StatEffectTypes} 注册 + 覆写本方法，StatStore 零改动。
     */
    enum EffectKind {
        /** 属性亚型：由 {@link StatEngine#fold} 读时聚合（ModifierStatEffect）。 */
        FOLD,
        /** 触发亚型：由 {@link StatEngine#fire} 事件触发执行（heal/damage/status）。 */
        FIRE,
        /** 资源转换：扣费前重定向（ResourceConvertStatEffect）。 */
        RESOURCE_CONVERT,
        /** 伤害类型重路由（DamageConvertStatEffect）。 */
        DAMAGE_CONVERT,
        /** 承伤转化（DamageTakenConvertStatEffect）。 */
        DAMAGE_TAKEN_CONVERT,
        /** 状态免疫（ImmuneStatEffect）：由 StatusEngine.apply 查询「目标对该状态是否免疫」短路。 */
        IMMUNE,
        /** 掷骰修饰（LuckyStatEffect）：由 DamageHelper 查询 LUCKY/UNLUCKY 双掷语义（伤害/暴击/闪避/格挡）。 */
        LUCKY,
        /** 命中标志（HitFlagStatEffect）：由 DamageHelper 查询不可闪避 / 绝不暴击 / 不可被暴击短路。 */
        HIT_FLAG,
        /** 投送修饰（DeliveryStatEffect）：由 SupportProfile 查询额外投射物/穿透/链/叉/范围参数（fold 型，无副作用）。 */
        DELIVERY,
        /** 地面生成（CreateGroundStatEffect）：由 CreateGroundDriver 每 tick 驱动，在持有者脚下周期性生成地面场
         *  （非事件触发，独立管道，不混入光环）。 */
        GROUND,
        /** 周期施加（PeriodicStatEffect）：由 PeriodicDriver 每 tick 节流驱动，把「施加某效果」按
         *  {@code interval_ticks} / {@code interval_expr} 周期性地重新施加到 {@code entry.target()} 解析出的
         *  每个目标（非事件触发，通用节流通道 —— {@code StatEngine} 的 fold / fire 均无节流概念）。 */
        PERIODIC,
        /** 惰性（RequirementStatEffect：不进 StatStore，仅词缀生成器读取）。 */
        INERT
    }

    /** 效果类型 id（写入 JSON 的 "type" 字段，用于 Codec 分发）。 */
    String type();

    /**
     * 触发所需事件标签（kill / hit / crit …）——<b>标签布尔表达式</b>（{@link GateExpr}，
     * 支持 与（数组多项）/ 或（{@code |}）/ 非（{@code !}）/ 括号嵌套），对事件作用域标签集合求值。
     * 缺省 {@link GateExpr#ALWAYS}（无条件）。
     */
    default GateExpr tags() {
        return GateExpr.ALWAYS;
    }

    /**
     * 领域标签（mod 分类维度：fire / physical / attack / defence / spell …）。
     * 与 {@link #tags()}（事件触发布尔表达式）正交：本方法描述「这条 stat 的效果属于哪些领域」，
     * 供 Bench / Fossil / Essence 等修饰系统经 {@code GateTagRegistry} 做域匹配与排除。
     *
     * <p><b>默认实现（D6 终态）</b>：从本效果的<b>事件标签 {@link #tags()}</b>自动抽取领域维度
     * （经 {@link DomainTags#fromTags(GateExpr)}，过滤纯触发作用域词 + 父类别展开），
     * 故<b>声明域 = 在构造处的事件标签里写 fire/cold/…</b>，新增一个 fire 类效果自动继承 fire+elemental 域，
     * 零额外登记。{@link AbstractStatEffect} 覆写为「显式 {@code domainTags} 字段优先，否则回退派生」，
     * 显式字段（构造参数 / JSON {@code domain}）作覆盖逃生舱。
     * 这是 D6（Stat 自动派生标签，team-lead 2026-09-19 裁定）的落点：标签唯一权威来源是 StatEffect，
     * 不再维护「逐 stat 手工登记」的精选表。
     */
    default Set<String> domainTags() {
        return DomainTags.fromTags(tags());
    }

    /**
     * 本亚型的默认三段选择器：条目 JSON 未显式指定的槽位由此补齐
     * （如伤害亚型默认「作用于事件目标、按事件目标自身属性取值」）。
     */
    default StatTargets defaultTargets() {
        return StatTargets.EMPTY;
    }

    /** 是否为触发亚型（由 StatEngine.fire 事件驱动执行）。属性亚型返回 false。 */
    default boolean isTrigger() {
        return true;
    }

    /**
     * 本效果归入 {@link StatStore} 的哪个索引。默认按触发亚型二分；
     * 转换 / 惰性类效果必须显式覆写（默认 FOLD/FIRE 无法表达 convert 与 inert 语义）。
     */
    default EffectKind indexKind() {
        return isTrigger() ? EffectKind.FIRE : EffectKind.FOLD;
    }

    /**
     * 触发亚型的执行体：事件（击杀 / 击中 …）经 {@link GateExpr#matches} / {@link StatEngine#conditionsHold} 过筛通过后调用。
     * <p>
     * 效果持有者 / 事件目标统一由 {@code evalCtx}（{@link StatEvalContext#owner()} /
     * {@link StatEvalContext#triggerTarget()}）提供，不再作为独立参数重复持有；
     * 伤害数值经 {@code tc}（可为 null：非伤害事件如击杀 / 攻击起手 / 抬盾无需数值）传入，
     * 供「按造成伤害 / 承受伤害比例回复资源」等效果（偷取 / 补给 / 受击转能量）使用。
     *
     * @param entry    所属条目（数值 / 条件 / 选择器都从这里取）
     * @param scope    本次事件作用域（含事件标签与技能标识）
     * @param evalCtx 本次判定作用域内的实体缓存（钥匙即条目的 {@link StatTargets}）
     * @param tc       本次伤害结算上下文（可为 null）
     */
    default void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                           @Nullable TriggerContext tc) {
    }

    /** UI 展示文本（数值来自所属条目）。 */
    List<Component> describe(StatEntry entry);

    /**
     * 效果「片段」：只产出本效果的 what 文本，不含 amount / targets / condition 拼装。
     * {@code ModifierStatEffect} / {@code DamageStatEffect} 覆写为数据驱动翻译键。
     * 装配顺序与四段合并由 {@link StatEntry#describe} → {@link StatDescriber} 统一负责。
     */
    default List<Component> describeFragment(StatEntry entry) {
        return describe(entry);
    }

    /**
     * 效果「片段」（本地化，P3）：只产出本效果的 what 文本，不含 amount / targets / condition 拼装。
     * 默认委托 {@link #describe(StatEntry)}（locale 不影响旧子类）；P3 由
     * {@code ModifierStatEffect} / {@code DamageStatEffect} 覆写为 per-X 反汇编 + 数字本地化。
     */
    default List<Component> describeFragment(StatEntry entry, Locale locale) {
        return describe(entry);
    }

    /**
     * 效果「片段」（本地化 + 范围值，重中之重）：默认委托 2 参数版本（范围模式无副作用，零回归）。
     * 单值常量词缀经 {@code ModifierStatEffect} 覆写为「值渲染拆离 + 区间注入」，
     * 其余 40+ 效果子类维持旧行为。
     */
    default List<Component> describeFragment(StatEntry entry, Locale locale, Optional<ValueRange> range) {
        return describeFragment(entry, locale);
    }
}
