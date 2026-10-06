#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
技能效果「无 Status 化」统一评估检测器
=================================================
背景：标记/捷/奉献/战旗/变形/光环/地面效果/神龛/护符/药剂 等技能效果，曾多次被要求
「无 Status 化」（即数值与机制由 stat/effect 引擎驱动，而非写死在 StatusType 注册中），
但每轮常半接线/未接线便略过，现各家族实现手段不一（部分纯 Status、部分 stat 部分 Status、
部分 stat 已注册却零读点=死线、部分仅停留在 unwired_now.txt）。

本脚本对这 10 个家族做统一交叉检测，输出：
  - STATUS 足迹：该家族注册了哪些 StatusType（id 列表 + 数量）
  - ATTR   足迹：AttributeRegister 中归属该家族的属性（id 列表 + 数量）
  - WIRE   足迹：StatEffectGen/Wire* 中归属该家族的接线（数量）
  - UNWIRED 足迹：unwired_now.txt 中归属该家族的未接线词条（条数 + 抽样）
  - HARDCODE：家族源文件中 StatusEngine.apply 调用里 magnitude/时长含字面量（#48 同型死线候选）
  - 判定：PURE_STATUS / HYBRID / STAT_DRIVEN / UNWIRED_ONLY

用法：
  python scripts/skill_effect_status_audit.py            # 汇总表
  python scripts/skill_effect_status_audit.py --detail  # 含每条 id / unwired 抽样
"""
import os, re, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe")
ATTR_FILE = os.path.join(SRC, "data", "AttributeRegister.java")
GEN_FILE = os.path.join(SRC, "data", "gen", "stat", "StatEffectGen.java")
WIRE_DIR = os.path.join(SRC, "data", "gen", "stat", "wirings")
UNWIRED = os.path.join(ROOT, "unwired_now.txt")

# 家族定义：status 正则（匹配 StatusType id）、attr 正则、wire 正则、unwired 正则
FAMILIES = {
    "标记 Mark":        dict(status=r"mark|hazard_mark",   attr=r"mark",           wire=r"mark",           unwired=r"mark"),
    "捷 Herald":        dict(status=r"herald",             attr=r"herald",         wire=r"herald",         unwired=r"herald"),
    "奉献 Offering":    dict(status=r"offering",           attr=r"offering",       wire=r"offering",       unwired=r"offering"),
    "战旗 Banner":      dict(status=r"banner",             attr=r"banner",         wire=r"banner",         unwired=r"banner"),
    "变形 Shapeshift":  dict(status=r"form_|shapeshift",   attr=r"shape",          wire=r"shape",          unwired=r"shapeshift"),
    "光环 Aura":        dict(status=r"aura|presence",      attr=r"aura|presence",  wire=r"aura|presence",  unwired=r"aura"),
    "地面 Ground":      dict(status=r"ground|jagged",      attr=r"ground",         wire=r"ground",         unwired=r"ground"),
    "神龛 Shrine":      dict(status=r"shrine",             attr=r"shrine",         wire=r"shrine",         unwired=r"shrine"),
    "护符 Charm":       dict(status=r"(?!)",               attr=r"charm",          wire=r"charm",          unwired=r"charm"),
    "药剂 Flask":       dict(status=r"(?!)",               attr=r"flask",          wire=r"flask",          unwired=r"flask"),
}

def read(p):
    try:
        return open(p, encoding="utf-8", errors="ignore").read()
    except FileNotFoundError:
        return ""

# ---- 收集全部 StatusType id ----
status_ids = []
for dp, _, fs in os.walk(SRC):
    for f in fs:
        if not f.endswith(".java"):
            continue
        t = read(os.path.join(dp, f))
        # StatusType.register("id", ...) / StatusType.dynamic("id", ...) / registerForm("id")
        for m in re.finditer(r'StatusType\.(?:register|dynamic|registerForm|registerCurse|registerAura|registerShrine|registerHerald|registerMark|registerGround|registerOffering|registerBanner)\s*\(\s*(?:[^,]*,\s*)?["\']([a-z0-9_]+)["\']', t):
            status_ids.append(m.group(1))
        # new StatusType(...) 首参为 id
        for m in re.finditer(r'new StatusType\s*\([^)]*?["\']([a-z0-9_]+)["\']', t):
            status_ids.append(m.group(1))
        # 经 helper 注册（presence("id", ...) / registerForm("id") / registerCurse("id") 等，首参为字面量 id）
        for m in re.finditer(r'\b(?:presence|registerForm|registerCurse|registerAura|registerShrine|registerHerald|registerMark|registerGround|registerOffering|registerBanner)\s*\(\s*["\']([a-z0-9_]+)["\']', t):
            status_ids.append(m.group(1))
status_ids = sorted(set(status_ids))

# ---- AttributeRegister 字段 ----
attr_lines = []
for m in re.finditer(r'public static Holder<Attribute>\s+([A-Z_][A-Z0-9_]*)\s*=\s*register(?:Double|Percentage|Int)?\s*\(\s*["\']([a-z0-9_]+)["\']', read(ATTR_FILE)):
    attr_lines.append((m.group(1), m.group(2)))

# ---- Wiring（StatEffectGen + Wire*）----
wire_text = read(GEN_FILE)
for dp, _, fs in os.walk(WIRE_DIR):
    for f in fs:
        if f.endswith(".java"):
            wire_text += "\n" + read(os.path.join(dp, f))
wire_lines = wire_text.splitlines()

# ---- unwired ----
unwired_lines = [l.rstrip("\n") for l in read(UNWIRED).splitlines() if l.strip()]

def count_regex(lst, pat):
    rx = re.compile(pat, re.I)
    return [x for x in lst if rx.search(x)]

def verdict(status_n, attr_n, wire_n, unwired_n, hard_n):
    if unwired_n and status_n == 0 and attr_n > 0 and wire_n == 0:
        return "UNWIRED_ONLY"
    if status_n == 0 and attr_n > 0 and wire_n > 0:
        return "STAT_DRIVEN"
    if status_n > 0 and (attr_n > 0 or wire_n > 0):
        return "HYBRID"
    if status_n > 0:
        return "PURE_STATUS"
    return "EMPTY"

detail = "--detail" in sys.argv
print(f"{'家族':<16}{'STATUS':>8}{'ATTR':>7}{'WIRE':>7}{'UNWIRED':>9}{'HARD':>6}  判定")
print("-" * 70)
summary = []
for name, kw in FAMILIES.items():
    s_ids = count_regex(status_ids, kw["status"])
    a_ids = count_regex([a[1] for a in attr_lines], kw["attr"])
    a_fields = count_regex([a[0] for a in attr_lines], kw["attr"])
    # 精确 wire：本家族的 AttributeRegister 字段名是否被 StatEffectGen/Wire* 引用
    w_n = sum(1 for fld in a_fields if any(fld in ln for ln in wire_lines))
    u_ids = count_regex(unwired_lines, kw["unwired"])
    v = verdict(len(s_ids), len(a_ids), w_n, len(u_ids), 0)
    summary.append((name, len(s_ids), len(a_ids), w_n, len(u_ids), v))
    print(f"{name:<16}{len(s_ids):>8}{len(a_ids):>7}{w_n:>7}{len(u_ids):>9}{0:>6}  {v}")
    if detail:
        if s_ids: print("    STATUS :", ", ".join(s_ids))
        if a_ids: print("    ATTR   :", ", ".join(a_ids))
        if u_ids:
            for u in u_ids[:6]:
                print("    UNWIRED:", u.split("\t")[0] if "\t" in u else u[:80])

# ---- HARDCODE 检测：全仓 StatusEngine.apply 调用里 magnitude/duration 为字面量（#48 同型死线候选）----
# 判定：apply(..., mag, dur, ...) 中 mag 或 dur 是裸数字（非变量/字段），即「写死数值」。
print("\n=== HARDCODE（StatusEngine.apply 中 magnitude/时长 为字面量，#48 同型死线候选）===")
HARD_PAT = re.compile(r'StatusEngine\.apply[A-Za-z]*\(([^;]*?)\)\s*;')
LIT = re.compile(r'^\s*\d+\.?\d*\s*$')
for dp, _, fs in os.walk(SRC):
    for f in fs:
        if not f.endswith(".java"):
            continue
        rel = os.path.relpath(os.path.join(dp, f), SRC)
        t = read(os.path.join(dp, f))
        for m in HARD_PAT.finditer(t):
            args = m.group(1)
            parts = [p.strip() for p in args.split(",")]
            # 典型重载：apply(tgt, type, src, mag, dur[, stacks])；mag=第4、dur=第5
            lit_mag = len(parts) >= 4 and LIT.match(parts[3])
            lit_dur = len(parts) >= 5 and (LIT.match(parts[4]) or re.search(r'^\d+\s*\*\s*20$', parts[4]))
            if lit_mag or lit_dur:
                tag = "MAG" if lit_mag else "DUR"
                print(f"    [{tag}] {rel}: StatusEngine.apply({m.group(1)[:120]})")

print("\n=== 汇总 ===")
for row in summary:
    print(f"  {row[0]:<16} status={row[1]} attr={row[2]} wire={row[3]} unwired={row[4]} -> {row[5]}")
