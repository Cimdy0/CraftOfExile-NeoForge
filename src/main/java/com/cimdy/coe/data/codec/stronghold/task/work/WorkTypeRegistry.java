package com.cimdy.coe.data.codec.stronghold.task.work;

import com.cimdy.coe.entity.goal.stronghold.behavior.BehaviorRegistry;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdBehavior;
import com.cimdy.coe.data.codec.stronghold.task.Task;

import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.TaskGenerator;
import com.cimdy.coe.data.codec.progression.PlayerProgress;
import com.cimdy.coe.data.codec.progression.ProgressionService;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.charge.*;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant.*;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest.*;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.SiteTypes;
import com.cimdy.coe.entity.goal.stronghold.impl.work.animal.*;
import com.cimdy.coe.entity.goal.stronghold.impl.work.block.*;
import com.cimdy.coe.entity.goal.stronghold.impl.work.charge.*;
import com.cimdy.coe.entity.goal.stronghold.impl.work.misc.*;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 工种总注册表：将「基于 {@link WorkTask} 的 8 个工种」的 Site / Task / Goal 在一处声明，
 * 作为 {@link WorkTaskRegistry}（方块 → 任务）与 {@link com.cimdy.coe.entity.goal.stronghold.behavior.BehaviorRegistry}
 * （目标注入）的唯一内建来源，避免三套平行清单需手动同步。
 * <p>
 * 第三方模组仍可通过 {@code META-INF/services} 声明 {@link WorkTaskFactory} / {@link com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdBehavior}
 * 进行零核心改动扩展，本注册表仅收纳内建工种。
 */
public final class WorkTypeRegistry {
    private static final List<WorkType> TYPES = new ArrayList<>();

    static {
        // 一处声明：Site 类型 + Task 类 + Goal 入口 + 任务生成器 + 方块谓词。
        // 类初始化是线程安全的，避免 ensureInitialized 竞态；workTypes() 返回不可变快照。
        TYPES.add(WorkType.of(SiteTypes.ENDER_CHARGE_SITE, EnderChargeTask.class, EnderChargeGoal.ENTRY, EnderChargeTask::generateTask, EnderChargeTask.BLOCK_PREDICATE));
        TYPES.add(WorkType.of(SiteTypes.AGRICULTURE_SITE,  FarmerTask.class,    FarmerGoal.ENTRY,    FarmerTask::generateTask,    FarmerTask.BLOCK_PREDICATE));
        TYPES.add(WorkType.of(SiteTypes.FORESTRY_SITE,    ForestryTask.class,   ForestryGoal.ENTRY,   ForestryTask::generateTask,   ForestryTask.BLOCK_PREDICATE));
        TYPES.add(WorkType.of(SiteTypes.MINING_SITE,      MiningTask.class,     MiningGoal.ENTRY,     MiningTask::generateTask,     MiningTask.BLOCK_PREDICATE));
        TYPES.add(WorkType.of(SiteTypes.SHEAR_SITE,       ShearTask.class,      ShearGoal.ENTRY,      ShearTask::generateTask,      ShearTask.BLOCK_PREDICATE));
        TYPES.add(WorkType.of(SiteTypes.HUNT_SITE,        HuntTask.class,       HuntGoal.ENTRY,       HuntTask::generateTask,       HuntTask.BLOCK_PREDICATE));
        TYPES.add(WorkType.of(SiteTypes.MILK_SITE,        MilkTask.class,       MilkGoal.ENTRY,       MilkTask::generateTask,       MilkTask.BLOCK_PREDICATE));
        TYPES.add(WorkType.of(SiteTypes.FISHER_SITE,      FisherTask.class,     FisherGoal.ENTRY,     FisherTask::generateTask,     FisherTask.BLOCK_PREDICATE));
        // 注意：烹饪 / 冶炼 / 堆肥 / 研磨 不再作为独立工种注册。它们与合成同构，
        // 改由 TaskGenerator.generateConvertTasks 在「需求/供应」判断后生成
        // 「搬运原料 → 站点转化 → 搬运产物」的 DAG 链，其执行器为 ConvertGoal（见 BehaviorRegistry）。
    }

    private WorkTypeRegistry() {}

    /** 注册一个内建工种（第三方扩展保留接口）。 */
    public static synchronized void register(WorkType type) {
        TYPES.add(type);
    }

    /** 返回全部内建工种（不可变快照，防止外部修改注册表）。 */
    public static List<WorkType> workTypes() {
        return List.copyOf(TYPES);
    }

    /** 兼容保留：内建工种已在静态初始化块注册完成。 */
    public static void ensureInitialized() {
    }

    // ==================== 进度门控（P4：工种授权） ====================

    /**
     * 该玩家当前<b>已获授权</b>的工种。
     *
     * <p>这是「基建循序渐进」的关键一环：8 个工种不再开局全开，而是随等级与科技逐个解禁
     * （规则见 {@code job_charter} 数据包与 {@link ProgressionService#isWorkTypeUnlocked}）。
     * 站点绑定与终端 UI 都应走本方法，而不是直接遍历 {@link #workTypes()}。
     */
    public static List<WorkType> availableFor(PlayerProgress progress) {
        List<WorkType> list = new ArrayList<>();
        for (WorkType type : workTypes())
            if (ProgressionService.isWorkTypeUnlocked(progress, type.name())) list.add(type);
        return list;
    }

    public static List<WorkType> availableFor(Player player) {
        return availableFor(PlayerProgress.get(player));
    }

    /** 该站点类型对应的工种是否已对玩家开放（未在注册表中的站点视为不受门控）。 */
    public static boolean isUnlocked(Player player, Site.Type<?> siteType) {
        for (WorkType type : workTypes())
            if (type.siteType() == siteType)
                return ProgressionService.isWorkTypeUnlocked(player, type.name());
        return true;
    }

    /** 未解锁工种的提示名（用于给玩家的失败反馈）。 */
    public static String workTypeName(Site.Type<?> siteType) {
        return SiteTypes.workTypeName(siteType);
    }
}
