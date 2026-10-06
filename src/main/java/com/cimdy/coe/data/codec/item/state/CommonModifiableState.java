package com.cimdy.coe.data.codec.item.state;

import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.helper.item.QualityOps;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
/**
 * 物品状态基类 —— 承载 {@code level / rarity / quality / mods} 四个通用维度，实现 {@link ModifiableState}。
 *
 * <p>装备 / 药剂 / 护符等具体状态继承本类，从而共享「物等 + 稀有度 + 品质 + 词缀 + 类型链」这一套基础维度，
 * 不必各自重复声明字段与改写方法。品质在写入基类时即钳制到 {@link CoeConfig#maxQuality} 上限；
 * 词缀在写入时冻结为不可变副本（{@link List#copyOf}），避免状态对象被外部列表回写污染；
 * 类型链由 {@code ItemWeight} 类型树解析后注入，是槽位判定与机制门控的统一权威来源。</p>
 *
 * <p><b>「通用」不等于「每类都用满」</b>（POE2 实证，详见 {@link ModifiableState}）：</p>
 * <ul>
 *   <li>药剂：物等定词缀 tier、稀有度仅到 Magic、品质走玻璃珠、词缀上限 1 前缀 + 1 后缀。</li>
 *   <li>护符：与药剂同口径；品质维度无对应通货，保持惰性缺省 0。</li>
 *   <li>装备：四维全消费。基类统一承载，具体消费由各子类读取点决定。</li>
 * </ul>
 *
 * <p>协变改写方法（{@code withQuality / withRarity / withLevel / withMods}）声明为抽象，强制各具体
 * 状态返回自身类型，从而保留协变返回，使写入调用点（如 {@code QualityOps#add}）能链式得到精确类型。</p>
 */
public abstract class CommonModifiableState implements ModifiableState {

    /** 物品等级（无等级概念的载体可为缺省 1）。 */
    protected final int level;
    /** 稀有度（无稀有度维度的载体为 {@link Rarity#NONE}）。 */
    protected final Rarity rarity;
    /**
     * 类型链（由 {@code ItemWeight} 类型树解析后缓存）。装备 / 药剂 / 护符共用此维度，
     * 是槽位判定与机制门控的统一权威来源——一切「按物品类别判定」的读点都须读此字段。
     * 运行时由 {@code ModGenerator#getItemType} 初始化时写入。
     */
    protected final List<Identifier> types;
    /** 品质（0–{@code MAX}）。 */
    protected final int quality;
    /** 词缀（前缀 / 后缀 / 额外 / 附魔）：装备 / 药剂 / 护符共享的通用维度，不可变列表。 */
    protected final List<ModifierEntry> mods;

    protected CommonModifiableState(int level, Rarity rarity, List<Identifier> types, int quality, List<ModifierEntry> mods) {
        this.level = level;
        this.rarity = rarity == null ? Rarity.NONE : rarity;
        this.types = types == null ? List.of() : List.copyOf(types);
        this.quality = QualityOps.clamp(quality);
        this.mods = mods == null ? List.of() : List.copyOf(mods);
    }

    @Override
    public final int level() {
        return level;
    }

    @Override
    public final Rarity rarity() {
        return rarity;
    }

    @Override
    public final int quality() {
        return quality;
    }

    @Override
    public final List<ModifierEntry> mods() {
        return mods;
    }

    @Override
    public final List<Identifier> types() {
        return types;
    }

    /** 协变改写品质：返回具体状态自身类型的新实例。 */
    @Override
    public abstract CommonModifiableState withQuality(int quality);

    /** 协变改写稀有度：返回具体状态自身类型的新实例。 */
    @Override
    public abstract CommonModifiableState withRarity(Rarity rarity);

    /** 协变改写物等：返回具体状态自身类型的新实例。 */
    @Override
    public abstract CommonModifiableState withLevel(int level);

    /** 协变改写词缀：返回具体状态自身类型的新实例。 */
    @Override
    public abstract CommonModifiableState withMods(List<ModifierEntry> mods);

    /** 协变改写类型链：返回具体状态自身类型的新实例。 */
    @Override
    public abstract CommonModifiableState withType(List<Identifier> types);

    /**
     * 仅比较四个通用维度的相等性（供子类 {@code equals} 调用，避免重复比较）。
     * 注意不限制运行时类型：跨子类的同四维值仍判等，最终由子类 {@code equals} 的
     * {@code instanceof} 排除不同类。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommonModifiableState that)) {
            return false;
        }
        return level == that.level && quality == that.quality && rarity == that.rarity
                && types.equals(that.types) && mods.equals(that.mods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, rarity, quality, types, mods);
    }
}
