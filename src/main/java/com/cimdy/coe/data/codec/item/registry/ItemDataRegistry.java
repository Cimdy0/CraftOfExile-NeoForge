package com.cimdy.coe.data.codec.item.registry;

import com.cimdy.coe.data.codec.item.ItemWeight;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentRead;
import com.cimdy.coe.data.codec.item.equipment.EquipmentModPreset;
import com.cimdy.coe.data.codec.vanilla.EnchantModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物品数据注册中心（解耦重构 v2.1+，去除冗余中间层）：旧 6 个 {@code Pair<String,String>} 平铺 Map
 * 收敛为三层类型化模型，键统一为 {@link net.minecraft.resources.Identifier}：
 * <ul>
 *   <li>词缀池（Layer A）：{@code modPools} + {@code presetModPools}；</li>
 *   <li>物品分类与装备预设（Layer B）：{@link ItemClassification}；</li>
 *   <li>掉落权重（Layer C）：{@link DropWeightTable}，未来掉落系统的输入层；</li>
 * </ul>
 * 附魔层保持原 {@code Map} 不变（已类型化）。
 *
 * <p>单一注册中心，<b>不再委托独立的 {@code ModPoolRepository} 包装类</b>（该冗余层已移除——
 * 它只是两个 Map 的二次封装，每个方法都纯转发，无独立语义）。三层数据直接持有于此，
 * 对外提供加载器与查询 getter；调用点（{@code LoadEvent}/{@code ModGenerator}/
 * {@code StateInitializer}/{@code ModifierPipeline}）无需感知中间层。
 */
public final class ItemDataRegistry {

    private final Map<Identifier, List<WeightedList.Builder<CommonMod>>> modPools = new HashMap<>();
    private final Map<Identifier, Map<EquipmentModPreset.Condition, List<WeightedList.Builder<CommonMod>>>> presetModPools = new HashMap<>();
    private final ItemClassification classification = new ItemClassification();
    private final DropWeightTable dropWeights = new DropWeightTable();
    private final Map<ResourceKey<Enchantment>, EnchantModifier.Builder> enchantModifiers = new HashMap<>();

    // ---- 加载（由 LoadEvent 的 reload 监听器调用） ----

    public void loadModPool(Identifier key, WeightedList.Builder<CommonMod> builder) {
        modPools.computeIfAbsent(key, k -> new ArrayList<>()).add(builder);
    }

    public void loadPresetModPool(Identifier key, EquipmentModPreset.Condition cond, WeightedList.Builder<CommonMod> builder) {
        presetModPools.computeIfAbsent(key, k -> new HashMap<>())
                .computeIfAbsent(cond, c -> new ArrayList<>()).add(builder);
    }

    public void loadTypeWeight(Identifier type, Map<ItemWeight.Type, Integer> map) {
        dropWeights.loadTypeWeight(type, map);
    }

    public void loadEquipmentRead(Identifier type, EquipmentRead read) {
        classification.loadEquipmentRead(type, read);
    }

    public void loadEnchantModifier(ResourceKey<Enchantment> key, EnchantModifier.Builder builder) {
        enchantModifiers.put(key, builder);
    }

    public void clear() {
        modPools.clear();
        presetModPools.clear();
        classification.clear();
        dropWeights.clear();
        enchantModifiers.clear();
    }

    // ---- 查询（Map 返回型，沿用旧调用点） ----

    public Map<Identifier, List<WeightedList.Builder<CommonMod>>> getModPools() {
        return modPools;
    }

    public Map<Identifier, Map<EquipmentModPreset.Condition, List<WeightedList.Builder<CommonMod>>>> getPresetModPools() {
        return presetModPools;
    }

    public Map<Identifier, Map<ItemWeight.Type, Integer>> getTypeWeights() {
        return dropWeights.getTypeWeights();
    }

    public Map<Identifier, List<EquipmentRead>> getEquipmentReads() {
        return classification.getEquipmentReads();
    }

    public Map<ResourceKey<Enchantment>, EnchantModifier.Builder> getEnchantModifiers() {
        return enchantModifiers;
    }

    // ---- 查询（类型化层对象，供未来直接消费层语义） ----

    public ItemClassification getClassification() {
        return classification;
    }

    public DropWeightTable getDropWeights() {
        return dropWeights;
    }

    // ---- 单例（测试中可替换） ----

    private static ItemDataRegistry instance = new ItemDataRegistry();

    public static ItemDataRegistry getInstance() {
        return instance;
    }

}
