#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
属性注册字段处理 + 备注整理（机械重写 AttributeRegister.java，不改任何注册字段值/类型）。

产出：
  - 压缩头注释为 scope 体系 + 三定律 + 图例（约 30 行，替代原 95 行设计说明）。
  - 每个属性声明上方注入一行统一格式备注：
        /** [tag] 用途 / 作用域 / 状态 */
    保留 section 分隔注释（含 ───── / ===== 的行），不改动注册调用。
  - 零基属性显式标 [零基·禁inc]；悬空/数据先行标 [数据先行·待核]/[悬空·待裁定]，
    便于后续写 StatGen 时快速判定「该用哪个属性、用哪种操作符」。

图例 tag：
  [核心]    战斗/系统核心属性，消费端直接读取（Helper / getAttributeValue）
  [fold]    读时折叠数值，StatGen 经 ModifierStatEffect 接线，scope fold 聚合
  [legacy]  原版 AttributeModifier 直挂（合法，非 Stat 数据驱动）
  [reserved] 预留/规划，暂无消费端
  [待删]    过时设计，待词缀 Stat 化后移除（勿新接）
  [数据先行] 数据已落地，运行时消费端待接（reserved-empty，不删常量）
  [悬空]    当前无消费端（注释声称的读取不实），待裁定
  [零基]    base=0，仅可 pctAdd/ADD_VALUE，禁 inc（inc 会 ADD_MULTIPLIED_BASE 静默死）
  [scope X] 须 StatScopes.enter(X) 后读取，否则 EMPTY 下恒读默认

用法：
  python _annotate_attributes.py --dry    仅打印新文件到 stdout
  python _annotate_attributes.py --apply  写回 AttributeRegister.java（先自动备份 .pre_annotate）
"""
import re, sys, os, shutil

SRC = "E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java/com/cimdy/coe/data/AttributeRegister.java"

# ---- 每个属性的统一备注（tag + 用途/范围/状态），按常量名索引 ----
NOTES = {
    "WORK_SPEED": "[legacy] 全局工作速度倍率；WorkSpeed 直接读取（EntityAttributeModificationEvent 挂载全体）",
    "WORK_APTITUDE": "[legacy] 全工种适应度加成；WorkSpeed 直接读取",
    "TALENT_AUTO_SMELT": "[reserved] 天赋：自动熔炼等级（预留，无消费端）",
    "TALENT_FORTUNE": "[reserved] 天赋：幸运（预留，无消费端）",
    "TALENT_LOOTING": "[reserved] 天赋：掠夺（预留，无消费端）",
    "TALENT_MENDING": "[reserved] 天赋：修复（预留，无消费端）",
    "TALENT_EXTRA_XP": "[reserved] 天赋：额外经验（预留，无消费端）",
    "TALENT_MAGNET": "[reserved] 天赋：磁吸（预留，无消费端）",
    "SKILL_SLOT_COUNT": "[核心] 技能槽数量上限；SkillScreen 读取",
    "BANNER_LINGER": "[数据先行·待核] 战旗离开后增益残留秒数；消费端待接",
    "ADD_EMPOWERED_ATTACKS": "[零基·禁inc] 战吼强化额外攻击次数；StatGen 以 inc 接 base=0 静默死，须改 pctAdd",
    "RECENTLY_CONSUMED_DAMAGE": "[数据先行·待核] 近期消耗尸体攻击增伤点数；消费端待接",
    "CORPSE_RETAIN_CHANCE": "[fold·scope corpse] 消耗尸体不摧毁几率；ConsumeCorpseAction 读取",
    "ITEM_CAPACITY": "[核心] 物品容量；CapacityHelper 读取",
    "FOUND_ITEM_RARITY": "[零基·禁inc] 掉落物稀有度；StatGen 以 inc 接 base=0 静默死，须改 pctAdd",
    "FOUND_ITEM_NUMBER": "[零基·禁inc] 掉落物数量；StatGen 以 inc 接 base=0 静默死，须改 pctAdd",
    "ACCURACY": "[核心·scope attack] 命中准确率；命中判定读取（攻方，{attack} 作用域）",
    "AILMENT_BUILDUP": "[fold·scope ailment] 异常积蓄倍率；StatGen 接线",
    "AREA_OF_EFFECT": "[fold·scope] 范围效果倍率；技能投送范围读取",
    "COOLDOWN_RECOVERY_RATE": "[fold] 冷却回复速率倍率",
    "AILMENT_CHANCE": "[fold·scope ailment] 异常触发几率；StatGen 接线",
    "AILMENT_DAMAGE_CONTRIBUTION": "[fold·scope ailment] 异常伤害贡献",
    "AILMENT_DAMAGE_RATE": "[fold·scope ailment] 异常伤害速率",
    "AILMENT_DURATION": "[fold·scope ailment] 异常时长倍率（归一属性）",
    "AILMENT_MAGNITUDE": "[fold·scope ailment] 异常幅度倍率（归一属性，按异常名作用域）",
    "AILMENT_THRESHOLD": "[fold·scope ailment,threshold] 异常门槛倍率（归一属性）",
    "AILMENT_THRESHOLD_ADD": "[fold·scope ailment,threshold] 异常门槛加性（归一属性，所有异常共有）",
    "AILMENT_THRESHOLD_ADAPTATION": "[核心] 异常门槛适应倍率；StatusEngine:470 读取",
    "AMOUNT_LEECHED": "[fold·scope resource] 偷取量倍率（按资源作用域）",
    "AVOID_AILMENT_CHANCE": "[fold·scope ailment] 异常回避几率",
    "BLOCK_CHANCE": "[fold·scope block] 格挡几率（max 0.75）",
    "CAST_SPEED": "[核心] 施法速度倍率（与 ATTACK_SPEED 正交）",
    "ARMOR_BREAK": "[fold] 护甲破坏倍率",
    "CROSS_BOW_RELOAD_TIME": "[fold·scope crossbow] 弩装填时间倍率",
    "CRIT_CHANCE": "[fold·scope crit] 暴击几率",
    "CRIT_DAMAGE": "[fold·scope crit] 暴击伤害倍率",
    "DAMAGE": "[fold·scope] 伤害倍率（归一属性，按 scope 区分 attack/spell/...）",
    "DAMAGE_OVER_TIME_MULTIPLIER": "[fold] 持续伤害倍率",
    "DEXTERITY": "[零基·禁inc] 敏捷（属性点）；base=0，仅 valueMod/pctAdd 安全",
    "ENERGY_RECHARGE": "[fold] 能量（施法资源）回复速率倍率",
    "MANA_TAKE_OF_DAMAGE": "[fold·scope] 受伤转魔力比例",
    "EVASION": "[核心] 闪避值",
    "INTELLIGENCE": "[零基·禁inc] 智力（属性点）；StatGen 以 inc(INC_INTELLIGENCE_2) 接 base=0 静默死，须改 pctAdd",
    "LAUNCH_ADDITIONAL_PROJECTILE": "[fold·scope projectile] 额外投射物数量",
    "LEECH": "[fold·scope resource] 偷取率（按资源作用域）",
    "LEECH_RATE": "[核心] 偷取速率倍率（归一）；DamageHelper:295 读取",
    "LEECHES_RESPONSE": "[fold·scope resource] 偷取响应（按资源作用域）",
    "MAX_ENDURANCE_CHARGES": "[核心] 最大耐力球数",
    "META_ENERGY_MAX": "[核心] 元技能能量上限（base 100）",
    "MAX_ENERGY_SHIELD": "[核心] 最大能量护盾（ES）；StateCalculator 读取，承伤吸收层消费",
    "MAX_FRENZY_CHARGES": "[核心] 最大狂怒球",
    "MAX_MANA": "[核心] 最大魔力",
    "MAX_POWER_CHARGES": "[核心] 最大能量球",
    "MAX_RAGE": "[核心] 最大怒气",
    "RAGE_LOSS_RATE": "[核心] 怒气固有流失速率倍率；RageHelper 读取",
    "RAGE_LOSS_GRACE": "[核心] 怒气固有流失宽限期（tick）；RageHelper 读取",
    "MAX_REGENERATION": "[fold] 最大再生率",
    "MAX_RESISTANCES": "[核心] 最大抗性上限（0.75）",
    "MAX_SPIRIT": "[核心] 最大精魂",
    "PARRY_RANGE": "[fold·scope parry] 招架范围倍率",
    "PRESENCE_RANGE": "[核心·scope presence] 光环/威势范围；EntityOperationEvent 读取",
    "PROJECTILE_PIERCE": "[fold·scope projectile] 投射物穿透次数",
    "PROJECTILE_PIERCE_CHANCE": "[fold·scope projectile] 投射物穿透几率",
    "PROJECTILE_RANDOM_FIRE": "[fold·scope projectile] 投射物随机发射（布尔）",
    "PROJECTILE_SPEED": "[fold·scope projectile] 投射物速度倍率",
    "RECOVERY_RATE": "[fold] 跨来源恢复率倍率（Regen/Leech/Flask/Recoup 同乘）",
    "FLASK_RECOVERY_RATE": "[fold·scope flask] Flask 回复量倍率",
    "FLASK_CHARGES_GAINED": "[fold·scope flask] Flask 充能获得量倍率",
    "FLASK_NO_CONSUME_CHANCE": "[fold·scope flask] Flask 不消耗充能几率",
    "CHARM_CHARGES_GAINED": "[fold·scope charm] 护符充能获得量倍率",
    "CHARM_ACTIVATION_CHANCE": "[fold·scope charm] 护符连锁触发几率",
    "CHARM_CHARGES_USED": "[fold·scope charm] 护符充能消耗倍率（<1 减耗）",
    "CHARM_DURATION": "[fold·scope charm] 护符效果时长倍率",
    "CHARM_EFFECT": "[fold·scope charm] 护符效果强度倍率",
    "AURA_MAGNITUDE": "[fold·scope aura] 光环幅度倍率（归一属性）",
    "MAX_DARKNESS": "[核心·scope darkness] 最大黑暗值；DarknessHelper 读取",
    "BASE_ENERGY_SHIELD_RECHARGE": "[核心] ES 基础回复倍率；EnergyShieldHelper 读取",
    "CURRENT_ENERGY_SHIELD_RECHARGE": "[核心] ES 当前回复倍率；EnergyShieldHelper 读取",
    "DAMAGE_TAKEN_BYPASSES_ENERGY": "[fold] 承伤绕过 ES 比例",
    "DARKNESS_RESERVATION_LASTS": "[核心·scope darkness] 黑暗保留持续；DarknessHelper 读取",
    "DEFEND_ARMOUR": "[数据先行·待核] 护甲招架几率；当前无消费者（{crit} 作用域）",
    "ELEMENTAL_DAMAGE_CONTRIBUTES_BLEEDING": "[fold·scope bleeding] 元素伤害贡献流血比例",
    "GLORY": "[核心] 荣耀资源（ResourceType.GLORY）",
    "NO_INHERENT_MANA_REGENERATION": "[fold] 禁用固有魔力再生",
    "NO_SPIRIT_BASE_DARKNESS": "[核心·scope darkness] 无精魂黑暗基底；DarknessHelper/ManaHelper 读取",
    "REGENERATE_MANA_EQUAL_LIFE": "[fold] 魔力再生等于生命比例",
    "REMNANTS_COLLECTED_FURTHER_AWAY": "[fold·scope remnant] 残迹收集距离倍率",
    "TAKE_NO_DAMAGE_VOLATILITY": "[核心·scope volatility] 波动免伤；VolatilityDetonationManager 读取",
    "DEFLECTION_RATING": "[fold·scope deflect] 招架判定强度（格挡判定读取）",
    "DODGE_CHANCE": "[fold·scope dodge] 闪避几率（格挡判定读取）",
    "CORRUPTION_ENDURES_CHANCE": "[核心] 免死几率（corruption_endures keystone）；DamagePipeline:552 读取",
    "GUARD_RECOVERY": "[fold·scope guard] Guard 护盾回复量",
    "ARCHON_RECOVERY_RATE": "[fold·scope archon] Archon 恢复速率倍率",
    "GLORY_GENERATION": "[悬空·待裁定] 荣耀生成速率；当前无消费端（注释称 Banner 读取不实），待接 fire 通道或删",
    "MAX_VOLATILITY": "[核心·scope volatility] 波动上限；VolatilityHelper 读取",
    "VOLATILITY_GENERATION": "[fold·scope volatility] 波动获取速率倍率",
    "VOLATILITY_DETONATION_TIME": "[fold·scope volatility] 波动爆炸延迟（秒）",
    "MAX_INFUSION": "[核心·scope infusion] 元素灌入层数上限；InfusionHelper 读取",
    "RESERVATION_EFFICIENCY": "[fold·scope aura] 保留效率（降低精魂保留量）",
    "THORNS": "[fold] 荆棘反伤倍率（受击方）",
    "STUN_RECOVERY": "[fold·scope stun] 眩晕恢复速率倍率",
    "KNOCKBACK": "[fold] 击退距离倍率",
    "CULLING_STRIKE_THRESHOLD": "[fold] 斩杀阈值倍率",
    "REGENERATION_RATE": "[fold] 再生速率倍率",
    "RESISTANCES": "[fold·scope element] 抗性（归一属性，按元素作用域读取）",
    "RESISTANCE_PENETRATION": "[fold·scope element] 抗性穿透（攻击方，全局乘数）",
    "STATUS_EXTRA_STACKS": "[fold·scope status] 状态额外层数",
    "STATUS_MAX_STACKS": "[fold·scope status] 状态最大层数",
    "REMNANT_EXTRA_CHANCE": "[核心·scope remnant] 额外残迹几率；RemnantState 读取",
    "REMNANT_DOUBLE_CHANCE": "[核心·scope remnant] 残迹双倍几率；RemnantState:53 读取",
    "REMNANT_MAGNITUDE": "[核心·scope remnant] 残迹幅度；RemnantState:54 读取",
    "AFTERSHOCK_CHANCE": "[fold·scope slam] 余震几率（归一 {slam}/{mace,slam}/{form,slam}）；AreaDeliveryExecutor 读取",
    "AFTERSHOCK_AREA": "[fold·scope slam] 余震范围效果倍率",
    "PUPPET_MASTER_CHANCE": "[核心·待归一] 傀儡师获得层数几率；SkillEvent 读取（应走 fire 通道 grant_on_event）",
    "PUPPET_MASTER_EFFECT": "[fold·scope puppet] 傀儡师效果强度（零基累积）",
    "PUPPET_MASTER_DURATION": "[fold·scope puppet] 傀儡师持续（零基累积）",
    "STRENGTH": "[零基·禁inc] 力量（属性点）；base=0，仅 valueMod/pctAdd 安全",
    "TAKEN_CRIT_DAMAGE": "[fold·scope taken,crit] 承暴击伤害倍率",
    "TAKEN_CRIT_CHANCE": "[fold·scope taken,crit] 被暴击几率（受害者侧）",
    "TAKEN_DAMAGE": "[fold·scope taken] 承伤倍率",
    "TOTEM_TAKEN_AURA": "[fold·scope totem,taken] 图腾承伤光环（攻击方读取附近图腾数）",
    "ADDED_DAMAGE": "[零基·禁inc] 附加伤害（按元素 valueMod 接）；base=0，禁 inc",
}

HEADER = '''    /**
     * ===== 属性注册层（scope 体系 + 三定律 + 图例）=====
     *
     * 【读取机制】getAttributeValue / readInScopePublic → LivingAttributeValue mixin →
     * StatEngine.fold 按当前作用域（ExpressionContext 栈顶 StatContext）即时聚合 Stat 贡献。
     * 作用域是「栈」：同一时刻仅栈顶生效；嵌套（引导→命中）内层 merge 外层 tags 入栈，避免遮蔽。
     * 取特定场景值 = 先 StatScopes.enter(实体, 对应标签作用域) 再读，退出后版本失效回落。
     *
     * 【三定律】（属性层归一纪律，逐条封死轮回）
     * 1. scope 通道：StatGen 带 scope 标签的条目，消费端必须 StatScopes.enter(scope) 读取，
     *    否则 EMPTY 下恒不命中 → 读注册默认值（等价绕过 Stat）。
     * 2. 触发型走 fire：一次性资源/状态/伤害/几率授予必须走 StatEffect.fire，
     *    禁「裸属性 + 硬编码消费端」（本类越界见 GLORY_GENERATION / PUPPET_MASTER_CHANCE / REMNANT_*）。
     * 3. 零基禁 inc：base=0 属性只能 pctAdd(ADD_VALUE)/ADD_MULTIPLIED_TOTAL，绝不可 inc
     *    （fold 式 ADD_MULTIPLY_BASE ≡ base*ΣmultBase ≡ 0 静默死，见 [零基] 标注属性）。
     *
     * 【图例】每行备注格式： [tag] 用途 / 作用域 / 状态
     *   [核心]   战斗/系统核心属性，消费端直接读取（Helper / getAttributeValue）
     *   [fold]   读时折叠数值，StatGen 经 ModifierStatEffect 接线，scope fold 聚合
     *   [legacy] 原版 AttributeModifier 直挂（合法，非 Stat 数据驱动）
     *   [reserved] 预留/规划，暂无消费端
     *   [待删]   过时设计，待词缀 Stat 化后移除（勿新接）
     *   [数据先行] 数据已落地，运行时消费端待接（reserved-empty，不删常量）
     *   [悬空]   当前无消费端（注释声称的读取不实），待裁定
     *   [零基]   base=0，仅可 pctAdd/ADD_VALUE，禁 inc
     *   [scope X] 须 StatScopes.enter(X) 后读取，否则 EMPTY 下恒读默认
     *
     * 拆分维度：事件（hit/kill/crit…）与资源（health/mana/energy…）正交；资源按量/基数/指定倍率拆，
     * 全局乘数归一单属性；承受侧统一走 *TAKEN* + {taken} 作用域，不建独立 _TAKEN 属性。
     */'''

DIVIDER_RE = re.compile(r"─────|=====")
JDOC_OPEN = re.compile(r"/\*\*")
JDOC_CLOSE = re.compile(r"\*/")
ATTR_RE = re.compile(r"^\s*public static Holder<Attribute>\s+(\w+)\s*=\s*register\w+\(")


def is_divider(line):
    s = line.strip()
    return s.startswith("//") and DIVIDER_RE.search(s)


def is_comment_line(line):
    s = line.strip()
    return s.startswith("//") or s.startswith("*") or s.startswith("/**") or s.endswith("*/") or s == ""


def main():
    apply = "--apply" in sys.argv
    with open(SRC, encoding="utf-8") as fh:
        lines = fh.readlines()

    # 1) 定位 class 级 javadoc（public class 后的第一个 /** ... */）并替换
    out = []
    i = 0
    n = len(lines)
    replaced_header = False
    while i < n:
        if not replaced_header and "public class AttributeRegister" in lines[i]:
            out.append(lines[i])  # class 行原样
            i += 1
            # 跳到下一个 /** 起点
            while i < n and not JDOC_OPEN.search(lines[i]):
                out.append(lines[i]); i += 1
            # 消费整个 javadoc 块到 */
            while i < n and not JDOC_CLOSE.search(lines[i]):
                i += 1
            if i < n:
                i += 1  # 跳过 */
            out.append(HEADER + "\n")
            replaced_header = True
            continue
        out.append(lines[i])
        i += 1

    # 2) 处理属性声明：替换其上方紧邻注释块为新单行备注
    # 重新扫描 out，找出属性行，向上吞掉注释块（遇 divider / 非注释 停止）
    final = []
    attr_indices = [k for k, ln in enumerate(out) if ATTR_RE.match(ln)]
    # 计算每行是否属性行
    is_attr = [False] * len(out)
    for k in attr_indices:
        is_attr[k] = True

    skip = set()
    for k in attr_indices:
        # 向上吞注释
        j = k - 1
        while j >= 0:
            ln = out[j]
            if is_divider(ln):
                break
            if is_comment_line(ln):
                skip.add(j)
                j -= 1
            else:
                break
    for idx, ln in enumerate(out):
        if idx in skip:
            continue
        if is_attr[idx]:
            name = ATTR_RE.match(ln).group(1)
            note = NOTES.get(name)
            if note is None:
                note = "[未分类] TODO：补备注"
            final.append("    /** " + note + " */\n")
        final.append(ln)

    text = "".join(final)
    if not apply:
        sys.stdout.write(text)
        return
    # 备份 + 写回
    bak = SRC + ".pre_annotate"
    if not os.path.exists(bak):
        shutil.copy(SRC, bak)
    with open(SRC, "w", encoding="utf-8") as fh:
        fh.write(text)
    # 统计
    missing = [nm for nm in NOTES if nm not in {ATTR_RE.match(l).group(1) for l in final if ATTR_RE.match(l)}]
    print("written. attributes annotated:", len(attr_indices), "notes provided:", len(NOTES),
          "missing-in-file:", missing)


if __name__ == "__main__":
    main()
