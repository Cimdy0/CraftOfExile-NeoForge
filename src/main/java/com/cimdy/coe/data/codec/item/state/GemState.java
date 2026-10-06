package com.cimdy.coe.data.codec.item.state;

import com.cimdy.coe.data.codec.support.rarity.Rarity;
import net.minecraft.resources.Identifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Objects;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
/**
 * 宝石物品状态 —— 继承 {@link CommonModifiableState}，在通用维度（物等 / 稀有度 / 品质 / 词缀）之上
 * 叠加宝石专属维度（等阶 tier）。
 *
 * <p>由 {@code record} 改为普通类以承载 {@link CommonModifiableState} 基类（Java record 不能继承类）。</p>
 *
 * <p><b>关键不变量（用户约束）—— 等阶（tier）不是物等（level）：</b></p>
 * <ul>
 *   <li>技能石 / 精魂石的「等阶」是 1–20 的递进档位；辅助石是 1–5。它<b>不</b>等同装备的物等
 *       （{@code level}，决定词缀 tier 上限）。二者语义独立，绝不可混用。</li>
 *   <li>因此 GemState 在基类五维之外单独持有 {@code tier} 字段，<b>不</b>复用 {@code level()} 表示等阶。</li>
 *   <li>品质（{@code quality}）维度技能石可经通货修改（未切割宝石的品质修饰），故保留基类品质字段。</li>
 *   <li>词缀（{@code mods}）、稀有度（{@code rarity}）对宝石为惰性缺省（切割后的具体数值在 SKILL_DATA / SUPPORT_DATA）。</li>
 * </ul>
 *
 * <p><b>单轨 API</b>：仅保留唯一的规范 6 参构造器；{@link #create} 是唯一的便捷构造路径（供 CODEC 复用）。
 * 判别编解码见 {@link ModifiableStateCodec}（必填字段 {@code gem_tier} 与装备 / 药剂 / 护符互斥）。</p>
 */
public class GemState extends CommonModifiableState {

    private final int tier;

    /** 规范构造器（level / rarity / types / quality / mods 由基类持有，tier 为宝石专属）。 */
    public GemState(int level, Rarity rarity, List<Identifier> types, int quality, List<ModifierEntry> mods, int tier) {
        super(level, rarity, types, quality, mods);
        this.tier = tier;
    }

    /**
     * 便捷构造：按 (types, tier, quality, mods) 生成，补 level / rarity 缺省。
     * 宝石无物等 / 稀有度语义，level 取 1、rarity 取 NONE；等阶由 {@code tier} 独立承载。
     */
    public static GemState create(List<Identifier> types, int tier, int quality, List<ModifierEntry> mods) {
        return new GemState(1, Rarity.NONE, types, quality, mods, tier);
    }

    public static final Codec<GemState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.listOf().optionalFieldOf("type", List.of()).forGetter(GemState::types),
            Codec.INT.fieldOf("gem_tier").forGetter(GemState::tier),
            Codec.INT.optionalFieldOf("quality", 0).forGetter(GemState::quality),
            ModifierEntry.CODEC.listOf().optionalFieldOf("mods", List.of()).forGetter(GemState::mods)
    ).apply(i, GemState::create));

    public int tier() { return tier; }

    /** 协变改写等阶（宝石专属维度），返回自身类型新实例。 */
    public GemState withTier(int tier) {
        return new GemState(level, rarity, types, quality, mods, tier);
    }

    // ---- 共享维度改写：物等 / 稀有度 / 品质 / 词缀 / 类型链（宝石仅品质可经通货修改）----
    @Override
    public GemState withType(List<Identifier> types) {
        return new GemState(level, rarity, types, quality, mods, tier);
    }

    @Override
    public GemState withQuality(int quality) {
        return new GemState(level, rarity, types, quality, mods, tier);
    }

    @Override
    public GemState withRarity(Rarity rarity) {
        return new GemState(level, rarity, types, quality, mods, tier);
    }

    @Override
    public GemState withLevel(int level) {
        return new GemState(level, rarity, types, quality, mods, tier);
    }

    @Override
    public GemState withMods(List<ModifierEntry> mods) {
        return new GemState(level, rarity, types, quality, mods, tier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GemState that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return tier == that.tier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tier);
    }

    @Override
    public String toString() {
        return "GemState{level=" + level + ", rarity=" + rarity + ", tier=" + tier
                + ", quality=" + quality + ", mods=" + mods + "}";
    }
}
