#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""属性层根因分类 (纯 python, 无 subprocess):
   对每个 AttributeRegister 属性, 用三层证据判定:
     (1) consumed  : src 中 AttributeRegister.<X> 引用 (排除定义 + StatGen)
     (2) node_refs: 其 StatGen 条目 StatIds.<ID> 在 talent gen 目录被引用
     (3) json_refs: 其 id 字符串(snake) 在 src/generated + src/main/resources 被引用
   分类:
     IMPLEMENT : 核心机制(CORE_KEEP) 或 node_refs>0 (节点接到死 stat, 需补 reader 或禁用节点)
     REMOVE    : consumed==0 AND node_refs==0 AND json_refs==0 (静默死, 可清; StatIds 留预留)
   核心保护集避免误删 AREA_OF_EFFECT/DEFEND_ARMOUR 等。
"""
import re, os, json, pathlib

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC = os.path.join(ROOT, "src", "main", "java")
GEN = os.path.join(SRC, "com", "cimdy", "coe", "data", "gen")
STATGEN = os.path.join(GEN, "stat", "StatGen.java")
ATTR = os.path.join(SRC, "com", "cimdy", "coe", "data", "AttributeRegister.java")
TALENT = os.path.join(GEN, "talent")

CORE_KEEP = {  # 中央机制: 即便当前 consumed==0, 也判定为 IMPLEMENT(需补 reader), 不删
    "AREA_OF_EFFECT", "DEFEND_ARMOUR", "DAMAGE", "TAKEN_DAMAGE",
}

# 1) parse attributes
attrs = {}
pat = re.compile(r'public static Holder<Attribute>\s+([A-Za-z0-9_]+)\s*=\s*register(Percentage|Double|Flat)?\(\s*"([^"]+)"\s*,\s*([0-9.]+)')
text = open(ATTR, encoding="utf-8").read()
for m in pat.finditer(text):
    attrs[m.group(1)] = {"id": m.group(3), "base": float(m.group(4))}

# 2) consumed via pure python
java_files = [str(p) for p in pathlib.Path(SRC).rglob("*.java")]
consumed = {}
for n in attrs:
    pn = re.compile(r"AttributeRegister\." + re.escape(n) + r"\b")
    cnt = 0
    for fp in java_files:
        if fp.endswith("AttributeRegister.java") or fp.endswith("StatGen.java"):
            continue
        try:
            with open(fp, encoding="utf-8", errors="ignore") as fh:
                if any(pn.search(l) for l in fh):
                    cnt += 1
        except Exception:
            pass
    consumed[n] = cnt

# 3) StatGen entries (ID -> ATTR)
entry_re = re.compile(r'\b(?:inc|add|pctAdd|more|chance|faceMod|valueMod|entityMod|regenMod|dtcArmour|dtcReroute|esRechargeMod)\(\s*map\s*,\s*StatIds\.(\w+)\s*,\s*AttributeRegister\.(\w+)')
sg = open(STATGEN, encoding="utf-8").read()
entries = entry_re.findall(sg)
attr_to_ids = {}
id_to_attr = {}
for idn, at in entries:
    attr_to_ids.setdefault(at, []).append(idn)
    id_to_attr[idn] = at

# 4) node_refs + json_refs via pure python
talent_files = [str(p) for p in pathlib.Path(TALENT).rglob("*.java")]
res_files = [str(p) for p in pathlib.Path(os.path.join(ROOT, "src", "generated", "resources")).rglob("*.json")]
res_files += [str(p) for p in pathlib.Path(os.path.join(ROOT, "src", "main", "resources")).rglob("*.json")]

def count_refs(token, files):
    cnt = 0
    for fp in files:
        try:
            with open(fp, encoding="utf-8", errors="ignore") as fh:
                if any(token in l for l in fh):
                    cnt += 1
        except Exception:
            pass
    return cnt

# 5) classify
implement, remove = [], []
for at, info in sorted(attrs.items()):
    ids = attr_to_ids.get(at, [])
    node_refs = sum(count_refs("StatIds." + i, talent_files) for i in ids)
    json_refs = count_refs('"' + info["id"] + '"', res_files) if ids else 0
    if at in CORE_KEEP:
        implement.append((at, ids, "CORE_KEEP", consumed[at], node_refs, json_refs))
    elif node_refs > 0:
        implement.append((at, ids, "node_dangling", consumed[at], node_refs, json_refs))
    elif consumed[at] == 0 and node_refs == 0 and json_refs == 0:
        remove.append((at, ids, consumed[at], node_refs, json_refs))
    # else: 有消费 或 json 引用 -> 保留(正常), 不列入两类

print(f"属性总数: {len(attrs)}")
print(f"IMPLEMENT(核心/节点悬空): {len(implement)}")
print(f"REMOVE(静默死, 零引用): {len(remove)}")
print()
print("=== IMPLEMENT (核心机制需补 reader / 节点悬空需裁定) ===")
for at, ids, why, c, n, j in implement:
    print(f"  {at:<42} [{why}] consumed={c} node={n} json={j} ids={ids[:3]}")
print()
print("=== REMOVE (静默死, 可清) ===")
for at, ids, c, n, j in remove:
    print(f"  {at:<42} consumed={c} node={n} json={j} ids={ids}")

out = {
    "total": len(attrs),
    "implement": [{"attr": a, "ids": i, "why": w} for a, i, w, *_ in implement],
    "remove": [{"attr": a, "ids": i} for a, i, *_ in remove],
}
with open(os.path.join(os.path.dirname(__file__), "normalize_classify.json"), "w", encoding="utf-8") as f:
    json.dump(out, f, indent=2, ensure_ascii=False)
print("\nwritten: poe2_assets/_test/normalize_classify.json")
