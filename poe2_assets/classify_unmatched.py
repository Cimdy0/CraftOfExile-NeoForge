# -*- coding: utf-8 -*-
"""第 5 步（1.5/2）：对 §B 失配清单做**非模糊**分层，把 594 条压成少数几类决策。

判据（全部为确定性字符串判定，无相似度/无近似）：
  B1 自有引擎 stat：注释仅是常量名的 Title-Case 回译（to_name(注释) == 常量名）。
     说明该条目从未承载语料文本，属模组自有子系统（技能/状态/物品/MC 原生属性），
     与天赋树语料无关 —— 应原样保留。
  B2 语料型失配：注释含数字或 POE 语料特征（{X}/%/to/per 等），是真实 stat 文本，
     但规范化后在新表中无逐字符相等项 —— 须逐条裁决。
  B3 其他：既非回译也无语料特征。

进一步把 B2 按「新表中是否存在同 kebab 词集的条目」拆成：
  B2a 新表有词集完全相同的条目（仅语序/虚词差异）-> 高置信候选，仍需人工确认
  B2b 新表无任何同词集条目 -> 语料中确实不存在，属模组自有或已废弃
"""
import re
from collections import defaultdict

OLD = "poe2_assets/_backup/stat/StatIds.java.old"
NEW = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
REFS = "poe2_assets/_test/referenced_consts.txt"
OUT = "poe2_assets/_test/unmatched_classified.md"

import importlib.util
spec = importlib.util.spec_from_file_location("m", "poe2_assets/map_old_stats.py")
m = importlib.util.module_from_spec(spec)
spec.loader.exec_module(m)


def to_name(t):
    s = t.replace("{X}", "X").lower()
    s = re.sub(r"[%+\-•]", " ", s)
    s = re.sub(r"[^a-z0-9 ]", " ", s)
    return "_".join(w.upper() for w in s.split() if w)


def wordset(t):
    s = t.replace("{X}", "X").lower()
    s = re.sub(r"[^a-z0-9 ]", " ", s)
    return frozenset(w for w in s.split() if w)


old = m.parse(OLD)
new = m.parse(NEW)
refs = {l.strip() for l in open(REFS, encoding="utf-8") if l.strip()}

new_by_text = defaultdict(list)
new_by_ws = defaultdict(list)
for const, eid, comment in new:
    new_by_text[comment].append(const)
    new_by_ws[wordset(comment)].append((const, comment))

CORPUS_HINT = re.compile(r"\d|\{X\}|%")

b1, b2a, b2b, b3 = [], [], [], []
for const, eid, comment in old:
    if not comment:
        b3.append((const, eid, comment, ""))
        continue
    key = m.normalize(comment)
    if new_by_text.get(key):
        continue  # 已在 §A 精确命中
    if to_name(comment) == const:
        b1.append((const, eid, comment, key))
    elif CORPUS_HINT.search(comment):
        cands = new_by_ws.get(wordset(key), [])
        (b2a if cands else b2b).append((const, eid, comment, key, cands))
    else:
        b3.append((const, eid, comment, key))

with open(OUT, "w", encoding="utf-8") as f:
    w = f.write
    w("# §B 失配清单分层（594 条 -> 4 类决策）\n\n")
    w("| 类别 | 数量 | 判据 | 建议处置 |\n|---|---|---|---|\n")
    w(f"| B1 自有引擎 stat | {len(b1)} | 注释仅为常量名回译，无语料文本 | **原样保留** |\n")
    w(f"| B2a 语料型·有同词集候选 | {len(b2a)} | 新表存在词集相同条目 | 逐条确认改名 |\n")
    w(f"| B2b 语料型·无候选 | {len(b2b)} | 新表无同词集条目 | 保留或废弃 |\n")
    w(f"| B3 其他 | {len(b3)} | 无注释 / 无语料特征 | 逐条裁决 |\n\n")

    w("---\n\n## B2a 语料型失配 · 新表有同词集候选（逐条确认）\n\n")
    w("| 旧常量 | 旧注释规范化 | 新表候选 |\n|---|---|---|\n")
    for const, eid, comment, key, cands in sorted(b2a):
        c = "<br>".join(f"`{a}` — {b}" for a, b in cands)
        w(f"| `{const}` | {key} | {c} |\n")

    w("\n---\n\n## B3 其他（逐条裁决）\n\n")
    w("| 旧常量 | 旧注释 | 规范化 |\n|---|---|---|\n")
    for const, eid, comment, key in sorted(b3):
        w(f"| `{const}` | {comment} | {key} |\n")

    w("\n---\n\n## B2b 语料型失配 · 新表无候选\n\n")
    w("| 旧常量 | 旧注释 | 规范化 |\n|---|---|---|\n")
    for const, eid, comment, key, _ in sorted(b2b):
        w(f"| `{const}` | {comment} | {key} |\n")

    w("\n---\n\n## B1 自有引擎 stat（建议原样保留）\n\n")
    for const, eid, comment, key in sorted(b1):
        w(f"- `{const}` — {comment}\n")

print(f"B1 自有引擎 stat（回译，保留）: {len(b1)}")
print(f"B2a 语料型·有同词集候选      : {len(b2a)}")
print(f"B2b 语料型·无候选            : {len(b2b)}")
print(f"B3 其他                      : {len(b3)}")
print(f"合计: {len(b1)+len(b2a)+len(b2b)+len(b3)}")
print(f"\n报告: {OUT}")
