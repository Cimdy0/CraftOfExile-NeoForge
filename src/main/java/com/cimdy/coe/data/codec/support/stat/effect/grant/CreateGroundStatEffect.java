package com.cimdy.coe.data.codec.support.stat.effect.grant;

import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.helper.engine.ground.GroundFieldManager;

import com.cimdy.coe.data.helper.engine.ground.CreateGroundDriver;

import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * 地面生成（GROUND 型，非触发）：词缀佩戴者每 {@code interval_ticks} tick 在脚下生成一片
 * {@code ground_status} 指定的地面场（由 {@code GroundFieldManager} 投放：随维度附件存档、
 * 服务端运算、客户端简单渲染），效果由 {@link com.cimdy.coe.data.codec.support.status.registry.ground.GroundTypes#def}
 * 解析的 {@code stats} 载荷提供（直接注入范围内实体 STAT_STORE，无存在性状态层，R3 安全）。
 *
 * <p>与 {@link PresenceAuraStatEffect}（AURA 型）同构：非触发、由独立管道
 * {@code CreateGroundDriver} 每 tick 节流驱动（不混入 Presence 光环施加）。区别于
 * 「造成异常时落地面」（{@code create_ground_on_ailment}）：本效果<b>常驻周期性</b>生成，
 * 适用于「佩戴者持续在脚下留下 X 地面」（如「站立时生成点燃地面」类词缀）。
 *
 * <p><b>数值语义</b>：本效果只配置「生成哪片地面 + 几何参数」（半径 / 时长 / 节拍），
 * 地面本身的效果幅度由 {@code GroundTypes.def(ground_status).stats()} 数据承载（数据驱动，
 * 符合用户裁定「地面效果 = {@code List<Stat>}」）。条目 {@code amount} 不在此类使用。
 */
public class CreateGroundStatEffect extends AbstractStatEffect {

    private final String groundId;
    private final float radius;
    private final float duration;
    private final int intervalTicks;

    public CreateGroundStatEffect(String groundId, float radius, float duration, int intervalTicks, GateExpr tags) {
        super(tags);
        this.groundId = groundId;
        this.radius = Math.max(0.5F, radius);
        this.duration = Math.max(1.0F, duration);
        this.intervalTicks = Math.max(1, intervalTicks);
    }

    public static final MapCodec<CreateGroundStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("ground_status").forGetter(CreateGroundStatEffect::groundId),
            Codec.FLOAT.optionalFieldOf("radius", 4.0F).forGetter(CreateGroundStatEffect::radius),
            Codec.FLOAT.optionalFieldOf("duration", 100.0F).forGetter(CreateGroundStatEffect::duration),
            Codec.INT.optionalFieldOf("interval_ticks", 20).forGetter(CreateGroundStatEffect::intervalTicks),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, CreateGroundStatEffect::new));

    public String groundId() {
        return groundId;
    }

    public float radius() {
        return radius;
    }

    public float duration() {
        return duration;
    }

    public int intervalTicks() {
        return intervalTicks;
    }

    @Override
    public String type() {
        return "create_ground_self";
    }

    /** 非触发亚型：由 CreateGroundDriver 每 tick 节流驱动，不进 StatEngine.fire 触发通道。 */
    @Override
    public boolean isTrigger() {
        return false;
    }

    /** 归入 GROUND 索引：由 CreateGroundDriver 全量扫描驱动（见 StatStore 分类开关）。 */
    @Override
    public EffectKind indexKind() {
        return EffectKind.GROUND;
    }

    @Override
    public List<Component> describe(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    @Override
    public List<Component> describeFragment(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        return List.of(Component.translatable("stat_effect.create_ground_self",
                StatValueKey.of("ground", groundId),
                Component.literal(String.valueOf(radius)),
                Component.literal(String.valueOf(duration)),
                Component.literal(String.valueOf(intervalTicks))));
    }
}
