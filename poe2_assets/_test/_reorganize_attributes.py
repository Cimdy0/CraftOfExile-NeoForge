#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
按类重排 AttributeRegister.java（只读解析 + 重排生成，不改语义）。

- 解析每个字段及其原有 /** ... */ 注解（原样保留描述文本）。
- 按语义域分类（资源上限/资源机制/属性点/异常维度/效果维度/核心战斗/承伤侧/
  抗性状态/偷取回复/投射物/天赋预留/数据先行），section banner 同时给出
  归一指引 + 相关原版 Attributes.* 备注。
- 字段声明（registerXxx(...) 参数）逐字保留，仅重排顺序。
- 生成 AttributeRegister.java.new 供人工审阅，确认无误后再覆盖原文件。
"""
import re, json

SRC = "E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java/com/cimdy/coe/data/AttributeRegister.java"
OUT = "E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java/com/cimdy/coe/data/AttributeRegister.java.new"

lines = open(SRC, encoding="utf-8").read().split("\n")

# ---- 1) 解析 head（第1行到首个字段之前）与 tail（AUTO-GEN 标记到 EOF） ----
field_re = re.compile(r"^\s*public static Holder<Attribute>\s+(\w+)\s*=\s*(register\w+\(.*\));\s*$")
first_field_idx = None
for i, ln in enumerate(lines):
    if field_re.match(ln):
        first_field_idx = i
        break
# tail 起点：AUTO-GEN 标记行
tail_idx = None
for i, ln in enumerate(lines):
    if "AUTO-GEN" in ln and "END" in ln:
        tail_idx = i
        break

head = lines[:first_field_idx]          # 含 package/import/class/head-comment/ATTRIBUTES/ALL_ATTRIBUTE
tail = lines[tail_idx:]                   # AUTO-GEN END 标记 + register* 方法 + 类尾

# ---- 2) 解析字段块（注解 + 声明） ----
fields = []  # (name, decl, ann_raw)
i = first_field_idx
while i < tail_idx:
    ln = lines[i]
    m = field_re.match(ln)
    if m:
        name = m.group(1)
        decl = "    public static Holder<Attribute> %s = %s;" % (name, m.group(2))
        # 向前找注解：紧邻上方 /** ... */（单行）
        ann = ""
        j = i - 1
        while j >= first_field_idx and lines[j].strip() == "":
            j -= 1
        if j >= first_field_idx and lines[j].strip().startswith("/**"):
            ann = lines[j].strip()
        fields.append((name, decl, ann))
    i += 1

# ---- 3) 分类（语义域）。未归类进 ZZ，脚本会告警。 ----
SECTIONS = [
    ("A", "原版兼容直挂 [legacy]",
     "原版 AttributeModifier 直挂（合法，非 Stat 数据驱动）；由 EntityAttributeModificationEvent 挂载全体。无 CoE Stat 折叠。",
     "原版对应：无（CoE 自管 work_speed/work_aptitude，不经 StatEngine）。",
     ["WORK_SPEED", "WORK_APTITUDE"]),

    ("B", "资源上限 [Resource Caps · DO_NOT_MERGE]",
     "各资源/球数独立上限池；归一会破坏独立资源池，禁止并入通用属性。",
     "原版对应：MAX_HEALTH / MAX_MANA / MAX_ENERGY_SHIELD(NeoForge) 由 StatGen 直接挂 vanilla Attributes.*；其余（rage/spirit/darkness/volatility/infusion/charges/resistances/regen）为 CoE 扩展上限。",
     ["MAX_MANA", "MAX_ENERGY_SHIELD", "MAX_RAGE", "MAX_SPIRIT", "MAX_DARKNESS",
      "MAX_VOLATILITY", "MAX_INFUSION", "MAX_REGENERATION", "MAX_RESISTANCES",
      "MAX_ENDURANCE_CHARGES", "MAX_FRENZY_CHARGES", "MAX_POWER_CHARGES", "META_ENERGY_MAX"]),

    ("C", "资源机制 [Resource Mechanics]",
     "资源生成/流失/回复/保留/基底；多为直读或 fold，独立机制不并入通用维度。",
     "原版对应：MOVEMENT_SPEED / MAX_HEALTH / MAX_MANA / MAX_ENERGY_SHIELD 由 StatGen 直接挂 vanilla；glory/volatility/darkness/reservation 为 CoE 资源层扩展。",
     ["GLORY", "GLORY_GENERATION", "RAGE_LOSS_RATE", "RAGE_LOSS_GRACE", "ENERGY_RECHARGE",
      "BASE_ENERGY_SHIELD_RECHARGE", "CURRENT_ENERGY_SHIELD_RECHARGE", "DARKNESS_RESERVATION_LASTS",
      "NO_SPIRIT_BASE_DARKNESS", "REGENERATE_MANA_EQUAL_LIFE", "NO_INHERENT_MANA_REGENERATION",
      "VOLATILITY_GENERATION", "VOLATILITY_DETONATION_TIME", "RESERVATION_EFFICIENCY"]),

    ("D", "属性点 [Attribute Points · 零基]",
     "力量/敏捷/智力属性点；base=0 仅可 valueMod/pctAdd，禁 inc。仅词缀需求判定用（AffixGenerator）。",
     "原版对应：无（原版无属性点体系，CoE 扩展）。",
     ["STRENGTH", "DEXTERITY", "INTELLIGENCE"]),

    ("E", "异常维度 [Ailment · 归一重点]",
     "异常 magnitude/duration/chance/buildup/rate/contribution/threshold 族——按异常名作用域 {ailment, X} 折叠的通用维度。AILMENT_MAGNITUDE/DURATION 已承载 28+ 作用域（含 {AURA}/{CHARM}），新增异常维度优先并入此处而非建独立属性。",
     "原版对应：无（异常体系为 CoE 扩展，原版仅有 ignite/poison 等硬编码，无统一维度属性）。",
     ["AILMENT_MAGNITUDE", "AILMENT_DURATION", "AILMENT_CHANCE", "AILMENT_BUILDUP",
      "AILMENT_DAMAGE_RATE", "AILMENT_DAMAGE_CONTRIBUTION", "AILMENT_THRESHOLD",
      "AILMENT_THRESHOLD_ADD", "AILMENT_THRESHOLD_ADAPTATION"]),

    ("F", "效果维度 [Effect: aura/charm/remnant/puppet/aftershock · 归一候选]",
     "光环/护符/残迹/傀儡/余震效果维度。注意：AURA_MAGNITUDE 为 AILMENT_MAGNITUDE+{AURA} 的重复孤儿（待合并）；CHARM_DURATION 为死属性（真实接线在 AILMENT_DURATION+{CHARM}，待合并）；CHARM_EFFECT/REMNANT_MAGNITUDE 为独立维度保留。",
     "原版对应：无（效果维度为 CoE 扩展）。",
     ["AURA_MAGNITUDE", "CHARM_DURATION", "CHARM_EFFECT", "CHARM_CHARGES_GAINED",
      "CHARM_ACTIVATION_CHANCE", "CHARM_CHARGES_USED", "REMNANT_MAGNITUDE",
      "REMNANT_EXTRA_CHANCE", "REMNANT_DOUBLE_CHANCE", "REMNANTS_COLLECTED_FURTHER_AWAY",
      "PUPPET_MASTER_CHANCE", "PUPPET_MASTER_EFFECT", "PUPPET_MASTER_DURATION",
      "AFTERSHOCK_CHANCE", "AFTERSHOCK_AREA", "THORNS", "GUARD_RECOVERY", "ARCHON_RECOVERY_RATE"]),

    ("G", "核心战斗 [Core Combat · 直读/归一伤害]",
     "伤害/命中/格挡/暴击/攻速/范围等核心战斗只读或 fold 属性。DAMAGE 为全局伤害倍率（按 {attack}/{spell} 等作用域区分）；ARMOR 名对应原版 ARMOR 但由 CoE 体系扩展。",
     "原版对应：ATTACK_DAMAGE（伤害，原版仅近战，CoE DAMAGE 自管全局）、ATTACK_SPEED（攻速，CoE CAST_SPEED 经 Mixin 复用攻击间隔机制）、ARMOR、BLOCK_INTERACTION_RANGE（原版交互范围）。",
     ["DAMAGE", "DAMAGE_OVER_TIME_MULTIPLIER", "ADDED_DAMAGE", "ACCURACY", "EVASION",
      "ARMOR_BREAK", "BLOCK_CHANCE", "DEFLECTION_RATING", "DODGE_CHANCE", "PARRY_RANGE",
      "CRIT_CHANCE", "CRIT_DAMAGE", "CAST_SPEED", "CROSS_BOW_RELOAD_TIME", "KNOCKBACK",
      "STUN_RECOVERY", "AREA_OF_EFFECT", "COOLDOWN_RECOVERY_RATE", "DEFEND_ARMOUR",
      "ELEMENTAL_DAMAGE_CONTRIBUTES_BLEEDING", "CULLING_STRIKE_THRESHOLD"]),

    ("H", "承伤侧 [Taken]",
     "受害者侧承伤/承暴击倍率与几率；统一走 *TAKEN* + {taken} 作用域，不建独立 _TAKEN 属性。",
     "原版对应：无（承伤侧为 CoE Stat 折叠扩展）。",
     ["TAKEN_DAMAGE", "TAKEN_CRIT_DAMAGE", "TAKEN_CRIT_CHANCE", "TOTEM_TAKEN_AURA",
      "DAMAGE_TAKEN_BYPASSES_ENERGY", "TAKE_NO_DAMAGE_VOLATILITY"]),

    ("I", "抗性与状态 [Resist / Status]",
     "元素抗性（按 {element} 作用域归一）/抗性穿透/状态层数。MAX_RESISTANCES 为上限池（见 §B）。",
     "原版对应：无统一抗性属性（原版抗性由各护甲/附魔隐含）；CoE 以 RESISTANCES 归一各元素。",
      ["RESISTANCES", "RESISTANCE_PENETRATION", "STATUS_EXTRA_STACKS", "STATUS_MAX_STACKS",
      "AVOID_AILMENT_CHANCE"]),

    ("J", "偷取与回复 [Leech / Recovery]",
     "偷取量/率/响应、跨来源回复率、Flask 回复/充能、魔力转化、再生速率。",
     "原版对应：无（回复/偷取体系为 CoE 扩展）。",
      ["LEECH", "LEECH_RATE", "LEECHES_RESPONSE", "AMOUNT_LEECHED", "RECOVERY_RATE",
      "FLASK_RECOVERY_RATE", "FLASK_CHARGES_GAINED", "FLASK_NO_CONSUME_CHANCE",
      "MANA_TAKE_OF_DAMAGE", "REGENERATION_RATE"]),

    ("K", "投射物 [Projectile]",
     "额外投射物/穿透次数/穿透几率/速度/随机发射；统一 {projectile} 作用域。",
     "原版对应：无（原版投射物无对应属性，CoE 扩展）。",
     ["LAUNCH_ADDITIONAL_PROJECTILE", "PROJECTILE_PIERCE", "PROJECTILE_PIERCE_CHANCE",
      "PROJECTILE_RANDOM_FIRE", "PROJECTILE_SPEED"]),

    ("L", "天赋预留 [Talent Reserved]",
     "天赋树预留属性，当前无消费端；待天赋节点接入或移除。",
     "原版对应：无。",
     ["TALENT_AUTO_SMELT", "TALENT_FORTUNE", "TALENT_LOOTING", "TALENT_MENDING",
      "TALENT_EXTRA_XP", "TALENT_MAGNET"]),

    ("M", "数据先行/悬空/待删 [Pending]",
     "数据先行（运行时消费端待接）、悬空（注释称读取不实）、待归一（应走 fire 通道）。按 R2 保留，列入跟踪，不删。",
     "原版对应：无。",
     ["BANNER_LINGER", "RECENTLY_CONSUMED_DAMAGE", "CORPSE_RETAIN_CHANCE", "ADD_EMPOWERED_ATTACKS",
      "FOUND_ITEM_RARITY", "FOUND_ITEM_NUMBER", "ITEM_CAPACITY", "SKILL_SLOT_COUNT",
      "CORRUPTION_ENDURES_CHANCE", "PRESENCE_RANGE"]),
]

# 建立 name->section 映射并打印未归类
sec_of = {}
for s in SECTIONS:
    for n in s[4]:
        sec_of[n] = s[0]
parsed_names = [f[0] for f in fields]
unclassified = [n for n in parsed_names if n not in sec_of]
assert not unclassified, "UNCLASSIFIED: %s" % unclassified
assert len(parsed_names) == len(set(parsed_names)), "DUPLICATE FIELD NAMES"

# 头注释：增强为「注册 + 归一指南」双角色说明（保留三定律与图例，追加分类索引）
NEW_HEAD = '''package com.cimdy.coe.data;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.common.BooleanAttribute;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class AttributeRegister {
    /**
     * ===== 属性注册层（scope 体系 + 三定律 + 图例）=====
     * 本类双重角色：① 属性注册表（DeferredRegister 注册 122 个 CoE 自有属性）；
     *               ② 归一指南（按语义域分组 + section banner 标注归一规则与原版对应）。
     * 重排仅调整字段顺序与注解，注册键字符串（registerXxx 的第一个参数）不变，无行为影响。
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
     * 【分类索引（归一指南）】字段按下述语义域分组，相邻即同维度，便于判定归一：
     *   §A 原版兼容直挂 · §B 资源上限(DO_NOT_MERGE) · §C 资源机制 · §D 属性点(零基)
     *   §E 异常维度(归一重点) · §F 效果维度(归一候选) · §G 核心战斗 · §H 承伤侧
     *   §I 抗性与状态 · §J 偷取与回复 · §K 投射物 · §L 天赋预留 · §M 数据先行/悬空/待删
     * 拆分维度：事件（hit/kill/crit…）与资源（health/mana/energy…）正交；资源按量/基数/指定倍率拆，
     * 全局乘数归一单属性；承受侧统一走 *TAKEN* + {taken} 作用域，不建独立 _TAKEN 属性。
     */
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, CraftOfExile.MODID);
    public static List<Holder<Attribute>> ALL_ATTRIBUTE = new ArrayList<>();'''

# ---- 4) 生成 body ----
out_parts = [NEW_HEAD, ""]
for key, title, rule, vanilla, members in SECTIONS:
    out_parts.append("    // ===================== §%s %s =====================" % (key, title))
    out_parts.append("    // 归一指引：%s" % rule)
    out_parts.append("    // %s" % vanilla)
    out_parts.append("")
    # 组内按名字排序，稳定且易查
    members_sorted = sorted(members)
    fmap = {f[0]: f for f in fields}
    for name in members_sorted:
        ann, decl = fmap[name][2], fmap[name][1]
        if ann:
            out_parts.append("    " + ann)
        out_parts.append(decl)
        out_parts.append("")
    out_parts.append("")

body = "\n".join(out_parts)
new_content = body.rstrip("\n") + "\n" + "\n".join(tail)

open(OUT, "w", encoding="utf-8").write(new_content)
print("fields parsed:", len(fields))
print("sections:", len(SECTIONS))
print("written ->", OUT)
