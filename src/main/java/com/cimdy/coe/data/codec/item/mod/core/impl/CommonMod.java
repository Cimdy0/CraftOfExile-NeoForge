package com.cimdy.coe.data.codec.item.mod.core.impl;

import com.cimdy.coe.data.codec.item.mod.core.ModifierEntries;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;

import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.cimdy.coe.data.codec.item.mod.value.ModTier;
import com.cimdy.coe.data.codec.support.stat.entry.ValueRange;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

import java.util.Objects;
import java.util.Optional;

/**
 * 普通词缀（下缀的一种）—— 占据装备一条原生前后缀槽，由候选池 roll 产出。

 *
 * <p>继承 {@link AffixMod}（因而也是 {@link RangedMod} 范围态）：位置与冲突路径由下缀基类承载，
 * 本类只补充普通词缀专有的<b>阶名词（{@code mod}）与阶级（{@code tier}）</b> —— 名称渲染为
 * 词缀阶级名（{@code item.mod.coe.<阶名词>}），与精髓词缀渲染「精髓」相区别。</p>
 *
 * <p>冲突由基类按 Addition 注册身份（或显式 {@code conflict} 路径）派生，本类不持有任何冲突 id。</p>
 *
 * <p>继承体系：Java record 不能继承类，而本体系要求「普通词缀继承下缀」，故本类为普通类；
 * 保留 5 参构造器（缺省冲突 = 派生）与全部访问器，调用点不变。</p>
 */
public final class CommonMod extends AffixMod {

    public static final MapCodec<CommonMod> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(CommonMod::mod),
                    ModTier.CODEC.fieldOf("tier").forGetter(CommonMod::tier),
                    Codec.INT.fieldOf("level").forGetter(CommonMod::level),
                    Addition.CODEC.fieldOf("addition").forGetter(CommonMod::addition),
                    Location.CODEC.fieldOf("location").forGetter(CommonMod::location),
                    Identifier.CODEC.optionalFieldOf("conflict").forGetter(CommonMod::conflict)
            ).apply(instance, CommonMod::new));

    /** 阶名词（渲染键 {@code item.mod.coe.<mod>}）。 */
    private final String mod;
    /** 阶级（着色用；{@code ModTier(max, value)}）。 */
    private final ModTier tier;

    /** 五参构造（历史调用点 / 数据解码缺省）：冲突路径默认空（{@link Optional#empty()}，由 Addition 注册身份派生）。 */
    public CommonMod(String mod, ModTier tier, int level, Addition addition, Location location) {
        this(mod, tier, level, addition, location, Optional.empty());
    }

    /** 全参构造：显式指定冲突路径（手动填写），空 {@link Optional} 表示由 Addition 注册身份派生。 */
    public CommonMod(String mod, ModTier tier, int level, Addition addition, Location location, Optional<Identifier> conflict) {
        super(addition, level, location, conflict);
        this.mod = mod;
        this.tier = tier == null ? ModTier.EMPTY : tier;
    }

    /** 阶名词（渲染键 {@code item.mod.coe.<mod>}）。 */
    public String mod() {
        return mod;
    }

    /** 阶级（着色用）。 */
    public ModTier tier() {
        return tier;
    }

    @Override
    public Optional<MutableComponent> name() {
        return Optional.of(Component.translatable("item.mod.coe." + mod).withStyle(tier.getColor()));
    }

    @Override
    public ModifierEntry.Type<CommonMod> getType() {
        return ModifierEntries.COMMON;
    }

    /**
     * 词缀效果展示：经 {@link com.cimdy.coe.data.codec.item.addition.AdditionType#describe} 单轨输出。
     * 全局 / 局部 / 组合种类各自多态给出片段（局部经关联符 → 真实属性再渲染）。
     */
    public MutableComponent getComponent() {
        return getComponent(false);
    }

    /**
     * 词缀效果展示：经 {@link com.cimdy.coe.data.codec.item.addition.AdditionType#describe} 单轨输出。
     * {@code showRange = true} 时注入词缀 roll 区间（Shift 范围模式），渲染 {@code value(min-max)}。
     */
    public MutableComponent getComponent(boolean showRange) {
        MutableComponent comp = Component.literal("").withStyle(tier.getColor());
        Addition addition = addition();
        if (addition != null) {
            Optional<ValueRange> range = showRange ? additionRange() : Optional.empty();
            addition.type().describe(addition.getValue(), ClientLocale.get(), range).forEach(comp::append);
        }
        return comp;
    }

    /** 词缀 roll 区间（来自 Addition.min~max），仅 Shift 范围模式注入描述链。 */
    private Optional<ValueRange> additionRange() {
        Addition addition = addition();
        return addition != null && addition.max().isPresent()
                ? Optional.of(new ValueRange(addition.min(), addition.max().get()))
                : Optional.empty();
    }

    /** 数值随机落定（范围态）：返回保留阶名 / 阶级 / 位置的新实例。 */
    public CommonMod random(RandomSource random) {
        return (CommonMod) roll(random);
    }

    @Override
    public CommonMod withAddition(Addition addition) {
        return new CommonMod(mod, tier, level(), addition, location(), conflict());
    }

    @Override
    public CommonMod withLocation(Location location) {
        return new CommonMod(mod, tier, level(), addition(), location, conflict());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof CommonMod that
                && conflict().equals(that.conflict())
                && mod.equals(that.mod)
                && tier.equals(that.tier)
                && level() == that.level()
                && location() == that.location()
                && Objects.equals(addition(), that.addition());
    }

    @Override
    public int hashCode() {
        return Objects.hash(mod, conflict(), tier, level(), addition(), location());
    }

    @Override
    public String toString() {
        return "CommonMod{mod=" + mod + ", conflict=" + conflict() + ", tier=" + tier
                + ", level=" + level() + ", addition=" + addition() + ", location=" + location() + "}";
    }
}
