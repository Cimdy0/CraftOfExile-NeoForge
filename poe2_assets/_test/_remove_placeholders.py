#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""移除静默死占位属性 (REMOVE 批次):
   - AttributeRegister.java: 删属性声明行
   - StatGen.java: 删引用该属性的条目行 (helper 形式, 单行)
   - 删 src/generated + build 下对应 stat_entry/<id>.json
   - 保留 StatIds 常量(预留词表)
   执行前 DRY=1 打印待删行; DRY=0 实际改写。
"""
import re, os, json, pathlib, sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC = os.path.join(ROOT, "src", "main", "java")
STATGEN = os.path.join(SRC, "com", "cimdy", "coe", "data", "gen", "stat", "StatGen.java")
ATTR = os.path.join(SRC, "com", "cimdy", "coe", "data", "AttributeRegister.java")

cls = json.load(open(os.path.join(os.path.dirname(__file__), "normalize_classify.json"), encoding="utf-8"))
remove = cls["remove"]
attrs = [r["attr"] for r in remove]
ids = [i for r in remove for i in r["ids"]]

DRY = os.environ.get("DRY", "1") == "1"

# 1) AttributeRegister property lines
attr_text = open(ATTR, encoding="utf-8").read().splitlines()
attr_lines_to_del = []
for idx, line in enumerate(attr_text):
    for at in attrs:
        if re.search(r'public static Holder<Attribute>\s+' + re.escape(at) + r'\s*=', line):
            attr_lines_to_del.append((idx, at, line.strip()))
            break

# 2) StatGen entry lines (helper form, 可能多行: 删到以 ';' 结尾的续行)
sg_text = open(STATGEN, encoding="utf-8").read().splitlines()
sg_ranges_to_del = []
sg_meta = []
for idx, line in enumerate(sg_text):
    for at in attrs:
        if re.search(r'AttributeRegister\.' + re.escape(at) + r'\b', line):
            if re.search(r'\b(?:inc|add|pctAdd|more|chance|faceMod|valueMod|entityMod|regenMod|dtcArmour|dtcReroute|esRechargeMod|addChance)\(map', line):
                # 计算删除区间 [idx, end] (end=首个以 ';' 结尾的后续行)
                end = idx
                while end < len(sg_text) and not sg_text[end].rstrip().endswith(';'):
                    end += 1
                sg_ranges_to_del.append((idx, end))
                sg_meta.append((idx, at, line.strip()))
            break
sg_lines_to_del = sg_meta

print(f"[DRY={DRY}] REMOVE 属性数: {len(attrs)}, 涉及 StatIds id 数: {len(ids)}")
print(f"AttributeRegister 待删行: {len(attr_lines_to_del)}")
for idx, at, l in attr_lines_to_del[:5]:
    print(f"   L{idx+1} [{at}] {l[:90]}")
if len(attr_lines_to_del) > 5: print(f"   ... 共 {len(attr_lines_to_del)}")
print(f"StatGen 待删条目区间: {len(sg_ranges_to_del)}")
for k in range(min(8, len(sg_ranges_to_del))):
    a, b = sg_ranges_to_del[k]
    idx, at, l = sg_lines_to_del[k]
    print(f"   L{a+1}-{b+1} [{at}] {l.strip()[:90]}")
if len(sg_ranges_to_del) > 8: print(f"   ... 共 {len(sg_ranges_to_del)}")

# 3) stale json
json_to_del = []
for base in [os.path.join(ROOT, "src", "generated", "resources", "data", "coe", "coe", "stat_entry"),
             os.path.join(ROOT, "build", "resources", "main", "data", "coe", "coe", "stat_entry")]:
    if not os.path.isdir(base): continue
    for i in ids:
        p = os.path.join(base, i.lower() + ".json")
        if os.path.exists(p):
            json_to_del.append(p)
print(f"陈旧 stat_entry json 待删: {len(json_to_del)}")
for p in json_to_del[:5]:
    print(f"   {os.path.relpath(p, ROOT)}")
if len(json_to_del) > 5: print(f"   ... 共 {len(json_to_del)}")

if DRY:
    print("\n[DRY] 未改写任何文件。设 DRY=0 执行。")
    sys.exit(0)

# 执行: 仅删 StatGen 条目 (消除静默死接线面); 保留 AttributeRegister 属性 + StatIds 常量(预留, 落地但空)
# AttributeRegister 属性保留 (约定: 机制未实现 -> 数据落地但置空)
# StatGen (按区间删, 含多行)
del_ranges = sg_ranges_to_del
keep2 = []
skip = set()
for a, b in del_ranges:
    skip.update(range(a, b + 1))
keep2 = [l for j, l in enumerate(sg_text) if j not in skip]
open(STATGEN, "w", encoding="utf-8").write("\n".join(keep2) + "\n")
for p in json_to_del:
    try: os.remove(p)
    except OSError: pass
print(f"\n[DONE] 已删 StatGen {len(del_ranges)} 条目区间, json {len(json_to_del)} 个。")
print("AttributeRegister 属性 + StatIds 常量保留为预留(落地但空, 待补引擎 reader)。")
