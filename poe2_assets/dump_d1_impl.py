# -*- coding: utf-8 -*-
"""为 D1 的 20 个多行块导出「旧常量 StatGen 实现原文 + 新常量候选」，供逐块定序裁决。"""
import json, re, importlib.util
from collections import defaultdict

_m = importlib.util.spec_from_file_location("m", "poe2_assets/map_old_stats.py")
m = importlib.util.module_from_spec(_m); _m.loader.exec_module(m)
_et = importlib.util.spec_from_file_location("et", "poe2_assets/extract_templates.py")
et = importlib.util.module_from_spec(_et); _et.loader.exec_module(et)

OLD = "poe2_assets/_backup/stat/StatIds.java.old"
NEW = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
STATGEN = "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
CLEAN = "poe2_assets/poe2_tree_clean.json"
FULL = "poe2_assets/poe2_tree.json"
OUT = "poe2_assets/_test/d1_decide.md"

CONSUMERS = [
    "src/main/java/com/cimdy/coe/data/codec/item/text/AdditionType.java",
    "src/main/java/com/cimdy/coe/data/codec/support/status/StatusBehavior.java",
    "src/main/java/com/cimdy/coe/data/codec/support/status/registry/buff/BuffStatusTypes.java",
    "src/main/java/com/cimdy/coe/data/codec/support/status/registry/charm/CharmStatusTypes.java",
    "src/main/java/com/cimdy/coe/data/codec/support/status/registry/form/FormStatusTypes.java",
    "src/main/java/com/cimdy/coe/data/gen/gem/skill/SkillFactory.java",
]
REF = re.compile(r"StatIds\.([A-Z][A-Z0-9_]*)")
non_statgen = set()
for p in CONSUMERS:
    non_statgen |= set(REF.findall(open(p, encoding="utf-8").read()))

old = m.parse(OLD)
new = m.parse(NEW)
new_by_text = defaultdict(list)
for c, e, cm in new:
    new_by_text[cm].append(c)
new_text = set(new_by_text)

clean = json.load(open(CLEAN, encoding="utf-8"))
full = json.load(open(FULL, encoding="utf-8"))

joined2pieces = {}
for nid, n in clean["nodes"].items():
    for stat in (n.get("stats") or []):
        lines = [l.strip() for l in stat.split("\n") if l.strip()]
        if len(lines) > 1:
            pieces = [m.normalize(p) for p in et.split_stat(stat)]
            if len(pieces) > 1:
                joined2pieces.setdefault(m.normalize(" ".join(lines)), pieces)

full_lines = set()
for nid, n in (full.get("nodes") or {}).items():
    for stat in (n.get("stats") or []):
        for p in et.split_stat(stat):
            full_lines.add(m.normalize(p))
        lines = [l.strip() for l in stat.split("\n") if l.strip()]
        if len(lines) > 1:
            full_lines.add(m.normalize(" ".join(lines)))

d1 = []
for const, eid, comment in old:
    key = m.normalize(comment) if comment else None
    if key and key in new_text:
        continue
    if const in non_statgen:
        continue
    if key and key in joined2pieces:
        d1.append((const, eid, key))

# --- 抽 StatGen 实现原文 ---
sg = open(STATGEN, encoding="utf-8").read().split("\n")


def impl_of(const):
    """返回 (起始行号, 原文块)。块 = 前置连续 // 注释 + 语句直到分号收尾且括号配平。"""
    pat = re.compile(r"(?:map\.put\(|\b\w+\(map,\s*)StatIds\." + const + r"\b")
    for i, line in enumerate(sg):
        if pat.search(line):
            j = i - 1
            while j >= 0 and sg[j].strip().startswith("//"):
                j -= 1
            head = j + 1
            k, depth = i, 0
            while k < len(sg):
                depth += sg[k].count("(") - sg[k].count(")")
                if depth <= 0 and sg[k].rstrip().endswith(";"):
                    break
                k += 1
            return head + 1, "\n".join(sg[head:k + 1])
    return None, None


groups = defaultdict(list)
for const, eid, key in d1:
    groups[key].append((const, eid))

with open(OUT, "w", encoding="utf-8") as f:
    w = f.write
    w("# D1 一对多拆分 · 逐块裁决材料\n\n")
    w(f"共 **{len(d1)}** 条旧常量 / **{len(groups)}** 个多行块。全部仅被 StatGen 引用。\n\n")
    w("每块给出：① 语料原文按换行语义切出的新常量（有序）② 旧常量及其现有 StatGen 实现原文。\n")
    w("裁决目标：把每个旧实现挂到对应的新常量上；无对应者标记删除。\n\n---\n\n")
    for bi, (key, consts) in enumerate(sorted(groups.items()), 1):
        pieces = joined2pieces[key]
        w(f"## 块 {bi}\n\n**合并原文**：`{key}`\n\n")
        w(f"### 新常量（{len(pieces)}，有序）\n\n")
        for i, p in enumerate(pieces, 1):
            hit = new_by_text.get(p)
            w(f"{i}. `{hit[0] if hit else '缺失'}` — {p}\n")
        w(f"\n### 旧常量（{len(consts)}）及现有实现\n\n")
        for c, e in sorted(consts):
            ln, body = impl_of(c)
            w(f"**`{c}`** (id `{e}`)  StatGen:{ln}\n\n```java\n{body}\n```\n\n")
        w("---\n\n")

print(f"D1 常量 {len(d1)} / 块 {len(groups)}")
missing = [c for c, e, k in d1 if impl_of(c)[1] is None]
print(f"未找到实现的旧常量: {len(missing)} {missing}")
print(f"报告: {OUT}")
