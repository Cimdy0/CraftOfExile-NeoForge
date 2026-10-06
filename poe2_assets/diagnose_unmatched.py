# -*- coding: utf-8 -*-
"""第 5 步（1.6/2）：对 594 条失配做**溯源诊断**，给出每条的确定性归因。

三条互斥的确定性检验（无相似度、无近似）：
  D1 多行合并型：旧注释 == 某节点「整块多行 stat 空格合并后」的规范化形。
     旧管道未按行拆分（正是第 3 步修掉的 bug），故一条旧常量对应新表中的 N 条模板。
     -> 可精确拆解为 N 个新常量。
  D2 已剔除语料型：旧注释匹配**完整** poe2_tree.json 的某行 stat，但不在清洗后语料中。
     即来自第 1 步已删除的升华(ascendancy)/专精(mastery)/不可达节点。
     -> 超出当前范围，保留为遗留区或废弃。
  D3 自有引擎 stat：注释仅为常量名回译，或在完整语料中亦无对应。
     -> 模组自有子系统（技能/状态/物品/MC 原生属性），原样保留。
"""
import json, re
from collections import defaultdict
import importlib.util

spec = importlib.util.spec_from_file_location("m", "poe2_assets/map_old_stats.py")
m = importlib.util.module_from_spec(spec)
spec.loader.exec_module(m)

# 复用第 3 步修正后的换行语义拆分（区分真分隔 / 软折行 / 项目符号）
_et = importlib.util.spec_from_file_location("et", "poe2_assets/extract_templates.py")
et = importlib.util.module_from_spec(_et)
_et.loader.exec_module(et)

OLD = "poe2_assets/_backup/stat/StatIds.java.old"
NEW = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
REFS = "poe2_assets/_test/referenced_consts.txt"
FULL = "poe2_assets/poe2_tree.json"
CLEAN = "poe2_assets/poe2_tree_clean.json"
OUT = "poe2_assets/_test/unmatched_diagnosis.md"


def to_name(t):
    s = t.replace("{X}", "X").lower()
    s = re.sub(r"[%+\-•]", " ", s)
    s = re.sub(r"[^a-z0-9 ]", " ", s)
    return "_".join(w.upper() for w in s.split() if w)


old = m.parse(OLD)
new = m.parse(NEW)
refs = {l.strip() for l in open(REFS, encoding="utf-8") if l.strip()}
new_text = {c for _, _, c in new}

clean = json.load(open(CLEAN, encoding="utf-8"))
full = json.load(open(FULL, encoding="utf-8"))

# D1 素材：清洗语料中「整块多行 stat」合并后的规范化形 -> 该块按换行语义切出的模板
# 切分复用第 3 步修正后的 split_stat（真分隔 / 软折行 / 项目符号三判据），
# 因此只有「切出 >=2 片」的块才构成真正的一对多；切出 1 片的块其 joined 形
# 与该片相等，会在上游被 §A 精确命中吸收。
joined2lines = {}
for nid, n in clean["nodes"].items():
    for stat in (n.get("stats") or []):
        lines = [l.strip() for l in stat.split("\n") if l.strip()]
        if len(lines) > 1:
            joined = m.normalize(" ".join(lines))
            pieces = [m.normalize(p) for p in et.split_stat(stat)]
            if len(pieces) > 1:
                joined2lines.setdefault(joined, pieces)

# D2 素材：完整语料（含升华/专精/不可达）的规范化模板集。
# 同时收录两种形态，覆盖旧管道可能的两种产出：
#   a) 按换行语义切出的单条 stat（正确形）
#   b) 整块多行合并成一行（旧管道 bug 形）
full_nodes = full.get("nodes") or {}
full_lines = set()
for nid, n in full_nodes.items():
    for stat in (n.get("stats") or []):
        for p in et.split_stat(stat):
            full_lines.add(m.normalize(p))
        lines = [l.strip() for l in stat.split("\n") if l.strip()]
        if len(lines) > 1:
            full_lines.add(m.normalize(" ".join(lines)))

d1, d2, d3 = [], [], []
for const, eid, comment in old:
    if not comment:
        d3.append((const, eid, comment, "无注释"))
        continue
    key = m.normalize(comment)
    if key in new_text:
        continue  # §A 已精确命中
    if key in joined2lines:
        d1.append((const, eid, comment, key, joined2lines[key]))
    elif key in full_lines:
        d2.append((const, eid, comment, key))
    elif to_name(comment) == const:
        d3.append((const, eid, comment, "常量名回译"))
    else:
        d3.append((const, eid, comment, "完整语料中亦无对应"))

# D1 分组：同一 joined 键下的旧常量族
d1_groups = defaultdict(list)
for const, eid, comment, key, lines in d1:
    d1_groups[key].append(const)

with open(OUT, "w", encoding="utf-8") as f:
    w = f.write
    w("# 594 条失配 · 溯源诊断\n\n")
    w("| 归因 | 数量 | 根因 | 处置 |\n|---|---|---|---|\n")
    w(f"| D1 多行合并型 | {len(d1)} | 旧管道未按行拆分（第 3 步已修的同一 bug） | 拆解为多个新常量 |\n")
    w(f"| D2 已剔除语料型 | {len(d2)} | 来自第 1 步删除的升华/专精/不可达节点 | 超范围，遗留或废弃 |\n")
    w(f"| D3 自有引擎 stat | {len(d3)} | 非天赋树语料，属模组自有子系统 | **原样保留** |\n")
    w(f"| **合计** | **{len(d1)+len(d2)+len(d3)}** | | |\n\n")
    w(f"D1 涉及 {len(d1_groups)} 个多行块。\n\n")

    w("---\n\n## D1 多行合并型（旧常量族 -> 新表逐行模板）\n\n")
    for key, consts in sorted(d1_groups.items()):
        lines = joined2lines[key]
        w(f"**合并原文**：{key}\n\n")
        w(f"- 旧常量（{len(consts)}）：" + ", ".join(f"`{c}`" for c in sorted(consts)) + "\n")
        w(f"- 新表逐行模板（{len(lines)}）：\n")
        for l in lines:
            hit = to_name(l) if l in new_text else None
            w(f"    - {'`'+hit+'`' if hit else '（新表缺失）'} — {l}\n")
        w("\n")

    w("---\n\n## D2 已剔除语料型（升华/专精/不可达节点）\n\n")
    w("| 旧常量 | 规范化 stat | 被引用 |\n|---|---|---|\n")
    for const, eid, comment, key in sorted(d2):
        w(f"| `{const}` | {key} | {'是' if const in refs else '否'} |\n")

    w("\n---\n\n## D3 自有引擎 stat（原样保留）\n\n")
    w("| 旧常量 | 注释 | 归因 |\n|---|---|---|\n")
    for const, eid, comment, why in sorted(d3):
        w(f"| `{const}` | {comment} | {why} |\n")

print(f"D1 多行合并型   : {len(d1)}  （涉及 {len(d1_groups)} 个多行块）")
print(f"D2 已剔除语料型 : {len(d2)}")
print(f"D3 自有引擎 stat: {len(d3)}")
print(f"合计: {len(d1)+len(d2)+len(d3)}")
print(f"\n报告: {OUT}")
