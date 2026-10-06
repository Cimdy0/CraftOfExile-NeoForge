package com.cimdy.coe.data.helper.mod;

import com.cimdy.coe.data.helper.affix.ModHelper;
import com.cimdy.coe.CoeConfig;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.helper.mod.ModGenerator;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import net.minecraft.resources.Identifier;

/**
 * 词缀数据操控（单独文件夹）：通货操作实际改写物品词缀状态的唯一落点。
 * 原先分散在 {@code ModHelper} 的 apply 与 {@code CurrencyApplier} 的 switch 中的逻辑在此集中，
 * 由 {@link com.cimdy.coe.data.codec.item.currency.OpType} 各常量按数据驱动方式调用。
 *
 * <p>载体无关：词缀属通用维度，装备 / 药剂 / 护符统一经
 * {@link com.cimdy.coe.data.helper.item.ModifiableStateOps} 读写，本类不再只服务 {@link EquipmentState}。</p>
 *
 * <p>不变式：本类绝不手算属性，只委托 {@link ModGenerator} 改写状态组件，随后由现有 Stat 层重算实体属性。</p>
 */
public final class ModOps {

    private ModOps() {
    }

    public static void regenRandomDown(RandomSource random, ItemStack target) {
        ModGenerator.applyRegenRandomDown(random, target);
    }

    public static void regenRandomDown(RandomSource random, ItemStack target, int minTier) {
        ModGenerator.applyRegenRandomDown(random, target, minTier);
    }

    public static void regenRandomDownWithRarity(RandomSource random, ItemStack target, Rarity rarity) {
        ModGenerator.applyRegenRandomDownWithRarity(random, target, rarity);
    }

    /** 升阶（含词缀等级下限）：动作与 3 参版本一致，仅新增词缀受 {@code minTier} 约束（POE2 分阶通货）。 */
    public static void regenRandomDownWithRarity(RandomSource random, ItemStack target, Rarity rarity, int minTier) {
        ModGenerator.applyRegenRandomDownWithRarity(random, target, rarity, minTier);
    }

    /**
     * 替换一条词缀：先移除随机一条，再新增一条，且仅「新增」那条受 {@code minTier} 下限约束。
     * POE2 混沌石「改一条」与高阶改造石「增一减一」共用此原子操作。
     */
    public static void replaceRandomDownMod(RandomSource random, Level level, ItemStack target, int minTier) {
        ModGenerator.applyReplaceRandomDownMod(random, level, target, minTier);
    }

    public static boolean addRandomDownMod(RandomSource random, ItemStack target) {
        return ModGenerator.applyAddRandomDownMod(random, target);
    }

    /**
     * 该物品在给定位置是否还有可 roll 的候选词缀（含 {@code minTier} 下限过滤）。
     * 用于「新增词缀」类通货的守卫：若物品的 item class 尚无词缀池（或候选全被下限剔除），
     * 消费一枚通货却什么都不发生——必须在消耗前拦住。
     */
    public static boolean hasAvailableMod(ItemStack target, List<Location> locations, int minTier) {
        return !ModPoolBuilder.getModBuilder(target, locations, minTier).isEmpty();
    }

    public static boolean addRandomDownMod(RandomSource random, ItemStack target, int minTier) {
        return ModGenerator.applyAddRandomDownMod(random, target, minTier);
    }

    public static void randomModValue(RandomSource random, ItemStack target) {
        ModGenerator.applyRandomModValue(random, target);
    }

    public static void removeRandomDownMod(Level level, ItemStack target) {
        ModGenerator.applyRemoveRandomDownMod(level, target);
    }

    /** 洗白（Scouring Orb）：清除词缀并清除工作台工艺排除标签，使其不再影响后续 roll。 */
    public static void resetItemToNormal(ItemStack target) {
        ModGenerator.resetItemToNormal(target);
        target.remove(DataComponentRegistry.ITEM_CRAFT_TAGS);
    }

    public static void applyInitialization(RandomSource random, ItemStack target, int level) {
        ModGenerator.applyInitialization(random, target, level);
    }

    /** 卡兰德拉之镜（MIRROR）：克隆目标物品，标记副本为镜像（受 blockMirror 守卫拦截），副本送入玩家背包。 */
    public static void applyMirror(Player player, ItemStack target) {
        ItemStack copy = target.copy();
        copy.set(DataComponentRegistry.MODIFIABLE_STATE,
                ModifiableStateOps.equipment(copy).withMirror(true));
        if (!player.getInventory().add(copy)) {
            player.drop(copy, false);
        }
    }

    /** 机会石（CHANCE_UPGRADE）：赌博升阶，随机目标稀有度（默认 RARE）。 */
    public static void chanceUpgrade(RandomSource random, ItemStack target) {
        Rarity r = CoeConfig.itemRarityWeightsNotCommon.getRandom(random).orElse(Rarity.RARE);
        ModGenerator.applyRegenRandomDownWithRarity(random, target, r);
    }

    public static boolean isDownNotMaxed(ItemStack target) {
        return ModGenerator.isDownNotMaxed(target);
    }

    public static boolean hasMods(ItemStack target) {
        return ModGenerator.hasMods(target);
    }

    public static List<Identifier> getItemType(ItemStack stack) {
        return ModGenerator.getItemType(stack);
    }

    public static void setType(ItemStack stack, List<Identifier> types) {
        ModGenerator.setType(stack, types);
    }
}
