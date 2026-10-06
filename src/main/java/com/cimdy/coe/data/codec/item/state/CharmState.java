package com.cimdy.coe.data.codec.item.state;

import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.gen.equipment.EquipmentGen;
import com.cimdy.coe.item.charm.CharmTrigger;
import net.minecraft.resources.Identifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Objects;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
/**
 * Charm（护符）物品静态数据 —— 继承 {@link CommonModifiableState}，在通用维度（物等 / 稀有度 / 品质 / 词缀）
 * 之上叠加护符专属维度（触发 / 效果 / 标签 / 持续 / 充能）。
 *
 * <p>原实现为 {@code record}；为承载 {@link CommonModifiableState} 基类（Java record 不能继承类），
 * 改为普通类并保持公共契约一致（含 {@link #of}）。</p>
 *
 * <p><b>POE2 实证（poe2db.tw / poe2wiki）—— 护符与药剂同口径：</b></p>
 * <ul>
 *   <li>{@code level}：基底自带 {@code Requires: Level N}，决定词缀池可 roll 的 tier 上限。</li>
 *   <li>{@code rarity}：Normal → Magic 由蜕变石达成；炼金石对护符不可用，故<b>无 Rare 档</b>。</li>
 *   <li>{@code quality}：护符无对应品质通货，本维度保持惰性缺省 0。</li>
 *   <li>{@code mods}：词缀上限 = 1 前缀 + 1 后缀（共 2 条），tier 随物等提升。</li>
 * </ul>
 *
 * <p>{@code effects} 为护符<b>基底固有</b>效果（触发时注入玩家属性层），与 {@code mods} 的
 * 可 roll 词缀为两条独立通道：前者是基底数据，后者由通货 / 掉落 roll 产生。</p>
 *
 * <p><b>类型链（{@code types}）</b>：与装备 / 药剂同源，由 {@link CommonModifiableState} 持有。运行时经
 * {@code ModGenerator#getItemType} 自 {@code ItemWeight} 类型树解析为 {@code [accessory/charm, accessory/charm/ruby]} 之类；
 * 槽位判定（{@link com.cimdy.coe.data.codec.item.mod.identity.ItemTypeChecker#isCharm}）据此统一裁决，不再依赖物品标签。
 * 便捷工厂 {@link #of} 以 {@code [accessory/charm]} 作为兜底类别。</p>
 */
public class CharmState extends CommonModifiableState {

    private final CharmTrigger trigger;
    private final List<StatEntry> effects;
    private final String effectLabel;
    private final int durationTicks;
    private final int chargesUsed;
    private final int maxCharges;

    /** 规范构造器（level / rarity / types / quality / mods 由基类持有）。 */
    public CharmState(int level, Rarity rarity, List<Identifier> types, int quality, List<ModifierEntry> mods, CharmTrigger trigger,
                          List<StatEntry> effects, String effectLabel, int durationTicks, int chargesUsed,
                          int maxCharges) {
        super(level, rarity, types, quality, mods);
        this.trigger = trigger;
        this.effects = effects;
        this.effectLabel = effectLabel;
        this.durationTicks = durationTicks;
        this.chargesUsed = chargesUsed;
        this.maxCharges = maxCharges;
    }

    public static final Codec<CharmState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.listOf().optionalFieldOf("type", List.of(EquipmentGen.id(EquipmentGen.CHARM_PATH))).forGetter(CharmState::types),
            CharmTrigger.CODEC.fieldOf("trigger").forGetter(CharmState::trigger),
            StatEntry.CODEC.listOf().fieldOf("effects").forGetter(CharmState::effects),
            Codec.STRING.fieldOf("effect_label").forGetter(CharmState::effectLabel),
            Codec.INT.fieldOf("duration_ticks").forGetter(CharmState::durationTicks),
            Codec.INT.fieldOf("charges_used").forGetter(CharmState::chargesUsed),
            Codec.INT.fieldOf("max_charges").forGetter(CharmState::maxCharges),
            ModifierEntry.CODEC.listOf().optionalFieldOf("mods", List.of()).forGetter(CharmState::mods)
    ).apply(i, CharmState::create));

    /** CODEC 工厂：JSON 字段顺序为 (type, trigger, effects, label, duration, used, max, mods)，补 level/rarity/quality 缺省。 */
    private static CharmState create(List<Identifier> types, CharmTrigger trigger, List<StatEntry> effects, String effectLabel,
                                         int durationTicks, int chargesUsed, int maxCharges, List<ModifierEntry> mods) {
        return new CharmState(1, Rarity.COMMON, types, 0, mods, trigger, effects, effectLabel, durationTicks, chargesUsed, maxCharges);
    }

    public CharmTrigger trigger() { return trigger; }
    public List<StatEntry> effects() { return effects; }
    public String effectLabel() { return effectLabel; }
    public int durationTicks() { return durationTicks; }
    public int chargesUsed() { return chargesUsed; }
    public int maxCharges() { return maxCharges; }

    /** 便捷工厂：无词缀、默认充能上限 80（POE2 基础规格）、兜底类别 {@code [accessory/charm]}。 */
    public static CharmState of(CharmTrigger trigger, List<StatEntry> effects, String effectLabel,
                                    int durationTicks, int chargesUsed) {
        return new CharmState(1, Rarity.COMMON, List.of(EquipmentGen.id(EquipmentGen.CHARM_PATH)), 0, List.of(), trigger, effects, effectLabel, durationTicks, chargesUsed, 80);
    }

    // ---- 共享维度改写：物等 / 稀有度 / 品质 / 词缀 / 类型链（护符已消费前两者与词缀，品质为惰性缺省）----
    @Override
    public CharmState withType(List<Identifier> types) {
        return new CharmState(level, rarity, types, quality, mods, trigger, effects, effectLabel, durationTicks, chargesUsed, maxCharges);
    }

    @Override
    public CharmState withQuality(int quality) {
        return new CharmState(level, rarity, types, quality, mods, trigger, effects, effectLabel, durationTicks, chargesUsed, maxCharges);
    }

    @Override
    public CharmState withRarity(Rarity rarity) {
        return new CharmState(level, rarity, types, quality, mods, trigger, effects, effectLabel, durationTicks, chargesUsed, maxCharges);
    }

    @Override
    public CharmState withLevel(int level) {
        return new CharmState(level, rarity, types, quality, mods, trigger, effects, effectLabel, durationTicks, chargesUsed, maxCharges);
    }

    @Override
    public CharmState withMods(List<ModifierEntry> mods) {
        return new CharmState(level, rarity, types, quality, mods, trigger, effects, effectLabel, durationTicks, chargesUsed, maxCharges);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CharmState that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return durationTicks == that.durationTicks && chargesUsed == that.chargesUsed && maxCharges == that.maxCharges
                && Objects.equals(trigger, that.trigger) && Objects.equals(effects, that.effects)
                && Objects.equals(effectLabel, that.effectLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), trigger, effects, effectLabel, durationTicks, chargesUsed, maxCharges);
    }

    @Override
    public String toString() {
        return "CharmState{level=" + level + ", rarity=" + rarity + ", trigger=" + trigger + ", effects=" + effects
                + ", effectLabel=" + effectLabel + ", durationTicks=" + durationTicks + ", chargesUsed=" + chargesUsed
                + ", maxCharges=" + maxCharges + ", quality=" + quality + ", mods=" + mods + "}";
    }
}
