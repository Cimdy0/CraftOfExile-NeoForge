package com.cimdy.coe.data.codec.item.essence;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.state.ModifiableState;
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
 * 精髓条目（一名 × 一阶）：仅承载该阶属性 —— {@code name} / {@code tier} / {@code location} / 该阶 {@code branches}。
 *
 * <p><b>名称即身份</b>：{@code name} 是精髓名（{@code Identifier}，如 {@code coe:abrasion}），直接充当条目身份 ——
 * {@link #id()} = {@code <阶前缀><名路径>_essence}（如 {@code perfect_abrasion_essence}），是注册键；
 * 精髓<b>不设「族名」</b>这一层，名称也不进显示名（显示名走 {@link #displayName()} 的 {@code T<阶号>} 段）；
 * 名称不代表效果，效果只用于冲突判定。
 * 精髓<b>没有需求等级概念</b>（该维度为符文专有：{@code RuneState#levelReq()}），故不提供 {@code levelReq}。</p>
 *
 *
 * <p>名称与模型路径同样委托 {@link Tier}：<b>显示名 = {@code T<阶号>} + 精髓类名 + 「精髓」</b>
 * （{@code T<阶号>} 段即 {@link Tier#label()} 的深灰着色前缀，与 {@code CommonMod} 的阶级渲染同法）；
 * 模型贴图 = {@code item/essence/<阶>_<名路径>_essence}。
 * 这套派生与符文<b>同构</b>，已收敛到 {@link Tier}（按 {@link Tier.Carrier#ESSENCE} 取精髓字面量），
 * 本类只提供「精髓」这一侧的语义（{@code location} / {@link #upgrades()}）。</p>
 */
public record EssenceState(Identifier name, Tier tier, Map<String, List<Bonus>> branches) implements ModifiableState {

    /** 条目 id：{@code <阶前缀><精髓名路径>_essence}。 */
    public String id() {
        return tier.id(Tier.Carrier.ESSENCE, name.getPath());
    }

    /**
     * 显示名：{@code T<阶号>} + 精髓类名 + 「精髓」。
     *
     * <p>阶号段（{@link Tier#label()}）与 {@code CommonMod} 的阶级渲染同法：深灰 {@code T<n>} 字面量前缀。</p>
     */
    public MutableComponent displayName() {
        return Component.literal(tier.label())
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("item.coe.essence." + name.getPath()))
                .append(Component.translatable("item.coe.essence"));
    }

    /** 模型贴图路径：{@code item/essence/<阶>_<名路径>_essence}。 */
    public String modelTexture() {
        return tier.modelTexture(Tier.Carrier.ESSENCE, name.getPath());
    }

    /**
     * 是否为「升阶」阶（lesser / normal / greater，即前三阶）：作用于<b>魔法</b>物品，
     * 将其升为稀有并<b>必定增加一项</b>词缀（保留既有词缀）。
     *
     * <p>终阶（第四阶 perfect / 单阶 corrupted）返回 false：作用于<b>稀有</b>物品，
     * 先移除一条随机词缀，再添加必定出现的精髓词缀。</p>
     */
    public boolean upgrades() {
        return tier.upgrades();
    }

    /** 取某宿主分支的加成列表（分支缺失返回空）。 */
    public List<Bonus> branch(String key) {
        return branches.getOrDefault(key, List.of());
    }

    // ── ModifiableState 实现：精髓无物等 / 稀有度 / 品质 / 词缀，类型链为 ["essence"] ──

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
        return List.of(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "essence"));
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
