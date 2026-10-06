#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""判定可安全移除的占位属性批次:
   占位(引擎零消费) AND 其 StatGen 条目零 talent 节点引用 AND 属性无其他 StatGen 条目引用
   => 条目+属性可干净移除; StatIds 常量保留(预留词表, 不 dangling).
"""
import re, os, json, pathlib, subprocess

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))  # -> CraftOfExile-NeoForge
SRC = os.path.join(ROOT, "src", "main", "java")
STATGEN = os.path.join(SRC, "com", "cimdy", "coe", "data", "gen", "stat", "StatGen.java")
ATTR = os.path.join(SRC, "com", "cimdy", "coe", "data", "AttributeRegister.java")
TALENT = os.path.join(SRC, "com", "cimdy", "coe", "data", "gen", "talent")

# 1) parse attribute list + base
attrs = {}
pat = re.compile(r'public static Holder<Attribute>\s+([A-Za-z0-9_]+)\s*=\s*register(Percentage|Double|Flat)?\(\s*"([^"]+)"\s*,\s*([0-9.]+)')
with open(ATTR, encoding="utf-8") as f:
    for line in f:
        m = pat.search(line)
        if m:
            attrs[m.group(1)] = {"id": m.group(3), "base": float(m.group(4))}

# 2) engine consumption (pure python, exclude AttributeRegister + StatGen)
src_root = pathlib.Path(SRC)
files = [str(p) for p in src_root.rglob("*.java")]
consumed = {}
for n in attrs:
    pn = re.compile(r"AttributeRegister\." + re.escape(n) + r"\b")
    cnt = 0
    for fp in files:
        if fp.endswith("AttributeRegister.java") or fp.endswith("StatGen.java"):
            continue
        try:
            with open(fp, encoding="utf-8", errors="ignore") as fh:
                if any(pn.search(l) for l in fh):
                    cnt += 1
        except Exception:
            pass
    consumed[n] = cnt

# 3) parse StatGen entries: (StatIds.ID, AttributeRegister.ATTR)
entry_re = re.compile(r'\b(?:inc|add|pctAdd|more|chance|faceMod|valueMod|entityMod|regenMod|dtcArmour|dtcReroute|esRechargeMod)\(\s*map\s*,\s*StatIds\.(\w+)\s*,\s*AttributeRegister\.(\w+)')
sg = open(STATGEN, encoding="utf-8").read()
entries = entry_re.findall(sg)  # list of (ID, ATTR)
attr_to_ids = {}
id_to_attr = {}
for idn, at in entries:
    attr_to_ids.setdefault(at, []).append(idn)
    id_to_attr[idn] = at

# 4) node references: for each StatIds ID, grep talent dir
def node_refs(idn):
    r = subprocess.run(["grep", "-rln", re.escape(idn), TALENT], capture_output=True, text=True)
    return [x for x in r.stdout.splitlines() if x.strip()]

# 5) classify placeholders
placeholder = [n for n in attrs if consumed[n] == 0]
safe_remove = []   # (attr, ids, reason)
keep_todo = []     # (attr, ids, reason)
for at in placeholder:
    ids = attr_to_ids.get(at, [])
    if not ids:
        # 纯预留属性(无条目) -> 保留为预留, 不动作
        continue
    # 属性是否被其它条目引用(多条目共享)
    other_entries = [e for e in entries if e[1] == at]
    # 所有 id 的节点引用
    total_node_refs = 0
    ref_map = {}
    for idn in ids:
        rf = node_refs(idn)
        ref_map[idn] = len(rf)
        total_node_refs += len(rf)
    # 属性是否除本条目外被引擎外的其它 java 引用(已在 consumed 计, consumed==0 即无)
    if total_node_refs == 0 and consumed[at] == 0 and len(other_entries) == len(ids):
        safe_remove.append((at, ids, ref_map))
    else:
        keep_todo.append((at, ids, {"node_refs": total_node_refs, "other_entries": len(other_entries)-len(ids)}))

print(f"占位属性总数(无引擎消费): {len(placeholder)}")
print(f"  其中有 StatGen 条目的: {sum(1 for at in placeholder if attr_to_ids.get(at))}")
print(f"可安全移除(零节点引用 且 属性零其他引用): {len(safe_remove)}")
print(f"需保留TODO/进一步裁定: {len(keep_todo)}")
print()
print("=== 可安全移除批次 ===")
for at, ids, rm in sorted(safe_remove, key=lambda x: x[0]):
    print(f"  {at:<42} ids={ids}")
print()
print("=== 需保留TODO/裁定 (前40) ===")
for at, ids, info in sorted(keep_todo, key=lambda x: x[0])[:40]:
    print(f"  {at:<42} node_refs={info['node_refs']} other_entries={info['other_entries']} ids={ids[:2]}")

out = {
    "placeholder_total": len(placeholder),
    "with_entries": sum(1 for at in placeholder if attr_to_ids.get(at)),
    "safe_remove": [{"attr": a, "ids": i} for a, i, _ in safe_remove],
    "keep_todo_count": len(keep_todo),
}
with open(os.path.join(os.path.dirname(__file__), "safe_remove_batch.json"), "w", encoding="utf-8") as f:
    json.dump(out, f, indent=2, ensure_ascii=False)
print("\nwritten: poe2_assets/_test/safe_remove_batch.json")
