#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
同 EFF 兄弟漏接扫描（反向视角）。

背景：正向「按 StatId 逐条找 EFF」会漏掉大量同义异写的语料（PoE2 同一机制常有多条措辞）。
反向做法：先把「已接 StatId → 使用的 EFF」建表，再对每条未接 StatId 按词元相似度
找最相近的已接兄弟，直接给出「兄弟用了哪个 EFF」的候选，供人工逐个核验 R3 读点。

用法：
    python scripts/sibling_scan.py            # 全量，按相似度输出前 N 候选
    python scripts/sibling_scan.py --min 0.5  # 提高相似度阈值
    python scripts/sibling_scan.py --eff EFF_ADD_VALUE_ATTRIBUTE_COE_ACCURACY   # 只看某 EFF 的已接兄弟

输出：纯文本报告，不改任何文件。
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys
from collections import defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WIRING_DIR = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat", "wirings")
UNWIRED_JSON = os.path.join(ROOT, "scripts", "_unwired.json")

PUT_RE = re.compile(r"map\.put\(\s*StatIds\.([A-Z0-9_]+)\s*,")
EFF_RE = re.compile(r"StatEffectIds\.(EFF_[A-Z0-9_]+)")
COND_RE = re.compile(r"StatConditionIds\.(COND_[A-Z0-9_]+)")

# 词元归一化：这些词几乎不携带区分信息（「提高/降低/of/to/with...」），参与相似只会制造噪声。
STOP = {
    "A", "AN", "THE", "OF", "TO", "WITH", "AND", "OR", "FOR", "ON", "IN", "AT", "BY", "IS", "ARE",
    "IF", "WHEN", "WHILE", "YOU", "YOUR", "HAVE", "HAS", "HAVE", "BEEN", "BE", "THAT", "THEY",
    "ENEMY", "ENEMIES", "TARGET", "TARGETS", "SKILL", "SKILLS", "DAMAGE", "HIT", "HITS",
    "INC", "ADD", "MORE", "DECREASE", "REDUCED", "GAIN", "GRANT", "PER", "UP", "MAX", "MINIMUM",
    "EACH", "ALL", "ANY", "NOT", "NO", "CAN", "CANNOT", "FROM", "AS", "THAN", "THEIR", "ITS",
}


def parse_wirings() -> dict[str, dict]:
    """解析 wirings/ 下全部 map.put(StatIds.X, ...) 条目，提取其引用的 EFF / COND。"""
    out: dict[str, dict] = {}
    for fn in sorted(os.listdir(WIRING_DIR)):
        if not fn.endswith(".java"):
            continue
        path = os.path.join(WIRING_DIR, fn)
        with open(path, encoding="utf-8") as f:
            src = f.read()
        for m in PUT_RE.finditer(src):
            key = m.group(1)
            # 从 map.put( 的 '(' 开始做括号配平，取完整条目体（StatBuilder 链是多行）。
            depth = 0
            i = m.end() - 1  # 指向 StatIds.X 后的逗号前的 '(' 对应位置：回退到最近的 '('
            start = src.rfind("(", 0, m.end())
            j = start
            while j < len(src):
                if src[j] == "(":
                    depth += 1
                elif src[j] == ")":
                    depth -= 1
                    if depth == 0:
                        break
                j += 1
            body = src[start:j + 1]
            rec = out.setdefault(key, {"effs": set(), "conds": set(), "file": fn})
            rec["effs"].update(EFF_RE.findall(body))
            rec["conds"].update(COND_RE.findall(body))
    return out


def toks(name: str) -> set[str]:
    return {t for t in name.split("_") if t and t not in STOP}


def jaccard(a: set[str], b: set[str]) -> float:
    if not a or not b:
        return 0.0
    return len(a & b) / len(a | b)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--min", type=float, default=0.45, help="相似度阈值（默认 0.45）")
    ap.add_argument("--top", type=int, default=2, help="每条未接 StatId 输出的候选兄弟数")
    ap.add_argument("--eff", default=None, help="只打印某 EFF 的已接兄弟（调试用）")
    ap.add_argument("--limit", type=int, default=60, help="最多输出多少条未接 StatId")
    args = ap.parse_args()

    wired = parse_wirings()
    if not os.path.exists(UNWIRED_JSON):
        print("缺少 scripts/_unwired.json，请先运行 python scripts/analyze_unwired.py", file=sys.stderr)
        return 1
    with open(UNWIRED_JSON, encoding="utf-8") as f:
        unwired = json.load(f)
    # scripts/_unwired.json 是 analyze_unwired.py 落盘的**快照**：落线后若不重跑，它会继续把
    # 已接条目当成未接候选（曾出现「刚接完 DEAL_NO_NON_FIRE_DAMAGE，重跑仍列它为 1.00 候选」）。
    # 这里以 parse_wirings() 的实时结果为准做一次差集，保证「接完立刻重跑」不会自相矛盾。
    stale = sorted(k for k in unwired if k in wired)
    unwired_names = sorted(k for k in unwired if k not in wired)
    if stale:
        print(f"[警告] _unwired.json 已过时：{len(stale)} 条实际已接线，已按实时 wirings/ 剔除 —— "
              f"例如 {', '.join(stale[:5])}。建议重跑 python scripts/analyze_unwired.py",
              file=sys.stderr)

    if args.eff:
        print(f"=== 已接 StatId（使用 {args.eff}）===")
        hits = [k for k, v in sorted(wired.items()) if args.eff in v["effs"]]
        for k in hits:
            print("   ", k, f"({wired[k]['file']})")
        print(f"共 {len(hits)} 条")
        return 0

    print(f"已接条目 {len(wired)} 条；未接 {len(unwired_names)} 条；阈值 {args.min}\n")

    # 预算已接条目的词元，避免 O(n*m) 反复 split
    wired_toks = [(k, toks(k)) for k in wired]
    rows = []
    for u in unwired_names:
        tu = toks(u)
        scored = []
        for k, tk in wired_toks:
            s = jaccard(tu, tk)
            if s >= args.min:
                scored.append((s, k))
        scored.sort(key=lambda x: (-x[0], x[1]))
        if scored:
            rows.append((u, scored[: args.top]))

    # 相似度越高越可能是「同义兄弟」，排前面
    rows.sort(key=lambda r: -r[1][0][0])

    shown = 0
    for u, cands in rows:
        if shown >= args.limit:
            break
        parts = []
        for s, k in cands:
            effs = sorted(wired[k]["effs"])
            eff = effs[0] if effs else "(无 EFF)"
            extra = f" +{len(effs) - 1}" if len(effs) > 1 else ""
            conds = sorted(wired[k]["conds"])
            c = f"+{conds[0]}" if conds else ""
            parts.append(f"{s:.2f} ~ {k}  →  {eff}{extra}{c}")
        print(f"{u}")
        for p in parts:
            print(f"      {p}")
        shown += 1

    print(f"\n共 {len(rows)} 条未接 StatId 存在 ≥{args.min} 的已接兄弟（显示 {shown} 条）。")
    print("注意：相似度只是候选，**必须逐条人工核验 R3 真读点后再接线**。")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
