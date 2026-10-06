package com.cimdy.coe.data.helper.affix;

import com.cimdy.coe.data.helper.stat.StatStoreHelper;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.event.combat.AttributeEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.entity.LivingMod;
import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.event.data.LoadEvent;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 实体词缀（{@link LivingMod}）抽选与固化机制。
 *
 * <p>原内联于 {@code AttributeEvent}（modGen / grantModSkills / withConditions / getModNumber /
 * 权重表构造 / 背包容量基础值）——按分层纪律（事件类只做派发，机制归 {@code data/helper/**}）迁出。
 *
 * <p>流程：按稀有度掷出词缀条数 → 按权重表抽选 → 把每条词缀的 Stat 条目并入 STAT_STORE
 * （词缀级 conditions 一并并入每条条目，运行时由 StatEngine 按作用域门控）→ 固化词缀声明的技能。
 */
public final class LivingModRoller {

    private LivingModRoller() {}

    /** 背包容量基础值修饰符 id（每 tick 覆写，先移除旧值再加新值）。 */
    public static final Identifier CARRY_BASE_MODIFIER =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "carry_base");

    /** 按当前基础容量覆写实体的 ITEM_CAPACITY 基础修饰符。 */
    public static void applyCarryCapacity(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(AttributeRegistry.ITEM_CAPACITY);
        if (attr == null) return;
        // 移除旧的基础修饰符（如果有）
        attr.removeModifier(CARRY_BASE_MODIFIER);

        int base = CapacityHelper.getBaseCapacity(entity);
        AttributeModifier mod = new AttributeModifier(
                CARRY_BASE_MODIFIER,
                base,
                AttributeModifier.Operation.ADD_VALUE
        );
        attr.addPermanentModifier(mod);
    }

    /** 按稀有度掷出本实体应持有的词缀条数。 */
    public static int getModNumber(RandomSource random, Rarity rarity) {
        Pair<Integer, Integer> pair = rarity.get();
        int min = pair.getFirst();
        int max = pair.getSecond();
        return max == min ? max : random.nextInt(max - min) + min;
    }

    /** 词缀权重表（纯 Stat 模型：不做生成期谓词门控，生效范围统一由 LivingMod.conditions 运行时门控）。 */
    public static WeightedList.Builder<LivingMod.Mod> modWeightedList(LivingEntity living) {
        WeightedList.Builder<LivingMod.Mod> mods = WeightedList.builder();
        LoadEvent.LIVING_MOD_MAP.forEach((name, mod) ->
                mods.add(mod, mod.mod().weight()));
        return mods;
    }

    /**
     * 抽选并固化 {@code count} 条词缀（原 {@code AttributeEvent#modGen}）。
     *
     * <p>本轮抽中的词缀：整批收集后统一固化技能（见 {@link #grantModSkills}）。
     *
     * @param mods 待抽权重表（调用方已排除实体当前持有项）
     */
    public static void rollMods(ServerLevel serverLevel, LivingEntity living, int count,
                                WeightedList.Builder<LivingMod.Mod> mods) {
        List<LivingMod.Mod> picked = new ArrayList<>();
        int i = count;
        while (i > 0 && !mods.getList().isEmpty()) {
            Optional<LivingMod.Mod> optionalMod = mods.build().getRandom(serverLevel.getRandom());
            if (optionalMod.isPresent()) {
                LivingMod.Mod mod = optionalMod.get();
                picked.add(mod);
                living.setData(AttachmentRegistry.ENTITY_MOD,
                        living.getData(AttachmentRegistry.ENTITY_MOD).add(mod.name()));
                // 纯 Stat 模型：把属性加成语义合并进 STAT_STORE，由 StatEngine.fold 聚合；
                // 词缀级 conditions 一并合并进每条 Stat 条目，运行时按作用域门控（与 LivingModifier 同构）。
                int mIdx = 0;
                for (StatEntry entry : mod.mod().stats())
                    StatStoreHelper.addStatEntry(living, mod.name() + "#" + (mIdx++),
                            withConditions(entry, mod.mod().conditions()));
                mods.remove(mod);
                i--;
            }
        }
        grantModSkills(living, serverLevel.getRandom(), picked);
    }

    /**
     * 把本轮抽中词缀声明的技能固化进实体的 SKILL_ASSEMBLY。
     *
     * <p><b>稳定性</b>：词缀抽取每 30 tick 重跑一次（词缀列表整体重抽），
     * 若每轮都按当前词缀重写技能，实体的技能集会每 1.5 秒抖动一次。
     * {@link LivingMod#grantSkills} 内部「装配表非空即跳过」，因此技能<b>只在实体首次
     * 抽到带技能词缀时生成一次</b>，此后无论词缀怎么重抽都不再变化；
     * 已装备技能的实体由 {@code MobSkillCastGoal} 持续施放。
     */
    private static void grantModSkills(LivingEntity living, RandomSource random, List<LivingMod.Mod> mods) {
        if (mods.isEmpty()) return;
        for (LivingMod.Mod mod : mods) {
            if (!mod.mod().skills().isEmpty()) {
                LivingMod.grantSkills(living, random, mods);
                return;
            }
        }
    }

    /**
     * 把 LivingModifier 级 conditions 合并进单条 Stat 条目：
     * 若条目本身无 conditions，则直接复用修饰器级 conditions；
     * 否则取两者并集（修饰器级条件为全局生效前提）。返回新 Stat 实例。
     */
    public static StatEntry withConditions(StatEntry entry, List<ValueExpr> modifierConditions) {
        if (modifierConditions.isEmpty()) return entry;
        List<ValueExpr> merged = new ArrayList<>(entry.conditions());
        merged.addAll(modifierConditions);
        return new StatRead(entry.amount(), merged, entry.targets(), entry.effect(),
                Map.of(), entry.spillTo(), entry.convertTo()).withCondition(entry.condition());
    }
}
