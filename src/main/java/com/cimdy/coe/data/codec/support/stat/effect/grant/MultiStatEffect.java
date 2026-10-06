package com.cimdy.coe.data.codec.support.stat.effect.grant;

import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffectTypes;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.event.TriggerContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 复合施加（PERIODIC 型，非触发）：持有任意数量的真实子效果（{@code List<StatEffect>}），
 * 由 {@code PeriodicDriver} 按<b>条目时长段</b>（{@link com.cimdy.coe.data.codec.support.stat.duration.StatDuration.Kind#INTERVAL}）
 * 的节拍，对 {@code entry.target()}（PRESENCE_ENEMIES / PRESENCE_ALLIES / PRESENCE_TARGETS /
 * 任意选择器）解析出的每个目标，逐个调用子效果自身的 {@code onTrigger} 重新施加。
 *
 * <h2>本类不持有任何时长</h2>
 * 时长是 <b>Stat 第五部分</b>（{@link StatEntry#duration()}），对所有 Effect 生效；
 * 本复合类与全部子效果内部<b>一律不再持有自己的时长字段</b>——节拍由驱动从条目读，
 * 施加时长由子效果从条目读。本类因此只剩「子效果容器」这一个职责。
 *
 * <p>取代原 {@code PeriodicStatEffect}（强行填充 sealed Apply 两臂）与 {@code PresenceAuraStatEffect}
 * （AURA 桶）：复合类不做任何「施加分支」——它只是子效果的容器，每个子效果自己知道怎么施加
 * （{@code StatusStatEffect} 走状态通道、{@code ModifierStatEffect} 授予形态经 Target 写各目标 store）。
 *
 * <p>数值语义：复合条目只有一个 {@code amount}（{@code StatEntry} 唯一数值），所有子效果共享它——
 * 需要不同数值的多个效果请拆成多条复合条目。
 *
 * <p><b>本类不持有 tags</b>：容器不是触发亚型、驱动不按 tags 过筛，作用域由各子效果自带。
 */
public final class MultiStatEffect extends AbstractStatEffect {

    public static final MapCodec<MultiStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            StatEffectTypes.MAP_CODEC.codec().listOf().fieldOf("children").forGetter(MultiStatEffect::children)
    ).apply(i, MultiStatEffect::new));

    private final List<StatEffect> children;

    /**
     * 本类<b>不持有作用域门控（tags）</b>：它是 PERIODIC 容器，不是触发亚型
     * （{@link #isTrigger()} 恒 false），{@code PeriodicDriver} 只按条目 conditions 过筛、
     * 从不按 tags 过筛。作用域是「被授予内容」的属性，由<b>每个子效果自己</b>携带
     * （如子 {@code ModifierStatEffect} 的 {@code {damage}}），容器层再存一份只会造成
     * 「父 tags 与子 tags 必须人工保持一致」的重复。
     */
    public MultiStatEffect(List<StatEffect> children) {
        super(GateExpr.ALWAYS);
        if (children == null || children.isEmpty())
            throw new IllegalArgumentException("MultiStatEffect.children 不能为空（至少包含一个子效果）");
        this.children = List.copyOf(children);
    }

    public List<StatEffect> children() {
        return children;
    }

    @Override
    public String type() {
        return "multi";
    }

    @Override
    public boolean isTrigger() {
        return false;
    }

    @Override
    public StatEffect.EffectKind indexKind() {
        return StatEffect.EffectKind.PERIODIC;
    }

    /**
     * 复合分发：由 PeriodicDriver 每节拍调用一次。时长由各子效果自行从
     * {@code entry.duration()}（条目第五部分）读取，本方法不传递任何时长。
     *
     * <p>目标解析由各子效果的 {@code onTrigger} 内部经 {@code entry.forEachTarget} 完成——
     * 驱动与复合本身都不预先解析目标、不写施加分支。
     */
    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        for (StatEffect child : children) {
            if (child instanceof ModifierStatEffect m) {
                m.grantPeriodic(entry, scope, evalCtx);
            } else {
                child.onTrigger(entry, scope, evalCtx, tc);
            }
        }
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
        List<Component> out = new java.util.ArrayList<>();
        for (StatEffect child : children) {
            out.addAll(child.describeFragment(entry, locale));
        }
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MultiStatEffect e)) return false;
        return Objects.equals(children, e.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children);
    }

    @Override
    public String toString() {
        return "MultiStatEffect[" + children.size() + " children]";
    }
}
