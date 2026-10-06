package com.cimdy.coe.data.codec.support.status.def;

/**
 * 状态大类（与 POE2DB 实证分类一致，取代原单一 Ailment 概念）。
 * 文档 §1.2：异常 / 增益 / 减益 / 技能型(诅咒·光环·标记·捷) / 神龛，各自平级。
 */
public enum StatusKind {
    /** 伤害性异常：点燃 / 流血 / 中毒（DoT） */
    DAMAGING,
    /** 非伤害性异常（控制）：冰缓 / 冰冻 / 感电 / 电击 / 钉住 / 眩晕 / 重击晕 */
    CONTROL,
    /** 增益状态：猛攻 / 疾风 / 噬魂者 / 秘术增强 / 肾上腺素 / 护体 / 相位 / 不洁之力 / 执政官 */
    BUFF,
    /** 减益状态：致盲 / 伤残 / 阻碍 / 曝露 / 凋零 / 破甲 / 完全破甲 / 嘲讽 / 恐吓 / 石化 / 招架 / 穿刺 */
    DEBUFF,
    /** 诅咒（技能型） */
    CURSE,
    /** 光环（技能型） */
    AURA,
    /** 标记（技能型） */
    MARK,
    /** 捷（技能型 buff） */
    HERALD,
    /** 神龛（非技能型，击败神龛怪互动获得） */
    SHRINE,
    /** 充能球（堆叠型单独状态，每种球独立 StatusType */
    CHARGED,
    /** 形态（Shapeshift）：熊/狼/龙等变身状态，互斥（同时仅一形态），带 form 标签 */
    FORM,
    /** 地面（存在性标记，由 GroundFieldManager 持续刷新施加；运行时经 StatusType.dynamic 动态注册，不静态注册） */
    GROUND
}
