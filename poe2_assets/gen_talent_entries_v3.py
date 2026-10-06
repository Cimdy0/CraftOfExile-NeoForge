#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
C2 规模化生成器 v3.2（registry-driven + 保守拼写桥接，零新增 effect 注册，零语义猜测）。

纪律（coe-corpus-stat-wiring skill §14 三红线）：
- R1: 绝不读 runData 产物。只读 .java 源码。
- R2: 绝不删 StatIds 常量。
- R3: 只复用 StatEffectGen 已注册的 EFF 常量。

匹配：对每一未接线 SIMPLE 模板，(属性短语, op) 在「全量已注册 Effect 表」找唯一匹配。
- 安全拼写桥接（仅项目文档化的纯拼写分歧，不改变语义作用域）：
  lightning->light / critical->crit / maximum->max / armour->armor / 去 rating。
  （ailment duration/magnitude 家族、复合属性、条件式 不桥接 -> 留人工核验，skill §3 警告）
- 唯一匹配 -> 安全接线（复用现有 effect，零发明）。
- 'increased' 仅 TOTAL 注册 或 BASE/TOTAL 并存 -> 按 inc()=BASE 约定选，标 [verify]。
- 零匹配 -> 设计缺口（推迟）。
绝不新建 effect / 绝不写节点文件。
"""
import json
import os
import re
from collections import Counter, defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STATGEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")
EFFIDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectIds.java")
EFFGEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectGen.java")
SPEC = os.path.join(ROOT, "poe2_assets/stat_entry_spec.json")

LEADING_NS = {
    "minecraft", "coe", "charm", "ailment", "buff", "debuff", "form", "ground",
    "shrine", "puppet", "master",
}
STOP = {
    "x", "the", "a", "an", "any", "to", "of", "on", "you", "your", "from",
    "with", "while", "when", "if", "against", "per", "for", "in", "is",
    "increased", "more", "reduced", "less", "add", "added", "gain", "gains",
    "rating",
}
# 仅项目文档化的纯拼写桥接（不改变语义作用域）
BRIDGE_NODE = [
    (r"\blightning\b", "light"),
    (r"\bcritical\b", "crit"),
    (r"\bmaximum\b", "max"),
    (r"\barmour\b", "armor"),
]
BRIDGE_SEG = [
    (r"\barmour\b", "armor"),
]


def norm(s):
    s = s.lower()
    s = re.sub(r"[^a-z0-9 ]", " ", s)
    s = re.sub(r"\s+", " ", s).strip()
    return s


def apply_bridge(s, rules):
    for pat, rep in rules:
        s = re.sub(pat, rep, s)
    return s


def node_attr_phrase(attr_raw):
    t = norm(attr_raw)
    t = apply_bridge(t, BRIDGE_NODE)
    toks = [w for w in t.split() if w and w not in STOP]
    while toks and toks[0] in LEADING_NS:
        toks = toks[1:]
    return " ".join(toks) if toks else ""


def seg_to_phrase(seg):
    toks = seg.split("_")
    while toks and toks[0] in LEADING_NS:
        toks = toks[1:]
    ph = " ".join(toks)
    ph = apply_bridge(ph, BRIDGE_SEG)
    return ph


# ---- 1. 全量已注册 Effect：(op, attr_phrase) -> [EFF 常量] + 签名 ----
eff_index = defaultdict(list)
eff_sig = {}
for line in open(EFFIDS, encoding="utf-8"):
    m = re.search(r'public static final Identifier (\w+) = id\("([^"]+)"\)', line)
    if not m:
        continue
    const, path = m.group(1), m.group(2)
    mm = re.search(r"^add_(multiplied_base|multiplied_total|value)_attribute_(.+)$", path)
    if not mm:
        continue
    op, seg = mm.group(1), mm.group(2)
    ph = seg_to_phrase(seg)
    if not ph:
        continue
    eff_index[(op, ph)].append(const)


# ---- 1b. 解析 StatEffectGen 签名（attribute, op, tags）用于审计 ----
def parse_signatures():
    sig = {}
    for line in open(EFFGEN, encoding="utf-8"):
        m = re.search(r"StatEffectIds\.(EFF_\w+),\s*new ModifierStatEffect\(", line)
        if not m:
            continue
        const = m.group(1)
        am = re.search(r"AttributeRegister\.(\w+)", line)
        om = re.search(r"AttributeModifier\.Operation\.(\w+)", line)
        tm = re.search(r"Set\.of\((.*?)\)", line)
        tags = []
        if tm:
            tags = re.findall(r"StatTag\.(\w+)", tm.group(1))
            tags += [f"!{t}" for t in re.findall(r"not\(StatTag\.(\w+)\)", tm.group(1))]
        sig[const] = (am.group(1) if am else "?", om.group(1) if om else "?", tags)
    return sig


sig = parse_signatures()

# ---- 2. buildEntries 已接线 ----
existing = {}
for line in open(STATGEN, encoding="utf-8"):
    m = re.search(r"map\.put\(StatIds\.(\w+),\s*(?:stat|self|onTarget)\(", line)
    if not m:
        continue
    em = re.search(r"StatEffectIds\.(\w+)", line)
    if em:
        existing[m.group(1)] = em.group(1)


# ---- 3. 节点模板解析 ----
def parse_template(tmpl):
    t = norm(tmpl)
    op_family = "inc" if re.search(r"\b(increased|more|reduced|less)\b", t) else "add"
    m = re.match(r"^\+?\{X\}%?\s*(?:inc|increased|more|reduced|less|add|added|gain|gains|to)\s+(.+)$", t)
    attr_raw = m.group(1) if m else tmpl
    return node_attr_phrase(attr_raw), op_family


# ---- 4. 遍历 spec（仅 SIMPLE） ----
spec = json.load(open(SPEC, encoding="utf-8"))
clean_wire, verify_wire, ambiguous, gap = [], [], [], []
gap_by_attr = Counter()

for t in spec:
    C, T, kind = t["constant"], t["template"], t["kind"]
    if C in existing or kind not in ("SIMPLE_INC", "SIMPLE_ADD"):
        continue
    attr_ph, op_family = parse_template(T)
    if not attr_ph:
        gap.append((C, T, kind, "属性短语为空")); gap_by_attr["<empty>"] += 1; continue
    if op_family == "inc":
        base = eff_index.get(("multiplied_base", attr_ph), [])
        total = eff_index.get(("multiplied_total", attr_ph), [])
        if base and not total:
            clean_wire.append((C, base[0], attr_ph, T, kind))
        elif total and not base:
            verify_wire.append((C, total[0], attr_ph, T, kind, "inc但仅TOTAL(=more,verify)"))
        elif base and total:
            verify_wire.append((C, base[0], attr_ph, T, kind, "BASE/TOTAL并存按inc=BASE(verify)"))
        else:
            gap.append((C, T, kind, f"无注册effect(属性={attr_ph})")); gap_by_attr[attr_ph] += 1
    else:
        vals = eff_index.get(("value", attr_ph), [])
        if len(vals) == 1:
            clean_wire.append((C, vals[0], attr_ph, T, kind))
        elif len(vals) > 1:
            ambiguous.append((C, T, kind, attr_ph, vals))
        else:
            gap.append((C, T, kind, f"无注册effect(属性={attr_ph})")); gap_by_attr[attr_ph] += 1

# ---- 5. 输出 ----
print(f"已接线 (existing)                 : {len(existing)}")
print(f"spec SIMPLE 未接线               : {sum(1 for t in spec if t['constant'] not in existing and t['kind'] in ('SIMPLE_INC','SIMPLE_ADD'))}")
print(f"CLEAN 安全接线 (唯一匹配)        : {len(clean_wire)}")
print(f"VERIFY 约定选线 (需确认 op)      : {len(verify_wire)}")
print(f"ambiguous (多 VALUE 候选)        : {len(ambiguous)}")
print(f"gap 设计缺口                     : {len(gap)}")
print("\n=== CLEAN 安全接线（含目标 effect 签名审计）===")
for C, eff, ph, T, kind in clean_wire:
    a, o, tg = sig.get(eff, ("?", "?", []))
    print(f"  {C:40s} <- [{ph:22s}] {kind:10s}\n      {eff}\n        attr={a} op={o} tags={tg}")
print(f"\n=== VERIFY 约定选线（{len(verify_wire)}）===")
for C, eff, ph, T, kind, note in verify_wire:
    a, o, tg = sig.get(eff, ("?", "?", []))
    print(f"  {C:40s} <- [{ph:22s}] {eff} | {note} | attr={a} op={o} tags={tg}")
print("\n=== 设计缺口 Top 35 ===")
for attr, n in gap_by_attr.most_common(35):
    print(f"  {n:4d}  {attr}")

with open(os.path.join(ROOT, "poe2_assets/c3_wire.txt"), "w", encoding="utf-8") as f:
    f.write("        // === C3 registry-driven 安全接线（SEG/op 源自 StatEffectGen 已注册 effect，零新增 effect 注册） ===\n")
    for row in clean_wire + verify_wire:
        C, eff = row[0], row[1]
        f.write(f"        map.put(StatIds.{C}, stat(StatTargetIds.TGT_SELF_ONLY, StatEffectIds.{eff}));\n")
with open(os.path.join(ROOT, "poe2_assets/c3_gap.json"), "w", encoding="utf-8") as f:
    json.dump([{"constant": g[0], "template": g[1], "kind": g[2], "reason": g[3]} for g in gap], f, ensure_ascii=False, indent=1)
print(f"\n候选 -> poe2_assets/c3_wire.txt ({len(clean_wire)+len(verify_wire)}) | gap -> poe2_assets/c3_gap.json ({len(gap)})")
