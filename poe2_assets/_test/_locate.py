# -*- coding: utf-8 -*-
import importlib.util, re, collections
spec = importlib.util.spec_from_file_location("s10","poe2_assets/step10_classify.py")
m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m)
ids=m.load_statids(); gen=m.load_generated(); bases=m.load_attr_bases()
names=[]
for name,path,cmt,ln in ids:
    j=gen.get(path)
    if not j or j.get("type")!="attribute" or j.get("operation")!="add_multiplied_base": continue
    a=str(j.get("attribute","")); bi=bases.get(a)
    if bi and bi[1]==0.0: names.append((name,a))
src=open("src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java",encoding="utf-8").read().splitlines()
idx=collections.defaultdict(list)
for i,l in enumerate(src):
    for mm in re.finditer(r"StatIds\.([A-Z][A-Z0-9_]*)\b", l):
        idx[mm.group(1)].append((i+1,l.strip()))
form=collections.Counter()
for name,a in sorted(names,key=lambda x:x[1]):
    hits=idx.get(name,[])
    call=hits[0][1] if hits else "<NOT FOUND>"
    f=re.match(r"(\w+)\(", call)
    form[f.group(1) if f else "?"]+=1
    print(f"[{a}] {name}\n   L{hits[0][0] if hits else '?'}: {call}")
print("\n--- 调用形态汇总 ---")
for k,v in form.most_common(): print(f"{v:4d}  {k}")
