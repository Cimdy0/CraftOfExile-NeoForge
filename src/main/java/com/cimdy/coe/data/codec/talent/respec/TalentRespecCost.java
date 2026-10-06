package com.cimdy.coe.data.codec.talent.respec;

/**
 * 天赋点重置成本表（照 POE2）。
 *
 * <p>POE2 权威规则：<b>每点重置单价只与角色等级有关</b>，与职业 / 已分配点数 / 重置次数无关；
 * 1 级 15 gold → 100 级 10129 gold。POE2 已移除 Orb of Regret，重置 100% 走金币。
 *
 * <p>数值来源为 1–100 级<b>完整查表</b>（下标即等级，下标 0 未使用、恒为 0），
 * 不使用拟合公式替代——分段幂律在 1–20 级区间误差可达 15%，会导致重置定价明显偏离原作。
 *
 * <p>本表是全模组<b>唯一</b>的重置成本数值来源：UI / 网络包 / 实际扣费一律经
 * {@link #perPoint(int)} 与 {@link #total(int, int)} 取数，禁止在别处内联任何单价常量。
 */
public final class TalentRespecCost {
    private TalentRespecCost() {
    }

    /** 成本表覆盖的最低等级。 */
    public static final int MIN_LEVEL = 1;
    /** 成本表覆盖的最高等级。 */
    public static final int MAX_LEVEL = 100;

    /** 每点重置单价表：下标 = 角色等级（0 号位占位、不使用）。 */
    private static final long[] PER_POINT = {
            0,        // 0：占位
            15, 19, 25, 31, 39, 46, 60, 73, 85, 98,                                     // 1-10
            113, 128, 145, 163, 182, 211, 225, 241, 257, 273,                            // 11-20
            290, 308, 326, 344, 364, 384, 404, 425, 447, 470,                            // 21-30
            493, 517, 542, 567, 593, 620, 648, 676, 706, 736,                            // 31-40
            767, 799, 832, 866, 900, 936, 973, 1010, 1049, 1089,                         // 41-50
            1130, 1172, 1215, 1259, 1304, 1351, 1399, 1448, 1498, 1550,                  // 51-60
            1603, 1657, 1713, 1770, 1829, 1889, 1950, 2014, 2078, 2145,                  // 61-70
            2213, 2282, 2354, 2427, 2502, 2578, 2657, 2737, 2820, 2904,                  // 71-80
            3089, 3281, 3480, 3686, 3899, 4120, 4349, 4585, 4829, 5081,                  // 81-90
            5509, 5952, 6412, 6889, 7383, 7895, 8425, 8974, 9542, 10129                  // 91-100
    };

    /**
     * 指定等级下重置 <b>单个</b>天赋点的金币单价。{@code level} 超出 {@code [1, 100]} 时钳制到边界。
     */
    public static long perPoint(int level) {
        int lv = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
        return PER_POINT[lv];
    }

    /**
     * 指定等级下重置 {@code pointCount} 个天赋点的总金币花费（= 单价 × 点数）。
     * {@code pointCount} 为负时按 0 计。
     */
    public static long total(int level, int pointCount) {
        return perPoint(level) * Math.max(0, pointCount);
    }
}
