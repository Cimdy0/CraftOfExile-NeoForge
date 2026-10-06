# -*- coding: utf-8 -*-
import sys, os, json, re, collections
sys.path.insert(0, "poe2_assets")
import importlib.util
spec = importlib.util.spec_from_file_location("s10", "poe2_assets/step10_classify.py")
m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m)
ids = m.load_statids(); gen = m.load_generated(); bases = m.load_attr_bases()
rows=[]
for name, path, cmt, ln in ids:
    j = gen.get(path)
    if not j: continue
    if j.get("type")!="attribute" or j.get("operation")!="add_multiplied_base": continue
    a=str(j.get("attribute","")); bi=bases.get(a)
    if not bi or bi[1]!=0.0: continue
    rows.append((bi[0], a, name, cmt[:95], ln))
rows.sort(key=lambda x:(x[0],x[1],x[2]))
print(f"A1 total = {len(rows)}\n")
cur=None
for kind,a,name,cmt,ln in rows:
    if (kind,a)!=cur:
        cur=(kind,a); print(f"\n=== [{kind}] {a} ===")
    print(f"  L{ln:<5} {name}\n         > {cmt}")
print("\n--- kind 汇总 ---")
for k,v in collections.Counter(r[0] for r in rows).items(): print(f"{v:4d}  {k}")
