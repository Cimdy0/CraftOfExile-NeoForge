package com.cimdy.coe.data.codec.stronghold.task.helper.dispatch;

import com.cimdy.coe.entity.goal.stronghold.impl.work.misc.EnchantGoal;
import com.cimdy.coe.data.codec.stronghold.task.helper.recipe.CraftPlanDetail;
import com.cimdy.coe.data.codec.stronghold.task.helper.demand.DemandInfo;
import com.cimdy.coe.data.codec.stronghold.task.helper.demand.DemandSource;
import com.cimdy.coe.data.codec.stronghold.task.helper.result.DistributedResult;
import com.cimdy.coe.data.codec.stronghold.task.helper.result.MatchResultWithTask;
import com.cimdy.coe.data.codec.stronghold.task.helper.recipe.RecipeCache;
import com.cimdy.coe.data.codec.stronghold.task.helper.recipe.RecipeCacheManager;
import com.cimdy.coe.data.codec.stronghold.task.helper.supply.SupplyInfo;
import com.cimdy.coe.data.codec.stronghold.task.helper.supply.SupplySource;
import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.data.codec.stronghold.ItemCount;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockRange;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.CarryTask;
import com.cimdy.coe.data.codec.stronghold.task.impl.EatTask;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant.ConvertTask;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant.ConvertType;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant.EnchantTask;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant.EnchantType;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.*;

public class TaskGenerator {
    /**
     * 生成「注册表驱动」的合成/处理链任务，覆盖所有原版配方类型。
     * <p>
     * 遍历据点的各站点，通过 {@link com.cimdy.coe.data.codec.stronghold.task.site.Site#getRecipeType()}
     * 取出其对应的原版 {@link RecipeType}，把站点存储坐标注册为该配方类型的生产点，
     * 从而让工人系统覆盖 合成 / 冶炼 / 高炉 / 烟熏 / 营火 / 切石 / 锻造 等全部配方。
     * 递归规划由 {@link RecipeCacheManager#planAll} 完成（含中间产物合成与预算门控），
     * 执行器统一为 {@code CraftGoal}（按配方 id 取原版配方，与配方类型无关地执行）。
     */
    public static List<Task> generateCraftTasks(ServerLevel level, GlobalPos terminalPos, Stronghold hold,
                                                List<DemandInfo> allDemands, List<SupplyInfo> allSupplies) {
        // 用据点真实站点的存储坐标注册各配方类型的生产点，覆盖所有原版配方
        Map<BlockPos, RecipeType<?>> recipeTypes = new LinkedHashMap<>();
        boolean hasCraftSite = false;
        for (Site site : hold.holdState().sites()) {
            RecipeType<?> rt = site.getRecipeType();
            if (rt == null) continue;
            List<BlockPos> storages = site.getStorages();
            if (storages.isEmpty()) continue;
            recipeTypes.put(storages.get(0), rt);
            if (rt == RecipeType.CRAFTING) hasCraftSite = true;
        }
        if (!hasCraftSite) recipeTypes.put(new BlockPos(0, 0, 0), RecipeType.CRAFTING);

        List<CraftPlanDetail> plans = RecipeCacheManager.planAll(allDemands, allSupplies, recipeTypes, level);
        return plans.stream().map(p -> p.createTaskChainForPlan(level, terminalPos)).toList();
    }

    /**
     * 生成转化链（与合成同构的「需求/供应 → DAG 链」管线）。
     * <p>
     * 对每种转化类型（烹饪/冶炼/堆肥/研磨），在据点的对应站点上，把「产物需求」与「原料供应」配对，
     * 生成形如 {@code 搬运原料 → 站点转化 → 搬运产物} 的链：前驱 CarryTask 把原料从供应容器搬到站点存储，
     * ConvertTask 在站点把原料转为产物（进度持久化），后继 CarryTask 把产物搬到需求容器。
     * 单一产物的需求预算在同类所有站点间共享，避免多原料/多站点重复分配导致超额转化。
     */
    public static List<Task> generateConvertTasks(ServerLevel level, GlobalPos terminalPos, Stronghold hold,
                                                  List<DemandInfo> allDemands, List<SupplyInfo> allSupplies) {
        List<Task> chains = new ArrayList<>();

        // 注册表驱动的通用管线已覆盖「有原版配方」的产物（如染料/骨粉同时有合成配方），
        // 此处仅保留「无原版配方」的自定义变换，避免与通用管线重复产出。
        RecipeCache recipeCache = RecipeCacheManager.get(level);

        for (ConvertType ct : ConvertType.all()) {
            // 按产物聚合需求（同一产物可能被多个原料映射，需统一预算）
            Map<Item, Integer> demandByProduct = new HashMap<>();
            Map<Item, Integer> demandMinTransfer = new HashMap<>();
            Map<Item, BlockPos> demandContainer = new HashMap<>();
            for (DemandInfo d : allDemands) {
                if (ct.getConversionMap().values().stream().anyMatch(s -> s.getItem() == d.resource().getItem())) {
                    demandByProduct.merge(d.resource().getItem(), d.remaining(), Integer::sum);
                    demandMinTransfer.merge(d.resource().getItem(), d.minTransfer(), Integer::max);
                    demandContainer.putIfAbsent(d.resource().getItem(), d.containerPos());
                }
            }
            if (demandByProduct.isEmpty()) continue;

            Map<Item, Integer> supplyByRaw = new HashMap<>();
            Map<Item, Integer> supplyMinTransfer = new HashMap<>();
            Map<Item, BlockPos> supplyContainer = new HashMap<>();
            for (SupplyInfo s : allSupplies) {
                if (ct.getConversionMap().containsKey(s.resource().getItem())) {
                    supplyByRaw.merge(s.resource().getItem(), s.remaining(), Integer::sum);
                    supplyMinTransfer.merge(s.resource().getItem(), s.minTransfer(), Integer::max);
                    supplyContainer.putIfAbsent(s.resource().getItem(), s.containerPos());
                }
            }
            if (supplyByRaw.isEmpty()) continue;

            // 需求预算在同类所有站点间共享，防止超额转化
            Map<Item, Integer> remainingDemand = new HashMap<>(demandByProduct);

            for (Site site : hold.holdState().sites()) {
                if (!ct.matchesSite(site)) continue;
                List<BlockPos> storages = site.getStorages();
                if (storages.isEmpty()) continue;
                BlockPos ioStorage = storages.get(0);

                for (Map.Entry<Item, ItemStack> e : ct.getConversionMap().entrySet()) {
                    Item rawItem = e.getKey();
                    ItemStack productStack = e.getValue();
                    Item productItem = productStack.getItem();
                    // 该产品已有原版配方 → 交由通用管线处理，此处跳过以防重复产出
                    if (recipeCache.hasProductRecipe(productItem)) continue;
                    int supply = supplyByRaw.getOrDefault(rawItem, 0);
                    if (supply <= 0) continue;
                    int demand = remainingDemand.getOrDefault(productItem, 0);
                    if (demand <= 0) continue;
                    int amount = Math.min(demand, supply);
                    int minTransfer = Math.max(
                            demandMinTransfer.getOrDefault(productItem, 1),
                            supplyMinTransfer.getOrDefault(rawItem, 1));
                    if (amount < minTransfer) continue;

                    BlockPos supplyPos = supplyContainer.get(rawItem);
                    BlockPos demandPos = demandContainer.get(productItem);
                    if (supplyPos == null || demandPos == null) continue;

                    ItemResource rawRes = ItemResource.of(new ItemStack(rawItem));
                    ItemResource productRes = ItemResource.of(productStack);

                    // 头节点必须是 CarryTask（其 groupingKey 可被 mergePendingTasks 去重），否则每轮扫描重复生成
                    // 的 ConvertTask（无 groupingKey）会堆积导致超额转化。故即便原料已在站点存储（supply==io），
                    // 仍生成一次「原地搬运」作为去重锚点。
                    Task carryIn = new CarryTask(new ItemCount(rawRes, amount), supplyPos, ioStorage, terminalPos, terminalPos);
                    ConvertTask convert = new ConvertTask(ct, site.getSitePos(), rawRes, productRes, amount);
                    Task carryOut = null;
                    if (!demandPos.equals(ioStorage)) {
                        carryOut = new CarryTask(new ItemCount(productRes, amount), ioStorage, demandPos, terminalPos, terminalPos);
                    }

                    Task head = null, prev = null;
                    for (Task node : new Task[]{carryIn, convert, carryOut}) {
                        if (node == null) continue;
                        if (prev == null) head = node;
                        else prev.setNext(node);
                        prev = node;
                    }
                    if (head != null) chains.add(head);

                    // 扣减该产物需求预算，避免被其他原料再次分配而超额
                    remainingDemand.put(productItem, demand - amount);
                }
            }
        }
        return chains;
    }

    /**
     * 生成附魔应用链（与转化链同构的「需求/供应/末影能量 → 链」管线）。
     * <p>
     * 对每种附魔规格（{@code EnchantType.EnchantSpec}），在据点的附魔站点上，把「附魔产物需求」与
     * 「附魔书 / 目标物品 / 青金石 供应」配对，并校验据点末影能量充足，生成形如
     * {@code 搬运附魔书 → 搬运目标 → 搬运青金石 → 站点附魔 → 搬运产物} 的链。
     * 头节点为 CarryTask（去重锚点），中段为 EnchantTask，由 EnchantGoal 执行。
     */
    public static List<Task> generateEnchantTasks(ServerLevel level, GlobalPos terminalPos, Stronghold hold,
                                                  List<DemandInfo> allDemands, List<SupplyInfo> allSupplies) {
        List<Task> chains = new ArrayList<>();
        ItemResource lapisRes = ItemResource.of(new ItemStack(Items.LAPIS_LAZULI));

        for (EnchantType et : EnchantType.all()) {
            for (Site site : hold.holdState().sites()) {
                if (!et.matchesSite(site)) continue;
                List<BlockPos> storages = site.getStorages();
                if (storages.isEmpty()) continue;
                BlockPos ioStorage = storages.get(0);

                for (EnchantType.EnchantSpec spec : et.getSpecs()) {
                    ItemResource outRes = spec.outputRes(level);
                    // 是否存在对附魔产物的需求
                    DemandInfo demand = null;
                    for (DemandInfo d : allDemands) {
                        if (d.resource().equals(outRes)) { demand = d; break; }
                    }
                    if (demand == null) continue;

                    ItemResource bookRes = spec.inputBookRes(level);
                    ItemResource targetRes = spec.baseTargetRes();
                    int bookSupply = sumSupply(allSupplies, bookRes);
                    int targetSupply = sumSupply(allSupplies, targetRes);
                    int lapisSupply = sumSupply(allSupplies, lapisRes);
                    if (bookSupply <= 0 || targetSupply <= 0 || lapisSupply < spec.lapisCost()) continue;

                    int amount = Math.min(demand.remaining(), Math.min(bookSupply, targetSupply));
                    amount = Math.min(amount, lapisSupply / spec.lapisCost());
                    if (amount <= 0) continue;

                    // 末影能量门控：据点账本按终端坐标扣费，不足则不生成该链
                    if (!StrongholdHelper.hasEnderEnergy(level, terminalPos, (double) spec.enderEnergyCost() * amount)) continue;

                    BlockPos bookPos = supplyContainer(allSupplies, bookRes);
                    BlockPos targetPos = supplyContainer(allSupplies, targetRes);
                    BlockPos lapisPos = supplyContainer(allSupplies, lapisRes);
                    BlockPos demandPos = demand.containerPos();
                    if (bookPos == null || targetPos == null || lapisPos == null) continue;

                    Task carryBook = new CarryTask(new ItemCount(bookRes, amount), bookPos, ioStorage, terminalPos, terminalPos);
                    Task carryTarget = new CarryTask(new ItemCount(targetRes, amount), targetPos, ioStorage, terminalPos, terminalPos);
                    Task carryLapis = new CarryTask(new ItemCount(lapisRes, amount * spec.lapisCost()), lapisPos, ioStorage, terminalPos, terminalPos);
                    EnchantTask enchant = new EnchantTask(et, site.getSitePos(), bookRes, targetRes, lapisRes, outRes, spec.lapisCost(), spec.enderEnergyCost(), amount);
                    Task carryOut = null;
                    if (!demandPos.equals(ioStorage)) {
                        carryOut = new CarryTask(new ItemCount(outRes, amount), ioStorage, demandPos, terminalPos, terminalPos);
                    }

                    // 线性链：搬运书 → 搬运目标 → 搬运青金石 → 附魔 → 搬运产物
                    carryBook.setNext(carryTarget);
                    carryTarget.setNext(carryLapis);
                    carryLapis.setNext(enchant);
                    if (carryOut != null) enchant.setNext(carryOut);
                    chains.add(carryBook);
                }
            }
        }
        return chains;
    }

    private static int sumSupply(List<SupplyInfo> supplies, ItemResource res) {
        int sum = 0;
        for (SupplyInfo s : supplies) if (s.resource().equals(res)) sum += s.remaining();
        return sum;
    }

    private static BlockPos supplyContainer(List<SupplyInfo> supplies, ItemResource res) {
        for (SupplyInfo s : supplies) if (s.resource().equals(res)) return s.containerPos();
        return null;
    }

    public static Map<UUID, List<Task>> generateEatTasks(ServerLevel level, Stronghold hold, List<SupplyInfo> supplies) {
        Map<UUID, List<Task>> assignedTasks = new HashMap<>();

        List<UUID> entities = hold.entities();
        List<Mob> hungryMobs = new ArrayList<>();
        for (UUID id : entities) {
            if (level.getEntity(id) instanceof Mob mob
                    && !hold.holdState().getActiveTasks().containsKey(mob.getUUID())) {
                if (StrongholdHelper.getHunger(mob) <= 0) {
                    hungryMobs.add(mob);
                }
            }
        }
        if (hungryMobs.isEmpty()) return assignedTasks;

        for (int i = 0; i < supplies.size(); i++) {
            SupplyInfo supply = supplies.get(i);
            FoodProperties food = supply.resource().toStack().get(DataComponents.FOOD);
            if (food == null) continue;

            int remaining = supply.remaining();
            Iterator<Mob> it = hungryMobs.iterator();
            while (it.hasNext() && remaining > 0) {
                Mob mob = it.next();
                double currentHunger = StrongholdHelper.getHunger(mob);
                double nutrition = Math.max(1, food.nutrition());

                // 计算最多能吃几个，确保不超过最大饥饿值 40
                int maxEat = (int) Math.floor((40.0 - currentHunger) / nutrition);
                if (maxEat <= 0) {
                    it.remove();
                    continue;
                }
                int take = Math.min(maxEat, remaining);

                // 生成 EatTask 并标记为 ASSIGNED
                EatTask eatTask = new EatTask(new ItemCount(supply.resource(), take), supply.containerPos());
                eatTask.onStart(level);

                assignedTasks.computeIfAbsent(mob.getUUID(), k -> new ArrayList<>()).add(eatTask);

                remaining -= take;
                it.remove();
            }

            // 更新供应剩余量
            if (remaining != supply.remaining()) {
                supplies.set(i, supply.withRemaining(remaining));
            }
        }
        return assignedTasks;
    }

    /**
     * 本地匹配，返回带任务的 MatchResultWithTask（终端自身调用）。
     */
    public static MatchResultWithTask matchAll(
            List<DemandInfo> demands,
            List<SupplyInfo> supplies,
            GlobalPos commonTerminalPos) {

        List<DemandSource> demandSources = demands.stream()
                .map(d -> new DemandSource(d, commonTerminalPos)).toList();
        List<SupplySource> supplySources = supplies.stream()
                .map(s -> new SupplySource(s, commonTerminalPos)).toList();

        DistributedResult distributed = matchAllDistributed(demandSources, supplySources, StockRange.LOCAL);

        List<DemandInfo> promDemands = new ArrayList<>(
                distributed.newDemands().getOrDefault(commonTerminalPos, List.of()));
        List<SupplyInfo> promSupplies = new ArrayList<>(
                distributed.newSupplies().getOrDefault(commonTerminalPos, List.of()));
        List<CarryTask> tasks = distributed.newTasksByHold().getOrDefault(commonTerminalPos, List.of());

        return new MatchResultWithTask(new ArrayList<>(tasks), promDemands, promSupplies);
    }

    /**
     * 分布式匹配，根据 scope 消耗当前范围供需，剩余供需只保留更高级别的项。
     * @param scope 当前匹配的范围（LOCAL / DIMENSION / GLOBAL）
     */
    public static DistributedResult matchAllDistributed(
            List<DemandSource> demandSources,
            List<SupplySource> supplySources,
            StockRange scope) {

        Map<ItemResource, List<DemandSource>> groupedDemands = new HashMap<>();
        for (var ds : demandSources)
            groupedDemands.computeIfAbsent(ds.demand().resource(), k -> new ArrayList<>()).add(ds);

        Map<ItemResource, List<SupplySource>> groupedSupplies = new HashMap<>();
        for (var ss : supplySources)
            groupedSupplies.computeIfAbsent(ss.supply().resource(), k -> new ArrayList<>()).add(ss);

        Map<GlobalPos, List<CarryTask>> tasksMap = new HashMap<>();
        Map<GlobalPos, List<DemandInfo>> promDemandsMap = new HashMap<>();
        Map<GlobalPos, List<SupplyInfo>> promSuppliesMap = new HashMap<>();

        for (var entry : groupedDemands.entrySet()) {
            ItemResource resource = entry.getKey();
            List<DemandSource> demands = entry.getValue();
            List<SupplySource> supplies = groupedSupplies.getOrDefault(resource, List.of());

            demands.sort(Comparator.comparingInt((DemandSource d) -> d.demand().range().ordinal())
                    .thenComparingInt(d -> -d.demand().priority()));
            List<SupplySource> mutableSupplies = new ArrayList<>(supplies);
            mutableSupplies.sort(Comparator.comparingInt((SupplySource s) -> s.supply().range().ordinal())
                    .thenComparingInt(s -> -s.supply().priority()));

            Set<Set<ContainerKey>> usedPairs = new HashSet<>();

            for (DemandSource demandSrc : demands) {
                DemandInfo demand = demandSrc.demand();
                int remaining = demand.remaining();
                var suppIter = mutableSupplies.iterator();

                while (suppIter.hasNext() && remaining > 0) {
                    SupplySource supplySrc = suppIter.next();
                    SupplyInfo supply = supplySrc.supply();
                    if (supply.remaining() <= 0) continue;
                    if (!supply.canSupplyTo(demand.range())) continue;
                    if (demandSrc.terminalPos().equals(supplySrc.terminalPos()) &&
                            demand.containerPos().equals(supply.containerPos())) continue;

                    var fromKey = new ContainerKey(supplySrc.terminalPos(), supply.containerPos());
                    var toKey = new ContainerKey(demandSrc.terminalPos(), demand.containerPos());
                    var pair = Set.of(fromKey, toKey);
                    if (usedPairs.contains(pair)) continue;

                    int minTransfer = Math.max(demand.minTransfer(), supply.minTransfer());
                    int canMove = Math.min(remaining, supply.remaining());
                    if (canMove >= minTransfer) {
                        usedPairs.add(pair);

                        var carry = new CarryTask(
                                new ItemCount(resource, canMove),
                                supply.containerPos(),
                                demand.containerPos(),
                                supplySrc.terminalPos(),
                                demandSrc.terminalPos()
                        );
                        tasksMap.computeIfAbsent(demandSrc.terminalPos(), k -> new ArrayList<>()).add(carry);

                        remaining -= canMove;
                        suppIter.remove();
                        if (supply.remaining() - canMove > 0) {
                            mutableSupplies.add(new SupplySource(
                                    supply.withRemaining(supply.remaining() - canMove),
                                    supplySrc.terminalPos()));
                        }
                    }
                }

                // 剩余需求：只保留范围大于当前 scope 的项
                if (remaining > 0 && demand.range().ordinal() > scope.ordinal()) {
                    promDemandsMap.computeIfAbsent(demandSrc.terminalPos(), k -> new ArrayList<>())
                            .add(demand.withRemaining(remaining));
                }
            }

            // 剩余供应：只保留范围大于当前 scope 的项
            for (SupplySource supplySrc : mutableSupplies) {
                SupplyInfo supply = supplySrc.supply();
                if (supply.remaining() > 0 && supply.range().ordinal() > scope.ordinal()) {
                    promSuppliesMap.computeIfAbsent(supplySrc.terminalPos(), k -> new ArrayList<>()).add(supply);
                }
            }
        }

        // 无对应需求的供应也按范围过滤
        for (var entry : groupedSupplies.entrySet()) {
            if (groupedDemands.containsKey(entry.getKey())) continue;
            for (var ss : entry.getValue()) {
                SupplyInfo supply = ss.supply();
                if (supply.remaining() > 0 && supply.range().ordinal() > scope.ordinal()) {
                    promSuppliesMap.computeIfAbsent(ss.terminalPos(), k -> new ArrayList<>()).add(supply);
                }
            }
        }

        return new DistributedResult(tasksMap, promDemandsMap, promSuppliesMap);
    }

    private record ContainerKey(GlobalPos terminal, BlockPos pos) {}

}