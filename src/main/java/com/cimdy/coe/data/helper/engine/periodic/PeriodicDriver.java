package com.cimdy.coe.data.helper.engine.periodic;

import com.cimdy.coe.data.codec.support.stat.duration.StatDuration;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.OptionalInt;

/**
 * <b>周期驱动（公共计时器）</b>：唯一权威的「按条目节拍把效果施加一次」入口。
 *
 * <h2>它同时服务两类调用方</h2>
 * <ul>
 *   <li><b>STAT_STORE 持有者</b>——{@link #tick(LivingEntity)} 每 tick 遍历持有者 store 内
 *       {@link StatStore#periodicIndexed()} 的全部周期条目，逐个交给 {@link #drive}；</li>
 *   <li><b>技能 / 其它非 store 持有者</b>——直接调 {@link #drive(LivingEntity, StatEntry, long, StatContext, LivingEntity)}，
 *       传入<b>自己的 tick 计数</b>与事件目标即可，
 *       无需把条目塞进 STAT_STORE、也无需自建节流定时器；（注：<b>技能的施放节拍不属于本类</b>——那是技能内部状态机的计时，见
 *       {@code SkillInstance#tick} 的 Continuous 脉冲，二者不同源、不可合并。）</li>
 * </ul>
 *
 * <h2>节拍来源</h2>
 * 节拍<b>不在本类</b>：读条目第五部分 {@link StatEntry#duration()}，且必须是
 * {@link StatDuration.Kind#INTERVAL} 语义（{@code interval:20} / {@code read:coe:xxx}）。
 * <b>未声明节拍 → 每刻驱动</b>：本类不过节流、逐 tick 施加一次；被授予的效果靠「每次施加即刷新」
 * 维持存在（Presence 光环是<b>集合关系而非倒计时</b>——范围内的目标持续拥有该效果，
 * 离开范围即停止刷新，效果随即过期）。只有显式声明了 INTERVAL 的条目才按节拍驱动。
 *
 * <h2>驱动只做三件事</h2>
 * 「读节拍（无节拍则恒真） → 节流 → 过条目 conditions → 调 {@code effect.onTrigger}」。
 * <b>施加内容完全由效果自己决定</b>：复合效果（{@code MultiStatEffect}）把子效果逐个派发，
 * 状态子效果走状态通道、属性子效果经 Target 写各目标 store。本驱动不写任何施加分支。
 */
public final class PeriodicDriver {

    private PeriodicDriver() {
    }

    /**
     * <b>公共节拍判定</b>（技能侧传自己的 tick 计数即可复用）：
     * {@code interval <= 0} 视为不节流（每刻恒真）；否则 {@code tickCount % interval == 0}。
     */
    public static boolean beat(long tickCount, int interval) {
        return interval <= 0 || tickCount % interval == 0;
    }

    /**
     * 读出条目的<b>节拍</b>（ticks）：须为 INTERVAL 语义；无时长 / 其它时间语义（DURATION / COOLDOWN）→ 空。
     * <p>为什么另两类不算节拍：{@code DURATION} 是「施加后持续多久」、{@code COOLDOWN} 是「再次触发前的最短间隔」，
     * 二者都不表达「每隔多久重新施加一次」，混用会把节拍与有效期悄悄改成同一个数。
     * <p><b>空 = 未声明节拍 = 每刻驱动</b>（不是「不驱动」）：{@link #drive} 收到空时跳过节流，
     * 对本刻照常走 conditions 门控与 {@code effect.onTrigger}。
     */
    public static OptionalInt intervalOf(StatEntry entry) {
        // 优先读专属节拍字段（StatEntry.interval），缺失时回落 duration 的 INTERVAL 语义（向后兼容）。
        StatDuration inter = entry.interval();
        if (inter != null && inter.kind() == StatDuration.Kind.INTERVAL) return OptionalInt.of(inter.ticks());
        StatDuration d = entry.duration();
        if (d == null || d.kind() != StatDuration.Kind.INTERVAL) return OptionalInt.empty();
        return OptionalInt.of(d.ticks());
    }

    /**
     * 公共单条目驱动：读节拍 → 按 {@code tickCount} 节流 → 过条目 conditions → 调 {@code effect.onTrigger}。
     *
     * <p><b>未声明节拍 = 每刻驱动</b>：{@link #intervalOf} 为空时节拍恒真、不节流，本刻照常走后续门控与施加。
     * 这是 Presence 光环的既定语义——「无 INTERVAL 声明」读作「每刻赋予并刷新」，而非「永不驱动」。
     *
     * <p><b>不预设效果类型</b>：任何 {@link StatEffect.EffectKind#PERIODIC} 的效果都可被驱动
     * （{@code MultiStatEffect} 只是其中之一）。施加由效果自己完成。
     *
     * @param owner         效果持有者（conditions / amount 的求值主体）
     * @param tickCount     调用方自己的 tick 计数（store 路径传 {@code living.tickCount}，技能传实例存活刻）；
     *                      条目未声明节拍时不参与判定
     * @param triggerTarget 事件目标（周期施加无事件目标时传 {@code null}）
     * @return 本刻是否真的施加了一次（未声明节拍时，只要 conditions 与 effect 齐备即返回 {@code true}）
     */
    public static boolean drive(LivingEntity owner, StatEntry entry, long tickCount,
                                StatContext scope, @Nullable LivingEntity triggerTarget,
                                @Nullable Vec3 center) {
        // 未声明节拍 → 每刻驱动：跳过节流、对本刻照常施加（光环条目靠逐刻刷新维持存在）。
        OptionalInt interval = intervalOf(entry);
        if (interval.isPresent() && !beat(tickCount, interval.getAsInt())) return false;
        StatEvalContext evalCtx = new StatEvalContext(owner, triggerTarget, center);
        // 条目条件门控：持有者需满足条目 conditions 才刷新。只判 conditions（不判 tags）：
        // 效果携带的 tags 是被授予条目的作用域标注（如 {damage} / {health,regen}），不是触发门控。
        //
        // 经 Condition 缝判定（解析出的实体集非空即满足）：求值基座由 filter 节点经
        // StatContext#evalBase 组装（注入事件名 / 命中上下文），取代原裸
        // ExpressionContext.get(owner)——后者让 event:/hit_state: 在 Stat 通道恒 0（旧缺陷）。
        // 周期通道无「事件对象」，故 eventEntity 为 null，解析集即 {owner}（或 triggerTarget）。
        if (StatEngine.resolveCondition(entry, evalCtx, scope).isEmpty()) return false;
        StatEffect fx = entry.effect();
        if (fx == null) return false;
        // 目标由各子效果经 entry.forEachTarget 解析，故 triggerTarget 恒可为空；center 供半径 Target 源使用。
        fx.onTrigger(entry, scope, evalCtx, null);
        return true;
    }

    /**
     * 每实体每 tick 调用（{@code AttributeEvent.EntityTickEvent.Pre}）：驱动持有者 STAT_STORE 内的周期条目。
     * 无 store / 服务端外 / 无周期条目 → 零开销短路。
     */
    public static void tick(LivingEntity living) {
        if (living.level().isClientSide()) return;
        if (!living.hasData(AttachmentRegistry.STAT_STORE)) return;
        StatStore store = living.getData(AttachmentRegistry.STAT_STORE);
        List<StatStore.IndexedEntry> entries = store.periodicIndexed();
        if (entries.isEmpty()) return;
        for (StatStore.IndexedEntry ie : entries) {
            drive(living, ie.entry(), living.tickCount, StatContext.EMPTY, null, null);
        }
    }
}
