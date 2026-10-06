package com.cimdy.coe.data.codec.item.mod.state;

import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeChecker;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.helper.affix.ModHelper;
import com.cimdy.coe.data.codec.item.registry.ItemDataRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class StateInitializer {

    private StateInitializer() {}

    /** 默认权重（具体装备未声明权重时回落；权重入数据层后由 EquipmentRead 读取）。 */
    private static final int DEFAULT_WEIGHT = 1000;

    /**
     * 掉落初始化：遍历分类层按路径归类的 EquipmentRead，筛选与物品类型匹配且等级需求满足者，
     * 加权随机选取其一，将其展开结果（属性加成 / 需求 / 基底）写入 MODIFIABLE_STATE，并触发属性应用。
     * 传奇独有词缀不在此处处理——由传奇预设 UniqueDef 按 baseItem 路径读取具体基础装备后叠加。
     */
    public static void applyInitialization(EquipmentState state, RandomSource random, ItemStack stack, int level) {
        WeightedList.Builder<EquipmentRead> candidates = new WeightedList.Builder<>();
        ItemDataRegistry.getInstance().getClassification().getEquipmentReads().forEach((type, list) -> {
            if (!ItemTypeChecker.hasType(state, type)) return;
            list.stream()
                    .filter(read -> matchesLevel(read, level))
                    .forEach(read -> candidates.add(read, DEFAULT_WEIGHT));
        });
        candidates.build().getRandom(random)
                .ifPresent(read -> applyRead(state, stack, read, level));
    }

    private static boolean matchesLevel(EquipmentRead read, int level) {
        return read.requirements().getOrDefault("l", 0.0) <= level;
    }

    private static void applyRead(EquipmentState state, ItemStack stack, EquipmentRead read, int level) {
        // 词缀层重置（鉴定 = 首次展开基底），但品质与孔属于物品结构维度，不受鉴定影响，须原样保留
        stack.set(DataComponentRegistry.MODIFIABLE_STATE, new EquipmentState(
                level, state.rarity(), ModHelper.getItemType(stack), state.markers(),
                Optional.of(read), List.of(), state.quality(), state.sockets()));
        ModHelper.applyModValue(stack);
    }
}
