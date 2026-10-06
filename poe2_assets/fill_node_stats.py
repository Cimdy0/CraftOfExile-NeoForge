#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""把天赋节点的原生 [Stat] 描述文本 → StatIds 授权，回填进 TalentNodeDef.of 第 6 参（StatList）。

数据源（非 JSON 反推，而是沿原流水线）：
  poe2_tree_clean.json (nodes[].stats[] 原生文本)
    -> node_stat_map.json 记录的 raw/template/path/constant/values
       （path 由 gen_stat_ids.py 的 to_id() 算出，与 StatIds.java 同源）
    -> 本脚本只做「按 node id 取回已记录的 path/constant/values」并落进节点生成代码。

取值口径（用户 2026-09-15 裁定）：用语料原值（"75 Spirit" -> 75.0，不做 /100）。
跳过规则：
  1. path 不在 StatIds.java，或无对应 stat/entry 产物文件 -> 跳过（防悬空引用）
  2. entry 的 amount 表达式引用 $per / $cap -> 跳过（TalentNodeGen.stat(Map) 只能给单个 value）
"""
import os
import re
import sys
import json
import glob

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
NODEDIR = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/talent/node")
STATIDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
ENTRYDIR = os.path.join(ROOT, "src/generated/resources/data/coe/coe/stat/entry")
MAP = os.path.join(ROOT, "poe2_assets/node_stat_map.json")

REQUIRED_IMPORTS = [
    "import com.cimdy.coe.data.codec.support.stat.StatIds;",
    "import com.cimdy.coe.data.gen.talent.TalentNodeGen;",
    "import java.util.Map;",
]

APPLY = "--apply" in sys.argv


def load_catalog():
    txt = open(STATIDS, encoding="utf-8").read()
    const_by_path = {p: n for n, p in
                     re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\)', txt)}
    entries = {os.path.basename(p)[:-5] for p in glob.glob(os.path.join(ENTRYDIR, "*.json"))}
    nmap = json.load(open(MAP, encoding="utf-8"))
    return const_by_path, entries, nmap


def amount_expr(path):
    """取 entry 的 amount 表达式字符串；无则 None。"""
    f = os.path.join(ENTRYDIR, path + ".json")
    if not os.path.isfile(f):
        return None
    j = json.load(open(f, encoding="utf-8"))
    amt = j.get("amount")
    if isinstance(amt, dict):
        v = amt.get("value")
        return str(v) if v is not None else None
    return str(amt) if amt is not None else None


def build_stats(nid, const_by_path, entries):
    """返回 [(常量名, 数值, 原始描述)]；不可表达者跳过。"""
    out = []
    recs = NMAP.get(nid, []) if nid else []
    for r in recs:
        path = r.get("path")
        if not path or path not in const_by_path or path not in entries:
            continue
        expr = amount_expr(path)
        if expr is not None and ("$per" in expr or "$cap" in expr):
            continue
        vals = r.get("values") or []
        if vals:
            v = vals[0].get("value")
            if v is None:
                continue
        else:
            v = 1.0  # 纯开关型（如 "Deal no Non-Fire Damage"），与 keystone 写法一致
        out.append((const_by_path[path], float(v), r.get("raw") or r.get("template") or ""))
    seen, ded = set(), []
    for c, v, t in out:
        if c in seen:
            continue
        seen.add(c)
        ded.append((c, v, t))
    return ded


def jnum(v):
    s = repr(float(v))
    return s if ("." in s or "e" in s) else s + ".0"


def render(stats):
    if not stats:
        return None
    lines = ["        List.of(TalentNodeGen.stat(Map.of("]
    for i, (c, v, _t) in enumerate(stats):
        tail = "," if i < len(stats) - 1 else ""
        lines.append("                StatIds.%s, %s%s" % (c, jnum(v), tail))
    lines.append("        ))),")
    return "\n".join(lines)


def ensure_imports(lines):
    """补齐缺失 import，插到 package 行之后。返回新 lines。"""
    have = set(l.strip() for l in lines if l.strip().startswith("import "))
    missing = [i for i in REQUIRED_IMPORTS if i not in have]
    if not missing:
        return lines, 0
    out = list(lines)
    idx = 0
    for i, l in enumerate(out):
        if l.strip().startswith("package "):
            idx = i + 1
            break
    for m in reversed(missing):
        out.insert(idx, m + "\n")
    return out, len(missing)


def process_file(path, const_by_path, entries):
    src = open(path, encoding="utf-8", errors="replace").read()
    lines = src.split("\n")
    stats_total = nodes_filled = 0
    i = 0
    while i < len(lines):
        if lines[i].strip() == "List.of(),":
            # 确认是 TalentNodeDef.of( 的第 6 参：向前找 of(，向后确认 links
            j = i - 1
            anchor = -1
            while j >= 0 and i - j < 8:
                if "TalentNodeDef.of(" in lines[j]:
                    anchor = j
                    break
                j -= 1
            nxt = lines[i + 1].strip() if i + 1 < len(lines) else ""
            if anchor < 0 or not nxt.startswith("List.of("):
                i += 1
                continue
            m = None
            for k in range(anchor, min(anchor + 3, len(lines))):
                m = re.search(r'"(\d+)"', lines[k])
                if m:
                    break
            if not m:
                i += 1
                continue
            nid = m.group(1)
            stats = build_stats(nid, const_by_path, entries)
            if stats:
                lines[i] = render(stats)
                nodes_filled += 1
                stats_total += len(stats)
            i += 1
        else:
            i += 1
    new_lines, added = ensure_imports(lines)
    return "\n".join(new_lines), nodes_filled, stats_total, added


def main():
    global NMAP
    const_by_path, entries, NMAP = load_catalog()
    print("StatIds constants: %d   entry files: %d" % (len(const_by_path), len(entries)))
    tot_files = tot_nodes = tot_stats = tot_imports = 0
    for cat in ["notable", "minor"]:
        for f in sorted(glob.glob(os.path.join(NODEDIR, cat, "*.java"))):
            new, filled, nstats, added = process_file(f, const_by_path, entries)
            tot_files += 1
            tot_nodes += filled
            tot_stats += nstats
            tot_imports += added
            if APPLY and filled:
                open(f, "w", encoding="utf-8", newline="\n").write(new)
            print("  %-22s filled=%4d stats=%5d imports_added=%d"
                  % (os.path.basename(f), filled, nstats, added))
    print("TOTAL files=%d nodes_filled=%d stats_written=%d imports_added=%d"
          % (tot_files, tot_nodes, tot_stats, tot_imports))
    print("MODE:", "APPLY" if APPLY else "DRY-RUN (no files written)")


if __name__ == "__main__":
    main()
