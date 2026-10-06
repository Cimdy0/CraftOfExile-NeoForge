package com.cimdy.coe.data.codec.item.mod.core.impl;

import com.cimdy.coe.data.codec.item.mod.core.AbstractMod;
import com.cimdy.coe.data.codec.item.mod.core.ModLayer;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntries;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.MutableComponent;
import java.util.Objects;
import java.util.Optional;

/**
 * 符文加成 —— 镶嵌产物，落 {@link ModLayer#RUNE} 层。

 *
 * <p>与下缀的本质区别：</p>
 * <ul>
 *   <li><b>不渲染名称</b>（{@link #name()} 恒空）；</li>
 *   <li>没有前后缀位置，不占 {@code ModNumber} 档位；</li>
 *   <li>min~max 是「附加 X 到 Y 点伤」的<b>伤害区间本身</b>，取中值落定、<b>不随机</b>；</li>
 *   <li>{@link #level()} = 符文阶的需求等级（0 / 15 / 35 / 60），参与装备需求等级取大。</li>
 * </ul>
 *
 * <p>{@code source} 记录来源符文条目 id（如 {@code Lesser_Desert_Rune}），供移除 / 升阶定位。</p>
 */
public final class RuneMod extends AbstractMod {

    public static final MapCodec<RuneMod> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.fieldOf("source").forGetter(RuneMod::source),
                    Addition.CODEC.fieldOf("addition").forGetter(RuneMod::addition),
                    Codec.INT.fieldOf("level").forGetter(RuneMod::level)
            ).apply(instance, RuneMod::new));

    private final String source;

    public RuneMod(String source, Addition addition, int level) {
        super(addition, level);
        this.source = source;
    }

    /** 来源符文条目 id（镶嵌侧按此定位移除 / 升阶）。 */
    public String source() {
        return source;
    }

    @Override
    public ModLayer layer() {
        return ModLayer.RUNE;
    }

    @Override
    public Optional<MutableComponent> name() {
        return Optional.empty();
    }

    @Override
    public ModifierEntry.Type<RuneMod> getType() {
        return ModifierEntries.RUNE;
    }

    /**
     * 符文<b>不是范围态</b>（不实现 {@link RangedMod}）：min~max 是伤害区间本身而非取值范围，
     * 构造时即按中值落定，重掷不改变数值 —— 故此处刻意不提供 {@code roll}。
     */

    /** 以新加成重建（升阶换符文时用，数值恒为中值）。 */
    public RuneMod withAddition(Addition addition) {
        return new RuneMod(source, addition, level());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof RuneMod that
                && source.equals(that.source)
                && level() == that.level()
                && Objects.equals(addition(), that.addition());
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, level(), addition());
    }

    @Override
    public String toString() {
        return "RuneMod{source=" + source + ", level=" + level() + ", addition=" + addition() + "}";
    }

    /** 中值：符文 min~max 是伤害区间本身，按既定口径取 (min+max)/2。 */
    public static Addition midValue(Addition addition) {
        return addition.max().isPresent()
                ? Addition.from(addition.type(), (addition.min() + addition.max().get()) / 2.0)
                : addition;
    }
}
