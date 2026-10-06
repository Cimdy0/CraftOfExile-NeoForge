#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
方案A 前置风险交叉表 v3（全注入面）。

条目真相：src/generated/resources/data/coe/coe/stat_entry/*.json
注入面（已穷举 `new StatRead(id, vars)` 的全部构造点）：
  面1  StatGen.ref(StatIds.X, expr)                     Buff/Charm/Form/Skill
  面2  AdditionType X = from(K, id(StatIds.Y)) + Mod.from(..)+WithoutTier.from(lo,hi,name,..)
  面3  StatusBehavior.sb(StatIds.X, expr) / sb(fromNamespaceAndPath(NS, "pfx_"+s), expr)
  面4  SkillFactory.readAilmentStat(face, type, value)  -> coe:<face>_<statusId>
  面5  StatGen.modifierAttr(attr, amount, ..)           自注册裸 $value 模板（自洽，跳过）
  面X  天赋节点：0 处引用 StatIds（走 talentAttr 烘常量），架构上不消费 $value 模板
"""
import json
import os
import re
from collections import defaultdict

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
GEN = os.path.join(ROOT, "src", "generated", "resources", "data", "coe", "coe", "stat_entry")
SRC = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe")
NUM = r'-?\d+(?:\.\d+)?'

# ---------------------------------------------------------------- 条目真相
entries = {}
for fn in sorted(os.listdir(GEN)):
    if not fn.endswith(".json"):
        continue
    with open(os.path.join(GEN, fn), "r", encoding="utf-8") as f:
        j = json.load(f)
    amt = j.get("amount", "")
    if not isinstance(amt, str):
        amt = repr(amt)
    entries[fn[:-5]] = {
        "amount": amt, "attr": j.get("attribute", ""), "op": j.get("operation", ""),
        "div100": "/ 100" in amt or "/100" in amt,
        "bare": amt.strip() in ("$value", "-$value"),
        "hasval": "$value" in amt,
    }

# ---------------------------------------------------------------- StatIds / 属性类型
with open(os.path.join(SRC, "data", "codec", "support", "stat", "StatIds.java"), "r", encoding="utf-8") as f:
    const2sid = {m.group(1): m.group(2)
                 for m in re.finditer(r'Identifier\s+(\w+)\s*=\s*id\("([^"]+)"\)', f.read())}

with open(os.path.join(SRC, "data", "AttributeRegister.java"), "r", encoding="utf-8") as f:
    atxt = f.read()
attr_kind = {}
for m in re.finditer(r'register(Percentage|Double|Ranged|Boolean|\w*)\s*\(\s*"([^"]+)"', atxt):
    attr_kind["coe:" + m.group(2)] = m.group(1).lower()

# ---------------------------------------------------------------- 源文件集合（排除备份）
java_files = []
for dp, _, fns in os.walk(SRC):
    for fn in fns:
        if fn.endswith(".java"):
            java_files.append(os.path.join(dp, fn))

VAR = re.compile(r'\b(?:final\s+)?(?:double|float|int|long)\s+(\w+)\s*=\s*([^;]+);')


def resolve(expr, vmap, depth=0):
    if depth > 5:
        return expr
    out = expr
    for name, val in vmap.items():
        if re.search(r'\b' + re.escape(name) + r'\b', out):
            out = re.sub(r'\b' + re.escape(name) + r'\b', "(" + val + ")", out)
    return resolve(out, vmap, depth + 1) if out != expr else out


callers = defaultdict(list)  # sid -> [(surface, file, raw, resolved)]

# 面1 + 面3（StatGen.ref / sb 形态一致，统一扫）
CALL = re.compile(r'\b(?:StatGen\.ref|sb)\(\s*StatIds\.(\w+)\s*,\s*([^;]+?)\)\s*(?:,|\)|;)')
DYN = re.compile(r'\bsb\(\s*Identifier\.fromNamespaceAndPath\(\s*NS\s*,\s*"([^"]+)"\s*(?:\+\s*(\w+))?\s*\)\s*,\s*([^;]+?)\)\s*(?:,|\)|;)')
for jf in java_files:
    with open(jf, "r", encoding="utf-8") as f:
        txt = f.read()
    if "StatGen.ref" not in txt and "sb(" not in txt:
        continue
    rel = os.path.relpath(jf, ROOT).replace("\\", "/")
    vmap = {m.group(1): m.group(2).strip() for m in VAR.finditer(txt)}
    for m in CALL.finditer(txt):
        sid = const2sid.get(m.group(1))
        if sid:
            callers[sid].append(("ref", rel, m.group(2).strip(), resolve(m.group(2).strip(), vmap)))
    for m in DYN.finditer(txt):
        pfx, raw = m.group(1), m.group(3).strip()
        for sid in entries:
            if sid.startswith(pfx):
                callers[sid].append(("ref-dyn", rel, raw, resolve(raw, vmap)))

# 面4 SkillFactory.readAilmentStat
FACE = re.compile(r'readAilmentStat\(\s*(\w+)\s*,\s*[^,]+,\s*([^;]+?)\)\s*(?:,|\)|;)')
FACEC = re.compile(r'String\s+(FACE_\w+|\w+)\s*=\s*"([^"]+)"')
for jf in java_files:
    if "SkillFactory" not in jf:
        continue
    with open(jf, "r", encoding="utf-8") as f:
        txt = f.read()
    facemap = {m.group(1): m.group(2) for m in FACEC.finditer(txt)}
    rel = os.path.relpath(jf, ROOT).replace("\\", "/")
    for m in FACE.finditer(txt):
        pfx = facemap.get(m.group(1), m.group(1))
        for sid in entries:
            if sid.startswith(pfx + "_"):
                callers[sid].append(("ailment-face", rel, m.group(2).strip()[:40], "?"))

# 面2 词缀
with open(os.path.join(SRC, "data", "codec", "item", "text", "AdditionType.java"), "r", encoding="utf-8") as f:
    at2sid = {}
    for m in re.finditer(r'AdditionType\s+(\w+)\s*=\s*from\([^;]*?id\(StatIds\.(\w+)\)', f.read(), re.S):
        s = const2sid.get(m.group(2))
        if s:
            at2sid[m.group(1)] = s

ROLL = re.compile(r'Mod\.from\(\s*\d+\s*,\s*AdditionType\.(\w+)\b(.*?)\)\s*;', re.S)
# 只取 (Without|With)Tier.from( 到首个字符串字面量之前的数字 —— 后面是 name/level/weight
TIER = re.compile(r'(?:WithoutTier|WithTier)\.from\(\s*([^")]*?)\s*"', re.S)
affix_rolls = defaultdict(list)
for dp, _, fns in os.walk(os.path.join(SRC, "data", "gen", "equipment")):
    for fn in fns:
        if not fn.endswith(".java"):
            continue
        p = os.path.join(dp, fn)
        with open(p, "r", encoding="utf-8") as f:
            txt = f.read()
        rel = os.path.relpath(p, ROOT).replace("\\", "/")
        for m in ROLL.finditer(txt):
            sid = at2sid.get(m.group(1))
            if not sid:
                continue
            nums = []
            for t in TIER.finditer(m.group(2)):
                nums += [float(x) for x in re.findall(NUM, t.group(1))]
            affix_rolls[sid].append((rel, m.group(1), nums))

# 面6 装备预设内联幅度：ItemBasicMod.fromBase / ValueMod.from / Addition.from (AdditionType.X, min[, max])
PRESET = re.compile(
    r'(?:ItemBasicMod\.fromBase|ValueMod\.from|Addition\.from)\(\s*AdditionType\.(\w+)\s*,\s*'
    r'(' + NUM + r')(?:\s*,\s*(' + NUM + r'))?\s*\)')
for dp, _, fns in os.walk(os.path.join(SRC, "data", "gen")):
    for fn in fns:
        if not fn.endswith(".java"):
            continue
        p = os.path.join(dp, fn)
        with open(p, "r", encoding="utf-8") as f:
            txt = f.read()
        rel = os.path.relpath(p, ROOT).replace("\\", "/")
        for m in PRESET.finditer(txt):
            sid = at2sid.get(m.group(1))
            if not sid:
                continue
            nums = [float(m.group(2))] + ([float(m.group(3))] if m.group(3) else [])
            ln = txt[:m.start()].count("\n") + 1
            affix_rolls[sid].append(("%s:%d" % (rel, ln), "preset:" + m.group(1), nums))

# ---------------------------------------------------------------- 单位判定
def cls_expr(e):
    e2 = re.sub(r'\b\w+\.fold\([^)]*\)', "?", e)
    nums = [abs(float(x)) for x in re.findall(NUM, e2)]
    nums = [n for n in nums if n not in (0.0, 1.0, 2.0, 100.0)]
    if "?" in e2 or not nums:
        return "unknown"
    if all(n < 1.0 for n in nums):
        return "fraction"
    if all(n >= 2.0 for n in nums):
        return "points"
    return "unknown"


def cls_rolls(nums):
    ns = [abs(n) for n in nums if n != 0]
    if not ns:
        return "unknown"
    if all(n < 1.0 for n in ns):
        return "fraction"
    if all(n >= 1.0 for n in ns):
        return "points"
    return "unknown"


def collect(sid):
    cs = []
    for s, f, raw, res in callers.get(sid, []):
        cs.append((s, f, raw, "unknown" if res == "?" else cls_expr(res)))
    for f, at, nums in affix_rolls.get(sid, []):
        cs.append(("affix:" + at, f, str(nums[:4]), cls_rolls(nums)))
    return cs


div = {s: e for s, e in entries.items() if e["div100"]}
bare = {s: e for s, e in entries.items() if e["bare"]}

# ---- 内部分裂检测：同一 stat 同时收到 fraction 与 points 调用方（无论约定必错一侧）----
split = []
for sid, e in sorted(entries.items()):
    if not e["hasval"]:
        continue
    cs = collect(sid)
    ks = {c[3] for c in cs}
    if "fraction" in ks and "points" in ks:
        split.append((sid, e, cs))

risk, fix, mixed, orphan, bare_risk = [], [], [], [], []
for sid, e in sorted(div.items()):
    cs = collect(sid)
    kinds = {c[3] for c in cs}
    if not cs:
        orphan.append((sid, e, cs))
    elif kinds == {"points"}:
        risk.append((sid, e, cs))
    elif kinds == {"fraction"}:
        fix.append((sid, e, cs))
    else:
        mixed.append((sid, e, cs))

for sid, e in sorted(bare.items()):
    cs = collect(sid)
    if not cs or attr_kind.get(e["attr"]) != "percentage":
        continue
    if any(c[3] == "points" for c in cs):
        bare_risk.append((sid, e, cs))

# ---------------------------------------------------------------- 报告
o = ["# 方案A 前置风险交叉表 v3（全注入面）\n",
     "| 指标 | 值 |", "|---|---|",
     "| 生成物条目总数 | %d |" % len(entries),
     "| 含 `$value` 条目 | %d |" % sum(1 for e in entries.values() if e["hasval"]),
     "| 烘 `/100` 条目 | %d |" % len(div),
     "| 裸 `$value` 条目 | %d |" % len(bare),
     "| 有调用方的 stat 数（ref 面） | %d |" % len(callers),
     "| 有调用方的 stat 数（词缀面） | %d |" % len(affix_rolls),
     "| 天赋节点引用 StatIds 数 | 0（走 talentAttr 烘常量） |", "",
     "| 分类 | 数量 | 方案A 影响 |", "|---|---|---|",
     "| RISK（`/100` + 整数点调用方） | %d | **会被打坏，须同步改** |" % len(risk),
     "| FIX（`/100` + 小数调用方） | %d | 方案A 直接修复 |" % len(fix),
     "| MIXED（含未知/混合调用方） | %d | 逐条裁定 |" % len(mixed),
     "| ORPHAN（`/100` 无任何调用方） | %d | 安全 |" % len(orphan),
     "| BARE-RISK（裸 `$value` + 整数点，pct 属性） | %d | 现存放大 100× |" % len(bare_risk), ""]

o.append("| SPLIT（同 stat 内部单位分裂） | %d | **必错一侧，与约定无关** |" % len(split))
o.append("")

for title, g in (("SPLIT", split), ("RISK", risk), ("FIX", fix), ("MIXED", mixed), ("BARE-RISK", bare_risk)):
    o.append("## %s（%d）\n" % (title, len(g)))
    if not g:
        o.append("_无_\n")
        continue
    o.append("| stat | 属性 | 类型 | op | 注入面 | 表达式 | 单位 |")
    o.append("|---|---|---|---|---|---|---|")
    for sid, e, cs in g:
        for s, f, raw, u in cs:
            o.append("| `%s` | %s | %s | %s | %s | `%s` | **%s** |"
                     % (sid, e["attr"], attr_kind.get(e["attr"], "-"), e["op"], s,
                        raw.replace("|", "\\|")[:60], u))
    o.append("")

o.append("## ORPHAN 归类（%d）\n" % len(orphan))
by = defaultdict(list)
for sid, e, _ in orphan:
    by[(e["attr"], attr_kind.get(e["attr"], "-"), e["op"])].append(sid)
o.append("| 属性 | 类型 | op | 条目数 | 示例 |")
o.append("|---|---|---|---|---|")
for (a, k, op), sids in sorted(by.items(), key=lambda x: -len(x[1])):
    o.append("| %s | %s | %s | %d | %s |" % (a, k, op, len(sids), ", ".join("`%s`" % s for s in sids[:3])))

with open(os.path.join(os.path.dirname(__file__), "plana_risk.md"), "w", encoding="utf-8") as f:
    f.write("\n".join(o))

print("entries=%d hasval=%d div100=%d bare=%d"
      % (len(entries), sum(1 for e in entries.values() if e["hasval"]), len(div), len(bare)))
print("SPLIT=%d RISK=%d FIX=%d MIXED=%d ORPHAN=%d BARE-RISK=%d" % (len(split), len(risk), len(fix), len(mixed), len(orphan), len(bare_risk)))
for title, g in (("SPLIT", split), ("RISK", risk), ("MIXED", mixed), ("FIX", fix), ("BARE-RISK", bare_risk)):
    print("\n--- %s (%d) ---" % (title, len(g)))
    for sid, e, cs in g:
        print("  %-42s %-28s %s" % (sid, e["attr"], e["op"]))
        for s, f, raw, u in cs:
            print("      %-30s %-26s %-40s -> %s" % (s, f.split("/")[-1], raw[:40], u))
