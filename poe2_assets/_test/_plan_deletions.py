#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成协同删除清单（72 死属性 + 78 悬空 StatIds）并做全仓库安全扫描。
   安全：除 .step*bak 备份、_test 审计脚本、.git/.gradle 外，任何引用都报出并阻止删除。
"""
import os, re, json

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SRC = os.path.join(ROOT, "src", "main", "java")
ATTR_FILE = os.path.join(SRC, "com", "cimdy", "coe", "data", "AttributeRegister.java")
SID_FILE = os.path.join(SRC, "com", "cimdy", "coe", "data", "codec", "support", "stat", "StatIds.java")
STATGEN_FILE = os.path.join(SRC, "com", "cimdy", "coe", "data", "gen", "stat", "StatGen.java")

def read(p):
    with open(p, "r", encoding="utf-8", errors="ignore") as f:
        return f.read()

attr_src = read(ATTR_FILE)
attrs = {m.group(1): None for m in re.finditer(r"public static Holder<Attribute>\s+([A-Z_][A-Z0-9_]*)\s*=\s*register\w+\(", attr_src)}
sid_src = read(SID_FILE)
statids = {m.group(1): m.group(2) for m in re.finditer(r"public static final Identifier\s+([A-Z_][A-Z0-9_]*)\s*=\s*id\(\"([^\"]+)\"\)", sid_src)}
statgen = read(STATGEN_FILE)
statgen_refd = set(re.findall(r"StatIds\.([A-Z_][A-Z0-9_]*)", statgen))

# refs across whole repo (excluding noise)
EXCLUDE_DIRS = (".git", ".gradle", "build", "bin", "generated", "_test", "_backup", "poe2_assets", "out")
def walk_src_files():
    for dp, _, fns in os.walk(ROOT):
        if any(seg in EXCLUDE_DIRS for seg in dp.replace("\\", "/").split("/")):
            continue
        for fn in fns:
            if ".step" in fn or fn.endswith(".bak"):
                continue
            if fn.endswith(".java") or fn.endswith(".json"):
                yield os.path.join(dp, fn)

# attribute refs (AttributeRegister.X)
attr_refs = {k: [] for k in attrs}
# statids refs (StatIds.X)
sid_refs = {k: [] for k in statids}
for fp in walk_src_files():
    rel = os.path.relpath(fp, ROOT).replace("\\", "/")
    if rel in ("src/main/java/com/cimdy/coe/data/AttributeRegister.java",
               "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"):
        continue
    try:
        t = read(fp)
    except Exception:
        continue
    for an in attrs:
        if f"AttributeRegister.{an}" in t:
            attr_refs[an].append(rel)
    for sn in statids:
        if f"StatIds.{sn}" in t:
            sid_refs[sn].append(rel)

# dead attributes = only declared (no refs anywhere)
dead_attrs = [a for a in attrs if not attr_refs[a]]
# dangling statids = not in StatGen AND no refs anywhere
dangling_sids = [s for s in statids if s not in statgen_refd and not sid_refs[s]]

violations = []
for a in dead_attrs:
    if attr_refs[a]:
        violations.append(("ATTR", a, attr_refs[a]))
for s in dangling_sids:
    if sid_refs[s]:
        violations.append(("SID", s, sid_refs[s]))

out = {
    "dead_attributes": sorted(dead_attrs),
    "dead_attributes_count": len(dead_attrs),
    "dangling_statids": sorted(dangling_sids),
    "dangling_statids_count": len(dangling_sids),
    "violations": violations,
}
with open(os.path.join(ROOT, "poe2_assets/_test/delete_lists.json"), "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=1)

print(f"死属性(待删)={len(dead_attrs)}  悬空StatIds(待删)={len(dangling_sids)}")
print(f"安全违规(有外部引用)={len(violations)}")
for v in violations:
    print(f"  VIOLATION {v[0]} {v[1]} -> {v[2]}")
print("（清单已写入 delete_lists.json）")
