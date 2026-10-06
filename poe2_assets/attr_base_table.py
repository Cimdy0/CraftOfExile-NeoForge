#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Parse AttributeRegister: every attribute's DEFAULT BASE + [零基·禁inc] marker.

AttributeRegister "三定律" #3 (零基禁 inc):
  base==0 attributes may only use ADD_VALUE(pctAdd) / ADD_MULTIPLIED_TOTAL.
  ADD_MULTIPLIED_BASE folds to base*ΣmultBase == 0 -> the stat entry silently dies.

Therefore a CHECK-B "INC_* should be ADD_MULTIPLIED_BASE" report on a zero-base
attribute is an AUDITOR FALSE POSITIVE: the current wiring is the only viable one.
"""
import os, re, json
ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
P = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "AttributeRegister.java")
txt = open(P, "r", encoding="utf-8").read()

fields = {}
for m in re.finditer(r'(/\*\*(?:[^*]|\*(?!/))*\*/)?\s*public static Holder<Attribute> (\w+)\s*=\s*register(?:Double|Percentage|Boolean)\s*\((.*?)\);', txt, re.S):
    doc, name, args = m.group(1) or "", m.group(2), m.group(3)
    # strip the leading quoted name, then the first remaining number is the default base
    rest = re.sub(r'^\s*"[^"]*"\s*,?', '', args)
    n = re.search(r'-?\d+\.?\d*(?:[fF])?', rest)
    if not n: continue
    base = float(n.group(0).rstrip("fF"))
    fields[name] = dict(base=base, zero_marker="[零基" in doc, args=args.strip()[:52])

zero    = {k: v for k, v in fields.items() if v["base"] == 0}
nonzero = {k: v for k, v in fields.items() if v["base"] != 0}
print(f"attributes parsed: {len(fields)}    base==0 (零基): {len(zero)}    base!=0: {len(nonzero)}")
print(f"[零基*] markers: {sum(1 for v in fields.values() if v['zero_marker'])}\n")

dis = [(k, v) for k, v in fields.items() if v["zero_marker"] != (v["base"] == 0)]
print(f"marker/base disagreements: {len(dis)}")
for k, v in dis: print(f"    {k:32} base={v['base']}  marker={v['zero_marker']}")

print(f"\n--- base==0 : ADD_MULTIPLIED_BASE IS FATAL ({len(zero)}) ---")
for k in sorted(zero):
    print(f"    {k:32} base={zero[k]['base']}{'   <marked 零基>' if zero[k]['zero_marker'] else ''}")
print(f"\n--- base!=0 : ADD_MULTIPLIED_BASE viable ({len(nonzero)}) ---")
for k in sorted(nonzero):
    print(f"    {k:32} base={nonzero[k]['base']}")

json.dump(fields, open(os.path.join(ROOT,"poe2_assets","attr_base_table.json"),"w"), indent=1)
print("\nwrote poe2_assets/attr_base_table.json")
