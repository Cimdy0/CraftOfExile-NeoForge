#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Inventory: per-attribute op-variant coverage in StatEffectGen.

For every attribute referenced by a ModifierStatEffect, list which Operation
variants exist (ADD_VALUE / ADD_MULTIPLIED_BASE / ADD_MULTIPLIED_TOTAL) together
with their tag scopes. Goal: find attributes that have a flat(ADD_VALUE) variant
but lack the inc(MULTIPLIED_BASE) / more(MULTIPLIED_TOTAL) variant => "true op
gap" (C5 model), which is the safe new-EFF-variant frontier.
"""
import os, re, json
ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
def read(p):
    with open(p, "r", encoding="utf-8") as f: return f.read()

eg = read(os.path.join(GEN, "StatEffectGen.java"))
rows = []
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\);', eg, re.S):
    name, body = m.group(1), m.group(2)
    am = re.search(r'AttributeRegister\.(\w+)', body)
    om = re.search(r'AttributeModifier\.Operation\.(\w+)', body)
    if not am or not om: continue
    if "TagExpr.ALWAYS" in body: tags = ["ALWAYS"]
    else:
        tm = re.search(r'TagExpr\.of\(Set\.of\((.*?)\)\)', body, re.S)
        tags = re.findall(r'StatTag\.(\w+)', tm.group(1)) if tm else ["<OTHER>"]
    rows.append((name, am.group(1), om.group(1), tuple(sorted(tags))))

OPS = ["ADD_VALUE", "ADD_MULTIPLIED_BASE", "ADD_MULTIPLIED_TOTAL"]
by_attr = {}
for n,a,o,t in rows:
    by_attr.setdefault(a, []).append((n,o,t))

# attributes used at all
attrs = sorted(by_attr)
print(f"ModifierStatEffect total={len(rows)}  distinct attributes={len(attrs)}")
print()
gap = {}
for a in attrs:
    have = {o for (_,o,_) in by_attr[a]}
    missing = [o for o in OPS if o not in have]
    gap[a] = (sorted(have), missing)

full = [a for a in attrs if not gap[a][1]]
partial = [a for a in attrs if gap[a][1]]
print(f"[full coverage 3/3] {len(full)}")
for a in full: print("   ", a)
print()
print(f"[partial] {len(partial)}")
for a in partial:
    have, missing = gap[a]
    print(f"   {a:44} have={have} missing={missing}")
json.dump({a:{"have":gap[a][0],"missing":gap[a][1],
              "effs":[{"name":n,"op":o,"tags":list(t)} for n,o,t in by_attr[a]]}
           for a in attrs},
          open(os.path.join(ROOT,"poe2_assets","opgap_inventory.json"),"w"), indent=1)
print("\nwrote poe2_assets/opgap_inventory.json")
