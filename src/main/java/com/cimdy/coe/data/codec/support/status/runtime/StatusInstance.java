package com.cimdy.coe.data.codec.support.status.runtime;

import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.registry.DamageTypeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 单一状态实例（挂在 {@link StatusStore} 上，取代原 MobEffectInstance 特例 + StatusContents）。
 *
 * <p>统一承载：强度 magnitude / 剩余时长 durationTicks / 层数 stacks / 施加者 source /
 * 积累值 buildup（DoT 类型用于触发阈值）/ 自定义载荷 payload（如寒霜之捷附带的伤害类型）。
 */
public final class StatusInstance {

    public final StatusType type;
    public double magnitude;
    public int durationTicks;
    public int stacks;
    public UUID source;
    public double buildup;
    /** 非序列化自定义数据（如捷附带的元素伤害类型标签），仅运行时使用 */
    public Object payload;
    /** 非序列化：由技能施加时携带技能图标，HUD 优先使用；为 null 时回退 {@link StatusType#icon()}。 */
    public Identifier iconOverride;
    /**
     * DoT 拆分：按来源伤害类型的原始命中伤害分量（索引对齐 DamageTypeRegistry.DAMAGE_TYPE_TAGS：
     * 0=物理 1=混沌 2=火焰 3=冰霜 4=闪电）。仅伤害性异常（中毒/点燃/流血）使用；为 null 表示非拆分（如控制类异常）。
     * 输出伤害类型不在此冻结——由伤害事件（LivingDamageEventPre）的预减免阶段经施法者 Stat 动态重路由。
     */
    public double[] dotComposition;
    /** 施加时刻的 level.getGameTime()（tick）：用于「状态存续时长」条件（如流血 >4s）。序列化持久化，缺省 0。 */
    public long appliedTick;
    /** 加重（Aggravated）标记：仅流血/点燃/中毒等 DoT 异常实例使用；DoT 结算时按 (1+aggravatedFraction) 放大（PLAN.md S1）。非序列化缺省 0。 */
    public double aggravatedFraction;
    /** 重晕档标记：仅 STUN 状态实例使用；重晕时置 true，轻晕置 false。非序列化缺省 false。 */
    public boolean heavy;
    /** 完全破甲档标记：仅 ARMOUR_BREAK 状态实例使用；护甲归零时置 true，轻破甲置 false。非序列化缺省 false。 */
    public boolean fullyBroken;
    /** 由施加系统统一计算的状态效果幅度倍率（进攻侧 AILMENT_MAGNITUDE × 防守侧 taken AILMENT_MAGNITUDE × 光环额外 {aura} 倍率）。缺省 1.0 = 不额外增强。 */
    public double effectMul = 1.0;
    /**
     * 是否由光环/存在范围授予。
     *
     * <p><b>⚠ 已知易错点：本字段只是 {@code StatusEngine.effectMul} 的计算中间量，<b>零读点</b>。</b>
     * 全仓无任何代码读取 {@code inst.auraSourced}——{@code effectMul} 只在 {@code StatusEngine.apply} 的
     * 7 参 / 9 参重载里<b>就地</b>由形参 {@code auraSourced} 算出并写入 {@link #effectMul}，
     * 本字段只是被顺手写一份留痕。<b>给本字段赋值不改变任何行为。</b></p>
     *
     * <p>想改光环倍率，唯一正确路径是<b>让 {@code StatusEngine.apply} 的 7 参重载被调用</b>
     * （形参 {@code auraSourced=true}），由它重算 {@link #effectMul}；直接写
     * {@code inst.auraSourced = true} 是无效操作。</p>
     */
    public boolean auraSourced = false;

    public StatusInstance(StatusType type, double magnitude, int durationTicks, int stacks,
                          UUID source, double buildup, Object payload, Identifier iconOverride) {
        this(type, magnitude, durationTicks, stacks, source, buildup, payload, iconOverride, null, 0L);
    }

    public StatusInstance(StatusType type, double magnitude, int durationTicks, int stacks,
                          UUID source, double buildup, Object payload) {
        this(type, magnitude, durationTicks, stacks, source, buildup, payload, null, null, 0L);
    }

    public StatusInstance(StatusType type, double magnitude, int durationTicks, int stacks,
                          UUID source, double buildup) {
        this(type, magnitude, durationTicks, stacks, source, buildup, null, null, null, 0L);
    }

    public StatusInstance(StatusType type, double magnitude, int durationTicks, int stacks,
                          UUID source, double buildup, Object payload, Identifier iconOverride,
                          double[] dotComposition) {
        this(type, magnitude, durationTicks, stacks, source, buildup, payload, iconOverride, dotComposition, 0L);
    }

    /** 规范构造（含存续起点 appliedTick，由 StatusEngine.apply 在施加时记录 level.getGameTime()）。 */
    public StatusInstance(StatusType type, double magnitude, int durationTicks, int stacks,
                          UUID source, double buildup, Object payload, Identifier iconOverride,
                          double[] dotComposition, long appliedTick) {
        this.type = type;
        this.magnitude = magnitude;
        this.durationTicks = durationTicks;
        this.stacks = stacks;
        this.source = source;
        this.buildup = buildup;
        this.payload = payload;
        this.iconOverride = iconOverride;
        this.dotComposition = dotComposition;
        this.appliedTick = appliedTick;
    }

    /** 时长到期（beat 类无时长状态以 stacks>0 判定）。 */
    public boolean isExpired() {
        return durationTicks <= 0 && !type.stackable;
    }

    /** 叠加效果幅度后的强度（供按 magnitude 推导的 stat 提供器使用）。 */
    public double scaledMagnitude() { return magnitude * effectMul; }
    /** 叠加效果幅度后的层数（供按 stacks 推导的 stat 提供器使用）。 */
    public int scaledStacks() { return (int) Math.round(stacks * effectMul); }

    // ── Codec（payload / 拆分数组参与序列化） ──
    public static final Codec<StatusInstance> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("type").forGetter(s -> s.type.id),
            Codec.DOUBLE.fieldOf("magnitude").forGetter(s -> s.magnitude),
            Codec.INT.fieldOf("duration").forGetter(s -> s.durationTicks),
            Codec.INT.fieldOf("stacks").forGetter(s -> s.stacks),
            UUIDUtil.CODEC.optionalFieldOf("source").forGetter(s -> Optional.ofNullable(s.source)),
            Codec.DOUBLE.fieldOf("buildup").forGetter(s -> s.buildup),
            Codec.DOUBLE.listOf().optionalFieldOf("dot_comp").forGetter(s -> Optional.ofNullable(toDoubleList(s.dotComposition))),
            Codec.LONG.optionalFieldOf("applied_tick", 0L).forGetter(s -> s.appliedTick),
            Codec.DOUBLE.optionalFieldOf("aggravated", 0.0).forGetter(s -> s.aggravatedFraction)
    ).apply(i, (typeId, mag, dur, st, src, bu, comp, tick, agg) -> {
        StatusInstance r = new StatusInstance(StatusType.byId(typeId).orElseThrow(
                () -> new IllegalStateException("unknown status type: " + typeId)),
                mag, dur, st, src.orElse(null), bu, null, null,
                toDoubleArray(comp.orElse(null)), tick);
        r.aggravatedFraction = agg;
        return r;
    }));

    private static List<Double> toDoubleList(double[] a) {
        if (a == null) return null;
        List<Double> l = new ArrayList<>();
        for (double v : a) l.add(v);
        return l;
    }

    private static double[] toDoubleArray(List<Double> l) {
        if (l == null) return null;
        double[] a = new double[l.size()];
        for (int k = 0; k < l.size(); k++) a[k] = l.get(k);
        return a;
    }
}
