#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
C2 规模化生成器 v2（短语匹配式，零新增 effect 注册，零语义猜测）。

纪律（coe-corpus-stat-wiring skill）：
- R1: 绝不读 runData 产物。只读 .java 源码 + 派生分析物。
- R3: 只复用 StatEffectGen 已注册的 EFF 常量。
- SEG 完全从「已接线条目的 StatIds 注释模板 + EFF 路径」反向推导，不臆造。

算法：
  1. 解析 StatIds.java：每个常量 X 的紧邻注释 = 二级归一模板（如 "{X}% inc Armour"）。
  2. 解析 buildEntries：X -> EFF.Y（已接线）。
  3. 对每个已接线条目，从注释抽「属性短语」(去操作符)，从 EFF.Y 路径抽 SEG，
     建 短语->SEG 字典（短语唯一才用，歧义则推迟）。
  4. 对每个未接线 SIMPLE 模板：抽属性短语归一 -> 查字典得 SEG ->
     目标 EFF = EFF_ADD_<MB|VALUE>_ATTRIBUTE_<SEG>，须在 StatEffectGen 注册 -> 安全接线。
"""
import json
import os
import re
from collections import Counter, defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STATGEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")
EFFGEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectGen.java")
EFFIDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectIds.java")
STATIDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
SPEC = os.path.join(ROOT, "poe2_assets/stat_entry_spec.json")


def norm_phrase(s):
    s = s.lower()
    s = re.sub(r"[^a-z0-9 ]", " ", s)
    s = re.sub(r"\s+", " ", s).strip()
    return s


def extract_phrase(template):
    """从归一模板抽属性短语（去操作符）。返回归一短语或 None。"""
    t = template.strip()
    m = re.match(r"^\+?\{X\}%?\s*(?:inc|increased|more|reduced|less)\s+(.+)$", t)
    if m:
        return norm_phrase(m.group(1))
    m = re.match(r"^\+?\{X\}\s*(?:to|add|gain)\s+(.+)$", t)
    if m:
        return norm_phrase(m.group(1))
    return None


def seg_of_path(path):
    if path.startswith("add_multiplied_base_attribute_"):
        return path[len("add_multiplied_base_attribute_"):]
    if path.startswith("add_value_attribute_"):
        return path[len("add_value_attribute_"):]
    return None


# ---- 1. StatIds 常量 -> 注释模板 ----
comment_tmpl = {}
lines = open(STATIDS, encoding="utf-8").read().splitlines()
pending = None
for ln in lines:
    s = ln.strip()
    if s.startswith("//"):
        pending = s[2:].strip()
    elif "public static final Identifier" in ln and "= id(" in ln:
        m = re.search(r"Identifier (\w+) = id\(\"([^\"]+)\"\)", ln)
        if m:
            comment_tmpl[m.group(1)] = pending
        pending = None
    else:
        pending = None

# ---- 2. buildEntries: X -> EFF.Y ----
existing = {}
for line in open(STATGEN, encoding="utf-8"):
    m = re.search(r"map\.put\(StatIds\.(\w+),\s*(?:stat|self|onTarget)\(", line)
    if not m:
        continue
    em = re.search(r"StatEffectIds\.(\w+)", line)
    if em:
        existing[m.group(1)] = em.group(1)

# ---- 3. StatEffectGen 注册 EFF 集合 ----
registered = set()
for line in open(EFFGEN, encoding="utf-8"):
    for em in re.finditer(r"StatEffectIds\.(\w+)", line):
        registered.add(em.group(1))

# ---- 4. EFF 名 -> 路径 ----
eff_path = {}
for line in open(EFFIDS, encoding="utf-8"):
    m = re.search(r'public static final Identifier (\w+) = id\("([^"]+)"\)', line)
    if m:
        eff_path[m.group(1)] = m.group(2)

# ---- 5. 建短语 -> SEG 字典 ----
phrase_seg = defaultdict(set)
for sid, eff in existing.items():
    tmpl = comment_tmpl.get(sid)
    if not tmpl:
        continue
    ph = extract_phrase(tmpl)
    if not ph:
        continue
    p = eff_path.get(eff)
    if not p:
        continue
    seg = seg_of_path(p)
    if seg:
        phrase_seg[ph].add(seg)

# 仅保留唯一映射
unique_phrase_seg = {ph: list(s)[0] for ph, s in phrase_seg.items() if len(s) == 1}


# ---- 6. 保守同义词桥接（仅拼写/rating 后缀，基词保留，可审计）----
def bridge(ph):
    rules = [
        (r"\barmour\b", "armor"),
        (r"\brating\b", ""),
        (r"\bevasion rating\b", "evasion"),
        (r"\barmour and evasion rating\b", "armour and evasion"),
    ]
    out = ph
    for pat, rep in rules:
        out = re.sub(pat, rep, out)
    return re.sub(r"\s+", " ", out).strip()


# ---- 7. 遍历 spec ----
spec = json.load(open(SPEC, encoding="utf-8"))
wire, defer = [], []
for t in spec:
    C, T, kind = t["constant"], t["template"], t["kind"]
    if C in existing:
        continue
    ph = extract_phrase(T)
    if not ph or kind not in ("SIMPLE_INC", "SIMPLE_ADD"):
        defer.append((C, T, kind, "COMPLEX/无短语"))
        continue
    matched, via = None, None
    if ph in unique_phrase_seg:
        matched, via = unique_phrase_seg[ph], "direct"
    else:
        b = bridge(ph)
        if b != ph and b in unique_phrase_seg:
            matched, via = unique_phrase_seg[b], "bridge:" + b
    if not matched:
        reason = "短语无映射"
        if ph in phrase_seg:
            reason = f"短语歧义:{sorted(phrase_seg[ph])}"
        defer.append((C, T, kind, reason))
        continue
    op = "MULTIPLIED_BASE" if kind == "SIMPLE_INC" else "VALUE"
    te = "EFF_ADD_" + op + "_ATTRIBUTE_" + matched.upper()
    if te in registered:
        wire.append((C, te, matched, ph, via))
    else:
        defer.append((C, T, kind, f"目标未注册:{te}"))


print(f"StatIds 注释模板数      : {len(comment_tmpl)}")
print(f"buildEntries 已接线     : {len(existing)}")
print(f"StatEffectGen 注册 EFF  : {len(registered)}")
print(f"短语->SEG(唯一)        : {len(unique_phrase_seg)}")
print(f"spec 模板总数          : {len(spec)}")
print(f"安全可接线 (wire)      : {len(wire)}")
print(f"需推迟 (defer)         : {len(defer)}")
print()
print("=== 安全接线预览（前 40）===")
for C, te, seg, ph, via in wire[:40]:
    print(f"  {C:44s} <- {ph:30s} ({via}) -> {te}")
if len(wire) > 40:
    print(f"  ... 其余 {len(wire)-40} 条")
print()
print("=== 推迟原因分布 ===")
rc = Counter(d[3].split(":")[0] for d in defer)
for k, v in rc.most_common():
    print(f"  {v:4d}  {k}")

with open(os.path.join(ROOT, "poe2_assets/c2_wire_candidates.txt"), "w", encoding="utf-8") as f:
    f.write("        // === C2 规模化：短语匹配式安全接线（SEG 源自已接线条目注释 + 保守同义词桥接，零新增 effect 注册） ===\n")
    for C, te, seg, ph, via in wire:
        f.write(f"        map.put(StatIds.{C}, stat(StatTargetIds.TGT_SELF_ONLY, StatEffectIds.{te}));\n")

with open(os.path.join(ROOT, "poe2_assets/c2_defer.json"), "w", encoding="utf-8") as f:
    json.dump([{"constant": d[0], "template": d[1], "kind": d[2], "reason": d[3]} for d in defer],
              f, ensure_ascii=False, indent=1)

print(f"\n候选片段 -> poe2_assets/c2_wire_candidates.txt ({len(wire)} 条)")
print(f"推迟清单 -> poe2_assets/c2_defer.json ({len(defer)} 条)")
