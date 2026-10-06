package com.cimdy.coe.data.codec.item.mod.identity;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.ItemWeight;
import com.cimdy.coe.data.codec.item.registry.ItemDataRegistry;
import com.cimdy.coe.data.codec.support.meta.TagHierarchy;
import net.minecraft.resources.Identifier;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 物品类型层级（父类别辖制子类别），与 {@code GateTag} 的标签层级同构，统一收口到 {@link TagHierarchy} 接口。
 *
 * <p>类型树由 {@code ItemWeight} 声明（见 {@code CoeDataBootstrap.registerItemType}），运行时经 {@code LoadEvent}
 * 加载进 {@code ItemDataRegistry} 的 typeWeights（父裸键 -> 子 {@link ItemWeight.Type} 集合）。本类据此推导：
 * <ul>
 *   <li>子 -> 祖先 传递闭包（{@link #ancestorsOf}）：查询时由叶类型派生其全部上位类别；</li>
 *   <li>父 -> 子 传递闭包（{@link #childrenOf}）：表达式匹配时父类别可命中任一子类别；</li>
 * </ul>
 * 使得 {@code EquipmentState.types()} 只需存储<b>叶类型键</b>（不再冗余存储整条类型链），
 * 所有「祖先判定 / 父辖子」均在查询时由本层级表派生。</p>
 *
 * <p>{@link #rebuild()} 在 {@code LoadEvent.loadItemMod} 每次加载完 typeWeights 后调用，整体替换快照（volatile 发布，读侧无锁）。</p>
 */
public final class ItemTypeHierarchy {

    private ItemTypeHierarchy() {}

    private static volatile Map<Identifier, Set<Identifier>> ancestors = Map.of();
    private static volatile Map<Identifier, Set<Identifier>> descendants = Map.of();

    private static final TagHierarchy RESOLVER = new TagHierarchy() {
        @Override
        public Set<String> childrenOf(String tag) {
            Identifier id = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, tag);
            return descendants.getOrDefault(id, Set.of()).stream()
                    .map(Identifier::getPath)
                    .collect(Collectors.toSet());
        }

        @Override
        public boolean supportsProjectionWildcard() {
            return false;
        }
    };

    /** 供 {@link GateExpr} 求值装备类型键作用域的解析器（不启用 proj: 通配）。 */
    public static TagHierarchy resolver() {
        return RESOLVER;
    }

    /** 由当前 ItemDataRegistry 的 typeWeights 重建层级表（整体替换，读侧无锁）。 */
    public static void rebuild() {
        Map<Identifier, Map<ItemWeight.Type, Integer>> typeWeights =
                ItemDataRegistry.getInstance().getDropWeights().getTypeWeights();
        Map<Identifier, Set<Identifier>> directChildren = new HashMap<>();
        for (Map.Entry<Identifier, Map<ItemWeight.Type, Integer>> e : typeWeights.entrySet()) {
            for (ItemWeight.Type t : e.getValue().keySet()) {
                if (t.type().isPresent()) {
                    directChildren.computeIfAbsent(e.getKey(), k -> new LinkedHashSet<>())
                            .add(t.type().get());
                }
            }
        }
        // 子孙闭包：沿 directChildren 自顶向下 BFS 展开（节点 -> 其全部子孙）
        Map<Identifier, Set<Identifier>> anc = new HashMap<>();
        for (Identifier node : directChildren.keySet()) {
            Set<Identifier> acc = new LinkedHashSet<>();
            Deque<Identifier> queue = new ArrayDeque<>(directChildren.getOrDefault(node, Set.of()));
            while (!queue.isEmpty()) {
                Identifier cur = queue.poll();
                if (!acc.add(cur)) continue;
                queue.addAll(directChildren.getOrDefault(cur, Set.of()));
            }
            anc.put(node, Set.copyOf(acc));
        }
        // 祖先闭包：每个节点反填到其全部上位（节点 -> 其全部祖先）
        Map<Identifier, Set<Identifier>> desc = new HashMap<>();
        for (Map.Entry<Identifier, Set<Identifier>> e : anc.entrySet()) {
            Identifier child = e.getKey();
            for (Identifier parent : e.getValue()) {
                desc.computeIfAbsent(parent, k -> new LinkedHashSet<>()).add(child);
            }
        }
        // anc 为子孙闭包 → 供 childrenOf 做「父辖子」；desc 为祖先闭包 → 供 ancestorsOf 做「子查父」。
        ancestors = Collections.unmodifiableMap(desc);
        descendants = Collections.unmodifiableMap(anc);
    }

    /** 父类别的子集合（传递闭包：含全部子孙），供 GateExpr 解析器做「父辖子」匹配。 */
    public static Set<Identifier> childrenOf(Identifier tag) {
        return descendants.getOrDefault(tag, Set.of());
    }

    /** 子类别的祖先集合（传递闭包，不含自身）。 */
    public static Set<Identifier> ancestorsOf(Identifier tag) {
        return ancestors.getOrDefault(tag, Set.of());
    }

    /**
     * 作用域命中：物品叶类型集合 {@code leafTypes} 是否落在 {@code category} 之下。
     * <ul>
     *   <li>{@code category} 为单键（如 {@code weapon} / {@code foci}）：叶或其任一祖先等于该键即命中；</li>
     *   <li>{@code category} 为路径（如 {@code armor/legs} / {@code accessory/charm}）：路径每个段都必须
     *       落在（叶 ∪ 祖先）中，等价于「类型链包含该路径」。</li>
     * </ul>
     * 例：legs 物品 leaf=["legs"]，ancestors={armor,equipment}；isWithin("armor/legs") →
     * 段 armor、legs 均在 {legs,armor,equipment} 中 → 命中。
     */
    public static boolean isWithin(Identifier category, java.util.Collection<Identifier> leafTypes) {
        if (category == null) return false;
        Set<Identifier> scope = new HashSet<>();
        for (Identifier t : leafTypes) {
            addScope(scope, t);
            for (Identifier a : ancestorsOf(t)) {
                addScope(scope, a);
            }
        }
        for (String seg : category.getPath().split("/")) {
            if (!scope.contains(Identifier.fromNamespaceAndPath(category.getNamespace(), seg))) return false;
        }
        return true;
    }

    /**
     * 把类型路径及其各段都纳入作用域：路径型类别键（如 {@code accessory/charm}）拆为
     * {@code accessory} / {@code charm} 两段，使仅取单段命名空间的词缀池（{@code charm}）也能命中
     * 嵌套路径型物品类型（{@code accessory/charm}）。原实现只加完整路径与祖先路径，导致
     * 药剂（类型键裸 {@code flask}）可命中、护符（类型键 {@code accessory/charm}）却命不中同名词缀池。
     */
    private static void addScope(Set<Identifier> scope, Identifier type) {
        if (type == null) return;
        scope.add(type);
        for (String seg : type.getPath().split("/")) {
            if (!seg.isEmpty()) scope.add(Identifier.fromNamespaceAndPath(type.getNamespace(), seg));
        }
    }
}
