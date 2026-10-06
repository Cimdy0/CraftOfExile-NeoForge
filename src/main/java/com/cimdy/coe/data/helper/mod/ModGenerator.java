package com.cimdy.coe.data.helper.mod;

import com.cimdy.coe.CoeConfig;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.item.ItemWeight;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.cimdy.coe.data.codec.item.mod.core.ModLayer;
import com.cimdy.coe.data.codec.item.mod.core.impl.AffixMod;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeChecker;
import com.cimdy.coe.data.codec.item.mod.value.ModNumber;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.item.registry.ItemDataRegistry;
import com.cimdy.coe.data.codec.support.stat.effect.marker.RequirementStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import com.cimdy.coe.data.helper.affix.ModHelper;
import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeHierarchy;
import com.cimdy.coe.data.codec.item.mod.core.impl.RangedMod;
import com.cimdy.coe.CraftOfExile;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentRead;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class ModGenerator {

    private ModGenerator() {}

    public static boolean currentEquipment(LivingEntity living, ItemStack stack, @Nullable String type) {
        EquipmentState state =ModifiableStateOps.equipment(stack);
        boolean currentType = true;
        if (living instanceof Player player && !player.isCreative()) {
            // 「装备与技能石属性需求 ±X%」(node 35688/38138/45278)：全局折叠的属性需求倍率，默认 0（无修正），
            // -0.04 → reqMul 0.96 → 全部需求降 4%；下界钳 0 避免出现负需求。
            double reqMul = Math.max(0.0, 1.0 + living.getAttributeValue(AttributeRegistry.ATTRIBUTE_REQUIREMENTS));
            // 「武术武器属性需求 ×3」(node 32349)：持此 INERT 标记且被检物品为武术武器（剑/斧/锤）时，需求倍率再 ×3。
            double martialMul = 1.0;
            for (StatEntry e : living.getData(AttachmentRegistry.STAT_STORE).entries()) {
                if (e.effect() instanceof RequirementStatEffect r && r.martialMultiplier() != 1.0) {
                    martialMul *= r.martialMultiplier();
                }
            }
            if (martialMul != 1.0
                    && (ItemTypeChecker.isSword(state) || ItemTypeChecker.isAxe(state) || ItemTypeChecker.isMace(state))) {
                reqMul *= martialMul;
            }
            currentType = state.getRequireLevel() * reqMul <= living.getData(AttachmentRegistry.LEVEL)
                    && state.getRequireDexterity() * reqMul <= living.getAttributeValue(AttributeRegistry.DEXTERITY)
                    && state.getRequireStrength() * reqMul <= living.getAttributeValue(AttributeRegistry.STRENGTH)
                    && state.getRequireIntelligence() * reqMul <= living.getAttributeValue(AttributeRegistry.INTELLIGENCE);
        }
        // 类型判定一律走族别名展开（sword/axe/mace 横跨单手 / 双手，crossbow ↔ cross_bow）
        return currentType && (type == null || ItemTypeChecker.hasType(state, type));
    }

    /** 写入类型链（装备 / 药剂 / 护符共用维度：类型链由状态本身承载，按组件分派回写）。 */
    public static void setType(ItemStack stack, List<Identifier> types) {
        ModifiableStateOps.get(stack).ifPresent(state -> ModifiableStateOps.set(stack, state.withType(types)));
    }

    /**
     * 物品类型键：仅返回<b>叶类型键</b>（如 {@code one_hand_sword} / {@code chest} / {@code charm}）。
     * 祖先类别（{@code one_hand} / {@code main_hand} / {@code weapon} / {@code equipment} …）不再冗余写入状态，
     * 所有「祖先判定 / 父辖子」都在查询时由 {@link ItemTypeHierarchy} 派生。
     */
    public static List<Identifier> getItemType(ItemStack stack) {
        List<Identifier> types = new ArrayList<>();
        Map<Identifier, Map<ItemWeight.Type, Integer>> typeMap = new HashMap<>(ItemDataRegistry.getInstance().getDropWeights().getTypeWeights());
        getItemTypeRecursive(stack, null, types, typeMap);
        return types.isEmpty() ? List.of() : List.of(types.get(0));
    }

    private static void getItemTypeRecursive(ItemStack stack, @Nullable Identifier currentType, List<Identifier> types,
                                             Map<Identifier, Map<ItemWeight.Type, Integer>> typeMap) {
        Identifier matchedType = null;
        for (Map.Entry<Identifier, Map<ItemWeight.Type, Integer>> entry : typeMap.entrySet()) {
            Identifier typeKey = entry.getKey();
            boolean matches = entry.getValue().keySet().stream()
                    .anyMatch(type -> currentType == null ?
                            type.matches(stack) :
                            type.matches(currentType));
            if (matches) {
                types.add(typeKey);
                matchedType = typeKey;
                if (typeKey.getPath().equals("item")) return;
                break;
            }
        }
        if (matchedType != null) {
            typeMap.remove(matchedType);
            getItemTypeRecursive(stack, matchedType, types, typeMap);
        }
    }

    // 获取第一个匹配的物品类型
    public static Identifier getFirstItemType(ItemStack stack) {
        for (Map.Entry<Identifier, Map<ItemWeight.Type, Integer>> entry : ItemDataRegistry.getInstance().getDropWeights().getTypeWeights().entrySet()) {
            Identifier itemKey = entry.getKey();
            boolean matches = entry.getValue().keySet().stream()
                    .anyMatch(type -> type.matches(stack));
            if (matches) {
                return itemKey;
            }
        }
        return null;
    }

    public static boolean isDownNotMaxed(ItemStack stack) {
        ModNumber.Number matchedNumber = findMatchedNumber(stack);
        if (matchedNumber == null) {
            return false;
        }
        return isDownTypesNotMaxed(AttributeApplicator.locationBuckets(stack), matchedNumber);
    }

    private static boolean isDownTypesNotMaxed(
            Map<Location, List<ModifierEntry>> allMods,
            ModNumber.Number matchedNumber
    ) {
        return Stream.of(Location.PREFIX, Location.SUFFIX).anyMatch(type ->
                isLocationNotMaxed(type, allMods, matchedNumber));
    }

    private static boolean isLocationNotMaxed(
            Location type,
            Map<Location, List<ModifierEntry>> allMods,
            ModNumber.Number matchedNumber
    ) {
        return allMods.getOrDefault(type, new ArrayList<>()).size()
                < matchedNumber.getLocationNumber(type);
    }

    public static List<Location> getNotMaxedLocations(ModNumber.Number matchedNumber, List<Location> types, ItemStack stack) {
        // 无匹配档位（如普通稀有度的物品本就不应有词缀）时没有任何可用位置，直接空池。
        if (matchedNumber == null) {
            return List.of();
        }
        Map<Location, List<ModifierEntry>> allMods =
                AttributeApplicator.locationBuckets(stack);
        return types.stream().filter(type ->
                isLocationNotMaxed(type, allMods, matchedNumber)).toList();
    }

    /**
     * 按栈的物等 / 稀有度查可用词缀数量档位（{@code ModNumber.Number}）。
     * 二者皆属 {@link ModifiableState} 共享维度，故装备 / 药剂 / 护符统一经通用载体读取。
     */
    public static ModNumber.Number findMatchedNumber(ItemStack itemStack) {
        return findMatchedNumber(ModifiableStateOps.level(itemStack), ModifiableStateOps.rarity(itemStack));
    }

    private static ModNumber.Number findMatchedNumber(int level, com.cimdy.coe.data.codec.support.rarity.Rarity rarity) {
        return configModNumbers().stream()
                .filter(n -> n.matches(level, rarity))
                .findFirst()
                .orElse(null);
    }

    /** Item affix-count tiers, sourced from {@link CoeConfig} (replaces the hardcoded ModNumber default). */
    private static List<ModNumber.Number> configModNumbers() {
        return List.of(
                ModNumber.Number.from(Rarity.MAGIC,
                        Location.PREFIX, CoeConfig.itemMagicModPrefixMax,
                        Location.SUFFIX, CoeConfig.itemMagicModSuffixMax,
                        CoeConfig.itemMagicModMin),
                ModNumber.Number.from(Rarity.RARE,
                        Location.PREFIX, CoeConfig.itemRareModPrefixMax,
                        Location.SUFFIX, CoeConfig.itemRareModSuffixMax,
                        CoeConfig.itemRareModMin));
    }

    private static ModNumber.Number findMatchedNumber(int level, com.cimdy.coe.data.codec.support.rarity.Rarity rarity,
                                                      List<ModNumber.Number> numberList) {
        return numberList.stream()
                .filter(n -> n.matches(level, rarity))
                .findFirst()
                .orElse(null);
    }

    private static Location getRandomLocation(
            RandomSource random, Map<Location, WeightedList.Builder<CommonMod>> builders) {
        List<Location> availableTypes = builders.entrySet().stream()
                .filter(entry -> !entry.getValue().build().isEmpty())
                .map(Map.Entry::getKey)
                .toList();
        return availableTypes.get(random.nextInt(availableTypes.size()));
    }

    private static ModLayer getRandomLocationWithMatch(
            RandomSource random,
            Map<ModLayer, List<ModifierEntry>> allMods,
            List<Location> locations
    ) {
        List<ModLayer> matchedLocations = allMods.keySet().stream()
                .filter(location -> locations.contains(location) && !allMods.get(location).isEmpty()).toList();
        return matchedLocations.get(random.nextInt(matchedLocations.size()));
    }

    /** 前后缀词缀总条数（不含 ENCHANT 层）。用于「移除一条随机词缀」前的空池守卫。 */
    public static int getDownModCount(ItemStack stack) {
        return getLocationsHasModNumber(stack, List.of(Location.PREFIX, Location.SUFFIX));
    }

    private static int getLocationsHasModNumber(ItemStack stack, List<Location> locations) {
        Map<Location, List<ModifierEntry>> allMods = AttributeApplicator.locationBuckets(stack);
        return locations.stream().mapToInt(location -> allMods.getOrDefault(location, new ArrayList<>()).size()).sum();
    }

    private static void initializeModifiableState(
            RandomSource random, ItemStack stack, int level, com.cimdy.coe.data.codec.support.rarity.Rarity rarity) {
        // 类型链：装备 / 药剂 / 护符共用 ItemWeight 类型树解析；类别判定（槽位 / 机制门控）据此统一裁决。
        // 初始化逻辑多态分派到各状态载体（装备：智慧标记 + 需求计算；其余：仅类型链），消除 instanceof 分支。
        ModifiableStateOps.get(stack).ifPresent(current ->
                ModifiableStateOps.set(stack, current.initialize(random, stack, level, rarity, getItemType(stack))));
    }

    public static void applyForceInitializationWithRarity(RandomSource random, ItemStack stack, int level, com.cimdy.coe.data.codec.support.rarity.Rarity rarity) {
        initializeModifiableState(random, stack, level, rarity);
        applyRandomModValue(random, stack);
    }

    /**
     * 知识卷轴：全局随机生成一件装备。流程（数据驱动，仅写入基底属性，不附词缀）：
     * <ol>
     *   <li>枚举 ItemWeight 类型树，筛选「既有叶物品、又有满足物等门槛的装备读取」的类型；</li>
     *   <li>随机选一个通过类型，按权重挑一个叶物品；</li>
     *   <li>解析该物品真实叶类型，挑对应装备读取作基底属性写入状态（物等 = 玩家等级）。</li>
     * </ol>
     * 与 {@link #applyInitialization} 的区别：后者对「已有物品」在自身类型内重铸并附词缀；
     * 本方法从零造一件全新装备，且去掉属性修饰符（词缀）层。
     */
    public static ItemStack generateRandomEquipment(RandomSource random, int level) {
        ItemDataRegistry registry = ItemDataRegistry.getInstance();
        Map<Identifier, List<EquipmentRead>> reads = registry.getClassification().getEquipmentReads();
        Map<Identifier, Map<ItemWeight.Type, Integer>> typeWeights = registry.getDropWeights().getTypeWeights();

        List<Identifier> candidates = new ArrayList<>();
        for (Map.Entry<Identifier, Map<ItemWeight.Type, Integer>> e : typeWeights.entrySet()) {
            Identifier type = e.getKey();
            boolean hasItem = e.getValue().entrySet().stream().anyMatch(en -> en.getKey().item().isPresent());
            if (!hasItem) continue;
            List<EquipmentRead> typeReads = reads.get(type);
            if (typeReads == null) continue;
            boolean passing = typeReads.stream()
                    .anyMatch(r -> r.requirements().getOrDefault("l", 0.0) <= level);
            if (passing) candidates.add(type);
        }
        if (candidates.isEmpty()) return ItemStack.EMPTY;

        Identifier type = candidates.get(random.nextInt(candidates.size()));

        WeightedList.Builder<Item> itemBuilder = new WeightedList.Builder<>();
        for (Map.Entry<ItemWeight.Type, Integer> w : typeWeights.get(type).entrySet()) {
            w.getKey().item().ifPresent(h -> itemBuilder.add(h.value(), w.getValue()));
        }
        Item item = itemBuilder.build().getRandom(random).orElse(null);
        if (item == null) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(item);

        Identifier itemType = getFirstItemType(stack);
        List<EquipmentRead> primary = reads.get(itemType);
        List<EquipmentRead> fallback = reads.get(type);
        WeightedList.Builder<EquipmentRead> rb = new WeightedList.Builder<>();
        if (primary != null) {
            for (EquipmentRead r : primary) {
                if (r.requirements().getOrDefault("l", 0.0) <= level) rb.add(r, 1000);
            }
        }
        WeightedList<EquipmentRead> built = rb.build();
        if (built.isEmpty() && fallback != null) {
            WeightedList.Builder<EquipmentRead> rb2 = new WeightedList.Builder<>();
            for (EquipmentRead r : fallback) {
                if (r.requirements().getOrDefault("l", 0.0) <= level) rb2.add(r, 1000);
            }
            built = rb2.build();
        }
        Identifier finalType = itemType == null ? type : itemType;
        built.getRandom(random).ifPresent(read -> {
            List<Identifier> types = List.of(finalType);
            EquipmentState state = new EquipmentState(level, Rarity.COMMON, types, Map.of(),
                    Optional.of(read), List.of(), 0, List.of());
            ModifiableStateOps.set(stack, state);
            ModHelper.applyModValue(stack);
        });
        return stack;
    }

    public static void applyInitialization(RandomSource random, ItemStack stack, int level) {
        com.cimdy.coe.data.codec.support.rarity.Rarity rarity = CoeConfig.itemRarityWeights.getRandom(random).orElse(com.cimdy.coe.data.codec.support.rarity.Rarity.COMMON);
        // 词缀生成以物品的物等（iLevel）为准，而非固定 100；创造物品栏/物品注册表的 100 仅作为物品初始等级默认值。
        int itemLevel = ModifiableStateOps.level(stack);
        if (itemLevel <= 0) {
            itemLevel = level;
        }
        initializeModifiableState(random, stack, itemLevel, rarity);
        applyRegenRandomDown(random, stack);
    }

    public static void applyRegenRandomDownWithRarity(RandomSource random, ItemStack stack, com.cimdy.coe.data.codec.support.rarity.Rarity rarity) {
        applyRegenRandomDownWithRarity(random, stack, rarity, 0);
    }

    /**
     * 升阶（含词缀等级下限）：与 3 参版本动作完全一致，仅新增词缀受 {@code minTier} 下限约束。
     * 用于 POE2 分阶通货（Greater / Perfect）——「每一阶动作相同，唯一区别是新词缀的等级下限」。
     */
    public static void applyRegenRandomDownWithRarity(RandomSource random, ItemStack stack,
                                                      com.cimdy.coe.data.codec.support.rarity.Rarity rarity, int minTier) {
        // 稀有度为共享维度：装备 / 药剂 / 护符统一经通用载体写入（无状态载体不动作）。
        ModifiableStateOps.setRarity(stack, rarity);
        applyRegenRandomDown(random, stack, minTier);
    }

    /**
     * 替换一条词缀（先移除随机一条，再新增一条，新增那条受 {@code minTier} 下限约束）。
     * POE2 混沌石「改一条」与高阶改造石「增一减一」共用此原子操作；移除侧不受下限影响。
     */
    public static void applyReplaceRandomDownMod(RandomSource random, Level level, ItemStack stack, int minTier) {
        applyRemoveRandomDownMod(level, stack);
        applyAddRandomDownMod(random, stack, minTier);
    }

    public static void applyRegenRandomDown(RandomSource random, ItemStack stack) {
        applyRegenRandomDown(random, stack, 0);
    }

    public static void applyRegenRandomDown(RandomSource random, ItemStack stack, int minTier) {
        // 重掷只重铸前后缀词缀（通用载体的 mods 维度）；原生附魔（ENCHANTMENTS）作为独立 ENCHANT 层存活，
        // 不被清空（D9：附魔同源、重掷应保留；磨石全清在 EnchantEvent.GrindstoneEnchantEvent 处理）。
        ModifiableStateOps.setMods(stack, List.of());
        applyFillRandomDown(random, stack, minTier);
    }

    /**
     * 空槽随机填补：<b>保留</b>既有前后缀词缀，按当前物等 / 稀有度档位把每个位置补到随机条数
     * （0 ~ 该位置剩余上限，即「可能一条不补也可能补满」），再补齐档位要求的最小总数。
     *
     * <p>POE2 精髓升阶（Magic → Rare）即此步：精髓词为必定项先落槽，其余空槽由本方法按可用池随机填补。</p>
     */
    public static void applyFillRandomDown(RandomSource random, ItemStack stack, int minTier) {
        List<Location> locations = List.of(Location.PREFIX, Location.SUFFIX);
        List<ModNumber.Number> numberList = configModNumbers();
        ModNumber.Number matchedNumber = findMatchedNumber(ModifiableStateOps.level(stack), ModifiableStateOps.rarity(stack), numberList);
        if (matchedNumber != null) {
            int minTotal = matchedNumber.min();
            gen:
            for (Location location : locations) {
                int maxCount = matchedNumber.getLocationNumber(location);
                int hasCount = getLocationsHasModNumber(stack, List.of(location));
                int remaining = Math.max(0, maxCount - hasCount);
                int modN = random.nextInt(remaining + 1);
                for (int i = 0; i < modN; i++) {
                    if (!addRandomLocationMod(random, stack, List.of(location), minTier)) {
                        break gen;
                    }
                }
            }
            while (minTotal > getLocationsHasModNumber(stack, locations)) {
                if (!addRandomLocationMod(random, stack, locations, minTier)) {
                    break;
                }
            }
        }
        AttributeApplicator.applyModValue(stack);
    }

    public static boolean applyAddRandomDownMod(RandomSource random, ItemStack stack) {
        return applyAddRandomDownMod(random, stack, 0);
    }

    public static boolean applyAddRandomDownMod(RandomSource random, ItemStack stack, int minTier) {
        boolean success = addRandomLocationMod(random, stack, List.of(Location.PREFIX, Location.SUFFIX), minTier);
        AttributeApplicator.applyModValue(stack);
        return success;
    }

    public static void applyRemoveRandomDownMod(Level level, ItemStack stack) {
        List<Location> types = List.of(Location.PREFIX, Location.SUFFIX);
        removeRandomLocationMod(level, stack, types);
        AttributeApplicator.applyModValue(stack);
    }

    private static boolean addRandomLocationMod(RandomSource random, ItemStack stack, List<Location> locations) {
        return addRandomLocationMod(random, stack, locations, 0);
    }

    private static boolean addRandomLocationMod(RandomSource random, ItemStack stack, List<Location> locations, int minTier) {
        List<ModifierEntry> mods = new ArrayList<>(ModifiableStateOps.mods(stack));
        Map<Location, WeightedList.Builder<CommonMod>> builders = ModPoolBuilder.getModBuilder(stack, locations, minTier);
        if (!builders.isEmpty()) {
            Location randomType = getRandomLocation(random, builders);
            WeightedList.Builder<CommonMod> builder = builders.get(randomType);
            // 冲突剔除：同位置内「既有下缀与候选的冲突目标表集合相交」则剔除（共享任一生效表 = 同族互斥）。
            // 冲突由 AdditionType 身份派生、与附加来源无关 ——
            // 普通词缀（候选池抽取）与精髓词缀（强制作用）共享同一冲突判定（用户定调：所有下缀都存在冲突，
            // 不论如何附加）。故既排除同组的既有普通词缀，也排除同组的既有精髓词缀；符文无前后缀位置不参与。
            // 载体：供冲突表按部位谓词筛出「本部位生效」的表（无状态则跳过部位判定）。
            ModifiableState carrier = ModifiableStateOps.get(stack).orElse(null);
            builder.removeIf(m -> mods.stream()
                    .filter(e -> e instanceof AffixMod)
                    .map(e -> (AffixMod) e)
                    .anyMatch(a -> AffixMod.conflicts(m.value(), a, carrier)));
            builder.build().getRandom(random).ifPresent(mod ->
                    mods.add(mod.random(random).withLocation(randomType)));
        }
        ModifiableStateOps.setMods(stack, mods);
        return !builders.isEmpty();
    }

    private static void removeRandomLocationMod(Level level, ItemStack stack, List<Location> locations) {
        Map<ModLayer, List<ModifierEntry>> allMods = AttributeApplicator.combineModsAndEnchantments(stack);
        ModLayer type = getRandomLocationWithMatch(level.getRandom(), allMods, locations);
        List<ModifierEntry> mods = new ArrayList<>(allMods.get(type));
        mods.remove(level.getRandom().nextInt(mods.size()));
        allMods.put(type, mods);
        AttributeApplicator.setAllMods(level, stack, allMods);
    }

    public static void resetItemToNormal(ItemStack stack) {
        // 装备：reset 回 COMMON 并清空词缀（原先「先清词缀、后用旧副本 reset」会把词缀写回，此处修正为单次写入）。
        ModifiableStateOps.get(stack).ifPresentOrElse(
                s -> {
                    if (s instanceof EquipmentState equipment) {
                        stack.set(DataComponentRegistry.MODIFIABLE_STATE, equipment.reset().withMods(List.of()));
                    }
                },
                () -> ModifiableStateOps.setMods(stack, List.of()));
        AttributeApplicator.applyModValue(stack);
    }

    /** 有词缀或有附魔即算「非空」：两者是并列的两类，任一类存在都不能当空物品处理。 */
    public static boolean hasMods(ItemStack stack) {
        return !AttributeApplicator.combineModsAndEnchantments(stack).isEmpty();
    }

    private static List<ModifierEntry> randomModValue(
            RandomSource random, ItemStack stack) {
        // 范围态臂（基底 / 普通词缀 / 精髓）重掷数值；非范围态（符文，取中值）保持不变。
        return ModifiableStateOps.mods(stack).stream()
                .map(mod -> mod instanceof RangedMod ranged ? ranged.roll(random) : mod)
                .toList();
    }

    public static void applyRandomModValue(RandomSource random, ItemStack stack) {
        ModifiableStateOps.setMods(stack, randomModValue(random, stack));
        AttributeApplicator.applyModValue(stack);
    }
}
