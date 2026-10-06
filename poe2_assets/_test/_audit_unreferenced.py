#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""修正版三证据交叉审计（真值：生成的 stat_entry JSON 文件名 = StatIds 是否真有条目）。
   A = AttributeRegister 属性
   E = 引擎消费（event/skill/helper/data，非 gen/、非 AttributeRegister 自身）——真正读取属性值
   W = StatGen 接线（StatGen.java 引用 AttributeRegister.X）
   T = 天赋接线（gen/talent 引用 AttributeRegister.X）
   S = StatIds 常量; S_impl = id 串存在于生成的 stat_entry/*.json 文件名
分类：
   LIVE   = E 或 T 非空（被引擎/天赋真实消费）
   CHANNEL= W 非空 且 E 空 且 T 空（仅 StatGen 接线、引擎/天赋从不读其值=不必要的通道接线）
   DEAD   = 仅 AttributeRegister 自身声明（完全未引用也未移除）
   UNIMPL_SID = StatIds 常量但无生成条目（未实现）
scope-baked 启发：属性名含作用域/条件焊词 → 非归一候选
"""
import os, re, json

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SRC = os.path.join(ROOT, "src", "main", "java")
ATTR_FILE = os.path.join(SRC, "com", "cimdy", "coe", "data", "AttributeRegister.java")
STATIDS_FILE = os.path.join(SRC, "com", "cimdy", "coe", "data", "codec", "support", "stat", "StatIds.java")
STATGEN_FILE = os.path.join(SRC, "com", "cimdy", "coe", "data", "gen", "stat", "StatGen.java")
STATENTRY_DIR = os.path.join(ROOT, "src", "generated", "resources", "data", "coe", "coe", "stat_entry")

def read(p):
    with open(p, "r", encoding="utf-8", errors="ignore") as f:
        return f.read()

# 1) attributes
attr_src = read(ATTR_FILE)
attr_re = re.compile(r"public static Holder<Attribute>\s+([A-Z_][A-Z0-9_]*)\s*=\s*(register\w+)\(")
attrs = {m.group(1): m.group(2) for m in attr_re.finditer(attr_src)}
print(f"[parse] attributes={len(attrs)}")

# 2) StatIds
sid_src = read(STATIDS_FILE)
sid_re = re.compile(r"public static final Identifier\s+([A-Z_][A-Z0-9_]*)\s*=\s*id\(\"([^\"]+)\"\)")
statids = {m.group(1): m.group(2) for m in sid_re.finditer(sid_src)}
print(f"[parse] statids={len(statids)}")

# 3) generated stat_entry id set (truth for implemented)
gen_ids = set()
if os.path.isdir(STATENTRY_DIR):
    for fn in os.listdir(STATENTRY_DIR):
        if fn.endswith(".json"):
            gen_ids.add(fn[:-5])
print(f"[parse] generated stat_entry json={len(gen_ids)}")

# 4) walk refs
attr_refs = {k: {"engine": set(), "statgen": set(), "talent": set(), "decl": set()} for k in attrs}
for dp, _, fns in os.walk(SRC):
    for fn in fns:
        if not fn.endswith(".java"):
            continue
        fp = os.path.join(dp, fn)
        rel = os.path.relpath(fp, SRC)
        if rel == "com/cimdy/coe/data/AttributeRegister.java":
            continue  # decl handled separately
        try:
            txt = read(fp)
        except Exception:
            continue
        for an in attrs:
            tok = f"AttributeRegister.{an}"
            if tok in txt:
                if rel.endswith("data/gen/stat/StatGen.java") or "/gen/stat/" in rel:
                    attr_refs[an]["statgen"].add(rel)
                elif "/gen/talent/" in rel:
                    attr_refs[an]["talent"].add(rel)
                else:
                    attr_refs[an]["engine"].add(rel)

# 5) classify
LIVE, CHANNEL, DEAD = [], [], []
for an, r in attr_refs.items():
    if r["engine"] or r["talent"]:
        LIVE.append(an)
    elif r["statgen"]:
        CHANNEL.append(an)
    else:
        DEAD.append(an)

# 6) StatIds implemented via generated json
impl = [sn for sn in statids if statids[sn] in gen_ids]
unimpl = [sn for sn in statids if statids[sn] not in gen_ids]

# scope-baked heuristic
BAKE = re.compile(r"_(SLAM|MACE|FORM|SHAPESHIFT|RECENT|PROJECTILE|CONDITION|WHILE|WHEN|IF_|YOU_|SKILL|AURA|MINION|BEAR|WOLF|SHIELD|TRAP|TOTEM|WHEN_YOU|AFTER_|WITH_|OF_|FROM_)")
dead_baked = [a for a in DEAD if BAKE.search(a)]
dead_plain = [a for a in DEAD if not BAKE.search(a)]

out = {
    "totals": {
        "attributes": len(attrs), "live": len(LIVE), "channel": len(CHANNEL), "dead": len(DEAD),
        "statids": len(statids), "statids_impl": len(impl), "statids_unimpl": len(unimpl),
        "dead_scope_baked": len(dead_baked), "dead_plain": len(dead_plain),
    },
    "channel_attributes": sorted(CHANNEL),
    "dead_attributes": sorted(DEAD),
    "dead_scope_baked": sorted(dead_baked),
    "dead_plain": sorted(dead_plain),
    "statids_unimpl_count": len(unimpl),
}
with open(os.path.join(ROOT, "poe2_assets/_test/audit_unreferenced.json"), "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=1)

print("=" * 72)
print(f"属性: {len(attrs)}  LIVE={len(LIVE)}  CHANNEL(不必要通道)={len(CHANNEL)}  DEAD(未引用未移除)={len(DEAD)}")
print(f"DEAD 中 作用域焊死(非归一)={len(dead_baked)}  普通未实现={len(dead_plain)}")
print(f"StatIds: {len(statids)}  已实现(有生成条目)={len(impl)}  未实现={len(unimpl)}")
print("=" * 72)
print(f"\n### CHANNEL 不必要的通道接线 ({len(CHANNEL)}):")
for a in sorted(CHANNEL):
    r = attr_refs[a]
    print(f"  {a}  statgen={len(r['statgen'])} engine={len(r['engine'])} talent={len(r['talent'])}")
print(f"\n### DEAD 属性 ({len(DEAD)}) — 未引用也未移除:")
print("  -- 作用域焊死(非归一) --")
for a in sorted(dead_baked):
    print(f"    {a}  [{attrs[a]}]")
print("  -- 普通未实现 --")
for a in sorted(dead_plain):
    print(f"    {a}  [{attrs[a]}]")
print(f"\n未实现 StatIds 总数={len(unimpl)}  (样例前30)")
for sn in sorted(unimpl)[:30]:
    print(f"    {sn} -> {statids[sn]}")
