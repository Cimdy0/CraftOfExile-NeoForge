package com.cimdy.coe.ui.screen.talent;

import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.ui.screen.graph.NodeAllocator;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.codec.talent.data.*;
import com.cimdy.coe.data.codec.talent.data.TalentWeaponSet;
import com.cimdy.coe.data.codec.talent.effect.*;
import com.cimdy.coe.data.codec.talent.node.*;
import com.cimdy.coe.data.codec.talent.tree.*;
import com.cimdy.coe.event.net.NetEvent;
import com.cimdy.coe.item.gem.Jewel;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 天赋分配状态：持有模型 + 已分配集合 + 每节点所选选项 + 每节点武器组绑定，提供连通性规则。
 * 规则：起点始终可点；其余节点必须「与某个<b>已点亮</b>节点相邻」且有点数剩余。
 * 多选一节点（旅行节点）在分配时记录玩家所选下标，效果据此聚合。
 *
 * <p><b>武器专精（仿 POE2）</b>：每个已分配节点都绑定一个武器组（{@link TalentWeaponSet}）。
 * 只有「全局节点」或「绑定到当前激活武器组」的节点才处于<b>点亮</b>状态（{@link #isActive}），
 * 其余已分配节点<b>休眠</b>（仍分配但失效、不提供效果、不计入点亮连通）。
 * 切换激活武器组（{@link #setActiveSet}）即可让不同武器组的节点点亮/休眠，对应 POE2
 * 「切换武器后，部分天赋点亮到其他位置」。分配时新节点绑定到当时激活的武器组。
 *
 * <p><b>预算模型</b>：玩家总点数为 {@link TalentTreeModel#totalPoints()}。各武器组（含全局 0）
 * 独立统计「已分配数」，但所有组的已分配数总和不得超过总点数：这就是
 * 「全局天赋 + 套装天赋 = 玩家所有天赋树」。某组加点消耗该组 1 点；
 * 切换到另一组后加同一节点时，原组返还 1 点、新组消耗 1 点；只有彻底取消节点才
 * 释放其所占预算（除非该点已被另一组使用，详见 {@link #preview(String, boolean)}）。
 */
public class TalentAllocator implements NodeAllocator {
    private final TalentTreeModel model;
    private final Player player;
    private final Set<String> allocated = new HashSet<>();
    private final Map<String, Integer> choices = new HashMap<>();
    /** 节点 id → 已插入的珠宝物品（珠宝插槽节点用）。 */
    private final Map<String, ItemStack> sockets = new HashMap<>();
    /** 节点 id → 绑定的武器组 id（0=全局，1=I，2=II）。 */
    private final Map<String, Integer> bindings = new HashMap<>();
    /** 当前激活武器组（1 或 2），由玩家切换；决定哪些绑定节点点亮。 */
    private int activeSet;
    /**
     * 各武器组（含全局 0）已分配的点数：索引 0..{@link TalentWeaponSet#MAX_BINDING}。
     * 全局天赋 + 各套装天赋 = {@link #remainingBySet} 各项「已用」之和，
     * 且总和不得超过 {@link TalentTreeModel#totalPoints()}。
     */
    private final Map<Integer, Integer> usedBySet = new java.util.LinkedHashMap<>();
    /** 无向邻接表：由节点 links 推导的统一无向图。 */
    private final Map<String, List<String>> adjacency = new HashMap<>();
    /**
     * 珠宝「辐射点亮」集合：所有处于某已插入辐射珠宝半径内的节点 id。
     * 这些节点即使没有任何图连接，也可被点亮（见 {@link #canAllocate}）。
     * 半径由珠宝 {@link JewelData#radius()} 决定，圆心为插槽节点位置（见 {@link #positions}）。
     */
    private final Set<String> jewelActivated = new HashSet<>();
    /** 珠宝辐射范围圈（GUI 用）：圆心 + 半径，供画布绘制辐射范围。 */
    private final List<JewelZone> jewelZones = new ArrayList<>();
    /** 节点 id → 世界坐标（由各节点显式 x/y 提取），用于计算辐射半径覆盖。 */
    private final Map<String, float[]> positions;

    /**
     * 布局坐标缩放系数：UI 层可把 POE2 原始坐标按此比例缩小，使节点间距更紧凑。
     * 珠宝辐射半径也会同步缩放，保证「半径内节点」的判定与视觉一致。
     * 默认 1.0（服务端校验使用，保持与数据一致）。
     */
    private final float layoutScale;

    /**
     * @param layout 节点世界坐标（由各节点显式 x/y 提取），用于辐射半径判定。
     */
    public TalentAllocator(TalentTreeModel model, TalentState state, Player player, Map<String, float[]> layout) {
        this(model, state, player, layout, 1.0f);
    }

    /**
     * @param layout      节点世界坐标（由各节点显式 x/y 提取）。
     * @param layoutScale 坐标缩放系数（UI 层小于 1.0 以压缩间距）。
     */
    public TalentAllocator(TalentTreeModel model, TalentState state, Player player,
                           Map<String, float[]> layout, float layoutScale) {
        this.model = model;
        this.player = player;
        this.positions = layout;
        this.layoutScale = layoutScale;
        // 原 AttachmentRegistry.ACTIVE_WEAPON_SET 已迁入 AdditionData（key=active_weapon_set，默认 1）
        this.activeSet = (int) AdditionData.getOrDefault(player, AdditionKeys.ACTIVE_WEAPON_SET, 1.0);
        for (TalentAllocation a : state.allocated()) {
            allocated.add(a.id());
            choices.put(a.id(), a.choice());
            bindings.put(a.id(), a.weaponSet());
            if (a.hasSocket()) sockets.put(a.id(), a.socket());
        }
        for (var e : model.resolvedEdges()) {
            adjacency.computeIfAbsent(e.fromId(), k -> new ArrayList<>()).add(e.toId());
            adjacency.computeIfAbsent(e.toId(), k -> new ArrayList<>()).add(e.fromId());
        }
        rebuildJewelActivation();
        rebuildBudgets();
    }

    /**
     * 按当前 {@code bindings} 重算各武器组的「已分配数」并填入 {@link #usedBySet}。
     * 用于构造时与 {@link #syncAllocations} / 取消分配后的统计同步。
     */
    private void rebuildBudgets() {
        usedBySet.clear();
        for (int s = 0; s <= TalentWeaponSet.MAX_BINDING; s++) usedBySet.put(s, 0);
        for (String id : allocated) {
            int s = bindings.getOrDefault(id, 0);
            if (s < 0 || s > TalentWeaponSet.MAX_BINDING) s = 0;
            usedBySet.merge(s, 1, Integer::sum);
        }
    }

    /**
     * 某武器组（含全局 0）的剩余预算：{@code totalPoints - 该组已分配}。
     * 全局与各套装共享总预算：总和不能超过 {@code totalPoints}。
     */
    public int remainingForSet(int set) {
        int s = (set < 0 || set > TalentWeaponSet.MAX_BINDING) ? 0 : set;
        int used = usedBySet.getOrDefault(s, 0);
        return Math.max(0, model.totalPoints() - used);
    }

    /**
     * 当前 UI 激活武器组的剩余预算（与 {@link #remainingForSet(int)} 同义，便于顶部点数显示）。
     */
    public int remainingForActiveSet() {
        return remainingForSet(activeSet);
    }

    /**
     * 武器组（含全局 0）的已分配节点数，供 UI 副标题 / tooltip 显示。
     */
    public int usedForSet(int set) {
        int s = (set < 0 || set > TalentWeaponSet.MAX_BINDING) ? 0 : set;
        return usedBySet.getOrDefault(s, 0);
    }

    /**
     * 便捷工厂：服务端校验时用。布局直接取自 {@link TalentTreeModelLoader#getLayout(Identifier)} 的缓存
     * （加载 / 重载阶段已算好），与服务端、客户端结果一致，避免每次校验重复 O(n²) 计算。
     *
     * @param treeId 天赋树 id（{@link Identifier}），用于定位缓存的布局坐标。
     */
    public static TalentAllocator create(TalentTreeModel model, Identifier treeId, TalentState state, Player player) {
        return new TalentAllocator(model, state, player, TalentTreeModelLoader.getLayout(treeId));
    }

    /**
     * 设置当前激活武器组（0–9：0=全局，1–9=快捷栏 0–8）。
     * 切换后 {@link #isActive} 结果随之改变，画布自动重渲染。
     * 该值由玩家实际选中的快捷栏栏位驱动（见 {@code NetEvent}），UI 中也可临时预览某栏位。
     */
    public void setActiveSet(int set) {
        this.activeSet = (set >= 0 && set <= TalentWeaponSet.MAX_BINDING) ? set : 0;
    }

    public int activeSet() {
        return activeSet;
    }

    /**
     * 重算珠宝「辐射点亮」集合与辐射范围圈：遍历所有已插入的珠宝，
     * 对 {@link JewelData#radiates()} 为真的珠宝，将以插槽节点为圆心、{@code radius} 为半径的
     * 圆形范围内的所有节点标记为可无链接点亮（仿 PoE 珠宝辐射语义），并记录范围圈供 GUI 绘制。
     * 在构造与每次插入 / 取出珠宝后调用。
     */
    private void rebuildJewelActivation() {
        jewelActivated.clear();
        jewelZones.clear();
        for (var e : sockets.entrySet()) {
            ItemStack jewel = e.getValue();
            if (jewel.isEmpty()) continue;
            JewelData data = Jewel.dataOf(jewel);
            if (!data.radiates()) continue;
            float[] sp = positions.get(e.getKey());
            if (sp == null) continue;
            // 与布局坐标同比例缩放，保证视觉半径与节点间距一致。
            float r = data.radius() * layoutScale;
            float r2 = r * r;
            for (TalentNodeDef n : model.nodes()) {
                float[] np = positions.get(n.id());
                if (np == null) continue;
                float dx = np[0] - sp[0], dy = np[1] - sp[1];
                if (dx * dx + dy * dy <= r2) jewelActivated.add(n.id());
            }
            // 范围圈颜色：珠宝紫，便于与武器组色 / 插槽青区分
            jewelZones.add(new JewelZone(sp[0], sp[1], r, Theme.current().talentJewelZone()));
        }
    }

    /** 珠宝辐射范围圈（GUI 视图模型）：圆心 + 半径 + 颜色。 */
    public record JewelZone(float x, float y, float radius, int color) {}

    /** 所有已插入辐射珠宝的范围圈（供画布绘制）。 */
    public List<JewelZone> jewelZones() {
        return jewelZones;
    }

    public int totalPoints() {
        return model.totalPoints();
    }

    public int allocatedCount() {
        return allocated.size();
    }

    /** 当前完整分配（含选项、插槽珠宝与武器组绑定），用于权威持久化与效果聚合。 */
    public List<TalentAllocation> allocations() {
        List<TalentAllocation> list = new ArrayList<>();
        for (String id : allocated) {
            list.add(new TalentAllocation(id, choices.getOrDefault(id, -1),
                    sockets.getOrDefault(id, ItemStack.EMPTY), bindings.getOrDefault(id, 0)));
        }
        return list;
    }

    public int weaponSetOf(String id) {
        return bindings.getOrDefault(id, 0);
    }

    public void setChoice(String id, int choice) {
        choices.put(id, choice);
    }

    /** 该插槽节点当前插入的珠宝（无则返回 {@link ItemStack#EMPTY}）。 */
    public ItemStack socketOf(String id) {
        return sockets.getOrDefault(id, ItemStack.EMPTY);
    }

    /** 设置 / 清空某插槽节点的珠宝（传入空则视为清空）。 */
    public void setSocket(String id, ItemStack jewel) {
        if (jewel == null || jewel.isEmpty()) sockets.remove(id);
        else sockets.put(id, jewel);
        rebuildJewelActivation(); // 珠宝变化会改变「无链接点亮」集合
    }

    public int remainingPoints() {
        // 全局武器组（0）的剩余预算 = 玩家可见的「可用点数」。
        // 全局与各套装共享总预算，但 UI 顶部 / 弹窗总是展示当前激活组的剩余数。
        return remainingForActiveSet();
    }

    public boolean isAllocated(String id) {
        return allocated.contains(id);
    }

    /**
     * 该节点当前是否点亮（生效）。起点恒点亮；其余已分配节点仅当
     * 其武器组绑定匹配当前激活武器组（或全局）时才点亮。
     */
    public boolean isActive(String id) {
        if (!isAllocated(id)) return false;
        if (model.startNodeIds().contains(id)) return true; // 起点恒为全局点亮，避免切换武器组后整树失联
        return TalentWeaponSet.active(bindings.getOrDefault(id, 0), activeSet);
    }

    public boolean canAllocate(String id) {
        if (isAllocated(id)) return false;
        // 预算门槛：当前激活武器组（加点归属组）必须有剩余点数
        if (remainingForSet(activeSet) <= 0) return false;
        if (model.startNodeIds().contains(id)) return true;
        TalentNodeDef def = model.node(id);
        if (def != null) {
            // 互斥：已分配任一互斥节点则不可再分配本节点
            for (String ex : def.excludes()) {
                if (isAllocated(ex)) return false;
            }
        }
        // 连通性基于「已点亮」邻居：只有当前激活武器组（及全局）的节点能延伸出新节点，
        // 休眠的武器组专属节点不能当作连通来源（仿 POE2：切换武器组后该组节点才会被点亮/可延伸）。
        for (String n : adjacency.getOrDefault(id, List.of())) {
            if (isActive(n) || jewelActivated.contains(n)) return true;
        }
        // 珠宝「无链接点亮」：插入珠宝插槽的特殊宝石可点亮指定节点，即便它们之间没有任何图连接。
        if (jewelActivated.contains(id)) return true;
        return false;
    }

    /** 该节点是否处于某已插入辐射珠宝的范围内（无需图连接即可分配 / 点亮）。 */
    public boolean isJewelRadiated(String id) {
        return jewelActivated.contains(id);
    }

    public void preview(String id, boolean allocate) {
        if (allocate) {
            if (!canAllocate(id)) return;
            if (allocated.contains(id)) {
                // 已在某组分配的节点被再次点击：视为「换组」——原组返还 1 点，新组消耗 1 点。
                // 这是用户语义「套装天赋数返还一点」的体现：玩家切换到另一组加同一节点时，
                // 原组（套装）被该节点占用的 1 点被释放，改由新组占用。
                int prevSet = bindings.getOrDefault(id, 0);
                if (prevSet != activeSet) {
                    if (prevSet >= 0 && prevSet <= TalentWeaponSet.MAX_BINDING) {
                        usedBySet.merge(prevSet, -1, Integer::sum);
                        if (usedBySet.get(prevSet) < 0) usedBySet.put(prevSet, 0);
                    }
                    usedBySet.merge(activeSet, 1, Integer::sum);
                    bindings.put(id, activeSet);
                }
                return;
            }
            allocated.add(id);
            // 新分配的节点绑定到当前激活武器组（仿 POE2：在查看某武器组时加点即归属该组）
            bindings.put(id, activeSet);
            usedBySet.merge(activeSet, 1, Integer::sum);
        } else {
            int prevSet = bindings.getOrDefault(id, 0);
            allocated.remove(id);
            choices.remove(id);
            bindings.remove(id);
            sockets.remove(id); // 取消节点时清空其插槽珠宝（服务端会把珠宝退回背包）
            if (prevSet >= 0 && prevSet <= TalentWeaponSet.MAX_BINDING) {
                usedBySet.merge(prevSet, -1, Integer::sum);
                if (usedBySet.get(prevSet) < 0) usedBySet.put(prevSet, 0);
            }
        }
    }

    public void applySync(Set<String> allocated) {
        this.allocated.clear();
        this.choices.clear();
        this.bindings.clear();
        this.sockets.clear();
        this.allocated.addAll(allocated);
    }

    /** 带选项与插槽珠宝的权威同步（服务端下发的完整分配）。 */
    public void syncAllocations(List<TalentAllocation> allocations) {
        this.allocated.clear();
        this.choices.clear();
        this.bindings.clear();
        this.sockets.clear();
        for (TalentAllocation a : allocations) {
            allocated.add(a.id());
            choices.put(a.id(), a.choice());
            bindings.put(a.id(), a.weaponSet());
            if (a.hasSocket()) sockets.put(a.id(), a.socket());
        }
        rebuildBudgets();
    }
}
