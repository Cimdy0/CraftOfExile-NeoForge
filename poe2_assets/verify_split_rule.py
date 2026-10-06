# -*- coding: utf-8 -*-
"""校验修正后的换行拆分判据，逐条打印全部 44 条多行 stat 的拆分结果。

判据（确定性，无相似度）：
  对多行 stat 自上而下扫描，每行 strip 后：
    - 首字符为小写字母        -> 软折行续行，合并到上一行（POE 为 UI 宽度插入的换行）
    - 首字符为 '•'            -> 项目符号子句，合并到上一行（表头 + 子句属同一条逻辑 stat）
    - 其余（大写/数字/+/-/[）  -> 独立 stat，另起一条
"""
import json

CLEAN = "poe2_assets/poe2_tree_clean.json"


def split_stat(stat):
    """把一条原始 stat 文本切成若干独立 stat（修正软折行与项目符号）。"""
    out = []
    for raw in stat.split("\n"):
        line = raw.strip()
        if not line:
            continue
        cont = line[0].islower() or line[0] == "\u2022"
        if cont and out:
            out[-1] = out[-1] + " " + line
        else:
            out.append(line)
    return out


clean = json.load(open(CLEAN, encoding="utf-8"))

blocks = {}
for nid, n in clean["nodes"].items():
    for stat in (n.get("stats") or []):
        if "\n" in stat:
            blocks.setdefault(stat, 0)
            blocks[stat] += 1

print(f"含换行的唯一 stat 块: {len(blocks)}\n")
n_join = n_split = 0
for i, (stat, cnt) in enumerate(sorted(blocks.items()), 1):
    parts = split_stat(stat)
    raw_lines = len([l for l in stat.split("\n") if l.strip()])
    kind = "合并" if len(parts) < raw_lines else "全拆"
    if len(parts) < raw_lines:
        n_join += 1
    else:
        n_split += 1
    print(f"--- #{i}  原始 {raw_lines} 行 -> {len(parts)} 条  [{kind}]  (出现 {cnt} 次)")
    for l in stat.split("\n"):
        print(f"    原: {l}")
    for p in parts:
        print(f"    => {p}")
print(f"\n发生合并的块: {n_join}   全拆的块: {n_split}")
