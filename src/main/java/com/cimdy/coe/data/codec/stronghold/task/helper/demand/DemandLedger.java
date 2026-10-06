package com.cimdy.coe.data.codec.stronghold.task.helper.demand;

import com.cimdy.coe.data.codec.stronghold.task.helper.claim.DataUpdate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;

import java.util.*;

/**
 * 需求账本（脏标记调度核心，方案 B）。
 * <p>
 * 原 {@code DataUpdate.tick} 每 20 tick 全量扫描所有据点组的供需，复杂度高。
 * 引入脏标记后：库存变化 / 任务完成 / 跨维搬运落地等事件才标记对应终端为脏，
 * 调度只重算脏终端；每 {@link #FULL_SCAN_EVERY} tick 触发一次全量兜底校正，
 * 确保未被事件捕获的变更最终也能被处理。大规模据点 tick 耗时通常降一个数量级。
 */
public final class DemandLedger {
    /** 全量兜底校正间隔（tick）。20tps × 100 = 5 秒一次全扫描。 */
    public static final int FULL_SCAN_EVERY = 100;

    private static final Set<GlobalPos> dirtyHolds = new HashSet<>();
    /** 站点级脏标记（终端 + 站点方块坐标），用于 SiteManager 增量重算。 */
    private static final Set<SiteKey> dirtySites = new HashSet<>();
    private static int tickCounter = 0;

    /** 每个站点最近一次扫描得到的「需求清单」，供转化类工种（cook/smelt/compost/mill）做需求门控。键为站点方块坐标。 */
    private static final Map<BlockPos, List<DemandInfo>> siteDemands = new HashMap<>();

    /** 站点全量兜底校正间隔（以 DataUpdate tick 计，每 DataUpdate tick ≈ 20 游戏 tick）。5 ≈ 5 秒。 */
    public static final int SITE_FULL_SCAN_EVERY = 5;

    private DemandLedger() {
    }

    /** 标记某终端为脏（其供需 / 任务状态发生变化，需重算）。 */
    public static void markHoldDirty(GlobalPos pos) {
        if (pos != null) dirtyHolds.add(pos);
    }

    /** 标记某终端为干净（处理完成后调用）。 */
    public static void markHoldClean(GlobalPos pos) {
        dirtyHolds.remove(pos);
    }

    public static boolean isHoldDirty(GlobalPos pos) {
        return dirtyHolds.contains(pos);
    }

    /** 标记某终端下的某站点为脏（工人分配 / 库存 / 配置变化，需重算工人平衡）。 */
    public static void markSiteDirty(GlobalPos terminalPos, BlockPos sitePos) {
        if (terminalPos != null && sitePos != null) dirtySites.add(new SiteKey(terminalPos, sitePos));
    }

    /** 标记某终端下的某站点为干净（重算完成后调用）。 */
    public static void markSiteClean(GlobalPos terminalPos, BlockPos sitePos) {
        dirtySites.remove(new SiteKey(terminalPos, sitePos));
    }

    public static boolean isSiteDirty(GlobalPos terminalPos, BlockPos sitePos) {
        return dirtySites.contains(new SiteKey(terminalPos, sitePos));
    }

    /**
     * 记录某站点对应的需求清单（来自最近一次终端扫描）。
     * 转化类工种在生成任务时据此判断「是否存在对产物的需求」，从而由 StockSet 需求驱动而非盲目生成。
     */
    public static void setSiteDemands(BlockPos sitePos, List<DemandInfo> demands) {
        if (sitePos != null) siteDemands.put(sitePos, demands);
    }

    /** 读取某站点对应的需求清单（无记录时返回空列表）。 */
    public static List<DemandInfo> getSiteDemands(BlockPos sitePos) {
        return siteDemands.getOrDefault(sitePos, List.of());
    }

    /** 当前是否为站点全量兜底校正 tick。 */
    public static boolean isFullSiteScanTick() {
        return tickCounter % SITE_FULL_SCAN_EVERY == 0;
    }

    /** 站点级脏标记键（终端维度坐标 + 站点方块坐标）。 */
    private record SiteKey(GlobalPos terminal, BlockPos site) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o instanceof SiteKey k && Objects.equals(terminal, k.terminal) && Objects.equals(site, k.site);
        }

        @Override
        public int hashCode() {
            return Objects.hash(terminal, site);
        }
    }

    /** 推进 tick 计数，返回当前计数。 */
    public static int advanceTick() {
        return ++tickCounter;
    }

    public static int currentTick() {
        return tickCounter;
    }

    /** 当前是否为全量兜底校正 tick。 */
    public static boolean isFullScanTick() {
        return tickCounter % FULL_SCAN_EVERY == 0;
    }

    /** 清空所有脏标记（维度卸载等场景使用）。 */
    public static void clear() {
        dirtyHolds.clear();
        dirtySites.clear();
        siteDemands.clear();
    }
}
