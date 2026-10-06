# -*- coding: utf-8 -*-
import importlib.util, collections, re
spec = importlib.util.spec_from_file_location("s10","poe2_assets/step10_classify.py")
m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m)
ids=m.load_statids(); gen=m.load_generated()
# S1-P3: 描述无 {X} 但 amount=$value  →  判定注释形态
noX=[]
for name,path,cmt,ln in ids:
    j=gen.get(path)
    if not j:
        exp=sorted(k for k in gen if k.startswith(path+"_from_"))
        if exp: j=gen[exp[0]]
    if not j: continue
    amt=j.get("amount")
    if m.HAS_VAR.search(cmt): continue
    if cmt and isinstance(amt,str) and "$value" in amt and not m.CANNOT_RE.search(cmt):
        noX.append((name,cmt,ln))
print(f"S1-P3 total={len(noX)}")
# 注释形态分类
PAT=[("含具体数字%(如 '10% increased')", re.compile(r"\d+(\.\d+)?%")),
     ("含具体数字(无%)", re.compile(r"\b\d+(\.\d+)?\b")),
     ("占位/简写(无数字, <=4词)", re.compile(r"^(\S+\s+){0,3}\S+$")),
     ("其它散文", re.compile(r".")) ]
buck=collections.Counter(); sample=collections.defaultdict(list)
for name,cmt,ln in noX:
    for lab,rx in PAT:
        if rx.search(cmt):
            buck[lab]+=1
            if len(sample[lab])<6: sample[lab].append((name,cmt[:70]))
            break
for lab,c in buck.most_common():
    print(f"\n--- {lab}: {c}")
    for n,ct in sample[lab]: print(f"    {n}\n      > {ct}")
