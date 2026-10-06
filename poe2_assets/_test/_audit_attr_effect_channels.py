#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
属性 Stat 通道 × 引擎 Effect 类型 交叉比对（只读 .java，不改动代码）。

目标：对用户关心的候选属性（Q3 magnitude/duration 族 + 两个真冗余项），
抽取它们在 StatGen 中的全部条目通道，判定每条通道使用的 Effect 类型（FOLD vs 各直接通道），
并对照引擎已有 StatEffect 实现类，界定「归一化属性」该修的范围：
  - 仅 FOLD(ModifierStatEffect) 读时聚合 → 可安全 scope 折叠归一（重定向属性引用）
  - 含 FIRE/转换/免疫/掷骰/光环等直接通道 → 超出纯属性归一，需单独裁定

用法：python _audit_attr_effect_channels.py
"""
import json
import re

MOD = "E:/McModding/Modding/CraftOfExile-NeoForge"
ST_GEN = f"{MOD}/src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
EFF_DIR = f"{MOD}/src/main/java/com/cimdy/coe/data/codec/support/stat/effect"

# 候选属性（来自 ATTRIBUTE_SEMANTIC_SCOPE_AUDIT.md §3/§5）
CANDIDATES = [
    "AILMENT_MAGNITUDE", "AURA_MAGNITUDE", "REMNANT_MAGNITUDE",   # Q3 magnitude 族
    "AILMENT_DURATION",   "CHARM_DURATION",                       # Q3 duration 族
    "CHARM_EFFECT",                                             # 边界项：报告判定「非冗余」的独立 effect 维度属性
]

# helper 方法 → (Effect 类型, EffectKind)
HELPER_MAP = {
    "inc":           ("ModifierStatEffect", "FOLD"),
    "pctAdd":        ("ModifierStatEffect", "FOLD"),
    "more":          ("ModifierStatEffect", "FOLD"),
    "add":           ("ModifierStatEffect", "FOLD"),
    "chance":        ("ModifierStatEffect", "FOLD"),
    "valueMod":      ("ModifierStatEffect", "FOLD"),
    "entityMod":     ("ModifierStatEffect", "FOLD"),
    "regenMod":      ("ModifierStatEffect", "FOLD"),
    "conditionMod":  ("ModifierStatEffect", "FOLD"),
    "recoup":        ("RecoupStatEffect",   "FIRE"),
}

# 直接 map.put(new XxxStatEffect(...)) 中的效果类型 → EffectKind（按 StatEffect.java EffectKind 枚举）
# 注：ChanceOnEventStatEffect 虽 extends AbstractStatEffect，但其文件名不含 "StatEffect" 后缀，
#     故用精确集合匹配；StatTargets/TargetSelector 是选择器/目标类，非 StatEffect 子类，排除。
DIRECT_KIND = {
    "ModifierStatEffect":              "FOLD",
    "StatusStatEffect":                "FIRE",
    "ResourceStatEffect":              "FIRE",
    "ResourceConvertStatEffect":       "RESOURCE_CONVERT",
    "RequirementStatEffect":           "INERT",
    "RecoupStatEffect":                "FIRE",
    "RandomTakenConvertStatEffect":    "DAMAGE_TAKEN_CONVERT",
    "RandomConvertStatEffect":         "DAMAGE_CONVERT",
    "PresenceAuraStatEffect":          "AURA",
    "LuckyStatEffect":                 "LUCKY",
    "LeechStatEffect":                 "FIRE",
    "ImmuneStatEffect":                "IMMUNE",
    "HitFlagStatEffect":               "HIT_FLAG",
    "GrantOnEventStatEffect":          "FIRE",
    "DeliveryStatEffect":              "DELIVERY",
    "DamageTakenConvertStatEffect":     "DAMAGE_TAKEN_CONVERT",
    "DamageStatEffect":                "FIRE",
    "DamageConvertStatEffect":         "DAMAGE_CONVERT",
    "CreateGroundOnAilmentEffect":     "FIRE",
    "ChargeConvertStatEffect":         "FIRE",
    "ChanceOnEventStatEffect":         "FIRE",
}


def enumerate_effect_types():
    """扫描 effect 目录，列出所有 StatEffect 实现类 + 其 EffectKind（从源码注释/类名推断）。"""
    import os, glob
    kinds = {}
    for f in glob.glob(f"{EFF_DIR}/*.java"):
        base = os.path.basename(f)[:-5]
        if base in ("StatEffect", "AbstractStatEffect", "StatEffectTypes"):
            continue
        kinds[base] = DIRECT_KIND.get(base, "?")
    return kinds


def parse_statgen():
    """逐行扫描 StatGen.java，抽取所有条目构造，按目标属性归类。"""
    lines = open(ST_GEN, encoding="utf-8").read().splitlines()
    # 多行条目：合并到分号结束
    merged = []
    buf = ""
    for ln in lines:
        buf = (buf + " " + ln.strip()) if buf else ln.strip()
        if buf.rstrip().endswith(";"):
            merged.append(buf)
            buf = ""
    if buf:
        merged.append(buf)

    # 候选属性 → 条目列表
    by_attr = {a: [] for a in CANDIDATES}

    # helper 模式：Helper(map, StatIds.ID, AttributeRegister.ATTR, tags...)
    hpat = re.compile(
        r"\b(inc|pctAdd|more|add|chance|valueMod|entityMod|regenMod|conditionMod|recoup)\s*\(\s*map\s*,\s*StatIds\.(\w+)\s*,\s*AttributeRegister\.(\w+)")
    # 直接效果模式：map.put(StatIds.ID, new XxxStatEffect(... AttributeRegister.ATTR ...))
    dpat = re.compile(r"map\.put\(\s*StatIds\.(\w+)\s*,\s*new\s+(\w+StatEffect)\(")

    for stmt in merged:
        m = hpat.search(stmt)
        if m:
            helper, sid, attr = m.group(1), m.group(2), m.group(3)
            if attr in by_attr:
                etype, kind = HELPER_MAP[helper]
                tags = extract_tags(stmt)
                by_attr[attr].append({
                    "stat_id": sid, "helper": helper,
                    "effect": etype, "kind": kind, "tags": tags,
                })
            continue
        m = dpat.search(stmt)
        if m:
            sid, etype = m.group(1), m.group(2)
            # 在合并语句内找 AttributeRegister.ATTR
            am = re.search(r"AttributeRegister\.(\w+)", stmt)
            attr = am.group(1) if am else None
            if attr in by_attr:
                kind = DIRECT_KIND.get(etype, "?")
                tags = extract_tags(stmt)
                by_attr[attr].append({
                    "stat_id": sid, "helper": "map.put(new)",
                    "effect": etype, "kind": kind, "tags": tags,
                })
    return by_attr


def extract_tags(stmt):
    """抽取语句内 StatTag.X / not(StatTag.Y) 标签集合。"""
    tags = []
    for t in re.findall(r"StatTag\.(\w+)", stmt):
        if t in ("not",):
            continue
        tags.append(t)
    # not(...) 标记
    nots = re.findall(r"not\(\s*StatTag\.(\w+)\s*\)", stmt)
    out = []
    for t in tags:
        if t in nots:
            out.append("!" + t)
        else:
            out.append(t)
    # 去重保序
    seen = set(); res = []
    for t in out:
        if t not in seen:
            seen.add(t); res.append(t)
    return res


def main():
    eff_types = enumerate_effect_types()
    by_attr = parse_statgen()

    # 统计每个候选属性的 kind 分布
    summary = {}
    for attr in CANDIDATES:
        entries = by_attr[attr]
        kind_count = {}
        for e in entries:
            kind_count[e["kind"]] = kind_count.get(e["kind"], 0) + 1
        # 该属性在 StatGen 是否被读取端消费（孤儿判定需另外全仓 grep，这里只报通道）
        summary[attr] = {
            "entry_count": len(entries),
            "kinds": kind_count,
            "all_fold": all(e["kind"] == "FOLD" for e in entries) if entries else None,
            "entries": entries,
        }

    out = {
        "engine_effect_types": eff_types,
        "effect_type_count": len(eff_types),
        "candidates": summary,
    }
    print(json.dumps(out, indent=1, ensure_ascii=False))


if __name__ == "__main__":
    main()
