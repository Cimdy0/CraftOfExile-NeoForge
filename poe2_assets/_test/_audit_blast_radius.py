#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""聚焦核查：
   (a) 88 未实现 StatIds 的引用爆破半径（除 StatIds.java 自身外，是否还被 gen/talent 或其它代码引用）
   (b) 仅天赋接线的属性（T 非空 E 空 W 空）——live 仅当节点生效，若节点 inert 则是死通道
   (c) 仅 StatGen 接线 + 引擎注册但引擎不读属性值的“可疑通道”：粗略列出 W 非空且 E 仅出现在 AttributeRegister/talent 的属性
"""
import os, re, json

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SRC = os.path.join(ROOT, "src", "main", "java")
ATTR_FILE = os.path.join(SRC, "com", "cimdy", "coe", "data", "AttributeRegister.java")
SID_FILE = os.path.join(SRC, "com", "cimdy", "coe", "data", "codec", "support", "stat", "StatIds.java")
STATENTRY_DIR = os.path.join(ROOT, "src", "generated", "resources", "data", "coe", "coe", "stat_entry")

def read(p):
    with open(p, "r", encoding="utf-8", errors="ignore") as f:
        return f.read()

attr_src = read(ATTR_FILE)
attrs = {m.group(1): None for m in re.finditer(r"public static Holder<Attribute>\s+([A-Z_][A-Z0-9_]*)\s*=\s*register\w+\(", attr_src)}
sid_src = read(SID_FILE)
statids = {m.group(1): m.group(2) for m in re.finditer(r"public static final Identifier\s+([A-Z_][A-Z0-9_]*)\s*=\s*id\(\"([^\"]+)\"\)", sid_src)}
gen_ids = {fn[:-5] for fn in os.listdir(STATENTRY_DIR) if fn.endswith(".json")} if os.path.isdir(STATENTRY_DIR) else set()

unimpl = [sn for sn in statids if statids[sn] not in gen_ids]

# (a) blast radius of unimpl StatIds
sid_refs = {sn: set() for sn in unimpl}
attr_refs = {k: {"engine": set(), "statgen": set(), "talent": set()} for k in attrs}
for dp, _, fns in os.walk(SRC):
    for fn in fns:
        if not fn.endswith(".java"):
            continue
        fp = os.path.join(dp, fn)
        rel = os.path.relpath(fp, SRC)
        if rel == "com/cimdy/coe/data/codec/support/stat/StatIds.java":
            continue
        try:
            txt = read(fp)
        except Exception:
            continue
        for sn in unimpl:
            if f"StatIds.{sn}" in txt:
                sid_refs[sn].add(rel)
        for an in attrs:
            tok = f"AttributeRegister.{an}"
            if tok in txt:
                if "/gen/stat/" in rel:
                    attr_refs[an]["statgen"].add(rel)
                elif "/gen/talent/" in rel:
                    attr_refs[an]["talent"].add(rel)
                else:
                    attr_refs[an]["engine"].add(rel)

# (b) talent-only attributes
talent_only = [an for an, r in attr_refs.items() if r["talent"] and not r["engine"] and not r["statgen"]]

out = {
    "unimpl_sid_total": len(unimpl),
    "unimpl_sid_with_refs": {sn: sorted(v) for sn, v in sid_refs.items() if v},
    "unimpl_sid_with_refs_count": sum(1 for v in sid_refs.values() if v),
    "talent_only_attributes": sorted(talent_only),
}
with open(os.path.join(ROOT, "poe2_assets/_test/audit_blast_radius.json"), "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=1)

print("=" * 72)
print(f"未实现 StatIds={len(unimpl)}  其中仍被其它代码引用(删除爆破半径)={sum(1 for v in sid_refs.values() if v)}")
for sn in sorted(unimpl):
    if sid_refs[sn]:
        print(f"  REF  {sn} -> {statids[sn]}  refs={sorted(sid_refs[sn])}")
print(f"\n仅天赋接线的属性(引擎/StatGen均无引用, live仅当节点生效)={len(talent_only)}:")
for an in sorted(talent_only):
    print(f"    {an}")
