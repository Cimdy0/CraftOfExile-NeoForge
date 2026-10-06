package com.cimdy.coe.event.data;

import com.cimdy.coe.CraftOfExile;

import java.util.ArrayList;
import java.util.List;

/**
 * M8 · 装载期护栏（数据驱动反馈，对抗「推论 B 静默失效」）。
 *
 * <p>技能数据装载期间，各装载环节（形态校验、伤害类型校验、stat 接线缺口等）调用
 * {@link #warn(String)} / {@link #error(String)} 汇聚告警，装载收尾时由 {@link #end()} 统一 dump 汇总日志。
 * 这样任何「数据引用了不存在的形态 / 伤害类型 / stat」都不会再静默通过，而是在启动日志里可见。
 *
 * <p>设计为 fail-open：本类只记录、不抛异常、不阻断装载——错误数据照常落盘，但被显式暴露，
 * 避免「拼错的 damage_type 静默按通用伤害结算」「未登记的形态静默忽略」这类难排查缺陷。
 */
public final class SkillLoadReporter {

    private static final List<String> WARNINGS = new ArrayList<>();
    private static final List<String> ERRORS = new ArrayList<>();
    private static boolean active = false;

    private SkillLoadReporter() {
    }

    /** 一次装载批次开始时清空并激活。 */
    public static void begin() {
        WARNINGS.clear();
        ERRORS.clear();
        active = true;
    }

    public static void warn(String msg) {
        if (active) WARNINGS.add(msg);
    }

    public static void error(String msg) {
        if (active) ERRORS.add(msg);
    }

    /** 装载批次结束：dump 汇总（仅在有告警/错误时打日志，避免无谓刷屏）。 */
    public static void end() {
        active = false;
        if (WARNINGS.isEmpty() && ERRORS.isEmpty()) return;
        CraftOfExile.LOGGER.warn("[SkillLoadReport] 装载期校验汇总：{} 条警告 / {} 条错误",
                WARNINGS.size(), ERRORS.size());
        for (String w : WARNINGS) {
            CraftOfExile.LOGGER.warn("[SkillLoadReport] WARN: " + w);
        }
        for (String e : ERRORS) {
            CraftOfExile.LOGGER.error("[SkillLoadReport] ERROR: " + e);
        }
    }
}
