#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""面6 装备预设注入面全量清单：每个 ItemBasicMod.fromBase/ValueMod.from/Addition.from 调用点
   → AdditionType → StatIds → 生成物条目形态（bare / div100 / const）→ 属性类型 → 单位定性。"""
import json
import os
import re

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
GEN = os.path.join(ROOT, "src", "generated", "resources", "data", "coe", "coe", "stat_entry")
SRC = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe")
NUM = r'-?\d+(?:\.\d+)?'

entries = {}
for fn in sorted(os.listdir(GEN)):
    if fn.endswith(".json"):
        with open(os.path.join(GEN, fn), "r", encoding="utf-8") as f:
            j = json.load(f)
        amt = j.get("amount", "")
        amt = amt if isinstance(amt, str) else repr(amt)
        entries[fn[:-5]] = (amt, j.get("attribute", ""), j.get("operation", ""))

with open(os.path.join(SRC, "data", "codec", "support", "stat", "StatIds.java"), "r", encoding="utf-8") as f:
    const2sid = {m.group(1): m.group(2)
                 for m in re.finditer(r'Identifier\s+(\w+)\s*=\s*id\("([^"]+)"\)', f.read())}
with open(os.path.join(SRC, "data", "codec", "item", "text", "AdditionType.java"), "r", encoding="utf-8") as f:
    at2sid = {}
    for m in re.finditer(r'AdditionType\s+(\w+)\s*=\s*from\([^;]*?id\(StatIds\.(\w+)\)', f.read(), re.S):
        s = const2sid.get(m.group(2))
        if s:
            at2sid[m.group(1)] = s
with open(os.path.join(SRC, "data", "AttributeRegister.java"), "r", encoding="utf-8") as f:
    akind = {"coe:" + m.group(2): m.group(1).lower()
             for m in re.finditer(r'register(Percentage|Double|Ranged|Boolean|\w*)\s*\(\s*"([^"]+)"', f.read())}

PRESET = re.compile(
    r'(?:ItemBasicMod\.fromBase|ValueMod\.from|Addition\.from)\(\s*AdditionType\.(\w+)\s*,\s*'
    r'(' + NUM + r')(?:\s*,\s*(' + NUM + r'))?\s*\)')

rows = []
for dp, _, fns in os.walk(os.path.join(SRC, "data", "gen")):
    for fn in fns:
        if not fn.endswith(".java"):
            continue
        p = os.path.join(dp, fn)
        with open(p, "r", encoding="utf-8") as f:
            txt = f.read()
        for m in PRESET.finditer(txt):
            at = m.group(1)
            sid = at2sid.get(at, "?")
            amt, attr, op = entries.get(sid, ("<无条目>", "", ""))
            form = "div100" if ("/100" in amt or "/ 100" in amt) else (
                "bare" if amt.strip() in ("$value", "-$value") else (
                    "tmpl" if "$value" in amt else "const"))
            nums = [float(m.group(2))] + ([float(m.group(3))] if m.group(3) else [])
            mag = max(abs(n) for n in nums) if nums else 0
            unit = "fraction" if mag < 1.0 else "points"
            kind = akind.get(attr, "vanilla" if attr.startswith("minecraft:") else "-")
            # 期望单位：div100 -> points；bare/tmpl -> fraction（比例类属性）
            expect = "points" if form == "div100" else "fraction"
            pct_like = kind == "percentage" or attr in (
                "coe:ailment_threshold", "coe:damage_over_time_multiplier", "coe:taken_damage",
                "coe:regeneration_rate", "coe:crit_damage", "coe:ailment_magnitude",
                "coe:resistance_penetration", "coe:damage", "coe:amount_leeched", "coe:leech")
            verdict = "-"
            if form in ("div100", "bare", "tmpl") and pct_like:
                verdict = "OK" if unit == expect else ("放大100x" if unit == "points" and form != "div100"
                                                       else "缩小100x")
            rows.append((os.path.relpath(p, ROOT).replace("\\", "/"),
                         txt[:m.start()].count("\n") + 1, at, sid, form, attr, kind, op,
                         str(nums), unit, expect, verdict))

rows.sort(key=lambda r: (r[11] == "OK", r[11] == "-", r[0], r[1]))

o = ["# 面6 装备预设注入面全量清单（%d 处）\n" % len(rows),
     "| 文件 | 行 | AdditionType | stat | 条目形态 | 属性 | 类型 | op | 数值 | 单位 | 期望 | 判定 |",
     "|---|---|---|---|---|---|---|---|---|---|---|---|"]
for r in rows:
    o.append("| %s | %d | `%s` | `%s` | %s | %s | %s | %s | %s | %s | %s | **%s** |"
             % (r[0].split("/")[-1], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], r[9], r[10], r[11]))
with open(os.path.join(os.path.dirname(__file__), "preset_audit.md"), "w", encoding="utf-8") as f:
    f.write("\n".join(o))

from collections import Counter
print("总计 %d 处；判定分布 %s" % (len(rows), dict(Counter(r[11] for r in rows))))
print("%-26s %-4s %-34s %-30s %-7s %-28s %-9s %-14s %-8s %s"
      % ("file", "ln", "AdditionType", "stat", "form", "attr", "nums", "unit/expect", "verdict", ""))
for r in rows:
    print("%-26s %-4d %-34s %-30s %-7s %-28s %-9s %-14s %s"
          % (r[0].split("/")[-1], r[1], r[2], r[3], r[4], r[5], r[8], r[9] + "/" + r[10], r[11]))
