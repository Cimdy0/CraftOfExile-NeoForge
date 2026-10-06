package com.cimdy.coe.data.codec.item.flask;

import com.cimdy.coe.data.codec.item.state.CommonModifiableState;
import com.cimdy.coe.data.gen.equipment.EquipmentGen;
import net.minecraft.resources.Identifier;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Objects;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
/**
 * Flask（药剂）物品静态数据 —— 继承 {@link CommonModifiableState}，在通用维度（物等 / 稀有度 / 品质 / 词缀）
 * 之上叠加药剂专属维度（资源键 / 回复量 / 持续 / 充能）。
 *
 * <p>由 {@code record} 改为普通类以承载 {@link CommonModifiableState} 基类（Java record 不能继承类）。</p>
 *
 * <p><b>POE2 实证（poe2db.tw / poe2wiki）—— 药剂四维全消费：</b></p>
 * <ul>
 *   <li>{@code level}：基底自带 {@code Requires: Level N}，决定词缀池中可 roll 的 tier 上限。</li>
 *   <li>{@code rarity}：Normal → Magic 由蜕变石（{@code Orb of Transmutation}）达成；
 *       炼金石（{@code Orb of Alchemy}）对药剂明确不可用，故药剂<b>无 Rare 档</b>。</li>
 *   <li>{@code quality}：仅玻璃珠（{@code Glassblower's Bauble}）可加，每 1% = 1% more 局部回复，
 *       消费点在 {@link com.cimdy.coe.item.flask.FlaskHelper} 与 {@link #qualityScaledRecovery()}。</li>
 *   <li>{@code mods}：词缀上限固定 = 1 前缀 + 1 后缀（共 2 条），tier 随物等提升
 *       （前缀 T1–T18 / 后缀 T1–T9）；可用通货为蜕变 / 增幅 / 改造 / 湮灭 / 混沌 / 神圣。</li>
 * </ul>
 *
 * <p><b>类型链（{@code types}）</b>：与装备同源，由 {@link CommonModifiableState} 持有。运行时经
 * {@code ModGenerator#getItemType} 自 {@code ItemWeight} 类型树解析为 {@code [flask, life_flask]} 之类；
 * 槽位判定（{@link com.cimdy.coe.data.codec.item.mod.identity.ItemTypeChecker#isFlask}）据此统一裁决，
 * 不再依赖物品标签。内置常量 {@link #LIFE} / {@link #MANA} 以 {@code [flask]} 作为兜底类别。</p>
 *
 * <p><b>单轨 API</b>：构造器仅保留唯一的规范 10 参构造器；{@link #create} 是唯一的便捷构造路径
 * （供 CODEC 与内置常量复用）。</p>
 */
public class FlaskState extends CommonModifiableState {

    private final String resource;
    private final float recoveryAmount;
    private final int durationTicks;
    private final int maxCharges;
    private final int chargesPerUse;

    /** 唯一构造器（level / rarity / types / quality / mods 由基类持有，quality 由基类钳制）。 */
    public FlaskState(int level, Rarity rarity, List<Identifier> types, int quality, List<ModifierEntry> mods, String resource,
                          float recoveryAmount, int durationTicks, int maxCharges, int chargesPerUse) {
        super(level, rarity, types, quality, mods);
        this.resource = resource;
        this.recoveryAmount = recoveryAmount;
        this.durationTicks = durationTicks;
        this.maxCharges = maxCharges;
        this.chargesPerUse = chargesPerUse;
    }

    /**
     * 唯一便捷构造：按 (types, resource, recovery, duration, max, perUse, quality, mods) 生成，
     * 补 level / rarity 缺省。兼作 CODEC 的 apply 工厂与内置常量 {@link #LIFE} / {@link #MANA} 的构造口径。
     */
    public static FlaskState create(List<Identifier> types, String resource, float recoveryAmount, int durationTicks,
                                       int maxCharges, int chargesPerUse, int quality, List<ModifierEntry> mods) {
        return new FlaskState(1, Rarity.COMMON, types, quality, mods, resource, recoveryAmount, durationTicks,
                maxCharges, chargesPerUse);
    }

    public static final Codec<FlaskState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.listOf().optionalFieldOf("type", List.of(EquipmentGen.id(EquipmentGen.FLASK))).forGetter(FlaskState::types),
            Codec.STRING.fieldOf("resource").forGetter(FlaskState::resource),
            Codec.FLOAT.fieldOf("recovery_amount").forGetter(FlaskState::recoveryAmount),
            Codec.INT.fieldOf("duration_ticks").forGetter(FlaskState::durationTicks),
            Codec.INT.fieldOf("max_charges").forGetter(FlaskState::maxCharges),
            Codec.INT.fieldOf("charges_per_use").forGetter(FlaskState::chargesPerUse),
            Codec.INT.optionalFieldOf("quality", 0).forGetter(FlaskState::quality),
            ModifierEntry.CODEC.listOf().optionalFieldOf("mods", List.of()).forGetter(FlaskState::mods)
    ).apply(i, FlaskState::create));

    public String resource() { return resource; }
    public float recoveryAmount() { return recoveryAmount; }
    public int durationTicks() { return durationTicks; }
    public int maxCharges() { return maxCharges; }
    public int chargesPerUse() { return chargesPerUse; }

    /**
     * 写入品质并返回新状态（品质为逐栈数据，故以副本承载）。
     * 效果口径：POE2「每 1% 品质 = 1% more 局部生命 / 魔力回复」，消费点在 {@code FlaskHelper}。
     */
    @Override
    public FlaskState withType(List<Identifier> types) {
        return new FlaskState(level, rarity, types, quality, mods, resource, recoveryAmount,
                durationTicks, maxCharges, chargesPerUse);
    }

    @Override
    public FlaskState withQuality(int quality) {
        return new FlaskState(level, rarity, types, quality, mods, resource, recoveryAmount,
                durationTicks, maxCharges, chargesPerUse);
    }

    @Override
    public FlaskState withRarity(Rarity rarity) {
        return new FlaskState(level, rarity, types, quality, mods, resource, recoveryAmount, durationTicks, maxCharges, chargesPerUse);
    }

    @Override
    public FlaskState withLevel(int level) {
        return new FlaskState(level, rarity, types, quality, mods, resource, recoveryAmount, durationTicks, maxCharges, chargesPerUse);
    }

    /** 协变改写词缀（共享维度，实现自 {@link CommonModifiableState#withMods}）。 */
    @Override
    public FlaskState withMods(List<ModifierEntry> mods) {
        return new FlaskState(level, rarity, types, quality, mods, resource, recoveryAmount, durationTicks, maxCharges, chargesPerUse);
    }

    /** 品质加成后的单次恢复总量（读取点统一走本方法，避免散落计算）。 */
    public float qualityScaledRecovery() {
        return recoveryAmount * (1.0F + quality / 100.0F);
    }

    /** POE2 Lesser Life Flask（50 生命 / 3 秒，消耗 10/60 充能）。 */
    public static final FlaskState LIFE = create(List.of(EquipmentGen.id(EquipmentGen.FLASK)), "health", 50f, 60, 60, 10, 0, List.of());

    /** POE2 Lesser Mana Flask（50 魔力 / 2 秒，消耗 10/60 充能）。 */
    public static final FlaskState MANA = create(List.of(EquipmentGen.id(EquipmentGen.FLASK)), "mana", 50f, 40, 60, 10, 0, List.of());

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlaskState that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return Float.compare(that.recoveryAmount, recoveryAmount) == 0
                && durationTicks == that.durationTicks && maxCharges == that.maxCharges
                && chargesPerUse == that.chargesPerUse && Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), resource, recoveryAmount, durationTicks, maxCharges, chargesPerUse);
    }

    @Override
    public String toString() {
        return "FlaskState{level=" + level + ", rarity=" + rarity + ", resource=" + resource
                + ", recoveryAmount=" + recoveryAmount + ", durationTicks=" + durationTicks + ", maxCharges=" + maxCharges
                + ", chargesPerUse=" + chargesPerUse + ", quality=" + quality + ", mods=" + mods + "}";
    }
}
