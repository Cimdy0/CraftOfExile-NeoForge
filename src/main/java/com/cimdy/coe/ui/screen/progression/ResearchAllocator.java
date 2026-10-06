package com.cimdy.coe.ui.screen.progression;

import com.cimdy.coe.data.codec.progression.PlayerProgress;
import com.cimdy.coe.ui.screen.graph.NodeAllocator;
import com.cimdy.coe.data.codec.progression.ProgressionData;
import com.cimdy.coe.data.codec.progression.ProgressionService;
import com.cimdy.coe.data.codec.progression.research.ResearchNodeDef;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

/**
 * 研究树的画布分配器：把「玩家研究进度」翻译成画布认识的三态。
 *
 * <p>与天赋分配器的关键差别——研究<b>不可退点</b>，也不存在武器组绑定，
 * 因此这里只做只读投影 + 一次性乐观预览：
 * <ul>
 *   <li>{@link #isAllocated} = 已完成 <b>或</b> 进行中（画布据此不再显示为可点）；</li>
 *   <li>{@link #isActive} = 仅已完成（区分「投入了」与「拿到了」，进行中呈暗色）；</li>
 *   <li>{@link #canAllocate} = 委托 {@link ProgressionService#canStartResearch}，
 *       与服务端同一份判定，避免客户端点得动、服务端却拒绝的错位。</li>
 * </ul>
 *
 * <p>{@link #preview} 只做本地即时反馈（点下去立刻变暗青），真正的状态仍以
 * 每帧从已同步附件 {@code PLAYER_PROGRESS} 重新投影为准（见 {@link #refresh}）。
 */
public class ResearchAllocator implements NodeAllocator {

    /** 已完成研究（节点 id 字符串形式）。 */
    private final Set<String> completed = new HashSet<>();
    /** 进行中研究（含本地乐观预览）。 */
    private final Set<String> active = new HashSet<>();

    private PlayerProgress progress;

    public ResearchAllocator(PlayerProgress progress) {
        refresh(progress);
    }

    /** 从玩家进度重建两个集合。每帧调用（附件已由服务端同步，成本只是两次小集合重建）。 */
    public final void refresh(PlayerProgress p) {
        this.progress = p;
        completed.clear();
        for (Identifier id : p.completedResearch()) completed.add(id.toString());
        // 注意：保留本地乐观预览项——服务端确认后它会出现在 activeResearch 里，
        // 被下面的循环重新加入；若服务端拒绝，则本次 clear 后不再出现，自动回滚。
        active.clear();
        for (Identifier id : p.activeResearch()) active.add(id.toString());
    }

    public PlayerProgress progress() {
        return progress;
    }

    public boolean isAllocated(String nodeId) {
        return completed.contains(nodeId) || active.contains(nodeId);
    }

    /** 只有真正完成的研究才「点亮」——进行中的呈休眠态，提示玩家还得等。 */
    public boolean isActive(String nodeId) {
        return completed.contains(nodeId);
    }

    public boolean canAllocate(String nodeId) {
        ResearchNodeDef def = defOf(nodeId);
        return def != null && ProgressionService.canStartResearch(progress, def);
    }

    public int allocatedCount() {
        return completed.size();
    }

    public int remainingPoints() {
        return progress.researchPoints();
    }

    /** 乐观更新：研究不可撤销，故只处理「开始研究」方向。 */
    public void preview(String nodeId, boolean allocate) {
        if (allocate) active.add(nodeId);
        else active.remove(nodeId);
    }

    /** 权威同步：直接以服务端集合覆盖「已完成」，进行中集合交由 {@link #refresh} 维护。 */
    public void applySync(Set<String> allocated) {
        completed.clear();
        completed.addAll(allocated);
    }

    /** 节点 id 字符串 → 研究定义。 */
    public static ResearchNodeDef defOf(String nodeId) {
        Identifier id = Identifier.tryParse(nodeId);
        return id == null ? null : ProgressionData.RESEARCH_NODES.get(id);
    }
}
