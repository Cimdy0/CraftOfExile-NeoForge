package com.cimdy.coe.data.codec.item.state;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import com.cimdy.coe.data.codec.item.tiered.Bonus;
import com.cimdy.coe.data.codec.item.tiered.Tier;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

/**
 * 符文条目（一名 × 一阶）：仅承载该阶属性 —— {@code name} / {@code tier} / 该阶 {@code branches}。
 *
 * <p>id 与需求等级<b>均由 {@link Tier} 派生</b>，不在本类另算：{@link #id()} = {@code 阶前缀 + 名路径 + "_Rune"}
 * （如 {@code Lesser_Desert_Rune}），{@link #levelReq()} = {@link Tier#levelReq()}（0 / 15 / 35 / 60）。
 *
 * <p><b>符文不渲染名称</b>：{@code name} 只作条目身份（id）、贴图键与翻译键；显示名 = {@code T<阶号>} + 「符文」
 * 两段（{@link Tier#label()} 深灰着色前缀，与 {@code CommonMod} 的阶级渲染同法）—— 符文本身是单独一类，
 * 名称由效果承担、不进商品名。模型贴图 = {@code item/rune/<阶>_<名路径>_rune}。
 * 这套派生与精髓<b>同构</b>，已收敛到 {@link Tier}（按 {@link Tier.Carrier#RUNE} 取符文字面量），
 * 本类只提供「符文」这一侧的语义入口。</p>
 */
public record RuneState(Identifier name, Tier tier, Map<String, List<Bonus>> branches) implements ModifiableState {

    /** 条目 id：{@code <阶前缀><名路径>_Rune}。 */
    public String id() {
        return idOf(name, tier);
    }

    /** 按名 + 阶拼条目 id（Masterwork 升阶等「换阶不改名」场景用）。 */
    public static String idOf(Identifier name, Tier tier) {
        return tier.id(Tier.Carrier.RUNE, name.getPath());
    }

    /** 需求等级：由阶派生（符文专有维度）。 */
    public int levelReq() {
        return tier.levelReq();
    }

    /**
     * 显示名：{@code T<阶号>} + 「符文」—— <b>不渲染符文名</b>
     * （符文为单独一类：{@code name} 只作条目身份 / 贴图键 / 翻译键）。
     */
    public MutableComponent displayName() {
        return Component.literal(tier.label())
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("item.coe.rune"));
    }

    /** 模型贴图路径：{@code item/rune/<阶>_<族名>_rune}。 */
    public String modelTexture() {
        return tier.modelTexture(Tier.Carrier.RUNE, name.getPath());
    }

    /** 取某宿主分支的加成列表（分支缺失返回空）。 */
    public List<Bonus> branch(String key) {
        return branches.getOrDefault(key, List.of());
    }

    // ── ModifiableState 实现：符文无物等 / 稀有度 / 品质 / 词缀，类型链为 ["rune"] ──

    @Override
    public int level() {
        return 1;
    }

    @Override
    public Rarity rarity() {
        return Rarity.NONE;
    }

    @Override
    public int quality() {
        return 0;
    }

    @Override
    public List<ModifierEntry> mods() {
        return List.of();
    }

    @Override
    public List<Identifier> types() {
        return List.of(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rune"));
    }

    @Override
    public ModifiableState withQuality(int quality) {
        return this;
    }

    @Override
    public ModifiableState withRarity(Rarity rarity) {
        return this;
    }

    @Override
    public ModifiableState withLevel(int level) {
        return this;
    }

    @Override
    public ModifiableState withMods(List<ModifierEntry> mods) {
        return this;
    }

    @Override
    public ModifiableState withType(List<Identifier> types) {
        return this;
    }
}
