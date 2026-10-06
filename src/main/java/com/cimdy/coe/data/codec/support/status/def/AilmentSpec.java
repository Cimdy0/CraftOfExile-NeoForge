package com.cimdy.coe.data.codec.support.status.def;

/**
 * POE2 异常「积蓄值（buildup）」规格：描述某异常如何经积蓄模型触发（取代 POE1 固定几率模型），
 * 并承载该异常的<b>全部定义性数值</b>（每秒伤害系数 / 默认来源伤害槽位 / 是否攻击类 / 基础强度 / 基础时长）——
 * 使异常强度/时长的施加细节数据化，代码内不再按状态 id switch 硬编码（审计收敛项 ②⑤）。
 *
 * <p>POE2 机制（相对 POE1 的核心改动）：异常不再以「每次命中固定几率」施加，而是命中造成的
 * 对应元素伤害会向该异常的积蓄条增加；增加量 = {@code 命中该元素伤害 / 异常门槛}（百分比进度，
 * 门槛 ≈ 目标最大生命的一半）。积蓄达到 100%（= 门槛）即触发，随后积蓄清零（保留溢出）。
 * 单次高伤比多次小伤累积更快、更可靠（大命中优势）。积蓄条会随时间衰减。
 *
 * <p>各异常分工：
 * <ul>
 *   <li>{@link BuildupMode#THRESHOLD}：积蓄满门槛后触发（冰冻 / 感电 / 电击 / 重击晕 / 点燃 / 中毒）。</li>
 *   <li>{@link BuildupMode#PARTIAL}：部分积累即时生效，无需满值（冰缓：每次命中按积蓄比例减速）。</li>
 *   <li>{@link BuildupMode#CHANCE}：固定几率触发，不随门槛缩放（流血 —— POE2 中唯一仍走几率的伤害性异常）。</li>
 * </ul>
 *
 * @param element            触发元素（"fire"/"cold"/"lightning"/"physical"/"chaos"/"none"）
 * @param mode               积蓄模式
 * @param thresholdMultiplier 门槛倍率（如电击 1.5）
 * @param perSecondCoeff     每秒伤害系数（DoT 异常：ignite/poison=0.20，bleed=0.15；非 DoT 为 0）
 * @param defaultSourceSlot  默认来源伤害槽位（damages[]: 0=物理 1=混沌 2=火 3=冰 4=电；bleed=0/poison=1/ignite=2；无效 -1）
 * @param attackAilment      是否攻击类异常（流血/中毒 → DoT 结算额外计入 ATTACK 标签）
 * @param baseMagnitude      基础强度（控制类 0~1，×100 得百分比；冰缓/感电 0.2，冰冻/电击 1.0）
 * @param baseDurationSeconds 基础时长（秒；DoT：bleed 5/ignite 4/poison 2；控制：chill 2/freeze 4/shock 4/electrocute 3）
 */
public record AilmentSpec(String element, BuildupMode mode, double thresholdMultiplier,
                          double perSecondCoeff, int defaultSourceSlot,
                          boolean attackAilment, double baseMagnitude, int baseDurationSeconds) {

    /** 不参与积累的状态（如直接施加的 buff / debuff）。 */
    public static final AilmentSpec NONE = new AilmentSpec("none", BuildupMode.NONE, 1.0, 0.0, -1, false, 1.0, 0);

    public static AilmentSpec of(String element, BuildupMode mode) {
        return new AilmentSpec(element, mode, 1.0, 0.0, -1, false, 1.0, 0);
    }

    public static AilmentSpec of(String element, BuildupMode mode, double thresholdMultiplier) {
        return new AilmentSpec(element, mode, thresholdMultiplier, 0.0, -1, false, 1.0, 0);
    }

    /** 伤害性异常（DoT）：声明每秒伤害系数、默认来源伤害槽位、是否攻击类、基础时长（秒）。 */
    public static AilmentSpec dot(String element, BuildupMode mode, double perSecondCoeff,
                                  int defaultSourceSlot, boolean attackAilment, int baseDurationSeconds) {
        return new AilmentSpec(element, mode, 1.0, perSecondCoeff, defaultSourceSlot, attackAilment, 1.0, baseDurationSeconds);
    }

    /** 控制类异常（冰缓/冰冻/感电/电击）：声明基础强度（0~1，×100 得百分比）与基础时长（秒）。 */
    public static AilmentSpec control(String element, BuildupMode mode, double baseMagnitude, int baseDurationSeconds) {
        return new AilmentSpec(element, mode, 1.0, 0.0, -1, false, baseMagnitude, baseDurationSeconds);
    }

    /** 控制类异常（带门槛倍率，如电击 1.5）。 */
    public static AilmentSpec control(String element, BuildupMode mode, double thresholdMultiplier,
                                      double baseMagnitude, int baseDurationSeconds) {
        return new AilmentSpec(element, mode, thresholdMultiplier, 0.0, -1, false, baseMagnitude, baseDurationSeconds);
    }

    /**
     * 积累模式枚举。
     */
    public enum BuildupMode {
        /** 不参与积蓄（如直接施加的 buff/debuff）。 */
        NONE,
        /** 积蓄满 100%（门槛）后触发。 */
        THRESHOLD,
        /** 部分积累即时生效（冰缓：每次命中按比例减速，无需满值）。 */
        PARTIAL,
        /** 固定几率触发，不随门槛缩放（流血 —— POE2 例外）。 */
        CHANCE
    }
}
