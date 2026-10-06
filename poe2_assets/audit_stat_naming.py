# -*- coding: utf-8 -*-
"""
天赋树 Stat 命名 / 统计错漏 —— 全量审计（基石 + 非基石 一体看待）

唯一权威判据：`poe2_assets/node_stat_map.json`
  每条记录 = {raw(语料原文), template(数字模板化), path(文本转写的 StatId),
              constant, has_entry, values[]}
  它已经按 \\n 把「一个数组元素内的多条 mod」切开，且 path **不含节点名**。
因此：节点类里实际写入的 StatId path 必须 == 该节点那条描述对应的 path。

检查项：
  A. 命名错：节点类写入的 path != 语料转写 path
  B. 命名污染：写入的 path 以「该节点名（蛇形）」开头
  C. 孤儿 id：path 不出现在 node_stat_map 的全集里（= 不是从描述文本转写出来的）
  D. 漏：节点的描述没有任何 StatId 写入
  E. 拆/多：一个节点写入的 StatId 数 > 描述条数
只读脚本，不改动任何代码。
"""
import json
import os
import re
from collections import defaultdict, Counter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "poe2_assets")
JAVA = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe")
NODE_DIR = os.path.join(JAVA, "data", "gen", "talent", "node")


def snake(s):
    s = re.sub(r"[^A-Za-z0-9]+", "_", s or "")
    return re.sub(r"_+", "_", s).strip("_").lower()


# ---------------------------------------------------------------- 载入
tree = json.load(open(os.path.join(ASSETS, "poe2_tree_clean.json"), encoding="utf-8"))
nodes = tree.get("nodes", tree)
stat_map = json.load(open(os.path.join(ASSETS, "node_stat_map.json"), encoding="utf-8"))

src_ids = open(os.path.join(JAVA, "data", "codec", "support", "stat", "StatIds.java"),
               encoding="utf-8").read()
CONST2PATH = dict(re.findall(
    r"public static final Identifier\s+([A-Z0-9_]+)\s*=\s*id\(\"([^\"]+)\"\)", src_ids))
PATH2CONST = {p: c for c, p in CONST2PATH.items()}

# ---------------------------------------------------------------- 解析节点类
# 形如：
#   list.add(TalentNodeDef.of(
#           "10277", 7790.97f, 739.331f,
#           "accuracy", "passives_accuracydex",
#   List.of(TalentNodeGen.stat(Map.of(
#           StatIds.INC_ACCURACY, 0.08
#   ))),
#           List.of("35477", "64064")
#   ));
DEF_RE = re.compile(
    r'TalentNodeDef\.of\(\s*\n?\s*"(\d+)"[^"]*"[^"]*",\s*"([^"]*)"\s*,\s*'
    r'([\s\S]*?)\n\s*List\.of\(', re.M)

node_wired = {}   # nodeId -> [const,...]
node_name = {}    # nodeId -> name
node_file = {}    # nodeId -> file

for sub in ("keystone", "notable", "minor"):
    d = os.path.join(NODE_DIR, sub)
    if not os.path.isdir(d):
        continue
    for fn in sorted(os.listdir(d)):
        if not fn.endswith(".java"):
            continue
        s = open(os.path.join(d, fn), encoding="utf-8").read()
        for m in DEF_RE.finditer(s):
            nid, nm, body = m.group(1), m.group(2), m.group(3)
            node_name[nid] = nm
            node_file[nid] = f"{sub}/{fn}"
            consts = re.findall(r"StatIds\.([A-Z0-9_]+)", body)
            node_wired[nid] = consts

# ---------------------------------------------------------------- 期望全集
expected_paths = set()
for recs in stat_map.values():
    for r in recs:
        if r.get("path"):
            expected_paths.add(r["path"])

# ---------------------------------------------------------------- 逐节点比对
mismatch = []     # A
contam = []       # B
orphan = Counter()  # C
gaps = []         # D
splits = []       # E

for nid, recs in stat_map.items():
    if not recs:
        continue
    nd = nodes.get(nid, {})
    shown = nd.get("name") or node_name.get(nid) or "?"
    wired = node_wired.get(nid, [])
    wired_paths = [CONST2PATH.get(c, "?" + c) for c in wired]
    exp = [(r.get("raw"), r.get("path")) for r in recs]

    # C: 孤儿 id
    for wp in wired_paths:
        if wp not in expected_paths:
            orphan[wp] += 1

    # B: 污染（path 以本节点名开头）
    pref = snake(nd.get("name") or "")
    if pref:
        for wp in wired_paths:
            if wp.startswith(pref + "_"):
                contam.append((nid, shown, wp))

    # A: 逐条比对（按序）
    if wired_paths and exp:
        exp_paths = [p for _, p in exp]
        if wired_paths != exp_paths:
            mismatch.append((nid, shown, node_file.get(nid, "?"), exp, wired_paths))

    # D: 漏
    if not wired:
        gaps.append((nid, shown, [r.get("raw") for r in recs]))

    # E: 拆/多
    if wired and len(wired) > len(recs):
        splits.append((nid, shown, len(recs), len(wired),
                       [r.get("raw") for r in recs], wired_paths))

# ---------------------------------------------------------------- 报告
out = []
A = out.append
A("# 天赋树 Stat 命名 / 统计错漏 —— 全量审计")
A("")
A("判据：`poe2_assets/node_stat_map.json`（语料原文 → 文本转写 path，已按 \\n 切行，不含节点名）")
A("")
A("| 指标 | 值 |")
A("|---|---|")
A(f"| 天赋节点（clean.json） | {len(nodes)} |")
A(f"| node_stat_map 覆盖节点 | {len(stat_map)} |")
A(f"| 节点类解析出的节点 | {len(node_wired)} |")
A(f"| 语料描述记录总数 | {sum(len(v) for v in stat_map.values())} |")
A(f"| 已写入 StatId 总数 | {sum(len(v) for v in node_wired.values())} |")
A(f"| 语料转写 path 去重数 | {len(expected_paths)} |")
A(f"| StatIds 常量数 | {len(CONST2PATH)} |")
A("")
A(f"**A 命名错 {len(mismatch)} · B 污染 {len(contam)} · C 孤儿 id {len(orphan)} · "
  f"D 漏 {len(gaps)} · E 拆/多 {len(splits)}**")

A("")
A("## A. 命名错（写入 path != 语料转写 path）")
A("")
if mismatch:
    for nid, nm, f, exp, wired in mismatch[:200]:
        A(f"- `{nid}` **{nm}**  `{f}`")
        for r, p in exp:
            A(f"    - 语料: `{r}` → 应为 `{p}`")
        A(f"    - 实写: {wired}")
else:
    A("无。")

A("")
A("## B. 命名污染（path 以节点名开头）")
A("")
if contam:
    A("| 节点 id | 节点名 | 被污染的 path |")
    A("|---|---|---|")
    for nid, nm, wp in sorted(set(contam)):
        A(f"| `{nid}` | {nm} | `{wp}` |")
else:
    A("无。")

A("")
A("## C. 孤儿 id（不来自任何描述文本的转写）")
A("")
if orphan:
    A("| path | 被引用次数 |")
    A("|---|---|")
    for wp, c in orphan.most_common():
        A(f"| `{wp}` | {c} |")
else:
    A("无。")

A("")
A("## D. 漏（节点有描述但没写 StatId）")
A("")
if gaps:
    A(f"共 {len(gaps)} 个节点：")
    for nid, nm, raws in gaps[:100]:
        A(f"- `{nid}` **{nm}**")
        for r in raws:
            A(f"    - `{r}`")
else:
    A("无。")

A("")
A("## E. 拆/多（写入条数 > 语料描述条数）")
A("")
if splits:
    for nid, nm, nr, nw, raws, wired in splits:
        A(f"- `{nid}` **{nm}** — 语料 {nr} 条 / 写入 {nw} 条")
        for r in raws:
            A(f"    - 语料: `{r}`")
        A(f"    - 写入: {wired}")
else:
    A("无。")

rep = os.path.join(ASSETS, "AUDIT_STAT_NAMING.md")
open(rep, "w", encoding="utf-8").write("\n".join(out) + "\n")
print("written:", rep)
print(f"A命名错={len(mismatch)} B污染={len(set(contam))} C孤儿={len(orphan)} "
      f"D漏={len(gaps)} E拆={len(splits)}")
print(f"解析节点={len(node_wired)} 写入StatId={sum(len(v) for v in node_wired.values())}")
