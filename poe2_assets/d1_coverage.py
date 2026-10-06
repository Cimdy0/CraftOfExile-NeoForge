# -*- coding: utf-8 -*-
"""D1 覆盖分析：D1 各块拆出的新常量，是否已被 §A 精确命中的旧实现覆盖？

若某片新常量已有 A 类旧常量精确对应（该旧常量注释就是这一片的原文），
则该片的实现已有出处，D1 旧常量对它是**重复登记**，可直接删除，无需裁决。
只有「无任何 A 类覆盖」的片才需要从 D1 REAL 实现中挑一个挂上去。
"""
import json, re, importlib.util
from collections import defaultdict

_m = importlib.util.spec_from_file_location("m", "poe2_assets/map_old_stats.py")
m = importlib.util.module_from_spec(_m); _m.loader.exec_module(m)
_et = importlib.util.spec_from_file_location("et", "poe2_assets/extract_templates.py")
et = importlib.util.module_from_spec(_et); _et.loader.exec_module(et)

OLD = "poe2_assets/_backup/stat/StatIds.java.old"
NEW = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
OUT = "poe2_assets/_test/d1_coverage.md"

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

clean = json.load(open("poe2_assets/poe2_tree_clean.json", encoding="utf-8"))
joined2pieces = {}
for nid, n in clean["nodes"].items():
    for stat in (n.get("stats") or []):
        lines = [l.strip() for l in stat.split("\n") if l.strip()]
        if len(lines) > 1:
            pieces = [m.normalize(p) for p in et.split_stat(stat)]
            if len(pieces) > 1:
                joined2pieces.setdefault(m.normalize(" ".join(lines)), pieces)

# §A：旧注释规范化后精确命中新表 -> 该片已有旧实现
A_cover = defaultdict(list)   # 新常量 -> [旧常量]
for const, eid, comment in old:
    key = m.normalize(comment) if comment else None
    if key and key in new_text:
        A_cover[new_by_text[key][0]].append(const)

d1 = []
for const, eid, comment in old:
    key = m.normalize(comment) if comment else None
    if key and key in new_text:
        continue
    if const in non_statgen:
        continue
    if key and key in joined2pieces:
        d1.append((const, eid, key))

real = {l.strip() for l in open("poe2_assets/_test/d1_real.txt", encoding="utf-8") if l.strip()}
dead = {l.strip() for l in open("poe2_assets/_test/d1_dead.txt", encoding="utf-8") if l.strip()}

groups = defaultdict(list)
for const, eid, key in d1:
    groups[key].append(const)

need_decision = []   # (block, piece_newconst, piece_text, 候选旧实现)
covered_pieces = 0
total_pieces = 0

with open(OUT, "w", encoding="utf-8") as f:
    w = f.write
    w("# D1 覆盖分析\n\n")
    w("判定每个拆分片是否已被 §A 精确命中的旧实现覆盖。已覆盖 -> D1 旧常量为重复登记，直接删除。\n\n")
    for bi, (key, consts) in enumerate(sorted(groups.items()), 1):
        pieces = joined2pieces[key]
        w(f"## 块 {bi}\n\n`{key}`\n\n")
        w("| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |\n|---|---|---|---|\n")
        uncovered = []
        for i, p in enumerate(pieces, 1):
            total_pieces += 1
            nc = new_by_text.get(p, ["缺失"])[0]
            cov = A_cover.get(nc, [])
            if cov:
                covered_pieces += 1
            else:
                uncovered.append((nc, p))
            w(f"| {i} | `{nc}` | {p} | {', '.join('`'+c+'`' for c in cov) or '**无**'} |\n")
        w(f"\n旧 D1 常量：REAL {sorted(c for c in consts if c in real)} / "
          f"DEAD {sorted(c for c in consts if c in dead)}\n\n")
        if uncovered:
            w(f"**待裁决片（{len(uncovered)}）**：" +
              ", ".join(f"`{nc}`" for nc, _ in uncovered) + "\n\n")
            for nc, p in uncovered:
                need_decision.append((bi, key, nc, p, sorted(c for c in consts if c in real)))
        else:
            w("**本块全部片已被 §A 覆盖 -> 该块所有 D1 旧常量均为重复登记，可删除。**\n\n")
        w("---\n\n")

print(f"D1 块 {len(groups)} / 拆分片 {total_pieces}")
print(f"已被 §A 覆盖的片: {covered_pieces}")
print(f"待裁决片        : {total_pieces - covered_pieces}")
print(f"其中涉及块数    : {len({d[0] for d in need_decision})}")
print(f"报告: {OUT}")
